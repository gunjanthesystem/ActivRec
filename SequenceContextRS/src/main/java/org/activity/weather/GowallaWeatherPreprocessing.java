package org.activity.weather;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Timeline;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;

/**
 * Preprocessing the Gowalla checkin data to generata data in format more suitable for fetching weather data
 * corresonding to checkins
 * 
 * @author gunjan
 *
 */
public class GowallaWeatherPreprocessing
{
	static String commonPath;// = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/GowallaWeather/";
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug30/";
	static String checkinFileNameToRead;// = "";
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug22_2016/gw2CheckinsSpots1TargetUsersDatesOnly.csv";///
	// gw2CheckinsSpots1Slim1TargetUsersDatesOnly.csv";
	// static String newline = null;
	// String checkinFileNameToWrite =
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug22_2016/gw2CheckinsSpots1TargetUsersDatesOnlyWithLevels.csv";
	static String fileContainingAPIKeys;// =
										// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/GowallaWeather/ListOfAPIKeys.txt";
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug23/ListOfAPIKeys.txt";
	static long startIndexToRead;// = 0;
	static String startIndexToReadFileName = commonPath + "startIndexToRead.csv";
	static String newline;// = "\n";// = System.lineSeparator();

	static long httpRequestCount = 0;
	static final long httpRequestCountLimit = 50000;
	static final int decimalPlacesToKeepForLatLon = 3;
	static LocalDateTime currentDateTime;

	public static void GowallaWeatherPreprocessingController(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String commonPathToWrite)
	{
		writeLatLongRoundedDateSetFromTimelines(usersCleanedDayTimelines,
				commonPathToWrite + "LatLongRoundedDateSet.txt", decimalPlacesToKeepForLatLon, false);
	}

