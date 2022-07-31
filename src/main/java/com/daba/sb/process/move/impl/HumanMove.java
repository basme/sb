package com.daba.sb.process.move.impl;

import com.daba.sb.Dialogue;
import com.daba.sb.model.process.MoveResult;
import com.daba.sb.model.process.StrikeResult;
import com.daba.sb.model.board.Board;
import com.daba.sb.process.move.Move;

public class HumanMove implements Move {

    @Override
    public MoveResult make(Board target) {
        var dot = Dialogue.join().readDotFromScanner("Enter your move: ", target.getSize());
        if (dot == null) {
            return MoveResult.illegal();
        }
        return MoveResult.legal(dot);
    }

    @Override
    public void getNotified(StrikeResult strikeResult) {
        // no-op
    }

}
