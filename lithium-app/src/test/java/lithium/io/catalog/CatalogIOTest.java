package lithium.io.catalog;

import java.io.*;
import java.net.*;
import java.util.*;

import com.github.meinders.common.*;
import junit.framework.*;
import lithium.*;
import lithium.catalog.*;
import lithium.catalog.TypedGroup.*;
import lithium.io.*;

/**
 * Unit test of the catalog IO facilities.
 *
 * @version 0.9 (2006.02.21)
 * @author Gerrit Meinders
 */
public class CatalogIOTest extends TestCase {
	/** A predefined catalog used as a reference to verify results. */
	protected Catalog referenceCatalog;

	/**
	 * Sets up the test fixture by creating the reference catalog.
	 */
	@Override
	public void setUp() {
		// ensure resources are loaded
		Resources.set(new ResourceUtilities(
		        ResourceBundle.getBundle("lithium.Resources")));
		referenceCatalog = createReferenceCatalog();
	}

	public void testNewFormatInput() throws IOException {
		// read and verify catalog
		URL source = getClass().getResource( "newCatalogFormat.xml");
		Catalog catalog = CatalogIO.read(source);
		verifyCatalog(catalog, referenceCatalog);
	}

	public void testOldFormatInput() throws IOException {
		// read and verify catalog
		URL source = getClass().getResource( "oldCatalogFormat.xml");
		Catalog catalog = CatalogIO.read(source);
		verifyCatalog(catalog, referenceCatalog);
	}

