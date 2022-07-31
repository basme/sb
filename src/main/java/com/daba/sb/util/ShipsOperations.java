package com.daba.sb.util;

import com.daba.sb.Dialogue;
import com.daba.sb.model.Alignment;
import com.daba.sb.model.Player;
import com.daba.sb.model.ShipConfig;
import com.daba.sb.model.board.Board;
import com.daba.sb.model.GameContext;
import com.daba.sb.model.process.GameStage;
import com.daba.sb.util.BoardUtils;
import com.daba.sb.view.BoardDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShipsOperations {

    private static final String FINISH_COMMAND = "F";

    private static final String SHIP_CONFIG_DELIMITER = " of ";

    private static final int RANDOM_PLACEMENT_ATTEMPTS_LIMIT = 10_000;

    private final Dialogue dialogue = Dialogue.join();

    private static final List<ShipConfig> DEFAULT_SHIPS = List.of(
            new ShipConfig(1, 4),
            new ShipConfig(2, 3),
            new ShipConfig(3, 2),
            new ShipConfig(4, 1)
    );

    public List<ShipConfig> configureShips(int boardSize) {
        List<ShipConfig> configs = new ArrayList<>();
        dialogue.say("Standard ships are: 1 ship of size 4, 2 ships of size 3, 3 ships of size 2, 4 ships of size 1");
        var makeCustomShips = dialogue.readYesNo("Do you want to set custom ship configs?");
        if (makeCustomShips) {
            dialogue.say("Now configure custom ships");
            while (true) {
                var response = dialogue.readString(
                        "Enter ship config in format 'a of b' (without quotes) to add a ships of size b. Enter F to finish");
                if (FINISH_COMMAND.equals(response)) {
                    break;
                }
                Optional.ofNullable(createConfig(response, boardSize)).ifPresent(configs::add);
            }
            if (isPlausibleConfigs(configs, boardSize)) {
                return configs;
            } else {
                dialogue.say("The config of ships you created is implausible. Please start the game once again");
                return null;
            }
        } else {
            if (!isPlausibleConfigs(DEFAULT_SHIPS, boardSize)) {
                dialogue.say("The board size is too small to play on standard configs. Please start the game once again");
                return null;
            }
            dialogue.say("Game will be played with standard ships");
            return DEFAULT_SHIPS;
        }
    }

    private boolean isPlausibleConfigs(List<ShipConfig> configs, int boardSize) {
        if (configs.isEmpty()) {
            dialogue.say("You cannot play without ships");
            return false;
        }
        if (configs.stream().mapToInt(c -> c.getCount() * c.getSize()).sum() > boardSize * boardSize) {
            dialogue.say("You added too many ships, they cannot be placed on the board of that size");
            return false;
        }
        return true;
    }

    private ShipConfig createConfig(String raw, int boardSize) {
        var parts = raw.split(SHIP_CONFIG_DELIMITER);
        if (parts.length != 2) {
            dialogue.say("Cannot parse ship config. It should be in format 'a of b' (without quotes)");
        }
        try {
            int count = Integer.parseInt(parts[0]);
            int size = Integer.parseInt(parts[1]);
            if (count <= 0 || size <= 0) {
                dialogue.say("Both number of ships and size of a ship should be a positive integers");
                return null;
            }
            if (size > boardSize) {
                dialogue.say("It's impossible to place ships of size {} on a board of size {}", size, boardSize);
                return null;
            }
            return new ShipConfig(count, size);
        } catch (NumberFormatException nfe) {
            dialogue.say("Both number of ships and size of a ship should be a non-negative integers");
            return null;
        }
    }

    public boolean placeShips(Player player, List<ShipConfig> shipConfigs) {
        GameContext.getInstance().setStage(GameStage.SHIP_PLACEMENT);
        GameContext.getInstance().setPlayer(player);
        var board = player.getBoard();
        if (!player.isHuman()) {
            dialogue.say("AI player will place ships randomly");
            return placeRandomShips(board, shipConfigs);
        }
        dialogue.say("Player {} places ships", player.getName());
        var random = dialogue.readYesNo("Should the game place ships randomly for you?");
        if (random) {
            dialogue.say("Your ships will now be placed randomly");
            return placeRandomShips(board, shipConfigs);
        }
        placeShipsManually(board, shipConfigs);
        return true;
    }

    private void placeShipsManually(Board board, List<ShipConfig> shipConfigs) {
        dialogue.say("Now place your ships");
        BoardDrawer.drawOwn(board);
        for (ShipConfig shipConfig : shipConfigs) {
            dialogue.say("Placing {} ships with size {}", shipConfig.getCount(), shipConfig.getSize());
            for (int i = 1; i <= shipConfig.getCount(); i++) {
                dialogue.say("Placing ship #{} of size {}", i, shipConfig.getSize());
                var alignment = shipConfig.getSize() == 1 ?
                        Alignment.HORIZONTAL :
                        dialogue.readAlignment("Should it be vertical or horizontal?");
                while (true) {
                    var baseDot = dialogue.readDotFromScanner("Enter upper-left dot of a new ship", board.getSize());
                    if (board.placeShip(shipConfig.getSize(), baseDot, alignment)) {
                        BoardDrawer.drawOwn(board);
                        break;
                    } else {
                        dialogue.say("Ship cannot be placed in the dot you entered. It's either cannot fit the board or touches/intersects with other ships. Enter dot again");
                    }
                }
            }
        }
    }

    private boolean placeRandomShips(Board board, List<ShipConfig> shipConfigs) {
        for (ShipConfig shipConfig : shipConfigs) {
            for (int i = 1; i <= shipConfig.getCount(); i++) {
                for (int j = 0; ; j++) {
                    var alignment = Alignment.getRandom();
                    var dot = BoardUtils.getRandomDot(board.getSize());
                    if (board.placeShip(shipConfig.getSize(), dot, alignment)) {
                        break;
                    }
                    if (j == RANDOM_PLACEMENT_ATTEMPTS_LIMIT) {
                        return false;
                    }
                }
            }
        }
        dialogue.say("Ships has been placed randomly");
        return true;
    }

}
