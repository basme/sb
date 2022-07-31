package com.daba.sb.model;

import com.daba.sb.model.board.Board;
import com.daba.sb.process.move.Move;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Player {

    private String name;
    private Board board;

    private boolean isHuman;
    private Move move;

}
