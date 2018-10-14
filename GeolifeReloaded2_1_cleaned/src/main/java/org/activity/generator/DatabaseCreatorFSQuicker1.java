package org.activity.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.CheckinEntryV2;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.UserGowalla;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.StringUtils;

public class DatabaseCreatorFSQuicker1
{

	static String pathToAllCheckinFile = "/home/gunjan/RWorkspace/GowallaRWorks/FSNY2018-10-04AllTargetUsersDatesOnly.csv";

	public static void main(String[] args)
	{
		// Start of curtain 9 Oct 2018
		// mapAlphaNumericPlaceCatIDToInts();
		// End of curtain 9 Oct 2018

		String commonPathToWrite = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/DataCreations9OctFSNY/";
		String placeIDANPlaceIDIntFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/placeIDANPlaceIDInt.kryo";
		String catIDANCatIDIntFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/catIDANCatIDInt.kryo";
		String catIDNameDictFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/catIDIntCatName.kryo";
		dataCreatorFSNY6Oct(commonPathToWrite, pathToAllCheckinFile, placeIDANPlaceIDIntFileName,
				catIDANCatIDIntFileName, catIDNameDictFileName);

	}

	///// Start of Oct 6 2018
	/**
	 * Fork of dataCreator3_April8
	 * 
	 * @param commonPathToWrite
	 * @param checkinDataFileNameP
	 * @param placeIDANPlaceIDIntFileName
	 * @param catIDANCatIDIntFileName
	 * @param catIDNameDictFileName
	 * @since 6 Oct 2018
	 */
	public static void dataCreatorFSNY6Oct(String commonPathToWrite, String checkinDataFileNameP,
			String placeIDANPlaceIDIntFileName, String catIDANCatIDIntFileName, String catIDNameDictFileName)
	// , String categoryHierarchyTreeFileNameP,int workingCatLevelP)
	{
		System.out.println("Running starts:  " + LocalDateTime.now());
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 12, 2016
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> mapForAllCheckinData;
		LinkedHashMap<Integer, LocationGowalla> mapForAllLocationData;
		LinkedHashMap<String, UserGowalla> mapForAllUserData;
		Set<String> userIDsInCheckinData, locationIDsInCheckinData;

		try
		{
			long ct1 = System.currentTimeMillis();
			Constant.setCommonPath(commonPathToWrite);
			// commonPath = Constant.getCommonPath();
			// Redirecting the console output
			PrintStream consoleLogStream = new PrintStream(
					new File(commonPathToWrite + "consoleLogDatabaseCreatorGowalla.txt"));
			// System.setOut(new PrintStream(new FileOutputStream('/dev/stdout')));
			System.setOut(new PrintStream(consoleLogStream));
			System.setErr(consoleLogStream);
			System.out.println("Default timezone = " + TimeZone.getDefault());

			//// Start of curtain 8 Oct 2018
			// DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
			// .deSerializeThis(categoryHierarchyTreeFileNameP);
			// TreeMap<Integer, String> catIDNameDictionary = (TreeMap<Integer, String>) Serializer
			// .kryoDeSerializeThis(catIDNameDictionaryFileNameP);
			// WToFile.writeMapToNewFile(catIDNameDictionary, "catID,catName", ",",
			// commonPathP + "catIDNameDictionary.csv");
			// // int workingCatLevel = DomainConstants.gowallaWorkingCatLevel;
			//
			// Pair<TreeMap<Integer, String>, LinkedHashSet<Integer>> catIDWorkingLevelCatIDsDictResult =
			// getWorkingLevelCatIDsForAllCatIDs(
			// catIDNameDictionary, workingCatLevelP, rootOfCategoryTree);
			//
			// TreeMap<Integer, String> catIDWorkingLevelCatIDsDict = catIDWorkingLevelCatIDsDictResult.getFirst();
			// WToFile.writeMapToNewFile(catIDWorkingLevelCatIDsDict, "catID,WorkingLevelCatID", ",",
			// commonPathP + "catIDWorkingLevelCatIDsDict.csv");
			//
			// LinkedHashSet<Integer> catIDsInHierarchy = catIDWorkingLevelCatIDsDictResult.getSecond();
			// WToFile.writeToNewFile(catIDsInHierarchy.toString(), commonPathP + "UniqueCatIDsInHierarchy.csv");
			//
			// TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict = getLevelWiseCatIDsForAllCatIDs(catIDNameDictionary,
			// rootOfCategoryTree, DomainConstants.numOfCatLevels);
			// WToFile.writeMapOfArrayValsToNewFile(catIDLevelWiseCatIDsDict, "catID,LevelWiseCatIDs", ",",
			// commonPathP + "catIDLevelWiseCatIDsDict.csv");
			//
			// HashMap<String, Double> mapCatIDsHierDist = null;
			// // disabled temporarily createCatIDsHierarchicalDistMap(catIDLevelWiseCatIDsDict,
			// // catIDNameDictionary, catIDsInHierarchy);
			//
			// // used in create checkin entries to determine if a cat id is acceptable
			// LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap = UIUtilityBox
			// .getCatIDsFoundNodesMap(rootOfCategoryTree, catIDNameDictionary);
			//
			//// End of curtain 8 Oct 2018

			// Start of added on 9 Oct 2018
			Map<String, Integer> placeIDANPlaceIDIntMap = (Map<String, Integer>) Serializer
					.kryoDeSerializeThis(placeIDANPlaceIDIntFileName);
			// Map<Integer, String> placeIDIntPlaceIDAN = new TreeMap<>();
			Map<String, Integer> catIDANCatIDIntMap = (Map<String, Integer>) Serializer
					.kryoDeSerializeThis(catIDANCatIDIntFileName);
			System.out.println("placeIDANPlaceIDIntMap.size() = " + placeIDANPlaceIDIntMap.size());
			System.out.println("catIDANCatIDIntMap.size() = " + catIDANCatIDIntMap.size());
			// Map<Integer, String> catIDIntCatIDAN = new TreeMap<>();

			// End of added on 9 Oct 2018

			Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>> checkinResult = createCheckinEntriesFSNY6Oct2018(
					checkinDataFileNameP, commonPathToWrite, catIDANCatIDIntMap, placeIDANPlaceIDIntMap);

			mapForAllCheckinData = checkinResult.getFirst();
			long numOfCheckins = mapForAllCheckinData.entrySet().stream().mapToLong(e -> e.getValue().size()).sum();
			System.out.println("num of checkins = " + numOfCheckins); // 6276222
			// PopUps.showMessage("num of checkins = " + numOfCheckins);

			////// consecutive same analysis
			// countConsecutiveSimilarActivities2(mapForAllCheckinData, commonPath, catIDNameDictionaryFileName);
			// $$Function<CheckinEntryV2, String> consecCompareDirectCatID = ce -> String.valueOf(ce.getActivityID());
			Function<CheckinEntryV2, String> consecCompareLocationID = ce -> String.valueOf(ce.getLocationIDs().get(0));// String.valueOf(ce.getFirstLocationID());
			DatabaseCreatorGowallaQuicker1.countConsecutiveSimilarActivities3_3April2018(mapForAllCheckinData,
					commonPathToWrite, catIDNameDictFileName, consecCompareLocationID);// consecCompareDirectCatID);

			// WritingToFile.writeLinkedHashMapOfTreemapCheckinEntryV2(mapForAllCheckinData,
			// commonPathP + "mapForAllCheckinData.csv", catIDNameDictionary);
			// compare this filr to the dataset read to ensure that the read dataset and the Checkin objcts created are
			// equivalent

			// $$ TODO WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2_ForRecreating(mapForAllCheckinData,
			// commonPathP + "mapForAllCheckinDataAfterMerged_8AprilSanityCheck.csv", catIDNameDictionary);

			userIDsInCheckinData = mapForAllCheckinData.keySet();
			locationIDsInCheckinData = checkinResult.getSecond();
			System.out.println("userIDsInCheckinData.size()=" + userIDsInCheckinData.size());
			System.out.println("locationIDsInCheckinData.size()=" + locationIDsInCheckinData.size());

			// mapForAllUserData = createUserGowalla(userDataFileNameP, userIDsInCheckinData, commonPathP);
			// mapForAllLocationData = createLocationGowalla0(userLocationFileNameP,
			// locationIDsInCheckinData,commonPathP);

			// Triple<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, LinkedHashMap<String, UserGowalla>,
			// LinkedHashMap<String, LocationGowalla>> allData =
			// new Triple<>(mapForAllCheckinData, mapForAllUserData, mapForAllLocationData);

			// Serializer.serializeThis(allData, commonPath + "GowallaAllData13Sep2016.obj");
			// Serializer.fstSerializeThis2(allData, commonPath + "GowallaAllData13Sep2016.obj");
			Serializer.kryoSerializeThis(mapForAllCheckinData, commonPathToWrite + "mapForAllCheckinData.kryo");
			// Serializer.kryoSerializeThis(mapForAllUserData, commonPathP + "mapForAllUserData.kryo");
			// Serializer.kryoSerializeThis(mapForAllLocationData, commonPathP + "mapForAllLocationData.kryo");
			// Serializer.kryoSerializeThis(mapCatIDsHierDist, commonPathP + "mapCatIDsHierDist.kryo");
			// Serializer.kryoSerializeThis(catIDLevelWiseCatIDsDict, commonPathP + "mapCatIDLevelWiseCatIDsDict.kryo");
			// catIDsHierDistDict
			// $Serializer.kryoSerializeThis(allData, commonPath + "GowallaAllData13Sep2016.kryo");
			//// end of curtian1

			consoleLogStream.close();

			long ct4 = System.currentTimeMillis();
			PopUps.showMessage("All data creation done in " + ((ct4 - ct1) / 1000) + " seconds since start");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	///// End of Oct 6 2018

	/**
	 * Fork of createCheckinEntries8April2018(). Created for FSNY dataset
	 * 
	 * @param checkinFileNameToRead
	 * @param commonPathToWrite
	 * @param placeIDANPlaceIDIntMap
	 * @param catIDANCatIDIntMap
	 * @param rootOfCategoryTree
	 * @param catIDWorkingLevelCatIDsDict
	 * @param catIDsFoundNodesMap
	 * @param workingCatLevel
	 * @param catIDLevelWiseCatIDsDict
	 * @return
	 * @since 6 October
	 */
	private static Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>> createCheckinEntriesFSNY6Oct2018(
			String checkinFileNameToRead, String commonPathToWrite, Map<String, Integer> catIDANCatIDIntMap,
			Map<String, Integer> placeIDANPlaceIDIntMap)
	{
		int countOfCheckinEntryObjects = 0;
		int numOfDuplicateTimestamps = 0;
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> result = new LinkedHashMap<>();

		Set<String> locationIDsInCheckinData = new HashSet<>();

		int countOfLines = 0;

		String lineRead = "";

		StringBuilder logRejectedCheckins = new StringBuilder("LineNumOfCheckin,Reason,DirectCatID\n");
		long countOfRejectedCheckinNotInHierarchy = 0, countOfRejectedCHeckinBelowLevel2 = 0,
				countOfCinWithMultipleWorkingLevelCatIDs = 0, countOfRejectedCheckinNotInCatIDNameDict = 0;
		System.out.println("----Inside createCheckinEntries----------------");
		Pattern underScore = RegexUtils.patternUnderScore;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(checkinFileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				// Start of temp limiter
				// if (countOfLines > 5000000)
				// {
				// break;
				// }
				// end of temp limiter

				countOfLines += 1;
				if (countOfLines == 1)
				{
					continue; // skip the header line
				}

				if (countOfLines % 100000/* 200000 */ == 0)
				{
					System.out.println(" #lines read = " + countOfLines);
					// System.exit(0);
				}

				String splittedLine[] = RegexUtils.patternComma.split(lineRead);
				// lineRead.split(",");
				// System.out.println("splittedLine[]=" + Arrays.asList(splittedLine).toString());

				String userID = splittedLine[0];

				// also involves converting alphanumeric locID to ints.
				ArrayList<String> locationIDsString = StringUtils.splitAsStringList(splittedLine[1], underScore);
				ArrayList<Integer> locationIDs = (ArrayList<Integer>) locationIDsString.stream()
						.map(i -> placeIDANPlaceIDIntMap.get(i)).collect(Collectors.toList());
				// ArrayList<Integer> locationIDs = StringUtils.splitAsIntegerList(splittedLine[1], underScore);

				// Integer locationID = Integer.valueOf(splittedLine[1]);// .replaceAll("\"", ""));
				locationIDsInCheckinData
						.addAll(locationIDs.stream().map(i -> String.valueOf(i)).collect(Collectors.toList()));

				Integer catIDDirect = catIDANCatIDIntMap.get(splittedLine[2]); // convert alphanumeric catID to int
				String workingLevelCatIDs = String.valueOf(catIDDirect);
				// Integer catName = Integer.valueOf(splittedLine[3]);

				ArrayList<String> latitudes = StringUtils.splitAsStringList(splittedLine[4], underScore);
				ArrayList<String> longitudes = StringUtils.splitAsStringList(splittedLine[5], underScore);

				// Timestamp ts = Timestamp.from(Instant.parse(splittedLine[3].replaceAll("\"", "")));

				// 2009-03-16T21:08:46
				// LocalDateTime ldtTS = LocalDateTime.parse(splittedLine[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				String localTimeString = splittedLine[6].substring(0, splittedLine[6].length() - 2) + "Z";
				// System.out.println("localTimeString = " + localTimeString);
				// Timestamp ts = java.sql.Timestamp.valueOf(localTimeString);
				Timestamp ts = DateTimeUtils.getTimestampFromISOString(localTimeString);
				// System.out.println("ts2= " + ts);
				// System.out.println("\nts\nread =" + splittedLine[2] + "\nstored=" + ts.toString());// SANITY CHECK
				// OK

				// String workingLevelCatIDs = splittedLine[6];

				// Note in the dataset being read: from prev line --> from next checkin and from next line--> from prev
				// checkin, since the data read in R for the creating was that dataset was in reverse order by time.
				Double distanceInMetersFromPrev = Double.valueOf(splittedLine[9]);
				Long durationInSecsFromPrev = Long.valueOf(splittedLine[10]);
				// String[] levelWiseCatIDs = underScore.split(splittedLine[9]);
				Double distanceInMeterFromNextCheckin = Double.valueOf(-9999);// Double.valueOf(splittedLine[10]);
				Long durationInSecsFromNextCheckin = Long.valueOf(-9999);
				Long.valueOf(splittedLine[11]);
				// String tz = new String(splittedLine[12]);

				// a direct catid is acceptable only if it is present in cat hierarchy tree at one of more nodes and
				// atleast one of those nodes have direct level >= workingLevel (2).. in other words, ignore catid
				// at level 1
				// Pair<Boolean, String> isAcceptableDirectCatID = isAcceptableDirectCatIDFaster(catIDDirect,
				// catIDsFoundNodesMap, workingCatLevel);
				String[] levelWiseCatIDs = null;
				String tz = "UTC";

				CheckinEntryV2 cobj = new CheckinEntryV2(userID, locationIDs, ts, latitudes, longitudes, catIDDirect,
						workingLevelCatIDs, distanceInMetersFromPrev, durationInSecsFromPrev, levelWiseCatIDs,
						distanceInMeterFromNextCheckin, durationInSecsFromNextCheckin, tz);

				// (userID, locationID, ts, latitude, longitude, catIDDirect,
				// workingLevelCatIDs, distFromPrevCheckinInM, durationFromPrevCheckinInM,
				// catIDLevelWiseCatIDsDict.get(catIDDirect), distFromNextCheckinInM, durationFromNextCheckinInM,
				// timeZone);// , distanceFromPrevInM, durationFromPrevInSec);

				countOfCheckinEntryObjects += 1;

				TreeMap<Timestamp, CheckinEntryV2> mapForThisUser = null;
				mapForThisUser = result.get(userID); // if userid already in map

				if (mapForThisUser == null) // else create new map for this userid
				{
					mapForThisUser = new TreeMap<Timestamp, CheckinEntryV2>();
				}

				if (mapForThisUser.containsKey(ts))
				{
					System.err.println("Error: duplicate ts: map for this user (userID=" + userID
							+ ") already contains ts =" + ts.toString());
					numOfDuplicateTimestamps += 1;
				}

				mapForThisUser.put(ts, cobj);
				result.put(userID, mapForThisUser);

			}

			System.out.println("num of users = " + result.size());
			System.out.println("num of lines read = " + countOfLines);

			System.out.println("num of lines NotInHierarchy = " + countOfRejectedCheckinNotInHierarchy);
			System.out.println("num of lines LevelNotAcceptable = " + countOfRejectedCHeckinBelowLevel2);
			System.out.println("num of lines NotInCatIDNameDict = " + countOfRejectedCheckinNotInCatIDNameDict);
			System.out.println(
					"num of CheckinEntry objects created = countOfCheckinEntryObjects =" + countOfCheckinEntryObjects);
			System.out.println("num of duplicate timestamps = " + numOfDuplicateTimestamps);
			System.out.println("actual num of CheckinEntry objects created returned ="
					+ result.entrySet().stream().mapToLong(e -> e.getValue().size()).sum());

			System.out.println("countOfCinWithMultipleWorkingLevelCatIDs =" + countOfCinWithMultipleWorkingLevelCatIDs);
			// numOfDuplicateTimestamps

			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("lineRead=\n" + lineRead);
		}

		WToFile.appendLineToFileAbs(logRejectedCheckins.toString(), commonPathToWrite + "RejectedCheckinsLog.txt");

		System.out.println("----Exiting createCheckinEntries----------------");
		return new Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>>(result,
				locationIDsInCheckinData);

	}

	/**
	 * Convert alphanumeric place and cat ID to integer IDs.
	 * <p>
	 * Create catID name dictionary
	 */
	public static void mapAlphaNumericPlaceCatIDToInts()
	{
		String commonPathToRead = "/home/gunjan/RWorkspace/GowallaRWorks/";
		String commonPathToWrite = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/";
		String fileToRead = commonPathToRead + "FSNY2018-10-04AllTargetUsersDatesOnly.csv";
		String fileToWrite = commonPathToWrite + "FSNY2018-10-04AllTargetUsersDatesOnlyCatIDNameDict.csv";

		try
		{

			List<List<String>> allData = ReadingFromFile.nColumnReaderString(fileToRead, ",", true);
			// List<String> res = ReadingFromFile.twoColumnReaderString(fileToRead, ",", 2, 3, true);
			System.out.println("allData.size() = " + allData.size());
			Pattern comma = RegexUtils.patternComma;

			Map<String, Integer> placeIDANPlaceIDInt = new TreeMap<>();
			Map<Integer, String> placeIDIntPlaceIDAN = new TreeMap<>();
			Map<String, Integer> catIDANCatIDInt = new TreeMap<>();
			Map<Integer, String> catIDIntCatIDAN = new TreeMap<>();

			Set<String> allPlaceIDsAn = new TreeSet<>();
			Set<String> allCatIDsAn = new TreeSet<>();
			Map<String, String> catIDANCatName = new TreeMap<>();
			Map<Integer, String> catIDIntcatName = new TreeMap<>();

			for (List<String> line : allData)
			{
				allCatIDsAn.add(line.get(2));
				catIDANCatName.put(line.get(2), line.get(3));
				allPlaceIDsAn.add(line.get(1));
			}

			System.out.println("catIDsAn.size() = " + allCatIDsAn.size());
			System.out.println("placeIDsAn.size() = " + allPlaceIDsAn.size());
			System.out.println("catIDANcatName.size() = " + catIDANCatName.size());

			int catIDInt = 0;
			for (String catAN : allCatIDsAn)
			{
				catIDInt += 1;
				catIDANCatIDInt.put(catAN, catIDInt);
				catIDIntCatIDAN.put(catIDInt, catAN);
				catIDIntcatName.put(catIDInt, catIDANCatName.get(catAN));
			}

			int placeIDInt = 0;
			for (String placeIDAN : allPlaceIDsAn)
			{
				placeIDInt += 1;
				placeIDANPlaceIDInt.put(placeIDAN, placeIDInt);
				placeIDIntPlaceIDAN.put(placeIDInt, placeIDAN);
			}

			System.out.println("catIDANcatIDInt.size() = " + catIDANCatIDInt.size());
			System.out.println("catIDIntcatIDAN.size() = " + catIDIntCatIDAN.size());
			System.out.println("catIDIntcatName.size() = " + catIDIntcatName.size());
			System.out.println("placeIDANplaceIDInt.size() = " + placeIDANPlaceIDInt.size());
			System.out.println("placeIDIntplaceIDAN.size() = " + placeIDIntPlaceIDAN.size());

			Serializer.kryoSerializeThis(catIDANCatIDInt, commonPathToWrite + "catIDANCatIDInt.kryo");
			Serializer.kryoSerializeThis(catIDIntCatIDAN, commonPathToWrite + "catIDIntCatIDAN.kryo");
			Serializer.kryoSerializeThis(catIDIntcatName, commonPathToWrite + "catIDIntCatName.kryo");
			Serializer.kryoSerializeThis(placeIDANPlaceIDInt, commonPathToWrite + "placeIDANPlaceIDInt.kryo");
			Serializer.kryoSerializeThis(placeIDIntPlaceIDAN, commonPathToWrite + "placeIDInPlaceIDAN.kryo");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
