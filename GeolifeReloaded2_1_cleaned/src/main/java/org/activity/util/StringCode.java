package org.activity.util;

import java.util.ArrayList;

import org.activity.objects.ActivityObject;
import org.activity.ui.PopUps;
import org.apache.commons.lang3.ArrayUtils;

//import com.google.uzaygezen.core.GunjanUtils;

public class StringCode
{
	/**
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @param featureName
	 * @return
	 */
	public static String[] getRelativeStringCodesForFeature(ArrayList<ActivityObject> activityObjects1,
			ArrayList<ActivityObject> activityObjects2, String featureName)
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
	 * @param code
	 * @return
	 */
	public static String getActivityNameFromStringCode(String code)
	{
		String name = null;
		;
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
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID and hence is guaranteed to be unique for at least 107
	 * activities.
	 * 
	 * @param activityName
	 * @return
	 */
	public static String getStringCodeFromActivityName(String activityName)
	{
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
	 * 
	 * @param actObjs
	 * @return
	 */
	public static String getStringCodeForStartTime(ArrayList<ActivityObject> actObjs)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodeForStartTime");
		
		String resultant = new String();
		
		double vals[] = new double[actObjs.size()];
		long stamps[] = new long[actObjs.size()];
		
		for (int i = 0; i < actObjs.size(); i++)
		{
			vals[i] = DateTimeUtils.getTimeInDayInSeconds(actObjs.get(i).getStartTimestamp()); // should i convert it to minutes
			stamps[i] = actObjs.get(i).getStartTimestamp().getTime();
			
			if (Constant.verboseSAX)
				System.out.print(vals[i] + " ");
		}
		
		try
		{
			resultant = SAXUtilityBelt.getSAXString(vals, stamps, actObjs.size(), Constant.SAXStartTimeAlphabsetSize);// Constant.SAXStartTimeAlphabetSize);//
																														// SAXFactory.ts2string(ts,
																														// actObjs.size(), new
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
		
		if (Constant.verboseSAX)
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
	public static String[] getStringCodesForStartTime(ArrayList<ActivityObject> actObjs1, ArrayList<ActivityObject> actObjs2)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodeForStartTime");
		
		String finalResultant[] = new String[2];
		
		double vals1[] = new double[actObjs1.size()];
		long stamps1[] = new long[actObjs1.size()];
		for (int i = 0; i < actObjs1.size(); i++)
		{
			vals1[i] = DateTimeUtils.getTimeInDayInSeconds(actObjs1.get(i).getStartTimestamp()); // should i convert it to minutes
			stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals1[i] + " ");
		}
		if (Constant.verboseSAX)
			System.out.println();
		
		double vals2[] = new double[actObjs2.size()];
		long stamps2[] = new long[actObjs2.size()];
		for (int i = 0; i < actObjs2.size(); i++)
		{
			vals2[i] = DateTimeUtils.getTimeInDayInSeconds(actObjs2.get(i).getStartTimestamp()); // should i convert it to minutes
			stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals2[i] + " ");
		}
		
		double valsAll[] = ArrayUtils.addAll(vals1, vals2);
		long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);
		String resultant;
		try
		{
			resultant = SAXUtilityBelt.getSAXString(valsAll, stampsAll, stampsAll.length, Constant.SAXStartTimeAlphabsetSize);
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
		
		if (Constant.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}
	
	public static String getStringCodeForDuration(ArrayList<ActivityObject> actObjs)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodeForDuration");
		
		String resultant = new String();
		
		double vals[] = new double[actObjs.size()];
		long stamps[] = new long[actObjs.size()];
		
		for (int i = 0; i < actObjs.size(); i++)
		{
			vals[i] = actObjs.get(i).getDurationInSeconds(); // should i convert it to minutes
			stamps[i] = actObjs.get(i).getStartTimestamp().getTime();
			
			if (Constant.verboseSAX)
				System.out.print(vals[i] + " ");
		}
		
		try
		{
			resultant = SAXUtilityBelt.getSAXString(vals, stamps, actObjs.size(), Constant.SAXDurationAlphabsetSize);// SAXFactory.ts2string(ts, actObjs.size(), new
			// NormalAlphabet(), 10);
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
		if (Constant.verboseSAX)
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
	public static String[] getStringCodesForDuration(ArrayList<ActivityObject> actObjs1, ArrayList<ActivityObject> actObjs2)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodeForDuration");
		
		String finalResultant[] = new String[2];
		
		double vals1[] = new double[actObjs1.size()];
		long stamps1[] = new long[actObjs1.size()];
		
		for (int i = 0; i < actObjs1.size(); i++)
		{
			vals1[i] = actObjs1.get(i).getDurationInSeconds(); // should i convert it to minutes
			stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
			
			if (Constant.verboseSAX)
				System.out.print(vals1[i] + " ");
		}
		if (Constant.verboseSAX)
			System.out.println();
		
		double vals2[] = new double[actObjs2.size()];
		long stamps2[] = new long[actObjs2.size()];
		for (int i = 0; i < actObjs2.size(); i++)
		{
			vals2[i] = actObjs2.get(i).getDurationInSeconds(); // should i convert it to minutes
			stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals2[i] + " ");
		}
		
		double valsAll[] = ArrayUtils.addAll(vals1, vals2);
		long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);
		
