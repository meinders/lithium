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
import java.util.concurrent.*;

import lithium.*;
import org.w3c.dom.*;

/**
 * This class provides the ability to read and write opwViewer configuration
 * files.
 *
 * @version 0.9 (2006.03.26)
 * @author Gerrit Meinders
 */
public class ConfigIO {
    /** The error message used if an unsupported DTD is encountered. */
    public static final String INVALID_DTD = "invalid DTD";

    /** The error message used if invalid content is encountered. */
    public static final String INVALID_CONTENT = "invalid content";

    /** The public ID of the old configuration format DTD. */
    public static final String PUBLIC_ID = "-//Frixus//DTD opwViewer Configuration 1.0//EN";

    /** The namespace of the new configuration format. */
    public static final String NAMESPACE = "urn:opwviewer:config";

    /**
     * Reads a configuration from the specified file.
     *
     * @param file the file to be read
     * @return the configuration
     * @throws IOException if an exception occurs while reading
     */
    public static Config read(File file) throws IOException {
        return read(file.toURI().toURL());
    }

    /**
     * Reads a configuration from the specified URL.
     *
     * @param source the URL
     * @return the configuration
     * @throws IOException if an exception occurs while reading
     */
    public static Config read(URL source) throws IOException {
        try {
            ConfigParser parser = new ConfigParser();
            parser.setContext(source);
            Task<Config> task;
            task = ParserUtilities.createParserTask(parser, source);
            task.run();
            return task.get();
        } catch (ExecutionException e) {
            throw (IOException) new IOException().initCause(e);
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Reads a configuration from the specified reader.
     *
     * @param source the reader
     * @return the configuration
     * @throws IOException if an exception occurs while reading
     */
    public static Config read(Reader source) throws IOException {
        try {
            Task<Config> task = ParserUtilities.createParserTask(
                    new ConfigParser(), source);
            task.run();
            return task.get();
        } catch (ExecutionException e) {
            throw (IOException) new IOException().initCause(e);
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Writes the given configuration settings to the given file.
     *
     * @param config the configuration settings to be written
     * @param file the name of the file to write the settings to
     * @throws IOException if an exception occurs while writing the file
     */
    public static void write(Config config, String file) throws IOException {
        write(config, new File(file));
    }

    /**
     * Writes the given configuration settings to the given file.
     *
     * @param config the configuration settings to be written
     * @param file the file to write the settings to
     * @throws IOException if an exception occurs while writing the file
     */
    public static void write(Config config, File file) throws IOException {
        URL contextURL = file.toURI().toURL();
        ConfigBuilder builder = new ConfigBuilder(config, contextURL);
        Task<Document> task = BuilderUtilities.createBuilderTask(builder, file);
        task.run();
    }

    /** Unused private constructor. */
    private ConfigIO() {
    }
}
