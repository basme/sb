package com.daba.sb.model.board;

import com.daba.sb.util.BoardUtils;
import com.daba.sb.model.process.StrikeResult;
import com.daba.sb.model.Alignment;
import lombok.Data;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Data
public class Board {

    private int size;
    private Figure[][] figures;
    private Collection<Ship> fleet;
    private BoardStats stats;
    private Map<Dot, Ship> shipCache;

    public Board(int size){
        if (size < 3 || size > 9) {
            throw new IllegalArgumentException("Board size cannot be less than 3 and more than 9; requested size is " + size);
        }
        this.size = size;
        initEmptyBoard(size);
        fleet = new HashSet<>();
        shipCache = new HashMap<>();
        stats = new BoardStats();
    }

    public boolean placeShip(int shipSize, Dot dot, Alignment alignment) {
        if (!canBePlaced(shipSize, dot, alignment)) {
            return false;
        }
        Ship ship = new Ship(shipSize, dot, alignment);
        if (intersects(ship)) {
            return false;
        }
        fleet.add(ship);
        for (Dot d : ship.getDots()) {
            shipCache.put(d, ship);
            set(d, Figure.SHIP);
        }
        stats.shipAdded(ship);
        return true;
    }

    public StrikeResult strike(Dot dot) {
        int x = dot.getX();
        int y = dot.getY();
        if (x < 0 || y < 0 || x > size - 1 || y > size - 1) {
            throw new IllegalArgumentException("Strike position is out of bound");
        }
        Figure current = get(dot);
        if (current == Figure.MISS || current == Figure.DESTROYED) {
            return StrikeResult.DO_AGAIN;
        }
        var ship = shipCache.get(dot);
        if (ship == null) {
            set(dot, Figure.MISS);
            stats.miss();
            return StrikeResult.MISS;
        }
        set(dot, Figure.DESTROYED);
        var strikeResult = ship.strike();
        if (strikeResult == StrikeResult.KILLED) {
            stats.shipDestroyed(ship);
            markAdjacentCells(ship);
        }
        if (strikeResult == StrikeResult.WOUND) {
            stats.shipWound(ship);
        }
        return strikeResult;
    }

    public boolean isGameOver() {
        return fleet.stream().allMatch(ship -> ship.getCellsLeft() == 0);
    }

    private boolean canBePlaced(int shipSize, Dot dot, Alignment alignment) {
        int x = dot.getX();
        int y = dot.getY();
        if (alignment == Alignment.HORIZONTAL) {
            if (x < 0 || x > size - shipSize || y < 0 || y > size - 1) {
                return false;
            }
        }
        if (alignment == Alignment.VERTICAL) {
            if (x < 0 || x > size - 1 || y < 0 || y > size - shipSize) {
                return false;
            }
        }
        return true;
    }

    private boolean intersects(Ship ship) {
        for (Dot dot : ship.getDots()) {
            if (get(dot) != Figure.EMPTY) {
                return true;
            }
            if (BoardUtils.getAdjacentCells(dot, size).stream().anyMatch(d -> get(d) != Figure.EMPTY)) {
                return true;
            }
        }
        return false;
    }

    private void initEmptyBoard(int size) {
        figures = new Figure[size][size];
        for (int i = 0; i < size; i++) {
            Figure[] row = new Figure[size];
            Arrays.fill(row, Figure.EMPTY);
            figures[i] = row;
        }
    }

    private void markAdjacentCells(Ship ship) {
        for (Dot dot : ship.getDots()) {
            BoardUtils.getAdjacentCells(dot, size).forEach(d -> {
                if (get(d) == Figure.EMPTY) {
                    set(d, Figure.MISS);
                }
            });
        }
    }

    public Figure get(int x, int y) {
        return figures[y][x];
    }

    public Figure get(Dot dot) {
        return get(dot.getX(), dot.getY());
    }

    private void set(int x, int y, Figure figure) {
        figures[y][x] = figure;
    }

    private void set(Dot dot, Figure figure) {
        set(dot.getX(), dot.getY(), figure);
    }

}
