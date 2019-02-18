package org.activity.constants;

import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.objects.Timeline;
import org.activity.ui.PopUps;

/**
 * To centralise paths which need to be set for experiments
 * 
 * @author gunjan
 *
 */
public class PathConstants
{
	public static String commonPathToGowallaPreProcessedData;
	public static String pathToSerialisedCatIDNameDictionary;
	public static String pathToSerialisedLocationObjects;
	public static String pathToSerialisedUniqueLocIDsInCleanedTimelines;
	public static String pathToSerialisedCatIDsHierDist;
	public static String pathToSerialisedLevelWiseCatIDsDict;
	public static String pathToSerialisedMergedCheckinData;
	public static String pathToSerialisedUserObjects;
	public static String pathToLocationTimezoneInfo;
	public static String pathToSerialisedGowallaLocZoneIdMap;
	// ./dataToRead/Feb26/UniqueLocationObjects5DaysTrainTestWithTZUsingPy.csv
	public static String pathToToyTimelines6JUN;// =
												// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN7ED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrs75RTVToyRun6Chosen/ToyTimelinesManually6June.kryo";
	public static String pathToToyTimelines12AUG;// =
													// "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/AUG12ToyTimelineCreation/ToyTimelinesManuallyAUG12.kryo";
	public static String pathToFileWithIndicesOfGTZeroUsers;// =
															// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN29ResultsDistributionFirstToMax3/FiveDays/Concatenated/MinMUWithMaxFirst3_GTZero.csv";

	// moved from SuperController on 12 Sep 2018
	public static String[] pathToSetsOfRandomlySampled100Users;
	// = {
	// "./dataToRead/RandomlySample100UsersApril24_2018.csv",
	// "./dataToRead/RandomlySample100UsersApril24_2018.SetB",
	// "./dataToRead/RandomlySample100UsersApril24_2018.SetC",
	// "./dataToRead/RandomlySample100UsersApril24_2018.SetD",
	// "./dataToRead/RandomlySample100UsersApril24_2018.SetE" };

	// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY30Toy/ToyTimelinesManually28May.kryo";

	/**
	 * Map<Long, Long>
	 * <p>
	 * see: org.giscience.utils.geogrid.gunjanUtils.GLatLonToGridTransformer.main(String[]).locIDGridIDMap
	 */
	public static String pathToSerialisedLocIDGridIDGowallaMap;// =
																// "./dataToRead/HexGridRes16_JUL17/locIDGridIDMap.kryo";

	/**
	 * Map<Long, Integer>
	 * <p>
	 * see: org.giscience.utils.geogrid.gunjanUtils.GLatLonToGridTransformer.main(String[]).locIDGridIDMap
	 */
	public static String pathToSerialisedLocIDGridIndexGowallaMap;// =
																	// "./dataToRead/HexGridRes16_JUL17/locIDGridIndexMap.kryo";

	/**
	 * Map<Long, Set<Long>>
	 * <p>
	 * see: org.giscience.utils.geogrid.gunjanUtils.GLatLonToGridTransformer.main(String[]).gridIDLocIDs
	 */
	public static String pathToSerialisedGridIDLocIDsGowallaMap;// =
																// "./dataToRead/HexGridRes16_JUL17/gridIDLocIDs.kryo";

	public static String pathToSerialisedGridIndexPairDist;// =
															// "./dataWritten/JUL25GridIndexDistances/pairedIndicesTo1DConverterIntDoubleWith1DConverter.kryo";
	public static String pathToSerialisedGridIndexPairDistConverter;// =
																	// "./dataWritten/JUL25GridIndexDistances/gridIndexPairHaversineDistIntDoubleWith1DConverter.kryo";

	public static String pathToSerialisedHaversineDistOnEngine;// = "./dataWritten/AUG2GridIndexDistances/";

	public static String pathToJavaGridIndexRGridLatRGridLon;// =
																// "./dataToRead/July30RGridIDJavaGridIndex/javaGridIndexRGridLatRGridLon.kryo";

