package org.activity.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums.SummaryStat;
import org.activity.constants.PathConstants;
import org.activity.constants.VerbosityConstants;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.UtilityBelt;

/**
 * (note: In earlier version (before 14 April 2015, this class was name as TestStats.java)
 * 
 * @author gunjan
 *
 */
public class EvaluationSeq
{
	static String commonPath;// =Constant.commonPath;
	static final int theKOriginal = 5;
	static final String[] timeCategories = { "All" };// }, "Morning", "Afternoon", "Evening" };
	static String groupsOf100UsersLabels[] = DomainConstants.gowallaUserGroupsLabelsFixed;// { "1", "101", "201",
	// "301", "401", "501",
	// "601", "701", "801","901" };

	// List of filenames from different user groups (i.e., 100 users group) which need to be concenated to get results
	// for all (500) users in single file
	// for each individual seq index
	// outer list for each MU, inner list for each user group
	ArrayList<ArrayList<String>> listOfNumAgreementsFiles, listOfPerAgreementsFiles, listOfNumAgreementsFilesL1,
			listOfPerAgreementsFilesL1;

	// for top k indices in a sequence
	ArrayList<ArrayList<String>> listOfNumTopKAgreementsFiles, listOfPerTopKAgreementsFiles,
			listOfNumTopKAgreementsFilesL1, listOfPerTopKAgreementsFilesL1;

	ArrayList<ArrayList<String>> listOfStep0MeanReciprocalRankFiles, listOfStep0AvgPrecisionFiles,
			listOfStep0AvgRecallFiles, listOfStep0AvgFMeasureFiles;// listOfReciprocalRankFiles,

	boolean evaluatePostFiltering;// = false;
	boolean evaluateSeqPrediction;

