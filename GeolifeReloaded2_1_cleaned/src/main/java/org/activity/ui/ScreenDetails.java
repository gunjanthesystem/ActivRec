package org.activity.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class ScreenDetails
{

	/**
	 * 
	 */
	public static void printScreensDetails()
	{
		ObservableList<Screen> screenList = Screen.getScreens();
		System.out.println("Screens Count: " + screenList.size());

		// Print the details of all screens
		for (Screen screen : screenList)
		{
			printScreenDetails(screen);
		}
	}

	/**
	 * Print Screen details
	 * 
	 * @param s
	 */
	public static void printScreenDetails(Screen s)
	{
		System.out.println("DPI: " + s.getDpi());
		System.out.print("Screen Bounds: ");
		Rectangle2D bounds = s.getBounds();
		print(bounds);
		System.out.print("Screen Visual Bounds: ");

		Rectangle2D visualBounds = s.getVisualBounds();

		print(visualBounds);
		System.out.println("-----------------------");
	}

	public static void print(Rectangle2D r)
	{
		System.out.format("minX=%.2f, minY=%.2f, width=%.2f, height=%.2f%n", r.getMinX(), r.getMinY(), r.getWidth(),
				r.getHeight());
	}

}
