package io.github.labyrinthgenerator.pages.game3d.entities.player.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;

public class PlayersInventory {
    private final Player player;

    int currentInventorySlot = 1;

    PlayersInventory(Player player) {
        this.player = player;
    }

    void handleInventoryInput() {
        if (player.screen.game.getGameInput().scrolledYDown) {
            currentInventorySlot = (currentInventorySlot % 6) + 1;
        } else if (player.screen.game.getGameInput().scrolledYUp) {
            currentInventorySlot = (currentInventorySlot - 2 + 6) % 6 + 1;
        }
        for (int i = 1; i <= 6; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + (i - 1))) {
                currentInventorySlot = i;
            }
        }
    }
}
