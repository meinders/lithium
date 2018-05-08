package lithium.catalog;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link BibleRef} class.
 *
 * @author Gerrit Meinders
 */
public class BibleRefTest {
	private static final String VERSE = "vers";

	private String book;

	@Before
	public void initialize() {
		book = "Genesis";
	}

	@Test(expected = IllegalArgumentException.class)
	public void noBookInteger() {
		new BibleRef((Integer) null, null, null, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noBookString() {
		new BibleRef((String) null, null, null, null, null);
	}

	@Test
	public void bookOnly() {
		assertEquals("Incorrect representation", book, new BibleRef(book, null,
		        null, null, null).toString());
	}

	@Test
	public void singleChapterOnly() {
		assertEquals("Incorrect representation", book + " 12", new BibleRef(
		        book, 12, null, null, null).toString());
	}

	@Test
	public void multiChapterOnly() {
		assertEquals("Incorrect representation", book + " 12-34", new BibleRef(
		        book, 12, 34, null, null).toString());
	}

	@Test
	public void singleVerse() {
		assertEquals("Incorrect representation", book + " 12:23", new BibleRef(
		        book, 12, null, 23, null).toString());
	}

	@Test
	public void multiVerse() {
		assertEquals("Incorrect representation", book + " 12:23-34",
		        new BibleRef(book, 12, null, 23, 34).toString());
	}

	@Test
	public void multiChapterAndVerse() {
		assertEquals("Incorrect representation", book + " 12:34-23:45",
		        new BibleRef(book, 12, 23, 34, 45).toString());
	}

	@Test
	public void singleVerseOnly() {
		assertEquals("Incorrect representation", book + " " + VERSE + " 12",
		        new BibleRef(book, null, null, 12, null).toString());
	}

	@Test
	public void multiVerseOnly() {
		assertEquals("Incorrect representation", book + " " + VERSE + " 12-23",
		        new BibleRef(book, null, null, 12, 23).toString());
	}

	@Test
	public void equalStartAndEnd() {
		assertEquals("Incorrect representation", book + " 12:23", new BibleRef(
		        book, 12, 12, 23, 23).toString());
	}

	@Test
	public void equalStartAndEndChapter() {
		assertEquals("Incorrect representation", book + " 12:23-34",
		        new BibleRef(book, 12, 12, 23, 34).toString());
	}

	@Test
	public void equalStartAndEndVerse() {
		assertEquals("Incorrect representation", book + " 12:34-23:34",
		        new BibleRef(book, 12, 23, 34, 34).toString());
	}

	@Test
	public void equalStartAndEndChapterOnly() {
		assertEquals("Incorrect representation", book + " 12", new BibleRef(
		        book, 12, 12, null, null).toString());
	}

	@Test
	public void equalStartAndEndVerseOnly() {
		assertEquals("Incorrect representation", book + " " + VERSE + " 12",
		        new BibleRef(book, null, null, 12, 12).toString());
	}

	@Test
	public void containsBook() {
		assertTrue("Unexpected result",
		        new BibleRef(1, null, null, null, null).contains(new BibleRef(
		                1, null, null, null, null)));
		assertFalse("Unexpected result",
		        new BibleRef(1, null, null, null, null).contains(new BibleRef(
		                2, null, null, null, null)));
	}

	@Test
	public void containsChapters() {
		assertTrue("Unexpected result",
		        new BibleRef(1, 1, null, null, null).contains(new BibleRef(1,
		                1, null, null, null)));
		assertFalse("Unexpected result",
		        new BibleRef(1, 2, null, null, null).contains(new BibleRef(1,
		                1, null, null, null)));
		assertFalse("Unexpected result",
		        new BibleRef(2, 1, null, null, null).contains(new BibleRef(1,
		                1, null, null, null)));

		assertTrue("Unexpected result",
		        new BibleRef(1, 1, 2, null, null).contains(new BibleRef(1, 1,
		                2, null, null)));
		assertTrue("Unexpected result",
		        new BibleRef(1, 1, 2, null, null).contains(new BibleRef(1, 1,
		                null, null, null)));
		assertFalse("Unexpected result",
		        new BibleRef(1, 1, null, null, null).contains(new BibleRef(1,
		                1, 2, null, null)));
		assertTrue("Unexpected result",
		        new BibleRef(1, 1, 4, null, null).contains(new BibleRef(1, 2,
		                3, null, null)));
		assertFalse("Unexpected result",
		        new BibleRef(1, 2, 3, null, null).contains(new BibleRef(1, 1,
		                4, null, null)));
		assertFalse("Unexpected result",
		        new BibleRef(1, 1, 3, null, null).contains(new BibleRef(1, 2,
		                4, null, null)));
		assertFalse("Unexpected result",
		        new BibleRef(1, 2, 4, null, null).contains(new BibleRef(1, 1,
		                3, null, null)));
		assertFalse("Unexpected result",
		        new BibleRef(2, 1, 2, null, null).contains(new BibleRef(1, 1,
		                2, null, null)));
	}

	@Test
	public void containsVerses() {
		assertTrue("Unexpected result",
		        new BibleRef(1, 1, 2, 3, 4).contains(new BibleRef(1, 1, 1, 8,
		                10)));
		assertTrue(
		        "Unexpected result",
		        new BibleRef(1, 1, 2, 3, 4).contains(new BibleRef(1, 2, 2, 1, 2)));
		assertFalse("Unexpected result",
		        new BibleRef(1, 1, 2, 1, 10).contains(new BibleRef(1, 2, 3, 11,
		                2)));
	}
}
