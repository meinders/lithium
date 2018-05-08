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

package lithium.reminder;

import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

import lithium.editor.*;

/**
 * Keeps track of reminders and ensures that reminders are displayed at the
 * correct time.
 *
 * @author Gerrit Meinders
 */
public class ReminderManager
{
	private static ReminderManager instance;

	public static synchronized ReminderManager getInstance()
	{
		if (instance == null)
		{
			instance = new ReminderManager();
		}
		return instance;
	}

	private ScheduledExecutorService executor;

	private Collection<Reminder> reminders;

	public ReminderManager()
	{
		executor = Executors.newScheduledThreadPool(1);
		reminders = new ConcurrentLinkedQueue<Reminder>();
	}

	/**
	 * Adds a reminder.
	 *
	 * @param reminder a reminder
	 */
	public void addReminder(Reminder reminder)
	{
		Date time = reminder.getTime();
		long delay = time.getTime() - System.currentTimeMillis();
		executor.schedule(new ScheduledReminder(reminder), delay,
		        TimeUnit.MILLISECONDS);
		reminders.add(reminder);
	}

	/**
	 * Removes a reminder.
	 *
	 * @param reminder a reminder
	 */
	public void removeReminder(Reminder reminder)
	{
		reminders.remove(reminder);
	}

	/**
	 * Returns all currently scheduled reminders.
	 *
	 * @return all scheduled reminders
	 */
	public Collection<Reminder> getReminders()
	{
		return Collections.unmodifiableCollection(reminders);
	}

	private void showReminder(Reminder reminder)
	{
		assert SwingUtilities.isEventDispatchThread();

		EditorFrame editor = EditorFrame.getInstance();
		JOptionPane.showMessageDialog(editor, reminder.getMessage());
	}

	private class ScheduledReminder implements Runnable
	{
		private final Reminder reminder;

		public ScheduledReminder(Reminder reminder)
		{
			this.reminder = reminder;
		}

		@Override
		public void run()
		{
			if (SwingUtilities.isEventDispatchThread())
			{
				showReminder(reminder);
				reminders.remove(reminder);
			}
			else if (reminders.contains(reminder))
			{
				SwingUtilities.invokeLater(this);
			}
		}
	}
}
