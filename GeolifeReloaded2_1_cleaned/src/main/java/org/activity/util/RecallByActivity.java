package org.activity.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class RecallByActivity
{
	static String path = "/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global new without 99 1000/";
	static final String[] activityNames = { "Others", "Commuting", "Computer", "Eating", "Exercising", "Housework",
			"On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV", "Unknown" };

	public static void main(String[] args)
	{
		BufferedReader brActual, brRecall1;

		LinkedHashMap<String, Integer> countActivityRecallIs1 = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> countActivityInActual = new LinkedHashMap<String, Integer>();

		for (String s : activityNames)
		{
			countActivityRecallIs1.put(s, 0);
			countActivityInActual.put(s, 0);
		}
		try
		{
			brActual = new BufferedReader(new FileReader(path + "dataActual.csv"));
			brRecall1 = new BufferedReader(new FileReader(path + "AlgoAlltop5Recall.csv"));
			String actualLine, recall1Line;
			while ((actualLine = brActual.readLine()) != null)
			{
				recall1Line = brRecall1.readLine();

				String[] tokensInActualLine = actualLine.split(",");
				String[] tokensInRecall1Line = recall1Line.split(",");

				for (int i = 0; i < tokensInActualLine.length; i++)

				{
					countActivityInActual.put(tokensInActualLine[i],
							countActivityInActual.get(tokensInActualLine[i]) + 1);

					// System.out.println((tokensInRecall1Line[i]));
					if (tokensInRecall1Line[i].equals("1.0"))
					{
						countActivityRecallIs1.put(tokensInActualLine[i],
								countActivityRecallIs1.get(tokensInActualLine[i]) + 1);
					}
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Count as current activity");
		for (Entry<String, Integer> entry : countActivityInActual.entrySet())
		{
			System.out.println(/* entry.getKey()+","+ */entry.getValue());
			// do stuff
		}

		System.out.println("\nCount of times recall is 1");
		for (Entry<String, Integer> entry : countActivityRecallIs1.entrySet())
		{
			System.out.println(/* entry.getKey()+","+ */entry.getValue());
			// do stuff
		}
	}

}
