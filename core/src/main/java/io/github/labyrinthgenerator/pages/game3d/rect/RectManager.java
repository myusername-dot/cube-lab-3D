package io.github.labyrinthgenerator.pages.game3d.rect;

import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Enemy;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;

import java.util.*;
import java.util.stream.Collectors;

public class RectManager {

    private final Map<RectanglePlusFilter, Set<RectanglePlus>> rects = new HashMap<>();

    private final CubeLab3D game;

    public RectManager(final CubeLab3D game) {
        this.game = game;
    }

    public void addRect(final RectanglePlus rect) {
        rects.computeIfAbsent(rect.filter, k -> new HashSet<>());
        rects.get(rect.filter).add(rect);
    }

    public List<RectanglePlus> getNearestRectsByFilters(final RectanglePlus rect) {
        List<RectanglePlus> nearestRects = new ArrayList<>();
        List<RectanglePlusFilter> filters = game.getOverlapFilterMan().getFiltersOverlap(rect.filter);

        for (RectanglePlusFilter filter : filters) {
            if (rects.get(filter) == null) continue;
            Set<RectanglePlus> otherRects = rects.get(filter);
            for (final RectanglePlus otherRect : otherRects) {
                /*int connectedEntityId = otherRect.getConnectedEntityId();
                Entity entity = game.getEntMan().getEntityFromId(connectedEntityId);
                if (entity instanceof Enemy && !((Enemy) entity).isPlayerInRange()) {
                    continue;
                }*/

                if (overlapsPlusDistance(rect, otherRect)) {
                    nearestRects.add(otherRect);
                }
            }
        }
        return nearestRects;
    }

    public boolean overlapsPlusDistance(RectanglePlus r1, RectanglePlus r2) {
        float distance = 0.1f;
        return
            r1.getX() < r2.getX() + r2.getWidth() + distance && r1.getX() + r1.getWidth() + distance > r2.getX()
            && r1.getY() < r2.getY() + r2.getHeight() + distance && r1.getY() + r1.getHeight() + distance > r2.getY()
            && r1.getZ() < r2.getZ() + r2.getDepth() + distance && r1.getZ() + r1.getDepth() + distance > r2.getZ();
        /*return x < r.x + r.width && x + width > r.x
                && y < r.y + r.height && y + height > r.y
                && z < r.z + r.depth && z + depth > r.z;*/
    }

    public boolean checkCollisions(final RectanglePlus rect, final List<RectanglePlus> nearestRects) {
        for (final RectanglePlus otherRect : nearestRects) {
            if (checkCollision(rect, otherRect)) return true;
        }
        return false;
    }

    public boolean checkCollisions(final RectanglePlus rect) {
        List<RectanglePlusFilter> filters = game.getOverlapFilterMan().getFiltersOverlap(rect.filter);
        for (RectanglePlusFilter filter : filters) {
            if (rects.get(filter) == null) continue;
            Set<RectanglePlus> otherRects = rects.get(filter);
            for (final RectanglePlus otherRect : otherRects) {
                if (checkCollision(rect, otherRect)) return true;
            }
        }
        return false;
    }

    public boolean checkCollision(final RectanglePlus rect, final RectanglePlus otherRect) {
        if (otherRect != rect) { // if not itself...
            if (rect.overlaps(otherRect)) {
                if (game.getEntMan().getEntityFromId(rect.getConnectedEntityId()) != null) {
//							System.out.println("id1: " + rect.getConnectedEntityId());
                    game.getEntMan().getEntityFromId(rect.getConnectedEntityId()).onCollision(otherRect);
                }

                if (game.getEntMan().getEntityFromId(otherRect.getConnectedEntityId()) != null) {
//							System.out.println("id2: " + otherRect.getConnectedEntityId());
                    game.getEntMan().getEntityFromId(otherRect.getConnectedEntityId()).onCollision(rect);
                }
                return true;
            }
        }
        return false;
    }

    public Set<RectanglePlus> getRectsByFilter(RectanglePlusFilter filter) {
        if (rects.containsKey(filter)) return rects.get(filter);
        else return new HashSet<>();
    }

    public Set<RectanglePlus> getRects() {
        return rects.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public void removeRect(final RectanglePlus rect) {
        rects.values().forEach(l -> l.remove(rect));
    }

    public void clear() {
        rects.clear();
    }
}
