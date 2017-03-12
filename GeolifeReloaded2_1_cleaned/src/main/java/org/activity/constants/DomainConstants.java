package org.activity.constants;

/**
 * 
 * @author gunjan
 *
 */
public class DomainConstants
{

	/**
	 * Note: 'userID' (ignore case) always refers to the raw user-ids. While index of user id refers to user 1, user
	 * 2,... or user 0, user 1, ... user-ids.
	 */
	public static final int tenUserIDsGeolifeData[] = { 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 };
	public static final int allUserIDsGeolifeData[] = { 62, 84, 52, 68, 167, 179, 153, 85, 128, 10, 105, /* 78, */67,
			126, 64, 111, 163, 98, 154, 125, 65, 80, 21, 69, /* 101, 175, */81, 96, 129, /* 115, */56, 91, 58, 82, 141,
			112, 53, 139, 102, 20, 138, 108, 97, /* 92, 75, */
			161, 117, 170 /* ,114, 110, 107 */ };
	public static final int userIDsDCUData[] = { 0, 1, 2, 3, 4 };
	public static final String userNamesDCUData[] = { "Stefan", "Tengqi", "Cathal", "Zaher", "Rami" };
	static final String[] GeolifeActivityNames = { "Not Available", "Unknown", "airplane", "bike", "boat", "bus", "car",
			"motorcycle", "run", "subway", "taxi", "train", "walk" };
	// static final String[] GeolifeActivityNames = { "Not Available", "Unknown", /* "airplane", */"bike", /* "boat",
	// */"bus", "car", /* "motorcycle", *//* "run", */// "subway", "taxi", "train", "walk" };
	static final String[] gowallaActivityNames = null;
	static final String[] DCUDataActivityNames = { "Others", "Unknown", "Commuting", "Computer", "Eating", "Exercising",
			"Housework", "On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV" };
	/**
	 * Most active users in the geolife dataset
	 */

	public static final int above10RTsUserIDsGeolifeData[] = { 62, 84, 52, 68, 167, 179, 153, 85, 128, 10, 126, 111,
			163, 65, 91, 82, 139, 108 };
	public static final int gowallaWorkingCatLevel = 2; // -1 indicates original working cat
	static int[] gowallaUserIDs = null;
	public static String[] featureNames = { "ActivityName", "StartTime", "Duration", "DistanceTravelled",
	"StartGeoCoordinates", "EndGeoCoordinates", "AvgAltitude" };

	public DomainConstants()
	{
	}

}
