package org.activity.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.distances.AlignmentBasedDistance;
import org.activity.generator.DatabaseCreatorGowallaQuicker0;
import org.activity.io.Serializer;
import org.activity.objects.TraceMatrix;
import org.activity.ui.PopUps;
import org.activity.ui.UIUtilityBox;
import org.activity.util.UtilityBelt;

import weka.classifiers.Classifier;

/**
 * Stores the global constant use throughout the framework and thus all member variable and methods are static.
 * 
 * @author gunjan TODO: try to minimise or remove public access to variable to reduce of chances of accidental changes
 *         in state of global variables
 */
public final class Constant
{
	static String commonPath; // ALWAYS UPADTE THE CURRENT PATH TO THE CURRENT WORKING PATH

	// **** Parameters to set **** DO NOT CHANGE ****//
	public static final boolean toSerializeJSONArray = false, toDeSerializeJSONArray = false, toCreateTimelines = true, // false,
			toSerializeTimelines = false, toDeSerializeTimelines = false;

	public static String INVALID_ACTIVITY1 = "";// "Unknown";
	public static String INVALID_ACTIVITY2 = "";// "Not Available";

	static String[] activityNames;
	public static ArrayList<String> activityNamesGowallaLabels;
	static String DATABASE_NAME = "";// ;"geolife1";// default database name,
										// dcu_data_2";// "geolife1";// "start_base_2";databaseName
	public static String rankScoring = "";// "sum";// default product"; // "sum"
	public static final Enums.CaseType caseType = Enums.CaseType.SimpleV3;// null;// String caseType CaseBasedV1";//
																			// default "
	// CaseBasedV1 " or SimpleV3

	public static String howManyUsers = "AllUsers";// "TenUsers";// "AllUsers" "UsersAbove10RTs"
	static double currentMatchingUnit = -99; // stores the current matching unit at all times, used for some
												// sanity checks
	// Redudant since we have lookPastType/**
	// * This variable is not used for anything currently but just to write to console the type of matching
	// */
	// static String typeOfTimelineMatching;// = "Daywise"; // N-count, N-hours
	/**
	 * ALPHA value for sum-based rank scoring
	 */
	public static double ALPHA = -99;// 0.25d;

	public static final double maxForNorm = 9999999; // assuming unnormalised edit distance is never greater than this
	public static final double minForNorm = -9999999; // assuming unnormalised edit distance is never lower than this.
														// note: in current form edit distance cannot
														// be negative

	public static final double distanceTravelledAlert = 200; // kms
	public static final double unknownDistanceTravelled = -9999;
	/**
	 * <p>
	 * <font color="red">Num of decimal digits to be kept in latitude and longitude. Latitude and longitude HAVE to be
	 * kept in database in decimal format and they HAVE to have atmost 6 decimal places.</font> This affects during
	 * hilbert space filled curve index for linearisation of geo coordinates. <b>This limits the precision of hilbert
	 * sace filling curve index</b>
	 * </p>
	 */
	public static final int decimalPlacesInGeocordinatesForComputations = 100000;// 1000000;
	/**
	 * Path to the folder were all the results will be stored
	 */
	public static String outputCoreResultsPath = "";

	// ////************* PARAMETERS TO BE SET ********************//////
	public static final boolean hasInvalidActivityNames = false;
	public static final boolean removeCurrentActivityNameFromRecommendations = false;// true;

	public static final boolean checkForDistanceTravelledAnomaly = false;
	public static final boolean checkForHaversineAnomaly = true; // false;
	public static final boolean checkArrayOfFeatures = false;
	/**
	 * Can be disabled for better performance for subsequent runs if previous run of experiments shows that timelines
	 * created had no chronological anomaly
	 */
	public static final boolean checkIfTimelineCreatedIsChronological = true;// false;

	/**
	 * Percentage/100 for training test split of dataset
	 */
	public static final double percentageInTraining = 0.8;// 0.8;

	/**
	 * whether there threshold should be applied on candidate timelines based on edit distance
	 */
	// public static final boolean candidateThresholding = false; //
	public static final Enums.TypeOfThreshold[] typeOfThresholds = { Enums.TypeOfThreshold.Global };
	// String[] typeOfThresholds = { "Global" };// Global"};//"Percent", "None"

