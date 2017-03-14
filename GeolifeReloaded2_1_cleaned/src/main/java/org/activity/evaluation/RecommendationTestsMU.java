package org.activity.evaluation;

import java.io.BufferedWriter;
import java.io.File;
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
import java.util.regex.Pattern;

import org.activity.constants.Constant;
import org.activity.constants.VerbosityConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.UserDayTimeline;
import org.activity.recomm.RecommendationMasterMU;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.ConnectDatabase;
import org.activity.util.DateTimeUtils;
import org.activity.util.TimelineUtils;

/**
 * Executes the experiments for generating recommendations
 * 
 * @author gunjan
 *
 */
public class RecommendationTestsMU
{
	double percentageInTraining;// = 0.8;
	// String fullCandOrSubCand="fullCand";
	/**
	 * threshold for choosing candidate timelines, those candidate timelines whose distance from the 'activity Objects
	 * guiding recommendations' is higher than the cost of replacing 'percentageDistanceThresh' % of Activity Objects in
	 * the activities guiding recommendation are pruned out from set of candidate timelines
	 */
	String typeOfThresholds[];// = { "Global" };// Global"};//"Percent",
	int globalThresholds[] = { 10000000 };// {50,100,150,200,250,300,350,400,450,500,550,600,650,700,1000};
	int percentThresholds[] = { 100 };// {50,60,70,80,90,100};

	String caseType;// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
	String lookPastType;// = "Count";// "Hrs"

	int userIDs[];// = { 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 };

	TreeMap<Integer, Integer> userIdNumOfRTsMap;

	double matchingUnitHrsArray[];
	double matchingUnitAsPastCount[];

	double matchingUnitArray[];
	String commonPath;

	public boolean pruningHasSaturated;

	int thresholdsArray[];

	/**
	 * ALERT: this is not to be used while thresholding <User,<MRR0, MRR2, .... > >
	 */
	// LinkedHashMap<String, ArrayList<Double>> allUsersMRRForAllMUs;

