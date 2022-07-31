package com.daba.sb.model;

import com.daba.sb.model.process.GameStage;
import lombok.Data;

@Data
public class GameContext {

    private static GameContext instance;

    public static GameContext getInstance() {
        if (instance == null) {
            instance = new GameContext();
        }
        return instance;
    }

    private Player player;
    private Player opponent;
    private GameStage stage;

    public String getPlayerName() {
        return player == null ? "No current player" : player.getName();
    }

    public String getOpponentsName() {
        return opponent == null ? "No opponent" : opponent.getName();
    }

}
