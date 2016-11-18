package org.activity.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.activity.io.WritingToFile;
import org.activity.objects.Timeline;
import org.activity.ui.PopUps;
import org.activity.util.Constant;
import org.activity.util.UtilityBelt;

/**
 * 
 * @author gunjan
 *
 */
public class KCentroidsTimelines
{
	int numOfClusters, numOfIterations, numOfUpdates;
	Cluster clusters[];
	static DistancesMap distancesMap;
	// AlignmentBasedDistance distance;
	double interClusterVariance;
	
	static boolean createDistancesMap = true; // to avoid recreating it redundantly and share it for same datapoints
	
	static final boolean writeResultToFile = false;
	static final boolean trimDataPointsToMinimalHead = false;
	static final boolean trimDataPointsToMinimalTail = false;
	/**
	 * <ID or user id, sequence of symbols as string>
	 */
	ArrayList<DataPoint> dataPoints; // dataPoints to be clustered
	
	public static boolean isCreateDistancesMap()
	{
		return createDistancesMap;
	}
	
	public static void setCreateDistancesMap(boolean createDistancesMap)
	{
		KCentroidsTimelines.createDistancesMap = createDistancesMap;
	}
	
	public ArrayList<Cluster> getListOfClusters()
	{
		ArrayList<Cluster> set = new ArrayList<Cluster>();
		for (int i = 0; i < numOfClusters; i++)
		{
			set.add(clusters[i]);
		}
		return set;
	}
	
	public Set<Cluster> getSetOfClusters()
	{
		Set<Cluster> set = new HashSet<Cluster>();
		for (int i = 0; i < numOfClusters; i++)
		{
			set.add(clusters[i]);
		}
		return set;
	}
	
	/**
	 * ArrayList of avg inter cluster var, inter cluster stddev, intra cluster variance, intra cluster std dev
	 * 
	 * @return
	 */
	public ArrayList<Double> getClusterQualityInfo()
	{
		ArrayList<Double> quality = new ArrayList<Double>();
		quality.add(this.getAvgOfIntraClusterVariances());
		quality.add(this.getAvgOfIntraClusterStandardDeviations());
		quality.add(this.getInterClusterVariance());
		quality.add(this.getInterClusterStandardDeviation());
		
		return quality;
	}
	
	public void initialise(int numberOfClusters, int numOfIterations, LinkedHashMap<String, Timeline> dataPointsGiven)
	{
		System.out.println("trimDataPointsToMinimalHead =" + trimDataPointsToMinimalHead + "\ntrimDataPointsToMinimalTail:"
				+ trimDataPointsToMinimalTail);
		
		// distance = new AlignmentBasedDistance();
		this.numOfClusters = numberOfClusters;
		this.numOfIterations = numOfIterations;
		clusters = new Cluster[numberOfClusters];
		for (int i = 0; i < numOfClusters; i++)
		{
			clusters[i] = new Cluster();
		}
		
		setDataPoints(dataPointsGiven);
		// printDataPoints();
		
		System.out.println("num of clusters =" + numOfClusters);
		System.out.println("num of max iterations =" + numOfIterations);
		System.out.println("seq of symbols =" + this.dataPoints);
		
		if (createDistancesMap)
		{
			distancesMap = new DistancesMap(dataPoints, "HJDistance");
		}
		distancesMap.printDistancesMap();
		// debugging: System.out.println(" Distance between " + dataPoints.get(2).toString() + " and " + dataPoints.get(4).toString() + " ="
		// + distancesMap.getDistanceBetweenDataPoints(dataPoints.get(2), dataPoints.get(4)) + " and equally"
		// + distancesMap.getDistanceBetweenDataPoints(dataPoints.get(4), dataPoints.get(2)));
	}
	
