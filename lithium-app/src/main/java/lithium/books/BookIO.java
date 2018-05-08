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

package lithium.books;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This class provides static methods to read and write collections to and from
 * files, using a XML/ZIP-based file format. By default, proxies are used to
 * increase performance by loading parts of a collection only when needed.
 *
 * @author Gerrit Meinders
 */
public class BookIO {
    /**
     * Returns the collection that results from reading the given file. Where
     * applicable, proxies will be used to read data only when needed.
     *
     * @param file the file
     * @return the collection
     */
    public static MemoryLibrary read(File file) throws IOException {
        return read(file, true);
    }

    /**
     * Returns the collection that results from reading the given file. If
     * <code>proxy</code> is set to <code>true</code>, proxies will be used
     * where applicable. Otherwise all data is immediately read from the file.
     *
     * @param proxy <code>true</code> to use proxies; <code>false</code>
     *        otherwise
     * @return the collection
     */
    public static MemoryLibrary read(File file, boolean proxy) throws IOException {
        Document collectionDoc = getDocument(file, "collection.xml");
        Element collectionElement = collectionDoc.getDocumentElement();
        return parseCollection(collectionElement, file, proxy);
    }

    /**
     * Writes the given collection to the ouput file.
     *
     * @param collection the collection
     * @param output the output file
     */
    public static void write(Library collection, File output) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(output);
        ZipOutputStream zipOut = new ZipOutputStream(fileOut);
        zipOut.setLevel(Deflater.BEST_SPEED);
        zipOut.putNextEntry(new ZipEntry("collection.xml"));
        writeDocument(buildCollection(collection), zipOut);

        for (Book book : collection.getBooks()) {
            String name = book.getTitle();
            zipOut.putNextEntry(new ZipEntry(name + "/book.xml"));
            writeDocument(buildBook(book), zipOut);

            for (Chapter chapter : book.getChapters()) {
                zipOut.putNextEntry(new ZipEntry(name + "/" + chapter.getNumber() + ".xml"));
                writeDocument(buildChapter(chapter), zipOut);
            }
        }

