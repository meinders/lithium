package lithium.catalog;

/**
 * Unit test of the default catalog implementation.
 * 
 * @version 0.9 (2006.02.10)
 * @author Gerrit Meinders
 */
public class ReferenceGroupTest extends AbstractGroupTest {
	/**
	 * Creates a concrete group instance to be tested.
	 * 
	 * @param displayName the display name
	 * @param name the name
	 * @param version the version
	 * @return the group
	 */
	protected Group createGroup(String displayName, String name, String version) {
		return new ReferenceGroup(displayName, name, version);
	}

	public void testDuplicateLyric() {
		Lyric first = createLyric(1);
		Lyric otherFirst = createLyric(1);
		group.addLyric(first);
		group.addLyric(first);
		group.addLyric(otherFirst);
		assertTrue(group.getLyrics().size() == 2);
		assertTrue(group.getLyrics().contains(first));
		assertTrue(group.getLyrics().contains(otherFirst));
	}

	public void testLyricChangeHandling() {
		Lyric first = createLyric(1);
		group.addLyric(first);
		group.setModified(false);
		PropertyChangeTestListener listener = new PropertyChangeTestListener();
		group.addPropertyChangeListener(listener);
		first.setText("new text");
		assertTrue(listener.getLastEvent() == null);
		assertTrue(!group.isModified());
	}

	/*
	 * XXX: re-enable lengthy test public void testWeakReferencing() { Lyric
	 * first = createLyric(1); Lyric second = createLyric(2); Lyric third =
	 * createLyric(3); group.addLyric(first); group.addLyric(second);
	 * group.addLyric(third); second = null; System.gc();
	 * assertTrue(group.getLyrics().size() == 2); }
	 */
}
