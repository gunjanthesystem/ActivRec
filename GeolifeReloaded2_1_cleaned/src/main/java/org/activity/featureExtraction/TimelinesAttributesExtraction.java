package org.activity.featureExtraction;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.objects.UserDayTimeline;
import org.activity.stats.TimelineStats;
import org.activity.ui.PopUps;
import org.activity.util.Constant;
import org.activity.util.UtilityBelt;
import org.activity.util.weka.WekaUtilityBelt;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * 
 * @author gunjan
 *
 */
public class TimelinesAttributesExtraction
{
	String absoluteNameOfAttributesFile;
	LinkedHashMap<String, ArrayList<String>> timelineAttributeVectors;
	ArrayList<String> attributeLabels;
	/**
	 * User ID to to serial number of corresponding instance in atribute vectors
	 */
	LinkedHashMap<String, Integer> userIDInstanceID; // useful to determin the instance
	// LinkedHashMap<String, String> userIDActualClassMap;
	LinkedHashMap<String, String> groundTruth; // the map containg (raw user id, manual cluster)
	
	private final int minEpochSampEn = 2, maxEpochSampEn = 4;
	
	// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb8/CountsForClusterLabelAccToMinMUHavMaxMRR.csv", 4, true);
	
	// public LinkedHashMap<String, String> getUserIDClass()
	// {
	// return userIDActualClassMap;
	// }
	
	public LinkedHashMap<String, Integer> getUserIDInstanceID()
	{
		return userIDInstanceID;
	}
	
