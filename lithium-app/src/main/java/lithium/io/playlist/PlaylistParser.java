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
import java.text.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.validation.*;

import lithium.*;
import lithium.catalog.*;
import lithium.config.*;
import lithium.io.*;
import lithium.io.Parser;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * A parser for catalogs stored in Lithium's XML file format.
 *
 * @author Gerrit Meinders
 */
public class PlaylistParser extends ConfigurationSupport
implements
        Parser<Playlist>
{
	/** The catalog being constructed by the parser. */
	protected MutableCatalog catalog;

	/** The input source of the parser. */
	private Reader in;

	/** Context URI used to resolve relative URIs. */
	private URI context;

	/** Constructs a new catalog parser. */
	public PlaylistParser()
	{
		in = null;
	}

	/**
	 * Sets the input source of the parser.
	 *
	 * @param in the reader to be used as an input source
	 */
	public void setInput(Reader in)
	{
		this.in = in;
	}

	public URI getContext()
	{
		return context;
	}

	public void setContext(URI context)
	{
		this.context = context;
	}

	/**
	 * Constructs a catalog from the parser's input source.
	 *
	 * @return the catalog
	 * @throws IOException if an exception occurs while parsing
	 */
	public Playlist call() throws IOException
	{
		if (in == null)
		{
			throw new NullPointerException("input not set");
		}

		Document document;
		try
		{
			// open and parse source file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new DefaultEntityResolver());
			document = builder.parse(new InputSource(in));
			in.close();
		}
		catch (SAXException e)
		{
			throw (IOException) new IOException().initCause(e);
		}
		catch (ParserConfigurationException e)
		{
			throw new AssertionError(e);
		}

		Playlist playlist;

		Element root = document.getDocumentElement();
		if (PlaylistIO.LITHIUM_PLAYLIST_NS_URI.equals(root.getNamespaceURI()))
		{
			String version = root.getAttribute("version");

			if ("1.1".equals(version))
			{
				validate(document, PlaylistIO.LITHIUM_PLAYLIST_1_1);
				playlist = parsePlaylist1_1(document);
			}
			else
			{
				throw new IOException();
			}
		}
		else if ("playlist".equals(root.getTagName()))
		{
			playlist = parsePlaylist0(document);
		}
		else
		{
			throw new IOException();
		}

		return playlist;
	}

	private void validate(Document document, String schemaLocation)
	        throws IOException
	{
		try
		{
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(getClass().getResource(
			        schemaLocation));

			Validator validator = schema.newValidator();
			validator.validate(new DOMSource(document), new DOMResult(document));
		}
		catch (SAXException e)
		{
			throw new IOException(e);
		}
	}

	private Playlist parsePlaylist1_1(Document document) throws IOException
	{
		Playlist playlist = new Playlist();

		Element rootElement = document.getDocumentElement();
		NodeList childNodes = rootElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element)
			{
				Element element = (Element) childNode;
				String tagName = element.getTagName();

				if ("lyric".equals(tagName))
				{
					LyricRef lyricRef = new LyricRef(
					        element.getAttribute("bundle"),
					        Integer.parseInt(element.getAttribute("number")));
					playlist.add(new PlaylistItem(lyricRef));
				}
				else if ("text".equals(tagName))
				{
					playlist.add(new PlaylistItem(element.getTextContent()));
				}
				else if ("book".equals(tagName))
				{
					// TODO
				}
				else if ("image".equals(tagName))
				{
					try
					{
						URI uri = new URI(element.getAttribute("src"));
						URI resolved = getContext().resolve(uri);
						playlist.add(new PlaylistItem(new ImageRef(
						        resolved.toURL())));
					}
					catch (URISyntaxException e)
					{
						throw new IOException(e);
					}
				}
			}
			else
			{
				// Ignored.
			}
		}

		return playlist;
	}

	private Playlist parsePlaylist0(Document document) throws IOException
	{
		Playlist playlist = new Playlist();

		Element rootElement = document.getDocumentElement();
		NodeList childNodes = rootElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);

			PlaylistItem item;

			if (node instanceof Element)
			{
				Element element = (Element) node;
				String tagName = element.getTagName();

				if ("lyric-ref".equals(tagName))
				{
					String bundle = element.getAttribute("bundle");
					int number;
					try
					{
						number = Integer.parseInt(element.getAttribute("number"));
					}
					catch (NumberFormatException e)
					{
						throw (IOException) new IOException().initCause(new ParseException(
						        e.getMessage(), 0));
					}
					LyricRef lyricRef = new LyricRef(bundle, number);
					item = new PlaylistItem(lyricRef);

				}
				else if ("text".equals(tagName))
				{
					String text = element.getTextContent();
					item = new PlaylistItem(text.trim());

				}
				else
				{
					/* Ignore unknown element. */
					continue;
				}

				playlist.add(item);
			}
		}

		playlist.setModified(false);

		return playlist;
	}
}
