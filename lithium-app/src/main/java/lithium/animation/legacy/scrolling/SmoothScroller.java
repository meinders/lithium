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

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import lithium.animation.legacy.*;


/**
 * This class implements the Scroller interface to provide smooth scrolling. An
 * algorithm using an exponential equation is used to approximate the target
 * value over time.
 *
 * @author Gerrit Meinders
 */
public class SmoothScroller implements Scroller {
    private float target = 0.0f;

    private float value = 0;

    private ScrollThread scrollThread;

    private Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();

    /**
     * Constructs a new smooth scroller and starts its thread.
     */
    public SmoothScroller() {
        this(true);
    }

    /**
     * Constructs a new smooth scroller and optionally starts its thread.
     *
     * @param autoStart if <code>true</code> the scroller's thread should be
     *        started
     */
    public SmoothScroller(boolean autoStart) {
        scrollThread = new ScrollThread();
        if (autoStart) {
            start();
        }
    }

    /** @see lithium.animation.legacy.Startable#start() */
    public void start() {
        scrollThread.start();
    }

    /** @see lithium.animation.legacy.Disposable#dispose() */
    public void dispose() {
        scrollThread.dispose();
    }

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        float oldValue = this.value;
        this.value = value;
        if (value != oldValue) {
            fireChangeEvent();
        }
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        setTarget(target, false);
    }

    public void setTarget(float target, boolean addTargetDistance) {
        if (addTargetDistance) {
            this.target += target - value;
        } else {
            this.target = target;
        }
        if (this.target < 0.0f) {
            this.target = 0.0f;
        }
    }

    private void fireChangeEvent() {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from event dispatch thread";
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    private class ScrollThread extends Thread implements Disposable {
        private boolean stop;

        /**
         * Creates a new scroll thread with less than normal priority (which
         * seems to make scrolling smoother).
         */
        public ScrollThread() {
            super(ScrollThread.class.getSimpleName());
            setPriority(NORM_PRIORITY - 1);
        }

        public void dispose() {
            stop = true;
            interrupt();
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            // long lastFrameTime = System.nanoTime();

            while (!stop) {
                // long currentTime = System.nanoTime();

                try {
                    // handle timing
                    // final long delta = currentTime - lastFrameTime;
                    final float weight = 0.1f;
                    // TODO: real-time visuals
                    /* Fix distance and speed changes per time... or not */
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            setValue(target * weight + value * (1 - weight));
                        }
                    });

                    // allow other threads to run
                    yield();

                    // sleep 1ms anyway, cuz it seems to fix problems
                    sleep(1);

                } catch (InterruptedException e) {
                    // ignore interruptions
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // lastFrameTime = currentTime;
            }
        }
    }
}
