
/**
 * A timed event in the virtual world.  Events are queued, and then
 * executed when their time arrives.
 */
final class Event
{
    public Action action;
    public double time;
    public Object target;

    public Event(Action action, double time, Object target)
    {
        this.action = action;
        this.time = time;
        this.target = target;
    }

    public Action getAction(){return  action;}
    public double getTime(){return time;}
    // public Entity getEntity(){return entity;}

}
