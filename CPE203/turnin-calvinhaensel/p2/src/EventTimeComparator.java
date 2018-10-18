import java.util.Comparator;

/**
 * A comparator used to sort events by time.
 */
final class EventTimeComparator implements Comparator<Event>
{
    public int compare(Event left, Event right)
    {
        return Double.compare(left.time, right.time);
    }
}
