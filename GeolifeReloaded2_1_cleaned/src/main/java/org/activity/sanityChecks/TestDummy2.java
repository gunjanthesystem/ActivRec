package org.activity.sanityChecks;

import java.io.BufferedWriter;

import org.activity.io.WritingToFile;

/**
 * Just to check/run some random snippets of code NOT IMPORTANT
 * 
 * @author gunjan
 *
 */
public class TestDummy2
{
	public static void main(String args[])
	{
		checkWritePerformance();
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
	
}
