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

package lithium.io;

import java.io.*;

import junit.framework.*;
import lithium.*;

/**
 * Unit test of the configuration settings builder.
 *
 * @version 0.9 (2006.03.11)
 * @author Gerrit Meinders
 */
public class ConfigBuilderTest extends TestCase {
	public ConfigBuilderTest() {
	}

	public void testBuildConfig() throws IOException {
		InputStream referenceIn = getClass().getResourceAsStream( "newConfig.xml" );
		Config reference;
		try
		{
			// parse reference config
			ConfigParser parser = new ConfigParser();
			parser.setInput(new InputStreamReader( referenceIn ));
			reference = parser.call();
		}
		finally
		{
   			referenceIn.close();
		}

		// build document
		Writer buffer = new StringWriter();
		ConfigBuilder builder = new ConfigBuilder(reference);
		builder.setOutput(buffer);
		builder.call();
		String output = buffer.toString();

		// compare to reference
		referenceIn = getClass().getResourceAsStream( "newConfig.xml" );
		try
		{
			BufferedReader referenceReader = new BufferedReader( new InputStreamReader( referenceIn ) );
			BufferedReader reader = new BufferedReader(new StringReader(output));
			while (true) {
				String referenceLine = referenceReader.readLine();
				String actualLine = reader.readLine();
				if (referenceLine == null) {
					TestCase.assertNull( "unexpected end of file", actualLine );
					break;
				}
				if (!referenceLine.equals(actualLine)) {
					try {
						TestCase.assertEquals( "incorrect line", referenceLine, actualLine );
					} catch (Error e) {
						System.out.println(referenceLine);
						System.out.println(actualLine);
						throw e;
					}
				}
			}
		}
		finally
		{
   			referenceIn.close();
		}
	}
}
