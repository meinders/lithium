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

package lithium.io.catalog;

import java.io.*;
import java.util.*;

import lithium.*;
import lithium.catalog.*;
import lithium.config.*;
import lithium.io.*;

/**
 * A builder for creating a plain text representation of a catalog.
 *
 * @since 0.9
 * @version 0.9 (2006.05.14)
 * @author Gerrit Meinders
 */
public class PlainTextCatalogBuilder extends ConfigurationSupport
implements
        Builder<Object> {
    private Catalog catalog;

    private Writer out;

    /**
     * Constructs a new plain text catalog builder.
     *
     * @param catalog the catalog to be exported
     */
    public PlainTextCatalogBuilder(Catalog catalog) {
        super();
        if (catalog == null) {
            throw new NullPointerException("catalog");
        }
        this.catalog = catalog;

        addParameter(createParameter("outputBundles", Boolean.class, true));
        addParameter(createParameter("outputNumbers", Boolean.class, true));
        addParameter(createParameter("outputTitles", Boolean.class, true));
        addParameter(createParameter("outputTexts", Boolean.class, true));
        addParameter(createParameter("outputOriginalTitles", Boolean.class,
                false));
        addParameter(createParameter("outputCopyrights", Boolean.class, true));
    }

    /**
     * Creates a parameter object.
     *
     * @param <T> the value type
     * @param name the name of the parameter
     * @param type the value type's class
     * @param defaultValue the default value of the parameter
     * @param values the selectable values, if any
     * @return the created parameter
     */
    private <T> Parameter createParameter(String name, Class<T> type,
            T defaultValue, T... values) {
        String key = "PlainTextCatalogBuilder." + name;
        String displayName = Resources.get().getString(key);
        return new Parameter(name, displayName, type, defaultValue, values);
    }

    /**
     * Sets the output target to which the builder should write its result.
     *
     * @param out the output target
     */
    public void setOutput(Writer out) {
        this.out = out;
    }

    public Object call() throws IOException {
        boolean outputBundles = (Boolean) getParameterValue("outputBundles");
        boolean outputNumbers = (Boolean) getParameterValue("outputNumbers");
        boolean outputTitles = (Boolean) getParameterValue("outputTitles");
        boolean outputTexts = (Boolean) getParameterValue("outputTexts");
        boolean outputOriginalTitles = (Boolean) getParameterValue("outputOriginalTitles");
        boolean outputCopyrights = (Boolean) getParameterValue("outputCopyrights");

        PrintWriter out = new PrintWriter(this.out);
        for (Group bundle : catalog.getBundles()) {
            if (outputBundles) {
                out.println(bundle.getName());
                out.println();
            }

            TreeSet<Lyric> lyrics = new TreeSet<Lyric>(bundle.getLyrics());
            for (Lyric lyric : lyrics) {
                // lyric number/title
                if (outputNumbers) {
                    out.print(lyric.getNumber());
                    if (outputTitles) {
                        out.print(". ");
                    }
                }
                if (outputTitles) {
                    out.print(lyric.getTitle());
                }
                if (outputNumbers || outputTitles) {
                    out.println();
                    out.println();
                }

                // lyric text
                if (outputTexts) {
                    String[] lines = lyric.getText().split("\n");
                    for (int k = 0; k < lines.length; k++) {
                        out.println(lines[k]);
                    }
                    out.println();
                }

                // original title
                if (outputOriginalTitles) {
                    System.out.println(lyric.getOriginalTitle());
                    System.out.println();
                }

                // copyrights
                if (outputCopyrights) {
                    String[] lines = lyric.getCopyrights().split("\n");
                    for (int k = 0; k < lines.length; k++) {
                        out.println(lines[k]);
                    }
                    out.println();
                }
            }
        }
        out.close();
        return null;
    }
}
