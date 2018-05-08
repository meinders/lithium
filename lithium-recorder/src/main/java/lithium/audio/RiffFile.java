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

package lithium.audio;

import java.io.*;
import java.util.*;

public class RiffFile {
    private byte[] type;

    private Collection<Chunk> chunks;

    public RiffFile(String type) {
        try {
            this.type = type.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("type", e);
        }

        if (this.type.length != 4) {
            throw new IllegalArgumentException("type");
        }
    }

    public final void write(OutputStream out) throws IOException {
        out.write(type);
        int size = 0;
        for (Chunk chunk : chunks) {

        }
    }

    protected static abstract class Chunk {
        private final byte[] type;

        private int size;

        protected Chunk(String type) {
            try {
                this.type = type.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }

            if (this.type.length != 4) {
                throw new IllegalArgumentException("Type must be 4 bytes.");
            }

            size = 0;
        }

        public final int getSize() {
            return size;
        }

        public final void setSize(int size) {
            this.size = size;
        }

        public final void write(OutputStream out) throws IOException {
            out.write(type);
            out.write(size);
            out.write(size >> 8);
            out.write(size >> 16);
            out.write(size >> 24);
            writeContents();
        }

        protected void writeContents() {
            /* to be overridden in subclasses */
        }
    }
}
