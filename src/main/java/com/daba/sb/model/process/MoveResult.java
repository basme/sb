package com.daba.sb.model.process;

import com.daba.sb.model.board.Dot;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveResult {

    private boolean legalMove;
    private Dot dot;

    public static MoveResult legal(Dot dot) {
        return new MoveResult(true, dot);
    }

    public static MoveResult illegal() {
        return new MoveResult(false, null);
    }

}
