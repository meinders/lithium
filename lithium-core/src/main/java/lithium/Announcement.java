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

package lithium;

import java.beans.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

/**
 * An announcement preset, which may contain parameters that are filled in
 * either by the user or automatically. Parameters are added to the text of the
 * preset as dollar-sign followed by the name of the parameter.
 *
 * @since 0.9
 * @version 0.9 (2006.08.25)
 * @author Gerrit Meinders
 */
public class Announcement implements Cloneable {
    public static final String TEXT_PROPERTY = "text";

    public static final String PARAMETERS_PROPERTY = "parameters";

    /** The name of the announcement preset. */
    private String name;

    /**
     * The text of the announcement preset, which may contain parameter
     * placeholders.
     */
    private String text;

    /** The parameters of the announcement. */
    private Set<Parameter> parameters;

    /** Provides support for bounds properties. */
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Constructs a new announcement preset with the given name and text, and an
     * empty set of parameters.
     *
     * @param name the name
     * @param text the text
     */
    public Announcement(String name, String text) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (text == null) {
            throw new NullPointerException("text");
        }
        this.name = name;
        this.text = text;
        parameters = new LinkedHashSet<Parameter>();
    }

    /**
     * Returns the name of the announcement preset.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the text of the announcement preset.
     *
     * @see #generateText(JFrame)
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text of the announcement preset.
     *
     * @see #generateText(JFrame)
     * @param text the text
     */
    public void setText(String text) {
        String oldValue = this.text;
        this.text = text;
        pcs.firePropertyChange(TEXT_PROPERTY, oldValue, text);
    }

    /**
     * Adds the given parameter to the preset.
     *
     * @param parameter the parameter to be added
     */
    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
        pcs.firePropertyChange(PARAMETERS_PROPERTY, null, null);
    }

    /**
     * Removes the given parameter from the preset.
     *
     * @param parameter the parameter to be removed
     */
    public void removeParameter(Parameter parameter) {
        parameters.remove(parameter);
        pcs.firePropertyChange(PARAMETERS_PROPERTY, null, null);
    }

    /**
     * Returns the preset's parameters.
     *
     * @return an unmodifiable set of parameters
     */
    public Set<Parameter> getParameters() {
        return Collections.unmodifiableSet(parameters);
    }

    /**
     * Builds the text value of the announcement. Since this may require user
     * input, a parent window is provided.
     *
     * @param parent the parent window
     * @return the generated text
     */
    public String generateText(JFrame parent) {
        String text = getText();
        for (Parameter parameter : parameters) {
            String tag = Pattern.quote(parameter.getTag());
            String replacement = parameter.generateText(parent);
            replacement = replacement.replaceAll("\\$", "\\\\\\$");
            text = text.replaceAll(tag, replacement);
        }
        return text;
    }

    @Override
    public Announcement clone() {
        try {
            Announcement clone = (Announcement) super.clone();
            clone.parameters = new LinkedHashSet<Parameter>();
            for (Parameter parameter : parameters) {
                clone.parameters.add(parameter.clone());
            }
            return clone;

        } catch (CloneNotSupportedException e) {
            throw new AssertionError("clone must be supported");
        }
    }

    /**
     * Adds the given property change listener to the listener list.
     *
     * @param listener the property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes the given property change listener from the listener list.
     *
     * @param listener the property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * The base class for parameters of an announcement preset. Subclasses
     * should either be immutable or override {@link #clone()} to return a
     * deep clone of any added (mutable) members.
     */
    public abstract static class Parameter implements Cloneable {
        private String tag;

        public Parameter(String tag) {
            if (tag == null) {
                throw new NullPointerException("tag");
            }
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }

        /**
         * Builds the text value of the property. Since this may require user
         * input, a parent window is provided.
         *
         * @param parent the parent window
         * @return the generated text
         */
        public abstract String generateText(JFrame parent);

        @Override
        public Parameter clone() {
            try {
                return (Parameter) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError("must be cloneable");
            }
        }
    }

    public static class TextParameter extends Parameter {
        private String label;

        public TextParameter(String tag, String label) {
            super(tag);
            setLabel(label);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public String generateText(JFrame parent) {
            String text = JOptionPane.showInputDialog(parent, label);
            if (text == null) {
                text = ""; // TODO: declare/throw exception to abort
            }
            return text;
        }
    }

    public static class DateParameter extends Parameter {
        private SimpleDateFormat format;
        private Date value;

        public DateParameter(String tag, SimpleDateFormat format, Date value) {
            super(tag);
            this.format = format;
            this.value = value;
        }

        public SimpleDateFormat getFormat() {
            return (SimpleDateFormat) format.clone();
        }

        public Date getValue() {
            if (value == null) {
                return null;
            } else {
                return new Date(value.getTime());
            }
        }

        /**
         * Returns the date value, formatted using the specified format. If the
         * value is {@code null}, the current date and time are used.
         */
        @Override
        public String generateText(JFrame parent) {
            Date value = this.value;
            if (value == null) {
                value = new Date();
            }
            return format.format(value);
        }
    }
}
