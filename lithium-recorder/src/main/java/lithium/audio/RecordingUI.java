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

package lithium.audio;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import javax.swing.*;

import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.editor.*;

//import lithium.remote.server.*;

public class RecordingUI extends JFrame
{
	public static void start(Config config)
	{
		RecordingUI recordingUI = new RecordingUI();
		recordingUI.setVisible(true);

		// TODO: Resolve dependencies to support lithium.remote.
/*
		final Server remoteServer = new Server(7171,
		        new RemoteConnectionHandlerFactory(null,
		                recordingUI.getRecorder()));
		final Thread remoteServerThread = new Thread(remoteServer);
		remoteServerThread.start();

		recordingUI.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				remoteServer.close();
				remoteServerThread.interrupt();
			}
		});
*/
	}

	private Recorder recorder;

	public RecordingUI()
	{
		super(Resources.get().getString("recorder.title"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setContentPane(createContentPane());

		pack();
		GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();
		Rectangle bounds = graphicsConfiguration.getBounds();
		setLocation((int) bounds.getCenterX() - getWidth() / 2,
		        (int) bounds.getCenterY() - getHeight() / 2);
	}

	private Recorder getRecorder()
	{
		return recorder;
	}

	private Container createContentPane()
	{
		Config config = ConfigManager.getConfig();
		recorder = new Recorder(config);

		final JVolumeBar volumeBar = new JVolumeBar();
		volumeBar.setOrientation(JVolumeBar.Orientation.HORIZONTAL);
		volumeBar.setAlignmentX(CENTER_ALIGNMENT);
		volumeBar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		final JLabel timeLabel = new JLabel("-:--'--\"");
		timeLabel.setAlignmentX(CENTER_ALIGNMENT);
		timeLabel.setBorder(BorderFactory.createEmptyBorder());
		int fontSize = 8 * getToolkit().getScreenResolution() / 72;
		timeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));

		class Listener implements AmplitudeListener, GainListener
		{
			private DecimalFormat twoDigitFormat = new DecimalFormat("00");

			private long startTimeMillis;

			@Override
			public void amplitudeChanged(int channel, double amplitude)
			{
				if (recorder.isStarted())
				{
					volumeBar.setAmplitude(channel, amplitude);

					int seconds = (int) ((System.currentTimeMillis() - startTimeMillis) / 1000);
					int minutes = seconds / 60;
					int hours = minutes / 60;
					minutes %= 60;
					seconds %= 60;
					timeLabel.setText(hours + ":"
					        + twoDigitFormat.format(minutes) + "'"
					        + twoDigitFormat.format(seconds) + '"');
				}
			}

			@Override
			public void gainChanged(int channel, double gain)
			{
				volumeBar.setGain(channel, gain);
			}
		}

		final Listener listener = new Listener();
		recorder.addAmplitudeListener(listener);
		recorder.addGainListener(listener);

		final Action[] recordStopActions = new Action[2];

		Action recordAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setEnabled(false);
				recorder.setConfig(ConfigManager.getConfig());
				try
				{
					recorder.start();

					recordStopActions[1].setEnabled(true);
					listener.startTimeMillis = System.currentTimeMillis();

				}
				catch (Exception ex)
				{
					try
					{
						recorder.stop();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					showExceptionDialog(Resources.get().getString(
					        "recorder.exception.record"), ex);
					setEnabled(true);
				}
			}
		};
		recordAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon( "toolbarButtonGraphics/custom/Record24.gif" ));
		recordStopActions[0] = recordAction;

		Action stopAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setEnabled(false);
				try
				{
					recorder.stop();

					volumeBar.clear();
					timeLabel.setText("-:--'--\"");
					recordStopActions[0].setEnabled(true);

				}
				catch (Exception ex)
				{
					showExceptionDialog(Resources.get().getString(
					        "recorder.exception.stop"), ex);
					setEnabled(true);
				}
			}
		};
		stopAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon( "toolbarButtonGraphics/media/Stop24.gif" ));
		stopAction.setEnabled(false);
		recordStopActions[1] = stopAction;

		// TODO: Resolve dependencies to allow access to ConfigEditor.
		Action preferencesAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Config config = ConfigManager.getConfig();
				BasicConfigEditor configEditor = new BasicConfigEditor(RecordingUI.this, config);

				SwingUtilities2.centerOnParent( configEditor );
				configEditor.setVisible(true);

				recorder.setConfig(ConfigManager.getConfig());
			}
		};
		preferencesAction.putValue( Action.MNEMONIC_KEY, Resources.get().getMnemonic( "settings" ) );
		preferencesAction.putValue( Action.SMALL_ICON, getIcon( "toolbarButtonGraphics/general/Preferences16.gif" ) );
		preferencesAction.putValue( Action.LARGE_ICON_KEY, getIcon( "toolbarButtonGraphics/general/Preferences24.gif" ) );
		preferencesAction.putValue( Action.SHORT_DESCRIPTION, Resources.get().getString( "settings.tooltip" ) );

		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentX(LEFT_ALIGNMENT);
		toolBar.setFloatable(false);
		toolBar.setOrientation(SwingConstants.HORIZONTAL);
		toolBar.add(preferencesAction);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(timeLabel);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(recordAction);
		toolBar.add(stopAction);
		toolBar.setBorder(BorderFactory.createEmptyBorder());

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.LINE_AXIS));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.add(volumeBar);
		volumeBar.setPreferredSize(new Dimension(200, 50));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(toolBar, BorderLayout.PAGE_END);
		panel.add(centerPanel, BorderLayout.CENTER);
		return panel;
	}

	private void showExceptionDialog(String message, Exception e)
	{
		showExceptionDialog(this, message, e);
	}

	private void showExceptionDialog(Component parent, String message,
	        Exception e)
	{
		if (e == null)
		{
			JOptionPane.showMessageDialog(parent, message, getTitle(),
			        JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, Resources.get().getString(
			        "editorFrame.exception", message, e.getLocalizedMessage()),
			        e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
		}
	}

	private ImageIcon getIcon(String name)
	{
		return new ImageIcon(getClass().getClassLoader().getResource(name));
	}
}
