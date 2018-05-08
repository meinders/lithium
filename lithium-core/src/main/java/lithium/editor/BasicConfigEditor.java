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

package lithium.editor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.*;

import com.github.meinders.common.FileFilter;
import com.github.meinders.common.*;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.gui.*;

import static javax.swing.SwingConstants.*;

/**
 * An editor to change common configuration settings.
 *
 * @author G. Meinders
 */
public class BasicConfigEditor
extends JDialog
{
	/**
	 * Property fired when another configuration instance is set. This property
	 * is only used by the inner MarginsPreview class.
	 */
	protected static final String CONFIG_PROPERTY = "config";

	/** The configuration settings edited in this window. */
	protected final Config config = new Config();

	/**
	 * Actions performed on apply, e.g. setting config values, performing
	 * checks.
	 */
	protected Collection<Callable<Boolean>> applyActions;

	/** Saves any changed settings and closes the window. */
	private Action okAction;

	/** Saves any changed settings, but does not close the window. */
	private Action applyAction;

	/** Closes the the window without saving any changes. */
	private Action cancelAction;

	/**
	 * Constructs a new configuration editor for the given configuration.
	 *
	 * @param parent the parent window
	 * @param config the configuration
	 */
	public BasicConfigEditor( final Window parent, final Config config )
	{
		super( parent, Resources.get().getString( "ConfigEditor.settings" ), ModalityType.APPLICATION_MODAL );

		applyActions = new ArrayList<Callable<Boolean>>();

		setConfig( config );
		init();
	}

	public void setConfig( final Config config )
	{
		final Config oldValue = this.config.clone();
		this.config.setConfig( config );
		firePropertyChange( CONFIG_PROPERTY, oldValue, this.config );
	}

	protected Config getConfig()
	{
		return config;
	}

	private File browse( final FileFilter[] filters, final File initialDirectory )
	{
		final JFileChooser chooser = FileChoosers.createFileChooser();
		chooser.setAcceptAllFileFilterUsed( true );
		for ( final FileFilter filter : filters )
		{
			chooser.addChoosableFileFilter( filter );
		}
		if ( initialDirectory != null )
		{
			chooser.setCurrentDirectory( initialDirectory );
		}

		final int option = chooser.showOpenDialog( this );
		if ( option == JFileChooser.APPROVE_OPTION )
		{
			return chooser.getSelectedFile();
		}
		else
		{
			return null;
		}
	}

	protected void init()
	{
		setResizable( true );
		createActions();
		setContentPane( createContentPane() );
		pack();
	}

	private void apply()
	{
		boolean allowApply = true;
		for ( final Callable<Boolean> applyAction : applyActions )
		{
			try
			{
				allowApply &= applyAction.call();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
				return;
			}
			if ( !allowApply )
			{
				return;
			}
		}

		final Config oldConfig = ConfigManager.getConfig();
		if ( oldConfig.getAcceleration() != config.getAcceleration() )
		{
			final String message = Resources.get().getString(
			"configEditor.restartNeeded" );
			final String title = Resources.get().getString( "configEditor.apply" );
			JOptionPane.showMessageDialog( this, message, title,
			                               JOptionPane.INFORMATION_MESSAGE );
		}

		final ConfigManager configManager = ConfigManager.getInstance();
		configManager.setConfig( config.clone() );
		try
		{
			ConfigManager.writeConfig();
		}
		catch ( IOException ex )
		{
			// failed to save config
			final String message = Resources.get().getString( "config.writeFailed",
			                                                  ex.getLocalizedMessage() );
			final String title = Resources.get().getString( "configEditor.apply" );
			JOptionPane.showMessageDialog( this, message, title,
			                               JOptionPane.WARNING_MESSAGE );
			ex.printStackTrace();
			return;
		}
	}

	protected void createActions()
	{
		okAction = new AbstractAction( Resources.get().getString( "ok" ) )
		{
			{
				putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER, 0 ) );
			}

			public void actionPerformed( final ActionEvent e )
			{
				applyAction.actionPerformed( e );
				dispose();
			}
		};

		applyAction = new AbstractAction( Resources.get().getString( "apply" ) )
		{
			public void actionPerformed( final ActionEvent e )
			{
				setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
				apply();
				setCursor( Cursor.getDefaultCursor() );
			}
		};

		cancelAction = new AbstractAction( Resources.get().getString( "cancel" ) )
		{
			public void actionPerformed( final ActionEvent e )
			{
				dispose();
			}
		};
	}

	private JPanel createContentPane()
	{
		final JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		panel.add( createTabbedPane(), BorderLayout.CENTER );
		panel.add( createButtonPanel(), BorderLayout.SOUTH );
		return panel;
	}

	private JTabbedPane createTabbedPane()
	{
		final JTabbedPane tabbedPane = new JTabbedPane();
		// tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		// NOTE: maybe when there are more tabs... looks kinda kewl

		createTabs( tabbedPane );

		return tabbedPane;
	}

	protected void createTabs( final JTabbedPane tabbedPane )
	{
		final ResourceUtilities res = Resources.get( "configEditor" );

		final JPanel utilitiesTab = createTab( createUtilitiesTab() );
		tabbedPane.add( res.getString( "utilities" ), utilitiesTab );

		final JPanel recorderTab = createTab( createRecorderTab() );
		tabbedPane.add( res.getString( "recorder" ), recorderTab );
	}

	private JPanel createButtonPanel()
	{
		final JButton okButton = new JButton( okAction );
		final JButton cancelButton = new JButton( cancelAction );
		final JButton applyButton = new JButton( applyAction );

		// OK is default button
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				getRootPane().setDefaultButton( okButton );
			}
		} );

		// escape cancels dialog
		final InputMap inputMap = cancelButton.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
		final ActionMap actionMap = cancelButton.getActionMap();
		final Object cancelKey = cancelAction.getValue( Action.NAME );
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), cancelKey );
		actionMap.put( cancelKey, cancelAction );

		final JPanel panel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 5 ) );
		panel.add( okButton );
		panel.add( cancelButton );
		panel.add( applyButton );

		return panel;
	}

	protected JPanel createTab( final JPanel content )
	{
		final JPanel panel = new JPanel( new BorderLayout() );
		panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		panel.add( content, BorderLayout.NORTH );
		return panel;
	}

	private JPanel createUtilitiesTab()
	{
		final JLabel aboutLabel = createMultiLineLabel( Resources.get().getString(
		"configEditor.utilities.about" ), 5 );
		aboutLabel.setAlignmentX( LEFT_ALIGNMENT );

		final JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		panel.add( aboutLabel );
		panel.add( createUtilitiesFields() );
		panel.add( Box.createVerticalGlue() );
		return panel;
	}

	private JPanel createUtilitiesFields()
	{
		final JPanel panel = new JPanel();
		panel.setAlignmentX( LEFT_ALIGNMENT );
		panel.setLayout( new SpringLayout() );
		addUtilityField( panel, Config.UTILITY_PPT );
		addUtilityField( panel, Config.UTILITY_PPT_VIEWER );
		addUtilityField( panel, Config.UTILITY_MEDIA_PLAYER_CLASSIC );
		addUtilityField( panel, Config.UTILITY_VLC );
		addUtilityField( panel, Config.UTILITY_LAME );
		addUtilityField( panel, Config.UTILITY_OPEN_OFFICE );
		SpringUtilities.makeCompactGrid( panel, 6, 3, 0, 0, 5, 5 );
		return panel;
	}

	private void addUtilityField( final JPanel container, final String utility )
	{
		final JLabel label = new JLabel( Resources.get().getString( "label",
		                                                            Resources.get().getString( "utilities." + utility ) ) );

		final JTextField locationField = new JTextField( 10 );
		final File utilityFile = config.getUtility( utility );
		if ( utilityFile != null )
		{
			locationField.setText( utilityFile.toString() );
		}
		locationField.getDocument().addDocumentListener( new DocumentListener()
		{
			public void changedUpdate( final DocumentEvent e )
			{
				changed();
			}

			public void insertUpdate( final DocumentEvent e )
			{
				changed();
			}

			public void removeUpdate( final DocumentEvent e )
			{
				changed();
			}

			private void changed()
			{
				final String fileName = locationField.getText();
				if ( fileName.isEmpty() )
				{
					config.removeUtility( utility );
				}
				else
				{
					final File utilityFile = new File( fileName );
					if ( utilityFile.exists() )
					{
						config.addUtility( utility, utilityFile );
					}
					else
					{
						config.removeUtility( utility );
					}
				}
			}
		} );

		final JButton browseButton = new JButton( Resources.get().getString( "browse" ) );
		browseButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( final ActionEvent e )
			{
				final File currentFile = config.getUtility( utility );
				final File browsedFile = browse(
				FilterManager.getFilters( FilterType.UTILITY ),
				currentFile );
				if ( browsedFile != null )
				{
					locationField.setText( browsedFile.toString() );
				}
			}
		} );

		container.add( label );
		container.add( locationField );
		container.add( browseButton );
	}

	private JPanel createRecorderTab()
	{
		// JLabel aboutLabel = createMultiLineLabel(Resources.get().getString(
		// "configEditor.recorder.about"), 5);
		// aboutLabel.setAlignmentX(LEFT_ALIGNMENT);

		final JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		// panel.add(aboutLabel);
		panel.add( createRecorderFields() );
		panel.add( Box.createVerticalGlue() );
		return panel;
	}

	private JPanel createRecorderFields()
	{
		final RecorderConfig recorderConfig = config.getRecorderConfig();
		final AudioFormat audioFormat = recorderConfig.getAudioFormat();
		final RecorderConfig.EncodingFormat encodingFormat = recorderConfig.getEncodingFormat();

		final Integer[] sampleRateValues = {
		8000, 11025, 22050, 32000, 44100, 48000
		};
		final String[] scopeValues = {
		"recorder.scope.all", "recorder.scope.channel"
		};
		final String[] formatValues = {
		"recorder.format.wave", "recorder.format.mp3"
		};
		final String[] modeValues = {
		"recorder.mode.stereo",
		"recorder.mode.jointStereo", "recorder.mode.mono"
		};
		final String[] bitRateTypeValues = {
		"recorder.bitRateType.constant",
		"recorder.bitRateType.average", "recorder.bitRateType.variable"
		};

		final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		final String[] mixerValues = new String[ mixerInfo.length ];
		for ( int i = 0; i < mixerValues.length; i++ )
		{
			mixerValues[ i ] = mixerInfo[ i ].getName();
		}

		Mp3Options.Mode mode = null;
		String bitRateType = bitRateTypeValues[ 0 ];
		Integer bitRate = null;
		Integer quality = null;

		if ( encodingFormat instanceof RecorderConfig.EncodingFormat.MP3 )
		{
			final Mp3Options options = ( (RecorderConfig.EncodingFormat.MP3)encodingFormat ).getOptions();
			mode = options.getMode();
			final Mp3Options.BitRate bitRateObject = options.getBitRate();
			if ( bitRateObject instanceof Mp3Options.ConstantBitRate )
			{
				bitRateType = "recorder.bitRateType.constant";
				bitRate = ( (Mp3Options.ConstantBitRate)bitRateObject ).getBitRate();
			}
			else if ( bitRateObject instanceof Mp3Options.AverageBitRate )
			{
				bitRateType = "recorder.bitRateType.average";
				bitRate = ( (Mp3Options.AverageBitRate)bitRateObject ).getBitRate();
			}
			else if ( bitRateObject instanceof Mp3Options.VariableBitRate )
			{
				bitRateType = "recorder.bitRateType.variable";
				quality = ( (Mp3Options.VariableBitRate)bitRateObject ).getQuality();
			}
		}

		final String scopeValue = String.valueOf( recorderConfig.isNormalizePerChannel() ? scopeValues[ 1 ]
		                                                                                 : scopeValues[ 0 ] );
		final String formatValue = ( encodingFormat instanceof RecorderConfig.EncodingFormat.MP3 ) ? formatValues[ 1 ]
		                                                                                           : formatValues[ 0 ];
		final String modeValue = ( mode == null ) ? modeValues[ 0 ]
		                                          : modeValues[ mode.ordinal() ];

		final JForm form = new JForm();
		form.setAlignmentX( LEFT_ALIGNMENT );

		final JComboBox mixerField = createField( recorderConfig.getMixerName(),
		                                          mixerValues, false );
		final JTextField channelsField = createField( audioFormat.getChannels() );
		final JTextField bitsPerSampleField = createField( audioFormat.getSampleSizeInBits() );
		final JComboBox sampleRateField = createField(
		(int)audioFormat.getFrameRate(), sampleRateValues,
		"recorder.sampleRate.format" );

		final JCheckBox normalizeField = createField( recorderConfig.isNormalize() );
		final JTextField maximumGainField = createField( recorderConfig.getMaximumGain() );
		final JTextField windowSizeField = createField( recorderConfig.getWindowSize() );
		final JComboBox scopeField = createField( scopeValue, scopeValues );

		final JCheckBox encodeField = createField( encodingFormat != null );
		final JComboBox formatField = createField( formatValue, formatValues );
		final JComboBox modeField = createField( modeValue, modeValues );
		final JComboBox bitRateTypeField = createField( bitRateType,
		                                                bitRateTypeValues );
		final JTextField bitRateField = createField( bitRate );
		final JSlider qualityField = createField( quality, 4, 0,
		                                          "recorder.quality.best", 9, "recorder.quality.worst" );

		final JTextField storageFolderField = createField( recorderConfig.getStorageFolder().toString() );
		final JTextField namingSchemeField = createNamingSchemeField( recorderConfig.getNamingScheme() );

		form.addField( "recorder.mixer", mixerField );
		form.addField( "recorder.channels", channelsField );
		form.addField( "recorder.bitsPerSample", bitsPerSampleField );
		form.addField( "recorder.sampleRate", sampleRateField );
		form.addSeparator();
		form.addField( "recorder.normalize", normalizeField );
		form.addField( "recorder.maximumGain", maximumGainField );
		form.addField( "recorder.windowSize", windowSizeField );
		form.addField( "recorder.scope", scopeField );
		form.addSeparator();
		form.addField( "recorder.encode", encodeField );
		form.addField( "recorder.format", formatField );
		form.addField( "recorder.mode", modeField );
		form.addField( "recorder.bitRateType", bitRateTypeField );
		form.addField( "recorder.bitRate", bitRateField );
		form.addField( "recorder.quality", qualityField );
		form.addSeparator();
		form.addField( "recorder.storageFolder",
		               createBrowsableFolderField( storageFolderField ) );
		form.addField( "recorder.namingScheme", namingSchemeField );

		final ChangeListener normalizeChangeListener = new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final boolean normalize = normalizeField.isSelected();

				form.setEnabled( maximumGainField, normalize );
				form.setEnabled( windowSizeField, normalize );
				form.setEnabled( scopeField, normalize );
			}
		};

		normalizeField.addChangeListener( normalizeChangeListener );
		normalizeChangeListener.stateChanged( null );

		final ChangeListener encodeChangeListener = new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final boolean encode = encodeField.isSelected();
				final boolean mp3 = formatField.getSelectedIndex() == 1;
				final boolean vbr = bitRateTypeField.getSelectedIndex() == 2;

				form.setEnabled( formatField, encode );
				form.setEnabled( modeField, encode && mp3 );
				form.setEnabled( bitRateTypeField, encode && mp3 );
				form.setEnabled( bitRateField, encode && mp3 && !vbr );
				form.setEnabled( qualityField, encode && mp3 && vbr );
			}
		};
		final ItemListener encodeItemListener = new ItemListener()
		{
			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				encodeChangeListener.stateChanged( null );
			}
		};

		encodeField.addChangeListener( encodeChangeListener );
		formatField.addItemListener( encodeItemListener );
		bitRateTypeField.addItemListener( encodeItemListener );
		encodeChangeListener.stateChanged( null );

		applyActions.add( new Callable<Boolean>()
		{
			@Override
			public Boolean call()
			throws Exception
			{
				final String mixer = (String)mixerField.getSelectedItem();

				final int channels = Integer.parseInt( channelsField.getText() );
				final int sampleRate = Integer.parseInt( sampleRateField.getSelectedItem().toString() );
				final int bitsPerSample = Integer.parseInt( bitsPerSampleField.getText() );
				final boolean bigEndian = false;

				final AudioFormat audioFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, sampleRate,
				bitsPerSample, channels, channels * bitsPerSample / 8,
				sampleRate, bigEndian );

				final RecorderConfig.EncodingFormat encodingFormat;
				if ( encodeField.isSelected() )
				{
					switch ( formatField.getSelectedIndex() )
					{
						default:
						case 0:
							encodingFormat = new RecorderConfig.EncodingFormat.Wave();
							break;

						case 1:
							final RecorderConfig.EncodingFormat.MP3 mp3Format = new RecorderConfig.EncodingFormat.MP3();
							encodingFormat = mp3Format;

							final Mp3Options options = mp3Format.getOptions();
							switch ( modeField.getSelectedIndex() )
							{
								default:
								case 0:
									options.setMode( Mp3Options.Mode.STEREO );
									break;
								case 1:
									options.setMode( Mp3Options.Mode.JOINT_STEREO );
									break;
								case 2:
									options.setMode( Mp3Options.Mode.MONO );
									break;
							}

							switch ( bitRateTypeField.getSelectedIndex() )
							{
								default:
								case 0:
								{
									Integer bitRate;
									try
									{
										bitRate = Integer.valueOf( bitRateField.getText() );
									}
									catch ( NumberFormatException e )
									{
										bitRate = null;
									}
									options.setBitRate( new Mp3Options.ConstantBitRate(
									bitRate ) );
									break;
								}

								case 1:
								{
									final int bitRate = Integer.parseInt( bitRateField.getText() );
									options.setBitRate( new Mp3Options.AverageBitRate(
									bitRate ) );
									break;
								}

								case 2:
								{
									final int quality = qualityField.getValue();
									options.setBitRate( new Mp3Options.VariableBitRate(
									quality ) );
									break;
								}
							}
							break;
					}
				}
				else
				{
					encodingFormat = null;
				}

				final RecorderConfig.NamingScheme namingScheme = new RecorderConfig.NamingScheme();
				namingScheme.setFormat( namingSchemeField.getText() );

				final Config config = getConfig();
				final RecorderConfig recorderConfig = config.getRecorderConfig();
				recorderConfig.setMixerName( mixer );
				recorderConfig.setAudioFormat( audioFormat );
				recorderConfig.setNormalize( normalizeField.isSelected() );
				recorderConfig.setMaximumGain( Double.parseDouble( maximumGainField.getText() ) );
				recorderConfig.setWindowSize( Double.parseDouble( windowSizeField.getText() ) );
				recorderConfig.setNormalizePerChannel( scopeField.getSelectedIndex() == 1 );
				recorderConfig.setEncodingFormat( encodingFormat );
				recorderConfig.setStorageFolder( new File(
				storageFolderField.getText() ) );
				recorderConfig.setNamingScheme( namingScheme );
				return true;
			}
		} );

		return form;
	}

	private JTextField createNamingSchemeField( final RecorderConfig.NamingScheme namingScheme )
	{
		return createField( namingScheme.getFormat() );
	}

	protected JComponent createBrowsableFolderField( final JTextField field )
	{
		final JButton browseButton = new JButton( new AbstractAction( Resources.get().getString( "browse" ) )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final String currentValue = field.getText();
				final File file = currentValue.isEmpty() ? null : new File( currentValue );
				final DirectoryChooser chooser = new DirectoryChooser( file );
				if ( chooser.showDialog( BasicConfigEditor.this ) )
				{
					final File selectedDirectory = chooser.getSelectedDirectory();
					field.setText( selectedDirectory == null ? "" : selectedDirectory.toString() );
				}
			}
		} );
		field.setEditable( false );

		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout( panel );
		layout.setAutoCreateContainerGaps( false );
		layout.setAutoCreateGaps( true );
		panel.setLayout( layout );

		final GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup();
		final GroupLayout.ParallelGroup vertical = layout.createBaselineGroup( true, true );

		horizontal.addComponent( field );
		horizontal.addComponent( browseButton );
		vertical.addComponent( field );
		vertical.addComponent( browseButton );

		layout.setHorizontalGroup( horizontal );
		layout.setVerticalGroup( vertical );

		return panel;
	}

	private JTextField createField( final String value )
	{
		final JTextField field = new JTextField( 10 );
		if ( value != null )
		{
			field.setText( value );
		}
		return field;
	}

	private JSlider createField( final Integer value, final int defaultValue, final int minimum, final String minimumLabel, final int maximum, final String maximumLabel )
	{
		final JSlider field = new JSlider( minimum, maximum, ( value == null ) ? defaultValue : value );
		field.setSnapToTicks( true );
		field.setPaintLabels( true );
		field.setPaintTicks( true );
		field.setMajorTickSpacing( 1 );

		final JLabel minimumLabelComponent = new JLabel( Resources.get().getString( minimumLabel ) );
		final JLabel maximumLabelComponent = new JLabel( Resources.get().getString( maximumLabel ) );

		final Dictionary<Integer, Component> labelTable = new Hashtable<Integer, Component>();
		labelTable.put( minimum, minimumLabelComponent );
		labelTable.put( maximum, maximumLabelComponent );
		field.setLabelTable( labelTable );

		return field;
	}

	private JTextField createField( final int value )
	{
		return createField( String.valueOf( value ) );
	}

	private JTextField createField( final Integer value )
	{
		return createField( ( value == null ) ? "" : String.valueOf( value ) );
	}

	private JTextField createField( final double value )
	{
		return createField( String.valueOf( value ) );
	}

	private JCheckBox createField( final boolean value )
	{
		final JCheckBox field = new JCheckBox();
		field.setSelected( value );
		return field;
	}

	private JComboBox createField( final String value, final String[] options )
	{
		return createField( value, options, true );
	}

	private JComboBox createField( final String value, final String[] options,
	                               final boolean localized )
	{
		final JComboBox comboBox = new JComboBox( options );
		comboBox.setSelectedItem( value );
		if ( localized )
		{
			comboBox.setRenderer( new DefaultListCellRenderer()
			{
				@Override
				public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
				{
					final String displayValue = Resources.get().getString( String.valueOf( value ) );
					return super.getListCellRendererComponent( list, displayValue, index, isSelected, cellHasFocus );
				}
			} );
		}

		return comboBox;
	}

	private JComboBox createField( final Integer value, final Integer[] options,
	                               final String format )
	{
		final JComboBox comboBox = new JComboBox( options );
		comboBox.setEditable( true );
		comboBox.setSelectedItem( value );
		comboBox.setEditable( false );
		comboBox.setRenderer( new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
			{
				final String displayValue = Resources.get().getString( format, value );
				return super.getListCellRendererComponent( list, displayValue, index, isSelected, cellHasFocus );
			}
		} );

		return comboBox;
	}

	protected JLabel createMultiLineLabel( final String text, final int lines )
	{
		final JLabel label = new JLabel( "<html>" + text + "</html>" );
		label.setHorizontalAlignment( LEFT );
		label.setVerticalAlignment( TOP );
		final Dimension preferredSize = label.getPreferredSize();
		preferredSize.width = 0;
		final FontMetrics metrics = label.getFontMetrics( label.getFont() );
		preferredSize.height = lines * metrics.getHeight();
		label.setPreferredSize( preferredSize );
		return label;
	}

	private static class JForm
	extends JPanel
	{
		private GroupLayout.ParallelGroup labels;

		private GroupLayout.ParallelGroup fields;

		private GroupLayout.SequentialGroup rows;

		public JForm()
		{
			final GroupLayout layout = new GroupLayout( this );
			layout.setAutoCreateContainerGaps( true );
			layout.setAutoCreateGaps( true );
			setLayout( layout );

			final GroupLayout.SequentialGroup columns = layout.createSequentialGroup();
			layout.setHorizontalGroup( columns );

			labels = layout.createParallelGroup();
			fields = layout.createParallelGroup();
			columns.addGroup( labels );
			columns.addGroup( fields );

			rows = layout.createSequentialGroup();
			layout.setVerticalGroup( rows );
		}

		public void setEnabled( final JComponent field, final boolean enabled )
		{
			field.setEnabled( enabled );

			final int index = getComponentZOrder( field );
			final Component label = getComponent( index - 1 );
			if ( label != null )
			{
				label.setEnabled( enabled );
			}
		}

		public void addField( final String name, final JComponent field )
		{
			final JLabel label = new JLabel( Resources.get().getString( "label",
			                                                            Resources.get().getString( name ) ) );

			final GroupLayout layout = (GroupLayout)getLayout();
			final GroupLayout.ParallelGroup row = layout.createBaselineGroup( true, true );
			rows.addGroup( row );

			row.addComponent( label, GroupLayout.Alignment.CENTER );
			row.addComponent( field );
			labels.addComponent( label );
			fields.addComponent( field );
		}

		public void addSeparator()
		{
			rows.addGap( 15 );
		}
	}
}
