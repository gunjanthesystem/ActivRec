package org.activity.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.activity.io.WToFile;
import org.activity.objects.Pair;

/**
 * Contains an assortment of utility methods for string manipulation
 * 
 * @author gunjan
 *
 */
public class StringUtils
{

	/**
	 * ref:https://stackoverflow.com/questions/13475388/generate-fixed-length-strings-filled-with-whitespaces/32045108
	 * 
	 * @param string
	 * @param length
	 * @return
	 */
	public static String fixedLengthString(String string, int length)
	{
		return String.format("%1$" + length + "s", string);
	}

	public static void main0(String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();

		list.add("G");
		list.add("G");
		list.add("G");
		list.add("U");
		list.add("U");
		list.add("U");
		list.add("U");
		list.add("U");
		list.add("N");
		list.add("N");
		list.add("J");
		list.add("J");
		list.add("J");

		String res1 = toStringCompactWithCount(list);
		System.out.println("Res1= " + res1);
	}

	public static void main(String[] args)
	{
		analysePerformance3dArray();
	}

	public static void analysePerformance3dArray()
	{
		// stringConcatPerformance();
		// charArrayExperiments();
		long t1 = System.nanoTime();
		create3DCharArray(500 + 500, false);
		long t2 = System.nanoTime();

		System.out.println("time elapsed = " + (t2 - t1) + " ns\n\n");
		long t3 = System.nanoTime();
		create3DCharArrayOptimalSize(500, 500, false);
		long t4 = System.nanoTime();
		// System.out.println(PerformanceAnalytics.getHeapInformation());
		System.out.println("time elapsed = " + (t4 - t3) + " ns\n\n");

	}

	public static void create3DCharArray(int oneSizeForThreeDimensions, boolean verbose)
	{
		System.out.println("inside create3DCharArray");

		int capacity = oneSizeForThreeDimensions;
		char[][][] traceArray = new char[capacity][capacity][capacity];

		for (int i = 0; i < capacity; ++i)
			for (int j = 0; j < capacity; ++j)
				for (int k = 0; k < capacity; ++k)
					traceArray[i][j][k] = 'a';

		if (verbose)
		{
			StringBuilder sb = new StringBuilder("---\n");
			for (int i = 0; i < capacity; ++i)
			{
				sb.append("|");
				for (int j = 0; j < capacity; ++j)
				{
					for (int k = 0; k < capacity; ++k)
						sb.append(traceArray[i][j][k]);
					sb.append("|");
				}
				sb.append("\n");
			}
			System.out.println("sb=\n" + sb);
		}
		System.out.println(PerformanceAnalytics.getHeapInformation());
		System.out.println("exiting create3DCharArray");
	}

	public static int[][] create2DIntArray(int lengthOfWord1, int lengthOfWord2, boolean verbose)
	{
		int numOfRows = lengthOfWord1 + 1;
		int numOfCols = lengthOfWord2 + 1;

		int[][] endPointArray = new int[numOfRows][numOfCols];

		for (int i = 0; i < numOfRows; ++i)
			for (int j = 0; j < numOfCols; ++j)
				endPointArray[i][j] = -1;

		return endPointArray;
	}

	/**
	 * 
	 * @param lengthOfWord1
	 * @param lengthOfWord2
	 * @param verbose
	 * @return
	 */
	public static char[][][] create3DCharArrayOptimalSize(int lengthOfWord1, int lengthOfWord2, boolean verbose)
	{
		System.out.println("inside create3DCharArrayOptimalSize");
		int numOfRows = lengthOfWord1 + 1;
		int numOfCols = lengthOfWord2 + 1;
		int maxSize = numOfRows + numOfCols;
		System.out.println("maxSize = " + maxSize);
		char[][][] traceArray = new char[numOfRows][numOfCols][maxSize];

		for (int i = 0; i < numOfRows; ++i)
		{
			for (int j = 0; j < numOfCols; ++j)
			{
				for (int k = 0; k < i + j; k++)
				{
					// System.out.print("i=" + i + ",j=" + j + ",k=" + k);
					traceArray[i][j][k] = 'x';
				}
			}
		}
		if (verbose)
		{
			StringBuilder sb = new StringBuilder("---\n");
			for (int i = 0; i < numOfRows; ++i)
			{
				sb.append("|");
				for (int j = 0; j < numOfCols; ++j)
				{
					for (int k = 0; k < i + j; ++k)
					{
						sb.append(traceArray[i][j][k]);
					}
					sb.append("|");
				}
				sb.append("\n");
			}
			System.out.println("sb=\n" + sb);
		}
		System.out.println(PerformanceAnalytics.getHeapInformation());
		System.out.println("exiting create3DCharArrayOptimalSize");
		return traceArray;
	}

