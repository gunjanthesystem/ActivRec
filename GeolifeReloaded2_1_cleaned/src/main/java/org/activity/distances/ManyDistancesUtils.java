package org.activity.distances;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activity.constants.Enums;
import org.activity.constants.Enums.ActDistType;
import org.activity.constants.VerbosityConstants;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.apache.commons.text.similarity.HammingDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance;

public class ManyDistancesUtils
{

	public static void main(String args[])
	{
		System.out.println("test");
		// de.lmu.ifi.dbs.elki.distance.distancefunction.ClarkDistanceFunction d;
		// Catalano.Math.Distances;
		// System.out.println(String.format("|%30s|", "Hello World"));

		List<Pair<String, String>> allPairs = new ArrayList<>();
		// Pair<String, String> p0 = new Pair<>("cat", "cat");
		// Pair<String, String> p1 = new Pair<>("cat", "aat");
		// Pair<String, String> p2 = new Pair<>("cat", "bat");
		//
		// // Pair<String, String> p1 = new Pair<>("cat", "act");
		// // // Pair<String, String> p1 = new Pair<>("word", "word");
		// //
		// // Pair<String, String> p2 = new Pair<>("word", "plum");
		// Pair<String, String> p3 = new Pair<>("word", "wordd");
		// Pair<String, String> p4 = new Pair<>("kkword", "wordds");
		// Pair<String, String> p5 = new Pair<>("kkword", "kkwordds");
		// Pair<String, String> p6 = new Pair<>("werd", "wopd");
		// Pair<String, String> p7 = new Pair<>("werd", "wrpd");
		// Pair<String, String> p8 = new Pair<>("werd", "wrpdkluk");

		// Pair<String, String> p0 = new Pair<>("wwbw", "w");
		// Pair<String, String> p1 = new Pair<>("wbww", "w");
		// Pair<String, String> p2 = new Pair<>("bbbw", "w");

		Pair<String, String> p0 = new Pair<>("ssws", "swss");
		Pair<String, String> p1 = new Pair<>("wsws", "swss");
		Pair<String, String> p2 = new Pair<>("ssss", "swss");

		// Pair<String, String> p1 = new Pair<>("cat", "act");
		// // Pair<String, String> p1 = new Pair<>("word", "word");
		//
		// Pair<String, String> p2 = new Pair<>("word", "plum");
		Pair<String, String> p3 = new Pair<>("twss", "swss");
		Pair<String, String> p4 = new Pair<>("csss", "swss");
		Pair<String, String> p5 = new Pair<>("wwws", "swss");
		Pair<String, String> p6 = new Pair<>("ccws", "swss");
		Pair<String, String> p7 = new Pair<>("werd", "wrpd");
		Pair<String, String> p8 = new Pair<>("cat", "bzak");

		// allPairs.add(p1);
		allPairs.add(p0);
		allPairs.add(p1);
		allPairs.add(p2);
		// if (false)
		{

			allPairs.add(p3);
			allPairs.add(p4);
			allPairs.add(p5);
			allPairs.add(p6);

			allPairs.add(p7);
			allPairs.add(p8);
		}
		Enums.ActDistType[] distTypes = { ActDistType.AllZeroDistance };// ,.MyLevenshtein };
		// ActDistType.Jaccard, ActDistType.Jaro, ActDistType.Hamming,
		// ActDistType.LongestCommonSubsequence, ActDistType.MyLevenshtein };

		// VerbosityConstants.verboseLevenstein = true;
		// VerbosityConstants.verbose = true;

		for (Pair<String, String> pair : allPairs)
		{
			System.out.println("\n-----------");
			for (ActDistType distType : distTypes)
			{
				System.out.println("Pair = " + pair.toString() + " -- " + distType + " = "
						+ getGivenDistanceCompatibility(pair.getFirst(), pair.getSecond(), distType, 1, 1, 2, null));
			}
		}
	}

	/**
	 * To allow different distances to be easily pluggable for ActLevelDistance.
	 * 
	 * @param word1
	 * @param word2
	 * @param distanceType
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @param replaceWtMultiplierMap
	 * @return
	 * @aince 6 Jan 2019
	 */
	public static Triple<String, Double, Triple<char[], int[], int[]>> getGivenDistanceCompatibility(String word1,
			String word2, Enums.ActDistType distanceType, int insertWt, int deleteWt, int replaceWt,
			Map<String, Double> replaceWtMultiplierMap)
	{
		Double resultantDistance = null;

		switch (distanceType)
		{
		case MyLevenshtein:
			// return AlignmentBasedDistance.getMySimpleLevenshteinDistance(word1, word2, insertWt, deleteWt, replaceWt,
			// replaceWtMultiplierMap);
			return AlignmentBasedDistance.getMySimpleForeignBodyAwareLevenshteinDistance_9Jan(word1, word2, insertWt,
					deleteWt, replaceWt, replaceWtMultiplierMap);

		case Jaccard:
			resultantDistance = JaccardDistance.STATIC.getDistance(word1, word2);
			break;
		case Jaro:
			resultantDistance = JaroWinklerDistance.STATIC.getDistance(word1, word2);
			break;
		case LongestCommonSubsequence:
			resultantDistance = (double) new LongestCommonSubsequenceDistance().apply(word1, word2);
			break;
		case AllZeroDistance:
			resultantDistance = 0d;
			break;
		case RandomDistance:
			resultantDistance = StatsUtils.round(StatsUtils.randomInRange(0d, 1d), 4);
			break;
		case Hamming:
			if (word1.length() != word2.length())
			{
				System.out.println("Warning: not doing Hamming distance as: strings are of unequal length");
				return null;
			}

			resultantDistance = (double) new HammingDistance().apply(word1, word2);
			break;
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown distanceType = " + distanceType);
		}

		String resultantTrace = "";
		char[] DISNTrace = new char[] {};
		Pair<int[], int[]> coordTraces = new Pair<>();

		if (VerbosityConstants.verboseLevenstein)
		{
			System.out.println("Resultant Distance = " + resultantDistance + "\n -------- ");
		}

		return new Triple<>(resultantTrace, resultantDistance,
				new Triple<char[], int[], int[]>(DISNTrace, coordTraces.getFirst(), coordTraces.getSecond()));
	}
}
