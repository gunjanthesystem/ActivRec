package org.activity.sanityChecks;

public class TestFeb9
{
	public static void main(String[] args)
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
}
