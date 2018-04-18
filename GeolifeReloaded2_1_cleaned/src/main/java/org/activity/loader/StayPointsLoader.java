package org.activity.loader;

import java.io.PrintStream;

import org.activity.io.WToFile;

public class StayPointsLoader
{
	private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost:3306/";// "jdbc:mysql://csserver.ucd.ie/";//

	private static final String USER = "root";// "gkumar";//"root";
	private static final String PASS = "root";// "V12xt!07";//"root";

	private static String databaseName;
	static String commonPath;

	public static void main(String[] args)
	{
		try
		{
			databaseName = "StayPoints";
			commonPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/TrajectorySpace/Jun13/";
			PrintStream loadLogStream = WToFile.redirectConsoleOutput(commonPath + "LoadingStayPointsLog.txt");

		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
