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
		try
		{
			Table t = Table.read().csv(
					"/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_NOV15ED0.5STimeStFilter0hrsNoTTFilter/AllActObjs.csv");
			System.out.println("t=" + t.shape() + "\n" + t.toString() + "\n" + t.structure());

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
