package org.activity.spmf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.constants.Constant;
import org.activity.ui.PopUps;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceStatsGenerator;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Markov.MarkovAllKPredictor;

/**
 * Fork of AKOMSeqPredictor for a more lightweight version
 * <p>
 * an intermediary to leverage the spmf package for All kth order markov model
 * 
 * @author gunjan
 *
 */
public class AKOMSeqPredictorLighter
{
	// int predictedNextSymbol = -1;
	// ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase trainingSet;
	// ArrayList<Integer> currentTimeline;
	// Sequence currentSeq;
	MarkovAllKPredictor predictionModel;
	int orderOfMarkovModel;
	/**
	 * <UserID, SeqPredictor> Used in case of PureAKOM when we do not need to retrain AKOM model for each RT of a user
	 * separately.
	 */
	private static LinkedHashMap<String, AKOMSeqPredictorLighter> seqPredictorsForEachUserStored = new LinkedHashMap<>();

	public static final AKOMSeqPredictorLighter getSeqPredictorsForEachUserStored(String userID)
	{
		return seqPredictorsForEachUserStored.get(userID);
	}

	/**
	 * delete all stored AKOMSeqPredictorLighter because the stored users wont be involved anymore. This is because this
	 * class was becoming too heavy and we need to make it light.
	 */
	public static final void clearSeqPredictorsForEachUserStored()
	{
		seqPredictorsForEachUserStored.clear();
	}