	public KCentroidsTimelines(int numberOfClusters, int numOfIterations, LinkedHashMap<String, Timeline> dataPointsGiven)
	{
		System.out.println("\n\n~~~~~~~~~~Inside KCentroids~~~~~~~~~");
		
		if (dataPointsGiven.size() <= numberOfClusters)
			PopUps.showError("Alert in KCentroids()! sequenceOfSymbols.size() <= numberOfClusters");
		
		initialise(numberOfClusters, numOfIterations, dataPointsGiven);
		
		randomlySeedClusters();
		
		assignDataToClusters();
		
		// System.out.println("----clusters after seeding: ");
		// printClusters();
		System.out.println("--- Starting Iterations ----- ");
		
		int countOfIte = 1;
		while (updateCentroids()) // terminate is the centroid do not change. If the centroids do not change, then there will no reassignment.
		{
			if (countOfIte > numOfIterations)
			{
				System.out.println("Warning! count of iterations " + countOfIte + " exceeds max num of iterations " + numOfIterations);
				PopUps.showMessage("Warning! count of iterations " + countOfIte + " exceeds max num of iterations " + numOfIterations);
				break;
			}
			assignDataToClusters();
			
			System.out.println("---- Iteration = " + countOfIte);
			// printClusters();
			System.out.println("---------");
			
			countOfIte++;
		}
		
		this.numOfUpdates = (countOfIte - 1);
		System.out.println("----After clustering (updating centroids) for -----" + (countOfIte - 1) + " iterations"); // num of times centroids are updated, if (countOfIte - 1) = 2
																														// , this means 2
																														// times the centroid were updated with change and third
																														// time there was no
																														// change in centroids
		printClustersUserwise();
		
		if (writeResultToFile)
			writeResultToFile("KCentroid" + numOfClusters + "ClustersResults");
		
		// System.out.println("seq of centroidse =" + this.indicesOfCentroidsInData);
		System.out.println("~~~~~~~~~~Exiting KCentroids~~~~~~~~~");
	}
	
	public int getNumOfUpdates()
	{
		return numOfUpdates;
	}
	
	@SuppressWarnings("unused")
	public void setDataPoints(LinkedHashMap<String, Timeline> sequenceOfSymbols)
	{
		dataPoints = new ArrayList<DataPoint>();
		
		int minimumLength = Integer.MAX_VALUE;
		// int maximumLength = Integer.MIN_VALUE;
		
		for (Map.Entry<String, Timeline> dataEntry : sequenceOfSymbols.entrySet())
		{
			
			Timeline val = dataEntry.getValue();
			if (trimDataPointsToMinimalHead || trimDataPointsToMinimalTail)
			{
				if (val.size() < minimumLength)
				{
					minimumLength = val.size();
				}
				
				// if (val.length() < maximumLength)
				// {
				// maximumLength = val.length();
				// }
			}
			else
			{
				dataPoints.add(new DataPoint(dataEntry.getKey(), dataEntry.getValue()));
			}
		}
		
		if (trimDataPointsToMinimalHead || trimDataPointsToMinimalTail)
		{
			dataPoints.clear();
			
			for (Map.Entry<String, Timeline> dataEntry : sequenceOfSymbols.entrySet())
			{
				Timeline val = dataEntry.getValue();
				
				if (trimDataPointsToMinimalHead)
				{
					val = new Timeline(val.getActivityObjectsInTimelineFromToIndex(0, minimumLength));
				}
				
				else if (trimDataPointsToMinimalTail)
				{
					val = new Timeline(val.getActivityObjectsInTimelineFromToIndex(val.size() - minimumLength, val.size()));
				}
				DataPoint d = new DataPoint(dataEntry.getKey(), val);
				dataPoints.add(d);
			}
			
		}
		
	}
	
	public int randomlySeedClusters()
	{
		System.out.println("--------Start of random Seeding");
		ArrayList<Integer> indicesOfSeeds = new ArrayList<Integer>();
		Random rn = new Random();
		
		for (int i = 0; i < numOfClusters; i++)
		{
			int ind;
			do
				ind = rn.nextInt(this.dataPoints.size());
			while (indicesOfSeeds.contains(ind) == true);
			
			System.out.println("random index =" + ind);
			
			// clusters[i].addToCluster(dataPoints.get(ind));
			clusters[i].setCentroid(dataPoints.get(ind));
			System.out.println(" random centroid of " + i + "th cluster =" + clusters[i].getCentroid().getLabel().toString());
			// clusters[i].setCentroidInMembers(0);
			
			indicesOfSeeds.add(ind);
		}
		System.out.println("--------End of random Seeding\n");
		// updateIndicesOfCentroidsInData(indicesOfSeeds);
		return 0;
	}
	
	public boolean assignDataToClusters()
	{
		System.out.println("------ Starting assignDataToClusters");
		
		for (int i = 0; i < numOfClusters; i++)
		{
			clusters[i].removeAllMembers();
		}
		
		for (int i = 0; i < dataPoints.size(); i++)
		{
			// if its not a centroid add it to cluster
			// if (indicesOfCentroidsInData.contains(i) == false) // is not a centroid
			// if (alreadyInCluster(dataPoints.get(i)) == false)
			// {
			// System.out.println("data points = " + i);
			addToNearestCluster(dataPoints.get(i));
			// }
		}
		System.out.println("------ Exiting assignDataToClusters\n");
		
		return true;
	}
	
