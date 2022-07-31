package com.daba.sb.ai;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Random;

@RequiredArgsConstructor
public enum AiDirection {

    UP_LEFT(-1),
    DOWN_RIGHT(1);

    @Getter
    private final int delta;

    public static AiDirection getRandom() {
        return new Random().nextInt(2) % 2 == 0 ? UP_LEFT : DOWN_RIGHT;
    }

    public AiDirection inverse() {
        return this == UP_LEFT ? DOWN_RIGHT : UP_LEFT;
    }

}
