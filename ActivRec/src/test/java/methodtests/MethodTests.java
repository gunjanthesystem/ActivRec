package methodtests;

import java.util.Arrays;

import org.activity.evaluation.Evaluation;
import org.activity.util.RegexUtils;

import junit.framework.Assert;

public class MethodTests
{

	public static void main(String[] args)
	{
		// regexTest2();
		regexTestForHJDistance();
	}

	public static void regexTest1()
	{
		String sampleString = "1_10/4/2014_18:39:3";
		int hour = Evaluation.getHourFromMetaString(sampleString);
		System.out.println("Hour = " + hour);
		Assert.assertEquals(hour, 18);
	}

	public static void regexTest2()
	{
		String s1 = "__a__b__c__d__e";
		String[] splitted1 = RegexUtils.patternDoubleUnderScore.split(s1);
		System.out.println("splitted1.length = " + splitted1.length);
		System.out.println("s[0] = " + splitted1[0]);
		System.out.println("s[5] = " + splitted1[5]);
	}

	public static void regexTestForHJDistance()
	{
		String s1 = "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(6-2)";

		System.out.println("-s1 = " + s1);
		String[] splitted = RegexUtils.patternUnderScore.split(s1);
		System.out.println("splitted = " + Arrays.asList(splitted).toString());

		for (int i = 1; i < splitted.length; i++)
		{
			String op = splitted[i]; // D(1-0)
			System.out.println("op = " + op);

			String[] splitOps = RegexUtils.patternOpeningRoundBrace.split(op);// $$ op.split("\\("); // D and 1-0)
			System.out.println("splitOps = " + Arrays.asList(splitOps).toString());

			// System.out.println(splitted[i]); //D(1-0)

			String operation = splitOps[0]; // D

			System.out.println("operation = " + operation);

			String splitCo[] = RegexUtils.patternHyphen.split(splitOps[1]);
			System.out.println("splitCo = " + Arrays.asList(splitCo).toString());
			// $$splitOps[1].split("-"); // 1 and 0)
			String splitCoAgain[] = RegexUtils.patternClosingRoundBrace.split(splitCo[1]);// $$splitCo[1].split("\\)");
																							// // 0
																							// and nothing
			System.out.println("splitCoAgain = " + Arrays.asList(splitCoAgain).toString());

		}
	}

}
