package com.daba.sb.process;

import com.daba.sb.process.move.impl.AiMove;
import com.daba.sb.model.board.Board;
import com.daba.sb.Dialogue;
import com.daba.sb.model.GameContext;
import com.daba.sb.model.process.GameStage;
import com.daba.sb.util.GlobalCommandsAssist;
import com.daba.sb.process.move.impl.HumanMove;
import com.daba.sb.model.Player;
import com.daba.sb.model.ShipConfig;

import java.util.List;

public class Game {

    private final StepMachine stepMachine;

    private final Dialogue dialogue;

    private final GameContext context;

    private final ShipsOperations shipsOperations;

    public Game() {
        this.stepMachine = new StepMachine();
        this.dialogue = Dialogue.join();
        this.shipsOperations = new ShipsOperations();
        this.context = GameContext.getInstance();

        GlobalCommandsAssist.registerGlobalCommands(context);
    }

    public void play() {
        context.setStage(GameStage.PREPARATION);
        dialogue.reportGlobalCommands();
        int boardSize = queryBoardSize();

        List<ShipConfig> shipConfigs = shipsOperations.configureShips(boardSize);
        if (shipConfigs == null) {
            return;
        }

        var first = introducePlayer(boardSize);
        var second = introducePlayer(boardSize);

        if (!shipsOperations.placeShips(first, shipConfigs) || !shipsOperations.placeShips(second, shipConfigs)) {
            dialogue.say("Something went wrong with ship placement. " +
                    "Probably, the field is too small for given amount of ships. Kindly start the game again.");
            return;
        }

        context.setStage(GameStage.GAME);
        boolean firstStep = true;
        while (true) {
            var stepResult = stepMachine.cycleSteps(first, second, firstStep);
            firstStep = false;
            if (stepResult.isGameOver()) {
                dialogue.say("Game over! {} has won", stepResult.getWinner().getName());
                GlobalCommandsAssist.drawPlayersBoard(dialogue);
                GlobalCommandsAssist.drawOpponentsBoard(dialogue);
                GlobalCommandsAssist.reportSituation(dialogue);
                return;
            }
        }
    }

    private int queryBoardSize() {
        return dialogue.readPositiveNumber("Enter board size: ");
    }

    private Player introducePlayer(int boardSize) {
        var name = dialogue.readString("Enter player name: ");
        var isHuman = dialogue.readYesNo("Is it a human player? Otherwise it will be an AI player");
        return new Player(name, new Board(boardSize), isHuman, isHuman ? new HumanMove() : new AiMove());
    }

}