	private void addToNearestCluster(DataPoint object)
	{
		int indexOfNearestCluster = -1;
		double distFromNearestClusterCentroid = Constant.maxForNorm;
		// System.out.println("Inside addToNearestCluster");
		// if (indicesOfCentroids.size() <= 0)
		// {
		// System.err.println("Error in org.activity.clustering.KCentroids.getIndexOfNearestCentroid() indicesOfCentroids.size()=" + indicesOfCentroids.size());
		// }
		// for (int i = 0; i < indicesOfCentroids.size();)
		// {
		// double dist = getDistance(object, sequenceOfSymbols.get(i));
		// if (dist < distFromNearestCentroid)
		// {
		// indexOfNearestCentroid = i;
		// distFromNearestCentroid = dist;
		// }
		// }
		for (int i = 0; i < numOfClusters; i++)
		{
			double dist = distancesMap.getDistanceBetweenDataPoints(object, clusters[i].getCentroid());// getDistance(object, clusters[i].getCentroid());
			
			if (dist < distFromNearestClusterCentroid)
			{
				indexOfNearestCluster = i;
				distFromNearestClusterCentroid = dist;
			}
		}
		System.out.println("  .... added data point [" + object.getLabel() + "] to cluster " + indexOfNearestCluster);
		clusters[indexOfNearestCluster].addToCluster(object);
		// System.out.println("Exiting addToNearestCluster");
	}
	
	public void checkCentroid()
	{
		for (int i = 0; i < numOfClusters; i++)
		{
			if (clusters[i].getCentroid() == null)
			{
				System.out.println("Alert: Centroid of " + i + "th cluster is null");
			}
		}
	}
	
	private boolean updateCentroids()
	{
		boolean anyClusterCentroidChanged = false;
		System.out.println("\n---- Start of updating centroids");
		for (int i = 0; i < numOfClusters; i++)
		{
			System.out.println("updating centroid for " + i + "th cluster");
			
			boolean centroidChanged = clusters[i].updateCentroid(distancesMap);
			
			if (centroidChanged)
			{
				anyClusterCentroidChanged = true;
				// System.out.println("clusters[i].getCentroid()" + (clusters[i].getCentroid() == null));
				System.out.println("\t\tcentroid of " + i + "th cluster changed, new centroid =" + clusters[i].getCentroid().toString());
			}
		}
		
		if (!anyClusterCentroidChanged)
		{
			System.out.println("\t\tNote: no centroid changed");
		}
		System.out.println("---- End of updating centroids\n");
		return anyClusterCentroidChanged;
	}
	
	// private double getDistance(DataPoint a, DataPoint b)
	// {
	// double dist = AlignmentBasedDistance.getMySimpleLevenshteinDistanceWithoutTrace(a.toString(), b.toString(), 1, 1, 2);// .getSecond();
	// return (dist);
	// }
	
	public void printClusters()
	{
		System.out.println("The clusters are: ");
		for (int i = 0; i < numOfClusters; i++)
		{
			System.out.println(clusters[i].toString() + "\n");
			// System.out.println(clusters[i].toStringDataValue() + "\n");
		}
		System.out.println("---xxx-- ");
	}
	
	public void printClustersUserwise()
	{
		System.out.println("The clusters are: ");
		for (int i = 0; i < numOfClusters; i++)
		{
			System.out.println(clusters[i].toStringUserWise());
			// System.out.println(clusters[i].toStringDataValue());// UserWise());
		}
		System.out.println("---xxx-- ");
	}
	
	public void writeResultToFile(String filename)
	{
		StringBuffer s = new StringBuffer();// ("The clusters are: ");
		for (int i = 0; i < numOfClusters; i++)
		{
			s.append(clusters[i].toStringUserWiseToPrint());
		}
		s.append(getAvgOfIntraClusterVariances() + "," + getAvgOfIntraClusterStandardDeviations() + "," + getInterClusterVariance() + ","
				+ getInterClusterStandardDeviation() + "," + this.numOfUpdates);
		WritingToFile.appendLineToFile(s.toString() + "\n", filename);
	}
	
	public void printDataPoints()
	{
		System.out.println("The data points are: ");
		for (int i = 0; i < dataPoints.size(); i++)
		{
			System.out.println("[" + dataPoints.get(i).getLabel() + "]" + dataPoints.get(i).getDataValue());
		}
		System.out.println("---xxx-- ");
	}
	
