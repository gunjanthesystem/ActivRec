package org.activity.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.io.WritingToFile;

public class RandomNumber
{

	public RandomNumber()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[])
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
		WritingToFile.writeToNewFile(sb.toString(), "./dataToRead/RandomlySample100UsersMar1_2018.csv");
	}

}
