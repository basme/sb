package com.daba.sb.util;

import com.daba.sb.model.board.Dot;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BoardUtils {

    private static final List<Pair<Integer, Integer>> adjacency = List.of(
            Pair.of(0, 1),
            Pair.of(0, -1),
            Pair.of(1, 0),
            Pair.of(-1, 0),
            Pair.of(1, 1),
            Pair.of(1, -1),
            Pair.of(-1, 1),
            Pair.of(-1, -1)
    );

    private static final Random random = new Random();

    public static int letterToIndex(String letter) {
        return (int) letter.toLowerCase().charAt(0) - (int) 'a';
    }

    public static String indexToLetter(int index) {
        return String.valueOf((char) ('a' + index)).toUpperCase();
    }

    public static Dot createDot(String raw, int boardSize) {
        if (!isCorrectDot(raw, boardSize)) {
            return null;
        }
        var parts = raw.split("");
        var firstNum = BoardUtils.letterToIndex(parts[0]);
        var secondNum = Integer.parseInt(parts[1]) - 1;
        return Dot.of(firstNum, secondNum);
    }

    public static boolean isCorrectDot(String raw, int boardSize) {
        try {
            if (StringUtils.isEmpty(raw)) {
                return false;
            }
            if (raw.length() != 2) {
                return false;
            }
            var parts = raw.split("");
            int first = letterToIndex(parts[0]);
            int second = Integer.parseInt(parts[1]) - 1;
            if (first > boardSize - 1 || second > boardSize - 1) {
                return false;
            }
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static Set<Dot> getAdjacentCells(Dot dot, int boardSize) {
        Set<Dot> result = new HashSet<>();
        for (Pair<Integer, Integer> adj : adjacency) {
            var x = dot.getX() + adj.getLeft();
            var y = dot.getY() + adj.getRight();
            if (x >= 0 && y >= 0 && x < boardSize && y < boardSize) {
                result.add(Dot.of(x, y));
            }
        }
        return result;
    }

    public static Dot getRandomDot(int boardSize) {
        return Dot.of(random.nextInt(boardSize), random.nextInt(boardSize));
    }

}
