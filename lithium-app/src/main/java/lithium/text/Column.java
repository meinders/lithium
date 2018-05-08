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

package lithium.text;

public class Column {
	private float weight;

	private float x;

	private float width;

	public Column(float weight) {
		this.weight = weight;
	}

	public float getWeight() {
	    return weight;
    }

	public void setWeight(float weight) {
	    this.weight = weight;
    }

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}
}
