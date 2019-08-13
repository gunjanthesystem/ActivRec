package org.activity.weather;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.TimeZone;

import org.activity.io.WToFile;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONProcessing
{
	final static String commonPath = "/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug31/temp/";

	public static void main(String args[])
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		ArrayList<JSONArray> arrayOfHourlyArrays = extractHourlyDataFromForecastIOJSON(
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug30/ fetchedWeatherDataAUG30.json",
				commonPath + "HourlyForDifferentFetchTime.json", true);
		ArrayList<JSONObject> arrayOfCurrWeatherObjs = extractCurrentlyDataFromForecastIOJSON(
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug30/ fetchedWeatherDataAUG30.json",
				commonPath + "CurrentlyForDifferentFetchTime.json");

		checkIntegrityOfCurrenlyDataInHourlyData(arrayOfHourlyArrays, arrayOfCurrWeatherObjs,
				commonPath + "checkIntegrityOfCurrenlyDataInHourlyData.txt");
		;
		// makeTimeReadableHourlyDataFromForecastIOJSON("/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug30/
		// fetchedWeatherDataAUG30.json",
		// commonPath + "ReadableHourlyForDifferentFetchTime.json");

		// getWeatherDataFromHourlyArray();

	}

	/**
	 * currently fetched correctly, verified by looking at raw data</br>
	 * corresponding hourly extracted correctly, verified by looking at raw data
	 * 
	 * @param arrayOfHourlyArrays
	 * @param arrayOfCurrWeatherObjs
	 */
	public static void checkIntegrityOfCurrenlyDataInHourlyData(ArrayList<JSONArray> arrayOfHourlyArrays,
			ArrayList<JSONObject> arrayOfCurrWeatherObjs, String fileNameToWrite)
	{
		int sizeOfArrOfHrArray = arrayOfHourlyArrays.size();
		int sizeOfArrCurrObjs = arrayOfCurrWeatherObjs.size();
		System.out.println("Size of array of hourly arrays = " + arrayOfHourlyArrays.size());
		System.out.println("Size of array of currently objects = " + arrayOfCurrWeatherObjs.size());
		StringBuffer res = new StringBuffer();

		if (sizeOfArrOfHrArray != sizeOfArrOfHrArray)
		{
			System.err.println("Error in checkIntegrityOfCurrenlyDataInHourlyData");
		}

		for (int i = 0; i < sizeOfArrCurrObjs; i++)
		{
			System.out.println();
			JSONObject currObj = arrayOfCurrWeatherObjs.get(i);
			JSONObject currWeatherDataFromHourly = getWeatherDataFromHourlyArray(arrayOfHourlyArrays.get(i),
					currObj.getInt("time"));

			Instant currObjTime = Instant.ofEpochSecond(currObj.getInt("time"));
			Instant currWeatherDataFromHourlyTime = Instant.ofEpochSecond(currWeatherDataFromHourly.getInt("time"));

			System.out.println("                   currently time = " + currObjTime.toString());
			System.out.println("corresponding hr from hourly time = " + currWeatherDataFromHourlyTime.toString());

			res.append("                   currently time = " + currObjTime.toString() + "\n");
			res.append("corresponding hr from hourly time = " + currWeatherDataFromHourlyTime.toString() + "\n");

			JSONObject tempCurrObj = currObj;
			tempCurrObj.remove("time");

			JSONObject tempCurrWeatherDataFromHourly = currWeatherDataFromHourly;
			tempCurrWeatherDataFromHourly.remove("time");

			if (tempCurrObj.equals(tempCurrWeatherDataFromHourly))
			{
				System.out.println("~~~~~~~~~~~  currently same as hourly  ~~~~~~~~~~~");
				res.append("~~~~~~~~~~~  currently same as hourly  ~~~~~~~~~~~\n");
			}
			else
			{
				System.out.println("xxxxxxxxxx  CURRENT DIFFERENT FROM HOURLY  xxxxxxxxxx");
				System.out.println("currObj                   = " + currObj.toString());
				System.out.println("currWeatherDataFromHourly = " + currWeatherDataFromHourly.toString());

				res.append("CURRENT DIFFERENT FROM HOURLY xxxxxxxxxx" + "\n");
				res.append("                   currently data = " + currObj.toString() + "\n");
				res.append("corresponding hr from hourly data = " + currWeatherDataFromHourly.toString() + "\n");
			}

			if (i % 6 == 0)
			{
				System.out.println("\n");
				res.append("\n\n");
			}
			else
				res.append("\n");
		}

		WToFile.writeToNewFile(res.toString(), fileNameToWrite);

	}

	/**
	 * Takes in an array of json objects contains hourly data for a day and return the json object correpsonding to the
	 * given query timestamp in unix epoch seconds. </br>
	 * <font color="red">Assuming hourly data be at 1 hour gaps</font>
	 * 
	 * @param hourlyArray
	 * @param queryTimestamp
	 */
	public static JSONObject getWeatherDataFromHourlyArray(JSONArray hourlyArray, int queryEpochTimeInSecs)
	{
		JSONObject concernedHourWeatherData = new JSONObject();
		try
		{
			for (int i = 0; i < hourlyArray.length(); i++)
			{
				JSONObject currHrObj = hourlyArray.getJSONObject(i);
				int currEpochSecs = currHrObj.getInt("time");

				int diff = queryEpochTimeInSecs - currEpochSecs;

				if (diff < 3600 && diff > 0) // this is the hour concerned, difference <=59 mins and positive
				{
					concernedHourWeatherData = currHrObj;
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return concernedHourWeatherData;
	}

	/**
	 * Correctness of written data validated by looking at raw data.</br>
	 * Extracts currently data and writes the currently weather data with lat,lon,timestamp of weather query in json
	 * format
	 * 
	 * @param fileNameToRead
	 * @param fileNameToWrite
	 */
	public static ArrayList<JSONObject> extractCurrentlyDataFromForecastIOJSON(String fileNameToRead,
			String fileNameToWrite)
	{
		ArrayList<JSONObject> arrayOfCurrentWeatherObjects = new ArrayList<>();

		StringBuffer result = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			String lineRead;

			ArrayList<String> linesRead = new ArrayList<String>();

			while ((lineRead = br.readLine()) != null)
			{
				linesRead.add(lineRead);
			}

			System.out.println("Num of lines read: " + linesRead.size() + "\n\n"); // each line representig one weather
																					// query request

			int count = 0;

			JSONArray previousHourlyWeatherData = new JSONArray();

			System.out.println("[");
			result.append("[\n");
			for (String currentLine : linesRead) // each line is a json object
			{
				count++;
				// System.out.println("reading line: " + ++count);
				JSONObject jObj = new JSONObject(currentLine);
				Set<String> level0Keys = jObj.keySet();
				// System.out.println("Num of level0Keys = " + level0Keys.size());

				JSONObject fetchedWeather = jObj.getJSONObject("FW");

				JSONObject currentlyObj = fetchedWeather.getJSONObject("currently");
				arrayOfCurrentWeatherObjects.add(currentlyObj);

				int currentEpochSec = (int) currentlyObj.get("time");

				Instant tsInstant = Instant.ofEpochSecond(currentEpochSec);
				// LocalDateTime ldateTime = LocalDateTime.from(tsInstant);
				String queryInput = "\"lat\":" + fetchedWeather.get("latitude").toString() + "," + "\"lon\":"
						+ fetchedWeather.get("longitude").toString() + "," + "\"currentTimeEpoch\":"
						+ currentlyObj.get("time").toString() + "," + "\"currentTimeISO\":\"" + tsInstant.toString()
						+ "\",";

				String currentlyWeatherDataString = "\"currently\":" + currentlyObj.toString() + "";

				result.append("{" + queryInput + currentlyWeatherDataString + "}");
				System.out.println("{" + queryInput + currentlyWeatherDataString + "}");
				// System.out.println("hourlyWeatherData = " + hourlyWeatherData.toString());

				if (count != linesRead.size())
				{
					System.out.println(",\n");
					result.append(",\n");
				}

				if (count % 6 == 0)
				{
					System.out.println("\n");
					result.append("\n");
				}
			}
			System.out.println("]");
			result.append("\n]");
			// JSONObject jObj = new JSONObject(jsonStringBuf.toString());

			// Set<String> level0Keys = jObj.keySet();
			//
			// System.out.println("Num of level0Keys = " + level0Keys.size());
			//
			// level0Keys.stream().forEach(e -> System.out.println(e + "\n"));
			System.out.println("---------");

			// System.out.println(jObj.toString());
			System.out.println("---------");
			WToFile.writeToNewFile(result.toString(), fileNameToWrite);// commonPath +
																				// "CurrentlyForDifferentFetchTime.json");
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		return arrayOfCurrentWeatherObjects;

	}

	/**
	 * Extracts currently data and writes the currently weather data with lat,lon,timestamp of weather query in json
	 * format
	 * 
	 * @param fileNameToRead
	 * @param fileNameToWrite
	 */
	public static void extractHourlyDataFromForecastIOJSON(String fileNameToRead, String fileNameToWrite)
	{

		// System.out.println("Inside getHourlyDataFromForecastIOJSON()");
		StringBuffer result = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			String lineRead;

			ArrayList<String> linesRead = new ArrayList<String>();

			while ((lineRead = br.readLine()) != null)
			{
				linesRead.add(lineRead);
			}

			System.out.println("Num of lines read: " + linesRead.size() + "\n\n"); // each line representig one weather
																					// query request

			int count = 0;

			JSONArray previousHourlyWeatherData = new JSONArray();

			System.out.println("[");
			result.append("[\n");
			for (String currentLine : linesRead) // each line is a json object
			{
				count++;
				// System.out.println("reading line: " + ++count);
				JSONObject jObj = new JSONObject(currentLine);
				Set<String> level0Keys = jObj.keySet();
				// System.out.println("Num of level0Keys = " + level0Keys.size());

				JSONObject fetchedWeather = jObj.getJSONObject("FW");

				JSONObject currentlyObj = fetchedWeather.getJSONObject("currently");

				int currentEpochSec = (int) currentlyObj.get("time");

				Instant tsInstant = Instant.ofEpochSecond(currentEpochSec);
				// LocalDateTime ldateTime = LocalDateTime.from(tsInstant);
				String queryInput = "\"lat\":" + fetchedWeather.get("latitude").toString() + "," + "\"lon\":"
						+ fetchedWeather.get("longitude").toString() + "," + "\"currentTimeEpoch\":"
						+ currentlyObj.get("time").toString() + "," + "\"currentTimeISO\":\"" + tsInstant.toString()
						+ "\",";

				// System.out.println("fetchedWeather = " + fetchedWeather.toString());

				JSONObject hourlyWeather = fetchedWeather.getJSONObject("hourly");

				// System.out.println("hourly = " + hourlyWeather.toString());

				JSONArray hourlyWeatherData = (JSONArray) hourlyWeather.get("data");

				// System.out.print("lat" + hourlyWeatherData.toString());
				// indentical and is not the first line for that sample, note we have 6 lines for each lat,long,day
				// combination. each of the six lines have different
				// current timestamps for fetching
				String indentialToPrev = "\"NA\"";
				if ((count - 1) % 6 != 0)
				{
					if (previousHourlyWeatherData.toString().equals(hourlyWeatherData.toString()))
					{
						indentialToPrev = "\"true\"";
						// System.out.println("-----IDENTICAL to previousHourlyWeatherData");
					}
					else
					{
						indentialToPrev = "\"false\"";
						// System.out.println("*****DIFFERENT from previousHourlyWeatherData");
					}
				}

				String hourlyWeatherDataString = "\"hourly\":{\"isIdenticalToPrevious\":" + indentialToPrev
						+ ", \"data\":" + hourlyWeatherData.toString() + "}";

				result.append("{" + queryInput + hourlyWeatherDataString + "}");
				System.out.println("{" + queryInput + hourlyWeatherDataString + "}");
				// System.out.println("hourlyWeatherData = " + hourlyWeatherData.toString());

				previousHourlyWeatherData = hourlyWeatherData;

				if (count != linesRead.size())
				{
					System.out.println(",\n");
					result.append(",\n");
				}

				if (count % 6 == 0)
				{
					System.out.println("\n");
					result.append("\n");
				}
			}
			System.out.println("]");
			result.append("\n]");
			System.out.println("---------");
			WToFile.writeToNewFile(result.toString(), fileNameToWrite);// commonPath +
																				// "HourlyForDifferentFetchTime.json");
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * Correctness of written data validated by looking at raw data.</br>
	 * <font color ="red">Note: identialToPrev is to compare each line (JSONObj) to previous line(JSONObj). This is
	 * relevant when running the experiment for sample to check if hourly data array is invariant of query's current
	 * timestamp for a given (lat,lon). Also, in the test sample there are 6x4 input queries (6 different timestamps in
	 * same day for each lat,lon) </font>
	 * 
	 * 
	 * @param fileNameToRead
	 * @param fileNameToWrite
	 * @param addISOTimestamps
	 */
	public static ArrayList<JSONArray> extractHourlyDataFromForecastIOJSON(String fileNameToRead,
			String fileNameToWrite, boolean addISOTimestamps)
	{

		ArrayList<JSONArray> arrayOfHourlyArrays = new ArrayList<>();

		StringBuffer result = new StringBuffer();

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			String lineRead;

			ArrayList<String> linesRead = new ArrayList<String>();

			while ((lineRead = br.readLine()) != null)
			{
				linesRead.add(lineRead);
			}

			System.out.println("Num of lines read: " + linesRead.size() + "\n\n"); // each line representig one weather
																					// query request

			int count = 0;

			JSONArray previousHourlyWeatherData = new JSONArray();

			System.out.println("[");
			result.append("[\n");
			for (String currentLine : linesRead) // each line is a json object
			{
				count++;
				// System.out.println("reading line: " + ++count);
				JSONObject jObj = new JSONObject(currentLine);
				Set<String> level0Keys = jObj.keySet();
				// System.out.println("Num of level0Keys = " + level0Keys.size());

				JSONObject fetchedWeather = jObj.getJSONObject("FW");

				JSONObject currentlyObj = fetchedWeather.getJSONObject("currently");

				int currentEpochSec = (int) currentlyObj.get("time");

				Instant tsInstant = Instant.ofEpochSecond(currentEpochSec);
				// LocalDateTime ldateTime = LocalDateTime.from(tsInstant);
				String queryInput = "\"lat\":" + fetchedWeather.get("latitude").toString() + "," + "\"lon\":"
						+ fetchedWeather.get("longitude").toString() + "," + "\"currentTimeEpoch\":"
						+ currentlyObj.get("time").toString() + "," + "\"currentTimeISO\":\"" + tsInstant.toString()
						+ "\",";

				JSONObject hourlyWeather = fetchedWeather.getJSONObject("hourly");

				JSONArray hourlyWeatherData = (JSONArray) hourlyWeather.get("data");

				if (addISOTimestamps) // add ISO timestamps besides epoch timestamp to improve readability
				{
					for (int i = 0; i < hourlyWeatherData.length(); i++)
					{
						JSONObject eachHourObj = hourlyWeatherData.getJSONObject(i);
						int hrEpochTime = eachHourObj.getInt("time");
						Instant hrInstant = Instant.ofEpochSecond(hrEpochTime);
						eachHourObj.put("timestamp", (String) hrInstant.toString());
					}
				}
				arrayOfHourlyArrays.add(hourlyWeatherData);

				String identialToPrev = "\"NA\"";
				if ((count - 1) % 6 != 0) // to only do it for non-first lines for the sample
				{
					if (previousHourlyWeatherData.toString().equals(hourlyWeatherData.toString()))
					{
						identialToPrev = "\"true\"";
						// System.out.println("-----IDENTICAL to previousHourlyWeatherData");
					}
					else
					{
						identialToPrev = "\"false\"";
						// System.out.println("*****DIFFERENT from previousHourlyWeatherData");
					}
				}

				String hourlyWeatherDataString = "\"hourly\":{\"isIdenticalToPrevious\":" + identialToPrev
						+ ", \"data\":" + hourlyWeatherData.toString() + "}";

				result.append("{" + queryInput + hourlyWeatherDataString + "}");
				System.out.println("{" + queryInput + hourlyWeatherDataString + "}");
				// System.out.println("hourlyWeatherData = " + hourlyWeatherData.toString());

				previousHourlyWeatherData = hourlyWeatherData;

				if (count != linesRead.size())
				{
					System.out.println(",\n");
					result.append(",\n");
				}

				if (count % 6 == 0)
				{
					System.out.println("\n");
					result.append("\n");
				}
			}
			System.out.println("]");
			result.append("\n]");
			System.out.println("---------");
			WToFile.writeToNewFile(result.toString(), fileNameToWrite);// commonPath +
																				// "HourlyForDifferentFetchTime.json");
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		return arrayOfHourlyArrays;
	}

	// public static TreeMap<Integer, JSONObject> gettHourlyDataFromForecastIOJSON(String fileNameToRead)// , String
	// fileNameToWrite)
	// {
	//
	// try
	// {
	// BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
	// String lineRead;
	//
	// ArrayList<String> linesRead = new ArrayList<String>();
	//
	// while ((lineRead = br.readLine()) != null)
	// {
	// linesRead.add(lineRead);
	// }
	//
	// System.out.println("Num of lines read: " + linesRead.size() + "\n\n"); // each line representig one weather query
	// request
	//
	// int count = 0;
	//
	// for (String currentLine : linesRead) // each line is a json object
	// {
	// count++;
	//
	// JSONObject jObj = new JSONObject(currentLine);
	// Set<String> level0Keys = jObj.keySet();
	// // System.out.println("Num of level0Keys = " + level0Keys.size());
	//
	// JSONObject fetchedWeather = jObj.getJSONObject("FW");
	//
	// JSONObject currentlyObj = fetchedWeather.getJSONObject("currently");
	//
	// int currentEpochSec = (int) currentlyObj.get("time");
	//
	// Instant tsInstant = Instant.ofEpochSecond(currentEpochSec);
	// // LocalDateTime ldateTime = LocalDateTime.from(tsInstant);
	// String queryInput = "\"lat\":" + fetchedWeather.get("latitude").toString() + "," + "\"lon\":"
	// + fetchedWeather.get("longitude").toString() + "," + "\"currentTimeEpoch\":" +
	// currentlyObj.get("time").toString()
	// + "," + "\"currentTimeISO\":\"" + tsInstant.toString() + "\",";
	//
	// // System.out.println("fetchedWeather = " + fetchedWeather.toString());
	//
	// JSONObject hourlyWeather = fetchedWeather.getJSONObject("hourly");
	//
	// // System.out.println("hourly = " + hourlyWeather.toString());
	//
	// JSONArray hourlyWeatherData = (JSONArray) hourlyWeather.get("data");
	//
	// // System.out.print("lat" + hourlyWeatherData.toString());
	// // indentical and is not the first line for that sample, note we have 6 lines for each lat,long,day combination.
	// each of the six lines have different
	// // current timestamps for fetching
	// String indentialToPrev = "\"NA\"";
	// if ((count - 1) % 6 != 0)
	// {
	// if (previousHourlyWeatherData.toString().equals(hourlyWeatherData.toString()))
	// {
	// indentialToPrev = "\"true\"";
	// // System.out.println("-----IDENTICAL to previousHourlyWeatherData");
	// }
	// else
	// {
	// indentialToPrev = "\"false\"";
	// // System.out.println("*****DIFFERENT from previousHourlyWeatherData");
	// }
	// }
	//
	// String hourlyWeatherDataString =
	// "\"hourly\":{\"isIdenticalToPrevious\":" + indentialToPrev + ", \"data\":" + hourlyWeatherData.toString() + "}";
	//
	// result.append("{" + queryInput + hourlyWeatherDataString + "}");
	// System.out.println("{" + queryInput + hourlyWeatherDataString + "}");
	// // System.out.println("hourlyWeatherData = " + hourlyWeatherData.toString());
	//
	// previousHourlyWeatherData = hourlyWeatherData;
	//
	// if (count != linesRead.size())
	// {
	// System.out.println(",\n");
	// result.append(",\n");
	// }
	//
	// if (count % 6 == 0)
	// {
	// System.out.println("\n");
	// result.append("\n");
	// }
	// }
	// System.out.println("]");
	// result.append("\n]");
	// System.out.println("---------");
	// WritingToFile.writeToNewFile(result.toString(), fileNameToWrite);// commonPath +
	// "HourlyForDifferentFetchTime.json");
	// }
	//
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }

}
