package org.activity.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.activity.generator.generateSyntheticData;
import org.activity.io.ReadingFromFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TrajectoryEntry;
import org.activity.objects.Triple;
import org.activity.objects.UserDayTimeline;
import org.activity.ui.PopUps;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.analysis.function.Asin;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;

/**
 * 
 * @author gunjan
 *
 */
public class UtilityBelt
{
	
	/**
	 * Enforcing non-instantiability
	 */
	private UtilityBelt()
	{
		throw new AssertionError();
	}
	
	// public static final int ThresholdFrequency = 2;
	public static final double radiusOfEarthInKMs = 6372.8; // In kilometers radius of earth
	
	// public static NumberFormat getDecimalFormat(int numOfDecimalPlaces)
	// {
	// String d;
	//
	// for (int i = 0; i < numOfDecimalPlaces; i++)
	// {
	// d += "#";
	// }
	// return new DecimalFormat("##.###");
	// }
	/**
	 * Adds the given value to each element of the given array
	 * 
	 * @param array
	 * @param constantToAdd
	 * @return
	 */
	public static int[] addConstantToEach(int[] array, int constantToAdd)
	{
		int[] newArray = new int[array.length];
		
		for (int i = 0; i < array.length; i++)
		{
			newArray[i] = array[i] + constantToAdd;
		}
		return newArray;
	}
	
	public static double getSD(double[] vals)
	{
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		double sd = ds.getStandardDeviation();
		return ds.getStandardDeviation();
	}
	