	public void testNewFormatOutput() throws IOException {
		// write catalog to string
		Writer stringWriter = new StringWriter();
		try {
			CatalogIO.write(referenceCatalog, stringWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// read and verify catalog
		String output = stringWriter.toString();
		System.out.println(output);
		Reader stringReader = new StringReader(output);
		Catalog catalog = CatalogIO.read(stringReader);
		verifyCatalog(catalog, referenceCatalog);
	}

	public void testNewFormatOutputToFile() throws IOException {
		File file = File.createTempFile("test", null);

		// write catalog to file
		try {
			CatalogIO.write(referenceCatalog, file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// read and verify catalog
		Catalog catalog = CatalogIO.read(file);
		verifyCatalog(catalog, referenceCatalog);
	}

	/*
	 * Like any real-life test, it just won't work. This is due to changes since
	 * the old catalog version was created.
	 */
	/*
	 * XXX: disabled to gain some speed during testing; it's basically a
	 * redundant test anyway public void testRealLifeFormatEquivalence() throws
	 * IOException { File oldFile = new File("test",
	 * "oldCatalogFormat-Opwekking.xml"); File newFile = new File("test",
	 * "newCatalogFormat-Opwekking.xml"); Catalog oldCatalog =
	 * CatalogIO.read(oldFile); Catalog newCatalog = CatalogIO.read(newFile);
	 * try { verifyCatalog(oldCatalog, newCatalog); } catch (Throwable e) {
	 * e.printStackTrace(); } }
	 */

	private Catalog createReferenceCatalog() {
		DefaultCatalog catalog = new DefaultCatalog();

		Group bundle1 = Group.createBundle();
		bundle1.setName("bundle 1");
		bundle1.setVersion("today");
		Lyric lyric1 = new DefaultLyric(1, "lyric 1");
		lyric1.setText(createText("text 1, line 1", "text 1, line 2",
		        "text 1, line 3"));
		bundle1.addLyric(lyric1);
		Lyric lyric2 = new DefaultLyric(2, "lyric 2");
		lyric2.setText(createText("text 2, line 1", "text 2, line 2"));
		lyric2.setOriginalTitle("original lyric 2");
		lyric2.setCopyrights(createText("\u00a9 copyrights 2, line 1",
		        "copyrights 2, line 2"));
		lyric2.addKey("Dmin");
		lyric2.addKey("Es");
		lyric2.addBibleRef(new BibleRef(18, 136, null, 1, null));
		bundle1.addLyric(lyric2);
		catalog.addBundle(bundle1);

		Group bundle2 = Group.createBundle();
		bundle2.setName("bundle 2");
		bundle2.setVersion("yesterday");
		Lyric lyric3 = new DefaultLyric(1, "lyric 3");
		lyric3.setText(createText("text 3, line 1", "text 3, line 2",
		        "text 3, line 3"));
		bundle2.addLyric(lyric3);
		catalog.addBundle(bundle2);

		Group bundle3 = Group.createBundle();
		bundle3.setName("bundle 3");
		bundle3.setVersion("tomorrow");
		catalog.addBundle(bundle3);

		Group category1 = Group.createCategory();
		category1.setName("category 1");
		category1.setVersion("");
		category1.addLyric(lyric1);
		category1.addLyric(lyric2);
		catalog.addCategory(category1);

		Group category2 = Group.createCategory();
		category2.setName("category 2");
		category2.setVersion("");
		category2.addLyric(lyric1);
		category2.addLyric(lyric3);
		catalog.addCategory(category2);

		Group category3 = Group.createCategory();
		category3.setName("category 3");
		category3.setVersion("");
		catalog.addCategory(category3);

		Group cd1 = Group.createCD();
		cd1.setName("cd 1");
		cd1.setVersion("");
		cd1.addLyric(lyric1);
		cd1.addLyric(lyric2);
		cd1.addLyric(lyric3);
		catalog.addCD(cd1);

		Group cd2 = Group.createCD();
		cd2.setName("cd 2");
		cd2.setVersion("");
		catalog.addCD(cd2);

		return catalog;
	}

	private String createText(String... lines) {
		StringBuilder builder = new StringBuilder();
		for (String line : lines) {
			builder.append(line);
			builder.append('\n');
		}
		return builder.toString();
	}

	/**
	 * Verifies whether the given catalog matches a predefined test catalog. Two
	 * individually created catalogs will never match using equals for several
	 * reasons, so this method is used in stead to match the content of the
	 * catalogs.
	 *
	 * @param catalog the catalog to verify
	 * @param reference the catalog used to verify against
	 */
	private void verifyCatalog(Catalog catalog, Catalog reference) {
		CatalogManager.open(catalog);

		Set<Group> groups = catalog.getGroups();
		Set<Group> referenceGroups = reference.getGroups();

		if (groups.size() != referenceGroups.size()) {
			String message = "Unequal number of groups: " + groups.size()
			        + " (reference: " + referenceGroups.size() + ")";
			throw new AssertionError(message);
		}

		for (Group referenceGroup : referenceGroups) {
			Group group;
			if (referenceGroup instanceof TypedGroup) {
				TypedGroup typedGroup = (TypedGroup) referenceGroup;
				group = getGroup(groups, typedGroup.getType());
			} else {
				group = getGroup(groups, referenceGroup.getName());
			}
			if (group == null) {
				throw new AssertionError("Missing group: " + referenceGroup);
			}

			String version = group.getVersion();
			if (version == null) {
				assertTrue(referenceGroup.getVersion() == null);
			} else {
				assertTrue(version.equals(referenceGroup.getVersion()));
			}

			verifyGroup(group, referenceGroup);
		}

		CatalogManager.close(catalog);
	}

	private void verifyGroup(Group parentGroup, Group reference) {
		Set<Group> groups = parentGroup.getGroups();
		Set<Group> referenceGroups = reference.getGroups();
		for (Group referenceGroup : referenceGroups) {
			Group group = getGroup(groups, referenceGroup.getName());
			if (group == null) {
				throw new AssertionError("Missing group: " + referenceGroup);
			}

			String version = group.getVersion();
			String refVersion = referenceGroup.getVersion();
			if (version == null) {
				assertTrue("Incorrect version: " + version, refVersion == null);
			} else {
				assertTrue("Incorrect version: " + version + " (reference: "
				        + refVersion + ")", version.equals(refVersion));
			}

			verifyGroup(group, referenceGroup);
		}

		Collection<Lyric> lyrics = parentGroup.getLyrics();
		Collection<Lyric> referenceLyrics = reference.getLyrics();

		for (Lyric referenceLyric : referenceLyrics) {
			Lyric lyric = getLyric(lyrics, referenceLyric);
			if (lyric == null) {
				// print some debugging information before failing
				System.out.println("parentGroup.class: "
				        + parentGroup.getClass());
				System.out.println("--" + parentGroup);
				for (Lyric aLyric : lyrics) {
					System.out.println(" - " + aLyric);
				}
				System.out.println("reference.class: " + reference.getClass());
				System.out.println("--" + reference);
				for (Lyric aLyric : referenceLyrics) {
					System.out.println(" - " + aLyric);
				}
				throw new AssertionError("Missing lyric: " + referenceLyric
				        + " (in group " + parentGroup + ")");
			}

			verifyLyric(lyric, referenceLyric);
		}
	}

	private void verifyLyric(Lyric lyric, Lyric reference) {
		verify(lyric.getNumber(), reference.getNumber());
		verify(lyric.getText(), reference.getText());
		verify(lyric.getOriginalTitle(), reference.getOriginalTitle());
		verify(lyric.getCopyrights(), reference.getCopyrights());
		verify(lyric.getKeys(), reference.getKeys());
		verify(lyric.getBibleRefs(), reference.getBibleRefs());
	}

	/** Asserts that both object are equal or both <code>null</code>. */
	private void verify(Object value, Object reference) {
		if (!equals(value, reference)) {
			throw new AssertionError("Incorrect value: \"" + value
			        + "\" (reference: \"" + reference + "\")");
		}
	}

	/**
	 * Returns whether both objects are equal (according to equals or if both
	 * objects are <code>null</code>).
	 */
	private boolean equals(Object value, Object other) {
		return value == null ? other == null : value.equals(other);
	}

	/**
	 * Returns the first group with the given name for the given set of groups.
	 */
	private Group getGroup(Set<Group> groups, String name) {
		for (Group group : groups) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		return null;
	}

	/**
	 * Returns the first group with the given type for the given set of groups.
	 */
	private Group getGroup(Set<Group> groups, GroupType type) {
		for (Group group : groups) {
			if (group instanceof TypedGroup) {
				TypedGroup typedGroup = (TypedGroup) group;
				if (typedGroup.getType() == type) {
					return group;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the first lyric from the given group that matches the number and
	 * title of the given lyric.
	 *
	 * @param lyrics the set of lyrics to search in
	 * @param lyric the lyric to match
	 */
	private Lyric getLyric(Collection<Lyric> lyrics, Lyric match) {
		for (Lyric lyric : lyrics) {
			if (lyric.getNumber() == match.getNumber()
			        && equals(lyric.getTitle(), match.getTitle())) {
				return lyric;
			}
		}
		return null;
	}
}
