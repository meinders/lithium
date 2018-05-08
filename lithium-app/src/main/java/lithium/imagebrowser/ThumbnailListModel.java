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

package lithium.imagebrowser;

import java.awt.image.*;
import java.net.*;
import javax.swing.*;


/**
 * A list model that automatically creates thumbnails for the URLs in the list.
 *
 * @version 0.9 (2006.02.25)
 * @author Gerrit Meinders
 */
public class ThumbnailListModel extends IconListModel<URL> {
    private ThumbnailLoader loader;

    public ThumbnailListModel(ThumbnailLoader loader) {
        super();
        this.loader = loader;
    }

    public void add(final URL source, final String description) {
        ImageIcon placeholder;

        // add placeholder
        placeholder = new ImageIcon(loader.getPlaceholder(), description);
        super.add(source, placeholder);

        if (source != null) {
            // asynchronously load image
            loader.load(source, new Runnable() {
                public void run() {
                    // replace placeholder
                    BufferedImage thumbnail = loader.getThumbnail(source);
                    set(source, new ImageIcon(thumbnail, description));
                }});
        }
    }
}

