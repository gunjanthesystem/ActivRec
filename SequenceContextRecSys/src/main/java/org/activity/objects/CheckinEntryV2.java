package org.activity.objects;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

import org.activity.util.DateTimeUtils;

public class CheckinEntryV2 extends CheckinEntry
{

	private static final long serialVersionUID = 5017800993648746875L;

	private double distanceInMeterFromNextCheckin;

	private long durationInSecsFromNextCheckin;
	String tz;

	public CheckinEntryV2()
	{
		super();
	}

	/**
	 * For recreating CheckinEntryV2 Object, and cols to conform (but with more cols) to the previous Mar2018 merged
	 * data so that the same R subsetting code is valid while still having complete information to recreate the object.
	 * 
	 * @since April 8 2018
	 * @return
	 */
	public String toStringForRecreating()
	{
		return (this.getUserID() + "," + this.getLocationIDs('_') + "," + this.getTimestamp() + ','
				+ String.join("_", this.getStartLats()) + "," + String.join("_", this.getStartLons()) + ","
				+ this.getActivityID() + "," + this.getWorkingLevelCatIDs() + "," + this.getDistanceInMetersFromPrev()
				+ "," + this.getDurationInSecsFromPrev() + ","
				+ String.join("_", Arrays.asList(this.getLevelWiseCatIDs())) + ","
				+ this.getDistanceInMeterFromNextCheckin() + "," + this.getDurationInSecsFromNextCheckin() + ","
				+ this.getTz() + "," + this.locationIDs.get(0) + "," + this.getStartLatitude() + ","
				+ this.getStartLongitude() + ","
				+ DateTimeUtils.getLocalDate(this.getTimestamp(), TimeZone.getTimeZone("UTC")));
	}

	/**
	 * For recreating CheckinEntryV2 Object, and cols to conform (but with more cols) to the previous Mar2018 merged
	 * data so that the same R subsetting code is valid.
	 * 
	 * @since April 8 2018
	 * 
	 * @return
	 */
	public static String headerForRecreating()
	{
		// return
		// "userID,LocationIDs,ts,Lats,Lons,ActID,WorkingLevelCatID,DistInMFromPrev,DurInSecFromPrev,levelWiseCatID,DistInMFromNextCheckin"
		// + "DurInSecFromNextCheckin,TZ";
		return "userID,placeID,timestamp,Lats,Lons,activityID,workingLevelCatID,distanceInMetersFromPrev,"
				+ "durationInSecsFromPrev,levelWiseCatID,distanceInMeterFromNextCheckin,"
				+ "durationInSecsFromNextCheckin,tz,firstLocID,firstLat,firstLon,Date";

	}

	/**
	 * Most useful, used
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
	 * @param levelWiseCatIDs
	 */
	public CheckinEntryV2(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrevCheckin,
			long durationInSecsFromPrevCheckin, String[] levelWiseCatIDs, double distanceInMeterFromNextCheckin,
			long durationInSecsFromNextCheckin, String tz)
	{
		super(userID, locationID, ts, latitude, longitude, catID, workingLevelCatIDs, distanceInMetersFromPrevCheckin,
				durationInSecsFromPrevCheckin, levelWiseCatIDs);
		this.distanceInMeterFromNextCheckin = distanceInMeterFromNextCheckin;
		this.durationInSecsFromNextCheckin = durationInSecsFromNextCheckin;
		this.tz = tz;
	}

	/**
	 * 
	 * @param userID
	 * @param locationIDs
	 * @param ts
	 * @param latitudes
	 * @param longitudes
	 * @param catID
	 * @param workingLevelCatIDs
	 * @param distanceInMetersFromPrev
	 * @param durationInSecsFromPrev
	 * @param levelWiseCatIDs
	 * @param distanceInMeterFromNextCheckin
	 * @param durationInSecsFromNextCheckin
	 * @param tz
	 */
	public CheckinEntryV2(String userID, ArrayList<Integer> locationIDs, Timestamp ts, ArrayList<String> latitudes,
			ArrayList<String> longitudes, Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrev,
			long durationInSecsFromPrev, String[] levelWiseCatIDs, double distanceInMeterFromNextCheckin,
			long durationInSecsFromNextCheckin, String tz)
	{
		super(userID, locationIDs, ts, latitudes, longitudes, catID, workingLevelCatIDs, distanceInMetersFromPrev,
				durationInSecsFromPrev, levelWiseCatIDs);
		this.distanceInMeterFromNextCheckin = distanceInMeterFromNextCheckin;
		this.durationInSecsFromNextCheckin = durationInSecsFromNextCheckin;
		this.tz = tz;
	}

