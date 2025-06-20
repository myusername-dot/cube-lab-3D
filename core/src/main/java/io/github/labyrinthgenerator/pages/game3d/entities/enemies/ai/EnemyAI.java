package io.github.labyrinthgenerator.pages.game3d.entities.enemies.ai;

import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Enemy;

public abstract class EnemyAI {
	protected enum AiState {
		IDLE, MOVING, ATTACKING;
	}

	protected Enemy parent;
	protected AiState aiState = AiState.IDLE;

	public EnemyAI(final Enemy parent) {
		this.parent = parent;
	}

	public void tick(final float delta) {

	}
}
