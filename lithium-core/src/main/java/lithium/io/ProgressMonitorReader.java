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
 * A reader that monitors the progress of an underlying reader and signals any
 * progress changes to a {@link Task} instance.
 *
 * @version 0.9 (2006.02.21)
 * @author Gerrit Meinders
 */
public class ProgressMonitorReader extends FilterReader {
    private Task task;
    private long bytesRead = 0;
    private long size;

    public ProgressMonitorReader(Reader in) {
        this(in, 0);
    }

    public ProgressMonitorReader(Reader in, long size) {
        super(in);
        this.size = size;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int read() throws IOException {
        int read = super.read();
        if (read != -1 && size != 0) {
            bytesRead++;
            task.setProgress((int) (bytesRead * 100L / size));
        }
        return read;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int read = super.read(cbuf, off, len);
        if (read != -1 && size != 0) {
            bytesRead += read;
            task.setProgress((int) (bytesRead * 100L / size));
        }
        return read;
    }
}

