package org.activity.objects;

import java.io.Serializable;
//import java.math.String;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.activity.stats.StatsUtils;
import org.activity.util.StringUtils;

/**
 * 
 * @author gunjan
 *
 */
public class TrajectoryEntryV2 extends DataEntry implements Serializable
{
	private static final long serialVersionUID = 1L;

	// Start of Inherited fields//
	// protected Timestamp timestamp;
	// protected long differenceWithNextInSeconds, durationInSeconds;
	// protected ArrayList<String> trajectoryID;
	// protected String extraComments; // some extra information which need to be stores
	// /**
	// * This may or may not be relvant dependent on whether the activities are broken over days
	// */
	// protected int breakOverDaysCount;
	// End of Inherited fields//

	private String mode, userID;
	private ArrayList<String> lat, lon, alt;

	/**
	 * All these count values are currently used on per user basis, i.e. they are set to initial values at the start of
	 * creating Trajectory Entries for each user. However, this is not ensured in this class, but ensured in class which
	 * creates Trajectory Entries. In current case for Geolife data, the method
	 * org.activity.generator.DatabaseCreatorGeolife.createAnnotatedTrajectoryMap() parses the raw trajectory entries to
	 * create Trajectory entries and hence it is responsible of for how it uses these count values (they are used on per
	 * user basis)
	 **/
	static long totalCountTrajectoryEntries = 0;

	static long countNegativeAltitudes = 0, countZeroAltitudes = 0, countUnknownAltitudes = 0; // invalids are with -777
																								// as value, these are
																								// Unknown values
	static long countNegativeLatitudes = 0, countZeroLatitudes = 0, countUnknownLatitudes = 0;
	static long countNegativeLongitudes = 0, countZeroLongitudes = 0, countUnknownLongitudes = 0;

	/**
	 * 
	 * @param tleA
	 * @param tleB
	 * @return
	 */
	public static boolean isMergeableAllSame(DataEntry tleAD, DataEntry tleBD)
	{
		TrajectoryEntryV2 tleA = (TrajectoryEntryV2) tleAD;
		TrajectoryEntryV2 tleB = (TrajectoryEntryV2) tleBD;

		if (tleA.userID.equals(tleB.userID) && tleA.trajectoryID.toString().equals(tleB.trajectoryID.toString())
		// && tleA.lat.toString().equals(tleB.lat.toString()) && tleA.lon.toString().equals(tleB.lon.toString())
				&& (tleA.breakOverDaysCount == tleB.breakOverDaysCount))
		{
			return true;
		}
		else
		{
			return false;
		}

	}

	/**
	 * Note: latitude and longitude are rounded off to 10 decimal places
	 * 
	 * @param lat
	 * @param lon
	 * @param alt
	 * @param t
	 * @param mod
	 */
	public TrajectoryEntryV2(String lat, String lon, String alt, Timestamp t, String mod)
	{
		this.lat = new ArrayList<String>();
		this.lon = new ArrayList<String>();
		this.alt = new ArrayList<String>();

		this.lat.add(StatsUtils.round(lat, 6));
		this.lon.add(StatsUtils.round(lon, 6));
		this.alt.add(alt);

		this.timestamp = t;
		this.mode = mod;

		countNegativesZerosInvalids(lat, lon, alt);
	}

	/**
	 * Note: latitude and longitude are rounded off to 10 decimal places
	 * 
	 * @param lat
	 * @param lon
	 * @param alt
	 * @param t
	 * @param mod
	 */
	public TrajectoryEntryV2(String lat, String lon, String alt, Timestamp t, String mod, String trajectoryID)
	{
		this.lat = new ArrayList<String>();
		this.lon = new ArrayList<String>();
		this.alt = new ArrayList<String>();
		this.trajectoryID = new ArrayList<String>();

		this.lat.add(StatsUtils.round(lat, 6));
		this.lon.add(StatsUtils.round(lon, 6));
		this.alt.add(alt);

		this.timestamp = t;
		this.mode = mod;

		countNegativesZerosInvalids(lat, lon, alt);

		this.trajectoryID.add(trajectoryID);
	}

