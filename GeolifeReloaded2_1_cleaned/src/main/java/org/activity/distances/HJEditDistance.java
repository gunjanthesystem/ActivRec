package org.activity.distances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.Enums.ActDistType;
import org.activity.constants.Enums.GowGeoFeature;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
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
	// PrimaryDimension primaryDimension;
	boolean needsToPruneFirstUnknown;

	/**
	 * Sets the tolerance according the truth value of Constant.useTolerance
	 */
	public HJEditDistance(PrimaryDimension primaryDimension)
	{
		super(primaryDimension);
		this.EDAlpha = -99999;
		// this.primaryDimension = primaryDimension;
		needsToPruneFirstUnknown = Constant.needsToPruneFirstUnknown;
	}

	public HJEditDistance(double edAlpha, PrimaryDimension primaryDimension)
	{
		super(primaryDimension);
		this.EDAlpha = edAlpha;
		System.out.println("Setting EDAlpha=" + this.EDAlpha);
		// this.primaryDimension = primaryDimension;
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
			ArrayList<ActivityObject2018> activityObjects1Original,
			ArrayList<ActivityObject2018> activityObjects2Original, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateTimelineId)
	{
		ArrayList<ActivityObject2018> activityObjects1 = new ArrayList<ActivityObject2018>();
		activityObjects1.addAll(activityObjects1Original);

		ArrayList<ActivityObject2018> activityObjects2 = new ArrayList<ActivityObject2018>();
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
	public final Pair<String, Double> getHJEditDistanceInvalidsExpunged(ArrayList<ActivityObject2018> activityObjects1,
			ArrayList<ActivityObject2018> activityObjects2, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateTimelineId)
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
			ArrayList<ActivityObject2018> activityObjects1, ArrayList<ActivityObject2018> activityObjects2,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
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
			ArrayList<ActivityObject2018> activityObjects1Original,
			ArrayList<ActivityObject2018> activityObjects2Original, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateTimelineId)
	{
		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject2018> activityObjects1 = pruneFirstUnknown(activityObjects1Original);
		ArrayList<ActivityObject2018> activityObjects2 = pruneFirstUnknown(activityObjects2Original);

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
					.singletonList(StringCode.getStringCodeForActivityObjectsFromActID(activityObjects1));
			stringCodesForActivityObjects2 = (ArrayList<String>) Collections
					.singletonList(StringCode.getStringCodeForActivityObjectsFromActID(activityObjects2));
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

			WToFile.writeEditSimilarityCalculations(activityObjects1, activityObjects2, distanceTotal,
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
			ArrayList<ActivityObject2018> activityObjects1Original,
			ArrayList<ActivityObject2018> activityObjects2Original, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateTimelineId)
	{
		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject2018> activityObjects1 = pruneFirstUnknown(activityObjects1Original);
		ArrayList<ActivityObject2018> activityObjects2 = pruneFirstUnknown(activityObjects2Original);

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
					.singletonList(StringCode.getStringCodeForActivityObjectsFromActID(activityObjects1));
			stringCodesForActivityObjects2 = (ArrayList<String>) Collections
					.singletonList(StringCode.getStringCodeForActivityObjectsFromActID(activityObjects2));
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

			WToFile.writeEditSimilarityCalculations(activityObjects1, activityObjects2, distanceTotal,
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
			ArrayList<ActivityObject2018> activityObjects1Original,
			ArrayList<ActivityObject2018> activityObjects2Original, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateTimelineId)
	{
		// PrimaryDimension primaryDimension = Constant.primaryDimension;//moved to constructor
		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject2018> activityObjects1 = activityObjects1Original;
		ArrayList<ActivityObject2018> activityObjects2 = activityObjects2Original;

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

				if (Constant.useDecayInFED && (i == (splitted.length - 1)))
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
							EDAlpha, userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);
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
				WToFile.appendLineToFileAbs(sb.toString(),
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
				WToFile.appendLineToFileAbs(sb.toString(),
						Constant.getOutputCoreResultsPath() + "CombinedEDInvestigationCountAllAnomaly.txt");

			}

		}
		// End of added on Feb 4 2018

		if (VerbosityConstants.WriteEditSimilarityCalculations)
		{
			// System.out.println("passing Activity Objects of sizes: " + activityObjects1.size() + " " +
			// activityObjects2.size());
			WToFile.writeEditSimilarityCalculations(activityObjects1, activityObjects2, distanceTotal,
					levenshteinDistance.getFirst(), dAct, dFeat, userAtRecomm, dateAtRecomm, timeAtRecomm,
					candidateTimelineId);
		}

		if (Constant.debugFeb24_2018)
		{
			WToFile.appendLineToFileAbs(
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
	 * 
	 * @return Pair<Trace as String, Edit Distance> ///we can also do n Pair<Trace as String, Pair <total Edit Distance,
	 *         act level edit distance> /
	 * @since Mar 1 2018 TODO 25 July 2018: needs to be checked if correctly works for secondary dimension or not
	 */
	public final Pair<String, Double> getHJEditDistanceWithTrace(ArrayList<ActivityObject2018> activityObjects1Original,
			ArrayList<ActivityObject2018> activityObjects2Original, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateTimelineId)
	{
		// PrimaryDimension primaryDimension = Constant.primaryDimension;//moved to constructor

		boolean shouldComputeFeatureLevelDistance = this.getShouldComputeFeatureLevelDistance();

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("\n---calc HJeditDist between " + activityObjects1Original.size() + " & "
					+ activityObjects2Original.size() + " objs");
		}

		ArrayList<ActivityObject2018> activityObjects1 = activityObjects1Original;
		ArrayList<ActivityObject2018> activityObjects2 = activityObjects2Original;

		if (needsToPruneFirstUnknown)
		{
			activityObjects1 = pruneFirstUnknown(activityObjects1Original);
			activityObjects2 = pruneFirstUnknown(activityObjects2Original);
		}

		double dAct = 0, dFeat = 0, distanceTotal = 0;

		// long t0, t2, t3, t4, t5, t6;
		// t0 = t2 = t3 = t4 = t5 = t6 = Long.MIN_VALUE;
		// t0 = System.nanoTime();
		HashMap<Integer, Character> uniqueCharCodes = StringCode.getLocallyUniqueCharCodeMap(activityObjects1,
				activityObjects2, primaryDimension);
		// t2 = System.nanoTime();
		// Int2CharOpenHashMap uniqueCharCodesFU = StringCode.getLocallyUniqueCharCodeMapFU(activityObjects1,
		// activityObjects2, primaryDimension);
		// t3 = System.nanoTime();
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
		// t4 = System.nanoTime();
		stringCodesForActivityObjects1 = StringCode.getStringCodesForActivityObjects17July2018(activityObjects1,
				primaryDimension, uniqueCharCodes, VerbosityConstants.verbose);
		stringCodesForActivityObjects2 = StringCode.getStringCodesForActivityObjects17July2018(activityObjects2,
				primaryDimension, uniqueCharCodes, VerbosityConstants.verbose);
		// t5 = System.nanoTime();

		// Start of added on 17 Mar 2018
		if (Constant.useFeatureDistancesOfAllActs && shouldComputeFeatureLevelDistance)
		{
			dFeat = getFeatureLevelEditDistanceAllActsV2(activityObjects1, activityObjects2);
		}
		// End of added on 17 Mar 2018
		//// temp start
		// ArrayList<String> stringCodesForActivityObjects1FU = StringCode.getStringCodesForActivityObjectsFU(
		// activityObjects1, primaryDimension, uniqueCharCodesFU, VerbosityConstants.verbose);
		// ArrayList<String> stringCodesForActivityObjects2FU = StringCode.getStringCodesForActivityObjectsFU(
		// activityObjects2, primaryDimension, uniqueCharCodesFU, VerbosityConstants.verbose);
		// t6 = System.nanoTime();

		// String debug9Mar = (t2 - t0) + "," + (t3 - t2) + "," + (t5 - t4) + "," + (t6 - t5) + ","
		// + stringCodesForActivityObjects1.equals(stringCodesForActivityObjects1FU) + ","
		// + stringCodesForActivityObjects2.equals(stringCodesForActivityObjects2FU) + ","
		// + stringCodesForActivityObjects1 + "," + (stringCodesForActivityObjects1FU) + ","
		// + stringCodesForActivityObjects2 + "," + (stringCodesForActivityObjects2FU) + "\n";
		// WritingToFile.appendLineToFileAbsolute(debug9Mar.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar9_2018.csv");
		/// temp end // }

		Triple<String, Double, Triple<char[], int[], int[]>> levenshteinDistance = null;
		long t1 = System.nanoTime();

		levenshteinDistance = getLowestMySimpleLevenshteinDistance(stringCodesForActivityObjects1,
				stringCodesForActivityObjects2, 1, 1, 2, null);// getMySimpleLevenshteinDistance

		if (false)// sanity checking new getLowestMySimpleLevenshteinDistance and getMySimpleLevenshteinDistance()
		{
			sanityCheckLevenshteinDistOutput1Mar2018(levenshteinDistance);
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
				if (Constant.useFeatureDistancesOfAllActs == false && shouldComputeFeatureLevelDistance)
				{// i.e., feature distance of only N (matched) act objs
					// System.out.println("dAct=" + dAct);
					double decayWt = 1;
					// System.out.println("N matched");
					if (Constant.useDecayInFED && (i == (DINSTrace.length - 1)))
					{
						decayWt = 3;
					}
					// System.out.println("Decay wt=" + decayWt);
					// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
					dFeat += (decayWt * getFeatureLevelDistance(activityObjects1.get(coordOfAO1),
							activityObjects2.get(coordOfAO2)));
				}
			}
		} // end of for over DINS trace

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
		// if (this.EDAlpha > 0)//Disabled on April 26 2018
		if (this.EDAlpha > -1)// Added on April 26 2018
		{
			if (shouldComputeFeatureLevelDistance == false)
			{
				dFeat = 0;
			}
			distanceTotal = /* dAct + dFeat; */
					combineActAndFeatLevelDistance(dAct, dFeat, activityObjects1.size(), activityObjects2.size(),
							EDAlpha, userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);
		}
		else
		{
			if (Constant.noFED)
			{
				distanceTotal = dAct;
			}
			if (Constant.noAED)
			{
				distanceTotal = dFeat;
			}
			else
			{
				distanceTotal = dAct + dFeat;
			}

			if (VerbosityConstants.verboseCombinedEDist || VerbosityConstants.verboseDistDistribution)
			{
				String infoHeader = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candidateTimelineId;
				WToFile.appendLineToFileAbs(
						infoHeader + "," + dAct + "," + dFeat + "," + distanceTotal + "," + dAct + "," + dFeat + ","
								+ activityObjects1.size() + "," + activityObjects2.size() + "\n",
						Constant.getCommonPath() + "DistanceDistribution.csv");// primarily for correlated analysis
			}
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
				WToFile.appendLineToFileAbs(sb.toString(),
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
				WToFile.appendLineToFileAbs(sb.toString(),
						Constant.getOutputCoreResultsPath() + "CombinedEDInvestigationCountAllAnomaly.txt");

			}

		}
		// End of added on Feb 4 2018

		if (VerbosityConstants.WriteEditSimilarityCalculations)
		{
			// System.out.println("passing Activity Objects of sizes: " + activityObjects1.size() + " " +
			// activityObjects2.size());
			WToFile.writeEditSimilarityCalculations(activityObjects1, activityObjects2, distanceTotal,
					levenshteinDistance.getFirst(), dAct, dFeat, userAtRecomm, dateAtRecomm, timeAtRecomm,
					candidateTimelineId);
		}

		if (Constant.debugFeb24_2018)
		{
			WToFile.appendLineToFileAbs(
					"\t" + userAtRecomm + "\t" + dateAtRecomm + "\t" + timeAtRecomm + distanceTotal + "\t"
							+ levenshteinDistance.getFirst() + "\t" + dAct + "\t" + dFeat + "\n",
					Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		}

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("HJ dist=" + dAct + " + " + dFeat + "\n returning(" + levenshteinDistance.getFirst()
					+ "," + distanceTotal + ")");
		}

		// $ WritingToFile.writeOnlyTrace(levenshteinDistance.getFirst());

		// WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
		// WritingToFile.writeEditDistance(levenshteinDistance);
		return new Pair<String, Double>(levenshteinDistance.getFirst(), distanceTotal);
	}

	// start of Jan 5 2019
	/**
	 * Fork of getActEditDistWithTrace_FeatDiffs_13April2018(): calculate the act level edit distance with trance and
	 * return the feature level VALUES OF CORRESONDING AOs.
	 * <p>
	 * Calculate the Act level Edit Distance as per HJ's specification. First calculating simple levenshtein distance
	 * and then calculating the cost from the trace of operations performed using the assigned costs and wts of objects
	 * So, while optimisation of distance, the optimisation is done using 112 costs while the resultant cost is
	 * calculated after the operations have been thus established... using the assigned costs for Activity Objects and
	 * features. (dAct rounded off to 4 decimal places)
	 * <p>
	 * Note: each activity object can have multiple primary dimension vals in case they are resultant of mergers.
	 * 
	 * @param actObjs1Original
	 *            sequence of activity objects to be compared (usually from a candidate timeline).
	 * @param actObjs2Original
	 *            activitiesGuidingRecommendation, i.e, sequence of activity objects forming current timeline
	 * 
	 * @param userAtRecomm
	 *            only used for writing to file
	 * @param dateAtRecomm
	 *            only used for writing to file
	 * @param timeAtRecomm
	 *            only used for writing to file
	 * @param candidateTimelineId
	 * @param actLevelDistanceType
	 * @return Triple{TraceAsString,ActLevelEditDistance,List of EnumMap of {GowallaFeatures, ValPairsForThatFeature}
	 *         one for each corresponding AO comparison}}
	 *         <p>
	 *         Triple{TraceAsString,ActLevelEditDistance,List{EnumMap{GowallaFeatures, Double}}}}
	 *         <p>
	 *         (old)Pair<Trace as String, Edit Distance> ///we can also do n Pair<Trace as String, Pair <total Edit
	 *         Distance, act level edit distance> /
	 * @since Jan 5 2018
	 */
	public final Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>> getActEditDistWithTrace_FeatValPairs_5Jan2019(
			ArrayList<ActivityObject2018> actObjs1Original, ArrayList<ActivityObject2018> actObjs2Original,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId,
			ActDistType actLevelDistanceType)
	{
		double actED = 0;
		List<EnumMap<GowGeoFeature, Pair<String, String>>> featValPairs = new ArrayList<>();
		boolean shouldComputeFeatureLevelDiffs = this.getShouldComputeFeatureLevelDistance();
		// max value of feature diff over all AOs (horizontally) for this cand timeline (wrt corresonding AO in current
		// timeline)EnumMap<GowallaFeatures, Double> maxFeatureDiffs = new EnumMap<>(GowallaFeatures.class);

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("\n---calc HJeditDist between " + actObjs1Original.size() + " & "
					+ actObjs2Original.size() + " objs" + "  primaryDimension=" + this.primaryDimension);
		}

		ArrayList<ActivityObject2018> actObjs1 = actObjs1Original;
		ArrayList<ActivityObject2018> actObjs2 = actObjs2Original;

		if (needsToPruneFirstUnknown)
		{
			actObjs1 = pruneFirstUnknown(actObjs1Original);
			actObjs2 = pruneFirstUnknown(actObjs2Original);
		}

		// Start of added on 17 Mar 2018
		if (Constant.useFeatureDistancesOfAllActs && shouldComputeFeatureLevelDiffs)
		{
			// dFeat = getFeatureLevelEditDistanceAllActsV2(activityObjects1, activityObjects2);
			featValPairs = getFeatureLevelPairsBetweenAllAOsV2(actObjs1, actObjs2);
		}
		else
		{// empty one, which will be filler iteratively over matched AOs
			featValPairs = new ArrayList<>();
		}
		// End of added on 17 Mar 2018

		long t1 = System.nanoTime();

		// Triple<String, Double, Triple<char[], int[], int[]>> levenshteinDistance =
		// getLowestMySimpleLevenshteinDistance(
		// actObjs1, actObjs2, this.primaryDimension, 1, 1, 2);// getMySimpleLevenshteinDistance
		////////////////////////////////
		Triple<String, Double, Triple<char[], int[], int[]>> actSimpleDistance = getLowestDistanceOfGivenType(actObjs1,
				actObjs2, this.primaryDimension, 1, 1, 2, actLevelDistanceType);
		/////////////////////////////

		long t2 = System.nanoTime();

		if (false)// sanity checking new getLowestMySimpleLevenshteinDistance and getMySimpleLevenshteinDistance()
		{// not sure if applicable non-simple-levenshtein distance
			sanityCheckLevenshteinDistOutput1Mar2018(actSimpleDistance);
		}
		// { levenshteinDistance = ProcessUtils.executeProcessEditDistance(stringCodeForActivityObjects1,
		// stringCodeForActivityObjects2, Integer.toString(1), Integer.toString(1), Integer.toString(2));
		// System.out.println("getMySimpleLevenshteinProcesse took " + (System.nanoTime() - t1) + " ns");}

		if (Constant.useFeatureDistancesOfAllActs == false)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: Constant.useFeatureDistancesOfAllActs is false. This version has been disabled in 9 Jan version. "
							+ "If reimplementation is required. See: HJEditDistance.getActEditDistWithTrace_FeatValPairs_5Jan2019()");
		}

		if (false && actLevelDistanceType.equals(ActDistType.MyLevenshtein))// disabled on 9 Jan 2019 since it
		{// overshadows dynamic variations in insert delete replacement wts done inside simple ed computation.
			char[] DINSTrace = actSimpleDistance.getThird().getFirst();
			int[] coord1Trace = actSimpleDistance.getThird().getSecond();
			int[] coord2Trace = actSimpleDistance.getThird().getThird();

			int numOfNsForSanityCheck = 0;
			for (int i = 0; i < DINSTrace.length; i++)
			{
				char operationChar = DINSTrace[i];
				int coordOfAO1 = coord1Trace[i] - 1;// 1 minus 1
				int coordOfAO2 = coord2Trace[i] - 1; // 0 minus 1
				if (operationChar == 'D')
				{
					actED += costDeleteActivityObject; // 1d*costReplaceFullActivityObject;
					// System.out.println("D matched"); System.out.println("dAct=" + dAct);
				}
				else if (operationChar == 'I')
				{
					actED += costInsertActivityObject; // 1d*costReplaceFullActivityObject;
					// System.out.println("I matched");System.out.println("dAct=" + dAct);
				}
				else if (operationChar == 'S')
				{
					actED += costReplaceActivityObject; // 2d*costReplaceFullActivityObject;
					// System.out.println("S matched");System.out.println("dAct=" + dAct);
				}
				else if (operationChar == 'N')
				{
					if (Constant.useFeatureDistancesOfAllActs == false && shouldComputeFeatureLevelDiffs)
					{
						numOfNsForSanityCheck += 1;
						// i.e., feature distance of only N (matched) act objs
						// System.out.println("dAct=" + dAct);System.out.println("N matched");
						// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
						featValPairs.add(
								getFeatureLevelValPairsBetweenAOs(actObjs1.get(coordOfAO1), actObjs2.get(coordOfAO2)));
					}
				}
			} // end of for over DINS trace

			// Start of a sanity check
			if (Constant.useFeatureDistancesOfAllActs == false)
			{
				Sanity.eq(numOfNsForSanityCheck, featValPairs.size(), "Error:numOfNsForSanityCheck="
						+ numOfNsForSanityCheck + "!=featureDifferences.size()" + featValPairs.size());
			}
			// end of a sanity check
		}
		else
		{
			actED = wtFullActivityObject * actSimpleDistance.getSecond();
		}

		if (!Constant.disableRoundingEDCompute)
		{
			actED = StatsUtils.round(actED, 4);
		}

		// Start of added on Feb 4 2018
		// double EDAlpha = 0.5;
		// if (this.EDAlpha > 0)
		// {if (this.getShouldComputeFeatureLevelDistance() == false)
		// {dFeat = 0;}
		// distanceTotal = /* dAct + dFeat; */
		// combineActAndFeatLevelDistance(dAct, dFeat, activityObjects1.size(), activityObjects2.size(),EDAlpha);}
		// else{distanceTotal = dAct + dFeat;
		// // System.out.println("distanceTotal = dAct + dFeat = " + distanceTotal + "=" + dAct + "+" + dFeat);}
		// System.out.println("EDAlpha = " + EDAlpha);

		// writing dFeat and distanceTotal as -1 as they are not computed in this method
		if (VerbosityConstants.WriteEditSimilarityCalculations)
		{
			// System.out.println("passing Activity Objects of sizes: " + activityObjects1.size() + " " +
			// activityObjects2.size());
			WToFile.writeEditSimilarityCalculations(actObjs1, actObjs2, -1, actSimpleDistance.getFirst(), actED, -1,
					userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);
		}

		if (VerbosityConstants.verboseDistance)
		{
			StringBuilder sb = new StringBuilder(
					"\t" + userAtRecomm + "\t" + dateAtRecomm + "\t" + timeAtRecomm + "\n");
			sb.append("getMySimpleLevenshteinDistance took " + (t2 - t1) + " ns");
			sb.append("\nAOs1:");
			actObjs1.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
			sb.append("\nAOs2:");
			actObjs2.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));

			sb.append("dAct=" + actED + "\n" + "Trace =" + actSimpleDistance.getFirst() + " DINSTrace="
					+ new String(actSimpleDistance.getThird().getFirst()) + "\n third_second="
					+ Arrays.toString(actSimpleDistance.getThird().getSecond()) + "\n third_third="
					+ Arrays.toString(actSimpleDistance.getThird().getThird()) + "  simpleLevenshteinDistance112="
					+ actSimpleDistance.getSecond() + "\n");
			WToFile.appendLineToFileAbs(sb.toString(), Constant.getOutputCoreResultsPath() + "VerboseED.txt");
			System.out.println(sb.toString());
		}

		// Start of temp
		// System.out.println("levenshteinDistance.getFirst() = " + levenshteinDistance.getFirst() + " actED= " + actED
		// + " featDiffs.size()=" + featDiffs.size());
		Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>> result = new Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>>(
				actSimpleDistance.getFirst(), actED, featValPairs);

		// if (result == null)
		// {
		// System.out.println("Eureka!! results is null");
		// }
		// end of temp

		// $ WritingToFile.writeOnlyTrace(levenshteinDistance.getFirst());
		// WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
		// WritingToFile.writeEditDistance(levenshteinDistance);
		return result;
		// new Pair<String, Double>(levenshteinDistance.getFirst(), distanceTotal);
	}

	// end of Jan 5 2019

	/// Start of April 13 2018
	/**
	 * Fork of getHJEditDistanceWithTrace(): calculate the act level edit distance with trance and return the feature
	 * level differences
	 * <p>
	 * Calculate the Act level Edit Distance as per HJ's specification. First calculating simple levenshtein distance
	 * and then calculating the cost from the trace of operations performed using the assigned costs and wts of objects
	 * So, while optimisation of distance, the optimisation is done using 112 costs while the resultant cost is
	 * calculated after the operations have been thus established... using the assigned costs for Activity Objects and
	 * features. (dAct rounded off to 4 decimal places)
	 * <p>
	 * Note: each activity object can have multiple primary dimension vals in case they are resultant of mergers.
	 * 
	 * @param actObjs1Original
	 *            sequence of activity objects to be compared (usually from a candidate timeline).
	 * @param actObjs2Original
	 *            activitiesGuidingRecommendation, i.e, sequence of activity objects forming current timeline
	 * 
	 * @param userAtRecomm
	 *            only used for writing to file
	 * @param dateAtRecomm
	 *            only used for writing to file
	 * @param timeAtRecomm
	 *            only used for writing to file
	 * @param candidateTimelineId
	 * @return Triple{TraceAsString,ActLevelEditDistance,List of EnumMap of {GowallaFeatures, DiffForThatFeature} one
	 *         for each corresponding AO comparison}}
	 *         <p>
	 *         Triple{TraceAsString,ActLevelEditDistance,List{EnumMap{GowallaFeatures, Double}}}}
	 *         <p>
	 *         (old)Pair<Trace as String, Edit Distance> ///we can also do n Pair<Trace as String, Pair <total Edit
	 *         Distance, act level edit distance> /
	 * @since April 13 2018
	 */
	public final Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>> getActEditDistWithTrace_FeatDiffs_13April2018(
			ArrayList<ActivityObject2018> actObjs1Original, ArrayList<ActivityObject2018> actObjs2Original,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
	{
		double actED = 0;
		List<EnumMap<GowGeoFeature, Double>> featDiffs = new ArrayList<>();
		boolean shouldComputeFeatureLevelDiffs = this.getShouldComputeFeatureLevelDistance();
		// max value of feature diff over all AOs (horizontally) for this cand timeline (wrt corresonding AO in current
		// timeline)EnumMap<GowallaFeatures, Double> maxFeatureDiffs = new EnumMap<>(GowallaFeatures.class);

		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("\n---calc HJeditDist between " + actObjs1Original.size() + " & "
					+ actObjs2Original.size() + " objs" + "  primaryDimension=" + this.primaryDimension);
		}

		ArrayList<ActivityObject2018> actObjs1 = actObjs1Original;
		ArrayList<ActivityObject2018> actObjs2 = actObjs2Original;

		if (needsToPruneFirstUnknown)
		{
			actObjs1 = pruneFirstUnknown(actObjs1Original);
			actObjs2 = pruneFirstUnknown(actObjs2Original);
		}

		// Start of added on 17 Mar 2018
		if (Constant.useFeatureDistancesOfAllActs && shouldComputeFeatureLevelDiffs)
		{
			// dFeat = getFeatureLevelEditDistanceAllActsV2(activityObjects1, activityObjects2);
			featDiffs = getFeatureLevelDiffsBetweenAllAOsV2(actObjs1, actObjs2);
		}
		else
		{// empty one, which will be filler iteratively over matched AOs
			featDiffs = new ArrayList<>();
		}
		// End of added on 17 Mar 2018

		long t1 = System.nanoTime();
		Triple<String, Double, Triple<char[], int[], int[]>> levenshteinDistance = getLowestMySimpleLevenshteinDistance(
				actObjs1, actObjs2, this.primaryDimension, 1, 1, 2);// getMySimpleLevenshteinDistance
		long t2 = System.nanoTime();

		if (false)// sanity checking new getLowestMySimpleLevenshteinDistance and getMySimpleLevenshteinDistance()
		{
			sanityCheckLevenshteinDistOutput1Mar2018(levenshteinDistance);
		}
		// { levenshteinDistance = ProcessUtils.executeProcessEditDistance(stringCodeForActivityObjects1,
		// stringCodeForActivityObjects2, Integer.toString(1), Integer.toString(1), Integer.toString(2));
		// System.out.println("getMySimpleLevenshteinProcesse took " + (System.nanoTime() - t1) + " ns");}

		char[] DINSTrace = levenshteinDistance.getThird().getFirst();
		int[] coord1Trace = levenshteinDistance.getThird().getSecond();
		int[] coord2Trace = levenshteinDistance.getThird().getThird();

		int numOfNsForSanityCheck = 0;
		for (int i = 0; i < DINSTrace.length; i++)
		{
			char operationChar = DINSTrace[i];
			int coordOfAO1 = coord1Trace[i] - 1;// 1 minus 1
			int coordOfAO2 = coord2Trace[i] - 1; // 0 minus 1
			if (operationChar == 'D')
			{
				actED += costDeleteActivityObject; // 1d*costReplaceFullActivityObject;
				// System.out.println("D matched"); System.out.println("dAct=" + dAct);
			}
			else if (operationChar == 'I')
			{
				actED += costInsertActivityObject; // 1d*costReplaceFullActivityObject;
				// System.out.println("I matched");System.out.println("dAct=" + dAct);
			}
			else if (operationChar == 'S')
			{
				actED += costReplaceActivityObject; // 2d*costReplaceFullActivityObject;
				// System.out.println("S matched");System.out.println("dAct=" + dAct);
			}
			else if (operationChar == 'N')
			{
				if (Constant.useFeatureDistancesOfAllActs == false && shouldComputeFeatureLevelDiffs)
				{
					numOfNsForSanityCheck += 1;
					// i.e., feature distance of only N (matched) act objs
					// System.out.println("dAct=" + dAct);System.out.println("N matched");
					// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
					featDiffs.add(getFeatureLevelDiffsBetweenAOs(actObjs1.get(coordOfAO1), actObjs2.get(coordOfAO2)));
				}
			}
		} // end of for over DINS trace

		// Start of a sanity check
		if (Constant.useFeatureDistancesOfAllActs == false)
		{
			Sanity.eq(numOfNsForSanityCheck, featDiffs.size(), "Error:numOfNsForSanityCheck=" + numOfNsForSanityCheck
					+ "!=featureDifferences.size()" + featDiffs.size());
		}
		// end of a sanity check

		if (!Constant.disableRoundingEDCompute)
		{
			actED = StatsUtils.round(actED, 4);
		}

		// Start of added on Feb 4 2018
		// double EDAlpha = 0.5;
		// if (this.EDAlpha > 0)
		// {if (this.getShouldComputeFeatureLevelDistance() == false)
		// {dFeat = 0;}
		// distanceTotal = /* dAct + dFeat; */
		// combineActAndFeatLevelDistance(dAct, dFeat, activityObjects1.size(), activityObjects2.size(),EDAlpha);}
		// else{distanceTotal = dAct + dFeat;
		// // System.out.println("distanceTotal = dAct + dFeat = " + distanceTotal + "=" + dAct + "+" + dFeat);}
		// System.out.println("EDAlpha = " + EDAlpha);

		// writing dFeat and distanceTotal as -1 as they are not computed in this method
		if (VerbosityConstants.WriteEditSimilarityCalculations)
		{
			// System.out.println("passing Activity Objects of sizes: " + activityObjects1.size() + " " +
			// activityObjects2.size());
			WToFile.writeEditSimilarityCalculations(actObjs1, actObjs2, -1, levenshteinDistance.getFirst(), actED, -1,
					userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);
		}

		if (VerbosityConstants.verboseDistance)
		{
			StringBuilder sb = new StringBuilder(
					"\t" + userAtRecomm + "\t" + dateAtRecomm + "\t" + timeAtRecomm + "\n");
			sb.append("getMySimpleLevenshteinDistance took " + (t2 - t1) + " ns");
			sb.append("\nAOs1:");
			actObjs1.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
			sb.append("\nAOs2:");
			actObjs2.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));

			sb.append("dAct=" + actED + "\n" + "Trace =" + levenshteinDistance.getFirst() + " DINSTrace="
					+ new String(levenshteinDistance.getThird().getFirst()) + "\n third_second="
					+ Arrays.toString(levenshteinDistance.getThird().getSecond()) + "\n third_third="
					+ Arrays.toString(levenshteinDistance.getThird().getThird()) + "  simpleLevenshteinDistance112="
					+ levenshteinDistance.getSecond() + "\n");
			WToFile.appendLineToFileAbs(sb.toString(), Constant.getOutputCoreResultsPath() + "VerboseED.txt");
			System.out.println(sb.toString());
		}

		// Start of temp
		// System.out.println("levenshteinDistance.getFirst() = " + levenshteinDistance.getFirst() + " actED= " + actED
		// + " featDiffs.size()=" + featDiffs.size());
		Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>> result = new Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>(
				levenshteinDistance.getFirst(), actED, featDiffs);

		// if (result == null)
		// {
		// System.out.println("Eureka!! results is null");
		// }
		// end of temp

		// $ WritingToFile.writeOnlyTrace(levenshteinDistance.getFirst());
		// WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
		// WritingToFile.writeEditDistance(levenshteinDistance);
		return result;
		// new Pair<String, Double>(levenshteinDistance.getFirst(), distanceTotal);
	}

	/**
	 * 
	 * @param levenshteinDistance
	 */
	private void sanityCheckLevenshteinDistOutput1Mar2018(
			Triple<String, Double, Triple<char[], int[], int[]>> levenshteinDistance)
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
		WToFile.appendLineToFileAbs(sbTemp1.toString(),
				Constant.getCommonPath() + "Debug1Mar2018newLevenshteinDistance.csv");
	}

	/// End of April 13 2018
	/**
	 * @since Mar 17 2018
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public double getFeatureLevelEditDistanceAllActs(ArrayList<ActivityObject2018> activityObjects1,
			ArrayList<ActivityObject2018> activityObjects2)
	{
		double dFeat = 0;
		int ao1Size = activityObjects1.size();
		int ao2Size = activityObjects2.size();

		List<ActivityObject2018> aos1ToCompare = activityObjects1;
		List<ActivityObject2018> aos2ToCompare = activityObjects2;

		StringBuilder sbtt1 = new StringBuilder();
		if (true)// debug Mar17 2018 //Sanity Checked ok
		{
			sbtt1.append("\n\nactivityObjects1=:1" + "" + getActIDsAsString(activityObjects1) + "\nactivityObjects2="
					+ getActIDsAsString(activityObjects2));
			sbtt1.append("\nao1Size=" + ao1Size + "\nao2Size=" + ao2Size + "  equalSize=" + (ao1Size == ao2Size));

		}

		if (ao1Size > ao2Size)
		{
			// aos1ToCompare = activityObjects1.subList(ao2Size - 2, ao1Size);
			sbtt1.append("\nao1Size > ao2Size\n");
			aos1ToCompare = activityObjects1.subList(ao2Size - ao1Size, ao1Size);
		}
		else if (ao1Size < ao2Size)
		{
			sbtt1.append("\nao1Size < ao2Size\n");
			aos2ToCompare = activityObjects2.subList(ao1Size - ao2Size, ao2Size);// SHOWING INDEX OUT
		}

		for (int i = aos1ToCompare.size() - 1; i >= 0; i--)
		{
			double decayWt = 1;
			// System.out.println("N matched");
			// if (Constant.useDecayInFeatureLevelED)
			// {
			// decayWt = 3;
			// }
			// System.out.println("Decay wt=" + decayWt);
			// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
			dFeat += (decayWt * getFeatureLevelDistance(activityObjects1.get(i), activityObjects2.get(i)));
		}

		// if (dFeat == 0)
		// {
		// StringBuilder sb = new StringBuilder();
		// sb.append("\naos1ToCompare=" + aos1ToCompare + "\naos2ToCompare=" + aos2ToCompare);
		// WritingToFile.appendLineToFileAbsolute(sb.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar17_2018EDWhyFEDIS0.csv");
		//
		// }

		if (true)// debug Mar17 2018 //Sanity Checked ok
		{
			// StringBuilder sbtt1 = new StringBuilder();
			// sbtt1.append("\n\nactivityObjects1=:1" + "" + getActIDsAsString(activityObjects1) + "\nactivityObjects2="
			// + getActIDsAsString(activityObjects2));
			// sbtt1.append("\nao1Size=" + ao1Size + "\nao2Size=" + ao2Size);
			sbtt1.append("\naos1ToCompare=" + getActIDsAsString(aos1ToCompare) + "\naos2ToCompare="
					+ getActIDsAsString(aos2ToCompare));
			WToFile.appendLineToFileAbs(sbtt1.toString(),
					Constant.getOutputCoreResultsPath() + "DebugMar17_2018EDFeatureLevel.csv");
		}
		return dFeat;
	}

	/**
	 * No sublisting required
	 * 
	 * @since Mar 22 2018
	 * @param actObjs1
	 * @param actObjs2
	 * @return
	 */
	public double getFeatureLevelEditDistanceAllActsV2(ArrayList<ActivityObject2018> actObjs1,
			ArrayList<ActivityObject2018> actObjs2)
	{
		double dFeat = 0;
		int ao1Size = actObjs1.size();
		int ao2Size = actObjs2.size();

		// List<ActivityObject> aos1ToCompare = actObjs1;
		// List<ActivityObject> aos2ToCompare = actObjs2;

		StringBuilder sbtt1 = new StringBuilder();
		if (false)// debug Mar17 2018 //Sanity Checked ok
		{
			sbtt1.append("\n\nactivityObjects1=" + "" + getActIDsAsString(actObjs1) + "\nactivityObjects2="
					+ getActIDsAsString(actObjs2));
			sbtt1.append("\nao1Size=" + ao1Size + "\nao2Size=" + ao2Size + "  equalSize=" + (ao1Size == ao2Size));
		}

		int minSize = Math.min(ao1Size, ao2Size);
		sbtt1.append("\nminSize=" + minSize + "\n");

		for (int minIter = 0; minIter < minSize; minIter++)
		{
			double decayWt = 1;
			// System.out.println("N matched");
			// if (Constant.useDecayInFeatureLevelED)
			// {decayWt = 3;}
			// System.out.println("Decay wt=" + decayWt);
			// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
			ActivityObject2018 ao1ToCompare = actObjs1.get(ao1Size - 1 - minIter);
			ActivityObject2018 ao2ToCompare = actObjs2.get(ao2Size - 1 - minIter);
			// $sbtt1.append(ao1ToCompare.getActivityID() + "--" + ao2ToCompare.getActivityID() + "\n");
			// Pairwise compare the last minSize AOs
			dFeat += (decayWt * getFeatureLevelDistance(ao1ToCompare, ao2ToCompare));
		}

		// if (dFeat == 0)
		// {
		// StringBuilder sb = new StringBuilder();
		// sb.append("\naos1ToCompare=" + aos1ToCompare + "\naos2ToCompare=" + aos2ToCompare);
		// WritingToFile.appendLineToFileAbsolute(sb.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar17_2018EDWhyFEDIS0.csv");
		// }

		// if (true)// debug Mar22 2018 //Sanity Checked ok
		// {
		// WritingToFile.appendLineToFileAbsolute(sbtt1.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar22_2018EDFeatureLevel.csv");
		// }
		return dFeat;
	}

	/**
	 * Fork of org.activity.distances.HJEditDistance.getSummaryStatsForEachFeatureDiffOverListOfAOsInACand()
	 * <p>
	 * Called for each candidate timelines
	 * <p>
	 * Summary (e.g. max) of feature diff for each of the GowallaFeature. In other words, max diff value for each
	 * feature over corresponding pairwise comparison of each act obj of the two timelines.
	 * <p>
	 * Aggregation stats over list (where each item is a map of feature diffs).
	 * <p>
	 * <b>Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)</b>
	 * 
	 * @param featureValPairList
	 *            list of enummaps, where each enumMap contains pairs {feature,diffWithCorresondingAOInCurrentTimeline}
	 * @param userAtRecomm
	 *            for logging only
	 * @param dateAtRecomm
	 *            for logging only
	 * @param timeAtRecomm
	 *            for logging only
	 * @param candId
	 *            for logging only
	 * @param considerValOrValDiff
	 *            TRUE: take summary stat of each feature val, FALSE: take summary stat of diff of feature val pairs
	 *            (added on 15 Feb 2019)
	 * 
	 * @return one enummap which contains pairs {feature,summaryStatForThatFeature}
	 * 
	 */
	public static EnumMap<GowGeoFeature, DoubleSummaryStatistics> getSummaryStatsForEachFeatureValsPairsOverListOfAOsInACand(
			List<EnumMap<GowGeoFeature, Pair<String, String>>> featureValPairList, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm, String candId, boolean considerValOrValDiff)
	{
		// Max feature diff for each of the GowallaFeature. In other words, max diff value for each feature over
		// corresponding pairwise comparison of each act obj of the two timelines
		EnumMap<GowGeoFeature, DoubleSummaryStatistics> summaryFeatureValsOverAllActsInACand = new EnumMap<>(
				GowGeoFeature.class);

		// added 23 Nov 2018 NOT USED AT THE MOMENT
		// EnumMap<GowGeoFeature, DoubleSummaryStatistics> summaryOfSquaredFeatDiffOverAllActsInACand = new EnumMap<>(
		// GowGeoFeature.class);
		// EnumMap<GowallaFeatures, Double> minFeatureDiffOverAllActs = new EnumMap<>(GowallaFeatures.class);

		// Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
		Set<GowGeoFeature> listOfGowallaFeatures = featureValPairList.get(0).keySet();

		for (GowGeoFeature gowallaFeature : listOfGowallaFeatures)
		{
			if (gowallaFeature.equals(GowGeoFeature.StartGeoF) || gowallaFeature.equals(GowGeoFeature.EndGeoF))
			{
				continue;// as "startLat|startLon"
			}
			ArrayList<Double> allValsForThisFeatureOverList = new ArrayList<Double>();

			for (EnumMap<GowGeoFeature, Pair<String, String>> eForEachAOInCand : featureValPairList)
			{
				Pair<String, String> valPairForThisFeatForThisAO = eForEachAOInCand.get(gowallaFeature);

				if (considerValOrValDiff)
				{
					allValsForThisFeatureOverList.add(Double.valueOf(valPairForThisFeatForThisAO.getFirst()));
					allValsForThisFeatureOverList.add(Double.valueOf(valPairForThisFeatForThisAO.getSecond()));
				}
				else
				{
					allValsForThisFeatureOverList.add(Math.abs(Double.valueOf(valPairForThisFeatForThisAO.getFirst())
							- Double.valueOf(valPairForThisFeatForThisAO.getSecond())));
				}
			}

			DoubleSummaryStatistics summaryStatsForThisFeaturesOverList = allValsForThisFeatureOverList.stream()
					.mapToDouble(Double::doubleValue).summaryStatistics();

			summaryFeatureValsOverAllActsInACand.put(gowallaFeature, summaryStatsForThisFeaturesOverList);
			// summaryStatsForThisFeaturesOverList.maxFeatureDiffOverAllActs.put(gowallaFeature,
			// summaryStatsForThisFeaturesOverList.getMax());
			// minFeatureDiffOverAllActs.put(gowallaFeature, summaryStatsForThisFeaturesOverList.getMin());

			// DoubleSummaryStatistics summaryStatsForThisSquaredFeaturesOverList = featureDifferencesList.stream()
			// .map(listEntry -> listEntry.get(gowallaFeature)).mapToDouble(Double::doubleValue)
			// .map(v -> Math.pow(v, 2)).summaryStatistics();
			// summaryOfSquaredFeatDiffOverAllActsInACand.put(gowallaFeature,
			// summaryStatsForThisSquaredFeaturesOverList);
		}

		// Start of sanity check
		if (false)// Sanity Checked Okay on 22 April 2018//sanity checked okay on 25 Nov 2018
		{
			String debugFileName = Constant.getCommonPath()
					+ "DebugApril16_getSummaryStatsForEachFeatureDiffOverList.csv";
			WToFile.appendLineToFileAbs(
					"\n----------\n" + userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candId + "\n",
					debugFileName);
			WToFile.writeListOfMap2(featureValPairList, debugFileName, "FeatureDiffsListKey-Value", ",", "-", true);
			// WToFile.appendLineToFileAbs("\n", debugFileName);
			WToFile.writeMapToFile(summaryFeatureValsOverAllActsInACand, "GowallaFeature,SummaryStat", ",",
					debugFileName);
			// WToFile.writeMapToFile(summaryOfSquaredFeatDiffOverAllActsInACand, "GowallaFeature,SummaryStatOfSquared",
			// ",", debugFileName);
		}
		// end of sanity check

		return summaryFeatureValsOverAllActsInACand;
		// return new Pair<>(maxFeatureDiffOverAllActs, minFeatureDiffOverAllActs);
	}

	/**
	 * Called for each candidate timelines
	 * <p>
	 * Summary (e.g. max) of feature diff for each of the GowallaFeature. In other words, max diff value for each
	 * feature over corresponding pairwise comparison of each act obj of the two timelines.
	 * <p>
	 * Aggregation stats over list (where each item is a map of feature diffs).
	 * <p>
	 * <b>Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)</b>
	 * 
	 * @param featureDifferencesList
	 *            list of enummaps, where each enumMap contains pairs {feature,diffWithCorresondingAOInCurrentTimeline}
	 * @param userAtRecomm
	 *            for logging only
	 * @param dateAtRecomm
	 *            for logging only
	 * @param timeAtRecomm
	 *            for logging only
	 * @param candId
	 *            for logging only
	 * 
	 * @return one enummap which contains pairs {feature,summaryStatForThatFeature}
	 * @since April 16 2018
	 */
	public static EnumMap<GowGeoFeature, DoubleSummaryStatistics> getSummaryStatsForEachFeatureDiffOverListOfAOsInACand(
			List<EnumMap<GowGeoFeature, Double>> featureDifferencesList, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candId)
	{
		// Max feature diff for each of the GowallaFeature. In other words, max diff value for each feature over
		// corresponding pairwise comparison of each act obj of the two timelines
		EnumMap<GowGeoFeature, DoubleSummaryStatistics> summaryFeatureDiffOverAllActsInACand = new EnumMap<>(
				GowGeoFeature.class);

		// added 23 Nov 2018 NOT USED AT THE MOMENT
		// EnumMap<GowGeoFeature, DoubleSummaryStatistics> summaryOfSquaredFeatDiffOverAllActsInACand = new EnumMap<>(
		// GowGeoFeature.class);
		// EnumMap<GowallaFeatures, Double> minFeatureDiffOverAllActs = new EnumMap<>(GowallaFeatures.class);

		// Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
		Set<GowGeoFeature> listOfGowallaFeatures = featureDifferencesList.get(0).keySet();

		for (GowGeoFeature gowallaFeature : listOfGowallaFeatures)
		{
			DoubleSummaryStatistics summaryStatsForThisFeaturesOverList = featureDifferencesList.stream()
					.map(listEntry -> listEntry.get(gowallaFeature)).mapToDouble(Double::doubleValue)
					.summaryStatistics();

			summaryFeatureDiffOverAllActsInACand.put(gowallaFeature, summaryStatsForThisFeaturesOverList);
			// summaryStatsForThisFeaturesOverList.maxFeatureDiffOverAllActs.put(gowallaFeature,
			// summaryStatsForThisFeaturesOverList.getMax());
			// minFeatureDiffOverAllActs.put(gowallaFeature, summaryStatsForThisFeaturesOverList.getMin());

			// DoubleSummaryStatistics summaryStatsForThisSquaredFeaturesOverList = featureDifferencesList.stream()
			// .map(listEntry -> listEntry.get(gowallaFeature)).mapToDouble(Double::doubleValue)
			// .map(v -> Math.pow(v, 2)).summaryStatistics();
			// summaryOfSquaredFeatDiffOverAllActsInACand.put(gowallaFeature,
			// summaryStatsForThisSquaredFeaturesOverList);

		}

		// Start of sanity check
		if (false)// Sanity Checked Okay on 22 April 2018//sanity checked okay on 25 Nov 2018
		{
			String debugFileName = Constant.getCommonPath()
					+ "DebugApril16_getSummaryStatsForEachFeatureDiffOverList.csv";
			WToFile.appendLineToFileAbs(
					"\n----------\n" + userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candId + "\n",
					debugFileName);
			WToFile.writeListOfMap2(featureDifferencesList, debugFileName, "FeatureDiffsListKey-Value", ",", "-", true);
			// WToFile.appendLineToFileAbs("\n", debugFileName);
			WToFile.writeMapToFile(summaryFeatureDiffOverAllActsInACand, "GowallaFeature,SummaryStat", ",",
					debugFileName);
			// WToFile.writeMapToFile(summaryOfSquaredFeatDiffOverAllActsInACand, "GowallaFeature,SummaryStatOfSquared",
			// ",", debugFileName);
		}
		// end of sanity check

		return summaryFeatureDiffOverAllActsInACand;
		// return new Pair<>(maxFeatureDiffOverAllActs, minFeatureDiffOverAllActs);
	}

	/**
	 * Fork of getSummaryStatsForEachFeatureDiffOverList. (change: instead of each item of given list being an enumMap
	 * of {feature, double}, now each item is an enumMap of {feature,SummaryStat}.)
	 * <p>
	 * Summary (e.g. max) of feature diff for each of the GowallaFeature. In other words, max diff value for each
	 * feature over corresponding pairwise comparison of each act obj of the two timelines.
	 * <p>
	 * Aggregation stats over list (where each item is a map of feature diffs).
	 * <p>
	 * <b>Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)</b>
	 * 
	 * @param featureDifferencesList
	 *            list of enummaps, where each enumMap contains pairs {feature,SummaryStat}
	 * @param minOrMax
	 *            0 for min and 1 for max
	 * @param userAtRecomm
	 *            for logging only
	 * @param dateAtRecomm
	 *            for logging only
	 * @param timeAtRecomm
	 *            for logging only
	 * @return one enummap which contains pairs {feature,maxOfMaxForThatFeature} or {feature,minOfMinForThatFeature}
	 * @since April 17 2018
	 */
	public static EnumMap<GowGeoFeature, Double> getSummaryStatOfSummaryStatForEachFeatureDiffOverList(
			List<EnumMap<GowGeoFeature, DoubleSummaryStatistics>> featureDifferencesList, int minOrMax,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm)
	{
		// Max feature diff for each of the GowallaFeature. In other words, max diff value for each feature over
		// corresponding pairwise comparison of each act obj of the two timelines
		EnumMap<GowGeoFeature, Double> summaryFeatureDiffOverAllActs = new EnumMap<>(GowGeoFeature.class);

		// Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
		Set<GowGeoFeature> listOfGowallaFeatures = featureDifferencesList.get(0).keySet();

		if (minOrMax == 1)
		{
			for (GowGeoFeature gowallaFeature : listOfGowallaFeatures)
			{
				double maxOfMaxForThisFeature = featureDifferencesList.stream()
						.map(listEntry -> listEntry.get(gowallaFeature).getMax()).mapToDouble(Double::doubleValue).max()
						.getAsDouble();
				summaryFeatureDiffOverAllActs.put(gowallaFeature, maxOfMaxForThisFeature);
			}
		}
		else if (minOrMax == 0)
		{
			for (GowGeoFeature gowallaFeature : listOfGowallaFeatures)
			{
				double minOfMinForThisFeature = featureDifferencesList.stream()
						.map(listEntry -> listEntry.get(gowallaFeature).getMin()).mapToDouble(Double::doubleValue).min()
						.getAsDouble();
				summaryFeatureDiffOverAllActs.put(gowallaFeature, minOfMinForThisFeature);
			}

		}

		else
		{
			PopUps.showError("Error: unrecognised minOrMax = " + minOrMax);
		}

		// Start of sanity check
		if (false)// Sanity checked Okay on 22 April 2018 by quick visual inspection
		{
			String debugFileName = Constant.getCommonPath()
					+ "DebugApril17_getSummaryStatOfSummaryStatForEachFeatureDiffOverList" + minOrMax + ".csv";
			WToFile.appendLineToFileAbs("\n--------minOrMax=" + minOrMax + "--\n" + "\n----------\n" + userAtRecomm
					+ "," + dateAtRecomm + "," + timeAtRecomm + "\n", debugFileName);
			WToFile.writeListOfMap2(featureDifferencesList, debugFileName, "FeatureDiffsListKey-Value", ",", "-", true);
			WToFile.appendLineToFileAbs("\n", debugFileName);
			WToFile.writeMapToFile(summaryFeatureDiffOverAllActs, "GowallaFeature,SummaryStat", ",", debugFileName);
		}
		// end of sanity check

		return summaryFeatureDiffOverAllActs;
		// return new Pair<>(maxFeatureDiffOverAllActs, minFeatureDiffOverAllActs);
	}

	// Start of added on 23 Nov 2018
	/**
	 * Convert list of enummaps for each activity object in a candidate. Size = Num of AOs in Cand (list) X (num of
	 * features (each enummap in list))
	 * <p>
	 * to
	 * <p>
	 * enumap of lists , where each list is for a sequence of diff for a feature. Size = Num of features (size of
	 * enumap) X (num of AO (size of each list))
	 * 
	 * @param featureDifferencesList
	 * @return
	 */
	public static EnumMap<GowGeoFeature, List<Double>> convertListOfMapsToMapOfLists(
			List<EnumMap<GowGeoFeature, Double>> featureDifferencesList, boolean writeToFile, String absFileNameToWrite)
	{
		EnumMap<GowGeoFeature, List<Double>> mapOfListOfSequenceForEachFeature = new EnumMap<>(GowGeoFeature.class);
		StringBuilder sb = new StringBuilder();

		// Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
		Set<GowGeoFeature> listOfGowallaFeatures = featureDifferencesList.get(0).keySet();
		String featureNamesString = listOfGowallaFeatures.stream().map(e -> e.toString())
				.collect(Collectors.joining(","));

		for (GowGeoFeature gowallaFeature : listOfGowallaFeatures)
		{
			// for each feature we will have a list.
			List<Double> listOfDiffForThisFeature = featureDifferencesList.stream()
					.map(listEntry -> listEntry.get(gowallaFeature)).collect(Collectors.toList());
			mapOfListOfSequenceForEachFeature.put(gowallaFeature, listOfDiffForThisFeature);
		}

		if (writeToFile)
		{
			String debugFileName = Constant.getCommonPath() + "DebugNov23_convertListOfMapsToMapOfLists.csv";
			WToFile.appendLineToFileAbs("\n----------\n", debugFileName);
			WToFile.writeListOfMap2(featureDifferencesList, debugFileName, "FeatureDiffsListKey-Value", ",", "-", true);
			WToFile.appendLineToFileAbs("\n", debugFileName);
			WToFile.writeMapToFile(mapOfListOfSequenceForEachFeature, "GowallaFeature,FeatureSequence", ",",
					debugFileName);
		}
		// end of sanity check

		return mapOfListOfSequenceForEachFeature;
	}

	// End of added on 23 Nov 2018

	/**
	 * Fork of HJEditDistance.getPthPercentileInRTVerseOfDiffs()
	 * <p>
	 * Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
	 * 
	 * @param listOfListOfFeatDiffs
	 * @param percentile
	 *            in range (0,100]
	 * @param considerValOrValDiff
	 *            TRUE: take summary stat of each feature val, FALSE: take summary stat of diff of feature val pairs
	 *            (added on 15 Feb 2019)
	 * @return
	 * @since 7 Jan 2019
	 */
	public static EnumMap<GowGeoFeature, Double> getPthPercentileInRTVerseOfValPairs(
			List<List<EnumMap<GowGeoFeature, Pair<String, String>>>> listOfListOfFeatValPairs, double percentile,
			boolean considerValOrValDiff)
	{
		// Max feature diff for each of the GowallaFeature. In other words, max diff value for each feature over
		// corresponding pairwise comparison of each act obj of the two timelines
		EnumMap<GowGeoFeature, Double> summaryFeatureDiffOverAllActs = new EnumMap<>(GowGeoFeature.class);

		List<EnumMap<GowGeoFeature, Pair<String, String>>> allCollected = listOfListOfFeatValPairs.stream()
				.flatMap(l -> l.stream()).collect(Collectors.toList());

		// Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
		Set<GowGeoFeature> listOfGowallaFeatures = allCollected.get(0).keySet();

		for (GowGeoFeature gowallaFeature : listOfGowallaFeatures)
		{
			// List<Double> allValsForThisFeature = allCollected.stream().map(listEntry ->
			// listEntry.get(gowallaFeature)).collect(Collectors.toList());
			if (gowallaFeature.equals(GowGeoFeature.StartGeoF) || gowallaFeature.equals(GowGeoFeature.EndGeoF))
			{
				continue;
			}

			List<Double> allValsForThisFeatureV2 = new ArrayList<>();

			for (EnumMap<GowGeoFeature, Pair<String, String>> listEntry : allCollected)
			{
				if (considerValOrValDiff)
				{
					allValsForThisFeatureV2.add(Double.valueOf(listEntry.get(gowallaFeature).getFirst()));
					allValsForThisFeatureV2.add(Double.valueOf(listEntry.get(gowallaFeature).getSecond()));
				}
				else
				{
					allValsForThisFeatureV2.add(Math.abs(Double.valueOf(listEntry.get(gowallaFeature).getFirst())
							- Double.valueOf(listEntry.get(gowallaFeature).getSecond())));
				}
			}
			// allCollected.stream().map(listEntry -> listEntry.get(gowallaFeature))
			// .collect(Collectors.toList());

			Double pthPercentileForThisFeature = StatsUtils.getPercentile(allValsForThisFeatureV2, percentile);
			summaryFeatureDiffOverAllActs.put(gowallaFeature, pthPercentileForThisFeature);
		}

		if (true)// write the pth percentile of each feature
		{
			StringBuilder sb = new StringBuilder("");
			summaryFeatureDiffOverAllActs.entrySet().stream().forEachOrdered(e -> sb.append(e.getValue() + ","));
			WToFile.appendLineToFileAbs(sb.toString() + "\n",
					Constant.getCommonPath() + "PthPercentileOfEachFeature.csv");
		}

		return summaryFeatureDiffOverAllActs;
	}

	/**
	 * <p>
	 * Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
	 * 
	 * @param listOfListOfFeatDiffs
	 * @param percentile
	 *            in range (0,100]
	 * @return
	 * @since May 8 2018
	 */
	public static EnumMap<GowGeoFeature, Double> getPthPercentileInRTVerseOfDiffs(
			List<List<EnumMap<GowGeoFeature, Double>>> listOfListOfFeatDiffs, double percentile)
	{

		// Max feature diff for each of the GowallaFeature. In other words, max diff value for each feature over
		// corresponding pairwise comparison of each act obj of the two timelines
		EnumMap<GowGeoFeature, Double> summaryFeatureDiffOverAllActs = new EnumMap<>(GowGeoFeature.class);

		List<EnumMap<GowGeoFeature, Double>> allCollected = listOfListOfFeatDiffs.stream().flatMap(l -> l.stream())
				.collect(Collectors.toList());

		// Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
		Set<GowGeoFeature> listOfGowallaFeatures = allCollected.get(0).keySet();

		for (GowGeoFeature gowallaFeature : listOfGowallaFeatures)
		{
			List<Double> allValsForThisFeature = allCollected.stream().map(listEntry -> listEntry.get(gowallaFeature))
					.collect(Collectors.toList());

			Double pthPercentileForThisFeature = StatsUtils.getPercentile(allValsForThisFeature, percentile);
			summaryFeatureDiffOverAllActs.put(gowallaFeature, pthPercentileForThisFeature);
		}

		if (true)// write the pth percentile of each feature
		{
			StringBuilder sb = new StringBuilder("");
			summaryFeatureDiffOverAllActs.entrySet().stream().forEachOrdered(e -> sb.append(e.getValue() + ","));
			WToFile.appendLineToFileAbs(sb.toString() + "\n",
					Constant.getCommonPath() + "PthPercentileOfEachFeature.csv");
		}

		return summaryFeatureDiffOverAllActs;
	}

	// /**
	// * <b>Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)</b>
	// *
	// * @param <V>
	// * @param <K>
	// *
	// * @param featureDifferencesList
	// * @since April 16 2018
	// */
	// public static <V, K> void getMinMaxOverListOfMaps(List<Map<K, V>> listOfMaps)
	// {
	// // Max feature diff for each of the GowallaFeature. In other words, max diff value for each feature over
	// // corresponding pairwise comparison of each act obj of the two timelines
	// Map<K, V> maxFeatureDiffOverAllActs = new EnumMap<>(GowallaFeatures.class);
	// Map<K, V> minFeatureDiffOverAllActs = new EnumMap<>(GowallaFeatures.class);
	//
	// // Assuming that all maps in the list contain the same set of keys (i.e., same GowallaFeatures)
	// Set<GowallaFeatures> listOfGowallaFeatures = featureDifferencesList.get(0).keySet();
	//
	// for (GowallaFeatures gowallaFeature : listOfGowallaFeatures)
	// {
	// DoubleSummaryStatistics summaryStatsForThisFeaturesOverList = featureDifferencesList.stream()
	// .map(listEntry -> listEntry.get(gowallaFeature)).mapToDouble(Double::doubleValue)
	// .summaryStatistics();
	//
	// maxFeatureDiffOverAllActs.put(gowallaFeature, summaryStatsForThisFeaturesOverList.getMax());
	// minFeatureDiffOverAllActs.put(gowallaFeature, summaryStatsForThisFeaturesOverList.getMin());
	// }
	//
	// }

	/**
	 * Fork of getFeatureLevelDiffsBetweenAllAOsV2
	 * <p>
	 * No sublisting required
	 * 
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return List{EnumMap{GowallaFeatures, pairs of Doubles}} featureDifferences
	 *         <p>
	 *         List of EnumMap of {GowallaFeatures, Val pairs for ThatFeature} one for each corresponding AO comparison}
	 *         <p>
	 * 
	 * @since Jan 5 2019
	 */
	public List<EnumMap<GowGeoFeature, Pair<String, String>>> getFeatureLevelPairsBetweenAllAOsV2(
			ArrayList<ActivityObject2018> actObjs1, ArrayList<ActivityObject2018> actObjs2)
	{

		int ao1Size = actObjs1.size();
		int ao2Size = actObjs2.size();

		List<EnumMap<GowGeoFeature, Pair<String, String>>> featureDifferencesList = new ArrayList<>(
				Math.max(ao1Size, ao2Size));

		// here: get max for each feature over these act objsSkerr

		StringBuilder sbtt1 = new StringBuilder();
		if (false)// debug Mar17 2018 //Sanity Checked ok
		{
			sbtt1.append("\n\nactivityObjects1=" + "" + getActIDsAsString(actObjs1) + "\nactivityObjects2="
					+ getActIDsAsString(actObjs2));
			sbtt1.append("\nao1Size=" + ao1Size + "\nao2Size=" + ao2Size + "  equalSize=" + (ao1Size == ao2Size));
		}

		int minSize = Math.min(ao1Size, ao2Size);
		sbtt1.append("\nminSize=" + minSize + "\n");

		for (int minIter = 0; minIter < minSize; minIter++)
		{
			// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
			ActivityObject2018 ao1ToCompare = actObjs1.get(ao1Size - 1 - minIter);
			ActivityObject2018 ao2ToCompare = actObjs2.get(ao2Size - 1 - minIter);
			// $sbtt1.append(ao1ToCompare.getActivityID() + "--" + ao2ToCompare.getActivityID() + "\n");
			// Pairwise compare the last minSize AOs
			// featureDifferencesList.add(getFeatureLevelDifferenceGowallaPD13Apr2018(ao1ToCompare, ao2ToCompare));
			featureDifferencesList.add(getFeatureLevelValPairsBetweenAOs(ao1ToCompare, ao2ToCompare));
		}

		// if (dFeat == 0)
		// {
		// StringBuilder sb = new StringBuilder();
		// sb.append("\naos1ToCompare=" + aos1ToCompare + "\naos2ToCompare=" + aos2ToCompare);
		// WritingToFile.appendLineToFileAbsolute(sb.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar17_2018EDWhyFEDIS0.csv");
		// }

		// if (true)
		// {
		// WritingToFile.appendLineToFileAbsolute(sbtt1.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar22_2018EDFeatureLevel.csv");
		// }
		// Because they are in reverse order. The one for last act obj as first item in list. Now we reverse it and get
		// it in initial order.
		Collections.reverse(featureDifferencesList);
		return featureDifferencesList;
	}

	/**
	 * No sublisting required
	 * 
	 * 
	 * @param actObjs1
	 * @param actObjs2
	 * @return List{EnumMap{GowallaFeatures, Double}} featureDifferences
	 *         <p>
	 *         List of EnumMap of {GowallaFeatures, DiffForThatFeature} one for each corresponding AO comparison}
	 *         <p>
	 * 
	 * @since April 14 2018
	 */
	public List<EnumMap<GowGeoFeature, Double>> getFeatureLevelDiffsBetweenAllAOsV2(
			ArrayList<ActivityObject2018> actObjs1, ArrayList<ActivityObject2018> actObjs2)
	{

		int ao1Size = actObjs1.size();
		int ao2Size = actObjs2.size();

		List<EnumMap<GowGeoFeature, Double>> featureDifferencesList = new ArrayList<>(Math.max(ao1Size, ao2Size));

		// here: get max for each feature over these act objsSkerr

		StringBuilder sbtt1 = new StringBuilder();
		if (false)// debug Mar17 2018 //Sanity Checked ok
		{
			sbtt1.append("\n\nactivityObjects1=" + "" + getActIDsAsString(actObjs1) + "\nactivityObjects2="
					+ getActIDsAsString(actObjs2));
			sbtt1.append("\nao1Size=" + ao1Size + "\nao2Size=" + ao2Size + "  equalSize=" + (ao1Size == ao2Size));
		}

		int minSize = Math.min(ao1Size, ao2Size);
		sbtt1.append("\nminSize=" + minSize + "\n");

		for (int minIter = 0; minIter < minSize; minIter++)
		{
			// System.out.println("coordOfAO1="+coordOfAO1+" coordOfAO2="+coordOfAO2);
			ActivityObject2018 ao1ToCompare = actObjs1.get(ao1Size - 1 - minIter);
			ActivityObject2018 ao2ToCompare = actObjs2.get(ao2Size - 1 - minIter);
			// $sbtt1.append(ao1ToCompare.getActivityID() + "--" + ao2ToCompare.getActivityID() + "\n");
			// Pairwise compare the last minSize AOs
			// featureDifferencesList.add(getFeatureLevelDifferenceGowallaPD13Apr2018(ao1ToCompare, ao2ToCompare));
			featureDifferencesList.add(getFeatureLevelDiffsBetweenAOs(ao1ToCompare, ao2ToCompare));
		}

		// if (dFeat == 0)
		// {
		// StringBuilder sb = new StringBuilder();
		// sb.append("\naos1ToCompare=" + aos1ToCompare + "\naos2ToCompare=" + aos2ToCompare);
		// WritingToFile.appendLineToFileAbsolute(sb.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar17_2018EDWhyFEDIS0.csv");
		// }

		// if (true)
		// {
		// WritingToFile.appendLineToFileAbsolute(sbtt1.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar22_2018EDFeatureLevel.csv");
		// }
		// Because they are in reverse order. The one for last act obj as first item in list. Now we reverse it and get
		// it in initial order.
		Collections.reverse(featureDifferencesList);
		return featureDifferencesList;
	}

	public static String getActIDsAsString(List<ActivityObject2018> aos)
	{
		StringBuilder sb = new StringBuilder();
		aos.stream().forEachOrdered(ao -> sb.append(">" + ao.getActivityID()));
		return sb.toString();
	}

	/**
	 * 
	 * @param dAct
	 * @param dFeat
	 * @param size1
	 * @param size2
	 * @param alpha
	 * 
	 * @return
	 */
	/**
	 * 
	 * @param dAct
	 * @param dFeat
	 * @param size1
	 * @param size2
	 * @param alpha
	 * @param userAtRecomm
	 *            just for writing
	 * @param dateAtRecomm
	 *            just for writing
	 * @param timeAtRecomm
	 *            just for writing
	 * @param candidateTimelineId
	 *            just for writing
	 * @return
	 */
	private double combineActAndFeatLevelDistance(double dAct, double dFeat, int size1, int size2, double alpha,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
	{
		double distanceTotal = -9999, normalisedDACt = -9999, normalisedDFeat = -9999;
		// (length of current timeline-1)*replaceWt*WtObj
		double maxActLevelDistance = Math.max((Math.max(size1, size2) - 1), 1) * costReplaceActivityObject;
		// = (length of current timeline)*(wtStartTime + wtLocation + wtLocPopularity)

		// Disabled on April 10 2018 as this is not regulated by which features to use:
		// double maxFeatLevelDistance = Math.max(size1, size2) * (wtStartTime + wtLocation + wtLocPopularity);

		// added on April 10 2018
		double maxFeatLevelDistance = Math.max(size1, size2) * this.getSumOfWeightOfFeaturesExceptPrimaryDimension();

		// Sanity check start: Okay as of April 10 2018
		// System.out.println("\ndAct" + dAct + " maxActLevelDistance=" + maxActLevelDistance + " dFeat=" + dFeat
		// + " maxFeatLevelDistance=" + maxFeatLevelDistance + " size1=" + size1 + " size2=" + size2 + " alpha="
		// + alpha + "\ncostReplaceActivityObject=" + costReplaceActivityObject
		// + "\tgetSumOfWeightOfFeaturesExceptPrimaryDimension="
		// + getSumOfWeightOfFeaturesExceptPrimaryDimension());
		// Sanity check end

		if (dAct > maxActLevelDistance || dFeat > maxFeatLevelDistance)
		{
			// tolerate some difference 17.8 vs 17.7999997 etc
			double epsilon = Constant.epsilonForFloatZeroRelaxed;
			if (Math.abs(dAct - maxActLevelDistance) <= epsilon)
			{
				maxActLevelDistance = dAct;
			}
			else
			{
				PopUps.printTracedErrorMsg("Error in combineActAndFeatLevelDistance : dAct" + dAct
						+ " maxActLevelDistance=" + maxActLevelDistance + " dFeat=" + dFeat + " maxFeatLevelDistance="
						+ maxFeatLevelDistance + " size1=" + size1 + " size2=" + size2 + " alpha=" + alpha
						+ "(dAct - maxActLevelDistance) = " + (dAct - maxActLevelDistance) + " epsilon = " + epsilon);
				return -1;
			}

			if (Math.abs(dFeat - maxFeatLevelDistance) <= epsilon)
			{
				maxFeatLevelDistance = dFeat;
			}
			else
			{
				PopUps.printTracedErrorMsg("Error in combineActAndFeatLevelDistance : dAct" + dAct
						+ " maxActLevelDistance=" + maxActLevelDistance + " dFeat=" + dFeat + " maxFeatLevelDistance="
						+ maxFeatLevelDistance + " size1=" + size1 + " size2=" + size2 + " alpha=" + alpha
						+ "(dAct - maxActLevelDistance) = " + (dAct - maxActLevelDistance) + " epsilon = " + epsilon);
				return -1;
			}

		}

		normalisedDACt = (dAct / maxActLevelDistance);
		// when FED should not be computed,dFeat is made 0 . Also in that case
		// getSumOfWeightOfFeaturesExceptPrimaryDimension()=0, hence we need to take care to avoid division by zero
		normalisedDFeat = (dFeat == 0) ? 0 : (dFeat / maxFeatLevelDistance);

		distanceTotal = alpha * normalisedDACt + (1 - alpha) * normalisedDFeat;

		if (!Constant.disableRoundingEDCompute)
		{
			distanceTotal = StatsUtils.round(distanceTotal, Constant.RoundingPrecision);
		}
		if (VerbosityConstants.verboseCombinedEDist || VerbosityConstants.verboseDistDistribution)
		{
			String infoHeader = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candidateTimelineId;
			WToFile.appendLineToFileAbs(
					infoHeader + "," + normalisedDACt + "," + normalisedDFeat + "," + distanceTotal + "," + dAct + ","
							+ dFeat + "," + size1 + "," + size2 + "\n",
					Constant.getCommonPath() + "DistanceDistribution.csv");// primarily for correlated analysis
			// indexOfUserID = 0,indexOfNormAED = 5, indexOfNormFED = 6, indexOfTotalDis = 7, indexOfRawAED = 8,
			// indexOfRawFED = 9,
			// WToFile.appendLineToFileAbs(
			// distanceTotal + "," + dAct + "," + dFeat + "," + size1 + "," + size2 + "," + normalisedDACt + ","
			// + normalisedDFeat + "," + distanceTotal + "\n",
			// Constant.getCommonPath() + "DistanceTotalAlpha" + alpha + ".csv");
		}
		if (VerbosityConstants.verboseDistance)
		{
			System.out.println("alpha= " + alpha + ". dAct=" + dAct + ",dFeat=" + dFeat + ",maxActLevelDistance="
					+ maxActLevelDistance + ",maxFeatLevelDistance=" + maxFeatLevelDistance + ",size1=" + size1
					+ ",size2=" + size2 + ",costReplaceActivityObject=" + costReplaceActivityObject
					+ ",getSumOfWeightOfFeaturesExceptPrimaryDimension()="
					+ getSumOfWeightOfFeaturesExceptPrimaryDimension() + ",normalisedDACt=" + normalisedDACt
					+ ",normalisedDFeat=" + normalisedDFeat + "," + distanceTotal);
		}

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
