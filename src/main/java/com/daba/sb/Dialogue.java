package com.daba.sb;

import com.daba.sb.model.Alignment;
import com.daba.sb.model.board.Dot;
import com.daba.sb.util.BoardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/*
Это - самый абстрактный класс приложения. Он инкапсулирует такую сущность, как диалог с пользователем.
Наше приложение консольное, а значит, общается с пользователем посредством вывода данных на консоль и чтения оттуда же.
Разумеется, мы могли бы везде, где требуется, использовать отдельный экземпляр Scanner и писать в логи через @Slf4J, но это плохое решение по нескольким причинам:
1. Консоль у нас физически одна, и несколько отдельных сканеров, читающих из нее - верный путь к инконсистенциям. В однопоточном окружении получить их будет сложно,
но общий принцип "пиши так, чтобы переход к многопоточке не вызвал архитектурных проблем" весьма полезен. Сейчас у нас одна консоль и одна точка доступа к ней
в виде этого класса (обратите внимание, он сделан синглтоном).
2. Сейчас мы используем стандартную консоль. В дальнейшем при эволюции приложения мы можем захотеть ввести новый способ взаимодействия с пользователем.
Если инкапсулировать всю логику взаимодействия в одном классе, придется менять только его, не трогая прочую кодовую базу, что несоизмеримо удобнее, чем искать
по-отдельности каждый случай вывода в лог и использования сканера.
3. Как видно из кодовой базы, простого использования сканера мало - надо валидировать вводимые значения, не позволять вводить пустоту, иногда позволять вводить
только определенные варианты. Реализовывать такие проверки на каждом вводе отдельно, по месту - ужасный подход, многократное дублирования одного и того же
простого кода. Лучше инкапсулировать этот общий функционал здесь.
 */
@Slf4j
@RequiredArgsConstructor
public class Dialogue {

    /*
    Пользователю позволено вводить глобальные команды в любое время, когда игра ожидает от него ввода.
    Глобальная команда определяется по префиксу :
    Любые фиксированные значения, используемые в коде, выделяются в константы.
     */
    private static final String GLOBAL_COMMAND_PREFIX = ":";

    /*
    См. readVariant/readYesNo
    Имеет смысл выделить стандартный ответ да/нет в утильный метод
     */
    private static final Map<String, Supplier<Boolean>> YES_NO = Map.of(
            "y", () -> true, "Y", () -> true, "yes", () -> true, "Yes", () -> true,
            "n", () -> false, "N", () -> false, "no", () -> false, "No", () -> false
    );

    /*
    См. readVariant/readAlignment
    Ввод варианта вертикальный/горизонтальный - стандартный в терминологии игры, и поэтому выделен в отдельный метод в общем классе,
    несмотря на то, что используется в данный момент всего в одном месте
     */
    private static final Map<String, Supplier<Alignment>> ALIGNMENTS = Map.of(
            "V", () -> Alignment.VERTICAL, "v", () -> Alignment.VERTICAL,
            "H", () -> Alignment.HORIZONTAL, "h", () -> Alignment.HORIZONTAL);

    /*
    Dialogue - синглтон
     */
    private static Dialogue instance;

    /*
    Суть диалога - во взаимодействии с консолью, это неотъемлемая его часть.
    Поэтому диалог имеет собственный scanner. Так как класс - синглтон, а все взаимодействия с консолью ведутся через него,
    сканер у нас тоже будет один на все приложение
     */
    private final Scanner scanner;

    /*
    В эту мапу будут добавляться глобальные команды, которые игрок сможет вводить в любое время
    Их список общий на все приложение, значит, можно обойтись одной мапой
     */
    private final Map<String, Consumer<Dialogue>> GLOBAL_COMMANDS;
    /*
    Здесь хранятся описания глобальных команд, чтобы можно было вывести в консоль красивый список
    Можно было бы обойтись одной мапой, заведя класс, хранящий саму команду и её описание, и иметь мапу
    вида команда -> такой класс, но я посчитал это излишним усложнением
     */
    private final Map<String, String> GLOBAL_COMMANDS_DESCRIPTIONS;

    /*
    Здесь хранится последний запрос, сделанный пользователю через диалог. Нужно, чтобы по команде его выдавать,
    например, если пользователь забыл, а консоль убежала далеко вниз
     */
    private String currentRequest;

