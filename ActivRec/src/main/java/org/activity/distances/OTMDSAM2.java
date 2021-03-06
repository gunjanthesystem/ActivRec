package org.activity.distances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Triple;

/**
 * 
 * @author gunjan
 *
 */
public class OTMDSAM2 extends AlignmentBasedDistance
{
	/**
	 * 
	 * @param primaryDimension
	 */
	public OTMDSAM2(PrimaryDimension primaryDimension)
	{
		super(primaryDimension); // nothing used form super yet
	}

	/**
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return OTMDSAM Edit Distance between the two activity objects
	 */
	@SuppressWarnings("unchecked")
	public Pair<String, Double> getOTMDSAMEditDistanceWithTrace(ArrayList<ActivityObject2018> activityObjects1,
			ArrayList<ActivityObject2018> activityObjects2)
	{
		// primary dimension introduced on 17 July 2018.
		LinkedHashMap<String, Pair<String, Double>> featureWiseEditDistance = new FeatureWiseEditDistance(
				this.primaryDimension).getFeatureWiseEditDistanceWithTrace(activityObjects1, activityObjects2);
		@SuppressWarnings("rawtypes")
		ArrayList allEditOperations = getAllEditOperations(featureWiseEditDistance);

		if (allEditOperations.size() > 3)
		{
			System.err.println(
					"Error in org.activity.distances.OTMDSAM.getOTMDSAMEditDistance() size of allEditOperations (>3) = "
							+ allEditOperations.size()); // because it should be for insertions,
															// deletions and replacements, and
															// thus only three elements
		}
		return calculateOTMDSAMDistance((ArrayList<Pair<Integer, String>>) allEditOperations.get(0),
				(ArrayList<Pair<Integer, String>>) allEditOperations.get(1),
				(ArrayList<Triple<Integer, Integer, String>>) allEditOperations.get(2));
	}

	/**
	 * 
	 * @param pairs
	 * @return an ArrayList where the first element is an ArrayList of insertions, seconds element is an ArrayList of
	 *         deletions and third element us an ArrayList of replacements
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList getAllEditOperations(LinkedHashMap<String, Pair<String, Double>> pairs)
	{
		// Obtain the list of insertion, deletions and replacements operations for minimum edit distance for all
		// features.
		// <coordinate of edit operation, feature name>
		ArrayList<Pair<Integer, String>> insertions = new ArrayList<Pair<Integer, String>>();
		ArrayList<Pair<Integer, String>> deletions = new ArrayList<Pair<Integer, String>>();
		ArrayList<Triple<Integer, Integer, String>> replacements = new ArrayList<Triple<Integer, Integer, String>>();
		if (VerbosityConstants.verboseOTMDSAM) System.out.println("Inside getAllEditOperations");

		for (Map.Entry<String, Pair<String, Double>> entry : pairs.entrySet())
		{
			String featureName = entry.getKey();
			if (VerbosityConstants.verboseOTMDSAM)
			{
				System.out.println("\tFeature: " + entry.getKey());
				System.out.println("\tLevenshtein distance: " + entry.getValue().getSecond());
				System.out.println("\tTrace: " + entry.getValue().getFirst());
			}
			String[] splitted = entry.getValue().getFirst().split("_"); // "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(6-2)";

			for (int i = 1; i < splitted.length; i++)
			{
				String op = splitted[i]; // D(1-0)
				String[] splitOps = op.split("\\("); // D and 1-0)

				// System.out.println(splitted[i]); //D(1-0)
				String operation = splitOps[0]; // D

				String splitCo[] = splitOps[1].split("-"); // 1 and 0)
				String splitCoAgain[] = splitCo[1].split("\\)"); // 0 and nothing

				int coordOfAO1 = Integer.parseInt(splitCo[0]) - 1;// 1 minus 1
				int coordOfAO2 = Integer.parseInt(splitCoAgain[0]) - 1; // 0 minus 1

				// int coordOfAO1= Character.getNumericValue(splitOps[1].charAt(0))-1;//1
				// int coordOfAO2=Character.getNumericValue(splitOps[1].charAt(2))-1;//0
				// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);

				if (operation.equals("D"))
				{
					deletions.add(new Pair<Integer, String>(coordOfAO1, featureName));
				}

				else if (operation.equals("I"))
				{
					insertions.add(new Pair<Integer, String>(coordOfAO2, featureName));
				}

				else if (operation.equals("S"))
				{
					replacements.add(new Triple<Integer, Integer, String>(coordOfAO1, coordOfAO2, featureName));
				}
			}
		}

		if (VerbosityConstants.verboseOTMDSAM)
		{
			printAllEditOperations(insertions, deletions, replacements);
		}

		ArrayList all = new ArrayList();
		all.add(insertions);
		all.add(deletions);
		all.add(replacements);

		if (VerbosityConstants.verboseOTMDSAM) System.out.println("exiting getAllEditOperations");

		return all;
	}

	/**
	 * 
	 * @param insertions
	 *            ArrayList containing all insertions for edit distances for all features as Pair<Position of insertion
	 *            from target, feature name>
	 * @param deletions
	 *            ArrayList containing all deletion for edit distances for all features as Pair<Position of deletion in
	 *            source, feature name>
	 * @param replacements
	 *            ArrayList containing all replacements for edit distances for all features as Triple<Position of
	 *            replacement in source,Position of replacement from target, feature name>
	 * @return
	 */
	public final Pair<String, Double> calculateOTMDSAMDistance(ArrayList<Pair<Integer, String>> insertions,
			ArrayList<Pair<Integer, String>> deletions, ArrayList<Triple<Integer, Integer, String>> replacements)
	{
		// double costOfInsertion
		// find insertions happening at the same position but across different features
		for (Pair<Integer, String> p : insertions)
		{

		}

		return null;
	}

