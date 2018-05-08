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
import java.util.zip.*;
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;

import lithium.catalog.*;
import org.w3c.dom.*;

public class HTMLArchiveExporter
{
	public HTMLArchiveExporter()
	{
	}

	public void export(MutableCatalog catalog, File file) throws IOException,
	        XMLStreamException
	{
		Transformer identityTransformer;
		try
		{
			identityTransformer = TransformerFactory.newInstance().newTransformer();
		}
		catch (Exception e)
		{
			throw new AssertionError(e);
		}
		Transformer indexTransformer = createTransformer("catalog-index.xsl");
		Transformer bundleTransformer = createTransformer("catalog-bundle.xsl");
		Transformer lyricTransformer = createTransformer("catalog-lyric.xsl");

		XPath xpath = XPathFactory.newInstance().newXPath();

		NamespaceSupportContext namespaceContext = new NamespaceSupportContext();
		namespaceContext.declarePrefix("cat", CatalogIO.NAMESPACE);
		xpath.setNamespaceContext(namespaceContext);

		Document catalogDocument = CatalogIO.buildDocument(catalog, false);

		FileOutputStream fileOut = new FileOutputStream(file);
		try
		{
			ZipOutputStream zipOut = new ZipOutputStream(
			        new BufferedOutputStream(fileOut));

			/*
			 * Write table of contents.
			 */
			zipOut.putNextEntry(new ZipEntry("index.html"));
			transform(catalogDocument, indexTransformer, zipOut);

			/*
			 * Write bundles.
			 */
			NodeList bundleElements = (NodeList) xpath.evaluate(
			        "//cat:group[cat:lyric/@title]", catalogDocument,
			        XPathConstants.NODESET);
			for (int i = 0; i < bundleElements.getLength(); i++)
			{
				Element bundleElement = (Element) bundleElements.item(i);
				String bundleName = bundleElement.getAttribute("name");

				zipOut.putNextEntry(new ZipEntry(bundleName + "/index.html"));
				transform(bundleElement, bundleTransformer, zipOut);

				/*
				 * Write lyrics.
				 */
				NodeList lyricElements = (NodeList) xpath.evaluate(
				        "cat:lyric[@title]", bundleElement,
				        XPathConstants.NODESET);
				for (int j = 0; j < lyricElements.getLength(); j++)
				{
					Element lyricElement = (Element) lyricElements.item(j);
					int lyricNumber = Integer.parseInt(lyricElement.getAttribute("number"));

					zipOut.putNextEntry(new ZipEntry(bundleName + "/"
					        + lyricNumber + ".html"));
					transform(lyricElement, lyricTransformer, zipOut);
				}
			}

			zipOut.closeEntry();
			zipOut.close();
		}
		catch (XPathExpressionException e)
		{
			throw new IOException(e);
		}
		finally
		{
			fileOut.close();
		}
	}

	private Transformer createTransformer(String templateName)
	        throws IOException
	{
		try
		{
			StreamSource transformerSource = new StreamSource(
			        getClass().getResource(templateName).openStream());
			TransformerFactory factory = TransformerFactory.newInstance();
			return factory.newTransformer(transformerSource);
		}
		catch (TransformerConfigurationException e)
		{
			throw new AssertionError(e);
		}
	}

	private void transform(Node node, Transformer transformer, OutputStream out)
	        throws IOException
	{
		DOMSource source = new DOMSource(node);
		StreamResult result = new StreamResult(out);
		try
		{
			transformer.transform(source, result);
		}
		catch (TransformerException e)
		{
			throw new IOException(e);
		}
	}
}
