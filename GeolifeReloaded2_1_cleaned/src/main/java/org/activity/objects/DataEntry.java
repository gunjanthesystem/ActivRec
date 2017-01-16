package org.activity.objects;

import java.io.Serializable;
//import java.math.String;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author gunjan
 *
 */
public abstract class DataEntry implements Serializable // why abstract class and not interface: because we dont want to
														// make all fields public static final
{
	protected Timestamp timestamp;
	protected long differenceWithNextInSeconds, durationInSeconds;
	protected ArrayList<String> trajectoryID;
	protected String extraComments; // some extra information which need to be stores
	/**
	 * This may or may not be relvant dependent on whether the activities are broken over days
	 */
	protected int breakOverDaysCount;

	public DataEntry(Timestamp timestamp, long differenceWithNextInSeconds, long durationInSeconds,
			ArrayList<String> trajectoryID, String extraComments, int breakOverDaysCount)
	{
		super();
		this.timestamp = timestamp;
		this.differenceWithNextInSeconds = differenceWithNextInSeconds;
		this.durationInSeconds = durationInSeconds;
		this.trajectoryID = trajectoryID;
		this.extraComments = extraComments;
		this.breakOverDaysCount = breakOverDaysCount;
	}

	public DataEntry()
	{
	}

	/**
	 * Usually used for creating <i>Unknown</i> data entries
	 * 
	 * @param t
	 * @param mod
	 */
	public DataEntry(Timestamp t, long durationInSeconds, String mod)
	{
	}

	// abstract public boolean isMergeableAllSame(DataEntry tleA, DataEntry tleB);

	abstract public String toString();

	abstract public String toStringWithTrajID();

	abstract public String toStringWithTrajIDWithTrajPurityCheck();

	abstract public String toStringWithTrajIDsInfo();

	/**
	 * returned values are in sequence,
	 * "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
	 * 
	 * @return
	 */
	abstract public String toStringWithoutHeaders();

	/**
	 * Returns with comma as delimiter
	 * 
	 * @return
	 */
	abstract public String toStringWithoutHeadersWithTrajID();

	abstract public String toStringWithoutHeadersWithTrajID(String delimiter);

	/**
	 * returns values are in sequence,"timestamp,
	 * endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,#distinctTrajIDs,#TrajIDs,trajID,,latitude,longitude,alt"
	 * Timestamps strings changed to GMT string on April 7 2016
	 * 
	 * @return
	 */
	abstract public String toStringWithoutHeadersWithTrajIDPurityCheck();

	/**
	 * returned values are in sequence, "Timestamp,Mode,Latitude,Longitude,Altitude"
	 * 
	 * @return
	 */
	abstract public String toStringEssentialsWithoutHeaders();

	public void setDurationInSeconds(long tInSecs)
	{
		this.durationInSeconds = tInSecs;
	}

	public long getDurationInSeconds()
	{
		return this.durationInSeconds;
	}

	public void setDifferenceWithNextInSeconds(long t)
	{
		this.differenceWithNextInSeconds = t;
	}

	public long getDifferenceWithNextInSeconds()
	{
		return this.differenceWithNextInSeconds;
	}

	/**
	 * Returns the end timestamp computed using the duration in seconds
	 * 
	 * @return
	 */
	public Timestamp getEndTimestamp()
	{
		Timestamp ts = null;
		if (durationInSeconds != 0)// not set
		{
			ts = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000));
		}
		return ts;
	}

	public void setExtraComments(String comments)
	{
		this.extraComments = comments;
	}

	public String getExtraComments()
	{
		return this.extraComments;
	}

	// public void setMode(String mod)
	// {
	// this.mode = mod;
	// }
	//
	// public String getMode()
	// {
	// return this.mode;
	// }

	// /**
	// *
	// * @param lat
	// */
	// public void addLatitude(String lat)
	// {
	// this.lat.add(UtilityBelt.round(lat, 6));
	// }
	//
	// /**
	// * clears previous latitude entries and adds the new ones. </br> <b>Used when merging Trajectory entries and not
	// when reading raw files.</b>
	// *
	// * @param a
	// */
	// public void setLatitude(ArrayList<String> a)
	// {
	// this.lat.clear();
	// this.lat.addAll(a);
	// }
	//
	// public ArrayList<String> getLatitude()
	// {
	// return this.lat;
	// }
	//
	// public void addLongitude(String lon)
	// {
	// this.lon.add(UtilityBelt.round(lon, 6));
	// }
	//
	// public void setLongitude(ArrayList<String> a)
	// {
	// this.lon.clear();
	// this.lon.addAll(a);
	// }
	//
	// public ArrayList<String> getLongitude()
	// {
	// return this.lon;
	// }
	//
	// public void addAltitude(String alt)
	// {
	// this.alt.add(alt);
	// }
	//
	// public void setAltitude(ArrayList<String> a)
	// {
	// this.alt.clear();
	// this.alt.addAll(a);
	// }
	//
	// public ArrayList<String> getAltitude()
	// {
	// return this.alt;
	// }
	//
	public void setTrajectoryID(ArrayList<String> a)
	{
		this.trajectoryID.clear();
		this.trajectoryID.addAll(a);
	}

	public ArrayList<String> getTrajectoryID()
	{
		return this.trajectoryID;
	}

	/**
	 * This is useful for merged trajectories which have more than one trajectory entries
	 * 
	 * @return
	 */
	public int getNumberOfDistinctTrajectoryIDs()
	{
		TreeSet<String> set = new TreeSet(this.trajectoryID);
		return set.size();
	}

	public int getNumberOfTrajectoryIDs()
	{
		return this.trajectoryID.size();
	}

	/**
	 * Returns a trajectory ID /** Returns distinct trajectory IDs separated by delimiter.
	 * 
	 * i.e., if there is only one distinct trajectory Id in the the arraylist of trajectory IDs, else returns the
	 * distinct trajIDs separated by delimiter. There is an arraylist of trajectory ids in the first place to allow for
	 * multiple trajectory entries (with same or difference trajectory IDs) to be merged.
	 * 
	 * @param delimiter
	 * @return
	 */
	public String getDistinctTrajectoryIDs(String delimiter)
	{
		String distinctTrajIds = "";

		Set<String> set = new LinkedHashSet<String>(this.trajectoryID);

		return String.join(delimiter, set);

	}

	public void setTimestamp(Timestamp t)
	{
		this.timestamp = t;
	}

	public Timestamp getTimestamp()
	{
		return this.timestamp;
	}

	// public String getStartLat()
	// {
	// if (lat.size() > 0)
	// {
	// return lat.get(0);
	// }
	//
	// else
	// return "-777";
	// }
	//
	// public String getEndLat()
	// {
	// if (lat.size() > 0)
	// {
	// return lat.get(lat.size() - 1);
	// }
	//
	// else
	// return "-777";
	// }
	//
	// public String getStartLon()
	// {
	// if (lon.size() > 0)
	// {
	// return lon.get(0);
	// }
	//
	// else
	// return new String("-777");
	// }
	//
	// public String getEndLon()
	// {
	// if (lon.size() > 0)
	// {
	// return lon.get(lon.size() - 1);
	// }
	//
	// else
	// return new String("-777");
	// }
	//
	// public String getStartAlt()
	// {
	// if (alt.size() > 0)
	// {
	// return alt.get(0);
	// }
	//
	// else
	// return new String("-777");
	// }
	//
	// public String getEndAlt()
	// {
	// if (alt.size() > 0)
	// {
	// return alt.get(alt.size() - 1);
	// }
	//
	// else
	// return new String("-777");
	// }
	//
	public void setBreakOverDaysCount(int c)
	{
		this.breakOverDaysCount = c;
	}

	public int getBreakOverDaysCount()
	{
		return this.breakOverDaysCount;
	}

	// //////////Getters for counts
	// public static long getCountNegativeAltitudes()
	// {
	// return countNegativeAltitudes;
	// }
	//
	// public static long getCountZeroAltitudes()
	// {
	// return countZeroAltitudes;
	// }
	//
	// public static long getCountUnknownAltitudes()
	// {
	// return countUnknownAltitudes;
	// }
	//
	// public static long getCountNegativeLatitudes()
	// {
	// return countNegativeLatitudes;
	// }
	//
	// public static long getCountZeroLatitudes()
	// {
	// return countZeroLatitudes;
	// }
	//
	// public static long getCountUnknownLatitudes()
	// {
	// return countUnknownLatitudes;
	// }
	//
	// public static long getCountNegativeLongitudes()
	// {
	// return countNegativeLongitudes;
	// }
	//
	// public static long getCountZeroLongitudes()
	// {
	// return countZeroLongitudes;
	// }
	//
	// public static long getCountUnknownLongitudes()
	// {
	// return countUnknownLongitudes;
	// }
	//
	// public static long getTotalCountTrajectoryEntries()
	// {
	// return totalCountTrajectoryEntries;
	// }
	// public String getAvgAlt()
	// {
	// if(alt.size()>0)
	// {
	// return getAverage(alt);
	// }
	//
	// else
	// return -777;
	// }

	// public static String getAverage(ArrayList <String> marks)
	// {
	// String sum = new String(0);
	// if(!marks.isEmpty())
	// {
	// for (String mark : marks)
	// {
	// sum += mark;
	// }
	//
	// return sum.StringValue() / marks.size();
	// }
	// return sum;
	// }
}
