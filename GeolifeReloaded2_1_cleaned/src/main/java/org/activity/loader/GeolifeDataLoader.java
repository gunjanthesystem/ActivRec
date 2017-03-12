package org.activity.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.activity.constants.Constant;
import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.FlatActivityLogEntry;
import org.activity.objects.TrajectoryEntry;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ConnectDatabase;
import org.activity.util.StringUtils;
import org.activity.util.UtilityBelt;

public class GeolifeDataLoader
{
	// static final String[] userNames= {"Stefan", "Tengqi","Cathal", "Zaher","Rami"};
	// public static String commonPath="/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/";

	// static final String databaseTableName = "Image2_Table";

	/*
	 * static final String[] activityNames= {"Others", "Commuting", "Computer", "Eating", "Exercising", "Housework",
	 * "On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV", "Unknown"};
	 * 
	 * static final String path=commonPath+"AllTogether7July/";
	 */
	static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedPlusDuration;
	// <UserName, <Timestamp,'activityName||durationInSeconds'>>
	// activityId
	static HashMap<String, Integer> modeNames;

	/*********** PARAMTERS TO SET ***************/
	static String commonPath = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/21April2016AllUsersDataGenerationUMAPAgainCorrectedTimeZone/";// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May18AllUsersDataGeneration2/";
	static String dataLoadingLogFilename = "dataLoadingLog21April2016.txt";// "dataLoadingLog18May.txt";
	static String nameForDataMapSerialised = "mapForAllDataMergedPlusDuration21Apr2016UMAPCorrectedTimeZone.map";// "mapForAllDataMergedPlusDuration18May2015_2.map";

	/*********** END OF PARAMTERS TO SET ***************/
	// static int modeCount=0;

