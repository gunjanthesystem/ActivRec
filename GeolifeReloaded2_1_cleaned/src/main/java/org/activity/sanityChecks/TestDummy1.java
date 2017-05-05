package org.activity.sanityChecks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.activity.util.RegexUtils;

import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.IntArraySerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
//import com.esotericsoftware.kryo.serializers.MapSerializer.BindMap;
import com.esotericsoftware.kryo.serializers.MapSerializer.BindMap;

/**
 * Class for testing or confirming experimental code snippets
 * 
 * @author gunjan
 *
 */
public class TestDummy1
{

	@BindMap(valueSerializer = IntArraySerializer.class, keySerializer = StringSerializer.class,
			valueClass = int[].class, keyClass = String.class, keysCanBeNull = false)
	Map map;

	// @Bind(StringSerializer.class)
	// Object obj;

	int var1 = 2, var2 = 4;

	int var3 = 5 * var2;

	TestDummy1()
	{
		// FieldSerializer fs = new FieldSerializer(null, null);
		// fs.
	}

	void fun1()
	{
		System.out.println("var 3 = " + var3);
	}

	public static void main0(String args[])
	{
		// System.out.println(System.getProperty("user.dir"));
		// stringSplitPerformance();

		String s = "manali";

		fun1(s);
		System.out.println(s);

	}

	static void fun1(String s)
	{
		s = "gunjan";
	}

	public static void stringSplitPerformance()
	{
		String s1 = "gunjan,manali,neha";
		String[] splitted1 = null, splitted2 = null, splitted3 = null;
		long timeTaken1 = -1, timeTaken2 = -1, timeTaken3 = -1;
		int numOfIterations = 1000000;
		String delimiter = ",";
		/////////////////////////////////////////
		long begin = System.nanoTime();
		for (int i = 0; i < numOfIterations; i++)
		{
			splitted1 = s1.split(Pattern.quote(delimiter));
		}
		long end = System.nanoTime();
		timeTaken1 = end - begin;
		System.out.println(" time taken for split pattern quote = " + timeTaken1 + "ns");
		/////////////////////////////////////////
		begin = System.nanoTime();
		for (int i = 0; i < numOfIterations; i++)
		{
			Pattern doubleUnderScorePattern = RegexUtils.patternComma;// Pattern.compile(delimiter);
			splitted2 = doubleUnderScorePattern.split(s1);
		}
		end = System.nanoTime();
		timeTaken2 = end - begin;
		System.out.println(" time taken for pattern compile = " + timeTaken2 + "ns");
		/////////////////////////////////////////
		begin = System.nanoTime();
		Pattern doubleUnderScorePattern = RegexUtils.patternComma;
		for (int i = 0; i < numOfIterations; i++)
		{
			splitted3 = doubleUnderScorePattern.split(s1);
		}
		end = System.nanoTime();
		timeTaken3 = end - begin;
		System.out.println(" time taken for pattern precompiled = " + timeTaken3 + "ns");
		/////////////////////////////////////////

		double times = (timeTaken1 * 1.0 / timeTaken2);
		System.out.println("timeTaken1/timeTaken2 = " + times);

		times = (timeTaken1 * 1.0 / timeTaken3);
		System.out.println("timeTaken1/timeTaken3 = " + times);
		// System.out.println(Arrays.stream(splitted1).forEach(e -> System.out.println(e.toString())));
		/////////////////////////////////////////

		System.out.println("Splitted 1 = ");
		for (String s : splitted1)
		{
			System.out.println(s);
		}
		System.out.println("Splitted 2 = ");
		for (String s : splitted2)
		{
			System.out.println(s);
		}

		System.out.println("Splitted 3 = ");
		for (String s : splitted3)
		{
			System.out.println(s);
		}
	}

	public static void main1(String args[])
	{
		// String rawPathToRead =
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/last.fm
		// dataset/lastfm-dataset/lastfm-dataset-1K/";
		// String pathToParse = rawPathToRead + "userid-timestamp-artid-artname-traid-traname.tsv";
		// ReadingFromFile.getNumOfLines(pathToParse);
		// TestDummy1 td = new TestDummy1();

		ArrayList<Integer> arr = new ArrayList<Integer>();
		arr.add(10);
		arr.add(20);
		arr.add(30);
		arr.add(42);

		Double avg = arr.stream().mapToDouble(a -> a).average().getAsDouble();
		// System.out.println("Avg =" + avg);

		ArrayList<Integer> arr2 = new ArrayList<Integer>();
		arr2.add(10);

		arr2.add(20);
		arr2.add(30);
		arr2.add(42);

		System.out.println("arr.equals(arr2) =" + arr.equals(arr2));
		// td.fun1();
	}

	public static String testStringBuilder()
	{
		StringBuilder sb = new StringBuilder("ajooba");

		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				sb.append("i = " + i + "j = " + j + ": ajooba\n");
			}
		}
		return sb.toString();
	}

	public static void main2(String args[])
	{
		int a = 1;
		char aChar = (char) (a + '0'); // http://stackoverflow.com/questions/17984975/convert-int-to-char-in-java
		System.out.println(aChar);
		// System.out.println(testStringBuilder());
	}

	public static void main(String args[])
	{
		HashMap<String, ArrayList<String>> map = new HashMap<>();

		map.put("India", new ArrayList<String>(Arrays.asList("New Delhi", "Mumbai")));
		map.put("China", new ArrayList<String>(Arrays.asList("Beijing", "Shanghai")));
		map.put("Indonesia", new ArrayList<String>(Arrays.asList("Jakarta", "Surabaya")));

		map.get("India").add("Kolkata");

		List<String> citiesOfIndia = map.get("India");
		citiesOfIndia.add("Bangalore");

		StringBuilder sb = new StringBuilder();
		map.entrySet().stream().peek(e -> sb.append("\n" + e.getKey() + "\t")).map(e -> e.getValue())
				.flatMap(v -> v.stream()).forEachOrdered(v -> sb.append(v + ","));

		System.out.println(sb.toString());
	}

	// public static void checkMemory
}
