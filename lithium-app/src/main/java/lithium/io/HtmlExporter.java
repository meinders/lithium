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
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import lithium.catalog.*;

/**
 * This class provides methodes to export catalogs as HTML.
 *
 * @version 0.9 (2006.02.06)
 * @author Gerrit Meinders
 */
public class HtmlExporter {
    private static final String XSL_TEMPLATE_NAME = "catalog.xsl";

    private Transformer transformer;

    public HtmlExporter() throws IOException {
        try {
            StreamSource transformerSource = new StreamSource(
                    getClass().getResource(XSL_TEMPLATE_NAME).openStream());
            TransformerFactory factory = TransformerFactory.newInstance();
            transformer = factory.newTransformer(transformerSource);
        } catch (TransformerConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    public void export(Catalog catalog, File file) throws IOException {
        DOMSource source = new DOMSource(CatalogIO.buildDocument(catalog));
        StreamResult result = new StreamResult(file);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
}

