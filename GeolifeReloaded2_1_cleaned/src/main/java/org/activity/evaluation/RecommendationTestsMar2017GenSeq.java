package org.activity.evaluation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums;
import org.activity.constants.Enums.CaseType;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.SeqRecommConstants;
import org.activity.constants.VerbosityConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.recomm.RecommendationMasterI;
import org.activity.recomm.RecommendationMasterMar2017Gen;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ConnectDatabase;
import org.activity.util.DateTimeUtils;
import org.activity.util.PerformanceAnalytics;
import org.activity.util.RegexUtils;
import org.activity.util.StringUtils;
import org.activity.util.TimelineUtils;

/**
 * Fork of org.activity.evaluation.RecommendationTestsMar2017Gen, extending it to recommending sequences Executes the
 * experiments for generating recommendations
 * 
 * @author gunjan
 *
 */
public class RecommendationTestsMar2017GenSeq
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
	int globalThresholds[] = { 10000000 };// {50,100,150,200,250,300,350,400,450,500,550,600,650,700,1000};
	int percentThresholds[] = { 100 };// {50,60,70,80,90,100};

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
	 * @param usersTimelines
	 * @param lookPastType
	 * @param caseType
	 * @param typeOfThresholds
	 * @param userIDs
	 * @param percentageInTraining
	 */
	public RecommendationTestsMar2017GenSeq(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines,
			Enums.LookPastType lookPastType, Enums.CaseType caseType, Enums.TypeOfThreshold[] typeOfThresholds,
			int[] userIDs, double percentageInTraining)// , int lengthOfRecommendedSequence)
	{
		System.out.println(
				"\n\n **********Entering RecommendationTestsMar2017********** " + lookPastType + " " + caseType);
		long recommTestsStarttime = System.currentTimeMillis();

		this.lookPastType = lookPastType;
		this.caseType = caseType;

		this.percentageInTraining = percentageInTraining;
		this.typeOfThresholds = typeOfThresholds;
		this.userIDs = userIDs;

		this.recommSeqLength = SeqRecommConstants.RecommSeqLen;

		if (userIDs == null || userIDs.length == 0) // if userid is not set in constant class, in case of gowalla
		{
			userIDs = usersTimelines.keySet().stream().mapToInt(userID -> Integer.valueOf(userID)).toArray();
			System.out.println("UserIDs not set in Constant, hence extracted" + userIDs.length
					+ " user ids from usersTimelines keyset");
			Constant.setUserIDs(userIDs);
		}
		// System.out.println("User ids = " + Arrays.toString(userIDs));

		// Rts not used in daywise matching owing to unavailability of cand timelines for them
		List<String> blackListedRTs = null;
		if (Constant.BLACKLISTING)
		{
			blackListedRTs = ReadingFromFile.getBlackListedRTs(Constant.getDatabaseName());
		}

		// check if directory is empty to prevent overwriting of results
		// if (UtilityBelt.isDirectoryEmpty(Constant.outputCoreResultsPath) == false)
		// { System.err.println("Warning with exit: results' directory not empty");
		// System.exit(-1);}

		setMatchingUnitArray(lookPastType);

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
						long ctmu1 = System.currentTimeMillis();
						TreeMap<Integer, Integer> userIdNumOfRTsMap = new TreeMap<Integer, Integer>();

						Constant.setCurrentMatchingUnit(matchingUnit); // used for sanity checks
						System.out.println("Executing RecommendationTests for matching unit: " + matchingUnit);

						// matching unit is only relevant if it is not daywise
						if (this.lookPastType.equals(Enums.LookPastType.NCount)
								|| this.lookPastType.equals(Enums.LookPastType.NHours))
						{
							String dirToCreate = Constant.outputCoreResultsPath + "/MatchingUnit"
									+ String.valueOf(matchingUnit);
							WritingToFile.createDirectory(dirToCreate);// Creating the directory for that matching unit
							commonPath = dirToCreate + "/";
						}
						else // daywise
						{
							commonPath = Constant.outputCoreResultsPath;
						}

						Constant.setCommonPath(commonPath);
						System.out.println("Common path=" + Constant.getCommonPath());

						PrintStream consoleLogStream = WritingToFile
								.redirectConsoleOutput(commonPath + "consoleLog.txt");

						BufferedWriter metaBufferWriter = WritingToFile.getBWForNewFile(commonPath + "meta.csv");
						BufferedWriter actualBufferWriter = WritingToFile
								.getBWForNewFile(commonPath + "dataActual.csv");

						BufferedWriter topNextActsWithoutDistance = WritingToFile
								.getBWForNewFile(commonPath + "topNextActivitiesWithoutDistance.csv");
						BufferedWriter topNextActsWithDistance = WritingToFile
								.getBWForNewFile(commonPath + "topNextActivitiesWithDistance.csv");
						BufferedWriter rtsInvalidWriter = WritingToFile
								.getBWForNewFile(commonPath + "recommPointsInvalidBecuzNoValidActivityAfterThis.csv");
						BufferedWriter rtsWithNoCandsWriter = WritingToFile
								.getBWForNewFile(commonPath + "recommPointsWithNoCandidates.csv");

						BufferedWriter rtsRejWithNoDWButMUCandsCands = WritingToFile
								.getBWForNewFile(commonPath + "recommPointsWithNoDWButMUCandidates.csv");

						BufferedWriter rankedRecommWithScoreWriter = WritingToFile
								.getBWForNewFile(commonPath + "dataRankedRecommendationWithScores.csv");
						BufferedWriter rankedRecommWithoutScoreWriter = WritingToFile
								.getBWForNewFile(commonPath + "dataRankedRecommendationWithoutScores.csv");

						BufferedWriter metaIfCurrentTargetSameWriter = WritingToFile
								.getBWForNewFile(commonPath + "metaIfCurrentTargetSameWriter.csv");

						BufferedWriter numOfCandidateTimelinesWriter = WritingToFile
								.getBWForNewFile(commonPath + "numOfCandidateTimelines.csv");

						/**
						 * Contains list of activity names sorted by frequency of occurrence/duration. Num of unique
						 * sorted lists = number of users, however, each list is repeated so as maintain structural
						 * conformity with dataRankedRecommendationWithoutScores.csv
						 */
						BufferedWriter baseLineOccurrence = WritingToFile
								.getBWForNewFile(commonPath + "dataBaseLineOccurrence.csv");
						BufferedWriter baseLineDuration = WritingToFile
								.getBWForNewFile(commonPath + "dataBaseLineDuration.csv");

						BufferedWriter bwRaw = WritingToFile.getBWForNewFile(commonPath + "Raw.csv");

						BufferedWriter bwNumOfWeekendsInTraining = WritingToFile
								.getBWForNewFile(commonPath + "NumberOfWeekendsInTraining.csv");
						BufferedWriter bwNumOfWeekendsInAll = WritingToFile
								.getBWForNewFile(commonPath + "NumberOfWeekendsInAll.csv");
						BufferedWriter bwCountTimeCategoryOfRecomm = WritingToFile
								.getBWForNewFile(commonPath + "CountTimeCategoryOfRecommPoitns.csv");
						BufferedWriter bwNextActInvalid = WritingToFile
								.getBWForNewFile(commonPath + "NextActivityIsInvalid.csv");
						BufferedWriter bwWriteNormalisationOfDistance = WritingToFile
								.getBWForNewFile(commonPath + "NormalisationDistances.csv");

						BufferedWriter bwNumOfValidAOsAfterRTInDay = WritingToFile
								.getBWForNewFile(commonPath + "NumOfValidAOsAfterRTInDay.csv");

						rtsInvalidWriter.write(
								"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity\n");
						rtsWithNoCandsWriter.write(
								"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity,TotalNumOfPossibleCands, NumCandsRejectedDueToNoCurrentActivityAtNonLast,NumCandsRejectedDueToNoNextActivity\n");

						bwRaw.write(
								"User,DateOfRecomm,TimeOfRecomm,TimeCategoryOfRecomm,CurrentTimeline, CurrentActivity(ActivityAtRecommPoint),NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline,NumOfCandidateTimelinesBelowThresh, WeekDayOfRecomm,Target(ActualActivity), RecommendedActivities, NumOfDistinctRecomms, PearsonCorrOfCandSimsAndEndCaseSims, AvgRestSimilarity, StdDevRestSimilarity, AvgEndSimilarity, StdDevEndSimilarity,IsCurrentActTargetActSame\n");// LastActivityOnRecommDay");//,ActivitiesOnRecommDayUntiRecomm");

						bwCountTimeCategoryOfRecomm
								.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings,TotalRTs\n");

						bwNumOfWeekendsInTraining.write("User,NumOfWeekends,NumOfWeekdays\n");

						bwNextActInvalid.write("User,Timestamp_of_Recomm\n");

						// bwCountInActivitiesGuidingRecomm.write("User,RecommendationTime,TimeCategory,NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline\n");

						BufferedWriter numActsInEachCandbw = WritingToFile.getBWForNewFile(commonPath + "NumActs.csv");
						// commonPath + "NumActsmatchingUnit" + String.valueOf(matchingUnit) + ".csv");
						numActsInEachCandbw.write(
								"NumOfActObjsInCand-1,candTimelineID,UserId, DateAtRT, TimeAtRT, ActObjsInCand\n");

						bwWriteNormalisationOfDistance
								.write("User, DateOfRecomm, TimeOfRecom, EditDistance,NormalisedEditDistance\n");

						bwNumOfValidAOsAfterRTInDay
								.write("User, DateOfRecomm, TimeOfRecom, NumOfValidsAOsAfterRTInDay\n");

						// writes the edit similarity calculations for this recommendation master
						// WritingToFile.writeEditSimilarityCalculationsHeader();
						WritingToFile.writeToNewFile(
								"UserAtRecomm,DateAtRecomm,TimeAtRecomm,CandidateTimelineID,EditDistance,ActLevelDistance,FeatLevelDistance,Trace, ActivityObjects1,ActivityObjects2\n",
								commonPath + "EditSimilarityCalculations.csv");

						// writes the header for EditDistancePerRtPerCand.csv//
						// WritingToFile.writeDistanceScoresSortedMapHeader();
						// for org.activity.io.WritingToFile.writeEditDistancesPerRtPerCand() which is called in recomm
						// master
						WritingToFile.writeToNewFile(
								"UserAtRecomm,DateAtRecomm,TimeAtRecomm,CandID,EndPointIndexOfCand, EditOpsTraceOfCand,EditDistOfCand,#L1_EditOps, #ObjInSameOrder_#L2EditOps,NextActivityForRecomm, diffSTEndPointsCand_n_CurrActInSecs,diffETEndPointsCand_n_CurrActInSecs,CandidateTimeline,CurrentTimeline\n",
								commonPath + "EditDistancePerRtPerCand.csv");

						System.out.println(Constant.getAllGlobalConstants());
						System.out.println(Constant.getCommonPath());

						/** Can be used to select users above 10 RTs **/
						LinkedHashMap<Integer, Integer> numOfValidRTs = new LinkedHashMap<Integer, Integer>();

						// LinkedHashMap<Integer, LinkedHashMap<Timestamp, Integer>> rtsWithMoreThan4ValidsAfter = new
						// LinkedHashMap<>();
						StringBuilder sbNumOfValidsAfterAnRT = new StringBuilder();

						// int userCount = 0;
						/**
						 * This can be made class variable and does not need to be repeated for each matching unit, but
						 * in current setup we are doing it for each matching unit. Note: this is very small performance
						 * effect, hence should not be of major concern. (UserId,ActName,RepresentativeAO)
						 */
						LinkedHashMap<Integer, LinkedHashMap<String, ActivityObject>> mapOfRepAOs = new LinkedHashMap<>();
						LinkedHashMap<Integer, LinkedHashMap<String, Pair<Double, Double>>> mapOfMedianPreSuccDuration = new LinkedHashMap<>();

						for (int userId : userIDs) // for(int userId=minTestUser;userId <=maxTestUser;userId++)
						{ // int numberOfValidRTs = 0;// userCount += 1;
							System.out.println("\nUser id=" + userId);
							String userName = "";
							if (Constant.getDatabaseName().equals("gowalla1"))
							{
								userName = String.valueOf(userId);
							}
							else
							{
								userName = ConnectDatabase.getUserName(userId);// ConnectDatabase.getUserNameFromDatabase(userId);
							}

							if (Constant.blacklistingUsersWithLargeMaxActsPerDay)
							{
								if (Constant.getDatabaseName().equals("gowalla1"))
								{
									if (Arrays.asList(DomainConstants.gowallaUserIDsWithGT553MaxActsPerDay)
											.contains(userId))
									{
										System.out.println("Skipping user: " + userId
												+ " as in gowallaUserIDsWithGT553MaxActsPerDay");
										continue;
									}
								}
								else
								{
									System.err.println("Warning: Constant.blacklistingUsersWithLargeMaxActsPerDay= "
											+ Constant.blacklistingUsersWithLargeMaxActsPerDay
											+ " but blacklisted user not defined for this database");
								}
							}

							// BufferedWriter bwMaxNumOfDistinctRecommendations = WritingToFile
							// .getBWForNewFile(commonPath + userName + "MaxNumberOfDistinctRecommendation.csv");
							// bwMaxNumOfDistinctRecommendations.write("DateOfRecomm" + ",TimeOfRecomm"
							// + ",Week_Day,MaxNumOfDistictRecommendation,NumOfCandidateTimelines(after applying
							// Threshold)\n");

							// replacing iterative write with StringBuilder for better performance
							StringBuilder sbMaxNumOfDistinctRecommendations = new StringBuilder();
							sbMaxNumOfDistinctRecommendations.append(
									"DateOfRecomm ,TimeOfRecomm ,Week_Day,MaxNumOfDistictRecommendation,NumOfCandidateTimelines(after applying Threshold)\n");

							// BufferedWriter bwNumOfCandTimelinesBelowThreshold = WritingToFile
							// .getBWForNewFile(commonPath + userName + "numberOfCandidateTimelinesBelow"
							// + typeOfThreshold + thresholdValue + ".csv");
							// bwNumOfCandTimelinesBelowThreshold.write("DateOfRecomm" + ",TimeOfRecomm"
							// + ",Week_Day,ThresholdAsDistance,NumOfCandidateTimelinesBelowThreshold,\n");

							// replacing iterative write with StringBuilder for better performance
							StringBuilder sbNumOfCandTimelinesBelowThreshold = new StringBuilder();
							sbNumOfCandTimelinesBelowThreshold.append(
									"DateOfRecomm ,TimeOfRecomm ,Week_Day,ThresholdAsDistance,NumOfCandidateTimelinesBelowThreshold,\n");

							// BufferedWriter bwRecommTimesWithEditDistances = WritingToFile
							// .getBWForNewFile(commonPath + userName + "RecommTimesWithEditDistance.csv");
							// bwRecommTimesWithEditDistances.write("DateOfRecomm" + ",TimeOfRecomm,"
							// +
							// "CandidateTimelineID,TargetActivity,EditDistanceOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,EndIndexOf(Sub)Cand,CandidateTimeline,WeekDayOfRecomm\n");
							StringBuilder sbRecommTimesWithEditDistances = new StringBuilder();
							sbRecommTimesWithEditDistances.append("DateOfRecomm" + ",TimeOfRecomm,"
									+ "CandidateTimelineID,TargetActivity,EditDistanceOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,EndIndexOf(Sub)Cand,CandidateTimeline,WeekDayOfRecomm\n");

							userAllDatesTimeslines = usersTimelines.get(Integer.toString(userId));// userId);

							if (userAllDatesTimeslines == null)
							{
								System.err.println("Error: userAllDatesTimeslines = " + userAllDatesTimeslines
										+ " user " + userId);
							}

							// //////////////////REMOVING SELECTED TIMELINES FROM DATASET////////////////////
							userAllDatesTimeslines = TimelineUtils.cleanUserDayTimelines(userAllDatesTimeslines,
									commonPath + "InsideRecommTestCleanUserDayTimelines", String.valueOf(userId));
							// ////////////////////////////////////////////////////////////////////////////////
							if (this.writeDayTimelinesOnce)
							{// if (matchingUnitIterator == 0) // write the given day timelines only once
								WritingToFile.writeGivenDayTimelines(userName, userAllDatesTimeslines, "All", true,
										true, true);
								this.writeDayTimelinesOnce = false;
							}

							// Splitting the set of timelines into training set and test set.
							List<LinkedHashMap<Date, Timeline>> trainTestTimelines = TimelineUtils
									.splitTestTrainingTimelines(userAllDatesTimeslines, percentageInTraining);
							LinkedHashMap<Date, Timeline> userTrainingTimelines = trainTestTimelines.get(0);
							LinkedHashMap<Date, Timeline> userTestTimelines = trainTestTimelines.get(1);

							////// START of build representative activity objects for this user.
							// if (true)// representativeAOsNotComputed == false) //do this
							// {// we only need to do it once for each user and dont need to repeat it for each matching
							// unit, so we can take it out of the for loop, however, it that we will also need to
							// take out the train-test splitting of timelines out of the loop, which can be done as
							// well
							Pair<LinkedHashMap<String, ActivityObject>, LinkedHashMap<String, Pair<Double, Double>>> repAOResult = buildRepresentativeAOsForUser(
									userId, TimelineUtils.dayTimelinesToATimeline(userTrainingTimelines, false, true),
									Constant.getActivityNames(),
									TimelineUtils.dayTimelinesToATimeline(userTestTimelines, false, true));
							LinkedHashMap<String, ActivityObject> repAOsForThisUser = repAOResult.getFirst();
							mapOfRepAOs.put(userId, repAOsForThisUser);
							mapOfMedianPreSuccDuration.put(userId, repAOResult.getSecond());
							// continue;
							// }
							////// END of build representative activity objects for this user.

							if (userTrainingTimelines.size() == 0)
							{
								System.out.println(
										"Warning: Skipping this user " + userId + " as it has 0 training days");
								WritingToFile.appendLineToFileAbsolute("User " + userId + ",",
										commonPath + "UserWithNoTrainingDay.csv");
								numOfValidRTs.put(userId, 0);
								continue;
							}

							/////////// WRITE activity names as sequence for HMM or markov chains
							// $$ if (true)
							// {
							// TimelineTransformers.writeUserActNamesSeqAsNames(userId, userTrainingTimelines,
							// userTestTimelines, userAllDatesTimeslines);
							// continue;// TEMPORARY // continue to next user
							// $$ }
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
							 * ********** ************************************************
							 * ********************************
							 */

							int numberOfWeekendsInTraining = TimelineUtils
									.getNumberOfWeekendsInGivenDayTimelines(userTrainingTimelines);
							int numberOfWeekdaysInTraining = userTrainingTimelines.size() - numberOfWeekendsInTraining;
							bwNumOfWeekendsInTraining.write(userName + "," + numberOfWeekendsInTraining + ","
									+ numberOfWeekdaysInTraining + "\n");

							int numberOfWeekendsInAll = TimelineUtils
									.getNumberOfWeekendsInGivenDayTimelines(userAllDatesTimeslines);
							int numberOfWeekdaysInAll = userAllDatesTimeslines.size() - numberOfWeekendsInAll;
							bwNumOfWeekendsInAll
									.write(userName + "," + numberOfWeekendsInAll + "," + numberOfWeekdaysInAll + "\n");
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

								ArrayList<ActivityObject> activityObjectsInTestDay = testDayTimelineForUser
										.getActivityObjectsInDay();

								////////// added to improve write speed
								StringBuilder bwRawToWriteForThisUserDate = new StringBuilder();
								StringBuilder metaToWriteForThisUserDate = new StringBuilder();
								StringBuilder dataActualToWriteForThisUserDate = new StringBuilder();
								StringBuilder metaIfCurrentTargetSameToWriteForThisUserDate = new StringBuilder();
								StringBuilder numOfCandidateTimelinesForThisUserDate = new StringBuilder();

								StringBuilder topNextActsWithoutDistToWriteForThisUserDate = new StringBuilder();
								StringBuilder topNextActsWithDistToWriteForThisUserDate = new StringBuilder();
								StringBuilder rankedRecommWithScoreToWriteForThisUserDate = new StringBuilder();
								StringBuilder rankedRecommWithoutScoreToWriteForThisUserDate = new StringBuilder();
								StringBuilder baseLineOccurrenceToWriteForThisUserDate = new StringBuilder();
								StringBuilder baseLineDurationToWriteForThisUserDate = new StringBuilder();
								///////////

								// loop over the activity objects for this day
								// will not make recommendation for days which have only one activity
								for (int indexOfAOInDay = 0; indexOfAOInDay < activityObjectsInTestDay.size()
										- 1; indexOfAOInDay++)
								{
									ActivityObject activityObjectInTestDay = activityObjectsInTestDay
											.get(indexOfAOInDay);
									String activityNameInTestDay = activityObjectInTestDay.getActivityName();

									System.out.println(
											"----\nIterating over potential recommendation times: current activityNameInTestDay="
													+ activityNameInTestDay);// (activityObjectsInThatDay.get(j).getActivityName()));

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
									String endTimeString = endTimeStamp.getHours() + ":" + endTimeStamp.getMinutes()
											+ ":" + endTimeStamp.getSeconds();
									String timeCategory = DateTimeUtils
											.getTimeCategoryOfTheDay(endTimeStamp.getHours());

									// SeqChange to check for more number of activities after the recomm point
									if (TimelineUtils.hasAtleastNValidAOsAfterItInTheDay(indexOfAOInDay,
											testDayTimelineForUser, recommSeqLength) == false)
									{
										System.out.println("Skipping this recommendation point because there less than "
												+ recommSeqLength + " activity objects after this in the day");
										rtsInvalidWriter.write(userId + "," + dateToRecomm + "," + indexOfAOInDay + ","
												+ endTimeStamp + "," + weekDay + "," + timeCategory
												+ activityNameInTestDay + "\n");
										continue;
									}

									// Target Activity, actual next activity
									ActivityObject nextValidActivityObjectAfterRecommPoint1 = testDayTimelineForUser
											.getNextValidActivityAfterActivityAtThisTime(
													new Timestamp(year - 1900, month - 1, date, endTimeStamp.getHours(),
															endTimeStamp.getMinutes(), endTimeStamp.getSeconds(), 0));

									// TODO: check if it is giving correct results: concern: passing end ts directly and
									// using LocalDate for comparison
									ArrayList<ActivityObject> nextValidActivityObjectsAfterRecommPoint1 = TimelineUtils
											.getNextNValidAOsAfterActivityAtThisTimeSameDay(testDayTimelineForUser,
													endTimeStamp, recommSeqLength);

									// timestamp sanity check start
									{
										Timestamp temp1 = new Timestamp(year - 1900, month - 1, date,
												endTimeStamp.getHours(), endTimeStamp.getMinutes(),
												endTimeStamp.getSeconds(), 0);

										System.out
												.println("Debug: timestamp concern line 534 temp1.equals(endTimeStamp)"
														+ temp1.equals(endTimeStamp));
										if (!temp1.equals(endTimeStamp))
										{
											System.err.println(PopUps.getCurrentStackTracedErrorMsg(
													"Error in sanity check timestamp concern line 534 "));
										}
									}
									// timestamp sanity check start

									if (nextValidActivityObjectsAfterRecommPoint1.size() != recommSeqLength)
									{
										System.err.println(PopUps
												.getCurrentStackTracedErrorMsg("Error in Sanity Check RT407: User id"
														+ userId + " Next activity Objects after " + endTimeStamp
														+ " nextValidActivityObjectsAfterRecommPoint1.size() ="
														+ nextValidActivityObjectsAfterRecommPoint1.size()
														+ " expected =" + recommSeqLength
														+ "\nif it was such, we should have not reached this point of execution"));
										// because hasAtleastNValidAOsAfterItInTheDay already checked if there exists N
										// next valid activity
										PopUps.showError(
												"Error in Sanity Check RT407: nextValidActivityObjectsAfterRecommPoint1.size()!=recommSeqLength, if it was such, we should have not reached this point of execution");
									}

									System.out.println("User id" + userId + " Next activity Object after recomm time:"
											+ endTimeStamp + " ="
											+ nextValidActivityObjectAfterRecommPoint1.getActivityName());

									System.out.println("User id" + userId + " Next activity Objects after recomm time:"
											+ endTimeStamp + " =");
									nextValidActivityObjectsAfterRecommPoint1.stream()
											.forEachOrdered(ao -> System.out.print(ao.getActivityName() + ","));

									// sanity check
									if (nextValidActivityObjectAfterRecommPoint1.getActivityName()
											.equals(nextValidActivityObjectsAfterRecommPoint1.get(0)
													.getActivityName()) == false)
									{
										System.err.println(PopUps.getCurrentStackTracedErrorMsg(
												"Error in Sanity check 575\nnextValidActivityObjectAfterRecommPoint1.getActivityName() ="
														+ nextValidActivityObjectAfterRecommPoint1.getActivityName()
														+ "!= nextValidActivityObjectsAfterRecommPoint1.get(0).getActivityName()"
														+ nextValidActivityObjectsAfterRecommPoint1.get(0)
																.getActivityName()));
									}

									if (VerbosityConstants.WriteNumOfValidsAfterAnRTInSameDay)
									{
										int numOfValidsAOsAfterThisRT = testDayTimelineForUser
												.getNumOfValidActivityObjectsAfterThisTimeInSameDay(endTimeStamp);
										// .getNumOfValidActivityObjectAfterThisTime(endTimeStamp);
										sbNumOfValidsAfterAnRT.append(userId + "," + dateToRecomm + "," + endTimeStamp
												+ "," + numOfValidsAOsAfterThisRT + "\n");
										// if (numOfValidsAOsAfterThisRT >= 4) { WritingToFile.appendLineToFileAbsolute(
										// userId + "," + dateToRecomm + "," + endTimeStamp + ","
										// + numOfValidsAOsAfterThisRT + "\n", commonPath +
										// "numOfValidsAfterRTsGEQ4.csv"); }
									}
									// ///////////
									// Now we have those recommendation times which are valid for making recommendations
									// ///////////////////Start//////////////////////////////////
									// /IMPORTANT
									long ta1 = System.currentTimeMillis();

									// start of curtain April 7
									RecommendationMasterI recommP1 = new RecommendationMasterMar2017Gen(
											userTrainingTimelines, userTestTimelines, dateToRecomm, endTimeString,
											userId, thresholdValue, typeOfThreshold, matchingUnit, caseType,
											this.lookPastType, false);
									// end of curtain April 7

									System.out.println("time taken by recommMaster = "
											+ (System.currentTimeMillis() - ta1) + " ms");

									WritingToFile.appendLineToFileAbsolute(
											(System.currentTimeMillis() - ta1) + ","
													+ PerformanceAnalytics.getUsedMemoryInMB() + "\n",
											Constant.getCommonPath() + "recommMasterPerformance.csv");

									System.out.println("Back to RecommendationTests: received "
											+ recommP1.getNumOfCandidateTimelines()// candidateTimelines.size()
											+ " candidate timelines for matching unit " + matchingUnit);

									if (VerbosityConstants.WriteNumActsPerRTPerCand)
									{
										StringBuilder tmpWriter = new StringBuilder();
										// System.out.println("\tIterating over candidate timelines:");
										for (Timeline candtt1 : recommP1.getOnlyCandidateTimeslines())// candidateTimelines.entrySet())
										{
											// Timeline candtt1 = entryAjooba.getValue(); // ArrayList<ActivityObject>
											// aa1=candtt1.getActivityObjectsInTimeline();
											// excluding the current activity at the end of the candidate timeline
											// System.out.println("Number of activity Objects in this timeline (except
											// the end current activity) is: "+sizez1);
											tmpWriter = StringUtils.fCat(tmpWriter,
													String.valueOf(candtt1.countNumberOfValidActivities() - 1), ",",
													candtt1.getTimelineID(), ",", Integer.toString(userId), ",",
													dateToRecomm, ",", endTimeString, ",",
													candtt1.getActivityObjectNamesWithTimestampsInSequence(), "\n");
										}
										numActsInEachCandbw.write(tmpWriter.toString());
									}

									double thresholdAsDistance = recommP1.getThresholdAsDistance();

									if (recommP1.hasThresholdPruningNoEffect() == false)
									{
										pruningHasSaturated = false;
									}

									// pruningHasSaturated=recommP1.hasThresholdPruningNoEffect();

									if (recommP1.hasCandidateTimeslines() == false)
									{
										rtsWithNoCandsWriter.write(userId + "," + dateToRecomm + "," + indexOfAOInDay
												+ "," + endTimeStamp + "," + weekDay + "," + timeCategory + ","
												+ recommP1.getActivityObjectAtRecomm().getActivityName() + ",\n");// $$+recommP1.totalNumberOfProbableCands+","+recommP1.numCandsRejectedDueToNoCurrentActivityAtNonLast+","+recommP1.numCandsRejectedDueToNoNextActivity);
										System.out.println(
												"Cannot make recommendation at this point as there are no candidate timelines");

										// bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimeStamp +
										// "," + weekDay + "," + thresholdAsDistance + "," + 0 + "\n");
										sbNumOfCandTimelinesBelowThreshold.append(dateToRecomm + "," + endTimeStamp
												+ "," + weekDay + "," + thresholdAsDistance + "," + 0 + "\n");// bwNumOfCandTimelinesBelowThreshold.newLine();

										continue;
									}

									////////////////////////////////////////////////////////////////////////
									// check if this RT will have daywise candidate timelines
									boolean hasDayWiseCandidateTimelines = TimelineUtils.hasDaywiseCandidateTimelines(
											userTrainingTimelines, recommP1.getActsGuidingRecomm(),
											recommP1.getDateAtRecomm(), recommP1.getActivityObjectAtRecomm());

									if (hasDayWiseCandidateTimelines == false)
									{
										rtsRejWithNoDWButMUCandsCands
												.write(userId + "," + dateToRecomm + "," + indexOfAOInDay + ","
														+ endTimeStamp + "," + weekDay + "," + timeCategory + ","
														+ recommP1.getActivityObjectAtRecomm().getActivityName()
														+ "HasMUCandsButNoDWCands,\n");
										System.out.println(
												"Cannot make recommendation at this point as there are no daywise candidate timelines, even though there are mu candidate timelines");

										// bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimeStamp +
										// ","
										// + weekDay + "," + thresholdAsDistance + "," + 0 + "\n");
										sbNumOfCandTimelinesBelowThreshold.append(dateToRecomm + "," + endTimeStamp
												+ "," + weekDay + "," + thresholdAsDistance + "," + 0 + "\n");
										continue;
									}
									///////////////////////////////////////////////////////////////////////

									// ////////////////////////////Start of New addition for blacklisted RTs
									if (Constant.BLACKLISTING)
									{
										if (blackListedRTs.contains(new String(userId + " " + endTimeStamp)))
										{
											System.out.println("Alert: blacklisted RT: " + userId + " " + endTimeStamp
													+ " will not be used and will be logged in rtsWithNoCands");
											rtsWithNoCandsWriter.write(userId + "," + dateToRecomm + ","
													+ indexOfAOInDay + "," + endTimeStamp + "," + weekDay + ","
													+ timeCategory + ","
													+ recommP1.getActivityObjectAtRecomm().getActivityName() + ",");// $$+recommP1.totalNumberOfProbableCands+","+recommP1.numCandsRejectedDueToNoCurrentActivityAtNonLast+","+recommP1.numCandsRejectedDueToNoNextActivity);
											rtsWithNoCandsWriter.newLine();

											// bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," +
											// endTimeStamp + "," + weekDay + "," + thresholdAsDistance + "," + 0);
											// bwNumOfCandTimelinesBelowThreshold.newLine();
											sbNumOfCandTimelinesBelowThreshold.append(dateToRecomm + "," + endTimeStamp
													+ "," + weekDay + "," + thresholdAsDistance + "," + 0 + "\n");// bwNumOfCandTimelinesBelowThreshold.newLine();

											continue;

										}
									}
									// ////////////////////////////End of New addition for blacklisted RTs

									if (recommP1.hasCandidateTimelinesBelowThreshold() == false)
									{
										// bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimeStamp +
										// "," + weekDay + "," + thresholdAsDistance + "," + 0);
										// bwNumOfCandTimelinesBelowThreshold.newLine();
										sbNumOfCandTimelinesBelowThreshold.append(dateToRecomm + "," + endTimeStamp
												+ "," + weekDay + "," + thresholdAsDistance + "," + 0 + "\n");

										System.out.println(
												"Cannot make recommendation at this point as there are no candidate timelines BELOW THRESHOLD");
										continue;
									}

									// bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimeStamp + ","
									// + weekDay + "," + thresholdAsDistance + "," +
									// recommP1.getNumberOfCandidateTimelinesBelowThreshold());
									// bwNumOfCandTimelinesBelowThreshold.newLine();

									sbNumOfCandTimelinesBelowThreshold.append(dateToRecomm + "," + endTimeStamp + ","
											+ weekDay + "," + thresholdAsDistance + ","
											+ recommP1.getNumOfCandTimelinesBelowThresh() + "\n");// bwNumOfCandTimelinesBelowThreshold.newLine();

									if (recommP1.getRankedRecommendedActNamesWithoutRankScores().length() <= 0)
									{
										System.err.println(PopUps.getCurrentStackTracedErrorMsg(
												"Error in Sanity Check RT500:recommP1.getRankedRecommendedActivityNamesWithoutRankScores().length()<=0, but there are candidate timelines "));
									}

									if (recommP1.isNextActivityJustAfterRecommPointIsInvalid())
									{
										bwNextActInvalid.write(userId + "," + endTimeStamp + "\n");
									}

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
									String actActualDone = nextValidActivityObjectAfterRecommPoint1.getActivityName();

									// String nextActivityForRecommAtStartWithoutDistance = recommP1
									// .getNextActivityNamesWithoutDistanceString();
									// String nextActivityForRecommAtStartWithDistance = recommP1
									// .getNextActivityNamesWithDistanceString();

									String actAtRecommPoint = recommP1.getActivityObjectAtRecomm().getActivityName(); // current
																														// activity

									String rankedRecommWithScoreForThisRT = recommP1
											.getRankedRecommendedActNamesWithRankScores(); // rankedRecommAtStartWithScore
									String rankedRecommWithoutScoreForThisRT = recommP1
											.getRankedRecommendedActNamesWithoutRankScores();

									System.out.println("** Ranked Recommendation=" + rankedRecommWithoutScoreForThisRT
											+ ", while actual was=" + actActualDone);

									// metaBufferWriter.write(userId + "_" + dateToRecomm + "_" + endTimeString + ",");
									metaToWriteForThisUserDate.append(userId).append("_").append(dateToRecomm)
											.append("_").append(endTimeString).append(",");

									// actualBufferWriter.write(actActualDone + ",");
									dataActualToWriteForThisUserDate.append(actActualDone).append(",");

									if (VerbosityConstants.WriteTopNextActivitiesWithoutDistance)
									{
										topNextActsWithoutDistToWriteForThisUserDate
												.append(recommP1.getNextActNamesWithoutDistString()).append(",");
									}

									if (VerbosityConstants.WriteTopNextActivitiesWithDistance)
									{
										topNextActsWithDistToWriteForThisUserDate
												.append(recommP1.getNextActNamesWithDistString()).append(",");
									}
									// topNextActsWithoutDistance.write(nextActivityForRecommAtStartWithoutDistance +
									// ",");
									// topNextActsWithDistance.write(nextActivityForRecommAtStartWithDistance + ",");

									// rankedRecommWithScore.write(rankedRecommAtStartWithScore + ",");
									rankedRecommWithScoreToWriteForThisUserDate.append(rankedRecommWithScoreForThisRT)
											.append(",");

									// rankedRecommWithoutScore.write(rankedRecommAtStartWithoutScore + ",");
									rankedRecommWithoutScoreToWriteForThisUserDate
											.append(rankedRecommWithoutScoreForThisRT).append(",");

									char isCurrentTargetActSame;
									if (actAtRecommPoint.equals(actActualDone))
										isCurrentTargetActSame = 't';
									else
										isCurrentTargetActSame = 'f';
									metaIfCurrentTargetSameToWriteForThisUserDate.append(isCurrentTargetActSame)
											.append(",");

									numOfCandidateTimelinesForThisUserDate
											.append(recommP1.getNumOfCandTimelinesBelowThresh()).append("\n");
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
									// baseLineOccurrence
									// .write(activityNameCountPairsOverAllTrainingDaysWithoutCount + ",");

									if (Constant.DoBaselineOccurrence)
									{
										baseLineOccurrenceToWriteForThisUserDate
												.append(actNamesCountsWithoutCountOverTrain).append(",");
									}
									if (Constant.DoBaselineDuration)
									{
										baseLineDurationToWriteForThisUserDate
												.append(actNamesDurationsWithoutDurationOverTrain).append(",");
									}

									/*
									 * *********************************************************************************
									 * ***
									 */

									String[] splittedRecomm = RegexUtils.patternDoubleUnderScore
											.split(rankedRecommWithoutScoreForThisRT);
									// rankedRecommWithoutScoreForThisRT.split(Pattern.quote("__"));

									sbMaxNumOfDistinctRecommendations
											.append(dateToRecomm + "," + endTimeStamp + "," + weekDay + // UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())+
													"," + (splittedRecomm.length - 1) + ","
													+ recommP1.getNumOfCandTimelinesBelowThresh() + "\n");

									bwRawToWriteForThisUserDate = StringUtils.fCat(bwRawToWriteForThisUserDate,
											userName, ",", dateToRecomm, ",", endTimeString, ",", timeCategory, ",",
											recommP1.getActivityNamesGuidingRecomm/* withTimestamps */(), ",",
											actAtRecommPoint, ",",
											Integer.toString(recommP1.getNumOfValidActsInActsGuidingRecomm()), ",",
											Integer.toString(recommP1.getNumOfActsInActsGuidingRecomm()), ",",
											Integer.toString(recommP1.getNumOfCandTimelinesBelowThresh()), ",", weekDay,
											",", actActualDone, ",", rankedRecommWithScoreForThisRT, ",",
											Integer.toString(recommP1.getNumOfDistinctRecommendations()), ",,,,,,"
											// , recommP1.getRestAndEndSimilaritiesCorrelation() , ","
											// , recommP1.getAvgRestSimilarity() , "," , recommP1.getSDRestSimilarity()
											// , "," , recommP1.getAvgEndSimilarity() , "," ,
											// recommP1.getSDEndSimilarity()
											, ",", Boolean.toString((actAtRecommPoint.equals(actActualDone))), "\n");// ,",",recommP1.getActivitiesGuidingRecomm());

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
								} // end of for loop over all activity objects in test date

								bwRaw.write(bwRawToWriteForThisUserDate.toString());
								metaBufferWriter.write(metaToWriteForThisUserDate.toString());
								actualBufferWriter.write(dataActualToWriteForThisUserDate.toString());

								if (VerbosityConstants.WriteTopNextActivitiesWithoutDistance)
								{
									topNextActsWithoutDistance
											.write(topNextActsWithoutDistToWriteForThisUserDate.toString());
								}

								if (VerbosityConstants.WriteTopNextActivitiesWithDistance)
								{
									topNextActsWithDistance.write(topNextActsWithDistToWriteForThisUserDate.toString());
								}

								rankedRecommWithScoreWriter
										.write(rankedRecommWithScoreToWriteForThisUserDate.toString());
								rankedRecommWithoutScoreWriter
										.write(rankedRecommWithoutScoreToWriteForThisUserDate.toString());

								if (Constant.DoBaselineOccurrence)
								{
									baseLineOccurrence.write(baseLineOccurrenceToWriteForThisUserDate.toString());
								}
								if (Constant.DoBaselineDuration)
								{
									baseLineDuration.write(baseLineDurationToWriteForThisUserDate.toString());
								}

								metaIfCurrentTargetSameWriter
										.write(metaIfCurrentTargetSameToWriteForThisUserDate.toString());
								numOfCandidateTimelinesWriter.write(numOfCandidateTimelinesForThisUserDate.toString());

								System.out.println("/end of for loop over all test dates");
							} // end of for loop over all test dates

							bwCountTimeCategoryOfRecomm.write(userName + "," + numberOfMorningRTs + ","
									+ numberOfAfternoonRTs + "," + numberOfEveningRTs + ","
									+ (numberOfMorningRTs + numberOfAfternoonRTs + numberOfEveningRTs));
							bwCountTimeCategoryOfRecomm.newLine();

							numOfValidRTs.put(userId, numberOfMorningRTs + numberOfAfternoonRTs + numberOfEveningRTs);

							// bwCountTimeCategoryOfRecomm.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings");

							// RecommendationMaster recommendationMaster=new RecommendationMaster(userTimelines,
							// userTrainingTimelines,userTestTimelines,dateAtRecomm,timeAtRecomm,userAtRecomm);
							metaBufferWriter.newLine();
							actualBufferWriter.newLine();
							topNextActsWithoutDistance.newLine();
							topNextActsWithDistance.newLine();
							rankedRecommWithScoreWriter.newLine();
							rankedRecommWithoutScoreWriter.newLine();

							baseLineOccurrence.newLine();
							baseLineDuration.newLine();

							WritingToFile.writeToNewFile(sbMaxNumOfDistinctRecommendations.toString(),
									commonPath + userName + "MaxNumberOfDistinctRecommendation.csv");
							// bwMaxNumOfDistinctRecommendations.close();

							WritingToFile.writeToNewFile(sbRecommTimesWithEditDistances.toString(),
									commonPath + userName + "RecommTimesWithEditDistance.csv");
							// bwRecommTimesWithEditDistances.close();

							// if (matchingUnitIterator == 0)
							{// write it only for one mu since it will remain same over
								// mus and is only for stat purpose
								WritingToFile.writeToNewFile(sbNumOfCandTimelinesBelowThreshold.toString(),
										commonPath + userName + "numberOfCandidateTimelinesBelow" + typeOfThreshold
												+ thresholdValue + ".csv");
							}
							// bwNumOfCandTimelinesBelowThreshold.close();
							System.out.println("//end of for over userID");
						} // end of for over userID
						numActsInEachCandbw.close();

						metaBufferWriter.close();
						actualBufferWriter.close();
						topNextActsWithoutDistance.close();
						topNextActsWithDistance.close();

						rtsInvalidWriter.close();
						rtsWithNoCandsWriter.close();
						rtsRejWithNoDWButMUCandsCands.close();
						rankedRecommWithScoreWriter.close();
						rankedRecommWithoutScoreWriter.close();

						metaIfCurrentTargetSameWriter.close();
						numOfCandidateTimelinesWriter.close();

						baseLineOccurrence.close();
						baseLineDuration.close();

						bwRaw.close();
						bwNumOfWeekendsInTraining.close();
						bwCountTimeCategoryOfRecomm.close();

						bwNextActInvalid.close();

						bwWriteNormalisationOfDistance.close();
						bwNumOfValidAOsAfterRTInDay.close();

						new Evaluation();

						System.out.println("Pruning has Saturated is :" + pruningHasSaturated);
						String msg = "";

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
								+ (recommTestsEndtime - recommTestsStarttime) / 1000 + "seconds");

						writeRepAOs(mapOfRepAOs, mapOfMedianPreSuccDuration, Constant.getCommonPath());

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
			}
		}
		// PopUps.showMessage("ALL TESTS DONE... u can shutdown the server");// +msg);
		System.out.println("**********Exiting Recommendation Tests**********");

	}

	/**
	 * 
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDuration
	 */
	private static void writeRepAOs(LinkedHashMap<Integer, LinkedHashMap<String, ActivityObject>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<String, Pair<Double, Double>>> mapOfMedianPreSuccDuration,
			String commonPath)
	{

		StringBuilder sbTemp = new StringBuilder();

		WritingToFile.appendLineToFileAbsolute("UserID,ActID,RepAO\n", commonPath + "RepAO.txt");

		for (Entry<Integer, LinkedHashMap<String, ActivityObject>> uEntry : mapOfRepAOs.entrySet())
		{
			// sbTemp.append("\nUser:" + uEntry.getKey()).append("Rep AOs:\n");
			uEntry.getValue().entrySet().stream().forEachOrdered(e -> sbTemp
					.append(uEntry.getKey() + "," + e.getKey() + "," + e.getValue().toStringAllGowallaTS() + "\n"));
		}
		WritingToFile.appendLineToFileAbsolute(sbTemp.toString(), commonPath + "RepAO.txt");

		StringBuilder sbTemp2 = new StringBuilder();
		WritingToFile.appendLineToFileAbsolute("UserID,ActID,PreceedingMedian,SuceedingMedian",
				commonPath + "PreSucMedianDuration.txt");
		for (Entry<Integer, LinkedHashMap<String, Pair<Double, Double>>> uEntry : mapOfMedianPreSuccDuration.entrySet())
		{
			// sbTemp2.append("\nUser:" + uEntry.getKey()).append("\nMedian Prec Succeeding durations:\n");
			uEntry.getValue().entrySet().stream().forEachOrdered(e -> sbTemp2.append(uEntry.getKey() + "," + e.getKey()
					+ "," + e.getValue().getFirst() + "," + e.getValue().getSecond() + "\n"));

		}
		WritingToFile.appendLineToFileAbsolute(sbTemp2.toString(), commonPath + "PreSucMedianDuration.txt");

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
			RecommendationMasterI recommP1, String actActualDone, ActivityObject activityAtRecommPoint)
	{
		StringBuilder rtsWithEditDistancesMsg = new StringBuilder();

		for (Map.Entry<String, Pair<String, Double>> entryDistance : recommP1.getDistancesSortedMap().entrySet())
		{
			String candidateTimelineID = entryDistance.getKey();

			Timeline candidateTimeline = recommP1.getCandidateTimeline(candidateTimelineID);
			// recommP1.getCandidateTimeslines() .get(candidateTimelineID);

			int endPointIndexThisCandidate = candidateTimeline.getActivityObjectsInTimeline().size() - 1;
			ActivityObject endPointActivityInCandidate = candidateTimeline.getActivityObjectsInTimeline()
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
	public void setMatchingUnitArray(Enums.LookPastType lookPastType)
	{
		if (lookPastType.equals(Enums.LookPastType.NCount))// "Count"))
		{
			this.matchingUnitArray = Constant.matchingUnitAsPastCount;// matchingUnitAsPastCount; //
																		// PopUps.showMessage(matchingUnitArray.toString());
		}
		else if (lookPastType.equals(Enums.LookPastType.NHours))// "Hrs"))
		{
			this.matchingUnitArray = Constant.matchingUnitHrsArray;// matchingUnitHrsArray; //
																	// PopUps.showMessage(matchingUnitArray.toString());
		}
		else if (lookPastType.equals(Enums.LookPastType.Daywise))// "Hrs"))
		{
			this.matchingUnitArray = new double[] { -9999 };
		}
		else if (lookPastType.equals(Enums.LookPastType.ClosestTime))// "Hrs"))
		{
			this.matchingUnitArray = new double[] { -9999 };
		}
		// else if
		else
		{
			System.err.println(
					"Error: unknown look past type in in setMatchingUnitArray() RecommendationTests():" + lookPastType);
			System.exit(-1);
		}
	}

	/**
	 * Set the array of doubles containing threshold valyues to be used.
	 * 
	 * @param typeOfThreshold
	 */
	public void setThresholdsArray(Enums.TypeOfThreshold typeOfThreshold)
	{
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
	private Pair<LinkedHashMap<String, ActivityObject>, LinkedHashMap<String, Pair<Double, Double>>> buildRepresentativeAOsForUser(
			int userId, Timeline userTrainingTimelines, String[] allPossibleActivityNames, Timeline userTestTimelines)
	{
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeActivityObjects for user " + userId);
		LinkedHashMap<String, ActivityObject> repAOsForThisUser = null;
		LinkedHashMap<String, Pair<Double, Double>> actMedianPreSuccDuration = null;

		long t1 = System.currentTimeMillis();
		try
		{
			if (Constant.getDatabaseName().equals("gowalla1") == false)
			{
				System.err.println("Error: database is  not gowalla1:" + Constant.getDatabaseName());
				System.exit(-1);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// {
			// System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1);
			// }

			else
			{

				LinkedHashMap<String, ArrayList<ActivityObject>> aosForEachActName = new LinkedHashMap<>();

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

				for (ActivityObject ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					String actNameEncountered = ao.getActivityName();
					distinctActNamesEncounteredInTraining.add(actNameEncountered);

					// add this act objec to the correct map entry in map of aos for given act names
					ArrayList<ActivityObject> aosStored = aosForEachActName.get(actNameEncountered);
					if (aosStored == null)
					{
						System.err.println(PopUps.getCurrentStackTracedErrorMsg("Error: encountered act name '"
								+ actNameEncountered + "' is not in the list of all possible act names"));
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

				Pair<LinkedHashMap<String, ActivityObject>, LinkedHashMap<String, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserV2(
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

		return new Pair<LinkedHashMap<String, ActivityObject>, LinkedHashMap<String, Pair<Double, Double>>>(
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
		WritingToFile.appendLineToFileAbsolute(
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

		WritingToFile.appendLineToFileAbsolute("U:" + userId + "," + actNamesInTestButNotInTraining.size() + ","
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
	private Pair<LinkedHashMap<String, ActivityObject>, LinkedHashMap<String, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserV1(
			int userID, LinkedHashMap<String, ArrayList<ActivityObject>> aosForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromPrevForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromNextForEachActName)
	{
		System.out.println("Inside computeRepresentativeActivityObject for userID" + userID);
		LinkedHashMap<String, ActivityObject> repAOs = new LinkedHashMap<String, ActivityObject>();
		LinkedHashMap<String, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		try
		{
			if (!Constant.getDatabaseName().equals("gowalla1"))
			{
				System.err.println(PopUps.getCurrentStackTracedErrorMsg(
						"Error: this method is currently only suitable for gowalla dataset"));
				System.exit(-1);
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.

			for (Entry<String, ArrayList<ActivityObject>> actNameEntry : aosForEachActName.entrySet())
			{
				String actName = actNameEntry.getKey();
				ArrayList<ActivityObject> aos = actNameEntry.getValue();

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
				ActivityObject repAOForThisActNameForThisUser = new ActivityObject((int) Integer.valueOf(actName),
						new ArrayList<Integer>(), actName, "", mediaStartTS, "", "", "", String.valueOf(userID), -1,
						medianCinsCount, -1, -1, -1, -1, -1, actName + "__", -1, -1, new String[] { "" });

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
		return new Pair<LinkedHashMap<String, ActivityObject>, LinkedHashMap<String, Pair<Double, Double>>>(repAOs,
				actMedPreSuccDuration);
	}

	/**
	 * 
	 * @param userID
	 * @param aosForEachActName
	 * @param durationFromPrevForEachActName
	 * @param durationFromNextForEachActName
	 * @return
	 */
	private Pair<LinkedHashMap<String, ActivityObject>, LinkedHashMap<String, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserV2(
			int userID, LinkedHashMap<String, ArrayList<ActivityObject>> aosForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromPrevForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromNextForEachActName)
	{
		System.out.println("Inside computeRepresentativeActivityObject for userID" + userID);
		LinkedHashMap<String, ActivityObject> repAOs = new LinkedHashMap<String, ActivityObject>();
		LinkedHashMap<String, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		try
		{
			if (!Constant.getDatabaseName().equals("gowalla1"))
			{
				System.err.println(PopUps.getCurrentStackTracedErrorMsg(
						"Error: this method is currently only suitable for gowalla dataset"));
				System.exit(-1);
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.
			for (Entry<String, ArrayList<ActivityObject>> actNameEntry : aosForEachActName.entrySet())
			{
				String actName = actNameEntry.getKey();
				ArrayList<ActivityObject> aos = actNameEntry.getValue();

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
				Timestamp dummyMedianStartTS = getMedianSecondsSinceMidnightTimestamp(
						aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID, actName);

				// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				// int medianCinsCount = (int) StatsUtils
				// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
				// .getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				ActivityObject repAOForThisActNameForThisUser = new ActivityObject((int) Integer.valueOf(actName),
						new ArrayList<Integer>(), actName, "", dummyMedianStartTS, "", "", "", String.valueOf(userID),
						-1, -1, -1, -1, -1, -1, -1, actName + "__", -1, -1, new String[] { "" });

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
		return new Pair<LinkedHashMap<String, ActivityObject>, LinkedHashMap<String, Pair<Double, Double>>>(repAOs,
				actMedPreSuccDuration);
	}

	/**
	 * Returns the median seconds since midnight for the given timestamps
	 * 
	 * @param tss
	 * @param actName
	 * @param userID
	 * @return
	 */
	private double getMedianSeconds(List<Timestamp> tss, int userID, String actName)
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
	 * @param actName
	 * @param userID
	 * @return
	 */
	private Timestamp getMedianSecondsSinceMidnightTimestamp(List<Timestamp> tss, int userID, String actName)
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

}
