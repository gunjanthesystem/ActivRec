package ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.Markov;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;

/*
 * This file is copyright (c) Ted Gueniche 
 * <ted.gueniche@gmail.com>
 *
 * This file is part of the IPredict project
 * (https://github.com/tedgueniche/IPredict).
 *
 * IPredict is distributed under The MIT License (MIT).
 * You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT 
 */
public class MarkovState
{

	/**
	 * Number of transitions for this state
	 */
	private Integer count;

	/**
	 * Hashmap of each transition and their respective support
	 */
	private HashMap<Integer, Integer> transitions; // outgoing states and their count

	public MarkovState()
	{
		count = 0;
		transitions = new HashMap<Integer, Integer>();
	}

	/**
	 * Returns the number of transition for this state - not the support
	 */
	public int getTransitionCount()
	{
		return count;
	}

	/**
	 * Adds or update a transition from this state
	 * 
	 * @param val
	 *            Value of the new state
	 */
	public void addTransition(Integer val)
	{

		// Getting the current value or creating it
		Integer support = transitions.get(val);
		if (support == null)
		{
			support = 0;
			count += 1;
		}

		// updating value
		support++;

		// pushing value back to the transitions map
		transitions.put(val, support);

	}

	/**
	 * 
	 * @return
	 * @deprecated by gunjan on April 24 2019 in favour of getRankedListOfBestNextStates which predict a ranked list
	 *             instead of top-1
	 */
	public Integer getBestNextState()
	{
		Integer highestCount = 0;
		Integer highestValue = null;

		Iterator<Entry<Integer, Integer>> it = transitions.entrySet().iterator();
		while (it.hasNext())
		{

			Entry<Integer, Integer> pairs = it.next();

			if ((pairs.getValue()) > highestCount)
			{
				highestCount = (pairs.getValue());
				highestValue = (pairs.getKey());
			}

		}

		return highestValue;
	}

	public String toString()
	{
		String output = "";
		Iterator<Entry<Integer, Integer>> it = transitions.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Integer, Integer> pairs = it.next();
			output += pairs.getKey() + "(" + pairs.getValue() + ") ";
		}
		return output;
	}

	// Start of added by Gunjan
	/**
	 * Fork of getBestNextState() to predict ranked list of next states, instead of just one
	 * 
	 * @return
	 * @author gunjan
	 * @since 24 April
	 */
	public LinkedHashMap<Integer, Double> getRankedListOfBestNextStates()
	{
		LinkedHashMap<Integer, Double> rankedList = new LinkedHashMap<>();

		// Integer highestCount = 0;
		// Integer highestValue = null;

		Iterator<Entry<Integer, Integer>> it = transitions.entrySet().iterator();
		while (it.hasNext())
		{

			Entry<Integer, Integer> pairs = it.next();

			if (rankedList.containsKey(pairs.getKey()))
			{
				PopUps.printTracedErrorMsgWithExit("Error: rankedList already contains: " + pairs.getKey()
						+ " didn't expect non-unique predictions. rankedList = " + rankedList);
			}
			rankedList.put(pairs.getKey(), (double) pairs.getValue());
			// if ((pairs.getValue()) > highestCount)
			// {
			// highestCount = (pairs.getValue());
			// highestValue = (pairs.getKey());
			// }
		}

		rankedList = (LinkedHashMap<Integer, Double>) ComparatorUtils.sortByValueDesc(rankedList);

		if (false)// sanityCheck
		{
			Integer bestPredFromRankedList = rankedList.entrySet().iterator().next().getKey();
			Integer bestPredFromOriginalMethod = getBestNextState();

			if (bestPredFromRankedList.equals(bestPredFromOriginalMethod) == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: Sanity check failed bestPredFromRankedList = "
						+ bestPredFromRankedList + " while bestPredFromOriginalMethod = " + bestPredFromOriginalMethod
						+ " rankedList = " + rankedList);
			}
		}

		return rankedList;
	}
	// End of added by Gunjan
}
