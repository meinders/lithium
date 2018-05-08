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

import java.net.*;

public class Reference {
    private String library;

    private String book;

    private int chapter;

    private int verse;

    /**
     * Constructs a reference from the given book reference URI. The format to
     * be used is as follows:
     *
     * <pre>
     *       urn:opwviewer:book[//LibraryID][/BookID[/ChapterID[/VerseID]]]
     * </pre>
     *
     * (Square brackets ('[' and ']') are used to denote optional parts.)
     *
     * @param uri The URI to construct a reference from.
     */
    public Reference(URI uri) {
        if (!"urn".equals(uri.getScheme())) {
            throw new IllegalArgumentException("uri: Must be a Uniform Resource Name.");
        }

        String ssp = uri.getSchemeSpecificPart();
        String[] ssnParts = ssp.split(":");
        if (ssnParts.length != 2) {
            throw new IllegalArgumentException(
                    "uri: Scheme-specific part must start with 'opwviewer:book'.");
        }
        if (!"opwviewer".equals(ssnParts[0]) && !"X-opwviewer".equals(ssnParts[0])) {
            throw new IllegalArgumentException(
                    "uri: Namespace Identifier must be 'opwviewer'.");
        }

        String[] nssParts = ssnParts[1].split("/");
        if (nssParts.length < 1 || !"book".equals(nssParts[0])) {
            throw new IllegalArgumentException(
                    "uri: Namespace Specific String must start with 'book'.");
        }

        if (nssParts.length < 2) {
            throw new IllegalArgumentException("uri: Expected library or book identifier.");
        }

        int offset;
        if (nssParts[1].isEmpty()) {
            /* LibraryID is specified. */
            offset = 2;
            if (nssParts.length > 2 && !nssParts[2].isEmpty()) {
                library = nssParts[2];
            } else {
                throw new IllegalArgumentException("uri: Expected library identifier.");
            }
        } else {
            offset = 0;
        }

        if (nssParts.length > offset + 1) {
            if (nssParts[offset + 1].isEmpty()) {
                throw new IllegalArgumentException("uri: Expected book identifier.");
            } else {
                book = nssParts[offset + 1];
                if (nssParts.length > offset + 2) {
                    if (nssParts[offset + 2].isEmpty()) {
                        throw new IllegalArgumentException("uri: Expected chapter identifier.");
                    } else {
                        try {
                            chapter = Integer.parseInt(nssParts[offset + 2]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "uri: Not a valid chapter identifier.", e);
                        }
                        if (chapter < 1) {
                            throw new IllegalArgumentException(
                                    "uri: Not a valid chapter identifier.");
                        } else if (nssParts.length > offset + 3) {
                            if (nssParts[offset + 3].isEmpty()) {
                                throw new IllegalArgumentException(
                                        "uri: Expected verse identifier.");
                            } else {
                                try {
                                    verse = Integer.parseInt(nssParts[offset + 3]);
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException(
                                            "uri: Not a valid verse identifier.", e);
                                }
                                if (verse < 1) {
                                    throw new IllegalArgumentException(
                                            "uri: Not a valid verse identifier.");
                                } else if (nssParts.length > 6) {
                                    throw new IllegalArgumentException(
                                            "uri: Unexpected token: '" + nssParts[6] + "'.");
                                }
                            }
                        }
                    }
                }
            }
        } else if (nssParts.length < 2) {
            throw new IllegalArgumentException("uri: Expected book identifier.");
        }
    }

    public Reference(String book) {
        super();
        this.library = null;
        this.book = book;
        this.chapter = 0;
        this.verse = 0;
    }

    public Reference(String library, String book) {
        super();
        this.library = library;
        this.book = book;
        this.chapter = 0;
        this.verse = 0;
    }

    public Reference(String library, String book, int chapter) {
        super();
        this.library = library;
        this.book = book;
        this.chapter = chapter;
        this.verse = 0;
    }

    public Reference(String library, String book, int chapter, int verse) {
        super();
        this.library = library;
        this.book = book;
        this.chapter = chapter;
        this.verse = verse;
    }

    public String getLibrary() {
        return library;
    }

    public String getBook() {
        return book;
    }

    public int getChapter() {
        return chapter;
    }

    public int getVerse() {
        return verse;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Reference) {
            Reference other = (Reference) obj;
            return ((library == null) ? other.library == null : library.equals(other.library))
                    && ((book == null) ? other.book == null : book.equals(other.book))
                    && (chapter == other.chapter) && (verse == other.verse);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return library.hashCode() ^ book.hashCode() ^ chapter ^ (verse << 16);
    }

    /**
     * Returns a URI representing the reference. The format is as follows:
     *
     * <pre>
     *       urn:opwviewer:book[//LibraryID][/BookID[/ChapterID[/VerseID]]]
     * </pre>
     *
     * (Square brackets ('[' and ']') are used to denote optional parts.)
     *
     * @return A URI representing the reference.
     */
    public URI toURI() {
        StringBuilder builder = new StringBuilder();
        builder.append("X-opwviewer:book");

        if (library != null) {
            builder.append('/');
            builder.append('/');
            builder.append(library);
        }

        if (book != null) {
            builder.append('/');
            builder.append(book);
        }

        if (chapter != 0) {
            builder.append('/');
            builder.append(chapter);
        }

        if (verse != 0) {
            builder.append('/');
            builder.append(verse);
        }

        try {
            return new URI("urn", builder.toString(), null);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
