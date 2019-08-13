package org.activity.objects;

import java.sql.Timestamp;

public class StayPoint extends TrajectoryEntry
{
	// protected ArrayList<TimeStamp> ts;

	public StayPoint(String lat, String lon, String alt, Timestamp t, String mod)
	{
		super(lat, lon, alt, t, mod);

	}

	public StayPoint(String lat, String lon, String alt, Timestamp t, String mod, String trajectoryID)
	{
		super(lat, lon, alt, t, mod, trajectoryID);

	}

	public StayPoint(Timestamp t, long durationInSeconds, String mod)
	{
		super(t, durationInSeconds, mod);

	}

}
