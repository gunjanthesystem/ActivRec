package org.activity.tools;

import java.io.IOException;

import tech.tablesaw.api.Table;

public class Tablesaw
{

	public static void main(String args[])
	{

		double[] numbers = { 1, 2, 3, 4 };
		// NumberColumn nc = NumberColumn.create("Test", numbers);
		// out(nc.print());
		String fileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/GeolifeAnalysisNov2018/geolife1_NOV15ED0.75STimeAllActsFDStFilter0hrsNoTTFilterAllMUsMeanReciprocalRank.csv";
		try
		{
			Table t = Table.read().csv(fileToRead);
			System.out.println("t=" + t.shape() + "\n" + t.toString() + "\n" + t.structure());

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
