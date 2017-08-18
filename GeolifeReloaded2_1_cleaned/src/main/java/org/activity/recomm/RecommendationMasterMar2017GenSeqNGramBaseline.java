package org.activity.recomm;

//TODO:enable:temporarily disabled on Aug 16,2017

// import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.VerbosityConstants;
import org.activity.evaluation.Evaluation;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.sanityChecks.TimelineSanityChecks;
import org.activity.stats.TimelineStats;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.StringUtils;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

/***
 * For a given current timeline,recommend the second act of most frequentally occuring two-gram with the
 * current*activity as the first act of that 2-gram**
 * 
 * @author gunjan
 **/
public class RecommendationMasterMar2017GenSeqNGramBaseline implements RecommendationMasterI// IRecommenderMaster
{

	// (UserID,(nOfN-Gram,(ngram,count)))
	private static LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, Long>>> allNGramData;

	// // (UserID,(ActID,NextActIDBasedOnMost2-gramCount)))
	// private static LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> nextMostFreqActBasedOn2Grams;

	// (UserID,(nOfN-Gram,(CurrentSubNGram,ListOfActsIDsWithMaxCountAsCHarCode))
	private static LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<Character>>>> nextMostFreqActsBasedOnNGrams = new LinkedHashMap<>();

	// (UserID,(nOfN-Gram,(CurrentSubNGram,ListOfActsIDsWithMaxCountAsActID))
	private static LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<Integer>>>> nextMostFreqActsBasedOnNGramsActID = new LinkedHashMap<>();

	// (UserID,(nOfN-Gram,(CurrentSubNGram,ListOfActsIDsWithMaxCountAsActName))
	private static LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<String>>>> nextMostFreqActsBasedOnNGramsActName = new LinkedHashMap<>();

	// // (UserID,(nOfN-Gram,(currentActID,NextActID)))
	// private static LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<Integer, Integer>>>
	// nextMostFreqActBasedOnNGrams;

	static int maxNOfNGrams = 2;

	// private double matchingUnitInCountsOrHours;
	// private double reductionInMatchingUnit = 0;
	// private Timeline trainingTimeline;
	// private Timeline testTimeline;
	// in case of MU approach {String, TimelineWithNext}, in case if daywise approach {Date as String, Timeline}
	private LinkedHashMap<String, Timeline> candidateTimelines;
	// here key is the TimelineID, which is already a class variable of Value,
	// So we could have used ArrayList but we used LinkedHashMap purely for search performance reasons

	private Date dateAtRecomm;
	private Time timeAtRecomm;
	private String userAtRecomm;
	private String userIDAtRecomm;

	/**
	 * Current Timeline sequence of activity objects happening from the recomm point back until the matching unit
	 */
	// private TimelineWithNext currentTimeline; // =current timelines
	private ArrayList<ActivityObject> activitiesGuidingRecomm; // Current Timeline ,
	private ActivityObject activityAtRecommPoint; // current Activity Object
	private String activityNameAtRecommPoint;// current Activity Name

	/**
	 * {Cand TimelineID, Pair{Trace,Edit distance}} this LinkedHashMap is sorted by the value of edit distance in
	 * ascending order://formerly: editDistancesSortedMapFullCand
	 */
	// private LinkedHashMap<String, Pair<String, Double>> distancesSortedMap;

	/**
	 * Relevant for daywise approach: the edit distance is computed for least distance subsequence in case current
	 * activity name occurs at multiple times in a day timeline {Cand TimelineID, End point index of least distant
	 * subsequence}
	 */
	// private LinkedHashMap<String, Integer> endPointIndicesConsideredInCands;

	// private LinkedHashMap<String, ActivityObject> endPointActivityObjectsInCands;

	/*
	 * List of of top next activity objects with their edit distances and the timeline id of the candidate producing
	 * them. Triple <Next Activity Object,edit distance, TimelineID> formerly ArrayList<Triple<ActivityObject, Double,
	 * String>> nextActivityObjectsFromCands;// topNextActivityObjects
	 */
	/**
	 * List of of top next activity objects with their edit distances and the timeline id of the candidate producing
	 * them. {TimelineID, {Next Activity Object,edit distance}}
	 */
	// private LinkedHashMap<String, Pair<ActivityObject, Double>> nextActivityObjectsFromCands;

	/**
	 * This is only relevant when case type is 'CaseBasedV1' (Cand TimeineId, Edit distance of the end point activity
	 * object of this candidate timeline with the current activity object(activity at recomm point))
	 * <p>
	 * <font color = orange>currently its a similarity and not edit distance</font>
	 */
	// private LinkedHashMap<String, Double> similarityOfEndPointActivityObjectCand;

	/**
	 * Recommended Activity names with their rank score
	 */
	private LinkedHashMap<String, Double> recommendedActivityNamesWithRankscores;
	private String rankedRecommendedActNamesWithRankScoresStr;
	private String rankedRecommendedActNamesWithoutRankScoresStr;

	private boolean hasCandidateTimelines, hasCandidateTimelinesBelowThreshold;

	private boolean nextActivityJustAfterRecommPointIsInvalid;
	// private double thresholdAsDistance = Double.MAX_VALUE;

	// private int fullCandOrSubCand; //0 means full cand .... 1 means subcand
	// private boolean thresholdPruningNoEffect;

	public Enums.LookPastType lookPastType;// String
	// public int totalNumberOfProbableCands; // public int numCandsRejectedDueToNoCurrentActivityAtNonLast;
	// public int numCandsRejectedDueToNoNextActivity;
	// candidateTimelinesStatus; //1 for has candidate timelines, -1 for no candidate timelines because no past timeline
	// with current act, -2 for no candodate timelines because

