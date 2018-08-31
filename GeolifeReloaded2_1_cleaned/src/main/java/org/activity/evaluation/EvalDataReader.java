package org.activity.evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.activity.io.ReadingFromFile;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.RegexUtils;

public class EvalDataReader
{

	/**
	 * 
	 * @param dataToRead
	 * @param labelForLog
	 * @return Triple (arrayData, countOfLinesData, log.toString())
	 * @throws IOException
	 */
	public static Triple<ArrayList<ArrayList<String>>, Integer, String> extractDataFromFile(BufferedReader dataToRead,
			String labelForLog, boolean verbose) throws IOException
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
			String[] tokensInCurrentDataLine = RegexUtils.patternComma.split(dataCurrentLine, -1);// ends with comma
			// -1 argument added on //changed on 20 July 2018
			// System.out.println("number of tokens in this meta line=" + tokensInCurrentMetaLine.length);
			if (verbose)
			{
				log.append(labelForLog + " line num:" + (countOfLinesData + 1) + "#tokensInLine:"
						+ tokensInCurrentDataLine.length + "\n");
			}
			// for (int i = 0; i < tokensInCurrentDataLine.length; i++)
			for (int i = 0; i < tokensInCurrentDataLine.length - 1; i++)// changed on 20 July 2018
			{
				currentLineArray.add(tokensInCurrentDataLine[i]);
			}
	
			// Start of sanity check 20 July 2018
			long numOfCommas = dataCurrentLine.chars().filter(num -> num == ',').count();
			Sanity.eq(numOfCommas, currentLineArray.size(),
					"Error in extractDataFromFile for dataToRead = " + dataToRead + " lineNum= "
							+ (countOfLinesData + 1) + " numOfCommas=" + numOfCommas + " currentLineArray.size()= "
							+ currentLineArray.size() + "\n currentLineArray = " + currentLineArray
							+ "\ndataCurrentLine= " + dataCurrentLine + "\ntokensInCurrentDataLine="
							+ tokensInCurrentDataLine);
			if (false)
			{
				System.out.println(" currentLineArray.size()= " + currentLineArray.size() + "\n currentLineArray = "
						+ currentLineArray + "\ndataCurrentLine= " + dataCurrentLine + "\ntokensInCurrentDataLine="
						+ tokensInCurrentDataLine);
			}
			// End of sanity check 20 July 2018
	
