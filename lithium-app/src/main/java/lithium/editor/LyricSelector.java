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
import java.util.regex.*;

import lithium.*;
import lithium.catalog.*;

public class LyricSelector implements Selector {
	public Collection<?> select(String text) {
		Pattern bundleNumberPattern = Pattern.compile(
		        "^\\s*([^0-9\\s]+)?\\s*([0-9]+)\\s*$", Pattern.CASE_INSENSITIVE);
		Matcher bundleNumberMatcher = bundleNumberPattern.matcher(text);

		if (bundleNumberMatcher.find()) {
			String bundleName;
			Catalog catalog = CatalogManager.getCatalog();
			if (bundleNumberMatcher.group(1) == null) {
				bundleName = getDefaultGroup();
			} else {
				String bundleIdentifier = bundleNumberMatcher.group(1);
				bundleName = bundleIdentifier;
				for (Group bundle : catalog.getBundles()) {
					String name = bundle.getName();
					if (name.toLowerCase().startsWith(
					        bundleIdentifier.toLowerCase())) {
						bundleName = name;
						break;
					}
				}
			}

			int number = Integer.parseInt(bundleNumberMatcher.group(2));

			try {
				LyricRef selection = new LyricRef(bundleName, number);
				Lyric lyric = catalog.getLyric(selection);

				List<Lyric> result = new ArrayList<Lyric>(1);

				if (lyric != null) {
					result.add(lyric);
				}

				for (Group bundle : catalog.getBundles()) {
					Lyric alternative = bundle.getLyric(number);

					// XXX: Special case to allow for our old numbering scheme.
					if ("spoorzicht".equalsIgnoreCase(bundle.getName()) && (number > 700)) {
						alternative = bundle.getLyric(number - 700);
					}

					if ((alternative != null)
					        && ((lyric == null) || !alternative.equals(lyric))) {
						result.add(alternative);
					}
				}

				return result;

			} catch (NumberFormatException ex) {
				// ex.printStackTrace();
			} catch (IllegalArgumentException ex) {
				// ex.printStackTrace();
			}
		}

		return Collections.emptySet();
	}

	protected String getDefaultGroup() {
		return ConfigManager.getConfig().getDefaultBundle();
		// TODO: override as:
		// return (String) bundleCombo.getSelectedItem();
	}
}
