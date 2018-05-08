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

/**
 * Converts all read '&' characters to '&amp;' entity references.
 *
 * (This implementation should be modified to convert only '&' characters
 * that aren't part of a valid entity reference.)
 *
 * @version 0.8 (2004.12.18)
 * @author Gerrit Meinders
 */
public class FixEntitiesInputStream extends FilterInputStream {
    private byte[] insert = null;
    private int insertIndex = 0;

    public FixEntitiesInputStream(InputStream in) {
        super(in);
    }

    public int read() throws IOException {
        int read;
        if (insert == null) {
            read = in.read();
            if (read == '&') {
                insert = new byte[] {
                        (byte) 'a', (byte) 'm',
                        (byte) 'p', (byte) ';'};
                insertIndex = 0;
            }
        } else {
            read = insert[insertIndex];
            insertIndex++;
            if (insertIndex == insert.length) {
                insert = null;
            }
        }
        return read;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) return 0;

        int read = read();
        if (read == -1) {
            return -1;
        } else {
            b[off] = (byte) read;
            return 1;
        }
    }
}
