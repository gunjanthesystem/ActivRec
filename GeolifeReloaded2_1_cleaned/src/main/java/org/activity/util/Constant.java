package org.activity.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.distances.AlignmentBasedDistance;
import org.activity.generator.DatabaseCreatorGowallaQuicker0;
import org.activity.io.Serializer;
import org.activity.ui.PopUps;
import org.activity.ui.UIUtilityBox;

import weka.classifiers.Classifier;

/**
 * Stores the global constant use throughout the framework and thus all member variable and methods are static.
 * 
 * @author gunjan TODO: try to minimise or remove public access to variable to reduce of chances of accidental changes in state of global variables
 */
public final class Constant
{
	static String commonPath; // ALWAYS UPADTE THE CURRENT PATH TO THE CURRENT WORKING PATH
	
	// **** Parameters to set **** DO NOT CHANGE ****//
	// public static final boolean toSerializeJSONArray = false, toDeSerializeJSONArray = false, toCreateTimelines = false,
	// toSerializeTimelines = false, toDeSerializeTimelines = true;
	
	// **** Parameters to set for runtime performance **** DO NOT CHANGE ****//
	public static final boolean toSerializeJSONArray = false, toDeSerializeJSONArray = false, toCreateTimelines = true, // false,
			toSerializeTimelines = false, toDeSerializeTimelines = false;
	
	// **** Parameters to set **** DO NOT CHANGE ****//
	// public static final boolean toSerializeJSONArray = true, toDeSerializeJSONArray = true, toCreateTimelines = true,
	// toSerializeTimelines = true, toDeSerializeTimelines = true;
	// public static final boolean toSerializeJSONArray = true, toDeSerializeJSONArray = true, toCreateTimelines = true,
	// toSerializeTimelines = true,
	// toDeSerializeTimelines = true;
	
	public static String INVALID_ACTIVITY1 = "";// "Unknown";
	public static String INVALID_ACTIVITY2 = "";// "Not Available";
	
	static String[] activityNames;
	
	public static String DATABASE_NAME = "";// ;"geolife1";// default database name,
											// dcu_data_2";// "geolife1";// "start_base_2";databaseName
	public static String rankScoring = "";// "sum";// default product"; // "sum"
	public static String caseType = "";// CaseBasedV1";// default " CaseBasedV1 " or SimpleV3
	
	public static String howManyUsers = "UsersAbove10RTs";// "TenUsers";// "AllUsers" "UsersAbove10RTs"
	static double currentMatchingUnit = -99; // stores the current matching unit at all times, used for some sanity
												// checks
	/**
	 * This variable is not used for anything currently but just to write to console the type of matching
	 */
	static String typeOfTimelineMatching;// = "Daywise"; // N-count, N-hours
	/**
	 * ALPHA value for sum-based rank scoring
	 */
	public static double ALPHA = -99;// 0.25d;
	
	public static final double maxForNorm = 99999; // assuming unnormalised edit distance is never greater than this
	public static final double minForNorm = -99999; // assuming unnormalised edit distance is never lower than this.
													// note: in current form edit distance cannot
													// be negative
	
	public static final double distanceTravelledAlert = 200; // kms
	public static final double unknownDistanceTravelled = -9999;
	/**
	 * <p>
	 * <font color="red">Num of decimal digits to be kept in latitude and longitude. Latitude and longitude HAVE to be kept in database in decimal format and they HAVE to have
	 * atmost 6 decimal places.</font> This affects during hilbert space filled curve index for linearisation of geo coordinates. <b>This limits the precision of hilbert sace
	 * filling curve index</b>
	 * </p>
	 */
	public static final int decimalPlacesInGeocordinatesForComputations = 100000;// 1000000;
	/**
	 * Path to the folder were all the results will be stored
	 */
	public static String outputCoreResultsPath = "";
	
	// ////************* PARAMETERS TO BE SET ********************//////
	public static final boolean checkForDistanceTravelledAnomaly = false;
	public static final boolean checkForHaversineAnomaly = true; // false;
	/**
	 * Percentage/100 for training test split of dataset
	 */
	public static final double percentageInTraining = 0.8;// 0.8;
	
	/**
	 * whether there threshold should be applied on candidate timelines based on edit distance
	 */
	public static final boolean candidateThresholding = false; //
	public static final String[] typeOfThresholds = { "Global" };// Global"};//"Percent", "None"
	
