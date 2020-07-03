package org.activity.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.activity.stats.StatsUtils;

/**
 * For storing checkin entries for Gowalla dataset
 * 
 * @author gunjan
 *
 */
public class CheckinEntry extends DataEntry implements Serializable
{
	///////// start of data members in DataEntry///////////
	// protected Timestamp timestamp;
	// protected long differenceWithNextInSeconds, durationInSeconds;
	// protected ArrayList<String> trajectoryID;
	// protected String extraComments; // some extra information which need to be stores
	// /**
	// * This may or may not be relvant dependent on whether the activities are broken over days
	// */
	// protected int breakOverDaysCount;
	///////// end of data members in DataEntry///////////

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userID, workingLevelCatIDs; // note: working level catid can be multiple "36_60"
	// private int locationID;
	// private String startLatitude;
	// private String startLongitude;

	ArrayList<Integer> locationIDs; // needed for merged checkin entries
	ArrayList<String> startLats, startLons; // needed for merged checkin entries

	/**
	 * Category ID (direct)
	 */
	private int activityID;

	/**
	 * [0] level 1 catids, [1] level 2 catid
	 */
	private String[] levelWiseCatIDs;

	private double distanceInMetersFromPrevCheckin;
	private long durationInSecsFromPrevCheckin;

	// private int numOfCheckins; // num of checkins this checkin is composed of, this is relevant when multiple
	// checkins
	// // are merged to form one checkin

	/**
	 * <p>
	 * // note: 6 decimal places offers the precision upto .11m //
	 * ref:http://dpstyles.tumblr.com/post/95952859425/how-much-does-the-precision-of-a-latlong-change //
	 * https://en.wikipedia.org/wiki/Decimal_degrees
	 * </p>
	 * 
	 * @param userID
	 * @param locationID
	 * @param ts
	 * @param latitude
	 * @param longitude
	 * @param catID
	 * @param workingLevelCatIDs
	 */
	public CheckinEntry(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs)
	{
		this.userID = userID;
		this.locationIDs = new ArrayList<Integer>();
		this.locationIDs.add((locationID));
		this.timestamp = ts;

		this.startLats = new ArrayList<String>();
		this.startLons = new ArrayList<String>();

		this.startLats.add(StatsUtils.round(latitude, 6));
		this.startLons.add(StatsUtils.round(longitude, 6));

		this.activityID = catID;
		this.setWorkingLevelCatIDs(workingLevelCatIDs);
	}

	/**
	 * <font color = green>Most useful constructor</font>
	 * 
	 * @param userID
	 * @param locationID
	 * @param ts
	 * @param latitude
	 * @param longitude
	 * @param catID
	 * @param workingLevelCatIDs
	 * @param distanceInMetersFromPrevCheckin
	 * @param durationInSecsFromPrevCheckin
	 */
	public CheckinEntry(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrevCheckin,
			long durationInSecsFromPrevCheckin)
	{
		this.userID = userID;
		this.locationIDs = new ArrayList<Integer>();
		this.locationIDs.add((locationID));
		this.timestamp = ts;

		this.startLats = new ArrayList<String>();
		this.startLons = new ArrayList<String>();

		this.startLats.add(StatsUtils.round(latitude, 6));
		this.startLons.add(StatsUtils.round(longitude, 6));

		this.activityID = catID;
		this.setWorkingLevelCatIDs(workingLevelCatIDs);

		this.distanceInMetersFromPrevCheckin = distanceInMetersFromPrevCheckin;
		this.durationInSecsFromPrevCheckin = durationInSecsFromPrevCheckin;
	}

	/**
	 * <font color = green>Most useful constructor</font>
	 * <p>
	 * Currently used March 2018
	 * 
	 * @param userID
	 * @param locationID
	 * @param ts
	 * @param latitude
	 * @param longitude
	 * @param catID
	 * @param workingLevelCatIDs
	 * @param distanceInMetersFromPrevCheckin
	 * @param durationInSecsFromPrevCheckin
	 */
	public CheckinEntry(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrevCheckin,
			long durationInSecsFromPrevCheckin, String[] levelWiseCatIDs)
	{
		this.userID = userID;
		this.locationIDs = new ArrayList<Integer>();
		this.locationIDs.add((locationID));
		this.timestamp = ts;

		this.startLats = new ArrayList<String>();
		this.startLons = new ArrayList<String>();

		this.startLats.add(StatsUtils.round(latitude, 6));
		this.startLons.add(StatsUtils.round(longitude, 6));

		this.activityID = catID;
		this.setWorkingLevelCatIDs(workingLevelCatIDs);

		this.distanceInMetersFromPrevCheckin = distanceInMetersFromPrevCheckin;
		this.durationInSecsFromPrevCheckin = durationInSecsFromPrevCheckin;

		this.levelWiseCatIDs = levelWiseCatIDs;
	}

