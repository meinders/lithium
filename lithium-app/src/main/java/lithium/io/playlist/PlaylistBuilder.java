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

package lithium.io.playlist;

import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import lithium.*;
import lithium.catalog.*;
import lithium.config.*;
import lithium.io.*;
import org.w3c.dom.*;

/**
 * Constructs an XML document from a playlist, according to the Lithium 1.1
 * playlist format.
 *
 * @author Gerrit Meinders
 */
public class PlaylistBuilder extends ConfigurationSupport
implements
        Builder<Document>
{
	/** The playlist being processed. */
	protected Playlist playlist;

	/** The document being constructed. */
	protected Document document;

	/** The writer that receives the output of the builder. */
	private Writer out = null;

	/**
	 * Constructs a new playlist builder for the given playlist.
	 *
	 * @param playlist the playlist
	 */
	public PlaylistBuilder(Playlist playlist)
	{
		this.playlist = playlist;
	}

	public void setOutput(Writer out)
	{
		this.out = out;
	}

	/**
	 * Builds an XML document representation of the playlist.
	 *
	 * @return the constructed document
	 */
	public Document call() throws IOException
	{
		try
		{
			Document document = buildDocument();
			if (out != null)
			{
				writeDocument();
			}
			return document;
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private Document buildDocument() throws IOException
	{
		DocumentBuilder builder;
		try
		{
			// create document builder
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			builder = builderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
			return null;
		}

		// create document
		DOMImplementation domImpl = builder.getDOMImplementation();
		document = domImpl.createDocument(PlaylistIO.LITHIUM_PLAYLIST_NS_URI,
		        "playlist", null);

		// build XML document
		Element documentElement = document.getDocumentElement();
		documentElement.setAttribute("version", "1.1");

		for (PlaylistItem item : playlist.getItems())
		{
			Node element = buildPlaylistItem(item);
			if (element != null)
			{
				documentElement.appendChild(element);
			}
		}

		return document;
	}

	private Element buildPlaylistItem(PlaylistItem item) throws IOException
	{
		return buildPlaylistItem(item, item.getValue());
	}

	private Element buildPlaylistItem(PlaylistItem item, Object value)
	        throws IOException
	{
		Element result;

		if (value instanceof Lyric)
		{
			Lyric lyric = (Lyric) value;
			Catalog catalog = CatalogManager.getCatalog();
			Group bundle = catalog.getBundle(lyric);
			result = buildPlaylistItem(item, new LyricRef(bundle.getName(),
			        lyric.getNumber()));
		}
		else if (value instanceof LyricRef)
		{
			LyricRef lyricRef = (LyricRef) value;
			result = document.createElementNS(
			        PlaylistIO.LITHIUM_PLAYLIST_NS_URI, "lyric");
			result.setAttribute("bundle", lyricRef.getBundle());
			result.setAttribute("number", String.valueOf(lyricRef.getNumber()));
		}
		else if (value instanceof CharSequence)
		{
			CharSequence charSequence = (CharSequence) value;
			result = document.createElementNS(
			        PlaylistIO.LITHIUM_PLAYLIST_NS_URI, "text");
			result.appendChild(document.createTextNode(charSequence.toString()));
		}
		else if (value instanceof ImageRef)
		{
			ImageRef imageRef = (ImageRef) value;
			result = document.createElementNS(
			        PlaylistIO.LITHIUM_PLAYLIST_NS_URI, "image");

			try
			{
				URI sourceURI = imageRef.getSource().toURI();
				result.setAttribute("src", sourceURI.toString());
			}
			catch (URISyntaxException e)
			{
				throw new IOException(e);
			}
		}
		else
		{
			result = null;
		}

		return result;
	}

	protected void writeDocument() throws IOException
	{
		try
		{
			// transform DOM to Stream
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(out);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(source, result);

			playlist.setModified(false);
		}
		catch (TransformerConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			throw (IOException) new IOException().initCause(e);
		}
	}
}
