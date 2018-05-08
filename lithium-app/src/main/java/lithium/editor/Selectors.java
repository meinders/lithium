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

package lithium.editor;

import java.util.*;

public class Selectors {
	public static Selector all(Selector... selectors) {
		return new SelectAll(selectors);
	}

	public static Selector all(Collection<Selector> selectors) {
		return new SelectAll(selectors);
	}

	public static Selector firstNonEmpty(Selector... selectors) {
		return new SelectFirst(selectors);
	}

	public static Selector firstNonEmpty(Collection<Selector> selectors) {
		return new SelectFirst(selectors);
	}

	private static class SelectAll implements Selector {
		private Collection<Selector> selectors;

		public SelectAll(Selector... selectors) {
			this.selectors = Arrays.asList(selectors);
		}

		public SelectAll(Collection<? extends Selector> selectors) {
			this.selectors = new ArrayList<Selector>(selectors);
		}

		@Override
		public Collection<?> select(String text) {
			Collection<Object> result = Collections.emptySet();

			for (Selector selector : selectors) {
				Collection<?> selectorResult = selector.select(text);
				if (!selectorResult.isEmpty()) {
					if (result.isEmpty()) {
						result = (Collection<Object>) selectorResult;
					} else {
						result = new ArrayList<Object>(result);
						result.addAll(selectorResult);
					}
				}
			}

			return result;
		}
	}

	private static class SelectFirst implements Selector {
		private Collection<Selector> selectors;

		public SelectFirst(Selector... selectors) {
			this.selectors = Arrays.asList(selectors);
		}

		public SelectFirst(Collection<? extends Selector> selectors) {
			this.selectors = new ArrayList<Selector>(selectors);
		}

		@Override
		public Collection<?> select(String text) {
			Collection<?> result = Collections.emptySet();

			for (Selector selector : selectors) {
				result = selector.select(text);
				if (!result.isEmpty()) {
					break;
				}
			}

			return result;
		}
	}

	private Selectors() {
	}
}