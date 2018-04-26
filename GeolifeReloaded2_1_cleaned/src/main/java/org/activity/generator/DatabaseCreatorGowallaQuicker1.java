package org.activity.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
//import java.math.String;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.CheckinEntry;
import org.activity.objects.CheckinEntryV2;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.UserGowalla;
import org.activity.sanityChecks.Sanity;
import org.activity.spatial.SpatialUtils;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.ui.UIUtilityBox;
import org.activity.util.RegexUtils;
import org.activity.util.StringCode;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

/**
 * Forked from DatabaseCreatorGowallaQuicker0,primarily because it uses CheckinEntryV2 which is an extension of
 * CheckingEntry. Reads USED AS OF 26 APRIL 2017
 * 
 * @author gunjan
 * @since 3 April 2018
 */
public class DatabaseCreatorGowallaQuicker1
{

	// static ArrayList<String> modeNames;
	// static Set<String> userIDsInCheckinData, locationIDsInCheckinData;
	// static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataTimeDifference;
	// static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedContinuousWithDuration;
	// static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedSandwichedWithDuration;
	// static List<String> userIDsOriginal;static List<String> userIDs;static String dataSplitLabel;

	// ******************PARAMETERS TO SET*****************************//
	// commented out on March 23 2018
	// public static String commonPath =
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Aug10/DatabaseCreatedMerged/";

	// added on April 3 2018
	public static String commonPath = "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/Mar27/DatabaseCreatedMerging/";

	// added on March 23 2018, commented out on April 3 2018
	// "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/Mar15/DatabaseCreatedMerged/";// seagate

	// commented out on Aug 10 2017
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/May17/DatabaseCreatedMerged/";
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/April6/DatabaseCreatedUnMerged/";
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Mar30/DatabaseCreatedMerged/";
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb23/DatabaseCreatedNoMerge/";
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/DatabaseCreated/";
	// commented out on 2 feb 2017
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Dec1/DatabaseCreation/";
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/";
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/";
	// Data Works/";
	public static final String rawPathToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/";
	// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another
	// source/gowalla/";

	public static final String checkinDataFileName = "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnlyMar29.csv";

	// Added on March 23, 2018, commented out on April 3 2018
	// "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/Mar15/RsubsettedData/gw2CheckinsSpots1TargetUsersDatesOnlyWeekdayOnly19Mar2018.csv";
	// commented out on March 23 2018
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Aug10/RSubsettedData/gw2CheckinsSpots1TargetUsersDatesOnly10Aug2017.csv";
	// commented out on Aug 10 2017
	// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/RSubsettedData/gw2CheckinsSpots1TargetUsersDatesOnly2Feb2017.csv";

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

	static final int gowallaContinuityThresholdInSecs = 10 * 60;
	public static final int continuityThresholdInSeconds = gowallaContinuityThresholdInSecs;// = Integer.MAX_VALUE;//
	public static final int continuityThresholdInMeters = 600;// = Integer.MAX_VALUE;//

	static final boolean merge = true;// false;// false;// true;// false;
	// * 60; // changed from 30 min in DCU dataset...., if two timestamps are separated by less than equal
	// to this value and have same mode name, then they are assumed to be continuos
	// public static final int assumeContinuesBeforeNextInSecs = 600; // changed from 30 min in DCU dataset we assume
	// public static final int assumeContinuesBeforeNextInMeters = 600; // that

	// if two activities have a start time gap of more than 'assumeContinuesBeforeNextInSecs' seconds ,
	// then the first activity continues for 'assumeContinuesBeforeNextInSecs' seconds before the next activity starts.

	// public static final int thresholdForMergingNotAvailables = 5 * 60;
	// public static final int thresholdForMergingSandwiches = 10 * 60;
	//
	// public static final int timeDurationForLastSingletonTrajectoryEntry = 2 * 60;

	// public static final int sandwichFillerDurationInSecs = 10 * 60;

	// ******************END OF PARAMETERS TO SET*****************************//
	final static boolean disableExpensiveWriting = false;// true;

