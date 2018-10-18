import java.util.List;
import edu.calpoly.spritely.Tile;
import java.util.Random;

/**
 * An entity in our virtual world.  An entity occupies a square
 * on the grid.  It might move around, and interact with other
 * entities in the world.
 */
final class Entity
{
    public EntityKind kind;
    public Point position;
    public List<Tile> tiles;
    public int tileIndex;       // Index into tiles for animation
    public int resourceLimit;
    public int resourceCount;
    public int actionPeriod;
    public int animationPeriod;
    public static final Random rand = new Random();

    public Entity(EntityKind kind, Point position,
                  List<Tile> tiles, int resourceLimit, int resourceCount,
                  int actionPeriod, int animationPeriod)
    {
        this.kind = kind;
        this.position = position;
        this.tiles = tiles;
        this.tileIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public Action createAnimationAction(Entity entity, int repeatCount)
    {
        return new Action(ActionKind.ANIMATION, entity, null, repeatCount);
    }

    public Action createActivityAction(Entity entity, WorldModel world)
    {
        return new Action(ActionKind.ACTIVITY, entity, world, 0);
    }

    public static Entity createBlacksmith(Point position)
    {
        return new Entity(EntityKind.BLACKSMITH, position,
                          VirtualWorld.blacksmithTiles, 0, 0, 0, 0);
    }

    public static Entity
    createMinerFull(int resourceLimit, Point position,
                    int actionPeriod, int animationPeriod)
    {
        return new Entity(EntityKind.MINER_FULL, position,
                          VirtualWorld.minerFullTiles,
                          resourceLimit, resourceLimit, actionPeriod,
                          animationPeriod);
    }

    public static Entity
    createMinerNotFull(int resourceLimit, Point position, int actionPeriod,
                       int animationPeriod)
    {
        return new Entity(EntityKind.MINER_NOT_FULL, position,
                          VirtualWorld.minerTiles,
                          resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public static Entity
    createObstacle(Point position)
    {
        return new Entity(EntityKind.OBSTACLE, position,
                          VirtualWorld.obstacleTiles, 0, 0, 0, 0);
    }

    public static Entity
    createOre(Point position, int actionPeriod)
    {
        return new Entity(EntityKind.ORE, position,
                          VirtualWorld.oreTiles, 0, 0, actionPeriod, 0);
    }

    public static Entity
    createOreBlob(Point position, int actionPeriod, int animationPeriod)
    {
        
        return new Entity(EntityKind.ORE_BLOB, position,
                          VirtualWorld.blobTiles,
                          0, 0, actionPeriod, animationPeriod);
       
    }

    public static Entity createQuake(Point position)
    {
     
        return new Entity(EntityKind.QUAKE, position,
                          VirtualWorld.quakeTiles, 0, 0, 1100, 100);
    }

    public static Entity createVein(Point position, int actionPeriod)
    {
        return new Entity(EntityKind.VEIN, position,
                          VirtualWorld.veinTiles, 0, 0, actionPeriod, 0);
    }

    public void
    executeMinerFullActivity(Entity entity, WorldModel world,
                             EventSchedule eventSchedule)
    {
        Entity fullTarget
            = world.findNearest(position, EntityKind.BLACKSMITH);

        if (fullTarget != null  &&
            moveToFull(this, world, fullTarget, eventSchedule))
        {
            transformFull(entity, world, eventSchedule);
        }
        else
        {
            eventSchedule.scheduleEvent(entity,
                createActivityAction(entity, world),
                actionPeriod);
        }
    }

    public void
    executeMinerNotFullActivity(Entity entity, WorldModel world,
                                EventSchedule eventSchedule)
    {
        Entity notFullTarget = world.findNearest(entity.position,
            EntityKind.ORE);

        if (notFullTarget == null ||
            !moveToNotFull(entity, world, notFullTarget, eventSchedule) ||
            !transformNotFull(entity, world, eventSchedule))
        {
            eventSchedule.scheduleEvent(entity, 
                createActivityAction(entity, world),
                entity.actionPeriod);
        }
    }

    public void
    executeOreActivity(Entity entity, WorldModel world,
                       EventSchedule eventSchedule)
    {
        Point pos = position;    // store current position before removing

        world.removeEntity(entity);
        eventSchedule.unscheduleAllEvents(entity);

        Entity blob = createOreBlob(pos, entity.actionPeriod / 4,
                                    50 + rand.nextInt(100));

        world.addEntity(blob);
        scheduleActions(blob, eventSchedule, world);
    }

    public void
    executeOreBlobActivity(Entity entity, WorldModel world,
                           EventSchedule eventSchedule)
    {
        Entity blobTarget = world.findNearest(
            entity.position, EntityKind.VEIN);
        long nextPeriod = entity.actionPeriod;

        if (blobTarget != null)
        {
            Point tgtPos = blobTarget.position;

            if (moveToOreBlob(entity, world, blobTarget, eventSchedule))
            {
                Entity quake = createQuake(tgtPos);

                world.addEntity(quake);
                nextPeriod += entity.actionPeriod;
                scheduleActions(quake, eventSchedule, world);
            }
        }

        eventSchedule.scheduleEvent(entity,
            createActivityAction(entity, world),
            nextPeriod);
    }

    public void
    executeQuakeActivity(Entity entity, WorldModel world,
                         EventSchedule eventSchedule)
    {
        eventSchedule.unscheduleAllEvents(entity);
        world.removeEntity(entity);
    }

    public void
    executeVeinActivity(Entity entity, WorldModel world,
                        EventSchedule eventSchedule)
    {
        Point openPt = world.findOpenAround(entity.position);

        if (openPt != null) {
            Entity ore = createOre(openPt, 20000 + rand.nextInt(10000));
            world.addEntity(ore);
            scheduleActions(ore, eventSchedule, world);
        }

        eventSchedule.scheduleEvent(entity,
            createActivityAction(entity, world),
            actionPeriod);
    }

    public void
    scheduleActions(Entity entity, EventSchedule eventSchedule,
                    WorldModel world)
    {
        switch (kind)
        {
        case MINER_FULL:
            eventSchedule.scheduleEvent(entity,
                createActivityAction(entity, world),
                actionPeriod);
            eventSchedule.scheduleEvent(entity, createAnimationAction(entity, 0),
                getAnimationPeriod());
            break;

        case MINER_NOT_FULL:
            eventSchedule.scheduleEvent(entity,
                createActivityAction(entity, world),
                actionPeriod);
            eventSchedule.scheduleEvent(entity,
                createAnimationAction(entity, 0), getAnimationPeriod());
            break;

        case ORE:
            eventSchedule.scheduleEvent(entity,
                createActivityAction(entity, world),
                actionPeriod);
            break;

        case ORE_BLOB:
            eventSchedule.scheduleEvent(entity,
                createActivityAction(entity, world),
                actionPeriod);
            eventSchedule.scheduleEvent(entity,
                entity.createAnimationAction(entity, 0), entity.getAnimationPeriod());
            break;

        case QUAKE:
            eventSchedule.scheduleEvent(entity,
                createActivityAction(entity, world),
                actionPeriod);
            eventSchedule.scheduleEvent(entity,
                entity.createAnimationAction(entity, 10),
                entity.getAnimationPeriod());
            break;

        case VEIN:
            eventSchedule.scheduleEvent(entity,
                createActivityAction(entity, world),
                actionPeriod);
            break;

        default:
        }
    }

    private boolean
    transformNotFull(Entity entity, WorldModel world,
                     EventSchedule eventSchedule)
    {
        if (entity.resourceCount >= entity.resourceLimit)
        {
            Entity miner = createMinerFull(entity.resourceLimit,
                entity.position, entity.actionPeriod, entity.animationPeriod);

            world.removeEntity(entity);
            eventSchedule.unscheduleAllEvents(entity);

            world.addEntity(miner);
            scheduleActions(miner, eventSchedule, world);

            return true;
        }

        return false;
    }

    private void
    transformFull(Entity entity, WorldModel world, EventSchedule eventSchedule)
    {
        Entity miner = createMinerNotFull(resourceLimit,
            position, actionPeriod, animationPeriod);

        world.removeEntity(entity);
        eventSchedule.unscheduleAllEvents(entity);

        world.addEntity(miner);
        scheduleActions(miner, eventSchedule, world);
    }

    private boolean
    moveToNotFull(Entity miner, WorldModel world,
                  Entity target,  EventSchedule eventSchedule)
    {
        if (position.adjacent(miner.position, target.position))
        {
            miner.resourceCount += 1;
            world.removeEntity(target);
            eventSchedule.unscheduleAllEvents(target);

            return true;
        }
        else
        {
            Point nextPos = nextPositionMiner(miner, world, target.position);

            if (!miner.position.equals(nextPos))
            {
                world.moveEntity(miner, nextPos);
            }
            return false;
        }
    }

    private boolean
    moveToFull(Entity miner, WorldModel world,
               Entity target,  EventSchedule eventSchedule)
    {
        if (position.adjacent(miner.position, target.position))
        {
            return true;
        }
        else
        {
            Point nextPos = nextPositionMiner(miner, world, target.position);

            if (!miner.position.equals(nextPos))
            {
                world.moveEntity(miner, nextPos);
            }
            return false;
        }
    }

    private boolean
    moveToOreBlob(Entity blob, WorldModel world,
                  Entity target,  EventSchedule eventSchedule)
    {
        if (position.adjacent(blob.position, target.position))
        {
            world.removeEntity(target);
            eventSchedule.unscheduleAllEvents(target);
            return true;
        }
        else
        {
            Point nextPos = nextPositionOreBlob(world, target.position);

            if (!blob.position.equals(nextPos))
            {
                Entity occupant = world.getOccupant(nextPos);
                if (occupant != null)
                {
                    eventSchedule.unscheduleAllEvents(occupant);
                }

                world.moveEntity(blob, nextPos);
            }
            return false;
        }
    }

    private Point
    nextPositionMiner(Entity entity, WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.getX() - entity.position.getX());
        Point newPos = new Point(entity.position.getX() + horiz,
            entity.position.getY());

        if (horiz == 0 || world.isOccupied(newPos))
        {
            int vert = Integer.signum(destPos.getY() - entity.position.getY());
            newPos = new Point(entity.position.getX(),
                entity.position.getY() + vert);

            if (vert == 0 || world.isOccupied(newPos))
            {
                newPos = entity.position;
            }
        }

        return newPos;
    }

    private Point
    nextPositionOreBlob(WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.getX() - this.position.getX());
        Point newPos = new Point(this.position.getX() + horiz,
            this.position.getY());

        Entity occupant = world.getOccupant(newPos);

        if (horiz == 0 ||
            (occupant != null && !(occupant.kind == EntityKind.ORE)))
        {
            int vert = Integer.signum(destPos.getY() - this.position.getY());
            newPos = new Point(this.position.getX(),
                               this.position.getY() + vert);
            occupant = world.getOccupant(newPos);

            if (vert == 0 ||
                (occupant != null && !(occupant.kind == EntityKind.ORE)))
            {
                newPos = position;
            }
        }

        return newPos;
    }


    public int getAnimationPeriod()
    {
        switch (kind)
        {
        case MINER_FULL:
        case MINER_NOT_FULL:
        case ORE_BLOB:
        case QUAKE:
            return animationPeriod;
        default:
            throw new UnsupportedOperationException(
                String.format("getAnimationPeriod not supported for %s",
                kind));
        }
    }

    public void nextImage()
    {
        tileIndex = (tileIndex + 1) % tiles.size();
    }

    public Tile getCurrentTile()
    {
        return tiles.get(tileIndex);
    }


}
