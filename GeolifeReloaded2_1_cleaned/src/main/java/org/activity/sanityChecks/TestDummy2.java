package org.activity.sanityChecks;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activity.io.WritingToFile;
import org.activity.objects.CheckinEntry;

/**
 * Just to check/run some random snippets of code NOT IMPORTANT
 * 
 * @author gunjan
 *
 */
public class TestDummy2
{
	public static void main1(String args[])
	{
		// checkWritePerformance();

		String commonPath = "./";

		String currentPath = System.getProperty("user.dir") + "/";
		String separator = File.separator;
		System.out.println(currentPath + separator);

		WritingToFile.appendLineToFileAbsolute("test", currentPath + separator + "DeleteMe.txt");

		WritingToFile.appendLineToFileAbsolute("test", "DeleteMe2.txt");
		// String str = " 112:Music^^ || 12600__2.1891152325934935%";
		//
		// String splitted[] = str.split("\\|\\|");
		//
		// System.out.println("splitted[0] = " + splitted[0]);

		// System.out.println(Double.NaN);
		//
		// System.out.println(("gunjan".equals("gunjan")));
		// System.out.println("gunjan".equals("manali"));
		// String s[] = { "1", "101", "201", "301", "401", "501", "601" };
		//
		// for (int i = 0; i < s.length; i++)
		// {
		// int startUserIndex = Integer.valueOf(s[i]) - 1;// 100
		// int endUserIndex = startUserIndex + 99; // 199
		//
		// int countOfSampleUsers = 0;
		// System.out.println("startUserIndex=" + startUserIndex + " endUserIndex" + endUserIndex);
		// }

		// byte c = 70;
		// byte c1 = 84;
		//
		// System.out.println("c = " + " c1=" + c1);
		// String[] b1 = { "true", "True", "1", "t" };
		//
		// for (String s : b1)
		// {
		// System.out.println(Boolean.parseBoolean(s));
		// }

	}

	public static void main(String args[])
	{
		testDirectoryDeletion();
		// List<String> list = Arrays.asList(new String[] { "gunjan", "manali", "sukany" });
		// System.out.println(list.toString());
		// list.stream().forEach(e -> System.out.print(e.toString() + "_"));

		// steamFunctionParameter(2);
		// streamExp1();
	}

	public static void testDirectoryDeletion()
	{
		WritingToFile.deleteNonEmptyDirectory(Paths.get("/home/gunjan/Downloads/test/"));
	}

	public static void sets()
	{

	}

	/**
	 * 
	 * @param choice
	 */
	public static void steamFunctionParameter(int choice)
	{
		try
		{
			Function<CheckinEntry, String> funVariable = ce -> String.valueOf(ce.getActivityID());// new Function<>();

			if (choice == 1)
			{
				funVariable = ce -> String.valueOf(ce.getActivityID());
			}
			else if (choice == 2)
			{
				funVariable = ce -> String.valueOf(ce.getTimestamp());
			}

			for (int i = 0; i < 10; i++)
			{

				CheckinEntry ce = new CheckinEntry("1", i, Timestamp.valueOf(LocalDateTime.now()), "1.1", "3.3", 15,
						"5");
				System.out.println(funVariable.apply(ce));
				System.out.println(funVariable.toString());
				// compares the choice variable for each of the 100 times, even though value of choice is known before
				// the loop evn starts.
				// Can we store the function to be called in a variable and assign it before the for loop.
				// if (choice == 1)
				// {
				// System.out.println(ce.getActivityID());
				// }
				// else if (choice == 2)
				// {
				// System.out.println(ce.getTimestamp());
				// }
			}

		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void streamExp1()
	{
		LinkedHashMap<String, ArrayList<Integer>> map = new LinkedHashMap<String, ArrayList<Integer>>();

		ArrayList<Integer> a1 = new ArrayList<Integer>();
		ArrayList<Integer> a2 = new ArrayList<Integer>();
		ArrayList<Integer> a3 = new ArrayList<Integer>();

		for (int i = 1; i <= 5; i++)
		{
			a1.add(i);
		}

		for (int i = 1; i <= 8; i++)
		{
			a2.add(i);
		}

		for (int i = 1; i <= 3; i++)
		{
			a3.add(i);
		}

		map.put("A1", a1);
		map.put("A2", a2);
		map.put("A3", a3);

		OptionalInt max = map.entrySet().stream().mapToInt(e -> e.getValue().size()).max();

		System.out.println("max = " + max.getAsInt());

		long count = map.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();

		System.out.println("count = " + count);

		map.entrySet().stream().map(e -> (e.getKey() + ":" + e.getValue())).forEach(System.out::println);

		System.out.println("-------------");
		Map<String, ArrayList<Integer>> map2 = map.entrySet().stream().limit(2)
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		map2.entrySet().stream().map(e -> (e.getKey() + ":" + e.getValue())).forEach(System.out::println);

		System.out.println("-------------");
		// LinkedHashMap<String, ArrayList<Integer>> map3 = (LinkedHashMap<String, ArrayList<Integer>>) map2;
		LinkedHashMap<String, ArrayList<Integer>> map3 = new LinkedHashMap<String, ArrayList<Integer>>(map2);
		map3.entrySet().stream().map(e -> (e.getKey() + ":" + e.getValue())).forEach(System.out::println);
	}

	public static void checkWritePerformance()
	{
		long maxIteration = 80000000;
		long st1 = System.currentTimeMillis();

		String fileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Test/writePerformance.txt";
		String msg = "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn";
		try
		{
			BufferedWriter bwSimple = WritingToFile.getBufferedWriterForNewFile(fileName, 3072);

			for (int i = 0; i < maxIteration; i++)
			{
				bwSimple.write(msg + "\n");
			}

			bwSimple.close();
			long st2 = System.currentTimeMillis();

			System.out.println("file written with bwSimple in " + ((st2 - st1) * 1.0 / 1000) + " secs");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}

	public static void streamExp1Spartanized()
	{
		LinkedHashMap<String, ArrayList<Integer>> map = new LinkedHashMap<String, ArrayList<Integer>>();

		ArrayList<Integer> a1 = new ArrayList<Integer>(), a2 = new ArrayList<Integer>(), a3 = new ArrayList<Integer>();
		for (int ¢ = 1; ¢ <= 5; ++¢)
			a1.add(¢);

		for (int ¢ = 1; ¢ <= 8; ++¢)
			a2.add(¢);

		for (int ¢ = 1; ¢ <= 3; ++¢)
			a3.add(¢);

		map.put("A1", a1);
		map.put("A2", a2);
		map.put("A3", a3);

		System.out.println("max = " + map.entrySet().stream().mapToInt(λ -> λ.getValue().size()).max().getAsInt());
	}

}
