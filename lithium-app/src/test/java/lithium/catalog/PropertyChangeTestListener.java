package lithium.catalog;

import java.beans.*;

/**
 * A property change listener that stores the last received event for testing
 * purposes.
 *
 * @version 0.9 (2006.07.14)
 * @author Gerrit Meinders
 */
class PropertyChangeTestListener implements PropertyChangeListener {
	private volatile PropertyChangeEvent e;

	/**
	 * Constructs a new listener.
	 */
	public PropertyChangeTestListener() {
		e = null;
	}

	/**
	 * Returns the last received event, or <code>null</code> if no events have
	 * been received yet.
	 *
	 * @return the event object
	 */
	public PropertyChangeEvent getLastEvent() {
		return e;
	}

	public void propertyChange(PropertyChangeEvent e) {
		this.e = e;
	}
}