	/*
	 * NOTE: mode = Unknown: no data point in dataset mode = Not Availabe: data point available but no mode assignment.
	 */

	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		try
		{
			TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 21, 2016
			ConnectDatabase.truncateAllData("geolife1");

			// String commonPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May14AllUsersDataGeneration/";//
			// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May13DataGeneration/";
			// Constant.setCommonPath();// ("/run/media/gunjan/HOME/gunjan/Geolife Data Works/");//
			// ("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April14_2015/DataLoading/");//
			// new Constant(commonPath, "geolife1");
			// new ConnectDatabase(Constant.getDatabaseName()); // all method and variable in this class are static

			Constant.initialise(commonPath, "geolife1");
			// Constant.setDatabaseName("geolife1");
			ConnectDatabase.initialise(Constant.getDatabaseName()); // all method and variable in this class are static

			// Constant.setUserIDs();// Constant.setInvalidNames(); // Constant.setActivityNames(); //
			// Constant.setCommonPath(commonPath);
			// ConnectDatabase.createPooledConnection();
			// ConnectDatabase.setDatabaseName(Constant.DATABASE_NAME);

			modeNames = new HashMap<String, Integer>();

			PrintStream loadLogStream = WritingToFile
					.redirectConsoleOutput(Constant.getCommonPath() + dataLoadingLogFilename);

			// deserialiser for one map data
			// mapForAllDataMergedPlusDuration =
			// (LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>)
			// (Serializer.deSerializeThis(Constant.getCommonPath()
			// + nameForDataMapSerialised));//
			// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April14_2015/DuringDataGeneration/mapForAllDataMergedPlusDuration14April2015.map"));//

			// deserialiser for distributed data
			mapForAllDataMergedPlusDuration = (LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>) (Serializer
					.deserialiseAndConsolidateMaps(Constant.getCommonPath() + nameForDataMapSerialised, 0, 65, 5));

			// traverseMapForAllData(testSerializer);;

			// mapForAllDataMergedPlusDuration=replaceDataEntry(mapForAllDataMergedPlusDuration,"Not
			// Available","Others");
			// mapForAllDataMergedPlusDuration=replaceDataEntry(mapForAllDataMergedPlusDuration,"badImages","Others");
			/**
			 * @todo //WritingToFile.writeActivityDistributionOcurrence(mapForAllDataMergedPlusDuration);
			 *       //WritingToFile.writeActivityDistributionDuration(mapForAllDataMergedPlusDuration);
			 **/
			// $$checkForGapsInTimeInterval(mapForAllDataMergedPlusDuration);

			// traverseMapForAllData(mapForAllDataMergedPlusDuration);
			long countOfTotalTrajectoryEntries = -1;
			// Uncomment below to load data in database
			long timeId = -1;
			long dateId = -1;
			long locationId = 2;
			long locationIdCounter = 2;

			ArrayList<Integer> uniqueActivityIds = new ArrayList<Integer>();
			ArrayList<Long> uniqueDateIds = new ArrayList<Long>();
			ArrayList<Long> uniqueLocationIds = new ArrayList<Long>();
			ArrayList<Long> uniqueTimeIds = new ArrayList<Long>();

			ArrayList<FlatActivityLogEntry> listOfActivityEntries = new ArrayList<FlatActivityLogEntry>();

			/*
			 * For Batch Insertion to improve performance
			 */
			ArrayList<FlatActivityLogEntry> toInsertInActivityFact = new ArrayList<FlatActivityLogEntry>();
			ArrayList<FlatActivityLogEntry> toInsertInDateDimension = new ArrayList<FlatActivityLogEntry>();
			ArrayList<FlatActivityLogEntry> toInsertInLocationDimension = new ArrayList<FlatActivityLogEntry>();
			ArrayList<FlatActivityLogEntry> toInsertInTimeDimension = new ArrayList<FlatActivityLogEntry>();
			// ArrayList<FlatActivityLogEntry> toInsertInUserDimension = new ArrayList<FlatActivityLogEntry>();

			boolean unknownLocationAdded = false; // only one location entry for all 'Unknown' mode entries.

			// ////////////////////////////////////////////////
			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entry : mapForAllDataMergedPlusDuration
					.entrySet())
			{
				String userName = entry.getKey();
				// System.out.println("\nUser ="+userName+ " userName.equals(010)=="+ userName.equals("010"));
				// if(userName.equals("010")==false)
				// {
				// continue;
				// }

				System.out.println("\nUser =" + userName);// System.out.println("User id = "+userId);
				int userId = Integer.parseInt(userName);// getUserIdFromName(userName);
				int userAge = getUserAge(userId);
				String ageCategory = getAgeCategory(userAge);
				String profession = getProfession(userId);
				String personality = getPersonality(profession);

				ConnectDatabase.insertIntoUserDimension(userId, userName, userAge, personality, profession,
						ageCategory);

				for (Map.Entry<Timestamp, TrajectoryEntry> dataForAUser : entry.getValue().entrySet())
				{
					// System.out.print(".");
					countOfTotalTrajectoryEntries++;
					TrajectoryEntry te = dataForAUser.getValue();
					Timestamp startTimestamp = dataForAUser.getKey();

					if (te.getTimestamp() != startTimestamp)
					{
						System.err.println("Error in Trajectory entry, te.getTimestamp() != dataForAUser.getKey()");
					}

					String activityName = te.getMode();// getActivityNameFromDataEntry(dataForAUser.getValue());
					int activityId = getActivityIdFromActivityName2(activityName);
					String activityCategory = getActivityCategoryFromActivityName(activityName);

					if (!(uniqueActivityIds.contains(activityId)))
					{
						// ## System.out.println("inserting in activity dimension:"+activityId+" "+activityName+"
						// "+activityCategory);
						ConnectDatabase.insertIntoActivityDimension(activityId, activityName, activityCategory); // only
																													// arund
																													// 13
																													// insertions,
																													// hence
																													// batch
																													// update
																													// is
																													// not
																													// necessary
						uniqueActivityIds.add(activityId);
					}

					int startHour = startTimestamp.getHours();
					int startMinute = startTimestamp.getMinutes();
					int startSecond = startTimestamp.getSeconds();

					// Timestamp endTimestamp = new Timestamp(dataForAUser.getKey().getTime()+
					// getDurationInSecondsFromDataEntry(dataForAUser.getValue())*1000);
					// endTimestamp = new Timestamp(endTimestamp.getTime()-1000);
					// int endHour= endTimestamp.getHours();
					// int endMinute= endTimestamp.getMinutes();
					// int endSecond= endTimestamp.getSeconds();//-1; // Be careful of the propagation of its affect

					long duration = te.getDurationInSeconds(); // getDurationInSecondsFromDataEntry(dataForAUser.getValue());

					Timestamp endTimestamp = new Timestamp(startTimestamp.getTime() + (duration * 1000) - 1000); // decrementing
																													// 1
																													// second
																													// to
																													// keep
																													// consecutive
																													// activities
																													// separated
																													// by
																													// 1
																													// seconds
					int endHour = endTimestamp.getHours();
					int endMinute = endTimestamp.getMinutes();
					int endSecond = endTimestamp.getSeconds();

					// timeId= //timeId+1; (iiWAS version) .... (the version causing error:
					// startHour+startMinute+startSecond+endHour+endMinute+endSecond; )

					// String timeIdString =
					// Integer.toString(startHour)+Integer.toString(startMinute)+Integer.toString(startSecond)
					// +Integer.toString(endHour)+Integer.toString(endMinute)+Integer.toString(endSecond);
					//
					// System.out.println("time id string="+timeIdString);
					// System.out.println("as long"+Long.parseLong(timeIdString));

					timeId = timeId + 1;// Long.parseLong(Integer.toString(startHour) + Integer.toString(startMinute) +
										// Integer.toString(startSecond) + Integer.toString(endHour) +
										// Integer.toString(endMinute)
					// + Integer.toString(endSecond));

					// System.out.print(" timeid:"+timeId);
					String timeCategory = getTimeCategory(startHour);
					if (((endTimestamp.getTime() - startTimestamp.getTime()) / 1000) != (duration - 1))
					{
						System.err.println("Error in data loading wrt: starttime-endtime != duration,  starttime="
								+ startTimestamp + " endtime=" + endTimestamp + " duration=" + duration);
						System.err.println("(endTimestamp.getTime()-startTimestamp.getTime()/1000) = "
								+ ((endTimestamp.getTime() - startTimestamp.getTime()) / 1000));// +"
																								// endtime"+endTimestamp+"
																								// duration="+duration);
					}

					if (((endTimestamp.getTime() < startTimestamp.getTime())))
					{
						System.err.println("endTimestamp.getTime() <startTimestamp.getTime()  starttimestamp="
								+ startTimestamp + " endtimestamp=" + endTimestamp + " duration=" + duration);
					}

					// else
					// System.out.println("\nstarttime="+startTimestamp+" endtime="+endTimestamp+" duration="+duration+"
					// endHourminutesseconds="+endHour+":"+endMinute+":"+endSecond);
					int year = startTimestamp.getYear() + 1900;
					int month = startTimestamp.getMonth() + 1;
					int day = startTimestamp.getDate();

					int endyear = endTimestamp.getYear() + 1900;
					int endmonth = endTimestamp.getMonth() + 1;
					int endday = endTimestamp.getDate();

					String weekDay = getWeekDay(startTimestamp);
					String weekOfYear = getWeekOfYear(startTimestamp);

					dateId = dateId + 1;// Long.parseLong(Integer.toString(day) + Integer.toString(month) +
										// Integer.toString(year) + Integer.toString(endday) +
										// Integer.toString(endmonth) +
										// Integer.toString(endyear));

					// System.out.println("date ID");
					// LocationObject locationObject=LocationObject.getLocationObjectInstance(userId,99);

					if (activityName.equalsIgnoreCase("Unknown"))
					{
						locationId = 1;
					}
					else
					{
						locationId = locationIdCounter;
						locationIdCounter++;
					}

					// System.out.print(// locationId+"*");
					String latitudes = te.getLatitude().toString(); // [12.2322, 23,2323, ...]
					String longitudes = te.getLongitude().toString(); // [12.2322, 23,2323, ...]
					String altitudes = te.getAltitude().toString(); // [12.2322, 23,2323, ...]

					String locationName = "NA";
					String locationCategory = "NA";
					String county = "NA";
					String city = "NA";
					String country = "China";
					String continent = "Asia";
					FlatActivityLogEntry activityLogEntry = new FlatActivityLogEntry(); // represents a row of all
																						// combined table

					activityLogEntry.setUser_ID(userId);
					activityLogEntry.setActitivity_ID(activityId);

					activityLogEntry.setTime_ID(timeId);

					activityLogEntry.setDate_ID(dateId);

					activityLogEntry.setLocation_ID(locationId);
					activityLogEntry.setDuration(safeLongToInt(duration));
					activityLogEntry.setFrequency(1);
					activityLogEntry.setUser_Name(userName);
					activityLogEntry.setPersonality_Tags(personality);
					activityLogEntry.setProfession(profession);
					activityLogEntry.setAge_Category(ageCategory);
					activityLogEntry.setUser_Age(userAge);
					activityLogEntry.setActivity_Name(activityName);
					activityLogEntry.setActivity_Category(activityCategory);
					activityLogEntry.setStart_Time(startHour, startMinute, startSecond);
					activityLogEntry.setEnd_Time(endHour, endMinute, endSecond);
					activityLogEntry.setTime_Category(timeCategory);
					activityLogEntry.setStart_Date(year, month, day);
					activityLogEntry.setEnd_Date(endyear, endmonth, endday);
					activityLogEntry.setWeek_Day(weekDay);
					activityLogEntry.setMonth(month);
					activityLogEntry.setQuarter(month);
					activityLogEntry.setWeek(Integer.parseInt(weekOfYear));
					activityLogEntry.setYear(year);

					activityLogEntry.setLatitudes(latitudes.substring(1, latitudes.length() - 1));// truncating opening
																									// and closing
																									// braces
					activityLogEntry.setLongitudes(longitudes.substring(1, longitudes.length() - 1));// truncating
																										// opening and
																										// closing
																										// braces
					activityLogEntry.setAltitudes(altitudes.substring(1, altitudes.length() - 1));// truncating opening
																									// and closing
																									// braces

					activityLogEntry.setLocation_Name(locationName);
					activityLogEntry.setLocation_Category(locationCategory);
					activityLogEntry.setCity(city);
					activityLogEntry.setCounty(county);
					activityLogEntry.setCountry(country);
					activityLogEntry.setContinent(continent);

					activityLogEntry.setStartLatitude(te.getStartLat());
					activityLogEntry.setEndLatitude(te.getEndLat());

					activityLogEntry.setStartLongitude(te.getStartLon());
					activityLogEntry.setEndLongitude(te.getEndLon());

					activityLogEntry.setStartAltitude(te.getStartAlt());// te.getAltitude().get(0));
					activityLogEntry.setEndAltitude(te.getEndAlt());// te.getAltitude().get(te.getAltitude().size() -
																	// 1));

					activityLogEntry.setAvgAltitude(
							StatsUtils.averageDecimalsAsStrings(UtilityBelt.removeInvalidAlts(te.getAltitude())));// .stream().a//.mapToDouble(a
																													// ->

					String trajectoryIDsCompact = StringUtils.toStringCompactWithCount(te.getTrajectoryID());
					if (trajectoryIDsCompact.length() > 50)
					{
						System.out.println("Alert: trajectoryIDsCompact.length() > 50 = "
								+ trajectoryIDsCompact.length() + " \n" + trajectoryIDsCompact);
					}
					activityLogEntry.setTrajectory_IDs(StringUtils.toStringCompactWithCount(te.getTrajectoryID()));// Note:
																													// there
																													// can
																													// be
																													// multiple
																													// trajectoryIDs
																													// if
																													// the
																													// mergers
																													// during
																													// data
																													// creating
																													// was
																													// not
																													// trajectory
																													// sensitive.

					toInsertInActivityFact.add(activityLogEntry); // ConnectDatabase.insertIntoActivityFact(activityLogEntry);

					if (!(uniqueTimeIds.contains(timeId)))
					{
						toInsertInTimeDimension.add(activityLogEntry);// ConnectDatabase.insertIntoTimeDimension(activityLogEntry);
						uniqueTimeIds.add(timeId);
					}

					if (!(uniqueDateIds.contains(dateId)))
					{
						toInsertInDateDimension.add(activityLogEntry);// ConnectDatabase.insertIntoDateDimension(activityLogEntry);
						uniqueDateIds.add(dateId);
					}

					// if(!(uniqueLocationIds.contains(locationId)))
					// for current implemenation this is always unique..hence for each entry we need to insert a
					// location entry.
					if (locationId == 1)
					{
						if (unknownLocationAdded == false)
						{
							toInsertInLocationDimension.add(activityLogEntry);// ConnectDatabase.insertIntoLocationDimensionTrajectory(activityLogEntry);
							uniqueLocationIds.add(locationId);
							unknownLocationAdded = true;
						}
						else
						{
						}
					}

					else
					{
						toInsertInLocationDimension.add(activityLogEntry);// ConnectDatabase.insertIntoLocationDimensionTrajectory(activityLogEntry);
						uniqueLocationIds.add(locationId);
					}

					listOfActivityEntries.add(activityLogEntry);

				} // end of iteration over data for single user
					// break;// ALERT DEBUGGING ONLY FOR FIRST USER ONLY
			} // end of iteration over all user