	/**
	 * Determines if thresholding is used to eliminate candidate timelines beyond the threshold distance
	 */
	public static final boolean useThreshold = false;

	/**
	 * Determines whether tolerance is used for comparing features when calculating edit distance
	 * <p>
	 * Not sure whether i used tolerance in the geolife experiments or not. I think i was using it and then i was told
	 * to not use it, I don't remember what was the setting for final experiment. But it could be the case that I was
	 * using tolerance. I think I was.
	 * <p>
	 * But i don't want to waste time on investigating this now as I critically need to move my research "Forward"
	 */
	public static final boolean useTolerance = true;// false;

	public static final boolean useHierarchicalDistance = false;// true;
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
	 * Expunge the invalid Activity Objects even before the recommendation process starts
	 */
	public static final boolean EXPUNGE_INVALIDS_B4_RECOMM_PROCESS = true;// false;// true;
	// NOT NEEDED IN CASE OF GOWALLA //TODO check the places where this is involved so that it can safely be set to
	// false

	/**
	 * to have the same RTs in daywise and MU, some RTs in MU have to be blacklisted as they did not had any cand
	 * timeline in daywise
	 */
	public static final boolean BLACKLISTING = false;// true;// true;

	public static final boolean blacklistingUsersWithLargeMaxActsPerDay = true;

	public static final boolean DoBaselineDuration = false, DoBaselineOccurrence = false, DoBaselineNGramSeq = true;

	public static final Enums.LookPastType lookPastType = Enums.LookPastType.ClosestTime;// .NGram;// .Daywise;//
																							// NCount;//
	// Enums.LookPastType.NCount;
	// "Count";// "Count";// "Hrs"// "Daywise"
	/**
	 * determines if current timeline is allowed to go beyod the day boundaries, note that until the KDD paper, we were
	 * restricting this baseline to day boundaries
	 */
	public static final boolean ClosestTimeAllowSpillOverDays = true;
	/**
	 * Determines if candidate timelines only includes those days which contain the current activity name
	 */
	public static final boolean ClosestTimeFilterCandidates = false;

	public static final Enums.EditDistanceTimeDistanceType editDistTimeDistType =
			Enums.EditDistanceTimeDistanceType.NearerScaled;
	// .FurtherScaled;

	/**
	 * Number of past activities to look excluding the current activity
	 */
	public static final double matchingUnitAsPastCount[] = { 0, 1, 2, 3, 4, 6, 8, 10, 12 };// , 14, 16, 18 };
	// { 0, 1, 2, 3,// 4, 5, 6 };//// , 7, 8, 9,//// 10, 11, 12,// 13, 14, 15,// 16,// 17, 18, 19, 20, 21, 22, 23, 24,
	// 26, 28, 30 };// , 32,// 34, 36, 38, 40, 42 };

	public static final double matchingUnitHrsArray[] = { 0.5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
			17, 18, 19, 20, 21, 22, 23, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42 };

	public static final double matchingDummy[] = { -1 };
	// public static final double matchingUnitHrsArray[] = { 24, 26, 28, 30, 32, 34, 36, 38, 40, 42 };

	public static String distanceUsed = "HJEditDistance"; // "FeatureWiseEditDistance",FeatureWiseEditDistance,
															// OTMDSAMEditDistance

	public static boolean useJarForMySimpleLevenshteinDistance = false;// true;

	/****** Evaluation Constants Start ***********/
	public static final boolean EvalPrecisionRecallFMeasure = false;// true;// false;
	/****** Evaluation Constants End ***********/

	static TimeZone timeZoneForExperiments = null;

	public static final int RoundingPrecision = 4;

	public static TraceMatrix reusableTraceMatrix;

	/**
	 * determines the hierarchical level of the activity name to be used in edit distance computation
	 */
	public static final int HierarchicalLevelForEditDistance = 1;// 2, -1 when not used

	////////////////////////////////////////////////////////////////////////

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
	public static boolean considerActivityNameInFeatureWiseEditDistance = false,
			considerStartTimeInFeatureWiseEditDistance = false, considerDurationInFeatureWiseEditDistance = false,
			considerDistanceTravelledInFeatureWiseEditDistance = false,
			considerStartGeoCoordinatesInFeatureWiseEditDistance = false,
			considerEndGeoCoordinatesInFeatureWiseEditDistance = false,
			considerAvgAltitudeInFeatureWiseEditDistance = false;

