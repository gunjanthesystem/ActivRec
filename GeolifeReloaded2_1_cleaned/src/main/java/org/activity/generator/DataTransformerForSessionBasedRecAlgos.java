package org.activity.generator;

import java.io.PrintStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.PathConstants;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.LeakyBucket;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;

/**
 * To create sessionIDs for Jupyter Baselines
 * 
 * @author gunjan
 * @since 28 Dec 2018
 */
public class DataTransformerForSessionBasedRecAlgos
{

	static LinkedHashMap<String, Integer> uniqueUserIdDatesSessionIdMap;

	static final String sessionDelimiter = "|";

	public static LinkedHashMap<String, Integer> getUniqueUserIdDatesSessionIdMap()
	{
		return uniqueUserIdDatesSessionIdMap;
	}

	/**
	 * Convert timelines to convertToMQInputFormat
	 * 
	 * @param args
	 */
	public static void main(String args[])
	{

		String databaseName = "geolife1";
		String commonPathToWrite = "./dataWritten/" + databaseName + "ForSessionBasedBaselines"
				+ DateTimeUtils.getMonthDateHourMinLabel() + "/";

		try
		{
			WToFile.createDirectory(commonPathToWrite);
			PrintStream consoleStream = WToFile.redirectConsoleOutput(commonPathToWrite + "consoleLog.txt");
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = PathConstants
					.deserializeAndGetCleanedTimelines28Dec(databaseName);
			System.out.println("usersCleanedDayTimelines.size() = " + usersCleanedDayTimelines.size());

			convertToMQInputFormat(usersCleanedDayTimelines, commonPathToWrite,
					commonPathToWrite + databaseName + "sessions.csv", null);

			consoleStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * <ul>
	 * <li>SessionID: userID|date --> converted to unique numeric numbers
	 * <li>UserID: userID (numeric)
	 * <li>SongID: actID (numeric)
	 * <li>TS: startTS (numeric)
	 * <li>Playtime: duration if avaialable, or duration from previous, or constant val .
	 * </ul>
	 * session_id,user_id,song_id,ts,playtime</br>
	 * 1902204,4,16,1421163674,274</br>
	 * 1902204,4,17,1421163948,250</br>
	 * 
	 * @param usersCleanedDayTimelines
	 * @param commonPath
	 * @param absFileNameToWrite
	 * @param set
	 */
	public static void convertToMQInputFormat(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String commonPath,
			String absFileNameToWrite, Set<String> sampledUsersToMakeRecommFor)
	{

		// get all the unique userID-Dates
		LinkedHashSet<String> uniqueUserIdDates = getAllUniqueUserIdDates(usersCleanedDayTimelines, sessionDelimiter);

		// assign session ID
		uniqueUserIdDatesSessionIdMap = assignSessionID(commonPath, uniqueUserIdDates);

		WToFile.writeToNewFile(sampledUsersToMakeRecommFor.stream().collect(Collectors.joining("\n")),
				commonPath + "SampledUserIDs.csv");

		writeInMQFormat(uniqueUserIdDatesSessionIdMap, usersCleanedDayTimelines, commonPath, absFileNameToWrite,
				sessionDelimiter);
	}

	/**
	 * session_id,user_id,song_id,ts,playtime</br>
	 * 1902204,4,16,1421163674,274</br>
	 * 1902204,4,17,1421163948,250</br>
	 * 
	 * @param uniqueUserIdDatesSessionIdMap
	 * @param usersCleanedDayTimelines
	 * @param commonPath
	 * @param absFileNameToWrite
	 * @param sessionIdDelimter
	 */
	private static void writeInMQFormat(LinkedHashMap<String, Integer> uniqueUserIdDatesSessionIdMap,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String commonPath,
			String absFileNameToWrite, String sessionIdDelimter)
	{
		LeakyBucket lb = new LeakyBucket(2000, absFileNameToWrite, false);
		lb.addToLeakyBucketWithNewline("session_id,user_id,song_id,ts,playtime");
		long expectedNumOfLines = 0;

		System.out.println("User ID = " + usersCleanedDayTimelines.keySet());
		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
		{
			String userId = userEntry.getKey();
			for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
			{
				String sessionIdKey = userId + sessionIdDelimter + dateEntry.getKey().toString();
				Integer sessionId = uniqueUserIdDatesSessionIdMap.get(sessionIdKey);
				if (sessionId == null)
				{
					PopUps.printTracedErrorMsgWithExit("Error: session not found for sessionKey: " + sessionIdKey);
				}

				for (ActivityObject2018 aoEntry : dateEntry.getValue().getActivityObjectsInTimeline())
				{
					expectedNumOfLines += 1;
					lb.addToLeakyBucketWithNewline(sessionId + "," + userId + "," + aoEntry.getActivityID() + ","
							+ (long) (aoEntry.getStartTimestampInms() / 1000) + "," + aoEntry.getDurationInSeconds());
				}
			}
		}
		lb.flushLeakyBucket();
		System.out.println("expectedNumOfLines = " + expectedNumOfLines);
	}

	/**
	 * 
	 * @param commonPath
	 * @param uniqueUserIdDates
	 * @return
	 */
	private static LinkedHashMap<String, Integer> assignSessionID(String commonPath,
			LinkedHashSet<String> uniqueUserIdDates)
	{
		LinkedHashMap<String, Integer> uniqueUserIdDatesSessionIdMap = new LinkedHashMap<>(uniqueUserIdDates.size());
		int sessionID = 0;
		for (String uniqueUserIdDate : uniqueUserIdDates)
		{
			uniqueUserIdDatesSessionIdMap.put(uniqueUserIdDate, ++sessionID);
		}

		System.out.println("uniqueUserIdDatesSessionIdMap.size() = " + uniqueUserIdDatesSessionIdMap.size());
		Serializer.kryoSerializeThis(uniqueUserIdDatesSessionIdMap, commonPath + "uniqueUserIdDatesSessionIdMap.kryo");
		LinkedHashMap<String, Integer> uniqueUserIdDatesSessionIdMapDe = (LinkedHashMap<String, Integer>) Serializer
				.kryoDeSerializeThis(commonPath + "uniqueUserIdDatesSessionIdMap.kryo");
		System.out.println("uniqueUserIdDatesSessionIdMapDe.size() = " + uniqueUserIdDatesSessionIdMapDe.size());

		StringBuilder sb = new StringBuilder("UniqueUserIdDate,SessionIdAssigned\n");
		uniqueUserIdDatesSessionIdMap.entrySet().stream()
				.forEachOrdered(e -> sb.append(e.getKey() + "," + e.getValue() + "\n"));
		WToFile.writeToNewFile(sb.toString(), commonPath + "uniqueUserIdDatesSessionIdMap.csv");

		return uniqueUserIdDatesSessionIdMap;
	}

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param userIDDateDelimiter
	 * @return
	 */
	private static LinkedHashSet<String> getAllUniqueUserIdDates(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String userIDDateDelimiter)
	{
		LinkedHashSet<String> userIDDates = new LinkedHashSet<>();
		int numOfExpectedUserIDDates = 0;
		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
		{
			String userID = userEntry.getKey();

			for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
			{
				numOfExpectedUserIDDates += 1;
				String dateAsString = dateEntry.getKey().toString();
				userIDDates.add(userID + userIDDateDelimiter + dateAsString);
			}
		}
		System.out.println("numOfExpectedUserIDDates = " + numOfExpectedUserIDDates + " userIDDates.size()= "
				+ userIDDates + " isSane = " + (userIDDates.size() == numOfExpectedUserIDDates));

		return userIDDates;
	}

	/**
	 * 
	 * @param trainTimelinesAllUsersDWFiltrd
	 * @param trainTestTimelinesForAllUsersDW
	 * @param commonPath
	 */
	public static void writeSessionIDsForFilteredTrainAndTestDays(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> trainTimelinesAllUsersDWFiltrd,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW,
			String commonPath)
	{
		LinkedHashSet<Integer> trainSessionIDs = new LinkedHashSet<>();
		LinkedHashSet<Integer> testSessionIDs = new LinkedHashSet<>();

		if (uniqueUserIdDatesSessionIdMap == null || uniqueUserIdDatesSessionIdMap.size() == 0)
		{
			System.out.println(
					"Warning: doing nothing in writeSessionIDsForFilteredTrainAndTestDays as : uniqueUserIdDatesSessionIdMap ="
							+ uniqueUserIdDatesSessionIdMap);
		}
		else
		{
			// write trainSessionIDs
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : trainTimelinesAllUsersDWFiltrd.entrySet())
			{
				String userId = userEntry.getKey();
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					String sessionIdKey = userId + sessionDelimiter + dateEntry.getKey().toString();
					Integer sessionId = uniqueUserIdDatesSessionIdMap.get(sessionIdKey);
					if (sessionId == null)
					{
						PopUps.printTracedErrorMsgWithExit(
								"Error: train session not found for sessionKey: " + sessionIdKey);
					}
					trainSessionIDs.add(sessionId);
				}
			}

			// write testSessionIDs
			for (Entry<String, List<LinkedHashMap<Date, Timeline>>> userEntry : trainTestTimelinesForAllUsersDW
					.entrySet())
			{
				String userId = userEntry.getKey();
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().get(1).entrySet())
				{
					String sessionIdKey = userId + sessionDelimiter + dateEntry.getKey().toString();
					Integer sessionId = uniqueUserIdDatesSessionIdMap.get(sessionIdKey);
					if (sessionId == null)
					{
						PopUps.printTracedErrorMsgWithExit(
								"Error: test session not found for sessionKey: " + sessionIdKey);
					}
					testSessionIDs.add(sessionId);
				}
			}
		}

