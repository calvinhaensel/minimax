//import Entity.java;
//import EventSchedule.java;

/**
 * An action data structure records information about
 * an action that is to be performed on an entity.  It 
 * is attached to an Event data structure.
 */

final class Action
{
    public ActionKind kind;
    public Entity entity;
    public WorldModel world;
    public int repeatCount;	// A repeat count of 0 means to repeat forever

    public Action(ActionKind kind, Entity entity, WorldModel world,
		  int repeatCount)
    {
        this.kind = kind;
        this.entity = entity;
        this.world = world;
        this.repeatCount = repeatCount;
    }

    public void executeAction(EventSchedule eventSchedule)
    {
        switch (kind)
        {
        case ACTIVITY:
            executeActivityAction(eventSchedule);
            break;

        case ANIMATION:
            executeAnimationAction(eventSchedule);
            break;
        }
    }

    public void
    executeAnimationAction(EventSchedule eventSchedule)
    {
        
        entity.nextImage();

        if (repeatCount != 1)
        {
            eventSchedule.scheduleEvent(entity,
                entity.createAnimationAction(entity,
                    Math.max(repeatCount - 1, 0)),
                entity.getAnimationPeriod());
        }
    }

    public void
    executeActivityAction(EventSchedule eventSchedule)
    {
        switch (entity.kind)
        {
        case MINER_FULL:
            entity.executeMinerFullActivity(entity, world,
                                     eventSchedule);
            break;

        case MINER_NOT_FULL:
            entity.executeMinerNotFullActivity(entity, world,
                                        eventSchedule);
            break;

        case ORE:
            entity.executeOreActivity(entity, world, eventSchedule);
            break;

        case ORE_BLOB:
            entity.executeOreBlobActivity(entity, world, eventSchedule);
            break;

        case QUAKE:
            entity.executeQuakeActivity(entity, world, eventSchedule);
            break;

        case VEIN:
            entity.executeVeinActivity(entity, world, eventSchedule);
            break;

        default:
            throw new UnsupportedOperationException(
                String.format("executeActivityAction not supported for %s",
                entity.kind));
        }
    }
}
