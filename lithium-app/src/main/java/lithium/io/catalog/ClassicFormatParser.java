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

import com.github.meinders.common.*;
import lithium.*;
import lithium.catalog.*;
import lithium.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * <p>This class parses the file format used by the original software package
 * from the lyric cds from Stichting Opwekking. All known versions (2003-2005)
 * use the same format, which is completely supported by this class.</p>
 *
 * <p>Several kinds of errors found on the original cds are automatically fixed.
 * These include inconsistent capitalization, use of slightly different bundle
 * names (within the same bundle) and excessive white space.</p>
 *
 * <p>The file format seems to be some kind of SGML, but is not valid XML, due
 * to the lack of a single root element. The files are encoded using the CP1252
 * character set, a derivative of ISO-8859-1 that is commonly used on Windows
 * platforms.</p>
 *
 * @author Gerrit Meinders
 */
public class ClassicFormatParser extends GenericWorker<MutableCatalog> {
    private File source;

    /**
     * The common bundle name used for all bundle fragments, used to remove
     * spelling errors and other inconsistencies in bundle naming.
     */
    private String commonName = null;

    /**
     * Constructs a new classic format parser for the given source.
     *
     * @param source the file to be parsed
     */
    public ClassicFormatParser(File source) {
        super();
        this.source = source;
    }

    @Override
    public MutableCatalog construct() {
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        fireWorkerStarted();

        File[] files = source.listFiles(
                new ExtensionFileFilter("", "opw", false));

        MutableCatalog catalog = new DefaultCatalog();

        for (int i=0; i<files.length; i++) {
            File file = files[i];

            if (Thread.interrupted()) {
                fireWorkerInterrupted();
                return null;
            }

            fireWorkerProgress(i, files.length, file.getName());
            try {
                Catalog fragment = parse(file);
                Group bundle = fragment.getBundles().iterator().next();

                if (commonName == null) {
                    commonName = bundle.getName();
                }
                bundle.setName(commonName);

                DefaultCatalog.merge(catalog, fragment);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return catalog;
    }

    private String capitalize(String text) {
        String[] words = text.split(" ");
        StringBuilder newText = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                newText.append(word.substring(0, 1).toUpperCase());
                newText.append(word.substring(1, word.length()).toLowerCase());
            }
            newText.append(' ');
        }
        newText.deleteCharAt(newText.length() - 1);
        return newText.toString();
    }

    /**
     * Trims only the *trailing* spaces of each line in the given text.
     *
     * @param text the text to be processed
     * @return the text with each line's trailing white space removed
     */
    private String trimLines(String text) {
        String[] lines = text.split("[ \t\f]+\n");
        StringBuilder newText = new StringBuilder();
        for (String line : lines) {
            newText.append(line);
            newText.append('\n');
        }
        newText.deleteCharAt(newText.length() - 1);
        return newText.toString();
    }

