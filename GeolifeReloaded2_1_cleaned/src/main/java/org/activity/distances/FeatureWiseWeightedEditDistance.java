package org.activity.distances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activity.constants.Constant;
import org.activity.constants.SanityConstants;
import org.activity.constants.VerbosityConstants;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.util.DateTimeUtils;
import org.activity.util.StringCode;

/**
 * It is a featurewise weighted edit distance. The edit operations are weighted. The edit distance is an aggregation of
 * weighted edit distances between the corresponding symbolic representation of the feature timelines.
 * 
 * @author gunjan
 *
 */
public class FeatureWiseWeightedEditDistance extends AlignmentBasedDistance
{
	public FeatureWiseWeightedEditDistance()
	{
		super(); // nothing used form super yet
	}

	/**
	 * 
	 * @param aos
	 * @param featureName
	 * @return
	 */
	public double[] getArrayOfFeatures(ArrayList<ActivityObject> aos, String featureName)
	{
		Object arr[] = new Object[aos.size()];
		double arrd[] = new double[aos.size()];
		switch (featureName)
		{
			case "StartTime":
				arr = aos.stream().map(ao -> DateTimeUtils.getTimeInDayInSeconds(ao.getStartTimestamp())).toArray();
				break;
			case "Duration":
				arr = aos.stream().map(ao -> ao.getDurationInSeconds()).toArray();
				break;
			case "DistanceTravelled":
				arr = aos.stream().map(ao -> ao.getDistanceTravelled()).toArray();
				break;
			case "AvgAltitude":
				arr = aos.stream().map(ao -> ao.getAvgAltitude()).toArray();
				break;
			default:
				System.err.println(
						"Error in org.activity.distances.FeatureWiseWeightedEditDistance.getArrayOfFeatures(ArrayList<ActivityObject>, String): unsuitable feature name: "
								+ featureName);
				break;
		}

		for (int i = 0; i < arr.length; i++)
		{
			arrd[i] = Double.valueOf(arr[i].toString());
		}
		if (SanityConstants.checkArrayOfFeatures)
		{
			System.out.println("Inside getArrayOfFeatures for feature:" + featureName + " \nActivity Objects are:");
			aos.stream().forEach(a -> System.out.print(a.toString() + " "));

			System.out.println("array of feature are: " + Arrays.toString(arrd));// //ArrayUtils.toPrimitive(arr)));
		}
		return arrd;// ArrayUtils.toPrimitive(arr);
	}

	/**
	 * 
	 * @param aos
	 * @param featureName
	 * @return
	 */
	public Pair<Double, Double>[] getArrayOfGeocoordinates(ArrayList<ActivityObject> aos, String featureName)
	{
		Object coordinates[] = new Object[aos.size()];
		Pair<Double, Double> coordinatesd[] = new Pair[aos.size()];
		switch (featureName)
		{
			case "StartGeoCoordinates":
				coordinates = aos.stream().map(ao -> new Pair<Double, Double>(Double.valueOf(ao.getStartLatitude()),
						Double.valueOf(ao.getStartLongitude()))).toArray();
				break;
			case "EndGeoCoordinates":
				coordinates = aos.stream().map(ao -> new Pair<Double, Double>(Double.valueOf(ao.getEndLatitude()),
						Double.valueOf(ao.getEndLongitude()))).toArray();
				break;
			default:
				System.err.println(
						"Error in org.activity.distances.FeatureWiseWeightedEditDistance.getArrayOfGeocordinates(ArrayList<ActivityObject>, String): unsuitable feature name: "
								+ featureName);
				break;
		}

		for (int i = 0; i < coordinates.length; i++)
		{
			coordinatesd[i] = (Pair) coordinates[i];// .toString());

		}
		return coordinatesd;
	}

	/**
	 * Return weighted edit distance for given feature where weight are based on raw values.
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @param featureName
	 * @return
	 */
	public Pair<String, Double> getWeightedEditDistanceRawValsWithTraceForFeature(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2, String featureName)
	{
		if (featureName.contains("Coordinates"))
		{
			System.err.println(
					"Error in org.activity.distances.FeatureWiseWeightedEditDistance.getWeightedEditDistanceRawValsWithTraceForFeature(ArrayList<ActivityObject>, ArrayList<ActivityObject>, String): called on unsuitabe feature:"
							+ featureName);
			new Exception(
					"Error in org.activity.distances.FeatureWiseWeightedEditDistance.getWeightedEditDistanceRawValsWithTraceForFeature(ArrayList<ActivityObject>, ArrayList<ActivityObject>, String): called on unsuitabe feature:"
							+ featureName);
			System.exit(-2);
		}

		double st1Vals[] = getArrayOfFeatures(activityObjects1, featureName);
		double st2Vals[] = getArrayOfFeatures(activityObjects2, featureName);

		String[] stringCodes = StringCode.getRelativeStringCodesForFeature(activityObjects1, activityObjects2,
				featureName);

		return getWeightedLevenshteinDistanceRawVals(stringCodes[0], stringCodes[1], 1, 1, 2, st1Vals, st2Vals);
	}

