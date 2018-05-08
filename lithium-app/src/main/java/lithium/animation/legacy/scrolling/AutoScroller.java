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

package lithium.animation.legacy.scrolling;

import javax.swing.*;

/**
 * <p>
 * This class adds automatic scrolling to a Scroller object. The scrolling can
 * be paused and resumed at any time. All updates are performed from the event
 * dispatcher thread for thread safety.
 *
 * @version 0.9x (2005.08.04)
 * @author Gerrit Meinders
 */
public class AutoScroller extends Thread {
    private static final float DEFAULT_SCROLL_RATE = 0.25f;

    private Scroller scroller;

    private volatile float scrollRate;

    private boolean enabled = false;

    private boolean stop = false;

    /**
     * Constructs a new auto-scroller without an associated scroller and set to
     * the default scroll rate. Until a scroller is set the auto-scroller can't
     * be enabled.
     */
    public AutoScroller() {
        this(null);
    }

    /**
     * Constructs a new auto-scroller without an associated scroller and set to
     * the given scroll rate. Until a scroller is set the auto-scroller can't be
     * enabled.
     *
     * @param scrollRate the scroll rate in logical units per second
     */
    public AutoScroller(float scrollRate) {
        this(null, scrollRate);
    }

    /**
     * Constructs a new auto-scroller that operates on the given scroller and
     * uses the default scroll rate.
     *
     * @param scroller the scroller
     */
    public AutoScroller(Scroller scroller) {
        this(scroller, DEFAULT_SCROLL_RATE);
    }

    /**
     * Constructs a new auto-scroller that operates on the given scroller and
     * uses the given scroll rate.
     *
     * @param scroller the scroller
     * @param scrollRate the scroll rate in logical units per second
     */
    public AutoScroller(Scroller scroller, float scrollRate) {
        super("AutoScroller");
        this.scroller = scroller;
        this.scrollRate = scrollRate;
        start();
    }

    /**
     * Disposes the scroller if it's thread is still alive.
     */
    public void finalize() {
        dispose();
    }

    /**
     * Sets the scroll rate.
     *
     * @param scrollRate the scroll rate in units per second
     */
    public void setScrollRate(float scrollRate) {
        this.scrollRate = scrollRate;
    }

    /**
     * Sets the scroller on which to perform automatic scrolling.
     *
     * @param scroller the scroller
     */
    public void setScroller(Scroller scroller) {
        if (scroller == null) {
            setEnabled(false);
        }
        this.scroller = scroller;
    }

    /**
     * Returns the scroller on which automatic scrolling is performed.
     *
     * @return the scroller
     */
    public Scroller getScroller() {
        return scroller;
    }

    /**
     * Sets whether automatic scrolling is enabled.
     *
     * @param enabled <code>true</code> to enable scrolling,
     *        <code>false</code> to disable scrolling
     */
    public synchronized void setEnabled(boolean enabled) {
        if (!enabled || getScroller() != null) {
            this.enabled = enabled;
            if (enabled) {
                interrupt();
            }
        }
    }

    /**
     * Returns whether automatic scrolling is enabled.
     *
     * @return <code>true</code> if scrolling is enabled; <code>false</code>
     *         otherwise
     */
    public synchronized boolean isEnabled() {
        return enabled;
    }

    /**
     * Shuts down the auto-scroller permanently.
     */
    public synchronized void dispose() {
        if (isAlive()) {
            stop = true;
            interrupt();
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Performs scrolling on the associated scroller when the auto-scroller is
     * neither disabled or paused.
     */
    public void run() {
        ScrollCommand scrollCommand = new ScrollCommand();
        long lastFrameTime = System.nanoTime();

        // as long as the thread isn't shut down
        while (!stop) {
            long currentTime = System.nanoTime();
            if (isEnabled()) {
                try {
                    // handle timing
                    long delta = currentTime - lastFrameTime;
                    scrollCommand.setScrollAmount(scrollRate * (float) delta
                            / 1000000000f);

                    // scroll
                    SwingUtilities.invokeAndWait(scrollCommand);

                    // allow other threads to run
                    yield();
                    sleep(1); // 1 ms extra seems to smoothen things a lot

                } catch (InterruptedException e) {
                    // ignore the exception
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                // wait until the scroller is enabled
                while (!isInterrupted()) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        // stop waiting
                        break;
                    }
                }
                currentTime = System.nanoTime();
            }

            lastFrameTime = currentTime;
        }
    }

    /**
     * Sets the target of the scroller associated with the enclosing class.
     */
    private class ScrollCommand implements Runnable {
        private float scrollAmount = 0.0f;

        public ScrollCommand() {
        }

        public void setScrollAmount(float scrollAmount) {
            this.scrollAmount = scrollAmount;
        }

        public void run() {
            Scroller scroller = getScroller();
            scroller.setTarget(scroller.getTarget() + scrollAmount);
        }
    }
}
