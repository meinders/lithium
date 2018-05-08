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

package lithium;

import java.util.*;
import javax.swing.*;

import com.github.meinders.common.*;

/**
 * This class manages file filters and icons for the supported file types.
 *
 * @version 0.9 (2007.01.02)
 * @author Gerrit Meinders
 */
public class FilterManager
{
	private static FilterManager _instance = new FilterManager();

	public static FilterManager getInstance()
	{
		return _instance;
	}

	private final Map<FilterType, FilterImpl> _filters;

	private FilterManager()
	{
		_filters = new HashMap<FilterType, FilterImpl>();
	}

	public void addFilter( FilterType type, FilterImpl filter )
	{
		_filters.put( type, filter );
	}

	public FilterImpl getFilter( FilterType type )
	{
		return _filters.get( type );
	}

	public static FileFilter getCombinedFilter( FilterType type )
	{
		FilterManager instance = getInstance();
		FilterImpl filter = instance.getFilter( type );
		return filter.getCombinedFilter();
	}

	public static FileFilter[] getFilters( FilterType type )
	{
		FilterManager instance = getInstance();
		FilterImpl filter = instance.getFilter( type );
		return filter.getFilters();
	}

	public static ImageIcon getFilterIcon( FilterType type )
	{
		FilterManager instance = getInstance();
		FilterImpl filter = instance.getFilter( type );
		return filter.getIcon();
	}
}
