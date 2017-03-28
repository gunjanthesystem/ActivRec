package org.activity.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;

import org.activity.io.WritingToFile;

public class FileFormatter
{

	public static void main(String[] args)
	{
		String fileToRead = "/run/media/gunjan/Space/GUNJAN/LastFMSpace/June22AllNoCaseC/Concatenated/AllUsersAfterMergingContinuousSlimmerBackup.csv";
		// "/run/media/gunjan/Space/GUNJAN/LastFMSpace/June22AllNoCaseC/Concatenated_test2/AllUsersAfterMergingContinuousSlimmer.csv";
		String fileToWrite = "/run/media/gunjan/Space/GUNJAN/LastFMSpace/June22AllNoCaseC/Concatenated/AllUsersAfterMergingContinuousSlimmerFormatted.csv";
		// "/run/media/gunjan/Space/GUNJAN/LastFMSpace/June22AllNoCaseC/Concatenated_test2/Formatted1.csv";
		removeTrailingWhiteSpaces(fileToRead, fileToWrite);
	}

	public static void removeTrailingWhiteSpaces(String absFileNameToRead, String absFileNameToWrite)
	{
		StringBuffer sb = new StringBuffer();

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(absFileNameToRead));// Constant.getCommonPath() +
			BufferedWriter bw = WritingToFile.getBWForNewFile(absFileNameToWrite);
			String lineRead = "";
			int countOfLines = 0;
			while ((lineRead = br.readLine()) != null)
			{
				countOfLines++;
				sb.append(lineRead.trim() + "\n");

				if (countOfLines % 5000 == 0)
				{
					bw.write(sb.toString());
					sb.setLength(0);
				}
			}

			bw.write(sb.toString());
			sb.setLength(0);

			bw.close();
			br.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
