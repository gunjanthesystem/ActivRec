package org.activity.sanityChecks;

import org.activity.ui.PopUps;

public class Sanity
{

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean eq(int a, int b, String errorMsgConsole)
	{
		if (a == b)
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return true if equal
	 */
	public static boolean eq(double a, double b, String errorMsgConsole)
	{
		if (a == b)
		{
			return true;
		}
		else
		{
			errorMsgConsole += "a = " + a + "!= b=" + b;
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return true if equal
	 */
	public static boolean eq(double a, double b, double c, String errorMsgConsole)
	{
		boolean v1 = eq(a, b, errorMsgConsole);
		boolean v2 = eq(a, c, errorMsgConsole);
		boolean v3 = eq(b, c, errorMsgConsole);

		if (v1 && v2 && v3)
		{
			return true;
		}
		else
		{
			// System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			PopUps.printTracedErrorMsg(errorMsgConsole);
			return false;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean leq(int a, int b, String errorMsgConsole)
	{
		if (a <= b)
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean gt(int a, int b, String errorMsgConsole)
	{
		if (a > b)
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * If unequal, print and display error message
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean eq(String a, String b, String errorMsgConsole)
	{
		if (a.equals(b))
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * If unequal, print and display error message
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean eq(boolean a, boolean b, String errorMsgConsole)
	{
		if (a == b)
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}
}