	/**
	 * Determines if thresholding is used to eliminate candidate timelines beyond the threshold distance
	 */
	public static final boolean useThreshold = false;
	/**
	 * Determines whether tolerance is used for comparing features when calculating edit distance
	 * <p>
	 * Not sure whether i used tolerance in the geolife experiments or not. I think i was using it and then i was told to not use it, I don't remember what was the setting for
	 * final experiment. But it could be the case that I was using tolerance. I think I was.
	 * <p>
	 * But i don't want to waste time on investigating this now as I critically need to move my research "Forward"
	 */
	public static final boolean useTolerance = true;// false;
	/**
	 * Determines whether the sorting of candiates is stable or unstable
	 */
	public static final boolean breakTiesWithShuffle = true;
	/**
	 * Determines if connection pooling is used for database connections
	 */
	public static final boolean USEPOOLED = true;
	// public static boolean write = false; // public static boolean writeAllDayTimelinesPerUser = true;
	/**
	 * Controlling the verbosity of console log output
	 */
	public static boolean verbose = true;// false;
	public static final boolean verboseTimelineCleaning = false;// true;// false;// false; // verbosity level 2: if false it further
	// minimises verbosity
	public static final boolean verboseSAX = false;// false;
	public static boolean verboseLevenstein = true;// false;
	public static final boolean verboseNormalisation = false;
	// public static boolean debuggingMessageEditDistance = false;
	public static final boolean verboseHilbert = false;
	public static final boolean checkArrayOfFeatures = false;
	public static final boolean verboseOTMDSAM = false;
	public static final boolean verboseDistance = false;
	/**
	 * Whether to write the file EditDistancePerRtPerCand.csv (note: the files 'UserId'RecommTimesWithEditDistance.csv and EditDistancePerRtPerCand.csv have some similar
	 * information and have corresponding records.
	 */
	public static final boolean WriteEditDistancePerRtPerCand = false; //
	public static final boolean WriteCandInEditDistancePerRtPerCand = false;// false;
	public static boolean WriteNumActsPerRTPerCand = false; // this information is redundant as well
	public static final boolean WriteRedundant = false;
	public static final boolean WriteEditOperatationsInEditDistancePerRtPerCand = false;
	
	public static final boolean WriteEditSimilarityCalculations = true;// false;
	public static final boolean WriteActivityObjectsInEditSimilarityCalculations = false;
	
	public static final boolean WriteNormalisation = false;
	/**
	 * Write each cand of each RT in separate line
	 */
	public static final boolean WriteNormalisationsSeparateLines = false;
	/**
	 * Expunge the invalid Activity Objects even before the recommendation process starts
	 */
	public static final boolean EXPUNGE_INVALIDS_B4_RECOMM_PROCESS = true;// false;// true; NOT NEEDED IN CASE OF GOWALLA //TODO check the places where this is involved so that it
																			// can safely be set to false
	
	public static final boolean BLACKLISTING = false;// true;// true; // to have the same RTs in daywise and MU, some RTs in MU have to be blacklisted as they did not had any cand
														// timeline
														// in
														// daywise
	
	// Clustering0
	public static final int cluster1Min = 0, cluster1Max = 1, cluster2Min = 2, cluster2Max = 5, cluster3Min = 6,
			cluster3Max = Integer.MAX_VALUE;
	
	/**
	 * Most active users in the geolife dataset
	 */
	
	/**
	 * Note: 'userID' (ignore case) always refers to the raw user-ids. While index of user id refers to user 1, user 2,... or user 0, user 1, ... user-ids.
	 */
	public static final int tenUserIDsGeolifeData[] = { 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 };
	public static final int allUserIDsGeolifeData[] =
			{ 62, 84, 52, 68, 167, 179, 153, 85, 128, 10, 105, /* 78, */67, 126, 64, 111, 163, 98, 154, 125, 65, 80, 21, 69,
					/* 101, 175, */81, 96, 129, /* 115, */56, 91, 58, 82, 141, 112, 53, 139, 102, 20, 138, 108, 97, /* 92, 75, */
					161, 117, 170 /* ,114, 110, 107 */ };
	public static final int above10RTsUserIDsGeolifeData[] =
			{ 62, 84, 52, 68, 167, 179, 153, 85, 128, 10, 126, 111, 163, 65, 91, 82, 139, 108 };
	static int[] gowallaUserIDs = null;
	