	public static void charArrayExperiments()
	{
		int capacity = 10;

		char[][][] traceArray = new char[capacity][capacity][capacity];

		for (int i = 0; i < capacity; i++)
		{
			for (int j = 0; j < capacity; j++)
			{
				for (int k = 0; k < capacity; k++)
				{
					traceArray[i][j][k] = 'a';
				}
			}
		}

		StringBuilder sb = new StringBuilder("---\n");
		for (int i = 0; i < capacity; i++)
		{
			// sb.append("i=").append(i).append("|");
			sb.append("|");
			for (int j = 0; j < capacity; j++)
			{
				// System.out.print("i=" + i + ",j=" + j + "\t\t");
				for (int k = 0; k < capacity; k++)
				{
					sb.append(traceArray[i][j][k]);
				}
				sb.append("|");
			}
			sb.append("\n");
		}
		System.out.println("sb=\n" + sb.toString());
		// Arrays.asList(traceArray).stream().forEach(System.out::println);

		// for (int i = 0; i < traceArray.length; i++)
		// {
		//
		// for (int j = 0; j < traceArray[0].length; j++)
		// {
		// System.out.print("i=" + i + ",j=" + j + "\t\t");
		// for (int k = 0; k < traceArray[0][0].length; k++)
		// {
		// System.out.print("_k=" + k);
		// }
		// System.out.println();
		// }
		// }
	}

