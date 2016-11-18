package org.activity.clustering.weka;

import java.time.LocalDateTime;

import org.activity.io.WritingToFile;
import org.activity.util.weka.WekaUtilityBelt;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * 
 * @author gunjan (modified original version by FracPete available in wekaexamples.clusterers.ClusteringDemo)
 *
 */
public class KMeans
{
	/**
	 * 
	 * @param inputAbsoluteFileName
	 * @param outputAbsoluteFileName
	 * @param numOfClusters
	 */
	public KMeans(String inputAbsoluteFileName, String outputAbsoluteFileName, int numOfClusters)
	{
		ClusterEvaluation clusterEvaluation;
		Instances dataInstances;
		String[] evaluationOptions;
		SimpleKMeans kmeans;
		
		try
		{
			dataInstances = DataSource.read(inputAbsoluteFileName);
			
			dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances, "first");
			System.out.println("\n--> Manual Clustering " + LocalDateTime.now().toString());
			
			kmeans = new SimpleKMeans();
			
			kmeans.setDisplayStdDevs(true);
			kmeans.setNumClusters(numOfClusters);
			kmeans.setPreserveInstancesOrder(true);
			
			kmeans.buildClusterer(dataInstances);
			
			clusterEvaluation = new ClusterEvaluation();
			clusterEvaluation.setClusterer(kmeans);
			clusterEvaluation.evaluateClusterer(new Instances(dataInstances));
			// System.out.println("using distance function: " + kmeans.getDistanceFunction().toString() + "\n");
			
			WritingToFile.appendLineToFileAbsolute(kmeans.globalInfo() + "\n", outputAbsoluteFileName);
			WritingToFile.appendLineToFileAbsolute(clusterEvaluation.clusterResultsToString() + "\n", outputAbsoluteFileName);
			
			int[] assignments = kmeans.getAssignments();
			
			int i = 0;
			for (int clusterNum : assignments)
			{
				WritingToFile.appendLineToFileAbsolute("Instance " + i + ":  User " + (i + 1) + " assigned to Cluster " + clusterNum + " \n", outputAbsoluteFileName);
				i++;
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
