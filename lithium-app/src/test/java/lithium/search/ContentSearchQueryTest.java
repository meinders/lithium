package lithium.search;

import java.util.*;

import junit.framework.*;
import lithium.catalog.*;

public class ContentSearchQueryTest extends TestCase {
	// some strings from random web pages with some words in common
	private static final String[] TITLES = {
	        "Patience in Britain and Solitaire in America",
	        "Why Human Rights Requires Free Software",
	        "Testing Requires A Comprehensive Program",
	        "JUnit, Testing Resources for Extreme Programming",
	        "Linux Scalability Effort Homepage",
	        "testing requires more time and hardware" };

	private Catalog createTestCatalog() {
		Group group = new ContainerGroup("Group 1", "Group 1", "1.0");
		for (int i = 0; i < TITLES.length; i++) {
			group.addLyric(new DefaultLyric(i, TITLES[i]));
		}

		DefaultCatalog catalog = new DefaultCatalog();
		catalog.addGroup(group);
		return catalog;
	}

	public void testAnyWordQuery() {
		// if (true) throw new AssertionError("TODO: implement an actual test");

		// content search
		String words = "testing requires patience";
		ContentSearchQuery query = new ContentSearchQuery(words,
		        ContentSearchQuery.Method.ANY_WORD, true, true, false, false);
		query.compile();

		// find matching lyrics
		TreeSet<SearchResult> results = new TreeSet<SearchResult>();
		Catalog catalog = createTestCatalog();
		{
			for (Group group : catalog.getGroups()) {
				for (Lyric lyric : group.getLyrics()) {
					double relevance = query.match(lyric);
					LyricRef lyricRef = new LyricRef(group.getName(),
					        lyric.getNumber());
					results.add(new SearchResult(relevance, lyricRef));
				}
			}
		}

		assertEquals("unexpected result count", TITLES.length, results.size());
		for (SearchResult result : results) {
			Lyric lyric = catalog.getLyric(result.getLyricRef());
			if (lyric.getTitle() == TITLES[4]) {
				assertEquals("unexpected relevance", 0.0, result.getRelevance());
			} else {
				assertEquals("unexpected relevance", 1.0, result.getRelevance());
			}
		}
	}
}
