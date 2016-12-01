package org.activity.sanityChecks;

import org.activity.util.StringCode;

/**
 * Just to check/run some random snippets of code NOT IMPORTANT
 * 
 * @author gunjan
 *
 */
public class TestDummy2
{
	public static void main(String args[])
	{
		// String str = " 112:Music^^ || 12600__2.1891152325934935%";
		//
		// String splitted[] = str.split("\\|\\|");
		//
		// System.out.println("splitted[0] = " + splitted[0]);
		
		for (int i = 0; i <= 600; i++)
		{
			System.out.println(" i = " + i + " code = " + StringCode.getCharCodeFromActivityID(i));
		}
	}
	
}
