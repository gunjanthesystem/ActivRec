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

import org.activity.ui.PopUps;
import org.activity.util.ConnectDatabase;
import org.activity.util.Constant;
import org.activity.util.UtilityBelt;
import org.apache.commons.math3.*;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class TestStatsMore
{
	public static final int theKOriginal = 5;
	public static final String[] timeCategories =
	{ "All", "Morning", "Afternoon", "Evening" };
	public static String commonPath;// =Constant.commonPath;

	public static void main(String args[])
	{
		for (int i = 50; i <= 700;)
		{
			String filePath = "/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global " + i + "/";
			BufferedReader br1 = null;

			ArrayList<Double> recallValsForEachRT = new ArrayList<Double>();
			try
			{
				br1 = new BufferedReader(new FileReader(filePath + "AlgoAlltop1Recall.csv"));

				String currentLine;
				while ((currentLine = br1.readLine()) != null)
				{
					String tokens[] = currentLine.split(Pattern.quote(","));

					for (int j = 0; j < tokens.length; j++)
					{
						// System.out.println("threshold ="+i+" "+tokens[j]);
						if (Double.parseDouble(tokens[j]) >= 0)
						{
							recallValsForEachRT.add(Double.parseDouble(tokens[j]));
						}
						else
						{
						}
						// System.out.println("It is negative");
					}
				}
				br1.close();

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			StandardDeviation sd = new StandardDeviation();

			// System.out.print(//"Standard deviation at "+i+" is"+
			// UtilityBelt.round(sd.evaluate(arrayFromArrayList(recallValsForEachRT)),4)+",");

			System.out.print(// "mean at "+i+" is"+
					UtilityBelt.round(mean(arrayFromArrayList(recallValsForEachRT)), 4) + ",");
			i += 50;
		}
	}

	public static double[] arrayFromArrayList(ArrayList<Double> ls)
	{
		double[] d = new double[ls.size()];

		for (int i = 0; i < ls.size(); i++)
		{
			d[i] = ls.get(i);
		}

		return d;
	}

	public static double mean(double[] m)
	{
		double sum = 0;
		for (int i = 0; i < m.length; i++)
		{
			sum += m[i];
		}
		return sum / m.length;
	}

	//

	public static void writePrecisionRecallFMeasure(String fileNamePhrase, String timeCategory, int theKOriginal,
			ArrayList<ArrayList<String>> arrayMeta, ArrayList<ArrayList<String>> arrayTopK,
			ArrayList<ArrayList<String>> arrayActual)
	{
		// BufferedReader brMeta = null, brTopK = null, brActual = null;
		BufferedWriter bwTopKPrecision = null, bwTopKRecall = null, bwTopKF = null,
				bwNumberOfRecommendationTimes = null;
		;
		int theK = theKOriginal;
		try
		{

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

			System.out.println("size of meta array=" + arrayMeta.size() + "     size of topK array=" + arrayTopK.size()
					+ "   size of actual array=" + arrayActual.size());

			bwNumberOfRecommendationTimes.write("," + timeCategory);
			bwNumberOfRecommendationTimes.newLine();

			for (int i = 0; i < arrayMeta.size(); i++) // iterating over users
			{
				ArrayList<String> currentLineArray = arrayMeta.get(i);

				double topKPrecisionVal = -99, topKRecallVal = -99, accuracy = -99, topKFVal = -99;

				bwNumberOfRecommendationTimes.write(ConnectDatabase.getUserNameFromDatabase(i) + ",");
				// int theK=0;
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered

				for (int j = 0; j < currentLineArray.size(); j++) // iterating over recommendation times
				{
					int hourOfTheDay = getHourFromMetaString(currentLineArray.get(j));
					if (getTimeCategoryOfTheDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
							|| timeCategory.equals("All"))
					{
						countOfRecommendationTimesConsidered++;

						String actual = arrayActual.get(i).get(j);

						String[] topKStrings = arrayTopK.get(i).get(j).split("__"); // __a__b__c__d__e is of length 6...
																					// value at index 0 is empty.

						theK = theKOriginal;
						System.out.println();

						if (topKStrings.length - 1 < theK) // for this RT we are not able to make K recommendations.
						{
							System.err.println(
									"Warning: For " + currentLineArray.get(j) + ", Only top " + (topKStrings.length - 1)
											+ " recommendation present while the asked for K is " + theK
											/* +"\nDecreasing asked for K to "+ (topKStrings.length-1)); */
											+ "\nWriting -999 values");
							theK = topKStrings.length - 1;

							topKPrecisionVal = -9999;
							topKRecallVal = -9999;
							topKFVal = -9999;
						}
						// theK=topKStrings.length-1;

						else
						{
							System.out.println("topKString=arrayTopK.get(i).get(j)=" + arrayTopK.get(i).get(j));
							System.out.println("actual string =" + actual);

							int countOfOccurence = 0; // in current case will be always be either 1 or 0.

							for (int y = 1; y <= theK; y++)
							{
								if (topKStrings[y].equalsIgnoreCase(actual))
								{
									countOfOccurence++;
								}
							}

							if (countOfOccurence > 0) countOfOccurence = 1;

							topKPrecisionVal = round((double) countOfOccurence / theK, 4);
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
							System.out.println("count-of-occurence-used=" + countOfOccurence + "         " + "k-used="
									+ theK + " k-Original=" + theKOriginal);// + " /
																			// ="+round((double)countOfOccurence/theK,4));
						}

						bwTopKPrecision.write(topKPrecisionVal + ",");
						bwTopKRecall.write(topKRecallVal + ",");
						bwTopKF.write(topKFVal + ",");
						// bwAccuracy.write(accuracy+",");

						System.out.println("top-" + theKOriginal + "-precision=" + topKPrecisionVal + "    " + "top-"
								+ theKOriginal + "-recall=" + topKRecallVal + "   top" + theKOriginal + "F=" + topKFVal
								+ "   accuracy=" + accuracy);
					}
				} // end of current line array

				bwTopKPrecision.write("\n");
				bwTopKRecall.write("\n");
				bwTopKF.write("\n");

				bwNumberOfRecommendationTimes.write(countOfRecommendationTimesConsidered + "\n");
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

	public static void writeAvgPrecisionsForAllKs(String fileNamePhrase, String timeCategory, int numUsers)
	{
		// BufferedReader br= null;

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
																							// gives an array of
																							// length1, also splitting
																							// on one values line gives
																							// an array of length 1
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

							System.out.println(
									"Calculating avg precision (K=" + K + "):" + sum + "/" + countOfValidPValues);
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

	public static void writeAvgRecallsForAllKs(String fileNamePhrase, String timeCategory, int numUsers)
	{
		// BufferedReader br= null;

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
							System.out
									.println("Calculating avg recall (K=" + K + "):" + sum + "/" + countOfValidRValues);
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
							System.out.println(
									"Calculating avg FMeasure (K=" + K + "):" + sum + "/" + countOfValidFValues);
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
