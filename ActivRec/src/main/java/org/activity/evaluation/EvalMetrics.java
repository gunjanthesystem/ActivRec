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
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.constants.DomainConstants;
import org.activity.constants.VerbosityConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.UtilityBelt;

public class EvalMetrics
{

	/**
	 * 
	 * Moved from org.activity.evaluation.EvaluationPostExperiment and refactored.
	 * <p>
	 * executed post execution of experiments (raw values checked was checked before refactoring it on 24 Aug 2018).
	 * TODO: sanity check again.
	 * <p>
	 * Reads: ReciprocalRank.csv
	 * <p>
	 * Writes: PerActivityMeanReciprocalRank.csv, NumOfRTsPerAct.csv
	 * 
	 * @param fileNamePhrase
	 *            "Algo","BaselineOccurrence","BaselineDuration"
	 * @param timeCategory
	 *            { "All", "Morning", "Afternoon", "Evening" };
	 * @param arrayActual
	 * @param dimensionPhrase
	 * @param commonPath
	 * @param catIDNameDict
	 *            added on 14 Nov 2018, since Geolife act names are not same as act ID
	 */
	public static void writePerActMRRV2(String fileNamePhrase, String timeCategory, /* String[] activityNames, */
			ArrayList<ArrayList<String>> arrayActual, String dimensionPhrase, String commonPath,
			TreeMap<Integer, String> catIDNameDict)
	// , int numUsers)
	{
		boolean verbose = true;// for sanity check
		// BufferedReader br= null;//String commonPath = Constant.getCommonPath();
		try
		{
			BufferedReader brRR = new BufferedReader(new FileReader(
					commonPath + fileNamePhrase + timeCategory + "ReciprocalRank" + dimensionPhrase + ".csv"));
			// BufferedReader brDataActual = new BufferedReader(
			// new FileReader(commonPath + "dataActual" + dimensionPhrase + ".csv"));

			BufferedWriter bw = WToFile.getBWForNewFile(commonPath + fileNamePhrase + timeCategory
					+ "PerActivityMeanReciprocalRank" + dimensionPhrase + ".csv");
			BufferedWriter bwDistri = WToFile.getBWForNewFile(
					commonPath + fileNamePhrase + timeCategory + "NumOfRTsPerAct" + dimensionPhrase + ".csv");
			// String[] activityNames = Constant.getActivityNames();

			bw.write("User");
			bwDistri.write("User");
			// PopUps.showMessage("catIDNameDict = " + catIDNameDict.toString());
			// for (String s : activityNames)
			for (Entry<Integer, String> cEntry : catIDNameDict.entrySet())
			{
				if (UtilityBelt.isValidActivityName(cEntry.getValue()))
				{
					String s = String.valueOf(cEntry.getKey() + "-" + cEntry.getValue());
					bw.write("," + s);
					bwDistri.write("," + s);
				}
			}
			bw.newLine();
			bwDistri.newLine();

			String currentRRLine;// , currentDataActual;
			int lineNumber = 0;
			while ((currentRRLine = brRR.readLine()) != null)
			{
				ArrayList<String> dataActualForThisUser = arrayActual.get(lineNumber);

				if (dataActualForThisUser == null)
				{
					new Exception("Error: number of lines mismatch in writePerActivityMeanReciprocalRank");
					PopUps.showException(
							new Exception("Error: number of lines mismatch in writePerActivityMeanReciprocalRank"),
							"writePerActivityMeanReciprocalRank");
					System.exit(-1);
				}

				String[] rrValuesForThisUser = currentRRLine.split(",");
				// String[] dataActualValuesForThisUser = currentDataActual.split(",");

				if (verbose)
				{
					System.out.println("currentRRLine = " + currentRRLine + "\nrrValuesForThisUser = "
							+ Arrays.asList(rrValuesForThisUser) + "\ndataActualForThisUser = "
							+ dataActualForThisUser);
				}

				System.out.println("rrValuesForThisUser.size=" + rrValuesForThisUser.length);
				System.out.println("dataActualForThisUser.size=" + dataActualForThisUser.size());

				if (rrValuesForThisUser.length != dataActualForThisUser.size())
				{
					new Exception("Error: number of tokens in line mismatch in writePerActMRRV2");
					PopUps.showException(new Exception("Error: number of tokens in line mismatch in writePerActMRRV2"),
							"writePerActivityMeanReciprocalRank");
					System.exit(-1);
				}

				LinkedHashMap<String, ArrayList<Double>> perActMRR = new LinkedHashMap<>();
				// initialised to maintain same order for activity names
				// for (String actName : activityNames)
				// {
				// if (UtilityBelt.isValidActivityName(actName))
				// {
				// perActMRR.put(actName, new ArrayList<Double>());
				// }
				// }

				// Start of Dec 16 debugging
				// $System.out.println("catIDNameDict = " + catIDNameDict);
				// End of Dec 16 debugging

				for (Entry<Integer, String> cEntry : catIDNameDict.entrySet())
				{
					if (UtilityBelt.isValidActivityName(cEntry.getValue()))
					{
						perActMRR.put(String.valueOf(cEntry.getKey()), new ArrayList<Double>());// actID as key
					}
				}

				int numOfTokens = rrValuesForThisUser.length;

				for (int tokenI = 0; tokenI < numOfTokens; tokenI++)
				{
					String actualActName = dataActualForThisUser.get(tokenI);// dataActualValuesForThisUser[tokenI];
					Double rrValue = Double.valueOf(rrValuesForThisUser[tokenI]);

					// ignore RR which are less than 0 which is indicative of no recommendation for the RT
					if (rrValue < 0)// added on 14 March 2019
					{
						continue;
					}
					// Start of Dec 16 debugging
					if (false)
					{
						System.out.println("dataActualForThisUser = " + dataActualForThisUser);
						System.out.println("actualActName = " + actualActName);
						System.out.println("rrValue = " + rrValue);
						System.out.println("perActMRR = " + perActMRR);
					} // End of Dec 16 debugging

					// start of debug Mar 14 2019
					if (perActMRR == null)
					{
						System.out.println("perActMRR==null");
					}
					if (perActMRR.containsKey(actualActName) == false)
					{
						System.out.println("perActMRR.containsKey(actualActName) == false");
					}
					// end of debug Mar 14 2019

					ArrayList<Double> listOfRRsForThisAct = perActMRR.get(actualActName);
					if (listOfRRsForThisAct == null)
					{
						System.out.println("listOfRRsForThisAct == null");
					}
					if (rrValue == null)
					{
						System.out.println("rrValue == null");
					}
					listOfRRsForThisAct.add(rrValue);
					// perActMRR.get(actualActName).add(rrValue);

				}

				bw.write("User_" + lineNumber);
				bwDistri.write("User_" + lineNumber);

				for (Map.Entry<String, ArrayList<Double>> e : perActMRR.entrySet())
				{
					// $System.out.println(" number of vals =" + e.getValue().size());
					bw.write("," + String.valueOf(StatsUtils.meanOfArrayList(e.getValue(), 4)));
					bwDistri.write("," + String.valueOf(e.getValue().size()));
				}
				bw.newLine();
				bwDistri.newLine();
				lineNumber++;
			}

			brRR.close();
			// brDataActual.close();
			bw.close();
			bwDistri.close();
			// bwValidRTCount.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
			PopUps.showException(e, "writePerActMRRV2");
		}
	}

