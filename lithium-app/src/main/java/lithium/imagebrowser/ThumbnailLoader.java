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

import java.awt.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.*;
import javax.swing.*;

/**
 * This class performs asynchronous sequential loading of thumbnails.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class ThumbnailLoader {
    public static final String PROGRESS_PROPERTY = "progress";

    public static final String TOTAL_PROPERTY = "total";

    /** Queue used to organize threads sequentially. */
    private ConcurrentLinkedQueue<ThumbnailLoaderThread> loaderThreadQueue;

    /** Set of all files currently being loaded. */
    private Set<URL> loading;

    /** Map with thumbnails that are loaded but not yet returned. */
    private Map<URL, BufferedImage> loaded;

    /** Total number of files queued since the last time the queue was empty. */
    private int total = 0;

    /** Total number of files loaded since the last time the queue was empty. */
    private int progress = 0;

    /** Provides support for bounds properties. */
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** Thumbnail width. */
    private int width;

    /** Thumbnail height. */
    private int height;

    /** Thumbnail placeholder. */
    private BufferedImage placeholder;

    public ThumbnailLoader(int width, int height) {
        setWidth(width);
        setHeight(height);
        setPlaceholder(createPlaceholder(width, height));

        loaderThreadQueue = new ConcurrentLinkedQueue<ThumbnailLoaderThread>();
        loading = new HashSet<URL>();
        loaded = new HashMap<URL, BufferedImage>();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    private void setPlaceholder(BufferedImage placeholder) {
        this.placeholder = placeholder;
    }

    public BufferedImage getPlaceholder() {
        return placeholder;
    }

    private void setTotal(int total) {
        int oldValue = this.total;
        this.total = total;
        pcs.firePropertyChange(TOTAL_PROPERTY, oldValue, total);
    }

    public int getTotal() {
        return total;
    }

    private void setProgress(int progress) {
        int oldValue = this.progress;
        this.progress = progress;
        pcs.firePropertyChange(PROGRESS_PROPERTY, oldValue, progress);
    }

    public int getProgress() {
        return progress;
    }

    /**
     * Loads the images from the given source and executes the specified
     * runnable afterwards. The runnable is executed from the event dispatcher
     * thread.
     *
     * @return <code>true</code> if the image is not already being loaded;
     *         <code>false</code> otherwise
     */
    public synchronized boolean load(URL source, Runnable completedAction) {
        boolean newLoader = !isLoading(source);
        if (newLoader) {
            ThumbnailLoaderThread loaderThread = new ThumbnailLoaderThread(
                    source, completedAction);
            loaderThread.start();
            loaderStarted(source);
        }
        return newLoader;
    }

    public synchronized BufferedImage getThumbnail(URL source) {
        return loaded.get(source);
    }

    private synchronized boolean isLoading(URL source) {
        return loading.contains(source);
    }

    private synchronized void loaderStarted(URL source) {
        // update status
        setTotal(getTotal() + 1);

        // add to the set of images being loaded
        loading.add(source);
    }

    private synchronized void loaderCompleted(URL source) {
        // update progress
        setProgress(getProgress() + 1);

        // loader thread queue
        loaderThreadQueue.poll();
        if (loaderThreadQueue.size() == 0) {
            // reset total when queue is empty
            setProgress(0);
            setTotal(0);
        }

        // remove additional data used by loader
        loading.remove(source);
        loaded.remove(source);
    }

    private BufferedImage createPlaceholder(int width, int height) {
        if (placeholder == null) {
            placeholder = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = placeholder.createGraphics();
            JPanel panel = new JPanel(); // used to get L&F colours

            // fill background
            g2.setColor(panel.getBackground());
            g2.fillRect(0, 0, width - 1, height - 1);

            // paint flat border
            g2.setColor(panel.getBackground().darker());
            g2.drawRect(0, 0, width - 1, height - 1);

            /*
             * // paint raised border Border border =
             * BorderFactory.createBevelBorder(BevelBorder.RAISED);
             * border.paintBorder(panel, g2, 0, 0, width, height);
             */

            g2.dispose();
        }
        return placeholder;
    }

    public synchronized void dispose() {
        loaderThreadQueue.clear();
        loading.clear();
        loaded.clear();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(
            String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    public boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    private class ThumbnailLoaderThread extends Thread {
        private URL source;

        private Runnable completedAction;

        public ThumbnailLoaderThread(URL source, Runnable completedAction) {
            super("ThumbnailLoaderThread");
            this.source = source;
            this.completedAction = completedAction;
            setPriority(MIN_PRIORITY);
            setDaemon(true);

            // enter queue
            loaderThreadQueue.add(this);
        }

        public void run() {
            // wait in queue
            while (loaderThreadQueue.peek() != this) {
                if (loaderThreadQueue.isEmpty())
                    return;
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                }
            }

            try {
                // load the image
                BufferedImage image = ImageIO.read(source);

                // create a thumbnail
                loaded.put(source, createThumbnail(image));

            } catch (IOException e) {
                // image can't be loaded; getThumbnail will return null
                e.printStackTrace();
            }

            // perform custom action
            try {
                SwingUtilities.invokeAndWait(completedAction);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // notify ThumbnailLoader class
            loaderCompleted(source);
        }

        /**
         * Creates a thumbnail of the given image. The width and height of the
         * thumbnail are taken from the enclosing ThumbnailLoader.
         *
         * @param image the source image
         * @return the thumbnail
         */
        private BufferedImage createThumbnail(BufferedImage image)
                throws IOException {
            BufferedImage thumbnail = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = thumbnail.createGraphics();
            double thumbAspect = (double) width / (double) height;
            double imageAspect = (double) image.getWidth()
                    / (double) image.getHeight();
            double scale;
            if (imageAspect >= thumbAspect) {
                scale = (double) width / (double) image.getWidth();
            } else {
                scale = (double) height / (double) image.getHeight();
            }

            if (imageAspect != thumbAspect) {
                // draw black background
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, width, height);
            }

            int sWidth = (int) (image.getWidth() * scale);
            int sHeight = (int) (image.getHeight() * scale);
            int left = (width - sWidth) / 2;
            int top = (height - sHeight) / 2;
            g2.drawImage(image, left, top, sWidth, sHeight, null);
            g2.dispose();

            return thumbnail;
        }
    }
}
