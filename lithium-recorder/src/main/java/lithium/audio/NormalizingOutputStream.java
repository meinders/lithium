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
import javax.sound.sampled.*;
import javax.swing.*;

import com.github.meinders.common.util.*;
import lithium.*;

/**
 * Performs on-the-fly normalization on audio samples written to the stream. The
 * normalized samples are written to the underlying output stream in the same
 * format.
 *
 * @author Gerrit Meinders
 */
public class NormalizingOutputStream extends MonitorAudioOutputStream
{
	/**
	 * Rolling window of samples that have been read but have not been
	 * normalized and written yet.
	 */
	private RollingWindow window;

	/**
	 * Provides the maximum amplitude in the rolling window for each channel.
	 */
	private RollingMaximum[] rollingMaximums;

	/**
	 * Specifies the gain applied to each channel.
	 */
	private Gain[] gains;

	/**
	 * The index of the current channel.
	 */
	private int channel;

	/**
	 * Registered amplitude listeners.
	 */
	private Collection<GainListener> gainListeners;

	/**
	 * Indicates whether DC offset in the written data should be corrected.
	 */
	private boolean dcOffsetEnabled;

	/**
	 * Approximates the constant offset of the signal for each channel.
	 */
	private double[] dcOffsets;

	/**
	 * The rate at which {@link #dcOffsets} change to match processed samples.
	 */
	private double dcOffsetFactor;

	/**
	 * Constructs a new normalizing output stream for data in the specified
	 * audio format.
	 *
	 * @param out Output stream to write normalized data to.
	 * @param format Audio format of the written data.
	 * @param windowSize Duration of the window used for normalization, in
	 *            seconds.
	 * @param maxGain Maximum gain to multiply input samples with.
	 * @param normalizingPerChannel Whether channels are normalized
	 *            individually, or all together.
	 *
	 * @throws IllegalArgumentException if the specified format isn't supported.
	 */
	public NormalizingOutputStream(OutputStream out, AudioFormat format,
	        double windowSize, double maxGain, boolean normalizingPerChannel)
	{
		super(out, format);
		gainListeners = new ArrayList<GainListener>();

		int channels = format.getChannels();
		int samplesPerWindow = (int) (format.getSampleRate() * channels * windowSize);
		window = new RollingWindow(samplesPerWindow);
		channel = 0;

		int samplesPerWindowPerChannel;
		if (normalizingPerChannel)
		{
			samplesPerWindowPerChannel = samplesPerWindow / channels;
			rollingMaximums = new RollingMaximum[channels];
		}
		else
		{
			/*
			 * Normalize the stream as if all samples belong to the same
			 * channel.
			 */
			samplesPerWindowPerChannel = samplesPerWindow;
			rollingMaximums = new RollingMaximum[1];
		}

		gains = new Gain[rollingMaximums.length];
		for (int i = 0; i < rollingMaximums.length; i++)
		{
			rollingMaximums[i] = new RollingMaximum(samplesPerWindowPerChannel);
			gains[i] = new Gain(sampleFormat, samplesPerWindowPerChannel,
			        maxGain);
		}

		dcOffsetEnabled = true;
		dcOffsets = new double[channels];
		dcOffsetFactor = 1.0 / format.getSampleRate();
	}

	/**
	 * Returns whether DC offset in the written data should be corrected.
	 *
	 * @return Whether DC offset correction is enabled.
	 */
	public boolean isDCOffsetEnabled()
	{
		return dcOffsetEnabled;
	}

	/**
	 * Sets whether DC offset in the written data should be corrected.
	 *
	 * @param dcOffsetEnabled Whether DC offset correction should be enabled.
	 */
	public void setDCOffsetEnabled(boolean dcOffsetEnabled)
	{
		this.dcOffsetEnabled = dcOffsetEnabled;
	}

	/**
	 * Returns the current DC offset for the given channel.
	 *
	 * @param channel Channel to get the offset for.
	 *
	 * @return DC offset.
	 */
	double getDCOffset(int channel)
	{
		return dcOffsets[channel];
	}

	@Override
	protected void writeSample(int sourceChannel, int sample)
	        throws IOException
	{
		/*
		 * Update gain level for this channel.
		 */
		RollingMaximum rollingMaximum = rollingMaximums[channel];
		Gain gain = gains[channel];
		gain.update(rollingMaximum);

		/*
		 * Adjust sample for DC offset.
		 */
		int adjustedSample;
		if (dcOffsetEnabled)
		{
			dcOffsets[sourceChannel] = dcOffsets[sourceChannel]
			        * (1.0 - dcOffsetFactor) + sample * dcOffsetFactor;
			adjustedSample = sample - (int) dcOffsets[sourceChannel];
		}
		else
		{
			adjustedSample = sample;
		}

		/*
		 * Update the rolling window, writing the sample that is removed from
		 * the window as output.
		 */
		boolean windowFull = window.isFull();
		int output = window.add(adjustedSample);
		if (windowFull)
		{
			writeNormalizedSample(output);
		}

		/*
		 * Allow for volume events to be fired.
		 */
		monitorSample(adjustedSample);

		/*
		 * Update the rolling maximum for this channel.
		 */
		rollingMaximum.remove(Math.abs(output));
		rollingMaximum.add(Math.abs(adjustedSample));

		/*
		 * Samples for different channels are interleaved.
		 */
		nextChannel();
	}