	/**
	 * 
	 * Moved from org.activity.evaluation.EvaluationPostExperiment and refactored.
	 * <p>
	 * executed post execution of experiments (raw values checked was checked before refactoring it on 24 Aug 2018).
	 * TODO: sanity check again.
	 * <p>
	 * Reads: ReciprocalRank.csv, dataActual.csv
	 * <p>
	 * Writes: PerActivityMeanReciprocalRank.csv, NumOfRTsPerAct.csv
	 * 
	 * Note: can be superceded by writePerActMRRV2()
	 * 
	 * @param fileNamePhrase
	 *            "Algo","BaselineOccurrence","BaselineDuration"
	 * @param timeCategory
	 *            { "All", "Morning", "Afternoon", "Evening" };
	 * 
	 * @param activityNames
	 * @param dimensionPhrase
	 * @param commonPath
	 * @param nextKRecommendation
	 *            added on Nov 15 2018 for Step0, Step1, Step2
	 * 
	 */
	public static void writePerActMRR(String fileNamePhrase, String timeCategory, String[] activityNames,
			String dimensionPhrase, String commonPath, String nextKRecommendation)
	// , int numUsers)
	{
		boolean verbose = true;// for sanity check
		// BufferedReader br= null;
		// String commonPath = Constant.getCommonPath();
		try
		{
			BufferedReader brRR = new BufferedReader(new FileReader(
					commonPath + fileNamePhrase + timeCategory + "ReciprocalRank" + dimensionPhrase + ".csv"));
			BufferedReader brDataActual = new BufferedReader(
					new FileReader(commonPath + "dataActual" + dimensionPhrase + ".csv"));

			BufferedWriter bw = WToFile.getBWForNewFile(commonPath + fileNamePhrase + timeCategory
					+ "PerActivityMeanReciprocalRank" + dimensionPhrase + ".csv");
			BufferedWriter bwDistri = WToFile.getBWForNewFile(commonPath + "NumOfRTsPerAct" + dimensionPhrase + ".csv");
			// String[] activityNames = Constant.getActivityNames();

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

				if (verbose)
				{
					System.out.println("currentRRLine = " + currentRRLine + "\nrrValuesForThisUser = "
							+ Arrays.asList(rrValuesForThisUser) + "\ncurrentDataActual = " + currentDataActual
							+ "\ndataActualValuesForThisUser = " + Arrays.asList(dataActualValuesForThisUser));
				}

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
				// initialised to maintain same order for activity names
				for (String actName : activityNames)
				{
					if (UtilityBelt.isValidActivityName(actName))
					{
						perActMRR.put(actName, new ArrayList<Double>());
					}
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

	/**
	 * THIS MIGHT HAVE THE POSSIBILITY OF OPTIMISATION
	 * <p>
	 * NOTE: Reads ReciprocalRank file.
	 * <p>
	 * Reads: MeanReciprocalRank.csv
	 * <p>
	 * Writes: ReciprocalRank.csv
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 * @param dimensionPhrase
	 * @param commonPath
	 * 
	 */
	public static void writeMeanReciprocalRank(String fileNamePhrase, String timeCategory, int numUsers,
			String dimensionPhrase, String commonPath)
	{
		// String commonPath = Constant.getCommonPath();
		// LinkedHashMap<String,Integer> numOfNeg

		try
		{
			BufferedWriter bw = WToFile.getBWForNewFile(
					commonPath + fileNamePhrase + timeCategory + "MeanReciprocalRank" + dimensionPhrase + ".csv");
			bw.write(",MRR\n");

			for (int user = 0; user < numUsers; user++)
			{
				bw.write("User_" + user + ","); // TODO: currently this starts from User_0, change it to start from
												// User_1 but this will also require necessary changes in other places

				BufferedReader br = new BufferedReader(new FileReader(
						commonPath + fileNamePhrase + timeCategory + "ReciprocalRank" + dimensionPhrase + ".csv"));
				// TODO: take br out of this loop and check it affects
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
	 * @param commonPath
	 */
	public static void writeReciprocalRank(String fileNamePhrase, String timeCategory,
			ArrayList<ArrayList<String>> arrayMeta, ArrayList<ArrayList<String>> arrayTopK,
			ArrayList<ArrayList<String>> arrayActual, String dimensionPhrase, String commonPath)
	{
		// String commonPath = Constant.getCommonPath();
		BufferedWriter bwRR = null;
		BufferedWriter bwEmptyRecomms = null;
		StringBuilder sbUnrolledRes = new StringBuilder("UserID,RTDate,RTTime,ActualAct,RR\n");// added on 14 Jan 2019

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
					int hourOfTheDay = EvalMetrics.getHourFromMetaString(currentLineArray.get(j));
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
						sbUnrolledRes
								.append(currentLineArray.get(j).replaceAll("_", ",") + "," + actual + "," + RR + "\n");
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

			WToFile.writeToNewFile(sbUnrolledRes.toString(),
					commonPath + fileNamePhrase + timeCategory + "ReciprocalRankUnrolled" + dimensionPhrase + ".csv");

			/////////// start of added on 15 Jan 2019
			// fileNamePhrase = fileNamePhrase + timeCategory + metricName + "Unrolled" + dimensionPhrase
			String metricName = "ReciprocalRank";
			summarizeByUserAndActualV2(fileNamePhrase + timeCategory + metricName + "Unrolled" + dimensionPhrase,
					commonPath, 4, 3, 0);
			// metricName);
			/////////// end of added on 15 Jan 2019
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// /**
	// *
	// * @param fileNamePhrase
	// * @param commonPath
	// * @since 15 jan 2019
	// * @deprecated
	// */
	// public static void summarizeByUserAndActual(String fileNamePhrase, String commonPath)// , String metricName)
	// {
	// // fileNamePhrase = fileNamePhrase + timeCategory + metricName + "Unrolled" + dimensionPhrase
	// List<List<String>> allRRRead = ReadingFromFile.readLinesIntoListOfLists(commonPath + fileNamePhrase + ".csv",
	// ",");
	//
	// LinkedHashMap<String, DoubleSummaryStatistics> allRRGrpByUser = allRRRead.stream().skip(1)
	// .collect(Collectors.groupingBy(e -> String.valueOf(e.get(0)), LinkedHashMap::new,
	// Collectors.summarizingDouble(e -> Double.valueOf(e.get(4)))));
	// LinkedHashMap<String, DoubleSummaryStatistics> allRRGrpByTargetAct = allRRRead.stream().skip(1)
	// .collect(Collectors.groupingBy(e -> String.valueOf(e.get(3)), LinkedHashMap::new,
	// Collectors.summarizingDouble(e -> Double.valueOf(e.get(4)))));
	//
	// WToFile.writeToNewFile(
	// "User,MRR,Count\n" + allRRGrpByUser.entrySet().stream()
	// .map(e -> e.getKey() + "," + StatsUtils.round(e.getValue().getAverage(), 4) + ","
	// + e.getValue().getCount())
	// .collect(Collectors.joining("\n")),
	// commonPath + fileNamePhrase + "MeanPerUser.csv");
	//
	// WToFile.writeToNewFile(
	// "Actual,MRR,Count\n" + allRRGrpByTargetAct.entrySet().stream()
	// .map(e -> e.getKey() + "," + StatsUtils.round(e.getValue().getAverage(), 4) + ","
	// + e.getValue().getCount())
	// .collect(Collectors.joining("\n")),
	// commonPath + fileNamePhrase + "MeanPerActual.csv");
	// }

	/**
	 * Fork of summarizeByUserAndActual
	 * <p>
	 * ignore RR < 0
	 * 
	 * @param fileNamePhrase
	 * @param commonPath
	 * @param indexOfRR
	 * @param indexOfTargetAct
	 * @param indexOfUser
	 * @since 14 Mar 2019
	 */
	public static void summarizeByUserAndActualV2(String fileNamePhrase, String commonPath, int indexOfRR,
			int indexOfTargetAct, int indexOfUser)// , String metricName)
	{
		// fileNamePhrase = fileNamePhrase + timeCategory + metricName + "Unrolled" + dimensionPhrase
		List<List<String>> allRRRead = ReadingFromFile.readLinesIntoListOfLists(commonPath + fileNamePhrase + ".csv",
				",");
		// 4,3,0
		//////////////////////////////////////////////////

		List<List<String>> allRRReadOnlyValidRRs = allRRRead.stream().skip(1)
				.filter(v -> Double.valueOf(v.get(indexOfRR)) >= 0).collect(Collectors.toList());

		/// temp start
		// List<List<String>> allRRReadOnlyValidRRs = allRRRead.stream().skip(1).collect(Collectors.toList());
		/// temp end

		//////////////////////////////////////////////////
		LinkedHashMap<String, DoubleSummaryStatistics> allRRGrpByUser = allRRReadOnlyValidRRs.stream()
				.collect(Collectors.groupingBy(e -> String.valueOf(e.get(indexOfUser)), LinkedHashMap::new,
						Collectors.summarizingDouble(e -> Double.valueOf(e.get(indexOfRR)))));
		LinkedHashMap<String, DoubleSummaryStatistics> allRRGrpByTargetAct = allRRReadOnlyValidRRs.stream()
				.collect(Collectors.groupingBy(e -> String.valueOf(e.get(indexOfTargetAct)), LinkedHashMap::new,
						Collectors.summarizingDouble(e -> Double.valueOf(e.get(indexOfRR)))));

		WToFile.writeToNewFile(
				"User,MRR,Count\n" + allRRGrpByUser.entrySet().stream()
						.map(e -> e.getKey() + "," + StatsUtils.round(e.getValue().getAverage(), 4) + ","
								+ e.getValue().getCount())
						.collect(Collectors.joining("\n")),
				commonPath + fileNamePhrase + "MeanPerUser.csv");

		WToFile.writeToNewFile(
				"Actual,MRR,Count\n" + allRRGrpByTargetAct.entrySet().stream()
						.map(e -> e.getKey() + "," + StatsUtils.round(e.getValue().getAverage(), 4) + ","
								+ e.getValue().getCount())
						.collect(Collectors.joining("\n")),
				commonPath + fileNamePhrase + "MeanPerActual.csv");
	}

	/**
	 * 
	 * @param fileNamePhrase
	 * @param commonPath
	 * @param indexOfRR
	 * @param indexOfTargetAct
	 * @param pathToCatIDNameDict
	 */
	public static void distributionByActual(String fileNamePhrase, String commonPath, int indexOfRR,
			int indexOfTargetAct, String pathToCatIDNameDict)// , String metricName)
	{
		// fileNamePhrase = fileNamePhrase + timeCategory + metricName + "Unrolled" + dimensionPhrase
		List<List<String>> allRRRead = ReadingFromFile.readLinesIntoListOfLists(commonPath + fileNamePhrase + ".csv",
				",");

		//////////////////////////////////////////////////
		List<List<String>> allRRReadOnlyValidRRs = allRRRead.stream().skip(1)
				.filter(v -> Double.valueOf(v.get(indexOfRR)) >= 0).collect(Collectors.toList());
		//////////////////////////////////////////////////
		// int indexOfRR = 4;
		// int indexOfTargetAct = 3;

		// LinkedHashMap<String, DoubleSummaryStatistics> allRRGrpByUser = allRRRead.stream().skip(1)
		// .collect(Collectors.groupingBy(e -> String.valueOf(e.get(0)), LinkedHashMap::new,
		// Collectors.summarizingDouble(e -> Double.valueOf(e.get(4)))));
		// LinkedHashMap<String, DoubleSummaryStatistics> allRRGrpByTargetAct = allRRRead.stream().skip(1)
		// .collect(Collectors.groupingBy(e -> String.valueOf(e.get(3)), LinkedHashMap::new,
		// Collectors.summarizingDouble(e -> Double.valueOf(e.get(4)))));

		// listPerTargetAct =
		// allRRRead.stream().skip(1).collect(Collectors.groupingBy(e -> e.get(indexOfTargetAct), LinkedHashMap::new,
		// Collectors.summarizingDouble(e -> Double.valueOf(e.get(indexOfRR)))));
		LinkedHashMap<String, List<String>> listRRPerTargetAct = allRRReadOnlyValidRRs.stream()
				.collect(Collectors.groupingBy(e -> e.get(indexOfTargetAct), LinkedHashMap::new,
						Collectors.mapping(e -> e.get(indexOfRR), Collectors.toList())));
		// allRRRead.stream().skip(1).collect(Collectors.groupingBy(e -> e.get(3), Collectors.toList()));
		// e -> String.valueOf(e.get(3))));
		// , // LinkedHashMap::new, Collectors.toList()));
		// WToFile.writeToNewFile(
		// "User,MRR,Count\n" + allRRGrpByUser.entrySet().stream()
		// .map(e -> e.getKey() + "," + StatsUtils.round(e.getValue().getAverage(), 4) + ","
		// + e.getValue().getCount())
		// .collect(Collectors.joining("\n")),
		// commonPath + fileNamePhrase + "MeanPerUser.csv");

		// remove -1 RR
		// listRRPerTargetAct.entrySet().stream().collect(Collectors.mapping(e -> e.getKey(),));
		// LinkedHashMap<String, List<String>> listRRPerTargetActInvalidsRemoved = new LinkedHashMap<>(
		// listRRPerTargetAct.size());
		//
		// for (Entry<String, List<String>> e : listRRPerTargetAct.entrySet())
		// {
		// listRRPerTargetActInvalidsRemoved.put(e.getKey(),
		// e.getValue().stream().filter(v -> Double.valueOf(v.trim()) < 0).collect(Collectors.toList()));
		// }

		List<List<String>> catIDNameData = ReadingFromFile.readLinesIntoListOfLists(pathToCatIDNameDict, ",");
		int indexOfCatID = 0;
		int indexOfCatName = 1;
		Map<String, String> catIDNameMap = catIDNameData.stream()
				.collect(Collectors.toMap(e -> e.get(indexOfCatID), e -> e.get(indexOfCatName)));

		StringBuilder sb = new StringBuilder("Actual,ActualName,RR\n");

		for (Entry<String, List<String>> e : listRRPerTargetAct.entrySet())
		{
			String actID = e.getKey().trim();
			String actName = catIDNameMap.getOrDefault(actID, "NotFoundInCatIDNameMap:" + actID);

			for (String le : e.getValue())
			{
				sb.append(actID + "," + actName + "," + le + "\n");
			}
		}

		WToFile.writeToNewFile(sb.toString(), commonPath + fileNamePhrase + "ListRRPerActual.csv");
		// WToFile.writeToNewFile(
		// "Actual,MRR,Count\n" + allRRGrpByTargetAct.entrySet().stream()
		// .map(e -> e.getKey() + "," + StatsUtils.round(e.getValue().getAverage(), 4) + ","
		// + e.getValue().getCount())
		// .collect(Collectors.joining("\n")),
		// commonPath + fileNamePhrase + "MeanPerActual.csv");
	}

	/**
	 * Calculate and write the metrics:precision, recall and fmeasure values, for the recommendations generated for each
	 * user for each recommendation time. (note: also write a file containing the count of number of recommendation
	 * times considered for the given parameters passsed to this method.)
	 * 
	 * <p>
	 * Reads:
	 * 
	 * Writes:"top" + theK + "Precision.csv", "top" + theK + "Recall.csv", "top" + theK + "FMeasure.csv"
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
			ArrayList<ArrayList<String>> arrayActual, String commonPath)
	{
		// BufferedReader brMeta = null, brTopK = null, brActual = null;
		// String commonPath = Constant.getCommonPath();
		BufferedWriter bwTopKPrecision = null, bwTopKRecall = null, bwTopKF = null,
				bwNumberOfRecommendationTimes = null;

		// added on 14 Jan 2019
		StringBuilder sbUnrolledPrecision = new StringBuilder("UserID,RTDate,RTTime,ActualAct,Precision\n");
		StringBuilder sbUnrolledRecall = new StringBuilder("UserID,RTDate,RTTime,ActualAct,Recall\n");
		StringBuilder sbUnrolledF = new StringBuilder("UserID,RTDate,RTTime,ActualAct,F\n");// added on 14 Jan 2019

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
					int hourOfTheDay = EvalMetrics.getHourFromMetaString(currentLineArray.get(j));
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
							topKRecallVal = round((double) countOfOccurence / 1, 4); // since there is
																						// only one actual
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

						sbUnrolledPrecision.append(currentLineArray.get(j).replaceAll("_", ",") + "," + actual + ","
								+ topKPrecisionVal + "\n");
						sbUnrolledRecall.append(currentLineArray.get(j).replaceAll("_", ",") + "," + actual + ","
								+ topKRecallVal + "\n");
						sbUnrolledF.append(
								currentLineArray.get(j).replaceAll("_", ",") + "," + actual + "," + topKFVal + "\n");

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

			WToFile.writeToNewFile(sbUnrolledPrecision.toString(),
					commonPath + fileNamePhrase + timeCategory + "top" + theK + "PrecisionUnrolled.csv");
			WToFile.writeToNewFile(sbUnrolledRecall.toString(),
					commonPath + fileNamePhrase + timeCategory + "top" + theK + "RecallUnrolled.csv");
			WToFile.writeToNewFile(sbUnrolledF.toString(),
					commonPath + fileNamePhrase + timeCategory + "top" + theK + "FMeasureUnrolled.csv");

			// ReadingFromFile.readLinesIntoListOfLists(absFileName, ",");
			// groupByUserAndActual(fileNamePhrase, timeCategory, dimensionPhrase, commonPath, "ReciprocalRank");
			String[] metricNames = { "Precision", "Recall", "FMeasure" };
			for (String metricName : metricNames)
			{
				summarizeByUserAndActualV2(fileNamePhrase + timeCategory + "top" + theK + metricName + "Unrolled",
						commonPath, 4, 3, 0);// metricName);
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
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 */
	public static void writeAvgRecallsForAllKs(String fileNamePhrase, String timeCategory, int numUsers,
			String dimensionPhrase, String commonPath)
	{
		// BufferedReader br= null;
		// String commonPath = Constant.getCommonPath();
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
			bw.write("User");
			bwValidRTCount.write("User");
			for (int K = EvaluationSeq.theKOriginal; K > 0; K--)
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

				for (int K = EvaluationSeq.theKOriginal; K > 0; K--)
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
	 * Reads:
	 * <p>
	 * Writes: AvgPrecision.csv, AvgPrecisionCountValidRT.csv
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 * @param dimensionPhrase
	 * @param commonPath
	 */
	public static void writeAvgPrecisionsForAllKs(String fileNamePhrase, String timeCategory, int numUsers,
			String dimensionPhrase, String commonPath)
	{
		// BufferedReader br= null;
		// String commonPath = Constant.getCommonPath();
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
			bw.write("User");
			bwValidRTCount.write("User");
			for (int K = EvaluationSeq.theKOriginal; K > 0; K--)
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
				for (int K = EvaluationSeq.theKOriginal; K > 0; K--)
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
	 * Reads: "top" + K + "FMeasure.csv"
	 * 
	 * Writes: AvgFMeasure.csv, AvgFMeasureCountValidRT.csv
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param numUsers
	 */
	public static void writeAvgFMeasuresForAllKs(String fileNamePhrase, String timeCategory, int numUsers,
			String dimensionPhrase, String commonPath)
	{
		// BufferedReader br= null;
		// String commonPath = Constant.getCommonPath();
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
			bw.write("User");
			bwValidRTCount.write("User");
			for (int K = EvaluationSeq.theKOriginal; K > 0; K--)
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
				for (int K = EvaluationSeq.theKOriginal; K > 0; K--)
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

	/**
	 * 
	 * @param catID1
	 * @param catID2
	 * @param levelAtWhichToMatch
	 * @return
	 */
	public static boolean isAgree(String catID1, String catID2, int levelAtWhichToMatch)
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
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayMeta
	 * @param arrayRecommendedSeq
	 * @param arrayActualSeq
	 * @param levelAtWhichToMatch
	 * @return ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements
	 */
	public static ArrayList<ArrayList<ArrayList<Integer>>> computeDirectAgreements(String fileNamePhrase,
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
					int hourOfTheDay = EvalMetrics.getHourFromMetaString(currentMetaLineArray.get(j));

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
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayDirectAgreements
	 * @param pathToWrite
	 */
	public static void writeNumAndPercentageDirectAgreements(String fileNamePhrase, String timeCategory,
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
	public static void writeDirectAgreements(String fileNamePhrase, String timeCategory,
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
	public static void writeDirectTopKAgreements(String fileNamePhrase, String timeCategory,
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

}