	/**
	 * Return weighted edit distance for given feature where weight are based on raw values.
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @param featureName
	 * @return
	 */
	public Pair<String, Double> getWeightedEditDistanceRawValsWithTraceForGeoCoordinates(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2, String featureName)
	{
		if (featureName.contains("Coordinate") == false)
		{
			System.err.println(
					"Error in org.activity.distances.FeatureWiseWeightedEditDistance.getWeightedEditDistanceRawValsWithTraceFor2dFeature(ArrayList<ActivityObject>, ArrayList<ActivityObject>, String): called on unsuitabe feature:"
							+ featureName);
			new Exception(
					"Error in org.activity.distances.FeatureWiseWeightedEditDistance.getWeightedEditDistanceRawValsWithTraceFor2dFeature(ArrayList<ActivityObject>, ArrayList<ActivityObject>, String): called on unsuitabe feature:"
							+ featureName);
			System.exit(-2);
		}
		Pair<Double, Double> st1Vals[] = getArrayOfGeocoordinates(activityObjects1, featureName);
		Pair<Double, Double> st2Vals[] = getArrayOfGeocoordinates(activityObjects2, featureName);

		String[] stringCodes = StringCode.getRelativeStringCodesForFeature(activityObjects1, activityObjects2,
				featureName);

		return getWeightedLevenshteinDistanceRawValsForGeoCoordinates(stringCodes[0], stringCodes[1], 1, 1, 2, st1Vals,
				st2Vals);
	}

	/**
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return ArrayList of Pairs where each Pair object contains the weighted edit distance for that feature and the
	 *         trace of operations
	 */
	public final LinkedHashMap<String, Pair<String, Double>> getFeatureWiseWeightedEditDistanceRawValsWithTrace(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original)
	{
		if (VerbosityConstants.verboseDistance)
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

		String stringCodeForActivityNames1 = StringCode.getStringCodeForActivityObjects(activityObjects1);
		String stringCodeForActivityNames2 = StringCode.getStringCodeForActivityObjects(activityObjects2);

		Pair<String, Double> levenshteinActivityNameTripe = getMySimpleLevenshteinDistancePair(
				stringCodeForActivityNames1, stringCodeForActivityNames2, 1, 1, 2);

		Pair<String, Double> levenshteinActivityName = new Pair<>(levenshteinActivityNameTripe.getFirst(),
				levenshteinActivityNameTripe.getSecond());

		mapOfDistances.put("ActivityName", levenshteinActivityName);

		mapOfDistances.put("StartTime",
				getWeightedEditDistanceRawValsWithTraceForFeature(activityObjects1, activityObjects2, "StartTime"));

		mapOfDistances.put("Duration",
				getWeightedEditDistanceRawValsWithTraceForFeature(activityObjects1, activityObjects2, "Duration"));

		if (Constant.getDatabaseName().equals("geolife1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			mapOfDistances.put("DistanceTravelled", getWeightedEditDistanceRawValsWithTraceForFeature(activityObjects1,
					activityObjects2, "DistanceTravelled"));

			mapOfDistances.put("StartGeoCoordinates", getWeightedEditDistanceRawValsWithTraceForGeoCoordinates(
					activityObjects1, activityObjects2, "StartGeoCoordinates"));

			mapOfDistances.put("EndGeoCoordinates", getWeightedEditDistanceRawValsWithTraceForGeoCoordinates(
					activityObjects1, activityObjects2, "EndGeoCoordinates"));

			mapOfDistances.put("AvgAltitude", getWeightedEditDistanceRawValsWithTraceForFeature(activityObjects1,
					activityObjects2, "AvgAltitude"));
		}

		if (VerbosityConstants.verboseSAX) traverse(mapOfDistances);

		return mapOfDistances;
	}

	public final void traverse(HashMap<String, Pair<String, Double>> pairs)// , ArrayList<ActivityObject>
																			// activityObjects1,
																			// ArrayList<ActivityObject>
																			// activityObjects2)

	{
		System.out.println("\nTraversing map of feature weighted distances SAX: ");
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
	public final LinkedHashMap<String, Pair<String, Double>> getFeatureWiseWeightedEditDistanceWithoutEndCurrentActivity(
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

		LinkedHashMap<String, Pair<String, Double>> result = getFeatureWiseWeightedEditDistanceRawValsWithTrace(
				activityObjects1, activityObjects2);// , userAtRecomm, dateAtRecomm, timeAtRecomm,
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
	public final LinkedHashMap<String, Pair<String, Double>> getFeatureWiseWeightedEditDistanceInvalidsExpunged(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2)// , String
																									// userAtRecomm,
	// String dateAtRecomm, String timeAtRecomm, Integer candidateTimelineId)
	{
		return getFeatureWiseWeightedEditDistanceRawValsWithTrace(expungeInvalids(activityObjects1),
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
	public final LinkedHashMap<String, Pair<String, Double>> getFeatureWiseWeightedEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2)
	{
		return getFeatureWiseWeightedEditDistanceWithoutEndCurrentActivity(expungeInvalids(activityObjects1),
				expungeInvalids(activityObjects2));
	}
	// /////////
}
