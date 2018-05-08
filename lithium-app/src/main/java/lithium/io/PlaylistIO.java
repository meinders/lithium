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

import lithium.*;
import lithium.io.playlist.*;
import org.w3c.dom.*;

/**
 * This class provides the ability to read and write playlists from and to files
 * in opwViewer's playlist file format. Reading from URLs is also supported.
 *
 * @author Gerrit Meinders
 */
public class PlaylistIO
{
	/**
	 * Namespace URI for Lithium playlists.
	 */
	public static final String LITHIUM_PLAYLIST_NS_URI = "urn:lithium:playlist";

	/**
	 * Resource name of the Lithium 1.1 playlist schema.
	 */
	public static final String LITHIUM_PLAYLIST_1_1 = "/lithium/io/playlist-1.1.xsd";

	/**
	 * Public ID for the playlist (version 0) document type.
	 *
	 * @deprecated Used only in the version 0 playlist format.
	 */
	public static final String LEGACY_PUBLIC_ID = "-//Frixus//DTD opwViewer Playlist 1.0//EN";

	/**
	 * Reads a playlist from the specified file.
	 *
	 * @param file the file to be read
	 *
	 * @return the playlist
	 *
	 * @throws IOException if an exception occurs while reading
	 */
	public static Playlist read(File file) throws IOException
	{
		return read(file.toURI().toURL());
	}

	/**
	 * Reads a playlist from the specified URL.
	 *
	 * @param source the URL
	 *
	 * @return the playlist
	 *
	 * @throws IOException if an exception occurs while reading
	 */
	public static Playlist read(URL source) throws IOException
	{
		try
		{
			PlaylistParser parser = new PlaylistParser();
			try
			{
				parser.setContext(source.toURI());
			}
			catch (URISyntaxException e)
			{
				/* Ignore. Attempt to parse without context. */
			}
			Task<Playlist> task = ParserUtilities.createParserTask(parser,
			        source);
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
	 * Reads a playlist from the specified reader.
	 *
	 * @param source the reader
	 * @param context the URI used to resolve any relative URLs
	 *
	 * @return the playlist
	 *
	 * @throws IOException if an exception occurs while reading
	 */
	public static Playlist read(Reader source, URI context) throws IOException
	{
		try
		{
			PlaylistParser parser = new PlaylistParser();
			parser.setContext(context);
			Task<Playlist> task = ParserUtilities.createParserTask(parser,
			        source);
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
	 * Writes the given playlist to the given file.
	 *
	 * @param playlist the playlist to be written
	 * @param file the name of the file to write the playlist to
	 *
	 * @throws IOException if an exception occurs while writing the file
	 */
	public static void write(Playlist playlist, String file) throws IOException
	{
		write(playlist, new File(file));
	}

	/**
	 * Writes the given playlist to the given file.
	 *
	 * @param playlist the playlist to be written
	 * @param file the file to write the playlist to
	 *
	 * @throws IOException if an exception occurs while writing the file
	 */
	public static void write(Playlist playlist, File file) throws IOException
	{
		PlaylistBuilder builder = new PlaylistBuilder(playlist);
		Task<Document> task = BuilderUtilities.createBuilderTask(builder, file);
		task.run();
	}

	private PlaylistIO()
	{
		/* Unused private constructor. */
	}
}