	public static final int userIDsDCUData[] = { 0, 1, 2, 3, 4 };
	public static final String userNamesDCUData[] = { "Stefan", "Tengqi", "Cathal", "Zaher", "Rami" };
	
	static final String[] GeolifeActivityNames = { "Not Available", "Unknown", "airplane", "bike", "boat", "bus", "car", "motorcycle",
			"run", "subway", "taxi", "train", "walk" };
	
	static final String[] gowallaActivityNames = null;
	public static final int gowallaWorkingCatLevel = 2;
	
	// static final String[] GeolifeActivityNames = { "Not Available", "Unknown", /* "airplane", */"bike", /* "boat", */"bus", "car", /* "motorcycle", */
	/* "run", */// "subway", "taxi", "train", "walk" };
	
	static final String[] DCUDataActivityNames = { "Others", "Unknown", "Commuting", "Computer", "Eating", "Exercising", "Housework",
			"On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV" };
	
	public static final String lookPastType = "Count";// "Count";// "Hrs"
	/**
	 * Number of past activities to look excluding the current activity
	 */
	public static final double matchingUnitAsPastCount[] = { 0, 1, 2, 4, 6, 8, 10 };// { 0, 1, 2, 3, 4, 5, 6 };// , 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
																					// 24, 26, 28, 30 };// , 32,
																					// 34, 36, 38, 40, 42 };
	
	public static final double matchingUnitHrsArray[] = { 0.5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
			22, 23, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42 };
	// public static final double matchingUnitHrsArray[] = { 24, 26, 28, 30, 32, 34, 36, 38, 40, 42 };
	
	public static final int SAXStartTimeAlphabsetSize = 20; // maximum possible aplhabet size for new alphabet
															// implementation in the sax code
	public static final int SAXDurationAlphabsetSize = 20;
	
	public static final int SAXDistanceTravelledAlphabsetSize = 20; // 12;
	public static final int SAXAvgAltitudeAlphabsetSize = 20;// 12;
	
	public static String distanceUsed = "HJEditDistance"; // "FeatureWiseEditDistance",FeatureWiseEditDistance,
															// OTMDSAMEditDistance
	
	static TimeZone timeZoneForExperiments = null;
	
	public static void setDefaultTimeZone(String timeZoneString)
	{
		timeZoneForExperiments = TimeZone.getTimeZone(timeZoneString);
		TimeZone.setDefault(timeZoneForExperiments);
	}
	
	public static TimeZone getTimeZone()
	{
		return timeZoneForExperiments;
	}
	
