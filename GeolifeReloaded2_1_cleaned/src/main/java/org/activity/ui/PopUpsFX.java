package org.activity.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * <font color = "red"> NOT FINISHED, DO NOT USE </font>
 * 
 * @author gunjan
 *
 */
public class PopUpsFX extends Application
{
	public static void main(String args[])
	{
		launch(args);
		PopUps.showMessage("test");
	}

	/**
	 * 
	 * @param msg
	 */
	public static void showMessage(String msg)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Message1");
		alert.setContentText(msg);
		alert.showAndWait();
		alert.setHeight(150);
		alert.setHeight(300);

		// JFrame frame = new JFrame();
		// // frame.setSize(200, 150);
		// // frame.getContentPane( ).setLayout(new BorderLayout( ));
		// JOptionPane.showMessageDialog(frame, msg);
		// // frame.getContentPane( ).add(p, BorderLayout.SOUTH);
		// // frame.setVisible(true);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * 
	 * @param msg
	 */
	public static void showError(String msg)
	{
		JFrame frame = new JFrame();
		JOptionPane.showMessageDialog(frame, msg, "Error Encountered", JOptionPane.ERROR_MESSAGE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Displays a popup pane with exception message
	 * 
	 * @param e
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
		PopUpsFX.showError(exceptionMsg);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		showMessage("Testing aler box");
	}

}
