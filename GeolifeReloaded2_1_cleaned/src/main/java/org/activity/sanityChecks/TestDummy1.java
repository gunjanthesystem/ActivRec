package org.activity.sanityChecks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.activity.stats.StatsUtils;
import org.activity.util.RegexUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.IntArraySerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
//import com.esotericsoftware.kryo.serializers.MapSerializer.BindMap;
import com.esotericsoftware.kryo.serializers.MapSerializer.BindMap;

import jViridis.ColorMap;

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
		// $$ checkReferencing();
		checkNDArray();
	}

	public static void checkNDArray()
	{
		INDArray input135 = Nd4j.zeros(1, 3, 5);
		// System.out.println("input135=\n" + input135.toString());
	}

	/**
	 * 
	 */
	public static void checkReferencing()
	{
		HashMap<String, ArrayList<String>> map;
		map = new HashMap<>();
		map.put("India", new ArrayList<String>(Arrays.asList("New Delhi", "Mumbai")));
		map.put("China", new ArrayList<String>(Arrays.asList("Beijing", "Shanghai")));
		map.put("Indonesia", new ArrayList<String>(Arrays.asList("Jakarta", "Surabaya")));
		map.put("Ireland", new ArrayList<String>(Arrays.asList("Dublin", "Cork", "Galway")));
		map.put("Canada", new ArrayList<String>(Arrays.asList("Vancouver", "Toronto", "Montreal")));

		map.get("India").add("Kolkata");

		List<String> citiesOfIndia = map.get("India");
		citiesOfIndia.add("Bangalore");

		printMapOfListsVals("map", map);

		HashMap<String, ArrayList<String>> mapFromChangeValOnPassedObj = changeValOnPassedObj(map, "India");
		printMapOfListsVals("mapFromChangeValOnPassedObj", mapFromChangeValOnPassedObj);
		printMapOfListsVals("map", map);

		HashMap<String, ArrayList<String>> mapFromChangeValOnWorkingCopy = changeValOnWorkingCopy(map, "China");
		printMapOfListsVals("mapFromChangeValOnWorkingCopy", mapFromChangeValOnWorkingCopy);
		printMapOfListsVals("map", map);

		HashMap<String, ArrayList<String>> mapFromChangeValOnClone = changeValOnClone(map, "Indonesia");
		printMapOfListsVals("mapFromChangeValOnClone", mapFromChangeValOnClone);
		printMapOfListsVals("map", map);

		HashMap<String, ArrayList<String>> mapFromChangeValOnDefensiveCopyinge = changeValOnTypicalDefensiveCopying(map,
				"Canada");
		printMapOfListsVals("mapFromChangeValOnDefensiveCopyinge", mapFromChangeValOnDefensiveCopyinge);
		printMapOfListsVals("map", map);
		/**
		 * Lesson: clone() is not sufficient as it provide only shallow copy and simply creating a new
		 * declaration/pointer to the reference (which i used to do sometimes earlier) is not sufficient.
		 */
	}

	private static void printMapOfListsVals(String label, HashMap<String, ArrayList<String>> map)
	{
		StringBuilder sb = new StringBuilder(label + "=");
		map.entrySet().stream().peek(e -> sb.append("\n" + e.getKey() + "\t")).map(e -> e.getValue())
				.flatMap(v -> v.stream()).forEachOrdered(v -> sb.append(v + ","));
		System.out.println(sb.toString() + "\n--------\n");
	}

	private static HashMap<String, ArrayList<String>> changeValOnPassedObj(HashMap<String, ArrayList<String>> map2,
			String keyToDelete)
	{
		map2.remove(keyToDelete);
		return map2;
	}

	private static HashMap<String, ArrayList<String>> changeValOnWorkingCopy(HashMap<String, ArrayList<String>> map2,
			String keyToDelete)
	{
		HashMap<String, ArrayList<String>> mapWC = map2;
		mapWC.remove(keyToDelete);
		return mapWC;
	}

	private static HashMap<String, ArrayList<String>> changeValOnClone(HashMap<String, ArrayList<String>> map2,
			String keyToDelete)
	{
		HashMap<String, ArrayList<String>> mapWC = (HashMap<String, ArrayList<String>>) map2.clone();
		mapWC.get("Ireland").add("InvalidCity");
		mapWC.remove(keyToDelete);
		return mapWC;
	}

	private static HashMap<String, ArrayList<String>> changeValOnTypicalDefensiveCopying(
			HashMap<String, ArrayList<String>> map2, String keyToDelete)
	{
		HashMap<String, ArrayList<String>> mapWC = new HashMap<>(map2);
		mapWC.get("Ireland").add("InvalidCity2");
		mapWC.remove(keyToDelete);
		return mapWC;
	}

	public static void main3(String args[])
	{
		// String[] arr = new String[] { "Gunjan", "Manali" };
		// ArrayList<String> list = (ArrayList<String>) Arrays.stream(arr).collect(Collectors.toList());
		// System.out.println("list = " + list.toString());

		double arr1[] = { 1, 2, 3, 4, 5, 17 };

		ArrayList<Double> list1 = (ArrayList<Double>) DoubleStream.of(arr1).boxed().collect(Collectors.toList());

		System.out.println("mean = " + StatsUtils.meanOfArrayList(list1, 4));
		System.out.println("median = " + StatsUtils.medianOfArrayList(list1, 4));

		double arr2[] = { 1, 0, 1, 2, 3, 4, 5, 17 };
		for (int j = 0; j < 3; j++)
		{
			System.out.println("j = " + j);
			System.out.println(DoubleStream.of(arr2).boxed().limit(j + 1).peek(v -> System.out.print(v + ","))
					.allMatch(v -> v == 1));
		}

	}

	public static void colorPalleteTests()
	{
		ColorMap v = ColorMap.getInstance(ColorMap.VIRIDIS);// .show(this, 50, 30);
		System.out.println("v.getColorMapLength()= " + v.getColorMapLength());
		StringBuilder sb = new StringBuilder();
		Arrays.asList(v.getColorPalette(50)).stream().forEachOrdered(e -> sb.append(e.toString() + "\n"));
		System.out.println(sb.toString());
	}

	public static void main4(String args[])
	{
		// colorPalleteTests();
		// bafflingBooleanTest();
		// nanTest();

		String s = "/home/gunjankumar/SyncedWorkspace/Aug2Workspace/GeolifeReloaded2_1_cleaned/dataWritten/APR27PureAKOMOrder3/AllPerDirectTopKAgreements_3.csv";
		String[] splitted = s.split("Order");
		String[] splitted2 = splitted[1].split("/");
		double muForOrder = Double.valueOf(splitted2[0]);
		System.out.println(Arrays.asList(splitted).toString());
		System.out.println(Arrays.asList(splitted2).toString());
		System.out.println(muForOrder);
	}

	private static void nanTest()
	{
		System.out.println(1 * 123 + 0 * Double.NaN);

	}

	public static void bafflingBooleanTest()
	{
		boolean val1 = false;

		for (int i = 0; i < 10; i++)
		{
			val1 = (i == 5) ? true : false;

		}
		System.out.println("val1 = " + val1);
	}

	public static void main_(String args[])
	{

		List<Integer> items = IntStream.of(1, 2, 3, 4).boxed().collect(Collectors.toList());
		int orders[] = { 1, 2, 3, 5, 8 };
		System.out.println("items = " + items);
		for (int order : orders)
		{
			List<Integer> newitems = items.subList(items.size() > order ? (items.size() - order) : 0, items.size());
			System.out.println("order=" + order + "\nnewitems= " + newitems);
		}
		// compareToString();
	}

	public static void compareToString()
	{
		Random r = new Random();

		int iter = 10000;

		long t1 = System.nanoTime();
		for (int i = 0; i < iter; i++)
		{
			int num = r.nextInt(5000);

			String s = String.valueOf(num);
		}
		long t2 = System.nanoTime();
		for (int i = 0; i < iter; i++)
		{
			int num = r.nextInt(5000);

			String s = String.valueOf(num);
		}
		long t3 = System.nanoTime();
		for (int i = 0; i < iter; i++)
		{
			int num = r.nextInt(5000);
			String s = Integer.toString(num);
		}
		long t4 = System.nanoTime();
		for (int i = 0; i < iter; i++)
		{
			int num = r.nextInt(5000);
			String s = Integer.toString(num);
		}
		long t5 = System.nanoTime();

		System.out.println("t2-t1=" + (t2 - t1));
		System.out.println("t3-t2=" + (t3 - t2));
		System.out.println("t4-t3=" + (t4 - t3));
		System.out.println("t5-t4=" + (t5 - t4));
	}
	// public static void checkMemory
}
