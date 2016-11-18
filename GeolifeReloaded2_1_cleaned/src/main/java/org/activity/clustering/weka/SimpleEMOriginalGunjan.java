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
public class SimpleEMOriginalGunjan
{
	public SimpleEMOriginalGunjan(String inputFileName, String outputPath)
	{
		ClusterEvaluation clusterEvaluation;
		Instances dataInstances;
		String[] evaluationOptions;
		DensityBasedClusterer densityBasedClusterer;
		double logLikelyhood;
		
		try
		{
			dataInstances = DataSource.read(inputFileName);
			
			// // normal
			// System.out.println("\n--> normal");
			// evaluationOptions = new String[2];
			// evaluationOptions[0] = "-t"; // speficy the training file
			// evaluationOptions[1] = inputFileName;
			//
			// // options[2] = "-g"; // graph representation of the clusterer (Only for clusterer that implemented the weka.core.Drawable interface
			// // options[3] = outputPath + "CluterGraph.png";
			//
			// System.out.println(ClusterEvaluation.evaluateClusterer(new EM(), evaluationOptions));
			
			// manual call
			System.out.println("\n--> Manual Clustering " + LocalDateTime.now().toString());
			
			densityBasedClusterer = new EM();
			densityBasedClusterer.buildClusterer(dataInstances);
			
			clusterEvaluation = new ClusterEvaluation();
			clusterEvaluation.setClusterer(densityBasedClusterer);
			clusterEvaluation.evaluateClusterer(new Instances(dataInstances));
			System.out.println(clusterEvaluation.clusterResultsToString());
			
			// // cross-validation for density based clusterers
			// // NB: use MakeDensityBasedClusterer to turn any non-density clusterer
			// // into such.
			// System.out.println("\n--> Cross-validation");
			// densityBasedClusterer = new EM();
			// logLikelyhood = ClusterEvaluation.crossValidateModel(densityBasedClusterer, dataInstances, 10, dataInstances.getRandomNumberGenerator(1));
			// System.out.println("log-likelyhood: " + logLikelyhood);
		}
		
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
