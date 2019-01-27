package org.activity.util;

import java.util.regex.Pattern;

/**
 * To improve regex related performances by precompiling regexes. can be used for string splits
 * 
 * @author gunjan
 *
 */
public class RegexUtils
{
	public static final Pattern patternHyphen = Pattern.compile("-");
	public static final Pattern patternPipe = Pattern.compile("|");
	public static final Pattern patternUnderScore = Pattern.compile("_");
	public static final Pattern patternDoubleUnderScore = Pattern.compile("__");
	public static final Pattern patternColon = Pattern.compile(":");
	public static final Pattern patternCaret = Pattern.compile("\\^");
	public static final Pattern patternForwardSlash = Pattern.compile("/");
	public static final Pattern patternComma = Pattern.compile(",");// Pattern.compile(",");
	public static final Pattern patternOpeningRoundBrace = Pattern.compile("\\(");
	public static final Pattern patternClosingRoundBrace = Pattern.compile("\\)");
	public static final Pattern patternGreaterThan = Pattern.compile(">");

	/**
	 * to find out how many times this method is being called when a variable in declaration calls it.
	 * 
	 * @param s
	 * @return
	 */
	public static Pattern myCompile(String s)
	{
		System.out.println("my compile called");
		return Pattern.compile(s);
	}

	public RegexUtils()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)
	{
		stringSplitPerformance();
	}

	/**
	 * The execution of this method shows that using precompile pattern to split string is relatively faster than using
	 * String.split() or compiling pattern each time with Pattern.split()
	 */
	public static void stringSplitPerformance()
	{
		String s1 = "gunjan,manali,neha";
		String[] splitted1 = null, splitted2 = null, splitted3 = null;
		long timeTaken1 = -1, timeTaken2 = -1, timeTaken3 = -1;
		int numOfIterations = 1000000;
		String delimiter = ",";
		/////////////////////////////////////////
		long begin = System.nanoTime();
		for (int i = 0; i < numOfIterations; i++)
		{
			splitted1 = s1.split(Pattern.quote(delimiter));
		}
		long end = System.nanoTime();
		timeTaken1 = end - begin;
		System.out.println(" time taken for split pattern quote = " + timeTaken1 + "ns");
		/////////////////////////////////////////
		begin = System.nanoTime();
		for (int i = 0; i < numOfIterations; i++)
		{
			Pattern doubleUnderScorePattern = Pattern.compile(delimiter);
			splitted2 = doubleUnderScorePattern.split(s1);
		}
		end = System.nanoTime();
		timeTaken2 = end - begin;
		System.out.println(" time taken for pattern compile = " + timeTaken2 + "ns");
		/////////////////////////////////////////
		begin = System.nanoTime();
		Pattern doubleUnderScorePattern = Pattern.compile(delimiter);
		for (int i = 0; i < numOfIterations; i++)
		{
			splitted3 = doubleUnderScorePattern.split(s1);
		}
		end = System.nanoTime();
		timeTaken3 = end - begin;
		System.out.println(" time taken for pattern precompiled = " + timeTaken3 + "ns");
		/////////////////////////////////////////

		double times = (timeTaken1 * 1.0 / timeTaken2);
		System.out.println("timeTaken1/timeTaken2 = " + times);

		times = (timeTaken1 * 1.0 / timeTaken3);
		System.out.println("timeTaken1/timeTaken3 = " + times);
		// System.out.println(Arrays.stream(splitted1).forEach(e -> System.out.println(e.toString())));
		/////////////////////////////////////////

		System.out.println("Splitted 1 = ");
		for (String s : splitted1)
		{
			System.out.println(s);
		}
		System.out.println("Splitted 2 = ");
		for (String s : splitted2)
		{
			System.out.println(s);
		}

		System.out.println("Splitted 3 = ");
		for (String s : splitted3)
		{
			System.out.println(s);
		}
	}

}
