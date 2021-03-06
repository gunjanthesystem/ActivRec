package org.activity.evaluation;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.controller.SuperController;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.SFTPFile;
import org.activity.io.ServerUtils;
import org.activity.io.WToFile;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;

import com.jcraft.jsch.Session;

/**
 * To do signficance tests on results over users
 * 
 * @since 25 Jan 2018
 * @author gunjan
 *
 */
public class ResultsDistributionEvaluation
{
	static final int port = 22;
	// static final int firstToMax = 3;
	static final int[] firstToMaxInOrder = { 1 };// 3, 2, 1 };
	static final String userSetlabels[] = { "" };// , "B", "C", "D", "E" };
	static final boolean tempDisable20July2018 = true;
	static final int firstToMax = 1;

	public ResultsDistributionEvaluation()
	{
	}

	public static void main20Nov(String args[])
	{
		String commonPath = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_NOV20ED-1.0STimeDurDistTrStartGeoEndGeoAvgAltStFilter0hrsNoTTFilter/All/MatchingUnit3.0/";
		writeCorrelationBetweenDistancesOverCands(commonPath, false);
	}

	public static void writeCorrelationBetweenDistancesOverCands(String commonPath,
			boolean useRTVerseNormalisationForED)
	{// String commonPath =
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_NOV20ED-1.0STimeDurDistTrStartGeoEndGeoAvgAltStFilter0hrsNoTTFilter/All/MatchingUnit3.0/";

		try
		{
			if (useRTVerseNormalisationForED)
			{
				int indexOfUserID = 0, indexOfnormActDistForThisCand = 10, indexOfmeanOverAOsNormFDForThisCand = 12,
						indexOfresultantEditDist = 16, indexOfActDistForThisCand = 9;
				// indexOfsumOfNormFDsOverAOsOfThisCand = 11;

				String distancePerCandFileName = "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachCand.csv";

				mainGetCorrelationBetweenAEDFED(commonPath + distancePerCandFileName, indexOfnormActDistForThisCand,
						indexOfmeanOverAOsNormFDForThisCand, false, indexOfUserID, "UserID",
						commonPath + "PearCorrNormAEDFED.csv", true);

				// there is no RawFED in this case, since the FEDis computed using already normalised feature
				// diffs,hence we
				// find correlation between RawAED and indexOfmeanOverAOsNormFDForThisCand
				mainGetCorrelationBetweenAEDFED(commonPath + distancePerCandFileName, indexOfActDistForThisCand,
						indexOfmeanOverAOsNormFDForThisCand, false, indexOfUserID, "UserID",
						commonPath + "PearCorrRawAEDFED.csv", true);

				mainGetCorrelationBetweenAEDFED(commonPath + distancePerCandFileName, indexOfnormActDistForThisCand,
						indexOfresultantEditDist, false, indexOfUserID, "UserID",
						commonPath + "PearCorrNormAEDResultantDist.csv", true);

				mainGetCorrelationBetweenAEDFED(commonPath + distancePerCandFileName,
						indexOfmeanOverAOsNormFDForThisCand, indexOfresultantEditDist, false, indexOfUserID, "UserID",
						commonPath + "PearCorrNormFEDResultantDist.csv", true);
			}
			else
			{
				int indexOfUserID = 0, indexOfNormAED = 4, indexOfNormFED = 5, indexOfTotalDis = 6, indexOfRawAED = 7,
						indexOfRawFED = 8;

				String distancePerCandFileName = "DistanceDistribution.csv";

				mainGetCorrelationBetweenAEDFED(commonPath + distancePerCandFileName, indexOfNormAED, indexOfNormFED,
						false, indexOfUserID, "UserID", commonPath + "PearCorrNormAEDFED.csv", true);

				mainGetCorrelationBetweenAEDFED(commonPath + distancePerCandFileName, indexOfRawAED, indexOfRawFED,
						false, indexOfUserID, "UserID", commonPath + "PearCorrRawAEDFED.csv", true);

				mainGetCorrelationBetweenAEDFED(commonPath + distancePerCandFileName, indexOfNormAED, indexOfTotalDis,
						false, indexOfUserID, "UserID", commonPath + "PearCorrNormAEDResultantDist.csv", true);

				mainGetCorrelationBetweenAEDFED(commonPath + distancePerCandFileName, indexOfNormFED, indexOfTotalDis,
						false, indexOfUserID, "UserID", commonPath + "PearCorrNormFEDResultantDist.csv", true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param fileToRead
	 * @param column1Index
	 * @param column2Index
	 * @param hasHeader
	 * @param byColumnIndex
	 * @param byLabel
	 * @param absFileNameToWrite
	 * @param isAlreadySortedByByLabel
	 * @since 20 Nov 2018
	 */
	public static void mainGetCorrelationBetweenAEDFED(String fileToRead, int column1Index, int column2Index,
			boolean hasHeader, int byColumnIndex, String byLabel, String absFileNameToWrite,
			boolean isAlreadySortedByByLabel)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Double> vals1 = (ArrayList<Double>) ReadingFromFile.oneColumnReaderDouble(fileToRead, ",",
				column1Index, hasHeader);
		ArrayList<Double> vals2 = (ArrayList<Double>) ReadingFromFile.oneColumnReaderDouble(fileToRead, ",",
				column2Index, hasHeader);
		List<String> byCol = ReadingFromFile.oneColumnReaderString(fileToRead, ",", byColumnIndex, hasHeader);

		// System.out.println(vals1.size() + " " + vals2.size() + " " + byCol.size());
		// compute overall pearson correlation
		double overallPearsonCorr = StatsUtils.getPearsonCorrelation(vals1, vals2);
		sb.append("overallPearsonCorr," + overallPearsonCorr + "\n");

		if (isAlreadySortedByByLabel)
		{
			String prevValOfByLabel = "";
			ArrayList<Double> vals1ForByLabel = new ArrayList<>();
			ArrayList<Double> vals2ForByLabel = new ArrayList<>();
			// Assuming the read file is sorted by IDs

			for (int rowIndex = 0; rowIndex < vals1.size(); rowIndex++)
			{
				String valOfByLabelForThisRow = byCol.get(rowIndex);
				if (rowIndex == vals1.size() - 1 /* for last val */
						|| (valOfByLabelForThisRow.equals(prevValOfByLabel) == false && prevValOfByLabel.length() > 0))
				{
					// System.out.println(vals1ForByLabel.size() + " " + vals2ForByLabel.size() + " " + byCol.size());
					double thisByLabelPearsonCorr = StatsUtils.getPearsonCorrelation(vals1ForByLabel, vals2ForByLabel);
					sb.append(prevValOfByLabel + "," + thisByLabelPearsonCorr + "," + vals1ForByLabel.size() + "\n");
					vals1ForByLabel.clear();
					vals2ForByLabel.clear();
				}

				vals1ForByLabel.add(vals1.get(rowIndex));
				vals2ForByLabel.add(vals2.get(rowIndex));
				prevValOfByLabel = valOfByLabelForThisRow;
			}
		}
		// ReadingFromFile.twoColumnReaderString(fileToRead, ",", column1Index, column2Index, hasHeader);
		// List<List<String>> allLines = ReadingFromFile.readLinesIntoListOfLists(fileToRead, ",");
		// ReadingFromFile.nColumnReaderStringLargeFile(inputStream, delimiter, hasHeader, verboseReading)
		WToFile.writeToNewFile(sb.toString(), absFileNameToWrite);
	}

	public static void main20Nov2018(String args[])
	{
		// $$mainBefore19July();
		// String[] dimensionPhrase = { "Fltr_on_Top1Loc" };
		String[] pfFilterNames = { "" };
		// , "SecDim", "WtdAlphaPF", "Fltr_on_TopKLocsPF", "Fltr_on_ActualLocPF",
		// "Fltr_on_Top1Loc", "Fltr_on_Random2LocPF", "Fltr_on_Random10LocPF", "Fltr_on_Random20LocPF",
		// "Fltr_on_Random50LocPF", "Fltr_on_Random50LocPF", "Fltr_on_RandomLocPF" };
		// , "", "WtdAlphaPF", "Fltr_on_TopKLocsPF", "Fltr_on_ActualLocPF",
		// "Fltr_on_Top1Loc" };
		// "" , "Fltr_on_Top1Loc" "Fltr_on_ActualLocPF" };// ,
		// "Fltr_on_TopKLocsPF",
		// "WtdAlphaPF" };
		for (String s : pfFilterNames)
		{
			// main19July2018(s);
			main19Nov2018(s);
		}
	}

	public static void main(String args[])
	{
		// Constant.setDatabaseName("gowalla1");
		String resultsfileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/"
				+ "ResultsToReadAPR24Gowalla.csv";
		// + "ResultsToReadAPR24GowallaSecDim.csv";
		// + "/ResultsToReadApr7Geolife4.csv";
		// + "ResultsToReadMar22GeolifeSeq.csv";
		boolean recommSeq = false;
		boolean hasLevel1 = false;
		boolean recommSecDim = false;
		// + "/ResultsToReadMar7GowallaSecDim.csv";
		// + "Mar6Temp2.csv";
		// + "ResultsToReadTestGeoMar1.csv";
		// + "ResultsToReadGeolifeImproveEDFeb14.csv";
		// + "ResultsToReadJan15Gowalla1_2.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadDCUInevstigating7Feb.csv";
		// + "ResultsToReadJan15GeolifeSubset3_4.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadJan15DCUSubset3.csv";

		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadJan24ImprovED0.csv";
		// + "ResultsToReadJan15Gowalla1_5Investigating_2.csv";
		// + "ResultsToReadJan11Gowalla1_2.csv";
		// "ResultsToReadJan15GeolifeSubset3.csv";
		// + "ResultsToReadJan11Gowalla1.csv";
		// + "ResultsToReadJan3Geolife.csv";
		// + "ResultsToReadJan4FEDInvesitgation.csv";
		// + "ResultsToReadJan3Gowalla1.csv";
		// "Geolife.csv";
		// "ResultsToReadDec25Truncated6.csv";
		// "ResultsToReadDec21Truncated2.csv";
		// ResultsToReadDec8Truncated.csv";
		// "ResultsToReadNov28_OnlyED1.csv";// 25_3.csv";
		// dcu_data_2_JAN15H10M31ED1.0AllActsFDStFilter0hrsRTVPNN50NoTTFilterNC_AllReciprocalRank_MinMUWithMaxFirst0Aware.csv

		if (false)
		{
			runSomePostExperimentEvals(resultsfileToRead, "");
		}
		if (true)
		{
			//
			// getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerUser", 1);
			// getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerActual", 1);
			// getResultsNov21(resultsfileToRead, "AllMeanReciprocalRank_MinMUWithMaxFirst0Aware", 2);
			// getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0Aware", 4);
			// getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreements_ChosenMU", 2);
			// getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMU", 4);
			// getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMU", 6);
			// getResultsNov21(resultsfileToRead, "AllAvgPrecision_ChosenMU", 4);
			// getResultsNov21(resuchltsfileToRead, "AllAvgPrecision_ChosenMU", 6);
			// getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareListRRPerActual", 2);

			if (!recommSeq)
			{

				getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerActual", 1);
				getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerUser", 1);
				getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0Aware", 4);

				// getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareListRRPerActual", 1);
				// getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareListRRPerActual", 2);
				if (true)
				{
					getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareListRRPerActual", 1);
					getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareListRRPerActual", 2);
					getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerActual", 2);
					getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerUser", 2);

					getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMU", 6);
					getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMU", 5);
					getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMU", 4);
					getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMU", 3);
					getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMU", 2);

					getResultsNov21(resultsfileToRead, "AllAvgPrecision_ChosenMU", 6);
					getResultsNov21(resultsfileToRead, "AllAvgPrecision_ChosenMU", 5);
					getResultsNov21(resultsfileToRead, "AllAvgPrecision_ChosenMU", 4);
					getResultsNov21(resultsfileToRead, "AllAvgPrecision_ChosenMU", 3);
					getResultsNov21(resultsfileToRead, "AllAvgPrecision_ChosenMU", 2);
				}
			}

			getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreements_ChosenMU", 2);
			if (recommSeq)
			{
				getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreements_ChosenMU", 3);
				getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreements_ChosenMU", 4);

				if (hasLevel1)
				{
					getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreementsL1_ChosenMU", 2);
					getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreementsL1_ChosenMU", 3);
					getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreementsL1_ChosenMU", 4);
				}
			}

			if (recommSecDim)
			{
				getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerUserSecDim", 1);
				// #getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerActual", 1);
				getResultsNov21(resultsfileToRead, "AllMeanReciprocalRank_MinMUWithMaxFirst0AwareSecDim", 2);
				getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareSecDim", 4);

				getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreements_ChosenMUSecDim", 2);
				getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMUSecDim", 4);
				getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMUSecDim", 6);
				getResultsNov21(resultsfileToRead, "AllAvgPrecision_ChosenMUSecDim", 4);
				getResultsNov21(resultsfileToRead, "AllAvgPrecision_ChosenMUSecDim", 6);
			}

		}

		if (false)
		{
			getResultsNov21(resultsfileToRead, "AllMeanReciprocalRank_MinMUWithMaxFirst0Aware", 2);
			getResultsNov21(resultsfileToRead, "AllAvgRecall_ChosenMU", 4);
			getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0AwareMeanPerActual", 1);
		} //

		// getResultsNov21(resultsfileToRead, "AllReciprocalRank_MinMUWithMaxFirst0Aware", 4);
		if (false)
		{
			getResultsNov21(resultsfileToRead, "AllPerDirectTopKAgreements_MinMUWithMaxFirst0Aware", 2);

			getResultsNov21(resultsfileToRead, "AllAvgRecall_MinMUWithMaxFirst0Aware", 6);
			// $$ getResultsNov21(resultsfileToRead, "AllAvgPrecision_MinMUWithMaxFirst0Aware", 6);
			// $$ getResultsNov21(resultsfileToRead, "AllAvgRecall_MinMUWithMaxFirst0Aware", 5);
			getResultsNov21(resultsfileToRead, "AllAvgRecall_MinMUWithMaxFirst0Aware", 4);
		}
		// PopUps.showMessage("Finished AllReciprocalRank_MinMUWithMaxFirst0Aware");

		PopUps.showMessage("End of main");
		System.exit(0);
	}

	/**
	 * 
	 * @param resultsfileToRead
	 * @param resultLabel
	 *            AllMeanReciprocalRank_MinMUWithMaxFirst0Aware
	 * @param colIndexToRead
	 *            2
	 */
	public static void getResultsNov21(String resultsfileToRead, String resultLabel, int colIndexToRead)
	{
		// String resultsfileToRead =
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadNov25_3.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsNov21.csv";

		try
		{
			List<List<String>> allLines = ReadingFromFile.readLinesIntoListOfLists(resultsfileToRead, ",");
			Map<String, List<String>> valsFromAllExperiments = new LinkedHashMap<>();

			for (List<String> line : allLines)
			{
				String usersHeader = line.get(0);
				String host = line.get(1);
				String path = line.get(2);

				/// mnt/sshServers/claritytrec/SyncedWorkspace/Aug2Workspace/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_NOV20ED1.0STimeDurDistTrStartGeoEndGeoAvgAltAllActsFDStFilter0hrsRTVNoTTFilter/geolife1_NOV20ED1.0STimeDurDistTrStartGeoEndGeoAvgAltAllActsFDStFilter0hrsRTVNoTTFilter_AllMeanReciprocalRank_MinMUWithMaxFirst0Aware.csv
				String splitted[] = path.split("/");
				// String splitted2[] = splitted[splitted.length - 1].split("_");
				String expLabel = splitted[splitted.length - 1];
				System.out.println(expLabel);
				// PopUps.showMessage(expLabel);
				String fileToRead = path + expLabel + "_" + resultLabel + ".csv";
				// List<Double> valsRead = new ArrayList<>();
				List<String> valsRead = new ArrayList<>();

				if (host.contains("local"))
				{
					// valsRead = ReadingFromFile.oneColumnReaderDouble(fileToRead, ",", colIndexToRead, true);
					valsRead = ReadingFromFile.oneColumnReaderString(fileToRead, ",", colIndexToRead, true);
				}
				else
				{
					String passwd = ServerUtils.getPassWordForHost(host);
					String user = ServerUtils.getUserForHost(host);
					Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, 22,
							fileToRead, user, passwd);

					valsRead = ReadingFromFile.oneColumnReaderString(inputAndSession.getFirst(), ",", colIndexToRead,
							true);
				}
				valsFromAllExperiments.put(expLabel, valsRead);
			}

			StringBuilder sb = new StringBuilder();
			valsFromAllExperiments.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + ","
					+ e.getValue().stream().map(s -> String.valueOf(s)).collect(Collectors.joining(",")) + "\n"));

			WToFile.writeToNewFile(sb.toString(),
					"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsDistribution"
							+ DateTimeUtils.getMonthDateHourMinLabel() + resultLabel + colIndexToRead + ".csv");

			System.out.println("valsFromAllExperiments.size() = " + valsFromAllExperiments.size());
			// System.out.println("\nvalsFromAllExperiments = " + valsFromAllExperiments);
		}
		catch (Exception e)
		{
			System.out.println("resultsfileToRead = " + resultsfileToRead + " resultLabel = " + resultLabel);
			e.printStackTrace();
		}
		// PopUps.showMessage("End of getResultsNov21");
	}