	public static void main(String args[])
	{
		commonPath = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/GowallaWeather/";
		fileContainingAPIKeys = commonPath + "ListOfAPIKeys.txt";
		startIndexToReadFileName = commonPath + "startIndexToRead.csv";

		// extractUserLatLongTSFromCheckins(checkinFileNameToRead, null);
		// newline = System.getProperty("line.separator");
		try
		{
			// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(commonPath + "consoleLog.txt");
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			currentDateTime = LocalDateTime.now();
			newline = System.lineSeparator();

			// to be set only once.
			// $$updateStartIndexToReadFile(0);

			// WritingToFile.writeToNewFile("0" + newline, startIndexToReadFileName);
			// .writeToFile(map, fullPath);

			// $$ Start of commenting useful code 1
			// // makeHTTPSRequest();
			// LinkedHashSet<String> latLonTSSet =
			// getLatLongRoundedTSSetFromCheckins(checkinFileNameToRead, ",", decimalPlacesToKeepForLatLon); //
			// "lat||lon||tsInJavaEpochSecs", to find number of unique queries
			// // for
			// // weather
			//
			// // checking sanity
			// System.out.println("allPresent = " +
			// areAllLatLongRoundedTSFromCheckinsPresentInSet(checkinFileNameToRead, latLonTSSet, ",",
			// decimalPlacesToKeepForLatLon));
			//
			// StringBuffer latLonTSSetAllString = new StringBuffer();
			//
			// latLonTSSet.stream().forEach(e -> latLonTSSetAllString.append(e.toString() + "\n"));//
			// WritingToFile.appendLineToFile(Se.toString() + "\ns", commonPath +
			// // "latLonTSSet"));
			//
			// WritingToFile.writeToNewFile(latLonTSSetAllString.toString(), commonPath + "latLonTSSet.csv");
			// $$ End of commenting useful code 1

			// $$ Start of commenting useful code 3
			// // makeHTTPSRequest();
			// LinkedHashSet<String> latLonDateSet =
			// getLatLongRoundedDateSetFromCheckins(checkinFileNameToRead, ",", decimalPlacesToKeepForLatLon); //
			// "lat||lon||tsInJavaEpochSecs", to find number of unique
			// // queries
			// // checking sanity
			// System.out.println("allPresent = " +
			// areAllLatLongRoundedDateFromCheckinsPresentInSet(checkinFileNameToRead, latLonDateSet, ",",
			// decimalPlacesToKeepForLatLon));
			//
			// StringBuffer latLonDateSetAllString = new StringBuffer();
			//
			// latLonDateSet.stream().forEach(e -> latLonDateSetAllString.append(e.toString() + "\n"));//
			// WritingToFile.appendLineToFile(Se.toString() + "\ns", commonPath +
			// // "latLonTSSet"));
			//
			// WritingToFile.writeToNewFile(latLonDateSetAllString.toString(), commonPath + "latLonDateSet.csv");
			// $$ End of commenting useful code 3

			// // $$ Start of commenting useful code 2
			// String fetchedWeatherFileName = commonPath + " fetchedWeatherData" +
			// currentDateTime.getMonth().toString().substring(0, 3)
			// + currentDateTime.getDayOfMonth() + ".json";
			String fetchedWeatherFileName = commonPath + "fetchedWeatherDataDecAll.json";

			String inputLatLonTSDataFileName = commonPath + "LatLongRoundedDateSet.txt";// "latLonTSSet.csv";

			List<String> APIKeys = ReadingFromFile.oneColumnReaderString(fileContainingAPIKeys, ",", 0, false);
			System.out.println("Num of API Keys = " + APIKeys);
			// // fetchWeatherData(inputLatLonTSDataFileName, APIKeys, fetchedWeatherFileName);
			List<String> inputLatLonTSData = Files.lines(Paths.get(inputLatLonTSDataFileName))
					.collect(Collectors.toList());

			fetchWeatherData(inputLatLonTSData, APIKeys, fetchedWeatherFileName, startIndexToReadFileName);

			// // data.stream().limit(10).forEach(e -> System.out.println(e.toString()));
			// // APIKeys.stream().forEach(APIKey -> System.out.println("API Key = " + APIKey));
			// // APIKeys.stream().forEach(APIKey -> fetchWeatherData(inputLatLonTSDataFileName, APIKey,
			// fetchedWeatherFileName));
			// // fetchWeatherData(commonPath + "latLonTSSet.csv", "cab767fc679c753b9db70fcbc020462c", commonPath +
			// "fetchedWeatherData.json");
			// // $$ End of commenting useful code 2

			// $$checkWeatherUpdateConcern1();
			// consoleLogStream.close();

			PopUps.showMessage("httpRequestCount = " + httpRequestCount);
			System.out.println("done");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public static void checkWeatherUpdateConcern1()
	{
		try
		{
			List<String> APIKeys = ReadingFromFile.oneColumnReaderString(fileContainingAPIKeys, ",", 0, false);
			System.out.println("Num of API Keys = " + APIKeys);

			String fetchedWeatherFileName = commonPath + "fetchedWeatherData"
					+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + ".json";
			String inputLatLonTSDataFileName = "/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug30/sampleConstructed.csv";

			// fetchWeatherData(inputLatLonTSDataFileName, APIKeys, fetchedWeatherFileName);

			List<String> inputLatLonTSData = Files.lines(Paths.get(inputLatLonTSDataFileName))
					.collect(Collectors.toList());

			fetchWeatherData(inputLatLonTSData, APIKeys, fetchedWeatherFileName, startIndexToReadFileName);

			// data.stream().limit(10).forEach(e -> System.out.println(e.toString()));
			// APIKeys.stream().forEach(APIKey -> System.out.println("API Key = " + APIKey));
			// APIKeys.stream().forEach(APIKey -> fetchWeatherData(inputLatLonTSDataFileName, APIKey,
			// fetchedWeatherFileName));
			// fetchWeatherData(commonPath + "latLonTSSet.csv", "cab767fc679c753b9db70fcbc020462c", commonPath +
			// "fetchedWeatherData.json");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param index
	 */
	public static void updateStartIndexToReadFile(int index)
	{
		WToFile.writeToNewFile(index + newline, startIndexToReadFileName);
		// PopUps.showMessage("index to read next = " + index);
	}

	/**
	 *
	 * @param latLonTSInputData
	 * @param APIKeys
	 * @param absolutePathForResult
	 * @param startIndexToRead
	 */
	public static void fetchWeatherData(List<String> latLonTSInputData, List<String> APIKeys,
			String absolutePathForResult, String startIndexToReadFileName)
	{
		String baseString = "https://api.forecast.io/forecast/";// + APIKey + "/";
		String optionsString = "?units=si";
		String apiRequestString = new String();
		final String newline = System.lineSeparator();
		String latLonTSInput;
		int countOfLinesRead = 0;

		try
		{
			for (String APIKey : APIKeys)
			{
				int startIndexToReadNext = Integer
						.valueOf(CSVUtils.getCellValueFromCSVFile(1, 1, startIndexToReadFileName));
				// PopUps.showMessage("Start index to read " + startIndexToReadNext);
				System.out.println("Start index to read " + startIndexToReadNext);
				// String allResult = new String();

				StringBuilder toWrite = new StringBuilder();
				int countOfLinesToWrite = 0;
				while (startIndexToReadNext < latLonTSInputData.size())
				{
					latLonTSInput = latLonTSInputData.get(startIndexToReadNext);

					apiRequestString = baseString + APIKey + "/" + latLonTSInput + optionsString;
					String obtainedResultString = makeHttpsRequest(apiRequestString);// + newline;

					// PopUps.showMessage("obtainedResultString.length = " + obtainedResultString.length());
					// PopUps.showMessage("obtainedResultString = " + obtainedResultString);

					if (obtainedResultString.length() < 1)// should detect if the rate limit has exceeded, right now not
															// sure of rate limit exceeded msg
					{
						// PopUps.showMessage("Rate limit exceeded, breaking");
						System.out.println("Rate limit exceeded, breaking");
						// updateStartIndexToReadFile(startIndexToReadNext);
						httpRequestCount = 0;// resetting httpRequest count for next API Key
						// startIndexToReadNext += 1;

						// write whatever is in buffer
						WToFile.appendLineToFileAbs(toWrite.toString(), absolutePathForResult);
						toWrite.setLength(0);

						break;
					}

					String keyToIdentify = String.valueOf(startIndexToReadNext + 1);
					String resultToWrite = "{\"IN\":" + keyToIdentify + ", \"FW\":" + obtainedResultString + "}"
							+ newline;
					// allResult = resultToWrite;// countOfLinesRead + "-" + latLonTSInput + "," +
					// obtainedResultString);// index-LatLonTS,{jsonResult}
					countOfLinesToWrite += 1;
					startIndexToReadNext += 1;

					toWrite.append(resultToWrite);

					if (countOfLinesToWrite % 20 == 0)
					{
						WToFile.appendLineToFileAbs(toWrite.toString(), absolutePathForResult);
						toWrite.setLength(0);
						updateStartIndexToReadFile(startIndexToReadNext);
					}
				}
				updateStartIndexToReadFile(startIndexToReadNext);
				// WritingToFile.appendLineToFileAbsolute(allResult.toString(), absolutePathForResult);
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	//
	/**
	 * 
	 * @param absFileNameOfDataToRead
	 * @param APIKey
	 * @param absolutePathForResult
	 */
	public static void fetchWeatherData(String absFileNameOfDataToRead, List<String> APIKeys,
			String absolutePathForResult)
	{

		String baseString = "https://api.forecast.io/forecast/";// + APIKey + "/";
		String optionsString = "?units=si";
		String apiRequestString = new String();
		final String newline = System.lineSeparator();

		StringBuffer allResult = new StringBuffer();
		String latLonTSInput;

		int countOfLinesRead = 0;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(absFileNameOfDataToRead));

			// for (String APIKey : APIKeys)
			String APIKey = APIKeys.get(0);
			{

				while ((latLonTSInput = br.readLine()) != null)
				// && countOfLinesRead % 2 == 0)
				{
					countOfLinesRead += 1;

					// if (countOfLinesRead > 2)
					// {
					// break;
					// }
					if (countOfLinesRead % 100 == 0)
					{
						System.out.println(countOfLinesRead + " lines read");
					}

					apiRequestString = baseString + APIKey + "/" + latLonTSInput + optionsString;
					String obtainedResultString = makeHttpsRequest(apiRequestString) + newline;

					String keyToIdentify = String.valueOf(countOfLinesRead);// + "|" + latLonTSInput;

					String resultToWrite = "{\"IN\":" + keyToIdentify + ", \"FW\":" + obtainedResultString + "}";
					allResult.append(resultToWrite);// countOfLinesRead + "-" + latLonTSInput + "," +
													// obtainedResultString);// index-LatLonTS,{jsonResult}
				}
			}
			br.close();
			WToFile.writeToNewFile(allResult.toString(), absolutePathForResult);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param absFileNameOfDataToRead
	 * @param APIKey
	 * @param absolutePathForResult
	 */
	public static void fetchWeatherData(String absFileNameOfDataToRead, String APIKey, String absolutePathForResult)
	{
		String baseStringWithKey = "https://api.forecast.io/forecast/" + APIKey + "/";
		String optionsString = "?units=si";
		String apiRequestString = new String();
		final String newline = System.lineSeparator();

		StringBuffer allResult = new StringBuffer();
		String lineRead;

		int countOfLinesRead = 0;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(absFileNameOfDataToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLinesRead += 1;

				// if (countOfLinesRead > 1000)
				// {
				// break;
				// }
				if (countOfLinesRead % 100 == 0)
				{
					System.out.println(countOfLinesRead + " lines read");
				}

				apiRequestString = baseStringWithKey + lineRead + optionsString;
				String obtainedResultString = makeHttpsRequest(apiRequestString) + newline;
				allResult.append(countOfLinesRead + "-" + lineRead + "," + obtainedResultString);// index-LatLonTS,{jsonResult}
			}
			br.close();
			WToFile.writeToNewFile(allResult.toString(), absolutePathForResult);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Forecast.io requests are to be of the form
	 * "https://api.forecast.io/forecast/APIKEY/LATITUDE,LONGITUDE,TIMEInUnixEpochSecs" </br>
	 * ref:https://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
	 * 
	 * @param httpsURL
	 */
	public static String makeHttpsRequest(String httpsURL)
	{
		// String resultString = null;
		StringBuffer resultStringBuffer = new StringBuffer();
		httpRequestCount += 1;

		if (httpRequestCount <= httpRequestCountLimit)
		{

			try
			{
				URL url = new URL(httpsURL);
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null)
				{
					resultStringBuffer.append(inputLine);
				}

				in.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return resultStringBuffer.toString();
	}

	/**
	 * Checks if all the "lat||lon||tsInJavaEpochSecs" present in the checking data are present in the set returned from
	 * getLatLongTSSetFromCheckins()
	 * 
	 * @param fileNameToRead
	 * @param fileNameToRead
	 */
	public static boolean areAllLatLongRoundedDateFromCheckinsPresentInSet(String fileNameToRead,
			LinkedHashSet<String> latLonDateSet, String delimiter, int decimalPlacesToKeep)
	{
		boolean areAllPresent = true;

		int countOfLinesRead = 0;

		StringBuffer sbuf = new StringBuffer();
		BufferedReader br = null;
		String lineRead;

		System.out.println("Num of unique \"latLonTS\" with lat,lon rounded to " + decimalPlacesToKeep
				+ " decimal places = latLonTSSet.size() = " + latLonDateSet.size());

		long numOfLatLongDateReadButNotInSet = 0;
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLinesRead += 1;

				if (countOfLinesRead == 1) // skip the first
				{
					continue;
				}

				String[] splittedLine = lineRead.split(",");

				// 1,3,5,6
				Integer userID = Integer.valueOf(splittedLine[1]);// .replaceAll("\"", ""));

				String date = splittedLine[3].replaceAll("\"", "");

				String lat = StatsUtils.round(splittedLine[5], decimalPlacesToKeep);// .replaceAll("\"", ""));
				String lon = StatsUtils.round(splittedLine[6], decimalPlacesToKeep);

				if (latLonDateSet.contains(lat + delimiter + lon + delimiter + date) == false)
				{
					numOfLatLongDateReadButNotInSet += 1;
				}
			}

			if (numOfLatLongDateReadButNotInSet > 0)
			{
				areAllPresent = false;
			}

			System.out.println("Num of checkins = " + (countOfLinesRead - 1));
			System.out.println("Num of checkins  - Num of unique rounded\"latLonDate\" = "
					+ ((countOfLinesRead - 1) - latLonDateSet.size()));

			System.out.println("numOfLatLongDateReadButNotInSet = " + numOfLatLongDateReadButNotInSet);
			System.out.println("areAllPresent = " + areAllPresent);
			System.out.println("-------------");

			// latLonTSSet.stream().limit(10).forEach(e -> System.out.println(e));

			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}

		return areAllPresent;

	}

	/**
	 * Checks if all the "lat||lon||tsInJavaEpochSecs" present in the checking data are present in the set returned from
	 * getLatLongTSSetFromCheckins()
	 * 
	 * @param fileNameToRead
	 * @param fileNameToRead
	 */
	public static boolean areAllLatLongRoundedTSFromCheckinsPresentInSet(String fileNameToRead,
			LinkedHashSet<String> latLonTSSet, String delimiter, int decimalPlacesToKeep)
	{
		boolean areAllPresent = true;

		int countOfLinesRead = 0;

		StringBuffer sbuf = new StringBuffer();
		BufferedReader br = null;
		String lineRead;

		System.out.println("Num of unique \"latLonTS\" with lat,lon rounded to " + decimalPlacesToKeep
				+ " decimal places = latLonTSSet.size() = " + latLonTSSet.size());

		long numOfLatLongTSReadButNotInSet = 0;
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLinesRead += 1;

				if (countOfLinesRead == 1) // skip the first
				{
					continue;
				}

				String[] splittedLine = lineRead.split(",");

				// 1,3,5,6
				Integer userID = Integer.valueOf(splittedLine[1]);// .replaceAll("\"", ""));

				String timestampString = splittedLine[3].replaceAll("\"", "");
				Timestamp ts = DateTimeUtils.getTimestampFromISOString(timestampString);
				Instant instant = Instant.parse(timestampString);
				long epochSeconds = instant.getEpochSecond();

				String lat = StatsUtils.round(splittedLine[5], decimalPlacesToKeep);// .replaceAll("\"", ""));
				String lon = StatsUtils.round(splittedLine[6], decimalPlacesToKeep);

				if (latLonTSSet.contains(lat + delimiter + lon + delimiter + epochSeconds) == false)
				{
					numOfLatLongTSReadButNotInSet += 1;
				}
			}

			if (numOfLatLongTSReadButNotInSet > 0)
			{
				areAllPresent = false;
			}

			System.out.println("Num of checkins = " + (countOfLinesRead - 1));
			System.out.println("Num of checkins  - Num of unique rounded\"latLonTS\" = "
					+ ((countOfLinesRead - 1) - latLonTSSet.size()));

			System.out.println("numOfLatLongTSReadButNotInSet = " + numOfLatLongTSReadButNotInSet);
			System.out.println("areAllPresent = " + areAllPresent);
			System.out.println("-------------");

			// latLonTSSet.stream().limit(10).forEach(e -> System.out.println(e));

			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}

		return areAllPresent;

	}

	/**
	 * Checks if all the "lat||lon||tsInJavaEpochSecs" present in the checking data are present in the set returned from
	 * getLatLongTSSetFromCheckins()
	 * 
	 * @param fileNameToRead
	 * @param fileNameToRead
	 */
	public static boolean areAllLatLongTSFromCheckinsPresentInSet(String fileNameToRead,
			LinkedHashSet<String> latLonTSSet, String delimiter)
	{
		boolean areAllPresent = true;

		int countOfLinesRead = 0;

		StringBuffer sbuf = new StringBuffer();
		BufferedReader br = null;
		String lineRead;

		System.out.println("Num of unique \"latLonTS\" = latLonTSSet.size() = " + latLonTSSet.size());

		long numOfLatLongTSReadButNotInSet = 0;
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLinesRead += 1;

				if (countOfLinesRead == 1) // skip the first
				{
					continue;
				}

				String[] splittedLine = lineRead.split(",");

				// 1,3,5,6
				Integer userID = Integer.valueOf(splittedLine[1]);// .replaceAll("\"", ""));

				String timestampString = splittedLine[3].replaceAll("\"", "");
				Timestamp ts = DateTimeUtils.getTimestampFromISOString(timestampString);
				Instant instant = Instant.parse(timestampString);
				long epochSeconds = instant.getEpochSecond();

				String lat = splittedLine[5];// .replaceAll("\"", ""));
				String lon = splittedLine[6];

				if (latLonTSSet.contains(lat + delimiter + lon + delimiter + epochSeconds) == false)
				{
					numOfLatLongTSReadButNotInSet += 1;
				}
			}

			if (numOfLatLongTSReadButNotInSet > 0)
			{
				areAllPresent = false;
			}

			System.out.println("Num of checkins = " + (countOfLinesRead - 1));
			System.out.println(
					"Num of checkins  - Num of unique \"latLonTS\" = " + ((countOfLinesRead - 1) - latLonTSSet.size()));

			System.out.println("numOfLatLongTSReadButNotInSet = " + numOfLatLongTSReadButNotInSet);
			System.out.println("areAllPresent = " + areAllPresent);
			System.out.println("-------------");

			// latLonTSSet.stream().limit(10).forEach(e -> System.out.println(e));

			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}

		return areAllPresent;

	}

	/**
	 * Write "rounded lat,rounded long,date" set extracted from the given timelines
	 * 
	 * @param usersCleanedDayTimelines
	 * 
	 */
	public static void writeLatLongRoundedDateSetFromTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String fileNameToWrite,
			int numOfDecimalPlacesToKeep, boolean dateOrTimestamp)
	{
		LinkedHashSet<String> latLongDate = new LinkedHashSet<String>();
		LinkedHashMap<String, Integer> userNumOfActs = new LinkedHashMap<String, Integer>();

		int numOfUsers = 0, numOfActivityObjectsOverAllUsers = 0;

		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				numOfUsers += 1;
				int numOfActivityObjectForThisUser = 0;

				for (Entry<Date, Timeline> dateEntryForThisUser : userEntry.getValue().entrySet())
				{
					Date date = dateEntryForThisUser.getKey();
					Timeline timeline = dateEntryForThisUser.getValue();

					for (ActivityObject2018 aos : timeline.getActivityObjectsInTimeline())
					{
						numOfActivityObjectsOverAllUsers += 1;
						numOfActivityObjectForThisUser += 1;

						String lat = StatsUtils.round(aos.getStartLatitude(), numOfDecimalPlacesToKeep);
						String lon = StatsUtils.round(aos.getStartLongitude(), numOfDecimalPlacesToKeep);

						if (dateOrTimestamp == true)
						{
							latLongDate.add(lat + "," + lon + "," + date);
						}
						else
						{
							java.util.Date utilDate = new java.util.Date(date.getTime());
							long epochSeconds = utilDate.toInstant().getEpochSecond();
							long toUseEpochSeconds = epochSeconds + 7 * 60 * 60; // 7 am on that day
							latLongDate.add(lat + "," + lon + "," + toUseEpochSeconds);
						}
					}
				}
			}

			System.out.println("size of latLongDate = " + latLongDate.size());
			System.out.println("num of aos over all users = " + numOfActivityObjectsOverAllUsers);

			BufferedWriter bw = WToFile.getBWForNewFile(fileNameToWrite);

			// StringBuilder sb = new StringBuilder();
			for (String st : latLongDate)
			{
				bw.append(st + "\n");
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	public static LinkedHashSet<String> getLatLongRoundedDateSetFromTimelines()
	{
		return null;

	}

	/**
	 * Return set of unique "lat||lon||tsInJavaEpochSecs" in the checkins
	 * 
	 * @param fileNameToRead
	 * @return
	 */
	public static LinkedHashSet<String> getLatLongRoundedDateSetFromCheckins(String fileNameToRead, String delimiter,
			int numOfDecimalPlacesToKeep)
	{
		int countOfLinesRead = 0;
		BufferedReader br = null;
		String lineRead;

		LinkedHashSet<String> latLonTSSet = new LinkedHashSet<>(); // "lat||lon||tsInJavaEpochSecs", to find number of
																	// unique queries for weather
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLinesRead += 1;

				if (countOfLinesRead == 1) // skip the first
				{
					continue;
				}

				String[] splittedLine = lineRead.split(",");

				// String timestampString = splittedLine[3].replaceAll("\"", "");
				// Instant instant = Instant.parse(timestampString);
				// Timestamp ts = Timestamp.from(instant);
				String date = splittedLine[4].replaceAll("\"", "");
				// ts.toLocalDateTime().toLocalDate().toString();
				// long epochSeconds = instant.getEpochSecond();

				String lat = StatsUtils.round(splittedLine[5], numOfDecimalPlacesToKeep);
				String lon = StatsUtils.round(splittedLine[6], numOfDecimalPlacesToKeep);

				latLonTSSet.add(lat + delimiter + lon + delimiter + date);
			}
			br.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return latLonTSSet;

	}

	/**
	 * Return set of unique "lat||lon||tsInJavaEpochSecs" in the checkins
	 * 
	 * @param fileNameToRead
	 * @return
	 */
	public static LinkedHashSet<String> getLatLongRoundedTSSetFromCheckins(String fileNameToRead, String delimiter,
			int numOfDecimalPlacesToKeep)
	{
		int countOfLinesRead = 0;
		BufferedReader br = null;
		String lineRead;

		LinkedHashSet<String> latLonTSSet = new LinkedHashSet<>(); // "lat||lon||tsInJavaEpochSecs", to find number of
																	// unique queries for weather
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLinesRead += 1;

				if (countOfLinesRead == 1) // skip the first
				{
					continue;
				}

				String[] splittedLine = lineRead.split(",");

				String timestampString = splittedLine[3].replaceAll("\"", "");
				Instant instant = Instant.parse(timestampString);
				long epochSeconds = instant.getEpochSecond();

				String lat = StatsUtils.round(splittedLine[5], numOfDecimalPlacesToKeep);
				String lon = StatsUtils.round(splittedLine[6], numOfDecimalPlacesToKeep);

				latLonTSSet.add(lat + delimiter + lon + delimiter + epochSeconds);
			}
			br.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return latLonTSSet;

	}

	/**
	 * Return set of unique "lat||lon||tsInJavaEpochSecs" in the checkins
	 * 
	 * @param fileNameToRead
	 * @return
	 */
	public static LinkedHashSet<String> getLatLongTSSetFromCheckins(String fileNameToRead, String delimiter)
	{
		int countOfLinesRead = 0;
		BufferedReader br = null;
		String lineRead;

		LinkedHashSet<String> latLonTSSet = new LinkedHashSet<>(); // "lat||lon||tsInJavaEpochSecs", to find number of
																	// unique queries for weather
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));

