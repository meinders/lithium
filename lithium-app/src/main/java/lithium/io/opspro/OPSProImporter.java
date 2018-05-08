/*
 * Copyright 2018 Gerrit Meinders
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

package lithium.io.opspro;

import java.io.*;
import java.sql.*;
import java.util.regex.*;

import com.github.meinders.common.*;
import com.github.meinders.common.parser.*;
import lithium.catalog.*;
import lithium.io.*;

/**
 * Importer for catalogs stored in the format used by 'OPS Pro 2007'. The format
 * uses a Microsoft (Access) database format, weakly encrypted with a password.
 * The database is quite the abomination, but can with some effort be converted
 * reliably.
 *
 * @author Gerrit Meinders
 */
public class OPSProImporter extends GenericWorker<MutableCatalog>
{
	private final File source;

	private final String key;

	static
	{
		/*
		 * Initialize Sun's JDBC-ODBC Driver.
		 */
		try
		{
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		}
		catch (ClassNotFoundException e)
		{
			System.err.println("JDBC-ODBC Driver ontbreekt. Details:");
			e.printStackTrace();
		}
	}

	/**
	 * Creates a connection to a Micrsoft Access database stored in the given
	 * file using JDBC-ODBC.
	 *
	 * @param file The file containing the database. (*.mdb)
	 * @param password The database password, or <code>null</code>.
	 * @param readOnly Whether the connection should be read-only.
	 *
	 * @return The created connection.
	 *
	 * @throws SQLException if no connection can be created.
	 */
	public static Connection getAccessDBConnection(File file, String password,
	        boolean readOnly) throws SQLException
	{
		StringBuilder url = new StringBuilder();
		url.append("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)}");
		url.append(";READONLY=");
		url.append(readOnly);
		url.append(";DBQ=");
		url.append(file.getAbsolutePath());
		url.append(";PWD=");
		url.append(password);
		url.append(";}");

		return DriverManager.getConnection(url.toString());
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param source Source file.
	 * @param key    Key needed to access the database.
	 */
	public OPSProImporter(File source, String key)
	{
		this.source = source;
		this.key = key;
	}

	@Override
	public void finished()
	{
		fireWorkerFinished();
	}

	@Override
	public MutableCatalog construct()
	{
		Connection db = null;
		try
		{
			db = getAccessDBConnection(source, key, true);

			final PreparedStatement selectBundles = db.prepareStatement(""
			        + "SELECT BundelnaamID, Bundelnaam FROM Bundels WHERE "
			        + "NOT Bundelnaam = '#';");

			final DefaultCatalog catalog = new DefaultCatalog();

			final ResultSet bundles = selectBundles.executeQuery();
			while (bundles.next())
			{
				final String bundleID = bundles.getString(1);
				final String bundleName = bundles.getString(2);

				constructBundle(db, catalog, bundleID, bundleName);
			}
			db.close();

			return catalog;

		}
		catch (SQLException e)
		{
			e.printStackTrace();
			fireWorkerError(e);
			return null;

		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
			throw e;

		}
		finally
		{
			if (db != null)
			{
				try
				{
					db.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					fireWorkerWarning(e);
				}
			}
		}
	}

	private void constructBundle(Connection db, MutableCatalog catalog,
	        String bundleID, String bundleName) throws SQLException
	{
		Group bundle = new Bundle(bundleName, "1.0"); // FIXME: fix versioning
		catalog.addBundle(bundle);

		final PreparedStatement selectLyricCount = db.prepareStatement("SELECT COUNT(*) FROM ["
		        + bundleID + "];");
		ResultSet lyricCountResult = selectLyricCount.executeQuery();
		lyricCountResult.next();
		int lyricCount = lyricCountResult.getInt(1);

		final PreparedStatement selectLyrics = db.prepareStatement("SELECT * FROM ["
		        + bundleID + "];");
		/*
		 * NOTE: There is also a (sometimes slightly modified) copy of some of
		 * the lyrics in [OPSBundels].
		 */

		final ResultSet lyrics = selectLyrics.executeQuery();

		int lyricIndex = 0;
		while (lyrics.next())
		{
			final DefaultLyric lyric = new DefaultLyric(
			        lyrics.getInt("liednr"), lyrics.getString("titel"));

			if (bundle.getLyric(lyric.getNumber()) != null)
			{
				fireWorkerWarning("Skipping duplicate lyric: "
				        + lyric.getNumber() + " in bundle " + bundle.getName());
				continue;
			}

			bundle.addLyric(lyric);

			final String text = fixNewlines(lyrics.getString("tekst"));
			parseText(lyric, text);

			final String originalTitle = lyrics.getString("oorsprong");
			if (!isFakeNull(originalTitle))
			{
				lyric.setOriginalTitle(originalTitle);
			}

			final String copyrights = fixNewlines(lyrics.getString("copyright"));
			if (!isFakeNull(copyrights))
			{
				lyric.setCopyrights(copyrights);
			}

			final int cdNumber = lyrics.getInt("cd");
			if (cdNumber > 0)
			{
				final String name = String.valueOf(cdNumber);
				Group cd = catalog.getCD(name);
				if (cd == null)
				{
					cd = new CD(name);
					catalog.addCD(cd);
				}
				cd.addLyric(lyric);
			}

			for (int i = 1; i <= 9; i++)
			{
				final String name = lyrics.getString("cat" + i);
				if (!isFakeNull(name))
				{
					Group category = catalog.getCategory(name);
					if (category == null)
					{
						category = new Category(name);
						catalog.addCategory(category);
					}
					category.addLyric(lyric);
				}
			}

			for (int i = 1; i <= 4; i++)
			{
				final String bibleRefText = lyrics.getString("bijbel" + i);
				if (!isFakeNull(bibleRefText))
				{
					BibleRef bibleRef;
					try
					{
						bibleRef = BibleRefParser.parse(bibleRefText);
						lyric.addBibleRef(bibleRef);
					}
					catch (ParserException e)
					{
						System.out.println("Invalid bible reference: "
						        + bibleRefText);
						fireWorkerWarning(e);
					}
				}
			}

			for (int i = 1; i <= 3; i++)
			{
				final String key = lyrics.getString("toonsrt" + i);
				if (!isFakeNull(key))
				{
					lyric.addKey(key);
				}
			}

			lyricIndex++;
			fireWorkerProgress(lyricIndex, lyricCount);
		}
	}

	/**
	 * Parses the given text and stores it in the given lyric.
	 *
	 * @param lyric the lyric to store the result in
	 * @param text the text to be parsed
	 */
	private void parseText(DefaultLyric lyric, String text)
	{
		Pattern annotationPattern = Pattern.compile("\\[([^\\]]+)\\]\n?");
		Matcher annotationMatcher = annotationPattern.matcher(text);

		StringBuffer plainText = new StringBuffer(text.length());

		while (annotationMatcher.find())
		{
			String identifier = annotationMatcher.group(1);
			if ("split".equals(identifier) || "splits".equals(identifier)
			        || "splts".equals(identifier)
			        || "spluits".equals(identifier))
			// (I guess they have yet to discover constants...)
			{
				/*
				 * An invisible line that splits a strophe into parts to fit the
				 * screen. (Note that OPS displays around 4 lines of text,
				 * switching from one block of text to another.)
				 */
				annotationMatcher.appendReplacement(plainText, "");
			}
			else if ("join".equals(identifier))
			{
				/*
				 * An empty line that is part of the same strophe as the lines
				 * surrounding it, joining them together.
				 */
				annotationMatcher.appendReplacement(plainText, "\n");
			}
			else if ("trans off".equals(identifier))
			{
				/*
				 * Used for translated lyrics and similar constructs where two
				 * strophes are essentially intertwined, to indicate that a
				 * particular strophe is unaffected by this. The annotation
				 * appears at the start of each strophe, but after a label (if
				 * present).
				 */
				annotationMatcher.appendReplacement(plainText, "");
			}
			else
			{
				System.out.println("Unknown annotation: " + identifier);
			}
		}
		annotationMatcher.appendTail(plainText);

		lyric.setText(plainText.toString());
	}

	private String fixNewlines(String string)
	{
		return string.replaceAll("\\r\\n?", "\n");
	}

	/**
	 * Returns whether the given value is intended as a NULL value. Apparently
	 * the concept of actually using NULLs hasn't quite reached the developers
	 * that defined the imported format.
	 *
	 * @param value The value to be checked.
	 *
	 * @return <code>true</code> if the value should be interpreted as a NULL;
	 *         <code>false</code> otherwise.
	 */
	private boolean isFakeNull(String value)
	{
		return "#".equals(value);
	}
}
