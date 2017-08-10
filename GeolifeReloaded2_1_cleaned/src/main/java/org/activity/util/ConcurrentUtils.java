package org.activity.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.activity.distances.AlignmentBasedDistance;
import org.activity.objects.Pair;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * 
 * @author gunjan
 *
 */
public class ConcurrentUtils
{
	public static ArrayList<Long> measureEDComputs(int num)
	{
		ArrayList<Long> time = new ArrayList<>();
		Map<String, String> words = new LinkedHashMap<>();
		// words.put("1", "gunjan");
		// words.put("2", "manali");
		// int num = 10;
		for (int i = 0; i < num; i++)
		{
			if (i % 5 == 0)
			{
				words.put(Integer.toString(i), "ajoob");
			}
			else
			{
				words.put(Integer.toString(i), RandomStringUtils.randomAlphanumeric(20).toUpperCase());
			}
		}
		// .map(e -> fun2(e.getValue()))

		StringBuilder sb = new StringBuilder();
		StringBuilder sbTime = new StringBuilder();

		long t1 = System.nanoTime();
		Map<String, String> res1 = words.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> getEditDist(e.getValue())));
		long t2 = System.nanoTime();

		long t3 = System.nanoTime();// BEST
		Map<String, String> res2 = words.entrySet().parallelStream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> getEditDist(e.getValue())));
		long t4 = System.nanoTime();

		long t5 = System.nanoTime();
		Map<String, String> res3 = words.entrySet().parallelStream()
				.collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> getEditDist(e.getValue())));
		long t6 = System.nanoTime();

		long t7 = System.nanoTime();
		Map<String, String> res4 = words.entrySet().stream()
				.collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> getEditDist(e.getValue())));
		long t8 = System.nanoTime();

		//////////
		if (false)
		{
			sb.append("words:\n");// + words.toString());
			words.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + "-" + e.getValue() + "\n"));

			sb.append("res1:\n");// + words.toString());
			res1.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + "-" + e.getValue() + "\n"));

			sb.append("res2:\n");// + words.toString());
			res2.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + "-" + e.getValue() + "\n"));

			sb.append("res3:\n");// + words.toString());

			res3.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + "-" + e.getValue() + "\n"));

			sb.append("res4:\n");// + words.toString());

			res4.entrySet().stream().forEachOrdered(e -> sb.append(e.getKey() + "-" + e.getValue() + "\n"));
			System.out.println(sb.toString());
		}
		//////////

		time.add(t2 - t1);
		time.add(t4 - t3);
		time.add(t6 - t5);
		time.add(t8 - t7);

		sbTime.append("--> t2-t1= " + (t2 - t1) + "\n");
		sbTime.append("--> t4-t3= " + (t4 - t3) + "\n");
		sbTime.append("--> t6-t5= " + (t6 - t5) + "\n");
		sbTime.append("--> t8-t7= " + (t8 - t7) + "\n");

		//

		System.out.println(sbTime.toString());
		return time;
	}

	public static String getEditDist(String s)
	{
		Pair<String, Double> d = AlignmentBasedDistance.getMySimpleLevenshteinDistance(s, "ajooba", 1, 1, 2);
		return d.toString();

		// return s.substring(0, s.length() - 2);
	}

	public static void main(String args[])
	{
		int repeatTimes = 10;

		ArrayList<Double> avgTime = new ArrayList<>(4);

		for (int i = 0; i < repeatTimes; i++)
		{
			ArrayList<Long> timeCurrent = measureEDComputs(2000);// 0000);

			// if (timeCurrent.size() != avgTime.size())
			// {
			// System.out.println("Error: diff size");
			// }

			// for (int j = 0; j < avgTime.size(); j++)
			// {
			// double
			// avgTime.add(j, avgTime.get(j)+);
			// }
		}
	}
}
