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
public class CheckinEntry_backup20Feb2017 extends DataEntry implements Serializable
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
	private int locationID;
	private String startLatitude;
	private String startLongitude;

	ArrayList<Integer> locationIDs; // needed for merged checkin entries
	ArrayList<String> startLats, startLons; // needed for merged checkin entries
	/**
	 * Category ID (direct)
	 */
	private int activityID;

	private double distanceInMetersFromNext;
	private long durationInSecsFromNext;

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
	public CheckinEntry_backup20Feb2017(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs)
	{
		this.userID = userID;
		this.locationID = locationID;
		this.timestamp = ts;

		this.startLatitude = StatsUtils.round(latitude, 6);
		this.startLongitude = StatsUtils.round(longitude, 6);

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
	public CheckinEntry_backup20Feb2017(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs, double distanceInMetersFromNext, long durationInSecsFromNext)
	{
		this.userID = userID;
		this.locationID = locationID;
		this.timestamp = ts;

		this.startLatitude = StatsUtils.round(latitude, 6);
		this.startLongitude = StatsUtils.round(longitude, 6);

		this.activityID = catID;
		this.setWorkingLevelCatIDs(workingLevelCatIDs);

		this.distanceInMetersFromNext = distanceInMetersFromNext;
		this.durationInSecsFromNext = durationInSecsFromNext;
	}

	// /**
	// * <font color = green>Most useful constructor</font>
	// *
	// * @param userID
	// * @param locationID
	// * @param ts
	// * @param latitude
	// * @param longitude
	// * @param catID
	// * @param workingLevelCatIDs
	// * @param distanceInMetersFromNext
	// * @param durationInSecsFromNext
	// */
	// public CheckinEntry(String userID, ArrayList<Integer> locationID, Timestamp ts, ArrayList<String> latitude,
	// ArrayList<String> longitude, Integer catID, String workingLevelCatIDs, double distanceInMetersFromNext,
	// long durationInSecsFromNext)
	// {
	// this.userID = userID;
	// this.locationIDs = locationID;
	// this.timestamp = ts;
	//
	// this.startLatitude = StatsUtils.round(latitude, 6);
	// this.startLongitude = StatsUtils.round(longitude, 6);
	//
	// this.activityID = catID;
	// this.setWorkingLevelCatIDs(workingLevelCatIDs);
	//
	// this.distanceInMetersFromNext = distanceInMetersFromNext;
	// this.durationInSecsFromNext = durationInSecsFromNext;
	// }

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
	public CheckinEntry_backup20Feb2017(Timestamp timestamp, long differenceWithNextInSeconds, long durationInSeconds,
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
	public CheckinEntry_backup20Feb2017()
	{
		super();
	}

	public String getUserID()
	{
		return userID;
	}

	public int getLocationID()
	{
		return locationID;
	}

	public String getStartLatitude()
	{
		return startLatitude;
	}

	public String getStartLongitude()
	{
		return startLongitude;
	}

	public int getActivityID()
	{
		return activityID;
	}

	@Override
	public String toString()
	{
		return null;
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
		// TODO Auto-generated method stub
		return null;
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

	public double getDistanceInMetersFromNext()
	{
		return distanceInMetersFromNext;
	}

	public void setDistanceInMetersFromNext(double distanceInMetersFromNext)
	{
		this.distanceInMetersFromNext = distanceInMetersFromNext;
	}

	public long getDurationInSecsFromNext()
	{
		return durationInSecsFromNext;
	}

	public void setDurationInSecsFromNext(long durationInSecsFromNext)
	{
		this.durationInSecsFromNext = durationInSecsFromNext;
	}

	public String getWorkingLevelCatIDs()
	{
		return workingLevelCatIDs;
	}

	public void setWorkingLevelCatIDs(String workingLevelCatIDs)
	{
		this.workingLevelCatIDs = workingLevelCatIDs;
	}

}
