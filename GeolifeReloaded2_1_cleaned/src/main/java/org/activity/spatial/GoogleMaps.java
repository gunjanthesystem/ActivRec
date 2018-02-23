package org.activity.spatial;

import org.activity.io.ReadingFromFile;

public class GoogleMaps
{

	public GoogleMaps()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[])
	{
		System.out.println(getAPIKey());
	}

	private static String getAPIKey()
	{
		return (ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/spamg.kry", ",", 0, false)).get(0);
	}
}
