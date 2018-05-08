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
import java.io.*;
import java.util.*;
import javax.swing.*;

import com.github.meinders.common.*;
import lithium.audio.*;

/**
 * This class initiates loading of configuration settings and creation of the
 * user interface.
 *
 * @version 0.9 (2006.07.14)
 * @author Gerrit Meinders
 */
public class RecorderStandalone
implements Runnable
{
	private static final int DEBUG_LEVEL = 0;

	/**
	 * Starts the application from the AWT event thread with the given
	 * arguments.
	 *
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args)
	{
		configureAWT();
		SwingUtilities.invokeLater( new RecorderStandalone( args ) );
	}

	private static void configureAWT()
	{
		// required for loading the config
		Resources.set(new ResourceUtilities("lithium.Resources"));
	}

	private static enum State
	{
		INITIAL, DATA_LOADING, GUI_STARTUP, FINAL, DEBUG_IMAGE_BROWSER
	}

	private State state;

	/** The command-line arguments given to this instance of the application. */
	private Set<String> argSet = new HashSet<String>();

	/**
	 * Creates a runnable instance of the application start-up code with the
	 * given set of arguments.
	 *
	 * @param args the command-line arguments
	 */
	private RecorderStandalone( String[] args )
	{
		for (String argument : args)
		{
			argSet.add(argument);
		}
		stateStart( State.INITIAL);
	}

	/**
	 * Starts the application.
	 */
	public void run()
	{
		if (DEBUG_LEVEL >= 2)
		{
			System.out.println("Main: running from " + Thread.currentThread());
		}
		try
		{
			switch (state)
			{
			case INITIAL:
			{
				assert SwingUtilities.isEventDispatchThread() : "must be on run event dispatching thread";

				ApplicationDescriptor application = Application.getInstance().getDescriptor();
				System.out.println(application.getTitle() + " "
				        + application.getVersion());

				// configure look and feel
				try
				{
					UIManager.put("swing.boldMetal", Boolean.FALSE);
					// UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				}
				catch (Exception e)
				{
					// The cross-platform LAF is always available.
				}
				ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
				toolTipManager.setLightWeightPopupEnabled(false);
				// JFrame.setDefaultLookAndFeelDecorated(true);
				// JDialog.setDefaultLookAndFeelDecorated(true);

				// interpret commandline arguments
				/*
				 * TODO: design: standardize parsing commandline arguments
				 *
				 * The parsing of command-line arguments should not be done
				 * here, but using a seperate package more suited for that job.
				 * The resulting code should look like the following:
				 *
				 * CommandLineModel model = new CommandLineModel(); String
				 * description = ...; Option helpOption = new Option("help",
				 * description); ... model.add(helpOption); ...
				 * CommandLineParser parser = new CommandLineParser(model);
				 * CommandLineData data = parser.parse(args);
				 *
				 * if (data.isSet(helpOption)) { ... }
				 */
				if (argSet.contains("--locale=NL"))
				{
					Locale.setDefault(new Locale("nl", "NL"));
				}

				if (argSet.contains("--locale=US"))
				{
					Locale.setDefault(Locale.US);
				}

				/*
				 * // localization test Locale.setDefault(new Locale("en",
				 * "US"));
				 */

				// load resources
				Resources.set(new ResourceUtilities("lithium.Resources"));

				// process some more command-line arguments
				if (argSet.contains("-?") || argSet.contains("--help"))
				{
					showCommandLineHelp();
					return;
				}

				Filters.registerFilters();

				break;
			}

			case DATA_LOADING:
			{
				ApplicationDescriptor application = Application.getInstance().getDescriptor();

				ConfigManager configManager = ConfigManager.getInstance();

				// load configuration (config.xml)
				Config config;
				if (ConfigManager.readConfig())
				{
					config = ConfigManager.getConfig();
				}
				else
				{
					config = Config.createDefaultConfig();
					configManager.setConfig( config );
					ConfigManager.writeConfig();
				}

				break;
			}

			case GUI_STARTUP:
			{
				assert SwingUtilities.isEventDispatchThread() : "must be on run event dispatching thread";

				// start GUI
				RecordingUI.start(ConfigManager.getConfig());
				break;
			}

			default:
				assert false : "Illegal state for Main.run(): state=" + state;
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			stateEnd(false);
			return;
		}
		stateEnd(true);
	}

	private void stateEnd(boolean success)
	{
		if (DEBUG_LEVEL >= 1)
			System.out.println("Main: state " + state + " ended.");
		if (success)
		{
			switch (state)
			{
			case INITIAL:
				stateStart( State.DATA_LOADING);
				break;
			case DATA_LOADING:
				stateStart( State.GUI_STARTUP);
				break;
			default:
				stateStart( State.FINAL);
				break;
			}
		}
		else
		{
			assert false : "Not implemented: stateEnd(false);";
		}
	}

	private void stateStart(State state)
	{
		this.state = state;
		if (DEBUG_LEVEL >= 1)
			System.out.println("Main: state " + state + " started.");
		switch (state)
		{
		case INITIAL:
			// called by constructor
			break;
		case DATA_LOADING:
			Thread t = new Thread(this);
			t.setPriority(Thread.NORM_PRIORITY);
			t.start();
			break;
		case FINAL:
			break;
		default:
			SwingUtilities.invokeLater(this);
			break;
		}
	}

	private void showCommandLineHelp()
	{
		Console console = System.console();

		PrintWriter out;
		StringWriter stringOut = null;

		if (console == null && !GraphicsEnvironment.isHeadless())
		{
			stringOut = new StringWriter();
			out = new PrintWriter(stringOut);

			ApplicationDescriptor application = Application.getInstance().getDescriptor();
			out.println(application.getTitle() + " " + application.getVersion());
		}
		else
		{
			out = new PrintWriter(System.out);
		}

		// TODO: design: standardize parsing commandline arguments
		/* To be updated with new command-line parser (see above) */
		out.println();
		out.println( "  -? --help         displays this help message" );
		out.println("  -o --disable-overrides");
		out.println("                    disables the automatic loading of overrides on startup");
		out.println();
		out.println("Additional options (via config.xml) are:");

		SortedSet<String> definedOptions = Config.getDefinedOptions();
		for (String option : definedOptions)
		{
			out.print("  ");
			out.println(option);
		}
		out.flush();

		if (stringOut != null)
		{
			JTextPane textPane = new JTextPane();
			textPane.setEditable(false);
			textPane.setText(stringOut.toString());
			textPane.setCaretPosition(0);

			Font defaultFont = textPane.getFont();
			Font monospace = new Font(Font.MONOSPACED, defaultFont.getStyle(),
			        defaultFont.getSize());
			textPane.setFont(monospace);

			Insets margin = textPane.getMargin();
			FontMetrics fontMetrics = textPane.getFontMetrics(monospace);
			int width = 80 * fontMetrics.charWidth('a') + margin.left
			        + margin.right;

			JScrollPane scroller = new JScrollPane(textPane,
			        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroller.getViewport().setPreferredSize(new Dimension(width, 200));
			JOptionPane.showMessageDialog(null, scroller);
		}
	}
}
