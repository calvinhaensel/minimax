
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.LinkedList;
// import Action.java;

/**
 * This data stucture is used to hold the schedule events that are queued
 * for later execution.
 */
final class EventSchedule {

    /**
     * A queue of events, sorted by time
     */
    public PriorityQueue<Event> eventQueue;

    /**
     * A record of all the events in the queue relating to a given
     * target.  We keep this so we can efficiently remove them all,
     * if needed.
     */
    public Map<Object, List<Event>> pendingEvents;

    /**
     * The current time in ms, according to how far we've advanced.
     */
    public double currentTime;

    /**
     * A factor to double all times by.  This allows us to speed up
     * the game logic for testing.
     */
    public double timeScale;

    /**
     * Create a new EventSchedule.
     *
     * @param timeScale  A multiplier applied whenever we schedule an
     *                   event.  This allows us to run the simulation
     *                   faster.
     */
    public EventSchedule(double timeScale)
    {
        this.eventQueue = new PriorityQueue<>(new EventTimeComparator());
        this.pendingEvents = new HashMap<>();
        this.timeScale = timeScale;
	this.currentTime = 0.0;
    }

    public void
    scheduleEvent(Object target, Action action,
                  long after)
    {
        assert after >= 0;
        double time = currentTime +
                      (after * timeScale);
        Event event = new Event(action, time, target);

        eventQueue.add(event);

        // update list of pending events for the given target
        List<Event> pending = pendingEvents.get(target);
        if (pending == null) {
            pending = new LinkedList<>();
            pendingEvents.put(target, pending);
        }
        pending.add(event);
    }

    public void
    unscheduleAllEvents(Object target)
    {
        List<Event> pending = pendingEvents.remove(target);

        if (pending != null)
        {
            for (Event event : pending)
            {
                eventQueue.remove(event);
            }
        }
    }

    private void
    removePendingEvent(Event event)
    {
        List<Event> pending = pendingEvents.get(event.target);

        if (pending != null)
        {
            pending.remove(event);
        }
    }

    public void processEvents(double time)
    {
        while (!eventQueue.isEmpty() &&
               eventQueue.peek().time <= time)
        {
            Event next = eventQueue.poll();
            assert currentTime <= next.time;
            currentTime = next.time;

            removePendingEvent(next);

            next.getAction().executeAction(this);
        }
        currentTime = time;
    }
}
