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

import java.awt.*;
import java.awt.geom.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.List;

/**
 * <p>
 * This class maintains information regarding settings configurable by the user.
 * Input and output is performed by a seperate IO class,
 * {@link lithium.io.ConfigIO}. {@code Config} objects are managed by the
 * {@link ConfigManager} class.
 *
 * @see ConfigManager
 * @see lithium.editor.ConfigEditor
 * @see lithium.io.ConfigIO
 *
 * @since 0.1
 * @author Gerrit Meinders
 */
public class Config implements Cloneable
{
	/**
	 * An enumeration of possible types of scrolling.
	 *
	 * @since 0.8
	 */
	public static enum ScrollType
	{
		/** Plain scrolling, which scrolls the full distance at once. */
		PLAIN,
		/** Smooth scrolling, which scrolls a certain distance over time. */
		SMOOTH
	}

	/**
	 * An enumeration of units used by scrollers.
	 *
	 * @since 0.8
	 */
	public static enum ScrollUnits
	{
		/** Lines of text. */
		LINES,
		/** Individual characters. */
		CHARACTERS
	}

	/**
	 * An enumeration of supported hardware acceleration methods.
	 */
	public static enum Acceleration
	{
		/** Uses the system default. */
		SYSTEM_DEFAULT,
		/** Uses Direct3D. */
		DIRECT3D,
		/** Uses OpenGL. */
		OPENGL
	}

	/**
	 * Specifies a certain kind of PPT viewer.
	 */
	public static enum PPTViewerType
	{
		/** Microsoft PowerPoint 95 or later */
		POWERPOINT,

		/** Microsoft PowerPoint Viewer 97 or later */
		POWERPOINT_VIEWER,

		/** OpenOffice.org, version 2.x */
		OPEN_OFFICE_2
	}

	/** Provides support for bounds properties. */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/** A map of display configuration settings */
	private final Map<String, DisplayConfig> displayConfigs;

	/** A list of URLs of the catalogs that get loaded on start-up */
	private final List<URL> catalogURLs;

	/** Property used to indicate that the list of default catalogs was changed. */
	public static final String CATALOG_URL_PROPERTY = "catalogURL";

	/** A list of URLs of the available collections */
	private final List<URL> collectionURLs;

	/** Property name: list of URLs of the available collections. */
	public static final String COLLECTION_URLS_PROPERTY = "collectionURLs";

	/** A list of recently opened files */
	private final LinkedList<File> recentFiles;

	/** The recent files list property. */
	public static final String RECENT_FILES_PROPERTY = "recentFiles";

	/** The maximum number of recent files in the recent files list */
	public static final int RECENT_FILE_COUNT = 4;

	/** The name of the default bundle */
	private String defaultBundle;

	/** The default bundle name property. */
	public static final String DEFAULT_BUNDLE_PROPERTY = "defaultBundle";

	/** The default background color for a lyric view */
	private Color backgroundColor;

	/** The background color property. */
	public static final String BACKGROUND_COLOR_PROPERTY = "backgroundColor";

	/** The default background color for a lyric view */
	private Color foregroundColor;

	/** The foreground color property. */
	public static final String FOREGROUND_COLOR_PROPERTY = "foregroundColor";

	/** The default background image for a lyric view */
	private URL backgroundImage;

	/** The background image property. */
	public static final String BACKGROUND_IMAGE_PROPERTY = "backgroundImage";

	/**
	 * Whether the background image should be visible in lyric views set to be
	 * previews.
	 */
	private boolean backgroundVisibleInPreview;

	/**
	 * This property indicates whether background images are visible in
	 * previews.
	 */
	public static final String BACKGROUND_VISIBLE_IN_PREVIEW_PROPERTY = "backgroundVisibleInPreview";

	/** Whether lyric views should have a visible scroll bar. */
	private boolean scrollBarVisible;

	/**
	 * This property indicates whether a scroll bar will be visible in text
	 * displays.
	 */
	public static final String SCROLLBAR_VISIBLE_PROPERTY = "scrollBarVisible";

	/**
	 * Whether there should be a visible divider (JSplitPane) between controls
	 * and the rest of a lyric view.
	 */
	private boolean dividerVisible;

	/**
	 * Indicates whether a divider is shown between the text display and
	 * controls panel in single-screen mode.
	 */
	public static final String DIVIDER_VISIBLE_PROPERTY = "dividerVisible";

	/** The minimum size of a margin in a lyric view */
	public static double MINIMUM_MARGIN = 0.0;

	/** The maximum size of a margin in a lyric view */
	public static double MAXIMUM_MARGIN = 0.3;

	/**
	 * The width of the margins used in full-screen mode, in percent relative to
	 * the screen's width.
	 */
	private Rectangle2D fullScreenMargins;

	/** The full screen margins property. */
	public static final String FULL_SCREEN_MARGINS_PROPERTY = "fullScreenMargins";

	/** The speed at which automatic scrolling scrolls. */
	private float autoScrollSpeed;

	/** The automatic scrolling speed property. */
	public static final String AUTO_SCROLL_SPEED_PROPERTY = "autoScrollSpeed";

	/**
	 * The type of scrolling used.
	 *
	 * @since 0.8, experimental 0.9x
	 */
	private ScrollType scrollType;