	/**
	 * <font color = green>Most useful constructor</font>
	 * <p>
	 * Used to created merged checkin entry
	 * 
	 * @param userID
	 * @param locationID
	 * @param ts
	 * @param latitude
	 * @param longitude
	 * @param catID
	 * @param workingLevelCatIDs
	 * @param distanceInMetersFromNext
	 * @param durationInSecsFromNext
	 */
	public CheckinEntry(String userID, ArrayList<Integer> locationIDs, Timestamp ts, ArrayList<String> latitudes,
			ArrayList<String> longitudes, Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrev,
			long durationInSecsFromPrev)
	{
		this.userID = userID;
		this.locationIDs = locationIDs;
		this.timestamp = ts;

		this.startLats = latitudes;
		this.startLons = longitudes;

		this.activityID = catID;
		this.setWorkingLevelCatIDs(workingLevelCatIDs);

		this.distanceInMetersFromPrevCheckin = distanceInMetersFromPrev;
		this.durationInSecsFromPrevCheckin = durationInSecsFromPrev;
	}

	/**
	 * <font color = green>Most useful constructor</font> Used to created merged checkin entry
	 * <p>
	 * Used as of April 4 2018
	 * 
	 * @param userID
	 * @param locationID
	 * @param ts
	 * @param latitude
	 * @param longitude
	 * @param catID
	 * @param workingLevelCatIDs
	 * @param distanceInMetersFromNext
	 * @param durationInSecsFromNext
	 */
	public CheckinEntry(String userID, ArrayList<Integer> locationIDs, Timestamp ts, ArrayList<String> latitudes,
			ArrayList<String> longitudes, Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrev,
			long durationInSecsFromPrev, String[] levelWiseCatIDs)
	{
		this.userID = userID;
		this.locationIDs = locationIDs;
		this.timestamp = ts;

		this.startLats = latitudes;
		this.startLons = longitudes;

		this.activityID = catID;
		this.setWorkingLevelCatIDs(workingLevelCatIDs);

		this.distanceInMetersFromPrevCheckin = distanceInMetersFromPrev;
		this.durationInSecsFromPrevCheckin = durationInSecsFromPrev;

		this.levelWiseCatIDs = levelWiseCatIDs;
	}

	/**
	 * <font color = orange>currently not called</font>
	 * 
	 * @param timestamp
	 * @param differenceWithNextInSeconds
	 * @param durationInSeconds
	 * @param trajectoryID
	 * @param extraComments
	 * @param breakOverDaysCount
	 */
	public CheckinEntry(Timestamp timestamp, long differenceWithNextInSeconds, long durationInSeconds,
			ArrayList<String> trajectoryID, String extraComments, int breakOverDaysCount)
	{
		super(timestamp, differenceWithNextInSeconds, durationInSeconds, trajectoryID, extraComments,
				breakOverDaysCount);
	}

	// public CheckinEntry(Timestamp t, long durationInSeconds, String mod)
	// {
	// super(t, durationInSeconds, mod);
	// }

	/**
	 * 
	 */
	public CheckinEntry()
	{
		super();
	}

	public String getUserID()
	{
		return userID;
	}

	/**
	 * [0] level 1 catids, [1] level 2 catid
	 * 
	 * @return
	 */
	public String[] getLevelWiseCatIDs()
	{
		return levelWiseCatIDs;
	}

	// /**
	// *
	// * @return the first location id
	// */
	// public String getLocationID()
	// {
	// return this.locationIDs.get(0);// return this.lo;
	// }

	/**
	 * 
	 * @return first lat
	 */
	public String getStartLatitude()
	{
		return this.startLats.get(0);
	}

	/**
	 * 
	 * @return first lon
	 */
	public String getStartLongitude()
	{
		return this.startLons.get(0);
	}

	/**
	 * Category ID (direct)
	 * 
	 * @return
	 */
	public int getActivityID()
	{
		return activityID;
	}

	@Override
	public String toString()
	{
		return "CheckinEntry [userID=" + userID + ", workingLevelCatIDs=" + workingLevelCatIDs + ", locationIDs="
				+ locationIDs + ", startLats=" + startLats + ", startLons=" + startLons + ", activityID=" + activityID
				+ ", distanceInMetersFromPrev=" + distanceInMetersFromPrevCheckin + ", durationInSecsFromPrev="
				+ durationInSecsFromPrevCheckin + "]";
	}

