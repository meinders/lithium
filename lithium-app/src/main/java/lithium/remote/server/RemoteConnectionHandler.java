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

package lithium.remote.server;

import java.awt.*;
import java.awt.font.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import lithium.*;
import lithium.animation.*;
import lithium.animation.legacy.scrolling.*;
import lithium.audio.*;
import lithium.display.*;
import lithium.remote.*;
import lithium.text.*;

public class RemoteConnectionHandler implements ConnectionHandler,
        PropertyChangeListener, AmplitudeListener
{
	private final Socket socket;

	private final ViewModel viewModel;

	private DataOutputStream out;

	private DataInputStream in;

	private final Recorder recorder;

	private byte[] recorderLevels = new byte[2];

	private long lastAmplitudeChanged = 0;

	public RemoteConnectionHandler(Socket socket, ViewModel viewModel,
	        Recorder recorder)
	{
		super();
		this.socket = socket;
		this.viewModel = viewModel;
		this.recorder = recorder;
	}

	private void handleMessage(Message message) throws IOException
	{
		if (message instanceof StateRequestMessage)
		{
			sendState();
		}
		else if (message instanceof ScrollMessage)
		{
			ScrollMessage scrollMessage = (ScrollMessage) message;
			Scroller scroller = viewModel.getScroller();
			if (scroller instanceof NewScroller)
			{
				NewScroller newScroller = (NewScroller) scroller;
				newScroller.setFadingEnabled(Math.abs(scrollMessage.getAmount()) > 5.0f);
			}
			scroller.setTarget(scroller.getTarget() + scrollMessage.getAmount());
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();

		try
		{
			if (ViewModel.CONTENT_PROPERTY.equals(name))
			{
				sendModelContent();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void sendModelContent() throws IOException
	{
		Object content = viewModel.getContent();
		ContentModel contentModel = viewModel.getContentModel(content);
		PreparedContent preparedContent = contentModel.getPreparedContent();
		Object preparedValue = (preparedContent == null) ? null
		        : preparedContent.getValue();

		if (preparedValue instanceof Document)
		{
			Document document = (Document) preparedValue;

			List<String> lines = new ArrayList<String>();
			List<Float> tops = new ArrayList<Float>();

			float top = 0.0f;

			FontRenderContext fontRenderContext = new FontRenderContext(null,
			        true, true);

			Config config = ConfigManager.getConfig();
			Font defaultFont = config.getFont(Config.TextKind.DEFAULT);
			float normalLineHeight = 2.0f * (float) defaultFont.getStringBounds(
			        "x", fontRenderContext).getHeight();

			for (Row row : document.getRows())
			{
				for (Paragraph paragraph : row.getParagraphs())
				{
					Font font = paragraph.getFont();
					float lineHeight = (float) font.getMaxCharBounds(
					        fontRenderContext).getHeight();

					top += paragraph.getTopMargin();
					for (Line line : paragraph.getLines())
					{
						lines.add(line.toString());
						tops.add(top / normalLineHeight);
						top += lineHeight * paragraph.getLineHeight();
					}
					top += paragraph.getBottomMargin();
				}
			}

			String[] lineArray = lines.toArray(new String[lines.size()]);
			float[] topArray = new float[lines.size()];
			for (int i = 0; i < lines.size(); i++)
			{
				topArray[i] = tops.get(i);
			}

			sendMessage(new ContentMessage(lineArray, topArray));
		}
		else
		{
			String textContent = (content == null) ? "" : content.toString();
			sendMessage(new ContentMessage(new String[] { textContent },
			        new float[] { 0.0f }));
		}
	}

	@Override
	public void amplitudeChanged(int channel, double amplitude)
	{
		if (channel >= 2)
		{
			// ignore
		}
		else
		{
			recorderLevels[channel] = (byte) (amplitude * 127);

			long currentTime = System.currentTimeMillis();
			if (lastAmplitudeChanged - currentTime > 1000L)
			{
				lastAmplitudeChanged = currentTime;

				try
				{
					sendRecorderStatus();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void sendState() throws IOException
	{
		sendModelContent();
		sendRecorderStatus();
	}

	private void sendRecorderStatus() throws IOException
	{
		if (recorder != null)
		{
			sendMessage(new RecorderStatusMessage(recorder.isStarted(),
			        recorderLevels));
		}
	}

	private void sendMessage(Message message) throws IOException
	{
		System.out.println("Sending " + message);

		try
		{
			if (out == null)
			{
				throw new IllegalStateException("Not connected");
			}

			synchronized (out)
			{
				message.write(out);
				out.flush();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw e;
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
			throw e;
		}
		catch (Error e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void run()
	{
		try
		{
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if (viewModel != null)
		{
			viewModel.addPropertyChangeListener(this);
		}
		recorder.addAmplitudeListener(this);

		try
		{
			while (!Thread.interrupted() && socket.isConnected())
			{
				try
				{
					Message message = MessageParser.parse(in);
					System.out.println("Received " + message);
					handleMessage(message);
				}
				catch (RuntimeException e)
				{
					e.printStackTrace();
				}
				catch (EOFException e)
				{
					System.out.println("Connection reset by peer.");
					break;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					break;
				}
			}
		}
		finally
		{
			if (viewModel != null)
			{
				viewModel.removePropertyChangeListener(this);
			}
			recorder.removeAmplitudeListener(this);

			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