			if (toInsertInDateDimension.size() == 0)
			{
				System.err.println("toInsertInDateDimension =0 ");
			}

			System.out.println("Total number of trajectory entries in data:" + countOfTotalTrajectoryEntries);

			System.out.println("Number of location entries to insert:" + toInsertInLocationDimension.size());
			System.out.println("Number of date entries to insert:" + toInsertInDateDimension.size());
			System.out.println("Number of time entries to insert:" + toInsertInTimeDimension.size());
			System.out.println("Number of activity fact entries to insert:" + toInsertInActivityFact.size());

			ConnectDatabase.insertIntoLocationDimensionTrajectory(toInsertInLocationDimension);
			ConnectDatabase.insertIntoDateDimension(toInsertInDateDimension);
			ConnectDatabase.insertIntoTimeDimension(toInsertInTimeDimension);
			ConnectDatabase.insertIntoActivityFact(toInsertInActivityFact);

			WritingToFile.writeArrayListFlatActivityLogEntry(listOfActivityEntries, "FlatActivityLogEntries",
					"User_ID,Activity_ID,Date_ID,Time_ID,Location_ID,User_Name,Activity_Name,Start_Time,End_Time,Start_Date,End_Date,Duration,Start_Latitude,Start_Longitude,Start_Altitude,End_Latitude,End_Longitude,End_Altitude,Avg_Altitude");

			// Serializer.serializeAllLogEntries(listOfActivityEntries, "flatFile");
			System.out.println("Data loading finished: " + listOfActivityEntries.size() + "entries created");
			PopUps.showMessage("Data loading finished");

			loadLogStream.close();

			if (Constant.USEPOOLED)
			{
				ConnectDatabase.destroyPooledConnection();
			}

