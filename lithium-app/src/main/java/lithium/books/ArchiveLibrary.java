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
import java.net.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;

public class ArchiveLibrary implements Library {
    private final File archive;

    private String name;

    private Map<String, Book> books;

    public ArchiveLibrary(URI uri) throws IOException {
        this(getFile(uri));
    }

    public ArchiveLibrary(File source) throws IOException {
        this.archive = source;
        books = new LinkedHashMap<String, Book>();

        readCollection();
    }

    private static File getFile(URI uri) throws IOException {
        if ("file".equals(uri.getScheme())) {
            return new File(uri.getPath());
        } else {
            throw new IOException("uri: Expected a file, but was: " + uri);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Book> getBooks() {
        return new LinkedHashSet<Book>(books.values());
    }

    public void addBook(Book book) {
        books.put(book.getTitle(), book);
    }

    public void removeBook(String name) {
        books.remove(name);
    }

    public Book getBook(String name) {
        return books.get(name);
    }

    public Verse getVerse(String book, int chapter, int verse) {
        Book theBook = getBook(book);
        if (theBook == null) {
            return null;
        }

        Chapter theChapter = theBook.getChapter(chapter);
        if (theChapter == null) {
            return null;
        }

        return theChapter.getVerse(verse);
    }

    private void readCollection() throws IOException {
        ZipFile zip = new ZipFile(archive);
        try {
            ZipEntry entry = zip.getEntry("collection.xml");
            parseCollection(zip.getInputStream(entry));
        } finally {
            zip.close();
        }
    }

    private void parseCollection(InputStream in) {
        Document document;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                .newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.parse(in);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Element collection = document.getDocumentElement();
        name = collection.getAttribute("name");

        NodeList bookElements = collection.getElementsByTagName("book");
        for (int i = 0; i < bookElements.getLength(); i++) {
            Element bookElement = (Element) bookElements.item(i);
            Book book = parseBook(bookElement);
            if (book != null) {
                books.put(book.getTitle(), book);
            }
        }
    }

    private Book parseBook(Element element) {
        Book result = null;

        try {
            URI source = new URI(null, element.getAttribute("src"), null);
            String name = element.getAttribute("name");
            result = new ArchiveBook(archive, source, name);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
