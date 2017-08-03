package org.activity.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.activity.clustering.weka.WekaUtilityBelt;
import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.loader.GeolifeDataLoader;
import org.activity.objects.ActivityObject;
import org.activity.objects.CheckinEntry;
import org.activity.objects.DataEntry;
import org.activity.objects.FlatActivityLogEntry;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.TrackListenEntry;
import org.activity.objects.TrajectoryEntry;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.math3.complex.Complex;

/**
 * TODO: convert all method to take in full path name, i.e., absolute file name
 * 
 * @author gunjan
 *
 */
public class WritingToFile
{
	static String commonPath;// = Constant.getCommonPath();//
								// "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/";

	// static final String[] activityNames = { "Not Available", "Unknown", "airplane", "bike", "boat", "bus", "car",
	// "motorcycle", "run", "subway", "taxi",
	// "train", "walk" };

	static int counterEditAllEndPoints = 0;

	/**
	 * Writes a file with MUs as rows, Users as columns and the MRR as the cell value.
	 * 
	 * @param rootPath
	 *            the path to read, i.e., the root path for all MU results
	 * @param absFileNameToWrite
	 *            file name to write for MRR for all user and all MUs result
	 * @param whichAlgo
	 *            "Algo", or "BaselineOccurrence", etc
	 * 
	 * @return number of users
	 */

	public static int writeMRRForAllUsersAllMUs(String rootPath, String absFileNameToWrite, String whichAlgo)
	{
		double[] matchingUnitArray = null;
		int numberOfUsers = -1;

		if (rootPath != null)
		{
			WritingToFile.appendLineToFileAbsolute("MUs/Users\n", absFileNameToWrite);

			if (Constant.lookPastType.equals(Enums.LookPastType.NCount))
			{
				matchingUnitArray = Constant.matchingUnitAsPastCount;// matchingUnitAsPastCount; //
																		// PopUps.showMessage(matchingUnitArray.toString());
			}
			else if (Constant.lookPastType.equals(Enums.LookPastType.NHours))
			{
				matchingUnitArray = Constant.matchingUnitHrsArray;// matchingUnitHrsArray; //
																	// PopUps.showMessage(matchingUnitArray.toString());
			}

			else if (Constant.lookPastType.equals(Enums.LookPastType.Daywise))
			{
				matchingUnitArray = Constant.matchingDummy;
			}

			else if (Constant.lookPastType.equals(Enums.LookPastType.ClosestTime))
			{
				matchingUnitArray = Constant.matchingDummy;
			}
			else
			{
				System.err.println("Error: unknown look past type in in setMatchingUnitArray() RecommendationTests()");
				System.exit(-1);
			}

			for (double mu : matchingUnitArray)
			{
				String fileName = "";
				if (mu == -1)
				{
					fileName = rootPath + whichAlgo + "AllMeanReciprocalRank.csv";
				}
				else
				{
					fileName = rootPath + "MatchingUnit" + mu + "/" + whichAlgo + "AllMeanReciprocalRank.csv";
				}
				List<Double> mrrVals = ReadingFromFile.oneColumnReaderDouble(fileName, ",", 1, true);
				numberOfUsers = mrrVals.size(); // note we need to do this only once, but no harm done if done multiple
												// times, overwriting the same value.
				String mrrValsString = mrrVals.stream().map(Object::toString).collect(Collectors.joining(","));

				WritingToFile.appendLineToFileAbsolute("" + mu + "," + mrrValsString + "\n", absFileNameToWrite);
			}

			// writeMaxOfColumns(absFileNameToWrite, absFileNameToWrite + "MaxOfCols.csv", 1, 18, matchingUnitArray);
		}
		else
		{
			System.out.println("root path is empty");
		}

		return numberOfUsers;
	}

	/**
	 * 
	 * 
	 * @param absFileNameToRead
	 *            with each col corresponding to user while each row corresponding to an MU and the cell values
	 *            containing the corresponding MRR
	 * @param absFileNameToWrite
	 * @param numberOfUsers
	 * @param hasRowHeader
	 * @param booleanHasColHeader
	 * @return LinkedHashMap (UserID, Pair( MUs having Max MRR, max MRR)) // User ID as User1, User2, ...
	 */
	public static LinkedHashMap<String, Pair<List<Double>, Double>> writeDescendingMRRs(String absFileNameToRead,
			String absFileNameToWrite, int numberOfUsers, boolean hasRowHeader, boolean booleanHasColHeader)
	{
		System.out.println("Inside writeDescendingMRRs for file to read:" + absFileNameToRead);
		int startColIndx = 0, lastColIndx = numberOfUsers - 1;
		List<Double> rowLabels = new ArrayList<Double>();

		// (User, Pair( MUs having Max MRR, max MRR))
		LinkedHashMap<String, Pair<List<Double>, Double>> usersMaxMUMRRMap = new LinkedHashMap<String, Pair<List<Double>, Double>>();

		if (hasRowHeader)
		{
			startColIndx += 1; // 1
			lastColIndx += 1; // 18
			rowLabels = ReadingFromFile.oneColumnReaderDouble(absFileNameToRead, ",", 0, booleanHasColHeader);
		}
		else
		{
			int numOfRows = ReadingFromFile.oneColumnReaderDouble(absFileNameToRead, ",", 0, booleanHasColHeader)
					.size();
			for (int i = 0; i < numOfRows; i++)
			{
				rowLabels.add(Double.valueOf(i)); // Row = 0 to Row = <numOfUsers-1>
			}
		}

		WritingToFile.appendLineToFileAbsolute("User" + ",MU, MRR\n", absFileNameToWrite);
		LinkedHashMap<String, String> userCluster = new LinkedHashMap<String, String>();

		for (int colInd = startColIndx; colInd <= lastColIndx; colInd++) // each column is for a user
		{
			List<Double> mrrVals = ReadingFromFile.oneColumnReaderDouble(absFileNameToRead, ",", colInd,
					booleanHasColHeader);

			// (MU,MRR)
			LinkedHashMap<Double, Double> mrrMap = new LinkedHashMap<Double, Double>();

			int serialNum = 0;
			for (Double v : mrrVals)
			{
				mrrMap.put(rowLabels.get(serialNum), v);
				serialNum++;
			}

			mrrMap = (LinkedHashMap<Double, Double>) ComparatorUtils.sortByValueDesc(mrrMap);// sorted by descending
																								// vals

			double maxMRR = Collections.max(mrrMap.values()); // for this col, i.e., for this user
			List<Double> MUsHavingMaxMRR = new ArrayList<Double>(); // for this col, i.e., for this user

			// find the MU's having this max MRR
			for (Entry<Double, Double> entry : mrrMap.entrySet())
			{
				if (entry.getValue() == maxMRR)
				{
					MUsHavingMaxMRR.add(entry.getKey());// adding the corresponding MU
				}
				WritingToFile.appendLineToFileAbsolute(
						"User " + colInd + "," + entry.getKey() + "," + entry.getValue() + "\n", absFileNameToWrite);
			}
			WritingToFile.appendLineToFileAbsolute("\n", absFileNameToWrite);

			Collections.sort(MUsHavingMaxMRR); // MUs with have the max MRR are sorted in ascending order of their MU
												// value, just for convenience of reading
			// Pair(List of MUs with highest MRR, highestMRR)
			Pair<List<Double>, Double> MUsWithMaxMRR = new Pair(MUsHavingMaxMRR, maxMRR);
			usersMaxMUMRRMap.put("User" + colInd, MUsWithMaxMRR);

			// WritingToFile.appendLineToFileAbsolute(
			// "User " + colInd + "," + maxMUMRR.getFirst() + "," + maxMUMRR.getSecond() + "," +
			// getClusterLabel(Double.valueOf(maxMUMRR.getFirst()))
			// + "\n", absFileNameToWrite + "Cluster.csv");
		}

		return usersMaxMUMRRMap;
	}

	/**
	 * Incomplete to write the max MRR over MUs
	 * 
	 * @param absFileNameToRead
	 * @param absFileNameToWrite
	 * @param startColIndx
	 * @param lastColIndx
	 */
	public static void writeMaxOfColumns(String absFileNameToRead, String absFileNameToWrite, int startColIndx,
			int lastColIndx, double[] matchingUnitArray)// , boolean
														// hasColHeader)
	{
		// int startColInd = 0;
		// if (hasColHeader)
		// {
		// startColInd = 1;
		// }
		WritingToFile.appendLineToFileAbsolute("User" + ",MU, MRR\n", absFileNameToWrite);

		LinkedHashMap<String, String> userCluster = new LinkedHashMap<String, String>();

		for (int colInd = startColIndx; colInd <= lastColIndx; colInd++) // each column is for a user
		{
			List<Double> mrrVals = ReadingFromFile.oneColumnReaderDouble(absFileNameToRead, ",", colInd, true);

			LinkedHashMap<String, Double> mrrMap = new LinkedHashMap<String, Double>();

			int count = 0;
			for (Double v : mrrVals)
			{

				mrrMap.put(Double.toString(matchingUnitArray[count]), v);
				count++;
			}

			mrrMap = (LinkedHashMap<String, Double>) ComparatorUtils.sortByValueDesc(mrrMap);// sorted by descending
																								// vals

			Pair<String, Double> maxMUMRR = new Pair<String, Double>("0", 0.0);
			for (Entry<String, Double> entry : mrrMap.entrySet())
			{
				// String mrrMapString = mrrMap.stream().map(Object::toString).collect(Collectors.joining(","));
				if (entry.getValue() > maxMUMRR.getSecond())
				{
					maxMUMRR = new Pair(entry.getKey(), entry.getValue());
				}
				WritingToFile.appendLineToFileAbsolute(
						"User " + colInd + "," + entry.getKey() + "," + entry.getValue() + "\n", absFileNameToWrite);
			}

			WritingToFile.appendLineToFileAbsolute("\n", absFileNameToWrite);

			WritingToFile.appendLineToFileAbsolute(
					"User " + colInd + "," + maxMUMRR.getFirst() + "," + maxMUMRR.getSecond() + ","
							+ WekaUtilityBelt.getClusterLabelClustering0(Double.valueOf(maxMUMRR.getFirst())) + "\n",
					absFileNameToWrite + "Cluster.csv");
		}
	}

	public static void main(String args[])
	{
		try
		{
			// Path path = Paths.get("./dataWritten/RecommUnmergedNCount/101/MatchingUnit0.0/");
			Files.createDirectories(Paths.get("./dataWritten/RecommUnmergedNCount/101/MatchingUnit0.0/"));
		}

		catch (Exception e)
		{
			e.printStackTrace();

		}
	}

	public static void main1(String args[])
	{
		// List<Double> vals = new ArrayList<Double>();
		// vals.add(12.2);
		// vals.add(34.0);
		// vals.add(55.0);
		//
		// String joined = vals.stream().map(Object::toString).collect(Collectors.joining(","));

		// LinkedHashMap<String,String> map = new Map
		// System.out.println(joined);
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan27Daywise/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Jan27DaywiseAllMRR.csv");

		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan27NCount/Geolife/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Jan27NCountAllMRR.csv");
		//
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan28NCount/Geolife/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Jan28NCountAllMRR.csv");
		//
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan28NCount2NoShuffle/Geolife/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Jan28NCount2NoShuffleAllMRR.csv");
		//
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan28NCount3NoShuffle/Geolife/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Jan28NCount3NoShuffleAllMRR.csv");
		//
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/August14/Geolife/SimpleV3/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Aug14NCountAllMRR.csv");
		//
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/August14/Geolife/SimpleV3/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Aug14NCountAllMRR.csv");
		//
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June18HJDistance/Geolife/SimpleV3/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/June18NCountAllMRR.csv");

		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan27NCountBlackListed/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Jan27NCountBlackListedAllMRR.csv");

		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Feb4NCount/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Feb4NCountAllMRR.csv");
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Feb4NCount2/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/Feb4NCount2AllMRR.csv");
		// writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June18HJDistance/Geolife/SimpleV3/",
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/June18HJDistanceAllMRR.csv");
		writeMRRForAllUsersAllMUs("/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June18HJDistance/Geolife/SimpleV3/",
				"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsJan28/June18HJDistanceAllMRR.csv", "Algo");

		// /
		//
	}

	/**
	 * Returns stream to redirect the console output and error to the given path. (note: redirects the system output
	 * stream as well as system error stream.)
	 * 
	 * @param fullPathFileName
	 *            absolute path with filename
	 * @return PrintStream <b><font color="red">Remember to close this Printstream after writing to it.</font></b>
	 */
	public static PrintStream redirectConsoleOutput(String fullPathFileName)
	{
		PrintStream consoleLogStream = null;
		try
		{
			File consoleLog = new File(fullPathFileName);
			consoleLog.delete();
			consoleLog.createNewFile();
			consoleLogStream = new PrintStream(consoleLog);
			// System.setOut(new PrintStream(new FileOutputStream("/dev/stdout")));
			System.setOut(new PrintStream(consoleLogStream));
			System.setErr(consoleLogStream);
		}
		catch (Exception e)
		{
			System.out.println("Exception generated for fullPathFileName =" + fullPathFileName);
			e.printStackTrace();
		}

		return consoleLogStream;
	}

