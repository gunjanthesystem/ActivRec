package org.activity.clustering.weka;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.activity.constants.Constant;
import org.activity.featureExtraction.TimelinesAttributesExtractionCleaned22Feb2019;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.output.prediction.CSV;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

/**
 * Fork of TimelineWEKAClusteringController
 * <p>
 * For clustering or classification of users based on attributes extracted from their timelines
 * 
 * @author gunjan
 * @since 21 Feb 2019
 */
public class TimelineWEKAClusteringController2019
{
	private String pathToWrite;
	private Classifier usedClassifier;
	private TimelinesAttributesExtractionCleaned22Feb2019 attributeExtraction;
	// private Instances allInstances;
	/**
	 * (raw user id, predicted class)
	 */
	private LinkedHashMap<String, String> userIDPredictedClass;

	/**
	 * (user id as instance id, predicted class)
	 */
	private LinkedHashMap<Integer, String> userAsInstanceIDPredictedClass;

	// private LinkedHashMap<String, List<String>> userIDPredictedClassProb;
	private LinkedHashMap<String, List<Double>> userIDPredictedClassProbInt; // here class is numeric 1 for First
																				// cluster, 2 for Third CLuster and 3
																				// for Second Cluster

	private final String attributeGroups[] = { "SampEn2", "SampEn3", "KGram" };

	// final Pair<String, String> groundTruth1 = new Pair<String, String>("Clusters0Min",
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb8/Mod0/CountsForClusterLabelAccToMinMUHavMaxMRR.csv");
	// final Pair<String, String> groundTruth2 = new Pair<String, String>("Clusters1Min",
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb8/Mod1/CountsForClusterLabelAccToMinMUHavMaxMRR.csv");
	// final Pair<String, String> groundTruth3 = new Pair<String, String>("Clusters2Min",
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb8/Mod2/CountsForClusterLabelAccToMinMUHavMaxMRR.csv");
	//
	// final Pair<String, String> groundTruth4 = new Pair<String, String>("Clusters0Maj",
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb8/Mod0/CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
	// final Pair<String, String> groundTruth5 = new Pair<String, String>("Clusters1Maj",
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb8/Mod1/CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
	// final Pair<String, String> groundTruth6 = new Pair<String, String>("Clusters2Maj",
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb8/Mod2/CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
	// final Pair<String, String> groundTruth1 = new Pair<String, String>("Clusters0PrimeMin",
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb11/CountsForClusterLabelAccToMinMUHavMaxMRR.csv");

	final Pair<String, String> groundTruth1_1 = new Pair<String, String>("Clustering0ModifiedMUTil30Min",
			"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb22/Clustering0MUTil30/CountsForClusterLabelAccToMinMUHavMaxMRR.csv");

	final Pair<String, String> groundTruth1 = new Pair<String, String>("Clustering0MUTil30Min",
			"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb20_1/Clustering0MUTil30/CountsForClusterLabelAccToMinMUHavMaxMRR.csv");

	final Pair<String, String> groundTruth2 = new Pair<String, String>("Clustering0MUTil30Maj",
			"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb20_1/Clustering0MUTil30/CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");

	final Pair<String, String> groundTruth3 = new Pair<String, String>("Clustering1MUTil30Min",
			"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb20_1/Clustering1MUTil30/CountsForClusterLabelAccToMinMUHavMaxMRR.csv");

	final Pair<String, String> groundTruth4 = new Pair<String, String>("Clustering1MUTil30Maj",
			"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb20_1/Clustering1MUTil30/CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");

	final Pair<String, String> groundTruth5 = new Pair<String, String>("Clustering2MUTil30Min",
			"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb20_1/Clustering2MUTil30/CountsForClusterLabelAccToMinMUHavMaxMRR.csv");

	final Pair<String, String> groundTruth6 = new Pair<String, String>("Clustering2MUTil30Maj",
			"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb20_1/Clustering2MUTil30/CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");

	final Pair<String, String> gowallaFroundTruthFirst1 = new Pair<String, String>("GowallaFirst1",
			"/home/gunjan/RWorkspace/GowallaRWorks/allDataGrpByUserInToleranceFirst1Tolerance5.csv");

	final Pair<String, String> gowallaFroundTruthFirst2 = new Pair<String, String>("GowallaFirst2",
			"/home/gunjan/RWorkspace/GowallaRWorks/allDataGrpByUserInToleranceFirst2Tolerance5.csv");

	final Pair<String, String> gowallaFroundTruthFirst3 = new Pair<String, String>("GowallaFirst3",
			"./dataToRead/userGroupsByFirst3_25June.csv");
	// "/home/gunjan/RWorkspace/GowallaRWorks/userGroupsByFirst3_25June.csv");
	// "/home/gunjan/RWorkspace/GowallaRWorks/allDataGrpByUserInToleranceFirst3Tolerance5.csv");

	//
	// private ArrayList<Pair<String, String>> groundTruthFileNames;// = { groundTruth1, groundTruth2, groundTruth3 };

	/**
	 * 
	 * @return
	 */
	private ArrayList<Pair<String, String>> getGroundTruthFileNames()
	{
		ArrayList<Pair<String, String>> groundTruthFileNames = new ArrayList<Pair<String, String>>();

		if (Constant.getDatabaseName().equals("gowalla1"))
		{
			// groundTruthFileNames.add(gowallaFroundTruthFirst1);
			// groundTruthFileNames.add(gowallaFroundTruthFirst2);
			groundTruthFileNames.add(gowallaFroundTruthFirst3);
		}
		else
		{

			// NOTE: ground truth labels are used to determine the clustering used. (in
			// TimelineWEKAClusteringController.classToExpectedOptimalMU(String,
			// String))

			groundTruthFileNames.add(new Pair("Clustering1MUTil18Min",
					"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Clustering1Analysis/CountsForClusterLabelAccToMinMUHavMaxMRR.csv"));// groundTruth1_1);
			// groundTruthFileNames.add(groundTruth2);
			// groundTruthFileNames.add(groundTruth3);
			// groundTruthFileNames.add(groundTruth4);
			// groundTruthFileNames.add(groundTruth5);
			// groundTruthFileNames.add(groundTruth6);
			// groundTruthFileNames.add(groundTruth1);
		}
		return groundTruthFileNames;
	}

