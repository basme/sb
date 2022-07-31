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

@Slf4j
@RequiredArgsConstructor
public class Dialogue {

    private static final String GLOBAL_COMMAND_PREFIX = ":";

    private static final Map<String, Supplier<Boolean>> YES_NO = Map.of(
            "y", () -> true, "Y", () -> true, "yes", () -> true, "Yes", () -> true,
            "n", () -> false, "N", () -> false, "no", () -> false, "No", () -> false
    );

    private static final Map<String, Supplier<Alignment>> ALIGNMENTS = Map.of(
            "V", () -> Alignment.VERTICAL, "v", () -> Alignment.VERTICAL,
            "H", () -> Alignment.HORIZONTAL, "h", () -> Alignment.HORIZONTAL);

    private static Dialogue instance;

    private final Scanner scanner;

    private final Map<String, Consumer<Dialogue>> GLOBAL_COMMANDS;
    private final Map<String, String> GLOBAL_COMMANDS_DESCRIPTIONS;

    private String currentRequest;

    public static Dialogue join() {
        if (instance == null) {
            instance = new Dialogue(new Scanner(System.in), new HashMap<>(), new HashMap<>());
        }
        return instance;
    }

    public void addGlobalCommand(String command, String description, Consumer<Dialogue> handle) {
        GLOBAL_COMMANDS.put(GLOBAL_COMMAND_PREFIX + command, handle);
        GLOBAL_COMMANDS_DESCRIPTIONS.put(GLOBAL_COMMAND_PREFIX + command, description);
    }

    public <I, R> R queryUntilSuccess(
            String requestMessage,
            Supplier<String> input,
            Predicate<String> test,
            Function<String, R> processor,
            String failedTestMessage) {
        this.currentRequest = requestMessage;
        say(requestMessage);
        while (true) {
            var attempt = input.get();
            if (handleGlobalCommand(attempt, requestMessage)) {
                continue;
            }
            if (test.test(attempt)) {
                this.currentRequest = null;
                return processor.apply(attempt);
            }
            say(failedTestMessage);
        }
    }

    private boolean handleGlobalCommand(String attempt, String requestMessage) {
        if (attempt.startsWith(GLOBAL_COMMAND_PREFIX)) {
            var globalCommand = GLOBAL_COMMANDS.get(attempt);
            if (globalCommand == null) {
                say("The game does not know such command");
                reportGlobalCommands();
            } else {
                globalCommand.accept(this);
            }
            say("Game still needs your answer: {}", requestMessage);
            return true;
        }
        return false;
    }

    public Integer readNumber(String requestMessage) {
        return queryUntilSuccess(
                requestMessage,
                scanner::nextLine,
                NumberUtils::isParsable,
                Integer::parseInt,
                "Entered value is not an integer. Please enter again");
    }

    public Integer readPositiveNumber(String requestMessage) {
        return queryUntilSuccess(
                requestMessage,
                scanner::nextLine,
                (s) -> NumberUtils.isParsable(s) && Integer.parseInt(s) > 0,
                Integer::parseInt,
                "Entered value is not a positive integer. Please enter again");
    }

    public String readString(String requestMessage) {
        return queryUntilSuccess(
                requestMessage,
                scanner::nextLine,
                StringUtils::isNotEmpty,
                Function.identity(),
                "Entered value is empty. Please enter again");
    }

    public Dot readDotFromScanner(String requestMessage, int boardSize) {
        return queryUntilSuccess(
                requestMessage,
                scanner::nextLine,
                (raw) -> BoardUtils.isCorrectDot(raw, boardSize),
                (raw) -> BoardUtils.createDot(raw, boardSize),
                "Dot is incorrect for given board. Please enter again"
        );
    }

    public <O> O readVariant(String requestMessage, Map<String, Supplier<O>> mappers) {
        if (mappers == null || mappers.size() == 0) {
            return null;
        }
        return queryUntilSuccess(
                requestMessage + "\n Variants are: " + String.join(", ", mappers.keySet()),
                scanner::nextLine,
                mappers::containsKey,
                (raw) -> mappers.get(raw).get(),
                "Given variant is not in the list. Please enter again");
    }

    public boolean readYesNo(String requestMessage) {
        return readVariant(requestMessage, YES_NO);
    }

    public Alignment readAlignment(String requestMessage) {
        return readVariant(requestMessage, ALIGNMENTS);
    }

    public void say(String pattern, Object... args) {
        log.info(pattern, args);
    }

    public void reportGlobalCommands() {
        if (GLOBAL_COMMANDS.size() == 0) {
            say("No global commands available");
            return;
        }
        say("Available commands are: \n{}",
                GLOBAL_COMMANDS.keySet().stream()
                        .map(c -> c + " - " + GLOBAL_COMMANDS_DESCRIPTIONS.get(c))
                        .collect(Collectors.joining("\n")));
    }

    public void reportCurrentRequest() {
        say("Current request is: {}", currentRequest);
    }

}
