package com.daba.sb.model.board;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class BoardStats {

    private int cellsDestroyed;
    private int cellsLeft;

    private int shipsWound;
    private int shipsDestroyed;
    private int shipsLeft;

    private int timesMissed;

    private Set<Ship> woundShipsCache = new HashSet<>();

    public void shipAdded(Ship ship) {
        cellsLeft += ship.getSize();
        shipsLeft++;
    }

    public void shipWound(Ship ship) {
        if (!woundShipsCache.contains(ship)) {
            shipsWound++;
            woundShipsCache.add(ship);
        }
        cellsDestroyed++;
        cellsLeft--;
    }

    public void shipDestroyed(Ship ship) {
        cellsDestroyed++;
        cellsLeft--;
        shipsDestroyed++;
        shipsLeft--;
        if (woundShipsCache.contains(ship)) {
            shipsWound--;
        }
    }

    public void miss() {
        timesMissed++;
    }

}
