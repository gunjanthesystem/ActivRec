package org.giscience.utils.geogrid.gunjanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.constants.PathConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.FastFlippableIntPair;
import org.activity.objects.LeakyBucket;
import org.activity.objects.Pair;
import org.activity.objects.PairedIndicesTo1DArrayConverter;
import org.activity.objects.PairedIndicesTo1DArrayStorage;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
import org.activity.spatial.SpatialUtils;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.PerformanceAnalytics;
import org.giscience.utils.geogrid.cells.GridCell;
import org.giscience.utils.geogrid.grids.ISEA3H;

/**
 * 
 * @author gunjan
 *
 */
public class GLatLonToGridTransformer
{
	static int gridResolution = 16;
	ISEA3H theGrid;

	public static void fun1()
	{
		GridCell c;

	}

	/**
	 * NOT USER
	 * 
	 * @param pathToSerialisedGridIDCellMapToUse
	 */
	public static void getDistanceBetweenGrids(String pathToSerialisedGridIDCellMapToUse)
	{
		try
		{
			Map<Long, GridCell> gridIDGridCellMap = (Map<Long, GridCell>) Serializer
					.kryoDeSerializeThis(pathToSerialisedGridIDCellMapToUse);

			int numOfUniqueGrids = gridIDGridCellMap.size();

			if (false)
			{// creating 2d array for fetch efficiency but high space cost
				Map<Integer, Long> gridIndexIDMap = new TreeMap<>();
				Map<Long, Integer> gridIDIndexMap = new TreeMap<>();
				int gridIDIndex = 0;
				for (Entry<Long, GridCell> e : gridIDGridCellMap.entrySet())
				{
					gridIndexIDMap.put(gridIDIndex, e.getKey());
					gridIDIndexMap.put(e.getKey(), gridIDIndex);
					gridIDIndex += 1;
				}

				double[][] distancesBetweenGrids = new double[numOfUniqueGrids][numOfUniqueGrids];
			}

			for (Entry<Long, GridCell> eOuter : gridIDGridCellMap.entrySet())
			{
				Long gridID1 = eOuter.getKey();

				for (Entry<Long, GridCell> eInnner : gridIDGridCellMap.entrySet())
				{
					Long gridID2 = eInnner.getKey();
					if (gridID1.equals(gridID2) == false)
					{
						eOuter.getValue().getLat();
					}
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * NOT USED
	 * 
	 * @param pathToSerialisedLatLongLocIDGridMapToUse
	 */
	public static void getDistanceBetweenLocIDs(String pathToSerialisedLatLongLocIDGridMapToUse)
	{
		try
		{
			List<Pair<Triple<Double, Double, String>, GridCell>> latLonLocIDGridCellAllLocs = (List<Pair<Triple<Double, Double, String>, GridCell>>) Serializer
					.kryoDeSerializeThis(pathToSerialisedLatLongLocIDGridMapToUse);

			// locID1,locID2
			// Map<HashSet<Integer>,>
			int countOfComputations = 0;
			int numOfUniqueLocIDs = latLonLocIDGridCellAllLocs.size();

			for (Pair<Triple<Double, Double, String>, GridCell> eOuter : latLonLocIDGridCellAllLocs)
			{
				double lat1 = eOuter.getFirst().getFirst();
				double lon1 = eOuter.getFirst().getSecond();
				Integer locID = Integer.valueOf(eOuter.getFirst().getThird());

				for (Pair<Triple<Double, Double, String>, GridCell> eInner : latLonLocIDGridCellAllLocs)
				{
					double lat2 = eInner.getFirst().getFirst();
					double lon2 = eInner.getFirst().getSecond();
					Integer locID2 = Integer.valueOf(eInner.getFirst().getThird());

					if (locID.equals(locID2) == false)
					{
						double haversineDist5 = SpatialUtils.haversineFastMathV3NoRound(lat1, lon1, lat2, lon2);
						countOfComputations += 1;
					}
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void main(String args[])
	{
		//
		System.out.println("Inside main()");
		mainToComputeDistancesBetweenGrids();
	}

	public static void analysePrecomputedDistances()
	{
		String host = "";
		String pathToReadData = "";
		String commonPathToWrite = "";
		int limitOnNumberOfComputations = 200;// 10000000;
		String fileNamePhrase = "IntDoubleWith1DConverter";

		// String passwd = Utils.getPassWordForHost(host);
		// String user = Utils.getUserForHost(host);

		try
		{
			WToFile.createDirectoryIfNotExists(commonPathToWrite);
			WToFile.redirectConsoleOutput(commonPathToWrite + "consoleLog.txt");

			// Deserialise
			HashMap<Integer, Double> gridIndexPairHaversineDist = (HashMap<Integer, Double>) Serializer
					.kryoDeSerializeThis(pathToReadData + "gridIndexPairHaversineDist" + fileNamePhrase + ".kryo");
			System.out.println("deserialised gridIndexPairHaversineDist.size()= " + gridIndexPairHaversineDist.size());

			PairedIndicesTo1DArrayConverter pairedIndicesTo1DArrayConverter = (PairedIndicesTo1DArrayConverter) Serializer
					.kryoDeSerializeThis(commonPathToWrite + "pairedIndicesTo1DConverter" + fileNamePhrase + ".kryo");
			System.out.println(
					"deserialised pairedIndicesTo1DArrayConverter= " + pairedIndicesTo1DArrayConverter.toString());

			////////////

			// if (host.contains("local"))
			// {// each row corresponds to a user
			// readResFromFile = ReadingFromFile.nColumnReaderDouble(statFileForThisMU, ",", statFileHasHeader);
			// }
			// else
			// {
			// Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port,
			// statFileForThisMU, user, passwd);
			// readResFromFile = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",",
			// statFileHasHeader);
			// }
			//////////////

		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public static void mainToComputeDistancesBetweenGrids()
	{
		System.out.println("Inside mainToComputeDistancesBetweenGrids()");
		// $$ readLocDataMapToGGridCellAndGridIndex();//disabled on 22 July 2018

		// Used on 30 July and then disabled
		// $$mapJavaGridIndexToRGridID(true,
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/July30RGridIDJavaGridIndex/");//
		// July22RGridIDJavaGridIndex
		// System//.exit(0);
		// long t1 = System.currentTimeMillis();
		// Map<FastFlippableIntPair, Double> precomputedDistances = (Map<FastFlippableIntPair, Double>) Serializer
		// .kryoDeSerializeThis(
		// "./dataWritten/July23RGridIDJavaGridIndexDistances_3/gridIndexPairHaversineDist.kryo");
		// long t2 = System.currentTimeMillis();
		// System.out.println("Deserialisation took: " + (t2 - t1) / 1000 + " secs");
		// System.out.println("precomputedDistances.size() = " + precomputedDistances.size());
		// System.out.println(" used memory in MiB: " + PerformanceAnalytics.getUsedMemoryInMB());

		String commonPathToRead = "./dataToRead/July30RGridIDJavaGridIndex/";// July22RGridIDJavaGridIndex
		/// run/media/gunjan/BackupVault/GOWALLA/GowallaResults
		// "./dataWritten/"/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/
		String commonPathToWrite = "./dataWritten/" + DateTimeUtils.getMonthDateLabel() + "GridIndexDistances/";
		// PairedIndicesComplexityComparison/";
		WToFile.createDirectoryIfNotExists(commonPathToWrite);
		WToFile.redirectConsoleOutput(commonPathToWrite + "consoleLog.txt");

		int limitOnNumberOfComputations = 200;// 10000000;

		// computeDistancesBetweenGridCentresFFlippableIntPair(commonPathToWrite, true, commonPathToRead,
		// limitOnNumberOfComputations, "FFlippableIntPair");
		//
		// computeDistancesBetweenGridCentresStringKeys(commonPathToWrite, true, commonPathToRead,
		// limitOnNumberOfComputations, "StringKeys");
		//
		// computeDistancesBetweenGridCentresPairedTo1D(commonPathToWrite, true, commonPathToRead,
		// limitOnNumberOfComputations, "PairedTo1D");

		String fileNamePhrase = "IntDoubleWith1DConverter";
		computeDistancesBetweenGridCentresIntDoubleWith1DConverter(commonPathToWrite, true, commonPathToRead,
				limitOnNumberOfComputations, fileNamePhrase);

		if (true)
		{
			HashMap<Integer, Double> gridIndexPairHaversineDist = (HashMap<Integer, Double>) Serializer
					.kryoDeSerializeThis(commonPathToWrite + "gridIndexPairHaversineDist" + fileNamePhrase + ".kryo");
			// Int2DoubleOpenHashMap gridIndexPairHaversineDist = (Int2DoubleOpenHashMap) Serializer
			// .kryoDeSerializeThis(commonPathToWrite + "gridIndexPairHaversineDist" + fileNamePhrase + ".kryo");
			// Int2DoubleOpenHashMap
			System.out.println("deserialised gridIndexPairHaversineDist.size()= " + gridIndexPairHaversineDist.size());
		}

		if (true)
		{
			PairedIndicesTo1DArrayConverter pairedIndicesTo1DArrayConverter =
					// (PairedIndicesTo1DArrayConverter) Serializer
					// .kryoDeSerializeThis(commonPathToWrite + "pairedIndicesTo1DConverter" + fileNamePhrase + ".kryo",
					// PairedIndicesTo1DArrayConverter.class);
					(PairedIndicesTo1DArrayConverter) Serializer.kryoDeSerializeThis(
							commonPathToWrite + "pairedIndicesTo1DConverter" + fileNamePhrase + ".kryo");
			System.out.println(
					"deserialised pairedIndicesTo1DArrayConverter= " + pairedIndicesTo1DArrayConverter.toString());
		}

		//

	}

	/**
	 * USED
	 * 
	 * @param pathToWrite
	 * @param writeToFile
	 * @param commonPathToRead
	 * @param limitOfNumOfComputations
	 * @param fileNamePhraseToWrite
	 * @since 24 July 2018
	 */
	public static void computeDistancesBetweenGridCentresIntDoubleWith1DConverter(String pathToWrite,
			boolean writeToFile, String commonPathToRead, int limitOfNumOfComputations, String fileNamePhraseToWrite)
	{
		String pathToJavaGridIndexRGridID = commonPathToRead + "javaGridIndexRGridID.kryo";
		String pathToJavaGridIndexRGridLatRGridLon = commonPathToRead + "javaGridIndexRGridLatRGridLon.kryo";
		System.out.println("\n\nInside computeDistancesBetweenGridCentres using FastFlippableIntPair: ");
		try
		{
			Map<Integer, String> javaGridIndexRGridID = (Map<Integer, String>) Serializer
					.kryoDeSerializeThis(pathToJavaGridIndexRGridID);
			Map<Integer, double[]> javaGridIndexRGridLatRGridLon = (Map<Integer, double[]>) Serializer
					.kryoDeSerializeThis(pathToJavaGridIndexRGridLatRGridLon);

			System.out.println("javaGridIndexRGridID.size() = " + javaGridIndexRGridID.size()
					+ "\njavaGridIndexRGridLatRGridLon.size()=" + javaGridIndexRGridLatRGridLon.size());
			Sanity.eq(javaGridIndexRGridID.size(), javaGridIndexRGridLatRGridLon.size(),
					"Error: javaGridIndexRGridID.size()!=njavaGridIndexRGridLatRGridLon.size()");

			PairedIndicesTo1DArrayConverter pairedIndicesTo1DConverter = new PairedIndicesTo1DArrayConverter(
					javaGridIndexRGridID.size());
			int numOfUniquePairs = pairedIndicesTo1DConverter.getSizeOf1DArray();
			System.out.println("numOfUniquePairs = " + numOfUniquePairs);
			if (numOfUniquePairs > Integer.MAX_VALUE)
			{
				PopUps.showError("numOfUniquePairs= " + numOfUniquePairs + "  > Integer.MAX_VALUE");
			}
			HashMap<Integer, Double> gridIndexPairHaversineDist = new HashMap<>((int) numOfUniquePairs);
			// Int2DoubleOpenHashMap gridIndexPairHaversineDist = new Int2DoubleOpenHashMap((int) numOfUniquePairs);

			// sanity check indices start: check is all gridIndices are 1... size with increment of 1.
			for (int index = 0; index < javaGridIndexRGridLatRGridLon.size(); index++)
			{
				if (javaGridIndexRGridLatRGridLon.containsKey(index) == false)
				{
					PopUps.showError("Error: javaGridIndexRGridLatRGridLon does not contain index = " + index);
				}
			}
			// sanity check indices end: check is all gridIndices are 1... size with increment of 1.

			if (true)
			{
				int countOfComputations = 0;
				long t1 = System.currentTimeMillis();

				for (int gridIndex1 = 0; gridIndex1 < (javaGridIndexRGridLatRGridLon.size() - 1); gridIndex1++)
				{
					double[] latLonEntry1 = javaGridIndexRGridLatRGridLon.get(gridIndex1);
					double lat1 = latLonEntry1[0];
					double lon1 = latLonEntry1[1];

					for (int gridIndex2 = gridIndex1 + 1; gridIndex2 < (javaGridIndexRGridLatRGridLon
							.size()); gridIndex2++)
					{
						double[] latLonEntry2 = javaGridIndexRGridLatRGridLon.get(gridIndex2);
						double lat2 = latLonEntry2[0];
						double lon2 = latLonEntry2[1];

						double haversineDist5 = SpatialUtils.haversineFastMathV3NoRound(lat1, lon1, lat2, lon2);

						// start of added on 29 July 2018
						if (haversineDist5 < 1.009)
						{
							String rGridID1 = javaGridIndexRGridID.get(gridIndex1);
							String rGridID2 = javaGridIndexRGridID.get(gridIndex2);
							WToFile.writeToNewFile(
									rGridID1 + "," + gridIndex1 + "," + lat1 + "," + lon1 + "," + rGridID2 + ","
											+ gridIndex2 + "," + lat2 + "," + lon2 + "," + haversineDist5 + "\n",
									pathToWrite + fileNamePhraseToWrite + "VeryLowDist.csv");
						}
						// end of added on 29 July 2018

						countOfComputations += 1;

						gridIndexPairHaversineDist.put(
								pairedIndicesTo1DConverter.pairedIndicesTo1DArrayIndex(gridIndex1, gridIndex2),
								haversineDist5);
						// if (countOfComputations > 10000){ break;}
						if (limitOfNumOfComputations > 0 && (countOfComputations % limitOfNumOfComputations == 0))
						{
							System.out.print("countOfComputations = " + countOfComputations);
							System.out.println("  used memory in MiB: " + PerformanceAnalytics.getUsedMemoryInMB());
							break;
						}
						if (countOfComputations % 500000 == 0)
						{
							System.out.print("countOfComputations = " + countOfComputations);
							System.out.println("  used memory in MiB: " + PerformanceAnalytics.getUsedMemoryInMB());
						}
					}
					if (limitOfNumOfComputations > 0 && (countOfComputations % limitOfNumOfComputations == 0))
					{
						break;
					}
				}

				System.out.println("#HaversineComputations = " + countOfComputations + " time taken= "
						+ (System.currentTimeMillis() - t1) + " ms");
			}

			System.out
					.println("After computation, before serialisation:\n" + PerformanceAnalytics.getHeapInformation());
			System.out.println("gridIndexPairHaversineDist.size() = " + gridIndexPairHaversineDist.size());
			Serializer.kryoSerializeThis(gridIndexPairHaversineDist,
					pathToWrite + "gridIndexPairHaversineDist" + fileNamePhraseToWrite + ".kryo");
			Serializer.kryoSerializeThis(pairedIndicesTo1DConverter,
					pathToWrite + "pairedIndicesTo1DConverter" + fileNamePhraseToWrite + ".kryo");
			System.out.println("After computation, after serialisation:\n" + PerformanceAnalytics.getHeapInformation());

			if (writeToFile)
			{
				LeakyBucket lb = new LeakyBucket(10000,
						pathToWrite + "gridIndexPairHaversineDist" + fileNamePhraseToWrite + ".csv", false);
				lb.addToLeakyBucketWithNewline("1DIndex,HaversineDist");
				gridIndexPairHaversineDist.entrySet().stream()
						.forEachOrdered(e -> lb.addToLeakyBucketWithNewline(e.getKey() + "," + e.getValue()));
				lb.flushLeakyBucket();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param pathToWrite
	 * @param writeToFile
	 * @param commonPathToRead
	 * @param limitOfNumOfComputations
	 * @param fileNamePhraseToWrite
	 */
	public static void computeDistancesBetweenGridCentresPairedTo1D(String pathToWrite, boolean writeToFile,
			String commonPathToRead, int limitOfNumOfComputations, String fileNamePhraseToWrite)
	{
		String pathToJavaGridIndexRGridID = commonPathToRead + "javaGridIndexRGridID.kryo";
		String pathToJavaGridIndexRGridLatRGridLon = commonPathToRead + "javaGridIndexRGridLatRGridLon.kryo";
		System.out.println("\n\nInside computeDistancesBetweenGridCentresV3 using PairedIndicesTo1DArrayStorage: ");
		try
		{
			Map<Integer, String> javaGridIndexRGridID = (Map<Integer, String>) Serializer
					.kryoDeSerializeThis(pathToJavaGridIndexRGridID);
			Map<Integer, double[]> javaGridIndexRGridLatRGridLon = (Map<Integer, double[]>) Serializer
					.kryoDeSerializeThis(pathToJavaGridIndexRGridLatRGridLon);

			System.out.println("javaGridIndexRGridID.size() = " + javaGridIndexRGridID.size()
					+ "\njavaGridIndexRGridLatRGridLon.size()=" + javaGridIndexRGridLatRGridLon.size());
			Sanity.eq(javaGridIndexRGridID.size(), javaGridIndexRGridLatRGridLon.size(),
					"Error: javaGridIndexRGridID.size()!=njavaGridIndexRGridLatRGridLon.size()");

			long numOfUniquePairs = ((long) javaGridIndexRGridID.size() * ((long) javaGridIndexRGridID.size() - 1)) / 2; // n(n-1)/2
			System.out.println("numOfUniquePairs = " + numOfUniquePairs);
			if (numOfUniquePairs > Integer.MAX_VALUE)
			{
				PopUps.showError("numOfUniquePairs= " + numOfUniquePairs + "  > Integer.MAX_VALUE");
			}

			PairedIndicesTo1DArrayStorage gridIndexPairHaversineDist = new PairedIndicesTo1DArrayStorage(
					javaGridIndexRGridID.size());

			// sanity check indices start: check is all gridIndices are 1... size with increment of 1.
			for (int index = 0; index < javaGridIndexRGridLatRGridLon.size(); index++)
			{
				if (javaGridIndexRGridLatRGridLon.containsKey(index) == false)
				{
					PopUps.showError("Error: javaGridIndexRGridLatRGridLon does not contain index = " + index);
				}
			}
			// sanity check indices end: check is all gridIndices are 1... size with increment of 1.

			if (true)
			{
				int countOfComputations = 0;
				long t1 = System.currentTimeMillis();

				for (int index1 = 0; index1 < (javaGridIndexRGridLatRGridLon.size() - 1); index1++)
				{
					double[] latLonEntry1 = javaGridIndexRGridLatRGridLon.get(index1);
					double lat1 = latLonEntry1[0];
					double lon1 = latLonEntry1[1];

					for (int index2 = index1 + 1; index2 < (javaGridIndexRGridLatRGridLon.size() - 1); index2++)
					{
						double[] latLonEntry2 = javaGridIndexRGridLatRGridLon.get(index2);
						double lat2 = latLonEntry2[0];
						double lon2 = latLonEntry2[1];

						double haversineDist5 = SpatialUtils.haversineFastMathV3NoRound(lat1, lon1, lat2, lon2);
						countOfComputations += 1;

						// gridIndexPairHaversineDist.put(getOrderedIndexPairString(index1, index2), haversineDist5);
						gridIndexPairHaversineDist.addVal(index1, index2, haversineDist5);
						// if (countOfComputations > 10000){ break;}
						if (limitOfNumOfComputations > 0 && (countOfComputations % limitOfNumOfComputations == 0))
						{
							System.out.print("countOfComputations = " + countOfComputations);
							System.out.println("  used memory in MiB: " + PerformanceAnalytics.getUsedMemoryInMB());
							break;
						}
					}
					if (limitOfNumOfComputations > 0 && (countOfComputations % limitOfNumOfComputations == 0))
					{
						break;
					}
				}

				System.out.println("#HaversineComputations = " + countOfComputations + " time taken= "
						+ (System.currentTimeMillis() - t1) + " ms");
			}

			System.out
					.println("After computation, before serialisation:\n" + PerformanceAnalytics.getHeapInformation());
			System.out.println(
					"gridIndexPairHaversineDist.size() = " + gridIndexPairHaversineDist.getValsStored().length);
			Serializer.kryoSerializeThis(gridIndexPairHaversineDist,
					pathToWrite + "gridIndexPairHaversineDist" + fileNamePhraseToWrite + ".kryo");
			System.out.println("After computation, after serialisation:\n" + PerformanceAnalytics.getHeapInformation());

			if (writeToFile)
			{
				LeakyBucket lb = new LeakyBucket(10000,
						pathToWrite + "gridIndexPairHaversineDist" + fileNamePhraseToWrite + ".csv", false);
				double[] vals = gridIndexPairHaversineDist.getValsStored();

				for (double v : vals)
				{
					lb.addToLeakyBucketWithNewline(String.valueOf(v));
				}

				lb.flushLeakyBucket();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param pathToWrite
	 * @param writeToFile
	 * @param commonPathToRead
	 * @param limitOfNumOfComputations
	 * @param fileNamePhraseToWrite
	 */
	public static void computeDistancesBetweenGridCentresStringKeys(String pathToWrite, boolean writeToFile,
			String commonPathToRead, int limitOfNumOfComputations, String fileNamePhraseToWrite)
	{
		String pathToJavaGridIndexRGridID = commonPathToRead + "javaGridIndexRGridID.kryo";
		String pathToJavaGridIndexRGridLatRGridLon = commonPathToRead + "javaGridIndexRGridLatRGridLon.kryo";
		System.out.println("\n\nInside computeDistancesBetweenGridCentresV2 using String keys: ");
		try
		{
			Map<Integer, String> javaGridIndexRGridID = (Map<Integer, String>) Serializer
					.kryoDeSerializeThis(pathToJavaGridIndexRGridID);
			Map<Integer, double[]> javaGridIndexRGridLatRGridLon = (Map<Integer, double[]>) Serializer
					.kryoDeSerializeThis(pathToJavaGridIndexRGridLatRGridLon);

			System.out.println("javaGridIndexRGridID.size() = " + javaGridIndexRGridID.size()
					+ "\njavaGridIndexRGridLatRGridLon.size()=" + javaGridIndexRGridLatRGridLon.size());
			Sanity.eq(javaGridIndexRGridID.size(), javaGridIndexRGridLatRGridLon.size(),
					"Error: javaGridIndexRGridID.size()!=njavaGridIndexRGridLatRGridLon.size()");

			long numOfUniquePairs = ((long) javaGridIndexRGridID.size() * ((long) javaGridIndexRGridID.size() - 1)) / 2; // n(n-1)/2
			System.out.println("numOfUniquePairs = " + numOfUniquePairs);
			if (numOfUniquePairs > Integer.MAX_VALUE)
			{
				PopUps.showError("numOfUniquePairs= " + numOfUniquePairs + "  > Integer.MAX_VALUE");
			}

			Map<String, Double> gridIndexPairHaversineDist = new HashMap<>((int) numOfUniquePairs + 1);

			// sanity check indices start: check is all gridIndices are 1... size with increment of 1.
			for (int index = 0; index < javaGridIndexRGridLatRGridLon.size(); index++)
			{
				if (javaGridIndexRGridLatRGridLon.containsKey(index) == false)
				{
					PopUps.showError("Error: javaGridIndexRGridLatRGridLon does not contain index = " + index);
				}
			}
			// sanity check indices end: check is all gridIndices are 1... size with increment of 1.

			if (true)
			{
				int countOfComputations = 0;
				long t1 = System.currentTimeMillis();

				for (int index1 = 0; index1 < (javaGridIndexRGridLatRGridLon.size() - 1); index1++)
				{
					double[] latLonEntry1 = javaGridIndexRGridLatRGridLon.get(index1);
					double lat1 = latLonEntry1[0];
					double lon1 = latLonEntry1[1];

					for (int index2 = index1 + 1; index2 < (javaGridIndexRGridLatRGridLon.size() - 1); index2++)
					{
						double[] latLonEntry2 = javaGridIndexRGridLatRGridLon.get(index2);
						double lat2 = latLonEntry2[0];
						double lon2 = latLonEntry2[1];

						double haversineDist5 = SpatialUtils.haversineFastMathV3NoRound(lat1, lon1, lat2, lon2);
						countOfComputations += 1;

						gridIndexPairHaversineDist.put(getOrderedIndexPairString(index1, index2), haversineDist5);
						// if (countOfComputations > 10000){ break;}
						if (limitOfNumOfComputations > 0 && (countOfComputations % limitOfNumOfComputations == 0))
						{
							System.out.print("countOfComputations = " + countOfComputations);
							System.out.println("  used memory in MiB: " + PerformanceAnalytics.getUsedMemoryInMB());
							break;
						}
					}
					if (limitOfNumOfComputations > 0 && (countOfComputations % limitOfNumOfComputations == 0))
					{
						break;
					}
				}

				System.out.println("#HaversineComputations = " + countOfComputations + " time taken= "
						+ (System.currentTimeMillis() - t1) + " ms");
			}

			System.out
					.println("After computation, before serialisation:\n" + PerformanceAnalytics.getHeapInformation());
			System.out.println("gridIndexPairHaversineDist.size() = " + gridIndexPairHaversineDist.size());
			Serializer.kryoSerializeThis(gridIndexPairHaversineDist,
					pathToWrite + "gridIndexPairHaversineDist" + fileNamePhraseToWrite + ".kryo");
			System.out.println("After computation, after serialisation:\n" + PerformanceAnalytics.getHeapInformation());

			if (writeToFile)
			{
				LeakyBucket lb = new LeakyBucket(10000,
						pathToWrite + "gridIndexPairHaversineDist" + fileNamePhraseToWrite + ".csv", false);
				gridIndexPairHaversineDist.entrySet().stream()
						.forEachOrdered(e -> lb.addToLeakyBucketWithNewline(e.getKey() + "," + e.getValue()));
				lb.flushLeakyBucket();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static String getOrderedIndexPairString(int index1, int index2)
	{
		if (index1 <= index2)
		{
			return index1 + "_" + index2;
		}
		else
		{
			return index2 + "_" + index1;
		}
	}

	/**
	 * USED
	 * 
	 * @param pathToWrite
	 * @param writeToFile
	 * @param commonPathToRead
	 * @param limitOfNumOfComputations
	 * @param fileNamePhraseToWrite
	 * @since 22 July 2018
	 */
	public static void computeDistancesBetweenGridCentresFFlippableIntPair(String pathToWrite, boolean writeToFile,
			String commonPathToRead, int limitOfNumOfComputations, String fileNamePhraseToWrite)
	{
		String pathToJavaGridIndexRGridID = commonPathToRead + "javaGridIndexRGridID.kryo";
		String pathToJavaGridIndexRGridLatRGridLon = commonPathToRead + "javaGridIndexRGridLatRGridLon.kryo";
		System.out.println("\n\nInside computeDistancesBetweenGridCentres using FastFlippableIntPair: ");
		try
		{
			Map<Integer, String> javaGridIndexRGridID = (Map<Integer, String>) Serializer
					.kryoDeSerializeThis(pathToJavaGridIndexRGridID);
			Map<Integer, double[]> javaGridIndexRGridLatRGridLon = (Map<Integer, double[]>) Serializer
					.kryoDeSerializeThis(pathToJavaGridIndexRGridLatRGridLon);

			System.out.println("javaGridIndexRGridID.size() = " + javaGridIndexRGridID.size()
					+ "\njavaGridIndexRGridLatRGridLon.size()=" + javaGridIndexRGridLatRGridLon.size());
			Sanity.eq(javaGridIndexRGridID.size(), javaGridIndexRGridLatRGridLon.size(),
					"Error: javaGridIndexRGridID.size()!=njavaGridIndexRGridLatRGridLon.size()");

			long numOfUniquePairs = ((long) javaGridIndexRGridID.size() * ((long) javaGridIndexRGridID.size() - 1)) / 2; // n(n-1)/2
			System.out.println("numOfUniquePairs = " + numOfUniquePairs);
			if (numOfUniquePairs > Integer.MAX_VALUE)
			{
				PopUps.showError("numOfUniquePairs= " + numOfUniquePairs + "  > Integer.MAX_VALUE");
			}

			Map<FastFlippableIntPair, Double> gridIndexPairHaversineDist = new HashMap<>((int) numOfUniquePairs + 1);

			// sanity check indices start: check is all gridIndices are 1... size with increment of 1.
			for (int index = 0; index < javaGridIndexRGridLatRGridLon.size(); index++)
			{
				if (javaGridIndexRGridLatRGridLon.containsKey(index) == false)
				{
					PopUps.showError("Error: javaGridIndexRGridLatRGridLon does not contain index = " + index);
				}
			}
			// sanity check indices end: check is all gridIndices are 1... size with increment of 1.

			if (true)
			{
				int countOfComputations = 0;
				long t1 = System.currentTimeMillis();

				for (int index1 = 0; index1 < (javaGridIndexRGridLatRGridLon.size() - 1); index1++)
				{
					double[] latLonEntry1 = javaGridIndexRGridLatRGridLon.get(index1);
					double lat1 = latLonEntry1[0];
					double lon1 = latLonEntry1[1];

					for (int index2 = index1 + 1; index2 < (javaGridIndexRGridLatRGridLon.size() - 1); index2++)
					{
						double[] latLonEntry2 = javaGridIndexRGridLatRGridLon.get(index2);
						double lat2 = latLonEntry2[0];
						double lon2 = latLonEntry2[1];

						double haversineDist5 = SpatialUtils.haversineFastMathV3NoRound(lat1, lon1, lat2, lon2);
						countOfComputations += 1;

						gridIndexPairHaversineDist.put(new FastFlippableIntPair(index1, index2), haversineDist5);
						// if (countOfComputations > 10000){ break;}
						if (limitOfNumOfComputations > 0 && (countOfComputations % limitOfNumOfComputations == 0))
						{
							System.out.print("countOfComputations = " + countOfComputations);
							System.out.println("  used memory in MiB: " + PerformanceAnalytics.getUsedMemoryInMB());
							break;
						}
					}
					if (limitOfNumOfComputations > 0 && (countOfComputations % limitOfNumOfComputations == 0))
					{
						break;
					}
				}

				System.out.println("#HaversineComputations = " + countOfComputations + " time taken= "
						+ (System.currentTimeMillis() - t1) + " ms");
			}

			System.out
					.println("After computation, before serialisation:\n" + PerformanceAnalytics.getHeapInformation());
			System.out.println("gridIndexPairHaversineDist.size() = " + gridIndexPairHaversineDist.size());
			Serializer.kryoSerializeThis(gridIndexPairHaversineDist,
					pathToWrite + "gridIndexPairHaversineDist" + fileNamePhraseToWrite + ".kryo");
			System.out.println("After computation, after serialisation:\n" + PerformanceAnalytics.getHeapInformation());

			if (writeToFile)
			{
				LeakyBucket lb = new LeakyBucket(10000,
						pathToWrite + "gridIndexPairHaversineDist" + fileNamePhraseToWrite + ".csv", false);
				gridIndexPairHaversineDist.entrySet().stream().forEachOrdered(e -> lb.addToLeakyBucketWithNewline(
						e.getKey().getLower() + "," + e.getKey().getHigher() + "," + e.getValue()));
				lb.flushLeakyBucket();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * So effectively speaking now we have locID -> JavaGridIndex and locID -> RGridID
	 * 
	 * <p>
	 * todo: map RGridID -> JavaGridIndex
	 * <p>
	 * how:
	 * <ol>
	 * <li>read locID -> JavaGridIndex</li>
	 * <li>read locID -> RGridID</li>
	 * <li>read RGridID -> RGridIDCentre (lat,lon)</li>
	 * <li>loop over locID: - create and serialise JavaGridIndex-RgridID map - create and serialise
	 * RgridID-JavaGridIndex map</li>
	 * <li>loop over RGridIDs: - create and serialise JavaGridIndex-RGridLatRGridLon map</li>
	 * </ol>
	 * <p>
	 * 
	 * writes:wrote: a)javaGridIndexRGridID.csv/kryo, b)javaGridIndexRGridLatRGridLon.csv/kryo (ps: 07/22/18 12:29:33
	 * PM)
	 * 
	 * @since 22 July 2018
	 */
	public static void mapJavaGridIndexToRGridID(boolean writeToFile, String absPathToWrite)
	{
		WToFile.createDirectoryIfNotExists(absPathToWrite);
		String pathToRLocIDGridID = "/home/gunjan/RWorkspace/GowallaRWorks/locIDCellIDUsingR.csv";
		String pathToRGridIDCentreLatLon = "/home/gunjan/RWorkspace/GowallaRWorks/cellIDWithCentreLatLongUniqueWithinUsingR.csv";

		Map<Integer, String> javaGridIndexRGridID = new LinkedHashMap<>();
		Map<String, Integer> RGridIDJavaGridIndex = new LinkedHashMap<>();
		Map<Integer, double[]> javaGridIndexRGridLatRGridLon = new LinkedHashMap<>();
		// Map<Integer, String> javaGridIndexRGridID = new LinkedHashMap<>();
		//
		try
		{
			// locID --> JavaGridIndex
			Map<Long, Integer> locIDJavaGridIndexGowallaMap = (Map<Long, Integer>) Serializer
					.kryoDeSerializeThis(PathConstants.pathToSerialisedLocIDGridIndexGowallaMap);
			System.out.println("locIDJavaGridIndexGowallaMap.size()= " + locIDJavaGridIndexGowallaMap.size());

			// locID -> RGridID
			List<List<String>> rLocIDRGridID = ReadingFromFile.nColumnReaderString(pathToRLocIDGridID, ",", true);
			System.out.println("rLocIDRGridID.size()= " + rLocIDRGridID.size());

			// RGridID -> RGridIDCentre (lat,lon)
			List<List<String>> rGridIDCentreLatLon = ReadingFromFile.nColumnReaderString(pathToRGridIDCentreLatLon, ",",
					true);
			System.out.println("rGridIDCentreLatLon.size()= " + rGridIDCentreLatLon.size());

			// to create JavaGridIndex -> RGridID and RGridID -> JavaGridIndex
			for (List<String> entry : rLocIDRGridID)
			{
				Long locID = Long.valueOf(entry.get(0));
				String rGridID = entry.get(3);
				// Double locLat = Double.valueOf(entry.get(1));// before 31 July: mistook this as cell centre lat
				// Double locLon = Double.valueOf(entry.get(2));
				Integer javaGridIndexForThisLocID = locIDJavaGridIndexGowallaMap.get(locID);
				javaGridIndexRGridID.put(javaGridIndexForThisLocID, rGridID);
				RGridIDJavaGridIndex.put(rGridID, javaGridIndexForThisLocID);
				// javaGridIndexRGridLatRGridLon.put(javaGridIndexForThisLocID, new double[] { locLat, locLon });
			}

			System.out.println("javaGridIndexRGridID.size()= " + javaGridIndexRGridID.size());
			Serializer.kryoSerializeThis(javaGridIndexRGridID, absPathToWrite + "javaGridIndexRGridID.kryo");
			System.out.println("RGridIDJavaGridIndex.size()= " + RGridIDJavaGridIndex.size());
			Serializer.kryoSerializeThis(RGridIDJavaGridIndex, absPathToWrite + "RGridIDJavaGridIndex.kryo");

			// to create JavaGridIndex -> RGridCentre (Lat,Lon)
			for (List<String> entry : rGridIDCentreLatLon)
			{// cellID,CentrLat,CentreLon,numUniqueLocationsInside
				String rGridID = entry.get(0);
				Double centreLat = Double.valueOf(entry.get(1));
				Double centreLon = Double.valueOf(entry.get(2));
				javaGridIndexRGridLatRGridLon.put(RGridIDJavaGridIndex.get(rGridID),
						new double[] { centreLat, centreLon });
			}

			System.out.println("javaGridIndexRGridLatRGridLon.size()= " + javaGridIndexRGridLatRGridLon.size());
			Serializer.kryoSerializeThis(javaGridIndexRGridLatRGridLon,
					absPathToWrite + "javaGridIndexRGridLatRGridLon.kryo");

			if (writeToFile)
			{
				StringBuilder sb = new StringBuilder("javaGridIndex,RGridID\n");
				javaGridIndexRGridID.entrySet().stream()
						.forEachOrdered(e -> sb.append(e.getKey() + "," + e.getValue() + "\n"));
				WToFile.writeToNewFile(sb.toString(), absPathToWrite + "javaGridIndexRGridID.csv");

				StringBuilder sb2 = new StringBuilder("javaGridIndex,RGridCellLat,RGridCellLon,RGridID\n");
				javaGridIndexRGridLatRGridLon.entrySet().stream().forEachOrdered(e -> sb2.append(e.getKey() + ","
						+ e.getValue()[0] + "," + e.getValue()[1] + "," + javaGridIndexRGridID.get(e.getKey()) + "\n"));
				WToFile.writeToNewFile(sb2.toString(), absPathToWrite + "javaGridIndexRGridLatRGridLon.csv");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * <ul>
	 * <li>Read location data from
	 * /home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUL10ForLocationAnalysis2/</li>
	 * 
	 * <li>get geogrid gridcell for each loc using GLatLonToGridTransformer.locationsToGridCell() and serialise as
	 * latLonLocIDGridCellAllLocs</li>
	 * 
	 * <li>create and serialise locID-GridCellID map</li>
	 * <li>create and serialise locID-gridIndex map</li>
	 * <li>create and serialise gridIndex-GridCellID map</li>
	 * <li>create and serialise GridCellID-gridIndex map</li>
	 * <li>create and serialise GridCellID-locIDs map</li>
	 * <li>create and serialise GridCellID-GridCell map</li>
	 * </ul>
	 */
	public static void readLocDataMapToGGridCellAndGridIndex()
	{
		String pathToWrite = "./dataWritten/HexGridRes" + gridResolution + "_" + DateTimeUtils.getMonthDateLabel()
				+ "/";
		WToFile.createDirectoryIfNotExists(pathToWrite);
		WToFile.redirectConsoleOutput(pathToWrite + "consoleLog.txt");
		System.out.println("gridResolution = " + gridResolution);
		try
		{
			GLatLonToGridTransformer gt = new GLatLonToGridTransformer(gridResolution);

			if (false)// sanity check
			{
				Collection<GridCell> cells = gt.getGridCellsForBound(41, 42, 6, 7);
				System.out.println(cells.size());
				System.out.println("------");

				double lat = 67.213117;
				double lon = -113.807761;

				GridCell c = gt.getCellForLatLon(lat, lon);
				System.out.println(c);
				System.out.println("------");
				System.out.println("GridID= " + c.getID());
				System.out.println("lat of cell= " + c.getLat() + " lon of cell= " + c.getLon());
			}

			if (true)
			{
				String pathToLocationAnalysis = "./dataWritten/JUL10ForLocationAnalysis2/";
				String absFileNameForLatLon5MostRecenTrainTestJul10 = pathToLocationAnalysis
						+ "UniqueLocationObjects5DaysTrainTest.csv";
				String absFileNameForLatLonAllJul10 = pathToLocationAnalysis + "UniqueLocationObjects.csv";
				int latColIndex2 = 9, lonColIndex2 = 10, labelColIndex2 = 0;// locID as label

				// lat,lon,locationID
				List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile.readListOfLocationsV2(
						absFileNameForLatLonAllJul10, ",", latColIndex2, lonColIndex2, labelColIndex2);
				System.out.println("listOfLocs.size()= " + listOfLocs.size());

				long t1 = System.currentTimeMillis();
				// lat,lon,locationID, GridCell (GeoGrid)
				List<Pair<Triple<Double, Double, String>, GridCell>> latLonLocIDGridCellAllLocs = locationsToGridCell(
						listOfLocs, gt, true, pathToWrite + "latLonLocIDGridCellAllLocs.csv");
				long t2 = System.currentTimeMillis();

				System.out.println("Time taken to find and write grids = " + (t2 - t1) + " secs");

				Set<GridCell> uniqueGridCells = latLonLocIDGridCellAllLocs.stream().map(v -> v.getSecond())
						.collect(Collectors.toSet());
				System.out.println("uniqueGridCells.size()= " + uniqueGridCells.size());

				Map<Long, GridCell> gridIDGridCellMap = uniqueGridCells.stream()
						.collect(Collectors.toMap(gc -> gc.getID(), gc -> gc));
				System.out.println("gridIDGridCellMap.size()= " + uniqueGridCells.size());

				///////////////////////////
				Map<Integer, Long> gridIndexIDMap = new TreeMap<>();
				Map<Long, Integer> gridIDIndexMap = new TreeMap<>();
				int gridIDIndex = 0;
				for (Entry<Long, GridCell> e : gridIDGridCellMap.entrySet())
				{
					gridIndexIDMap.put(gridIDIndex, e.getKey());
					gridIDIndexMap.put(e.getKey(), gridIDIndex);
					gridIDIndex += 1;
				}
				System.out.println("gridIndexIDMap.size()= " + gridIndexIDMap.size());
				System.out.println("gridIDIndexMap.size()= " + gridIDIndexMap.size());
				///////////////////////////

				Map<Long, Long> locIDGridIDMap = latLonLocIDGridCellAllLocs.stream().collect(
						Collectors.toMap(e -> Long.valueOf(e.getFirst().getThird()), e -> e.getSecond().getID()));
				System.out.println("locIDGridIDMap.size()= " + locIDGridIDMap.size());

				Map<Long, Integer> locIDGridIndexMap = latLonLocIDGridCellAllLocs.stream().collect(Collectors.toMap(
						e -> Long.valueOf(e.getFirst().getThird()), e -> gridIDIndexMap.get(e.getSecond().getID())));
				System.out.println("locIDGridIndexMap.size()= " + locIDGridIndexMap.size());

				Map<Long, Set<Long>> gridIDLocIDs = getLocIDsInEachGrid(latLonLocIDGridCellAllLocs, true,
						pathToWrite + "gridIDLocIDs.csv");
				System.out.println("gridIDLocIDs.size()= " + gridIDLocIDs.size());

				Serializer.kryoSerializeThis(latLonLocIDGridCellAllLocs,
						pathToWrite + "latLonLocIDGridCellAllLocs.kryo");
				Serializer.kryoSerializeThis(gridIndexIDMap, pathToWrite + "gridIndexIDMap.kryo");
				Serializer.kryoSerializeThis(gridIDIndexMap, pathToWrite + "gridIDIndexMap.kryo");
				Serializer.kryoSerializeThis(locIDGridIDMap, pathToWrite + "locIDGridIDMap.kryo");
				Serializer.kryoSerializeThis(locIDGridIndexMap, pathToWrite + "locIDGridIndexMap.kryo");
				Serializer.kryoSerializeThis(gridIDLocIDs, pathToWrite + "gridIDLocIDs.kryo");
				Serializer.kryoSerializeThis(gridIDGridCellMap, pathToWrite + "gridIDGridCellMap.kryo");

				List<Long> allGridIDs = ReadingFromFile
						.oneColumnReaderLong(pathToWrite + "latLonLocIDGridCellAllLocs.csv", ",", 3, true);

				Set<Long> uniqueGridIDs = new TreeSet<>();
				uniqueGridIDs.addAll(allGridIDs);

				System.out.println("Num of gridIDs = " + allGridIDs.size());
				System.out.println("Num of unique gridIDs = " + uniqueGridIDs.size());
				System.out.println("% unique of total " + (100 * uniqueGridIDs.size()) / allGridIDs.size());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		WToFile.resetConsoleOutput();
	}

	private static Map<Long, Set<Long>> getLocIDsInEachGrid(
			List<Pair<Triple<Double, Double, String>, GridCell>> latLonLocIDGridCellAllLocs, boolean writeToFile,
			String absFileNameToWrite)
	{
		Map<Long, Set<Long>> gridIDLocIDs = new HashMap<>();

		// generate keys
		Set<Long> setOfGridIDs = latLonLocIDGridCellAllLocs.stream().map(e -> e.getSecond().getID())
				.collect(Collectors.toSet());

		// initialise keys
		setOfGridIDs.stream().forEach(gid -> gridIDLocIDs.put(gid, new TreeSet<>()));

		for (Pair<Triple<Double, Double, String>, GridCell> e : latLonLocIDGridCellAllLocs)
		{
			long gridID = e.getSecond().getID();
			long locID = Long.valueOf(e.getFirst().getThird());
			gridIDLocIDs.get(gridID).add(locID);
		}

		if (writeToFile)
		{
			LeakyBucket lb = new LeakyBucket(5000, absFileNameToWrite, false);
			lb.addToLeakyBucketWithNewline("gridID,NumOfLocIDs,LocIDs");

			List<Double> numOfLocIDsInEachGridID = new ArrayList<>();

			for (Entry<Long, Set<Long>> e : gridIDLocIDs.entrySet())
			{
				String locIDsString = e.getValue().stream().map(v -> String.valueOf(v))
						.collect(Collectors.joining("|"));

				int numOfLocIDs = e.getValue().size();
				numOfLocIDsInEachGridID.add((double) numOfLocIDs);

				lb.addToLeakyBucketWithNewline(e.getKey() + "," + numOfLocIDs + "," + locIDsString);
			}
			lb.flushLeakyBucket();

			// StatsUtils.binValuesByNumOfBins(numOfLocIDsInEachGridID, 200, true);
			StatsUtils.binValuesByBinSize(numOfLocIDsInEachGridID, 5, true, 0, "Histogram of numOfLocIDsInEachGridID");

		}

		return gridIDLocIDs;
	}

	/**
	 * Fetches Geogrid GridCell for for given list of {lat,lon,locID}
	 * 
	 * @param listOfLocs
	 *            { lat,lon,locationID}
	 * @param gt
	 * @param write
	 * @param absFileNameToWrite
	 * @return {lat,lon,locationID, gridCellUsingGeoGrid}
	 * @throws Exception
	 */
	private static List<Pair<Triple<Double, Double, String>, GridCell>> locationsToGridCell(
			List<Triple<Double, Double, String>> listOfLocs, GLatLonToGridTransformer gt, boolean write,
			String absFileNameToWrite) throws Exception
	{
		List<Pair<Triple<Double, Double, String>, GridCell>> res = new ArrayList<>(listOfLocs.size());
		LeakyBucket lb = new LeakyBucket(5000, absFileNameToWrite, false);
		lb.addToLeakyBucketWithNewline("Lat,Lon,Label,GridID");

		for (Triple<Double, Double, String> l : listOfLocs)
		{
			GridCell c = gt.getCellForLatLon(l.getFirst(), l.getSecond());
			res.add(new Pair<Triple<Double, Double, String>, GridCell>(l, c));
			lb.addToLeakyBucketWithNewline(
					l.getFirst() + "," + l.getSecond() + "," + l.getThird() + "," + String.valueOf(c.getID()));
		}
		lb.flushLeakyBucket();

		return res;
	}

	public GLatLonToGridTransformer(int resolution) throws Exception
	{
		// GRID
		theGrid = new ISEA3H(resolution);

		// print properties of the grid
		System.out.format("number of hexagon cells: %d%n", theGrid.numberOfHexagonalCells());
		System.out.format("number of pentagon cells: %d%n", theGrid.numberOfPentagonalCells());
		System.out.format("diameter of a hexagon cell (km): %f%n", theGrid.diameterOfHexagonalCellOnIcosahedron());
		System.out.format("area of a hexagon cell (sq km): %f%n", theGrid.areaOfAHexagonalCell());
		System.out.println("------");

	}

	/**
	 * get cells in given bounds
	 * 
	 * @param lat0
	 * @param lat1
	 * @param lon0
	 * @param lon1
	 * @return
	 * @throws Exception
	 */
	public Collection<GridCell> getGridCellsForBound(double lat0, double lat1, double lon0, double lon1)
			throws Exception
	{
		Collection<GridCell> cells = theGrid.cellsForBound(lat0, lat1, lon0, lon1);
		return cells;
	}

	/**
	 * determine cell for geographic coordinates
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 * @throws Exception
	 */
	public GridCell getCellForLatLon(double lat, double lon) throws Exception
	{
		return theGrid.cellForLocation(lat, lon);
	}

	public ISEA3H getTheGrid()
	{
		return theGrid;
	}
}
