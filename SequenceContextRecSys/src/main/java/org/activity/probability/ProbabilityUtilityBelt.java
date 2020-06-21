package org.activity.probability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.objects.Pair;
import org.activity.stats.StatsUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ProbabilityUtilityBelt
{

	public ProbabilityUtilityBelt()
	{
	}

	public static void main(String args[])
	{

		// System.out.println(selectNWithoutReplacement(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18),
		// 3));
		// System.out.println(
		// selectNObjsWithoutReplacement(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18), 3));
		testSampling();
	}

	public static void testSampling()
	{
		List<Integer> seq = IntStream.rangeClosed(1, 9000).boxed().collect(Collectors.toList());
		List<String> seqS = new ArrayList<>();

		seq.stream().forEachOrdered(e -> seqS.add(e.toString()));
		// System.out.println(seq);
		// System.out.println(seqS);

		int numOfSublists = 9;
		int sizeOfEachSublist = 1000;

		double coverage = -1;

		while (coverage < 0.66)
		{
			ArrayList<ArrayList<String>> sublists = randomlySampleIntoSublists(seqS, numOfSublists, sizeOfEachSublist);
			// System.out.println("sublists= \n" + sublists);

			Pair<Integer, ArrayList<Double>> res = getIntersectionSizes(sublists);
			int numOfUniqueElements = res.getFirst();
			ArrayList<Double> intersectionSizes = res.getSecond();

			Pair<Double, String> stats = getSamplingIntersectionStats(seqS.size(), numOfSublists, sizeOfEachSublist,
					numOfUniqueElements, intersectionSizes);
			coverage = stats.getFirst();
			System.out.println(stats.getSecond());
		}
	}

	/**
	 * 
	 * @param sizeOfSeq
	 * @param numOfSublists
	 * @param sizeOfEachSublist
	 * @param numOfUniqueElements
	 * @param intersectionSizes
	 * @return
	 */
	public static Pair<Double, String> getSamplingIntersectionStats(int sizeOfSeq, int numOfSublists,
			int sizeOfEachSublist, int numOfUniqueElements, ArrayList<Double> intersectionSizes)
	{
		double coverage;
		DescriptiveStatistics intersectionStats = StatsUtils.getDescriptiveStatisticsDouble(intersectionSizes, "", "",
				false);

		coverage = StatsUtils.round((numOfUniqueElements * 1.0) / sizeOfSeq, 4);
		StringBuilder sb = new StringBuilder();
		sb.append("============================================\n");
		sb.append("numOfUniqueElements= " + numOfUniqueElements + "\n");
		sb.append("seq.size()= " + sizeOfSeq + "\n");
		sb.append("coverage = " + coverage + " = numOfUniqueElements/seq.size()" + "\n");
		sb.append("numOfSublists= " + numOfSublists + "  sizeOfEachSublist=" + sizeOfEachSublist + "\n");
		sb.append("#comparisons= " + intersectionStats.getN() + "\n");
		sb.append("mean intersection= " + StatsUtils.round(intersectionStats.getMean(), 4) + "\n");
		sb.append("mean intersection/ num of elements in list= "
				+ StatsUtils.round(intersectionStats.getMean() / sizeOfEachSublist, 4) + "\n");
		sb.append("mean intersection/ numOfUniqueElements= "
				+ StatsUtils.round(intersectionStats.getMean() / numOfUniqueElements, 4) + "\n");
		sb.append("============================================\n");
		return new Pair<>(coverage, sb.toString());
	}

	/**
	 * 
	 * @param allElements
	 * @param numOfSublists
	 * @param sizeOfEachSublist
	 * @return
	 */
	public static ArrayList<ArrayList<String>> randomlySampleIntoSublists(List<String> allElements, int numOfSublists,
			int sizeOfEachSublist)
	{
		ArrayList<ArrayList<String>> listOfSublists = new ArrayList<>();
		for (int i = 0; i < numOfSublists; i++)
		{
			listOfSublists.add((ArrayList<String>) selectNObjsWithoutReplacement(allElements, sizeOfEachSublist));
		}
		return listOfSublists;
	}

	/**
	 * 
	 * @param listOfSublists
	 * @return new Pair<>(numOfUniqueElements, intersectionSizes);
	 */
	public static <T> Pair<Integer, ArrayList<Double>> getIntersectionSizes(ArrayList<ArrayList<T>> listOfSublists)
	{
		int numOfUniqueElements = -1;
		Set<T> allUniqueElements = new LinkedHashSet<>();

		for (List<T> listA : listOfSublists)
		{
			allUniqueElements.addAll(listA);
		}
		numOfUniqueElements = allUniqueElements.size();

		ArrayList<Double> intersectionSizes = new ArrayList<>();

		for (int i = 0; i < listOfSublists.size(); i++)
		{
			for (int j = i + 1; j < listOfSublists.size(); j++)
			{

				Set<T> intersection = UtilityBelt.getIntersection2(listOfSublists.get(i), listOfSublists.get(j));
				intersectionSizes.add((double) intersection.size());
				// System.out.println("comparing i=" + i + " j=" + j + " intersection.size()=" + intersection.size());
			}
		}

		return new Pair<>(numOfUniqueElements, intersectionSizes);
		// return StatsUtils.getDescriptiveStatisticsDouble(intersectionSizes, "IntersectionStats",
		// "IntersectionStats");
	}

	/**
	 * Returns true with given probability
	 * 
	 * @param p
	 * @return
	 */
	public static boolean trueWithProbability(double p)
	{
		double randomNumber = Math.random();

		if (randomNumber < p)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 
	 * @param allElements
	 * @param N
	 * @return
	 */
	public static List<Integer> selectNWithoutReplacement(List<Integer> allElements, int N)
	{
		List<Integer> box = new ArrayList<>(allElements);
		List<Integer> result = new ArrayList<>(N);

		while (result.size() != N)
		{
			Collections.shuffle(box);
			Integer pickedElement = box.get(0);
			result.add(pickedElement);
			box.remove(pickedElement);
		}

		return result;
	}

	/**
	 * 
	 * @param <T>
	 * @param allElements
	 * @param N
	 * @return
	 */
	public static <T> List<T> selectNObjsWithoutReplacement(List<T> allElements, int N)
	{
		List<T> box = new ArrayList<>(allElements); // TODO Is shallow copy okay here?
		List<T> result = new ArrayList<>(N);

		while (result.size() != N)
		{
			Collections.shuffle(box);
			T pickedElement = box.get(0);
			result.add(pickedElement);
			box.remove(pickedElement);
		}

		return result;
	}

}
