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

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;
import javax.swing.*;

import com.github.meinders.common.util.*;
import lithium.*;
import lithium.RecorderConfig.*;

/**
 * Provides a simple interface for recording audio.
 *
 * @author Gerrit Meinders
 */
public class Recorder
{
	private Config config;

	private RecorderConfig recorderConfig;

	private TargetDataLineInputStream recordIn;

	private ExecutorService threadPool;

	private Collection<AmplitudeListener> amplitudeListeners;

	private Collection<GainListener> gainListeners;

	public Recorder(Config config)
	{
		this(config, config.getRecorderConfig());
	}

	public Recorder(Config config, RecorderConfig recorderConfig)
	{
		this.config = config;
		this.recorderConfig = recorderConfig;

		amplitudeListeners = new ArrayList<AmplitudeListener>();
		gainListeners = new ArrayList<GainListener>();
	}

	public void setConfig(Config config)
	{
		this.config = config;
		this.recorderConfig = config.getRecorderConfig();
	}

	public void start() throws LineUnavailableException, IOException
	{
		AudioFormat audioFormat = recorderConfig.getAudioFormat();
		EncodingFormat encodingFormat = recorderConfig.getEncodingFormat();

		List<InputStream> inputs = new ArrayList<InputStream>();
		List<OutputStream> outputs = new ArrayList<OutputStream>();

		/*
		 * Storage
		 */
		File storageFolder = recorderConfig.getStorageFolder();
		NamingScheme namingScheme = recorderConfig.getNamingScheme();
		File file = namingScheme.getFile(storageFolder, encodingFormat);
		System.out.println("Writing output to " + file);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

		/*
		 * Recording
		 */
		Mixer.Info mixer = getMixer(recorderConfig.getMixerName());
		TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(
		        audioFormat, mixer);
		System.out.println("Recording from " + targetDataLine);
		System.out.println("Audio format: " + audioFormat);
		System.out.println("Buffer size: " + targetDataLine.getBufferSize()
		        + " bytes");
		recordIn = new TargetDataLineInputStream(targetDataLine, audioFormat);
		inputs.add(new BufferedInputStream(recordIn));

		/*
		 * Encoding
		 */
		if (encodingFormat != null)
		{
			if (encodingFormat instanceof EncodingFormat.MP3)
			{
				System.out.println("Encoding as MP3");
				EncodingFormat.MP3 mp3Format = (EncodingFormat.MP3) encodingFormat;

				File executable = config.getUtility(Config.UTILITY_LAME);
				if (executable == null)
				{
					// throw new RecordingException(new MissingUtilityException(
					// Config.UTILITY_LAME));
				}

				LameOutputStream lameOut = new LameOutputStream(out,
				        mp3Format.getOptions(), executable);

				out = new WaveOutputStream(lameOut, audioFormat);

			}
			else if (encodingFormat instanceof EncodingFormat.Wave)
			{
				System.out.println("Encoding as Wave");
				out = new WaveOutputStream(out, audioFormat);
			}
		}

		/*
		 * Normalization
		 */
		MonitorAudioOutputStream monitor;
		if (recorderConfig.isNormalize())
		{
			System.out.println("Normalizing enabled");
			NormalizingOutputStream normalizeOut = new NormalizingOutputStream(
			        out, audioFormat, recorderConfig.getWindowSize(),
			        recorderConfig.getMaximumGain(),
			        recorderConfig.isNormalizePerChannel());

			if (config.isEnabled(Config.DEBUG_DISABLE_DC_OFFSET))
			{
				normalizeOut.setDCOffsetEnabled(false);
			}

			out = normalizeOut;
			monitor = normalizeOut;

			normalizeOut.addGainListener(new GainListener()
			{
				public void gainChanged(int channel, double gain)
				{
					fireGainChange(channel, gain);
				}
			});

		}
		else
		{
			monitor = new MonitorAudioOutputStream(out, audioFormat);
			out = monitor;
		}

		/*
		 * Monitor volume and forward volume events.
		 */
		monitor.addAmplitudeListener(new AmplitudeListener()
		{
			public void amplitudeChanged(int channel, double amplitude)
			{
				fireAmplitudeChange(channel, amplitude);
			}
		});

		/*
		 * Connect inputs to outputs with pipes.
		 */
		outputs.add(out);

		if (inputs.size() != outputs.size())
		{
			throw new AssertionError("inputs.size() != outputs.size(): "
			        + inputs.size() + " != " + outputs.size());
		}

		threadPool = Executors.newFixedThreadPool(inputs.size());

		for (int i = 0; i < inputs.size(); i++)
		{
			Pipe pipe = new Pipe(inputs.get(i), outputs.get(i));
			threadPool.execute(pipe);
		}

		recordIn.start();
	}

	private Mixer.Info getMixer(String mixerName)
	{
		Mixer.Info mixer = null;
		if (mixerName != null)
		{
			for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
			{
				if (mixerName.equals(mixerInfo.getName()))
				{
					mixer = mixerInfo;
				}
			}
		}
		return mixer;
	}

	public boolean isStarted()
	{
		return (recordIn != null) && recordIn.isStarted();
	}

	public void stop() throws IOException
	{
		if (recordIn != null)
		{
			System.out.println("Stop");
			recordIn.stop();
			System.out.println("Drain");
			recordIn.drain();
			System.out.println("Close");
			recordIn.close();
		}
		if (threadPool != null)
		{
			System.out.println("Shutdown");
			threadPool.shutdown();
		}
		System.out.println("Recorder stopped.");
	}

	public void addAmplitudeListener(AmplitudeListener volumeListener)
	{
		amplitudeListeners.add(volumeListener);
	}

	public void removeAmplitudeListener(AmplitudeListener volumeListener)
	{
		amplitudeListeners.remove(volumeListener);
	}

	private void fireAmplitudeChange(final int channel, final double amplitude)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for (AmplitudeListener listener : amplitudeListeners)
				{
					listener.amplitudeChanged(channel, amplitude);
				}
			}
		});
	}

	public void addGainListener(GainListener gainListener)
	{
		gainListeners.add(gainListener);
	}

	public void removeGainListener(GainListener gainListener)
	{
		gainListeners.remove(gainListener);
	}

	private void fireGainChange(final int channel, final double gain)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for (GainListener listener : gainListeners)
				{
					listener.gainChanged(channel, gain);
				}
			}
		});
	}
}
