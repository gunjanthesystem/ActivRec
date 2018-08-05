package org.activity.constants;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.PairedIndicesTo1DArrayConverter;
import org.activity.objects.UserGowalla;
import org.activity.spatial.SpatialUtils;
import org.activity.ui.PopUps;
import org.activity.util.RegexUtils;
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

	public static TreeMap<Integer, ArrayList<Integer>> catIDGivenLevelCatIDMap;

	private static Map<Integer, ZoneId> gowallaLocZoneIdMap = null;

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
	// { "901", "801", "701", "601", "501", "401" /* "1", "101", "201", "301", */ };//

	public final static String[] gowallaUserGroupsLabelsFixed = { "1", "101", "201", "301", "401", "501", "601", "701",
			"801", "901" };//

	public final static String gowalla100RandomUsersLabel = "100R";

	public final static Set<Integer> locIDsIn5DaysTrainTestDataWithNullTZ = new HashSet<>(
			Arrays.asList(259769, 328496, 339529, 339618, 351286, 354613));
	/**
	 * keeping it globally to avoid recomputing for each matching unit
	 * <P>
	 * {UserID, {{PDVal,repAO},{PDVal,{precDuration,succDuration}}}
	 * <P>
	 * NOT USED AT THE MOMENT
	 */
	static LinkedHashMap<Integer, Pair<LinkedHashMap<Integer, ActivityObject>, LinkedHashMap<Integer, Pair<Double, Double>>>> userIDRepAOResultMap;

	/**
	 * Added on 12 July 2018 {direct cat id, list{list of catids at level 1,list of catids at level 2, list of catids at
	 * level 3 }
	 */
	public static TreeMap<Integer, ArrayList<ArrayList<Integer>>> catIDLevelWiseCatIDsList;

	static Map<Long, Set<Long>> gridIDLocIDsGowallaMap;
	static Map<Long, Long> locIDGridIDGowallaMap;
	static Map<Long, Integer> locIDGridIndexGowallaMap;

	/**
	 * as of 4 Aug 2018, only used for Sanity check
	 */
	static Map<Integer, double[]> javaGridIndexRGridLatRGridLon;

	static HashMap<Integer, Double> gridIndexPairHaversineDist;// added on 3 Aug 2018
	static PairedIndicesTo1DArrayConverter pairedIndicesTo1DArrayConverter;// added on 3 Aug 2018

	/////////////////////////////////////////////////////////////

	/**
	 * Deserializes and set gridIndexPairHaversineDist and pairedIndicesTo1DArrayConverter for that.
	 * <p>
	 * Note: the data is to be read from the engine.
	 */
	public static void setGridIndexPairDistMaps()
	{
		String fileNamePhrase = "IntDoubleWith1DConverter";
		try
		{
			gridIndexPairHaversineDist = (HashMap<Integer, Double>) Serializer
					.kryoDeSerializeThis(PathConstants.pathToSerialisedHaversineDistOnEngine
							+ "gridIndexPairHaversineDist" + fileNamePhrase + ".kryo");
			System.out.println("deserialised gridIndexPairHaversineDist.size()= " + gridIndexPairHaversineDist.size());

			pairedIndicesTo1DArrayConverter = (PairedIndicesTo1DArrayConverter) Serializer
					.kryoDeSerializeThis(PathConstants.pathToSerialisedHaversineDistOnEngine
							+ "pairedIndicesTo1DConverter" + fileNamePhrase + ".kryo");
			System.out.println(
					"deserialised pairedIndicesTo1DArrayConverter= " + pairedIndicesTo1DArrayConverter.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param gridIndex1
	 * @param gridIndex2
	 * @return
	 */
	public static double getHaversineDistForGridIndexPairs(int gridIndex1, int gridIndex2)
	{
		if (gridIndexPairHaversineDist == null || pairedIndicesTo1DArrayConverter == null
				|| gridIndexPairHaversineDist.size() == 0)
		{
			PopUps.showError("Error: GridIndexPairDistMaps NOT SET!!");
			System.exit(-1);
		}

		if (gridIndex1 == gridIndex2)
		{
			PopUps.showError("Error: gridIndex1 = gridIndex2 (NOT allowed for pairedIndicesTo1DArrayConverter");
			System.exit(-1);
		}

		int oneDIndex = pairedIndicesTo1DArrayConverter.pairedIndicesTo1DArrayIndex(gridIndex1, gridIndex2);
		Double dist = gridIndexPairHaversineDist.get(oneDIndex);

		if (dist == null)
		{
			PopUps.showError(
					"Error: dist not available for gridIndex1=" + gridIndex1 + " gridIndex2=" + gridIndex2 + " !!");
			System.exit(-1);
		}

		// Sanity check pass ok on Aug 5 2018
		if (false)// Sanity check of distance fetched from deserialised precomputed distance vs actual distance
		{
			// Sanity check if precomputed distances between grid centres are correclty fetched
			// get grid centre lat,lon for gridIndex1 and gridInex3
			double[] latLon1 = javaGridIndexRGridLatRGridLon.get(gridIndex1);
			double[] latLon2 = javaGridIndexRGridLatRGridLon.get(gridIndex2);
			// reomputing haversine distance to match it with precomputed distance
			double recomputedHaversineDist = SpatialUtils.haversineFastMathV3NoRound(latLon1[0], latLon1[1], latLon2[0],
					latLon2[1]);

			boolean insane = Math.abs(dist - recomputedHaversineDist) > 1.0E-6;// Constant.epsilonForFloatZero;

			WToFile.appendLineToFileAbs(
					gridIndex1 + "," + gridIndex2 + "," + latLon1[0] + "," + latLon1[1] + "," + latLon2[0] + ","
							+ latLon2[1] + "," + dist + "," + recomputedHaversineDist + "," + insane + "\n",
					Constant.getCommonPath() + "DebuggetHaversineDistForGridIndexPairs.csv");

			if (insane)
			{
				PopUps.printTracedErrorMsgWithExit("Error: precomputed (" + dist + ") and recomputed ("
						+ recomputedHaversineDist + ") distances do not match!");
			}
		}

		return dist;
	}

	public static void setGridIDLocIDGowallaMaps()
	{
		gridIDLocIDsGowallaMap = (Map<Long, Set<Long>>) Serializer
				.kryoDeSerializeThis(PathConstants.pathToSerialisedGridIDLocIDsGowallaMap);

		locIDGridIDGowallaMap = (Map<Long, Long>) Serializer
				.kryoDeSerializeThis(PathConstants.pathToSerialisedLocIDGridIDGowallaMap);

		locIDGridIndexGowallaMap = (Map<Long, Integer>) Serializer
				.kryoDeSerializeThis(PathConstants.pathToSerialisedLocIDGridIndexGowallaMap);

		// as of 4 Aug 2018, only used for Sanity check
		javaGridIndexRGridLatRGridLon = (Map<Integer, double[]>) Serializer
				.kryoDeSerializeThis(PathConstants.pathToJavaGridIndexRGridLatRGridLon);

		if (true)// write
		{
			StringBuilder sb1 = new StringBuilder("GridID\tLocIDs\n");
			gridIDLocIDsGowallaMap.entrySet().stream()
					.forEach(e -> sb1.append(e.getKey() + "\t" + e.getValue() + "\n"));
			WToFile.appendLineToFileAbs(sb1.toString(), Constant.getCommonPath() + "gridIDLocIDsGowallaMap.csv");

			StringBuilder sb2 = new StringBuilder("LocID\tGridID\n");
			locIDGridIDGowallaMap.entrySet().stream().forEach(e -> sb2.append(e.getKey() + "\t" + e.getValue() + "\n"));
			WToFile.appendLineToFileAbs(sb2.toString(), Constant.getCommonPath() + "locIDGridIDGowallaMap.csv");

			StringBuilder sb3 = new StringBuilder("LocID\tGridIndex\n");
			locIDGridIndexGowallaMap.entrySet().stream()
					.forEach(e -> sb3.append(e.getKey() + "\t" + e.getValue() + "\n"));
			WToFile.appendLineToFileAbs(sb3.toString(), Constant.getCommonPath() + "locIDGridIndexGowallaMap.csv");

			StringBuilder sb4 = new StringBuilder("GridIndex\tTGridCentreLat\tRGridCentrLon\n");
			javaGridIndexRGridLatRGridLon.entrySet().stream()
					.forEach(e -> sb4.append(e.getKey() + "\t" + e.getValue()[0] + "\t" + e.getValue()[1] + "\n"));
			WToFile.appendLineToFileAbs(sb4.toString(), Constant.getCommonPath() + "javaGridIndexRGridLatRGridLon.csv");

		}
		System.out.println("gridIDLocIDsGowallaMap.size()=" + gridIDLocIDsGowallaMap.size());
		System.out.println("locIDGridIDGowallaMap.size()=" + locIDGridIDGowallaMap.size());
		System.out.println("locIDGridIndexGowallaMap.size()=" + locIDGridIndexGowallaMap.size());
		System.out.println("javaGridIndexRGridLatRGridLon.size()=" + javaGridIndexRGridLatRGridLon.size());
	}

	public static Map<Long, Set<Long>> getGridIDLocIDsGowallaMap()
	{
		return gridIDLocIDsGowallaMap;
	}

	public static Map<Long, Long> getLocIDGridIDGowallaMap()
	{
		return locIDGridIDGowallaMap;
	}

	public static Map<Long, Integer> getLocIDGridIndexGowallaMap()
	{
		return locIDGridIndexGowallaMap;
	}

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
	 * @deprecated
	 * @return
	 */
	public static ZoneId getGowallaLocZoneId(List<Integer> locIDs)
	{
		ZoneId id = null;

		for (int i = 0; i < locIDs.size(); i++)
		{
			if (gowallaLocZoneIdMap.get(locIDs.get(i)) != null)
			{
				return gowallaLocZoneIdMap.get(locIDs.get(i));
			}
		}
		return id;
	}

	public static void setGowallaLocZoneIdMap(String pathToSerialisedGowallaLocZoneIdMap)
	{
		if (pathToSerialisedGowallaLocZoneIdMap.trim().length() == 0)
		{
			System.out.println("ALERT! pathToSerialisedGowallaLocZoneIdMap is empty = "
					+ pathToSerialisedGowallaLocZoneIdMap + "\nNOT SETTING gowallaLocZoneIdMap");
		}
		else
		{
			DomainConstants.gowallaLocZoneIdMap = (LinkedHashMap<Integer, ZoneId>) Serializer
					.kryoDeSerializeThis(pathToSerialisedGowallaLocZoneIdMap);
		}
	}

	/**
	 * Sets to null to save space
	 */
	public static void clearGowallaLocZoneIdMap()
	{
		DomainConstants.gowallaLocZoneIdMap = null;
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

	/**
	 * @deprecated on 12 July 2018 for design reason (minimising number of knobs)
	 * @param givenCatID
	 * @return
	 */
	public static ArrayList<Integer> getGivenLevelCatIDBefore12July2018(int givenCatID)
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
	 * @param givenCatID
	 * @param givenLevel
	 *            1, 2 or 3
	 * @return
	 * @since 12 July 2018
	 */
	public static ArrayList<Integer> getGivenLevelCatID(int givenCatID, int givenLevel)
	{
		if (catIDLevelWiseCatIDsList == null)
		{
			System.out.println(PopUps.getTracedErrorMsg("Error: catIDLevelWiseCatIDsList==null"));
			System.exit(1);
		}

		if (givenLevel < 1 || givenLevel > 3)
		{
			PopUps.printTracedErrorMsg("Error in getGivenLevelCatID: givenLevel= " + givenLevel);
			System.exit(1);
		}

		if (catIDLevelWiseCatIDsList.get(givenCatID) == null)
		{
			System.err.println(PopUps
					.getTracedErrorMsg("Error: catIDLevelWiseCatIDsList.get(givenCatID" + givenCatID + ")==null"));
		}

		ArrayList<Integer> catIDsForGivenLevelForGivenDirectCatID = catIDLevelWiseCatIDsList.get(givenCatID)
				.get(givenLevel - 1);

		if (catIDsForGivenLevelForGivenDirectCatID.size() == 0 || catIDsForGivenLevelForGivenDirectCatID == null)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error: catIDLevelWiseCatIDsList.get(givenCatID" + givenCatID + ") for the given level is empty"));
		}

		return catIDsForGivenLevelForGivenDirectCatID;
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
		// $$PopUps.showMessage("getGivenLevelCatIDForAllCatIDs called");
		TreeMap<Integer, ArrayList<Integer>> catIDGivenLevelCatIDMap = new TreeMap<>();
		ArrayList<Integer> catIDsWithNoGivenLevelCatID = new ArrayList<>();

		if (givenLevel > 3 || givenLevel < 1)
		{// changed on July 12 2018
			// System.err.println(
			// PopUps.getTracedErrorMsg("Error: only three levels for Gowalla while given level =" + givenLevel));
			// PopUps.printTracedWarningMsg
			System.err.println(
					"Warning in getGivenLevelCatIDForAllCatIDs (only three levels for Gowalla while given level ="
							+ givenLevel + " will return null");
			return null;
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
			WToFile.writeToNewFile(sb1.toString(), Constant.getCommonPath() + "catIDGivenLevelCatIDMap.csv");

			StringBuilder sb2 = new StringBuilder();
			catIDsWithNoGivenLevelCatID.stream().forEach(e -> sb2.append(String.valueOf(e)).append("\n"));
			WToFile.writeToNewFile(sb2.toString(), Constant.getCommonPath() + "catIDsWithNoGivenLevelCatID.csv");
		}

		return catIDGivenLevelCatIDMap;
	}

	/**
	 * see for how it was originally created:
	 * org.activity.generator.DatabaseCreatorGowallaQuicker1.getLevelWiseCatIDsForAllCatIDs(TreeMap<Integer, String>,
	 * DefaultMutableTreeNode, int)
	 * 
	 * @param pathToSerialisedLevelWiseCatIDsDict
	 * @since 12 July 2018
	 */
	public static void setCatIDLevelWiseCatIDsList(String pathToSerialisedLevelWiseCatIDsDict)
	{
		catIDLevelWiseCatIDsList = new TreeMap<>();

		/*
		 * see the following method for how it was originally created:
		 * org.activity.generator.DatabaseCreatorGowallaQuicker1.getLevelWiseCatIDsForAllCatIDs(TreeMap<Integer,
		 * String>, DefaultMutableTreeNode, int)
		 */
		TreeMap<Integer, String[]> catIDLevelWiseCatIDsDictDeserialised = (TreeMap<Integer, String[]>) Serializer
				.kryoDeSerializeThis(pathToSerialisedLevelWiseCatIDsDict);

		for (Entry<Integer, String[]> catIDEntry : catIDLevelWiseCatIDsDictDeserialised.entrySet())
		{
			Integer directCatID = catIDEntry.getKey();
			String[] arr = catIDEntry.getValue();
			ArrayList<ArrayList<Integer>> levelWiseCatIDForThis = new ArrayList<>(3);
			boolean atleastOneLevelArrayIsNonEmpty = false;

			for (int level = 1; level <= 3; level++)
			{
				String givenLevelCatIDs = arr[level - 1];
				ArrayList<Integer> thisLevelCatIDs = new ArrayList<>();

				if (givenLevelCatIDs == null || givenLevelCatIDs.trim().length() == 0)
				{ // does not have a single numeric catID and does not contain "__" which would be for multiple cat
					// ids does not have given level cat id.//catIDsWithNoGivenLevelCatID.add(catIDEntry.getKey());
				}

				else if (givenLevelCatIDs.contains("__") == true)
				{
					String[] splitted = RegexUtils.patternDoubleUnderScore.split(givenLevelCatIDs);
					thisLevelCatIDs = (ArrayList<Integer>) Arrays.stream(splitted).map(s -> Integer.valueOf(s))
							.collect(Collectors.toList());
				}

				else if (StringUtils.isNumeric(givenLevelCatIDs))
				{
					thisLevelCatIDs.add(Integer.valueOf(givenLevelCatIDs));
				}

				else
				{
					PopUps.showError(
							"Error in org.activity.constants.DomainConstants.setCatIDLevelWiseCatIDsDict(String): for directCatID = "
									+ directCatID + " givenLevelCatIDs = " + givenLevelCatIDs);
				}

				if (thisLevelCatIDs.size() > 0)
				{
					atleastOneLevelArrayIsNonEmpty = true;
				}

				levelWiseCatIDForThis.add(level - 1, thisLevelCatIDs);
			}

			if (atleastOneLevelArrayIsNonEmpty)
			{
				catIDLevelWiseCatIDsList.put(directCatID, levelWiseCatIDForThis);
			}
		}

		StringBuilder sb3 = new StringBuilder("DirectCatID,Level1,Level2,Level3\n");
		catIDLevelWiseCatIDsList.entrySet().stream().forEachOrdered(e -> sb3.append(
				e.getKey() + "," + e.getValue().get(0) + "," + e.getValue().get(1) + "," + e.getValue().get(2) + "\n"));
		WToFile.writeToNewFile(sb3.toString(), Constant.commonPath + "catIDLevelWiseCatIDsListNonEmpty.csv");
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