	// start of added on 28 Dec 2018
	public static final String pathToSerializedDCUCleanedTimelines28Dec = "./dataToRead/SerializedTimelines28Dec2018/dcu_data_2_DEC28H19M26HighDurNoTTFilter/usersCleanedDayTimelines.kryo";
	public static final String pathToSerializedDCUCleanedInvalidsExpungedTimelines28Dec = "./dataToRead/SerializedTimelines28Dec2018/dcu_data_2_DEC28H19M26HighDurNoTTFilter/usersCleanedInvalidsExpungedDayTimelines.kryo";
	public static final String pathToSerializedGeolifeCleanedTimelines28Dec = "./dataToRead/SerializedTimelines28Dec2018/geolife1_DEC28H19M28HighDurPNN500NoTTFilter/usersCleanedDayTimelines.kryo";
	public static final String pathToSerializedGowallaCleanedTimelines28Dec = "./dataToRead/SerializedTimelines28Dec2018/gowalla1_DEC28H19M28HighDurPNN500coll/usersCleanedDayTimelines.kryo";
	// end of added on 28 Dec 2018

	///
	public static final String pathToSerialisedDCUCleanedTimelines12Feb2019 = "./dataToRead/SerializedTimeline17Feb2019/dcu_data_2_written/usersCleanedDayTimelines.kryo";
	/// home/gunjan/git/GeolifeReloaded2_1_cleaned/dataToRead/SerializedTimeline17Feb2019/gowalla1_written/usersCleanedDayTimelines.kryo
	public static final String pathToSerialisedGeolifeCleanedTimelines12Feb2019 = "./dataToRead/SerializedTimeline17Feb2019/geolife1_written/usersCleanedDayTimelines.kryo";
	public static final String pathToSerialisedGowallaCleanedTimelines12Feb2019 = "./dataToRead/SerializedTimeline17Feb2019/gowalla1_written/usersCleanedDayTimelines.kryo";
	///

