package org.activity.constants;

import java.util.List;
import java.util.stream.Collectors;

import org.activity.io.ReadingFromFile;

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
	public static final String pathToToyTimelines = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN7ED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrs75RTVToyRun6Chosen/ToyTimelinesManually6June.kryo";

	public static final String pathToFileWithIndicesOfGTZeroUsers = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN29ResultsDistributionFirstToMax3/FiveDays/Concatenated/MinMUWithMaxFirst3_GTZero.csv";

	// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY30Toy/ToyTimelinesManually28May.kryo";

	/**
	 * Map<Long, Long>
	 * <p>
	 * see: org.giscience.utils.geogrid.gunjanUtils.GLatLonToGridTransformer.main(String[]).locIDGridIDMap
	 */
	public static final String pathToSerialisedLocIDGridIDGowallaMap = "./dataToRead/HexGridRes16_JUL17/locIDGridIDMap.kryo";

	/**
	 * Map<Long, Integer>
	 * <p>
	 * see: org.giscience.utils.geogrid.gunjanUtils.GLatLonToGridTransformer.main(String[]).locIDGridIDMap
	 */
	public static final String pathToSerialisedLocIDGridIndexGowallaMap = "./dataToRead/HexGridRes16_JUL17/locIDGridIndexMap.kryo";

	/**
	 * Map<Long, Set<Long>>
	 * <p>
	 * see: org.giscience.utils.geogrid.gunjanUtils.GLatLonToGridTransformer.main(String[]).gridIDLocIDs
	 */
	public static final String pathToSerialisedGridIDLocIDsGowallaMap = "./dataToRead/HexGridRes16_JUL17/gridIDLocIDs.kryo";

	public static final String pathToSerialisedGridIndexPairDist = "./dataWritten/JUL25GridIndexDistances/pairedIndicesTo1DConverterIntDoubleWith1DConverter.kryo";
	public static final String pathToSerialisedGridIndexPairDistConverter = "./dataWritten/JUL25GridIndexDistances/gridIndexPairHaversineDistIntDoubleWith1DConverter.kryo";

	public static final String pathToSerialisedHaversineDistOnEngine = "./dataWritten/AUG2GridIndexDistances/";

	public static final String pathToJavaGridIndexRGridLatRGridLon = "./dataToRead/July30RGridIDJavaGridIndex/javaGridIndexRGridLatRGridLon.kryo";

	/**
	 * Set paths to serialised datasets
	 * 
	 * @param For9kUsers
	 */
	public static final void intialise(boolean For9kUsers)
	{

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
