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
 * Provides basic support of the {@code Configurable} interface for other
 * configurable objects to use. The configuration support also provides methods
 * to facilitate implementing apply/cancel behavior for any configurable object.
 *
 * @since 0.9
 * @version 0.9 (2006.05.13)
 * @author Gerrit Meinders
 */
public class ConfigurationSupport implements Configurable {
    /** The supported parameters. */
    private Set<Parameter> parameters;

    /** The values for each parameter. */
    private Map<Parameter, Object> values;

    /**
     * Constructs a new configuration support with no parameters or values set.
     */
    public ConfigurationSupport() {
        super();
        parameters = new LinkedHashSet<Parameter>();
        values = new LinkedHashMap<Parameter, Object>();
    }

    /**
     * Constructs a new configuration support with the same parameters and
     * values as the given configurable.
     *
     * <p>
     * This constructor is particularly useful in combination with the
     * {@link #applyTo(Configurable)} method, to realise apply/cancel behavior.
     *
     * @param source the configurable
     */
    public ConfigurationSupport(Configurable source) {
        this();
        for (Parameter parameter : source.getParameters()) {
            addParameter(parameter);
            Object value = source.getParameterValue(parameter);
            if (value != parameter.getDefaultValue()) {
                setParameterValue(parameter, value);
            }
        }
    }

    /**
     * Applies the parameter values of the configuration support to the given
     * target, setting its values appropriately.
     *
     * <p>
     * Any parameters of the target that are not associated with the
     * configuration support will be reset to their default values. Parameters
     * that are only associated with the configuration support (and not with the
     * target) are ignored.
     *
     * @param target the object to be configured
     */
    public void applyTo(Configurable target) {
        for (Parameter parameter : target.getParameters()) {
            target.setParameterValue(parameter, getParameterValue(parameter));
        }
    }

    /**
     * Adds the given parameter to the set of supported parameters.
     *
     * @param parameter the parameter to be added
     */
    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public Set<Parameter> getParameters() {
        return Collections.unmodifiableSet(parameters);
    }

    public void setParameterValue(Parameter parameter, Object value) {
        if (value == parameter.getDefaultValue()) {
            values.remove(parameter);
        } else {
            values.put(parameter, value);
        }
    }

    public Object getParameterValue(Parameter parameter) {
        if (values.containsKey(parameter)) {
            return values.get(parameter);
        } else {
            return parameter.getDefaultValue();
        }
    }

    /**
     * Returns the value of the parameter with the given name.
     *
     * @param name the name of the parameter
     * @return the value of the parameter, or {@code null} if there is no
     *         parameter with the given name
     */
    public Object getParameterValue(String name) {
        for (Parameter parameter : getParameters()) {
            if (parameter.getName().equals(name)) {
                return getParameterValue(parameter);
            }
        }
        return null;
    }
}
