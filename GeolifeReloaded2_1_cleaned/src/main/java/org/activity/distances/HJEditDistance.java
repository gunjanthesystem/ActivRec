package org.activity.distances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.activity.constants.Constant;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.RegexUtils;
import org.activity.util.StringCode;

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
	double EDAlpha;
	PrimaryDimension primaryDimension;
	boolean needsToPruneFirstUnknown;

	/**
	 * Sets the tolerance according the truth value of Constant.useTolerance
	 */
	public HJEditDistance()
	{
		super();
		this.EDAlpha = -99999;
		primaryDimension = Constant.primaryDimension;
		needsToPruneFirstUnknown = Constant.needsToPruneFirstUnknown;
	}

	public HJEditDistance(double edAlpha)
	{
		super();
		this.EDAlpha = edAlpha;
		System.out.println("Setting EDAlpha=" + this.EDAlpha);
		primaryDimension = Constant.primaryDimension;
		needsToPruneFirstUnknown = Constant.needsToPruneFirstUnknown;
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
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
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
			String candidateTimelineId)
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
			String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
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
	 * <p>
	 * Note: each activity object can have multiple primary dimension vals in case they are resultant of mergers.
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
	public final Pair<String, Double> getHJEditDistanceWithTraceUntil13July2017(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
	{
		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject> activityObjects1 = pruneFirstUnknown(activityObjects1Original);
		ArrayList<ActivityObject> activityObjects2 = pruneFirstUnknown(activityObjects2Original);

		double dAct = 0, dFeat = 0, distanceTotal = 0;

		// multiple string codes when an AO in the list has act name which at desired level can have multiple ids. For
		// example Vineyards is under Community as well as Food
		ArrayList<String> stringCodesForActivityObjects1, stringCodesForActivityObjects2;

		if (Constant.HierarchicalCatIDLevelForEditDistance > 0)
		{
			stringCodesForActivityObjects1 = StringCode.getStringCodeForActivityObjectsV2(activityObjects1,
					Constant.HierarchicalCatIDLevelForEditDistance, false);
			stringCodesForActivityObjects2 = StringCode.getStringCodeForActivityObjectsV2(activityObjects2,
					Constant.HierarchicalCatIDLevelForEditDistance, false);
		}
		else
		{
			stringCodesForActivityObjects1 = (ArrayList<String>) Collections
					.singletonList(StringCode.getStringCodeForActivityObjects(activityObjects1));
			stringCodesForActivityObjects2 = (ArrayList<String>) Collections
					.singletonList(StringCode.getStringCodeForActivityObjects(activityObjects2));
		}

		Pair<String, Double> levenshteinDistance = null;
		long t1 = System.nanoTime();

		levenshteinDistance = getLowestMySimpleLevenshteinDistancePair(stringCodesForActivityObjects1,
				stringCodesForActivityObjects2, 1, 1, 2);// getMySimpleLevenshteinDistance

		// { levenshteinDistance = ProcessUtils.executeProcessEditDistance(stringCodeForActivityObjects1,
		// stringCodeForActivityObjects2, Integer.toString(1), Integer.toString(1), Integer.toString(2));
		// System.out.println("getMySimpleLevenshteinProcesse took " + (System.nanoTime() - t1) + " ns");}

		String[] splitted = RegexUtils.patternUnderScore.split(levenshteinDistance.getFirst());
		// $$ levenshteinDistance.getFirst().split("_");// "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(6-2)";

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("Trace =" + levenshteinDistance.getFirst() + "  simpleLevenshteinDistance112="
					+ levenshteinDistance.getSecond());
			System.out.println("getMySimpleLevenshteinDistance took " + (System.nanoTime() - t1) + " ns");
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

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("HJ dist=" + dAct + " + " + dFeat);
		}

		distanceTotal = dAct + dFeat;

		if (VerbosityConstants.WriteEditSimilarityCalculations)
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
	public final Pair<String, Double> getHJEditDistanceWithTraceBackup13Jul2017(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
	{
		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject> activityObjects1 = pruneFirstUnknown(activityObjects1Original);
		ArrayList<ActivityObject> activityObjects2 = pruneFirstUnknown(activityObjects2Original);

		double dAct = 0, dFeat = 0, distanceTotal = 0;

		// multiple string codes when an AO in the list has act name which at desired level can have multiple ids. For
		// example Vineyards is under Community as well as Food
		ArrayList<String> stringCodesForActivityObjects1, stringCodesForActivityObjects2;

		if (Constant.HierarchicalCatIDLevelForEditDistance > 0)
		{
			stringCodesForActivityObjects1 = StringCode.getStringCodeForActivityObjectsV2(activityObjects1,
					Constant.HierarchicalCatIDLevelForEditDistance, false);
			stringCodesForActivityObjects2 = StringCode.getStringCodeForActivityObjectsV2(activityObjects2,
					Constant.HierarchicalCatIDLevelForEditDistance, false);
		}
		else
		{
			stringCodesForActivityObjects1 = (ArrayList<String>) Collections
					.singletonList(StringCode.getStringCodeForActivityObjects(activityObjects1));
			stringCodesForActivityObjects2 = (ArrayList<String>) Collections
					.singletonList(StringCode.getStringCodeForActivityObjects(activityObjects2));
		}

		Pair<String, Double> levenshteinDistance = null;
		long t1 = System.nanoTime();

		levenshteinDistance = getLowestMySimpleLevenshteinDistancePair(stringCodesForActivityObjects1,
				stringCodesForActivityObjects2, 1, 1, 2);// getMySimpleLevenshteinDistance

		// { levenshteinDistance = ProcessUtils.executeProcessEditDistance(stringCodeForActivityObjects1,
		// stringCodeForActivityObjects2, Integer.toString(1), Integer.toString(1), Integer.toString(2));
		// System.out.println("getMySimpleLevenshteinProcesse took " + (System.nanoTime() - t1) + " ns");}

		String[] splitted = RegexUtils.patternUnderScore.split(levenshteinDistance.getFirst());
		// $$ levenshteinDistance.getFirst().split("_");// "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(6-2)";

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("Trace =" + levenshteinDistance.getFirst() + "  simpleLevenshteinDistance112="
					+ levenshteinDistance.getSecond());
			System.out.println("getMySimpleLevenshteinDistance took " + (System.nanoTime() - t1) + " ns");
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

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("HJ dist=" + dAct + " + " + dFeat);
		}

		distanceTotal = dAct + dFeat;

		if (VerbosityConstants.WriteEditSimilarityCalculations)
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

	/**
	 * Calculate the Edit Distance as per HJ's specification. First calculating simple levenshtein distance and then
	 * calculating the cost from the trace of operations performed using the assigned costs and wts of objects and then
	 * adding the distance at feature level. So, while optimisation of distance, the optimisation is done using 112
	 * costs while the resultant cost is calculated after the operations have been thus established... using the
	 * assigned costs for Activity Objects and features. (dAct and dFeat rounded off to 4 decimal places)
	 * <p>
	 * Note: each activity object can have multiple primary dimension vals in case they are resultant of mergers.
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
	 * @param primaryDimension
	 * @return Pair<Trace as String, Edit Distance> ///we can also do n Pair<Trace as String, Pair <total Edit Distance,
	 *         act level edit distance> /
	 * @since 14 July 2017
	 */
	public final Pair<String, Double> getHJEditDistanceWithTraceBefore1Mar2018(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
	{
		// PrimaryDimension primaryDimension = Constant.primaryDimension;//moved to constructor
		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject> activityObjects1 = activityObjects1Original;
		ArrayList<ActivityObject> activityObjects2 = activityObjects2Original;

		if (needsToPruneFirstUnknown)
		{
			activityObjects1 = pruneFirstUnknown(activityObjects1Original);
			activityObjects2 = pruneFirstUnknown(activityObjects2Original);
		}

		double dAct = 0, dFeat = 0, distanceTotal = 0;

		HashMap<Integer, Character> uniqueCharCodes = StringCode.getLocallyUniqueCharCodeMap(activityObjects1,
				activityObjects2, primaryDimension);

		// multiple string codes when an AO in the list has act name which at desired level can have multiple ids. For
		// example Vineyards is under Community as well as Food
		ArrayList<String> stringCodesForActivityObjects1, stringCodesForActivityObjects2;

		// //start of curtain 17 July 2017
		// if (Constant.HierarchicalCatIDLevelForEditDistance > 0)
		// {// TODO: need to implement this for multi dimensional case, e.g., recommending location
		// // PopUps.printTracedErrorMsgWithExit("Constant.HierarchicalLevelForEditDistance > 0) not implemented yet");
		// stringCodesForActivityObjects1 = StringCode.getStringCodeForActivityObjectsV2(activityObjects1,
		// Constant.HierarchicalCatIDLevelForEditDistance, false);
		// stringCodesForActivityObjects2 = StringCode.getStringCodeForActivityObjectsV2(activityObjects2,
		// Constant.HierarchicalCatIDLevelForEditDistance, false);
		// }
		// else
		// {
		// //end of curtain 17 July 2017
		stringCodesForActivityObjects1 = StringCode.getStringCodesForActivityObjects(activityObjects1, primaryDimension,
				uniqueCharCodes, VerbosityConstants.verbose);
		stringCodesForActivityObjects2 = StringCode.getStringCodesForActivityObjects(activityObjects2, primaryDimension,
				uniqueCharCodes, VerbosityConstants.verbose);
		// }

		Pair<String, Double> levenshteinDistance = null;
		long t1 = System.nanoTime();

		levenshteinDistance =

				getLowestMySimpleLevenshteinDistancePair(stringCodesForActivityObjects1, stringCodesForActivityObjects2,
						1, 1, 2);// getMySimpleLevenshteinDistance

		// { levenshteinDistance = ProcessUtils.executeProcessEditDistance(stringCodeForActivityObjects1,
		// stringCodeForActivityObjects2, Integer.toString(1), Integer.toString(1), Integer.toString(2));
		// System.out.println("getMySimpleLevenshteinProcesse took " + (System.nanoTime() - t1) + " ns");}

		String[] splitted = RegexUtils.patternUnderScore.split(levenshteinDistance.getFirst());
		// $$ levenshteinDistance.getFirst().split("_");// "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(6-2)";

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("Trace =" + levenshteinDistance.getFirst() + "  simpleLevenshteinDistance112="
					+ levenshteinDistance.getSecond());
			System.out.println("getMySimpleLevenshteinDistance took " + (System.nanoTime() - t1) + " ns");
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
				double decayWt = 1;

				if (Constant.useDecayInFeatureLevelED && (i == (splitted.length - 1)))
				{
					decayWt = 3;
				}
				// System.out.println("Decay wt=" + decayWt);
				// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
				dFeat += (decayWt
						* getFeatureLevelDistance(activityObjects1.get(coordOfAO1), activityObjects2.get(coordOfAO2)));
			}
		}

		if (!Constant.disableRoundingEDCompute)

		{
			dAct = StatsUtils.round(dAct, 4);
			dFeat = StatsUtils.round(dFeat, 4);
		}

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("HJ dist=" + dAct + " + " + dFeat);
		}

		// Start of disabled on Feb 4 2018
		// distanceTotal = dAct + dFeat;
		// End of disabled on Feb 4 2018

		// Start of added on Feb 4 2018
		// double EDAlpha = 0.5;
		if (this.EDAlpha > 0)
		{
			distanceTotal = /* dAct + dFeat; */
					combineActAndFeatLevelDistance(dAct, dFeat, activityObjects1.size(), activityObjects2.size(),
							EDAlpha);
		}
		else
		{
			distanceTotal = dAct + dFeat;
		}
		// System.out.println("EDAlpha = " + EDAlpha);
		// Start of sanity check Feb 9
		// End of sanity check Feb 9

		if (Constant.checkEDSanity)
		{
			if (dFeat > 100)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("\ncalc HJeditDist between " + activityObjects1.size() + " & " + activityObjects2.size()
						+ " objs\nAOs1:");
				activityObjects1.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
				sb.append("\nAOs2:");
				activityObjects2.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));

				sb.append("\nTrace =" + levenshteinDistance.getFirst() + "  simpleLevenshteinDistance112="
						+ levenshteinDistance.getSecond());
				PopUps.printTracedErrorMsg("\nError:Feb5_1Bug: HJ dist=" + dAct + " + " + dFeat + "\n" + sb.toString());
				WritingToFile.appendLineToFileAbsolute(sb.toString(),
						Constant.getOutputCoreResultsPath() + "FeatEDInvestigationCountAllAnomaly.txt");

			}
			if (distanceTotal < 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("\ncalc HJeditDist between " + activityObjects1.size() + " & " + activityObjects2.size()
						+ " objs\nAOs1:");
				activityObjects1.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
				sb.append("\nAOs2:");
				activityObjects2.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));

				sb.append("\nTrace =" + levenshteinDistance.getFirst() + "  simpleLevenshteinDistance112="
						+ levenshteinDistance.getSecond());
				PopUps.printTracedErrorMsg("\nError: distanceTotal=" + distanceTotal + " \nHJ dist=" + dAct + " + "
						+ dFeat + "\n" + sb.toString());
				WritingToFile.appendLineToFileAbsolute(sb.toString(),
						Constant.getOutputCoreResultsPath() + "CombinedEDInvestigationCountAllAnomaly.txt");

			}

		}
		// End of added on Feb 4 2018

		if (VerbosityConstants.WriteEditSimilarityCalculations)
		{
			// System.out.println("passing Activity Objects of sizes: " + activityObjects1.size() + " " +
			// activityObjects2.size());
			WritingToFile.writeEditSimilarityCalculations(activityObjects1, activityObjects2, distanceTotal,
					levenshteinDistance.getFirst(), dAct, dFeat, userAtRecomm, dateAtRecomm, timeAtRecomm,
					candidateTimelineId);
		}

		if (Constant.debugFeb24_2018)
		{
			WritingToFile.appendLineToFileAbsolute(
					"\t" + userAtRecomm + "\t" + dateAtRecomm + "\t" + timeAtRecomm + distanceTotal + "\t"
							+ levenshteinDistance.getFirst() + "\t" + dAct + "\t" + dFeat + "\n",
					Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		}

		// $ WritingToFile.writeOnlyTrace(levenshteinDistance.getFirst());

		// WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
		// WritingToFile.writeEditDistance(levenshteinDistance);
		return new Pair<String, Double>(levenshteinDistance.getFirst(), distanceTotal);
	}

	/**
	 * Fork of getHJEditDistanceWithTraceBefore1Mar2018()
	 * <p>
	 * Calculate the Edit Distance as per HJ's specification. First calculating simple levenshtein distance and then
	 * calculating the cost from the trace of operations performed using the assigned costs and wts of objects and then
	 * adding the distance at feature level. So, while optimisation of distance, the optimisation is done using 112
	 * costs while the resultant cost is calculated after the operations have been thus established... using the
	 * assigned costs for Activity Objects and features. (dAct and dFeat rounded off to 4 decimal places)
	 * <p>
	 * Note: each activity object can have multiple primary dimension vals in case they are resultant of mergers.
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
	 * @param primaryDimension
	 * @return Pair<Trace as String, Edit Distance> ///we can also do n Pair<Trace as String, Pair <total Edit Distance,
	 *         act level edit distance> /
	 * @since Mar 1 2018
	 */
	public final Pair<String, Double> getHJEditDistanceWithTrace(ArrayList<ActivityObject> activityObjects1Original,
			ArrayList<ActivityObject> activityObjects2Original, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateTimelineId)
	{
		// PrimaryDimension primaryDimension = Constant.primaryDimension;//moved to constructor
		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject> activityObjects1 = activityObjects1Original;
		ArrayList<ActivityObject> activityObjects2 = activityObjects2Original;

		if (needsToPruneFirstUnknown)
		{
			activityObjects1 = pruneFirstUnknown(activityObjects1Original);
			activityObjects2 = pruneFirstUnknown(activityObjects2Original);
		}

		double dAct = 0, dFeat = 0, distanceTotal = 0;

		HashMap<Integer, Character> uniqueCharCodes = StringCode.getLocallyUniqueCharCodeMap(activityObjects1,
				activityObjects2, primaryDimension);

		// multiple string codes when an AO in the list has act name which at desired level can have multiple ids. For
		// example Vineyards is under Community as well as Food
		ArrayList<String> stringCodesForActivityObjects1, stringCodesForActivityObjects2;

		// //start of curtain 17 July 2017
		// if (Constant.HierarchicalCatIDLevelForEditDistance > 0)
		// {// TODO: need to implement this for multi dimensional case, e.g., recommending location
		// // PopUps.printTracedErrorMsgWithExit("Constant.HierarchicalLevelForEditDistance > 0) not implemented yet");
		// stringCodesForActivityObjects1 = StringCode.getStringCodeForActivityObjectsV2(activityObjects1,
		// Constant.HierarchicalCatIDLevelForEditDistance, false);
		// stringCodesForActivityObjects2 = StringCode.getStringCodeForActivityObjectsV2(activityObjects2,
		// Constant.HierarchicalCatIDLevelForEditDistance, false);
		// }
		// else
		// {
		// //end of curtain 17 July 2017
		stringCodesForActivityObjects1 = StringCode.getStringCodesForActivityObjects(activityObjects1, primaryDimension,
				uniqueCharCodes, VerbosityConstants.verbose);
		stringCodesForActivityObjects2 = StringCode.getStringCodesForActivityObjects(activityObjects2, primaryDimension,
				uniqueCharCodes, VerbosityConstants.verbose);
		// }

		Triple<String, Double, Triple<char[], int[], int[]>> levenshteinDistance = null;
		long t1 = System.nanoTime();

		levenshteinDistance = getLowestMySimpleLevenshteinDistance(stringCodesForActivityObjects1,
				stringCodesForActivityObjects2, 1, 1, 2);// getMySimpleLevenshteinDistance

		if (false)// sanity checking new getLowestMySimpleLevenshteinDistance and getMySimpleLevenshteinDistance()
		{
			// Triple<String, Double, char[]> newLevenshteinDistance = getLowestMySimpleLevenshteinDistance(
			// stringCodesForActivityObjects1, stringCodesForActivityObjects2, 1, 1, 2);//
			// getMySimpleLevenshteinDistance
			StringBuilder sbTemp1 = new StringBuilder();
			// sbTemp1.append("Debug 1Mar2018:
			// newLevenshteinDistance.getFirst().equals(levenshteinDistance.getFirst())="
			// + levenshteinDistance.getFirst().equals(levenshteinDistance.getFirst())
			// + "\nnewLevenshteinDistance.getSecond().equals(levenshteinDistance.getSecond())="
			// + newLevenshteinDistance.getSecond().equals(levenshteinDistance.getSecond()) + "\n");
			sbTemp1.append("levenshteinDistance=" + levenshteinDistance.toString() + "\n");
			sbTemp1.append("first=" + levenshteinDistance.getFirst().toString() + " second="
					+ levenshteinDistance.getSecond().toString() + " third_first="
					+ new String(levenshteinDistance.getThird().getFirst()));

			sbTemp1.append("\n third_second=" + Arrays.toString(levenshteinDistance.getThird().getSecond()));
			sbTemp1.append("\n third_second.len=" + (levenshteinDistance.getThird().getSecond().length));
			sbTemp1.append("\n third_third=" + Arrays.toString(levenshteinDistance.getThird().getThird()));
			sbTemp1.append("\n third_third.len=" + (levenshteinDistance.getThird().getThird().length));
			sbTemp1.append("\n---\n");
			WritingToFile.appendLineToFileAbsolute(sbTemp1.toString(),
					Constant.getCommonPath() + "Debug1Mar2018newLevenshteinDistance.csv");
		}
		// { levenshteinDistance = ProcessUtils.executeProcessEditDistance(stringCodeForActivityObjects1,
		// stringCodeForActivityObjects2, Integer.toString(1), Integer.toString(1), Integer.toString(2));
		// System.out.println("getMySimpleLevenshteinProcesse took " + (System.nanoTime() - t1) + " ns");}
		// String[] splitted = RegexUtils.patternUnderScore.split(levenshteinDistance.getFirst());
		// $$ levenshteinDistance.getFirst().split("_");// "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(6-2)";

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("Trace =" + levenshteinDistance.getFirst() + " DINSTrace="
					+ new String(levenshteinDistance.getThird().getFirst()) + "\n third_second="
					+ Arrays.toString(levenshteinDistance.getThird().getSecond()) + "\n third_third="
					+ Arrays.toString(levenshteinDistance.getThird().getThird()) + "  simpleLevenshteinDistance112="
					+ levenshteinDistance.getSecond());
			System.out.println("getMySimpleLevenshteinDistance took " + (System.nanoTime() - t1) + " ns");
		}

		char[] DINSTrace = levenshteinDistance.getThird().getFirst();
		int[] coord1Trace = levenshteinDistance.getThird().getSecond();
		int[] coord2Trace = levenshteinDistance.getThird().getThird();
		for (int i = 0; i < DINSTrace.length; i++)
		{
			char operationChar = DINSTrace[i];
			// String op = splitted[i]; // D(1-0)
			// String[] splitOps = RegexUtils.patternOpeningRoundBrace.split(op);// $$op.split("\\("); // D and 1-0)
			// System.out.println(splitted[i]); //D(1-0)
			// String operation = splitOps[0]; // D//Sanity checked ok for String -> char on Mar 1 2018
			// char operationChar = splitOps[0].charAt(0); // D
			// $$System.out.println("operation=" + operation + " operationCHar=" + operationChar);
			// String splitCo[] = RegexUtils.patternHyphen.split(splitOps[1]);
			// $$splitOps[1].split("-"); // 1 and 0)
			// String splitCoAgain[] = RegexUtils.patternClosingRoundBrace.split(splitCo[1]);
			// $$splitCo[1].split("\\)"); // 0 and nothing

			// int coordOfAO1 = Integer.parseInt(splitCo[0]) - 1;// 1 minus 1
			int coordOfAO1 = coord1Trace[i] - 1;// 1 minus 1
			// int coordOfAO2 = Integer.parseInt(splitCoAgain[0]) - 1; // 0 minus 1
			int coordOfAO2 = coord2Trace[i] - 1; // 0 minus 1

			// int coordOfAO1= Character.getNumericValue(splitOps[1].charAt(0))-1;//1
			// int coordOfAO2=Character.getNumericValue(splitOps[1].charAt(2))-1;//0
			// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);

			if (operationChar == 'D')
			{
				// System.out.println("D matched");
				dAct += costDeleteActivityObject; // 1d*costReplaceFullActivityObject;
				// System.out.println("dAct=" + dAct);
			}

			else if (operationChar == 'I')
			{
				// System.out.println("I matched");
				dAct += costInsertActivityObject; // 1d*costReplaceFullActivityObject;
				// System.out.println("dAct=" + dAct);
			}

			else if (operationChar == 'S')
			{
				// System.out.println("S matched");
				dAct += costReplaceActivityObject; // 2d*costReplaceFullActivityObject;
				// System.out.println("dAct=" + dAct);
			}

			else if (operationChar == 'N')
			{
				// System.out.println("dAct=" + dAct);
				double decayWt = 1;
				// System.out.println("N matched");
				if (Constant.useDecayInFeatureLevelED && (i == (DINSTrace.length - 1)))
				{
					decayWt = 3;
				}
				// System.out.println("Decay wt=" + decayWt);
				// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
				dFeat += (decayWt
						* getFeatureLevelDistance(activityObjects1.get(coordOfAO1), activityObjects2.get(coordOfAO2)));
			}
		}

		if (!Constant.disableRoundingEDCompute)

		{
			dAct = StatsUtils.round(dAct, 4);
			dFeat = StatsUtils.round(dFeat, 4);
		}

		// Start of disabled on Feb 4 2018
		// distanceTotal = dAct + dFeat;
		// End of disabled on Feb 4 2018

		// Start of added on Feb 4 2018
		// double EDAlpha = 0.5;
		if (this.EDAlpha > 0)
		{
			distanceTotal = /* dAct + dFeat; */
					combineActAndFeatLevelDistance(dAct, dFeat, activityObjects1.size(), activityObjects2.size(),
							EDAlpha);
		}
		else
		{
			distanceTotal = dAct + dFeat;
			// System.out.println("distanceTotal = dAct + dFeat = " + distanceTotal + "=" + dAct + "+" + dFeat);
		}
		// System.out.println("EDAlpha = " + EDAlpha);
		// Start of sanity check Feb 9
		// End of sanity check Feb 9

		if (Constant.checkEDSanity)
		{
			if (dFeat > 100)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("\ncalc HJeditDist between " + activityObjects1.size() + " & " + activityObjects2.size()
						+ " objs\nAOs1:");
				activityObjects1.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
				sb.append("\nAOs2:");
				activityObjects2.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));

				sb.append("\nTrace =" + levenshteinDistance.getFirst() + "  simpleLevenshteinDistance112="
						+ levenshteinDistance.getSecond());
				PopUps.printTracedErrorMsg("\nError:Feb5_1Bug: HJ dist=" + dAct + " + " + dFeat + "\n" + sb.toString());
				WritingToFile.appendLineToFileAbsolute(sb.toString(),
						Constant.getOutputCoreResultsPath() + "FeatEDInvestigationCountAllAnomaly.txt");

			}
			if (distanceTotal < 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("\ncalc HJeditDist between " + activityObjects1.size() + " & " + activityObjects2.size()
						+ " objs\nAOs1:");
				activityObjects1.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
				sb.append("\nAOs2:");
				activityObjects2.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));

				sb.append("\nTrace =" + levenshteinDistance.getFirst() + "  simpleLevenshteinDistance112="
						+ levenshteinDistance.getSecond());
				PopUps.printTracedErrorMsg("\nError: distanceTotal=" + distanceTotal + " \nHJ dist=" + dAct + " + "
						+ dFeat + "\n" + sb.toString());
				WritingToFile.appendLineToFileAbsolute(sb.toString(),
						Constant.getOutputCoreResultsPath() + "CombinedEDInvestigationCountAllAnomaly.txt");

			}

		}
		// End of added on Feb 4 2018

		if (VerbosityConstants.WriteEditSimilarityCalculations)
		{
			// System.out.println("passing Activity Objects of sizes: " + activityObjects1.size() + " " +
			// activityObjects2.size());
			WritingToFile.writeEditSimilarityCalculations(activityObjects1, activityObjects2, distanceTotal,
					levenshteinDistance.getFirst(), dAct, dFeat, userAtRecomm, dateAtRecomm, timeAtRecomm,
					candidateTimelineId);
		}

		if (Constant.debugFeb24_2018)
		{
			WritingToFile.appendLineToFileAbsolute(
					"\t" + userAtRecomm + "\t" + dateAtRecomm + "\t" + timeAtRecomm + distanceTotal + "\t"
							+ levenshteinDistance.getFirst() + "\t" + dAct + "\t" + dFeat + "\n",
					Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		}

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("HJ dist=" + dAct + " + " + dFeat + "\n returning(" + levenshteinDistance.getFirst()
					+ distanceTotal + ")");
		}

		// $ WritingToFile.writeOnlyTrace(levenshteinDistance.getFirst());

		// WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
		// WritingToFile.writeEditDistance(levenshteinDistance);
		return new Pair<String, Double>(levenshteinDistance.getFirst(), distanceTotal);
	}

	/**
	 * 
	 * @param dAct
	 * @param dFeat
	 * @param size1
	 * @param size2
	 * @param alpha
	 * @return
	 */
	private double combineActAndFeatLevelDistance(double dAct, double dFeat, int size1, int size2, double alpha)
	{
		double distanceTotal = -1;
		// (length of current timeline-1)*replaceWt*WtObj
		double maxActLevelDistance = Math.max((Math.max(size1, size2) - 1), 1) * this.costReplaceActivityObject;
		// = (length of current timeline)*(wtStartTime + wtLocation + wtLocPopularity)
		double maxFeatLevelDistance = Math.max(size1, size2) * (wtStartTime + wtLocation + wtLocPopularity);

		if (dAct > maxActLevelDistance || dFeat > maxFeatLevelDistance)
		{
			PopUps.printTracedErrorMsg("Error in combineActAndFeatLevelDistance : dAct" + dAct + " maxActLevelDistance="
					+ maxActLevelDistance + " dFeat=" + dFeat + " maxFeatLevelDistance=" + maxFeatLevelDistance
					+ " size1=" + size1 + " size2=" + size2 + " alpha=" + alpha);
			return -1;
		}

		if (Constant.disableRoundingEDCompute)
		{
			distanceTotal = alpha * (dAct / maxActLevelDistance) + (1 - alpha) * (dFeat / maxFeatLevelDistance);
		}
		else
		{
			distanceTotal = StatsUtils.round(
					alpha * (dAct / maxActLevelDistance) + (1 - alpha) * (dFeat / maxFeatLevelDistance),
					Constant.RoundingPrecision);
		}

		// if (VerbosityConstants.verboseCombinedEDist)
		// {
		// WritingToFile.appendLineToFileAbsolute(
		// distanceTotal + "," + dAct + "," + dFeat + "," + size1 + "," + size2 + "\n",
		// Constant.getCommonPath() + "DistanceTotalAlpha" + alpha + ".csv");
		// }

		return distanceTotal;
	}

	@Override
	public String toString()
	{
		return "HJEditDistance [EDAlpha=" + EDAlpha + "]" + "\n" + super.toString();
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
