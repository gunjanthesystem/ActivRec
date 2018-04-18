package org.activity.desktop;

import java.time.LocalDateTime;

public class DesktopEntry
{
	String windowTitle, applicationName;
	LocalDateTime ts;

	public DesktopEntry()
	{

	}

	public DesktopEntry(LocalDateTime ts, String windowTitle, String applicationName)
	{
		super();
		this.windowTitle = windowTitle;
		this.applicationName = applicationName;
		this.ts = ts;
	}

	@Override
	public String toString()
	{
		return windowTitle + ", " + applicationName + ", " + ts;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicationName == null) ? 0 : applicationName.hashCode());
		result = prime * result + ((ts == null) ? 0 : ts.hashCode());
		result = prime * result + ((windowTitle == null) ? 0 : windowTitle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DesktopEntry other = (DesktopEntry) obj;
		if (applicationName == null)
		{
			if (other.applicationName != null) return false;
		}
		else if (!applicationName.equals(other.applicationName)) return false;
		if (ts == null)
		{
			if (other.ts != null) return false;
		}
		else if (!ts.equals(other.ts)) return false;
		if (windowTitle == null)
		{
			if (other.windowTitle != null) return false;
		}
		else if (!windowTitle.equals(other.windowTitle)) return false;
		return true;
	}

	public String getWindowTitle()
	{
		return windowTitle;
	}

	public String getApplicationName()
	{
		return applicationName;
	}

	public LocalDateTime getTs()
	{
		return ts;
	}

}
