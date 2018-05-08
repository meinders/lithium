package lithium.catalog;

/**
 * Unit tests for the container group class.
 * 
 * @version 0.9 (2006.03.10)
 * @author Gerrit Meinders
 */
public class ContainerGroupTest extends AbstractGroupTest {
	/**
	 * Creates a concrete group instance to be tested.
	 * 
	 * @param displayName the display name
	 * @param name the name
	 * @param version the version
	 * @return the group
	 */
	protected Group createGroup(String displayName, String name, String version) {
		return new ContainerGroup(displayName, name, version);
	}

	public void testDuplicateLyricNumber() {
		Lyric first = createLyric(1);
		Lyric otherFirst = createLyric(1);
		group.addLyric(first);
		group.addLyric(otherFirst);
		assertFalse(group.getLyrics().contains(first));
		assertTrue(group.getLyrics().contains(otherFirst));
		assertFalse(first.getPropertyChangeSupport().hasListeners(null));
		assertTrue(otherFirst.getPropertyChangeSupport().hasListeners(null));
	}

	public void testLyricChangeHandling() {
		Lyric first = createLyric(1);
		group.addLyric(first);
		group.setModified(false);
		PropertyChangeTestListener listener = new PropertyChangeTestListener();
		group.addPropertyChangeListener(listener);
		first.setText("new text");
		assertTrue(listener.getLastEvent() != null);
		assertTrue(group.isModified());
	}
}
