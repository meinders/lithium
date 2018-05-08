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

package lithium.io.catalog;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import com.github.meinders.common.*;
import lithium.catalog.*;
import lithium.config.*;
import lithium.io.*;
import org.w3c.dom.*;

import static lithium.io.CatalogIO.*;

/**
 * A builder for constructing an XML document representation of a catalog using
 * Lithium's XML-based catalogs file format. This class implements the new
 * catalog format, introduced in version 0.9.
 *
 * @author Gerrit Meinders
 */
public class CatalogBuilder extends ConfigurationSupport
implements
        Builder<Document>
{
	/** The catalog being processed. */
	protected Catalog catalog;

	/** The document being constructed. */
	protected Document document;

	/** The writer that receives the output of the builder. */
	private Writer out = null;

	/** Add newlines and indentation to improve human-readability. */
	private boolean indent = true;

	private enum Format
	{
		CATALOG_V09, CATALOG_V10
	}

	/**
	 * Constructs a new catalog builder for the given catalog.
	 *
	 * @param catalog the catalog
	 */
	public CatalogBuilder(Catalog catalog)
	{
		this.catalog = catalog;
	}

	/** {@inheritDoc} */
	public void setOutput(Writer out)
	{
		this.out = out;
	}

	public void setIndent(boolean indent)
	{
		this.indent = indent;
	}

	/**
	 * Builds an XML document representation of the catalog.
	 *
	 * @return the constructed document
	 */
	public Document call() throws IOException
	{
		try
		{
			Document document = buildDocument(Format.CATALOG_V10);
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

	protected Document buildDocument(Format format)
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
		document = domImpl.createDocument(NAMESPACE, "catalog", null);

		// build XML document
		Element documentElement = document.getDocumentElement();
		if (format == Format.CATALOG_V09)
		{
			documentElement.setAttribute("version", "0.9");
		}
		else
		{
			documentElement.setAttribute("version", "1.0");
		}

		for (Group group : catalog.getGroups())
		{
			Element groupElement = buildGroup(group, format);
			documentElement.appendChild(groupElement);
		}

		if (indent)
		{
			// insert whitespace to improve readability
			XMLFormatter.formatXML(document, false);
		}

		return document;
	}

	private Element buildGroup(Group group, Format format)
	{
		Element element = document.createElementNS(NAMESPACE, "group");

		if (group instanceof TypedGroup)
		{
			TypedGroup typedGroup = (TypedGroup) group;
			String type = typedGroup.getType().toString().toLowerCase();
			element.setAttribute("type", type);
			if (!group.getName().equals(group.getDisplayName()))
			{
				element.setAttribute("name", group.getName());
			}
		}
		else
		{
			element.setAttribute("name", group.getName());
		}

		if (group.getVersion().length() > 0)
		{
			element.setAttribute("version", group.getVersion());
		}

		for (Group subGroup : group.getGroups())
		{
			Element groupElement = buildGroup(subGroup, format);
			element.appendChild(groupElement);
		}

		if (group instanceof ReferenceGroup)
		{
			for (Lyric lyric : group.getLyrics())
			{
				Lyric refLyric = new ReferenceLyric(createLyricRef(lyric));
				Element lyricElement = buildLyric(refLyric, format);
				element.appendChild(lyricElement);
			}
		}
		else
		{
			for (Lyric lyric : group.getLyrics())
			{
				Element lyricElement = buildLyric(lyric, format);
				element.appendChild(lyricElement);
			}
		}

		return element;
	}

	private LyricRef createLyricRef(Lyric lyric)
	{
		if (lyric instanceof ReferenceLyric)
		{
			return ((ReferenceLyric) lyric).getReference();
		}
		else
		{
			Set<Group> groups = catalog.getGroups(lyric);
			if (groups.isEmpty())
			{
				return null;
			}
			else
			{
				for (Group group : groups)
				{
					if (group instanceof ContainerGroup
					        && (group.getLyric(lyric.getNumber()) == lyric))
					{
						return new LyricRef(group.getName(), lyric.getNumber());
					}
				}

				throw new IllegalArgumentException(
				        "no ContainerGroup found for lyric");
			}
		}
	}

	private Element buildLyric(Lyric lyric, Format format)
	{
		Element element = document.createElementNS(NAMESPACE, "lyric");
		int number = lyric.getNumber();
		element.setAttribute("number", "" + number);

		if (lyric instanceof ReferenceLyric)
		{
			ReferenceLyric referenceLyric = (ReferenceLyric) lyric;
			LyricRef reference = referenceLyric.getReference();
			element.setAttribute("ref", reference.getBundle());
			int refNumber = reference.getNumber();
			if (refNumber != number)
			{
				element.setAttribute("refNumber", "" + refNumber);
			}
			return element;

		}
		else
		{
			element.setAttribute("title", lyric.getTitle());
		}

		// text
		Element textElement = document.createElementNS(NAMESPACE, "text");
		textElement.appendChild(document.createTextNode(lyric.getText()));
		element.appendChild(textElement);

		// originalTitle
		String originalTitle = lyric.getOriginalTitle();
		if (originalTitle != null)
		{
			Element originalTitleElement = document.createElementNS(NAMESPACE,
			        "originalTitle");
			originalTitleElement.appendChild(document.createTextNode(originalTitle));
			element.appendChild(originalTitleElement);
		}

		// copyrights
		String copyrights = lyric.getCopyrights();
		if (copyrights != null)
		{
			Element copyrightsElement = document.createElementNS(NAMESPACE,
			        "copyrights");
			copyrightsElement.appendChild(document.createTextNode(copyrights));
			element.appendChild(copyrightsElement);
		}

		// bible references
		for (BibleRef bibleRef : lyric.getBibleRefs())
		{
			Element bibleRefElement = buildBibleRef(bibleRef, format);
			element.appendChild(bibleRefElement);
		}

		// keys
		for (String key : lyric.getKeys())
		{
			Element keyElement = document.createElementNS(NAMESPACE, "key");
			keyElement.appendChild(document.createTextNode(key));
			element.appendChild(keyElement);
		}

		return element;
	}

	private Element buildBibleRef(BibleRef bibleRef, Format format)
	{
		Element element;
		if (format == Format.CATALOG_V09)
		{
			element = document.createElementNS(NAMESPACE, "bible-ref");
		}
		else
		{
			element = document.createElementNS(NAMESPACE, "bibleRef");
		}
		element.setAttribute("book", "" + bibleRef.getBookIndex());

		final Integer startChapter = bibleRef.getStartChapter();
		final Integer endChapter = bibleRef.getEndChapter();

		if (startChapter != null)
		{
			element.setAttribute("chapter", "" + startChapter);
			if (startChapter != endChapter)
			{
				element.setAttribute("endChapter", "" + endChapter);
			}
		}

		final Integer startVerse = bibleRef.getStartVerse();
		final Integer endVerse = bibleRef.getEndVerse();

		if (startVerse != null)
		{
			element.setAttribute("verse", "" + startVerse);
			if (startChapter != endChapter || startVerse != endVerse)
			{
				element.setAttribute("endVerse", "" + endVerse);
			}
		}

		return element;
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

			if (catalog instanceof MutableCatalog)
			{
				((MutableCatalog) catalog).setModified(false);
			}

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