	// //////////////
	/**
	 * 
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getOTMDSAMEditDistanceWithoutEndCurrentActivity(
			ArrayList<ActivityObject2018> activityObjects1Original, ArrayList<ActivityObject2018> activityObjects2Original)// ,
																													// String
																													// userAtRecomm,
																													// String
																													// dateAtRecomm,
																													// String
																													// timeAtRecomm,
																													// Integer
																													// candidateTimelineId)
	{
		ArrayList<ActivityObject2018> activityObjects1 = new ArrayList<ActivityObject2018>();
		activityObjects1.addAll(activityObjects1Original);

		ArrayList<ActivityObject2018> activityObjects2 = new ArrayList<ActivityObject2018>();
		activityObjects2.addAll(activityObjects2Original);

		activityObjects1.remove(activityObjects1.size() - 1);
		activityObjects2.remove(activityObjects2.size() - 1);

		Pair<String, Double> result = getOTMDSAMEditDistanceWithTrace(activityObjects1, activityObjects2);// ,
																											// userAtRecomm,
																											// dateAtRecomm,
																											// timeAtRecomm,
																											// candidateTimelineId);

		// $$WritingToFile.writeEditSimilarityCalculation(activityObjects1, activityObjects2, result.getSecond(),
		// result.getFirst()); //uncomment to write edit distance calculations
		// $$WritingToFile.writeOnlyTrace(result.getFirst()); //uncomment to write trace to a file

		return result;
	}

	/**
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getOTMDSAMEditDistanceInvalidsExpunged(ArrayList<ActivityObject2018> activityObjects1,
			ArrayList<ActivityObject2018> activityObjects2)// , String userAtRecomm,
	// String dateAtRecomm, String timeAtRecomm, Integer candidateTimelineId)
	{
		return getOTMDSAMEditDistanceWithTrace(expungeInvalids(activityObjects1), expungeInvalids(activityObjects2));// ,
																														// userAtRecomm,
																														// dateAtRecomm,
																														// timeAtRecomm,
																														// candidateTimelineId);//
																														// similarity;
	}

	/**
	 * Finds the Edit similarity between two given lists of ActivityObjects ignoring the invalid ActivityObjects
	 * 
	 * Version Oct 10
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getOTMDSAMEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
			ArrayList<ActivityObject2018> activityObjects1, ArrayList<ActivityObject2018> activityObjects2)
	{
		return getOTMDSAMEditDistanceWithoutEndCurrentActivity(expungeInvalids(activityObjects1),
				expungeInvalids(activityObjects2));
	}

	/**
	 * 
	 * @param insertions
	 * @param deletions
	 * @param replacements
	 */
	public void printAllEditOperations(ArrayList<Pair<Integer, String>> insertions,
			ArrayList<Pair<Integer, String>> deletions, ArrayList<Triple<Integer, Integer, String>> replacements)
	{
		System.out.println("Insertions:");
		for (Pair<Integer, String> p : insertions)
		{
			System.out.println(p.getFirst() + " " + p.getSecond());
		}
		System.out.println("Deletions:");
		for (Pair<Integer, String> p : deletions)
		{
			System.out.println(p.getFirst() + " " + p.getSecond());
		}
		System.out.println("Replacements:");
		for (Triple<Integer, Integer, String> p : replacements)
		{
			System.out.println(p.getFirst() + " " + p.getSecond() + " " + p.getThird());
		}
	}