	public static void clearCountNegativesZerosInvalids()
	{
		totalCountTrajectoryEntries = 0;
		countNegativeAltitudes = 0;
		countZeroAltitudes = 0;
		countUnknownAltitudes = 0; // invalids are with -777 as value; these are Unknown values
		countNegativeLatitudes = 0;
		countZeroLatitudes = 0;
		countUnknownLatitudes = 0;
		countNegativeLongitudes = 0;
		countZeroLongitudes = 0;
		countUnknownLongitudes = 0;
	}

	public void countNegativesZerosInvalids(String lat, String lon, String alt)
	{
		totalCountTrajectoryEntries++;

		if (Double.valueOf(lat) < 0)
		{
			countNegativeLatitudes++;
		}
		if (Double.valueOf(lon) < 0)
		{
			countNegativeLongitudes++;
		}
		if (Double.valueOf(alt) < 0)
		{
			countNegativeAltitudes++;
		}
		// ////////////////////////////////////
		if (Double.valueOf(lat) == 0)
		{
			countZeroLatitudes++;
		}
		if (Double.valueOf(lon) == 0)
		{
			countZeroLongitudes++;
		}
		if (Double.valueOf(alt) == 0)
		{
			countZeroAltitudes++;
		}
		// ////////////////////////////////////
		if (Double.valueOf(lat) == -777)
		{
			countUnknownLatitudes++;
		}
		if (Double.valueOf(lon) == -777)
		{
			countUnknownLongitudes++;
		}
		if (Double.valueOf(alt) == -777)
		{
			countUnknownAltitudes++;
		}

	}

	/**
	 * usually used for creating "Unknown trajectories"
	 * 
	 * @param t
	 * @param mod
	 */
	public TrajectoryEntryV2(Timestamp t, long durationInSeconds, String mod)
	{
		this.timestamp = t;
		this.mode = mod;
		this.durationInSeconds = durationInSeconds;

		this.lat = new ArrayList<String>();
		this.lon = new ArrayList<String>();
		this.alt = new ArrayList<String>();
		this.trajectoryID = new ArrayList<String>();

		if (mod.equals("Unknown"))
		{
			lat.add(new String("-777"));
			lon.add(new String("-777"));
			alt.add(new String("-777"));
			trajectoryID.add(new String("-777"));
		}

		// latitude=longitude= altitude =-99;
	}

	// public TrajectoryEntry()
	// {
	// }

	// /**
	// * <font color="red">NOT USED CURRENTLY</font></br> usually used for creating "Unknown trajectories"
	// *
	// * @param t
	// * @param mod
	// */
	// public TrajectoryEntry(Timestamp t, long durationInSeconds, String mod, int breakOverDaysCount)
	// {
	// this.timestamp = t;
	// this.mode = mod;
	// this.durationInSeconds = durationInSeconds;
	// this.breakOverDaysCount = breakOverDaysCount;
	//
	// // latitude=longitude= altitude =-99;
	// }

