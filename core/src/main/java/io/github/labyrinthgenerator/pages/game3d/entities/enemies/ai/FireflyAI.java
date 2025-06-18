package io.github.labyrinthgenerator.pages.game3d.entities.enemies.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
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
        currentPos.set(parent.getPosition());

        switch (aiState) {
            case IDLE:
                aiState = AiState.MOVING;
                break;
            case MOVING:
                if (!targetPosSet) {
                    targetPos.set(
                        currentPos.x + MathUtils.random(-targetPosXZRandomMaxMin, targetPosXZRandomMaxMin),
                        currentPos.y + MathUtils.random(-targetPosYRandomMaxMin, targetPosYRandomMaxMin),
                        currentPos.z + MathUtils.random(-targetPosXZRandomMaxMin, targetPosXZRandomMaxMin)
                    );

                    timerStart = System.currentTimeMillis();
                    timerEnd = timerStart + timerTime;
                    timerSet = true;

                    targetPosSet = true;
                } else {
                    if (timerSet) {
                        if (System.currentTimeMillis() >= timerEnd) {
//						System.out.println("time!");
                            timerSet = false;
                            targetPosSet = false;
                            break;
                        } else {
                            direction.x = targetPos.x - currentPos.x;
                            direction.y = targetPos.y - currentPos.y;
                            direction.z = targetPos.z - currentPos.z;

                            direction.nor().scl(moveSpeed * delta);

                            distanceFromTargetPos = Vector3.dst(
                                currentPos.x, currentPos.y, currentPos.z,
                                targetPos.x, targetPos.y, targetPos.z
                            );

                            if (distanceFromTargetPos < inRangeDistance) {
//							System.out.println("close enough!");
                                timerSet = false;
                                targetPosSet = false;
                                break;
                            } else {
                                parent.getRect().newPosition.add(direction.x, direction.y, direction.z);
                            }
                        }
                    }
                }
                break;
            case ATTACKING:
                break;
            default:
                aiState = AiState.IDLE;
                break;
        }
    }

}