    /*
    Это - метод getInstance синглтона. Назван join, чтобы красивее выглядело использование: Dialogue.join().say(...)
    Не стесняйтесь давать методам имена, имеющие смысл в том месте, где вы их будете использовать.
    Если в месте декларации метода имя не вполне понятно (как здесь), всегда можно написать к нему джавадок, чего здесь не сделано :)
     */
    public static Dialogue join() {
        /*
        Обратите внимание - этот синглтон не потокобезопасен и так писать getInstance можно только в гарантированно однопоточном окружении!
        См. больше здесь: https://habr.com/ru/post/129494/
         */
        if (instance == null) {
            instance = new Dialogue(new Scanner(System.in), new HashMap<>(), new HashMap<>());
        }
        return instance;
    }

    /*
    Метод позволяет зарегистрировать глобальную команду. Его вызывает класс Game на этапе приготовлений игры.
     */
    public void addGlobalCommand(String command, String description, Consumer<Dialogue> handle) {
        // Кладем команду
        GLOBAL_COMMANDS.put(GLOBAL_COMMAND_PREFIX + command, handle);
        // И её описание
        GLOBAL_COMMANDS_DESCRIPTIONS.put(GLOBAL_COMMAND_PREFIX + command, description);
        // Использовать это будет метод queryUntilSuccess
    }

    /*
    Это абстрактный метод, который инкапсулирует в себе две вещи:
    1. Проверка, не глобальная ли команда введена и исполнение этой команды
    2. Перезапрос значения у игрока до тех пор, пока игру не устроит результат
    Это базовый метод, допускающий максимальную вариативность. Его вызывают все прочие методы этого класса, добавляя деталей.
    Он сделан public на случай, если кому-то понадобится большая гибкость, чем дают прочие методы этого класса.
    Это можно сделать, так как сам по себе этот метод не дает доступа ко внутренним деталям класса.
     */
    public <I, R> R queryUntilSuccess(
            // Что игра должна спросить у игрока
            String requestMessage,
            // Как игра должна получить значение
            Supplier<String> input,
            // Как игра должна понять, хорошее ли это значение
            Predicate<String> test,
            // Что игра должна сделать с введенной строкой, чтобы получить нужный результат
            Function<String, R> processor,
            // Что игра должна сказать игроку, если test провалился и нужно ввести значение еще раз
            String failedTestMessage) {
        // Сохраняем запрос в поле, это позволит перевывести его по отдельной команде
        // Каждый следующий вызов метода будет обновлять поле, так что, там всегда будет последний запрос
        this.currentRequest = requestMessage;
        // Говорим приглашение
        say(requestMessage);
        // Когда тест пройдет, выйдем из цикла. А если не будет проходить, будем крутиться вечно, перезапрашивая значение
        while (true) {
            // Получаем значение
            var attempt = input.get();
            // Если это глобальная команда, то обработаем её в отдельном методе
            if (handleGlobalCommand(attempt, requestMessage)) {
                // и запустим цикл заново, потому что игре все еще нужно получить ответ на изначальный запрос
                continue;
            }
            // Если дошли сюда, значит введенное - не глобальная команда (иначе был бы continue). Проверим введенное на правильность
            if (test.test(attempt)) {
                // Проверка прошла, можно занулить запрос, потому что он выполнен и запроса больше нет
                this.currentRequest = null;
                // Вызываем процессор, чтобы получить обработанный результат и возвращаем его
                return processor.apply(attempt);
            }
            // Если дошли сюда, значит, тест провалился. Говорим об этом заготовленной фразой и идем на следующую итерацию, спрашивать по-новой
            say(failedTestMessage);
        }
    }

    /*
    Сюда я вынес обработку глобальных команд. Этот метод вызывается ровно из одного места, и вынесен, чтобы не загромождать
    queryUntilSuccess обработкой, которая логически выделяется в отдельную операцию
     */
    private boolean handleGlobalCommand(String attempt, String requestMessage) {
        if (attempt.startsWith(GLOBAL_COMMAND_PREFIX)) {
            var globalCommand = GLOBAL_COMMANDS.get(attempt);
            if (globalCommand == null) {
                say("The game does not know such command");
                // Если говорим, что не нашли команду, то неплохо бы рассказать, какие есть, а то получается не user friendly
                reportGlobalCommands();
            } else {
                // Команда принимают экземпляр диалога. Поскольку он один, а мы уже в нем, можно просто передать себя.
                // Передавать команде диалог логично, поскольку команда, введенная в диалог, с высокой вероятностью подразумевает
                // наличие ответа, возвращенного в тот же диалог. Чтобы не заставлять реализацию каждой команды получать
                // диалог вручную, удобно заложить его в качестве аргумента на уровне контракта
                globalCommand.accept(this);
            }
            // Команда выполнена, но запрос был на что-то иное. Надо напомнить пользователю, что игра все еще ожидает его ответа
            say("Game still needs your answer: {}", requestMessage);
            return true;
        }
        return false;
    }

