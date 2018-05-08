package lithium.catalog;

import junit.framework.*;

/**
 * Unit tests for sub-classes of the abstract Group class.
 *
 * @version 0.9 (2006.02.12)
 * @author Gerrit Meinders
 */
public abstract class AbstractGroupTest extends TestCase {
	/** The primary group used for testing. */
	protected Group group;

	protected void setUp() throws Exception {
		group = createPrimaryGroup();
		group.setModified(false);
		assertNotNull(group);
	}

	/**
	 * Creates a 'primary group', with a version number lower than that of
	 * secondary groups.
	 */
	protected Group createPrimaryGroup() {
		return createGroup("Group.displayName", "primary group", "version p1");
	}

	/**
	 * Creates a numbered 'secondary group', with a version number based on the
	 * group's number, but always higher than of the primary group.
	 */
	protected Group createSecondaryGroup(int index) {
		return createGroup("Group.displayName", "secondary group " + index,
		        "version s" + index);
	}

	/**
	 * Creates a lyric for testing.
	 *
	 * @param number the number of the lyric
	 * @return the lyric
	 */
	protected Lyric createLyric(int number) {
		return new DefaultLyric(number, "title " + number);
	}

	/**
	 * Creates a concrete group instance to be tested.
	 *
	 * @param displayName the display name
	 * @param name the name
	 * @param version the version
	 * @return the group
	 */
	protected abstract Group createGroup(String displayName, String name,
	        String version);

	public void testEmptyGetLyrics() {
		assertTrue(group.getLyrics().isEmpty());
	}

	public void testEmptyGetGroups() {
		assertTrue(group.getGroups().isEmpty());
	}

	public void testCreatedModified() {
		Group otherGroup = createPrimaryGroup();
		assertTrue(otherGroup.isModified());
	}

	public void testSetModifiedTrue() {
		group.setModified(false);
		group.setModified(true);
		assertTrue(group.isModified());
	}

	public void testSetModifiedFalse() {
		group.setModified(true);
		group.setModified(false);
		assertFalse(group.isModified());
	}

	public void testAddGroup() {
		PropertyChangeTestListener listener = new PropertyChangeTestListener();
		group.addPropertyChangeListener(listener);
		Group addedGroup = createSecondaryGroup(1);
		group.addGroup(addedGroup);
		assertTrue(Group.GROUPS_PROPERTY == listener.getLastEvent().getPropertyName());
		assertTrue(group.getGroups().contains(addedGroup));
		assertTrue(group.isModified());
	}

	public void testRemoveGroup() {
		PropertyChangeTestListener listener = new PropertyChangeTestListener();
		Group addedGroup = createSecondaryGroup(1);
		group.addGroup(addedGroup);
		group.addPropertyChangeListener(listener);
		group.removeGroup(addedGroup);
		assertTrue(Group.GROUPS_PROPERTY == listener.getLastEvent().getPropertyName());
		assertFalse(group.getGroups().contains(addedGroup));
		assertTrue(group.isModified());
	}

	public void testSetModifiedTruePropagation() {
		Group first = createSecondaryGroup(1);
		Group second = createSecondaryGroup(2);
		Group third = createSecondaryGroup(3);
		group.addGroup(first);
		group.addGroup(second);
		group.addGroup(third);
		group.setModified(false);
		second.setModified(true);
		assertTrue(group.isModified());
	}

	public void testSetModifiedFalsePropagation() {
		Group first = createSecondaryGroup(1);
		Group second = createSecondaryGroup(2);
		Group third = createSecondaryGroup(3);
		group.addGroup(first);
		group.addGroup(second);
		group.addGroup(third);
		second.setModified(true);
		group.setModified(false);
		assertFalse(group.isModified() || second.isModified());
	}

	public void testGetEmptyLyrics() {
		assertTrue(group.getLyrics().isEmpty());
	}

	public void testAddLyric() {
		Lyric lyric = createLyric(1);
		group.addLyric(lyric);
		assertTrue(group.getLyrics().contains(lyric));
		assertTrue(group.isModified());
	}

	public void testRemoveLyric() {
		Lyric lyric = createLyric(1);
		group.addLyric(lyric);
		group.setModified(false);
		group.removeLyric(lyric);
		assertTrue(group.getLyrics().isEmpty());
		assertTrue(group.isModified());
	}

	public void testRemoveLyrics() {
		Lyric first = createLyric(1);
		Lyric second = createLyric(2);
		group.addLyric(first);
		group.addLyric(second);
		group.setModified(false);
		group.removeLyrics();
		assertTrue(group.getLyrics().isEmpty());
		assertTrue(group.isModified());
	}

	public void testGetLyricByNumber() {
		Lyric first = createLyric(1);
		Lyric second = createLyric(2);
		group.addLyric(first);
		group.addLyric(second);
		assertTrue(group.getLyric(first.getNumber()) == first);
		assertTrue(group.getLyric(second.getNumber()) == second);
	}

	public void testRemoveLyricByNumber() {
		Lyric first = createLyric(1);
		Lyric second = createLyric(2);
		group.addLyric(first);
		group.addLyric(second);
		group.removeLyric(first.getNumber());
		group.removeLyric(second.getNumber());
		assertTrue(group.getLyrics().isEmpty());
	}

	public void testCompareTo() {
		Group first = createSecondaryGroup(1);
		Group second = createSecondaryGroup(2);
		Group otherSecond = createSecondaryGroup(2);
		assertTrue(group.compareTo(first) < 0);
		assertTrue(group.compareTo(second) < 0);
		assertTrue(first.compareTo(second) < 0);
		assertTrue(first.compareTo(group) > 0);
		assertTrue(second.compareTo(group) > 0);
		assertTrue(second.compareTo(first) > 0);
		assertTrue(second.compareTo(otherSecond) == 0);
	}

	public void testEquals() {
		Group first = createSecondaryGroup(1);
		Group otherFirst = createSecondaryGroup(1);
		assertFalse(group.equals(first));
		assertTrue(first.equals(otherFirst));
	}
}