	/**
	 * 
	 * @param resultsfileToRead
	 * @param dimensionPhrase
	 * @since 12 March 2019
	 */
	public static void runSomePostExperimentEvals(String resultsfileToRead, String dimensionPhrase)
	{
		boolean extractForBestMU = false;
		boolean extractForChosenMU = false;
		boolean writeMRRStatsOverUsersBestMUs = false;
		boolean writeRRForOptimalMUAllUsers = false;
		boolean writeMRRByUserAndActualForChosenMU = false;
		boolean writeRRByActualForChosenMU = true;

		try
		{
			List<List<String>> allLines = ReadingFromFile.readLinesIntoListOfLists(resultsfileToRead, ",");

			for (List<String> line : allLines)
			{
				String host = line.get(1);
				String path = line.get(2);
				String commonPath = path.substring(0, path.length());// ignore the last char which is '/'
				String splitted[] = path.split("/");
				String expLabel = splitted[splitted.length - 1];

				System.out.println("commonPath = " + commonPath + "\nexpLabel = " + expLabel);
				if (host.contains("local"))
				{

					SuperController.extractAggregateEvalResultsOverMUs(commonPath, dimensionPhrase, extractForBestMU,
							extractForChosenMU, writeMRRStatsOverUsersBestMUs, writeRRForOptimalMUAllUsers,
							writeMRRByUserAndActualForChosenMU, writeRRByActualForChosenMU);
				}
				else
				{
					PopUps.showError(
							"Error: runSomePostExperimentEvals() implemented for local (use sshfs to make local), while current path to read is: "
									+ commonPath + "\nhost = " + host);
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	///
	/**
	 * 
	 * 
	 * @param dimensionPhrase
	 */
	public static void main19Nov2018(String dimensionPhrase)
	{
		// String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		String resultsLabelsPathFileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsNov19.csv";
		// "ResultsAug9ToReadRandomPF.csv";
		// + "ResultsAug7ToRead_1.csv";
		/// ResultsJuly31ToRead_2.csv";
		// ResultsMay18ToRead_1Jun28T.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsMay10ToRead_1.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsApril30ToRead_2.csv";//
		// ResultsApril26ToRead_2.csv";
		String pathToRead = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/NOV19ResultsDistributionFirstToMax1/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUL27ResultsDistributionFirstToMax1/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUL20ResultsDistributionFirstToMax1/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN29ResultsDistributionFirstToMax3/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY10ResultsDistributionFirstToMax3/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY2ResultsDistributionFirstToMax3/FiveDays/";

		// $$String resultForUserGroupingMay2 = pathToRead
		// $$ +
		// "/Concatenated/ConcatenatedED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrsRTV_AllPerDirectTopKAgreements_MinMUWithMaxFirst0Aware.csv";
		// String resultForUserGroupingMay4 = // pathToRead
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY10ResultsDistributionFirstToMax3/FiveDays/"
		// +
		// "/Concatenated/ConcatenatedED1.0AllActsFDStFilter0hrsRTV_AllPerDirectTopKAgreements_MinMUWithMaxFirst0Aware.csv";
		int firstToMax = 1;
		if (true)
		{
			fetchResultsFromServersInFormat(resultsLabelsPathFileToRead, firstToMax, dimensionPhrase);
		}
		else
		{
			// // String fileToSort = ;String sortedFileToWrite = ;String uniqueConfigsFileToWrite = ;
			// // sortIgnoringDates(pathToRead + "GTE100UserLabels.csv", pathToRead + "GTE100UserLabelsSorted.csv");
			Set<String> uniqueConfigs = findUniqueConfigs(pathToRead + "LT916UserLabels.csv",
					pathToRead + "GTE100UserLabelsUniqueConfigs.csv");

			PopUps.showMessage("Finished findUniqueConfigs");

			// if (tempDisable20July2018)
			// {
			// concatenateFromDifferentSetsOfUsers(pathToRead, pathToRead + "Concatenated/",
			// new String[] { "userMUKeyVals" + dimensionPhrase + ".csv",
			// "MinMUWithMaxFirst" + firstToMax + dimensionPhrase + ".csv",
			// "MinMUWithMaxFirst0Aware" + dimensionPhrase + ".csv" },
			// uniqueConfigs, userSetlabels);
			//
			// PopUps.showMessage("Finished first concatenateFromDifferentSetsOfUsers");
			// splitUsersMUZeroNonZeroGroup(resultForUserGroupingMay4, pathToRead + "Concatenated/");
			// }
			// PopUps.showMessage("Finished splitUsersMUZeroNonZeroGroup");
			// Choose fixed MU for each user based on one given result: resultForUserGroupingMay4
			// Map<String, Integer> userIdentifierChosenMU =
			// getUserChosenBestMUBasedOnGiveFile(resultForUserGroupingMay4,
			// pathToRead + "Concatenated/").getFirst();
			// //
			// // String secondPathToRead = "";
			// if (true)
			// {
			// // fetch results from server again, writing files for fixed MU for each user "ChosenMU.csv"
			// secondPathToRead = fetchResultsFromServersInFormat19July2018(resultsLabelsPathFileToRead, false, false,
			// true, userIdentifierChosenMU, "", firstToMax, dimensionPhrase);
			// }
			// else
			// {
			// secondPathToRead =
			// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY4ResultsDistributionFirstToMax3/FiveDays/";
			// }
			// // concatenating ChosenMU.csv form different sets of users
			// concatenateFromDifferentSetsOfUsers(secondPathToRead, secondPathToRead + "Concatenated/",
			// new String[] { "ChosenMU" + dimensionPhrase + ".csv" }, uniqueConfigs, userSetlabels);
			//
			// PopUps.showMessage("Finished second concatenateFromDifferentSetsOfUsers");
		}

		// System.exit(0);

	}

	public static void main19July2018(String dimensionPhrase)
	{
		// String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		String resultsLabelsPathFileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsAug23.csv";
		// "ResultsAug9ToReadRandomPF.csv";
		// + "ResultsAug7ToRead_1.csv";
		/// ResultsJuly31ToRead_2.csv";
		// ResultsMay18ToRead_1Jun28T.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsMay10ToRead_1.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsApril30ToRead_2.csv";//
		// ResultsApril26ToRead_2.csv";
		String pathToRead = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUL27ResultsDistributionFirstToMax1/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUL20ResultsDistributionFirstToMax1/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN29ResultsDistributionFirstToMax3/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY10ResultsDistributionFirstToMax3/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY2ResultsDistributionFirstToMax3/FiveDays/";

		// $$String resultForUserGroupingMay2 = pathToRead
		// $$ +
		// "/Concatenated/ConcatenatedED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrsRTV_AllPerDirectTopKAgreements_MinMUWithMaxFirst0Aware.csv";
		String resultForUserGroupingMay4 = // pathToRead
				"/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY10ResultsDistributionFirstToMax3/FiveDays/"
						+ "/Concatenated/ConcatenatedED1.0AllActsFDStFilter0hrsRTV_AllPerDirectTopKAgreements_MinMUWithMaxFirst0Aware.csv";
		int firstToMax = 1;
		if (true)
		{
			fetchResultsFromServersInFormat(resultsLabelsPathFileToRead, firstToMax, dimensionPhrase);
		}
		else
		{
			// String fileToSort = ;String sortedFileToWrite = ;String uniqueConfigsFileToWrite = ;
			// sortIgnoringDates(pathToRead + "GTE100UserLabels.csv", pathToRead + "GTE100UserLabelsSorted.csv");
			Set<String> uniqueConfigs = findUniqueConfigs(pathToRead + "GTE100UserLabels.csv",
					pathToRead + "GTE100UserLabelsUniqueConfigs.csv");

			PopUps.showMessage("Finished findUniqueConfigs");

			if (tempDisable20July2018)
			{
				concatenateFromDifferentSetsOfUsers(pathToRead, pathToRead + "Concatenated/",
						new String[] { "userMUKeyVals" + dimensionPhrase + ".csv",
								"MinMUWithMaxFirst" + firstToMax + dimensionPhrase + ".csv",
								"MinMUWithMaxFirst0Aware" + dimensionPhrase + ".csv" },
						uniqueConfigs, userSetlabels);

				PopUps.showMessage("Finished first concatenateFromDifferentSetsOfUsers");
				splitUsersMUZeroNonZeroGroup(resultForUserGroupingMay4, pathToRead + "Concatenated/");
			}
			PopUps.showMessage("Finished splitUsersMUZeroNonZeroGroup");
			// Choose fixed MU for each user based on one given result: resultForUserGroupingMay4
			Map<String, Integer> userIdentifierChosenMU = getUserChosenBestMUBasedOnGiveFile(resultForUserGroupingMay4,
					pathToRead + "Concatenated/").getFirst();

			String secondPathToRead = "";
			if (true)
			{
				// fetch results from server again, writing files for fixed MU for each user "ChosenMU.csv"
				secondPathToRead = fetchResultsFromServersInFormat19July2018(resultsLabelsPathFileToRead, false, false,
						true, userIdentifierChosenMU, "", firstToMax, dimensionPhrase);
			}
			else
			{
				secondPathToRead = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY4ResultsDistributionFirstToMax3/FiveDays/";
			}
			// concatenating ChosenMU.csv form different sets of users
			concatenateFromDifferentSetsOfUsers(secondPathToRead, secondPathToRead + "Concatenated/",
					new String[] { "ChosenMU" + dimensionPhrase + ".csv" }, uniqueConfigs, userSetlabels);

			PopUps.showMessage("Finished second concatenateFromDifferentSetsOfUsers");
		}

		// System.exit(0);

	}

	/**
	 * For sequence recommendation
	 * <p>
	 * Used before 19 July 2018.
	 * 
	 */
	public static void mainBefore19July(String dimensionPhrase)
	{
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		String resultsLabelsPathFileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsMay18ToRead_1Jun28T.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsMay10ToRead_1.csv";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsApril30ToRead_2.csv";//
		// ResultsApril26ToRead_2.csv";
		String pathToRead = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUN29ResultsDistributionFirstToMax3/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY10ResultsDistributionFirstToMax3/FiveDays/";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY2ResultsDistributionFirstToMax3/FiveDays/";

		String resultForUserGroupingMay2 = pathToRead
				+ "/Concatenated/ConcatenatedED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrsRTV_AllPerDirectTopKAgreements_MinMUWithMaxFirst0Aware.csv";
		String resultForUserGroupingMay4 = // pathToRead
				"/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY10ResultsDistributionFirstToMax3/FiveDays/"
						+ "/Concatenated/ConcatenatedED1.0AllActsFDStFilter0hrsRTV_AllPerDirectTopKAgreements_MinMUWithMaxFirst0Aware.csv";
		int firstToMax = 3;
		if (false)
		{
			fetchResultsFromServersInFormat(resultsLabelsPathFileToRead, firstToMax, dimensionPhrase);
		}
		else
		{
			// String fileToSort = ;String sortedFileToWrite = ;String uniqueConfigsFileToWrite = ;
			// sortIgnoringDates(pathToRead + "GTE100UserLabels.csv", pathToRead + "GTE100UserLabelsSorted.csv");
			Set<String> uniqueConfigs = findUniqueConfigs(pathToRead + "GTE100UserLabels.csv",
					pathToRead + "GTE100UserLabelsUniqueConfigs.csv");

			PopUps.showMessage("Finished findUniqueConfigs");
			concatenateFromDifferentSetsOfUsers(pathToRead, pathToRead + "Concatenated/",
					new String[] { "userMUKeyVals.csv", "MinMUWithMaxFirst3.csv", "MinMUWithMaxFirst0Aware.csv" },
					uniqueConfigs, userSetlabels);

			PopUps.showMessage("Finished first concatenateFromDifferentSetsOfUsers");
			splitUsersMUZeroNonZeroGroup(resultForUserGroupingMay4, pathToRead + "Concatenated/");

			PopUps.showMessage("Finished splitUsersMUZeroNonZeroGroup");
			// Choose fixed MU for each user based on one given result: resultForUserGroupingMay4
			Map<String, Integer> userIdentifierChosenMU = getUserChosenBestMUBasedOnGiveFile(resultForUserGroupingMay4,
					pathToRead + "Concatenated/").getFirst();

			String secondPathToRead = "";
			if (true)
			{
				// fetch results from server again, writing files for fixed MU for each user "ChosenMU.csv"
				secondPathToRead = fetchResultsFromServersInFormat(resultsLabelsPathFileToRead, false, false, true,
						userIdentifierChosenMU, "");
			}
			else
			{
				secondPathToRead = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY4ResultsDistributionFirstToMax3/FiveDays/";
			}
			// concatenating ChosenMU.csv form different sets of users
			concatenateFromDifferentSetsOfUsers(secondPathToRead, secondPathToRead + "Concatenated/",
					new String[] { "ChosenMU.csv" }, uniqueConfigs, userSetlabels);

			PopUps.showMessage("Finished second concatenateFromDifferentSetsOfUsers");
		}

		System.exit(0);

	}

	/**
	 * 
	 * @param fileToRead
	 * @param pathToWrite
	 * @return userIdentifierChosenMU
	 */
	protected static Pair<Map<String, Integer>, Map<Integer, Integer>> getUserChosenBestMUBasedOnGiveFile(
			String fileToRead, String pathToWrite)
	{
		Map<String, Integer> userIdentifierChosenMU = new LinkedHashMap<>();
		Map<Integer, Integer> userIndexChosenMU = new LinkedHashMap<>();

		List<List<String>> dataRead = ReadingFromFile.readLinesIntoListOfLists(fileToRead, ",");
		System.out.println("Removing header:" + dataRead.remove(0));

		int userIndex = -1;
		for (List<String> e : dataRead)
		{
			userIdentifierChosenMU.put(e.get(0), Integer.valueOf(e.get(1)));
			userIndexChosenMU.put(++userIndex, Integer.valueOf(e.get(1)));
		}

		// Start of writing
		StringBuilder sb = new StringBuilder("userIdentifier,ChosenMU\n");
		StringBuilder sb2 = new StringBuilder("userIndex,ChosenMU\n");
		userIdentifierChosenMU.entrySet().stream()
				.forEachOrdered(e -> sb.append(e.getKey() + "," + e.getValue() + "\n"));
		userIndexChosenMU.entrySet().stream().forEachOrdered(e -> sb2.append(e.getKey() + "," + e.getValue() + "\n"));
		WToFile.writeToNewFile(sb.toString(), pathToWrite + "userIdentifierChosenMU.csv");
		WToFile.writeToNewFile(sb2.toString(), pathToWrite + "userIndexChosenMU.csv");
		// End of writing

		return new Pair<>(userIdentifierChosenMU, userIndexChosenMU);
		// new Pair<LinkedHashMap<Integer, Double>, LinkedHashMap<String,
		// Double>>(userIndexChosenMU,userIdentifierChosenMU);
	}

	/**
	 * To concatenateResultsFromAllSets
	 * 
	 * @param commonPath
	 * @param commonPathToWrite
	 * @param fileToConcatenateLabelTails
	 */
	public static void concatenateFromDifferentSetsOfUsers(String commonPath, String commonPathToWrite,
			String[] fileToConcatenateLabelTails, Set<String> uniqueConfigs, String userSetlabels[])
	{

		// String commonPath =
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY2ResultsDistributionFirstToMax3/FiveDays/";
		// String commonPathToWrite = commonPath + "Concatenated/";
		WToFile.createDirectoryIfNotExists(commonPathToWrite);
		concatenateResultsFromAllSets(uniqueConfigs, commonPath, commonPathToWrite, fileToConcatenateLabelTails,
				userSetlabels);
		// findGroupsOfUsersBasedOnBestMU(uniqueConfigs, commonPath);

	}

	/**
	 * 
	 * @param splitOnBasisOfThisFile
	 * @param pathToWrite
	 */
	public static void splitUsersMUZeroNonZeroGroup(String splitOnBasisOfThisFile, String pathToWrite)
	{
		System.out.println(
				"Inside splitUsersMUZeroNonZeroGroup: \nCreating user groups based on file: " + splitOnBasisOfThisFile);
		List<List<String>> readData = ReadingFromFile.readLinesIntoListOfLists(splitOnBasisOfThisFile, ",");
		System.out.println("removing header: " + readData.remove(0));// remove header
		int indexOfMinMuWithMaxFirst3 = 1;

		Map<String, ArrayList<Pair<String, Integer>>> groupsOfUsers = new LinkedHashMap<>();
		groupsOfUsers.put("MinMUWithMaxFirst3_Zero", new ArrayList<>());
		groupsOfUsers.put("MinMUWithMaxFirst3_GTZero", new ArrayList<>());

		int rowIndex = -1;
		for (List<String> d : readData)
		{
			try
			{
				rowIndex += 1;
				String keyLabel = "";
				if (Integer.valueOf(d.get(indexOfMinMuWithMaxFirst3)) == 0)
				{
					keyLabel = "MinMUWithMaxFirst3_Zero";
				}
				else if (Integer.valueOf(d.get(indexOfMinMuWithMaxFirst3)) > 0)
				{
					keyLabel = "MinMUWithMaxFirst3_GTZero";
				}
				else
				{
					PopUps.showError("Error: d.get(indexOfMinMuWithMaxFirst3) = " + d.get(indexOfMinMuWithMaxFirst3));
				}
				groupsOfUsers.get(keyLabel).add(new Pair<String, Integer>(d.get(0), rowIndex));
			}
			catch (Exception e)
			{

				e.printStackTrace();
				PopUps.showError("Exception: for rowRead=" + d.toString());

			}
		}

		System.out.println("MinMUWithMaxFirst3_Zero.size = " + groupsOfUsers.get("MinMUWithMaxFirst3_Zero").size());
		System.out.println("MinMUWithMaxFirst3_GTZero.size = " + groupsOfUsers.get("MinMUWithMaxFirst3_GTZero").size());

		WToFile.writeToNewFile(groupsOfUsers.get("MinMUWithMaxFirst3_Zero").stream()
				.map(e -> e.getFirst() + "," + e.getSecond()).collect(Collectors.joining("\n")),
				pathToWrite + "MinMUWithMaxFirst3_Zero.csv");
		WToFile.writeToNewFile(groupsOfUsers.get("MinMUWithMaxFirst3_GTZero").stream()
				.map(e -> e.getFirst() + "," + e.getSecond()).collect(Collectors.joining("\n")),
				pathToWrite + "MinMUWithMaxFirst3_GTZero.csv");
	}

	/**
	 * INCOMPLETE
	 * 
	 * @param uniqueConfigs
	 * @param commonPath
	 */
	private static void findGroupsOfUsersBasedOnBestMU(Set<String> uniqueConfigs, String commonPath)
	{
		// List<List<Integer>> groupsOfUsers = new ArrayList<>();
		LinkedHashMap<String, Map<String, List<Integer>>> groupsOfUsers = new LinkedHashMap<>();

		String[] resultLabelTails = { "_AllPerDirectTopKAgreements_.csv" };
		for (String config : uniqueConfigs)
		{
			String concatenatedFileToRead = commonPath + config + resultLabelTails;
			List<List<Double>> res = ReadingFromFile.nColumnReaderDouble(concatenatedFileToRead, ",", true);

			List<Integer> usersWithMUE0 = new ArrayList<>();
			List<Integer> usersWithMUGT0 = new ArrayList<>();

			int userIndex = -1;
			for (List<Double> r : res)
			{
				userIndex++;
				if (r.get(1) == 0)
				{
					usersWithMUE0.add(userIndex);
				}
				else if (r.get(1) > 0)
				{
					usersWithMUGT0.add(userIndex);
				}
				else
				{
					PopUps.showError("Error: unexpected best MU");
				}
			}

			Map<String, List<Integer>> mapOfUserGroups = new LinkedHashMap<>(2);
			mapOfUserGroups.put("usersWithMUE0", usersWithMUE0);
			mapOfUserGroups.put("usersWithMUGT0", usersWithMUGT0);

			// groupsOfUsers.put(config+resultLabelTails, value)
		}

	}

	/**
	 * 
	 * @param fileWithContentsToSort
	 * @param sortedFileToWrite
	 */
	private static void sortIgnoringDates(String fileWithContentsToSort, String sortedFileToWrite)
	{
		List<String> res = ReadingFromFile.oneColumnReaderString(fileWithContentsToSort, ",", 0, false);

		TreeMap<String, String> resSorted = new TreeMap<>(Collections.reverseOrder());
		// since all labels have first 5 characters as date such as APR29
		for (String r : res)
		{
			resSorted.put(r.substring(5), r);
		}

		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> e : resSorted.entrySet())
		{
			System.out.println(e.getKey() + " - " + e.getValue());
			sb.append(e.getValue() + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), sortedFileToWrite);

	}

	/**
	 * 
	 * @param fileWithContentsToSort
	 * @param sortedFileToWrite
	 * @return
	 */
	private static Set<String> findUniqueConfigs(String fileWithContentsToSort, String sortedFileToWrite)
	{
		List<String> res = ReadingFromFile.oneColumnReaderString(fileWithContentsToSort, ",", 0, false);
		String sets[] = { "B", "C", "D", "E" };
		Set<String> uniqueConfigs = new TreeSet<>();
		// since all labels have first 5 characters as date such as APR29
		for (String r : res)
		{
			r = r.replace("LikeRecSys", "");// remove "LikeRecSys" comment in label

			if (r.contains("APR"))
			{
				r = r.substring(5);
			}
			if (r.contains("JUN"))
			{
				r = r.substring(5);
			}
			else if (r.contains("MAY"))
			{
				r = r.substring(4);
			}
			for (String set : sets)
			{
				r = r.replace("Set" + set, "");
			}
			uniqueConfigs.add(r);
		}

		StringBuilder sb = new StringBuilder();
		for (String e : uniqueConfigs)
		{
			System.out.println(e);
			sb.append(e + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), sortedFileToWrite);
		return uniqueConfigs;
	}

	/**
	 * 
	 * @param uniqueConfigs
	 * @param commonPathToRead
	 * @param commonPathToWrite
	 */
	private static void concatenateResultsFromAllSets(Set<String> uniqueConfigs, String commonPathToRead,
			String commonPathToWrite, String[] resultLabelTails2, String userSetlabels[])
	{
		String[] resultLabelTails1 = { "_AllPerDirectTopKAgreementsL1_", "_AllPerDirectTopKAgreements_" };
		// String statFileNamesPRF[] = { "_AvgPrecision_", "AvgRecall_", "AvgFMeasure_" };
		// String statFileNamesMRR[] = { "_AllMeanReciprocalRank_" };
		// String[] resultLabelTails2 = { "userMUKeyVals.csv", "MinMUWithMaxFirst3.csv", "MinMUWithMaxFirst0Aware.csv"
		// };

		String sets[] = userSetlabels;// { "" };// , "B", "C", "D", "E" };

		// get list of files in the current directory
		try
		{
			List<String> pathToAllFiles = Files.list(Paths.get(commonPathToRead)).filter(Files::isRegularFile)
					.map(p -> p.toString()).collect(Collectors.toList());

			for (String config : uniqueConfigs)
			{
				for (String labelTail1 : resultLabelTails1)
				{
					for (String labelTail2 : resultLabelTails2)
					{
						ArrayList<String> filesToConcatenateInOrder = new ArrayList<>();

						for (String set : sets)
						{
							String setLabel = set.length() > 0 ? "Set" + set : "";
							String specialTail = (set.length() == 0 && config.contains("ED-1")) ? "LikeRecSys" : "";

							String resultsLabel = setLabel + config + specialTail + labelTail1 + labelTail2;
							List<String> filenamesContainingResultsLabel = null;
							if (set.length() > 0)
							{
								filenamesContainingResultsLabel = pathToAllFiles.stream()
										.filter(p -> p.contains(resultsLabel)).collect(Collectors.toList());
							}
							else
							{
								filenamesContainingResultsLabel = pathToAllFiles.stream()
										.filter(p -> p.contains(resultsLabel)).filter(p -> p.contains("Set") == false)
										.collect(Collectors.toList());
							}

							System.out.println("resultsLabel= " + resultsLabel + "  num of matching file = "
									+ filenamesContainingResultsLabel.size() + " \n\t"
									+ String.join("\n\t", filenamesContainingResultsLabel));

							if (filenamesContainingResultsLabel.size() != 1)
							{
								PopUps.showError("Error filenamesContainingResultsLabel.size()="
										+ filenamesContainingResultsLabel.size() + "resultsLabel = " + resultsLabel);
							}
							else
							{
								filesToConcatenateInOrder.add(filenamesContainingResultsLabel.get(0));
							}
						} // end of loop over set
						System.out.println("-----------------\nfilesToConcatenateInOrder= \n\t"
								+ String.join("\n\t", filesToConcatenateInOrder) + "\n");
						CSVUtils.concatenateCSVFilesV2(filesToConcatenateInOrder, true,
								commonPathToWrite + "Concatenated" + config + labelTail1 + labelTail2, ',');
					}
				} // end of loop of resultsLabelTail
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param resultsLabelsPathFileToRead
	 * @param firstToMax
	 * @param dimensionPhrase
	 * @param statFileNames
	 */
	public static void fetchResultsFromServersInFormat(String resultsLabelsPathFileToRead, int firstToMax,
			String dimensionPhrase)
	{
		// runOneDay();
		// runFiveDays();
		// $runFeb2OneDayResults();//disabled on Feb 12 2018
		// $runFeb2FiveDaysResults(); //disabled on Feb 12 2018
		// $runFeb5FiveDaysResults();//disabled on Feb 12 2018
		// runFeb11FiveDaysResults();//disabled on feb 18 2018
		// runFeb17FiveDaysResults();
		// $$runMar9FiveDaysResults();
		//
		runApril10Results(resultsLabelsPathFileToRead, firstToMax, dimensionPhrase);
		SFTPFile.closeAllChannels();
	}

	public static String fetchResultsFromServersInFormat19July2018(String resultsLabelsPathFileToRead,
			boolean doFirstToMax, boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String pathToWrite, int firstToMax, String dimensionPhrase)
	{
		String pathWritten = runMayResults19July2018(resultsLabelsPathFileToRead, firstToMax, doFirstToMax,
				doFirstToMaxZeroAware, doChosenMUForEachUser, userIdentifierChosenMuMap, pathToWrite, dimensionPhrase);
		SFTPFile.closeAllChannels();
		return pathWritten;
	}

	/**
	 * Fork of org.activity.evaluation.ResultsDistributionEvaluation.fetchResultsFromServersInFormat(String)
	 * <p>
	 * 
	 * @param resultsLabelsPathFileToRead
	 * @param doFirstToMax
	 * @param doFirstToMaxZeroAware
	 * @param doChosenMUForEachUser
	 * @param userIdentifierChosenMuMap
	 * @param pathToWrite
	 * @since May 4 2018
	 */
	public static String fetchResultsFromServersInFormat(String resultsLabelsPathFileToRead, boolean doFirstToMax,
			boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String pathToWrite)
	{
		String pathWritten = runMayResults(resultsLabelsPathFileToRead, 3, doFirstToMax, doFirstToMaxZeroAware,
				doChosenMUForEachUser, userIdentifierChosenMuMap, pathToWrite);
		SFTPFile.closeAllChannels();
		return pathWritten;
	}

	/**
	 * 
	 * @param resultsLabelsPathFileToRead
	 * @param firstToMax
	 * @param doFirstToMax
	 * @param doFirstToMaxZeroAware
	 * @param doChosenMUForEachUser
	 * @param userIdentifierChosenMuMap
	 * @return
	 * @since May 4 2018
	 */
	public static String runMayResults(String resultsLabelsPathFileToRead, int firstToMax, boolean doFirstToMax,
			boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String pathToWrite)
	{
		if (pathToWrite.trim().length() == 0)
		{
			pathToWrite = "./dataWritten/" + LocalDateTime.now().getMonth().toString().substring(0, 3)
					+ LocalDateTime.now().getDayOfMonth() + "ResultsDistributionFirstToMax" + firstToMax + "/FiveDays/";// "./dataWritten/Mar9/FiveDays/";
		}
		WToFile.createDirectoryIfNotExists(pathToWrite);
		WToFile.createDirectoryIfNotExists(pathToWrite + "ReadMe/");

		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCountFixed;
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFileToRead), StandardOpenOption.READ), ",", false);

			for (List<String> resEntry : resLabels)
			{
				if (resEntry.size() == 3)
				{
					pathToRead = resEntry.get(2).trim();
					String splitted[] = pathToRead.split("/");
					resultsLabel = splitted[splitted.length - 1];

					host = getHostFromString(resEntry.get(1)).trim();

					WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + resultsLabel + "UserLabels.csv");

					System.out.println(
							"pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\nhost:" + host + "\n");

					int resSize = getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray,
							statFileNames, host, firstToMax, doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser,
							userIdentifierChosenMuMap, "", firstToMaxInOrder);

					if (resSize < 0)
					{
						continue;
					}
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return pathToWrite;
	}

	/**
	 * 
	 * @param resultsLabelsPathFileToRead
	 * @param firstToMax
	 * @param doFirstToMax
	 * @param doFirstToMaxZeroAware
	 * @param doChosenMUForEachUser
	 * @param userIdentifierChosenMuMap
	 * @param dimensionPhrase
	 * 
	 * @return
	 * @since May 4 2018
	 */
	public static String runMayResults19July2018(String resultsLabelsPathFileToRead, int firstToMax,
			boolean doFirstToMax, boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String pathToWrite, String dimensionPhrase)
	{
		if (pathToWrite.trim().length() == 0)
		{
			pathToWrite = "./dataWritten/" + LocalDateTime.now().getMonth().toString().substring(0, 3)
					+ LocalDateTime.now().getDayOfMonth() + "ResultsDistributionFirstToMax" + firstToMax + "/FiveDays/";// "./dataWritten/Mar9/FiveDays/";
		}
		WToFile.createDirectoryIfNotExists(pathToWrite);
		WToFile.createDirectoryIfNotExists(pathToWrite + "ReadMe/");

		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		String statFileNamesPRF[] = { "AllAvgPrecision_", "AllAvgRecall_", "AllAvgFMeasure_" };
		String statFileNamesMRR[] = { "AllMeanReciprocalRank_" };

		double muArray[] = Constant.matchingUnitAsPastCountFixed;
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFileToRead), StandardOpenOption.READ), ",", false);

			for (List<String> resEntry : resLabels)
			{
				if (resEntry.size() == 3)
				{
					pathToRead = resEntry.get(2).trim();
					String splitted[] = pathToRead.split("/");
					resultsLabel = splitted[splitted.length - 1];

					host = getHostFromString(resEntry.get(1)).trim();

					WToFile.appendLineToFileAbs(resultsLabel + "\n",
							pathToWrite + resultsLabel + "UserLabels" + dimensionPhrase + ".csv");

					System.out.println(
							"pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\nhost:" + host + "\n");

					int resSize = -1;
					if (tempDisable20July2018)
					{
						getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host,
								firstToMax, doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser,
								userIdentifierChosenMuMap, dimensionPhrase, firstToMaxInOrder);
					}
					int resSize2 = getResultsForEachStatFile_PrecisionRecallFMeasure2(pathToWrite, resultsLabel,
							pathToRead, muArray, statFileNamesPRF, host, 5, doFirstToMax, doFirstToMaxZeroAware,
							doChosenMUForEachUser, userIdentifierChosenMuMap, dimensionPhrase);

					int resSize3 = getResultsForEachStatFile_MRR2(pathToWrite, resultsLabel, pathToRead, muArray,
							statFileNamesMRR, host, 1, doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser,
							userIdentifierChosenMuMap, dimensionPhrase);

					if (tempDisable20July2018)
					{
						Sanity.eq(resSize, resSize2, "Error resSize != resSize2");
						Sanity.eq(resSize, resSize3, "Error resSize != resSize3");
					}
					if (resSize2 < 0)
					{
						continue;
					}
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return pathToWrite;
	}

	/**
	 * 
	 * @param resultsLabelsPathFileToRead
	 * @param firstToMax
	 * @param dimensionPhrase
	 * @param statFileNames
	 *            e..g, { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
	 * @since Feb 2 2018
	 * @updated 19 July 2018
	 */
	public static void runApril10Results(String resultsLabelsPathFileToRead, int firstToMax, String dimensionPhrase)
	{

		String pathToWrite = "./dataWritten/" + LocalDateTime.now().getMonth().toString().substring(0, 3)
				+ LocalDateTime.now().getDayOfMonth() + "ResultsDistributionFirstToMax" + firstToMax + "/FiveDays/";// "./dataWritten/Mar9/FiveDays/";
		WToFile.createDirectoryIfNotExists(pathToWrite);
		WToFile.createDirectoryIfNotExists(pathToWrite + "ReadMe/");

		// String resultsLabelsPathFile =
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsApril10ToRead.csv";
		// String resultsLabelsPathFile =
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsApril30ToRead_2.csv";//
		// ResultsApril26ToRead_2.csv";
		// String statFileNames[];
		List<String> statFileNamesList = new ArrayList<>();
		statFileNamesList.add("AllPerDirectTopKAgreements_");
		if (dimensionPhrase.equals(""))// level1 only comes into play for activity dimension
		{
			statFileNamesList.add("AllPerDirectTopKAgreementsL1_");
		}
		String statFileNames[] = statFileNamesList.toArray(new String[0]);
		String statFileNamesPRF[] = { "AllAvgPrecision_", "AllAvgRecall_", "AllAvgFMeasure_" };
		String statFileNamesMRR[] = { "AllMeanReciprocalRank_" };

		double muArray[] = Constant.matchingUnitAsPastCountFixed;// { 0 }
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFileToRead), StandardOpenOption.READ), ",", false);

			for (List<String> resEntry : resLabels)
			{
				if (resEntry.size() >= 3)
				{
					pathToRead = resEntry.get(2).trim();
					String splitted[] = pathToRead.split("/");
					resultsLabel = splitted[splitted.length - 1];

					host = getHostFromString(resEntry.get(1)).trim();

					WToFile.appendLineToFileAbs(resultsLabel + "\n",
							pathToWrite + resultsLabel + "UserLabels" + dimensionPhrase + ".csv");

					System.out.println(
							"pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\nhost:" + host + "\n");
					int resSize = -1;
					if (dimensionPhrase.equals(""))
					{
						resSize = getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray,
								statFileNames, host, firstToMax, dimensionPhrase, firstToMaxInOrder);
					}

					int resSize2 = getResultsForEachStatFile_PrecisionRecallFMeasure(pathToWrite, resultsLabel,
							pathToRead, muArray, statFileNamesPRF, host, 5, dimensionPhrase);

					int resSize3 = getResultsForEachStatFile_MRR(pathToWrite, resultsLabel, pathToRead, muArray,
							statFileNamesMRR, host, 1, dimensionPhrase);

					if (!tempDisable20July2018)
					{
						Sanity.eq(resSize, resSize2, "Error resSize != resSize2");
						Sanity.eq(resSize, resSize3, "Error resSize != resSize3");
					}

					if (resSize2 < 0)
					{
						continue;
					}
				}
				else
				{
					PopUps.showError("Error: resEntry.size() = " + resEntry.size() + " <3");
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		// pathToRead =
		// "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		// getResults2(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host, firstToMax);

	}

	/**
	 * For of org.activity.evaluation.ResultsDistributionEvaluation.runApril10Results(String, int, String) to be run
	 * during experiments. For stat files:- AllPerDirectTopKAgreements_, AllPerDirectTopKAgreementsL1_,
	 * AllAvgPrecision_, AllAvgRecall_, AllAvgFMeasure_, AllMeanReciprocalRank_, AllPerActivityMeanReciprocalRank_
	 * 
	 * @param pathToRead
	 * @param firstToMax
	 * @param dimensionPhrase
	 * @param chosenFixedMUForEachUserFile
	 * @param doFirstToMax
	 * @param doFirstToMaxZeroAware
	 * @param doChosenMUForEachUser
	 * @since Nov 20 2018
	 */
	public static void runNov20Results(String pathToRead, int firstToMax, String dimensionPhrase,
			String chosenFixedMUForEachUserFile, boolean doFirstToMax, boolean doFirstToMaxZeroAware,
			boolean doChosenMUForEachUser)
	{
		// PopUps.showMessage("Inside runNov20Results");
		// added on 1 Jan 2018
		PrintStream debugLog = WToFile.redirectConsoleOutput(pathToRead + "runNov20ResultsgLog.txt");

		// doChosenMUForEachUser = chosenFixedMUForEachUserFile.trim().length() > 0 ? true : false;
		// boolean doFirstToMax = true;
		// boolean doFirstToMaxZeroAware = true;

		// DescriptiveStatistics mrrStatsOverUsers = null;
		String pathToWrite = pathToRead;
		boolean doLevel1 = Constant.getDatabaseName().contains("gowalla1") ? true : false;// false;
		String host = "local";
		WToFile.createDirectoryIfNotExists(pathToWrite);
		WToFile.createDirectoryIfNotExists(pathToWrite + "ReadMe/");

		List<String> statFileNamesList = new ArrayList<>();
		statFileNamesList.add("AllPerDirectTopKAgreements_");

		if (doLevel1 && dimensionPhrase.equals(""))// level1 only comes into play for activity dimension
		{
			statFileNamesList.add("AllPerDirectTopKAgreementsL1_");
		}

		String statFileNames[] = statFileNamesList.toArray(new String[0]);
		String statFileNamesPRF[] = { "AllAvgPrecision_", "AllAvgRecall_", "AllAvgFMeasure_" };
		String statFileNamesMRR[] = { "AllMeanReciprocalRank_" };
		// added 17 Jan 2019
		// $$temproarliy disabled on 8 May 2019
		String statFileNamesPerUserPerActMRR[] = { "AllPerActivityMeanReciprocalRank_" };// , "AllNumOfRTsPerAct_"

		double muArray[] = Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor);// Constant.matchingUnitAsPastCountFixed;//
																											// { 0 }
		// PopUps.showMessage("muArray in runNov20 =" + Arrays.toString(muArray));
		// String pathToRead = "", resultsLabel = "", host = "";

		//// for fixed chosen MU
		// Choose fixed MU for each user based on one given result: resultForUserGroupingMay4
		Map<String, Integer> userIdentifierChosenMuMap = null;
		if (doChosenMUForEachUser)
		{
			userIdentifierChosenMuMap = getUserChosenBestMUBasedOnGiveFile(chosenFixedMUForEachUserFile, pathToWrite)
					.getFirst();
		}

		try
		{
			// List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
			// Files.newInputStream(Paths.get(resultsLabelsPathFileToRead), StandardOpenOption.READ), ",", false);

			// for (List<String> resEntry : resLabels)
			// {
			// if (resEntry.size() >= 3)
			// {
			// pathToRead = resEntry.get(2).trim();
			String splitted[] = pathToRead.split("/");
			String resultsLabel = splitted[splitted.length - 1];
			// host = getHostFromString(resEntry.get(1)).trim();

			WToFile.appendLineToFileAbs(resultsLabel + "\n",
					pathToWrite + resultsLabel + "UserLabels" + dimensionPhrase + ".csv");
			System.out.println("pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\n");

			int resSize = -1;
			if (dimensionPhrase.equals(""))
			{
				resSize = // getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames,
							// host,firstToMax, dimensionPhrase, firstToMaxInOrder);
						getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host,
								firstToMax, doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser,
								userIdentifierChosenMuMap, dimensionPhrase, firstToMaxInOrder);
			}

			// int resSize2 = getResultsForEachStatFile_PrecisionRecallFMeasure(pathToWrite, resultsLabel, pathToRead,
			// muArray, statFileNamesPRF, host, 5, dimensionPhrase);
			int resSize2 = getResultsForEachStatFile_PrecisionRecallFMeasure2(pathToWrite, resultsLabel, pathToRead,
					muArray, statFileNamesPRF, host, 5, doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser,
					userIdentifierChosenMuMap, dimensionPhrase);

			// int resSize3 = getResultsForEachStatFile_MRR(pathToWrite, resultsLabel, pathToRead, muArray,
			// statFileNamesMRR, host, 1, dimensionPhrase);
			int resSize3 = getResultsForEachStatFile_MRR2(pathToWrite, resultsLabel, pathToRead, muArray,
					statFileNamesMRR, host, 1, doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser,
					userIdentifierChosenMuMap, dimensionPhrase);

			if (false)// temproarily disabled on May 8 2019
			{
				int resSize4 = getResultsForEachStatFile_generic(pathToWrite, resultsLabel, pathToRead, muArray,
						statFileNamesPerUserPerActMRR, host, -9999, false, false, doChosenMUForEachUser,
						userIdentifierChosenMuMap, new int[] {}, true, true, dimensionPhrase);

				// firstToMax as empty, only for chosen MU

				if (!tempDisable20July2018)
				{
					Sanity.eq(resSize, resSize2, "Error resSize != resSize2");
					Sanity.eq(resSize, resSize3, "Error resSize != resSize3");
					Sanity.eq(resSize, resSize4, "Error resSize != resSize4");
				}
			}
			// if (resSize2 < 0)
			// {
			// continue;
			// }
			// }
			// else
			// {
			// PopUps.showError("Error: resEntry.size() = " + resEntry.size() + " <3");
			// }
			// }
			debugLog.close();// added on 1 Jan 2018
		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}

		// return mrrStatsOverUsers;
		// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		// pathToRead =
		// "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		// getResults2(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host, firstToMax);

	}
	//

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runMar9FiveDaysResults()
	{

		String pathToWrite = "./dataWritten/Mar26ResultsDistribution/FiveDays/";// "./dataWritten/Mar9/FiveDays/";
		String resultsLabelsPathFile = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultMar26ToReadFormatted.csv";// ResultMar9ToReadFormatted.csv";
		int numOfDay = 5;
		int firstToMax = 3;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFile), StandardOpenOption.READ), ",", false);

			resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
			pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
			// resLabels.add(Arrays.asList(new String[] { "mortar", resultsLabel, pathToRead }));

			// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-600";
			// pathToRead =
			// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Feb2NCount_5Day_ThresholdNN600/";
			// resLabels.add(Arrays.asList(new String[] { "howitzer", resultsLabel, pathToRead }));

			for (List<String> resEntry : resLabels)
			{
				if (resEntry.size() > 1)
				{
					pathToRead = resEntry.get(2).trim();
					String splitted[] = pathToRead.split("/");
					resultsLabel = splitted[splitted.length - 1];
					host = getHostFromString(resEntry.get(1)).trim();

					if (resEntry.get(0).equals("100R"))
					{
						WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "100RUserLabels.csv");
					}
					if (resEntry.get(0).equals("916"))
					{
						WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "916UserLabels.csv");
					}

					System.out.println(
							"pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\nhost:" + host + "\n");
					int resSize = getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray,
							statFileNames, host, firstToMax, "", firstToMaxInOrder);
					if (resSize < 0)
					{
						continue;
					}
				}
			}

		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}
		// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		// pathToRead =
		// "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		// getResults2(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host, firstToMax);

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb17FiveDaysResults()
	{

		String pathToWrite = "./dataWritten/Mar5/FiveDays/";
		String resultsLabelsPathFile = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultMar5ToReadFormatted2.csv";
		int numOfDay = 5;
		int firstToMax = 3;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFile), StandardOpenOption.READ), ",", false);

			resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
			pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
			// resLabels.add(Arrays.asList(new String[] { "mortar", resultsLabel, pathToRead }));

			// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-600";
			// pathToRead =
			// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Feb2NCount_5Day_ThresholdNN600/";
			// resLabels.add(Arrays.asList(new String[] { "howitzer", resultsLabel, pathToRead }));

			for (List<String> resEntry : resLabels)
			{
				if (resEntry.size() > 1)
				{
					pathToRead = resEntry.get(2).trim();
					resultsLabel = resEntry.get(1).trim();
					host = getHostFromString(resEntry.get(0)).trim();

					System.out.println(
							"pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\nhost:" + host + "\n");
					int resSize = getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray,
							statFileNames, host, firstToMax, "", firstToMaxInOrder);
					if (resSize < 0)
					{
						continue;
					}
				}
			}

		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}
		// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		// pathToRead =
		// "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		// getResults2(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host, firstToMax);

	}

	/**
	 * Call getResult() for each stat file and accumulates results in a map
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNames
	 * @param host
	 * @param firstToMax
	 * @return
	 * @throws Exception
	 */
	public static int getResultsForEachStatFile(String pathToWrite, String resultsLabel, String pathToRead,
			double[] muArray, String[] statFileNames, String host, int firstToMax, String dimensionPhrase,
			int[] firstToMaxInOrder)
	{
		Map<Integer, Map<Integer, List<Double>>> res = null;

		for (String statFileName : statFileNames)
		{
			res = getResult19July2018(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, firstToMax,
					dimensionPhrase, false, firstToMaxInOrder);
		}

		if (res == null)
		{
			return -1;
		}

		if (res.size() >= 100)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		}
		if (res.size() == 916)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		}

		if (res.size() < 916)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		}
		return res.size();
	}

	/**
	 * Fork of getResultsForEachStatFile Call getResult() for each stat file and accumulates results in a map
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNames
	 * @param host
	 * @param firstToMax
	 *            5 -> Max top1, i.e. last col
	 * @param dimensionPhrase
	 * @param firstToMaxInOrder
	 * @return
	 * @since 19 July 2018
	 */
	public static int getResultsForEachStatFile_PrecisionRecallFMeasure(String pathToWrite, String resultsLabel,
			String pathToRead, double[] muArray, String[] statFileNames, String host, int firstToMax,
			String dimensionPhrase)
	{
		Map<Integer, Map<Integer, List<Double>>> res = null;

		for (String statFileName : statFileNames)
		{
			res = getResult19July2018_MRR_PRF(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host,
					firstToMax/* 5= Max top1, i.e. last col */, dimensionPhrase, true, new int[] { 5, 4, 3, 2, 1 },
					true, "top5,top4,top3,top2,top1");
		}
		if (res == null)
		{
			return -1;
		}
		// if (res.size() >= 100)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		// }
		// if (res.size() == 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		// }
		//
		// if (res.size() < 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		// }
		return res.size();
	}

	public static int getResultsForEachStatFile_PrecisionRecallFMeasure2(String pathToWrite, String resultsLabel,
			String pathToRead, double[] muArray, String[] statFileNames, String host, int firstToMax,
			boolean doFirstToMax, boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String dimensionPhrase)
	{
		Map<Integer, Map<Integer, List<Double>>> res = null;

		for (String statFileName : statFileNames)
		{
			res = getResultChosenMU(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, firstToMax,
					doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser, userIdentifierChosenMuMap,
					dimensionPhrase, true, new int[] { 5, 4, 3, 2, 1 }, true, "top5,top4,top3,top2,top1");
			// res = getResult19July2018_MRR_PRF(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host,
			// firstToMax/* 5= Max top1, i.e. last col */, dimensionPhrase, true, new int[] { 5, 4, 3, 2, 1 },
			// true, "top5,top4,top3,top2,top1");
		}
		// if (res.size() >= 100)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		// }
		// if (res.size() == 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		// }
		//
		// if (res.size() < 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		// }
		return res.size();
	}

	/**
	 * Fork of getResultsForEachStatFile Call getResult() for each stat file and accumulates results in a map
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNames
	 * @param host
	 * @param firstToMax
	 * @return
	 * @throws Exception
	 * @since 19 July 2018
	 */
	public static int getResultsForEachStatFile_MRR2(String pathToWrite, String resultsLabel, String pathToRead,
			double[] muArray, String[] statFileNames, String host, int firstToMax, boolean doFirstToMax,
			boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String dimensionPhrase)
	{
		Map<Integer, Map<Integer, List<Double>>> res = null;

		for (String statFileName : statFileNames)
		{
			res = getResultChosenMU(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, firstToMax,
					doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser, userIdentifierChosenMuMap,
					dimensionPhrase, true, new int[] { 1 }, true, "MRR");
			// res = getResult19July2018_MRR_PRF(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, 1,
			// dimensionPhrase, true, new int[] { 1 }, true, "MRR");
		}
		// if (res.size() >= 100)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		// }
		// if (res.size() == 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		// }
		//
		// if (res.size() < 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		// }
		return res.size();
	}

	/**
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNames
	 * @param host
	 * @param firstToMax
	 * @param doFirstToMax
	 * @param doFirstToMaxZeroAware
	 * @param doChosenMUForEachUser
	 * @param userIdentifierChosenMuMap
	 * @param firstToMaxInOrder
	 * @param statFileHasColHeader
	 * @param statFileHasRowHeader
	 * @param dimensionPhrase
	 * @return
	 */
	public static int getResultsForEachStatFile_generic(String pathToWrite, String resultsLabel, String pathToRead,
			double[] muArray, String[] statFileNames, String host, int firstToMax, boolean doFirstToMax,
			boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, int[] firstToMaxInOrder, boolean statFileHasColHeader,
			boolean statFileHasRowHeader, String dimensionPhrase)
	{
		Map<Integer, Map<Integer, List<Double>>> res = null;

		for (String statFileName : statFileNames)
		{
			res = getResultChosenMU(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, firstToMax,
					doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser, userIdentifierChosenMuMap,
					dimensionPhrase, statFileHasColHeader, firstToMaxInOrder, statFileHasRowHeader, "");
			// res = getResult19July2018_MRR_PRF(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, 1,
			// dimensionPhrase, true, new int[] { 1 }, true, "MRR");
		}
		// if (res.size() >= 100)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		// }
		// if (res.size() == 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		// }
		//
		// if (res.size() < 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		// }
		return res.size();
	}

	public static int getResultsForEachStatFile_MRR(String pathToWrite, String resultsLabel, String pathToRead,
			double[] muArray, String[] statFileNames, String host, int firstToMax, String dimensionPhrase)
	{
		Map<Integer, Map<Integer, List<Double>>> res = null;

		for (String statFileName : statFileNames)
		{
			res = getResult19July2018_MRR_PRF(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, 1,
					dimensionPhrase, true, new int[] { 1 }, true, "MRR");
		}
		if (res == null)
		{
			return -1;
		}
		// if (res.size() >= 100)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		// }
		// if (res.size() == 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		// }
		//
		// if (res.size() < 916)
		// {
		// WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		// }
		return res.size();
	}

	/**
	 * Call getResult() for each stat file and accumulates results in a map
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNames
	 * @param host
	 * @param firstToMax
	 * @param doFirstToMax
	 * @param doFirstToMaxZeroAware
	 * @param doChosenMUForEachUser
	 * @param userIdentifierChosenMuMap
	 * @return
	 * @throws Exception
	 */
	public static int getResultsForEachStatFile(String pathToWrite, String resultsLabel, String pathToRead,
			double[] muArray, String[] statFileNames, String host, int firstToMax, boolean doFirstToMax,
			boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String dimensionPhrase, int[] firstToMaxInOrder)
	{
		Map<Integer, Map<Integer, List<Double>>> res = null;

		for (String statFileName : statFileNames)
		{
			res = getResultChosenMU(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, firstToMax,
					doFirstToMax, doFirstToMaxZeroAware, doChosenMUForEachUser, userIdentifierChosenMuMap,
					dimensionPhrase, false, firstToMaxInOrder, false, "First1,First2,First3");
			// new int[] { 5, 4, 3, 2, 1 }, true, "top5,top4,top3,top2,top1");
		}

		if (res == null)
		{
			return -1;
		}

		if (res.size() >= 100)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		}
		if (res.size() == 916)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		}

		if (res.size() < 916)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		}
		return res.size();
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String getHostFromString(String s)
	{
		if (s.trim().toLowerCase().contains("engine")) return ServerUtils.engineHost;
		if (s.trim().toLowerCase().contains("howitzer")) return ServerUtils.howitzerHost;
		if (s.trim().toLowerCase().contains("mortar")) return ServerUtils.mortarHost;
		if (s.trim().toLowerCase().contains("claritytrec")) return ServerUtils.clarityHost;
		if (s.trim().toLowerCase().contains("local")) return ServerUtils.localHost;

		PopUps.printTracedErrorMsgWithExit("Host not found for String:" + s);
		return "unknownHost";

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb2OneDayResults()
	{

		String pathToWrite = "./dataWritten/Feb2/OneDay/";
		int numOfDay = 1;
		int firstToMax = 3;
		int[] orders = { 5, 3, 4, 2, 1 };
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "";

		// prepare AKOM results
		for (int order : orders)
		{
			resultsLabel = "AKOM_916U_915N_" + numOfDay + "dayC_Order-" + order;
			pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb1_AKOM_"
					+ numOfDay + "DayFilter_Order" + order + "/";

			for (String statFileName : statFileNames)
			{
				getResult(pathToWrite, resultsLabel, pathToRead, new double[] { order - 1 }, statFileName,
						ServerUtils.engineHost, firstToMax, "");
			}
		}
		resultsLabel = "Ncount_916U_915N_1dayC_ThreshNN-500";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_Ncount_AllCand1DayFilter_part1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.howitzerHost,
					firstToMax, "");
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshPer-50";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec26_NCount_AllCand1DayFilter_percentile50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.mortarHost, firstToMax,
					"");
		}

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb2FiveDaysResults()
	{

		String pathToWrite = "./dataWritten/Feb2/FiveDays/";
		int numOfDay = 5;
		int firstToMax = 3;
		int[] orders = { 5, 3, 4, 2, 1 };
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "";

		// prepare AKOM results
		for (int order : orders)
		{
			resultsLabel = "AKOM_916U_915N_" + numOfDay + "dayC_Order-" + order;
			pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb1_AKOM_"
					+ numOfDay + "DayFilter_Order" + order + "/";

			for (String statFileName : statFileNames)
			{
				getResult(pathToWrite, resultsLabel, pathToRead, new double[] { order - 1 }, statFileName,
						ServerUtils.engineHost, firstToMax, "");
			}
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.mortarHost, firstToMax,
					"");
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-50";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.engineHost, firstToMax,
					"");
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-100";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold100/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.engineHost, firstToMax,
					"");
		}

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb5FiveDaysResults()
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Feb5/FiveDays/";
		int numOfDay = 5;
		int[] orders = { 5, 3, 4, 2, 1 };
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "";

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-600";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Feb2NCount_5Day_ThresholdNN600/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.howitzerHost,
					firstToMax, "");
		}
	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb11FiveDaysResults()
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Feb11/FiveDays/";
		int numOfDay = 5;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "";

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.engineHost, firstToMax,
					"");
		}
	}

	//

	/**
	 * @since Jan 31 2018
	 * @param args
	 */
	public static void runOneDay()
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Jan31/OneDay/";

		// String rootPath0 =
		// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";

		// "AllPerDirectTopKAgreements_0.csv", "AllPerDirectTopKAgreementsL1_0.csv"

		double muArray[] = Constant.matchingUnitAsPastCount;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };

		// // user index (row index), mu, list of vals
		// LinkedHashMap<Integer, LinkedHashMap<Integer, List<List<Double>>>> userIndexMUKeysValuesMap = new
		// LinkedHashMap<>();

		String pathToRead = "", resultsLabel = "";

		resultsLabel = "AKOM_916U_915N_1dayC_Order-1";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, ServerUtils.howitzerHost,
					firstToMax, "");
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-1_run2";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, ServerUtils.engineHost,
					firstToMax, "");
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 2 }, statFileName, ServerUtils.engineHost,
					firstToMax, "");
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, ServerUtils.engineHost,
					firstToMax, "");
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5_run2";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, ServerUtils.howitzerHost,
					firstToMax, "");
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshNN-500";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_Ncount_AllCand1DayFilter_part1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.howitzerHost,
					firstToMax, "");
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshPer-50";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec26_NCount_AllCand1DayFilter_percentile50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.mortarHost, firstToMax,
					"");
		}

	}

	/**
	 * @since Jan 31 2018
	 * @param args
	 */
	public static void runFiveDays()
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Jan31/FiveDays/";

		// String rootPath0 =
		// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";

		// "AllPerDirectTopKAgreements_0.csv", "AllPerDirectTopKAgreementsL1_0.csv"

		double muArray[] = Constant.matchingUnitAsPastCount;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };

		// // user index (row index), mu, list of vals
		// LinkedHashMap<Integer, LinkedHashMap<Integer, List<List<Double>>>> userIndexMUKeysValuesMap = new
		// LinkedHashMap<>();

		String pathToRead = "", resultsLabel = "";

		resultsLabel = "AKOM_916U_915N_5dayC_Order-1";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter5Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, ServerUtils.howitzerHost,
					firstToMax, "");
		}

		resultsLabel = "AKOM_916U_915N_5dayC_Order-1_run2";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, ServerUtils.engineHost,
					firstToMax, "");
		}

		////////////////////////
		resultsLabel = "AKOM_916U_915N_5dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 2 }, statFileName, ServerUtils.engineHost,
					firstToMax, "");
		}

		resultsLabel = "AKOM_916U_915N_5dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, ServerUtils.engineHost,
					firstToMax, "");
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.mortarHost, firstToMax,
					"");
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-50";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.engineHost, firstToMax,
					"");
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-100";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold100/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.engineHost, firstToMax,
					"");
		}

	}

	/**
	 * used before 31 Jan: old results
	 * 
	 * @param args
	 */
	public static void mainBefore31Jan2018(String[] args)
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Jan26/";

		// String rootPath0 =
		// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";

		// "AllPerDirectTopKAgreements_0.csv", "AllPerDirectTopKAgreementsL1_0.csv"

		double muArray[] = Constant.matchingUnitAsPastCount;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };

		// // user index (row index), mu, list of vals
		// LinkedHashMap<Integer, LinkedHashMap<Integer, List<List<Double>>>> userIndexMUKeysValuesMap = new
		// LinkedHashMap<>();

		String pathToRead = "", resultsLabel = "";

		resultsLabel = "AKOM_916U_915N_1dayC_Order-1";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, ServerUtils.howitzerHost,
					firstToMax, "");
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_AKOM_1DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, ServerUtils.engineHost,
					firstToMax, "");
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, ServerUtils.engineHost,
					firstToMax, "");
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshNN-500";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_Ncount_AllCand1DayFilter_part1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, ServerUtils.howitzerHost,
					firstToMax, "");
		}

	}

	/**
	 * Fork of org.activity.evaluation.ResultsDistributionEvaluation.getResult(String, String, String, double[], String,
	 * String, int, String) to allow refactoring without having to take care of compatibility of other than concerned
	 * calls to it.
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNameToRead
	 * @param host
	 * @param firstToMax
	 * @param doFirstToMax
	 * @param dimensionPhrase
	 * @param statFileHasHeader
	 * @param firstToMaxInOrder
	 * @return
	 * @since 19 July 2018
	 */
	private static Map<Integer, Map<Integer, List<Double>>> getResult19July2018(String pathToWrite, String resultsLabel,
			String pathToRead, double[] muArray, String statFileNameToRead, String host, int firstToMax,
			String dimensionPhrase, boolean statFileHasHeader, int[] firstToMaxInOrder)

	{
		String passwd = ServerUtils.getPassWordForHost(host);
		String user = ServerUtils.getUserForHost(host);

		// MU , <list for each user, <list of first1,2,3 for that user and mu>>
		Map<Integer, List<List<Double>>> muKeyAllValsMap = new LinkedHashMap<>();

		// Convert to user wise result
		// userIndex , <MU, <list of first1,2,3 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = null;

		String userSetLabel = "";
		try
		{// if PureAKOM, then mu is order of AKOM -1
			if (pathToRead.contains("PureAKOMOrder"))
			{
				String[] splitted = pathToRead.split("Order");
				String[] splitted2 = splitted[1].split("/");
				double muForOrder = Double.valueOf(splitted2[0]);
				muArray = new double[] { muForOrder - 1 };
			}
			if (pathToRead.contains("Set"))
			{
				int i = pathToRead.indexOf("Set");
				userSetLabel = pathToRead.substring(i, i + 4);
			}

			for (double muD : muArray)
			{
				int mu = (int) muD;
				List<List<Double>> readResFromFile = null;

				String statFileForThisMU = pathToRead + statFileNameToRead + mu + dimensionPhrase + ".csv";

				if (host.contains("local"))
				{// each row corresponds to a user
					readResFromFile = ReadingFromFile.nColumnReaderDouble(statFileForThisMU, ",", statFileHasHeader);
				}
				else
				{
					Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port,
							statFileForThisMU, user, passwd);
					readResFromFile = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",",
							statFileHasHeader);
				}
				// System.out.println("mu= " + mu + " res=" + res);
				muKeyAllValsMap.put(mu, readResFromFile);
				// inputAndSession.getSecond().disconnect();
			}

			// Convert to user wise result
			// userIndex , <MU, <list of first1,2,3 for that user and mu>>
			userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

			writeUserMuKeyVals(userMUKeyVals,
					pathToWrite + resultsLabel + "_" + statFileNameToRead + "userMUKeyVals" + dimensionPhrase + ".csv",
					userSetLabel, "First1,First2,First3");

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Pair<Integer, List<Double>>> firstsForMinMUWithMaxFxForEachUser = getValsForMinMUHavingMaxF3(
					firstToMax, userMUKeyVals);
			StringBuilder sb = new StringBuilder("userIndex,minMuWithMaxFirst3,First1,First2,First3\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForMinMUWithMaxFxForEachUser.entrySet())
			{
				// sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
				// + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
				// + r.getValue().getSecond().get(2) + "\n");
				// modified on 19 July 2018 so that it also works only first1
				sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
				for (double firstVal : r.getValue().getSecond())
				{
					sb.append("," + firstVal);
				}
				sb.append("\n");
			}
			WToFile.writeToNewFile(sb.toString(), pathToWrite + resultsLabel + "_" + statFileNameToRead
					+ "MinMUWithMaxFirst" + firstToMax + dimensionPhrase + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForMinMUWithMaxFx0AwareForEachUser = getValsForMinMUHavingMaxF3ZeroAware(
					firstToMaxInOrder, userMUKeyVals);
			StringBuilder sb2 = new StringBuilder("userIndex,minMuWithMaxFirst3,First1,First2,First3,ChosenFirst\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Triple<Integer, List<Double>, Integer>> r : firstsForMinMUWithMaxFx0AwareForEachUser
					.entrySet())
			{
				// sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
				// + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
				// + r.getValue().getSecond().get(2) + "," + r.getValue().getThird() + "\n");
				sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
				for (double firstVal : r.getValue().getSecond())
				{
					sb2.append("," + firstVal);
				}
				sb2.append("," + r.getValue().getThird() + "\n");
			}
			WToFile.writeToNewFile(sb2.toString(), pathToWrite + resultsLabel + "_" + statFileNameToRead
					+ "MinMUWithMaxFirst0Aware" + dimensionPhrase + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			WToFile.writeToNewFile(host + ":" + pathToRead, pathToWrite + "ReadMe/" + resultsLabel + "_"
					+ statFileNameToRead + "ReadMe" + dimensionPhrase + ".txt");
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			String s = "pathToRead=" + pathToRead + "\nresultsLabel=" + resultsLabel + "\nstatFileNameToRead="
					+ statFileNameToRead;
			WToFile.appendLineToFileAbs(PopUps.getTracedErrorMsg("\n\nException in getResult()\n") + " \nfor:" + s,
					pathToWrite + "ExceptionsEncountered" + dimensionPhrase + ".csv");

			System.exit(-1);
			return null;
		}
		return (userMUKeyVals);

	}

	/**
	 * Fork of org.activity.evaluation.ResultsDistributionEvaluation.getResult19July2018(String, String, String,
	 * double[], String, String, int, String) for MRR, avg percision , recall, fmeasure.
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNameToRead
	 *            MRR, AvgPrecision, AvgRecall, AvgFMeasure at top 5,4,3,2,1
	 * @param host
	 * @param firstToMax
	 * @param dimensionPhrase
	 * @param statFileHasColHeader
	 * @param firstToMaxInOrder
	 * @param statFileHasRowHeader
	 * @param valColHeader
	 *            "top5,top4,top3,top2,top1"
	 * @return
	 */
	private static Map<Integer, Map<Integer, List<Double>>> getResult19July2018_MRR_PRF(String pathToWrite,
			String resultsLabel, String pathToRead, double[] muArray, String statFileNameToRead, String host,
			int firstToMax, String dimensionPhrase, boolean statFileHasColHeader, int[] firstToMaxInOrder,
			boolean statFileHasRowHeader, String valColHeader)

	{
		String passwd = ServerUtils.getPassWordForHost(host);
		String user = ServerUtils.getUserForHost(host);

		// MU , <list for each user, <list of recall@5,recall@4,...recall@1 for that user and mu>>
		Map<Integer, List<List<Double>>> muKeyAllValsMap = new LinkedHashMap<>();

		// Convert to user wise result
		// userIndex , <MU, <list of recall@5,recall@4,...recall@1 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = null;

		String userSetLabel = "";
		try
		{// if PureAKOM, then mu is order of AKOM -1
			if (pathToRead.contains("PureAKOMOrder"))
			{
				String[] splitted = pathToRead.split("Order");
				String[] splitted2 = splitted[1].split("/");
				double muForOrder = Double.valueOf(splitted2[0]);
				muArray = new double[] { muForOrder - 1 };
			}
			if (pathToRead.contains("Set"))
			{
				int i = pathToRead.indexOf("Set");
				userSetLabel = pathToRead.substring(i, i + 4);
			}

			for (double muD : muArray)
			{
				int mu = (int) muD;
				List<List<Double>> readResFromFile = null;

				String statFileForThisMU = pathToRead + statFileNameToRead + mu + dimensionPhrase + ".csv";

				if (host.contains("local"))
				{// each row corresponds to a user
					readResFromFile = ReadingFromFile.nColumnReaderDouble(statFileForThisMU, ",", statFileHasColHeader,
							statFileHasRowHeader);
				}
				else
				{
					Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port,
							statFileForThisMU, user, passwd);
					readResFromFile = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",",
							statFileHasColHeader, statFileHasRowHeader);
				}
				// System.out.println("mu= " + mu + " res=" + res);
				muKeyAllValsMap.put(mu, readResFromFile);
				// inputAndSession.getSecond().disconnect();
			}

			// Convert to user wise result
			// userIndex , <MU, <list of first1,2,3 for that user and mu>>
			userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

			writeUserMuKeyVals(userMUKeyVals,
					pathToWrite + resultsLabel + "_" + statFileNameToRead + "userMUKeyVals" + dimensionPhrase + ".csv",
					userSetLabel, valColHeader);

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Pair<Integer, List<Double>>> firstsForMinMUWithMaxFxForEachUser = getValsForMinMUHavingMaxF3(
					firstToMax, userMUKeyVals);
			StringBuilder sb = new StringBuilder("userIndex,minMuWithMaxFirst3," + valColHeader + "\n");
			// IntStream.rangeClosed(1, 3);
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForMinMUWithMaxFxForEachUser.entrySet())
			{
				// sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
				// + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
				// + r.getValue().getSecond().get(2) + "\n");
				// modified on 19 July 2018 so that it also works only first1
				sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
				for (double firstVal : r.getValue().getSecond())
				{
					sb.append("," + firstVal);
				}
				sb.append("\n");
			}
			WToFile.writeToNewFile(sb.toString(), pathToWrite + resultsLabel + "_" + statFileNameToRead
					+ "MinMUWithMaxFirst" + firstToMax + dimensionPhrase + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForMinMUWithMaxFx0AwareForEachUser = getValsForMinMUHavingMaxF3ZeroAware(
					firstToMaxInOrder, userMUKeyVals);
			StringBuilder sb2 = new StringBuilder("userIndex,minMuWithMaxFirst3," + valColHeader + ",ChosenFirst\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Triple<Integer, List<Double>, Integer>> r : firstsForMinMUWithMaxFx0AwareForEachUser
					.entrySet())
			{
				// sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
				// + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
				// + r.getValue().getSecond().get(2) + "," + r.getValue().getThird() + "\n");
				sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
				for (double firstVal : r.getValue().getSecond())
				{
					sb2.append("," + firstVal);
				}
				sb2.append("," + r.getValue().getThird() + "\n");
			}
			WToFile.writeToNewFile(sb2.toString(), pathToWrite + resultsLabel + "_" + statFileNameToRead
					+ "MinMUWithMaxFirst0Aware" + dimensionPhrase + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			WToFile.writeToNewFile(host + ":" + pathToRead, pathToWrite + "ReadMe/" + resultsLabel + "_"
					+ statFileNameToRead + "ReadMe" + dimensionPhrase + ".txt");
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			WToFile.appendLineToFileAbs(PopUps.getCurrentStackTracedWarningMsg("\n\nException in getResult()\n"),
					pathToWrite + "ExceptionsEncountered" + dimensionPhrase + ".csv");
			System.exit(-1);
			return null;
		}
		return (userMUKeyVals);

	}

	/**
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNameToRead
	 * @param host
	 * @param firstToMax
	 * @param doFirstToMax
	 * @param dimensionPhrase
	 *            added on 19 July 2018
	 * @return
	 */
	private static Map<Integer, Map<Integer, List<Double>>> getResult(String pathToWrite, String resultsLabel,
			String pathToRead, double[] muArray, String statFileNameToRead, String host, int firstToMax,
			String dimensionPhrase)

	{
		String passwd = ServerUtils.getPassWordForHost(host);
		String user = ServerUtils.getUserForHost(host);

		// MU , <list for each user, <list of first1,2,3 for that user and mu>>
		Map<Integer, List<List<Double>>> muKeyAllValsMap = new LinkedHashMap<>();

		// Convert to user wise result
		// userIndex , <MU, <list of first1,2,3 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = null;

		String userSetLabel = "";
		try
		{// if PureAKOM, then mu is order of AKOM -1
			if (pathToRead.contains("PureAKOMOrder"))
			{
				String[] splitted = pathToRead.split("Order");
				String[] splitted2 = splitted[1].split("/");
				double muForOrder = Double.valueOf(splitted2[0]);
				muArray = new double[] { muForOrder - 1 };
			}
			if (pathToRead.contains("Set"))
			{
				int i = pathToRead.indexOf("Set");
				userSetLabel = pathToRead.substring(i, i + 4);
			}

			for (double muD : muArray)
			{
				int mu = (int) muD;
				List<List<Double>> readResFromFile = null;

				String statFileForThisMU = pathToRead + statFileNameToRead + mu + dimensionPhrase + ".csv";

				if (host.contains("local"))
				{// each row corresponds to a user
					readResFromFile = ReadingFromFile.nColumnReaderDouble(statFileForThisMU, ",", false);
				}
				else
				{
					Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port,
							statFileForThisMU, user, passwd);
					readResFromFile = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",", false);
				}
				// System.out.println("mu= " + mu + " res=" + res);
				muKeyAllValsMap.put(mu, readResFromFile);
				// inputAndSession.getSecond().disconnect();
			}

			// Convert to user wise result
			// userIndex , <MU, <list of first1,2,3 for that user and mu>>
			userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

			writeUserMuKeyVals(userMUKeyVals,
					pathToWrite + resultsLabel + "_" + statFileNameToRead + "userMUKeyVals" + dimensionPhrase + ".csv",
					userSetLabel, "First1,First2,First3");

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Pair<Integer, List<Double>>> firstsForMinMUWithMaxFxForEachUser = getValsForMinMUHavingMaxF3(
					firstToMax, userMUKeyVals);
			StringBuilder sb = new StringBuilder("userIndex,minMuWithMaxFirst3,First1,First2,First3\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForMinMUWithMaxFxForEachUser.entrySet())
			{
				sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
						+ r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
						+ r.getValue().getSecond().get(2) + "\n");
			}
			WToFile.writeToNewFile(sb.toString(), pathToWrite + resultsLabel + "_" + statFileNameToRead
					+ "MinMUWithMaxFirst" + firstToMax + dimensionPhrase + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForMinMUWithMaxFx0AwareForEachUser = getValsForMinMUHavingMaxF3ZeroAware(
					new int[] { 3, 2, 1 }, userMUKeyVals);
			StringBuilder sb2 = new StringBuilder("userIndex,minMuWithMaxFirst3,First1,First2,First3,ChosenFirst\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Triple<Integer, List<Double>, Integer>> r : firstsForMinMUWithMaxFx0AwareForEachUser
					.entrySet())
			{
				sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
						+ r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
						+ r.getValue().getSecond().get(2) + "," + r.getValue().getThird() + "\n");
			}
			WToFile.writeToNewFile(sb2.toString(), pathToWrite + resultsLabel + "_" + statFileNameToRead
					+ "MinMUWithMaxFirst0Aware" + dimensionPhrase + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			WToFile.writeToNewFile(host + ":" + pathToRead, pathToWrite + "ReadMe/" + resultsLabel + "_"
					+ statFileNameToRead + "ReadMe" + dimensionPhrase + ".txt");
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			WToFile.appendLineToFileAbs(PopUps.getCurrentStackTracedWarningMsg("\n\nException in getResult()\n"),
					pathToWrite + "ExceptionsEncountered" + dimensionPhrase + ".csv");
			System.exit(-1);
			return null;
		}
		return (userMUKeyVals);

	}

	/**
	 * Fork of org.activity.evaluation.ResultsDistributionEvaluation.getResult(String, String, String, double[], String,
	 * String, int)
	 * <p>
	 * TODO: safely delete this later after safely refactoring calls to this method, as it can now be replaced by the
	 * succeeding method.
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileName
	 * @param host
	 * @param firstToMax
	 * @param doFirstToMax
	 * @param doFirstToMaxZeroAware
	 * @param doChosenMUForEachUser
	 * @param userIdentifierChosenMuMap
	 * @return
	 * @since May 4 2018
	 * @deprecated
	 */
	private static Map<Integer, Map<Integer, List<Double>>> getResult(String pathToWrite, String resultsLabel,
			String pathToRead, double[] muArray, String statFileName, String host, int firstToMax, boolean doFirstToMax,
			boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String dimensionPhrase, boolean statFileHasHeader,
			int[] firstToMaxInOrder)
	{
		String passwd = ServerUtils.getPassWordForHost(host);
		String user = ServerUtils.getUserForHost(host);

		// MU , <list for each user, <list of first1,2,3 for that user and mu>>
		Map<Integer, List<List<Double>>> muKeyAllValsMap = new LinkedHashMap<>();

		// Convert to user wise result
		// userIndex , <MU, <list of first1,2,3 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = null;

		String userSetLabel = "";
		try
		{
			// if PureAKOM, then mu is order of AKOM -1
			if (pathToRead.contains("PureAKOMOrder"))
			{
				String[] splitted = pathToRead.split("Order");
				String[] splitted2 = splitted[1].split("/");
				double muForOrder = Double.valueOf(splitted2[0]);
				muArray = new double[] { muForOrder - 1 };
			}
			else if (pathToRead.contains("RNN1"))
			{
				muArray = new double[] { 0 };
			}
			if (pathToRead.contains("Set"))
			{
				int i = pathToRead.indexOf("Set");
				userSetLabel = pathToRead.substring(i, i + 4);
			}

			for (double muD : muArray)
			{
				int mu = (int) muD;
				List<List<Double>> readResFromFile = null;

				// String statFileForThisMU = pathToRead + statFileName + mu + ".csv";
				String statFileForThisMU = pathToRead + statFileName + mu + dimensionPhrase + ".csv";

				if (host.contains("local"))
				{// each row corresponds to a user
					readResFromFile = ReadingFromFile.nColumnReaderDouble(statFileForThisMU, ",", statFileHasHeader);
				}
				else
				{
					Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port,
							statFileForThisMU, user, passwd);
					readResFromFile = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",",
							statFileHasHeader);
				}
				// System.out.println("mu= " + mu + " res=" + res);
				muKeyAllValsMap.put(mu, readResFromFile);
				// inputAndSession.getSecond().disconnect();
			}

			// Convert to user wise result
			// userIndex , <MU, <list of first1,2,3 for that user and mu>>
			userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

			writeUserMuKeyVals(userMUKeyVals,
					pathToWrite + resultsLabel + "_" + statFileName + "userMUKeyVals" + dimensionPhrase + ".csv",
					userSetLabel, "First1,First2,First3");

			/////////////////////////////////////////////////////////////////////////////////////////////////
			if (doFirstToMax)
			{
				// userIndex, min mu with highest first 3, first3 for this mu and user
				Map<Integer, Pair<Integer, List<Double>>> firstsForMinMUWithMaxFxForEachUser = getValsForMinMUHavingMaxF3(
						firstToMax, userMUKeyVals);
				StringBuilder sb = new StringBuilder("userIndex,minMuWithMaxFirst3,First1,First2,First3\n");
				//// user, min mu with highest first 3, first3 for this mu and user
				for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForMinMUWithMaxFxForEachUser.entrySet())
				{
					sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
					// + "," + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
					// + r.getValue().getSecond().get(2) + "\n");

					for (double v : r.getValue().getSecond())
					{
						sb.append("," + v);
					}
					sb.append("\n");
				}

				WToFile.writeToNewFile(sb.toString(), pathToWrite + resultsLabel + "_" + statFileName
						+ "MinMUWithMaxFirst" + firstToMax + dimensionPhrase + ".csv");
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////////////////////////////
			if (doFirstToMaxZeroAware)
			{
				// userIndex, min mu with highest first 3, first3 for this mu and user
				Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForMinMUWithMaxFx0AwareForEachUser = getValsForMinMUHavingMaxF3ZeroAware(
						firstToMaxInOrder, userMUKeyVals);
				StringBuilder sb2 = new StringBuilder(
						"userIndex,minMuWithMaxFirst3,First1,First2,First3,ChosenFirst\n");
				//// user, min mu with highest first 3, first3 for this mu and user
				for (Entry<Integer, Triple<Integer, List<Double>, Integer>> r : firstsForMinMUWithMaxFx0AwareForEachUser
						.entrySet())
				{
					sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
					// + "," + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
					// + r.getValue().getSecond().get(2) + "," + r.getValue().getThird() + "\n");
					for (double d : r.getValue().getSecond())
					{
						sb2.append("," + d);
					}
					sb2.append("," + r.getValue().getThird() + "\n");

				}
				WToFile.writeToNewFile(sb2.toString(), pathToWrite + resultsLabel + "_" + statFileName
						+ "MinMUWithMaxFirst0Aware" + dimensionPhrase + ".csv");
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////////////////////////////
			if (doChosenMUForEachUser)
			{
				// userIndex, min mu with highest first 3, first3 for this mu and user
				Map<Integer, Pair<Integer, List<Double>>> firstsForChosenMUForEachUser = getValsForCHosenMUForEachUser(
						userMUKeyVals, userIdentifierChosenMuMap, userSetLabel, muArray, pathToRead);

				StringBuilder sb = new StringBuilder("userIndex,ChosenMU,First1,First2,First3\n");
				//// user, min mu with highest first 3, first3 for this mu and user
				for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForChosenMUForEachUser.entrySet())
				{
					sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
					// + ","+ r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
					// + r.getValue().getSecond().get(2) + "\n");
					for (double v : r.getValue().getSecond())
					{
						sb.append("," + v);
					}
					sb.append("\n");
				}
				WToFile.writeToNewFile(sb.toString(),
						pathToWrite + resultsLabel + "_" + statFileName + "ChosenMU" + dimensionPhrase + ".csv");
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////
			WToFile.writeToNewFile(host + ":" + pathToRead,
					pathToWrite + "ReadMe/" + resultsLabel + "_" + statFileName + "ReadMe" + dimensionPhrase + ".txt");
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			String s = "pathToRead=" + pathToRead + "\nresultsLabel=" + resultsLabel + "\nstatFileNameToRead="
					+ statFileName;
			WToFile.appendLineToFileAbs(
					PopUps.getCurrentStackTracedWarningMsg("\n\nException in getResult()\n") + " \nfor:" + s,
					pathToWrite + "ExceptionsEncountered" + dimensionPhrase + ".csv");

			System.exit(-1);
			return null;
		}
		return (userMUKeyVals);
	}

	/**
	 * Fork of org.activity.evaluation.ResultsDistributionEvaluation.getResult(String, String, String, double[], String,
	 * String, int, boolean, boolean, boolean, Map<String, Integer>, String, boolean, int[])
	 * <p>
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileName
	 * @param host
	 * @param firstToMax
	 * @param doFirstToMax
	 * @param doFirstToMaxZeroAware
	 * @param doChosenMUForEachUser
	 * @param userIdentifierChosenMuMap
	 * @param dimensionPhrase
	 * @param statFileHasColHeader
	 * @param firstToMaxInOrder
	 * @param statFileHasRowHeader
	 * @param valColHeader
	 *            header for values columns. if valColHeader is empty then extract valColHeader from file being read
	 *            (modification 17 Jan 2019)
	 * @return
	 * @since 19 July 2018
	 */
	private static Map<Integer, Map<Integer, List<Double>>> getResultChosenMU(String pathToWrite, String resultsLabel,
			String pathToRead, double[] muArray, String statFileName, String host, int firstToMax, boolean doFirstToMax,
			boolean doFirstToMaxZeroAware, boolean doChosenMUForEachUser,
			Map<String, Integer> userIdentifierChosenMuMap, String dimensionPhrase, boolean statFileHasColHeader,
			int[] firstToMaxInOrder, boolean statFileHasRowHeader, String valColHeader)
	{
		// PopUps.showMessage("Inside getResultChosenMU, host = " + host);
		if (firstToMaxInOrder.length == 0)
		{
			System.out.println("Warning: since firstToMaxInOrder is of length: " + firstToMaxInOrder.length
					+ " setting doFirstToMax and doFirstToMaxZeroAware to false");
			doFirstToMax = false;
			doFirstToMaxZeroAware = false;
		}

		// PopUps.showMessage("here 00");
		String passwd = ServerUtils.getPassWordForHost(host);
		String user = ServerUtils.getUserForHost(host);

		// PopUps.showMessage("here 02");
		// MU , <list for each user, <list of first1,2,3 for that user and mu>>
		Map<Integer, List<List<Double>>> muKeyAllValsMap = new LinkedHashMap<>();

		// Convert to user wise result
		// userIndex , <MU, <list of first1,2,3 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = null;

		String userSetLabel = "";
		// PopUps.showMessage("here 01");
		try
		{
			// if PureAKOM, then mu is order of AKOM -1
			if (pathToRead.contains("PureAKOMOrder"))
			{
				String[] splitted = pathToRead.split("Order");
				String[] splitted2 = splitted[1].split("/");

				splitted2[0] = splitted2[0].replace("NoTTFilter", "");// added on 1 Jan 2018
				splitted2[0] = splitted2[0].replace("PNN500", "");// added on 1 Jan 2018
				splitted2[0] = splitted2[0].replace("PNN50", "");// added on 1 Jan 2018
				splitted2[0] = splitted2[0].replace("PNN100", "");// added on 1 Jan 2018
				splitted2[0] = splitted2[0].replace("coll", "");// added on 7 March 2019
				splitted2[0] = splitted2[0].replace("Seq", "");

				double muForOrder = Double.valueOf(splitted2[0]);
				muArray = new double[] { muForOrder - 1 };
				// PopUps.showMessage("muArray = " + muArray);
			}
			else if (pathToRead.contains("RNN1"))
			{
				muArray = new double[] { 0 };
			}
			if (pathToRead.contains("Set"))
			{
				int i = pathToRead.indexOf("Set");
				userSetLabel = pathToRead.substring(i, i + 4);
			}

			// will be same of all MUs, hence should do it only once
			// valColHeader = valColHeader.length() == 0 ? "" : valColHeader;// added on 17 Jan 2019,

			for (double muD : muArray)
			{
				int mu = (int) muD;
				List<List<Double>> readResFromFile = null;

				// String statFileForThisMU = pathToRead + statFileName + mu + ".csv";
				String statFileForThisMU = pathToRead + statFileName + mu + dimensionPhrase + ".csv";
				List<String> valColHeaderList = null;

				if (host.contains("local"))
				{// each row corresponds to a user
					readResFromFile = ReadingFromFile.nColumnReaderDouble(statFileForThisMU, ",", statFileHasColHeader,
							statFileHasRowHeader);

					if (valColHeader.length() == 0)
					{
						valColHeaderList = ReadingFromFile.nColumnReaderString(statFileForThisMU, ",", false).get(0);
					}
				}
				else
				{
					Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port,
							statFileForThisMU, user, passwd);

					readResFromFile = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",",
							statFileHasColHeader, statFileHasRowHeader);

					if (valColHeader.length() == 0)
					{
						valColHeaderList = ReadingFromFile.nColumnReaderString(inputAndSession.getFirst(), ",", false)
								.get(0);
					}
				}

				if (valColHeaderList != null && statFileHasColHeader)
				{
					valColHeaderList.remove(0);
					valColHeader = valColHeaderList.stream().collect(Collectors.joining(","));
				}

				// System.out.println("mu= " + mu + " res=" + res);
				muKeyAllValsMap.put(mu, readResFromFile);
				// inputAndSession.getSecond().disconnect();
			}

			// Convert to user wise result
			// userIndex , <MU, <list of first1,2,3 for that user and mu>>
			userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

			writeUserMuKeyVals(userMUKeyVals,
					pathToWrite + resultsLabel + "_" + statFileName + "userMUKeyVals" + dimensionPhrase + ".csv",
					userSetLabel, valColHeader);
			// PopUps.showMessage("here1");
			/////////////////////////////////////////////////////////////////////////////////////////////////
			if (doFirstToMax)
			{
				// userIndex, min mu with highest first 3, first3 for this mu and user
				Map<Integer, Pair<Integer, List<Double>>> firstsForMinMUWithMaxFxForEachUser = getValsForMinMUHavingMaxF3(
						firstToMax, userMUKeyVals);
				StringBuilder sb = new StringBuilder("userIndex,minMuWithMaxFirst3," + valColHeader + "\n");
				//// user, min mu with highest first 3, first3 for this mu and user
				for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForMinMUWithMaxFxForEachUser.entrySet())
				{
					sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
					// + "," + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
					// + r.getValue().getSecond().get(2) + "\n");

					for (double v : r.getValue().getSecond())
					{
						sb.append("," + v);
					}
					sb.append("\n");
				}

				WToFile.writeToNewFile(sb.toString(), pathToWrite + resultsLabel + "_" + statFileName
						+ "MinMUWithMaxFirst" + firstToMax + dimensionPhrase + ".csv");
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////
			// PopUps.showMessage("here2");
			/////////////////////////////////////////////////////////////////////////////////////////////////
			if (doFirstToMaxZeroAware)
			{
				// userIndex, min mu with highest first 3, first3 for this mu and user
				Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForMinMUWithMaxFx0AwareForEachUser = getValsForMinMUHavingMaxF3ZeroAware(
						firstToMaxInOrder, userMUKeyVals);
				StringBuilder sb2 = new StringBuilder(
						"userIndex,minMuWithMaxFirst3," + valColHeader + ",ChosenFirst\n");
				//// user, min mu with highest first 3, first3 for this mu and user
				for (Entry<Integer, Triple<Integer, List<Double>, Integer>> r : firstsForMinMUWithMaxFx0AwareForEachUser
						.entrySet())
				{
					sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
					// + "," + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
					// + r.getValue().getSecond().get(2) + "," + r.getValue().getThird() + "\n");
					for (double d : r.getValue().getSecond())
					{
						sb2.append("," + d);
					}
					sb2.append("," + r.getValue().getThird() + "\n");

				}
				WToFile.writeToNewFile(sb2.toString(), pathToWrite + resultsLabel + "_" + statFileName
						+ "MinMUWithMaxFirst0Aware" + dimensionPhrase + ".csv");
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////
			// PopUps.showMessage("here3");
			/////////////////////////////////////////////////////////////////////////////////////////////////
			if (doChosenMUForEachUser)
			{
				// userIndex, min mu with highest first 3, first3 for this mu and user
				Map<Integer, Pair<Integer, List<Double>>> firstsForChosenMUForEachUser = getValsForCHosenMUForEachUser(
						userMUKeyVals, userIdentifierChosenMuMap, userSetLabel, muArray, pathToRead);

				StringBuilder sb = new StringBuilder("userIndex,ChosenMU," + valColHeader + "\n");
				//// user, min mu with highest first 3, first3 for this mu and user
				for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForChosenMUForEachUser.entrySet())
				{
					sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
					// + ","+ r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
					// + r.getValue().getSecond().get(2) + "\n");
					for (double v : r.getValue().getSecond())
					{
						sb.append("," + v);
					}
					sb.append("\n");
				}
				WToFile.writeToNewFile(sb.toString(),
						pathToWrite + resultsLabel + "_" + statFileName + "ChosenMU" + dimensionPhrase + ".csv");
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////
			WToFile.writeToNewFile(host + ":" + pathToRead,
					pathToWrite + "ReadMe/" + resultsLabel + "_" + statFileName + "ReadMe" + dimensionPhrase + ".txt");
		}
		catch (Exception e)
		{

			e.printStackTrace();
			PopUps.showException(e, "getResultChosenMU()");
			String s = "pathToRead=" + pathToRead + "\nresultsLabel=" + resultsLabel + "\nstatFileNameToRead="
					+ statFileName;
			WToFile.appendLineToFileAbs(
					PopUps.getCurrentStackTracedWarningMsg("\n\nException in getResultChosenMU()\n") + " \nfor:" + s,
					pathToWrite + "ExceptionsEncountered" + dimensionPhrase + ".csv");

			System.exit(-1);
			return null;
		}
		return (userMUKeyVals);
	}

	/**
	 * Write rows of for all MUs "UserIndex,MU,First1,First2,First3"
	 * 
	 * @param userMUKeyVals
	 * @param fileNameToWrite
	 * @param userSetLabel
	 * @param valColHeader
	 *            "First1,First2,First3
	 */
	private static void writeUserMuKeyVals(Map<Integer, Map<Integer, List<Double>>> userMUKeyVals,
			String fileNameToWrite, String userSetLabel, String valColHeader)
	{
		StringBuilder sb = new StringBuilder("UserIndex,MU," + valColHeader + "\n");
		for (Entry<Integer, Map<Integer, List<Double>>> e : userMUKeyVals.entrySet())
		{
			Integer userIndex = e.getKey();
			for (Entry<Integer, List<Double>> e2 : e.getValue().entrySet())
			{
				sb.append(userSetLabel + userIndex + "," + e2.getKey() + ","
						+ e2.getValue().stream().map(i -> i.toString()).collect(Collectors.joining(",")) + "\n");
			}
		}
		WToFile.writeToNewFile(sb.toString(), fileNameToWrite);
	}

	/**
	 * 
	 * @param firstToMax
	 *            this will determine which MU is chose, min mu with max firstToMax (say First3) is chosen
	 * @param userMUKeyVals
	 *            { userIndex , {MU, {list of first1,2,3 for that user and mu}}}
	 * @return { userIndex,{ min mu with highest first 3, first3 for this mu and user}}
	 */
	private static Map<Integer, Pair<Integer, List<Double>>> getValsForMinMUHavingMaxF3(int firstToMax,
			Map<Integer, Map<Integer, List<Double>>> userMUKeyVals)
	{
		// userIndex, min mu with highest first 3, first3 for this mu and user
		Map<Integer, Pair<Integer, List<Double>>> firstsWithHighF3ResultForEachUserOverMUs = new LinkedHashMap<>();

		for (Entry<Integer, Map<Integer, List<Double>>> userVals : userMUKeyVals.entrySet())
		{
			int userIndex = userVals.getKey();
			// MU, list of first 1,2,3 for that mu for current user
			Map<Integer, List<Double>> valsForAllMUs = userVals.getValue();

			// find max first 3 over all MUs
			double maxFirst3OverAllMUS = valsForAllMUs.entrySet().stream()
					.mapToDouble(e -> e.getValue().get(firstToMax - 1)).max().getAsDouble();

			// find MUs having Max first 3 found just above.
			Set<Integer> musHavingTheMax = valsForAllMUs.entrySet().stream()
					.filter(e -> e.getValue().get(firstToMax - 1).equals(maxFirst3OverAllMUS)).map(e -> e.getKey())
					.collect(Collectors.toSet());

			// find min MU having max first 3
			int minMUHavingMaxFirst3ForThisUser = musHavingTheMax.stream().mapToInt(e -> e).min().getAsInt();

			firstsWithHighF3ResultForEachUserOverMUs.put(userIndex, new Pair<Integer, List<Double>>(
					minMUHavingMaxFirst3ForThisUser, valsForAllMUs.get(minMUHavingMaxFirst3ForThisUser)));

		}
		return firstsWithHighF3ResultForEachUserOverMUs;
	}

	/**
	 * instead of choosing minMUHavingMaxFirst3, if maxfirst3 is 0 then choose the min mu having max first 2 and if that
	 * is 0 then choose min mu having max first 1
	 * 
	 * @param firstsToMaxInOrder
	 *            this will determine which MU is chose, min mu with max firstToMax (say First3) is chosen, {a,b,c} If
	 *            max First_a is 0 then choose max First_b, if that is 0 then choose max First_c.
	 * @param userMUKeyVals
	 *            { userIndex , {MU, {list of first1,2,3 for that user and mu}}}
	 * @return { userIndex,{ min mu with highest first 3, corresponding first1 2 & 3 vals for this mu &
	 *         user,chosenFirstForThisUser}}
	 */
	private static Map<Integer, Triple<Integer, List<Double>, Integer>> getValsForMinMUHavingMaxF3ZeroAware(
			int[] firstsToMaxInOrder, Map<Integer, Map<Integer, List<Double>>> userMUKeyVals)
	{
		// {userIndex,Triple {
		// minMU with highest chosen first (either 3 2 or 1),
		// corresponding first1 2 & 3 vals for this mu & user,
		// chosenFirstForThisUser
		// }
		Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForHighChosenFirstResultForEachUserOverMUs = new LinkedHashMap<>();

		for (Entry<Integer, Map<Integer, List<Double>>> userVals : userMUKeyVals.entrySet())
		{
			int userIndex = userVals.getKey();
			// MU, list of first 1,2,3 for that mu for current user
			Map<Integer, List<Double>> valsForAllMUs = userVals.getValue();

			int chosenFirstToMax = -99999;// whether first 3, 2, or 1
			Double maxOfChosenFirstOverAllMUS = Double.valueOf(-99999);

			for (int firstToMax : firstsToMaxInOrder)
			{
				// find max first 3 over all MUs
				maxOfChosenFirstOverAllMUS = valsForAllMUs.entrySet().stream()
						.mapToDouble(e -> e.getValue().get(firstToMax - 1)).max().getAsDouble();

				// if (maxOfChosenFirstOverAllMUS.compareTo(Double.valueOf(0)) > 0)
				if (Constant.equalsForFloat(maxOfChosenFirstOverAllMUS, Double.valueOf(0)))
				{
					continue;
				}
				else
				{
					chosenFirstToMax = firstToMax;
					break;
				}
			}

			// if all First3, 2, 1 are all zeros for all MU, then set chosenFirstToMax = first preference (say 3)
			if (chosenFirstToMax < -1)
			{
				chosenFirstToMax = firstsToMaxInOrder[0];
				// maxOfChosenFirstOverAllMUS = 0;
				System.out.println("Warning: all First3, 2, 1 are all zeros for all MU, chosenFirstToMax="
						+ chosenFirstToMax + " maxOfChosenFirstOverAllMUS=" + maxOfChosenFirstOverAllMUS);
			}

			// find MUs having Max first 3 found just above.
			Set<Integer> musHavingTheMax = new LinkedHashSet<>();
			// valsForAllMUs.entrySet().stream()
			// .filter(e -> e.getValue().get(chosenFirstToMax - 1).equals(maxOfChosenFirstOverAllMUS))
			// .map(e -> e.getKey()).collect(Collectors.toSet());

			for (Entry<Integer, List<Double>> muEntry : valsForAllMUs.entrySet())
			{
				Double valueForChosenFirstForThisMU = muEntry.getValue().get(chosenFirstToMax - 1);
				// (valueForChosenFirstForThisMU - maxOfChosenFirstOverAllMUS) < Constant.epsilonForFloatZero)
				if (Constant.equalsForFloat(valueForChosenFirstForThisMU, maxOfChosenFirstOverAllMUS))
				{
					musHavingTheMax.add(muEntry.getKey());
				}
			}

			// find min MU having max first 3
			int minMUHavingMaxChosenFirstForThisUser = musHavingTheMax.stream().mapToInt(e -> e).min().getAsInt();

			firstsForHighChosenFirstResultForEachUserOverMUs.put(userIndex,
					new Triple<Integer, List<Double>, Integer>(minMUHavingMaxChosenFirstForThisUser,
							valsForAllMUs.get(minMUHavingMaxChosenFirstForThisUser), chosenFirstToMax));
			// the corresponding first1, first2 and first3 for minMuHavingMaxFirstChosen
		}
		return firstsForHighChosenFirstResultForEachUserOverMUs;
	}

	/**
	 * 
	 * @param userMUKeyVals
	 *            { userIndex , {MU, {list of first1,2,3 for that user and mu}}}
	 * @param userIdentifierChosenMuMap
	 * @param userSetLabel
	 * @return { userIndex,{chosen mu first3 for this mu and user}}
	 */
	private static Map<Integer, Pair<Integer, List<Double>>> getValsForCHosenMUForEachUser(
			Map<Integer, Map<Integer, List<Double>>> userMUKeyVals, Map<String, Integer> userIdentifierChosenMuMap,
			String userSetLabel, double[] muArray, String pathToRead)
	{
		// userIndex, chosen mu , first3 for this mu and user
		Map<Integer, Pair<Integer, List<Double>>> res = new LinkedHashMap<>();

		if (muArray.length == 1)// if only one allowed MU, for example for PureAKOM, MU is order -1.
		{
			System.out.println("Warning for pathToRead=" + pathToRead + " only one MU");
		}

		for (Entry<Integer, Map<Integer, List<Double>>> userVals : userMUKeyVals.entrySet())
		{
			int userIndex = userVals.getKey();
			String userIdentifier = userSetLabel + userIndex; // e.g. SetD68

			// MU, list of first 1,2,3 for that mu for current user
			Map<Integer, List<Double>> valsForAllMUs = userVals.getValue();

			Integer chosenMUForThisUser = userIdentifierChosenMuMap.get(userIdentifier);
			// expecting only one MU for PureAKOM
			if ((muArray.length == 1) && pathToRead.contains("PureAKOMOrder"))
			{
				System.out.println("Alert! PureAKOMOrder hence mu=" + Arrays.toString(muArray));
				chosenMUForThisUser = (int) muArray[0];
			}
			if ((muArray.length == 1) && pathToRead.contains("RNN1"))
			{
				System.out.println("Alert! RNN1 hence mu=" + Arrays.toString(muArray));
				chosenMUForThisUser = (int) muArray[0];
			}

			List<Double> firstValsForChosenMU = valsForAllMUs.get(chosenMUForThisUser);

			if (chosenMUForThisUser == null)
			{
				PopUps.showError("Error: Chosen MU not found for user:" + userIdentifier);
			}
			if (firstValsForChosenMU == null)
			{
				PopUps.showError("Error: firstValsForChosenMU is null for userIdentifier:" + userIdentifier
						+ " \npathToRead:" + pathToRead);
			}

			res.put(userIndex, new Pair<Integer, List<Double>>(chosenMUForThisUser, firstValsForChosenMU));

		}
		return res;
	}

	/**
	 * convert ( MU , (list for each user, (list of first1,2,3 for that user and mu))) to (userIndex , (MU, (list of
	 * first1,2,3 for that user and mu))
	 * 
	 * @param muArray
	 * @param muKeyAllValsMap
	 * @return {userIndex ,{MU, {list of first1,2,3 for that user and mu}}}
	 */
	public static Map<Integer, Map<Integer, List<Double>>> transformToUserWise(double[] muArray,
			Map<Integer, List<List<Double>>> muKeyAllValsMap)
	{
		System.out.println("Inside transformToUserWise: muArray.length= " + muArray.length + " muKeyAllValsMap.size()="
				+ muKeyAllValsMap.size());

		// userIndex , <MU, <list of first1,2,3 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = new LinkedHashMap<>();

		// assuming all MU results have same number of users which should be true;
		int numOfUsers = muKeyAllValsMap.get((int) muArray[0]).size();
		System.out.println("Num of users = " + numOfUsers);

		for (int u = 0; u < numOfUsers; u++)// for each user
		{
			Map<Integer, List<Double>> valsForThisUserAllMUs = new LinkedHashMap<>();
			for (double muD : muArray)
			{
				List<List<Double>> muResForAllUsersThisMU = muKeyAllValsMap.get((int) muD);
				System.out.println("Num of users for muD=" + muD + " =" + muResForAllUsersThisMU.size());

				valsForThisUserAllMUs.put((int) muD, muResForAllUsersThisMU.get(u));
			}
			userMUKeyVals.put(u, valsForThisUserAllMUs);
		}
		return userMUKeyVals;
	}

	//// Start of 19 Nov 2018
	/**
	 * NOT USED
	 * <P>
	 * 
	 * Fork of org.activity.evaluation.ResultsDistributionEvaluation.getResult19July2018_MRR_PRF(String, String, String,
	 * double[], String, String, int, String) for MRR, avg percision , recall, fmeasure.
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNameToRead
	 *            MRR, AvgPrecision, AvgRecall, AvgFMeasure at top 5,4,3,2,1
	 * @param host
	 * @param firstToMax
	 * @param dimensionPhrase
	 * @param statFileHasColHeader
	 * @param firstToMaxInOrder
	 * @param statFileHasRowHeader
	 * @param valColHeader
	 *            "top5,top4,top3,top2,top1"
	 * @return
	 */
	public static Map<Integer, Map<Integer, List<Double>>> getResult19Nov2018_MRR_PRF(String pathToWrite,
			String resultsLabel, String pathToRead, double[] muArray, String statFileNameToRead, String host,
			int firstToMax, String dimensionPhrase, boolean statFileHasColHeader, int[] firstToMaxInOrder,
			boolean statFileHasRowHeader, String valColHeader)
	{
		// MU , <list for each user, <list of recall@5,recall@4,...recall@1 for that user and mu>>
		Map<Integer, List<List<Double>>> muKeyAllValsMap = new LinkedHashMap<>();
		// Convert to user wise result
		// userIndex , <MU, <list of recall@5,recall@4,...recall@1 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = null;

		String passwd = host.contains("local") ? ServerUtils.getPassWordForHost(host) : "";
		String user = host.contains("local") ? ServerUtils.getUserForHost(host) : "";

		String userSetLabel = "";

		try
		{// if PureAKOM, then mu is order of AKOM -1
			if (pathToRead.contains("PureAKOMOrder"))
			{
				String[] splitted = pathToRead.split("Order");
				String[] splitted2 = splitted[1].split("/");
				double muForOrder = Double.valueOf(splitted2[0]);
				muArray = new double[] { muForOrder - 1 };
			}
			if (pathToRead.contains("Set"))
			{
				int i = pathToRead.indexOf("Set");
				userSetLabel = pathToRead.substring(i, i + 4);
			}

			for (double muD : muArray)
			{
				int mu = (int) muD;
				List<List<Double>> readResFromFile = null;
				String statFileForThisMU = pathToRead + statFileNameToRead + mu + dimensionPhrase + ".csv";

				if (host.contains("local"))
				{// each row corresponds to a user
					readResFromFile = ReadingFromFile.nColumnReaderDouble(statFileForThisMU, ",", statFileHasColHeader,
							statFileHasRowHeader);
				}
				else
				{
					Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port,
							statFileForThisMU, user, passwd);
					readResFromFile = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",",
							statFileHasColHeader, statFileHasRowHeader);
				}
				// System.out.println("mu= " + mu + " res=" + res);
				muKeyAllValsMap.put(mu, readResFromFile);
				// inputAndSession.getSecond().disconnect();
			}

			// Convert to user wise result
			// userIndex , <MU, <list of first1,2,3 for that user and mu>>
			userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

			writeUserMuKeyVals(userMUKeyVals,
					pathToWrite + resultsLabel + "_" + statFileNameToRead + "userMUKeyVals" + dimensionPhrase + ".csv",
					userSetLabel, valColHeader);

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Pair<Integer, List<Double>>> firstsForMinMUWithMaxFxForEachUser = getValsForMinMUHavingMaxF3(
					firstToMax, userMUKeyVals);
			StringBuilder sb = new StringBuilder("userIndex,minMuWithMaxFirst3," + valColHeader + "\n");
			// IntStream.rangeClosed(1, 3);
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForMinMUWithMaxFxForEachUser.entrySet())
			{
				// sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
				// + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
				// + r.getValue().getSecond().get(2) + "\n");
				// modified on 19 July 2018 so that it also works only first1
				sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
				for (double firstVal : r.getValue().getSecond())
				{
					sb.append("," + firstVal);
				}
				sb.append("\n");
			}
			WToFile.writeToNewFile(sb.toString(), pathToWrite + resultsLabel + "_" + statFileNameToRead
					+ "MinMUWithMaxFirst" + firstToMax + dimensionPhrase + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForMinMUWithMaxFx0AwareForEachUser = getValsForMinMUHavingMaxF3ZeroAware(
					firstToMaxInOrder, userMUKeyVals);
			StringBuilder sb2 = new StringBuilder("userIndex,minMuWithMaxFirst3," + valColHeader + ",ChosenFirst\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Triple<Integer, List<Double>, Integer>> r : firstsForMinMUWithMaxFx0AwareForEachUser
					.entrySet())
			{
				// sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
				// + r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
				// + r.getValue().getSecond().get(2) + "," + r.getValue().getThird() + "\n");
				sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst());
				for (double firstVal : r.getValue().getSecond())
				{
					sb2.append("," + firstVal);
				}
				sb2.append("," + r.getValue().getThird() + "\n");
			}
			WToFile.writeToNewFile(sb2.toString(), pathToWrite + resultsLabel + "_" + statFileNameToRead
					+ "MinMUWithMaxFirst0Aware" + dimensionPhrase + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			WToFile.writeToNewFile(host + ":" + pathToRead, pathToWrite + "ReadMe/" + resultsLabel + "_"
					+ statFileNameToRead + "ReadMe" + dimensionPhrase + ".txt");
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			WToFile.appendLineToFileAbs(PopUps.getCurrentStackTracedWarningMsg("\n\nException in getResult()\n"),
					pathToWrite + "ExceptionsEncountered" + dimensionPhrase + ".csv");
			System.exit(-1);
			return null;
		}
		return (userMUKeyVals);

	}
	/// End of 19 Nov 2018

}
