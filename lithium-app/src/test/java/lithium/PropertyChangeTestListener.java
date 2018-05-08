package lithium;

import java.beans.*;
import java.util.*;

/**
 * Property change listener implementation for test purposes that gathers
 * received events, allowing them to be inspected later. This implementation is
 * safe for use from multiple threads.
 *
 * @author Gerrit Meinders
 */
public class PropertyChangeTestListener implements PropertyChangeListener
{
	private List<PropertyChangeEvent> events;

	public PropertyChangeTestListener()
	{
		events = new ArrayList<PropertyChangeEvent>();
	}

	public synchronized void propertyChange(PropertyChangeEvent evt)
	{
		events.add(evt);
	}

	public synchronized List<PropertyChangeEvent> getEvents()
	{
		return new ArrayList<PropertyChangeEvent>(events);
	}

	public synchronized int getEventCount()
	{
		return events.size();
	}

	public synchronized void clear()
	{
		events.clear();
	}

	public synchronized List<PropertyChangeEvent> getEvents(
	        final String property)
	{
		final List<PropertyChangeEvent> result = new ArrayList<PropertyChangeEvent>();
		for (Iterator<PropertyChangeEvent> i = events.iterator(); i.hasNext();)
		{
			final PropertyChangeEvent event = i.next();
			if (property.equals(event.getPropertyName()))
			{
				result.add(event);
			}
		}
		return result;
	}

	public synchronized int getEventCount(final String property)
	{
		int result = 0;
		for (Iterator<PropertyChangeEvent> i = events.iterator(); i.hasNext();)
		{
			final PropertyChangeEvent event = i.next();
			if (property.equals(event.getPropertyName()))
			{
				result++;
			}
		}
		return result;
	}

	public synchronized void clear(final String property)
	{
		for (Iterator<PropertyChangeEvent> i = events.iterator(); i.hasNext();)
		{
			final PropertyChangeEvent event = i.next();
			if (property.equals(event.getPropertyName()))
			{
				i.remove();
			}
		}
	}
}
