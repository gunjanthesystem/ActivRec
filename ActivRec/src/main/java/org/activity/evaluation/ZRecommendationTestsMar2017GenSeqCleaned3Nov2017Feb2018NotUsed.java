package org.activity.evaluation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums;
import org.activity.constants.Enums.CaseType;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.recomm.RecommendationMasterI;
import org.activity.recomm.RecommendationMasterMar2017AltAlgoSeqNov2017;
import org.activity.recomm.RecommendationMasterMar2017GenSeqNGramBaseline;
import org.activity.recomm.RecommendationMasterMar2017GenSeqNov2017;
import org.activity.sanityChecks.Sanity;
import org.activity.spmf.AKOMSeqPredictorLighter;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ConnectDatabase;
import org.activity.util.DateTimeUtils;
import org.activity.util.PerformanceAnalytics;
import org.activity.util.RegexUtils;
import org.activity.util.StringUtils;
import org.activity.util.TimelineTransformers;
import org.activity.util.TimelineTrimmers;
import org.activity.util.TimelineUtils;

/**
 * <b>NOT USED AS OF 10 APRIL 2018</b>
 * </p>
 * Fork of org.activity.evaluation.RecommendationTestsMar2017GenSeqCleaned3Nov2017Feb2018, refactoring it so as to be
 * able to run experiments for multiple matching units in parallel to maximally utilise computing resources.
 * <p>
 * . cleaner version (recommending sequences). Executes the experiments for generating recommendations
 * 
 * @author gunjan
 * @since 16 Feb 2018
 * @deprecated
 */
public class ZRecommendationTestsMar2017GenSeqCleaned3Nov2017Feb2018NotUsed
{
	// String typeOfMatching; //"Daywise","
	double percentageInTraining;// = 0.8;
	// String fullCandOrSubCand="fullCand";
	/**
	 * threshold for choosing candidate timelines, those candidate timelines whose distance from the 'activity Objects
	 * guiding recommendations' is higher than the cost of replacing 'percentageDistanceThresh' % of Activity Objects in
	 * the activities guiding recommendation are pruned out from set of candidate timelines
	 */
	Enums.TypeOfThreshold typeOfThresholds[];// = { "Global" };// Global"};//"Percent",
	public final int globalThresholds[] = { 100 };// },30, 50, 60, 70, 75, 80, 90, 95 };// 10000000 };//
	// {50,100,150,200,250,300,350,400,450,500,550,600,650,700,1000};
	int percentThresholds[] = { 50, 60, 70, 80, 90, 100 };// { 100 };// {50,60,70,80,90,100};

	CaseType caseType;// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
	LookPastType lookPastType;// = "Count";// "Hrs" "Daywise"

	int userIDs[];// = { 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 };

	// TreeMap<Integer, Integer> userIdNumOfRTsMap;

	double matchingUnitHrsArray[];
	double matchingUnitAsPastCount[];

	double matchingUnitArray[];

	public boolean pruningHasSaturated;

	private boolean writeDayTimelinesOnce = true;;

	int thresholdsArray[];

	int recommSeqLength;

	PrimaryDimension primaryDimension;
	String databaseName;
	/**
	 * // * (UserId,ActName,RepresentativeAO) //
	 */ // currently doing it for each matching unit
	// LinkedHashMap<Integer, LinkedHashMap<String, ActivityObject>> mapOfRepAOs = new LinkedHashMap<>();
	// LinkedHashMap<Integer, LinkedHashMap<String, Pair<Double, Double>>> mapOfMedianPreSuccDuration = new
	// LinkedHashMap<>();

	/**
	 * ALERT: this is not to be used while thresholding <User,<MRR0, MRR2, .... > >
	 */
	// LinkedHashMap<String, ArrayList<Double>> allUsersMRRForAllMUs;

	/**
	 * 
	 * @param sampledUsersTimelines
	 * @param lookPastType
	 * @param caseType
	 * @param typeOfThresholds
	 * @param userIDs
	 * @param percentageInTraining
	 * @param lengthOfRecommendedSequence
	 * @param allUsersTimelines
	 *            for collaborative approach, all neighbours
	 */
	// @SuppressWarnings("unused")
	public ZRecommendationTestsMar2017GenSeqCleaned3Nov2017Feb2018NotUsed(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsersTimelines, Enums.LookPastType lookPastType,
			Enums.CaseType caseType, Enums.TypeOfThreshold[] typeOfThresholds, int[] userIDs,
			double percentageInTraining, int lengthOfRecommendedSequence,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersTimelines)
	{
		System.out.println("\n\n **********Entering RecommendationTestsMar2017GenSeqCleaned2********** " + lookPastType
				+ " " + caseType + " lengthOfRecommendedSequence:" + lengthOfRecommendedSequence);
		long recommTestsStarttime = System.currentTimeMillis();

		this.primaryDimension = Constant.primaryDimension;
		this.databaseName = Constant.getDatabaseName();
		this.lookPastType = lookPastType;
		this.caseType = caseType;

		this.percentageInTraining = percentageInTraining;
		this.typeOfThresholds = typeOfThresholds;
		this.userIDs = userIDs;

		this.recommSeqLength = lengthOfRecommendedSequence;// SeqRecommConstants.RecommSeqLen;

		// if userid is not set in constant class, in case of gowalla, then extract user id from given timelines
		if (userIDs == null || userIDs.length == 0)
		{
			userIDs = sampledUsersTimelines.keySet().stream().mapToInt(userID -> Integer.valueOf(userID)).toArray();
			System.out.println("UserIDs not set in Constant, hence extracted" + userIDs.length
					+ " user ids from usersTimelines keyset");
			Constant.setUserIDs(userIDs);
		}
		// System.out.println("User ids = " + Arrays.toString(userIDs));

		// Rts not used in daywise matching owing to unavailability of cand timelines for them
		List<String> blackListedRTs = null;
		if (Constant.BLACKLISTING)
		{
			blackListedRTs = ReadingFromFile.getBlackListedRTs(this.databaseName);
		}

		// check if directory is empty to prevent overwriting of results
		// if (UtilityBelt.isDirectoryEmpty(Constant.outputCoreResultsPath) == false)
		// { System.err.println("Warning with exit: results' directory not empty");
		// System.exit(-1);}

		// setMatchingUnitArray(lookPastType);
		this.matchingUnitArray = Constant.getMatchingUnitArray(lookPastType, Constant.altSeqPredictor);

		// buildRepresentativeActivityObjectsForUsers()

		for (Enums.TypeOfThreshold typeOfThreshold : typeOfThresholds)
		{
			setThresholdsArray(typeOfThreshold);
			for (int thresholdValue : thresholdsArray)
			{
				System.out.println("Executing RecommendationTests for threshold value: " + thresholdValue);
				// ArrayList<String> userNames = new ArrayList<String>();
				LinkedHashMap<Date, Timeline> userAllDatesTimeslines = null;
				pruningHasSaturated = true;
				try
				{
					for (double matchingUnit : matchingUnitArray)
					{
						runRecommTestsForMU(sampledUsersTimelines, lookPastType, caseType, userIDs,
								percentageInTraining, allUsersTimelines, blackListedRTs, typeOfThreshold,
								thresholdValue, userAllDatesTimeslines, matchingUnit);
					} // end of loop over matching unit
				}

				catch (IOException e)
				{
					e.printStackTrace();
				}

				catch (Exception e)
				{
					e.printStackTrace();
				}
				// new TestStats();
				// /////////////////////core
			} // end of loop over threshold
		}
		// PopUps.showMessage("ALL TESTS DONE... u can shutdown the server");// +msg);
		System.out.println("**********Exiting Recommendation Tests**********");

	}

	/**
	 * Prime motivation to create/absract this method is to be able to run experiment for multiple matching unit in
	 * parallel
	 * 
	 * @since 16 Feb 2018
	 * @param sampledUsersTimelines
	 * @param lookPastType
	 * @param caseType
	 * @param userIDs
	 * @param percentageInTraining
	 * @param allUsersTimelines
	 * @param blackListedRTs
	 * @param typeOfThreshold
	 * @param thresholdValue
	 * @param userAllDatesTimeslines
	 * @param matchingUnit
	 * @throws IOException
	 */
	public void runRecommTestsForMU(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsersTimelines,
			Enums.LookPastType lookPastType, Enums.CaseType caseType, int[] userIDs, double percentageInTraining,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersTimelines, List<String> blackListedRTs,
			Enums.TypeOfThreshold typeOfThreshold, int thresholdValue,
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines, double matchingUnit) throws IOException
	{
		long ctmu1 = System.currentTimeMillis();
		TreeMap<Integer, Integer> userIdNumOfRTsMap = new TreeMap<Integer, Integer>();

		Constant.setCurrentMatchingUnit(matchingUnit); // used for sanity checks
		System.out.println("Executing RecommendationTests for matching unit: " + matchingUnit);

		String commonPath = computeCommonPath(matchingUnit, lookPastType, Constant.getOutputCoreResultsPath(),
				thresholdValue);
		Constant.setCommonPath(commonPath);
		System.out.println("Common path=" + Constant.getCommonPath());

		PrintStream consoleLogStream = WToFile.redirectConsoleOutput(commonPath + "consoleLog.txt");

		BufferedWriter metaBw = WToFile.getBWForNewFile(commonPath + "meta.csv");

		BufferedWriter recommSeqWithoutScoreBw = WToFile.getBWForNewFile(commonPath + "dataRecommSequence.csv");// **

		BufferedWriter recommSeqWithScoreBw = WToFile.getBWForNewFile(commonPath + "dataRecommSequenceWithScore.csv");// **

		BufferedWriter actualSeqBw = WToFile.getBWForNewFile(commonPath + "dataActualSequence.csv");// **

		ArrayList<BufferedWriter> bwsDataActual = new ArrayList<>(this.recommSeqLength);

		BufferedWriter topNextActsWithoutDistance = WToFile
				.getBWForNewFile(commonPath + "topNextActivitiesWithoutDistance.csv");
		BufferedWriter topNextActsWithDistance = WToFile
				.getBWForNewFile(commonPath + "topNextActivitiesWithDistance.csv");

		BufferedWriter rtsRejNoValidActAfterWriter = WToFile
				.getBWForNewFile(commonPath + "recommPointsInvalidBecuzNoValidActivityAfterThis.csv");
		BufferedWriter rtsRejWithNoCandsWriter = WToFile
				.getBWForNewFile(commonPath + "recommPointsWithNoCandidates.csv");
		BufferedWriter rtsRejWithNoCandsBelowThreshWriter = WToFile
				.getBWForNewFile(commonPath + "recommPointsWithNoCandidatesBelowThresh.csv");
		BufferedWriter rtsRejBlackListedWriter = WToFile.getBWForNewFile(commonPath + "recommPointsRejBlacklisted.csv");
		BufferedWriter rtsRejWithNoDWButMUCandsCands = WToFile
				.getBWForNewFile(commonPath + "recommPointsWithNoDWButMUCandidates.csv");

		ArrayList<BufferedWriter> bwsRankedRecommWithScore = new ArrayList<>(this.recommSeqLength);
		ArrayList<BufferedWriter> bwRankedRecommWithoutScore = new ArrayList<>(this.recommSeqLength);

		BufferedWriter metaIfCurrentTargetSameWriter = WToFile
				.getBWForNewFile(commonPath + "metaIfCurrentTargetSameWriter.csv");

		ArrayList<BufferedWriter> numOfCandidateTimelinesWriter = new ArrayList<>(this.recommSeqLength);
		ArrayList<BufferedWriter> bwsRaw = new ArrayList<>(this.recommSeqLength);

		for (int i = 0; i < this.recommSeqLength; i++)
		{
			numOfCandidateTimelinesWriter.add(i,
					WToFile.getBWForNewFile(commonPath + "numOfCandidateTimelines" + i + ".csv"));
			bwsRaw.add(i, WToFile.getBWForNewFile(commonPath + "Raw" + i + ".csv"));
			bwsRaw.get(i).write(
					"User,DateOfRecomm,TimeOfRecomm,TimeCategoryOfRecomm,CurrentTimeline, CurrentActivity(ActivityAtRecommPoint),NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline,NumOfCandidateTimelinesBelowThresh, WeekDayOfRecomm,Target(ActualActivity), RecommendedActivities, NumOfDistinctRecomms, PearsonCorrOfCandSimsAndEndCaseSims, AvgRestSimilarity, StdDevRestSimilarity, AvgEndSimilarity, StdDevEndSimilarity,IsCurrentActTargetActSame\n");// LastActivityOnRecommDay");//,ActivitiesOnRecommDayUntiRecomm");

			bwsRankedRecommWithScore.add(i,
					WToFile.getBWForNewFile(commonPath + "dataRankedRecommendationWithScores" + i + ".csv"));
			bwRankedRecommWithoutScore.add(i,
					WToFile.getBWForNewFile(commonPath + "dataRankedRecommendationWithoutScores" + i + ".csv"));

			bwsDataActual.add(i, WToFile.getBWForNewFile(commonPath + "dataActual" + i + ".csv"));
		}

		/**
		 * Contains list of activity names sorted by frequency of occurrence/duration. Num of unique sorted lists =
		 * number of users, however, each list is repeated so as maintain structural conformity with
		 * dataRankedRecommendationWithoutScores.csv
		 */
		BufferedWriter baseLineOccurrence = WToFile.getBWForNewFile(commonPath + "dataBaseLineOccurrence.csv");
		BufferedWriter baseLineDuration = WToFile.getBWForNewFile(commonPath + "dataBaseLineDuration.csv");

		BufferedWriter bwNumOfWeekendsInTraining = WToFile
				.getBWForNewFile(commonPath + "NumberOfWeekendsInTraining.csv");
		BufferedWriter bwNumOfWeekendsInAll = WToFile.getBWForNewFile(commonPath + "NumberOfWeekendsInAll.csv");
		BufferedWriter bwCountTimeCategoryOfRecomm = WToFile
				.getBWForNewFile(commonPath + "CountTimeCategoryOfRecommPoitns.csv");
		BufferedWriter bwNextActInvalid = WToFile.getBWForNewFile(commonPath + "NextActivityIsInvalid.csv");
		BufferedWriter bwWriteNormalisationOfDistance = WToFile
				.getBWForNewFile(commonPath + "NormalisationDistances.csv");

		BufferedWriter bwNumOfValidAOsAfterRTInDay = WToFile
				.getBWForNewFile(commonPath + "NumOfValidAOsAfterRTInDay.csv");

		rtsRejNoValidActAfterWriter.write(
				"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity\n");
		rtsRejWithNoCandsWriter.write(
				"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity,TotalNumOfPossibleCands, NumCandsRejectedDueToNoCurrentActivityAtNonLast,NumCandsRejectedDueToNoNextActivity\n");
		rtsRejWithNoCandsBelowThreshWriter.write(
				"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity,TotalNumOfPossibleCands, NumCandsRejectedDueToNoCurrentActivityAtNonLast,NumCandsRejectedDueToNoNextActivity\n");
		rtsRejBlackListedWriter.write("User_ID,End_Timestamp\n");

		WToFile.writeToNewFile("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings,TotalRTs\n",
				commonPath + "UsersWithNoValidRTs.csv");

		bwCountTimeCategoryOfRecomm.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings,TotalRTs\n");

		bwNumOfWeekendsInTraining.write("User,NumOfWeekends,NumOfWeekdays\n");
		bwNumOfWeekendsInAll.write("User,NumOfWeekends,NumOfWeekdays\n");

		bwNextActInvalid.write("User,Timestamp_of_Recomm\n");

		// bwCountInActivitiesGuidingRecomm.write("User,RecommendationTime,TimeCategory,NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline\n");

		BufferedWriter numActsInEachCandbw = WToFile.getBWForNewFile(commonPath + "NumActs.csv");
		// commonPath + "NumActsmatchingUnit" + String.valueOf(matchingUnit) + ".csv");
		numActsInEachCandbw.write("NumOfActObjsInCand-1,candTimelineID,UserId, DateAtRT, TimeAtRT, ActObjsInCand\n");

		bwWriteNormalisationOfDistance.write("User, DateOfRecomm, TimeOfRecom, EditDistance,NormalisedEditDistance\n");

		bwNumOfValidAOsAfterRTInDay.write("User, DateOfRecomm, TimeOfRecom, NumOfValidsAOsAfterRTInDay\n");

		// writes the edit similarity calculations for this recommendation master
		// WritingToFile.writeEditSimilarityCalculationsHeader();
		WToFile.writeToNewFile(
				"UserAtRecomm,DateAtRecomm,TimeAtRecomm,CandidateTimelineID,EditDistance,ActLevelDistance,FeatLevelDistance,Trace, ActivityObjects1,ActivityObjects2,NumOfActivityObjects1,NumOfActivityObjects2\n",
				commonPath + "EditSimilarityCalculations.csv");

		// writes the header for EditDistancePerRtPerCand.csv//
		// WritingToFile.writeDistanceScoresSortedMapHeader();
		// for org.activity.io.WritingToFile.writeEditDistancesPerRtPerCand() which is called in recomm
		// master
		WToFile.writeToNewFile(
				"UserAtRecomm,DateAtRecomm,TimeAtRecomm,CandID,EndPointIndexOfCand, EditOpsTraceOfCand,EditDistOfCand,#L1_EditOps, #ObjInSameOrder_#L2EditOps,NextActivityForRecomm, diffSTEndPointsCand_n_CurrActInSecs,diffETEndPointsCand_n_CurrActInSecs,CandidateTimeline,CurrentTimeline\n",
				commonPath + "EditDistancePerRtPerCand.csv");

		System.out.println(Constant.getCommonPath() + "\n" + Constant.getAllGlobalConstants());
		WToFile.writeToNewFile(Constant.getCommonPath() + "\n" + Constant.getAllGlobalConstants(),
				commonPath + "Config.csv");

		/** Can be used to select users above 10 RTs **/
		LinkedHashMap<Integer, Integer> numOfValidRTs = new LinkedHashMap<Integer, Integer>();

		// LinkedHashMap<Integer, LinkedHashMap<Timestamp, Integer>> rtsWithMoreThan4ValidsAfter = new
		// LinkedHashMap<>();
		StringBuilder sbNumOfValidsAfterAnRT = new StringBuilder();

		// int userCount = 0;
		/**
		 * This can be made class variable and does not need to be repeated for each matching unit, but in current setup
		 * we are doing it for each matching unit. Note: this is very small performance effect, hence should not be of
		 * major concern. (UserId,ActName,RepresentativeAO)
		 */
		LinkedHashMap<Integer, LinkedHashMap<Integer, ActivityObject2018>> mapOfRepAOs = new LinkedHashMap<>();
		LinkedHashMap<Integer, LinkedHashMap<Integer, Pair<Double, Double>>> mapOfMedianPreSuccDuration = new LinkedHashMap<>();
		// PopUps.showMessage("Starting iteration over user");

		/**
		 * training test DAY timelines for all users
		 **/
		LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW = null;

		// training test timelines for all users continuous
		LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous = null;

		/**
		 * {ActID,RepAO}, {ActID,{medianDurFromPrevForEachActName, medianDurFromNextForEachActName}}
		 */
		Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultGenericUser = null;

		if (Constant.collaborativeCandidates)
		{
			trainTestTimelinesForAllUsersDW = TimelineUtils.splitAllUsersTestTrainingTimelines(allUsersTimelines,
					percentageInTraining, Constant.cleanTimelinesAgainInsideTrainTestSplit);

			if (Constant.filterTrainingTimelinesByRecentDays)
			{
				trainTimelinesAllUsersContinuous = getContinousTrainingTimelinesWithFilterByRecentDaysV2(
						trainTestTimelinesForAllUsersDW, Constant.getRecentDaysInTrainingTimelines());
			}
			else
			{
				// sampledUsersTimelines
				trainTimelinesAllUsersContinuous = getContinousTrainingTimelines(trainTestTimelinesForAllUsersDW);
			}
		}
		/// temp start
		////// START of build representative activity objects for this user.
		// if (true)// representativeAOsNotComputed == false) //do this
		// we only need to do it once for each user and dont need to repeat it for each matching
		// unit, so we can take it out of the for loop, however, it that we will also need to
		// take out the train-test splitting of timelines out of the loop, however that can be done
		// as well
		// Start of curtain: incomplete: motive: faster speed
		if (Constant.collaborativeCandidates)
		{// collaborative approach
			// disabled on 1 Aug 2017, will do just-in-time computation of representative act object
			// based only on candidate timelines
			if (Constant.preBuildRepAOGenericUser)
			{
				repAOResultGenericUser = buildRepresentativeAOsAllUsersPDVCOllAllUsers(trainTestTimelinesForAllUsersDW,
						Constant.getUniqueLocIDs(), Constant.getUniqueActivityIDs());
			}
			// end of curtain
			////// END of build representative activity objects for this user.
			/// temp end
		}

		System.out.println("Will now loop over users in RecommTests: num of userIDs.length= " + userIDs.length
				+ " trainTimelinesAllUsersContinuous.size()=" + trainTimelinesAllUsersContinuous.size());
		for (int userId : userIDs) // for(int userId=minTestUser;userId <=maxTestUser;userId++)
		{ // int numberOfValidRTs = 0;// userCount += 1;

			// Start of Added on 21 Dec 2017
			if (Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM)
					|| Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.AKOM))
			{
				AKOMSeqPredictorLighter.clearSeqPredictorsForEachUserStored();
			}
			// End of Added on 21 Dec 2017

