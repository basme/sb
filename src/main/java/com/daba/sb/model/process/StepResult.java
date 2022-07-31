package com.daba.sb.model.process;

import com.daba.sb.model.Player;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StepResult {

    private boolean gameOver;
    private Player winner;

    public static StepResult gameOver(Player winner) {
        return new StepResult(true, winner);
    }

    public static StepResult gameContinues() {
        return new StepResult(false, null);
    }

}