	// Enums.CaseType caseType; // String 'SimpleV3' for no case case, 'CaseBasedV1' for first cased based
	// implementation

	// public static String commonPath ;//= Constant.commonPath;

	private boolean errorExists;

	// private ArrayList<Double> normEditSimilarity, simEndActivityObjForCorr;
	// public double percentageDistanceThresh;
	/*
	 * threshold for choosing candidate timelines, those candidate timelines whose distance from the 'activity objects
	 * guiding recommendations' is higher than the cost of replacing 'percentageDistanceThresh' % of Activity Objects in
	 * the activities guiding recommendation are pruned out from set of candidate timelines
	 */
	public LinkedHashMap<String, String> getCandUserIDs()
	{
		return null;// NOT IMPLEMENENTED
	}

	/**
	 * <p>
	 * Example:<br>
	 * 2-Gram, freq of that 2gram (ab,3;ac,29;ad,10) <br>
	 * expanded 2 gram (a--(b,3);a--(c,29);a--(d,10))
	 *
	 * @param userAtRecomm
	 * @param nGramCountsFromTrainingTimelines
	 *            e.g. for freq of that 2gram (ab,3;ac,29;ad,10), for freq of that 3gram (abc,3;abd,29;abe,10)
	 * @param maxNOfNGrams
	 * @return e.g. for expanded 2 gram (a--(b,3);a--(c,29);a--(d,10)), for expanded 3 gram
	 *         (ab--(c,3);ab--(d,29);ab--(e,10))
	 */
	private static LinkedHashMap<Integer, HashMap<String, HashMap<Character, Long>>> computeNGramCountsExpandedForThisUser(
			int userAtRecomm, LinkedHashMap<Integer, HashMap<String, Long>> nGramCountsFromTrainingTimelines,
			int maxNOfNGrams)
	{
		/**
		 * (nOfgram, (currentSubNgrm,(nextActAsCharCode,Count))
		 */
		LinkedHashMap<Integer, HashMap<String, HashMap<Character, Long>>> nGramCountsExpandedForThisUser = new LinkedHashMap<>();
		try
		{
			for (int n = 2; n <= maxNOfNGrams; n++)
			{
				// 2-Gram, freq of that 2gram (ab,3;ac,29;ad,10)
				// expanded 2 gram (a--(b,3);a--(c,29);a--(d,10))
				HashMap<String, HashMap<Character, Long>> nGramCountsExpanded = new LinkedHashMap<>();

				HashMap<String, Long> thisNGramFreqDists = nGramCountsFromTrainingTimelines.get(n);
				for (Entry<String, Long> thisNGramFreqDist : thisNGramFreqDists.entrySet())
				{
					String nGram = thisNGramFreqDist.getKey();
					String currentSubNGram = nGram.substring(0, nGram.length() - 1);
					String nextSubNGram = nGram.substring(nGram.length() - 1, nGram.length());// should be a char

					if (nextSubNGram.length() != 1)
					{
						System.err.println(PopUps.getTracedErrorMsg("nextSubNGram.length()=" + nextSubNGram.length())
								+ " expected 1");
						System.exit(-1);
					}

					Character nextActCharCode = nextSubNGram.charAt(0);
					// /Integer nextActID = DomainConstants.charCodeCatIDMap.get(nextActCharCode);

					HashMap<Character, Long> nextActCountForThisCurrSubNGram = null;
					if (nGramCountsExpanded.containsKey(currentSubNGram))
					{
						nextActCountForThisCurrSubNGram = nGramCountsExpanded.get(currentSubNGram);
						if (nextActCountForThisCurrSubNGram.containsKey(nextActCharCode))
						{// This should not have happened.
							System.err.println(PopUps.getTracedErrorMsg(
									"nextActCountForThisCurrSubNGram.containsKey(nextActCharCode) is true for currentSubNGram"
											+ currentSubNGram + " nextActCharCode=" + nextActCharCode));
							System.exit(-1);
						}
					}
					else
					{
						nextActCountForThisCurrSubNGram = new HashMap<>();
					}
					nextActCountForThisCurrSubNGram.put(nextActCharCode, thisNGramFreqDist.getValue());
					nGramCountsExpanded.put(currentSubNGram, nextActCountForThisCurrSubNGram);
				}
				nGramCountsExpandedForThisUser.put(n, nGramCountsExpanded);
			} // end of loop over this n-gram

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return nGramCountsExpandedForThisUser;
	}

	/**
	 *
	 * @param trainingTimelines
	 * @param userAtRecomm
	 */
	private static void initialiseNGramData(LinkedHashMap<Date, Timeline> trainingTimelines, int userAtRecomm,
			boolean verbose, boolean writeToFile)
	{
		System.out.println("started initialiseNGramData for user " + userAtRecomm);
		try
		{
			if (allNGramData == null)
			{
				allNGramData = new LinkedHashMap<>();
				System.out.println("Initialising allNGramData");
			}

			if (allNGramData.containsKey(userAtRecomm) == false)
			{
				LinkedHashMap<Integer, HashMap<String, Long>> nGramCountsFromTrainingTimelines = new LinkedHashMap<>();
				System.out.println("initialising nGramCountsFromTrainingTimelines for user : " + userAtRecomm);

				Timeline trainingTimelinesAsATimeline = TimelineUtils.dayTimelinesToATimeline(trainingTimelines, false,
						true);

				if (Constant.hasInvalidActivityNames)
				{
					trainingTimelinesAsATimeline = TimelineUtils.expungeInvalids(trainingTimelinesAsATimeline);
				}

				String stringCodeOfTimeline = trainingTimelinesAsATimeline.getActivityObjectsAsStringCode();

				for (int n = 2; n <= maxNOfNGrams; n++)
				{
					HashMap<String, Long> freqDistr = TimelineStats.getNGramOccurrenceDistribution(stringCodeOfTimeline,
							n);
					writeFreqDistr(userAtRecomm, n, freqDistr);
					nGramCountsFromTrainingTimelines.put(n, freqDistr);
					System.out.println("Counted for n = " + n + " " + freqDistr.size() + " ngrams");
					// WritingToFile.writeSimpleMapToFile(freqDistr,
					// Constant.getCommonPath() + n + "gram" + userAtRecomm + "FreqDist.csv", "subsequence",
					// "count");
				}
				// user id
				LinkedHashMap<Integer, HashMap<String, HashMap<Character, Long>>> nGramCountsExpanded = computeNGramCountsExpandedForThisUser(
						userAtRecomm, nGramCountsFromTrainingTimelines, maxNOfNGrams);

				LinkedHashMap<Integer, HashMap<String, ArrayList<Character>>> nextActsBasedOnNGram = computeNextActsBasedOnNGram(
						nGramCountsExpanded, userAtRecomm);

				LinkedHashMap<Integer, HashMap<String, ArrayList<Integer>>> nextActsBasedOnNGramActID = computeNextActsBasedOnNGramActID(
						nGramCountsExpanded, userAtRecomm, ">");

				LinkedHashMap<Integer, HashMap<String, ArrayList<String>>> nextActsBasedOnNGramActName = computeNextActsBasedOnNGramActName(
						nGramCountsExpanded, userAtRecomm, ">");

				allNGramData.put(userAtRecomm, nGramCountsFromTrainingTimelines);
				nextMostFreqActsBasedOnNGrams.put(userAtRecomm, nextActsBasedOnNGram);
				nextMostFreqActsBasedOnNGramsActID.put(userAtRecomm, nextActsBasedOnNGramActID);
				nextMostFreqActsBasedOnNGramsActName.put(userAtRecomm, nextActsBasedOnNGramActName);
				if (writeToFile)
				{
					WritingToFile.writeMapOfMapOfMapOfList(nextMostFreqActsBasedOnNGrams,
							Constant.outputCoreResultsPath + "nextMostFreqActsBasedOnNGrams.csv", "|", "__");
					WritingToFile.writeMapOfMapOfMapOfListInt(nextMostFreqActsBasedOnNGramsActID,
							Constant.outputCoreResultsPath + "nextMostFreqActsBasedOnNGramsActID.csv", "|", "__");
					WritingToFile.writeMapOfMapOfMapOfListString(nextMostFreqActsBasedOnNGramsActName,
							Constant.outputCoreResultsPath + "nextMostFreqActsBasedOnNGramsActName.csv", "|", "__");
				}
			}
			else
			{
				System.out.println("already contains this user");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param userAtRecomm
	 * @param n
	 * @param freqDistr
	 * @return
	 */
	private static HashMap<String, Long> writeFreqDistr(int userAtRecomm, int n, HashMap<String, Long> freqDistr)
	{
		freqDistr = (LinkedHashMap<String, Long>) ComparatorUtils.sortByValueDescNoShuffle(freqDistr);
		WritingToFile.writeSimpleMapToFile(freqDistr,
				Constant.outputCoreResultsPath + n + "gram" + userAtRecomm + "FreqDistTrain.csv", "subsequence",
				"count");

		/// If there is a need to write n-grams as actname or act ids instead of char codes
		LinkedHashMap<String, Long> freqDistrWithActNames = freqDistr.entrySet().stream()
				.collect(Collectors.toMap(e -> TimelineStats.getNGramAsActName(e.getKey(), "--"), e -> e.getValue(),
						(e1, e2) -> e1, LinkedHashMap::new));

		LinkedHashMap<String, Long> freqDistrWithActID = freqDistr.entrySet().stream()
				.collect(Collectors.toMap(e -> TimelineStats.getNGramAsActID(e.getKey(), "--"), e -> e.getValue(),
						(e1, e2) -> e1, LinkedHashMap::new));

		WritingToFile.writeSimpleMapToFile(freqDistrWithActNames,
				Constant.outputCoreResultsPath + n + "gram" + userAtRecomm + "FreqDistActNameTrain.csv", "subsequence",
				"count");

		WritingToFile.writeSimpleMapToFile(freqDistrWithActID,
				Constant.outputCoreResultsPath + n + "gram" + userAtRecomm + "FreqDistActIDTrain.csv", "subsequence",
				"count");

		return freqDistr;
	}

	/**
	 *
	 * @param nGramsCountsExpanded
	 * @param userAtRecomm
	 * @return (NOfNGram, (CurrentSubNGram,list of next acts with max count))
	 */
	private static LinkedHashMap<Integer, HashMap<String, ArrayList<Character>>> computeNextActsBasedOnNGram(
			LinkedHashMap<Integer, HashMap<String, HashMap<Character, Long>>> nGramsCountsExpanded, int userAtRecomm)
	{
		// (NOfNGram, (CurrentSubNGram,list of next acts with max count))
		LinkedHashMap<Integer, HashMap<String, ArrayList<Character>>> res = new LinkedHashMap<>();

		try
		{
			// (NOfNGram, (CurrentSubNGram,nextActAsCharCode,Count))
			for (Entry<Integer, HashMap<String, HashMap<Character, Long>>> nGramsEntry : nGramsCountsExpanded
					.entrySet())
			{
				HashMap<String, ArrayList<Character>> nextActsForEachCurrentSubNGram = new HashMap<>();

				for (Entry<String, HashMap<Character, Long>> curSubNGramEntry : nGramsEntry.getValue().entrySet())
				{
					ArrayList<Character> nextActsWithMaxCount = (ArrayList<Character>) ComparatorUtils
							.getKeysWithMaxValues(curSubNGramEntry.getValue());
					nextActsForEachCurrentSubNGram.put(curSubNGramEntry.getKey(), nextActsWithMaxCount);
				}
				res.put(nGramsEntry.getKey(), nextActsForEachCurrentSubNGram);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 *
	 * @param nGramsCountsExpanded
	 * @param userAtRecomm
	 * @return (NOfNGram, (CurrentSubNGram,list of next acts with max count))
	 */
	private static LinkedHashMap<Integer, HashMap<String, ArrayList<Integer>>> computeNextActsBasedOnNGramActID(
			LinkedHashMap<Integer, HashMap<String, HashMap<Character, Long>>> nGramsCountsExpanded, int userAtRecomm,
			String actIDDelimiter)
	{
		// (NOfNGram, (CurrentSubNGram,list of next acts with max count))
		LinkedHashMap<Integer, HashMap<String, ArrayList<Integer>>> res = new LinkedHashMap<>();

		try
		{
			// (NOfNGram, (CurrentSubNGram,nextActAsCharCode,Count))
			for (Entry<Integer, HashMap<String, HashMap<Character, Long>>> nGramsEntry : nGramsCountsExpanded
					.entrySet())
			{
				HashMap<String, ArrayList<Integer>> nextActsForEachCurrentSubNGram = new HashMap<>();

				for (Entry<String, HashMap<Character, Long>> curSubNGramEntry : nGramsEntry.getValue().entrySet())
				{
					ArrayList<Character> nextActsWithMaxCount = (ArrayList<Character>) ComparatorUtils
							.getKeysWithMaxValues(curSubNGramEntry.getValue());

					ArrayList<Integer> nextActsWithMaxCountActID = (ArrayList<Integer>) nextActsWithMaxCount.stream()
							.map(c -> DomainConstants.charCodeCatIDMap.get(c)).collect(Collectors.toList());

					String curSubNGramEntryKeyAsActIDs = curSubNGramEntry.getKey().chars().mapToObj(i -> (char) i)
							.map(c -> String.valueOf(DomainConstants.charCodeCatIDMap.get(c)))
							.collect(Collectors.joining(actIDDelimiter));

					nextActsForEachCurrentSubNGram.put(curSubNGramEntryKeyAsActIDs, nextActsWithMaxCountActID);
				}
				res.put(nGramsEntry.getKey(), nextActsForEachCurrentSubNGram);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * NOTE: this is only for logging purpose.
	 *
	 * @param nGramsCountsExpanded
	 * @param userAtRecomm
	 * @return (NOfNGram, (CurrentSubNGram,list of next acts with max count))
	 */
	private static LinkedHashMap<Integer, HashMap<String, ArrayList<String>>> computeNextActsBasedOnNGramActName(
			LinkedHashMap<Integer, HashMap<String, HashMap<Character, Long>>> nGramsCountsExpanded, int userAtRecomm,
			String actNameDelimter)
	{
		// (NOfNGram, (CurrentSubNGram,list of next acts with max count))
		LinkedHashMap<Integer, HashMap<String, ArrayList<String>>> res = new LinkedHashMap<>();

		try
		{
			// (NOfNGram, (CurrentSubNGram,nextActAsCharCode,Count))
			for (Entry<Integer, HashMap<String, HashMap<Character, Long>>> nGramsEntry : nGramsCountsExpanded
					.entrySet())
			{
				HashMap<String, ArrayList<String>> nextActsForEachCurrentSubNGram = new HashMap<>();

				for (Entry<String, HashMap<Character, Long>> curSubNGramEntry : nGramsEntry.getValue().entrySet())
				{
					ArrayList<Character> nextActsWithMaxCount = (ArrayList<Character>) ComparatorUtils
							.getKeysWithMaxValues(curSubNGramEntry.getValue());

					ArrayList<String> nextActsWithMaxCountActID = (ArrayList<String>) nextActsWithMaxCount.stream()
							.map(c -> DomainConstants.catIDNameDictionary.get(DomainConstants.charCodeCatIDMap.get(c)))
							.collect(Collectors.toList());

					String curSubNGramEntryKeyAsActNames = curSubNGramEntry.getKey().chars().mapToObj(i -> (char) i)
							.map(c -> DomainConstants.catIDNameDictionary.get(DomainConstants.charCodeCatIDMap.get(c)))
							.collect(Collectors.joining(actNameDelimter));

					nextActsForEachCurrentSubNGram.put(curSubNGramEntryKeyAsActNames, nextActsWithMaxCountActID);
				}
				res.put(nGramsEntry.getKey(), nextActsForEachCurrentSubNGram);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Recommendation for a particular RT
	 *
	 * @param trainingTimelines
	 * @param testTimelines
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 *            start time of the current activity, equivalent to using end time of the current activity
	 * @param userAtRecomm
	 *            user for which recommendation is being generated
	 * @param thresholdVal
	 * @param typeOfThreshold
	 * @param matchingUnitInCountsOrHours
	 * @param caseType
	 * @param lookPastType
	 * @param dummy
	 * @param actObjsToAddToCurrentTimeline
	 */
	public RecommendationMasterMar2017GenSeqNGramBaseline(LinkedHashMap<Date, Timeline> trainingTimelines,
			LinkedHashMap<Date, Timeline> testTimelines, String dateAtRecomm, String timeAtRecomm, int userAtRecomm,
			ArrayList<ActivityObject> actObjsToAddToCurrentTimeline)
	{
		// PopUps.showMessage("called RecommendationMasterMar2017GenSeq");
		try
		{
			System.out
					.println("\n-----------Starting RecommendationMasterMar2017GenSeqNGramBaseline " + "-------------");

			if (Constant.NGramColl == false)
			{
				initialiseNGramData(trainingTimelines, userAtRecomm, true, true);
			}

			errorExists = false;

			this.hasCandidateTimelines = true;
			this.hasCandidateTimelinesBelowThreshold = true;
			this.nextActivityJustAfterRecommPointIsInvalid = false;

			// dd/mm/yyyy // okay java.sql.Date with no hidden time
			this.dateAtRecomm = DateTimeUtils.getDateFromDDMMYYYY(dateAtRecomm, RegexUtils.patternForwardSlash);
			this.timeAtRecomm = Time.valueOf(timeAtRecomm);
			this.userAtRecomm = Integer.toString(userAtRecomm);
			this.userIDAtRecomm = Integer.toString(userAtRecomm);
			System.out.println(" User at Recomm = " + this.userAtRecomm + "\tDate at Recomm = " + this.dateAtRecomm
					+ "\tTime at Recomm = " + this.timeAtRecomm);

			//////
			// converting day timelines into continuous timeline
			Timeline testTimeline = TimelineUtils.dayTimelinesToATimeline(testTimelines, false, true);
			// if its subsequent seq index recommendation then take the last ao of the prev recomms
			if (actObjsToAddToCurrentTimeline.size() > 0)
			{
				this.activityAtRecommPoint = actObjsToAddToCurrentTimeline
						.get(actObjsToAddToCurrentTimeline.size() - 1);
				this.activitiesGuidingRecomm = new ArrayList<ActivityObject>();
				activitiesGuidingRecomm.add(activityAtRecommPoint);

			}
			else // extract current activity object with mu=0
			{
				Pair<TimelineWithNext, Double> currentTimelineTemp = TimelineUtils
						.getCurrentTimelineFromLongerTimelineMUCount(testTimeline, this.dateAtRecomm, this.timeAtRecomm,
								this.userIDAtRecomm, 0);
				this.activitiesGuidingRecomm = currentTimelineTemp.getFirst().getActivityObjectsInTimeline();
				if (activitiesGuidingRecomm.size() != 1)
				{
					currentTimelineTemp.getFirst().printActivityObjectNamesWithTimestampsInSequence();
					PopUps.printTracedErrorMsgWithExit(
							"activitiesGuidingRecomm.size() = " + activitiesGuidingRecomm.size() + " != 1");
				}
				this.activityAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
			}

			this.activityNameAtRecommPoint = this.activityAtRecommPoint.getActivityName();

			// sanity check start
			if (actObjsToAddToCurrentTimeline.size() > 0)
			{
				if (activityNameAtRecommPoint.equals(actObjsToAddToCurrentTimeline
						.get(actObjsToAddToCurrentTimeline.size() - 1).getActivityName()) == false)
				{
					System.err.println(
							"Error act name of actAtRecommPoint and last act in acts to add do not match: activityNameAtRecommPoint= "
									+ activityNameAtRecommPoint + " last act in acts to add = "
									+ actObjsToAddToCurrentTimeline.get(actObjsToAddToCurrentTimeline.size() - 1)
											.getActivityName());
				}
			}
			// sanity check end
			if (VerbosityConstants.verbose)
			{
				System.out.println("activitiesGuidingRecomm.size()=" + activitiesGuidingRecomm.size()
						+ " activityAtRecommPoint = " + activityAtRecommPoint.getActivityName());
				System.out.println(" activitiesGuidingRecomm.size =" + this.activitiesGuidingRecomm.size());
				System.out.println("\nActivity at Recomm point (Current Activity) =" + activityNameAtRecommPoint);
			}

			//////// Create ranked recommended act names
			this.recommendedActivityNamesWithRankscores = createRankedTopRecommendedActivityNamesNGram(
					this.activityNameAtRecommPoint, Integer.valueOf(this.userAtRecomm), 2);

			if (recommendedActivityNamesWithRankscores == null)
			{
				this.hasCandidateTimelines = false;
				this.hasCandidateTimelinesBelowThreshold = false;
			}
			else
			{
				this.rankedRecommendedActNamesWithRankScoresStr = getRankedRecommendedActivityNamesWithRankScoresString(
						this.recommendedActivityNamesWithRankscores);
				this.rankedRecommendedActNamesWithoutRankScoresStr = getRankedRecommendedActivityNamesWithoutRankScoresString(
						this.recommendedActivityNamesWithRankscores);
			}
			// //
			if (VerbosityConstants.verbose)
			{
				System.out.println("Debug: rankedRecommendedActNamesWithRankScoresStr= "
						+ rankedRecommendedActNamesWithRankScoresStr);
				System.out.println("Debug: rankedRecommendedActNamesWithoutRankScoresStr= "
						+ rankedRecommendedActNamesWithoutRankScoresStr);
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.getTracedErrorMsg("Exception in recommendation master");
		}

		System.out.println("\n^^^^^^^^^^^^^^^^Exiting Recommendation Master");
	}

	/**
	 *
	 * @param activityNameAtRecommPoint
	 * @param userID
	 * @param nGram
	 * @return
	 */
	private LinkedHashMap<String, Double> createRankedTopRecommendedActivityNamesNGram(String activityNameAtRecommPoint,
			Integer userID, Integer nGram)
	{
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug inside createRankedTopRecommendedActivityNamesNGram:");
		}
		LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<>(); //
		// <ActivityName,RankScore>

		// (UserID,(nOfN-Gram,(CurrentSubNGram,ListOfActsIDsWithMaxCountAsCHarCode))
		// private static LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<Character>>>>
		// nextMostFreqActsBasedOnNGrams;
		HashMap<String, ArrayList<Character>> nextMostFreqActsForThisUserN = nextMostFreqActsBasedOnNGrams.get(userID)
				.get(nGram);

		// is of length 1 as we are working with 2grams now.
		Character currentSubNGramChar = DomainConstants.catIDCharCodeMap
				.get(Integer.valueOf(activityNameAtRecommPoint));
		String currentSubNGram = currentSubNGramChar.toString();

		ArrayList<Character> listOfActsIDsWithMaxCountAsCharCode = nextMostFreqActsForThisUserN.get(currentSubNGram);

		if (listOfActsIDsWithMaxCountAsCharCode == null)
		{
			System.err.println("Warning 623: listOfActsIDsWithMaxCountAsCharCode == null " + " for currentSubNGram = "
					+ currentSubNGram + " i.e., activityNameAtRecommPoint = " + activityNameAtRecommPoint
					+ " is not in ngram map. This can happen if this act was in test but not in training");
			return null;
		}

		for (Character c : listOfActsIDsWithMaxCountAsCharCode)
		{
			recommendedActivityNamesRankscorePairs.put(String.valueOf(DomainConstants.charCodeCatIDMap.get(c)), 1.0);
		}

		return recommendedActivityNamesRankscorePairs;
	}

	/**
	 *
	 * @param testTimelinesOrig
	 * @param lookPastType2
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCountsOrHours
	 * @return
	 */
	private static Pair<TimelineWithNext, Double> extractCurrentTimeline(
			LinkedHashMap<Date, Timeline> testTimelinesOrig, LookPastType lookPastType2, Date dateAtRecomm,
			Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInCountsOrHours)
	{
		TimelineWithNext extractedCurrentTimeline = null;
		LinkedHashMap<Date, Timeline> testTimelinesDaywise = testTimelinesOrig;
		double reductionInMu = 0;// done when not enough acts in past to look into, only relevant for NCount

		if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
		{
			if (Constant.hasInvalidActivityNames)
			{
				testTimelinesDaywise = TimelineUtils.expungeInvalidsDT(testTimelinesOrig);
				// $$System.out.println("Expunging invalids before recommendation process: expunging test timelines");
			}
			else
			{
				// $$System.out.println("Data assumed to have no invalid act names to be expunged from test timelines");
			}
		}

		// //////////////////
		if (lookPastType2.equals(Enums.LookPastType.Daywise) || lookPastType2.equals(Enums.LookPastType.ClosestTime))
		{
			extractedCurrentTimeline = TimelineUtils.getCurrentTimelineFromLongerTimelineDaywise(testTimelinesDaywise,
					dateAtRecomm, timeAtRecomm, userIDAtRecomm);
		}
		else
		{
			// converting day timelines into continuous timelines suitable to be used for matching unit views
			Timeline testTimeline = TimelineUtils.dayTimelinesToATimeline(testTimelinesDaywise, false, true);

			if (lookPastType2.equals(Enums.LookPastType.NCount))
			{
				Pair<TimelineWithNext, Double> result = TimelineUtils.getCurrentTimelineFromLongerTimelineMUCount(
						testTimeline, dateAtRecomm, timeAtRecomm, userIDAtRecomm, matchingUnitInCountsOrHours);
				extractedCurrentTimeline = result.getFirst();
				reductionInMu = result.getSecond();
			}

			else if (lookPastType2.equals(Enums.LookPastType.NHours))
			{
				extractedCurrentTimeline = TimelineUtils.getCurrentTimelineFromLongerTimelineMUHours(testTimeline,
						dateAtRecomm, timeAtRecomm, userIDAtRecomm, matchingUnitInCountsOrHours);
			}
			else
			{
				System.err.println(PopUps.getTracedErrorMsg("Error: Unrecognised lookPastType "));
				System.exit(-1);
			}
		}
		// ////////////////////
		if (extractedCurrentTimeline == null || extractedCurrentTimeline.getActivityObjectsInTimeline().size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: current timeline is empty"));
			System.exit(-1);
			// this.errorExists = true;
		}
		if (VerbosityConstants.verbose)
		{
			System.out.println(
					"Extracted current timeline: " + extractedCurrentTimeline.getPrimaryDimensionValsInSequence());//
			// .getActivityObjectNamesInSequence());
		}
		return new Pair<>(extractedCurrentTimeline, reductionInMu);
	}

	/**
	 *
	 * @param testTimelinesOrig
	 * @param lookPastType2
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCountsOrHours
	 * @param actObjsToAddToCurrentTimeline
	 * @return
	 * @since May 2, 2017
	 */
	private static Pair<TimelineWithNext, Double> extractCurrentTimelineSeq(
			LinkedHashMap<Date, Timeline> testTimelinesOrig, LookPastType lookPastType2, Date dateAtRecomm,
			Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInCountsOrHours,
			ArrayList<ActivityObject> actObjsToAddToCurrentTimeline)
	{
		// System.out.println("called extractCurrentTimelineSeq");
		Pair<TimelineWithNext, Double> extractedCurrentTimelineResult = extractCurrentTimeline(testTimelinesOrig,
				lookPastType2, dateAtRecomm, timeAtRecomm, userIDAtRecomm, matchingUnitInCountsOrHours);
		TimelineWithNext extractedCurrentTimeline = extractedCurrentTimelineResult.getFirst();

		// //////////////////
		ArrayList<ActivityObject> actObjsForCurrTimeline = new ArrayList<>(
				extractedCurrentTimeline.getActivityObjectsInTimeline());

		// sanity check is act objs to add are later than act objs in timeline
		TimelineSanityChecks.checkIfChronoLogicalOrder(actObjsForCurrTimeline, actObjsToAddToCurrentTimeline);

		actObjsForCurrTimeline.addAll(actObjsToAddToCurrentTimeline);

		// NOTE: WE ARE NOT SETTING NEXT ACTIVITY OBJECT OF CURRENT TIMELINE HERE.
		if (lookPastType2.equals(Enums.LookPastType.Daywise) || lookPastType2.equals(Enums.LookPastType.ClosestTime))
		{
			extractedCurrentTimeline = new TimelineWithNext(actObjsForCurrTimeline, null, true, true);
		}
		else if (lookPastType2.equals(Enums.LookPastType.NCount) || lookPastType2.equals(Enums.LookPastType.NHours))
		{
			extractedCurrentTimeline = new TimelineWithNext(actObjsForCurrTimeline, null, false, true);
		}
		extractedCurrentTimeline.setImmediateNextActivityIsInvalid(-1);
		// ////////////////////
		if (extractedCurrentTimeline.getActivityObjectsInTimeline().size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error: extractCurrentTimeline extractedCurrentTimeline.getActivityObjectsInTimeline().size()="
							+ extractedCurrentTimeline.getActivityObjectsInTimeline().size()));
			System.exit(-1);
			// this.errorExists = true;
		}
		if (VerbosityConstants.verbose)
		{
			System.out.println("Extracted current timeline extractCurrentTimeline: "
					+ extractedCurrentTimeline.getActivityObjectNamesInSequence());
		}

		return new Pair<>(extractedCurrentTimeline, extractedCurrentTimelineResult.getSecond());

	}

	public Date getDateAtRecomm()
	{
		return dateAtRecomm;
	}

	public String getActivityNamesGuidingRecomm()
	{
		StringBuilder res = new StringBuilder();

		for (ActivityObject ae : activitiesGuidingRecomm)
		{
			res = StringUtils.fCat(res, ">>", ae.getActivityName());
			// res.append(">>" + ae.getActivityName());
		}
		return res.toString();
	}

	public LinkedHashMap<String, Double> getRecommendedActivityNamesWithRankscores()
	{
		return this.recommendedActivityNamesWithRankscores;
	}

	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Generate the string: '__recommendedActivityName1:simRankScore1__recommendedActivityName2:simRankScore2'
	 *
	 * @param recommendedActivityNameRankscorePairs
	 */
	private static String getRankedRecommendedActivityNamesWithRankScoresString(
			LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs)
	{
		StringBuilder topRankedString = new StringBuilder();// String topRankedString= new String();
		StringBuilder msg = new StringBuilder();
		for (Map.Entry<String, Double> entry : recommendedActivityNameRankscorePairs.entrySet())
		{
			String recommAct = entry.getKey();
			double roundedRankScore = Evaluation.round(entry.getValue(), 4);
			topRankedString.append("__" + recommAct + ":" + roundedRankScore);
			msg.append("recomm act:" + recommAct + ", rank score: " + roundedRankScore + "\n");
			// topRankedString+= "__"+entry.getKey()+":"+TestStats.round(entry.getValue(),4);
		}
		if (VerbosityConstants.verboseRankScoreCalcToConsole)
		{
			System.out.println(msg.toString() + "\n");
		}
		return topRankedString.toString();
	}

	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Generate string as '__recommendedActivityName1__recommendedActivityName2'
	 *
	 * @param recommendedActivityNameRankscorePairs
	 * @return
	 */
	private static String getRankedRecommendedActivityNamesWithoutRankScoresString(
			LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs)
	{
		StringBuilder rankedRecommendationWithoutRankScores = new StringBuilder();
		for (Map.Entry<String, Double> entry : recommendedActivityNameRankscorePairs.entrySet())
		{
			rankedRecommendationWithoutRankScores.append("__" + entry.getKey());
		}
		return rankedRecommendationWithoutRankScores.toString();
	}

	// /////////////////////////////////////////////////////////////////////

	public static LinkedHashMap<String, Double> removeRecommPointActivityFromRankedRecomm(
			LinkedHashMap<String, Double> recommendedActivityNamesWithRankscores, String activityNameAtRecommPoint)
	{
		// String activityNameAtRecommPoint = activityAtRecommPoint.getActivityName();
		System.out.println("removeRecommPointActivityFromRankedRecomm called");
		Double d = recommendedActivityNamesWithRankscores.remove(activityNameAtRecommPoint);
		if (d == null)
		{
			System.out.println("Note: removeRecommPointActivityFromRankedRecomm: curr act not in recommendation");
		}
		return recommendedActivityNamesWithRankscores;
	}

	public boolean hasError()
	{
		return this.errorExists;
	}

	public void setErrorExists(boolean exists)
	{
		this.errorExists = exists;
	}

	/// Start of Methods for interface

	public int getNumOfCandidateTimelines()
	{
		return this.recommendedActivityNamesWithRankscores.size();
	}

	public int getNumOfActsInActsGuidingRecomm()
	{
		return this.activitiesGuidingRecomm.size();
	}

	public int getNumOfValidActsInActsGuidingRecomm()
	{
		int count = 0;
		for (ActivityObject ae : this.activitiesGuidingRecomm)
		{
			if (UtilityBelt.isValidActivityName(ae.getActivityName()))
			{
				count++;
			}
		}
		return count++;
	}

	public Timeline getCandidateTimeline(String timelineID)
	{
		return null;// this.candidateTimelines.get(timelineID);
	}

	public ArrayList<Timeline> getOnlyCandidateTimeslines()
	{
		return new ArrayList<Timeline>();// ) this.candidateTimelines.entrySet().stream().map(e -> (Timeline)
		// e.getValue())
		// .collect(Collectors.toList());
	}

	public Set<String> getCandidateTimelineIDs()
	{
		return new HashSet<String>();// this.candidateTimelines.keySet();
	}

	public boolean hasCandidateTimeslines()
	{
		return hasCandidateTimelines;
	}

	public boolean hasCandidateTimelinesBelowThreshold()
	{
		return hasCandidateTimelinesBelowThreshold;
	}

	public boolean hasThresholdPruningNoEffect()
	{
		return true;// thresholdPruningNoEffect;
	}

	public boolean isNextActivityJustAfterRecommPointIsInvalid()
	{
		return this.nextActivityJustAfterRecommPointIsInvalid;
	}

	public ActivityObject getActivityObjectAtRecomm()
	{
		return this.activityAtRecommPoint;
	}

	/**
	 *
	 * @return
	 */
	public double getThresholdAsDistance()
	{
		return -1;// thresholdAsDistance;
	}

	public int getNumOfCandTimelinesBelowThresh() // satisfying threshold
	{
		if (hasCandidateTimelinesBelowThreshold == false)
		{
			System.err.println(
					"Error: Sanity Check RM60 failed: trying to get number of candidate timelines below threshold while there is no  candidate below threshold, u shouldnt have called this function");
		}
		/*
		 * Assuming that threshold has already been applied
		 */
		return this.recommendedActivityNamesWithRankscores.size();// this.distancesSortedMap.size();
	}

	/**
	 * Returns next activity names as String
	 *
	 * @return
	 */
	public String getNextActNamesWithoutDistString()
	{// LinkedHashMap<String, Pair<ActivityObject, Double>>
		StringBuilder result = new StringBuilder("");
		// nextActivityObjectsFromCands.entrySet().stream()
		// .forEach(e -> result.append("__" + e.getValue().getFirst().getActivityName()));
		return result.toString();
	}

	/**
	 * Returns next activity names with distance as String
	 *
	 * @return
	 */
	public String getNextActNamesWithDistString()
	{// LinkedHashMap<String, Pair<ActivityObject, Double>>
		StringBuilder result = new StringBuilder("");
		// nextActivityObjectsFromCands.entrySet().stream().forEach(e -> result
		// .append("__" + e.getValue().getFirst().getActivityName() + ":" + e.getValue().getSecond().toString()));
		return result.toString();
	}

	public String getActivityNamesGuidingRecommwithTimestamps()
	{
		StringBuilder res = new StringBuilder();
		for (ActivityObject ae : activitiesGuidingRecomm)
		{
			res = StringUtils.fCat(res, " ", ae.getActivityName(), "__", ae.getStartTimestamp().toString(), "_to_",
					ae.getEndTimestamp().toString());
			// res.append(" " + ae.getActivityName() + "__" + ae.getStartTimestamp() + "_to_" + ae.getEndTimestamp());
		}
		return res.toString();
	}

	public String getRankedRecommendedActNamesWithoutRankScores()
	{
		return this.rankedRecommendedActNamesWithoutRankScoresStr;
	}

	public String getRankedRecommendedActNamesWithRankScores()
	{
		return this.rankedRecommendedActNamesWithRankScoresStr;
	}

	public int getNumOfDistinctRecommendations()
	{
		return recommendedActivityNamesWithRankscores.size();
	}

	public LinkedHashMap<String, Pair<String, Double>> getDistancesSortedMap()
	{
		return new LinkedHashMap<String, Pair<String, Double>>();// this.distancesSortedMap;
	}

	public LinkedHashMap<String, Integer> getEndPointIndicesConsideredInCands()
	{
		return new LinkedHashMap<String, Integer>();// endPointIndicesConsideredInCands;
	}

	public ArrayList<ActivityObject> getActsGuidingRecomm()
	{
		return activitiesGuidingRecomm;
	}
	/// end of methods for interface

}

/////////////////////////////////////////////////

//////////
/// **
// *INCOMPLETE
// * @param userAtRecomm
// * @param nGramCountsFromTrainingTimelines
// */
// private static void computeNext2GramRecommendations(int userAtRecomm,
// LinkedHashMap<Integer, HashMap<String, Long>> nGramCountsFromTrainingTimelines)
// {
// try
// {
// if (nextMostFreqActBasedOn2Grams == null)
// {
// nextMostFreqActBasedOn2Grams = new LinkedHashMap<>();
// System.out.println("Initialising nextMostFreqActBasedOn2Grams");
// }
//
// if (nextMostFreqActBasedOn2Grams.containsKey(userAtRecomm) == false)
// {
// LinkedHashMap<Integer, Integer> nextActsBasedOnFrequency = new LinkedHashMap<>();
// System.out.println("initialising nextActsBasedOnFrequency for user : " + userAtRecomm);
//
// // 2-Gram, freq of that 2gram (ab,3;ac,29;ad,10)
// HashMap<String, Long> twoGramsFreqDist = nGramCountsFromTrainingTimelines.get(new Integer(2));
//
// // (a--(b,3);a--(c,29);a--(d,10))
// HashMap<Character, LinkedHashMap<Character, Long>> twoGramsExpanded =
// new LinkedHashMap<>(twoGramsFreqDist.size());
//
// for (Entry<String, Long> twoGramFreqDist : twoGramsFreqDist.entrySet())
// {
// String twoGramString = twoGramFreqDist.getKey();
//
// if (twoGramString.length() != 2)
// {
// System.err.println(
// PopUps.getTracedErrorMsg("twoGramString.length()(" + twoGramString.length() + ") !=2"));
// System.exit(-1);
// }
//
// Character a = twoGramString.charAt(0);
// Character b = twoGramString.charAt(1);
//
// if (twoGramsExpanded.containsKey(a))
// {
// if (twoGramsExpanded.get(a).containsKey(b))
// {
// System.err.println(PopUps.getTracedErrorMsg(
// "twoGramsExpanded.get(a).containsKey(b)==true,a=" + a + " b=" + b));
// System.exit(-2);
// }
// twoGramsExpanded.get(a);
//
// twoGramsExpanded.put(a, l.put(b, twoGramFreqDist.getValue()));
// }
//
// }
// }
// else
// {
// System.out.println("already contains this user");
// }
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
// }
