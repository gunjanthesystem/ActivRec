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
}
