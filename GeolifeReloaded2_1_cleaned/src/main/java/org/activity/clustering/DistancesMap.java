package org.activity.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.activity.distances.AlignmentBasedDistance;
import org.activity.distances.HJEditDistance;
import org.activity.objects.Pair;
import org.activity.util.UtilityBelt;

/**
 * 
 * @author gunjan
 *
 */
public class DistancesMap
{
	/**
	 * Pair<TimelineID1,TimelineID2>,Distance between them
	 */
	HashMap<Pair<String, String>, Double> distances;
	ArrayList<DataPoint> dataPoints;
	AlignmentBasedDistance alignmentBasedDistance;
	String typeOfDistance;
	String distanceToUse;

	public DistancesMap(ArrayList<DataPoint> dataPoints, String distanceToUse)// String typeOfDistance)
	{
		// this.typeOfDistance = typeOfDistance;
		this.dataPoints = dataPoints;
		this.distanceToUse = distanceToUse;
		System.out.println("distance to use=" + distanceToUse);
		alignmentBasedDistance = new AlignmentBasedDistance();
		setDistancesMap();
	}

	public void setDistancesMap()
	{
		if (dataPoints == null)
		{
			new Exception("Error in setDistancesMap: datapoints are null").printStackTrace();
			System.err.println("Error in setDistancesMap: datapoints are null");
			System.exit(-1);
		}

		distances = new HashMap<Pair<String, String>, Double>();

		for (int i = 0; i < dataPoints.size(); i++)
		{
			for (int j = 0; j < dataPoints.size(); j++) // we can also start from j=i if the distance is symmetrical,
														// but then when searching in the map will compare pairs using
														// equalsIgnoreCase()
														// instead of equals()
			{
				if (i != j)
				{
					DataPoint dataPointA = dataPoints.get(i);
					DataPoint dataPointB = dataPoints.get(j);

					Pair<String, String> pairOfIds = new Pair(dataPointA.getLabel().toString(),
							dataPointB.getLabel().toString());
					distances.put(pairOfIds, computeDistancesBetweenDataPoints(dataPointA, dataPointB));
				}
			}
		}
	}

	public double computeDistancesBetweenDataPoints(DataPoint A, DataPoint B)
	{
		switch (distanceToUse)
		{
		case "HJDistance":
			Pair<String, Double> dist = (new HJEditDistance()).getHJEditDistanceWithTrace(
					A.toTimeline().getActivityObjectsInTimeline(), B.toTimeline().getActivityObjectsInTimeline(), "",
					"", "", "0");// 0);
			// .(A.getDataValue().toString(), B.getDataValue().toString(), 1, 1, 2);
			return dist.getSecond();

		case "SimpleLevenshtein":
			return AlignmentBasedDistance.getMySimpleLevenshteinDistanceWithoutTrace(A.getDataValue().toString(),
					B.getDataValue().toString(), 1, 1, 2);

		default:
			UtilityBelt.showErrorExceptionPopup(
					"Unknown distance to use in org.activity.clustering.DistancesMap.computeDistancesBetweenDataPoints()");
			return (Double) null;
		}
	}

	public void printDistancesMap()
	{
		System.out.println("----- Printing Distances Map--------\n");
		for (Map.Entry<Pair<String, String>, Double> entry : this.distances.entrySet())
		{
			System.out.println("Distance between: " + entry.getKey().toString() + " = " + entry.getValue());
		}
		System.out.println("------------------------------------\n");
	}

	public Double getDistanceBetweenDataPoints(DataPoint dataPointA, DataPoint dataPointB)
	{
		Pair<String, String> pairOfIds = new Pair<String, String>(dataPointA.getLabel().toString(),
				dataPointB.getLabel().toString());
		Double res = distances.get(pairOfIds);

		if (dataPointA.equals(dataPointB))
		{
			return new Double(0);
		}

		if (res == null)
		{
			System.err.println(
					"Error in org.activity.clustering.DistancesMap.getDistanceBetweenDataPoints() given data points:"
							+ dataPointA.getLabel().toString() + " and " + dataPointB.getLabel().toString()
							+ " NOT IN THE DISTANCES MAP");
		}
		return res;
	}
}
