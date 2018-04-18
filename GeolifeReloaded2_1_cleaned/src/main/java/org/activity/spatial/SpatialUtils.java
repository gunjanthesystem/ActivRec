package org.activity.spatial;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.PathConstants;
import org.activity.constants.SanityConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.LocationGowalla;
import org.activity.objects.LocationSlim;
import org.activity.objects.Pair;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.apache.commons.math3.util.FastMath;

public final class SpatialUtils
{

	private SpatialUtils()
	{
	}

	/**
	 * 
	 * @param pathToReadUniqueLocs
	 * @param commonPath
	 */
	public static void spatialDistanceDatabaseController(String pathToReadUniqueLocs, String commonPath)
	{

		try
		{
			long tt1 = System.currentTimeMillis();
			Pair<Map<Long, LocationSlim>, Map<Long, Map<Long, Double>>> res = createSpatialDistanceDatabase(
					pathToReadUniqueLocs);
			long tt2 = System.currentTimeMillis();
			System.out.println("createSpatialDistanceDatabase took " + (1.0 * (tt2 - tt1)) / 1000 + " secs");

			Map<Long, Map<Long, Double>> allLocDists = res.getSecond();
			WToFile.writeMapOfMap(allLocDists, "loc1ID,loc2ID,DistInM\n", commonPath + "allLocDists.csv");

			long tt3 = System.currentTimeMillis();

			System.out.println("writing took " + (1.0 * (tt3 - tt2)) / 1000 + " secs");
			Serializer.kryoSerializeThis(res.getFirst(), commonPath + "uniqLocs.kry");
			Serializer.kryoSerializeThis(allLocDists, commonPath + "allLocDists.kry");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param pathToReadUniqueLocs
	 * @param commonPath
	 */
	public static void spatialDistanceDatabaseController2(String pathToReadUniqueLocs, String commonPath)
	{

		try
		{
			long tt1 = System.currentTimeMillis();
			Pair<Map<Long, LocationSlim>, Map<Long, Map<Integer, Pair<Long, Double>>>> res = createSpatialDistanceDatabase2(
					pathToReadUniqueLocs);
			long tt2 = System.currentTimeMillis();
			System.out.println("createSpatialDistanceDatabase took " + (1.0 * (tt2 - tt1)) / 1000 + " secs");

			Map<Long, Map<Integer, Pair<Long, Double>>> allLocNearestForEachActIDDists = res.getSecond();
			// write(allLocNearestForEachActIDDists, commonPath + "allLocNearestForEachActIDDists.csv");
			WToFile.writeMapOfMap(allLocNearestForEachActIDDists,
					"loc1ID,ActD,(NearestLoc2ID,NearestLoc2DistInM)\n",
					commonPath + "allLocNearestForEachActIDDists.csv");

			long tt3 = System.currentTimeMillis();

			System.out.println("writing took " + (1.0 * (tt3 - tt2)) / 1000 + " secs");
			Serializer.kryoSerializeThis(res.getFirst(), commonPath + "uniqLocs.kry");
			Serializer.kryoSerializeThis(allLocNearestForEachActIDDists,
					commonPath + "allLocNearestForEachActIDDists.kry");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// private static void write(Map<Long, Pair<Long, Double>[]> allLocNearestForEachActIDDists, String
	// absFileNameToWrite)
	// {
	//
	// try
	// {
	// WritingToFile.writeToNewFile("loc1ID,ActID,NearestLoc2ID,DistNearestLoc2ID\n", absFileNameToWrite);
	//
	// for (Entry<Long, Pair<Long, Double>[]> e : allLocNearestForEachActIDDists.entrySet())
	// {
	// StringBuilder sb = new StringBuilder();
	// String loc1ID = e.getKey().toString();
	//
	// for(int i=0;i<
	// {
	//
	// }
	//
	// }
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }

	/**
	 * 
	 * @param pathToReadUniqueLocs
	 * @return
	 */
	public static Pair<Map<Long, LocationSlim>, Map<Long, Map<Long, Double>>> createSpatialDistanceDatabase(
			String pathToReadUniqueLocs)
	{
		Map<Long, Map<Long, Double>> allLocDists = new TreeMap<>();
		Map<Long, LocationSlim> uniqLocs = new TreeMap<>();
		long count = 0;
		try
		{
			Set<LocationSlim> locsForDist = createLocationSlimObjects(pathToReadUniqueLocs);

			locsForDist.stream().forEachOrdered(e -> uniqLocs.put(e.getLocID(), e));

			System.out.println("locsForDist.size()=" + locsForDist.size());
			System.out.println("uniqLocs.size()=" + uniqLocs.size());

			for (LocationSlim loc1 : locsForDist)
			{
				Map<Long, Double> locDists = new TreeMap<>();
				for (LocationSlim loc2 : locsForDist)
				{
					if (loc1.getLocID() != loc2.getLocID())
					{
						count++;
						double dist = SpatialUtils.haversineFastMathV3NoRound(loc1.getLatitude(), loc1.getLongitude(),
								loc2.getLatitude(), loc2.getLongitude());
						locDists.put(loc2.getLocID(), dist);
						if ((count % 50000) == 0)
						{
							System.out.println(
									"count of haversines computed = " + count + " locDists.size=" + locDists.size());
						}
					}
				}
				allLocDists.put(loc1.getLocID(), ComparatorUtils.sortByValueAscending(locDists));
				// if (true)// temporary
				// {
				// if ((count > 1000000))
				// {
				// break;
				// }
				// }
				//
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new Pair<>(uniqLocs, allLocDists);

	}

	/**
	 * 
	 * @param pathToReadUniqueLocs
	 * @return
	 * @return
	 */
	public static Pair<Map<Long, LocationSlim>, Map<Long, Map<Integer, Pair<Long, Double>>>> createSpatialDistanceDatabase2(
			String pathToReadUniqueLocs)
	{
		// loc1, list of 134 one for each act { {nearestLocIDForAct1,distance},{nearestLocIDForAct2,distance}}
		Map<Long, Map<Integer, Pair<Long, Double>>> allLocDists = new TreeMap<>();
		Map<Long, LocationSlim> uniqLocs = new TreeMap<>();

		Set<LocationSlim> locsForDist;
		ArrayList<Integer> uniqActIDs;

		long count = 0;

		try
		{
			locsForDist = createLocationSlimObjects(pathToReadUniqueLocs);
			uniqActIDs = locsForDist.stream().map(l -> (int) l.getActID())
					.collect(Collectors.toCollection(ArrayList::new));

			locsForDist.stream().forEachOrdered(e -> uniqLocs.put(e.getLocID(), e));

			System.out.println("locsForDist.size()=" + locsForDist.size());
			System.out.println("uniqLocs.size()=" + uniqLocs.size());
			System.out.println("uniqActIDs.size()=" + uniqActIDs.size());

			int loc1Count = 0;
			for (LocationSlim loc1 : locsForDist)
			{
				Map<Integer, Pair<Long, Double>> nearestLocForEachActID = new TreeMap<>();
				System.out.println("loc1Count=" + (++loc1Count));
				// if (loc1Count > 100)
				// {
				// break;
				// }

				// Map<Long, Double> locDists = new TreeMap<>();
				int actCount = 0;
				for (Integer actID : uniqActIDs)
				{
					// if (++actCount > 10)
					// {
					// break;
					// }
					Pair<Long, Double> lowestDistLoc2 = new Pair<Long, Double>(new Long(-9999), Double.MAX_VALUE);

					for (LocationSlim loc2 : locsForDist)
					{
						if ((loc2.getActID() == actID) && (loc1.getLocID() != loc2.getLocID()))
						{
							count++;
							double dist = SpatialUtils.haversineFastMathV3NoRound(loc1.getLatitude(),
									loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());

							if (dist < lowestDistLoc2.getSecond())
							{
								lowestDistLoc2 = new Pair<>(loc2.getLocID(), dist);
							}
							if ((count % 500000) == 0)
							{
								System.out.println("count of haversines computed = " + count);
							}
						}
					}
					nearestLocForEachActID.put(actID, lowestDistLoc2);
				}
				allLocDists.put(loc1.getLocID(), nearestLocForEachActID);
			}
		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}
		return new Pair<>(uniqLocs, allLocDists);

	}

	/**
	 * 
	 * @param pathToReadUniqueLocs
	 * @return
	 */
	private static Set<LocationSlim> createLocationSlimObjects(String pathToReadUniqueLocs)
	{
		Set<LocationSlim> locsForDist = new TreeSet<>();
		try
		{
			List<List<String>> allLines = ReadingFromFile.readLinesIntoListOfLists(pathToReadUniqueLocs, ",");
			System.out.println(allLines.get(1));
			locsForDist = allLines.stream().skip(1).map(l -> new LocationSlim(l.get(9), l.get(10), l.get(0), l.get(13)))
					.collect(Collectors.toSet());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return locsForDist;
	}

	// spatialDistanceDatabaseController
	public static void main(String args[])
	{
		// compareHaversines();
		// spatialDistanceDatabaseController(
		// "/run/media/gunjan/BufferVault/GowallaResults/Feb22/UniqueLocationObjects5DaysTrainTest.csv",
		// "/run/media/gunjan/BufferVault/GowallaResults/Feb23/");

		spatialDistanceDatabaseController2("./dataWritten/Feb22/UniqueLocationObjects5DaysTrainTest.csv",
				"./dataWritten/Feb23/");
	}

	public static void compareHaversines()
	{
		// getGivenLevelCatIDForAllCatIDs(pathToSerialisedLevelWiseCatIDsDict, 1, true);
		PathConstants.intialise(Constant.For9kUsers);
		LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> locIDsHaversineDists = computeHaversineDistanceBetweenAllLocIDs(
				PathConstants.pathToSerialisedLocationObjects);

		// StringBuilder sb = new StringBuilder();
		// for (Entry<Integer, LinkedHashMap<Integer, Double>> e1 : locIDsHaversineDists.entrySet())
		// {
		// sb.append(e1.getKey() + ",");
		// for (Entry<Integer, Double> e2 : e1.getValue().entrySet())
		// {
		// sb.append(e2.getKey() + "," + e2.getValue() + "\n");
		// }
		// }
		// WritingToFile.appendLineToFileAbsolute(sb.toString(),
		// "./dataWritten/locationDistances/locationDistances.csv");
	}

	/**
	 * 
	 * @param pathToLocObjects
	 * @return
	 */
	public static LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> computeHaversineDistanceBetweenAllLocIDs(
			String pathToLocObjects)
	{
		LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> locIDsHaversineDists = new LinkedHashMap<>();
		try
		{
			LinkedHashMap<Integer, LocationGowalla> locObjs = (LinkedHashMap<Integer, LocationGowalla>) Serializer
					.kryoDeSerializeThis(PathConstants.pathToSerialisedLocationObjects);

			// TreeSet<Integer> uniqueLocIDsInCleanedTimelines = (TreeSet<Integer>) Serializer
			// .deSerializeThis(pathToSerialisedUniqueLocIDsInCleanedTimelines);

			// TreeSet<Integer> uniqueLocIDsInCleanedTimelines = (TreeSet<Integer>) Serializer
			// .deSerializeThis("./dataToRead/July12/UniqueLocIDsInCleanedTimeines.ser");
			TreeSet<Integer> uniqueLocIDsInCleanedTimelines = (TreeSet<Integer>) Serializer
					.kryoDeSerializeThis(PathConstants.pathToSerialisedUniqueLocIDsInCleanedTimelines);

			System.out.println("Num of unique loc ids = " + uniqueLocIDsInCleanedTimelines.size());
			long t1 = System.currentTimeMillis();
			int count = 0, numOfComparisons = 0;

			double sumOfTimeTakenByF1 = 0, sumOfTimeTakenByF2 = 0, sumOfTimeTakenByF3 = 0, sumOfTimeTakenByF4 = 0,
					sumOfTimeTakenByF5 = 0;

			for (Integer locID1 : uniqueLocIDsInCleanedTimelines)
			{
				LocationGowalla loc1 = locObjs.get(locID1);
				LinkedHashMap<Integer, Double> distMapForLocID1 = new LinkedHashMap<>(
						uniqueLocIDsInCleanedTimelines.size());
				System.out.println(count++ + "-locID1 = " + locID1);

				for (Integer locID2 : uniqueLocIDsInCleanedTimelines)
				{
					LocationGowalla loc2 = locObjs.get(locID2);
					numOfComparisons++;

					long pt1 = System.currentTimeMillis();
					double haversineDist = SpatialUtils.haversine(loc1.getLatitude(), loc1.getLongitude(),
							loc2.getLatitude(), loc2.getLongitude());
					long pt2 = System.currentTimeMillis();
					sumOfTimeTakenByF1 += (pt2 - pt1);

					double haversineDist2 = SpatialUtils.haversineFasterV1(loc1.getLatitude(), loc1.getLongitude(),
							loc2.getLatitude(), loc2.getLongitude());
					long pt3 = System.currentTimeMillis();
					sumOfTimeTakenByF2 += (pt3 - pt2);

					double haversineDist3 = SpatialUtils.haversineFastMathV2(loc1.getLatitude(), loc1.getLongitude(),
							loc2.getLatitude(), loc2.getLongitude());
					long pt4 = System.currentTimeMillis();
					sumOfTimeTakenByF3 += (pt4 - pt3);

					double haversineDist4 = SpatialUtils.haversineFastMathV2NoRound(loc1.getLatitude(),
							loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());
					long pt5 = System.currentTimeMillis();
					sumOfTimeTakenByF4 += (pt5 - pt4);

					double haversineDist5 = SpatialUtils.haversineFastMathV3NoRound(loc1.getLatitude(),
							loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());
					long pt6 = System.currentTimeMillis();
					sumOfTimeTakenByF5 += (pt6 - pt5);

					// System.out.println("\nhaversineDist=" + haversineDist + "\nhaversineDist2=" + haversineDist2
					// + "\nhaversineDist3=" + haversineDist3);
					Sanity.eq(haversineDist, haversineDist2, haversineDist3, "haverfunctions are giving diff results");
					Sanity.eq(haversineDist4, haversineDist5, "haverfunctions are giving diff results");
					System.out.println(haversineDist + " , " + haversineDist2 + " , " + haversineDist3 + " , "
							+ haversineDist4 + " , " + haversineDist5);

					System.exit(0);
					distMapForLocID1.put(locID2, haversineDist);
				}
				locIDsHaversineDists.put(locID1, distMapForLocID1);

				if (count > 10)
				{
					break;
				}
			}
			long t2 = System.currentTimeMillis();
			System.out.println(
					"#comparisons = " + (numOfComparisons) + "\nsumOfTimeTakenByF1= " + sumOfTimeTakenByF1 / 1000);
			System.out.println("sumOfTimeTakenByF2= " + sumOfTimeTakenByF2 / 1000);
			System.out.println("sumOfTimeTakenByF3= " + sumOfTimeTakenByF3 / 1000);
			System.out.println("sumOfTimeTakenByF4= " + sumOfTimeTakenByF4 / 1000);
			System.out.println("sumOfTimeTakenByF5= " + sumOfTimeTakenByF5 / 1000);
			System.out
					.println("computeHaversineDistanceBetweenAllLocIDs took: " + (((t2 - t1) * 1.0) / 1000) + " secs");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return locIDsHaversineDists;
	}

	// Num of unique loc ids = 901178
	// 0locID1 = 8904
	// 1locID1 = 8932
	// 2locID1 = 8936
	// 3locID1 = 8938
	// 4locID1 = 8947
	// 5locID1 = 8956
	// 6locID1 = 8957
	// 7locID1 = 8964
	// 8locID1 = 8965
	// 9locID1 = 8966
	// 10locID1 = 8968
	// #comparisons = 9912958
	// sumOfTimeTakenByF1= 23.016
	// sumOfTimeTakenByF2= 15.822
	// sumOfTimeTakenByF3= 11.394
	// computeHaversineDistanceBetweenAllLocIDs took: 52.431 secs

	/**
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * TODO LATER can use non-native math libraries for faster computation. User jafama or apache common maths.</br>
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversine(String lat1s, String lon1s, String lat2s, String lon2s)
	{

		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);

		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);

		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + ","
					+ lon1s + "  " + lat2s + "," + lon2s);
			if (SanityConstants.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided "
						+ lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);

		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range:
		// provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String,
		// String)");
		// }

		double c = 2 * Math.asin(Math.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);

		if (SanityConstants.checkForDistanceTravelledAnomaly
				&& (StatsUtils.radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + StatsUtils.radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s
					+ "," + lon2s);
		}

		return StatsUtils.round(StatsUtils.radiusOfEarthInKMs * c, 4);
	}

	/**
	 * Fork of haversine() to improve speed
	 * 
	 * <p>
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * TODO LATER can use non-native math libraries for faster computation. User jafama or apache common maths.</br>
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversineFasterV1(String lat1s, String lon1s, String lat2s, String lon2s)
	{

		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);

		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);

		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			PopUps.printTracedErrorMsg("Possible Error in haversin: latitude and/or longitude outside range: provided "
					+ lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
			return Constant.unknownDistanceTravelled;
		}

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);

		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range:
		// provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String,
		// String)");
		// }
		double c = 2 * Math.asin(Math.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);
		return StatsUtils.round(StatsUtils.radiusOfEarthInKMs * c, 4);
	}

	/**
	 * 
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * uses FastMath from apache common maths as drop in replacement for java's standard Math.</br>
	 * 
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversineFastMath(String lat1s, String lon1s, String lat2s, String lon2s)
	{

		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);

		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);

		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (FastMath.abs(lat1) > 90 || FastMath.abs(lat2) > 90 || FastMath.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + ","
					+ lon1s + "  " + lat2s + "," + lon2s);
			if (SanityConstants.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided "
						+ lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}

		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLon = FastMath.toRadians(lon2 - lon1);
		lat1 = FastMath.toRadians(lat1);
		lat2 = FastMath.toRadians(lat2);

		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);

		double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2)
				+ FastMath.sin(dLon / 2) * FastMath.sin(dLon / 2) * FastMath.cos(lat1) * FastMath.cos(lat2);

		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range:
		// provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String,
		// String)");
		// }

		double c = 2 * FastMath.asin(FastMath.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);

		if (SanityConstants.checkForDistanceTravelledAnomaly
				&& (StatsUtils.radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + StatsUtils.radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s
					+ "," + lon2s);
		}

		return StatsUtils.round(StatsUtils.radiusOfEarthInKMs * c, 4);
	}

	/**
	 * 
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * uses FastMath from apache common maths as drop in replacement for java's standard Math.</br>
	 * 
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversineFastMathV2(String lat1s, String lon1s, String lat2s, String lon2s)
	{

		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);

		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);

		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (FastMath.abs(lat1) > 90 || FastMath.abs(lat2) > 90 || FastMath.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			PopUps.printTracedErrorMsg("Possible Error in haversin: latitude and/or longitude outside range: provided "
					+ lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}

		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLon = FastMath.toRadians(lon2 - lon1);
		lat1 = FastMath.toRadians(lat1);
		lat2 = FastMath.toRadians(lat2);

		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);

		double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2)
				+ FastMath.sin(dLon / 2) * FastMath.sin(dLon / 2) * FastMath.cos(lat1) * FastMath.cos(lat2);

		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range:
		// provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String,
		// String)");
		// }

		double c = 2 * FastMath.asin(FastMath.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);
		//
		// if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		// {
		// System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
		// + " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + " " + lat2s
		// + "," + lon2s);
		// }

		return StatsUtils.round(StatsUtils.radiusOfEarthInKMs * c, 4);
	}

	/**
	 * Fork of haversineFastMathV2 with no rounding
	 * <p>
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * uses FastMath from apache common maths as drop in replacement for java's standard Math.</br>
	 * 
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversineFastMathV2NoRound(String lat1s, String lon1s, String lat2s, String lon2s)
	{
		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);
		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);
		return haversineFastMathV2NoRound(lat1, lon1, lat2, lon2);
	}

	public static double haversineFastMathV3NoRound(String lat1s, String lon1s, String lat2s, String lon2s)
	{
		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);
		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);
		return haversineFastMathV3NoRound(lat1, lon1, lat2, lon2);
	}

	/**
	 * Fork of haversineFastMathV2 with no rounding
	 * <p>
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * uses FastMath from apache common maths as drop in replacement for java's standard Math.</br>
	 * 
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversineFastMathV2NoRound(double lat1, double lon1, double lat2, double lon2)
	{

		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (FastMath.abs(lat1) > 90 || FastMath.abs(lat2) > 90 || FastMath.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			PopUps.printTracedErrorMsg("Possible Error in haversin: latitude and/or longitude outside range: provided "
					+ lat1 + "," + lon1 + "  " + lat2 + "," + lon2);
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}

		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLon = FastMath.toRadians(lon2 - lon1);
		lat1 = FastMath.toRadians(lat1);
		lat2 = FastMath.toRadians(lat2);

		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);

		double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2)
				+ FastMath.sin(dLon / 2) * FastMath.sin(dLon / 2) * FastMath.cos(lat1) * FastMath.cos(lat2);

		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range:
		// provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String,
		// String)");
		// }

		double c = 2 * FastMath.asin(FastMath.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);
		//
		// if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		// {
		// System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
		// + " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + " " + lat2s
		// + "," + lon2s);
		// }

		return (StatsUtils.radiusOfEarthInKMs * c);
	}

	/**
	 * Fork of haversineFastMathV2 with no rounding
	 * <p>
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * uses FastMath from apache common maths as drop in replacement for java's standard Math.</br>
	 * 
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversineFastMathV3NoRound(double lat1, double lon1, double lat2, double lon2)
	{

		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (FastMath.abs(lat1) > 90 || FastMath.abs(lat2) > 90 || FastMath.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			PopUps.printTracedErrorMsg("Possible Error in haversin: latitude and/or longitude outside range: provided "
					+ lat1 + "," + lon1 + "  " + lat2 + "," + lon2);
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}

		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLon = FastMath.toRadians(lon2 - lon1);
		lat1 = FastMath.toRadians(lat1);
		lat2 = FastMath.toRadians(lat2);

		double dLatBy2Sin = FastMath.sin(dLat / 2);
		double dLonBy2Sin = FastMath.sin(dLon / 2);

		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);

		double a = dLatBy2Sin * dLatBy2Sin + dLonBy2Sin * dLonBy2Sin * FastMath.cos(lat1) * FastMath.cos(lat2);

		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range:
		// provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String,
		// String)");
		// }

		double c = 2 * FastMath.asin(FastMath.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);
		//
		// if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		// {
		// System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
		// + " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + " " + lat2s
		// + "," + lon2s);
		// }

		return (StatsUtils.radiusOfEarthInKMs * c);
	}

	/**
	 * 
	 * @param lat1s
	 * @param lon1s
	 * @param lat2s
	 * @param lon2s
	 * @param roundTheResult
	 * @return
	 */
	public static double haversine(String lat1s, String lon1s, String lat2s, String lon2s, boolean roundTheResult)
	{

		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);

		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);

		if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + ","
					+ lon1s + "  " + lat2s + "," + lon2s);

			if (SanityConstants.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided "
						+ lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;
			// System.exit(-1);

		}

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));

		if (SanityConstants.checkForDistanceTravelledAnomaly
				&& (StatsUtils.radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + StatsUtils.radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s
					+ "," + lon2s);
		}

		if (roundTheResult)
		{
			return StatsUtils.round(StatsUtils.radiusOfEarthInKMs * c, 4);
		}

		else
		{
			return (StatsUtils.radiusOfEarthInKMs * c);
		}

	}

	/**
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversine(double lat1, double lon1, double lat2, double lon2)
	{
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return StatsUtils.radiusOfEarthInKMs * c;
	}

	/**
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static double haversineFastMath(double lat1, double lon1, double lat2, double lon2)
	{
		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLon = FastMath.toRadians(lon2 - lon1);
		lat1 = FastMath.toRadians(lat1);
		lat2 = FastMath.toRadians(lat2);

		double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2)
				+ FastMath.sin(dLon / 2) * FastMath.sin(dLon / 2) * FastMath.cos(lat1) * FastMath.cos(lat2);
		double c = 2 * FastMath.asin(FastMath.sqrt(a));
		return StatsUtils.radiusOfEarthInKMs * c;
	}

	public static void createLocationDistanceDatabase(
			LinkedHashMap<Integer, LocationGowalla> locIDLocationObjectDictionary)
	{

	}

}
/////////////////////////////////////////////////////////////////
/// **
// *
// * @param pathToLocObjects
// * @return
// */
// public static LinkedHashMap<Set<Integer>, Double> computeHaversineDistanceBetweenAllLocIDsV2(
// String pathToLocObjects)
// {
// LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> locIDsHaversineDists = new LinkedHashMap<>();
// try
// {
// LinkedHashMap<Integer, LocationGowalla> locObjs = (LinkedHashMap<Integer, LocationGowalla>) Serializer
// .kryoDeSerializeThis(pathToSerialisedLocationObjects);
//
// Set<Integer> setOfLocIDs = locObjs.keySet();
// System.out.println("Num of unique loc ids = " + setOfLocIDs.size());
// long t1 = System.currentTimeMillis();
// int count = 0, numOfComparisons = 0;
//
// double sumOfTimeTakenByF1 = 0, sumOfTimeTakenByF2 = 0, sumOfTimeTakenByF3 = 0;
// for (Integer locID1 : setOfLocIDs)
// {
// LocationGowalla loc1 = locObjs.get(locID1);
// LinkedHashMap<Integer, Double> distMapForLocID1 = new LinkedHashMap<>(setOfLocIDs.size());
// System.out.println(count++ + "locID1 = " + locID1);
// for (Integer locID2 : setOfLocIDs)
// {
// LocationGowalla loc2 = locObjs.get(locID2);
// numOfComparisons++;
//
// long pt1 = System.currentTimeMillis();
// double haversineDist = StatsUtils.haversine(loc1.getLatitude(), loc1.getLongitude(),
// loc2.getLatitude(), loc2.getLongitude());
// long pt2 = System.currentTimeMillis();
// sumOfTimeTakenByF1 += (pt2 - pt1);
//
// double haversineDist2 = StatsUtils.haversineFasterV1(loc1.getLatitude(), loc1.getLongitude(),
// loc2.getLatitude(), loc2.getLongitude());
// long pt3 = System.currentTimeMillis();
// sumOfTimeTakenByF2 += (pt3 - pt2);
//
// double haversineDist3 = StatsUtils.haversineFastMathV2(loc1.getLatitude(), loc1.getLongitude(),
// loc2.getLatitude(), loc2.getLongitude());
// long pt4 = System.currentTimeMillis();
// sumOfTimeTakenByF3 += (pt4 - pt3);
//
// // System.out.println("\nhaversineDist=" + haversineDist + "\nhaversineDist2=" + haversineDist2
// // + "\nhaversineDist3=" + haversineDist3);
// Sanity.eq(haversineDist, haversineDist2, haversineDist3, "haverfunctions are giving diff results");
//
// distMapForLocID1.put(locID2, haversineDist);
// }
// locIDsHaversineDists.put(locID1, distMapForLocID1);
//
// if (count > 2)
// {
// break;
// }
// }
// long t2 = System.currentTimeMillis();
// System.out.println(
// "#comparisons = " + (numOfComparisons) + "\nsumOfTimeTakenByF1= " + sumOfTimeTakenByF1 / 1000);
// System.out.println("sumOfTimeTakenByF2= " + sumOfTimeTakenByF2 / 1000);
// System.out.println("sumOfTimeTakenByF3= " + sumOfTimeTakenByF3 / 1000);
// System.out
// .println("computeHaversineDistanceBetweenAllLocIDs took: " + (((t2 - t1) * 1.0) / 1000) + " secs");
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
// return locIDsHaversineDists;
// }
