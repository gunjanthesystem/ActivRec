package org.activity.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.VerbosityConstants;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.Pair;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ConnectDatabase;
import org.activity.util.UtilityBelt;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * 
 * @author gunjan
 *
 */
public class EvaluationPostExperiment
{
	public static final String[] timeCategories = { "All" };// , "Morning", "Afternoon", "Evening" };

	public static void main(String args[])
	{
		// GowallaEvalCurrentEqualsTargetPerformance();
		// GeolifePostExperimentsNov2018();//

		// PostExperimentEvaluation2018();
		PostExperimentEvaluation2018V2("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/", null,
				"geolife1_NOV19ED0.5STimeDurDistTrStartGeoEndGeoAvgAltAllActsFDStFilter0hrsNoTTFilter");

		PopUps.showMessage("All done");
	}

	/**
	 * @since 19 Nov 2018
	 */
	public static void PostExperimentEvaluation2018V2(String commonPathToRead, List<String> listOfExperimentLabels,
			String experimentLabelForChosenMu)
	{
		// String experimentlabel = "geolife1_NOV15ED-1.0STimeAllActsFDStFilter0hrsNoTTFilterNoFED";
		// String commonPathToRead = "/run/media/gunjan/BackupVault/Geolife2018Results/";
		String commonPathToWrite = commonPathToRead + "PostExperimentEvaluation/";
		WToFile.createDirectoryIfNotExists(commonPathToWrite);
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/GeolifeAnalysisNov2018/";
		String experimentLabels[] = { "geolife1_NOV15ED0.0STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED0.15STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED0.25STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED0.75STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED1.0STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED0.5STimeAllActsFDStFilter0hrsNoTTFilter" };

		try
		{
			double[] muArray = Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor.None);
			String valLabel = "MeanReciprocalRank";
			MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentLabelForChosenMu + "/All/",
					commonPathToWrite + experimentLabelForChosenMu + "AllMUs" + valLabel + ".csv",
					"AlgoStep0All" + valLabel + ".csv", muArray, 1, true);
			System.exit(0);

			String pathToChosenMUBasedOnAlpha1 = "/home/gunjan/RWorkspace/GowallaRWorks/ChosenMU.csv";
			Pair<Map<String, Integer>, Map<Integer, Integer>> chosenMUPerUser = ResultsDistributionEvaluation
					.getUserChosenBestMUBasedOnGiveFile(pathToChosenMUBasedOnAlpha1, commonPathToWrite);

			Map<Integer, Integer> chosenMUPerUserIndex = chosenMUPerUser.getSecond();

			extractAndWriteValsForEachUserForChosenMU2(experimentLabels, commonPathToRead, commonPathToWrite,
					chosenMUPerUserIndex, "ReciprocalRank", false, false, "RRVals");
			extractAndWriteValsForEachUserForChosenMU3(experimentLabels, commonPathToRead, commonPathToWrite,
					chosenMUPerUserIndex, "ReciprocalRank", false, false, "RRVals");
			extractAndWriteValsForEachUserForChosenMU4(experimentLabels, commonPathToRead, commonPathToWrite,
					chosenMUPerUserIndex, "ReciprocalRank", false, false, "RRVals");

			for (String experimentlabel : experimentLabels)
			{
				// Read RR values only for chosen MU for each user
				{
					// Map<String, Integer> chosenMUPerUserID = chosenMUPerUser.getFirst();
					extractAndWriteValsForEachUserForChosenMU(experimentlabel, commonPathToRead, commonPathToWrite,
							chosenMUPerUserIndex, "ReciprocalRank", false, false, "RRVals");

				} // end of writing RR values for chosen MUs for each user
					// System.exit(0);

				{

					valLabel = "MeanReciprocalRank";
					MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
							commonPathToWrite + experimentlabel + "AllMUs" + valLabel + ".csv",
							"AlgoStep0All" + valLabel + ".csv", muArray, 1, true);

					valLabel = "NumOfRecommendationTimes";
					MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
							commonPathToWrite + experimentlabel + "AllMUs" + valLabel + ".csv",
							"AlgoStep0All" + valLabel + ".csv", muArray, 0, true);