	/** @since 0.8, experimental 0.9x */
	public static final String SCROLL_TYPE_PROPERTY = "scrollType";

	/**
	 * The units used for scrolling and text alignment.
	 *
	 * @since 0.8, experimental 0.9x
	 */
	private ScrollUnits scrollUnits;

	/** @since 0.8, experimental 0.9x */
	public static final String SCROLL_UNITS_PROPERTY = "scrollUnits";

	/**
	 * A map containing the location of utilities by their name.
	 *
	 * @since 0.8, experimental 0.9x
	 */
	private Map<String, File> utilities;

	/** @since 0.8, experimental 0.9x */
	public static final String UTILITIES_PROPERTY = "utilities";

	/**
	 * Utility identifier: Microsoft PowerPoint, or equivalent.
	 */
	public static final String UTILITY_PPT = "ppt";

	/**
	 * Utility identifier: Microsoft PowerPoint Viewer, or equivalent.
	 */
	public static final String UTILITY_PPT_VIEWER = "pptViewer";

	/**
	 * Utility identifier: OpenOffice.
	 */
	public static final String UTILITY_OPEN_OFFICE = "openOffice";

	/**
	 * Utility identifier: VLC media player.
	 */
	public static final String UTILITY_VLC = "vlc";

	/**
	 * Utility identifier: Media Player Classic.
	 */
	public static final String UTILITY_MEDIA_PLAYER_CLASSIC = "mediaPlayerClassic";

	/**
	 * Utility identifier: LAME MP3 Encoder.
	 */
	public static final String UTILITY_LAME = "lame";

	/**
	 * The hardware acceleration method to be used. This setting requires the
	 * virtual machine to be restarted to take effect.
	 */
	private Acceleration acceleration;

	/** The hardware acceleration property. */
	public static final String ACCELERATION_PROPERTY = "acceleration";

	/** The announcement presets. */
	private Set<Announcement> announcementPresets;

	/** The announcement presets property. */
	public static final String ANNOUNCEMENT_PRESETS_PROPERTY = "announcementPresets";

	/** The announcement presets. */
	private RecorderConfig recorderConfig;

	/** The announcement presets property. */
	public static final String RECORDER_CONFIG = "recorderConfig";

	/**
	 * The fonts used to display various kinds of text, mapped by the constant
	 * for that associated kind of text.
	 */
	private final Map<TextKind, Font> fonts;

	/**
	 * Set of all defined options (defined using {@link #defineOption}).
	 */
	private static final Set<String> definedOptions = new HashSet<String>();

	/**
	 * Returns the names of all configuration options that have been defined.
	 *
	 * @return Configuration options.
	 */
	public static final SortedSet<String> getDefinedOptions()
	{
		return new TreeSet<String>(definedOptions);
	}

	/**
	 * Registers the specified option such that it will be included when
	 * available options are requested.
	 *
	 * @param identifier a name identifying the option
	 *
	 * @return the specified identifier
	 */
	private static String defineOption(String identifier)
	{
		definedOptions.add(identifier);
		return identifier;
	}

	/**
	 * Debug option: show an additional 'Debug' menu in the editor.
	 */
	public static final String DEBUG_MENU = defineOption("debugMenu");

	/**
	 * Debug option: force use of seperate controls, even if there is only one
	 * display.
	 */
	public static final String FORCE_SEPERATE_CONTROLS = defineOption("forceSeperateControls");

	/**
	 * Debug option: show full-screen controls when the editor is started.
	 */
	public static final String SHOW_CONTROLS_AT_STARTUP = defineOption("showControlsAtStartup");

	/**
	 * Debug option: add some random content to the playlist when full-screen
	 * mode is started, for testing purposes.
	 */
	public static final String DEBUG_PLAYLIST_CONTENT = defineOption("debugPlaylistContent");

	/**
	 * Experimental option: show drop shadow under text.
	 *
	 * @see lithium.display.opengl.GLTextRenderer
	 */
	public static final String TEXT_SHADOW = defineOption("textShadow");

	/**
	 * Debug option: text renderers should visibly render the baseline of each
	 * line or character. Can be useful when debugging text rendering.
	 */
	public static final String RENDER_TEXT_BASELINE = defineOption("renderTextBaseline");

	/**
	 * Debug option: disable correction of DC offset when normalizing an audio
	 * stream. This functionality is relatively new and could still be buggy, so
	 * this flag is provided to turn it off if a bug is found.
	 */
	public static final String DEBUG_DISABLE_DC_OFFSET = defineOption("debugNoDC");

	/**
	 * If enabled, a frames per second counter is shown in all view, not just
	 * previews.
	 */
	public static final String SHOW_FRAMES_PER_SECOND = defineOption("showFPS");

	/**
	 * Disables calls to 'GLTextRenderer.expandReferences'.
	 */
	public static final String DISABLE_REFERENCE_EXPANSION = defineOption("disableReferenceExpansion");

	/**
	 * Enables mark shown on previews to indicate content bounds. Which kinds of
	 * indicators are actually useful remains to be seen, so this new indicator
	 * is disabled by default.
	 */
	public static final String CONTENT_BOUNDS_INDICATOR = defineOption("contentBoundsIndicator");

