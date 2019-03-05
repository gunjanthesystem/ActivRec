package org.activity.clustering.weka;

import java.util.Random;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaQuickExperiment2019
{

	public static void main(String[] args)
	{
		try
		{
			// https://waikato.github.io/weka-wiki/use_weka_in_your_java_code/#filter

			DataSource source = new DataSource(
					"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/WekaFebExperimentsFOrGowalla/TimelineFeatureVectors.arff");
			Instances instances = source.getDataSet();
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if (instances.classIndex() == -1)
			{
				instances.setClassIndex(instances.numAttributes() - 1);
			}

			AttributeSelection as = new AttributeSelection();
			ASSearch asSearch = ASSearch.forName("weka.attributeSelection.BestFirst",
					new String[] { "-D", "0", "-N", "7" });
			as.setSearch(asSearch);
			ASEvaluation asEval = ASEvaluation.forName("weka.attributeSelection.CfsSubsetEval", new String[] {});
			as.setEvaluator(asEval);
			as.SelectAttributes(instances);
			instances = as.reduceDimensionality(instances);
			Classifier classifier = AbstractClassifier.forName("weka.classifiers.trees.RandomForest",
					new String[] { "-I", "10", "-K", "10", "-depth", "4" });
			classifier.buildClassifier(instances);

			System.out.println("classifier = " + classifier);

			String[] options = new String[2];
			options[0] = "-t";
			options[1] = "/some/where/somefile.arff";
			// System.out.println(Evaluation.evaluateModel(new J48(), options))

			///////////
			String resultToWrite = classifier.toString() + "\n";
			// ------------------------------------------------

			Evaluation eval = new Evaluation(instances);

			// StringBuffer predictionResults = new StringBuffer();
			// CSV predictionsResultPlain = new CSV();
			// predictionsResultPlain.setBuffer(predictionResults);
			// predictionsResultPlain.setAttributes("first-last");
			// predictionsResultPlain.setOutputDistribution(true);

			eval.crossValidateModel(classifier, instances, 10, new Random(1));

			// eval.crossValidateModel(fc, dataInstances, dataInstances.size(), new Random(1),
			// predictionsResultPlain);// ,
			// attsToOutput,
			// outputDist);
			// PopUps.showMessage("Still here 4: After crossvalidation ");

			resultToWrite += "\n---------- Leave one out Cross-validation-------------------\n";
			resultToWrite += WekaUtilityBelt.getRelevantEvaluationMetricsAsString(eval);

			resultToWrite += eval.toSummaryString();
			// resultToWrite += "\n Tree when run on all data: \n " + tree.graph();
			System.out.println("resultToWrite = \n" + resultToWrite);
			// resultToWrite += "\n" + predictionResults.toString() + "\n";
			////////////

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