					String valLabels[] = { "AvgRecall", "AvgPrecision", "AvgFMeasure" };
					for (String vall : valLabels)
					{
						for (int top = 1; top <= 5; top++)
						{
							MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
									commonPathToWrite + experimentlabel + "AllMUs" + vall + "@Top" + top + ".csv",
									"AlgoStep0All" + vall + ".csv", muArray, 6 - top, true);
						}
					}

					valLabel = "PercentageDirectAgreements";
					MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
							commonPathToWrite + experimentlabel + "AllMUs" + valLabel + ".csv",
							"AlgoL1All" + valLabel + ".csv", muArray, 0, false);

					valLabel = "PercentageDirectTopKAgreements";
					MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
							commonPathToWrite + experimentlabel + "AllMUs" + valLabel + ".csv",
							"AlgoL1All" + valLabel + ".csv", muArray, 0, false);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * @since 15 Nov 2018
	 */
	public static void PostExperimentEvaluation2018()
	{
		// String experimentlabel = "geolife1_NOV15ED-1.0STimeAllActsFDStFilter0hrsNoTTFilterNoFED";
		String commonPathToRead = "/run/media/gunjan/BackupVault/Geolife2018Results/";
		String commonPathToWrite = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/GeolifeAnalysisNov2018/";
		String experimentLabels[] = { "geolife1_NOV15ED0.0STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED0.15STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED0.25STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED0.75STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED1.0STimeAllActsFDStFilter0hrsNoTTFilter",
				"geolife1_NOV15ED0.5STimeAllActsFDStFilter0hrsNoTTFilter" };
		try
		{

			double[] muArray = Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor.None);
			String pathToChosenMUBasedOnAlpha1 = "/home/gunjan/RWorkspace/GowallaRWorks/ChosenMU.csv";
			Pair<Map<String, Integer>, Map<Integer, Integer>> chosenMUPerUser = ResultsDistributionEvaluation
					.getUserChosenBestMUBasedOnGiveFile(pathToChosenMUBasedOnAlpha1, commonPathToWrite);

			Map<Integer, Integer> chosenMUPerUserIndex = chosenMUPerUser.getSecond();

			extractAndWriteValsForEachUserForChosenMU2(experimentLabels, commonPathToRead, commonPathToWrite,
					chosenMUPerUserIndex, "ReciprocalRank", false, false, "RRVals");
			extractAndWriteValsForEachUserForChosenMU3(experimentLabels, commonPathToRead, commonPathToWrite,
					chosenMUPerUserIndex, "ReciprocalRank", false, false, "RRVals");
			extractAndWriteValsForEachUserForChosenMU4(experimentLabels, commonPathToRead, commonPathToWrite,
					chosenMUPerUserIndex, "ReciprocalRank", false, false, "RRVals");

			for (String experimentlabel : experimentLabels)
			{
				// Read RR values only for chosen MU for each user
				{
					// Map<String, Integer> chosenMUPerUserID = chosenMUPerUser.getFirst();
					extractAndWriteValsForEachUserForChosenMU(experimentlabel, commonPathToRead, commonPathToWrite,
							chosenMUPerUserIndex, "ReciprocalRank", false, false, "RRVals");

				} // end of writing RR values for chosen MUs for each user
					// System.exit(0);

				{

					String valLabel = "MeanReciprocalRank";
					MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
							commonPathToWrite + experimentlabel + "AllMUs" + valLabel + ".csv",
							"AlgoStep0All" + valLabel + ".csv", muArray, 1, true);

					valLabel = "NumOfRecommendationTimes";
					MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
							commonPathToWrite + experimentlabel + "AllMUs" + valLabel + ".csv",
							"AlgoStep0All" + valLabel + ".csv", muArray, 0, true);

					String valLabels[] = { "AvgRecall", "AvgPrecision", "AvgFMeasure" };
					for (String vall : valLabels)
					{
						for (int top = 1; top <= 5; top++)
						{
							MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
									commonPathToWrite + experimentlabel + "AllMUs" + vall + "@Top" + top + ".csv",
									"AlgoStep0All" + vall + ".csv", muArray, 6 - top, true);
						}
					}

					valLabel = "PercentageDirectAgreements";
					MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
							commonPathToWrite + experimentlabel + "AllMUs" + valLabel + ".csv",
							"AlgoL1All" + valLabel + ".csv", muArray, 0, false);

					valLabel = "PercentageDirectTopKAgreements";
					MUEvaluationUtils.writeValsForAllUsersAllMUs(commonPathToRead + experimentlabel + "/All/",
							commonPathToWrite + experimentlabel + "AllMUs" + valLabel + ".csv",
							"AlgoL1All" + valLabel + ".csv", muArray, 0, false);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * 
	 * @param experimentlabel
	 * @param commonPathToRead
	 * @param commonPathToWrite
	 * @param chosenMUPerUserIndex
	 * @param valLabel
	 * @param readFileHasColHeader
	 * @param readFileHasRowHeader
	 * @param headerForVals
	 * 
	 * @since 16 Nov 2018
	 */
	private static void extractAndWriteValsForEachUserForChosenMU(String experimentlabel, String commonPathToRead,
			String commonPathToWrite, Map<Integer, Integer> chosenMUPerUserIndex, String valLabel,
			boolean readFileHasColHeader, boolean readFileHasRowHeader, String headerForVals)
	{
		int maxNumOfValsForEachUserMU = 0;// such as multiple RRs for each user-MU
		StringBuilder sbToWrite2 = new StringBuilder();
		for (Entry<Integer, Integer> userEntry : chosenMUPerUserIndex.entrySet())
		{
			Integer userIndex = userEntry.getKey();
			Integer chosenMUForThisUSer = userEntry.getValue();

			String fileToRead = commonPathToRead + experimentlabel + "/All/MatchingUnit" + chosenMUForThisUSer
					+ ".0/AlgoStep0All" + valLabel + ".csv";
			List<List<String>> vals = ReadingFromFile.readLinesIntoListOfLists(fileToRead, userIndex + 1, 1, ",");
			if (readFileHasColHeader)
			{
				vals.remove(0);
			}

			Sanity.eq(vals.size(), 1, "Expected size 1");

			if (readFileHasRowHeader)
			{
				sbToWrite2.append(userIndex + "," + chosenMUForThisUSer + ","
						+ vals.get(0).stream().skip(1).collect(Collectors.joining(",")).toString() + "\n");
			}
			else
			{
				sbToWrite2.append(userIndex + "," + chosenMUForThisUSer + ","
						+ vals.get(0).stream().collect(Collectors.joining(",")).toString() + "\n");
			}

			maxNumOfValsForEachUserMU = vals.get(0).size() > maxNumOfValsForEachUserMU ? vals.get(0).size()
					: maxNumOfValsForEachUserMU;
			/// run/media/gunjan/BackupVault/Geolife2018Results/geolife1_NOV15ED-1.0STimeAllActsFDStFilter0hrsNoTTFilterNoFED/All/MatchingUnit3.0/AlgoStep0AllReciprocalRank.csv
		}

		String repeatedHeaderString = "";

		for (int i = 0; i < maxNumOfValsForEachUserMU; i++)
		{
			repeatedHeaderString += "," + headerForVals;
		}

		// write header and values
		WToFile.writeToNewFile("UserIndex,ChosenMU" + repeatedHeaderString + "\n" + sbToWrite2.toString(),
				commonPathToWrite + experimentlabel + "ChosenMU" + valLabel + ".csv");
	}

	/**
	 * 
	 * @param experimentlabel
	 * @param commonPathToRead
	 * @param commonPathToWrite
	 * @param chosenMUPerUserIndex
	 * @param valLabel
	 * @param readFileHasColHeader
	 * @param readFileHasRowHeader
	 * @param headerForVals
	 * 
	 * @since 16 Nov 2018
	 */
	private static void extractAndWriteValsForEachUserForChosenMU2(String[] experimentlabels, String commonPathToRead,
			String commonPathToWrite, Map<Integer, Integer> chosenMUPerUserIndex, String valLabel,
			boolean readFileHasColHeader, boolean readFileHasRowHeader, String headerForVals)
	{
		int maxNumOfValsForEachUserMU = 0;// such as multiple RRs for each user-MU
		StringBuilder sbToWrite2 = new StringBuilder();
		for (Entry<Integer, Integer> userEntry : chosenMUPerUserIndex.entrySet())
		{
			Integer userIndex = userEntry.getKey();
			Integer chosenMUForThisUSer = userEntry.getValue();

			for (String experimentLabel : experimentlabels)
			{
				String fileToRead = commonPathToRead + experimentLabel + "/All/MatchingUnit" + chosenMUForThisUSer
						+ ".0/AlgoStep0All" + valLabel + ".csv";
				List<List<String>> vals = ReadingFromFile.readLinesIntoListOfLists(fileToRead, userIndex + 1, 1, ",");
				if (readFileHasColHeader)
				{
					vals.remove(0);
				}

				Sanity.eq(vals.size(), 1, "Expected size 1");

				if (readFileHasRowHeader)
				{
					sbToWrite2.append(experimentLabel + "," + userIndex + "," + chosenMUForThisUSer + ","
							+ vals.get(0).stream().skip(1).collect(Collectors.joining(",")).toString() + "\n");
				}
				else
				{
					sbToWrite2.append(experimentLabel + "," + userIndex + "," + chosenMUForThisUSer + ","
							+ vals.get(0).stream().collect(Collectors.joining(",")).toString() + "\n");
				}

				maxNumOfValsForEachUserMU = vals.get(0).size() > maxNumOfValsForEachUserMU ? vals.get(0).size()
						: maxNumOfValsForEachUserMU;
			}
			/// run/media/gunjan/BackupVault/Geolife2018Results/geolife1_NOV15ED-1.0STimeAllActsFDStFilter0hrsNoTTFilterNoFED/All/MatchingUnit3.0/AlgoStep0AllReciprocalRank.csv
		}

		String repeatedHeaderString = "";

		for (int i = 0; i < maxNumOfValsForEachUserMU; i++)
		{
			repeatedHeaderString += "," + headerForVals;
		}

		// write header and values
		WToFile.writeToNewFile(
				"experimentLabel,UserIndex,ChosenMU" + repeatedHeaderString + "\n" + sbToWrite2.toString(),
				commonPathToWrite + "AllExperiments" + "ChosenMU" + valLabel + ".csv");
	}

	private static void extractAndWriteValsForEachUserForChosenMU4(String[] experimentlabels, String commonPathToRead,
			String commonPathToWrite, Map<Integer, Integer> chosenMUPerUserIndex, String valLabel,
			boolean readFileHasColHeader, boolean readFileHasRowHeader, String headerForVals)
	{

		StringBuilder sbToWrite2 = new StringBuilder();
		int sumNumOfValsForAllUsers = 0;
		for (String experimentLabel : experimentlabels)
		{
			sbToWrite2.append(experimentLabel);
			sumNumOfValsForAllUsers = 0;// such as multiple RRs for each user-MU
			for (Entry<Integer, Integer> userEntry : chosenMUPerUserIndex.entrySet())
			{
				Integer userIndex = userEntry.getKey();
				Integer chosenMUForThisUSer = userEntry.getValue();

				String fileToRead = commonPathToRead + experimentLabel + "/All/MatchingUnit" + chosenMUForThisUSer
						+ ".0/AlgoStep0All" + valLabel + ".csv";
				List<List<String>> vals = ReadingFromFile.readLinesIntoListOfLists(fileToRead, userIndex + 1, 1, ",");
				if (readFileHasColHeader)
				{
					vals.remove(0);
				}

				Sanity.eq(vals.size(), 1, "Expected size 1");

				if (readFileHasRowHeader)
				{
					sbToWrite2.append("," + vals.get(0).stream().skip(1).collect(Collectors.joining(",")).toString());
				}
				else
				{
					sbToWrite2.append("," + vals.get(0).stream().collect(Collectors.joining(",")).toString());
				}

				sumNumOfValsForAllUsers += vals.get(0).size();
			}
			sbToWrite2.append("\n");
			/// run/media/gunjan/BackupVault/Geolife2018Results/geolife1_NOV15ED-1.0STimeAllActsFDStFilter0hrsNoTTFilterNoFED/All/MatchingUnit3.0/AlgoStep0AllReciprocalRank.csv
		}

		String repeatedHeaderString = "";

		for (int i = 0; i < sumNumOfValsForAllUsers; i++)
		{
			repeatedHeaderString += "," + headerForVals;
		}

		// write header and values
		WToFile.writeToNewFile("experimentLabel" + repeatedHeaderString + "\n" + sbToWrite2.toString(),
				commonPathToWrite + "AllExperiments" + "AllUserConcatChosenMU" + valLabel + ".csv");
	}

	/**
	 * 
	 * @param experimentlabel
	 * @param commonPathToRead
	 * @param commonPathToWrite
	 * @param chosenMUPerUserIndex
	 * @param valLabel
	 * @param readFileHasColHeader
	 * @param readFileHasRowHeader
	 * @param headerForVals
	 * 
	 * @since 16 Nov 2018
	 */
	private static void extractAndWriteValsForEachUserForChosenMU3(String[] experimentlabels, String commonPathToRead,
			String commonPathToWrite, Map<Integer, Integer> chosenMUPerUserIndex, String valLabel,
			boolean readFileHasColHeader, boolean readFileHasRowHeader, String headerForVals)
	{
		int maxNumOfValsForEachUserMU = 0;// such as multiple RRs for each user-MU

		for (Entry<Integer, Integer> userEntry : chosenMUPerUserIndex.entrySet())
		{
			StringBuilder sbToWrite2 = new StringBuilder();
			Integer userIndex = userEntry.getKey();
			Integer chosenMUForThisUSer = userEntry.getValue();

			for (String experimentLabel : experimentlabels)
			{
				String fileToRead = commonPathToRead + experimentLabel + "/All/MatchingUnit" + chosenMUForThisUSer
						+ ".0/AlgoStep0All" + valLabel + ".csv";
				List<List<String>> vals = ReadingFromFile.readLinesIntoListOfLists(fileToRead, userIndex + 1, 1, ",");
				if (readFileHasColHeader)
				{
					vals.remove(0);
				}

				Sanity.eq(vals.size(), 1, "Expected size 1");

				if (readFileHasRowHeader)
				{
					sbToWrite2.append(experimentLabel + ","
							+ vals.get(0).stream().skip(1).collect(Collectors.joining(",")).toString() + "\n");
				}
				else
				{
					sbToWrite2.append(experimentLabel + ","
							+ vals.get(0).stream().collect(Collectors.joining(",")).toString() + "\n");
				}

				maxNumOfValsForEachUserMU = vals.get(0).size() > maxNumOfValsForEachUserMU ? vals.get(0).size()
						: maxNumOfValsForEachUserMU;
			}
			WToFile.writeToNewFile("ExpLabel\n" + sbToWrite2.toString(),
					commonPathToWrite + "AllExperiments" + "ChosenMU_User" + userIndex + "_" + valLabel + ".csv");
			/// run/media/gunjan/BackupVault/Geolife2018Results/geolife1_NOV15ED-1.0STimeAllActsFDStFilter0hrsNoTTFilterNoFED/All/MatchingUnit3.0/AlgoStep0AllReciprocalRank.csv
		}

		// write header and values

	}

	/**
	 * Evaluate the performance of recommendations when current activity = target activity
	 */
	public static void GowallaEvalCurrentEqualsTargetPerformance()
	{
		Constant.setDatabaseName("gowalla1");

		String rootPathToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";
		String s[] = { "101", "201", "301", "401", "501", "601", "701", "801", "901" };

		String path = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";

		for (String shead : s)
		{
			Constant.setOutputCoreResultsPath(path + shead + "/");
			// PopUps.showMessage("Constant.outputCoreResultsPath=" + Constant.outputCoreResultsPath);
			double[] matchingUnitArray = Constant.matchingUnitAsPastCount;

			for (double mu : matchingUnitArray)
			{
				Constant.setCommonPath(Constant.getOutputCoreResultsPath() + "MatchingUnit" + String.valueOf(mu) + "/");

				for (String timeCategory : timeCategories)
				{
					System.out.println(" " + Constant.getCommonPath());
					if (shead.equals("901"))
						writeMRRCurrTargetSameDistinguish("Algo", timeCategory, 134);
					else
						writeMRRCurrTargetSameDistinguish("Algo", timeCategory, 100);
					// writePerActivityMeanReciprocalRank("Algo", timeCategory);
					// writePerActivityMeanReciprocalRank("BaselineOccurrence", timeCategory);
					// // writePerActivityMeanReciprocalRank("BaselineDuration", timeCategory);
				}
			}
			Constant.setCommonPath(Constant.getOutputCoreResultsPath());
		}
	}

	/**
	 * Compute and write MRR as: </br>
	 * userid, MRR over RTs with current target same activity name, MRR over RTs with current target not same,MRR over
	 * RTs
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * 
	 *            only works for All time category
	 * @param numUsers
	 */
	public static void writeMRRCurrTargetSameDistinguish(String fileNamePhrase, String timeCategory, int numUsers)
	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();
		try
		{
			File file = new File(commonPath + fileNamePhrase + timeCategory + "MRRCurrTargetSameDistinguish.csv");

			file.delete();
			file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			BufferedWriter allTogetherForAnalysis = WToFile.getBufferedWriterForExistingFile(
					"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/MRRCurrTargetSameAllUsersAllMUs.csv");

			bw.write("User,MRRCurrTargetSame,MRRCurrTargetDiff,AllMRR\n");
			// allTogetherForAnalysis.write("User,MRRCurrTargetSame,MRRCurrTargetDiff,AllMRR,FileReadPath\n");
			// bwValidRTCount.write(",RTCount_RR");

			// bw.newLine();
			// bwValidRTCount.newLine();

			for (int user = 0; user < numUsers; user++)
			{

				bw.write("User_" + user + ","); // currently this starts from User_0
				allTogetherForAnalysis.write("User_" + user + ",");
				// bwValidRTCount.write("User_" + user + ",");
				// for (int K = theKOriginal; K > 0; K--)
				// {

				BufferedReader brRR = new BufferedReader(
						new FileReader(commonPath + fileNamePhrase + timeCategory + "ReciprocalRank.csv"));

				BufferedReader brifCurrTargetSame = new BufferedReader(
						new FileReader(commonPath + "metaIfCurrentTargetSameWriter.csv"));
				// no time category or filename phrase e.g."Algo" for meta.

				String currentRRLine, currentIfCurrTargetSameLine;

				if (VerbosityConstants.verboseEvaluationMetricsToConsole)
				{
					System.out.println("Calculating MRRCTD for user:" + user);
					System.out.println(
							("reading for MRR: " + commonPath + fileNamePhrase + timeCategory + "ReciprocalRank.csv"));
				}

				int lineNumber = 0;
				while ((currentRRLine = brRR.readLine()) != null)
				{
					currentIfCurrTargetSameLine = brifCurrTargetSame.readLine();
					if (currentIfCurrTargetSameLine == null)
					{
						PopUps.showError(
								"Error in org.activity.evaluation.EvaluationPostExperiment.writeMRRCurrTargetSameDistinguish(): currentIfCurrTargetSameLine is null");
					}

					if (lineNumber == user)
					{
						// get avg of all these rr values concern: splitting on empty lines gives an array of length1,
						// also splitting on one values line gives an array of length 1
						String[] allrrValuesForThisUser = currentRRLine.split(",");
						String[] ifCurrTargetSameValuesForThisUser = currentIfCurrTargetSameLine.split(",");

						if (allrrValuesForThisUser.length != ifCurrTargetSameValuesForThisUser.length)
						{
							PopUps.showError(
									"Error in org.activity.evaluation.EvaluationPostExperiment.writeMRRCurrTargetSameDistinguish():allrrValuesForThisUser.length ("
											+ allrrValuesForThisUser.length
											+ ") != ifCurrTargetSameValuesForThisUser.length ("
											+ ifCurrTargetSameValuesForThisUser.length + ")");
						}

						if (VerbosityConstants.verboseEvaluationMetricsToConsole)
						{
							System.out.println("current rrValues line read=" + currentRRLine + " trimmed length="
									+ currentRRLine.trim().length());
							System.out.println("current ifCurrTargetSameValues line read=" + currentIfCurrTargetSameLine
									+ " trimmed length=" + currentIfCurrTargetSameLine.trim().length());
						}
						System.out.println("#rr values for user(" + user + ") = " + allrrValuesForThisUser.length);

						// double[] pValuesForThisUserForThisK = new double[tokensInCurrentLine.length];

						double allMRRValueForThisUser = 0, currTargetSameMRRValueForThisUser = 0,
								currTargetDiffMRRValueForThisUser = 0;

						double allSum = 0, sumCurrTargetSame = 0, sumCurrTargetDiff = 0;
						int countOfValidRRValues = 0, countOfCurrTargetSame = 0, countOfCurrentTargetDiff = 0;

						ArrayList<Double> allrrValues = new ArrayList<Double>(),
								rrValuesForCurrTargetSame = new ArrayList<Double>(),
								rrValuesForCurrTargetDiff = new ArrayList<Double>();

						if (currentRRLine.trim().length() == 0)
						{
							System.out.println(" NOTE: line for user(" + user + ") is EMPTY for " + commonPath
									+ fileNamePhrase + timeCategory + "ReciprocalRank.csv");
						}

						else
						{ // calculate sum
							for (int i = 0; i < allrrValuesForThisUser.length; i++)
							{
								double rrValueRead = Double.parseDouble(allrrValuesForThisUser[i]);

								if (rrValueRead >= 0) // negative rr value for a given RT for given K means that we were
														// unable to make K recommendation at that RT
								{
									allrrValues.add(rrValueRead);
									countOfValidRRValues += 1;
									allSum += rrValueRead;
								}

								if (ifCurrTargetSameValuesForThisUser[i].equalsIgnoreCase("true")
										|| ifCurrTargetSameValuesForThisUser[i].equals("1")
										|| ifCurrTargetSameValuesForThisUser[i].equals("t"))
								{
									rrValuesForCurrTargetSame.add(rrValueRead);
									countOfCurrTargetSame += 1;
									sumCurrTargetSame += rrValueRead;
								}

								else if (ifCurrTargetSameValuesForThisUser[i].equalsIgnoreCase("false")
										|| ifCurrTargetSameValuesForThisUser[i].equals("0")
										|| ifCurrTargetSameValuesForThisUser[i].equals("f"))
								{
									rrValuesForCurrTargetDiff.add(rrValueRead);
									countOfCurrentTargetDiff += 1;
									sumCurrTargetDiff += rrValueRead;
								}
							}
						}
						// bwValidRTCount.write(countOfValidPValues + ",");
						if (countOfValidRRValues == 0) // to avoid divide by zero exception
							countOfValidRRValues = 1;
						if (countOfCurrTargetSame == 0) // to avoid divide by zero exception
							countOfCurrTargetSame = 1;
						if (countOfCurrentTargetDiff == 0) // to avoid divide by zero exception
							countOfCurrentTargetDiff = 1;

						if ((countOfCurrentTargetDiff + countOfCurrTargetSame) != countOfValidRRValues)
						{
							PopUps.showError(
									"Error in org.activity.evaluation.EvaluationPostExperiment.writeMRRCurrTargetSameDistinguish():(countOfCurrentTargetDiff("
											+ countOfCurrentTargetDiff + ")+ countOfCurrTargetSame("
											+ countOfCurrTargetSame + ")) != countOfValidRRValues("
											+ countOfValidRRValues + ")");
						}

						allMRRValueForThisUser = StatsUtils.round((double) allSum / countOfValidRRValues, 4);
						currTargetSameMRRValueForThisUser = StatsUtils
								.round((double) sumCurrTargetSame / countOfCurrTargetSame, 4);
						currTargetDiffMRRValueForThisUser = StatsUtils
								.round((double) sumCurrTargetDiff / countOfCurrentTargetDiff, 4);

						DescriptiveStatistics dsAllrr = StatsUtils.getDescriptiveStatistics(allrrValues);
						DescriptiveStatistics dsCurrentTargetSamerr = StatsUtils
								.getDescriptiveStatistics(rrValuesForCurrTargetSame);
						DescriptiveStatistics dsCurrentTargetDiffrr = StatsUtils
								.getDescriptiveStatistics(rrValuesForCurrTargetDiff);

						System.out.println("Calculating MRR: all = " + allSum + "/" + countOfValidRRValues);
						System.out.println("Calculating MRR: curr target same = " + sumCurrTargetSame + "/"
								+ countOfCurrTargetSame);
						System.out.println("Calculating MRR: curr target diff = " + sumCurrTargetDiff + "/"
								+ countOfCurrentTargetDiff);

						bw.write(currTargetSameMRRValueForThisUser + "," + currTargetDiffMRRValueForThisUser + ","
								+ allMRRValueForThisUser + ",");

						// allTogetherForAnalysis
						// .write(currTargetSameMRRValueForThisUser + "," + currTargetDiffMRRValueForThisUser + ","
						// + allMRRValueForThisUser + "," + countOfCurrTargetSame + ","
						// + countOfCurrentTargetDiff + "," + countOfValidRRValues + "," + commonPath);

						allTogetherForAnalysis.write(StatsUtils.round(dsCurrentTargetSamerr.getMean(), 4) + ","
								+ StatsUtils.round(dsCurrentTargetDiffrr.getMean(), 4) + ","
								+ StatsUtils.round(dsAllrr.getMean(), 4) + ","
								+ StatsUtils.round(dsCurrentTargetSamerr.getN(), 4) + ","
								+ StatsUtils.round(dsCurrentTargetDiffrr.getN(), 4) + ","
								+ StatsUtils.round(dsAllrr.getN(), 4) + ","
								+ StatsUtils.round(dsCurrentTargetSamerr.getStandardDeviation(), 4) + ","
								+ StatsUtils.round(dsCurrentTargetDiffrr.getStandardDeviation(), 4) + ","
								+ StatsUtils.round(dsAllrr.getStandardDeviation(), 4) + "," + commonPath);

						break;
					}
					lineNumber++;
				}

				brRR.close();
				brifCurrTargetSame.close();

				// }// end of K loop
				bw.newLine();
				allTogetherForAnalysis.newLine();
				// bwValidRTCount.newLine();
			}
			bw.close();
			allTogetherForAnalysis.close();
			// bwValidRTCount.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 */
	public static void GeolifePostExperiments()
	{
		Constant.setDatabaseName("geolife1");// "dcu_data_2";// "geolife1";////Constant.DATABASE_NAME = "geolife1";//
												// "dcu_data_2";// "geolife1";//

		// String features[] = { "ActivityName", "StartTime", "Duration", "DistanceTravelled", "StartEndGeoCoordinates",
		// "AvgAltitude" };

		// for (int j = 0; j < features.length; j++)
		{
			// String path =
			// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Feb11ImpBLNCount/Iteration1ForShowExp/Geolife/SimpleV3/";//
			// UMAP submission
			// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June18HJDistance/Geo/SimpleV3/";//
			String path = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/MUExperimentsBLNCount/Iteration1ForShow/Geolife/SimpleV3/";
			// rexperiment UMAP corrected TZ

			// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June7FeatureWiseEdit/Geo/SimpleV3"
			// +
			// features[j] + "/";

			Constant.setOutputCoreResultsPath(path);// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June5/DCU/SimpleV3/";//
													// /run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May7_2015/DCU/Sum1/";//
													// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May19/Geolife/Sum1/";
			System.out.println("path=" + path);
			ConnectDatabase.initialise(Constant.getDatabaseName());// .DATABASE_NAME); // all method and variable in
																	// this class are static
			String commonPath = Constant.getOutputCoreResultsPath();
			Constant.initialise("", Constant.getDatabaseName());// .DATABASE_NAME);

			double[] matchingUnitArray = Constant.matchingUnitAsPastCount;
			// Disabled as already written
			for (double mu : matchingUnitArray)
			{
				Constant.setCommonPath(Constant.getOutputCoreResultsPath() + "MatchingUnit" + String.valueOf(mu) + "/");

				String commonPathT = Constant.getCommonPath();
				String[] activityNamesT = Constant.getActivityNames();
				String dimensionPhraseT = "";
				for (String timeCategory : timeCategories)
				{
					EvalMetrics.writePerActMRR("Algo", timeCategory, activityNamesT, dimensionPhraseT, commonPathT, "");
					EvalMetrics.writePerActMRR("BaselineOccurrence", timeCategory, activityNamesT, dimensionPhraseT,
							commonPathT, "");
					EvalMetrics.writePerActMRR("BaselineDuration", timeCategory, activityNamesT, dimensionPhraseT,
							commonPathT, "");
				}
			}
			Constant.setCommonPath(Constant.getOutputCoreResultsPath());// + "MatchingUnit" + String.valueOf(mu) + "/");

			for (String timeCategory : timeCategories)
			{
				writePerActivityBestMeanReciprocalRankOverMUs("Algo", timeCategory);// s
				writePerRTBestMeanReciprocalRankOverMUs("Algo", timeCategory);// s
				// ALERT: make sure that correct getObservedOptimalMUForUserNum is used in
				// writePerRTRRForOptimalMUForEachUser
				String RRForEachRTForOptimalMUFile = writePerRTRRForOptimalMUForEachUser("Algo", timeCategory);// s
				LinkedHashMap<String, ArrayList<Double>> actRRsMap = getPerActivityEachRTKaRR("Algo", timeCategory,
						RRForEachRTForOptimalMUFile);
				WToFile.writeLinkedHashMapOfArrayList(actRRsMap, commonPath + "ActsRRMap.csv");
				writePerActivityBestMeanReciprocalRankOverMUs("BaselineOccurrence", timeCategory);
				writePerActivityBestMeanReciprocalRankOverMUs("BaselineDuration", timeCategory);
			}

		}
	}

	/**
	 * not used at the moment
	 * 
	 * @since 15 Nov 2018
	 */
	public static void GeolifePostExperimentsNov2018()
	{
		Constant.setDatabaseName("geolife1");

		String nextKRecommendation = "0";
		String algoLabel = "AlgoStep" + nextKRecommendation;

		{
			String path = "/run/media/gunjan/BackupVault/Geolife2018Results/geolife1_NOV15ED-1.0STimeAllActsFDStFilter0hrsNoTTFilterNoFED/All/";
			// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/MUExperimentsBLNCount/Iteration1ForShow/Geolife/SimpleV3/";
			Constant.setOutputCoreResultsPath(path);
			System.out.println("path=" + path);
			// ConnectDatabase.initialise(Constant.getDatabaseName());// .DATABASE_NAME); // all method and variable in
			// this class are static
			String commonPath = Constant.getOutputCoreResultsPath();
			Constant.initialise("", Constant.getDatabaseName());// .DATABASE_NAME);

			double[] matchingUnitArray = Constant.matchingUnitAsPastCount;
			// Disabled as already written
			for (double mu : matchingUnitArray)
			{
				Constant.setCommonPath(Constant.getOutputCoreResultsPath() + "MatchingUnit" + String.valueOf(mu) + "/");

				String commonPathT = Constant.getCommonPath();
				String[] activityNamesT = Constant.getActivityNames();
				String dimensionPhraseT = "";
				for (String timeCategory : timeCategories)
				{
					EvalMetrics.writePerActMRR(algoLabel, timeCategory, activityNamesT, dimensionPhraseT, commonPathT,
							nextKRecommendation);
					// EvalMetrics.writePerActMRR("BaselineOccurrence", timeCategory, activityNamesT, dimensionPhraseT,
					// commonPathT);
					// EvalMetrics.writePerActMRR("BaselineDuration", timeCategory, activityNamesT, dimensionPhraseT,
					// commonPathT);
				}
			}
			Constant.setCommonPath(Constant.getOutputCoreResultsPath());// + "MatchingUnit" + String.valueOf(mu) + "/");

			for (String timeCategory : timeCategories)
			{
				writePerActivityBestMeanReciprocalRankOverMUs(algoLabel, timeCategory);// s
				writePerRTBestMeanReciprocalRankOverMUs(algoLabel, timeCategory);// s
				// ALERT: make sure that correct getObservedOptimalMUForUserNum is used in
				// writePerRTRRForOptimalMUForEachUser
				String RRForEachRTForOptimalMUFile = writePerRTRRForOptimalMUForEachUser(algoLabel, timeCategory);// s
				LinkedHashMap<String, ArrayList<Double>> actRRsMap = getPerActivityEachRTKaRR(algoLabel, timeCategory,
						RRForEachRTForOptimalMUFile);
				WToFile.writeLinkedHashMapOfArrayList(actRRsMap, commonPath + "ActsRRMap.csv");
				writePerActivityBestMeanReciprocalRankOverMUs("BaselineOccurrence", timeCategory);
				writePerActivityBestMeanReciprocalRankOverMUs("BaselineDuration", timeCategory);
			}

		}
	}

	/**
	 * 
	 * @param string
	 * @param timeCategory
	 * @param rRForEachRTForOptimalMUFile
	 */
	private static LinkedHashMap<String, ArrayList<Double>> getPerActivityEachRTKaRR(String string, String timeCategory,
			String rRForEachRTForOptimalMUFile)
	{
		LinkedHashMap<String, ArrayList<Double>> actRRsMap = new LinkedHashMap<String, ArrayList<Double>>();

		String commonPath = Constant.getCommonPath();

		try
		{
			// BufferedWriter bw =
			// WritingToFile.getBufferedWriterForNewFile(commonPath + fileNamePhrase + timeCategory
			// + "PerActivityMeanReciprocalRank.csv");
			// BufferedWriter bwDistri = WritingToFile.getBufferedWriterForNewFile(commonPath + "NumOfRTsPerAct.csv");
			BufferedReader brRR = new BufferedReader(new FileReader(rRForEachRTForOptimalMUFile));// commonPath +
																									// fileNamePhrase +
																									// timeCategory +
																									// "ReciprocalRank.csv"));
			BufferedReader brDataActual = new BufferedReader(
					new FileReader(commonPath + "MatchingUnit1.0/dataActual.csv"));

			String[] activityNames = Constant.getActivityNames();

			// bw.write("User");
			// bwDistri.write("User");
			for (String s : activityNames)
			{
				if (UtilityBelt.isValidActivityName(s))
				{
					actRRsMap.put(s, new ArrayList<Double>());
					// bw.write("," + s);
					// bwDistri.write("," + s);
				}
			}
			// bw.newLine();
			// bwDistri.newLine();

			String currentRRLine, currentDataActual;
			int lineNumber = 0;
			while ((currentRRLine = brRR.readLine()) != null)
			{
				if ((currentDataActual = brDataActual.readLine()) == null)
				{
					new Exception("Error: number of lines mismatch in writePerActivityMeanReciprocalRank");
					PopUps.showException(
							new Exception("Error: number of lines mismatch in writePerActivityMeanReciprocalRank"),
							"writePerRTRRForEachActivity");
					System.exit(-1);
				}

				String[] rrValuesForThisUser = currentRRLine.split(",");
				String[] dataActualValuesForThisUser = currentDataActual.split(",");
				System.out.println("rrValuesForThisUser=" + rrValuesForThisUser.length);
				System.out.println("dataActualValuesForThisUser=" + dataActualValuesForThisUser.length);

				if (rrValuesForThisUser.length != dataActualValuesForThisUser.length)
				{
					new Exception("Error: number of tokens in line mismatch in writePerActivityMeanReciprocalRank");
					PopUps.showException(
							new Exception(
									"Error: number of tokens in line mismatch in writePerActivityMeanReciprocalRank"),
							"writePerRTRRForEachActivity");
					System.exit(-1);
				}

				// LinkedHashMap<String, ArrayList<Double>> perActMRR = new LinkedHashMap<String, ArrayList<Double>>();
				// for (String actName : activityNames)
				// {
				// if (UtilityBelt.isValidActivityName(actName))
				// perActMRR.put(actName, new ArrayList<Double>()); // initialised to maintain same order for activity
				// names
				// }

				int numOfTokens = rrValuesForThisUser.length;

				for (int tokenI = 0; tokenI < numOfTokens; tokenI++)
				{
					String actualActName = dataActualValuesForThisUser[tokenI];
					Double rrValue = Double.valueOf(rrValuesForThisUser[tokenI]);
					actRRsMap.get(actualActName).add(rrValue);
				}

				// bw.write("User_" + lineNumber);
				// bwDistri.write("User_" + lineNumber);
				//

				lineNumber++;
			}

			System.out.println("Act\t," + "Count of RRs");
			for (Map.Entry<String, ArrayList<Double>> e : actRRsMap.entrySet())
			{
				System.out.println(e.getKey() + "\t," + e.getValue().size());

			}

			brRR.close();
			brDataActual.close();

			// bwValidRTCount.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
		return actRRsMap;
	}

	// public static void main(String[] args)
	// {
	// Constant.DATABASE_NAME = "dcu_data_2";// "dcu_data_2";// "geolife1";//
	// Constant.outputCoreResultsPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June5/DCU/SimpleV3/";//
	// /run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May7_2015/DCU/Sum1/";//
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May19/Geolife/Sum1/";
	//
	// new ConnectDatabase(Constant.DATABASE_NAME); // all method and variable in this class are static
	// String commonPath = Constant.outputCoreResultsPath;
	// new Constant("", Constant.DATABASE_NAME);
	//
	// double[] matchingUnitArray = Constant.matchingUnitAsPastCount;
	//
	// for (double mu : matchingUnitArray)
	// {
	// Constant.setCommonPath(Constant.outputCoreResultsPath + "MatchingUnit" + String.valueOf(mu) + "/");
	//
	// for (String timeCategory : timeCategories)
	// {
	// writePerActivityMeanReciprocalRank("Algo", timeCategory);// s
	// writePerActivityMeanReciprocalRank("BaselineOccurrence", timeCategory);
	// writePerActivityMeanReciprocalRank("BaselineDuration", timeCategory);
	// }
	// }
	// }
	// // / Incomplete: supersided by a more generic implementation
	// private static void writePerActivityBestMeanReciprocalRankOverMUs(String string, String timeCategory)
	// {
	// double[] matchingUnitArray = Constant.matchingUnitAsPastCount;
	// LinkedHashMap<String, ArrayList<Double>> bestMRRPerActivity = new LinkedHashMap<String, ArrayList<Double>>();
	// String[] activityNames = Constant.getActivityNames();
	//
	// for (String actName : activityNames)
	// {
	// if (UtilityBelt.isValidActivityName(actName))
	// bestMRRPerActivity.put(actName, new ArrayList<Double>()); // initialised to maintain same order for activity
	// names
	// }
	//
	// try
	// {
	// String fileNamePhrase = "Algo";
	// for (double mu : matchingUnitArray)
	// {
	// String pathToRead = (Constant.outputCoreResultsPath + "MatchingUnit" + String.valueOf(mu) + "/");
	// BufferedReader brPerActMRREachMU =
	// new BufferedReader(new FileReader(pathToRead + fileNamePhrase + timeCategory +
	// "PerActivityMeanReciprocalRank.csv"));
	//
	// }
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// PopUps.showException(e, "writePerActivityBestMeanReciprocalRankOverMUs");
	// }
	// }
	/**
	 * 
	 * @param fileNamePhrase
	 *            "Algo"
	 * @param timeCategory
	 *            "All"
	 */
	private static void writePerActivityBestMeanReciprocalRankOverMUs(String fileNamePhrase, String timeCategory)
	{
		double[] matchingUnitArray = Constant.matchingUnitAsPastCount;
		String[] absCSVFileNamesToRead = new String[matchingUnitArray.length];// AlgoAllPerActivityMeanReciprocalRank.csv
		String commonPath = Constant.getCommonPath();
		try
		{
			int fileCounter = 0;
			for (double mu : matchingUnitArray)
			{
				String pathToRead = (commonPath + "MatchingUnit" + String.valueOf(mu) + "/");
				String absfileNameToRead = pathToRead + fileNamePhrase + timeCategory
						+ "PerActivityMeanReciprocalRank.csv";
				absCSVFileNamesToRead[fileCounter] = absfileNameToRead;
				fileCounter++;
			}

			String pathToRead = (commonPath + "MatchingUnit1.0" + "/");
			CSVUtils.writeMaxCellOverCSVFiles(absCSVFileNamesToRead, commonPath + "PerActivityBestMRROverMUs.csv", 2,
					19, 2, 12, pathToRead + "NumOfRTsPerAct.csv");

		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "writePerActivityBestMeanReciprocalRankOverMUs");
		}
	}

	/**
	 * 
	 * @param fileNamePhrase
	 *            "Algo"
	 * @param timeCategory
	 *            "All"
	 */
	private static void writePerRTBestMeanReciprocalRankOverMUs(String fileNamePhrase, String timeCategory)
	{
		double[] matchingUnitArray = Constant.matchingUnitAsPastCount;
		String[] absCSVFileNamesToRead = new String[matchingUnitArray.length];// AlgoAllPerActivityMeanReciprocalRank.csv
		String commonPath = Constant.getCommonPath();
		try
		{
			int fileCounter = 0;
			for (double mu : matchingUnitArray)
			{
				String pathToRead = (commonPath + "MatchingUnit" + String.valueOf(mu) + "/");
				String absfileNameToRead = pathToRead + fileNamePhrase + timeCategory + "ReciprocalRank.csv";
				absCSVFileNamesToRead[fileCounter] = absfileNameToRead;
				fileCounter++;
			}

			String pathToRead = (commonPath + "MatchingUnit1.0" + "/");
			CSVUtils.writeMaxCellOverCSVFiles(absCSVFileNamesToRead, commonPath + "PerRTBestMRROverMUs.csv", 1, 18, 1,
					197, pathToRead + "dataActual.csv");

		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "writePerRTBestMeanReciprocalRankOverMUs");
		}
	}

	/**
	 * 
	 * @param userNum
	 * @return
	 */
	public static LinkedHashMap<Integer, Integer> getObservedOptimalMUForUserNum()
	{
		LinkedHashMap<Integer, Integer> mapOfObservedOptimal = new LinkedHashMap<Integer, Integer>();
		mapOfObservedOptimal.put(1, 1);
		mapOfObservedOptimal.put(2, 30);
		mapOfObservedOptimal.put(3, 2);
		mapOfObservedOptimal.put(4, 5);
		mapOfObservedOptimal.put(5, 2);
		mapOfObservedOptimal.put(6, 2);
		mapOfObservedOptimal.put(7, 1);
		mapOfObservedOptimal.put(8, 2);
		mapOfObservedOptimal.put(9, 1);
		mapOfObservedOptimal.put(10, 3);
		mapOfObservedOptimal.put(11, 3); // for UMAP correctedTZ this is 3/
		// mapOfObservedOptimal.put(11, 4); // for UMAP submitted data this is 4/
		mapOfObservedOptimal.put(12, 0);
		mapOfObservedOptimal.put(13, 1);
		mapOfObservedOptimal.put(14, 21);
		mapOfObservedOptimal.put(15, 10);
		mapOfObservedOptimal.put(16, 1);
		mapOfObservedOptimal.put(17, 0);
		mapOfObservedOptimal.put(18, 0);

		return mapOfObservedOptimal;
	}

	/**
	 * 
	 * @param userChosenMU
	 *            csv file with userID as first col and chosen MU in second col
	 * @return
	 */
	public static LinkedHashMap<Double, Double> getObservedChosenMUForUserNumNov2018(String chosenMUFile)
	{
		List<List<Double>> res = ReadingFromFile.nColumnReaderDouble(chosenMUFile, ",", true, false);
		LinkedHashMap<Double, Double> userChosenMU = new LinkedHashMap<>();

		for (List<Double> e : res)
		{
			userChosenMU.put(e.get(0), e.get(1));
		}

		System.out.println("userChosenMU.size() = " + userChosenMU.size());
		System.out.println("userChosenMU = " + userChosenMU);
		return userChosenMU;
	}

	/**
	 * 
	 * @param fileNamePhrase
	 *            "Algo"
	 * @param timeCategory
	 *            "All"
	 */
	private static String writePerRTRRForOptimalMUForEachUser(String fileNamePhrase, String timeCategory)
	{
		// double[] matchingUnitArray = Constant.matchingUnitAsPastCount;
		// String[] absCSVFileNamesToRead = new String[matchingUnitArray.length];//
		// AlgoAllPerActivityMeanReciprocalRank.csv
		String commonPath = Constant.getCommonPath();

		try
		{
			LinkedHashMap<Integer, Integer> mapOfUserOptimalMU = getObservedOptimalMUForUserNum();
			BufferedWriter MRRAtOptimalMUForRTsFile = WToFile
					.getBWForNewFile(commonPath + "RRAtOptimalMUForRTsFile.csv");

			for (int user = 1; user <= 18; user++)
			{

				Integer optimalMuForUser = mapOfUserOptimalMU.get(user);

				String pathToRead = (commonPath + "MatchingUnit" + String.valueOf(new Double(optimalMuForUser)) + "/");
				String absfileNameToRead = pathToRead + fileNamePhrase + timeCategory + "ReciprocalRank.csv";
				// BufferedReader brRR = new BufferedReader(new FileReader(absfileNameToRead));// commonPath +
				// fileNamePhrase + timeCategory + "ReciprocalRank.csv"));

				System.out.println(
						"User " + user + " optimal mu:" + optimalMuForUser + "Have to read " + absfileNameToRead);

				CSVRecord rowRecord = CSVUtils.getRowFromCSVFile(absfileNameToRead, user);
				MRRAtOptimalMUForRTsFile.write(CSVUtils.CSVRecordToString(rowRecord, ",") + "\n");

				// brRR.close();

			}
			MRRAtOptimalMUForRTsFile.close();
			// int fileCounter = 0;
			// for (double mu : matchingUnitArray)
			// {
			// String pathToRead = (commonPath + "MatchingUnit" + String.valueOf(mu) + "/");
			// String absfileNameToRead = pathToRead + fileNamePhrase + timeCategory + "ReciprocalRank.csv";
			// absCSVFileNamesToRead[fileCounter] = absfileNameToRead;
			// fileCounter++;
			// }
			//
			// String pathToRead = (commonPath + "MatchingUnit1.0" + "/");
			// writeMaxCellOverCSVFiles(absCSVFileNamesToRead, commonPath + "PerRTBestMRROverMUs.csv", 1, 18, 1, 197,
			// pathToRead
			// + "dataActual.csv");

		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "writePerRTMRRForOptimalMUForEachUser");
		}

		return (commonPath + "RRAtOptimalMUForRTsFile.csv");
	}
}
