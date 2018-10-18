import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import edu.calpoly.spritely.Size;
import edu.calpoly.spritely.Tile;
import java.util.LinkedList;
import java.util.List;
//import Point.java;

/**
 * Data structures that hold the model of our virtual world.
 * It consists of a grid.  Each point on the grid is occupied
 * by a background tile, and, optionally, an Entity.
 */
final class WorldModel
{
    public final Size size;
    public final Tile background[][];
    public final Entity occupant[][];
    public final Set<Entity> entities;

    public WorldModel(Size gridSize)
    {
	this.size = gridSize;
        this.background = new Tile[gridSize.height][gridSize.width];
        this.occupant = new Entity[gridSize.height][gridSize.width];
        this.entities = new HashSet<Entity>();
    }

    public boolean withinBounds(Point pos)
    {
        return pos.getY() >= 0 && pos.getY() < size.height &&
            pos.getX() >= 0 && pos.getX() < size.width;
    }

    public boolean isOccupied(Point pos)
    {
        return withinBounds(pos) && getOccupantCell(pos) != null;
    }

    private Entity nearestEntity(List<Entity> entities, Point pos)
    {
        if (entities.isEmpty()) {
            return null;
        } else {
            Entity nearest = entities.get(0);
            int nearestDistance = pos.distanceSquared(nearest.position, pos);

            for (Entity other : entities)
            {
                int otherDistance = pos.distanceSquared(other.position, pos);

                if (otherDistance < nearestDistance)
                {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return nearest;
        }
    }

    public Entity findNearest(Point pos,
        EntityKind kind)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : entities)
        {
            if (entity.kind == kind)
            {
                ofType.add(entity);
            }
        }

        return nearestEntity(ofType, pos);
    }

    public void addEntity(Entity entity)
    {
        if (withinBounds(entity.position))
        {
            setOccupantCell(entity.position, entity);
            entities.add(entity);
        }
    }

    public void moveEntity(Entity entity, Point pos)
    {
        Point oldPos = entity.position;
        if (withinBounds(pos) && !pos.equals(oldPos))
        {
            setOccupantCell(oldPos, null);
            removeEntityAt(pos);
            setOccupantCell(pos, entity);
            entity.position = pos;
        }
    }

    public void removeEntity(Entity entity)
    {
        removeEntityAt(entity.position);
    }

    private void removeEntityAt(Point pos)
    {
        if (withinBounds(pos)
            && getOccupantCell(pos) != null)
        {
            Entity entity = getOccupantCell(pos);

            /* this moves the entity just outside of the grid for
                debugging purposes */
            entity.position = new Point(-1, -1);
            entities.remove(entity);
            setOccupantCell(pos, null);
        }
    }

    public Entity getOccupant(Point pos)
    {
        if (isOccupied(pos)) {
            return getOccupantCell(pos);
        } else {
            return null;
        }
    }

    private Entity getOccupantCell(Point pos)
    {
        return occupant[pos.getY()][pos.getX()];
    }

    private void
    setOccupantCell(Point pos, Entity entity)
    {
        occupant[pos.getY()][pos.getX()] = entity;
    }

    public Point findOpenAround(Point pos)
    {
        for (int dy = -1; dy <= 1; dy++)
        {
            for (int dx = -1; dx <= 1; dx++)
            {
                Point newPt = new Point(pos.getX() + dx, pos.getY() + dy);
                if (withinBounds(newPt) &&
                    !isOccupied(newPt))
                {
                    return newPt;
                }
            }
        }

        return null;
    }
}

