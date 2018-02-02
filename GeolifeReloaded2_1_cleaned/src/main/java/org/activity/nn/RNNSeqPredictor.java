package org.activity.nn;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.activity.constants.Constant;

public class RNNSeqPredictor extends BasicRNNForSeqRecSys
{
	private static LinkedHashMap<String, RNNSeqPredictor> RNNPredictorsForEachUserStored = new LinkedHashMap<>();
	private String userID;
	String trainingStats;

	public static final RNNSeqPredictor getRNNPredictorsForEachUserStored(String userID)
	{
		return RNNPredictorsForEachUserStored.get(userID);
	}

	public String getUserID()
	{
		return userID;
	}

	/**
	 * delete all stored rnnPredictorsForEachUserStored because the stored users wont be involved anymore. This is
	 * because this class was becoming too heavy and we need to make it light.
	 */
	public static final void clearRNNPredictorsForEachUserStored()
	{
		RNNPredictorsForEachUserStored.clear();
	}

	public RNNSeqPredictor(ArrayList<ArrayList<Integer>> trainingTimelines, int numOfNeuronsInHiddenLayers,
			int numOfHiddenLayers, int numOfEpochs, int numOfIterations, double learningRate, boolean verbose,
			String userID)
	{
		super(numOfNeuronsInHiddenLayers, numOfHiddenLayers, numOfEpochs, numOfIterations, learningRate, verbose);
		long t1 = System.currentTimeMillis();

		// System.out.println("Instanting AKOMSeqPredictor: " + PerformanceAnalytics.getHeapInformation());
		try
		{
			this.userID = userID;

			// Print the training sequences to the console

			// convert list of training timelines to a single string.
			String timelineString = toTimelineString(trainingTimelines);

			if (Constant.sameAKOMForAllRTsOfAUser)
			{
				RNNPredictorsForEachUserStored.put(userID, this);
			}
		}
		catch (Exception e)
		{
		}

		System.out.println(
				"Trained RNNSeqPredictor for user " + userID + "  in " + (System.currentTimeMillis() - t1) + "ms");
	}

	/**
	 * Convert list of lists of integers to a single char string
	 * 
	 * @param trainingTimelines
	 * @return
	 */
	private String toTimelineString(ArrayList<ArrayList<Integer>> trainingTimelines)
	{
		StringBuilder sb = new StringBuilder();

		for (ArrayList<Integer> timelineAsSeqOfInt : trainingTimelines)
		{
		}

		return null;
	}

}
