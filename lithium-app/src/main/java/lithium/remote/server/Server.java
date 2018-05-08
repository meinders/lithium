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

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server implements Runnable
{
	private final int serverPort;

	private ServerSocket serverSocket;

	private final ConnectionHandlerFactory connectionHandlerFactory;

	private ExecutorService threadPool = Executors.newCachedThreadPool();

	public Server(int serverPort,
	        ConnectionHandlerFactory connectionHandlerFactory)
	{
		super();
		this.serverPort = serverPort;
		this.connectionHandlerFactory = connectionHandlerFactory;
	}

	public void close()
	{
		try
		{
			serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(serverPort);
		}
		catch (IOException e)
		{
			System.err.println("Failed to open server socket: "
			        + e.getMessage());
			return;
		}

		System.out.println("Listening on port " + serverPort);

		while (!Thread.interrupted())
		{
			Socket socket = null;
			try
			{
				socket = serverSocket.accept();
				System.out.println("Accepting connection from "
				        + socket.getRemoteSocketAddress());
			}
			catch (SocketException e)
			{
				if (!"socket closed".equals(e.getMessage()))
				{
					e.printStackTrace();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				if (socket != null)
				{
					try
					{
						socket.close();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}

			ConnectionHandler connectionHandler = connectionHandlerFactory.newInstance(socket);
			threadPool.submit(connectionHandler);

			Thread.yield();
		}

		try
		{
			serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println("Shutting down server thread pool...");
		threadPool.shutdownNow();
		try
		{
			while (!threadPool.awaitTermination(1, TimeUnit.SECONDS))
			{
				System.out.println("...");
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
}
