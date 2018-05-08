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
import java.util.*;
import java.util.Map.*;
import java.util.zip.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import lithium.books.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class BookIO {
    public static void write(File output, Library library) throws IOException {
        // FIXME replace these hacked constants
        final String OPWVIEWER_BOOK_NS_URI = "urn:opwviewer:book";
        final String language = "nl";

        final String ns = OPWVIEWER_BOOK_NS_URI;

        Map<File, Document> outputFiles = new LinkedHashMap<File, Document>();

        Document libraryDocument;
        try {
            libraryDocument = createBookDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
        outputFiles.put(new File("content.xml"), libraryDocument);
        Element libraryNode = libraryDocument.createElementNS(ns, "library");
        libraryDocument.appendChild(libraryNode);
        libraryNode.setAttribute("lang", language);
        libraryNode.setAttribute("name", library.getName());
        libraryNode.setAttribute("ref", "urn:opwviewer:book//bible");
        // ^--- FIXME hack

        int bookID = 1; // FIXME hack
        for (Book book : library.getBooks()) {
            String bookFileName = bookID + "/book.xml";

            Element bookEntryNode = libraryDocument.createElementNS(ns, "book");
            bookEntryNode.setAttribute("title", book.getTitle());
            bookEntryNode.setAttribute("ref", "urn:opwviewer:book//bible/" + bookID);
            // ^--- FIXME hack
            bookEntryNode.setAttribute("src", bookFileName);
            libraryNode.appendChild(bookEntryNode);

            Document bookDocument;
            try {
                bookDocument = createBookDocument();
            } catch (SAXException e) {
                throw new IOException(e);
            }
            outputFiles.put(new File(bookFileName), bookDocument);
            Element bookNode = bookDocument.createElementNS(ns, "book");
            bookDocument.appendChild(bookNode);
            bookNode.setAttribute("title", book.getTitle());

            int chapterIndex = 1;
            for (Chapter chapter : book.getChapters()) {
                String chapterFileName = bookID + "/" + chapterIndex + ".xml";

                Element chapterEntryNode = bookDocument.createElementNS(ns, "chapter");
                chapterEntryNode.setAttribute("title", chapter.getTitle());
                chapterEntryNode.setAttribute("src", chapterFileName);
                bookNode.appendChild(chapterEntryNode);

                Document chapterDocument;
                try {
                    chapterDocument = createBookDocument();
                } catch (SAXException e) {
                    throw new IOException(e);
                }
                outputFiles.put(new File(chapterFileName), chapterDocument);
                Element chapterNode = chapterDocument.createElementNS(ns, "chapter");
                chapterDocument.appendChild(chapterNode);
                chapterNode.setAttribute("title", chapter.getTitle());

                for (Verse verse : chapter.getVerses()) {
                    Element verseNode = chapterDocument.createElementNS(ns, "verse");
                    chapterNode.appendChild(verseNode);
                    verseNode.setAttribute("start", String.valueOf(verse.getRangeStart()));
                    if (verse.getRangeEnd() != verse.getRangeStart()) {
                        verseNode.setAttribute("end", String.valueOf(verse.getRangeEnd()));
                    }
                    for (Verse.Fragment fragment : verse.getFragments()) {
                        if (fragment instanceof Verse.Text) {
                            Verse.Text textFragment = (Verse.Text) fragment;
                            Node targetNode;
                            if (fragment.getClass() == Verse.Text.class) {
                                targetNode = verseNode;
                            } else if (fragment instanceof Verse.Note) {
                                targetNode = chapterDocument.createElementNS(ns, "note");
                            } else if (fragment instanceof Verse.SmallCaps) {
                                targetNode = chapterDocument.createElementNS(ns, "smallCaps");
                            } else if (fragment instanceof Verse.Implied) {
                                targetNode = chapterDocument.createElementNS(ns, "implied");
                            } else if (fragment instanceof Verse.Literal) {
                                targetNode = chapterDocument.createElementNS(ns, "literal");
                            } else if (fragment instanceof Verse.Role) {
                                targetNode = chapterDocument.createElementNS(ns, "role");
                            } else {
                                throw new AssertionError("Unsupported fragment: " + fragment);
                            }
                            if (targetNode != verseNode) {
                                verseNode.appendChild(targetNode);
                            }
                            targetNode.appendChild(chapterDocument.createTextNode(textFragment
                                    .getText()));
                        } else if (fragment instanceof Verse.PericopeHeader) {
                            Element pericopeElement = chapterDocument.createElementNS(ns,
                                    "pericope");
                            Verse.PericopeHeader pericopeHeader = (Verse.PericopeHeader) fragment;
                            pericopeElement.setAttribute("title", pericopeHeader.getTitle());
                            verseNode.appendChild(pericopeElement);
                        }
                    }
                }
                chapterIndex++;
            }
            bookID++;
        }

        writeArchive(output, outputFiles);
    }

    private static Document createBookDocument() throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(BookIO.class.getResource("book-1.0.xsd"));

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setSchema(schema);
        documentBuilderFactory.setValidating(true);
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }

        return documentBuilder.newDocument();
    }

    private static void writeArchive(File file, Map<File, Document> outputFiles)
            throws IOException {
        System.out.println("Writing archive " + file);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new AssertionError(e);
        }

        FileOutputStream fileOut = new FileOutputStream(file);
        try {
            ZipOutputStream zipOut = new ZipOutputStream(fileOut);
            for (Entry<File, Document> entry : outputFiles.entrySet()) {
                File outputFile = entry.getKey();
                Document outputDocument = entry.getValue();
                zipOut.putNextEntry(new ZipEntry(outputFile.getPath()));
                try {
                    transformer.transform(new DOMSource(outputDocument), new StreamResult(
                            zipOut));
                } catch (TransformerException e) {
                    throw new IOException(e);
                }
                zipOut.closeEntry();
            }
            zipOut.close();
        } finally {
            fileOut.close();
        }
    }
}
