package org.activity.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.activity.io.WritingToFile;
import org.activity.util.ConnectDatabase;
import org.activity.util.Constant;
import org.activity.util.UtilityBelt;

public class CandDistCorrelation
{
	static int[] userIDs;
	static double[] matchingUnits;
	static LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<Double>>> allCorrelationsMUPairwise;

	static String commonPath1 = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/EDCorr/June5/GeolifePearson/";// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/EDCorr/June4Geolife/";//
	// /run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May14/DCU/";
	static String pathToRead1 = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June5/Geolife/SimpleV3/";// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May19latestGeo/Geolife/Sum0.5/";//
																											// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May7_2015/DCU/Sum0.5/";

	final static String correlationType = "pearson";// "kendalltau";

	// public static void main(String args[])
	// {
	// LinkedHashMap<String, ArrayList<Double>> all = new LinkedHashMap<String, ArrayList<Double>>();
	//
	// for (int i = 0; i < 2; i++)
	// {
	// ArrayList<Double> arr = new ArrayList<Double>();
	//
	// for (int j = 0; j < 3; j++)
	// {
	// all.put(i + "__" + j, Integer.valueOf(i + j));
	// }
	// }
	//
	// }
	public static void main(String args[])
	{
		try
		{
			Constant.setDatabaseName("geolife1");// "dcu_data_2";
			ConnectDatabase.initialise(Constant.getDatabaseName());
			Constant.initialise(commonPath1, Constant.getDatabaseName());

			// Constant.setCommonPath(commonPath1);
			File consoleLog = new File(
					/* "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data Works/stats/ConsoleLogs/" */commonPath1
							+ "EditDistancesOfCandsPerUserPerRtPerMU.txt");
			consoleLog.delete();
			consoleLog.createNewFile();
			PrintStream consoleLogStream = new PrintStream(consoleLog);
			System.setOut(new PrintStream(consoleLogStream));
			System.setErr(consoleLogStream);

			LinkedHashMap<String, ArrayList<Double>> all = getEditDistancePerUserPerRtPerMU();
			// TreeMap<String, ArrayList<Double>> allSorted = new TreeMap<String, ArrayList<Double>>();
			// allSorted.putAll(all);

			// traverseMap(all);
			// User,Rt,MU,Ed
			LinkedHashMap<Integer, LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>>> allFormatted = getFormattedMap(
					all);
			allCorrelationsMUPairwise = traverseTripleMapGetCorrelations(allFormatted);
			traverseDoubleMapWriteCorrelations(allCorrelationsMUPairwise);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<Double>>> traverseTripleMapGetCorrelations(
			LinkedHashMap<Integer, LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>>> userLevel)
	{
		allCorrelationsMUPairwise = new LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<Double>>>();

		for (Entry<Integer, LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>>> entryUserLevel : userLevel
				.entrySet())
		{
			System.out.println("User =" + entryUserLevel.getKey());
			LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>> rtLevel = entryUserLevel.getValue();

			allCorrelationsMUPairwise.put(entryUserLevel.getKey(), new LinkedHashMap<String, ArrayList<Double>>());

			for (Entry<String, LinkedHashMap<Double, ArrayList<Double>>> entryRTLevel : rtLevel.entrySet())
			{
				System.out.println("RT =" + entryRTLevel.getKey());
				LinkedHashMap<Double, ArrayList<Double>> muLevel = entryRTLevel.getValue();

				allCorrelationsMUPairwise.get(entryUserLevel.getKey()).put(entryRTLevel.getKey(),
						new ArrayList<Double>());

				ArrayList<Double> valsFromPreviousMU = new ArrayList<Double>();

				for (Entry<Double, ArrayList<Double>> entryMULevel : muLevel.entrySet())
				{
					System.out.println("MU =" + entryMULevel.getKey());
					System.out.println("Edit distance of cands =" + entryMULevel.getValue());

					// String path = "/home/gunjan/Geolife/";

					String fileName = entryUserLevel.getKey() + "__" + entryRTLevel.getKey() + "__"
							+ entryMULevel.getKey() + ".csv";
					fileName = fileName.replace('/', '-');
					WritingToFile.writeArrayListAbsolute(entryMULevel.getValue(), commonPath1 + fileName, "");

					double correlationForPrevAndCurrentMUValues = -9999;

					if (correlationType.equals("pearson"))
					{
						correlationForPrevAndCurrentMUValues = UtilityBelt.getPearsonCorrelation(valsFromPreviousMU,
								entryMULevel.getValue());
					}
					else if (correlationType.equals("kendalltau"))
					{
						correlationForPrevAndCurrentMUValues = UtilityBelt.getKendallTauCorrelation(valsFromPreviousMU,
								entryMULevel.getValue());
					}

					valsFromPreviousMU = entryMULevel.getValue();

					allCorrelationsMUPairwise.get(entryUserLevel.getKey()).get(entryRTLevel.getKey())
							.add(new Double(correlationForPrevAndCurrentMUValues));
				}
			}
		}
		return allCorrelationsMUPairwise;
	}

	public static void traverseDoubleMapWriteCorrelations(
			LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<Double>>> allCorrelationsMUPairwise)
	{
		WritingToFile.appendLineToFile("User,RecommDate,RecommTime," + correlationType + "Correlations",
				correlationType + "CorrelationsMUs");
		for (Entry<Integer, LinkedHashMap<String, ArrayList<Double>>> entryUserLevel : allCorrelationsMUPairwise
				.entrySet())
		{
			// System.out.print("%User =" + entryUserLevel.getKey());
			LinkedHashMap<String, ArrayList<Double>> rtLevel = entryUserLevel.getValue();

			for (Entry<String, ArrayList<Double>> entryRTLevel : rtLevel.entrySet())
			{
				// StringBuffer lineToWrite = new StringBuffer("\n%User =" + entryUserLevel.getKey() + ",RT =" +
				// entryRTLevel.getKey() + ",");
				StringBuffer lineToWrite = new StringBuffer(
						"\n" + entryUserLevel.getKey() + "," + entryRTLevel.getKey() + ",");

				// System.out.print("\n User =" + entryUserLevel.getKey() + ",RT =" + entryRTLevel.getKey() + ",");
				ArrayList<Double> muLevel = entryRTLevel.getValue();

				// lineToWrite.append("Correlation Values are:%,");
				for (Double val : muLevel)
				{
					lineToWrite.append(val + ",");
				}
				lineToWrite.append("nothing");
				Constant.setCommonPath(commonPath1);// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link
													// to Geolife Data Works/stats/ConsoleLogs/");
				WritingToFile.appendLineToFile(lineToWrite.toString(), correlationType + "CorrelationsMUs");
			}
		}
	}

	// /**
	// *
	// * @param muLevel
	// * @return
	// */
	// public static ArrayList<Double> correlationPairWiseColumns(Integer user, String RT, LinkedHashMap<Double,
	// ArrayList<Double>> muLevel)
	// {
	//
	// for (Entry<Double, ArrayList<Double>> entryMULevel : muLevel.entrySet())
	// {
	// System.out.println("MU =" + entryMULevel.getKey());
	// System.out.println("Edit distance of cands =" + entryMULevel.getValue());
	//
	// String path = "/home/gunjan/Geolife/";
	//
	// // String fileName = entryUserLevel.getKey() + "__" + entryRTLevel.getKey() + "__" + entryMULevel.getKey() +
	// ".csv";
	// // fileName = fileName.replace('/', '-');
	// //
	// // WritingToFile.writeArrayListAbsolute(entryMULevel.getValue(), path + fileName, "");
	// }
	//
	// }

	/**
	 * 
	 * @param allO
	 * @return
	 */
	public static LinkedHashMap<Integer, LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>>> getFormattedMap(
			LinkedHashMap<String, ArrayList<Double>> allO)
	{
		// <User, <RT, MU, <Edit Distance>
		LinkedHashMap<Integer, LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>>> allNew = new LinkedHashMap<Integer, LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>>>();

		for (Entry<String, ArrayList<Double>> entry : allO.entrySet())
		{
			// System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue().size());
			String key = entry.getKey();
			ArrayList<Double> arrayValue = entry.getValue();

			String[] splittedKey = entry.getKey().split("__");
			Integer user = new Integer(splittedKey[0]);
			String RT = splittedKey[1];
			Double mu = Double.valueOf(splittedKey[2]);

			if (allNew.containsKey(user))
			{
				LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>> rtLevel = allNew.get(user);
				if (rtLevel.containsKey(RT))
				{
					LinkedHashMap<Double, ArrayList<Double>> muLevel = rtLevel.get(RT);

					if (muLevel.containsKey(mu))
					{
						System.err.println("This should not happen: multiple entry for same --user rt  mu--");
					}
					else
					{
						muLevel.put(mu, arrayValue);
					}
				}
				else
				{
					LinkedHashMap<Double, ArrayList<Double>> newMULevel = new LinkedHashMap<Double, ArrayList<Double>>();
					newMULevel.put(new Double(mu), arrayValue);

					rtLevel.put(RT, newMULevel);
				}

			}
			else
			{

				LinkedHashMap<Double, ArrayList<Double>> newMULevel = new LinkedHashMap<Double, ArrayList<Double>>();
				newMULevel.put(new Double(mu), arrayValue);

				LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>> newRTLevel = new LinkedHashMap<String, LinkedHashMap<Double, ArrayList<Double>>>();
				newRTLevel.put(RT, newMULevel);

				allNew.put(user, newRTLevel);
			}
		}
		return allNew;
	}

	/**
	 * Get list of edit distance for each RT for each user for each MU
	 * 
	 * @return <userID + "__" + previousRTString + "__" + matchingUnit, List of <Edit Distances>>
	 */
	public static LinkedHashMap<String, ArrayList<Double>> getEditDistancePerUserPerRtPerMU()
	{
		userIDs = Constant.getUserIDs();// .userIDsDCUData;
		matchingUnits = Constant.matchingUnitAsPastCount;
		// LinkedHashMap<User,LinkedHashMap<MU,LinkedHashMap<RT,ArrayList<EDs of Cands>>>>

		// TreeMap<Integer, TreeMap<Double, TreeMap<String, ArrayList<Double>>>> all = new LinkedHashMap<Integer,
		// LinkedHashMap<Double, LinkedHashMap<String,
		// ArrayList<Double>>>>();

		// LinkedHashMap<User,LinkedHashMap<RT,LinkedHashMap<MU,ArrayList<EDs of Cands>>>>
		// TreeMap<Integer, TreeMap<String, TreeMap<Double, ArrayList<Double>>>> all = new TreeMap<Integer,
		// TreeMap<String, TreeMap<Double,
		// ArrayList<Double>>>>();
		// TreeMap<UserID__RT__MU, ArrayList<Double>>
		LinkedHashMap<String, ArrayList<Double>> all = new LinkedHashMap<String, ArrayList<Double>>();
		// TreeMap<Integer,Integer> userNumberOfRts
		for (int uIterator = 0; uIterator < userIDs.length; uIterator++)
		{
			int userID = userIDs[uIterator];

			String userIDString;

			if (Constant.getDatabaseName().equals("dcu_data_2"))
			{
				userIDString = Constant.userNamesDCUData[uIterator];
			}
			else
				userIDString = Integer.toString(userID);

			if (Constant.getDatabaseName().equals("geolife1"))
			{
				if (userIDString.length() < 3) // ALERT: THIS IS N0T A ROBUST WAY TO ADD LEADING ZEROS..CHANGE LATER
					userIDString = "0" + userIDString;
			}
			else
			{
				userIDString = ConnectDatabase.getUserName(userID);
			}
			double matchingUnit;
			for (int j = 0; j < matchingUnits.length; j++)
			{
				matchingUnit = matchingUnits[j];

				// read the file EditDistancePerRtPerCand.csv
				BufferedReader br = null;
				String pathToRead = /* "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/May1_2015/Geolife/Sum0.5/ */pathToRead1
						+ "MatchingUnit" + matchingUnit + "/" + userIDString + "RecommTimesWithEditDistance.csv";// "EditDistancePerRtPerCand.csv";
				// System.out.println(pathToRead);
				try
				{
					int lcount = 0, numOfRTs = 0;
					String previousRTString = "";

					ArrayList<Double> editDistancesForSingleRT = new ArrayList<Double>();

					String sCurrentLine;
					br = new BufferedReader(new FileReader(pathToRead));

					while ((sCurrentLine = br.readLine()) != null)
					{
						if (lcount == 0) // skip header
						{
							lcount++;
							continue;
						}

						String[] splittedString = sCurrentLine.split(",");
						// System.out.println(sCurrentLine + " " + splittedString[3]);
						String RTdateTimeRead = splittedString[0] + "||" + splittedString[1];
						double editDistance = UtilityBelt.round(Double.valueOf(splittedString[4]), 4);

						// System.out.println("Reading:" + userIDString + " " + matchingUnit + " " + RTdateTimeRead + "
						// " + editDistance);

						if (lcount > 1 && previousRTString.equals(RTdateTimeRead) == false)// new RT lcount==1 is the
																							// first non-header entry in
																							// file being read
						{
							// System.out.println(" New RT =" + (previousRTString.equals(RTdateTimeRead) == false));
							// System.out.println("new rt+ adding: " + userID + "__" + RTdateTimeRead + "__" +
							// matchingUnit + " num of cands:" +
							// editDistanceForSingleRT.size() + " --" +
							// editDistanceForSingleRT);
							//
							String key = new String(userID + "__" + previousRTString + "__" + matchingUnit);

							all.put(key, editDistancesForSingleRT);

							// System.out.println("new rt+ adding: " + key + " num of cands:" +
							// editDistanceForSingleRT.size() + " --" +
							// editDistanceForSingleRT);
							// traverseMap(all);

							editDistancesForSingleRT = new ArrayList<Double>();// .clear();//AAAAAAAAAAAAA! JAVA IS
																				// PASSED BY REFERENCE...CREATE NEW
																				// OBJECT EACH
																				// TIMEN
							editDistancesForSingleRT.add(editDistance);
							previousRTString = RTdateTimeRead;
							numOfRTs++;
						}
						else
						{
							// System.out.print("\telse ; increase array ");
							editDistancesForSingleRT.add(editDistance);
							// System.out.println(editDistanceForSingleRT.size());
							previousRTString = RTdateTimeRead; // current and previous are same, so it does not
																// matter...except for the first data entry
						}

						lcount++;
					}

					{
						// System.out.println("\tif: new add: last RT");
						// System.out.println(" New RT =" + (previousRTString.equals(RTdateTimeRead) == false));
						all.put(userID + "__" + previousRTString + "__" + matchingUnit, editDistancesForSingleRT);
						numOfRTs++;
					}
					System.out.println(
							"Number of RTs for user" + userID + " matching unit" + matchingUnit + " =" + numOfRTs);

				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						if (br != null) br.close();
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
				}

			}

		} // end of iteration over users

		System.out.println("all size=" + all.size());
		// traverseMap(all);
		return all;
		//
	}

	public static void traverseMap(LinkedHashMap<String, ArrayList<Double>> all)
	{
		System.out.println("----------------------Traversing map-----------------------");
		for (Entry<String, ArrayList<Double>> entry : all.entrySet())
		{
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue().size());
		}
		System.out.println("----------------------End of Traversing map-----------------------");
	}

	public static void traverseMap(TreeMap<String, ArrayList<Double>> all)
	{
		System.out.println("----------------------Traversing map-----------------------");
		for (Entry<String, ArrayList<Double>> entry : all.entrySet())
		{
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue().size());
		}
		System.out.println("----------------------End of Traversing map-----------------------");
	}
}
/*
 * try { String sCurrentLine; br = new BufferedReader(new FileReader(pathToRead));
 * 
 * int lcount = 0;
 * 
 * String currentRTString = "ajooba";
 * 
 * ArrayList<Double> editDistanceForSingleRT = new ArrayList<Double>(); int numOfRTs = 0; while ((sCurrentLine =
 * br.readLine()) != null) { if (lcount == 0) { lcount++; continue; } String[] splittedString = sCurrentLine.split(",");
 * 
 * String RTdateTimeRead = splittedString[0] + "||" + splittedString[1]; double editDistance =
 * Double.valueOf(splittedString[3]);
 * 
 * // System.out.println(userIDString + " " + matchingUnit + " " + RTdateTimeRead + " " + editDistance);
 * 
 * if (lcount != 1 && currentRTString.equals(RTdateTimeRead) == false)// new RT lcount==1 is the first non-header entry
 * in file being read { System.out.println("new rt+ adding: " + userID + "__" + RTdateTimeRead + "__" + matchingUnit +
 * " num of cands:" + editDistanceForSingleRT.size() + " --" + editDistanceForSingleRT);
 * 
 * all.put(userID + "__" + RTdateTimeRead + "__" + matchingUnit, editDistanceForSingleRT);
 * editDistanceForSingleRT.clear();// = new ArrayList<Double>(); editDistanceForSingleRT.add(editDistance); numOfRTs++;
 * } else { editDistanceForSingleRT.add(editDistance); } currentRTString = RTdateTimeRead; lcount++; }
 * 
 * 
 * 
 * 
 * System.out.println("Number of RTs for user" + userID + " matching unit" + matchingUnit + " =" + numOfRTs);
 * 
 * }
 */