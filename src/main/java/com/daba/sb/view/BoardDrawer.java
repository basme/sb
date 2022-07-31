package com.daba.sb.view;

import com.daba.sb.util.BoardUtils;
import com.daba.sb.Dialogue;
import com.daba.sb.model.board.Board;
import com.daba.sb.model.board.Figure;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoardDrawer {

    private static final String DEL = "|";

    private static final Dialogue dialogue = Dialogue.join();

    public static void drawOwn(Board board) {
        drawBoard(board, true);
    }

    public static void drawOpponents(Board board) {
        drawBoard(board, false);
    }
    private static void drawBoard(Board board, boolean isOwn) {
        // Header
        var row = new StringBuilder();
        row.append(" ").append(DEL);
        row.append(
                IntStream.range(0, board.getSize())
                        .boxed()
                        .map(BoardUtils::indexToLetter)
                        .collect(Collectors.joining(DEL)));
        row.append(DEL);
        dialogue.say(row.toString());

        // Content
        for (int v = 0; v < board.getSize(); v++) {
            row = new StringBuilder();
            row.append(v + 1).append(DEL);
            int finalV = v;
            row.append(
                    IntStream.range(0, board.getSize())
                            .boxed()
                            .map(h -> getFigureView(board.get(h, finalV), isOwn))
                            .collect(Collectors.joining(DEL)));
            row.append(DEL);
            dialogue.say(row.toString());
        }
    }

    private static String getFigureView(Figure figure, boolean isOwn) {
        return isOwn ? figure.getOwnBoardView() : figure.getEnemyBoardView();
    }

}
