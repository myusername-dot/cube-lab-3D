package io.github.labyrinthgenerator.pages.game3d.filters;

import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;

import java.util.ArrayList;
import java.util.List;

public class OverlapFilterManager {

    public final int[][] overlapFilters = new int[8][8];

    public OverlapFilterManager() {
        setupFilters();
    }

    public List<RectanglePlusFilter> getFiltersOverlap(final RectanglePlusFilter thisFilter) {
        List<RectanglePlusFilter> overlapFiltersList = new ArrayList<>();

        int x = thisFilter.ordinal();
        for (int y = 0; y < overlapFilters.length; y++) {
            if (overlapFilters[x][y] != RectanglePlusFilter.NONE.ordinal()) {
                overlapFiltersList.add(RectanglePlusFilter.values()[overlapFilters[x][y]]);
            }
        }
        return overlapFiltersList;
    }

    private boolean doesFiltersOverlap(final RectanglePlusFilter thisFilter, final RectanglePlusFilter otherFilter) {
        int x = thisFilter.ordinal();
        for (int y = 0; y < overlapFilters[x].length; y++) {
            if (overlapFilters[x][y] == otherFilter.ordinal()) {
                return true;
            }
        }
        return false;
    }

    private void setupFilters() {
//		Door
        overlapFilters[RectanglePlusFilter.DOOR.ordinal()][0] = RectanglePlusFilter.PLAYER.ordinal();
        overlapFilters[RectanglePlusFilter.DOOR.ordinal()][1] = RectanglePlusFilter.ENEMY.ordinal();
        overlapFilters[RectanglePlusFilter.DOOR.ordinal()][2] = RectanglePlusFilter.ENEMY_PROJECTILE.ordinal();

//		Player
        overlapFilters[RectanglePlusFilter.PLAYER.ordinal()][0] = RectanglePlusFilter.WALL.ordinal();
        overlapFilters[RectanglePlusFilter.PLAYER.ordinal()][1] = RectanglePlusFilter.DOOR.ordinal();
//		overlapFilters[RectanglePlusFilter.PLAYER.ordinal()][2] = RectanglePlusFilter.PLAYER.ordinal();
        overlapFilters[RectanglePlusFilter.PLAYER.ordinal()][3] = RectanglePlusFilter.ENEMY.ordinal();
        overlapFilters[RectanglePlusFilter.PLAYER.ordinal()][4] = RectanglePlusFilter.ENEMY_PROJECTILE.ordinal();
        overlapFilters[RectanglePlusFilter.PLAYER.ordinal()][5] = RectanglePlusFilter.ITEM.ordinal();

//		Enemy
        overlapFilters[RectanglePlusFilter.ENEMY.ordinal()][0] = RectanglePlusFilter.WALL.ordinal();
        overlapFilters[RectanglePlusFilter.ENEMY.ordinal()][1] = RectanglePlusFilter.DOOR.ordinal();
        overlapFilters[RectanglePlusFilter.ENEMY.ordinal()][2] = RectanglePlusFilter.PLAYER.ordinal();
        overlapFilters[RectanglePlusFilter.ENEMY.ordinal()][3] = RectanglePlusFilter.ENEMY.ordinal();

//		Projectile
        overlapFilters[RectanglePlusFilter.ENEMY_PROJECTILE.ordinal()][0] = RectanglePlusFilter.WALL.ordinal();
        overlapFilters[RectanglePlusFilter.ENEMY_PROJECTILE.ordinal()][1] = RectanglePlusFilter.DOOR.ordinal();
        overlapFilters[RectanglePlusFilter.ENEMY_PROJECTILE.ordinal()][2] = RectanglePlusFilter.PLAYER.ordinal();
//		overlapFilters[RectanglePlusFilter.ENEMY_PROJECTILE.ordinal()][3] = RectanglePlusFilter.ENEMY.ordinal();

//		Items
        overlapFilters[RectanglePlusFilter.ITEM.ordinal()][0] = RectanglePlusFilter.PLAYER.ordinal();

//      Entity
        overlapFilters[RectanglePlusFilter.ENTITY.ordinal()][0] = RectanglePlusFilter.WALL.ordinal();
        overlapFilters[RectanglePlusFilter.ENTITY.ordinal()][1] = RectanglePlusFilter.PLAYER.ordinal();

//		print out
//		System.err.println("FILTERS SETUP:");
//		for (int x = 0; x < OVERLAP_FILTERS.length; x++) {
//			System.out.println();
//			for (int y = 0; y < OVERLAP_FILTERS[x].length; y++) {
//				System.out.print("[" + OVERLAP_FILTERS[x][y] + "]");
//			}
//		}
//		System.out.println("\n");
    }
}
