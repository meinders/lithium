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
 * This class provides utility methods for creating tasks to run a builder on a
 * given output target.
 *
 * @version 0.9 (2006.03.10)
 * @author Gerrit Meinders
 */
public abstract class BuilderUtilities {
    /**
     * Creates a task that runs the given builder on the given target. The file
     * will be written as UTF-8.
     *
     * @param builder the builder to use
     * @param target the output target to be used by the builder
     * @return the task
     */
    public static <T> Task<T> createBuilderTask(Builder<T> builder, File target)
            throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(target), "UTF-8"));
        return createBuilderTask(builder, writer);
    }

    /**
     * Creates a task that runs the given builder on the given writer.
     *
     * @param builder the builder to use
     * @param writer the writer to be used by the builder
     * @return the task
     */
    public static <T> Task<T> createBuilderTask(Builder<T> builder,
            Writer writer) throws IOException {
        builder.setOutput(writer);
        Task<T> task = new MonitoredTask<T>(builder);
        return task;
    }
}

