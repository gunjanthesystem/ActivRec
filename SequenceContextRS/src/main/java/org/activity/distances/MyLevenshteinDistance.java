package org.activity.distances;

import java.util.Map;

import org.activity.constants.Constant;
import org.activity.constants.VerbosityConstants;
import org.activity.objects.Pair;
import org.activity.objects.TraceMatrixLeaner1;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;

/**
 * NOT USED AS OF 9 JAN 2019
 * <P>
 * Taken out of org.activity.distances.AlignmentBasedDistance.getMySimpleLevenshteinDistance(String, String, int, int,
 * int, Map<String, Double>)
 * 
 * @author gunjan
 * @since 7 Jan 2018
 */
public class MyLevenshteinDistance implements StringDistI
{
	int insertWt;
	int deleteWt;
	int replaceWt;
	Map<String, Double> replaceWtMultiplierMap;

	public MyLevenshteinDistance(int insertWt, int deleteWt, int replaceWt, Map<String, Double> replaceWtMultiplierMap)
	{
		super();
		this.insertWt = insertWt;
		this.deleteWt = deleteWt;
		this.replaceWt = replaceWt;
		this.replaceWtMultiplierMap = replaceWtMultiplierMap;
	}

	public double getDistance(String word1, String word2)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	// ~~~~~~~~~~~~~`
	/**
	 * Fork of org.activity.distances.AlignmentBasedDistance.getMySimpleLevenshteinDistance(String, String, int, int,
	 * int, Map<String, Double>)
	 * <p>
	 * faster v4: minimising splits to improve performance
	 * <p>
	 * Fork of org.activity.distances.AlignmentBasedDistance.getMySimpleLevenshteinDistance(String, String, int, int,
	 * int) for allowing replace wt modifier map
	 * <p>
	 * Computes Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * @param word1
	 * @param word2
	 * @return Triple{resultantTrace, resultantDistance, Triple{DISNTrace,coordTraces.getFirst(),
	 *         coordTraces.getSecond()}}
	 *         <p>
	 *         Trace =_I(0-1)_I(0-2)_I(0-3)_D(1-3)_D(2-3)_D(3-3)_N(4-4) <br/>
	 *         simpleLevenshteinDistance112=6.0<br/>
	 *         DINSTrace=IIIDDDN <br/>
	 *         third_second=[0, 0, 0, 1, 2, 3, 4] <br/>
	 *         third_third=[1, 2, 3, 3, 3, 3, 4]
	 * @since Aug 3, 2018
	 */
	public Triple<String, Double, Triple<char[], int[], int[]>> getMySimpleLevenshteinDistance(String word1,
			String word2)
	{
		boolean useTimeDecay = Constant.useTimeDecayInAED;// added on 20 Aug 2018
		double timeDecayPower = Constant.powerOfTimeDecayInAED;// added on 20 Aug 2018
		// boolean useHierarchicalDistance = Constant.useHierarchicalDistance;
		// HashMap<String, Double> catIDsHierarchicalDistance = null;
		// if (useHierarchicalDistance){ catIDsHierarchicalDistance = DomainConstants.catIDsHierarchicalDistance;}
		boolean hasReplaceWtModifierMap = replaceWtMultiplierMap == null ? false : true;

		TraceMatrixLeaner1 traceMatrix = new TraceMatrixLeaner1(word1.length(), word2.length());

		// long performanceTime1 = System.currentTimeMillis();
		if (VerbosityConstants.verboseLevenstein)// Constant.verbose ||
		{
			System.out.println("inside getMySimpleLevenshteinDistance  for word1=" + word1 + "  word2=" + word2
					+ " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt=" + replaceWt);
		}
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		double[][] dist = new double[len1 + 1][len2 + 1];
		// StringBuilder[][] traceMatrix = new StringBuilder[len1 + 1][len2 + 1];

		traceMatrix.resetLengthOfCells();
		// for (int i = 0; i <= len1; i++){ for (int j = 0; j <= len2; j++){traceMatrix[i][j] = new StringBuilder();}}

		dist[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dist[i][0] = i;
			// traceMatrix.addCharsToCell(i, 0, traceMatrix.getCellAtIndex(i - 1, 0), '_', 'D', '(', (char) (i + '0'),
			// '-', '0', ')');
			// (char)(i+'0') converts i to char i safely and not disturbed by ascii value;
			traceMatrix.addCharsToCell(i, 0, traceMatrix.getCellAtIndex(i - 1, 0), '_', 'D', '(', i, '-', 0, ')');
			// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dist[0][j] = j;
			// traceMatrix.addCharsToCell(0, j, traceMatrix.getCellAtIndex(0, j - 1), '_', 'I', '(', '0', '-',
			// (char) (j + '0'), ')');
			traceMatrix.addCharsToCell(0, j, traceMatrix.getCellAtIndex(0, j - 1), '_', 'I', '(', 0, '-', j, ')');
			// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);

				// System.out.println("\nComparing " + c1 + " and " + c2);
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dist[i + 1][j + 1] = dist[i][j];

					traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'N', '(', i + 1,
							'-', j + 1, ')');
					// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'N', '(',
					// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
					// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j
					// + 1) + ")");
				}
				else
				{
					double replaceWtMultiplier = 1;
					// Start of added on 3 Aug 2018
					if (hasReplaceWtModifierMap)
					{
						Double res = replaceWtMultiplierMap.get(c1 + "_" + c2);
						if (res == null)
						{
							PopUps.printTracedErrorMsgWithExit("Error: no entry found in replaceWtMultiplierMap for :"
									+ String.valueOf(c1) + "_" + String.valueOf(c2));
						}
						replaceWtMultiplier = res;
					}
					// End of added on 3 Aug 2018

					// start of added on 20 Aug 2018
					double timeDecayMultiplier = 1;
					if (useTimeDecay)
					{
						int valTemp = len2 - (j + 1) + 1;
						timeDecayMultiplier = Math.pow(valTemp, timeDecayPower);
						// $$System.out.println("valTemp = " + valTemp + " timeDecayMultiplier = " +
						// timeDecayMultiplier);
					}
					// end of added on 20 Aug 2018

					// diagonally previous, see slides from STANFORD NLP on // min edit distance
					double replace = dist[i][j] + (replaceWtMultiplier * timeDecayMultiplier * replaceWt);// 2;
					// deletion --previous row, i.e, cell above
					double delete = dist[i][j + 1] + (timeDecayMultiplier * deleteWt);// 1;
					// insertion --previous column, i.e, cell on left
					double insert = dist[i + 1][j] + (timeDecayMultiplier * insertWt);// 1;

					// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					// int min = replace > insert ? insert : replace;
					// min = delete > min ? min : delete;
					double min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j + 1), '_', 'D', '(',
								i + 1, '-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j + 1), '_', 'D', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = delete;
						// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_D(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(insert, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i + 1, j), '_', 'I', '(',
								i + 1, '-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i + 1, j), '_', 'I', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = insert;
						// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_I(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'S', '(', i + 1,
								'-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'S', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						min = replace;
						// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_S(" + (i + 1) + "-" + (j + 1) + ")");
					}

					if (min == -9999)
					{
						System.out.println(PopUps.getTracedErrorMsg("Error in minDistance"));
					}

					dist[i + 1][j + 1] = min;
				}
			}
		}

		String resultantTrace = String.valueOf(traceMatrix.getCellAtIndex(len1, len2));
		char[] DISNTrace = traceMatrix.getCellAtIndexOnlyDISN(len1, len2);
		Pair<int[], int[]> coordTraces = traceMatrix.getCellAtIndexOnlyCoordinates(len1, len2);

		Double resultantDistance = Double.valueOf(dist[len1][len2]);

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println(" Trace Matrix here--: \n" + traceMatrix.toString());
			// for (int i = 0; i <= len1; i++)
			// {
			// for (int j = 0; j <= len2; j++)
			// {
			// System.out.print(traceMatrix[i][j] + "|");
			// }
			// System.out.println();
			// }

			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dist[i][j] + "|");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + resultantDistance);// new Double(dist[len1][len2]));
			System.out.println("Resultant Trace = " + resultantTrace);// traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}

		// long performanceTime2 = System.currentTimeMillis();
		// WritingToFile.appendLineToFileAbsolute(
		// Integer.toString(word1.length()) + "," + Integer.toString(word2.length()) + ","
		// + Long.toString(performanceTime2 - performanceTime1) + "\n",
		// Constant.getCommonPath() + "MySimpleLevenshteinDistanceTimeTakenInms.csv");
		return new Triple<>(resultantTrace, resultantDistance,
				new Triple<char[], int[], int[]>(DISNTrace, coordTraces.getFirst(), coordTraces.getSecond()));
	}
	// ~~~~~~~~~~~~`

	public static boolean isMinimum(double tocheck, double a, double b, double c)
	{
		return tocheck == Math.min(Math.min(a, b), c);
	}
}
