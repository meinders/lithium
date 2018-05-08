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
import java.text.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import lithium.*;
import lithium.Config.*;
import lithium.config.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import static lithium.io.ConfigIO.*;

/**
 * A parser that reads only basic configuration settings from opwViewer
 * configuration files.
 *
 * @version 0.9 (2006.03.12)
 * @author Gerrit Meinders
 */
public class BasicConfigParser extends ConfigurationSupport
implements Parser<Config> {
    /** The configuration being constructed by the parser. */
    private Config config;

    /** The input source of the parser. */
    private Reader in = null;

    /** The URL used to resolve relative URIs. */
    private URL context;

    /** Constructs a new config parser. */
    public BasicConfigParser() {
        try {
            context = new File("").toURI().toURL();
        } catch (MalformedURLException e) {
            // nothing to be done about it
            e.printStackTrace();
        }
    }

    /**
     * Sets the input source of the parser.
     *
     * @param in the reader to be used as an input source
     */
    public void setInput(Reader in) {
        this.in = in;
    }

    /**
     * Sets the URL used to resolve relative URIs in the parsed configuration.
     *
     * @param context the context URL
     */
    public void setContext(URL context) {
        this.context = context;
    }

    /**
     * Reads a configuration from the parser's input source.
     *
     * @return the configuration
     * @throws IOException if an exception occurs while parsing
     */
    public Config call() throws IOException {
        if (in == null) {
            throw new NullPointerException("input not set");
        }

		Document document;
		try {
			DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new DefaultEntityResolver());
			document = builder.parse(new InputSource(in));
            in.close();

        } catch (SAXException e) {
            throw (IOException) new IOException().initCause(e);
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }

        String namespace = document.getDocumentElement().getNamespaceURI();
        if (NAMESPACE.equals(namespace)) {
            try {
                return parseConfig(document);
            } catch (XPathExpressionException e) {
                throw (IOException) new IOException().initCause(e);
            }
        } else {
            // use DTD for backward-compatibility
            DocumentType doctype = document.getDoctype();
            if (doctype == null) {
                throw (IOException) new IOException().initCause(
                        new ParseException(INVALID_DTD, 0));
            }

            String publicId = doctype.getPublicId();
            if (PUBLIC_ID.equals(publicId)) {
                return parseOldConfig(document);
            } else {
                throw (IOException) new IOException().initCause(
                        new ParseException(INVALID_DTD, 0));
            }
        }
    }

    protected Config parseConfig(Document document)
            throws IOException, XPathExpressionException {
		config = new Config();

        XPath xpath = XPathFactory.newInstance().newXPath();
        Element root = document.getDocumentElement();

        // define config namespace prefix (XPath needs it)
        NamespaceSupportContext nsContext = new NamespaceSupportContext();
        nsContext.declarePrefix("cfg", NAMESPACE);
        xpath.setNamespaceContext(nsContext);

        // hardware acceleration
        String acceleration = xpath.evaluate(
                "cfg:displays[1]/@acceleration", root);
        if (acceleration.length() > 0) {
            config.setAcceleration(Enum.valueOf(Acceleration.class,
                    acceleration.toUpperCase()));
        }

        return config;
    }

    protected Config parseOldConfig(Document document) throws IOException {
        return new Config();
	}
}

