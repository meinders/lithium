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

package lithium.config;

import java.util.*;

import lithium.io.*;

/**
 * An interface for classes that can be configured by setting predefined
 * parameters.
 *
 * @author Gerrit Meinders
 */
public interface Configurable {
    /**
     * Returns the parameters that can be set on the builder.
     *
     * @return a set of parameters
     */
    Set<? extends Parameter> getParameters();

    /**
     * Sets the value of the given parameter.
     *
     * @param parameter the parameter
     * @param value the value to be set
     */
    void setParameterValue(Parameter parameter, Object value);

    /**
     * Returns the value of the given parameter.
     *
     * @param parameter the parameter
     * @return the value of the parameter
     */
    Object getParameterValue(Parameter parameter);
}
