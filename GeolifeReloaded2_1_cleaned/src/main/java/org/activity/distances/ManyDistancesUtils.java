package org.activity.distances;

import java.util.ArrayList;
import java.util.List;

import org.activity.constants.VerbosityConstants;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
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

		List<Pair<String, String>> allPairs = new ArrayList<>();
		Pair<String, String> p1 = new Pair<>("word", "word");

		Pair<String, String> p2 = new Pair<>("word", "plum");
		Pair<String, String> p3 = new Pair<>("word", "wordd");
		Pair<String, String> p4 = new Pair<>("kkword", "wordds");
		Pair<String, String> p5 = new Pair<>("kkword", "kkwordds");
		Pair<String, String> p6 = new Pair<>("werd", "wopd");
		Pair<String, String> p7 = new Pair<>("werd", "wrpd");
		Pair<String, String> p8 = new Pair<>("werd", "wrpdkluk");

		allPairs.add(p1);
		allPairs.add(p2);
		allPairs.add(p3);
		allPairs.add(p4);
		allPairs.add(p5);
		allPairs.add(p6);
		allPairs.add(p7);
		allPairs.add(p8);

		String[] distTypes = { "Jaccard", "Jaro", "Hamming", "LongestCommonSubsequence" };

		for (Pair<String, String> pair : allPairs)
		{
			System.out.println("\n-----------");
			for (String distType : distTypes)
			{
				System.out.println("Pair = " + pair.toString() + " -- " + distType + " = "
						+ getGivenDistanceCompatibility(pair.getFirst(), pair.getSecond(), distType));
			}
		}
	}

	/**
	 * 
	 * @param word1
	 * @param word2
	 * @param distanceType
	 * @return
	 */
	public static Triple<String, Double, Triple<char[], int[], int[]>> getGivenDistanceCompatibility(String word1,
			String word2, String distanceType)
	{
		Double resultantDistance = null;

		switch (distanceType)
		{
		case "Jaccard":
			resultantDistance = JaccardDistance.STATIC.getDistance(word1, word2);
			break;
		case "Jaro":
			resultantDistance = JaroWinklerDistance.STATIC.getDistance(word1, word2);
			break;
		case "LongestCommonSubsequence":
			resultantDistance = (double) new LongestCommonSubsequenceDistance().apply(word1, word2);
			break;
		case "Hamming":
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
