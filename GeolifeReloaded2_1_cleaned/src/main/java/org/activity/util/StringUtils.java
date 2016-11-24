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
		
		String res1 = toStringCompactWithCount(list);
		System.out.println("Res1= " + res1);
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
}