	public RecommendationTestsMU(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	{
		System.out.println("\n\n **********Entering Recommendation Tests**********");
		// PopUps.showMessage("**********Entering Recommendation Tests**********\n with output path = " +
		// Constant.outputCoreResultsPath);

		long recommTestsStarttime = System.currentTimeMillis();

		this.percentageInTraining = Constant.percentageInTraining;
		this.caseType = Constant.caseType;
		this.lookPastType = Constant.lookPastType;
		this.typeOfThresholds = Constant.typeOfThresholds;
		this.userIDs = Constant.getUserIDs();

		if (userIDs == null || userIDs.length == 0) // if userid is not set in constant class, in case of gowalla
		{
			userIDs = new int[usersTimelines.size()];
			// System.out.println("usersTimelines.size() = " + usersTimelines.size());
			System.out.println("UserIDs not set, hence extracting user ids from usersTimelines keyset");
			int count = 0;
			for (String userS : usersTimelines.keySet())
			{
				userIDs[count++] = Integer.valueOf(userS);
			}
		}

		System.out.println("User ids = " + Arrays.toString(userIDs));
		// allUsersMRRForAllMUs = new LinkedHashMap<String, ArrayList<Double>>();

		// Rts not used in daywise matching owing to unavailability of cand timelines for them
		List<String> blackListedRTs = null;
		if (Constant.BLACKLISTING)
		{
			blackListedRTs = ReadingFromFile.getBlackListedRTs(Constant.getDatabaseName());
		}

		// check if directory is empty to prevent overwriting of results
		// if (UtilityBelt.isDirectoryEmpty(Constant.outputCoreResultsPath) == false)
		// {
		// System.err.println("Warning with exit: results' directory not empty");
		// System.exit(-1);
		// }
		setMatchingUnitArray(lookPastType);

		for (String typeOfThreshold : typeOfThresholds)
		{
			setThresholdsArray(typeOfThreshold);

			for (int thresholdValue : thresholdsArray)
			{
				System.out.println("Executing RecommendationTests for threshold value: " + thresholdValue);
				Constant.setTypeOfTimelineMatching("N-count"); // N-count, N-hours
				ArrayList<String> userNames = new ArrayList<String>();
				LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines = null;
				pruningHasSaturated = true;
				try
				{
					for (int matchingUnitIterator = 0; matchingUnitIterator < matchingUnitArray.length; matchingUnitIterator++)// double
																																// matchingUnitInHoursc)
					{
						long ctmu1 = System.currentTimeMillis();
						userIdNumOfRTsMap = new TreeMap<Integer, Integer>();
						double matchingUnit = matchingUnitArray[matchingUnitIterator];
						Constant.setCurrentMatchingUnit(matchingUnit); // used for sanity checks
						System.out.println("Executing RecommendationTests for matching unit: " + matchingUnit);

						// if (matchingUnitIterator % 8 == 0) // to control the verbosity of console output
						// {
						// Constant.setVerbose(true);
						// Constant.setWriteNumActsmatchingUnit(true);
						// }
						// else
						// {
						// Constant.setVerbose(false);
						// if (matchingUnitIterator % 8 == 0)
						// Constant.setWriteNumActsmatchingUnit(true);
						// else
						// Constant.setWriteNumActsmatchingUnit(false);
						// }
						// commonPath = Constant.outputCoreResultsPath;
						commonPath = Constant.outputCoreResultsPath;// + Constant.DATABASE_NAME + "_" +
																	// LocalDateTime.now().getMonth().toString().substring(0,
																	// 3)
						// + LocalDateTime.now().getDayOfMonth();

						// Creating the directory for that matching unit
						boolean dir = WritingToFile
								.createDirectory(commonPath + "/MatchingUnit" + String.valueOf(matchingUnit));
						if (!dir)
						{
							System.exit(-135);
						}

						commonPath = commonPath + "/" + "MatchingUnit" + matchingUnit + "/";
						Constant.setCommonPath(commonPath);
						System.out.println("Common path=" + Constant.getCommonPath()); // commonPath=Constant.getCommonPath();

						new File(commonPath + "EditSimilarityCalculations.csv").delete();

						PrintStream consoleLogStream = WritingToFile
								.redirectConsoleOutput(commonPath + "consoleLog.txt");

						BufferedWriter metaBufferWriter = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "meta.csv");// new BufferedWriter(metaWriter);
						BufferedWriter actualBufferWriter = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "dataActual.csv");// new
																							// BufferedWriter(actualWriter);
						BufferedWriter top5BufferWriter = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "topNextActivitiesWithoutDistance.csv");
						BufferedWriter topRecommWithDistance = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "topNextActivitiesWithDistance.csv");
						BufferedWriter rtsInvalidWriter = WritingToFile.getBufferedWriterForNewFile(
								commonPath + "recommPointsInvalidBecuzNoValidActivityAfterThis.csv");
						BufferedWriter rtsWithNoCands = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "recommPointsWithNoCandidates.csv");
						BufferedWriter rankedRecommWithScore = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "dataRankedRecommendationWithScores.csv");
						BufferedWriter rankedRecommWithoutScore = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "dataRankedRecommendationWithoutScores.csv");
						/**
						 * Contains list of activity names sorted by frequency of occurrence/duration. Num of unique
						 * sorted lists = number of users, however, each list is repeated so as maintain structural
						 * conformity with dataRankedRecommendationWithoutScores.csv
						 */
						BufferedWriter baseLineOccurrence = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "dataBaseLineOccurrence.csv");
						BufferedWriter baseLineDuration = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "dataBaseLineDuration.csv");
						BufferedWriter bwRaw = WritingToFile.getBufferedWriterForNewFile(commonPath + "Raw.csv");
						BufferedWriter bwNumOfWeekendsInTraining = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "NumberOfWeekendsInTraining.csv");
						BufferedWriter bwNumOfWeekendsInAll = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "NumberOfWeekendsInAll.csv");
						BufferedWriter bwCountTimeCategoryOfRecomm = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "CountTimeCategoryOfRecommPoitns.csv");
						BufferedWriter bwNextActInvalid = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "NextActivityIsInvalid.csv");
						BufferedWriter bwWriteNormalisationOfDistance = WritingToFile
								.getBufferedWriterForNewFile(commonPath + "NormalisationDistances.csv");
						// BufferedWriter bwCountInActivitiesGuidingRecomm= new BufferedWriter(new
						// FileWriter(fileCountInActivitiesGuidingRecomm.getAbsoluteFile(),true));
						// fileCountTimeCategoryOfRecomm
						// BufferedWriter bwNumOfWeekdaysInTraining = new BufferedWriter(new
						// FileWriter(fileNumOfWeekdaysInTraining.getAbsoluteFile(),true));
						// FileWriter singleRecommWriter = new FileWriter(file4.getAbsoluteFile(),true);
						// BufferedWriter bufferWriter4 = new BufferedWriter(singleRecommWriter);

						rtsInvalidWriter.write(
								"User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity\n");
						rtsWithNoCands
								.write("User_ID,Date,Index_of_Activity Object,Start_Timestamp,Week_Day,Time_Category,Current_Activity,TotalNumOfPossibleCands,"
										+ "NumCandsRejectedDueToNoCurrentActivityAtNonLast,NumCandsRejectedDueToNoNextActivity\n");

						bwRaw.write("User,DateOfRecomm,TimeOfRecomm,TimeCategoryOfRecomm,CurrentTimeline,"
								+ "CurrentActivity(ActivityAtRecommPoint),NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline,"
								+ "NumOfCandidateTimelinesBelowThresh," + "WeekDayOfRecomm,Target(ActualActivity),"
								+ "RecommendedActivities," + "NumOfDistinctRecomms,"
								+ "PearsonCorrOfCandSimsAndEndCaseSims," + "AvgRestSimilarity,"
								+ "StdDevRestSimilarity," + "AvgEndSimilarity," + "StdDevEndSimilarity");// LastActivityOnRecommDay");//,ActivitiesOnRecommDayUntiRecomm");
						bwRaw.newLine();

						bwCountTimeCategoryOfRecomm
								.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings,TotalRTs");
						bwCountTimeCategoryOfRecomm.newLine();

						bwNumOfWeekendsInTraining.write("User,NumOfWeekends,NumOfWeekdays");
						bwNumOfWeekendsInTraining.newLine();

						bwNextActInvalid.write("User,Timestamp_of_Recomm");
						bwNextActInvalid.newLine();

						// bwCountInActivitiesGuidingRecomm.write("User,RecommendationTime,TimeCategory,NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline");
						// bwCountInActivitiesGuidingRecomm.newLine();

						BufferedWriter numActsInEachCandbw = WritingToFile.getBufferedWriterForNewFile(
								commonPath + "NumActsmatchingUnit" + String.valueOf(matchingUnit) + ".csv");
						numActsInEachCandbw.write(
								"NumberOfActivityObjectInCandidateTimeline,TimelineID,UserId, DateAtRT, TimeAtRT, ActivitytObjectsInCandidateTimeline");
						numActsInEachCandbw.newLine();

						bwWriteNormalisationOfDistance
								.write("User, DateOfRecomm, TimeOfRecom, EditDistance,NormalisedEditDistance");
						bwWriteNormalisationOfDistance.newLine();

						WritingToFile.writeDistanceScoresSortedMapHeader(); // writes EditDistancePerRtPerCand.csv
						WritingToFile.writeEditSimilarityCalculationsHeader(); // writes the edit similarity
																				// calculations for this recommendation
																				// master

						System.out.println(Constant.getAllGlobalConstants());
						System.out.println(Constant.getCommonPath());

						/** Can be used to select users above 10 RTs **/
						LinkedHashMap<Integer, Integer> numOfValidRTs = new LinkedHashMap<Integer, Integer>();

						// int userCount = 0;
						for (int userId : userIDs) // for(int userId=minTestUser;userId <=maxTestUser;userId++)
						{
							// int numberOfValidRTs = 0;
							// userCount += 1;

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

							BufferedWriter bwMaxNumOfDistinctRecommendations = WritingToFile
									.getBufferedWriterForNewFile(
											commonPath + userName + "MaxNumberOfDistinctRecommendation.csv");
							bwMaxNumOfDistinctRecommendations.write("DateOfRecomm" + ",TimeOfRecomm"
									+ ",Week_Day,MaxNumOfDistictRecommendation,NumOfCandidateTimelines(after applying Threshold)");
							bwMaxNumOfDistinctRecommendations.newLine();

							BufferedWriter bwNumOfCandTimelinesBelowThreshold = WritingToFile
									.getBufferedWriterForNewFile(
											commonPath + userName + "numberOfCandidateTimelinesBelow" + typeOfThreshold
													+ thresholdValue + ".csv");
							bwNumOfCandTimelinesBelowThreshold.write("DateOfRecomm" + ",TimeOfRecomm"
									+ ",Week_Day,ThresholdAsDistance,NumOfCandidateTimelinesBelowThreshold,");
							bwNumOfCandTimelinesBelowThreshold.newLine();

							BufferedWriter bwRecommTimesWithEditDistances = WritingToFile.getBufferedWriterForNewFile(
									commonPath + userName + "RecommTimesWithEditDistance.csv");
							bwRecommTimesWithEditDistances.write("DateOfRecomm" + ",TimeOfRecomm,"
									+ "CandidateTimelineID,TargetActivity,EditDistanceOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,EndIndexOf(Sub)Cand,CandidateTimeline,WeekDayOfRecomm");
							bwRecommTimesWithEditDistances.newLine();

							userAllDatesTimeslines = usersTimelines.get(Integer.toString(userId));// userId);

							if (userAllDatesTimeslines == null)
							{
								System.err.println(
										"userAllDatesTimeslines = " + userAllDatesTimeslines + " user " + userId);
							}

							// //////////////////REMOVING SELECTED TIMELINES FROM
							// DATASET///////////////////////////////////////////////////////
							// userAllDatesTimeslines = TimelineUtilities.cleanUserDayTimelines(userAllDatesTimeslines);
							userAllDatesTimeslines = TimelineUtils.cleanUserDayTimelines(userAllDatesTimeslines,
									commonPath + "InsideRecommTestCleanUserDayTimelines", String.valueOf(userId));
							// ////////////////////////////////////////////////////////////////////////////////
							if (matchingUnitIterator == 0) // write the given day timelines only once
							{
								WritingToFile.writeGivenDayTimelines(userName, userAllDatesTimeslines, "All", true,
										true, true);
							}

							// Splitting the set of timelines into training set and test set.
							List<LinkedHashMap<Date, UserDayTimeline>> trainTestTimelines = splitTestTrainingTimelines(
									userAllDatesTimeslines, percentageInTraining);
							LinkedHashMap<Date, UserDayTimeline> userTrainingTimelines = trainTestTimelines.get(0);
							LinkedHashMap<Date, UserDayTimeline> userTestTimelines = trainTestTimelines.get(1);

							if (userTrainingTimelines.size() == 0)
							{
								System.out.println(
										"Warning: Skipping this user " + userId + " as it has 0 training days");
								WritingToFile.appendLineToFile("User " + userId + ",", "UserWithNoTrainingDay");
								numOfValidRTs.put(userId, 0);
								continue;
							}

							/*
							 * ********** ************************************************
							 * ********************************
							 */
							// if (matchingUnitIterator == 0) // do this only for one matching unit as it does not
							// change per matching unit

							LinkedHashMap<String, LinkedHashMap<String, ?>> mapsForCountDurationBaselines = WritingToFile
									.writeBasicActivityStatsAndGetBaselineMaps(userName, userAllDatesTimeslines,
											userTrainingTimelines, userTestTimelines);
							// note: the maps of maps here contains a map for baselines count and another map for
							// baseline
							// duration: they will be used later to
							// write prediction results for baseline count and baseline duration

							/*
							 * ********** ************************************************
							 * ********************************
							 */

							int numberOfWeekendsInTraining = TimelineUtils
									.getNumberOfWeekendsInGivenDayTimelines(userTrainingTimelines);
							int numberOfWeekdaysInTraining = userTrainingTimelines.size() - numberOfWeekendsInTraining;

							bwNumOfWeekendsInTraining.write(
									userName + "," + numberOfWeekendsInTraining + "," + numberOfWeekdaysInTraining);
							bwNumOfWeekendsInTraining.newLine();

							int numberOfWeekendsInAll = TimelineUtils
									.getNumberOfWeekendsInGivenDayTimelines(userAllDatesTimeslines);
							int numberOfWeekdaysInAll = userAllDatesTimeslines.size() - numberOfWeekendsInAll;

							bwNumOfWeekendsInAll
									.write(userName + "," + numberOfWeekendsInAll + "," + numberOfWeekdaysInAll);
							bwNumOfWeekendsInAll.newLine();
							// ////////////////////////////////////////
							// }
							// ////////////////////////////////////////

							int numberOfMorningRTs = 0, numberOfAfternoonRTs = 0, numberOfEveningRTs = 0;
							// Generating Recommendation Timestamps
							// generate date and times for recommendation
							for (Map.Entry<Date, UserDayTimeline> entry : userTestTimelines.entrySet())
							{
								int date = entry.getKey().getDate();
								int month = entry.getKey().getMonth() + 1;
								int year = entry.getKey().getYear() + 1900;

								String dateToRecomm = date + "/" + month + "/" + year;
								System.out.println("For userid=" + userId + " entry.getKey()=" + entry.getKey()
										+ "  dateToRecomm=" + dateToRecomm);

								String weekDay = DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay());

								UserDayTimeline eachDayTimelineForUser = entry.getValue();
								ArrayList<ActivityObject> activityObjectsInThatDay = eachDayTimelineForUser
										.getActivityObjectsInDay();

								for (int j = 0; j < activityObjectsInThatDay.size() - 1; j++) // will not make
																								// recommendation for
																								// days which have only
																								// one
																								// activity
								{
									ActivityObject activityObjectInThatDay = activityObjectsInThatDay.get(j);
									String activityNameInThatDay = activityObjectInThatDay.getActivityName();

									System.out.println(
											"Iterating over potential recommendation times: current activityAtPotentialRecommTime="
													+ activityNameInThatDay);// (activityObjectsInThatDay.get(j).getActivityName()));

									if (activityNameInThatDay.equals(Constant.INVALID_ACTIVITY1))// (activityObjectsInThatDay.get(j).getActivityName().equals(Constant.INVALID_ACTIVITY1))//
																									// ("Unknown"))
									{
										System.out.println("Skipping because " + Constant.INVALID_ACTIVITY1);
										continue;
									}
									if (activityNameInThatDay.equals(Constant.INVALID_ACTIVITY2))// ("Others"/"Not
																									// Available"))
									{
										System.out.println("Skipping because " + Constant.INVALID_ACTIVITY2);
										continue;
									}

									Timestamp endTimeStamp = activityObjectInThatDay.getEndTimestamp();// getStartTimestamp();

									String timeCategory = DateTimeUtils.getTimeCategoryOfTheDay(endTimeStamp.getHours());

									if (UserDayTimeline.isNoValidActivityAfterItInTheDay(j, eachDayTimelineForUser))
									{ // this will rarely happen because we are already not including the last activity
										// of day as RT (see j's loop). So this
										// will happen only if j is a not last ao in day timeline and has only invalid
										// aos after it.
										System.out.println(
												"Skipping this recommendation point because there are no valid activity Objects after this in the day");
										// if (j == activityObjectsInThatDay.size() - 1) // this should never happen,
										// see j's loop
										// {System.out.println("This was the last activity of the day:" + dateToRecomm +
										// " for user:" + userId); }
										rtsInvalidWriter
												.write(userId + "," + dateToRecomm + "," + j + "," + endTimeStamp + ","
														+ weekDay + "," + timeCategory + activityNameInThatDay);
										rtsInvalidWriter.newLine();
										continue;
									}
									// Recommendation is made at the end time of the activity object in consideration
									// (activityObjectInThatDay)
									String endTimeString = endTimeStamp.getHours() + ":" + endTimeStamp.getMinutes()
											+ ":" + endTimeStamp.getSeconds();

									ActivityObject nextValidActivityObjectAfterRecommPoint1 = eachDayTimelineForUser
											.getNextValidActivityAfterActivityAtThisTime(
													new Timestamp(year - 1900, month - 1, date, endTimeStamp.getHours(),
															endTimeStamp.getMinutes(), endTimeStamp.getSeconds(), 0));

									if (nextValidActivityObjectAfterRecommPoint1 == null)
									{
										System.out.println("Error in Sanity Check RT407: User id" + userId
												+ " Next activity Object after " + endTimeStamp + " is null");
										System.out.println(
												"nextValidActivityAfteractivityRecommPoint1 is null, if it was such, we should have not reached this point of execution");
										// because isNoValidActivityAfterItInTheDay already checked if there exists a
										// next valid activity
										PopUps.showError(
												"Error in Sanity Check RT407: nextValidActivityAfteractivityRecommPoint1 is null, if it was such, we should have not reached this point of execution");
									}

									System.out.println(
											"User id" + userId + " Next activity Object after " + endTimeStamp + " ="
													+ nextValidActivityObjectAfterRecommPoint1.getActivityName());

									System.out.println("Recommendation point at this Activity Object are:- End time: "
											+ endTimeString);// +" ,and Middle: "+middleTimeString);

									// ///////////
									// Now we have those recommendation times which are valid for making recommendations
									// ///////////////////Start//////////////////////////////////
									// String
									// actRecommAtStart=recommP1.getSingleNextRecommendedActivity();RecommendationMaster
									// recommP1=new
									// RecommendationMaster(/*userTimelines,*/userTrainingTimelines,userTestTimelines,dateToRecomm,startTimeString,
									// userId);

									// create interface for recommendermaster
									// /IMPORTANT
									// $$RecommendationMasterMU recommP1 = new
									// RecommendationMasterMU(userTrainingTimelines, userTestTimelines, dateToRecomm,
									// endTimeString, userId,
									// $$ thresholdValue, typeOfThreshold, matchingUnit, caseType, this.lookPastType);//
									// ,fullCandOrSubCand);

									RecommendationMasterMU recommP1 = new RecommendationMasterMU(userTrainingTimelines,
											userTestTimelines, dateToRecomm, endTimeString, userId, thresholdValue,
											typeOfThreshold, matchingUnit, caseType, this.lookPastType, false);// LAST
																												// PARAM
																												// TRUE
																												// IS
																												// DUMMY
																												// FOR
																												// CALLING
																												// PERFORMANCE
																												// CONSTRUCTOR,
																												// REMOVE
																												// IT
																												// FOR
																												// EXPERIMENTS
									// ,fullCandOrSubCand);

									LinkedHashMap<Integer, TimelineWithNext> candidateTimelines = recommP1
											.getCandidateTimeslines();

									System.out.println(
											"Back to RecommendationTests: received " + candidateTimelines.size()
													+ " candidate timelines for matching unit " + matchingUnit);
									System.out.println("\tIterating over candidate timelines:");

									if (VerbosityConstants.WriteNumActsPerRTPerCand)
									{
										StringBuilder tmpWriter = new StringBuilder();
										for (Map.Entry<Integer, TimelineWithNext> entryAjooba : candidateTimelines
												.entrySet())
										{
											Timeline candtt1 = entryAjooba.getValue(); // ArrayList<ActivityObject>
																						// aa1=candtt1.getActivityObjectsInTimeline();
											int sizez1 = candtt1.countNumberOfValidActivities() - 1; // excluding the
																										// current
																										// activity at
																										// the end of
																										// the
																										// candidate
																										// timeline
											// System.out.println("Number of activity Objects in this timeline (except
											// the end current activity) is: "+sizez1);
											// numActsInEachCandbw.write
											tmpWriter.append(String.valueOf(sizez1) + ","
													+ candtt1.getTimelineID().toString() + "," + userId + ","
													+ dateToRecomm + "," + endTimeString + ","
													+ candtt1.getActivityObjectNamesWithTimestampsInSequence() + "\n");
											// numActsInEachCandbw.newLine();
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
										rtsWithNoCands.write(userId + "," + dateToRecomm + "," + j + "," + endTimeStamp
												+ "," + weekDay + "," + timeCategory + ","
												+ recommP1.getActivityObjectAtRecomm().getActivityName() + ",");// $$+recommP1.totalNumberOfProbableCands+","+recommP1.numCandsRejectedDueToNoCurrentActivityAtNonLast+","+recommP1.numCandsRejectedDueToNoNextActivity);
										rtsWithNoCands.newLine();
										System.out.println(
												"Cannot make recommendation at this point as there are no candidate timelines");

										bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimeStamp + ","
												+ weekDay + "," + thresholdAsDistance + "," + 0 + "\n");
										// bwNumOfCandTimelinesBelowThreshold.newLine();

										continue;
									}

									// ////////////////////////////Start of New addition for blacklisted RTs
									if (Constant.BLACKLISTING)
									{
										if (blackListedRTs.contains(new String(userId + " " + endTimeStamp)))
										{
											System.out.println("Alert: blacklisted RT: " + userId + " " + endTimeStamp
													+ " will not be used and will be logged in rtsWithNoCands");
											rtsWithNoCands.write(userId + "," + dateToRecomm + "," + j + ","
													+ endTimeStamp + "," + weekDay + "," + timeCategory + ","
													+ recommP1.getActivityObjectAtRecomm().getActivityName() + ",");// $$+recommP1.totalNumberOfProbableCands+","+recommP1.numCandsRejectedDueToNoCurrentActivityAtNonLast+","+recommP1.numCandsRejectedDueToNoNextActivity);
											rtsWithNoCands.newLine();

											bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimeStamp
													+ "," + weekDay + "," + thresholdAsDistance + "," + 0);
											bwNumOfCandTimelinesBelowThreshold.newLine();

											continue;

										}
									}
									// ////////////////////////////End of New addition for blacklisted RTs

									if (recommP1.hasCandidateTimelinesBelowThreshold() == false)
									{
										bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimeStamp + ","
												+ weekDay + "," + thresholdAsDistance + "," + 0);
										bwNumOfCandTimelinesBelowThreshold.newLine();

										System.out.println(
												"Cannot make recommendation at this point as there are no candidate timelines BELOW THRESHOLD");
										continue;
									}

									bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimeStamp + ","
											+ weekDay + "," + thresholdAsDistance + ","
											+ recommP1.getNumberOfCandidateTimelinesBelowThreshold());
									bwNumOfCandTimelinesBelowThreshold.newLine();

									if (recommP1.getTopNextActivityObjects() == null)
									{
										System.err.println(
												"Error in Sanity Check RT500:recommP1.getTopNextActivityObjects()==null, but there are candidate timelines ");
									}

									if (recommP1.isNextActivityJustAfterRecommPointIsInvalid())
									{
										bwNextActInvalid.write(userId + "," + endTimeStamp);
										bwNextActInvalid.newLine();
									}

									if (timeCategory.equalsIgnoreCase("Morning"))
										numberOfMorningRTs++;
									else if (timeCategory.equalsIgnoreCase("Afternoon"))
										numberOfAfternoonRTs++;
									else if (timeCategory.equalsIgnoreCase("Evening")) numberOfEveningRTs++;

									String actActualDone = nextValidActivityObjectAfterRecommPoint1.getActivityName(); // target
																														// activity
																														// for
																														// recommendation

									String topNextActivityForRecommAtStartWithoutDistance = recommP1
											.getTopNextActivityNamesWithoutDistanceString();
									String topNextActivityForRecommAtStartWithDistance = recommP1
											.getTopNextActivityNamesWithDistanceString();

									String actAtRecommPoint = recommP1.getActivityObjectAtRecomm().getActivityName(); // current
																														// activity

									String rankedRecommAtStartWithScore = recommP1
											.getRankedRecommendedActivityNamesWithRankScores(); // rankedRecommAtStartWithScore
									String rankedRecommAtStartWithoutScore = recommP1
											.getRankedRecommendedActivityNamesWithoutRankScores();

									System.out.println("** Ranked Recommended at Start="
											+ rankedRecommAtStartWithoutScore + ", while actual was=" + actActualDone);

									metaBufferWriter.write(userId + "_" + dateToRecomm + "_" + endTimeString + ",");
									actualBufferWriter.write(actActualDone + ",");

									top5BufferWriter.write(topNextActivityForRecommAtStartWithoutDistance + ",");
									topRecommWithDistance.write(topNextActivityForRecommAtStartWithDistance + ",");

									rankedRecommWithScore.write(rankedRecommAtStartWithScore + ",");
									rankedRecommWithoutScore.write(rankedRecommAtStartWithoutScore + ",");

									/*
									 * *********************************************************************************
									 * ***
									 */
									LinkedHashMap<String, Long> activityNameCountPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) mapsForCountDurationBaselines
											.get("activityNameCountPairsOverAllTrainingDays");
									ComparatorUtils.assertNotNull(activityNameCountPairsOverAllTrainingDays);
									String activityNameCountPairsOverAllTrainingDaysWithoutCount = getActivityNameCountPairsOverAllTrainingDaysWithoutCount(
											activityNameCountPairsOverAllTrainingDays);
									baseLineOccurrence
											.write(activityNameCountPairsOverAllTrainingDaysWithoutCount + ",");
									/*
									 * *********************************************************************************
									 * ***
									 */
									LinkedHashMap<String, Long> activityNameDurationPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) mapsForCountDurationBaselines
											.get("activityNameDurationPairsOverAllTrainingDays");
									ComparatorUtils.assertNotNull(activityNameDurationPairsOverAllTrainingDays);
									String activityNameDurationPairsOverAllTrainingDaysWithoutDuration = getActivityNameDurationPairsOverAllTrainingDaysWithoutDuration(
											activityNameDurationPairsOverAllTrainingDays);
									baseLineDuration
											.write(activityNameDurationPairsOverAllTrainingDaysWithoutDuration + ",");
									/*
									 * *********************************************************************************
									 * ***
									 */

									String[] splittedRecomm = rankedRecommAtStartWithoutScore
											.split(Pattern.quote("__"));
									bwMaxNumOfDistinctRecommendations
											.write(dateToRecomm + "," + endTimeStamp + "," + weekDay + // UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())+
													"," + (splittedRecomm.length - 1) + ","
													+ recommP1.getNumberOfCandidateTimelinesBelowThreshold());
									bwMaxNumOfDistinctRecommendations.newLine();

									bwRaw.write(userName + "," + dateToRecomm + "," + endTimeStamp.getHours() + ":"
											+ endTimeStamp.getMinutes() + ":" + endTimeStamp.getSeconds() + ","
											+ timeCategory + ","
											+ recommP1.getActivityNamesGuidingRecommwithTimestamps() + ","
											+ actAtRecommPoint + ","
											+ recommP1.getNumberOfValidActivitiesInActivitesGuidingRecommendation()
											+ "," + recommP1.getNumberOfActivitiesInActivitesGuidingRecommendation()
											+ "," + recommP1.getNumberOfCandidateTimelinesBelowThreshold() + ","
											+ weekDay + ","// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
											+ actActualDone + "," + rankedRecommAtStartWithScore + ","
											+ recommP1.getNumberOfDistinctRecommendations() + ","
											+ recommP1.getRestAndEndSimilaritiesCorrelation() + ","
											+ recommP1.getAvgRestSimilarity() + "," + recommP1.getSDRestSimilarity()
											+ "," + recommP1.getAvgEndSimilarity() + ","
											+ recommP1.getSDEndSimilarity());// +","+recommP1.getActivitiesGuidingRecomm());
									bwRaw.newLine();

									ActivityObject activityAtRecommPoint = recommP1.getActivityObjectAtRecomm();

									LinkedHashMap<Integer, Pair<String, Double>> editDistancesSortedMapFullCand = recommP1
											.getEditDistancesSortedMapFullCand();

									if (VerbosityConstants.WriteRecommendationTimesWithEditDistance)
									{
										StringBuilder rtsWithEditDistancesMsg = new StringBuilder();

										for (Map.Entry<Integer, Pair<String, Double>> entryDistance : editDistancesSortedMapFullCand
												.entrySet())
										{
											Integer candidateTimelineID = entryDistance.getKey();

											TimelineWithNext candidateTimeline = recommP1.getCandidateTimeslines()
													.get(candidateTimelineID);

											int endPointIndexThisCandidate = candidateTimeline
													.getActivityObjectsInTimeline().size() - 1;
											ActivityObject endPointActivityInCandidate = candidateTimeline
													.getActivityObjectsInTimeline().get(endPointIndexThisCandidate);

											// difference in start time of end point activity of candidate and start
											// time of current activity
											long diffStartTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint
													.getStartTimestamp().getTime()
													- endPointActivityInCandidate.getStartTimestamp().getTime()) / 1000;
											// difference in end time of end point activity of candidate and end time of
											// current activity
											long diffEndTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint
													.getEndTimestamp().getTime()
													- endPointActivityInCandidate.getEndTimestamp().getTime()) / 1000;

											// ("DateOfRecomm"+",TimeOfRecomm,"+"TargetActivity,EditDistanceOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,CandidateTimeline,WeekDayOfRecomm");
											// bwRecommTimesWithEditDistances.write
											rtsWithEditDistancesMsg.append(dateToRecomm + "," + endTimeStamp.getHours()
													+ ":" + endTimeStamp.getMinutes() + ":" + endTimeStamp.getSeconds()
													+ "," + candidateTimelineID + "," + actActualDone + ","
													+ entryDistance.getValue().getSecond() + ","
													// +dateOfCand+","
													+ diffStartTimeForEndPointsCand_n_GuidingInSecs + ","
													+ diffEndTimeForEndPointsCand_n_GuidingInSecs + ","
													+ endPointIndexThisCandidate + ","
													+ candidateTimeline.getActivityObjectNamesInSequence() + ","
													+ weekDay + "\n");
											// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
											// +"," +UtilityBelt.getWeekDayFromWeekDayInt(entryScore.getKey().getDay())
											// bwRecommTimesWithEditDistances.newLine();
										}
										bwRecommTimesWithEditDistances.write(rtsWithEditDistancesMsg + "\n");
									}
									System.out.println("// end of for loop over all activity objects in test date");
								} // end of for loop over all activity objects in test date
								System.out.println("/end of for loop over all test dates");
							} // end of for loop over all test dates

							bwCountTimeCategoryOfRecomm.write(userName + "," + numberOfMorningRTs + ","
									+ numberOfAfternoonRTs + "," + numberOfEveningRTs + ","
									+ (numberOfMorningRTs + numberOfAfternoonRTs + numberOfEveningRTs));
							numOfValidRTs.put(userId, numberOfMorningRTs + numberOfAfternoonRTs + numberOfEveningRTs);

							bwCountTimeCategoryOfRecomm.newLine();
							// bwCountTimeCategoryOfRecomm.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings");

							// RecommendationMaster recommendationMaster=new RecommendationMaster(userTimelines,
							// userTrainingTimelines,userTestTimelines,dateAtRecomm,timeAtRecomm,userAtRecomm);
							metaBufferWriter.newLine();
							actualBufferWriter.newLine();
							top5BufferWriter.newLine();
							topRecommWithDistance.newLine();
							rankedRecommWithScore.newLine();
							rankedRecommWithoutScore.newLine();

							baseLineOccurrence.newLine();
							baseLineDuration.newLine();

							bwMaxNumOfDistinctRecommendations.close();
							bwRecommTimesWithEditDistances.close();
							bwNumOfCandTimelinesBelowThreshold.close();
							System.out.println("//end of for over userID");
						} // end of for over userID
						numActsInEachCandbw.close();

						metaBufferWriter.close();
						actualBufferWriter.close();
						top5BufferWriter.close();
						topRecommWithDistance.close();

						rtsInvalidWriter.close();
						rtsWithNoCands.close();

						rankedRecommWithScore.close();
						rankedRecommWithoutScore.close();

						baseLineOccurrence.close();
						baseLineDuration.close();

						bwRaw.close();
						bwNumOfWeekendsInTraining.close();
						bwCountTimeCategoryOfRecomm.close();

						bwNextActInvalid.close();

						bwWriteNormalisationOfDistance.close();

						new Evaluation();

						System.out.println("Pruning has Saturated is :" + pruningHasSaturated);
						String msg = "";

						if (pruningHasSaturated)
						{
							System.out.println("Pruning has Saturated");
							// PopUps.showMessage("Pruning has Saturated");
						}

						System.out.println("Total Number of Timelines created for matching unit (" + matchingUnit
								+ "hrs) =" + Timeline.getCountTimelinesCreatedUntilNow());
						System.out.println("ALL TESTS DONE for this matching unit");

						long recommTestsEndtime = System.currentTimeMillis();

						System.out.println("Time taken for executing Recommendation Tests for this matching unit ="
								+ (recommTestsEndtime - recommTestsStarttime) / 1000 + "seconds");

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
	 * Splits the given timelines into training and test list of timelines.
	 * 
	 * @param userAllDatesTimeslines
	 * @param percentageInTraining
	 * @return
	 */
	public static List<LinkedHashMap<Date, UserDayTimeline>> splitTestTrainingTimelines(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines, double percentageInTraining)
	{
		ArrayList<LinkedHashMap<Date, UserDayTimeline>> trainTestTimelines = new ArrayList<LinkedHashMap<Date, UserDayTimeline>>();

		int numberOfValidDays = 0;

		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false)
			{ // if the day timelines contains no valid activity, then don't consider it for training or test
				System.err.println(
						"Error in splitTestTrainingTimelines: 45: userAllDatesTimeslines contains a day timeline with no valid activity, but we already tried to remove it");
				continue;
			}
			numberOfValidDays++;
		}
		// int numberOfDays = userAllDatesTimeslines.size();
		int numberOfDaysForTraining = (int) Math.round(numberOfValidDays * percentageInTraining);// floor

		int numberOfDaysForTest = numberOfValidDays - numberOfDaysForTraining;

		if (numberOfDaysForTest < 1)
		{
			numberOfDaysForTest = 1;
			numberOfDaysForTraining = numberOfValidDays - numberOfDaysForTest;
		}

		LinkedHashMap<Date, UserDayTimeline> userTrainingTimelines = new LinkedHashMap<Date, UserDayTimeline>();
		LinkedHashMap<Date, UserDayTimeline> userTestTimelines = new LinkedHashMap<Date, UserDayTimeline>();

		int count = 1;
		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false) // not essential anymore
			{ // if the day timelines contains no valid activity, then don't consider it for training or test
				continue;
			}
			if (count <= numberOfDaysForTraining)
			{
				userTrainingTimelines.put(entry.getKey(), entry.getValue());
				count++;
			}
			else
			{
				userTestTimelines.put(entry.getKey(), entry.getValue());
				count++;
			}
		}

		trainTestTimelines.add(userTrainingTimelines);
		trainTestTimelines.add(userTestTimelines);

		System.out.println("Number of Training days = " + trainTestTimelines.get(0).size());
		System.out.println("Number of Test days = " + trainTestTimelines.get(1).size());

		if (trainTestTimelines.size() > 2)
		{
			System.err.println(
					"Error in splitTestTrainingTimelines: there are more than two (train+test) timelines in returned result, there are "
							+ trainTestTimelines.size() + " timelines.");
			System.exit(-43);
		}

		return trainTestTimelines;
	}

	public String getActivityNameCountPairsOverAllTrainingDaysWithCount(
			LinkedHashMap<String, Long> nameCountPairsSorted)
	{
		String result = "";

		for (Map.Entry<String, Long> entry : nameCountPairsSorted.entrySet())
		{
			result += "__" + entry.getKey() + ":" + entry.getValue();
		}
		return result;
	}

	public String getActivityNameCountPairsOverAllTrainingDaysWithoutCount(
			LinkedHashMap<String, Long> nameCountPairsSorted)
	{
		String result = "";

		for (Map.Entry<String, Long> entry : nameCountPairsSorted.entrySet())
		{
			result += "__" + entry.getKey();
		}
		return result;
	}

	public String getActivityNameDurationPairsOverAllTrainingDaysWithDuration(
			LinkedHashMap<String, Long> nameDurationPairsSorted)
	{
		String result = "";

		for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
		{
			result += "__" + entry.getKey() + ":" + entry.getValue();
		}
		return result;
	}

	public String getActivityNameDurationPairsOverAllTrainingDaysWithoutDuration(
			LinkedHashMap<String, Long> nameDurationPairsSorted)
	{
		String result = "";

		for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
		{
			result += "__" + entry.getKey();
		}
		return result;
	}

	/**
	 * Sets the matching unit array to be used depending on the lookPastType argument and matching unit values in Class
	 * Constant.
	 * 
	 * @param lookPastType
	 */
	public void setMatchingUnitArray(String lookPastType)
	{
		if (lookPastType.equals("Count"))
		{
			this.matchingUnitArray = Constant.matchingUnitAsPastCount;// matchingUnitAsPastCount; //
																		// PopUps.showMessage(matchingUnitArray.toString());
		}
		else if (lookPastType.equals("Hrs"))
		{
			this.matchingUnitArray = Constant.matchingUnitHrsArray;// matchingUnitHrsArray; //
																	// PopUps.showMessage(matchingUnitArray.toString());
		}
		else
		{
			System.err.println("Error: unknown look past type in in setMatchingUnitArray() RecommendationTests()");
			System.exit(-1);
		}
	}

	/**
	 * Set the array of doubles containing threshold valyues to be used.
	 * 
	 * @param typeOfThreshold
	 */
	public void setThresholdsArray(String typeOfThreshold)
	{
		switch (typeOfThreshold)
		{
		case "Percent":
			this.thresholdsArray = percentThresholds;
			break;
		case "Global":
			this.thresholdsArray = globalThresholds;
			break;
		case "None":
			this.thresholdsArray = new int[] { 10000000 };
			break;
		default:
			System.err.println("Error: Unrecognised threshold type in setThresholdsArray()");
		}
	}
}
