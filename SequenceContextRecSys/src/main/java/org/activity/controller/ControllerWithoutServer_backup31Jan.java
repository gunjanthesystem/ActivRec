// package org.activity.controller;
//
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.sql.Date;
// import java.sql.SQLException;
// import java.sql.Timestamp;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Map.Entry;
// import java.util.TimeZone;
// import java.util.TreeMap;
// import java.util.stream.Collectors;
//
// import org.activity.clustering.weka.TimelineWEKAClusteringController;
// import org.activity.constants.Constant;
// import org.activity.constants.DomainConstants;
// import org.activity.constants.PathConstants;
// import org.activity.evaluation.RecommendationTestsMar2017GenSeqCleaned3Nov2017;
// import org.activity.io.SerializableJSONArray;
// import org.activity.io.Serializer;
// import org.activity.io.WritingToFile;
// import org.activity.objects.ActivityObject;
// import org.activity.objects.CheckinEntry;
// import org.activity.objects.LocationGowalla;
// import org.activity.objects.Pair;
// import org.activity.objects.Timeline;
// import org.activity.objects.UserGowalla;
// import org.activity.ui.PopUps;
// import org.activity.util.ConnectDatabase;
// import org.activity.util.PerformanceAnalytics;
// import org.activity.util.TimelineUtils;
// import org.activity.util.UtilityBelt;
//
/// **
// * This class is used for recommending without the web interface (no web server) Note: to run these experiments proper
// * parameters must be set in class org.activity.util.Constant and this class
// *
// * @author gunjan
// */
// public class ControllerWithoutServer_backup31Jan
// {
// String pathToLatestSerialisedJSONArray = "", pathForLatestSerialisedJSONArray = "",
// pathToLatestSerialisedTimelines = "", pathForLatestSerialisedTimelines = "", commonPath = "";
//
// public ControllerWithoutServer_backup31Jan()
// {
// try
// {
// System.out.println("Starting ControllerWithoutServer>>>>");
// System.out.println(PerformanceAnalytics.getHeapInformation());
// LocalDateTime currentDateTime = LocalDateTime.now();
// TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 21, 2016
// Constant.setDefaultTimeZone("UTC");
//
// // String pathToLatestSerialisedJSONArray = "", pathForLatestSerialisedJSONArray = "",
// // pathToLatestSerialisedTimelines = "", pathForLatestSerialisedTimelines = "", commonPath = "";
// System.out.println("Running experiments for database: " + Constant.getDatabaseName());// .DATABASE_NAME);
//
// setPaths(Constant.getDatabaseName());
// // new ConnectDatabase(Constant.getDatabaseName()); // all method and variable in this class are static
// // new Constant(commonPath, Constant.getDatabaseName());
//
// System.out.println("Just before Constant.initialise:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
// + PerformanceAnalytics.getHeapPercentageFree());
// /*
// * $ Disabled for Gowalla dataset for now// ConnectDatabase.initialise(Constant.getDatabaseName()); // all
// * method and variable in this class are static
// */
// // specific for Gowalla dataset
// PathConstants.intialise(Constant.For9kUsers);
// Constant.initialise(commonPath, Constant.getDatabaseName(), PathConstants.pathToSerialisedCatIDsHierDist,
// PathConstants.pathToSerialisedCatIDNameDictionary, PathConstants.pathToSerialisedLocationObjects,
// PathConstants.pathToSerialisedUserObjects, PathConstants.pathToSerialisedGowallaLocZoneIdMap);
//
// // ,// 550);
// System.out.println("Just after Constant.initialise:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
// + PerformanceAnalytics.getHeapPercentageFree());
//
// long dt1 = System.currentTimeMillis();
//
// if (Constant.toSerializeJSONArray)
// {
// fetchAndSerializeJSONArray(Constant.getDatabaseName());
// }
// //
// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// SerializableJSONArray jsonArrayD = null;
// long dt2 = System.currentTimeMillis();
// if (Constant.toDeSerializeJSONArray)
// {
// jsonArrayD = (SerializableJSONArray) Serializer.deSerializeThis(pathToLatestSerialisedJSONArray);//
// GeolifeJSONArrayDec24_2.obj");
// System.out.println("Deserialized JSONArray");
// }
// // // System.out.println(jsonArray.getJSONArray().toString() == jsonArrayD.getJSONArray().toString());
// // // System.out.println(jsonArray.getJSONArray().toString().length());
// // // System.out.println("---------");
// // // System.out.println(jsonArray.getJSONArray().toString().length());
// //
// // // System.out.println(StringUtils.difference(jsonArray.getJSONArray().toString(),
// // jsonArrayD.getJSONArray().toString()));
// // System.out.println("ajooba");1
// // //System.out.println(jsonArray.toString());
// //
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal = null;
// // new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
// System.out.println("Before createTimelines\n" + PerformanceAnalytics.getHeapInformation());
// if (Constant.toCreateTimelines)
// {
// usersDayTimelinesOriginal = createTimelines(Constant.getDatabaseName(), jsonArrayD,
// PathConstants.commonPathToGowallaPreProcessedData);
// }
// System.out.println("After createTimelines\n" + PerformanceAnalytics.getHeapInformation());
//
// // //to improve repeat execution performance...serialising
// if (Constant.toSerializeTimelines)
// {
// Serializer.serializeThis(usersDayTimelinesOriginal, pathForLatestSerialisedTimelines);
// pathToLatestSerialisedTimelines = pathForLatestSerialisedTimelines;
// System.out.println("Serialized Timelines");
// }
// //
// // // /////////////////////////////////////
// if (Constant.toDeSerializeTimelines)
// {
// if (Constant.getDatabaseName().equals("gowalla1"))
// {
// usersDayTimelinesOriginal = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
// .kryoDeSerializeThis(pathToLatestSerialisedTimelines);
// }
// else
// {
// usersDayTimelinesOriginal = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
// .deSerializeThis(pathToLatestSerialisedTimelines);
// }
// System.out.println("deserialised userTimelines.size()=" + usersDayTimelinesOriginal.size());
// System.out.println("Deserialized Timelines");
// }
// long ct = System.currentTimeMillis();
//
// if (!Constant.checkAllParametersSet())
// {
// System.err.println("All essential paramaters in Constant not set. Exiting");
// PopUps.showError("All essential paramaters in Constant not set. Exiting");
// System.exit(-162);
// }
//
// Pair<Boolean, String> hasDuplicateDates = TimelineUtils.hasDuplicateDates(usersDayTimelinesOriginal);
// if (hasDuplicateDates.getFirst())
// {
// System.out.println(
// "Alert!Alert! hasDuplicateDates. for users:" + hasDuplicateDates.getSecond().toString());
// }
// else
// {
// System.out.println("Thank God, no duplicate dates.");
// }
// //////////// for Gowalla start
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false, "GowallaUserDayTimelines.csv");
// System.out.println("Before reduceAndCleanTimelines\n" + PerformanceAnalytics.getHeapInformation());
//
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = null;
// // For 9k users
// if (Constant.For9kUsers)
// {
// usersCleanedDayTimelines = ControllerWithoutServer_backup31Jan
// .reduceAndCleanTimelines2(Constant.getDatabaseName(), usersDayTimelinesOriginal, true);
// }
// else
// {
// // For 916 users
// usersCleanedDayTimelines = ControllerWithoutServer_backup31Jan
// .reduceAndCleanTimelines(Constant.getDatabaseName(), usersDayTimelinesOriginal, true);
// }
// usersDayTimelinesOriginal = null; // null this out so as to be ready for garbage collection.
// System.out.println("After reduceAndCleanTimelines\n" + PerformanceAnalytics.getHeapInformation());
// long dt3 = System.currentTimeMillis();
// System.out.println("Time taken = " + (dt3 - dt1) + " ms");
// // System.exit(0);
// /////////// start of temp
// // $$WritingToFile.writeNumberOfActsWithMultipleWorkingLevelCatID(usersCleanedDayTimelines, true,
// // $$ Constant.outputCoreResultsPath + "MultipleWorkingLevelCatIds.csv");
// /////////// end of temp
//
// // $$TimelineStats.timelineStatsController(usersCleanedDayTimelines);
// // $$TimelineWEKAClusteringController clustering = new
// // TimelineWEKAClusteringController(usersCleanedDayTimelines, null);
//
// // // start of for gowalla weather data generation //commented out 22 Jan 2017
// // GowallaWeatherPreprocessing.GowallaWeatherPreprocessingController(usersCleanedDayTimelines,
// // Constant.outputCoreResultsPath);
// // // end of for gowalla weather data generation //commented out 22 Jan 2017
//
// // start of consective counts
// // good curtain 7 Feb 2017 start
// // LinkedHashMap<String, ArrayList<Integer>> consecutiveCounts = TimelineUtils
// // .countConsecutiveSimilarActivities2(usersCleanedDayTimelines,
// // /* $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan22/" */
// // // "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/ConsecutiveAnalysis/",
// // "./dataWritten/ConsecutiveDiffAnalysis3/",
// // "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/UI/CatIDNameDictionary.kryo");
// // "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/CatIDNameDictionary.kryo");
// // good curtain 7 Feb 2017 end
// // end of consecutive counts
//
// // String groupsOf100UsersLabels[] = { "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };
// // ,// "1001" };
// // System.out.println("List of all users:\n" + usersCleanedDayTimelines.keySet().toString() + "\n");
//
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// Constant.getCommonPath() + "NumOfActsPerUserPerDate.csv");
//
// String commonBasePath = Constant.getCommonPath();
//
// System.out.println("Before sampleUsersExec\n" + PerformanceAnalytics.getHeapInformation());
//
// ///////////////////
// TimelineUtils.countNumOfMultipleLocationIDs(usersCleanedDayTimelines);
// Constant.setUniqueLocIDs(TimelineUtils.getUniqueLocIDs(usersCleanedDayTimelines, false));
// Constant.setUniqueActivityIDs(TimelineUtils.getUniqueActivityIDs(usersCleanedDayTimelines, false));
// TimelineUtils.getUniquePDValPerUser(usersCleanedDayTimelines, true, "UniquePDValPerUser.csv");
// ///////////////////
// /*
// * TreeSet<Integer> uniqueLocIDs = getUniqueLocIDs(usersCleanedDayTimelines);
// * Serializer.serializeThis(uniqueLocIDs, "./dataWritten/UniqueLocIDsInCleanedTimeines.ser");
// * TreeSet<Integer> uniqueLocIDs1 = (TreeSet<Integer>) Serializer
// * .deSerializeThis("./dataWritten/UniqueLocIDsInCleanedTimeines.ser");
// * Serializer.kryoSerializeThis(uniqueLocIDs, "./dataWritten/UniqueLocIDsInCleanedTimeines.kryo");
// * TreeSet<Integer> uniqueLocIDs2 = (TreeSet<Integer>) Serializer
// * .kryoDeSerializeThis("./dataWritten/UniqueLocIDsInCleanedTimeines.kryo");
// */
//
// // $$TimelineStats.writeAllCitiesCounts(usersCleanedDayTimelines,
// // $$ Constant.outputCoreResultsPath + "AllCitiesCount");
// // // important curtain 1 start 21 Dec 2017 10 Feb 2017
// // $$ if (Constant.For9kUsers)
// // {
// // // Start of curtain Aug 14 2017
// // selectGivenUsersExecuteRecommendationTests(usersCleanedDayTimelines, IntStream
// // .of(DomainConstants.gowallaUserIDInUserGroup1Users).boxed().collect(Collectors.toList()),
// // commonBasePath, "1");
// // // End of curtain Aug 14 2017
// // }
// // else
// // {
// // // Start of curtain Aug 11 2017
// // sampleUsersExecuteRecommendationTests(usersCleanedDayTimelines, DomainConstants.gowallaUserGroupsLabels,
// // commonBasePath);
// // // End of curtain Aug 11 2017
// // }
// // $$// important curtain 1 end 21 Dec 2017 10 Feb 2017
//
// // // important curtain 2 start 2 June 2017
// // TimelineStats.timelineStatsController(usersCleanedDayTimelines);
// // // important curtain 2 start 2 June 2017
// //////////// for Gowalla end
//
// // WritingToFile.writeUsersDayTimelines(usersDayTimelinesOriginal, "usersDayTimelinesOriginal", true, true,
// // true);// users
// // $$ WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // true, true, true, "AllInSameFileApr21UncorrrectedTZ.csv");// users
//
// // //Start of disabled on 15 Sep 2016 _1
// // String serialisedTimelines1 = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR21.lmap";
// // String serialisedTimelines2 = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesJUN18.lmap";
// // TimelineUtilities.CompareTimelines(serialisedTimelines1, serialisedTimelines2);
// // LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersCleanedRearrangedDayTimelines =
// // new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
// // usersCleanedRearrangedDayTimelines = TimelineUtilities.cleanDayTimelines(usersDayTimelinesOriginal);
// // usersCleanedRearrangedDayTimelines =
// // TimelineUtilities.rearrangeDayTimelinesOrderForDataset(usersCleanedRearrangedDayTimelines);
// // //end of disabled on 15 Sep 2016 _1
//
// // $$ WritingToFile.writeUsersDayTimelines(usersDayTimelinesOriginal, "usersCleanedRearrangedDayTimelines",
// // true, true, true);// users
//
// // UtilityBelt.traverseUserTimelines(userTimelines); // Debugging Check: OK Cheked again with timestamp: OK
// // UtilityBelt.traverseActivityEvents(allActivityEvents); // Debugging Check: OK
//
// // for actual recommendation
// // RecommendationMaster recommendationMaster=new
// // RecommendationMaster(userTimelines,dateAtRecomm,timeAtRecomm,weekDayAtRecomm,userAtRecomm);
//
// // for testing
// // RecommendationTestsBaseClosestTime recommendationsTest=new
// // RecommendationTestsBaseClosestTime(userTimelines);//,userAtRecomm);
// // RecommendationTestsDayWise recommendationsTest=new
// // RecommendationTestsDayWise(userTimelines);//,userAtRecomm);
//
// /** CURRENT: To run the recommendation experiments **/
// // $$ RecommendationTestsDayWise2FasterJan2016 recommendationsTest =
// // $$ new RecommendationTestsDayWise2FasterJan2016(usersDayTimelinesOriginal);
// // $$ disabled on 15 Sep 2016RecommendationTestsMU recommendationsTest = new
// // RecommendationTestsMU(usersDayTimelinesOriginal);
// // $$RecommendationTestsMU recommendationsTest = new RecommendationTestsMU(sampledUsers);//
// // usersCleanedDayTimelines);
// // $$$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);//
// // usersCleanedDayTimelines);
// // $$RecommendationTestsBaseClosestTime recommendationsTest = new
// // RecommendationTestsBaseClosestTime(sampledUsers);// usersCleanedDayTimelines);
//
// /**** CURRENT END ****/
//
// /** To get some stats on the generated timelines **/
// // $$TimelineStats.timelineStatsController(usersCleanedDayTimelines);// usersTimelines);
// // TimelineStats.timelineStatsController(usersDayTimelines);
// // $$ TimelineStats.timelineStatsController(usersDayTimelinesOriginal);
//
// /** Clustering timelines using weka and after feature extraction **/
// // LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersCleanedRearrangedDayTimelines = new
// // LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
// // usersCleanedRearrangedDayTimelines = UtilityBelt.cleanDayTimelines(usersDayTimelinesOriginal);
// // usersCleanedRearrangedDayTimelines =
// // UtilityBelt.rearrangeDayTimelinesOrderForDataset(usersCleanedRearrangedDayTimelines);
//
// /** CURRENT **/
// TimelineWEKAClusteringController clustering = new TimelineWEKAClusteringController(usersCleanedDayTimelines,
// null);
// // usersCleanedRearrangedDayTimelines, null);
//
// /** END OF CURRENT **/
//
// /** CURRENT **/
// // $$TimelineStats.timelineStatsController(usersCleanedRearrangedDayTimelines);
// /** END OF CURRENT **/
//
// /** To experiment with distance **/
// // ExperimentDistances ed = new ExperimentDistances(usersTimelines);
//
// long et = System.currentTimeMillis();
//
// if (Constant.USEPOOLED)
// {
// ConnectDatabase.destroyPooledConnection();
// }
// System.out.println("This experiment took " + ((et - ct) / 1000) + " seconds");
//
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
//
// }
//
// /**
// *
// * @param usersCleanedDayTimelines
// * @param groupsOf100UsersLabels
// * @param commonBasePath
// * @throws IOException
// */
// private void sampleUsersExecuteRecommendationTests(
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
// String[] groupsOf100UsersLabels, String commonBasePath) throws IOException
// {
// // LinkedHashMap<Integer, String> indexOfBlackListedUsers = new LinkedHashMap<>();
//
// for (String groupsOf100UsersLabel : groupsOf100UsersLabels)
// {
// System.out.println("-- iteration start for groupsOf100UsersLabel = " + groupsOf100UsersLabel);
// // important so as to wipe the previously assigned user ids
// Constant.initialise(commonPath, Constant.getDatabaseName());
//
// int startUserIndex = Integer.valueOf(groupsOf100UsersLabel) - 1;// 100
// int endUserIndex = startUserIndex + 99;// $$ should be 99;// 199;// 140; // 199
// Constant.setOutputCoreResultsPath(commonBasePath + groupsOf100UsersLabel + "/");
//
// Files.createDirectories(Paths.get(Constant.getOutputCoreResultsPath())); // added on 9th Feb 2017
// int indexOfSampleUser = 0;
//
// /// sample users
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsers = new LinkedHashMap<>(134);
// // (ceil) 100/0.75
//
// int numOfUsersSkippedGT553MaxActsPerDay = 0;
// for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
// {
// System.out.println(" indexOfSampleUser = " + indexOfSampleUser);
// // countOfSampleUsers += 1;
// if (indexOfSampleUser < startUserIndex)
// {
// System.out.println(" " + indexOfSampleUser + "<" + startUserIndex + " hence skipping");
//
// indexOfSampleUser += 1;
// continue;
// }
// if (indexOfSampleUser > endUserIndex)
// {
// System.out.println(" " + indexOfSampleUser + ">" + startUserIndex + " hence breaking");
//
// indexOfSampleUser += 1;
// break;
// }
//
// if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
// {
// System.out.println(" " + indexOfSampleUser + " Skipping user: " + userEntry.getKey()
// + " as in gowallaUserIDsWithGT553MaxActsPerDay");
// WritingToFile.appendLineToFileAbsolute(indexOfSampleUser + "," + userEntry.getKey() + "\n",
// "IndexOfBlacklistedUsers.csv");
// numOfUsersSkippedGT553MaxActsPerDay += 1;
// indexOfSampleUser += 1;
// continue;
// }
// else
// {
// System.out.println(" choosing this ");
//
// sampledUsers.put(userEntry.getKey(), userEntry.getValue());
// indexOfSampleUser += 1;
// }
// // $$System.out.println("putting in user= " + userEntry.getKey());
//
// }
//
// // start of get timelines for all users for collaborative approach
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsers = new LinkedHashMap<>(1000);
// int numOfAllUsersSkippedGT553MaxActsPerDay = 0;
// if (Constant.collaborativeCandidates)
// {
// for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
// {
// if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
// {
// numOfAllUsersSkippedGT553MaxActsPerDay += 1;
// continue;
// }
// else
// {
// allUsers.put(userEntry.getKey(), userEntry.getValue());
// }
// }
// System.out.println("got timelines for all users for coll cand: allUsers.size()= " + allUsers.size());
// // Sanity.eq(numOfUsersSkippedGT553MaxActsPerDay, numOfAllUsersSkippedGT553MaxActsPerDay,
// System.out.println("numOfUsersSkippedGT553MaxActsPerDay=" + numOfUsersSkippedGT553MaxActsPerDay
// + " numOfAllUsersSkippedGT553MaxActsPerDay=" + numOfAllUsersSkippedGT553MaxActsPerDay);
//
// }
// // end of get timelines for all users for collaborative approach
//
// System.out.println("num of sampled users for this iteration = " + sampledUsers.size());
// System.out.println(" -- Users = " + sampledUsers.keySet().toString());
//
// // $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
// // $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
// // $$RecommendationTestsBaseClosestTime recommendationsTest = new RecommendationTestsBaseClosestTime(
// // $$ sampledUsers);
//
// System.out.println("Just Before recommendationsTest\n" + PerformanceAnalytics.getHeapInformation());
//
// // // start of curtain may 4 2017
// // RecommendationTestsMar2017Gen recommendationsTest = new RecommendationTestsMar2017Gen(sampledUsers,
// // Constant.lookPastType, Constant.caseType, Constant.typeOfThresholds, Constant.getUserIDs(),
// // Constant.percentageInTraining);
// // // end of curtain may 4 2017
//
// RecommendationTestsMar2017GenSeqCleaned3Nov2017 recommendationsTest = new
// RecommendationTestsMar2017GenSeqCleaned3Nov2017(
// sampledUsers, Constant.lookPastType, Constant.caseType, Constant.typeOfiiWASThresholds,
// Constant.getUserIDs(), Constant.percentageInTraining, 3, allUsers);
//
// /// /// RecommendationTestsMar2017GenDummyOnlyRTCount
//
// // RecommendationTestsDayWise2FasterJan2016 recommendationsTest = new
// // RecommendationTestsDayWise2FasterJan2016(sampledUsers);
//
// System.out.println("-- iteration end for groupsOf100UsersLabel = " + groupsOf100UsersLabel);
// }
// }
//
// /**
// *
// * @param usersCleanedDayTimelines
// * @param userIDsToSelect
// * @param commonBasePath
// * @param groupLabel
// * @throws IOException
// */
// private void selectGivenUsersExecuteRecommendationTests(
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
// List<Integer> userIDsToSelect, String commonBasePath, String groupLabel) throws IOException
// {
// System.out.println("-- iteration start for groupLabel = " + groupLabel);
// System.out.println("userIDsToSelect.size()= " + userIDsToSelect.size());
// // important so as to wipe the previously assigned user ids
// Constant.initialise(commonPath, Constant.getDatabaseName());
//
// Constant.setOutputCoreResultsPath(commonBasePath + groupLabel + "/");
// Files.createDirectories(Paths.get(Constant.getOutputCoreResultsPath())); // added on 9th Feb 2017
//
// /// sample users
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsers = new LinkedHashMap<>(userIDsToSelect.size());
// // (ceil) 100/0.75
//
// for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
// {
// if (userIDsToSelect.contains(Integer.valueOf(userEntry.getKey())))
// {
// sampledUsers.put(userEntry.getKey(), userEntry.getValue());
// }
// // $$System.out.println("putting in user= " + userEntry.getKey());
// }
//
// System.out.println("num of sampled users for this iteration = " + sampledUsers.size());
// System.out.println(" -- Users = " + sampledUsers.keySet().toString());
//
// // $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
// // $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
// // $$RecommendationTestsBaseClosestTime recommendationsTest = new RecommendationTestsBaseClosestTime(
// // $$ sampledUsers);
//
// System.out.println("Just Before recommendationsTest\n" + PerformanceAnalytics.getHeapInformation());
//
// // // start of curtain may 4 2017
// // RecommendationTestsMar2017Gen recommendationsTest = new RecommendationTestsMar2017Gen(sampledUsers,
// // Constant.lookPastType, Constant.caseType, Constant.typeOfThresholds, Constant.getUserIDs(),
// // Constant.percentageInTraining);
// // // end of curtain may 4 2017
//
// RecommendationTestsMar2017GenSeqCleaned3Nov2017 recommendationsTest = new
// RecommendationTestsMar2017GenSeqCleaned3Nov2017(
// sampledUsers, Constant.lookPastType, Constant.caseType, Constant.typeOfiiWASThresholds,
// Constant.getUserIDs(), Constant.percentageInTraining, 3, usersCleanedDayTimelines);
//
// System.out.println("-- iteration end for groupLabel = " + groupLabel);
// }
//
// /**
// * Sets commonPath, pathToLatestSerialisedJSONArray, pathForLatestSerialisedJSONArray,
// * pathToLatestSerialisedTimelines, pathForLatestSerialisedTimelines
// *
// * @param databaseName
// */
// public void setPaths(String databaseName)
// {
// LocalDateTime currentDateTime = LocalDateTime.now();
//
// switch (databaseName)
// {
// case "gowalla1":
// pathToLatestSerialisedJSONArray = null;// "";
// pathForLatestSerialisedJSONArray = "" + currentDateTime.getMonth().toString().substring(0, 3)
// + currentDateTime.getDayOfMonth() + "obj";
// pathToLatestSerialisedTimelines = null;
// // "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelinesNOV30.kryo";
// // "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelines.kryo";
// // "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/GowallaUserDayTimelines13Sep2016.kryo";//
// // "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep15DatabaseGenerationJava/GowallaUserDayTimelines13Sep2016.kryo";
// pathForLatestSerialisedTimelines = null;
// // "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelines"
// // + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth()
// // + ".kryo";
// commonPath = Constant.getOutputCoreResultsPath();
// // "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/";
// // $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan22/";// $$Nov30/";//
// // run/media/gunjan/BoX1/GowallaSpaceSpaceSpace/GowallaDataWorksSep19/";//
// /// "/run/media/gunjan/BoX2/GowallaSpaceSpace/GowallaDataWorksSep16/";
// break;
//
// case "geolife1":
// pathToLatestSerialisedJSONArray = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArrayAPR21obj";
// // "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArrayMAY27obj";
// // GeolifeJSONArrayFeb13.obj";
// pathForLatestSerialisedJSONArray = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArray"
// + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth()
// + "obj";
//
// // $$UMAP submission
// // $$pathToLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data
// // Works/UserTimelinesJUN18.lmap";//
// // "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesJUN15.lmap";//
// // "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data
// // Works/UserTimelinesAPR15.lmap";//
// // "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR10.lmap";//
// // UserTimelinesFeb13.lmap";
//
// // After UMAP submission 19th April 2016
// pathToLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR21.lmap";
//
// pathForLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelines"
// + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth()
// + ".lmap";
// commonPath = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/";// version 3 based on rank score
// // function
// break;
//
// case "dcu_data_2":
// pathToLatestSerialisedJSONArray = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
// Works/WorkingSet7July/JSONArrayOct29.obj";
// pathForLatestSerialisedJSONArray = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
// Works/WorkingSet7July/JSONArray"
// + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth()
// + "obj";
//
// pathToLatestSerialisedTimelines = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
// Works/WorkingSet7July/DCUUserTimelinesJUN19.lmap";
// // "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
// // Works/WorkingSet7July/DCUUserTimelinesJUN15.lmap";
// // "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
// // Works/WorkingSet7July/DCUUserTimelinesMAY7.lmap"; DCUUserTimelinesOct29.lmap";
// pathForLatestSerialisedTimelines = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
// Works/WorkingSet7July/DCUUserTimelines"
// + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth()
// + ".lmap";
//
// commonPath = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/";
// break;
//
// default:
// System.err.println("Error: unrecognised database name");
// break;
// }
//
// }
//
// /**
// * Fetch fresh data from database and Serialized JSONArray
// *
// * @param databaseName
// * @throws SQLException
// */
// public void fetchAndSerializeJSONArray(String databaseName) throws SQLException
// {
// String selectedAttributes = "activity_fact_table.User_ID," +
// // date_dimension_table.Date,time_dimension_table.Start_Time," +
// " activity_fact_table.Activity_ID, activity_fact_table.Time_ID, activity_fact_table.Location_ID,
// activity_fact_table.Date_ID";
//
// String orderByString = "activity_fact_table.User_ID, date_dimension_table.Date, time_dimension_table.Start_Time";
// // String whereQueryString ="where activity_dimension_table.Activity_Name!='Not Available' &&
// // activity_dimension_table.Activity_Name!='Unknown'";
//
// String whereQueryString = "";
//
// if (databaseName.equals("geolife1"))
// {
// whereQueryString = "where "
// + /*
// * * * "activity_fact_table.User_ID in ( 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 ) && " +
// */"activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY1
// + "' && activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY2 + "'";// for
// // faster
// } // "";// "where activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY1 +
// // "' && activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY2 + "'";
//
// SerializableJSONArray jsonArray = new SerializableJSONArray(
// ConnectDatabase.getJSONArrayOfDataTable(selectedAttributes, whereQueryString, orderByString));
//
// WritingToFile.writeToNewFile(jsonArray.toString(), Constant.getCommonPath() + "JSONArray.csv");
// // System.out.println(jsonArray.toString());
// Serializer.serializeThis(jsonArray, pathForLatestSerialisedJSONArray);
// pathToLatestSerialisedJSONArray = pathForLatestSerialisedJSONArray;
// System.out.println("Fetched fresh data from database and Serialized JSONArray");
// }
//
// /**
// *
// * @param databaseName
// * @param jsonArrayD
// * @param gowallaDataFolder
// * @return
// */
// public LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createTimelines(String databaseName,
// SerializableJSONArray jsonArrayD, String gowallaDataFolder)
// {
// long dt = System.currentTimeMillis();
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal = null;
//
// if (databaseName.equals("gowalla1"))
// {
// // $$String gowallaDataFolder = "./dataToRead/Feb23/DatabaseCreatedMerged/";// DatabaseCreatedMerged/";//
// // Feb2/DatabaseCreated/";
// System.out.println("gowallaDataFolder = " + gowallaDataFolder);
// // $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/DatabaseCreated/";
// // $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov29/DatabaseCreation/";
// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinData = (LinkedHashMap<String,
// TreeMap<Timestamp, CheckinEntry>>) Serializer
// .kryoDeSerializeThis(gowallaDataFolder + "mapForAllCheckinData.kryo");
// // "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllCheckinData.kryo");
// LinkedHashMap<String, UserGowalla> mapForAllUserData = (LinkedHashMap<String, UserGowalla>) Serializer
// .kryoDeSerializeThis(gowallaDataFolder + "mapForAllUserData.kryo");
// // "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllUserData.kryo");
// LinkedHashMap<Integer, LocationGowalla> mapForAllLocationData = (LinkedHashMap<Integer, LocationGowalla>) Serializer
// .kryoDeSerializeThis(gowallaDataFolder + "mapForAllLocationData.kryo");
//
// // "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllLocationData.kryo");
//
// System.out.println("before creating timelines while having deserialised objects in memory\n"
// + PerformanceAnalytics.getHeapInformation());
//
// usersDayTimelinesOriginal = TimelineUtils.createUserTimelinesFromCheckinEntriesGowalla(mapForAllCheckinData,
// mapForAllLocationData);
//
// }
// else
// {
// ArrayList<ActivityObject> allActivityEvents = UtilityBelt
// .createActivityObjectsFromJsonArray(jsonArrayD.getJSONArray());
// // UtilityBelt.traverseActivityEvents(allActivityEvents); // Debugging Check: OK
// usersDayTimelinesOriginal = TimelineUtils.createUserTimelinesFromActivityObjects(allActivityEvents);
// }
//
// System.out
// .println("userTimelines.size()=" + usersDayTimelinesOriginal.size() + " for database: " + databaseName);
// long lt = System.currentTimeMillis();
// System.out.println("timelines creation takes " + (lt - dt) / 1000 + " secs");
//
// return usersDayTimelinesOriginal;
// }
//
// /**
// * For Gowalla data:
// * <ol type="1">
// * <li>removes days with less than 10 acts per day</li>
// * <li>removes users with less than 50 days</li>
// * </ol>
// * For all data, removes day timelines:
// * <ol type="1">
// * <li>with no valid activity,</li>
// * <li>with <=1 distinct valid activity, and</li>
// * <li>the weekend day timelines.</li>
// * </ol>
// * For Gowalla data:
// * <ol type="1">
// * <li>again removes users with less than 50 days (this is after cleaning)</li>
// *
// * @param databaseName
// * @param usersDayTimelinesOriginal
// * @param writeToFile
// * @return
// */
// public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceAndCleanTimelines2(String databaseName,
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile)
// {
// if (databaseName.equals("gowalla1"))
// {
// usersDayTimelinesOriginal = reduceGowallaTimelines3(Constant.getDatabaseName(), usersDayTimelinesOriginal,
// true, false, 10, 7, 500);
// }
//
// ///// clean timelines
// System.out.println(
// "\n-- Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day
// timelines.");
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = TimelineUtils
// .cleanDayTimelines(usersDayTimelinesOriginal);
// if (writeToFile)
// {// WritingToFile.writeUsersDayTimelinesSameFile(usersCleanedDayTimelines, "usersCleanedDayTimelines", false,
// // false, false,"GowallaUserDayTimelinesCleaned.csv");// users
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// "usersCleanedDayTimelines", "GowallaPerUserDayNumOfActsCleaned.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// Constant.getCommonPath() + "NumOfDaysPerUserCleaned.csv");
// }
// System.out.println("Num of users cleaned = " + usersCleanedDayTimelines.size());
// ///
//
// if (databaseName.equals("gowalla1"))
// {
// ///// again remove users with less than 2 days (these are the clean days)
// // if (false)// disabled
// {
// System.out.println("\n--again remove users with less than 2 day (these are the clean days)");
// usersCleanedDayTimelines = TimelineUtils.removeUsersWithLessDays(usersCleanedDayTimelines, 2,
// Constant.getCommonPath() + "removeCleanedDayTimelinesWithLessThan2DaysLog.csv");
// }
// }
//
// if (writeToFile)
// {// Writing user day timelines. big file ~ 17.3GB
// // WritingToFile.writeUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// // "usersCleanedDayTimelinesReduced3",false, false, false, "GowallaUserDayTimelinesCleanedReduced3.csv");//
//
// // write a subset of timelines
// Map<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelinesSampled = usersCleanedDayTimelines
// .entrySet().stream().limit(2).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
//
// WritingToFile.writeUsersDayTimelinesSameFile(
// new LinkedHashMap<String, LinkedHashMap<Date, Timeline>>(usersCleanedDayTimelinesSampled),
// "usersCleanedDayTimelinesReduced3First2UsersOnly", false, false, false,
// "GowallaUserDayTimelinesCleanedReduced3First2UsersOnly.csv");// users
//
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// "usersCleanedDayTimelinesReduced3", "GowallaPerUserDayNumOfActsCleanedReduced3.csv");// us
//
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// Constant.getCommonPath() + "NumOfDaysPerUserCleanedReduced3.csv");
//
// WritingToFile.writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// "usersCleanedDayTimelinesReduced3", "GowallaPerUserDayNumOfDistinctValidActsCleanedReduced3.csv");// us
//
// System.out.println("Num of users cleaned reduced3 = " + usersCleanedDayTimelines.size());
// }
// /// temp Start
// ////////// removed
// // System.out.println("\n-- removing users with GT553");
// // LinkedHashMap<String, LinkedHashMap<Date, Timeline>> tempUsersCleanedDayTimelines = new LinkedHashMap<>();
// // for (Entry<String, LinkedHashMap<Date, Timeline>> uEntry : usersCleanedDayTimelines.entrySet())
// // {
// // if (!DomainConstants.isGowallaUserIDsWithGT553MaxActsPerDay(Integer.valueOf(uEntry.getKey())))
// // {
// // tempUsersCleanedDayTimelines.put(uEntry.getKey(), uEntry.getValue());
// // }
// // }
// //
// // if (writeToFile)
// // {
// // WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(tempUsersCleanedDayTimelines,
// // "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced4.csv");// us
// // WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(tempUsersCleanedDayTimelines,
// // Constant.getCommonPath() + "NumOfDaysPerUserReduced4.csv");
// // // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // // false, false, false,"GowallaUserDayTimelinesReduced2.csv");// users
// // }
// // System.out.println("Num of users reduced4 = " + tempUsersCleanedDayTimelines.size());
// //////////
// // temp End
//
// return usersCleanedDayTimelines;
// }
//
// /**
// * For Gowalla data:
// * <ol type="1">
// * <li>removes days with less than 10 acts per day</li>
// * <li>removes users with less than 50 days</li>
// * </ol>
// * For all data, removes day timelines:
// * <ol type="1">
// * <li>with no valid activity,</li>
// * <li>with <=1 distinct valid activity, and</li>
// * <li>the weekend day timelines.</li>
// * </ol>
// * For Gowalla data:
// * <ol type="1">
// * <li>again removes users with less than 50 days (this is after cleaning)</li>
// *
// * @param databaseName
// * @param usersDayTimelinesOriginal
// * @param writeToFile
// * @return
// */
// public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceAndCleanTimelines(String databaseName,
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile)
// {
// if (databaseName.equals("gowalla1"))
// {
// usersDayTimelinesOriginal = reduceGowallaTimelines(Constant.getDatabaseName(), usersDayTimelinesOriginal,
// true);
// }
//
// ///// clean timelines
// System.out.println(
// "\n-- Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day
// timelines.");
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = TimelineUtils
// .cleanDayTimelines(usersDayTimelinesOriginal);
//
// if (writeToFile)
// {// WritingToFile.writeUsersDayTimelinesSameFile(usersCleanedDayTimelines, "usersCleanedDayTimelines", false,
// // false, false,"GowallaUserDayTimelinesCleaned.csv");// users
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// "usersCleanedDayTimelines", "GowallaPerUserDayNumOfActsCleaned.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// Constant.getCommonPath() + "NumOfDaysPerUserCleaned.csv");
// }
// System.out.println("Num of users cleaned = " + usersCleanedDayTimelines.size());
// ///
//
// if (databaseName.equals("gowalla1"))
// {
// ///// again remove users with less than 50 days (these are the clean days)
// System.out.println("\n--again remove users with less than 50 days (these are the clean days)");
// usersCleanedDayTimelines = TimelineUtils.removeUsersWithLessDays(usersCleanedDayTimelines, 50,
// Constant.getCommonPath() + "removeCleanedDayTimelinesWithLessThan50DaysLog.csv");
// }
//
// if (writeToFile)
// {// Writing user day timelines. big file ~ 17.3GB
// // WritingToFile.writeUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// // "usersCleanedDayTimelinesReduced3",false, false, false, "GowallaUserDayTimelinesCleanedReduced3.csv");//
//
// // write a subset of timelines
// Map<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelinesSampled = usersCleanedDayTimelines
// .entrySet().stream().limit(2).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
//
// WritingToFile.writeUsersDayTimelinesSameFile(
// new LinkedHashMap<String, LinkedHashMap<Date, Timeline>>(usersCleanedDayTimelinesSampled),
// "usersCleanedDayTimelinesReduced3First2UsersOnly", false, false, false,
// "GowallaUserDayTimelinesCleanedReduced3First2UsersOnly.csv");// users
//
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// "usersCleanedDayTimelinesReduced3", "GowallaPerUserDayNumOfActsCleanedReduced3.csv");// us
//
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// Constant.getCommonPath() + "NumOfDaysPerUserCleanedReduced3.csv");
//
// WritingToFile.writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
// "usersCleanedDayTimelinesReduced3", "GowallaPerUserDayNumOfDistinctValidActsCleanedReduced3.csv");// us
//
// System.out.println("Num of users cleaned reduced3 = " + usersCleanedDayTimelines.size());
//
// /// temp Start
// ////////// removed users with GT553 acts per day
// // System.out.println("\n-- removing users with GT553");
// // LinkedHashMap<String, LinkedHashMap<Date, Timeline>> tempUsersCleanedDayTimelines = new
// // LinkedHashMap<>();
// // for (Entry<String, LinkedHashMap<Date, Timeline>> uEntry : usersCleanedDayTimelines.entrySet())
// // {
// // if (!DomainConstants.isGowallaUserIDsWithGT553MaxActsPerDay(Integer.valueOf(uEntry.getKey())))
// // {
// // tempUsersCleanedDayTimelines.put(uEntry.getKey(), uEntry.getValue());
// // }
// // }
// //
// // if (writeToFile)
// // {
// // WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(tempUsersCleanedDayTimelines,
// // "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced4.csv");// us
// // WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(tempUsersCleanedDayTimelines,
// // Constant.getCommonPath() + "NumOfDaysPerUserReduced4.csv");
// // // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // // false, false, false,"GowallaUserDayTimelinesReduced2.csv");// users
// // }
// // System.out.println("Num of users reduced4 = " + tempUsersCleanedDayTimelines.size());
// //////////
// // temp End
// }
// return usersCleanedDayTimelines;
// }
//
// //
// /**
// * removed days with less than actsPerDayLowerLimit acts per day
// * <p>
// * removed days with greater than actsPerDayUpperLimit acts per day
// * <p>
// * removed users with less than numOfSuchDaysLowerLimit days
// *
// *
// * @param databaseName
// * @param usersDayTimelinesOriginal
// * @param writeToFile
// * @param writeLogs
// * @param actsPerDayLowerLimit
// * @param numOfSuchDaysLowerLimit
// * @param actsPerDayUpperLimit
// * @return
// */
// public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceGowallaTimelines4(String databaseName,
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile,
// boolean writeLogs, int actsPerDayUpperLimit)
// {
// System.out.println("Inside reduceGowallaTimelines3");
//
// if (databaseName.equals("gowalla1") == false)
// {
// String msg = PopUps.getTracedErrorMsg(
// "Error in reduceGowallaTimelines2(): should not be called for databases other than gowalla1. Called for database: "
// + databaseName);
// System.err.println(msg);
// return null;
// }
//
// else
// {
// // Originally received timelines
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActs.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUser.csv");
// }
// System.out.println("Num of users = " + usersDayTimelinesOriginal.size());
//
// ////////// removed days with greater than actsPerDayUpperLimit acts per day
// System.out.println("\n-- removing days with greater than " + actsPerDayUpperLimit + " acts per day");
// usersDayTimelinesOriginal = TimelineUtils
// .removeDayTimelinesWithGreaterAct(
// usersDayTimelinesOriginal, actsPerDayUpperLimit, Constant.getCommonPath()
// + "removeDayTimelinesWithGreaterThan" + actsPerDayUpperLimit + "ActLog.csv",
// writeLogs);
//
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced1_2.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUserReduced1_2.csv");
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false, "GowallaUserDayTimelinesReduced1.csv");// users
// }
// System.out.println("Num of users reduced1_2 = " + usersDayTimelinesOriginal.size());
// //////////
//
// return usersDayTimelinesOriginal;
// }
// }
//
// //
// /**
// * removed days with less than actsPerDayLowerLimit acts per day
// * <p>
// * removed days with greater than actsPerDayUpperLimit acts per day
// * <p>
// * removed users with less than numOfSuchDaysLowerLimit days
// *
// *
// * @param databaseName
// * @param usersDayTimelinesOriginal
// * @param writeToFile
// * @param writeLogs
// * @param actsPerDayLowerLimit
// * @param numOfSuchDaysLowerLimit
// * @param actsPerDayUpperLimit
// * @return
// */
// public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceGowallaTimelines3(String databaseName,
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile,
// boolean writeLogs, int actsPerDayLowerLimit, int numOfSuchDaysLowerLimit, int actsPerDayUpperLimit)
// {
// System.out.println("Inside reduceGowallaTimelines3");
//
// if (databaseName.equals("gowalla1") == false)
// {
// String msg = PopUps.getTracedErrorMsg(
// "Error in reduceGowallaTimelines2(): should not be called for databases other than gowalla1. Called for database: "
// + databaseName);
// System.err.println(msg);
// return null;
// }
//
// else
// {
// // Originally received timelines
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActs.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUser.csv");
// }
// System.out.println("Num of users = " + usersDayTimelinesOriginal.size());
//
// ////////// removed days with less than actsPerDayLowerLimit acts per day
// System.out.println("\n-- removing days with less than " + actsPerDayLowerLimit + " acts per day");
// usersDayTimelinesOriginal = TimelineUtils.removeDayTimelinesWithLessAct(usersDayTimelinesOriginal,
// actsPerDayLowerLimit,
// Constant.getCommonPath() + "removeDayTimelinesWithLessThan" + actsPerDayLowerLimit + "ActLog.csv",
// writeLogs);
//
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced1.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUserReduced1.csv");
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false, "GowallaUserDayTimelinesReduced1.csv");// users
// }
// System.out.println("Num of users reduced1 = " + usersDayTimelinesOriginal.size());
// //////////
//
// ////////// removed days with greater than actsPerDayUpperLimit acts per day
// System.out.println("\n-- removing days with greater than " + actsPerDayUpperLimit + " acts per day");
// usersDayTimelinesOriginal = TimelineUtils
// .removeDayTimelinesWithGreaterAct(
// usersDayTimelinesOriginal, actsPerDayUpperLimit, Constant.getCommonPath()
// + "removeDayTimelinesWithGreaterThan" + actsPerDayUpperLimit + "ActLog.csv",
// writeLogs);
//
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced1_2.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUserReduced1_2.csv");
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false, "GowallaUserDayTimelinesReduced1.csv");// users
// }
// System.out.println("Num of users reduced1_2 = " + usersDayTimelinesOriginal.size());
// //////////
//
// ////////// removed users with less than numOfSuchDaysLowerLimit days
// System.out.println("\n-- removing users with less than " + numOfSuchDaysLowerLimit + " days");
// usersDayTimelinesOriginal = TimelineUtils.removeUsersWithLessDays(usersDayTimelinesOriginal,
// numOfSuchDaysLowerLimit, Constant.getCommonPath() + "removeDayTimelinesWithLessThan"
// + numOfSuchDaysLowerLimit + "DaysLog.csv");
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced2.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUserReduced2.csv");
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false,"GowallaUserDayTimelinesReduced2.csv");// users
// }
// System.out.println("Num of users reduced2 = " + usersDayTimelinesOriginal.size());
// //////////
//
// return usersDayTimelinesOriginal;
// }
// }
//
// //
// /**
// * removed days with less than actsPerDayLowerLimit acts per day
// * <p>
// * removed users with less than numOfSuchDaysLowerLimit days
// *
// * @param databaseName
// * @param usersDayTimelinesOriginal
// * @param writeToFile
// * @param writeLogs
// * @param actsPerDayLowerLimit
// * @param numOfSuchDaysLowerLimit
// * @return
// */
// public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceGowallaTimelines2(String databaseName,
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile,
// boolean writeLogs, int actsPerDayLowerLimit, int numOfSuchDaysLowerLimit)
// {
// System.out.println("Inside reduceGowallaTimelines2");
//
// if (databaseName.equals("gowalla1") == false)
// {
// String msg = PopUps.getTracedErrorMsg(
// "Error in reduceGowallaTimelines2(): should not be called for databases other than gowalla1. Called for database: "
// + databaseName);
// System.err.println(msg);
// return null;
// }
//
// else
// {
// // Originally received timelines
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActs.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUser.csv");
// }
// System.out.println("Num of users = " + usersDayTimelinesOriginal.size());
//
// ////////// removed days with less than actsPerDayLowerLimit acts per day
// System.out.println("\n-- removing days with less than " + actsPerDayLowerLimit + " acts per day");
// usersDayTimelinesOriginal = TimelineUtils.removeDayTimelinesWithLessAct(usersDayTimelinesOriginal,
// actsPerDayLowerLimit,
// Constant.getCommonPath() + "removeDayTimelinesWithLessThan" + actsPerDayLowerLimit + "ActLog.csv",
// writeLogs);
//
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced1.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUserReduced1.csv");
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false, "GowallaUserDayTimelinesReduced1.csv");// users
// }
// System.out.println("Num of users reduced1 = " + usersDayTimelinesOriginal.size());
// //////////
//
// ////////// removed users with less than numOfSuchDaysLowerLimit days
// System.out.println("\n-- removing users with less than " + numOfSuchDaysLowerLimit + " days");
// usersDayTimelinesOriginal = TimelineUtils.removeUsersWithLessDays(usersDayTimelinesOriginal,
// numOfSuchDaysLowerLimit, Constant.getCommonPath() + "removeDayTimelinesWithLessThan"
// + numOfSuchDaysLowerLimit + "DaysLog.csv");
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced2.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUserReduced2.csv");
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false,"GowallaUserDayTimelinesReduced2.csv");// users
// }
// System.out.println("Num of users reduced2 = " + usersDayTimelinesOriginal.size());
// //////////
//
// return usersDayTimelinesOriginal;
// }
// }
//
// //
// /**
// * removed days with less than 10 acts per day
// * <p>
// * removed users with less than 50 days
// *
// * @param databaseName
// * @param usersDayTimelinesOriginal
// * @param writeToFile
// * @return
// */
// public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceGowallaTimelines(String databaseName,
// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile)
// {
// System.out.println("Inside reduceGowallaTimelines");
//
// if (databaseName.equals("gowalla1") == false)
// {
// String msg = PopUps.getTracedErrorMsg(
// "Error in reduceTimelines(): should not be called for databases other than gowalla1. Called for database: "
// + databaseName);
// System.err.println(msg);
// return null;
// }
//
// else
// {
// // Originally received timelines
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActs.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUser.csv");
// }
// System.out.println("Num of users = " + usersDayTimelinesOriginal.size());
//
// ////////// removed days with less than 10 acts per day
// System.out.println("\n-- removing days with less than 10 acts per day");
// usersDayTimelinesOriginal = TimelineUtils.removeDayTimelinesWithLessAct(usersDayTimelinesOriginal, 10,
// Constant.getCommonPath() + "removeDayTimelinesWithLessThan10ActLog.csv", true);
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced1.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUserReduced1.csv");
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false, "GowallaUserDayTimelinesReduced1.csv");// users
// }
// System.out.println("Num of users reduced1 = " + usersDayTimelinesOriginal.size());
// //////////
//
// ////////// removed users with less than 50 days
// System.out.println("\n-- removing users with less than 50 days");
// usersDayTimelinesOriginal = TimelineUtils.removeUsersWithLessDays(usersDayTimelinesOriginal, 50,
// Constant.getCommonPath() + "removeDayTimelinesWithLessThan50DaysLog.csv");
// if (writeToFile)
// {
// WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// "usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced2.csv");// us
// WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
// Constant.getCommonPath() + "NumOfDaysPerUserReduced2.csv");
// // WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
// // false, false, false,"GowallaUserDayTimelinesReduced2.csv");// users
// }
// System.out.println("Num of users reduced2 = " + usersDayTimelinesOriginal.size());
// //////////
//
// return usersDayTimelinesOriginal;
// }
// }
//
// }