package org.activity.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.activity.util.Constant;

/**
 * (note: In earlier version (before 14 April 2015, this class was name as TestStats.java)
 * 
 * @author gunjan
 *
 */
public class Evaluation
{
	public static final int theKOriginal = 5;
	public static final String[] timeCategories =
	{ "All", "Morning", "Afternoon", "Evening" };
	public String commonPath;// =Constant.commonPath;

	public Evaluation()// static void main(String args[])
	{
		// commonPath="/home/gunjan/MATLAB/bin/DCU data works/July20/New_20_Aug/Copy 1 aug global
		// 1000/";//Constant.getCommonPath();
		commonPath = Constant.getCommonPath();
		System.out.println("Inside Evaluation: common path is:" + commonPath);

		BufferedReader brMeta = null, brTopK = null, brActual = null, brBaseLineOccurrence = null,
				brBaseLineDuration = null, brCurrentTargetSame = null;

		// ArrayList<ArrayList<String>> arrayMeta, arrayTopK, arrayActual, arrayBaselineOccurrence,
		// arrayBaselineDuration;
		/**
		 * A 2 dimensional arraylist, with rows corresponding to user and columns corresponding to recommendation times
		 * and a cell contains the meta information (userid_dateOfRt_timeOfRt) for the corresponding user for
		 * correspnding recommendation time
		 */
		ArrayList<ArrayList<String>> arrayMeta = new ArrayList<ArrayList<String>>();
		/**
		 * A 2 dimensional arraylist, with rows corresponding to user and columns corresponding to recommendation times
		 * and a cell contains the topK recommended items for the corresponding user for corresponding recommendation
		 * time
		 */
		ArrayList<ArrayList<String>> arrayTopK = new ArrayList<ArrayList<String>>();
		/**
		 * A 2 dimensional arraylist, with rows corresponding to user and columns corresponding to recommendation times
		 * and a cell contains the actual next item (e.g. Activity Name) for the corresponding user for corresponding
		 * recommendation time
		 */
		ArrayList<ArrayList<String>> arrayActual = new ArrayList<ArrayList<String>>();

		ArrayList<ArrayList<Boolean>> arrayCurrentTargetSame = new ArrayList<ArrayList<Boolean>>();

		/**
		 * A 2 dimensional arraylist, with rows corresponding to user and columns corresponding to recommendation times
		 * and a cell contains the topK recommended items for the corresponding user for corresponding recommendation
		 * time, where the topK recommendations are the top K frequent items in that user's dataset. (note: for a given
		 * user, the top K items in this case are same across all RTs)
		 */
		ArrayList<ArrayList<String>> arrayBaselineOccurrence = new ArrayList<ArrayList<String>>();
		/**
		 * A 2 dimensional arraylist, with rows corresponding to user and columns corresponding to recommendation times
		 * and a cell contains the topK recommended items for the corresponding user for corresponding recommendation
		 * time, where the topK recommendations are the top K items based on duration in that user's dataset. (note: for
		 * a given user, the top K items in this case are same across all RTs)
		 */
		ArrayList<ArrayList<String>> arrayBaselineDuration = new ArrayList<ArrayList<String>>();

		try
		{
			String metaCurrentLine, topKCurrentLine, actualCurrentLine, baseLineOccurrenceCurrentLine,
					baseLineDurationCurrentLine, currentTargetSame;

			brMeta = new BufferedReader(new FileReader(commonPath + "meta.csv"));
			brTopK = new BufferedReader(new FileReader(commonPath + "dataRankedRecommendationWithoutScores.csv"));// /dataRecommTop5.csv"));
			brActual = new BufferedReader(new FileReader(commonPath + "dataActual.csv"));

			brCurrentTargetSame = new BufferedReader(new FileReader(commonPath + "metaIfCurrentTargetSameWriter.csv"));

			brBaseLineOccurrence = new BufferedReader(new FileReader(commonPath + "dataBaseLineOccurrence.csv"));
			brBaseLineDuration = new BufferedReader(new FileReader(commonPath + "dataBaseLineDuration.csv"));

			int countOfLinesMeta = 0;

			StringBuilder consoleLogBuilder = new StringBuilder();
			// consoleLogBuilder.append(
			while ((metaCurrentLine = brMeta.readLine()) != null)
			{
				ArrayList<String> currentLineArray = new ArrayList<String>();
				// System.out.println(metaCurrentLine);
				String[] tokensInCurrentMetaLine = metaCurrentLine.split(",");
				// System.out.println("number of tokens in this meta line=" + tokensInCurrentMetaLine.length);
				consoleLogBuilder.append("meta line num:" + (countOfLinesMeta + 1) + "#tokensInLine:"
						+ tokensInCurrentMetaLine.length + "\n");
				for (int i = 0; i < tokensInCurrentMetaLine.length; i++)
				{
					currentLineArray.add(tokensInCurrentMetaLine[i]);
				}
				arrayMeta.add(currentLineArray);
				countOfLinesMeta++;
			}
			consoleLogBuilder.append("\n number of meta lines =" + countOfLinesMeta + "\n");

			int countOfLinesTopK = 0;
			while ((topKCurrentLine = brTopK.readLine()) != null)
			{
				ArrayList<String> currentLineArray = new ArrayList<String>();
				// System.out.println("topKCurrentLine is" + topKCurrentLine);
				String[] tokensInCurrentTopKLine = topKCurrentLine.split(",");
				// System.out.println("number of tokens in this topK line=" + tokensInCurrentTopKLine.length);
				consoleLogBuilder.append("topk line num:" + (countOfLinesTopK + 1) + "#tokensInLine:"
						+ tokensInCurrentTopKLine.length + "\n");
				for (int i = 0; i < tokensInCurrentTopKLine.length; i++)
				{
					currentLineArray.add(tokensInCurrentTopKLine[i]);
				}
				arrayTopK.add(currentLineArray);
				countOfLinesTopK++;
			}
			consoleLogBuilder.append("\n number of topK lines =" + countOfLinesTopK + "\n");

			int countOfLinesBaselineOccurrence = 0;
			while ((baseLineOccurrenceCurrentLine = brBaseLineOccurrence.readLine()) != null)
			{
				ArrayList<String> currentLineArray = new ArrayList<String>();
				// System.out.println("baseLineOccurrenceCurrentLine is" + baseLineOccurrenceCurrentLine);
				String[] tokensInCurrentBaseLineOccurrenceLine = baseLineOccurrenceCurrentLine.split(",");
				// System.out.println("number of tokens in this baselineOccurrence line=" +
				// tokensInCurrentBaseLineOccurrenceLine.length);
				consoleLogBuilder.append("baselineOccurrence line num:" + (countOfLinesBaselineOccurrence + 1)
						+ "#tokensInLine:" + tokensInCurrentBaseLineOccurrenceLine.length + "\n");

				for (int i = 0; i < tokensInCurrentBaseLineOccurrenceLine.length; i++)
				{
					currentLineArray.add(tokensInCurrentBaseLineOccurrenceLine[i]);
				}
				arrayBaselineOccurrence.add(currentLineArray);
				countOfLinesBaselineOccurrence++;
			}
			consoleLogBuilder.append("\n number of baselineOccurrence lines =" + countOfLinesBaselineOccurrence + "\n");

			int countOfLinesBaselineDuration = 0;
			while ((baseLineDurationCurrentLine = brBaseLineDuration.readLine()) != null)
			{
				ArrayList<String> currentLineArray = new ArrayList<String>();
				// System.out.println("baseLineDurationCurrentLine is" + baseLineDurationCurrentLine);
				String[] tokensInCurrentBaseLineDurationLine = baseLineDurationCurrentLine.split(",");
				// System.out.println("number of tokens in this baseLineDuration line=" +
				// tokensInCurrentBaseLineDurationLine.length);
				consoleLogBuilder.append("baseLineDuration line num:" + (countOfLinesBaselineDuration + 1)
						+ "#tokensInLine:" + tokensInCurrentBaseLineDurationLine.length + "\n");

				for (int i = 0; i < tokensInCurrentBaseLineDurationLine.length; i++)
				{
					currentLineArray.add(tokensInCurrentBaseLineDurationLine[i]);
				}
				arrayBaselineDuration.add(currentLineArray);
				countOfLinesBaselineDuration++;
			}
			consoleLogBuilder.append("\n number of baseLineDuration lines =" + countOfLinesBaselineDuration + "\n");

			int countOfLinesActual = 0;
			while ((actualCurrentLine = brActual.readLine()) != null)
			{
				ArrayList<String> currentLineArray = new ArrayList<String>();
				// System.out.println(actualCurrentLine);
				String[] tokensInCurrentActualLine = actualCurrentLine.split(",");
				// System.out.println("number of token in this actual line=" + tokensInCurrentActualLine.length);
				consoleLogBuilder.append("actual line num:" + (countOfLinesActual + 1) + "#tokensInLine:"
						+ tokensInCurrentActualLine.length + "\n");

				for (int i = 0; i < tokensInCurrentActualLine.length; i++)
				{
					currentLineArray.add(tokensInCurrentActualLine[i]);
				}
				arrayActual.add(currentLineArray);
				countOfLinesActual++;
			}

			int countOfLinesCurrentTargetSame = 0;
			while ((actualCurrentLine = brCurrentTargetSame.readLine()) != null)
			{
				ArrayList<Boolean> currentLineArray = new ArrayList<Boolean>(); // System.out.println(actualCurrentLine);
				String[] tokensInCurrentTargetSameLine = actualCurrentLine.split(","); // System.out.println("number of
																						// token in this actual line=" +
																						// tokensInCurrentActualLine.length);
				consoleLogBuilder.append("current target same line num:" + (countOfLinesCurrentTargetSame + 1)
						+ "#tokensInLine:" + tokensInCurrentTargetSameLine.length + "\n");

				for (String val : tokensInCurrentTargetSameLine)
				{
					if (val.equals("true") || val.equals("1"))
					{
						currentLineArray.add(true);
					}

					else if (val.equals("false") || val.equals("0"))
					{
						currentLineArray.add(false);
					}

					else
					{
						new Exception(
								"Error in org.activity.evaluation.Evaluation.Evaluation(): unknown token for brCurrentTargetSame = "
										+ val);
					}
				}
				arrayCurrentTargetSame.add(currentLineArray);
				countOfLinesCurrentTargetSame++;
			}

			consoleLogBuilder.append("\n number of actual lines =" + countOfLinesTopK + "\n");
			consoleLogBuilder.append("size of meta array=" + arrayMeta.size() + "     size of topK array="
					+ arrayTopK.size() + "   size of actual array=" + arrayActual.size()
					+ "   size of current target same array=" + arrayCurrentTargetSame.size() + "\n");

			System.out.println(consoleLogBuilder.toString());
			consoleLogBuilder.setLength(0); // empty the consolelog stringbuilder
			// //////////////////////////// finished creating and populating the data structures needed

			for (String timeCategory : timeCategories)
			{
				for (int theK = theKOriginal; theK > 0; theK--)
				{
					writePrecisionRecallFMeasure("Algo", timeCategory, theK, arrayMeta, arrayTopK, arrayActual);
					writePrecisionRecallFMeasure("BaselineOccurrence", timeCategory, theK, arrayMeta,
							arrayBaselineOccurrence, arrayActual);
					writePrecisionRecallFMeasure("BaselineDuration", timeCategory, theK, arrayMeta,
							arrayBaselineDuration, arrayActual);
				}

				writeReciprocalRank("Algo", timeCategory, arrayMeta, arrayTopK, arrayActual);
				writeReciprocalRank("BaselineOccurrence", timeCategory, arrayMeta, arrayBaselineOccurrence,
						arrayActual);
				writeReciprocalRank("BaselineDuration", timeCategory, arrayMeta, arrayBaselineDuration, arrayActual);

				writeMeanReciprocalRank("Algo", timeCategory, countOfLinesTopK); // average over data points
				writeMeanReciprocalRank("BaselineOccurrence", timeCategory, countOfLinesBaselineOccurrence);
				writeMeanReciprocalRank("BaselineDuration", timeCategory, countOfLinesBaselineDuration);

				// writeAvgPrecisionsForAllKs("Algo", timeCategory, countOfLinesTopK); // average over data points
				writeAvgRecallsForAllKs("Algo", timeCategory, countOfLinesTopK);
				// writeAvgFMeasuresForAllKs("Algo", timeCategory, countOfLinesTopK);

				// writeAvgPrecisionsForAllKs("BaselineOccurrence", timeCategory, countOfLinesBaselineOccurrence);
				writeAvgRecallsForAllKs("BaselineOccurrence", timeCategory, countOfLinesBaselineOccurrence);
				// writeAvgFMeasuresForAllKs("BaselineOccurrence", timeCategory, countOfLinesBaselineOccurrence);

				// writeAvgPrecisionsForAllKs("BaselineDuration", timeCategory, countOfLinesBaselineDuration);
				writeAvgRecallsForAllKs("BaselineDuration", timeCategory, countOfLinesBaselineDuration);
				// writeAvgFMeasuresForAllKs("BaselineDuration", timeCategory, countOfLinesBaselineDuration);

				break;// only 'All' time category
			}
			System.out.println("All test stats done");
			// PopUps.showMessage("All test stats done");

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				brMeta.close();
				brTopK.close();
				brActual.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Calculate and write the metrics:reciprocal rank for the recommendations generated for each user for each
	 * recommendation time. (note: also write a file containing the count of number of recommendation times considered
	 * for the given parameters passsed to this method.)
	 * 
	 * @param fileNamePhrase
	 *            the name phrase used to name the file to be written
	 * @param timeCategory
	 *            time category for which metrics are to be calculated, the recommendation times to be considered are
	 *            filtered based on this.
	 * @param arrayMeta
	 * @param arrayTopK
	 * @param arrayActual
	 */
	public static void writeReciprocalRank(String fileNamePhrase, String timeCategory,
			ArrayList<ArrayList<String>> arrayMeta, ArrayList<ArrayList<String>> arrayTopK,
			ArrayList<ArrayList<String>> arrayActual)
	{
		// BufferedReader brMeta = null, brTopK = null, brActual = null;
		String commonPath = Constant.getCommonPath();
		BufferedWriter bwRR = null;// , bwTopKRecall = null, bwTopKF = null, bwNumberOfRecommendationTimes = null;
		try
		{
			String metaCurrentLine, topKCurrentLine, actualCurrentLine;
			/*
			 * brMeta = new BufferedReader(new FileReader("/home/gunjan/meta.csv")); brTopK = new BufferedReader(new
			 * FileReader("/home/gunjan/dataRecommTop5.csv")); brActual = new BufferedReader(new
			 * FileReader("/home/gunjan/dataActual.csv"));
			 */
			File fileRR = new File(commonPath + fileNamePhrase + timeCategory + "ReciprocalRank.csv");

			// File fileNumberOfRecommendationTimes = new File(commonPath + fileNamePhrase + timeCategory +
			// "NumOfRecommendationTimes.csv");
			// File fileAccuracy = new File("/home/gunjan/accuracy.csv");

			fileRR.delete();
			// fileAccuracy.delete();

			if (!fileRR.exists())
			{
				fileRR.createNewFile();
			}
			// fileNumberOfRecommendationTimes.createNewFile();
			/*
			 * if (!fileAccuracy.exists()) { fileAccuracy.createNewFile(); }
			 */

			bwRR = new BufferedWriter(new FileWriter(fileRR.getAbsoluteFile()));
			// bwNumberOfRecommendationTimes = new BufferedWriter(new
			// FileWriter(fileNumberOfRecommendationTimes.getAbsoluteFile()));

			// bwAccuracy = new BufferedWriter(new FileWriter(fileAccuracy.getAbsoluteFile()));

			System.out.println("size of meta array=" + arrayMeta.size() + "     size of topK array=" + arrayTopK.size()
					+ "   size of actual array=" + arrayActual.size());

			// bwNumberOfRecommendationTimes.write("," + timeCategory);
			// bwNumberOfRecommendationTimes.newLine();

			for (int i = 0; i < arrayMeta.size(); i++) // iterating over users (or rows)
			{
				ArrayList<String> currentLineArray = arrayMeta.get(i);
				System.out.println("Calculating RR for user:" + i);

				double RR = -99;

				// bwNumberOfRecommendationTimes.write(ConnectDatabase.getUserName(i) + ",");
				// int theK = 0;
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered

				for (int j = 0; j < currentLineArray.size(); j++) // iterating over recommendation times (or columns)
				{
					int hourOfTheDay = getHourFromMetaString(currentLineArray.get(j));
					if (getTimeCategoryOfTheDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
							|| timeCategory.equals("All"))
					{
						countOfRecommendationTimesConsidered++;

						String actual = arrayActual.get(i).get(j);

						String[] topKString = arrayTopK.get(i).get(j).split("__"); // topK is of the form string:
																					// __a__b__c__d__e is of length 6...
																					// value at index 0 is empty.

						// theK = theKOriginal;
						// System.out.println();

						// if (topKStrings.length - 1 < theK) // for this RT we are not able to make K recommendations
						// as less than K recommendations are present.
						// {
						// System.err.println("Warning: For " + currentLineArray.get(j) + ", Only top " +
						// (topKStrings.length - 1) +
						// " recommendation present while the asked for K is " + theK
						// + "\nDecreasing asked for K to " + (topKStrings.length - 1));
						// // +"\nWriting -999 values");
						// theK = topKStrings.length - 1;
						// // $topKPrecisionVal=-9999;
						// // $topKRecallVal=-9999;
						// // $topKFVal=-9999;
						// }
						// theK=topKStrings.length-1;
						// $else
						// ${
						if (Constant.verbose)
						{
							System.out.println("topKString=arrayTopK.get(i).get(j)=" + arrayTopK.get(i).get(j));
							System.out.println("actual string =" + actual);
						}
						// int countOfOccurence = 0; // the number of times the actual item appears in the top K
						// recommended list. NOTE: in the current case will be always be
						// either 1 or 0.
						int rank = -99;
						for (int y = 1; y <= topKString.length - 1; y++)
						{
							if (topKString[y].equalsIgnoreCase(actual))
							{
								rank = y;
								break;// assuming that the actual occurs only once in the recommended list
							}
						}

						// if (countOfOccurence > 1)
						// {
						// System.err.println("Error: in writePrecisionRecallFMeasure(): the actual string appears
						// multiple times in topK, which should not be the case as per our
						// current algorithm.");
						// }
						// if (countOfOccurence > 0)
						// {
						// countOfOccurence = 1;
						// }
						if (rank != -99)
						{
							RR = round((double) 1 / rank, 4);
						}
						else
						{
							RR = 0; // assuming the rank is at infinity
						}

						bwRR.write(RR + ",");

						if (Constant.verbose)
						{
							System.out.println("RR = " + RR);
						}

					}
				} // end of current line array

				bwRR.write("\n");
				// bwNumberOfRecommendationTimes.write(countOfRecommendationTimesConsidered + "\n");
				// bwAccuracy.write("\n");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bwRR.close();
				// bwNumberOfRecommendationTimes.close();
				// bwAccuracy.close();

			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Calculate and write the metrics:precision, recall and fmeasure values, for the recommendations generated for each
	 * user for each recommendation time. (note: also write a file containing the count of number of recommendation
	 * times considered for the given parameters passsed to this method.)
	 * 
	 * @param fileNamePhrase
	 *            the name phrase used to name the file to be written
	 * @param timeCategory
	 *            time category for which metrics are to be calculated, the recommendation times to be considered are
	 *            filtered based on this.
	 * @param theKOriginal
	 *            the top value, e.g. 5 in top-5
	 * @param arrayMeta
	 * @param arrayTopK
	 * @param arrayActual
	 */
	public static void writePrecisionRecallFMeasure(String fileNamePhrase, String timeCategory, int theKOriginal,
			ArrayList<ArrayList<String>> arrayMeta, ArrayList<ArrayList<String>> arrayTopK,
			ArrayList<ArrayList<String>> arrayActual)
	{
		// BufferedReader brMeta = null, brTopK = null, brActual = null;
		String commonPath = Constant.getCommonPath();
		BufferedWriter bwTopKPrecision = null, bwTopKRecall = null, bwTopKF = null,
				bwNumberOfRecommendationTimes = null;

		int theK = theKOriginal;
		try
		{
			StringBuilder consoleLogBuilder = new StringBuilder();

			String metaCurrentLine, topKCurrentLine, actualCurrentLine;
			/*
			 * brMeta = new BufferedReader(new FileReader("/home/gunjan/meta.csv")); brTopK = new BufferedReader(new
			 * FileReader("/home/gunjan/dataRecommTop5.csv")); brActual = new BufferedReader(new
			 * FileReader("/home/gunjan/dataActual.csv"));
			 */
			File fileTopKPrecision = new File(
					commonPath + fileNamePhrase + timeCategory + "top" + theK + "Precision.csv");
			File fileTopKRecall = new File(commonPath + fileNamePhrase + timeCategory + "top" + theK + "Recall.csv");
			File fileTopKF = new File(commonPath + fileNamePhrase + timeCategory + "top" + theK + "FMeasure.csv");

			File fileNumberOfRecommendationTimes = new File(
					commonPath + fileNamePhrase + timeCategory + "NumOfRecommendationTimes.csv");
			// File fileAccuracy = new File("/home/gunjan/accuracy.csv");

			fileTopKPrecision.delete();
			fileTopKRecall.delete();
			fileTopKF.delete();
			fileNumberOfRecommendationTimes.delete();
			// fileAccuracy.delete();

			if (!fileTopKPrecision.exists())
			{
				fileTopKPrecision.createNewFile();
			}

			if (!fileTopKRecall.exists())
			{
				fileTopKRecall.createNewFile();
			}

			if (!fileTopKF.exists())
			{
				fileTopKF.createNewFile();
			}

			fileNumberOfRecommendationTimes.createNewFile();
			/*
			 * if (!fileAccuracy.exists()) { fileAccuracy.createNewFile(); }
			 */

			bwTopKPrecision = new BufferedWriter(new FileWriter(fileTopKPrecision.getAbsoluteFile()));
			bwTopKRecall = new BufferedWriter(new FileWriter(fileTopKRecall.getAbsoluteFile()));
			bwTopKF = new BufferedWriter(new FileWriter(fileTopKF.getAbsoluteFile()));

			bwNumberOfRecommendationTimes = new BufferedWriter(
					new FileWriter(fileNumberOfRecommendationTimes.getAbsoluteFile()));

			// bwAccuracy = new BufferedWriter(new FileWriter(fileAccuracy.getAbsoluteFile()));

			consoleLogBuilder.append("size of meta array=" + arrayMeta.size() + "     size of topK array="
					+ arrayTopK.size() + "   size of actual array=" + arrayActual.size() + "\n");

			bwNumberOfRecommendationTimes.write("," + timeCategory);
			bwNumberOfRecommendationTimes.newLine();

			for (int i = 0; i < arrayMeta.size(); i++) // iterating over users (or rows)
			{
				ArrayList<String> currentLineArray = arrayMeta.get(i);

				double topKPrecisionVal = -99, topKRecallVal = -99, accuracy = -99, topKFVal = -99;

				// bwNumberOfRecommendationTimes.write(ConnectDatabase.getUserNameFromDatabase(i) + ",");

				int[] userIDs = Constant.getUserIDs();
				bwNumberOfRecommendationTimes.write(userIDs[i] + ",");

				// int theK=0;
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered

				for (int j = 0; j < currentLineArray.size(); j++) // iterating over recommendation times (or columns)
				{
					int hourOfTheDay = getHourFromMetaString(currentLineArray.get(j));
					if (getTimeCategoryOfTheDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
							|| timeCategory.equals("All"))
					{
						countOfRecommendationTimesConsidered++;

						String actual = arrayActual.get(i).get(j);

						String[] topKStrings = arrayTopK.get(i).get(j).split("__"); // topK is of the form string:
																					// __a__b__c__d__e is of length 6...
																					// value at index 0 is empty.

						theK = theKOriginal;
						// System.out.println();

						if (topKStrings.length - 1 < theK) // for this RT we are not able to make K recommendations as
															// less than K recommendations are present.
						{
							// System.err.println
							consoleLogBuilder.append("Warning: For " + currentLineArray.get(j) + ", Only top "
									+ (topKStrings.length - 1) + " recommendation present while the asked for K is "
									+ theK + "\tDecreasing asked for K to " + (topKStrings.length - 1) + "\n");
							// +"\nWriting -999 values");
							theK = topKStrings.length - 1;
							// $topKPrecisionVal=-9999;
							// $topKRecallVal=-9999;
							// $topKFVal=-9999;
						}
						// theK=topKStrings.length-1;
						// $else
						// ${
						if (Constant.verbose)
						{
							consoleLogBuilder
									.append("topKString=arrayTopK.get(i).get(j)=" + arrayTopK.get(i).get(j) + "\n");
							consoleLogBuilder.append("actual string =" + actual + "\n");
						}
						int countOfOccurence = 0; // the number of times the actual item appears in the top K
													// recommended list. NOTE: in the current case will be always be
													// either 1
													// or 0.

						for (int y = 1; y <= theK; y++)
						{
							if (topKStrings[y].equalsIgnoreCase(actual))
							{
								countOfOccurence++;
							}
						}

						if (countOfOccurence > 1)
						{
							// System.err.println
							consoleLogBuilder.append(
									"Error: in writePrecisionRecallFMeasure(): the actual string appears multiple times in topK, which should not be the case as per our current algorithm.\n");
						}
						if (countOfOccurence > 0)
						{
							countOfOccurence = 1;
						}

						topKPrecisionVal = round((double) countOfOccurence / theKOriginal, 4);
						topKRecallVal = round((double) countOfOccurence / 1, 4); // since there is only one actual
																					// values

						if ((topKPrecisionVal + topKRecallVal) == 0)
						{
							topKFVal = 0;
						}
						else
						{
							topKFVal = 2 * ((topKPrecisionVal * topKRecallVal) / (topKPrecisionVal + topKRecallVal));
						}
						topKFVal = round(topKFVal, 4);

						bwTopKPrecision.write(topKPrecisionVal + ",");
						bwTopKRecall.write(topKRecallVal + ",");
						bwTopKF.write(topKFVal + ",");
						// bwAccuracy.write(accuracy+",");
						if (Constant.verboseEvaluationMetricsToConsole)
						{
							consoleLogBuilder.append("count-of-occurence-used=" + countOfOccurence + "         "
									+ "k-used=" + theK + " k-Original=" + theKOriginal + "\n");// +
																								// " /
																								// ="+round((double)countOfOccurence/theK,4));
							// $}
							consoleLogBuilder.append("top-" + theKOriginal + "-precision=" + topKPrecisionVal + "    "
									+ "top-" + theKOriginal + "-recall=" + topKRecallVal + "   top" + theKOriginal
									+ "F=" + topKFVal + "   accuracy=" + accuracy + "\n");
						}

					}
				} // end of current line array

				bwTopKPrecision.write("\n");
				bwTopKRecall.write("\n");
				bwTopKF.write("\n");

				bwNumberOfRecommendationTimes.write(countOfRecommendationTimesConsidered + "\n");
				// bwAccuracy.write("\n");

			}
			System.out.println(consoleLogBuilder.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				/*
				 * brMeta.close(); brTopK.close(); brActual.close();
				 */
				bwTopKPrecision.close();
				bwTopKRecall.close();
				bwTopKF.close();

				bwNumberOfRecommendationTimes.close();
				// bwAccuracy.close();

			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/**
	 * THIS MIGHT HAVE THE POSSIBILITY OF OPTIMISATION
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 */
	public static void writeMeanReciprocalRank(String fileNamePhrase, String timeCategory, int numUsers)
	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();
		try
		{
			File file = new File(commonPath + fileNamePhrase + timeCategory + "MeanReciprocalRank.csv");

			file.delete();
			file.createNewFile();

			// File validRTCountFile = new File(commonPath + fileNamePhrase + timeCategory +
			// "AvgReciprocalRankCountValidRT.csv");
			// validRTCountFile.delete();
			// validRTCountFile.createNewFile();
			// BufferedWriter bwValidRTCount = new BufferedWriter(new FileWriter(validRTCountFile.getAbsoluteFile()));

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(",MRR");
			// bwValidRTCount.write(",RTCount_RR");

			bw.newLine();
			// bwValidRTCount.newLine();

			for (int user = 0; user < numUsers; user++)
			{

				bw.write("User_" + user + ","); // TODO: currently this starts from User_0, change it to start from
												// User_1 but this will also require necessary changes in other
												// places
				// bwValidRTCount.write("User_" + user + ",");
				// for (int K = theKOriginal; K > 0; K--)
				// {
				BufferedReader br = new BufferedReader(
						new FileReader(commonPath + fileNamePhrase + timeCategory + "ReciprocalRank.csv"));
				String currentLine;

				if (Constant.verboseEvaluationMetricsToConsole)
				{
					System.out.println("Calculating MRR for user:" + user);
					System.out.println(
							("reading for MRR: " + commonPath + fileNamePhrase + timeCategory + "ReciprocalRank.csv"));
				}

				int lineNumber = 0;
				while ((currentLine = br.readLine()) != null)
				{
					if (lineNumber == user)
					{
						String[] rrValuesForThisUser = currentLine.split(","); // get avg of all these rr values
																				// concern: splitting on empty lines
																				// gives an array of length1, also
																				// splitting on one values line gives an
																				// array of length 1
						if (Constant.verboseEvaluationMetricsToConsole)
						{
							System.out.println("current rrValues line read=" + currentLine + " trimmed length="
									+ currentLine.trim().length());
						}
						System.out.println("#rr values for user(" + user + ") = " + rrValuesForThisUser.length);

						// double[] pValuesForThisUserForThisK = new double[tokensInCurrentLine.length];

						double MRRValueForThisUser = 0;
						double sum = 0;
						int countOfValidRRValues = 0;

						if (currentLine.trim().length() == 0)
						{
							System.out.println(" NOTE: line for user(" + user + ") is EMPTY for " + commonPath
									+ fileNamePhrase + timeCategory + "ReciprocalRank.csv");
						}

						else
						{ // calculate sum
							for (int i = 0; i < rrValuesForThisUser.length; i++)
							{
								double rrValueRead = Double.parseDouble(rrValuesForThisUser[i]);

								if (rrValueRead >= 0) // negative P value for a given RT for given K means that we were
														// unable to make K recommendation at that RT
								{
									countOfValidRRValues += 1;
									sum = sum + rrValueRead;
								}
							}
						}
						// bwValidRTCount.write(countOfValidPValues + ",");
						if (countOfValidRRValues == 0) // to avoid divide by zero exception
							countOfValidRRValues = 1;
						MRRValueForThisUser = round((double) sum / countOfValidRRValues, 4);

						System.out.println("Calculating MRR:" + sum + "/" + countOfValidRRValues);
						bw.write(MRRValueForThisUser + ",");

						break;
					}
					lineNumber++;
				}

				br.close();
				// }// end of K loop
				bw.newLine();
				// bwValidRTCount.newLine();
			}
			bw.close();
			// bwValidRTCount.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 */
	public static void writeAvgPrecisionsForAllKs(String fileNamePhrase, String timeCategory, int numUsers)
	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();
		try
		{
			File file = new File(commonPath + fileNamePhrase + timeCategory + "AvgPrecision.csv");

			file.delete();
			file.createNewFile();

			File validRTCountFile = new File(
					commonPath + fileNamePhrase + timeCategory + "AvgPrecisionCountValidRT.csv");
			validRTCountFile.delete();
			validRTCountFile.createNewFile();
			BufferedWriter bwValidRTCount = new BufferedWriter(new FileWriter(validRTCountFile.getAbsoluteFile()));

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (int K = theKOriginal; K > 0; K--)
			{
				bw.write(",Avg_Precision_Top" + K + "");
				bwValidRTCount.write(",RTCount_Avg_Precision_Top" + K + "");
			}

			bw.newLine();
			bwValidRTCount.newLine();

			for (int user = 0; user < numUsers; user++)
			{
				bw.write("User_" + user + ",");
				bwValidRTCount.write("User_" + user + ",");
				for (int K = theKOriginal; K > 0; K--)
				{
					BufferedReader br = new BufferedReader(
							new FileReader(commonPath + fileNamePhrase + timeCategory + "top" + K + "Precision.csv"));
					System.out.println(("reading for avg precision: " + commonPath + fileNamePhrase + timeCategory
							+ "top" + K + "Precision.csv"));
					String currentLine;

					int lineNumber = 0;
					while ((currentLine = br.readLine()) != null)
					{
						if (lineNumber == user)
						{
							String[] pValuesForThisUserForThisK = currentLine.split(","); // get avg of all these
																							// precision values concern:
																							// splitting on empty lines
																							// gives an array
																							// of length1, also
																							// splitting on one values
																							// line gives an array of
																							// length 1
							System.out.println("the current pValues line read=" + currentLine + " trimmed length="
									+ currentLine.trim().length());
							System.out.println("The number of p values for user(" + user + ") = "
									+ pValuesForThisUserForThisK.length);

							// double[] pValuesForThisUserForThisK = new double[tokensInCurrentLine.length];

							double avgPValueForThisUserForThisK = 0;
							double sum = 0;
							int countOfValidPValues = 0;

							if (currentLine.trim().length() == 0)
							{
								System.out.println(" NOTE: line for user(" + user + ") is EMPTY for " + commonPath
										+ fileNamePhrase + timeCategory + "top" + K + "Precision.csv");
							}

							else
							{ // calculate sum
								for (int i = 0; i < pValuesForThisUserForThisK.length; i++)
								{
									// pValuesForThisUserForThisK[i]=Double.parseDouble(tokensInCurrentLine[i]);
									// double toAdd=0;
									// try{
									// toAdd = Double.parseDouble(pValuesForThisUserForThisK[i]);
									// }
									//
									// catch(NumberFormatException e)
									// {
									// toAdd=0;
									// }
									//
									// sum+= toAdd;

									// if(pValuesForThisUserForThisK[i]!=null)
									// {
									// System.out.println(">>user="+user+" K="+K+" lineNumber(user)="+lineNumber+" is
									// not null ="+pValuesForThisUserForThisK[i]);
									// sum = sum + Double.parseDouble(pValuesForThisUserForThisK[i]);
									// }
									// else
									// System.out.println("pValuesForThisUserForThisK(K="+K+"[i="+i+"] is null");
									double pValueRead = Double.parseDouble(pValuesForThisUserForThisK[i]);

									if (pValueRead >= 0) // negative P value for a given RT for given K means that we
															// were unable to make K recommendation at that RT
									{
										countOfValidPValues += 1;
										sum = sum + pValueRead;

									}
								}
							}

							bwValidRTCount.write(countOfValidPValues + ",");

							if (countOfValidPValues == 0) // to avoid divide by zero exception
								countOfValidPValues = 1;
							avgPValueForThisUserForThisK = round((double) sum / countOfValidPValues, 4);

							if (Constant.verboseEvaluationMetricsToConsole)
							{
								System.out.println(
										"Calculating avg precision (K=" + K + "):" + sum + "/" + countOfValidPValues);
							}
							bw.write(avgPValueForThisUserForThisK + ",");

							break;
						}
						lineNumber++;
					}

					br.close();
				}
				bw.newLine();
				bwValidRTCount.newLine();
			}
			bw.close();
			bwValidRTCount.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 */
	public static void writeAvgRecallsForAllKs(String fileNamePhrase, String timeCategory, int numUsers)
	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();
		try
		{
			File file = new File(commonPath + fileNamePhrase + timeCategory + "AvgRecall.csv");
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			File validRTCountFile = new File(commonPath + fileNamePhrase + timeCategory + "AvgRecallCountValidRT.csv");
			validRTCountFile.delete();
			validRTCountFile.createNewFile();
			BufferedWriter bwValidRTCount = new BufferedWriter(new FileWriter(validRTCountFile.getAbsoluteFile()));

			for (int K = theKOriginal; K > 0; K--)
			{
				bw.write(",Avg_Recall_Top" + K + "");
				bwValidRTCount.write(",RTCount_Avg_Recall_Top" + K + "");
			}

			bw.newLine();
			bwValidRTCount.newLine();

			for (int user = 0; user < numUsers; user++)
			{
				bw.write("User_" + user + ",");
				bwValidRTCount.write("User_" + user + ",");

				for (int K = theKOriginal; K > 0; K--)
				{
					BufferedReader br = new BufferedReader(
							new FileReader(commonPath + fileNamePhrase + timeCategory + "top" + K + "Recall.csv"));

					String currentLine;

					int lineNumber = 0;
					while ((currentLine = br.readLine()) != null)
					{
						if (lineNumber == user)
						{
							String[] rValuesForThisUserForThisK = currentLine.split(",");
							// double[] pValuesForThisUserForThisK = new double[tokensInCurrentLine.length];

							double avgRValueForThisUserForThisK = 0;
							double sum = 0;
							double countOfValidRValues = 0;
							if (currentLine.trim().length() == 0)
							{
								System.out.println(" NOTE: line for user(" + user + ") is EMPTY for " + commonPath
										+ fileNamePhrase + timeCategory + "top" + K + "Precision.csv");
							}
							else
							{
								for (int i = 0; i < rValuesForThisUserForThisK.length; i++)
								{
									// pValuesForThisUserForThisK[i]=Double.parseDouble(tokensInCurrentLine[i]);
									double rValueRead = Double.parseDouble(rValuesForThisUserForThisK[i]);

									if (rValueRead >= 0)
									{
										countOfValidRValues += 1;
										sum = sum + rValueRead;
									}
								}
							}

							bwValidRTCount.write(countOfValidRValues + ",");

							if (countOfValidRValues == 0) // to avoid divide by zero exception
								countOfValidRValues = 1;
							avgRValueForThisUserForThisK = round((double) sum / countOfValidRValues, 4);
							if (Constant.verboseEvaluationMetricsToConsole)
							{
								System.out.println(
										"Calculating avg recall (K=" + K + "):" + sum + "/" + countOfValidRValues);
							}
							bw.write(avgRValueForThisUserForThisK + ",");
							break;
						}
						lineNumber++;
					}

					br.close();
				}
				bw.newLine();
				bwValidRTCount.newLine();
			}
			bw.close();
			bwValidRTCount.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void writeAvgFMeasuresForAllKs(String fileNamePhrase, String timeCategory, int numUsers)
	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();
		try
		{
			File file = new File(commonPath + fileNamePhrase + timeCategory + "AvgFMeasure.csv");

			file.delete();

			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			File validRTCountFile = new File(
					commonPath + fileNamePhrase + timeCategory + "AvgFMeasureCountValidRT.csv");
			validRTCountFile.delete();
			validRTCountFile.createNewFile();
			BufferedWriter bwValidRTCount = new BufferedWriter(new FileWriter(validRTCountFile.getAbsoluteFile()));

			for (int K = theKOriginal; K > 0; K--)
			{
				bw.write(",Avg_FMeasure_Top" + K + "");
				bwValidRTCount.write(",RTCount_Avg_FMeasure_Top" + K + "");
			}

			bw.newLine();
			bwValidRTCount.newLine();

			for (int user = 0; user < numUsers; user++)
			{
				bw.write("User_" + user + ",");
				bwValidRTCount.write("User_" + user + ",");
				for (int K = theKOriginal; K > 0; K--)
				{
					BufferedReader br = new BufferedReader(
							new FileReader(commonPath + fileNamePhrase + timeCategory + "top" + K + "FMeasure.csv"));

					String currentLine;

					int lineNumber = 0;
					while ((currentLine = br.readLine()) != null)
					{
						if (lineNumber == user)
						{
							String[] fValuesForThisUserForThisK = currentLine.split(","); // this is 1 in case of empty
																							// string
							// double[] pValuesForThisUserForThisK = new double[tokensInCurrentLine.length];

							double avgFValueForThisUserForThisK = 0;
							double sum = 0;
							double countOfValidFValues = 0;

							if (currentLine.trim().length() == 0)
							{
								System.out.println(" NOTE: line for user(" + user + ") is EMPTY for " + commonPath
										+ fileNamePhrase + timeCategory + "top" + K + "Precision.csv");
							}

							else
							{
								for (int i = 0; i < fValuesForThisUserForThisK.length; i++)

								{
									// pValuesForThisUserForThisK[i]=Double.parseDouble(tokensInCurrentLine[i]);
									double fValueRead = Double.parseDouble(fValuesForThisUserForThisK[i]);

									if (fValueRead >= 0)
									{
										countOfValidFValues += 1;
										sum = sum + Double.parseDouble(fValuesForThisUserForThisK[i]);
									}
								}
							}

							bwValidRTCount.write(countOfValidFValues + ",");

							if (countOfValidFValues == 0) // to avoid divide by zero exception
								countOfValidFValues = 1;
							avgFValueForThisUserForThisK = round((double) sum / countOfValidFValues, 4);
							if (Constant.verboseEvaluationMetricsToConsole)
							{
								System.out.println(
										"Calculating avg FMeasure (K=" + K + "):" + sum + "/" + countOfValidFValues);
							}
							bw.write(avgFValueForThisUserForThisK + ",");
							break;
						}
						lineNumber++;
					}

					br.close();
				}
				bw.newLine();
				bwValidRTCount.newLine();
			}
			bw.close();
			bwValidRTCount.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * public static void writeAvgRecallForAllKs() { //BufferedReader br= null;
	 * 
	 * try { File file = new File("/home/gunjan/AvgRecall.csv");
	 * 
	 * file.delete();
	 * 
	 * if (!file.exists()) { file.createNewFile(); }
	 * 
	 * FileWriter fw = new FileWriter(file.getAbsoluteFile()); BufferedWriter bw = new BufferedWriter(fw);
	 * 
	 * for(int K=theKOriginal;K>0;K--) { bw.write(",Avg_Recall_Top"+K); }
	 * 
	 * bw.newLine(); for(int K=theKOriginal;K>0;K--) { BufferedReader br =new BufferedReader(new
	 * FileReader("/home/gunjan/top"+K+"Recall.csv"));
	 * 
	 * String currentLine;
	 * 
	 * while ((currentLine = br.readLine()) != null) { String[] rValuesForThisUserForThisK=currentLine.split(",");
	 * //double[] pValuesForThisUserForThisK = new double[tokensInCurrentLine.length];
	 * 
	 * double avgRValueForThisUserForThisK = 0; double sum=0;
	 * 
	 * for(int i=0;i<rValuesForThisUserForThisK.length;i++) {
	 * //pValuesForThisUserForThisK[i]=Double.parseDouble(tokensInCurrentLine[i]); sum = sum +
	 * Double.parseDouble(rValuesForThisUserForThisK[i]); }
	 * 
	 * avgRValueForThisUserForThisK = sum/rValuesForThisUserForThisK.length;
	 * System.out.println("Calculating avg recall (K="+K+"):"+sum+"/"+rValuesForThisUserForThisK.length); } br.close();
	 * } bw.close(); }
	 * 
	 * catch (IOException e) { e.printStackTrace(); } }
	 * 
	 * 
	 * 
	 * 
	 * public static void writeAvgFForAllKs() { //BufferedReader br= null;
	 * 
	 * try { File file = new File("/home/gunjan/AvgFMeasure.csv");
	 * 
	 * file.delete();
	 * 
	 * if (!file.exists()) { file.createNewFile(); }
	 * 
	 * FileWriter fw = new FileWriter(file.getAbsoluteFile()); BufferedWriter bw = new BufferedWriter(fw);
	 * 
	 * for(int K=theKOriginal;K>0;K--) { bw.write(",Avg_FMeasure_Top"+K); }
	 * 
	 * bw.newLine(); for(int K=theKOriginal;K>0;K--) { BufferedReader br =new BufferedReader(new
	 * FileReader("/home/gunjan/top"+K+"FMeasure.csv"));
	 * 
	 * String currentLine;
	 * 
	 * while ((currentLine = br.readLine()) != null) { String[] fValuesForThisUserForThisK=currentLine.split(",");
	 * //double[] pValuesForThisUserForThisK = new double[tokensInCurrentLine.length];
	 * 
	 * double avgFValueForThisUserForThisK = 0; double sum=0;
	 * 
	 * for(int i=0;i<fValuesForThisUserForThisK.length;i++) {
	 * //pValuesForThisUserForThisK[i]=Double.parseDouble(tokensInCurrentLine[i]); sum = sum +
	 * Double.parseDouble(fValuesForThisUserForThisK[i]); }
	 * 
	 * avgFValueForThisUserForThisK = sum/fValuesForThisUserForThisK.length;
	 * System.out.println("Calculating avg recall (K="+K+"):"+sum+"/"+fValuesForThisUserForThisK.length); } br.close();
	 * } bw.close(); }
	 * 
	 * catch (IOException e) { e.printStackTrace(); } }
	 */

	public static int getHourFromMetaString(String metaString) // example metaString: 1_10/4/2014_18:39:3
	{
		String[] splitted1 = metaString.split(Pattern.quote("_"));
		String[] splitted2 = splitted1[2].split(Pattern.quote(":"));

		return Integer.valueOf(splitted2[0]);
	}

	public static String getTimeCategoryOfTheDay(int hour)
	{
		String timeCategory = null;

		if (hour >= 0 && hour < 12)
		{
			timeCategory = timeCategories[1];
		}

		else if (hour >= 12 && hour < 16)
		{
			timeCategory = timeCategories[2];
		}

		else if (hour >= 16 && hour <= 23)
		{
			timeCategory = timeCategories[3];
		}

		return timeCategory;
	}

	public static double round(double value, int places)
	{
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

}
