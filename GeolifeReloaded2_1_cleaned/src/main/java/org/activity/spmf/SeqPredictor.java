package org.activity.spmf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceStatsGenerator;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Markov.MarkovAllKPredictor;

/**
 * an intermediary to leverage the spmf package for All kth order markov model
 * 
 * @author gunjan
 *
 */
public class SeqPredictor
{
	int predictedNextSymbol = -1;
	ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase trainingSet;
	ArrayList<Integer> currentTimeline;
	Sequence currentSeq;

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
			trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 9).boxed().collect(Collectors.toList()));
			trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList()));
			trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 9).boxed().collect(Collectors.toList()));
			trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 1).boxed().collect(Collectors.toList()));
			trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 7).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList()));
			trainingSet.add((ArrayList<Integer>) IntStream.of(4, 3, 2, 5).boxed().collect(Collectors.toList()));
			trainingSet.add((ArrayList<Integer>) IntStream.of(4, 3, 2, 5).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList()));
			// trainingSet.add((ArrayList<Integer>) IntStream.of(1, 2, 3, 4, 6).boxed().collect(Collectors.toList()));

			ArrayList<Integer> curr = (ArrayList<Integer>) IntStream.of(2, 3, 4).boxed().collect(Collectors.toList());

			SeqPredictor p = new SeqPredictor(trainingSet, curr, true);
			System.out.println("predictedNextSymbol = " + p.AKOMSeqPredictor(4, true));
		}
	}

	public int getPredictedNextSymbol()
	{
		return this.predictedNextSymbol;
	}

	public SeqPredictor(ArrayList<ArrayList<Integer>> trainingTimelines, ArrayList<Integer> currentTimeline,
			boolean verbose)
	{
		try
		{

			trainingSet = toSequenceDatabase(trainingTimelines);

			// create the current sequence
			List<Item> items = currentTimeline.stream().map(i -> new Item(i)).collect(Collectors.toList());
			currentSeq = new Sequence(0, items);

			// Print the training sequences to the console
			if (verbose)
			{
				System.out.println("--- Training sequences ---");
				trainingSet.getSequences().stream().forEachOrdered(seq -> System.out.println(seq.toString()));
				System.out.println();

				// Print statistics about the training sequences
				SequenceStatsGenerator.prinStats(trainingSet, " training sequences ");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param orderOfMarkovModel
	 * @param verbose
	 * @return
	 */
	public Integer AKOMSeqPredictor(int orderOfMarkovModel, boolean verbose)
	{
		int predictedNextSymbol = -1;
		try
		{
			// Here we set the order of the markov model to 5.
			String optionalParameters = "order:" + orderOfMarkovModel;

			// Train the prediction model
			MarkovAllKPredictor predictionModel = new MarkovAllKPredictor("AKOM", optionalParameters);
			predictionModel.Train(trainingSet.getSequences());

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
	private SequenceDatabase toSequenceDatabase(List<ArrayList<Integer>> trainingTimelines)
	{
		SequenceDatabase trainingSet = new SequenceDatabase();

		List<Sequence> listOfSeq = new ArrayList<>();
		int trainingID = 1;
		for (ArrayList<Integer> timelineAsSeqOfInt : trainingTimelines)
		{
			List<Item> items = timelineAsSeqOfInt.stream().map(i -> new Item(i)).collect(Collectors.toList());
			listOfSeq.add(new Sequence(trainingID++, items));
		}

		trainingSet.setSequences(listOfSeq);

		return trainingSet;
	}

}
