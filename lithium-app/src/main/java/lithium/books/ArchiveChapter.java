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

public class ArchiveChapter implements Chapter {

    private final File archive;

    private final URI source;

    private final int number;

    public ArchiveChapter(File archive, URI source, int number) {
        this.archive = archive;
        this.source = source;
        this.number = number;
    }

    public String getTitle() {
        return String.valueOf(number);
    }

    public int getNumber() {
        return number;
    }

    public SortedSet<Verse> getVerses() {
        try {
            return readVerses();
        } catch (IOException e) {
            return null;
        }
    }

    public void addVerse(Verse verse) {
        throw new UnsupportedOperationException();
    }

    public Verse getVerse(int number) {
        SortedSet<Verse> verses = getVerses();
        SortedSet<Verse> tailSet = verses.tailSet(Verse.getStub(number));
        Iterator<Verse> iterator = tailSet.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    private SortedSet<Verse> readVerses() throws IOException {
        SortedSet<Verse> result = null;
        ZipFile zip = new ZipFile(archive);
        try {
            ZipEntry entry = zip.getEntry(source.getPath());
            if (entry == null) {
                throw new RuntimeException("Failed to read book: " + source.getPath());
            }
            result = parseVerses(zip.getInputStream(entry));
        } finally {
            zip.close();
        }
        return result;
    }

    private SortedSet<Verse> parseVerses(InputStream in) throws IOException {
        Document document;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.parse(in);

        } catch (Exception e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }

        Element chapter = document.getDocumentElement();

        SortedSet<Verse> verses = new TreeSet<Verse>();

        NodeList verseElements = chapter.getElementsByTagName("verse");
        for (int i = 0; i < verseElements.getLength(); i++) {
            Element verseElement = (Element) verseElements.item(i);
            Verse verse = BookIO.parseVerse(verseElement);
            if (verse != null) {
                verses.add(verse);
            }
        }

        return verses;
    }
}
