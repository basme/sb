package com.daba.sb.model.board;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Figure {

    EMPTY(" ", " "),
    SHIP("O"," "),
    DESTROYED("X", "X"),
    MISS(".", ".");

    @Getter
    private final String ownBoardView;
    @Getter
    private final String enemyBoardView;

    public boolean isStrikable() {
        return this == EMPTY || this == SHIP;
    }

}
