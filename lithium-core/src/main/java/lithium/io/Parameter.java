/*
 * Copyright 2008 Gerrit Meinders
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

/**
 * A parameter used to configure a builder or parser instance.
 *
 * @since 0.9
 * @version 0.9 (2006.05.13)
 * @author Gerrit Meinders
 */
public class Parameter {
    private String name;

    private String displayName;

    private Class<?> type;

    private Object defaultValue;

    private Object[] values;

    /**
     * Constructs a new parameter with the given name, display name, type,
     * default value and selectable values.
     *
     * @param <T> the value type
     * @param name the name
     * @param displayName the display name
     * @param type the class of the value type
     * @param defaultValue the default value
     * @param values the values that can be selected (optional)
     */
    public <T> Parameter(String name, String displayName, Class<T> type,
            T defaultValue, T... values) {
        super();
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.defaultValue = defaultValue;
        this.values = values.length == 0 ? null : values;
    }

    /**
     * Returns the name of the parameter.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the display name of the parameter.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the parameter's value type.
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the default value of the parameter.
     *
     * @return the default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the values that can be chosen for the parameters, or {@code null}
     * if there is no predefined set of values.
     *
     * @return the values, or {@code null}
     */
    public Object[] getValues() {
        return values;
    }
}
