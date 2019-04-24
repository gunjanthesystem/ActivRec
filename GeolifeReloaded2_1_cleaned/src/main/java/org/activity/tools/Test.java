package org.activity.tools;

public class Test
{
	private int x;
	private int y;

	public static void main1(String[] args)
	{
		System.out.println(ObjectSizeFetcher.getObjectSize(new Test()));
	}
}