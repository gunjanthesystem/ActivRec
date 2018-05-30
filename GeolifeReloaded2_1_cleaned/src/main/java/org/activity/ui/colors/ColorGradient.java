package org.activity.ui.colors;

import java.awt.Color;

/**
 * A utility class that returns categorical gradients of a given color.The returned gradient ranges from the current
 * color brightness to 80% brightness.
 * 
 * @author juan salamanca
 *
 */
public class ColorGradient
{
	/**
	 * Gets a gradient of a given color with the specified number of steps. The returned gradient ranges from the
	 * current brightness to 80% brightness.
	 * 
	 * @param c
	 *            the java.lang.Color
	 * @param steps
	 *            the number of gradient steps
	 * @return the set of colors
	 */
	public static Color[] getColorGradient(Color c, int steps)
	{
		Color[] palette = new Color[steps];
		float[] hsbVals = new float[3];

		Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbVals);

		float step = 0.8f / (float) steps;

		for (int i = 0; i < steps; i++)
		{
			palette[i] = Color.getHSBColor(hsbVals[0], hsbVals[1], 1 - (step * i));
		}
		return palette;
	}

	/**
	 * Gets a specific color from a color gradient
	 * 
	 * @param c
	 *            the java.lang.Color
	 * @param totalSteps
	 *            the number of gradient steps
	 * @param position
	 *            the color position in the gradient scale
	 * @return the integer value of this RGB color. If no color is available, it returns black
	 */
	public static int getStepIntValueFromGradient(Color c, int totalSteps, int position)
	{
		int rtn = 0;
		Color[] palette = getColorGradient(c, totalSteps);

		try
		{
			return palette[position].getRGB();
		}
		catch (Exception np)
		{
			return rtn;
		}
	}

	/**
	 * Gets a specific color from a color gradient
	 * 
	 * @param c
	 *            the java.lang.Color
	 * @param totalSteps
	 *            the number of gradient steps
	 * @param position
	 *            the color position in the gradient scale
	 * @return the integer value of this RGB color. If no color is available, it returns black
	 */
	public static Color getStepColorFromGradient(Color c, int totalSteps, int position)
	{
		Color[] palette = getColorGradient(c, totalSteps);

		try
		{
			return palette[position];
		}
		catch (Exception np)
		{
			return Color.BLACK;
		}
	}
}
