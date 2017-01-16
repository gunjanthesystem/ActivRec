package org.activity.distances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.ui.PopUps;
import org.activity.util.Constant;
import org.activity.util.StringCode;

/**
 * 
 * @author gunjan
 *
 */
public class FeatureWiseEditDistance extends AlignmentBasedDistance
{
	public FeatureWiseEditDistance()
	{
		super(); // nothing used form super yet
	}

	/**
	 * Returns the feature wise edit distance using ONLY SINGLE FEATURE. (which feature to use is set by parameters in
	 * Class Constant)
	 * 
	 * @param activityObjects1Original
	 * @param activityObjects2Original
	 * @return
	 */
	public Pair<String, Double> getFeatureWiseEditDistanceWithTraceSingleFeature(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original)
	{
		if (Constant.verboseDistance)
		{
			System.out.println("Debug note: calc (single) feature-wise editDist between "
					+ activityObjects1Original.size() + " & " + activityObjects2Original.size() + " objs");
		}

		if (Constant.considerAllFeaturesForFeatureWiseEditDistance)
		{
			System.err.println(
					"Error in org.activity.distances.FeatureWiseEditDistance.getFeatureWiseEditDistanceWithTraceSingleFeature(): Constant.considerAllFeaturesForFeatureWiseEditDistance is "
							+ Constant.considerAllFeaturesForFeatureWiseEditDistance);
			PopUps.showError(
					"Error in org.activity.distances.FeatureWiseEditDistance.getFeatureWiseEditDistanceWithTraceSingleFeature(): Constant.considerAllFeaturesForFeatureWiseEditDistance is "
							+ Constant.considerAllFeaturesForFeatureWiseEditDistance);
			return null;
		}

		LinkedHashMap<String, Pair<String, Double>> resultsFetched = getFeatureWiseEditDistanceWithTrace(
				activityObjects1Original, activityObjects2Original);

		if (resultsFetched.size() > 1)
		{
			String msg = "Error in org.activity.distances.FeatureWiseEditDistance.getFeatureWiseEditDistanceWithTraceSingleFeature(): More than one features are being used (check features to consider in Constant)";
			System.err.println(msg + "\n Traversing to see which features:\n");
			traverse(resultsFetched);
			PopUps.showError(msg);

			return null;
		}
		else
		{
			for (Map.Entry<String, Pair<String, Double>> entry : resultsFetched.entrySet())
			{
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return ArrayList of Pairs where each Pair object contains the levenshtein distance for that feature and the
	 *         trace of operations
	 */
	// getFeatureStringLevenshteinSAXWithTrace
	public final LinkedHashMap<String, Pair<String, Double>> getFeatureWiseEditDistanceWithTrace(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original)
	{
		if (Constant.verboseDistance)
		{
			System.out.println("Debug note: calc feature-wise editDist between " + activityObjects1Original.size()
					+ " & " + activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject> activityObjects1 = pruneFirstUnknown(activityObjects1Original);
		ArrayList<ActivityObject> activityObjects2 = pruneFirstUnknown(activityObjects2Original);

		/**
		 * <ActivityName,Pair<Trace,EditDistance>>
		 */
		LinkedHashMap<String, Pair<String, Double>> mapOfDistances = new LinkedHashMap<String, Pair<String, Double>>();

		if (Constant.considerAllFeaturesForFeatureWiseEditDistance
				|| Constant.considerActivityNameInFeatureWiseEditDistance)
		{
			String stringCodeForActivityNames1 = ActivityObject.getStringCodeForActivityObjects(activityObjects1);
			String stringCodeForActivityNames2 = ActivityObject.getStringCodeForActivityObjects(activityObjects2);
			Pair<String, Double> levenshteinActivityName = getMySimpleLevenshteinDistance(stringCodeForActivityNames1,
					stringCodeForActivityNames2, 1, 1, 2);
			mapOfDistances.put("ActivityName", levenshteinActivityName);
		}
		// String stringCodeForStartTime1 = StringCode.getStringCodeForStartTime(activityObjects1);
		// String stringCodeForStartTime2 = StringCode.getStringCodeForStartTime(activityObjects2);
		// Pair<String, Double> levenshteinStartTime = getMySimpleLevenshteinDistance(stringCodeForStartTime1,
		// stringCodeForStartTime2, 1, 1, 2);
		// mapOfDistances.put("StartTime", levenshteinStartTime);
		if (Constant.considerAllFeaturesForFeatureWiseEditDistance
				|| Constant.considerStartTimeInFeatureWiseEditDistance)
		{
			String stringCodeForStartTimes[] = StringCode.getStringCodesForStartTime(activityObjects1,
					activityObjects2);
			// String stringCodeForStartTime2 = StringCode.getStringCodeForStartTime(activityObjects2);
			Pair<String, Double> levenshteinStartTime = getMySimpleLevenshteinDistance(stringCodeForStartTimes[0],
					stringCodeForStartTimes[1], 1, 1, 2);
			mapOfDistances.put("StartTime", levenshteinStartTime);
		}

		if (Constant.considerAllFeaturesForFeatureWiseEditDistance
				|| Constant.considerDurationInFeatureWiseEditDistance)
		{
			String stringCodeForDurations[] = StringCode.getStringCodesForDuration(activityObjects1, activityObjects2);
			// String stringCodeForDuration2 = StringCode.getStringCodeForDuration(activityObjects2);
			Pair<String, Double> levenshteinDuration = getMySimpleLevenshteinDistance(stringCodeForDurations[0],
					stringCodeForDurations[1], 1, 1, 2);
			mapOfDistances.put("Duration", levenshteinDuration);
		}

		if (Constant.getDatabaseName().equals("geolife1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			if (Constant.considerAllFeaturesForFeatureWiseEditDistance
					|| Constant.considerDistanceTravelledInFeatureWiseEditDistance)
			{
				// String stringCodeForDistanceTravelled1 =
				// StringCode.getStringCodeForDistanceTravelled(activityObjects1);
				String stringCodesForDistanceTravelled[] = StringCode
						.getStringCodesForDistanceTravelled(activityObjects1, activityObjects2);
				Pair<String, Double> levenshteinDistanceTravelled = getMySimpleLevenshteinDistance(
						stringCodesForDistanceTravelled[0], stringCodesForDistanceTravelled[1], 1, 1, 2);
				mapOfDistances.put("DistanceTravelled", levenshteinDistanceTravelled);
			}
			// String stringCodeForStartGeo1 = StringCode.getStringCodeForStartGeo(activityObjects1);
			// String stringCodeForStartGeo2 = StringCode.getStringCodeForStartGeo(activityObjects2);
			// Pair<String, Double> levenshteinStartGeo = getMySimpleLevenshteinDistance(stringCodeForStartGeo1,
			// stringCodeForStartGeo2, 1, 1, 2);
			// String stringCodeForEndGeo1 = StringCode.getStringCodeForEndGeo(activityObjects1);
			// String stringCodeForEndGeo2 = StringCode.getStringCodeForEndGeo(activityObjects2);
			// Pair<String, Double> levenshteinEndGeo = getMySimpleLevenshteinDistance(stringCodeForEndGeo1,
			// stringCodeForEndGeo2, 1, 1, 2);

			if (Constant.considerAllFeaturesForFeatureWiseEditDistance
					|| Constant.considerStartGeoCoordinatesInFeatureWiseEditDistance)
			{
				String stringCodesForStartGeoCoordinates[] = StringCode
						.getStringCodesForStartGeoCoordinates(activityObjects1, activityObjects2);
				Pair<String, Double> levenshteinStartGeoCordinates = getMySimpleLevenshteinDistance(
						stringCodesForStartGeoCoordinates[0], stringCodesForStartGeoCoordinates[1], 1, 1, 2);
				mapOfDistances.put("StartGeoCordinates", levenshteinStartGeoCordinates);
			}

			if (Constant.considerAllFeaturesForFeatureWiseEditDistance
					|| Constant.considerEndGeoCoordinatesInFeatureWiseEditDistance)
			{
				String stringCodesForEndGeoCoordinates[] = StringCode
						.getStringCodesForEndGeoCoordinates(activityObjects1, activityObjects2);
				Pair<String, Double> levenshteinEndGeoCordinates = getMySimpleLevenshteinDistance(
						stringCodesForEndGeoCoordinates[0], stringCodesForEndGeoCoordinates[1], 1, 1, 2);
				mapOfDistances.put("EndGeoCordinates", levenshteinEndGeoCordinates);
			}

			if (Constant.considerAllFeaturesForFeatureWiseEditDistance
					|| Constant.considerAvgAltitudeInFeatureWiseEditDistance)
			{
				// String stringCodeForAvgAltitude1 = StringCode.getStringCodeForAvgAltitudes(activityObjects1);
				String stringCodesForAvgAltitude[] = StringCode.getStringCodesForAvgAltitudes(activityObjects1,
						activityObjects2);
				Pair<String, Double> levenshteinAvgAltitude = getMySimpleLevenshteinDistance(
						stringCodesForAvgAltitude[0], stringCodesForAvgAltitude[1], 1, 1, 2);
				mapOfDistances.put("AvgAltitude", levenshteinAvgAltitude);
			}
		}

		if (Constant.verboseSAX) traverse(mapOfDistances);

		return mapOfDistances;
	}

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

	// //////////////
	/**
	 * 
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final LinkedHashMap<String, Pair<String, Double>> getFeatureWiseEditDistanceWithoutEndCurrentActivity(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original)// ,
																													// String
																													// userAtRecomm,
																													// String
																													// dateAtRecomm,
																													// String
																													// timeAtRecomm,
																													// Integer
																													// candidateTimelineId)
	{
		ArrayList<ActivityObject> activityObjects1 = new ArrayList<ActivityObject>();
		activityObjects1.addAll(activityObjects1Original);

		ArrayList<ActivityObject> activityObjects2 = new ArrayList<ActivityObject>();
		activityObjects2.addAll(activityObjects2Original);

		activityObjects1.remove(activityObjects1.size() - 1);
		activityObjects2.remove(activityObjects2.size() - 1);

		LinkedHashMap<String, Pair<String, Double>> result = getFeatureWiseEditDistanceWithTrace(activityObjects1,
				activityObjects2);// , userAtRecomm, dateAtRecomm, timeAtRecomm,
									// candidateTimelineId);

		// $$WritingToFile.writeEditSimilarityCalculation(activityObjects1, activityObjects2, result.getSecond(),
		// result.getFirst()); //uncomment to write edit distance
		// calculations
		// $$WritingToFile.writeOnlyTrace(result.getFirst()); //uncomment to write trace to a file

		return result;
	}

	/**
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final LinkedHashMap<String, Pair<String, Double>> getFeatureWiseEditDistanceInvalidsExpunged(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2)// , String
																									// userAtRecomm,
	// String dateAtRecomm, String timeAtRecomm, Integer candidateTimelineId)
	{
		return getFeatureWiseEditDistanceWithTrace(expungeInvalids(activityObjects1),
				expungeInvalids(activityObjects2));// , userAtRecomm, dateAtRecomm, timeAtRecomm,
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
	public final LinkedHashMap<String, Pair<String, Double>> getFeatureWiseEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2)
	{
		return getFeatureWiseEditDistanceWithoutEndCurrentActivity(expungeInvalids(activityObjects1),
				expungeInvalids(activityObjects2));
	}
	// /////////
}