        zipOut.close();
    }

    /**
     * Returns the result from reading an XML document from the entry with the
     * given name in the given ZIP file.
     *
     * @param file the ZIP file
     * @param name the entry's name
     * @return the XML document
     */
    private static Document getDocument(File file, String name) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        try {
            InputStream in = zipFile.getInputStream(new ZipEntry(name));
            if (in == null) {
                throw new FileNotFoundException(name + " (" + file + ")");
            } else {
                return readDocument(in);
            }
        } finally {
            zipFile.close();
        }
    }

    /**
     * Returns the result of reading an XML document from the given input
     * stream.
     *
     * @param in the input stream
     * @return the XML document
     */
    private static Document readDocument(InputStream in) throws IOException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            return builder.parse(in);

        } catch (SAXException e) {
            IOException ex = new IOException(e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            IOException ex = new IOException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    /**
     * Writes the given document to the given output stream.
     *
     * @param document the document
     * @param out the output stream
     */
    private static void writeDocument(Document document, OutputStream out) throws IOException {
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(out);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            IOException ex = new IOException(e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (TransformerException e) {
            IOException ex = new IOException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    /**
     * Loads a non-proxy collection from the given element, loading linked
     * documents from the given context, which must be a ZIP file.
     *
     * @param collectionElement the element
     * @param context the ZIP file
     */
    private static Library parseCollection(Element collectionElement, File context)
            throws IOException {
        return parseCollection(collectionElement, context, false);
    }

    /**
     * Loads a collection from the given element, loading linked documents from
     * the given context, which must be a ZIP file. Proxies can be used to load
     * data only when needed. The collection itself doesn't actually use
     * proxies, but the books and chapters it contains do.
     *
     * @param collectionElement the element
     * @param context the ZIP file
     * @param proxy <code>true</code> to enable the use of proxies
     */
    private static MemoryLibrary parseCollection(Element collectionElement, File context,
            boolean proxy) throws IOException {
        String collectionName = null;
        if (collectionElement.hasAttribute("name")) {
            collectionName = collectionElement.getAttribute("name");
        }
        MemoryLibrary collection = new MemoryLibrary(collectionName);

        NodeList bookList = collectionElement.getElementsByTagName("book");
        for (int i = 0; i < bookList.getLength(); i++) {
            Element bookElement = (Element) bookList.item(i);
            MemoryBook book = parseBook(bookElement, context, "", proxy);
            collection.addBook(book);
        }

        return collection;
    }

    /**
     * Loads a book from the given element, loading linked documents from the
     * given context, which must be a ZIP file. Proxies can be used to load data
     * only when needed. A proxied book loads its chapters, which are also
     * proxied, if and when they are used.
     *
     * @param bookElement the element
     * @param context the ZIP file
     * @param path the current path used to resolve links
     * @param proxy <code>true</code> to enable the use of proxies
     */
    private static MemoryBook parseBook(Element bookElement, File context, String path,
            boolean proxy) throws IOException {
        String bookName;
        if (bookElement.hasAttribute("name")) {
            bookName = bookElement.getAttribute("name");
        } else {
            throw new IOException("missing name attribute");
        }

        if (bookElement.hasAttribute("src")) {
            String bookSrc = path + bookElement.getAttribute("src");
            String srcPath = bookSrc.substring(0, bookSrc.lastIndexOf('/') + 1);

            if (proxy) {
                return new BookProxy(bookName, context, bookSrc);
            } else {
                Document bookDoc = getDocument(context, bookSrc);
                Element srcBookElement = bookDoc.getDocumentElement();
                return parseBook(srcBookElement, context, srcPath, proxy);
            }
        }

        MemoryBook book = new MemoryBook(bookName);
        parseBook(book, bookElement, context, path, proxy);
        return book;
    }

    /**
     * Loads a book into the give Book object from the given element, loading
     * linked documents from the given context, which must be a ZIP file.
     * Proxies can be used to load data only when needed. A proxied book loads
     * its chapters, which are also proxied, if and when they are used.
     *
     * @param book the book
     * @param bookElement the element
     * @param context the ZIP file
     * @param path the current path used to resolve links
     * @param proxy <code>true</code> to enable the use of proxies
     */
    public static void parseBook(Book book, Element bookElement, File context, String path,
            boolean proxy) throws IOException {
        NodeList chapterList = bookElement.getElementsByTagName("chapter");
        for (int i = 0; i < chapterList.getLength(); i++) {
            Element chapterElement = (Element) chapterList.item(i);
            book.addChapter(parseChapter(chapterElement, context, path, proxy));
        }
    }

    private static Chapter parseChapter(Element chapterElement, File context, String path,
            boolean proxy) throws IOException {
        int chapterNumber;
        if (chapterElement.hasAttribute("number")) {
            chapterNumber = Integer.parseInt(chapterElement.getAttribute("number"));
        } else {
            throw new IOException("parseChapter: missing name attribute");
        }

        if (chapterElement.hasAttribute("src")) {
            String chapterSrc = path + chapterElement.getAttribute("src");
            String srcPath = chapterSrc.substring(0, chapterSrc.lastIndexOf('/') + 1);

            if (proxy) {
                return new ChapterProxy(chapterNumber, context, chapterSrc);
            } else {
                Document chapterDoc = getDocument(context, chapterSrc);
                Element srcChapterElement = chapterDoc.getDocumentElement();
                return parseChapter(srcChapterElement, context, srcPath, proxy);
            }
        }

        Chapter chapter = new MemoryChapter(chapterNumber);
        parseChapter(chapter, chapterElement, context, path, proxy);
        return chapter;
    }

    private static void parseChapter(Chapter chapter, Element chapterElement, File context,
            String path, boolean proxy) throws IOException {
        NodeList verseList = chapterElement.getElementsByTagName("verse");
        for (int i = 0; i < verseList.getLength(); i++) {
            Element verseElement = (Element) verseList.item(i);
            chapter.addVerse(parseVerse(verseElement));
        }
    }

    static Verse parseVerse(Element verseElement) throws IOException {
        String text = verseElement.getTextContent();

        if (verseElement.hasAttribute("range")) {
            String[] range = verseElement.getAttribute("range").split("-");
            if (range.length == 1) {
                int number = Integer.parseInt(range[0]);
                return new Verse(number, text);
            } else {
                int from = Integer.parseInt(range[0]);
                int to = Integer.parseInt(range[1]);
                return new Verse(from, to, text);
            }
        } else {
            throw new IOException("parseVerse: missing range attribute");
        }
    }

    private static Document buildCollection(Library collection) {
        Document document = newDocument();

        Element collectionElement = document.createElement("collection");
        collectionElement.setAttribute("name", collection.getName());
        document.appendChild(collectionElement);

        for (Book book : collection.getBooks()) {
            String bookName = book.getTitle();
            Element bookElement = document.createElement("book");
            bookElement.setAttribute("name", bookName);
            bookElement.setAttribute("src", bookName + "/book.xml");
            collectionElement.appendChild(bookElement);
        }

        return document;
    }

    private static Document buildBook(Book book) {
        Document document = newDocument();

        Element bookElement = document.createElement("book");
        bookElement.setAttribute("name", book.getTitle());
        document.appendChild(bookElement);

        for (Chapter chapter : book.getChapters()) {
            Element chapterElement = document.createElement("chapter");
            chapterElement.setAttribute("number", "" + chapter.getNumber());
            chapterElement.setAttribute("src", chapter.getNumber() + ".xml");
            bookElement.appendChild(chapterElement);
        }

        return document;
    }

    private static Document buildChapter(Chapter chapter) {
        Document document = newDocument();

        Element chapterElement = document.createElement("chapter");
        chapterElement.setAttribute("number", "" + chapter.getNumber());
        document.appendChild(chapterElement);

        for (Verse verse : chapter.getVerses()) {
            Element verseElement = document.createElement("verse");
            verseElement.setAttribute("range", verse.getRange());
            verseElement.appendChild(document.createTextNode(verse.getText()));
            chapterElement.appendChild(verseElement);
        }

        return document;
    }

    private static Document newDocument() {
        DocumentBuilder builder;
        try {
            // create document object
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return builder.newDocument();
    }

    private BookIO() {
        throw new AssertionError("This class must not be instantiated.");
    }

    /**
     * A proxy subclass of Book that loads the book's data on the fly when
     * needed.
     */
    private static class BookProxy extends MemoryBook {
        private boolean loaded;

        private boolean loading;

        private File file;

        private String entry;

        public BookProxy(String name, File file, String entry) {
            super(name);
            this.file = file;
            this.entry = entry;
        }

        @Override
        public String getTitle() {
            return super.getTitle();
        }

        @Override
        public Set<Chapter> getChapters() {
            ensureLoaded();
            return super.getChapters();
        }

        @Override
        public void addChapter(Chapter chapter) {
            ensureLoaded();
            super.addChapter(chapter);
        }

        @Override
        public Chapter getChapter(int number) {
            ensureLoaded();
            return super.getChapter(number);
        }

        private void ensureLoaded() {
            if (!(loaded || loading)) {
                loading = true;
                try {
                    String path = entry.substring(0, entry.lastIndexOf('/') + 1);
                    Document bookDoc = getDocument(file, entry);
                    Element bookElement = bookDoc.getDocumentElement();
                    BookIO.parseBook(this, bookElement, file, path, true);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                loaded = true;
            }
        }
    }

    /**
     * A proxy subclass of Chapter that loads the chapter's data on the fly when
     * needed.
     */
    private static class ChapterProxy extends MemoryChapter {
        private boolean loaded;

        private boolean loading;

        private File file;

        private String entry;

        public ChapterProxy(int number, File file, String entry) {
            super(number);
            this.file = file;
            this.entry = entry;
        }

        @Override
        public int getNumber() {
            return super.getNumber();
        }

        @Override
        public Collection<Verse> getVerses() {
            ensureLoaded();
            return super.getVerses();
        }

        @Override
        public void addVerse(Verse verse) {
            ensureLoaded();
            super.addVerse(verse);
        }

        @Override
        public Verse getVerse(int number) {
            ensureLoaded();
            return super.getVerse(number);
        }

        private void ensureLoaded() {
            if (!(loaded || loading)) {
                loading = true;
                try {
                    String path = entry.substring(0, entry.lastIndexOf('/') + 1);
                    Document chapterDoc = getDocument(file, entry);
                    Element chapterElement = chapterDoc.getDocumentElement();
                    BookIO.parseChapter(this, chapterElement, file, path, true);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                loaded = true;
            }
        }
    }
}
