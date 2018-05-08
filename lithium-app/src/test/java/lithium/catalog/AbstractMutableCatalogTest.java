package lithium.catalog;

/**
 * Unit tests for implementations of the MutableCatalog interface.
 * 
 * @version 0.9 (2006.02.12)
 * @author Gerrit Meinders
 */
public abstract class AbstractMutableCatalogTest extends AbstractCatalogTest {
	/** The catalog used for testing. */
	protected MutableCatalog catalog;

	public void setUp() {
		super.setUp();
		catalog = (MutableCatalog) super.catalog;
		catalog.setModified(false);
	}

	/**
	 * Creates a catalog instance.
	 * 
	 * @return the catalog
	 */
	protected abstract MutableCatalog createCatalog();

	/**
	 * Creates a numbered group, with a version number based on the group's
	 * number.
	 * 
	 * @return the group
	 */
	protected Group createGroup(int index) {
		return new ContainerGroup("", "group " + index, "version " + index);
	}

	/**
	 * Creates a lyric with the given number.
	 * 
	 * @return the lyric
	 */
	protected Lyric createLyric(int number) {
		return new DefaultLyric(number, "lyric " + number);
	}

	public void testInitiallyModified() {
		assertTrue(createCatalog().isModified());
	}

	public void testSetModifiedTrue() {
		catalog.setModified(false);
		catalog.setModified(true);
		assertTrue(catalog.isModified());
	}

	public void testSetModifiedFalse() {
		catalog.setModified(true);
		catalog.setModified(false);
		assertFalse(catalog.isModified());
	}

	public void testAddGroup() {
		PropertyChangeTestListener listener = new PropertyChangeTestListener();
		catalog.addPropertyChangeListener(listener);
		Group addedGroup = createGroup(1);
		catalog.addGroup(addedGroup);
		assertTrue(MutableCatalog.GROUPS_PROPERTY == listener.getLastEvent().getPropertyName());
		assertTrue(catalog.getGroups().contains(addedGroup));
		assertTrue(catalog.isModified());
	}

	public void testRemoveGroup() {
		PropertyChangeTestListener listener = new PropertyChangeTestListener();
		Group addedGroup = createGroup(1);
		catalog.addGroup(addedGroup);
		catalog.addPropertyChangeListener(listener);
		catalog.removeGroup(addedGroup);
		assertTrue(MutableCatalog.GROUPS_PROPERTY == listener.getLastEvent().getPropertyName());
		assertFalse(catalog.getGroups().contains(addedGroup));
		assertTrue(catalog.isModified());
	}

	public void testSetModifiedTruePropagation() {
		Group first = createGroup(1);
		Group second = createGroup(2);
		Group third = createGroup(3);
		catalog.addGroup(first);
		catalog.addGroup(second);
		catalog.addGroup(third);
		catalog.setModified(false);
		second.setModified(true);
		assertTrue(catalog.isModified());
	}

	public void testSetModifiedFalsePropagation() {
		Group first = createGroup(1);
		Group second = createGroup(2);
		Group third = createGroup(3);
		catalog.addGroup(first);
		catalog.addGroup(second);
		catalog.addGroup(third);
		second.setModified(true);
		catalog.setModified(false);
		assertFalse(catalog.isModified() || second.isModified());
	}

	public void testGetGroupByName() {
		Group first = createGroup(1);
		Group second = createGroup(2);
		Group third = createGroup(3);
		catalog.addGroup(first);
		catalog.addGroup(second);
		first.addGroup(third);
		assertTrue(catalog.getGroup(first.getName()) == first);
		assertTrue(catalog.getGroup(second.getName()) == second);
		assertTrue(catalog.getGroup(third.getName()) == third);
	}

	public void testGetGroupsByLyric() {
		Group first = createGroup(1);
		Group second = createGroup(2);
		Group third = createGroup(3);
		catalog.addGroup(first);
		catalog.addGroup(second);
		first.addGroup(third);
		Lyric lyric = createLyric(1);
		first.addLyric(lyric);
		third.addLyric(lyric);
		assertTrue(catalog.getGroups(lyric).contains(first));
		assertTrue(catalog.getGroups(lyric).contains(third));
		assertTrue(catalog.getGroups(lyric).size() == 2);
	}

	public void testGetLyricByReference() {
		Group first = createGroup(1);
		Group second = createGroup(2);
		Group third = createGroup(3);
		catalog.addGroup(first);
		catalog.addGroup(second);
		first.addGroup(third);
		Lyric lyric = createLyric(1);
		Lyric otherLyric = createLyric(2);
		first.addLyric(lyric);
		third.addLyric(lyric);
		third.addLyric(otherLyric);
		assertTrue(catalog.getLyric(new LyricRef(first.getName(),
		        lyric.getNumber())) == lyric);
		assertTrue(catalog.getLyric(new LyricRef(third.getName(),
		        lyric.getNumber())) == lyric);
		assertTrue(catalog.getLyric(new LyricRef(third.getName(),
		        otherLyric.getNumber())) == otherLyric);
	}
}
