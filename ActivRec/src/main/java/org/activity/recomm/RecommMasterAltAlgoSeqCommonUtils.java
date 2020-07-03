package org.activity.recomm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.CaseType;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.spmf.AKOMSeqPredictorLighter;
import org.activity.ui.PopUps;
import org.activity.util.TimelineTransformers;

/**
 * Create to keep common methods together for safer and efficient refactorings
 * 
 * @author gunjan
 *
 */
public class RecommMasterAltAlgoSeqCommonUtils
{

	/**
	 * reuse already trained AKOM or train a new one for each user and get top-1 prediction using
	 * getAKOMTopKPredictions()
	 * 
	 * @param highestOrder
	 * @param userID
	 * @param currSeq
	 * @param candidateTimelinesWithNextAppended
	 * @param alternateSeqPredictor
	 * @param givenDimension
	 *            //added on 5 Aug 2018
	 * @return
	 * @throws Exception
	 * @deprecated on April 24 in favour of getAKOMTopKPredictedSymbols() which predicts top-K instead of top-1 symbols
	 */
	protected static int getAKOMPredictedSymbol(int highestOrder, String userID, ArrayList<Integer> currSeq,
			LinkedHashMap<String, Timeline> candidateTimelinesWithNextAppended,
			Enums.AltSeqPredictor alternateSeqPredictor, PrimaryDimension givenDimension) throws Exception// ArrayList<ArrayList<Integer>>
	// candTimelinesAsSeq
	{
		ArrayList<ArrayList<Integer>> candTimelinesAsSeq = new ArrayList<>();

		int predSymbol = -1;
		AKOMSeqPredictorLighter seqPredictor = null;
		boolean savedReTrain = false;
		AKOMSeqPredictorLighter sanityCheckSeqPredictor = null;

		if (Constant.sameAKOMForAllRTsOfAUser && alternateSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM))
		{
			seqPredictor = AKOMSeqPredictorLighter.getSeqPredictorsForEachUserStored(userID, givenDimension);
			if (seqPredictor == null) // AKOM NOT already trained for this user
			{
				for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
				{
					candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfGivenDimensionVals(
							candT.getValue().getActivityObjectsInTimeline(), VerbosityConstants.verbose,
							givenDimension));
				}
				seqPredictor = new AKOMSeqPredictorLighter(candTimelinesAsSeq, highestOrder,
						VerbosityConstants.verboseAKOM, userID, givenDimension);// verbose);
			}
			else
			{
				savedReTrain = true;
				System.out.println("Ajooba: already trained AKOM for this user:" + userID);
			}
		}
		else
		{
			for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
			{
				candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfGivenDimensionVals(
						candT.getValue().getActivityObjectsInTimeline(), VerbosityConstants.verbose, givenDimension));
			}
			seqPredictor = new AKOMSeqPredictorLighter(candTimelinesAsSeq, highestOrder, VerbosityConstants.verboseAKOM,
					userID, givenDimension);// verbose);
		}

		predSymbol = seqPredictor.getAKOMPrediction(currSeq, VerbosityConstants.verboseAKOM);// verbose);

		// Start of Sanity CHeck
		// if (savedReTrain)// PASSED
		// {
		// // training again and checking if predSYmbol same as fetched from pretrained model
		// for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
		// {
		// candTimelinesAsSeq.add(TimelineTransformers
		// .timelineToSeqOfActIDs(candT.getValue().getActivityObjectsInTimeline(), false));
		// }
		// AKOMSeqPredictor sanityCheckseqPredictor = new AKOMSeqPredictor(candTimelinesAsSeq, highestOrder, false,
		// userID);// verbose);
		// int sanityCheckPredSymbol = seqPredictor.getAKOMPrediction(currSeq, false);// verbose);
		//
		// Sanity.eq(sanityCheckPredSymbol, predSymbol,
		// "Sanity Error sanityCheckPredSymbol=" + sanityCheckPredSymbol + "!= predSymbol" + predSymbol);
		// System.out.println(
		// "SanityCHeck sanityCheckPredSymbol=" + sanityCheckPredSymbol + ", predSymbol=" + predSymbol);
		// }
		// End of Sanity check

		return predSymbol;
	}

	/**
	 * Fork of getAKOMPredictedSymbol to allow for top-K prediction instead of top-1 prediction
	 * <p>
	 * reuse already trained AKOM or train a new one for each user and get top-K predictions using
	 * getAKOMTopKPredictions()
	 * 
	 * @param highestOrder
	 * @param userID
	 * @param currSeq
	 * @param candidateTimelinesWithNextAppended
	 * @param alternateSeqPredictor
	 * @param givenDimension
	 *            //added on 5 Aug 2018
	 * @return sorted map of {predicted value, count (score)}
	 * @throws Exception
	 * @since April 24 2019
	 */
	protected static LinkedHashMap<Integer, Double> getAKOMTopKPredictedSymbols(int highestOrder, String userID,
			ArrayList<Integer> currSeq, LinkedHashMap<String, Timeline> candidateTimelinesWithNextAppended,
			Enums.AltSeqPredictor alternateSeqPredictor, PrimaryDimension givenDimension) throws Exception// ArrayList<ArrayList<Integer>>
	// candTimelinesAsSeq
	{
		ArrayList<ArrayList<Integer>> candTimelinesAsSeq = new ArrayList<>();

		// int predSymbol = -1;
		// ArrayList<Integer> predSymbols = null;

		AKOMSeqPredictorLighter seqPredictor = null;
		boolean savedReTrain = false;
		AKOMSeqPredictorLighter sanityCheckSeqPredictor = null;

		///// Start of reusing already trained AKOM or training a new one for each user
		if (Constant.sameAKOMForAllRTsOfAUser && alternateSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM))
		{
			seqPredictor = AKOMSeqPredictorLighter.getSeqPredictorsForEachUserStored(userID, givenDimension);
			if (seqPredictor == null) // AKOM NOT already trained for this user
			{
				for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
				{
					candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfGivenDimensionVals(
							candT.getValue().getActivityObjectsInTimeline(), VerbosityConstants.verbose,
							givenDimension));
				}
				seqPredictor = new AKOMSeqPredictorLighter(candTimelinesAsSeq, highestOrder,
						VerbosityConstants.verboseAKOM, userID, givenDimension);// verbose);
			}
			else
			{
				savedReTrain = true;
				System.out.println("Ajooba: already trained AKOM for this user:" + userID);
			}
		}
		else
		{
			for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
			{
				candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfGivenDimensionVals(
						candT.getValue().getActivityObjectsInTimeline(), VerbosityConstants.verbose, givenDimension));
			}
			seqPredictor = new AKOMSeqPredictorLighter(candTimelinesAsSeq, highestOrder, VerbosityConstants.verboseAKOM,
					userID, givenDimension);// verbose);
		}
		///// End of reusing already trained AKOM or training a new one for each user

		LinkedHashMap<Integer, Double> predSymbols = seqPredictor.getAKOMTopKPredictions(currSeq,
				VerbosityConstants.verboseAKOM);// verbose);

		// Start of Sanity CHeck
		// if (savedReTrain)// PASSED
		// {
		// // training again and checking if predSYmbol same as fetched from pretrained model
		// for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
		// {
		// candTimelinesAsSeq.add(TimelineTransformers
		// .timelineToSeqOfActIDs(candT.getValue().getActivityObjectsInTimeline(), false));
		// }
		// AKOMSeqPredictor sanityCheckseqPredictor = new AKOMSeqPredictor(candTimelinesAsSeq, highestOrder, false,
		// userID);// verbose);
		// int sanityCheckPredSymbol = seqPredictor.getAKOMPrediction(currSeq, false);// verbose);
		//
		// Sanity.eq(sanityCheckPredSymbol, predSymbol,
		// "Sanity Error sanityCheckPredSymbol=" + sanityCheckPredSymbol + "!= predSymbol" + predSymbol);
		// System.out.println(
		// "SanityCHeck sanityCheckPredSymbol=" + sanityCheckPredSymbol + ", predSymbol=" + predSymbol);
		// }
		// End of Sanity check

		return predSymbols;
	}

	/**
	 * 
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param lookPastType
	 * @param candidateTimelines
	 * @param constantValScore
	 * @param verbose
	 * @param highestOrder
	 * @param userID
	 * @param alternateSeqPredictor
	 * @param givenDimension
	 *            //added on 5 Aug 2018
	 * @return
	 * @throws Exception
	 * @deprecated on April 24 2019 in favour of getTopKPredictedAKOMActivityPDVals() which recommended top-K next
	 *             symbols instead of top-1
	 */
	public static LinkedHashMap<String, Double> getTopPredictedAKOMActivityPDVals(
			ArrayList<ActivityObject2018> activitiesGuidingRecomm, CaseType caseType, LookPastType lookPastType,
			LinkedHashMap<String, Timeline> candidateTimelines, double constantValScore, boolean verbose,
			int highestOrder, String userID, Enums.AltSeqPredictor alternateSeqPredictor,
			PrimaryDimension givenDimension) throws Exception
	{
		LinkedHashMap<String, Double> res = new LinkedHashMap<>();

		if (lookPastType.equals(Enums.LookPastType.Daywise) || lookPastType.equals(Enums.LookPastType.NCount)
				|| lookPastType.equals(Enums.LookPastType.NHours))
		{
			// System.out.println("Current timeline:");
			// Convert current timeline to a seq of integers
			ArrayList<Integer> currSeq = TimelineTransformers
					.listOfActObjsToListOfGivenDimensionVals(activitiesGuidingRecomm, verbose, givenDimension);
			// .listOfActObjsToListOfActIDs(activitiesGuidingRecomm,false);

			// if NCount matching, then the next activity should be included in the training seq.
			LinkedHashMap<String, Timeline> candidateTimelinesWithNextAppended = candidateTimelines;

			// However, for Pure AKOM approach there is no Next act since the candidate timeline from each user is its
			// entire trainining timeline (reduced or not reduced)
			if (alternateSeqPredictor.equals(Enums.AltSeqPredictor.AKOM)
					&& !alternateSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM))
			{
				for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
				{
					TimelineWithNext t = (TimelineWithNext) candT.getValue();
					t.appendAO(t.getNextActivityObject());
				}
			}

			// Convert cand timeline to a list of seq of integers
			// On 15 Dec 2017 removed candTimelinesAsSeq to be as internal to getAKOMPredictedSymbol()
			// ArrayList<ArrayList<Integer>> candTimelinesAsSeq = new ArrayList<>();

			// System.out.println("Cand timelines:");

			// System.out.println("predictedNextSymbol = ");
			// TimelineTransformers.timelineToSeqOfActIDs(timeline, delimiter)
			int predSymbol = getAKOMPredictedSymbol(highestOrder, userID, currSeq, candidateTimelinesWithNextAppended,
					alternateSeqPredictor, givenDimension);
			// candTimelinesAsSeq);

			// System.out.println("predictedNextSymbol = " +
			// SeqPredictor p = new SeqPredictor(candTimelinesAsSeq, currSeq, highestOrder, verbose);
			Integer predictedGivenDimensionVal = Integer.valueOf(predSymbol);
			System.out.println("givenDimension = " + givenDimension + "\npredictedGivenDimensionVal = "
					+ predictedGivenDimensionVal.toString());

			if (predictedGivenDimensionVal.equals(-1))
			{
				System.out.println("Return null, no predictions");
				return null;
			}

			res.put(predictedGivenDimensionVal.toString(), constantValScore);
			return res;
			// return createRankedTopRecommendedActivityNamesClosestTime(distancesSortedMap);
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg("Error:unrecognised lookpast type = " + lookPastType));
			return null;
		}

	}

	/**
	 * Fork of getTopPredictedAKOMActivityPDVals() to predict top-K symbols instead of top-1 symbols
	 * 
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param lookPastType
	 * @param candidateTimelines
	 * @param constantValScore
	 * @param verbose
	 * @param highestOrder
	 * @param userID
	 * @param alternateSeqPredictor
	 * @param givenDimension
	 *            //added on 5 Aug 2018
	 * @return
	 * @throws Exception
	 * @since April 24 2019
	 */
	public static LinkedHashMap<String, Double> getTopKPredictedAKOMActivityPDVals(
			ArrayList<ActivityObject2018> activitiesGuidingRecomm, CaseType caseType, LookPastType lookPastType,
			LinkedHashMap<String, Timeline> candidateTimelines, /* double constantValScore, */ boolean verbose,
			int highestOrder, String userID, Enums.AltSeqPredictor alternateSeqPredictor,
			PrimaryDimension givenDimension) throws Exception
	{
		LinkedHashMap<String, Double> res = null;// new LinkedHashMap<>();

		if (lookPastType.equals(Enums.LookPastType.Daywise) || lookPastType.equals(Enums.LookPastType.NCount)
				|| lookPastType.equals(Enums.LookPastType.NHours))
		{
			// System.out.println("Current timeline:");
			// Convert current timeline to a seq of integers
			ArrayList<Integer> currSeq = TimelineTransformers
					.listOfActObjsToListOfGivenDimensionVals(activitiesGuidingRecomm, verbose, givenDimension);
			// .listOfActObjsToListOfActIDs(activitiesGuidingRecomm,false);

			// if NCount matching, then the next activity should be included in the training seq.
			LinkedHashMap<String, Timeline> candidateTimelinesWithNextAppended = candidateTimelines;

			// However, for Pure AKOM approach there is no Next act since the candidate timeline from each user is its
			// entire trainining timeline (reduced or not reduced)
			if (alternateSeqPredictor.equals(Enums.AltSeqPredictor.AKOM)
					&& !alternateSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM))
			{
				for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
				{
					TimelineWithNext t = (TimelineWithNext) candT.getValue();
					t.appendAO(t.getNextActivityObject());
				}
			}

			// Convert cand timeline to a list of seq of integers
			// On 15 Dec 2017 removed candTimelinesAsSeq to be as internal to getAKOMPredictedSymbol()
			// ArrayList<ArrayList<Integer>> candTimelinesAsSeq = new ArrayList<>();

			// System.out.println("Cand timelines:");

			// System.out.println("predictedNextSymbol = ");
			// TimelineTransformers.timelineToSeqOfActIDs(timeline, delimiter)
			LinkedHashMap<Integer, Double> predSymbols = getAKOMTopKPredictedSymbols(highestOrder, userID, currSeq,
					candidateTimelinesWithNextAppended, alternateSeqPredictor, givenDimension);
			// candTimelinesAsSeq);

			// System.out.println("predictedNextSymbol = " +
			// SeqPredictor p = new SeqPredictor(candTimelinesAsSeq, currSeq, highestOrder, verbose);

			if (predSymbols != null)
			{
				res = new LinkedHashMap<>();
				for (Entry<Integer, Double> e : predSymbols.entrySet())
				{
					Integer predictedGivenDimensionVal = e.getKey();
					System.out.println("givenDimension = " + givenDimension + "\npredictedGivenDimensionVal = "
							+ predictedGivenDimensionVal.toString());

					if (predictedGivenDimensionVal == null || predictedGivenDimensionVal.equals(-1))
					{
						PopUps.printTracedErrorMsg(
								"Error in getTopKPredictedAKOMActivityPDVals(): predictedGivenDimensionVal == null || predictedGivenDimensionVal.equals(-1)");
						// System.out.println("Return null, no predictions");
						// return null;
					}
					res.put(predictedGivenDimensionVal.toString(), e.getValue());
				}
			}
			return res;
			// return createRankedTopRecommendedActivityNamesClosestTime(distancesSortedMap);
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg("Error:unrecognised lookpast type = " + lookPastType));
			return null;
		}

	}

}
