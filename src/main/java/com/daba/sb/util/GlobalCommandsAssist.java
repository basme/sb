package com.daba.sb.util;

import com.daba.sb.Dialogue;
import com.daba.sb.model.GameContext;
import com.daba.sb.model.process.GameStage;
import com.daba.sb.view.BoardDrawer;

public class GlobalCommandsAssist {

    public static void registerGlobalCommands(GameContext context) {
        Dialogue dialogue = Dialogue.join();

        dialogue.addGlobalCommand("quit", "Quits game immediately", (d) -> {
            d.say("Game finishes immediately");
            System.exit(0);
        });
        dialogue.addGlobalCommand("help", "Gives general info about the game and current situation", GlobalCommandsAssist::printHelp);
        dialogue.addGlobalCommand("myboard", "Shows the board of current player", GlobalCommandsAssist::drawPlayersBoard);
        dialogue.addGlobalCommand("oppboard", "Shows the board of opponent", GlobalCommandsAssist::drawOpponentsBoard);
        dialogue.addGlobalCommand("whoami", "Shows the name of current player", (d) -> {
            d.say("You're {}", context.getPlayerName());
        });
        dialogue.addGlobalCommand("score", "Shows current scores", GlobalCommandsAssist::reportScore);
        dialogue.addGlobalCommand("situation", "Shows current game situation in details", GlobalCommandsAssist::reportSituation);
    }

    public static void printHelp(Dialogue d) {
        var context = GameContext.getInstance();
        d.say("This is a Seabattle game. Probably, right now you have to enter something the game requires from you");
        d.say("You can enter one of global commands");
        d.reportGlobalCommands();
        d.say("Current game stage is {}", context.getStage().getName());
        d.say("Current player is {}", context.getPlayerName());
        d.say("The current request the game expects you to answer is:");
        d.reportCurrentRequest();
    }

    public static void drawPlayersBoard(Dialogue d) {
        var context = GameContext.getInstance();
        if (context.getPlayer() != null) {
            d.say("{}, this is your board", context.getPlayerName());
            BoardDrawer.drawOwn(context.getPlayer().getBoard());
        } else {
            d.say("There is no board at the moment");
        }
    }

    public static void drawOpponentsBoard(Dialogue d) {
        var context = GameContext.getInstance();
        if (context.getOpponent() != null) {
            d.say("{}, this is the board of your opponent", context.getPlayerName());
            BoardDrawer.drawOpponents(context.getOpponent().getBoard());
        } else {
            d.say("There is no board at the moment");
        }
    }

    public static void reportScore(Dialogue d) {
        var context = GameContext.getInstance();
        if (context.getStage() != GameStage.GAME) {
            d.say("Game is not started yet");
        }
        var player = context.getPlayer();
        var opponent = context.getOpponent();
        d.say("Player {} destroyed {} ships", player.getName(), opponent.getBoard().getStats().getShipsDestroyed());
        d.say("Player {} destroyed {} ships", opponent.getName(), player.getBoard().getStats().getShipsDestroyed());
    }

    public static void reportSituation(Dialogue d) {
        var context = GameContext.getInstance();
        if (context.getStage() != GameStage.GAME) {
            d.say("Game is not started yet");
        }
        var player = context.getPlayer();
        var opponent = context.getOpponent();
        var playerStats = player.getBoard().getStats();
        var opponentStats = opponent.getBoard().getStats();
        d.say("Player {}'s board: {} cells destroyed, {} left; {} ships wounded, {} destroyed, {} left (including wounded)",
                player.getName(), playerStats.getCellsDestroyed(), playerStats.getCellsLeft(),
                playerStats.getShipsWound(), playerStats.getShipsDestroyed(), playerStats.getShipsLeft());
        d.say("Player {}'s board: {} cells destroyed, {} left; {} ships wounded, {} destroyed, {} left (including wounded)",
                opponent.getName(), opponentStats.getCellsDestroyed(), opponentStats.getCellsLeft(),
                opponentStats.getShipsWound(), opponentStats.getShipsDestroyed(), opponentStats.getShipsLeft());
    }

}
