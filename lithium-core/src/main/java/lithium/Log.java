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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Logs information provided by the application to the standard output and to a
 * file, if specified using the {@code lithium.log.file} system property.
 *
 * @author Gerrit Meinders
 */
public class Log
{
	private static final String NEW_LINE = System.getProperty("line.separator");

	private static Log instance;

	private static Pipe out;

	static
	{
		out = new Pipe();

		out.valves.add(System.out);

		String logFile = System.getProperty("lithium.log.file");
		if (logFile != null)
		{
			try
			{
				out.valves.add(new FileWriter(logFile));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private Log()
	{
	}

	public static Log getLog()
	{
		if (instance == null)
		{
			instance = new Log();
		}
		return instance;
	}

	public void writeEntry(Object context, String message)
	{
		try
		{
			out.append("[");
			out.append(new Date().toString());
			out.append("] ");

			if (context != null)
			{
				out.append(context.getClass().getName());
				out.append(": ");
			}

			out.append(message);
			out.append(NEW_LINE);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void writeDetails(String key, Object value)
	{
		try
		{
			out.append("\t");
			out.append(key);
			out.append(": ");
			writeValue(value);
			out.append(NEW_LINE);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void writeValue(final Object value) throws IOException
	{
		if (value == null)
		{
			out.append(String.valueOf(value));
		}
		else if (value instanceof Iterable)
		{
			out.append("[");
			boolean first = true;
			for (Object element : (Iterable) value)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					out.append(",");
				}
				writeValue(element);
			}
			out.append("]");
		}
		else if (value.getClass().isArray())
		{
			writeValue(new Iterable<Object>()
			{
				public Iterator<Object> iterator()
				{
					return new Iterator<Object>()
					{
						private int i = 0;

						private int length = Array.getLength(value);

						@Override
						public Object next()
						{
							return Array.get(value, i++);
						}

						@Override
						public boolean hasNext()
						{
							return i < length;
						}

						@Override
						public void remove()
						{
							throw new UnsupportedOperationException();
						}
					};
				}
			});
		}
		else
		{
			out.append(String.valueOf(value));
		}
	}

	private static class Pipe implements Appendable
	{
		private List<Appendable> valves;

		public Pipe()
		{
			valves = new ArrayList<Appendable>();
		}

		@Override
		public Appendable append(char c) throws IOException
		{
			for (Appendable valve : valves)
			{
				valve.append(c);
			}
			return this;
		}

		@Override
		public Appendable append(CharSequence csq) throws IOException
		{
			for (Appendable valve : valves)
			{
				valve.append(csq);
			}
			return this;
		}

		@Override
		public Appendable append(CharSequence csq, int start, int end)
		        throws IOException
		{
			for (Appendable valve : valves)
			{
				valve.append(csq, start, end);
			}
			return this;
		}
	}
}
