package org.activity.util;

public class IntegerUtils
{

	/**
	 * @author gunjan
	 * @param n
	 * @return
	 */
	public static char[] intToCharArray(int n)
	{
		int numOfDigits = IntegerUtils.getNumOfDigits4(n);
		char[] cArr = new char[numOfDigits];

		if (numOfDigits == 1)
		{// 5
			cArr[0] = (char) (n + '0');
		}
		else // atleast two digits
		{// 52
			int index = numOfDigits - 1;// 1
			while (n > 0)// 52>0, 5>0
			{
				cArr[index--] = (char) ((n % 10) + '0'); // cArr[1]=2, cArr[0]=5
				n /= 10;// n=5
			}
			// do{ a = n / 10; cArr[i++] = (char) (a + '0'); n = n % 10; } while (a > 0);
		}
		return cArr;
	}

	public static void main(String args[])
	{
		intToCharArrPerformanceTest(490, 100000000);
	}

	public static void intToCharArrPerformanceTest(int n, int iterations)
	{
		// int n = 440560000;
		char[] res1 = null;
		String res2 = null;
		System.out.println("num of digit in " + n + " = " + getNumOfDigits4(n));

		long t1 = System.nanoTime();
		for (int i = 0; i < iterations; i++)
		{
			res1 = intToCharArray(n);
		}
		long t2 = System.nanoTime();
		System.out.println("res1=" + String.valueOf(res1) + " time taken= " + (t2 - t1) / 1000000.0 + " ms");

		long t3 = System.nanoTime();
		for (int i = 0; i < iterations; i++)
		{
			res2 = Integer.toString(n);
		}
		long t4 = System.nanoTime();
		System.out.println("res2=" + res2 + " time taken= " + (t4 - t3) / 1000000.0 + " ms");

	}

	public static int getNumOfDigits1(int n)
	{
		return Integer.toString(n).length();
	}

	public static int getNumOfDigits2(int n)
	{
		if (n == 0) return 1;
		return (int) (Math.log10(n) + 1);
	}

	public static int getNumOfDigits3(int n)
	{
		if (n == 0) return 1;
		int l;
		for (l = 0; n > 0; ++l)
			n /= 10;
		return l;
	}

	/**
	 * Returns the num of digit in an int
	 * <p>
	 * ref: http://stackoverflow.com/questions/1306727/way-to-get-number-of-digits-in-an-int note: works for only until
	 * 10 digit after which it is out of int range
	 * 
	 * @param n
	 * @return
	 */
	public static int getNumOfDigits4(int n)
	{
		if (n < 100000)
		{
			// 5 or less
			if (n < 100)
			{
				// 1 or 2
				if (n < 10)
					return 1;
				else
					return 2;
			}
			else
			{
				// 3 or 4 or 5
				if (n < 1000)
					return 3;
				else
				{
					// 4 or 5
					if (n < 10000)
						return 4;
					else
						return 5;
				}
			}
		}
		else
		{
			// 6 or more
			if (n < 10000000)
			{
				// 6 or 7
				if (n < 1000000)
					return 6;
				else
					return 7;
			}
			else
			{
				// 8 to 10
				if (n < 100000000)
					return 8;
				else
				{
					// 9 or 10
					if (n < 1000000000)
						return 9;
					else
						return 10;
				}
			}
		}
	}

}
