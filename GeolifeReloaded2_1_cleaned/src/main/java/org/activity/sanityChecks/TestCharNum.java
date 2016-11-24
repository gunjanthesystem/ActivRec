package org.activity.sanityChecks;

public class TestCharNum
{
	
	public static void main(String[] args)
	{
		String s = "LACACACACACACACACACACACACACACACACACACACACACACACACAJCJAJAJACACACACACAJCACACACAJAJABL";
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			System.out.println("index =" + i + "  char=" + c);
			// Process char
		}
		
	}
	
}
