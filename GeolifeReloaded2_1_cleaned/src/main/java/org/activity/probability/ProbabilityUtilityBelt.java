package org.activity.probability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProbabilityUtilityBelt
{

	public ProbabilityUtilityBelt()
	{
	}

	public static void main(String args[])
	{

		System.out.println(selectNWithoutReplacement(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18), 3));
		System.out.println(
				selectNObjsWithoutReplacement(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18), 3));
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
