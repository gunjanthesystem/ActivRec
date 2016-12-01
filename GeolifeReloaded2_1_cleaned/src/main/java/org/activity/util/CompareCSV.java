package org.activity.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class CompareCSV
{
	
	// notes: ll threes csv files have same number of rows and columns
	public static void main(String[] args)
	{
		BufferedReader br1 = null, br2 = null, brMeta = null;
		
		int rowCount = 0;
		int columnCount = 0;
		
		String fileName1 = "/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global 550/dataRankedRecommendationWithScores.csv";
		String fileName2 = "/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global 500/dataRankedRecommendationWithScores.csv";
		String fileNameMeta = "/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global 500/meta.csv";
		
		System.out.println("File 1 is:" + fileName1);
		System.out.println("File 2 is:" + fileName2 + "\n");
		
		try
		{
			br1 = new BufferedReader(new FileReader(fileName1));
			br2 = new BufferedReader(new FileReader(fileName2));
			brMeta = new BufferedReader(new FileReader(fileNameMeta));
			
			String currentLine1, currentLine2, currentLineMeta;
			
			while ((currentLineMeta = brMeta.readLine()) != null)
			{
				rowCount++;
				
				currentLine1 = br1.readLine();
				currentLine2 = br2.readLine();
				
				if (currentLine1.equals(currentLine2))
				{
					System.out.println("User " + rowCount + " is same");
				}
				else
				{
					System.out.println("User " + rowCount + " is different");
					
					String columns1[] = currentLine1.split(Pattern.quote(","));
					String columns2[] = currentLine2.split(Pattern.quote(","));
					String columnsMeta[] = currentLineMeta.split(Pattern.quote(","));
					
					System.out.println("count colums1=" + columns1.length + " count columns2=" + columns2.length + " count meta columns="
							+ columnsMeta.length);
					for (int j = 0; j < columns2.length; j++)
					{
						if (columns1[j].equals(columns2[j]))
						{
							
						}
						else
						{
							System.out.println("\tcolumns " + (j + 1) + " is different.");// Timestamp="+columnsMeta[j]);
							System.out.println("\t\tfile 1 is:" + columns1[j]);
							System.out.println("\t\tfile 2 is:" + columns2[j]);
						}
					}
					
				}
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
				if (br2 != null)
					br2.close();
				if (brMeta != null)
					brMeta.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
	}
	
}