			System.out.println("\nUser id=" + userId);
			// PopUps.showMessage("\nUser id=" + userId);
			String userName = "";
			if (this.databaseName.equals("gowalla1"))
			{
				userName = String.valueOf(userId);
			}
			else
			{
				userName = ConnectDatabase.getUserName(userId);// ConnectDatabase.getUserNameFromDatabase(userId);
			}

			// PopUps.showMessage("before blacklisting");
			if (Constant.blacklistingUsersWithLargeMaxActsPerDay && isBlackListedUser(userId))
			{
				continue;
			}
			// PopUps.showMessage("after blacklisting");

			// replacing iterative write with StringBuilder for better performance
			ArrayList<StringBuilder> sbsMaxNumOfDistinctRecommendations = createListOfStringBuilders(
					this.recommSeqLength);
			sbsMaxNumOfDistinctRecommendations.parallelStream().forEach(sb -> sb.append(
					"DateOfRecomm ,TimeOfRecomm ,Week_Day,MaxNumOfDistictRecommendation,NumOfCandidateTimelines(after applying Threshold)\n"));

			// replacing iterative write with StringBuilder for better performance
			ArrayList<StringBuilder> sbsNumOfCandTimelinesBelowThreshold = createListOfStringBuilders(
					this.recommSeqLength);
			sbsNumOfCandTimelinesBelowThreshold.parallelStream().forEach(sb -> sb.append(
					"DateOfRecomm ,TimeOfRecomm ,Week_Day,ThresholdAsDistance,NumOfCandidateTimelinesBelowThreshold,\n"));

			ArrayList<StringBuilder> sbsRecommTimesWithEditDistances = createListOfStringBuilders(this.recommSeqLength);
			sbsRecommTimesWithEditDistances.parallelStream().forEach(sb -> sb.append("DateOfRecomm"
					+ ",TimeOfRecomm,CandidateTimelineID,TargetActivity,EditDistanceOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,EndIndexOf(Sub)Cand,CandidateTimeline,WeekDayOfRecomm\n"));

			userAllDatesTimeslines = sampledUsersTimelines.get(Integer.toString(userId));// userId);
			if (userAllDatesTimeslines == null)
			{
				System.err.println("Error: userAllDatesTimeslines = " + userAllDatesTimeslines + " user " + userId);
			}

			// //////////////////REMOVING SELECTED TIMELINES FROM DATASET////////////////////
			userAllDatesTimeslines = TimelineTrimmers.cleanUserDayTimelines(userAllDatesTimeslines,
					commonPath + "InsideRecommTestCleanUserDayTimelines", String.valueOf(userId));
			// ////////////////////////////////////////////////////////////////////////////////
			if (this.writeDayTimelinesOnce)
			{// if (matchingUnitIterator == 0) // write the given day timelines only once
				WToFile.writeGivenDayTimelines(userName, userAllDatesTimeslines, "All", true, true, true);
				this.writeDayTimelinesOnce = false;
			}

			// Splitting the set of timelines into training set and test set.
			List<LinkedHashMap<Date, Timeline>> trainTestTimelines = TimelineUtils
					.splitTestTrainingTimelines(userAllDatesTimeslines, percentageInTraining);
			LinkedHashMap<Date, Timeline> userTrainingTimelines = trainTestTimelines.get(0);
			LinkedHashMap<Date, Timeline> userTestTimelines = trainTestTimelines.get(1);

			sanityCheckTrainTestSplitSameForCollNonColl(trainTestTimelinesForAllUsersDW, userId, userTrainingTimelines);

			////// START of build representative activity objects for this user.
			// if (true)// representativeAOsNotComputed == false) //do this
			// we only need to do it once for each user and dont need to repeat it for each matching
			// unit, so we can take it out of the for loop, however, it that we will also need to
			// take out the train-test splitting of timelines out of the loop, however that can be done
			// as well
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResult = null;
			if (Constant.preBuildRepAOGenericUser == false)
			{
				if (Constant.collaborativeCandidates == false)
				{
					repAOResult = prebuildRepresentativeActivityObjects(trainTestTimelinesForAllUsersDW, userId,
							userTrainingTimelines, userTestTimelines);

					LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = repAOResult.getFirst();
					mapOfRepAOs.put(userId, repAOsForThisUser);
					mapOfMedianPreSuccDuration.put(userId, repAOResult.getSecond());
				}
				else
				{// collaborative approach
					// disabled on 1 Aug 2017, will do just-in-time computation of representative act
					// object// based only on candidate timelines
					if (Constant.buildRepAOJustInTime == false)
					{
						repAOResult = buildRepresentativeAOsForUserPDVCOll(userId, trainTestTimelinesForAllUsersDW,
								Constant.getUniqueLocIDs(), Constant.getUniqueActivityIDs());
						LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = repAOResult.getFirst();
						mapOfRepAOs.put(userId, repAOsForThisUser);
						mapOfMedianPreSuccDuration.put(userId, repAOResult.getSecond());
					}
				}
			}
			////// END of build representative activity objects for this user.

			if (Constant.collaborativeCandidates)
			{
				if (trainTimelinesAllUsersContinuous.size() <= 1)
				{
					System.out.println("Warning: Skipping this user " + userId
							+ " as it has 1 training user: trainTimelinesAllUsersContinuous.size()="
							+ trainTimelinesAllUsersContinuous.size());
					WToFile.appendLineToFileAbs("User " + userId + ",", commonPath + "UserWithNoTrainingDay.csv");
					numOfValidRTs.put(userId, 0);
					continue;
				}
			}
			else
			{
				if (userTrainingTimelines.size() == 0)
				{
					System.out.println("Warning: Skipping this user " + userId + " as it has 0 training days");
					WToFile.appendLineToFileAbs("User " + userId + ",", commonPath + "UserWithNoTrainingDay.csv");
					numOfValidRTs.put(userId, 0);
					continue;
				}
			}

			/////////// WRITE activity names as sequence for HMM or markov chains
			// $$ if (true)
			// { TimelineTransformers.writeUserActNamesSeqAsNames(userId, userTrainingTimelines,
			// userTestTimelines, userAllDatesTimeslines);
			// continue;// TEMPORARY // continue to next user $$ }
			//////////

			// ////////////////////////////////////////
			// if (matchingUnitIterator == 0) // do this only for one matching unit as it does not
			// change per matching unit
			// note: the maps of maps here contains a map for baselines count and another map for
			// baseline duration: they will be used later to write prediction results for baseline count
			// and baseline duration
			LinkedHashMap<String, LinkedHashMap<String, ?>> mapsForCountDurationBaselines = null;
			String actNamesCountsWithoutCountOverTrain = "";
			String actNamesDurationsWithoutDurationOverTrain = "";

			// START OF Curtain disable trivial baselines
			// if (Constant.DoBaselineDuration || Constant.DoBaselineOccurrence)
			// {
			// mapsForCountDurationBaselines = WritingToFile.writeBasicActivityStatsAndGetBaselineMaps(
			// userName, userAllDatesTimeslines, userTrainingTimelines, userTestTimelines);
			// LinkedHashMap<String, Long> activityNameCountPairsOverAllTrainingDays =
			// (LinkedHashMap<String, Long>) mapsForCountDurationBaselines
			// .get("activityNameCountPairsOverAllTrainingDays");
			// ComparatorUtils.assertNotNull(activityNameCountPairsOverAllTrainingDays);
			// actNamesCountsWithoutCountOverTrain = getActivityNameCountPairsWithoutCount(
			// activityNameCountPairsOverAllTrainingDays);
			//
			// LinkedHashMap<String, Long> activityNameDurationPairsOverAllTrainingDays =
			// (LinkedHashMap<String, Long>) mapsForCountDurationBaselines
			// .get("activityNameDurationPairsOverAllTrainingDays");
			// ComparatorUtils.assertNotNull(activityNameDurationPairsOverAllTrainingDays);
			// actNamesDurationsWithoutDurationOverTrain = getActivityNameDurationPairsWithoutDuration(
			// activityNameDurationPairsOverAllTrainingDays);
			// }
			// END OF Curtain disable trivial baselines

			// else if (matchingUnitIterator == 0) // do this only for one matching unit as it does not
			// // change per matching unit
			// {
			// mapsForCountDurationBaselines = WritingToFile.writeBasicActivityStatsAndGetBaselineMaps(
			// userName, userAllDatesTimeslines, userTrainingTimelines, userTestTimelines);
			// }

			/*
			 * ********** ************************************************ ********************************
			 */

			int numOfWeekendsInTraining = TimelineUtils.getNumOfWeekendsInGivenDayTimelines(userTrainingTimelines);
			int numOfWeekdaysInTraining = userTrainingTimelines.size() - numOfWeekendsInTraining;
			bwNumOfWeekendsInTraining
					.write(userName + "," + numOfWeekendsInTraining + "," + numOfWeekdaysInTraining + "\n");

			int numOfWeekendsInAll = TimelineUtils.getNumOfWeekendsInGivenDayTimelines(userAllDatesTimeslines);
			int numOfWeekdaysInAll = userAllDatesTimeslines.size() - numOfWeekendsInAll;
			bwNumOfWeekendsInAll.write(userName + "," + numOfWeekendsInAll + "," + numOfWeekdaysInAll + "\n");
			// }
			// ////////////////////////////////////////

			int numberOfMorningRTs = 0, numberOfAfternoonRTs = 0, numberOfEveningRTs = 0;
			// Generating Recommendation Timestamps
			// generate date and times for recommendation

