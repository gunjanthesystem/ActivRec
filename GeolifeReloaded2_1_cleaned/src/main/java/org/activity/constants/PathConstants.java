package org.activity.constants;

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
			 * Start of Gowalla path constants for before Aug 11 experiments: 916 users
			 */
			commonPathToGowallaPreProcessedData = "./dataToRead/Mar30/DatabaseCreatedMerged/";//// Feb23

			pathToSerialisedCatIDNameDictionary = "./dataToRead/UI/CatIDNameDictionary.kryo";
			pathToSerialisedLocationObjects = "./dataToRead/Mar30/DatabaseCreatedMerged/mapForAllLocationData.kryo";
			// pathToSerialisedUniqueLocIDsInCleanedTimelines =
			// "./dataToRead/Mar30/DatabaseCreatedMerged/UniqueLocIDsInCleanedTimeines.ser";
			pathToSerialisedUniqueLocIDsInCleanedTimelines = "./dataToRead/July12/UniqueLocIDsInCleanedTimeines.kryo";

			pathToSerialisedLevelWiseCatIDsDict = "./dataToRead/May17/mapCatIDLevelWiseCatIDsDict.kryo";
			pathToSerialisedCatIDsHierDist = "./dataToRead/April7/mapCatIDsHierDist.kryo";
			pathToSerialisedUserObjects = "./dataToRead/Mar30/DatabaseCreatedMerged/mapForAllUserData.kryo";
			/**
			 * End of Gowalla path constants for before Aug 11 experiments
			 */
		}

	}
	// public final static String pathToSerialisedCatIDsHierDist;

}