	/**
	 * 
	 * @param usersDayTimelinesAll
	 * @param trainingTestUsers
	 */
	public TimelineWEKAClusteringController2019(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesAll,
			Pair<ArrayList<String>, ArrayList<String>> trainingTestUsers)
	{
		String nameOfFeatureFile = "";
		String typeOfDataMining = "Classification";// "KMeans";// EMClustering KMeans

		//////////////////////////////
		int[] userIDs = Constant.getUserIDs();

		// if userid is not set in constant class, in case of gowalla
		if (userIDs == null || userIDs.length == 0)
		{
			userIDs = new int[usersDayTimelinesAll.size()];// System.out.println("usersTimelines.size() = " +
															// usersTimelines.size());
			System.out.println("UserIDs not set, hence extracting user ids from usersTimelines keyset");
			int count = 0;
			for (String userS : usersDayTimelinesAll.keySet())
			{
				userIDs[count++] = Integer.valueOf(userS);
			}
			Constant.setUserIDs(userIDs);
		}
		System.out.println("User ids = " + Arrays.toString(userIDs));

		////////////////////////////

		try
		{
			////////// Start of added 22 Feb 2019///////////////////////////////////////////////////////////
			String commonPath = "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb21GowallaNoHaversine/"
					+ "gowalla1_FEB21H3M25ED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrsFEDPerFS_10F_RTVPNN500NCcoll/";
			String fileToRead = commonPath
					+ "gowalla1_FEB21H3M25ED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrsFEDPerFS_10F_RTVPNN500NCcoll_"
					+ "AllMeanReciprocalRank_MinMUWithMaxFirst0Aware.csv";
			int colIndexUser = 0, colIndexMinMUWithMaxMRR = 1;
			String groundTruthLabel = "GowallaFeb21Clustering";

			// create the clustering ranges
			Map<String, Pair<Integer, Integer>> gowallaClusteringRanges = new LinkedHashMap<>(3);
			gowallaClusteringRanges.put("FirstCluster__", new Pair<>(0, 0));
			gowallaClusteringRanges.put("SecondCluster__", new Pair<>(1, 4));
			gowallaClusteringRanges.put("ThirdCluster__", new Pair<>(5, 100));

			LinkedHashMap<String, String> userIDIndex_ClusterLabel = createGroundTruthForData(fileToRead, colIndexUser,
					colIndexMinMUWithMaxMRR, gowallaClusteringRanges);

			String clusterLabelToWrite = userIDIndex_ClusterLabel.entrySet().stream()
					.map(e -> e.getKey() + "," + e.getValue()).collect(Collectors.joining("\n"));
			WToFile.writeToNewFile("UserIndex,ClusterLabel\n" + clusterLabelToWrite,
					commonPath + "WekaClusteringLabels.csv");
			////////// End of added 22 Feb 2019///////////////////////////////////////////////////////////

			// PopUps.showMessage("Num of user timelnes recieved in weka clustering = " + usersDayTimelinesAll.size());
			// ArrayList<Pair<String, String>> groundTruthFileNames = getGroundTruthFileNames();
			//
			// for (Pair<String, String> gtEntry : groundTruthFileNames)
			// {
			// String groundTruthLabel = "GowallaGroundTruth21Feb2019";// gtEntry.getFirst();
			// String groundTruthFileName = gtEntry.getSecond();
			// createGroundTruthForData();

			String directoryToWrite = commonPath// Constant.getOutputCoreResultsPath()
					// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/WekaCLustering/"
					// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/"//
					// TimelinesClustering/"
					+ Constant.getDatabaseName() + "_" + LocalDateTime.now().getMonth().toString().substring(0, 3)
					+ LocalDateTime.now().getDayOfMonth() + "_" + Constant.geolife1howManyUsers + "_"
					+ groundTruthLabel;
			new File(directoryToWrite).mkdir();
			pathToWrite = directoryToWrite + "/";
			// PopUps.showMessage("Path to write = " + pathToWrite);
			PrintStream consoleLogStream = WToFile
					.redirectConsoleOutput(pathToWrite + typeOfDataMining + "ConsoleLog.txt");
			// System.out.println();

			if (trainingTestUsers == null)
			{
				System.out.println("Inside TimelineWEKAClusteringController: NOT USING TRAINING TEST SPLIT");
			}

			// ///////////////////////// MAIN LOGIC STARTS////////////////////////////////////////////////////
			// (Absolute path to file containing ground truth, column in which ground truth is there, has column
			// headers or not)

			// Triple<String, Integer, Boolean> groundTruth = new Triple(groundTruthFileName, 4, true);

			attributeExtraction = new TimelinesAttributesExtractionCleaned22Feb2019(usersDayTimelinesAll, pathToWrite,
					userIDIndex_ClusterLabel);
			nameOfFeatureFile = attributeExtraction.getAttributeFilenameAbs();

			DataLoader dl = new DataLoader(nameOfFeatureFile,
					nameOfFeatureFile.substring(0, nameOfFeatureFile.length() - 4) + ".arff");
			String outputArffFile = dl.getArffFileName();
			System.out.println("Output arff file is:" + outputArffFile);

			// $$ performClassification(outputArffFile, trainingTestUsers, "SetOf25", gtEntry);

			// switch (typeOfClustering)
			// {
			// case "KMeans":
			// KMeans kmeans = new KMeans(outputArffFile, pathToWrite + typeOfClustering + "Results.txt", 3);
			// break;
			// case "EMClustering":
			// EMClustering emClustering = new EMClustering(outputArffFile, pathToWrite + typeOfClustering +
			// "Results");
			// break;
			// default:
			// System.err.println("Unknown clustering type: " + typeOfClustering);
			// PopUps.showError("Unknown clustering type: " + typeOfClustering);
			// }
			consoleLogStream.close();
			// }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void main(String args[])
	{
		temp22Feb2019();
	}

	private static void temp22Feb2019()
	{
		String commonPath = "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/gowalla1_FEB21H4M0ED0.5STimeLocPop"
				+ "DistPrevDurPrevAllActsFDStFilter0hrsFEDPerFS_10F_RTVPNN100NCcoll/";
		String fileToRead = commonPath + "/gowalla1_FEB21H4M0ED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrsFEDPer"
				+ "FS_10F_RTVPNN100NCcoll_AllMeanReciprocalRank_MinMUWithMaxFirst0Aware.csv";
		int colIndexUser = 0, colIndexMinMUWithMaxMRR = 1;

		// create the clustering ranges
		Map<String, Pair<Integer, Integer>> gowallaClusteringRanges = new LinkedHashMap<>(3);
		gowallaClusteringRanges.put("FirstCluster__", new Pair<>(0, 0));
		gowallaClusteringRanges.put("SecondCluster__", new Pair<>(1, 4));
		gowallaClusteringRanges.put("ThirdCluster__", new Pair<>(5, 100));

		LinkedHashMap<String, String> userIDIndex_ClusterLabel = createGroundTruthForData(fileToRead, colIndexUser,
				colIndexMinMUWithMaxMRR, gowallaClusteringRanges);

		String clusterLabelToWrite = userIDIndex_ClusterLabel.entrySet().stream()
				.map(e -> e.getKey() + "," + e.getValue()).collect(Collectors.joining("\n"));
		WToFile.writeToNewFile("UserIndex,ClusterLabel\n" + clusterLabelToWrite,
				commonPath + "WekaClusteringLabels.csv");
	}

	/**
	 * 
	 * @param minMUWithMaxMRR
	 * @param gowallaClusteringRanges
	 * @return
	 * @since 22 Feb 2019
	 */
	private static String getGowallaClusterLabelFeb2019(int minMUWithMaxMRR,
			Map<String, Pair<Integer, Integer>> gowallaClusteringRanges)
	{
		String clusterLabel = null;

		for (Entry<String, Pair<Integer, Integer>> e : gowallaClusteringRanges.entrySet())
		{
			int minMU = e.getValue().getFirst();
			int maxMU = e.getValue().getSecond();
			if (minMUWithMaxMRR >= minMU && minMUWithMaxMRR <= maxMU)
			{
				return (e.getKey());
			}
		}
		return clusterLabel;
	}

	/**
	 * 
	 * @param fileNameToRead
	 * @param colIndexUser
	 * @param colIndexMinMUWithMaxMRR
	 * @param gowallaClusteringRanges
	 * @return
	 * @since 21 Feb 2019
	 */
	private static LinkedHashMap<String, String> createGroundTruthForData(String fileNameToRead, int colIndexUser,
			int colIndexMinMUWithMaxMRR, Map<String, Pair<Integer, Integer>> gowallaClusteringRanges)
	{
		LinkedHashMap<String, String> userIDIndex_ClusterLabel = new LinkedHashMap<String, String>();
		try
		{
			List<List<String>> groundTruthData = ReadingFromFile.readLinesIntoListOfLists(fileNameToRead, ",");
			groundTruthData.remove(0);
			for (List<String> line : groundTruthData)
			{
				int minMUWithMaxMRR = Integer.valueOf(line.get(colIndexMinMUWithMaxMRR));
				String clusterLabel = getGowallaClusterLabelFeb2019(minMUWithMaxMRR, gowallaClusteringRanges);
				userIDIndex_ClusterLabel.put(line.get(colIndexUser), clusterLabel);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return userIDIndex_ClusterLabel;
	}

	/**
	 * NOT WORKING
	 * 
	 * @param originalDataInstances
	 * @return
	 */
	private LinkedHashMap<String, Instances> splitDataByAttributeGroups(Instances originalDataInstances)
	{
		LinkedHashMap<String, Instances> res = new LinkedHashMap<String, Instances>();

		Instances sampEn2 = WekaUtilityBelt.selectAttributesByRegex(originalDataInstances, Pattern.quote("SampEn"));
		res.put("SampEn2", sampEn2);

		Instances sampEn3 = WekaUtilityBelt.selectAttributesByRegex(originalDataInstances, Pattern.quote("^SampEn*3*"));
		res.put("SampEn3", sampEn3);

		Instances kGram = WekaUtilityBelt.removeAttributesByRegex(originalDataInstances,
				Pattern.quote("^SampEn*|ManualClust*"));
		res.put("KGram", kGram);

		PopUps.showMessage("In spliting data" + " Num of sampen2 instances= " + res.get("SampEn2").size() + " atts = "
				+ sampEn2.numAttributes() + "\nSampEn3 instance = " + res.get("SampEn3").size() + " atts = "
				+ sampEn3.numAttributes() + "\nKGram instance = " + res.get("KGram").size() + " atts = "
				+ kGram.numAttributes());
		return res;
	}

	/**
	 * Splits the instance into three sets of instance. For attributes groups
	 * (InstID,SampEn2related,classID),(InstID,SampEn3related,classID) and (InstID,KGram,classID) had to do it long way.
	 * RemoveByName from weka was not working, neither regex working. brain fried
	 * 
	 * @param originalDataInstances
	 * @return
	 */
	private LinkedHashMap<String, Instances> splitDataInAttributeGroupsG(Instances ins)
	{
		ins.setClassIndex(ins.numAttributes() - 1);
		LinkedHashMap<String, Instances> res = new LinkedHashMap<String, Instances>();

		ArrayList<String> substringsToMatch = new ArrayList<String>();
		substringsToMatch.add("SampEn");
		substringsToMatch.add("2");

		ArrayList<String> substringsToMatch2 = new ArrayList<String>();
		substringsToMatch2.add("ManualClustering");

		Instances n = WekaUtilityBelt.selectAttributesBySubstrings(ins, substringsToMatch, substringsToMatch2, true);
		res.put("SampEn2", n);
		System.out.println(n.size() + " " + n.numAttributes());

		ArrayList<String> substringsToMatch21 = new ArrayList<String>();
		substringsToMatch21.add("SampEn");
		substringsToMatch21.add("3");

		ArrayList<String> substringsToMatch22 = new ArrayList<String>();
		substringsToMatch22.add("ManualClustering");

		Instances n2 = WekaUtilityBelt.selectAttributesBySubstrings(ins, substringsToMatch21, substringsToMatch22,
				true);
		res.put("SampEn3", n2);
		System.out.println(n2.size() + " " + n2.numAttributes());

		ArrayList<String> substringsToMatch31 = new ArrayList<String>();
		substringsToMatch31.add("Gram");

		// NumOfValidDistinctActivities/LengthOfTimeline

		ArrayList<String> substringsToMatch32 = new ArrayList<String>();
		substringsToMatch32.add("NumOfValidDistinctActivities/LengthOfTimeline");

		Instances n3 = WekaUtilityBelt.selectAttributesBySubstrings(ins, substringsToMatch31, substringsToMatch32,
				true);
		res.put("KGram", n3);
		System.out.println(n3.size() + " " + n3.numAttributes());

		return res;
	}

	/**
	 * Performs classification using J48 classifier
	 * 
	 * @param outputArffFile
	 * @param trainingTestUsers
	 * @param idForPreselectingSetOfAttributes
	 * @param groundTruth
	 *            (Label, AbsFileName)
	 */
	private void performClassification(String outputArffFile,
			Pair<ArrayList<String>, ArrayList<String>> trainingTestUsers, String idForPreselectingSetOfAttributes,
			Pair<String, String> groundTruth)
	{
		Classifier classifiersUsed[] = new J48[4];
		// /String idForPreselectingSetOfAttributes = "SetOf25";
		Instances dataInstances;
		try
		{
			if (trainingTestUsers == null) // doing cross-validation instead
			{
				dataInstances = preSelectSetOfAttributes(outputArffFile, new ArrayList(),
						idForPreselectingSetOfAttributes);
			}
			else
			{
				dataInstances = preSelectSetOfAttributes(outputArffFile, trainingTestUsers.getSecond(),
						idForPreselectingSetOfAttributes);
			}

			// PopUps.showMessage("remains " + dataInstances.size() + " instance after splitting ");
			classifiersUsed[0] = buildClassifierAndCrossValidate(dataInstances, "1", idForPreselectingSetOfAttributes,
					true);

			this.usedClassifier = classifiersUsed[0];
			Constant.setClassifier(usedClassifier);
			System.out.println(" Is constant classifier okay = " + Constant.getClassifierUsed().toString());
			System.out.println(" ************************** OutputMarker245 ");

			if (trainingTestUsers != null) // that is, using training test split, now we get explicit predictions.. NOT
											// NEEDED for leave one out cross validation
			{
				PopUps.showError("Error in performClassification"
						+ "  not supposed to user training test users split like this");
				// userIDPredictedClass = predictOptimalMU(trainingTestUsers.getSecond(), usedClassifier, allInstances);
				// LinkedHashMap<String, String> userIDClassMap = featureExtraction.getUserIDClass();
				// int correctlyClassified;
				// for (Map.Entry<String, Double> entry : userIDPredictedClass.entrySet())
				// { WritingToFile.appendLineToFile(entry.getKey() + "," + entry.getValue() + "," +
				// userIDPredictedClass.get(entry.getKey()) + "\n", "PredictedClass");}
				// for (Entry<String, List<Double>> entry : userIDPredictedClassProb.entrySet())
				// {WritingToFile.appendLineToFile(entry.getKey() + "," + entry.getValue() + "," +
				// userIDPredictedClass.get(entry.getKey()) + "\n", "PredictedClassProbab");}
			}

			else
			{
				// get predicted classes for each instance from the file PredictedClasses written in BuildClassifier
				userIDPredictedClass = setPredictedClasses(idForPreselectingSetOfAttributes);
			}

			writePredictedClasses(idForPreselectingSetOfAttributes, groundTruth);

			/* *******Now for splitted instances ********** */

			// PopUps.showMessage("Received " + dataInstances.size() + " instance after preselction");
			LinkedHashMap<String, Instances> splittedData = splitDataInAttributeGroupsG(dataInstances);

			// PopUps.showMessage("Path to write =" + this.pathToWrite);
			// WekaUtilityBelt.writeArffAbsolute(splittedData.get("SampEn2"), this.pathToWrite + "SampEn2.arff");
			// WekaUtilityBelt.writeArffAbsolute(splittedData.get("SampEn3"), this.pathToWrite + "SampEn3.arff");
			// WekaUtilityBelt.writeArffAbsolute(splittedData.get("KGram"), this.pathToWrite + "KGram.arff");
			//
			int classifiersUsedCount = 1;
			for (String attrGrp : attributeGroups)
			{
				WekaUtilityBelt.writeArffAbsolute(splittedData.get(attrGrp), this.pathToWrite + attrGrp + ".arff");

				// PopUps.showMessage("AttrGrps = " + attrGrp);

				Instances insts = splittedData.get(attrGrp);

				if (insts == null)
				{
					System.out.println("insts == null for attGrps:" + attrGrp);
				}
				classifiersUsed[classifiersUsedCount] = buildClassifierAndCrossValidate(insts, "1",
						idForPreselectingSetOfAttributes + attrGrp, false);
				// Constant.setClassifier(usedClassifier);
				// System.out.println(" Is constant classifier okay = " + Constant.getClassifierUsed().toString());

				if (trainingTestUsers != null) // that is, using training test split, now we get explicit predictions..
												// NOT NEEDED for leave one out cross validation
				{
					PopUps.showError("Error in performClassification"
							+ "  not supposed to user training test users split like this");
				}

				else
				{
					userIDPredictedClass = setPredictedClasses(idForPreselectingSetOfAttributes + attrGrp);
				}

				writePredictedClasses(idForPreselectingSetOfAttributes + attrGrp, groundTruth);
				classifiersUsedCount++;
			}

			ensembleLateFusion();
			// wekaEnsemble(classifiersUsed[1], classifiersUsed[2], classifiersUsed[3], dataInstances);
		}

		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.performClassification(String, Pair<ArrayList<String>, ArrayList<String>>)");
		}

	}

	private void wekaEnsemble(Classifier classifier1, Classifier classifier2, Classifier classifier3,
			Instances dataInstances)
	{
		System.out.println("Inside weka ensemble");

		PopUps.showMessage(classifier1.toString() + "\n" + classifier2.toString() + "\n" + classifier3.toString());

		if (classifier1.equals(classifier2))
		{
			PopUps.showMessage("classfier 1 and classifier 2 are equal\n");
		}

		Vote voteaVG = new Vote();

		voteaVG.addPreBuiltClassifier(classifier1);
		voteaVG.addPreBuiltClassifier(classifier2);
		voteaVG.addPreBuiltClassifier(classifier3);
		voteaVG.setCombinationRule(new SelectedTag(1, Vote.TAGS_RULES));

		buildEnsembleClassifierAndCrossValidate(voteaVG, dataInstances, "1", "Average");

		Vote votePro = new Vote();

		votePro.addPreBuiltClassifier(classifier1);
		votePro.addPreBuiltClassifier(classifier2);
		votePro.addPreBuiltClassifier(classifier3);
		votePro.setCombinationRule(new SelectedTag(2, Vote.TAGS_RULES));

		buildEnsembleClassifierAndCrossValidate(votePro, dataInstances, "1", "Product");

		Vote voteMaj = new Vote();

		voteMaj.addPreBuiltClassifier(classifier1);
		voteMaj.addPreBuiltClassifier(classifier2);
		voteMaj.addPreBuiltClassifier(classifier3);
		voteMaj.setCombinationRule(new SelectedTag(3, Vote.TAGS_RULES));

		buildEnsembleClassifierAndCrossValidate(voteMaj, dataInstances, "1", "Majority");

		Vote voteMax = new Vote();

		voteMax.addPreBuiltClassifier(classifier1);
		voteMax.addPreBuiltClassifier(classifier2);
		voteMax.addPreBuiltClassifier(classifier3);
		voteMax.setCombinationRule(new SelectedTag(5, Vote.TAGS_RULES));

		buildEnsembleClassifierAndCrossValidate(voteMax, dataInstances, "1", "Max");

		// Vote voteMedian = new Vote();
		//
		// voteMedian.addPreBuiltClassifier(classifier1);
		// voteMedian.addPreBuiltClassifier(classifier2);
		// voteMedian.addPreBuiltClassifier(classifier3);
		// voteMedian.setCombinationRule(new SelectedTag(6, Vote.TAGS_RULES));
		//
		// buildEnsembleClassifierAndCrossValidate(voteMedian, dataInstances, "1", "Median");
		//
		// System.out.println(vote1.classifyInstance(instance))

	}

	public void buildEnsembleClassifierAndCrossValidate(Vote meta, Instances dataInstancesOriginal,
			String indexOfInstanceID, String metaNamePhrase)
	{
		System.out.println("Inside ensemble buildClassifier for " + metaNamePhrase);

		String resultToWrite = new String();
		Instances dataInstances = new Instances(dataInstancesOriginal);

		// String namesOfAllAttributesRecieved;
		int numOfAllAttributesRecieved;
		try
		{
			numOfAllAttributesRecieved = dataInstancesOriginal.numAttributes();

			resultToWrite += ("\n--> Ensemble Classification " + LocalDateTime.now().toString() + "\n");
			resultToWrite += ("Index of Instance ID: " + indexOfInstanceID + "\n");
			resultToWrite += dataInstances.toSummaryString();

			Remove removeID = new Remove();
			removeID.setAttributeIndices(indexOfInstanceID);// ("1");

			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(removeID);
			fc.setClassifier(meta);

			resultToWrite += fc.globalInfo() + "\n";
			// ------------------------------------------------

			Evaluation eval = new Evaluation(dataInstances);

			StringBuffer predictionResults = new StringBuffer();
			CSV predictionsResultPlain = new CSV();

			predictionsResultPlain.setBuffer(predictionResults);
			predictionsResultPlain.setAttributes("first-last");
			predictionsResultPlain.setOutputDistribution(true);

			eval.crossValidateModel(fc, dataInstances, dataInstances.size(), new Random(1), predictionsResultPlain);// ,
																													// attsToOutput,
																													// outputDist);
			// PopUps.showMessage("Still here 4: After crossvalidation ");

			resultToWrite += "\n---------- Leave one out Cross-validation-------------------\n";
			resultToWrite += WekaUtilityBelt.getRelevantEvaluationMetricsAsString(eval);

			resultToWrite += eval.toSummaryString();
			// resultToWrite += "\n Tree when run on all data: \n " + tree.graph();

			resultToWrite += "\n" + predictionResults.toString() + "\n";

			WToFile.appendLineToFileAbs(resultToWrite,
					this.pathToWrite + metaNamePhrase + "EnsembleClassificationEvaluation.txt");
			WToFile.appendLineToFileAbs(predictionResults.toString(),
					this.pathToWrite + metaNamePhrase + "EnsemblePredictedClasses.csv");
		}

		catch (Exception e)
		{
			String exceptionMsg = e.getMessage();
			e.printStackTrace();
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.buildClassifierAndCrossValidate(Instances, String, String, boolean)");
		}
		// return tree;
	}

	/**
	 * Performs the (manual) pre-selection of attributes </br>
	 * <font color="red">Note: the first attribute should be the instance id, which will be user id</font>
	 * 
	 * @param inputAbsoluteFileName
	 *            the data source file to be read for building classifier
	 * 
	 * @param usersToRemove
	 *            userids of users which should not be used for building classifier (useful for training-test split and
	 *            not for cross-validation)
	 * 
	 * @param idSetOfAttributes
	 * @return
	 */
	private Instances preSelectSetOfAttributes(String inputAbsoluteFileName, ArrayList<String> usersToRemove,
			String idSetOfAttributes)
	{
		Instances dataInstances = null;
		// PopUps.showMessage("Received);
		try
		{
			dataInstances = DataSource.read(inputAbsoluteFileName);
			dataInstances.setClassIndex(dataInstances.numAttributes() - 1);
			int initialNumOfInstances = dataInstances.size();
			// allInstances = dataInstances;

			// dataInstances = WekaUtilityBelt.keepInstancesByRangeList(dataInstances,
			// "1,2,3,4,5,6,7,8,9,10,11,12,13,14");
			// PopUps.showMessage("Created " + dataInstances.size() + " instance --1 ");
			dataInstances = WekaUtilityBelt.removeInstancesByRangeList(dataInstances,
					userIDsToInstanceIDs(usersToRemove));
			// PopUps.showMessage("Have " + dataInstances.size() + " instances --2 ");

			if (dataInstances.size() != initialNumOfInstances)
			{
				PopUps.showMessage(
						"Alert! Some Instances (users) removed.\n Removed users: " + usersToRemove.toString());
			}
			// dataInstances = WekaUtilityBelt.removeInstancesByRangeList(dataInstances, "2,3,4,15");
			// dataInstances = WekaUtilityBelt.removeAttributeByRangeList(dataInstances,
			// "3,5-7,9,11-20,22-34,37-42,45-50");// 3,5-7,11-18,22-29,31-34,36-41,44-49");

			switch (idSetOfAttributes)
			{
			case "SetOf10":
				dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances,
						"4,6-8,10,12-21,23-35,38-43,46-51");// 3,5-7,11-18,22-29,31-34,36-41,44-49");
				break;
			case "SetOf13":
				dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances,
						"4,6-8,10,12-13,15,17-19,21,23-35,38-43,46-51");// 3,5-7,11-18,22-29,31-34,36-41,44-49");
				break;
			case "SetOf12":
				dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances, "10,12-21,23-35,38-51");// 3,5-7,11-18,22-29,31-34,36-41,44-49");
				break;
			case "SetOf13b":
				dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances, "10,12-21,23-35,39-51");// 3,5-7,11-18,22-29,31-34,36-41,44-49");
				break;
			case "SetOf14":
				dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances,
						"10,12-21,23-35,38-43,46-51");// 3,5-7,11-18,22-29,31-34,36-41,44-49");
				break;
			case "SetOf25":
				// Set of 25 to remove:10, 13,21,24-35, 39-43, 47-69
				dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances,
						"10,13,21,24-35, 39-43, 47-69");// 3,5-7,11-18,22-29,31-34,36-41,44-49");
				// PopUps.showMessage("Have " + dataInstances.size() + " instances --2.5 ");
				break;
			default:
				// Set of 25 to remove:10, 13,21,24-35, 39-43, 47-69
				dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances,
						"10,13,21,24-35, 39-43, 47-69");// 3,5-7,11-18,22-29,31-34,36-41,44-49");
				PopUps.showMessage("Alert! Choosing default set of preselected attributes");
				break;
			}

			// adding id to track instance ..ids correspond to userid WRONG NOT YET
			// AddID addID = new AddID();
			// addID.setInputFormat(dataInstances);
			// dataInstances = Filter.useFilter(dataInstances, addID);

			// ///////////

			WekaUtilityBelt.writeArffAbsolute(dataInstances, this.pathToWrite + "PrunedTimelineFeatureVectors.arff");
			// PopUps.showMessage("Have " + dataInstances.size() + " instances --3 ");
		}

		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.preSelectSetOfAttributes(String, ArrayList<String>, String)");
		}

		// PopUps.showMessage("preSelectSetOfAttributes() is returning " + dataInstances.size() + " instances now");
		return dataInstances;
	}

	/**
	 * Writes the predicted classes to a file named 'idForPreselectingSetOfAttributes+ UserSerialNumPredictedClass.csv'
	 * at pathToWrite.
	 * <p>
	 * Alert. Here predicted best MU is considered as integer. So not suitable for fractions as MU TODO : use
	 * isFractionsInMatchingUnits for which i need to determine the matching unit array used here. I think its too much
	 * for minimal impact/utility
	 * 
	 * @param fileNamePhrase
	 * @param groundTruth
	 */
	public void writePredictedClasses(String fileNamePhrase, Pair<String, String> groundTruth)
	{
		// PopUps.showMessage("Inside writing predicted classes: " + userSerialNumPredictedClass.size() + " " +
		// userIDPredictedClass.size());
		WToFile.appendLineToFileAbs("UserSerialNum,UserID,PredictedClass,PredictedBestMU, MRRForPredictedBestMU\n",
				this.pathToWrite + fileNamePhrase + "UserSerialNumPredictedClass.csv");

		Map<Integer, String> treeMap = new TreeMap<Integer, String>(userAsInstanceIDPredictedClass);
		Map<String, String> treeMap2 = new TreeMap<String, String>(userIDPredictedClass);

		System.out.println("inside writePredictedClasses");

		for (Entry<Integer, String> e : treeMap.entrySet())
		{
			// Alert: predicted best Mu as integer
			int predictedBestMU = classToExpectedOptimalMU(e.getValue(), groundTruth.getFirst());
			int userID = e.getKey();

			double mrrForPredictedBestMRR = Double.parseDouble(CSVUtils.getCellValueFromCSVFile((predictedBestMU + 2),
					(userID + 1), getAllMRRFileNameToRead(groundTruth)));

			String msg = "User_" + e.getKey() + "," + instanceIDToUserID(e.getKey()) + ", " + e.getValue() + ","
					+ predictedBestMU + "," + mrrForPredictedBestMRR + "\n";

			WToFile.appendLineToFileAbs(msg, this.pathToWrite + fileNamePhrase + "UserSerialNumPredictedClass.csv");

			// Sanity CHeck start //NOT useful as it doesnt address the issue and morover not needed
			// String predictedClassFromInstanceIDClassMap = instanceIDToUserID(e.getKey());
			// String predictedClassFromUserIDClassMap = treeMap2.get(predictedClassFromInstanceIDClassMap);
			//
			// if (predictedClassFromInstanceIDClassMap.equals(predictedClassFromUserIDClassMap) == false)
			// {
			// System.err.println("Sanity Check Error in writePredictedClasses: predictedClassFromInstanceIDClassMap= "
			// + predictedClassFromInstanceIDClassMap + " while predictedClassFromUserIDClassMap= "
			// + predictedClassFromUserIDClassMap + " for userID = " + instanceIDToUserID(e.getKey()));
			// }
			// else
			// {
			// System.out.println("Sanity Check Okay in writePredictedClasses: predictedClassFromInstanceIDClassMap= "
			// + predictedClassFromInstanceIDClassMap + " while predictedClassFromUserIDClassMap= "
			// + predictedClassFromUserIDClassMap + " for userID = " + instanceIDToUserID(e.getKey()));
			// }
			// // Sanity Check end
		}
	}

	/**
	 * Returns all MRR filename for Iteration 1 </br>
	 * Alert: using / as file path separator. Not suitable for windows machines.
	 * 
	 * @param groundTruth
	 * @return
	 */
	private String getAllMRRFileNameToRead(Pair<String, String> groundTruth)
	{
		// String fileNameToReturn = "";
		/*
		 * Ground truth looks like: new Pair<String, String>( "Clustering0MUTil30Min",
		 * "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb19/Clustering0MUTil30/CountsForClusterLabelAccToMinMUHavMaxMRR.csv"
		 * );
		 */
		String[] splittedFileName = groundTruth.getSecond().split("/");

		String basePath = "";

		// removing the last path
		for (int i = 0; i < splittedFileName.length - 1; i++)
		{
			basePath += "/" + splittedFileName[i];
		}

		basePath += "/Iteration1AllMRR.csv";
		return basePath;
	}

	/**
	 * 
	 */
	public void ensembleLateFusion()
	{
		String fileAll, fileSampEn2, fileSampEn3, fileKGram;
		int colNumActualClassAll, colNumPredictedClassAll, colNumPredictedClassSampEn2, colNumPredictedClassSampEn3,
				colNumPredictedClassKGram;
		int colNumUserIDAll, colNumUserIDSampEn2, colNumUserIDSampEn3, colNumUserIDKGram;
		String ensembleFileName = pathToWrite + "EnsembleLF.csv";

		fileAll = pathToWrite + "SetOf25PredictedClasses.csv";
		fileSampEn2 = pathToWrite + "SetOf25SampEn2PredictedClasses.csv";
		fileSampEn3 = pathToWrite + "SetOf25SampEn3PredictedClasses.csv";
		fileKGram = pathToWrite + "SetOf25KGramPredictedClasses.csv";

		List<CSVRecord> allAttRecords = CSVUtils.getCSVRecords(fileAll);
		colNumActualClassAll = getColumnNumberOfHeader("actual", allAttRecords.get(0));
		colNumPredictedClassAll = getColumnNumberOfHeader("predicted", allAttRecords.get(0));
		colNumUserIDAll = getColumnNumberOfHeader("UserID", allAttRecords.get(0));
		allAttRecords.remove(0);// removing header

		List<CSVRecord> sampEn2AttRecords = CSVUtils.getCSVRecords(fileSampEn2);
		colNumPredictedClassSampEn2 = getColumnNumberOfHeader("predicted", sampEn2AttRecords.get(0));
		colNumUserIDSampEn2 = getColumnNumberOfHeader("UserID", sampEn2AttRecords.get(0));
		sampEn2AttRecords.remove(0);// removing header

		List<CSVRecord> sampEn3AttRecords = CSVUtils.getCSVRecords(fileSampEn3);
		colNumPredictedClassSampEn3 = getColumnNumberOfHeader("predicted", sampEn3AttRecords.get(0));
		colNumUserIDSampEn3 = getColumnNumberOfHeader("UserID", sampEn3AttRecords.get(0));
		sampEn3AttRecords.remove(0);// removing header

		List<CSVRecord> kGramAttRecords = CSVUtils.getCSVRecords(fileKGram);
		colNumPredictedClassKGram = getColumnNumberOfHeader("predicted", kGramAttRecords.get(0));
		colNumUserIDKGram = getColumnNumberOfHeader("UserID", kGramAttRecords.get(0));
		kGramAttRecords.remove(0);// removing header
		// User1
		TreeMap<String, String> actualClass = new TreeMap<String, String>(ComparatorUtils.getUserIDComparator());
		TreeMap<String, String> predictedClassAll = new TreeMap<String, String>(ComparatorUtils.getUserIDComparator());
		TreeMap<String, String> predictedClassSampEn2 = new TreeMap<String, String>(
				ComparatorUtils.getUserIDComparator());
		TreeMap<String, String> predictedClassSampEn3 = new TreeMap<String, String>(
				ComparatorUtils.getUserIDComparator());
		TreeMap<String, String> predictedClassKGram = new TreeMap<String, String>(
				ComparatorUtils.getUserIDComparator());

		// distribution <first cluster, thirdcluster, secondcluster>
		TreeMap<String, Triple<Double, Double, Double>> predictedClassAllDist = new TreeMap<String, Triple<Double, Double, Double>>(
				ComparatorUtils.getUserIDComparator());
		TreeMap<String, Triple<Double, Double, Double>> predictedClassSampEn2Dist = new TreeMap<String, Triple<Double, Double, Double>>(
				ComparatorUtils.getUserIDComparator());
		TreeMap<String, Triple<Double, Double, Double>> predictedClassSampEn3Dist = new TreeMap<String, Triple<Double, Double, Double>>(
				ComparatorUtils.getUserIDComparator());
		TreeMap<String, Triple<Double, Double, Double>> predictedClassKGramDist = new TreeMap<String, Triple<Double, Double, Double>>(
				ComparatorUtils.getUserIDComparator());

		System.out.println(">> Debug Marker: 679: " + colNumActualClassAll + "  " + colNumPredictedClassAll + "  "
				+ colNumPredictedClassSampEn2 + "  " + colNumPredictedClassSampEn3 + "  " + colNumPredictedClassKGram
				+ " " + colNumUserIDAll + "  " + colNumUserIDSampEn2 + "  " + colNumUserIDSampEn3 + "  "
				+ colNumUserIDKGram + "\n");
		for (int i = 0; i < allAttRecords.size(); i++)
		{
			actualClass.put(allAttRecords.get(i).get(colNumUserIDAll - 1),
					allAttRecords.get(i).get(colNumActualClassAll - 1).substring(2));

			predictedClassAll.put(allAttRecords.get(i).get(colNumUserIDAll - 1),
					allAttRecords.get(i).get(colNumPredictedClassAll - 1).substring(2));
			predictedClassAllDist.put(allAttRecords.get(i).get(colNumUserIDAll - 1),
					getDistribution(allAttRecords.get(i)));

			predictedClassSampEn2.put(sampEn2AttRecords.get(i).get(colNumUserIDSampEn2 - 1),
					sampEn2AttRecords.get(i).get(colNumPredictedClassSampEn2 - 1).substring(2));
			predictedClassSampEn2Dist.put(sampEn2AttRecords.get(i).get(colNumUserIDSampEn2 - 1),
					getDistribution(sampEn2AttRecords.get(i)));

			predictedClassSampEn3.put(sampEn3AttRecords.get(i).get(colNumUserIDSampEn3 - 1),
					sampEn3AttRecords.get(i).get(colNumPredictedClassSampEn3 - 1).substring(2));
			predictedClassSampEn3Dist.put(sampEn3AttRecords.get(i).get(colNumUserIDSampEn3 - 1),
					getDistribution(sampEn3AttRecords.get(i)));

			predictedClassKGram.put(kGramAttRecords.get(i).get(colNumUserIDKGram - 1),
					kGramAttRecords.get(i).get(colNumPredictedClassKGram - 1).substring(2));
			predictedClassKGramDist.put(kGramAttRecords.get(i).get(colNumUserIDKGram - 1),
					getDistribution(kGramAttRecords.get(i)));

			// actualClass.put(allAttRecords.get(i).get(7), allAttRecords.get(i).get(1).substring(2));
			//
			// predictedClassAll.put(allAttRecords.get(i).get(7), allAttRecords.get(i).get(2).substring(2));
			// predictedClassAllDist.put(allAttRecords.get(i).get(7), getDistribution(allAttRecords.get(i)));
			//
			// predictedClassSampEn2.put(sampEn2AttRecords.get(i).get(7), sampEn2AttRecords.get(i).get(2).substring(2));
			// predictedClassSampEn2Dist.put(sampEn2AttRecords.get(i).get(7),
			// getDistribution(sampEn2AttRecords.get(i)));
			//
			// predictedClassSampEn3.put(sampEn3AttRecords.get(i).get(7), sampEn3AttRecords.get(i).get(2).substring(2));
			// predictedClassSampEn3Dist.put(sampEn3AttRecords.get(i).get(7),
			// getDistribution(sampEn3AttRecords.get(i)));
			//
			// predictedClassKGram.put(kGramAttRecords.get(i).get(7), kGramAttRecords.get(i).get(2).substring(2));
			// predictedClassKGramDist.put(kGramAttRecords.get(i).get(7), getDistribution(kGramAttRecords.get(i)));

		}

		WToFile.appendLineToFileAbs("User ID, SampEn2D,,,SampEn3D,,,KGramD,,\n", ensembleFileName);

		for (Entry<String, Triple<Double, Double, Double>> entrySampEn2 : predictedClassSampEn2Dist.entrySet())
		{
			String userID = entrySampEn2.getKey();

			String msg = userID + "," + entrySampEn2.getValue().toStringCSV() + ","
					+ predictedClassSampEn3Dist.get(userID).toStringCSV() + ","
					+ predictedClassKGramDist.get(userID).toStringCSV();
			WToFile.appendLineToFileAbs(msg + "\n", ensembleFileName);// .appendLineToFileAbs(msg, path);
		}

		fuse(predictedClassSampEn2Dist, predictedClassSampEn3Dist, predictedClassKGramDist, actualClass,
				predictedClassAll, predictedClassSampEn2, predictedClassSampEn3, predictedClassKGram);
	}

	/**
	 * Note: the distribution fetched from weka file is CLUSTER 1, CLUSTER 3, CLUSTER 2) , and we will maintain this
	 * order in our code and wirtten files.
	 * 
	 * @param predictedClassSampEn2Dist
	 *            (UserID, (distribution for sampEn2 atts)) in the form (UserID, (Cluster1Probability,
	 *            Cluster3Probability, Cluster2Probability))
	 * @param predictedClassSampEn3Dist
	 *            (UserID, (Cluster1Probability, Cluster3Probability, Cluster2Probability))
	 * @param predictedClassKGramDist
	 *            (UserID, (Cluster1Probability, Cluster3Probability, Cluster2Probability))
	 * @param actualClass
	 * @param predictedClassAll
	 * @param predictedClassSampEn2
	 * @param predictedClassSampEn3
	 * @param predictedClassKGram
	 */
	private void fuse(TreeMap<String, Triple<Double, Double, Double>> predictedClassSampEn2Dist,
			TreeMap<String, Triple<Double, Double, Double>> predictedClassSampEn3Dist,
			TreeMap<String, Triple<Double, Double, Double>> predictedClassKGramDist,
			TreeMap<String, String> actualClass, TreeMap<String, String> predictedClassAll,
			TreeMap<String, String> predictedClassSampEn2, TreeMap<String, String> predictedClassSampEn3,
			TreeMap<String, String> predictedClassKGram)
	{
		/**
		 * (UserID, (mean of sampen2 sampen3 kgram for cluster1, mean of sampen2 sampen3 kgram for cluster3, mean of
		 * sampen2 sampen3 kgram for cluster2))
		 */
		TreeMap<String, Triple<Double, Double, Double>> meanEnsemble = new TreeMap<String, Triple<Double, Double, Double>>(
				ComparatorUtils.getUserIDComparator());

		/**
		 * (UserID, (max of sampen2 sampen3 kgram for cluster1, max of sampen2 sampen3 kgram for cluster3, max of
		 * sampen2 sampen3 kgram for cluster2))
		 */
		TreeMap<String, Triple<Double, Double, Double>> maxEnsemble = new TreeMap<String, Triple<Double, Double, Double>>(
				ComparatorUtils.getUserIDComparator());

		/**
		 * (UserID, (max of sampen2 sampen3 kgram for cluster1, max of sampen2 sampen3 kgram for cluster3, max of
		 * sampen2 sampen3 kgram for cluster2))
		 */
		// TreeMap<String, Triple<Double, Double, Double>> modeEnsemble = new TreeMap<String, Triple<Double, Double,
		// Double>>(getUserIDComparator());

		TreeMap<String, String> predictedClassMaxEnsemble = new TreeMap<String, String>(
				ComparatorUtils.getUserIDComparator());
		TreeMap<String, String> predictedClassMeanEnsemble = new TreeMap<String, String>(
				ComparatorUtils.getUserIDComparator());
		TreeMap<String, String> predictedClassMajorityVoting = new TreeMap<String, String>(
				ComparatorUtils.getUserIDComparator());
		TreeMap<String, String> predictedClassWeightedMajorityVoting = new TreeMap<String, String>(
				ComparatorUtils.getUserIDComparator());
		// TreeMap<String, String> predictedClassModeEnsemble = new TreeMap<String, String>(getUserIDComparator());

		String fuseDistriFileToWrite = pathToWrite + "FusedDistributionFile.csv";
		String fusePredicFileToWrite = pathToWrite + "FusedPredictionFile.csv";

		WToFile.appendLineToFileAbs("UserID, MaxEnsemble,,,MeanEnsemble,,\n", fuseDistriFileToWrite);
		WToFile.appendLineToFileAbs(
				"UserID, MaxEnsemblePredictedC,MeanEnsemblePredictedC,MajVotingPredictedC,WtdMajorityPredictC, actualClass, EarlyFusionPrediction\n",
				fusePredicFileToWrite);

		for (Entry<String, Triple<Double, Double, Double>> entrySampEn2 : predictedClassSampEn2Dist.entrySet())
		{

			double cluster1Vals[] = new double[3]; // three values: for sampEn2, sampEn3 and KGram attributes
			double cluster2Vals[] = new double[3]; // three values: for sampEn2, sampEn3 and KGram attributes
			double cluster3Vals[] = new double[3]; // three values: for sampEn2, sampEn3 and KGram attributes

			String userID = entrySampEn2.getKey();

			cluster1Vals[0] = entrySampEn2.getValue().getFirst();// sampEn2
			cluster1Vals[1] = predictedClassSampEn3Dist.get(userID).getFirst();// sampEn3
			cluster1Vals[2] = predictedClassKGramDist.get(userID).getFirst();// KGram

			cluster2Vals[0] = entrySampEn2.getValue().getThird();// sampEn2
			cluster2Vals[1] = predictedClassSampEn3Dist.get(userID).getThird();// sampEn3
			cluster2Vals[2] = predictedClassKGramDist.get(userID).getThird();// KGram

			cluster3Vals[0] = entrySampEn2.getValue().getSecond();// sampEn2
			cluster3Vals[1] = predictedClassSampEn3Dist.get(userID).getSecond();// sampEn3
			cluster3Vals[2] = predictedClassKGramDist.get(userID).getSecond();// KGram

			maxEnsemble.put(userID, new Triple<Double, Double, Double>(StatUtils.max(cluster1Vals),
					StatUtils.max(cluster3Vals), StatUtils.max(cluster2Vals)));
			meanEnsemble.put(userID, new Triple<Double, Double, Double>(StatUtils.mean(cluster1Vals),
					StatUtils.mean(cluster3Vals), StatUtils.mean(cluster2Vals)));
			// modeEnsemble.put(userID,
			// new Triple<Double, Double, Double>(StatUtils.mode(cluster1Vals), StatUtils.mode(cluster3Vals),
			// StatUtils.(cluster2Vals)));

			predictedClassMaxEnsemble.put(userID, getPredClassWithHighestConfFromDistribution(maxEnsemble.get(userID)));
			predictedClassMeanEnsemble.put(userID,
					getPredClassWithHighestConfFromDistribution(meanEnsemble.get(userID)));

			// String msg = userID + "," + entrySampEn2.getValue().toStringCSV() + "," +
			// predictedClassSampEn3Dist.get(userID).toStringCSV() + ","
			// + predictedClassKGramDist.get(userID).toStringCSV();

			String msg1 = userID + "," + maxEnsemble.get(userID).getFirst() + "," + maxEnsemble.get(userID).getSecond()
					+ "," + maxEnsemble.get(userID).getThird();
			msg1 += "," + meanEnsemble.get(userID).getFirst() + "," + meanEnsemble.get(userID).getSecond() + ","
					+ meanEnsemble.get(userID).getThird() + "\n";

			predictedClassMajorityVoting.put(userID, getMajority(predictedClassSampEn2.get(userID),
					predictedClassSampEn3.get(userID), predictedClassKGram.get(userID)));

			predictedClassWeightedMajorityVoting.put(userID,
					getWeightedMajority(predictedClassSampEn2.get(userID), predictedClassSampEn3.get(userID),
							predictedClassKGram.get(userID), cluster1Vals, cluster2Vals, cluster3Vals));

			String msg2 = userID + "," + predictedClassMaxEnsemble.get(userID) + ","
					+ predictedClassMeanEnsemble.get(userID) + "," + predictedClassMajorityVoting.get(userID) + ","
					+ predictedClassWeightedMajorityVoting.get(userID) + "," + actualClass.get(userID) + ","
					+ predictedClassAll.get(userID) + "\n";

			WToFile.appendLineToFileAbs(msg1, fuseDistriFileToWrite);
			WToFile.appendLineToFileAbs(msg2, fusePredicFileToWrite);
		}

		// evaluateFusion(actualClass, predictedClassMaxEnsemble, "EvalMaxEnsemble.csv");
		// evaluateFusion(actualClass, predictedClassMeanEnsemble, "EvalMeanEnsemble.csv");
		// evaluateFusion(actualClass, predictedClassMajorityVoting, "EvalMajorityVotingEnsemble.csv");
		// evaluateFusion(actualClass, predictedClassWeightedMajorityVoting, "EvalWeightedMajorityVotingEnsemble.csv");
		evaluateFusion(actualClass, predictedClassMaxEnsemble, "EvalEnsemble.csv", "Max");
		evaluateFusion(actualClass, predictedClassMeanEnsemble, "EvalEnsemble.csv", "Mean");
		evaluateFusion(actualClass, predictedClassMajorityVoting, "EvalEnsemble.csv", "Majority Voting");
		evaluateFusion(actualClass, predictedClassWeightedMajorityVoting, "EvalEnsemble.csv", "Wtd Majority Voting");

	}

	private void evaluateFusion(TreeMap<String, String> groundtruth, TreeMap<String, String> prediction,
			String fileNamePhrase, String titlePhrase)
	{
		// FIrst Cluster, ThirdCluster, SecondCluster
		Triple<Double, Double, Double> classWiseTruePositives = new Triple<Double, Double, Double>((double) 0,
				(double) 0, (double) 0);
		Triple<Double, Double, Double> classWiseFalseNegatives = new Triple<Double, Double, Double>((double) 0,
				(double) 0, (double) 0);
		Triple<Double, Double, Double> classWiseFalsePositives = new Triple<Double, Double, Double>((double) 0,
				(double) 0, (double) 0);
		Triple<Double, Double, Double> classWiseTrueNegatives = new Triple<Double, Double, Double>((double) 0,
				(double) 0, (double) 0);

		Triple<Double, Double, Double> classWiseRandAccuracy = new Triple<Double, Double, Double>((double) 0,
				(double) 0, (double) 0);
		Triple<Double, Double, Double> classWisePositives = new Triple<Double, Double, Double>((double) 0, (double) 0,
				(double) 0);
		Triple<Double, Double, Double> classWiseNegatives = new Triple<Double, Double, Double>((double) 0, (double) 0,
				(double) 0);
		Triple<Double, Double, Double> classWisePrecision = new Triple<Double, Double, Double>((double) 0, (double) 0,
				(double) 0);
		Triple<Double, Double, Double> classWiseRecall = new Triple<Double, Double, Double>((double) 0, (double) 0,
				(double) 0);

		for (Entry<String, String> entryG : groundtruth.entrySet())
		{
			String userID = entryG.getKey();
			String actualClass = entryG.getValue();

			String predictClass = getOneRandomlyIfMoreThanOne(prediction.get(userID), "__");// prediction.get(userID);
																							// //

			// switch (actualClass)
			// {
			// case "FirstCluster":
			// classWisePositives.setFirst(classWisePositives.getFirst() + 1);
			// case "ThirdCluster":
			// classWisePositives.setSecond(classWisePositives.getSecond() + 1);
			// case "SecondCluster":
			// classWisePositives.setThird(classWisePositives.getThird() + 1);
			// default:
			// PopUps.showError("Unrecognised cluster: " + actualClass + " in evaluateFusion()");
			// }

			if (actualClass.equals("FirstCluster") && predictClass.contains("FirstCluster"))
			{
				double oldVal = classWiseTruePositives.getFirst();
				classWiseTruePositives.setFirst(oldVal + 1);
			}
			if (actualClass.equals("ThirdCluster") && predictClass.contains("ThirdCluster"))
			{
				double oldVal = classWiseTruePositives.getSecond();
				classWiseTruePositives.setSecond(oldVal + 1);
			}
			if (actualClass.equals("SecondCluster") && predictClass.contains("SecondCluster"))
			{
				double oldVal = classWiseTruePositives.getThird();
				classWiseTruePositives.setThird(oldVal + 1);
			}

			// //////////////////////////////////////

			if (actualClass.equals("FirstCluster") && !predictClass.contains("FirstCluster"))
			{
				double oldVal = classWiseFalseNegatives.getFirst();
				classWiseFalseNegatives.setFirst(oldVal + 1);
			}
			if (actualClass.equals("ThirdCluster") && !predictClass.contains("ThirdCluster"))
			{
				double oldVal = classWiseFalseNegatives.getSecond();
				classWiseFalseNegatives.setSecond(oldVal + 1);
			}
			if (actualClass.equals("SecondCluster") && !predictClass.contains("SecondCluster"))
			{
				double oldVal = classWiseFalseNegatives.getThird();
				classWiseFalseNegatives.setThird(oldVal + 1);
			}

			// //////////////////////////////////////

			if (!actualClass.equals("FirstCluster") && !predictClass.contains("FirstCluster"))
			{
				double oldVal = classWiseTrueNegatives.getFirst();
				classWiseTrueNegatives.setFirst(oldVal + 1);
			}
			if (!actualClass.equals("ThirdCluster") && !predictClass.contains("ThirdCluster"))
			{
				double oldVal = classWiseTrueNegatives.getSecond();
				classWiseTrueNegatives.setSecond(oldVal + 1);
			}
			if (!actualClass.equals("SecondCluster") && !predictClass.contains("SecondCluster"))
			{
				double oldVal = classWiseTrueNegatives.getThird();
				classWiseTrueNegatives.setThird(oldVal + 1);
			}

			// //////////////////////////////////////

			if (!actualClass.equals("FirstCluster") && predictClass.contains("FirstCluster"))
			{
				double oldVal = classWiseFalsePositives.getFirst();
				classWiseFalsePositives.setFirst(oldVal + 1);
			}
			if (!actualClass.equals("ThirdCluster") && predictClass.contains("ThirdCluster"))
			{
				double oldVal = classWiseFalsePositives.getSecond();
				classWiseFalsePositives.setSecond(oldVal + 1);
			}
			if (!actualClass.equals("SecondCluster") && predictClass.contains("SecondCluster"))
			{
				double oldVal = classWiseFalsePositives.getThird();
				classWiseFalsePositives.setThird(oldVal + 1);
			}

		}

		Double sumOfTruePositives = classWiseTruePositives.getFirst() + classWiseTruePositives.getSecond()
				+ classWiseTruePositives.getThird();
		Double sumOfTrueNegatives = classWiseTrueNegatives.getFirst() + classWiseTrueNegatives.getSecond()
				+ classWiseTrueNegatives.getThird();
		Double sumOfFalsePositives = classWiseFalsePositives.getFirst() + classWiseFalsePositives.getSecond()
				+ classWiseFalsePositives.getThird();
		Double sumOfFalseNegatives = classWiseFalseNegatives.getFirst() + classWiseFalseNegatives.getSecond()
				+ classWiseFalseNegatives.getThird();

		// accuracy = (TP+TN)/(TP+FN+TN+FP) .... this is correct verified by paper DATA MINING FOR IMBALANCED DATASETS:
		// AN OVERVIEW by Nitesh V. Chawla
		Double firstClusterAccuracy = (classWiseTruePositives.getFirst() + classWiseTrueNegatives.getFirst())
				/ (classWiseTruePositives.getFirst() + classWiseFalseNegatives.getFirst()
						+ classWiseTrueNegatives.getFirst() + classWiseFalsePositives.getFirst());
		Double thirdClusterAccuracy = (classWiseTruePositives.getSecond() + classWiseTrueNegatives.getSecond())
				/ (classWiseTruePositives.getSecond() + classWiseFalseNegatives.getSecond()
						+ classWiseTrueNegatives.getSecond() + classWiseFalsePositives.getSecond());

		Double secondClusterAccuracy = (classWiseTruePositives.getThird() + classWiseTrueNegatives.getThird())
				/ (classWiseTruePositives.getThird() + classWiseFalseNegatives.getThird()
						+ classWiseTrueNegatives.getThird() + classWiseFalsePositives.getThird());

		Double overallAccuracy = (sumOfTruePositives + sumOfTrueNegatives)
				/ (sumOfTruePositives + sumOfFalsePositives + sumOfTrueNegatives + sumOfFalseNegatives);

		// precision = TP / (TP+FP) ... this is correct verified by paper DATA MINING FOR IMBALANCED DATASETS: AN
		// OVERVIEW by Nitesh V. Chawla
		double firstClusterPrecision = classWiseTruePositives.getFirst()
				/ (classWiseTruePositives.getFirst() + classWiseFalsePositives.getFirst());
		double thirdClusterPrecision = classWiseTruePositives.getSecond()
				/ (classWiseTruePositives.getSecond() + classWiseFalsePositives.getSecond());
		double secondClusterPrecision = classWiseTruePositives.getThird()
				/ (classWiseTruePositives.getThird() + classWiseFalsePositives.getThird());

		double overallPrecision = sumOfTruePositives / (sumOfTruePositives + sumOfFalsePositives);

		// recall = TP / (TP+FN) ... this is correct verified by paper DATA MINING FOR IMBALANCED DATASETS: AN OVERVIEW
		// by Nitesh V. Chawla
		double firstClusterRecall = classWiseTruePositives.getFirst()
				/ (classWiseTruePositives.getFirst() + classWiseFalseNegatives.getFirst());
		double thirdClusterRecall = classWiseTruePositives.getSecond()
				/ (classWiseTruePositives.getSecond() + classWiseFalseNegatives.getSecond());
		double secondClusterRecall = classWiseTruePositives.getThird()
				/ (classWiseTruePositives.getThird() + classWiseFalseNegatives.getThird());

		double overallRecall = sumOfTruePositives / (sumOfTruePositives + sumOfFalseNegatives);

		// Specificity or true negative = TN / (TN+FP)

		String msg1 = ("Results from evaluateFusion:" + fileNamePhrase + " " + titlePhrase + "\n");

		msg1 += "classWiseCorrectCount = " + classWiseTruePositives.toStringCSV();
		msg1 += "classWiseWrongCount = " + classWiseFalseNegatives.toStringCSV();

		int[][] cm = getConfusionMatrix(groundtruth, prediction);

		System.out.println(msg1);
		WToFile.appendLineToFileAbs("Evaluation: " + titlePhrase + "\n", pathToWrite + fileNamePhrase);

		WToFile.appendLineToFileAbs(confusionMatrixToString(cm) + "\n", pathToWrite + fileNamePhrase);
		// WritingToFile.appendLineToFileAbsolute(",FirstCluster, ThirdCluster,SecondCluster\n", pathToWrite +
		// fileNamePhrase);
		WToFile.appendLineToFileAbs("classWiseTruePositives , " + classWiseTruePositives.toStringCSV() + "\n",
				pathToWrite + fileNamePhrase);
		WToFile.appendLineToFileAbs("classWiseFalseNegatives , " + classWiseFalseNegatives.toStringCSV() + "\n",
				pathToWrite + fileNamePhrase);
		WToFile.appendLineToFileAbs("classWiseTrueNegatives , " + classWiseTrueNegatives.toStringCSV() + "\n",
				pathToWrite + fileNamePhrase);
		WToFile.appendLineToFileAbs("classWiseFalsePositives , " + classWiseFalsePositives.toStringCSV() + "\n",
				pathToWrite + fileNamePhrase);

		WToFile.appendLineToFileAbs("classWiseAccuracy , " + firstClusterAccuracy + "," + thirdClusterAccuracy + ","
				+ secondClusterAccuracy + "\n", pathToWrite + fileNamePhrase);

		WToFile.appendLineToFileAbs("OverAllAccuracy , " + overallAccuracy + "\n", pathToWrite + fileNamePhrase);

		WToFile.appendLineToFileAbs("classWisePrecision , " + firstClusterPrecision + "," + thirdClusterPrecision + ","
				+ secondClusterPrecision + "\n", pathToWrite + fileNamePhrase);

		WToFile.appendLineToFileAbs("OverAllPrecision , " + overallPrecision + "\n", pathToWrite + fileNamePhrase);

		WToFile.appendLineToFileAbs(
				"classWiseRecall , " + firstClusterRecall + "," + thirdClusterRecall + "," + secondClusterRecall + "\n",
				pathToWrite + fileNamePhrase);

		WToFile.appendLineToFileAbs("OverAllRecall , " + overallRecall + "\n", pathToWrite + fileNamePhrase);
		WToFile.appendLineToFileAbs("--,--,--,--\n", pathToWrite + fileNamePhrase);
		WToFile.appendLineToFileAbs("\n", pathToWrite + fileNamePhrase);
	}

	private String confusionMatrixToString(int[][] cm)
	{
		String s = "(GT)\\(Pred), FirstCluster,ThirdCluster,SecondCluster\n";
		for (int i = 0; i < 3; i++)
		{
			s += ",";
			for (int j = 0; j < 3; j++)
			{
				s += cm[i][j] + ",";
			}
			s += "\n";
		}
		return s;
	}

	private int[][] getConfusionMatrix(TreeMap<String, String> groundtruth, TreeMap<String, String> prediction)
	{
		int[][] cm = new int[3][3]; // FirstCluster, ThirdCLuster, SecondCluster, .... along row actucal, along column
									// predicted

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				cm[i][j] = 0;
			}
		}
		int count = 0;
		for (Entry<String, String> entryG : groundtruth.entrySet())
		{
			count += 1;
			System.out.println("Count check = " + count);

			if (count > 18)
			{
				PopUps.showError("Error in confusion matrix" + " more than 18 entries = " + groundtruth.size());
			}

			String userID = entryG.getKey();
			String actualClass = entryG.getValue();
			String predictClass = getOneRandomlyIfMoreThanOne(prediction.get(userID), "__");

			System.out.println(
					"User id: " + userID + " actualClass: " + actualClass + " predictedClass: " + predictClass);

			if (actualClass.equals("FirstCluster") && predictClass.contains("FirstCluster"))
			{
				cm[0][0] += 1;
			}

			if (actualClass.equals("FirstCluster") && predictClass.contains("ThirdCluster"))
			{
				cm[0][1] += 1;
			}

			if (actualClass.equals("FirstCluster") && predictClass.contains("SecondCluster"))
			{
				cm[0][2] += 1;
			}

			if (actualClass.equals("ThirdCluster") && predictClass.contains("FirstCluster"))
			{
				cm[1][0] += 1;
			}
			if (actualClass.equals("ThirdCluster") && predictClass.contains("ThirdCluster"))
			{
				cm[1][1] += 1;
			}

			if (actualClass.equals("ThirdCluster") && predictClass.contains("SecondCluster"))
			{
				cm[1][2] += 1;
			}

			if (actualClass.equals("SecondCluster") && predictClass.contains("FirstCluster"))
			{
				cm[2][0] += 1;
			}
			if (actualClass.equals("SecondCluster") && predictClass.contains("ThirdCluster"))
			{
				cm[2][1] += 1;
			}

			if (actualClass.equals("SecondCluster") && predictClass.contains("SecondCluster"))
			{
				cm[2][2] += 1;
			}
			System.out.println("cm state= \n" + confusionMatrixToString(cm) + "\n");
		}
		return cm;
	}

	/**
	 * If there are more than one predicted class and then pick one randonly
	 * 
	 * @param bigString
	 * @param delimiter
	 * @return
	 */
	private String getOneRandomlyIfMoreThanOne(String bigString, String delimiter)
	{
		String splitted[] = bigString.split(delimiter);

		int len = splitted.length;

		if (len == 1)
		{
			return bigString;
		}
		else
		{
			int randomNum = 0 + (int) (Math.random() * (len - 1));
			return splitted[randomNum];
		}

	}

	/**
	 * 
	 * @param sampEn2Prediction
	 * @param sampEn3Prediction
	 * @param kGramPrediction
	 * @param cluster1Vals
	 * @param cluster2Vals
	 * @param cluster3Vals
	 * @return
	 */
	private String getWeightedMajority(String sampEn2Prediction, String sampEn3Prediction, String kGramPrediction,
			double[] cluster1Vals, double[] cluster2Vals, double[] cluster3Vals)
	{
		Double wtForSampEn2 = null;
		switch (sampEn2Prediction)
		{
		case "FirstCluster":
			wtForSampEn2 = cluster1Vals[0];
			break;
		case "SecondCluster":
			wtForSampEn2 = cluster2Vals[0];
			break;
		case "ThirdCluster":
			wtForSampEn2 = cluster3Vals[0];
			break;
		default:
			String msg = "Error in getWeightedMajority: unrecgnised sampEn2Prediction: " + sampEn2Prediction;
			PopUps.showError(msg);
		}

		Double wtForSampEn3 = null;
		switch (sampEn3Prediction)
		{
		case "FirstCluster":
			wtForSampEn3 = cluster1Vals[1];
			break;
		case "SecondCluster":
			wtForSampEn3 = cluster2Vals[1];
			break;
		case "ThirdCluster":
			wtForSampEn3 = cluster3Vals[1];
			break;
		default:
			String msg = "Error in getWeightedMajority: unrecgnised sampEn3Prediction: " + sampEn3Prediction;
			PopUps.showError(msg);
		}

		Double wtForKGram = null;
		switch (kGramPrediction)
		{
		case "FirstCluster":
			wtForKGram = cluster1Vals[2];
			break;
		case "SecondCluster":
			wtForKGram = cluster2Vals[2];
			break;
		case "ThirdCluster":
			wtForKGram = cluster3Vals[2];
			break;
		default:
			String msg = "Error in getWeightedMajority: unrecgnised KGramPrediction: " + kGramPrediction;
			PopUps.showError(msg);
		}

		LinkedHashMap<String, Double> resultsOfWtdVoting = new LinkedHashMap<String, Double>();

		resultsOfWtdVoting.put("FirstCluster", (double) 0);
		resultsOfWtdVoting.put("SecondCluster", (double) 0);
		resultsOfWtdVoting.put("ThirdCluster", (double) 0);

		resultsOfWtdVoting.put(sampEn2Prediction, resultsOfWtdVoting.get(sampEn2Prediction) + wtForSampEn2);
		resultsOfWtdVoting.put(sampEn3Prediction, resultsOfWtdVoting.get(sampEn3Prediction) + wtForSampEn3);
		resultsOfWtdVoting.put(kGramPrediction, resultsOfWtdVoting.get(kGramPrediction) + wtForKGram);

		Triple<Double, Double, Double> finalDistribution = new Triple<Double, Double, Double>(
				resultsOfWtdVoting.get("FirstCluster"), resultsOfWtdVoting.get("ThirdCluster"),
				resultsOfWtdVoting.get("SecondCluster"));

		return getPredClassWithHighestConfFromDistribution(finalDistribution);
	}

	private String getMajority(String string1, String string2, String string3)
	{
		if (string1.equals(string2))
		{
			return string1;
		}

		else if (string2.equals(string3))
		{
			return string2;
		}

		else if (string1.equals(string3))
		{
			return string1;
		}

		else
		// (classLabel.length() <= 0) // do a random prediction
		{
			String s[] = { "FirstCluster__zz", "SecondCluster__zz", "ThirdCluster__zz" };

			int randomNum = 0 + (int) (Math.random() * 2);
			return s[randomNum];
		}

	}

	/**
	 * Return "__" separated class labels
	 * 
	 * @param dist
	 * @return
	 */
	private static String getPredClassWithHighestConfFromDistribution(Triple<Double, Double, Double> dist)
	{
		String classLabel = "";

		double a = dist.getFirst(), b = dist.getSecond(), c = dist.getThird();

		if (a >= b && a >= c)
		{
			classLabel += "FirstCluster__";
		}

		if (b >= c && b >= a)
		{
			classLabel += "ThirdCluster__";
		}

		if (c >= a && c >= b)
		{
			classLabel += "SecondCluster__";
		}

		if (classLabel.length() > 15)
		{
			System.out.println("Warning in getPredictedClassFromDistribution multiple classes with highest val");
		}

		if (classLabel.length() <= 0) // do a random prediction
		{
			String s[] = { "FirstCluster__zz", "SecondCluster__zz", "ThirdCluster__zz" };

			int randomNum = 0 + (int) (Math.random() * 2);
			return s[randomNum];
		}
		return classLabel;
	}

	/**
	 * 
	 * @param rec
	 * @return
	 */
	private static Triple<Double, Double, Double> getDistribution(CSVRecord rec)
	{
		Triple<Double, Double, Double> res = null;// new Triple<Double,Double,Double>():
		res = new Triple<Double, Double, Double>(getDoubleVal(rec.get(4)), getDoubleVal(rec.get(5)),
				getDoubleVal(rec.get(6)));
		return res;

	}

	/**
	 * Removed any asterix in the string
	 * 
	 * @param s
	 * @return
	 */
	public static Double getDoubleVal(String s)
	{
		return Double.valueOf(s.replaceAll("[*]", ""));
	}

	/**
	 * Return a chosen optimal MU for the given MU cluster class
	 * 
	 * @param clusterClass
	 * @return
	 */
	/**
	 * 
	 * @param clusterClass
	 * @param groundTruthLabel
	 *            eg. Clustering2MUTil30Maj
	 * @return
	 */
	public Integer classToExpectedOptimalMU(String clusterClass, String groundTruthLabel)
	{
		int expectedBestMU = -1;

		System.out.println("Inside classToExpectedOptimalMU: ground truth label = " + groundTruthLabel);

		if (groundTruthLabel.contains("Clustering0"))
		{
			switch (clusterClass)
			{
			case "FirstCluster": // note: MU in paper = MU here+1
				expectedBestMU = 1;// current activty obj + 1 ( = MU=2 in paper) ; // old =1
				break;
			case "SecondCluster":
				expectedBestMU = 3; // current activty obj + 1 ( = MU=4 in paper) ; // old =2
				break;
			case "ThirdCluster":
				expectedBestMU = 5; // current activty obj + 1 ( = MU=10 in paper) ; // old =20
				break;
			default:
				PopUps.showError(
						"Error in classToExpectedBestMU: unknown cluster class: clusterClass read =" + clusterClass);

			}
		}
		else if (groundTruthLabel.contains("Clustering1"))
		{
			switch (clusterClass)
			{
			case "FirstCluster": // note: MU in paper = MU here+1
				expectedBestMU = 0;// current activty obj + 1 ( = MU=2 in paper) ; // old =1
				break;
			case "SecondCluster":
				expectedBestMU = 2; // current activty obj + 1 ( = MU=4 in paper) ; // old =2
				break;
			case "ThirdCluster":
				expectedBestMU = 5; // current activty obj + 1 ( = MU=10 in paper) ; // old =20
				break;
			default:
				PopUps.showError(
						"Error in classToExpectedBestMU: unknown cluster class: clusterClass read =" + clusterClass);

			}
		}
		else if (groundTruthLabel.contains("Clustering2"))
		{
			switch (clusterClass)
			{
			case "FirstCluster": // note: MU in paper = MU here+1
				expectedBestMU = 1;// current activty obj + 1 ( = MU=2 in paper) ; // old =1
				break;
			case "SecondCluster":
				expectedBestMU = 3; // current activty obj + 1 ( = MU=4 in paper) ; // old =2
				break;
			case "ThirdCluster":
				expectedBestMU = 5; // current activty obj + 1 ( = MU=10 in paper) ; // old =20
				break;
			default:
				PopUps.showError(
						"Error in classToExpectedBestMU: unknown cluster class: clusterClass read =" + clusterClass);

			}
		}
		else
		{
			PopUps.showError(
					"Error in classToExpectedBestMU: unknown ground truth clustering label =" + groundTruthLabel);
		}
		return expectedBestMU;
	}

	// \\public TimelineWEKAClusteringController(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>
	// usersDayTimelinesAll)
	// {
	// String nameOfFeatureFile = "";
	// String typeOfClustering = "Classification";// "KMeans";// EMClustering KMeans
	//
	// try
	// {
	// PopUps.showMessage("Num of user timelnes recieved in weka clustering = " + usersDayTimelinesAll.size());
	//
	// String directoryToWrite = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/"//
	// TimelinesClustering/"
	// + Constant.getDatabaseName() + "_" + LocalDateTime.now().getMonth().toString().substring(0, 3) +
	// LocalDateTime.now().getDayOfMonth() + "_" + Constant.howManyUsers;
	// new File(directoryToWrite).mkdir();
	// pathToWrite = directoryToWrite + "/";
	//
	// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + typeOfClustering +
	// "ConsoleLog.txt");
	//
	// TimelinesFeatureExtraction featureExtraction = new TimelinesFeatureExtraction(usersDayTimelinesAll, pathToWrite);
	// nameOfFeatureFile = featureExtraction.getFeatureAbsoluteFileName();
	//
	// DataLoader dl = new DataLoader(nameOfFeatureFile, nameOfFeatureFile.substring(0, nameOfFeatureFile.length() - 4)
	// + ".arff");
	// String outputArffFile = dl.getArffFileName();
	// System.out.println("Output arff file is:" + outputArffFile);
	// // PopUps.showMessage("Exiting feature extraction");
	//
	// // predictOptimalMU(outputArffFile);
	// buildClassifier(outputArffFile);
	//
	// Constant.setClassifier(usedClassifier);
	//
	// J48 temp = Constant.getJ48Classifier();
	// System.out.println(" Is constant classifier okay = " + temp.toString());
	// // switch (typeOfClustering)
	// // {
	// // case "KMeans":
	// // KMeans kmeans = new KMeans(outputArffFile, pathToWrite + typeOfClustering + "Results.txt", 3);
	// // break;
	// // case "EMClustering":
	// // EMClustering emClustering = new EMClustering(outputArffFile, pathToWrite + typeOfClustering + "Results");
	// // break;
	// // default:
	// // System.err.println("Unknown clustering type: " + typeOfClustering);
	// // PopUps.showError("Unknown clustering type: " + typeOfClustering);
	// // }
	// consoleLogStream.close();
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }
	/*
	 * Scheme: weka.classifiers.trees.J48 -C 0.25 -M 2 Relation:
	 * TimelineFeatureVectors-weka.filters.unsupervised.attribute.Remove-R3,5-7,11-18,22-29,31-34,36-41,44-49-weka.
	 * filters.unsupervised.attribute .Remove-R7,10-weka.filters.unsupervised.attribute.Remove-R5,7
	 */

	/**
	 * building clasifier and evaluating it
	 * 
	 * @param inputAbsoluteFileName
	 */
	public void predictOptimalMU(String inputAbsoluteFileName)
	{
		System.out.println("Inside predictOptimalMU");
		// PopUps.showMessage("Inside predictOptimalMU");
		String resultToWrite = new String();

		try
		{
			Instances dataInstances = DataSource.read(inputAbsoluteFileName);
			dataInstances.setClassIndex(dataInstances.numAttributes() - 1);

			dataInstances = WekaUtilityBelt.removeAttributesByRangeList(dataInstances,
					"3,5-7,9,11-20,22-34,37-42,45-50");// 3,5-7,11-18,22-29,31-34,36-41,44-49");
			WekaUtilityBelt.writeArffAbsolute(dataInstances, this.pathToWrite + "PrunedTimelineFeatureVectors.arff");

			resultToWrite += ("\n--> Classification " + LocalDateTime.now().toString() + "\n");
			resultToWrite += dataInstances.toSummaryString();
			// PopUps.showMessage("\n--> Classification " + LocalDateTime.now().toString());
			// PopUps.showMessage("File location=" + this.pathToWrite + "ClassificationEvaluation.txt");
			// String[] options = new String[2];
			// options[0] = "-C 0.25";
			// options[1] = "-M 2";
			// PopUps.showMessage("Still here 1");
			J48 tree = new J48(); // new instance of tree
			// PopUps.showMessage("Still here 2: After tree");
			// tree.setOptions(options); // set the options
			String[] options = tree.getOptions();

			// PopUps.showMessage("Still here 2.5: After setting options " + Arrays.toString(options));
			tree.buildClassifier(dataInstances); // build classifier ////ERROR HERE
			resultToWrite += tree.globalInfo() + "\n";
			// PopUps.showMessage("Still here 3: After classifier");
			Evaluation eval = new Evaluation(dataInstances);

			eval.crossValidateModel(tree, dataInstances, 10, new Random(1));
			// PopUps.showMessage("Still here 4: After crossvalidation ");
			// PopUps.showMessage("Again File location=" + this.pathToWrite + "ClassificationEvaluation.txt");
			resultToWrite += "\n---------- Stratified 10-fold Cross-validation-------------------\n";
			resultToWrite += WekaUtilityBelt.getRelevantEvaluationMetricsAsString(eval);
			resultToWrite += eval.toSummaryString();
			WToFile.appendLineToFileAbs(resultToWrite, this.pathToWrite + "ClassificationEvaluation.txt");

		}
		catch (Exception e)
		{
			String exceptionMsg = e.getMessage();
			PopUps.showError(exceptionMsg);
			e.printStackTrace();
		}
	}

	/**
	 * NOT USED CURRENTLY Just prediction using the given classifier and users/instances for prediction.
	 * 
	 * @param usersForPrediction
	 *            user/instances to be predicted
	 * @param classifierToUser
	 * @param instances
	 *            all instances
	 * @return
	 */
	public LinkedHashMap<String, Double> predictOptimalMU(ArrayList<String> usersForPrediction,
			Classifier classifierToUser, Instances instances)
	{
		System.out.println("Inside predictOptimalMU");
		// PopUps.showMessage("Inside predictOptimalMU");
		String resultToWrite = new String();
		LinkedHashMap<Integer, Double> userSerialNumPredictedClass = new LinkedHashMap<Integer, Double>();
		LinkedHashMap<String, Double> userIDPredictedClass = new LinkedHashMap<String, Double>();
		userIDPredictedClassProbInt = new LinkedHashMap<String, List<Double>>();

		ArrayList<Integer> usersToConsiderAsInstancesIDs = userIDsToInstanceIDsList(usersForPrediction);

		try
		{
			for (Integer userInstanceID : usersToConsiderAsInstancesIDs)
			{
				double predictedClass = classifierToUser.classifyInstance(instances.get(userInstanceID - 1));
				userSerialNumPredictedClass.put(userInstanceID, predictedClass);
				userIDPredictedClass.put(instanceIDToUserID(userInstanceID), predictedClass);

				double[] dsitribution = classifierToUser.distributionForInstance(instances.get(userInstanceID - 1));

				Double[] doubleArray = ArrayUtils.toObject(dsitribution);
				List<Double> distributionList = Arrays.asList(doubleArray);

				userIDPredictedClassProbInt.put(instanceIDToUserID(userInstanceID), distributionList);
			}

		}
		catch (Exception e)
		{
			String exceptionMsg = e.getMessage();
			PopUps.showError(exceptionMsg);
			e.printStackTrace();
		}

		return userIDPredictedClass;
	}

	/**
	 * Converts a list of raw user IDs to a string containing the corresponding instance IDs
	 * 
	 * @param usersToUse
	 * @return
	 */
	public String userIDsToInstanceIDs(ArrayList<String> usersToUse)
	{
		String serials = new String();
		LinkedHashMap<String, Integer> map = attributeExtraction.getUserIDInstanceID();

		ArrayList<Integer> userSerialsInt = new ArrayList<Integer>(); // to sort user IDs, NOTE: idiosyncracy of weka
																		// instance filtering by range, range must be in
																		// ascending order
																		// or esle
																		// excption..not sure..to be checked later if
																		// its a cause, my current overnight error cause
																		// was double
																		// quotes surrounding
																		// the string
		// thrown

		for (String userID : usersToUse)
		{
			Integer serialNum = map.get(userID);
			userSerialsInt.add(serialNum);
		}

		Collections.sort(userSerialsInt);

		for (Integer serialNum : userSerialsInt)
		{
			serials += String.valueOf(serialNum) + ",";
		}

		serials = removeLastChar(serials);

		// PopUps.showMessage(serials);

		return serials;
	}

	public String instanceIDToUserID(int instanceID)
	{
		String userID = null;
		LinkedHashMap<String, Integer> map = attributeExtraction.getUserIDInstanceID();

		for (Map.Entry<String, Integer> entry : map.entrySet())
		{
			if (entry.getValue() == instanceID)
			{
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param usersToUse
	 * @return
	 */
	public ArrayList<Integer> userIDsToInstanceIDsList(ArrayList<String> usersToUse)
	{
		LinkedHashMap<String, Integer> map = attributeExtraction.getUserIDInstanceID();

		ArrayList<Integer> userSerialsInt = new ArrayList<Integer>(); // to sort user IDs, NOTE: idiosyncracy of weka
																		// instance filtering by range, range must be in
																		// ascending order
																		// or esle
																		// excption..not sure..to be checked later if
																		// its a cause, my current overnight error cause
																		// was double
																		// quotes surrounding
																		// the string
		// thrown

		for (String userID : usersToUse)
		{
			Integer instanceID = map.get(userID);
			userSerialsInt.add(instanceID);
		}

		Collections.sort(userSerialsInt);

		return userSerialsInt;
	}

	public String removeLastChar(String s)
	{
		if (s == null || s.length() == 0)
		{
			return s;
		}
		return s.substring(0, s.length() - 1);
	}

	/**
	 * Builds the classifier and evaluates using cross validation. (note: not checked for train-test split validation)
	 * and writes files (idForPreselectingSetOfAttributesOrPhrase + "ClassificationEvaluation.txt) and
	 * (idForPreselectingSetOfAttributesOrPhrase + "PredictedClasses.csv")
	 * <p>
	 * inputAbsoluteFileName: the data source file to be read for building classifier.// (usersToRemove) userids of
	 * users which should not be used for building classifier (useful for training-test split and not for
	 * cross-validation)
	 * 
	 * @param dataInstancesOriginal
	 * @param indexOfInstanceID
	 *            (an attribute)used as indentifier to tag instances. Index (starting from 1) of the attribute used to
	 *            identify each instance, e.g., user id when each instance represents a users. This attribute is not to
	 *            be used for building classifier, but should be retained in the dataset for identifying instances.
	 * @param idForPreselectingSetOfAttributesOrPhrase
	 * @param performAttributeSelection
	 * 
	 * @return
	 */
	// TODO: try to separate out cross validation as a different method. To keep in mind: that the data instances upon
	// which validation has to be done should also have only the
	// selected
	// attributes which had been selected using the attributes selection algorithm
	public Classifier buildClassifierAndCrossValidate(Instances dataInstancesOriginal, String indexOfInstanceID,
			String idForPreselectingSetOfAttributesOrPhrase, boolean performAttributeSelection)
	{
		System.out.println("Inside buildClassifier");
		// PopUps.showMessage("Inside predictOptimalMU");
		// PopUps.showMessage("Received " + dataInstancesOriginal.size() + " instances in
		// buildClassifierAndCrossValidate");
		String resultToWrite = new String();
		Instances dataInstances = new Instances(dataInstancesOriginal);
		J48 tree = null;
		// String namesOfAllAttributesRecieved;
		int numOfAllAttributesRecieved;
		try
		{
			numOfAllAttributesRecieved = dataInstancesOriginal.numAttributes();
			// /////////////Attribute selection start 16 dec
			if (performAttributeSelection)
			{
				dataInstances = performAttributeSelection(dataInstances, indexOfInstanceID);
			}
			// /////////////Attribute selection end 16 dec
			// /////////////Attribute selection start 7 dec
			// AttributeSelection attributeSelectionFilter = new AttributeSelection();
			//
			// CfsSubsetEval attributeSelectionEval = new CfsSubsetEval();
			// String optionsAttributeSelectionEval[] = new String[1];
			// optionsAttributeSelectionEval[0] = new String("-P 1");
			// attributeSelectionEval.setOptions(optionsAttributeSelectionEval); // starting set: keep the user id
			// attribute
			//
			// GreedyStepwise attributeSelectionSearch = new GreedyStepwise();
			// attributeSelectionSearch.setSearchBackwards(true);
			//
			// attributeSelectionFilter.setEvaluator(attributeSelectionEval);
			// attributeSelectionFilter.setSearch(attributeSelectionSearch);
			// attributeSelectionFilter.setInputFormat(dataInstances);
			//
			// dataInstances = Filter.useFilter(dataInstances, attributeSelectionFilter);
			// WekaUtilityBelt.writeArffAbsolute(dataInstances, this.pathToWrite +
			// "PrunedAttSelectedTimelineFeatureVectors.arff");
			// /////////////Attribute selection end 7 dec

			resultToWrite += ("\n--> Classification " + LocalDateTime.now().toString() + "\n");
			resultToWrite += ("idForPreselectingSetOfAttributes: " + idForPreselectingSetOfAttributesOrPhrase + "\n");
			resultToWrite += ("Num of attributes before algorithmic attribute selection: (inludes target attribute and instance id attribute)"
					+ numOfAllAttributesRecieved + "\n");

			resultToWrite += ("Index of Instance ID: " + indexOfInstanceID + "\n");
			resultToWrite += dataInstances.toSummaryString();
			// PopUps.showMessage("\n--> Classification " + LocalDateTime.now().toString());
			// PopUps.showMessage("File location=" + this.pathToWrite + "ClassificationEvaluation.txt");
			// String[] options = new String[2];
			// options[0] = "-C 0.25";
			// options[1] = "-M 2";
			// PopUps.showMessage("Still here 1");

			/**
			 * Filtering the InstanceID on the fly
			 */
			Remove removeID = new Remove();
			removeID.setAttributeIndices(indexOfInstanceID);// ("1");

			tree = new J48(); // new instance of tree
			// RandomForest tree = new RandomForest();
			// tree.setNumExecutionSlots(1000);
			// tree.setNumFeatures(5);
			// ///////
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(removeID);
			fc.setClassifier(tree);

			// Attribute selection

			// Attribute selection end
			// ///////////

			// PopUps.showMessage("Still here 2: After tree");
			// tree.setOptions(options); // set the options
			// ------------------------------------------------
			// String[] options = tree.getOptions();
			//
			// // PopUps.showMessage("Still here 2.5: After setting options " + Arrays.toString(options));
			// tree.buildClassifier(dataInstances); // build classifier /
			// resultToWrite += tree.globalInfo() + "\n";
			// ------------------------------------------------
			// PopUps.showMessage("Still here 3: After classifier");
			// ------------------------Changed from tree to fc------------------------
			// String[] options = fc.getOptions();
			// fc.buildClassifier(dataInstances); // build classifier using fc /
			resultToWrite += fc.globalInfo() + "\n";
			// ------------------------------------------------

			Evaluation eval = new Evaluation(dataInstances);

			StringBuffer predictionResults = new StringBuffer();
			CSV predictionsResultPlain = new CSV();

			predictionsResultPlain.setBuffer(predictionResults);
			predictionsResultPlain.setAttributes("first-last");
			predictionsResultPlain.setOutputDistribution(true);
			// predictionsResultPlain.se

			// Range attsToOutput = new Range("first-last");
			// Boolean outputDist = new Boolean(true);

			// PopUps.showMessage("predictionsResultPlain.getAttributes() = \n" +
			// predictionsResultPlain.getAttributes());
			// num of folds is same as dataInstances.size to make the evaluation as leave one out.
			eval.crossValidateModel(fc, dataInstances, dataInstances.size(), new Random(1), predictionsResultPlain);// ,
																													// attsToOutput,
																													// outputDist);
			// PopUps.showMessage("Still here 4: After crossvalidation ");

			resultToWrite += "\n---------- Leave one out Cross-validation-------------------\n";
			resultToWrite += WekaUtilityBelt.getRelevantEvaluationMetricsAsString(eval);

			resultToWrite += eval.toSummaryString();
			// resultToWrite += "\n Tree when run on all data: \n " + tree.graph();

			resultToWrite += "\n Tree when built using all data (without instance id) : \n "
					+ getTreeGraph(dataInstances, tree, 0); // build classifier using fc /

			resultToWrite += "\n" + predictionResults.toString() + "\n";

			WToFile.appendLineToFileAbs(resultToWrite,
					this.pathToWrite + idForPreselectingSetOfAttributesOrPhrase + "ClassificationEvaluation.txt");
			WToFile.appendLineToFileAbs(predictionResults.toString(),
					this.pathToWrite + idForPreselectingSetOfAttributesOrPhrase + "PredictedClasses.csv");
		}

		catch (Exception e)
		{
			String exceptionMsg = e.getMessage();
			e.printStackTrace();
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.buildClassifierAndCrossValidate(Instances, String, String, boolean)");
		}
		return tree;
	}

	/**
	 * Returns the tree graph for the given tree built on all data instances (without instance id) (Note: creates a copy
	 * of Instances to work with so as not affect the original instances)
	 * 
	 * @param dataInstances
	 * @param tree
	 * @param indexInstanceID
	 * @return
	 * @throws Exception
	 */
	public String getTreeGraph(Instances dataInstances, J48 tree, int indexInstanceID) throws Exception
	{
		Instances instanceWithoutInstID = new Instances(dataInstances);
		instanceWithoutInstID.remove(indexInstanceID);
		tree.buildClassifier(instanceWithoutInstID);
		// visualizeTree(tree.graph());
		return tree.graph();
	}

	// Not working correctly
	public void visualizeTree(String treeGraph) throws IOException
	{
		// display classifier
		final javax.swing.JFrame jf = new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
		jf.setSize(500, 400);
		jf.getContentPane().setLayout(new BorderLayout());
		TreeVisualizer tv = new TreeVisualizer(null, treeGraph, new PlaceNode2());
		jf.getContentPane().add(tv, BorderLayout.CENTER);

		// /SAVE as png
		Container c = jf.getContentPane();
		BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
		c.paint(im.getGraphics());
		ImageIO.write(im, "PNG", new File(pathToWrite + "Tree.png"));

		jf.addWindowListener(new java.awt.event.WindowAdapter()
			{
				public void windowClosing(java.awt.event.WindowEvent e)
				{
					jf.dispose();
				}
			});

		jf.setVisible(true);
		tv.fitToScreen();
	}

	/**
	 * Perform attribute selection and returns the data instances with selected attributes with instanceID and target
	 * class attributes. </br>
	 * <font color = "red">Note: if InstanceID is present it must be the first attribute (as the adjustment of indices
	 * in this method is dependent on this)</font>. </br>
	 * <font color = "red">Note: the target class is assumed to be at the end, i.e., the last attribute.</font>
	 * 
	 * @param originalDataInstances
	 * @param indexOfInstanceID
	 *            instanceID which has to be retained for tagging instances but should not be used for attribute
	 *            selection
	 * @return Instances with selected attributes
	 */
	private Instances performAttributeSelection(Instances originalDataInstances, String indexOfInstanceID)
	{
		// PopUps.showMessage("Ajooba");
		System.out.println("Inside performAttributeSelection()");
		Instances data = new Instances(originalDataInstances); // copying instances
		Instances dataWithSelectedAttributes = null; // to be returned;

		// It seems the targetID is selected by default
		// String indexOfTargetID = String.valueOf(originalDataInstances.numAttributes());

		if (indexOfInstanceID != null)
		{
			data = WekaUtilityBelt.removeAttributesByRangeList(data, indexOfInstanceID); // remove the instance id
																							// attribute. Note that this
																							// will cause a decrement of
																							// 1 in indices
																							// of remaining
																							// attributes. In current
																							// case, the instance ID is
																							// the first attribute.
		}
		// WekaUtilityBelt.writeArffAbsolute(data, this.pathToWrite + "WhileAttributeSelectionTimelineFVs.arff"); // FV
		// : feature vector

		try
		{

			// WrapperSubsetEval wrapperEvaluator = new WrapperSubsetEval();
			// wrapperEvaluator.setClassifier(new J48());
			// wrapperEvaluator.setFolds(data.size()); // leave 1 out cross validation
			//
			// String[] wrapperEvalOptions = { "-E auc" };
			// wrapperEvaluator.setOptions(wrapperEvalOptions);

			// wrapperEvaluator.setEvaluationMeasure(newMethod);

			// /////////////////////////////////////////////
			int[] indices = performWrapperJ48EvalGreedyStepwiseAttributeSelection(data);// performCfsSubsetGreedyStepwiseAttributeSelection(data);//
			WToFile.appendLineToFileAbs("all data together " + "," + Arrays.toString(indices) + "\n",
					pathToWrite + "CheckAttributeSelectionIsSameForFullAndLeaveOneOut.csv");
			// ////////////////////////////////////////////
			// ALERT ALERT ALERT ALERT
			// indices =
			checkAttributeSelectionIsSameForFullAndLeaveOneOut(data); // ALERT JUT CHECKING USING MORE ATTS
			// END OF ALERT ALERT ALERT
			// convert the indices with respect to the original data instances
			// PopUps.showMessage("before adjustment Index of Selected Attributes =" + Utils.arrayToString(indices));
			indices = StatsUtils.addConstantToEach(indices, 2); // adding 2, (1:compensating the deletion of
																// instanceid, 1: to compensating that the selected
																// indices start from 0
																// while here we
																// need them to begin at 1)

			String selectedAttributesAsString = (Utils.arrayToString(indices));
			// PopUps.showMessage("Adjusted Index of Selected Attributes =" + selectedAttributesAsString);

			selectedAttributesAsString = indexOfInstanceID + "," + selectedAttributesAsString; // + "," +
																								// indexOfTargetID;
			// PopUps.showMessage("Index of Attributes to keep =" + selectedAttributesAsString);

			dataWithSelectedAttributes = WekaUtilityBelt.selectAttributesByRangeList(originalDataInstances,
					selectedAttributesAsString);
			WekaUtilityBelt.writeArffAbsolute(dataWithSelectedAttributes,
					this.pathToWrite + "SelectedTimelineFVs.arff"); // FV : feature vector

		}
		catch (Exception e)
		{
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.performAttributeSelection(Instances, String)");
			e.printStackTrace();
		}
		return dataWithSelectedAttributes;
	}

	/**
	 * Selects attributes from the given data using CfsSubset Evaluator with Greedy Stepwise search.
	 * 
	 * @param data
	 * @return indices (starts from 0) of selected attributes wrt to the given data
	 */
	@SuppressWarnings("unused")
	private int[] performCfsSubsetGreedyStepwiseAttributeSelection(Instances data)
	{
		int[] indicesSelected = null;

		AttributeSelection attsel = new AttributeSelection(); // package weka.attributeSelection
		CfsSubsetEval eval = new CfsSubsetEval();
		GreedyStepwise search = new GreedyStepwise();
		search.setSearchBackwards(true);
		attsel.setEvaluator(eval);
		attsel.setSearch(search);
		try
		{
			attsel.SelectAttributes(data);
			indicesSelected = attsel.selectedAttributes();
		}
		catch (Exception e)
		{
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.performCfsSubsetGreedyStepwiseAttributeSelection(Instances)n");
			e.printStackTrace();
		}
		return indicesSelected;
	}

	/**
	 * Selects attributes from the given data using CfsSubset Evaluator with Greedy Stepwise search.
	 * 
	 * @param data
	 * @return indices (starts from 0) of selected attributes wrt to the given data
	 */
	private int[] performWrapperJ48EvalGreedyStepwiseAttributeSelection(Instances data)
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
			wrapperEvaluator.setFolds(10);
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

	/**
	 * Since we are not using separate training and test split. and Attribute selection should be performed using using
	 * training set only.<\br> In this method we will verify whether the attributes selected are same using full dataset
	 * Vs. full dataset -1 (corresponding to leave 1 out)
	 * 
	 * @param data
	 * @return set of all selected attributes over all the folds
	 */
	private int[] checkAttributeSelectionIsSameForFullAndLeaveOneOut(Instances data)
	{
		boolean same = true;

		ArrayList<int[]> indicesSelected = new ArrayList<int[]>();
		// PopUps.showMessage("Inside performWrapperJ48EvalGreedyStepwiseAttributeSelection");
		System.out.println("Inside checkAttributeSelectionIsSameForFullAndLeaveOneOut");
		try
		{
			for (int fold = 0; fold < data.size(); fold++)
			{
				int[] selectionForFold = performWrapperJ48EvalGreedyStepwiseAttributeSelection(
						WekaUtilityBelt.removeInstancesByRangeList(data, String.valueOf(fold + 1)));
				indicesSelected.add(selectionForFold);
			}

		}
		catch (Exception e)
		{
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.checkAttributeSelectionIsSameForFullAndLeaveOneOut(Instances)n");
			e.printStackTrace();
		}

		TreeSet<Integer> indicesSlectedOverAllFolds = new TreeSet<Integer>();

		for (int i = 0; i < indicesSelected.size(); i++)
		{
			WToFile.appendLineToFileAbs("fold " + i + "," + Arrays.toString(indicesSelected.get(i)) + "\n",
					pathToWrite + "CheckAttributeSelectionIsSameForFullAndLeaveOneOut.csv");
			for (int j = i; j < indicesSelected.size(); j++)
			{
				if (Arrays.equals(indicesSelected.get(i), indicesSelected.get(j)) == false) // check whether both arrays
																							// have same contents
					same = false;
			}

			for (int a : indicesSelected.get(i))
			{
				indicesSlectedOverAllFolds.add(a);
			}
		}
		System.out.println("indicesSlectedOverAllFolds = " + indicesSlectedOverAllFolds.toString());
		System.out.println("Exiting checkAttributeSelectionIsSameForFullAndLeaveOneOut");

		final Integer[] NO_INTS = new Integer[0];
		int[] indicesSlectedOverAllFoldsInt = ArrayUtils.toPrimitive(indicesSlectedOverAllFolds.toArray(NO_INTS));
		return indicesSlectedOverAllFoldsInt;
	}

	/**
	 * For Debugging Purpose only
	 * 
	 * @param args
	 */
	public static void mainBefore21Feb2019(String[] args)
	{
		int[] indicesSelected = null;
		AttributeSelection attsel = new AttributeSelection(); // package weka.attributeSelection
		try
		{

			// CfsSubsetEval eval = new CfsSubsetEval();
			WrapperSubsetEval wrapperEvaluator = new WrapperSubsetEval();
			wrapperEvaluator.setClassifier(new J48());

			// System.out.println("Tags Evaluation");
			// int count = 0;
			// SelectedTag evalAUCTag;
			// for (SelectedTag tag : WrapperSubsetEval.TAGS_EVALUATION)
			// {
			// if (++count == 5)
			// {
			// evalAUCTag = tag;
			// break;
			// }
			// // System.out.println("\t" + tag);
			// }

			// wrapperEvaluator.setEvaluationMeasure(newMethod);
			wrapperEvaluator.setEvaluationMeasure(
					new SelectedTag(WrapperSubsetEval.EVAL_AUC, WrapperSubsetEval.TAGS_EVALUATION));
			// wrapperEvaluator.setEvaluationMeasure(evalAUCTag);// WrapperSubsetEval.EVAL_AUC);
			// String[] wrapperEvalOptions = { "-E auc" };
			// wrapperEvaluator.setOptions(wrapperEvalOptions);

			System.out.println(
					"Changed Wrapper Evaluation Measure :" + wrapperEvaluator.getEvaluationMeasure().toString());// .getOptions().toString());//
																													// .globalInfo());

			System.out.println("Wrapper Evaluation Measure :" + wrapperEvaluator.getEvaluationMeasure().toString());// .getOptions().toString());//
																													// .globalInfo());
			System.out.println("Wrapper technical info :" + wrapperEvaluator.getTechnicalInformation());// .getOptions().toString());//
																										// .globalInfo());

			String[] options = wrapperEvaluator.getOptions();

			GreedyStepwise search = new GreedyStepwise();
			search.setSearchBackwards(true);
			attsel.setEvaluator(wrapperEvaluator);
			attsel.setSearch(search);

		}
		catch (Exception e)
		{
			PopUps.showException(e, "");// ,
										// "org.activity.clustering.weka.TimelineWEKAClusteringController.performCfsSubsetGreedyStepwiseAttributeSelection(Instances)n");
			e.printStackTrace();
		}

	}

	/*
	 * // * inst#,actual,predicted,error,prediction //1,3:SecondCluster,3:SecondCluster,,1
	 * //1,3:SecondCluster,3:SecondCluster,,1 //1,3:SecondCluster,1:FirstCluster,+,0.889 //...
	 */// get predicted classes for each instance from the file PredictedClasses written in BuildClassifier
	/**
	 * Sets userAsInstanceIDPredictedClass and userIDPredictedClass by reading from given
	 * idForPreselectingSetOfAttributes+"PredictedClasses.csv" </br>
	 * Note that, the file being read does not have users in the right (row) order, however that is taken care of
	 * because we are using the userID (1..18) in each row identify the user number and not using the row number
	 * 
	 * @param idForPreselectingSetOfAttributes
	 * @return
	 */
	private LinkedHashMap<String, String> setPredictedClasses(String idForPreselectingSetOfAttributes)
	{
		// PopUps.showMessage("Inside setPredictedClasses");
		String absoluteFileNameToRead = this.pathToWrite + idForPreselectingSetOfAttributes + "PredictedClasses.csv";

		userAsInstanceIDPredictedClass = new LinkedHashMap<Integer, String>();
		userIDPredictedClass = new LinkedHashMap<String, String>();

		try
		{
			List<CSVRecord> records = CSVUtils.getCSVRecords(absoluteFileNameToRead);
			// PopUps.showMessage("Number of records = " + records.size());

			int rowNumber = 0, numOfInconsistentRecords = 0;
			int colNumPredictedClass = -1, colNumUserID = -1;

			for (CSVRecord record : records)
			{
				// PopUps.showMessage(++instanceNumber + ": ");

				++rowNumber;
				if (rowNumber == 1)
				{
					colNumPredictedClass = getColumnNumberOfHeader("predicted", record);
					colNumUserID = getColumnNumberOfHeader("UserID", record);
					continue; // skipping header
				}

				// PopUps.showMessage("colNumPredictedClass= " + colNumPredictedClass + " colNumUserID=" +
				// colNumUserID);
				if (record.isConsistent() == false)
				{
					numOfInconsistentRecords += 1;
				}

				Iterator it = record.iterator();

				int col = 0; // column count starts from 1
				String predictedClass = "";
				int userAsInstanceID = -1;

				while (it.hasNext())
				{
					col++;
					String val = it.next().toString();
					System.out.println(">> col = " + col + "  val = " + val);

					if (col == colNumPredictedClass) // 3)// predicted class
					{
						// PopUps.showMessage("3rd col val = " + val);
						String[] splittedStrings = val.split(":");
						predictedClass = splittedStrings[1];
						System.out.println("predictedClass = " + predictedClass);
					}
					if (col == colNumUserID)// 8)// user serial number: user
					{
						// PopUps.showMessage("8th col val = " + val + " " + "row num = " + rowNumber);
						userAsInstanceID = Integer.parseInt(val.substring(4)); // User8 --> 8

						System.out.println(" 8th col val = " + val + " " + "row num = " + rowNumber);

						// System.out.println("Sanity Check in TimelineWEKAClusteringController.setPredictedClasses")
						// PopUps.showMessage("instance num = " + instanceNumber);
					}
				}
				// PopUps.showMessage("userSerial = " + userSerial + " predicted class =" + predictedClass);

				userAsInstanceIDPredictedClass.put(userAsInstanceID, predictedClass);
				userIDPredictedClass.put(instanceIDToUserID(userAsInstanceID), predictedClass);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e,
					"org.activity.clustering.weka.TimelineWEKAClusteringController.setPredictedClasses(String)");
		}

		// PopUps.showMessage("Exiting setPredictedClasses");
		return userIDPredictedClass;
	}

	/**
	 * Returns the column number (starting from 1) of the string 'header' in the given CSV record (row).
	 * 
	 * @param header
	 * @param rec
	 * @return column number (starting from 1) of the string 'header' in the given CSV record (row)
	 */
	private int getColumnNumberOfHeader(String header, CSVRecord rec)
	{
		int col = 0;
		Iterator iterator = rec.iterator();

		while (iterator.hasNext())
		{
			col++;
			String s = iterator.next().toString();
			// PopUps.showMessage(">> s = " + s + " header = " + header);
			if (s.equals(header))
			{
				// PopUps.showMessage("matched returning " + col);
				return col;
			}
		}

		PopUps.showError(
				"Error in org.activity.clustering.weka.TimelineWEKAClusteringController.getColumnNumberOfHeader(String, Iterator<String>)\nColumn number not found for header"
						+ header);

		return col;
	}
}

// if (actualClass.equals("FirstCluster"))// predictClass.contains(actualClass) &&
// {
// if (predictClass.contains(actualClass))
// {
// double oldVal = classWiseTruePositives.getFirst();
// classWiseTruePositives.setFirst(oldVal + 1);
// }
// else
// {
// double oldVal = classWiseFalseNegatives.getFirst();
// classWiseFalseNegatives.setFirst(oldVal + 1);
// }
// }
//
// if (!actualClass.equals("FirstCluster"))// predictClass.contains(actualClass) &&
// {
// if (!predictClass.contains(actualClass))
// {
// double oldVal = classWiseTrueNegatives.getFirst();
// classWiseTrueNegatives.setFirst(oldVal + 1);
// }
// else
// {
// double oldVal = classWiseFalsePositives.getFirst();
// classWiseFalsePositives.setFirst(oldVal + 1);
// }
// }
//
// if (!actualClass.equals("ThirdCluster")) // predictClass.contains(actualClass) &&
// {
// if (!predictClass.contains(actualClass))
// {
// double oldVal = classWiseTruePositives.getSecond();
// classWiseTruePositives.setSecond(oldVal + 1);
// }
// else
// {
// double oldVal = classWiseFalseNegatives.getSecond();
// classWiseFalseNegatives.setSecond(oldVal + 1);
//
// }
// }
// else if (actualClass.equals("SecondCluster"))// predictClass.contains(actualClass) &&
// {
// if (predictClass.contains(actualClass))
// {
// double oldVal = classWiseTruePositives.getThird();
// classWiseTruePositives.setThird(oldVal + 1);
// }
// else
// {
// double oldVal = classWiseFalseNegatives.getThird();
// classWiseFalseNegatives.setThird(oldVal + 1);
// }
// }
// }

// for (Entry<String, String> entryP : prediction.entrySet())
// {
// String userID = entryP.getKey();
// String predictClass = entryP.getValue();
//
// String actualClass = groundtruth.get(userID);
//
// if (predictClass.equals("FirstCluster"))// predictClass.contains(actualClass) &&
// {
// if (!predictClass.contains(actualClass))
// {
// double oldVal = classWiseFalsePositives.getFirst();
// classWiseFalsePositives.setFirst(oldVal + 1);
// }
// }
//
// else if (predictClass.equals("ThirdCluster")) // predictClass.contains(actualClass) &&
// {
// if (predictClass.contains(actualClass))
// {
// double oldVal = classWiseTruePositives.getSecond();
// classWiseTruePositives.setSecond(oldVal + 1);
// }
// else
// {
// double oldVal = classWiseFalsePositives.getSecond();
// classWiseFalsePositives.setSecond(oldVal + 1);
//
// }
// }
// else if (predictClass.equals("SecondCluster"))// predictClass.contains(actualClass) &&
// {
// if (predictClass.contains(actualClass))
// {
// double oldVal = classWiseTruePositives.getThird();
// classWiseTruePositives.setThird(oldVal + 1);
// }
// else
// {
// double oldVal = classWiseFalsePositives.getThird();
// classWiseFalsePositives.setThird(oldVal + 1);
// }
// }
// }