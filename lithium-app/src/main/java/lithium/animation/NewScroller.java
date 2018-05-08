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

package lithium.animation;

import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;

import com.github.meinders.common.animation.*;
import com.github.meinders.common.animation.Timer;
import lithium.animation.legacy.scrolling.*;

public class NewScroller implements Scroller
{
	private Timer timer;

	private double transitionTime;

	private final Transition1D position;

	private final Transition1D visibility;

	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

	private boolean fadingEnabled = false;

	public NewScroller(double initialValue, double transitionTime)
	{
		timer = new Timer();
		position = new Transition1D();
		visibility = new Transition1D();

		position.setVariable(new Constant1D(initialValue));
		visibility.setVariable(new Constant1D(1.0));

		this.transitionTime = transitionTime;

		start();
	}

	public boolean isFadingEnabled()
	{
		return fadingEnabled;
	}

	public void setFadingEnabled(boolean fadingEnabled)
	{
		this.fadingEnabled = fadingEnabled;
	}

	public double get()
	{
		return position.get(timer.currentTime());
	}

	public void set(double value)
	{
		if (Math.abs(position.getEndValue() - value) > 0.0001)
		{
			double currentTime = timer.currentTime();
			double endTime = currentTime + transitionTime;
			double endValue = Math.max(0.0, value);

			double averageRate = (endValue - position.get(currentTime))
			        / transitionTime;

			if (fadingEnabled && (Math.abs(averageRate) > 10.0))
			{
				double fadeOutTime = currentTime + 0.5 * transitionTime;
				double fadeInTime = endTime;

				Cubic1D fadeOut = Cubic1D.branch(visibility.getVariable(),
				        currentTime, 0.0, 0.0, fadeOutTime);
				Cubic1D fadeIn = Cubic1D.branch(fadeOut, fadeOutTime, 1.0, 0.0,
				        fadeInTime);

				visibility.set(new Switch1D(fadeOut, fadeIn, fadeOutTime),
				        currentTime, endTime);
				position.set(new Switch1D(position.getVariable(),
				        new Constant1D(endValue), fadeOutTime), currentTime,
				        endTime);

			}
			else
			{
				position.set(Cubic1D.branch(position.getVariable(),
				        currentTime, endValue, 0.0, endTime), currentTime,
				        endTime);
			}
		}
	}

	@Override
	public void addChangeListener(ChangeListener listener)
	{
		changeListeners.add(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener)
	{
		changeListeners.remove(listener);
	}

	@Override
	public float getTarget()
	{
		return (float) position.getEndValue();
	}

	@Override
	public float getValue()
	{
		return (float) get();
	}

	@Override
	public void setTarget(float target)
	{
		set(target);
	}

	@Override
	public void setTarget(float target, boolean addTargetDistance)
	{
		if (addTargetDistance)
		{
			set(target + (position.getEndValue() - get()));
		}
		else
		{
			set(target);
		}
	}

	@Override
	public void setValue(float value)
	{
		double currentTime = timer.currentTime();
		position.set(new Constant1D(value), currentTime, currentTime);
	}

	private void fireEvents()
	{
		for (ChangeListener listener : changeListeners)
		{
			listener.stateChanged(null);
		}
	}

	private javax.swing.Timer activeTimer = null;

	@Override
	@Deprecated
	public void start()
	{
		// FIXME: This is stupid. Let users create their own threads.
		activeTimer = new javax.swing.Timer(10, new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireEvents();
			}
		});
		activeTimer.start();
	}

	@Override
	@Deprecated
	public void dispose()
	{
		// FIXME: This is stupid. Let users create their own threads.
		if (activeTimer != null)
		{
			activeTimer.stop();
		}
	}

	public double getVisibility()
	{
		return Math.max(0.0, Math.min(visibility.get(timer.currentTime()), 1.0));
	}
}