	public static void stringConcatPerformance()
	{
		ArrayList<String> list0 = new ArrayList<String>();
		ArrayList<String> testList = new ArrayList<String>();

		// list.add("G");
		// list.add("G");
		// list.add("G");
		// list.add("U");
		// list.add("U");
		// list.add("U");
		// list.add("U");
		// list.add("U");
		// list.add("N");
		// list.add("N");
		// list.add("J");
		// list.add("J");
		// list.add("J");

		for (int i = 0; i < 1000; i++)
		{
			list0.add("wonderfuli");// "wonderful ");
		}

		String longString = fCat(list0.toArray(new String[list0.size()]));
		System.out.println("longString.length()=" + longString.length());

		System.out.println("\n1: getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());

		for (int i = 0; i < 10000; i++)
		{
			testList.add(longString);// "wonderfuli");// "wonderful ");
		}

		// System.out.println("\n2: getHeapInfo\n" + PerformanceAnalytics.getHeapPercentageFree());

		Pair<String, Long> res1 = PerformanceAnalytics.timeThisFunctionInns(e -> fastStringConcat(e), testList,
				"fastStringConcat(ArrayList<String> arrayOfStrings)\n");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

		// System.out.println("\n3: getHeapInfo\n" + PerformanceAnalytics.getHeapPercentageFree());

		res1 = PerformanceAnalytics.timeThisFunctionInns(e -> fastStringConcatIter(e), testList,
				"fastStringConcatIter(ArrayList<String> arrayOfStrings)\n");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

		System.out.println("\n4: getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());

		res1 = PerformanceAnalytics.timeThisFunctionInns(e -> fCat(e), testList.toArray(new String[testList.size()]),
				"fCat(String... stringsToConcat)\n");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

		System.out.println("\n4: getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());

		StringBuilder sb = new StringBuilder();

		Pair<StringBuilder, Long> res2 = PerformanceAnalytics.timeThisFunctionInns((e, f) -> fastStringConcat(e, f), sb,
				testList.toArray(new String[testList.size()]),
				"fastStringConcat(StringBuilder res, String... stringsToConcat)\n");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

		System.out.println("\n4: getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());

		res2 = PerformanceAnalytics.timeThisFunctionInns((e, f) -> fCatLeaner1(e, f), sb,
				testList.toArray(new String[testList.size()]),
				"fCatLeaner1(StringBuilder givenSB, String... stringsToConcat)\n");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

		System.out.println("\n3: getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());

		res2 = PerformanceAnalytics.timeThisFunctionInns((e, f) -> fCat(e, f), sb,
				testList.toArray(new String[testList.size()]),
				"fCatLeaner2(StringBuilder givenSB, String... stringsToConcat)\n");
		System.out.println("timetaken = " + res1.getSecond());
		System.out.println("res1.length = " + res1.getFirst().length());

		System.out.println("\n3: getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());

		res1 = PerformanceAnalytics.timeThisFunctionInns(e -> fastStringConcatFor(e),
				testList.toArray(new String[testList.size()]), "fastStringConcatFor(String... stringsToConcat)\n");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

		// System.out.println("\n3: getHeapInfo\n" + PerformanceAnalytics.getHeapPercentageFree());

		// res1 = PerformanceAnalytics.timeThisFunctionInns(e -> fastStringConcat(e), "fastStringConcatArray var args",
		// "G", "G", "G", "U", "U", "U", "U", "U", "N", "N", "J", "J", "J");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

		res1 = PerformanceAnalytics.timeThisFunctionInns(e -> writeToFile(e), testList, "writeToFile");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

		// System.out.println("\n3: getHeapInfo\n" + PerformanceAnalytics.getHeapPercentageFree());

		// res1 = PerformanceAnalytics.timeThisFunctionInns(e -> stringConcat2(e), list, "stringConcat2");
		// System.out.println("timetaken = " + res1.getSecond());
		// System.out.println("res1.length = " + res1.getFirst().length());

	}

	public static String fastStringConcat(ArrayList<String> arrayOfStrings)
	{
		StringBuilder res = new StringBuilder();
		arrayOfStrings.stream().forEach(s -> res.append(s));
		return res.toString();
	}

	public static String fastStringConcatIter(ArrayList<String> arrayOfStrings)
	{
		StringBuilder res = new StringBuilder();
		for (String s : arrayOfStrings)
		{
			res.append(s);
		}
		return res.toString();
	}

	/**
	 * <font color = green>Very Fast string concatenation found.</font>
	 * <p>
	 * previously names as fastStringConcat
	 * 
	 * @param stringsToConcat
	 * @return
	 */
	public static String fCat(String... stringsToConcat)
	{
		StringBuilder res = new StringBuilder();
		for (String s : stringsToConcat)
		{
			res.append(s);
		}
		System.out.println(
				"\nInside fCat(String... stringsToConcat): getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());

		return res.toString();
	}

	/**
	 * Appends given strings to the given StringBuilder
	 * <p>
	 * <font color = green>Very Fast string concatenation found.</font>
	 * <p>
	 * previously names as fastStringConcat
	 * <p>
	 * fCatV1
	 * 
	 * @param res
	 * @param stringsToConcat
	 * @return res appended with stringsToConcat
	 */
	public static StringBuilder fastStringConcat(StringBuilder res, String... stringsToConcat)
	{
		for (String s : stringsToConcat)
		{
			res.append(s);
		}

		System.out.println("\nInside fCat(StringBuilder res, String... stringsToConcat): getHeapInfo\n"
				+ PerformanceAnalytics.getHeapInformation());
		return res;
	}
	//
	// /**
	// * Appends given strings to the given StringBuilder with newline.
	// * <p>
	// * <font color = green>Very Fast string concatenation found.</font>
	// * <p>
	// * previously names as fastStringConcat
	// *
	// * @param res
	// * @param stringsToConcat
	// * @return res appended with stringsToConcat
	// */
	// public static StringBuilder fCatln(StringBuilder res, String... stringsToConcat)
	// {
	// for (String s : stringsToConcat)
	// {
	// res.append(s);
	// }
	// res.append("\n");
	// return res;
	// }

	/**
	 * Appends given strings to the given StringBuilder
	 * <p>
	 * <font color = green>Very Fast string concatenation found.</font>
	 * <p>
	 * previously names as fastStringConcat
	 * 
	 * @param res
	 * @param stringsToConcat
	 * @return res appended with stringsToConcat
	 */
	public static StringBuilder fCatLeaner1(StringBuilder givenSB, String... stringsToConcat)
	{
		int expectedSize = givenSB.length();
		expectedSize += Arrays.stream(stringsToConcat).parallel().mapToLong(String::length).sum();

		StringBuilder result = new StringBuilder(expectedSize + 100);
		result.append(givenSB);
		givenSB = null;
		for (String s : stringsToConcat)
		{
			result.append(s);
			// res.append(s);
		}
		System.out.println("\nInsidefCatLeaner1: getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());
		return result;
	}

	/**
	 * Fastest for long string until 30 Mar 2017 but takes larger heap space.
	 * <p>
	 * previously called: fCatLeaner2
	 * 
	 * @param givenSB
	 * @param stringsToConcat
	 * @return
	 */
	public static StringBuilder fCat(StringBuilder givenSB, String... stringsToConcat)
	{
		int expectedSize = givenSB.length();
		for (String s : stringsToConcat)
		{
			expectedSize += s.length();
		}

		StringBuilder result = new StringBuilder(expectedSize + 100);

		result.append(givenSB);
		// givenSB = null; Disable on 11 Jan 2018

		for (String s : stringsToConcat)
		{
			result.append(s);
		}
		// System.out.println("\nInsidefCatLeaner2: getHeapInfo\n" + PerformanceAnalytics.getHeapInformation());

		return result;
	}

	public static String fastStringConcatFor(String... stringsToConcat)
	{
		StringBuilder res = new StringBuilder();

		for (int i = 0; i < stringsToConcat.length; i++)
		{
			res.append(stringsToConcat[i]);
		}
		// for (String s : stringsToConcat)
		// {
		// res.append(s);
		// }
		return res.toString();
	}

	// public static String fastStringConcatFor2(String... stringsToConcat)
	// {
	// StringBuilder res = new StringBuilder();
	//
	// return fcat(res, stringsToConcat).toString();
	// // for (int i = 0; i < stringsToConcat.length; i++)
	// // {
	// // res.append(stringsToConcat[i]);
	// // }
	// // // for (String s : stringsToConcat)
	// // // {
	// // // res.append(s);
	// // // }
	// // return res.toString();
	// }

	public static String stringConcat1(ArrayList<String> arrayOfStrings)
	{
		String res = new String();
		arrayOfStrings.stream().forEach(s -> res.concat(s));
		System.out.println("\tres.length = " + res.length());
		return res;
	}

	public static String stringConcat2(ArrayList<String> arrayOfStrings)
	{
		String res = new String();
		for (String s : arrayOfStrings)
		{
			res += s;
		}
		return res;
	}

	public static String writeToFile(ArrayList<String> arrayOfStrings)
	{
		try
		{
			BufferedWriter bw = WToFile.getBWForNewFile("dummyTest.csv");
			for (String s : arrayOfStrings)
			{
				bw.write(s);
			}
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return "dummy";
	}

	/**
	 * // For e.g A, A, A, A, A, B, B ,B // stored are A:5, B:3 // GGGUUUUUNNJJJ to G:3,U:5,N:2,J:3
	 * 
	 * @param list
	 * @return
	 */
	public static String toStringCompactWithCount(ArrayList<String> list, String delimiter)
	{
		String res = "", currentString = "", previousString = "";
		int countForpreviousString = 0;

		// if (list.size() == 0)
		// {
		// return "--";
		// }
		// list.forEach(x -> System.out.print(x));
		// System.out.println("\n");
		for (int i = 0; i < list.size(); i++)
		{
			currentString = list.get(i);
			// System.out.println("\n\ni = " + i);
			// System.out.println("currentString = " + currentString);
			// System.out.println("previousString = " + previousString);
			// System.out.println("countForpreviousString = " + countForpreviousString);

			if (previousString.length() == 0) // if first string, initialise previous string and count =1
			{
				// System.out.println("flag0");
				// previousString = currentString; //already done outside the if else
				countForpreviousString = 1;
			}
			else if (currentString.equals(previousString))
			{
				// System.out.println("flag1");
				countForpreviousString += 1;
			}
			else
			{
				// System.out.println("flag2");
				res += previousString + ":" + countForpreviousString + delimiter;
				countForpreviousString = 1;

			}
			previousString = currentString;

			// System.out.println("res = " + res);
		}
		res += previousString + ":" + countForpreviousString + delimiter;

		res = res.substring(0, res.length() - 1); // removing the last stray comma
		return res;
	}

	/**
	 * // For e.g A, A, A, A, A, B, B ,B // stored are A:5, B:3 // GGGUUUUUNNJJJ to G:3,U:5,N:2,J:3
	 * 
	 * @param list
	 * @return
	 */
	public static String toStringCompactWithCount(ArrayList<String> list)
	{
		String res = "", currentString = "", previousString = "";
		int countForpreviousString = 0;

		// if (list.size() == 0)
		// {
		// return "--";
		// }
		// list.forEach(x -> System.out.print(x));
		// System.out.println("\n");
		for (int i = 0; i < list.size(); i++)
		{
			currentString = list.get(i);
			// System.out.println("\n\ni = " + i);
			// System.out.println("currentString = " + currentString);
			// System.out.println("previousString = " + previousString);
			// System.out.println("countForpreviousString = " + countForpreviousString);

			if (previousString.length() == 0) // if first string, initialise previous string and count =1
			{
				// System.out.println("flag0");
				// previousString = currentString; //already done outside the if else
				countForpreviousString = 1;
			}
			else if (currentString.equals(previousString))
			{
				// System.out.println("flag1");
				countForpreviousString += 1;
			}
			else
			{
				// System.out.println("flag2");
				res += previousString + ":" + countForpreviousString + ",";
				countForpreviousString = 1;

			}
			previousString = currentString;

			// System.out.println("res = " + res);
		}
		res += previousString + ":" + countForpreviousString + ",";

		res = res.substring(0, res.length() - 1); // removing the last stray comma
		return res;
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	public static String toStringCompactWithoutCount(ArrayList<String> list)
	{
		String res = "", currentString = "", previousString = "";
		int countForpreviousString = 0;

		// if (list.size() == 0)
		// {
		// return "--";
		// }
		// list.forEach(x -> System.out.print(x));
		// System.out.println("\n");
		for (int i = 0; i < list.size(); i++)
		{
			currentString = list.get(i);
			// System.out.println("\n\ni = " + i);
			// System.out.println("currentString = " + currentString);
			// System.out.println("previousString = " + previousString);
			// System.out.println("countForpreviousString = " + countForpreviousString);

			if (previousString.length() == 0) // if first string, initialise previous string and count =1
			{
				// System.out.println("flag0");
				// previousString = currentString; //already done outside the if else
				countForpreviousString = 1;
			}
			else if (currentString.equals(previousString))
			{
				// System.out.println("flag1");
				countForpreviousString += 1;
			}
			else
			{
				// System.out.println("flag2");
				res += previousString + /* ":" + countForpreviousString + */",";
				countForpreviousString = 1;

			}
			previousString = currentString;

			// System.out.println("res = " + res);
		}
		res += previousString + /* ":" + countForpreviousString + */",";

		res = res.substring(0, res.length() - 1); // removing the last stray comma
		return res;
	}

	/**
	 * 
	 * @param list
	 * @param delimiter
	 * @return
	 */
	public static String toStringCompactWithoutCount(ArrayList<String> list, String delimiter)
	{
		String res = "", currentString = "", previousString = "";
		int countForpreviousString = 0;

		// if (list.size() == 0)
		// {
		// return "--";
		// }
		// list.forEach(x -> System.out.print(x));
		// System.out.println("\n");
		for (int i = 0; i < list.size(); i++)
		{
			currentString = list.get(i);
			// System.out.println("\n\ni = " + i);
			// System.out.println("currentString = " + currentString);
			// System.out.println("previousString = " + previousString);
			// System.out.println("countForpreviousString = " + countForpreviousString);

			if (previousString.length() == 0) // if first string, initialise previous string and count =1
			{
				// System.out.println("flag0");
				// previousString = currentString; //already done outside the if else
				countForpreviousString = 1;
			}
			else if (currentString.equals(previousString))
			{
				// System.out.println("flag1");
				countForpreviousString += 1;
			}
			else
			{
				// System.out.println("flag2");
				res += previousString + /* ":" + countForpreviousString + */delimiter;
				countForpreviousString = 1;

			}
			previousString = currentString;

			// System.out.println("res = " + res);
		}
		res += previousString + /* ":" + countForpreviousString + */delimiter;

		res = res.substring(0, res.length() - 1); // removing the last stray comma
		return res;
	}

	/**
	 * 
	 * @param a
	 * @param delim
	 * @return
	 */
	public static String getArrayAsStringDelimited(int a[], String delim)
	{
		String s = new String();
		for (int d : a)
		{
			s += d + delim;
		}
		return s;
	}

	/**
	 * 
	 * @param subStr
	 * @param str
	 * @return
	 */
	public static int countSubstring(String subStr, String str)
	{
		return (str.length() - str.replace(subStr, "").length()) / subStr.length();
	}

	/**
	 * 
	 * @param s
	 * @param delimiter
	 * @return
	 * @since 16 July 2018
	 */
	public static <T> String collectionToString(Collection<T> s, String delimiter)
	{
		return s.stream().map(v -> String.valueOf(v)).collect(Collectors.joining(delimiter));
	}

	/**
	 * 
	 * @param s
	 * @param delimiter
	 * @return
	 */
	public static ArrayList<String> splitAsStringList(String s, Pattern delimiter)
	{
		ArrayList<String> res = new ArrayList();
		List<String> resTemp = Arrays.asList(delimiter.split(s));
		res.addAll(resTemp);
		return res;
	}

	/**
	 * 
	 * @param s
	 * @param delimiter
	 * @return
	 */
	public static ArrayList<Integer> splitAsIntegerList(String s, Pattern delimiter)
	{
		List<String> resTemp = Arrays.asList(delimiter.split(s));
		ArrayList<Integer> res = (ArrayList<Integer>) resTemp.stream().map(i -> Integer.valueOf(i))
				.collect(Collectors.toList());
		return res;
	}
}
