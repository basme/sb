package com.daba.sb.model.process;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GameStage {

    PREPARATION("Preparation"),
    SHIP_PLACEMENT("Ship placement"),
    GAME("Game");

    @Getter
    private final String name;

}
