package org.activity.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activity.constants.Constant;
import org.activity.constants.VerbosityConstants;
import org.activity.io.CSVUtils;
import org.activity.io.WritingToFile;
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
		GowallaEvalCurrentEqualsTargetPerformance();
		PopUps.showMessage("All done");
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
			BufferedWriter allTogetherForAnalysis = WritingToFile.getBufferedWriterForExistingFile(
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
			String path = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/MUExperimentsBLNCount/Iteration1ForShow/Geolife/SimpleV3/"; // rexperiment
																																				// UMAP
																																				// corrected
																																				// TZ

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

				for (String timeCategory : timeCategories)
				{
					writePerActivityMeanReciprocalRank("Algo", timeCategory);
					writePerActivityMeanReciprocalRank("BaselineOccurrence", timeCategory);
					writePerActivityMeanReciprocalRank("BaselineDuration", timeCategory);
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
				WritingToFile.writeLinkedHashMapOfArrayList(actRRsMap, commonPath + "ActsRRMap.csv");
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
			BufferedWriter MRRAtOptimalMUForRTsFile = WritingToFile
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

	/**
	 * executed post execution of experiments raw values checked
	 * 
	 * @param fileNamePhrase
	 *            "Algo","BaselineOccurrence","BaselineDuration"
	 * @param timeCategory
	 *            { "All", "Morning", "Afternoon", "Evening" };
	 */
	public static void writePerActivityMeanReciprocalRank(String fileNamePhrase, String timeCategory)// , int numUsers)

	{
		// BufferedReader br= null;
		String commonPath = Constant.getCommonPath();

		try
		{
			BufferedWriter bw = WritingToFile
					.getBWForNewFile(commonPath + fileNamePhrase + timeCategory + "PerActivityMeanReciprocalRank.csv");
			BufferedWriter bwDistri = WritingToFile.getBWForNewFile(commonPath + "NumOfRTsPerAct.csv");
			BufferedReader brRR = new BufferedReader(
					new FileReader(commonPath + fileNamePhrase + timeCategory + "ReciprocalRank.csv"));
			BufferedReader brDataActual = new BufferedReader(new FileReader(commonPath + "dataActual.csv"));

			String[] activityNames = Constant.getActivityNames();

			bw.write("User");
			bwDistri.write("User");
			for (String s : activityNames)
			{
				if (UtilityBelt.isValidActivityName(s))
				{
					bw.write("," + s);
					bwDistri.write("," + s);
				}
			}
			bw.newLine();
			bwDistri.newLine();

			String currentRRLine, currentDataActual;
			int lineNumber = 0;
			while ((currentRRLine = brRR.readLine()) != null)
			{
				if ((currentDataActual = brDataActual.readLine()) == null)
				{
					new Exception("Error: number of lines mismatch in writePerActivityMeanReciprocalRank");
					PopUps.showException(
							new Exception("Error: number of lines mismatch in writePerActivityMeanReciprocalRank"),
							"writePerActivityMeanReciprocalRank");
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
							"writePerActivityMeanReciprocalRank");
					System.exit(-1);
				}

				LinkedHashMap<String, ArrayList<Double>> perActMRR = new LinkedHashMap<String, ArrayList<Double>>();
				for (String actName : activityNames)
				{
					if (UtilityBelt.isValidActivityName(actName)) perActMRR.put(actName, new ArrayList<Double>()); // initialised
																													// to
																													// maintain
																													// same
																													// order
																													// for
																													// activity
																													// names
				}

				int numOfTokens = rrValuesForThisUser.length;

				for (int tokenI = 0; tokenI < numOfTokens; tokenI++)
				{
					String actualActName = dataActualValuesForThisUser[tokenI];
					Double rrValue = Double.valueOf(rrValuesForThisUser[tokenI]);
					perActMRR.get(actualActName).add(rrValue);
				}

				bw.write("User_" + lineNumber);
				bwDistri.write("User_" + lineNumber);

				for (Map.Entry<String, ArrayList<Double>> e : perActMRR.entrySet())
				{
					System.out.println(" number of vals =" + e.getValue().size());
					bw.write("," + String.valueOf(StatsUtils.meanOfArrayList(e.getValue(), 4)));
					bwDistri.write("," + String.valueOf(e.getValue().size()));
				}
				bw.newLine();
				bwDistri.newLine();
				lineNumber++;
			}

			brRR.close();
			brDataActual.close();
			bw.close();
			bwDistri.close();
			// bwValidRTCount.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