	/**
	 * FOR NO MUs, useful for baseline
	 * 
	 * @param seqLength
	 * @param outputCoreResultsPath
	 * @param matchingUnitAsPastCount
	 */
	public EvaluationSeq(int seqLength, String outputCoreResultsPath)
	{
		// commonPath = "./dataWritten/";
		System.out.println("Inside EvaluationSeq for NO MU");
		System.out.println("groupsOf100UsersLabels= " + Arrays.asList(groupsOf100UsersLabels).toString());
		WToFile.writeToNewFile(Arrays.asList(groupsOf100UsersLabels).toString(),
				Constant.getOutputCoreResultsPath() + "UserGroupsOrder.csv");

		PathConstants.intialise(Constant.For9kUsers);
		intialiseListOfFilenamesNoMU();
		int totalNumOfUsersComputedFor = 0;
		try
		{
			PrintStream consoleLogStream = WToFile.redirectConsoleOutput(outputCoreResultsPath + "EvaluationLog.txt");

			for (String groupsOf100UsersLabel : groupsOf100UsersLabels)
			{
				commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/";
				System.out.println("For groupsOf100UsersLabel: " + groupsOf100UsersLabel);
				// Constant.initialise(commonPath, Constant.getDatabaseName(),
				// PathConstants.pathToSerialisedCatIDsHierDist, PathConstants.pathToSerialisedCatIDNameDictionary,
				// PathConstants.pathToSerialisedLocationObjects);

				Constant.initialise(commonPath, Constant.getDatabaseName(),
						PathConstants.pathToSerialisedCatIDsHierDist, PathConstants.pathToSerialisedCatIDNameDictionary,
						PathConstants.pathToSerialisedLocationObjects, PathConstants.pathToSerialisedUserObjects,
						PathConstants.pathToSerialisedGowallaLocZoneIdMap);

				// for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
				// {
				// double mu = matchingUnitAsPastCount[muIndex];
				commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/";
				Constant.setCommonPath(commonPath);
				System.out.println("\nCommon path=" + Constant.getCommonPath());
				// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(commonPath + "EvaluationLog.txt");

				// PrintStream consoleLogStream =
				// WritingToFile.redirectConsoleOutput(outputCoreResultsPath + "EvaluationLog.txt");

				int numOfUsersComputerFor = doEvaluationSeq(seqLength, commonPath, commonPath, commonPath, true, "");
				totalNumOfUsersComputedFor += numOfUsersComputerFor;
				System.out.println("numOfUsersComputerFor = " + numOfUsersComputerFor);
				System.out.println("totalNumOfUsersComputedFor = " + totalNumOfUsersComputedFor);
				// PopUps.showMessage("FINISHED EVAL FOR mu = " + mu + " USERGROUP=" + groupsOf100UsersLabel);

				// create lists of filenames of results for different MUs which need to be concatenated
				String algoLabel = "Algo";
				int muIndex = 0;

				for (String timeCategory : timeCategories)
				{
					listOfNumAgreementsFiles.get(muIndex)
							.add(commonPath + algoLabel + timeCategory + "NumDirectAgreements.csv");
					listOfPerAgreementsFiles.get(muIndex)
							.add(commonPath + algoLabel + timeCategory + "PercentageDirectAgreements.csv");
					listOfNumAgreementsFilesL1.get(muIndex)
							.add(commonPath + algoLabel + "L1" + timeCategory + "NumDirectAgreements.csv");
					listOfPerAgreementsFilesL1.get(muIndex)
							.add(commonPath + algoLabel + "L1" + timeCategory + "PercentageDirectAgreements.csv");

					listOfNumTopKAgreementsFiles.get(muIndex)
							.add(commonPath + algoLabel + timeCategory + "NumDirectTopKAgreements.csv");
					listOfPerTopKAgreementsFiles.get(muIndex)
							.add(commonPath + algoLabel + timeCategory + "PercentageDirectTopKAgreements.csv");
					listOfNumTopKAgreementsFilesL1.get(muIndex)
							.add(commonPath + algoLabel + "L1" + timeCategory + "NumDirectTopKAgreements.csv");
					listOfPerTopKAgreementsFilesL1.get(muIndex)
							.add(commonPath + algoLabel + "L1" + timeCategory + "PercentageDirectTopKAgreements.csv");
				}
				// consoleLogStream.close();
				// }

			}

			double matchingUnitAsPastCount[] = { 0 };
			// PopUps.showMessage("BREAKING");
			ArrayList<String> listOfWrittenFiles = concatenateFiles(outputCoreResultsPath, matchingUnitAsPastCount,
					listOfNumAgreementsFiles, listOfPerAgreementsFiles, listOfNumAgreementsFilesL1,
					listOfPerAgreementsFilesL1, "");

			ArrayList<String> listOfTopKWrittenFiles = concatenateTopKFiles(outputCoreResultsPath,
					matchingUnitAsPastCount, listOfNumTopKAgreementsFiles, listOfPerTopKAgreementsFiles,
					listOfNumTopKAgreementsFilesL1, listOfPerTopKAgreementsFilesL1, "");

			String[] fileNamePhrases = { "AllNumDirectAgreements_", "AllPerDirectAgreements_",
					"AllNumDirectAgreementsL1_", "AllPerDirectAgreementsL1_" };
			String[] fileNamePhrasesTopK = { "AllNumDirectTopKAgreements_", "AllPerDirectTopKAgreements_",
					"AllNumDirectTopKAgreementsL1_", "AllPerDirectTopKAgreementsL1_" };

			SummaryStat[] summaryStats = { SummaryStat.Mean, SummaryStat.Median };

			summariseResults(seqLength, outputCoreResultsPath, matchingUnitAsPastCount, fileNamePhrases, summaryStats,
					"SummaryLog", "");
			summariseResults(seqLength, outputCoreResultsPath, matchingUnitAsPastCount, fileNamePhrasesTopK,
					summaryStats, "SummaryTopKLog", "");
			// consoleLogStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting EvaluationSeq for NO MU");
	}

	/**
	 * 
	 * @param seqLength
	 * @param outputCoreResultsPath
	 * @param matchingUnitAsPastCount
	 * @param dimensionPhrase
	 *            only if necessary, not essential for primary dimension, e.g. SecDim for secondary dimension
	 *            <p>
	 *            used as of 18 July 2018
	 * @param evaluatePostFiltering
	 * @param evaluatedSequencePrediction
	 */
	public EvaluationSeq(int seqLength, String outputCoreResultsPath, double[] matchingUnitAsPastCount,
			String dimensionPhrase, boolean evaluatePostFiltering, boolean evaluatedSequencePrediction)// sdsd
	{
		this.evaluatePostFiltering = evaluatePostFiltering;
		this.evaluateSeqPrediction = evaluatedSequencePrediction;

		// commonPath = "./dataWritten/";
		PopUps.showMessage("Starting EvaluationSeq for dimensionPhrase = " + dimensionPhrase);
		if (Constant.useRandomlySampled100Users)
		{
			groupsOf100UsersLabels = new String[] { DomainConstants.gowalla100RandomUsersLabel };
		}
		if (Constant.runForAllUsersAtOnce)
		{
			groupsOf100UsersLabels = new String[] { "All" };
		}

		System.out.println("outputCoreResultsPath= " + Constant.getOutputCoreResultsPath());
		System.out.println("groupsOf100UsersLabels= " + Arrays.asList(groupsOf100UsersLabels).toString());
		WToFile.writeToNewFile(Arrays.asList(groupsOf100UsersLabels).toString(),
				Constant.getOutputCoreResultsPath() + "UserGroupsOrder.csv");

		PathConstants.intialise(Constant.For9kUsers);
		intialiseListOfFilenames(matchingUnitAsPastCount);
		int totalNumOfUsersComputedFor = 0;
		try
		{
			for (String groupsOf100UsersLabel : groupsOf100UsersLabels)
			{
				commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/";
				System.out.println("For groupsOf100UsersLabel: " + groupsOf100UsersLabel);
				Constant.initialise(commonPath, Constant.getDatabaseName(),
						PathConstants.pathToSerialisedCatIDsHierDist, PathConstants.pathToSerialisedCatIDNameDictionary,
						PathConstants.pathToSerialisedLocationObjects, PathConstants.pathToSerialisedUserObjects,
						PathConstants.pathToSerialisedGowallaLocZoneIdMap);

				for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
				{
					double mu = matchingUnitAsPastCount[muIndex];
					commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/MatchingUnit" + String.valueOf(mu)
							+ "/";
					Constant.setCommonPath(commonPath);
					System.out.println("For mu: " + mu + "\nCommon path=" + Constant.getCommonPath());

					PrintStream consoleLogStream = WToFile.redirectConsoleOutput(commonPath + "EvaluationLog.txt");

					int numOfUsersComputedFor = doEvaluationSeq(seqLength, commonPath, commonPath, commonPath, true,
							dimensionPhrase);
					totalNumOfUsersComputedFor += numOfUsersComputedFor;
					System.out.println("numOfUsersComputerFor = " + numOfUsersComputedFor);
					System.out.println("totalNumOfUsersComputedFor = " + totalNumOfUsersComputedFor);

					// PopUps.showMessage("FINISHED EVAL FOR mu = " + mu + " USERGROUP=" + groupsOf100UsersLabel);

					// create lists of filenames of results for different MUs which need to be concatenated
					String algoLabel = "Algo";
					String timeCategory = "All";
					// for (String timeCategory : timeCategories){
					listOfNumAgreementsFiles.get(muIndex).add(
							commonPath + algoLabel + timeCategory + "NumDirectAgreements" + dimensionPhrase + ".csv");
					listOfPerAgreementsFiles.get(muIndex).add(commonPath + algoLabel + timeCategory
							+ "PercentageDirectAgreements" + dimensionPhrase + ".csv");
					listOfNumAgreementsFilesL1.get(muIndex).add(commonPath + algoLabel + "L1" + timeCategory
							+ "NumDirectAgreements" + dimensionPhrase + ".csv");
					listOfPerAgreementsFilesL1.get(muIndex).add(commonPath + algoLabel + "L1" + timeCategory
							+ "PercentageDirectAgreements" + dimensionPhrase + ".csv");

					listOfNumTopKAgreementsFiles.get(muIndex).add(commonPath + algoLabel + timeCategory
							+ "NumDirectTopKAgreements" + dimensionPhrase + ".csv");
					listOfPerTopKAgreementsFiles.get(muIndex).add(commonPath + algoLabel + timeCategory
							+ "PercentageDirectTopKAgreements" + dimensionPhrase + ".csv");
					listOfNumTopKAgreementsFilesL1.get(muIndex).add(commonPath + algoLabel + "L1" + timeCategory
							+ "NumDirectTopKAgreements" + dimensionPhrase + ".csv");
					listOfPerTopKAgreementsFilesL1.get(muIndex).add(commonPath + algoLabel + "L1" + timeCategory
							+ "PercentageDirectTopKAgreements" + dimensionPhrase + ".csv");

					// start of added on 19 July 2018
					listOfStep0MeanReciprocalRankFiles.get(muIndex).add(commonPath + algoLabel + "Step0" + timeCategory
							+ "MeanReciprocalRank" + dimensionPhrase + ".csv");
					listOfStep0AvgPrecisionFiles.get(muIndex).add(commonPath + algoLabel + "Step0" + timeCategory
							+ "AvgPrecision" + dimensionPhrase + ".csv");
					listOfStep0AvgRecallFiles.get(muIndex).add(
							commonPath + algoLabel + "Step0" + timeCategory + "AvgRecall" + dimensionPhrase + ".csv");
					listOfStep0AvgFMeasureFiles.get(muIndex).add(
							commonPath + algoLabel + "Step0" + timeCategory + "AvgFMeasure" + dimensionPhrase + ".csv");
					// end of added on 19 July 2018

					// }
					consoleLogStream.close();
				} // end of loop over MUs

			} // end of loop over groups of 100 users

			WToFile.appendLineToFileAbs("Now will concatenate files from different groups of 100 users: ",
					Constant.getOutputCoreResultsPath() + "Debug1.txt\n");
			System.out.println("Debug: listOfNumTopKAgreementsFiles = \n" + listOfNumTopKAgreementsFiles.toString());
			System.out.println("Now will concatenate files: ");
			// PopUps.showMessage("BREAKING");
			ArrayList<String> listOfWrittenFiles = concatenateFiles(outputCoreResultsPath, matchingUnitAsPastCount,
					listOfNumAgreementsFiles, listOfPerAgreementsFiles, listOfNumAgreementsFilesL1,
					listOfPerAgreementsFilesL1, dimensionPhrase);

			ArrayList<String> listOfTopKWrittenFiles = concatenateTopKFiles(outputCoreResultsPath,
					matchingUnitAsPastCount, listOfNumTopKAgreementsFiles, listOfPerTopKAgreementsFiles,
					listOfNumTopKAgreementsFilesL1, listOfPerTopKAgreementsFilesL1, dimensionPhrase);

			ArrayList<String> listOfMRR_P_R_F_Files = concatenateMRRPrecisonRecallFMeasureFiles(outputCoreResultsPath,
					matchingUnitAsPastCount, listOfStep0MeanReciprocalRankFiles, listOfStep0AvgPrecisionFiles,
					listOfStep0AvgRecallFiles, listOfStep0AvgFMeasureFiles, dimensionPhrase);
			// end of file condatenation from different user groups

			String[] fileNamePhrases = { "AllNumDirectAgreements_", "AllPerDirectAgreements_",
					"AllNumDirectAgreementsL1_", "AllPerDirectAgreementsL1_" };
			String[] fileNamePhrasesTopK = { "AllNumDirectTopKAgreements_", "AllPerDirectTopKAgreements_",
					"AllNumDirectTopKAgreementsL1_", "AllPerDirectTopKAgreementsL1_" };

			WToFile.appendLineToFileAbs("Now will write summary stats: ",
					Constant.getOutputCoreResultsPath() + "Debug1.txt\n");
			System.out.println("Now will write summary stats: ");
			SummaryStat[] summaryStats = { SummaryStat.Mean, SummaryStat.Median };

			summariseResults(seqLength, outputCoreResultsPath, matchingUnitAsPastCount, fileNamePhrases, summaryStats,
					"SummaryLog", dimensionPhrase);
			summariseResults(seqLength, outputCoreResultsPath, matchingUnitAsPastCount, fileNamePhrasesTopK,
					summaryStats, "SummaryTopKLog", dimensionPhrase);
			WToFile.appendLineToFileAbs("Finishing: ",
					Constant.getOutputCoreResultsPath() + "Debug1" + dimensionPhrase + ".txt\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param seqLength
	 * @param outputCoreResultsPath
	 * @param matchingUnitAsPastCount
	 */
	public EvaluationSeq(int seqLength, String outputCoreResultsPath, double[] matchingUnitAsPastCount,
			int thresholdVal)
	{
		// commonPath = "./dataWritten/";

		intialiseListOfFilenames(matchingUnitAsPastCount);
		int totalNumOfUsersComputedFor = 0;
		try
		{
			for (String groupsOf100UsersLabel : groupsOf100UsersLabels)
			{
				commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/" + thresholdVal + "/";
				System.out.println("For groupsOf100UsersLabel: " + groupsOf100UsersLabel);
				Constant.initialise(commonPath, Constant.getDatabaseName(),
						PathConstants.pathToSerialisedCatIDsHierDist, PathConstants.pathToSerialisedCatIDNameDictionary,
						PathConstants.pathToSerialisedLocationObjects, PathConstants.pathToSerialisedUserObjects,
						PathConstants.pathToSerialisedGowallaLocZoneIdMap);

				for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
				{
					double mu = matchingUnitAsPastCount[muIndex];
					commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/MatchingUnit" + String.valueOf(mu)
							+ "/";
					Constant.setCommonPath(commonPath);
					System.out.println("For mu: " + mu + "\nCommon path=" + Constant.getCommonPath());

					PrintStream consoleLogStream = WToFile.redirectConsoleOutput(commonPath + "EvaluationLog.txt");

					int numOfUsersComputerFor = doEvaluationSeq(seqLength, commonPath, commonPath, commonPath, true,
							"");
					totalNumOfUsersComputedFor += numOfUsersComputerFor;
					System.out.println("numOfUsersComputerFor = " + numOfUsersComputerFor);
					System.out.println("totalNumOfUsersComputedFor = " + totalNumOfUsersComputedFor);

					// PopUps.showMessage("FINISHED EVAL FOR mu = " + mu + " USERGROUP=" + groupsOf100UsersLabel);

					// create lists of filenames of results for different MUs which need to be concatenated
					String algoLabel = "Algo";

					for (String timeCategory : timeCategories)
					{
						listOfNumAgreementsFiles.get(muIndex)
								.add(commonPath + algoLabel + timeCategory + "NumDirectAgreements.csv");
						listOfPerAgreementsFiles.get(muIndex)
								.add(commonPath + algoLabel + timeCategory + "PercentageDirectAgreements.csv");
						listOfNumAgreementsFilesL1.get(muIndex)
								.add(commonPath + algoLabel + "L1" + timeCategory + "NumDirectAgreements.csv");
						listOfPerAgreementsFilesL1.get(muIndex)
								.add(commonPath + algoLabel + "L1" + timeCategory + "PercentageDirectAgreements.csv");

						listOfNumTopKAgreementsFiles.get(muIndex)
								.add(commonPath + algoLabel + timeCategory + "NumDirectTopKAgreements.csv");
						listOfPerTopKAgreementsFiles.get(muIndex)
								.add(commonPath + algoLabel + timeCategory + "PercentageDirectTopKAgreements.csv");
						listOfNumTopKAgreementsFilesL1.get(muIndex)
								.add(commonPath + algoLabel + "L1" + timeCategory + "NumDirectTopKAgreements.csv");
						listOfPerTopKAgreementsFilesL1.get(muIndex).add(
								commonPath + algoLabel + "L1" + timeCategory + "PercentageDirectTopKAgreements.csv");
					}
					consoleLogStream.close();
				}

			}

			// PopUps.showMessage("BREAKING");
			ArrayList<String> listOfWrittenFiles = concatenateFiles(outputCoreResultsPath, matchingUnitAsPastCount,
					listOfNumAgreementsFiles, listOfPerAgreementsFiles, listOfNumAgreementsFilesL1,
					listOfPerAgreementsFilesL1, "");

			ArrayList<String> listOfTopKWrittenFiles = concatenateTopKFiles(outputCoreResultsPath,
					matchingUnitAsPastCount, listOfNumTopKAgreementsFiles, listOfPerTopKAgreementsFiles,
					listOfNumTopKAgreementsFilesL1, listOfPerTopKAgreementsFilesL1, "");

			String[] fileNamePhrases = { "AllNumDirectAgreements_", "AllPerDirectAgreements_",
					"AllNumDirectAgreementsL1_", "AllPerDirectAgreementsL1_" };
			String[] fileNamePhrasesTopK = { "AllNumDirectTopKAgreements_", "AllPerDirectTopKAgreements_",
					"AllNumDirectTopKAgreementsL1_", "AllPerDirectTopKAgreementsL1_" };

			SummaryStat[] summaryStats = { SummaryStat.Mean, SummaryStat.Median };

			summariseResults(seqLength, outputCoreResultsPath, matchingUnitAsPastCount, fileNamePhrases, summaryStats,
					"SummaryLog", "");
			summariseResults(seqLength, outputCoreResultsPath, matchingUnitAsPastCount, fileNamePhrasesTopK,
					summaryStats, "SummaryTopKLog", "");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	////
	/**
	 * 
	 * @param seqLength
	 * @param outputCoreResultsPath
	 * @param matchingUnitAsPastCount
	 * @param globalThresholds
	 */
	public EvaluationSeq(int seqLength, String outputCoreResultsPath, double[] matchingUnitAsPastCount,
			int thresholdsArray[])
	{
		// commonPath = "./dataWritten/";
		PrintStream consoleLogStream = WToFile.redirectConsoleOutput(outputCoreResultsPath + "EvaluationLog.txt");
		int totalNumOfUsersComputedFor = 0;
		try
		{
			for (int thresholdValue : thresholdsArray)
			{
				// listOfNumAgreementsFiles.clear();
				// listOfPerAgreementsFiles.clear();
				// listOfNumAgreementsFilesL1.clear();
				// listOfPerAgreementsFilesL1.clear();
				//
				// listOfNumTopKAgreementsFiles.clear();
				// listOfPerTopKAgreementsFiles.clear();
				//
				// listOfNumTopKAgreementsFilesL1.clear();
				// listOfPerTopKAgreementsFilesL1.clear();
				System.out.println("intialiseListOfFilenames");
				intialiseListOfFilenames(matchingUnitAsPastCount);

				for (String groupsOf100UsersLabel : groupsOf100UsersLabels)
				{
					String pathT = outputCoreResultsPath + groupsOf100UsersLabel + "/" + thresholdValue;
					System.out.println("For groupsOf100UsersLabel: " + groupsOf100UsersLabel);

					Constant.initialise(pathT, Constant.getDatabaseName(), PathConstants.pathToSerialisedCatIDsHierDist,
							PathConstants.pathToSerialisedCatIDNameDictionary,
							PathConstants.pathToSerialisedLocationObjects, PathConstants.pathToSerialisedUserObjects,
							PathConstants.pathToSerialisedGowallaLocZoneIdMap);
					System.out.println("Constant.initialise done ---");
					// outputCoreResultsPath = commonPathT;

					for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
					{
						double mu = matchingUnitAsPastCount[muIndex];
						commonPath = pathT + "/MatchingUnit" + String.valueOf(mu) + "/";
						Constant.setCommonPath(commonPath);

						System.out.println("For mu: " + mu + "\nCommon path=" + Constant.getCommonPath());

						// PrintStream consoleLogStream = WritingToFile
						// .redirectConsoleOutput(commonPath + "EvaluationLog.txt");

						int numOfUsersComputerFor = doEvaluationSeq(seqLength, commonPath, commonPath, commonPath,
								false, "");
						totalNumOfUsersComputedFor += numOfUsersComputerFor;
						System.out.println("numOfUsersComputerFor = " + numOfUsersComputerFor);
						System.out.println("totalNumOfUsersComputedFor = " + totalNumOfUsersComputedFor);

						// PopUps.showMessage("FINISHED EVAL FOR mu = " + mu + " USERGROUP=" + groupsOf100UsersLabel);

						// create lists of filenames of results for different MUs which need to be concatenated
						String algoLabel = "Algo";

						for (String timeCategory : timeCategories)
						{
							listOfNumAgreementsFiles.get(muIndex)
									.add(commonPath + algoLabel + timeCategory + "NumDirectAgreements.csv");
							listOfPerAgreementsFiles.get(muIndex)
									.add(commonPath + algoLabel + timeCategory + "PercentageDirectAgreements.csv");
							listOfNumAgreementsFilesL1.get(muIndex)
									.add(commonPath + algoLabel + "L1" + timeCategory + "NumDirectAgreements.csv");
							listOfPerAgreementsFilesL1.get(muIndex).add(
									commonPath + algoLabel + "L1" + timeCategory + "PercentageDirectAgreements.csv");

							listOfNumTopKAgreementsFiles.get(muIndex)
									.add(commonPath + algoLabel + timeCategory + "NumDirectTopKAgreements.csv");
							listOfPerTopKAgreementsFiles.get(muIndex)
									.add(commonPath + algoLabel + timeCategory + "PercentageDirectTopKAgreements.csv");
							listOfNumTopKAgreementsFilesL1.get(muIndex)
									.add(commonPath + algoLabel + "L1" + timeCategory + "NumDirectTopKAgreements.csv");
							listOfPerTopKAgreementsFilesL1.get(muIndex).add(commonPath + algoLabel + "L1" + timeCategory
									+ "PercentageDirectTopKAgreements.csv");
						}
						// consoleLogStream.close();
					} // end of loop over MUs

					StringBuilder sb = new StringBuilder();
					sb.append("listOfNumAgreementsFiles= " + listOfNumAgreementsFiles + "\n");
					sb.append("listOfPerAgreementsFiles= " + listOfPerAgreementsFiles + "\n");
					sb.append("listOfNumAgreementsFilesL1= " + listOfNumAgreementsFilesL1 + "\n");
					sb.append("listOfPerAgreementsFilesL1= " + listOfPerAgreementsFilesL1 + "\n");
					sb.append("listOfNumTopKAgreementsFiles= " + listOfNumTopKAgreementsFiles + "\n");
					System.out.println(sb.toString());
					// PopUps.showMessage(sb.toString());

					ArrayList<String> listOfWrittenFiles = concatenateFiles(pathT, matchingUnitAsPastCount,
							listOfNumAgreementsFiles, listOfPerAgreementsFiles, listOfNumAgreementsFilesL1,
							listOfPerAgreementsFilesL1, "");

					ArrayList<String> listOfTopKWrittenFiles = concatenateTopKFiles(pathT, matchingUnitAsPastCount,
							listOfNumTopKAgreementsFiles, listOfPerTopKAgreementsFiles, listOfNumTopKAgreementsFilesL1,
							listOfPerTopKAgreementsFilesL1, "");

					String[] fileNamePhrases = { "AllNumDirectAgreements_", "AllPerDirectAgreements_",
							"AllNumDirectAgreementsL1_", "AllPerDirectAgreementsL1_" };
					String[] fileNamePhrasesTopK = { "AllNumDirectTopKAgreements_", "AllPerDirectTopKAgreements_",
							"AllNumDirectTopKAgreementsL1_", "AllPerDirectTopKAgreementsL1_" };

					SummaryStat[] summaryStats = { SummaryStat.Mean, SummaryStat.Median };

					summariseResults(seqLength, pathT, matchingUnitAsPastCount, fileNamePhrases, summaryStats,
							"SummaryLog", "");
					summariseResults(seqLength, pathT, matchingUnitAsPastCount, fileNamePhrasesTopK, summaryStats,
							"SummaryTopKLog", "");
				} // end of loop over thresholds
			} // end of loop over users
			consoleLogStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	////

	/**
	 * 
	 * @param matchingUnitAsPastCount
	 */
	private void intialiseListOfFilenames(double[] matchingUnitAsPastCount)
	{
		// Initialise list of list of filenames
		// we need to concated results for different user groups, 1-100,102-200, and so on
		listOfNumAgreementsFiles = new ArrayList<>();
		listOfPerAgreementsFiles = new ArrayList<>();
		listOfNumAgreementsFilesL1 = new ArrayList<>();
		listOfPerAgreementsFilesL1 = new ArrayList<>();

		listOfNumTopKAgreementsFiles = new ArrayList<>();
		listOfPerTopKAgreementsFiles = new ArrayList<>();
		listOfNumTopKAgreementsFilesL1 = new ArrayList<>();
		listOfPerTopKAgreementsFilesL1 = new ArrayList<>();

		listOfStep0MeanReciprocalRankFiles = new ArrayList<>();
		listOfStep0AvgPrecisionFiles = new ArrayList<>();
		listOfStep0AvgRecallFiles = new ArrayList<>();
		listOfStep0AvgFMeasureFiles = new ArrayList<>();

		// we concatenate results for each mu over all users (groups)
		for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
		{
			listOfNumAgreementsFiles.add(muIndex, new ArrayList<String>());
			listOfPerAgreementsFiles.add(muIndex, new ArrayList<String>());
			listOfNumAgreementsFilesL1.add(muIndex, new ArrayList<String>());
			listOfPerAgreementsFilesL1.add(muIndex, new ArrayList<String>());

			listOfNumTopKAgreementsFiles.add(muIndex, new ArrayList<String>());
			listOfPerTopKAgreementsFiles.add(muIndex, new ArrayList<String>());
			listOfNumTopKAgreementsFilesL1.add(muIndex, new ArrayList<String>());
			listOfPerTopKAgreementsFilesL1.add(muIndex, new ArrayList<String>());

			listOfStep0MeanReciprocalRankFiles.add(muIndex, new ArrayList<String>());
			listOfStep0AvgPrecisionFiles.add(muIndex, new ArrayList<String>());
			listOfStep0AvgRecallFiles.add(muIndex, new ArrayList<String>());
			listOfStep0AvgFMeasureFiles.add(muIndex, new ArrayList<String>());
		}
	}

	/**
	 * 
	 * @param matchingUnitAsPastCount
	 */
	private void intialiseListOfFilenamesNoMU()
	{
		// Initialise list of list of filenames
		// we need to concated results for different user groups, 1-100,102-200, and so on
		listOfNumAgreementsFiles = new ArrayList<>();
		listOfPerAgreementsFiles = new ArrayList<>();
		listOfNumAgreementsFilesL1 = new ArrayList<>();
		listOfPerAgreementsFilesL1 = new ArrayList<>();

		listOfNumTopKAgreementsFiles = new ArrayList<>();
		listOfPerTopKAgreementsFiles = new ArrayList<>();
		listOfNumTopKAgreementsFilesL1 = new ArrayList<>();
		listOfPerTopKAgreementsFilesL1 = new ArrayList<>();

		// we concatenate results for each mu over all users (groups)
		int muIndex = 0;
		listOfNumAgreementsFiles.add(muIndex, new ArrayList<String>());
		listOfPerAgreementsFiles.add(muIndex, new ArrayList<String>());
		listOfNumAgreementsFilesL1.add(muIndex, new ArrayList<String>());
		listOfPerAgreementsFilesL1.add(muIndex, new ArrayList<String>());

		listOfNumTopKAgreementsFiles.add(muIndex, new ArrayList<String>());
		listOfPerTopKAgreementsFiles.add(muIndex, new ArrayList<String>());
		listOfNumTopKAgreementsFilesL1.add(muIndex, new ArrayList<String>());
		listOfPerTopKAgreementsFilesL1.add(muIndex, new ArrayList<String>());
	}

	/**
	 * 
	 * @param seqLength
	 * @param pathToRead
	 * @param matchingUnitAsPastCount
	 * @param fileNamePhraseToRead
	 * @param stat
	 * @return
	 */
	private static ArrayList<ArrayList<Double>> getSummaryPerMUPerSeqIndex(int seqLength, String pathToRead,
			double[] matchingUnitAsPastCount, String fileNamePhraseToRead, SummaryStat stat, String dimensionPhrase)
	{
		// outerlist is for mu, inner list for seq index
		ArrayList<ArrayList<Double>> summaryPerMUPerSeqIndex = new ArrayList<ArrayList<Double>>();

		for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
		{
			int mu = (int) matchingUnitAsPastCount[muIndex];

			ArrayList<Double> summaryStatsPerIndex = StatsUtils.getColumnSummaryStatDouble(
					pathToRead + fileNamePhraseToRead + mu + dimensionPhrase + ".csv", seqLength, 4, stat);

			summaryPerMUPerSeqIndex.add(summaryStatsPerIndex);
		}

		return summaryPerMUPerSeqIndex;
		// WritingToFile.writeArrayListOfArrayList(finalSummaryStat, pathToWrite + fileNamePhraseToRead + stat + ".csv",
		// "", ",");
	}

	/**
	 * 
	 * @param seqLength
	 * @param pathToWrite
	 * @param matchingUnitAsPastCount
	 * @param fileNamePhrases
	 * @param summaryStats
	 */
	private static void summariseResults(int seqLength, String pathToWrite, double[] matchingUnitAsPastCount,
			String[] fileNamePhrases, SummaryStat[] summaryStats, String consoleLogFileName, String dimensionPhrase)
	{
		// String[] fileNamePhrases = { "", "" };
		// SummaryStat[] summaryStats = { SummaryStat.Mean, SummaryStat.Median };
		// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + consoleLogFileName);

		System.out.println("fileNamePhrases = " + fileNamePhrases.toString());
		System.out.println("summaryStats = " + summaryStats);
		// ArrayList<String> rowNames =
		ArrayList<String> rowNames = (ArrayList<String>) DoubleStream.of(matchingUnitAsPastCount).boxed()
				.map(v -> v.toString()).collect(Collectors.toList());
		// (ArrayList<String>) Stream.of(matchingUnitAsPastCount).map(v -> v.toString())
		// .collect(Collectors.toList());// .toArray(String[]::new);
		ArrayList<String> colNames = (ArrayList<String>) IntStream.range(0, seqLength).boxed().map(v -> v.toString())
				.collect(Collectors.toList());
		// String[] colNames = (String[]) IntStream.range(0, seqLength).boxed().map(v -> v.toString()).toArray();

		// PopUps.showMessage("rowNames: " + rowNames + " \ncolNames: " + colNames);

		for (String fileNamePhraseToRead : fileNamePhrases)
		{
			for (SummaryStat stat : summaryStats)
			{
				ArrayList<ArrayList<Double>> summaryPerMUPerSeqIndex = getSummaryPerMUPerSeqIndex(seqLength,
						pathToWrite, matchingUnitAsPastCount, fileNamePhraseToRead, stat, dimensionPhrase);

				WToFile.writeArrayListOfArrayList(summaryPerMUPerSeqIndex,
						pathToWrite + stat + fileNamePhraseToRead + dimensionPhrase + ".csv", ",", colNames, rowNames);
			}
		}
		// consoleLogStream.close();
	}

	/**
	 * 
	 * @param seqLength
	 * @param pathToWrite
	 * @param matchingUnitAsPastCount
	 */
	private static void summariseResultsV0(int seqLength, String pathToWrite, double[] matchingUnitAsPastCount)
	{
		{
			PrintStream consoleLogStream = WToFile.redirectConsoleOutput(pathToWrite + "Summarise.txt");

			StringBuilder sbQ1 = new StringBuilder();
			StringBuilder sbQ11 = new StringBuilder();
			StringBuilder sbQ2 = new StringBuilder();
			StringBuilder sbQ22 = new StringBuilder();
			StringBuilder sbQ3 = new StringBuilder();
			StringBuilder sbQ33 = new StringBuilder();
			StringBuilder sbQ4 = new StringBuilder();
			StringBuilder sbQ44 = new StringBuilder();

			for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
			{
				int mu = (int) matchingUnitAsPastCount[muIndex];

				Pair<ArrayList<Double>, ArrayList<Double>> res1 = getMeanMedian(
						pathToWrite + "AllNumDirectAgreements" + mu + ".csv", seqLength, 4);

				for (int k = 0; k < res1.getFirst().size(); k++)
				{
					sbQ1.append(res1.getFirst().get(k));
					sbQ11.append(res1.getSecond().get(k));

					if (k == res1.getFirst().size() - 1)
					{
						sbQ1.append("\n");
						sbQ11.append("\n");
					}
					else
					{
						sbQ1.append(",");
						sbQ11.append(",");
					}
				}

				Pair<ArrayList<Double>, ArrayList<Double>> res2 = getMeanMedian(
						pathToWrite + "AllPerDirectAgreements" + mu + ".csv", seqLength, 4);
				for (int k = 0; k < res2.getFirst().size(); k++)
				{
					sbQ2.append(res2.getFirst().get(k));
					sbQ22.append(res2.getSecond().get(k));

					if (k == res2.getFirst().size() - 1)
					{
						sbQ2.append("\n");
						sbQ22.append("\n");
					}
					else
					{
						sbQ2.append(",");
						sbQ22.append(",");
					}
				}

				Pair<ArrayList<Double>, ArrayList<Double>> res3 = getMeanMedian(
						pathToWrite + "AllNumDirectAgreementsL1" + mu + ".csv", seqLength, 4);
				for (int k = 0; k < res3.getFirst().size(); k++)
				{
					sbQ3.append(res3.getFirst().get(k));
					sbQ33.append(res3.getSecond().get(k));

					if (k == res3.getFirst().size() - 1)
					{
						sbQ3.append("\n");
						sbQ33.append("\n");
					}
					else
					{
						sbQ3.append(",");
						sbQ33.append(",");
					}
				}

				Pair<ArrayList<Double>, ArrayList<Double>> res4 = getMeanMedian(
						pathToWrite + "AllPerDirectAgreementsL1" + mu + ".csv", seqLength, 4);
				for (int k = 0; k < res4.getFirst().size(); k++)
				{
					sbQ4.append(res4.getFirst().get(k));
					sbQ44.append(res4.getSecond().get(k));

					if (k == res4.getFirst().size() - 1)
					{
						sbQ4.append("\n");
						sbQ44.append("\n");
					}
					else
					{
						sbQ4.append(",");
						sbQ44.append(",");
					}
				}
			}

			WToFile.writeToNewFile(sbQ1.toString(), pathToWrite + "NumAgreementMean.csv");
			WToFile.writeToNewFile(sbQ11.toString(), pathToWrite + "NumAgreementMedian.csv");

			WToFile.writeToNewFile(sbQ2.toString(), pathToWrite + "PerAgreementMean.csv");
			WToFile.writeToNewFile(sbQ22.toString(), pathToWrite + "PerAgreementMedian.csv");

			WToFile.writeToNewFile(sbQ3.toString(), pathToWrite + "NumAgreementMeanL1.csv");
			WToFile.writeToNewFile(sbQ33.toString(), pathToWrite + "NumAgreementMedianL1.csv");

			WToFile.writeToNewFile(sbQ4.toString(), pathToWrite + "PerAgreementMeanL1.csv");
			WToFile.writeToNewFile(sbQ44.toString(), pathToWrite + "PerAgreementMedianL1.csv");
			consoleLogStream.close();
		}
	}

	/**
	 * For each mu, concatenate results files for each groups of users, so that we get single files containing results
	 * for all users.
	 * 
	 * @param pathToWriteLog
	 * @param matchingUnitAsPastCount
	 * @param listOfNumAgreementsFiles
	 * @param listOfPerAgreementsFiles
	 * @param listOfNumAgreementsFilesL1
	 * @param listOfPerAgreementsFilesL1
	 * @return
	 */
	private static ArrayList<String> concatenateFiles(String pathToWrite, double matchingUnitAsPastCount[],
			ArrayList<ArrayList<String>> listOfNumAgreementsFiles,
			ArrayList<ArrayList<String>> listOfPerAgreementsFiles,
			ArrayList<ArrayList<String>> listOfNumAgreementsFilesL1,
			ArrayList<ArrayList<String>> listOfPerAgreementsFilesL1, String dimensionPhrase)
	{
		ArrayList<String> listOfWrittenFiles = new ArrayList<String>();
		// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + "CSVConcatLog.txt");

		for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
		{
			int mu = (int) matchingUnitAsPastCount[muIndex];
			// PopUps.showMessage("Will now concatenate:" + listOfNumAgreementsFiles.get(muIndex).size() + "files");
			// PopUps.showMessage("listOfNumAgreementsFilesget(muIndex) = " +
			// listOfNumAgreementsFiles.get(muIndex));

			CSVUtils.concatenateCSVFiles(listOfNumAgreementsFiles.get(muIndex), false,
					pathToWrite + "AllNumDirectAgreements_" + mu + dimensionPhrase + ".csv");
			listOfWrittenFiles.add(pathToWrite + "AllNumDirectAgreements" + mu + dimensionPhrase + ".csv");

			CSVUtils.concatenateCSVFiles(listOfPerAgreementsFiles.get(muIndex), false,
					pathToWrite + "AllPerDirectAgreements_" + mu + dimensionPhrase + ".csv");
			listOfWrittenFiles.add(pathToWrite + "AllPerDirectAgreements" + mu + dimensionPhrase + ".csv");

			CSVUtils.concatenateCSVFiles(listOfNumAgreementsFilesL1.get(muIndex), false,
					pathToWrite + "AllNumDirectAgreementsL1_" + mu + dimensionPhrase + ".csv");
			listOfWrittenFiles.add(pathToWrite + "AllNumDirectAgreementsL1" + mu + dimensionPhrase + ".csv");

			CSVUtils.concatenateCSVFiles(listOfPerAgreementsFilesL1.get(muIndex), false,
					pathToWrite + "AllPerDirectAgreementsL1_" + mu + dimensionPhrase + ".csv");
			listOfWrittenFiles.add(pathToWrite + "AllPerDirectAgreementsL1" + mu + dimensionPhrase + ".csv");
		}
		// consoleLogStream.close();

		return listOfWrittenFiles;

	}

	/**
	 * For each mu, concatenate results top k results' files for each groups of users, so that we get single files
	 * containing results for all users.
	 * 
	 * @param pathToWriteLog
	 * @param matchingUnitAsPastCount
	 * @param listOfNumAgreementsFiles
	 * @param listOfPerAgreementsFiles
	 * @param listOfNumAgreementsFilesL1
	 * @param listOfPerAgreementsFilesL1
	 * @return
	 */
	private static ArrayList<String> concatenateTopKFiles(String pathToWrite, double matchingUnitAsPastCount[],
			ArrayList<ArrayList<String>> listOfNumTopKAgreementsFiles,
			ArrayList<ArrayList<String>> listOfPerTopKAgreementsFiles,
			ArrayList<ArrayList<String>> listOfNumTopKAgreementsFilesL1,
			ArrayList<ArrayList<String>> listOfPerTopKAgreementsFilesL1, String dimensionPhrase)
	{
		ArrayList<String> listOfWrittenFiles = new ArrayList<String>();
		// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + "CSVTopKConcatLog.txt");

		for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
		{
			int mu = (int) matchingUnitAsPastCount[muIndex];
			// PopUps.showMessage("Will now concatenate:" + listOfNumAgreementsFiles.get(muIndex).size() + "files");
			// PopUps.showMessage("listOfNumAgreementsFilesget(muIndex) = " +
			// listOfNumAgreementsFiles.get(muIndex));

			CSVUtils.concatenateCSVFiles(listOfNumTopKAgreementsFiles.get(muIndex), false,
					pathToWrite + "AllNumDirectTopKAgreements_" + mu + dimensionPhrase + ".csv");
			listOfWrittenFiles.add(pathToWrite + "AllNumDirectTopKAgreements_" + mu + dimensionPhrase + ".csv");

			CSVUtils.concatenateCSVFiles(listOfPerTopKAgreementsFiles.get(muIndex), false,
					pathToWrite + "AllPerDirectTopKAgreements_" + mu + dimensionPhrase + ".csv");
			listOfWrittenFiles.add(pathToWrite + "AllPerDirectTopKAgreements_" + mu + dimensionPhrase + ".csv");

			CSVUtils.concatenateCSVFiles(listOfNumTopKAgreementsFilesL1.get(muIndex), false,
					pathToWrite + "AllNumDirectTopKAgreementsL1_" + mu + dimensionPhrase + ".csv");
			listOfWrittenFiles.add(pathToWrite + "AllNumDirectTopKAgreementsL1_" + mu + dimensionPhrase + ".csv");

			CSVUtils.concatenateCSVFiles(listOfPerTopKAgreementsFilesL1.get(muIndex), false,
					pathToWrite + "AllPerDirectTopKAgreementsL1_" + mu + dimensionPhrase + ".csv");
			listOfWrittenFiles.add(pathToWrite + "AllPerDirectTopKAgreementsL1_" + mu + dimensionPhrase + ".csv");
		}
		// consoleLogStream.close();

		return listOfWrittenFiles;

	}

	/**
	 * For each mu, concatenate results files for each groups of users, so that we get single files containing results
	 * for all users.
	 * 
	 * @param pathToWrite
	 * @param matchingUnitAsPastCount
	 * @param listOfStep0MeanReciprocalRankFiles
	 * @param listOfStep0AvgPrecisionFiles
	 * @param listOfStep0AvgRecallFiles
	 * @param listOfStep0AvgFMeasureFiles
	 * @param dimensionPhrase
	 * @return
	 */
	private static ArrayList<String> concatenateMRRPrecisonRecallFMeasureFiles(String pathToWrite,
			double matchingUnitAsPastCount[], ArrayList<ArrayList<String>> listOfStep0MeanReciprocalRankFiles,
			ArrayList<ArrayList<String>> listOfStep0AvgPrecisionFiles,
			ArrayList<ArrayList<String>> listOfStep0AvgRecallFiles,
			ArrayList<ArrayList<String>> listOfStep0AvgFMeasureFiles, String dimensionPhrase)
	{
		ArrayList<String> listOfWrittenFiles = new ArrayList<String>();
		// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + "CSVConcatLog.txt");

		for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
		{
			int mu = (int) matchingUnitAsPastCount[muIndex];
			// PopUps.showMessage("Will now concatenate:" + listOfNumAgreementsFiles.get(muIndex).size() + "files");
			// PopUps.showMessage("listOfNumAgreementsFilesget(muIndex) = " +
			// listOfNumAgreementsFiles.get(muIndex));

			String fileNameToWrite1 = pathToWrite + "AllMeanReciprocalRank_" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0MeanReciprocalRankFiles.get(muIndex), true, fileNameToWrite1);
			listOfWrittenFiles.add(fileNameToWrite1);

			String fileNameToWrite2 = pathToWrite + "AvgPrecision_" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0AvgPrecisionFiles.get(muIndex), true, fileNameToWrite2);
			listOfWrittenFiles.add(fileNameToWrite2);

			String fileNameToWrite3 = pathToWrite + "AvgRecall_" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0AvgRecallFiles.get(muIndex), true, fileNameToWrite3);
			listOfWrittenFiles.add(fileNameToWrite3);

			String fileNameToWrite4 = pathToWrite + "AvgFMeasure_" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0AvgFMeasureFiles.get(muIndex), true, fileNameToWrite4);
			listOfWrittenFiles.add(fileNameToWrite4);
		}
		// consoleLogStream.close();

		return listOfWrittenFiles;

	}

	/// Start of added on 21 Dec

	/// End of added on 21 Dec

	/**
	 * 
	 * @param fileToRead
	 * @param seqLength
	 * @param roundOfPlaces
	 * @return
	 */
	public static Pair<ArrayList<Double>, ArrayList<Double>> getMeanMedian(String fileToRead, int seqLength,
			int roundOfPlaces)
	{
		int[] columnIndicesToRead = IntStream.range(0, seqLength).toArray();
		ArrayList<ArrayList<Double>> columnWiseVals = ReadingFromFile.allColumnsReaderDouble(fileToRead, ",",
				columnIndicesToRead, false);

		ArrayList<Double> all1ValsMean = new ArrayList<>();
		ArrayList<Double> all1ValsMedian = new ArrayList<>();

		for (ArrayList<Double> valsForAColumn : columnWiseVals)
		{
			all1ValsMean.add(StatsUtils.meanOfArrayList(valsForAColumn, roundOfPlaces));
			all1ValsMedian.add(StatsUtils.meanOfArrayList(valsForAColumn, roundOfPlaces));
		}

		return new Pair<ArrayList<Double>, ArrayList<Double>>(all1ValsMean, all1ValsMedian);
	}

	/**
	 * 
	 * @param seqLength
	 * @param pathToReadResults
	 * @param pathToWrite
	 * @param commonPath
	 * @param verbose
	 * @param dimensionPhrase
	 *            intrdocued on 19 July 2018
	 * @return
	 *         <p>
	 *         used as of 19 July 2018
	 */
	public int doEvaluationSeq(int seqLength, String pathToReadResults, String pathToWrite, String commonPath,
			boolean verbose, String dimensionPhrase)
	{
		int numOfUsersComputedForSeqPred = Integer.MIN_VALUE;
		// all data must have same number of users, i.e., same numer of rows, i.e., same size for outer arraylist.
		Set<Integer> numOfUsersSanityCheck = new LinkedHashSet<>();

		System.out.println("Inside doEvaluationSeq dimensionPhrase=" + dimensionPhrase);
		try
		{
			// if (evaluatePostFiltering == false)// temp on 20 July 2018, remove
			if (this.evaluateSeqPrediction)
			{// block for evaluating sequence prediction overall
				// {
				Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArraysPredSeq = readDataMetaActualTopK(
						pathToReadResults, "dataRecommSequence" + dimensionPhrase + "WithScore.csv",
						"dataActualSequence" + dimensionPhrase + ".csv", commonPath, verbose);

				ArrayList<ArrayList<String>> arrayMeta = readArraysPredSeq.getFirst();
				ArrayList<ArrayList<String>> arrayRecommendedSequence = readArraysPredSeq.getThird();
				ArrayList<ArrayList<String>> arrayActualSequence = readArraysPredSeq.getSecond();

				numOfUsersSanityCheck.add(arrayMeta.size());
				numOfUsersSanityCheck.add(arrayRecommendedSequence.size());
				numOfUsersSanityCheck.add(arrayActualSequence.size());

				numOfUsersComputedForSeqPred = doEvaluationSeq(arrayMeta, arrayRecommendedSequence, arrayActualSequence,
						timeCategories, "Algo", verbose, pathToWrite, seqLength, dimensionPhrase);
			}

			// block for evaluating sequence prediction at each first K
			for (int seqIndex = 0; seqIndex < seqLength; seqIndex++)
			{
				String actualFileToEvaluateAgainst = "";

				if (evaluatePostFiltering == false)
				{// matched against actual data of corresponding dimension
					actualFileToEvaluateAgainst = "dataActual" + dimensionPhrase + seqIndex + ".csv";
				}
				else// for postfiltering
				{// matched against actual data of primary (no) dimension
					actualFileToEvaluateAgainst = "dataActual" + "" + seqIndex + ".csv";
				}

				Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArraysForFirstK = readDataMetaActualTopK(
						pathToReadResults, "dataRankedRecommendation" + dimensionPhrase + "WithoutScores"
								+ seqIndex/* seqLength */ + ".csv",
						actualFileToEvaluateAgainst, commonPath, verbose);

				// should be same same meta for all
				ArrayList<ArrayList<String>> arrayMetaFirstK = readArraysForFirstK.getFirst();
				ArrayList<ArrayList<String>> arrayRecommendedSequenceFirstK = readArraysForFirstK.getThird();
				ArrayList<ArrayList<String>> arrayActualSequenceFirstK = readArraysForFirstK.getSecond();

				numOfUsersSanityCheck.add(arrayMetaFirstK.size());
				numOfUsersSanityCheck.add(arrayRecommendedSequenceFirstK.size());
				numOfUsersSanityCheck.add(arrayActualSequenceFirstK.size());

				doEvaluationNonSeq(arrayMetaFirstK, arrayRecommendedSequenceFirstK, arrayActualSequenceFirstK,
						timeCategories, Constant.EvalPrecisionRecallFMeasure, theKOriginal, "AlgoStep" + seqIndex,
						dimensionPhrase);
			}

			// for (int i = 0; i < seqLength; i++)
			// {
			// Triple(arrayMeta, arrayActual, arrayTopK)
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
			// }
			if (numOfUsersSanityCheck.size() != 1)
			{
				PopUps.showError("Error: numOfUsersSanityCheck.size() = " + numOfUsersSanityCheck.size());
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "org.activity.evaluation.EvaluationSeq.doEvaluationSeq()");
		}

		System.out.println("Exiting  doEvaluationSeq");

		// System.out.println("All test stats done");
		// PopUps.showMessage("All test stats done");
		return numOfUsersComputedForSeqPred;
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

	/**
	 * 
	 * for level 2 and level 1: writeDirectAgreements, writeNumAndPercentageDirectAgreements, writeDirectTopKAgreements
	 * 
	 * @param arrayMeta
	 * @param arrayRecommendedSeq
	 * @param arrayActualSeq
	 * @param timeCategories
	 * @param algoLabel
	 * @param verbose
	 * @param pathToWrite
	 * @param seqLength
	 * @param dimensionPhrase
	 *            - do level 2 evaluation only then dimensionPhrase is "" - introduced on 19 July 2018 only for
	 *            filenames of files to be written
	 * @return
	 */
	private static int doEvaluationSeq(ArrayList<ArrayList<String>> arrayMeta,
			ArrayList<ArrayList<String>> arrayRecommendedSeq, ArrayList<ArrayList<String>> arrayActualSeq,
			String[] timeCategories, String algoLabel, boolean verbose, String pathToWrite, int seqLength,
			String dimensionPhrase)
	{
		int numOfUsers = arrayMeta.size();
		int numOfUsersFromDirectAgreements = Integer.MIN_VALUE;
		// if (verbose)
		{
			System.out.println("Inside doEvaluationSeq\nNum of users = " + numOfUsers);
		}

		for (String timeCategory : timeCategories)
		{
			// Direct category level: level 2
			ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements = computeDirectAgreements(algoLabel,
					timeCategory, arrayMeta, arrayRecommendedSeq, arrayActualSeq, -1);

			writeDirectAgreements(algoLabel, timeCategory, arrayDirectAgreements, pathToWrite, dimensionPhrase);
			writeNumAndPercentageDirectAgreements(algoLabel, timeCategory, arrayDirectAgreements, pathToWrite,
					seqLength, dimensionPhrase);
			writeDirectTopKAgreements(algoLabel, timeCategory, arrayDirectAgreements, pathToWrite, seqLength,
					dimensionPhrase);

			// category level: level 1
			if (dimensionPhrase.equals(""))// primary dimension
			{
				ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreementsL1 = computeDirectAgreements(algoLabel,
						timeCategory, arrayMeta, arrayRecommendedSeq, arrayActualSeq, 1);

				writeDirectAgreements(algoLabel + "L1", timeCategory, arrayDirectAgreementsL1, pathToWrite,
						dimensionPhrase);
				writeNumAndPercentageDirectAgreements(algoLabel + "L1", timeCategory, arrayDirectAgreementsL1,
						pathToWrite, seqLength, dimensionPhrase);
				writeDirectTopKAgreements(algoLabel + "L1", timeCategory, arrayDirectAgreementsL1, pathToWrite,
						seqLength, dimensionPhrase);
			}
			numOfUsersFromDirectAgreements = arrayDirectAgreements.size();
		}

		return numOfUsersFromDirectAgreements;
	}

	/**
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayDirectAgreements
	 * @param pathToWrite
	 */
	private static void writeNumAndPercentageDirectAgreements(String fileNamePhrase, String timeCategory,
			ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements, String pathToWrite, int seqLength,
			String dimensionPhrase)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbPercentage = new StringBuilder();
			for (int i = 0; i < arrayDirectAgreements.size(); i++)
			{
				ArrayList<ArrayList<Integer>> arrayForAUser = arrayDirectAgreements.get(i);
				for (int seqIndex = 0; seqIndex < seqLength; seqIndex++)
				{
					int sumAgreementsForThisIndex = 0;
					for (int j = 0; j < arrayForAUser.size(); j++)
					{
						ArrayList<Integer> arrayForAnRt = arrayForAUser.get(j);
						if (arrayForAnRt.get(seqIndex) == 1)
						{
							sumAgreementsForThisIndex += 1;
						}
						// if (VerbosityConstants.verboseEvaluationMetricsToConsole)
						// {System.out.println("arrayForAnRt = " + arrayForAnRt); }
					}

					double percentageAgreement = StatsUtils
							.round((sumAgreementsForThisIndex * 100.0) / arrayForAUser.size(), 4);
					sbPercentage.append(percentageAgreement);
					sb.append(sumAgreementsForThisIndex);

					if (seqIndex == seqLength - 1)
					{
						sb.append("\n");
						sbPercentage.append("\n");
					}
					else
					{
						sb.append(",");
						sbPercentage.append(",");
					}
				}
			}

			WToFile.writeToNewFile(sb.toString(),
					pathToWrite + fileNamePhrase + timeCategory + "NumDirectAgreements" + dimensionPhrase + ".csv");
			WToFile.writeToNewFile(sbPercentage.toString(), pathToWrite + fileNamePhrase + timeCategory
					+ "PercentageDirectAgreements" + dimensionPhrase + ".csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "org.activity.evaluation.EvaluationSeq.writeNumAndPercentageDirectAgreements()");
		}
	}

	/**
	 * Agreements at each index of the sequence individually
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayDirectAgreements
	 * @param pathToWritex
	 */
	private static void writeDirectAgreements(String fileNamePhrase, String timeCategory,
			ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements, String pathToWrite, String dimensionPhrase)
	{
		System.out
				.println("Ajooba writeDirectAgreements: arrayDirectAgreements.size()= " + arrayDirectAgreements.size());
		// PopUps.showMessage(
		// "Ajooba writeDirectAgreements: arrayDirectAgreements.size()= " + arrayDirectAgreements.size());

		try
		{
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < arrayDirectAgreements.size(); i++)
			// ArrayList<ArrayList<Integer>> arrayForAUser : // arrayDirectAgreements)
			{
				ArrayList<ArrayList<Integer>> arrayForAUser = arrayDirectAgreements.get(i);
				for (int j = 0; j < arrayForAUser.size(); j++)
				// ArrayList<Integer> arrayForAnRt : arrayForAUser)
				{
					ArrayList<Integer> arrayForAnRt = arrayForAUser.get(j);

					if (VerbosityConstants.verboseEvaluationMetricsToConsole)
					{
						System.out.println("arrayForAnRt = " + arrayForAnRt);
					}

					arrayForAnRt.stream().forEachOrdered(v -> sb.append(v));
					if (j == arrayForAUser.size() - 1)
					{
						sb.append("\n");
					}
					else
					{
						sb.append(",");
					}
				}
			}

			WToFile.writeToNewFile(sb.toString(),
					pathToWrite + fileNamePhrase + timeCategory + "DirectAgreements" + dimensionPhrase + ".csv");

		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "org.activity.evaluation.EvaluationSeq.writeDirectAgreements()");
		}
	}

	/**
	 * Agreements at Top 1, top 2....top (seq length)
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayDirectAgreements
	 * @param pathToWrite
	 */
	private static void writeDirectTopKAgreements(String fileNamePhrase, String timeCategory,
			ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements, String pathToWrite, int seqLength,
			String dimensionPhrase)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			StringBuilder sbPercentage = new StringBuilder();

			for (int i = 0; i < arrayDirectAgreements.size(); i++)
			{
				ArrayList<ArrayList<Integer>> arrayForAUser = arrayDirectAgreements.get(i);

				for (int topKSeqIndex = 0; topKSeqIndex < seqLength; topKSeqIndex++)
				{
					int sumAgreementsForThisTopK = 0;

					for (ArrayList<Integer> arrayForAnRt : arrayForAUser)// int j = 0; j < arrayForAUser.size(); j++)
					{
						// check if arrayForAnRt(0)...to...arrayForAnRt(topKSeqIndex) are 1 (matched/agreement).
						boolean areAllOnes = arrayForAnRt.stream().limit(topKSeqIndex + 1).allMatch(v -> v == 1);
						if (areAllOnes)
						{
							sumAgreementsForThisTopK += 1;
						}
					}

					double percentageAgreement = StatsUtils
							.round((sumAgreementsForThisTopK * 100.0) / arrayForAUser.size(), 4);
					sbPercentage.append(percentageAgreement);
					sb.append(sumAgreementsForThisTopK);

					if (topKSeqIndex == seqLength - 1)
					{
						sb.append("\n");
						sbPercentage.append("\n");
					}
					else
					{
						sb.append(",");
						sbPercentage.append(",");
					}
				}
			}

			WToFile.writeToNewFile(sb.toString(),
					pathToWrite + fileNamePhrase + timeCategory + "NumDirectTopKAgreements" + dimensionPhrase + ".csv");
			WToFile.writeToNewFile(sbPercentage.toString(), pathToWrite + fileNamePhrase + timeCategory
					+ "PercentageDirectTopKAgreements" + dimensionPhrase + ".csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "org.activity.evaluation.EvaluationSeq.writeDirectTopKAgreements()");
		}
	}

	/**
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayMeta
	 * @param arrayRecommendedSeq
	 * @param arrayActualSeq
	 * @param levelAtWhichToMatch
	 * @return ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements
	 */
	private static ArrayList<ArrayList<ArrayList<Integer>>> computeDirectAgreements(String fileNamePhrase,
			String timeCategory, ArrayList<ArrayList<String>> arrayMeta,
			ArrayList<ArrayList<String>> arrayRecommendedSeq, ArrayList<ArrayList<String>> arrayActualSeq,
			int levelAtWhichToMatch)
	{
		ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements = new ArrayList<>();

		try
		{
			System.out.println("size of meta array=" + arrayMeta.size() + "     size of arrayRecommendedSeq array="
					+ arrayRecommendedSeq.size() + "   size of arrayActualSeq array=" + arrayActualSeq.size());

			for (int i = 0; i < arrayMeta.size(); i++) // iterating over users (or rows)
			{
				ArrayList<String> currentMetaLineArray = arrayMeta.get(i);
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered
				ArrayList<ArrayList<Integer>> directAgreementsForThisUser = new ArrayList<>();

				for (int j = 0; j < currentMetaLineArray.size(); j++)// iterating over RTs (or columns)
				{
					int hourOfTheDay = getHourFromMetaString(currentMetaLineArray.get(j));

					if (DateTimeUtils.getTimeCategoryOfDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
							|| timeCategory.equals("All")) // TODO check why ALL
					{
						countOfRecommendationTimesConsidered++;

						String[] splittedActualSequence = RegexUtils.patternGreaterThan
								.split(arrayActualSeq.get(i).get(j));

						String[] splittedRecommSequence = RegexUtils.patternGreaterThan
								.split(arrayRecommendedSeq.get(i).get(j));

						if (splittedActualSequence.length != splittedRecommSequence.length)
						{
							PopUps.printTracedErrorMsg(
									"splittedActualSequence.length != splittedRecommSequence.length");
							// System.err.println(PopUps.getTracedErrorMsg("splittedActualSequence.length !=
							// splittedRecommSequence.length"));
						}
						ArrayList<Integer> directAgreement = new ArrayList<>();

						if (VerbosityConstants.verboseEvaluationMetricsToConsole)
						{
							System.out.print("\tsplittedRecomm = ");
						}
						for (int y = 0; y < splittedActualSequence.length; y++)
						{
							// removing score
							String splittedRecommY[] = RegexUtils.patternColon.split(splittedRecommSequence[y]);

							if (VerbosityConstants.verboseEvaluationMetricsToConsole)
							{// Suppressing this output since 16 Nov 2017
								System.out.print(">" + splittedRecommY[0]);// + "levelAtWhichToMatch = " +
																			// levelAtWhichToMatch);
							}

							if (splittedActualSequence[y] == null)
							{
								System.out.println("splittedActualSequence");
							}
							if (isAgree(splittedActualSequence[y], splittedRecommY[0], levelAtWhichToMatch))// splittedActualSequence[y].equals(splittedRecomm[0]))
							{
								if (VerbosityConstants.verboseEvaluationMetricsToConsole)
								{
									System.out.print("Eureka!");
								}
								directAgreement.add(1);
							}
							else
							{
								directAgreement.add(0);
							}
						}
						// System.out.println();
						directAgreementsForThisUser.add(directAgreement);

						if (VerbosityConstants.verbose)
						{
							System.out.println("\tarrayRecommendedSeq=" + arrayRecommendedSeq.get(i).get(j));
							System.out.println("\tarrayActualSeq string =" + arrayActualSeq.get(i).get(j));
							System.out.println("\tdirectAgreement" + directAgreement);
							System.out.println("\n-------------------");
						}
					} // end of if for this time categegory
				} // end of current line array
				arrayDirectAgreements.add(directAgreementsForThisUser);
				// bwRR.write("\n");
			} // end of loop over user
				// bwRR.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return arrayDirectAgreements;
	}

	/**
	 * 
	 * @param catID1
	 * @param catID2
	 * @param levelAtWhichToMatch
	 * @return
	 */
	private static boolean isAgree(String catID1, String catID2, int levelAtWhichToMatch)
	{

		try
		{
			// TODO
			if (levelAtWhichToMatch == -1)
			{
				return catID1.equals(catID2);
			}
			else if (levelAtWhichToMatch > 0 && levelAtWhichToMatch < 3)
			{
				ArrayList<Integer> catID1AtGivenLevel = DomainConstants.getGivenLevelCatID(Integer.valueOf(catID1),
						levelAtWhichToMatch);
				ArrayList<Integer> catID2AtGivenLevel = DomainConstants.getGivenLevelCatID(Integer.valueOf(catID2),
						levelAtWhichToMatch);

				int intersection = UtilityBelt.getIntersection(catID1AtGivenLevel, catID2AtGivenLevel).size();

				if (VerbosityConstants.verboseEvaluationMetricsToConsole)
				{
					System.out.println("levelAtWhichToMatch =" + levelAtWhichToMatch + "\ncatID1= " + catID1
							+ " catID2=" + catID2);
					System.out.println(
							"catID1AtGivenLevel= " + catID1AtGivenLevel + " catID2AtGivenLevel=" + catID2AtGivenLevel);
					System.out.println("Intersection.size = " + intersection);
				}

				if (intersection > 0)
				{
					if (VerbosityConstants.verboseEvaluationMetricsToConsole)
					{
						System.out.println("got intersection");
					}
					return true;
				}
				else
				{
					return false;
				}

			}
			else
			{
				System.err.println(PopUps.getTracedErrorMsg("Unknown levelAtWhichToMatch = " + levelAtWhichToMatch));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
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
	private static void doEvaluationNonSeq(ArrayList<ArrayList<String>> arrayMeta,
			ArrayList<ArrayList<String>> arrayTopK, ArrayList<ArrayList<String>> arrayActual, String[] timeCategories,
			boolean evalPrecisionRecallFMeasure, int theKOriginal, String algoLabel, String dimensionPhrase)
	{
		int numOfUsers = arrayTopK.size();
		for (String timeCategory : timeCategories)
		{
			writeReciprocalRank(algoLabel, timeCategory, arrayMeta, arrayTopK, arrayActual, dimensionPhrase);
			writeMeanReciprocalRank(algoLabel, timeCategory, numOfUsers, dimensionPhrase); // average over data points

			if (evalPrecisionRecallFMeasure)
			{
				for (int theK = theKOriginal; theK > 0; theK--)
				{
					writePrecisionRecallFMeasure(algoLabel, timeCategory, theK, arrayMeta, arrayTopK, arrayActual);
				}

				writeAvgPrecisionsForAllKs(algoLabel, timeCategory, numOfUsers, dimensionPhrase); // average over data
																									// points
				writeAvgRecallsForAllKs(algoLabel, timeCategory, numOfUsers, dimensionPhrase);
				writeAvgFMeasuresForAllKs(algoLabel, timeCategory, numOfUsers, dimensionPhrase);
			}
		}
	}

	/**
	 * 
	 * @param dataToRead
	 * @param labelForLog
	 * @return Triple (arrayData, countOfLinesData, log.toString())
	 * @throws IOException
	 */
	private static Triple<ArrayList<ArrayList<String>>, Integer, String> extractDataFromFile(BufferedReader dataToRead,
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
			ArrayList<ArrayList<String>> arrayActual, String dimensionPhrase)
	{
		String commonPath = Constant.getCommonPath();
		BufferedWriter bwRR = null;
		BufferedWriter bwEmptyRecomms = null;
		try
		{
			String metaCurrentLine, topKCurrentLine, actualCurrentLine;
			bwRR = WToFile.getBWForNewFile(
					commonPath + fileNamePhrase + timeCategory + "ReciprocalRank" + dimensionPhrase + ".csv");
			bwEmptyRecomms = WToFile.getBWForNewFile(
					commonPath + fileNamePhrase + timeCategory + "EmptyRecommsCount" + dimensionPhrase + ".csv");
			System.out.println("size of meta array=" + arrayMeta.size() + "     size of topK array=" + arrayTopK.size()
					+ "   size of actual array=" + arrayActual.size());

			for (int i = 0; i < arrayMeta.size(); i++) // iterating over users (or rows)
			{
				ArrayList<String> currentLineArray = arrayMeta.get(i);
				// $$System.out.println("Calculating RR for user:" + i);
				double RR = -99;
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered
				int countOfRTsForThisUserWithEmptyRecommendation = 0;

				for (int j = 0; j < currentLineArray.size(); j++) // iterating over recommendation times (or columns)
				{
					int hourOfTheDay = getHourFromMetaString(currentLineArray.get(j));
					if (DateTimeUtils.getTimeCategoryOfDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
							|| timeCategory.equals("All"))
					{
						countOfRecommendationTimesConsidered++;

						String actual = arrayActual.get(i).get(j);
						String topKRecommForThisUserForThisRT = arrayTopK.get(i).get(j);
						if (topKRecommForThisUserForThisRT.equals(null)
								|| topKRecommForThisUserForThisRT.trim().length() == 0)
						{
							// empty recommendation list
							countOfRTsForThisUserWithEmptyRecommendation += 1;
							RR = -1; // assuming the rank is at infinity
						}
						else
						{
							String[] topKString = RegexUtils.patternDoubleUnderScore
									.split(topKRecommForThisUserForThisRT);
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
				bwEmptyRecomms.write(countOfRTsForThisUserWithEmptyRecommendation + "\n");
			}
			bwRR.close();
			bwEmptyRecomms.close();
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

				// int[] userIDs = Constant.getUserIDs();//disable on 19 July, instead of userIDs, lets use row id as id
				// bwNumberOfRecommendationTimes.write();// userIDs[i] + ",");//disabled on 19 July

				// int theK=0;
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered

				for (int j = 0; j < currentLineArray.size(); j++) // iterating over recommendation times (or columns)
				{
					int hourOfTheDay = getHourFromMetaString(currentLineArray.get(j));
					if (DateTimeUtils.getTimeCategoryOfDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
							|| timeCategory.equals("All"))
					{
						countOfRecommendationTimesConsidered++;
						int countOfOccurence = 0; // the number of times the actual item appears in the top K
						// recommended list. NOTE: in the current case will be always be either 1 or 0.

						String actual = arrayActual.get(i).get(j);
						String topKForThisUserThisRT = arrayTopK.get(i).get(j);
						if (topKForThisUserThisRT.equals(null) || topKForThisUserThisRT.trim().length() == 0)
						{
							topKPrecisionVal = -1; // assigning 0 , or may be -1 for
							topKRecallVal = -1;
							topKFVal = -1;
						}
						else
						{
							String[] topKStrings = RegexUtils.patternDoubleUnderScore.split(arrayTopK.get(i).get(j));
							// arrayTopK.get(i).get(j).split("__");
							// topK is of the form string: __a__b__c__d__e is of length 6...
							// value at index 0 is empty.

							theK = theKOriginal;
							// System.out.println();

							if (topKStrings.length - 1 < theK) // for this RT we are not able to make K recommendations
																// as less than K recommendations are present.
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
								topKFVal = 2
										* ((topKPrecisionVal * topKRecallVal) / (topKPrecisionVal + topKRecallVal));
							}
							topKFVal = round(topKFVal, 4);
						}

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
				WToFile.closeBWs(bwTopKPrecision, bwTopKRecall, bwTopKF, bwNumberOfRecommendationTimes);
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
	 * <p>
	 * NOTE: Reads ReciprocalRank file.
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 */
	public static void writeMeanReciprocalRank(String fileNamePhrase, String timeCategory, int numUsers,
			String dimensionPhrase)
	{
		String commonPath = Constant.getCommonPath();
		try
		{
			BufferedWriter bw = WToFile.getBWForNewFile(
					commonPath + fileNamePhrase + timeCategory + "MeanReciprocalRank" + dimensionPhrase + ".csv");
			bw.write(",MRR\n");

			for (int user = 0; user < numUsers; user++)
			{
				bw.write("User_" + user + ","); // TODO: currently this starts from User_0, change it to start from
												// User_1 but this will also require necessary changes in other
												// places
				BufferedReader br = new BufferedReader(new FileReader(
						commonPath + fileNamePhrase + timeCategory + "ReciprocalRank" + dimensionPhrase + ".csv"));
				String currentLine;

				if (VerbosityConstants.verboseEvaluationMetricsToConsole)
				{
					System.out.println("Calculating MRR for user:" + user);
					System.out.println(("reading for MRR: " + commonPath + fileNamePhrase + timeCategory
							+ "ReciprocalRank" + dimensionPhrase + ".csv"));
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
									+ fileNamePhrase + timeCategory + "ReciprocalRank" + dimensionPhrase + ".csv");
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
	public static void writeAvgPrecisionsForAllKs(String fileNamePhrase, String timeCategory, int numUsers,
			String dimensionPhrase)
	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();
		try
		{
			File file = new File(
					commonPath + fileNamePhrase + timeCategory + "AvgPrecision" + dimensionPhrase + ".csv");

			file.delete();
			file.createNewFile();

			File validRTCountFile = new File(
					commonPath + fileNamePhrase + timeCategory + "AvgPrecisionCountValidRT" + dimensionPhrase + ".csv");
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
					System.out.println("reading for avg precision: " + commonPath + fileNamePhrase + timeCategory
							+ "top" + K + "Precision.csv" + " dimensionPhrase=" + dimensionPhrase);
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
	public static void writeAvgRecallsForAllKs(String fileNamePhrase, String timeCategory, int numUsers,
			String dimensionPhrase)
	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();
		try
		{
			File file = new File(commonPath + fileNamePhrase + timeCategory + "AvgRecall" + dimensionPhrase + ".csv");
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			File validRTCountFile = new File(
					commonPath + fileNamePhrase + timeCategory + "AvgRecallCountValidRT" + dimensionPhrase + ".csv");
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
										+ fileNamePhrase + timeCategory + "top" + K + "Precision.csv dimensionPhrase="
										+ dimensionPhrase);
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
	public static void writeAvgFMeasuresForAllKs(String fileNamePhrase, String timeCategory, int numUsers,
			String dimensionPhrase)
	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();
		try
		{
			File file = new File(commonPath + fileNamePhrase + timeCategory + "AvgFMeasure" + dimensionPhrase + ".csv");

			file.delete();

			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			File validRTCountFile = new File(
					commonPath + fileNamePhrase + timeCategory + "AvgFMeasureCountValidRT" + dimensionPhrase + ".csv");
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
										+ fileNamePhrase + timeCategory + "top" + K + "Precision.csv dimensionPhrase="
										+ dimensionPhrase);
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
