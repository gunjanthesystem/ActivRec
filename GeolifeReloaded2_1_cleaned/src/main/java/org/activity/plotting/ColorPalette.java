package org.activity.plotting;

import javafx.scene.paint.Color;

public class ColorPalette
{

	public ColorPalette()
	{

	}

	static String skyBlue = "69D2E7";
	static String lightCyan = "A7DBD8";
	static String veryLightOlive = "E0E4CC";
	static String orange = "F38630";
	static String vermillionRed = "FA6900";
	static String PALEVIOLETRED = "DB7093";
	static String YELLOWGREEN = "9ACD32";
	// http://www.colourlovers.com/palette/92095/Giant_Goldfish
	static String palleteGoldFish[] = { skyBlue, lightCyan, veryLightOlive, orange, PALEVIOLETRED, YELLOWGREEN };

	public static Color getGoldFishColor(int id)
	{
		if (id > palleteGoldFish.length)
		{
			System.err.println("Error: color our of palette range: id = " + id);
			return null;
		}
		else
		{
			return Color.web(palleteGoldFish[id]);
		}
		// Color k;
	}

}
