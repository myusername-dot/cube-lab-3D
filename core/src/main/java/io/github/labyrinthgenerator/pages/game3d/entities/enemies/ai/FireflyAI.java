package io.github.labyrinthgenerator.pages.game3d.entities.enemies.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Enemy;

public class FireflyAI extends EnemyAI {
    private final Vector3 targetPos = new Vector3();
    private final Vector3 direction = new Vector3();
    private final Vector3 currentPos = new Vector3();

    public boolean targetPosSet = false;
    private float distanceFromTargetPos;
    public boolean targetPosReached = false;

    private final float moveSpeed = 0.1f;
    private final float targetPosXZRandomMaxMin = 1f;
    private final float targetPosYRandomMaxMin = 0.3f;
    private final float inRangeDistance = 0.1f;

    private boolean timerSet = false;
    private long timerStart;
    private long timerEnd;
    private final long timerTime = 2000L;

    public FireflyAI(final Enemy parent) {
        super(parent);
    }

    @Override
    public void tick(final float delta) {
        updateCurrentPosition();

        switch (aiState) {
            case IDLE:
                aiState = AiState.MOVING;
                break;
            case MOVING:
                handleMovingState(delta);
                break;
            case ATTACKING:
                // Handle attacking state if needed
                break;
            default:
                aiState = AiState.IDLE;
                break;
        }
    }

    private void updateCurrentPosition() {
        currentPos.set(parent.getPositionX(), parent.getPositionY(), parent.getPositionZ());
    }

    private void handleMovingState(final float delta) {
        if (!targetPosSet) {
            setNewTargetPosition();
        } else {
            if (timerSet) {
                if (isTimerExpired()) {
                    resetTargetPosition();
                } else {
                    moveTowardsTarget(delta);
                }
            }
        }
    }

    private void setNewTargetPosition() {
        targetPos.set(
            currentPos.x + MathUtils.random(-targetPosXZRandomMaxMin, targetPosXZRandomMaxMin),
            currentPos.y + MathUtils.random(-targetPosYRandomMaxMin, targetPosYRandomMaxMin),
            currentPos.z + MathUtils.random(-targetPosXZRandomMaxMin, targetPosXZRandomMaxMin)
        );

        timerStart = System.currentTimeMillis();
        timerEnd = timerStart + timerTime;
        timerSet = true;
        targetPosSet = true;
    }

    private boolean isTimerExpired() {
        return System.currentTimeMillis() >= timerEnd;
    }

    private void resetTargetPosition() {
        timerSet = false;
        targetPosSet = false;
    }

    private void moveTowardsTarget(final float delta) {
        direction.set(targetPos).sub(currentPos).nor().scl(moveSpeed * delta);
        distanceFromTargetPos = currentPos.dst(targetPos);

        if (distanceFromTargetPos < inRangeDistance) {
            resetTargetPosition();
        } else {
            parent.getRect().newPosition.add(direction);
        }
    }
}