		String resultant;
		try
		{
			resultant = SAXUtilityBelt.getSAXString(valsAll, stampsAll, stampsAll.length, Constant.SAXDurationAlphabsetSize);
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
		
		if (Constant.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}
	
	public static String getStringCodeForDistanceTravelled(ArrayList<ActivityObject> actObjs)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodeForDistanceTravelled");
		String resultant = new String();
		
		double vals[] = new double[actObjs.size()];
		long stamps[] = new long[actObjs.size()];
		
		for (int i = 0; i < actObjs.size(); i++)
		{
			vals[i] = actObjs.get(i).getDistanceTravelled();
			stamps[i] = actObjs.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals[i] + " ");
		}
		
		try
		{
			resultant = SAXUtilityBelt.getSAXString(vals, stamps, actObjs.size(), Constant.SAXDistanceTravelledAlphabsetSize);// SAXFactory.ts2string(ts,
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
		if (Constant.verboseSAX)
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
	public static String[] getStringCodesForDistanceTravelled(ArrayList<ActivityObject> actObjs1, ArrayList<ActivityObject> actObjs2)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodeForDistanceTravelled");
		String finalResultant[] = new String[2];
		
		double vals1[] = new double[actObjs1.size()];
		long stamps1[] = new long[actObjs1.size()];
		
		for (int i = 0; i < actObjs1.size(); i++)
		{
			vals1[i] = actObjs1.get(i).getDistanceTravelled();
			stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals1[i] + " ");
		}
		
		if (Constant.verboseSAX)
			System.out.println();
		
