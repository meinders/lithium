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
* FIXME Need comment
*
* @author G. Meinders
*/
public class FilterImpl
{
	private final FileFilter[] filters;

	private final ImageIcon icon;

	public FilterImpl( final FileFilter[] filters, final ImageIcon icon )
	{
		this.filters = filters;
		this.icon = icon;
	}

	public FileFilter getCombinedFilter()
	{
		FileFilter[] filters = getFilters();
		if (filters.length == 1)
		{
			return filters[0];
		}
		else
		{
			return new CombinedFileFilter(filters[0].getDescription(),
			        CombinedFileFilter.Method.UNION, Arrays.asList( filters ));
		}
	}

	public final FileFilter[] getFilters()
	{
		return filters;
	}

	public final ImageIcon getIcon()
	{
		return icon;
	}
}
