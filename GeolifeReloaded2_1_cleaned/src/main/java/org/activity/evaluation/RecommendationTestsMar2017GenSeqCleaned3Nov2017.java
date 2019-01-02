package org.activity.evaluation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.AltSeqPredictor;
import org.activity.constants.Enums.CaseType;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.generator.DataTransformerForSessionBasedRecAlgos;
import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.nn.LSTMCharModelling_SeqRecJun2018;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.recomm.RecommendationMasterI;
import org.activity.recomm.RecommendationMasterMar2017AltAlgoSeqNov2017;
import org.activity.recomm.RecommendationMasterMar2017GenSeqMultiDJul2018;
import org.activity.recomm.RecommendationMasterMar2017GenSeqNGramBaseline;
import org.activity.recomm.RecommendationMasterMar2017GenSeqNov2017;
import org.activity.recomm.RecommendationMasterRNN1Jun2018;
import org.activity.sanityChecks.Sanity;
import org.activity.sanityChecks.TimelineSanityChecks;
import org.activity.spmf.AKOMSeqPredictorLighter;
import org.activity.stats.FeatureStats;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.TimelineTrimmers;
import org.activity.util.TimelineUtils;

/**
 * Used as of 12 April 2018
 * </p>
 * Fork of org.activity.evaluation.RecommendationTestsMar2017GenSeqCleaned2, extending it to a more cleaner version
 * (recommending sequences). Executes the experiments for generating recommendations
 * 
 * @author gunjan
 *
 */
