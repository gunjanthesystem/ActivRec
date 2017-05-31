package org.activity.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.clustering.Cluster;
import org.activity.clustering.KCentroids;
import org.activity.clustering.KCentroidsTimelines;
import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.distances.AlignmentBasedDistance;
import org.activity.distances.HJEditDistance;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.stats.entropy.SampleEntropyG;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.ConnectDatabase;
import org.activity.util.StringCode;
import org.activity.util.TimelineTransformers;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

//import com.google.uzaygezen.core.GunjanUtils;

/**
 * 
 * @author gunjan
 *
 */
public class TimelineStats
{
	// public static void main(String args[]) {
	// traverseHashMap(frequencyDistributionOfNGramSubsequences("ABACDAACCCDCDABDAB", 1));}
	public static final int intervalInSecs = 1, numOfClusters = 2, numOfKCentroidsExperiments = 25;

	/**
	 * Show only the top 5 clusterings based on intra-cluster variance. The tighter the clusters, the better the
	 * clustering.
	 */
	public static final boolean clusteringOnlyTop5 = false;

	static String pathToWrite;// , directoryToWrite;// =
								// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife
								// Data Works/stats/";

	// public static final boolean performNGramAnalysis = false;
	// public static final boolean performTimeSeriesAnalysis = false;
	/**
	 * ActivityRegularityAnalysisTwoLevel";// "ActivityRegularityAnalysisOneLevel";// "Clustering";// "Clustering";// //
	 * NGramAnalysis"; // "TimeSeriesAnalysis", "FeatureAnalysis"
	 */
	public static final String typeOfAnalysis = "NGramAnalysis";
	// "TimelineStats";// "NGramAnalysis";// "TimeSeriesCorrelationAnalysis";// "SampleEntropyPerMAnalysis";//
	// "SampleEntropyPerMAnalysis";//
	// "TimeSeriesAnalysis";// "AlgorithmicAnalysis2"; "Clustering";// "ClusteringTimelineHolistic";// "

	/**
	 * <UserID, num of activity-objects in timeline>
	 */
	// static LinkedHashMap<String, Timeline> userTimelines;

	/**
	 * 
	 * @param usersTimelines
	 */
	// @SuppressWarnings("unused")
	public static void
			timelineStatsController(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesAll)
	{
		Constant.setCommonPath(Constant.outputCoreResultsPath);
		// PopUps.showMessage("Inside timelineStats controller");
		String directoryToWrite = Constant.outputCoreResultsPath + Constant.getDatabaseName() + "_"
				+ LocalDateTime.now().getMonth().toString().substring(0, 3) + LocalDateTime.now().getDayOfMonth()
				+ typeOfAnalysis + "_" + Constant.howManyUsers;

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

		if (typeOfAnalysis.equals("Clustering"))
		{
			directoryToWrite += numOfClusters;
			if (clusteringOnlyTop5) directoryToWrite += "OnlyTop5Clusters";
		}

		new File(directoryToWrite).mkdir();
		pathToWrite = directoryToWrite + "/";
		Constant.setCommonPath(pathToWrite);
		PopUps.showMessage("path to write: " + pathToWrite);
		PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + "ConsoleLog.txt");
		// /////////////////////
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines =
				new LinkedHashMap<String, LinkedHashMap<Date, Timeline>>();
		if (!Constant.getDatabaseName().equals("gowalla1"))
		{

			writeNumOfActivityObjectsInTimelines(usersDayTimelinesAll, "NumOfActivityObjectsInUncleanedTimelines");
			writeNumOfValidActivityObjectsInTimelines(usersDayTimelinesAll,
					"NumOfValidActivityObjectsInUncleanedTimelines");
			writeNumOfDaysInTimelines(usersDayTimelinesAll, "NumOfDaysInUncleanedTimelines");
			writeAvgNumOfDistinctActsPerDayInTimelines(usersDayTimelinesAll,
					"AvgNumOfDistinctActsInUncleanedTimelines");
			writeAvgNumOfTotalActsPerDayInTimelines(usersDayTimelinesAll, "AvgNumOfTotalActsInUncleanedTimelines");
			// //////////////////

			usersDayTimelines = TimelineUtils.cleanDayTimelines(usersDayTimelinesAll);
			usersDayTimelines = TimelineUtils.rearrangeDayTimelinesOrderForDataset(usersDayTimelines);// UtilityBelt.dayTimelinesToCleanedExpungedRearrangedTimelines(usersDayTimelines);

			WritingToFile.writeUsersDayTimelines(usersDayTimelines, "users", true, true, true);// users
		}
		else
		{
			usersDayTimelines = usersDayTimelinesAll;
		}
		// usersDayTimelines = UtilityBelt.reformatUserIDs(usersDayTimelines);

		writeNumOfActivityObjectsInTimelines(usersDayTimelines, "NumOfActivityObjectsInCleanedTimelines");
		writeNumOfValidActivityObjectsInTimelines(usersDayTimelines, "NumOfValidActivityObjectsInCleanedTimelines");
		writeNumOfDaysInTimelines(usersDayTimelines, "NumOfDaysInCleanedTimelines");
		writeAvgNumOfDistinctActsPerDayInTimelines(usersDayTimelines, "AvgNumOfDistinctActsInCleanedTimelines");
		writeAvgNumOfTotalActsPerDayInTimelines(usersDayTimelines, "AvgNumOfTotalActsInCleanedTimelines");
		writeAllNumOfDistinctActsPerDayInTimelines(usersDayTimelines, "AllNumOfDistinctActsInCleanedTimelines");
		writeAllNumOfTotalActsPerDayInTimelines(usersDayTimelines, "AllNumOfTotalActsInCleanedTimelines");
		writeStatsNumOfDistinctActsPerDayInTimelines(usersDayTimelines, "StatsNumOfDistinctActsInCleanedTimelines");
		writeStatsNumOfTotalActsPerDayInTimelines(usersDayTimelines, "StatsNumOfTotalActsInCleanedTimelines");

