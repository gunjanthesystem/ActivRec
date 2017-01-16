package org.activity.clustering.weka;

import java.time.LocalDateTime;

import org.activity.io.WritingToFile;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * 
 * @author gunjan (modified original version by FracPete available in wekaexamples.clusterers.ClusteringDemo)
 *
 */
public class EMClustering
{
	public EMClustering(String inputAbsoluteFileName, String outputAbsoluteFileName)
	{
		ClusterEvaluation clusterEvaluation;
		Instances dataInstances;

		String[] clusteringOptions;
		String[] evaluationOptions;
		EM EMClusterer;
		double logLikelyhood;

		try
		{
			dataInstances = DataSource.read(inputAbsoluteFileName);

			System.out.println("\n--> Manual Clustering " + LocalDateTime.now().toString());

			EMClusterer = new EM();

			clusteringOptions = new String[2];
			clusteringOptions[0] = "-V";
			EMClusterer.buildClusterer(dataInstances);

			// EMClusterer.setOptions(clusteringOptions);

			clusterEvaluation = new ClusterEvaluation();
			clusterEvaluation.setClusterer(EMClusterer);
			clusterEvaluation.evaluateClusterer(new Instances(dataInstances));
			WritingToFile.appendLineToFileAbsolute(clusterEvaluation.clusterResultsToString(), outputAbsoluteFileName);

		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