public class RecommendationTestsMar2017GenSeqCleaned3Nov2017
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
	String commonPath;

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
	public RecommendationTestsMar2017GenSeqCleaned3Nov2017(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsersTimelines, Enums.LookPastType lookPastType,
			Enums.CaseType caseType, Enums.TypeOfThreshold[] typeOfThresholds, int[] userIDs,
			double percentageInTraining, int lengthOfRecommendedSequence,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersTimelines)
	{

		// PopUps.showMessage("RecommendationTestsMar2017GenSeqCleaned3Nov2017: sampledUsersTimelines.size() ="
		// + sampledUsersTimelines.size());
		System.out.println("\n\n **********Entering RecommendationTestsMar2017GenSeqCleaned2********** " + lookPastType
				+ " " + caseType + " lengthOfRecommendedSequence:" + lengthOfRecommendedSequence);
		// PopUps.showMessage("Entering RecommendationTestsMar2017GenSeqCleaned2");
		long recommTestsStarttime = System.currentTimeMillis();

		// start of added on 31 Dec 2018 for Jupyter baseline
		if (Constant.doForJupyterBaselines)
		{
			DataTransformerForSessionBasedRecAlgos.convertToMQInputFormat(allUsersTimelines, Constant.getCommonPath(),
					Constant.getCommonPath() + Constant.getDatabaseName() + "InMQFormat.csv",
					sampledUsersTimelines.keySet());
		}
		// end of added on 31 Dec 2018

		AltSeqPredictor altSeqPredictor = Constant.altSeqPredictor;// added on 26 Dec 2018
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
			PopUps.showMessage("userIDs = " + Arrays.toString(userIDs));
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
		this.matchingUnitArray = Constant.getMatchingUnitArray(lookPastType, altSeqPredictor);

		// buildRepresentativeActivityObjectsForUsers()

		// Start of added on 20 Dec 2018
		// Start of 20 Dec 2018 Curtain
		// LinkedHashMap<Integer, LinkedHashMap<Integer, ActivityObject2018>> mapOfRepAOs = new LinkedHashMap<>();
		// LinkedHashMap<Integer, LinkedHashMap<Integer, Pair<Double, Double>>> mapOfMedianPreSuccDurationInms = new
		// LinkedHashMap<>();

		// training test DAY timelines for all users
		LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW = null;

		// training test timelines for all users continuous
		LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd = null;
		// added on 27 Dec 2018, needed for baseline coll high occur and high dur
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> trainTimelinesAllUsersDWFiltrd = null;

		// {ActID,RepAO}, {ActID,{medianDurFromPrevForEachActName, medianDurFromNextForEachActName}}
		// $$Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>
		// repAOResultGenericUser = null;
		// End of 20 Dec 2018 Curtain
		// Split timelines into training-test for each user to be used collaboratively

		trainTestTimelinesForAllUsersDW = TimelineUtils.splitAllUsersTestTrainingTimelines(allUsersTimelines,
				percentageInTraining, Constant.cleanTimelinesAgainInsideTrainTestSplit);

		// PopUps.showMessage("allUsersTimelines.size() = "+allUsersTimelines.size());

		trainTimelinesAllUsersContinuousFiltrd = RecommendationTestsUtils
				.getContinousTrainingTimelinesWithFilterByRecentDaysV2(trainTestTimelinesForAllUsersDW,
						Constant.getRecentDaysInTrainingTimelines(), Constant.filterTrainingTimelinesByRecentDays);

		// start of added on 27 Dec 2018
		// added on 27 Dec 2018, needed for baseline coll high occur and high dur
		trainTimelinesAllUsersDWFiltrd = RecommendationTestsUtils.getTrainingTimelinesWithFilterByRecentDaysV3(
				trainTestTimelinesForAllUsersDW, Constant.getRecentDaysInTrainingTimelines(),
				Constant.filterTrainingTimelinesByRecentDays);

		if (true)// can be disabled after initial run for speed
		{
			// start of sanity check // System.out.println("Sanity check 27 Dec 2018");
			for (Map.Entry<String, LinkedHashMap<Date, Timeline>> e : trainTimelinesAllUsersDWFiltrd.entrySet())
			{ // compare daywise and continuous timelines
				// System.out.println
				if ((TimelineSanityChecks.isDaywiseAndContinousTimelinesSameWRTAoNameTS(e.getValue(),
						trainTimelinesAllUsersContinuousFiltrd.get(e.getKey()), false)) == false)
				{
					PopUps.printTracedErrorMsgWithExit("Error: isDaywiseAndContinousTimelinesSameWRTAoNameTS = false");
				}
			}
		}
		// end of sanity check OKAY.// System.exit(0);
		// end of added on 27 Dec 2018
		// else
		// { // sampledUsersTimelines
		// trainTimelinesAllUsersContinuousFiltrd = RecommendationTestsUtils
		// .getContinousTrainingTimelines(trainTestTimelinesForAllUsersDW);
		// }

		// start of added on 29 Dec 2018
		if (Constant.doForJupyterBaselines)
		{
			DataTransformerForSessionBasedRecAlgos.writeSessionIDsForFilteredTrainAndTestDays(
					trainTimelinesAllUsersDWFiltrd, trainTestTimelinesForAllUsersDW, Constant.getCommonPath());
		}
		// UserID,Date,IndexInDay
		ArrayList<Triple<Integer, Date, Integer>> validRtsAsUserIdDateIndexInDay = new ArrayList<>();
		// end of added on 29 Dec 2018
		if (false)
		{
			Serializer.kryoSerializeThis(trainTimelinesAllUsersContinuousFiltrd,
					Constant.getCommonPath() + "trainTimelinesAllUsersContinuousFiltrd.kryo");
			System.exit(0);
		}

		StringBuilder sbT1 = new StringBuilder("User,NumOfAOsInTraining");
		trainTimelinesAllUsersContinuousFiltrd.entrySet().stream()
				.forEachOrdered(e -> sbT1.append(e.getKey() + "," + e.getValue().size() + "\n"));
		WToFile.appendLineToFileAbs(sbT1.toString(), this.commonPath + "User_NumOfActsInTraining.csv");

		///////////
		LinkedHashMap<String, RepresentativeAOInfo> repAOInfoForEachUser = new LinkedHashMap<>();// new approach

		// Start of curtain: incomplete: motive: faster speed
		// PopUps.showMessage("repAOInfoForEachUser.keySet() empty =" + repAOInfoForEachUser.keySet()
		// + " trainTestTimelinesForAllUsersDW.size()=" + trainTestTimelinesForAllUsersDW.size()
		// + " trainTimelinesAllUsersContinuousFiltrd.size() = " + trainTimelinesAllUsersContinuousFiltrd.size());
		if (Constant.lengthOfRecommendedSequence > 1)
		{
			repAOInfoForEachUser = RepresentativeAOInfo.buildRepresentativeAOInfosDec2018(
					trainTimelinesAllUsersContinuousFiltrd, primaryDimension, databaseName,
					Constant.collaborativeCandidates);

			// PopUps.showMessage("repAOInfoForEachUser.keySet() empty =" + repAOInfoForEachUser.keySet());

			System.out.println("repAOInfoForEachUser.keySet() =" + repAOInfoForEachUser.keySet());
			// if (Constant.preBuildRepAOGenericUser)
			// {// collaborative approach
			// // disabled on 1 Aug 2017, will do just-in-time computation of representative act object
			// // based only on candidate timelines
			// // added on14Nov2018
			// if (Constant.collaborativeCandidates)
			// {
			// repAOResultGenericUser = RepresentativeAOInfo.buildRepresentativeAOsAllUsersPDVCOllAllUsers(
			// trainTestTimelinesForAllUsersDW, Constant.getUniqueLocIDs(),
			// Constant.getUniqueActivityIDs(), primaryDimension, databaseName);
			// }
			// // end of curtain
			// ////// END of build representative activity objects for this user.
			// /// temp end
			// // start of added on 20 Dec 2018
			// else
			// {
			// repAOResultGenericUser = RepresentativeAOInfo.buildRepresentativeAOsAllUsersPDVCOllAllUsers(
			// trainTestTimelinesForAllUsersDW, Constant.getUniqueLocIDs(),
			// Constant.getUniqueActivityIDs(), primaryDimension, databaseName);
			// }
			// // end of added on 20 Dec 2018
			// }
		}
		// End of added on 20 Dec 2018

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
						long ctmu1 = System.currentTimeMillis();
						TreeMap<Integer, Integer> userIdNumOfRTsMap = new TreeMap<Integer, Integer>();

						Constant.setCurrentMatchingUnit(matchingUnit); // used for sanity checks
						System.out.println("Executing RecommendationTests for matching unit: " + matchingUnit);

						commonPath = RecommendationTestsUtils.computeCommonPath(matchingUnit, lookPastType,
								Constant.getOutputCoreResultsPath(), thresholdValue);
						Constant.setCommonPath(commonPath);
						System.out.println("Common path=" + Constant.getCommonPath());

						PrintStream consoleLogStream = WToFile.redirectConsoleOutput(commonPath + "consoleLog.txt");

						BufferedWriter metaBw = WToFile.getBWForNewFile(commonPath + "meta.csv");

						BufferedWriter recommSeqWithoutScoreBw = WToFile
								.getBWForNewFile(commonPath + "dataRecommSequence.csv");// **

						BufferedWriter recommSeqWithScoreBw = WToFile
								.getBWForNewFile(commonPath + "dataRecommSequenceWithScore.csv");// **

						BufferedWriter actualSeqBw = WToFile.getBWForNewFile(commonPath + "dataActualSequence.csv");// **

						ArrayList<BufferedWriter> bwsDataActual = new ArrayList<>(this.recommSeqLength);
						// added on 5 June 18, to write current activity for each RT in matrix format
						// ArrayList<BufferedWriter> bwsCurrentActivity = new ArrayList<>(this.recommSeqLength);

						BufferedWriter topNextActsWithoutDistance = WToFile
								.getBWForNewFile(commonPath + "topNextActivitiesWithoutDistance.csv");
						BufferedWriter topNextActsWithDistance = WToFile
								.getBWForNewFile(commonPath + "topNextActivitiesWithDistance.csv");

						BufferedWriter rtsAllUsingRecommMasterWriter = WToFile
								.getBWForNewFile(commonPath + "recommPointsAllUsingRecommMaster.csv");

						BufferedWriter rtsRejNoValidActAfterWriter = WToFile
								.getBWForNewFile(commonPath + "recommPointsInvalidBecuzNoValidActivityAfterThis.csv");
						BufferedWriter rtsRejWithNoCandsWriter = WToFile
								.getBWForNewFile(commonPath + "recommPointsWithNoCandidates.csv");
						BufferedWriter rtsRejWithNoCandsBelowThreshWriter = WToFile
								.getBWForNewFile(commonPath + "recommPointsWithNoCandidatesBelowThresh.csv");
						BufferedWriter rtsRejBlackListedWriter = WToFile
								.getBWForNewFile(commonPath + "recommPointsRejBlacklisted.csv");
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

							bwsRankedRecommWithScore.add(i, WToFile
									.getBWForNewFile(commonPath + "dataRankedRecommendationWithScores" + i + ".csv"));
							bwRankedRecommWithoutScore.add(i, WToFile.getBWForNewFile(
									commonPath + "dataRankedRecommendationWithoutScores" + i + ".csv"));

							bwsDataActual.add(i, WToFile.getBWForNewFile(commonPath + "dataActual" + i + ".csv"));
							// bwsCurrentActivity.add(i,
							// WToFile.getBWForNewFile(commonPath + "currentActivityMatrix" + i + ".csv"));
						}

						/**
						 * Contains list of activity names sorted by frequency of occurrence/duration. Num of unique
						 * sorted lists = number of users, however, each list is repeated so as maintain structural
						 * conformity with dataRankedRecommendationWithoutScores.csv
						 */
						BufferedWriter baseLineOccurrence = WToFile
								.getBWForNewFile(commonPath + "dataBaseLineOccurrence.csv");
						BufferedWriter baseLineDuration = WToFile
								.getBWForNewFile(commonPath + "dataBaseLineDuration.csv");
						// added 27 Dec 2018
						BufferedWriter baseLineOccurrenceWithScore = WToFile
								.getBWForNewFile(commonPath + "dataBaseLineOccurrenceWithScore.csv");
						BufferedWriter baseLineDurationWithScore = WToFile
								.getBWForNewFile(commonPath + "dataBaseLineDurationWithScore.csv");

						BufferedWriter bwNumOfWeekendsInTraining = WToFile
								.getBWForNewFile(commonPath + "NumberOfWeekendsInTraining.csv");
						BufferedWriter bwNumOfWeekendsInAll = WToFile
								.getBWForNewFile(commonPath + "NumberOfWeekendsInAll.csv");
						BufferedWriter bwCountTimeCategoryOfRecomm = WToFile
								.getBWForNewFile(commonPath + "CountTimeCategoryOfRecommPoitns.csv");
						BufferedWriter bwNextActInvalid = WToFile
								.getBWForNewFile(commonPath + "NextActivityIsInvalid.csv");
						BufferedWriter bwWriteNormalisationOfDistance = WToFile
								.getBWForNewFile(commonPath + "NormalisationDistances.csv");

						BufferedWriter bwNumOfValidAOsAfterRTInDay = WToFile
								.getBWForNewFile(commonPath + "NumOfValidAOsAfterRTInDay.csv");

						rtsRejNoValidActAfterWriter.write(
								"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity\n");
						rtsAllUsingRecommMasterWriter.write(
								"UserID,Date,indexOfAOInTestDay,RTTime,WeekDay,TimeCategory,PDVal,SeqIndex,NumOfCandsBelowThresh\n");
						rtsRejWithNoCandsWriter.write(
								"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity,TotalNumOfPossibleCands, NumCandsRejectedDueToNoCurrentActivityAtNonLast,NumCandsRejectedDueToNoNextActivity\n");
						rtsRejWithNoCandsBelowThreshWriter.write(
								"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity,TotalNumOfPossibleCands, NumCandsRejectedDueToNoCurrentActivityAtNonLast,NumCandsRejectedDueToNoNextActivity\n");
						rtsRejBlackListedWriter.write("User_ID,End_Timestamp\n");

						WToFile.writeToNewFile("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings,TotalRTs\n",
								commonPath + "UsersWithNoValidRTs.csv");

						bwCountTimeCategoryOfRecomm
								.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings,TotalRTs\n");

						bwNumOfWeekendsInTraining.write("User,NumOfWeekends,NumOfWeekdays\n");
						bwNumOfWeekendsInAll.write("User,NumOfWeekends,NumOfWeekdays\n");

						bwNextActInvalid.write("User,Timestamp_of_Recomm\n");

						// bwCountInActivitiesGuidingRecomm.write("User,RecommendationTime,TimeCategory,NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline\n");

						BufferedWriter numActsInEachCandbw = WToFile.getBWForNewFile(commonPath + "NumActs.csv");
						// commonPath + "NumActsmatchingUnit" + String.valueOf(matchingUnit) + ".csv");
						numActsInEachCandbw.write(
								"NumOfActObjsInCand-1,candTimelineID,UserId, DateAtRT, TimeAtRT, ActObjsInCand\n");

						bwWriteNormalisationOfDistance
								.write("User, DateOfRecomm, TimeOfRecom, EditDistance,NormalisedEditDistance\n");

						bwNumOfValidAOsAfterRTInDay
								.write("User, DateOfRecomm, TimeOfRecom, NumOfValidsAOsAfterRTInDay\n");

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
								"UserAtRecomm,DateAtRecomm,TimeAtRecomm,CandID,EndPointIndexOfCand,CurrTimeline,CandTimeline, EditOpsTraceOfCand,EditDistOfCand,#L1_EditOps, #ObjInSameOrder_#L2EditOps,NextActivityForRecomm, diffSTEndPointsCand_n_CurrActInSecs,diffETEndPointsCand_n_CurrActInSecs,CandidateTimeline,CurrentTimeline\n",
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
						// /**
						// * This can be made class variable and does not need to be repeated for each matching unit,
						// but
						// * in current setup we are doing it for each matching unit. Note: this is very small
						// performance
						// * effect, hence should not be of major concern. (UserId,ActName,RepresentativeAO)
						// */
						// LinkedHashMap<Integer, LinkedHashMap<Integer, ActivityObject2018>> mapOfRepAOs = new
						// LinkedHashMap<>();
						// LinkedHashMap<Integer, LinkedHashMap<Integer, Pair<Double, Double>>>
						// mapOfMedianPreSuccDurationInms = new LinkedHashMap<>();
						// // PopUps.showMessage("Starting iteration over user");
						//
						// /**
						// * training test DAY timelines for all users
						// **/
						// LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW =
						// null;
						//
						// // training test timelines for all users continuous
						// LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd = null;
						//
						// /**
						// * {ActID,RepAO}, {ActID,{medianDurFromPrevForEachActName, medianDurFromNextForEachActName}}
						// */
						// Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double,
						// Double>>> repAOResultGenericUser = null;

						// // Split timelines into training-test for each user to be used collaboratively
						// if (true)// Disabled on 14 Nov 2016, check if any damage done in non coll way.
						// // Constant.collaborativeCandidates)
						// {// 23 Feb 2018: probably we can take this out of MU loop
						// trainTestTimelinesForAllUsersDW = TimelineUtils.splitAllUsersTestTrainingTimelines(
						// allUsersTimelines, percentageInTraining,
						// Constant.cleanTimelinesAgainInsideTrainTestSplit);
						//
						// if (Constant.filterTrainingTimelinesByRecentDays)
						// {
						// trainTimelinesAllUsersContinuousFiltrd = RecommendationTestsUtils
						// .getContinousTrainingTimelinesWithFilterByRecentDaysV2(
						// trainTestTimelinesForAllUsersDW,
						// Constant.getRecentDaysInTrainingTimelines());
						// }
						// else
						// { // sampledUsersTimelines
						// trainTimelinesAllUsersContinuousFiltrd = RecommendationTestsUtils
						// .getContinousTrainingTimelines(trainTestTimelinesForAllUsersDW);
						// }
						// StringBuilder sbT1 = new StringBuilder();
						// trainTimelinesAllUsersContinuousFiltrd.entrySet().stream()
						// .forEachOrdered(e -> sbT1.append(e.getKey() + "," + e.getValue().size() + "\n"));
						// WToFile.appendLineToFileAbs(sbT1.toString(),
						// this.commonPath + "User_NumOfActsInTraining.csv");
						// }
						/// temp start
						////// START of build representative activity objects for this user.
						// if (true)// representativeAOsNotComputed == false) //do this
						// we only need to do it once for each user and dont need to repeat it for each matching
						// unit, so we can take it out of the for loop, however, it that we will also need to
						// take out the train-test splitting of timelines out of the loop, however that can be done
						// as well
						// // Start of curtain: incomplete: motive: faster speed
						// if (Constant.lengthOfRecommendedSequence > 1)
						// {
						// if (Constant.preBuildRepAOGenericUser)
						// {// collaborative approach
						// // disabled on 1 Aug 2017, will do just-in-time computation of representative act object
						// // based only on candidate timelines
						// // added on14Nov2018
						// if (Constant.collaborativeCandidates)
						// {
						// repAOResultGenericUser = buildRepresentativeAOsAllUsersPDVCOllAllUsers(
						// trainTestTimelinesForAllUsersDW, Constant.getUniqueLocIDs(),
						// Constant.getUniqueActivityIDs());
						// }
						// // end of curtain
						// ////// END of build representative activity objects for this user.
						// /// temp end
						// // start of added on 20 Dec 2018
						// else
						// {
						// repAOResultGenericUser = buildRepresentativeAOsAllUsersPDVCOllAllUsers(
						// trainTestTimelinesForAllUsersDW, Constant.getUniqueLocIDs(),
						// Constant.getUniqueActivityIDs());
						// }
						// // end of added on 20 Dec 2018
						// }
						// }
						System.out.println(
								"\nWill now loop over users in RecommTests: num of userIDs.length= " + userIDs.length);
						if (Constant.collaborativeCandidates)
						{
							System.out.println(" trainTimelinesAllUsersContinuous.size()="
									+ trainTimelinesAllUsersContinuousFiltrd.size());
						}
						for (int userId : userIDs) // for(int userId=minTestUser;userId <=maxTestUser;userId++)
						{ // int numberOfValidRTs = 0;// userCount += 1;

							System.out.println("\nUser id=" + userId);// PopUps.showMessage("\nUser id=" + userId);
							String userName = RecommendationTestsUtils.getUserNameFromUserID(userId, this.databaseName);

							// Start of Added on 21 Dec 2017
							if (altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM)
									|| altSeqPredictor.equals(Enums.AltSeqPredictor.AKOM)
									|| altSeqPredictor.equals(Enums.AltSeqPredictor.RNN1))
							{
								AKOMSeqPredictorLighter.clearSeqPredictorsForEachUserStored();
								LSTMCharModelling_SeqRecJun2018.clearLSTMPredictorsForEachUserStored();
							}
							// End of Added on 21 Dec 2017

							// PopUps.showMessage("before blacklisting");
							if (Constant.blacklistingUsersWithLargeMaxActsPerDay
									&& RecommendationTestsUtils.isBlackListedUser(userId))
							{
								WToFile.appendLineToFileAbs("UserRejectedSinceInBlacklist:" + userId,
										this.commonPath + "UserRejectedSinceInBlacklist.csv");
								continue;
							}
							// PopUps.showMessage("after blacklisting");

							// replacing iterative write with StringBuilder for better performance
							ArrayList<StringBuilder> sbsMaxNumOfDistinctRecommendations = RecommendationTestsUtils
									.createListOfStringBuilders(this.recommSeqLength);
							sbsMaxNumOfDistinctRecommendations.parallelStream().forEach(sb -> sb.append(
									"DateOfRecomm ,TimeOfRecomm ,Week_Day,MaxNumOfDistictRecommendation,NumOfCandidateTimelines(after applying Threshold)\n"));

							// replacing iterative write with StringBuilder for better performance
							ArrayList<StringBuilder> sbsNumOfCandTimelinesBelowThreshold = RecommendationTestsUtils
									.createListOfStringBuilders(this.recommSeqLength);
							sbsNumOfCandTimelinesBelowThreshold.parallelStream().forEach(sb -> sb.append(
									"DateOfRecomm ,TimeOfRecomm ,Week_Day,ThresholdAsDistance,NumOfCandidateTimelinesBelowThreshold,\n"));

							ArrayList<StringBuilder> sbsRecommTimesWithEditDistances = RecommendationTestsUtils
									.createListOfStringBuilders(this.recommSeqLength);
							sbsRecommTimesWithEditDistances.parallelStream().forEach(sb -> sb.append("DateOfRecomm"
									+ ",TimeOfRecomm,CandidateTimelineID,TargetActivity,EditDistanceOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,EndIndexOf(Sub)Cand,CandidateTimeline,WeekDayOfRecomm\n"));

							userAllDatesTimeslines = sampledUsersTimelines.get(Integer.toString(userId));// userId);
							if (userAllDatesTimeslines == null)
							{
								System.err.println("Error: userAllDatesTimeslines = " + userAllDatesTimeslines
										+ " user " + userId);
							}

							// //////////////////REMOVING SELECTED TIMELINES FROM DATASET////////////////////
							if (Constant.cleanTimelinesAgainInsideRecommendationTests) // added on April 11 2018
							{// Not sure why we would need to clean day timelines agains since it was already cleaned in
								// before being passed to this method
								userAllDatesTimeslines = TimelineTrimmers.cleanUserDayTimelines(userAllDatesTimeslines,
										commonPath + "InsideRecommTestCleanUserDayTimelines", String.valueOf(userId));
							}
							// ////////////////////////////////////////////////////////////////////////////////
							if (this.writeDayTimelinesOnce)
							{// if (matchingUnitIterator == 0) // write the given day timelines only once
								WToFile.writeGivenDayTimelines(userName, userAllDatesTimeslines, "All", true, true,
										true);
								this.writeDayTimelinesOnce = false;
							}

							// Splitting the set of timelines into training set and test set.
							List<LinkedHashMap<Date, Timeline>> trainTestTimelines = TimelineUtils
									.splitTestTrainingTimelines(userAllDatesTimeslines, percentageInTraining);
							LinkedHashMap<Date, Timeline> userTrainingTimelines = trainTestTimelines.get(0);
							LinkedHashMap<Date, Timeline> userTestTimelines = trainTestTimelines.get(1);

							RecommendationTestsUtils.sanityCheckTrainTestSplitSameForCollNonColl(
									trainTestTimelinesForAllUsersDW, userId, userTrainingTimelines);

							// start of added on Nov 29
							FeatureStats.writeFeatDistributionForEachUsersTrainingTimelines(userId,
									userTrainingTimelines, Constant.getCommonPath() + "FeatsOfTrainingTimelines.csv");
							// added on Dec 3 2018
							FeatureStats.writeFeatDistributionForEachUsersTrainingTimelinesSlidingWindowWise(userId,
									userTrainingTimelines,
									Constant.getCommonPath() + "FeatsOfTrainingTimelinesSlidingWindow3.csv", 4);

							// end of added on Nov 29

							////// START of build representative activity objects for this user.
							// if (true)// representativeAOsNotComputed == false) //do this
							// we only need to do it once for each user and dont need to repeat it for each matching
							// unit, so we can take it out of the for loop, however, it that we will also need to
							// take out the train-test splitting of timelines out of the loop, however that can be done
							// as well
							Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResult = null;
							// start of Dec 20 curtain
							// if (Constant.preBuildRepAOGenericUser == false && lengthOfRecommendedSequence > 1)
							// {
							// if (Constant.collaborativeCandidates == false)
							// {
							// repAOResult = RepresentativeAOInfo.prebuildRepresentativeActivityObjects(
							// trainTestTimelinesForAllUsersDW, userId, userTrainingTimelines,
							// userTestTimelines, databaseName, primaryDimension);
							//
							// LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = repAOResult
							// .getFirst();
							// mapOfRepAOs.put(userId, repAOsForThisUser);
							// mapOfMedianPreSuccDurationInms.put(userId, repAOResult.getSecond());
							// }
							// else
							// {// collaborative approach
							// // disabled on 1 Aug 2017, will do just-in-time computation of representative act
							// // object// based only on candidate timelines
							// if (Constant.buildRepAOJustInTime == false)
							// {
							// repAOResult = buildRepresentativeAOsForUserPDVCOll(userId,
							// trainTestTimelinesForAllUsersDW, Constant.getUniqueLocIDs(),
							// Constant.getUniqueActivityIDs(), databaseName, primaryDimension);
							// LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = repAOResult
							// .getFirst();
							// mapOfRepAOs.put(userId, repAOsForThisUser);
							// mapOfMedianPreSuccDurationInms.put(userId, repAOResult.getSecond());
							// }
							// }
							// }
							// end of Dec 20 curtain
							////// END of build representative activity objects for this user.

							if (Constant.collaborativeCandidates)
							{
								if (trainTimelinesAllUsersContinuousFiltrd.size() <= 1)
								{
									System.out.println("Warning: Skipping this user " + userId
											+ " as it has 1 training user: trainTimelinesAllUsersContinuous.size()="
											+ trainTimelinesAllUsersContinuousFiltrd.size());
									WToFile.appendLineToFileAbs("User " + userId + ",",
											commonPath + "UserWithNoTrainingDay.csv");
									numOfValidRTs.put(userId, 0);
									continue;
								}
							}
							else
							{
								if (userTrainingTimelines.size() == 0)
								{
									System.out.println(
											"Warning: Skipping this user " + userId + " as it has 0 training days");
									WToFile.appendLineToFileAbs("User " + userId + ",",
											commonPath + "UserWithNoTrainingDay.csv");
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
							String actNamesCountsWithCountOverTrain = "";
							String actNamesDurationsWithDurationOverTrain = "";

							// START OF Curtain disable trivial baselines
							if (Constant.DoBaselineDuration || Constant.DoBaselineOccurrence)
							{
								boolean doWrite = true;
								if (databaseName.equals("gowalla1"))
								{
									doWrite = false;
								}
								// this is content based approach
								if (Constant.collaborativeCandidates == false)
								{
									// Okay but disabled on 27 Dec 2018 in favour of method with String keys which is
									// also used for collaborative approach.
									// mapsForCountDurationBaselines =
									// WToFile.writeBasicActivityStatsAndGetBaselineMaps(
									// userName, userAllDatesTimeslines, userTrainingTimelines, userTestTimelines,
									// Constant.primaryDimension, true, false);
									mapsForCountDurationBaselines = WToFile
											.writeBasicActivityStatsAndGetBaselineMapsStringKeys(userName,
													TimelineUtils.toStringKeys(userAllDatesTimeslines),
													TimelineUtils.toStringKeys(userTrainingTimelines),
													TimelineUtils.toStringKeys(userTestTimelines),
													Constant.primaryDimension, doWrite, false);
								}
								else
								{
									// give training timelines of other users except current user (as daywise timeline)
									LinkedHashMap<String, LinkedHashMap<Date, Timeline>> trainTimelinesDWForAllExceptCurrUser = new LinkedHashMap<>(
											trainTimelinesAllUsersDWFiltrd);

									LinkedHashMap<Date, Timeline> removedSuccess = trainTimelinesDWForAllExceptCurrUser
											.remove(String.valueOf(userId));
									if (removedSuccess == null)
									{
										PopUps.printTracedErrorMsgWithExit(
												"Error: remove curr user unsuccessful. userId = " + userId
														+ " while keySet to match to :"
														+ trainTimelinesDWForAllExceptCurrUser.keySet());
									}

									LinkedHashMap<String, Timeline> trainTimelinesDWForAllExceptCurrUserStringKeys = TimelineUtils
											.toTogetherWithUserIDStringKeys(trainTimelinesDWForAllExceptCurrUser);

									mapsForCountDurationBaselines = WToFile
											.writeBasicActivityStatsAndGetBaselineMapsStringKeys(userName, null,
													trainTimelinesDWForAllExceptCurrUserStringKeys, null,
													Constant.primaryDimension, doWrite, true);
								}

								LinkedHashMap<String, Long> activityNameCountPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) mapsForCountDurationBaselines
										.get("activityNameCountPairsOverAllTrainingDays");
								ComparatorUtils.assertNotNull(activityNameCountPairsOverAllTrainingDays);
								actNamesCountsWithoutCountOverTrain = RecommendationTestsUtils
										.getActivityNameCountPairsWithoutCount(
												activityNameCountPairsOverAllTrainingDays);
								actNamesCountsWithCountOverTrain = RecommendationTestsUtils
										.getActivityNameCountPairsWithCount(activityNameCountPairsOverAllTrainingDays);

								LinkedHashMap<String, Long> activityNameDurationPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) mapsForCountDurationBaselines
										.get("activityNameDurationPairsOverAllTrainingDays");
								ComparatorUtils.assertNotNull(activityNameDurationPairsOverAllTrainingDays);
								actNamesDurationsWithoutDurationOverTrain = RecommendationTestsUtils
										.getActivityNameDurationPairsWithoutDuration(
												activityNameDurationPairsOverAllTrainingDays);
								actNamesDurationsWithDurationOverTrain = RecommendationTestsUtils
										.getActivityNameDurationPairsWithDuration(
												activityNameDurationPairsOverAllTrainingDays);
							}
							// END OF Curtain disable trivial baselines

							// else if (matchingUnitIterator == 0) // do this only for one matching unit as it does not
							// // change per matching unit
							// {
							// mapsForCountDurationBaselines = WritingToFile.writeBasicActivityStatsAndGetBaselineMaps(
							// userName, userAllDatesTimeslines, userTrainingTimelines, userTestTimelines);
							// }

							/*
							 * ********** ************************************************
							 * ********************************
							 */

							int numOfWeekendsInTraining = TimelineUtils
									.getNumOfWeekendsInGivenDayTimelines(userTrainingTimelines);
							int numOfWeekdaysInTraining = userTrainingTimelines.size() - numOfWeekendsInTraining;
							bwNumOfWeekendsInTraining.write(
									userName + "," + numOfWeekendsInTraining + "," + numOfWeekdaysInTraining + "\n");

							int numOfWeekendsInAll = TimelineUtils
									.getNumOfWeekendsInGivenDayTimelines(userAllDatesTimeslines);
							int numOfWeekdaysInAll = userAllDatesTimeslines.size() - numOfWeekendsInAll;
							bwNumOfWeekendsInAll
									.write(userName + "," + numOfWeekendsInAll + "," + numOfWeekdaysInAll + "\n");
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
								System.out.println("For userid=" + userId + " entry.getKey()=" + testDate
										+ "  dateToRecomm=" + dateToRecomm);

								String weekDay = DateTimeUtils.getWeekDayFromWeekDayInt(testDate.getDay());

								ArrayList<ActivityObject2018> activityObjectsInTestDay = testDayTimelineForUser
										.getActivityObjectsInDay();

								////////// added to improve write speed
								ArrayList<StringBuilder> sbsRawToWriteForThisUserDate = RecommendationTestsUtils
										.createListOfStringBuilders(recommSeqLength);

								StringBuilder metaToWriteForThisUserDate = new StringBuilder();
								StringBuilder recommSequenceWithScoreForThisUserDate = new StringBuilder();
								StringBuilder recommSequenceWithoutScoreForThisUserDate = new StringBuilder();

								ArrayList<StringBuilder> sbsDataActualToWriteForThisUserDate = RecommendationTestsUtils
										.createListOfStringBuilders(recommSeqLength);

								// Added on June 5 2018
								// ArrayList<StringBuilder> sbsCurrentActivityToWriteForThisUserDate =
								// createListOfStringBuilders(
								// recommSeqLength);

								StringBuilder dataActualSeqActsToWriteForThisUserDate = new StringBuilder();
								StringBuilder metaIfCurrentTargetSameToWriteForThisUserDate = new StringBuilder();

								ArrayList<StringBuilder> sbsNumOfCandTimelinesForThisUserDate = RecommendationTestsUtils
										.createListOfStringBuilders(recommSeqLength);

								StringBuilder topNextActsWithoutDistToWriteForThisUserDate = new StringBuilder();
								StringBuilder topNextActsWithDistToWriteForThisUserDate = new StringBuilder();

								ArrayList<StringBuilder> rankedRecommWithScoreToWriteForThisUserDate = RecommendationTestsUtils
										.createListOfStringBuilders(recommSeqLength);

								ArrayList<StringBuilder> rankedRecommWithoutScoreToWriteForThisUserDate = RecommendationTestsUtils
										.createListOfStringBuilders(recommSeqLength);

								StringBuilder baseLineOccurrenceToWriteForThisUserDate = new StringBuilder();
								StringBuilder baseLineDurationToWriteForThisUserDate = new StringBuilder();
								// added 27 Dec 2018
								StringBuilder baseLineOccurrenceWithScoreToWriteForThisUserDate = new StringBuilder();
								StringBuilder baseLineDurationWithScoreToWriteForThisUserDate = new StringBuilder();
								///////////

								// loop over the activity objects for this day
								// will not make recommendation for days which have only one activity
								// PopUps.showMessage("Just before iterating over AOs in test:
								// activityObjectsInTestDay.size() = " + activityObjectsInTestDay.size());
								for (int indexOfAOInTestDay = 0; indexOfAOInTestDay < activityObjectsInTestDay.size()
										- 1; indexOfAOInTestDay++)
								{
									ActivityObject2018 activityObjectInTestDay = activityObjectsInTestDay
											.get(indexOfAOInTestDay);

									String activityNameInTestDay = activityObjectInTestDay.getActivityName();
									ArrayList<Integer> primaryDimensionValInTestDay = activityObjectInTestDay
											.getPrimaryDimensionVal();

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
									{// ("Others"/"Not Available"))
										System.out.println("Skipping because " + Constant.INVALID_ACTIVITY2);
										continue;
									}
									// Recommendation is made at the end time of the activity object in consideration
									Timestamp endTimeStamp = activityObjectInTestDay.getEndTimestamp();// getStartTimestamp();
									String timeCategory = DateTimeUtils.getTimeCategoryOfDay(endTimeStamp.getHours());

									// SeqChange to check for more number of activities after the recomm point
									if (TimelineUtils.hasAtleastNValidAOsAfterItInTheDay(indexOfAOInTestDay,
											testDayTimelineForUser, recommSeqLength) == false)
									{
										System.out.println("Skipping this recommendation point because there less than "
												+ recommSeqLength + " activity objects after this in the day");
										rtsRejNoValidActAfterWriter.write(userId + "," + dateToRecomm + ","
												+ indexOfAOInTestDay + "," + endTimeStamp + "," + weekDay + ","
												+ timeCategory + activityNameInTestDay + "\n");
										continue;
									}

									// Target Activity, actual next activity
									ActivityObject2018 nextValidActivityObjectAfterRecommPoint1 = testDayTimelineForUser
											.getNextValidActivityAfterActivityAtThisTime(
													new Timestamp(year - 1900, month - 1, date, endTimeStamp.getHours(),
															endTimeStamp.getMinutes(), endTimeStamp.getSeconds(), 0));

									// checked if it is giving correct results: concern: passing end ts directly and
									// using LocalDate for comparison
									ArrayList<ActivityObject2018> nextValidActivityObjectsAfterRecommPoint1 = TimelineUtils
											.getNextNValidAOsAfterActivityAtThisTimeSameDay(testDayTimelineForUser,
													endTimeStamp, this.recommSeqLength);
									// timestamp sanity check start
									// $DateTimeSanityChecks.assertEqualsTimestamp(date, month, year, endTimeStamp);
									// FOUND ABOVE OKAY IN RUN

									Sanity.eq(nextValidActivityObjectsAfterRecommPoint1.size(), recommSeqLength,
											"Error in Sanity Check RT407: User id" + userId
													+ " Next activity Objects after " + endTimeStamp
													+ " nextValidActivityObjectsAfterRecommPoint1.size() ="
													+ nextValidActivityObjectsAfterRecommPoint1.size() + " expected ="
													+ recommSeqLength
													+ "\nif it was such, we should have not reached this point of execution");

									System.out.println("UserId" + userId + " Next AO after RT:" + endTimeStamp + " ="
											+ nextValidActivityObjectAfterRecommPoint1.getActivityName()
											+ "\nNext AOs after RT:" + endTimeStamp + " = ");
									nextValidActivityObjectsAfterRecommPoint1.stream().forEachOrdered(ao -> System.out
											.print(ao.getPrimaryDimensionVal("/") + ":" + ao.getEndTimestamp() + ","));

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
												.getValidAOsAfterThisTimeInTheDay(endTimeStamp, testDayTimelineForUser)
												.size();
										// testDayTimelineForUser.getNumOfValidActivityObjectsAfterThisTimeInSameDay(endTimeStamp);
										// .getNumOfValidActivityObjectAfterThisTime(endTimeStamp);
										sbNumOfValidsAfterAnRT.append(userId + "," + dateToRecomm + "," + endTimeStamp
												+ "," + numOfValidsAOsAfterThisRT + "\n");
										// if (numOfValidsAOsAfterThisRT >= 4) { WritingToFile.appendLineToFileAbsolute(
										// userId + "," + dateToRecomm + "," + endTimeStamp + ","
										// + numOfValidsAOsAfterThisRT + "\n", commonPath +
										// "numOfValidsAfterRTsGEQ4.csv"); }
									}

									// ////////////////////////////Start of New addition for blacklisted RTs
									if (Constant.BLACKLISTING
											&& blackListedRTs.contains(new String(userId + " " + endTimeStamp)))
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
									ArrayList<ActivityObject2018> repAOsFromPrevRecomms = new ArrayList<>(
											recommSeqLength);

									// String recommendationTimeString = endTimeString;
									String recommTimesStrings[] = new String[recommSeqLength];
									Timestamp recommendationTimes[] = new Timestamp[recommSeqLength];
									recommendationTimes[0] = endTimeStamp;// the first RT is endTS of current AO

									String topRecommendedPrimaryDimensionVal[] = new String[recommSeqLength];
									boolean skipThisRTNoCandTimelines = false; // skip this RT because no cand timelines

									for (int seqIndex = 0; seqIndex < recommSeqLength; seqIndex++)
									{ // if (VerbosityConstants.verbose){
										System.out.println("\n** Recommendation Iteration " + seqIndex + ": userID:"
												+ userId + " repAOsFromPrevRecomms.size:" + repAOsFromPrevRecomms.size()
												+ " rt:" + recommendationTimes[seqIndex]);
										if (VerbosityConstants.verbose)
										{
											System.out.println("repAOsFromPrevRecomms:");
											repAOsFromPrevRecomms.stream().forEachOrdered(
													ao -> System.out.print("  >>" + ao.toStringAllGowallaTS()));
										} // }
										recommTimesStrings[seqIndex] = recommendationTimes[seqIndex].getHours() + ":"
												+ recommendationTimes[seqIndex].getMinutes() + ":"
												+ recommendationTimes[seqIndex].getSeconds();

										if (this.lookPastType.equals(Enums.LookPastType.NGram) && (!Constant.NGramColl))
										{
											recommMasters[seqIndex] = new RecommendationMasterMar2017GenSeqNGramBaseline(
													userTrainingTimelines, userTestTimelines, dateToRecomm,
													recommTimesStrings[0], userId, repAOsFromPrevRecomms);
										}
										// Alternative algorithm
										// else if (altSeqPredictor == Enums.AltSeqPredictor.PureAKOM)
										else if (altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM)
												|| altSeqPredictor.equals(Enums.AltSeqPredictor.AKOM)
												|| altSeqPredictor.equals(Enums.AltSeqPredictor.ClosestTime)
												|| altSeqPredictor.equals(Enums.AltSeqPredictor.HighDur)
												|| altSeqPredictor.equals(Enums.AltSeqPredictor.HighOccur))
										// && (this.lookPastType.equals(Enums.LookPastType.Daywise)
										{
											recommMasters[seqIndex] = new RecommendationMasterMar2017AltAlgoSeqNov2017(
													userTrainingTimelines, userTestTimelines, dateToRecomm,
													recommTimesStrings[0], userId, thresholdValue, typeOfThreshold,
													matchingUnit, caseType, this.lookPastType, false,
													repAOsFromPrevRecomms, trainTestTimelinesForAllUsersDW,
													trainTimelinesAllUsersContinuousFiltrd, altSeqPredictor,
													mapsForCountDurationBaselines);
										}

										else if (altSeqPredictor == Enums.AltSeqPredictor.RNN1)
										// Alternative algorithm
										{// recommMasters[seqIndex]
											RecommendationMasterRNN1Jun2018 rnnMaster = null;
											// Since this algorithm does not recommend interatively;
											if (seqIndex == 0)
											{
												rnnMaster = new RecommendationMasterRNN1Jun2018(userTrainingTimelines,
														userTestTimelines, dateToRecomm, recommTimesStrings[0], userId,
														thresholdValue, typeOfThreshold, caseType, this.lookPastType,
														false, repAOsFromPrevRecomms, trainTestTimelinesForAllUsersDW,
														trainTimelinesAllUsersContinuousFiltrd, altSeqPredictor,
														recommSeqLength);
											}
											else
											{
												rnnMaster = (RecommendationMasterRNN1Jun2018) recommMasters[0];
											}
											rnnMaster.setIndexOfRecommSeq(seqIndex);
											recommMasters[seqIndex] = rnnMaster;
											// (50, 2,dateToRecomm, recommTimesStrings[0], userId,
											// trainTestTimelinesForAllUsersDW, trainTimelinesAllUsersContinuous);
										}

										else
										{
											if (Constant.doSecondaryDimension)
											{ // added on 18 July 2018
												recommMasters[seqIndex] = new RecommendationMasterMar2017GenSeqMultiDJul2018(
														userTrainingTimelines, userTestTimelines, dateToRecomm,
														recommTimesStrings[0], userId, thresholdValue, typeOfThreshold,
														matchingUnit, caseType, this.lookPastType, false,
														repAOsFromPrevRecomms, trainTestTimelinesForAllUsersDW,
														trainTimelinesAllUsersContinuousFiltrd);
											}
											else
											{
												recommMasters[seqIndex] = new RecommendationMasterMar2017GenSeqNov2017(
														userTrainingTimelines, userTestTimelines, dateToRecomm,
														recommTimesStrings[0], userId, thresholdValue, typeOfThreshold,
														matchingUnit, caseType, this.lookPastType, false,
														repAOsFromPrevRecomms, trainTestTimelinesForAllUsersDW,
														trainTimelinesAllUsersContinuousFiltrd);
											}
										}

										// Note: RT passed to the recommendation master is always endTimestamp. This is
										// intentional to make the current implementation of extracting current timeline
										// work. RT is only important for extraction of current timeline

										//////////////////////////
										RecommendationMasterI recommMaster = recommMasters[seqIndex];
										////////////////////////////////////////////////////////////////////////

										double thresholdAsDistance = recommMasters[0].getThresholdAsDistance();

										String rtDescription = userId + "," + dateToRecomm + "," + indexOfAOInTestDay
												+ "," + recommTimesStrings[0] + "," + weekDay + "," + timeCategory + ","
												+ recommMaster.getActivityObjectAtRecomm().getPrimaryDimensionVal("/")
												+ "," + seqIndex + ","
												+ recommMaster.getNumOfCandTimelinesBelowThresh();

										// Write num of cands using the recommMaster for eachRT including zero cand
										// WritingToFile.appendLineToFileAbsolute(rtDescription + "\n",
										// this.commonPath + "recommPointsAllUsingRecommMaster.csv");
										rtsAllUsingRecommMasterWriter.write(rtDescription + "\n");
										// check if all seq recomms for this RT has daywise candidate timelines
										if (recommMaster.hasCandidateTimeslines() == false)
										{
											System.out.println(
													"Can't make recommendation at this point as no cand timelines for recommMaster i = "
															+ seqIndex);
											rtsRejWithNoCandsWriter.write(rtDescription + "\n");
											sbsNumOfCandTimelinesBelowThreshold.get(seqIndex)
													.append(dateToRecomm + "," + recommendationTimes[seqIndex] + ","
															+ weekDay + "," + thresholdAsDistance + "," + 0 + "\n");// bwNumOfCandTimelinesBelowThreshold.newLine();
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

											rtsRejWithNoCandsBelowThreshWriter.write(rtDescription + "\n");// $$+recommP1.totalNumberOfProbableCands+","+recommP1.numCandsRejectedDueToNoCurrentActivityAtNonLast+","+recommP1.numCandsRejectedDueToNoNextActivity);
											sbsNumOfCandTimelinesBelowThreshold.get(seqIndex)
													.append(dateToRecomm + "," + recommendationTimes[seqIndex] + ","
															+ weekDay + "," + thresholdAsDistance + "," + 0 + "\n");
											skipThisRTNoCandTimelines = true;
											break; // break out of iterative recommendation for each seq index
										}

										////////////////////////////////////////////////////////////////////////
										// check if all seq recomms for this RT will have daywise candidate timelines
										boolean hasDayWiseCandidateTimelines = TimelineUtils
												.hasDaywiseCandidateTimelines(userTrainingTimelines,
														recommMaster.getDateAtRecomm(),
														recommMaster.getActivityObjectAtRecomm(),
														Constant.collaborativeCandidates, String.valueOf(userId),
														trainTestTimelinesForAllUsersDW);

										if (hasDayWiseCandidateTimelines == false)
										{
											rtsRejWithNoDWButMUCandsCands.write(rtDescription + "\n");
											System.out.println(
													"Cannot make recommendation at this point as there are no daywise candidate timelines, even though there are mu candidate timelines");
											sbsNumOfCandTimelinesBelowThreshold.get(seqIndex)
													.append(dateToRecomm + "," + recommendationTimes[seqIndex] + ","
															+ weekDay + "," + thresholdAsDistance + "," + 0 + "\n");
											skipThisRTNoCandTimelines = true;
											break; // break out of iterative recommendation for each seq index
										}

										sbsNumOfCandTimelinesBelowThreshold.get(seqIndex)
												.append(dateToRecomm + "," + recommendationTimes[seqIndex] + ","
														+ weekDay + "," + thresholdAsDistance + ","
														+ recommMaster.getNumOfCandTimelinesBelowThresh() + ","
														+ seqIndex + "\n");// bwNumOfCandTimelinesBelowThreshold.newLine();

										Sanity.gt(recommMaster.getRankedRecommendedActNamesWithoutRankScores().length(),
												0,
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
											System.out.println(
													"recommMasters[i] .getRankedRecommendedActNamesWithoutRankScores()"
															+ recommMasters[seqIndex]
																	.getRankedRecommendedActNamesWithoutRankScores());
										}
										String[] splittedRankedRecommendedActName = RegexUtils.patternDoubleUnderScore
												.split(recommMasters[seqIndex]
														.getRankedRecommendedActNamesWithoutRankScores());

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
										topRecommendedPrimaryDimensionVal[seqIndex] = splittedRankedRecommendedActName[1];
										// PopUps.showMessage("here12_2");

										if (seqIndex < (recommSeqLength - 1))
										{
											// disabled on 20 Dec 2018
											// ActivityObject2018 repAOForTopRecommActName = RepresentativeAOInfo
											// .getRepresentativeAOForActName(Constant.preBuildRepAOGenericUser,
											// Constant.collaborativeCandidates,
											// Constant.buildRepAOJustInTime, mapOfRepAOs,
											// mapOfMedianPreSuccDurationInms, repAOResultGenericUser,
											// userId, recommendationTimes[seqIndex],
											// topRecommendedPrimaryDimensionVal[seqIndex], recommMaster,
											// this.primaryDimension,
											// trainTimelinesAllUsersContinuousFiltrd);

											RepresentativeAOInfo ree1 = repAOInfoForEachUser
													.get(String.valueOf(userId));
											// System.out.println("String.valueOf(userId) = " + String.valueOf(userId));
											// System.out.println("ree1 = " + ree1.toString());

											ActivityObject2018 repAOForTopRecommActName = ree1
													.getRepresentativeAOForActName(recommendationTimes[seqIndex],
															Integer.valueOf(
																	topRecommendedPrimaryDimensionVal[seqIndex]),
															recommMaster, this.primaryDimension, databaseName);

											// PopUps.showMessage("here12_3");
											repAOsFromPrevRecomms.add(repAOForTopRecommActName);
											recommendationTimes[seqIndex + 1] = repAOForTopRecommActName
													.getEndTimestamp();

											System.out.println("repAOForTopRecommActName = " + repAOForTopRecommActName
													.toStringAllGowallaTSWithNameForHeaded(","));
										} // PopUps.showMessage("here12_4");
									} // END of loop over iterative recommendation for each seq index

									if (skipThisRTNoCandTimelines)
									{
										System.out.println("Skipping this RT:skipThisRTNoCandTimelines="
												+ skipThisRTNoCandTimelines);
										continue; // continue to the next RT
									}
									// end of curtain April 7
									System.out.println("time taken by recommMaster = "
											+ (System.currentTimeMillis() - ta1) + " ms");
									// $$ Disabled for speed
									// $$ WritingToFile.appendLineToFileAbsolute(
									// $$ (System.currentTimeMillis() - ta1) + ","
									// $$ + PerformanceAnalytics.getUsedMemoryInMB() + "\n",
									// $$ Constant.getCommonPath() + "recommMasterPerformance.csv");

									System.out.print(
											"Back to RecommendationTests: received following #of cand timelines for mu "
													+ matchingUnit + ": (currentAOPDVal,numOfCands) -");
									Arrays.stream(recommMasters)
											.forEachOrdered(rm -> System.out.print(
													"__(" + rm.getActivityObjectAtRecomm().getPrimaryDimensionVal("||")
															+ rm.getNumOfCandidateTimelines() + ")"));
									System.out.println();

									if (VerbosityConstants.WriteNumActsPerRTPerCand)
									{
										RecommendationTestsUtils.writeNumOfActsInEachCand(numActsInEachCandbw, userId,
												dateToRecomm, recommMasters, recommTimesStrings);
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
									String actActualDone = nextValidActivityObjectAfterRecommPoint1
											.getPrimaryDimensionVal("/");// .getActivityName();
									ArrayList<String> actsActualDoneInSeq = (ArrayList<String>) nextValidActivityObjectsAfterRecommPoint1
											.stream().map(ao -> ao.getPrimaryDimensionVal("/"))
											.collect(Collectors.toList());

									String actsActualDoneInSeqString = nextValidActivityObjectsAfterRecommPoint1
											.stream().map(ao -> ao.getPrimaryDimensionVal("/"))
											.collect(Collectors.joining(">"));

									// current activity
									String actsAtRecommPoint = Arrays.stream(recommMasters)
											.map(rm -> rm.getActivityObjectAtRecomm().getPrimaryDimensionVal("/"))
											.collect(Collectors.joining(">"));

									ArrayList<String> rankedRecommWithScoreForThisRTIter = (ArrayList<String>) Arrays
											.stream(recommMasters)
											.map(rm -> rm.getRankedRecommendedActNamesWithRankScores())
											.collect(Collectors.toList());

									ArrayList<String> rankedRecommWithoutScoreForThisRTIter = (ArrayList<String>) Arrays
											.stream(recommMasters)
											.map(rm -> rm.getRankedRecommendedActNamesWithoutRankScores())
											.collect(Collectors.toList());

									// extract the top 1 ranked recommendation from each recommendation master
									String topRankedRecommSequenceWithScore = rankedRecommWithScoreForThisRTIter
											.stream().map(s -> RegexUtils.patternDoubleUnderScore.split(s)[1])
											.collect(Collectors.joining(">"));

									String topRankedRecommSequenceWithoutScore = rankedRecommWithScoreForThisRTIter
											.stream().map(s -> RegexUtils.patternDoubleUnderScore.split(s)[1])
											.map(s -> RegexUtils.patternColon.split(s)[0])
											.collect(Collectors.joining(">"));

									if (VerbosityConstants.verbose)
									{
										System.out
												.println("** Ranked Recommendation=" + topRankedRecommSequenceWithScore
														+ ", while actual was=" + actsActualDoneInSeqString);// rankedRecommWithoutScoreForThisRT
										System.out.println("topRankedRecommSequenceWithoutScore = "
												+ topRankedRecommSequenceWithoutScore);
									}
									// metaBufferWriter.write(userId + "_" + dateToRecomm + "_" + endTimeString + ",");
									metaToWriteForThisUserDate.append(userId).append("_").append(dateToRecomm)
											.append("_").append(recommTimesStrings[0]).append(",");

									// start of added on 28 Dec 2018
									validRtsAsUserIdDateIndexInDay.add(
											new Triple<Integer, Date, Integer>(userId, testDate, indexOfAOInTestDay));// activityObjectInTestDay.getStartTimestamp()));
									// end of added on 28 Dec 2018

									recommSequenceWithScoreForThisUserDate.append(topRankedRecommSequenceWithScore)
											.append(",");
									recommSequenceWithoutScoreForThisUserDate
											.append(topRankedRecommSequenceWithoutScore).append(",");

									// dataActualToWriteForThisUserDateIter.append(actActualDone).append(",");
									dataActualSeqActsToWriteForThisUserDate.append(actsActualDoneInSeqString)
											.append(",");

									for (int seqI = 0; seqI < this.recommSeqLength; seqI++)
									{
										// actualBufferWriter.write(actActualDone + ",");
										// write each actual done separately
										// IntStream.range(0, this.recommSeqLength).forEach(i ->
										// dataActualToWriteForThisUserDateIter.get(i).append(actsActualDoneInSeq.get(i)));
										if (VerbosityConstants.writeDataActualForEachSeqIndex)
										{
											sbsDataActualToWriteForThisUserDate.get(seqI)
													.append(actsActualDoneInSeq.get(seqI)).append(",");
										}

										// if (VerbosityConstants.writeCurrentTimelineForEachSeqIndex)
										// { sbsCurrentActivityToWriteForThisUserDate.get(seqI).append("").append(",");}

										rankedRecommWithScoreToWriteForThisUserDate.get(seqI)
												.append(rankedRecommWithScoreForThisRTIter.get(seqI)).append(",");

										if (VerbosityConstants.writeRankedRecommsWOScoreForEachSeqIndex)
										{
											rankedRecommWithoutScoreToWriteForThisUserDate.get(seqI)
													.append(rankedRecommWithoutScoreForThisRTIter.get(seqI))
													.append(",");
										}

										// HERE
										sbsNumOfCandTimelinesForThisUserDate.get(seqI)
												.append(userId + "," + recommendationTimes[seqI] + ","
														+ recommMasters[seqI].getNumOfCandTimelinesBelowThresh()
														+ "\n");
										// TODO disabled for speed on 1 Nov 2017 .append(new
										// HashSet<>(recommMasters[i].getCandUserIDs().values())).append("|")
										// .append(new HashSet<>(recommMasters[i].getCandUserIDs().size()))

										/////////////////
										String[] splittedRecomm = RegexUtils.patternDoubleUnderScore
												.split(rankedRecommWithoutScoreForThisRTIter.get(seqI));

										sbsMaxNumOfDistinctRecommendations.get(seqI)
												.append(dateToRecomm + "," + recommendationTimes[seqI] + "," + weekDay + // UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())+
														"," + (splittedRecomm.length - 1) + ","
														+ recommMasters[seqI].getNumOfCandTimelinesBelowThresh()
														+ "\n");

										if (VerbosityConstants.WriteRaw)
										{
											WToFile.writeRawLogs(userName, dateToRecomm, weekDay,
													sbsRawToWriteForThisUserDate.get(seqI), timeCategory,
													recommMasters[seqI], recommTimesStrings[seqI],
													actsActualDoneInSeq.get(seqI),
													rankedRecommWithScoreForThisRTIter.get(seqI));
										}
									}

									// start of curtain isCurrentTargetActSame 11 May
									// $$ disabled becuase not essential at the moment
									// char isCurrentTargetActSame; if (actAtRecommPoint.equals(actActualDone))
									// isCurrentTargetActSame = 't'; else isCurrentTargetActSame = 'f';
									// metaIfCurrentTargetSameToWriteForThisUserDate.append(isCurrentTargetActSame).append(",");
									// end of curtain isCurrentTargetActSame 11 May

									/*
									 * *********************************************************************************
									 * ***
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
										baseLineOccurrenceToWriteForThisUserDate
												.append(actNamesCountsWithoutCountOverTrain).append(",");
										// Added on 27 Dec 2018
										baseLineOccurrenceWithScoreToWriteForThisUserDate
												.append(actNamesCountsWithCountOverTrain).append(",");
									}
									if (Constant.DoBaselineDuration)
									{
										baseLineDurationToWriteForThisUserDate
												.append(actNamesDurationsWithoutDurationOverTrain).append(",");
										// Added on 27 Dec 2018
										baseLineDurationWithScoreToWriteForThisUserDate
												.append(actNamesDurationsWithDurationOverTrain).append(",");
									}

									/*
									 * *********************************************************************************
									 * ***
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
									topNextActsWithoutDistance
											.write(topNextActsWithoutDistToWriteForThisUserDate.toString());
								}

								if (VerbosityConstants.WriteTopNextActivitiesWithDistance)
								{
									topNextActsWithDistance.write(topNextActsWithDistToWriteForThisUserDate.toString());
								}

								if (Constant.DoBaselineOccurrence)
								{
									baseLineOccurrence.write(baseLineOccurrenceToWriteForThisUserDate.toString());
									// added 27 Dec 2018
									baseLineOccurrenceWithScore
											.write(baseLineOccurrenceWithScoreToWriteForThisUserDate.toString());
								}
								if (Constant.DoBaselineDuration)
								{
									baseLineDuration.write(baseLineDurationToWriteForThisUserDate.toString());
									// added 27 Dec 2018
									baseLineDurationWithScore
											.write(baseLineDurationWithScoreToWriteForThisUserDate.toString());
								}

								metaIfCurrentTargetSameWriter
										.write(metaIfCurrentTargetSameToWriteForThisUserDate.toString());

								for (int seqI = 0; seqI < this.recommSeqLength; seqI++)
								{
									if (VerbosityConstants.WriteRaw)
									{
										bwsRaw.get(seqI).write(sbsRawToWriteForThisUserDate.get(seqI).toString());
									}
									bwsRankedRecommWithScore.get(seqI)
											.write(rankedRecommWithScoreToWriteForThisUserDate.get(seqI).toString());
									if (VerbosityConstants.writeRankedRecommsWOScoreForEachSeqIndex)
									{
										bwRankedRecommWithoutScore.get(seqI).write(
												rankedRecommWithoutScoreToWriteForThisUserDate.get(seqI).toString());
									}
									numOfCandidateTimelinesWriter.get(seqI)
											.write(sbsNumOfCandTimelinesForThisUserDate.get(seqI).toString());

									if (VerbosityConstants.writeDataActualForEachSeqIndex)
									{
										bwsDataActual.get(seqI)
												.write(sbsDataActualToWriteForThisUserDate.get(seqI).toString());
									}
								}

								// numOfCandidateTimelinesWriter.write(numOfCandidateTimelinesForThisUserDate.toString());
								System.out.println("/end of for loop over all test dates");
							} // end of for loop over all test dates

							// Start of Added on Jan 15 2018
							int totalNumOfRTs = (numberOfMorningRTs + numberOfAfternoonRTs + numberOfEveningRTs);
							if (totalNumOfRTs <= 0)
							{
								WToFile.appendLineToFileAbs(
										userName + "," + numberOfMorningRTs + "," + numberOfAfternoonRTs + ","
												+ numberOfEveningRTs + "," + totalNumOfRTs + "\n",
										commonPath + "UsersWithNoValidRTs.csv");
								continue; // continue to the next user
							}
							// End of Added on Jan 15 2018

							bwCountTimeCategoryOfRecomm.write(userName + "," + numberOfMorningRTs + ","
									+ numberOfAfternoonRTs + "," + numberOfEveningRTs + "," + (totalNumOfRTs) + "\n");

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

							for (int seqI = 0; seqI < this.recommSeqLength; seqI++)
							{
								bwsRankedRecommWithScore.get(seqI).newLine();
								bwRankedRecommWithoutScore.get(seqI).newLine();
								bwsDataActual.get(seqI).newLine();
							}

							baseLineOccurrence.newLine();
							baseLineDuration.newLine();
							// added 27 Dec 2017
							baseLineOccurrenceWithScore.newLine();
							baseLineDurationWithScore.newLine();

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
								WToFile.writeToNewFile(sbsNumOfCandTimelinesBelowThreshold.toString(),
										commonPath + userName + "numberOfCandidateTimelinesBelow" + typeOfThreshold
												+ thresholdValue + ".csv");
							}
							// bwNumOfCandTimelinesBelowThreshold.close();
							System.out.println("//end of for over userID");
						} // end of for over userID
						numActsInEachCandbw.close();

						RecommendationTestsUtils.closeBWs(metaBw, recommSeqWithoutScoreBw, recommSeqWithScoreBw,
								actualSeqBw);
						RecommendationTestsUtils.closeBWs(topNextActsWithoutDistance, topNextActsWithDistance);
						RecommendationTestsUtils.closeBWs(rtsAllUsingRecommMasterWriter, rtsRejNoValidActAfterWriter,
								rtsRejWithNoCandsWriter, rtsRejWithNoCandsBelowThreshWriter,
								rtsRejWithNoDWButMUCandsCands, rtsRejBlackListedWriter);
						// changed 27 Dec 2018
						RecommendationTestsUtils.closeBWs(baseLineOccurrence, baseLineDuration,
								baseLineOccurrenceWithScore, baseLineDurationWithScore);
						RecommendationTestsUtils.closeBWs(bwNumOfWeekendsInTraining, bwNumOfWeekendsInAll,
								bwCountTimeCategoryOfRecomm);
						RecommendationTestsUtils.closeBWs(metaIfCurrentTargetSameWriter, bwNextActInvalid);
						RecommendationTestsUtils.closeBWs(bwWriteNormalisationOfDistance, bwNumOfValidAOsAfterRTInDay);

						for (int i = 0; i < this.recommSeqLength; i++)
						{
							RecommendationTestsUtils.closeBWs(bwsRankedRecommWithScore.get(i),
									bwRankedRecommWithoutScore.get(i), bwsDataActual.get(i), bwsRaw.get(i),
									numOfCandidateTimelinesWriter.get(i));
						}

						// $$new Evaluation();

						System.out.println("Pruning has Saturated is :" + pruningHasSaturated);
						// String msg = "";

						if (pruningHasSaturated)
						{
							System.out.println("Pruning has Saturated");
							// PopUps.showMessage("Pruning has Saturated");
						}

						System.out.println("LookPastType: " + this.lookPastType
								+ "Total Number of Timelines created for matching unit (" + matchingUnit + ") ="
								+ Timeline.getCountTimelinesCreatedUntilNow());
						System.out.println("ALL TESTS DONE for this matching unit");

						long recommTestsEndtime = System.currentTimeMillis();

						System.out.println("Time taken for executing Recommendation Tests for this matching unit ="
								+ (recommTestsEndtime - ctmu1) / 1000 + "seconds");
						WToFile.appendLineToFileAbs(
								"MU=" + matchingUnit + ",time(s)=" + ((recommTestsEndtime - ctmu1) / 1000) + "\n",
								Constant.getOutputCoreResultsPath() + "RecommendationTestsTimeTaken.csv");
						// writeRepAOs(mapOfRepAOs, mapOfMedianPreSuccDuration, Constant.getCommonPath());

						// Start of added on 20 Nov 2018
						if (altSeqPredictor.equals(AltSeqPredictor.None) && VerbosityConstants.verboseDistDistribution
								&& Constant.useRTVerseNormalisationForED)
						{// can do this only if distance distribution files are written.
							ResultsDistributionEvaluation.writeCorrelationBetweenDistancesOverCands(
									Constant.getCommonPath(), Constant.useRTVerseNormalisationForED);
						}
						// End of added on 20 Nov 2018
						consoleLogStream.close();
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

		// added on 28 Dec 2018
		if (Constant.doForJupyterBaselines)
		{
			DataTransformerForSessionBasedRecAlgos.writeValidRTs2(validRtsAsUserIdDateIndexInDay,
					Constant.getOutputCoreResultsPath());
		} // PopUps.showMessage("ALL TESTS DONE... u can shutdown the server");// +msg);
		System.out.println("**********Exiting Recommendation Tests**********");

	}

	// End of added on 20 Dec 2018

	//

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

}
