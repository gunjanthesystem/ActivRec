package org.activity.sanityChecks;

import java.util.ArrayList;
import java.util.Map;

public class TestDummy1
{
	
	// @BindMap(valueSerializer = IntArraySerializer.class, keySerializer = StringSerializer.class, valueClass = int[].class,
	// keyClass = String.class, keysCanBeNull = false)
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
	
	public static void main(String args[])
	{
		// String rawPathToRead =
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/last.fm dataset/lastfm-dataset/lastfm-dataset-1K/";
		// String pathToParse = rawPathToRead + "userid-timestamp-artid-artname-traid-traname.tsv";
		// ReadingFromFile.getNumOfLines(pathToParse);
		// TestDummy1 td = new TestDummy1();
		
		ArrayList<Integer> arr = new ArrayList<Integer>();
		arr.add(10);
		arr.add(20);
		arr.add(30);
		arr.add(42);
		
		Double avg = arr.stream().mapToDouble(a -> a).average().getAsDouble();
		System.out.println("Avg =" + avg);
		
		// td.fun1();
	}
}
