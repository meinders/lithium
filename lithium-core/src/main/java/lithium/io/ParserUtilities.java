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
import java.net.*;

/**
 * This class provides utility methods for creating tasks to run a parser on a
 * given input source.
 *
 * @version 0.9 (2006.03.10)
 * @author Gerrit Meinders
 */
public abstract class ParserUtilities {
    /**
     * Creates a task that runs the given parser on the given source. The
     * content-encoding will default to UTF-8 if it can't be determined.
     *
     * @param parser the parser to use
     * @param source the input source to be used by the parser
     * @return the task
     */
    public static <T> Task<T> createParserTask(Parser<T> parser, URL source)
            throws IOException {
        URLConnection connection = source.openConnection();

        String encoding = connection.getContentEncoding();
        if (encoding == null) {
            encoding = "UTF-8";
        }

        Reader urlReader = new InputStreamReader(
                connection.getInputStream(), encoding);
        int size = connection.getContentLength();

        return createParserTask(parser, urlReader, size);
    }

    /**
     * Creates a task that runs the given parser on the given reader.
     *
     * @param parser the parser to use
     * @param reader the reader to be used by the parser
     * @return the task
     */
    public static <T> Task<T> createParserTask(Parser<T> parser, Reader reader)
            throws IOException {
        return createParserTask(parser, reader, 0);
    }

    /**
     * Creates a task that runs the given parser on the given source, having
     * the given size.
     *
     * @param parser the parser to use
     * @param reader the reader to be used by the parser
     * @param size the length of the data that can be read from reader, in
     *             bytes, or {@code 0} if the length is unknown
     * @return the task
     */
    public static <T> Task<T> createParserTask(Parser<T> parser, Reader reader,
            long size) throws IOException {
        ProgressMonitorReader in = new ProgressMonitorReader(
                new BufferedReader(reader), size);
        parser.setInput(in);
        Task<T> task = new MonitoredTask<T>(parser);
        in.setTask(task);
        return task;
    }
}

