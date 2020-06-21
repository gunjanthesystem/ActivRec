package org.activity.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class StayPointsAllDataContainer implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7169765244008259776L;

	public LinkedHashMap<String, TreeMap<String, ArrayList<TrajectoryEntry>>> mapStayPoints;

	public StayPointsAllDataContainer(LinkedHashMap<String, TreeMap<String, ArrayList<TrajectoryEntry>>> mapStayPoints)
	{
		this.mapStayPoints = mapStayPoints;
	}

	public StayPointsAllDataContainer()
	{
		mapStayPoints = null;
	}

}
