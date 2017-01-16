package org.activity.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activity.io.WritingToFile;
import org.activity.ui.PopUps;
import org.activity.util.CSVUtils;
import org.activity.util.ConnectDatabase;
import org.activity.util.Constant;
import org.activity.util.UtilityBelt;
import org.apache.commons.csv.CSVRecord;

/**
 * 
 * @author gunjan
 *
 */
public class EvaluationPostExperiment
{
	public static final String[] timeCategories =
	{ "All" };// , "Morning", "Afternoon", "Evening" };

	public static void main(String args[])
	{
		GowallaEvalCurrentEqualsTargetPerformance();
	}

	/**
	 * Evaluate the performance of recommendations when current activity = target activity
	 */
	public static void GowallaEvalCurrentEqualsTargetPerformance()
	{
		Constant.setDatabaseName("gowalla1");

		String rootPathToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";
		String s[] =
		{ "101", "201", "301", "401", "501", "601", "701", "801", "901" };

		String path = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";

		for (String shead : s)
		{
			Constant.outputCoreResultsPath = path + shead + "/";
			double[] matchingUnitArray = Constant.matchingUnitAsPastCount;

			for (double mu : matchingUnitArray)
			{
				Constant.setCommonPath(Constant.outputCoreResultsPath + "MatchingUnit" + String.valueOf(mu) + "/");

				for (String timeCategory : timeCategories)
				{
					System.out.println(" " + Constant.getCommonPath());

					// writePerActivityMeanReciprocalRank("Algo", timeCategory);
					// writePerActivityMeanReciprocalRank("BaselineOccurrence", timeCategory);
					// // writePerActivityMeanReciprocalRank("BaselineDuration", timeCategory);
				}
			}
			Constant.setCommonPath(Constant.outputCoreResultsPath);
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

			Constant.outputCoreResultsPath = path;// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June5/DCU/SimpleV3/";//
													// /run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May7_2015/DCU/Sum1/";//
													// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May19/Geolife/Sum1/";
			System.out.println("path=" + path);
			ConnectDatabase.initialise(Constant.getDatabaseName());// .DATABASE_NAME); // all method and variable in
																	// this class are static
			String commonPath = Constant.outputCoreResultsPath;
			Constant.initialise("", Constant.getDatabaseName());// .DATABASE_NAME);

			double[] matchingUnitArray = Constant.matchingUnitAsPastCount;
			// Disabled as already written
			for (double mu : matchingUnitArray)
			{
				Constant.setCommonPath(Constant.outputCoreResultsPath + "MatchingUnit" + String.valueOf(mu) + "/");

				for (String timeCategory : timeCategories)
				{
					writePerActivityMeanReciprocalRank("Algo", timeCategory);
					writePerActivityMeanReciprocalRank("BaselineOccurrence", timeCategory);
					writePerActivityMeanReciprocalRank("BaselineDuration", timeCategory);
				}
			}
			Constant.setCommonPath(Constant.outputCoreResultsPath);// + "MatchingUnit" + String.valueOf(mu) + "/");

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
					.getBufferedWriterForNewFile(commonPath + "RRAtOptimalMUForRTsFile.csv");

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
				MRRAtOptimalMUForRTsFile.write(UtilityBelt.CSVRecordToString(rowRecord, ",") + "\n");

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
			BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(
					commonPath + fileNamePhrase + timeCategory + "PerActivityMeanReciprocalRank.csv");
			BufferedWriter bwDistri = WritingToFile.getBufferedWriterForNewFile(commonPath + "NumOfRTsPerAct.csv");
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
					bw.write("," + String.valueOf(UtilityBelt.meanOfArrayList(e.getValue(), 4)));
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
