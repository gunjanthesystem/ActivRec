package org.activity.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.activity.util.UtilityBelt;

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
	private int locationID;
	private String startLatitude;
	private String startLongitude;
	private int activityID;
	
	/**
	 * <p>
	 * // note: 6 decimal places offers the precision upto .11m // ref:http://dpstyles.tumblr.com/post/95952859425/how-much-does-the-precision-of-a-latlong-change //
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
	public CheckinEntry(String userID, Integer locationID, Timestamp ts, String latitude, String longitude, Integer catID,
			String workingLevelCatIDs)
	{
		this.userID = userID;
		this.locationID = locationID;
		this.timestamp = ts;
		
		this.startLatitude = UtilityBelt.round(latitude, 6);
		this.startLongitude = UtilityBelt.round(longitude, 6);
		
		this.activityID = catID;
		this.setWorkingLevelCatIDs(workingLevelCatIDs);
	}
	
	public CheckinEntry(Timestamp timestamp, long differenceWithNextInSeconds, long durationInSeconds, ArrayList<String> trajectoryID,
			String extraComments, int breakOverDaysCount)
	{
		super(timestamp, differenceWithNextInSeconds, durationInSeconds, trajectoryID, extraComments, breakOverDaysCount);
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
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + activityID;
		result = prime * result + locationID;
		result = prime * result + ((startLatitude == null) ? 0 : startLatitude.hashCode());
		result = prime * result + ((startLongitude == null) ? 0 : startLongitude.hashCode());
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CheckinEntry other = (CheckinEntry) obj;
		if (activityID != other.activityID)
			return false;
		if (locationID != other.locationID)
			return false;
		if (startLatitude == null)
		{
			if (other.startLatitude != null)
				return false;
		}
		else if (!startLatitude.equals(other.startLatitude))
			return false;
		if (startLongitude == null)
		{
			if (other.startLongitude != null)
				return false;
		}
		else if (!startLongitude.equals(other.startLongitude))
			return false;
		if (userID == null)
		{
			if (other.userID != null)
				return false;
		}
		else if (!userID.equals(other.userID))
			return false;
		return true;
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
