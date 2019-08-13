package org.activity.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;

public class RandomNumber
{

	private RandomNumber()
	{
	}

	public static void main(String args[])
	{
		// $$randomlySelectNUserIndices(1666, 100);
		// randomlySelectNUserIndicesExcept(1666, 100, "./dataToRead/RandomlySample100UsersSetBApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersApril24_2018.csv");

		// randomlySelectNUserIndicesExcept(1666, 100, "./dataToRead/RandomlySample100UsersSetCApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersSetBApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersApril24_2018.csv");

		// randomlySelectNUserIndicesExcept(1666, 100, "./dataToRead/RandomlySample100UsersSetDApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersSetCApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersSetBApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersApril24_2018.csv");

		// randomlySelectNUserIndicesExcept(1666, 100, "./dataToRead/RandomlySample100UsersSetEApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersSetDApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersSetCApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersSetBApril24_2018.csv",
		// "./dataToRead/RandomlySample100UsersApril24_2018.csv");

		ensureAllSetsAreDisjointWithUniqueElements("./dataToRead/RandomlySample100UsersSetEApril24_2018.csv",
				"./dataToRead/RandomlySample100UsersSetDApril24_2018.csv",
				"./dataToRead/RandomlySample100UsersSetCApril24_2018.csv",
				"./dataToRead/RandomlySample100UsersSetBApril24_2018.csv",
				"./dataToRead/RandomlySample100UsersApril24_2018.csv");
	}

	/**
	 * 
	 * @param absFileNamesOfSets
	 * @return
	 */
	private static boolean ensureAllSetsAreDisjointWithUniqueElements(String... absFileNamesOfSets)
	{
		int totalSize = 0;
		Set<Double> allIndicesReadUnique = new LinkedHashSet<>();

		for (String file : absFileNamesOfSets)
		{
			List<Double> dataRead = ReadingFromFile.oneColumnReaderDouble(file, ",", 0, false);
			totalSize += dataRead.size();
			allIndicesReadUnique.addAll(dataRead);
		}

		System.out.println("totalSize= " + totalSize);
		System.out.println("(allIndicesReadUnique.size() == totalSize)= " + (allIndicesReadUnique.size() == totalSize));

		return (allIndicesReadUnique.size() == totalSize);
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
	 * @param totalNumOfUsers
	 * @param numOfUsersToSelect
	 * @since April 23 2018
	 */
	public static void randomlySelectNUserIndices(int totalNumOfUsers, int numOfUsersToSelect)
	{
		// int numOfUsersToSelect = 100;
		List<Integer> allUserIndices = IntStream.rangeClosed(0, totalNumOfUsers - 1).boxed()
				.collect(Collectors.toList());

		System.out.println(allUserIndices.toString());
		Random r = new Random();

		// Shuffle random number of times
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
		// WToFile.writeToNewFile(sb.toString(), "./dataToRead/RandomlySample100UsersApril23_2018.csv");
		WToFile.writeToNewFile(sb.toString(), "./dataToRead/RandomlySample100UsersApril24_2018.csv");
	}

	/**
	 * Fork of randomlySelectNUserIndices
	 * <p>
	 * To sample random users
	 * 
	 * @param totalNumOfUsers
	 * @param numOfUsersToSelect
	 * @param absFileNameToWrite
	 * @param absFileNamesForIndicesToExclude
	 * @since April 28 2018
	 */
	public static void randomlySelectNUserIndicesExcept(int totalNumOfUsers, int numOfUsersToSelect,
			String absFileNameToWrite, String... absFileNamesForIndicesToExclude)
	{
		List<Integer> allUserIndices = IntStream.rangeClosed(0, totalNumOfUsers - 1).boxed()
				.collect(Collectors.toList());

		System.out.println("\nallUserIndices: size()=" + allUserIndices.size() + "\n" + allUserIndices.toString());

		List<Integer> allIndicesToExclude = new ArrayList<>();
		for (String absFileNameForIndicesToExclude : absFileNamesForIndicesToExclude)
		{
			List<Double> indicesToExclude = ReadingFromFile.oneColumnReaderDouble(absFileNameForIndicesToExclude, ",",
					0, false);
			indicesToExclude.stream().forEachOrdered(d -> allIndicesToExclude.add(d.intValue()));
		}

		System.out.println(
				"\nallIndicesToExclude:size()=" + allIndicesToExclude.size() + "\n" + allIndicesToExclude.toString());

		Random r = new Random();

		// remove user indices to exclude
		allUserIndices.removeAll(allIndicesToExclude);

		System.out.println(
				"\nallIndices after Exclude:size()=" + allUserIndices.size() + "\n" + allUserIndices.toString());

		// Shuffle random number of times
		int numOfSHuffles = 100 + r.nextInt(100) + r.nextInt(100);
		for (int i = 0; i < numOfSHuffles; i++)
		{
			Collections.shuffle(allUserIndices);
		}

		List<Integer> listOfSelectedIndices = allUserIndices.stream().limit(numOfUsersToSelect)
				.collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();
		listOfSelectedIndices.stream().forEachOrdered(e -> sb.append(e + "\n"));

		System.out.println("\nlistOfSelectedIndices:size()=" + listOfSelectedIndices.size() + "\n"
				+ listOfSelectedIndices.toString());

		// WToFile.writeToNewFile(sb.toString(), "./dataToRead/RandomlySample100UsersApril23_2018.csv");
		WToFile.writeToNewFile(sb.toString(), absFileNameToWrite);
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
