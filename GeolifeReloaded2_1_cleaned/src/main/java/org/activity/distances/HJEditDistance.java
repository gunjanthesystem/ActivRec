package org.activity.distances;

import java.util.ArrayList;

import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.util.Constant;
import org.activity.util.RegexUtils;
import org.activity.util.StatsUtils;

/**
 * Note: this has been modified for Geolife data set to account for the additional attributes from geolocation
 * 
 * IT IS ADVISABLE NOT TO CREATE ANY STATIC METHOD IN THIS CLASS
 * 
 * @author gunjan
 * 
 */
public class HJEditDistance extends AlignmentBasedDistance
{
	/**
	 * Sets the tolerance according the truth value of Constant.useTolerance
	 */
	public HJEditDistance()
	{
		super();
	}

	// //////////////
	/**
	 * Finds the HJ Edit similarity between two given lists of ActivityObjects excluding the end point current activity
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getHJEditDistanceWithoutEndCurrentActivity(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, Long candidateTimelineId)
	{
		ArrayList<ActivityObject> activityObjects1 = new ArrayList<ActivityObject>();
		activityObjects1.addAll(activityObjects1Original);

		ArrayList<ActivityObject> activityObjects2 = new ArrayList<ActivityObject>();
		activityObjects2.addAll(activityObjects2Original);

		Pair<String, Double> result;
		if (activityObjects1.size() - 1 == 0 && activityObjects2.size() - 1 == 0)
		{
			result = new Pair<String, Double>("", new Double(0));
		}

		else if (activityObjects1.size() - 1 == 0 && activityObjects2.size() - 1 != 0)
		{
			result = new Pair<String, Double>("", new Double(activityObjects2.size() - 1));
		}

		else if (activityObjects1.size() - 1 != 0 && activityObjects2.size() - 1 == 0)
		{
			result = new Pair<String, Double>("", new Double(activityObjects1.size() - 1));
		}

		else
		{
			activityObjects1.remove(activityObjects1.size() - 1);
			activityObjects2.remove(activityObjects2.size() - 1);

			result = getHJEditDistanceWithTrace(activityObjects1, activityObjects2, userAtRecomm, dateAtRecomm,
					timeAtRecomm, candidateTimelineId);
		}

		// $$WritingToFile.writeEditSimilarityCalculation(activityObjects1, activityObjects2, result.getSecond(),
		// result.getFirst()); //uncomment to write edit distance
		// calculations
		// $$WritingToFile.writeOnlyTrace(result.getFirst()); //uncomment to write trace to a file

		return result;
	}

	/**
	 * Finds the Edit similarity between two given lists of ActivityObjects ignoring the invalid ActivityObjects
	 * 
	 * Version Oct 9
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getHJEditDistanceInvalidsExpunged(ArrayList<ActivityObject> activityObjects1,
			ArrayList<ActivityObject> activityObjects2, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			Long candidateTimelineId)
	{
		return getHJEditDistanceWithTrace(expungeInvalids(activityObjects1), expungeInvalids(activityObjects2),
				userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);// similarity;
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
	public final Pair<String, Double> getHJEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm, Long candidateTimelineId)
	{
		return getHJEditDistanceWithoutEndCurrentActivity(expungeInvalids(activityObjects1),
				expungeInvalids(activityObjects2), userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);// similarity;
	}

	/**
	 * Calculate the Edit Distance as per HJ's specification. First calculating simple levenshtein distance and then
	 * calculating the cost from the trace of operations performed using the assigned costs and wts of objects and then
	 * adding the distance at feature level. So, while optimisation of distance, the optimisation is done using 112
	 * costs while the resultant cost is calculated after the operations have been thus established... using the
	 * assigned costs for Activity Objects and features. (dAct and dFeat rounded off to 4 decimal places)
	 * 
	 * @param activityObjects1Original
	 *            sequence of activity objects to be compared (usually from a candidate timeline).
	 * @param activityObjects2Original
	 *            activitiesGuidingRecommendation, i.e, sequence of activity objects forming current timeline
	 * 
	 * @param userAtRecomm
	 *            only used for writing to file
	 * @param dateAtRecomm
	 *            only used for writing to file
	 * @param timeAtRecomm
	 *            only used for writing to file
	 * @param candidateTimelineId
	 * @return Pair<Trace as String, Edit Distance> ///we can also do n Pair<Trace as String, Pair <total Edit Distance,
	 *         act level edit distance> /
	 */
	public final Pair<String, Double> getHJEditDistanceWithTrace(ArrayList<ActivityObject> activityObjects1Original,
			ArrayList<ActivityObject> activityObjects2Original, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, long candidateTimelineId)
	{
		if (Constant.verboseDistance)
		{
			System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject> activityObjects1 = pruneFirstUnknown(activityObjects1Original);
		ArrayList<ActivityObject> activityObjects2 = pruneFirstUnknown(activityObjects2Original);

		double dAct = 0, dFeat = 0, distanceTotal = 0;
		String stringCodeForActivityObjects1 = ActivityObject.getStringCodeForActivityObjects(activityObjects1);
		String stringCodeForActivityObjects2 = ActivityObject.getStringCodeForActivityObjects(activityObjects2);

		Pair<String, Double> levenshteinDistance = getMySimpleLevenshteinDistance(stringCodeForActivityObjects1,
				stringCodeForActivityObjects2, 1, 1, 2);

		String[] splitted = RegexUtils.patternUnderScore.split(levenshteinDistance.getFirst());
		// $$ levenshteinDistance.getFirst().split("_");
		// "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(6-2)";

		if (Constant.verboseDistance)
		{
			System.out.println("Trace =" + levenshteinDistance.getFirst() + "  simpleLevenshteinDistance112="
					+ levenshteinDistance.getSecond());
		}

		for (int i = 1; i < splitted.length; i++)
		{
			String op = splitted[i]; // D(1-0)
			String[] splitOps = RegexUtils.patternOpeningRoundBrace.split(op);// $$op.split("\\("); // D and 1-0)

			// System.out.println(splitted[i]); //D(1-0)

			String operation = splitOps[0]; // D

			String splitCo[] = RegexUtils.patternHyphen.split(splitOps[1]);
			// $$splitOps[1].split("-"); // 1 and 0)
			String splitCoAgain[] = RegexUtils.patternClosingRoundBrace.split(splitCo[1]);
			// $$splitCo[1].split("\\)"); // 0 and nothing

			int coordOfAO1 = Integer.parseInt(splitCo[0]) - 1;// 1 minus 1

			int coordOfAO2 = Integer.parseInt(splitCoAgain[0]) - 1; // 0 minus 1

			// int coordOfAO1= Character.getNumericValue(splitOps[1].charAt(0))-1;//1
			// int coordOfAO2=Character.getNumericValue(splitOps[1].charAt(2))-1;//0
			// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);

			if (operation.equals("D"))
			{
				dAct += costDeleteActivityObject; // 1d*costReplaceFullActivityObject;
			}

			else if (operation.equals("I"))
			{
				dAct += costInsertActivityObject; // 1d*costReplaceFullActivityObject;
			}

			else if (operation.equals("S"))
			{
				dAct += costReplaceActivityObject; // 2d*costReplaceFullActivityObject;
			}

			else if (operation.equals("N"))
			{
				// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
				dFeat += getFeatureLevelDistance(activityObjects1.get(coordOfAO1), activityObjects2.get(coordOfAO2));
			}
		}

		dAct = StatsUtils.round(dAct, 4);
		dFeat = StatsUtils.round(dFeat, 4);

		if (Constant.verboseDistance)
		{
			System.out.println("HJ dist=" + dAct + " + " + dFeat);
		}

		distanceTotal = dAct + dFeat;

		if (Constant.WriteEditSimilarityCalculations)
		{
			// System.out.println("passing Activity Objects of sizes: " + activityObjects1.size() + " " +
			// activityObjects2.size());

			WritingToFile.writeEditSimilarityCalculations(activityObjects1, activityObjects2, distanceTotal,
					levenshteinDistance.getFirst(), dAct, dFeat, userAtRecomm, dateAtRecomm, timeAtRecomm,
					candidateTimelineId);
		}
		// $ WritingToFile.writeOnlyTrace(levenshteinDistance.getFirst());

		// WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
		// WritingToFile.writeEditDistance(levenshteinDistance);
		return new Pair<String, Double>(levenshteinDistance.getFirst(), distanceTotal);
	}

	// public static final Pair<String, Double> getHJEditDistanceWithTrace(ArrayList<ActivityObject>
	// activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original,
	// String userAtRecomm, String dateAtRecomm, String timeAtRecomm, Integer candidateTimelineId)
	// {
	// if (Constant.verboseDistance)
	// {
	// System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & " +
	// activityObjects2Original.size() + " objs");
	// }
	//
	// ArrayList<ActivityObject> activityObjects1 = pruneFirstUnknown(activityObjects1Original);
	// ArrayList<ActivityObject> activityObjects2 = pruneFirstUnknown(activityObjects2Original);
	//
	// double dAct = 0, dFeat = 0, distanceTotal = 0;
	// String stringCodeForActivityObjects1 = ActivityObject.getStringCodeForActivityObjects(activityObjects1);
	// String stringCodeForActivityObjects2 = ActivityObject.getStringCodeForActivityObjects(activityObjects2);
	//
	// Pair<String, Double> levenshteinDistance = new
	// AlignmentBasedDistance().getMySimpleLevenshteinDistance(stringCodeForActivityObjects1,
	// stringCodeForActivityObjects2, 1, 1,
	// 2);
	//
	// String[] splitted = levenshteinDistance.getFirst().split("_"); // "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(6-2)";
	//
	// if (Constant.verboseDistance)
	// {
	// System.out.println("Trace =" + levenshteinDistance.getFirst() + " simpleLevenshteinDistance112=" +
	// levenshteinDistance.getSecond());
	// }
	//
	// for (int i = 1; i < splitted.length; i++)
	// {
	// String op = splitted[i]; // D(1-0)
	// String[] splitOps = op.split("\\("); // D and 1-0)
	//
	// // System.out.println(splitted[i]); //D(1-0)
	//
	// String operation = splitOps[0]; // D
	//
	// String splitCo[] = splitOps[1].split("-"); // 1 and 0)
	// String splitCoAgain[] = splitCo[1].split("\\)"); // 0 and nothing
	//
	// int coordOfAO1 = Integer.parseInt(splitCo[0]) - 1;// 1 minus 1
	//
	// int coordOfAO2 = Integer.parseInt(splitCoAgain[0]) - 1; // 0 minus 1
	//
	// // int coordOfAO1= Character.getNumericValue(splitOps[1].charAt(0))-1;//1
	// // int coordOfAO2=Character.getNumericValue(splitOps[1].charAt(2))-1;//0
	// // System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
	//
	// if (operation.equals("D"))
	// {
	// dAct += costDeleteActivityObject; // 1d*costReplaceFullActivityObject;
	// }
	//
	// else if (operation.equals("I"))
	// {
	// dAct += costInsertActivityObject; // 1d*costReplaceFullActivityObject;
	// }
	//
	// else if (operation.equals("S"))
	// {
	// dAct += costReplaceActivityObject; // 2d*costReplaceFullActivityObject;
	// }
	//
	// else if (operation.equals("N"))
	// {
	// // System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
	// dFeat += getFeatureLevelDistance(activityObjects1.get(coordOfAO1), activityObjects2.get(coordOfAO2));
	// }
	// }
	//
	// dAct = UtilityBelt.round(dAct, 4);
	// dFeat = UtilityBelt.round(dFeat, 4);
	//
	// if (Constant.verboseDistance)
	// {
	// System.out.println("HJ dist=" + dAct + " + " + dFeat);
	// }
	//
	// distanceTotal = dAct + dFeat;
	//
	// if (Constant.WriteEditSimilarityCalculations)
	// {
	// // System.out.println("passing Activity Objects of sizes: " + activityObjects1.size() + " " +
	// activityObjects2.size());
	//
	// WritingToFile.writeEditSimilarityCalculations(activityObjects1, activityObjects2, distanceTotal,
	// levenshteinDistance.getFirst(), dAct, dFeat, userAtRecomm, dateAtRecomm,
	// timeAtRecomm, candidateTimelineId);
	// }
	// // $ WritingToFile.writeOnlyTrace(levenshteinDistance.getFirst());
	//
	// // WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
	// // WritingToFile.writeEditDistance(levenshteinDistance);
	// return new Pair<String, Double>(levenshteinDistance.getFirst(), distanceTotal);
	// }
}
