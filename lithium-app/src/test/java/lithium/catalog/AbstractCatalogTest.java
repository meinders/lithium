package lithium.catalog;

import junit.framework.*;

/**
 * Unit tests for implementations of the Catalog interface.
 *
 * @version 0.9 (2006.02.10)
 * @author Gerrit Meinders
 */
public abstract class AbstractCatalogTest extends TestCase
{
	/** The catalog used for testing. */
	protected Catalog catalog;

	public void setUp()
	{
		catalog = createCatalog();
	}

	/**
	 * Creates a catalog instance.
	 *
	 * @return the catalog
	 */
	protected abstract Catalog createCatalog();

	public void testEmptyGroups()
	{
		assertTrue(catalog.getGroups().isEmpty());
	}
}