	public String toString()
	{
		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000))
					.toGMTString();

		return "t:" + timestamp.toGMTString() + ",  mod:" + mode + " ,endt:" + endTimestampString
				+ ", timeDiffWithNextInSecs:" + this.differenceWithNextInSeconds + ",  durationInSeconds:"
				+ this.durationInSeconds + ",  bodCount:" + this.breakOverDaysCount + ", lat:" + lat.toString()
				+ ", lon:" + lon.toString() + ", alt:" + alt.toString();
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}

	/**
	 * Note: Changed to GMT string on 11 April 2016
	 * 
	 * @return
	 */
	public String toStringWithTrajID()
	{
		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000))
					.toGMTString();

		return "t:" + timestamp.toGMTString() + ",mod:" + mode + " ,endt:" + endTimestampString
				+ ", timeDiffWithNextInSecs:" + this.differenceWithNextInSeconds + ",  durationInSeconds:"
				+ this.durationInSeconds + ",  bodCount:" + this.breakOverDaysCount + ", lat:" + lat.toString()
				+ ", lon:" + lon.toString() + ", alt:" + alt.toString() + "tid:"
				+ trajectoryID.toString().replaceAll(",", "__");
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
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

	public String toStringWithTrajIDWithTrajPurityCheck()
	{
		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000))
					.toGMTString();

		return "t:" + timestamp.toGMTString() + ",mod:" + mode + " ,endt:" + endTimestampString
				+ ", timeDiffWithNextInSecs:" + this.differenceWithNextInSeconds + ",  durationInSeconds:"
				+ this.durationInSeconds + ",  bodCount:" + this.breakOverDaysCount + ",#distinctTids:"
				+ getNumberOfDistinctTrajectoryIDs() + ",Tid:" + trajectoryID.toString().replaceAll(",", "__")
				+ ", lat:" + lat.toString() + ", lon:" + lon.toString() + ", alt:" + alt.toString();
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}

	public String toStringWithTrajIDsInfo()
	{
		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000))
					.toGMTString();

		return "t:" + timestamp.toGMTString() + ",mod:" + mode + " ,endt:" + endTimestampString
				+ ", timeDiffWithNextInSecs:" + this.differenceWithNextInSeconds + ",  durationInSeconds:"
				+ this.durationInSeconds + ",#distinctTids:" + getNumberOfDistinctTrajectoryIDs() + ",#Tids:"
				+ getNumberOfTrajectoryIDs() + ",Tid:" + trajectoryID.toString().replaceAll(",", "__");
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}

	/**
	 * returned values are in sequence,
	 * "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
	 * 
	 * @return
	 */
	public String toStringWithoutHeaders()
	{// "trajID,timestamp,
		// endt,mode,latitude,longitude,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount",

		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000))
					.toGMTString();

		return timestamp.toGMTString() + " , " + endTimestampString + ", " + mode + ", " + lat.toString() + ", "
				+ lon.toString() + ", " + alt.toString() + ", " + this.differenceWithNextInSeconds + ", "
				+ this.durationInSeconds + ", " + this.breakOverDaysCount;
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}

	/**
	 * returns values are in sequence,"timestamp,
	 * endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt"
	 * Changed timestamps string to GMT string on April 7 2016
	 * 
	 * @return
	 */
	public String toStringWithoutHeadersWithTrajID()
	{// "timestamp,
		// endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",

		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000))
					.toGMTString();

		return timestamp.toGMTString() + " , " + endTimestampString + ", " + mode + ", "
				+ this.differenceWithNextInSeconds + ", " + this.durationInSeconds + ", " + this.breakOverDaysCount
				+ ", " + StringUtils.toStringCompactWithCount(this.trajectoryID).replaceAll(",", "_") + ", "
				+ lat.toString().replaceAll(",", "_") + ", " + lon.toString().replaceAll(",", "_") + ", "
				+ alt.toString().replaceAll(",", "_");

		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}

	/**
	 * returns values are in sequence,"timestamp,
	 * endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,#distinctTrajIDs,#TrajIDs,trajID,,latitude,longitude,alt"
	 * Timestamps strings changed to GMT string on April 7 2016
	 * 
	 * @return
	 */
	public String toStringWithoutHeadersWithTrajIDPurityCheck()
	{

		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000))
					.toGMTString();

		return timestamp.toGMTString() + " , " + endTimestampString + ", " + mode + ", "
				+ this.differenceWithNextInSeconds + ", " + this.durationInSeconds + ", " + this.breakOverDaysCount
				+ ", " + "," + this.getNumberOfDistinctTrajectoryIDs() + "," + this.getNumberOfTrajectoryIDs() + ","
				+ this.trajectoryID.toString().replaceAll(",", "_") + ", " + lat.toString().replaceAll(",", "_") + ", "
				+ lon.toString().replaceAll(",", "_") + ", " + alt.toString().replaceAll(",", "_");

		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}

	/**
	 * returned values are in sequence, "Timestamp,Mode,Latitude,Longitude,Altitude"
	 * 
	 * @return
	 */
	public String toStringEssentialsWithoutHeaders()
	{

		return timestamp.toGMTString() + ", " + mode + ", " + lat.toString() + ", " + lon.toString() + ", "
				+ alt.toString();
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude // +", el:"+endLatitude+",
		// slo:"+endLongitude+", sal:"+endAltitude // +",avgAl:"+
	}

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

	public void setMode(String mod)
	{
		this.mode = mod;
	}

	public String getMode()
	{
		return this.mode;
	}

	/**
	 * 
	 * @param lat
	 */
	public void addLatitude(String lat)
	{
		this.lat.add(StatsUtils.round(lat, 6));
	}

	/**
	 * clears previous latitude entries and adds the new ones. </br>
	 * <b>Used when merging Trajectory entries and not when reading raw files.</b>
	 * 
	 * @param a
	 */
	public void setLatitude(ArrayList<String> a)
	{
		this.lat.clear();
		this.lat.addAll(a);
	}

	public ArrayList<String> getLatitude()
	{
		return this.lat;
	}

	public void addLongitude(String lon)
	{
		this.lon.add(StatsUtils.round(lon, 6));
	}

	public void setLongitude(ArrayList<String> a)
	{
		this.lon.clear();
		this.lon.addAll(a);
	}

	public ArrayList<String> getLongitude()
	{
		return this.lon;
	}

	public void addAltitude(String alt)
	{
		this.alt.add(alt);
	}

	public void setAltitude(ArrayList<String> a)
	{
		this.alt.clear();
		this.alt.addAll(a);
	}

	public ArrayList<String> getAltitude()
	{
		return this.alt;
	}

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

	public String getStartLat()
	{
		if (lat.size() > 0)
		{
			return lat.get(0);
		}

		else
			return "-777";
	}

	public String getEndLat()
	{
		if (lat.size() > 0)
		{
			return lat.get(lat.size() - 1);
		}

		else
			return "-777";
	}

	public String getStartLon()
	{
		if (lon.size() > 0)
		{
			return lon.get(0);
		}

		else
			return new String("-777");
	}

	public String getEndLon()
	{
		if (lon.size() > 0)
		{
			return lon.get(lon.size() - 1);
		}

		else
			return new String("-777");
	}

	public String getStartAlt()
	{
		if (alt.size() > 0)
		{
			return alt.get(0);
		}

		else
			return new String("-777");
	}

	public String getEndAlt()
	{
		if (alt.size() > 0)
		{
			return alt.get(alt.size() - 1);
		}

		else
			return new String("-777");
	}

	public void setBreakOverDaysCount(int c)
	{
		this.breakOverDaysCount = c;
	}

	public int getBreakOverDaysCount()
	{
		return this.breakOverDaysCount;
	}

	// //////////Getters for counts
	public static long getCountNegativeAltitudes()
	{
		return countNegativeAltitudes;
	}

	public static long getCountZeroAltitudes()
	{
		return countZeroAltitudes;
	}

	public static long getCountUnknownAltitudes()
	{
		return countUnknownAltitudes;
	}

	public static long getCountNegativeLatitudes()
	{
		return countNegativeLatitudes;
	}

	public static long getCountZeroLatitudes()
	{
		return countZeroLatitudes;
	}

	public static long getCountUnknownLatitudes()
	{
		return countUnknownLatitudes;
	}

	public static long getCountNegativeLongitudes()
	{
		return countNegativeLongitudes;
	}

	public static long getCountZeroLongitudes()
	{
		return countZeroLongitudes;
	}

	public static long getCountUnknownLongitudes()
	{
		return countUnknownLongitudes;
	}

	public static long getTotalCountTrajectoryEntries()
	{
		return totalCountTrajectoryEntries;
	}

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

	@Override
	public String toStringWithoutHeadersWithTrajID(String delimiter)
	{
		// TODO Auto-generated method stub
		return null;
	}

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
