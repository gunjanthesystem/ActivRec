// a full curtain over the thing just to prevent annoying compilation error
// package org.activity.evaluation;
//
// import java.io.BufferedWriter;
// import java.io.File;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.io.PrintStream;
// import java.sql.Date;
// import java.sql.Timestamp;
// import java.util.ArrayList;
// import java.util.LinkedHashMap;
// import java.util.Map;
// import java.util.regex.Pattern;
//
// import org.activity.io.WritingToFile;
// import org.activity.objects.ActivityObject;
// import org.activity.objects.Triple;
// import org.activity.objects.UserDayTimeline;
// import org.activity.recomm.RecommendationMasterDayWise2FasterJan2016;
// import org.activity.ui.PopUps;
// import org.activity.util.ConnectDatabase;
// import org.activity.util.Constant;
// import org.activity.util.DateTimeUtils;
// import org.activity.util.TimelineUtils;
// import org.activity.util.UtilityBelt;
//
/// **
// * Used for experiments for iiWAS , editing now
// *
// * @author gunjan
// *
// */
// public final class RecommendationTestsDayWise2Faster
// {
// public double percentageInTraining;// = 0.8;
// // public String outputPath ="/run/media/gunjan/OS/Users/gunjan/Documents/LifeLog App/OUTPUT/";
// public String commonPath;// =Constant.commonPath;//"/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/";
//
// // public String typeOfThreshold = "Global";// "Percent"/"Global"
// // public final double globalDistanceThresh=1000;
// /*
// * public final double percentDistanceThresh=100; /*threshold for choosing candidate timelines, those candidate timelines whose distance from the 'activity events guiding
// * recommendations' is higher than the cost of replacing 'percentageDistanceThresh' % of Activity Events in the activities guiding recommendation are pruned out from set of
// * candidate timelines
// */
// String typeOfThresholds[]; // = { "Global" };// Global"};//"Percent",
// public final int globalThresholds[] = { 10000000 };// 50,100,150,200,250,300,350,400,450,500,550,600,650,700,1000
// public final int percentThresholds[] = { 100 };// {50,60,70,80,90,100};
//
// String caseType;// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
// String lookPastType;// = "Count";// "Hrs"
//
// public final int minUser = 0;
// public final int maxUser = 4;// CHANGE TO 4
//
// /*
// * int userIDs[]={10,20,21,52,53,56,58,59,60,62,64,65,67,68,69,73,75,76,78, 80,81,82,84,85,86,87,88,89,91,92,96,97,98,100,101,102,104,105,106,
// * 107,108,110,111,112,114,115,116,117,118,124,125,126,128,129,136,138, 139,141,144,147,153,154,161,163,167,170,174,175,179};
// */
// // /**
// // * Most active users
// // */
// int userIDs[];// = { 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 };
//
// public boolean pruningHasSaturated;
//
// int thresholdsArray[];
//
// public RecommendationTestsDayWise2Faster(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines)// /, String userAtRecomm)
// {
// System.out.println("inside RecommendationTestsDayWise2Faster");
// this.caseType = Constant.caseType;
// this.percentageInTraining = Constant.percentageInTraining;
// this.typeOfThresholds = Constant.typeOfThresholds;
// this.userIDs = Constant.getUserIDs();
//
// for (String typeOfThreshold : typeOfThresholds)
// {
// setThresholdsArray(typeOfThreshold);
//
// for (int thresholdValue : thresholdsArray)
// {
// System.out.println("Executing RecommendationTests for threshold value: " + thresholdValue);
// ArrayList<String> userNames = new ArrayList<String>();
// LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines = null;
// pruningHasSaturated = true;
//
// // Constant.setCommonPath("/run/media/gunjan/HOME/gunjan/DCU Data Works Oct Space/Oct 29 exp HJ Distance/");
// // $$Constant.setCommonPath("/run/media/gunjan/HOME/gunjan/Geolife Data Works/resultsJan14_2/");
// // $$commonPath = Constant.getCommonPath();
// commonPath = Constant.outputCoreResultsPath;// + Constant.DATABASE_NAME + "_" + LocalDateTime.now().getMonth().toString().substring(0, 3)
//
// try
// {
// new File(commonPath + "EditSimilarityCalculations.csv").delete();
//
// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(commonPath + "consoleLog.txt");
// BufferedWriter metaBufferWriter = WritingToFile.getBufferedWriterForNewFile(commonPath + "meta.csv");
// BufferedWriter actualBufferWriter = WritingToFile.getBufferedWriterForNewFile(commonPath + "dataActual.csv");// new
// // BufferedWriter(actualWriter);
//
// BufferedWriter top5BufferWriter =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "topNextActivitiesWithoutDistance.csv");
//
// BufferedWriter topRecommWithDistance =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "topNextActivitiesWithDistance.csv");
//
// BufferedWriter rtsInvalidWriter =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "recommPointsInvalidBecuzNoValidActivityAfterThis.csv");
// BufferedWriter rtsWithNoCands =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "recommPointsWithNoCandidates.csv");
// BufferedWriter rankedRecommWithScore =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "dataRankedRecommendationWithScores.csv");
// BufferedWriter rankedRecommWithoutScore =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "dataRankedRecommendationWithoutScores.csv");
//
// BufferedWriter baseLineOccurrence =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "dataBaseLineOccurrence.csv");
// BufferedWriter baseLineDuration = WritingToFile.getBufferedWriterForNewFile(commonPath + "dataBaseLineDuration.csv");
// BufferedWriter bwRaw = WritingToFile.getBufferedWriterForNewFile(commonPath + "Raw.csv");
// BufferedWriter bwNumOfWeekendsInTraining =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "NumberOfWeekendsInTraining.csv");
// BufferedWriter bwNumOfWeekendsInTest =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "NumberOfWeekendsInTest.csv");
// BufferedWriter bwNumOfWeekendsInAll =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "NumberOfWeekendsInAll.csv");
//
// BufferedWriter bwCountTimeCategoryOfRecomm =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "CountTimeCategoryOfRecommPoitns.csv");
// BufferedWriter bwNextActInvalid = WritingToFile.getBufferedWriterForNewFile(commonPath + "NextActivityIsInvalid.csv");
// BufferedWriter bwWriteNormalisationOfDistance =
// WritingToFile.getBufferedWriterForNewFile(commonPath + "NormalisationDistances.csv"); // TODO
// // NEW
//
// rtsInvalidWriter.write("User_ID,Date,Index_of_Activity Event,Start_Timestamp,Week_Day,Time_Category\n");
// rtsWithNoCands.write(
// "User_ID,Date,Index_of_Activity
// Event,Start_Timestamp,Week_Day,Time_Category,Current_Activity,TotalNumOfPossibleCands,NumCandsRejectedDueToNoCurrentActivityAtNonLast,NumCandsRejectedDueToNoNextActivity\n");
//
// bwRaw.write(
// "User,DateOfRecomm,TimeOfRecomm,TimeCategoryOfRecomm,NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline,WeekDayOfRecomm,Target(ActualActivity),RecommendedActivity,LastActivityOnRecommDay,ActivitiesOnRecommDayUntiRecomm");
// bwRaw.newLine();
//
// bwCountTimeCategoryOfRecomm.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings");
// bwCountTimeCategoryOfRecomm.newLine();
//
// bwNumOfWeekendsInTraining.write("User,NumOfWeekends,NumOfWeekdays");
// bwNumOfWeekendsInTraining.newLine();
//
// bwNumOfWeekendsInTest.write("User,NumOfWeekends,NumOfWeekdays");
// bwNumOfWeekendsInTest.newLine();
//
// bwNumOfWeekendsInAll.write("User,NumOfWeekends,NumOfWeekdays");
// bwNumOfWeekendsInAll.newLine();
//
// bwNextActInvalid.write("User,Timestamp_of_Recomm");
// bwNextActInvalid.newLine();
//
// WritingToFile.writeDistanceScoresSortedMapHeader();
// // $$WritingToFile.writeEditDistancesOfAllEndPointsHeader();
// for (int userId : userIDs)// int userId=minUser;userId <=maxUser;userId++) //0 to 5
// // for(int userId=3;userId <4;userId++)
// {
//
// System.out.println("\nUser id=" + userId);
// String userName = ConnectDatabase.getUserNameFromDatabase(userId);
//
// File maxNumberOfDistinctRecommendations = new File(commonPath + userName + "MaxNumberOfDistinctRecommendation.csv");
// maxNumberOfDistinctRecommendations.delete();
// maxNumberOfDistinctRecommendations.createNewFile();
// BufferedWriter bwMaxNumOfDistinctRecommendations =
// new BufferedWriter(new FileWriter(maxNumberOfDistinctRecommendations.getAbsoluteFile(), true));
// bwMaxNumOfDistinctRecommendations.write("DateOfRecomm" + ",TimeOfRecomm"
// + ",Week_Day,MaxNumOfDistictRecommendation,NumOfCandidateTimelines(after applying Threshold)");
// bwMaxNumOfDistinctRecommendations.newLine();
//
// File numOfCandTimelinesBelowThreshold = new File(
// commonPath + userName + "numberOfCandidateTimelinesBelow" + typeOfThreshold + thresholdValue + ".csv");
//
// numOfCandTimelinesBelowThreshold.delete();
// numOfCandTimelinesBelowThreshold.createNewFile();
// BufferedWriter bwNumOfCandTimelinesBelowThreshold =
// new BufferedWriter(new FileWriter(numOfCandTimelinesBelowThreshold.getAbsoluteFile(), true));
// bwNumOfCandTimelinesBelowThreshold.write(
// "DateOfRecomm" + ",TimeOfRecomm" + ",Week_Day,ThresholdAsDistance,NumOfCandidateTimelinesBelowThreshold,");
// bwNumOfCandTimelinesBelowThreshold.newLine();
//
// File recommTimesWithEditDistances = new File(commonPath + userName + "RecommTimesWithEditDistance.csv");
// recommTimesWithEditDistances.delete();
// recommTimesWithEditDistances.createNewFile();
// BufferedWriter bwRecommTimesWithEditDistances =
// new BufferedWriter(new FileWriter(recommTimesWithEditDistances.getAbsoluteFile(), true));
// bwRecommTimesWithEditDistances.write("DateOfRecomm" + ",TimeOfRecomm,"
// + "TargetActivity,EditDistanceOfCandidateTimeline,DateOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,CandidateTimeline,WeekDayOfRecomm,WeekDayOfCandidate");
// bwRecommTimesWithEditDistances.newLine();
//
// userAllDatesTimeslines = userTimelines.get(Integer.toString(userId));// userId);
//
// if (userAllDatesTimeslines == null)
// continue;
// // //////////////////REMOVING SELECTED TIMELINES FROM DATASET///////////////////////////////////////////////////////
// userAllDatesTimeslines = TimelineUtils.removeDayTimelinesWithNoValidAct(userAllDatesTimeslines);
// userAllDatesTimeslines = TimelineUtils.removeDayTimelinesWithOneOrLessDistinctValidAct(userAllDatesTimeslines);
//
// userAllDatesTimeslines = TimelineUtils.removeWeekendDayTimelines(userAllDatesTimeslines);
// // userAllDatesTimeslines=UtilityBelt.removeWeekdaysTimelines(userAllDatesTimeslines);
// // ////////////////////////////////////////////////////////////////////////////////
//
// WritingToFile.writeGivenDayTimelines(userName, userAllDatesTimeslines, "All", false, false, false);
//
// // Splitting the set of timelines into training set and test set.
// int numberOfValidDays = 0;
//
// for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
// {
// if (entry.getValue().containsAtLeastOneValidActivity() == false)
// { // if the day timelines contains no valid activity, then don't consider it for training or test
// continue;
// }
// numberOfValidDays++;
// }
//
// // int numberOfDays = userAllDatesTimeslines.size();
// int numberOfDaysForTraining = (int) Math.round(numberOfValidDays * percentageInTraining);// floor
//
// int numberOfDaysForTest = numberOfValidDays - numberOfDaysForTraining;
//
// if (numberOfDaysForTest < 1)
// {
// numberOfDaysForTest = 1;
// numberOfDaysForTraining = numberOfValidDays - numberOfDaysForTest;
// }
//
// LinkedHashMap<Date, UserDayTimeline> userTrainingTimelines = new LinkedHashMap<Date, UserDayTimeline>();
// LinkedHashMap<Date, UserDayTimeline> userTestTimelines = new LinkedHashMap<Date, UserDayTimeline>();
//
// int count = 1;
// for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
// {
// if (entry.getValue().containsAtLeastOneValidActivity() == false)
// { // if the day timelines contains no valid activity, then don't consider it for training or test
// continue;
// }
//
// if (count <= numberOfDaysForTraining)
// {
// userTrainingTimelines.put(entry.getKey(), entry.getValue());
// count++;
// }
//
// else
// {
// userTestTimelines.put(entry.getKey(), entry.getValue());
// count++;
// }
// }
//
// System.out.println("Number of Test days = " + userTestTimelines.size());
// System.out.println("Number of Training days = " + userTrainingTimelines.size());
//
// //
// WritingToFile.writeActivityCountsInGivenDayTimelines(userName, userAllDatesTimeslines, "AllTimelines");
// WritingToFile.writeActivityDurationInGivenDayTimelines(userName, userAllDatesTimeslines, "AllTimelines");
// WritingToFile.writeActivityOccPercentageOfTimelines(userName, userAllDatesTimeslines, "AllTimelines");
//
// //
//
// LinkedHashMap<String, Long> activityNameCountPairsOverAllTrainingDays =
// WritingToFile.writeActivityCountsInGivenDayTimelines(userName, userTrainingTimelines, "TrainingTimelines");
// activityNameCountPairsOverAllTrainingDays =
// (LinkedHashMap<String, Long>) UtilityBelt.sortByValue(activityNameCountPairsOverAllTrainingDays);
// String activityNameCountPairsOverAllTrainingDaysWithCount =
// getActivityNameCountPairsOverAllTrainingDaysWithCount(activityNameCountPairsOverAllTrainingDays);
// String activityNameCountPairsOverAllTrainingDaysWithoutCount =
// getActivityNameCountPairsOverAllTrainingDaysWithoutCount(activityNameCountPairsOverAllTrainingDays);
//
// System.out.println("for user:" + userName + " activityNameCountPairsOverAllTrainingDaysWithCount="
// + activityNameCountPairsOverAllTrainingDaysWithCount);
// System.out.println("for user:" + userName + " activityNameCountPairsOverAllTrainingDaysWithoutCount="
// + activityNameCountPairsOverAllTrainingDaysWithoutCount);
//
// WritingToFile.writeActivityCountsInGivenDayTimelines(userName, userTestTimelines, "TestTimelines");
//
// LinkedHashMap<String, Long> activityNameDurationPairsOverAllTrainingDays = WritingToFile
// .writeActivityDurationInGivenDayTimelines(userName, userTrainingTimelines, "TrainingTimelines");
// activityNameDurationPairsOverAllTrainingDays =
// (LinkedHashMap<String, Long>) UtilityBelt.sortByValue(activityNameDurationPairsOverAllTrainingDays);
// String activityNameDurationPairsOverAllTrainingDaysWithDuration =
// getActivityNameDurationPairsOverAllTrainingDaysWithDuration(activityNameDurationPairsOverAllTrainingDays);
// String activityNameDurationPairsOverAllTrainingDaysWithoutDuration =
// getActivityNameDurationPairsOverAllTrainingDaysWithoutDuration(
// activityNameDurationPairsOverAllTrainingDays);
//
// System.out.println("for user:" + userName + " activityNameDurationtPairsOverAllTrainingDaysWithDuration="
// + activityNameDurationPairsOverAllTrainingDaysWithDuration);
// System.out.println("for user:" + userName + " activityNameDurationPairsOverAllTrainingDaysWithoutDuration="
// + activityNameDurationPairsOverAllTrainingDaysWithoutDuration);
//
// WritingToFile.writeActivityDurationInGivenDayTimelines(userName, userTestTimelines, "TestTimelines");
//
// WritingToFile.writeNumOfDistinctValidActivitiesPerDayInGivenDayTimelines(userName, userTrainingTimelines,
// "Training");
// WritingToFile.writeNumOfDistinctValidActivitiesPerDayInGivenDayTimelines(userName, userTestTimelines, "Test");
//
// WritingToFile.writeActivityCountsInGivenDayTimelines(userName, userTrainingTimelines, "Training");
// WritingToFile.writeActivityCountsInGivenDayTimelines(userName, userTestTimelines, "Test");
//
// // WritingToFile.writeActivityOccPercentageOfTimelines(userName,userTrainingTimelines, "Training");
// // WritingToFile.writeActivityOccPercentageOfTimelines(userName,userTestTimelines, "Test");
//
// int numberOfWeekendsInTraining = WritingToFile.getNumberOfWeekendsInGivenDayTimelines(userTrainingTimelines);
// int numberOfWeekdaysInTraining = userTrainingTimelines.size() - numberOfWeekendsInTraining;
//
// bwNumOfWeekendsInTraining.write(userName + "," + numberOfWeekendsInTraining + "," + numberOfWeekdaysInTraining);
// bwNumOfWeekendsInTraining.newLine();
// // /////////////////////////////
//
// int numberOfWeekendsInAll = WritingToFile.getNumberOfWeekendsInGivenDayTimelines(userAllDatesTimeslines);
// int numberOfWeekdaysInAll = userAllDatesTimeslines.size() - numberOfWeekendsInAll;
//
// bwNumOfWeekendsInAll.write(userName + "," + numberOfWeekendsInAll + "," + numberOfWeekdaysInAll);
// bwNumOfWeekendsInAll.newLine();
// // ////////
//
// // /////////////////////////////
//
// int numberOfWeekendsInTest = WritingToFile.getNumberOfWeekendsInGivenDayTimelines(userTestTimelines);
// int numberOfWeekdaysInTest = userTestTimelines.size() - numberOfWeekendsInTest;
//
// bwNumOfWeekendsInTest.write(userName + "," + numberOfWeekendsInTest + "," + numberOfWeekdaysInTest);
// bwNumOfWeekendsInTest.newLine();
// // ////////
//
// int numberOfMorningRTs = 0, numberOfAfternoonRTs = 0, numberOfEveningRTs = 0;
// // Generating Recommendation Timestamps
// // generate date and times for recommendation
//
// // //////////////////////////////////
// // //Graft001
// for (Map.Entry<Date, UserDayTimeline> entry : userTestTimelines.entrySet())
// {
// int date = entry.getKey().getDate();
// int month = entry.getKey().getMonth() + 1;
// int year = entry.getKey().getYear() + 1900;
//
// String dateToRecomm = date + "/" + month + "/" + year;
// System.out.println(
// "For userid=" + userId + " entry.getKey()=" + entry.getKey() + " dateToRecomm=" + dateToRecomm);
//
// String weekDay = DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay());
//
// UserDayTimeline eachDayTimelineForUser = entry.getValue();
// ArrayList<ActivityObject> activityObjectsInThatDay = eachDayTimelineForUser.getActivityObjectsInDay();
//
// for (int j = 0; j < activityObjectsInThatDay.size() - 1; j++) // will not make recommendation for days which have only one activity
// {
// System.out.println("Iterating over potential recommendation times: current activityAtPotentialRecommTime="
// + (activityObjectsInThatDay.get(j).getActivityName()));
//
// if (activityObjectsInThatDay.get(j).getActivityName().equals("Unknown"))
// {
// System.out.println("Skipping because Unknown");
// continue;
// }
//
// if (activityObjectsInThatDay.get(j).getActivityName().equals("Others"))
// {
// System.out.println("Skipping because Others");
// continue;
// }
//
// Timestamp startTimestamp = activityObjectsInThatDay.get(j).getEndTimestamp();// getStartTimestamp();
//
// String timeCategory = Evaluation.getTimeCategoryOfTheDay(startTimestamp.getHours());
//
// if (UserDayTimeline.isNoValidActivityAfterItInTheDay(j, eachDayTimelineForUser))
// {
// System.out.println(
// "Skipping this recommendation point because there are no valid activity events after this in the day");
// if (j == activityObjectsInThatDay.size() - 1)
// {
// System.out.println("This was the last activity of the day:" + dateToRecomm + " for user:" + userId);
// }
// rtsInvalidWriter.write(userId + "," + dateToRecomm + "," + j + "," + startTimestamp + "," + weekDay
// + "," + timeCategory);
// rtsInvalidWriter.newLine();
// continue;
// }
//
// String startTimeString =
// startTimestamp.getHours() + ":" + startTimestamp.getMinutes() + ":" + startTimestamp.getSeconds();
//
// ActivityObject nextValidActivityAfteractivityRecommPoint1 = eachDayTimelineForUser
// .getNextValidActivityAfterActivityAtThisTime(new Timestamp(year - 1900, month - 1, date,
// startTimestamp.getHours(), startTimestamp.getMinutes(), startTimestamp.getSeconds(), 0));
//
// if (nextValidActivityAfteractivityRecommPoint1 == null)
// {
// System.out.println("User id" + userId + " Next activity event after " + startTimestamp + " is null");
// System.out.println(
// "Error in Sanity Check RT331: nextValidActivityAfteractivityRecommPoint1 is null, if it was such, we should have not reached this point of execution");
// }
//
// System.out.println("User id" + userId + " Next activity event after " + startTimestamp + " ="
// + nextValidActivityAfteractivityRecommPoint1.getActivityName());
// System.out.println("Recommendation point at this Activity Event are:- Start: " + startTimeString);// +" ,and Middle: "+middleTimeString);
//
// // ///////////
// // Now we have those recommendation times which are valid for making recommendations
// // ///////////////////Start//////////////////////////////////
// // $$RecommendationMasterDayWise2Faster recommP1 = new RecommendationMasterDayWise2Faster(userTrainingTimelines,
// // userTestTimelines, dateToRecomm,
// // $$ startTimeString, userId, thresholdValue, typeOfThreshold);
//
// RecommendationMasterDayWise2FasterJan2016 recommP1 =
// new RecommendationMasterDayWise2FasterJan2016(userTrainingTimelines, userTestTimelines,
// dateToRecomm, startTimeString, userId, thresholdValue, typeOfThreshold);
//
// WritingToFile.writeEditDistancesOfAllEndPointsHeader();
//
// double thresholdAsDistance = recommP1.getThresholdAsDistance();
//
// if (recommP1.hasThresholdPruningNoEffect() == false)
// {
// pruningHasSaturated = false;
// }
// // pruningHasSaturated=recommP1.hasThresholdPruningNoEffect();
//
// if (/* recommP1.getTopFiveRecommendedActivities()==null|| */recommP1.hasCandidateTimeslines() == false)
// {
// rtsWithNoCands.write(userId + "," + dateToRecomm + "," + j + "," + startTimestamp + "," + weekDay + ","
// + timeCategory + "," + recommP1.getActivityObjectAtRecomm().getActivityName() + ",");//
// // TODO + recommP1.totalNumberOfProbableCands + ","
// // + recommP1.numCandsRejectedDueToNoCurrentActivityAtNonLast + ","
// // + recommP1.numCandsRejectedDueToNoNextActivity);
// rtsWithNoCands.newLine();
// System.out.println("Cannot make recommendation at this point as there are no candidate timelines");
//
// bwNumOfCandTimelinesBelowThreshold.write(
// dateToRecomm + "," + startTimestamp + "," + weekDay + "," + thresholdAsDistance + "," + 0);
// bwNumOfCandTimelinesBelowThreshold.newLine();
//
// continue;
// }
//
// if (recommP1.hasCandidateTimelinesBelowThreshold() == false)
// {
// // rtsWithNoCands.write(userId+","+dateToRecomm+","+j+","+startTimestamp+","+weekDay+","+timeCategory);
// // rtsWithNoCands.newLine();
//
// bwNumOfCandTimelinesBelowThreshold.write(
// dateToRecomm + "," + startTimestamp + "," + weekDay + "," + thresholdAsDistance + "," + 0);
// bwNumOfCandTimelinesBelowThreshold.newLine();
//
// System.out.println(
// "Cannot make recommendation at this point as there are no candidate timelines BELOW THRESHOLD");
// continue;
// }
//
// bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + startTimestamp + "," + weekDay + ","
// + thresholdAsDistance + "," + recommP1.getNumberOfCandidateTimelinesBelowThreshold());
// bwNumOfCandTimelinesBelowThreshold.newLine();
//
// if (recommP1.getNextActivityNamesByEditDist() == null)
// {
// System.err.println(
// "Error in Sanity Check RT377:recommP1.getTopFiveRecommendedActivities()==null, but there are candidate timelines ");
// }
//
// if (recommP1.isNextActivityJustAfterRecommPointIsInvalid())
// {
// bwNextActInvalid.write(userId + "," + startTimestamp);
// bwNextActInvalid.newLine();
// }
//
// if (timeCategory.equalsIgnoreCase("Morning"))
// numberOfMorningRTs++;
// else if (timeCategory.equalsIgnoreCase("Afternoon"))
// numberOfAfternoonRTs++;
// else if (timeCategory.equalsIgnoreCase("Evening"))
// numberOfEveningRTs++;
//
// String actActualAtStart = nextValidActivityAfteractivityRecommPoint1.getActivityName();
// String actRecommAtStartWithoutDistance = recommP1.getTopRecommendedActivityNamesWithoutDistanceString();// getTopFiveRecommendedActivities();
// String actRecommAtStartWithDistance = recommP1.getNextActivityNamesByEditDist(); // its not just top five but top all
//
// String actAtRecommPoint = recommP1.getActivityObjectAtRecomm().getActivityName();
//
// String rankedRecommAtStartWithScore = recommP1.getRankedRecommendationWithRankScores();
// String rankedRecommAtStartWithoutScore = recommP1.getRankedRecommendationWithoutRankScores();
//
// System.out.println("** Ranked Recommended at Start=" + rankedRecommAtStartWithoutScore
// + ", while actual was=" + actActualAtStart);
//
// metaBufferWriter.write(userId + "_" + dateToRecomm + "_" + startTimeString + ",");
// actualBufferWriter.write(actActualAtStart + ",");
// top5BufferWriter.write(actRecommAtStartWithoutDistance + ",");
// topRecommWithDistance.write(actRecommAtStartWithDistance + ",");
//
// rankedRecommWithScore.write(rankedRecommAtStartWithScore + ",");
// rankedRecommWithoutScore.write(rankedRecommAtStartWithoutScore + ",");
//
// baseLineOccurrence.write(activityNameCountPairsOverAllTrainingDaysWithoutCount + ",");
// baseLineDuration.write(activityNameDurationPairsOverAllTrainingDaysWithoutDuration + ",");
//
// String[] splittedRecomm = rankedRecommAtStartWithoutScore.split(Pattern.quote("__"));
// bwMaxNumOfDistinctRecommendations.write(dateToRecomm + "," + startTimestamp + "," + weekDay + // UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())+
// "," + (splittedRecomm.length - 1) + "," + recommP1.getNumberOfCandidateTimelinesBelowThreshold());
// bwMaxNumOfDistinctRecommendations.newLine();
//
// bwRaw.write(userName + "," + dateToRecomm + "," + startTimestamp.getHours() + ":"
// + startTimestamp.getMinutes() + ":" + startTimestamp.getSeconds() + "," + timeCategory + ","
// + recommP1.getNumberOfValidActivitiesInActivitesGuidingRecommendation() + ","
// + recommP1.getNumberOfActivitiesInActivitesGuidingRecommendation() + "," + weekDay// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
// + "," + actActualAtStart + "," + rankedRecommAtStartWithScore + "," + actAtRecommPoint + ","
// + recommP1.getActivitiesGuidingRecomm());
// bwRaw.newLine();
//
// ActivityObject activityAtRecommPoint = recommP1.getActivityObjectAtRecomm();
//
// LinkedHashMap<Date, Triple<Integer, String, Double>> distanceScoresForRecomm =
// recommP1.getDistanceScoresSorted();
//
// for (Map.Entry<Date, Triple<Integer, String, Double>> entryScore : distanceScoresForRecomm.entrySet())
// {
// int dayOfCand = entryScore.getKey().getDate();
// int monthOfCand = entryScore.getKey().getMonth() + 1;
// int yearOfCand = entryScore.getKey().getYear() + 1900;
// String dateOfCand = dayOfCand + "-" + monthOfCand + "-" + yearOfCand;
//
// UserDayTimeline dayTimelineTemp =
// TimelineUtils.getUserDayTimelineByDateFromMap(userTrainingTimelines, entryScore.getKey());
//
// Integer endPointIndexWithLeastDistanceForThisCandidate = entryScore.getValue().getFirst(); //
// UtilityBelt.getIntegerByDateFromMap(recommP1.getEndPointIndexWithLeastDistanceForCandidateTimelines(),
// // entryScore.getKey());
// ActivityObject endPointActivityInCandidate =
// dayTimelineTemp.getActivityObjectAtPosition(endPointIndexWithLeastDistanceForThisCandidate);
// long diffStartTimeForEndPointsCand_n_GuidingInSecs =
// (activityAtRecommPoint.getStartTimestamp().getTime()
// - endPointActivityInCandidate.getStartTimestamp().getTime()) / 1000;
// long diffEndTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint.getEndTimestamp().getTime()
// - endPointActivityInCandidate.getEndTimestamp().getTime()) / 1000;
//
// bwRecommTimesWithEditDistances.write(dateToRecomm + "," + startTimestamp.getHours() + ":"
// + startTimestamp.getMinutes() + ":" + startTimestamp.getSeconds() + "," + actActualAtStart + ","
// + entryScore.getValue() + "," + dateOfCand + "," + diffStartTimeForEndPointsCand_n_GuidingInSecs
// + "," + diffEndTimeForEndPointsCand_n_GuidingInSecs + ","
// + dayTimelineTemp.getActivityObjectNamesInSequence() + "," + weekDay// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
// + "," + DateTimeUtils.getWeekDayFromWeekDayInt(entryScore.getKey().getDay()));
// bwRecommTimesWithEditDistances.newLine();
// }
// } // end of for loop over Activity Objects in that day.
// } // end of for loop over all test dates
// // //////////////////////////////////
//
// bwCountTimeCategoryOfRecomm
// .write(userName + "," + numberOfMorningRTs + "," + numberOfAfternoonRTs + "," + numberOfEveningRTs);
// bwCountTimeCategoryOfRecomm.newLine();
// metaBufferWriter.newLine();
// actualBufferWriter.newLine();
// top5BufferWriter.newLine();
// topRecommWithDistance.newLine();
// rankedRecommWithScore.newLine();
// rankedRecommWithoutScore.newLine();
//
// baseLineOccurrence.newLine();
// baseLineDuration.newLine();
//
// bwMaxNumOfDistinctRecommendations.close();
// bwRecommTimesWithEditDistances.close();
// bwNumOfCandTimelinesBelowThreshold.close();
// } // end of for over userID
//
// metaBufferWriter.close();
// actualBufferWriter.close();
// top5BufferWriter.close();
// topRecommWithDistance.close();
//
// rtsInvalidWriter.close();
// rtsWithNoCands.close();
// consoleLogStream.close();
//
// rankedRecommWithScore.close();
// rankedRecommWithoutScore.close();
//
// baseLineOccurrence.close();
// baseLineDuration.close();
//
// bwRaw.close();
// bwNumOfWeekendsInTraining.close();
// bwNumOfWeekendsInTest.close();
// bwNumOfWeekendsInAll.close();
// bwCountTimeCategoryOfRecomm.close();
//
// bwNextActInvalid.close();
// // bufferWriter4.close();
// }
// catch (IOException e)
// {
// e.printStackTrace();
// }
//
// catch (Exception e)
// {
// e.printStackTrace();
// }
// System.out.println("Pruning has Saturation is :" + pruningHasSaturated);
// String msg = "";
// if (pruningHasSaturated)
// {
// System.out.println("Pruning has Saturated");
// // PopUps.showMessage("Pruning has Saturated");
// }
// new Evaluation();
// // PopUps.showMessage(msg)
// // /////////////////////core
// }
// }
// System.out.println("ALL TESTS DONE");
// PopUps.showMessage("ALL TESTS DONE... u can shutdown the server");// +msg);
// }
//
// public String getActivityNameCountPairsOverAllTrainingDaysWithCount(LinkedHashMap<String, Long> nameCountPairsSorted)
// {
// String result = "";
//
// for (Map.Entry<String, Long> entry : nameCountPairsSorted.entrySet())
// {
// result += "__" + entry.getKey() + ":" + entry.getValue();
// }
// return result;
// }
//
// public String getActivityNameCountPairsOverAllTrainingDaysWithoutCount(LinkedHashMap<String, Long> nameCountPairsSorted)
// {
// String result = "";
//
// for (Map.Entry<String, Long> entry : nameCountPairsSorted.entrySet())
// {
// result += "__" + entry.getKey();
// }
// return result;
// }
//
// public String getActivityNameDurationPairsOverAllTrainingDaysWithDuration(LinkedHashMap<String, Long> nameDurationPairsSorted)
// {
// String result = "";
//
// for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
// {
// result += "__" + entry.getKey() + ":" + entry.getValue();
// }
// return result;
// }
//
// public String getActivityNameDurationPairsOverAllTrainingDaysWithoutDuration(LinkedHashMap<String, Long> nameDurationPairsSorted)
// {
// String result = "";
//
// for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
// {
// result += "__" + entry.getKey();
// }
// return result;
// }
//
// /**
// * Set the array of doubles containing threshold valyues to be used.
// *
// * @param typeOfThreshold
// */
// public void setThresholdsArray(String typeOfThreshold)
// {
// switch (typeOfThreshold)
// {
// case "Percent":
// this.thresholdsArray = percentThresholds;
// break;
// case "Global":
// this.thresholdsArray = globalThresholds;
// break;
// case "None":
// this.thresholdsArray = new int[] { 10000000 };
// break;
// default:
// System.err.println("Error: Unrecognised threshold type in setThresholdsArray()");
// }
// }
// }