	/**
	 * Extracts features and write the corresponding csv file
	 * 
	 * @param usersDayTimelines
	 *            (already cleaned and rearranged)
	 * @param pathToWrite
	 * @param groundTruthToRead
	 *            (Absolute path to file containing ground truth, column in which ground truth is there, has column headers or not)
	 */
	public TimelinesAttributesExtraction(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines, String pathToWrite,
			Triple<String, Integer, Boolean> groundTruthToRead)
	{
		// PopUps.showMessage("Num of user timelnes recieved inTimelinesFeatureExtraction= " + usersDayTimelinesAll.size());
		Constant.setCommonPath(pathToWrite);
		// PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + "FeatureExtractionConsoleLog.txt");
		
		// PopUps.showMessage("num of users for feature extraction = " + usersDayTimelines.size());
		// WritingToFile.writeUsersDayTimelines(usersDayTimelines, "users", true, true, true);// users
		TimelineStats.writeNumOfActivityObjectsInTimelines(usersDayTimelines, "NumOfActivityObjectsInCleanedTimelines");
		
		initialiseTimelineAttributeVectors(usersDayTimelines);
		// $$ addDoubleFeatureToFeatureVectors(getSequenceEntropyAfterExpungingInvalids(usersDayTimelines), "ShannonsEntropy");
		
		LinkedHashMap<String, Timeline> usersTimelines = UtilityBelt.dayTimelinesToTimelines(usersDayTimelines);
		LinkedHashMap<String, Timeline> usersTimelinesInvalidsExpunged = UtilityBelt.expungeInvalids(usersTimelines);
		
		sanityCheckDayTimelineToTimelineConversion(usersDayTimelines, usersTimelines);
		
		// $$ addDoubleFeatureToFeatureVectors(getNumberOfActivityObjects(usersTimelinesInvalidsExpunged), "NumOfActivityObjects");
		// $$ addDoubleFeatureToFeatureVectors(getNumberOfValidDistinctActivities(usersTimelinesInvalidsExpunged), "NumOfValidDistinctActivities");
		addDoubleAttributeToAttributeVectors(getNumberOfValidDistinctActivitiesUponLengthOfTimeline(usersTimelinesInvalidsExpunged),
				"NumOfValidDistinctActivities/LengthOfTimeline");
		//
		
		TimelineStats.performTimeSeriesAnalysis((usersDayTimelines));//
		TimelineStats.performSampleEntropyVsMAnalysis2((usersDayTimelines));// UtilityBelt.reformatUserIDs
		
		for (int m = minEpochSampEn; m <= maxEpochSampEn; m++)
		{
			for (String featureName : Constant.getFeatureNames())
			{
				addDoubleAttributeToAttributeVectors(getFeatureSampleEntropyAfterExpungingInvalids((usersDayTimelines), featureName, m), "SampEn" + featureName
						+ m);
			}
			addDoubleAttributeToAttributeVectors(getAggSampleEntropyAfterExpungingInvalids((usersDayTimelines), "Sum", m), "SampEn" + "Sum" + m);
			addDoubleAttributeToAttributeVectors(getAggSampleEntropyAfterExpungingInvalids((usersDayTimelines), "Avg", m), "SampEn" + "Avg" + m);
			addDoubleAttributeToAttributeVectors(getAggSampleEntropyAfterExpungingInvalids((usersDayTimelines), "StdDev", m), "SampEn" + "StdDev" + m);
			addDoubleAttributeToAttributeVectors(getAggSampleEntropyAfterExpungingInvalids((usersDayTimelines), "Median", m), "SampEn" + "Median" + m);
		}
		
		LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> NGramFeatureVectors = getNGramAttributes(usersTimelinesInvalidsExpunged,
				2, 3, pathToWrite);//
		writeNGramAttributes(NGramFeatureVectors, "NGramFeatures");
		addNGramAttributeVectorsToAttributeVectors(NGramFeatureVectors);
		
		// ///////////HJorth Paramaters//////////////////////////////////////////////////////
		TimelineStats.performHjorthParameterAnalysis(usersDayTimelines);
		for (String featureName : Constant.getFeatureNames())
		{
			if (featureName.equals("ActivityName")) // not a numerical feature, hence derivative based metric is not suitable i think
			{
				continue;
			}
			else
			{
				addDoubleAttributeToAttributeVectors(getHjorthParametersAfterExpungingInvalids((usersDayTimelines), featureName, 0), "" + featureName
						+ "Activity");
				addDoubleAttributeToAttributeVectors(getHjorthParametersAfterExpungingInvalids((usersDayTimelines), featureName, 1), "" + featureName
						+ "Mobility");
				addDoubleAttributeToAttributeVectors(getHjorthParametersAfterExpungingInvalids((usersDayTimelines), featureName, 1), "" + featureName
						+ "Complexity");
			}
		}
		// ///////////////////////////////////////////////////////////////////
		
		// addStringFeatureToFeatureVectors(getManualClustering(1), "ManualClustering1");
		// addStringFeatureToFeatureVectors(getManualClustering(2), "ManualClustering2");
		// manualClustering = WekaUtilityBelt.getManualClustering(9, UtilityBelt.getListOfUsers(usersDayTimelines),
		// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/ManualClustersUserAbove10RTs.csv", true);
		groundTruth = WekaUtilityBelt.getGroundTruth(groundTruthToRead.getSecond(), UtilityBelt.getListOfUsers(usersDayTimelines),
				groundTruthToRead.getFirst(), groundTruthToRead.getThird());
		
		// manualClustering = WekaUtilityBelt.getManualClustering(2, UtilityBelt.getListOfUsers(usersDayTimelines));
		
		addStringAttributeToAttributeVectors(groundTruth, "ManualClustering2");
		
		// userIDActualClassMap = WekaUtilityBelt.getUserActualClusterMap(manualClustering);
		// $$ addDoubleFeatureToFeatureVectors(getBestMU(3), "BestMU");
		
		this.absoluteNameOfAttributesFile = writeTimelineAttributeVectors("TimelineFeatureVectors");// writeTimelineFeatureVectorsWithoutUserID
		// writeTimelineFeatureVectors("TimelineFeatureVectors");
		
		// consoleLogStream.close();
		// PopUps.showMessage("Exiting feature extraction");
	}
	
	// public static LinkedHashMap<String, String> getManualClustering(int indexOfColumn, ArrayList<String> selectedUsers)
	// {
	// LinkedHashMap<String, String> clustering1 = new LinkedHashMap<String, String>();
	// String fileName = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/ManualClustersUserAbove10RTs.csv";
	//
	// List<String> clusterLabel = ReadingFromFile.oneColumnReaderString(fileName, ",", indexOfColumn, true); // ALERT: ensure that the order of user is correct (alternative use
	// two column
	// // reader with
	// // LinkadedHashMap
	//
	// int[] userIDs = Constant.getUserIDs();
	// // for (int i = 1; i <= clusterLabel.size(); i++)
	// // {
	// // clustering1.put(String.valueOf(userIDs[i - 1]), clusterLabel.get(i - 1));
	// // }
	// //
	// for (int i = 0; i < clusterLabel.size(); i++)
	// {
	// if (selectedUsers.contains(String.valueOf(userIDs[i])))
	// {
	// clustering1.put(String.valueOf(userIDs[i]), clusterLabel.get(i));
	//
	// System.out.println("user id =" + userIDs[i] + " cluster label = " + clusterLabel.get(i));
	// }
	// }
	//
	// return clustering1;
	//
	// }
	
