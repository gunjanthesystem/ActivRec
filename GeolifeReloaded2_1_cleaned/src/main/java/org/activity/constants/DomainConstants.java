package org.activity.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.LocationGowalla;
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

	public static final int above10RTsUserIDsGeolifeData[] =
			{ 62, 84, 52, 68, 167, 179, 153, 85, 128, 10, 126, 111, 163, 65, 91, 82, 139, 108 };
	public static final int gowallaWorkingCatLevel = 2; // -1 indicates original working cat
	static int[] gowallaUserIDs = null;
	public static String[] featureNames = { "ActivityName", "StartTime", "Duration", "DistanceTravelled",
			"StartGeoCoordinates", "EndGeoCoordinates", "AvgAltitude" };

	public static String[] gowallaFeatureNames = { "ActivityName", "StartTime" };

	public static final int gowallaUserIDsWithGT553MaxActsPerDay[] =
			{ 5195, 9298, 9751, 16425, 17012, 18382, 19416, 19957, 20316, 23150, 28509, 30293, 30603, 42300, 44718,
					46646, 74010, 74274, 76390, 79509, 79756, 86755, 103951, 105189, 106328, 114774, 118023, 136677,
					154692, 179386, 194812, 213489, 224943, 235659, 246993, 251408, 269889, 311530, 338587, 395223,
					563986, 624892, 862876, 1722363, 2084969, 2096330, 2103094, 2126604, 2190642 };

	public static final int gowallaUserIDsWithGT553MaxActsPerDayIndex[] = { 117, 148, 152, 215, 223, 236, 245, 250, 251,
			266, 306, 313, 314, 333, 338, 341, 431, 433, 437, 447, 449, 471, 510, 515, 520, 543, 547, 607, 634, 661,
			678, 696, 714, 729, 741, 749, 761, 792, 816, 841, 855, 861, 869, 894, 902, 907, 912, 921, 934, };

	public final static int numOfCatLevels = 3;

	public static HashMap<String, Double> catIDsHierarchicalDistance = null;
	public static TreeMap<Integer, String> catIDNameDictionary = null;
	public static LinkedHashMap<Integer, LocationGowalla> locIDLocationObjectDictionary = null;
	public static TreeMap<Integer, Character> catIDCharCodeMap = null;
	public static TreeMap<Character, Integer> charCodeCatIDMap = null;

	public final static String pathToSerialisedCatIDNameDictionary = "./dataToRead/UI/CatIDNameDictionary.kryo";
	public final static String pathToSerialisedLocationObjects =
			"./dataToRead/Mar30/DatabaseCreatedMerged/mapForAllLocationData.kryo";
	public final static String pathToSerialisedLevelWiseCatIDsDict =
			"./dataToRead/May17/mapCatIDLevelWiseCatIDsDict.kryo";
	public final static String pathToSerialisedCatIDsHierDist = "./dataToRead/April7/mapCatIDsHierDist.kryo";

	public static TreeMap<Integer, ArrayList<Integer>> catIDGivenLevelCatIDMap;
	// public static final Integer hierarchyLevelForEditDistance = 1;// is in Constant class

	public final static String[] gowallaUserGroupsLabels =
			{ "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };

	public static void main(String args[])
	{
		getGivenLevelCatIDForAllCatIDs(pathToSerialisedLevelWiseCatIDsDict, 1, true);
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
			catIDNameDictionary =
					(TreeMap<Integer, String>) Serializer.kryoDeSerializeThis(pathToSerialisedCatIDNameDictionary);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

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

	/**
	 * 
	 * @param catIDsHierDistSerialisedFile
	 */
	public static void setCatIDsHierarchicalDistance(String catIDsHierDistSerialisedFile)
	{
		try
		{
			catIDsHierarchicalDistance =
					(HashMap<String, Double>) Serializer.kryoDeSerializeThis(catIDsHierDistSerialisedFile);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void setCatIDGivenLevelCatIDMap()
	{
		catIDGivenLevelCatIDMap = getGivenLevelCatIDForAllCatIDs(pathToSerialisedLevelWiseCatIDsDict,
				Constant.HierarchicalLevelForEditDistance, true);
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

		TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict =
				(TreeMap<Integer, String[]>) Serializer.kryoDeSerializeThis(pathToSerialisedLevelWiseCatIDsDict);

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
