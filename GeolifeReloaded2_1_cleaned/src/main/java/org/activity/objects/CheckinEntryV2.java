// package org.activity.objects;
//
// import java.sql.Timestamp;
// import java.util.ArrayList;
//
// public class CheckinEntryV2 extends CheckinEntry
// {
// private double distanceInMetersFromNext;
//
// public double getDistanceInMetersFromNext()
// {
// return distanceInMetersFromNext;
// }
//
// public long getDurationInSecsFromNext()
// {
// return durationInSecsFromNext;
// }
//
// private long durationInSecsFromNext;
//
// public int getFirstLocalID()
// {
// return locationIDs.get(0);
// }
//
// public CheckinEntryV2(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
// Integer catID, String workingLevelCatIDs)
// {
// super(userID, locationID, ts, latitude, longitude, catID, workingLevelCatIDs);
// }
//
// public CheckinEntryV2(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
// Integer catID, String workingLevelCatIDs, double distanceInMetersFromNext, long durationInSecsFromNext)
// {
// super(userID, locationID, ts, latitude, longitude, catID, workingLevelCatIDs, distanceInMetersFromNext,
// durationInSecsFromNext);
// }
//
// public CheckinEntryV2(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
// Integer catID, String workingLevelCatIDs, double distanceInMetersFromNext, long durationInSecsFromNext,
// String[] levelWiseCatIDs)
// {
// super(userID, locationID, ts, latitude, longitude, catID, workingLevelCatIDs, distanceInMetersFromNext,
// durationInSecsFromNext, levelWiseCatIDs);
// }
//
// public CheckinEntryV2(String userID, ArrayList<Integer> locationIDs, Timestamp ts, ArrayList<String> latitudes,
// ArrayList<String> longitudes, Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrev,
// long durationInSecsFromPrev)
// {
// super(userID, locationIDs, ts, latitudes, longitudes, catID, workingLevelCatIDs, distanceInMetersFromPrev,
// durationInSecsFromPrev);
// }
//
// public CheckinEntryV2(String userID, ArrayList<Integer> locationIDs, Timestamp ts, ArrayList<String> latitudes,
// ArrayList<String> longitudes, Integer catID, String workingLevelCatIDs, double distanceInMetersFromPrev,
// long durationInSecsFromPrev, String[] levelWiseCatIDs)
// {
// super(userID, locationIDs, ts, latitudes, longitudes, catID, workingLevelCatIDs, distanceInMetersFromPrev,
// durationInSecsFromPrev, levelWiseCatIDs);
// }
//
// public CheckinEntryV2(Timestamp timestamp, long differenceWithNextInSeconds, long durationInSeconds,
// ArrayList<String> trajectoryID, String extraComments, int breakOverDaysCount)
// {
// super(timestamp, differenceWithNextInSeconds, durationInSeconds, trajectoryID, extraComments,
// breakOverDaysCount);
// }
//
// public CheckinEntryV2()
// {
// }
//
// }
