package lithium.catalog;

/**
 * Unit test of the default catalog implementation.
 * 
 * @version 0.9 (2006.02.10)
 * @author Gerrit Meinders
 */
public class DefaultCatalogTest extends AbstractMutableCatalogTest {
	/**
	 * Creates a catalog instance.
	 * 
	 * @return the catalog
	 */
	protected MutableCatalog createCatalog() {
		return new DefaultCatalog();
	}
}