			while ((lineRead = br.readLine()) != null)
			{
				countOfLinesRead += 1;

				if (countOfLinesRead == 1) // skip the first
				{
					continue;
				}

				String[] splittedLine = lineRead.split(",");

				String timestampString = splittedLine[3].replaceAll("\"", "");
				Instant instant = Instant.parse(timestampString);
				long epochSeconds = instant.getEpochSecond();

				String lat = splittedLine[5];// .replaceAll("\"", ""));
				String lon = splittedLine[6];

				latLonTSSet.add(lat + delimiter + lon + delimiter + epochSeconds);
			}
			br.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return latLonTSSet;

	}

	/**
	 * Convert the timestamps in checkin data from iso8601 format to unix epochs
	 * 
	 * @param fileNameToRead
	 * @param fileNameToRead
	 */
	public static void convertCheckinDataToUnixEpochs(String fileNameToRead, String fileNameToWrite)
	{

	}

	/**
	 * Extract (userid,lat,lon,timestampinunixepochs) from checkins.
	 * 
	 * @param fileNameToRead
	 * @param fileNameToRead
	 */
	public static void extractUserLatLongTSFromCheckins(String fileNameToRead, String fileNameToWrite)
	{
		int countOfLinesRead = 0;

		StringBuffer sbuf = new StringBuffer();
		BufferedReader br = null;
		String lineRead;

		Set<String> latLonTSSet = new LinkedHashSet<String>(); // "lat||lon||tsInJavaEpochSecs", to find number of
																// unique queries for weather
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));
			// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileNameToWrite);

			while ((lineRead = br.readLine()) != null)
			{
				countOfLinesRead += 1;

				if (countOfLinesRead == 1) // skip the first
				{
					continue;
				}

				// if (countOfLinesRead > 10) // skip the first
				// {
				// break;
				// }

				String[] splittedLine = lineRead.split(",");

				// 1,3,5,6
				Integer userID = Integer.valueOf(splittedLine[1]);// .replaceAll("\"", ""));

				String timestampString = splittedLine[3].replaceAll("\"", "");
				Timestamp ts = DateTimeUtils.getTimestampFromISOString(timestampString);
				Instant instant = Instant.parse(timestampString);
				long epochSeconds = instant.getEpochSecond();

				String lat = splittedLine[5];// .replaceAll("\"", ""));
				String lon = splittedLine[6];

				if (countOfLinesRead <= 10)
				{
					System.out.println(userID + "," + timestampString + "--" + ts.toString() + "--" + epochSeconds + ","
							+ lat + "," + lon);
				}

				latLonTSSet.add(lat + "||" + lon + "||" + epochSeconds);
			}

			System.out.println("num of line read = " + countOfLinesRead);

			System.out.println("Num of checkins = " + (countOfLinesRead - 1));
			System.out.println("Num of unique \"latLonTS\" = latLonTSSet.size() = " + latLonTSSet.size());

			System.out.println(
					"Num of checkins  - Num of unique \"latLonTS\" = " + ((countOfLinesRead - 1) - latLonTSSet.size()));

			System.out.println("-------------");
			latLonTSSet.stream().limit(10).forEach(e -> System.out.println(e));

			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}

	}

}