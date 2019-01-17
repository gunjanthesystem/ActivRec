package org.activity.ui;

import java.awt.HeadlessException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.activity.constants.Constant;
import org.activity.io.WToFile;

/**
 * 
 * @author gunjan
 *
 */
public class PopUps
{
	/**
	 * 
	 * @param msg
	 */
	public static void showMessage(String msg)
	{
		try
		{
			JFrame frame = new JFrame();
			// frame.setSize(200, 150);
			// frame.getContentPane( ).setLayout(new BorderLayout( ));
			JOptionPane.showMessageDialog(frame, msg);
			// frame.getContentPane( ).add(p, BorderLayout.SOUTH);
			// frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		catch (HeadlessException e)
		{
			System.out.println("\n Headless: hence printing msg instead of PopUp.\n" + msg);
			WToFile.appendLineToFileAbs(msg, Constant.getCommonPath() + Constant.messageFileName);
		}
	}

	/**
	 * Displays a popup pane with error message.
	 * 
	 * @param msg
	 */
	public static void showError(String msg)
	{
		try
		{
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame, msg, "Error Encountered", JOptionPane.ERROR_MESSAGE);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			System.err.println("\nError Encountered\n" + msg);
			WToFile.appendLineToFileAbs(msg, Constant.getCommonPath() + Constant.errorFileName);
		}
		catch (HeadlessException e)
		{
			msg = "\nError: Headless: hence printing error msg instead of PopUp.\n" + msg;
			System.err.println(msg);
			WToFile.appendLineToFileAbs(msg, Constant.getCommonPath() + Constant.errorFileName);
		}
	}

	/**
	 * Displays a popup pane with exception message
	 * 
	 * @param e
	 *            exception
	 * @param methodName
	 */
	public static void showException(Exception e, String methodName)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionMsg = sw.toString(); // stack trace as a string
		exceptionMsg += ("\n Exception in " + methodName + "\n");
		try
		{
			// String exceptionMsg =ExceptionUtils.getStackTrace(throwable)// e.getMessage();
			PopUps.showError(exceptionMsg);
		}
		catch (HeadlessException ex)
		{

			exceptionMsg = "\n Headless: hence printing exeception msg instead of PopUp.\n" + exceptionMsg;
			System.err.println(exceptionMsg);
			WToFile.appendLineToFileAbs(exceptionMsg, Constant.getCommonPath() + Constant.errorFileName);
		}
	}

	/**
	 * 
	 * @param errorMsg
	 */
	public static void printTracedErrorMsgWithExit(String errorMsg)
	{
		// StringBuilder sb = new StringBuilder("\nError:" + errorMsg + "\n" + "--------- current stack -------\n");
		// Arrays.stream(Thread.currentThread().getStackTrace()).forEach(e -> sb.append(e.toString() + "\n"));
		// sb.append("timestamp:" + new Timestamp(System.currentTimeMillis()));
		System.err.println(getTracedErrorMsg(errorMsg) + "------- ----- EXITWITH WITH NON ZERO STATUS----- -----");
		WToFile.appendLineToFileAbs(errorMsg, Constant.getCommonPath() + Constant.errorFileName);
		System.exit(-1);
		// return sb.append("--------- ------- ----- -------").toString();
	}

	/**
	 * 
	 * @param errorMsg
	 */
	public static void printTracedErrorMsg(String errorMsg)
	{
		System.err.println(getTracedErrorMsg(errorMsg) + "\n------- ---------- -----");
		// StringBuilder sb = new StringBuilder("\nError:" + errorMsg + "\n" + "--------- current stack -------\n");
		// Arrays.stream(Thread.currentThread().getStackTrace()).forEach(e -> sb.append(e.toString() + "\n"));
		// System.err.println(sb.toString() + "------------");
		// return sb.append("--------- ------- ----- -------").toString();
		WToFile.appendLineToFileAbs(getTracedErrorMsg(errorMsg) + "--\n",
				Constant.getCommonPath() + Constant.errorFileName);
	}

	// /**
	// *
	// * @param errorMsg
	// */
	// public static void printTracedErrorMsgAndWriteToFile(String errorMsg, String errorFileName)
	// {
	// System.err.println(getTracedErrorMsg(errorMsg) + "\n------- ---------- -----");
	// WritingToFile.appendLineToFileAbsolute(errorMsg, errorFileName);
	// // StringBuilder sb = new StringBuilder("\nError:" + errorMsg + "\n" + "--------- current stack -------\n");
	// // Arrays.stream(Thread.currentThread().getStackTrace()).forEach(e -> sb.append(e.toString() + "\n"));
	// // System.err.println(sb.toString() + "------------");
	// // return sb.append("--------- ------- ----- -------").toString();
	// }

	/**
	 * 
	 * @param errorMsg
	 */
	public static void printTracedWarningMsg(String errorMsg)
	{
		System.err.println(getCurrentStackTracedWarningMsg(errorMsg) + "\n------- ---------- -----");
		// StringBuilder sb = new StringBuilder("\nError:" + errorMsg + "\n" + "--------- current stack -------\n");
		// Arrays.stream(Thread.currentThread().getStackTrace()).forEach(e -> sb.append(e.toString() + "\n"));
		// System.err.println(sb.toString() + "------------");
		// return sb.append("--------- ------- ----- -------").toString();
		WToFile.appendLineToFileAbs(errorMsg, Constant.getCommonPath() + Constant.warningFileName);
	}

	/**
	 * 
	 * @param errorMsg
	 * @return
	 */
	public static String getTracedErrorMsg(String errorMsg)
	{
		StringBuilder sb = new StringBuilder("\nError:" + errorMsg + "\n" + "--------- current stack -------\n");
		Arrays.stream(Thread.currentThread().getStackTrace()).forEach(e -> sb.append(e.toString() + "\n"));
		sb.append("timestamp:" + new Timestamp(System.currentTimeMillis()));
		return sb.append("--------- ------- ----- -------").toString();
	}

	/**
	 * 
	 * @param errorMsg
	 * @return
	 */
	public static String getCurrentStackTracedWarningMsg(String errorMsg)
	{
		StringBuilder sb = new StringBuilder("Warning:" + errorMsg + "\n" + "--------- current stack -------\n");
		Arrays.stream(Thread.currentThread().getStackTrace()).forEach(e -> sb.append(e.toString() + "\n"));
		return sb.append("--------- ------- ----- -------").toString();
	}

}
