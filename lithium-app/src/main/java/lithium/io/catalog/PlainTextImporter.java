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

import com.github.meinders.common.*;
import lithium.catalog.*;

/**
 * A worker for importing plain text.
 *
 * @version 0.9 (2006.02.07)
 * @author Gerrit Meinders
 */
public class PlainTextImporter extends GenericWorker<MutableCatalog> {
    private File file;
    private PlainTextCatalogParser parser;

    public PlainTextImporter(File file, PlainTextCatalogParser parser) {
        super();
        this.file = file;
        this.parser = parser;
    }

    public MutableCatalog construct() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        fireWorkerStarted();
        try {
            MutableCatalog catalog = parser.parse(file);
            fireWorkerFinished();
            return catalog;
        } catch (Exception e) {
            e.printStackTrace();
            fireWorkerError(e);
            fireWorkerInterrupted();
            return null;
        }
    }
}