			for (Map.Entry<Date, Timeline> testDayTimelineEntry : userTestTimelines.entrySet())
			{
				Date testDate = testDayTimelineEntry.getKey();
				Timeline testDayTimelineForUser = testDayTimelineEntry.getValue();

				int date = testDate.getDate();
				int month = testDate.getMonth() + 1;
				int year = testDate.getYear() + 1900;

				String dateToRecomm = date + "/" + month + "/" + year;
				System.out.println(
						"For userid=" + userId + " entry.getKey()=" + testDate + "  dateToRecomm=" + dateToRecomm);

				String weekDay = DateTimeUtils.getWeekDayFromWeekDayInt(testDate.getDay());

				ArrayList<ActivityObject2018> activityObjectsInTestDay = testDayTimelineForUser
						.getActivityObjectsInDay();

				////////// added to improve write speed
				ArrayList<StringBuilder> sbsRawToWriteForThisUserDate = createListOfStringBuilders(recommSeqLength);

				StringBuilder metaToWriteForThisUserDate = new StringBuilder();
				StringBuilder recommSequenceWithScoreForThisUserDate = new StringBuilder();
				StringBuilder recommSequenceWithoutScoreForThisUserDate = new StringBuilder();

				ArrayList<StringBuilder> sbsDataActualToWriteForThisUserDate = createListOfStringBuilders(
						recommSeqLength);

				StringBuilder dataActualSeqActsToWriteForThisUserDate = new StringBuilder();
				StringBuilder metaIfCurrentTargetSameToWriteForThisUserDate = new StringBuilder();

				ArrayList<StringBuilder> sbsNumOfCandTimelinesForThisUserDate = createListOfStringBuilders(
						recommSeqLength);

				StringBuilder topNextActsWithoutDistToWriteForThisUserDate = new StringBuilder();
				StringBuilder topNextActsWithDistToWriteForThisUserDate = new StringBuilder();

				ArrayList<StringBuilder> rankedRecommWithScoreToWriteForThisUserDate = createListOfStringBuilders(
						recommSeqLength);

				ArrayList<StringBuilder> rankedRecommWithoutScoreToWriteForThisUserDate = createListOfStringBuilders(
						recommSeqLength);

				StringBuilder baseLineOccurrenceToWriteForThisUserDate = new StringBuilder();
				StringBuilder baseLineDurationToWriteForThisUserDate = new StringBuilder();
				///////////

				// loop over the activity objects for this day
				// will not make recommendation for days which have only one activity
				// PopUps.showMessage(
				// "Just before iterating over AOs in test: activityObjectsInTestDay.size() = "
				// + activityObjectsInTestDay.size());
				for (int indexOfAOInDay = 0; indexOfAOInDay < activityObjectsInTestDay.size() - 1; indexOfAOInDay++)
				{
					ActivityObject2018 activityObjectInTestDay = activityObjectsInTestDay.get(indexOfAOInDay);

					String activityNameInTestDay = activityObjectInTestDay.getActivityName();
					ArrayList<Integer> primaryDimensionValInTestDay = activityObjectInTestDay.getPrimaryDimensionVal();

					System.out.println(
							"----\nIterating over potential recommendation times: current activityNameInTestDay="
									+ activityNameInTestDay + "  primaryDimensionValInTestDay="
									+ primaryDimensionValInTestDay);

					// (activityObjectsInThatDay.get(j).getActivityName()));

					if (activityNameInTestDay.equals(Constant.INVALID_ACTIVITY1))
					{// ("Unknown"))
						System.out.println("Skipping because " + Constant.INVALID_ACTIVITY1);
						continue;
					}
					if (activityNameInTestDay.equals(Constant.INVALID_ACTIVITY2))
					{// ("Others"/"Not// Available"))
						System.out.println("Skipping because " + Constant.INVALID_ACTIVITY2);
						continue;
					}
					// Recommendation is made at the end time of the activity object in consideration
					Timestamp endTimeStamp = activityObjectInTestDay.getEndTimestamp();// getStartTimestamp();
					String timeCategory = DateTimeUtils.getTimeCategoryOfDay(endTimeStamp.getHours());

					// SeqChange to check for more number of activities after the recomm point
					if (TimelineUtils.hasAtleastNValidAOsAfterItInTheDay(indexOfAOInDay, testDayTimelineForUser,
							recommSeqLength) == false)
					{
						System.out.println("Skipping this recommendation point because there less than "
								+ recommSeqLength + " activity objects after this in the day");
						rtsRejNoValidActAfterWriter.write(userId + "," + dateToRecomm + "," + indexOfAOInDay + ","
								+ endTimeStamp + "," + weekDay + "," + timeCategory + activityNameInTestDay + "\n");
						continue;
					}

					// Target Activity, actual next activity
					ActivityObject2018 nextValidActivityObjectAfterRecommPoint1 = testDayTimelineForUser
							.getNextValidActivityAfterActivityAtThisTime(new Timestamp(year - 1900, month - 1, date,
									endTimeStamp.getHours(), endTimeStamp.getMinutes(), endTimeStamp.getSeconds(), 0));

					// checked if it is giving correct results: concern: passing end ts directly and
					// using LocalDate for comparison
					ArrayList<ActivityObject2018> nextValidActivityObjectsAfterRecommPoint1 = TimelineUtils
							.getNextNValidAOsAfterActivityAtThisTimeSameDay(testDayTimelineForUser, endTimeStamp,
									this.recommSeqLength);
					// timestamp sanity check start
					// $DateTimeSanityChecks.assertEqualsTimestamp(date, month, year, endTimeStamp);
					// FOUND ABOVE OKAY IN RUN

					Sanity.eq(nextValidActivityObjectsAfterRecommPoint1.size(), recommSeqLength,
							"Error in Sanity Check RT407: User id" + userId + " Next activity Objects after "
									+ endTimeStamp + " nextValidActivityObjectsAfterRecommPoint1.size() ="
									+ nextValidActivityObjectsAfterRecommPoint1.size() + " expected =" + recommSeqLength
									+ "\nif it was such, we should have not reached this point of execution");

					System.out.println("UserId" + userId + " Next AO after RT:" + endTimeStamp + " ="
							+ nextValidActivityObjectAfterRecommPoint1.getActivityName() + "\nNext AOs after RT:"
							+ endTimeStamp + " = ");
					nextValidActivityObjectsAfterRecommPoint1.stream().forEachOrdered(
							ao -> System.out.print(ao.getPrimaryDimensionVal("/") + ":" + ao.getEndTimestamp() + ","));

					// TODO do again 14 Jul 2017
					// Sanity.eq(nextValidActivityObjectAfterRecommPoint1.getActivityName(),
					// nextValidActivityObjectsAfterRecommPoint1.get(0).getActivityName(),
					// "Error in Sanity check
					// 575\nnextValidActivityObjectAfterRecommPoint1.getActivityName() ="
					// + nextValidActivityObjectAfterRecommPoint1.getActivityName()
					// + "!= nextValidActivityObjectsAfterRecommPoint1.get(0).getActivityName()"
					// + nextValidActivityObjectsAfterRecommPoint1.get(0)
					// .getActivityName());

					if (VerbosityConstants.WriteNumOfValidsAfterAnRTInSameDay)
					{
						int numOfValidsAOsAfterThisRT = TimelineUtils
								.getValidAOsAfterThisTimeInTheDay(endTimeStamp, testDayTimelineForUser).size();
						// testDayTimelineForUser.getNumOfValidActivityObjectsAfterThisTimeInSameDay(endTimeStamp);
						// .getNumOfValidActivityObjectAfterThisTime(endTimeStamp);
						sbNumOfValidsAfterAnRT.append(userId + "," + dateToRecomm + "," + endTimeStamp + ","
								+ numOfValidsAOsAfterThisRT + "\n");
						// if (numOfValidsAOsAfterThisRT >= 4) { WritingToFile.appendLineToFileAbsolute(
						// userId + "," + dateToRecomm + "," + endTimeStamp + ","
						// + numOfValidsAOsAfterThisRT + "\n", commonPath +
						// "numOfValidsAfterRTsGEQ4.csv"); }
					}

					// ////////////////////////////Start of New addition for blacklisted RTs
					if (Constant.BLACKLISTING && blackListedRTs.contains(new String(userId + " " + endTimeStamp)))
					{
						System.out.println("Alert: blacklisted RT: " + userId + " " + endTimeStamp
								+ " will not be used and will be logged in rtsRejBlackListedWriter");
						rtsRejBlackListedWriter.write(userId + "," + endTimeStamp + ",\n");
						// sbsNumOfCandTimelinesBelowThreshold.get(0)
						// .append(dateToRecomm + "," + recommendationTimes[0] + "," + weekDay
						// + "," + thresholdAsDistance + "," + 0 + "\n");
						continue;
					}
					// ////////////////////////////End of New addition for blacklisted RTs

					// ///////////
					// Now we have those recommendation times which are valid for making recommendations
					// ///////////////////Start//////////////////////////////////
					// /IMPORTANT
					long ta1 = System.currentTimeMillis();

					RecommendationMasterI recommMasters[] = new RecommendationMasterI[recommSeqLength];

					// start of curtain April 7 //iterative recommendation
					ArrayList<ActivityObject2018> repAOsFromPrevRecomms = new ArrayList<>(recommSeqLength);

					// String recommendationTimeString = endTimeString;
					String recommTimesStrings[] = new String[recommSeqLength];
					Timestamp recommendationTimes[] = new Timestamp[recommSeqLength];
					recommendationTimes[0] = endTimeStamp;// the first RT is endTS of current AO

					String topRecommendedPrimarDimensionVal[] = new String[recommSeqLength];
					boolean skipThisRTNoCandTimelines = false; // skip this RT because no cand timelines

					for (int seqIndex = 0; seqIndex < recommSeqLength; seqIndex++)
					{ // if (VerbosityConstants.verbose){
						System.out.println("\n** Recommendation Iteration " + seqIndex + ": repAOsFromPrevRecomms.size:"
								+ repAOsFromPrevRecomms.size() + " rt:" + recommendationTimes[seqIndex]);
						if (VerbosityConstants.verbose)
						{
							System.out.println("repAOsFromPrevRecomms:");
							repAOsFromPrevRecomms.stream()
									.forEachOrdered(ao -> System.out.print("  >>" + ao.toStringAllGowallaTS()));
						} // }
						recommTimesStrings[seqIndex] = recommendationTimes[seqIndex].getHours() + ":"
								+ recommendationTimes[seqIndex].getMinutes() + ":"
								+ recommendationTimes[seqIndex].getSeconds();

						if (this.lookPastType.equals(Enums.LookPastType.NGram) && (!Constant.NGramColl))
						{
							recommMasters[seqIndex] = new RecommendationMasterMar2017GenSeqNGramBaseline(
									userTrainingTimelines, userTestTimelines, dateToRecomm, recommTimesStrings[0],
									userId, repAOsFromPrevRecomms);
						}
						// Alternative algorithm
						// else if (Constant.altSeqPredictor == Enums.AltSeqPredictor.PureAKOM)
						else if (Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM)
								|| Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.AKOM))
						// && (this.lookPastType.equals(Enums.LookPastType.Daywise)
						{
							recommMasters[seqIndex] = new RecommendationMasterMar2017AltAlgoSeqNov2017(
									userTrainingTimelines, userTestTimelines, dateToRecomm, recommTimesStrings[0],
									userId, thresholdValue, typeOfThreshold, matchingUnit, caseType, this.lookPastType,
									false, repAOsFromPrevRecomms, trainTestTimelinesForAllUsersDW,
									trainTimelinesAllUsersContinuous, Constant.altSeqPredictor, null);
						}

						else if (Constant.altSeqPredictor == Enums.AltSeqPredictor.RNN1)
						// Alternative algorithm
						{
							// recommMasters[seqIndex] = new RecommendationMasterRNN1Nov2018(50, 2, dateToRecomm,
							// recommTimesStrings[0], userId, trainTestTimelinesForAllUsersDW,
							// trainTimelinesAllUsersContinuous);

						}

						else
						{
							recommMasters[seqIndex] = new RecommendationMasterMar2017GenSeqNov2017(
									userTrainingTimelines, userTestTimelines, dateToRecomm, recommTimesStrings[0],
									userId, thresholdValue, typeOfThreshold, matchingUnit, caseType, this.lookPastType,
									false, repAOsFromPrevRecomms, trainTestTimelinesForAllUsersDW,
									trainTimelinesAllUsersContinuous);
						}

						// Note: RT passed to the recommendation master is always endTimestamp. This is
						// intentional to make the current implementation of extracting current timeline
						// work. RT is only important for extraction of current timeline

						//////////////////////////
						RecommendationMasterI recommMaster = recommMasters[seqIndex];
						////////////////////////////////////////////////////////////////////////

						double thresholdAsDistance = recommMasters[0].getThresholdAsDistance();
						// check if all seq recomms for this RT has daywise candidate timelines
						if (recommMaster.hasCandidateTimeslines() == false)
						{
							System.out.println(
									"Can't make recommendation at this point as no cand timelines for recommMaster i = "
											+ seqIndex);

							rtsRejWithNoCandsWriter.write(userId + "," + dateToRecomm + "," + indexOfAOInDay + ","
									+ recommTimesStrings[0] + "," + weekDay + "," + timeCategory + ","
									+ recommMaster.getActivityObjectAtRecomm().getPrimaryDimensionVal("/") + ","
									+ seqIndex + ",\n");
							sbsNumOfCandTimelinesBelowThreshold.get(seqIndex)
									.append(dateToRecomm + "," + recommendationTimes[seqIndex] + "," + weekDay + ","
											+ thresholdAsDistance + "," + 0 + "\n");// bwNumOfCandTimelinesBelowThreshold.newLine();
							skipThisRTNoCandTimelines = true;
							break; // break out of iterative recommendation for each seq index
						}

						///////////////////////////////////////////////////////////////////////
						// check if all seq recomms for this RT will have candidate timelines below
						// threshold
						if (recommMaster.hasCandidateTimelinesBelowThreshold() == false)
						{
							System.out.println(
									"Cannot make recommendation at this point as there are no candidate timelines BELOW THRESHOLD");

							rtsRejWithNoCandsBelowThreshWriter.write(userId + "," + dateToRecomm + "," + indexOfAOInDay
									+ "," + recommTimesStrings[0] + "," + weekDay + "," + timeCategory + ","
									+ recommMaster.getActivityObjectAtRecomm().getPrimaryDimensionVal("/") + ","
									+ seqIndex + ",\n");// $$+recommP1.totalNumberOfProbableCands+","+recommP1.numCandsRejectedDueToNoCurrentActivityAtNonLast+","+recommP1.numCandsRejectedDueToNoNextActivity);
							sbsNumOfCandTimelinesBelowThreshold.get(seqIndex)
									.append(dateToRecomm + "," + recommendationTimes[seqIndex] + "," + weekDay + ","
											+ thresholdAsDistance + "," + 0 + "\n");
							skipThisRTNoCandTimelines = true;
							break; // break out of iterative recommendation for each seq index
						}

						////////////////////////////////////////////////////////////////////////
						// check if all seq recomms for this RT will have daywise candidate timelines
						boolean hasDayWiseCandidateTimelines = TimelineUtils.hasDaywiseCandidateTimelines(
								userTrainingTimelines, recommMaster.getDateAtRecomm(),
								recommMaster.getActivityObjectAtRecomm(), Constant.collaborativeCandidates,
								String.valueOf(userId), trainTestTimelinesForAllUsersDW);

						if (hasDayWiseCandidateTimelines == false)
						{
							rtsRejWithNoDWButMUCandsCands.write(userId + "," + dateToRecomm + "," + indexOfAOInDay + ","
									+ recommendationTimes[0] + "," + weekDay + "," + timeCategory + ","
									+ recommMaster.getActivityObjectAtRecomm().getPrimaryDimensionVal("/") + ","
									+ seqIndex + "\n");
							System.out.println(
									"Cannot make recommendation at this point as there are no daywise candidate timelines, even though there are mu candidate timelines");
							sbsNumOfCandTimelinesBelowThreshold.get(seqIndex)
									.append(dateToRecomm + "," + recommendationTimes[seqIndex] + "," + weekDay + ","
											+ thresholdAsDistance + "," + 0 + "\n");
							skipThisRTNoCandTimelines = true;
							break; // break out of iterative recommendation for each seq index
						}

						sbsNumOfCandTimelinesBelowThreshold.get(seqIndex)
								.append(dateToRecomm + "," + recommendationTimes[seqIndex] + "," + weekDay + ","
										+ thresholdAsDistance + "," + recommMaster.getNumOfCandTimelinesBelowThresh()
										+ "," + seqIndex + "\n");// bwNumOfCandTimelinesBelowThreshold.newLine();

						Sanity.gt(recommMaster.getRankedRecommendedActNamesWithoutRankScores().length(), 0,
								"Error in Sanity Check RT 800:recommMaster.getRankedRecommendedActivityNamesWithoutRankScores().length()<=0, but there are candidate timelines, i ="
										+ seqIndex);

						// disable for now as next activity after recomm is invalid is not used
						// and probably not set.
						// $$if (recommMaster.isNextActivityJustAfterRecommPointIsInvalid())
						// $${ bwNextActInvalid.write(userId + "," + endTimeStamp + "," + i + "\n");}

						/////////////////////////

						// issue here solved
						if (recommMasters[seqIndex] == null)
						{
							PopUps.printTracedErrorMsgWithExit(("Error: recommMasters[i] = null"));
						}

						if (VerbosityConstants.verbose)
						{
							System.out.println("recommMasters[i] .getRankedRecommendedActNamesWithoutRankScores()"
									+ recommMasters[seqIndex].getRankedRecommendedActNamesWithoutRankScores());
						}
						String[] splittedRankedRecommendedActName = RegexUtils.patternDoubleUnderScore
								.split(recommMasters[seqIndex].getRankedRecommendedActNamesWithoutRankScores());

						// PopUps.showMessage("splittedRankedRecommendedActName.length="
						// + splittedRankedRecommendedActName.length);
						// ArrayList<String> splittedRankedRecommendedActName = (ArrayList<String>)
						// Arrays
						// .asList(RegexUtils.patternDoubleUnderScore.split(recommMasters[i]
						// .getRankedRecommendedActNamesWithoutRankScores()));

						// PopUps.showMessage("here12_1");
						// $System.out.println("debug 645: splittedRankedRecommendedActName = "
						// $ + Arrays.asList(splittedRankedRecommendedActName));

						// get the top recommended activity names from previous recommendation
						topRecommendedPrimarDimensionVal[seqIndex] = splittedRankedRecommendedActName[1];
						// PopUps.showMessage("here12_2");
						ActivityObject2018 repAOForTopRecommActName = getRepresentativeAOForActName(
								Constant.preBuildRepAOGenericUser, Constant.collaborativeCandidates,
								Constant.buildRepAOJustInTime, mapOfRepAOs, mapOfMedianPreSuccDuration,
								repAOResultGenericUser, userId, recommendationTimes[seqIndex],
								topRecommendedPrimarDimensionVal[seqIndex], recommMaster, this.primaryDimension,
								commonPath);

						// PopUps.showMessage("here12_3");
						repAOsFromPrevRecomms.add(repAOForTopRecommActName);

						if (seqIndex < (recommSeqLength - 1))
						{
							recommendationTimes[seqIndex + 1] = repAOForTopRecommActName.getEndTimestamp();
						} // PopUps.showMessage("here12_4");
					} // END of loop over iterative recommendation for each seq index

					if (skipThisRTNoCandTimelines)
					{
						System.out.println("Skipping this RT:skipThisRTNoCandTimelines=" + skipThisRTNoCandTimelines);
						continue; // continue to the next RT
					}
					// end of curtain April 7
					System.out.println("time taken by recommMaster = " + (System.currentTimeMillis() - ta1) + " ms");
					// $$ Disabled for speed
					// $$ WritingToFile.appendLineToFileAbsolute(
					// $$ (System.currentTimeMillis() - ta1) + ","
					// $$ + PerformanceAnalytics.getUsedMemoryInMB() + "\n",
					// $$ Constant.getCommonPath() + "recommMasterPerformance.csv");

					System.out.println("Back to RecommendationTests: received following #of cand timelines for mu "
							+ matchingUnit);
					Arrays.stream(recommMasters)
							.forEachOrdered(rm -> System.out.print("__" + rm.getNumOfCandidateTimelines()));
					System.out.println();

					if (VerbosityConstants.WriteNumActsPerRTPerCand)
					{
						writeNumOfActsInEachCand(numActsInEachCandbw, userId, dateToRecomm, recommMasters,
								recommTimesStrings);
					}

					// double thresholdAsDistance = recommMasters[0].getThresholdAsDistance();
					if (recommMasters[0].hasThresholdPruningNoEffect() == false)
					{
						pruningHasSaturated = false;
					}

					// pruningHasSaturated=recommP1.hasThresholdPruningNoEffect();

					if (timeCategory.equalsIgnoreCase("Morning"))
					{
						numberOfMorningRTs++;
					}
					else if (timeCategory.equalsIgnoreCase("Afternoon"))
					{
						numberOfAfternoonRTs++;
					}
					else if (timeCategory.equalsIgnoreCase("Evening"))
					{
						numberOfEveningRTs++;
					}

					// target activity for recommendation
					String actActualDone = nextValidActivityObjectAfterRecommPoint1.getPrimaryDimensionVal("/");// .getActivityName();
					ArrayList<String> actsActualDoneInSeq = (ArrayList<String>) nextValidActivityObjectsAfterRecommPoint1
							.stream().map(ao -> ao.getPrimaryDimensionVal("/")).collect(Collectors.toList());

					// .stream().map(ao -> ao.getActivityName()).collect(Collectors.toList());

					String actsActualDoneInSeqString = nextValidActivityObjectsAfterRecommPoint1.stream()
							.map(ao -> ao.getPrimaryDimensionVal("/")).collect(Collectors.joining(">"));
					// .stream().map(ao -> ao.getActivityName()).collect(Collectors.joining(">"));
					// current activity
					String actsAtRecommPoint = Arrays.stream(recommMasters)
							.map(rm -> rm.getActivityObjectAtRecomm().getPrimaryDimensionVal("/"))
							.collect(Collectors.joining(">"));

					ArrayList<String> rankedRecommWithScoreForThisRTIter = (ArrayList<String>) Arrays
							.stream(recommMasters).map(rm -> rm.getRankedRecommendedActNamesWithRankScores())
							.collect(Collectors.toList());

					ArrayList<String> rankedRecommWithoutScoreForThisRTIter = (ArrayList<String>) Arrays
							.stream(recommMasters).map(rm -> rm.getRankedRecommendedActNamesWithoutRankScores())
							.collect(Collectors.toList());

					// extract the top 1 ranked recommendation from each recommendation master
					String topRankedRecommSequenceWithScore = rankedRecommWithScoreForThisRTIter.stream()
							.map(s -> RegexUtils.patternDoubleUnderScore.split(s)[1]).collect(Collectors.joining(">"));

					String topRankedRecommSequenceWithoutScore = rankedRecommWithScoreForThisRTIter.stream()
							.map(s -> RegexUtils.patternDoubleUnderScore.split(s)[1])
							.map(s -> RegexUtils.patternColon.split(s)[0]).collect(Collectors.joining(">"));