	/**
	 * Set of currently enabled options.
	 */
	private final Set<String> options;

	/**
	 * Folder where files can be placed to be automatically loaded on startup.
	 */
	private File loadOnStartupFolder;

	/**
	 * Defines a particular kind of text.
	 */
	public enum TextKind
	{
		/** The default, used for the body of the content being viewed. */
		DEFAULT(40),

		/** The title of a lyric. */
		TITLE(40),

		/** A reference to a lyric. */
		REFERENCE(24),

		/** Copyrights of a lyric. */
		COPYRIGHTS(14);

		private int defaultSize;

		private TextKind(int defaultSize)
		{
			this.defaultSize = defaultSize;
		}

		public int getDefaultSize()
		{
			return defaultSize;
		}
	}

	/* Initializes collections at start of all constructors */
	{
		displayConfigs = new LinkedHashMap<String, DisplayConfig>();
		catalogURLs = new ArrayList<URL>();
		collectionURLs = new ArrayList<URL>();
		recentFiles = new LinkedList<File>();
		utilities = new LinkedHashMap<String, File>();
		announcementPresets = new LinkedHashSet<Announcement>();

		fonts = new HashMap<TextKind, Font>();
	}

	/** Constructs a config instance with default settings. */
	public Config()
	{
		defaultBundle = "";

		foregroundColor = Color.WHITE;
		backgroundColor = new Color(60, 115, 170);
		backgroundImage = null;
		backgroundVisibleInPreview = false;

		scrollBarVisible = false;
		dividerVisible = false;
		setFullScreenMargins(new Rectangle2D.Double(0.05, 0.05, 0.9, 0.9));

		autoScrollSpeed = 0.25f;
		scrollType = ScrollType.SMOOTH;
		scrollUnits = ScrollUnits.LINES;

		acceleration = Acceleration.SYSTEM_DEFAULT;

		recorderConfig = new RecorderConfig();

		fonts.put(TextKind.TITLE, new Font(Font.SANS_SERIF, Font.BOLD
		        | Font.ITALIC, 40));
		fonts.put(TextKind.DEFAULT, new Font(Font.SANS_SERIF, Font.PLAIN, 40));
		fonts.put(TextKind.REFERENCE, new Font(Font.SANS_SERIF, Font.PLAIN, 24));
		fonts.put(TextKind.COPYRIGHTS,
		        new Font(Font.SANS_SERIF, Font.PLAIN, 14));

		options = new HashSet<String>();

		loadOnStartupFolder = null;
	}

	/**
	 * Constructs a new configuration that exactly matches the given config
	 * instance.
	 *
	 * @param config the config
	 */
	public Config(Config config)
	{
		this();

		this.displayConfigs.putAll(config.displayConfigs);
		this.catalogURLs.addAll(config.catalogURLs);
		this.collectionURLs.addAll(config.collectionURLs);
		this.recentFiles.addAll(config.recentFiles);
		this.utilities.putAll(config.utilities);
		for (Announcement preset : config.announcementPresets)
		{
			this.announcementPresets.add(preset.clone());
		}

		this.defaultBundle = config.defaultBundle;

		this.backgroundColor = config.backgroundColor;
		this.foregroundColor = config.foregroundColor;
		this.backgroundImage = config.backgroundImage;
		this.backgroundVisibleInPreview = config.backgroundVisibleInPreview;

		this.scrollBarVisible = config.scrollBarVisible;
		this.dividerVisible = config.dividerVisible;
		this.fullScreenMargins = config.fullScreenMargins;

		this.autoScrollSpeed = config.autoScrollSpeed;
		this.scrollType = config.scrollType;
		this.scrollUnits = config.scrollUnits;

		this.acceleration = config.acceleration;

		this.recorderConfig = new RecorderConfig(config.recorderConfig);

		this.fonts.putAll(config.fonts);
		this.options.addAll(config.options);

		this.loadOnStartupFolder = config.loadOnStartupFolder;
	}

	@Override
	public Config clone()
	{
		return new Config(this);
	}

	public void setConfig(Config config)
	{
		displayConfigs.clear();
		displayConfigs.putAll(config.displayConfigs);

		catalogURLs.clear();
		catalogURLs.addAll(config.catalogURLs);
		pcs.firePropertyChange(CATALOG_URL_PROPERTY, null, null);

		collectionURLs.clear();
		collectionURLs.addAll(config.collectionURLs);
		pcs.firePropertyChange(COLLECTION_URLS_PROPERTY, null, null);

		recentFiles.clear();
		recentFiles.addAll(config.recentFiles);
		pcs.firePropertyChange(RECENT_FILES_PROPERTY, null, null);

		utilities.clear();
		utilities.putAll(config.utilities);
		pcs.firePropertyChange(UTILITIES_PROPERTY, null, null);

		announcementPresets.clear();
		for (Announcement preset : config.announcementPresets)
		{
			announcementPresets.add(preset.clone());
		}
		pcs.firePropertyChange(ANNOUNCEMENT_PRESETS_PROPERTY, null, null);

		setDefaultBundle(config.defaultBundle);

		setBackgroundColor(config.backgroundColor);
		setForegroundColor(config.foregroundColor);
		setBackgroundImage(config.backgroundImage);
		setBackgroundVisibleInPreview(config.backgroundVisibleInPreview);

		setScrollBarVisible(config.scrollBarVisible);
		setDividerVisible(config.dividerVisible);
		setFullScreenMargins(config.fullScreenMargins);

		setAutoScrollSpeed(config.autoScrollSpeed);
		setScrollType(config.scrollType);
		setScrollUnits(config.scrollUnits);

		setAcceleration(config.acceleration);

		setRecorderConfig(new RecorderConfig(config.recorderConfig));

		fonts.clear();
		fonts.putAll(config.fonts);

		options.clear();
		options.addAll(config.options);

		setLoadOnStartupFolder(config.loadOnStartupFolder);
	}

