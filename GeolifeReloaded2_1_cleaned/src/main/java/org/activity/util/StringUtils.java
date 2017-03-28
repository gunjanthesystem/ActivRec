package org.activity.util;

import java.util.ArrayList;

/**
 * Contains an assortment of utility methods for string manipulation
 * 
 * @author gunjan
 *
 */
public class StringUtils
{

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
		for (int i = 0; i < 1000000; i++)
		{
			list.add("wonderful ");
		}

		{
			long t1 = System.nanoTime();
			String res1 = fastStringConcat(list);
			long t2 = System.nanoTime();
			System.out.println("timetaken = " + (t2 - t1) + " ns");
			System.out.println("res1.length = " + res1.length());
		}

		{
			long t1 = System.nanoTime();
			String res1 = stringConcat1(list);
			long t2 = System.nanoTime();
			System.out.println("timetaken = " + (t2 - t1) + " ns");
			System.out.println("res1.length = " + res1.length());
		}

		{
			long t1 = System.nanoTime();
			String res1 = stringConcat2(list);
			long t2 = System.nanoTime();
			System.out.println("timetaken = " + (t2 - t1) + " ns");
			System.out.println("res1.length = " + res1.length());
		}
	}

	public static String fastStringConcat(ArrayList<String> arrayOfStrings)
	{
		StringBuilder res = new StringBuilder();
		arrayOfStrings.stream().forEach(s -> res.append(s));
		return res.toString();
	}

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
}
