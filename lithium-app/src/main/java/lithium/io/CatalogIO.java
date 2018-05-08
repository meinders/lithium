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

package lithium.io;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import lithium.catalog.*;
import lithium.io.catalog.*;
import org.w3c.dom.*;

/**
 * This class provides the ability to read and write catalogs from and to files
 * in Lithium's XML file format. Reading from URLs is also supported.
 *
 * @version 0.9 (2006.03.10)
 * @author Gerrit Meinders
 */
public class CatalogIO
{
	/** The error message used if an unsupported DTD is encountered. */
	public static final String INVALID_DTD = "invalidDTD";

	/** The public ID of the old catalog format DTD. */
	public static final String PUBLIC_ID = "-//Frixus//DTD opwViewer Catalog 1.0//EN";

	/** The namespace of the new catalog format. */
	public static final String NAMESPACE = "urn:opwviewer:catalog";

	/** Resource name of the schema for catalogs, version 1.0. */
	public static final String CATALOG_V10_SCHEMA_LOCATION = "/lithium/io/catalog-1.0.xsd";

	/**
	 * Reads a catalog from the specified file.
	 *
	 * @param source the file
	 * @return the catalog
	 */
	public static MutableCatalog read(File source) throws IOException
	{
		return read(source.toURI().toURL());
	}

	/**
	 * Reads a catalog from the specified URL.
	 *
	 * @param source the URL
	 * @return the catalog
	 */
	public static MutableCatalog read(URL source) throws IOException
	{
		try
		{
			Task<MutableCatalog> task = ParserUtilities.createParserTask(
			        new CatalogParser(), source);
			task.run();
			return task.get();
		}
		catch (ExecutionException e)
		{
			throw (IOException) new IOException().initCause(e);
		}
		catch (InterruptedException e)
		{
			return null;
		}
	}

	/**
	 * Reads a catalog from the specified reader.
	 *
	 * @param source the reader
	 * @return the catalog
	 */
	public static MutableCatalog read(Reader source) throws IOException
	{
		try
		{
			Task<MutableCatalog> task = ParserUtilities.createParserTask(
			        new CatalogParser(), source);
			task.run();
			return task.get();
		}
		catch (ExecutionException e)
		{
			throw (IOException) new IOException().initCause(e);
		}
		catch (InterruptedException e)
		{
			return null;
		}
	}

	/**
	 * Writes the given catalog to a file with the given name.
	 *
	 * @param catalog the catalog
	 * @param filename the file name
	 */
	public static void write(Catalog catalog, String filename)
	        throws IOException
	{
		write(catalog, new File(filename));
	}

	/**
	 * Writes the given catalog to a given file.
	 *
	 * @param catalog the catalog
	 * @param file the file
	 */
	public static void write(Catalog catalog, File file) throws IOException
	{
		Task<Document> task = BuilderUtilities.createBuilderTask(
		        new CatalogBuilder(catalog), file);
		task.run();
	}

	/**
	 * Writes the given catalog to a given writer.
	 *
	 * @param catalog the catalog
	 * @param writer the writer
	 */
	public static void write(Catalog catalog, Writer writer) throws IOException
	{
		Task<Document> task = BuilderUtilities.createBuilderTask(
		        new CatalogBuilder(catalog), writer);
		task.run();
	}

	/**
	 * Builds an XML document from the given catalog and returns it, in stead of
	 * writing it to a file or writer.
	 *
	 * @param catalog the catalog
	 * @return the XML document describing the catalog
	 */
	public static Document buildDocument(Catalog catalog)
	{
		return buildDocument(catalog);
	}

	/**
	 * Builds an XML document from the given catalog and returns it, in stead of
	 * writing it to a file or writer.
	 *
	 * @param catalog the catalog
	 * @param indent whether the output should be indented for improved
	 *            human-readability
	 * @return the XML document describing the catalog
	 */
	public static Document buildDocument(Catalog catalog, boolean indent)
	{
		try
		{
			CatalogBuilder builder = new CatalogBuilder(catalog);
			builder.setIndent(indent);

			Task<Document> task = new MonitoredTask<Document>(builder);
			task.run();
			return task.get();
		}
		catch (ExecutionException e)
		{
			throw (RuntimeException) new RuntimeException().initCause(e);
		}
		catch (InterruptedException e)
		{
			return null;
		}
	}
}