    private MutableCatalog parse(File file) {
        // open inputstream to source file
        InputStream sourceIn;
        try {
            sourceIn = new BufferedInputStream(
                new FileInputStream(file));
        } catch (IOException e) {
            // TODO: user friendly message
            fireWorkerError(e);
            return null;
        }

        // create inputstream that produces correct XML
        InputStream fixedEntityRefIn = new FixEntitiesInputStream(sourceIn);
        String start = "<root>\n";
        String end = "</root>";
        byte[] startBytes = start.getBytes();
        byte[] endBytes = end.getBytes();
        InputStream startStream = new ByteArrayInputStream(startBytes);
        InputStream endStream = new ByteArrayInputStream(endBytes);

        InputStream xmlIn;
        xmlIn = new SequenceInputStream(startStream, fixedEntityRefIn);
        xmlIn = new SequenceInputStream(xmlIn, endStream);

        try {
            return parse(file, xmlIn);

        } finally {
            try {
                xmlIn.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private MutableCatalog parse(File file, InputStream xmlIn) {
        LineNumberReader in = new LineNumberReader(
                new InputStreamReader(xmlIn));

        ClassicFormatHandler handler = new ClassicFormatHandler();
        try {
            // parse the file
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            InputSource source = new InputSource(in);
            source.setEncoding("CP1252");
            ParserAdapter pa = new ParserAdapter(sp.getParser());
            pa.setContentHandler(handler);
            pa.parse(source);

	    } catch (Exception e) {
            // TODO: user friendly message
            fireWorkerError(e);
            return null;
        }

        // create Catalog object
        try {
            MutableCatalog catalog = new DefaultCatalog();

            String bundleName;
            if (commonName == null) {
                bundleName = handler.get("bundel").trim();
                bundleName = capitalize(bundleName);
            } else {
                bundleName = commonName;
            }

            String version = handler.get("versie");
            if (version == null) {
                version = "";
            }
            Bundle bundle = new Bundle(bundleName, version);
            catalog.addBundle(bundle);

            String numberString = handler.get("nummer");
            int number = Integer.parseInt(numberString);
            String title = handler.get("titel");
            Lyric lyric = new DefaultLyric(number, title);
            bundle.addLyric(lyric);

            // text
            String text = handler.get("tekst");
            if (text == null) {
                /*
                throw new ParseException(
                    "Required element <tekst> is missing or empty." +
                    " bundle = " + bundle.getName() +
                    ", lyric = " + lyric.getNumber(), 0);
                */
                // TODO: fire warning
                text = "";
            }
            lyric.setText(trimLines(text));

            // copyrights
            String copyrights = handler.get("copyrights");
            if (copyrights != null) {
                lyric.setCopyrights(trimLines(copyrights));
            }

            // originalTitle
            String originalTitle = handler.get("oorsprong");
            if (originalTitle != null) {
                lyric.setOriginalTitle(originalTitle);
            }

            // cd
            // TODO: seems preferable to prefix cd name with bundle name
            String cdShortName = handler.get("cd");
            if (cdShortName != null) {
                String cdName = bundleName + " " + cdShortName.trim();
                CD cd = new CD(cdName);
                cd.addLyric(lyric);
                catalog.addCD(cd);
            }

            // categories
            for (int i=1; ; i++) {
                String categoryName = handler.get("categorie" + i);
                if (categoryName == null) {
                    break;
                }
                categoryName = categoryName.trim();

                Category category = new Category(categoryName);
                category.addLyric(lyric);
                catalog.addCategory(category);
            }

            // keys
            for (int i=1; ; i++) {
                String key = handler.get("toonsoort" + i);
                if (key == null) break;

                lyric.addKey(key);
            }

            // bible verwijzingen
            for (int i=1; ; i++) {
                String bibleRefString = handler.get("bijbel" + i);
                if (bibleRefString == null) break;

                try {
                    BibleRef bibleRef = BibleRefParser.parse(bibleRefString);
                    lyric.addBibleRef(bibleRef);

                } catch (Exception e) {
                    String message = Resources.get().getString(
                            "classicFormatParser.invalidBibleRef",
                            file.getName(), in.getLineNumber(), bibleRefString);
                    fireWorkerWarning(message);
                }
            }

            return catalog;

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void finished() {
        fireWorkerFinished();
    }

    private class ClassicFormatHandler extends DefaultHandler {
        private boolean insideRoot = false;
        private boolean insideElement = false;
        private String qName = null;
        private StringBuffer elementContent = null;
        private Map<String,String> propertyMap;

        private ClassicFormatHandler() {
            propertyMap = new HashMap<String,String>();
        }

        private String get(String key) {
            return propertyMap.get(key);
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            if (!insideRoot) {
                insideRoot = true;
            } else {
                if (!insideElement) {
                    insideElement = true;
                    this.qName = qName;
                    elementContent = new StringBuffer();
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (insideRoot) {
                if (insideElement) {
                    elementContent.append(ch, start, length);
                }
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) {
            if (insideRoot) {
                if (insideElement) {
                    if (qName.equals(this.qName)) {
                        if (elementContent.length() > 0) {
                            propertyMap.put(qName, elementContent.toString());
                        }
                        elementContent = null;
                        insideElement = false;
                    }
                } else {
                    insideRoot = false;
                }
            }
        }
    }
}

