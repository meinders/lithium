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
import java.text.*;
import java.util.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.validation.*;

import lithium.catalog.*;
import lithium.catalog.TypedGroup.*;
import lithium.config.*;
import lithium.io.*;
import lithium.io.Parser;
import org.w3c.dom.*;
import org.xml.sax.*;

import static lithium.io.CatalogIO.*;

/**
 * A parser for catalogs stored in Lithium's XML file format.
 *
 * @author Gerrit Meinders
 */
public class CatalogParser extends ConfigurationSupport
implements Parser<MutableCatalog> {
    /** The catalog being constructed by the parser. */
    protected MutableCatalog catalog;

    /** The input source of the parser. */
    private Reader in;

    /** Constructs a new catalog parser. */
    public CatalogParser() {
        in = null;
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
     * Constructs a catalog from the parser's input source.
     *
     * @return the catalog
     * @throws IOException if an exception occurs while parsing
     */
    public MutableCatalog call() throws IOException {
        if (in == null) {
            throw new NullPointerException("input not set");
        }

        Document document;
        try {
            // open and parse source file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
            return parseCatalog(document);
        } else {
            // use DTD for backward-compatibility
            DocumentType doctype = document.getDoctype();
            if (doctype == null) {
                throw (IOException) new IOException().initCause(new ParseException(
                        INVALID_DTD, 0));
            }

            String publicId = doctype.getPublicId();
            if (PUBLIC_ID.equals(publicId)) {
                return parseCatalog(document);
            } else {
                throw (IOException) new IOException().initCause(new ParseException(
                        INVALID_DTD, 0));
            }
        }
    }

    private MutableCatalog parseCatalog(Document document) throws IOException {
        catalog = new DefaultCatalog();

        Element element = document.getDocumentElement();
        if (element.hasAttribute("version")) {
            String version = element.getAttribute("version");
            if ("1.0".equals(version)) {
                try {
                    return parseCatalogV10(document);
                } catch (SAXException e) {
                    throw new IOException("Illegal catalog", e);
                }
            } else if ("0.9".equals(version)) {
                return parseCatalogV09(document);
            } else {
                throw new IOException("Unsupported version: " + version);
            }
        } else {
            return parseOldCatalog(document);
        }
    }

    @Deprecated
    private MutableCatalog parseOldCatalog(Document document) {
        Element element = document.getDocumentElement();
        NodeList bundleElements = element.getElementsByTagName("bundle");
        for (int i = 0; i < bundleElements.getLength(); i++) {
            Element bundleElement = (Element) bundleElements.item(i);
            Bundle bundle = parseBundle(bundleElement);
            catalog.addBundle(bundle);
        }

        NodeList categoryElements = element.getElementsByTagName("category");
        for (int i = 0; i < categoryElements.getLength(); i++) {
            Element categoryElement = (Element) categoryElements.item(i);
            Category category = parseCategory(categoryElement);
            catalog.addCategory(category);
        }

        NodeList cdElements = element.getElementsByTagName("cd");
        for (int i = 0; i < cdElements.getLength(); i++) {
            Element cdElement = (Element) cdElements.item(i);
            CD cd = parseCD(cdElement);
            catalog.addCD(cd);
        }

        catalog.setModified(false);

        return catalog;
    }

    private MutableCatalog parseCatalogV09(Document document) {
        Element element = document.getDocumentElement();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                String tagName = childElement.getTagName();
                if ("group".equals(tagName)) {
                    Group group = parseGroup(childElement, false);
                    catalog.addGroup(group);
                }
            }
        }
        catalog.setModified(false);
        return catalog;
    }

    /**
     * Parses a catalog document conforming to version 1.0 of the specification.
     *
     * @param document Document to be parsed.
     * @return Catalog stored in the given document.
     * @throws SAXException If a SAX error occurs during parsing.
     */
    private MutableCatalog parseCatalogV10(Document document) throws SAXException {
        // Load the appropriate schema.
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(getClass().getResource(
                CatalogIO.CATALOG_V10_SCHEMA_LOCATION));
        Validator schemaValidator = schema.newValidator();

        // Validate the document using the schema.
        DOMSource documentSource = new DOMSource(document);
        DOMResult documentResult = new DOMResult(document);
        try {
            schemaValidator.validate(documentSource, documentResult);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        // Parse the document.
        Element element = document.getDocumentElement();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                String tagName = childElement.getTagName();
                if ("group".equals(tagName)) {
                    Group group = parseGroup(childElement, true);
                    catalog.addGroup(group);
                }
            }
        }
        catalog.setModified(false);
        return catalog;
    }

    private Group parseGroup(Element element, boolean version10) {
        // parse basic group attributes
        String name = element.getAttribute("name");
        if (name.length() == 0) {
            name = null;
        }
        String version = element.getAttribute("version");
        String type;
        if (element.hasAttribute("type")) {
            type = element.getAttribute("type");
        } else {
            type = null;
        }

        // parse sub-groups and lyrics
        Set<Group> groups = new LinkedHashSet<Group>();
        List<Lyric> lyrics = new ArrayList<Lyric>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                String tagName = childElement.getTagName();
                if ("lyric".equals(tagName)) {
                    lyrics.add(parseLyric(childElement, version10));
                } else if ("group".equals(tagName)) {
                    groups.add(parseGroup(childElement, version10));
                }
            }
        }

        // create suitable group instance
        Group group = null;
        if (type != null) {
            if ("bundles".equals(type)) {
                group = new TypedGroup(GroupType.BUNDLES, name, version);
            } else if ("categories".equals(type)) {
                group = new TypedGroup(GroupType.CATEGORIES, name, version);
            } else if ("cds".equals(type)) {
                group = new TypedGroup(GroupType.CDS, name, version);
            }
        }
        if (group == null) {
            boolean references = false;
            for (Lyric lyric : lyrics) {
                if (lyric instanceof ReferenceLyric) {
                    references = true;
                }
            }
            if (references) {
                group = new ReferenceGroup("Group.displayName", name, version);
            } else {
                group = new ContainerGroup("Group.displayName", name, version);
            }
        }

        // add lyrics and sub-groups
        for (Group subGroup : groups) {
            group.addGroup(subGroup);
        }
        for (Lyric lyric : lyrics) {
            group.addLyric(lyric);
        }

        return group;
    }

    @Deprecated
    private Bundle parseBundle(Element element) {
        String name = element.getAttribute("name");
        String version = element.getAttribute("version");
        Bundle bundle = new Bundle(name, version);

        NodeList lyricElements = element.getElementsByTagName("lyric");
        for (int i = 0; i < lyricElements.getLength(); i++) {
            Element lyricElement = (Element) lyricElements.item(i);
            Lyric lyric = parseLyric(lyricElement, false);
            bundle.addLyric(lyric);
        }

        return bundle;
    }

    private Lyric parseLyric(Element element, boolean version10) {
        int number = Integer.parseInt(element.getAttribute("number"));
        String title = element.getAttribute("title");

        if (element.hasAttribute("ref")) {
            String ref = element.getAttribute("ref");
            int refNumber;
            if (element.hasAttribute("refNumber")) {
                refNumber = Integer.parseInt(element.getAttribute("refNumber"));
            } else {
                refNumber = number;
            }
            LyricRef reference = new LyricRef(ref, refNumber);
            return new ReferenceLyric(number, reference);
        }

        Lyric lyric = new DefaultLyric(number, title);

        Node textNode = element.getElementsByTagName("text").item(0);
        lyric.setText(textNode.getTextContent());

        NodeList originalTitleNodes = element.getElementsByTagName("originalTitle");
        if (originalTitleNodes.getLength() > 0) {
            lyric.setOriginalTitle(originalTitleNodes.item(0).getTextContent());
        }

        NodeList copyrightsNodes = element.getElementsByTagName("copyrights");
        if (copyrightsNodes.getLength() > 0) {
            lyric.setCopyrights(copyrightsNodes.item(0).getTextContent());
        }

        NodeList bibleRefList;
        if (version10) {
            bibleRefList = element.getElementsByTagName("bibleRef");
        } else {
            bibleRefList = element.getElementsByTagName("bible-ref");
        }
        for (int i = 0; i < bibleRefList.getLength(); i++) {
            Element bibleRefElement = (Element) bibleRefList.item(i);
            BibleRef bibleRef = parseBibleRef(bibleRefElement);
            lyric.addBibleRef(bibleRef);
        }

        NodeList keyList = element.getElementsByTagName("key");
        for (int i = 0; i < keyList.getLength(); i++) {
            Element key = (Element)keyList.item( i );
            lyric.addKey( key.hasAttribute( "name" ) ? key.getAttribute( "name" ) : key.getTextContent() );
        }

        return lyric;
    }

    private BibleRef parseBibleRef(Element element) {
        int book = Integer.parseInt(element.getAttribute("book"));

        Integer startChapter = null;
        Integer endChapter = null;
        if (element.hasAttribute("chapter")) {
            startChapter = Integer.parseInt(element.getAttribute("chapter"));
            if (element.hasAttribute("endChapter")) {
                endChapter = Integer.parseInt(element.getAttribute("endChapter"));
            }
        }

        Integer startVerse = null;
        Integer endVerse = null;
        if (element.hasAttribute("verse")) {
            startVerse = Integer.parseInt(element.getAttribute("verse"));
            if (element.hasAttribute("endVerse")) {
                endVerse = Integer.parseInt(element.getAttribute("endVerse"));
            }
        }

        return new BibleRef(book, startChapter, endChapter, startVerse, endVerse);
    }

    @Deprecated
    private Category parseCategory(Element element) {
        String categoryName = element.getAttribute("name");
        Category category = new Category(categoryName);

        NodeList lyricRefList = element.getElementsByTagName("lyric-ref");
        for (int i = 0; i < lyricRefList.getLength(); i++) {
            Element lyricRefElement = (Element) lyricRefList.item(i);
            LyricRef lyricRef = parseLyricRef(lyricRefElement);
            Lyric lyric = catalog.getLyric(lyricRef);
            category.addLyric(lyric);
        }

        return category;
    }

    @Deprecated
    private CD parseCD(Element element) {
        String cdName = element.getAttribute("name");
        CD cd = new CD(cdName);

        NodeList lyricRefList = element.getElementsByTagName("lyric-ref");
        for (int i = 0; i < lyricRefList.getLength(); i++) {
            Element lyricRefElement = (Element) lyricRefList.item(i);
            LyricRef lyricRef = parseLyricRef(lyricRefElement);
            Lyric lyric = catalog.getLyric(lyricRef);
            cd.addLyric(lyric);
        }

        return cd;
    }

    @Deprecated
    private LyricRef parseLyricRef(Element element) {
        String bundleName = element.getAttribute("bundle");
        int lyricNumber = Integer.parseInt(element.getAttribute("number"));
        return new LyricRef(bundleName, lyricNumber);
    }
}
