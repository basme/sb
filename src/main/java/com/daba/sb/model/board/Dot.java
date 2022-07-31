package com.daba.sb.model.board;

import com.daba.sb.util.BoardUtils;
import org.apache.commons.lang3.tuple.MutablePair;

public class Dot extends MutablePair<Integer, Integer> {

    public Dot(Integer left, Integer right) {
        super(left, right);
    }

    public static Dot of(int x,  int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinates cannot be negative");
        }
        return new Dot(x, y);
    }

    public int getX() {
        return getLeft();
    }

    public int getY() {
        return getRight();
    }

    @Override
    public String toString() {
        return BoardUtils.indexToLetter(getX()) + (getY() + 1);
    }
}