					if (VerbosityConstants.verbose)
					{
						System.out.println("** Ranked Recommendation=" + topRankedRecommSequenceWithScore
								+ ", while actual was=" + actsActualDoneInSeqString);// rankedRecommWithoutScoreForThisRT
						System.out.println(
								"topRankedRecommSequenceWithoutScore = " + topRankedRecommSequenceWithoutScore);
					}
					// metaBufferWriter.write(userId + "_" + dateToRecomm + "_" + endTimeString + ",");
					metaToWriteForThisUserDate.append(userId).append("_").append(dateToRecomm).append("_")
							.append(recommTimesStrings[0]).append(",");

					recommSequenceWithScoreForThisUserDate.append(topRankedRecommSequenceWithScore).append(",");
					recommSequenceWithoutScoreForThisUserDate.append(topRankedRecommSequenceWithoutScore).append(",");

					// dataActualToWriteForThisUserDateIter.append(actActualDone).append(",");
					dataActualSeqActsToWriteForThisUserDate.append(actsActualDoneInSeqString).append(",");

					for (int i = 0; i < this.recommSeqLength; i++)
					{
						// actualBufferWriter.write(actActualDone + ",");
						// write each actual done separately
						// IntStream.range(0, this.recommSeqLength).forEach(i ->
						// dataActualToWriteForThisUserDateIter.get(i).append(actsActualDoneInSeq.get(i)));
						if (VerbosityConstants.writeDataActualForEachSeqIndex)
						{
							sbsDataActualToWriteForThisUserDate.get(i).append(actsActualDoneInSeq.get(i)).append(",");
						}
						rankedRecommWithScoreToWriteForThisUserDate.get(i)
								.append(rankedRecommWithScoreForThisRTIter.get(i)).append(",");

						if (VerbosityConstants.writeRankedRecommsWOScoreForEachSeqIndex)
						{
							rankedRecommWithoutScoreToWriteForThisUserDate.get(i)
									.append(rankedRecommWithoutScoreForThisRTIter.get(i)).append(",");
						}

						// HERE
						sbsNumOfCandTimelinesForThisUserDate.get(i).append(userId + "," + recommendationTimes[i] + ","
								+ recommMasters[i].getNumOfCandTimelinesBelowThresh() + "\n");
						// TODO disabled for speed on 1 Nov 2017 .append(new
						// HashSet<>(recommMasters[i].getCandUserIDs().values())).append("|")
						// .append(new HashSet<>(recommMasters[i].getCandUserIDs().size()))

						/////////////////
						String[] splittedRecomm = RegexUtils.patternDoubleUnderScore
								.split(rankedRecommWithoutScoreForThisRTIter.get(i));

						sbsMaxNumOfDistinctRecommendations.get(i)
								.append(dateToRecomm + "," + recommendationTimes[i] + "," + weekDay + // UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())+
										"," + (splittedRecomm.length - 1) + ","
										+ recommMasters[i].getNumOfCandTimelinesBelowThresh() + "\n");

						if (VerbosityConstants.WriteRaw)
						{
							writeRawLogs(userName, dateToRecomm, weekDay, sbsRawToWriteForThisUserDate.get(i),
									timeCategory, recommMasters[i], recommTimesStrings[i], actsActualDoneInSeq.get(i),
									rankedRecommWithScoreForThisRTIter.get(i));
						}
					}

					// start of curtain isCurrentTargetActSame 11 May
					// $$ disabled becuase not essential at the moment
					// char isCurrentTargetActSame; if (actAtRecommPoint.equals(actActualDone))
					// isCurrentTargetActSame = 't'; else isCurrentTargetActSame = 'f';
					// metaIfCurrentTargetSameToWriteForThisUserDate.append(isCurrentTargetActSame).append(",");
					// end of curtain isCurrentTargetActSame 11 May

					/*
					 * ********************************************************************************* ***
					 */
					// LinkedHashMap<String, Long> activityNameCountPairsOverAllTrainingDays =
					// (LinkedHashMap<String, Long>) mapsForCountDurationBaselines
					// .get("activityNameCountPairsOverAllTrainingDays");
					// ComparatorUtils.assertNotNull(activityNameCountPairsOverAllTrainingDays);
					// String activityNameCountPairsOverAllTrainingDaysWithoutCount =
					// getActivityNameCountPairsOverAllTrainingDaysWithoutCount(
					// activityNameCountPairsOverAllTrainingDays);
					// baseLineOccurrence.write(activityNameCountPairsOverAllTrainingDaysWithoutCount +
					// ",");

					if (Constant.DoBaselineOccurrence)
					{
						baseLineOccurrenceToWriteForThisUserDate.append(actNamesCountsWithoutCountOverTrain)
								.append(",");
					}
					if (Constant.DoBaselineDuration)
					{
						baseLineDurationToWriteForThisUserDate.append(actNamesDurationsWithoutDurationOverTrain)
								.append(",");
					}

					/*
					 * ********************************************************************************* ***
					 */

					// curtain on 21 Mar 2017 start
					// + "," + recommP1.getRestAndEndSimilaritiesCorrelation() + ","
					// + recommP1.getAvgRestSimilarity() + "," + recommP1.getSDRestSimilarity()
					// + "," + recommP1.getAvgEndSimilarity() + ","
					// + recommP1.getSDEndSimilarity());// +","+recommP1.getActivitiesGuidingRecomm());
					// curtain on 21 Mar 2017 end

					// ActivityObject activityAtRecommPoint = recommP1.getActivityObjectAtRecomm();