			arrayData.add(currentLineArray);
			countOfLinesData++;
		}
		log.append("\n number of " + labelForLog + " lines =" + countOfLinesData + "\n");
	
		return new Triple<ArrayList<ArrayList<String>>, Integer, String>(arrayData, countOfLinesData, log.toString());
	}

	/**
	 * 
	 * @param pathToReadResults
	 * @param predictionDataFileName
	 * @param actualDataFileName
	 * @param commonPath
	 * @param verbose
	 * @return Triple(arrayMeta, arrayActual, arrayTopK)
	 */
	public static Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readDataMetaActualTopK(
			String pathToReadResults, String predictionDataFileName, String actualDataFileName, String commonPath,
			boolean verbose)
	{
		// commonPath = Constant.getCommonPath();
		System.out.println("Inside readDataForSeqIndex: common path is:" + commonPath + "\npathToReadResults="
				+ pathToReadResults + "\npredictionDataFileName=" + predictionDataFileName + "\nactualDataFileName="
				+ actualDataFileName);
	
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
			brMeta = new BufferedReader(new FileReader(pathToReadResults + "meta.csv"));
			brTopK = new BufferedReader(new FileReader(pathToReadResults + predictionDataFileName));
			// "dataRecommSequenceWithScore.csv"));// /dataRecommTop5.csv"));
			brActual = new BufferedReader(new FileReader(pathToReadResults + actualDataFileName));
			// "dataActualSequence.csv"));
	
			// brCurrentTargetSame = new BufferedReader(new FileReader(commonPath +
			// "metaIfCurrentTargetSameWriter.csv"));
			// brBaseLineOccurrence = new BufferedReader(new FileReader(commonPath + "dataBaseLineOccurrence.csv"));
			// brBaseLineDuration = new BufferedReader(new FileReader(commonPath + "dataBaseLineDuration.csv"));
	
			StringBuilder consoleLogBuilder = new StringBuilder();
	
			Triple<ArrayList<ArrayList<String>>, Integer, String> metaExtracted = extractDataFromFile(brMeta, "meta",
					verbose);
			arrayMeta = metaExtracted.getFirst();
			int countOfLinesMeta = metaExtracted.getSecond();
			int countOfTotalMetaToken = metaExtracted.getFirst().stream().mapToInt(v -> v.size()).sum();
			consoleLogBuilder.append(metaExtracted.getThird());
	
			Triple<ArrayList<ArrayList<String>>, Integer, String> topKExtracted = extractDataFromFile(brTopK, "topK",
					verbose);
			arrayTopK = topKExtracted.getFirst();
			int countOfLinesTopK = topKExtracted.getSecond();
			int countOfTotalTopKToken = topKExtracted.getFirst().stream().mapToInt(v -> v.size()).sum();
			consoleLogBuilder.append(topKExtracted.getThird());
	
			Triple<ArrayList<ArrayList<String>>, Integer, String> actualExtracted = extractDataFromFile(brActual,
					"actual", verbose);
			arrayActual = actualExtracted.getFirst();
			int countOfLinesActual = actualExtracted.getSecond();
			int countOfTotalActualToken = actualExtracted.getFirst().stream().mapToInt(v -> v.size()).sum();
			consoleLogBuilder.append(actualExtracted.getThird());
	
			consoleLogBuilder.append("\n number of actual lines =" + countOfLinesTopK + "\n");
			consoleLogBuilder.append("size of meta array=" + arrayMeta.size() + "     size of topK array="
					+ arrayTopK.size() + "   size of actual array=" + arrayMeta.size() + "\n");
			consoleLogBuilder.append("countOfTotalMetaToken=" + countOfTotalMetaToken + "     countOfTotalTopKToken="
					+ countOfTotalTopKToken + "   countOfTotalActualToken=" + countOfTotalActualToken + "\n");
			// + " size of current target same array=" + arrayCurrentTargetSame.size() + "\n");
	
			if (ComparatorUtils.areAllEqual(countOfLinesMeta, countOfLinesTopK, countOfLinesActual, arrayMeta.size(),
					arrayTopK.size(), arrayActual.size()) == false)
			{
				System.err.println(PopUps.getTracedErrorMsg("Error line numbers mismatch: countOfLinesMeta="
						+ countOfLinesMeta + ",countOfLinesTopK=" + countOfLinesTopK + " countOfLinesActual="
						+ countOfLinesActual + ", arrayMeta.size()=" + arrayMeta.size() + ", arrayTopK.size()="
						+ arrayTopK.size() + ", arrayActual.size()=" + arrayActual.size()));
			}
			if (ComparatorUtils.areAllEqual(countOfTotalMetaToken, countOfTotalTopKToken,
					countOfTotalActualToken) == false)
			{
				System.err.println(PopUps.getTracedErrorMsg("Error line numbers mismatch: countOfTotalMetaToken="
						+ countOfTotalMetaToken + ",countOfTotalTopKToken=" + countOfTotalTopKToken
						+ " countOfTotalActualToken=" + countOfTotalActualToken));
			}
	
			System.out.println(consoleLogBuilder.toString());
			consoleLogBuilder.setLength(0); // empty the consolelog stringbuilder
			// //////////////////////////// finished creating and populating the data structures needed
	
			ReadingFromFile.closeBufferedReaders(brMeta, brTopK, brActual);
		}
	
		catch (IOException e)
		{
			e.printStackTrace();
			PopUps.showException(e, "org.activity.evaluation.EvaluationSeq.readDataForSeqIndex(int, String, boolean)");
		}
		return new Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(
				arrayMeta, arrayActual, arrayTopK);
	}

}
