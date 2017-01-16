package org.activity.clustering.weka;

import java.time.LocalDateTime;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DensityBasedClusterer;
import weka.clusterers.EM;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * 
 * @author gunjan (modified original version by FracPete available in wekaexamples.clusterers.ClusteringDemo)
 *
 */
public class Clustering
{
	public Clustering(String inputFileName, String outputPath)
	{
		ClusterEvaluation clusterEvaluation;
		Instances dataInstances;
		String[] evaluationOptions;
		DensityBasedClusterer densityBasedClusterer;
		double logLikelyhood;

		try
		{
			dataInstances = DataSource.read(inputFileName);

			System.out.println("\n--> Manual Clustering " + LocalDateTime.now().toString());

			densityBasedClusterer = new EM();
			densityBasedClusterer.buildClusterer(dataInstances);

			clusterEvaluation = new ClusterEvaluation();
			clusterEvaluation.setClusterer(densityBasedClusterer);
			clusterEvaluation.evaluateClusterer(new Instances(dataInstances));
			System.out.println(clusterEvaluation.clusterResultsToString());
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
