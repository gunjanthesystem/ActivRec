package org.activity.ui;

import java.awt.HeadlessException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
		}
		catch (HeadlessException e)
		{
			System.err.println("\n Headless: hence printing error msg instead of PopUp.\n" + msg);
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
		// String exceptionMsg =ExceptionUtils.getStackTrace(throwable)// e.getMessage();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionMsg = sw.toString(); // stack trace as a string

		exceptionMsg += ("\n Exception in " + methodName + "\n");
		PopUps.showError(exceptionMsg);
	}

}
