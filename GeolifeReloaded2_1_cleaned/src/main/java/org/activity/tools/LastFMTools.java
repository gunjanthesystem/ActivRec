package org.activity.tools;

import java.util.ArrayList;

import org.activity.util.CSVUtils;

public class LastFMTools
{
	public static void main(String arg[])
	{
		// String pathToRead =
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/last.fm dataset/lastfm-dataset/lastfm-dataset-1K/userid-timestamp-artid-artname-traid-traname.tsv";
		// // "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/last.fm dataset/lastfm-dataset/lastfm-dataset-1K/dmicro.tsv";
		//
		// String pathToWrite =
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/last.fm dataset/lastfm-dataset/Lastfm-dataset-1K-splitted/";
		
		// ReadingFromFile.splitCSVFilesByColumnValueNoHeader(pathToRead, '\t', 0, pathToWrite);
		// $$ReadingFromFile.splitCSVFilesByColumnValueNoHeaderNoApache(pathToRead, "\t", 0, pathToWrite, 19150868);// 160);// 19150868
		
		ArrayList<String> listOfOutputFileNames = new ArrayList<String>();
		String commonPathToReadResults = "/run/media/gunjan/Space/GUNJAN/LastFMSpace/June22AllNoCaseC/";// Split1AfterMergingContinuous.csv";
		String pathToWriteResults = "/run/media/gunjan/Space/GUNJAN/LastFMSpace/June22AllNoCaseC/Concatenated/";
		
		ArrayList<String> listOfOutputFileNamesSlimmer = new ArrayList<String>();
		
		for (int i = 1; i <= 992; i++) // 992
		{
			listOfOutputFileNames.add(commonPathToReadResults + "Split" + i + "AfterMergingContinuous.csv");
			listOfOutputFileNamesSlimmer.add(commonPathToReadResults + "Split" + i + "AfterMergingContinuousSlimmer.csv");
		}
		
		CSVUtils.concatenateCSVFiles(listOfOutputFileNames, true, pathToWriteResults + "AllUsersAfterMergingContinuous.csv", '\t');
		CSVUtils.concatenateCSVFiles(listOfOutputFileNamesSlimmer, true,
				pathToWriteResults + "AllUsersAfterMergingContinuousSlimmer.csv", '\t');
	}
}
