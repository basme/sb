package com.daba.sb.model.board;

import com.daba.sb.model.process.StrikeResult;
import com.daba.sb.model.Alignment;
import lombok.Data;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class Ship {

    private int size;
    private Collection<Dot> dots;
    private int cellsLeft;

    public Ship(int size, Dot baseDot, Alignment alignment) {
        this.size = size;
        this.cellsLeft = size;
        this.dots = IntStream.range(0, size)
                .boxed()
                .map(i -> Dot.of(baseDot.getX() + i * alignment.getXd(), baseDot.getY() + i * alignment.getYd()))
                .collect(Collectors.toList());
    }

    public StrikeResult strike() {
        if (--cellsLeft > 0) {
            return StrikeResult.WOUND;
        } else {
            return StrikeResult.KILLED;
        }
    }

}