	public static boolean UsingSQLDatabase;

	/**
	 * Don't let anyone instantiate this class.
	 */
	private Constant()
	{
	}

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
		Constant.UsingSQLDatabase = false;
		Constant.setUserIDs();
		Constant.setInvalidNames();
		Constant.setActivityNames();
		Constant.setCommonPath(givenCommonpath);

		// Constant.setDistanceUsed("HJEditDistance");
	}

	/**
	 * 
	 * @param givenCommonpath
	 * @param databaseName
	 * @param catIDsHierDistSerialisedFile
	 * @param pathToSerialisedCatIDNameDictionary
	 */
	public static void initialise(String givenCommonpath, String databaseName, String catIDsHierDistSerialisedFile,
			String pathToSerialisedCatIDNameDictionary, String pathToSerialisedLocationObjects)
	{
		Constant.setDatabaseName(databaseName);
		Constant.UsingSQLDatabase = false;
		Constant.setUserIDs();
		Constant.setInvalidNames();
		Constant.setActivityNames();
		Constant.setCommonPath(givenCommonpath);
		DomainConstants.setCatIDsHierarchicalDistance(catIDsHierDistSerialisedFile);
		DomainConstants.setCatIDNameDictionary(pathToSerialisedCatIDNameDictionary);
		DomainConstants.setLocIDLocationObjectDictionary(pathToSerialisedLocationObjects);
		DomainConstants.setCatIDCharCodeMap();
		DomainConstants.setCatIDGivenLevelCatIDMap();
		// Constant.setDistanceUsed("HJEditDistance");
	}

	//

	/**
	 * setDatabaseName setUserIDs setInvalidNames setActivityNames setCommonPath setReusableTraceMatrix
	 * 
	 * 
	 * @param givenCommonpath
	 * @param databaseName
	 * @param maxActsInDay
	 */
	public static void initialise(String givenCommonpath, String databaseName, int maxActsInDay)
	{
		Constant.setDatabaseName(databaseName);
		Constant.UsingSQLDatabase = false;
		Constant.setUserIDs();
		Constant.setInvalidNames();
		Constant.setActivityNames();
		Constant.setCommonPath(givenCommonpath);
		Constant.setReusableTraceMatrix(maxActsInDay, maxActsInDay);
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
		return ClusteringConstants.classifierUsed;
	}

	public static void setClassifier(Classifier classifier)
	{
		ClusteringConstants.classifierUsed = classifier;
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
	public static void setFeatureToConsiderForFeatureWiseEditDistance(boolean activityName, boolean startTime,
			boolean duration, boolean distanceTravelled, boolean startGeo, boolean endGeo, boolean avgAltitude)
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
			PopUps.showError(
					"Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:" + dname);
			System.err.println(
					"Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:" + dname);
			// throw new
			// Exception("Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:"
			// + dname);
			System.exit(-1);
		}
		// if(dname.trim().equals("HJEditDistance")
	}

	// /**
	// * Set the type of timeline matching. The currently supported types of timeline matching are: Daywise, N-count and
	// * N-hours
	// *
	// * @param name
	// */
	// public static void setTypeOfTimelineMatching(String name)
	// {
	// name = name.trim();
	//
	// if (name != "Daywise" && name != "N-count" && name != "N-hours")
	// {
	// String msg = "Error in org.activity.util.Constant.setTypeOfTimelineMatching(String): unknown type of time line
	// matching:"
	// + name;
	// PopUps.showError(msg);
	// System.err.println(msg);
	//
	// }
	// else
	// {
	// typeOfTimelineMatching = name;
	// }
	// }

	// /**
	// * Returns the type of timeline matching
	// *
	// * @return
	// */
	// public static String getTypeOfTimelineMatching()
	// {
	// return typeOfTimelineMatching;
	// }

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

	public static void setReusableTraceMatrix(int wordLen1, int wordLen2)
	{
		reusableTraceMatrix = new TraceMatrix(wordLen1, wordLen2);
	}

	public static String getCommonPath()
	{
		return commonPath;
	}

	public static String cleanedDayTimelines()
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

	// public static void setVerbose(boolean value)
	// {
	// VerbosityConstants.verbose = value;
	// }

	public static boolean getVerbose()
	{
		return VerbosityConstants.verbose;
	}

	public static void setWriteNumActsmatchingUnit(boolean value)
	{
		VerbosityConstants.WriteNumActsPerRTPerCand = value;
	}

	/**
	 * <font color = red>for gowalla </font>
	 */
	public static void setUserIDs(int givenUserIDs[])
	{
		userIDs = givenUserIDs;
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
					userIDs = DomainConstants.allUserIDsGeolifeData;
					break;
				case "TenUsers":
					userIDs = DomainConstants.tenUserIDsGeolifeData;
					break;
				case "UsersAbove10RTs":
					userIDs = DomainConstants.above10RTsUserIDsGeolifeData;
					break;
				default:
					UtilityBelt.showErrorExceptionPopup("unknown Constant.howManyUsers =" + Constant.howManyUsers);
					break;
				}
				// userIDs = userIDsGeolifeData;
				break;
			case "dcu_data_2":
				userIDs = DomainConstants.userIDsDCUData;
				break;
			case "gowalla1":
				userIDs = DomainConstants.gowallaUserIDs;
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
				activityNames = DomainConstants.GeolifeActivityNames;
				break;
			case "dcu_data_2":
				activityNames = DomainConstants.DCUDataActivityNames;
				break;
			case "gowalla1":
				DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
						.deSerializeThis(DatabaseCreatorGowallaQuicker0.categoryHierarchyTreeFileName);
				LinkedHashSet<String> res =
						UIUtilityBox.getNodesAtGivenDepth(DomainConstants.gowallaWorkingCatLevel, rootOfCategoryTree);
				System.out.println(
						"num of nodes at depth " + DomainConstants.gowallaWorkingCatLevel + " are: " + res.size());
				activityNames = res.toArray(new String[res.size()]);

				// StringBuilder sb = new StringBuilder();
				System.out.println(Arrays.asList(activityNames).stream().collect(Collectors.joining(",")));

				// activityNamesGowallaLabels = (ArrayList<String>) Arrays.asList(activityNames).stream()
				// .map(a -> DomainConstants.catIDNameDictionary.get(a)).collect(Collectors.toList());
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
		case "gowalla1":
			return 2; // activity name, start time
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
		if ((Constant.getDatabaseName().equals("geolife1") || Constant.getDatabaseName().equals("dcu_data_2")
				|| Constant.getDatabaseName().equals("gowalla1")) == false)
		{
			PopUps.showError(
					"Error in getFeaturesName: unreliable for databases other than geolife and dcu, current database name ="
							+ Constant.getDatabaseName());
			new Exception(
					"Error in getFeaturesName: unreliable for databases other than geolife and dcu, current database name ="
							+ Constant.getDatabaseName()).printStackTrace();
		}

		int num = getNumberOfFeatures();
		String featureNamesSub[] = new String[num];

		for (int i = 0; i < num; i++)
		{
			if (Constant.getDatabaseName().equals("gowalla1"))
			{
				featureNamesSub[i] = DomainConstants.gowallaFeatureNames[i];
			}
			else
			{
				featureNamesSub[i] = DomainConstants.featureNames[i];
			}
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
			System.err.println("Alert! in Constant.getUserIDs: userIDs is NULL"); // user ids are not set set beforehand
																					// in some case such as for gowalla
																					// dataset
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

	/**
	 * 
	 * @return
	 */
	public static String getAllGlobalConstants()
	{
		StringBuilder s = new StringBuilder();

		s.append("~~~~~~~~~~~~~~~~~~ALL GLOBAL CONSTANTS~~~~~~~~~~~~~~~~~~~\nDatabase used:" + DATABASE_NAME);
		s.append("\ntimeZoneForExperiments:" + timeZoneForExperiments.toString());
		s.append("\nINVALID ACTIVITY 1:" + INVALID_ACTIVITY1);
		s.append("\nINVALID ACTIVITY 2:" + INVALID_ACTIVITY2);
		s.append("\nCommon path:" + commonPath);
		s.append("\npercentageInTraining:" + percentageInTraining);
		s.append("\nactivityNames:" + Arrays.toString(activityNames));

		s.append("\nlookPastType:" + lookPastType);
		s.append("\nClosestTimeAllowSpillOverDays:" + ClosestTimeAllowSpillOverDays);
		s.append("\nClosestTimeFilterCandidates:" + ClosestTimeFilterCandidates);
		s.append("\ncaseType:" + caseType);
		s.append("\nrankScoring: " + rankScoring);
		s.append("\nALPHA:" + ALPHA);
		s.append("\nWeights of features: " + (new AlignmentBasedDistance()).getAllWeightsOfFeatures());
		s.append("\ndistanceUsed:" + distanceUsed);

		s.append("\nuseTolerance:" + useTolerance);
		s.append("\ntypeOfThresholds:" + Arrays.asList(typeOfThresholds));
		// s.append("\nuseThreshold:" + useThreshold);
		s.append("\nbreakTiesWithShuffle:" + breakTiesWithShuffle);
		s.append("\nEXPUNGE_INVALIDS_B4_RECOMM_PROCESS:" + EXPUNGE_INVALIDS_B4_RECOMM_PROCESS);
		s.append("\nBLACKLISTING:" + BLACKLISTING);
		s.append("\nblacklistingUsersWithLargeMaxActsPerDay:" + blacklistingUsersWithLargeMaxActsPerDay);

		s.append("\nremoveCurrentActivityNameFromRecommendations:" + removeCurrentActivityNameFromRecommendations);
		s.append("\nhasInvalidActivityNames:" + hasInvalidActivityNames);

		s.append("\neditDistTimeDistType:" + editDistTimeDistType);

		s.append("\nRoundingPrecision:" + RoundingPrecision);
		s.append("\nEvalPrecisionRecallFMeasure:" + EvalPrecisionRecallFMeasure);
		s.append("\ndecimalPlacesInGeocordinatesForComputations:" + decimalPlacesInGeocordinatesForComputations);

		s.append("\nDoBaselineDuration:" + DoBaselineDuration);
		s.append("\nDoBaselineOccurrence:" + DoBaselineOccurrence);
		s.append("\nDoBaselineNGramSeq:" + DoBaselineNGramSeq);

		s.append("\ncheckIfTimelineCreatedIsChronological:" + checkIfTimelineCreatedIsChronological);
		s.append("\ncheckArrayOfFeatures:" + checkArrayOfFeatures);
		s.append("\ncheckForHaversineAnomaly:" + checkForHaversineAnomaly);
		s.append("\ncheckForDistanceTravelledAnomaly:" + checkForDistanceTravelledAnomaly);

		s.append("\nuseJarForMySimpleLevenshteinDistance:" + useJarForMySimpleLevenshteinDistance);
		// useJarForMySimpleLevenshteinDistance

		s.append("\nuseHierarchicalDistance:" + useHierarchicalDistance);
		s.append("\nHierarchicalLevelForEditDistance:" + HierarchicalLevelForEditDistance);

		if (distanceUsed.equals("FeatureWiseEditDistance"))
		{
			s.append("\nConsider all features for feature wise edit distance:"
					+ considerAllFeaturesForFeatureWiseEditDistance);

			if (considerAllFeaturesForFeatureWiseEditDistance == false)
			{
				s.append("\nconsiderActivityNameInFeatureWiseEditDistance:"
						+ considerActivityNameInFeatureWiseEditDistance);
				s.append("\nconsiderStartTimeInFeatureWiseEditDistance:" + considerStartTimeInFeatureWiseEditDistance);
				s.append("\nconsiderDurationInFeatureWiseEditDistance:" + considerDurationInFeatureWiseEditDistance);
				s.append("\nconsiderDistanceTravelledInFeatureWiseEditDistance:"
						+ considerDistanceTravelledInFeatureWiseEditDistance);
				s.append("\nconsiderStartGeoCordinatesInFeatureWiseEditDistance:"
						+ considerStartGeoCoordinatesInFeatureWiseEditDistance);
				s.append("\nconsiderEndGeoCordinatesInFeatureWiseEditDistance:"
						+ considerEndGeoCoordinatesInFeatureWiseEditDistance);
				s.append("\nconsiderAvgAltitudeInFeatureWiseEditDistance:"
						+ considerAvgAltitudeInFeatureWiseEditDistance);
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
		if (caseType == null)// .length() == 0)
		{
			System.err.println("caseType name is null");
			allSet = false;
		}
		if (ALPHA < 0 && rankScoring.equals("sum"))
		{
			System.err.println("ALPHA  is less than 0 (not set) for Sum-based Rank Scoring");
			allSet = false;
		}
		return allSet;
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

}