	/**
	 * Determines if the given matching unit array has any value in fractions. </br>
	 * Useful to ensure no damage done if converting double to integer
	 * 
	 * @param matchingUnitsArrayUsed
	 * @return
	 */
	public static boolean isFractionsInMatchingUnits(double matchingUnitsArrayUsed[])
	{
		// boolean flag = true;
		
		for (double mu : matchingUnitsArrayUsed)
		{
			if (mu % 1 != 0)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Whether to consider all features while calculating feature wise edit distance
	 */
	public static boolean considerAllFeaturesForFeatureWiseEditDistance = false;// true;
	
	/**
	 * If all features are not to be used for feature wise edit distance, then choose which features are to be used.
	 */
	public static boolean considerActivityNameInFeatureWiseEditDistance = false, considerStartTimeInFeatureWiseEditDistance = false,
			considerDurationInFeatureWiseEditDistance = false, considerDistanceTravelledInFeatureWiseEditDistance = false,
			considerStartGeoCoordinatesInFeatureWiseEditDistance = false, considerEndGeoCoordinatesInFeatureWiseEditDistance = false,
			considerAvgAltitudeInFeatureWiseEditDistance = false;
	
	public static String[] featureNames =
			{ "ActivityName", "StartTime", "Duration", "DistanceTravelled", "StartGeoCoordinates", "EndGeoCoordinates", "AvgAltitude" };
	
	// ///************* END OF PARAMETERS TO BE SET ********************//////
	public static Classifier classifierUsed;
	
	/**
	 * Don't let anyone instantiate this class.
	 */
	private Constant()
	{
	}
	
	// /**
	// * note: all variables are static for this class
	// *
	// * @param givenCommonpath
	// */
	// public Constant(String givenCommonpath, String databaseName)
	// {
	// Constant.setDatabaseName(databaseName);
	// Constant.setUserIDs();
	// Constant.setInvalidNames();
	// Constant.setActivityNames();
	// Constant.setCommonPath(givenCommonpath);
	// // Constant.setDistanceUsed("HJEditDistance");
	// }
	//
	// public Constant(String givenCommonpath, String databaseName, String distUsed)
	// {
	// Constant.setDatabaseName(databaseName);
	// Constant.setUserIDs();
	// Constant.setInvalidNames();
	// Constant.setActivityNames();
	// Constant.setCommonPath(givenCommonpath);
	// // Constant.setDistanceUsed(distUsed);
	// }
	
	// /////////
	/**
	 * note: all variables are static for this class
	 * 
	 * @param givenCommonpath
	 */
	/**
	 * setDatabaseName setUserIDs setInvalidNames setActivityNames setCommonPath
	 * 
	 * @param givenCommonpath
	 * @param databaseName
	 */
	public static void initialise(String givenCommonpath, String databaseName)
	{
		Constant.setDatabaseName(databaseName);
		Constant.setUserIDs();
		Constant.setInvalidNames();
		Constant.setActivityNames();
		Constant.setCommonPath(givenCommonpath);
		// Constant.setDistanceUsed("HJEditDistance");
	}
	
	// /**
	// * Seems its not being used 26 Jab 2016)
	// * @param givenCommonpath
	// * @param databaseName
	// * @param distUsed
	// */
	// public static void initialise(String givenCommonpath, String databaseName, String distUsed)
	// {
	// Constant.setDatabaseName(databaseName);
	// Constant.setUserIDs();
	// Constant.setInvalidNames();
	// Constant.setActivityNames();
	// Constant.setCommonPath(givenCommonpath);
	// // Constant.setDistanceUsed(distUsed);
	// }
	
	// /////////
	
	public static Classifier getClassifierUsed()
	{
		return classifierUsed;
	}
	
	public static void setClassifier(Classifier classifier)
	{
		classifierUsed = classifier;
	}
	
	/**
	 * 
	 * @return
	 */
	public static double getCurrentMatchingUnit()
	{
		return currentMatchingUnit;
	}
	
	/**
	 * 
	 * @param currentMatchingUnit
	 */
	public static void setCurrentMatchingUnit(double currentMatchingUnit)
	{
		Constant.currentMatchingUnit = currentMatchingUnit;
	}
	
	/**
	 * set the features to be considered for feature wise edit distance
	 * 
	 * @param activityName
	 * @param startTime
	 * @param duration
	 * @param distanceTravelled
	 * @param startGeo
	 * @param endGeo
	 * @param avgAltitude
	 */
	public static void setFeatureToConsiderForFeatureWiseEditDistance(boolean activityName, boolean startTime, boolean duration,
			boolean distanceTravelled, boolean startGeo, boolean endGeo, boolean avgAltitude)
	{
		considerActivityNameInFeatureWiseEditDistance = activityName;
		considerStartTimeInFeatureWiseEditDistance = startTime;
		considerDurationInFeatureWiseEditDistance = duration;
		considerDistanceTravelledInFeatureWiseEditDistance = distanceTravelled;
		considerStartGeoCoordinatesInFeatureWiseEditDistance = startGeo;
		considerEndGeoCoordinatesInFeatureWiseEditDistance = endGeo;
		considerAvgAltitudeInFeatureWiseEditDistance = avgAltitude;
	}
	
	public static int userIDs[];
	
	// public static void setWriteAllDayTimelinesPerUser(boolean value)
	// {
	// writeAllDayTimelinesPerUser = value;
	// }
	// removeDayTimelinesWithOneOrLessDistinctValidAct
	
	public static void setDistanceUsed(String dname) // throws Exception
	{
		dname = dname.trim();
		
		switch (dname)
		{
			case "HJEditDistance":
				distanceUsed = "HJEditDistance";
				break;
			case "FeatureWiseEditDistance":
				distanceUsed = "FeatureWiseEditDistance";
				break;
			
			case "FeatureWiseWeightedEditDistance":
				distanceUsed = "FeatureWiseWeightedEditDistance";
				break;
			
			case "OTMDSAMEditDistance":
				distanceUsed = "OTMDSAMEditDistance";
				break;
			default:
				PopUps.showError("Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:" + dname);
				System.err.println("Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:" + dname);
				// throw new
				// Exception("Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:"
				// + dname);
				System.exit(-1);
		}
		// if(dname.trim().equals("HJEditDistance")
	}
	
	/**
	 * Set the type of timeline matching. The currently supported types of timeline matching are: Daywise, N-count and N-hours
	 * 
	 * @param name
	 */
	public static void setTypeOfTimelineMatching(String name)
	{
		name = name.trim();
		
		if (name != "Daywise" && name != "N-count" && name != "N-hours")
		{
			String msg =
					"Error in org.activity.util.Constant.setTypeOfTimelineMatching(String): unknown type of time line matching:" + name;
			PopUps.showError(msg);
			System.err.println(msg);
			
		}
		else
		{
			typeOfTimelineMatching = name;
		}
	}
	
	/**
	 * Returns the type of timeline matching
	 * 
	 * @return
	 */
	public static String getTypeOfTimelineMatching()
	{
		return typeOfTimelineMatching;
	}
	
	public static String getDistanceUsed()
	{
		return distanceUsed;
	}
	
	/**
	 * Sets the common path for the whole application: this is the current path for reading and writing for data.
	 * 
	 * @param commonpath
	 */
	public static void setCommonPath(String commonpath)
	{
		commonPath = commonpath;// "/home/gunjan/MATLAB/bin/DCU data works/July20/New_10_Aug/";
	}// public static final String dataLoaderPath="/home/gunjan/MATLAB/bin/DCU data works/July20/New_10_Aug/";
	
	public static String getCommonPath()
	{
		return commonPath;
	}
	
	public static String getInvalidActivity1()
	{
		return INVALID_ACTIVITY1;
	}
	
	public static String getInvalidActivity2()
	{
		return INVALID_ACTIVITY2;
	}
	
	public static void setVerbose(boolean value)
	{
		verbose = value;
	}
	
	public static boolean getVerbose()
	{
		return verbose;
	}
	
	public static void setWriteNumActsmatchingUnit(boolean value)
	{
		WriteNumActsPerRTPerCand = value;
	}
	
	/**
	 * <font color = red>not yet set for gowalla </font>
	 */
	public static void setUserIDs()
	{
		try
		{
			switch (DATABASE_NAME)
			{
				case "geolife1":
					switch (Constant.howManyUsers)
					{
						case "AllUsers":
							userIDs = allUserIDsGeolifeData;
							break;
						case "TenUsers":
							userIDs = tenUserIDsGeolifeData;
							break;
						case "UsersAbove10RTs":
							userIDs = above10RTsUserIDsGeolifeData;
							break;
						default:
							UtilityBelt.showErrorExceptionPopup("unknown Constant.howManyUsers =" + Constant.howManyUsers);
							break;
					}
					// userIDs = userIDsGeolifeData;
					break;
				case "dcu_data_2":
					userIDs = userIDsDCUData;
					break;
				case "gowalla1":
					userIDs = gowallaUserIDs;
					break;
				default:
					System.out.println(DATABASE_NAME.equals("dcu_data_2"));
					System.err.println("Error in setUserIDs: unrecognised database name:" + DATABASE_NAME);
					throw new Exception();
					// break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param rawUserID
	 * @return
	 */
	public static int getIndexOfUserID(int rawUserID)
	{
		for (int i = 0; i < userIDs.length; i++)
		{
			if (userIDs[i] == rawUserID)
			{
				return i;
			}
		}
		return 99;
	}
	
	public static void setInvalidNames()
	{
		try
		{
			switch (DATABASE_NAME)
			{
				case "geolife1":
					INVALID_ACTIVITY1 = "Unknown";
					INVALID_ACTIVITY2 = "Not Available";
					break;
				case "dcu_data_2":
					INVALID_ACTIVITY1 = "Unknown";
					INVALID_ACTIVITY2 = "Others";
					break;
				
				case "gowalla1":
					INVALID_ACTIVITY1 = "Unknown";
					INVALID_ACTIVITY2 = "Not Available";
					break;
				
				default:
					System.err.println("Error in setInvalidNames: unrecognised database name:" + DATABASE_NAME);
					throw new Exception();
					// break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void setActivityNames()
	{
		try
		{
			switch (DATABASE_NAME)
			{
				case "geolife1":
					activityNames = GeolifeActivityNames;
					break;
				case "dcu_data_2":
					activityNames = DCUDataActivityNames;
					break;
				case "gowalla1":
					DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
							.deSerializeThis(DatabaseCreatorGowallaQuicker0.categoryHierarchyTreeFileName);
					LinkedHashSet<String> res = UIUtilityBox.getNodesAtGivenDepth(gowallaWorkingCatLevel, rootOfCategoryTree);
					System.out.println("num of nodes at depth " + gowallaWorkingCatLevel + " are: " + res.size());
					activityNames = res.toArray(new String[res.size()]);
					// gowallaActivityNames;
					break;
				default:
					System.err.println("Error: in setActivityNames: unrecognised database name:" + DATABASE_NAME);
					throw new Exception();
					// break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static int getNumberOfFeatures()
	{
		
		switch (DATABASE_NAME)
		{
			case "geolife1":
				return 7;// or 5 //activity name, start time, duration, dist travelled, start geo, end geo, avg altitude
			// break;
			case "dcu_data_2":
				return 3; // activity name, start time, duration
			// break;
			default:
				System.err.println("Error: in setActivityNames: unrecognised database name:" + DATABASE_NAME);
				return -1;
		}
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static String[] getFeatureNames()
	{
		if ((Constant.getDatabaseName().equals("geolife1") || Constant.getDatabaseName().equals("dcu_data_2")) == false)
		{
			PopUps.showError("Error in getFeaturesName: unreliable for databases other than geolife and dcu, current database name ="
					+ Constant.getDatabaseName());
			new Exception("Error in getFeaturesName: unreliable for databases other than geolife and dcu, current database name ="
					+ Constant.getDatabaseName()).printStackTrace();
		}
		
		int num = getNumberOfFeatures();
		String featureNamesSub[] = new String[num];
		
		for (int i = 0; i < num; i++)
		{
			featureNamesSub[i] = featureNames[i];
		}
		return featureNamesSub;// or 5 //activity name, start time, duration, dist travelled, start geo, end geo, avg
								// altitude
	}
	
	public static String[] getActivityNames()
	{
		if (activityNames.length == 0)
		{
			System.err.println("Error: in Constant.getActvitityNames(): activity names not set");
		}
		
		return activityNames;
	}
	
	public static boolean areActivityNamesInCorrectOrder(LinkedHashMap<String, ?> toTest)
	{
		// System.out.println("Inside areActivityNamesInCorrectOrder");
		boolean res = true;
		
		int i = 0;
		
		for (Map.Entry<String, ?> entry : toTest.entrySet())
		{
			while ((activityNames[i].equals(INVALID_ACTIVITY1) || activityNames[i].equals(INVALID_ACTIVITY2))
					&& i < (activityNames.length - 1))
			{
				i++;
			}
			
			// System.out.println("Map entry= " + entry.getKey() + " Constant.activitynames = " + activityNames[i]);
			if (entry.getKey().equals(activityNames[i]) == false)
			{
				// System.out.println("return false");
				return false;
			}
			i += 1;
		}
		// System.out.println("Exiting areActivityNamesInCorrectOrder");
		return res;
	}
	
	public static int[] getUserIDs()
	{
		if (userIDs != null)
		{
			return userIDs;
		}
		else
		{
			System.err.println("Alert! in Constant.getUserIDs: userIDs is NULL"); // user ids are not set set beforehand in some case such as for gowalla dataset
			return null;
		}
		
	}
	
	public static String[] getStringUserIDs()
	{
		if (userIDs != null)
		{
			Stream.of(userIDs).map(u -> u.toString()).toArray();
			return null;// userIDs;
		}
		else
		{
			System.err.println("Error in Constant.getUserIDs: userIDs is NULL");
			return null;
		}
		
	}
	
	/**
	 * With given number of digits (with added preceeding zeros)
	 * 
	 * @param numOfDigits
	 * @return
	 */
	public static String[] getStringUserIDs(int numOfDigits)
	{
		if (userIDs != null)
		{
			// Stream.of(userIDs).map(u -> addPreceedingZeros(u, numOfDigits)).toArray();
			String[] res = new String[userIDs.length];
			
			int count = 0;
			for (int userID : userIDs)
			{
				res[count] = addPreceedingZeros(String.valueOf(userID), numOfDigits);
			}
			
			return res;// userIDs;
		}
		else
		{
			System.err.println("Error in Constant.getUserIDs: userIDs is NULL");
			return null;
		}
		
	}
	
	public static String addPreceedingZeros(String s, int len)
	{
		String r = null;
		int diff = len - s.length();
		String preceeding = new String();
		if (diff > 0)
		{
			for (int j = 1; j <= len; j++)
			{
				preceeding = "0" + preceeding;
			}
		}
		return (preceeding + s);
	}
	
	public static void setDatabaseName(String dname)
	{
		DATABASE_NAME = dname;
	}
	
	public static String getDatabaseName()
	{
		return DATABASE_NAME;
	}
	
	public static void setRankScoring(String rankscoring)
	{
		rankScoring = rankscoring;
	}
	
	public static String getRankScoring()
	{
		return rankScoring;
	}
	
	/**
	 * 
	 * @param databaseName
	 * @return
	 */
	public static boolean hasGeoCoordinates(String databaseName)
	{
		if (databaseName.equals("geolife1"))
			return true;
		else
			return false;
	}
	
	public static String getAllGlobalConstants()
	{
		StringBuffer s = new StringBuffer();
		
		s.append("~~~~~~~~~~~~~~~~~~ALL GLOBAL CONSTANT~~~~~~~~~~~~~~~~~~~\nDatabase used:" + DATABASE_NAME);
		s.append("\nINVALID ACTIVITY 1:" + INVALID_ACTIVITY1);
		s.append("\nINVALID ACTIVITY 2:" + INVALID_ACTIVITY2);
		s.append("\nCommon path:" + commonPath);
		s.append("\nType of timeline matching:" + typeOfTimelineMatching);
		s.append("\nLook past type:" + lookPastType);
		s.append("\nactivityNames:" + Arrays.toString(activityNames));
		s.append("\nUsing Tolernace:" + useTolerance);
		s.append("\nBreaking tied with Shuffle:" + breakTiesWithShuffle);
		s.append("\nCase Type:" + caseType);
		s.append("\nRank scoring method: " + rankScoring);
		s.append("\nBETA value:" + ALPHA);
		s.append("\nEXPUNGE_INVALIDS_B4_RECOMM_PROCESS:" + EXPUNGE_INVALIDS_B4_RECOMM_PROCESS);
		s.append("\nWeights of features: " + (new AlignmentBasedDistance()).getAllWeightsOfFeatures());
		s.append("\ndistanceUsed:" + distanceUsed);
		s.append("\nIs blacklisting:" + BLACKLISTING);
		
		if (distanceUsed.equals("FeatureWiseEditDistance"))
		{
			s.append("\nConsider all features for feature wise edit distance:" + considerAllFeaturesForFeatureWiseEditDistance);
			
			if (considerAllFeaturesForFeatureWiseEditDistance == false)
			{
				s.append("\nconsiderActivityNameInFeatureWiseEditDistance:" + considerActivityNameInFeatureWiseEditDistance);
				s.append("\nconsiderStartTimeInFeatureWiseEditDistance:" + considerStartTimeInFeatureWiseEditDistance);
				s.append("\nconsiderDurationInFeatureWiseEditDistance:" + considerDurationInFeatureWiseEditDistance);
				s.append("\nconsiderDistanceTravelledInFeatureWiseEditDistance:" + considerDistanceTravelledInFeatureWiseEditDistance);
				s.append("\nconsiderStartGeoCordinatesInFeatureWiseEditDistance:" + considerStartGeoCoordinatesInFeatureWiseEditDistance);
				s.append("\nconsiderEndGeoCordinatesInFeatureWiseEditDistance:" + considerEndGeoCoordinatesInFeatureWiseEditDistance);
				s.append("\nconsiderAvgAltitudeInFeatureWiseEditDistance:" + considerAvgAltitudeInFeatureWiseEditDistance);
			}
		}
		s.append("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		return s.toString();
	}
	
	/**
	 * Checks if all core parameters (database name, , case type and alpha) are set
	 * 
	 * @return
	 */
	public static boolean checkAllParametersSet()
	{
		boolean allSet = true;
		if (DATABASE_NAME.length() == 0)
		{
			System.err.println("Database name is empty");
			allSet = false;
		}
		// if (rankScoring.length() == 0)
		// {
		// System.err.println("rankScoring name is empty");
		// allSet = false;
		// }
		if (caseType.length() == 0)
		{
			System.err.println("caseType name is empty");
			allSet = false;
		}
		if (ALPHA < 0 && rankScoring.equals("sum"))
		{
			System.err.println("ALPHA  is less than 0 (not set) for Sum-based Rank Scoring");
			allSet = false;
		}
		return allSet;
	}
}
