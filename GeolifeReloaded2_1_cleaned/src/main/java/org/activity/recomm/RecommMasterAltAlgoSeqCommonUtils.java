package org.activity.recomm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.AltSeqPredictor;
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

}
