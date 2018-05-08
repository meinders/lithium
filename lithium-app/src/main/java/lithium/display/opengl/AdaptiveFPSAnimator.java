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

package lithium.display.opengl;

import javax.media.opengl.*;
import javax.swing.event.*;

import com.github.meinders.common.animation.*;

class AdaptiveFPSAnimator implements Runnable, ChangeListener
{
	private final GLAutoDrawable drawable;

	private final int framesPerSecond;

	private Thread thread;

	private boolean animating;

	private int bias = 0;

	private FrameCounter counter;

	public AdaptiveFPSAnimator(GLAutoDrawable drawable, int framesPerSecond)
	{
		this.drawable = drawable;
		this.framesPerSecond = framesPerSecond;

		counter = new FrameCounter();
		counter.addChangeListener(this);
	}

	public void stop()
	{
		animating = false;
	}

	public boolean isAnimating()
	{
		return animating;
	}

	public void start()
	{
		animating = true;
		if (thread == null)
		{
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public void run()
	{
		if (Threading.isOpenGLThread())
		{
			drawable.display();
			counter.countFrame();
		}
		else
		{
			while (!Thread.interrupted())
			{
				if (isAnimating())
				{
					Threading.invokeOnOpenGLThread(false, this);
				}

				long timeout = 1000L / framesPerSecond + bias;
				if (timeout <= 0L)
				{
					Thread.yield();
				}
				else
				{
					try
					{
						Thread.sleep(timeout);
					}
					catch (InterruptedException e)
					{
						break;
					}
				}
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		int framesPerSecond = counter.getFramesPerSecond();
		if (framesPerSecond > this.framesPerSecond)
		{
			bias++;
		}
		else if (framesPerSecond < this.framesPerSecond)
		{
			bias--;
		}
	}
}