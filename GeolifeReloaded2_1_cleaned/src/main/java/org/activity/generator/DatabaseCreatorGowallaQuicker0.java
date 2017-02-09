package org.activity.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
//import java.math.String;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.CheckinEntry;
import org.activity.objects.LabelEntry;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.UserGowalla;
import org.activity.ui.PopUps;
import org.activity.ui.UIUtilityBox;
import org.activity.util.Constant;

/**
 * Reads
 * 
 * @author gunjan
 *
 */
public class DatabaseCreatorGowallaQuicker0
{

	static ArrayList<String> modeNames;

	// static LinkedHashMap<String, TreeMap<Timestamp,String>> mapForAllData;
	static LinkedHashMap<String, ArrayList<LabelEntry>> mapForLabelEntries;
	static LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinData;
	static LinkedHashMap<String, UserGowalla> mapForAllUserData;
	static LinkedHashMap<String, LocationGowalla> mapForAllLocationData;

	static Set<String> userIDsInCheckinData, locationIDsInCheckinData;
	// static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataTimeDifference;
	// static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedContinuousWithDuration;
	// static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedSandwichedWithDuration;

	// static List<String> userIDsOriginal;
	// static List<String> userIDs;
	// static String dataSplitLabel;

	// ******************PARAMETERS TO SET*****************************//
	public static String commonPath = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/DatabaseCreated/";
	// commented out on 2 feb 2017
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Dec1/DatabaseCreation/";
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/";
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/";
	// Data Works/";
	public static final String rawPathToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/";
	// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another
	// source/gowalla/";

	public static final String checkinDataFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/RSubsettedData/gw2CheckinsSpots1TargetUsersDatesOnly2Feb2017.csv";
	// commented out on 2 feb 2017
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/gw2CheckinsSpots1TargetUsersDatesOnlyNoDup.csv";
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep9DataGenerationR/gw2CheckinsSpots1TargetUsersDatesOnly.csv";

	public static final String userDataFileName = rawPathToRead + "gowalla_userinfo.csv";

	public static final String userLocationFileName = rawPathToRead + "gowalla_spots_subset1.csv";

	public static final String categoryHierarchyTreeFileName = "./dataToRead/Nov22/RootOfCategoryTree24Nov2016.DMTreeNode";
	// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/RootOfCategoryTree24Nov2016.DMTreeNode";
	static String nameForMapToBeSerialised = "mapForGowallaData25Nov2016.map";// "mapForGowallaData9Sep2016.map";

	static final String catIDNameDictionaryFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/UI/CatIDNameDictionary.kryo";
	// $$public static final int continuityThresholdInSeconds = 5 * 60; // changed from 30 min in DCU dataset...., if
	// two timestamps are separated by less than equal to this value
	// and
	public static final int continuityThresholdInSeconds = Integer.MAX_VALUE;// * 60; // changed from 30 min in DCU
																				// dataset...., if two timestamps are
																				// separated by less than equal
																				// to this value and have same mode
																				// name, then they are assumed to be
																				// continuos
	public static final int assumeContinuesBeforeNextInSecs = 2 * 60; // changed from 30 min in DCU dataset we assume
																		// that
	// if two activities have a start time gap of more than 'assumeContinuesBeforeNextInSecs' seconds ,
	// then the first activity continues for 'assumeContinuesBeforeNextInSecs' seconds before the next activity starts.
	public static final int thresholdForMergingNotAvailables = 5 * 60;
	public static final int thresholdForMergingSandwiches = 10 * 60;

	public static final int timeDurationForLastSingletonTrajectoryEntry = 2 * 60;

	// public static final int sandwichFillerDurationInSecs = 10 * 60;

	// ******************END OF PARAMETERS TO SET*****************************//

