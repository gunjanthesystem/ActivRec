package org.activity.spmf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
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
	String trainingStats;
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

			StringBuilder sb = new StringBuilder();
			sb.append("--- Training sequences ---\n");
			trainingSet.getSequences().stream().forEachOrdered(seq -> sb.append(seq.toString() + "\n"));
			sb.append("\n");
			if (verbose)
			{
				System.out.println(sb.toString());
			} // Print statistics about the training sequences
			SequenceStatsGenerator.prinStats(trainingSet, " training sequences ");
			sb.append(getStats(trainingSet, " training sequences "));
			trainingStats = sb.toString();

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

			// Start of added on 1 Feb 2018
			// take only last n items
			items = items.subList(items.size() > orderOfMarkovModel ? (items.size() - orderOfMarkovModel) : 0,
					items.size());
			// Sanity.eq(items.size(), this.orderOfMarkovModel, "Error in getAKOMPrediction");
			System.out.println("In getAKOMPrediction: items.size()=" + items.size());
			// End of added on 1 Feb 2018

			Sequence currentSeq = new Sequence(0, items);

			// System.out.println("currentSeq = " + currentSeq);
			// Then we perform the prediction
			Sequence thePrediction = predictionModel.Predict(currentSeq);

			if (thePrediction.size() != 1)
			{
				System.err.println("Warning in AKOMPredictor thePrediction.size()= " + thePrediction.size() + "\n--"
						+ trainingStats + "---\n");
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

	/////
	/**
	 * To replicate
	 * ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceStatsGenerator.prinStats(SequenceDatabase,
	 * String) to get stats as String instead of printing.
	 * <p>
	 * This method generates statistics for a sequence database (a file)
	 * 
	 * @param path
	 *            the path to the file
	 * @param name
	 *            of the database
	 * @throws IOException
	 *             exception if there is a problem while reading the file.
	 * @since 3 Jan 2017
	 */
	public static String getStats(SequenceDatabase database, String name) throws IOException
	{

		StringBuilder sb = new StringBuilder();
		// We will calculate statistics on this sequence database.

		sb.append("---" + name + "---\nNumber of sequences : \t" + database.size() + "\n");

		int maxItem = 0;
		// we initialize some variables that we will use to generate the statistics
		java.util.Set<Integer> items = new java.util.HashSet<Integer>(); // the set of all items
		List<Integer> sizes = new ArrayList<Integer>(); // the lengths of each sequence
		List<Integer> differentitems = new ArrayList<Integer>(); // the number of different item for each sequence
		List<Integer> appearXtimesbySequence = new ArrayList<Integer>(); // the average number of times that items
																			// appearing in a sequence, appears in this
																			// sequence.
		// Loop on sequences from the database
		for (Sequence sequence : database.getSequences())
		{
			// we add the size of this sequence to the list of sizes
			sizes.add(sequence.size());

			// this map is used to calculate the number of times that each item
			// appear in this sequence.
			// the key is an item
			// the value is the number of occurences of the item until now for this sequence
			HashMap<Integer, Integer> mapIntegers = new HashMap<Integer, Integer>();

			// Loop on itemsets from this sequence
			for (Item item : sequence.getItems())
			{
				// we add the size of this itemset to the list of itemset sizes
				// If the item is not in the map already, we set count to 0
				Integer count = mapIntegers.get(item.val);
				if (count == null)
				{
					count = 0;
				}
				// otherwise we set the count to count +1
				count = count + 1;
				mapIntegers.put(item.val, count);
				// finally, we add the item to the set of items
				items.add(item.val);

				if (item.val > maxItem)
				{
					maxItem = item.val;
				}
			}

			// we add all items found in this sequence to the global list
			// of different items for the database
			differentitems.add(mapIntegers.entrySet().size());

			// for each item appearing in this sequence,
			// we put the number of times in a global list "appearXtimesbySequence"
			// previously described.
			for (Entry<Integer, Integer> entry : mapIntegers.entrySet())
			{
				appearXtimesbySequence.add(entry.getValue());
			}
		}

		// we print the statistics
		// System.out.println();
		sb.append("Number of distinct items: \t" + items.size() + "\n");
		sb.append("Largest item id: \t" + maxItem + "\n");
		sb.append("Itemsets per sequence: \t" + calculateMean(sizes) + "\n");
		sb.append("Distinct item per sequence: \t" + calculateMean(differentitems) + "\n");
		sb.append("Occurences for each item: \t" + calculateMean(appearXtimesbySequence) + "\n");
		sb.append("Size of the dataset in MB: \t"
				+ ((database.size() * 4d) + (database.size() * calculateMean(sizes) * 4d) / (1000 * 1000)) + "\n");

		return sb.toString();
	}

	/**
	 * This method calculate the mean of a list of integers
	 * 
	 * @param list
	 *            the list of integers
	 * @return the mean
	 */
	private static double calculateMean(List<Integer> list)
	{
		double sum = 0;
		for (Integer val : list)
		{
			sum += val;
		}
		return sum / list.size();
	}

}
