package org.activity.objects;

//import java.math.String;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * 
 * @author gunjan
 *
 */
public interface IDataEntry
{
	// private Timestamp timestamp;
	// private long differenceWithNextInSeconds, durationInSeconds;
	// private ArrayList<String> trajectoryID;

	/**
	 * This may or may not be relvant dependent on whether the activities are broken over days
	 */
	// int breakOverDaysCount;

	// public DataEntryInterface()
	// {
	// }
	//
	// /**
	// * Usually used for creating <i>Unknown</i> data entries
	// *
	// * @param t
	// * @param mod
	// */
	// public DataEntryInterface(Timestamp t, long durationInSeconds, String mod)
	// {
	// }

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
	public String toStringWithoutHeaders();

	/**
	 * returns values are in sequence,"timestamp,
	 * endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt"
	 * Changed timestamps string to GMT string on April 7 2016
	 * 
	 * @return
	 */
	public String toStringWithoutHeadersWithTrajID();

	/**
	 * returns values are in sequence,"timestamp,
	 * endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,#distinctTrajIDs,#TrajIDs,trajID,,latitude,longitude,alt"
	 * Timestamps strings changed to GMT string on April 7 2016
	 * 
	 * @return
	 */
	public String toStringWithoutHeadersWithTrajIDPurityCheck();

	/**
	 * returned values are in sequence, "Timestamp,Mode,Latitude,Longitude,Altitude"
	 * 
	 * @return
	 */
	abstract public String toStringEssentialsWithoutHeaders();

	public void setDurationInSeconds(long tInSecs);

	public long getDurationInSeconds();

	public void setDifferenceWithNextInSeconds(long t);

	public long getDifferenceWithNextInSeconds();

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
	public void setTrajectoryID(ArrayList<String> a);

	public ArrayList<String> getTrajectoryID();

	/**
	 * This is useful for merged trajectories which have more than one trajectory entries
	 * 
	 * @return
	 */
	public int getNumberOfDistinctTrajectoryIDs();

	public int getNumberOfTrajectoryIDs();

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
	public String getDistinctTrajectoryIDs(String delimiter);

	public void setTimestamp(Timestamp t);

	public Timestamp getTimestamp();

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
	public void setBreakOverDaysCount(int c);

	public int getBreakOverDaysCount();
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