	/**
	 * Reads the manually assigned clusters (Alert: reads local file, absolute file path)
	 * 
	 * @param indexOfColumn
	 *            index of colum containinf the manual clustering
	 * @return
	 */
	private LinkedHashMap<String, Double> getBestMU(int indexOfColumn)
	{
		LinkedHashMap<String, Double> bestMU = new LinkedHashMap<String, Double>();
		String fileName = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/ManualClustersUserAbove10RTs.csv";
		
		List<Double> clusterLabel1 = ReadingFromFile.oneColumnReaderDouble(fileName, ",", indexOfColumn, true); // ensure that the order of user is correct (alternative use two
																												// column reader with
		
		int[] userIDs = Constant.getUserIDs();
		for (int i = 1; i <= clusterLabel1.size(); i++)
		{
			bestMU.put(String.valueOf(userIDs[i - 1]), clusterLabel1.get(i - 1));
		}
		
		return bestMU;
		
	}
	
	/**
	 * Writes the timelines feature vector in csv format. Here user id is replace by 'index of user id'
	 * 
	 * @param fileNamePhrase
	 * @return
	 */
	private String writeTimelineAttributeVectors(String fileNamePhrase)
	{
		WritingToFile.appendLineToFile("UserID", fileNamePhrase); // first column wil be user name
		
		for (String label : attributeLabels)
		{
			WritingToFile.appendLineToFile("," + label, fileNamePhrase);
		}
		
		WritingToFile.appendLineToFile("\n", fileNamePhrase);
		
		for (Map.Entry<String, ArrayList<String>> entry : timelineAttributeVectors.entrySet()) // iterating over users
		{
			int indexOfUserID = Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()));
			
			WritingToFile.appendLineToFile("User" + (indexOfUserID + 1), fileNamePhrase); // starting user id from 1 by incrementing 0 indexed values by 1
			
