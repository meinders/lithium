/*
 * Copyright 2013 Gerrit Meinders
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lithium;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import com.github.meinders.common.*;
import lithium.catalog.*;
import lithium.io.*;
import org.xml.sax.*;

/**
 * This class manages the default catalogs and any catalogs that are currently
 * loaded and provides access to them all as a single catalog.
 *
 * @since 0.3
 * @version 0.9 (2006.04.14)
 * @author Gerrit Meinders
 */
public abstract class CatalogManager {
	/**
	 * Cache of previously loaded catalogs. Key: the URL of the catalog.
	 */
	private static HashMap<URL, MutableCatalog> cache;

	/**
	 * Contains all loaded catalogs of interest to the user, in order of
	 * precedence: catalogs currently opened by the user, catalogs loaded as
	 * overrides, and catalogs loaded by default.
	 */
	private static LinkedCatalog catalogs;

	/**
	 * Contains all catalogs currently opened by the user.
	 */
	private static LinkedCatalog openCatalogs;

	/**
	 * Contains all catalogs loaded by default.
	 */
	private static LinkedCatalog defaultCatalogs;

	static {
		cache = new HashMap<URL, MutableCatalog>();
		defaultCatalogs = new LinkedCatalog();
		openCatalogs = new LinkedCatalog();

		catalogs = new LinkedCatalog();
		catalogs.add(openCatalogs);
		catalogs.add(defaultCatalogs);
	}

	/**
	 * Loads the catalogs listed in config.xml.
	 *
	 * @param listener the status listener to be informed about the progress of
	 *            the operation
	 */
	public static void loadDefaultCatalogs(StatusListener listener) {
		StatusListener status;
		if (listener == null) {
			status = new StatusListener() {
				public void setStatus(String status) {
					// ignore
				}
			};
		} else {
			status = listener;
		}

		status.setStatus(Resources.get().getString(
		        "ellipsis",
		        Resources.get().getString("loading",
		                Resources.get().getString("catalogs"))));
		defaultCatalogs.clear();

		for (URL catalogURL : ConfigManager.getConfig().getCatalogURLs()) {
			String catalogName = catalogURL.getFile().substring(
			        catalogURL.getFile().lastIndexOf("/"));
			status.setStatus(Resources.get().getString(
			        "details",
			        Resources.get().getString("loading",
			                Resources.get().getString("catalogs")), catalogName));
			try {
				loadDefault(catalogURL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Config config = ConfigManager.getConfig();
		Set<File> loadOnStartup = config.getLoadOnStartupFiles(FilterManager.getFilters( FilterType.CATALOG));
		for (File file : loadOnStartup) {
			try {
				loadDefault(file.toURI().toURL());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void loadDefault(URL catalogURL) throws IOException,
	        ParseException, SAXException {
		assert catalogURL != null;
		Catalog catalog = getCatalog(catalogURL);
		if (!catalogs.contains(catalog)) {
			defaultCatalogs.add(catalog);
		}
	}

	/**
	 * Returns a catalog containing all catalogs currently of interest to the
	 * user.
	 *
	 * @return the catalog
	 */
	public static Catalog getCatalog() {
		return catalogs;
	}

	/**
	 * Receives notification that a catalog was opened by the user.
	 *
	 * @param catalog the catalog
	 */
	public static void open(Catalog catalog) {
		if (!catalogs.contains(catalog)) {
			openCatalogs.add(catalog);
		}
	}

	/**
	 * Receives notification that a catalog was closed by the user.
	 *
	 * @param catalog the catalog
	 * @return whether the catalog was open before this method completed
	 */
	public static boolean close(Catalog catalog) {
		boolean isOpen = openCatalogs.contains(catalog);
		if (isOpen) {
			openCatalogs.remove(catalog);
		}
		return isOpen;
	}

	/**
	 * Returns the catalog at the given URL. Catalogs loaded using this method
	 * are cached.
	 *
	 * @param catalogURL the location of the catalog
	 * @return the catalog
	 * @throws IOException if the catalog can't be read
	 * @throws ParseException if the catalog contains invalid data
	 * @throws SAXException if the resource doesn't contain valid XML
	 */
	public static MutableCatalog getCatalog(URL catalogURL) throws IOException,
	        ParseException, SAXException {
		MutableCatalog catalog = cache.get(catalogURL);
		if (catalog == null) {
			catalog = CatalogIO.read(catalogURL);
			cache.put(catalogURL, catalog);
		}
		return catalog;
	}
}
