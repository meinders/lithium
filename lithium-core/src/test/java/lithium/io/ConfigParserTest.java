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

import java.awt.*;
import java.io.*;
import java.util.*;

import junit.framework.*;
import lithium.*;
import lithium.Announcement.*;
import lithium.Announcement.Parameter;

/**
 * Unit test of the configuration settings parser.
 *
 * @version 0.9 (2006.03.11)
 * @author Gerrit Meinders
 */
public class ConfigParserTest extends TestCase {
	public ConfigParserTest() {
	}

	public void testParseConfig() throws IOException {
		InputStream in = getClass().getResourceAsStream( "newConfig.xml" );
		try
		{
			ConfigParser parser = new ConfigParser();
			parser.setInput(new InputStreamReader( in ));
			Config config = parser.call();
			verifyConfig(config);
			verifyNewConfig(config);
		}
		finally
		{
			in.close();
		}
	}

	public void testParseConfigWithContext() throws IOException {
		InputStream in = getClass().getResourceAsStream( "newConfig.xml" );
		try
		{
			ConfigParser parser = new ConfigParser();
			parser.setContext(getClass().getResource( "newConfig.xml" ));
			parser.setInput(new InputStreamReader( in ));
			Config config = parser.call();
			verifyConfig(config);
			verifyNewConfig(config);
		}
		finally
		{
			in.close();
		}
	}

	public void testParseOldConfig() throws IOException {
		InputStream in = getClass().getResourceAsStream( "oldConfig.xml" );
		try
		{
			ConfigParser parser = new ConfigParser();
			parser.setInput(new InputStreamReader( in ));
			Config config = parser.call();
			verifyConfig(config);
		}
		finally
		{
			in.close();
		}
	}

	public void testParseOldConfigWithContext() throws IOException {
		InputStream in = getClass().getResourceAsStream( "oldConfig.xml" );
		try
		{
			ConfigParser parser = new ConfigParser();
			parser.setContext(getClass().getResource( "newConfig.xml" ));
			parser.setInput(new InputStreamReader( in ));
			Config config = parser.call();
			verifyConfig(config);
		}
		finally
		{
   			in.close();
		}
	}

	/**
	 * Verifies that the information from the config test files is present in
	 * the config object.
	 */
	private void verifyConfig(Config config) {
		TestCase.assertNotNull( config.getDisplayConfig( DisplayConfig.EDITOR_MODE ) );
		TestCase.assertNotNull( config.getDisplayConfig( DisplayConfig.PRESENTATION_MODE ) );

		TestCase.assertEquals( 3, config.getCatalogURLs().size() );
		TestCase.assertEquals( "Opwekking", config.getDefaultBundle() );

		TestCase.assertTrue( config.isScrollBarVisible() );
		TestCase.assertTrue( config.isDividerVisible() );
		TestCase.assertEquals( 0.4f, config.getAutoScrollSpeed() );
		TestCase.assertEquals( new Color( 0x0040c0 ), config.getBackgroundColor() );
		Object backgroundImage = config.getBackgroundImage();
		TestCase.assertNotNull( backgroundImage );
		TestCase.assertTrue( backgroundImage.toString().endsWith(
		"images/backgrounds/Blue%20Waves.png" ) );

		Object[] recentFiles = config.getRecentFiles();
		TestCase.assertEquals( "number of recent files", 1, recentFiles.length );
		TestCase.assertEquals( "recent file names",
		                       "D:\\Code\\Java\\Projects\\opwViewer\\dist\\newcatalog.xml",
		                       recentFiles[ 0 ].toString() );

		TestCase.assertEquals( "number of utilities", config.getUtilities().size(), 1 );
		TestCase.assertEquals( "utility locations", "C:\\Program Files\\"
		                                            + "Microsoft Office\\PowerPoint Viewer\\PPTVIEW.EXE",
		                       config.getUtility( "ppt" ).toString() );
	}

	/**
	 * Verifies that the additional information from the new format config test
	 * files is present in the config object.
	 */
	private void verifyNewConfig(Config config) {
		TestCase.assertTrue( config.getAnnouncementPresets().size() == 2 );
		for (Announcement preset : config.getAnnouncementPresets()) {
			String name = preset.getName();
			if ("Algemene informatie".equals(name)) {
				TestCase.assertEquals( "{0}\nzangdienst door {1}\n"
				                       + "overdenking door {2}", preset.getText() );
				Set<Parameter> parameters = preset.getParameters();
				TestCase.assertEquals( "number of parameters", 3, parameters.size() );
				for (Parameter param : parameters) {
					String tag = param.getTag();
					if ("{0}".equals(tag)) {
						TestCase.assertTrue( param instanceof DateParameter );
						DateParameter dateParam = (DateParameter) param;
						TestCase.assertEquals( "EEEE d MMMM yyyy",
						                       dateParam.getFormat().toPattern() );
						// XXX: fix date values (what about: today, null, ???)

					} else if ("{1}".equals(tag)) {
						TestCase.assertTrue( param instanceof TextParameter );
						TextParameter textParam = (TextParameter) param;
						TestCase.assertEquals( "Zangdienst", textParam.getLabel() );

					} else if ("{2}".equals(tag)) {
						TestCase.assertTrue( param instanceof TextParameter );
						TextParameter textParam = (TextParameter) param;
						TestCase.assertEquals( "Overdenking", textParam.getLabel() );

					} else {
						TestCase.fail( "unexpected parameter tag: " + tag );
					}
				}

			} else if ("Autolichten".equals(name)) {
				TestCase.assertEquals( "Een auto met kenteken {0} heeft de lichten "
				                       + "nog aan.\nGeeft niks: die gaan vanzelf uit.",
				                       preset.getText() );
				Set<Parameter> parameters = preset.getParameters();
				TestCase.assertEquals( "number of parameters", 1, parameters.size() );
				for (Parameter param : parameters) {
					String tag = param.getTag();
					if ("{0}".equals(tag)) {
						TestCase.assertTrue( param instanceof TextParameter );
						TextParameter textParam = (TextParameter) param;
						TestCase.assertEquals( "Kenteken", textParam.getLabel() );
					} else {
						TestCase.fail( "unexpected parameter tag: " + tag );
					}
				}

			} else {
				TestCase.fail( "unexpected announcement preset: " + preset );
			}
		}
	}
}