	/**
	 * Returns a BufferedWriter for the file (with append as true). Alert:If the file exists, the old file is deleted
	 * and new file is created.
	 * 
	 * @param fullAbsolutePath
	 *            absolute path for the file
	 * @return BufferedWriter for given file
	 */
	public static BufferedWriter getBWForNewFile(String fullPath)
	{
		BufferedWriter bw = null;
		// System.out.println("fullpath =" + fullPath);
		try
		{
			File file = new File(fullPath);
			file.delete();
			file.createNewFile();

			FileWriter writer = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(writer);
		}

		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-90);
		}
		return bw;
	}

	/**
	 * Returns a BufferedWriter for the file (with append as true). Alert:If the file exists, the old file is deleted
	 * and new file is created.
	 * 
	 * @param fullAbsolutePath
	 *            absolute path for the file
	 * @param bufsize
	 *            size of bufferwriter
	 * @return BufferedWriter for given file
	 */
	public static BufferedWriter getBufferedWriterForNewFile(String fullPath, int bufsize)
	{
		BufferedWriter bw = null;
		// System.out.println("fullpath =" + fullPath);
		try
		{
			File file = new File(fullPath);
			file.delete();
			file.createNewFile();

			FileWriter writer = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(writer, bufsize);
		}

		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-90);
		}
		return bw;
	}

	/**
	 * Returns a BufferedWriter for the file (with append as true). Alert:If the file exists, the old file is kept and
	 * new values are appended at the end.
	 * 
	 * @param fullAbsolutePath
	 *            absolute path for the file
	 * @return BufferedWriter for given file
	 */
	public static BufferedWriter getBufferedWriterForExistingFile(String fullPath)
	{
		BufferedWriter bw = null;
		// System.out.println("fullpath =" + fullPath);
		try
		{
			File file = new File(fullPath);
			FileWriter writer = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(writer);
		}

		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-90);
		}
		return bw;
	}

	public static void writeArrayList2(ArrayList<Pair<String, Long>> arrayList, String fileNameToUse, String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (Pair<String, Long> t : arrayList)
			{

				bw.write(t.getFirst().toString() + "," + t.getSecond().toString());
				bw.newLine();
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeArrayList(ArrayList<Pair<String, Long>> arrayList, String fileNameToUse, String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (Pair<String, Long> t : arrayList)
			{

				bw.write(Integer.parseInt(t.getFirst()) + "," + t.getSecond().toString());
				bw.newLine();
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param arrayArrayList
	 * @param absFileNameToWrite
	 * @param headerLine
	 * @param delimiter
	 */
	public static void writeArrayListOfArrayList(ArrayList<ArrayList<Double>> arrayArrayList, String absFileNameToWrite,
			String headerLine, String delimiter)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			if (headerLine.length() > 0)
			{
				sb.append(headerLine + "\n");
			}

			for (ArrayList<Double> outerList : arrayArrayList)
			{
				sb.append(outerList.stream().map(v -> v.toString()).collect(Collectors.joining(delimiter)));
				sb.append("\n");
			}

			WritingToFile.writeToNewFile(sb.toString(), absFileNameToWrite);

		}

		catch (Exception e)
		{
			PopUps.showException(e, "org.activity.io.WritingToFile.writeArrayListOfArrayList()");
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param arrayArrayList
	 * @param absFileNameToWrite
	 * @param delimiter
	 * @param colNames
	 * @param rowNames
	 */
	public static void writeArrayListOfArrayList(ArrayList<ArrayList<Double>> arrayArrayList, String absFileNameToWrite,
			String delimiter, ArrayList<String> colNames, ArrayList<String> rowNames)
	{
		try
		{
			StringBuilder sb = new StringBuilder();

			sb.append(delimiter + colNames.stream().collect(Collectors.joining(delimiter)) + "\n");

			int count = 0;
			for (ArrayList<Double> outerList : arrayArrayList)
			{
				String vals = outerList.stream().map(v -> v.toString()).collect(Collectors.joining(delimiter));

				sb.append(rowNames.get(count++) + delimiter + vals);
				sb.append("\n");
			}

			WritingToFile.writeToNewFile(sb.toString(), absFileNameToWrite);

		}

		catch (Exception e)
		{
			PopUps.showException(e, "org.activity.io.WritingToFile.writeArrayListOfArrayList()");
			e.printStackTrace();
		}

	}

	/**
	 * @deprecated
	 * @param arrayArrayList
	 * @param fileNameToUse
	 * @param headerLine
	 * @param commonPath
	 */
	public static void writeArrayListOfArrayListV0(ArrayList<ArrayList<Double>> arrayArrayList, String fileNameToUse,
			String headerLine, String commonPath)
	{
		// commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (int mu = 0; mu < arrayArrayList.size(); mu++)
			{
				ArrayList<Double> editDistances = arrayArrayList.get(mu);

				for (int pair = 0; pair < editDistances.size(); pair++)// (Pair<String, Long> t : arrayList)
				{
					bw.write((mu + 1) + "," + pair + "," + editDistances.get(pair));// Integer.parseInt(t.getFirst()) +
																					// "," + t.getSecond().toString());
					bw.newLine();
				}
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeArrayList(ArrayList<Double> arrayList, String fileNameToUse, String headerLine,
			String commonPath)
	{
		// commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			if (headerLine.length() > 0)
			{
				bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
				bw.newLine();
			}
			for (int pair = 0; pair < arrayList.size(); pair++)// (Pair<String, Long> t : arrayList)
			{
				bw.write(arrayList.get(pair).toString());// Integer.parseInt(t.getFirst()) + "," +
															// t.getSecond().toString());
				bw.newLine();
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param arrayList
	 * @param fullPath
	 *            with file extension
	 * @param headerLine
	 */
	public static void writeArrayListAbsolute(ArrayList<Double> arrayList, String fullPath, String headerLine)
	{
		// commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = fullPath;
			System.out.println("full path = " + fullPath);

			File file = new File(fileName);
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			if (headerLine.length() > 0)
			{
				bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
				bw.newLine();
			}
			for (int pair = 0; pair < arrayList.size(); pair++)// (Pair<String, Long> t : arrayList)
			{
				bw.write(arrayList.get(pair).toString());// Integer.parseInt(t.getFirst()) + "," +
															// t.getSecond().toString());
				bw.newLine();
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeArrayListFlatActivityLogEntry(ArrayList<FlatActivityLogEntry> arrayList,
			String fileNameToUse, String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);
			file.delete();

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (FlatActivityLogEntry t : arrayList)
			{

				bw.write(t.toStringWithoutHeaders());
				bw.newLine();
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Writes the given string to a new file with given filename
	 * 
	 * @param msg
	 * @param absFileNameToUse
	 * @param headerLin
	 */
	public static void writeToNewFile(String msg, String absFileNameToUse)
	{
		// commonPath = Constant.getCommonPath();//
		// System.out.println("commonPath in writeString() is " + commonPath);
		try
		{
			// String fileName = commonPath + absFileNameToUse;// + ".csv";
			File file = new File(absFileNameToUse);
			file.delete();

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(msg);
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Note: .csv automatically added to the name
	 * 
	 * @param msg
	 * @param fileNameToUse
	 */
	public static void appendLineToFile(String msg, String fileNameToUse)
	{
		commonPath = Constant.getCommonPath();//
		// System.out.println("commonPath in writeString() is "+commonPath);
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";
			File file = new File(fileName);

			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(msg);// + "\n");
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeNegativeZeroInvalidsLatLonAltHeader(String fileNameToUse)
	{
		appendLineToFile("User," + "NumOfNegativeLatitudes, NumOfZeroLatitude,NumOfUnknownLatitudes,"
				+ "NumOfNegativeLongitudes, NumOfZeroLongitude,NumOfUnknownLongitudes,"
				+ "NumOfNegativeAltitudes, NumOfZeroAltitude,NumOfUnknownAltitudes, TotalNumOfTrajectoryEntries" + "\n",
				fileNameToUse);
	}

	public static void writeNegativeZeroInvalidsLatLonAltFooter(String fileNameToUse)
	{
		appendLineToFile("Note: This stat is generated during parsing the raw trajectoy entries." + "\n",
				fileNameToUse);
	}

	public static void writeNegativeZeroInvalidsLatLonAlt(String userName, String fileNameToUse)
	{

		String stringToWrite = userName + "," + TrajectoryEntry.getCountNegativeLatitudes() + ","
				+ TrajectoryEntry.getCountZeroLatitudes() + "," + TrajectoryEntry.getCountUnknownLatitudes() + ","
				+ TrajectoryEntry.getCountNegativeLongitudes() + "," + TrajectoryEntry.getCountZeroLongitudes() + ","
				+ TrajectoryEntry.getCountUnknownLongitudes() + "," + TrajectoryEntry.getCountNegativeAltitudes() + ","
				+ TrajectoryEntry.getCountZeroAltitudes() + "," + TrajectoryEntry.getCountUnknownAltitudes() + ","
				+ TrajectoryEntry.getTotalCountTrajectoryEntries() + "\n";

		appendLineToFile(stringToWrite, fileNameToUse);
	}

	/**
	 * 
	 * @param msg
	 * @param fullPathfileNameToUse
	 *            with file extension
	 */
	public static void appendLineToFileAbsolute(String msg, String fullPathfileNameToUse)
	{
		String fileName = null;
		try
		{
			fileName = fullPathfileNameToUse;
			// PopUps.showMessage("Inside appendLineToFileAbsolute() for filename " + fileName);
			File file = new File(fileName);

			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(msg);// + "\n");
			bw.close();
		}

		catch (Exception e)
		{
			System.err.println("Exception generated for fileName = " + fileName);
			e.printStackTrace();
		}
	}

	public static void writeTimestampedActivityObjectsForUser(LinkedHashMap<Timestamp, ActivityObject> ts,
			String fileNameToUse, String userName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + userName + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Timestamp, ActivityObject> entry : ts.entrySet())
			{
				String timestamp = entry.getKey().toString();

				// String actNameToPut;
				if (entry.getValue() == null) // no ao at this time
				{
					continue;
				}

				bw.write(timestamp.substring(0, timestamp.length() - 2) + "," + entry.getValue().getActivityName()
						+ "\n");
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeAllTimestampedActivityObjects(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, String fileNameToUse)
	{
		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
			{
				writeTimestampedActivityObjectsForUser(entry.getValue(), fileNameToUse, entry.getKey());
			}

		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param ts
	 * @param fileNameToUse
	 * @param userName
	 */
	public static void writeTimeSeriesIntForUser(LinkedHashMap<Timestamp, Integer> ts, String fileNameToUse,
			String userName)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Timestamp, Integer> entry : ts.entrySet())
			{
				String timestamp = entry.getKey().toString();

				sb.append(timestamp.substring(0, timestamp.length() - 2) + "," + entry.getValue() + "\n");
				// also removes the last nano seconds precision
			}
			WritingToFile.writeToNewFile(sb.toString(), Constant.getCommonPath() + userName + fileNameToUse + ".csv");
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param ts
	 * @param fileNameToUse
	 * @param userName
	 */
	public static void writeTimeSeriesDoubleForUser(LinkedHashMap<Timestamp, Double> ts, String fileNameToUse,
			String userName)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Timestamp, Double> entry : ts.entrySet())
			{
				String timestamp = entry.getKey().toString();
				sb.append(timestamp.substring(0, timestamp.length() - 2) + "," + entry.getValue() + "\n");
				// also removes the last nano seconds precision
			}
			WritingToFile.writeToNewFile(sb.toString(), Constant.getCommonPath() + userName + fileNameToUse + ".csv");
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param ts
	 * @param fileNameToUse
	 * @param userName
	 */
	public static void writeTimeSeriesLongForUser(LinkedHashMap<Timestamp, Long> ts, String fileNameToUse,
			String userName)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Timestamp, Long> entry : ts.entrySet())
			{
				String timestamp = entry.getKey().toString();
				sb.append(timestamp.substring(0, timestamp.length() - 2) + "," + entry.getValue() + "\n");// bw.write
				// also removes the last nano seconds precision
			}
			WritingToFile.writeToNewFile(sb.toString(), Constant.getCommonPath() + userName + fileNameToUse + ".csv");
			// bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeTimeSeriesOnlyIntValueForUser(LinkedHashMap<Timestamp, Integer> ts, String fileNameToUse,
			String userName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + userName + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Timestamp, Integer> entry : ts.entrySet())
			{
				String timestamp = entry.getKey().toString();

				bw.write(entry.getValue() + "\n"); // also removes the last nano seconds precision
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeTimeSeriesCharForUser(LinkedHashMap<Timestamp, String> ts, String fileNameToUse,
			String userName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + userName + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Timestamp, String> entry : ts.entrySet())
			{
				String timestamp = entry.getKey().toString();

				bw.write(timestamp.substring(0, timestamp.length() - 2) + "," + entry.getValue() + "\n"); // also
																											// removes
																											// the last
																											// nano
																											// seconds
																											// precision
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeShannonEntropy(LinkedHashMap<String, Double> ts, String fileNameToUse)// , String userName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<String, Double> entry : ts.entrySet())
			{
				bw.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeLinkedHashMap(LinkedHashMap<String, String> ts, String fileNameToUse)// , String userName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<String, String> entry : ts.entrySet())
			{
				bw.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeLinkedHashMapStrInt(LinkedHashMap<String, Integer> ts, String absFileNameToUse)// , String
																											// userName)
	{
		// commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = absFileNameToUse;// commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<String, Integer> entry : ts.entrySet())
			{
				bw.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// TODO: make it for generic types..not just Double
	public static void writeLinkedHashMapOfArrayList(LinkedHashMap<String, ArrayList<Double>> ts,
			String absfileNameToUse)// , String userName)
	{
		// commonPath = Constant.getCommonPath();//
		try
		{
			File file = new File(absfileNameToUse);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<String, ArrayList<Double>> entry : ts.entrySet())
			{
				String s = entry.getKey();// + ",";

				for (Double t : entry.getValue())
				{
					s += "," + t.toString();
				}
				bw.write(s + "\n");
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param map
	 * @param absfileNameToUse
	 */
	public static void writeLinkedHashMapOfArrayListInteger(LinkedHashMap<?, ArrayList<Integer>> map,
			String absfileNameToUse)
	{
		try
		{
			BufferedWriter bw = getBWForNewFile(absfileNameToUse);
			for (Entry<?, ArrayList<Integer>> entry : map.entrySet())
			{
				String s = entry.getKey().toString();
				for (Object t : entry.getValue())
				{
					s += "," + t.toString();
				}
				bw.write(s + "\n");
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static Complex[] getFTTransform(double[] values)
	{

		return null;
	}

	/**
	 * Write the time series,to userwise files
	 * 
	 * @param ts
	 * @param fileNameToUse
	 */
	public static void writeAllTimeSeriesInt(LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> ts,
			String fileNameToUse)
	{
		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, Integer>> entry : ts.entrySet())
			{
				int userName = Integer.valueOf(entry.getKey());// UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey()));
				writeTimeSeriesIntForUser(entry.getValue(), fileNameToUse, String.valueOf(userName));
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Write time series to userwise files
	 * 
	 * @param ts
	 * @param fileNameToUse
	 */
	public static void writeAllTimeSeriesDouble(LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> ts,
			String fileNameToUse)
	{
		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, Double>> entry : ts.entrySet())
			{
				int userName = Integer.valueOf(entry.getKey());// UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey()));
				writeTimeSeriesDoubleForUser(entry.getValue(), fileNameToUse, String.valueOf(userName));
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Write the time series ,user wise to the files
	 * 
	 * @param ts
	 * @param fileNameToUse
	 */
	public static void writeAllTimeSeriesLong(LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> ts,
			String fileNameToUse)
	{
		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, Long>> entry : ts.entrySet())
			{
				int userName = Integer.valueOf(entry.getKey());// UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey()));
				writeTimeSeriesLongForUser(entry.getValue(), fileNameToUse, String.valueOf(userName));
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeAllTimeSeriesOnlyIntValue(LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> ts,
			String fileNameToUse)
	{
		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, Integer>> entry : ts.entrySet())
			{
				int userName = Integer.valueOf(entry.getKey());// UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey()));
				writeTimeSeriesOnlyIntValueForUser(entry.getValue(), fileNameToUse, String.valueOf(userName));
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeAllTimeSeriesChar(LinkedHashMap<String, LinkedHashMap<Timestamp, String>> ts,
			String fileNameToUse)
	{
		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, String>> entry : ts.entrySet())
			{
				int userName = Integer.valueOf(entry.getKey());// UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey()));
				writeTimeSeriesCharForUser(entry.getValue(), fileNameToUse, String.valueOf(userName));
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// // primarily used for writing unknowns, might be better to use generics here K,V
	public static void writeLinkedHashMapOfTreemap(LinkedHashMap<String, TreeMap<Timestamp, String>> mapOfMap,
			String fileNameToUse, String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapOfMap.entrySet())
			{

				String userName = entryForUser.getKey();
				TreeMap<Timestamp, String> mapForEachUser = entryForUser.getValue();

				for (Map.Entry<Timestamp, String> entryInside : mapForEachUser.entrySet())
				{
					bw.write(userName + "," + entryInside.getKey() + "," + entryInside.getValue());
					bw.newLine();
				}
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param mapOfMap
	 * @param absfileNameToUse
	 * @param headerLine
	 */
	public static void writeLinkedHashMapOfTreemapCheckinEntry(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapOfMap, String absfileNameToUse)
	{
		try
		{
			BufferedWriter bw = WritingToFile.getBWForNewFile(absfileNameToUse);
			StringBuilder sbToWrite = new StringBuilder();
			sbToWrite.append(CheckinEntry.getHeaderToWrite() + "\n");

			int count = 0;
			for (Entry<String, TreeMap<Timestamp, CheckinEntry>> entryForUser : mapOfMap.entrySet())
			{
				TreeMap<Timestamp, CheckinEntry> mapForEachUser = entryForUser.getValue();
				for (Map.Entry<Timestamp, CheckinEntry> entryInside : mapForEachUser.entrySet())
				{
					count += 1;
					sbToWrite.append(entryInside.getValue().toStringWithoutHeaders() + "\n");

					if (count % 50000 == 0) // write in chunks
					{
						bw.write(sbToWrite.toString());
						sbToWrite.setLength(0);
					}
				}
			}

			// write the remaining
			bw.write(sbToWrite.toString());
			sbToWrite.setLength(0);
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeLinkedHashMapOfTreemapTrajEntry(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapOfMap, String fileNameToUse,
			String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapOfMap.entrySet())
			{

				String userName = entryForUser.getKey();
				TreeMap<Timestamp, TrajectoryEntry> mapForEachUser = entryForUser.getValue();

				for (Map.Entry<Timestamp, TrajectoryEntry> entryInside : mapForEachUser.entrySet())
				{
					bw.write(userName + "," + entryInside.getValue().toStringWithoutHeaders());
					bw.newLine();
				}
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeLinkedHashMapOfTreemapDataEntry(
			LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapOfMap, String fileNameToUse, String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (Map.Entry<String, TreeMap<Timestamp, DataEntry>> entryForUser : mapOfMap.entrySet())
			{

				String userName = entryForUser.getKey();
				TreeMap<Timestamp, DataEntry> mapForEachUser = entryForUser.getValue();

				for (Map.Entry<Timestamp, DataEntry> entryInside : mapForEachUser.entrySet())
				{
					bw.write(userName + "," + entryInside.getValue().toStringWithoutHeaders());
					bw.newLine();
				}
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeLinkedHashMapOfTreemapTLE(
			LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapOfMap, String fileNameToUse,
			String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (Map.Entry<String, TreeMap<Timestamp, TrackListenEntry>> entryForUser : mapOfMap.entrySet())
			{

				String userName = entryForUser.getKey();
				TreeMap<Timestamp, TrackListenEntry> mapForEachUser = entryForUser.getValue();

				for (Map.Entry<Timestamp, TrackListenEntry> entryInside : mapForEachUser.entrySet())
				{
					bw.write(userName + "," + entryInside.getValue().toStringWithoutHeaders());
					bw.newLine();
				}
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeLinkedHashMapOfTreemapPureTrajectoryEntries(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapOfMap, String fileNameToUse,
			String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";
			File file = new File(fileName);
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapOfMap.entrySet())
			{
				String userName = entryForUser.getKey();
				TreeMap<Timestamp, TrajectoryEntry> mapForEachUser = entryForUser.getValue();

				for (Map.Entry<Timestamp, TrajectoryEntry> entryInside : mapForEachUser.entrySet())
				{
					bw.write(userName + "," + entryInside.getValue().toStringEssentialsWithoutHeaders());
					bw.newLine();
				}
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeLinkedHashMapOfTreemapAllString(LinkedHashMap<String, TreeMap<String, String>> mapOfMap,
			String fileNameToUse, String headerLine)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			for (Map.Entry<String, TreeMap<String, String>> entryForUser : mapOfMap.entrySet())
			{

				String userName = entryForUser.getKey();
				TreeMap<String, String> mapForEachUser = entryForUser.getValue();

				for (Map.Entry<String, String> entryInside : mapForEachUser.entrySet())
				{
					bw.write(
							userName + "," + entryInside.getKey().toString() + "," + entryInside.getValue().toString());
					bw.newLine();
				}
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// //

	// /////////
	/**
	 * Write to file about the 'Not Annotated' images in the given LinkedHashMap
	 * 
	 * @param data
	 *            LinkedHashMap containing the data
	 * @param filenameEndPhrase
	 *            Name for the file to be written
	 */
	public static void writeActivityTypeWithTimeDifference2(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> data, String activityNameToLookFor,
			String fileNameEnd)
	{
		String fileName = commonPath + activityNameToLookFor.replaceAll(" ", "_") + fileNameEnd + ".csv";

		try
		{
			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("UserName,ImageTimestamp,DifferenceInSecondsWithNext, ActivityName");
			bw.newLine();
			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : data.entrySet())
			{

				String userName = entryForUser.getKey();

				TreeMap<Timestamp, TrajectoryEntry> mapForEachUser = new TreeMap<Timestamp, TrajectoryEntry>();

				for (Map.Entry<Timestamp, TrajectoryEntry> entry : entryForUser.getValue().entrySet())
				{
					// System.out.println(entry.getKey()+","+entry.getValue());
					if (entry.getValue().getMode().equalsIgnoreCase(activityNameToLookFor))
						bw.write(userName + "," + entry.getValue().toString() + "\n");
				}
			}
			bw.close();
		} // end of try
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// ////
	// /////////
	/**
	 * Write to file about the 'Not Annotated' images in the given LinkedHashMap
	 * 
	 * @param data
	 *            LinkedHashMap containing the data
	 * @param filenameEndPhrase
	 *            Name for the file to be written
	 */
	public static void writeActivityTypeWithTimeDifference(LinkedHashMap<String, TreeMap<Timestamp, String>> data,
			String activityNameToLookFor, String fileNameEnd)
	{
		String fileName = commonPath + activityNameToLookFor.replaceAll(" ", "_") + fileNameEnd + ".csv";

		try
		{
			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("UserName,ImageTimestamp,DifferenceInSecondsWithNext, ActivityName");
			bw.newLine();
			for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : data.entrySet())
			{

				String userName = entryForUser.getKey();

				TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

				for (Map.Entry<Timestamp, String> entry : entryForUser.getValue().entrySet())
				{
					// System.out.println(entry.getKey()+","+entry.getValue());
					if (entry.getValue().contains(activityNameToLookFor))
						bw.write(userName + "," + entry.getKey() + "," + entry.getValue() + "\n");
				}
			}
			bw.close();
		} // end of try
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// ////

	/**
	 * To write the occurrence of given Activity name with duration and if that occurrence is a sandwich case.
	 * 
	 * @param mapForAllDataMergedPlusDuration
	 * @param activityNameToLookFor
	 *            the activity name to be looked for. If all avtivity names are to be looked for then use 'everything'
	 * @param fileNameEnd
	 * @param onlySandwich
	 *            if true only sandwich cases are mentioned, else sandwich as well as non-sandwich cases are mentioned
	 */
	public static void writeActivityTypeWithDurationGeo(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedPlusDuration,
			String activityNameToLookFor, String fileNameEnd, boolean onlySandwiches)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + activityNameToLookFor.replaceAll(" ", "_") + fileNameEnd + ".csv";
			File file = new File(fileName);
			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(
					"User_Name,Timestamp,Activity_Name,Duration_in_seconds,Is_Sandwich_Case,Preceeding_Activity,Succeeding_Activity,Timediff_with_prev,Timediff_with_next");// ,Num_of_data_points_Merged");
			bw.newLine();

			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapForAllDataMergedPlusDuration
					.entrySet())
			{

				String userName = entryForUser.getKey();
				TreeMap<Timestamp, TrajectoryEntry> mapForEachUser = entryForUser.getValue();
				String preceedingActivity = "";
				String succeedingActivity = "";

				int isSandwichCase = -99;

				ArrayList<TrajectoryEntry> entriesForUser = UtilityBelt.treeMapToArrayListGeo(mapForEachUser);

				for (int i = 0; i < entriesForUser.size(); i++)// String entry: entriesForUser)
				{
					// $$ System.out.println("Size is:"+entriesForUser.size()+" index is "+i);
					TrajectoryEntry te = entriesForUser.get(i);

					Timestamp timestamp = te.getTimestamp();
					String activityName = te.getMode();// splitted[1];
					String activityDurationInSecs = Long.toString(te.getDurationInSeconds());// splitted[2];
					long timeDiffWithPrev = -99, timeDiffWithNext = -99;

					// //////////////WRONG...PREfiltering give few wrong cases like like X---Target-- Empty is
					// classified as sandwich ..CHECKed IF THIS HAS ANY
					// EFFECT ON RESULT, LOGICALLY IT
					// SHOULDNT
					// if(!(activityName.trim().equalsIgnoreCase(activityNameToLookFor))){continue;}
					// //////////////

					if (i == 0) // no preceeding as its first
					{
						preceedingActivity = "--";
					}
					else if (i != 0) // our concern
					{
						// String splittedP[]=entriesForUser.get(i-1).split(Pattern.quote("||"));
						// preceedingActivity=splittedP[1];
						preceedingActivity = entriesForUser.get(i - 1).getMode();
						timeDiffWithPrev = (te.getTimestamp().getTime()
								- entriesForUser.get(i - 1).getTimestamp().getTime()) / 1000;
					}

					if (i == (entriesForUser.size() - 2)) // no succeeding activity as it is the last activity
					{
						succeedingActivity = "--";
					}

					else if (i < (entriesForUser.size() - 2)) // our concern
					{
						// String splittedS[]=entriesForUser.get(i+1).split(Pattern.quote("||"));
						// succeedingActivity=splittedS[1];
						succeedingActivity = entriesForUser.get(i + 1).getMode();
						timeDiffWithNext = (entriesForUser.get(i + 1).getTimestamp().getTime()
								- te.getTimestamp().getTime()) / 1000;
					}

					if (!(succeedingActivity.equals("--") || preceedingActivity.equals("--"))
							&& succeedingActivity.equals(preceedingActivity))
					{
						isSandwichCase = 1;
					}

					else if (!((!(succeedingActivity.equals("--") || preceedingActivity.equals("--"))
							&& succeedingActivity.equals(preceedingActivity))))
					{
						isSandwichCase = 0;
					}

					else
					{
						System.err.println(
								"Check Error: This should be unreachable code in writeActivityTypeWithDurationGeo");
					}

					// //////////////////////////////////////////////////////////
					// write all activity names, NO FILTER
					if (activityNameToLookFor.toLowerCase().trim().equals("everything"))
					{
						if (onlySandwiches == false) // NO SANDWICH FILTER
						{
							bw.write(userName + "," + timestamp + "," + activityName + "," + activityDurationInSecs
									+ "," + isSandwichCase + "," + preceedingActivity + "," + succeedingActivity + ","
									+ timeDiffWithPrev + "," + timeDiffWithNext);
							bw.newLine();
						}
						else if (onlySandwiches == true) // SANDWICH FILTER
						{
							if (isSandwichCase == 1) // sandwiches
							{
								bw.write(userName + "," + timestamp + "," + activityName + "," + activityDurationInSecs
										+ "," + isSandwichCase + "," + preceedingActivity + "," + succeedingActivity
										+ "," + timeDiffWithPrev + "," + timeDiffWithNext);
								bw.newLine();
							}
						}
					} // // Write only valids activity names
					else if (activityNameToLookFor.toLowerCase().trim().equals("validsonly"))
					{
						if (onlySandwiches == false) // NO SANDWICH FILTER
						{
							if (((activityName.trim().equalsIgnoreCase(Constant.INVALID_ACTIVITY1))
									|| (activityName.trim().equalsIgnoreCase(Constant.INVALID_ACTIVITY2))) == false)
							{
								bw.write(userName + "," + timestamp + "," + activityName + "," + activityDurationInSecs
										+ "," + isSandwichCase + "," + preceedingActivity + "," + succeedingActivity
										+ "," + timeDiffWithPrev + "," + timeDiffWithNext);
								bw.newLine();
							}
						}
						else if (onlySandwiches == true) // SANDWICH FILTER
						{
							if ((((activityName.trim().equalsIgnoreCase(Constant.INVALID_ACTIVITY1))
									|| (activityName.trim().equalsIgnoreCase(Constant.INVALID_ACTIVITY2))) == false)
									&& isSandwichCase == 1) // not
															// just
															// sandwiches
							{
								bw.write(userName + "," + timestamp + "," + activityName + "," + activityDurationInSecs
										+ "," + isSandwichCase + "," + preceedingActivity + "," + succeedingActivity
										+ "," + timeDiffWithPrev + "," + timeDiffWithNext);
								bw.newLine();
							}
						}
					}

					else
					// write only given activity names ,ACTIVITY NAME FILTER
					{
						if (onlySandwiches == false) // NO SANDWICH FILTER
						{
							if (activityName.trim().equalsIgnoreCase(activityNameToLookFor)) // not just sandwiches
							{
								bw.write(userName + "," + timestamp + "," + activityName + "," + activityDurationInSecs
										+ "," + isSandwichCase + "," + preceedingActivity + "," + succeedingActivity
										+ "," + timeDiffWithPrev + "," + timeDiffWithNext);
								bw.newLine();
							}
						}
						else if (onlySandwiches == true) // SANDWICH FILTER
						{
							if (activityName.trim().equalsIgnoreCase(activityNameToLookFor) && isSandwichCase == 1) // not
																													// just
																													// sandwiches
							{
								bw.write(userName + "," + timestamp + "," + activityName + "," + activityDurationInSecs
										+ "," + isSandwichCase + "," + preceedingActivity + "," + succeedingActivity
										+ "," + timeDiffWithPrev + "," + timeDiffWithNext);
								bw.newLine();
							}
						}
					}

				}
			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// ////

	/**
	 * 
	 * @param mapForAllDataMergedPlusDuration
	 *            <UserName, <Timestamp,'activityname||durationInSeconds'>>
	 */
	public static void writeActivityTypeWithDuration(
			LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllDataMergedPlusDuration,
			String activityNameToLookFor, String fileNameEnd)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + activityNameToLookFor.replaceAll(" ", "_") + fileNameEnd + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(
					"User_Name,Timestamp,Activity_Name,Duration_in_seconds,Is_Sandwich_Case,Preceeding_Activity,Succeeding_Activity,Num_of_Images_Merged");
			bw.newLine();

			for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapForAllDataMergedPlusDuration
					.entrySet())
			{

				String userName = entryForUser.getKey();
				TreeMap<Timestamp, String> mapForEachUser = entryForUser.getValue();

				// <Timestamp,'activityname||durationInSeconds'>
				String preceedingActivity = "";
				String succeedingActivity = "";
				int isSandwichCase = -99;

				ArrayList<String> entriesForUser = UtilityBelt.treeMapToArrayListString(mapForEachUser);

				for (int i = 0; i < entriesForUser.size(); i++)// String entry: entriesForUser)
				{
					// $$ System.out.println("Size is:"+entriesForUser.size()+" index is "+i);

					String splitted[] = entriesForUser.get(i).split(Pattern.quote("||"));
					// String dateString= UtilityBelt.getDateString(new Timestamp(Long.valueOf(splitted[0])));
					Timestamp timestamp = new Timestamp(Long.valueOf(splitted[0]));
					String activityName = splitted[1];
					String activityDurationInSecs = splitted[2];

					String theRest = "";

					int k = 3;
					while (k < splitted.length)
					{
						theRest = theRest + splitted[k] + ",";
						// $$System.out.println("k="+k);
						k++;
					}

					if (i == 0)
					{
						preceedingActivity = "--";
					}
					else if (i != 0)
					{
						String splittedP[] = entriesForUser.get(i - 1).split(Pattern.quote("||"));
						preceedingActivity = splittedP[1];
					}

					if (i == (entriesForUser.size() - 2))
					{
						succeedingActivity = "--";
					}

					else if (i < (entriesForUser.size() - 2))
					{
						String splittedS[] = entriesForUser.get(i + 1).split(Pattern.quote("||"));
						succeedingActivity = splittedS[1];
					}

					if (!(succeedingActivity.equals("--") || preceedingActivity.equals("--"))
							&& succeedingActivity.equals(preceedingActivity))
					{
						isSandwichCase = 1;
					}

					else if (!((!(succeedingActivity.equals("--") || preceedingActivity.equals("--"))
							&& succeedingActivity.equals(preceedingActivity))))
					{
						isSandwichCase = 0;
					}

					if (activityName.trim().equalsIgnoreCase(activityNameToLookFor) && isSandwichCase == 1) // remove
																											// this
																											// condition
																											// after
																											// experiment
																											// //this
																											// enumerates
																											// only
																											// sandwiched
					{
						bw.write(userName + "," + timestamp + "," + activityNameToLookFor + "," + activityDurationInSecs
								+ "," + isSandwichCase + "," + preceedingActivity + "," + succeedingActivity + ","
								+ theRest);
						bw.newLine();
					}

					/*
					 * else if(activityName.trim().equalsIgnoreCase("Not Available")) {
					 * bw.write(userName+","+timestamp+",Not_Annotated,"+activityDurationInSecs+
					 * ","+isSandwichCase+","+preceedingActivity+","+succeedingActivity+","); bw.newLine(); }
					 */

				}

				// for(Map.Entry<Timestamp, String> entry:entryForUser.getValue().entrySet())
				// {
				// String activityNameDuration=entry.getValue();
				//
				// String activityName=DCU_Data_Loader.getActivityNameFromDataEntry(activityNameDuration);
				// long activityDurationInSecs=DCU_Data_Loader.getDurationInSecondsFromDataEntry(activityNameDuration);
				// String dateString= UtilityBelt.getDateString(entry.getKey());
				//
				//
				// if(activityName.trim().equalsIgnoreCase("badImages"))
				// {
				// bw.write(userName+","+dateString+",Bad_Images,"+activityDurationInSecs);
				// bw.newLine();
				// }
				//
				// else if(activityName.trim().equalsIgnoreCase("Not Available"))
				// {
				// bw.write(userName+","+dateString+",Not_Annotated,"+activityDurationInSecs);
				// bw.newLine();
				// }
				// }

			}
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeEditSimilarityCalculation(ArrayList<ActivityObject> ActivityObjects1,
			ArrayList<ActivityObject> ActivityObjects2, double editDistance)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "EditSimilarityCalculations.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < ActivityObjects1.size(); i++)
			{
				// bw.write("ActsFirst:,");
				bw.write(ActivityObjects1.get(i).getActivityName() + "_"
						+ ActivityObjects1.get(i).getStartTimestamp().getHours() + ":"
						+ ActivityObjects1.get(i).getStartTimestamp().getMinutes() + ":"
						+ ActivityObjects1.get(i).getStartTimestamp().getSeconds() + "_"
						+ +ActivityObjects1.get(i).getDurationInSeconds() + ",");
			}
			bw.newLine();

			for (int i = 0; i < ActivityObjects2.size(); i++)
			{
				// bw.write("ActsSecond:,");
				bw.write(ActivityObjects2.get(i).getActivityName() + "_"
						+ ActivityObjects2.get(i).getStartTimestamp().getHours() + ":"
						+ ActivityObjects2.get(i).getStartTimestamp().getMinutes() + ":"
						+ ActivityObjects2.get(i).getStartTimestamp().getSeconds() + "_"
						+ +ActivityObjects2.get(i).getDurationInSeconds() + ",");
			}
			bw.newLine();

			bw.write("Edit_Distance," + editDistance);
			bw.newLine();
			bw.newLine();

			bw.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeEditSimilarityCalculationsHeader()
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "EditSimilarityCalculations.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);// appends
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(
					"UserAtRecomm,DateAtRecomm,TimeAtRecomm,CandidateTimelineID,EditDistance,ActLevelDistance,FeatLevelDistance,Trace, ActivityObjects1,ActivityObjects2\n");
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeEditSimilarityCalculations(ArrayList<ActivityObject> ActivityObjects1,
			ArrayList<ActivityObject> ActivityObjects2, double editDistance, String trace, double dAct, double dFeat,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, String candidateTimelineId)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "EditSimilarityCalculations.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);// appends
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candidateTimelineId + ","
					+ editDistance + "," + dAct + "," + dFeat + "," + trace + ",");

			// String activityObjects1String = "", activityObjects2String = "";

			StringBuilder activityObjects1String = new StringBuilder();
			StringBuilder activityObjects2String = new StringBuilder();

			if (VerbosityConstants.WriteActivityObjectsInEditSimilarityCalculations)
			{
				for (int i = 0; i < ActivityObjects1.size(); i++)
				{
					// bw.write("ActsFirst:,");
					// activityObjects1String = activityObjects1String +
					activityObjects1String.append(">>" + (ActivityObjects1.get(i).getActivityName() + "_"
							+ ActivityObjects1.get(i).getStartTimestamp().getHours() + ":"
							+ ActivityObjects1.get(i).getStartTimestamp().getMinutes() + ":"
							+ ActivityObjects1.get(i).getStartTimestamp().getSeconds() + "_"
							+ +ActivityObjects1.get(i).getDurationInSeconds()));
				}

				for (int i = 0; i < ActivityObjects2.size(); i++)
				{
					// bw.write("ActsSecond:,");
					// activityObjects2String = activityObjects2String +
					activityObjects2String.append(">>" + (ActivityObjects2.get(i).getActivityName() + "_"
							+ ActivityObjects2.get(i).getStartTimestamp().getHours() + ":"
							+ ActivityObjects2.get(i).getStartTimestamp().getMinutes() + ":"
							+ ActivityObjects2.get(i).getStartTimestamp().getSeconds() + "_"
							+ +ActivityObjects2.get(i).getDurationInSeconds()));
				}
			}

			bw.write(activityObjects1String.toString() + "," + activityObjects2String.toString());
			bw.newLine();
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeOnlyTrace(String trace)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "tracesEncountered.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(trace + "\n");
			bw.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Writes EditDistancesForAllEndPoints.csv with fields "Counter,
	 * UserID,CurrentTimeline,CandidateTimeline,EndPointIndex,EditDistance"
	 */
	public static void writeEditDistancesOfAllEndPointsHeader()
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "EditDistancesForAllEndPoints.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("Counter, UserID,CurrentTimeline,CandidateTimeline,EndPointIndex,EditDistance");
			bw.newLine();
			bw.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeEditDistancesOfAllEndPoints(ArrayList<ActivityObject> activitiesGuidingRecomm,
			Timeline userDayTimeline, LinkedHashMap<Integer, Pair<String, Double>> distanceScoresForEachSubsequence)// String
	{
		try
		{
			StringBuilder sbToWrite = new StringBuilder();
			counterEditAllEndPoints++;
			for (Map.Entry<Integer, Pair<String, Double>> entry : distanceScoresForEachSubsequence.entrySet())
			{

				sbToWrite.append(counterEditAllEndPoints + "," + userDayTimeline.getUserID() + ","
						+ ActivityObject.getArrayListOfActivityObjectsAsString(activitiesGuidingRecomm) + ","
						+ userDayTimeline.getActivityObjectNamesInSequenceWithFeatures() + "," + entry.getKey() + ","
						+ entry.getValue().getSecond() + "\n");
			}
			sbToWrite.append("\n");
			WritingToFile.appendLineToFileAbsolute(sbToWrite.toString(),
					Constant.getCommonPath() + "EditDistancesForAllEndPoints.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeEndPoinIndexCheck24Oct(String currentAct, String cand, ArrayList<Integer> arr1,
			ArrayList<Integer> arr2)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "EndPoinIndexCheck24Oct.csv";
			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			boolean isEqual = true;

			if (arr1.size() != arr2.size()) isEqual = false;

			if (isEqual)
			{
				isEqual = arr1.equals(arr2.toString());
			}

			int isEqualI;

			if (isEqual)
				isEqualI = 1;
			else
				isEqualI = 0;
			bw.write(arr1.toString() + "," + arr2.toString() + "," + isEqualI + "\n");
			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeEditDistance(double editDistance)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "EditDistance.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("Edit_Distance," + editDistance);
			bw.newLine();
			bw.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// /**
	// * Writes EditDistancePerRtPerCand.csv
	// * @param getDistanceScoresSorted
	// */
	// public static void writeDistanceScoresSorted(LinkedHashMap<Date, Double> getDistanceScoresSorted)
	// {
	// commonPath = Constant.getCommonPath();//
	// try
	// {
	// String fileName = commonPath + "EditDistancePerRtPerCand.csv";
	//
	// FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
	// BufferedWriter bw = new BufferedWriter(fw);
	//
	// for (Map.Entry<Date, Double> entry : getDistanceScoresSorted.entrySet())
	// {
	// bw.write(entry.getValue() + "," + entry.getKey());
	// bw.newLine();
	// }
	// bw.close();
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Just writing to file EditDistancePerRtPerCand.csv using data from distanceScoresSortedMap
	 * 
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param getDistanceScoresSorted
	 * @param candidateTimelines
	 * @param topNames
	 * @param currentTimeline
	 */
	public static void writeDistanceScoresSortedMap(String userAtRecomm, Date dateAtRecomm, Time timeAtRecomm,
			LinkedHashMap<Date, Triple<Integer, String, Double>> getDistanceScoresSorted,
			LinkedHashMap<Date, Timeline> candidateTimelines, LinkedHashMap<Date, String> topNames,
			ArrayList<ActivityObject> currentTimeline)
	{
		try
		{
			// String fileName = commonPath + "EditDistancePerRtPerCand.csv";
			// FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			// BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder toWrite = new StringBuilder();

			for (Map.Entry<Date, Triple<Integer, String, Double>> entry : getDistanceScoresSorted.entrySet())
			{
				int countOfL1Ops = UtilityBelt.getCountOfLevel1Ops(entry.getValue().getSecond());
				int countOfL2Ops = UtilityBelt.getCountOfLevel2Ops(entry.getValue().getSecond());

				toWrite.append(userAtRecomm + "," + dateAtRecomm.toString() + "," + timeAtRecomm.toString() + ","
						+ entry.getKey().toString() + "," + entry.getValue().getFirst() + ","
						+ entry.getValue().getSecond() + "," + entry.getValue().getThird() + "," + countOfL1Ops + ","
						+ countOfL2Ops + "," + topNames.get(entry.getKey()) + ","
						+ candidateTimelines.get(entry.getKey()).getActivityObjectNamesInSequenceWithFeatures() + ","
						+ "," + getStringActivityObjArray(currentTimeline) + "\n");
				// bw.newLine();
			}
			WritingToFile.appendLineToFileAbsolute(toWrite.toString(),
					Constant.getCommonPath() + "EditDistancePerRtPerCand.csv");
			// bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// public static void writeDistanceScoresSortedMapMUBroken(String userAtRecomm, Date dateAtRecomm, Time
	// timeAtRecomm, LinkedHashMap<Integer, Pair<String,
	// Double>> getDistanceScoresSorted,
	// LinkedHashMap<Integer, TimelineWithNext> candidateTimelines, ArrayList<Triple<ActivityObject, Double, Integer>>
	// topNextActivityObjects,
	// ArrayList<ActivityObject> currentTimeline,
	// boolean writeCandidateTimeline)
	// {
	// commonPath = Constant.getCommonPath();//
	// try
	// {
	// String fileName = commonPath + userAtRecomm + "EditDistancePerRtPerCandBroken.csv";
	//
	// FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile());// , true);
	// BufferedWriter bw = new BufferedWriter(fw);
	//
	// boolean writefull = true;
	//
	// for (Map.Entry<Integer, Pair<String, Double>> entry : getDistanceScoresSorted.entrySet())
	// {
	// int candTimelineID = entry.getKey();
	// String editOps = entry.getValue().getFirst();
	// double editDist = entry.getValue().getSecond();
	//
	// int countOfL1Ops = UtilityBelt.getCountOfLevel1Ops(editOps);// entry.getValue().getFirst());
	// int countOfL2Ops = UtilityBelt.getCountOfLevel2Ops(editOps);// entry.getValue().getFirst());
	//
	// String topAOName = "null";
	// for (Triple<ActivityObject, Double, Integer> t : topNextActivityObjects) // topNextActivityObjects should be
	// converted to hashmap for faster access.
	// {
	// if (t.getThird() == candTimelineID)
	// {
	// topAOName = t.getFirst().getActivityName();
	// break;
	// }
	// }
	//
	// String candidateTimelineAsString;
	// if (writeCandidateTimeline)
	// {
	// candidateTimelineAsString =
	// candidateTimelines.get(candTimelineID).getActivityObjectNamesWithTimestampsInSequence();
	// }
	// else
	// {
	// candidateTimelineAsString = "";
	// }
	//
	// if (writefull)
	// {
	// bw.write(userAtRecomm + "," + dateAtRecomm.toString() + "," + timeAtRecomm.toString() + "," + candTimelineID +
	// "," + " " + "," + editOps + "," + editDist
	// + "," + countOfL1Ops + ","
	// + countOfL2Ops + "," + topAOName + "," + candidateTimelineAsString + "," + "," +
	// getStringActivityObjArray(currentTimeline));
	// writefull = false;
	// }
	// else
	// // no need to write same repeating things everytime
	// {
	// bw.write("',','," + candTimelineID + "," + " " + "," + editOps + "," + editDist + "," + countOfL1Ops + "," +
	// countOfL2Ops + "," + topAOName + "," +
	// candidateTimelineAsString + "," + ",'");
	// }
	// bw.newLine();
	// }
	// bw.close();
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Writes the file EditDistancePerRtPerCand.csv
	 * 
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param getDistanceScoresSorted
	 * @param candidateTimelines
	 * @param topNextActivityObjects
	 * @param currentTimeline
	 * @param writeCandidateTimeline
	 */
	public static void writeDistanceScoresSortedMapMU(String userAtRecomm, Date dateAtRecomm, Time timeAtRecomm,
			LinkedHashMap<Integer, Pair<String, Double>> getDistanceScoresSorted,
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityObjects,
			ArrayList<ActivityObject> currentTimeline, boolean writeCandidateTimeline, boolean writeEditOperations)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "EditDistancePerRtPerCand.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			boolean writefull = true;

			for (Map.Entry<Integer, Pair<String, Double>> entry : getDistanceScoresSorted.entrySet())
			{
				int candTimelineID = entry.getKey();
				String editOps = entry.getValue().getFirst();
				double editDist = entry.getValue().getSecond();

				int countOfL1Ops = UtilityBelt.getCountOfLevel1Ops(editOps);// entry.getValue().getFirst());
				int countOfL2Ops = UtilityBelt.getCountOfLevel2Ops(editOps);// entry.getValue().getFirst());

				String topNextAOName = "null";

				for (Triple<ActivityObject, Double, Integer> t : topNextActivityObjects)
				{// topNextActivityObjects should be converted to hashmap for faster access.
					if (t.getThird() == candTimelineID)
					{
						topNextAOName = t.getFirst().getActivityName();
						break;
					}
				}

				String candidateTimelineAsString = " ";
				String editOperationsString = " ";

				if (writeCandidateTimeline)
				{
					candidateTimelineAsString = candidateTimelines.get(candTimelineID)
							.getActivityObjectNamesWithTimestampsInSequence();
				}

				if (writeEditOperations)
				{
					editOperationsString = editOps;
				}

				String userString = "'", dateString = "'", timeString = "'", currentTimelineString = "";

				/*
				 * "UserAtRecomm,DateAtRecomm,TimeAtRecomm, Candidate ID, End point index of cand, Edit operations trace of cand, Edit Distance of Candidate, #Level_1_EditOps, #ObjectsInSameOrder"
				 * + ",NextActivityForRecomm,CandidateTimeline,CurrentTimeline"
				 */
				if (writefull || VerbosityConstants.WriteRedundant)
				{
					userString = userAtRecomm;
					dateString = dateAtRecomm.toString();
					timeString = timeAtRecomm.toString();
					currentTimelineString = getStringActivityObjArray(currentTimeline);
					// current timeline is same throughout an execution of this method.
					writefull = false;
				}

				bw.write(userString + "," + dateString + "," + timeString + "," + candTimelineID + "," + " " + ","
						+ editOperationsString + "," + editDist + "," + countOfL1Ops + "," + countOfL2Ops + ","
						+ topNextAOName + "," + candidateTimelineAsString + "," + currentTimelineString);
				// else
				// // no need to write same repeating things everytime
				// {
				// bw.write("',','," + candTimelineID + "," + " " + "," + editOperationsString + "," + editDist + "," +
				// countOfL1Ops + "," + countOfL2Ops + ","
				// + topNextAOName + ","
				// + candidateTimelineAsString + "," + ",'");
				// }
				bw.newLine();
			}
			bw.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Writes the file EditDistancePerRtPerCand.csv
	 * 
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param editDistancesSorted
	 * @param candidateTimelines
	 * @param currentActivityObject
	 * @param nextActObjs
	 * @param currentTimeline
	 * @param writeCandidateTimeline
	 * @param writeEditOperations
	 * @param endPointIndicesInCands
	 */
	public static void writeEditDistancesPerRtPerCand(String userAtRecomm, Date dateAtRecomm, Time timeAtRecomm,
			LinkedHashMap<String, Pair<String, Double>> editDistancesSorted,
			LinkedHashMap<String, Timeline> candidateTimelines,
			LinkedHashMap<String, Pair<ActivityObject, Double>> nextActObjs, ArrayList<ActivityObject> currentTimeline,
			ActivityObject currentActivityObject, boolean writeCandidateTimeline, boolean writeEditOperations,
			LinkedHashMap<String, Integer> endPointIndicesInCands)
	// LinkedHashMap<String, Integer> endPointsOfLeastDisSubseq, Enums.LookPastType lookPastType,
	// Enums.CaseType caseType)
	{
		commonPath = Constant.getCommonPath();
		try
		{
			StringBuilder sbToWrite = new StringBuilder();
			boolean writefull = true;

			for (Map.Entry<String, Pair<String, Double>> entry : editDistancesSorted.entrySet())
			{
				String candTimelineID = entry.getKey();
				String editOps = entry.getValue().getFirst();
				double editDist = entry.getValue().getSecond();

				int countOfL1Ops = UtilityBelt.getCountOfLevel1Ops(editOps);// entry.getValue().getFirst());
				int countOfL2Ops = UtilityBelt.getCountOfLevel2Ops(editOps);// entry.getValue().getFirst());

				String nextAOName = nextActObjs.get(candTimelineID).getFirst().getActivityName();// "null";

				String candidateTimelineAsString = " ";
				String editOperationsString = " ";

				if (writeCandidateTimeline)
				{
					candidateTimelineAsString = candidateTimelines.get(candTimelineID)
							.getActivityObjectNamesWithTimestampsInSequence();
				}

				if (writeEditOperations)
				{
					editOperationsString = editOps;
				}

				String userString = "'", dateString = "'", timeString = "'", currentTimelineString = "";

				/*
				 * "UserAtRecomm,DateAtRecomm,TimeAtRecomm, Candidate ID, End point index of cand, Edit operations trace of cand, Edit Distance of Candidate, #Level_1_EditOps, #ObjectsInSameOrder"
				 * + ",NextActivityForRecomm,CandidateTimeline,CurrentTimeline"
				 */
				if (writefull || VerbosityConstants.WriteRedundant)
				{
					userString = userAtRecomm;
					dateString = dateAtRecomm.toString();
					timeString = timeAtRecomm.toString();
					currentTimelineString = getStringActivityObjArray(currentTimeline);
					// current timeline is same throughout an execution of this method.
					writefull = false;
				}

				///
				Integer endPointIndexInCand = endPointIndicesInCands.get(candTimelineID);
				ActivityObject endPointActivityInCandidate = candidateTimelines.get(candTimelineID)
						.getActivityObjectAtPosition(endPointIndexInCand);
				// difference in start time of end point activity of candidate and start
				// time of current activity
				long diffStartTimeForEndPointsCand_n_GuidingInSecs = (currentActivityObject.getStartTimestamp()
						.getTime() - endPointActivityInCandidate.getStartTimestamp().getTime()) / 1000;
				// difference in end time of end point activity of candidate and end time of
				// current activity
				long diffEndTimeForEndPointsCand_n_GuidingInSecs = (currentActivityObject.getEndTimestamp().getTime()
						- endPointActivityInCandidate.getEndTimestamp().getTime()) / 1000;
				///

				sbToWrite.append(userString + "," + dateString + "," + timeString + "," + candTimelineID + ","
						+ endPointIndexInCand + "," + editOperationsString + "," + editDist + "," + countOfL1Ops + ","
						+ countOfL2Ops + "," + nextAOName + "," + diffStartTimeForEndPointsCand_n_GuidingInSecs + ","
						+ diffEndTimeForEndPointsCand_n_GuidingInSecs + "," + candidateTimelineAsString + ","
						+ currentTimelineString + "\n");

				// sbToWrite.append(userString + "," + dateString + "," + timeString + "," + candTimelineID + "," + " "
				// + "," + editOperationsString + "," + editDist + "," + countOfL1Ops + "," + countOfL2Ops + ","
				// + nextAOName + "," + candidateTimelineAsString + "," + currentTimelineString + "\n");

				// else // no need to write same repeating things everytime
				// { bw.write("',','," + candTimelineID + "," + " " + "," + editOperationsString + "," + editDist + ","
				// + countOfL1Ops + "," + countOfL2Ops + "," + topNextAOName + "," + candidateTimelineAsString + "," +
				// ",'"); }
			}
			WritingToFile.appendLineToFileAbsolute(sbToWrite.toString(), commonPath + "EditDistancePerRtPerCand.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	//
	/**
	 * Fork of writeEditDistancesPerRtPerCand but with Primary Dimension
	 * <p>
	 * Writes the file EditDistancePerRtPerCand.csv
	 * 
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param editDistancesSorted
	 * @param candidateTimelines
	 * @param currentActivityObject
	 * @param nextActObjs
	 * @param currentTimeline
	 * @param writeCandidateTimeline
	 * @param writeEditOperations
	 * @param endPointIndicesInCands
	 */
	public static void writeEditDistancesPerRtPerCand(String userAtRecomm, Date dateAtRecomm, Time timeAtRecomm,
			LinkedHashMap<String, Pair<String, Double>> editDistancesSorted,
			LinkedHashMap<String, Timeline> candidateTimelines,
			LinkedHashMap<String, Pair<ActivityObject, Double>> nextActObjs, ArrayList<ActivityObject> currentTimeline,
			ActivityObject currentActivityObject, boolean writeCandidateTimeline, boolean writeEditOperations,
			LinkedHashMap<String, Integer> endPointIndicesInCands, PrimaryDimension primaryDimension)
	// LinkedHashMap<String, Integer> endPointsOfLeastDisSubseq, Enums.LookPastType lookPastType,
	// Enums.CaseType caseType)
	{
		commonPath = Constant.getCommonPath();
		try
		{
			StringBuilder sbToWrite = new StringBuilder();
			boolean writefull = true;

			for (Map.Entry<String, Pair<String, Double>> entry : editDistancesSorted.entrySet())
			{
				String candTimelineID = entry.getKey();
				String editOps = entry.getValue().getFirst();
				double editDist = entry.getValue().getSecond();

				int countOfL1Ops = UtilityBelt.getCountOfLevel1Ops(editOps);// entry.getValue().getFirst());
				int countOfL2Ops = UtilityBelt.getCountOfLevel2Ops(editOps);// entry.getValue().getFirst());

				String nextAOPDVals = nextActObjs.get(candTimelineID).getFirst().getPrimaryDimensionVal("/");
				// //.getPrimaryDimensionVal().stream()
				// .map(e -> e.toString()).collect(Collectors.joining("/"));// .getActivityName();//
				// "null";

				String candidateTimelineAsString = " ";
				String editOperationsString = " ";

				if (writeCandidateTimeline)
				{
					candidateTimelineAsString = candidateTimelines.get(candTimelineID)
							.getActivityObjectPDValsWithTimestampsInSequence();
				}

				if (writeEditOperations)
				{
					editOperationsString = editOps;
				}

				String userString = "'", dateString = "'", timeString = "'", currentTimelineString = "";

				/*
				 * "UserAtRecomm,DateAtRecomm,TimeAtRecomm, Candidate ID, End point index of cand, Edit operations trace of cand, Edit Distance of Candidate, #Level_1_EditOps, #ObjectsInSameOrder"
				 * + ",NextActivityForRecomm,CandidateTimeline,CurrentTimeline"
				 */
				if (writefull || VerbosityConstants.WriteRedundant)
				{
					userString = userAtRecomm;
					dateString = dateAtRecomm.toString();
					timeString = timeAtRecomm.toString();
					currentTimelineString = getStringActivityObjArray(currentTimeline);
					// current timeline is same throughout an execution of this method.
					writefull = false;
				}

				///
				Integer endPointIndexInCand = endPointIndicesInCands.get(candTimelineID);
				ActivityObject endPointActivityInCandidate = candidateTimelines.get(candTimelineID)
						.getActivityObjectAtPosition(endPointIndexInCand);
				// difference in start time of end point activity of candidate and start
				// time of current activity
				long diffStartTimeForEndPointsCand_n_GuidingInSecs = (currentActivityObject.getStartTimestamp()
						.getTime() - endPointActivityInCandidate.getStartTimestamp().getTime()) / 1000;
				// difference in end time of end point activity of candidate and end time of
				// current activity
				long diffEndTimeForEndPointsCand_n_GuidingInSecs = (currentActivityObject.getEndTimestamp().getTime()
						- endPointActivityInCandidate.getEndTimestamp().getTime()) / 1000;
				///

				sbToWrite.append(userString + "," + dateString + "," + timeString + "," + candTimelineID + ","
						+ endPointIndexInCand + "," + editOperationsString + "," + editDist + "," + countOfL1Ops + ","
						+ countOfL2Ops + "," + nextAOPDVals + "," + diffStartTimeForEndPointsCand_n_GuidingInSecs + ","
						+ diffEndTimeForEndPointsCand_n_GuidingInSecs + "," + candidateTimelineAsString + ","
						+ currentTimelineString + "\n");

				// sbToWrite.append(userString + "," + dateString + "," + timeString + "," + candTimelineID + "," + " "
				// + "," + editOperationsString + "," + editDist + "," + countOfL1Ops + "," + countOfL2Ops + ","
				// + nextAOName + "," + candidateTimelineAsString + "," + currentTimelineString + "\n");

				// else // no need to write same repeating things everytime
				// { bw.write("',','," + candTimelineID + "," + " " + "," + editOperationsString + "," + editDist + ","
				// + countOfL1Ops + "," + countOfL2Ops + "," + topNextAOName + "," + candidateTimelineAsString + "," +
				// ",'"); }
			}
			WritingToFile.appendLineToFileAbsolute(sbToWrite.toString(), commonPath + "EditDistancePerRtPerCand.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param array
	 * @return
	 */
	public static String getStringActivityObjArray(ArrayList<ActivityObject> array)
	{
		String s = "";

		for (ActivityObject ao : array)
		{
			s += ">>" + ao.getActivityName() + "--" + ao.getPrimaryDimensionVal("|") + "--" + ao.getStartTimestamp()
					+ "--" + ao.getDurationInSeconds();
		}
		return s;
	}

	/**
	 * Creates the file EditDistancePerRtPerCand.csv and write the header line
	 */
	public static void writeDistanceScoresSortedMapHeader()
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "EditDistancePerRtPerCand.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(
					"UserAtRecomm,DateAtRecomm,TimeAtRecomm, Candidate ID, End point index of cand, Edit operations trace of cand, Edit Distance of Candidate, #Level_1_EditOps, #ObjectsInSameOrder"
							+ ",NextActivityForRecomm,CandidateTimeline,CurrentTimeline");
			bw.newLine();
			bw.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void writeStartTimeDistancesSorted(
			LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> getDistanceScoresSorted)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + "StartTimeDistancePerRtPerCand.csv";

			FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Map.Entry<Date, Triple<Integer, ActivityObject, Double>> entry : getDistanceScoresSorted.entrySet())
			{
				bw.write(entry.getKey() + "," + entry.getValue().getFirst() + "," + entry.getValue().getSecond() + ","
						+ entry.getValue().getThird());
				bw.newLine();
			}
			bw.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param timelinesPhrase
	 */
	public static void writeNumOfDistinctValidActivitiesPerDayInGivenDayTimelines(String userName,
			LinkedHashMap<Date, Timeline> userTimelines, String timelinesPhrase)
	{
		commonPath = Constant.getCommonPath();//
		StringBuilder toWrite = new StringBuilder();

		try
		{
			System.out.println("writing " + userName + "CountDistinctValidIn" + timelinesPhrase + ".csv");
			String fileName = commonPath + userName + "CountDistinctValidIn" + timelinesPhrase + ".csv";

			File file = new File(fileName);
			file.delete();

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("Date, Num_of_Distict_Valid_Activities\n");// bw.newLine();

			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{
				int numOfDistinctValidActivities = entry.getValue().countNumberOfValidDistinctActivities();
				toWrite.append(entry.getKey() + "," + numOfDistinctValidActivities + "\n");
				// bw.write(entry.getKey() + "," + numOfDistinctValidActivities);
				// bw.newLine();
			}
			bw.write(toWrite.toString());
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
	}

	/**
	 * Write all the given day timelines.
	 * 
	 * @param usersDayTimelines
	 * @param timelinesPhrase
	 * @param writeStartEndGeocoordinates
	 * @param writeDistanceTravelled
	 * @param writeAvgAltitude
	 */
	public static void writeUsersDayTimelines(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines,
			String timelinesPhrase, boolean writeStartEndGeocoordinates, boolean writeDistanceTravelled,
			boolean writeAvgAltitude)
	{
		// System.out.println("Common path=" + commonPath);
		commonPath = Constant.getCommonPath();//
		System.out.println("Inside writeUsersDayTimelines(): num of users received = " + usersDayTimelines.size());
		System.out.println("Common path=" + commonPath);
		try
		{
			for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
			{
				writeGivenDayTimelines(entry.getKey(), entry.getValue(), timelinesPhrase, writeStartEndGeocoordinates,
						writeDistanceTravelled, writeAvgAltitude);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
		System.out.println("Exiting writeUsersDayTimelines()");
	}

	/**
	 * Write all the given day timelines.
	 * 
	 * @param usersDayTimelines
	 * @param timelinesPhrase
	 * @param writeStartEndGeocoordinates
	 * @param writeDistanceTravelled
	 * @param writeAvgAltitude
	 */
	public static void writeUsersDayTimelinesSameFile(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String timelinesPhrase,
			boolean writeStartEndGeocoordinates, boolean writeDistanceTravelled, boolean writeAvgAltitude,
			String fileName)
	{
		// System.out.println("Common path=" + commonPath);
		commonPath = Constant.getCommonPath();//
		System.out.println(
				"Inside writeUsersDayTimelinesSameFile(): num of users received = " + usersDayTimelines.size());
		System.out.println("Common path=" + commonPath);
		try
		{
			for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
			{
				writeGivenDayTimelinesSameFile2(entry.getKey(), entry.getValue(), timelinesPhrase,
						writeStartEndGeocoordinates, writeDistanceTravelled, writeAvgAltitude, fileName);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
		System.out.println("Exiting writeUsersDayTimelinesSameFile()");
	}

	/**
	 * 
	 * @param usersDayTimelines
	 * @param timelinesPhrase
	 * @param fileName
	 */
	public static void writeNumOfActsPerUsersDayTimelinesSameFile(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String timelinesPhrase,
			String fileName)
	{
		// System.out.println("Common path=" + commonPath);
		commonPath = Constant.getCommonPath();//
		System.out.println("Inside writeNumOfActsPerUsersDayTimelinesSameFile(): num of users received = "
				+ usersDayTimelines.size());
		System.out.println("Common path=" + commonPath);
		try
		{
			for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
			{
				writeNumOfActsInGivenDayTimelinesSameFile(entry.getKey(), entry.getValue(), timelinesPhrase, fileName);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
		System.out.println("Exiting writeNumOfActsPerUsersDayTimelinesSameFile()");
	}

	/**
	 * 
	 * @param usersDayTimelines
	 * @param timelinesPhrase
	 * @param fileName
	 */
	public static void writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String timelinesPhrase,
			String fileName)
	{
		// System.out.println("Common path=" + commonPath);
		commonPath = Constant.getCommonPath();//
		System.out.println("Inside writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(): num of users received = "
				+ usersDayTimelines.size());
		System.out.println("Common path=" + commonPath);
		try
		{
			for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
			{
				writeNumOfDistinctValidActsInGivenDayTimelinesSameFile(entry.getKey(), entry.getValue(),
						timelinesPhrase, fileName);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
		System.out.println("Exiting writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile()");
	}

	/**
	 * 
	 * @param usersDayTimelines
	 * @param absFileName
	 */
	public static void writeNumOfDaysPerUsersDayTimelinesSameFile(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String absFileName)
	{
		// System.out.println("Common path=" + commonPath);
		commonPath = Constant.getCommonPath();//
		System.out.println("Inside writeNumOfDaysPerUsersDayTimelinesSameFile(): num of users received = "
				+ usersDayTimelines.size());
		System.out.println("Common path=" + commonPath);
		StringBuilder msg = new StringBuilder();
		msg.append("User,#Days\n");
		try
		{
			for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
			{
				msg.append(entry.getKey() + "," + entry.getValue().size() + "\n");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}

		WritingToFile.writeToNewFile(msg.toString(), absFileName);
		System.out.println("Exiting writeNumOfDaysPerUsersDayTimelinesSameFile()");
	}

	/**
	 * Write all day timelines for a given user
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param timelinesPhrase
	 * @param writeStartEndGeoCoordinates
	 * @param writeDistanceTravelled
	 * @param writeAvgAltitude
	 */
	public static void writeGivenDayTimelines(String userName, LinkedHashMap<Date, Timeline> userTimelines,
			String timelinesPhrase, boolean writeStartEndGeoCoordinates, boolean writeDistanceTravelled,
			boolean writeAvgAltitude)
	{
		commonPath = Constant.getCommonPath();//

		try
		{
			System.out.println("writing " + userName + "DayTimelines" + timelinesPhrase + ".csv");

			StringBuilder toWrite = new StringBuilder();
			String fileName = commonPath + userName + "DayTimelines" + timelinesPhrase + ".csv";
			// PopUps.showMessage("Writing day timelines to" + fileName);

			// File file = new File(fileName);
			// file.delete();
			//
			// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			// BufferedWriter bw = new BufferedWriter(fw);

			// bw.write
			toWrite.append("Date, DayTimeline\n");
			// bw.newLine();

			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{

				// bw.write
				toWrite.append(entry.getKey() + ",");
				ArrayList<ActivityObject> ActivityObjects = entry.getValue().getActivityObjectsInDay();

				// if(!writeStartEndGeoCoordinates && !distanceTravelled && !)
				for (int i = 0; i < ActivityObjects.size(); i++)
				{
					// bw.write
					toWrite.append(ActivityObjects.get(i).getActivityName() + "__"
							+ ActivityObjects.get(i).getStartTimestamp().getHours() + ":"
							+ ActivityObjects.get(i).getStartTimestamp().getMinutes() + ":"
							+ ActivityObjects.get(i).getStartTimestamp().getSeconds() + "_to_"
							+ ActivityObjects.get(i).getEndTimestamp().getHours() + ":"
							+ ActivityObjects.get(i).getEndTimestamp().getMinutes() + ":"
							+ ActivityObjects.get(i).getEndTimestamp().getSeconds());// + ",");

					if (Constant.getDatabaseName().equals("geolife1"))
					{
						if (writeStartEndGeoCoordinates)
						{
							// bw.write
							toWrite.append("__(" + ActivityObjects.get(i).getStartLatitude() + "-"
									+ ActivityObjects.get(i).getStartLongitude() + ") to ("
									+ ActivityObjects.get(i).getEndLatitude() + "-"
									+ ActivityObjects.get(i).getEndLongitude() + ")");
						}
						if (writeDistanceTravelled)
						{
							// bw.write
							toWrite.append("__" + ActivityObjects.get(i).getDistanceTravelled());
						}
						if (writeAvgAltitude)
						{
							// bw.write
							toWrite.append("__" + ActivityObjects.get(i).getAvgAltitude());
						}
					}

					// bw.write
					toWrite.append(",");
				}
				// bw.newLine();
				// bw.write
				toWrite.append("\n");
			}
			WritingToFile.writeToNewFile(toWrite.toString(), fileName);
			// bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
	}

	/**
	 * Write all day timelines for a given user
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param timelinesPhrase
	 * @param writeStartEndGeoCoordinates
	 * @param writeDistanceTravelled
	 * @param writeAvgAltitude
	 */
	public static void writeGivenDayTimelinesSameFile(String userName, LinkedHashMap<Date, Timeline> userTimelines,
			String timelinesPhrase, boolean writeStartEndGeoCoordinates, boolean writeDistanceTravelled,
			boolean writeAvgAltitude, String fileName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			fileName = commonPath + fileName;// userName + "DayTimelines" + timelinesPhrase + ".csv";
			// PopUps.showMessage("Writing day timelines to" + fileName);
			// System.out.println("writing " + userName + "DayTimelines" + timelinesPhrase + ".csv");
			//
			// File file = new File(fileName);
			// file.delete();
			//
			// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			// BufferedWriter bw = new BufferedWriter(fw);
			//
			// bw.write("Date, DayTimeline");
			// bw.newLine();

			String toWrite = "";
			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{

				toWrite += (userName + "," + entry.getKey() + ",");
				ArrayList<ActivityObject> ActivityObjects = entry.getValue().getActivityObjectsInDay();

				// if(!writeStartEndGeoCoordinates && !distanceTravelled && !)
				for (int i = 0; i < ActivityObjects.size(); i++)
				{
					toWrite += (ActivityObjects.get(i).getActivityName() + "__"
							+ ActivityObjects.get(i).getStartTimestamp().getHours() + ":"
							+ ActivityObjects.get(i).getStartTimestamp().getMinutes() + ":"
							+ ActivityObjects.get(i).getStartTimestamp().getSeconds() + "_to_"
							+ ActivityObjects.get(i).getEndTimestamp().getHours() + ":"
							+ ActivityObjects.get(i).getEndTimestamp().getMinutes() + ":"
							+ ActivityObjects.get(i).getEndTimestamp().getSeconds());// + ",");

					if (Constant.getDatabaseName().equals("geolife1"))
					{
						if (writeStartEndGeoCoordinates)
						{
							toWrite += ("__(" + ActivityObjects.get(i).getStartLatitude() + "-"
									+ ActivityObjects.get(i).getStartLongitude() + ") to ("
									+ ActivityObjects.get(i).getEndLatitude() + "-"
									+ ActivityObjects.get(i).getEndLongitude() + ")");
						}
						if (writeDistanceTravelled)
						{
							toWrite += ("__" + ActivityObjects.get(i).getDistanceTravelled());
						}
						if (writeAvgAltitude)
						{
							toWrite += ("__" + ActivityObjects.get(i).getAvgAltitude());
						}
					}

					toWrite += (",");
				}
				toWrite += ("\n");
			}

			// bw.close();
			WritingToFile.appendLineToFileAbsolute(toWrite, fileName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
	}

	/**
	 * Write all day timelines for a given user
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param timelinesPhrase
	 * @param writeStartEndGeoCoordinates
	 * @param writeDistanceTravelled
	 * @param writeAvgAltitude
	 */
	public static void writeGivenDayTimelinesSameFile2(String userName, LinkedHashMap<Date, Timeline> userTimelines,
			String timelinesPhrase, boolean writeStartEndGeoCoordinates, boolean writeDistanceTravelled,
			boolean writeAvgAltitude, String fileName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			fileName = commonPath + fileName;
			StringBuffer toWrite = new StringBuffer();
			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{
				toWrite.append(userName + "," + entry.getKey() + ",");
				ArrayList<ActivityObject> ActivityObjects = entry.getValue().getActivityObjectsInDay();

				for (int i = 0; i < ActivityObjects.size(); i++)
				{
					if (Constant.getDatabaseName().equals("gowalla1"))
					{
						toWrite.append(ActivityObjects.get(i).toStringAllGowallaTSWithName());// toStringAllGowalla());//toStringAllGowallaTSWithName
					}
					else
					{
						toWrite.append(ActivityObjects.get(i).getActivityName() + "__"
								+ ActivityObjects.get(i).getStartTimestamp().getHours() + ":"
								+ ActivityObjects.get(i).getStartTimestamp().getMinutes() + ":"
								+ ActivityObjects.get(i).getStartTimestamp().getSeconds() + "_to_"
								+ ActivityObjects.get(i).getEndTimestamp().getHours() + ":"
								+ ActivityObjects.get(i).getEndTimestamp().getMinutes() + ":"
								+ ActivityObjects.get(i).getEndTimestamp().getSeconds());// + ",");

						if (Constant.getDatabaseName().equals("geolife1"))
						{
							if (writeStartEndGeoCoordinates)
							{
								toWrite.append("__(" + ActivityObjects.get(i).getStartLatitude() + "-"
										+ ActivityObjects.get(i).getStartLongitude() + ") to ("
										+ ActivityObjects.get(i).getEndLatitude() + "-"
										+ ActivityObjects.get(i).getEndLongitude() + ")");
							}
							if (writeDistanceTravelled)
							{
								toWrite.append("__" + ActivityObjects.get(i).getDistanceTravelled());
							}
							if (writeAvgAltitude)
							{
								toWrite.append("__" + ActivityObjects.get(i).getAvgAltitude());
							}
						}
					}
					toWrite.append(",");
				}
				toWrite.append("\n");
			}
			WritingToFile.appendLineToFileAbsolute(toWrite.toString(), fileName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
	}

	/**
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param timelinesPhrase
	 * @param fileName
	 */
	public static void writeNumOfActsInGivenDayTimelinesSameFile(String userName,
			LinkedHashMap<Date, Timeline> userTimelines, String timelinesPhrase, String fileName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			fileName = commonPath + fileName;
			StringBuilder toWrite = new StringBuilder();
			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{
				toWrite.append(
						userName + "," + entry.getKey() + "," + entry.getValue().getActivityObjectsInDay().size());
				toWrite.append("\n");
			}
			WritingToFile.appendLineToFileAbsolute(toWrite.toString(), fileName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
	}

	/////
	/**
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param timelinesPhrase
	 * @param fileName
	 */
	public static void writeNumOfDistinctValidActsInGivenDayTimelinesSameFile(String userName,
			LinkedHashMap<Date, Timeline> userTimelines, String timelinesPhrase, String fileName)
	{
		commonPath = Constant.getCommonPath();//
		try
		{
			fileName = commonPath + fileName;
			StringBuilder toWrite = new StringBuilder();
			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{
				toWrite.append(userName + "," + entry.getKey() + ","
						+ entry.getValue().countNumberOfValidDistinctActivities() + "\n");
			}
			WritingToFile.appendLineToFileAbsolute(toWrite.toString(), fileName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}
	}

	/////
	/**
	 * Sums the duration in seconds of activities for each of the days of given day timelines and writes it to a file
	 * and sums the duration activities over all days of given timelines and return it as a LinkedHashMap
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param fileNamePhrase
	 * @return duration of activities over all days of given timelines
	 */
	public static LinkedHashMap<String, Long> writeActivityDurationInGivenDayTimelines(String userName,
			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase)
	{
		commonPath = Constant.getCommonPath();//
		String[] activityNames = Constant.getActivityNames();// activityNames;
		LinkedHashMap<String, Long> activityNameDurationPairsOverAllDayTimelines = new LinkedHashMap<String, Long>();
		// count over all the days

		try
		{
			// String userName=entryForUser.getKey();
			// System.out.println("\nUser ="+entryForUser.getKey());
			String fileName = commonPath + userName + "ActivityDuration" + fileNamePhrase + ".csv";

			if (VerbosityConstants.verbose)
			{
				System.out.println("writing " + userName + "ActivityDuration" + fileNamePhrase + ".csv");
			}

			StringBuilder toWrite = new StringBuilder();

			toWrite.append(",");
			// bw.write(",");

			for (String activityName : activityNames)
			{
				if (UtilityBelt.isValidActivityName(activityName) == false)
				// (activityName.equals("Unknown")|| activityName.equals("Others"))
				{
					continue;
				}
				toWrite.append("," + activityName);
				// bw.write("," + activityName);
				activityNameDurationPairsOverAllDayTimelines.put(activityName, new Long(0));
			}
			toWrite.append("\n");
			// bw.newLine();

			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{
				// System.out.println("Date =" + entry.getKey());
				// bw.write(entry.getKey().toString());
				// bw.write("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));

				toWrite.append(entry.getKey().toString() + ","
						+ (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));

				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();
				LinkedHashMap<String, Long> activityNameDurationPairs = new LinkedHashMap<String, Long>();

				for (String activityName : activityNames) // written beforehand to maintain the same order of activity
															// names
				{
					if (UtilityBelt.isValidActivityName(activityName))
					// if((activityName.equalsIgnoreCase("Others")||activityName.equalsIgnoreCase("Unknown"))==false)
					{
						activityNameDurationPairs.put(activityName, new Long(0));
					}
				}

				for (ActivityObject actEvent : activitiesInDay)
				{
					if (UtilityBelt.isValidActivityName(actEvent.getActivityName()))
					// if((actEvent.getActivityName().equalsIgnoreCase("Unknown") ||
					// actEvent.getActivityName().equalsIgnoreCase("Others") ) ==false)
					{
						Long durationInSecondsForActivity = actEvent.getDurationInSeconds();
						// summing of duration for current day
						activityNameDurationPairs.put(actEvent.getActivityName(),
								activityNameDurationPairs.get(actEvent.getActivityName())
										+ durationInSecondsForActivity);

						// accumulative duration over all days
						activityNameDurationPairsOverAllDayTimelines.put(actEvent.getActivityName(),
								activityNameDurationPairsOverAllDayTimelines.get(actEvent.getActivityName())
										+ durationInSecondsForActivity);
					}
				}

				// write the activityNameDurationPairs to the file
				for (Map.Entry<String, Long> entryWrite : activityNameDurationPairs.entrySet())
				{
					// bw.write("," + entryWrite.getValue());
					toWrite.append("," + entryWrite.getValue());
				}
				toWrite.append("\n");
				// bw.newLine();
			}
			// File file = new File(fileName);
			// file.delete();
			// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			// BufferedWriter bw = new BufferedWriter(fw);
			WritingToFile.writeToNewFile(toWrite.toString(), fileName);
			// bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}

		writeSimpleLinkedHashMapToFileAppend(activityNameDurationPairsOverAllDayTimelines,
				commonPath + "ActivityNameDurationPairsOver" + fileNamePhrase + ".csv", "Activity", "Duration");
		// TODO check if it indeed should be an append

		return activityNameDurationPairsOverAllDayTimelines;

	}

	// ///////////////////
	/**
	 * Counts activities for each of the days of given day timelines and writes it to a file and counts activities over
	 * all days of given timelines and return it as a LinkedHashMap (fileName = commonPath + userName + "ActivityCounts"
	 * + fileNamePhrase + ".csv")
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param fileNamePhrase
	 * @return count of activities over all days of given timelines
	 */
	public static LinkedHashMap<String, Long> writeActivityCountsInGivenDayTimelines(String userName,
			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase)
	{
		commonPath = Constant.getCommonPath();//

		if (VerbosityConstants.verbose) System.out.println("Inside writeActivityCountsInGivenDayTimelines");

		/* <Activity Name, count over all days> */
		LinkedHashMap<String, Long> activityNameCountPairsOverAllDayTimelines = new LinkedHashMap<String, Long>();
		// count over all the days
		String[] activityNames = Constant.getActivityNames();// .activityNames;
		try
		{
			// String userName=entryForUser.getKey();
			// System.out.println("\nUser ="+entryForUser.getKey());
			String fileName = commonPath + userName + "ActivityCounts" + fileNamePhrase + ".csv";

			if (VerbosityConstants.verbose)
			{
				System.out.println("writing " + userName + "ActivityCounts" + fileNamePhrase + ".csv");
			}
			// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileName);// new BufferedWriter(fw);

			StringBuilder bwString = new StringBuilder();
			bwString.append(",");
			// bw.write(",");

			for (String activityName : activityNames)
			{
				if (UtilityBelt.isValidActivityName(activityName) == false) // if(activityName.equals("Unknown")||
																			// activityName.equals("Not Available"))
				{
					continue;
				}
				// bw.write("," + activityName);
				bwString.append("," + activityName);
				// System.out.println("ajooba:activityName = " + activityName + " bwString" + bwString.toString());
				activityNameCountPairsOverAllDayTimelines.put(activityName, new Long(0));
			}
			// bw.newLine();
			bwString.append("\n");

			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{
				// System.out.println("Date =" + entry.getKey());
				// bw.write(entry.getKey().toString());
				// bw.write("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));

				bwString.append(entry.getKey().toString());
				bwString.append("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));

				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();

				/* <Activity Name, count for the current day> */
				LinkedHashMap<String, Integer> activityNameCountPairs = new LinkedHashMap<String, Integer>();

				// written beforehand to maintain the same order of activity names
				for (String activityName : activityNames)
				{
					if (UtilityBelt.isValidActivityName(activityName))
					// if((activityName.equalsIgnoreCase("Not
					// Available")||activityName.equalsIgnoreCase("Unknown"))==false)
					{
						// System.out.println(" putting down -" + activityName + "- in activityNameCountPairs");
						activityNameCountPairs.put(activityName, 0);
					}
				}

				for (ActivityObject actEvent : activitiesInDay)
				{
					if (UtilityBelt.isValidActivityName(actEvent.getActivityName()))
					// if((actEvent.getActivityName().equalsIgnoreCase("Unknown") ||
					// actEvent.getActivityName().equalsIgnoreCase("Not Available") ) ==false)
					{
						String actName = actEvent.getActivityName();
						// System.out.println(activityNameCountPairs.size());

						// Integer val;
						// if (activityNameCountPairs.get(actName) == null)
						// {
						// val = 0;
						// }
						// else
						// {
						// val = activityNameCountPairs.get(actName);
						// }
						Integer val = activityNameCountPairs.get(actName);
						if (val == null)
						{
							new Exception(
									"Exception in org.activity.io.WritingToFile.writeActivityCountsInGivenDayTimelines(String, LinkedHashMap<Date, Timeline>, String) : actName = "
											+ actName + " has null val");// System.out.println("actName = " + actName);
						}

						// System.out.println("val:" + val);
						Integer newVal = new Integer(val.intValue() + 1);
						// count for current day
						activityNameCountPairs.put(actName, newVal);

						// accumulative count over all days
						activityNameCountPairsOverAllDayTimelines.put(actEvent.getActivityName(),
								activityNameCountPairsOverAllDayTimelines.get(actEvent.getActivityName()) + 1);
					}
				}

				// write the activityNameCountPairs to the file
				for (Map.Entry<String, Integer> entryWrite : activityNameCountPairs.entrySet())
				{
					// bw.write("," + entryWrite.getValue());
					bwString.append("," + entryWrite.getValue());
				}

				bwString.append("\n");
				// bw.newLine();
			}
			WritingToFile.writeToNewFile(bwString.toString(), fileName);
			// bw.write(bwString.toString());
			// bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}

		writeSimpleLinkedHashMapToFileAppend(activityNameCountPairsOverAllDayTimelines,
				commonPath + "ActivityNameCountPairsOver" + fileNamePhrase + ".csv", "Activity", "Count");
		// TODO check if it indeed should be an append

		if (VerbosityConstants.verbose) System.out.println("Exiting writeActivityCountsInGivenDayTimelines");

		return activityNameCountPairsOverAllDayTimelines;

	}

	// ///////////////////
	/**
	 * percentage of timelines in which the activity occurrs and counts activities over all days of given timelines and
	 * return it as a LinkedHashMap
	 * 
	 * @param userName
	 * @param userTimelines
	 * @param fileNamePhrase
	 * @return count of activities over all days of given timelines
	 */
	public static LinkedHashMap<String, Double> writeActivityOccPercentageOfTimelines(String userName,
			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase)
	{
		commonPath = Constant.getCommonPath();//
		LinkedHashMap<String, Double> activityNameCountPairsOverAllDayTimelines = new LinkedHashMap<String, Double>();
		String[] activityNames = Constant.getActivityNames();// .activityNames;
		try
		{
			// String userName=entryForUser.getKey();
			// System.out.println("\nUser ="+entryForUser.getKey());
			String fileName = commonPath + userName + "ActivityOccPerTimelines" + fileNamePhrase + ".csv";

			if (VerbosityConstants.verbose)
			{
				System.out.println("writing " + userName + "ActivityOccPerTimelines" + fileNamePhrase + ".csv");
			}

			StringBuilder toWrite = new StringBuilder();
			// bw.write(",");

			int actIndex = -1;
			for (String activityName : activityNames)
			{
				actIndex += 1;
				if (UtilityBelt.isValidActivityName(activityName) == false)
				// if(activityName.equals("Unknown")|| activityName.equals("Not Available"))
				{
					continue;
				}

				if (Constant.getDatabaseName().equals("gowalla1"))
				{
					// bw.write("," + activityName);
					toWrite.append("," + Constant.activityNamesGowallaLabels.get(actIndex));
				}
				else
				{
					// bw.write("," + activityName);
					toWrite.append("," + activityName);

				}

				activityNameCountPairsOverAllDayTimelines.put(activityName, new Double(0));
			}
			// bw.newLine();
			toWrite.append("\n");

			double numOfTimelines = userTimelines.size();

			for (String activityName : activityNames) // written beforehand to maintain the same order of activity names
			{
				if (UtilityBelt.isValidActivityName(activityName))
				// if((activityName.equalsIgnoreCase("Not Available")||activityName.equalsIgnoreCase("Unknown"))==false)
				{
					activityNameCountPairsOverAllDayTimelines.put(activityName, new Double(0));
				}
			}

			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
			{
				// System.out.println("Date =" + entry.getKey());
				// bw.write(entry.getKey().toString());
				// bw.write("," + (UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())));

				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();

				// written beforehand to maintain the same order of activity names
				for (String activityName : activityNames)
				{
					if (UtilityBelt.isValidActivityName(activityName))
					{
						if (entry.getValue().hasActivityName(activityName) == true)
						{
							activityNameCountPairsOverAllDayTimelines.put(activityName,
									activityNameCountPairsOverAllDayTimelines.get(activityName) + 1);
						}
					}
				}
			}

			// write the activityNameCountPairs to the file
			for (Map.Entry<String, Double> entryWrite : activityNameCountPairsOverAllDayTimelines.entrySet())
			{
				String actName = entryWrite.getKey();
				Double val = entryWrite.getValue();
				double percentageOccurrenceOverTimeline = ((double) activityNameCountPairsOverAllDayTimelines
						.get(actName) / (double) numOfTimelines) * 100;
				activityNameCountPairsOverAllDayTimelines.put(actName, percentageOccurrenceOverTimeline);
				// bw.write("," + percentageOccurrenceOverTimeline);
				toWrite.append("," + percentageOccurrenceOverTimeline);
			}
			// bw.newLine();
			toWrite.append("\n");

			// File file = new File(fileName);
			// file.delete();
			// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			// BufferedWriter bw = new BufferedWriter(fw);
			WritingToFile.writeToNewFile(toWrite.toString(), fileName);
			// bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-5);
		}

		writeSimpleLinkedHashMapToFileAppend(activityNameCountPairsOverAllDayTimelines,
				commonPath + "ActivityOccPerTimelines" + fileNamePhrase + ".csv", "Activity", "Count");// TODO check if
																										// it indeed
		// should be an append

		return activityNameCountPairsOverAllDayTimelines;

	}

	/*
	 * This method is called from DCU_DataLoader
	 */
	public static int writeActivityDistributionOcurrence(LinkedHashMap<String, TreeMap<Timestamp, String>> allData) // OUTOUT
																													// VALIDATED
																													// WITH
																													// SQL
																													// OUTPUT
																													// OK
	{
		commonPath = Constant.getCommonPath();//
		// <User , <day-month-year, <activity name, count of occurence> >>
		LinkedHashMap<String, TreeMap<String, LinkedHashMap<String, Integer>>> dataToWrite = new LinkedHashMap<String, TreeMap<String, LinkedHashMap<String, Integer>>>();
		String[] activityNames = Constant.getActivityNames();// .activityNames;
		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : allData.entrySet())
		{
			try
			{
				String userName = entryForUser.getKey();

				System.out.println("\nUser =" + entryForUser.getKey());

				// <day-month-year, <ActvityName, count of occurence>>
				TreeMap<String, LinkedHashMap<String, Integer>> mapForEachUser = new TreeMap<String, LinkedHashMap<String, Integer>>();

				int countOfDays = 0;

				for (Map.Entry<Timestamp, String> entry : entryForUser.getValue().entrySet())
				{
					// System.out.println(T)
					int date = entry.getKey().getDate();
					int month = entry.getKey().getMonth() + 1;
					int year = entry.getKey().getYear() + 1900;
					String day = date + "-" + month + "-" + year;

					if (mapForEachUser.containsKey(day) == false)
					{
						LinkedHashMap<String, Integer> activityNameValue = new LinkedHashMap<String, Integer>();

						for (String activityName : activityNames)
						{
							activityNameValue.put(activityName, new Integer(0));
						}

						mapForEachUser.put(day, activityNameValue);
						countOfDays++;
					}

					String activityNameInEntry = GeolifeDataLoader.getActivityNameFromDataEntry(entry.getValue());

					Integer currentCountForActivityInDay = mapForEachUser.get(day).get(activityNameInEntry);

					mapForEachUser.get(day).put(activityNameInEntry, currentCountForActivityInDay + 1);

					System.out.println(entry.getKey() + "," + entry.getValue());// +" "+ day);
				}

				System.out.println("count of days=" + countOfDays);

				dataToWrite.put(userName, mapForEachUser);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		// //////////// LinkedHashMap<String, TreeMap<String,LinkedHashMap<String,Integer>> > dataToWrite

		for (Map.Entry<String, TreeMap<String, LinkedHashMap<String, Integer>>> entryForUser : dataToWrite.entrySet())
		{
			try
			{

				String userName = entryForUser.getKey();

				System.out.println("\nUser =" + entryForUser.getKey());
				String fileName = commonPath + userName + "ActivityDistributionOcurrence.csv";

				File file = new File(fileName);

				file.delete();

				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write(",");
				for (String activityName : activityNames)
				{
					if (UtilityBelt.isValidActivityName(activityName) == false)
					// if(activityName.equals("Unknown")|| activityName.equals("Others"))
					{
						continue;
					}
					bw.write("," + activityName);
				}
				bw.newLine();

				for (Map.Entry<String, LinkedHashMap<String, Integer>> entry : entryForUser.getValue().entrySet())
				{
					if (hasNonZeroValidActivityNamesInteger(entry.getValue()))
					{
						System.out.println("Date =" + entry.getKey());
						bw.write(entry.getKey());
						bw.write("," + DateTimeUtils.getWeekDayFromDateString(entry.getKey()));

						for (Map.Entry<String, Integer> entryForAct : entry.getValue().entrySet())
						{
							String key = entryForAct.getKey();

							if (UtilityBelt.isValidActivityName(key) == false)
							// if(key.equals("Unknown")|| key.equals("Others"))
							{
								continue;
							}

							Integer value = entryForAct.getValue();
							System.out.println(" " + key + "=" + value);
							bw.write("," + value);
						}
						bw.newLine();
					}
				}

				bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		return 0;
	}

	public static int writeActivityDistributionDuration(LinkedHashMap<String, TreeMap<Timestamp, String>> allData) // OUTOUT
																													// VALIDATED
																													// WITH
																													// SQL
																													// OUTPUT
																													// OK
	{
		commonPath = Constant.getCommonPath();//
		// <User , <day-month-year, <activity name, sum of duration in seconds> >>
		LinkedHashMap<String, TreeMap<String, LinkedHashMap<String, Long>>> dataToWrite = new LinkedHashMap<String, TreeMap<String, LinkedHashMap<String, Long>>>();
		String[] activityNames = Constant.getActivityNames();// .activityNames;
		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : allData.entrySet())
		{
			try
			{
				String userName = entryForUser.getKey();

				System.out.println("\nUser =" + entryForUser.getKey());

				// <day-month-year, <ActvityName, count of occurence>>
				TreeMap<String, LinkedHashMap<String, Long>> mapForEachUser = new TreeMap<String, LinkedHashMap<String, Long>>();

				int countOfDays = 0;

				for (Map.Entry<Timestamp, String> entry : entryForUser.getValue().entrySet())
				{
					// System.out.println(T)
					int date = entry.getKey().getDate();
					int month = entry.getKey().getMonth() + 1;
					int year = entry.getKey().getYear() + 1900;
					String day = date + "-" + month + "-" + year;

					if (mapForEachUser.containsKey(day) == false)
					{
						LinkedHashMap<String, Long> activityNameValue = new LinkedHashMap<String, Long>();

						for (String activityName : activityNames)
						{
							activityNameValue.put(activityName, new Long(0));
						}

						mapForEachUser.put(day, activityNameValue);
						countOfDays++;
					}

					String activityNameInEntry = GeolifeDataLoader.getActivityNameFromDataEntry(entry.getValue());
					long activityDurationInEntry = GeolifeDataLoader
							.getDurationInSecondsFromDataEntry(entry.getValue());

					Long currentDurationForActivityInDay = mapForEachUser.get(day).get(activityNameInEntry);

					mapForEachUser.get(day).put(activityNameInEntry,
							currentDurationForActivityInDay + activityDurationInEntry);

					System.out.println(entry.getKey() + "," + entry.getValue());// +" "+ day);
				}

				System.out.println("count of days=" + countOfDays);

				dataToWrite.put(userName, mapForEachUser);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		// //////////// LinkedHashMap<String, TreeMap<String,LinkedHashMap<String,Integer>> > dataToWrite

		for (Map.Entry<String, TreeMap<String, LinkedHashMap<String, Long>>> entryForUser : dataToWrite.entrySet())
		{
			try
			{
				String userName = entryForUser.getKey();

				System.out.println("\nUser =" + entryForUser.getKey());
				String fileName = commonPath + userName + "ActivityDistributionDuration.csv";

				File file = new File(fileName);

				file.delete();

				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write(",");
				for (String activityName : activityNames)
				{
					if (UtilityBelt.isValidActivityName(activityName) == false)
					// if(activityName.equals("Unknown")|| activityName.equals("Others"))
					{
						continue;
					}
					bw.write("," + activityName);
				}
				bw.newLine();

				for (Map.Entry<String, LinkedHashMap<String, Long>> entry : entryForUser.getValue().entrySet())
				{
					// System.out.println(T)
					if (hasNonZeroValidActivityNamesLong(entry.getValue()))
					{
						System.out.println("Date =" + entry.getKey());
						bw.write(entry.getKey());
						bw.write("," + DateTimeUtils.getWeekDayFromDateString(entry.getKey()));

						for (Map.Entry<String, Long> entryForAct : entry.getValue().entrySet())
						{
							String key = entryForAct.getKey();
							if (UtilityBelt.isValidActivityName(key) == false)
							// if(key.equals("Unknown")|| key.equals("Others"))
							{
								continue;
							}
							Long value = entryForAct.getValue();
							System.out.println(" " + key + "=" + value);
							bw.write("," + value);
						}
						bw.newLine();
					}
				}

				bw.close();

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		return 0;
	}

	public static boolean hasNonZeroValidActivityNamesInteger(LinkedHashMap<String, Integer> map)
	{
		boolean hasNonZeroValid = false;
		String[] activityNames = Constant.getActivityNames();// .activityNames;
		for (int i = 2; i < activityNames.length; i++)
		{
			if (map.get(activityNames[i]) > 1)
			{
				hasNonZeroValid = true;
				break;
			}
		}

		return hasNonZeroValid;
	}

	public static boolean hasNonZeroValidActivityNamesLong(LinkedHashMap<String, Long> map)
	{
		boolean hasNonZeroValid = false;
		String[] activityNames = Constant.getActivityNames();// .activityNames;
		for (int i = 2; i < activityNames.length; i++)
		{
			if (map.get(activityNames[i]) > 1)
			{
				hasNonZeroValid = true;
				break;
			}
		}

		return hasNonZeroValid;
	}

	/**
	 * Write a Map to file
	 * 
	 * @param map
	 * @param absFileName
	 *            with fullPath
	 * @param headerKey
	 * @param headerValue
	 */
	public static void writeSimpleMapToFile(Map<String, Long> map, String absFileName, String headerKey,
			String headerValue)
	{
		commonPath = Constant.getCommonPath();//
		if (map.size() == 0 || map == null)
		{
			new Exception("Alert! writeSimpleMapToFile, the passed map is empty or null");
		}
		try
		{

			StringBuilder sb = new StringBuilder();
			sb.append(headerKey + "," + headerValue + "\n");

			// File fileToWrite = new File(fileName); fileToWrite.delete(); fileToWrite.createNewFile();
			// BufferedWriter bw = new BufferedWriter(new FileWriter(fileToWrite));// ,true));
			// bw.write(headerKey + "," + headerValue); bw.newLine();

			for (Map.Entry<String, Long> entry : map.entrySet())
			{
				// bw.write(entry.getKey() + "," + entry.getValue());bw.newLine();
				sb.append(entry.getKey() + "," + entry.getValue() + "\n");
			}
			WritingToFile.writeToNewFile(sb.toString(), absFileName);
			// bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Write a LinkedHashMap to file
	 * 
	 * @param map
	 * @param absFileName
	 *            absolute filename i.e., with absolute address
	 * @param headerKey
	 * @param headerValue
	 */
	public static void writeSimpleLinkedHashMapToFile(LinkedHashMap<String, ?> map, String absFileName,
			String headerKey, String headerValue)
	{
		// commonPath = Constant.getCommonPath();//
		// System.out.println("Inside writeSimpleLinkedHashMapToFile" + " commonPath=" + commonPath);
		try
		{
			StringBuilder sb = new StringBuilder();
			// File fileToWrite = new File(absFileName); fileToWrite.delete(); fileToWrite.createNewFile();
			// BufferedWriter bw = new BufferedWriter(new FileWriter(fileToWrite));// ,true));
			// bw.write(headerKey + "," + headerValue); bw.newLine();

			sb.append(headerKey + "," + headerValue + "\n");

			for (Map.Entry<String, ?> entry : map.entrySet())
			{
				// bw.write(entry.getKey() + "," + entry.getValue().toString());
				// // System.out.println(entry.getKey() + "," + entry.getValue()); bw.newLine();
				sb.append(entry.getKey() + "," + entry.getValue().toString() + "\n");
			}

			WritingToFile.writeToNewFile(sb.toString(), absFileName);
			// bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Write a LinkedHashMap to file modified for append
	 * 
	 * @param map
	 * @param absFileNameToWrite
	 * @param headerKey
	 * @param headerValue
	 */
	public static void writeSimpleLinkedHashMapToFileAppend(LinkedHashMap<String, ?> map, String absFileNameToWrite,
			String headerKey, String headerValue)
	{
		// commonPath = Constant.getCommonPath();//

		if (Constant.areActivityNamesInCorrectOrder(map) == false)
		{
			System.err.println(
					"Debug Feb24: Error inside writeSimpleLinkedHashMapToFileAppend: is correct order of activity names = "
							+ Constant.areActivityNamesInCorrectOrder(map));
		}
		// Constant.areActivityNamesInCorrectOrder(map);

		StringBuilder toWrite = new StringBuilder();

		try
		{
			File fileToWrite = new File(absFileNameToWrite);
			// fileToWrite.delete();
			// fileToWrite.createNewFile();

			BufferedWriter bw = new BufferedWriter(new FileWriter(fileToWrite, true));// ,true));

			// bw.write(headerKey+","+headerValue);
			bw.newLine();

			for (Map.Entry<String, ?> entry : map.entrySet())
			{
				// bw.write(entry.getKey()+","+entry.getValue());
				// bw.append(entry.getValue().toString() + ",");
				toWrite.append(entry.getValue().toString() + ",");
			}

			bw.append(toWrite.toString());
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Writes the following for All, Train and Test timelines ActivityCountsInGivenDayTimelines,
	 * ActivityDurationInGivenDayTimelines, ActivityOccPercentageOfTimelines,
	 * NumOfDistinctValidActivitiesPerDayInGivenDayTimelines and returns two maps (each sorted by decreasing order of
	 * values), one for baseline count and other for baseline duration for training timelines.
	 * 
	 * @param userName
	 * @param userAllDatesTimeslines
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 * @return linkedhashmap contain two maps for training timelines, one for baseline count and other for baseline
	 *         duration. Here, map for baseline count contains {ActivityNames,count over all days in training
	 *         timelines}, similarly for count of baseline duration
	 */
	public static LinkedHashMap<String, LinkedHashMap<String, ?>> writeBasicActivityStatsAndGetBaselineMaps(
			String userName, LinkedHashMap<Date, Timeline> userAllDatesTimeslines,
			LinkedHashMap<Date, Timeline> userTrainingTimelines, LinkedHashMap<Date, Timeline> userTestTimelines)
	{
		String commonPath = Constant.getCommonPath();

		String timelinesSets[] = { "AllTimelines", "TrainingTimelines", "TestTimelines" };
		LinkedHashMap<Date, Timeline> timelinesCursor = null;

		// the thing to return, contains two hashmaps used for count and duration baselines
		LinkedHashMap<String, LinkedHashMap<String, ?>> resultsToReturn = new LinkedHashMap<String, LinkedHashMap<String, ?>>();

		// Needed for base line recommendations (based on training set only)
		LinkedHashMap<String, Long> activityNameCountPairsOverAllTrainingDays;
		// Needed for base line recommendations (based on training set only)
		LinkedHashMap<String, Long> activityNameDurationPairsOverAllTrainingDays;
		// not used currently
		LinkedHashMap<String, Double> activityNameOccPercentageOverAllTrainingDays;

		for (String timelinesSet : timelinesSets)
		{
			switch (timelinesSet)
			{
				case "AllTimelines":
					timelinesCursor = userAllDatesTimeslines;
					break;
				case "TrainingTimelines":
					timelinesCursor = userTrainingTimelines;
					break;
				case "TestTimelines":
					timelinesCursor = userTestTimelines;
					break;
				default:
					PopUps.showError(
							"Error in org.activity.tests.RecommendationTestsDaywiseJan2016: Unrecognised timelinesSet");
					break;
			}

			if (timelinesSet.equals("TrainingTimelines"))
			{
				activityNameCountPairsOverAllTrainingDays = WritingToFile
						.writeActivityCountsInGivenDayTimelines(userName, timelinesCursor, timelinesSet);
				activityNameCountPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) ComparatorUtils
						.sortByValueDesc(activityNameCountPairsOverAllTrainingDays);
				resultsToReturn.put("activityNameCountPairsOverAllTrainingDays",
						activityNameCountPairsOverAllTrainingDays);

				activityNameDurationPairsOverAllTrainingDays = WritingToFile
						.writeActivityDurationInGivenDayTimelines(userName, timelinesCursor, timelinesSet);
				activityNameDurationPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) ComparatorUtils
						.sortByValueDesc(activityNameDurationPairsOverAllTrainingDays);
				resultsToReturn.put("activityNameDurationPairsOverAllTrainingDays",
						activityNameDurationPairsOverAllTrainingDays);

				activityNameOccPercentageOverAllTrainingDays = WritingToFile
						.writeActivityOccPercentageOfTimelines(userName, timelinesCursor, timelinesSet);
			}

			else
			{
				LinkedHashMap<String, Long> actCountRes1 = WritingToFile
						.writeActivityCountsInGivenDayTimelines(userName, timelinesCursor, timelinesSet);
				LinkedHashMap<String, Long> actDurationRes1 = WritingToFile
						.writeActivityDurationInGivenDayTimelines(userName, timelinesCursor, timelinesSet);
				LinkedHashMap<String, Double> actOccPercentageRes1 = WritingToFile
						.writeActivityOccPercentageOfTimelines(userName, timelinesCursor, timelinesSet);

				writeSimpleLinkedHashMapToFileAppend(actCountRes1,
						commonPath + "ActivityCounts" + timelinesSet + ".csv", "dummy", "dummy");
				writeSimpleLinkedHashMapToFileAppend(actDurationRes1,
						commonPath + "ActivityDurations" + timelinesSet + ".csv", "dummy", "dummy");
				writeSimpleLinkedHashMapToFileAppend(actOccPercentageRes1,
						commonPath + "ActivityPerOccur" + timelinesSet + ".csv", "dummy", "dummy");
				// writeSimpleLinkedHashMapToFileAppend(LinkedHashMap<String, ?> map, String fileName, String headerKey,
				// String headerValue)
				// writeSimpleLinkedHashMapToFile(LinkedHashMap<String, ?> map, String absFileName, String headerKey,
				// String headerValue)
			}
			WritingToFile.writeNumOfDistinctValidActivitiesPerDayInGivenDayTimelines(userName, timelinesCursor,
					timelinesSet);
		}

		return resultsToReturn;
	}

	public static void closeBufferWriters(ArrayList<BufferedWriter> list)
	{
		list.stream().close();
	}

	public static void writeToFile(LinkedHashMap<Integer, Pair<Timestamp, Integer>> map, String fullPath)
	{
		try
		{
			BufferedWriter bw = WritingToFile.getBWForNewFile(fullPath);
			bw.append("User,TimestampOfRT,NumOfValidsAfterIt");
			bw.newLine();

			for (Map.Entry<Integer, Pair<Timestamp, Integer>> entry : map.entrySet())
			{
				// bw.write(entry.getKey()+","+entry.getValue());
				String s = entry.getKey() + "," + entry.getValue().getFirst() + "," + entry.getValue().getSecond();
				bw.append(s);// entry.getValue().toString() + ","\);
				bw.newLine();
			}

			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void deleteNonEmptyDirectory(Path absRootPath)
	{
		try
		{
			Files.walk(absRootPath, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.peek(f -> System.out.println("Deleting :" + f.toString())).forEach(File::delete);
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	// /**
	// * Headerless
	// *
	// * @param tmap
	// * @param fullPathToFileName
	// */
	// public static void writeTripleLinkedHashMap(LinkedHashMap<Integer, LinkedHashMap<String, LinkedHashMap<Double,
	// ArrayList<Double>>>> userLevel, String
	// fullPathToFileName)
	// {
	// commonPath = Constant.getCommonPath();//
	// try
	// {
	// String fileName = fullPathToFileName;
	//
	// File file = new File(fileName);
	//
	// file.delete();
	// if (!file.exists())
	// {
	// file.createNewFile();
	// }
	//
	// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
	// BufferedWriter bw = new BufferedWriter(fw);
	//
	// for (Entry<Integer, LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>>> entryUserLevel :
	// userLevel.entrySet())
	// {
	// // String userser =" + entryUserLevel.getKey());
	// LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>> rtLevel = entryUserLevel.getValue();
	// for (Entry<String, LinkedHashMap<Double, ArrayList<Double>>> entryRTLevel : rtLevel.entrySet())
	// {
	// // System.out.println("RT =" + entryRTLevel.getKey());
	// LinkedHashMap<Double, ArrayList<Double>> muLevel = entryRTLevel.getValue();
	// for (Entry<Double, ArrayList<Double>> entryMULevel : muLevel.entrySet())
	// {
	// // System.out.println("MU =" + entryMULevel.getKey());
	// // System.out.println("Edit distance of cands =" + entryMULevel.getValue());
	//
	// }
	// }
	//
	// }
	//
	// for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapOfMap.entrySet())
	// {
	//
	// String userName = entryForUser.getKey();
	// TreeMap<Timestamp, String> mapForEachUser = entryForUser.getValue();
	//
	// for (Map.Entry<Timestamp, String> entryInside : mapForEachUser.entrySet())
	// {
	// bw.write(userName + "," + entryInside.getKey() + "," + entryInside.getValue());
	// bw.newLine();
	// }
	// }
	// bw.close();
	// }
	//
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Creates a directory if it does not already exist
	 * 
	 * @param pathname
	 * @return
	 */
	public static boolean createDirectory(String pathname)
	{
		boolean result = false;

		File directory = new File(pathname);

		// if the directory does not exist, create it
		if (!directory.exists())
		{
			System.out.println("creating directory: " + directory);
			try
			{
				result = directory.mkdir();
			}
			catch (SecurityException se)
			{
				System.err.println("Error: cannot create  directory " + directory);
				se.printStackTrace();
			}
			if (result)
			{
				System.out.println(pathname + " directory created");
			}
		}
		else
		{
			System.out.println("Cannot create  directory " + directory + " as it already exists");
		}
		return result;
	}

	public static boolean isDirectoryEmpty(String path)
	{
		boolean isEmpty = false;

		File file = new File(path);

		if (file.isDirectory())
		{
			if (file.list().length == 0)
			{
				isEmpty = true;
			}
		}
		else
		{
			System.err.println("Error in isDirectoryEmpty: " + path + " is not a directory");
		}

		return isEmpty;
	}

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param verbose
	 * @param absFileNameToWrite
	 * @return
	 */
	public static Pair<Long, Long> writeNumberOfActsWithMultipleWorkingLevelCatID(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean verbose,
			String absFileNameToWrite)
	{
		long numOfActWithMultipleWorkingLevelCatID = 0, numOfAOs = 0;
		HashSet<String> multipleWorkingLevelCatIds = new HashSet<>();

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
		{
			for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
			{
				for (ActivityObject ao : dateEntry.getValue().getActivityObjectsInTimeline())
				{
					numOfAOs += 1;
					if (RegexUtils.patternDoubleUnderScore.split(ao.getWorkingLevelCatIDs()).length > 1)
					{
						multipleWorkingLevelCatIds.add(ao.getWorkingLevelCatIDs());
						numOfActWithMultipleWorkingLevelCatID += 1;
					}
				}
			}
		}

		if (verbose)
		{
			System.out.println("num of AOs = " + numOfAOs);
			System.out.println("numOfActWithMultipleWorkingLevelCatID = " + numOfActWithMultipleWorkingLevelCatID);
			System.out.println("% ActWithMultipleWorkingLevelCatID = "
					+ ((numOfActWithMultipleWorkingLevelCatID / numOfAOs) * 100));
		}

		writeToNewFile(multipleWorkingLevelCatIds.stream().map(s -> s.toString()).collect(Collectors.joining("\n")),
				absFileNameToWrite);

		return new Pair<Long, Long>(numOfActWithMultipleWorkingLevelCatID, numOfAOs);
	}

	/**
	 * Make each row of equal length (i.e., same num of columns by filling in with "NA"s. This is to facilitate reading
	 * data in R.
	 * 
	 * @param map
	 * @param catIDNameDictionary
	 * @param absfileNameToUse
	 * @param insertNAs
	 * @param writeCatName
	 */
	public static void writeConsectiveCountsEqualLength(LinkedHashMap<String, ArrayList<Integer>> map,
			TreeMap<Integer, String> catIDNameDictionary, String absfileNameToUse, boolean insertNAs,
			boolean writeCatName)
	{
		int maxNumOfVals = -1; // max consec counts over all cat ids

		// find the max size
		maxNumOfVals = (map.entrySet().stream().mapToInt(e -> e.getValue().size()).max()).getAsInt();
		// maxNumOfVals += 1; // 1 additional column for header
		// for (Entry<String, ArrayList<Integer>> e : map.entrySet())
		// {
		// // if(e.getValue().size()>maxSizeOfArrayList)
		// }

		int numOfCatIDsInMap = 0;
		try
		{
			BufferedWriter bw = getBWForNewFile(absfileNameToUse);
			for (Entry<String, ArrayList<Integer>> entry : map.entrySet())
			{
				String catID = entry.getKey();
				ArrayList<Integer> consecCounts = entry.getValue();

				if (consecCounts.size() != 0)
				{
					int numOfNAsToBeInserted = maxNumOfVals - consecCounts.size();
					StringBuilder sb = new StringBuilder();

					if (writeCatName)
					{
						sb.append(catID + ":" + catIDNameDictionary.get(Integer.valueOf(catID)));
					}
					else
					{
						sb.append(catID + ":");
					}
					numOfCatIDsInMap += 1;

					for (Integer t : consecCounts)
					{
						sb.append("," + t);
					}

					if (insertNAs)
					{
						for (int i = 0; i < numOfNAsToBeInserted; i++)
						{
							sb.append(",NA");
						}
					}

					bw.write(sb.toString() + "\n");
				}
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Num of catIDs in dictionary = " + catIDNameDictionary.size());
		System.out.println("Num of catIDs in working dataset = " + numOfCatIDsInMap);
		System.out.println("maxNumOfVals = " + maxNumOfVals);
	}
	// LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise = new LinkedHashMap<>();
	//
	// // convert checkinentries to activity objects
	// for (Entry<String, TreeMap<Date, ArrayList<CheckinEntry>>> userEntry : checkinEntriesDatewise.entrySet()) // over
	// users
	// {
	// String userID = userEntry.getKey();
	// TreeMap<Date, ArrayList<ActivityObject>> dayWiseForThisUser = new TreeMap<>();
	//
	// for (Entry<Date, ArrayList<CheckinEntry>> dateEntry : userEntry.getValue().entrySet()) // over dates
	// {
	// Date date = dateEntry.getKey();
	// ArrayList<ActivityObject> activityObjectsForThisUserThisDate = new ArrayList<>();
	//
	// for (CheckinEntry e : dateEntry.getValue())// over checkins
	// {
	// int activityID = e.getActivityID();
	// int locationID = e.getLocationID();
	//
	// String activityName = "";//
	// String locationName = "";//
	// Timestamp startTimestamp = e.getTimestamp();
	// String startLatitude = e.getStartLatitude();
	// String startLongitude = e.getStartLongitude();
	// String startAltitude = "";//
	// String userIDInside = e.getUserID();
	//
	// // sanity check start
	// if (userIDInside.equals(userID) == false)
	// {
	// System.err.println("Sanity check failed in createUserTimelinesFromCheckinEntriesGowalla()");
	// }
	// // sanity check end
	//
	// LocationGowalla loc = locationObjects.get(String.valueOf(locationID));
	// int photos_count = loc.getPhotos_count();
	// int checkins_count = loc.getCheckins_count();
	// int users_count = loc.getUsers_count();
	// int radius_meters = loc.getRadius_meters();
	// int highlights_count = loc.getHighlights_count();
	// int items_count = loc.getItems_count();
	// int max_items_count = loc.getMax_items_count();
	//
	// ActivityObject ao = new ActivityObject(activityID, locationID, activityName, locationName, startTimestamp,
	// startLatitude, startLongitude, startAltitude, userID, photos_count, checkins_count, users_count, radius_meters,
	// highlights_count, items_count, max_items_count);
	//
	// activityObjectsForThisUserThisDate.add(ao);
	// }
	// dayWiseForThisUser.put(date, activityObjectsForThisUserThisDate);
	// }
	// activityObjectsDatewise.put(userID, dayWiseForThisUser);
	// }
	//
	// System.out.println("inside createUserTimelinesFromCheckinEntriesGowalla");
	//
	// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines =
	// new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
	//
	// // userid, usertimeline
	// LinkedHashMap<String, ArrayList<ActivityObject>> perUserActivityEvents = new LinkedHashMap<String,
	// ArrayList<ActivityObject>>();
	//
	// for (Map.Entry<String, TreeMap<Timestamp, CheckinEntry>> perUserCheckinEntry : checkinEntries.entrySet()) //
	// Iterate over Users
	// {
	//
	// String userID = perUserCheckinEntry.getKey();
	// TreeMap<Timestamp, CheckinEntry> checkinEntriesForThisUser = perUserCheckinEntry.getValue();
	//
	// System.out.println("user:" + userID + " #CheckinEntries =" + checkinEntriesForThisUser.size());
	//
	// LinkedHashMap<Date, ArrayList<ActivityObject>> perDateActivityObjectsForThisUser =
	// new LinkedHashMap<Date, ArrayList<ActivityObject>>();
	//
	// for (Map.Entry<Timestamp, CheckinEntry> checkin : checkinEntriesForThisUser.entrySet()) // iterare over activity
	// events for this user
	// {
	// Timestamp ts = checkin.getKey();
	// CheckinEntry checkinEntry = checkin.getValue();
	// Date date = DateTimeUtils.getDate(ts);// (Date)
	// activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension", "Date"); // start date
	//
	// if (!(perDateActivityObjectsForThisUser.containsKey(date)))
	// {
	// perDateActivityObjectsForThisUser.put(date, new ArrayList<ActivityObject>());
	// }
	//
	// perDateActivityObjectsForThisUser.get(date).add(activityEventsForThisUser.get(i));
	// }
	//
	// perDateActivityEventsForThisUser has been created now.

	/**
	 * 
	 * @param map
	 * @param catIDNameDictionary
	 * @param absfileNameToUse
	 */
	public static void writeConsectiveCounts(LinkedHashMap<String, ArrayList<Integer>> map,
			TreeMap<Integer, String> catIDNameDictionary, String absfileNameToUse)
	{
		int numOfCatIDsInMap = 0;
		try
		{
			BufferedWriter bw = getBWForNewFile(absfileNameToUse);
			for (Entry<String, ArrayList<Integer>> entry : map.entrySet())
			{
				String catID = entry.getKey();
				ArrayList<Integer> consecCounts = entry.getValue();

				if (consecCounts.size() != 0)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(catID + ":" + catIDNameDictionary.get(Integer.valueOf(catID)));

					numOfCatIDsInMap += 1;

					for (Integer t : consecCounts)
					{
						sb.append("," + t);
					}

					bw.write(sb.toString() + "\n");
				}
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Num of catIDs in dictionary = " + catIDNameDictionary.size());
		System.out.println("Num of catIDs in working dataset = " + numOfCatIDsInMap);
	}

	/**
	 * Closes multiple BufferedReaders
	 * 
	 * @param br1
	 * @param brs
	 * @throws IOException
	 */
	public static void closeBufferedWriters(BufferedWriter br1, BufferedWriter... brs) throws IOException
	{
		br1.close();
		for (BufferedWriter br : brs)
		{
			br.close();
		}
	}

	/**
	 * 
	 * @param map
	 * @param absFileNameToUse
	 * @param delimiterForKeys
	 * @param delimiterForValues
	 */
	public static <K2, K3, V, K1> void writeMapOfMapOfMapOfList(Map<K1, Map<K2, Map<K3, List<V>>>> map,
			String absFileNameToUse, String delimiterForKeys, String delimiterForValues)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Key1,Key2,Key3,Vals\n");

		for (Entry<K1, Map<K2, Map<K3, List<V>>>> firstLevelEntry : map.entrySet())
		{
			sb.append(firstLevelEntry.getKey().toString() + delimiterForKeys);

			for (Entry<K2, Map<K3, List<V>>> secondLevelEntry : firstLevelEntry.getValue().entrySet())
			{
				sb.append(secondLevelEntry.getKey().toString() + delimiterForKeys);
				for (Entry<K3, List<V>> thirdLevelEntry : secondLevelEntry.getValue().entrySet())
				{
					sb.append(thirdLevelEntry.getKey().toString() + delimiterForKeys);

					String s = thirdLevelEntry.getValue().stream().map(v -> v.toString())
							.collect(Collectors.joining(delimiterForValues));
					sb.append(s).append("\n");
				}
			}
		}
		WritingToFile.writeToNewFile(sb.toString(), absFileNameToUse);
	}

	/**
	 * 
	 * @param map
	 * @param absFileNameToUse
	 * @param delimiterForKeys
	 * @param delimiterForValues
	 */
	public static void writeMapOfMapOfMapOfList(
			LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<Character>>>> map,
			String absFileNameToUse, String delimiterForKeys, String delimiterForValues)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Key1,Key2,Key3,Vals\n");

		for (Entry<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<Character>>>> firstLevelEntry : map
				.entrySet())
		{
			// sb.append(firstLevelEntry.getKey().toString() + delimiterForKeys);

			for (Entry<Integer, HashMap<String, ArrayList<Character>>> secondLevelEntry : firstLevelEntry.getValue()
					.entrySet())
			{

				for (Entry<String, ArrayList<Character>> thirdLevelEntry : secondLevelEntry.getValue().entrySet())
				{
					sb.append(firstLevelEntry.getKey().toString() + delimiterForKeys)
							.append(secondLevelEntry.getKey().toString() + delimiterForKeys)
							.append(thirdLevelEntry.getKey().toString() + delimiterForKeys);

					String s = thirdLevelEntry.getValue().stream().map(v -> v.toString())
							.collect(Collectors.joining(delimiterForValues));
					sb.append(s).append("\n");
				}
			}
		}
		WritingToFile.writeToNewFile(sb.toString(), absFileNameToUse);

	}

	/**
	 * 
	 * @param map
	 * @param absFileNameToUse
	 * @param delimiterForKeys
	 * @param delimiterForValues
	 */
	public static void writeMapOfMapOfMapOfListInt(
			LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<Integer>>>> map,
			String absFileNameToUse, String delimiterForKeys, String delimiterForValues)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Key1,Key2,Key3,Vals\n");

		for (Entry<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<Integer>>>> firstLevelEntry : map
				.entrySet())
		{
			// sb.append(firstLevelEntry.getKey().toString() + delimiterForKeys);

			for (Entry<Integer, HashMap<String, ArrayList<Integer>>> secondLevelEntry : firstLevelEntry.getValue()
					.entrySet())
			{
				// sb.append(secondLevelEntry.getKey().toString() + delimiterForKeys);
				for (Entry<String, ArrayList<Integer>> thirdLevelEntry : secondLevelEntry.getValue().entrySet())
				{
					sb.append(firstLevelEntry.getKey().toString() + delimiterForKeys)
							.append(secondLevelEntry.getKey().toString() + delimiterForKeys)
							.append(thirdLevelEntry.getKey().toString() + delimiterForKeys);

					String s = thirdLevelEntry.getValue().stream().map(v -> v.toString())
							.collect(Collectors.joining(delimiterForValues));

					sb.append(s).append("\n");
				}
			}
		}
		WritingToFile.writeToNewFile(sb.toString(), absFileNameToUse);

	}

	/**
	 * 
	 * @param map
	 * @param absFileNameToUse
	 * @param delimiterForKeys
	 * @param delimiterForValues
	 */
	public static void writeMapOfMapOfMapOfListString(
			LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<String>>>> map,
			String absFileNameToUse, String delimiterForKeys, String delimiterForValues)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Key1,Key2,Key3,Vals\n");

		for (Entry<Integer, LinkedHashMap<Integer, HashMap<String, ArrayList<String>>>> firstLevelEntry : map
				.entrySet())
		{
			// sb.append(firstLevelEntry.getKey().toString() + delimiterForKeys);

			for (Entry<Integer, HashMap<String, ArrayList<String>>> secondLevelEntry : firstLevelEntry.getValue()
					.entrySet())
			{
				// sb.append(secondLevelEntry.getKey().toString() + delimiterForKeys);
				for (Entry<String, ArrayList<String>> thirdLevelEntry : secondLevelEntry.getValue().entrySet())
				{
					sb.append(firstLevelEntry.getKey().toString() + delimiterForKeys)
							.append(secondLevelEntry.getKey().toString() + delimiterForKeys)
							.append(thirdLevelEntry.getKey().toString() + delimiterForKeys);

					String s = thirdLevelEntry.getValue().stream().map(v -> v.toString())
							.collect(Collectors.joining(delimiterForValues));

					sb.append(s).append("\n");
				}
			}
		}
		WritingToFile.writeToNewFile(sb.toString(), absFileNameToUse);

	}

	// public static void writeSimpleLinkedHashMapToFileAppendDouble(LinkedHashMap<String, Double> map, String fileName,
	// String headerKey,
	// String headerValue)
	// {
	// commonPath = Constant.getCommonPath();//
	// try
	// {
	// File fileToWrite = new File(fileName);
	// // fileToWrite.delete();
	// // fileToWrite.createNewFile();
	//
	// BufferedWriter bw = new BufferedWriter(new FileWriter(fileToWrite, true));// ,true));
	//
	// // bw.write(headerKey+","+headerValue);
	// bw.newLine();
	//
	// for (Map.Entry<String, Double> entry : map.entrySet())
	// {
	// // bw.write(entry.getKey()+","+entry.getValue());
	// bw.append(entry.getValue() + ",");
	//
	// }
	//
	// bw.close();
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }
	// public static void writeMessage(String fileName, String message)
	// {
	// try
	// {
	// File fileToWrite= new File(fileName);
	// fileToWrite.delete();
	// fileToWrite.createNewFile();
	//
	// BufferedWriter bw= new BufferedWriter(new FileWriter(fileToWrite));//,true));
	//
	// bw.write(headerKey+","+headerValue);
	// bw.newLine();
	//
	// for (Map.Entry<String, Integer > entry: map.entrySet())
	// {
	// bw.write(entry.getKey()+","+entry.getValue());
	// bw.newLine();
	// }
	//
	// bw.close();
	// }
	// catch(Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

}