	/**
	 * 
	 * @param pairs
	 */
	public final void traverse(HashMap<String, Pair<String, Double>> pairs)// , ArrayList<ActivityObject>
																			// activityObjects1,
																			// ArrayList<ActivityObject>
																			// activityObjects2)

	{
		System.out.println("\nTraversing map of feature levenshtein distances SAX: ");
		for (Map.Entry<String, Pair<String, Double>> entry : pairs.entrySet())
		{
			System.out.println("\tFeature: " + entry.getKey());
			System.out.println("\tLevenshtein distance: " + entry.getValue().getSecond());
			System.out.println("\tTrace: " + entry.getValue().getFirst());
		}
		System.out.println("------------------------------------------------------");
	}
	// /////////

	// /**
	// *
	// * @param activityObjects1
	// * @param activityObjects2
	// * @return ArrayList of Pairs where each Pair objetc contains the levenshtein distance for that feature and the
	// trace of operations
	// */
	// // getFeatureStringLevenshteinSAXWithTrace
	// public final LinkedHashMap<String, Pair<String, Double>>
	// getOTMDSAMEditDistanceWithTrace(ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject>
	// activityObjects2)
	// {
	// /**
	// * <ActivityName,Pair<Trace,EditDistance>>
	// */
	// LinkedHashMap<String, Pair<String, Double>> mapOfDistances = new LinkedHashMap<String, Pair<String, Double>>();
	//
	// // if (Constant.considerAllFeaturesForOTMDSAMEditDistance || Constant.considerActivityNameInOTMDSAMEditDistance)
	// {
	// String stringCodeForActivityNames1 = ActivityObject.getStringCodeForActivityObjects(activityObjects1);
	// String stringCodeForActivityNames2 = ActivityObject.getStringCodeForActivityObjects(activityObjects2);
	// Pair<String, Double> levenshteinActivityName = getMySimpleLevenshteinDistance(stringCodeForActivityNames1,
	// stringCodeForActivityNames2, 1, 1, 2);
	// mapOfDistances.put("ActivityName", levenshteinActivityName);
	// }
	// // String stringCodeForStartTime1 = StringCode.getStringCodeForStartTime(activityObjects1);
	// // String stringCodeForStartTime2 = StringCode.getStringCodeForStartTime(activityObjects2);
	// // Pair<String, Double> levenshteinStartTime = getMySimpleLevenshteinDistance(stringCodeForStartTime1,
	// stringCodeForStartTime2, 1, 1, 2);
	// // mapOfDistances.put("StartTime", levenshteinStartTime);
	// // if (Constant.considerAllFeaturesForOTMDSAMEditDistance || Constant.considerStartTimeInOTMDSAMEditDistance)
	// {
	// String stringCodeForStartTimes[] = StringCode.getStringCodesForStartTime(activityObjects1, activityObjects2);
	// // String stringCodeForStartTime2 = StringCode.getStringCodeForStartTime(activityObjects2);
	// Pair<String, Double> levenshteinStartTime = getMySimpleLevenshteinDistance(stringCodeForStartTimes[0],
	// stringCodeForStartTimes[1], 1, 1, 2);
	// mapOfDistances.put("StartTime", levenshteinStartTime);
	// }
	//
	// // if (Constant.considerAllFeaturesForOTMDSAMEditDistance || Constant.considerDurationInOTMDSAMEditDistance)
	// {
	// String stringCodeForDurations[] = StringCode.getStringCodesForDuration(activityObjects1, activityObjects2);
	// // String stringCodeForDuration2 = StringCode.getStringCodeForDuration(activityObjects2);
	// Pair<String, Double> levenshteinDuration = getMySimpleLevenshteinDistance(stringCodeForDurations[0],
	// stringCodeForDurations[1], 1, 1, 2);
	// mapOfDistances.put("Duration", levenshteinDuration);
	// }
	//
	// if (Constant.DATABASE_NAME.equals("geolife1"))
	// {
	// // if (Constant.considerAllFeaturesForOTMDSAMEditDistance ||
	// Constant.considerDistanceTravelledInOTMDSAMEditDistance)
	// {
	// // String stringCodeForDistanceTravelled1 = StringCode.getStringCodeForDistanceTravelled(activityObjects1);
	// String stringCodesForDistanceTravelled[] = StringCode.getStringCodesForDistanceTravelled(activityObjects1,
	// activityObjects2);
	// Pair<String, Double> levenshteinDistanceTravelled =
	// getMySimpleLevenshteinDistance(stringCodesForDistanceTravelled[0], stringCodesForDistanceTravelled[1], 1, 1, 2);
	// mapOfDistances.put("DistanceTravelled", levenshteinDistanceTravelled);
	// }
	// // String stringCodeForStartGeo1 = StringCode.getStringCodeForStartGeo(activityObjects1);
	// // String stringCodeForStartGeo2 = StringCode.getStringCodeForStartGeo(activityObjects2);
	// // Pair<String, Double> levenshteinStartGeo = getMySimpleLevenshteinDistance(stringCodeForStartGeo1,
	// stringCodeForStartGeo2, 1, 1, 2);
	// // String stringCodeForEndGeo1 = StringCode.getStringCodeForEndGeo(activityObjects1);
	// // String stringCodeForEndGeo2 = StringCode.getStringCodeForEndGeo(activityObjects2);
	// // Pair<String, Double> levenshteinEndGeo = getMySimpleLevenshteinDistance(stringCodeForEndGeo1,
	// stringCodeForEndGeo2, 1, 1, 2);
	//
	// // if (Constant.considerAllFeaturesForOTMDSAMEditDistance ||
	// Constant.considerStartGeoCoordinatesInOTMDSAMEditDistance)
	// {
	// String stringCodesForStartGeoCoordinates[] = StringCode.getStringCodesForStartGeoCoordinates(activityObjects1,
	// activityObjects2);
	// Pair<String, Double> levenshteinStartGeoCordinates =
	// getMySimpleLevenshteinDistance(stringCodesForStartGeoCoordinates[0], stringCodesForStartGeoCoordinates[1], 1, 1,
	// 2);
	// mapOfDistances.put("StartGeoCordinates", levenshteinStartGeoCordinates);
	// }
	//
	// // if (Constant.considerAllFeaturesForOTMDSAMEditDistance ||
	// Constant.considerEndGeoCoordinatesInOTMDSAMEditDistance)
	// {
	// String stringCodesForEndGeoCoordinates[] = StringCode.getStringCodesForEndGeoCoordinates(activityObjects1,
	// activityObjects2);
	// Pair<String, Double> levenshteinEndGeoCordinates =
	// getMySimpleLevenshteinDistance(stringCodesForEndGeoCoordinates[0], stringCodesForEndGeoCoordinates[1], 1, 1, 2);
	// mapOfDistances.put("EndGeoCordinates", levenshteinEndGeoCordinates);
	// }
	//
	// // if (Constant.considerAllFeaturesForOTMDSAMEditDistance || Constant.considerAvgAltitudeInOTMDSAMEditDistance)
	// {
	// // String stringCodeForAvgAltitude1 = StringCode.getStringCodeForAvgAltitudes(activityObjects1);
	// String stringCodesForAvgAltitude[] = StringCode.getStringCodesForAvgAltitudes(activityObjects1,
	// activityObjects2);
	// Pair<String, Double> levenshteinAvgAltitude = getMySimpleLevenshteinDistance(stringCodesForAvgAltitude[0],
	// stringCodesForAvgAltitude[1], 1, 1, 2);
	// mapOfDistances.put("AvgAltitude", levenshteinAvgAltitude);
	// }
	// }
	//
	// if (Constant.verboseSAX)
	// traverse(mapOfDistances);
	//
	// return mapOfDistances;
	// }
}
