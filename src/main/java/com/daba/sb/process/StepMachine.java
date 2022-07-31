package com.daba.sb.process;

import com.daba.sb.model.board.Board;
import com.daba.sb.view.BoardDrawer;
import com.daba.sb.Dialogue;
import com.daba.sb.model.board.Dot;
import com.daba.sb.model.GameContext;
import com.daba.sb.model.Player;
import com.daba.sb.model.process.StepResult;
import com.daba.sb.model.process.StrikeResult;

public class StepMachine {

    private static final Dialogue dialogue = Dialogue.join();
    private static final GameContext context = GameContext.getInstance();

    public StepResult cycleSteps(Player first, Player second, boolean firstStep) {
        if (firstStep) {
            dialogue.say("Player {}, you start the game. Your board is:", first.getName());
            BoardDrawer.drawOwn(first.getBoard());
        }

        StepResult firstResult = makeMovesUntilMiss(first, second, firstStep);
        if (firstResult.isGameOver()) {
            return firstResult;
        }

        StepResult secondResult = makeMovesUntilMiss(second, first, firstStep);
        if (secondResult.isGameOver()) {
            return secondResult;
        }

        return StepResult.gameContinues();
    }

    private StepResult makeMovesUntilMiss(Player player, Player opponent, boolean firstStep) {
        context.setPlayer(player);
        context.setOpponent(opponent);
        if (!firstStep) {
            if (player.isHuman()) {
                dialogue.say("Player {}, opponent made his moves. Here's your board: ", player.getName());
                BoardDrawer.drawOwn(player.getBoard());
            } else {
                dialogue.say("AI player {} makes a move", player.getName());
            }
        }
        while (true) {
            if (player.isHuman()) {
                dialogue.say("Player {}, make a move. Here's opponent's board", player.getName());
                BoardDrawer.drawOpponents(opponent.getBoard());
            }
            var firstResult = makeMove(player, opponent.getBoard());
            if (firstResult.isChangingMove() && opponent.getBoard().isGameOver()) {
                return StepResult.gameOver(player);
            }
            if (firstResult == StrikeResult.DO_AGAIN) {
                dialogue.say("This move makes no sense. Try striking cells that aren't picked yet");
                continue;
            }
            dialogue.say(firstResult.getMessage());
            if (firstResult.isChangingMove()) {
                if (player.isHuman()) {
                    dialogue.say("Make another move, player {}", player.getName());
                } else {
                    dialogue.say("AI player {} stroke successfully and will make one more move", player.getName());
                }
            } else {
                return StepResult.gameContinues();
            }
        }
    }

    private StrikeResult makeMove(Player player, Board opponentsBoard) {
        boolean legalMove = false;
        Dot dot = null;
        while (!legalMove) {
            var moveAttempt = player.getMove().make(opponentsBoard);
            legalMove = moveAttempt.isLegalMove();
            dot = moveAttempt.getDot();
        }
        var strikeResult = opponentsBoard.strike(dot);
        player.getMove().getNotified(strikeResult);
        return strikeResult;
    }

}