	/**
	 * Creates a new configuration with default settings, with default display
	 * configurations and all catalogs found in the program directory set to
	 * load on startup.
	 *
	 * @return the default configuration settings
	 */
	public static Config createDefaultConfig()
	{
		Config config = new Config();

		config.addDisplayConfig(DisplayConfig.EDITOR_MODE,
		        DisplayConfig.createDefaultConfig());
		config.addDisplayConfig(DisplayConfig.PRESENTATION_MODE,
		        DisplayConfig.createDefaultConfig());

		File lyricsFolder = ConfigManager.getLyricsFolder();
		File[] catalogs = lyricsFolder.listFiles(FilterManager.getCombinedFilter(FilterType.CATALOG));
		if (catalogs != null)
		{
			for (File catalog : catalogs)
			{
				if (catalog.isFile())
				{
					try
					{
						config.addCatalogURL(catalog.toURI().toURL());
					}
					catch (MalformedURLException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		File booksFolder = ConfigManager.getBooksFolder();
		File[] collections = booksFolder.listFiles(FilterManager.getCombinedFilter(FilterType.COLLECTION));
		if (collections != null)
		{
			for (File collection : collections)
			{
				if (collection.isFile())
				{
					try
					{
						config.addCollectionURL(collection.toURI().toURL());
					}
					catch (MalformedURLException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		config.detectUtilities();

		return config;
	}

	/**
	 * Adds or replaces the specified display configuration.
	 *
	 * @param id the display configuration ID
	 * @param displayConfig the display configuration
	 */
	public void addDisplayConfig(String id, DisplayConfig displayConfig)
	{
		displayConfigs.put(id, displayConfig);
	}

	/**
	 * Returns the display configuration with the given ID.
	 *
	 * @param id the ID
	 * @return the display configuration
	 */
	public DisplayConfig getDisplayConfig(String id)
	{
		return displayConfigs.get(id);
	}

	/**
	 * Returns an array of the IDs for all configured display configurations.
	 *
	 * @return an array of IDs
	 */
	public String[] getDisplayConfigIds()
	{
		return displayConfigs.keySet().toArray(new String[0]);
	}

	/**
	 * Adds the catalog at the given URL to the default catalogs.
	 *
	 * @param url the location of the catalog
	 */
	public void addCatalogURL(URL url)
	{
		catalogURLs.add(url);
		pcs.firePropertyChange(CATALOG_URL_PROPERTY, null, url);
	}

	/**
	 * Adds the specified catalog to the default catalogs.
	 *
	 * @param index the index in the default catalogs list
	 * @param url the location of the catalog
	 */
	public void addCatalogURL(int index, URL url)
	{
		catalogURLs.add(index, url);
		pcs.firePropertyChange(CATALOG_URL_PROPERTY, null, url);
	}

	/**
	 * Removes the specified catalog from the default catalogs.
	 *
	 * @param url the location of the catalog
	 */
	public void removeCatalogURL(URL url)
	{
		catalogURLs.remove(url);
		pcs.firePropertyChange(CATALOG_URL_PROPERTY, url, null);
	}

	/**
	 * Returns the list of default catalogs.
	 *
	 * @return a list of catalog URLs
	 */
	public List<URL> getCatalogURLs()
	{
		return Collections.unmodifiableList(catalogURLs);
	}

	/**
	 * Adds the specified collection to the available collections.
	 *
	 * @param url the location of the collection
	 */
	public void addCollectionURL(URL url)
	{
		collectionURLs.add(url);
		pcs.firePropertyChange(COLLECTION_URLS_PROPERTY, null, null);
	}

	/**
	 * Removes the specified collection from the available collections.
	 *
	 * @param url the location of the collection
	 */
	public void removeCollectionURL(URL url)
	{
		collectionURLs.remove(url);
		pcs.firePropertyChange(COLLECTION_URLS_PROPERTY, null, null);
	}

	/**
	 * Returns the list of available collections.
	 *
	 * @return a list of available collection URLs
	 */
	public List<URL> getCollectionURLs()
	{
		return Collections.unmodifiableList(collectionURLs);
	}

	/**
	 * Sets the default bundle name.
	 *
	 * @param defaultBundle the bundle name to be set
	 */
	public void setDefaultBundle(String defaultBundle)
	{
		String oldValue = this.defaultBundle;
		this.defaultBundle = defaultBundle;
		pcs.firePropertyChange(DEFAULT_BUNDLE_PROPERTY, oldValue, defaultBundle);
	}

	/**
	 * Returns the default bundle name.
	 *
	 * @return the bundle name
	 */
	public String getDefaultBundle()
	{
		return defaultBundle;
	}

	/**
	 * Returns whether lyric views should show a scroll bar.
	 *
	 * @return whether a scroll bar should be visible
	 */
	public boolean isScrollBarVisible()
	{
		return scrollBarVisible;
	}

	/**
	 * Sets whether lyric views should show a scroll bar.
	 *
	 * @param scrollBarVisible whether a scroll bar should be visible
	 */
	public void setScrollBarVisible(boolean scrollBarVisible)
	{
		boolean oldValue = this.scrollBarVisible;
		this.scrollBarVisible = scrollBarVisible;
		if (oldValue != scrollBarVisible)
		{
			pcs.firePropertyChange(SCROLLBAR_VISIBLE_PROPERTY, oldValue,
			        scrollBarVisible);
		}
	}

	/**
	 * Returns whether the full-screen display should provide a divider to
	 * resize the controls panel.
	 *
	 * @return whether a divider should be visible
	 */
	public boolean isDividerVisible()
	{
		return dividerVisible;
	}

	/**
	 * Sets whether the full-screen display should provide a divider to resize
	 * the controls panel.
	 *
	 * @param dividerVisible whether a divider should be visible
	 */
	public void setDividerVisible(boolean dividerVisible)
	{
		boolean oldValue = this.dividerVisible;
		this.dividerVisible = dividerVisible;
		if (oldValue != dividerVisible)
		{
			pcs.firePropertyChange(DIVIDER_VISIBLE_PROPERTY, oldValue,
			        dividerVisible);
		}
	}

	/**
	 * Adds or moves the given file to the start of the recent files list.
	 *
	 * @param file the file
	 */
	public void setRecentFile(File file)
	{
		if (file == null)
		{
			throw new NullPointerException("file");
		}

		// remove the file from the recent files (if found)
		while (recentFiles.remove(file))
			;

		recentFiles.addFirst(file);
		while (recentFiles.size() > RECENT_FILE_COUNT)
		{
			recentFiles.removeLast();
		}
		pcs.firePropertyChange(RECENT_FILES_PROPERTY, null, file);
	}

	/**
	 * Returns a list of recently opened files.
	 *
	 * @return the recently opened files
	 */
	public File[] getRecentFiles()
	{
		return recentFiles.toArray(new File[0]);
	}

	/**
	 * Returns the the speed at which automatic scrolling scrolls.
	 *
	 * @return the automatic scrolling speed
	 */
	public float getAutoScrollSpeed()
	{
		return autoScrollSpeed;
	}

	/**
	 * Sets the the speed at which automatic scrolling scrolls.
	 *
	 * @param autoScrollSpeed the automatic scrolling speed
	 */
	public void setAutoScrollSpeed(float autoScrollSpeed)
	{
		float oldValue = this.autoScrollSpeed;
		this.autoScrollSpeed = autoScrollSpeed;
		pcs.firePropertyChange(AUTO_SCROLL_SPEED_PROPERTY, oldValue,
		        autoScrollSpeed);
	}

	/**
	 * Returns whether the controls panel should be separated from the
	 * full-screen display.
	 *
	 * @return whether seperate controls should be used
	 */
	public boolean isSeperateControlsEnabled()
	{
		if (isEnabled(Config.FORCE_SEPERATE_CONTROLS))
		{
			return true;
		}

		DisplayConfig editor = getDisplayConfig(DisplayConfig.EDITOR_MODE);
		DisplayConfig presentation = getDisplayConfig(DisplayConfig.PRESENTATION_MODE);
		if (editor == null || presentation == null)
		{
			return false;
		}
		else
		{
			return !editor.getGraphicsConfiguration().equals(
			        presentation.getGraphicsConfiguration());
		}
	}

	/**
	 * Returns the rectangle just inside of the margins used in full-screen
	 * mode. The units used are relative to the width and height of the screen.
	 *
	 * @return the margins rectangle
	 */
	public Rectangle2D getFullScreenMargins()
	{
		return fullScreenMargins;
	}

	/**
	 * Sets the rectangle just inside of the margins used in full-screen mode.
	 * The units used equate to 1 percent of the screen's width.
	 *
	 * @param fullScreenMargins the margins rectangle to be set
	 */
	public void setFullScreenMargins(Rectangle2D fullScreenMargins)
	{
		// enforce minimum/maximum margins
		Rectangle2D minimumMargins = new Rectangle2D.Double(MINIMUM_MARGIN,
		        MINIMUM_MARGIN, 1.0 - MINIMUM_MARGIN * 2,
		        1.0 - MINIMUM_MARGIN * 2);
		Rectangle2D maximumMargins = new Rectangle2D.Double(MAXIMUM_MARGIN,
		        MAXIMUM_MARGIN, 1.0 - MAXIMUM_MARGIN * 2,
		        1.0 - MAXIMUM_MARGIN * 2);

		Rectangle2D correctedMargins;
		correctedMargins = fullScreenMargins.createUnion(maximumMargins);
		correctedMargins = fullScreenMargins.createIntersection(minimumMargins);

		Rectangle2D oldValue = this.fullScreenMargins;
		this.fullScreenMargins = correctedMargins;
		pcs.firePropertyChange(FULL_SCREEN_MARGINS_PROPERTY, oldValue,
		        correctedMargins);
	}

	/**
	 * Returns the background color to be used by lyric views.
	 *
	 * @return the background color
	 */
	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	/**
	 * Sets the background color to be used by lyric views.
	 *
	 * @param backgroundColor the background color
	 */
	public void setBackgroundColor(Color backgroundColor)
	{
		assert backgroundColor != null : "backgroundColor != null";
		Color oldValue = this.backgroundColor;
		this.backgroundColor = backgroundColor;
		pcs.firePropertyChange(BACKGROUND_COLOR_PROPERTY, oldValue,
		        backgroundColor);
	}

	/**
	 * Returns the foreground color to be used by lyric views.
	 *
	 * @return the foreground color
	 */
	public Color getForegroundColor()
	{
		return foregroundColor;
	}

	/**
	 * Sets the foreground color to be used by lyric views.
	 *
	 * @param foregroundColor the foreground color
	 */
	public void setForegroundColor(Color foregroundColor)
	{
		assert foregroundColor != null : "foregroundColor != null";
		Color oldValue = this.foregroundColor;
		this.foregroundColor = foregroundColor;
		pcs.firePropertyChange(FOREGROUND_COLOR_PROPERTY, oldValue,
		        foregroundColor);
	}

	/**
	 * Returns the location of the background image to be used by lyric views.
	 *
	 * @return the background image location
	 */
	public URL getBackgroundImage()
	{
		return backgroundImage;
	}

	/**
	 * Sets the location of the background image to be used by lyric views.
	 *
	 * @param backgroundImage the background image location
	 */
	public void setBackgroundImage(URL backgroundImage)
	{
		URL oldValue = this.backgroundImage;
		this.backgroundImage = backgroundImage;
		pcs.firePropertyChange(BACKGROUND_IMAGE_PROPERTY, oldValue,
		        backgroundImage);
	}

	/**
	 * Returns whether a background image should be visible in preview lyric
	 * views.
	 *
	 * @return whether a background image should be visible
	 */
	public boolean isBackgroundVisibleInPreview()
	{
		return backgroundVisibleInPreview;
	}

	/**
	 * Sets whether a background image should be visible in preview lyric views.
	 *
	 * @param backgroundVisibleInPreview whether a background image should be
	 *            visible
	 */
	public void setBackgroundVisibleInPreview(boolean backgroundVisibleInPreview)
	{
		boolean oldValue = this.backgroundVisibleInPreview;
		this.backgroundVisibleInPreview = backgroundVisibleInPreview;
		pcs.firePropertyChange(BACKGROUND_VISIBLE_IN_PREVIEW_PROPERTY,
		        oldValue, backgroundVisibleInPreview);
	}

	/**
	 * Returns the folder where background images included with the software are
	 * stored.
	 *
	 * @return the background images folder
	 */
	public File getBackgroundsFolder()
	{
		return new File(ConfigManager.getSettingsFolder(),
		        "images/backgrounds/");
	}

	/**
	 * Returns the preferred type of scrolling.
	 *
	 * @return the type of scrolling
	 * @since 0.8, experimental 0.9x
	 */
	public ScrollType getScrollType()
	{
		return scrollType;
	}

	/**
	 * Sets the preferred type of scrolling.
	 *
	 * @param scrollType the type of scrolling
	 * @since 0.8, experimental 0.9x
	 */
	public void setScrollType(ScrollType scrollType)
	{
		ScrollType oldValue = this.scrollType;
		this.scrollType = scrollType;
		pcs.firePropertyChange(SCROLL_TYPE_PROPERTY, oldValue, scrollType);
	}

	/**
	 * Returns the logical units used for scrolling and text alignment.
	 *
	 * @return the kind of units
	 * @since 0.8, experimental 0.9x
	 */
	public ScrollUnits getScrollUnits()
	{
		return scrollUnits;
	}

	/**
	 * Sets the logical units used for scrolling and text alignment.
	 *
	 * @param scrollUnits the kind of units
	 * @since 0.8, experimental 0.9x
	 */
	public void setScrollUnits(ScrollUnits scrollUnits)
	{
		ScrollUnits oldValue = this.scrollUnits;
		this.scrollUnits = scrollUnits;
		pcs.firePropertyChange(SCROLL_UNITS_PROPERTY, oldValue, scrollUnits);
	}

	/**
	 * Returns the executable used to view presentations in Microsoft PowerPoint
	 * format.
	 *
	 * @return the viewer executable
	 * @since 0.8, experimental 0.9x
	 */
	public File getPPTViewer()
	{
		File result = getUtility(UTILITY_PPT);
		if (result == null || !result.exists())
		{
			result = getUtility(UTILITY_PPT_VIEWER);
		}
		if (result == null || !result.exists())
		{
			result = getUtility(UTILITY_OPEN_OFFICE);
		}
		return (result == null || !result.exists()) ? null : result;
	}

	public PPTViewerType getPPTViewerType()
	{
		final PPTViewerType result;
		if (getUtility(UTILITY_PPT) != null)
		{
			result = PPTViewerType.POWERPOINT;
		}
		else if (getUtility(UTILITY_PPT_VIEWER) != null)
		{
			result = PPTViewerType.POWERPOINT_VIEWER;
		}
		else if (getUtility(UTILITY_OPEN_OFFICE) != null)
		{
			result = PPTViewerType.OPEN_OFFICE_2;
		}
		else
		{
			result = null;
		}
		return result;
	}

	/**
	 * Returns the location of a utility given its name.
	 *
	 * @param name the utilty's name
	 * @return the utility's location
	 * @since 0.8, experimental 0.9x
	 */
	public File getUtility(String name)
	{
		return utilities.get(name);
	}

	/**
	 * Returns a map containing all configured utility locations by utility
	 * name.
	 *
	 * @return a map of utility locations by utility name
	 * @since 0.8, experimental 0.9x
	 */
	public Map<String, File> getUtilities()
	{
		return Collections.unmodifiableMap(utilities);
	}

	/**
	 * Adds or replaces the location of the given utility.
	 *
	 * @param name the name of the utility
	 * @param location the location of the utility
	 * @since 0.8, experimental 0.9x
	 */
	public void addUtility(String name, File location)
	{
		if (name == null)
		{
			throw new NullPointerException("name");
		}
		if (location == null)
		{
			throw new NullPointerException("location");
		}
		utilities.put(name, location);
		pcs.firePropertyChange(UTILITIES_PROPERTY, null, scrollUnits);
	}

	/**
	 * Rmoves the configuration entry for the given utility.
	 *
	 * @param name the name of the utility
	 * @since 0.8, experimental 0.9x
	 */
	public void removeUtility(String name)
	{
		if (name == null)
		{
			throw new NullPointerException("name");
		}
		File oldValue = getUtility(name);
		utilities.remove(name);
		pcs.firePropertyChange(UTILITIES_PROPERTY, oldValue, null);
	}

	/**
	 * Searches for utilties at common or default locations and adds settings
	 * for any found utilities. This method will never replace existing utility
	 * settings.
	 *
	 * @since 0.8, experimental 0.9x
	 */
	public void detectUtilities()
	{
		if (getUtility(Config.UTILITY_PPT) == null)
		{
			File powerPoint = detectNested(new File(
			        "C:\\Program Files\\Microsoft Office\\"), "POWERPNT.EXE");
			if (powerPoint != null)
			{
				System.out.println("Found utility: Microsoft Office");
				addUtility(Config.UTILITY_PPT, powerPoint);
			}
		}

		if (getUtility(Config.UTILITY_PPT_VIEWER) == null)
		{
			File powerPointViewer = new File("C:\\Program Files\\"
			        + "Microsoft Office\\PowerPoint Viewer\\PPTVIEW.EXE");
			if (powerPointViewer.exists())
			{
				System.out.println("Found utility: Microsoft PowerPoint Viewer");
				addUtility(Config.UTILITY_PPT_VIEWER, powerPointViewer);
			}
		}

		if (getUtility(Config.UTILITY_OPEN_OFFICE) == null)
		{
			File openOffice = new File("C:\\Program Files\\"
			        + "OpenOffice.org 3\\program\\soffice.exe");
			if (openOffice.exists())
			{
				System.out.println("Found utility: OpenOffice.org 3");
				addUtility(Config.UTILITY_OPEN_OFFICE, openOffice);
			}
		}

		if (getUtility(Config.UTILITY_MEDIA_PLAYER_CLASSIC) == null)
		{
			final File location1 = new File("C:\\Program Files\\"
			        + "Media Player Classic\\mplayerc.exe");
			final File location2 = new File("C:\\Program Files\\"
			        + "Combined Community Codec Pack\\MPC\\mplayerc.exe");
			File mediaPlayerClassic = detect(location1, location2);
			if (mediaPlayerClassic != null && mediaPlayerClassic.exists())
			{
				System.out.println("Found utility: Media Player Classic");
				addUtility(Config.UTILITY_MEDIA_PLAYER_CLASSIC,
				        mediaPlayerClassic);
			}
		}

		if (getUtility(Config.UTILITY_LAME) == null)
		{
			File[] locations = { new File("/usr/bin/lame") };

			File mediaPlayerClassic = detect(locations);
			if (mediaPlayerClassic != null && mediaPlayerClassic.exists())
			{
				System.out.println("Found utility: LAME MP3 Encoder");
				addUtility(Config.UTILITY_LAME, mediaPlayerClassic);
			}
		}
	}

	/**
	 * Detects whether the specified pathnames exist and returns the first one
	 * that exists.
	 *
	 * @param pathnames the pathnames to be checked
	 * @return the first existing pathname, if any.
	 * @throws NullPointerException if any pathname is <code>null</code>
	 */
	private File detect(File... pathnames)
	{
		for (File pathname : pathnames)
		{
			if (pathname.exists())
			{
				return pathname;
			}
		}
		return null;
	}

	/**
	 * Find the first file with the specified name located in the specified root
	 * folder or a nested folder inside it. File names are matched using
	 * {@link File#equals(Object)}.
	 *
	 * @param root the folder to search in
	 * @param fileName the filename to be found
	 * @return the first file found, if any
	 */
	private File detectNested(File root, final String fileName)
	{
		final File[] files = root.listFiles(new FileFilter()
		{
			public boolean accept(File pathname)
			{
				if (pathname.isDirectory())
				{
					return true;
				}
				else
				{
					File searched = new File(pathname.getParentFile(), fileName);
					return searched.equals(pathname);
				}
			}
		});

		if (files != null)
		{
			for (File file : files)
			{
				if (!file.isDirectory())
				{
					return file;
				}
			}

			for (File file : files)
			{
				File nestedResult = detectNested(file, fileName);
				if (nestedResult != null)
				{
					return nestedResult;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the hardware acceleration method to be used.
	 *
	 * @return the hardware acceleration method
	 */
	public Acceleration getAcceleration()
	{
		return acceleration;
	}

	/**
	 * Sets the hardware acceleration method to be used.
	 *
	 * @param acceleration the hardware acceleration method
	 */
	public void setAcceleration(Acceleration acceleration)
	{
		if (this.acceleration != acceleration)
		{
			Acceleration oldValue = this.acceleration;
			this.acceleration = acceleration;
			pcs.firePropertyChange(ACCELERATION_PROPERTY, oldValue,
			        acceleration);
		}
	}

	/**
	 * Returns the available announcement presets.
	 *
	 * @return a set of announcement presets
	 */
	public Set<Announcement> getAnnouncementPresets()
	{
		return Collections.unmodifiableSet(announcementPresets);
	}

	/**
	 * Sets the available announcement presets.
	 *
	 * @param announcementPresets a set of announcement presets
	 */
	public void setAnnouncementPresets(Set<Announcement> announcementPresets)
	{
		if (!this.announcementPresets.equals(announcementPresets))
		{
			this.announcementPresets.clear();
			this.announcementPresets.addAll(announcementPresets);
			pcs.firePropertyChange(ANNOUNCEMENT_PRESETS_PROPERTY, null, null);
		}
	}

	public RecorderConfig getRecorderConfig()
	{
		return recorderConfig;
	}

	public void setRecorderConfig(RecorderConfig recorderConfig)
	{
		if (this.recorderConfig != recorderConfig)
		{
			RecorderConfig oldValue = this.recorderConfig;
			this.recorderConfig = recorderConfig;
			pcs.firePropertyChange(RECORDER_CONFIG, oldValue, recorderConfig);
		}
	}

	public void setFont(TextKind kind, Font font)
	{
		fonts.put(kind, font);
	}

	public Font getFont(TextKind kind)
	{
		return fonts.get(kind);
	}

	/**
	 * Enables or disables the specified option based on the value of the
	 * {@code enabled} paramter.
	 *
	 * @param option the option to be enabled or disabled
	 * @param enabled {@code true} to enable the option; {@code false} to
	 *            disable it
	 */
	public void setEnabled(String option, boolean enabled)
	{
		if (enabled)
		{
			options.add(option);
		}
		else
		{
			options.remove(option);
		}
	}

	/**
	 * Returns whether the specified option is enabled. For use with
	 * experimental or debugging options.
	 *
	 * @param option the option to be checked
	 *
	 * @return whether the option is enabled
	 */
	public boolean isEnabled(String option)
	{
		return options.contains(option);
	}

	/**
	 * Returns the set of all currently enabled options.
	 *
	 * @return the options that are currently enabled
	 */
	public Set<String> getEnabledOptions()
	{
		return Collections.unmodifiableSet(options);
	}

	/**
	 * Returns the folder where files can be placed to be loaded on startup.
	 *
	 * @return the folder from which content is loaded on startup
	 */
	public File getLoadOnStartupFolder()
	{
		return loadOnStartupFolder;
	}

	/**
	 * Sets the folder where files can be placed to be loaded on startup.
	 *
	 * @param loadOnStartupFolder the folder to be set
	 */
	public void setLoadOnStartupFolder(File loadOnStartupFolder)
	{
		this.loadOnStartupFolder = loadOnStartupFolder;
	}

	/**
	 * Adds the given property change listener to the listener list.
	 *
	 * @param listener the property change listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Removes the given property change listener from the listener list.
	 *
	 * @param listener the property change listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}

	public Set<File> getLoadOnStartupFiles(com.github.meinders.common.FileFilter[] filters)
	{
		Set<File> result = new LinkedHashSet<File>();

		File mainStartupFolder = getLoadOnStartupFolder();
		if (mainStartupFolder != null)
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			String date = dateFormat.format(new Date());
			File dailyStartupFolder = new File(mainStartupFolder, date);

			for (FileFilter filter : filters)
			{
				File[] mainFiles = mainStartupFolder.listFiles(filter);
				if (mainFiles != null)
				{
					result.addAll(Arrays.asList(mainFiles));
				}

				File[] dailyFiles = dailyStartupFolder.listFiles(filter);
				if (dailyFiles != null)
				{
					result.addAll(Arrays.asList(dailyFiles));
				}
			}

			Iterator<File> iterator = result.iterator();
			while (iterator.hasNext())
			{
				if (iterator.next().isDirectory())
				{
					iterator.remove();
				}
			}
		}

		return result;
	}
}
