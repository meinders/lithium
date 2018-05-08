package lithium.io.playlist;

import java.io.*;
import java.net.*;
import java.util.*;

import junit.framework.*;
import lithium.*;
import lithium.catalog.*;
import lithium.io.*;

public class PlaylistIOTest extends TestCase
{
	public void testPlaylistInput1_1() throws IOException, URISyntaxException
	{
		URL source = getClass().getResource("playlist-1.1.xml");
		URI sourceURI = source.toURI();

		Playlist playlist = PlaylistIO.read(source);

		Collection<PlaylistItem> items = playlist.getItems();
		Iterator<PlaylistItem> iterator = items.iterator();

		{
			PlaylistItem item = iterator.next();
			LyricRef lyricRef = (LyricRef) item.getValue();
			assertEquals("bundle1", lyricRef.getBundle());
			assertEquals(123, lyricRef.getNumber());
		}

		{
			PlaylistItem item = iterator.next();
			LyricRef lyricRef = (LyricRef) item.getValue();
			assertEquals("bundle2", lyricRef.getBundle());
			assertEquals(234, lyricRef.getNumber());
		}

		{
			PlaylistItem item = iterator.next();
			String text = (String) item.getValue();
			assertEquals("Lorem ipsum dolor sit amet,\n"
			        + "consectetur adipiscing elit.\n"
			        + "Proin hendrerit orci\n"
			        + "sed sapien eleifend aliquam.\n"
			        + "Etiam at dolor ac metus\n"
			        + "consectetur porta in eu nisl.", text);
		}

		{
			PlaylistItem item = iterator.next();
			ImageRef imageRef = (ImageRef) item.getValue();
			URI expected = sourceURI.resolve("playlist-1.1.png");
			assertEquals(expected.toURL(), imageRef.getSource());
		}

		assertFalse(iterator.hasNext());
	}

	public void testPlaylistInput1_0() throws IOException
	{
		URL source = getClass().getResource("playlist-1.0.xml");
		Playlist playlist = PlaylistIO.read(source);

		Collection<PlaylistItem> items = playlist.getItems();
		Iterator<PlaylistItem> iterator = items.iterator();

		{
			PlaylistItem item = iterator.next();
			LyricRef lyricRef = (LyricRef) item.getValue();
			assertEquals("bundle1", lyricRef.getBundle());
			assertEquals(123, lyricRef.getNumber());
		}

		{
			PlaylistItem item = iterator.next();
			LyricRef lyricRef = (LyricRef) item.getValue();
			assertEquals("bundle2", lyricRef.getBundle());
			assertEquals(234, lyricRef.getNumber());
		}

		{
			PlaylistItem item = iterator.next();
			String text = (String) item.getValue();
			assertEquals("Lorem ipsum dolor sit amet,\n"
			        + "consectetur adipiscing elit.\n"
			        + "Proin hendrerit orci\n"
			        + "sed sapien eleifend aliquam.\n"
			        + "Etiam at dolor ac metus\n"
			        + "consectetur porta in eu nisl.", text);
		}

		assertFalse(iterator.hasNext());
	}
}
