package org.activity.sanityChecks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class has method to check program correctness
 * 
 * @author gunjan
 *
 */
public class RowDiffs
{
	public static void main(String[] args)
	{
		String fileName1 = "/run/media/gunjan/HOME/gunjan/DCU Data Works Oct Space/Oct 24 Double faster fuzzy 4/EditSimilarityCalculations.csv";
		String fileName2 = "/run/media/gunjan/HOME/gunjan/DCU Data Works Oct Space/Oct 24 Double faster ver 4/EditSimilarityCalculations.csv";
		findDiffOfRows(fileName1, fileName2);
	}

	public static void findDiffOfRows(String fileName1, String fileName2)
	{
		BufferedReader br1 = null, br2 = null;
		try
		{
			ArrayList<String> array1 = new ArrayList<String>();
			ArrayList<String> array2 = new ArrayList<String>();

			br1 = new BufferedReader(new FileReader(fileName1));
			br2 = new BufferedReader(new FileReader(fileName2));

			String s1, s2;
			while ((s1 = br1.readLine()) != null)
			{
				array1.add(s1);
			}
			while ((s2 = br2.readLine()) != null)
			{
				array2.add(s2);
			}
			System.out.println("populated");

			if (array1.size() == array2.size())
			{
				System.out.println("both files have same  number of lines");
			}

			// search
			int in1ButNotIn2 = 0;

			System.out.println("searching " + fileName1 + "  in " + fileName2 + "\n Not found");
			for (String search1 : array1)
			{
				// System.out.print("1\n");

				if (!array2.contains(search1))
				{
					// System.out.println(search1);
					in1ButNotIn2++;
				}
			}
			System.out.println("searchin 1 completed");

			int in2ButNotIn1 = 0;
			System.out.println("searching " + fileName2 + "  in " + fileName1 + "\n Not found");

			for (String search2 : array2)
			{
				// System.out.print("2\n");

				if (!array1.contains(search2))
				{
					// System.out.println(search2);
					in2ButNotIn1++;
				}
			}
			System.out.println("searchin 2 completed");
			System.out.println("in1ButNotIn2 = " + in1ButNotIn2 + "    and in2ButNotIn1 = " + in2ButNotIn1);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br1 != null) br1.close();
				if (br2 != null) br2.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
