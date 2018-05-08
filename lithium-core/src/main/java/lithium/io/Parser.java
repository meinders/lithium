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
import java.util.concurrent.*;

import lithium.config.*;

/**
 * An interface for classes that can be given an input source for processing,
 * resulting in a certain type of output.
 *
 * @param <T> the return type of the parser
 * @since 0.9
 * @version 0.9 (2006.05.13)
 * @author Gerrit Meinders
 */
public interface Parser<T> extends Callable<T>, Configurable
{
    /**
     * Sets the input source to be processed by the parser.
     *
     * @param in the input source
     */
    public void setInput(Reader in);
}