	public static void main(String args[])
	{
		System.out.println("Running starts");
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 12, 2016

		try
		{
			long ct1 = System.currentTimeMillis();

			Constant.setCommonPath(commonPath);
			// commonPath = Constant.getCommonPath();
			// Redirecting the console output
			PrintStream consoleLogStream = new PrintStream(
					new File(commonPath + "consoleLogDatabaseCreatorGowalla.txt"));
			// System.setOut(new PrintStream(new FileOutputStream('/dev/stdout')));
			System.setOut(new PrintStream(consoleLogStream));
			System.setErr(consoleLogStream);
			// ConnectDatabaseV1.getTimestamp("B00000028_21I5H1_20140216_170559E.JPG,");
			System.out.println("Default timezone = " + TimeZone.getDefault());
			System.out.println("\ncontinuityThresholdInSeconds=" + continuityThresholdInSeconds
					+ "\nassumeContinuesBeforeNextInSecs" + assumeContinuesBeforeNextInSecs
					+ "\thresholdForMergingSandwiches = " + thresholdForMergingSandwiches
					+ "\ntimeDurationForLastSingletonTrajectoryEntry" + timeDurationForLastSingletonTrajectoryEntry);

			//// start of curtian1
			// get root of the category hierarchy tree
			DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
					.deSerializeThis(categoryHierarchyTreeFileName);
			//
			TreeMap<Integer, String> catIDNameDictionary = (TreeMap<Integer, String>) Serializer
					.kryoDeSerializeThis(catIDNameDictionaryFileName);
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/CatIDNameDictionary.kryo");
			// commonPath + "CatIDNameDictionary.kryo");

			int workingCatLevel = Constant.gowallaWorkingCatLevel;

			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict = getWorkingLevelCatIDsForAllCatIDs(
					catIDNameDictionary, workingCatLevel, rootOfCategoryTree);

			Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>> checkinResult = createCheckinEntries(
					checkinDataFileName, commonPath, rootOfCategoryTree, catIDWorkingLevelCatIDsDict);

			mapForAllCheckinData = checkinResult.getFirst();

			userIDsInCheckinData = mapForAllCheckinData.keySet();
			locationIDsInCheckinData = checkinResult.getSecond();
			//
			System.out.println("userIDsInCheckinData.size()=" + userIDsInCheckinData.size());
			System.out.println("locationIDsInCheckinData.size()=" + locationIDsInCheckinData.size());

			mapForAllUserData = createUserGowalla(userDataFileName, userIDsInCheckinData, commonPath);
			mapForAllLocationData = createLocationGowalla0(userLocationFileName, locationIDsInCheckinData, commonPath);

			// Triple<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, LinkedHashMap<String, UserGowalla>,
			// LinkedHashMap<String, LocationGowalla>> allData =
			// new Triple<>(mapForAllCheckinData, mapForAllUserData, mapForAllLocationData);

			// Serializer.serializeThis(allData, commonPath + "GowallaAllData13Sep2016.obj");
			// Serializer.fstSerializeThis2(allData, commonPath + "GowallaAllData13Sep2016.obj");
			Serializer.kryoSerializeThis(mapForAllCheckinData, commonPath + "mapForAllCheckinData.kryo");
			Serializer.kryoSerializeThis(mapForAllUserData, commonPath + "mapForAllUserData.kryo");
			Serializer.kryoSerializeThis(mapForAllLocationData, commonPath + "mapForAllLocationData.kryo");
			// $Serializer.kryoSerializeThis(allData, commonPath + "GowallaAllData13Sep2016.kryo");
			//// end of curtian1

			//
			// // start of curtain deserialisation1
			// Triple<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, LinkedHashMap<String, UserGowalla>,
			// LinkedHashMap<String, LocationGowalla>> allData2 =
			// (Triple<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, LinkedHashMap<String, UserGowalla>,
			// LinkedHashMap<String, LocationGowalla>>) Serializer
			// .kryoDeSerializeThis(commonPath + "GowallaAllData13Sep2016.kryo");
			// // Object test2 = Serializer.fstDeSerializeThis2(commonPath + "GowallaAllData13Sep2016.obj");
			//
			// // Object test2 = Serializer.deSerializeThis(commonPath + "GowallaAllData13Sep2016.obj");
			// // Object test1 = Serializer.fstDeSerializeThis2(commonPath + "GowallaAllData13Sep2016.obj");
			// //
			// // Triple<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, LinkedHashMap<String, UserGowalla>,
			// LinkedHashMap<String, LocationGowalla>> allData2 =
			// // (Triple<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, LinkedHashMap<String, UserGowalla>,
			// LinkedHashMap<String, LocationGowalla>>) Serializer
			// // .fstDeSerializeThis2(commonPath + "GowallaAllData13Sep2016.obj");
			// //
			// // if (allData.getFirst() == allData2.getFirst() && allData.getSecond() == allData2.getSecond()
			// // && allData.getThird() == allData2.getThird())
			// // {
			// // System.out.println("Serilsation deserliation check 1 okay");
			// // }
			// // else
			// // {
			// // System.out.println("Serilsation deserliation check 1 NOT okay");
			// // }
			//
			// if (allData.getFirst().keySet().size() == allData2.getFirst().keySet().size()
			// && allData.getSecond().keySet().size() == allData2.getSecond().keySet().size()
			// && allData.getThird().keySet().size() == allData2.getThird().keySet().size())
			// {
			// System.out.println("Serilsation deserliation check 2 okay");
			// }
			// else
			// {
			// System.out.println("Serilsation deserliation check 2 NOT okay");
			// }
			// // end of curtain deserialisation1

			// $$ LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userDayTimelines =
			// $$ TimelineUtilities.createUserTimelinesFromCheckinEntriesGowalla(mapForAllCheckinData,
			// mapForAllLocationData);

			// $$Serializer.kryoSerializeThis(userDayTimelines, commonPath + "GowallaUserDayTimelines13Sep2016.kryo");

			consoleLogStream.close();

			long ct4 = System.currentTimeMillis();
			PopUps.showMessage("All data creation done in " + ((ct4 - ct1) / 1000) + " seconds since start");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("End of program");
		PopUps.showMessage("End of data creation");
		System.exit(0);
	}

	/**
	 * 
	 * @param catIDNameDictionary
	 * @param workingCatLevel
	 * @param rootOfCategoryTree
	 *            new root, root with newly manually added catids
	 * @return <catid, catid__catid__catid>
	 */
	private static TreeMap<Integer, String> getWorkingLevelCatIDsForAllCatIDs(
			TreeMap<Integer, String> catIDNameDictionary, int workingCatLevel,
			DefaultMutableTreeNode rootOfCategoryTree)
	{
		TreeMap<Integer, String> res = new TreeMap<>();
		StringBuilder sb = new StringBuilder();
		int numOfCatsWithMultipleWorkingLevelCats = 0, numOfCatsNotInHierarchyTree = 0, numOfCatsInHierarchyTree = 0;

		for (Entry<Integer, String> cat : catIDNameDictionary.entrySet())
		{
			Set<String> givenLevelOrAboveCatIDs = UIUtilityBox.getGivenLevelOrAboveCatID(String.valueOf(cat.getKey()),
					rootOfCategoryTree, workingCatLevel);

			String workingLevelCatIDs = "";
			if (givenLevelOrAboveCatIDs.size() > 0)
			{
				workingLevelCatIDs = givenLevelOrAboveCatIDs.stream().reduce((t, u) -> t + "__" + u).get();
				numOfCatsInHierarchyTree += 1;

				if (givenLevelOrAboveCatIDs.size() > 1)
				{
					numOfCatsWithMultipleWorkingLevelCats += 1;
				}
			}
			else
			{
				numOfCatsNotInHierarchyTree += 1;
			}
			res.put(cat.getKey(), workingLevelCatIDs);
			sb.append(cat.getKey() + "," + workingLevelCatIDs + "\n");
		}
		//
		WritingToFile.writeToNewFile(sb.toString(), commonPath + "MapForgetWorkingLevelCatIDsForAllCatIDs.csv");

		String s = "numOfCatsWithMultipleWorkingLevelCats = " + numOfCatsWithMultipleWorkingLevelCats
				+ "\nnumOfCatsNotInHierarchyTree = " + numOfCatsNotInHierarchyTree + "\nnumOfCatsInHierarchyTree = "
				+ numOfCatsInHierarchyTree;

		WritingToFile.appendLineToFileAbsolute(s, commonPath + "MapForgetWorkingLevelCatIDsForAllCatIDs.csv");

		return res;
	}

	/**
	 * <p>
	 * Read the checkin file and create checkin entry objects
	 * </p>
	 * <p>
	 * 
	 * </p>
	 * 
	 * @param checkindatafilename2
	 * @param commonPath2
	 * @param rootOfCategoryTree
	 * @param workingLevelForCat
	 * @param rootOfCategoryTree
	 * @param catIDWorkingLevelCatIDsDict
	 * @return
	 */

	private static Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>>

			createCheckinEntries(String checkinFileNameToRead, String commonPath,
					DefaultMutableTreeNode rootOfCategoryTree, TreeMap<Integer, String> catIDWorkingLevelCatIDsDict)
	{
		int countOfCheckinEntryObjects = 0;
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> result = new LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>();

		Set<String> locationIDsInCheckinData = new HashSet<String>();

		int countOfLines = 0;

		String lineRead;

		ArrayList<Integer> notFoundInFlatMap = new ArrayList<Integer>();
		ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();

		StringBuffer logRejectedCheckins = new StringBuffer();
		long countOfRejectedCheckinNotInHierarchy = 0, countOfRejectedCHeckinBelowLevel2 = 0;
		System.out.println("----Inside createCheckinEntries----------------");
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(checkinFileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;

				if (countOfLines == 1)
				{
					continue; // skip the header line
				}

				if (countOfLines % 200000 == 0)
				{
					System.out.println(" #lines read = " + countOfLines);
				}

				String splittedLine[] = lineRead.split(",");

				String userID = splittedLine[1];// .replaceAll("\"", ""));
				Integer locationID = Integer.valueOf(splittedLine[2]);// .replaceAll("\"", ""));
				locationIDsInCheckinData.add(locationID.toString());
				Timestamp ts = Timestamp.from(Instant.parse(splittedLine[3].replaceAll("\"", "")));
				String latitude = new String(splittedLine[5]);
				String longitude = new String(splittedLine[6]);
				Integer catIDDirect = Integer.valueOf(splittedLine[7]);// .replaceAll("\"", ""));
				Double distFromNextInM = Double.valueOf(splittedLine[9]);
				Long durationFromNextInM = Long.valueOf(splittedLine[10]);
				// String catName = splittedLine[8].replaceAll("\"", "");
				// Set<String> givenLevelOrAboveCatIDs =
				// UIUtilityBox.getGivenLevelOrAboveCatID(splittedLine[7], rootOfCategoryTree, workingLevelForCat);
				//
				// String workingLevelCatIDs = "";
				// if (givenLevelOrAboveCatIDs.size() > 0)
				// {
				// workingLevelCatIDs = givenLevelOrAboveCatIDs.stream().reduce((t, u) -> t + "__" + u).get();
				// }
				// // String workingLevelCatIDs =
				Pair<Boolean, String> isAcceptableDirectCatID = isAcceptableDirectCatID(catIDDirect,
						rootOfCategoryTree);

				if (isAcceptableDirectCatID.getFirst() == false)
				{
					String reason = isAcceptableDirectCatID.getSecond();
					// maintaining a log of rejected checkins with the reason for rejection
					logRejectedCheckins.append(countOfLines + "-" + reason + "\n");
					if (reason.equals("NotInHierarchy"))
					{
						countOfRejectedCheckinNotInHierarchy += 1;
					}
					else if (reason.equals("LevelNotAcceptable"))
					{
						countOfRejectedCHeckinBelowLevel2 += 1;
					}
					// int countOfRejectedCheckinNotInHierarchy = 0, countOfRejectedCHeckinBelowLevel2 = 0;
					continue;
				}

				String workingLevelCatIDs = catIDWorkingLevelCatIDsDict.get(catIDDirect);

				CheckinEntry cobj = new CheckinEntry(userID, locationID, ts, latitude, longitude, catIDDirect,
						workingLevelCatIDs, distFromNextInM, durationFromNextInM);

				countOfCheckinEntryObjects++;

				TreeMap<Timestamp, CheckinEntry> mapForThisUser;

				mapForThisUser = result.get(userID); // if userid already in map

				if (mapForThisUser == null) // else create new map for this userid
				{
					mapForThisUser = new TreeMap<Timestamp, CheckinEntry>();
				}

				mapForThisUser.put(ts, cobj);
				result.put(userID, mapForThisUser);
			}

			System.out.println("num of users = " + result.size());
			System.out.println("num of lines read = " + countOfLines);

			System.out.println("num of lines NotInHierarchy = " + countOfRejectedCheckinNotInHierarchy);
			System.out.println("num of lines LevelNotAcceptable = " + countOfRejectedCHeckinBelowLevel2);
			System.out.println("num of CheckinEntry objects created = " + countOfCheckinEntryObjects);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		WritingToFile.appendLineToFileAbsolute(logRejectedCheckins.toString(), commonPath + "RejectedCheckinsLog.txt");

		System.out.println("----Exiting createCheckinEntries----------------");
		return new Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>>(result,
				locationIDsInCheckinData);

	}

	/**
	 * Checks if the category id is present in hierarchy tree and if the level is more than level 1.
	 * 
	 * @param catIDDirect
	 * @param rootOfCategoryTree
	 * @return pair which can be either of (true, "Acceptable"),(false, "NotInHierarchy"),(false, "LevelNotAcceptable")
	 */
	private static Pair<Boolean, String> isAcceptableDirectCatID(Integer catIDToSearch,
			DefaultMutableTreeNode rootOfCategoryTree)
	{
		ArrayList<DefaultMutableTreeNode> foundNodes = UIUtilityBox.recursiveDfsMulipleOccurences2OnlyCatID(
				rootOfCategoryTree, catIDToSearch.toString(), new ArrayList<DefaultMutableTreeNode>());

		// System.out.println("num of matching nodes found = " + foundNodes2.size());
		boolean isLevelAcceptable = false;

		if (foundNodes.size() > 0) // is it there in hierarchy tree
		{

			for (DefaultMutableTreeNode foundnode : foundNodes)
			{
				if (foundnode.getLevel() >= 2) // atleast level 2
				{
					isLevelAcceptable = true;
					return new Pair<Boolean, String>(true, "Acceptable");
				}
				// System.out.println("Foundnode = " + foundnode.toString());
				// System.out.println("Foundnode depth = " + foundnode.getLevel());
				// System.out.println("Foundnode path = " + Arrays.toString(foundnode.getPath()));
			}
		}
		else
		{
			return new Pair<Boolean, String>(false, "NotInHierarchy");
		}

		return new Pair<Boolean, String>(false, "LevelNotAcceptable");
	}

	/**
	 * <p>
	 * Read the user file and create user objects for users which were in checkin data (i.e.,userIDsInCheckinData)
	 * </p>
	 * <p>
	 * 
	 * </p>
	 * 
	 * 
	 * @param userFileNameToRead
	 * @param userIDsInCheckinData
	 * @param commonPath
	 * @return
	 */
	private static LinkedHashMap<String, UserGowalla> createUserGowalla(String userFileNameToRead,
			Set<String> userIDsInCheckinData, String commonPath)
	{

		LinkedHashMap<String, UserGowalla> result = new LinkedHashMap<String, UserGowalla>();
		int countOfLines = 0;
		String lineRead;

		System.out.println("----Inside createUserGowalla----------------");
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(userFileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;

				if (countOfLines == 1)
				{
					continue; // skip the header line
				}

				if (countOfLines % 200000 == 0)
				{
					System.out.println(" #lines read = " + countOfLines);
				}

				String splittedLine[] = lineRead.split(",");

				String userID = splittedLine[0];

				if (userIDsInCheckinData.contains(userID)) // only if this userid is in ur checkin data
				{
					UserGowalla cobj = new UserGowalla(userID, "", "", "", "", -1, Integer.valueOf(splittedLine[1]),
							Integer.valueOf(splittedLine[2]), Integer.valueOf(splittedLine[3]),
							Integer.valueOf(splittedLine[4]), Integer.valueOf(splittedLine[5]),
							Integer.valueOf(splittedLine[6]), Integer.valueOf(splittedLine[7]),
							Integer.valueOf(splittedLine[8]), Integer.valueOf(splittedLine[9]),
							Integer.valueOf(splittedLine[10]), Integer.valueOf(splittedLine[11]),
							Integer.valueOf(splittedLine[12]), Integer.valueOf(splittedLine[13]),
							Integer.valueOf(splittedLine[14]), Integer.valueOf(splittedLine[15]));

					result.put(userID, cobj);
				}
			}

			System.out.println("num of UserGowalla objects = " + result.size());
			System.out.println("num of lines read = " + countOfLines);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("----Exiting createCheckinEntries----------------");
		return result;

	}

	/**
	 * <p>
	 * Read the location file and create location gowalla objects for locs which were in checkin data
	 * (i.e.,locationIDsInCheckinData)
	 * </p>
	 * <p>
	 * 
	 * </p>
	 * 
	 * 
	 * @param userFileNameToRead
	 * @param userIDsInCheckinData
	 * @param commonPath
	 * @return
	 */
	private static LinkedHashMap<String, LocationGowalla> createLocationGowalla0(String locationFileNameToRead,
			Set<String> locationIDsInCheckinData, String commonPath)
	{

		LinkedHashMap<String, LocationGowalla> result = new LinkedHashMap<>();
		int countOfLines = 0;
		String lineRead;

		System.out.println("----Inside createLocationGowalla0----------------");
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(locationFileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;

				if (countOfLines == 1)
				{
					continue; // skip the header line
				}

				if (countOfLines % 200000 == 0)
				{
					System.out.println(" #lines read = " + countOfLines);
				}

				String splittedLine[] = lineRead.split(",");

				String locID = splittedLine[0];

				if (locationIDsInCheckinData.contains(locID)) // only if this userid is in ur checkin data
				{
					Pair<String, String> spotCatIDName = DatabaseCreatorGowallaQuickerPreprocessor.getSpotCatIDName(
							splittedLine[splittedLine.length - 2] + "," + splittedLine[splittedLine.length - 1]);
					String spotCatID = spotCatIDName.getFirst();
					String spotCatName = spotCatIDName.getSecond();

					LocationGowalla cobj = new LocationGowalla(splittedLine[3], splittedLine[2], spotCatName, spotCatID,
							"", "", "", "", locID, Integer.valueOf(splittedLine[4]), Integer.valueOf(splittedLine[5]),
							Integer.valueOf(splittedLine[6]), Integer.valueOf(splittedLine[7]),
							Integer.valueOf(splittedLine[8]), Integer.valueOf(splittedLine[9]),
							Integer.valueOf(splittedLine[10]));

					// (String lat, String lon, String locName, String locCat, String city, String county, String
					// country,
					// String continent, String locationID, int photos_count, int checkins_count, int users_count, int
					// radius_meters,
					// int highlights_count, int items_count, int max_items_count)
					result.put(locID, cobj);
				}
			}

			System.out.println("num of location objects = " + result.size());
			System.out.println("num of lines read = " + countOfLines);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("----Exiting createLocationGowalla0----------------");
		return result;

	}

	/**
	 * THIS METHOD WAS FOUND TO BE TAKING TOO MUCH TIME TO RUN HENCE, createLocationGowalla0() was created as an
	 * alternative.
	 * <p>
	 * Read the location file and create location gowalla objects (Note: uses readSpotSubsetWithFilter())for locs which
	 * were in checkin data (i.e.,locationIDsInCheckinData)
	 * </p>
	 * <p>
	 * Alert: locid as string
	 * </p>
	 * 
	 * @param locationIDsInCheckinData
	 * 
	 * @param checkindatafilename2
	 * @param commonPath2
	 * @param rootOfCategoryTree
	 * @return
	 */
	private static LinkedHashMap<String, LocationGowalla> createLocationGowalla(String locationFileNameToRead,
			Set<String> locationIDsInCheckinData, String commonPath)
	{

		LinkedHashMap<String, LocationGowalla> result = new LinkedHashMap<String, LocationGowalla>();
		int countOfLines = 0;
		String lineRead;

		System.out.println("----Inside createLocationGowalla----------------");

		System.out.println("locationIDsInCheckinData.size()=" + locationIDsInCheckinData.size());
		try
		{
			HashMap<String, ArrayList<String>> spots1 = readSpotSubsetWithFilter(locationFileNameToRead,
					locationIDsInCheckinData);

			for (Entry<String, ArrayList<String>> locEntry : spots1.entrySet())
			{
				String locID = locEntry.getKey();

				ArrayList<String> rest = locEntry.getValue();

				Pair<String, String> spotCatIDName = DatabaseCreatorGowallaQuickerPreprocessor
						.getSpotCatIDCatName(rest);

				String spotCatID = spotCatIDName.getFirst();
				String spotCatName = spotCatIDName.getSecond();

				LocationGowalla cobj = new LocationGowalla(rest.get(2), rest.get(1), spotCatName, spotCatID, "", "", "",
						"", locID, Integer.valueOf(rest.get(3)), Integer.valueOf(rest.get(4)),
						Integer.valueOf(rest.get(5)), Integer.valueOf(rest.get(6)), Integer.valueOf(rest.get(7)),
						Integer.valueOf(rest.get(8)), Integer.valueOf(rest.get(9)));

				result.put(String.valueOf(locID), cobj);

				// String lat, String lon, String locName, String locCat, String city, String county, String country,
				// String continent, int locationID, int photos_count, int checkins_count, int users_count, int
				// radius_meters,
				// int highlights_count, int items_count, int max_items_count)

				// id,created_at,lng,lat,photos_count,checkins_count,users_count,radius_meters,highlights_count,items_count,max_items_count,spot_categories

			}

			System.out.println("num of locs = " + result.size());

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("----Exiting createLocationGowalla----------------");
		return result;

	}

	/**
	 * To read file: gowalla_spots_subset1.csv and 2
	 * <p>
	 * Read each line into a HashMap with first col being the key and res of the columns as array list of strings
	 * </p>
	 * 
	 * @param fileName
	 * @return map with each lines
	 */
	private static HashMap<String, ArrayList<String>> readSpotSubsetWithFilter(String fileName,
			Set<String> locationIDsInCheckinData)
	{
		System.out.println("Inside readSpotSubsetWithFilter-----");
		System.out.println("locationIDsInCheckinData.size()=" + locationIDsInCheckinData.size());

		String dlimPatrn = Pattern.quote(",");
		HashMap<String, ArrayList<String>> map1 = new HashMap<String, ArrayList<String>>();

		try
		{

			int lineCount = 0;
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			String currentLine;
			while ((currentLine = br.readLine()) != null)
			{
				lineCount++;

				if (lineCount == 1)
				{
					System.out.println("Skipping first line");
					continue; // skip the first line
				}
				else if (lineCount % 200000 == 0)
				{
					System.out.println("Lines read = " + lineCount);
				}

				String[] splittedString = currentLine.split(dlimPatrn);

				ArrayList<String> vals = new ArrayList<String>();
				String key = "";
				boolean first = true;
				for (String s : splittedString)
				{
					if (first == true) // first string becomes the key while the rest will be added as vals to the
										// arraylist
					{
						first = false;
						key = s;
						continue;
					}
					if (s.length() > 0) vals.add(s);
				}

				if (key.length() == 0)
				{
					System.err.println("Error: location ID empty " + splittedString[0]);
				}

				if (map1.containsKey(key))
				{
					PopUps.showError("Error in readSpotSubset: multiple entries with same key = " + key);
				}

				if (locationIDsInCheckinData.contains(key)) // only if locid is in checkindata
				{
					map1.put(key, vals);
				}
				// System.out.print(lineCount + " Line read = " + currentLine);
				// System.out.print(" putting in map");
				// System.out.println(" map size =" + map1.size());
			}
			// List<CSVRecord> csvRecords = ReadingFromFile.getCSVRecords(fileName);

			br.close();
			// db.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("returned map is of size: " + map1.size());
		return map1;
	}

}
