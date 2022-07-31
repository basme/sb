package com.daba.sb.process.move;

import com.daba.sb.model.process.MoveResult;
import com.daba.sb.model.process.StrikeResult;
import com.daba.sb.model.board.Board;

public interface Move {

    MoveResult make(Board target);

    void getNotified(StrikeResult strikeResult);

}
