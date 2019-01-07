package org.activity.distances;

import java.util.Arrays;

import org.activity.constants.VerbosityConstants;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.DoubleVector.Factory;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.CosineDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.set.HammingDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.set.JaccardSimilarityDistanceFunction;

public class ElkiUtils
{
	static Factory doubleVectorFactory = DoubleVector.FACTORY;

	public static double getDistUsingElkiDistFunction(double vals1[], double[] vals2,
			NumberVectorDistanceFunction distanceFunction)
	{
		DoubleVector nv1 = doubleVectorFactory.newNumberVector(vals1);
		DoubleVector nv2 = doubleVectorFactory.newNumberVector(vals2);
		return distanceFunction.distance(nv1, nv2);
	}

	public static void main1(String args[])
	{
		// CosineDistanceFunction cosineDist = CosineDistanceFunction.STATIC;
		// NumberVector n = new NumberVector();
		double vals1[] = { 1, 2, 3, 4, 5 };
		double vals2[] = { 1, 1, 4, 4, 5 };

		// NumberVector.Factory<NumberVector> nf = Factory<NumberVector>;
		// nf.newNumberVector(vals1);
		System.out.println(getDistUsingElkiDistFunction(vals1, vals2, CosineDistanceFunction.STATIC));
		System.out.println(getDistUsingElkiDistFunction(vals1, vals1, CosineDistanceFunction.STATIC));

		System.out.println(
				getDistUsingElkiDistFunction(vals1, vals2, new JaccardSimilarityDistanceFunction<NumberVector>()));
		System.out.println(
				getDistUsingElkiDistFunction(vals1, vals1, new JaccardSimilarityDistanceFunction<NumberVector>()));

	}

	public static void main2(String args[])
	{
		String word1 = "word1";
		String word2 = "word200";
		VerbosityConstants.verboseLevenstein = true;
		// NOT USED SINCE THESE TWO DISTANCE ARE FOR BINARY DATA
		System.out.println("JaccardDist= " + getGivenElkiDistance(word1, word2, "Jaccard"));
		System.out.println("Hamming= " + getGivenElkiDistance(word1, word2, "Hamming"));
	}

	public static Triple<String, Double, Triple<char[], int[], int[]>> getGivenElkiDistanceCompatibility(String word1,
			String word2, String distanceType)
	{
		Double resultantDistance = getGivenElkiDistance(word1, word2, distanceType);
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

	/**
	 * 
	 * @param word1
	 * @param word2
	 * @param distanceFunction
	 * @return
	 */
	public static double getGivenElkiDistance(String word1, String word2, String distanceType)
	{
		NumberVectorDistanceFunction distanceFunction = null;
		switch (distanceType)
		{
		case "Jaccard":
			distanceFunction = new JaccardSimilarityDistanceFunction<NumberVector>();
			break;
		case "Hamming":
			distanceFunction = HammingDistanceFunction.STATIC;
			break;
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown distanceType = " + distanceType);
		}

		return getGivenElkiDistance(word1, word2, distanceFunction);
	}

	/**
	 * @param word1
	 * 
	 * @param word2
	 * 
	 * @return Triple{resultantTrace, resultantDistance, Triple{DISNTrace,coordTraces.getFirst(),
	 *         coordTraces.getSecond()}}
	 *         <p>
	 *         Trace ="" <br/>
	 *         JaccardDistance<br/>
	 *         DINSTrace= <br/>
	 *         third_second=<br/>
	 *         third_third=
	 * 
	 * @since 6 Jan 2018
	 */
	public static double getGivenElkiDistance(String word1, String word2, NumberVectorDistanceFunction distanceFunction)
	{

		double vals1[] = word1.chars().mapToDouble(c -> (double) ((int) c)).toArray();
		double vals2[] = word2.chars().mapToDouble(c -> (double) ((int) c)).toArray();

		if (VerbosityConstants.verboseLevenstein)// Constant.verbose ||
		{
			System.out.println("inside getGivenElkiDistance  for\nword1 = " + word1 + "\nword2 = " + word2
					+ "\nvals1 = " + Arrays.toString(vals1) + "\nvals = " + Arrays.toString(vals2));
		}

		return getDistUsingElkiDistFunction(vals1, vals1, distanceFunction);

	}
	// ~~~~~~~~~~~~`
}