	/**
	 * 
	 * @param databaseName
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> deserializeAndGetCleanedTimelines28Dec(
			String databaseName)
	{
		switch (databaseName)
		{
		case "dcu_data_2":
			System.err.println("Warning: using invalids expunged timelines: ");
			return (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
					.kryoDeSerializeThis(pathToSerializedDCUCleanedTimelines28Dec);// pathToSerializedDCUCleanedInvalidsExpungedTimelines28Dec);
		case "geolife1":
			return (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
					.kryoDeSerializeThis(pathToSerializedGeolifeCleanedTimelines28Dec);
		case "gowalla1":
			return (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
					.kryoDeSerializeThis(pathToSerializedGowallaCleanedTimelines28Dec);
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown databaseName: " + databaseName);
			return null;
		}
	}

	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> deserializeAndGetCleanedTimelinesFeb2019(
			String databaseName)
	{
		switch (databaseName)
		{
		case "dcu_data_2":
			System.err.println("Warning: using invalids expunged timelines: ");
			return (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
					.kryoDeSerializeThis(pathToSerialisedDCUCleanedTimelines12Feb2019);// pathToSerializedDCUCleanedInvalidsExpungedTimelines28Dec);
		case "geolife1":
			return (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
					.kryoDeSerializeThis(pathToSerialisedGeolifeCleanedTimelines12Feb2019);
		case "gowalla1":
			return (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
					.kryoDeSerializeThis(pathToSerialisedGowallaCleanedTimelines12Feb2019);
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown databaseName: " + databaseName);
			return null;
		}
	}

	/**
	 * Set paths to serialised datasets
	 * 
	 * @param For9kUsers
	 * @param databaseName
	 *            (added on 12 Oct 2018)
	 */
	public static final void intialise(boolean For9kUsers, String databaseName)
	{
		if (databaseName.equals("fsny1"))
		{
			String commonPath = "./dataToRead/FSNY/";
			// commonPathToGowallaPreProcessedData = "./dataToRead/April25_2018/";
			// System.out.println("commonPathToGowallaPreProcessedData= " + commonPathToGowallaPreProcessedData);
			pathToSerialisedCatIDNameDictionary = commonPath + "catIDIntCatName.kryo";
			pathToSerialisedLocationObjects = "";
			pathToSerialisedLevelWiseCatIDsDict = "";
			pathToSerialisedCatIDsHierDist = "";
			pathToSerialisedMergedCheckinData = commonPath + "mapForAllCheckinData.kryo";
			pathToSerialisedUserObjects = "";
			// Irrelvant but included for conformity.
			pathToLocationTimezoneInfo = "";
			pathToSerialisedGowallaLocZoneIdMap = "";
		}
		else if (databaseName.equals("gowalla1"))
		{
			pathToToyTimelines6JUN = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN7ED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrs75RTVToyRun6Chosen/ToyTimelinesManually6June.kryo";
			pathToToyTimelines12AUG = "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/AUG12ToyTimelineCreation/ToyTimelinesManuallyAUG12.kryo";
			pathToFileWithIndicesOfGTZeroUsers = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN29ResultsDistributionFirstToMax3/FiveDays/Concatenated/MinMUWithMaxFirst3_GTZero.csv";

			pathToSetsOfRandomlySampled100Users = new String[] { "./dataToRead/RandomlySample100UsersApril24_2018.csv",
					"./dataToRead/RandomlySample100UsersApril24_2018.SetB",
					"./dataToRead/RandomlySample100UsersApril24_2018.SetC",
					"./dataToRead/RandomlySample100UsersApril24_2018.SetD",
					"./dataToRead/RandomlySample100UsersApril24_2018.SetE" };

			pathToSerialisedLocIDGridIDGowallaMap = "./dataToRead/HexGridRes16_JUL17/locIDGridIDMap.kryo";
			pathToSerialisedLocIDGridIndexGowallaMap = "./dataToRead/HexGridRes16_JUL17/locIDGridIndexMap.kryo";
			pathToSerialisedGridIDLocIDsGowallaMap = "./dataToRead/HexGridRes16_JUL17/gridIDLocIDs.kryo";
			pathToSerialisedGridIndexPairDist = "./dataWritten/JUL25GridIndexDistances/pairedIndicesTo1DConverterIntDoubleWith1DConverter.kryo";
			pathToSerialisedGridIndexPairDistConverter = "./dataWritten/JUL25GridIndexDistances/gridIndexPairHaversineDistIntDoubleWith1DConverter.kryo";

			pathToSerialisedHaversineDistOnEngine = "./dataWritten/AUG2GridIndexDistances/";
			pathToJavaGridIndexRGridLatRGridLon = "./dataToRead/July30RGridIDJavaGridIndex/javaGridIndexRGridLatRGridLon.kryo";

			if (For9kUsers)
			{
				/**
				 * Start of Gowalla path constants for Aug 11 experiments: 9k users
				 */
				commonPathToGowallaPreProcessedData = "./dataToRead/Aug10/DatabaseCreatedMerged/";
				// "./dataToRead/Mar30/DatabaseCreatedMerged/";//// Feb23

				pathToSerialisedCatIDNameDictionary = "./dataToRead/UI/CatIDNameDictionary.kryo";

				// "./dataToRead/Mar30/DatabaseCreatedMerged/mapForAllLocationData.kryo";

				// $$public final static String pathToSerialisedUniqueLocIDsInCleanedTimelines =
				// "./dataToRead/Mar30/DatabaseCreatedMerged/UniqueLocIDsInCleanedTimeines.ser";

				pathToSerialisedLocationObjects = commonPathToGowallaPreProcessedData + "mapForAllLocationData.kryo";
				pathToSerialisedLevelWiseCatIDsDict = commonPathToGowallaPreProcessedData
						+ "mapCatIDLevelWiseCatIDsDict.kryo";
				pathToSerialisedCatIDsHierDist = commonPathToGowallaPreProcessedData + "mapCatIDsHierDist.kryo";//
				// "./dataToRead/April7/mapCatIDsHierDist.kryo";"./dataToRead/April7/mapCatIDsHierDist.kryo"
				pathToSerialisedMergedCheckinData = commonPathToGowallaPreProcessedData
						+ "DatabaseCreatedMerged/mapForAllCheckinData.kryo";
				pathToSerialisedUserObjects = commonPathToGowallaPreProcessedData + "mapForAllUserData.kryo";
				/**
				 * End of Gowalla path constants for Aug 11 experiments
				 */

			}
			else
			{
				/**
				 * Start of Gowalla path constants for April 8 2018 experiments: 143 users
				 */
				// commonPathToGowallaPreProcessedData = "./dataToRead/April8_2018/";
				commonPathToGowallaPreProcessedData = "./dataToRead/April25_2018/";
				System.out.println("commonPathToGowallaPreProcessedData= " + commonPathToGowallaPreProcessedData);

				pathToSerialisedCatIDNameDictionary = "./dataToRead/UI/CatIDNameDictionary.kryo";
				pathToSerialisedLocationObjects = commonPathToGowallaPreProcessedData + "mapForAllLocationData.kryo";
				pathToSerialisedLevelWiseCatIDsDict = commonPathToGowallaPreProcessedData
						+ "mapCatIDLevelWiseCatIDsDict.kryo";
				pathToSerialisedCatIDsHierDist = commonPathToGowallaPreProcessedData + "mapCatIDsHierDist.kryo";//
				// "./dataToRead/April7/mapCatIDsHierDist.kryo";"./dataToRead/April7/mapCatIDsHierDist.kryo"
				pathToSerialisedMergedCheckinData = commonPathToGowallaPreProcessedData
						+ "DatabaseCreatedMerged/mapForAllCheckinData.kryo";
				pathToSerialisedUserObjects = commonPathToGowallaPreProcessedData + "mapForAllUserData.kryo";
				/**
				 * End of Gowalla path constants for April 8 2018 experiments: 143 users
				 */

				// Irrelvant but included for conformity.
				pathToLocationTimezoneInfo = "";
				pathToSerialisedGowallaLocZoneIdMap = "";

			}
		}
		else if (databaseName.equals("dcu_data_2"))
		{

			pathToSerialisedCatIDNameDictionary = "";
			pathToSerialisedLocationObjects = "";
			pathToSerialisedLevelWiseCatIDsDict = "";
			pathToSerialisedCatIDsHierDist = "";
			pathToSerialisedMergedCheckinData = "./dataToRead/DCULLDec15/mapForAllDataMergedPlusDuration.map";
			pathToSerialisedUserObjects = "";
			// Irrelvant but included for conformity.
			pathToLocationTimezoneInfo = "";
			pathToSerialisedGowallaLocZoneIdMap = "";
			System.out.println(""
					+ "Warning :setting empty paths in org.activity.constants.PathConstants.intialise(boolean, String) for unknown database: "
					+ Constant.getDatabaseName());
		}
		else
		{

			pathToSerialisedCatIDNameDictionary = "";
			pathToSerialisedLocationObjects = "";
			pathToSerialisedLevelWiseCatIDsDict = "";
			pathToSerialisedCatIDsHierDist = "";
			pathToSerialisedMergedCheckinData = "";
			pathToSerialisedUserObjects = "";
			// Irrelvant but included for conformity.
			pathToLocationTimezoneInfo = "";
			pathToSerialisedGowallaLocZoneIdMap = "";
			System.out.println(""
					+ "Warning :setting empty paths in org.activity.constants.PathConstants.intialise(boolean, String) for unknown database: "
					+ Constant.getDatabaseName());
		}
		// pathToSerialisedMergedCheckinData
		// //start of curtain April 9 2018
		// else
		// {
		// /**
		// * Start of Gowalla path constants for before Aug 11 experiments: 916 users
		// */
		// commonPathToGowallaPreProcessedData = "./dataToRead/Mar30/DatabaseCreatedMerged/";//// Feb23
		//
		// pathToSerialisedCatIDNameDictionary = "./dataToRead/UI/CatIDNameDictionary.kryo";
		// pathToSerialisedLocationObjects = "./dataToRead/Mar30/DatabaseCreatedMerged/mapForAllLocationData.kryo";
		// // pathToSerialisedUniqueLocIDsInCleanedTimelines =
		// // "./dataToRead/Mar30/DatabaseCreatedMerged/UniqueLocIDsInCleanedTimeines.ser";
		// pathToSerialisedUniqueLocIDsInCleanedTimelines = "./dataToRead/July12/UniqueLocIDsInCleanedTimeines.kryo";
		//
		// pathToSerialisedLevelWiseCatIDsDict = "./dataToRead/May17/mapCatIDLevelWiseCatIDsDict.kryo";
		// pathToSerialisedCatIDsHierDist = "./dataToRead/April7/mapCatIDsHierDist.kryo";
		// pathToSerialisedUserObjects = "./dataToRead/Mar30/DatabaseCreatedMerged/mapForAllUserData.kryo";
		// /**
		// * End of Gowalla path constants for before Aug 11 experiments
		// */
		//
		// pathToLocationTimezoneInfo = "./dataToRead/Feb26/UniqueLocationObjects5DaysTrainTestWithTZUsingPy.csv";
		// pathToSerialisedGowallaLocZoneIdMap =
		// "./dataToRead/Feb26/UniqueLocationObjects5DaysTrainTestWithTZUsingPy.kryo";
		// }
		// //end of curtain April 9 2018
	}
	// public final static String pathToSerialisedCatIDsHierDist;

	/**
	 * 
	 */
	public static List<String> o(String fileWithIndicesOfUserWithGTZero)
	{
		List<String> readData = ReadingFromFile.oneColumnReaderString(fileWithIndicesOfUserWithGTZero, ",", 0, false);
		System.out.println("Read userIndicesWithGTZeroBestMU: " + readData.size() + " indices :- \n"
				+ readData.stream().collect(Collectors.joining("\n")));
		return readData;
	}

}