		writeActivityStats(usersDayTimelines, "ActivityStats");
		switch (typeOfAnalysis)
		{
		/**
		 * For each user, for sequence of each features, get Sample Entropy vs m (segment length)
		 */

		case "TimeSeriesCorrelationAnalysis":
		{
			performTimeSeriesAnalysis(UtilityBelt.reformatUserIDs(usersDayTimelines));
			performTimeSeriesCorrelationAnalysis(UtilityBelt.reformatUserIDs(usersDayTimelines));
			break;
		}

		case "SampleEntropyPerMAnalysis":
		{
			performTimeSeriesAnalysis(UtilityBelt.reformatUserIDs(usersDayTimelines));
			// String pathForStoredTimelines
			performSampleEntropyVsMAnalysis2(UtilityBelt.reformatUserIDs(usersDayTimelines));
			break;
		}
		case "ClusteringTimelineHolistic": // applying Kcentroids with two-level edit distance
		{
			LinkedHashMap<String, Timeline> usersTimelines = TimelineUtils.dayTimelinesToTimelines(usersDayTimelines);
			LinkedHashMap<String, Timeline> usersTimelinesInvalidsExpunged =
					TimelineUtils.expungeInvalids(usersTimelines);

			applyKCentroidsTimelinesTwoLevel(usersTimelinesInvalidsExpunged);
			break;
		}

		case "ActivityRegularityAnalysisOneLevel":
		{
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> sequenceAll =
					TimelineTransformers.transformToSequenceDayWise(usersDayTimelines);
			LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS =
					TimelineTransformers.toCharsFromActivityObjectsNoTimestamp(sequenceAll, true);

			System.out.println(traverseHashMapStringString(sequenceCharInvalidsExpungedNoTS));

			// < User, ActivityName, MU, Avg pairwise distance of back-segments>
			LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>> regularityForEachTargetActivityOne =
					applyActivityRegularityAnalysisOneLevel(sequenceCharInvalidsExpungedNoTS);

			writeActivityRegularity(regularityForEachTargetActivityOne, typeOfAnalysis);
			break;
		}

		case "ActivityRegularityAnalysisTwoLevel":
		{
			LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>> regularityForEachTargetActivityTwo =
					applyActivityRegularityAnalysisTwoLevel(usersDayTimelines);
			// < User, ActivityName, MU, Avg pairwise distance of back-segments>
			writeActivityRegularity(regularityForEachTargetActivityTwo, typeOfAnalysis);
			break;
		}
		case "TimeSeriesAnalysis":
		{
			performTimeSeriesAnalysis(UtilityBelt.reformatUserIDs(usersDayTimelines));// UtilityBelt.reformatUserIDs(usersDayTimelines)
			break;
		}
		case "TimeSeriesEntropyAnalysis":// TimeSeriesAnalysis2
		{
			performTimeSeriesEntropyAnalysis(usersDayTimelines);
			break;
		}

		case "Clustering":
		{
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> sequenceAll =
					TimelineTransformers.transformToSequenceDayWise(usersDayTimelines);// , false);
			LinkedHashMap<String, LinkedHashMap<Timestamp, String>> sequenceCharInvalidsExpunged =
					TimelineTransformers.toCharsFromActivityObjects(sequenceAll, true);
			LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS =
					TimelineTransformers.toCharsFromActivityObjectsNoTimestamp(sequenceAll, true);

			applyKCentroids(sequenceCharInvalidsExpungedNoTS);
			break;
		}

		case "NGramAnalysis":
		{
			LinkedHashMap<String, Timeline> userTimelines = TimelineUtils.dayTimelinesToTimelines(usersDayTimelines);

			if (Constant.hasInvalidActivityNames)
			{
				userTimelines = TimelineUtils.expungeInvalids(userTimelines);
			}

			WritingToFile.writeSimpleLinkedHashMapToFile(getNumOfActivityObjectsInTimeline(userTimelines),
					pathToWrite + "UserNumOfActivityObjects.csv", "UserID", "NumOfActivityObjects");

			performNGramAnalysis(userTimelines, 1, 5/* 20 */, (pathToWrite));
			break;
		}

		case "AlgorithmicAnalysis":
		{
			LinkedHashMap<String, Timeline> userTimelines = TimelineUtils.dayTimelinesToTimelines(usersDayTimelines);
			userTimelines = TimelineUtils.expungeInvalids(userTimelines);

			performAlgorithmicAnalysis(userTimelines, 1, 20, (pathToWrite));
			break;
		}
		case "AlgorithmicAnalysis2":
		{
			LinkedHashMap<String, Timeline> userTimelines = TimelineUtils.dayTimelinesToTimelines(usersDayTimelines);
			userTimelines = TimelineUtils.expungeInvalids(userTimelines);

			performAlgorithmicAnalysis2(userTimelines, 1, 20, (pathToWrite));
			break;
		}
		case "FeatureAnalysis":
		{
			LinkedHashMap<String, Timeline> userTimelines = TimelineUtils.dayTimelinesToTimelines(usersDayTimelines);

			userTimelines = TimelineUtils.expungeInvalids(userTimelines);
			writeAllFeaturesValues(userTimelines, pathToWrite);
			break;
		}

		default:
		{
			System.err.println("Unknown typeOfAnalysis =" + typeOfAnalysis);
		}
		}
		consoleLogStream.close();
	}

	/**
	 * For each user, for sequence of each feature, get Sample Entropy vs m (segment length)
	 * 
	 * @param usersDayTimelines
	 */
	public static void
			performSampleEntropyVsMAnalysis(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines)
	{
		String path = Constant.getCommonPath();
		int numOfUser = usersDayTimelines.size();

		int mMin = 1;
		int mMax = 15;
		int mStep = 1;
		double rOriginal = 0.15d;
		// String[] inputFileNamePhrases =
		// {"activityNameSequenceIntInvalidsExpungedDummyTime","startTimeSequenceIntInvalidsExpungedDummyTime","durationSequenceIntInvalidsExpungedDummyTime","startGeoCoordinatesSequenceIntInvalidsExpungedDummyTime","endGeoCoordinatesSequenceIntInvalidsExpungedDummyTime","distanceTravelledSequenceIntInvalidsExpungedDummyTime","avgAltitudeSequenceIntInvalidsExpungedDummyTime"};
		String[] featureNames = Constant.getFeatureNames();

		LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<Integer, Double>>> userLevelSampEn =
				new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<Integer, Double>>>();
		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			String userIDN = entry.getKey(); // user id here is the user id formatted to be from 1 to Num of users

			LinkedHashMap<String, LinkedHashMap<Integer, Double>> featureLevelSampEn =
					new LinkedHashMap<String, LinkedHashMap<Integer, Double>>();
			for (String featureName : featureNames)
			{
				String absfileNameToRead = path + userIDN + featureName + "SequenceIntInvalidsExpungedDummyTime.csv";
				LinkedHashMap<Integer, Double> sampleEntropies = new LinkedHashMap<Integer, Double>();// < m , SampEn>

				double r;

				if (featureName.equalsIgnoreCase("ActivityName"))
					r = 0d;
				else
					r = rOriginal;

				for (int m = mMin; m <= mMax; m += mStep)
				{
					double valsTS[] = UtilityBelt.getTimeSeriesVals(absfileNameToRead);
					double sampleEntropy = SampleEntropyG.getSampleEntropyG(valsTS, m, r * StatsUtils.getSD(valsTS));
					if (Double.isInfinite(sampleEntropy) || Double.isNaN(sampleEntropy))
					{
						sampleEntropy = Double.NaN;// 12.33333;
					}
					sampleEntropies.put(m, sampleEntropy);
					System.out.println("user =" + userIDN + " featureName = " + featureName + " m = " + m
							+ " sample entropy = " + sampleEntropy);
				}
				featureLevelSampEn.put(featureName, sampleEntropies);
			}
			userLevelSampEn.put(userIDN, featureLevelSampEn);
		}

		traverseSampleEntropies(userLevelSampEn);
	}

	public static void
			traverseSampleEntropies(LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<Integer, Double>>> all)
	{
		System.out.println("Traversing sample entropies");
		for (Entry<String, LinkedHashMap<String, LinkedHashMap<Integer, Double>>> userEntry : all.entrySet())
		{
			String userIDN = userEntry.getKey();

			double[] sumSampEn = new double[Constant.getNumberOfFeatures()];
			double[] countValidSampEn = new double[Constant.getNumberOfFeatures()];

			for (int i = 0; i < Constant.getNumberOfFeatures(); i++)
			{
				sumSampEn[i] = 0d;
				countValidSampEn[i] = 0d;
			}

			for (Entry<String, LinkedHashMap<Integer, Double>> featureEntry : userEntry.getValue().entrySet())
			{
				String featureName = featureEntry.getKey();

				// String fileNameToUseForSum = userIDN + "SumSampEn";
				// String fileNameToUseForAvg = userIDN + "AvgSampEn";
				// double sampleEntropySum = 0;
				// double sampleEntropyAvg = 0;
				for (Entry<Integer, Double> mEntry : featureEntry.getValue().entrySet())
				{
					Integer m = mEntry.getKey();
					Double sampleEntropy = mEntry.getValue();
					System.out.println("user =" + userIDN + " featureName = " + featureName + " m = " + m
							+ " sample entropy = " + sampleEntropy);

					String toWrite = m + "," + sampleEntropy + "\n";
					String fileNameToUse = userIDN + featureName + "SampEn";
					WritingToFile.appendLineToFile(toWrite, fileNameToUse);
				}
				// String toWriteS = m + "," + sampleEntropySum + "\n";
				// WritingToFile.appendLineToFile(toWriteS, fileNameToUseForSum);
			}
		}
	}

	/**
	 * Must be preceeded by org.activity.stats.TimelineStats.performTimeSeriesAnalysis(LinkedHashMap<String,
	 * LinkedHashMap<Date, Timeline>>)
	 * 
	 * @param usersDayTimelines
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>>
			performSampleEntropyVsMAnalysis2(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines)
	{
		String path = Constant.getCommonPath();
		int numOfUser = usersDayTimelines.size();

		int mMin = 1;
		int mMax = 15;
		int mStep = 1;
		double rOriginal = 0.15d;
		// String[] inputFileNamePhrases =
		// {"activityNameSequenceIntInvalidsExpungedDummyTime","startTimeSequenceIntInvalidsExpungedDummyTime","durationSequenceIntInvalidsExpungedDummyTime","startGeoCoordinatesSequenceIntInvalidsExpungedDummyTime","endGeoCoordinatesSequenceIntInvalidsExpungedDummyTime","distanceTravelledSequenceIntInvalidsExpungedDummyTime","avgAltitudeSequenceIntInvalidsExpungedDummyTime"};
		String[] featureNames = Constant.getFeatureNames();

		LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> userLevelSampEn =
				new LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>>();
		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			String userIDN = entry.getKey(); // user id here is the user id formatted to be from 1 to Num of users

			LinkedHashMap<Integer, LinkedHashMap<String, Double>> mLevelSampEn =
					new LinkedHashMap<Integer, LinkedHashMap<String, Double>>();

			for (int m = mMin; m <= mMax; m += mStep)
			{
				LinkedHashMap<String, Double> sampleEntropies = new LinkedHashMap<String, Double>();// < m , SampEn>

				for (String featureName : featureNames)
				{
					String absfileNameToRead =
							path + userIDN + featureName + "SequenceIntInvalidsExpungedDummyTime.csv";

					double r;

					if (featureName.equalsIgnoreCase("ActivityName"))
						r = 0d;
					else
						r = rOriginal;

					double valsTS[] = UtilityBelt.getTimeSeriesVals(absfileNameToRead);
					double sampleEntropy = SampleEntropyG.getSampleEntropyG(valsTS, m, r * StatsUtils.getSD(valsTS));

					if (Double.isInfinite(sampleEntropy) || Double.isNaN(sampleEntropy))
					{
						sampleEntropy = Double.NaN;// 12.33333;
					}
					sampleEntropies.put(featureName, sampleEntropy);
					// System.out.println("user =" + userIDN + " featureName = " + featureName + " m = " + m + " sample
					// entropy = " + sampleEntropy);
				}
				mLevelSampEn.put(new Integer(m), sampleEntropies);
			}
			userLevelSampEn.put(userIDN, mLevelSampEn);
		}

		traverseSampleEntropies2(userLevelSampEn);
		traverseSampleEntropiesAggregate(userLevelSampEn);

		return userLevelSampEn;
	}

	public static void
			traverseSampleEntropies2(LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> all)
	{
		System.out.println("Traversing sample entropies2");
		for (Entry<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> userEntry : all.entrySet())
		{
			String userIDN = userEntry.getKey();

			double[] sumSampEn = new double[Constant.getNumberOfFeatures()];
			double[] countValidSampEn = new double[Constant.getNumberOfFeatures()];

			for (int i = 0; i < Constant.getNumberOfFeatures(); i++)
			{
				sumSampEn[i] = 0d;
				countValidSampEn[i] = 0d;
			}

			for (Entry<Integer, LinkedHashMap<String, Double>> mEntry : userEntry.getValue().entrySet())
			{
				Integer m = mEntry.getKey();// String featureName =
				// String fileNameToUseForSum = userIDN + "SumSampEn";
				// String fileNameToUseForAvg = userIDN + "AvgSampEn";
				// double sampleEntropySum = 0;
				// double sampleEntropyAvg = 0;
				for (Entry<String, Double> featureEntry : mEntry.getValue().entrySet())
				{
					Double sampleEntropy = featureEntry.getValue();
					// System.out.println("user =" + userIDN + " featureName = " + featureEntry.getValue() + " m = " + m
					// + " sample entropy = " + sampleEntropy);

					String toWrite = m + "," + sampleEntropy + "\n";
					String fileNameToUse = userIDN + featureEntry.getKey() + "SampEn";
					WritingToFile.appendLineToFile(toWrite, fileNameToUse);
				}
				// String toWriteS = m + "," + sampleEntropySum + "\n";
				// WritingToFile.appendLineToFile(toWriteS, fileNameToUseForSum);
			}
		}
	}

	public static void traverseSampleEntropiesAggregate(
			LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> all)
	{
		System.out.println("Traversing sample entropies2");
		for (Entry<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> userEntry : all.entrySet())
		{
			String userIDN = userEntry.getKey();

			// double[] sumSampEn = new double[Constant.getNumberOfFeatures()];
			// double[] countValidSampEn = new double[Constant.getNumberOfFeatures()];
			//
			// for (int i = 0; i < Constant.getNumberOfFeatures(); i++)
			// {
			// sumSampEn[i] = 0d;
			// countValidSampEn[i] = 0d;
			// }
			String fileNameToUseForSum = userIDN + "SumSampEn";
			String fileNameToUseForAvg = userIDN + "AvgSampEn";
			String fileNameToUseForStdDev = userIDN + "StdDevSampEn";
			String fileNameToUseForMedian = userIDN + "MedianSampEn";
			String fileNameToUseForApache = userIDN + "SampEnCalculationsApache";

			for (Entry<Integer, LinkedHashMap<String, Double>> mEntry : userEntry.getValue().entrySet())
			{
				Integer m = mEntry.getKey();

				WritingToFile.appendLineToFile(m + ",", fileNameToUseForApache);

				double sampleEntropySum = 0, sampleEntropyMedian = 0, sampleEntropyStdDev = 0;
				double countValid = 0;
				// double[] sampleEntropyVals = new double[mEntry.getValue().size()];
				ArrayList<Double> sampleEntropyVals = new ArrayList<Double>();

				int i = 0;
				for (Entry<String, Double> featureEntry : mEntry.getValue().entrySet())
				{
					Double sampleEntropy = featureEntry.getValue();
					// sampleEntropyVals[i] = sampleEntropy;
					WritingToFile.appendLineToFile(sampleEntropy + ",", fileNameToUseForApache);

					if ((sampleEntropy.isNaN() || sampleEntropy.isInfinite()) == true)
					{
						continue;
					}
					else
					{
						sampleEntropyVals.add(sampleEntropy);
						sampleEntropySum += sampleEntropy;
						countValid += 1;
					}
					i++;
				}

				DescriptiveStatisticsG ds = new DescriptiveStatisticsG(sampleEntropyVals);

				String toWriteS = m + "," + sampleEntropySum + "\n";
				WritingToFile.appendLineToFile(toWriteS, fileNameToUseForSum);

				String toWriteAvg = m + "," + sampleEntropySum / countValid + "\n";
				WritingToFile.appendLineToFile(toWriteAvg, fileNameToUseForAvg);

				WritingToFile.appendLineToFile(m + "," + ds.getStandardDeviation() + "\n", fileNameToUseForStdDev);
				WritingToFile.appendLineToFile(m + "," + ds.getPercentile(50) + "\n", fileNameToUseForMedian);

				WritingToFile.appendLineToFile(ds.getSum() + ",", fileNameToUseForApache);
				WritingToFile.appendLineToFile(ds.getMean() + ",", fileNameToUseForApache);
				WritingToFile.appendLineToFile(ds.getStandardDeviation() + ",", fileNameToUseForApache);
				WritingToFile.appendLineToFile(ds.getPercentile(50) + ",", fileNameToUseForApache);
				WritingToFile.appendLineToFile("\n", fileNameToUseForApache);
			}
		}
	}

	public static LinkedHashMap<String, LinkedHashMap<Pair<String, String>, Double>>
			performTimeSeriesCorrelationAnalysis(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines)
	{
		String path = Constant.getCommonPath();
		int numOfUser = usersDayTimelines.size();

		PearsonsCorrelation pc = new PearsonsCorrelation();
		String[] featureNames = Constant.getFeatureNames();

		LinkedHashMap<String, LinkedHashMap<Pair<String, String>, Double>> featSeriesCorr =
				new LinkedHashMap<String, LinkedHashMap<Pair<String, String>, Double>>();

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			String userIDN = entry.getKey(); // user id here is the user id formatted to be from 1 to Num of users
			LinkedHashMap<Pair<String, String>, Double> featSeriesCorrUserLevel =
					new LinkedHashMap<Pair<String, String>, Double>();

			for (int i = 0; i < featureNames.length; i++)
			{
				for (int j = i + 1; j < featureNames.length; j++)
				{
					String absfileNameToRead1 =
							path + userIDN + featureNames[i] + "SequenceIntInvalidsExpungedDummyTime.csv";
					double valsTS1[] = UtilityBelt.getTimeSeriesVals(absfileNameToRead1);
					String absfileNameToRead2 =
							path + userIDN + featureNames[j] + "SequenceIntInvalidsExpungedDummyTime.csv";
					double valsTS2[] = UtilityBelt.getTimeSeriesVals(absfileNameToRead2);

					double correlation = pc.correlation(valsTS1, valsTS2);

					featSeriesCorrUserLevel.put(new Pair(featureNames[i], featureNames[j]), correlation);
				}
			}
			featSeriesCorr.put(userIDN, featSeriesCorrUserLevel);
		}

		traverseTimeSeriesCorrelation(featSeriesCorr);
		return featSeriesCorr;

	}

	private static void traverseTimeSeriesCorrelation(
			LinkedHashMap<String, LinkedHashMap<Pair<String, String>, Double>> featSeriesCorr)
	{
		String fileNameToUse = "TimeSeriesFeatureCorrelation";
		String[] featureNames = Constant.getFeatureNames();

		WritingToFile.appendLineToFile("User", fileNameToUse);
		for (int i = 0; i < featureNames.length; i++)
		{
			for (int j = i + 1; j < featureNames.length; j++)
			{
				String msg = "," + featureNames[i] + "-" + featureNames[j];
				WritingToFile.appendLineToFile(msg, fileNameToUse);
			}
		}
		WritingToFile.appendLineToFile("\n", fileNameToUse);

		for (Map.Entry<String, LinkedHashMap<Pair<String, String>, Double>> entry : featSeriesCorr.entrySet())
		{
			String userIDN = entry.getKey();
			WritingToFile.appendLineToFile(userIDN, fileNameToUse);
			for (Map.Entry<Pair<String, String>, Double> entryUserLevel : entry.getValue().entrySet())
			{
				WritingToFile.appendLineToFile("," + entryUserLevel.getValue(), fileNameToUse);
			}
			WritingToFile.appendLineToFile("\n", fileNameToUse);
		}
	}

	/**
	 * 
	 * @param usersDayTimelines
	 *            already cleaned and rearranged
	 */
	public static void performTimeSeriesAnalysis(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines)
	{
		// usersDayTimelines = UtilityBelt.reformatUserIDs(usersDayTimelines); relocated to when calling this method
		// LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> timeSeries =
		// transformToEqualIntervalTimeSeriesDayWise(usersDayTimelines, intervalInSecs);
		// LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> timeSeriesInt =
		// toIntsFromActivityObjects(timeSeries, false);
		// LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> timeSeriesIntZeroValuedInvalids =
		// toTimeSeriesIntWithZeroValuedInvalids(timeSeries);
		// LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> timeSeriesIntInvalidsExpunged =
		// toIntsFromActivityObjects(timeSeries, true);
		// usersDayTimelines = UtilityBelt.reformatUserIDs(usersDayTimelines);
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> sequenceAll =
				TimelineTransformers.transformToSequenceDayWise(usersDayTimelines);// , false);
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> sequenceInt =
				TimelineTransformers.toIntsFromActivityObjects(sequenceAll, false);
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> sequenceIntZeroValuedInvalids =
				TimelineTransformers.toTimeSeriesIntWithZeroValuedInvalids(sequenceAll);
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> sequenceIntInvalidsExpunged =
				TimelineTransformers.toIntsFromActivityObjects(sequenceAll, true);
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> activityNameSequenceIntInvalidsExpungedDummyTime =
				TimelineTransformers.toIntsFromActivityObjectsDummyTime(sequenceAll, true);
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> durationSequenceIntInvalidsExpungedDummyTime =
				TimelineTransformers.toDurationsFromActivityObjectsDummyTime(sequenceAll, true);
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> startTimesSequenceIntInvalidsExpungedDummyTime =
				TimelineTransformers.toStartTimeFromActivityObjectsDummyTime(sequenceAll, true);

		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> startGeoSequenceIntInvalidsExpungedDummyTime = null,
				endGeoSequenceIntInvalidsExpungedDummyTime = null;
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> distanceTravelledIntInvalidsExpungedDummyTime = null,
				startAltitudeIntInvalidsExpungedDummyTime = null, endAltitudeIntInvalidsExpungedDummyTime = null,
				avgAltitudeIntInvalidsExpungedDummyTime = null;

		if (Constant.getDatabaseName().equals("geolife1"))
		{
			startGeoSequenceIntInvalidsExpungedDummyTime =
					TimelineTransformers.toStartGeoCoordinatesFromActivityObjectsDummyTime(sequenceAll, true);
			endGeoSequenceIntInvalidsExpungedDummyTime =
					TimelineTransformers.toEndGeoCoordinatesFromActivityObjectsDummyTime(sequenceAll, true);
			distanceTravelledIntInvalidsExpungedDummyTime =
					TimelineTransformers.toDistanceTravelledFromActivityObjectsDummyTime(sequenceAll, true);
			startAltitudeIntInvalidsExpungedDummyTime =
					TimelineTransformers.toStartAltitudeFromActivityObjectsDummyTime(sequenceAll, true);
			endAltitudeIntInvalidsExpungedDummyTime =
					TimelineTransformers.toEndAltitudeFromActivityObjectsDummyTime(sequenceAll, true);
			avgAltitudeIntInvalidsExpungedDummyTime =
					TimelineTransformers.toAvgAltitudeFromActivityObjectsDummyTime(sequenceAll, true);
		}
		// LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS =
		// toCharsFromActivityObjectsNoTimestamp(sequenceAll, true);
		// LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS =
		// toCharsFromActivityObjectsNoTimestamp(sequenceAll, true);

		// WritingToFile.writeAllTimestampedActivityObjects(timeSeries, "Time" + intervalInSecs + "Series");
		// WritingToFile.writeAllTimeSeriesInt(timeSeriesInt, "Time" + intervalInSecs + "SeriesInt");
		// WritingToFile.writeAllTimeSeriesInt(timeSeriesIntZeroValuedInvalids, "Time" + intervalInSecs +
		// "SeriesIntZeroValuedInvalids");
		// WritingToFile.writeAllTimeSeriesInt(timeSeriesIntInvalidsExpunged, "Time" + intervalInSecs +
		// "SeriesIntInvalidsExpunged");

		WritingToFile.writeAllTimestampedActivityObjects(sequenceAll, "Sequence");
		WritingToFile.writeAllTimeSeriesInt(sequenceInt, "SequenceInt");
		WritingToFile.writeAllTimeSeriesInt(sequenceIntZeroValuedInvalids, "SequenceIntZeroValuedInvalids ");
		WritingToFile.writeAllTimeSeriesInt(sequenceIntInvalidsExpunged, "SequenceIntInvalidsExpunged");

		WritingToFile.writeAllTimeSeriesInt(activityNameSequenceIntInvalidsExpungedDummyTime,
				"ActivityNameSequenceIntInvalidsExpungedDummyTime");
		WritingToFile.writeAllTimeSeriesLong(durationSequenceIntInvalidsExpungedDummyTime,
				"DurationSequenceIntInvalidsExpungedDummyTime");
		WritingToFile.writeAllTimeSeriesLong(startTimesSequenceIntInvalidsExpungedDummyTime,
				"StartTimeSequenceIntInvalidsExpungedDummyTime");

		if (Constant.getDatabaseName().equals("geolife1"))
		{
			WritingToFile.writeAllTimeSeriesLong(startGeoSequenceIntInvalidsExpungedDummyTime,
					"StartGeoCoordinatesSequenceIntInvalidsExpungedDummyTime");
			WritingToFile.writeAllTimeSeriesLong(endGeoSequenceIntInvalidsExpungedDummyTime,
					"EndGeoCoordinatesSequenceIntInvalidsExpungedDummyTime");
			WritingToFile.writeAllTimeSeriesDouble(distanceTravelledIntInvalidsExpungedDummyTime,
					"DistanceTravelledSequenceIntInvalidsExpungedDummyTime");
			WritingToFile.writeAllTimeSeriesDouble(startAltitudeIntInvalidsExpungedDummyTime,
					"StartAltitudeSequenceIntInvalidsExpungedDummyTime");
			WritingToFile.writeAllTimeSeriesDouble(endAltitudeIntInvalidsExpungedDummyTime,
					"EndAltitudeSequenceIntInvalidsExpungedDummyTime");
			WritingToFile.writeAllTimeSeriesDouble(avgAltitudeIntInvalidsExpungedDummyTime,
					"AvgAltitudeSequenceIntInvalidsExpungedDummyTime");

		}
		// WritingToFile.writeAllTimeSeriesOnlyIntValue(sequenceIntInvalidsExpunged, "SequenceIntInvalidsExpunged");
		// WritingToFile.writeLinkedHashMap(sequenceCharInvalidsExpungedNoTS, "SequenceAsString");
		// traverse(transformToEqualIntervalTimeSeries(userTimelines, 1));
	}

	/**
	 * 
	 * @param usersDayTimelines
	 */
	public static void
			performTimeSeriesEntropyAnalysis(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> timeSeries =
				TimelineTransformers.transformToEqualIntervalTimeSeriesDayWise(usersDayTimelines, intervalInSecs);
		// LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> timeSeriesInt = toTimeSeriesInt(timeSeries, false);
		// LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> timeSeriesIntZeroValuedInvalids =
		// toTimeSeriesIntWithZeroValuedInvalids(timeSeries);
		LinkedHashMap<String, LinkedHashMap<Timestamp, String>> timeSeriesCharInvalidsExpunged =
				TimelineTransformers.toCharsFromActivityObjects(timeSeries, true);
		LinkedHashMap<String, String> timeSeriesCharInvalidsExpungedNoTS =
				TimelineTransformers.toCharsFromActivityObjectsNoTimestamp(timeSeries, true);
		LinkedHashMap<String, Double> tsEntropy = getShannonEntropy(timeSeriesCharInvalidsExpungedNoTS);// , true);

		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> sequenceAll =
				TimelineTransformers.transformToSequenceDayWise(usersDayTimelines);// , false);
		// LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> sequenceInt = toTimeSeriesInt(sequenceAll, false);
		// LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> sequenceIntZeroValuedInvalids =
		// toTimeSeriesIntWithZeroValuedInvalids(sequenceAll);
		LinkedHashMap<String, LinkedHashMap<Timestamp, String>> sequenceCharInvalidsExpunged =
				TimelineTransformers.toCharsFromActivityObjects(sequenceAll, true);
		LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS =
				TimelineTransformers.toCharsFromActivityObjectsNoTimestamp(sequenceAll, true);
		LinkedHashMap<String, Double> seqEntropy = getShannonEntropy(sequenceCharInvalidsExpungedNoTS);

		WritingToFile.writeAllTimestampedActivityObjects(timeSeries, "Time" + intervalInSecs + "Series");

		// WritingToFile.writeAllTimeSeriesInt(timeSeriesInt, "Time" + intervalInSecs + "SeriesInt");
		WritingToFile.writeAllTimeSeriesChar(timeSeriesCharInvalidsExpunged,
				"Time" + intervalInSecs + "SeriesCharInvalidsExpunged");
		// WritingToFile.writeAllTimeSeriesInt(timeSeriesIntInvalidsExpunged, "Time" + intervalInSecs +
		// "SeriesIntInvalidsExpunged");

		// WritingToFile.writeAllTimeSeriesChar(sequenceChar, "SequenceChar");
		// WritingToFile.writeAllTimeSeriesChar(sequenceCharZeroValuedInvalids, "SequenceCharZeroValuedInvalids ");
		WritingToFile.writeAllTimeSeriesChar(sequenceCharInvalidsExpunged, "SequenceCharInvalidsExpunged");

		WritingToFile.writeShannonEntropy(tsEntropy, "TSShannonEntropy");
		WritingToFile.writeShannonEntropy(seqEntropy, "SeqShannonEntropy");
		// traverse(transformToEqualIntervalTimeSeries(userTimelines, 1));

	}

	/**
	 * 
	 * @param usersDayTimelines
	 * @return
	 */
	private static LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>>
			applyActivityRegularityAnalysisTwoLevel(
					LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines)
	{
		LinkedHashMap<String, Timeline> allTimelines = new LinkedHashMap<String, Timeline>();
		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			Timeline allDatesTimelineCombined = TimelineUtils.dayTimelinesToATimeline(entry.getValue(), false, true);
			Timeline newTimeline = TimelineUtils.expungeInvalids(allDatesTimelineCombined);
			// new Timeline(entry.getValue()));
			allTimelines.put(entry.getKey(), newTimeline);
		}

		LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>> regularityForEachTargetActivity =
				new LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>>();

		for (Map.Entry<String, Timeline> entry : allTimelines.entrySet())
		{
			Timeline timeline = entry.getValue();
			System.out.println("User " + entry.getKey());// + ":" + entry.getValue());

			String[] activityNames = Constant.getActivityNames();

			LinkedHashMap<String, TreeMap<Double, Double>> activityNamesMap =
					new LinkedHashMap<String, TreeMap<Double, Double>>();

			for (String activityName : activityNames)
			{
				if (UtilityBelt.isValidActivityName(activityName))
				{
					double[] MUs = Constant.matchingUnitAsPastCount;
					TreeMap<Double, Double> mapOfAvgEDPerMU = new TreeMap<Double, Double>();
					for (Double mu : MUs)
					{
						double avgPairwiseED = getAvgPairwiseTwoLevelEditDistance(mu, activityName, timeline);
						mapOfAvgEDPerMU.put(mu, avgPairwiseED);
					}
					activityNamesMap.put(activityName, mapOfAvgEDPerMU);
				}
			}
			regularityForEachTargetActivity.put(entry.getKey(), activityNamesMap);
		}
		return regularityForEachTargetActivity;
	}

	/**
	 * 
	 * @param sequenceCharInvalidsExpungedNoTS
	 */
	private static LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>>
			applyActivityRegularityAnalysisOneLevel(LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS)
	{
		// <User, ActityName, matching unit, AvgPariwiseEditDistance>
		LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>> regularityForEachTargetActivity =
				new LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>>();

		for (Map.Entry<String, String> entry : sequenceCharInvalidsExpungedNoTS.entrySet())
		{
			String timelinesAsString = entry.getValue();
			System.out.println("User " + entry.getKey());// + ":" + entry.getValue());

			String[] activityNames = Constant.getActivityNames();

			LinkedHashMap<String, TreeMap<Double, Double>> activityNamesMap =
					new LinkedHashMap<String, TreeMap<Double, Double>>();
			for (String activityName : activityNames)
			{
				if (UtilityBelt.isValidActivityName(activityName))
				{
					double[] MUs = Constant.matchingUnitAsPastCount;
					TreeMap<Double, Double> mapOfAvgEDPerMU = new TreeMap<>();
					for (Double mu : MUs)
					{
						double avgPairwiseED =
								getAvgPairwiseFirstLevelEditDistance(mu, activityName, timelinesAsString);
						mapOfAvgEDPerMU.put(mu, avgPairwiseED);
					}
					activityNamesMap.put(activityName, mapOfAvgEDPerMU);
				}
			}
			regularityForEachTargetActivity.put(entry.getKey(), activityNamesMap);
		}

		return regularityForEachTargetActivity;
		// writeActivityRegularity(regularityForEachTargetActivity, "ActivityRegularity");
	}

	/**
	 * 
	 * @param regularityForEachTargetActivity
	 * @param fileNamePhrase
	 */
	private static void writeActivityRegularity(
			LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>> regularityForEachTargetActivity,
			String fileNamePhrase)
	{
		for (Entry<String, LinkedHashMap<String, TreeMap<Double, Double>>> userEntry : regularityForEachTargetActivity
				.entrySet())
		{
			int user = Constant.getIndexOfUserID(Integer.valueOf(userEntry.getKey())) + 1;

			LinkedHashMap<String, TreeMap<Double, Double>> activitiesMap = userEntry.getValue();
			WritingToFile.appendLineToFile(
					"ActivityName" + "," + "MU" + ","
							+ "AvgPairwiseEditDistanceOfSegments,AvgPairwiseEditDistanceOfSegments/MU" + "\n",
					user + fileNamePhrase);
			for (Entry<String, TreeMap<Double, Double>> activityEntry : activitiesMap.entrySet())
			{
				String activityName = activityEntry.getKey();

				TreeMap<Double, Double> muEntries = activityEntry.getValue();

				for (Entry<Double, Double> muEntry : muEntries.entrySet())
				{
					double mu = muEntry.getKey();
					double avgPairwiseEditDistanceOfSegments = StatsUtils.round(muEntry.getValue(), 4);
					double upon;
					if (mu == 0)
					{
						upon = 0;
					}
					else
						upon = (avgPairwiseEditDistanceOfSegments / mu);
					WritingToFile.appendLineToFile(
							activityName + "," + mu + "," + avgPairwiseEditDistanceOfSegments + "," + upon + "\n",
							user + fileNamePhrase);
				}
			}
			// System.out.println("User " + entry.getKey());// + ":" + entry.getValue());
		}
	}

	/**
	 * 
	 * @param mu
	 * @param activityName
	 * @param timelineAsString
	 * @return
	 */
	private static double getAvgPairwiseFirstLevelEditDistance(Double mu, String activityName, String timelineAsString)
	{
		System.out.println("Inside getAvgPairwiseEditDistance");
		ArrayList<String> segments = new ArrayList<String>();
		String stringCodeForActivity = StringCode.getStringCodeFromActivityName(activityName);

		for (int i = 0; i < timelineAsString.length(); i++)
		{
			String character = Character.toString(timelineAsString.charAt(i));

			if (stringCodeForActivity.equals(character))
			{
				int startIndex = (int) Math.max(0, i - mu);
				int endIndex = i + 1;// exclusive

				segments.add(timelineAsString.substring(startIndex, endIndex));
			}
		}
		System.out.println("traversing segments for matching unit:" + mu + "  activity name " + activityName
				+ " activityCode=" + stringCodeForActivity);
		for (int j = 0; j < segments.size(); j++)
		{
			System.out.println(segments.get(j));
		}
		System.out.println("---------");

		System.out.println("exiting getAvgPairwiseEditDistance");

		return getAvgPairwiseED(segments);
	}

	/**
	 * Returns a list of trails of ActivityObjects ending at just before the given beginIndices and stretching back mu
	 * units.
	 * 
	 * @param muInCounts
	 * @param beginIndices
	 * @param timeline
	 * @return
	 */
	private static ArrayList<ArrayList<ActivityObject>> getTrailSegments(Double muInCounts,
			ArrayList<Integer> beginIndices, Timeline timeline)
	{
		// System.out.println("Inside getTrailSegments");
		ArrayList<ArrayList<ActivityObject>> segments = new ArrayList<ArrayList<ActivityObject>>();

		for (Integer endIndex : beginIndices)
		{
			// NOTE: going back matchingUnitCounts FROM THE index.
			int newStartIndex = (int) ((endIndex - muInCounts) >= 0 ? (endIndex - muInCounts) : 0);
			ArrayList<ActivityObject> activityObjectsForSegment =
					timeline.getActivityObjectsInTimelineFromToIndex(newStartIndex, endIndex + 1);
			segments.add(activityObjectsForSegment);
			// System.out.println("Num of elements in this segment = " + activityObjectsForSegment.size());
		}

		// System.out.println("Trail segments are: ");
		// for (int j = 0; j < segments.size(); j++)
		// {
		// System.out.println(segments.get(j));
		// }
		// System.out.println("---------");

		if (beginIndices.size() != segments.size())
		{
			ComparatorUtils.assertEquals(beginIndices.size(), segments.size());
		}
		// System.out.println("exiting getTrailSegments");

		return segments;
	}

	/**
	 * 
	 * @param mu
	 * @param activityName
	 * @param timeline
	 * @return
	 */
	private static double getAvgPairwiseTwoLevelEditDistance(Double mu, String activityName, Timeline timeline)
	{
		System.out
				.println("Inside getAvgPairwiseTwoLevelEditDistance for activity name: " + activityName + " mu:" + mu);
		ArrayList<ArrayList<ActivityObject>> segments = new ArrayList<ArrayList<ActivityObject>>();
		String stringCodeForActivity = StringCode.getStringCodeFromActivityName(activityName);

		for (int i = 0; i < timeline.size(); i++)
		{
			ActivityObject current = timeline.getActivityObjectAtPosition(i);
			String stringCodeForCurrent = StringCode.getStringCodeFromActivityName(current.getActivityName());

			if (stringCodeForCurrent.equals(stringCodeForActivity))
			{
				int startIndex = (int) Math.max(0, i - mu);
				int endIndex = i + 1;// exclusive

				segments.add(timeline.getActivityObjectsInTimelineFromToIndex(startIndex, endIndex));// timelineAsString.substring(startIndex,
																										// endIndex));
			}
		}
		// System.out.println("traversing segments for matching unit:" + mu + " activity name " + activityName + "
		// activityCode=" + stringCodeForActivity);
		// for (int j = 0; j < segments.size(); j++)
		// {
		// System.out.println(segments.get(j));
		// }
		// System.out.println("---------");

		System.out.println("exiting getAvgPairwiseTwoLevelEditDistance");

		return getAvgPairwiseTwoLevelED(segments);
	}

	/**
	 * Returns the avg pariwise two-level edit distance between the segments (Arrays of ActivityObjects). NOTE: CONTAINS
	 * COMPARISON TO SELF.
	 * 
	 * @param segments
	 * @return
	 */
	private static double getAvgPairwiseTwoLevelED(ArrayList<ArrayList<ActivityObject>> segments)
	{
		// System.out.println("Inside getAvgPairwiseTwoLevelED");
		int count = 0;
		double sumOfDistances = 0;
		for (int i = 0; i < segments.size(); i++)
		{
			for (int j = i; j < segments.size(); j++)// TODO SHOULD I START FROM J=I+1
			{
				HJEditDistance hjDist = new HJEditDistance();
				Pair<String, Double> dist =
						hjDist.getHJEditDistanceWithTrace(segments.get(i), segments.get(j), "", "", "", "1");// new
																												// Long(1)
				sumOfDistances += (dist.getSecond());// , 1, 1, 2);
				count++;
			}
		}
		// System.out.println("Exiting getAvgPairwiseTwoLevelED");
		return (sumOfDistances / count);
	}

	/**
	 * Returns the avg pariwise two-level edit distance between the segments (Arrays of ActivityObjects)
	 * 
	 * @param segments
	 * @return
	 */
	private static double[] getPairwiseTwoLevelEDs(ArrayList<ArrayList<ActivityObject>> segments)
	{
		// System.out.println("Inside getAvgPairwiseTwoLevelED");
		if (segments.size() == 1) // only one segment
			return new double[] { 0 };

		int count = 0;
		// int numOfPairs = (int) (CombinatoricsUtils.factorial(segments.size()) / (CombinatoricsUtils.factorial(2) *
		// CombinatoricsUtils.factorial(segments.size() - 2))); // n!/
		// 2!*(n-2)!
		ArrayList<Double> distances = new ArrayList<Double>();// new double[numOfPairs];

		for (int i = 0; i < segments.size(); i++)
		{
			for (int j = i + 1; j < segments.size(); j++)// should i START FROM J=I+1
			{
				HJEditDistance hjDist = new HJEditDistance();
				Pair<String, Double> dist =
						hjDist.getHJEditDistanceWithTrace(segments.get(i), segments.get(j), "", "", "", "1");// new
																												// Pair("a",
																												// new
																												// Double(2));//
																												// ALERT
																												// ALERT
																												// ALTER
																												// ALERT
																												// ALERT
				distances.add(dist.getSecond());// , 1, 1, 2);
				count++;
			}
		}

		if (distances.size() <= 2)
		{
			System.out.println("Inside getPairwiseTwoLevelEDs: num of pair comparisons = " + distances.size() + " for "
					+ segments.size() + " segments");
		}
		// UtilityBelt.assertEquals(numOfPairs, count);
		// System.out.println("Exiting getAvgPairwiseTwoLevelED");
		double[] res = new double[distances.size()];

		int k = 0;
		for (Double d : distances)
		{
			res[k++] = d;
		}
		return res;
	}

	/**
	 * Returns the avg pairwise first-level edit distance between the segments (Arrays of activity-name codes) NOTE:
	 * CONTAINS COMPARISON TO SELF.
	 * 
	 * @param segments
	 * @return
	 */
	private static double getAvgPairwiseED(ArrayList<String> segments)
	{
		int count = 0;
		double sumOfDistances = 0;
		for (int i = 0; i < segments.size(); i++)
		{
			for (int j = i; j < segments.size(); j++)
			{
				sumOfDistances += AlignmentBasedDistance.getMySimpleLevenshteinDistanceWithoutTrace(segments.get(i),
						segments.get(j), 1, 1, 2);
				count++;
			}
		}
		return (sumOfDistances / count);
	}

	private static void applyKCentroids(LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS)
	{
		int numberOfClusters = numOfClusters;
		int count = 0;

		String fileNameForWrite = "KCentroid" + numberOfClusters + "ClustersResults";// , numberOfClusters
		KCentroids.writeHeaderForResultsFile(fileNameForWrite, numberOfClusters);

		for (; numberOfClusters <= Constant.getUserIDs().length - 1; numberOfClusters++)
		{
			if (count == 0)
			{
				KCentroids.setCreateDistancesMap(true); // only create distance map once as same data points are used
			}

			KCentroids.writeToResultsFile("\nFor num of clusters = " + numberOfClusters + "\n", fileNameForWrite);
			/**
			 * NOT REALLY ABLE TO TAKE ADVANTAGE OF MAP, CANT MATCH UNIQUELY USING KEY, also issues with using HashSet
			 * as key
			 **/
			LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> mapOfResults =
					new LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>>();

			for (int i = 0; i < numOfKCentroidsExperiments; i++)
			{
				KCentroids kcentroids = new KCentroids(numberOfClusters, 50, sequenceCharInvalidsExpungedNoTS);
				KCentroids.setCreateDistancesMap(false);

				ArrayList<Cluster> clusters = (kcentroids.getListOfClusters());
				int numOfUpDates = kcentroids.getNumOfUpdates();
				ArrayList<Double> clusterQualityInfo = kcentroids.getClusterQualityInfo();

				String newNumOfUpdates = "";
				Integer newNumOfResults = -1;

				// if (mapOfResults.containsKey(clusters))
				if (containsCluster(mapOfResults, clusters, clusterQualityInfo))
				{
					System.out.println("Eureka: clustering alreadin map");
					// newNumOfUpdates = mapOfResults.get(clusters).getFirst() + "_" + String.valueOf(numOfUpDates);
					// newNumOfResults = mapOfResults.get(clusters).getSecond() + 1;
					newNumOfUpdates = getIt(mapOfResults, clusters).getFirst() + "_" + String.valueOf(numOfUpDates);
					newNumOfResults = getIt(mapOfResults, clusters).getSecond() + 1;
					mapOfResults = removeIt(mapOfResults, clusters);
				}
				else
				{
					newNumOfUpdates = "_" + String.valueOf(numOfUpDates);
					newNumOfResults = 1;
				}
				// mapOfResults.remove(clusters);
				mapOfResults.put(clusters, new Triple(newNumOfUpdates, newNumOfResults, clusterQualityInfo));
			}
			mapOfResults = sortByIntraClusterVarianceAndFilter(mapOfResults);
			writeKCentroidResults(mapOfResults, fileNameForWrite);
		}
	}

	private static void applyKCentroidsTimelinesTwoLevel(LinkedHashMap<String, Timeline> timelinesInvalidsExpunged)
	{
		int numberOfClusters = numOfClusters;
		int count = 0;

		String fileNameForWrite = "KCentroid" + numberOfClusters + "ClustersResults";// , numberOfClusters
		KCentroids.writeHeaderForResultsFile(fileNameForWrite, numberOfClusters);

		for (; numberOfClusters <= Constant.getUserIDs().length - 1; numberOfClusters++)
		{
			if (count == 0)
			{
				KCentroids.setCreateDistancesMap(true); // only create distance map once as same data points are used
			}

			KCentroids.writeToResultsFile("\nFor num of clusters = " + numberOfClusters + "\n", fileNameForWrite);
			/**
			 * NOT REALLY ABLE TO TAKE ADVANTAGE OF MAP, CANT MATCH UNIQUELY USING KEY, also issues with using HashSet
			 * as key
			 **/
			LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> mapOfResults =
					new LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>>();

			for (int i = 0; i < numOfKCentroidsExperiments; i++)
			{
				KCentroidsTimelines kcentroids =
						new KCentroidsTimelines(numberOfClusters, 50, timelinesInvalidsExpunged);
				KCentroids.setCreateDistancesMap(false);

				ArrayList<Cluster> clusters = (kcentroids.getListOfClusters());
				int numOfUpDates = kcentroids.getNumOfUpdates();
				ArrayList<Double> clusterQualityInfo = kcentroids.getClusterQualityInfo();

				String newNumOfUpdates = "";
				Integer newNumOfResults = -1;

				// if (mapOfResults.containsKey(clusters))
				if (containsCluster(mapOfResults, clusters, clusterQualityInfo))
				{
					System.out.println("Eureka: clustering alreadin map");
					// newNumOfUpdates = mapOfResults.get(clusters).getFirst() + "_" + String.valueOf(numOfUpDates);
					// newNumOfResults = mapOfResults.get(clusters).getSecond() + 1;
					newNumOfUpdates = getIt(mapOfResults, clusters).getFirst() + "_" + String.valueOf(numOfUpDates);
					newNumOfResults = getIt(mapOfResults, clusters).getSecond() + 1;
					mapOfResults = removeIt(mapOfResults, clusters);
				}
				else
				{
					newNumOfUpdates = "_" + String.valueOf(numOfUpDates);
					newNumOfResults = 1;
				}
				// mapOfResults.remove(clusters);
				mapOfResults.put(clusters, new Triple(newNumOfUpdates, newNumOfResults, clusterQualityInfo));
			}
			mapOfResults = sortByIntraClusterVarianceAndFilter(mapOfResults);
			writeKCentroidResults(mapOfResults, fileNameForWrite);
		}
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unused")
	public static LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>>
			sortByIntraClusterVarianceAndFilter(
					LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> map)
	{
		List<Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>>> list =
				new LinkedList<>(map.entrySet());

		Collections.sort(list,
				new Comparator<Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>>>()
					{
						@Override
						public int compare(Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> o1,
								Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> o2)
						{
							return (o1.getValue().getThird().get(0).compareTo(o2.getValue().getThird().get(0)));// (o1.getValue()).compareTo(o2.getValue());
						}
					});

		LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> result = new LinkedHashMap<>();

		int count = 0;
		for (Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> entry : list)
		{
			count++;
			if (clusteringOnlyTop5)
			{
				if (count <= 5)
				{
					result.put(entry.getKey(), entry.getValue());
				}
			}
			else
			{
				result.put(entry.getKey(), entry.getValue());
			}

		}
		return result;
	}

	/**
	 * Subsititute for remove as the remove of HashMap is not working here
	 * 
	 * @param mapOfResults
	 * @param clusteringToCheck
	 * @return
	 */
	public static LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> removeIt(
			LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> mapOfResults,
			ArrayList<Cluster> clusteringToCheck)
	{
		LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> mapOfNewResults =
				new LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>>();

		for (Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> entry : mapOfResults.entrySet())
		{
			ArrayList<Cluster> clustering = entry.getKey();

			if (clustering.containsAll(clusteringToCheck) && clusteringToCheck.containsAll(clustering))
			{
				System.out.println("Eureka: will remove duplicate");
			}
			else
			{
				mapOfNewResults.put(entry.getKey(), entry.getValue());
			}

		}
		return mapOfNewResults;
	}

	/**
	 * get to fetch by key as get from HashMap is not working here
	 * 
	 * @param mapOfResults
	 * 
	 * @param clusteringToCheck
	 * @return
	 */
	public static Triple<String, Integer, ArrayList<Double>> getIt(
			LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> mapOfResults,
			ArrayList<Cluster> clusteringToCheck)
	{
		// LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> mapOfNewResults = new
		// LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer,
		// ArrayList<Double>>>();

		for (Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> entry : mapOfResults.entrySet())
		{
			ArrayList<Cluster> clustering = entry.getKey();

			if (clustering.containsAll(clusteringToCheck) && clusteringToCheck.containsAll(clustering))
			{
				System.out.println("successfull getit");
				return entry.getValue();
			}
		}
		return null;
	}

	public static boolean containsCluster(
			LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> mapOfResults,
			ArrayList<Cluster> clusteringToCheck, ArrayList<Double> clusteringToCheckQuality)
	{
		boolean res = false;
		// System.out.println("Current map of results-----------");
		// for (Map.Entry<Set<Cluster>, Triple<String, Integer, ArrayList<Double>>> entry : mapOfResults.entrySet())
		// {
		// Set<Cluster> clusters = entry.getKey();
		// clusters.stream().forEachOrdered(c -> System.out.println(c.toString()));
		// }

		for (Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> entry : mapOfResults.entrySet())
		{
			ArrayList<Cluster> clustering = entry.getKey();

			if (clustering.containsAll(clusteringToCheck) && clusteringToCheck.containsAll(clustering))
			{
				System.out.println("Eureka: clustering alreadin map");
				res = true;
			}
			// else
			// {
			// // System.out.println("clustering NOT alreadin map");
			// }

			// System.out.println("Variance: " + entry.getValue().getThird().get(0) + " and " +
			// clusteringToCheckQuality.get(0));
			// if (entry.getValue().getThird().get(0).equals(clusteringToCheckQuality.get(0)))
			// {
			// System.out.println("Both clustering have same intra cluster variance");
			// System.out.println(clustering.toString() + "\n and \n" + clusteringToCheck.toString());
			// System.out.println("comparing again" + clustering.containsAll(clusteringToCheck));
			// }
			// if (clusters.containsAll(clustersToCheck))
			// {
			// System.out.println("Alert2! cluster in map contains: " + clustersToCheck.toString());
			// return true;
			// }
			//
			// if (clusters.equals(clustersToCheck))
			// {
			// System.out.println("Alert! cluster in map contains: " + clustersToCheck.toString());
			// return true;
			// }
			// else
			// {
			// System.out.println("Ohh! cluster in map does NOT contains:");// + clustersToCheck.toString());
			// System.out.println(clusters.toString() + " is not same as " + clustersToCheck.toString());
			// System.out.println(clusters.toString().equals(clustersToCheck.toString()));
			// }
		}
		// System.out.println("end Current map of results----------");
		return res;

	}

	/**
	 * 
	 * @param mapOfResults
	 * @param fileNameForWrite
	 */
	public static void writeKCentroidResults(
			LinkedHashMap<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> mapOfResults,
			String fileNameForWrite)
	{
		for (Map.Entry<ArrayList<Cluster>, Triple<String, Integer, ArrayList<Double>>> entry : mapOfResults.entrySet())
		{
			ArrayList<Cluster> clusters = entry.getKey();
			Triple<String, Integer, ArrayList<Double>> info = entry.getValue();
			ArrayList<Double> clusterQualityInfo = info.getThird();

			StringBuffer stringToWrite = new StringBuffer();

			for (Cluster c : clusters)
			{
				stringToWrite.append(c.toStringUserWiseToPrint());

			}
			stringToWrite.append(clusterQualityInfo.get(0) + "," + clusterQualityInfo.get(1) + ","
					+ clusterQualityInfo.get(2) + "," + clusterQualityInfo.get(3) + ",");
			stringToWrite.append(info.getSecond() + "," + info.getFirst() + "\n"); // number of updates as string
																					// 1_2_1_0 and number of times this
																					// clustering happens in result
			WritingToFile.appendLineToFile(stringToWrite.toString(), fileNameForWrite);
			// for (Map.Entry<LinkedHashSet<Cluster>, Pair<String, Integer>> entry : clusters..entrySet())
		}
	}

	/**
	 * 
	 * @param ts
	 * @return
	 */
	public static LinkedHashMap<String, Double> getShannonEntropy(LinkedHashMap<String, String> ts)// , boolean
																									// validsOnly)
	{
		LinkedHashMap<String, Double> r = new LinkedHashMap<String, Double>();
		System.out.println("inside getEntropy");

		for (Map.Entry<String, String> entry : ts.entrySet())
		{
			r.put(entry.getKey(), StatsUtils.round(StatsUtils.getShannonEntropy(entry.getValue()), 4));
		}
		System.out.println("exiting getEntropy");
		return r;
	}

	public static int getMagnifiedIntCodeForActivityObject(ActivityObject ao)
	{
		int r = (ConnectDatabase.getActivityID(ao.getActivityName()) + 0) * 100;
		return r;
	}

	/**
	 * 
	 * @param timelines
	 * @return
	 */
	public static LinkedHashMap<String, Integer>
			getNumOfActivityObjectsInTimeline(LinkedHashMap<String, Timeline> timelines)
	{
		LinkedHashMap<String, Integer> userNumOfAOsInTimeline = new LinkedHashMap<String, Integer>();

		for (Map.Entry<String, Timeline> entry : timelines.entrySet())
		{
			userNumOfAOsInTimeline.put(entry.getKey(), entry.getValue().getActivityObjectsInTimeline().size());
		}
		return userNumOfAOsInTimeline;
	}

	// public static LinkedHashMap<String,ArrayList<Long>> getUsersDurationDistribution()

	// public static LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> toTimeSeriesInt(LinkedHashMap<String,
	// LinkedHashMap<Timestamp, ActivityObject>> ts)
	// {
	// LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp,
	// Integer>>();
	//
	// for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
	// {
	// LinkedHashMap<Timestamp, Integer> dataToPut = new LinkedHashMap<Timestamp, Integer>();
	//
	// for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
	// {
	// dataToPut.put(dataEntry.getKey(), getIntCodeForActivityObject(dataEntry.getValue()));
	// }
	// r.put(entry.getKey(), dataToPut);
	// }
	// return r;
	// }

	// public static LinkedHashMap<Timestamp, ActivityObject> transformTimelineToEqualIntervalTimeSeries(Timeline
	// timeline, int intervalSizeInSeconds)
	// {
	// LinkedHashMap<Timestamp, ActivityObject>> timeSeries = new LinkedHashMap<String, LinkedHashMap<Timestamp,
	// ActivityObject>>();
	//
	// for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : allUsersDayTimelines.entrySet())
	// {
	// String userID = entry.getKey();
	// LinkedHashMap<Date, UserDayTimeline> dayTimelines = entry.getValue();
	//
	// LinkedHashMap<Timestamp, ActivityObject> dataPoints = new LinkedHashMap<Timestamp, ActivityObject>();
	//
	// for (Map.Entry<Date, UserDayTimeline> entryForDay : dayTimelines.entrySet())
	// {
	// UserDayTimeline dayTimeline = entryForDay.getValue();
	//
	// Timestamp cursorTimestamp = dayTimeline.getActivityObjectAtPosition(0).getStartTimestamp();
	// Timestamp endTimestamp = dayTimeline.getActivityObjectAtPosition(dayTimeline.getActivityObjectsInDay().size() -
	// 1).getEndTimestamp();
	//
	// while (cursorTimestamp.before(endTimestamp))
	// {
	// dataPoints.put(cursorTimestamp, dayTimeline.getActivityObjectAtTime(cursorTimestamp));
	// cursorTimestamp = new Timestamp(cursorTimestamp.getTime() + intervalSizeInSeconds * 1000); // increment by a
	// second
	// }
	// dataPoints.put(endTimestamp, dayTimeline.getActivityObjectAtTime(endTimestamp));
	// }
	//
	// timeSeries.put(userID, dataPoints);
	// }
	//
	// return timeSeries;
	// }

	public static void traverse(LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> timeSeries)
	{
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : timeSeries.entrySet())
		{
			String userID = entry.getKey();

			for (Map.Entry<Timestamp, ActivityObject> entryInn : entry.getValue().entrySet())
			{
				ActivityObject ao = entryInn.getValue();

				String s = entryInn.getKey().toString() + "," + ao.getActivityName();
				WritingToFile.appendLineToFile(s + "\n", userID + "TimeSeries");
			}
		}
	}

	// public static void performNGramAnalysis(LinkedHashMap<String,Timeline> timelines, int start)

	/**
	 * 
	 * @param timelines
	 * @param startN
	 * @param endN
	 * @param pathForResultFiles
	 */
	public static void performNGramAnalysis(LinkedHashMap<String, Timeline> timelines, int startN, int endN,
			String pathForResultFiles)
	{
		System.out.println("Performing NGram Analysis");
		for (Map.Entry<String, Timeline> entry : timelines.entrySet())
		{
			String userID = entry.getKey();

			///////////////
			if (Constant.blacklistingUsersWithLargeMaxActsPerDay)
			{
				if (Constant.getDatabaseName().equals("gowalla1"))
				{
					if (Arrays.asList(DomainConstants.gowallaUserIDsWithGT553MaxActsPerDay)
							.contains(Integer.valueOf(userID)))
					{
						System.out.println("Skipping user: " + userID + " as in gowallaUserIDsWithGT553MaxActsPerDay");
						// PopUps.showMessage("Skipping user: " + userId
						// + " as in gowallaUserIDsWithGT553MaxActsPerDay");
						continue;
					}
				}
				else
				{
					System.err.println("Warning: Constant.blacklistingUsersWithLargeMaxActsPerDay= "
							+ Constant.blacklistingUsersWithLargeMaxActsPerDay
							+ " but blacklisted user not defined for this database");
				}
			}
			///////////////

			Timeline timelineForUser = entry.getValue();
			String stringCodeOfTimeline = timelineForUser.getActivityObjectsAsStringCode(); // convert activity names to
																							// char codes.
			// System.out.println("Timeline as String code =" + stringCodeOfTimeline);
			for (int n = startN; n <= endN; n++)
			{
				LinkedHashMap<String, Long> freqDistr = getNGramOccurrenceDistribution(stringCodeOfTimeline, n);

				freqDistr = (LinkedHashMap<String, Long>) ComparatorUtils.sortByValueDescNoShuffle(freqDistr);

				WritingToFile.writeSimpleMapToFile(freqDistr, pathForResultFiles + n + "gram" + userID + "FreqDist.csv",
						"subsequence", "count");

				/// If there is a need to write n-grams as actname or act ids instead of char codes
				LinkedHashMap<String, Long> freqDistrWithActNames =
						freqDistr.entrySet().stream().collect(Collectors.toMap(e -> getNGramAsActName(e.getKey(), "--"),
								e -> e.getValue(), (e1, e2) -> e1, LinkedHashMap::new));

				LinkedHashMap<String, Long> freqDistrWithActID =
						freqDistr.entrySet().stream().collect(Collectors.toMap(e -> getNGramAsActID(e.getKey(), "--"),
								e -> e.getValue(), (e1, e2) -> e1, LinkedHashMap::new));

				WritingToFile.writeSimpleMapToFile(freqDistrWithActNames,
						pathForResultFiles + n + "gram" + userID + "FreqDistActName.csv", "subsequence", "count");

				WritingToFile.writeSimpleMapToFile(freqDistrWithActID,
						pathForResultFiles + n + "gram" + userID + "FreqDistActID.csv", "subsequence", "count");
				///
			}
		}
		System.out.println("Finsished NGram Analysis");
	}

	/**
	 * 
	 * @param nGram
	 * @param delimiter
	 * @return
	 */
	public static String getNGramAsActName(String nGram, String delimiter)
	{
		// System.out.println("Debug: inside getNGramAsActName for ngram:" + nGram);
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < nGram.length(); i++)
		{
			char c = nGram.charAt(i);
			Integer actID = DomainConstants.charCodeCatIDMap.get(c);
			String actName = DomainConstants.catIDNameDictionary.get(actID);
			// System.out.println("Debug: actID:" + actID + " actName:" + actName);
			sb.append(actName);
			if (i != (nGram.length() - 1))
			{
				sb.append(delimiter);
			}
		}
		// System.out.println("Debug: returning " + sb.toString() + " for nGram:" + nGram);

		return sb.toString();
	}

	/**
	 * 
	 * @param nGram
	 * @param delimiter
	 * @return
	 */
	public static String getNGramAsActID(String nGram, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nGram.length(); i++)
		{
			char c = nGram.charAt(i);
			Integer actID = DomainConstants.charCodeCatIDMap.get(c);
			sb.append(actID.toString());
			if (i != (nGram.length() - 1))
			{
				sb.append(delimiter);
			}
		}
		// System.out.println("Debug: returning " + sb.toString() + " for nGram:" + nGram);
		return sb.toString();
	}

	public static void traverseMapMapMap(
			LinkedHashMap<String, LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>>> result)
	{
		System.out.println("Traverse MapMapMap:");
		for (Map.Entry<String, LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>>> entryUser : result
				.entrySet()) // over each user
		{
			System.out.println("User = " + entryUser.getKey());

			for (Map.Entry<Pair<String, Integer>, LinkedHashMap<Double, double[]>> entryTwoGram : entryUser.getValue()
					.entrySet()) // over each user
			{
				System.out.println("\tTwo Gram = " + entryTwoGram.getKey().getFirst());
				System.out.println("\tNum of occure of this Two Gram = " + entryTwoGram.getKey().getSecond());

				for (Map.Entry<Double, double[]> entryMU : entryTwoGram.getValue().entrySet()) // over each user
				{
					System.out.println("\t\tMU = " + entryMU.getKey());
					System.out.println("\t\tpairwise edit distance = " + Arrays.toString(entryMU.getValue()));

				}

			}
		}
		System.out.println("end of Traverse MapMapMap:");
	}

	public static void writeMapMapMap(
			LinkedHashMap<String, LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>>> result)// ,
																												// String
																												// fileNamePhrase)
	{
		// System.out.println("writeMapMapMap");
		for (Map.Entry<String, LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>>> entryUser : result
				.entrySet()) // over each user
		{
			String user = entryUser.getKey();

			writeAlgorithmicAnalysisResults(user, entryUser.getValue(), user + "AlgrothmicAnalysisResults",
					" TwoGram, CountOccurrence, MU, MeanED, STDED, MaxED, MinED, MeanED/MU, STDED/MU, MaxED/MU, MinED/MU");

		}
		// System.out.println("end of Traverse MapMapMap:");
	}

	public static void writeMapMapMap2(
			LinkedHashMap<String, LinkedHashMap<Double, LinkedHashMap<Pair<String, Integer>, double[]>>> result)// ,
																												// String
																												// fileNamePhrase)
	{
		// System.out.println("writeMapMapMap");
		for (Map.Entry<String, LinkedHashMap<Double, LinkedHashMap<Pair<String, Integer>, double[]>>> entryUser : result
				.entrySet()) // over each user
		{
			String user = entryUser.getKey();

			writeAlgorithmicAnalysisResults2(user, entryUser.getValue(), user + "AlgorthmicAnalysis2Results",
					"MU, TwoGram, CountOccurrence,  MeanED, STDED, MaxED, MinED, MeanED/MU, STDED/MU, MaxED/MU, MinED/MU");
			writeAlgorithmicAnalysisResults2AggregatedOverTwoGrams(user, entryUser.getValue(),
					user + "AlgorthmicAnalysis2AggregatedOverTwoGramsResults",
					"MU,  MeanED, STDED, MaxED, MinED, MeanED/MU, STDED/MU, MaxED/MU, MinED/MU");

		}
		// System.out.println("end of Traverse MapMapMap:");
	}

	/**
	 * 
	 * @param user
	 * @param resultForUser
	 * @param fileNameToUse
	 * @param headerLine
	 *            " TwoGram, CountOccurrence, MU, MeanED, STDED, MaxED, MinED, MeanED/MU, STDED/MU, MaxED/MU, MinED/MU"
	 */
	public static void writeAlgorithmicAnalysisResults2(String user,
			LinkedHashMap<Double, LinkedHashMap<Pair<String, Integer>, double[]>> resultForUser, String fileNameToUse,
			String headerLine)
	{
		String commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			System.out.println("User = " + user);

			for (Map.Entry<Double, LinkedHashMap<Pair<String, Integer>, double[]>> entryMU : resultForUser.entrySet()) // over
																														// each
																														// user
			{
				double mu = entryMU.getKey();

				for (Entry<Pair<String, Integer>, double[]> entryTwoGram : entryMU.getValue().entrySet()) // over each
																											// user
				{
					bw.write(entryMU.getKey() + ",");

					bw.write(entryTwoGram.getKey().getFirst() + ",");
					bw.write(entryTwoGram.getKey().getSecond() + ",");

					DescriptiveStatistics ds = new DescriptiveStatistics(entryTwoGram.getValue());

					int numOfVals = entryTwoGram.getValue().length;

					bw.write(ds.getMean() + ",");
					bw.write(ds.getStandardDeviation() + ",");
					bw.write(ds.getMax() + ",");
					bw.write(ds.getMin() + ",");

					bw.write((ds.getMean() / mu) + ",");
					bw.write((ds.getStandardDeviation() / mu) + ",");
					bw.write((ds.getMax() / mu) + ",");
					bw.write((ds.getMin() / mu) + ",");

					bw.newLine();
				}

			}

			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param user
	 * @param resultForUser
	 * @param fileNameToUse
	 * @param headerLine
	 *            " TwoGram, CountOccurrence, MU, MeanED, STDED, MaxED, MinED, MeanED/MU, STDED/MU, MaxED/MU, MinED/MU"
	 */
	public static void writeAlgorithmicAnalysisResults2AggregatedOverTwoGrams(String user,
			LinkedHashMap<Double, LinkedHashMap<Pair<String, Integer>, double[]>> resultForUser, String fileNameToUse,
			String headerLine)
	{
		String commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			System.out.println("User = " + user);

			for (Map.Entry<Double, LinkedHashMap<Pair<String, Integer>, double[]>> entryMU : resultForUser.entrySet()) // over
																														// each
																														// user
			{
				double mu = entryMU.getKey();
				bw.write(entryMU.getKey() + ",");

				// double wtdMean = 0, wtdSTD = 0, wtdMax = 0, wtdMin = 0;
				// double countOccurrencesOfAllTwoGrams = 0;
				ArrayList<Double> editDistancesOverAllTwoGrams = new ArrayList<Double>();

				for (Entry<Pair<String, Integer>, double[]> entryTwoGram : entryMU.getValue().entrySet()) // over each
																											// user
				{
					List<Double> editDistancesPairwiseTrailsList =
							Arrays.asList(ArrayUtils.toObject(entryTwoGram.getValue())); // converting from double[] to
																							// list.
					editDistancesOverAllTwoGrams.addAll(new ArrayList(editDistancesPairwiseTrailsList));
					// countOccurrencesOfAllTwoGrams += entryTwoGram.getKey().getSecond();
					// DescriptiveStatistics ds = new DescriptiveStatistics(entryTwoGram.getValue());
					// // int numOfVals = entryTwoGram.getValue().length;
					// wtdMean += ds.getMean();
					// wtdSTD += ds.getStandardDeviation();
					// wtdMax += ds.getMax();
					// wtdMin += ds.getMin();
				}

				double[] editDistancesOverAllTwoGramsArray =
						editDistancesOverAllTwoGrams.stream().mapToDouble(Double::doubleValue).toArray(); // via method
																											// reference

				DescriptiveStatistics ds = new DescriptiveStatistics(editDistancesOverAllTwoGramsArray);

				bw.write(ds.getMean() + ",");
				bw.write(ds.getStandardDeviation() + ",");
				bw.write(ds.getMax() + ",");
				bw.write(ds.getMin() + ",");

				bw.write((ds.getMean() / mu) + ",");
				bw.write((ds.getStandardDeviation() / mu) + ",");
				bw.write((ds.getMax() / mu) + ",");
				bw.write((ds.getMin() / mu) + ",");

				bw.newLine();

			}

			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param user
	 * @param resultForUser
	 * @param fileNameToUse
	 * @param headerLine
	 *            " TwoGram, CountOccurrence, MU, MeanED, STDED, MaxED, MinED, MeanED/MU, STDED/MU, MaxED/MU, MinED/MU"
	 */
	public static void writeAlgorithmicAnalysisResults(String user,
			LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>> resultForUser, String fileNameToUse,
			String headerLine)
	{
		String commonPath = Constant.getCommonPath();//
		try
		{
			String fileName = commonPath + fileNameToUse + ".csv";

			File file = new File(fileName);

			file.delete();
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(headerLine);// .replaceAll("||",",")); //replacing pipes by commma
			bw.newLine();

			System.out.println("User = " + user);

			for (Map.Entry<Pair<String, Integer>, LinkedHashMap<Double, double[]>> entryTwoGram : resultForUser
					.entrySet()) // over each user
			{

				for (Map.Entry<Double, double[]> entryMU : entryTwoGram.getValue().entrySet()) // over each user
				{
					bw.write(entryTwoGram.getKey().getFirst() + ",");
					bw.write(entryTwoGram.getKey().getSecond() + ",");

					double mu = entryMU.getKey();
					bw.write(entryMU.getKey() + ",");

					DescriptiveStatistics ds = new DescriptiveStatistics(entryMU.getValue());

					int numOfVals = entryMU.getValue().length;

					bw.write(ds.getMean() + ",");
					bw.write(ds.getStandardDeviation() + ",");
					bw.write(ds.getMax() + ",");
					bw.write(ds.getMin() + ",");

					bw.write((ds.getMean() / mu) + ",");
					bw.write((ds.getStandardDeviation() / mu) + ",");
					bw.write((ds.getMax() / mu) + ",");
					bw.write((ds.getMin() / mu) + ",");

					bw.newLine();
				}

			}

			bw.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param timelines
	 * @param startN
	 * @param endN
	 * @param pathForResultFiles
	 */
	public static void performAlgorithmicAnalysis(LinkedHashMap<String, Timeline> timelines, int startN, int endN,
			String pathForResultFiles)
	{
		System.out.println("Performing Algorithmic Analysis");
		// <UserID, < Pair(each two gram, num of occcur of this two gram) , < given mu, pairwise edit distances>>>
		LinkedHashMap<String, LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>>> result =
				new LinkedHashMap<String, LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>>>();

		for (Map.Entry<String, Timeline> entry : timelines.entrySet()) // over each user
		{
			// <Pair(each two gram,num of occur) , < given mu, pairwise edit distances>>
			LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>> resultForAUser =
					new LinkedHashMap<Pair<String, Integer>, LinkedHashMap<Double, double[]>>();

			String userID = entry.getKey();
			Timeline timelineForUser = entry.getValue();

			HashMap<String, ArrayList<Integer>> freqDistrTwoGrams =
					getNGramOccurrenceDistributionWithIndices(timelineForUser.getActivityObjectsAsStringCode(), 2);
			int numOfDistinctTwoGrams = freqDistrTwoGrams.size();

			System.out.println("Number of two grams = " + numOfDistinctTwoGrams);

			// for each two gram go over different MUs get the pairwise edit distance of the MU trails
			for (Map.Entry<String, ArrayList<Integer>> entryTwoGram : freqDistrTwoGrams.entrySet())
			{
				String twoGram = entryTwoGram.getKey();
				ArrayList<Integer> startIndices = entryTwoGram.getValue();
				// < given mu, pairwise edit distances>
				LinkedHashMap<Double, double[]> resultForATwoGram = new LinkedHashMap<Double, double[]>();

				for (int muIndex = 1; muIndex < Constant.matchingUnitAsPastCount.length; muIndex++)
				{
					double mu = Constant.matchingUnitAsPastCount[muIndex];

					// obtain the trail segments for this mu.
					ArrayList<ArrayList<ActivityObject>> trailSegments =
							getTrailSegments(mu, startIndices, timelineForUser);

					double pairwiseTwoLevelEditDistances[] = getPairwiseTwoLevelEDs(trailSegments);
					resultForATwoGram.put(mu, pairwiseTwoLevelEditDistances);
				}

				resultForAUser.put(new Pair(twoGram, startIndices.size()), resultForATwoGram);
			}

			result.put(userID, resultForAUser);

		}

		// traverseMapMapMap(result);
		writeMapMapMap(result);
		System.out.println("Finsished Algorithmic Analysis");
	}

	public static void performAlgorithmicAnalysis2(LinkedHashMap<String, Timeline> timelines, int startN, int endN,
			String pathForResultFiles)
	{
		System.out.println("Performing Algorithmic Analysis2");
		// <UserID, < given mu , < Pair(each two gram, num of occcur of this two gram), pairwise edit distances of
		// trails>>>
		LinkedHashMap<String, LinkedHashMap<Double, LinkedHashMap<Pair<String, Integer>, double[]>>> result2 =
				new LinkedHashMap<String, LinkedHashMap<Double, LinkedHashMap<Pair<String, Integer>, double[]>>>();

		for (Map.Entry<String, Timeline> entry : timelines.entrySet()) // over each user
		{
			// < given mu , < Pair(each two gram, num of occcur of this two gram), pairwise edit distances of trails>>
			LinkedHashMap<Double, LinkedHashMap<Pair<String, Integer>, double[]>> resultForAUser =
					new LinkedHashMap<Double, LinkedHashMap<Pair<String, Integer>, double[]>>();

			String userID = entry.getKey();
			Timeline timelineForUser = entry.getValue();

			HashMap<String, ArrayList<Integer>> freqDistrTwoGrams =
					getNGramOccurrenceDistributionWithIndices(timelineForUser.getActivityObjectsAsStringCode(), 2);
			int numOfDistinctTwoGrams = freqDistrTwoGrams.size();

			System.out.println("Number of two grams = " + numOfDistinctTwoGrams);

			// go over each mu
			for (int muIndex = 1; muIndex < Constant.matchingUnitAsPastCount.length; muIndex++)
			{
				// for each two gram go over different MUs get the pairwise edit distance of the MU trails
				double mu = Constant.matchingUnitAsPastCount[muIndex];

				// < given mu, pairwise edit distances>
				LinkedHashMap<Pair<String, Integer>, double[]> resultForAMU =
						new LinkedHashMap<Pair<String, Integer>, double[]>();

				for (Map.Entry<String, ArrayList<Integer>> entryTwoGram : freqDistrTwoGrams.entrySet())
				{
					String twoGram = entryTwoGram.getKey();
					ArrayList<Integer> startIndices = entryTwoGram.getValue();

					// obtain the trail segments for this mu and this 2gram.
					ArrayList<ArrayList<ActivityObject>> trailSegments =
							getTrailSegments(mu, startIndices, timelineForUser);

					double pairwiseTwoLevelEditDistances[] = getPairwiseTwoLevelEDs(trailSegments);
					resultForAMU.put(new Pair(twoGram, startIndices.size()), pairwiseTwoLevelEditDistances);
				}

				resultForAUser.put(mu, resultForAMU);
			}

			result2.put(userID, resultForAUser);

		}

		// traverseMapMapMap(result);
		writeMapMapMap2(result2);
		System.out.println("Finsished Algorithmic Analysis2");
	}

	public static void traverseHashMap(HashMap<String, Long> map)
	{
		for (Map.Entry<String, Long> entry : map.entrySet())
		{
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
	}

	/**
	 * 
	 * @param map
	 */
	public static String traverseHashMapStringString(HashMap<String, String> map)
	{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : map.entrySet())
		{
			sb.append(entry.getKey()).append(":").append(entry.getValue());
			// System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param seqOfChars
	 *            the timeline as a sequence of characters, (String code of Timeline)
	 * @param N
	 *            ngram size
	 * @return HashMap of (Subsequence as String of size N, count of occurrence of this subsequence)
	 */
	public static LinkedHashMap<String, Long> getNGramOccurrenceDistribution(String seqOfChars, int N) // old name
																										// frequencyDistributionOfNGramSubsequences
	{
		// <Subsequence as String of size N, count of occurrence of this subsequence>
		LinkedHashMap<String, Long> subseqCounts = new LinkedHashMap<String, Long>();

		for (int i = 0; i < seqOfChars.length() - (N - 1); i++)
		{
			char[] subSeq = new char[N];

			for (int j = 0; j < N; j++)
			{
				// System.out.println("i=" + i + " j=" + j);
				subSeq[j] = seqOfChars.charAt(i + j);
			}

			String ssubSeq = new String(subSeq);

			// System.out.println("Reading subsequence:" + ssubSeq);

			if (subseqCounts.containsKey(ssubSeq))
			{
				subseqCounts.put(ssubSeq, subseqCounts.get(ssubSeq) + 1);
			}
			else
			{
				subseqCounts.put(ssubSeq, (long) 1);
			}
		}
		return subseqCounts;
	}

	public static LinkedHashMap<String, Long> getNGramFrequencyDistributionTest(String seqOfChars, int N)
	{
		// <Subsequence as String of size N, count of occurrence of this subsequence>
		LinkedHashMap<String, Long> subseqCounts = new LinkedHashMap<String, Long>();

		for (int i = 0; i < seqOfChars.length() - (N - 1); i++)
		{
			char[] subSeq = new char[N];

			for (int j = 0; j < N; j++)
			{
				subSeq[j] = seqOfChars.charAt(i + j);
				System.out.println("i=" + i + " j=" + j + "  subseq[" + j + "] =" + subSeq[j]);
			}

			String ssubSeq = new String(subSeq);

			System.out.println("Reading subsequence:" + ssubSeq);

			if (subseqCounts.containsKey(ssubSeq))
			{
				subseqCounts.put(ssubSeq, subseqCounts.get(ssubSeq) + 1);
			}
			else
			{
				subseqCounts.put(ssubSeq, (long) 1);
			}
		}
		return subseqCounts;
	}

	/**
	 * 
	 * @param seqOfChars
	 *            the timeline as a sequence of characters, (String code of Timeline)
	 * @param N
	 *            ngram size
	 * @return HashMap of (Subsequence as String of size N, ArrayList of start indices of the NGram's occurrence)
	 */
	public static LinkedHashMap<String, ArrayList<Integer>> getNGramOccurrenceDistributionWithIndices(String seqOfChars,
			int N)
	{
		// <Subsequence as String of size N, count of occurrence of this subsequence>
		LinkedHashMap<String, ArrayList<Integer>> subseqCounts = new LinkedHashMap<String, ArrayList<Integer>>();

		for (int i = 0; i < seqOfChars.length() - (N - 1); i++)
		{
			char[] subSeq = new char[N];

			for (int j = 0; j < N; j++)
			{
				subSeq[j] = seqOfChars.charAt(i + j);
				// System.out.println("i=" + i + " j=" + j + " subseq[" + j + "] =" + subSeq[j]);
			}

			String ssubSeq = new String(subSeq);

			// System.out.println("Reading subsequence:" + ssubSeq);

			if (subseqCounts.containsKey(ssubSeq))
			{
				ArrayList<Integer> newValue = new ArrayList(subseqCounts.get(ssubSeq));//
				newValue.add(new Integer(i));
				subseqCounts.put(ssubSeq, newValue);
			}
			else
			{
				ArrayList<Integer> newValue = new ArrayList();//
				newValue.add(new Integer(i));
				subseqCounts.put(ssubSeq, newValue);
			}
			// System.out.println("intermediate subseqCounts: " + subseqCounts);
		}
		return subseqCounts;
	}

	/**
	 * 
	 * @param timelines
	 * @param pathToWrite
	 */
	public static void writeAllFeaturesValues(LinkedHashMap<String, Timeline> timelines, String pathToWrite)
	{
		String originalPath = Constant.getCommonPath();
		Constant.setCommonPath(pathToWrite);
		ArrayList<Double> durationsForAll = new ArrayList<Double>();
		int countOneSecondDuration = 0, countLessOneMinute = 0;
		int count = 0;

		for (Map.Entry<String, Timeline> userTimelineEntry : timelines.entrySet())
		{
			String userID = userTimelineEntry.getKey();
			Timeline userTimeline = userTimelineEntry.getValue();

			for (ActivityObject ao : userTimeline.getActivityObjectsInTimeline())
			{
				count++;

				String activityName = ao.getActivityName();
				WritingToFile.appendLineToFile(activityName + "\n", userID + "activityName");

				Timestamp startTimestamp = ao.getStartTimestamp();
				WritingToFile.appendLineToFile((startTimestamp) + "\n", userID + "startTimestamp");

				Timestamp endTimestamp = ao.getEndTimestamp();
				WritingToFile.appendLineToFile((endTimestamp) + "\n", userID + "endTimestamp");

				Long duration = ao.getDurationInSeconds();
				WritingToFile.appendLineToFile(duration.toString() + "\n", userID + "duration");
				WritingToFile.appendLineToFile(duration.toString() + "\n", "AllUsersduration");

				durationsForAll.add((double) duration);
				if (duration == 1)
				{
					countOneSecondDuration++;
				}

				if (duration < 60)
				{
					countLessOneMinute++;
				}

				if (Constant.getDatabaseName().equals("Geolife1"))
				{
					String startLatitude = ao.getStartLatitude();
					WritingToFile.appendLineToFile(startLatitude + "\n", userID + "startLatitude");

					String endLatitude = ao.getEndAltitude();
					WritingToFile.appendLineToFile(endLatitude + "\n", userID + "endLatitude");

					String startLongitude = ao.getStartLongitude();
					WritingToFile.appendLineToFile(startLongitude + "\n", userID + "startLongitude");

					String endLongitude = ao.getEndLongitude();
					WritingToFile.appendLineToFile(endLongitude + "\n", userID + "endLongitude");

					String startAltitude = ao.getStartAltitude();
					WritingToFile.appendLineToFile(startAltitude + "\n", userID + "startAltitude");

					String endAltitude = ao.getStartAltitude();
					WritingToFile.appendLineToFile(endAltitude + "\n", userID + "endAltitude");

					String avgAltitude = ao.getAvgAltitude();
					WritingToFile.appendLineToFile(avgAltitude + "\n", userID + "avgAltitude");

					double distanceTravelled = ao.getDistanceTravelled();
					WritingToFile.appendLineToFile(String.valueOf(distanceTravelled) + "\n",
							userID + "distanceTravelled");
				}
			}
		}
		StatsUtils.getDescriptiveStatistics(durationsForAll.stream().mapToDouble(l -> l.doubleValue()).toArray(),
				"Durations For All users", "DurationsForAllUsers", true);
		System.out.println("Number of activity-objects with duration < 1 minutes: " + countLessOneMinute
				+ ", % of total = " + (((double) countLessOneMinute / count) * 100));
		System.out.println("Number of activity-objects with duration = 1 second: " + countOneSecondDuration
				+ ", % of total = " + (((double) countOneSecondDuration / count) * 100));
		// restoring original common path
		Constant.setCommonPath(originalPath);
	}

	/**
	 * Writes the num of activity objects in timeline
	 * 
	 * @param usersDayTimelines
	 */
	public static void writePreliminaryStats(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines,
			String fileNamePhrase)
	{
		String toWrite = "";

		toWrite += "Num of users = " + usersDayTimelines.size();

		LinkedHashMap<String, Timeline> usersTimelines = TimelineUtils.dayTimelinesToTimelines(usersDayTimelines);

		StringBuffer s = new StringBuffer();
		s.append("User, User, NumOfActivityObjects");
		for (Map.Entry<String, Timeline> entry : usersTimelines.entrySet())
		{
			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
					+ "," + entry.getValue().size());
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	/**
	 * Writes the num of activity objects in timeline
	 * 
	 * @param usersDayTimelines
	 */
	public static void writeNumOfActivityObjectsInTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
	{
		LinkedHashMap<String, Timeline> usersTimelines = TimelineUtils.dayTimelinesToTimelines(usersDayTimelines);

		StringBuffer s = new StringBuffer();
		s.append("User, User, NumOfActivityObjects");
		for (Map.Entry<String, Timeline> entry : usersTimelines.entrySet())
		{
			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
					+ "," + entry.getValue().size());
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	/**
	 * writeActivityCountsInGivenDayTimelines, writeActivityDurationInGivenDayTimelines,
	 * writeActivityOccPercentageOfTimelines
	 * 
	 * @param usersDayTimelines
	 * @param fileNamePhrase
	 */
	public static void writeActivityStats(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines,
			String fileNamePhrase)
	{
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			String userName = entry.getKey();
			WritingToFile.writeActivityCountsInGivenDayTimelines(userName, entry.getValue(), "AllTimelines");

			if (!Constant.getDatabaseName().equals("gowalla1"))
			{// since gowalla data does not have duration
				WritingToFile.writeActivityDurationInGivenDayTimelines(userName, entry.getValue(), "AllTimelines");
			}
			WritingToFile.writeActivityOccPercentageOfTimelines(userName, entry.getValue(), "AllTimelines");
		}
	}

	/**
	 * 
	 * @param usersDayTimelines
	 */
	public static void writeNumOfValidActivityObjectsInTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
	{
		StringBuffer s = new StringBuffer();
		// PopUps.showMessage("inside writeavgdis");
		s.append("User, User, NumOfValidActivityObjects");
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			int numOfTotalValidActs = 0;
			for (Entry<Date, Timeline> entryDay : entry.getValue().entrySet())
			{
				numOfTotalValidActs += entryDay.getValue().countNumberOfValidActivities();
			}
			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
					+ "," + numOfTotalValidActs);
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	/**
	 * 
	 * 
	 * @param usersDayTimelines
	 */
	public static void writeNumOfDaysInTimelines(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines,
			String fileNamePhrase)
	{
		StringBuffer s = new StringBuffer();
		s.append("User, User, NumOfDays, NumOfWeekDays,NumOfWeekends,%OfWeekdays");
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			int numOfWeekends = 0, numOfWeekDays = 0;

			for (Entry<Date, Timeline> entryDate : entry.getValue().entrySet())
			{
				if (entryDate.getKey().getDay() == 0 || entryDate.getKey().getDay() == 6)
				{
					numOfWeekends++;
				}
				else
				{
					numOfWeekDays++;
				}
			}
			double percentage = ((numOfWeekends + numOfWeekDays) > 0)
					? ((double) numOfWeekDays / (double) (numOfWeekends + numOfWeekDays) * 100) : 100;

			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
					+ "," + entry.getValue().size() + "," + numOfWeekDays + "," + numOfWeekends + "," + percentage);
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	public static void writeStatsNumOfDistinctActsPerDayInTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
	{
		StringBuffer s = new StringBuffer();
		// PopUps.showMessage("inside writeavgdis");
		s.append(
				"User, User, MeanNumOfDistinctActsPerDay, MedianNumOfDistinctActsPerDay, IQRNumOfDistinctActsPerDay, MaxNumOfDistinctActsPerDay, MinNumOfDistinctActsPerDay,SumNumOfDistinctActsPerDay,TotalNumOfDistinctActsOverAllTimelines");
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			double avgNumOfDistinctActsPerDay = 0;
			// long totalNumOfDistinctActsOverAllTimelines = 0;
			ArrayList<Double> numOfDistinctActsPerDay = new ArrayList<Double>();// size= num of days
			double[] numOfDistinctActsPerDayArray;
			ArrayList<ActivityObject> allActObjs = new ArrayList<ActivityObject>();

			for (Entry<Date, Timeline> entryDay : entry.getValue().entrySet())
			{
				allActObjs.addAll(entryDay.getValue().getActivityObjectsInDay());
				numOfDistinctActsPerDay.add((double) entryDay.getValue().countNumberOfValidDistinctActivities());
			}

			numOfDistinctActsPerDayArray = UtilityBelt.toPrimitive(numOfDistinctActsPerDay);
			DescriptiveStatistics ds = new DescriptiveStatistics(numOfDistinctActsPerDayArray);

			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
					+ "," + ds.getMean() + "," + (ds.getPercentile(50)) + ","
					+ (ds.getPercentile(75) - ds.getPercentile(25)) + "," + ds.getMax() + "," + ds.getMin() + ","
					+ ds.getSum() + "," + TimelineUtils.countNumberOfDistinctActivities(allActObjs));
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	public static void writeAllNumOfDistinctActsPerDayInTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
	{
		StringBuffer s = new StringBuffer();
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{

			String numOfDistinctActsPerDay = new String();

			for (Entry<Date, Timeline> entryDay : entry.getValue().entrySet())
			{
				numOfDistinctActsPerDay += (entryDay.getValue().countNumberOfValidDistinctActivities() + ",");
			}

			s.append(numOfDistinctActsPerDay.substring(0, numOfDistinctActsPerDay.length() - 1) + "\n");
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	/**
	 * 
	 * 
	 * @param usersDayTimelines
	 */
	public static void writeAvgNumOfDistinctActsPerDayInTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
	{
		StringBuffer s = new StringBuffer();
		// PopUps.showMessage("inside writeavgdis");
		s.append("User, User, AvgNumOfDistinctActsPerDay,Mean,Median, IQR");
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			double avgNumOfDistinctActsPerDay = 0;

			ArrayList<Integer> numOfDistinctActsPerDay = new ArrayList<Integer>();// size= num of days

			for (Entry<Date, Timeline> entryDay : entry.getValue().entrySet())
			{
				numOfDistinctActsPerDay.add(entryDay.getValue().countNumberOfValidDistinctActivities());
			}

			avgNumOfDistinctActsPerDay = StatsUtils.averageOfListInteger(numOfDistinctActsPerDay);

			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
					+ "," + avgNumOfDistinctActsPerDay + "," + StatsUtils.meanOfArrayListInt(numOfDistinctActsPerDay, 2)
					+ "," + StatsUtils.medianOfArrayListInt(numOfDistinctActsPerDay, 2) + ","
					+ StatsUtils.iqrOfArrayListInt(numOfDistinctActsPerDay, 2));
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	/**
	 * 
	 * 
	 * @param usersDayTimelines
	 */
	public static void writeStatsNumOfTotalActsPerDayInTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
	{
		StringBuffer s = new StringBuffer();
		// PopUps.showMessage("inside writeavgdis");
		s.append(
				"User, User, MeanNumOfTotalActsPerDay, MedianNumOfTotalActsPerDay, IQRNumOfTotalActsPerDay, MaxNumOfTotalActsPerDay, MinNumOfTotalActsPerDay,SumNumOfTotalActsPerDay,TotalNumOfActsOverAllTimelines");
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			double avgNumOfTotalActsPerDay = 0;
			long totalNumOfActsOverAllTimelines = 0;

			ArrayList<Double> numOfTotalActsPerDay = new ArrayList<Double>();// size= num of days
			double[] numOfTotalActsPerDayArray;

			for (Entry<Date, Timeline> entryDay : entry.getValue().entrySet())
			{
				totalNumOfActsOverAllTimelines += entryDay.getValue().countNumberOfValidActivities();
				numOfTotalActsPerDay.add((double) entryDay.getValue().countNumberOfValidActivities());
			}
			numOfTotalActsPerDayArray = UtilityBelt.toPrimitive(numOfTotalActsPerDay);
			DescriptiveStatistics ds = new DescriptiveStatistics(numOfTotalActsPerDayArray);

			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
					+ "," + ds.getMean() + "," + (ds.getPercentile(50)) + ","
					+ (ds.getPercentile(75) - ds.getPercentile(25)) + "," + ds.getMax() + "," + ds.getMin() + ","
					+ ds.getSum() + "," + totalNumOfActsOverAllTimelines);
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	//

	public static void writeAllNumOfTotalActsPerDayInTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
	{
		StringBuffer s = new StringBuffer();
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			String numOfTotalActsPerDay = new String();

			for (Entry<Date, Timeline> entryDay : entry.getValue().entrySet())
			{
				numOfTotalActsPerDay += (entryDay.getValue().countNumberOfValidActivities() + ",");
			}
			s.append(numOfTotalActsPerDay.substring(0, numOfTotalActsPerDay.length() - 1) + "\n");
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	/**
	 * 
	 * 
	 * @param usersDayTimelines
	 */
	public static void writeAvgNumOfTotalActsPerDayInTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
	{
		StringBuffer s = new StringBuffer();
		// PopUps.showMessage("inside writeavgdis");
		s.append("User,User, AvgNumOfTotalActsPerDay,Mean,Median,IQR");
		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			double avgNumOfTotalActsPerDay = 0;

			ArrayList<Integer> numOfTotalActsPerDay = new ArrayList<Integer>();// size= num of days

			for (Entry<Date, Timeline> entryDay : entry.getValue().entrySet())
			{
				numOfTotalActsPerDay.add(entryDay.getValue().countNumberOfValidActivities());
			}

			avgNumOfTotalActsPerDay = StatsUtils.averageOfListInteger(numOfTotalActsPerDay);

			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
					+ "," + avgNumOfTotalActsPerDay + "," + StatsUtils.meanOfArrayListInt(numOfTotalActsPerDay, 2) + ","
					+ StatsUtils.medianOfArrayListInt(numOfTotalActsPerDay, 2) + ","
					+ StatsUtils.iqrOfArrayListInt(numOfTotalActsPerDay, 2));
		}

		WritingToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
	}

	public static LinkedHashMap<String, Complex[]> getFTInt(LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> ts)// ,
																														// String
																														// fileNameToUse)
	{
		LinkedHashMap<String, Complex[]> transforms = new LinkedHashMap<String, Complex[]>();
		FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, Integer>> entry : ts.entrySet())
			{
				int userName = Integer.valueOf(entry.getKey());// UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey()));
				double vals[] = getValuesInt(entry.getValue());
				Complex[] ftCoefficient = fft.transform(vals, TransformType.FORWARD);
				transforms.put(entry.getKey(), ftCoefficient);
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return transforms;
	}

	/**
	 * 
	 * @param ts
	 * @param fileNameToUse
	 * @return
	 */
	public static LinkedHashMap<String, Complex[]>
			getFTDouble(LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> ts)// , String fileNameToUse)
	{
		LinkedHashMap<String, Complex[]> transforms = new LinkedHashMap<String, Complex[]>();
		FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, Double>> entry : ts.entrySet())
			{
				int userName = Integer.valueOf(entry.getKey());// UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey()));
				double vals[] = getValuesDouble(entry.getValue());
				Complex[] ftCoefficient = fft.transform(vals, TransformType.FORWARD);
				transforms.put(entry.getKey(), ftCoefficient);
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return transforms;
	}

	public static LinkedHashMap<String, Complex[]> getFTLong(LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> ts)// ,
																														// String
																														// fileNameToUse)
	{
		LinkedHashMap<String, Complex[]> transforms = new LinkedHashMap<String, Complex[]>();
		FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

		try
		{
			for (Map.Entry<String, LinkedHashMap<Timestamp, Long>> entry : ts.entrySet())
			{
				int userName = Integer.valueOf(entry.getKey());// UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey()));
				double vals[] = getValuesLong(entry.getValue());
				Complex[] ftCoefficient = fft.transform(vals, TransformType.FORWARD);
				transforms.put(entry.getKey(), ftCoefficient);
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return transforms;
	}

	private static double[] getValuesInt(LinkedHashMap<Timestamp, Integer> values)
	{
		double valsToTransform[] = new double[values.size()];
		int i = 0;
		for (Map.Entry<Timestamp, Integer> entry : values.entrySet())
		{
			valsToTransform[i] = entry.getValue();
			i++;
		}
		return valsToTransform;
	}

	private static double[] getValuesDouble(LinkedHashMap<Timestamp, Double> values)
	{
		double valsToTransform[] = new double[values.size()];
		int i = 0;
		for (Map.Entry<Timestamp, Double> entry : values.entrySet())
		{
			valsToTransform[i] = entry.getValue();
			i++;
		}
		return valsToTransform;
	}

	private static double[] getValuesLong(LinkedHashMap<Timestamp, Long> values)
	{
		double valsToTransform[] = new double[values.size()];
		int i = 0;
		for (Map.Entry<Timestamp, Long> entry : values.entrySet())
		{
			valsToTransform[i] = entry.getValue();
			i++;
		}
		return valsToTransform;
	}

	/**
	 * Must be preceeded by org.activity.stats.TimelineStats.performTimeSeriesAnalysis(LinkedHashMap<String,
	 * LinkedHashMap<Date, Timeline>>)
	 * 
	 * @param usersDayTimelines
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<String, Triple<Double, Double, Double>>>
			performHjorthParameterAnalysis(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines)
	{
		String path = Constant.getCommonPath();
		int numOfUser = usersDayTimelines.size();

		String[] featureNames = Constant.getFeatureNames();

		LinkedHashMap<String, LinkedHashMap<String, Triple<Double, Double, Double>>> userLevelHjorthParams =
				new LinkedHashMap<String, LinkedHashMap<String, Triple<Double, Double, Double>>>();
		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
		{
			String userIDN = entry.getKey(); // user id here is the user id formatted to be from 1 to Num of users

			LinkedHashMap<String, Triple<Double, Double, Double>> featureLevelHjorthParams =
					new LinkedHashMap<String, Triple<Double, Double, Double>>();

			for (String featureName : featureNames)
			{
				String absfileNameToRead = path + userIDN + featureName + "SequenceIntInvalidsExpungedDummyTime.csv";
				double valsTS[] = UtilityBelt.getTimeSeriesVals(absfileNameToRead);
				HjorthParameters hp = new HjorthParameters(valsTS);

				featureLevelHjorthParams.put(featureName,
						new Triple(hp.getActivity(), hp.getMobility(), hp.getComplexity()));
			}
			userLevelHjorthParams.put(userIDN, featureLevelHjorthParams);
		}

		writeHjorthParameters(userLevelHjorthParams);
		// traverseSampleEntropiesAggregate(userLevelSampEn);

		return userLevelHjorthParams;
	}

	public static void
			writeHjorthParameters(LinkedHashMap<String, LinkedHashMap<String, Triple<Double, Double, Double>>> all)
	{
		System.out.println("Writing writeHjorthParameters");
		for (Entry<String, LinkedHashMap<String, Triple<Double, Double, Double>>> userEntry : all.entrySet())
		{
			String userIDN = userEntry.getKey();

			for (Entry<String, Triple<Double, Double, Double>> featureEntry : userEntry.getValue().entrySet())
			{
				String featureName = featureEntry.getKey();
				Triple hjorthParams = featureEntry.getValue();
				// System.out.println("user =" + userIDN + " featureName = " + featureEntry.getValue() + " m = " + m + "
				// sample entropy = " + sampleEntropy);
				String toWrite = "activity," + hjorthParams.getFirst() + "\n" + "mobility," + hjorthParams.getSecond()
						+ "\n" + "complexity," + hjorthParams.getThird() + "\n";
				String fileNameToUse = userIDN + featureEntry.getKey() + "HjorthParams";
				WritingToFile.appendLineToFile(toWrite, fileNameToUse);
			}
		}
	}
}
