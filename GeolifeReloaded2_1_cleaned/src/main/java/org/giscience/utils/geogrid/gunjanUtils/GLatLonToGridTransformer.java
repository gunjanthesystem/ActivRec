package org.giscience.utils.geogrid.gunjanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.LeakyBucket;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.stats.StatsUtils;
import org.activity.util.DateTimeUtils;
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

	public static void getDistanceBetweenLocIDs(String pathToSerialisedLatLongLocIDGridMapToUse)
	{
		try
		{
			List<Pair<Triple<Double, Double, String>, GridCell>> latLonLocIDGridCellAllLocs = (List<Pair<Triple<Double, Double, String>, GridCell>>) Serializer
					.kryoDeSerializeThis(pathToSerialisedLatLongLocIDGridMapToUse);

			// locID1,locID2
			// Map<HashSet<Integer>,>

			int numOfUniqueLocIDs = latLonLocIDGridCellAllLocs.size();

			for (Pair<Triple<Double, Double, String>, GridCell> eOuter : latLonLocIDGridCellAllLocs)
			{
				double lat = eOuter.getFirst().getFirst();
				double lon = eOuter.getFirst().getSecond();
				Integer locID = Integer.valueOf(eOuter.getFirst().getThird());

				for (Pair<Triple<Double, Double, String>, GridCell> eInner : latLonLocIDGridCellAllLocs)
				{
					double lat2 = eInner.getFirst().getFirst();
					double lon2 = eInner.getFirst().getSecond();
					Integer locID2 = Integer.valueOf(eInner.getFirst().getThird());

					if (locID.equals(locID2) == false)
					{

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
		main1();
	}

	public static void main1()
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

				List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile.readListOfLocationsV2(
						absFileNameForLatLonAllJul10, ",", latColIndex2, lonColIndex2, labelColIndex2);
				System.out.println("listOfLocs.size()= " + listOfLocs.size());

				long t1 = System.currentTimeMillis();
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
