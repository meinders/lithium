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

import java.beans.*;
import java.io.*;
import java.util.*;

import com.github.meinders.common.*;
import lithium.io.*;

/**
 * This class manages the active configuration.
 *
 * @author Gerrit Meinders
 */
public class ConfigManager
{
	public static final String CONFIG_PROPERTY = "config";

	public static final String CONFIG_FILE = "config.xml";

	private static final ConfigManager instance = new ConfigManager();

	public static ConfigManager getInstance()
	{
		return instance;
	}

	private Config config = new Config();

	private PropertyChangeListener listener = new ConfigListener();

	private List<PropertyChangeListener> configListeners = new ArrayList<PropertyChangeListener>();

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private ConfigManager()
	{
		// private default constructor for singleton
	}

	public void addConfigListener( PropertyChangeListener listener )
	{
		configListeners.add( listener );
	}

	public static boolean readConfig()
	{
		Config config;
		try
		{
			config = ConfigIO.read(getConfigFile());
			config.detectUtilities();

			Set<String> enabledOptions = config.getEnabledOptions();
			if (!config.getEnabledOptions().isEmpty())
			{
				for (String option : enabledOptions)
				{
					System.out.println("Debug option '" + option + "' enabled.");
				}
			}

			ConfigManager configManager = getInstance();
			configManager.setConfig( config );
			return true;

		}
		catch (IOException e)
		{
			ResourceUtilities resources = Resources.get();
			String configData = resources.getString("configData");
			String cantRead = resources.getString("cantRead", configData);
			String message = resources.getString("details", cantRead,
			        e.getMessage());
			System.err.println(message);
			return false;
		}
	}

	/**
	 * Returns the user's home folder.
	 *
	 * @return Home folder.
	 */
	public static File getHomeFolder()
	{
		return new File(System.getProperty("user.home"));
	}

	/**
	 * Returns the folder where program settings are stored.
	 *
	 * @return Folder for settings.
	 */
	public static File getSettingsFolder()
	{
		Application application = Application.getInstance();
		ApplicationDescriptor descriptor = application.getDescriptor();
		String settingsFolderName = descriptor.getTitle();

		/*
		 * Find appropriate folder for application data.
		 */
		// Windows Vista
		File userHome = getHomeFolder();
		File applicationData = new File(userHome, "AppData");
		if (applicationData.exists())
		{
			applicationData = new File(applicationData, "Roaming");
		}
		else
		{
			// Windows 2000/XP
			applicationData = new File(userHome, "Application Data");
			if (!applicationData.exists())
			{
				// Linux
				settingsFolderName = "." + descriptor.getShortName();
				applicationData = userHome;
			}
		}

		/*
		 * Create settings folder if it doesn't exist yet.
		 */
		File settingsFolder = new File(applicationData, settingsFolderName);
		if (!settingsFolder.exists())
		{
			settingsFolder.mkdir();
		}

		return settingsFolder;
	}

	/**
	 * Returns the location of the main configuration file, 'config.xml'. The
	 * configuration file may not exist yet.
	 *
	 * @return Location of 'config.xml'.
	 */
	public static File getConfigFile()
	{
		return new File(getSettingsFolder(), CONFIG_FILE);
	}

	/**
	 * Returns the location of the lyrics folder, where catalog files containing
	 * lyrics are located by default. The configuration file may not exist yet.
	 *
	 * @return Location of 'config.xml'.
	 */
	public static File getLyricsFolder()
	{
		File lyricsFolder = new File(getSettingsFolder(), "lyrics");
		if (!lyricsFolder.exists())
		{
			lyricsFolder.mkdir();
		}
		return lyricsFolder;
	}

	public static File getBooksFolder()
	{
		File booksFolder = new File(getSettingsFolder(), "books");
		if (!booksFolder.exists())
		{
			booksFolder.mkdir();
		}
		return booksFolder;
	}

	public static void writeConfig() throws IOException
	{
		ConfigIO.write( getConfig(), getConfigFile());
	}

	public void setConfig(Config config)
	{
		assert config != null;
		Config oldConfig = this.config;
		if (oldConfig != config)
		{
			// remove listener
			if (oldConfig != null)
			{
				oldConfig.removePropertyChangeListener(listener);
			}

			this.config = config;
			config.addPropertyChangeListener(listener);
			instance.pcs.firePropertyChange(CONFIG_PROPERTY, oldConfig, config);
		}
	}

	public static Config getConfig()
	{
		ConfigManager configManager = getInstance();
		return configManager.getCurrentConfig();
	}

	public Config getCurrentConfig()
	{
		return config;
	}

	public void addPropertyChangeListener(PropertyChangeListener l)
	{
		instance.pcs.addPropertyChangeListener( l );
	}

	public void removePropertyChangeListener(PropertyChangeListener l)
	{
		instance.pcs.removePropertyChangeListener(l);
	}

	private class ConfigListener implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent e)
		{
			try
			{
				writeConfig();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}

			for ( final PropertyChangeListener configListener : configListeners )
			{
				configListener.propertyChange( e );
			}
		}
	}
}
