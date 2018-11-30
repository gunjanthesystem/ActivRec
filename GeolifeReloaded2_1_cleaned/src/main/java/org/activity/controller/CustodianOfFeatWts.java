package org.activity.controller;

import java.util.HashSet;
import java.util.Set;

/**
 * To search for optimal wts for features
 * 
 * @author gunjan
 * @since 21 Nov 2018
 */
public class CustodianOfFeatWts
{
	public static double givenWtActivityName;
	public static double givenWtStartTime;
	public static double givenWtDuration;
	public static double givenWtDistanceTravelled;
	public static double givenWtStartGeo;
	public static double givenWtEndGeo;
	public static double givenWtAvgAltitude;
	static Set<String> alreadyTried = new HashSet<>();

	public static void addToAlreadyTriedWts(String wtsAsString)
	{
		alreadyTried.add(wtsAsString);
	}

	public static boolean isAlreadyTried(String wtsAsString)
	{
		return alreadyTried.contains(wtsAsString);
	}

	public static String toStringWts()
	{
		return "CustodianOfFeatWts=\ngivenWtActivityName= " + givenWtActivityName + " givenWtStartTime= "
				+ givenWtStartTime + " givenWtDuration= " + givenWtDuration + " givenWtDistanceTravelled= "
				+ givenWtDistanceTravelled + " givenWtStartGeo= " + givenWtStartGeo + " givenWtEndGeo= " + givenWtEndGeo
				+ " givenWtAvgAltitude= " + givenWtAvgAltitude;
	}

	public static String toCSVWts()
	{
		return givenWtActivityName + "," + givenWtStartTime + "," + givenWtDuration + "," + givenWtDistanceTravelled
				+ "," + givenWtStartGeo + "," + givenWtEndGeo + "," + givenWtAvgAltitude;
	}
}