	public static void main(String args[])
	{
		System.out.println("Running starts:  " + LocalDateTime.now());
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));// added on April 12, 2016

		// $$dataCreator1(); useful

		// String commonPathApril8 =
		// "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/April8DataBeforeJavaExperiments/";
		// String checkinDataFileNameApril8 =
		// "/home/gunjan/RWorkspace/GowallaRWorks/gwCinsTarUDOnly_Merged_TarUDOnly_ChicagoTZ_TargetUsersDatesOnly_NVFUsers_GTE90Chicago200Users_April8.csv";

		// String commonPathApril23 =
		// "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/April23DataBeforeJavaExperiments/";
		// String checkinDataFileNameApril23 =
		// "/home/gunjan/RWorkspace/GowallaRWorks/gwCinsTarUDOnly_Merged_TarUDOnly_ChicagoTZ_TargetUsersDatesOnly_April8_5_5VFELT1.csv";

		String commonPathApril25 = "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/April25DataBeforeJavaExperiments/";
		String checkinDataFileNameApril25 = "/home/gunjan/RWorkspace/GowallaRWorks/gwCinsTarUDOnly_Merged_TarUDOnly_ChicagoTZ_TarUDSubBOnly_April24_5_5VFELT1.csv";

		dataCreator3_April8(commonPathApril25, checkinDataFileNameApril25, userDataFileName, userLocationFileName,
				catIDNameDictionaryFileName, categoryHierarchyTreeFileName, DomainConstants.gowallaWorkingCatLevel);

		// Start of 8 April 2018
		// TreeMap<Integer, String> catIDNameDictionary = (TreeMap<Integer, String>) Serializer
		// .kryoDeSerializeThis(catIDNameDictionaryFileName);
		// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> mapForAllCheckinData = mapForAllCheckinData =
		// (LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>) Serializer
		// .kryoDeSerializeThis(commonPath + "mapForAllCheckinData.kryo");
		// WritingToFile.writeLinkedHashMapOfTreemapCheckinEntryV2_ForRecreating(mapForAllCheckinData,
		// commonPath + "mapForAllCheckinDataAfterMerged_8April.csv", catIDNameDictionary);
		// End of 8 April 2018

		System.out.println("End of program");
		PopUps.showMessage("End of data creation");
		System.exit(0);
	}

	private static void createCheckinEntries()

	{

	}

	/**
	 * Note: before 8 April 2018, this was the main method.
	 */
	public static void dataCreator1()
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
					+ " continuityThresholdInMeters" + continuityThresholdInMeters);

			//// start of curtian1
			// get root of the category hierarchy tree
			DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
					.deSerializeThis(categoryHierarchyTreeFileName);
			//
			TreeMap<Integer, String> catIDNameDictionary = (TreeMap<Integer, String>) Serializer
					.kryoDeSerializeThis(catIDNameDictionaryFileName);
			WToFile.writeMapToNewFile(catIDNameDictionary, "catID,catName", ",",
					commonPath + "catIDNameDictionary.csv");
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/CatIDNameDictionary.kryo");
			// commonPath + "CatIDNameDictionary.kryo");

			int workingCatLevel = DomainConstants.gowallaWorkingCatLevel;

			Pair<TreeMap<Integer, String>, LinkedHashSet<Integer>> catIDWorkingLevelCatIDsDictResult = getWorkingLevelCatIDsForAllCatIDs(
					catIDNameDictionary, workingCatLevel, rootOfCategoryTree);

			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict = catIDWorkingLevelCatIDsDictResult.getFirst();
			WToFile.writeMapToNewFile(catIDWorkingLevelCatIDsDict, "catID,WorkingLevelCatID", ",",
					commonPath + "catIDWorkingLevelCatIDsDict.csv");

			LinkedHashSet<Integer> catIDsInHierarchy = catIDWorkingLevelCatIDsDictResult.getSecond();
			WToFile.writeToNewFile(catIDsInHierarchy.toString(), commonPath + "UniqueCatIDsInHierarchy.csv");

			TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict = getLevelWiseCatIDsForAllCatIDs(catIDNameDictionary,
					rootOfCategoryTree, DomainConstants.numOfCatLevels);
			WToFile.writeMapOfArrayValsToNewFile(catIDLevelWiseCatIDsDict, "catID,LevelWiseCatIDs", ",",
					commonPath + "catIDLevelWiseCatIDsDict.csv");

			HashMap<String, Double> mapCatIDsHierDist = null;
			// disabled temporarily createCatIDsHierarchicalDistMap(catIDLevelWiseCatIDsDict,
			// catIDNameDictionary, catIDsInHierarchy);

			///////////////////////// Start of sanity check
			// sanity check to verify if no cat id has empty working lvel cat ids
			System.out.println("Sanity Check: printing all catIDWorkingLevelCatIDsDict with val length > 0");
			catIDWorkingLevelCatIDsDict.entrySet().stream().filter(e -> e.getValue().length() > 0)
					.forEach(e -> System.out.println(e.getKey() + "-" + e.getValue()));

			System.out.println("num of catIDWorkingLevelCatIDsDict with val length > 1="
					+ catIDWorkingLevelCatIDsDict.entrySet().stream().filter(e -> e.getValue().length() > 1).count());
			// .forEach(e -> System.out.println(e.getKey() + "--" + e.getValue()));

			System.out.println("Sanity Check: printing all catIDWorkingLevelCatIDsDict with val length = 0");
			catIDWorkingLevelCatIDsDict.entrySet().stream().filter(e -> e.getValue().length() == 0)
					.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue()));

			System.out.println("Sanity Check: printing all catIDWorkingLevelCatIDsDict with val length > 3");
			catIDWorkingLevelCatIDsDict.entrySet().stream().filter(e -> e.getValue().length() > 3)
					.forEach(e -> System.out.println(e.getKey() + "-" + e.getValue()));

			System.out.println("Sanity Check: printing all catIDLevelWiseCatIDsDict");
			catIDLevelWiseCatIDsDict.entrySet().stream()
					.forEach(e -> System.out.println(e.getKey() + "-" + Arrays.toString(e.getValue())));
			///////////////////////// END of sanity check

			////
			// used in create checkin entries to determine if a cat id is acceptable
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap = UIUtilityBox
					.getCatIDsFoundNodesMap(rootOfCategoryTree, catIDNameDictionary);

			////
			// Disabled on Mar 23 2018
			// Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>> unmergedCheckinResult =
			// createCheckinEntries(
			// checkinDataFileName, commonPath, rootOfCategoryTree, catIDWorkingLevelCatIDsDict,
			// catIDsFoundNodesMap, workingCatLevel, catIDLevelWiseCatIDsDict);

			Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>> unmergedCheckinResult = // createCheckinEntries23Mar2018
					createCheckinEntries3April2018(checkinDataFileName, commonPath, rootOfCategoryTree,
							catIDWorkingLevelCatIDsDict, catIDsFoundNodesMap, workingCatLevel,
							catIDLevelWiseCatIDsDict);

			mapForAllCheckinData = unmergedCheckinResult.getFirst();

			long numOfCheckins = mapForAllCheckinData.entrySet().stream().mapToLong(e -> e.getValue().size()).sum();
			System.out.println("num of checkins = " + numOfCheckins); // 6276222
			// PopUps.showMessage("num of checkins = " + numOfCheckins);

			////// consecutive same analysis
			// countConsecutiveSimilarActivities2(mapForAllCheckinData, commonPath, catIDNameDictionaryFileName);
			// $$Function<CheckinEntryV2, String> consecCompareDirectCatID = ce -> String.valueOf(ce.getActivityID());
			Function<CheckinEntryV2, String> consecCompareLocationID = ce -> String.valueOf(ce.getLocationIDs().get(0));// String.valueOf(ce.getFirstLocationID());
			countConsecutiveSimilarActivities3_3April2018(mapForAllCheckinData, commonPath, catIDNameDictionaryFileName,
					consecCompareLocationID);// consecCompareDirectCatID);

			// System.exit(0);
			/////
			System.out.println("ALERT! merge = " + merge);
			if (merge)
			{
				if (!disableExpensiveWriting) // skipping writing on Aug 10 for performance.
				{
					WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2(mapForAllCheckinData,
							commonPath + "mapForAllCheckinDataBeforeMerged.csv", catIDNameDictionary);
				} /////
					// merge
					// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinDataMerged
				mapForAllCheckinData = DatageneratorUtils.mergeContinuousGowallaWithoutBOD4_3April2018(
						mapForAllCheckinData, commonPath, continuityThresholdInSeconds, continuityThresholdInMeters);

				if (!disableExpensiveWriting)// skipping writing on Aug 10 for performance.
				{
					WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2(mapForAllCheckinData,
							commonPath + "mapForAllCheckinDataAfterMerged.csv", catIDNameDictionary);

					WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2_ForRecreating(mapForAllCheckinData,
							commonPath + "mapForAllCheckinDataAfterMerged_8April.csv", catIDNameDictionary);

				}
			}
			else
			{
				WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2(mapForAllCheckinData,
						commonPath + "mapForAllCheckinNoMerging.csv", catIDNameDictionary);
			}

			userIDsInCheckinData = mapForAllCheckinData.keySet();
			locationIDsInCheckinData = unmergedCheckinResult.getSecond();
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
			Serializer.kryoSerializeThis(mapCatIDsHierDist, commonPath + "mapCatIDsHierDist.kryo");
			Serializer.kryoSerializeThis(catIDLevelWiseCatIDsDict, commonPath + "mapCatIDLevelWiseCatIDsDict.kryo");
			// catIDsHierDistDict
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
		// System.out.println("End of program");
		// PopUps.showMessage("End of data creation");
		// System.exit(0);
	}

	/**
	 * 
	 * @param commonPathP
	 * @param checkinDataFileNameP
	 * @param userDataFileNameP
	 * @param userLocationFileNameP
	 * @param catIDNameDictionaryFileNameP
	 * @param categoryHierarchyTreeFileNameP
	 * @param workingCatLevelP
	 * @since April 8 2018
	 */
	public static void dataCreator3_April8(String commonPathP, String checkinDataFileNameP, String userDataFileNameP,
			String userLocationFileNameP, String catIDNameDictionaryFileNameP, String categoryHierarchyTreeFileNameP,
			int workingCatLevelP)
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
			Constant.setCommonPath(commonPathP);
			// commonPath = Constant.getCommonPath();
			// Redirecting the console output
			PrintStream consoleLogStream = new PrintStream(
					new File(commonPathP + "consoleLogDatabaseCreatorGowalla.txt"));
			// System.setOut(new PrintStream(new FileOutputStream('/dev/stdout')));
			System.setOut(new PrintStream(consoleLogStream));
			System.setErr(consoleLogStream);
			System.out.println("Default timezone = " + TimeZone.getDefault());

			DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
					.deSerializeThis(categoryHierarchyTreeFileNameP);
			TreeMap<Integer, String> catIDNameDictionary = (TreeMap<Integer, String>) Serializer
					.kryoDeSerializeThis(catIDNameDictionaryFileNameP);
			WToFile.writeMapToNewFile(catIDNameDictionary, "catID,catName", ",",
					commonPathP + "catIDNameDictionary.csv");
			// int workingCatLevel = DomainConstants.gowallaWorkingCatLevel;

			Pair<TreeMap<Integer, String>, LinkedHashSet<Integer>> catIDWorkingLevelCatIDsDictResult = getWorkingLevelCatIDsForAllCatIDs(
					catIDNameDictionary, workingCatLevelP, rootOfCategoryTree);

			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict = catIDWorkingLevelCatIDsDictResult.getFirst();
			WToFile.writeMapToNewFile(catIDWorkingLevelCatIDsDict, "catID,WorkingLevelCatID", ",",
					commonPathP + "catIDWorkingLevelCatIDsDict.csv");

			LinkedHashSet<Integer> catIDsInHierarchy = catIDWorkingLevelCatIDsDictResult.getSecond();
			WToFile.writeToNewFile(catIDsInHierarchy.toString(), commonPathP + "UniqueCatIDsInHierarchy.csv");

			TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict = getLevelWiseCatIDsForAllCatIDs(catIDNameDictionary,
					rootOfCategoryTree, DomainConstants.numOfCatLevels);
			WToFile.writeMapOfArrayValsToNewFile(catIDLevelWiseCatIDsDict, "catID,LevelWiseCatIDs", ",",
					commonPathP + "catIDLevelWiseCatIDsDict.csv");

			HashMap<String, Double> mapCatIDsHierDist = null;
			// disabled temporarily createCatIDsHierarchicalDistMap(catIDLevelWiseCatIDsDict,
			// catIDNameDictionary, catIDsInHierarchy);

			// used in create checkin entries to determine if a cat id is acceptable
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap = UIUtilityBox
					.getCatIDsFoundNodesMap(rootOfCategoryTree, catIDNameDictionary);

			Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>> checkinResult = createCheckinEntries8April2018(
					checkinDataFileNameP, commonPathP, rootOfCategoryTree, catIDWorkingLevelCatIDsDict,
					catIDsFoundNodesMap, workingCatLevelP, catIDLevelWiseCatIDsDict);

			mapForAllCheckinData = checkinResult.getFirst();
			long numOfCheckins = mapForAllCheckinData.entrySet().stream().mapToLong(e -> e.getValue().size()).sum();
			System.out.println("num of checkins = " + numOfCheckins); // 6276222
			// PopUps.showMessage("num of checkins = " + numOfCheckins);

			////// consecutive same analysis
			// countConsecutiveSimilarActivities2(mapForAllCheckinData, commonPath, catIDNameDictionaryFileName);
			// $$Function<CheckinEntryV2, String> consecCompareDirectCatID = ce -> String.valueOf(ce.getActivityID());
			Function<CheckinEntryV2, String> consecCompareLocationID = ce -> String.valueOf(ce.getLocationIDs().get(0));// String.valueOf(ce.getFirstLocationID());
			countConsecutiveSimilarActivities3_3April2018(mapForAllCheckinData, commonPathP,
					catIDNameDictionaryFileNameP, consecCompareLocationID);// consecCompareDirectCatID);

			// WritingToFile.writeLinkedHashMapOfTreemapCheckinEntryV2(mapForAllCheckinData,
			// commonPathP + "mapForAllCheckinData.csv", catIDNameDictionary);
			// compare this filr to the dataset read to ensure that the read dataset and the Checkin objcts created are
			// equivalent
			WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2_ForRecreating(mapForAllCheckinData,
					commonPathP + "mapForAllCheckinDataAfterMerged_8AprilSanityCheck.csv", catIDNameDictionary);

			userIDsInCheckinData = mapForAllCheckinData.keySet();
			locationIDsInCheckinData = checkinResult.getSecond();
			System.out.println("userIDsInCheckinData.size()=" + userIDsInCheckinData.size());
			System.out.println("locationIDsInCheckinData.size()=" + locationIDsInCheckinData.size());

			mapForAllUserData = createUserGowalla(userDataFileNameP, userIDsInCheckinData, commonPathP);
			mapForAllLocationData = createLocationGowalla0(userLocationFileNameP, locationIDsInCheckinData,
					commonPathP);

			// Triple<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, LinkedHashMap<String, UserGowalla>,
			// LinkedHashMap<String, LocationGowalla>> allData =
			// new Triple<>(mapForAllCheckinData, mapForAllUserData, mapForAllLocationData);

			// Serializer.serializeThis(allData, commonPath + "GowallaAllData13Sep2016.obj");
			// Serializer.fstSerializeThis2(allData, commonPath + "GowallaAllData13Sep2016.obj");
			Serializer.kryoSerializeThis(mapForAllCheckinData, commonPathP + "mapForAllCheckinData.kryo");
			Serializer.kryoSerializeThis(mapForAllUserData, commonPathP + "mapForAllUserData.kryo");
			Serializer.kryoSerializeThis(mapForAllLocationData, commonPathP + "mapForAllLocationData.kryo");
			Serializer.kryoSerializeThis(mapCatIDsHierDist, commonPathP + "mapCatIDsHierDist.kryo");
			Serializer.kryoSerializeThis(catIDLevelWiseCatIDsDict, commonPathP + "mapCatIDLevelWiseCatIDsDict.kryo");
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

	/**
	 * Fork of dataCreator1. dataCreator1() was to convert R susbetted dataset into CheckinEntries and then merge the
	 * data.
	 * <p>
	 * Not used as of 8 April 2018, instead of calling this method, i will deserialised the previously created merged
	 * checkinentries and then write them again.
	 * 
	 * <p>
	 * dataCreator2() is improved dataCreator1() to convert final R subsetted data into CheckinEntries ready for java
	 * experiments. I believe it can replace dataCreator1() but needs to be checked thoroughly for that.
	 * <p>
	 * Trying to pass parameters inetad of using global (class) variables.
	 * 
	 * @param commonPath
	 * @param doMerge
	 * @param disableExpensiveWriting
	 * @param continuityThresholdInSeconds
	 * @param continuityThresholdInMeters
	 * @param categoryHierarchyTreeFileName
	 * @param catIDNameDictionaryFileName
	 * @param workingCatLevel
	 * @param numOfCatLevels
	 * @param checkinDataFileName
	 * @param userDataFileName
	 * @param userLocationFileName
	 * @since April 8 2018
	 * 
	 *        Note: before 8 April 2018, this was the main method.
	 */
	protected static void dataCreator2(String commonPath, boolean doMerge, boolean disableExpensiveWriting,
			int continuityThresholdInSeconds, int continuityThresholdInMeters, String categoryHierarchyTreeFileName,
			String catIDNameDictionaryFileName, int workingCatLevel, int numOfCatLevels, String checkinDataFileName,
			String userDataFileName, String userLocationFileName)
	{
		long ct1 = System.currentTimeMillis();
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> mapForAllCheckinData;
		LinkedHashMap<Integer, LocationGowalla> mapForAllLocationData;
		LinkedHashMap<String, UserGowalla> mapForAllUserData;
		Set<String> userIDsInCheckinData, locationIDsInCheckinData;

		try
		{
			Constant.setCommonPath(commonPath);// commonPath = Constant.getCommonPath();
			// Redirecting the console output
			PrintStream consoleLogStream = new PrintStream(
					new File(commonPath + "consoleLogDatabaseCreatorGowalla.txt"));
			// System.setOut(new PrintStream(new FileOutputStream('/dev/stdout')));
			System.setOut(new PrintStream(consoleLogStream));
			System.setErr(consoleLogStream);
			// ConnectDatabaseV1.getTimestamp("B00000028_21I5H1_20140216_170559E.JPG,");
			System.out.println("Default timezone = " + TimeZone.getDefault());
			System.out.println("\ncontinuityThresholdInSeconds=" + continuityThresholdInSeconds
					+ " continuityThresholdInMeters" + continuityThresholdInMeters);

			//// start of curtian1
			// get root of the category hierarchy tree
			DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
					.deSerializeThis(categoryHierarchyTreeFileName);
			//
			TreeMap<Integer, String> catIDNameDictionary = (TreeMap<Integer, String>) Serializer
					.kryoDeSerializeThis(catIDNameDictionaryFileName);
			WToFile.writeMapToNewFile(catIDNameDictionary, "catID,catName", ",",
					commonPath + "catIDNameDictionary.csv");
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/CatIDNameDictionary.kryo");
			// commonPath + "CatIDNameDictionary.kryo");

			// $$int workingCatLevel = DomainConstants.gowallaWorkingCatLevel;

			Pair<TreeMap<Integer, String>, LinkedHashSet<Integer>> catIDWorkingLevelCatIDsDictResult = getWorkingLevelCatIDsForAllCatIDs(
					catIDNameDictionary, workingCatLevel, rootOfCategoryTree);

			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict = catIDWorkingLevelCatIDsDictResult.getFirst();
			WToFile.writeMapToNewFile(catIDWorkingLevelCatIDsDict, "catID,WorkingLevelCatID", ",",
					commonPath + "catIDWorkingLevelCatIDsDict.csv");

			LinkedHashSet<Integer> catIDsInHierarchy = catIDWorkingLevelCatIDsDictResult.getSecond();
			WToFile.writeToNewFile(catIDsInHierarchy.toString(), commonPath + "UniqueCatIDsInHierarchy.csv");

			TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict = getLevelWiseCatIDsForAllCatIDs(catIDNameDictionary,
					rootOfCategoryTree, numOfCatLevels);
			WToFile.writeMapOfArrayValsToNewFile(catIDLevelWiseCatIDsDict, "catID,LevelWiseCatIDs", ",",
					commonPath + "catIDLevelWiseCatIDsDict.csv");

			HashMap<String, Double> mapCatIDsHierDist = null;
			// disabled temporarily createCatIDsHierarchicalDistMap(catIDLevelWiseCatIDsDict,
			// catIDNameDictionary, catIDsInHierarchy);

			///////////////////////// Start of sanity check
			// sanity check to verify if no cat id has empty working lvel cat ids
			System.out.println("Sanity Check: printing all catIDWorkingLevelCatIDsDict with val length > 0");
			catIDWorkingLevelCatIDsDict.entrySet().stream().filter(e -> e.getValue().length() > 0)
					.forEach(e -> System.out.println(e.getKey() + "-" + e.getValue()));

			System.out.println("num of catIDWorkingLevelCatIDsDict with val length > 1="
					+ catIDWorkingLevelCatIDsDict.entrySet().stream().filter(e -> e.getValue().length() > 1).count());
			// .forEach(e -> System.out.println(e.getKey() + "--" + e.getValue()));

			System.out.println("Sanity Check: printing all catIDWorkingLevelCatIDsDict with val length = 0");
			catIDWorkingLevelCatIDsDict.entrySet().stream().filter(e -> e.getValue().length() == 0)
					.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue()));

			System.out.println("Sanity Check: printing all catIDWorkingLevelCatIDsDict with val length > 3");
			catIDWorkingLevelCatIDsDict.entrySet().stream().filter(e -> e.getValue().length() > 3)
					.forEach(e -> System.out.println(e.getKey() + "-" + e.getValue()));

			System.out.println("Sanity Check: printing all catIDLevelWiseCatIDsDict");
			catIDLevelWiseCatIDsDict.entrySet().stream()
					.forEach(e -> System.out.println(e.getKey() + "-" + Arrays.toString(e.getValue())));
			///////////////////////// END of sanity check

			////
			// used in create checkin entries to determine if a cat id is acceptable
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap = UIUtilityBox
					.getCatIDsFoundNodesMap(rootOfCategoryTree, catIDNameDictionary);

			////
			// Disabled on Mar 23 2018
			// Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>> unmergedCheckinResult =
			// createCheckinEntries(
			// checkinDataFileName, commonPath, rootOfCategoryTree, catIDWorkingLevelCatIDsDict,
			// catIDsFoundNodesMap, workingCatLevel, catIDLevelWiseCatIDsDict);

			Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>> unmergedCheckinResult = // createCheckinEntries23Mar2018
					createCheckinEntries3April2018(checkinDataFileName, commonPath, rootOfCategoryTree,
							catIDWorkingLevelCatIDsDict, catIDsFoundNodesMap, workingCatLevel,
							catIDLevelWiseCatIDsDict);

			mapForAllCheckinData = unmergedCheckinResult.getFirst();

			long numOfCheckins = mapForAllCheckinData.entrySet().stream().mapToLong(e -> e.getValue().size()).sum();
			System.out.println("num of checkins = " + numOfCheckins); // 6276222
			// PopUps.showMessage("num of checkins = " + numOfCheckins);

			////// consecutive same analysis
			// countConsecutiveSimilarActivities2(mapForAllCheckinData, commonPath, catIDNameDictionaryFileName);
			// $$Function<CheckinEntryV2, String> consecCompareDirectCatID = ce -> String.valueOf(ce.getActivityID());
			Function<CheckinEntryV2, String> consecCompareLocationID = ce -> String.valueOf(ce.getLocationIDs().get(0));// String.valueOf(ce.getFirstLocationID());
			countConsecutiveSimilarActivities3_3April2018(mapForAllCheckinData, commonPath, catIDNameDictionaryFileName,
					consecCompareLocationID);// consecCompareDirectCatID);

			// System.exit(0);
			/////
			System.out.println("ALERT! merge = " + doMerge);
			if (doMerge)
			{
				if (!disableExpensiveWriting) // skipping writing on Aug 10 for performance.
				{
					WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2(mapForAllCheckinData,
							commonPath + "mapForAllCheckinDataBeforeMerged.csv", catIDNameDictionary);
				} /////
					// merge
					// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinDataMerged
				mapForAllCheckinData = DatageneratorUtils.mergeContinuousGowallaWithoutBOD4_3April2018(
						mapForAllCheckinData, commonPath, continuityThresholdInSeconds, continuityThresholdInMeters);

				if (!disableExpensiveWriting)// skipping writing on Aug 10 for performance.
				{
					WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2(mapForAllCheckinData,
							commonPath + "mapForAllCheckinDataAfterMerged.csv", catIDNameDictionary);
				}
			}
			else
			{
				WToFile.writeLinkedHashMapOfTreemapCheckinEntryV2(mapForAllCheckinData,
						commonPath + "mapForAllCheckinNoMerging.csv", catIDNameDictionary);
			}

			userIDsInCheckinData = mapForAllCheckinData.keySet();
			locationIDsInCheckinData = unmergedCheckinResult.getSecond();
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
			Serializer.kryoSerializeThis(mapCatIDsHierDist, commonPath + "mapCatIDsHierDist.kryo");
			Serializer.kryoSerializeThis(catIDLevelWiseCatIDsDict, commonPath + "mapCatIDLevelWiseCatIDsDict.kryo");
			// catIDsHierDistDict
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
	 * @param catIDLevelWiseCatIDsDict
	 * @param catIDNameDictionary
	 * @param catIDsInHierarchy
	 * @return
	 */
	private static HashMap<String, Double> createCatIDsHierarchicalDistMap(
			TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict, TreeMap<Integer, String> catIDNameDictionary,
			LinkedHashSet<Integer> catIDsInHierarchy)
	{
		HashMap<String, Double> result = new HashMap<>();

		StringBuilder sbCatIDStringCodeLog = new StringBuilder("CatID,CharCode\n");

		for (Integer catID : catIDsInHierarchy)
		{
			sbCatIDStringCodeLog.append(catID).append(",").append(StringCode.getCharCodeFromActivityID(catID))
					.append("\n");
		}
		WToFile.writeToNewFile(sbCatIDStringCodeLog.toString(), Constant.getCommonPath() + "CatIDCharCodeMap.csv");

		for (Integer catID1 : catIDsInHierarchy)
		{
			for (Integer catID2 : catIDsInHierarchy)
			{
				if (catID1.equals(catID2))
				{
					continue;
				}
				else
				{
					double dist = -1;

					System.out.println("Comparing catID1:" + catID1 + " (" + catIDNameDictionary.get(catID1) + ") "
							+ " catID2:" + catID2 + " (" + catIDNameDictionary.get(catID2) + ") ");
					String[] levelWiseCatIDsForCatID1 = catIDLevelWiseCatIDsDict.get(catID1);
					String[] levelWiseCatIDsForCatID2 = catIDLevelWiseCatIDsDict.get(catID2);

					// String[] level1CatIDsForCatID1 = RegexUtils.patternDoubleUnderScore
					// .split(levelWiseCatIDsForCatID1[0]);

					HashSet<String> level1CatIDsForCatID1 = levelWiseCatIDsForCatID1[0] != null
							? new HashSet<String>(Arrays
									.asList(RegexUtils.patternDoubleUnderScore.split(levelWiseCatIDsForCatID1[0])))
							: new HashSet<String>();

					System.out.println("level1CatIDsForCatID1= " + level1CatIDsForCatID1);
					// new HashSet<String>(
					// Arrays.asList(RegexUtils.patternDoubleUnderScore.split(levelWiseCatIDsForCatID1[0])));

					HashSet<String> level1CatIDsForCatID2 = levelWiseCatIDsForCatID2[0] != null
							? new HashSet<String>(Arrays
									.asList(RegexUtils.patternDoubleUnderScore.split(levelWiseCatIDsForCatID2[0])))
							: new HashSet<String>();
					System.out.println("level1CatIDsForCatID2= " + level1CatIDsForCatID2);

					// HashSet<String> level2CatIDsForCatID1 = new HashSet<String>(
					// Arrays.asList(RegexUtils.patternDoubleUnderScore.split(levelWiseCatIDsForCatID1[1])));
					//
					// HashSet<String> level2CatIDsForCatID2 = new HashSet<String>(
					// Arrays.asList(RegexUtils.patternDoubleUnderScore.split(levelWiseCatIDsForCatID2[1])));

					if (UtilityBelt.hasCommonElement(level1CatIDsForCatID1, level1CatIDsForCatID2))
					{
						System.out.println("\tsame level 1 parent");
						dist = 0.4;
					}
					else if // (UtilityBelt.hasCommonElement(level1CatIDsForCatID2, Integer.to(catID1))
					(level1CatIDsForCatID2.contains(Integer.toString(catID1)))
					{
						System.out.println("\tcatID1 is in level 1 of catID 2");
						dist = 0.4;
					}
					else if // (UtilityBelt.hasCommonElement(level1CatIDsForCatID1, catID2))
					(level1CatIDsForCatID1.contains(Integer.toString(catID2)))
					{
						System.out.println("\tcatID2 is in level 1 of catID 1");
						dist = 0.4;
					}
					else
					{
						dist = 1;
					}
					System.out.println("dist = " + dist + "\n");
					// result.put(Integer.toString(catID1) + "-" + Integer.toString(catID2), dist);
					result.put(String.valueOf(StringCode.getCharCodeFromActivityID(catID1))
							+ String.valueOf(StringCode.getCharCodeFromActivityID(catID2)), dist);
				}
			}
		}

		StringBuilder sbResult = new StringBuilder("CatIDs,Dist\n");

		for (Entry<String, Double> e : result.entrySet())
		{
			sbResult.append(e.getKey()).append(',').append(e.getValue()).append("\n");
		}

		WToFile.writeToNewFile(sbResult.toString(), Constant.getCommonPath() + "CatIDDistDict.csv");
		return result;
	}

	/**
	 * Fork of org.activity.generator.DatabaseCreatorGowallaQuicker0.countConsecutiveSimilarActivities3().
	 * 
	 * @param mapForAllCheckinData
	 * @param commonPathToWrite
	 * @param absPathToCatIDDictionary
	 * @return
	 */
	private static LinkedHashMap<String, ArrayList<Integer>> countConsecutiveSimilarActivities3_28Mar2018(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinData, String commonPathToWrite,
			String absPathToCatIDDictionary, Function<CheckinEntry, String> lambdaForConsecSameAttribute)
	{
		// LinkedHashMap<String, ArrayList<Long>> catIDTimeDifferencesOfConsecutives = new LinkedHashMap<>();
		Pair<LinkedHashMap<String, ArrayList<Integer>>, TreeMap<Integer, String>> r1 = TimelineUtils
				.getEmptyMapOfCatIDs(absPathToCatIDDictionary);

		// <catid,catname>
		TreeMap<Integer, String> catIDNameDictionary = r1.getSecond();

		// <catid, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> catIDLengthConsecs = r1.getFirst();
		System.out.println("catIDLengthConsecutives.size = " + catIDLengthConsecs.size());

		// <placeid, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> comparedAttribLengthConsecs = new LinkedHashMap<>();

		// <userID, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> userLengthConsecs = new LinkedHashMap<>();

		// write all checkins sequentially userwise
		StringBuilder sbEnumerateAllCheckins = new StringBuilder(
				"user,currTSString,currValOfComparisonAttribute,activityID,actCatName,distNext,durationNext,currLat,currLon,currLocID,distPrev,durPrev\n");
		StringBuilder sbAllDistanceInMDurationInSec = new StringBuilder();
		// changed to write dist and duration diff in same lin so in R analysis i can filter by both at the same time.
		// StringBuilder sbAllDurationFromNext = new StringBuilder();
		WToFile.appendLineToFileAbs(
				"User,Timestamp,CatID,CatName,DistFromNext,DurFromNext,DistFromPrev,DurFromPrev,prevPlaceIDSame\n",
				commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv"); // writing header

		long checkinsCount = 0, checkinsWithInvalidGeocoords = 0;
		// /* Uncomment to view the category ids in the map */
		// catIDLengthConsecs.entrySet().stream().forEach(e -> System.out.print(" " + e.getKey().toString() + "-" +
		// e.getValue()));

		try
		{
			for (Entry<String, TreeMap<Timestamp, CheckinEntry>> userE : mapForAllCheckinData.entrySet())
			{
				String user = userE.getKey();

				// can initiate here, since entries for each user is together, can't do same for cat and compared attrib
				ArrayList<Integer> userLengthConsecsVals = new ArrayList<Integer>();

				String prevValOfComparisonAttribute = "", prevActivityID = "";// activityID or placeID
				Timestamp prevTS = null;
				String prevLat = "";
				String prevLon = "";// added on 23 Mar 2018
				int prevLocID = Integer.MIN_VALUE;
				int numOfConsecutives = 1;

				StringBuilder distanceDurationFromNextSeq = new StringBuilder(); // only writes >1 consecs

				for (Entry<Timestamp, CheckinEntry> dateE : userE.getValue().entrySet())
				{
					CheckinEntry ce = dateE.getValue();
					checkinsCount += 1;

					if (!StatsUtils.isValidGeoCoordinate(ce.getStartLatitude(), ce.getStartLongitude()))
					{
						checkinsWithInvalidGeocoords += 1;
					}

					String currValOfComparisonAttribute = lambdaForConsecSameAttribute.apply(ce);
					String activityID = String.valueOf(ce.getActivityID());
					int currLocID = ce.getLocationIDs().get(0);
					double distNext = ce.getDistanceInMetersFromPrev();// .getDistInMetersFromNext();
					long durationNext = ce.getDurationInSecsFromPrev();

					Timestamp currTS = ce.getTimestamp();
					String currTSString = currTS.toString();
					String actCatName = catIDNameDictionary.get(Integer.valueOf(activityID));
					String currLat = ce.getStartLatitude();
					String currLon = ce.getStartLongitude();

					// double distPrev = ce.getDistInMetersFromPrev();
					// long durPrev = ce.getDurInSecsFromPrev();

					sbEnumerateAllCheckins.append(user + "," + currTSString + "," + currValOfComparisonAttribute + ","
							+ activityID + "," + actCatName + "," + distNext + "," + durationNext + "," + currLat + ","
							+ currLon + "," + currLocID /* + "," + distPrev + "," + durPrev */ + "\n");

					// if curr is same as prev for compared attrib,
					// keep on accumulating the consecutives & append entry for writing to file
					if (currValOfComparisonAttribute.equals(prevValOfComparisonAttribute))
					{
						// $$ System.out.println(" act name:" + activityName + " = prevActName = " + prevActivityName
						// $$ + " \n Hence append");
						numOfConsecutives += 1;

						long durationFromPrevInSecs = (currTS.getTime() - prevTS.getTime()) / 1000;
						double distFromPrevInM = 1000
								* SpatialUtils.haversineFastMathV2(prevLat, prevLon, currLat, currLon);

						// Sanity check start
						String saNityString = "";
						// saNityString += ((distPrev - distFromPrevInM) > 0.000005 ? ",F" : ",T");
						// saNityString += ((durPrev != durationFromPrevInSecs) ? ",F" : ",T");
						//
						// if (saNityString.contains("F"))
						// {
						// System.out.println("Alert:\ndistPrev=" + distPrev + "distFromPrevInM=" + distFromPrevInM);
						// System.out.println("Alert:\ndurPrev=" + durPrev + "distFromPrevInM=" + distFromPrevInM);
						//
						// }

						// Sanity check end
						int prevLocIDEqualsCurrLocID = (prevLocID == currLocID) ? 1 : 0;
						distanceDurationFromNextSeq.append(user + "," + currTSString + ","
								+ currValOfComparisonAttribute + "," + activityID + "," + actCatName + ","
								+ String.valueOf(distNext) + "," + String.valueOf(durationNext) + "," + distFromPrevInM
								+ "," + durationFromPrevInSecs + "," + prevLocIDEqualsCurrLocID + saNityString + "\n");

						// prevValOfComparisonAttribute = currValOfComparisonAttribute; //
						prevActivityID = activityID; // not for comparison but for consecValsCat
						prevLocID = currLocID;
						prevLat = currLat;
						prevLon = currLon;
						prevTS = currTS;

						continue;
					}
					// if current val is not equal to prev value, write the prev accumulated consecutives
					else
					{
						if (prevValOfComparisonAttribute.length() == 0)
						{
							// skip the first entry for this user.
						}
						else
						{
							// $$System.out.println(" act name:" + activityName + " != prevActName = " +
							// prevActivityName);
							// consec vals for this cat id. note: preassigned empty arraylist for each catid beforehand
							ArrayList<Integer> consecValsCat = catIDLengthConsecs.get(prevActivityID);

							// consec vals for this compared attibute (say place id)
							// ArrayList<Integer> consecValsCompAttrib;
							// if (comparedAttribLengthConsecs.containsKey(prevValOfComparisonAttribute))
							// {
							// consecValsCompAttrib = comparedAttribLengthConsecs.get(prevValOfComparisonAttribute);
							// }
							//
							// else
							// {
							// consecValsCompAttrib = new ArrayList<>();
							// }
							ArrayList<Integer> consecValsCompAttrib = comparedAttribLengthConsecs
									.get(prevValOfComparisonAttribute);

							if (consecValsCompAttrib == null)
							{
								consecValsCompAttrib = new ArrayList<>();
							}

							// $$System.out.println(" currently numOfConsecutives= " + numOfConsecutives);
							consecValsCat.add(numOfConsecutives); // append this consec value
							consecValsCompAttrib.add(numOfConsecutives); // append this consec value
							userLengthConsecsVals.add(numOfConsecutives); // append this consec value

							catIDLengthConsecs.put(prevActivityID, consecValsCat);
							comparedAttribLengthConsecs.put(prevValOfComparisonAttribute, consecValsCompAttrib);

							if (numOfConsecutives > 1)
							{
								sbAllDistanceInMDurationInSec.append(distanceDurationFromNextSeq.toString());
								// $$System.out.println("appending to dista, duration");
							}
							distanceDurationFromNextSeq.setLength(0); // resetting
							numOfConsecutives = 1;// resetting
						}
					}
					prevValOfComparisonAttribute = currValOfComparisonAttribute;
					prevActivityID = activityID; // not for comparison but for consecValsCat
					prevLocID = currLocID;
					prevLat = currLat;
					prevLon = currLon;
					prevTS = currTS;

					if (checkinsCount % 20000 == 0)
					{
						WToFile.appendLineToFileAbs(sbEnumerateAllCheckins.toString(),
								commonPathToWrite + "ActualOccurrenceOfCheckinsSeq.csv");
						sbEnumerateAllCheckins.setLength(0);

						WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
								commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
						sbAllDistanceInMDurationInSec.setLength(0);
					}
				} // end of loop over days
				userLengthConsecs.put(user, userLengthConsecsVals);
			} // end of loop over users

			// write remaining in buffer
			if (sbEnumerateAllCheckins.length() != 0)
			{
				WToFile.appendLineToFileAbs(sbEnumerateAllCheckins.toString(),
						commonPathToWrite + "ActualOccurrenceOfCheckinsSeq.csv");
				sbEnumerateAllCheckins.setLength(0);

				WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
						commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
				sbAllDistanceInMDurationInSec.setLength(0);
			}

			System.out.println("Num of checkins read = " + checkinsCount);
			System.out.println("checkinsWithInvalidGeocoords read = " + checkinsWithInvalidGeocoords);

			WToFile.writeConsectiveCountsEqualLength(catIDLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "CatwiseConsecCountsEqualLength.csv", true, true);
			WToFile.writeConsectiveCountsEqualLength(comparedAttribLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "ComparedAtributewiseConsecCounts.csv", false, false);
			WToFile.writeConsectiveCountsEqualLength(userLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "UserwiseConsecCounts.csv", false, false);

			// WritingToFile.appendLineToFileAbsolute(sbEnumerateAllCats.toString(),
			// commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return catIDLengthConsecs;
	}

	/**
	 * Fork of org.activity.generator.DatabaseCreatorGowallaQuicker0.countConsecutiveSimilarActivities3_28Mar2018().
	 * 
	 * @param mapForAllCheckinData
	 * @param commonPathToWrite
	 * @param absPathToCatIDDictionary
	 * @return
	 */
	private static LinkedHashMap<String, ArrayList<Integer>> countConsecutiveSimilarActivities3_3April2018(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> mapForAllCheckinData, String commonPathToWrite,
			String absPathToCatIDDictionary, Function<CheckinEntryV2, String> lambdaForConsecSameAttribute)
	{
		// LinkedHashMap<String, ArrayList<Long>> catIDTimeDifferencesOfConsecutives = new LinkedHashMap<>();
		Pair<LinkedHashMap<String, ArrayList<Integer>>, TreeMap<Integer, String>> r1 = TimelineUtils
				.getEmptyMapOfCatIDs(absPathToCatIDDictionary);

		// <catid,catname>
		TreeMap<Integer, String> catIDNameDictionary = r1.getSecond();

		// <catid, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> catIDLengthConsecs = r1.getFirst();
		System.out.println("catIDLengthConsecutives.size = " + catIDLengthConsecs.size());

		// <placeid, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> comparedAttribLengthConsecs = new LinkedHashMap<>();

		// <userID, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> userLengthConsecs = new LinkedHashMap<>();

		// write all checkins sequentially userwise
		StringBuilder sbEnumerateAllCheckins = new StringBuilder(
				"user,currTSString,activityID,actCatName,distFromPrevCheckin,durationFromPrevCheckin,currLat,currLon,currLocID,tz,workingLevelCatID\n");
		StringBuilder sbAllDistanceInMDurationInSec = new StringBuilder();
		// changed to write dist and duration diff in same lin so in R analysis i can filter by both at the same time.
		// StringBuilder sbAllDurationFromNext = new StringBuilder();
		WToFile.appendLineToFileAbs(
				"User,Timestamp,CatID,CatName,DistFromNext,DurFromNext,DistFromPrev,DurFromPrev,prevPlaceIDSame\n",
				commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv"); // writing header

		long checkinsCount = 0, checkinsWithInvalidGeocoords = 0;
		// /* Uncomment to view the category ids in the map */
		// catIDLengthConsecs.entrySet().stream().forEach(e -> System.out.print(" " + e.getKey().toString() + "-" +
		// e.getValue()));

		try
		{
			for (Entry<String, TreeMap<Timestamp, CheckinEntryV2>> userE : mapForAllCheckinData.entrySet())
			{
				String user = userE.getKey();

				// can initiate here, since entries for each user is together, can't do same for cat and compared attrib
				ArrayList<Integer> userLengthConsecsVals = new ArrayList<Integer>();

				String prevValOfComparisonAttribute = "", prevActivityID = "";// activityID or placeID
				Timestamp prevTS = null;
				String prevLat = "";
				String prevLon = "";// added on 23 Mar 2018
				int prevLocID = Integer.MIN_VALUE;
				int numOfConsecutives = 1;

				StringBuilder distanceDurationFromNextSeq = new StringBuilder(); // only writes >1 consecs

				for (Entry<Timestamp, CheckinEntryV2> timeEntry : userE.getValue().entrySet())
				{
					CheckinEntryV2 ce = timeEntry.getValue();
					checkinsCount += 1;

					if (!StatsUtils.isValidGeoCoordinate(ce.getStartLatitude(), ce.getStartLongitude()))
					{
						checkinsWithInvalidGeocoords += 1;
					}

					String currValOfComparisonAttribute = lambdaForConsecSameAttribute.apply(ce);
					String activityID = String.valueOf(ce.getActivityID());
					int currLocID = ce.getLocationIDs().get(0);
					double distFromPrevCheckin = ce.getDistanceInMetersFromPrev();
					long durationFromPrevCheckin = ce.getDurationInSecsFromPrev();

					Timestamp currTS = ce.getTimestamp();
					String currTSString = currTS.toString();
					String actCatName = catIDNameDictionary.get(Integer.valueOf(activityID));
					String currLat = ce.getStartLatitude();
					String currLon = ce.getStartLongitude();

					// double distPrev = ce.getDistInMetersFromPrev();
					// long durPrev = ce.getDurInSecsFromPrev();

					sbEnumerateAllCheckins.append(user
							+ "," + currTSString + "," + activityID + "," + actCatName + "," + distFromPrevCheckin + ","
							+ durationFromPrevCheckin + "," + currLat + "," + currLon + "," + currLocID + ","
							+ ce.getTz() + "," + ce.getWorkingLevelCatIDs() /* + "," + distPrev + "," + durPrev */
							+ "\n");

					// if curr is same as prev for compared attrib,
					// keep on accumulating the consecutives & append entry for writing to file
					if (currValOfComparisonAttribute.equals(prevValOfComparisonAttribute))
					{
						// $$ System.out.println(" act name:" + activityName + " = prevActName = " + prevActivityName
						// $$ + " \n Hence append");
						numOfConsecutives += 1;

						long durationFromPrevInSecsComputedAgain = (currTS.getTime() - prevTS.getTime()) / 1000;
						double distFromPrevInMsComputedAgain = 1000
								* SpatialUtils.haversineFastMathV2(prevLat, prevLon, currLat, currLon);

						// Sanity check start
						String saNityString = "";
						// saNityString += ((distPrev - distFromPrevInM) > 0.000005 ? ",F" : ",T");
						// saNityString += ((durPrev != durationFromPrevInSecs) ? ",F" : ",T");
						//
						// if (saNityString.contains("F"))
						// {
						// System.out.println("Alert:\ndistPrev=" + distPrev + "distFromPrevInM=" + distFromPrevInM);
						// System.out.println("Alert:\ndurPrev=" + durPrev + "distFromPrevInM=" + distFromPrevInM);
						//
						// }

						// Sanity check end
						int prevLocIDEqualsCurrLocID = (prevLocID == currLocID) ? 1 : 0;
						distanceDurationFromNextSeq.append(user + "," + currTSString + ","
								+ currValOfComparisonAttribute + "," + activityID + "," + actCatName + ","
								+ String.valueOf(distFromPrevCheckin) + "," + String.valueOf(durationFromPrevCheckin)
								+ "," + distFromPrevInMsComputedAgain + "," + durationFromPrevInSecsComputedAgain + ","
								+ prevLocIDEqualsCurrLocID + "\n");

						// prevValOfComparisonAttribute = currValOfComparisonAttribute; //
						prevActivityID = activityID; // not for comparison but for consecValsCat
						prevLocID = currLocID;
						prevLat = currLat;
						prevLon = currLon;
						prevTS = currTS;

						continue;
					}
					// if current val is not equal to prev value, write the prev accumulated consecutives
					else
					{
						if (prevValOfComparisonAttribute.length() == 0)
						{
							// skip the first entry for this user.
						}
						else
						{
							// $$System.out.println(" act name:" + activityName + " != prevActName = " +
							// prevActivityName);
							// consec vals for this cat id. note: preassigned empty arraylist for each catid beforehand
							ArrayList<Integer> consecValsCat = catIDLengthConsecs.get(prevActivityID);

							// consec vals for this compared attibute (say place id)
							// ArrayList<Integer> consecValsCompAttrib;
							// if (comparedAttribLengthConsecs.containsKey(prevValOfComparisonAttribute))
							// {
							// consecValsCompAttrib = comparedAttribLengthConsecs.get(prevValOfComparisonAttribute);
							// }
							//
							// else
							// {
							// consecValsCompAttrib = new ArrayList<>();
							// }
							ArrayList<Integer> consecValsCompAttrib = comparedAttribLengthConsecs
									.get(prevValOfComparisonAttribute);

							if (consecValsCompAttrib == null)
							{
								consecValsCompAttrib = new ArrayList<>();
							}

							// $$System.out.println(" currently numOfConsecutives= " + numOfConsecutives);
							consecValsCat.add(numOfConsecutives); // append this consec value
							consecValsCompAttrib.add(numOfConsecutives); // append this consec value
							userLengthConsecsVals.add(numOfConsecutives); // append this consec value

							catIDLengthConsecs.put(prevActivityID, consecValsCat);
							comparedAttribLengthConsecs.put(prevValOfComparisonAttribute, consecValsCompAttrib);

							if (numOfConsecutives > 1)
							{
								sbAllDistanceInMDurationInSec.append(distanceDurationFromNextSeq.toString());
								// $$System.out.println("appending to dista, duration");
							}
							distanceDurationFromNextSeq.setLength(0); // resetting
							numOfConsecutives = 1;// resetting
						}
					}
					prevValOfComparisonAttribute = currValOfComparisonAttribute;
					prevActivityID = activityID; // not for comparison but for consecValsCat
					prevLocID = currLocID;
					prevLat = currLat;
					prevLon = currLon;
					prevTS = currTS;

					if (checkinsCount % 20000 == 0)
					{
						WToFile.appendLineToFileAbs(sbEnumerateAllCheckins.toString(),
								commonPathToWrite + "ActualOccurrenceOfCheckinsSeq.csv");
						sbEnumerateAllCheckins.setLength(0);

						WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
								commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
						sbAllDistanceInMDurationInSec.setLength(0);
					}
				} // end of loop over days
				userLengthConsecs.put(user, userLengthConsecsVals);
			} // end of loop over users

			// write remaining in buffer
			if (sbEnumerateAllCheckins.length() != 0)
			{
				WToFile.appendLineToFileAbs(sbEnumerateAllCheckins.toString(),
						commonPathToWrite + "ActualOccurrenceOfCheckinsSeq.csv");
				sbEnumerateAllCheckins.setLength(0);

				WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
						commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
				sbAllDistanceInMDurationInSec.setLength(0);
			}

			System.out.println("Num of checkins read = " + checkinsCount);
			System.out.println("checkinsWithInvalidGeocoords read = " + checkinsWithInvalidGeocoords);

			WToFile.writeConsectiveCountsEqualLength(catIDLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "CatwiseConsecCountsEqualLength.csv", true, true);
			WToFile.writeConsectiveCountsEqualLength(comparedAttribLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "ComparedAtributewiseConsecCounts.csv", false, false);
			WToFile.writeConsectiveCountsEqualLength(userLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "UserwiseConsecCounts.csv", false, false);

			// WritingToFile.appendLineToFileAbsolute(sbEnumerateAllCats.toString(),
			// commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return catIDLengthConsecs;
	}

	////////

	/**
	 * Fork of
	 * org.activity.generator.DatabaseCreatorGowallaQuicker0.countConsecutiveSimilarActivities2(LinkedHashMap<String,
	 * TreeMap<Timestamp, CheckinEntry>>, String, String). Adds control for choosing which attribute to use for
	 * considering consecutives to be considered same
	 * 
	 * @param mapForAllCheckinData
	 * @param commonPathToWrite
	 * @param absPathToCatIDDictionary
	 * @return
	 */
	private static LinkedHashMap<String, ArrayList<Integer>> countConsecutiveSimilarActivities3(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinData, String commonPathToWrite,
			String absPathToCatIDDictionary, Function<CheckinEntry, String> lambdaForConsecSameAttribute)
	{
		// LinkedHashMap<String, ArrayList<Long>> catIDTimeDifferencesOfConsecutives = new LinkedHashMap<>();
		Pair<LinkedHashMap<String, ArrayList<Integer>>, TreeMap<Integer, String>> r1 = TimelineUtils
				.getEmptyMapOfCatIDs(absPathToCatIDDictionary);

		// <catid,catname>
		TreeMap<Integer, String> catIDNameDictionary = r1.getSecond();

		// <catid, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> catIDLengthConsecs = r1.getFirst();
		System.out.println("catIDLengthConsecutives.size = " + catIDLengthConsecs.size());

		// <placeid, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> comparedAttribLengthConsecs = new LinkedHashMap<>();

		// <userID, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> userLengthConsecs = new LinkedHashMap<>();

		StringBuilder sbEnumerateAllCheckins = new StringBuilder();// write all checkins sequentially userwise
		StringBuilder sbAllDistanceInMDurationInSec = new StringBuilder();
		// changed to write dist and duration diff in same lin so in R analysis i can filter by both at the same time.
		// StringBuilder sbAllDurationFromNext = new StringBuilder();
		WToFile.appendLineToFileAbs("User,Timestamp,CatID,CatName,DistDiff,DurationDiff\n",
				commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv"); // writing header

		long checkinsCount = 0, checkinsWithInvalidGeocoords = 0;
		// /* Uncomment to view the category ids in the map */
		// catIDLengthConsecs.entrySet().stream().forEach(e -> System.out.print(" " + e.getKey().toString() + "-" +
		// e.getValue()));

		try
		{
			for (Entry<String, TreeMap<Timestamp, CheckinEntry>> userE : mapForAllCheckinData.entrySet())
			{
				String user = userE.getKey();

				// can initiate here, since entries for each user is together, can't do same for cat and compared attrib
				ArrayList<Integer> userLengthConsecsVals = new ArrayList<Integer>();

				String prevValOfComparisonAttribute = "", prevActivityID = "";// activityID or placeID

				int numOfConsecutives = 1;

				StringBuilder distanceDurationFromNextSeq = new StringBuilder(); // only writes >1 consecs

				for (Entry<Timestamp, CheckinEntry> dateE : userE.getValue().entrySet())
				{
					CheckinEntry ce = dateE.getValue();
					checkinsCount += 1;

					if (!StatsUtils.isValidGeoCoordinate(ce.getStartLatitude(), ce.getStartLongitude()))
					{
						checkinsWithInvalidGeocoords += 1;
					}

					String currValOfComparisonAttribute = lambdaForConsecSameAttribute.apply(ce);
					String activityID = String.valueOf(ce.getActivityID());
					double distNext = ce.getDistanceInMetersFromPrev();
					long durationNext = ce.getDurationInSecsFromPrev();
					String ts = ce.getTimestamp().toString();
					String actCatName = catIDNameDictionary.get(Integer.valueOf(activityID));

					sbEnumerateAllCheckins.append(user + "," + ts + "," + currValOfComparisonAttribute + ","
							+ activityID + "," + actCatName + "," + distNext + "," + durationNext + "\n");

					// if curr is same as prev for compared attrib,
					// keep on accumulating the consecutives & append entry for writing to file
					if (currValOfComparisonAttribute.equals(prevValOfComparisonAttribute))
					{
						// $$ System.out.println(" act name:" + activityName + " = prevActName = " + prevActivityName
						// $$ + " \n Hence append");
						numOfConsecutives += 1;
						distanceDurationFromNextSeq.append(user + "," + ts + "," + currValOfComparisonAttribute + ","
								+ activityID + "," + actCatName + "," + String.valueOf(distNext) + ","
								+ String.valueOf(durationNext) + "\n");
						continue;
					}
					// if current val is not equal to prev value, write the prev accumulated consecutives
					else
					{
						if (prevValOfComparisonAttribute.length() == 0)
						{
							// skip the first entry for this user.
						}
						else
						{
							// $$System.out.println(" act name:" + activityName + " != prevActName = " +
							// prevActivityName);
							// consec vals for this cat id. note: preassigned empty arraylist for each catid beforehand
							ArrayList<Integer> consecValsCat = catIDLengthConsecs.get(prevActivityID);

							// consec vals for this compared attibute (say place id)
							// ArrayList<Integer> consecValsCompAttrib;
							// if (comparedAttribLengthConsecs.containsKey(prevValOfComparisonAttribute))
							// {
							// consecValsCompAttrib = comparedAttribLengthConsecs.get(prevValOfComparisonAttribute);
							// }
							//
							// else
							// {
							// consecValsCompAttrib = new ArrayList<>();
							// }
							ArrayList<Integer> consecValsCompAttrib = comparedAttribLengthConsecs
									.get(prevValOfComparisonAttribute);

							if (consecValsCompAttrib == null)
							{
								consecValsCompAttrib = new ArrayList<>();
							}

							// $$System.out.println(" currently numOfConsecutives= " + numOfConsecutives);
							consecValsCat.add(numOfConsecutives); // append this consec value
							consecValsCompAttrib.add(numOfConsecutives); // append this consec value
							userLengthConsecsVals.add(numOfConsecutives); // append this consec value

							catIDLengthConsecs.put(prevActivityID, consecValsCat);
							comparedAttribLengthConsecs.put(prevValOfComparisonAttribute, consecValsCompAttrib);

							if (numOfConsecutives > 1)
							{
								sbAllDistanceInMDurationInSec.append(distanceDurationFromNextSeq.toString());
								// $$System.out.println("appending to dista, duration");
							}
							distanceDurationFromNextSeq.setLength(0); // resetting
							numOfConsecutives = 1;// resetting
						}
					}
					prevValOfComparisonAttribute = currValOfComparisonAttribute;
					prevActivityID = activityID; // not for comparison but for consecValsCat

					if (checkinsCount % 20000 == 0)
					{
						WToFile.appendLineToFileAbs(sbEnumerateAllCheckins.toString(),
								commonPathToWrite + "ActualOccurrenceOfCheckinsSeq.csv");
						sbEnumerateAllCheckins.setLength(0);

						WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
								commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
						sbAllDistanceInMDurationInSec.setLength(0);
					}
				} // end of loop over days
				userLengthConsecs.put(user, userLengthConsecsVals);
			} // end of loop over users

			// write remaining in buffer
			if (sbEnumerateAllCheckins.length() != 0)
			{
				WToFile.appendLineToFileAbs(sbEnumerateAllCheckins.toString(),
						commonPathToWrite + "ActualOccurrenceOfCheckinsSeq.csv");
				sbEnumerateAllCheckins.setLength(0);

				WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
						commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
				sbAllDistanceInMDurationInSec.setLength(0);
			}

			System.out.println("Num of checkins read = " + checkinsCount);
			System.out.println("checkinsWithInvalidGeocoords read = " + checkinsWithInvalidGeocoords);

			WToFile.writeConsectiveCountsEqualLength(catIDLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "CatwiseConsecCountsEqualLength.csv", true, true);
			WToFile.writeConsectiveCountsEqualLength(comparedAttribLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "ComparedAtributewiseConsecCounts.csv", false, false);
			WToFile.writeConsectiveCountsEqualLength(userLengthConsecs, catIDNameDictionary,
					commonPathToWrite + "UserwiseConsecCounts.csv", false, false);

			// WritingToFile.appendLineToFileAbsolute(sbEnumerateAllCats.toString(),
			// commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return catIDLengthConsecs;
	}

	////////

	/**
	 * Similar to org.activity.util.TimelineUtils.countConsecutiveSimilarActivities2() but modified to work with
	 * checkins instead of timelines
	 * 
	 * @param mapForAllCheckinData
	 * @param commonPathToWrite
	 * @param absPathToCatIDDictionary
	 * @return
	 */
	private static LinkedHashMap<String, ArrayList<Integer>> countConsecutiveSimilarActivities2(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinData, String commonPathToWrite,
			String absPathToCatIDDictionary)
	{
		// LinkedHashMap<String, ArrayList<Long>> catIDTimeDifferencesOfConsecutives = new LinkedHashMap<>();
		Pair<LinkedHashMap<String, ArrayList<Integer>>, TreeMap<Integer, String>> r1 = TimelineUtils
				.getEmptyMapOfCatIDs(absPathToCatIDDictionary);

		// <catid, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> catIDLengthConsecutives = r1.getFirst();

		System.out.println("catIDLengthConsecutives.size = " + catIDLengthConsecutives.size());

		// /* Uncomment to view the category ids in the map */
		// catIDLengthConsecutives.entrySet().stream()
		// .forEach(e -> System.out.print(" " + e.getKey().toString() + "-" + e.getValue()));

		// <catid,catname>
		TreeMap<Integer, String> catIDNameDictionary = r1.getSecond();

		// <userIDt, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> userLengthConsecutives = new LinkedHashMap<>();

		StringBuilder sbAllDistanceInMDurationInSec = new StringBuilder();
		// changed to write dist and duration diff in same lin so in R analysis i can filter by both at the same time.
		// StringBuilder sbAllDurationFromNext = new StringBuilder();
		WToFile.appendLineToFileAbs("User,Timestamp,CatID,CatName,DistDiff,DurationDiff\n",
				commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv"); // writing header

		StringBuilder sbEnumerateAllCats = new StringBuilder();// write all catid sequentially userwise

		long checkinsCount = 0;
		try
		{
			for (Entry<String, TreeMap<Timestamp, CheckinEntry>> userE : mapForAllCheckinData.entrySet())
			{
				String user = userE.getKey();

				ArrayList<Integer> userLengthConsecutivesValues = new ArrayList<Integer>();

				String prevActivityID = "";// Timestamp prevActivityStartTimestamp = null;

				int numOfConsecutives = 1;// long timeDiff = 0;

				StringBuilder distanceDurationFromNextSeq = new StringBuilder(); // only write >1 consecs
				// StringBuilder durationFromNextSeq = new StringBuilder();// only write >1 consecs

				for (Entry<Timestamp, CheckinEntry> dateE : userE.getValue().entrySet())
				{
					// for (ActivityObject aos : dateE.getValue().getActivityObjectsInDay())
					// {
					checkinsCount += 1;
					CheckinEntry ce = dateE.getValue();
					String activityID = String.valueOf(ce.getActivityID());// aos.getActivityName();
					double distNext = ce.getDistanceInMetersFromPrev();// getDistInMetersFromNext();
					long durationNext = ce.getDurationInSecsFromPrev();// getDurInSecsFromNext();
					String ts = ce.getTimestamp().toString();
					String actCatName = catIDNameDictionary.get(Integer.valueOf(activityID));

					// System.out.println("aoCount=" + aoCount + " activityName=" + activityName);

					sbEnumerateAllCats.append(user + "," + ts + "," + activityID + "," + actCatName + "\n");
					// $$System.out.println("\nReading: " + user + "," + ts + "," + activityName + "," +
					// actCatName);

					if (activityID.equals(prevActivityID))
					{
						// $$ System.out.println(" act name:" + activityName + " = prevActName = " +
						// prevActivityName
						// $$ + " \n Hence append");
						numOfConsecutives += 1;
						distanceDurationFromNextSeq.append(user + "," + ts + "," + activityID + "," + actCatName + ","
								+ String.valueOf(distNext) + "," + String.valueOf(durationNext) + "\n");
						// durationFromNextSeq.append(user + "," + ts + "," + activityName + "," + actCatName + ","
						// + String.valueOf(durationNext) + "\n");
						// timeDiff += aos.getStartTimestamp().getTime() - prevActivityStartTimestamp.getTime();
						// System.out.println(" Current Prev act Same, numOfConsecutives =" + numOfConsecutives);
						continue;
					}

					else // not equals then
					{
						// $$System.out.println(" act name:" + activityName + " != prevActName = " +
						// prevActivityName);
						ArrayList<Integer> consecVals = catIDLengthConsecutives.get(prevActivityID);
						if (consecVals == null)
						{
							if (prevActivityID.length() > 0)
							{
								System.out.println(
										"Error in org.activity.generator.DatabaseCreatorGowallaQuicker0.countConsecutiveSimilarActivities2(): consecVals = null, i,e., array list for activityName="
												+ prevActivityID
												+ " hasn't been initialised in catIDLengthConsecutives");
							}
							else
							{
								// encountered the first activity for that user.
								// System.out.println(" first activity for this user.");
							}
						}
						else
						{
							// $$System.out.println(" currently numOfConsecutives= " + numOfConsecutives);
							consecVals.add(numOfConsecutives);
							catIDLengthConsecutives.put(prevActivityID, consecVals);
							userLengthConsecutivesValues.add(numOfConsecutives);

							if (numOfConsecutives > 1)
							{
								sbAllDistanceInMDurationInSec.append(distanceDurationFromNextSeq.toString());
								// sbAllDurationFromNext.append(durationFromNextSeq.toString());// + "\n");
								// $$System.out.println("appending to dista, duration");
							}
							// else
							// {
							distanceDurationFromNextSeq.setLength(0);
							// durationFromNextSeq.setLength(0);
							// }

							// System.out.println(" Current Prev act diff, numOfConsecutives =" +
							// numOfConsecutives);
							// System.out.println(" (prev) activity name=" + prevActivityName + " consecVals="
							// + catIDLengthConsecutives.get(prevActivityName).toString());
							numOfConsecutives = 1;// resetting
						}

					}
					prevActivityID = activityID;

					if (checkinsCount % 20000 == 0)
					{
						WToFile.appendLineToFileAbs(sbEnumerateAllCats.toString(),
								commonPathToWrite + "ActualOccurrenceOfCheckinsSeq.csv");
						sbEnumerateAllCats.setLength(0);

						/////////////////
						WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
								commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
						sbAllDistanceInMDurationInSec.setLength(0);

						// WritingToFile.appendLineToFileAbsolute(sbAllDurationFromNext.toString(),
						// commonPathToWrite + "sbAllDurationFromNext.csv");
						// sbAllDurationFromNext.setLength(0);
						/////////////////

					}
					// } // end of loop over aos over this day for this user
					// break;
				} // end of loop over days
					// break;
				userLengthConsecutives.put(user, userLengthConsecutivesValues);
			} // end of loop over users

			// write remaining in buffer
			if (sbEnumerateAllCats.length() != 0)
			{
				WToFile.appendLineToFileAbs(sbEnumerateAllCats.toString(),
						commonPathToWrite + "ActualOccurrenceOfCheckinsSeq.csv");
				sbEnumerateAllCats.setLength(0);

				/////////////////
				WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
						commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
				sbAllDistanceInMDurationInSec.setLength(0);

				// WritingToFile.appendLineToFileAbsolute(sbAllDurationFromNext.toString(),
				// commonPathToWrite + "sbAllDurationFromNext.csv");
				// sbAllDurationFromNext.setLength(0);
				/////////////////

			}

			System.out.println("Num of aos read = " + checkinsCount);
			WToFile.writeConsectiveCountsEqualLength(catIDLengthConsecutives, catIDNameDictionary,
					commonPathToWrite + "CatwiseConsecCountsEqualLength.csv", true, true);
			WToFile.writeConsectiveCountsEqualLength(userLengthConsecutives, catIDNameDictionary,
					commonPathToWrite + "UserwiseConsecCountsEqualLength.csv", false, false);

			// WritingToFile.appendLineToFileAbsolute(sbEnumerateAllCats.toString(),
			// commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return catIDLengthConsecutives;
	}

	////////

	/**
	 * Return a a map containing working level cat ids for for each cat id in the given cat id name dictionary and given
	 * working level cat id and writes MapForgetWorkingLevelCatIDsForAllCatIDs.csv containing working level cats for
	 * cats and num of not found in hierarchy tree, num of cat having multiple working level cat ids
	 * 
	 * @param catIDNameDictionary
	 * @param workingCatLevel
	 * @param rootOfCategoryTree
	 *            new root, root with newly manually added catids
	 * @return <catid, catid__catid__catid>
	 */
	private static Pair<TreeMap<Integer, String>, LinkedHashSet<Integer>> getWorkingLevelCatIDsForAllCatIDs(
			TreeMap<Integer, String> catIDNameDictionary, int workingCatLevel,
			DefaultMutableTreeNode rootOfCategoryTree)
	{
		TreeMap<Integer, String> res = new TreeMap<>();
		LinkedHashSet<Integer> catIDsInHierarchy = new LinkedHashSet<Integer>();

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
				catIDsInHierarchy.add(cat.getKey());

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
		WToFile.writeToNewFile(sb.toString(), commonPath + "MapWorkingLevelCatIDsForAllCatIDs.csv");

		String s = "numOfCatsWithMultipleWorkingLevelCats = " + numOfCatsWithMultipleWorkingLevelCats
				+ "\nnumOfCatsNotInHierarchyTree = " + numOfCatsNotInHierarchyTree + "\nnumOfCatsInHierarchyTree = "
				+ numOfCatsInHierarchyTree;

		WToFile.appendLineToFileAbs(s, commonPath + "MapWorkingLevelCatIDsForAllCatIDs.csv");

		return new Pair<>(res, catIDsInHierarchy);
	}

	/**
	 * Return a a map containing working level cat ids for for each cat id in the given cat id name dictionary and given
	 * working level cat id and writes MapForgetWorkingLevelCatIDsForAllCatIDs.csv containing working level cats for
	 * cats and num of not found in hierarchy tree, num of cat having multiple working level cat ids
	 * 
	 * @param catIDNameDictionary
	 * @param rootOfCategoryTree
	 *            new root, root with newly manually added catids
	 * @param numOfLevels
	 * @return <catid, catid__catid__catid>
	 */
	private static TreeMap<Integer, String[]> getLevelWiseCatIDsForAllCatIDs(
			TreeMap<Integer, String> catIDNameDictionary, DefaultMutableTreeNode rootOfCategoryTree, int numOfLevels)
	{
		TreeMap<Integer, String[]> res = new TreeMap<>();
		StringBuilder sb = new StringBuilder();
		int numOfCatsWithMultipleWorkingLevelCats = 0, numOfCatsNotInHierarchyTree = 0, numOfCatsInHierarchyTree = 0;

		for (Entry<Integer, String> cat : catIDNameDictionary.entrySet())
		{
			String[] levelWiseCatIDsForThisCatID = new String[numOfLevels];

			for (int level = 1; level <= numOfLevels; level++)
			{
				Set<String> givenLevelOrAboveCatIDs = UIUtilityBox.getGivenLevelCatIDs(String.valueOf(cat.getKey()),
						rootOfCategoryTree, level);

				if (givenLevelOrAboveCatIDs.size() > 0)
				{
					levelWiseCatIDsForThisCatID[level - 1] = givenLevelOrAboveCatIDs.stream()
							.reduce((t, u) -> t + "__" + u).get();
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
			}
			res.put(cat.getKey(), levelWiseCatIDsForThisCatID);
			sb.append(cat.getKey() + "," + Arrays.toString(levelWiseCatIDsForThisCatID) + "\n");
		}
		//
		WToFile.writeToNewFile(sb.toString(), commonPath + "MapLevelWiseCatIDsForAllCatIDs.csv");
		String s = "numOfCatsWithMultipleWorkingLevelCats = " + numOfCatsWithMultipleWorkingLevelCats
				+ "\nnumOfCatsNotInHierarchyTree = " + numOfCatsNotInHierarchyTree + "\nnumOfCatsInHierarchyTree = "
				+ numOfCatsInHierarchyTree;
		WToFile.appendLineToFileAbs(s, commonPath + "MapLevelWiseCatIDsForAllCatIDs.csv");

		return res;
	}

	/**
	 * <p>
	 * Read the checkin file and create checkin entry objects
	 * </p>
	 * <font color = yellow>#CheckinsReadFromData = #checkinNotInHierarchy + #checkinsLevelNotAcceptable +
	 * #checkinsCreated + #checkinsDuplicateTimestampUser</font>
	 * <p>
	 * <font color = orange>Note: Gowalla checkin data read: there exists 281 instances where same user checkins at
	 * different locations for the same timestamp. Currently, i am only considering the most recent location for that
	 * timestamp.</font>
	 * </p>
	 * 
	 * @param checkindatafilename2
	 * @param commonPath2
	 * @param rootOfCategoryTree
	 * @param workingLevelForCat
	 * @param rootOfCategoryTree
	 * @param catIDWorkingLevelCatIDsDict
	 * @param catIDsFoundNodesMap
	 *            (cat id, list of nodes in hierarchy tree at which this cat id is found)
	 * @return
	 */
	private static Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>> createCheckinEntries(
			String checkinFileNameToRead, String commonPath, DefaultMutableTreeNode rootOfCategoryTree,
			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict,
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap, int workingCatLevel)
	{
		int countOfCheckinEntryObjects = 0;
		int numOfDuplicateTimestamps = 0;
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

				String splittedLine[] = RegexUtils.patternComma.split(lineRead);
				// lineRead.split(",");

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

				// Pair<Boolean, String> isAcceptableDirectCatID = isAcceptableDirectCatID(catIDDirect,
				// rootOfCategoryTree);

				// a direct catid is acceptable only if it is present in cat hierarchy tree at one of more nodes and
				// atleast one of those nodes have direct level >= workingLevel (2).. in other words, ignore catid at
				// level 1
				Pair<Boolean, String> isAcceptableDirectCatID = isAcceptableDirectCatIDFaster(catIDDirect,
						catIDsFoundNodesMap, workingCatLevel);

				// catIDWorkingLevelCatIDsDict

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

				countOfCheckinEntryObjects += 1;

				TreeMap<Timestamp, CheckinEntry> mapForThisUser;

				mapForThisUser = result.get(userID); // if userid already in map

				if (mapForThisUser == null) // else create new map for this userid
				{
					mapForThisUser = new TreeMap<Timestamp, CheckinEntry>();
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
			System.out.println(
					"num of CheckinEntry objects created = countOfCheckinEntryObjects =" + countOfCheckinEntryObjects);
			System.out.println("num of duplicate timestamps = " + numOfDuplicateTimestamps);
			System.out.println("actual num of CheckinEntry objects created returned ="
					+ result.entrySet().stream().mapToLong(e -> e.getValue().size()).sum());
			// numOfDuplicateTimestamps

			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		WToFile.appendLineToFileAbs(logRejectedCheckins.toString(), commonPath + "RejectedCheckinsLog.txt");

		System.out.println("----Exiting createCheckinEntries----------------");
		return new Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>>(result,
				locationIDsInCheckinData);

	}

	/**
	 * <p>
	 * Read the checkin file and create checkin entry objects
	 * </p>
	 * <font color = orange>#CheckinsReadFromData = #checkinNotInHierarchy + #checkinsLevelNotAcceptable +
	 * #checkinsCreated + #checkinsDuplicateTimestampUser</font>
	 * <p>
	 * <font color = orange>Note: Gowalla checkin data read: there exists 281 instances where same user checkins at
	 * different locations for the same timestamp. Currently, i am only considering the most recent location for that
	 * timestamp.</font>
	 * </p>
	 * 
	 * @param checkindatafilename2
	 * @param commonPath2
	 * @param rootOfCategoryTree
	 * @param workingLevelForCat
	 * @param rootOfCategoryTree
	 * @param catIDWorkingLevelCatIDsDict
	 * @param catIDLevelWiseCatIDsDict
	 * 
	 * @param catIDsFoundNodesMap
	 *            (cat id, list of nodes in hierarchy tree at which this cat id is found)
	 * @return
	 */
	private static Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>> createCheckinEntries(
			String checkinFileNameToRead, String commonPath, DefaultMutableTreeNode rootOfCategoryTree,
			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict,
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap, int workingCatLevel,
			TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict)
	{
		int countOfCheckinEntryObjects = 0;
		int numOfDuplicateTimestamps = 0;
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> result = new LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>();

		Set<String> locationIDsInCheckinData = new HashSet<String>();

		int countOfLines = 0;

		String lineRead = "";

		ArrayList<Integer> notFoundInFlatMap = new ArrayList<Integer>();
		ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();

		StringBuilder logRejectedCheckins = new StringBuilder("LineNumOfCheckin,Reason,DirectCatID\n");
		long countOfRejectedCheckinNotInHierarchy = 0, countOfRejectedCHeckinBelowLevel2 = 0,
				countOfCinWithMultipleWorkingLevelCatIDs = 0, countOfRejectedCheckinNotInCatIDNameDict = 0;
		System.out.println("----Inside createCheckinEntries----------------");
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

				if (countOfLines % 200000/* 200000 */ == 0)
				{
					System.out.println(" #lines read = " + countOfLines);
				}

				String splittedLine[] = RegexUtils.patternComma.split(lineRead);
				// lineRead.split(",");

				String userID = splittedLine[1];// .replaceAll("\"", ""));
				Integer locationID = Integer.valueOf(splittedLine[2]);// .replaceAll("\"", ""));
				locationIDsInCheckinData.add(locationID.toString());
				Timestamp ts = Timestamp.from(Instant.parse(splittedLine[3].replaceAll("\"", "")));
				String latitude = new String(splittedLine[5]);
				String longitude = new String(splittedLine[6]);
				Integer catIDDirect = Integer.valueOf(splittedLine[7]);// .replaceAll("\"", ""));
				Double distFromNextInM = Double.valueOf(splittedLine[9]);
				Long durationFromNextInM = Long.valueOf(splittedLine[10]);

				// a direct catid is acceptable only if it is present in cat hierarchy tree at one of more nodes and
				// atleast one of those nodes have direct level >= workingLevel (2).. in other words, ignore catid at
				// level 1
				Pair<Boolean, String> isAcceptableDirectCatID = isAcceptableDirectCatIDFaster(catIDDirect,
						catIDsFoundNodesMap, workingCatLevel);

				if (isAcceptableDirectCatID.getFirst() == false)
				{
					String reason = isAcceptableDirectCatID.getSecond();
					// maintaining a log of rejected checkins with the reason for rejection
					logRejectedCheckins.append(countOfLines).append("-").append(reason).append("-").append(catIDDirect)
							.append("\n");
					if (reason.equals("NotInHierarchy"))
					{
						countOfRejectedCheckinNotInHierarchy += 1;
					}
					else if (reason.equals("LevelNotAcceptable"))
					{
						countOfRejectedCHeckinBelowLevel2 += 1;
					}
					else if (reason.equals("NotInCatIDNameDict"))
					{
						countOfRejectedCheckinNotInCatIDNameDict += 1;
					}
					// int countOfRejectedCheckinNotInHierarchy = 0, countOfRejectedCHeckinBelowLevel2 = 0;
					continue;
				}
				String workingLevelCatIDs = catIDWorkingLevelCatIDsDict.get(catIDDirect);
				if (RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs).length > 1)
				{
					countOfCinWithMultipleWorkingLevelCatIDs += 1;
				}
				CheckinEntry cobj = new CheckinEntry(userID, locationID, ts, latitude, longitude, catIDDirect,
						workingLevelCatIDs, distFromNextInM, durationFromNextInM,
						catIDLevelWiseCatIDsDict.get(catIDDirect));

				countOfCheckinEntryObjects += 1;

				TreeMap<Timestamp, CheckinEntry> mapForThisUser;

				mapForThisUser = result.get(userID); // if userid already in map

				if (mapForThisUser == null) // else create new map for this userid
				{
					mapForThisUser = new TreeMap<Timestamp, CheckinEntry>();
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

		WToFile.appendLineToFileAbs(logRejectedCheckins.toString(), commonPath + "RejectedCheckinsLog.txt");

		System.out.println("----Exiting createCheckinEntries----------------");
		return new Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>>(result,
				locationIDsInCheckinData);

	}

	/**
	 * Fork of createCheckinEntries() primarily since the file being read has a slightly different format to parse now.
	 * <p>
	 * Read the checkin file and create checkin entry objects
	 * </p>
	 * <font color = orange>#CheckinsReadFromData = #checkinNotInHierarchy + #checkinsLevelNotAcceptable +
	 * #checkinsCreated + #checkinsDuplicateTimestampUser</font>
	 * <p>
	 * <font color = orange>Note: Gowalla checkin data read: there exists 281 instances where same user checkins at
	 * different locations for the same timestamp. Currently, i am only considering the most recent location for that
	 * timestamp.</font>
	 * </p>
	 * 
	 * @param checkindatafilename2
	 * @param commonPath2
	 * @param rootOfCategoryTree
	 * @param workingLevelForCat
	 * @param rootOfCategoryTree
	 * @param catIDWorkingLevelCatIDsDict
	 * @param catIDLevelWiseCatIDsDict
	 * 
	 * @param catIDsFoundNodesMap
	 *            (cat id, list of nodes in hierarchy tree at which this cat id is found)
	 * @return
	 * @since 23 March 2018
	 */
	private static Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>> createCheckinEntries23Mar2018(
			String checkinFileNameToRead, String commonPath, DefaultMutableTreeNode rootOfCategoryTree,
			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict,
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap, int workingCatLevel,
			TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict)
	{
		int countOfCheckinEntryObjects = 0;
		int numOfDuplicateTimestamps = 0;
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> result = new LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>();

		Set<String> locationIDsInCheckinData = new HashSet<String>();

		int countOfLines = 0;

		String lineRead = "";

		ArrayList<Integer> notFoundInFlatMap = new ArrayList<Integer>();
		ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();

		StringBuilder logRejectedCheckins = new StringBuilder("LineNumOfCheckin,Reason,DirectCatID\n");
		long countOfRejectedCheckinNotInHierarchy = 0, countOfRejectedCHeckinBelowLevel2 = 0,
				countOfCinWithMultipleWorkingLevelCatIDs = 0, countOfRejectedCheckinNotInCatIDNameDict = 0;
		System.out.println("----Inside createCheckinEntries----------------");

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(checkinFileNameToRead));

			String prevUserID = "";
			Timestamp prevTS = null;
			String prevLat = "";
			String prevLon = "";// added on 23 Mar 2018

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

				if (countOfLines % 200000/* 200000 */ == 0)
				{
					System.out.println(" #lines read = " + countOfLines);
					// System.exit(0);
				}

				String splittedLine[] = RegexUtils.patternComma.split(lineRead);
				// lineRead.split(",");
				// System.out.println("splittedLine[]=" + Arrays.asList(splittedLine).toString());

				String userID = splittedLine[0];// .replaceAll("\"", ""));
				Integer locationID = Integer.valueOf(splittedLine[1]);// .replaceAll("\"", ""));
				locationIDsInCheckinData.add(locationID.toString());
				// Timestamp ts = Timestamp.from(Instant.parse(splittedLine[3].replaceAll("\"", "")));

				// 2009-03-16T21:08:46
				LocalDateTime ldtTS = LocalDateTime.parse(splittedLine[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				Timestamp ts = java.sql.Timestamp.valueOf(ldtTS);
				// $$System.out.println("\nts\nread =" + splittedLine[3] + "\nstored=" + ts.toString());//SANITY CHECK
				// OK

				String latitude = new String(splittedLine[5]);
				String longitude = new String(splittedLine[6]);
				Integer catIDDirect = Integer.valueOf(splittedLine[7]);// .replaceAll("\"", ""));
				Double distFromNextInM = Double.valueOf(splittedLine[9]);
				Long durationFromNextInM = Long.valueOf(splittedLine[10]);

				Long durationFromPrevInSec = (long) -99;
				Double distanceFromPrevInM = (double) -99;
				if (prevUserID.equals(userID))
				{
					durationFromPrevInSec = (prevTS.getTime() - ts.getTime()) / 1000;
					distanceFromPrevInM = 1000
							* SpatialUtils.haversineFastMathV2(prevLat, prevLon, latitude, longitude);
				}
				// a direct catid is acceptable only if it is present in cat hierarchy tree at one of more nodes and
				// atleast one of those nodes have direct level >= workingLevel (2).. in other words, ignore catid
				// at level 1
				Pair<Boolean, String> isAcceptableDirectCatID = isAcceptableDirectCatIDFaster(catIDDirect,
						catIDsFoundNodesMap, workingCatLevel);

				if (isAcceptableDirectCatID.getFirst() == false)
				{
					String reason = isAcceptableDirectCatID.getSecond();
					// maintaining a log of rejected checkins with the reason for rejection
					logRejectedCheckins.append(countOfLines).append("-").append(reason).append("-").append(catIDDirect)
							.append("\n");
					if (reason.equals("NotInHierarchy"))
					{
						countOfRejectedCheckinNotInHierarchy += 1;
					}
					else if (reason.equals("LevelNotAcceptable"))
					{
						countOfRejectedCHeckinBelowLevel2 += 1;
					}
					else if (reason.equals("NotInCatIDNameDict"))
					{
						countOfRejectedCheckinNotInCatIDNameDict += 1;
					}
					// int countOfRejectedCheckinNotInHierarchy = 0, countOfRejectedCHeckinBelowLevel2 = 0;

					prevUserID = userID;
					prevTS = ts;
					prevLat = latitude;
					prevLon = longitude;// added on 23 Mar 2018

					continue;
				}
				String workingLevelCatIDs = catIDWorkingLevelCatIDsDict.get(catIDDirect);
				if (RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs).length > 1)
				{
					countOfCinWithMultipleWorkingLevelCatIDs += 1;
				}

				CheckinEntry cobj = new CheckinEntry(userID, locationID, ts, latitude, longitude, catIDDirect,
						workingLevelCatIDs, distFromNextInM, durationFromNextInM,
						catIDLevelWiseCatIDsDict.get(catIDDirect));// , distanceFromPrevInM, durationFromPrevInSec);

				countOfCheckinEntryObjects += 1;

				TreeMap<Timestamp, CheckinEntry> mapForThisUser;

				mapForThisUser = result.get(userID); // if userid already in map

				if (mapForThisUser == null) // else create new map for this userid
				{
					mapForThisUser = new TreeMap<Timestamp, CheckinEntry>();
				}

				if (mapForThisUser.containsKey(ts))
				{
					System.err.println("Error: duplicate ts: map for this user (userID=" + userID
							+ ") already contains ts =" + ts.toString());
					numOfDuplicateTimestamps += 1;
				}

				mapForThisUser.put(ts, cobj);
				result.put(userID, mapForThisUser);

				prevUserID = userID;
				prevTS = ts;
				prevLat = latitude;
				prevLon = longitude;// added on 23 Mar 2018
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

		WToFile.appendLineToFileAbs(logRejectedCheckins.toString(), commonPath + "RejectedCheckinsLog.txt");

		System.out.println("----Exiting createCheckinEntries----------------");
		return new Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>, Set<String>>(result,
				locationIDsInCheckinData);

	}

	/**
	 * Fork of createCheckinEntries23Mar2018() primarily since the file being read has a slightly different format to
	 * parse now.
	 * <p>
	 * Read the checkin file and create checkin entry objects
	 * </p>
	 * <font color = orange>#CheckinsReadFromData = #checkinNotInHierarchy + #checkinsLevelNotAcceptable +
	 * #checkinsCreated + #checkinsDuplicateTimestampUser</font>
	 * <p>
	 * <font color = orange>Note: Gowalla checkin data read: there exists 281 instances where same user checkins at
	 * different locations for the same timestamp. Currently, i am only considering the most recent location for that
	 * timestamp.</font>
	 * </p>
	 * 
	 * @param checkinFileNameToRead
	 * @param commonPath
	 * @param rootOfCategoryTree
	 * @param catIDWorkingLevelCatIDsDict
	 * @param catIDsFoundNodesMap
	 * @param workingCatLevel
	 * @param catIDLevelWiseCatIDsDict
	 * 
	 * @param catIDsFoundNodesMap
	 *            (cat id, list of nodes in hierarchy tree at which this cat id is found)
	 * @return
	 * @since 3 April 2018
	 */
	private static Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>> createCheckinEntries3April2018(
			String checkinFileNameToRead, String commonPath, DefaultMutableTreeNode rootOfCategoryTree,
			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict,
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap, int workingCatLevel,
			TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict)
	{
		int countOfCheckinEntryObjects = 0;
		int numOfDuplicateTimestamps = 0;
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> result = new LinkedHashMap<>();

		Set<String> locationIDsInCheckinData = new HashSet<String>();

		int countOfLines = 0;

		String lineRead = "";

		ArrayList<Integer> notFoundInFlatMap = new ArrayList<Integer>();
		ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();

		StringBuilder logRejectedCheckins = new StringBuilder("LineNumOfCheckin,Reason,DirectCatID\n");
		long countOfRejectedCheckinNotInHierarchy = 0, countOfRejectedCHeckinBelowLevel2 = 0,
				countOfCinWithMultipleWorkingLevelCatIDs = 0, countOfRejectedCheckinNotInCatIDNameDict = 0;
		System.out.println("----Inside createCheckinEntries----------------");

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

				String userID = splittedLine[0];// .replaceAll("\"", ""));
				Integer locationID = Integer.valueOf(splittedLine[1]);// .replaceAll("\"", ""));
				locationIDsInCheckinData.add(locationID.toString());
				// Timestamp ts = Timestamp.from(Instant.parse(splittedLine[3].replaceAll("\"", "")));

				// 2009-03-16T21:08:46
				LocalDateTime ldtTS = LocalDateTime.parse(splittedLine[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				Timestamp ts = java.sql.Timestamp.valueOf(ldtTS);
				// $$System.out.println("\nts\nread =" + splittedLine[3] + "\nstored=" + ts.toString());//SANITY CHECK
				// OK

				String latitude = new String(splittedLine[5]);
				String longitude = new String(splittedLine[6]);
				Integer catIDDirect = Integer.valueOf(splittedLine[7]);// .replaceAll("\"", ""));

				// Note in the dataset being read: from prev line --> from next checkin and from next line--> from prev
				// checkin, since the data read in R for the creating was that dataset was in reverse order by time.
				Double distFromNextCheckinInM = Double.valueOf(splittedLine[9]);
				Long durationFromNextCheckinInM = Long.valueOf(splittedLine[10]);
				String timeZone = new String(splittedLine[11]);

				Double distFromPrevCheckinInM = Double.valueOf(splittedLine[12]);
				Long durationFromPrevCheckinInM = Long.valueOf(splittedLine[13]);

				// a direct catid is acceptable only if it is present in cat hierarchy tree at one of more nodes and
				// atleast one of those nodes have direct level >= workingLevel (2).. in other words, ignore catid
				// at level 1
				Pair<Boolean, String> isAcceptableDirectCatID = isAcceptableDirectCatIDFaster(catIDDirect,
						catIDsFoundNodesMap, workingCatLevel);

				if (isAcceptableDirectCatID.getFirst() == false)
				{
					String reason = isAcceptableDirectCatID.getSecond();
					// maintaining a log of rejected checkins with the reason for rejection
					logRejectedCheckins.append(countOfLines).append("-").append(reason).append("-").append(catIDDirect)
							.append("\n");
					if (reason.equals("NotInHierarchy"))
					{
						countOfRejectedCheckinNotInHierarchy += 1;
					}
					else if (reason.equals("LevelNotAcceptable"))
					{
						countOfRejectedCHeckinBelowLevel2 += 1;
					}
					else if (reason.equals("NotInCatIDNameDict"))
					{
						countOfRejectedCheckinNotInCatIDNameDict += 1;
					}
					continue;
				}

				String workingLevelCatIDs = catIDWorkingLevelCatIDsDict.get(catIDDirect);
				if (RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs).length > 1)
				{
					countOfCinWithMultipleWorkingLevelCatIDs += 1;
				}

				CheckinEntryV2 cobj = new CheckinEntryV2(userID, locationID, ts, latitude, longitude, catIDDirect,
						workingLevelCatIDs, distFromPrevCheckinInM, durationFromPrevCheckinInM,
						catIDLevelWiseCatIDsDict.get(catIDDirect), distFromNextCheckinInM, durationFromNextCheckinInM,
						timeZone);// , distanceFromPrevInM, durationFromPrevInSec);

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

		WToFile.appendLineToFileAbs(logRejectedCheckins.toString(), commonPath + "RejectedCheckinsLog.txt");

		System.out.println("----Exiting createCheckinEntries----------------");
		return new Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>>(result,
				locationIDsInCheckinData);

	}

	/**
	 * 
	 * @param s
	 * @param delimiter
	 * @return
	 */
	public static ArrayList<String> splitAsStringList(String s, Pattern delimiter)
	{
		ArrayList<String> res = new ArrayList();
		List<String> resTemp = Arrays.asList(delimiter.split(s));
		res.addAll(resTemp);
		return res;
	}

	/**
	 * 
	 * @param s
	 * @param delimiter
	 * @return
	 */
	public static ArrayList<Integer> splitAsIntegerList(String s, Pattern delimiter)
	{
		List<String> resTemp = Arrays.asList(delimiter.split(s));
		ArrayList<Integer> res = (ArrayList<Integer>) resTemp.stream().map(i -> Integer.valueOf(i))
				.collect(Collectors.toList());
		return res;
	}

	/**
	 * Fork of createCheckinEntries3April2018() primarily since the file being read has a slightly different format to
	 * parse now.
	 * <p>
	 * Read the checkin file and create checkin entry objects
	 * </p>
	 * <font color = orange>#CheckinsReadFromData = #checkinNotInHierarchy + #checkinsLevelNotAcceptable +
	 * #checkinsCreated + #checkinsDuplicateTimestampUser</font>
	 * <p>
	 * <font color = orange>Note: Gowalla checkin data read: there exists 281 instances where same user checkins at
	 * different locations for the same timestamp. Currently, i am only considering the most recent location for that
	 * timestamp.</font>
	 * </p>
	 * 
	 * @param checkinFileNameToRead
	 * @param commonPath
	 * @param rootOfCategoryTree
	 * @param catIDWorkingLevelCatIDsDict
	 * @param catIDsFoundNodesMap
	 * @param workingCatLevel
	 * @param catIDLevelWiseCatIDsDict
	 * 
	 * @param catIDsFoundNodesMap
	 *            (cat id, list of nodes in hierarchy tree at which this cat id is found)
	 * @return
	 * @since 9 April 2018
	 */
	private static Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>> createCheckinEntries8April2018(
			String checkinFileNameToRead, String commonPath, DefaultMutableTreeNode rootOfCategoryTree,
			TreeMap<Integer, String> catIDWorkingLevelCatIDsDict,
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIDsFoundNodesMap, int workingCatLevel,
			TreeMap<Integer, String[]> catIDLevelWiseCatIDsDict)
	{
		int countOfCheckinEntryObjects = 0;
		int numOfDuplicateTimestamps = 0;
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> result = new LinkedHashMap<>();

		Set<String> locationIDsInCheckinData = new HashSet<>();

		int countOfLines = 0;

		String lineRead = "";

		ArrayList<Integer> notFoundInFlatMap = new ArrayList<Integer>();
		ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();

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
				ArrayList<Integer> locationIDs = splitAsIntegerList(splittedLine[1], underScore);
				// Integer locationID = Integer.valueOf(splittedLine[1]);// .replaceAll("\"", ""));
				locationIDsInCheckinData
						.addAll(locationIDs.stream().map(i -> String.valueOf(i)).collect(Collectors.toList()));
				// Timestamp ts = Timestamp.from(Instant.parse(splittedLine[3].replaceAll("\"", "")));

				// 2009-03-16T21:08:46
				// LocalDateTime ldtTS = LocalDateTime.parse(splittedLine[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				Timestamp ts = java.sql.Timestamp.valueOf(splittedLine[2]);
				// System.out.println("\nts\nread =" + splittedLine[2] + "\nstored=" + ts.toString());// SANITY CHECK
				// OK

				ArrayList<String> latitudes = splitAsStringList(splittedLine[3], underScore);
				ArrayList<String> longitudes = splitAsStringList(splittedLine[4], underScore);

				Integer catIDDirect = Integer.valueOf(splittedLine[5]);
				String workingLevelCatIDs = splittedLine[6];

				// Note in the dataset being read: from prev line --> from next checkin and from next line--> from prev
				// checkin, since the data read in R for the creating was that dataset was in reverse order by time.
				Double distanceInMetersFromPrev = Double.valueOf(splittedLine[7]);
				Long durationInSecsFromPrev = Long.valueOf(splittedLine[8]);
				String[] levelWiseCatIDs = underScore.split(splittedLine[9]);
				Double distanceInMeterFromNextCheckin = Double.valueOf(splittedLine[10]);
				Long durationInSecsFromNextCheckin = Long.valueOf(splittedLine[11]);
				String tz = new String(splittedLine[12]);

				// a direct catid is acceptable only if it is present in cat hierarchy tree at one of more nodes and
				// atleast one of those nodes have direct level >= workingLevel (2).. in other words, ignore catid
				// at level 1
				Pair<Boolean, String> isAcceptableDirectCatID = isAcceptableDirectCatIDFaster(catIDDirect,
						catIDsFoundNodesMap, workingCatLevel);

				if (isAcceptableDirectCatID.getFirst() == false)
				{
					String reason = isAcceptableDirectCatID.getSecond();
					// maintaining a log of rejected checkins with the reason for rejection
					logRejectedCheckins.append(countOfLines).append("-").append(reason).append("-").append(catIDDirect)
							.append("\n");
					if (reason.equals("NotInHierarchy"))
					{
						countOfRejectedCheckinNotInHierarchy += 1;
					}
					else if (reason.equals("LevelNotAcceptable"))
					{
						countOfRejectedCHeckinBelowLevel2 += 1;
					}
					else if (reason.equals("NotInCatIDNameDict"))
					{
						countOfRejectedCheckinNotInCatIDNameDict += 1;
					}
					continue;
				}

				String workingLevelCatIDsAgain = catIDWorkingLevelCatIDsDict.get(catIDDirect);
				Sanity.eq(workingLevelCatIDs, workingLevelCatIDsAgain, "Error  in workingLevelCatIDs");
				if (RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs).length > 1)
				{
					countOfCinWithMultipleWorkingLevelCatIDs += 1;
				}

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

		WToFile.appendLineToFileAbs(logRejectedCheckins.toString(), commonPath + "RejectedCheckinsLog.txt");

		System.out.println("----Exiting createCheckinEntries----------------");
		return new Pair<LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>, Set<String>>(result,
				locationIDsInCheckinData);

	}

	/**
	 * Checks if the category id is present in hierarchy tree and at a level >=workingLevelCatID (=2) (i.e.,more than
	 * level 1).
	 * 
	 * @param catIDDirect
	 * @param rootOfCategoryTree
	 * @param catIDWorkingLevelCatIDsDict
	 * @return pair which can be either of (true, "Acceptable"),(false, "NotInHierarchy"),(false, "LevelNotAcceptable")
	 */
	private static Pair<Boolean, String> isAcceptableDirectCatIDFaster(Integer catIDToSearch,
			LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIdFoundNodesMap, int workingLevelCatID)
	{
		// get nodes containing this catID
		ArrayList<DefaultMutableTreeNode> foundNodes = catIdFoundNodesMap.get(String.valueOf(catIDToSearch));

		if (foundNodes == null)
		{
			System.out.println("foundNodes==null");
			return new Pair<Boolean, String>(false, "NotInCatIDNameDict");
		}
		// if (catIdFoundNodesMap == null)
		// {
		// System.out.println("catIdFoundNodesMap==null");
		// }
		// if (catIDToSearch == null)
		// {
		// System.out.println("catIDToSearch==null");
		// }

		// UIUtilityBox.recursiveDfsMulipleOccurences2OnlyCatID(
		// rootOfCategoryTree, catIDToSearch.toString(), new ArrayList<DefaultMutableTreeNode>());

		// System.out.println("num of matching nodes found = " + foundNodes2.size());
		// boolean isLevelAcceptable = false;

		if (foundNodes.size() > 0) // is it there in hierarchy tree
		{
			// check if any of these nodes containing catID are at level >=2.
			for (DefaultMutableTreeNode foundnode : foundNodes)
			{
				if (foundnode.getLevel() >= workingLevelCatID) // atleast level 2
				{
					// isLevelAcceptable = true;
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
		// TODO Performance: this can be performance optimised by creating a list once by traversing the hierarchy
		// instead of
		// recursively traversing it for every call.

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
	private static LinkedHashMap<Integer, LocationGowalla> createLocationGowalla0(String locationFileNameToRead,
			Set<String> locationIDsInCheckinData, String commonPath)
	{
		// locationid
		LinkedHashMap<Integer, LocationGowalla> result = new LinkedHashMap<>();
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

				String splittedLine[] = RegexUtils.patternComma.split(lineRead);// lineRead.split(",");

				Integer locID = Integer.valueOf(splittedLine[0]);

				// only if this userid is in ur checkin data
				if (locationIDsInCheckinData.contains(Integer.toString(locID)))// String.valueOf(locID)))
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
					// country, String continent, String locationID, int photos_count, int checkins_count, int
					// users_count, int radius_meters, int highlights_count, int items_count, int max_items_count)
					result.put(locID, cobj);
				}
			}

			System.out.println("num of location objects = " + result.size());
			System.out.println("num of lines read = " + countOfLines);
			br.close();
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
	private static LinkedHashMap<Integer, LocationGowalla> createLocationGowalla(String locationFileNameToRead,
			Set<String> locationIDsInCheckinData, String commonPath)
	{

		LinkedHashMap<Integer, LocationGowalla> result = new LinkedHashMap<>();
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
				Integer locID = Integer.valueOf(locEntry.getKey());

				ArrayList<String> rest = locEntry.getValue();

				Pair<String, String> spotCatIDName = DatabaseCreatorGowallaQuickerPreprocessor
						.getSpotCatIDCatName(rest);

				String spotCatID = spotCatIDName.getFirst();
				String spotCatName = spotCatIDName.getSecond();

				LocationGowalla cobj = new LocationGowalla(rest.get(2), rest.get(1), spotCatName, spotCatID, "", "", "",
						"", locID, Integer.valueOf(rest.get(3)), Integer.valueOf(rest.get(4)),
						Integer.valueOf(rest.get(5)), Integer.valueOf(rest.get(6)), Integer.valueOf(rest.get(7)),
						Integer.valueOf(rest.get(8)), Integer.valueOf(rest.get(9)));

				result.put(locID, cobj);

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