					// Not writing this anymore as similar information is written by:
					// org.activity.io.WritingToFile.writeEditDistancesPerRtPerCand()
					// if (VerbosityConstants.WriteRecommendationTimesWithEditDistance)
					// { bwRecommTimesWithEditDistances.write(getRTsWithDistancesToWrite(dateToRecomm,
					// weekDay, endTimeStamp, recommP1, actActualDone, activityAtRecommPoint)); }
					System.out.println("// end of for loop over all activity objects in test date");
				} // end of for loop over all activity objects in test date, i.e., over all RTs

				metaBw.write(metaToWriteForThisUserDate.toString());
				recommSeqWithoutScoreBw.write(recommSequenceWithoutScoreForThisUserDate.toString());
				recommSeqWithScoreBw.write(recommSequenceWithScoreForThisUserDate.toString());
				actualSeqBw.write(dataActualSeqActsToWriteForThisUserDate.toString());

				if (VerbosityConstants.WriteTopNextActivitiesWithoutDistance)
				{
					topNextActsWithoutDistance.write(topNextActsWithoutDistToWriteForThisUserDate.toString());
				}

				if (VerbosityConstants.WriteTopNextActivitiesWithDistance)
				{
					topNextActsWithDistance.write(topNextActsWithDistToWriteForThisUserDate.toString());
				}

				if (Constant.DoBaselineOccurrence)
				{
					baseLineOccurrence.write(baseLineOccurrenceToWriteForThisUserDate.toString());
				}
				if (Constant.DoBaselineDuration)
				{
					baseLineDuration.write(baseLineDurationToWriteForThisUserDate.toString());
				}

				metaIfCurrentTargetSameWriter.write(metaIfCurrentTargetSameToWriteForThisUserDate.toString());

				for (int i = 0; i < this.recommSeqLength; i++)
				{
					if (VerbosityConstants.WriteRaw)
					{
						bwsRaw.get(i).write(sbsRawToWriteForThisUserDate.get(i).toString());
					}
					bwsRankedRecommWithScore.get(i)
							.write(rankedRecommWithScoreToWriteForThisUserDate.get(i).toString());
					if (VerbosityConstants.writeRankedRecommsWOScoreForEachSeqIndex)
					{
						bwRankedRecommWithoutScore.get(i)
								.write(rankedRecommWithoutScoreToWriteForThisUserDate.get(i).toString());
					}
					numOfCandidateTimelinesWriter.get(i).write(sbsNumOfCandTimelinesForThisUserDate.get(i).toString());

					if (VerbosityConstants.writeDataActualForEachSeqIndex)
					{
						bwsDataActual.get(i).write(sbsDataActualToWriteForThisUserDate.get(i).toString());
					}
				}

				// numOfCandidateTimelinesWriter.write(numOfCandidateTimelinesForThisUserDate.toString());
				System.out.println("/end of for loop over all test dates");
			} // end of for loop over all test dates

			// Start of Added on Jan 15 2018
			int totalNumOfRTs = (numberOfMorningRTs + numberOfAfternoonRTs + numberOfEveningRTs);
			if (totalNumOfRTs <= 0)
			{
				WToFile.appendLineToFileAbs(userName + "," + numberOfMorningRTs + "," + numberOfAfternoonRTs + ","
						+ numberOfEveningRTs + "," + totalNumOfRTs + "\n", commonPath + "UsersWithNoValidRTs.csv");
				continue; // continue to the next user
			}
			// End of Added on Jan 15 2018

			bwCountTimeCategoryOfRecomm.write(userName + "," + numberOfMorningRTs + "," + numberOfAfternoonRTs + ","
					+ numberOfEveningRTs + "," + (totalNumOfRTs) + "\n");

			numOfValidRTs.put(userId, totalNumOfRTs);

			// bwCountTimeCategoryOfRecomm.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings");

			// RecommendationMaster recommendationMaster=new RecommendationMaster(userTimelines,
			// userTrainingTimelines,userTestTimelines,dateAtRecomm,timeAtRecomm,userAtRecomm);
			metaBw.newLine();
			recommSeqWithoutScoreBw.newLine();
			recommSeqWithScoreBw.newLine();
			actualSeqBw.newLine();
			topNextActsWithoutDistance.newLine();
			topNextActsWithDistance.newLine();

			for (int i = 0; i < this.recommSeqLength; i++)
			{
				bwsRankedRecommWithScore.get(i).newLine();
				bwRankedRecommWithoutScore.get(i).newLine();
				bwsDataActual.get(i).newLine();
			}

			baseLineOccurrence.newLine();
			baseLineDuration.newLine();

			if (VerbosityConstants.WriteMaxNumberOfDistinctRecommendation)
			{
				WToFile.writeToNewFile(sbsMaxNumOfDistinctRecommendations.toString(),
						commonPath + userName + "MaxNumberOfDistinctRecommendation.csv");
				// bwMaxNumOfDistinctRecommendations.close();
			}

			if (VerbosityConstants.WriteRecommendationTimesWithEditDistance)
			{
				WToFile.writeToNewFile(sbsRecommTimesWithEditDistances.toString(),
						commonPath + userName + "RecommTimesWithEditDistance.csv");
				// bwRecommTimesWithEditDistances.close();
			}

			// if (matchingUnitIterator == 0)
			if (VerbosityConstants.WriteNumberOfCandidateTimelinesBelow)

			{// write it only for one mu since it will remain same over
				// mus and is only for stat purpose
				WToFile.writeToNewFile(sbsNumOfCandTimelinesBelowThreshold.toString(), commonPath + userName
						+ "numberOfCandidateTimelinesBelow" + typeOfThreshold + thresholdValue + ".csv");
			}
			// bwNumOfCandTimelinesBelowThreshold.close();
			System.out.println("//end of for over userID");
		} // end of for over userID
		numActsInEachCandbw.close();

		closeBWs(metaBw, recommSeqWithoutScoreBw, recommSeqWithScoreBw, actualSeqBw);
		closeBWs(topNextActsWithoutDistance, topNextActsWithDistance);
		closeBWs(rtsRejNoValidActAfterWriter, rtsRejWithNoCandsWriter, rtsRejWithNoCandsBelowThreshWriter,
				rtsRejWithNoDWButMUCandsCands, rtsRejBlackListedWriter);
		closeBWs(baseLineOccurrence, baseLineDuration);
		closeBWs(bwNumOfWeekendsInTraining, bwNumOfWeekendsInAll, bwCountTimeCategoryOfRecomm);
		closeBWs(metaIfCurrentTargetSameWriter, bwNextActInvalid);
		closeBWs(bwWriteNormalisationOfDistance, bwNumOfValidAOsAfterRTInDay);

		for (int i = 0; i < this.recommSeqLength; i++)
		{
			closeBWs(bwsRankedRecommWithScore.get(i), bwRankedRecommWithoutScore.get(i), bwsDataActual.get(i),
					bwsRaw.get(i), numOfCandidateTimelinesWriter.get(i));
		}

		// $$new Evaluation();

		System.out.println("Pruning has Saturated is :" + pruningHasSaturated);
		// String msg = "";

		if (pruningHasSaturated)
		{
			System.out.println("Pruning has Saturated");
			// PopUps.showMessage("Pruning has Saturated");
		}

		System.out
				.println("LookPastType: " + this.lookPastType + "Total Number of Timelines created for matching unit ("
						+ matchingUnit + ") =" + Timeline.getCountTimelinesCreatedUntilNow());
		System.out.println("ALL TESTS DONE for this matching unit");

		long recommTestsEndtime = System.currentTimeMillis();

		System.out.println("Time taken for executing Recommendation Tests for this matching unit ="
				+ (recommTestsEndtime - ctmu1) / 1000 + "seconds");
		WToFile.appendLineToFileAbs("MU=" + matchingUnit + ",time(s)=" + ((recommTestsEndtime - ctmu1) / 1000) + "\n",
				Constant.getOutputCoreResultsPath() + "RecommendationTestsTimeTaken.csv");
		// writeRepAOs(mapOfRepAOs, mapOfMedianPreSuccDuration, Constant.getCommonPath());

		consoleLogStream.close();
	}

	/**
	 * Closes multiple BufferedReaders
	 * 
	 * @param br1
	 * @param brs
	 * @throws IOException
	 */
	public static void closeBWs(BufferedWriter br1, BufferedWriter... brs) throws IOException
	{
		br1.close();
		for (BufferedWriter br : brs)
		{
			br.close();
		}
	}

	/**
	 * 
	 * @param preBuildRepAOGenericUser
	 * @param collaborativeCandidates
	 * @param buildRepAOJustInTime
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDuration
	 * @param repAOResultGenericUser
	 * @param userId
	 * @param recommendationTime
	 * @param topRecommendedPrimarDimensionVal
	 * @param recommMaster
	 * @param primaryDimension
	 * @return
	 */
	private ActivityObject2018 getRepresentativeAOForActName(boolean preBuildRepAOGenericUser,
			boolean collaborativeCandidates, boolean buildRepAOJustInTime,
			LinkedHashMap<Integer, LinkedHashMap<Integer, ActivityObject2018>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<Integer, Pair<Double, Double>>> mapOfMedianPreSuccDuration,
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultGenericUser,
			int userId, Timestamp recommendationTime, String topRecommendedPrimarDimensionVal,
			RecommendationMasterI recommMaster, PrimaryDimension primaryDimension, String commonPath)
	{
		ActivityObject2018 repAOForTopRecommActName = null;

		if (preBuildRepAOGenericUser)
		{
			// disabled on 9 Feb 2018 start
			// repAOForTopRecommActName =
			// getRepresentativeAOCollGeneric(Integer.valueOf(topRecommendedPrimarDimensionVal),
			// userId, recommendationTime, primaryDimension, repAOResultGenericUser);
			// disabled on 9 Feb 2018 end

			repAOForTopRecommActName = getRepresentativeAOCollGenericV2(
					Integer.valueOf(topRecommendedPrimarDimensionVal), userId, recommendationTime, primaryDimension,
					repAOResultGenericUser);

			// Sanity check Feb8 Starts
			if (false)
			{
				ActivityObject2018 aoTemp = getRepresentativeAOCollGenericV2(
						Integer.valueOf(topRecommendedPrimarDimensionVal), userId, recommendationTime, primaryDimension,
						repAOResultGenericUser);
				WToFile.appendLineToFileAbs(
						repAOForTopRecommActName.toStringAllGowallaTS() + "\n" + aoTemp.toStringAllGowallaTS() + "\n\n",
						commonPath + "getRepresentativeAOCollGenericV2SanityCheck.txt");
				// SANITY CHECK PASSED on Feb 9 2018
			}
			// Sanity check Feb8 Ends
		}
		else
		{
			if (collaborativeCandidates && buildRepAOJustInTime)
			{
				// for just-in-time computation of representative activity object
				repAOForTopRecommActName = getRepresentativeAOColl(Integer.valueOf(topRecommendedPrimarDimensionVal),
						userId, recommendationTime, primaryDimension, recommMaster);
			}
			else
			{
				repAOForTopRecommActName = getRepresentativeAO(Integer.valueOf(topRecommendedPrimarDimensionVal),
						mapOfRepAOs, mapOfMedianPreSuccDuration, userId, recommendationTime, primaryDimension);
				// also use this for collaborative approach when using prebuilt
				// representative AOs created from training timelines of other users.
			}
		}
		return repAOForTopRecommActName;
	}

	/**
	 * Return a new ArrayList with 'recommSeqLength number of new StringBuilders
	 * 
	 * @param recommSeqLength
	 * @return
	 */
	private static ArrayList<StringBuilder> createListOfStringBuilders(int recommSeqLength)
	{
		ArrayList<StringBuilder> sbsRawToWriteForThisUserDate = new ArrayList<>(recommSeqLength);
		IntStream.range(0, recommSeqLength).forEachOrdered(i -> sbsRawToWriteForThisUserDate.add(new StringBuilder()));
		return sbsRawToWriteForThisUserDate;
	}

	/**
	 * 
	 * @param numActsInEachCandbw
	 * @param userId
	 * @param dateToRecomm
	 * @param recommMasters
	 * @param recommTimesStrings
	 * @throws IOException
	 */
	private void writeNumOfActsInEachCand(BufferedWriter numActsInEachCandbw, int userId, String dateToRecomm,
			RecommendationMasterI[] recommMasters, String[] recommTimesStrings) throws IOException
	{
		StringBuilder tmpWriter = new StringBuilder();
		// System.out.println("\tIterating over candidate timelines:");
		for (int i = 0; i < recommMasters.length; i++)
		{
			RecommendationMasterI recommMaster = recommMasters[i];
			for (Timeline candtt1 : recommMaster.getOnlyCandidateTimeslines())// candidateTimelines.entrySet())
			{
				// Timeline candtt1 = entryAjooba.getValue(); ArrayList<ActivityObject>
				// aa1=candtt1.getActivityObjectsInTimeline();
				// excluding the current activity at the end of the candidate timeline
				// System.out.println("Number of activity Objects in this timeline
				// (except the end current activity) is: "+sizez1);
				tmpWriter = StringUtils.fCat(tmpWriter, String.valueOf(candtt1.countNumberOfValidActivities() - 1), ",",
						candtt1.getTimelineID(), ",", Integer.toString(userId), ",", dateToRecomm, ",",
						recommTimesStrings[i], ",", candtt1.getActivityObjectNamesWithTimestampsInSequence(), ",");
			}
		}
		numActsInEachCandbw.write(tmpWriter.toString() + "\n");
	}

	/**
	 * Write some raw data/logs to Raw.csv
	 * 
	 * @param userName
	 * @param dateToRecomm
	 * @param weekDay
	 * @param sbsRawToWriteForThisUserDate
	 * @param timeCategory
	 * @param recommMaster
	 * @param recommTimesString
	 * @param actsActualDoneInSeq
	 * @param rankedRecommWithScoreForThisRTIter
	 */
	private void writeRawLogs(String userName, String dateToRecomm, String weekDay,
			StringBuilder sbsRawToWriteForThisUserDate, String timeCategory, RecommendationMasterI recommMaster,
			String recommTimesString, String actActualDoneInSeq, String rankedRecommWithScoreForThisRTIter)
	{
		String currentActName = recommMaster.getActivityObjectAtRecomm().getActivityName();
		sbsRawToWriteForThisUserDate.append(userName + "," + dateToRecomm + "," + recommTimesString + "," + timeCategory
				+ "," + recommMaster.getActivityNamesGuidingRecomm()/* withTimestamps */
				+ "," + currentActName + "," + Integer.toString(recommMaster.getNumOfValidActsInActsGuidingRecomm())
				+ "," + Integer.toString(recommMaster.getNumOfActsInActsGuidingRecomm()) + ","
				+ Integer.toString(recommMaster.getNumOfCandTimelinesBelowThresh()) + "," + weekDay + ","
				+ actActualDoneInSeq + "," + rankedRecommWithScoreForThisRTIter + ","
				+ Integer.toString(recommMaster.getNumOfDistinctRecommendations()) + ",,,,,,"
				// recommMasters[2].getActivityNamesGuidingRecomm() ,
				// recommP1.getRestAndEndSimilaritiesCorrelation() , "," ,
				// recommP1.getAvgRestSimilarity() , "," ,
				// recommP1.getSDRestSimilarity() , "," ,
				// recommP1.getAvgEndSimilarity()
				// , "," , recommP1.getSDEndSimilarity()
				+ Boolean.toString((currentActName.equals(actActualDoneInSeq))) + "\n");// ,",",recommP1.getActivitiesGuidingRecomm());
	}

	/**
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @return
	 */
	private final static LinkedHashMap<String, Timeline> getContinousTrainingTimelines(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW)
	{
		LinkedHashMap<String, Timeline> trainTimelineForAllUsers = new LinkedHashMap<>();

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsersDW
				.entrySet())
		{
			LinkedHashMap<Date, Timeline> trainingTimelineForThisUserDate = trainTestForAUser.getValue().get(0);

			Timeline trainingTimelineForThisUser = TimelineTransformers
					.dayTimelinesToATimeline(trainingTimelineForThisUserDate, false, true);
			// convert datetime to continouse timeline
			trainTimelineForAllUsers.put(trainTestForAUser.getKey(), trainingTimelineForThisUser);
		}

		return trainTimelineForAllUsers;
	}

	/**
	 * Take only recent numOfRecentDays days for each user
	 * 
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @param numOfRecentDays
	 * @return
	 */
	private final static LinkedHashMap<String, Timeline> getContinousTrainingTimelinesWithFilterByRecentDays(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW,
			int numOfRecentDays)
	{
		LinkedHashMap<String, Timeline> trainTimelineForAllUsers = new LinkedHashMap<>();

		StringBuilder sb = new StringBuilder("Debug 10 Dec\t");

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsersDW
				.entrySet())
		{
			LinkedHashMap<Date, Timeline> trainingTimelineForThisUserDate = trainTestForAUser.getValue().get(0);

			TreeMap<Date, Timeline> trainingTimelineForThisUserDateDescByDate = new TreeMap<Date, Timeline>(
					Collections.reverseOrder());

			trainingTimelineForThisUserDateDescByDate.putAll(trainingTimelineForThisUserDate);

			LinkedHashMap<Date, Timeline> filteredDayTrainingTimelineForThisUser = trainingTimelineForThisUserDateDescByDate
					.entrySet().stream().limit(numOfRecentDays)
					.collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

			Timeline filteredTrainingTimelineForThisUser = TimelineTransformers
					.dayTimelinesToATimeline(filteredDayTrainingTimelineForThisUser, false, true);
			// convert datetime to continouse timeline
			trainTimelineForAllUsers.put(trainTestForAUser.getKey(), filteredTrainingTimelineForThisUser);

			// start of debug print
			sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. original day timelines:");
			trainingTimelineForThisUserDate.entrySet().stream().forEachOrdered(e -> sb
					.append("\n" + e.getKey() + "--" + e.getValue().getActivityObjectNamesWithTimestampsInSequence()));
			sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. sorted day timelines:");
			trainingTimelineForThisUserDateDescByDate.entrySet().stream().forEachOrdered(e -> sb
					.append("\n" + e.getKey() + "--" + e.getValue().getActivityObjectNamesWithTimestampsInSequence()));
			sb.append("\\n-->User =" + trainTestForAUser.getKey() + "\n. filtered day timelines:");
			sb.append("\n" + filteredTrainingTimelineForThisUser.getActivityObjectNamesWithTimestampsInSequence());
			System.out.println(sb.toString() + "\n--\n");
			// end of debug print

		}

		return trainTimelineForAllUsers;
	}

	/**
	 * Take only recent numOfRecentDays days for each user
	 * 
	 * <p>
	 * Sanity checked
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @param numOfRecentDays
	 * @return
	 */
	private final static LinkedHashMap<String, Timeline> getContinousTrainingTimelinesWithFilterByRecentDaysV2(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW,
			int numOfRecentDays)
	{
		LinkedHashMap<String, Timeline> trainTimelineForAllUsers = new LinkedHashMap<>();
		System.out.println("Filtering by recent " + numOfRecentDays + " Days");
		StringBuilder sb = new StringBuilder("Debug 10 Dec\t");

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsersDW
				.entrySet())
		{
			LinkedHashMap<Date, Timeline> trainingTimelineForThisUserDate = trainTestForAUser.getValue().get(0);

			// Get most recent numOfRecentDays dates
			List<Date> datesList = new ArrayList<>();
			datesList.addAll(trainingTimelineForThisUserDate.keySet());
			Collections.sort(datesList, Collections.reverseOrder());
			Set<Date> setOfSelectedDatesForThisUser = datesList.stream().limit(numOfRecentDays)
					.collect(Collectors.toSet());

			// filter by date in setOfSelectedDatesForThisUser
			LinkedHashMap<Date, Timeline> filteredDayTrainingTimelineForThisUser = trainingTimelineForThisUserDate
					.entrySet().stream().filter(e -> setOfSelectedDatesForThisUser.contains(e.getKey()))
					.collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

			// convert datetime to continouse timeline
			Timeline filteredTrainingTimelineForThisUser = TimelineTransformers
					.dayTimelinesToATimeline(filteredDayTrainingTimelineForThisUser, false, true);

			trainTimelineForAllUsers.put(trainTestForAUser.getKey(), filteredTrainingTimelineForThisUser);

			// start of debug print
			if (false)
			{
				sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. original day timelines: size = "
						+ trainingTimelineForThisUserDate.size());
				trainingTimelineForThisUserDate.entrySet().stream().forEachOrdered(e -> sb.append(
						"\n" + e.getKey() + "--" + e.getValue().getActivityObjectNamesWithTimestampsInSequence()));
				sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. selected dates:");
				setOfSelectedDatesForThisUser.stream().forEachOrdered(e -> sb.append("\n" + e.toString() + "--"));
				sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. filtered day timelines: size= "
						+ filteredTrainingTimelineForThisUser.size());
				sb.append("\n" + filteredTrainingTimelineForThisUser.getActivityObjectNamesWithTimestampsInSequence());
				System.out.println(sb.toString() + "\n--\n");
			}
			// end of debug print

		}

		return trainTimelineForAllUsers;
	}

	/**
	 * 
	 * @param trainTestTimelinesForAllUsers
	 * @param userId
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 * @return
	 */
	private Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> prebuildRepresentativeActivityObjects(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers, int userId,
			LinkedHashMap<Date, Timeline> userTrainingTimelines, LinkedHashMap<Date, Timeline> userTestTimelines)
	{
		Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResult = null;

		// if (Constant.collaborativeCandidates)
		// { repAOResult = buildRepresentativeAOsForUserPDVCOll(userId, trainTestTimelinesForAllUsers,
		// Constant.getUniqueLocIDs(), Constant.getUniqueActivityIDs()); } else {
		repAOResult = buildRepresentativeAOsForUserPDV2(userId,
				TimelineTransformers.dayTimelinesToATimeline(userTrainingTimelines, false, true),
				TimelineTransformers.dayTimelinesToATimeline(userTestTimelines, false, true),
				Constant.getUniqueLocIDs(), Constant.getUniqueActivityIDs());

		// Start of Sanity Check for buildRepresentativeAOsForUserPD()
		if (VerbosityConstants.checkSanityPDImplementn && Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
		{
			// for Activity ID as primary dimension, the output of
			// buildRepresentativeAOsForUserPD and buildRepresentativeAOsForUser should be same
			// (except data type)
			Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> repAOResultActName = buildRepresentativeAOsForUser(
					userId, TimelineTransformers.dayTimelinesToATimeline(userTrainingTimelines, false, true),
					Constant.getActivityNames(),
					TimelineTransformers.dayTimelinesToATimeline(userTestTimelines, false, true));
			Sanity.compareOnlyNonEmpty(repAOResult, repAOResultActName);
		}
		// end of Sanity Check for buildRepresentativeAOsForUserPD()
		// }
		return repAOResult;
	}

	/**
	 * 
	 * @param userId
	 * @param trainTestTimelinesForAllUsers
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @return
	 */
	private Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsForUserPDVCOll(
			int userId, LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			Set<Integer> uniqueLocIDs, Set<Integer> uniqueActivityIDs)
	{
		// BookMark
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeAOsForUserPDV2 for user " + userId);
		LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = null;
		LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDuration = null;
		LinkedHashMap<String, Timeline> collTrainingTimelines = new LinkedHashMap<>();

		long t1 = System.currentTimeMillis();
		try
		{
			if (this.databaseName.equals("gowalla1") == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: database is  not gowalla1:" + this.databaseName);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }

			else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDVal = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDVal = new LinkedHashMap<>();

				// earlier version was using all possible vals fro PD but now using only those in training data.
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsers
						.entrySet())
				{
					int userIdCursor = Integer.valueOf(trainTestForAUser.getKey());
					if (userIdCursor != userId)
					{
						Timeline userTrainingTimeline = TimelineTransformers
								.dayTimelinesToATimeline(trainTestForAUser.getValue().get(0), false, true);

						for (ActivityObject2018 ao : userTrainingTimeline.getActivityObjectsInTimeline())
						{
							distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());
						}
						collTrainingTimelines.put(trainTestForAUser.getKey(), userTrainingTimeline);
					}
				}

				System.out.println("distinctPDValsEncounteredInCOllTraining.size() = "
						+ distinctPDValsEncounteredInTraining.size());

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : distinctPDValsEncounteredInTraining)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDVal.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				int countAOs = 0;
				for (Entry<String, Timeline> trainingTimelineEntry : collTrainingTimelines.entrySet())
				{
					String userID = trainingTimelineEntry.getKey();

					// long durationFromPreviousInSecs = 0;
					long prevTimestamp = 0;
					Set<Integer> prevPDValEncountered = null;

					for (ActivityObject2018 ao : trainingTimelineEntry.getValue().getActivityObjectsInTimeline())
					{
						countAOs += 1;
						Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
						for (Integer pdVal : uniquePdValsInAO)
						{
							// add this act object to the correct map entry in map of aos for given act names
							ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
							if (aosStored == null)
							{
								PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
										+ "' is not in the list of pd vals from training data");
							}
							aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
						}

						// store the preceeding and succeeding durations
						long currentTimestamp = ao.getStartTimestamp().getTime();

						if (prevTimestamp != 0)
						{ // add the preceeding duration for this AO
							for (Integer pdVal : uniquePdValsInAO)
							{
								durationFromPrevForEachPDVal.get(pdVal).add(currentTimestamp - prevTimestamp);
							}
						}

						if (prevPDValEncountered != null)
						{
							// add the succeeding duration for the previous AO
							for (Integer prevPDVal : prevPDValEncountered)
							{
								durationFromNextForEachPDVal.get(prevPDVal).add(currentTimestamp - prevTimestamp);
							}
						}

						prevTimestamp = currentTimestamp;
						prevPDValEncountered = uniquePdValsInAO;
					}
				}
				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					// userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
					// .print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = countAOs;// userTrainingTimelines.getActivityObjectsInTimeline().stream()
				// .count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserPDV2(
						userId, aosForEachPDVal, durationFromPrevForEachPDVal, durationFromNextForEachPDVal);

				repAOsForThisUser = result.getFirst();
				actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsForUserPDV2 for user " + userId + " time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOsForThisUser, actMedianPreSuccDuration);

	}

	/**
	 * 
	 * @param userId
	 * @param trainTestTimelinesForAllUsers
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @return
	 */
	private Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsAllUsersPDVCOllAllUsers(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			Set<Integer> uniqueLocIDs, Set<Integer> uniqueActivityIDs)
	{
		// BookMark
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeAOsAllUsersPDVCOllAllUsers using all users ");
		// LinkedHashMap<Integer, ActivityObject> repAOsForThisUser = null;
		// LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDuration = null;
		Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = null;
		LinkedHashMap<String, Timeline> collTrainingTimelines = new LinkedHashMap<>();

		long t1 = System.currentTimeMillis();
		try
		{
			if (this.databaseName.equals("gowalla1") == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: database is  not gowalla1:" + this.databaseName);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }

			else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Could be useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDVal = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDVal = new LinkedHashMap<>();

				// earlier version was using all possible vals fro PD but now using only those in training data.
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsers
						.entrySet())
				{
					Timeline userTrainingTimeline = TimelineTransformers
							.dayTimelinesToATimeline(trainTestForAUser.getValue().get(0), false, true);

					for (ActivityObject2018 ao : userTrainingTimeline.getActivityObjectsInTimeline())
					{
						distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());
					}
					collTrainingTimelines.put(trainTestForAUser.getKey(), userTrainingTimeline);
				}

				System.out.println("distinctPDValsEncounteredInCOllTraining.size() = "
						+ distinctPDValsEncounteredInTraining.size());

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : distinctPDValsEncounteredInTraining)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDVal.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				int countAOs = 0;
				for (Entry<String, Timeline> trainingTimelineEntry : collTrainingTimelines.entrySet())
				{
					String userID = trainingTimelineEntry.getKey();

					// long durationFromPreviousInSecs = 0;
					long prevTimestamp = 0;
					Set<Integer> prevPDValEncountered = null;

					for (ActivityObject2018 ao : trainingTimelineEntry.getValue().getActivityObjectsInTimeline())
					{
						countAOs += 1;
						Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
						for (Integer pdVal : uniquePdValsInAO)
						{
							// add this act object to the correct map entry in map of aos for given act names
							ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
							if (aosStored == null)
							{
								PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
										+ "' is not in the list of pd vals from training data");
							}
							aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
						}

						// store the preceeding and succeeding durations
						long currentTimestamp = ao.getStartTimestamp().getTime();

						if (prevTimestamp != 0)
						{ // add the preceeding duration for this AO
							for (Integer pdVal : uniquePdValsInAO)
							{
								durationFromPrevForEachPDVal.get(pdVal).add(currentTimestamp - prevTimestamp);
							}
						}

						if (prevPDValEncountered != null)
						{
							// add the succeeding duration for the previous AO
							for (Integer prevPDVal : prevPDValEncountered)
							{
								durationFromNextForEachPDVal.get(prevPDVal).add(currentTimestamp - prevTimestamp);
							}
						}

						prevTimestamp = currentTimestamp;
						prevPDValEncountered = uniquePdValsInAO;
					}
				}
				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					// userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
					// .print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = countAOs;// userTrainingTimelines.getActivityObjectsInTimeline().stream()
				// .count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				result = computeRepresentativeActivityObjectForUserPDV2GenericUser(aosForEachPDVal,
						durationFromPrevForEachPDVal, durationFromNextForEachPDVal);

				// repAOsForThisUser = result.getFirst();
				// actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsAllUsersPDVCOllAllUsers for all users  time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return result;

	}

	/**
	 * 
	 * @param userId
	 * @param databaseName
	 * @return
	 */
	private static boolean isBlackListedUser(int userId)
	{
		boolean isBlackListed = false;
		if (Constant.getDatabaseName().equals("gowalla1"))
		{
			if (Arrays.asList(DomainConstants.gowallaUserIDsWithGT553MaxActsPerDay).contains(userId))
			{
				System.out.println("Skipping user: " + userId + " as in gowallaUserIDsWithGT553MaxActsPerDay");
				// PopUps.showMessage("Skipping user: " + userId + " as in gowallaUserIDsWithGT553MaxActsPerDay");
				isBlackListed = true;
			}
		}
		else
		{
			System.err.println("Warning: Constant.blacklistingUsersWithLargeMaxActsPerDay= "
					+ Constant.blacklistingUsersWithLargeMaxActsPerDay
					+ " but blacklisted user not defined for this database");
		}
		return isBlackListed;
	}

	/**
	 * 
	 * @param matchingUnit
	 * @param lookPastType
	 * @param outputCoreResultsPath
	 */
	private static String computeCommonPath(double matchingUnit, Enums.LookPastType lookPastType,
			String outputCoreResultsPath)
	{
		String commonPath = null;
		// matching unit is only relevant if it is not daywise
		if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
		{
			String dirToCreate = outputCoreResultsPath + "/MatchingUnit" + String.valueOf(matchingUnit);
			WToFile.createDirectory(dirToCreate);// Creating the directory for that matching unit
			commonPath = dirToCreate + "/";
		}
		else // daywise //baseline closest time
		{
			commonPath = outputCoreResultsPath;
		}
		return commonPath;
	}

	/**
	 * 
	 * @param matchingUnit
	 * @param lookPastType
	 * @param outputCoreResultsPath
	 */
	private static String computeCommonPath(double matchingUnit, Enums.LookPastType lookPastType,
			String outputCoreResultsPath, int thresholdVal)
	{
		String commonPath = null;
		// matching unit is only relevant if it is not daywise
		if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
		{
			String dirToCreate;
			if (Constant.useiiWASThreshold)
			{
				dirToCreate = outputCoreResultsPath + thresholdVal + "/MatchingUnit" + String.valueOf(matchingUnit);
				WToFile.createDirectory(outputCoreResultsPath + thresholdVal);
				WToFile.createDirectory(
						outputCoreResultsPath + thresholdVal + "/MatchingUnit" + String.valueOf(matchingUnit));
			}
			else
			{
				dirToCreate = outputCoreResultsPath + "/MatchingUnit" + String.valueOf(matchingUnit);
				WToFile.createDirectory(outputCoreResultsPath + "/MatchingUnit" + String.valueOf(matchingUnit));

			}

			// Creating the directory for that matching unit
			commonPath = dirToCreate + "/";
		}
		else // daywise //baseline closest time
		{
			commonPath = outputCoreResultsPath;
		}
		return commonPath;
	}

	/**
	 * 
	 * @param topRecommActName
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDuration
	 * @param userId
	 * @param recommendationTime
	 * @return
	 */
	private ActivityObject2018 getRepresentativeAO(String topRecommActName,
			LinkedHashMap<Integer, LinkedHashMap<String, ActivityObject2018>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<String, Pair<Double, Double>>> mapOfMedianPreSuccDuration, int userId,
			Timestamp recommendationTime)
	{
		ActivityObject2018 repAO = mapOfRepAOs.get(userId).get(topRecommActName);
		if (repAO == null)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getRepresentativeAO: topRecommActName:" + topRecommActName + " not found in mapOfRepAo"));
			System.exit(-1);
		}

		double medianPreceedingDuration = mapOfMedianPreSuccDuration.get(userId).get(topRecommActName).getFirst();
		if (medianPreceedingDuration <= 0)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getRepresentativeAO: medianPreceedingDuration = " + medianPreceedingDuration));
			System.exit(-1);
		}

		long recommTime = recommendationTime.getTime();

		Timestamp newRecommTimestamp = new Timestamp((long) (recommTime + medianPreceedingDuration));

		if (!DateTimeUtils.isSameDate(recommendationTime, newRecommTimestamp))
		{
			System.err.println("recommendationTime = " + recommendationTime + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
					+ " for topRecommActName =" + topRecommActName);
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug: getRepresentativeAO: old recommendationTime="
					+ recommendationTime.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDuration + "  new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		repAO.setEndTimestamp(newRecommTimestamp); // only end timestamp used, make sure there is no start timestamp,
		// since we are making recommendation at end timestamps and gowalla act objs are points in time.
		repAO.setStartTimestamp(newRecommTimestamp);

		// {
		// System.err.println("Debug1357: inside getRepresentativeAO: recommendationTime = " + recommendationTime
		// + " newRecommTimestamp= " + newRecommTimestamp + ". medianPreceedingDuration = "
		// + medianPreceedingDuration + " for topRecommActName =" + topRecommActName);
		// System.out.println("repAO = " + repAO.toStringAllGowallaTS());
		// }

		return repAO;
	}

	//
	/**
	 * 
	 * @param topPrimaryDimensionVal
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDuration
	 * @param userId
	 * @param recommendationTime
	 * @param primaryDimension
	 * @return
	 * @since 14 July 2017
	 */
	private ActivityObject2018 getRepresentativeAO(Integer topPrimaryDimensionVal,
			LinkedHashMap<Integer, LinkedHashMap<Integer, ActivityObject2018>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<Integer, Pair<Double, Double>>> mapOfMedianPreSuccDuration, int userId,
			Timestamp recommendationTime, PrimaryDimension primaryDimension)
	{
		ActivityObject2018 repAO = mapOfRepAOs.get(userId).get(topPrimaryDimensionVal);
		if (repAO == null)
		{
			PopUps.printTracedErrorMsgWithExit("Error in getRepresentativeAO: topRecommActName:"
					+ topPrimaryDimensionVal + " not found in mapOfRepAo");
		}

		double medianPreceedingDuration = mapOfMedianPreSuccDuration.get(userId).get(topPrimaryDimensionVal).getFirst();
		if (medianPreceedingDuration <= 0)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error in getRepresentativeAO: medianPreceedingDuration = " + medianPreceedingDuration);
		}

		long recommTime = recommendationTime.getTime();

		Timestamp newRecommTimestamp = new Timestamp((long) (recommTime + medianPreceedingDuration));

		if (!DateTimeUtils.isSameDate(recommendationTime, newRecommTimestamp))
		{
			System.err.println("recommendationTime = " + recommendationTime + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
					+ " for topRecommActName =" + topPrimaryDimensionVal);
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug: getRepresentativeAO: old recommendationTime="
					+ recommendationTime.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDuration + "  new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		repAO.setEndTimestamp(newRecommTimestamp); // only end timestamp used, make sure there is no start timestamp,
		// since we are making recommendation at end timestamps and gowalla act objs are points in time.
		repAO.setStartTimestamp(newRecommTimestamp);

		// {
		// System.err.println("Debug1357: inside getRepresentativeAO: recommendationTime = " + recommendationTime
		// + " newRecommTimestamp= " + newRecommTimestamp + ". medianPreceedingDuration = "
		// + medianPreceedingDuration + " for topRecommActName =" + topRecommActName);
		// System.out.println("repAO = " + repAO.toStringAllGowallaTS());
		// }

		return repAO;
	}

	//
	/**
	 * Created to compute just-in-time rep AO when only one cand timeline (most recent one) is taken from other users.
	 * Otherwise, if there are more cands, then probably precomputation is better than just in time as in here.
	 * 
	 * @param topPrimaryDimensionVal
	 * @param userId
	 * @param recommendationTime
	 * @param primaryDimension
	 * @param recommMaster
	 * @return
	 * @since 1 August 2017
	 */
	private ActivityObject2018 getRepresentativeAOColl(Integer topPrimaryDimensionVal, int userId,
			Timestamp recommendationTime, PrimaryDimension primaryDimension, RecommendationMasterI recommMaster)
	{
		// StringBuilder verboseMsg = new StringBuilder();
		// System.out.println("Inside getRepresentativeAOColl(): topPrimaryDimensionVal=" + topPrimaryDimensionVal +
		// "\n");

		ArrayList<Long> durationPreceeding = new ArrayList<>();
		ArrayList<ActivityObject2018> aosWithSamePDVal = new ArrayList<>();

		// iterate over the candidate timelines (which are to be used to create representative activity objects)
		// System.out.println("#cands = " + recommMaster.getCandidateTimelineIDs().size());
		for (String candID : recommMaster.getCandidateTimelineIDs())
		{
			TimelineWithNext candTimeline = (TimelineWithNext) recommMaster.getCandidateTimeline(candID);
			// candTimeline.printActivityObjectNamesWithTimestampsInSequence();
			// System.out.println("next act=" + candTimeline.getNextActivityObject().toStringAllGowallaTS());
			ArrayList<ActivityObject2018> actObjs = candTimeline.getActivityObjectsInTimeline();
			actObjs.add(candTimeline.getNextActivityObject());

			// note cand will contain MU+1 acts, +1 next act
			// not always true, sometimes cand will have less aos
			// $$Sanity.eq(actObjs.size(), Constant.getCurrentMatchingUnit() + 2, "actObjs.size():" + actObjs.size()
			// $$ + "!= Constant.getCurrentMatchingUnit():" + Constant.getCurrentMatchingUnit());

			// System.out.println("actObjs.size():" + actObjs.size() + "!= Constant.getCurrentMatchingUnit():"
			// + Constant.getCurrentMatchingUnit());

			long prevTS = -99, currentTS = -99;
			for (ActivityObject2018 ao : actObjs)
			{
				currentTS = ao.getStartTimestamp().getTime();

				// contains the act name we are looking for
				if (ao.getPrimaryDimensionVal().contains(topPrimaryDimensionVal))
				{
					aosWithSamePDVal.add(ao);

					if (prevTS > 0)
					{
						durationPreceeding.add(currentTS - prevTS);
					}
				}
				prevTS = currentTS;
			}
			// System.out.println("");
		}

		// StringBuilder sb = new StringBuilder();
		// System.out.println("durationPreceeding= " + durationPreceeding.toString());
		// System.out.println("aosWithSamePDVal= ");
		// aosWithSamePDVal.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
		// System.out.println(sb.toString());

		double medianPreceedingDuration = StatsUtils.getDescriptiveStatisticsLong(durationPreceeding,
				"durationPreceeding", userId + "__" + topPrimaryDimensionVal + "durationPreceeding.txt", false)
				.getPercentile(50);

		// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
		// int medianCinsCount = (int) StatsUtils
		// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
		// .getPercentile(50);

		// NOTE: we only need to take care of feature which are used for edit distance computation.
		// Instantiate the representative activity object.
		int activityID = -1;
		ArrayList<Integer> locationIDs = new ArrayList<>();
		String activityName = "";
		// String locationName = "";
		String workingLevelCatIDs = "";

		switch (primaryDimension)
		{
		case ActivityID:
		{
			activityID = topPrimaryDimensionVal;
			if (Constant.getDatabaseName().equals("gowalla1"))
			{// for gowalla dataset, act id and act name are same
				activityName = String.valueOf(topPrimaryDimensionVal);
				workingLevelCatIDs = topPrimaryDimensionVal + "__";
			}
			else
			{
				PopUps.printTracedErrorMsgWithExit("Error: not implemented this for besides gowalla1");
			}

			break;
		}
		case LocationID:
		{
			locationIDs.add(topPrimaryDimensionVal);
			workingLevelCatIDs = topPrimaryDimensionVal + "__";
			// locationName =
			// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
			// if (locationName == null || locationName.length() == 0)
			// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
			break;
		}
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown primaryDimension = " + Constant.primaryDimension);
			break;
		}

		Timestamp newRecommTimestamp = new Timestamp((long) (recommendationTime.getTime() + medianPreceedingDuration));

		ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
				activityName, "", newRecommTimestamp, "", "", "", String.valueOf(userId), -1, -1, -1, -1, -1, -1, -1,
				workingLevelCatIDs, -1, -1, new String[] { "" });

		if (!DateTimeUtils.isSameDate(recommendationTime, newRecommTimestamp))
		{
			System.out.print("Warning: recommendationTime = " + recommendationTime + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
					+ " for topRecommActName =" + topPrimaryDimensionVal);
		}
		//
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug: getRepresentativeAO: old recommendationTime="
					+ recommendationTime.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDuration + " new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		// System.out.println("repAO=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());

		return repAOForThisActNameForThisUser;
	}
	//

	//
	/**
	 * Created to create rep AO from repAOResultGenericUser which has been created using training timelines of all
	 * users. (need to call only once for a test)
	 * 
	 * 
	 * @param topPrimaryDimensionVal
	 * @param userId
	 * @param recommendationTime
	 * @param primaryDimension
	 * @param repAOResultGenericUser
	 *            {ActID,RepAO}, {ActID,{medianDurFromPrevForEachActName, medianDurFromNextForEachActName}}
	 * @return
	 */
	private ActivityObject2018 getRepresentativeAOCollGeneric(Integer topPrimaryDimensionVal, int userId,
			Timestamp recommendationTime, PrimaryDimension primaryDimension,
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultGenericUser)
	{
		// StringBuilder verboseMsg = new StringBuilder();
		// System.out.println("Inside getRepresentativeAOColl(): topPrimaryDimensionVal=" + topPrimaryDimensionVal +
		// "\n");

		ArrayList<Long> durationPreceeding = new ArrayList<>();
		ArrayList<ActivityObject2018> aosWithSamePDVal = new ArrayList<>();

		// StringBuilder sb = new StringBuilder();
		// System.out.println("durationPreceeding= " + durationPreceeding.toString());
		// System.out.println("aosWithSamePDVal= ");
		// aosWithSamePDVal.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
		// System.out.println(sb.toString());

		double medianPreceedingDuration = repAOResultGenericUser.getSecond().get(topPrimaryDimensionVal).getFirst();

		// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
		// int medianCinsCount = (int) StatsUtils
		// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
		// .getPercentile(50);

		// NOTE: we only need to take care of feature which are used for edit distance computation.
		// Instantiate the representative activity object.
		int activityID = -1;
		ArrayList<Integer> locationIDs = new ArrayList<>();
		String activityName = "";
		// String locationName = "";
		String workingLevelCatIDs = "";

		switch (primaryDimension)
		{
		case ActivityID:
		{
			activityID = topPrimaryDimensionVal;
			if (Constant.getDatabaseName().equals("gowalla1"))
			{// for gowalla dataset, act id and act name are same
				activityName = String.valueOf(topPrimaryDimensionVal);
				workingLevelCatIDs = topPrimaryDimensionVal + "__";
			}
			else
			{
				PopUps.printTracedErrorMsgWithExit("Error: not implemented this for besides gowalla1");
			}

			break;
		}
		case LocationID:
		{
			locationIDs.add(topPrimaryDimensionVal);
			workingLevelCatIDs = topPrimaryDimensionVal + "__";
			// locationName =
			// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
			// if (locationName == null || locationName.length() == 0)
			// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
			break;
		}
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown primaryDimension = " + Constant.primaryDimension);
			break;
		}

		Timestamp newRecommTimestamp = new Timestamp((long) (recommendationTime.getTime() + medianPreceedingDuration));

		ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
				activityName, "", newRecommTimestamp, "", "", "", String.valueOf(userId), -1, -1, -1, -1, -1, -1, -1,
				workingLevelCatIDs, -1, -1, new String[] { "" });

		if (!DateTimeUtils.isSameDate(recommendationTime, newRecommTimestamp))
		{
			System.out.print("Warning: recommendationTime = " + recommendationTime + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
					+ " for topRecommActName =" + topPrimaryDimensionVal);
		}
		//
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug getRepresentativeAOCollGeneric: getRepresentativeAO: old recommendationTime="
					+ recommendationTime.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDuration + " new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		System.out.println("repAO=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());

		return repAOForThisActNameForThisUser;
	}

	/**
	 * 
	 * Created to create rep AO from repAOResultGenericUser which has been created using training timelines of all
	 * users. (need to call only once for a test)
	 * 
	 * <p>
	 * Fork of getRepresentativeAOCollGeneric. Instead of recreating a new ActivityObject, it just fetches the generic
	 * rep AO for this PD Val and modifies the relevant features accordingly.
	 * 
	 * @param topPrimaryDimensionVal
	 * @param userId
	 * @param recommendationTime
	 * @param primaryDimension
	 * @param repAOResultGenericUser
	 *            {ActID,RepAO}, {ActID,{medianDurFromPrevForEachActName, medianDurFromNextForEachActName}}
	 * @return
	 * @since 8 Feb 2018
	 */
	private ActivityObject2018 getRepresentativeAOCollGenericV2(Integer topPrimaryDimensionVal, int userId,
			Timestamp recommendationTime, PrimaryDimension primaryDimension,
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultGenericUser)
	{
		if (Constant.getDatabaseName().equals("gowalla1") == false)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: not implemented this (getRepresentativeAOCollGenericV2() for besides gowalla1");
		}
		// StringBuilder verboseMsg = new StringBuilder();
		// System.out.println("Inside getRepresentativeAOColl(): topPrimaryDimensionVal=" + topPrimaryDimensionVal +
		// "\n");
		// ArrayList<Long> durationPreceeding = new ArrayList<>();
		// ArrayList<ActivityObject> aosWithSamePDVal = new ArrayList<>();

		// StringBuilder sb = new StringBuilder();
		// System.out.println("durationPreceeding= " + durationPreceeding.toString());
		// System.out.println("aosWithSamePDVal= ");
		// aosWithSamePDVal.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
		// System.out.println(sb.toString());

		double medianPreceedingDuration = repAOResultGenericUser.getSecond().get(topPrimaryDimensionVal).getFirst();

		// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
		// int medianCinsCount = (int) StatsUtils
		// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
		// .getPercentile(50);

		// NOTE: we only need to take care of feature which are used for edit distance computation.
		// Instantiate the representative activity object.

		Timestamp newRecommTimestamp = new Timestamp((long) (recommendationTime.getTime() + medianPreceedingDuration));

		ActivityObject2018 repAOForThisActNameForThisUser = repAOResultGenericUser.getFirst()
				.get(topPrimaryDimensionVal);

		repAOForThisActNameForThisUser.setStartTimestamp(newRecommTimestamp);
		repAOForThisActNameForThisUser.setEndTimestamp(newRecommTimestamp);
		repAOForThisActNameForThisUser.setUserID(String.valueOf(userId));

		if (!DateTimeUtils.isSameDate(recommendationTime, newRecommTimestamp))
		{
			System.out.print("Warning: recommendationTime = " + recommendationTime + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
					+ " for topRecommActName =" + topPrimaryDimensionVal);
		}
		//
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug getRepresentativeAOCollGeneric: getRepresentativeAO: old recommendationTime="
					+ recommendationTime.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDuration + " new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		System.out.println("repAOV2=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());

		return repAOForThisActNameForThisUser;
	}

	//

	/**
	 * 
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDuration
	 */
	private void writeRepAOs(LinkedHashMap<Integer, LinkedHashMap<String, ActivityObject2018>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<String, Pair<Double, Double>>> mapOfMedianPreSuccDuration,
			String commonPath)
	{

		StringBuilder sbTemp = new StringBuilder();

		WToFile.appendLineToFileAbs("UserID,ActID,RepAO\n", commonPath + "RepAO.txt");

		for (Entry<Integer, LinkedHashMap<String, ActivityObject2018>> uEntry : mapOfRepAOs.entrySet())
		{
			// sbTemp.append("\nUser:" + uEntry.getKey()).append("Rep AOs:\n");
			uEntry.getValue().entrySet().stream().forEachOrdered(e -> sbTemp
					.append(uEntry.getKey() + "," + e.getKey() + "," + e.getValue().toStringAllGowallaTS() + "\n"));
		}
		WToFile.appendLineToFileAbs(sbTemp.toString(), commonPath + "RepAO.txt");

		StringBuilder sbTemp2 = new StringBuilder();
		WToFile.appendLineToFileAbs("UserID,ActID,PreceedingMedian,SuceedingMedian",
				commonPath + "PreSucMedianDuration.txt");
		for (Entry<Integer, LinkedHashMap<String, Pair<Double, Double>>> uEntry : mapOfMedianPreSuccDuration.entrySet())
		{
			// sbTemp2.append("\nUser:" + uEntry.getKey()).append("\nMedian Prec Succeeding durations:\n");
			uEntry.getValue().entrySet().stream().forEachOrdered(e -> sbTemp2.append(uEntry.getKey() + "," + e.getKey()
					+ "," + e.getValue().getFirst() + "," + e.getValue().getSecond() + "\n"));

		}
		WToFile.appendLineToFileAbs(sbTemp2.toString(), commonPath + "PreSucMedianDuration.txt");

	}

	/**
	 * 
	 * @param dateToRecomm
	 * @param weekDay
	 * @param endTimeStamp
	 * @param recommP1
	 * @param actActualDone
	 * @param activityAtRecommPoint
	 * @return
	 * 
	 * @deprecated Not writing this anymore as similar information is written by:
	 *             org.activity.io.WritingToFile.writeEditDistancesPerRtPerCand()
	 */
	private static String getRTsWithDistancesToWrite(String dateToRecomm, String weekDay, Timestamp endTimeStamp,
			RecommendationMasterI recommP1, String actActualDone, ActivityObject2018 activityAtRecommPoint)
	{
		StringBuilder rtsWithEditDistancesMsg = new StringBuilder();

		for (Map.Entry<String, Pair<String, Double>> entryDistance : recommP1.getDistancesSortedMap().entrySet())
		{
			String candidateTimelineID = entryDistance.getKey();

			Timeline candidateTimeline = recommP1.getCandidateTimeline(candidateTimelineID);
			// recommP1.getCandidateTimeslines() .get(candidateTimelineID);

			int endPointIndexThisCandidate = candidateTimeline.getActivityObjectsInTimeline().size() - 1;
			ActivityObject2018 endPointActivityInCandidate = candidateTimeline.getActivityObjectsInTimeline()
					.get(endPointIndexThisCandidate);

			// difference in start time of end point activity of candidate and start
			// time of current activity
			long diffStartTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint.getStartTimestamp().getTime()
					- endPointActivityInCandidate.getStartTimestamp().getTime()) / 1000;
			// difference in end time of end point activity of candidate and end time of
			// current activity
			long diffEndTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint.getEndTimestamp().getTime()
					- endPointActivityInCandidate.getEndTimestamp().getTime()) / 1000;

			// ("DateOfRecomm"+",TimeOfRecomm,"+"TargetActivity,EditDistanceOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,CandidateTimeline,WeekDayOfRecomm");
			// bwRecommTimesWithEditDistances.write
			rtsWithEditDistancesMsg.append(dateToRecomm + "," + endTimeStamp.getHours() + ":"
					+ endTimeStamp.getMinutes() + ":" + endTimeStamp.getSeconds() + "," + candidateTimelineID + ","
					+ actActualDone + "," + entryDistance.getValue().getSecond() + ","
					// +dateOfCand+","
					+ diffStartTimeForEndPointsCand_n_GuidingInSecs + "," + diffEndTimeForEndPointsCand_n_GuidingInSecs
					+ "," + endPointIndexThisCandidate + "," + candidateTimeline.getActivityObjectNamesInSequence()
					+ "," + weekDay + "\n");
			// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
			// +"," +UtilityBelt.getWeekDayFromWeekDayInt(entryScore.getKey().getDay())
			// bwRecommTimesWithEditDistances.newLine();
		}

		return rtsWithEditDistancesMsg.toString();// +"\n"
	}

	public static String getActivityNameCountPairsWithCount(LinkedHashMap<String, Long> nameCountPairsSorted)
	{
		// String result = "";
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, Long> entry : nameCountPairsSorted.entrySet())
		{
			result.append("__").append(entry.getKey()).append(":").append(entry.getValue());
			// result += "__" + entry.getKey() + ":" + entry.getValue();
		}
		return result.toString();
	}

	public static String getActivityNameCountPairsWithoutCount(LinkedHashMap<String, Long> nameCountPairsSorted)
	{
		// String result = "";
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, Long> entry : nameCountPairsSorted.entrySet())
		{
			result.append("__").append(entry.getKey());
			// result += "__" + entry.getKey();
		}
		return result.toString();
	}

	public static String getActivityNameDurationPairsWithDuration(LinkedHashMap<String, Long> nameDurationPairsSorted)
	{
		// String result = "";
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
		{
			result.append("__").append(entry.getKey()).append(":").append(entry.getValue());
			// result += "__" + entry.getKey() + ":" + entry.getValue();
		}
		return result.toString();
	}

	public static String getActivityNameDurationPairsWithoutDuration(
			LinkedHashMap<String, Long> nameDurationPairsSorted)
	{
		// String result = "";
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
		{
			result.append("__").append(entry.getKey());
			// result += "__" + entry.getKey();
		}
		return result.toString();
	}

	/**
	 * Sets the matching unit array to be used depending on the lookPastType argument and matching unit values in Class
	 * Constant.
	 * 
	 * @param lookPastType
	 */
	// public void setMatchingUnitArray(Enums.LookPastType lookPastType)
	// {
	// this.matchingUnitArray = Constant.getMatchingUnitArray(lookPastType);
	// }

	/**
	 * Set the array of doubles containing threshold valyues to be used.
	 * 
	 * @param typeOfThreshold
	 */
	public void setThresholdsArray(Enums.TypeOfThreshold typeOfThreshold)
	{
		System.out.println("setThresholdsArray");
		switch (typeOfThreshold)
		{
		case Percent:// "Percent":
			this.thresholdsArray = percentThresholds;
			break;
		case Global:// "Global":
			this.thresholdsArray = globalThresholds;
			break;
		// case "None":
		// this.thresholdsArray = new int[] { 10000000 };
		// break;
		default:
			System.err.println("Error: Unrecognised threshold type in setThresholdsArray():" + typeOfThreshold);
		}
	}

	/**
	 * for each user, for each activity (category id), create a representative avg activity object.
	 * <p>
	 * For example,
	 * 
	 * for user 1, for activity ‘Asian Food’, create an Activity Object with activity name as ‘Asian Food’ and other
	 * features are the average values of the features all the ‘Asian Food’ activity objects occurring the training set
	 * of user 1.
	 * 
	 * 
	 * @param userId
	 * @param userTrainingTimelines
	 * @param allPossibleActivityNames
	 * @param userTestTimelines
	 *            only to find which act names occur in test but not in training
	 * @return
	 */
	private Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> buildRepresentativeAOsForUser(
			int userId, Timeline userTrainingTimelines, String[] allPossibleActivityNames, Timeline userTestTimelines)
	{
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeActivityObjects for user " + userId);
		LinkedHashMap<String, ActivityObject2018> repAOsForThisUser = null;
		LinkedHashMap<String, Pair<Double, Double>> actMedianPreSuccDuration = null;

		long t1 = System.currentTimeMillis();
		try
		{
			if (this.databaseName.equals("gowalla1") == false)
			{
				System.err.println("Error: database is  not gowalla1:" + this.databaseName);
				System.exit(-1);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// {
			// System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1);
			// }

			else
			{

				LinkedHashMap<String, ArrayList<ActivityObject2018>> aosForEachActName = new LinkedHashMap<>();

				/**
				 * Useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<String, ArrayList<Long>> durationFromPrevForEachActName = new LinkedHashMap<>();
				LinkedHashMap<String, ArrayList<Long>> durationFromNextForEachActName = new LinkedHashMap<>();

				// iterate over all possible activity names. initialise to preserve order.
				for (String actName : allPossibleActivityNames)
				{
					aosForEachActName.put(actName, new ArrayList<>());
					durationFromPrevForEachActName.put(actName, new ArrayList<>());
					durationFromNextForEachActName.put(actName, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				// long durationFromPreviousInSecs = 0;
				long prevTimestamp = 0;
				String prevActNameEncountered = null;

				LinkedHashSet<String> distinctActNamesEncounteredInTraining = new LinkedHashSet<>();

				for (ActivityObject2018 ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					String actNameEncountered = ao.getActivityName();
					distinctActNamesEncounteredInTraining.add(actNameEncountered);

					// add this act objec to the correct map entry in map of aos for given act names
					ArrayList<ActivityObject2018> aosStored = aosForEachActName.get(actNameEncountered);
					if (aosStored == null)
					{
						System.err.println(PopUps.getTracedErrorMsg("Error: encountered act name '" + actNameEncountered
								+ "' is not in the list of all possible act names"));
					}
					aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated

					// store the preceeding and succeeding durations
					long currentTimestamp = ao.getStartTimestamp().getTime();

					if (prevTimestamp != 0)
					{ // add the preceeding duration for this AO
						durationFromPrevForEachActName.get(actNameEncountered).add(currentTimestamp - prevTimestamp);
					}

					if (prevActNameEncountered != null)
					{
						// add the succeeding duration for the previous AO
						durationFromNextForEachActName.get(prevActNameEncountered)
								.add(currentTimestamp - prevTimestamp);
					}
					prevTimestamp = currentTimestamp;
					prevActNameEncountered = actNameEncountered;
				}

				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(
							ao -> System.out.print(ao.getActivityName() + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachActName:");
					durationFromPrevForEachActName.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachActName:");
					durationFromNextForEachActName.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each act name:");
					aosForEachActName.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachActName.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = userTrainingTimelines.getActivityObjectsInTimeline().stream()
						.count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// System.out.println("Num of ");
				if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
				{
					System.err.println(
							"Sanity check failed in buildRepresentativeActivityObjects\n(sumOfCountOfAOsFroMap) != "
									+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
									+ sumOfCountOfAOsFromTimeline);
				}

				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserV2(
						userId, aosForEachActName, durationFromPrevForEachActName, durationFromNextForEachActName);
				repAOsForThisUser = result.getFirst();
				actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeActivityObjects for user " + userId + " time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return new Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>>(
				repAOsForThisUser, actMedianPreSuccDuration);
	}

	/**
	 * for each user, for each activity (category id), create a representative avg activity object.
	 * <p>
	 * For example,
	 * 
	 * for user 1, for activity ‘Asian Food’, create an Activity Object with activity name as ‘Asian Food’ and other
	 * features are the average values of the features all the ‘Asian Food’ activity objects occurring the training set
	 * of user 1.
	 * 
	 * @param userId
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 *            only to find which act names occur in test but not in training
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @return
	 */
	private Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsForUserPD(
			int userId, Timeline userTrainingTimelines, Timeline userTestTimelines, Set<Integer> uniqueLocIDs,
			Set<Integer> uniqueActivityIDs)
	{
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeAOsForUserPD for user " + userId);
		LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = null;
		LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDuration = null;

		long t1 = System.currentTimeMillis();
		try
		{
			if (this.databaseName.equals("gowalla1") == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: database is  not gowalla1:" + this.databaseName);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }

			else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDVal = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDVal = new LinkedHashMap<>();

				Set<Integer> allPossiblePDVals = null;
				if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					allPossiblePDVals = uniqueActivityIDs;// uniqueLocIDs,Set<Integer>
				}
				else if (Constant.primaryDimension.equals(PrimaryDimension.LocationID))
				{
					allPossiblePDVals = uniqueLocIDs;
				}
				else
				{
					PopUps.printTracedErrorMsgWithExit("Error: unknown primary dimension");
				}

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : allPossiblePDVals)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDVal.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				// long durationFromPreviousInSecs = 0;
				long prevTimestamp = 0;
				Set<Integer> prevPDValEncountered = null;
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (ActivityObject2018 ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
					distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());

					for (Integer pdVal : uniquePdValsInAO)
					{
						// add this act object to the correct map entry in map of aos for given act names
						ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
						if (aosStored == null)
						{
							PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
									+ "' is not in the list of all possible pd vals");
						}
						aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
					}

					// store the preceeding and succeeding durations
					long currentTimestamp = ao.getStartTimestamp().getTime();

					if (prevTimestamp != 0)
					{ // add the preceeding duration for this AO
						for (Integer pdVal : uniquePdValsInAO)
						{
							durationFromPrevForEachPDVal.get(pdVal).add(currentTimestamp - prevTimestamp);
						}
					}

					if (prevPDValEncountered != null)
					{
						// add the succeeding duration for the previous AO
						for (Integer prevPDVal : prevPDValEncountered)
						{
							durationFromNextForEachPDVal.get(prevPDVal).add(currentTimestamp - prevTimestamp);
						}
					}

					prevTimestamp = currentTimestamp;
					prevPDValEncountered = uniquePdValsInAO;
				}

				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
							.print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = userTrainingTimelines.getActivityObjectsInTimeline().stream()
						.count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPD\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPD\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserPDV2(
						userId, aosForEachPDVal, durationFromPrevForEachPDVal, durationFromNextForEachPDVal);

				repAOsForThisUser = result.getFirst();
				actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsForUserPD for user " + userId + " time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOsForThisUser, actMedianPreSuccDuration);
	}

	/**
	 * Fork of buildRepresentativeAOsForUserPDV() intiated to reduce memory consumption (for primary dimension version
	 * 2)
	 * <p>
	 * for each user, for each activity (category id), create a representative avg activity object.
	 * <p>
	 * For example,
	 * 
	 * for user 1, for activity ‘Asian Food’, create an Activity Object with activity name as ‘Asian Food’ and other
	 * features are the average values of the features all the ‘Asian Food’ activity objects occurring the training set
	 * of user 1.
	 * 
	 * @param userId
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 *            only to find which act names occur in test but not in training
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @return
	 */
	private Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsForUserPDV2(
			int userId, Timeline userTrainingTimelines, Timeline userTestTimelinesqq, Set<Integer> uniqueLocIDs,
			Set<Integer> uniqueActivityIDs)
	{
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeAOsForUserPDV2 for user " + userId);
		LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = null;
		LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDuration = null;

		long t1 = System.currentTimeMillis();
		try
		{
			if (this.databaseName.equals("gowalla1") == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: database is  not gowalla1:" + this.databaseName);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }

			else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDVal = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDVal = new LinkedHashMap<>();

				// earlier version was using all possible vals fro PD but now using only those in training data.
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (ActivityObject2018 ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());
				}
				System.out.println(
						"distinctPDValsEncounteredInTraining.size() = " + distinctPDValsEncounteredInTraining.size());

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : distinctPDValsEncounteredInTraining)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDVal.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				// long durationFromPreviousInSecs = 0;
				long prevTimestamp = 0;
				Set<Integer> prevPDValEncountered = null;

				for (ActivityObject2018 ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
					for (Integer pdVal : uniquePdValsInAO)
					{
						// add this act object to the correct map entry in map of aos for given act names
						ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
						if (aosStored == null)
						{
							PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
									+ "' is not in the list of pd vals from training data");
						}
						aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
					}

					// store the preceeding and succeeding durations
					long currentTimestamp = ao.getStartTimestamp().getTime();

					if (prevTimestamp != 0)
					{ // add the preceeding duration for this AO
						for (Integer pdVal : uniquePdValsInAO)
						{
							durationFromPrevForEachPDVal.get(pdVal).add(currentTimestamp - prevTimestamp);
						}
					}

					if (prevPDValEncountered != null)
					{
						// add the succeeding duration for the previous AO
						for (Integer prevPDVal : prevPDValEncountered)
						{
							durationFromNextForEachPDVal.get(prevPDVal).add(currentTimestamp - prevTimestamp);
						}
					}

					prevTimestamp = currentTimestamp;
					prevPDValEncountered = uniquePdValsInAO;
				}

				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
							.print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = userTrainingTimelines.getActivityObjectsInTimeline().stream()
						.count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserPDV2(
						userId, aosForEachPDVal, durationFromPrevForEachPDVal, durationFromNextForEachPDVal);

				repAOsForThisUser = result.getFirst();
				actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsForUserPDV2 for user " + userId + " time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOsForThisUser, actMedianPreSuccDuration);
	}

	/**
	 * Analyse activity names which are not in training timelines for each user.
	 * 
	 * @param userId
	 * @param allPossibleActivityNames
	 * @param distinctActNamesEncounteredInTraining
	 * @param fileNameToWrite
	 * @param userTestTimelines
	 */
	private static void analyseActNameNotInTraining(int userId, String[] allPossibleActivityNames,
			LinkedHashSet<String> distinctActNamesEncounteredInTraining, String fileNameToWrite,
			String fileNameToWrite2, Timeline userTestTimelines)
	{
		System.out.println("#Distinct act names encountered in training for this user = "
				+ distinctActNamesEncounteredInTraining.size());
		System.out.println("#All possible act names = " + allPossibleActivityNames.length);

		int numOfActNamesNotInTraining = (allPossibleActivityNames.length
				- distinctActNamesEncounteredInTraining.size());
		System.out.println("#act names not in training timelines for this user = " + numOfActNamesNotInTraining);
		System.out.println("Acts not in trainings: ");
		LinkedHashSet<String> actNamesNotInTraining = new LinkedHashSet<String>(
				Arrays.asList(allPossibleActivityNames));
		actNamesNotInTraining.removeAll(distinctActNamesEncounteredInTraining);

		String actNameNotInTrainingSB = actNamesNotInTraining.stream().collect(Collectors.joining("__"));
		WToFile.appendLineToFileAbs(
				"U:" + userId + "," + numOfActNamesNotInTraining + "," + actNameNotInTrainingSB.toString() + "\n",
				fileNameToWrite);

		////////////// Look at act names in Test set and see if they were in training timelines or not.
		LinkedHashSet<String> actNamesInTestSet = new LinkedHashSet<String>();
		userTestTimelines.getActivityObjectsInTimeline().stream()
				.forEach(ao -> actNamesInTestSet.add(ao.getActivityName()));

		LinkedHashSet<String> actNamesInTestButNotInTraining = new LinkedHashSet<String>(actNamesInTestSet);
		actNamesInTestButNotInTraining.removeAll(distinctActNamesEncounteredInTraining);
		System.out.println(
				"#act names in test but not in training  for this user = " + actNamesInTestButNotInTraining.size());

		String actNamesInTestButNotInTrainingS = actNamesInTestButNotInTraining.stream()
				.collect(Collectors.joining("__"));

		WToFile.appendLineToFileAbs("U:" + userId + "," + actNamesInTestButNotInTraining.size() + ","
				+ actNamesInTestButNotInTrainingS + "\n", fileNameToWrite2);

	}

	/**
	 * 
	 * @param userID
	 * @param aosForEachActName
	 * @param durationFromPrevForEachActName
	 * @param durationFromNextForEachActName
	 * @return
	 */
	private Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserV1(
			int userID, LinkedHashMap<String, ArrayList<ActivityObject2018>> aosForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromPrevForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromNextForEachActName)
	{
		System.out.println("Inside computeRepresentativeActivityObject for userID" + userID);
		LinkedHashMap<String, ActivityObject2018> repAOs = new LinkedHashMap<String, ActivityObject2018>();
		LinkedHashMap<String, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		try
		{
			if (!this.databaseName.equals("gowalla1"))
			{
				System.err.println(
						PopUps.getTracedErrorMsg("Error: this method is currently only suitable for gowalla dataset"));
				System.exit(-1);
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.

			for (Entry<String, ArrayList<ActivityObject2018>> actNameEntry : aosForEachActName.entrySet())
			{
				String actName = actNameEntry.getKey();
				ArrayList<ActivityObject2018> aos = actNameEntry.getValue();

				double medianDurationFromPrevForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromPrevForEachActName.get(actName),
								"durationFromPrevForEachActName",
								userID + "__" + actName + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				double medianDurationFromNextForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromNextForEachActName.get(actName),
								"durationFromPrevForEachActName",
								userID + "__" + actName + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				// double medianSecondsFromMidnight =
				Timestamp mediaStartTS = getMedianSecondsSinceMidnightTimestamp(
						aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID, actName);

				double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				int medianCinsCount = (int) StatsUtils.getDescriptiveStatistics(cinsCount, "cinsCount",
						userID + "__" + actName + "cinsCount.txt", false).getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(
						(int) Integer.valueOf(actName), new ArrayList<Integer>(), actName, "", mediaStartTS, "", "", "",
						String.valueOf(userID), -1, medianCinsCount, -1, -1, -1, -1, -1, actName + "__", -1, -1,
						new String[] { "" });

				repAOs.put(actName, repAOForThisActNameForThisUser);
				actMedPreSuccDuration.put(actName,
						new Pair<>(medianDurationFromPrevForEachActName, medianDurationFromNextForEachActName));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting computeRepresentativeActivityObject for userID" + userID);
		return new Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>>(repAOs,
				actMedPreSuccDuration);
	}

	/**
	 * TODO: ALERT REALISEDO N 8 FEB 2018: NOTE: CHECKINS COUNT IS -1 IN HERE, PROBABLY BETTER TO TAKE MEDIAN INSTEAD.
	 * 
	 * @param userID
	 * @param aosForEachActName
	 * @param durationFromPrevForEachActName
	 * @param durationFromNextForEachActName
	 * @return
	 */
	private Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserV2(
			int userID, LinkedHashMap<String, ArrayList<ActivityObject2018>> aosForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromPrevForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromNextForEachActName)
	{
		System.out.println("Inside computeRepresentativeActivityObject for userID" + userID);
		LinkedHashMap<String, ActivityObject2018> repAOs = new LinkedHashMap<String, ActivityObject2018>();
		LinkedHashMap<String, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		try
		{
			if (!this.databaseName.equals("gowalla1"))
			{
				System.err.println(
						PopUps.getTracedErrorMsg("Error: this method is currently only suitable for gowalla dataset"));
				System.exit(-1);
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.
			for (Entry<String, ArrayList<ActivityObject2018>> actNameEntry : aosForEachActName.entrySet())
			{
				String actName = actNameEntry.getKey();
				ArrayList<ActivityObject2018> aos = actNameEntry.getValue();

				double medianDurationFromPrevForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromPrevForEachActName.get(actName),
								"durationFromPrevForEachActName",
								userID + "__" + actName + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				double medianDurationFromNextForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromNextForEachActName.get(actName),
								"durationFromPrevForEachActName",
								userID + "__" + actName + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				// Dummy because we are not going to actually use this, we will instead extract the timestamp from the
				// preceeding duration + timestamp of preceeding activity object. But we assign this median anyway to
				// avoid having it as numm which causes exception.
				Timestamp dummyMedianStartTS = new Timestamp(0);
				// Disabled on 20 July 2017 to improve speed and compatibility for checking method from primary
				// dimension perspective. It was not being used anyway.
				// $ getMedianSecondsSinceMidnightTimestamp(
				// $ aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID, actName);

				// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				// int medianCinsCount = (int) StatsUtils
				// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
				// .getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(
						(int) Integer.valueOf(actName), new ArrayList<Integer>(), actName, "", dummyMedianStartTS, "",
						"", "", String.valueOf(userID), -1, -1, -1, -1, -1, -1, -1, actName + "__", -1, -1,
						new String[] { "" });

				repAOs.put(actName, repAOForThisActNameForThisUser);
				actMedPreSuccDuration.put(actName,
						new Pair<>(medianDurationFromPrevForEachActName, medianDurationFromNextForEachActName));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting computeRepresentativeActivityObject for userID" + userID);
		return new Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>>(repAOs,
				actMedPreSuccDuration);
	}

	/**
	 * 
	 * @param userID
	 * @param aosForEachPDVal
	 * @param durationFromPrevForEachPDVal
	 * @param durationFromNextForEachPDVal
	 * @return
	 */
	private Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserPDV2(
			int userID, LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal,
			LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDVal,
			LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDVal)
	{
		System.out.println("Inside computeRepresentativeActivityObjectForUserPDV2 for userID" + userID);
		LinkedHashMap<Integer, ActivityObject2018> repAOs = new LinkedHashMap<>();
		LinkedHashMap<Integer, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		System.out.println(
				PerformanceAnalytics.getHeapInformation() + "\n" + PerformanceAnalytics.getHeapPercentageFree());
		try
		{
			if (!this.databaseName.equals("gowalla1"))
			{
				PopUps.printTracedErrorMsgWithExit("Error: this method is currently only suitable for gowalla dataset");
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.
			for (Integer pdVal : durationFromPrevForEachPDVal.keySet())// aosForEachPDVal.entrySet())
			{
				double medianDurationFromPrevForEachActName = StatsUtils.getDescriptiveStatisticsLong(
						durationFromPrevForEachPDVal.get(pdVal), "durationFromPrevForEachActName",
						userID + "__" + pdVal + "durationFromPrevForEachActName.txt", false).getPercentile(50);

				double medianDurationFromNextForEachActName = StatsUtils.getDescriptiveStatisticsLong(
						durationFromNextForEachPDVal.get(pdVal), "durationFromPrevForEachActName",
						userID + "__" + pdVal + "durationFromPrevForEachActName.txt", false).getPercentile(50);

				// Dummy because we are not going to actually use this, we will instead extract the timestamp from the
				// preceeding duration + timestamp of preceeding activity object. But we assign this median anyway to
				// avoid having it as numm which causes exception.
				Timestamp dummyMedianStartTS = new Timestamp(0);
				// Disable getMedianSecondsSinceMidnightTimestamp for speed as we were not using it anyway.
				// getMedianSecondsSinceMidnightTimestamp(
				// aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID,
				// pdVal.toString());

				// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				// int medianCinsCount = (int) StatsUtils
				// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
				// .getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				// Instantiate the representative activity object.
				int activityID = -1;
				ArrayList<Integer> locationIDs = new ArrayList<>();
				String activityName = "";
				// String locationName = "";
				String workingLevelCatIDs = "";

				switch (primaryDimension)
				{
				case ActivityID:
				{
					activityID = pdVal;
					activityName = String.valueOf(pdVal); // for gowalla dataset, act id and act name are same
					workingLevelCatIDs = pdVal + "__";
					break;
				}
				case LocationID:
				{
					locationIDs.add(pdVal);
					workingLevelCatIDs = pdVal + "__";
					// locationName =
					// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
					// if (locationName == null || locationName.length() == 0)
					// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
					break;
				}
				default:
					PopUps.printTracedErrorMsgWithExit(
							"Error: unknown primaryDimension = " + Constant.primaryDimension);
					break;
				}
				// ActivityObject(int activityID, ArrayList<Integer> locationIDs, String activityName, String
				// locationName, Timestamp startTimestamp, String startLatitude, String startLongitude, String
				// startAltitude, String userID, int photos_count, int checkins_count, int users_count, int
				// radius_meters, int highlights_count, int items_count, int max_items_count, String workingLevelCatIDs,
				// double distanceInMFromNext, long durationInSecsFromNext, String[] levelWiseCatIDs)

				ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
						activityName, "", dummyMedianStartTS, "", "", "", String.valueOf(userID), -1, -1, -1, -1, -1,
						-1, -1, workingLevelCatIDs, -1, -1, new String[] { "" });

				repAOs.put(pdVal, repAOForThisActNameForThisUser);
				actMedPreSuccDuration.put(pdVal,
						new Pair<>(medianDurationFromPrevForEachActName, medianDurationFromNextForEachActName));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting computeRepresentativeActivityObjectForUserPDV2 for userID" + userID);
		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOs, actMedPreSuccDuration);
	}

	/**
	 * 
	 * @param aosForEachPDVal
	 * @param PDVal
	 * @return
	 */
	public Integer getMedianCheckinsCountForGivePDVal(
			LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal, Integer PDVal)
	{
		int res = -1;
		ArrayList<ActivityObject2018> aos = aosForEachPDVal.get(PDVal);

		ArrayList<Double> cinCounts = (ArrayList<Double>) aos.stream().map(ao -> (double) ao.getCheckins_count())
				.collect(Collectors.toList());

		res = (int) StatsUtils.getDescriptiveStatistics(cinCounts).getPercentile(50);
		System.out.println("Inside getMedianCheckinsCountForGivePDVal: median cin for PDVal:" + PDVal + " = " + res);
		return res;
	}

	/**
	 * 
	 * @param userID
	 * @param aosForEachPDVal
	 * @param durationFromPrevForEachPDVal
	 * @param durationFromNextForEachPDVal
	 * @return
	 */
	private Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserPDV2GenericUser(
			LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal,
			LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDVal,
			LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDVal)
	{
		System.out.println("Inside computeRepresentativeActivityObjectForUserPDV2genericUser for generic user");
		LinkedHashMap<Integer, ActivityObject2018> repAOs = new LinkedHashMap<>();
		LinkedHashMap<Integer, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		System.out.println(
				PerformanceAnalytics.getHeapInformation() + "\n" + PerformanceAnalytics.getHeapPercentageFree());
		try
		{
			if (!this.databaseName.equals("gowalla1"))
			{
				PopUps.printTracedErrorMsgWithExit("Error: this method is currently only suitable for gowalla dataset");
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.
			for (Integer pdVal : durationFromPrevForEachPDVal.keySet())// aosForEachPDVal.entrySet())
			{
				double medianDurationFromPrevForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromPrevForEachPDVal.get(pdVal),
								"durationFromPrevForEachActName",
								"GenericUser__" + pdVal + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				double medianDurationFromNextForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromNextForEachPDVal.get(pdVal),
								"durationFromPrevForEachActName",
								"GenericUser__" + pdVal + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				// Dummy because we are not going to actually use this, we will instead extract the timestamp from the
				// preceeding duration + timestamp of preceeding activity object. But we assign this median anyway to
				// avoid having it as numm which causes exception.
				Timestamp dummyMedianStartTS = new Timestamp(0);
				// Disable getMedianSecondsSinceMidnightTimestamp for speed as we were not using it anyway.
				// getMedianSecondsSinceMidnightTimestamp(
				// aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID,
				// pdVal.toString());

				// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				// int medianCinsCount = (int) StatsUtils
				// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
				// .getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				// Instantiate the representative activity object.
				int activityID = -1;
				ArrayList<Integer> locationIDs = new ArrayList<>();
				String activityName = "";
				// String locationName = "";
				String workingLevelCatIDs = "";

				switch (primaryDimension)
				{
				case ActivityID:
				{
					activityID = pdVal;
					activityName = String.valueOf(pdVal); // for gowalla dataset, act id and act name are same
					workingLevelCatIDs = pdVal + "__";
					break;
				}
				case LocationID:
				{
					locationIDs.add(pdVal);
					workingLevelCatIDs = pdVal + "__";
					// locationName =
					// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
					// if (locationName == null || locationName.length() == 0)
					// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
					break;
				}
				default:
					PopUps.printTracedErrorMsgWithExit(
							"Error: unknown primaryDimension = " + Constant.primaryDimension);
					break;
				}
				// ActivityObject(int activityID, ArrayList<Integer> locationIDs, String activityName, String
				// locationName, Timestamp startTimestamp, String startLatitude, String startLongitude, String
				// startAltitude, String userID, int photos_count, int checkins_count, int users_count, int
				// radius_meters, int highlights_count, int items_count, int max_items_count, String workingLevelCatIDs,
				// double distanceInMFromNext, long durationInSecsFromNext, String[] levelWiseCatIDs)

				int cins_count = -1;

				if (Constant.useMedianCinsForRepesentationAO)
				{
					cins_count = getMedianCheckinsCountForGivePDVal(aosForEachPDVal, pdVal);
				}

				ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
						activityName, "", dummyMedianStartTS, "", "", "", "GenericUser", -1, cins_count, -1, -1, -1, -1,
						-1, workingLevelCatIDs, -1, -1, new String[] { "" });

				repAOs.put(pdVal, repAOForThisActNameForThisUser);
				actMedPreSuccDuration.put(pdVal,
						new Pair<>(medianDurationFromPrevForEachActName, medianDurationFromNextForEachActName));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting computeRepresentativeActivityObjectForUserPDV2genericUser for generic user");
		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOs, actMedPreSuccDuration);
	}

	/**
	 * Returns the median seconds since midnight for the given timestamps
	 * 
	 * @param tss
	 * @param userID
	 * @param actName
	 *            just used for nameing the filename to write the result to
	 * @return
	 */
	private static double getMedianSeconds(List<Timestamp> tss, int userID, String actName)
	{
		double medianSecsSinceMidnight = -1;
		try
		{
			double[] secsSinceMidnight = tss.stream()
					.mapToDouble(t -> t.getHours() * 60 * 60 + t.getMinutes() * 60 + t.getSeconds()).toArray();

			medianSecsSinceMidnight = StatsUtils.getDescriptiveStatistics(secsSinceMidnight, "secsSinceMidnight",
					userID + "__" + actName + "secsSinceMidnight.txt", false).getPercentile(50);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return medianSecsSinceMidnight;
	}

	/**
	 * Computes the median of the seconds since midnight for the given timestamps and returns a Timestamp with dummy
	 * year, month, date while hours, mins, seconds extracted from the median.
	 * 
	 * @param tss
	 * @param userID
	 * @param actName
	 *            just used for naming the filename to write result to
	 * @return
	 */
	private static Timestamp getMedianSecondsSinceMidnightTimestamp(List<Timestamp> tss, int userID, String actName)
	{
		Timestamp ts = null;
		try
		{
			double secs = getMedianSeconds(tss, userID, actName);

			int hours = (int) secs / 3600;
			secs = secs - (hours * 3600);
			int minutes = (int) secs / 60;
			secs = secs - (minutes * 60);
			ts = new Timestamp(1900, 0, 1, hours, minutes, (int) secs, 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ts;
	}

	/**
	 * Make sure train test split for current user is same for both approaches
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @param userId
	 * @param userTrainingTimelines
	 */
	private void sanityCheckTrainTestSplitSameForCollNonColl(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW, int userId,
			LinkedHashMap<Date, Timeline> userTrainingTimelines)
	{
		if (Constant.collaborativeCandidates)
		{// start of sanity check
			// make sure train test split for current user is same for both approaches
			List<LinkedHashMap<Date, Timeline>> trainTestTimelinesCollForThisUser = trainTestTimelinesForAllUsersDW
					.get(Integer.toString(userId));
			// get the training test timelines for current user
			LinkedHashMap<Date, Timeline> userTrainingTimelinesColl = trainTestTimelinesCollForThisUser.get(0);
			// LinkedHashMap<Date, Timeline> userTestTimelinesColl =
			// trainTestTimelinesCollForThisUser.get(1);
			if (!userTrainingTimelinesColl.equals(userTrainingTimelines))
			{
				System.err.println("Error: !userTrainingTimelinesColl.equals(userTrainingTimelines) for user:" + userId
						+ " userTrainingTimelinesColl.size()=" + userTrainingTimelinesColl.size()
						+ "userTestTimelinesColl.size()=" + trainTestTimelinesCollForThisUser.get(1).size());
			}
			else
			{
				System.out.println("Timesplit OK.");
			}
		} // end of sanity check
	}

}
