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

public class ArchiveBook implements Book {

    private File archive;

    private URI source;

    private String name;

    private SortedMap<Integer, Chapter> chapters = null;

    public ArchiveBook(File archive, URI source, String name)
            throws IOException {
        this.archive = archive;
        this.source = source;
        this.name = name;
    }

    public String getTitle() {
        return name;
    }

    public Set<Chapter> getChapters() {
        ensureBookIsRead();
        return new LinkedHashSet<Chapter>(chapters.values());
    }

    public void addChapter(Chapter chapter) {
        ensureBookIsRead();
        chapters.put(chapter.getNumber(), chapter);
    }

    public void removeChapter(int number) {
        ensureBookIsRead();
        chapters.remove(number);
    }

    public Chapter getChapter(int number) {
        ensureBookIsRead();
        return chapters.get(number);
    }

    private void ensureBookIsRead() {
        if (chapters == null) {
            chapters = new TreeMap<Integer, Chapter>();
            try {
                readBook();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readBook() throws IOException {
        ZipFile zip = new ZipFile(archive);
        try {
            ZipEntry entry = zip.getEntry(source.getPath());
            if (entry == null) {
                throw new RuntimeException("Failed to read book: " + source.getPath());
            }
            parseBook(zip.getInputStream(entry));
        } finally {
            zip.close();
        }
    }

    private void parseBook(InputStream in) {
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

        Element book = document.getDocumentElement();
        name = book.getAttribute("name");

        NodeList chapterElements = book.getElementsByTagName("chapter");
        for (int i = 0; i < chapterElements.getLength(); i++) {
            Element chapterElement = (Element) chapterElements.item(i);
            Chapter chapter = parseChapter(chapterElement);
            if (chapter != null) {
                chapters.put(chapter.getNumber(), chapter);
            }
        }
    }

    private Chapter parseChapter(Element element) {
        try {
            URI chapterSource = source.resolve(element.getAttribute("src"));
            int number = Integer.parseInt(element.getAttribute("number"));
            return new ArchiveChapter(archive, chapterSource, number);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
