package org.activity.sanityChecks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import org.activity.objects.LeakyBucket;

public class TestFeb9
{
	public static void mainFeb9(String[] args)
	{
		final double EPSILON = 1E-14;

		double l1 = 1.1;
		double l2 = 2.2;
		double l3 = 3.3;

		System.out.println(l1 + l2);

		if (l1 + l2 > l3)
			System.out.println("true");
		else
			System.out.println("false");

		if (Math.abs(l1 + l2 - l3) > EPSILON && l1 + l2 > l3)
			System.out.println("true");
		else
			System.out.println("false");
	}

	public static void main(String[] args)
	{
		String absFileNameToRead = "/home/gunjan/Documents/Books/TheRepublicByPlato.txt";
		String absFileNameToWriteTo = "/home/gunjan/Temp/TheRepublicByPlatoCopy";
		try
		{
			List<String> allLinesRead = Files.readAllLines(new File(absFileNameToRead).toPath(),
					Charset.forName("UTF-8"));

			LeakyBucket lb = new LeakyBucket(500, absFileNameToWriteTo, true);
			for (String s : allLinesRead)
			{
				lb.addToLeakyBucketWithNewline(s);
			}
			lb.flushLeakyBucket();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
