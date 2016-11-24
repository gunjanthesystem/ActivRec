package org.activity.objects;

import java.io.Serializable;
//import java.math.String;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.activity.util.StringUtils;
import org.activity.util.UtilityBelt;

/**
 * 
 * @author gunjan
 *
 */
public class TrajectoryEntry implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	// String trajectoryID; // added on Mar 14, 2016
	private String mode;
	// String latitude,longitude, altitude, startLatitude, startLongitude, endLatitude, endLongitude, avgAltitude;
	private ArrayList<String> lat, lon, alt, trajectoryID;
	private ArrayList<Timestamp> timestamps; // timestamps of data points inside it. (useful in cases when this is a merged data entry)
	
	private Timestamp timestamp;
	
	private long differenceWithNextInSeconds, durationInSeconds;
	// private static final boolean roundOffGeoCoordinates =
	/**
	 * All these count values are currently used on per user basis, i.e. they are set to initial values at the start of creating Trajectory Entries for each user. However, this is
	 * not ensured in this class, but ensured in class which creates Trajectory Entries. In current case for Geolife data, the method
	 * org.activity.generator.DatabaseCreatorGeolife.createAnnotatedTrajectoryMap() parses the raw trajectory entries to create Trajectory entries and hence it is responsible of
	 * for how it uses these count values (they are used on per user basis)
	 **/
	static long totalCountTrajectoryEntries = 0;
	
	static long countNegativeAltitudes = 0, countZeroAltitudes = 0, countUnknownAltitudes = 0; // invalids are with -777 as value, these are Unknown values
	static long countNegativeLatitudes = 0, countZeroLatitudes = 0, countUnknownLatitudes = 0;
	static long countNegativeLongitudes = 0, countZeroLongitudes = 0, countUnknownLongitudes = 0;
	
	// /**
	// * Merge trajectory entry te2 with te1, where te1 immediately preceedes te2
	// *
	// * @param te1
	// * @param te2
	// * @return
	// */
	// public static TrajectoryEntry mergeTrajectoryEntries(TrajectoryEntry te1, TrajectoryEntry te2)
	// {
	// if (te1.getTimestamp())
	// return null;
	// }
	
	/**
	 * This may or may not be relvant dependent on whether the activities are broken over days
	 */
	int breakOverDaysCount;
	
	/**
	 * NoteL
	 * 
	 * @param t1
	 *            must not be a merged point
	 * @param t2
	 *            must not be a merged point
	 * @return
	 * @throws Exception
	 */
	public static double getDistanceInKms(TrajectoryEntry t1, TrajectoryEntry t2) throws Exception
	{
		double res = -1;
		
		if (t1.getLatitude().size() > 1 || t2.getLatitude().size() > 1)
		{
			throw new Exception(
					"Error in org.activity.util.TrajectoryEntry.getDistance(TrajectoryEntry, TrajectoryEntry): Trajectory entries are not single points but merged points");
		}
		res = UtilityBelt.haversine(t1.getLatitude().get(0), t1.getLongitude().get(0), t2.getLatitude().get(0), t2.getLongitude().get(0));
		return res;
	}
	
	public TrajectoryEntry(String mode, ArrayList<String> lat, ArrayList<String> lon, ArrayList<String> alt, ArrayList<String> trajectoryID,
			ArrayList<Timestamp> timestamps, Timestamp timestamp, long differenceWithNextInSeconds, long durationInSeconds,
			int breakOverDaysCount)
	{
		super();
		this.mode = mode;
		this.lat = lat;
		this.lon = lon;
		this.alt = alt;
		this.trajectoryID = trajectoryID;
		this.timestamps = timestamps;
		this.timestamp = timestamp;
		this.differenceWithNextInSeconds = differenceWithNextInSeconds;
		this.durationInSeconds = durationInSeconds;
		this.breakOverDaysCount = breakOverDaysCount;
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
	public TrajectoryEntry(String lat, String lon, String alt, Timestamp t, String mod)
	{
		this.lat = new ArrayList<String>();
		this.lon = new ArrayList<String>();
		this.alt = new ArrayList<String>();
		this.timestamps = new ArrayList<Timestamp>();
		this.lat.add(UtilityBelt.round(lat, 6));
		this.lon.add(UtilityBelt.round(lon, 6)); // note: 6 decimal places offers the precision upto .11m
													// ref:http://dpstyles.tumblr.com/post/95952859425/how-much-does-the-precision-of-a-latlong-change
		// https://en.wikipedia.org/wiki/Decimal_degrees
		this.alt.add(alt);
		this.timestamps.add(t);
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
	public TrajectoryEntry(String lat, String lon, String alt, Timestamp t, String mod, String trajectoryID)
	{
		this.lat = new ArrayList<String>();
		this.lon = new ArrayList<String>();
		this.alt = new ArrayList<String>();
		this.timestamps = new ArrayList<Timestamp>();
		
		this.trajectoryID = new ArrayList<String>();
		
		this.lat.add(UtilityBelt.round(lat, 6));
		this.lon.add(UtilityBelt.round(lon, 6));
		this.alt.add(alt);
		this.timestamps.add(t);
		
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
	public TrajectoryEntry(Timestamp t, long durationInSeconds, String mod)
	{
		this.timestamp = t;
		this.mode = mod;
		this.durationInSeconds = durationInSeconds;
		
		this.lat = new ArrayList<String>();
		this.lon = new ArrayList<String>();
		this.alt = new ArrayList<String>();
		this.timestamps = new ArrayList<Timestamp>();
		this.trajectoryID = new ArrayList<String>();
		
		this.timestamps.add(t);
		
		if (mod.equals("Unknown"))
		{
			lat.add(new String("-777"));
			lon.add(new String("-777"));
			alt.add(new String("-777"));
			trajectoryID.add(new String("-777"));
		}
		
		// latitude=longitude= altitude =-99;
	}
	
	/**
	 * Dummy no argument constructor for Kryo serialisation deserialisation
	 */
	public TrajectoryEntry()
	{
		this.timestamp = null;
		this.mode = "";
		this.durationInSeconds = 0;
		
		this.lat = new ArrayList<String>();
		this.lon = new ArrayList<String>();
		this.alt = new ArrayList<String>();
		this.timestamps = new ArrayList<Timestamp>();
		this.trajectoryID = new ArrayList<String>();
		
	}
	
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
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
		
		return "t:" + timestamp.toGMTString() + ",  mod:" + mode + " ,endt:" + endTimestampString + ", timeDiffWithNextInSecs:"
				+ this.differenceWithNextInSeconds + ",  durationInSeconds:" + this.durationInSeconds + ",  bodCount:"
				+ this.breakOverDaysCount + ", lat:" + lat.toString() + ", lon:" + lon.toString() + ", alt:" + alt.toString();
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
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
		
		return "t:" + timestamp.toGMTString() + ",mod:" + mode + " ,endt:" + endTimestampString + ", timeDiffWithNextInSecs:"
				+ this.differenceWithNextInSeconds + ",  durationInSeconds:" + this.durationInSeconds + ",  bodCount:"
				+ this.breakOverDaysCount + ", lat:" + lat.toString() + ", lon:" + lon.toString() + ", alt:" + alt.toString() + "tid:"
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
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
		
		return "t:" + timestamp.toGMTString() + ",mod:" + mode + " ,endt:" + endTimestampString + ", timeDiffWithNextInSecs:"
				+ this.differenceWithNextInSeconds + ",  durationInSeconds:" + this.durationInSeconds + ",  bodCount:"
				+ this.breakOverDaysCount + ",#distinctTids:" + getNumberOfDistinctTrajectoryIDs() + ",Tid:"
				+ trajectoryID.toString().replaceAll(",", "__") + ", lat:" + lat.toString() + ", lon:" + lon.toString() + ", alt:"
				+ alt.toString();
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
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
		
		return "t:" + timestamp.toGMTString() + ",mod:" + mode + " ,endt:" + endTimestampString + ", timeDiffWithNextInSecs:"
				+ this.differenceWithNextInSeconds + ",  durationInSeconds:" + this.durationInSeconds + ",#distinctTids:"
				+ getNumberOfDistinctTrajectoryIDs() + ",#Tids:" + getNumberOfTrajectoryIDs() + ",Tid:"
				+ trajectoryID.toString().replaceAll(",", "__");
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}
	
	/**
	 * returned values are in sequence, "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
	 * 
	 * @return
	 */
	public String toStringWithoutHeaders()
	{// "trajID,timestamp, endt,mode,latitude,longitude,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount",
		
		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
		
		return timestamp.toGMTString() + " , " + endTimestampString + ", " + mode + ", " + lat.toString() + ", " + lon.toString() + ", "
				+ alt.toString() + ", " + this.differenceWithNextInSeconds + ", " + this.durationInSeconds + ", " + this.breakOverDaysCount;
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}
	
	/**
	 * returns values are in sequence,"timestamp, endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt" Changed timestamps
	 * string to GMT string on April 7 2016
	 * 
	 * @return
	 */
	public String toStringWithoutHeadersWithTrajID()
	{// "timestamp, endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",
		
		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
		
		return timestamp.toGMTString() + " , " + endTimestampString + ", " + mode + ", " + this.differenceWithNextInSeconds + ", "
				+ this.durationInSeconds + ", " + this.breakOverDaysCount + ", " + getNumberOfDistinctTrajectoryIDs() + ","
				+ getNumberOfTrajectoryIDs() + StringUtils.toStringCompactWithCount(this.trajectoryID).replaceAll(",", "_") + ", "
				+ lat.toString().replaceAll(",", "_") + ", " + lon.toString().replaceAll(",", "_") + ", "
				+ alt.toString().replaceAll(",", "_");
		
		// "timestamp, endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,#distinctTrajIDs,#TrajIDs,trajID,,latitude,longitude,alt",
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}
	
	public String toStringWithoutHeadersWithTrajIDWithoutCount()
	{// "timestamp, endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",
		
		String endTimestampString;
		if (durationInSeconds == 0)// not set
		{
			endTimestampString = "null";
		}
		else
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
		
		return timestamp.toGMTString() + " , " + endTimestampString + ", " + mode + ", " + this.differenceWithNextInSeconds + ", "
				+ this.durationInSeconds + ", " + this.breakOverDaysCount + ", " + getNumberOfDistinctTrajectoryIDs() + ","
				+ getNumberOfTrajectoryIDs() + "," + StringUtils.toStringCompactWithoutCount(this.trajectoryID).replaceAll(",", "_")
				+ ", " + lat.toString().replaceAll(",", "_") + ", " + lon.toString().replaceAll(",", "_") + ", "
				+ alt.toString().replaceAll(",", "_");
		
		// "timestamp, endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,#distinctTrajIDs,#TrajIDs,trajID,,latitude,longitude,alt",
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
		// +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
		// +",avgAl:"+
	}
	
	// public String toStringWithoutHeadersWithTrajID2()
	// {// "timestamp, endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",
	//
	// String endTimestampString;
	// if (durationInSeconds == 0)// not set
	// {
	// endTimestampString = "null";
	// }
	// else
	// endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
	//
	// return timestamp.toGMTString() + " , " + endTimestampString + ", " + mode + ", " + this.differenceWithNextInSeconds + ", "
	// + this.durationInSeconds + ", " + this.breakOverDaysCount + ", "
	// + StringUtilityBelt.toStringCompactWithoutCount(this.trajectoryID).replaceAll(",", "_") + ", "
	// + lat.toString().replaceAll(",", "_") + ", " + lon.toString().replaceAll(",", "_") + ", "
	// + alt.toString().replaceAll(",", "_");
	//
	// // +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude
	// // +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude
	// // +",avgAl:"+
	// }
	
	/**
	 * returns values are in sequence, "timestamp,
	 * endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,#distinctTrajIDs,#TrajIDs,trajID,,latitude,longitude,alt" Timestamps strings changed to GMT
	 * string on April 7 2016
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
			endTimestampString = (new Timestamp(this.timestamp.getTime() + this.durationInSeconds * 1000 - 1000)).toGMTString();
		
		return timestamp.toGMTString() + " , " + endTimestampString + ", " + mode + ", " + this.differenceWithNextInSeconds + ", "
				+ this.durationInSeconds + ", " + this.breakOverDaysCount + ", " + "," + this.getNumberOfDistinctTrajectoryIDs() + ","
				+ this.getNumberOfTrajectoryIDs() + "," + this.trajectoryID.toString().replaceAll(",", "_") + ", "
				+ lat.toString().replaceAll(",", "_") + ", " + lon.toString().replaceAll(",", "_") + ", "
				+ alt.toString().replaceAll(",", "_");
		
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
		
		return timestamp.toGMTString() + ", " + mode + ", " + lat.toString() + ", " + lon.toString() + ", " + alt.toString();
		// +", sl:"+startLatitude+", slo:"+startLongitude+", sal:"+startAltitude // +", el:"+endLatitude+", slo:"+endLongitude+", sal:"+endAltitude // +",avgAl:"+
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
	
	public void addTimestamp(Timestamp t)
	{
		this.timestamps.add(t);
	}
	
	public void setTimestamps(ArrayList<Timestamp> t)
	{
		this.timestamps.clear();
		this.timestamps.addAll(t);
	}
	
	public ArrayList<Timestamp> getTimestamps()
	{
		return this.timestamps;
	}
	
	/**
	 * 
	 * @param lat
	 */
	public void addLatitude(String lat)
	{
		this.lat.add(UtilityBelt.round(lat, 6));
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
		this.lon.add(UtilityBelt.round(lon, 6));
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
	
	// StringUtilityBelt.toStringCompactWithCount(stayPoint.getTrajectoryID(), "_");
	/**
	 * Used to use trajID as a key....
	 * 
	 * @param te
	 * @return
	 */
	public static String getTrajectoryIDsAsCompactWithCount(TrajectoryEntry te)
	{
		return StringUtils.toStringCompactWithCount(te.getTrajectoryID(), "_");
	}
	
	/**
	 * Used to use trajID as a key....
	 * 
	 * @param te
	 * @return
	 */
	public static String getTrajectoryIDsAsCompactWithoutCount(TrajectoryEntry te)
	{
		return StringUtils.toStringCompactWithoutCount(te.getTrajectoryID(), "_");
	}
	
	/**
	 * Returns a trajectory ID /** Returns distinct trajectory IDs separated by delimiter.
	 * 
	 * i.e., if there is only one distinct trajectory Id in the the arraylist of trajectory IDs, else returns the distinct trajIDs separated by delimiter. There is an arraylist of
	 * trajectory ids in the first place to allow for multiple trajectory entries (with same or difference trajectory IDs) to be merged.
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
	
	public double getAvgPosAlts()
	{
		return this.getAvgPosValues(alt);
	}
	
	public double getAvgPosLats()
	{
		return this.getAvgPosValues(lat);
	}
	
	public double getAvgPosLons()
	{
		return this.getAvgPosValues(lon);
	}
	
	public static double getAvgPosValues(ArrayList<String> vals)
	{
		double sum = 0;
		double count = 0;
		for (int i = 0; i < vals.size(); i++)
		{
			if (Double.valueOf(vals.get(i)) > 0)
			{
				sum += Double.valueOf(vals.get(i));
				count++;
			}
		}
		return sum / count;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getAvgDistanceFromCentroidInKms()
	{
		double res = 0;
		
		double centreLat = getAvgPosLats();
		double centreLon = getAvgPosLons();
		
		double numOfLatsLons = lat.size();
		
		for (int i = 0; i < numOfLatsLons; i++)
		{
			res = res + UtilityBelt.haversine(String.valueOf(centreLat), String.valueOf(centreLon), lat.get(i), lon.get(i), false);
		}
		
		return (res / numOfLatsLons);
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
	
	/**
	 * Used for creating staypoints by merging data pointss
	 * 
	 * @param tleA
	 *            preceeding trajectory entry
	 * @param tleB
	 *            succeeding trajectory entry
	 * @return
	 * @throws Exception
	 */
	public static TrajectoryEntry mergeWithoutQuestion(TrajectoryEntry tleA, TrajectoryEntry tleB) throws Exception
	{
		
		// System.out.println("Inside mergeWithoutQuestion: merging teA with " + tleA.getTrajectoryID().size() + " trajIDs and teB with "
		// + tleB.getTrajectoryID().size());
		
		TrajectoryEntry newTle = null;
		
		long newDifferenceWithNextInSeconds = tleA.differenceWithNextInSeconds + tleB.differenceWithNextInSeconds;
		
		long newDurationInSeconds = tleA.durationInSeconds + tleB.durationInSeconds;
		tleA.getTrajectoryID().addAll(tleB.getTrajectoryID());
		tleA.getTimestamps().addAll(tleB.getTimestamps());
		// String newExtraComments = tleA.extraComments + "__" + tleB.extraComments;
		
		tleA.getLatitude().addAll(tleB.getLatitude());
		tleA.getLongitude().addAll(tleB.getLongitude());
		tleA.getAltitude().addAll(tleB.getAltitude());
		
		newTle = new TrajectoryEntry("", tleA.getLatitude(), tleA.getLongitude(), tleA.getAltitude(), tleA.getTrajectoryID(),
				tleA.getTimestamps(), tleA.getTimestamps().get(0), newDifferenceWithNextInSeconds, newDurationInSeconds, -99); // note break over days count is -99 as it is
																																// not relevant here.
		// making a copy
		
		return newTle;
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