			for (String attributeValue : entry.getValue())
			{
				WritingToFile.appendLineToFile("," + attributeValue, fileNamePhrase);
			}
			WritingToFile.appendLineToFile("\n", fileNamePhrase);
		}
		
		return Constant.getCommonPath() + fileNamePhrase;
	}
	
	/**
	 * 
	 * @param fileNamePhrase
	 * @return
	 */
	private String writeTimelineAttributeVectorsWithoutUserID(String fileNamePhrase)
	{
		int count = -1;
		for (String label : attributeLabels)
		{
			count += 1;
			if (count == 0)
				WritingToFile.appendLineToFile(label, fileNamePhrase);
			else
				WritingToFile.appendLineToFile("," + label, fileNamePhrase);
		}
		
		WritingToFile.appendLineToFile("\n", fileNamePhrase);
		
		for (Map.Entry<String, ArrayList<String>> entry : timelineAttributeVectors.entrySet()) // iterating over users
		{
			int indexOfUserID = Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()));
			
			// WritingToFile.appendLineToFile("User" + (indexOfUserID + 1), fileNamePhrase); // starting user id from 1 by incrementing 0 indexed values by 1
			count = -1;
			for (String featureValue : entry.getValue())
			{
				count += 1;
				if (count == 0)
					WritingToFile.appendLineToFile(featureValue, fileNamePhrase);
				else
					WritingToFile.appendLineToFile("," + featureValue, fileNamePhrase);
			}
			WritingToFile.appendLineToFile("\n", fileNamePhrase);
		}
		
		return Constant.getCommonPath() + fileNamePhrase;
	}
	
	/**
	 * 
	 * @param timelines
	 * @param startN
	 * @param endN
	 * @param pathForResultFiles
	 * @return LinkedHashMap<UserID, LinkedHashMap<NGramID, LinkedHashMap<FeatureLabel, FeatureValue>>>
	 */
	public static LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> getNGramAttributes(LinkedHashMap<String, Timeline> timelines,
			int startN, int endN, String pathForResultFiles)
	{
		System.out.println("Performing NGram Analysis");
		LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> features = new LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>>();
		
		for (Map.Entry<String, Timeline> entry : timelines.entrySet()) // iterating over users
		{
			LinkedHashMap<Integer, LinkedHashMap<String, Double>> featuresForThisUser = new LinkedHashMap<Integer, LinkedHashMap<String, Double>>();
			
			String userID = entry.getKey();
			Timeline timelineForUser = entry.getValue();
			String stringCodeOfTimeline = timelineForUser.getActivityObjectsAsStringCode(); // convert activity names to char codes.
			int numberOfActivityObjectsInTimeline = stringCodeOfTimeline.length();
			
			// System.out.println("Timeline as String code =" + stringCodeOfTimeline);
			
			for (int n = startN; n <= endN; n++) // iterating over NGRams
			{
				LinkedHashMap<String, Long> freqDistr = TimelineStats.getNGramOccurrenceDistribution(stringCodeOfTimeline, n);
				
				DescriptiveStatistics dsFreqDistrVals = getDescriptiveStatsOfValues(freqDistr);
				
				LinkedHashMap<String, Double> featuresForThisGram = new LinkedHashMap<String, Double>();
				// featuresForThisGram.put(n + "Gram_" + "SumOfFrequencies", dsFreqDistrVals.getSum());
				// featuresForThisGram.put(n + "Gram_" + "MeanOfFrequencies", dsFreqDistrVals.getMean());
				// featuresForThisGram.put(n + "Gram_" + "StdDevOfFrequencies", dsFreqDistrVals.getStandardDeviation());
				// featuresForThisGram.put(n + "Gram_" + "MaxOfFrequencies", dsFreqDistrVals.getMax());
				// featuresForThisGram.put(n + "Gram_" + "MinOfFrequencies", dsFreqDistrVals.getMin());
				// featuresForThisGram.put(n + "Gram_" + "MedianOfFrequencies", dsFreqDistrVals.getPercentile(50));// median
				// featuresForThisGram.put(n + "Gram_" + "NumOfDistinctGrams", getNumOfDistinctKeys(freqDistr));
				// featuresForThisGram.put(n + "Gram_" + "NumOfDistinctGrams/SumOfFrequencies", getNumOfDistinctKeys(freqDistr) / dsFreqDistrVals.getSum());
				// featuresForThisGram.put(n + "Gram_" + "MeanOfFrequencies/SumOfFrequencies", dsFreqDistrVals.getMean() / dsFreqDistrVals.getSum());
				
				// featuresForThisGram.put("SUM_Occur" + "_" + n + "Grams", dsFreqDistrVals.getSum());
				// featuresForThisGram.put("AVG_Occur" + "_" + n + "Grams", dsFreqDistrVals.getMean());
				// featuresForThisGram.put("STD_Occur" + "_" + n + "Grams", dsFreqDistrVals.getStandardDeviation());
				// featuresForThisGram.put("MAX_Occur" + "_" + n + "Grams", dsFreqDistrVals.getMax());
				// featuresForThisGram.put("MIN_Occur" + "_" + n + "Grams", dsFreqDistrVals.getMin());
				// featuresForThisGram.put("MEDIAN_Occur" + "_" + n + "Grams", dsFreqDistrVals.getPercentile(50));// median
				
				// $$ featuresForThisGram.put("COUNT_DISTINCT" + "_" + n + "Grams", getNumOfDistinctKeys(freqDistr));
				featuresForThisGram.put("COUNT_DISTINCT" + "_" + n + "Grams" + "/SUM_Occur" + "_" + n + "Grams", getNumOfDistinctKeys(freqDistr)
						/ dsFreqDistrVals.getSum());
				// $$ featuresForThisGram.put("COUNT_DISTINCT" + "_" + n + "Grams/lengthOfTimeline", getNumOfDistinctKeys(freqDistr) / numberOfActivityObjectsInTimeline);
				
				featuresForThisGram.put("AVG_Occur" + "_" + n + "Grams" + "/SUM_Occur" + "_" + n + "Grams",
						dsFreqDistrVals.getMean() / dsFreqDistrVals.getSum());
				
				// //normalised by length of timeline
				// featuresForThisGram.put("SUM_Occur" + "_" + n + "Grams/lengthOfTimeline", dsFreqDistrVals.getSum() / numberOfActivityObjectsInTimeline);
				// featuresForThisGram.put("AVG_Occur" + "_" + n + "Grams/lengthOfTimeline", dsFreqDistrVals.getMean() / numberOfActivityObjectsInTimeline);
				featuresForThisGram.put("STD_Occur" + "_" + n + "Grams/lengthOfTimeline", dsFreqDistrVals.getStandardDeviation()
						/ numberOfActivityObjectsInTimeline);
				// featuresForThisGram.put("MAX_Occur" + "_" + n + "Grams/lengthOfTimeline", dsFreqDistrVals.getMax() / numberOfActivityObjectsInTimeline);
				featuresForThisGram.put("MIN_Occur" + "_" + n + "Grams", dsFreqDistrVals.getMin() / numberOfActivityObjectsInTimeline);
				// featuresForThisGram.put("MEDIAN_Occur" + "_" + n + "Grams/lengthOfTimeline", dsFreqDistrVals.getPercentile(50) / numberOfActivityObjectsInTimeline);// median
				// //////////////////////////////////////////////////////
				
				// //normalised by max value in case of maximum regularity
				featuresForThisGram.put("SUM_Occur" + "_" + n + "Grams/Max_SUMR", dsFreqDistrVals.getSum() / (numberOfActivityObjectsInTimeline - n));
				featuresForThisGram.put("AVG_Occur" + "_" + n + "Grams/Max_AVGR", dsFreqDistrVals.getMean() / (numberOfActivityObjectsInTimeline - n));
				featuresForThisGram.put("MAX_Occur" + "_" + n + "Grams/Max_MAXR", dsFreqDistrVals.getMax() / (numberOfActivityObjectsInTimeline - n));
				featuresForThisGram.put("MEDIAN_Occur" + "_" + n + "Grams/Max_MEDIANR", dsFreqDistrVals.getPercentile(50)
						/ (numberOfActivityObjectsInTimeline - n));// median
				// //////////////////////////////////////////////////////
				
				featuresForThisUser.put(new Integer(n), featuresForThisGram);
				
				WritingToFile.writeSimpleMapToFile(freqDistr, pathForResultFiles + n + "gram" + userID + "FreqDist.csv", "subsequence", "count");
			}
			
			features.put(userID, featuresForThisUser);
		}
		
		System.out.println("Finished NGram Analysis");
		
		return features;
	}
	
	public void writeNGramAttributes(LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> map, String fileNamePhrase)
	{
		for (Map.Entry<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> entryUser : map.entrySet()) // iterating over users
		{
			WritingToFile.appendLineToFile("User: " + entryUser.getKey() + "\n", fileNamePhrase);
			
			for (Map.Entry<Integer, LinkedHashMap<String, Double>> entryNGram : entryUser.getValue().entrySet())
			{
				WritingToFile.appendLineToFile("\tfor " + entryNGram.getKey() + "-Gram: " + "\n", fileNamePhrase);// + entryUser.getKey());
				WritingToFile.appendLineToFile("  features are:" + "\n", fileNamePhrase);
				for (Map.Entry<String, Double> entryF : entryNGram.getValue().entrySet())
				{
					WritingToFile.appendLineToFile("\t\t" + entryF.getKey() + ": " + entryF.getValue() + "\n", fileNamePhrase);
				}
			}
		}
		
	}
	
	public void addNGramAttributeVectorsToAttributeVectors(LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> map)// , String fileNamePhrase)
	{
		int count = 0;
		for (Map.Entry<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> entryUser : map.entrySet()) // iterating over users
		{
			String userID = entryUser.getKey();
			
			for (Map.Entry<Integer, LinkedHashMap<String, Double>> entryNGram : entryUser.getValue().entrySet())
			{
				// WritingToFile.appendLineToFile("\tfor " + entryNGram.getKey() + "-Gram: " + "\n", fileNamePhrase);// + entryUser.getKey());
				// WritingToFile.appendLineToFile("  features are:" + "\n", fileNamePhrase);
				
				for (Map.Entry<String, Double> entryF : entryNGram.getValue().entrySet())
				{
					// WritingToFile.appendLineToFile("\t\t" + entryF.getKey() + ": " + entryF.getValue() + "\n", fileNamePhrase);
					String featureLabel = entryF.getKey();
					Double featureValue = entryF.getValue();
					
					this.timelineAttributeVectors.get(userID).add(UtilityBelt.toPlainStringSafely(featureValue));
					
					if (count == 0) // added only once and not repeated for each user
						this.attributeLabels.add(featureLabel);
				}
				
			}
			count++;
		}
		
	}
	
	public static DescriptiveStatistics getDescriptiveStatsOfValues(LinkedHashMap<String, Long> map)
	{
		double vals[] = new double[map.size()];// , res = 0;
		
		int i = 0;
		for (Map.Entry<String, Long> entry : map.entrySet())
		{
			vals[i++] = entry.getValue();
		}
		return new DescriptiveStatistics(vals);
	}
	
	// public static double getAvgOfValues(LinkedHashMap<String, Long> map)
	// {
	// double sum = 0, res = 0;
	//
	// for (Map.Entry<String, Long> entry : map.entrySet())
	// {
	// sum += entry.getValue();
	// }
	// return sum / map.size();
	//
	// }
	
	// public static double getSumOfValues(LinkedHashMap<String, Long> map)
	// {
	// double sum = 0;// , res = 0;
	//
	// for (Map.Entry<String, Long> entry : map.entrySet())
	// {
	// sum += entry.getValue();
	// }
	// return sum;
	//
	// }
	
	// public static double getSDOfValues(LinkedHashMap<String, Long> map)
	// {
	// double vals[] = new double[map.size()];// , res = 0;
	//
	// int i = 0;
	// for (Map.Entry<String, Long> entry : map.entrySet())
	// {
	// vals[i++] = entry.getValue();
	// }
	// return;
	//
	// }
	//
	public static double getNumOfDistinctKeys(LinkedHashMap<String, Long> map)
	{
		double sum = 0, res = 0;
		
		ArrayList<String> distinct = new ArrayList<String>();
		
		for (Map.Entry<String, Long> entry : map.entrySet())
		{
			if (distinct.contains(entry.getKey()) == false)
			{
				distinct.add(entry.getKey());
			}
		}
		return distinct.size();
		
	}
	
	public boolean sanityCheckDayTimelineToTimelineConversion(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines,
			LinkedHashMap<String, Timeline> usersTimelines)
	{
		boolean sane = true;
		
		if (usersDayTimelines.size() != usersTimelines.size())
		{
			sane = false;
			UtilityBelt.showErrorExceptionPopup("Error in sanityCheckDayTimelineTimelineMatching: (usersDayTimelines.size() !=usersTimelines.size())");
		}
		
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : usersDayTimelines.entrySet())
		{
			
		}
		
		return sane;
	}
	
	/**
	 * Creates empty timeline attribute vectors, attributeLabels and sets userIDInstanceID (actual user id and its serial number in attribute vector)
	 * 
	 * @param usersDayTimelines
	 */
	public void initialiseTimelineAttributeVectors(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines)
	{
		System.out.println("Inside initialiseTimelineAttributeVectors ");
		timelineAttributeVectors = new LinkedHashMap<String, ArrayList<String>>();
		
		userIDInstanceID = new LinkedHashMap<String, Integer>();
		
		int instanceID = 1;
		
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : usersDayTimelines.entrySet())
		{
			timelineAttributeVectors.put(entry.getKey(), new ArrayList<String>());
			userIDInstanceID.put(entry.getKey(), instanceID);
			
			int indexInConstantClasss = Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()));
			
			System.out.println(" User id = " + entry.getKey() + "\t Instance ID  = " + instanceID + " while index in Constant = " + indexInConstantClasss);
			
			// Start of Sanity Check
			if (instanceID != (indexInConstantClasss + 1))
			{
				String msg = "Error in org.activity.featureExtraction.TimelinesAttributesExtraction.initialiseTimelineAttributeVectors()"
						+ "Instance ID is not matching the index of user id used in Constant";
				PopUps.showError(msg);
			}
			// End of Sanity Check
			instanceID++;
		}
		
		attributeLabels = new ArrayList<String>();
	}
	
	/**
	 * 
	 * @param usersDayTimelines
	 * @return
	 */
	public LinkedHashMap<String, Double>
			getSequenceEntropyAfterExpungingInvalids(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> sequenceAll = TimelineStats.transformToSequenceDayWise(usersDayTimelines);// , false);
		LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS = TimelineStats.toCharsFromActivityObjectsNoTimestamp(sequenceAll, true);
		LinkedHashMap<String, Double> seqEntropy = TimelineStats.getShannonEntropy(sequenceCharInvalidsExpungedNoTS);
		
		return seqEntropy;
	}
	
	/**
	 * 
	 * @param usersDayTimelines
	 * @param featureName
	 * @param m
	 * @return
	 */
	public LinkedHashMap<String, Double> getFeatureSampleEntropyAfterExpungingInvalids(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines, String featureName, int m)
	{
		// TimelineStats.performTimeSeriesAnalysis(UtilityBelt.reformatUserIDs(usersDayTimelines));//
		// LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> userLevelSampEn = TimelineStats.performSampleEntropyVsMAnalysis2(usersDayTimelines);
		LinkedHashMap<String, Double> res = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : usersDayTimelines.entrySet())
		{
			String userIDN = entry.getKey();
			// System.out.println(" m =" + m);
			
			double sampen = UtilityBelt.getValByRowCol(Constant.getCommonPath() + userIDN + featureName + "SampEn.csv", m - 1, 1, false);
			
			// System.out.println(" putting " + userIDN + " , " + sampen);
			res.put(userIDN, sampen);
		}
		
		// System.out.println("res from getFeatureSampleEntropyAfterExpungingInvalids =  " + res.isEmpty());
		
		if (res == null)
		{
			System.out.println("res from getFeatureSampleEntropyAfterExpungingInvalids is null ");
		}
		return res;
	}
	
	/**
	 * 
	 * @param usersDayTimelines
	 * @param featureName
	 * @param m
	 * @return
	 */
	public LinkedHashMap<String, Double> getAggSampleEntropyAfterExpungingInvalids(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines, String aggName, int m)
	{
		// TimelineStats.performTimeSeriesAnalysis(UtilityBelt.reformatUserIDs(usersDayTimelines));//
		// LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<String, Double>>> userLevelSampEn = TimelineStats.performSampleEntropyVsMAnalysis2(usersDayTimelines);
		LinkedHashMap<String, Double> res = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : usersDayTimelines.entrySet())
		{
			String userIDN = entry.getKey();
			double sampen = UtilityBelt.getValByRowCol(Constant.getCommonPath() + userIDN + aggName + "SampEn.csv", m - 1, 1, false);
			res.put(userIDN, sampen);
		}
		// System.out.println("res from getAggSampleEntropyAfterExpungingInvalids =  " + res.isEmpty());
		return res;
	}
	
	/**
	 * Return a Hjorth parameter (selected by parameterIndex) for the sequence of feature values of given feature name for each of the user in the given data timelines.
	 * 
	 * @param usersDayTimelines
	 * @param featureName
	 * @param parameterIndex
	 *            0 for activity, 1 for mobility, 2 for complexity
	 * @return hjorth parameter rounded to 5 decimal places
	 */
	public LinkedHashMap<String, Double> getHjorthParametersAfterExpungingInvalids(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines, String featureName, int parameterIndex)
	{
		LinkedHashMap<String, Double> res = new LinkedHashMap<String, Double>();
		
		System.out.println(" inside getHjorthParametersAfterExpungingInvalids");
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : usersDayTimelines.entrySet())
		{
			String userIDN = entry.getKey();
			// System.out.println(" parameterIndex =" + parameterIndex);
			
			double hjParam = UtilityBelt.getValByRowCol(Constant.getCommonPath() + userIDN + featureName + "HjorthParams.csv", parameterIndex, 1, false);
			// System.out.println(" putting " + userIDN + " , " + UtilityBelt.round(hjParam, 5));
			res.put(userIDN, UtilityBelt.round(hjParam, 5));
		}
		
		// System.out.println("Is res from getHjorthParametersAfterExpungingInvalids =  " + res.isEmpty());
		
		if (res == null)
		{
			System.out.println("res from getHjorthParametersAfterExpungingInvalids is null ");
		}
		return res;
	}
	
	public LinkedHashMap<String, Double> getNumberOfActivityObjects(LinkedHashMap<String, Timeline> usersTimelines)
	{
		LinkedHashMap<String, Double> res = new LinkedHashMap<String, Double>();
		
		for (Map.Entry<String, Timeline> entry : usersTimelines.entrySet())
		{
			int numOfActivityObjects = entry.getValue().countNumberOfValidActivities();
			res.put(entry.getKey(), new Double(numOfActivityObjects));
		}
		return res;
	}
	
	public LinkedHashMap<String, Double> getNumberOfValidDistinctActivities(LinkedHashMap<String, Timeline> usersTimelines)
	{
		LinkedHashMap<String, Double> res = new LinkedHashMap<String, Double>();
		
		for (Map.Entry<String, Timeline> entry : usersTimelines.entrySet())
		{
			int numOfActivityObjects = entry.getValue().countNumberOfValidDistinctActivities();
			res.put(entry.getKey(), new Double(numOfActivityObjects));
		}
		return res;
	}
	
	public LinkedHashMap<String, Double> getNumberOfValidDistinctActivitiesUponLengthOfTimeline(LinkedHashMap<String, Timeline> usersTimelines)
	{
		LinkedHashMap<String, Double> res = new LinkedHashMap<String, Double>();
		
		for (Map.Entry<String, Timeline> entry : usersTimelines.entrySet())
		{
			int numOfAOsInTimeline = entry.getValue().size();
			double numOfDistinctActivityObjects = entry.getValue().countNumberOfValidDistinctActivities();
			res.put(entry.getKey(), new Double(numOfDistinctActivityObjects / numOfAOsInTimeline));
		}
		return res;
	}
	
	/**
	 * 
	 * @return the absolute file name of the written attribute file
	 */
	public String getAttributeFilenameAbs()
	{
		return this.absoluteNameOfAttributesFile + ".csv";
	}
	
	public void addStringAttributeToAttributeVectors(LinkedHashMap<String, String> toAdd, String featureLabel)
	{
		if (toAdd == null || toAdd.size() == 0)
		{
			new Exception("Error in addStringFeatureToFeatureVectors: toAdd.size = " + toAdd.size());
		}
		for (Map.Entry<String, String> entry : toAdd.entrySet())
		{
			this.timelineAttributeVectors.get(entry.getKey()).add(entry.getValue());
		}
		this.attributeLabels.add(featureLabel);
	}
	
	/**
	 * Adds the given map of features (of type double) to the map of feature vectors
	 * 
	 * @param toAdd
	 * @param featureLabel
	 */
	public void addDoubleAttributeToAttributeVectors(LinkedHashMap<String, Double> toAdd, String featureLabel)
	{
		System.out.println("Inside addDoubleFeatureToFeatureVectors for featureLabel = " + featureLabel);
		if (toAdd == null)
		{
			new Exception("Error in addDoubleFeatureToFeatureVectors: the feature map to add is null").printStackTrace();
		}
		
		if (this.timelineAttributeVectors == null)
		{
			new Exception("Error in addDoubleFeatureToFeatureVectors: this.timelineFeatureVectors is null").printStackTrace();
		}
		if (this.timelineAttributeVectors.size() == 0)
		{
			new Exception("Error in addDoubleFeatureToFeatureVectors: this.timelineFeatureVectors is of zero size").printStackTrace();
		}
		
		for (Map.Entry<String, Double> entry : toAdd.entrySet())
		{
			// System.out.println("--->" + entry.getKey() + " ," + entry.getValue());
			this.timelineAttributeVectors.get(entry.getKey()).add(UtilityBelt.toPlainStringSafely(entry.getValue()));
		}
		this.attributeLabels.add(featureLabel);
	}
	
	public void addVectorsToFeatureVectors(LinkedHashMap<String, ArrayList<String>> toAdd, String featureLabel)
	{
		for (Map.Entry<String, ArrayList<String>> entry : toAdd.entrySet())
		{
			for (String feature : entry.getValue())
			{
				this.timelineAttributeVectors.get(entry.getKey()).add(feature);
			}
		}
		this.attributeLabels.add(featureLabel);
	}
	
	public void traverseFeatureVectors()
	{
		System.out.println("Traversing feature vectors");
		int count = 0;
		for (Map.Entry<String, ArrayList<String>> entry : timelineAttributeVectors.entrySet())
		{
			System.out.println(attributeLabels.get(count++));
			System.out.println(entry.getKey() + ": " + entry.getValue().toString());
			
			System.out.println("");
		}
		System.out.println("End of traversing feature vectors");
	}
	
}
