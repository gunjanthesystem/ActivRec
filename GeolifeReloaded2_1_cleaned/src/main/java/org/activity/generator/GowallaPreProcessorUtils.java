package org.activity.generator;

import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;

public class GowallaPreProcessorUtils
{

	public GowallaPreProcessorUtils()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)
	{
		// Split files needed for fetching timezone for lats, longs in the datase:
		// include all locs in raw spots subset 1
		CSVUtils.splitCSVRowise("/home/gunjan/JupyterWorkspace/data/gowalla_spots_subset1_fromRaw28Feb2018.csv", ",",
				true, 10, "/home/gunjan/JupyterWorkspace/data/", "gowalla_spots_subset1_fromRaw28Feb2018smallerFile");

		long numOfLines = ReadingFromFile
				.getNumOfLines("/home/gunjan/JupyterWorkspace/data/gowalla_spots_subset1_fromRaw28Feb2018.csv");

		System.out.println("num of lines = " + numOfLines);

	}

}
