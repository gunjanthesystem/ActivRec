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
// import java.util.Arrays;
// import java.util.LinkedHashMap;
// import java.util.Map;
// import java.util.regex.Pattern;
//
// import org.activity.constants.Constant;
// import org.activity.constants.VerbosityConstants;
// import org.activity.io.WritingToFile;
// import org.activity.objects.ActivityObject;
// import org.activity.objects.Timeline;
// import org.activity.objects.Triple;
// import org.activity.recomm.RecommendationMasterBaseClosestTime;
// import org.activity.util.ComparatorUtils;
// import org.activity.util.ConnectDatabase;
// import org.activity.util.DateTimeUtils;
// import org.activity.util.TimelineUtils;
//
// public class RecommendationTestsBaseClosestTime
// {
// public static double percentageInTraining = 0.8;
// // public String outputPath ="/run/media/gunjan/OS/Users/gunjan/Documents/LifeLog App/OUTPUT/";
// public static String commonPath;// =Constant.commonPath;//"/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
// // Works/WorkingSet7July/";
//
// public String typeOfThreshold = "Percent";// "Percent"/"Global"
//
// public int globalThresholds[] = { 100000 };// 50,100,150,200,250,300,350,400,450,500,550,600,650,700,1000};
// public int percentThresholds[] = { 100 };// {50,60,70,80,90,100};
//
// // public final double globalDistanceThresh=1000;
// /*
// * public final double percentDistanceThresh=100; /*threshold for choosing candidate timelines, those candidate
// * timelines whose distance from the 'activity events guiding recommendations' is higher than the cost of replacing
// * 'percentageDistanceThresh' % of Activity Events in the activities guiding recommendation are pruned out from set
// * of candidate timelines
// */
// String typeOfThresholds[] = { "Global" };// Global"};//"Percent",
// public boolean pruningHasSaturated;
// int userIDs[];
// int thresholdsArray[];
//
// /**
// * TODO: make this faster, less writes, use stringbuilder for write
// *
// * @param userTimelines
// */
// public RecommendationTestsBaseClosestTime(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userTimelines)
// // /,// String userAtRecomm)
// {
// // if userid is not set in constant class, in case of gowalla
// if (userIDs == null || userIDs.length == 0)
// {
// userIDs = new int[userTimelines.size()];// System.out.println("usersTimelines.size() = " +
// // usersTimelines.size());
// System.out.println("UserIDs not set, hence extracting user ids from usersTimelines keyset");
// int count = 0;
// for (String userS : userTimelines.keySet())
// {
// userIDs[count++] = Integer.valueOf(userS);
// }
// Constant.setUserIDs(userIDs);
// }
// System.out.println("User ids = " + Arrays.toString(userIDs));
//
// for (String typeOfThreshold : typeOfThresholds)
// {
// if (typeOfThreshold.equals("Percent"))
// {
// thresholdsArray = percentThresholds;
// }
//
// else if (typeOfThreshold.equals("Global"))
// {
// thresholdsArray = globalThresholds;
// }
//
// else
// {
// System.err.println("Error: Unrecognised threshold type");
// }
//
// for (int thresholdValue : thresholdsArray)
// {
// // Constant.setCommonPath("/run/media/gunjan/HOME/gunjan/DCU Data Works Oct Space/Oct 20 BaseClosestST
// // Selective Cands/");
// commonPath = Constant.outputCoreResultsPath;
// Constant.setCommonPath(commonPath);
// System.out.println("Common path=" + Constant.getCommonPath());
// // commonPath = Constant.getCommonPath();
// // /////////////////////////////////////////////////////////////////////Core
//
// ArrayList<String> userNames = new ArrayList<String>();
// LinkedHashMap<Date, Timeline> userAllDatesTimeslines = null;
// pruningHasSaturated = true;
// try
// {
//
// File metaFile = new File(commonPath + "meta.csv");
// File dataActualFile = new File(commonPath + "dataActual.csv");
// File fileRecommWithoutDistance = new File(commonPath + "dataSimpleRecommTopWithoutDistance.csv");
// File fileRecommWithDistance = new File(commonPath + "dataSimpleRecommTopWithDistance.csv");
//
// File fileRankedRecommWithScore = new File(commonPath + "dataRankedRecommendationWithScores.csv");
// File fileRankedRecommWithoutScore = new File(
// commonPath + "dataRankedRecommendationWithoutScores.csv");
//
// File fileBaseLineOccurrence = new File(commonPath + "dataBaseLineOccurrence.csv");
// File fileBaseLineDuration = new File(commonPath + "dataBaseLineDuration.csv");
//
// File fileRTsInvalid = new File(commonPath + "recommPointsInvalidBecuzNoValidActivityAfterThis.csv");
// File fileRTsWithNoCands = new File(commonPath + "recommPointsWithNoCandidates.csv");
//
// File fileRaw = new File(commonPath + "Raw.csv");
//
// // File fileCountInActivitiesGuidingRecomm= new
// // File(commonPath+"CountInActivitiesGuidingRecomm.csv");
//
// File fileNumOfWeekendsInTraining = new File(commonPath + "NumberOfWeekendsInTraining.csv");
// // File fileNumOfWeekdaysInTraining = new File("NumberOfWeekdaysInTraining.csv");
//
// File fileCountTimeCategoryOfRecomm = new File(commonPath + "CountTimeCategoryOfRecommPoitns.csv");
//
// File consoleLog = new File(commonPath + "consoleLog.txt");
//
// File fileNextActInvalid = new File(commonPath + "NextActivityIsInvalid.csv");
// // File file4 =new File("dataRecommSingle.csv");
//
// metaFile.delete();
// dataActualFile.delete();
// fileRecommWithoutDistance.delete();
// fileRecommWithDistance.delete();
// fileRTsInvalid.delete();
// fileRTsWithNoCands.delete();
// consoleLog.delete();// file4.delete();
// fileRankedRecommWithScore.delete();
// fileRankedRecommWithoutScore.delete();
// fileBaseLineOccurrence.delete();
// fileBaseLineDuration.delete();
// fileRaw.delete();
// fileCountTimeCategoryOfRecomm.delete();
// fileNumOfWeekendsInTraining.delete();// fileNumOfWeekdaysInTraining.delete();
// fileNextActInvalid.delete();
// // fileCountInActivitiesGuidingRecomm.delete();
//
// new File(commonPath + "EditSimilarityCalculations.csv").delete();
//
// // if file doesnt exists, then create it
// metaFile.createNewFile();
// dataActualFile.createNewFile();
// fileRecommWithoutDistance.createNewFile();
// fileRecommWithDistance.createNewFile();
// fileRTsInvalid.createNewFile();
// fileRTsWithNoCands.createNewFile();
// consoleLog.createNewFile();
// fileRankedRecommWithScore.createNewFile();
// fileRankedRecommWithoutScore.createNewFile();
// fileBaseLineOccurrence.createNewFile();
// fileBaseLineDuration.createNewFile();
// fileRaw.createNewFile();
// fileNumOfWeekendsInTraining.createNewFile();
// fileCountTimeCategoryOfRecomm.createNewFile();
// // fileNumOfWeekdaysInTraining.createNewFile();
// fileNextActInvalid.createNewFile();
//
// PrintStream consoleLogStream = new PrintStream(consoleLog);
// System.setOut(consoleLogStream);
// System.setErr(consoleLogStream);
// /*
// * if(!file4.exists()){ file4.createNewFile(); }
// */
// // true = append file
// FileWriter metaWriter = new FileWriter(metaFile.getAbsoluteFile(), true);
// BufferedWriter bufferWriter = new BufferedWriter(metaWriter);
//
// FileWriter actualWriter = new FileWriter(dataActualFile.getAbsoluteFile(), true);
// BufferedWriter bufferActual = new BufferedWriter(actualWriter);
//
// FileWriter top5Writer = new FileWriter(fileRecommWithoutDistance.getAbsoluteFile(), true);
// BufferedWriter bufferRecommWithoutDistance = new BufferedWriter(top5Writer);
//
// BufferedWriter topRecommWithDistance = new BufferedWriter(
// new FileWriter(fileRecommWithDistance.getAbsoluteFile(), true));
//
// BufferedWriter rtsInvalidWriter = new BufferedWriter(
// new FileWriter(fileRTsInvalid.getAbsoluteFile(), true));
//
// BufferedWriter rtsWithNoCands = new BufferedWriter(
// new FileWriter(fileRTsWithNoCands.getAbsoluteFile(), true));
//
// BufferedWriter rankedRecommWithScore = new BufferedWriter(
// new FileWriter(fileRankedRecommWithScore.getAbsoluteFile(), true));
// BufferedWriter rankedRecommWithoutScore = new BufferedWriter(
// new FileWriter(fileRankedRecommWithoutScore.getAbsoluteFile(), true));
//
// BufferedWriter baseLineOccurrence = new BufferedWriter(
// new FileWriter(fileBaseLineOccurrence.getAbsoluteFile(), true));
// BufferedWriter baseLineDuration = new BufferedWriter(
// new FileWriter(fileBaseLineDuration.getAbsoluteFile(), true));
//
// BufferedWriter bwRaw = new BufferedWriter(new FileWriter(fileRaw.getAbsoluteFile(), true));
//
// BufferedWriter bwNumOfWeekendsInTraining = new BufferedWriter(
// new FileWriter(fileNumOfWeekendsInTraining.getAbsoluteFile(), true));
//
// BufferedWriter bwCountTimeCategoryOfRecomm = new BufferedWriter(
// new FileWriter(fileCountTimeCategoryOfRecomm.getAbsoluteFile(), true));
//
// BufferedWriter bwNextActInvalid = new BufferedWriter(
// new FileWriter(fileNextActInvalid.getAbsoluteFile(), true));
//
// // BufferedWriter bwCountInActivitiesGuidingRecomm= new BufferedWriter(new
// // FileWriter(fileCountInActivitiesGuidingRecomm.getAbsoluteFile(),true));
//
// // fileCountTimeCategoryOfRecomm
// // BufferedWriter bwNumOfWeekdaysInTraining = new BufferedWriter(new
// // FileWriter(fileNumOfWeekdaysInTraining.getAbsoluteFile(),true));
//
// // FileWriter singleRecommWriter = new FileWriter(file4.getAbsoluteFile(),true);
// // BufferedWriter bufferWriter4 = new BufferedWriter(singleRecommWriter);
//
// rtsInvalidWriter
// .write("User_ID,Date,Index_of_Activity Event,Start_Timestamp,Week_Day,Time_Categoryt\n");
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
// bwNextActInvalid.write("User,Timestamp_of_Recomm");
// bwNextActInvalid.newLine();
//
// // for(int userId=0;userId <3;userId++)
// // for(int userId=3;userId <5;userId++)
// //
// bwCountInActivitiesGuidingRecomm.write("User,RecommendationTime,TimeCategory,NumberOfValidActivities_in_Current_Timeline,NumberOfActivities_in_Current_Timeline");
// // bwCountInActivitiesGuidingRecomm.newLine();
// //
// for (int userId : userIDs) // for (int userId = 0; userId < 5; userId++)
// // for(int userId=3;userId <4;userId++)
// {
//
// System.out.println("\nUser id=" + userId);
//
// String userName = "";
// if (Constant.getDatabaseName().equals("gowalla1"))
// {
// userName = String.valueOf(userId);
// }
//
// else
// {
// userName = ConnectDatabase.getUserName(userId);// ConnectDatabase.getUserNameFromDatabase(userId);
// // String userName = ConnectDatabase.getUserNameFromDatabase(userId);
// }
//
// File maxNumberOfDistinctRecommendations = new File(
// commonPath + userName + "MaxNumberOfDistinctRecommendation.csv");
// maxNumberOfDistinctRecommendations.delete();
// maxNumberOfDistinctRecommendations.createNewFile();
// BufferedWriter bwMaxNumOfDistinctRecommendations = new BufferedWriter(
// new FileWriter(maxNumberOfDistinctRecommendations.getAbsoluteFile(), true));
// bwMaxNumOfDistinctRecommendations.write("DateOfRecomm" + ",TimeOfRecomm"
// + ",Week_Day,MaxNumOfDistictRecommendation,NumOfCandidateTimelines(after applying Threshold)");
// bwMaxNumOfDistinctRecommendations.newLine();
//
// File numOfCandTimelinesBelowThreshold = new File(commonPath + userName
// + "numberOfCandidateTimelinesBelow" + typeOfThreshold + thresholdValue + ".csv");
//
// numOfCandTimelinesBelowThreshold.delete();
// numOfCandTimelinesBelowThreshold.createNewFile();
// BufferedWriter bwNumOfCandTimelinesBelowThreshold = new BufferedWriter(
// new FileWriter(numOfCandTimelinesBelowThreshold.getAbsoluteFile(), true));
// bwNumOfCandTimelinesBelowThreshold.write("DateOfRecomm" + ",TimeOfRecomm"
// + ",Week_Day,ThresholdAsDistance,NumOfCandidateTimelinesBelowThreshold,");
// bwNumOfCandTimelinesBelowThreshold.newLine();
//
// File recommTimesWithEditDistances = new File(
// commonPath + userName + "RecommTimesWithEditDistance.csv");
// recommTimesWithEditDistances.delete();
// recommTimesWithEditDistances.createNewFile();
// BufferedWriter bwRecommTimesWithEditDistances = new BufferedWriter(
// new FileWriter(recommTimesWithEditDistances.getAbsoluteFile(), true));
// bwRecommTimesWithEditDistances.write("DateOfRecomm" + ",TimeOfRecomm,"
// +
// "TargetActivity,EditDistanceOfCandidateTimeline,DateOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,CandidateTimeline,WeekDayOfRecomm,WeekDayOfCandidate");
// bwRecommTimesWithEditDistances.newLine();
//
// userAllDatesTimeslines = userTimelines.get(Integer.toString(userId));// userId);
//
// // //////////////////REMOVING SELECTED TIMELINES FROM
// // DATASET///////////////////////////////////////////////////////
// userAllDatesTimeslines = TimelineUtils.removeDayTimelinesWithNoValidAct(userAllDatesTimeslines);
// userAllDatesTimeslines = TimelineUtils.removeWeekendDayTimelines(userAllDatesTimeslines);
// // userAllDatesTimeslines=UtilityBelt.removeWeekdaysTimelines(userAllDatesTimeslines);
// // ////////////////////////////////////////////////////////////////////////////////
//
// // $$WritingToFile.writeGivenDayTimelines(userName, userAllDatesTimeslines, "All", false, false,
// // $$ false);
//
// // Splitting the set of timelines into training set and test set.
// int numberOfValidDays = 0;
//
// for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
// {
// if (entry.getValue().containsAtLeastOneValidActivity() == false)
// { // if the day timelines contains no valid activity, then don't consider it for training or
// // test
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
// LinkedHashMap<Date, Timeline> userTrainingTimelines = new LinkedHashMap<>();
// LinkedHashMap<Date, Timeline> userTestTimelines = new LinkedHashMap<>();
//
// int count = 1;
// for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
// {
// if (entry.getValue().containsAtLeastOneValidActivity() == false)
// { // if the day timelines contains no valid activity, then don't consider it for training or
// // test
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
// LinkedHashMap<String, Long> activityNameCountPairsOverAllTrainingDays = WritingToFile
// .writeActivityCountsInGivenDayTimelines(userName, userTrainingTimelines,
// "TrainingTimelines");
// activityNameCountPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) ComparatorUtils
// .sortByValueDesc(activityNameCountPairsOverAllTrainingDays);
// String activityNameCountPairsOverAllTrainingDaysWithCount = getActivityNameCountPairsOverAllTrainingDaysWithCount(
// activityNameCountPairsOverAllTrainingDays);
// String activityNameCountPairsOverAllTrainingDaysWithoutCount =
// getActivityNameCountPairsOverAllTrainingDaysWithoutCount(
// activityNameCountPairsOverAllTrainingDays);
//
// System.out.println(
// "for user:" + userName + " activityNameCountPairsOverAllTrainingDaysWithCount="
// + activityNameCountPairsOverAllTrainingDaysWithCount);
// System.out.println(
// "for user:" + userName + " activityNameCountPairsOverAllTrainingDaysWithoutCount="
// + activityNameCountPairsOverAllTrainingDaysWithoutCount);
//
// WritingToFile.writeActivityCountsInGivenDayTimelines(userName, userTestTimelines,
// "TestTimelines");
//
// LinkedHashMap<String, Long> activityNameDurationPairsOverAllTrainingDays = WritingToFile
// .writeActivityDurationInGivenDayTimelines(userName, userTrainingTimelines,
// "TrainingTimelines");
// activityNameDurationPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) ComparatorUtils
// .sortByValueDesc(activityNameDurationPairsOverAllTrainingDays);
// String activityNameDurationPairsOverAllTrainingDaysWithDuration =
// getActivityNameDurationPairsOverAllTrainingDaysWithDuration(
// activityNameDurationPairsOverAllTrainingDays);
// String activityNameDurationPairsOverAllTrainingDaysWithoutDuration =
// getActivityNameDurationPairsOverAllTrainingDaysWithoutDuration(
// activityNameDurationPairsOverAllTrainingDays);
//
// System.out.println(
// "for user:" + userName + " activityNameDurationtPairsOverAllTrainingDaysWithDuration="
// + activityNameDurationPairsOverAllTrainingDaysWithDuration);
// System.out.println("for user:" + userName
// + " activityNameDurationPairsOverAllTrainingDaysWithoutDuration="
// + activityNameDurationPairsOverAllTrainingDaysWithoutDuration);
//
// WritingToFile.writeActivityDurationInGivenDayTimelines(userName, userTestTimelines,
// "TestTimelines");
//
// WritingToFile.writeNumOfDistinctValidActivitiesPerDayInGivenDayTimelines(userName,
// userTrainingTimelines, "Training");
// WritingToFile.writeNumOfDistinctValidActivitiesPerDayInGivenDayTimelines(userName,
// userTestTimelines, "Test");
//
// int numberOfWeekendsInTraining = TimelineUtils
// .getNumberOfWeekendsInGivenDayTimelines(userTrainingTimelines);
// int numberOfWeekdaysInTraining = userTrainingTimelines.size() - numberOfWeekendsInTraining;
//
// bwNumOfWeekendsInTraining
// .write(userName + "," + numberOfWeekendsInTraining + "," + numberOfWeekdaysInTraining);
// bwNumOfWeekendsInTraining.newLine();
// // /////////////////////////////
//
// int numberOfMorningRTs = 0, numberOfAfternoonRTs = 0, numberOfEveningRTs = 0;
// // Generating Recommendation Timestamps
// // generate date and times for recommendation
// for (Map.Entry<Date, Timeline> entry : userTestTimelines.entrySet())
// {
// int date = entry.getKey().getDate();
// int month = entry.getKey().getMonth() + 1;
// int year = entry.getKey().getYear() + 1900;
//
// String dateToRecomm = date + "/" + month + "/" + year;
// System.out.println("For userid=" + userId + " entry.getKey()=" + entry.getKey()
// + " dateToRecomm=" + dateToRecomm);
//
// String weekDay = DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay());
//
// Timeline eachDayTimelineForUser = entry.getValue();
// ArrayList<ActivityObject> activityObjectsInThatDay = eachDayTimelineForUser
// .getActivityObjectsInDay();
//
// for (int j = 0; j < activityObjectsInThatDay.size() - 1; j++) // will not make
// // recommendation for days
// // which have only one
// // activity
// {
// System.out.println(
// "Iterating over potential recommendation times: current activityAtPotentialRecommTime="
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
// Timestamp endTimestamp = activityObjectsInThatDay.get(j).getEndTimestamp();// getStartTimestamp();
//
// String timeCategory = DateTimeUtils.getTimeCategoryOfTheDay(endTimestamp.getHours());
//
// if (DateTimeUtils.isNoValidActivityAfterItInTheDay(j, eachDayTimelineForUser))
// {
// System.out.println(
// "Skipping this recommendation point because there are no valid activity events after this in the day");
// if (j == activityObjectsInThatDay.size() - 1)
// {
// System.out.println("This was the last activity of the day:" + dateToRecomm
// + " for user:" + userId);
// }
// rtsInvalidWriter.write(userId + "," + dateToRecomm + "," + j + "," + endTimestamp
// + "," + weekDay + "," + timeCategory);
// rtsInvalidWriter.newLine();
// continue;
// }
//
// String endTimeString = endTimestamp.getHours() + ":" + endTimestamp.getMinutes() + ":"
// + endTimestamp.getSeconds();
//
// ActivityObject nextValidActivityAfteractivityRecommPoint1 = eachDayTimelineForUser
// .getNextValidActivityAfterActivityAtThisTime(
// new Timestamp(year - 1900, month - 1, date, endTimestamp.getHours(),
// endTimestamp.getMinutes(), endTimestamp.getSeconds(), 0));
//
// if (nextValidActivityAfteractivityRecommPoint1 == null)
// {
// System.out.println("User id" + userId + " Next activity event after " + endTimestamp
// + " is null");
// System.out.println(
// "Error in Sanity Check RT331: nextValidActivityAfteractivityRecommPoint1 is null, if it was such, we should have not
// reached this point of execution");
// }
//
// System.out.println("User id" + userId + " Next activity event after " + endTimestamp
// + " =" + nextValidActivityAfteractivityRecommPoint1.getActivityName());
//
// System.out.println(
// "Recommendation point at this Activity Event are:- Start: " + endTimeString);// +"
// // ,and
// // Middle:
// // "+middleTimeString);
//
// // ///////////
// // Now we have those recommendation times which are valid for making recommendations
// // ///////////////////Start//////////////////////////////////
// // String
// // actRecommAtStart=recommP1.getSingleNextRecommendedActivity();RecommendationMaster
// // recommP1=new
// // RecommendationMaster(/*userTimelines,*/userTrainingTimelines,userTestTimelines,dateToRecomm,startTimeString,
// // userId);
//
// RecommendationMasterBaseClosestTime recommP1 = new RecommendationMasterBaseClosestTime(
// userTrainingTimelines, userTestTimelines, dateToRecomm, endTimeString, userId);
//
// double thresholdAsDistance = recommP1.getThresholdAsDistance();
//
// if (recommP1.hasThresholdPruningNoEffect() == false)
// {
// pruningHasSaturated = false;
// }
// // pruningHasSaturated=recommP1.hasThresholdPruningNoEffect();
//
// if (recommP1.hasCandidateTimeslines() == false)
// {
// rtsWithNoCands.write(userId + "," + dateToRecomm + "," + j + "," + endTimestamp
// + "," + weekDay + "," + timeCategory + ","
// + recommP1.getActivityAtRecomm().getActivityName() + ","
// + recommP1.totalNumberOfProbableCands + ","
// + recommP1.numCandsRejectedDueToNoCurrentActivityAtNonLast + ","
// + recommP1.numCandsRejectedDueToNoNextActivity);
// rtsWithNoCands.newLine();
// System.out.println(
// "Cannot make recommendation at this point as there are no candidate timelines");
//
// bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimestamp + ","
// + weekDay + "," + thresholdAsDistance + "," + 0);
// bwNumOfCandTimelinesBelowThreshold.newLine();
//
// continue;
// }
//
// if (recommP1.hasCandidateTimelinesBelowThreshold() == false)
// {
// bwNumOfCandTimelinesBelowThreshold.write(dateToRecomm + "," + endTimestamp + ","
// + weekDay + "," + thresholdAsDistance + "," + 0);
// bwNumOfCandTimelinesBelowThreshold.newLine();
//
// System.out.println(
// "Cannot make recommendation at this point as there are no candidate timelines BELOW THRESHOLD");
// continue;
// }
//
// bwNumOfCandTimelinesBelowThreshold.write(
// dateToRecomm + "," + endTimestamp + "," + weekDay + "," + thresholdAsDistance
// + "," + recommP1.getNumberOfCandidateTimelinesBelowThreshold());
// bwNumOfCandTimelinesBelowThreshold.newLine();
//
// if (recommP1.getRankedRecommendedActivityNamesWithoutRankScores() == null)
// {
// System.err.println(
// "Error in Sanity Check RT444:recommP1.getTopFiveRecommendedActivities()==null, but there are candidate timelines ");
// }
//
// if (recommP1.isNextActivityJustAfterRecommPointIsInvalid())
// {
// bwNextActInvalid.write(userId + "," + endTimestamp);
// bwNextActInvalid.newLine();
// }
//
// if (timeCategory.equalsIgnoreCase("Morning"))
// numberOfMorningRTs++;
// else if (timeCategory.equalsIgnoreCase("Afternoon"))
// numberOfAfternoonRTs++;
// else if (timeCategory.equalsIgnoreCase("Evening")) numberOfEveningRTs++;
//
// String actActualAtStart = nextValidActivityAfteractivityRecommPoint1.getActivityName();
// String actRecommAtStartWithoutDistance = recommP1
// .getNextActivityNamesWithoutDistanceString();// getTopFiveRecommendedActivities();
// String actRecommAtStartWithDistance = recommP1
// .getNextActivityNamesWithDistanceString(); // its not just top five but top
// // all
//
// String actAtRecommPoint = recommP1.getActivityAtRecomm().getActivityName();
//
// String rankedRecommAtStartWithScore = recommP1
// .getRankedRecommendedActivityNamesWithRankScores();
// String rankedRecommAtStartWithoutScore = recommP1
// .getRankedRecommendedActivityNamesWithoutRankScores();
//
// System.out.println("** Ranked Recommended at Start=" + rankedRecommAtStartWithoutScore
// + ", while actual was=" + actActualAtStart);
//
// bufferWriter.write(userId + "_" + dateToRecomm + "_" + endTimeString + ",");
// bufferActual.write(actActualAtStart + ",");
// bufferRecommWithoutDistance.write(actRecommAtStartWithoutDistance + ",");
// topRecommWithDistance.write(actRecommAtStartWithDistance + ",");
//
// rankedRecommWithScore.write(rankedRecommAtStartWithScore + ",");
// rankedRecommWithoutScore.write(rankedRecommAtStartWithoutScore + ",");
//
// baseLineOccurrence.write(activityNameCountPairsOverAllTrainingDaysWithoutCount + ",");
// baseLineDuration
// .write(activityNameDurationPairsOverAllTrainingDaysWithoutDuration + ",");
//
// String[] splittedRecomm = rankedRecommAtStartWithoutScore.split(Pattern.quote("__"));
// bwMaxNumOfDistinctRecommendations
// .write(dateToRecomm + "," + endTimestamp + "," + weekDay + //
// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())+
// "," + (splittedRecomm.length - 1) + ","
// + recommP1.getNumberOfCandidateTimelinesBelowThreshold());
// bwMaxNumOfDistinctRecommendations.newLine();
//
// bwRaw.write(userName + "," + dateToRecomm + "," + endTimestamp.getHours() + ":"
// + endTimestamp.getMinutes() + ":" + endTimestamp.getSeconds() + ","
// + timeCategory + ","
// + recommP1.getNumberOfValidActivitiesInActivitesGuidingRecommendation() + ","
// + recommP1.getNumberOfActivitiesInActivitesGuidingRecommendation() + ","
// + weekDay// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
// + "," + actActualAtStart + "," + rankedRecommAtStartWithScore + ","
// + actAtRecommPoint + "," + recommP1.getActivitiesGuidingRecomm());
// bwRaw.newLine();
//
// //
// //
// bwRaw.write(userName+","+dateToRecomm+","+startTimestamp.getHours()+":"+startTimestamp.getMinutes()+":"+startTimestamp.getSeconds()
// // +","+timeCategory+","+weekDay//UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
// // +","+
// // actActualAtStart+","+rankedRecommAtStartWithScore+","+actAtRecommPoint+","+recommP1.getActivitiesGuidingRecomm());
// // bwRaw.newLine();
// //
// bwRaw.write("User,DateOfRecomm,TimeOfRecomm,TimeCategoryOfRecomm,WeekDayOfRecomm,Target(ActualActivity),RecommendedActivity");
//
// ActivityObject activityAtRecommPoint = recommP1.getActivityAtRecomm();
//
// LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> distanceMapForRecomm = recommP1
// .getStartDistancesSortedMap();
//
// for (Map.Entry<Date, Triple<Integer, ActivityObject, Double>> entryDistance : distanceMapForRecomm
// .entrySet())
// {
// int dayOfCand = entryDistance.getKey().getDate();
// int monthOfCand = entryDistance.getKey().getMonth() + 1;
// int yearOfCand = entryDistance.getKey().getYear() + 1900;
// String dateOfCand = dayOfCand + "-" + monthOfCand + "-" + yearOfCand;
//
// Timeline dayTimelineTemp = TimelineUtils.getUserDayTimelineByDateFromMap(
// userTrainingTimelines, entryDistance.getKey());
// // Integer endPointIndexWithLeastDistanceForThisCandidate =
// // UtilityBelt.getIntegerByDateFromMap(recommP1.getEndPointIndexWithLeastDistanceForCandidateTimelines(),
// // entryDistance.getKey());
//
// ActivityObject endPointActivityInCandidate = entryDistance.getValue().getSecond();//
// dayTimelineTemp.getActivityObjectAtPosition(endPointIndexWithLeastDistanceForThisCandidate);
//
// long diffStartTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint
// .getStartTimestamp().getTime()
// - endPointActivityInCandidate.getStartTimestamp().getTime()) / 1000;
// long diffEndTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint
// .getEndTimestamp().getTime()
// - endPointActivityInCandidate.getEndTimestamp().getTime()) / 1000;
//
// if (VerbosityConstants.WriteRecommendationTimesWithEditDistance)
// {
// bwRecommTimesWithEditDistances.write(dateToRecomm + ","
// + endTimestamp.getHours() + ":" + endTimestamp.getMinutes() + ":"
// + endTimestamp.getSeconds() + "," + actActualAtStart + ","
// + entryDistance.getValue() + "," + dateOfCand + ","
// + diffStartTimeForEndPointsCand_n_GuidingInSecs + ","
// + diffEndTimeForEndPointsCand_n_GuidingInSecs + ","
// + dayTimelineTemp.getActivityObjectNamesInSequence() + "," + weekDay//
// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
// + "," + DateTimeUtils
// .getWeekDayFromWeekDayInt(entryDistance.getKey().getDay()));
// bwRecommTimesWithEditDistances.newLine();
// }
//
// }
// }
// } // end of for loop over all test dates
//
// bwCountTimeCategoryOfRecomm.write(userName + "," + numberOfMorningRTs + ","
// + numberOfAfternoonRTs + "," + numberOfEveningRTs);
// bwCountTimeCategoryOfRecomm.newLine();
// // bwCountTimeCategoryOfRecomm.write("User,Num_of_Mornings,Num_of_Afternoons,Number_of_Evenings");
//
// // RecommendationMaster recommendationMaster=new RecommendationMaster(userTimelines,
// // userTrainingTimelines,userTestTimelines,dateAtRecomm,timeAtRecomm,userAtRecomm);
// bufferWriter.newLine();
// bufferActual.newLine();
// bufferRecommWithoutDistance.newLine();
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
//
// // bufferWriter4.newLine();
// } // end of for over userID
//
// bufferWriter.close();
// bufferActual.close();
// bufferRecommWithoutDistance.close();
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
//
// // RecommendationMaster recommendationMaster=new
// // RecommendationMaster(userTimelines,dateAtRecomm,timeAtRecomm,weekDayAtRecomm,userAtRecomm);
// System.out.println("Pruning has Saturation is :" + pruningHasSaturated);
// String msg = "";
//
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
//
// // PopUps.showMessage("ALL TESTS DONE... u can shutdown the server");// +msg);
//
// }
//
// public String getActivityNameCountPairsOverAllTrainingDaysWithCount(
// LinkedHashMap<String, Long> nameCountPairsSorted)
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
// public String getActivityNameCountPairsOverAllTrainingDaysWithoutCount(
// LinkedHashMap<String, Long> nameCountPairsSorted)
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
// public String getActivityNameDurationPairsOverAllTrainingDaysWithDuration(
// LinkedHashMap<String, Long> nameDurationPairsSorted)
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
// public String getActivityNameDurationPairsOverAllTrainingDaysWithoutDuration(
// LinkedHashMap<String, Long> nameDurationPairsSorted)
// {
// String result = "";
//
// for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
// {
// result += "__" + entry.getKey();
// }
// return result;
// }
// }
