package com.daba.sb.model.process;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum StrikeResult {

    WOUND("Wound!"),
    KILLED("Killed!"),
    MISS("Miss!"),
    DO_AGAIN("Do again");

    @Getter
    private final String message;

    public boolean isChangingMove() {
        return this == WOUND || this == KILLED;
    }

}
