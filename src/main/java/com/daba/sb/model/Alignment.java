package com.daba.sb.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Random;

@RequiredArgsConstructor
public enum Alignment {

    HORIZONTAL(1, 0),
    VERTICAL(0,1);

    @Getter
    private final int xd;
    @Getter
    private final int yd;

    public static Alignment getRandom() {
        return new Random().nextInt(2) % 2 == 0 ? HORIZONTAL : VERTICAL;
    }

    public Alignment turn() {
        return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
    }

}
