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

import lithium.search.*;

/**
 * Performs a search for the currently entered search query and displays the
 * result in the search results list.
 */
public class LyricSearchSelector implements Selector
{
	public Collection<?> select(String text)
	{
		SearchQuery query = new AdvancedContentSearchQuery(text,
		        ContentSearchQuery.Method.ALL_WORDS, true, true, true, true);

		// TODO: allow for progress monitoring!
		// ProgressDialog<Collection<SearchResult>> progressDialog;
		// if (getTopLevelAncestor() instanceof JFrame)
		// {
		// JFrame parent = (JFrame) getTopLevelAncestor();
		// String title = Resources.get().getString("ellipsis",
		// Resources.get().getString("FindLyricDialog.searching"));
		// progressDialog = new ProgressDialog<Collection<SearchResult>>(
		// parent, title);
		// }
		// else {
		// throw new AssertionError(getTopLevelAncestor()
		// + " not instanceof JFrame");
		// }

		LyricFinder finder = new LyricFinder(query)
		{
			@Override
			public void finished()
			{
				// setCursor(Cursor.getDefaultCursor());
				// showSearchResults(results);
			}
		};
		// progressDialog.setWorker(finder);
		finder.start();
		// setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// FIXME: Allow for asynchronous processing...

		ArrayList<SearchResult> result = new ArrayList<SearchResult>(
		        finder.get());
		Collections.sort(result, Collections.reverseOrder());

		for (int i = result.size() - 1; i >= 10; i--)
		{
			result.remove(i);
		}

		return result;
	}
}