	@Override
	public String toStringWithTrajID()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringWithTrajIDWithTrajPurityCheck()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringWithTrajIDsInfo()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringWithoutHeaders()
	{
		return this.userID + "," + this.timestamp + "," + this.activityID + "," + this.getLocationIDs('_') + ","
				+ this.distanceInMetersFromPrevCheckin + "," + this.durationInSecsFromPrevCheckin;
	}

	/**
	 * returns correspnding header for toStringWithoutHeaders()
	 * 
	 * @return "userID,timestamp,activityID,distanceInMetersFromPrev,durationInSecsFromPrev"
	 */
	public static String getHeaderToWrite()
	{
		return "userID,timestamp,activityID,placeID,distanceInMetersFromPrev,durationInSecsFromPrev";
	}

	@Override
	public String toStringWithoutHeadersWithTrajID()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringWithoutHeadersWithTrajID(String delimiter)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringWithoutHeadersWithTrajIDPurityCheck()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringEssentialsWithoutHeaders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @return distanceInMetersFromPrevCheckin
	 */
	public double getDistanceInMetersFromPrev()
	{
		return distanceInMetersFromPrevCheckin;
	}

	public void setDistanceInMetersFromPrev(double distanceInMetersFromNext)
	{
		this.distanceInMetersFromPrevCheckin = distanceInMetersFromNext;
	}

	/**
	 * 
	 * @return durationInSecsFromPrevCheckin
	 */
	public long getDurationInSecsFromPrev()
	{
		return durationInSecsFromPrevCheckin;
	}

	public void setDurationInSecsFromPrev(long durationInSecsFromNext)
	{
		this.durationInSecsFromPrevCheckin = durationInSecsFromNext;
	}

	public String getWorkingLevelCatIDs()
	{
		return workingLevelCatIDs;
	}

	public void setWorkingLevelCatIDs(String workingLevelCatIDs)
	{
		this.workingLevelCatIDs = workingLevelCatIDs;
	}

	public ArrayList<Integer> getLocationIDs()
	{
		return locationIDs;
	}

	public String getLocationIDs(char delimiter)
	{
		StringBuilder sb = new StringBuilder();
		locationIDs.stream().forEach(e -> sb.append(String.valueOf(e) + delimiter));
		return sb.toString();
	}

	public void setLocationIDs(ArrayList<Integer> locationIDs)
	{
		this.locationIDs = locationIDs;
	}

	public ArrayList<String> getStartLats()
	{
		return startLats;
	}

	public void setStartLats(ArrayList<String> startLats)
	{
		this.startLats = startLats;
	}

	public ArrayList<String> getStartLons()
	{
		return startLons;
	}

	public void setStartLons(ArrayList<String> startLons)
	{
		this.startLons = startLons;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + activityID;
		long temp;
		temp = Double.doubleToLongBits(distanceInMetersFromPrevCheckin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (durationInSecsFromPrevCheckin ^ (durationInSecsFromPrevCheckin >>> 32));
		result = prime * result + ((locationIDs == null) ? 0 : locationIDs.hashCode());
		result = prime * result + ((startLats == null) ? 0 : startLats.hashCode());
		result = prime * result + ((startLons == null) ? 0 : startLons.hashCode());
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		result = prime * result + ((workingLevelCatIDs == null) ? 0 : workingLevelCatIDs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CheckinEntry other = (CheckinEntry) obj;
		if (activityID != other.activityID) return false;
		if (Double.doubleToLongBits(distanceInMetersFromPrevCheckin) != Double
				.doubleToLongBits(other.distanceInMetersFromPrevCheckin))
			return false;
		if (durationInSecsFromPrevCheckin != other.durationInSecsFromPrevCheckin) return false;
		if (locationIDs == null)
		{
			if (other.locationIDs != null) return false;
		}
		else if (!locationIDs.equals(other.locationIDs)) return false;
		if (startLats == null)
		{
			if (other.startLats != null) return false;
		}
		else if (!startLats.equals(other.startLats)) return false;
		if (startLons == null)
		{
			if (other.startLons != null) return false;
		}
		else if (!startLons.equals(other.startLons)) return false;
		if (userID == null)
		{
			if (other.userID != null) return false;
		}
		else if (!userID.equals(other.userID)) return false;
		if (workingLevelCatIDs == null)
		{
			if (other.workingLevelCatIDs != null) return false;
		}
		else if (!workingLevelCatIDs.equals(other.workingLevelCatIDs)) return false;
		return true;
	}

}
