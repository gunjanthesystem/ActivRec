package org.activity.controller;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.activity.evaluation.RecommendationTestsMU;
import org.activity.io.SerializableJSONArray;
import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.CheckinEntry;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.UserDayTimeline;
import org.activity.objects.UserGowalla;
import org.activity.ui.PopUps;
import org.activity.util.ConnectDatabase;
import org.activity.util.Constant;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

/**
 * This class is used for recommending without the web interface (no web server) Note: to run these experiments proper parameters must be set in class org.activity.util.Constant
 * and this class
 * 
 * @author gunjan
 */
public class ControllerWithoutServer
{
	// **** Parameters to set **** DO NOT CHANGE ****//
	// static final boolean toSerializeJSONArray = true, toDeSerializeJSONArray = true, toCreateTimelines = true,
	// toSerializeTimelines =
	// true,
	// toDeSerializeTimelines = true;
	
	// public static void main(String[] args)
	public ControllerWithoutServer()
	{
		try
		{
			System.out.println("Starting ControllerWithoutServer>>>>");
			LocalDateTime currentDateTime = LocalDateTime.now();
			TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 21, 2016
			Constant.setDefaultTimeZone("UTC");
			String pathToLatestSerialisedJSONArray = "", pathForLatestSerialisedJSONArray = "", pathToLatestSerialisedTimelines = "",
					pathForLatestSerialisedTimelines = "", commonPath = "";
			System.out.println("Running experiments for database: " + Constant.getDatabaseName());// .DATABASE_NAME);
			
			switch (Constant.getDatabaseName())
			{
				case "gowalla1":
					pathToLatestSerialisedJSONArray = "";
					pathForLatestSerialisedJSONArray =
							"" + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + "obj";
					
					pathToLatestSerialisedTimelines =
							"/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/GowallaUserDayTimelines13Sep2016.kryo";// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep15DatabaseGenerationJava/GowallaUserDayTimelines13Sep2016.kryo";
					pathForLatestSerialisedTimelines =
							"" + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + ".lmap";
					
					commonPath = "/run/media/gunjan/BoX1/GowallaSpaceSpaceSpace/GowallaDataWorksSep19/";// "/run/media/gunjan/BoX2/GowallaSpaceSpace/GowallaDataWorksSep16/";
					break;
				
				case "geolife1":
					pathToLatestSerialisedJSONArray = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArrayAPR21obj";
					// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArrayMAY27obj";
					// GeolifeJSONArrayFeb13.obj";
					pathForLatestSerialisedJSONArray = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArray"
							+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + "obj";
					
					// $$UMAP submission
					// $$pathToLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesJUN18.lmap";//
					// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesJUN15.lmap";//
					// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data Works/UserTimelinesAPR15.lmap";//
					// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR10.lmap";//
					// UserTimelinesFeb13.lmap";
					
					// After UMAP submission 19th April 2016
					pathToLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR21.lmap";
					
					pathForLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelines"
							+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + ".lmap";
					commonPath = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/";// version 3 based on rank score
																						// function
					break;
				
				case "dcu_data_2":
					pathToLatestSerialisedJSONArray =
							"/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/JSONArrayOct29.obj";
					pathForLatestSerialisedJSONArray =
							"/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/JSONArray"
									+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + "obj";
					
					pathToLatestSerialisedTimelines =
							"/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/DCUUserTimelinesJUN19.lmap"; // "/run/media/gunjan/OS/Users/gunjan/Documents/DCU
																																		// Data
																																		// Works/WorkingSet7July/DCUUserTimelinesJUN15.lmap";//
																																		// "/run/media/gunjan/OS/Users/gunjan/Documents/DCU
																																		// Data
																																		// Works/WorkingSet7July/DCUUserTimelinesMAY7.lmap";//
																																		// DCUUserTimelinesOct29.lmap";
					pathForLatestSerialisedTimelines =
							"/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/DCUUserTimelines"
									+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + ".lmap";
					
					commonPath = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/";
					break;
				
				default:
					System.err.println("Error: unrecognised database name");
					break;
			}
			
			// new ConnectDatabase(Constant.getDatabaseName()); // all method and variable in this class are static
			// new Constant(commonPath, Constant.getDatabaseName());
			
			/*
			 * $ Disabled for Gowalla dataset for now// ConnectDatabase.initialise(Constant.getDatabaseName()); // all method and variable in this class are static
			 */
			
			Constant.initialise(commonPath, Constant.getDatabaseName());
			
			long bt = System.currentTimeMillis();
			if (Constant.toSerializeJSONArray)
			{
				String selectedAttributes = "activity_fact_table.User_ID," + // date_dimension_table.Date,time_dimension_table.Start_Time,"
																				// +
						" activity_fact_table.Activity_ID, activity_fact_table.Time_ID, activity_fact_table.Location_ID, activity_fact_table.Date_ID";
				
				String orderByString = "activity_fact_table.User_ID, date_dimension_table.Date, time_dimension_table.Start_Time";
				// String whereQueryString =
				// "where activity_dimension_table.Activity_Name!='Not Available' && activity_dimension_table.Activity_Name!='Unknown'";
				
				String whereQueryString = "";
				if (Constant.getDatabaseName().equals("geolife1"))
				{
					whereQueryString = "where " + /*
													 * "activity_fact_table.User_ID in ( 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 ) && " +
													 */"activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY1
							+ "' && activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY2 + "'";// for faster
				} // "";// "where activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY1 +
					// "' && activity_dimension_table.Activity_Name!='" +
					// Constant.INVALID_ACTIVITY2 + "'";
				
				SerializableJSONArray jsonArray = new SerializableJSONArray(
						ConnectDatabase.getJSONArrayOfDataTable(selectedAttributes, whereQueryString, orderByString));
				WritingToFile.writeToNewFile(jsonArray.toString(), Constant.getCommonPath() + "JSONArray.csv");
				// System.out.println(jsonArray.toString());
				Serializer.serializeThis(jsonArray, pathForLatestSerialisedJSONArray);
				pathToLatestSerialisedJSONArray = pathForLatestSerialisedJSONArray;
				System.out.println("Fetched fresh data from database and Serialized JSONArray");
			}
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			SerializableJSONArray jsonArrayD = null;
			long dt = System.currentTimeMillis();
			if (Constant.toDeSerializeJSONArray)
			{
				jsonArrayD = (SerializableJSONArray) Serializer.deSerializeThis(pathToLatestSerialisedJSONArray);// GeolifeJSONArrayDec24_2.obj");
				dt = System.currentTimeMillis();
				System.out.println("Deserialiastion takes " + (dt - bt) / 1000 + " secs");
				System.out.println("Deserialized JSONArray");
			}
			// // System.out.println(jsonArray.getJSONArray().toString() == jsonArrayD.getJSONArray().toString());
			// // System.out.println(jsonArray.getJSONArray().toString().length());
			// // System.out.println("---------");
			// // System.out.println(jsonArray.getJSONArray().toString().length());
			//
			//
			// // System.out.println(StringUtils.difference(jsonArray.getJSONArray().toString(),
			// jsonArrayD.getJSONArray().toString()));
			// System.out.println("ajooba");1
			// //System.out.println(jsonArray.toString());
			//
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOriginal = null;// new LinkedHashMap<String,
																											// LinkedHashMap<Date,
																											// UserDayTimeline>>();
			if (Constant.toCreateTimelines)
			{
				
				if (Constant.getDatabaseName().equals("gowalla1"))
				{
					LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinData =
							(LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>) Serializer.kryoDeSerializeThis(
									"/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllCheckinData.kryo");
					LinkedHashMap<String, UserGowalla> mapForAllUserData =
							(LinkedHashMap<String, UserGowalla>) Serializer.kryoDeSerializeThis(
									"/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllUserData.kryo");
					
					LinkedHashMap<String, LocationGowalla> mapForAllLocationData =
							(LinkedHashMap<String, LocationGowalla>) Serializer.kryoDeSerializeThis(
									"/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllLocationData.kryo");
					
					usersDayTimelinesOriginal =
							TimelineUtils.createUserTimelinesFromCheckinEntriesGowalla(mapForAllCheckinData, mapForAllLocationData);
				}
				else
				{
					ArrayList<ActivityObject> allActivityEvents = UtilityBelt.createActivityObjectsFromJsonArray(jsonArrayD.getJSONArray());
					// UtilityBelt.traverseActivityEvents(allActivityEvents); // Debugging Check: OK
					
					usersDayTimelinesOriginal = TimelineUtils.createUserTimelinesFromActivityObjects(allActivityEvents);
					
				}
				
				System.out.println("userTimelines.size()=" + usersDayTimelinesOriginal.size());
				long lt = System.currentTimeMillis();
				System.out.println("timelines creation takes " + (lt - dt) / 1000 + " secs");
			}
			// //to improve repeat execution performance...serialising
			if (Constant.toSerializeTimelines)
			{
				Serializer.serializeThis(usersDayTimelinesOriginal, pathForLatestSerialisedTimelines);
				pathToLatestSerialisedTimelines = pathForLatestSerialisedTimelines;
				System.out.println("Serialized Timelines");
			}
			//
			// // /////////////////////////////////////
			if (Constant.toDeSerializeTimelines)
			{
				
				if (Constant.getDatabaseName().equals("gowalla1"))
				{
					usersDayTimelinesOriginal = (LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>) Serializer
							.kryoDeSerializeThis(pathToLatestSerialisedTimelines);
				}
				else
				{
					usersDayTimelinesOriginal = (LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>) Serializer
							.deSerializeThis(pathToLatestSerialisedTimelines);
				}
				System.out.println("deserialised userTimelines.size()=" + usersDayTimelinesOriginal.size());
				System.out.println("Deserialized Timelines");
			}
			long ct = System.currentTimeMillis();
			
			if (!Constant.checkAllParametersSet())
			{
				System.err.println("All essential paramaters in Constant not set. Exiting");
				PopUps.showError("All essential paramaters in Constant not set. Exiting");
				System.exit(-162);
			}
			
			Pair<Boolean, String> hasDuplicateDates = TimelineUtils.hasDuplicateDates(usersDayTimelinesOriginal);
			
			if (hasDuplicateDates.getFirst())
			{
				System.out.println("Alert!Alert! hasDuplicateDates.  for users:" + hasDuplicateDates.getSecond().toString());
			}
			else
			{
				System.out.println("Thank God, no duplicate dates.");
			}
			//////////// for Gowalla start
			// WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal", false, false, false,
			// "GowallaUserDayTimelines.csv");// users
			
			WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
					"GowallaPerUserDayNumOfActs.csv");// us
			WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
					Constant.getCommonPath() + "NumOfDaysPerUser.csv");
			
