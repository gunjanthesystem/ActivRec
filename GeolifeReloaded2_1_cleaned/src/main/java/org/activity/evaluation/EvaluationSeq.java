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

import org.activity.constants.Constant;
import org.activity.constants.VerbosityConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;

/**
 * (note: In earlier version (before 14 April 2015, this class was name as TestStats.java)
 * 
 * @author gunjan
 *
 */
public class EvaluationSeq
{
	public static String commonPath;// =Constant.commonPath;

	static final int theKOriginal = 5;
	public static final String[] timeCategories = { "All" };// }, "Morning", "Afternoon", "Evening" };

	public EvaluationSeq(int seqLength)
	{
		try
		{
			for (int i = 0; i < seqLength; i++)
			{
				Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArrays =
						readDataForSeqIndex(i);
				ArrayList<ArrayList<String>> arrayMeta = readArrays.getFirst();
				ArrayList<ArrayList<String>> arrayTopK = readArrays.getSecond();// new ArrayList<ArrayList<String>>();
				ArrayList<ArrayList<String>> arrayActual = readArrays.getThird();

				doEvaluation(arrayMeta, arrayTopK, arrayActual, timeCategories, Constant.EvalPrecisionRecallFMeasure,
						theKOriginal, "Algo");

				// if (Constant.DoBaselineOccurrence)
				// {
				// doEvaluation(arrayMeta, arrayBaselineOccurrence, arrayActual, timeCategories,
				// Constant.EvalPrecisionRecallFMeasure, theKOriginal, "BaselineOccurrence");
				// }
				// if (Constant.DoBaselineDuration)
				// {
				// doEvaluation(arrayMeta, arrayBaselineDuration, arrayActual, timeCategories,
				// Constant.EvalPrecisionRecallFMeasure, theKOriginal, "BaselineDuration");
				// }
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// System.out.println("All test stats done");
		// PopUps.showMessage("All test stats done");

	}

	/**
	 * 
	 * @param seqIndex
	 * @return
	 */
	public static Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>
			readDataForSeqIndex(int seqIndex)
	{
		commonPath = Constant.getCommonPath();
		System.out.println("Inside Evaluation: common path is:" + commonPath);

		BufferedReader brMeta = null, brTopK = null, brActual = null;
		// , brBaseLineOccurrence = null,brBaseLineDuration = null, brCurrentTargetSame = null;

		/**
		 * A 2 dimensional arraylist, with rows corresponding to user and columns corresponding to recommendation times
		 * and a cell contains the meta information (userid_dateOfRt_timeOfRt) for the corresponding user for
		 * corresponding recommendation time
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

		/**
		 * A 2 dimensional arraylist, with rows corresponding to user and columns corresponding to recommendation times
		 * and a cell boolean value representing whether the current and target activity names were same for this
		 * recommendation time
		 */
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
			brTopK = new BufferedReader(
					new FileReader(commonPath + "dataRankedRecommendationWithoutScores" + seqIndex + ".csv"));// /dataRecommTop5.csv"));
			brActual = new BufferedReader(new FileReader(commonPath + "dataActual" + seqIndex + ".csv"));

			// brCurrentTargetSame = new BufferedReader(new FileReader(commonPath +
			// "metaIfCurrentTargetSameWriter.csv"));
			// brBaseLineOccurrence = new BufferedReader(new FileReader(commonPath + "dataBaseLineOccurrence.csv"));
			// brBaseLineDuration = new BufferedReader(new FileReader(commonPath + "dataBaseLineDuration.csv"));

			StringBuilder consoleLogBuilder = new StringBuilder();

			Triple<ArrayList<ArrayList<String>>, Integer, String> metaExtracted = extractDataFromFile(brMeta, "meta");
			arrayMeta = metaExtracted.getFirst();
			int countOfLinesMeta = metaExtracted.getSecond();
			consoleLogBuilder.append(metaExtracted.getThird());

			Triple<ArrayList<ArrayList<String>>, Integer, String> topKExtracted = extractDataFromFile(brTopK, "topK");
			arrayTopK = topKExtracted.getFirst();
			int countOfLinesTopK = topKExtracted.getSecond();
			consoleLogBuilder.append(topKExtracted.getThird());

			Triple<ArrayList<ArrayList<String>>, Integer, String> actualExtracted =
					extractDataFromFile(brActual, "actual");
			arrayActual = actualExtracted.getFirst();
			int countOfLinesActual = actualExtracted.getSecond();
			consoleLogBuilder.append(actualExtracted.getThird());

			consoleLogBuilder.append("\n number of actual lines =" + countOfLinesTopK + "\n");
			consoleLogBuilder.append("size of meta array=" + arrayMeta.size() + "     size of topK array="
					+ arrayTopK.size() + "   size of actual array=" + arrayMeta.size() + "\n");
			// + " size of current target same array=" + arrayCurrentTargetSame.size() + "\n");

			if (ComparatorUtils.areAllEqual(countOfLinesMeta, countOfLinesTopK, countOfLinesActual, arrayMeta.size(),
					arrayTopK.size(), arrayActual.size()) == false)
			{
				System.err.println(PopUps.getCurrentStackTracedErrorMsg("Error line numbers mismatch: countOfLinesMeta="
						+ countOfLinesMeta + ",countOfLinesTopK=" + countOfLinesTopK + " countOfLinesActual="
						+ countOfLinesActual + ", arrayMeta.size()=" + arrayMeta.size() + ", arrayTopK.size()="
						+ arrayTopK.size() + ", arrayActual.size()=" + arrayActual.size()));
			}

			System.out.println(consoleLogBuilder.toString());
			consoleLogBuilder.setLength(0); // empty the consolelog stringbuilder
			// //////////////////////////// finished creating and populating the data structures needed

			ReadingFromFile.closeBufferedReaders(brMeta, brTopK, brActual);
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
		return new Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(
				arrayMeta, arrayActual, arrayTopK);
	}

	/**
	 * writePrecisionRecallFMeasure, writeReciprocalRank, writeMeanReciprocalRank, writeAvgPrecisionsForAllKs,
	 * writeAvgRecallsForAllKs, writeAvgFMeasuresForAllKs
	 * 
	 * @param arrayMeta
	 * @param arrayTopK
	 * @param arrayActual
	 * @param timeCategories
	 * @param evalPrecisionRecallFMeasure
	 * @param theKOriginal
	 */
	private static void doEvaluation(ArrayList<ArrayList<String>> arrayMeta, ArrayList<ArrayList<String>> arrayTopK,
			ArrayList<ArrayList<String>> arrayActual, String[] timeCategories, boolean evalPrecisionRecallFMeasure,
			int theKOriginal, String algoLabel)
	{
		int numOfUsers = arrayTopK.size();
		for (String timeCategory : timeCategories)
		{
			writeReciprocalRank(algoLabel, timeCategory, arrayMeta, arrayTopK, arrayActual);
			writeMeanReciprocalRank(algoLabel, timeCategory, numOfUsers); // average over data points

			if (evalPrecisionRecallFMeasure)
			{
				for (int theK = theKOriginal; theK > 0; theK--)
				{
					writePrecisionRecallFMeasure(algoLabel, timeCategory, theK, arrayMeta, arrayTopK, arrayActual);
				}

				writeAvgPrecisionsForAllKs(algoLabel, timeCategory, numOfUsers); // average over data points
				writeAvgRecallsForAllKs(algoLabel, timeCategory, numOfUsers);
				writeAvgFMeasuresForAllKs(algoLabel, timeCategory, numOfUsers);
			}
		}
	}

	/**
	 * 
	 * @param dataToRead
	 * @param label
	 * @return Triple (arrayData, countOfLinesData, log.toString())
	 * @throws IOException
	 */
	private static Triple<ArrayList<ArrayList<String>>, Integer, String> extractDataFromFile(BufferedReader dataToRead,
			String label) throws IOException
	{
		// outer arraylist: rows, inner arraylist: cols
		ArrayList<ArrayList<String>> arrayData = new ArrayList<ArrayList<String>>();
		StringBuilder log = new StringBuilder();
		int countOfLinesData = 0;

		String dataCurrentLine;
		while ((dataCurrentLine = dataToRead.readLine()) != null)
		{
			ArrayList<String> currentLineArray = new ArrayList<String>();
			// System.out.println(metaCurrentLine);
			String[] tokensInCurrentDataLine = RegexUtils.patternComma.split(dataCurrentLine);
			// System.out.println("number of tokens in this meta line=" + tokensInCurrentMetaLine.length);
			log.append(label + " line num:" + (countOfLinesData + 1) + "#tokensInLine:" + tokensInCurrentDataLine.length
					+ "\n");

			for (int i = 0; i < tokensInCurrentDataLine.length; i++)
			{
				currentLineArray.add(tokensInCurrentDataLine[i]);
			}

			arrayData.add(currentLineArray);
			countOfLinesData++;
		}
		log.append("\n number of " + label + " lines =" + countOfLinesData + "\n");

		return new Triple<ArrayList<ArrayList<String>>, Integer, String>(arrayData, countOfLinesData, log.toString());
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
		String commonPath = Constant.getCommonPath();
		BufferedWriter bwRR = null;
		try
		{
			String metaCurrentLine, topKCurrentLine, actualCurrentLine;
			bwRR = WritingToFile.getBWForNewFile(commonPath + fileNamePhrase + timeCategory + "ReciprocalRank.csv");
			System.out.println("size of meta array=" + arrayMeta.size() + "     size of topK array=" + arrayTopK.size()
					+ "   size of actual array=" + arrayActual.size());

			for (int i = 0; i < arrayMeta.size(); i++) // iterating over users (or rows)
			{
				ArrayList<String> currentLineArray = arrayMeta.get(i);
				// $$System.out.println("Calculating RR for user:" + i);
				double RR = -99;
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered

				for (int j = 0; j < currentLineArray.size(); j++) // iterating over recommendation times (or columns)
				{
					int hourOfTheDay = getHourFromMetaString(currentLineArray.get(j));
					if (DateTimeUtils.getTimeCategoryOfDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
							|| timeCategory.equals("All"))
					{
						countOfRecommendationTimesConsidered++;

						String actual = arrayActual.get(i).get(j);

						String[] topKString = RegexUtils.patternDoubleUnderScore.split(arrayTopK.get(i).get(j));
						// $$ arrayTopK.get(i).get(j).split("__");
						// topK is of the form string: __a__b__c__d__e is of length 6...
						// value at index 0 is empty.

						int rank = -99;
						for (int y = 1; y <= topKString.length - 1; y++)
						{
							if (topKString[y].equalsIgnoreCase(actual))
							{
								rank = y;
								break;// assuming that the actual occurs only once in the recommended list
							}
						}

						if (rank != -99)
						{
							RR = round((double) 1 / rank, 4);
						}
						else
						{
							RR = 0; // assuming the rank is at infinity
						}
						bwRR.write(RR + ",");
						if (VerbosityConstants.verbose)
						{
							System.out.println("topKString=arrayTopK.get(i).get(j)=" + arrayTopK.get(i).get(j));
							System.out.println("actual string =" + actual);
							System.out.println("RR = " + RR);
						}
					}
				} // end of current line array
				bwRR.write("\n");
			}
			bwRR.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
			File fileTopKPrecision =
					new File(commonPath + fileNamePhrase + timeCategory + "top" + theK + "Precision.csv");
			File fileTopKRecall = new File(commonPath + fileNamePhrase + timeCategory + "top" + theK + "Recall.csv");
			File fileTopKF = new File(commonPath + fileNamePhrase + timeCategory + "top" + theK + "FMeasure.csv");

			File fileNumberOfRecommendationTimes =
					new File(commonPath + fileNamePhrase + timeCategory + "NumOfRecommendationTimes.csv");
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

			bwNumberOfRecommendationTimes =
					new BufferedWriter(new FileWriter(fileNumberOfRecommendationTimes.getAbsoluteFile()));

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
					if (DateTimeUtils.getTimeCategoryOfDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
							|| timeCategory.equals("All"))
					{
						countOfRecommendationTimesConsidered++;

						String actual = arrayActual.get(i).get(j);

						String[] topKStrings = RegexUtils.patternDoubleUnderScore.split(arrayTopK.get(i).get(j));
						// arrayTopK.get(i).get(j).split("__");
						// topK is of the form string: __a__b__c__d__e is of length 6...
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
						if (VerbosityConstants.verbose)
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
						if (VerbosityConstants.verboseEvaluationMetricsToConsole)
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
				WritingToFile.closeBufferedWriters(bwTopKPrecision, bwTopKRecall, bwTopKF,
						bwNumberOfRecommendationTimes);
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
		String commonPath = Constant.getCommonPath();
		try
		{
			BufferedWriter bw = WritingToFile
					.getBWForNewFile(commonPath + fileNamePhrase + timeCategory + "MeanReciprocalRank.csv");
			bw.write(",MRR\n");

			for (int user = 0; user < numUsers; user++)
			{
				bw.write("User_" + user + ","); // TODO: currently this starts from User_0, change it to start from
												// User_1 but this will also require necessary changes in other
												// places
				BufferedReader br = new BufferedReader(
						new FileReader(commonPath + fileNamePhrase + timeCategory + "ReciprocalRank.csv"));
				String currentLine;

				if (VerbosityConstants.verboseEvaluationMetricsToConsole)
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
						String[] rrValuesForThisUser = RegexUtils.patternComma.split(currentLine);
						// get avg of all these rr values
						// concern: splitting on empty lines gives an array of length1, also splitting on one values
						// line gives an array of length 1
						if (VerbosityConstants.verboseEvaluationMetricsToConsole)
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

								if (rrValueRead >= 0) // negative rr value for a given RT for given K means that we were
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

						if (VerbosityConstants.verboseEvaluationMetricsToConsole)
						{
							System.out.println("Calculating MRR:" + sum + "/" + countOfValidRRValues);
						}
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

			File validRTCountFile =
					new File(commonPath + fileNamePhrase + timeCategory + "AvgPrecisionCountValidRT.csv");
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
							String[] pValuesForThisUserForThisK = RegexUtils.patternComma.split(currentLine);
							// currentLine.split(",");
							// get avg of all these precision values
							// concern: splitting on empty lines gives an array of length1, also splitting on one values
							// line gives an array of length 1
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

							if (VerbosityConstants.verboseEvaluationMetricsToConsole)
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
							String[] rValuesForThisUserForThisK = RegexUtils.patternComma.split(currentLine);
							// currentLine.split(",");
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
							if (VerbosityConstants.verboseEvaluationMetricsToConsole)
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

	/**
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 */
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

			File validRTCountFile =
					new File(commonPath + fileNamePhrase + timeCategory + "AvgFMeasureCountValidRT.csv");
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
							String[] fValuesForThisUserForThisK = RegexUtils.patternComma.split(currentLine);
							// currentLine.split(","); // this is 1 in case of empty string
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
							if (VerbosityConstants.verboseEvaluationMetricsToConsole)
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

	/**
	 * changed on 9 Feb to use percompiled regex patterns to improve string split performance correctness verified
	 * 
	 * @param metaString
	 * @return
	 */
	public static int getHourFromMetaString(String metaString) // example metaString: 1_10/4/2014_18:39:3
	{
		String[] splitted1 = RegexUtils.patternUnderScore.split(metaString);
		String[] splitted2 = RegexUtils.patternColon.split(splitted1[2]);
		return Integer.valueOf(splitted2[0]);
	}

	/**
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static double round(double value, int places)
	{
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

}