	public static void main(String args[])
	{

		// List<Integer> a1 = IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList());
		// List<Integer> a2 = IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList());
		// List<Integer> a3 = IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList());
		// List<Integer> a4 = IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList());
		// List<Integer> a5 = IntStream.of(4, 3, 2, 5).boxed().collect(Collectors.toList());
		// List<Integer> a6 = IntStream.of(4, 3, 2, 5).boxed().collect(Collectors.toList());
		// List<Integer> a7 = IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList());
		// List<Integer> a8 = IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList());

		// for (int i = 0; i < 25; i++)
		{
			ArrayList<ArrayList<Integer>> trainingSet = new ArrayList<>();

			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 9).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 8, 7).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 8, 1).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 7).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4,6).boxed().collect(Collectors.toList())); //
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3,
			// 4, 6).boxed().collect(Collectors.toList())); trainingSet.add((ArrayList<Integer>) IntStream.of(4, 3, 2,
			// 5).boxed().collect(Collectors.toList())); trainingSet.add((ArrayList<Integer>) IntStream.of(4, 3, 2,
			// 5).boxed().collect(Collectors.toList()));
			// /
			trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 8, 9, 1, 2, 3, 4, 6, 1, 2, 3, 8, 7, 1, 2, 3, 8,
					1, 1, 2, 3, 4, 7, 4, 3, 2, 5, 4, 3, 2, 5).boxed().collect(Collectors.toList()));

			// trainingSet.add((ArrayList<Integer>) IntStream.of(4, 3, 2, 5).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList()));

			ArrayList<Integer> curr = (ArrayList<Integer>) IntStream.of(2, 3).boxed().collect(Collectors.toList());

			AKOMSeqPredictorLighter p = new AKOMSeqPredictorLighter(trainingSet, 4, true, "dummy");
			System.out.println("predictedNextSymbol = " + p.getAKOMPrediction(curr, true));
		}
	}

	// public int getPredictedNextSymbol()
	// {
	// return this.predictedNextSymbol;
	// }

	/**
	 * Sets up and trains
	 * 
	 * @param trainingTimelines
	 * @param orderOfMarkovModel
	 * @param user
	 */
	public AKOMSeqPredictorLighter(ArrayList<ArrayList<Integer>> trainingTimelines, int orderOfMarkovModel,
			boolean verbose, String userID)
	{
		long t1 = System.currentTimeMillis();
		// System.out.println("Instanting AKOMSeqPredictor: " + PerformanceAnalytics.getHeapInformation());
		try
		{
			SequenceDatabase trainingSet = toSequenceDatabase(trainingTimelines);

			// Print the training sequences to the console
			if (verbose)
			{
				System.out.println("--- Training sequences ---");
				trainingSet.getSequences().stream().forEachOrdered(seq -> System.out.println(seq.toString()));
				System.out.println();
				// Print statistics about the training sequences
				SequenceStatsGenerator.prinStats(trainingSet, " training sequences ");
			}

			// Here we set the order of the markov model to 5.
			String optionalParameters = "order:" + orderOfMarkovModel;
			this.orderOfMarkovModel = orderOfMarkovModel;

			// Train the prediction model
			this.predictionModel = new MarkovAllKPredictor("AKOM", optionalParameters);
			this.predictionModel.Train(trainingSet.getSequences());

			// be careful of storing this as this object can consume a lot of memory
			if (Constant.sameAKOMForAllRTsOfAUser)
			{
				seqPredictorsForEachUserStored.put(userID, this);
			}
			// System.out.println("After training in AKOMSeqPredictor: " + PerformanceAnalytics.getHeapInformation());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.printTracedErrorMsg("");
		}

		// better to exit than to continue with error
		// catch (Error e)
		// {
		// e.printStackTrace();
		// PopUps.printTracedErrorMsg("");
		// System.out.println("Error in AKOMSeqPredictor(): " + PerformanceAnalytics.getHeapInformation());
		// }
		System.out.println("Trained AKOM for user " + userID + "  in " + (System.currentTimeMillis() - t1) + "ms");
	}

	/**
	 * 
	 * @param currentTimeline
	 * @param verbose
	 * @return
	 */
	public Integer getAKOMPrediction(ArrayList<Integer> currentTimeline, boolean verbose)
	{
		int predictedNextSymbol = -1;
		try
		{
			// create the current sequence
			List<Item> items = currentTimeline.stream().map(i -> new Item(i)).collect(Collectors.toList());
			Sequence currentSeq = new Sequence(0, items);

			// System.out.println("currentSeq = " + currentSeq);
			// Then we perform the prediction
			Sequence thePrediction = predictionModel.Predict(currentSeq);

			if (thePrediction.size() != 1)
			{
				System.err.println("Error in AKOMPredictor thePrediction.size()!=1, thePrediction.size()= "
						+ thePrediction.size());
			}
			else
			{
				predictedNextSymbol = Integer.valueOf(thePrediction.get(0).toString());
			}
			if (verbose)
			{
				System.out.println("For the sequence " + currentSeq.toString()
						+ ", the prediction for the next symbol is: +" + thePrediction);
				System.out.println("Order of markov model = " + orderOfMarkovModel);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return predictedNextSymbol;
	}

	/**
	 * 
	 * @param trainingTimelines
	 * @return
	 */
	private SequenceDatabase toSequenceDatabase(List<ArrayList<Integer>> trainingTimelines) throws Exception, Error
	{
		SequenceDatabase trainingSet = new SequenceDatabase();

		List<Sequence> listOfSeq = new ArrayList<>();
		int trainingID = 1;
		for (ArrayList<Integer> timelineAsSeqOfInt : trainingTimelines)
		{
			// List<Item> items = timelineAsSeqOfInt.stream().map(i -> new Item(i)).collect(Collectors.toList());
			// running out of memory GC overhead exception when all train data involved

			// writing alternatively to do it sequentially
			List<Item> timelineAsListOfItems = new ArrayList<>(timelineAsSeqOfInt.size());
			for (int i : timelineAsSeqOfInt)
			{
				timelineAsListOfItems.add(new Item(i));
			}
			// start of sanity check PASSED OK
			// if (items.equals(timelineAsListOfItems))
			// {System.out.println("Sanity Check 15 Dec 2017 passed");}
			// else
			// {System.err.println("Sanity Check 15 Dec 2017 FAILED:");}
			// end of sanity check
			listOfSeq.add(new Sequence(trainingID++, timelineAsListOfItems));
		}

		trainingSet.setSequences(listOfSeq);

		return trainingSet;
	}

}
