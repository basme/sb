package com.daba.sb.process.move.impl;

import com.daba.sb.Dialogue;
import com.daba.sb.model.board.Figure;
import com.daba.sb.model.process.MoveResult;
import com.daba.sb.model.process.StrikeResult;
import com.daba.sb.ai.AiDirection;
import com.daba.sb.ai.AiState;
import com.daba.sb.model.Alignment;
import com.daba.sb.model.board.Board;
import com.daba.sb.model.board.Dot;
import com.daba.sb.process.move.Move;

import java.util.Random;

public class AiMove implements Move {

    private final Dialogue dialogue = Dialogue.join();
    private final Random random = new Random();


    private AiState state = AiState.RANDOM;

    private Dot catchedDot;
    private Alignment alignment;
    private AiDirection direction;
    boolean changedDirection = false;

    @Override
    public MoveResult make(Board target) {
        if (target.getStats().getShipsLeft() == 1) {
            dialogue.say("[AI] Just one ship left! Let the hunt begin!");
        }
        if (state == AiState.RANDOM) {
            dialogue.say("[AI] Where should I strike...");
            think();
            Dot dot;
            while (true) {
                int x = random.nextInt(target.getSize());
                int y = random.nextInt(target.getSize());
                if (target.get(x, y).isStrikable()) {
                    dot = Dot.of(x, y);
                    break;
                }
            }
            catchedDot = dot;
            dialogue.say("[AI] " + dot + "!");
            return MoveResult.legal(dot);
        }
        if (state == AiState.FOCUSED) {
            think();
            if (alignment == null) {
                alignment = Alignment.getRandom();
            }
            if (direction == null) {
                if (catchedDot.getX() == 0 || catchedDot.getY() == 0) {
                    direction = AiDirection.DOWN_RIGHT;
                } else if (catchedDot.getX() == target.getSize() - 1 || catchedDot.getY() == target.getSize() - 1) {
                    direction = AiDirection.DOWN_RIGHT;
                } else if (direction == null) {
                    direction = AiDirection.getRandom();
                }
            }
            int x = catchedDot.getX();
            int y = catchedDot.getY();
            while (true) {
                if (alignment == Alignment.HORIZONTAL) {
                    x = x + direction.getDelta();
                } else {
                    y = y + direction.getDelta();
                }
                if (outOfBounds(x, y, target.getSize()) || target.get(x, y) == Figure.MISS) {
                    if (changedDirection) {
                        changedDirection = false;
                        alignment = alignment.turn();
                    } else {
                        direction = direction.inverse();
                        changedDirection = true;
                    }
                    x = catchedDot.getX();
                    y = catchedDot.getY();
                } else if (target.get(x, y).isStrikable()) {
                    Dot dot = Dot.of(x, y);
                    dialogue.say("[AI] " + dot + "!");
                    return MoveResult.legal(dot);
                }
            }
        }
        return null;
    }

    @Override
    public void getNotified(StrikeResult strikeResult) {
        if (strikeResult == StrikeResult.WOUND && state == AiState.RANDOM) {
            dialogue.say("[AI] Aha, that's your ship here! Let's strike around");
            state = AiState.FOCUSED;
            changedDirection = false;
            return;
        }
        if (strikeResult == StrikeResult.WOUND && state == AiState.FOCUSED) {
            dialogue.say("[AI] Now I know the direction. Your ship is doomed!");
            return;
        }

        if (strikeResult == StrikeResult.MISS && state == AiState.FOCUSED && !changedDirection) {
            dialogue.say("[AI] Hmmm... let's change direction");
            direction = direction.inverse();
            changedDirection = true;
            return;
        }
        if (strikeResult == StrikeResult.MISS && state == AiState.FOCUSED && changedDirection) {
            if (alignment == Alignment.HORIZONTAL) {
                dialogue.say("[AI] Ahh, it's vertical! Ok...");
            } else {
                dialogue.say("[AI] Ahh, it's horizontal! Ok...");
            }
            changedDirection = false;
            alignment = alignment.turn();
            return;
        }

        if (strikeResult == StrikeResult.KILLED) {
            dialogue.say("[AI] That was a good ship! Was...");
            state = AiState.RANDOM;
            changedDirection = false;
            catchedDot = null;
            alignment = null;
        }

    }

    private boolean outOfBounds(int x, int y, int boardSize) {
        return x < 0 || y < 0 || x >= boardSize || y >= boardSize;
    }

    private void think() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