	/**
	 * source: http://codereview.stackexchange.com/questions/37201/finding-all-indices-of-largest-value-in-an-array
	 * 
	 * @param numbers
	 * @return
	 */
	public static int[] findLargeNumberIndices(double[] numbers)
	{
		
		// create an array of at least 8 members.
		// We may need to make this bigger during processing in case
		// there's more than 8 values with the same large value
		int[] indices = new int[Math.max(numbers.length / 16, 8)];
		// how many large values do we have?
		int count = 0;
		// what is the largest value we have?
		double largeNumber = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < numbers.length; i++)
		{
			if (numbers[i] > largeNumber)
			{
				// we have a new large number value... reset our history....
				largeNumber = numbers[i];
				// setting count to zero is enough to 'clear' our previous references.
				count = 0;
				// we know there's space for at least index 0. No need to check.
				// note how we post-increment - this is a 'pattern'.
				indices[count++] = i;
			}
			else if (numbers[i] == largeNumber)
			{
				// we have another large value.
				if (count == indices.length)
				{
					// need to make more space for indices... increase array by 25%
					// count >>> 2 is the same as count / 4 ....
					indices = Arrays.copyOf(indices, count + (count >>> 2));
				}
				// again, use the post-increment
				indices[count++] = i;
			}
		}
		// return the number of values that are valid only.
		return Arrays.copyOf(indices, count);
	}
	
	/**
	 * Reads values from the second column of the specified file
	 * 
	 * @param filePath
	 * @return
	 */
	public static double[] getTimeSeriesVals(String filePath)
	{
		List<Double> vals = ReadingFromFile.oneColumnReaderDouble(filePath, ",", 1, false);
		Double[] vals2 = vals.toArray(new Double[vals.size()]);
		double[] vals3 = ArrayUtils.toPrimitive(vals2);
		return vals3;
	}
	
	public static double getValByRowCol(String filePath, int rowIndex, int colIndex, boolean hasColHeader)
	{
		List<Double> vals = ReadingFromFile.oneColumnReaderDouble(filePath, ",", colIndex, hasColHeader);
		Double[] vals2 = vals.toArray(new Double[vals.size()]);
		double[] vals3 = ArrayUtils.toPrimitive(vals2);
		return vals3[rowIndex];
	}
	
	/**
	 * Converts a double to string avoiding the scientific(exponential) notation while still preserving the original mumber of digits
	 * 
	 * @param ldouble
	 * @return
	 */
	public static String toPlainStringSafely(double ldouble)
	{
		if (Double.isNaN(ldouble))
		{
			return "NaN";
		}
		return ((new BigDecimal(Double.toString(ldouble))).toPlainString());
	}
	
	/**
	 * Used for sanity checks and validations
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean assertEquals(Object a, Object b)
	{
		if (a == b)
		{
			return true;
		}
		
		else
		{
			String stringA, stringB;
			if (a == null)
				stringA = "null";
			else
				stringA = a.toString();
			
			if (b == null)
				stringB = "null";
			else
				stringB = b.toString();
			
			Exception e = new Exception("Assertion failed - Error in assertEquals ( " + stringA + " != " + stringB + " )");
			PopUps.showException(e, "assertEquals");
			// PopUps.showError("Error: Assertion failed: \n" + ExceptionUtils.getStackTrace(e));
			System.err.println("Error: Assertion failed: \n" + ExceptionUtils.getStackTrace(e));
			return false;
		}
	}
	
	/**
	 * Used for sanity checks and validations
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean assertNotNull(Object a)
	{
		if (a == null)
		{
			Exception e = new Exception("Assertion failed - Error in assertNotNull: " + a.toString() + " is NULL");
			PopUps.showException(e, "assertNotNull");
			// PopUps.showError("Error: Assertion failed: \n" + ExceptionUtils.getStackTrace(e));
			System.err.println("Error: Assertion failed: \n" + ExceptionUtils.getStackTrace(e));
			return false;
		}
		else
			return true;
	}
	
	public static double meanOfArrayList(ArrayList<Double> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0)
			return 0;
		
		double[] vals = new double[arr.size()];
		
		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}
		
		return round(StatUtils.mean(vals), roundOffToPlaces);
	}
	
	public static double meanOfArrayListInt(ArrayList<Integer> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0)
			return 0;
		
		double[] vals = new double[arr.size()];
		
		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}
		
		return round(StatUtils.mean(vals), roundOffToPlaces);
	}
	
	public static double medianOfArrayListInt(ArrayList<Integer> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0)
			return 0;
		
		double[] vals = new double[arr.size()];
		
		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}
		
		return round(StatUtils.percentile(vals, 50), roundOffToPlaces);
	}
	
	public static double iqrOfArrayListInt(ArrayList<Integer> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0)
			return 0;
		
		double[] vals = new double[arr.size()];
		
		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}
		
		return round(StatUtils.percentile(vals, 75) - StatUtils.percentile(vals, 25), roundOffToPlaces);
	}
	
	/**
	 * Cleaned , expunge all invalid activity objects and rearrange according to user id order in Constant.userID for the current dataset </br>
	 * <font color="red">NOT NEED ANYMORE AS IT HAS BEEN BROKEN DOWN INTO SMALLER SINGLE PURPOSE FUNCTIONS</font>
	 * 
	 * @param usersTimelines
	 * @return
	 */
	public static LinkedHashMap<String, Timeline>
			dayTimelinesToCleanedExpungedRearrangedTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	{
		LinkedHashMap<String, Timeline> cleanedERTimelines = new LinkedHashMap<String, Timeline>();
		System.out.println("inside dayTimelinesToCleanedExpungedRearrangedTimelines()");
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			String userID = usersTimelinesEntry.getKey();
			LinkedHashMap<Date, UserDayTimeline> userDayTimelines = usersTimelinesEntry.getValue();
			
			userDayTimelines = TimelineUtils.cleanUserDayTimelines(userDayTimelines);
			
			Timeline timelineForUser = new Timeline(userDayTimelines); // converts the day time to continuous dayless timeline
			timelineForUser = UtilityBelt.expungeInvalids(timelineForUser); // expunges invalid activity objects
			
			cleanedERTimelines.put(userID, timelineForUser);
		}
		System.out.println("\t" + cleanedERTimelines.size() + " timelines created");
		cleanedERTimelines = TimelineUtils.rearrangeTimelinesOrderForDataset(cleanedERTimelines);
		System.out.println("\t" + cleanedERTimelines.size() + " timelines created");
		return cleanedERTimelines;
	}
	
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> expungeUserDayTimelinesByUser(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines, ArrayList<String> userIDsToExpunge)
	{
		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> reducedTimelines =
				new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
		System.out.println("number of users before reducing users timeline = " + usersTimelines.size());
		System.out.println("Users to remove = " + userIDsToExpunge.toString());
		
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			String userID = usersTimelinesEntry.getKey();
			System.out.println("Reading " + userID);
			if (userIDsToExpunge.contains(userID))
			{
				System.out.println("removing");
				continue;
			}
			else
			{
				System.out.println("keeping");
				reducedTimelines.put(userID, usersTimelinesEntry.getValue());
			}
		}
		
		System.out.println("number of users in reduced users timeline = " + reducedTimelines.size());
		return reducedTimelines;
	}
	
	/**
	 * 
	 * @param usersTimelines
	 * @return LinkedHashMap<User ID as String, Timeline of the user with user id as integer as timeline id>
	 */
	public static LinkedHashMap<String, Timeline>
			dayTimelinesToTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	{
		LinkedHashMap<String, Timeline> timelines = new LinkedHashMap<String, Timeline>();
		if (usersTimelines.size() == 0 || usersTimelines == null)
		{
			new Exception(
					"Error in org.activity.util.UtilityBelt.dayTimelinesToTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>): userTimeline.size = "
							+ usersTimelines.size()).printStackTrace();
			;
		}
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : usersTimelines.entrySet())
		{
			timelines.put(entry.getKey(), new Timeline(entry.getValue(), Integer.valueOf(entry.getKey())));
		}
		return timelines;
	}
	
	public static LinkedHashMap<String, Timeline> expungeInvalids(LinkedHashMap<String, Timeline> usersTimelines)
	{
		LinkedHashMap<String, Timeline> expungedTimelines = new LinkedHashMap<String, Timeline>();
		
		for (Map.Entry<String, Timeline> entry : usersTimelines.entrySet())
		{
			expungedTimelines.put(entry.getKey(), UtilityBelt.expungeInvalids(entry.getValue()));
		}
		return expungedTimelines;
	}
	
	// public static LinkedHashMap<String, Timeline> dayTimelinesToRearrangedTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	// {
	// LinkedHashMap<String, Timeline> userTimelines = new LinkedHashMap<String, Timeline>();
	// for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
	// {
	// String userID = usersTimelinesEntry.getKey();
	// LinkedHashMap<Date, UserDayTimeline> userDayTimelines = usersTimelinesEntry.getValue();
	//
	// userDayTimelines = UtilityBelt.cleanUserDayTimelines(userDayTimelines);
	//
	// Timeline timelineForUser = new Timeline(userDayTimelines); // converts the day time to continuous dayless timeline
	// // timelineForUser = UtilityBelt.expungeInvalids(timelineForUser); // expunges invalid activity objects
	//
	// userTimelines.put(userID, timelineForUser);
	// }
	// System.out.println("\t" + userTimelines.size() + " timelines created");
	// userTimelines = UtilityBelt.rearrangeTimelinesOrderForDataset(userTimelines);
	// System.out.println("\t" + userTimelines.size() + " timelines created");
	// return userTimelines;
	// }
	
	// public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> dayTimelinesToRearrangedDayTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>
	// usersTimelines)
	// {
	// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines = new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
	//
	// for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
	// {
	// String userID = usersTimelinesEntry.getKey();
	// LinkedHashMap<Date, UserDayTimeline> userDayTimelines = usersTimelinesEntry.getValue();
	//
	// userDayTimelines = UtilityBelt.cleanUserDayTimelines(userDayTimelines);
	//
	// Timeline timelineForUser = new Timeline(userDayTimelines); // converts the day time to continuous dayless timeline
	// // timelineForUser = UtilityBelt.expungeInvalids(timelineForUser); // expunges invalid activity objects
	//
	// userTimelines.put(userID, timelineForUser);
	// }
	// System.out.println("\t" + userTimelines.size() + " timelines created");
	// userTimelines = UtilityBelt.rearrangeTimelinesOrderForDataset(userTimelines);
	// System.out.println("\t" + userTimelines.size() + " timelines created");
	// return userTimelines;
	// }
	
	public static ArrayList<String> getListOfUsers(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	{
		ArrayList<String> users = new ArrayList<String>();
		
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			users.add(usersTimelinesEntry.getKey());
		}
		
		return users;
		
	}
	
	// public static long getCountOfActivityObjects(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	// {
	// long count = 0;
	//
	// for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
	// {
	// for (Map.Entry<Date, UserDayTimeline> entry : usersTimelinesEntry.getValue().entrySet())
	// {
	// if (entry.getValue().containsAtLeastOneValidActivity() == false)
	// {
	// datesToRemove.add(entry.getKey());
	// if (Constant.verboseTimelineCleaning)
	// System.out.print("," + entry.getKey());
	// }
	// }
	//
	// }
	//
	// return count;
	//
	// }
	
	/*
	 * TO create a copy or clone
	 */
	/*
	 * public static LinkedHashMap<Date, UserDayTimeline> cUserDayTimelines(LinkedHashMap<Date, UserDayTimeline> userDayTimelines) {
	 * 
	 * return userDayTimelines; }
	 */
	public static double getPearsonCorrelation(ArrayList<Double> a, ArrayList<Double> b)
	{
		if (a == null || b == null)
		{
			System.err.println("Error: in getPearsonCorrelation, ArrayList object are null (not set)");
			return -9999;
		}
		
		if (a.size() != b.size())
		{
			System.err.println("Warning: in getPearsonCorrelation, ArrayList object are of different sizes " + a.size() + "!=" + b.size());
			return -9999;
		}
		
		PearsonsCorrelation pc = new PearsonsCorrelation();
		double ans = (pc.correlation(UtilityBelt.toPrimitive(a), UtilityBelt.toPrimitive(b)));
		if (!(ans == Double.NaN) && Double.isFinite(ans))
		{
			ans = round(ans, 5);
		}
		return ans;
	}
	
	public static double getKendallTauCorrelation(ArrayList<Double> a, ArrayList<Double> b)
	{
		if (a == null || b == null)
		{
			System.err.println("Error: in getKendallTauCorrelation, ArrayList object are null (not set)");
			return -9999;
		}
		
		if (a.size() != b.size())
		{
			System.err
					.println("Warning: in getKendallTauCorrelation, ArrayList object are of different sizes " + a.size() + "!=" + b.size());
			return -9999;
		}
		
		KendallsCorrelation pc = new KendallsCorrelation();
		double ans = (pc.correlation(UtilityBelt.toPrimitive(a), UtilityBelt.toPrimitive(b)));
		if (!(ans == Double.NaN) && Double.isFinite(ans))
		{
			ans = round(ans, 5);
		}
		return ans;
	}
	
	public static boolean isDirectoryEmpty(String path)
	{
		boolean isEmpty = false;
		
		File file = new File(path);
		
		if (file.isDirectory())
		{
			if (file.list().length == 0)
			{
				isEmpty = true;
			}
		}
		else
		{
			System.err.println("Error in isDirectoryEmpty: " + path + " is not a directory");
		}
		
		return isEmpty;
	}
	
	/**
	 * 
	 * @param array
	 * @return
	 */
	public static double[] toPrimitive(ArrayList<Double> array)
	{
		if (array == null)
		{
			return null;
		}
		else if (array.size() == 0)
		{
			return new double[0];
		}
		
		final double[] result = new double[array.size()];
		
		for (int i = 0; i < array.size(); i++)
		{
			result[i] = array.get(i).doubleValue();
		}
		return result;
	}
	
	/**
	 * Creates a directory if it does not already exist
	 * 
	 * @param pathname
	 * @return
	 */
	public static boolean createDirectory(String pathname)
	{
		boolean result = false;
		
		File directory = new File(pathname);
		
		// if the directory does not exist, create it
		if (!directory.exists())
		{
			System.out.println("creating directory: " + directory);
			try
			{
				directory.mkdir();
				result = true;
			}
			catch (SecurityException se)
			{
				System.err.println("Error: cannot create  directory " + directory);
				se.printStackTrace();
			}
			if (result)
			{
				System.out.println(pathname + " directory created");
			}
		}
		else
		{
			System.out.println("Cannot create  directory " + directory + " as it already exists");
		}
		return result;
	}
	
	/**
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to BigDecimal for performance concerns,
	 * 
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the shortest distance over the earth’s surface – giving an
	 * ‘as-the-crow-flies’ distance between the points (ignoring any hills they fly over, of course!).</br>
	 * TODO LATER can use non-native math libraries for faster computation. User jafama or apache common maths.</br>
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversine(String lat1s, String lon1s, String lat2s, String lon2s)
	{
		
		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);
		
		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);
		
		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  "
					+ lat2s + "," + lon2s);
			if (Constant.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + "," + lon1s
						+ "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}
		
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);
		
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		
		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range: provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String, String)");
		// }
		
		double c = 2 * Math.asin(Math.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);
		
		if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
		}
		
		return UtilityBelt.round(radiusOfEarthInKMs * c, 4);
	}
	
	/**
	 * Using non-native math libraries for faster computation. User jafama or apache common maths.</br>
	 * TODO <font color = red>HAVE NOT FINISHED converting java math functions to new libraries. </font>This uses the ‘haversine’ formula to calculate the great-circle distance
	 * between two points – that is, the shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring any hills they fly over,
	 * of course!).
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversineFasterIncomplete(String lat1s, String lon1s, String lat2s, String lon2s)
	{
		
		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);
		
		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);
		
		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  "
					+ lat2s + "," + lon2s);
			if (Constant.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + "," + lon1s
						+ "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}
		
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);
		
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		
		// double c = 2 * Math.asin(Math.sqrt(a)); // TODO: #performanceEater
		double c = 2 * new Asin().value(Math.sqrt(a));
		// System.out.println("c = " + c);
		
		if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
		}
		
		return UtilityBelt.round(radiusOfEarthInKMs * c, 4);
	}
	
	public static double haversine(String lat1s, String lon1s, String lat2s, String lon2s, boolean roundTheResult)
	{
		
		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);
		
		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);
		
		if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  "
					+ lat2s + "," + lon2s);
			
			if (Constant.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + "," + lon1s
						+ "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;
			// System.exit(-1);
			
		}
		
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		
		if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
		}
		
		if (roundTheResult)
		{
			return UtilityBelt.round(radiusOfEarthInKMs * c, 4);
		}
		
		else
		{
			return (radiusOfEarthInKMs * c);
		}
		
	}
	
	/**
	 * 
	 * @param toCheck
	 * @return
	 */
	public static boolean isValidActivityName(String toCheck)
	{
		if (Constant.getDatabaseName().equals("gowalla1")) // gowalla has no invalid activity names
		{// to speed up
			return true;
		}
		if ((toCheck.trim().equalsIgnoreCase(Constant.INVALID_ACTIVITY1)) || (toCheck.trim().equalsIgnoreCase(Constant.INVALID_ACTIVITY2)))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	/**
	 * @todo convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversine(double lat1, double lon1, double lat2, double lon2)
	{
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return radiusOfEarthInKMs * c;
	}
	
	/**
	 * 
	 * 
	 * @param treeMap
	 * @return ArrayList of strings of the form 'timestampInMilliSeconds||ImageName||ActivityName' or more generally 'timestampInMilliSeconds||String1`the old separator
	 *         inherited`String2'
	 */
	public static ArrayList<String> treeMapToArrayListString(TreeMap<Timestamp, String> treeMap)
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
	
	public static ArrayList<TrajectoryEntry> treeMapToArrayListGeo(TreeMap<Timestamp, TrajectoryEntry> treeMap)
	{
		ArrayList<TrajectoryEntry> arrayList = new ArrayList<TrajectoryEntry>();
		
		for (Map.Entry<Timestamp, TrajectoryEntry> entry : treeMap.entrySet())
		{
			arrayList.add(entry.getValue());
		}
		
		// if (treeMap.size() == arrayList.size())
		// {
		// System.out.println("TreeMap converted to arraylist succesfully");
		// }
		
		if (treeMap.size() != arrayList.size())
		{
			String msg = "Error in TreeMap conversion to arraylist";
			PopUps.showException(new Exception(msg),
					"org.activity.util.UtilityBelt.treeMapToArrayListGeo(TreeMap<Timestamp, TrajectoryEntry>)");
			// PopUps.showError(msg);
		}
		return arrayList;
	}
	
	public static ArrayList<?> treeMapToArrayList(TreeMap<Timestamp, ?> treeMap)
	{
		ArrayList arrayList = new ArrayList();
		
		for (Map.Entry<Timestamp, ?> entry : treeMap.entrySet())
		{
			arrayList.add(entry.getValue());
		}
		
		// if (treeMap.size() == arrayList.size())
		// {
		// System.out.println("TreeMap converted to arraylist succesfully");
		// }
		
		if (treeMap.size() != arrayList.size())
		{
			String msg = "Error in TreeMap conversion to arraylist";
			PopUps.showException(new Exception(msg),
					"org.activity.util.UtilityBelt.treeMapToArrayListGeo(TreeMap<Timestamp, TrajectoryEntry>)");
			// PopUps.showError(msg);
		}
		return arrayList;
	}
	
	/**
	 * Returns the average of the BigDecimal values in the given ArrayList note: checked OK
	 * 
	 * @param vals
	 * @return
	 */
	public static BigDecimal average(ArrayList<BigDecimal> vals)
	{
		BigDecimal res = new BigDecimal("-999999");
		
		BigDecimal sum = BigDecimal.ZERO;
		
		for (BigDecimal value : vals)
		{
			sum = sum.add(value);
		}
		
		res = sum.divide(new BigDecimal(vals.size()));
		// res = sum.divide(BigDecimal.valueOf(vals.size()));
		
		return res;
		
	}
	
	public static double averageOfListDouble(ArrayList<Double> vals)
	{
		double res = -9999999;
		
		double sum = 0;// Double.ZERO;
		
		for (double value : vals)
		{
			sum = sum + value;
		}
		
		res = sum / vals.size();
		
		return res;
		
	}
	
	public static double averageOfListInteger(ArrayList<Integer> vals)
	{
		double res = -9999999;
		
		double sum = 0;// Double.ZERO;
		
		for (double value : vals)
		{
			sum = sum + value;
		}
		
		res = sum / vals.size();
		
		return res;
		
	}
	
	/**
	 * Returns the average of the decimals stored as String values in the given ArrayList. It uses BigDecimal during calculation to have precise results note: ALWYAS create
	 * BigDecimal from String and not from number(e.g. double) for precision.
	 * 
	 * @param vals
	 * @return
	 */
	public static String averageDecimalsAsStrings(ArrayList<String> vals)
	{
		String res = new String("-999999");
		BigDecimal resultInDecimal = new BigDecimal("-999999");
		
		BigDecimal sumInDecimal = BigDecimal.ZERO;
		
		if (vals.size() == 0)
		{
			return "0";
		}
		try
		{
			for (String value : vals)
			{
				BigDecimal valueInDecimal = new BigDecimal(value);
				sumInDecimal = sumInDecimal.add(valueInDecimal);
			}
			resultInDecimal = sumInDecimal.divide(new BigDecimal(String.valueOf(vals.size())), 3, RoundingMode.HALF_UP); // ref:
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return resultInDecimal.toString();
		
	}
	
	/**
	 * removes invalids altitudes, i.e, those which are below 0 and not just -777
	 * 
	 * in
	 * 
	 * @param vals
	 * @return
	 */
	public static ArrayList<String> removeInvalidAlts(ArrayList<String> vals)
	{
		ArrayList<String> res = new ArrayList<String>();
		
		for (String val : vals)
		{
			if (Double.parseDouble(val) >= 0)// != -777)
			{
				res.add(val);
			}
			else
			{
			}
		}
		
		return res;
	}
	
	/**
	 * Return the next double value(with no decimal part) greater than the value and is a multiple of multiple
	 * 
	 * @param value
	 * @param multiple
	 * @return
	 */
	public static double ceilNearestMultipleOf(double value, int multiple)
	{
		double res = value + 1; // we have to get next value and not this value (even though if the values was a multiple of multiple or not)
		
		System.out.println("res=" + res);
		double rem = res % multiple;
		
		System.out.println("rem=" + rem);
		if (rem != 0)
		{
			res = res + (multiple - rem);
		}
		System.out.println("res=" + res);
		return res;
	}
	
	public static int getCountOfLevel1Ops(String editOpsTrace)
	{
		int countOfL1Ops = 0;
		
		countOfL1Ops += countSubstring("_I(", editOpsTrace);
		countOfL1Ops += countSubstring("_D(", editOpsTrace);
		countOfL1Ops += countSubstring("_Sao(", editOpsTrace);
		
		return countOfL1Ops;
	}
	
	public static int getCountOfLevelInsertions(String editOpsTrace)
	{
		int countOfL1Ops = 0;
		
		countOfL1Ops += countSubstring("_I(", editOpsTrace);
		
		return countOfL1Ops;
	}
	
	// public static int getCountOfLevelDeletions(String editOpsTrace)
	// {
	// int countOfL1Ops=0;
	//
	//
	// countOfL1Ops += countSubstring("_D(",editOpsTrace);
	//
	//
	// return countOfL1Ops;
	// }
	//
	// public static int getCountOfLevelDeletions(String editOpsTrace)
	// {
	// int countOfL1Ops=0;
	//
	//
	// countOfL1Ops += countSubstring("_D(",editOpsTrace);
	//
	//
	// return countOfL1Ops;
	// }
	//
	//
	
	/**
	 * Removes the invalid activity objects from the given Timeline. Invalid Activity Objects are Activity Objects with Activity Name as 'Others' or 'Unknown'
	 * 
	 * @param timelineToPrune
	 * @return the Timeline without all its invalid activity objects
	 */
	public static Timeline expungeInvalids(Timeline timelineToPrune)
	{
		if (timelineToPrune == null)
		{
			System.err.println("Error inside UtulityBelt.expungeInvalids: timelineToPrune is null");
		}
		
		ArrayList<ActivityObject> arrayToPrune = timelineToPrune.getActivityObjectsInTimeline();
		ArrayList<ActivityObject> arrPruned = new ArrayList<ActivityObject>();
		
		Integer timelineID = timelineToPrune.getTimelineID();
		
		for (int i = 0; i < arrayToPrune.size(); i++)
		{
			if (arrayToPrune.get(i).isInvalidActivityName()) // if the first element is unknown, prune it
			{
				continue;
			}
			else
				arrPruned.add(arrayToPrune.get(i));
		}
		return (new Timeline(timelineID, arrPruned));
	}
	
	public static int getCountOfLevel2Ops(String editOpsTrace)
	{
		int countOfL2Ops = 0;
		
		countOfL2Ops += countSubstring("_Sst(", editOpsTrace);
		countOfL2Ops += countSubstring("_Sd(", editOpsTrace);
		countOfL2Ops += countSubstring("_Sstd(", editOpsTrace);
		
		return countOfL2Ops;
	}
	
	public static int countSubstring(String subStr, String str)
	{
		return (str.length() - str.replace(subStr, "").length()) / subStr.length();
	}
	
	/**
	 * Sorts a map in decreasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		
		Collections.sort(list, new Comparator<Map.Entry<K, V>>()
		{
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
			{
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		
		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sorts a map in decreasing order of value
	 * 
	 * 
	 * @param map
	 * @param isStable
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean isStable)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		
		if (!isStable)
		{
			Collections.shuffle(list);
		}
		// PopUps.showMessage("just before sorting");
		Collections.sort(list, new Comparator<Map.Entry<K, V>>()
		{
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
			{
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		// PopUps.showMessage("just after sorting");
		
		Map<K, V> result = new LinkedHashMap<>();
		int count = 0;
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
			// if (count++ % 100 == 0)
			// {
			// PopUps.showMessage("converted for " + count + " of " + list.size() + " elements");
			// }
		}
		
		PopUps.showMessage("just after creating converting back list to map");
		return result;
	}
	
	// public static TreeMap<Integer, Long> sortByValue2(TreeMap<Integer, Long> map, boolean isStable)
	// {
	// List<Map.Entry<Integer, Long>> list = new LinkedList<>(map.entrySet());
	//
	// if (!isStable)
	// {
	// Collections.shuffle(list);
	// }
	// // PopUps.showMessage("just before sorting");
	// Collections.sort(list, new Comparator<Map.Entry<Integer, Long>>()
	// {
	// @Override
	// public int compare(Map.Entry<Integer, Long> o1, Map.Entry<Integer, Long> o2)
	// {
	// return (o2.getValue()).compareTo(o1.getValue());
	// }
	// });
	// // PopUps.showMessage("just after sorting");
	//
	// Map<Integer, Long> result = new LinkedHashMap<>();
	// int count = 0;
	// for (Map.Entry<K, V> entry : list)
	// {
	// result.put(entry.getKey(), entry.getValue());
	// // if (count++ % 100 == 0)
	// // {
	// // PopUps.showMessage("converted for " + count + " of " + list.size() + " elements");
	// // }
	// }
	//
	// PopUps.showMessage("just after creating converting back list to map");
	// return result;
	// }
	
	// public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueJava8(Map<K, V> map)
	// {
	// Comparator<Entry<K, V>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue());
	//
	// Optional<Entry<K, V>> val = map.entrySet().stream().sorted(byValue.reversed()).findFirst();
	// if (val.isPresent())
	// {
	// Map<K, V> res = val;
	// return res;
	// }
	// else
	// return null;
	// // return val;
	// }
	
	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAscending(Map<K, V> map)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<K, V>>()
		{
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
			{
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		
		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, Pair<Integer, Double>> sortByValueAscending2(LinkedHashMap<String, Pair<Integer, Double>> map)
	{
		List<Map.Entry<String, Pair<Integer, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<String, Pair<Integer, Double>>>()
		{
			@Override
			public int compare(Map.Entry<String, Pair<Integer, Double>> o1, Map.Entry<String, Pair<Integer, Double>> o2)
			{
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		
		LinkedHashMap<String, Pair<Integer, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<String, Pair<Integer, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<String,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Integer, Pair<String, Double>>
			sortByValueAscendingIntStrDoub(LinkedHashMap<Integer, Pair<String, Double>> map)
	{
		List<Map.Entry<Integer, Pair<String, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Integer, Pair<String, Double>>>()
		{
			@Override
			public int compare(Map.Entry<Integer, Pair<String, Double>> o1, Map.Entry<Integer, Pair<String, Double>> o2)
			{
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		
		LinkedHashMap<Integer, Pair<String, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Integer, Pair<String, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Integer, Pair<Integer, Double>> sortByValueAscending3(LinkedHashMap<Integer, Pair<Integer, Double>> map)
	{
		List<Map.Entry<Integer, Pair<Integer, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Integer, Pair<Integer, Double>>>()
		{
			@Override
			public int compare(Map.Entry<Integer, Pair<Integer, Double>> o1, Map.Entry<Integer, Pair<Integer, Double>> o2)
			{
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		
		LinkedHashMap<Integer, Pair<Integer, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Integer, Pair<Integer, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Date, Pair<Integer, Double>> sortByValueAscending4(LinkedHashMap<Date, Pair<Integer, Double>> map)
	{
		List<Map.Entry<Date, Pair<Integer, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Date, Pair<Integer, Double>>>()
		{
			@Override
			public int compare(Map.Entry<Date, Pair<Integer, Double>> o1, Map.Entry<Date, Pair<Integer, Double>> o2)
			{
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		
		LinkedHashMap<Date, Pair<Integer, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Date, Pair<Integer, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * @todo to make it for generic Triples sorted by the third value /** Sorts a map in increasing order of value
	 * 
	 *       It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 *       note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>>
			sortByValueAscending5(LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> map)
	{
		List<Map.Entry<Date, Triple<Integer, ActivityObject, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Date, Triple<Integer, ActivityObject, Double>>>()
		{
			@Override
			public int compare(Map.Entry<Date, Triple<Integer, ActivityObject, Double>> o1,
					Map.Entry<Date, Triple<Integer, ActivityObject, Double>> o2)
			{
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		
		LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Date, Triple<Integer, ActivityObject, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Date, Triple<Integer, String, Double>>
			sortTripleByThirdValueAscending6(LinkedHashMap<Date, Triple<Integer, String, Double>> map)
	{
		List<Map.Entry<Date, Triple<Integer, String, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Date, Triple<Integer, String, Double>>>()
		{
			@Override
			public int compare(Map.Entry<Date, Triple<Integer, String, Double>> o1, Map.Entry<Date, Triple<Integer, String, Double>> o2)
			{
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		
		LinkedHashMap<Date, Triple<Integer, String, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Date, Triple<Integer, String, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/*
	 * static Map sortByValueAscending(Map map) //http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java { List list = new
	 * LinkedList(map.entrySet()); Collections.sort(list, new Comparator() { public int compare(Object o1, Object o2) { return ((Comparable) ((Map.Entry)
	 * (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue()); } });
	 * 
	 * Map result = new LinkedHashMap(); for (Iterator it = list.iterator(); it.hasNext();) { Map.Entry entry = (Map.Entry)it.next(); result.put(entry.getKey(), entry.getValue());
	 * } return result; }
	 */
	
	// ////
	
	public static String getDimensionNameFromDimenionIDName(String dimensionID)
	{
		return (dimensionID.split("_"))[0] + "_Dimension";
	}
	
	// replace attribute name with Integer code
	public static String reduceJSONString(String selectAttributeString, String jsonArrayForDataTableString)
	{
		
		System.out.println(
				"inside reduce json with selec" + selectAttributeString + " json of size =" + jsonArrayForDataTableString.length());
		String jsonS = jsonArrayForDataTableString;
		
		HashMap<String, Integer> attEnumerated = new HashMap<String, Integer>(); // < User_ID, 1>
		
		int counter = 1;
		for (String att : selectAttributeString.split(","))
		{
			System.out.println("att=" + att);
			String splittedAttName[] = att.split("\\."); // for user_dimension_table.User_ID
			System.out.println(att.split("\\."));
			System.out.println(att.split("\\.").length);
			
			System.out.println(splittedAttName.length);
			System.out.println(splittedAttName[0]);
			attEnumerated.put(splittedAttName[1], new Integer(counter));
			counter++;
		}
		
		for (Map.Entry<String, Integer> entry : attEnumerated.entrySet())
		{
			jsonS.replace(entry.getKey(), entry.getValue().toString());
			// System.out.println("Key : " + entry.getKey() + " Value : "
			// + entry.getValue());
		}
		
		System.out.println("reduce json for " + selectAttributeString + " is " + jsonS);
		return jsonS;
		
	}
	
	/**
	 * 
	 * @param timestampString
	 *            in ISO 8601 format
	 * @return
	 */
	// public static Instant getInstantFromISOString(String timestampString)// , String timeString)
	// {
	// Instant instant = null;
	// try
	// {
	// instant = Instant.parse(timestampString);
	// }
	// catch (Exception e)
	// {
	// System.out.println("Exception " + e + " thrown for getting timestamp from " + timestampString);
	// e.printStackTrace();
	// }
	// return instant;
	// }
	
	public static Timestamp getEarliestTimestamp(ArrayList<ActivityObject> activityEvents)
	{
		Timestamp earliestTimestamp = new Timestamp(9999 - 1900, 0, 0, 0, 0, 0, 0);
		
		for (ActivityObject activityEvent : activityEvents)
		{
			if (activityEvent.getStartTimestamp().before(earliestTimestamp))
				earliestTimestamp = activityEvent.getStartTimestamp();
		}
		
		System.out.println("Earliest timestamp for this array of activity events is" + earliestTimestamp);
		return earliestTimestamp;
	}
	
	public static Timestamp getLastTimestamp(ArrayList<ActivityObject> activityEvents)
	{
		Timestamp lastTimestamp = new Timestamp(0 - 1900, 0, 0, 0, 0, 0, 0);
		
		for (ActivityObject activityEvent : activityEvents)
		{
			if (activityEvent.getEndTimestamp().after(lastTimestamp))
				lastTimestamp = activityEvent.getEndTimestamp();
		}
		
		System.out.println("last timestamp for this array of activity events is" + lastTimestamp);
		return lastTimestamp;
	}
	
	public static int getLengthInIntervalsOfSplittedTimelines(HashMap<String, ArrayList<String>> timeLines)
	{
		Iterator it = timeLines.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry) it.next();
			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();
			
			return activityNames.size();
		}
		return 0;
	}
	
	// TODO this method needs refactoring (30 Sep changes: intersectingIntervalInSeconds replaced by doesOverlap
	// 21Oct public static String getActivityNameForInterval(Timestamp earliestTimestamp, Timestamp lastTimestamp, int intervalIndex, int timeUnitInSeconds,
	// ArrayList<ActivityObject>
	// activityEvents)
	// {
	// //$$30Sep
	// System.err.println("ERROR: This method needs refactoring");
	// PopUps.showMessage("ERROR: This method needs refactoring");
	// //
	//
	//
	// String activityNameToAssign= "not found";
	//
	// Timestamp startInterval = getIncrementedTimestamp(earliestTimestamp,(intervalIndex * timeUnitInSeconds));
	// Timestamp endInterval = getIncrementedTimestamp(earliestTimestamp,((intervalIndex+1) * timeUnitInSeconds));
	// if(endInterval.getTime()> lastTimestamp.getTime())
	// endInterval=lastTimestamp;
	//
	// //$$System.out.print("startinterval:"+startInterval+"endinterval:"+endInterval);
	//
	// for(ActivityObject activityEvent: activityEvents)
	// {
	// if(activityEvent.fullyContainsInterval(startInterval,endInterval)) // the interval falls inside only one activity event
	// {
	// activityNameToAssign = activityEvent.getActivityName();
	// //$$System.out.print("**contains**");
	// }
	// }
	//
	// if(activityNameToAssign.equals("not found")) // the interval falls inside multiple activity events
	// {
	// long longestDuration=0; //in seconds
	// for(ActivityObject activityEvent: activityEvents)
	// {
	// if(activityEvent.intersectingIntervalInSeconds(startInterval,endInterval)> longestDuration) // the interval falls inside only one activity event
	// {
	// longestDuration = activityEvent.intersectingIntervalInSeconds(startInterval,endInterval);
	// activityNameToAssign = activityEvent.getActivityName();
	// }
	// }
	// if(longestDuration>0)
	// {
	//
	// //$$System.out.print("**intersects**");
	// }
	// }
	//
	// if(activityNameToAssign.equals("not found"))
	// {
	// if(startInterval.getMinutes()==59 && endInterval.getHours() ==0 && endInterval.getMinutes()==0)
	// {
	// // all is well because this is the last minute of the day
	// //according to our current data. we have left an interval of a minute before the next day starts
	// }
	// else
	// {
	// System.out.println("Error inside getActivityNameForInterval(): No activity name found for given timeinterval "+startInterval+":"+endInterval+" assigning 'Others'");
	// //System.exit(0);
	//
	// }
	// activityNameToAssign="Others";
	// }
	//
	// //System.out.print(activityNameToAssign+"-"+"\n");
	// return activityNameToAssign;
	// }
	//
	public static Timestamp getEarliestOfAllTimestamp(HashMap<String, ArrayList<ActivityObject>> timelinesToAggregate)
	{
		
		Iterator timelinesIterator = timelinesToAggregate.entrySet().iterator();
		Map.Entry timelineEntry1 = (Map.Entry) timelinesIterator.next();
		
		ArrayList<ActivityObject> activityEvents1 = (ArrayList<ActivityObject>) timelineEntry1.getValue();
		Timestamp earliestOfAll = getEarliestTimestamp(activityEvents1);
		
		while (timelinesIterator.hasNext())
		{
			Map.Entry timelineEntry = (Map.Entry) timelinesIterator.next();
			ArrayList<ActivityObject> activityEvents = (ArrayList<ActivityObject>) timelineEntry.getValue();
			Timestamp currentEarliest = getEarliestTimestamp(activityEvents);
			
			if (currentEarliest.before(earliestOfAll))
				earliestOfAll = currentEarliest;
			
		}
		
		System.out.println("Earliest of all is:" + earliestOfAll);
		return earliestOfAll;
	}
	
	public static Timestamp getLastOfAllTimestamp(HashMap<String, ArrayList<ActivityObject>> timelinesToAggregate)
	{
		Iterator timelinesIterator = timelinesToAggregate.entrySet().iterator();
		Map.Entry timelineEntry1 = (Map.Entry) timelinesIterator.next();
		
		ArrayList<ActivityObject> activityEvents1 = (ArrayList<ActivityObject>) timelineEntry1.getValue();
		Timestamp lastOfAll = getLastTimestamp(activityEvents1);
		// Iterator timelinesIterator = timelinesToAggregate.entrySet().iterator();
		while (timelinesIterator.hasNext())
		{
			Map.Entry timelineEntry = (Map.Entry) timelinesIterator.next();
			ArrayList<ActivityObject> activityEvents = (ArrayList<ActivityObject>) timelineEntry.getValue();
			Timestamp currentLast = getEarliestTimestamp(activityEvents);
			
			if (currentLast.after(lastOfAll))
				lastOfAll = currentLast;
			
		}
		
		System.out.println("Last of all is:" + lastOfAll);
		return lastOfAll;
	}
	
	/*
	 * $$21Oct public static HashMap<String, ArrayList<String>> getSplittedTimelines(HashMap<String, ArrayList<ActivityObject>> timelinesToAggregate, int timeUnitInSeconds) {
	 * HashMap<String, ArrayList<String>> splittedTimelinesForComparison = new HashMap<String,ArrayList<String>> (); /* Key: Identifier for timeline (can be UserID in some case)
	 * Value: An ArrayList of Activity Names (which is an ordered array of time intervals) start time of an activity is = (earliestTime of all the activities in timelines)+
	 * (indexInArrrayList) * timeunit end time of an activity is = (earliestTime of all the activities in timelines)+ (indexInArrrayList+1) * timeunit
	 */
	
	/*
	 * $$21Oct Iterator timelinesIterator = timelinesToAggregate.entrySet().iterator();
	 * 
	 * Timestamp earliestTimestamp=getEarliestOfAllTimestamp(timelinesToAggregate); Timestamp lastTimestamp=getLastOfAllTimestamp(timelinesToAggregate);
	 * 
	 * while (timelinesIterator.hasNext()) { Map.Entry timelineEntry = (Map.Entry)timelinesIterator.next();
	 * 
	 * String id= timelineEntry.getKey().toString(); ArrayList<ActivityObject> activityEvents= (ArrayList<ActivityObject>) timelineEntry.getValue();
	 * 
	 * // Timestamp earliestTimestamp = getEarliestTimestamp(activityEvents); // Timestamp lastTimestamp = getLastTimestamp(activityEvents);
	 * 
	 * if( (lastTimestamp.getTime() - earliestTimestamp.getTime()) <0) System.err.println("Error (in getSplittedTimelines()): Error for length of timelines");
	 * 
	 * long lengthOfTimelineInSeconds= (lastTimestamp.getTime() - earliestTimestamp.getTime())/1000; // this should leave no remainder as our data collection is at not lower than
	 * seconds level long numberOfIndices= (lengthOfTimelineInSeconds / timeUnitInSeconds); System.out.println("Length of timeline in seconds ="+lengthOfTimelineInSeconds+
	 * "; number of whole indices="+numberOfIndices);
	 * 
	 * ArrayList<String> activityNamesForSplittedTimeline=new ArrayList<String> ();
	 * 
	 * int index=0; for(;index<=numberOfIndices; )//int secondsIterator=0;secondsIterator<=lengthOfTimelineInSeconds;) { String activityNameForThisInterval=
	 * getActivityNameForInterval(earliestTimestamp,lastTimestamp, index,timeUnitInSeconds,activityEvents); //System.out.print("@@ adding: "+activityNameForThisInterval+" ");
	 * activityNamesForSplittedTimeline.add(activityNameForThisInterval); //secondsIterator = secondsIterator+timeUnitInSeconds; index ++; }
	 * 
	 * //if lengthOfTimeline is not perfectly divisible by timeUnitInSeconds. // then the last remainder seconds make up the last element of arrayList (which is of smaller duration
	 * than timeUnitInSeconds; if((lengthOfTimelineInSeconds % timeUnitInSeconds) !=0) { //NOT SURE/SURE BUT NOT WHY String activityNameForThisInterval=
	 * getActivityNameForInterval(earliestTimestamp,lastTimestamp, index,timeUnitInSeconds,activityEvents); //NOT SURE/SURE BUT NOT WHY
	 * activityNamesForSplittedTimeline.add(activityNameForThisInterval); System.out.println("has remainder ="+(lengthOfTimelineInSeconds % timeUnitInSeconds)+" seconds"); }
	 * 
	 * System.out.println("Length of ArrayList of splitted timeline is "+activityNamesForSplittedTimeline.size());
	 * 
	 * splittedTimelinesForComparison.put(id, activityNamesForSplittedTimeline);
	 * 
	 * System.out.println(); //timelinesIterator.remove(); // avoids a ConcurrentModificationException }
	 * 
	 * return splittedTimelinesForComparison; }
	 */// 21Oct
	
	public static ArrayList<String> getActivityNamesAtIndex(int intervalIndex,
			HashMap<String, ArrayList<String>> splittedTimelinesForComparison)
	{
		ArrayList<String> activityNamesForComparison = new ArrayList<String>();
		Iterator it = splittedTimelinesForComparison.entrySet().iterator();
		int count = 0; // we will aggregate only two timelines at a time.
		
		while (it.hasNext()) // one iteration for one timeline
		{
			Map.Entry pairs = (Map.Entry) it.next();
			
			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();
			activityNamesForComparison.add(activityNames.get(intervalIndex));
			count++;
		} // all activity names for comparison collected
		
		return activityNamesForComparison;
	}
	
	/**
	 * Checks if the Activity names from the given timelines at the next intervalIndex are different
	 * 
	 * @param intervalIndex
	 * @param splittedTimelinesForComparison
	 * @return true if the activity names are different, false if all the activity names are same
	 */
	public static boolean nextIntervalActivityNamesAreDifferent(int intervalIndex,
			HashMap<String, ArrayList<String>> splittedTimelinesForComparison)
	{
		boolean different = true;
		
		ArrayList<String> activityNamesForComparison = new ArrayList<String>();
		
		Iterator it = splittedTimelinesForComparison.entrySet().iterator();
		int count = 0; // we will aggregate only two timelines at a time.
		
		while (it.hasNext())// Iterating over timelines, each iteration corresponds to a single timeline && count <2)
		{
			Map.Entry pairs = (Map.Entry) it.next();
			
			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();
			activityNamesForComparison.add(activityNames.get(intervalIndex + 1)); // add the activityName at that index for the current timeline
			count++;
		} // all activity names for comparison collected
		
		boolean same = true; // all activity names at that interval index for all the splittedtimelines are same
		
		String firstName = activityNamesForComparison.get(0);
		for (int i = 1; i < activityNamesForComparison.size(); i++)
		{
			if (activityNamesForComparison.get(i).equals(firstName) == false)
			{
				same = false;
				break;
			}
		}
		different = !same;
		
		return different;
		// only for two timelines
		// return !(activityNamesForComparison.get(0).equals(activityNamesForComparison.get(1)));
	}
	
	/**
	 * Checks if the Activity names from the given timelines at the next intervalIndex are different and and also belong to different categories
	 * 
	 * @param intervalIndex
	 * @param splittedTimelinesForComparison
	 * @return true if the activity names and categories are different, false if all the activity names and categories are same
	 */
	public static boolean nextIntervalActivityNamesAreTotallyDifferent(int intervalIndex,
			HashMap<String, ArrayList<String>> splittedTimelinesForComparison)
	{
		boolean different = true;
		
		ArrayList<String> activityNamesForComparison = new ArrayList<String>();
		
		ArrayList<String> activityCategoriesForComparison = new ArrayList<String>();
		
		Iterator it = splittedTimelinesForComparison.entrySet().iterator();
		int count = 0; // we will aggregate only two timelines at a time.
		
		while (it.hasNext())// Iterating over timelines, each iteration corresponds to a single timeline && count <2)
		{
			Map.Entry pairs = (Map.Entry) it.next();
			
			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();
			activityNamesForComparison.add(activityNames.get(intervalIndex + 1)); // add the activityName at that index for the current timeline
			activityCategoriesForComparison.add(generateSyntheticData.getActivityCategory(activityNames.get(intervalIndex + 1))); // add the activityName at that index for the
																																	// current timeline
			count++;
		} // all activity names for comparison collected
		
		boolean sameName = areAllStringsInListSame(activityNamesForComparison);// all activity names at that interval index for all the splittedtimelines are same
		
		boolean sameCategory = true; // all activity names at that interval index for all the splittedtimelines are of same category
		if (sameName == false) // if all the activity names at next intervalIndex are not same, we check if they are of same category or not
		{
			sameCategory = areAllStringsInListSame(activityCategoriesForComparison);
		}
		/*
		 * true; // all activity names at that interval index for all the splittedtimelines are same
		 * 
		 * String firstName=activityNamesForComparison.get(0); for(int i=1;i<activityNamesForComparison.size();i++) { if(activityNamesForComparison.get(i).equals(firstName) ==
		 * false) { sameName =false; break; } }
		 */
		
		/*
		 * boolean sameCategory = true; // all activity names at that interval index for all the splittedtimelines are of same category if(sameName == false ) // if all the
		 * activity names at next intervalIndex are not same, we check if they are of same category or not { String firstCategory=activityCategoriesForComparison.get(0);
		 * 
		 * for(int i=1;i<activityCategoriesForComparison.size();i++) { if(activityCategoriesForComparison.get(i).equals(firstCategory) == false) { sameCategory =false; break; } } }
		 */
		
		different = !(sameName && sameCategory);
		
		// Checking if it works (only for aggregating two timelines
		/*
		 * if(!sameName && sameCategory) { System.out.println("Check: not same name but same category- "+activityNamesForComparison.get(0)+" and "
		 * +activityNamesForComparison.get(1)); }
		 */
		
		return different;
		// only for two timelines
		// return !(activityNamesForComparison.get(0).equals(activityNamesForComparison.get(1)));
	}
	
	public static boolean areAllStringsInListSame(ArrayList<String> listToCheck)
	{
		
		boolean same = true;
		
		if (listToCheck.size() < 2)
		{
			System.err.println("Error in areAllStringInListSame(): less than 2 elements in list");
			return true;
		}
		
		String first = listToCheck.get(0);
		
		for (int i = 1; i < listToCheck.size(); i++)
		{
			if (listToCheck.get(i).equals(first) == false)
			{
				same = false;
				break;
			}
		}
		return same;
	}
	
	/**
	 * If there is only one non other activity in the arraylist, then that non other activity name is returned
	 * 
	 * @param listToCheck
	 * @return the single non-other activity name
	 */
	public static String ifOnlyOneNonOtherInList(ArrayList<String> listToCheck)
	{
		
		// boolean onlyOneNonOther = false;
		int countOfNonOthers = 0;
		String theNonOtherName = "null";
		
		for (int i = 0; i < listToCheck.size(); i++)
		{
			if (listToCheck.get(i).equalsIgnoreCase("Others") == false)
			{
				countOfNonOthers++;
				theNonOtherName = listToCheck.get(i);
			}
		}
		
		if (countOfNonOthers == 1)
		{
			return theNonOtherName;
		}
		
		return "null";
	}
	
	/**
	 * Retunr the number of unique elements in the ArrayList (primarily used to find the number of unique activity names in the disagreement space)
	 * 
	 * @param arrayList
	 * @return
	 */
	public static int getCountUniqueActivites(ArrayList<String> listToCount)
	{
		ArrayList<String> dummyList = new ArrayList<String>();
		
		for (int i = 0; i < listToCount.size(); i++)
		{
			if (!(dummyList.contains(listToCount.get(i))))
			{
				dummyList.add(listToCount.get(i));
			}
			
		}
		
		return dummyList.size();
	}
	
	public static ArrayList<String> getActivityCategoriesList(ArrayList<String> activityNames)
	{
		ArrayList<String> activityCategories = new ArrayList<String>();
		
		for (int i = 0; i < activityNames.size(); i++)
		{
			activityCategories.add(generateSyntheticData.getActivityCategory(activityNames.get(i)));
		}
		
		if (activityCategories.size() != activityNames.size())
		{
			System.err.println("Error: in getting activity category list from activity name list");
		}
		
		return activityCategories;
	}
	
	// /*21Oct
	// public static String aggregateTimelines(HashMap<String, ArrayList<ActivityObject>> timeLinesToAggregate,String aggregateByString, String methodForDisagreementSpace)
	// {
	// System.out.println("***********inside aggregate timelines:************"+methodForDisagreementSpace);
	// System.out.println(" number of timelines to agggregate = "+timeLinesToAggregate.size());
	//
	// int thresholdForDisagreementSpace= timeLinesToAggregate.size()+1;
	// System.out.println("Threshold for disagreement space: num of unique activitiy names <="+thresholdForDisagreementSpace);
	//
	// ArrayList<String> aggregatedSplittedTimeline = new ArrayList<String> ();
	//
	//
	// //Step 1
	// int timeUnitInSeconds=getTimeUnitInSeconds();
	//
	// //Step 2: Split the timelines into timeunit sized time intervals.
	// HashMap<String, ArrayList<String>> splittedTimelinesForComparison =getSplittedTimelines(timeLinesToAggregate, timeUnitInSeconds);
	// System.out.println("number of splitted timelines ="+splittedTimelinesForComparison.size());
	// /* Key: Identifier for timeline (can be UserID in some case)
	// Value: An ArrayList of Activity Names (which is an ordered array of time intervals)
	// start time of an activity is = (earliestTime of all the activities in timelines)+ (index) * timeunit
	// end time of an activity is = (earliestTime of all the activities in timelines)+ (index+1) * timeunit
	// */
	// //$$traverseSplittedTimelines(splittedTimelinesForComparison);
	//
	// // all splitted timelines will be of same length.
	// /*21Oct int lengthInIntervalsOfSplittedTimelines= getLengthInIntervalsOfSplittedTimelines(splittedTimelinesForComparison);
	// System.out.println("length of splitted timelines ="+lengthInIntervalsOfSplittedTimelines+" intervals");
	//
	// // Step 3: actually aggregate the timelines
	// ArrayList<String> disagreementSpace= new ArrayList<String> ();
	// int disagreementLengthInIntervals =0;
	// int addCounter=0;
	// for(int intervalIndex=0;intervalIndex<lengthInIntervalsOfSplittedTimelines;)//intervalIndex++)
	// {
	// // disagreementLengthInIntervals =0;
	//
	// //check for case a, b and c
	// ArrayList<String> activityNamesForComparison= new ArrayList<String>();
	//
	// activityNamesForComparison=getActivityNamesAtIndex(intervalIndex, splittedTimelinesForComparison);
	//
	// //System.out.println(activityNamesForComparison.size()+" activity names for comparison");
	// //Case a
	// if(areAllStringsInListSame(activityNamesForComparison))//activityNamesForComparison.get(0).equals(activityNamesForComparison.get(1)))
	// {
	// //$$ENABLESystem.out.println("Case A "+activityNamesForComparison.get(0)+" and "+activityNamesForComparison.get(1));
	// aggregatedSplittedTimeline.add(activityNamesForComparison.get(0));
	// addCounter++;
	// intervalIndex++;
	// disagreementSpace.clear();
	// disagreementLengthInIntervals =0;
	// continue;
	// }
	//
	// //Case b
	//
	// //ifOnlyOneNonOtherInList
	//
	//
	// if(ifOnlyOneNonOtherInList(activityNamesForComparison).equals("null") == false) //something other than "null" returned
	// {
	// //$$ENABLESystem.out.println("Case B "+activityNamesForComparison.get(0)+" and "+activityNamesForComparison.get(1));
	// aggregatedSplittedTimeline.add(ifOnlyOneNonOtherInList(activityNamesForComparison));
	// addCounter++;
	// intervalIndex++;
	// disagreementSpace.clear();
	// disagreementLengthInIntervals =0;
	// continue;
	// }
	//
	//
	// //Case c
	// ArrayList<String> activityCategoriesForComparison= getActivityCategoriesList(activityNamesForComparison);
	//
	//
	// if(areAllStringsInListSame(activityCategoriesForComparison))
	// {
	// //$$ENABLESystem.out.println("Case C "+activityNamesForComparison.get(0)+" and "+activityNamesForComparison.get(1));
	// aggregatedSplittedTimeline.add(activityCategoriesForComparison.get(0));
	// addCounter++;
	// intervalIndex++;
	// disagreementSpace.clear();
	// disagreementLengthInIntervals =0;
	// continue;
	// }
	//
	// //Case d
	// //$$System.out.println("Case D "+activityNamesForComparison.get(0)+" and "+activityNamesForComparison.get(1));
	//
	// disagreementSpace.addAll(activityNamesForComparison);
	// disagreementLengthInIntervals++;
	//
	//
	// //enlarge the disagreement space as long as the activities are differnt
	// while(true)
	// {
	// intervalIndex++;
	// if(( intervalIndex<lengthInIntervalsOfSplittedTimelines)
	// && (nextIntervalActivityNamesAreTotallyDifferent(intervalIndex-1,splittedTimelinesForComparison))
	// // && (getCountUniqueActivites(disagreementSpace) <= thresholdForDisagreementSpace)
	// )
	// {
	//
	// if(getCountUniqueActivites(disagreementSpace) > thresholdForDisagreementSpace) //CHECK suppposedly this leads to inclusion of one extra (time unit) sized interval (check it
	// by running for
	// 1 hr
	// time unit size)
	// {
	// //$$System.out.println("breaking because number of actities in space= "+disagreementSpace.size()+"the count of unique activites in disagreement space
	// is"+getCountUniqueActivites(disagreementSpace));
	//
	// break;
	// }
	// activityNamesForComparison=getActivityNamesAtIndex(intervalIndex, splittedTimelinesForComparison);
	// disagreementSpace.addAll(activityNamesForComparison);
	// disagreementLengthInIntervals++;
	//
	// //$$ENABLE
	// System.out.println("number of actities in space= "+disagreementSpace.size()+"the count of unique activites in disagreement space
	// is"+getCountUniqueActivites(disagreementSpace));
	// /*if(getCountUniqueActivites(disagreementSpace) > thresholdForDisagreementSpace)
	// {
	// System.out.println("breaking");
	// break;
	// }
	// else
	// */ continue;
	//
	// }
	// else
	// {
	// break;
	// }
	//
	// }
	//
	// System.out.println("Number of elements in disagreementSpace ="+disagreementSpace.size());
	// System.out.println("disagreementLengthInIntervals ="+disagreementLengthInIntervals);
	//
	// String activityNameForDisagreementSpace=" ";
	// switch (methodForDisagreementSpace)
	// {
	// case "MaxFrequency":
	// activityNameForDisagreementSpace = getMaximumFrequencyActivityName(disagreementSpace);
	// break;
	// case "ThresholdFrequency":
	// activityNameForDisagreementSpace = getThresholdFrequencyActivityName(disagreementSpace,ThresholdFrequency,true);
	// break;
	// /* case "AdhocTaxonomy":
	// activityNameForDisagreementSpace = getSuperAdhocTaxonomyActivityName(disagreementSpace);
	// break;
	// case "Cluster":
	// activityNameForDisagreementSpace = getSuperClusterActivityName(disagreementSpace);
	// break;*/
	// default:
	// System.err.println("Error for disagreement space: UNRECOGNIZED method for aggregating disagreement space.");
	// break;
	//
	// }
	//
	// System.out.println(" activityNameForDisagreementSpace = "+activityNameForDisagreementSpace);
	//
	//
	// for(int i=0;i<disagreementLengthInIntervals;i++)
	// {
	// aggregatedSplittedTimeline.add(activityNameForDisagreementSpace);
	// addCounter++;
	// }
	// disagreementSpace.clear();
	// disagreementLengthInIntervals =0;
	// // intervalIndex++;
	// }
	//
	//
	// // SOME ISSUE WITH TRaversing timeline to split as splitted timeline
	// System.out.println("First is"+splittedTimelinesForComparison.get("0").size()+" elements");
	// System.out.println("Second is"+splittedTimelinesForComparison.get("1").size()+" elements");
	// System.out.println("Aggregated is"+aggregatedSplittedTimeline.size()+" elements");
	// System.out.println("add counter is"+addCounter);
	//
	// // System.out.println("Second is"+splittedTimelinesForComparison.get(1).size()+" elements");
	//
	//
	// // UNCOMMENT BELOW TO SEE SPLITTED TIMELINES
	// //$$traverseSingleSplittedTimeline("First timeline splitted",splittedTimelinesForComparison.get("0"));
	// //$$traverseSingleSplittedTimeline("Second timeline splitted",splittedTimelinesForComparison.get("1"));
	// //$$traverseSingleSplittedTimeline("Aggregated timeline splitted",aggregatedSplittedTimeline);
	//
	// System.out.println("XXXXXend of aggregate timeline XXXXXXXXXXXXX");
	// Timestamp earliestTimestamp= getEarliestTimestamp(timeLinesToAggregate.get("0"));
	// Timestamp lastTimestamp = getLastTimestamp(timeLinesToAggregate.get("0"));
	// System.out.println("Earliest timestamp="+getEarliestTimestamp(timeLinesToAggregate.get("0")) );
	//
	// String aggregatedTimelineasJsonString = createJSONString(aggregatedSplittedTimeline,earliestTimestamp, lastTimestamp);
	// return aggregatedTimelineasJsonString;
	// }
	// //21Oct
	
	public static String createJSONString(ArrayList<String> aggregatedSplittedTimeline, Timestamp earliestTimestamp,
			Timestamp lastTimestamp)
	{
		String JSONString = "[";
		int timeUnitInSeconds = DateTimeUtils.getTimeUnitInSeconds();
		int lengthInIntervalsOfSplittedTimelines = aggregatedSplittedTimeline.size();// getLengthInIntervalsOfSplittedTimelines(aggregatedSplittedTimeline);
		// System.out.println("Inside create JSONString from splitter timeline");
		System.out.println(
				"Inside create JSONString from splitter timeline : size of splitted timeline =" + lengthInIntervalsOfSplittedTimelines);
		
		int counter = 0;
		for (; counter < aggregatedSplittedTimeline.size();)// String activityName: aggregatedSplittedTimeline)
		{
			// Timestamp startTimestamp= earliestTimestamp + counter* timeUnitInSeconds ;
			// Timestamp = earliestTimestamp + (counter+1)* timeUnitInSeconds ;
			String activityName = aggregatedSplittedTimeline.get(counter);
			// if(activityName.equals("Others"))
			// activityName="Ajooba";
			
			Timestamp startTimestamp = DateTimeUtils.getIncrementedTimestamp(earliestTimestamp, (counter * timeUnitInSeconds));
			Timestamp endTimestamp = DateTimeUtils.getIncrementedTimestamp(earliestTimestamp, ((counter + 1) * timeUnitInSeconds));
			
			// Merge consecutively similar activities
			while (((counter + 1) < lengthInIntervalsOfSplittedTimelines)
					&& (activityName.equals(aggregatedSplittedTimeline.get(counter + 1)))
					&& DateTimeUtils.isSameDate(startTimestamp, endTimestamp) // startTimestamp.getDate()
			// ==
			// endTimestamp.getDate()
			// // do
			// not merge
			// activities
			// from
			// different
			// days
			// (because
			// it
			// makes
			// sense and
			// also
			// because
			// it causes
			// problem
			// because
			// this
			// merger
			// would
			// mean we
			// will have
			// to allow
			// (startTimestamp.getHour()
			// >
			// endTimestamp.getHour())
			) // matching date to identify same day
			{
				counter++;
				endTimestamp = DateTimeUtils.getIncrementedTimestamp(earliestTimestamp, ((counter + 1) * timeUnitInSeconds)); // end time stamp of the new merged activity event
				// check if it is split for the last interval of the day
			}
			
			// check if it is split for the last interval of the day
			if (startTimestamp.getHours() > endTimestamp.getHours() // also WORKS (endTimestamp.getHours()== 0)
			)// && (endTimestamp.getMinutes()*60) < getTimeUnitInSeconds())
			{
				Timestamp newEndTimestamp =
						new Timestamp(endTimestamp.getYear(), endTimestamp.getMonth(), endTimestamp.getDay(), 23, 59, 59, 0);
				endTimestamp = newEndTimestamp;
			}
			
			// System.out.println("End time stamp: "+endTimestamp);
			if (endTimestamp.getTime() > lastTimestamp.getTime())
				endTimestamp = lastTimestamp;
			
			endTimestamp = new Timestamp(endTimestamp.getTime() - 1); // decrease a millisecond
			// in the above statement we decrease a milisecond from the end timestamp, so there is a gap of one millisecond before the end of activity and start of next activity.
			// Without this gap
			// in
			// time intervals (i.e,
			// if the end of an activity was exactly the same as start of next activity), it was causing rendering delay in the visjs timeline visualisation.
			
			String JSONObjectString = " {\"Activity Name\":\"" + activityName + "\",";
			
			JSONObjectString = JSONObjectString + "\"End Time\":\"" + DateTimeUtils.getTimeString(endTimestamp) + "\",";
			JSONObjectString = JSONObjectString + "\"Start Time\":\"" + DateTimeUtils.getTimeString(startTimestamp) + "\",";
			
			JSONObjectString = JSONObjectString + "\"User ID\":" + 99 + ",";
			JSONObjectString = JSONObjectString + "\"Date\":\"" + DateTimeUtils.getDateString(startTimestamp) + "\" },";
			
			JSONString = JSONString + JSONObjectString;
			counter++;
		}
		
		JSONString = JSONString.substring(0, JSONString.length() - 1);
		
		JSONString = JSONString + "]";
		
		// System.out.println("JSON String is--------------"+JSONString);
		
		return JSONString;
	}
	
	public static HashMap<String, Integer> getActivityNamesCountMap(ArrayList<String> disagreementSpace)
	{
		HashMap<String, Integer> activityNamesCount = new HashMap<String, Integer>();
		// < Activity Name, number of times the activity name appear in the disagreement space>
		for (int i = 0; i < disagreementSpace.size(); i++)
		{
			String currentActivityName = disagreementSpace.get(i);
			if (!(activityNamesCount.containsKey(currentActivityName)))
			{
				activityNamesCount.put(currentActivityName, new Integer(1));
			}
			else
			{
				activityNamesCount.put(currentActivityName, new Integer(activityNamesCount.get(currentActivityName).intValue() + 1));
			}
			
		}
		return activityNamesCount;
	}
	
	public static String getMaximumFrequencyActivityName(ArrayList<String> disagreementSpace)
	{
		String maximalActivityName = "not";
		String maximalsString = "";// useful in case of more than one maximals
		// ArrayList<String> maximals=new ArrayList<String> ();
		int maximalCount = 0;
		
		HashMap<String, Integer> activityNamesCount = getActivityNamesCountMap(disagreementSpace);
		// < Activity Name, number of times the activity name appear in the disagreement space>
		
		for (Map.Entry<String, Integer> entry : activityNamesCount.entrySet())
		{
			if (entry.getValue().intValue() > maximalCount)
			{
				maximalActivityName = entry.getKey();
				maximalCount = entry.getValue();
			}
		}
		
		if (countOfAboveThresholdFrequencyActivityName(disagreementSpace, maximalCount - 1) > 1)
		{
			maximalActivityName = getThresholdFrequencyActivityName(disagreementSpace, maximalCount - 1, true); // in case of ties at maximal count
		}
		
		else
			// single maximal
			maximalActivityName = getThresholdFrequencyActivityName(disagreementSpace, maximalCount - 1, false); // in case of ties at maximal count
		// $$ENABLESystem.out.print(" maximal activity name="+maximalActivityName+", with count="+maximalCount);
		
		return maximalActivityName;
	}
	
	/**
	 * 
	 * 
	 * @param disagreementSpace
	 * @param thresholdFrequency
	 * @param withCount
	 *            if set to true the activity name above threshold will be catenated with the count of occurrence of that activity in the current disagreement space, for example:
	 *            Computer#100
	 * @return
	 */
	public static String getThresholdFrequencyActivityName(ArrayList<String> disagreementSpace, int thresholdFrequency, boolean withCount)
	{
		String aboveThresholdActivityNames = "";
		int count = 0;
		
		HashMap<String, Integer> activityNamesCount = getActivityNamesCountMap(disagreementSpace);
		// < Activity Name, number of times the activity name appear in the disagreement space>
		
		for (Map.Entry<String, Integer> entry : activityNamesCount.entrySet())
		{
			if (entry.getValue().intValue() > thresholdFrequency)
			{
				aboveThresholdActivityNames = aboveThresholdActivityNames + "," + entry.getKey();// +"#"+entry.getValue();
				
				if (withCount)
				{
					aboveThresholdActivityNames = aboveThresholdActivityNames + "#" + entry.getValue();
				}
				count++;
			}
		}
		
		// $$ENABLESystem.out.print(" Above Threshold("+ thresholdFrequency+") activity names="+aboveThresholdActivityNames);
		if (count == 0)
		{
			System.out.println("No activity name above threshold, hence we use maxFrequency ");
			aboveThresholdActivityNames = getMaximumFrequencyActivityName(disagreementSpace);
		}
		
		else
			// trim first comma
			aboveThresholdActivityNames = aboveThresholdActivityNames.substring(1);
	
		return aboveThresholdActivityNames;
	}
	
	//
	public static int countOfAboveThresholdFrequencyActivityName(ArrayList<String> disagreementSpace, int thresholdFrequency)
	{
		// String aboveThresholdActivityNames= "";
		int count = 0;
		
		HashMap<String, Integer> activityNamesCount = getActivityNamesCountMap(disagreementSpace);
		// < Activity Name, number of times the activity name appear in the disagreement space>
		
		for (Map.Entry<String, Integer> entry : activityNamesCount.entrySet())
		{
			if (entry.getValue().intValue() > thresholdFrequency)
			{
				
				count++;
			}
		}
		
		return count;
	}
	
	//
	
	/**
	 * Takes in a JSON Array, extracts sets of Dimension IDs for each element from the Array and create Activity Objects using those sets of Dimension IDs.
	 * 
	 * @param jsonArray
	 * @return an ArrayList of all activityEvents created from the jsonArray (note: this json array was formed from all values satisfying select and where clause of query)
	 */
	public static ArrayList<ActivityObject> createActivityObjectsFromJsonArray(JSONArray jsonArray)
	{
		// HashMap<String,ArrayList<ActivityObject>> timeLines = new HashMap<String,ArrayList<ActivityObject>> ();
		// Key: Identifier for timeline (can be UserID in some case)
		// Value: An ArrayList of ActivityEvents
		ArrayList<ActivityObject> allActivityObjects = new ArrayList<ActivityObject>();
		
		System.out.println("inside createActivityEventsFromJsonArray: checking parsing json array");
		System.out.println("number of elements in json array = " + jsonArray.length());
		long dt = System.currentTimeMillis();
		
		try
		{
			for (int i = 0; i < jsonArray.length(); i++)
			{
				if ((i % 1000 == 0))// || (i == (jsonArray.length()-1)))
				{
					System.out.println(" JSON Array index:" + i);// //@toremoveatruntime
				}
				HashMap<String, String> dimensionIDsForActivityEvent = new HashMap<String, String>();// (User_ID, 0), (Location_ID, 10100), ...
				// if(i%100==0)
				// System.out.println(i);
				/* TO DO:Can DO: make it generic, extract primary key names from key table in database instead of writing explicity */
				dimensionIDsForActivityEvent.put("User_ID", jsonArray.getJSONObject(i).get("User_ID").toString());
				dimensionIDsForActivityEvent.put("Activity_ID", jsonArray.getJSONObject(i).get("Activity_ID").toString());
				dimensionIDsForActivityEvent.put("Time_ID", jsonArray.getJSONObject(i).get("Time_ID").toString());
				dimensionIDsForActivityEvent.put("Date_ID", jsonArray.getJSONObject(i).get("Date_ID").toString());
				dimensionIDsForActivityEvent.put("Location_ID", jsonArray.getJSONObject(i).get("Location_ID").toString());
				
				allActivityObjects.add(new ActivityObject(dimensionIDsForActivityEvent));
			} // note: the ActivityEvents obtained from the database are not in chronological order, thus the ArrayList as of now does not contain Activity Events in chronological
				// order. Also its
				// a
				// good assumption for
				// results obtained using any general database.
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long lt = System.currentTimeMillis();
		
		System.out.println("exiting createActivityEventsFromJsonArray with " + allActivityObjects.size() + " Activity Objects created in"
				+ (lt - dt) / 1000 + " secs");
		return allActivityObjects;
	}
	
	/*
	 * public static HashMap<String,ArrayList<UserDayTimeline>> createUserTimelinesFromJsonArray(JSONArray jsonArray) { HashMap<String, ArrayList<UserDayTimeline>> userTimelines=
	 * new HashMap<String, ArrayList<UserDayTimeline>> ();
	 * 
	 * // Key: userid // Value: An ArrayList of day timeline
	 * 
	 * 
	 * HashMap<String, ArrayList<ActivityObject>> perUserActivityEvents= new HashMap<String, ArrayList<ActivityObject>>();
	 * 
	 * //ArrayList<ActivityObject> allActivityEvents= new ArrayList<ActivityObject> ();
	 * 
	 * System.out.println("inside createUserTimelinesFromJsonArray: checking parsing json array"); System.out.println("number of elements in json array = "+jsonArray.length());
	 * 
	 * try { for(int i=0;i<jsonArray.length();i++) { HashMap<String,String> dimensionIDsForActivityEvent = new HashMap<String,String> ();
	 * 
	 * /*TO DO:Can DO: make it generic, extract primary key names from key table in database instead of writing explicity
	 */
	/*
	 * dimensionIDsForActivityEvent.put("User_ID",jsonArray.getJSONObject(i).get("User_ID").toString());
	 * dimensionIDsForActivityEvent.put("Activity_ID",jsonArray.getJSONObject(i).get("Activity_ID").toString());
	 * dimensionIDsForActivityEvent.put("Time_ID",jsonArray.getJSONObject(i).get("Time_ID").toString());
	 * dimensionIDsForActivityEvent.put("Date_ID",jsonArray.getJSONObject(i).get("Date_ID").toString());
	 * dimensionIDsForActivityEvent.put("Location_ID",jsonArray.getJSONObject(i).get("Location_ID").toString());
	 * 
	 * String userId= jsonArray.getJSONObject(i).get("User_ID").toString();
	 * 
	 * ActivityObject activityEventFormed= new ActivityObject(dimensionIDsForActivityEvent);
	 * 
	 * //String dateString=activityEventFormed.geDimensionAttributeValue("Date_Dimension","Date").toString();
	 * 
	 * if(!(userTimelines.containsKey(userId))) // if its a new user , create a new entry in hashmap { userTimelines.put(userId, new ArrayList<UserDayTimeline> ()); }
	 * 
	 * // get corresponding timeline (ArrayList of ActivityEvents) and add the Activity Event to that ArrayList ActivityObject activityEvent=new ActivityObject(activityName,
	 * startTime, endTime); timeLines.get(userID).add(activityEvent);
	 * 
	 * 
	 * allActivityEvents.add(new ActivityObject(dimensionIDsForActivityEvent)); } //note: the ActivityEvents obtained from the database are not in chronological order, thus the
	 * ArrayList as of now does not contain Activity Events in chronological order. Also its a good assumption for results obtained using any general database. }
	 * 
	 * catch(Exception e) { e.printStackTrace(); }
	 * 
	 * return allActivityEvents; }
	 */
	
	public static void traverseActivityEvents(ArrayList<ActivityObject> activityEvents)
	{
		System.out.println("** Traversing Activity Events **");
		for (int i = 0; i < activityEvents.size(); i++)
		{
			(activityEvents.get(i)).traverseActivityObject();
		}
		System.out.println("** End of Traversing Activity Events **");
	}
	
	public static int safeLongToInt(long l)
	{
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
	
	/**
	 * <font color = blue> Note: using the preferred way to convert double to BigDecimal</font>
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static double round(double value, int places)
	{
		if (Double.isInfinite(value))
		{
			return 99999;
		}
		if (Double.isNaN(value))
		{
			return 0;
		}
		
		if (places < 0)
			throw new IllegalArgumentException();
		
		BigDecimal bd = BigDecimal.valueOf(value);// new BigDecimal(value); //change on 22 Nov 2016
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	/**
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static String round(String value, int places)
	{
		if (places < 0)
			throw new IllegalArgumentException();
		
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}
	
	public static String getActivityNamesFromArrayList(ArrayList<ActivityObject> activityEvents)
	{
		String res = "";
		
		for (ActivityObject ae : activityEvents)
		{
			res += "_" + ae.getActivityName();
		}
		return res;
	}
	
	public static UserDayTimeline getUserDayTimelineByDateFromMap(LinkedHashMap<Date, UserDayTimeline> dayTimelinesForUser, Date dateA)
	{
		for (Map.Entry<Date, UserDayTimeline> entry : dayTimelinesForUser.entrySet())
		{
			// System.out.println("Date ="+entry.getKey());
			// if(entry.getKey().toString().equals((new Date(2014-1900,4-1,10)).toString()))
			// System.out.println("!!!!!!!!E U R E K A !!!!!!!");
			
			if (entry.getKey().toString().equals(dateA.toString()))
			{
				// System.out.println("!!!!!!!FOUND THE O N E!!!!!!");
				return entry.getValue();
			}
		}
		
		return null;
		
	}
	
	public static Integer getIntegerByDateFromMap(LinkedHashMap<Date, Integer> map, Date dateA)
	{
		for (Map.Entry<Date, Integer> entry : map.entrySet())
		{
			// System.out.println("Date ="+entry.getKey());
			// if(entry.getKey().toString().equals((new Date(2014-1900,4-1,10)).toString()))
			// System.out.println("!!!!!!!!E U R E K A !!!!!!!");
			
			if (entry.getKey().toString().equals(dateA.toString()))
			{
				// System.out.println("!!!!!!!FOUND THE O N E!!!!!!");
				return entry.getValue();
			}
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * @param userID
	 * @return index of the user id in the list of user ids
	 */
	public static int getIndexOfUserID(int userID)
	{
		int res = -99;
		int userIDs[] = Constant.userIDs;
		for (int i = 0; i < userIDs.length; i++)
		{
			if (userIDs[i] == userID)
			{
				res = i;
				break;
			}
		}
		
		if (res < 0)
		{
			System.err.println("Error in UtilityBelt.getIndexOfUserID for user " + userID + ": userID not found in class array userIDs");
			System.err.println("User ids in class array:");
			for (int k : userIDs)
			{
				System.err.print(k + ",");
			}
			
			new Exception().printStackTrace();
		}
		return res;
	}
	
	/**
	 * Rearranges the map according to the given order of keys. This is useful when original userIDs are to be considered in a different order, for example, as with Geolife
	 * dataset.
	 * 
	 * @param map
	 * @param orderKeys
	 * @return
	 */
	public static LinkedHashMap<String, Integer> rearrangeByGivenOrder(LinkedHashMap<String, Integer> map, int[] orderKeys)
	{
		LinkedHashMap<String, Integer> rearranged = new LinkedHashMap<String, Integer>();
		
		for (int key : orderKeys)
		{
			String keyString = Integer.toString(key);
			rearranged.put(keyString, map.get(keyString));
		}
		
		return rearranged;
	}
	
	/**
	 * changes the userIDs to be from 1 to n
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>
			reformatUserIDs(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> map)
	{
		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> rearranged =
				new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
		
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : map.entrySet())
		{
			int newUserID = UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1;
			rearranged.put(String.valueOf(newUserID), entry.getValue());
		}
		return rearranged;
	}
	
	/**
	 * Execute any *nix shell command and returns the output as String
	 * 
	 * @param command
	 * @return
	 */
	public static String executeShellCommand(String command)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = "";
			
			int count = 0;
			while ((line = reader.readLine()) != null)
			{
				// System.out.println("aaa");
				output.append(line + "\n");
				count++;
			}
			System.out.println(count - 1 + " lines");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return output.toString();
	}
	
	public static String executeShellCommand4(String command)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			System.out.println("starting executeShellCommand4");
			p = Runtime.getRuntime().exec(command);
			System.out.println("before waitFor");
			// p.waitFor();
			System.out.println("after waitFor");
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = "";
			
			int count = 0;
			while ((line = reader.readLine()).equals("end of program") == false)
			{
				// System.out.println("aaa");
				output.append(line + "\n");
				count++;
			}
			System.out.println(count - 1 + " lines");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return output.toString();
	}
	
	public static String executeShellCommand2(String command)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = "";
			
			int count = 0;
			while (true)
			{
				line = reader.readLine();
				
				System.out.println("aaa");
				
				if (count >= 10)
				{
					break;
				}
				if (line == null)
				{
					line = "";
				}
				
				else if (line.equals("end of program"))
				{
					break;
				}
				
				output.append(line + "\n");
				count++;
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return output.toString();
	}
	
	// //////////
	/**
	 * returns the shannon entropy of the given string (courtesy: http://rosettacode.org/wiki/Entropy#Java)
	 * 
	 * @param s
	 * @return
	 */
	@SuppressWarnings("boxing")
	public static double getShannonEntropy(String s)
	{
		
		int n = 0;
		Map<Character, Integer> occ = new HashMap<>();
		
		for (int c_ = 0; c_ < s.length(); ++c_)
		{
			char cx = s.charAt(c_);
			if (occ.containsKey(cx))
			{
				occ.put(cx, occ.get(cx) + 1);
			}
			else
			{
				occ.put(cx, 1);
			}
			++n;
		}
		
		double e = 0.0;
		for (Map.Entry<Character, Integer> entry : occ.entrySet())
		{
			char cx = entry.getKey();
			double p = (double) entry.getValue() / n;
			e += p * log2(p);
		}
		
		System.out.println("\n\nEntropy for string: " + s + "\n\t is " + (-e));
		return -e;
	}
	
	public static double log2(double a)
	{
		return Math.log(a) / Math.log(2);
	}
	
	public static void showErrorExceptionPopup(String msg)
	{
		System.err.println(msg);
		new Exception(msg);
		PopUps.showError(msg);
	}
	
	/**
	 * To compare user IDs
	 * 
	 * @return
	 * @throws RuntimeException
	 */
	public static Comparator<String> getUserIDComparator() throws RuntimeException
	{
		return new Comparator<String>()
		{
			public int compare(String s1, String s2)
			{// both string must contains user...not doing User because of ignoring case, we can user StringUtils.containsIgnoreCase() alternatively but that might affect
				// performance
				String s1C = s1, s2C = s2;
				if (s1.contains("ser") == true && s2.contains("ser") == true)
				{
					s1C = s1C.replaceAll("[^0-9]", "");
					// PopUps.showMessage("xxxx---"+s1c+" "+s2C);
					s2C = s2C.replaceAll("[^0-9]", "");
					// PopUps.showMessage("xxxx---" + s1C + " " + s2C);
					
					return Integer.compare(Integer.valueOf(s1C), Integer.valueOf(s2C));
				}
				else
				{
					throw new RuntimeException("Error in getUserIDComparator.compare(): the strings to compare do not contain 'ser' ");
				}
			}
		};
	}
	
	public static boolean isMaximum(double tocheck, double a, double b, double c)
	{
		return tocheck == Math.max(Math.max(a, b), c);
	}
	
	/**
	 * 
	 * @param s
	 * @param delimiter
	 * @return
	 */
	public static String CSVRecordToString(CSVRecord s, String delimiter)
	{
		String res = "";
		for (int i = 0; i < s.size(); i++)
		{
			res += s.get(i) + delimiter;
		}
		return res.substring(0, res.length() - delimiter.length());// a,b,c,d, // len = 8 (0,7)
	}
	
	/**
	 * Find duplicates in the given list. Source: https://stackoverflow.com/questions/7414667/identify-duplicates-in-a-list
	 * 
	 * @param list
	 * @return
	 */
	@SuppressWarnings("unused")
	public static <T> Set<T> findDuplicates(Collection<T> list)
	{
		
		Set<T> duplicates = new LinkedHashSet<T>();
		Set<T> uniques = new HashSet<T>();
		
		for (T t : list)
		{
			if (!uniques.add(t))
			{
				duplicates.add(t);
			}
		}
		
		return duplicates;
	}
	/*
	 * /** Fetches the current timeline from the given longer timeline from the recommendation point back until the matching unit length.
	 * 
	 * @param longerTimeline the timelines (test timeline) from which the current timeline is to be extracted
	 * 
	 * @param dateAtRecomm
	 * 
	 * @param timeAtRecomm
	 * 
	 * @param userIDAtRecomm
	 * 
	 * @param matchingUnitInHours
	 * 
	 * @return
	 */
	/*
	 * public static TimelineWithNext getCurrentTimelineFromLongerTimeline(Timeline longerTimeline,Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm, int
	 * matchingUnitInHours) { System.out.println("Inside getCurrentTimelineFromLongerTimeline");
	 * 
	 * Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(),dateAtRecomm.getMonth(),dateAtRecomm.getDate(), timeAtRecomm.getHours(),timeAtRecomm.getMinutes(),
	 * timeAtRecomm.getSeconds(),0); long currentEndTime=currentEndTimestamp.getTime(); Timestamp currentStartTimestamp = new Timestamp(currentEndTime-
	 * (matchingUnitInHours*60*60*1000));
	 * 
	 * System.out.println("Starttime of current timeline="+currentStartTimestamp); System.out.println("Endtime of current timeline="+currentEndTimestamp);
	 * 
	 * 
	 * //identify the recommendation point in longer timeline
	 * 
	 * ArrayList<ActivityObject> activityEventsInCurrentTimeline=longerTimeline.getActivityEventsBetweenTime(currentStartTimestamp,currentEndTimestamp);
	 * 
	 * ActivityObject nextValidActivityEvent= longerTimeline.getNextValidActivityAfterActivityAtThisTime(currentEndTimestamp); ActivityObject nextActivityEvent =
	 * longerTimeline.getNextActivityAfterActivityAtThisTime(currentEndTimestamp);
	 * 
	 * int isInvalid=-99; if(nextActivityEvent.isInvalidActivityName()) { isInvalid=1; } else isInvalid=-1;
	 * 
	 * TimelineWithNext currentTimeline= new TimelineWithNext(activityEventsInCurrentTimeline,nextValidActivityEvent); currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);
	 * 
	 * return currentTimeline; }
	 */
	
}