		double vals2[] = new double[actObjs2.size()];
		long stamps2[] = new long[actObjs2.size()];
		for (int i = 0; i < actObjs2.size(); i++)
		{
			vals2[i] = actObjs2.get(i).getDistanceTravelled();
			stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals2[i] + " ");
		}
		
		double valsAll[] = ArrayUtils.addAll(vals1, vals2);
		long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);
		
		String resultant;
		try
		{
			resultant = SAXUtilityBelt.getSAXString(valsAll, stampsAll, stampsAll.length, Constant.SAXDistanceTravelledAlphabsetSize);
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
		
		if (Constant.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}
	
	public static String getStringCodeForAvgAltitudes(ArrayList<ActivityObject> actObjs)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodeForAvgAltitudes");
		String resultant = new String();
		
		double vals[] = new double[actObjs.size()];
		long stamps[] = new long[actObjs.size()];
		
		for (int i = 0; i < actObjs.size(); i++)
		{
			vals[i] = Double.parseDouble(actObjs.get(i).getAvgAltitude());
			stamps[i] = actObjs.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals[i] + " ");
		}
		
		try
		{
			resultant = SAXUtilityBelt.getSAXString(vals, stamps, actObjs.size(), Constant.SAXAvgAltitudeAlphabsetSize);// SAXFactory.ts2string(ts, actObjs.size(),
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
		if (Constant.verboseSAX)
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
	public static String[] getStringCodesForAvgAltitudes(ArrayList<ActivityObject> actObjs1, ArrayList<ActivityObject> actObjs2)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodeForAvgAltitudes");
		
		String finalResultant[] = new String[2];
		
		double vals1[] = new double[actObjs1.size()];
		long stamps1[] = new long[actObjs1.size()];
		
		for (int i = 0; i < actObjs1.size(); i++)
		{
			vals1[i] = Double.parseDouble(actObjs1.get(i).getAvgAltitude());
			stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals1[i] + " ");
		}
		
		if (Constant.verboseSAX)
			System.out.println();
		
		double vals2[] = new double[actObjs2.size()];
		long stamps2[] = new long[actObjs2.size()];
		for (int i = 0; i < actObjs2.size(); i++)
		{
			vals2[i] = Double.parseDouble(actObjs2.get(i).getAvgAltitude());
			stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
			if (Constant.verboseSAX)
				System.out.print(vals2[i] + " ");
		}
		
		double valsAll[] = ArrayUtils.addAll(vals1, vals2);
		long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);
		
		String resultant;
		try
		{
			resultant = SAXUtilityBelt.getSAXString(valsAll, stampsAll, stampsAll.length, Constant.SAXAvgAltitudeAlphabsetSize);
			
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
		
		if (Constant.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}
	
	/**
	 * Sequence of (lat,lon) pairs extracted from the two given activity-objects converted to hilbert space filled curve index , which is then converted to symbol sequence using
	 * SAX. </br>
	 * note: the hilbert space filled curve (hsfc) implementation takes only long data types as input.
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return
	 */
	public static String[] getStringCodesForStartGeoCoordinates(ArrayList<ActivityObject> actObjs1, ArrayList<ActivityObject> actObjs2)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodesForGeoCoordinates");
		
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
				
				vals1[i] = GunjanUtils.getCompactHilbertCurveIndex(latitude1AsLong, longitude1AsLong);
				stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
				if (Constant.verboseHilbert)
				{
					System.out.println("lat:" + latitude1 + " lon:" + longitude1 + "->latL:" + latitude1AsLong + ", lonL:"
							+ longitude1AsLong + "  HSFC ind:" + vals1[i]);
				}
				
				if (Constant.verboseSAX && !Constant.verboseHilbert)
					System.out.print(vals1[i] + " ");
			}
			
			if (Constant.verboseSAX)
				System.out.println();
			
			double vals2[] = new double[actObjs2.size()];
			long stamps2[] = new long[actObjs2.size()];
			
			for (int i = 0; i < actObjs2.size(); i++)
			{
				double latitude2 = Double.parseDouble(actObjs2.get(i).getStartLatitude());
				double longitude2 = Double.parseDouble(actObjs2.get(i).getStartLongitude());
				
				long latitude2AsLong = (long) (latitude2 * Constant.decimalPlacesInGeocordinatesForComputations);
				long longitude2AsLong = (long) (longitude2 * Constant.decimalPlacesInGeocordinatesForComputations);
				
				vals2[i] = GunjanUtils.getCompactHilbertCurveIndex(latitude2AsLong, longitude2AsLong);
				stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
				if (Constant.verboseHilbert)
				{
					System.out.println("lat:" + latitude2 + " lon:" + longitude2 + "->latL:" + latitude2AsLong + ", lonL:"
							+ longitude2AsLong + "  HSFC ind:" + vals2[i]);
				}
				
				if (Constant.verboseSAX && !Constant.verboseHilbert)
					System.out.print(vals2[i] + " ");
			}
			
			double valsAll[] = ArrayUtils.addAll(vals1, vals2);
			long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);
			
			String resultant;
			resultant = SAXUtilityBelt.getSAXString(valsAll, stampsAll, stampsAll.length, Constant.SAXAvgAltitudeAlphabsetSize);// Constant.SAXStartTimeAlphabetSize);//
			
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
		
		if (Constant.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
	}
	
	/**
	 * Sequence of (lat,lon) pairs extracted from the two given activity-objects converted to hilbert space filled curve index , which is then converted to symbol sequence using
	 * SAX. </br>
	 * note: the hilbert space filled curve (hsfc) implementation takes only long data types as input.
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return
	 */
	public static String[] getStringCodesForEndGeoCoordinates(ArrayList<ActivityObject> actObjs1, ArrayList<ActivityObject> actObjs2)
	{
		if (Constant.verboseSAX)
			System.out.println("Inside getStringCodesForGeoCoordinates");
		
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
				
				vals1[i] = GunjanUtils.getCompactHilbertCurveIndex(latitude1AsLong, longitude1AsLong);
				stamps1[i] = actObjs1.get(i).getStartTimestamp().getTime();
				if (Constant.verboseHilbert)
				{
					System.out.println("lat:" + latitude1 + " lon:" + longitude1 + "->latL:" + latitude1AsLong + ", lonL:"
							+ longitude1AsLong + "  HSFC ind:" + vals1[i]);
				}
				
				if (Constant.verboseSAX && !Constant.verboseHilbert)
					System.out.print(vals1[i] + " ");
			}
			
			if (Constant.verboseSAX)
				System.out.println();
			
			double vals2[] = new double[actObjs2.size()];
			long stamps2[] = new long[actObjs2.size()];
			
			for (int i = 0; i < actObjs2.size(); i++)
			{
				double latitude2 = Double.parseDouble(actObjs2.get(i).getEndLatitude());
				double longitude2 = Double.parseDouble(actObjs2.get(i).getEndLongitude());
				
				long latitude2AsLong = (long) (latitude2 * Constant.decimalPlacesInGeocordinatesForComputations);
				long longitude2AsLong = (long) (longitude2 * Constant.decimalPlacesInGeocordinatesForComputations);
				
				vals2[i] = GunjanUtils.getCompactHilbertCurveIndex(latitude2AsLong, longitude2AsLong);
				stamps2[i] = actObjs2.get(i).getStartTimestamp().getTime();
				if (Constant.verboseHilbert)
				{
					System.out.println("lat:" + latitude2 + " lon:" + longitude2 + "->latL:" + latitude2AsLong + ", lonL:"
							+ longitude2AsLong + "  HSFC ind:" + vals2[i]);
				}
				
				if (Constant.verboseSAX && !Constant.verboseHilbert)
					System.out.print(vals2[i] + " ");
			}
			
			double valsAll[] = ArrayUtils.addAll(vals1, vals2);
			long stampsAll[] = ArrayUtils.addAll(stamps1, stamps2);
			
			String resultant;
			resultant = SAXUtilityBelt.getSAXString(valsAll, stampsAll, stampsAll.length, Constant.SAXAvgAltitudeAlphabsetSize);// Constant.SAXStartTimeAlphabetSize);//
			
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
		
		if (Constant.verboseSAX)
		{
			System.out.println("SAX: " + finalResultant[0] + " &  " + finalResultant[1]);
		}
		return finalResultant;
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