	// public static void main(String[] args)
	// {
	// Constant.setDatabaseName("geolife1");
	// // ArrayList<DataPoint> data = new ArrayList<DataPoint>();
	// LinkedHashMap<String, String> data2 = new LinkedHashMap<String, String>();
	// // String s[] = { "gunjangunjangunjangunjannnnnnnnnnnnnnnnnnnnnnnnn", "manalillllll", "tessakrolltessatessa" };
	// // String s[] = { "gunjankumar", "manaligaur", "tessakroll" };
	// String s[] = { "gunjankumar", "manaligaur", "gunjanthe", "gunjans", "manaligauris", "tessarolls", "tessakroll", "gunjnkmar", "nalaigaur", "testest" };
	// // String s2[]={"apple","apple2","orange",}
	// Random rn = new Random();
	//
	// for (int i = 0; i < 5; i++)
	// {
	// String name = s[rn.nextInt(10)];// % 3];// rn.nextInt(3)];
	// // String name = s[i % 10];// % 3];// rn.nextInt(3)];
	// // data.add(name + i + i);
	// data2.put(String.valueOf(i), name + i);
	// }
	//
	// KCentroidsTimelines kc = new KCentroidsTimelines(4, 20, data2);
	//
	// }
	
	public static void writeHeaderForResultsFile(String filename, int numOfClusters)
	{
		StringBuffer s = new StringBuffer();// ("The clusters are: ");
		for (int i = 0; i < numOfClusters; i++)
		{
			s.append("Centroid,Members,");
		}
		s.append(
				"AvgIntraClusterVariance, AvgIntraClusterStandardDeviation, InterClusterVariance,InterClusterStandardDeviation,NumberOfTimesAsResult,NumberOfUpdates");
		WritingToFile.appendLineToFile(s.toString() + "\n", filename);
	}
	
	public static void writeToResultsFile(String msg, String filename)
	{
		WritingToFile.appendLineToFile(msg, filename);
	}
	
	public double getAvgOfIntraClusterVariances()
	{
		double res = 0;
		for (int i = 0; i < numOfClusters; i++)
		{
			res += clusters[i].getIntraClusterVariance(distancesMap);
		}
		
		return UtilityBelt.round(res / numOfClusters, 4);
	}
	
	public double getAvgOfIntraClusterStandardDeviations()
	{
		double res = 0;
		for (int i = 0; i < numOfClusters; i++)
		{
			res += clusters[i].getIntraClusterStandardDeviation();
		}
		return UtilityBelt.round(res / numOfClusters, 4);
	}
	
	public double getInterClusterVariance()
	{
		double res = 0;
		int count = 0;
		for (int i = 0; i < numOfClusters; i++)
		{
			for (int j = 0; j < numOfClusters; j++)
			{
				if (i != j)
				{
					double dist = distancesMap.getDistanceBetweenDataPoints(clusters[i].getCentroid(), clusters[j].getCentroid());
					// AlignmentBasedDistance.getMySimpleLevenshteinDistanceWithoutTrace(clusters[i].getCentroid().toString(), clusters[j].getCentroid().toString(), 1, 1, 2);
					res += Math.pow(dist, 2);
					count++;
				}
			}
		}
		System.out.println("Inter cluster variance =" + res + "/" + count);
		res = res / count;
		
		this.interClusterVariance = UtilityBelt.round(res, 4);
		return interClusterVariance;// UtilityBelt.round(res, 4);
	}
	
	public double getInterClusterStandardDeviation()
	{
		// return UtilityBelt.round(Math.sqrt(getInterClusterVariance()), 4);
		return UtilityBelt.round(Math.sqrt(interClusterVariance), 4);
	}
	// public void traverseTimeline(LinkedHashMap<String, Timeline> allDayTimelines)
	// {
	// for (Map.Entry<String, Timeline> entry : allDayTimelines.entrySet())
	// {
	// System.out.println("User ID=" + entry.getKey() + " Timeline id=" + entry.getValue().getTimelineID() + " Num of activity dataPoints = " + entry.getValue().size());
	// }
	// }
	// public void updateIndicesOfCentroidsInData(ArrayList indices)
	// {
	// this.indicesOfCentroidsInData.clear();
	// this.indicesOfCentroidsInData.addAll(indices);
	// }
	// public boolean alreadyInCluster(DataPoint a)
	// {
	// boolean contains = false;
	//
	// for (int i = 0; i < numOfClusters; i++)
	// {
	// if (clusters[i].contains(a))
	// {
	// return true;
	// }
	// }
	// return contains;
	// }
}
