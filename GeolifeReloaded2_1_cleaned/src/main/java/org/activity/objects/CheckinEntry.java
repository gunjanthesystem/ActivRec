package org.activity.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.activity.util.StatsUtils;

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

	ArrayList<String> locationIDs; // needed for merged checkin entries
	ArrayList<String> startLats, startLons; // needed for merged checkin entries
	/**
	 * Category ID (direct)
	 */
	private int activityID;

	private double distanceInMetersFromPrev;
	private long durationInSecsFromPrev;

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
		this.locationIDs = new ArrayList<String>();
		this.locationIDs.add(String.valueOf(locationID));
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
	 * @param distanceInMetersFromNext
	 * @param durationInSecsFromNext
	 */
	public CheckinEntry(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs, double distanceInMetersFromNext, long durationInSecsFromNext)
	{
		this.userID = userID;
		this.locationIDs = new ArrayList<String>();
		this.locationIDs.add(String.valueOf(locationID));
		this.timestamp = ts;

		this.startLats = new ArrayList<String>();
		this.startLons = new ArrayList<String>();

		this.startLats.add(StatsUtils.round(latitude, 6));
		this.startLons.add(StatsUtils.round(longitude, 6));

		this.activityID = catID;
		this.setWorkingLevelCatIDs(workingLevelCatIDs);

		this.distanceInMetersFromPrev = distanceInMetersFromNext;
		this.durationInSecsFromPrev = durationInSecsFromNext;
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
	 * @param distanceInMetersFromNext
	 * @param durationInSecsFromNext
	 */
	public CheckinEntry(String userID, ArrayList<String> locationIDs, Timestamp ts, ArrayList<String> latitudes,
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

		this.distanceInMetersFromPrev = distanceInMetersFromPrev;
		this.durationInSecsFromPrev = durationInSecsFromPrev;
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
	 * 
	 * @return the first location id
	 */
	public String getLocationID()
	{
		return this.locationIDs.get(0);// return this.lo;
	}

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

	public int getActivityID()
	{
		return activityID;
	}

	@Override
	public String toString()
	{
		return "CheckinEntry [userID=" + userID + ", workingLevelCatIDs=" + workingLevelCatIDs + ", locationIDs="
				+ locationIDs + ", startLats=" + startLats + ", startLons=" + startLons + ", activityID=" + activityID
				+ ", distanceInMetersFromPrev=" + distanceInMetersFromPrev + ", durationInSecsFromPrev="
				+ durationInSecsFromPrev + "]";
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
		return this.userID + "," + this.timestamp + "," + this.activityID + "," + this.distanceInMetersFromPrev + ","
				+ this.durationInSecsFromPrev;
	}

	/**
	 * returns correspnding header for toStringWithoutHeaders()
	 * 
	 * @return "userID,timestamp,activityID,distanceInMetersFromPrev,durationInSecsFromPrev"
	 */
	public static String getHeaderToWrite()
	{
		return "userID,timestamp,activityID,distanceInMetersFromPrev,durationInSecsFromPrev";
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

	public double getDistanceInMetersFromPrev()
	{
		return distanceInMetersFromPrev;
	}

	public void setDistanceInMetersFromPrev(double distanceInMetersFromNext)
	{
		this.distanceInMetersFromPrev = distanceInMetersFromNext;
	}

	public long getDurationInSecsFromPrev()
	{
		return durationInSecsFromPrev;
	}

	public void setDurationInSecsFromPrev(long durationInSecsFromNext)
	{
		this.durationInSecsFromPrev = durationInSecsFromNext;
	}

	public String getWorkingLevelCatIDs()
	{
		return workingLevelCatIDs;
	}

	public void setWorkingLevelCatIDs(String workingLevelCatIDs)
	{
		this.workingLevelCatIDs = workingLevelCatIDs;
	}

	public ArrayList<String> getLocationIDs()
	{
		return locationIDs;
	}

	public void setLocationIDs(ArrayList<String> locationIDs)
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

}