	public CheckinEntryV2(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs)
	{
		super(userID, locationID, ts, latitude, longitude, catID, workingLevelCatIDs);
	}

	public CheckinEntryV2(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
			Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrevCheckin,
			long durationInSecsFromPrevCheckin)
	{
		super(userID, locationID, ts, latitude, longitude, catID, workingLevelCatIDs, distanceInMetersFromPrevCheckin,
				durationInSecsFromPrevCheckin);
	}

	public CheckinEntryV2(String userID, ArrayList<Integer> locationIDs, Timestamp ts, ArrayList<String> latitudes,
			ArrayList<String> longitudes, Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrev,
			long durationInSecsFromPrev)
	{
		super(userID, locationIDs, ts, latitudes, longitudes, catID, workingLevelCatIDs, distanceInMetersFromPrev,
				durationInSecsFromPrev);
	}

	public CheckinEntryV2(Timestamp timestamp, long differenceWithNextInSeconds, long durationInSeconds,
			ArrayList<String> trajectoryID, String extraComments, int breakOverDaysCount)
	{
		super(timestamp, differenceWithNextInSeconds, durationInSeconds, trajectoryID, extraComments,
				breakOverDaysCount);
	}

	public double getDistanceInMeterFromNextCheckin()
	{
		return distanceInMeterFromNextCheckin;
	}

	public long getDurationInSecsFromNextCheckin()
	{
		return durationInSecsFromNextCheckin;
	}

	public String getTz()
	{
		return tz;
	}

	@Override
	public String toStringWithoutHeaders()
	{
		// return this.userID + "," + this.timestamp + "," + this.activityID + "," + this.getLocationIDs('_') + ","
		// + this.distanceInMetersFromPrevCheckin + "," + this.durationInSecsFromPrevCheckin;
		return super.toStringWithoutHeaders() + "," + this.distanceInMeterFromNextCheckin + ","
				+ this.durationInSecsFromNextCheckin + "," + this.tz + "," + this.getLocationIDs().get(0) + ","
				+ this.getStartLatitude() + "," + this.getStartLongitude() + "," + this.getWorkingLevelCatIDs() + ","
				+ DateTimeUtils.getLocalDate(this.getTimestamp(), TimeZone.getTimeZone("UTC"));
	}

	/**
	 * returns correspnding header for toStringWithoutHeaders()
	 * 
	 * @return "userID,timestamp,activityID,distanceInMetersFromPrev,durationInSecsFromPrev"
	 */
	public static String getHeaderToWrite()
	{
		return CheckinEntry.getHeaderToWrite()
				+ ",distanceInMeterFromNextCheckin,durationInSecsFromNextCheckin,tz,firstLocID,firstLat,firstLon,workingLevelCatID,Date";
		// return super.
		// "userID,timestamp,activityID,placeID,distanceInMetersFromPrev,durationInSecsFromPrev";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(distanceInMeterFromNextCheckin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (durationInSecsFromNextCheckin ^ (durationInSecsFromNextCheckin >>> 32));
		result = prime * result + ((tz == null) ? 0 : tz.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		CheckinEntryV2 other = (CheckinEntryV2) obj;
		if (Double.doubleToLongBits(distanceInMeterFromNextCheckin) != Double
				.doubleToLongBits(other.distanceInMeterFromNextCheckin))
			return false;
		if (durationInSecsFromNextCheckin != other.durationInSecsFromNextCheckin) return false;
		if (tz == null)
		{
			if (other.tz != null) return false;
		}
		else if (!tz.equals(other.tz)) return false;
		return true;
	}

}