			System.out.println("Num of users = " + usersDayTimelinesOriginal.size());
			
			///// removed days with less than 10 acts per day
			usersDayTimelinesOriginal = TimelineUtils.removeDayTimelinesWithLessAct(usersDayTimelinesOriginal, 10,
					Constant.getCommonPath() + "removeDayTimelinesWithLessThan10ActLog.csv");
			
			// WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal", false, false, false,
			// "GowallaUserDayTimelinesReduced1.csv");// users
			WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
					"GowallaPerUserDayNumOfActsReduced1.csv");// us
			WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
					Constant.getCommonPath() + "NumOfDaysPerUserReduced1.csv");
			System.out.println("Num of users reduced1 = " + usersDayTimelinesOriginal.size());
			
			///// removed users with less than 50 days
			usersDayTimelinesOriginal = TimelineUtils.removeUsersWithLessDays(usersDayTimelinesOriginal, 50,
					Constant.getCommonPath() + "removeDayTimelinesWithLessThan50DaysLog.csv");
			
			// WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal", false, false, false,
			// "GowallaUserDayTimelinesReduced2.csv");// users
			WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
					"GowallaPerUserDayNumOfActsReduced2.csv");// us
			WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
					Constant.getCommonPath() + "NumOfDaysPerUserReduced2.csv");
			System.out.println("Num of users reduced2 = " + usersDayTimelinesOriginal.size());
			
			///// clean timelines
			/**
			 * Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day timelines.
			 * (removeDayTimelinesWithNoValidAct(),removeDayTimelinesWithOneOrLessDistinctValidAct(),removeWeekendDayTimelines() <b><font color="red">Note: this does not expunges
			 * all invalid activity objects from the timeline</font></b>
			 **/
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersCleanedDayTimelines =
					TimelineUtils.cleanDayTimelines(usersDayTimelinesOriginal);
			// WritingToFile.writeUsersDayTimelinesSameFile(usersCleanedDayTimelines, "usersCleanedDayTimelines", false, false, false,
			// "GowallaUserDayTimelinesCleaned.csv");// users
			WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines, "usersCleanedDayTimelines",
					"GowallaPerUserDayNumOfActsCleaned.csv");// us
			WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
					Constant.getCommonPath() + "NumOfDaysPerUserCleaned.csv");
			System.out.println("Num of users cleaned = " + usersCleanedDayTimelines.size());
			
			///// again remove users with less than 50 days (there are the clean days)
			usersCleanedDayTimelines = TimelineUtils.removeUsersWithLessDays(usersCleanedDayTimelines, 50,
					Constant.getCommonPath() + "removeCleanedDayTimelinesWithLessThan50DaysLog.csv");
			
			WritingToFile.writeUsersDayTimelinesSameFile(usersCleanedDayTimelines, "usersCleanedDayTimelinesReduced3", false, false, false,
					"GowallaUserDayTimelinesCleanedReduced3.csv");// users
			WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines, "usersCleanedDayTimelinesReduced3",
					"GowallaPerUserDayNumOfActsCleanedReduced3.csv");// us
			WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
					Constant.getCommonPath() + "NumOfDaysPerUserCleanedReduced3.csv");
			System.out.println("Num of users cleaned reduced3  = " + usersCleanedDayTimelines.size());
			//////////// for Gowalla end
			
			// WritingToFile.writeUsersDayTimelines(usersDayTimelinesOriginal, "usersDayTimelinesOriginal", true, true, true);// users
			// $$ WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal", true, true, true,
			// $$ "AllInSameFileApr21UncorrrectedTZ.csv");// users
			
			// //Start of disabled on 15 Sep 2016 _1
			// String serialisedTimelines1 = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR21.lmap";
			// String serialisedTimelines2 = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesJUN18.lmap";
			// // CompareTimelines(serialisedTimelines1, serialisedTimelines2);
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersCleanedRearrangedDayTimelines =
			// new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
			// usersCleanedRearrangedDayTimelines = TimelineUtilities.cleanDayTimelines(usersDayTimelinesOriginal);
			// usersCleanedRearrangedDayTimelines = TimelineUtilities.rearrangeDayTimelinesOrderForDataset(usersCleanedRearrangedDayTimelines);
			// //end of disabled on 15 Sep 2016 _1
			
			// $$ WritingToFile.writeUsersDayTimelines(usersDayTimelinesOriginal, "usersCleanedRearrangedDayTimelines", true, true, true);// users
			
			// UtilityBelt.traverseUserTimelines(userTimelines); // Debugging Check: OK Cheked again with timestamp: OK
			// UtilityBelt.traverseActivityEvents(allActivityEvents); // Debugging Check: OK
			
			// for actual recommendation
			// RecommendationMaster recommendationMaster=new RecommendationMaster(userTimelines,dateAtRecomm,timeAtRecomm,weekDayAtRecomm,userAtRecomm);
			
			// for testing
			// RecommendationTestsBaseClosestTime recommendationsTest=new RecommendationTestsBaseClosestTime(userTimelines);//,userAtRecomm);
			// RecommendationTestsDayWise recommendationsTest=new RecommendationTestsDayWise(userTimelines);//,userAtRecomm);
			
			/** CURRENT: To run the recommendation experiments **/
			// $$RecommendationTestsDayWise2FasterJan2016 recommendationsTest = new RecommendationTestsDayWise2FasterJan2016(usersDayTimelinesOriginal);
			// $$ disabled on 15 Sep 2016RecommendationTestsMU recommendationsTest = new RecommendationTestsMU(usersDayTimelinesOriginal);
			RecommendationTestsMU recommendationsTest = new RecommendationTestsMU(usersCleanedDayTimelines);
			
			/**** CURRENT END ****/
			
			/** To get some stats on the generated timelines **/
			// TimelineStats.timelineStatsController(usersTimelines);
			// TimelineStats.timelineStatsController(usersDayTimelines);
			// $$ TimelineStats.timelineStatsController(usersDayTimelinesOriginal);
			
			/** Clustering timelines using weka and after feature extraction **/
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersCleanedRearrangedDayTimelines = new
			// LinkedHashMap<String,
			// LinkedHashMap<Date,
			// UserDayTimeline>>();
			// usersCleanedRearrangedDayTimelines = UtilityBelt.cleanDayTimelines(usersDayTimelinesOriginal);
			// usersCleanedRearrangedDayTimelines =
			// UtilityBelt.rearrangeDayTimelinesOrderForDataset(usersCleanedRearrangedDayTimelines);
			
			/** CURRENT **/
			// $$ TimelineWEKAClusteringController clustering = new TimelineWEKAClusteringController(usersCleanedRearrangedDayTimelines, null);
			/** END OF CURRENT **/
			
			/** CURRENT **/
			// $$TimelineStats.timelineStatsController(usersCleanedRearrangedDayTimelines);
			/** END OF CURRENT **/
			
			/** To experiment with distance **/
			// ExperimentDistances ed = new ExperimentDistances(usersTimelines);
			
			// RecommendationMasterDayWise2Faster*/
			
			// Start of not needed any more as we are doing leave one out cross validation
			// Pair<ArrayList<String>, ArrayList<String>> trainingTestUsers = trainingTestSplit.getTrainingTestUsers();
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> onlyTrainingUsersTimelines =
			// UtilityBelt.expungeUserDayTimelinesByUser(usersDayTimelines,
			// trainingTestUsers.getSecond());
			// TimelineWEKAClusteringController clustering = new
			// TimelineWEKAClusteringController(onlyTrainingUsersTimelines);
			// TimelineWEKAClusteringController clustering = new TimelineWEKAClusteringController(usersDayTimelines,
			// trainingTestUsers);
			// End of not needed any more as we are doing leave one out cross validation
			
			long et = System.currentTimeMillis();
			
			if (Constant.USEPOOLED)
			{
				ConnectDatabase.destroyPooledConnection();
			}
			
			System.out.println("This experiment took " + ((et - ct) / 1000) + " seconds");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	// private void CompareTimelines(String serialisedTimelines1, String serialisedTimelines2)
	// {
	// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOriginal1 =
	// (LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>) Serializer.deSerializeThis(serialisedTimelines1);
	// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOriginal2 =
	// (LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>) Serializer.deSerializeThis(serialisedTimelines2);
	//
	// StringBuffer s = new StringBuffer("Comparing " + serialisedTimelines1 + " and " + serialisedTimelines2 + "\n");
	//
	// if (usersDayTimelinesOriginal1.size() == usersDayTimelinesOriginal2.size())
	// {
	// s.append("Num of users: same " + usersDayTimelinesOriginal1.size() + " = " + usersDayTimelinesOriginal2.size());
	//
	// for
	// }
	// else
	// {
	// s.append("Num of users: different " + usersDayTimelinesOriginal1.size() + " = " + usersDayTimelinesOriginal2.size());
	// }
	//
	//
	// WritingToFile.appendLineToFileAbsolute(s.toString(), Constant.getCommonPath() + "ComparingTimelines.txt");// , fullPathfileNameToUse);
	// }
}