    /*
    Стандартный метод на стандартную задачу "прочитай число из консоли"
    Пока не используется, оставлен на будущее.
    Иногда стандарты кодовой базы диктуют, что нельзя оставлять в ней неиспользуемые методы, мол, когда понадобится, тогда и напишем.
    Это верно для кода, контекст которого может быть неочевиден в будущем. Написали метод под целое число, а может, нужно будет не число, и не целое,
    и не вводить, а выводить.
    В данном случае, я считаю, контекст применения гипотетического функционала "введи целое число" для консольной игры достаточно очевиден,
    чтобы потомки догадались, как применить метод, поэтому решил его оставить.
     */
    public Integer readNumber(String requestMessage) {
        return queryUntilSuccess(
                requestMessage,
                scanner::nextLine,
                NumberUtils::isParsable,
                Integer::parseInt,
                "Entered value is not an integer. Please enter again");
    }

    // Метод для чтения неотрицательных чисел
    public Integer readPositiveNumber(String requestMessage) {
        return queryUntilSuccess(
                requestMessage,
                scanner::nextLine,
                // Изучите подробнее методы класса NumberUtils - isParseable там не единственный вариант теста на "числовость"
                // строки, и между вариантами - тонкая разница. Подробнее: https://www.baeldung.com/java-check-string-number
                (s) -> NumberUtils.isParsable(s) && Integer.parseInt(s) > 0,
                Integer::parseInt,
                "Entered value is not a positive integer. Please enter again");
    }

    // Метод для чтения непустой строки из консоли
    public String readString(String requestMessage) {
        return queryUntilSuccess(
                requestMessage,
                scanner::nextLine,
                StringUtils::isNotEmpty,
                Function.identity(),
                "Entered value is empty. Please enter again");
    }

    // Функционал для чтения точки на поле формата "А2", "B7" и подобных. Используется много где, вынесен в отдельный метож
    public Dot readDotFromScanner(String requestMessage, int boardSize) {
        return queryUntilSuccess(
                requestMessage,
                scanner::nextLine,
                (raw) -> BoardUtils.isCorrectDot(raw, boardSize),
                (raw) -> BoardUtils.createDot(raw, boardSize),
                "Dot is incorrect for given board. Please enter again"
        );
    }

    /*
    Метод для чтения одного из предложенных вариантов. Для каждого варианта сопоставляет supplier, возвращающий значение.
    Позволять задавать supplier - более гибко, чем просто сопоставлять вариант со статичным значением, ведь supplier
    может реализовывать логику, захватывая в себя локальные переменные из контекста своего объявления и полагаясь на них
     */
    public <O> O readVariant(String requestMessage, Map<String, Supplier<O>> mappers) {
        if (mappers == null || mappers.size() == 0) {
            return null;
        }
        return queryUntilSuccess(
                // Модифицируем приглашение, добавляя в него варианты, чтобы игрок всегда знал, из чего он выбирает
                requestMessage + "\n Variants are: " + String.join(", ", mappers.keySet()),
                scanner::nextLine,
                // Вариант корректен, если мапа содержит для него supplier
                mappers::containsKey,
                (raw) -> mappers.get(raw).get(),
                "Given variant is not in the list. Please enter again");
    }

    // Короткий метод для чтения "да или нет"
    public boolean readYesNo(String requestMessage) {
        return readVariant(requestMessage, YES_NO);
    }

    // Короткий метод для чтения "вертикальный или горизонтальный"
    public Alignment readAlignment(String requestMessage) {
        return readVariant(requestMessage, ALIGNMENTS);
    }

    // До этого был ввод, теперь вывод. С ним все проще - есть просто "сказать"
    // Сейчас реализация выводит в консоль. Если появится другой метод вывода, изменить поведение всего приложения разом можно одной правкой здесь
    public void say(String pattern, Object... args) {
        log.info(pattern, args);
    }

    // Рассказать игроку о том, какие есть команды
    public void reportGlobalCommands() {
        if (GLOBAL_COMMANDS.size() == 0) {
            // Без этого ифа игрок увидит пустоту в консоле и может не понять, что команд нет, а не игра зависла
            say("No global commands available");
            return;
        }
        say("Available commands are: \n{}",
                GLOBAL_COMMANDS.keySet().stream()
                        .map(c -> c + " - " + GLOBAL_COMMANDS_DESCRIPTIONS.get(c))
                        .collect(Collectors.joining("\n")));
    }

    // Метод для того, чтобы повторить игроку текущий запрос
    public void reportCurrentRequest() {
        say("Current request is: {}", currentRequest);
    }

}
