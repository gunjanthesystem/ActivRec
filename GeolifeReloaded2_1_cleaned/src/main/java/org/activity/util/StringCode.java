package org.activity.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.SaxConstants;
import org.activity.constants.VerbosityConstants;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.BalancedIntegerTree;
import org.activity.stats.HilbertCurveUtils;
import org.activity.ui.PopUps;
import org.apache.commons.lang3.ArrayUtils;

import it.unimi.dsi.fastutil.ints.Int2CharMap;
import it.unimi.dsi.fastutil.ints.Int2CharOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

//import com.google.uzaygezen.core.GunjanUtils;

public class StringCode
{

	/**
	 * For all unique primary dimension vals in the two given list of aos, generate unique char codes
	 * <p>
	 * can i generate string code specific for a particular edit distance computation, dynamically, i,e, it has to be
	 * unique not for all unique location ids in the dataset but not only the total number of unique location ids in the
	 * two timelines amongst whom the edit distance is being computed. Note: one activity object can also have multiple
	 * loc ids when it is actually a merged activity object.
	 * <p>
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @param givenDimensin
	 * @return map of {given dimension value, char code}
	 * @since 17 July 2018
	 */
	public static HashMap<Integer, Character> getLocallyUniqueCharCodeMap17July2018(
			ArrayList<ActivityObject2018> activityObjects1, ArrayList<ActivityObject2018> activityObjects2,
			PrimaryDimension givenDimension)
	{
		LinkedHashSet<Integer> uniqueGivenDimensionVals = new LinkedHashSet<>();

		for (ActivityObject2018 ao : activityObjects1)
		{
			uniqueGivenDimensionVals.addAll(ao.getGivenDimensionVal(givenDimension));
		}

		for (ActivityObject2018 ao : activityObjects2)
		{
			uniqueGivenDimensionVals.addAll(ao.getGivenDimensionVal(givenDimension));
		}

		int numOfUniqueVals = uniqueGivenDimensionVals.size();

		if (numOfUniqueVals > 400)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: numOfUniqueVals = " + numOfUniqueVals + " >400, beyond implementation range");
		}

		ArrayList<Integer> uniqueGivenDimensionValsList = new ArrayList<>(uniqueGivenDimensionVals);
		HashMap<Integer, Character> charCodeMap = new HashMap<>(numOfUniqueVals);

		// the val for the dimension can be more than 400 but num of unique vals for this dimension in this small world
		// should not be more than 400
		for (int indexOfUniqueVal = 0; indexOfUniqueVal < uniqueGivenDimensionValsList.size(); indexOfUniqueVal++)
		{
			char charCode = getCharCodeForInt(indexOfUniqueVal);
			Integer val = uniqueGivenDimensionValsList.get(indexOfUniqueVal);
			charCodeMap.put(val, charCode);
		}

