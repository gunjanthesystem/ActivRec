package org.activity.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CheckTies
{
	
	public static boolean checkDuplicateUsingSet(String[] input)
	{
		List inputList = Arrays.asList(input);
		Set inputSet = new HashSet(inputList);
		if (inputSet.size() < inputList.size())
			return true;
		
		return false;
	}
	
	// notes: ll threes csv files have same number of rows and columns
	public static void main(String[] args)
	{
		BufferedReader br1 = null, br2 = null, brMeta = null;
		
		int rowCount = 0;
		int columnCount = 0;
		
		String fileName1 = "/home/gunjan/MATLAB/bin/DCU data works/July20/New_14_Aug/Copy 1 aug global 1000/dataRankedRecommendationWithScores.csv";
		String fileName2 = "/home/gunjan/MATLAB/bin/DCU data works/July20/New_14_Aug/Copy 1 aug global 1000/dataActual.csv";
		// String fileNameMeta="/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global 500/meta.csv";
		//
		System.out.println("File 1 is:" + fileName1);
		System.out.println("File 2 is:" + fileName2 + "\n");
		
		try
		{
			br1 = new BufferedReader(new FileReader(fileName1));
			br2 = new BufferedReader(new FileReader(fileName2));
			// brMeta=new BufferedReader(new FileReader(fileNameMeta));
			
			String currentLine1, currentLine2;// ,currentLineMeta;
			
			while ((currentLine1 = br1.readLine()) != null)
			{
				currentLine2 = br2.readLine();
				
				rowCount++;
				
				System.out.println("\n--------------------");
				System.out.println("User " + rowCount);// is different");
				
				String columns1[] = currentLine1.split(Pattern.quote(","));
				String columns2[] = currentLine2.split(Pattern.quote(","));
				// String columnsMeta[]=currentLineMeta.split(Pattern.quote(","));
				
				// System.out.println("count colums1="+columns1.length+" count columns2="+columns2.length+" count meta columns="+columnsMeta.length);
				int tiesCellsCount = 0;
				for (int j = 0; j < columns1.length; j++)
				{
					String items[] = columns1[j].split(Pattern.quote("__"));
					
					String itemsScoreOnly[] = new String[items.length - 1];
					
					for (int k = 1; k < items.length; k++)
					{
						String splitted[] = items[k].split(Pattern.quote(":"));
						itemsScoreOnly[k - 1] = splitted[1];
					}
					
					if (checkDuplicateUsingSet(itemsScoreOnly))
					{
						tiesCellsCount++;
						System.out.println("\tRecomm with ties:" + columns1[j]);
						System.out.println("\tTarget item:" + columns2[j]);
					}
				}
				System.out.println("Number of recommendation lists with ties=" + tiesCellsCount);
				
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br1 != null)
					br1.close();
				// if (br2 != null)br2.close();
				// if (brMeta != null)brMeta.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
	}
	
}