			System.exit(0);
			// ConnectDatabase.destroyPooledConnection();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, String>> checkForGapsInTimeInterval(
			LinkedHashMap<String, TreeMap<Timestamp, String>> map)
	{
		LinkedHashMap<String, TreeMap<Timestamp, String>> newMap = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		for (Map.Entry<String, TreeMap<Timestamp, String>> entry : map.entrySet())
		{
			ArrayList<String> treeMapArrayList = UtilityBelt.treeMapToArrayListString(entry.getValue());
			TreeMap<Timestamp, String> augmentedTreeMap = entry.getValue();

			for (int i = 0; i < treeMapArrayList.size() - 1; i++)
			{
				String[] splitted = treeMapArrayList.get(i).split(Pattern.quote("||"));

				Timestamp currentTimestamp = new Timestamp(Long.valueOf(splitted[0]));
				long currentDuration = Long.valueOf(splitted[2]);

				String[] splittedNext = treeMapArrayList.get(i + 1).split(Pattern.quote("||"));
				Timestamp nextTimestamp = new Timestamp(Long.valueOf(splittedNext[0]));

				long differenceBeforeNextStarts = nextTimestamp.getTime() / 1000
						- (currentTimestamp.getTime() / 1000 + currentDuration);

				System.out.println(">> the time difference before next starts is: " + differenceBeforeNextStarts);
				if (differenceBeforeNextStarts > 0) System.out.println("GAP found in time continuum");

			}

			/*
			 * for (Map.Entry<Timestamp,String> entryTree : entry.getValue().entrySet()) {
			 * newTreeMap.put(entryTree.getKey(),entryTree.getValue().replaceAll(oldString, newString));
			 * 
			 * 
			 * }
			 * 
			 * if(entry.getValue().size() != newTreeMap.size())
			 * System.err.println("Error in replace Data Entry: size mismatch of treemap");
			 * 
			 * newMap.put(entry.getKey(), newTreeMap);
			 */
		}

		/*
		 * if(map.size() != newMap.size())
		 * System.err.println("Error in replace Data Entry: size mismatch of linked hash map");
		 */
		return newMap;
	}

	public static String getActivityNameFromDataEntry(String dataEntryForAnImage)
	{
		// String activityName= new String();
		String[] splitted = dataEntryForAnImage.split(Pattern.quote("||"));

		return splitted[0];
	}

	public static long getDurationInSecondsFromDataEntry(String dataEntryForAnImage)
	{
		// String activityName= new String();

		String[] splitted = dataEntryForAnImage.split(Pattern.quote("||"));

		return Long.valueOf(splitted[1]).longValue();
	}

	/*
	 * public static int getUserIdFromName(String userName) { int userId=-99;
	 * 
	 * for(int i=0;i<userNames.length;i++) { if(userNames[i].equals(userName.trim())) { userId=i; } }
	 * 
	 * if(userId==-99) {
	 * System.err.println("Error: inside getUserIdFromName - Cannot find User ID for User = "+userName); } return
	 * userId; }
	 * 
	 * public static int getActivityIdFromActivityName(String activityName) { int activityId=-99;
	 * 
	 * for(int i=0;i<activityNames.length;i++) { if(activityNames[i].equals(activityName.trim())) { activityId=i; } }
	 * 
	 * if(activityId==-99) {
	 * System.err.println("Error: inside getActivityIdFromActivityName - Cannot find Activity ID for Activity Name = "
	 * +activityName); } return activityId; }
	 */
	public static int getActivityIdFromActivityName2(String activityName)
	{
		int activityID;

		if (!modeNames.containsKey(activityName))
		{
			modeNames.put(activityName, modeNames.size() + 1);
			// activityID= modeNames.get(activityName);
		}

		activityID = modeNames.get(activityName);

		return activityID;
	}

	public static String getWeekDay(Timestamp timestamp)
	{
		String weekDay = "default";

		int day = timestamp.getDay();

		switch (day)
		{
		case 0:
			weekDay = "Sunday";
			break;
		case 1:
			weekDay = "Monday";
			break;
		case 2:
			weekDay = "Tuesday";
			break;
		case 3:
			weekDay = "Wednesday";
			break;
		case 4:
			weekDay = "Thursday";
			break;
		case 5:
			weekDay = "Friday";
			break;
		case 6:
			weekDay = "Saturday";
			break;
		default:
			weekDay = "unknown";
			break;

		}

		return weekDay;
	}

	public static String getWeekOfYear(Timestamp timestamp)
	{
		String weekOfYear = "default";

		try
		{
			weekOfYear = ConnectDatabase.getSQLStringResultSingleColumn("SELECT WEEKOFYEAR('" + timestamp + "');")
					.get(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return weekOfYear;
	}

	public static LinkedHashMap<String, TreeMap<Timestamp, String>> replaceDataEntry(
			LinkedHashMap<String, TreeMap<Timestamp, String>> map, String oldString, String newString)
	{
		LinkedHashMap<String, TreeMap<Timestamp, String>> newMap = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		for (Map.Entry<String, TreeMap<Timestamp, String>> entry : map.entrySet())
		{
			TreeMap<Timestamp, String> newTreeMap = new TreeMap<Timestamp, String>();

			for (Map.Entry<Timestamp, String> entryTree : entry.getValue().entrySet())
			{
				newTreeMap.put(entryTree.getKey(), entryTree.getValue().replaceAll(oldString, newString));
			}

			if (entry.getValue().size() != newTreeMap.size())
				System.err.println("Error in replace Data Entry: size mismatch of treemap");

			newMap.put(entry.getKey(), newTreeMap);
		}

		if (map.size() != newMap.size())
			System.err.println("Error in replace Data Entry: size mismatch of linked hash map");

		return newMap;
	}

	/*
	 * public static LinkedHashMap<String, TreeMap<Timestamp,String>> fillWithUnknown(LinkedHashMap<String,
	 * TreeMap<Timestamp,String>> map) { LinkedHashMap<String, TreeMap<Timestamp,String>> newMap= new
	 * LinkedHashMap<String, TreeMap<Timestamp,String>>();
	 * 
	 * for (Map.Entry<String, TreeMap<Timestamp,String>> entry : map.entrySet()) {
	 * 
	 * 
	 * TreeMap<TimeInterval,String> intervalMap= new TreeMap<TimeInterval,String> ();
	 * 
	 * 
	 * for (Map.Entry<Timestamp,String> entryTree : entry.getValue().entrySet()) {
	 * //newTreeMap.put(entryTree.getKey(),entryTree.getValue().replaceAll(oldString, newString)); Timestamp
	 * startTimestamp=entryTree.getKey();
	 * 
	 * 
	 * String[] splitted=entryTree.getValue().split(Pattern.quote("||")); long durationInSeconds =
	 * Long.valueOf(splitted[1]);
	 * 
	 * intervalMap.put(new TimeInterval(entryTree.getKey(),durationInSeconds), entryTree.getValue()); }
	 * 
	 * if(entry.getValue().size() != intervalMap.size())
	 * System.err.println("Error in replace Data Entry: size mismatch of treemap");
	 * 
	 * newMap.put(entry.getKey(), newTreeMap); }
	 * 
	 * if(map.size() != newMap.size())
	 * System.err.println("Error in replace Data Entry: size mismatch of linked hash map");
	 * 
	 * return newMap;
	 * 
	 * 
	 * }
	 */

	/**
	 * 
	 * 
	 * @param treeMap
	 * @return ArrayList of strings of the form 'timestampInMilliSeconds||ActivityName||durationInSeconds'
	 */
	public static ArrayList<String> treeMapToArrayList(TreeMap<Timestamp, String> treeMap)
	{
		ArrayList<String> arrayList = new ArrayList<String>();

		for (Map.Entry<Timestamp, String> entry : treeMap.entrySet())
		{
			String timestampString = Long.toString(entry.getKey().getTime());
			String imageNameActivityName = entry.getValue();

			arrayList.add(timestampString + "||" + imageNameActivityName);
		}

		if (treeMap.size() == arrayList.size())
		{
			System.out.println("TreeMap converted to arraylist succesfully");
		}
		return arrayList;
	}

	// timestampInMilliSeconds||ImageName||ActivityName
	/**
	 * 
	 * 
	 * @param arrayListToConvert
	 * @return
	 */
	public static TreeMap<Timestamp, String> arrayListToTreeMap(ArrayList<String> arrayListToConvert)
	{
		TreeMap<Timestamp, String> treeMap = new TreeMap<Timestamp, String>();

		for (int i = 0; i < arrayListToConvert.size(); i++)
		{
			String[] splitted = arrayListToConvert.get(i).split(Pattern.quote("||"));

			Timestamp timeStamp = new Timestamp(Long.valueOf(splitted[0]));

			String stringForValue = new String();

			for (int j = 1; j < splitted.length - 1; j++)
			{
				stringForValue += splitted[j] + "||";
			}

			stringForValue += splitted[splitted.length - 1];

			treeMap.put(timeStamp, stringForValue);
		}

		return treeMap;

	}

	public static String getUserName(int userId)
	{
		String userName = "";

		switch (userId)
		{
		case 0:
			userName = "Stefan";
			break;
		case 1:
			userName = "Tengqi";
			break;
		case 2:
			userName = "Cathal";
			break;
		case 3:
			userName = "Zaher";
			break;

		default:
			userName = "Nefertiti";
			break;
		}
		return userName;
	}

	public static int getUserAge(int userId)
	{
		int age = -1;

		switch (userId)
		{
		case 0:
			age = 25;
			break;
		case 1:
			age = 25;
			break;
		case 2:
			age = 25;
			break;
		case 3:
			age = 25;
			break;

		default:
			age = -1;
			break;
		}
		return age;
	}

	public static String getProfession(int userId)
	{
		String professionName = "";

		switch (userId)
		{
		case 0:
			professionName = "Researcher";
			break;
		case 1:
			professionName = "Researcher";
			break;
		case 2:
			professionName = "Professor";
			break;
		case 3:
			professionName = "Researcher";
			break;

		default:
			professionName = "Default Profession";
			break;
		}
		return professionName;
	}

	public static String getPersonality(String profession)
	{
		String personalityName = "";

		switch (profession)
		{
		case "Administrator":
			personalityName = "Enterprising";
			break;
		case "Soccer Player":
			personalityName = "Realistic Enterprising";
			break;
		case "Accountant":
			personalityName = "Realistic";
			break;
		case "Teacher":
			personalityName = "Investigative";
			break;
		case "Researcher":
			personalityName = "Investigative";
			break;
		case "Student":
			personalityName = "Student";
			break;
		default:
			personalityName = "Default personality";
			break;
		}
		return personalityName;
	}

	/**
	 * Returns the maximum duration in minutes for the given activity id.
	 * 
	 * @param activityId
	 * @return
	 */
	public static int getActivityMaxDurationInMinutes(int activityId)
	{
		int maxDurationInMinutes = 4 * 60;
		switch (activityId)
		{
		case 0:
			maxDurationInMinutes = 2 * 60;// activityName= "Commuting";
			break;
		case 1:
			maxDurationInMinutes = 4 * 60;// activityName= "Working";
			break;
		case 2:
			maxDurationInMinutes = (3 * 60) + 10;// activityName= "Socialising";
			break;
		case 3:
			maxDurationInMinutes = 4 * 60;// activityName= "Computer";
			break;
		case 4:
			maxDurationInMinutes = 2 * 55;// activityName= "WatchingTV";
			break;
		case 5:
			maxDurationInMinutes = 4 * 45;// activityName= "Shopping";
			break;
		case 6:
			maxDurationInMinutes = 30;// activityName= "On Phone";
			break;
		case 7:
			maxDurationInMinutes = 4 * 55;// activityName= "Relaxing";
			break;
		case 8:
			maxDurationInMinutes = 30;// activityName= "Meditating";
			break;
		case 9:
			maxDurationInMinutes = 3 * 35;// activityName= "Taking care of children";
			break;
		case 10:
			maxDurationInMinutes = 85;// activityName= "Preparing Food";
			break;
		case 11:
			maxDurationInMinutes = 2 * 60;// activityName= "Housework";
			break;
		case 12:
			maxDurationInMinutes = 30;// activityName= "Eating";
			break;
		case 13:
			maxDurationInMinutes = 75;// activityName= "Nap/Resting";
			break;
		case 14:
			maxDurationInMinutes = 60;// activityName= "Exercising";
			break;
		default:
			maxDurationInMinutes = 4 * 60;// activityName= "Others";
			break;
		}
		return maxDurationInMinutes;
	}

	public static int getActivityMinDurationInMinutes(int activityId)
	{
		int minDurationInMinutes = 15;
		switch (activityId)
		{
		case 0:
			minDurationInMinutes = 30;// activityName= "Commuting";
			break;
		case 1:
			minDurationInMinutes = 1 * 60;// activityName= "Working";
			break;
		case 2:
			minDurationInMinutes = 20;// activityName= "Socialising";
			break;
		case 3:
			minDurationInMinutes = 1 * 60;// activityName= "Computer";
			break;
		case 4:
			minDurationInMinutes = 30;// activityName= "WatchingTV";
			break;
		case 5:
			minDurationInMinutes = 80;// activityName= "Shopping";
			break;
		case 6:
			minDurationInMinutes = 15;// activityName= "On Phone";
			break;
		case 7:
			minDurationInMinutes = 60;// activityName= "Relaxing";
			break;
		case 8:
			minDurationInMinutes = 20;// activityName= "Meditating";
			break;
		case 9:
			minDurationInMinutes = 60;// activityName= "Taking care of children";
			break;
		case 10:
			minDurationInMinutes = 30;// activityName= "Preparing Food";
			break;
		case 11:
			minDurationInMinutes = 50;// activityName= "Housework";
			break;
		case 12:
			minDurationInMinutes = 30;// activityName= "Eating";
			break;
		case 13:
			minDurationInMinutes = 30;// activityName= "Nap/Resting";
			break;
		case 14:
			minDurationInMinutes = 20;// activityName= "Exercising";
			break;
		default:
			minDurationInMinutes = 0;// activityName= "Others";
			break;
		}
		return minDurationInMinutes;
	}

	/*
	 * public static String getActivityName(int activityId) { String activityName=" "; switch(activityId) { case 0:
	 * activityName= "Commuting"; break; ////////// categories in DCU data Version 1 case 1: activityName= "Working";
	 * break; case 2: activityName= "Socialising"; break;///////// case 3: activityName= "Computer"; break;///////////
	 * case 4: activityName= "WatchingTV"; break;////////// case 5: activityName= "Shopping"; break;///////////// case
	 * 6: activityName= "On Phone"; break;/////////// case 7: activityName= "Relaxing"; break; case 8: activityName=
	 * "Meditating"; break; case 9: activityName= "Taking care of children"; break; case 10: activityName=
	 * "Preparing Food"; break;/////////// case 11: activityName= "Housework"; break;///////// case 12: activityName=
	 * "Eating"; break;/////////// case 13: activityName= "Nap/Resting"; break; case 14: activityName= "Exercising";
	 * break;/////////// case 15: activityName= "Uncategorised"; break; ///// this is the 'badImages default:
	 * activityName= "Others"; break; } return activityName; }
	 * 
	 * public static String getActivityCategory(String activityName) { int id=0;
	 * 
	 * switch(activityName) { case "Commuting": id=0 ; break; case "Working": id=1 ; break; case "Socialising": id=2 ;
	 * break; case "Computer": id=3 ; break; case "WatchingTV": id=4 ; break; case "Shopping": id=5 ; break; case
	 * "On Phone": id=6 ; break; case "Relaxing": id=7 ; break; case "Meditating": id=8 ; break; case
	 * "Taking care of children": id=9 ; break; case "Preparing Food": id=10 ; break; case "Housework": id=11 ; break;
	 * case "Eating": id=12 ; break; case "Nap/Resting": id=13 ; break; case "Exercising": id=14 ; break; case
	 * "Uncategorised": id=15; break; ///// this is the 'badImages default: id= 99; break; }
	 * 
	 * return getActivityCategory(id); }
	 * 
	 * 
	 * 
	 * 
	 * public static int getActivityid(String activityName) { int id=0;
	 * 
	 * switch(activityName) { case "Commuting": id=0 ; break; case "Working": id=1 ; break; case "Socialising": id=2 ;
	 * break; case "Computer": id=3 ; break; case "WatchingTV": id=4 ; break; case "Shopping": id=5 ; break; case
	 * "On Phone": id=6 ; break; case "Relaxing": id=7 ; break; case "Meditating": id=8 ; break; case
	 * "Taking care of children": id=9 ; break; case "Preparing Food": id=10 ; break; case "Housework": id=11 ; break;
	 * case "Eating": id=12 ; break; case "Nap/Resting": id=13 ; break; case "Exercising": id=14 ; break; case
	 * "Uncategorised": id=15; break; ///// this is the 'badImages default: id= 99; break; }
	 * 
	 * return id; }
	 */

	public static String getActivityCategoryFromActivityName(String activityName)
	{
		String activityCategory = "default";

		/*
		 * DCU {"badImages", 0/// "Commuting",1/////////////// "Computer", 2//////////// "Eating", 3 "Exercising", 4
		 * "Housework", 5 "On the Phone", 6 "Preparing Food", 7 "Shopping", 8 "Socialising", 9 "Watching TV"}; 10
		 */

		switch (activityName.trim().toLowerCase())
		{
		case "working":
		case "computer":// ////////////
		case "meditating":
		case "exercising":// //////////
		case "eating":// /////////
			activityCategory = "Productive";
			break;

		case "on the phone": // /////////
		case "shopping":// ///////////
		case "housework":// //////////
		case "preparing food":// ////////////
		case "taking care of children":
		case "commuting":// //////
			activityCategory = "Necessities";
			break;

		case "watching tv": // ///////////////
		case "nap/resting":
		case "relaxing":
		case "socialising":// //////////////
			activityCategory = "Leisure";
			break;

		case "badimages":
		case "not available":
			activityCategory = "Others";
			break;

		default:
			activityCategory = "Others";
			break;

		}

		/*
		 * switch(activityId%3) { case 0: activityCategory="category 1"; break; //commuting, computer, on phone, taking
		 * care, eating, case 1: activityCategory="category 2"; break; //working, watchingtv. relaxing, preparing food,
		 * nap/resting case 2: activityCategory="category 3"; break; //socialising, shopping, meditating, housework,
		 * exercising default: activityCategory="others"; break; }
		 */
		return activityCategory;
	}

	public static String getActivityCategory(int activityId)
	{
		String activityCategory = "default";

		/*
		 * DCU {"badImages", 0 "Commuting",1 "Computer", 2 "Eating", 3 "Exercising", 4 "Housework", 5 "On the Phone", 6
		 * "Preparing Food", 7 "Shopping", 8 "Socialising", 9 "Watching TV"}; 10
		 */
		// Category PRODUCTIVE: (activity ids: 1,3,8,14,12)
		/*
		 * 1. Working 2. Computer 3. meditating 4. exercising 5. eating
		 */

		// Category NECESSITIES: (activity ids: 6, 5, 11,10,9, 0)
		/*
		 * 1. on phone 2. shopping 3. housework 4. preparing food 5. taking care of children 6. commuting
		 */

		// Category LEISURE: (activity ids: 4, 13, 7, 2)
		/*
		 * 1. Watchingtv 2. nap/resting 3. relaxing 4. socialising
		 */

		switch (activityId)
		{
		case 1:
		case 3:
		case 8:
		case 14:
		case 12:
			activityCategory = "Productive";
			break;

		case 6:
		case 5:
		case 11:
		case 10:
		case 9:
		case 0:
			activityCategory = "Necessities";
			break;

		case 4:
		case 13:
		case 7:
		case 2:
			activityCategory = "Leisure";
			break;

		default:
			activityCategory = "Others";
			break;

		}

		/*
		 * switch(activityId%3) { case 0: activityCategory="category 1"; break; //commuting, computer, on phone, taking
		 * care, eating, case 1: activityCategory="category 2"; break; //working, watchingtv. relaxing, preparing food,
		 * nap/resting case 2: activityCategory="category 3"; break; //socialising, shopping, meditating, housework,
		 * exercising default: activityCategory="others"; break; }
		 */
		return activityCategory;
	}

	public static String getAgeCategory(int age)
	{
		String category = "default";

		if (age <= 35 && age >= 28)
			category = "Adult";
		else
			category = "Young";
		return category;
	}

	public static String getTimeCategory(int startHour)
	{
		String category = "default";

		if (startHour >= 4 && startHour <= 11)
			category = "Morning";
		else if (startHour >= 12 && startHour <= 13)
			category = "Noon";
		else if (startHour >= 14 && startHour <= 19)
			category = "Evening";
		else if (startHour >= 19 && startHour <= 23) category = "Night";
		return category;
	}

	public static int randomInRange(int min, int max)
	{
		return (min + (int) (Math.random() * ((max - min) + 1)));
	}

	public static int randomInRangeWithBias(int min, int max, int biasNumber)
	{
		if (Math.random() < 0.35) // 35% bias approx
		{
			return biasNumber;
		}
		else
			return (min + (int) (Math.random() * ((max - min) + 1)));
	}

	public static boolean containsText(String fileName, String textToValidate)
	{
		Boolean contains = false;

		BufferedReader br = null;
		try
		{
			String currentLine;

			br = new BufferedReader(new FileReader(fileName));

			while ((currentLine = br.readLine()) != null)
			{
				if (currentLine.contains(textToValidate))
				{
					contains = true;
					br.close();
					return contains;
				}
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return contains;
	}

	public static int safeLongToInt(long l)
	{
		try
		{
			if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
			{
				throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return (int) l;
	}

	/*
	 * public static String getCategory(String userName, String imageName) { String category= "Not Available";
	 * 
	 * for(int iterator=0;iterator<categoryNames.length;iterator++) {
	 * if(containsText(path+userName+"_"+categoryNames[iterator]+".txt", imageName)) { category=categoryNames[iterator];
	 * return category; } } return category; }
	 */

	public static Timestamp getTimestamp(String imageName)
	{
		Timestamp timeStamp = null;
		int year = 0, month = 0, day = 0, hours = 0, minutes = 0, seconds = 0;

		// Pattern imageNamePattern= Pattern.compile("((.*)(_)(.*)(_)(");
		StringTokenizer tokenizer = new StringTokenizer(imageName, "_");
		int count = 0;
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			// System.out.println("token ="+token+" count="+count);

			if (count == 2)
			{
				year = Integer.parseInt(token.substring(0, 4));
				month = Integer.parseInt(token.substring(4, 6));
				day = Integer.parseInt(token.substring(6, 8));
			}

			if (count == 3)
			{
				hours = Integer.parseInt(token.substring(0, 2));
				minutes = Integer.parseInt(token.substring(2, 4));
				seconds = Integer.parseInt(token.substring(4, 6));
			}
			count++;

		}

		// System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds);
		timeStamp = new Timestamp(114, month - 1, day, hours, minutes, seconds, 0); // / CHECK it out
		System.out.println("Time stamp" + timeStamp);
		return timeStamp;
	}

	public static Date getDate(String imageName)
	{
		Date date = null;
		int year = 0, month = 0, day = 0;// , hours=0, minutes=0, seconds=0;

		// Pattern imageNamePattern= Pattern.compile("((.*)(_)(.*)(_)(");
		StringTokenizer tokenizer = new StringTokenizer(imageName, "_");
		int count = 0;
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			// System.out.println("token ="+token+" count="+count);

			if (count == 2)
			{
				year = Integer.parseInt(token.substring(0, 4));
				month = Integer.parseInt(token.substring(4, 6));
				day = Integer.parseInt(token.substring(6, 8));
				date = new Date(114, month - 1, day);
				return date;
			}

			count++;

		}

		// System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds);

		return date;
	}

	public static void traverseMapForAllData(LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData)
	{
		System.out.println("Traversing all data just before upload to database");
		for (Map.Entry<String, TreeMap<Timestamp, String>> entry : mapForAllData.entrySet())
		{
			System.out.println("\nUser =" + entry.getKey());

			for (Map.Entry<Timestamp, String> entryMapEachPhoto : entry.getValue().entrySet())
			{
				System.out.print(entryMapEachPhoto.getKey());
				System.out.print("   " + entryMapEachPhoto.getValue());
				System.out.print("\n");
			}
		}
	}

	/*
	 * public static void insertDummyIntoImageTable() {
	 * 
	 * Connection conn = null; PreparedStatement stmt = null;
	 * 
	 * try { Class.forName(JDBC_DRIVER);
	 * System.out.println("Connecting to a selected database for insertion in activity_fact_table..."); conn =
	 * DriverManager.getConnection(DB_URL, USER, PASS); System.out.println("Connected database successfully..."); String
	 * sqlInsertion = "INSERT INTO "+databaseName+"." +databaseTableName+ " VALUES (?,?,?,?,?,?,?)";
	 * 
	 * stmt = conn.prepareStatement(sqlInsertion);
	 * 
	 * for(int userIterator=0;userIterator<userNames.length;userIterator++) {
	 * 
	 * for(int activityIterator=0; activityIterator< categoryNames.length;activityIterator++) { try {
	 * stmt.setString(1,"Dummy"); stmt.setString(2,userNames[userIterator]); stmt.setTimestamp(3,null);
	 * stmt.setString(4,categoryNames[activityIterator]); stmt.setString(5,null); stmt.setString(6,null);
	 * stmt.setInt(7,0);
	 * 
	 * stmt.executeUpdate(); System.out.print(" > "); } catch (Exception e) { e.printStackTrace(); } }
	 * 
	 * 
	 * }
	 * 
	 * }
	 * 
	 * catch(SQLException se) //Handle errors for JDBC { se.printStackTrace(); } catch(Exception e) //Handle errors for
	 * Class.forName { e.printStackTrace(); } finally //finally block used to close resources { try { if(stmt!=null)
	 * conn.close(); } catch(SQLException se) {} try { if(conn!=null) conn.close(); } catch(SQLException se) {
	 * se.printStackTrace(); } }
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * /* public static void insertIntoImageTable() {
	 * 
	 * Connection conn = null; PreparedStatement stmt = null;
	 * 
	 * try { Class.forName(JDBC_DRIVER);
	 * System.out.println("Connecting to a selected database for insertion in activity_fact_table..."); conn =
	 * DriverManager.getConnection(DB_URL, USER, PASS); System.out.println("Connected database successfully..."); String
	 * sqlInsertion = "INSERT INTO "+databaseName+"." +databaseTableName+ " VALUES (?,?,?,?,?,?,?)";
	 * 
	 * stmt = conn.prepareStatement(sqlInsertion);
	 * 
	 * for(int userIterator=0;userIterator<userNames.length;userIterator++) { String
	 * imageListFileName=path+userNames[userIterator]+"_JPGFiles.txt";
	 * 
	 * BufferedReader br = null; try { String currentImageName;
	 * 
	 * br = new BufferedReader(new FileReader(imageListFileName));
	 * 
	 * 
	 * if((currentImageName = br.readLine()) == null) {
	 * System.out.println("ERROR: User:"+userNames[userIterator]+" file:"+imageListFileName+" is empty");
	 * br.close();return ; }
	 * 
	 * while ((currentImageName = br.readLine()) != null) { //getCategory(userNames[userIterator],currentImageName);
	 * 
	 * currentImageName= currentImageName.substring(0, currentImageName.length()-1); //removing the last comma Timestamp
	 * timestamp=getTimestamp(currentImageName);
	 * 
	 * String dayName=getSQLStringResultSingleColumn("SELECT DAYNAME('"+timestamp+"');").get(0);
	 * stmt.setString(1,currentImageName); stmt.setString(2,userNames[userIterator]);
	 * stmt.setTimestamp(3,getTimestamp(currentImageName));
	 * stmt.setString(4,getCategory(userNames[userIterator],currentImageName)); stmt.setString(5,dayName);
	 * stmt.setDate(6,getDate(currentImageName)); stmt.setInt(7,1);
	 * 
	 * stmt.executeUpdate(); System.out.print(" > ");
	 * 
	 * } br.close(); } catch (IOException e) { e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * catch(SQLException se) //Handle errors for JDBC { se.printStackTrace(); } catch(Exception e) //Handle errors for
	 * Class.forName { e.printStackTrace(); } finally //finally block used to close resources { try { if(stmt!=null)
	 * conn.close(); } catch(SQLException se) {} try { if(conn!=null) conn.close(); } catch(SQLException se) {
	 * se.printStackTrace(); } }
	 * 
	 * 
	 * }
	 * 
	 * 
	 * public static ArrayList<String> getSQLStringResultSingleColumn(String query) throws SQLException { Connection
	 * conn = null; PreparedStatement stmt = null; ResultSet resultset = null; ArrayList<String> resultStrings=new
	 * ArrayList<String>();
	 * 
	 * try { Class.forName(JDBC_DRIVER);
	 * System.out.println("Connecting to a selected database for getSQLStringResult()..."); conn =
	 * DriverManager.getConnection(DB_URL, USER, PASS); System.out.println("Connected database successfully..."); String
	 * sqlQuery = query; stmt = conn.prepareStatement(sqlQuery); resultset = stmt.executeQuery();
	 * while(resultset.next()) { resultStrings.add(resultset.getString(1).trim()); } }
	 * 
	 * catch(SQLException se) //Handle errors for JDBC { se.printStackTrace(); } catch(Exception e) //Handle errors for
	 * Class.forName { e.printStackTrace(); } finally //finally block used to close resources { try { if(stmt!=null)
	 * conn.close(); } catch(SQLException se) {} try { if(conn!=null) conn.close(); } catch(SQLException se) {
	 * se.printStackTrace(); } } System.out.println("Exiting getSQLStringResult() from database"); return resultStrings;
	 * }
	 */

	/*
	 * for(int userId=0;userId<=3;userId++) // 4 Users { String userName=getUserName(userId);
	 * 
	 * int userAge= getUserAge(userId); String ageCategory=getAgeCategory(userAge); String
	 * profession=getProfession(userId); String personality=getPersonality(profession);
	 * 
	 * ConnectDatabase.insertIntoUserDimension(userId,userName,userAge,personality,profession,ageCategory);
	 * 
	 * ///// String imageListFileName=path+userName+"_JPGFiles.txt";
	 * 
	 * BufferedReader br = null; try { String currentImageName;
	 * 
	 * br = new BufferedReader(new FileReader(imageListFileName));
	 * 
	 * 
	 * if((currentImageName = br.readLine()) == null) {
	 * System.out.println("ERROR: User:"+userName+" file:"+imageListFileName+" is empty"); br.close();return ; }
	 * 
	 * while ((currentImageName = br.readLine()) != null) { //getCategory(userNames[userIterator],currentImageName);
	 * 
	 * currentImageName= currentImageName.substring(0, currentImageName.length()-1); //removing the last comma
	 * 
	 * Timestamp timestamp=getTimestamp(currentImageName); Timestamp endTimestamp= getEndTimestamp(currentImageName);
	 * 
	 * 
	 * String dayName=getSQLStringResultSingleColumn("SELECT DAYNAME('"+timestamp+"');").get(0);
	 * 
	 * 
	 * dateId=Integer.parseInt( Integer.toString(timestamp.getDate())+ Integer.toString(timestamp.getMonth())+
	 * Integer.toString(timestamp.getYear()) );
	 * 
	 * String activityName=getCategory(userName,currentImageName); // here get Category fetches the activity name, .i.e
	 * activity category means activity name in this case String activityCategory=getActivityCategory(activityName); int
	 * activityId=getActivityid(activityName);
	 * 
	 * if(!(uniqueActivityIds.contains(activityId))) {
	 * ConnectDatabase.insertIntoActivityDimension(activityId,activityName,activityCategory);
	 * uniqueActivityIds.add(activityId); }
	 * 
	 * LocationObject locationObject=LocationObject.getLocationObjectInstance(userId,99); // dummy, not available in
	 * data currently
	 * 
	 * timeId=Integer.parseInt( Integer.toString(timestamp.getHours())+ Integer.toString(timestamp.getMinutes())+
	 * Integer.toString(endHour)+ Integer.toString(endMinute));
	 * 
	 * stmt.setString(1,currentImageName); stmt.setString(2,userNames[userIterator]);
	 * stmt.setTimestamp(3,getTimestamp(currentImageName));
	 * stmt.setString(4,getCategory(userNames[userIterator],currentImageName)); stmt.setString(5,dayName);
	 * stmt.setDate(6,getDate(currentImageName)); stmt.setInt(7,1);
	 * 
	 * stmt.executeUpdate();
	 * 
	 * 
	 * System.out.print(" > ");
	 * 
	 * } br.close(); } catch (IOException e) { e.printStackTrace(); } ////
	 */

}