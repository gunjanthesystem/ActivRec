package org.activity.evaluation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.SummaryStat;
import org.activity.constants.PathConstants;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;

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
			listOfStep0AvgRecallFiles, listOfStep0AvgFMeasureFiles, listOfStep0AvgRecallCountValidRTFiles,
			listOfStep0EmptyRecommsCountFiles, listOfStep0PerActMRRFiles;// listOfReciprocalRankFiles,

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

		PathConstants.intialise(Constant.For9kUsers, Constant.getDatabaseName());
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
						PathConstants.pathToSerialisedGowallaLocZoneIdMap, false);

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
	 * @param matchingUnitArray
	 * @param dimensionPhrase
	 *            only if necessary, not essential for primary dimension, e.g. SecDim for secondary dimension
	 *            <p>
	 *            used as of 18 July 2018
	 * @param evaluatePostFiltering
	 * @param evaluatedSequencePrediction
	 */
	public EvaluationSeq(int seqLength, String outputCoreResultsPath, double[] matchingUnitArray,
			String dimensionPhrase, boolean evaluatePostFiltering, boolean evaluatedSequencePrediction)// sdsd
	{
		// PopUps.showMessage("EvaluationSeq muArray = " + Arrays.toString(matchingUnitArray));
		PathConstants.intialise(Constant.For9kUsers, Constant.getDatabaseName());

		this.evaluatePostFiltering = evaluatePostFiltering;
		this.evaluateSeqPrediction = evaluatedSequencePrediction;

		// commonPath = "./dataWritten/";
		PopUps.showMessage("Starting EvaluationSeq for dimensionPhrase = " + dimensionPhrase);
		if (Constant.useRandomlySampled100Users)
		{
			groupsOf100UsersLabels = new String[] { DomainConstants.gowalla100RandomUsersLabel };
		}
		else if (Constant.runForAllUsersAtOnce)
		{
			groupsOf100UsersLabels = new String[] { "All" };
		}

		System.out.println("outputCoreResultsPath= " + Constant.getOutputCoreResultsPath());
		System.out.println("groupsOf100UsersLabels= " + Arrays.asList(groupsOf100UsersLabels).toString());
		WToFile.writeToNewFile(Arrays.asList(groupsOf100UsersLabels).toString(),
				Constant.getOutputCoreResultsPath() + "UserGroupsOrder.csv");

		intialiseListOfFilenames(matchingUnitArray);
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
						PathConstants.pathToSerialisedGowallaLocZoneIdMap, false);

				for (int muIndex = 0; muIndex < matchingUnitArray.length; muIndex++)
				{
					double mu = matchingUnitArray[muIndex];

					// if (matchingUnitArray.length == 1)// daywise i.e. no MUs
					// {
					// commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/";
					// }

					if (Constant.lookPastType.equals(LookPastType.Daywise) && matchingUnitArray.length == 1)
					{// daywise i.e. no MUs
						commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/";
					}
					else
					{
						commonPath = outputCoreResultsPath + groupsOf100UsersLabel + "/MatchingUnit"
								+ String.valueOf(mu) + "/";
					}
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

					// start of added on 30 July 2018
					listOfStep0AvgRecallCountValidRTFiles.get(muIndex).add(commonPath + algoLabel + "Step0"
							+ timeCategory + "AvgRecallCountValidRT" + dimensionPhrase + ".csv");
					listOfStep0EmptyRecommsCountFiles.get(muIndex).add(commonPath + algoLabel + "Step0" + timeCategory
							+ "EmptyRecommsCount" + dimensionPhrase + ".csv");
					// end of added on 30 July 2018

					listOfStep0PerActMRRFiles.get(muIndex).add(commonPath + algoLabel + "Step0" + timeCategory
							+ "PerActivityMeanReciprocalRank" + dimensionPhrase + ".csv");// added on 24 Aug 2018

					// }
					consoleLogStream.close();
				} // end of loop over MUs

			} // end of loop over groups of 100 users

			WToFile.appendLineToFileAbs("Now will concatenate files from different groups of 100 users: ",
					Constant.getOutputCoreResultsPath() + "Debug1.txt\n");
			System.out.println("Debug: listOfNumTopKAgreementsFiles = \n" + listOfNumTopKAgreementsFiles.toString());
			System.out.println("Now will concatenate files: ");
			// PopUps.showMessage("BREAKING");
			ArrayList<String> listOfWrittenFiles = concatenateFiles(outputCoreResultsPath, matchingUnitArray,
					listOfNumAgreementsFiles, listOfPerAgreementsFiles, listOfNumAgreementsFilesL1,
					listOfPerAgreementsFilesL1, dimensionPhrase);

			ArrayList<String> listOfTopKWrittenFiles = concatenateTopKFiles(outputCoreResultsPath, matchingUnitArray,
					listOfNumTopKAgreementsFiles, listOfPerTopKAgreementsFiles, listOfNumTopKAgreementsFilesL1,
					listOfPerTopKAgreementsFilesL1, dimensionPhrase);

			ArrayList<String> listOfMRR_P_R_F_PerActMRRFiles = concatenateMRRPrecisonRecallFMeasurePerActMRRFiles(
					outputCoreResultsPath, matchingUnitArray, listOfStep0MeanReciprocalRankFiles,
					listOfStep0AvgPrecisionFiles, listOfStep0AvgRecallFiles, listOfStep0AvgFMeasureFiles,
					listOfStep0PerActMRRFiles, dimensionPhrase);

			ArrayList<String> listOfCountFiles = concatenateCountFiles(outputCoreResultsPath, matchingUnitArray,
					listOfStep0AvgRecallCountValidRTFiles, listOfStep0EmptyRecommsCountFiles, dimensionPhrase);

			// end of file condatenation from different user groups

			String[] fileNamePhrases = { "AllNumDirectAgreements_", "AllPerDirectAgreements_",
					"AllNumDirectAgreementsL1_", "AllPerDirectAgreementsL1_" };
			String[] fileNamePhrasesTopK = { "AllNumDirectTopKAgreements_", "AllPerDirectTopKAgreements_",
					"AllNumDirectTopKAgreementsL1_", "AllPerDirectTopKAgreementsL1_" };

			WToFile.appendLineToFileAbs("Now will write summary stats: ",
					Constant.getOutputCoreResultsPath() + "Debug1.txt\n");
			System.out.println("Now will write summary stats: ");
			SummaryStat[] summaryStats = { SummaryStat.Mean, SummaryStat.Median };

			summariseResults(seqLength, outputCoreResultsPath, matchingUnitArray, fileNamePhrases, summaryStats,
					"SummaryLog", dimensionPhrase);
			summariseResults(seqLength, outputCoreResultsPath, matchingUnitArray, fileNamePhrasesTopK, summaryStats,
					"SummaryTopKLog", dimensionPhrase);
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
						PathConstants.pathToSerialisedGowallaLocZoneIdMap, false);

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
							PathConstants.pathToSerialisedGowallaLocZoneIdMap, false);
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

		listOfStep0AvgRecallCountValidRTFiles = new ArrayList<>();// added on 30 July 2018
		listOfStep0EmptyRecommsCountFiles = new ArrayList<>();// added on 30 July 2018

		listOfStep0PerActMRRFiles = new ArrayList<>();// added on 14 Nov 2018

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

			listOfStep0AvgRecallCountValidRTFiles.add(muIndex, new ArrayList<String>());
			listOfStep0EmptyRecommsCountFiles.add(muIndex, new ArrayList<String>());
			listOfStep0PerActMRRFiles.add(muIndex, new ArrayList<String>());
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

		listOfStep0PerActMRRFiles.add(muIndex, new ArrayList<String>());
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
	 * @param listOfStep0PerActMRRFiles
	 * @return
	 */
	private static ArrayList<String> concatenateMRRPrecisonRecallFMeasurePerActMRRFiles(String pathToWrite,
			double matchingUnitAsPastCount[], ArrayList<ArrayList<String>> listOfStep0MeanReciprocalRankFiles,
			ArrayList<ArrayList<String>> listOfStep0AvgPrecisionFiles,
			ArrayList<ArrayList<String>> listOfStep0AvgRecallFiles,
			ArrayList<ArrayList<String>> listOfStep0AvgFMeasureFiles,
			ArrayList<ArrayList<String>> listOfStep0PerActMRRFiles, String dimensionPhrase)
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

			String fileNameToWrite2 = pathToWrite + "AllAvgPrecision_" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0AvgPrecisionFiles.get(muIndex), true, fileNameToWrite2);
			listOfWrittenFiles.add(fileNameToWrite2);

			String fileNameToWrite3 = pathToWrite + "AllAvgRecall_" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0AvgRecallFiles.get(muIndex), true, fileNameToWrite3);
			listOfWrittenFiles.add(fileNameToWrite3);

			String fileNameToWrite4 = pathToWrite + "AllAvgFMeasure_" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0AvgFMeasureFiles.get(muIndex), true, fileNameToWrite4);
			listOfWrittenFiles.add(fileNameToWrite4);

			String fileNameToWrite5 = pathToWrite + "AllPerActivityMeanReciprocalRank_" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0PerActMRRFiles.get(muIndex), true, fileNameToWrite5);
			listOfWrittenFiles.add(fileNameToWrite5);
		}
		// consoleLogStream.close();

		return listOfWrittenFiles;

	}

	/// Start of added on 30 July 2018
	/**
	 * For each mu, concatenate results count files for each groups of users, so that we get single files containing
	 * results for all users.
	 * 
	 * @param pathToWrite
	 * @param matchingUnitAsPastCount
	 * @param listOfStep0AvgRecallCountValidRTFiles
	 * @param listOfStep0EmptyRecommsCountFiles
	 * @param dimensionPhrase
	 * @return
	 * @since 30 July 2018
	 */
	private static ArrayList<String> concatenateCountFiles(String pathToWrite, double matchingUnitAsPastCount[],
			ArrayList<ArrayList<String>> listOfStep0AvgRecallCountValidRTFiles,
			ArrayList<ArrayList<String>> listOfStep0EmptyRecommsCountFiles, String dimensionPhrase)
	{
		ArrayList<String> listOfWrittenFiles = new ArrayList<String>();
		// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + "CSVConcatLog.txt");

		for (int muIndex = 0; muIndex < matchingUnitAsPastCount.length; muIndex++)
		{
			int mu = (int) matchingUnitAsPastCount[muIndex];
			// PopUps.showMessage("Will now concatenate:" + listOfNumAgreementsFiles.get(muIndex).size() + "files");
			// PopUps.showMessage("listOfNumAgreementsFilesget(muIndex) = " +
			// listOfNumAgreementsFiles.get(muIndex));

			String fileNameToWrite1 = pathToWrite + "AllAvgRecallCountValidRT" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0AvgRecallCountValidRTFiles.get(muIndex), true, fileNameToWrite1);
			listOfWrittenFiles.add(fileNameToWrite1);

			String fileNameToWrite2 = pathToWrite + "AllEmptyRecommsCount" + mu + dimensionPhrase + ".csv";
			CSVUtils.concatenateCSVFiles(listOfStep0EmptyRecommsCountFiles.get(muIndex), false, fileNameToWrite2);
			listOfWrittenFiles.add(fileNameToWrite2);
		}
		// consoleLogStream.close();

		return listOfWrittenFiles;

	}

	/// End of added on 30 July 2018

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
				Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArraysPredSeq = EvalDataReader
						.readDataMetaActualTopK(pathToReadResults,
								"dataRecommSequence" + dimensionPhrase + "WithScore.csv",
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

				Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArraysForFirstK = EvalDataReader
						.readDataMetaActualTopK(
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
			ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements = EvalMetrics.computeDirectAgreements(
					algoLabel, timeCategory, arrayMeta, arrayRecommendedSeq, arrayActualSeq, -1);

			EvalMetrics.writeDirectAgreements(algoLabel, timeCategory, arrayDirectAgreements, pathToWrite,
					dimensionPhrase);
			EvalMetrics.writeNumAndPercentageDirectAgreements(algoLabel, timeCategory, arrayDirectAgreements,
					pathToWrite, seqLength, dimensionPhrase);
			EvalMetrics.writeDirectTopKAgreements(algoLabel, timeCategory, arrayDirectAgreements, pathToWrite,
					seqLength, dimensionPhrase);

			// category level: level 1
			if (dimensionPhrase.equals(""))// primary dimension
			{
				ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreementsL1 = EvalMetrics.computeDirectAgreements(
						algoLabel, timeCategory, arrayMeta, arrayRecommendedSeq, arrayActualSeq, 1);

				EvalMetrics.writeDirectAgreements(algoLabel + "L1", timeCategory, arrayDirectAgreementsL1, pathToWrite,
						dimensionPhrase);
				EvalMetrics.writeNumAndPercentageDirectAgreements(algoLabel + "L1", timeCategory,
						arrayDirectAgreementsL1, pathToWrite, seqLength, dimensionPhrase);
				EvalMetrics.writeDirectTopKAgreements(algoLabel + "L1", timeCategory, arrayDirectAgreementsL1,
						pathToWrite, seqLength, dimensionPhrase);
			}
			numOfUsersFromDirectAgreements = arrayDirectAgreements.size();
		}

		return numOfUsersFromDirectAgreements;
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
	 * @param algoLabel
	 * @param dimensionPhrase
	 */
	private static void doEvaluationNonSeq(ArrayList<ArrayList<String>> arrayMeta,
			ArrayList<ArrayList<String>> arrayTopK, ArrayList<ArrayList<String>> arrayActual, String[] timeCategories,
			boolean evalPrecisionRecallFMeasure, int theKOriginal, String algoLabel, String dimensionPhrase)
	{
		int numOfUsers = arrayTopK.size();
		String[] activityNamesT = Constant.getActivityNames();

		for (String timeCategory : timeCategories)
		{
			String cp = Constant.getCommonPath();
			EvalMetrics.writeReciprocalRank(algoLabel, timeCategory, arrayMeta, arrayTopK, arrayActual, dimensionPhrase,
					cp);
			EvalMetrics.writeMeanReciprocalRank(algoLabel, timeCategory, numOfUsers, dimensionPhrase, cp);
			// average over data points

			if (evalPrecisionRecallFMeasure)
			{
				for (int theK = theKOriginal; theK > 0; theK--)
				{
					EvalMetrics.writePrecisionRecallFMeasure(algoLabel, timeCategory, theK, arrayMeta, arrayTopK,
							arrayActual, cp);
				}

				EvalMetrics.writeAvgPrecisionsForAllKs(algoLabel, timeCategory, numOfUsers, dimensionPhrase, cp);
				// average over data points
				EvalMetrics.writeAvgRecallsForAllKs(algoLabel, timeCategory, numOfUsers, dimensionPhrase, cp);
				EvalMetrics.writeAvgFMeasuresForAllKs(algoLabel, timeCategory, numOfUsers, dimensionPhrase, cp);
				EvalMetrics.writePerActMRRV2(algoLabel, timeCategory, /* activityNamesT, */ arrayActual,
						dimensionPhrase, cp, DomainConstants.catIDNameDictionary);
				// NEED to modify it to take in numOfUsers.
			}
		}
	}

}
