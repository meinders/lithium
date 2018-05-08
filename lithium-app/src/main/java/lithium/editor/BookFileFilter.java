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

package lithium.editor;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.stream.*;

import com.github.meinders.common.*;
import lithium.books.*;

/**
 * File filter that accepts only zip files that appear to be book collection
 * files, based on the existence of a <code>collection.xml</code> file.
 *
 * @author Gerrit Meinders
 */
public class BookFileFilter extends ExtensionFileFilter {
    public BookFileFilter(String description) {
        super(description, "zip");
    }

    @Override
    public boolean accept(File file) {
        boolean result = false;
        if (super.accept(file)) {
            if (file.isDirectory()) {
                result = true;
            } else {
                try {
                    ZipFile zip = new ZipFile(file);
                    final ZipEntry entry = zip.getEntry("contents.xml");
                    if (entry != null) {
                        String documentElementNS = getDocumentElementNS(zip
                                .getInputStream(entry));
                        result = BookConstants.BOOK_NS_URI.equals(documentElementNS);
                    } else {
                        result = false;
                    }
                    zip.close();
                } catch (IOException e) {
                    // ignore I/O errors
                }
            }
        }
        return result;
    }

    private String getDocumentElementNS(InputStream inputStream) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader;
        try {
            reader = inputFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return null;
        }
        try {
            try {
                reader.nextTag();
                System.out.println("getNamespaceURI()  = '" + reader.getNamespaceURI()  + "'");
                return reader.getNamespaceURI();
            } catch (NoSuchElementException e) {
                return null;
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }
}