		PopUps.showMessage("trainSessionIDs.size() = " + trainSessionIDs.size() + "\ntestSessionIDs.size() = "
				+ testSessionIDs.size());
		WToFile.writeToNewFile(trainSessionIDs.stream().map(i -> String.valueOf(i)).collect(Collectors.joining("\n")),
				commonPath + "trainSessionIDs.csv");
		WToFile.writeToNewFile(testSessionIDs.stream().map(i -> String.valueOf(i)).collect(Collectors.joining("\n")),
				commonPath + "testSessionIDs.csv");
	}

	// start of added on 20 March 2019
	/**
	 * 
	 * @param trainTimelinesAllUsersDWFiltrd
	 * @param trainTestTimelinesForAllUsersDW
	 * @param commonPath
	 */
	public static void writeActivityCountsFilteredTrainAndTestDays(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> trainTimelinesAllUsersDWFiltrd,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW,
			String commonPath)
	{
		// TODO INCOMPLETE
		LinkedHashSet<Integer> trainSessionIDs = new LinkedHashSet<>();
		LinkedHashSet<Integer> testSessionIDs = new LinkedHashSet<>();

		if (uniqueUserIdDatesSessionIdMap == null || uniqueUserIdDatesSessionIdMap.size() == 0)
		{
			System.out.println(
					"Warning: doing nothing in writeSessionIDsForFilteredTrainAndTestDays as : uniqueUserIdDatesSessionIdMap ="
							+ uniqueUserIdDatesSessionIdMap);
		}
		else
		{
			// write trainSessionIDs
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : trainTimelinesAllUsersDWFiltrd.entrySet())
			{
				String userId = userEntry.getKey();
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					String sessionIdKey = userId + sessionDelimiter + dateEntry.getKey().toString();
					Integer sessionId = uniqueUserIdDatesSessionIdMap.get(sessionIdKey);
					if (sessionId == null)
					{
						PopUps.printTracedErrorMsgWithExit(
								"Error: train session not found for sessionKey: " + sessionIdKey);
					}
					trainSessionIDs.add(sessionId);
				}
			}

			// write testSessionIDs
			for (Entry<String, List<LinkedHashMap<Date, Timeline>>> userEntry : trainTestTimelinesForAllUsersDW
					.entrySet())
			{
				String userId = userEntry.getKey();
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().get(1).entrySet())
				{
					String sessionIdKey = userId + sessionDelimiter + dateEntry.getKey().toString();
					Integer sessionId = uniqueUserIdDatesSessionIdMap.get(sessionIdKey);
					if (sessionId == null)
					{
						PopUps.printTracedErrorMsgWithExit(
								"Error: test session not found for sessionKey: " + sessionIdKey);
					}
					testSessionIDs.add(sessionId);
				}
			}
		}

		PopUps.showMessage("trainSessionIDs.size() = " + trainSessionIDs.size() + "\ntestSessionIDs.size() = "
				+ testSessionIDs.size());
		WToFile.writeToNewFile(trainSessionIDs.stream().map(i -> String.valueOf(i)).collect(Collectors.joining("\n")),
				commonPath + "trainSessionIDs.csv");
		WToFile.writeToNewFile(testSessionIDs.stream().map(i -> String.valueOf(i)).collect(Collectors.joining("\n")),
				commonPath + "testSessionIDs.csv");
	}

	// end of added on 20 March 2019

	/**
	 * 
	 * @param validRtsAsUserIdDateEndTS
	 * @param commonPath
	 */
	public static void writeValidRTs(ArrayList<Triple<Integer, Date, Timestamp>> validRtsAsUserIdDateEndTS,
			String commonPath)
	{
		// SessionId,StartTimestamp in secs
		ArrayList<Pair<Integer, Long>> sessionIDSTInSecsOfValidRTs = new ArrayList<>();
		for (Triple<Integer, Date, Timestamp> e : validRtsAsUserIdDateEndTS)
		{
			Integer userId = e.getFirst();
			String sessionIdKey = userId + sessionDelimiter + e.getSecond().toString();
			long stTSInSecs = (long) e.getThird().getTime() / 1000;
			Integer sessionId = uniqueUserIdDatesSessionIdMap.get(sessionIdKey);
			if (sessionId == null)
			{
				PopUps.printTracedErrorMsgWithExit("Error: test session not found for sessionKey: " + sessionIdKey);
			}
			sessionIDSTInSecsOfValidRTs.add(new Pair<>(sessionId, stTSInSecs));
		}

		PopUps.showMessage("validRtsAsUserIdDateEndTS.size() = " + validRtsAsUserIdDateEndTS.size()
				+ "\nsessionIDSTInSecsOfValidRTs.size() = " + sessionIDSTInSecsOfValidRTs.size());

		WToFile.writeToNewFile(sessionIDSTInSecsOfValidRTs.stream().map(e -> e.getFirst() + "_" + e.getSecond())
				.collect(Collectors.joining("\n")), commonPath + "sessionIDSTInSecsOfValidRTs.csv");
	}

	/**
	 * 
	 * @param validRtsAsUserIdDateEndTS
	 * @param commonPath
	 */
	public static void writeValidRTs2(ArrayList<Triple<Integer, Date, Integer>> validRtsAsUserIdDateEndTS,
			String commonPath)
	{
		// SessionId,StartTimestamp in secs
		ArrayList<Pair<Integer, Integer>> sessionIDSTInSecsOfValidRTs = new ArrayList<>();
		for (Triple<Integer, Date, Integer> e : validRtsAsUserIdDateEndTS)
		{
			Integer userId = e.getFirst();
			String sessionIdKey = userId + sessionDelimiter + e.getSecond().toString();
			int stTSInSecs = e.getThird();
			Integer sessionId = uniqueUserIdDatesSessionIdMap.get(sessionIdKey);
			if (sessionId == null)
			{
				PopUps.printTracedErrorMsgWithExit("Error: test session not found for sessionKey: " + sessionIdKey);
			}
			sessionIDSTInSecsOfValidRTs.add(new Pair<>(sessionId, stTSInSecs));
		}

		PopUps.showMessage("validRtsAsUserIdDateEndTS.size() = " + validRtsAsUserIdDateEndTS.size()
				+ "\nsessionIDSTInSecsOfValidRTs.size() = " + sessionIDSTInSecsOfValidRTs.size());

		WToFile.writeToNewFile(sessionIDSTInSecsOfValidRTs.stream().map(e -> e.getFirst() + "_" + e.getSecond())
				.collect(Collectors.joining("\n")), commonPath + "sessionIDSTInSecsOfValidRTs.csv");
	}

}