	@Override
	protected void monitorSampleOnWrite(int sample)
	{
		// Don't monitor on write. Would cause delays due to window size.
	}

	/**
	 * Applies the current gain to the given sample and writes it to the
	 * underlying output stream.
	 *
	 * @param sample Sample to be written with the current gain applied.
	 */
	private void writeNormalizedSample(int sample) throws IOException
	{
		Gain gain = gains[channel];
		int amplified = (int) (sample * gain.get());

		int clamped = sampleFormat.clamp(amplified);
		if (amplified != clamped)
		{
			Log log = Log.getLog();
			log.writeEntry(this, "Sample clamped after normalization");
			writeState(log);
		}

		super.writeSample(channel, clamped);
	}

	@Override
	protected void writeState(Log log)
	{
		super.writeState(log);

		log.writeDetails("window.window", window.getWindow());
		log.writeDetails("window.capacity", window.getCapacity());
		log.writeDetails("window.offset", window.getOffset());

		for (int i = 0; i < rollingMaximums.length; i++)
		{
			log.writeDetails("rollingMaximums[" + i + "].buffer",
			        rollingMaximums[i].getBuffer());
			log.writeDetails("rollingMaximums[" + i + "].minimumIndex",
			        rollingMaximums[i].getMinimumIndex());
			log.writeDetails("rollingMaximums[" + i + "].maximumIndex",
			        rollingMaximums[i].getMaximumIndex());
		}

		for (int i = 0; i < gains.length; i++)
		{
			log.writeDetails("gains[" + i + "].gain", gains[i].gain);
			log.writeDetails("gains[" + i + "].maxGain", gains[i].maxGain);
			log.writeDetails("gains[" + i + "].maxGainIncrease",
			        gains[i].maxGainIncrease);
		}

		log.writeDetails("channel", channel);

		log.writeDetails("dcOffsetEnabled", dcOffsetEnabled);
		if (dcOffsetEnabled)
		{
			log.writeDetails("dcOffsetFactor", dcOffsetFactor);
			log.writeDetails("dcOffsets", dcOffsets);
		}
	}

	private void nextChannel()
	{
		channel++;
		channel %= rollingMaximums.length;
	}

	@Override
	public void flush() throws IOException
	{
		while (!window.isEmpty())
		{
			RollingMaximum rollingMaximum = rollingMaximums[channel];

			int flushed = window.remove();
			rollingMaximum.remove(Math.abs(flushed));
			writeNormalizedSample(flushed);

			nextChannel();
		}
		super.flush();
	}

	@Override
	protected void fireEvents(int sourceChannel, int sample)
	{
		fireGainChange(sourceChannel, gains[channel].get());
		super.fireEvents(sourceChannel, sample);
	}

	public void addGainListener(GainListener gainListener)
	{
		gainListeners.add(gainListener);
	}

	public void removeGainListener(GainListener gainListener)
	{
		gainListeners.remove(gainListener);
	}

	protected void fireGainChange(final int channel, final double gain)
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

	/**
	 * Specifies the gain (i.e. amplification) to be applied to audio samples.
	 * The gain level may be updated based on the maximum amplitude of an audio
	 * stream.
	 *
		 * @author Gerrit Meinders
	 */
	private static class Gain
	{
		private SampleFormat sampleFormat;

		private double gain;

		private double maxGain;

		private double maxGainIncrease;

		/**
		 * Constructs a new gain instance.
		 *
		 * @param sampleFormat The sample format of the audio stream.
		 * @param windowSize The window size for smooth gain changes.
		 * @param maxGain The maximum gain level.
		 */
		public Gain(SampleFormat sampleFormat, int windowSize, double maxGain)
		{
			this.sampleFormat = sampleFormat;
			this.maxGain = maxGain;
			gain = 1.0;
			maxGainIncrease = 1.0 + 1.0 / windowSize;
		}

		/**
		 * Returns the current gain level.
		 *
		 * @return The gain level.
		 */
		public double get()
		{
			return gain;
		}

		/**
		 * Updates the gain level according to the given maximum amplitude.
		 *
		 * @param rollingMaximum Specifies the maximum amplitude.
		 */
		public void update(RollingMaximum rollingMaximum)
		{
			int maximumAmplitude = sampleFormat.getMaximumAmplitude();

			// TODO: Gain below 1.0?! But it does happen.
			// See unit test: testExtremeVolumeChanges
			double clipGain = (double) maximumAmplitude / rollingMaximum.get();
			double limitedGain = Math.min(maxGain, clipGain);

			if (limitedGain > gain)
			{
				gain = Math.min(gain * maxGainIncrease, limitedGain);
			}
			else
			{
				/*
				 * TODO: Instead of using maxGain as a reference, add a rolling
				 * window to provide the actual gain one window size earlier.
				 */
				gain = Math.max(gain - (maxGain - limitedGain)
				        / rollingMaximum.getWindowSize(), limitedGain);
			}
		}
	}
}