		if (VerbosityConstants.verbose)
		{
			StringBuilder sb = new StringBuilder(
					"\nInside org.activity.util.StringCode.getLocallyUniqueCharCodeMap():\n");
			sb.append("\naos1 = \n");
			activityObjects1.stream().forEach(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("/")));// toStringAllGowallaTS()));
			sb.append("\naos2 = \n");
			activityObjects2.stream().forEach(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("/")));// (ao.toStringAllGowallaTS()));
			sb.append("\nuniquePrimaryDimensionVals = \n" + uniqueGivenDimensionVals.toString() + "\n");
			sb.append("Char code map:\n");
			charCodeMap.entrySet().stream()
					.forEachOrdered(e -> sb.append(">>" + e.getKey() + "--" + e.getValue() + "\n"));
			System.out.println(sb.toString());
		}

		return charCodeMap;
	}

	/**
	 * For all unique primary dimension vals in the two given list of aos, generate unique char codes
	 * <p>
	 * can i generate string code specific for a particular edit distance computation, dynamically, i,e, it has to be
	 * unique not for all unique location ids in the dataset but not only the total number of unique location ids in the
	 * two timelines amongst whom the edit distance is being computed. Note: one activity object can also have multiple
	 * loc ids when it is actually a merged activity object.
	 * <p>
	 * Note 12 July 2018: i dont think at the moment it is dynamically generating locally unique char code but instead
	 * it is just mapping pdVal to char
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return map of {primary dimension value, char code}
	 * @until 16 JUly 2018
	 * @deprecated
	 */
	public static HashMap<Integer, Character> getLocallyUniqueCharCodeMap(ArrayList<ActivityObject2018> activityObjects1,
			ArrayList<ActivityObject2018> activityObjects2, PrimaryDimension primaryDimension)
	{
		LinkedHashSet<Integer> uniquePrimaryDimensionVals = new LinkedHashSet<>();

		for (ActivityObject2018 ao : activityObjects1)
		{
			uniquePrimaryDimensionVals.addAll(ao.getPrimaryDimensionVal());
		}

		for (ActivityObject2018 ao : activityObjects2)
		{
			uniquePrimaryDimensionVals.addAll(ao.getPrimaryDimensionVal());
		}

		int numOfUniqueVals = uniquePrimaryDimensionVals.size();

		if (numOfUniqueVals > 400)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: numOfUniqueVals = " + numOfUniqueVals + " >400, beyond implementation range");
		}

		ArrayList<Integer> uniquePrimaryDimensionValsList = new ArrayList<>(uniquePrimaryDimensionVals);
		HashMap<Integer, Character> charCodeMap = new HashMap<>(numOfUniqueVals);

		for (int i = 0; i < uniquePrimaryDimensionValsList.size(); i++)
		{
			Integer val = uniquePrimaryDimensionValsList.get(i);
			char charCode = getCharCodeForInt(val);
			charCodeMap.put(val, charCode);
		}

		if (VerbosityConstants.verbose)
		{
			StringBuilder sb = new StringBuilder(
					"\nInside org.activity.util.StringCode.getLocallyUniqueCharCodeMap():\n");
			sb.append("\naos1 = \n");
			activityObjects1.stream().forEach(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("/")));// toStringAllGowallaTS()));
			sb.append("\naos2 = \n");
			activityObjects2.stream().forEach(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("/")));// (ao.toStringAllGowallaTS()));
			sb.append("\nuniquePrimaryDimensionVals = \n" + uniquePrimaryDimensionVals.toString() + "\n");
			sb.append("Char code map:\n");
			charCodeMap.entrySet().stream()
					.forEachOrdered(e -> sb.append(">>" + e.getKey() + "--" + e.getValue() + "\n"));
			System.out.println(sb.toString());
		}

		return charCodeMap;
	}

	/**
	 * Fork of getLocallyUniqueCharCodeMap using FastUtil collection For all unique primary dimension vals in the two
	 * given list of aos, generate unique char codes
	 * <p>
	 * OBSERVATION: THIS DID NOT RESULT IN CONSISTENT PERFORMANCE IMPROVEMENT
	 * <p>
	 * can i generate string code specific for a particular edit distance computation, dynamically, i,e, it has to be
	 * unique not for all unique location ids in the dataset but not only the total number of unique location ids in the
	 * two timelines amongst whom the edit distance is being computed. Note: one activity object can also have multiple
	 * loc ids when it is actually a merged activity object.
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return map of {primary dimension value, char code}
	 * @deprecated
	 */
	public static Int2CharOpenHashMap getLocallyUniqueCharCodeMapFU(ArrayList<ActivityObject2018> activityObjects1,
			ArrayList<ActivityObject2018> activityObjects2, PrimaryDimension primaryDimension)
	{
		IntOpenHashSet uniquePrimaryDimensionVals = new IntOpenHashSet();

		for (ActivityObject2018 ao : activityObjects1)
		{
			uniquePrimaryDimensionVals.addAll(ao.getPrimaryDimensionVal());
		}

		for (ActivityObject2018 ao : activityObjects2)
		{
			uniquePrimaryDimensionVals.addAll(ao.getPrimaryDimensionVal());
		}

		int numOfUniqueVals = uniquePrimaryDimensionVals.size();

		if (numOfUniqueVals > 400)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: numOfUniqueVals = " + numOfUniqueVals + " >400, beyond implementation range");
		}

		IntArrayList uniquePrimaryDimensionValsList = new IntArrayList(uniquePrimaryDimensionVals);
		Int2CharOpenHashMap charCodeMap = new Int2CharOpenHashMap(numOfUniqueVals);

		for (int i = 0; i < uniquePrimaryDimensionValsList.size(); i++)
		{
			int val = uniquePrimaryDimensionValsList.getInt(i);
			// int val = uniquePrimaryDimensionValsList.get(i);
			char charCode = getCharCodeForInt(val);
			charCodeMap.put(val, charCode);
		}

		if (VerbosityConstants.verbose)
		{
			StringBuilder sb = new StringBuilder(
					"\nInside org.activity.util.StringCode.getLocallyUniqueCharCodeMap():\n");
			sb.append("\naos1 = \n");
			activityObjects1.stream().forEach(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("/")));// toStringAllGowallaTS()));
			sb.append("\naos2 = \n");
			activityObjects2.stream().forEach(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("/")));// (ao.toStringAllGowallaTS()));
			sb.append("\nuniquePrimaryDimensionVals = \n" + uniquePrimaryDimensionVals.toString() + "\n");
			sb.append("Char code map:\n");
			charCodeMap.entrySet().stream()
					.forEachOrdered(e -> sb.append(">>" + e.getKey() + "--" + e.getValue() + "\n"));
			System.out.println(sb.toString());
		}

		return charCodeMap;
	}

	/**
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @param featureName
	 * @return
	 */
	public static String[] getRelativeStringCodesForFeature(ArrayList<ActivityObject2018> activityObjects1,
			ArrayList<ActivityObject2018> activityObjects2, String featureName)
	{
		switch (featureName)
		{
			case "StartTime":
				return getStringCodesForStartTime(activityObjects1, activityObjects2);

			case "Duration":
				return getStringCodesForDuration(activityObjects1, activityObjects2);

			case "DistanceTravelled":
				return getStringCodesForDistanceTravelled(activityObjects1, activityObjects2);

			case "StartGeoCoordinates":
				return getStringCodesForStartGeoCoordinates(activityObjects1, activityObjects2);

			case "EndGeoCoordinates":
				return getStringCodesForEndGeoCoordinates(activityObjects1, activityObjects2);

			case "AvgAltitude":
				return getStringCodesForAvgAltitudes(activityObjects1, activityObjects2);

			default:
				System.err.println(
						"Error in org.activity.util.StringCode.getStringCodeForFeature(ArrayList<ActivityObject>, String): unsuitable feature name"
								+ featureName);
		}
		System.err.println(
				"Error in org.activity.util.StringCode.getStringCodeForFeature(ArrayList<ActivityObject>, String): reached unreachable code.");
		return null;
	}

	/**
	 * Returns the activity-name corresponding to given String code of 1-character length
	 * 
	 * @deprecated used for dcu and geolife where num of uniques activities was <=10
	 * 
	 * @param code
	 * @return
	 */
	public static String getActivityNameFromStringCode(String code)
	{
		String name = null;

		String msg = "Alert!: you are using org.activity.util.StringCode.getActivityNameFromStringCode(String)! ";
		PopUps.showMessage(PopUps.getTracedErrorMsg(msg));
		System.err.println(msg);

		char[] charCode = code.toCharArray();
		if (charCode.length > 1)
		{
			System.err.println("Error in getActivityNameFromStringCode: the code string is more than one characher");
			// System.exit(5);
		}

		int activityCode = (int) charCode[0] - 65;
		try
		{
			// name = ConnectDatabase.getActivityNameFromDatabase(activityCode);
			name = ConnectDatabase.getActivityName(activityCode);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 107 activities.
	 * 
	 * @deprecated used for dcu and geolife where num of uniques activities was <=10
	 * @param activityName
	 * @return
	 */
	public static String getStringCodeFromActivityName(String activityName)
	{
		// if(activityName.equals(Constant.INVALID_ACTIVITY1))
		// {
		// return
		// }

		String msg = "Error!: you are using org.activity.util.StringCode.getStringCodeFromActivityName(String)! ";
		PopUps.showMessage(PopUps.getTracedErrorMsg(msg));
		System.err.println(PopUps.getTracedErrorMsg(msg));
		// System.err.println(msg);
		// PopUps.getCurrentStackTracedErrorMsg(msg);

		if (activityName.length() == 1)
		{
			return activityName;
		}
		else
		{
			String code = new String();
			try
			{
				// int activityID = ConnectDatabase.getActivityIDFromDatabase(activityName);
				int activityID = ConnectDatabase.getActivityID(activityName);
				code = Character.toString((char) (activityID + 65));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return code;
		}
	}

	/**
	 * Returns the 1-character code to be used for the Activity Name.
	 * <p>
	 * This code is derived from the ActivityID and hence is guaranteed to be unique for at least 400 activities. Will
	 * use unicode char sfrom 192-591
	 * <p>
	 * 65-90: A-Z Latin Alphabet: Uppercase
	 * <p>
	 * 91-96: ASCII Punctuation & Symbols
	 * <p>
	 * 97-122: a-z Latin Alphabet: Lowercase
	 * <p>
	 * 123-126: ASCII Punctuation & Symbols
	 * 
	 * <p>
	 * 192-255: Letters and math symbols of Latin 1 supplement
	 * <p>
	 * 256-383:Latin Extended-A - 128 chars
	 * <p>
	 * 384-591:Latin Extended-B - 208 chars
	 * <p>
	 * <font color = blue>So, 62 chars from 65-126 and 400 chars from 192 - 591</font>
	 * 
	 * @param activityName
	 * @return
	 */
	public static char getCharCodeFromActivityID(int activityID)
	{
		// uncode char from 127 to 159 are non printable, hence do not use them
		char code = '\u0000';// null character new String();
		try
		{
			code = (char) (activityID + 192); // 65 is A
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return code;
	}

	/**
	 * Returns the 1-character code to be used for the Activity Name.
	 * <p>
	 * This code is derived from the ActivityID and hence is guaranteed to be unique for at least 400 activities. Will
	 * use unicode char sfrom 192-591
	 * <p>
	 * 65-90: A-Z Latin Alphabet: Uppercase
	 * <p>
	 * 91-96: ASCII Punctuation & Symbols
	 * <p>
	 * 97-122: a-z Latin Alphabet: Lowercase
	 * <p>
	 * 123-126: ASCII Punctuation & Symbols
	 * 
	 * <p>
	 * 192-255: Letters and math symbols of Latin 1 supplement
	 * <p>
	 * 256-383:Latin Extended-A - 128 chars
	 * <p>
	 * 384-591:Latin Extended-B - 208 chars
	 * <p>
	 * <font color = blue>So, 62 chars from 65-126 and 400 chars from 192 - 591</font>
	 * 
	 * @param activityName
	 * @return
	 */
	public static char getCharCodeForInt(int integerVal)
	{
		// uncode char from 127 to 159 are non printable, hence do not use them
		char code = '\u0000';// null character new String();
		try
		{
			code = (char) (integerVal + 192);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return code;
	}

	/**
	 * 
	 * @param invalidActivityName
	 * @return<font color= red> 'A' for invalid act 1, 'B' for invalid act 2</font>
	 */
	public static char getCharCodeFromInvalidActivityName(String invalidActivityName)
	{
		// uncode char from 127 to 159 are non printable, hence do not use them
		char code = '\u0000';// null character new String();
		try
		{
			if (invalidActivityName.equals(Constant.INVALID_ACTIVITY1))
			{
				return (char) 65;// A
			}

			else if (invalidActivityName.equals(Constant.INVALID_ACTIVITY1))
			{
				return (char) 66;// B
			}
			else
			{
				System.out.println(PopUps.getTracedErrorMsg("Unknown invalid act name: = " + invalidActivityName));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return code;

	}

	/**
	 * 
	 * @param actObjs
	 * @return
	 */
	public static String getStringCodeForStartTime(ArrayList<ActivityObject2018> actObjs)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodeForStartTime");

		String resultant = new String();

		double vals[] = new double[actObjs.size()];
		long stamps[] = new long[actObjs.size()];

		for (int i = 0; i < actObjs.size(); i++)
		{
			vals[i] = DateTimeUtils.getTimeInDayInSeconds(actObjs.get(i).getStartTimestamp()); // should i convert it to
																								// minutes
			stamps[i] = actObjs.get(i).getStartTimestamp().getTime();

			if (VerbosityConstants.verboseSAX) System.out.print(vals[i] + " ");
		}

		try
		{
			resultant = SAXUtils.getSAXString(vals, stamps, actObjs.size(), SaxConstants.SAXStartTimeAlphabsetSize);// Constant.SAXStartTimeAlphabetSize);//
			// SAXFactory.ts2string(ts,
			// actObjs.size(),
			// new
			// NormalAlphabet(), 10);
			// System.out.println("String representation = "+ resultant);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (resultant.length() == 0)
		{
			System.err.println("Error in getStringCodeForStartTime(): no resultant SAX ");
		}

		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + resultant);
		}
		return resultant;
	}

	/**
	 * 
	 * @param actObjs
	 * @return
	 */
	public static String[] getStringCodesForStartTime(ArrayList<ActivityObject2018> actObjs1,
			ArrayList<ActivityObject2018> actObjs2)
	{
		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("Inside getStringCodeForStartTime");
		}

		String finalResultant[] = new String[2];

		double vals1[] = new double[actObjs1.size()];
		long stamps1[] = new long[actObjs1.size()];
		for (int i = 0; i < actObjs1.size(); i++)
		{
			vals1[i] = DateTimeUtils.getTimeInDayInSeconds(actObjs1.get(i).getStartTimestamp()); // should i convert it
																									// to minutes
			stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX) System.out.print(vals1[i] + " ");
		}
		if (VerbosityConstants.verboseSAX)
		{
			System.out.println();
		}

		double vals2[] = new double[actObjs2.size()];
		long stamps2[] = new long[actObjs2.size()];
		for (int i = 0; i < actObjs2.size(); i++)
		{
			vals2[i] = DateTimeUtils.getTimeInDayInSeconds(actObjs2.get(i).getStartTimestamp()); // should i convert it
																									// to minutes
			stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX)
			{
				System.out.print(vals2[i] + " ");
			}
		}

		double valsAll[] = ArrayUtils.addAll(vals1, vals2);
		long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);
		String resultant;
		try
		{
			resultant = SAXUtils.getSAXString(valsAll, stampsAll, stampsAll.length,
					SaxConstants.SAXStartTimeAlphabsetSize);
			finalResultant[0] = resultant.substring(0, stamps1.length);
			finalResultant[1] = resultant.substring(stamps1.length, stampsAll.length);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		if ((finalResultant[0].length() == 0) || (finalResultant[0].length() == 0))
		{
			System.err.println("Error in getStringCodeForStartTime(): no resultant SAX ");
		}

		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}

	/**
	 * 
	 * @param actObjs
	 * @return
	 */
	public static String getStringCodeForDuration(ArrayList<ActivityObject2018> actObjs)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodeForDuration");

		String resultant = new String();

		double vals[] = new double[actObjs.size()];
		long stamps[] = new long[actObjs.size()];

		for (int i = 0; i < actObjs.size(); i++)
		{
			vals[i] = actObjs.get(i).getDurationInSeconds(); // should i convert it to minutes
			stamps[i] = actObjs.get(i).getStartTimestamp().getTime();

			if (VerbosityConstants.verboseSAX) System.out.print(vals[i] + " ");
		}

		try
		{
			resultant = SAXUtils.getSAXString(vals, stamps, actObjs.size(), SaxConstants.SAXDurationAlphabsetSize);
			// SAXFactory.ts2string(ts, actObjs.size(), new NormalAlphabet(), 10);
			// System.out.println("String representation = "+ resultant);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (resultant.length() == 0)
		{
			System.err.println("Error in getStringCodeForDuration(): no resultant SAX ");
		}
		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + resultant);
		}

		return resultant;
	}

	/**
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return
	 */
	public static String[] getStringCodesForDuration(ArrayList<ActivityObject2018> actObjs1,
			ArrayList<ActivityObject2018> actObjs2)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodeForDuration");

		String finalResultant[] = new String[2];

		double vals1[] = new double[actObjs1.size()];
		long stamps1[] = new long[actObjs1.size()];

		for (int i = 0; i < actObjs1.size(); i++)
		{
			vals1[i] = actObjs1.get(i).getDurationInSeconds(); // should i convert it to minutes
			stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();

			if (VerbosityConstants.verboseSAX) System.out.print(vals1[i] + " ");
		}
		if (VerbosityConstants.verboseSAX) System.out.println();

		double vals2[] = new double[actObjs2.size()];
		long stamps2[] = new long[actObjs2.size()];
		for (int i = 0; i < actObjs2.size(); i++)
		{
			vals2[i] = actObjs2.get(i).getDurationInSeconds(); // should i convert it to minutes
			stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX) System.out.print(vals2[i] + " ");
		}

		double valsAll[] = ArrayUtils.addAll(vals1, vals2);
		long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);

		String resultant;
		try
		{
			resultant = SAXUtils.getSAXString(valsAll, stampsAll, stampsAll.length,
					SaxConstants.SAXDurationAlphabsetSize);
			finalResultant[0] = resultant.substring(0, stamps1.length);
			finalResultant[1] = resultant.substring(stamps1.length, stampsAll.length);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		if ((finalResultant[0].length() == 0) || (finalResultant[0].length() == 0))
		{
			System.err.println("Error in getStringCodeForDuration(): no resultant SAX ");
		}

		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}

	public static String getStringCodeForDistanceTravelled(ArrayList<ActivityObject2018> actObjs)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodeForDistanceTravelled");
		String resultant = new String();

		double vals[] = new double[actObjs.size()];
		long stamps[] = new long[actObjs.size()];

		for (int i = 0; i < actObjs.size(); i++)
		{
			vals[i] = actObjs.get(i).getDistanceTravelled();
			stamps[i] = actObjs.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX) System.out.print(vals[i] + " ");
		}

		try
		{
			resultant = SAXUtils.getSAXString(vals, stamps, actObjs.size(),
					SaxConstants.SAXDistanceTravelledAlphabsetSize);// SAXFactory.ts2string(ts,
			// actObjs.size(),
			// new
			// NormalAlphabet(), 10);
			// System.out.println("String representation = "+ resultant);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (resultant.length() == 0)
		{
			System.err.println("Error in getStringCodeForDistanceTravelled(): no resultant SAX ");
		}
		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + resultant);
		}

		return resultant;
	}

	/**
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return
	 */
	public static String[] getStringCodesForDistanceTravelled(ArrayList<ActivityObject2018> actObjs1,
			ArrayList<ActivityObject2018> actObjs2)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodeForDistanceTravelled");
		String finalResultant[] = new String[2];

		double vals1[] = new double[actObjs1.size()];
		long stamps1[] = new long[actObjs1.size()];

		for (int i = 0; i < actObjs1.size(); i++)
		{
			vals1[i] = actObjs1.get(i).getDistanceTravelled();
			stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX) System.out.print(vals1[i] + " ");
		}

		if (VerbosityConstants.verboseSAX) System.out.println();

		double vals2[] = new double[actObjs2.size()];
		long stamps2[] = new long[actObjs2.size()];
		for (int i = 0; i < actObjs2.size(); i++)
		{
			vals2[i] = actObjs2.get(i).getDistanceTravelled();
			stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX) System.out.print(vals2[i] + " ");
		}

		double valsAll[] = ArrayUtils.addAll(vals1, vals2);
		long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);

		String resultant;
		try
		{
			resultant = SAXUtils.getSAXString(valsAll, stampsAll, stampsAll.length,
					SaxConstants.SAXDistanceTravelledAlphabsetSize);
			finalResultant[0] = resultant.substring(0, stamps1.length);
			finalResultant[1] = resultant.substring(stamps1.length, stampsAll.length);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		if ((finalResultant[0].length() == 0) || (finalResultant[0].length() == 0))
		{
			System.err.println("Error in getStringCodesForDistanceTravelled(): no resultant SAX ");
		}

		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}

	public static String getStringCodeForAvgAltitudes(ArrayList<ActivityObject2018> actObjs)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodeForAvgAltitudes");
		String resultant = new String();

		double vals[] = new double[actObjs.size()];
		long stamps[] = new long[actObjs.size()];

		for (int i = 0; i < actObjs.size(); i++)
		{
			vals[i] = Double.parseDouble(actObjs.get(i).getAvgAltitude());
			stamps[i] = actObjs.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX) System.out.print(vals[i] + " ");
		}

		try
		{
			resultant = SAXUtils.getSAXString(vals, stamps, actObjs.size(), SaxConstants.SAXAvgAltitudeAlphabsetSize);// SAXFactory.ts2string(ts,
																														// actObjs.size(),
																														// new
			// NormalAlphabet(), 10);
			// System.out.println("String representation = "+ resultant);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (resultant.length() == 0)
		{
			System.err.println("Error in getStringCodeForAvgAltitude(): no resultant SAX ");
		}
		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + resultant);
		}

		return resultant;
	}

	/**
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return
	 */
	public static String[] getStringCodesForAvgAltitudes(ArrayList<ActivityObject2018> actObjs1,
			ArrayList<ActivityObject2018> actObjs2)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodeForAvgAltitudes");

		String finalResultant[] = new String[2];

		double vals1[] = new double[actObjs1.size()];
		long stamps1[] = new long[actObjs1.size()];

		for (int i = 0; i < actObjs1.size(); i++)
		{
			vals1[i] = Double.parseDouble(actObjs1.get(i).getAvgAltitude());
			stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX) System.out.print(vals1[i] + " ");
		}

		if (VerbosityConstants.verboseSAX) System.out.println();

		double vals2[] = new double[actObjs2.size()];
		long stamps2[] = new long[actObjs2.size()];
		for (int i = 0; i < actObjs2.size(); i++)
		{
			vals2[i] = Double.parseDouble(actObjs2.get(i).getAvgAltitude());
			stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
			if (VerbosityConstants.verboseSAX) System.out.print(vals2[i] + " ");
		}

		double valsAll[] = ArrayUtils.addAll(vals1, vals2);
		long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);

		String resultant;
		try
		{
			resultant = SAXUtils.getSAXString(valsAll, stampsAll, stampsAll.length,
					SaxConstants.SAXAvgAltitudeAlphabsetSize);

			finalResultant[0] = resultant.substring(0, stamps1.length);
			finalResultant[1] = resultant.substring(stamps1.length, stampsAll.length);
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		if ((finalResultant[0].length() == 0) || (finalResultant[0].length() == 0))
		{
			System.err.println("Error in getStringCodesForAvgAltitudes(): no resultant SAX ");
		}

		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}

	/**
	 * Sequence of (lat,lon) pairs extracted from the two given activity-objects converted to hilbert space filled curve
	 * index , which is then converted to symbol sequence using SAX. </br>
	 * note: the hilbert space filled curve (hsfc) implementation takes only long data types as input.
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return
	 */
	public static String[] getStringCodesForStartGeoCoordinates(ArrayList<ActivityObject2018> actObjs1,
			ArrayList<ActivityObject2018> actObjs2)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodesForGeoCoordinates");

		String finalResultant[] = new String[2];

		try
		{
			double vals1[] = new double[actObjs1.size()];
			long stamps1[] = new long[actObjs1.size()];

			for (int i = 0; i < actObjs1.size(); i++)
			{
				double latitude1 = Double.parseDouble(actObjs1.get(i).getStartLatitude());
				double longitude1 = Double.parseDouble(actObjs1.get(i).getStartLongitude());

				long latitude1AsLong = (long) (latitude1 * Constant.decimalPlacesInGeocordinatesForComputations);
				long longitude1AsLong = (long) (longitude1 * Constant.decimalPlacesInGeocordinatesForComputations);

				vals1[i] = HilbertCurveUtils.getCompactHilbertCurveIndex(latitude1AsLong, longitude1AsLong);
				stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
				if (VerbosityConstants.verboseHilbert)
				{
					System.out.println("lat:" + latitude1 + " lon:" + longitude1 + "->latL:" + latitude1AsLong
							+ ", lonL:" + longitude1AsLong + "  HSFC ind:" + vals1[i]);
				}

				if (VerbosityConstants.verboseSAX && !VerbosityConstants.verboseHilbert)
					System.out.print(vals1[i] + " ");
			}

			if (VerbosityConstants.verboseSAX) System.out.println();

			double vals2[] = new double[actObjs2.size()];
			long stamps2[] = new long[actObjs2.size()];

			for (int i = 0; i < actObjs2.size(); i++)
			{
				double latitude2 = Double.parseDouble(actObjs2.get(i).getStartLatitude());
				double longitude2 = Double.parseDouble(actObjs2.get(i).getStartLongitude());

				long latitude2AsLong = (long) (latitude2 * Constant.decimalPlacesInGeocordinatesForComputations);
				long longitude2AsLong = (long) (longitude2 * Constant.decimalPlacesInGeocordinatesForComputations);

				vals2[i] = HilbertCurveUtils.getCompactHilbertCurveIndex(latitude2AsLong, longitude2AsLong);
				stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
				if (VerbosityConstants.verboseHilbert)
				{
					System.out.println("lat:" + latitude2 + " lon:" + longitude2 + "->latL:" + latitude2AsLong
							+ ", lonL:" + longitude2AsLong + "  HSFC ind:" + vals2[i]);
				}

				if (VerbosityConstants.verboseSAX && !VerbosityConstants.verboseHilbert)
					System.out.print(vals2[i] + " ");
			}

			double valsAll[] = ArrayUtils.addAll(vals1, vals2);
			long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);

			String resultant;
			resultant = SAXUtils.getSAXString(valsAll, stampsAll, stampsAll.length,
					SaxConstants.SAXAvgAltitudeAlphabsetSize);// Constant.SAXStartTimeAlphabetSize);//

			finalResultant[0] = resultant.substring(0, stamps1.length);
			finalResultant[1] = resultant.substring(stamps1.length, stampsAll.length);
			// SAXFactory.ts2string(ts,
			// actObjs.size(), new
			// NormalAlphabet(), 10);
			// System.out.println("String representation = "+ resultant);
		}

		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showError(e.getMessage());
		}

		if ((finalResultant[0].length() == 0) || (finalResultant[0].length() == 0))
		{
			System.err.println("Error in getStringCodesForGeoCoordinates(): no resultant SAX ");
		}

		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}

	/**
	 * Sequence of (lat,lon) pairs extracted from the two given activity-objects converted to hilbert space filled curve
	 * index , which is then converted to symbol sequence using SAX. </br>
	 * note: the hilbert space filled curve (hsfc) implementation takes only long data types as input.
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return
	 */
	public static String[] getStringCodesForEndGeoCoordinates(ArrayList<ActivityObject2018> actObjs1,
			ArrayList<ActivityObject2018> actObjs2)
	{
		if (VerbosityConstants.verboseSAX) System.out.println("Inside getStringCodesForGeoCoordinates");

		String finalResultant[] = new String[2];

		try
		{
			double vals1[] = new double[actObjs1.size()];
			long stamps1[] = new long[actObjs1.size()];

			for (int i = 0; i < actObjs1.size(); i++)
			{
				double latitude1 = Double.parseDouble(actObjs1.get(i).getEndLatitude());
				double longitude1 = Double.parseDouble(actObjs1.get(i).getEndLongitude());

				long latitude1AsLong = (long) (latitude1 * Constant.decimalPlacesInGeocordinatesForComputations);
				long longitude1AsLong = (long) (longitude1 * Constant.decimalPlacesInGeocordinatesForComputations);

				vals1[i] = HilbertCurveUtils.getCompactHilbertCurveIndex(latitude1AsLong, longitude1AsLong);
				stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
				if (VerbosityConstants.verboseHilbert)
				{
					System.out.println("lat:" + latitude1 + " lon:" + longitude1 + "->latL:" + latitude1AsLong
							+ ", lonL:" + longitude1AsLong + "  HSFC ind:" + vals1[i]);
				}

				if (VerbosityConstants.verboseSAX && !VerbosityConstants.verboseHilbert)
					System.out.print(vals1[i] + " ");
			}

			if (VerbosityConstants.verboseSAX) System.out.println();

			double vals2[] = new double[actObjs2.size()];
			long stamps2[] = new long[actObjs2.size()];

			for (int i = 0; i < actObjs2.size(); i++)
			{
				double latitude2 = Double.parseDouble(actObjs2.get(i).getEndLatitude());
				double longitude2 = Double.parseDouble(actObjs2.get(i).getEndLongitude());

				long latitude2AsLong = (long) (latitude2 * Constant.decimalPlacesInGeocordinatesForComputations);
				long longitude2AsLong = (long) (longitude2 * Constant.decimalPlacesInGeocordinatesForComputations);

				vals2[i] = HilbertCurveUtils.getCompactHilbertCurveIndex(latitude2AsLong, longitude2AsLong);
				stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
				if (VerbosityConstants.verboseHilbert)
				{
					System.out.println("lat:" + latitude2 + " lon:" + longitude2 + "->latL:" + latitude2AsLong
							+ ", lonL:" + longitude2AsLong + "  HSFC ind:" + vals2[i]);
				}

				if (VerbosityConstants.verboseSAX && !VerbosityConstants.verboseHilbert)
					System.out.print(vals2[i] + " ");
			}

			double valsAll[] = ArrayUtils.addAll(vals1, vals2);
			long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);

			String resultant;
			resultant = SAXUtils.getSAXString(valsAll, stampsAll, stampsAll.length,
					SaxConstants.SAXAvgAltitudeAlphabsetSize);// Constant.SAXStartTimeAlphabetSize);//

			finalResultant[0] = resultant.substring(0, stamps1.length);
			finalResultant[1] = resultant.substring(stamps1.length, stampsAll.length);
			// SAXFactory.ts2string(ts,
			// actObjs.size(), new
			// NormalAlphabet(), 10);
			// System.out.println("String representation = "+ resultant);
		}

		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showError(e.getMessage());
		}

		if ((finalResultant[0].length() == 0) || (finalResultant[0].length() == 0))
		{
			System.err.println("Error in getStringCodesForGeoCoordinates(): no resultant SAX ");
		}

		if (VerbosityConstants.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}

	/**
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 400 activities.
	 * 
	 * @param activityObjects
	 * @return
	 */
	public static String getStringCodeForActivityObjectsFromActID(ArrayList<ActivityObject2018> activityObjects)
	{
		StringBuilder code = new StringBuilder();
		// long t1, t2, t3, t4;
		// t1 = t2 = t3 = t4 = Long.MIN_VALUE;
		for (ActivityObject2018 ao : activityObjects)
		{
			// t1 = System.nanoTime();
			code.append(ao.getCharCodeFromActID());
			// t2 = System.nanoTime();
		}

		// if (false)// sanity check
		// {
		// StringBuilder codeDUmmy = new StringBuilder();
		//
		// for (ActivityObject ao : activityObjects)
		// {
		// t3 = System.nanoTime();
		// codeDUmmy.append(ao.getCharCode());
		// t4 = System.nanoTime();
		// }
		// WritingToFile.appendLineToFileAbsolute(
		// code.toString().equals(codeDUmmy.toString()) + "," + code.toString() + "," + codeDUmmy.toString()
		// + "," + (t2 - t1) + "," + (t4 - t3) + "\n",
		// Constant.getOutputCoreResultsPath() + "Mar9SanityCheck.csv");
		// }

		// activityObjects.stream().forEach(ao -> code.append(ao.getStringCode()));
		String codeS = code.toString();

		if (VerbosityConstants.verbose || VerbosityConstants.verboseSAX)
		{
			System.out.print("\tInside getStringCodeForActivityObjects:\n Act Names:");
			activityObjects.stream().forEach(ao -> System.out.print(ao.getActivityName() + " "));
			System.out.println("\tCode: " + codeS);
		}
		return codeS;
	}

	/**
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 400 activities.
	 * 
	 * @param activityObjects
	 * @return
	 */
	public static char[] getCharCodeArrayForAOs(ArrayList<ActivityObject2018> activityObjects)
	{
		char[] code = new char[activityObjects.size()];

		int i = 0;
		for (ActivityObject2018 ao : activityObjects)
		{
			code[i] = (ao.getCharCodeFromActID());
			i++;
		}

		// activityObjects.stream().forEach(ao -> code.append(ao.getStringCode()));
		String codeS = code.toString();

		if (VerbosityConstants.verbose || VerbosityConstants.verboseSAX)
		{
			System.out.print("\tInside getCharCodeArrayForActivityObjects:\n Act Names:");
			activityObjects.stream().forEach(ao -> System.out.print(ao.getActivityName() + " "));
			System.out.println("\tCode: " + codeS);
		}
		return code;
	}

	/**
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 400 activities.
	 * 
	 * 
	 * @param activityObjects
	 * @param givenDimension
	 * @param uniqueCharCodes
	 * @param verbose
	 * @since 17 July 2018
	 * @return
	 */
	public static ArrayList<String> getStringCodesForActivityObjects17July2018(
			ArrayList<ActivityObject2018> activityObjects, PrimaryDimension givenDimension,
			HashMap<Integer, Character> uniqueCharCodes, boolean verbose)
	{

		ArrayList<String> stringCodeforAOs = new ArrayList<>();
		// for given seq of aos, extract all possible sequence of val. if all aos had single value for primary dimension
		// (such as in no merger), then we will get only one ArrayList, otherwis multiple arraylist, one for each
		// possible sequence.
		// such as if primary vals for AOs: 1,2/3,5,6 (here the second element is merger of 2 and 3) then we will get
		// seq: 1,2,5,6 and 1,3,5,6 .
		ArrayList<ArrayList<Integer>> possibleSequencesOfGivenDimensionVals = new ArrayList<>();
		// multiValSeqTo1ValSeqs(activityObjects,primaryDimension, verbose);

		if (givenDimension.equals(PrimaryDimension.ActivityID)) // each ao has single act id
		{
			ArrayList<Integer> listOfActIDs = new ArrayList<>();
			for (ActivityObject2018 ao : activityObjects)
			{
				listOfActIDs.add(ao.getActivityID());
			}
			possibleSequencesOfGivenDimensionVals.add(listOfActIDs);

			// sanity check start
			if (VerbosityConstants.checkSanityPDImplementn)
			{
				ArrayList<ArrayList<Integer>> dummyPossibleSequencesOfGivenDimensionVals = multiValSeqTo1ValSeqs(
						activityObjects, givenDimension, verbose);

				System.out.println("Debug: dummyPossibleSequencesOfGivenDimensionVals= "
						+ dummyPossibleSequencesOfGivenDimensionVals.toString());

				System.out.println("Debug: possibleSequencesOfGivenDimensionVals= "
						+ possibleSequencesOfGivenDimensionVals.toString());

				if (!dummyPossibleSequencesOfGivenDimensionVals.equals(possibleSequencesOfGivenDimensionVals))
				{
					PopUps.printTracedErrorMsg(
							"dummyPossibleSequencesOfGivenDimensionVals.equals(possibleSequencesOfGivenDimensionVals) is false");
				}
			} // sanity check end
		}
		else
		{
			possibleSequencesOfGivenDimensionVals = multiValSeqTo1ValSeqs(activityObjects, givenDimension, verbose);
		}

		for (ArrayList<Integer> seqOfPDVals : possibleSequencesOfGivenDimensionVals)
		{
			StringBuilder sb = new StringBuilder();
			for (Integer v : seqOfPDVals)
			{
				Character code = uniqueCharCodes.get(v);
				if (code == null)
				{
					PopUps.printTracedErrorMsgWithExit("Error: char code for given dimension " + givenDimension
							+ " val = " + v + " not found in uniqueCharCodes =" + uniqueCharCodes.toString());
				}
				sb.append(code);
			}
			stringCodeforAOs.add(sb.toString());
		}

		if (VerbosityConstants.verbose)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("\tInside getStringCodeForActivityObjects:\n Act Names:");
			activityObjects.stream().forEachOrdered(ao -> sb.append(">>" + ao.getActivityName()));
			sb.append("\nGiven dimension vals:");
			activityObjects.stream()
					.forEachOrdered(ao -> sb.append(">>" + ao.getGivenDimensionVal("/", givenDimension)));// .getPrimaryDimensionVal("/")));
			sb.append("\npossibleSequencesOfPrimaryDimensionVals:");
			possibleSequencesOfGivenDimensionVals.stream().forEachOrdered(p -> sb.append(p.toString() + "  "));
			sb.append("\ncorresponding string codes:");
			stringCodeforAOs.stream().forEachOrdered(s -> sb.append(s.toString() + "  "));
			System.out.println(sb.toString());
		}
		return stringCodeforAOs;
	}

	/**
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 400 activities.
	 * 
	 * @param activityObjects
	 * @return
	 * @since 13 July 2017
	 * @until 16 July 2018
	 * @deprecated
	 */
	public static ArrayList<String> getStringCodesForActivityObjects(ArrayList<ActivityObject2018> activityObjects,
			PrimaryDimension primaryDimension, HashMap<Integer, Character> uniqueCharCodes, boolean verbose)
	{

		ArrayList<String> stringCodeforAOs = new ArrayList<>();
		// for given seq of aos, extract all possible sequence of val. if all aos had single value for primary dimension
		// (such as in no merger), then we will get only one ArrayList, otherwis multiple arraylist, one for each
		// possible sequence.
		// such as if primary vals for AOs: 1,2/3,5,6 (here the second element is merger of 2 and 3) then we will get
		// seq: 1,2,5,6 and 1,3,5,6 .
		ArrayList<ArrayList<Integer>> possibleSequencesOfPrimaryDimensionVals = new ArrayList<>();
		// multiValSeqTo1ValSeqs(activityObjects,primaryDimension, verbose);

		if (primaryDimension.equals(PrimaryDimension.ActivityID)) // each ao has single act id
		{
			ArrayList<Integer> listOfActIDs = new ArrayList<>();
			for (ActivityObject2018 ao : activityObjects)
			{
				listOfActIDs.add(ao.getActivityID());
			}
			possibleSequencesOfPrimaryDimensionVals.add(listOfActIDs);

			// sanity check start
			if (VerbosityConstants.checkSanityPDImplementn)
			{
				ArrayList<ArrayList<Integer>> dummyPossibleSequencesOfPrimaryDimensionVals = multiValSeqTo1ValSeqs(
						activityObjects, primaryDimension, verbose);

				System.out.println("Debug: dummyPossibleSequencesOfPrimaryDimensionVals= "
						+ dummyPossibleSequencesOfPrimaryDimensionVals.toString());

				System.out.println("Debug: possibleSequencesOfPrimaryDimensionVals= "
						+ possibleSequencesOfPrimaryDimensionVals.toString());

				if (!dummyPossibleSequencesOfPrimaryDimensionVals.equals(possibleSequencesOfPrimaryDimensionVals))
				{
					PopUps.printTracedErrorMsg(
							"dummyPossibleSequencesOfPrimaryDimensionVals.equals(possibleSequencesOfPrimaryDimensionVals");
				}
			} // sanity check end
		}
		else
		{
			possibleSequencesOfPrimaryDimensionVals = multiValSeqTo1ValSeqs(activityObjects, primaryDimension, verbose);
		}

		for (ArrayList<Integer> seqOfPDVals : possibleSequencesOfPrimaryDimensionVals)
		{
			StringBuilder sb = new StringBuilder();
			for (Integer v : seqOfPDVals)
			{
				Character code = uniqueCharCodes.get(v);
				if (code == null)
				{
					PopUps.printTracedErrorMsgWithExit("Error: char code for primary dimension " + primaryDimension
							+ " val = " + v + " not found in uniqueCharCodes =" + uniqueCharCodes.toString());
				}
				sb.append(code);
			}
			stringCodeforAOs.add(sb.toString());
		}

		if (VerbosityConstants.verbose)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("\tInside getStringCodeForActivityObjects:\n Act Names:");
			activityObjects.stream().forEachOrdered(ao -> sb.append(">>" + ao.getActivityName()));
			sb.append("\nPrimary dimension vals:");
			activityObjects.stream().forEachOrdered(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("/")));
			sb.append("\npossibleSequencesOfPrimaryDimensionVals:");
			possibleSequencesOfPrimaryDimensionVals.stream().forEachOrdered(p -> sb.append(p.toString() + "  "));
			sb.append("\ncorresponding string codes:");
			stringCodeforAOs.stream().forEachOrdered(s -> sb.append(s.toString() + "  "));
			System.out.println(sb.toString());
		}
		return stringCodeforAOs;
	}

	/**
	 * Fork of getStringCodesForActivityObjects using FastUtil Collection
	 * <p>
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 400 activities.
	 * 
	 * @param activityObjects
	 * @return
	 * @since 9 March 2018
	 * @deprecated
	 */
	public static ArrayList<String> getStringCodesForActivityObjectsFU(ArrayList<ActivityObject2018> activityObjects,
			PrimaryDimension primaryDimension, Int2CharMap uniqueCharCodes, boolean verbose)
	{

		ArrayList<String> stringCodeforAOs = new ArrayList<>();
		// for given seq of aos, extract all possible sequence of val. if all aos had single value for primary dimension
		// (such as in no merger), then we will get only one ArrayList, otherwis multiple arraylist, one for each
		// possible sequence.
		// such as if primary vals for AOs: 1,2/3,5,6 (here the second element is merger of 2 and 3) then we will get
		// seq: 1,2,5,6 and 1,3,5,6 .
		ArrayList<ArrayList<Integer>> possibleSequencesOfPrimaryDimensionVals = new ArrayList<>();
		// multiValSeqTo1ValSeqs(activityObjects,primaryDimension, verbose);

		if (primaryDimension.equals(PrimaryDimension.ActivityID)) // each ao has single act id
		{
			ArrayList<Integer> listOfActIDs = new ArrayList<>();
			for (ActivityObject2018 ao : activityObjects)
			{
				listOfActIDs.add(ao.getActivityID());
			}
			possibleSequencesOfPrimaryDimensionVals.add(listOfActIDs);

			// sanity check start
			if (VerbosityConstants.checkSanityPDImplementn)
			{
				ArrayList<ArrayList<Integer>> dummyPossibleSequencesOfPrimaryDimensionVals = multiValSeqTo1ValSeqs(
						activityObjects, primaryDimension, verbose);

				System.out.println("Debug: dummyPossibleSequencesOfPrimaryDimensionVals= "
						+ dummyPossibleSequencesOfPrimaryDimensionVals.toString());

				System.out.println("Debug: possibleSequencesOfPrimaryDimensionVals= "
						+ possibleSequencesOfPrimaryDimensionVals.toString());

				if (!dummyPossibleSequencesOfPrimaryDimensionVals.equals(possibleSequencesOfPrimaryDimensionVals))
				{
					PopUps.printTracedErrorMsg(
							"dummyPossibleSequencesOfPrimaryDimensionVals.equals(possibleSequencesOfPrimaryDimensionVals");
				}
			} // sanity check end
		}
		else
		{
			possibleSequencesOfPrimaryDimensionVals = multiValSeqTo1ValSeqs(activityObjects, primaryDimension, verbose);
		}

		for (ArrayList<Integer> seqOfPDVals : possibleSequencesOfPrimaryDimensionVals)
		{
			StringBuilder sb = new StringBuilder();
			for (Integer v : seqOfPDVals)
			{
				Character code = uniqueCharCodes.get(v.intValue());
				// Character code = uniqueCharCodes.get(v);
				if (code == null)
				{
					PopUps.printTracedErrorMsgWithExit("Error: char code for primary dimension " + primaryDimension
							+ " val = " + v + " not found in uniqueCharCodes =" + uniqueCharCodes.toString());
				}
				sb.append(code);
			}
			stringCodeforAOs.add(sb.toString());
		}

		if (VerbosityConstants.verbose)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("\tInside getStringCodeForActivityObjects:\n Act Names:");
			activityObjects.stream().forEachOrdered(ao -> sb.append(">>" + ao.getActivityName()));
			sb.append("\nPrimary dimension vals:");
			activityObjects.stream().forEachOrdered(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("/")));
			sb.append("\npossibleSequencesOfPrimaryDimensionVals:");
			possibleSequencesOfPrimaryDimensionVals.stream().forEachOrdered(p -> sb.append(p.toString() + "  "));
			sb.append("\ncorresponding string codes:");
			stringCodeforAOs.stream().forEachOrdered(s -> sb.append(s.toString() + "  "));
			System.out.println(sb.toString());
		}
		return stringCodeforAOs;
	}

	/**
	 * extracting multiple lists(sequences) with single valued elements from one sequence of multi-valued elements,
	 * where the "one sequence of multi value elements" is the sequence of primary dimension vals from the given list of
	 * act objs
	 * <p>
	 * TODO NEEDS TO BE CHECKED FOR CORRECTNESS
	 * 
	 * @param activityObjects
	 * @param givenDimension
	 *            changed from primary dimension on 17 July 2018
	 * @since 13 July 2017
	 * @return List{List of primary dimension vals } in other words, multiple lists(sequences) with single valued
	 *         elements
	 */
	public static ArrayList<ArrayList<Integer>> multiValSeqTo1ValSeqs(ArrayList<ActivityObject2018> activityObjects,
			PrimaryDimension givenDimension, boolean verbose)
	{
		ArrayList<ArrayList<Integer>> words = null;
		// System.out.println("Inside multiValSeqTo1ValSeqs");
		try
		{
			BalancedIntegerTree integerTrie = new BalancedIntegerTree();
			for (ActivityObject2018 ao : activityObjects)
			{
				// each act obj can have multiple values for the primary dimension because of merger.
				ArrayList<Integer> pdValsForThisAO = ao.getGivenDimensionVal(givenDimension);// .getPrimaryDimensionVal();
				integerTrie.addToAllLeaves(pdValsForThisAO);
				// System.out.println("pdValsForThisAO = " + pdValsForThisAO);
			}
			words = integerTrie.getAllWords();
			if (verbose)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("\nInside multiValSeqTo1ValSeq: \n INPUT- act ids as multi valued seq :\n");
				activityObjects.stream().forEachOrdered(ao -> sb.append("-" + ao.getGivenDimensionVal(givenDimension)));
				// .getPrimaryDimensionVal()));
				sb.append("\n OUTPUT: Extracted multiple sequences of 1 val:\n ");
				words.stream().forEachOrdered(w -> sb.append("--" + w.toString()));
				System.out.println(sb.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return words;
	}

	/**
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 400 activities.
	 * 
	 * 
	 * @param activityObjects
	 * @param hierarchyLevelForEDForAO
	 * @return list of stringCodes, where each string code is for list of act name sequence in the list of act objs.
	 *         Note, we have multiple string codes when a cat id has multiple given level cat ids in hierarchy, for
	 *         example cat ID 157 Vineyard has Community as well as Food as its parent.
	 * @param verbose
	 * @return
	 */
	public static ArrayList<String> getStringCodeForActivityObjectsV2(ArrayList<ActivityObject2018> activityObjects,
			int hierarchyLevelForEDForAO, boolean verbose)
	{

		ArrayList<String> codeS = new ArrayList<>();
		ArrayList<ArrayList<Integer>> correspondCatLevelWords = new ArrayList<>();

		boolean anyCatHasMultipleCorrHierCat = false;

		// Working category level, i.e., the category level used in the act objects are same as the level desired
		if (DomainConstants.gowallaWorkingCatLevel == hierarchyLevelForEDForAO)
		{
			return (ArrayList<String>) Collections
					.singletonList(getStringCodeForActivityObjectsFromActID(activityObjects));
		}

		// Working category level, i.e., the category level used in the act objects is lower (more specific) than the
		// level desired

		else if (DomainConstants.gowallaWorkingCatLevel > hierarchyLevelForEDForAO)
		{
			if (hierarchyLevelForEDForAO != Constant.HierarchicalCatIDLevelForEditDistance)
			{
				System.err.println(PopUps.getTracedErrorMsg(
						"Error: hierarchyLevelForEDForAO != Constant.HierarchicalLevelForEditDistance"));
			}

			BalancedIntegerTree integerTrie = new BalancedIntegerTree();

			for (ActivityObject2018 ao : activityObjects)
			{
				// extract the corresponding hierarchy level cat id, its a list because there can be more than one. for
				// example, Vineyard is in Community as well as Food
				ArrayList<Integer> desiredHierarchyLevelActiIDs = DomainConstants.getGivenLevelCatID(ao.getActivityID(),
						hierarchyLevelForEDForAO);
				// convert to ArrayList of String/Character
				// ArrayList<String> desiredHierarchyLevelActiIDsChar = (ArrayList<String>) desiredHierarchyLevelActiIDs
				// .stream().map(i -> Integer.toString(i)).collect(Collectors.toList());

				integerTrie.addToAllLeaves(desiredHierarchyLevelActiIDs);
				// System.out.println(
				// "Debug: integerTrie.addToAllLeaves(desiredHierarchyLevelActiIDs), where desiredHierarchyLevelActiIDs
				// = "+ desiredHierarchyLevelActiIDs);

				if (desiredHierarchyLevelActiIDs.size() > 1)
				{
					anyCatHasMultipleCorrHierCat = true;
				}
			}

			correspondCatLevelWords = integerTrie.getAllWords();

			// now we have got Strings of catid, there will be multiple string if any cat id as multiple desired level
			// cat ids
			for (ArrayList<Integer> word : correspondCatLevelWords)
			{
				StringBuilder sb = new StringBuilder();
				for (Integer id : word)
				{
					sb.append(StringCode.getCharCodeFromActivityID(id));
				}
				codeS.add(sb.toString());
			}
		}
		else
		{
			System.err.println(PopUps
					.getTracedErrorMsg("Error: DomainConstants.gowallaWorkingCatLevel < hierarchyLevelForEDForAO"));
		}

		if (verbose)
		{
			System.out.print("\nInside getStringCodeForActivityObjects: act ids :");
			activityObjects.stream().forEachOrdered(ao -> System.out.print("-" + ao.getActivityID()));
			System.out.println("\ncorrespondCatLevelWords.size() = " + correspondCatLevelWords.size());

			System.out.print("Words for corr level " + hierarchyLevelForEDForAO + " act ids: ");
			correspondCatLevelWords.stream().forEachOrdered(word -> System.out.print("-" + word.toString()));

			System.out.println("\nReturned CodeS: " + codeS);
		}

		if (anyCatHasMultipleCorrHierCat == false && codeS.size() > 1)
		{
			PopUps.printTracedErrorMsg("anyCatHasMultipleCorrHierCat==false && codeS.size()>1");
		}

		if (codeS.size() != correspondCatLevelWords.size())
		{
			PopUps.printTracedErrorMsg("codeS.size() != correspondCatLevelStrings.size()");

		}
		// System.out.println("Inside getStringCodeForActivityObjects() oldOne:"
		// + getStringCodeForActivityObjects(activityObjects) + " new one returned=" + codeS);

		return codeS;
	}

	/**
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 400 activities.
	 * 
	 * 
	 * @param activityObjects
	 * @param hierarchyLevelForEDForAO
	 * @return list of stringCodes, where each string code is for list of act name sequence in the list of act objs.
	 *         Note, we have multiple string codes when a cat id has multiple given level cat ids in hierarchy, for
	 *         example cat ID 157 Vineyard has Community as well as Food as its parent.
	 */
	public static String getStringCodeForActivityObjectsV1(ArrayList<ActivityObject2018> activityObjects,
			int hierarchyLevelForEDForAO)
	{
		// System.out.println("Inside getStringCodeForActivityObjects");
		String codeS = "";

		// Working category level, i.e., the category level used in the act objects are same as the level desired
		if (DomainConstants.gowallaWorkingCatLevel == hierarchyLevelForEDForAO)
		{
			return getStringCodeForActivityObjectsFromActID(activityObjects);
		}

		// Working category level, i.e., the category level used in the act objects is lower (more specific) than the
		// level desired
		else if (DomainConstants.gowallaWorkingCatLevel > hierarchyLevelForEDForAO)
		{
			if (hierarchyLevelForEDForAO != Constant.HierarchicalCatIDLevelForEditDistance)
			{
				System.err.println(PopUps.getTracedErrorMsg(
						"Error: hierarchyLevelForEDForAO != Constant.HierarchicalLevelForEditDistance"));
			}

			StringBuilder code = new StringBuilder();

			for (ActivityObject2018 ao : activityObjects)
			{
				int workingLevelActID = ao.getActivityID();
				ArrayList<Integer> desiredHierarchyLevelActiIDs = DomainConstants.getGivenLevelCatID(workingLevelActID,
						hierarchyLevelForEDForAO);

				// System.out.println("workingLevelActID=" + workingLevelActID + " desiredHierarchyLevelActiID="
				// + desiredHierarchyLevelActiID);

				if (desiredHierarchyLevelActiIDs.size() == 1)
				{
					code.append(StringCode.getCharCodeFromActivityID(desiredHierarchyLevelActiIDs.get(0)));
				}
			}
			codeS = code.toString();
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error: DomainConstants.gowallaWorkingCatLevel <                hierarchyLevelForEDForAO"));

		}

		// System.out.println("Inside getStringCodeForActivityObjects() oldOne:"
		// + getStringCodeForActivityObjects(activityObjects) + " new one returned=" + codeS);

		return codeS;
	}

	// public static String getStringCodeForStartLatitudes(ArrayList<ActivityObject> actObjs)
	// {
	// String resultant = new String();
	//
	// double vals[] = new double[actObjs.size()];
	// long stamps[] = new long[actObjs.size()];
	// ;
	//
	// int i = 0;
	// for (ActivityObject ao : actObjs)
	// {
	// vals[i] = Double.parseDouble(ao.getStartLatitude());
	// }
	//
	// for (ActivityObject ao : actObjs)
	// {
	// stamps[i] = ao.getStartTimestamp().getTime();
	// }
	//
	// try
	// {
	// Timeseries ts = new Timeseries(vals, stamps);
	//
	// resultant = SAXFactory.ts2string(ts, actObjs.size(), new NormalAlphabet(), 10);
	//
	// // System.out.println("String representation = "+ resultant);
	// }
	//
	// catch (TSException | CloneNotSupportedException e)
	// {
	// e.printStackTrace();
	// }
	//
	// if (resultant.length() == 0)
	// {
	// System.err.println("Error in getStringCodeForStartTime(): no resultant SAX ");
	// }
	// return resultant;
	// }

	// public static String getStringCodeForStartLongitudes(ArrayList<ActivityObject> actObjs)
	// {
	// String resultant = new String();
	//
	// double vals[] = new double[actObjs.size()];
	// long stamps[] = new long[actObjs.size()];
	// ;
	//
	// int i = 0;
	// for (ActivityObject ao : actObjs)
	// {
	// vals[i] = Double.parseDouble(ao.getStartLatitude());
	// }
	//
	// for (ActivityObject ao : actObjs)
	// {
	// stamps[i] = ao.getStartTimestamp().getTime();
	// }
	//
	// try
	// {
	// Timeseries ts = new Timeseries(vals, stamps);
	//
	// resultant = SAXFactory.ts2string(ts, actObjs.size(), new NormalAlphabet(), 10);
	//
	// // System.out.println("String representation = "+ resultant);
	// }
	//
	// catch (TSException | CloneNotSupportedException e)
	// {
	// e.printStackTrace();
	// }
	//
	// if (resultant.length() == 0)
	// {
	// System.err.println("Error in getStringCodeForStartTime(): no resultant SAX ");
	// }
	// return resultant;
	// }

}
