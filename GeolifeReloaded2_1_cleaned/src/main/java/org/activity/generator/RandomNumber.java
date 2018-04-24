package org.activity.generator;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.io.WToFile;

public class RandomNumber
{

	private RandomNumber()
	{
	}

	public static void main(String args[])
	{
		randomlySelect100Users(1672);
	}

	/**
	 * To sample random users
	 * <p>
	 * Used on March 2 2018
	 * 
	 * @since Mar 1 2018
	 * @param args
	 */
	public static void main1(String args[])
	{
		int numOfUsersToSelect = 100;

		List<Integer> allUserIndices = IntStream.rangeClosed(1, 916).boxed().collect(Collectors.toList());
		System.out.println(allUserIndices.toString());
		Random r = new Random();
		int numOfSHuffles = 100 + r.nextInt(100) + r.nextInt(100);
		for (int i = 0; i < numOfSHuffles; i++)
		{
			Collections.shuffle(allUserIndices);
		}
		System.out.println(allUserIndices.toString());

		List<Integer> listOfSelectedIndices = allUserIndices.stream().limit(numOfUsersToSelect)
				.collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();
		listOfSelectedIndices.stream().forEachOrdered(e -> sb.append(e + "\n"));
		System.out.println(listOfSelectedIndices.toString());
		WToFile.writeToNewFile(sb.toString(), "./dataToRead/RandomlySample100UsersMar1_2018.csv");
	}

	/**
	 * To sample random users
	 * <p>
	 * 
	 * @since April 23 2018
	 * @param args
	 */
	public static void randomlySelect100Users(int totalNumOfUsers)
	{
		int numOfUsersToSelect = 100;

		List<Integer> allUserIndices = IntStream.rangeClosed(1, totalNumOfUsers).boxed().collect(Collectors.toList());
		System.out.println(allUserIndices.toString());
		Random r = new Random();
		int numOfSHuffles = 100 + r.nextInt(100) + r.nextInt(100);
		for (int i = 0; i < numOfSHuffles; i++)
		{
			Collections.shuffle(allUserIndices);
		}
		System.out.println(allUserIndices.toString());

		List<Integer> listOfSelectedIndices = allUserIndices.stream().limit(numOfUsersToSelect)
				.collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();
		listOfSelectedIndices.stream().forEachOrdered(e -> sb.append(e + "\n"));
		System.out.println(listOfSelectedIndices.toString());
		WToFile.writeToNewFile(sb.toString(), "./dataToRead/RandomlySample100UsersApril23_2018.csv");
	}

	/**
	 * extracted the logic to reusable method
	 * 
	 * @param numOfIndicesToSelect
	 * @param listOfIndicestoSelectFrom
	 * @param absFileNameToWriteSelectedIndices
	 * @return
	 * @since Mar 1 2018
	 */
	public static List<Integer> getSimpleRandomIndices(int numOfIndicesToSelect,
			List<Integer> listOfIndicestoSelectFrom, String absFileNameToWriteSelectedIndices)
	{

		System.out.println("Inside getSimpleRandomIndices()");
		System.out.println("listOfIndicestoSelectFrom = \n" + listOfIndicestoSelectFrom.toString());

		Random r = new Random();
		int numOfSHuffles = 100 + r.nextInt(100) + r.nextInt(100);
		for (int i = 0; i < numOfSHuffles; i++)
		{
			Collections.shuffle(listOfIndicestoSelectFrom);
		}
		System.out.println("suffled listOfIndicestoSelectFrom = \n" + listOfIndicestoSelectFrom.toString());

		List<Integer> listOfSelectedIndices = listOfIndicestoSelectFrom.stream().limit(numOfIndicesToSelect)
				.collect(Collectors.toList());

		if (absFileNameToWriteSelectedIndices.length() > 0)
		{
			StringBuilder sb = new StringBuilder();
			listOfSelectedIndices.stream().forEachOrdered(e -> sb.append(e + "\n"));
			System.out.println(listOfSelectedIndices.toString());
			WToFile.writeToNewFile(sb.toString(), absFileNameToWriteSelectedIndices);
		}
		return listOfSelectedIndices;
	}

}
