package org.activity.clustering.weka;

import java.util.Random;

import org.activity.ui.PopUps;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class WekaQuickExperiment2019
{

	public static void main(String[] args)
	{
		// may7Gowalla();
		may7Geolife();
	}

	public static void may7Geolife()
	{
		try
		{
			// https://waikato.github.io/weka-wiki/use_weka_in_your_java_code/#filter

			DataSource source = new DataSource(
					"/mnt/sshServers/howitzer/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_MAR19H4M42ED0.5STimeDurDistTrStartGeoEndGeoAvgAltAllActsFDStFilter0hrsFEDPerFS_10F_RTVPNN100NoTTFilterNC/geolife1_MAY7_UsersAbove10RTs_geolife1Feb21Clustering/TimelineFeatureVectors.arff");
			Instances instances = source.getDataSet();
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if (instances.classIndex() == -1)
			{
				instances.setClassIndex(instances.numAttributes() - 1);
			}
			// int initialNumOfInstances = dataInstances.size();

			// Remove rm = new Remove();
			// rm.setAttributeIndices("first");// // remove 1st attribute
			System.out.println("instances = " + instances);
			Instances newData;
			{// remove first attribue: userID
				String[] options = new String[2];
				options[0] = "-R"; // "range"
				options[1] = "1"; // first attribute
				Remove remove = new Remove(); // new instance of filter
				remove.setOptions(options); // set options
				remove.setInputFormat(instances); // inform filter about dataset **AFTER** setting options
				newData = Filter.useFilter(instances, remove);
			}
			System.out.println("newData = " + newData);

			int[] selectedIndices = performWrapperJ48EvalGreedyStepwiseAttributeSelection(newData, 18);
			System.out.println("selectedIndices = " + selectedIndices);
			System.exit(0);

			/////////////////

			AttributeSelection as = new AttributeSelection();
			ASSearch asSearch = ASSearch.forName("weka.attributeSelection.GreedyStepwise",
					new String[] { "-C", "-N", "53" });
			as.setSearch(asSearch);
			ASEvaluation asEval = ASEvaluation.forName("weka.attributeSelection.CfsSubsetEval",
					new String[] { "-M", "-L" });
			as.setEvaluator(asEval);
			as.SelectAttributes(instances);
			instances = as.reduceDimensionality(instances);
			Classifier classifier = AbstractClassifier.forName("weka.classifiers.trees.J48",
					new String[] { "-O", "-J", "-A", "-S", "-M", "3" });
			classifier.buildClassifier(instances);

			// Remove user id

			// System.exit(0);//

			// AttributeSelection as = new AttributeSelection();
			// ASSearch asSearch = ASSearch.forName("weka.attributeSelection.BestFirst",
			// new String[] { "-D", "0", "-N", "7" });
			// as.setSearch(asSearch);
			// ASEvaluation asEval = ASEvaluation.forName("weka.attributeSelection.CfsSubsetEval", new String[] {});
			// as.setEvaluator(asEval);
			// as.SelectAttributes(instances);
			// instances = as.reduceDimensionality(instances);
			// Classifier classifier = AbstractClassifier.forName("weka.classifiers.trees.RandomForest",
			// new String[] { "-I", "10", "-K", "10", "-depth", "4" });
			// classifier.buildClassifier(instances);
			//
			// System.out.println("classifier = " + classifier);
			//
			// String[] options = new String[2];
			// options[0] = "-t";
			// options[1] = "/some/where/somefile.arff";
			// // System.out.println(Evaluation.evaluateModel(new J48(), options))
			//
			// ///////////
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

	/**
	 * Forked from
	 * org.activity.clustering.weka.TimelineWEKAClusteringController2019.performWrapperJ48EvalGreedyStepwiseAttributeSelection(Instances)
	 * 
	 * @param data
	 * @return
	 */
	private static int[] performWrapperJ48EvalGreedyStepwiseAttributeSelection(Instances data, int numOfFolds)
	{
		int[] indicesSelected = null;
		// PopUps.showMessage("Inside performWrapperJ48EvalGreedyStepwiseAttributeSelection");
		System.out.println("Inside performWrapperJ48EvalGreedyStepwiseAttributeSelection");
		System.out.println("Data instances received = " + data.size());
		System.out.println("attributes recieved  = " + data.numAttributes());
		System.out.println("class index already in data instances  = " + data.classIndex());

		AttributeSelection attsel = new AttributeSelection(); // package weka.attributeSelection
		try
		{

			// CfsSubsetEval eval = new CfsSubsetEval();
			/**
			 * Evaluates attribute sets by using a learning scheme. Cross validation is used to estimate the accuracy of
			 * the learning scheme for a set of attributes.
			 **/
			WrapperSubsetEval wrapperEvaluator = new WrapperSubsetEval();
			wrapperEvaluator.setClassifier(new J48());
			// wrapperEvaluator.setFolds(data.size()); // leave 1 out cross validation
			//
			wrapperEvaluator.setEvaluationMeasure(
					new SelectedTag(WrapperSubsetEval.EVAL_AUC, WrapperSubsetEval.TAGS_EVALUATION)); // see the weka src
			wrapperEvaluator.setFolds(numOfFolds);
			// PopUps.showMessage("Wrapper Evaluation Measure :" +
			// wrapperEvaluator.getEvaluationMeasure().toString());// .getOptions().toString());// .globalInfo());
			// PopUps.showMessage("Wrapper technical info :" + wrapperEvaluator.getTechnicalInformation());//
			// .getOptions().toString());// .globalInfo());
			System.out.println("Wrapper Evaluator Options :" + wrapperEvaluator.getOptions().toString());// .globalInfo());
			// wrapperEvaluator.setEvaluationMeasure();
			// PopUps.showMessage("Changed Wrapper Evaluation Measure :" +
			// wrapperEvaluator.getEvaluationMeasure().toString());// .getOptions().toString());// .globalInfo());
			GreedyStepwise search = new GreedyStepwise();
			search.setSearchBackwards(true);
			attsel.setEvaluator(wrapperEvaluator);
			attsel.setSearch(search);
			attsel.SelectAttributes(data);
			indicesSelected = attsel.selectedAttributes();
		}
		catch (Exception e)
		{
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.performCfsSubsetGreedyStepwiseAttributeSelection(Instances)n");
			e.printStackTrace();
		}
		System.out.println("Exiting performWrapperJ48EvalGreedyStepwiseAttributeSelection");
		return indicesSelected;
	}

	public static void may7Gowalla()
	{
		try
		{
			// https://waikato.github.io/weka-wiki/use_weka_in_your_java_code/#filter

			DataSource source = new DataSource(
					"/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/gowalla1_MAR19H3M57ED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrsFEDPerFS_10F_RTVPNN100NCcoll/gowalla1_MAY7_UsersAbove10RTs_gowalla1Feb21ClusteringoldRange/TimelineFeatureVectors.arff");
			Instances instances = source.getDataSet();
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if (instances.classIndex() == -1)
			{
				instances.setClassIndex(instances.numAttributes() - 1);
			}
			// int initialNumOfInstances = dataInstances.size();

			Remove rm = new Remove();
			rm.setAttributeIndices("1");// // remove 1st attribute

			AttributeSelection as = new AttributeSelection();
			ASSearch asSearch = ASSearch.forName("weka.attributeSelection.GreedyStepwise",
					new String[] { "-C", "-N", "53" });
			as.setSearch(asSearch);
			ASEvaluation asEval = ASEvaluation.forName("weka.attributeSelection.CfsSubsetEval",
					new String[] { "-M", "-L" });
			as.setEvaluator(asEval);
			as.SelectAttributes(instances);
			instances = as.reduceDimensionality(instances);
			Classifier classifier = AbstractClassifier.forName("weka.classifiers.trees.J48",
					new String[] { "-O", "-J", "-A", "-S", "-M", "3" });
			classifier.buildClassifier(instances);

			// Remove user id

			// System.exit(0);//

			// AttributeSelection as = new AttributeSelection();
			// ASSearch asSearch = ASSearch.forName("weka.attributeSelection.BestFirst",
			// new String[] { "-D", "0", "-N", "7" });
			// as.setSearch(asSearch);
			// ASEvaluation asEval = ASEvaluation.forName("weka.attributeSelection.CfsSubsetEval", new String[] {});
			// as.setEvaluator(asEval);
			// as.SelectAttributes(instances);
			// instances = as.reduceDimensionality(instances);
			// Classifier classifier = AbstractClassifier.forName("weka.classifiers.trees.RandomForest",
			// new String[] { "-I", "10", "-K", "10", "-depth", "4" });
			// classifier.buildClassifier(instances);
			//
			// System.out.println("classifier = " + classifier);
			//
			// String[] options = new String[2];
			// options[0] = "-t";
			// options[1] = "/some/where/somefile.arff";
			// // System.out.println(Evaluation.evaluateModel(new J48(), options))
			//
			// ///////////
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

	public static void main0(String[] args)
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
