/*
 * Copyright 2009 Gerrit Meinders
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

package lithium.animation.legacy;

/**
 * An interface for objects that need to be started before they can be used,
 * such as objects that (might) use threads.
 *
 * @version 0.9x (2005.08.03)
 * @author Gerrit Meinders
 */
public interface Startable {
    /**
     * Performs any necessary initialization and starts the startable object.
     */
    public void start();
}

