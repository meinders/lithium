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

package lithium.search;

import java.util.*;

import com.github.meinders.common.*;
import lithium.*;
import lithium.catalog.*;

/**
 * A worker that search through a catalog to find lyrics matching a given search
 * query.
 *
 * @since 0.8
 * @author Gerrit Meinders
 */
public abstract class LyricFinder extends
        GenericWorker<Collection<SearchResult>>
{
	private static final double DEFAULT_CUTOFF = 0.1;// 0.3;

	private SearchQuery query;

	private double cutoff = DEFAULT_CUTOFF;

	/**
	 * Constructs a new lyric finder for the given search query.
	 *
	 * @param query the search query
	 */
	public LyricFinder(SearchQuery query)
	{
		this.query = query;
	}

	@Override
	public Collection<SearchResult> construct()
	{
		try
		{
			return doConstruct();
		}
		catch (InterruptedException e)
		{
			return null;
		}
		catch (RuntimeException e)
		{
			fireWorkerStarted();
			fireWorkerError(e);
			fireWorkerInterrupted();
			return null;
		}
	}

	private Collection<SearchResult> doConstruct() throws InterruptedException
	{
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

		fireWorkerStarted();
		query.compile();
		Catalog catalog = CatalogManager.getCatalog();
		Set<Group> bundles = catalog.getBundles();

		// count number of lyrics
		int lyricCount = 0;
		for (Group bundle : bundles)
		{
			lyricCount += bundle.getLyrics().size();
		}

		if (Thread.interrupted())
		{
			throw new InterruptedException();
		}

		fireWorkerProgress(0, lyricCount);

		final Set<SearchResult> results = new LinkedHashSet<SearchResult>();

		// find matching lyrics
		{
			int i = 0;
			for (Group bundle : bundles)
			{
				for (Lyric lyric : bundle.getLyrics())
				{
					if (Thread.interrupted())
					{
						throw new InterruptedException();
					}
					double relevance = query.match(lyric);
					if (relevance >= cutoff)
					{
						LyricRef lyricRef = new LyricRef(bundle.getName(),
						        lyric.getNumber());
						results.add(new SearchResult(relevance, lyricRef));
					}
					fireWorkerProgress(++i, lyricCount);
				}
			}
		}

		fireWorkerFinished();

		return results;
	}
}
