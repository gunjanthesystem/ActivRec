package org.activity.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.UserGowalla;
import org.activity.ui.PopUps;
import org.activity.util.RegexUtils;
import org.activity.util.StringCode;
import org.apache.commons.lang3.StringUtils;

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

	public static String[] gowallaFeatureNames = { "ActivityName", "StartTime" };

	public static final int gowallaUserIDsWithGT553MaxActsPerDay[] = { 5195, 9298, 9751, 16425, 17012, 18382, 19416,
			19957, 20316, 23150, 28509, 30293, 30603, 42300, 44718, 46646, 74010, 74274, 76390, 79509, 79756, 86755,
			103951, 105189, 106328, 114774, 118023, 136677, 154692, 179386, 194812, 213489, 224943, 235659, 246993,
			251408, 269889, 311530, 338587, 395223, 563986, 624892, 862876, 1722363, 2084969, 2096330, 2103094, 2126604,
			2190642 };

	public static final int gowallaUserIDsWithGT553MaxActsPerDayIndex[] = { 117, 148, 152, 215, 223, 236, 245, 250, 251,
			266, 306, 313, 314, 333, 338, 341, 431, 433, 437, 447, 449, 471, 510, 515, 520, 543, 547, 607, 634, 661,
			678, 696, 714, 729, 741, 749, 761, 792, 816, 841, 855, 861, 869, 894, 902, 907, 912, 921, 934, };

	public static final int gowallaUserIDInUserGroup1Users[] = { 50, 57, 60, 64, 72, 88, 98, 108, 120, 138, 151, 156,
			159, 193, 204, 209, 214, 232, 233, 238, 262, 270, 341, 354, 359, 369, 421, 459, 484, 515, 564, 624, 648,
			691, 702, 705, 708, 721, 776, 790, 825, 888, 938, 943, 990, 1082, 1094, 1152, 1177, 1199, 1276, 1343, 1356,
			1399, 1456, 1462, 1471, 1520, 1556, 1564, 1572, 1665, 1729, 1730, 1774, 1793, 1856, 1903, 1925, 2026, 2028,
			2045, 2046, 2133, 2214, 2224, 2232, 2444, 2479, 2616, 2643, 2691, 2702, 2723, 2732, 2755, 2825, 2839, 2900,
			2962, 2980, 2999, 3006, 3014, 3028, 3050, 3099, 3129, 3186, 3396 };

	public final static int numOfCatLevels = 3;

	public static HashMap<String, Double> catIDsHierarchicalDistance = null;
	public static TreeMap<Integer, String> catIDNameDictionary = null;
	static LinkedHashMap<Integer, LocationGowalla> locIDLocationObjectDictionary = null;
	static LinkedHashMap<String, UserGowalla> userIDUserObjectDictionary = null;

	static LinkedHashMap<Integer, String> locIDNameDictionary = null;
	public static TreeMap<Integer, Character> catIDCharCodeMap = null;
	public static TreeMap<Character, Integer> charCodeCatIDMap = null;

	public static TreeMap<Integer, ArrayList<Integer>> catIDGivenLevelCatIDMap;

	/**
	 * {Cat id, {list of location ids with support that cat (activity)}}
	 */
	static Map<Integer, List<Integer>> catIDSupportingLocIDsMap = null;

	// public static final Integer hierarchyLevelForEditDistance = 1;// is in Constant class

	/**
	 * Labels for groups of 100 users. If label is "101" then users from 100 to 199 are included.
	 */
	public final static String[] gowallaUserGroupsLabels = { "1", "101", "201", "301", "401", "501", "601", "701",
			"801", "901" };//

	public final static String[] gowallaUserGroupsLabelsFixed = { "1", "101", "201", "301", "401", "501", "601", "701",
			"801", "901" };//

	/**
	 * keeping it globally to avoid recomputing for each matching unit
	 * <P>
	 * {UserID, {{PDVal,repAO},{PDVal,{precDuration,succDuration}}}
	 * <P>
	 * NOT USED AT THE MOMENT
	 */
	static LinkedHashMap<Integer, Pair<LinkedHashMap<Integer, ActivityObject>, LinkedHashMap<Integer, Pair<Double, Double>>>> userIDRepAOResultMap;

	/////////////////////////////////////////////////////////////

	public static boolean isActNameTheActID()
	{
		if (Constant.getDatabaseName().equals("gowalla1"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 
	 * @param uid
	 * @return
	 */
	public static boolean isGowallaUserIDsWithGT553MaxActsPerDay(int uid)
	{
		boolean found = false;
		for (int i : gowallaUserIDsWithGT553MaxActsPerDay)
		{
			if (i == uid)
			{
				found = true;
			}
		}
		return found;
	}

	/**
	 * NOT USED AT THE MOMENT
	 * 
	 * @param userID
	 */
	public static void setUserIDRepAOResultMap(int userID)
	{
		if (userIDRepAOResultMap.containsKey(userID))
		{
			System.out.println("userIDRepAOResultMap had already been created for user=" + userID);
		}
		else
		{

		}

	}

	public static void main(String args[])
	{
		// getGivenLevelCatIDForAllCatIDs(pathToSerialisedLevelWiseCatIDsDict, 1, true);

		// StringBuilder sb = new StringBuilder();
		// for (Entry<Integer, LinkedHashMap<Integer, Double>> e1 : locIDsHaversineDists.entrySet())
		// {
		// sb.append(e1.getKey() + ",");
		// for (Entry<Integer, Double> e2 : e1.getValue().entrySet())
		// {
		// sb.append(e2.getKey() + "," + e2.getValue() + "\n");
		// }
		// }
		// WritingToFile.appendLineToFileAbsolute(sb.toString(),
		// "./dataWritten/locationDistances/locationDistances.csv");
	}

	/**
	 * 
	 */
	public static void setCatIDCharCodeMap()
	{
		try
		{
			if (catIDNameDictionary == null)
			{
				PopUps.getTracedErrorMsg("catIDNameDictionary is null");
				System.exit(-2);
			}

			catIDCharCodeMap = new TreeMap<>();// HashBiMap.create(catIDNameDictionary.size());
			charCodeCatIDMap = new TreeMap<>();
			// new HashBiMap<Integer, Character>();
			for (Integer actID : catIDNameDictionary.keySet())
			{
				catIDCharCodeMap.put(actID, StringCode.getCharCodeFromActivityID(actID));
				charCodeCatIDMap.put(StringCode.getCharCodeFromActivityID(actID), actID);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void setCatIDNameDictionary(String pathToSerialisedCatIDNameDictionary)
	{
		try
		{
			catIDNameDictionary = (TreeMap<Integer, String>) Serializer
					.kryoDeSerializeThis(pathToSerialisedCatIDNameDictionary);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param pathToSerialisedLocationObjects
	 */
	public static void setUserIDUserObjectDictionary(String pathToSerialisedUserObjects)
	{
		try
		{
			userIDUserObjectDictionary = (LinkedHashMap<String, UserGowalla>) Serializer
					.kryoDeSerializeThis(pathToSerialisedUserObjects);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static LinkedHashMap<String, UserGowalla> getUserIDUserObjectDictionary()
	{
		return userIDUserObjectDictionary;
	}

	/**
	 * 
	 * @param pathToSerialisedLocationObjects
	 */
	public static void setLocIDLocationObjectDictionary(String pathToSerialisedLocationObjects)
	{
		try
		{
			locIDLocationObjectDictionary = (LinkedHashMap<Integer, LocationGowalla>) Serializer
					.kryoDeSerializeThis(pathToSerialisedLocationObjects);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void setLocationIDNameDictionary(String pathToSerialisedLocationObjects)
	{
		try
		{
			LinkedHashMap<Integer, LocationGowalla> locIDLocationObjectDictionaryTemp = (LinkedHashMap<Integer, LocationGowalla>) Serializer
					.kryoDeSerializeThis(pathToSerialisedLocationObjects);

			LinkedHashMap<Integer, String> locIDNameDict = new LinkedHashMap<>();
			for (Entry<Integer, LocationGowalla> entry : locIDLocationObjectDictionaryTemp.entrySet())
			{
				locIDNameDict.put(entry.getKey(), entry.getValue().getLocationName());
			}
			locIDNameDictionary = locIDNameDict;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param locIDLocationObjectDictionary
	 */
	public static void setLocationIDNameDictionary(
			LinkedHashMap<Integer, LocationGowalla> locIDLocationObjectDictionary)
	{
		try
		{
			LinkedHashMap<Integer, String> locIDNameDict = new LinkedHashMap<>();
			for (Entry<Integer, LocationGowalla> entry : locIDLocationObjectDictionary.entrySet())
			{
				locIDNameDict.put(entry.getKey(), entry.getValue().getLocationName());
			}
			locIDNameDictionary = locIDNameDict;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static LinkedHashMap<Integer, String> getLocationIDNameDictionary()
	{
		return locIDNameDictionary;
	}

	public static LinkedHashMap<Integer, LocationGowalla> getLocIDLocationObjectDictionary()
	{
		return locIDLocationObjectDictionary;
	}

	/**
	 * 
	 * @param catIDsHierDistSerialisedFile
	 */
	public static void setCatIDsHierarchicalDistance(String catIDsHierDistSerialisedFile)
	{
		try
		{
			// System.err.println("catIDsHierDistSerialisedFile= " + catIDsHierDistSerialisedFile);
			catIDsHierarchicalDistance = (HashMap<String, Double>) Serializer
					.kryoDeSerializeThis(catIDsHierDistSerialisedFile);

		}
		catch (Exception e)
		{
			System.err.println("catIDsHierDistSerialisedFile= " + catIDsHierDistSerialisedFile);
			e.printStackTrace();
		}
	}

	public static void setCatIDGivenLevelCatIDMap()
	{
		catIDGivenLevelCatIDMap = getGivenLevelCatIDForAllCatIDs(PathConstants.pathToSerialisedLevelWiseCatIDsDict,
				Constant.HierarchicalCatIDLevelForEditDistance, true);
	}

	public static ArrayList<Integer> getGivenLevelCatID(int givenCatID)
	{
		if (catIDGivenLevelCatIDMap == null)
		{
			System.out.println(PopUps.getTracedErrorMsg("Error: catIDGivenLevelCatIDMap==null"));
			System.exit(1);
		}

		if (catIDGivenLevelCatIDMap.get(givenCatID) == null)
		{
			System.err.println(
					PopUps.getTracedErrorMsg("Error: catIDGivenLevelCatIDMap.get(givenCatID" + givenCatID + ")==null"));
		}

		return catIDGivenLevelCatIDMap.get(givenCatID);
	}

	/**
	 * 
	 * @param pathToSerialisedLevelWiseCatIDsDict
	 * @param givenLevel
	 * @param writeToFile
	 * @return
	 */
	public static TreeMap<Integer, ArrayList<Integer>> getGivenLevelCatIDForAllCatIDs(
			String pathToSerialisedLevelWiseCatIDsDict, int givenLevel, boolean writeToFile)
	{
		TreeMap<Integer, ArrayList<Integer>> catIDGivenLevelCatIDMap = new TreeMap<>();
		ArrayList<Integer> catIDsWithNoGivenLevelCatID = new ArrayList<>();

		if (givenLevel > 3 || givenLevel < 1)
		{
			System.err.println(
					PopUps.getTracedErrorMsg("Error: only three levels for Gowalla while given level =" + givenLevel));
		}

		TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict = (TreeMap<Integer, String[]>) Serializer
				.kryoDeSerializeThis(pathToSerialisedLevelWiseCatIDsDict);

		for (Entry<Integer, String[]> catIDEntry : catIDLevelWiseCatIDsDict.entrySet())
		{
			// System.out.println(catIDEntry.getKey() + "-" + catIDEntry.getValue());
			String[] arr = catIDEntry.getValue();
			String givenLevelCatIDs = arr[givenLevel - 1];

			if (givenLevelCatIDs == null)
			{
				// does not have a single numeric catID and does not contain "__" which would be for multiple cat ids
				// does not have given level cat id.
				catIDsWithNoGivenLevelCatID.add(catIDEntry.getKey());
				continue;
			}

			else if (givenLevelCatIDs.contains("__") == true)
			{
				String[] splitted = RegexUtils.patternDoubleUnderScore.split(givenLevelCatIDs);
				ArrayList<Integer> splittedCatIDs = (ArrayList<Integer>) Arrays.stream(splitted)
						.map(s -> Integer.valueOf(s)).collect(Collectors.toList());
				catIDGivenLevelCatIDMap.put(catIDEntry.getKey(), splittedCatIDs);
				continue;
			}

			else if (StringUtils.isNumeric(givenLevelCatIDs))
			{
				catIDGivenLevelCatIDMap.put(catIDEntry.getKey(),
						new ArrayList<>(Collections.singletonList(Integer.valueOf(givenLevelCatIDs))));
			}

			else
			{// does not have a single numeric catID and does not contain "__" which would be for multiple cat ids
				// does not have given level cat id.
				catIDsWithNoGivenLevelCatID.add(catIDEntry.getKey());
				continue;
			}

		}

		if (writeToFile)
		{
			System.out.println("Num of catIDs with no given level = " + catIDsWithNoGivenLevelCatID.size());
			System.out.println("Num of catIDs with given level = " + catIDGivenLevelCatIDMap.size());

			StringBuilder sb1 = new StringBuilder();
			catIDGivenLevelCatIDMap.entrySet().stream()
					.forEach(e -> sb1.append(e.getKey()).append("-").append(e.getValue()).append("\n"));
			WritingToFile.writeToNewFile(sb1.toString(), "catIDGivenLevelCatIDMap.csv");

			StringBuilder sb2 = new StringBuilder();
			catIDsWithNoGivenLevelCatID.stream().forEach(e -> sb2.append(String.valueOf(e)).append("\n"));
			WritingToFile.writeToNewFile(sb2.toString(), "catIDsWithNoGivenLevelCatID.csv");
		}

		return catIDGivenLevelCatIDMap;
	}

	public static boolean isGowallaUserIDWithGT553MaxActsPerDay(int userID)
	{
		boolean blacklisted = false;

		for (int a : gowallaUserIDsWithGT553MaxActsPerDay)
		{
			if (a == userID)
			{
				return true;
			}
		}
		return blacklisted;
	}

	public static boolean isGowallaUserIDWithGT553MaxActsPerDayIndex(int userID)
	{
		boolean blacklisted = false;

		for (int a : gowallaUserIDsWithGT553MaxActsPerDayIndex)
		{
			if (a == userID)
			{
				return true;
			}
		}
		return blacklisted;
	}

	public DomainConstants()
	{
	}

}
