package org.activity.ui.colors;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Implementation of Viridis color palette in Java.
 * 
 * It takes a sequence of RGB values contained in a file and creates an object with several color functions.
 * 
 * It works with 'viridis', 'inferno', 'magma', and 'plasma' color maps retrieved from:
 * https://github.com/BIDS/colormap/blob/master/colormaps.py
 * 
 * More information about the color palette at: https://bids.github.io/colormap/
 * 
 * 
 * @author juan salamanca
 *
 */
public class ColorMap
{
	private Color[] colorMap;
	private static String currentMap;

	public static final String VIRIDIS = "viridis";
	public static final String INFERNO = "inferno";
	public static final String MAGMA = "magma";
	public static final String PLASMA = "plasma";

	private static ColorMap colorMapInstance = null;

	/**
	 * Get the ColorMap instance. VIRIDIS is set as default color map
	 * 
	 * @return ColorMap instance. Viridis by default
	 */
	public static ColorMap getInstance()
	{
		if (colorMapInstance == null)
		{
			colorMapInstance = new ColorMap(VIRIDIS);
		}
		return colorMapInstance;
	}

	/**
	 * Get the ColorMap set to the desired color map name.
	 * 
	 * @param cMapName
	 * @return ColorMap instance with the desired color map
	 */
	public static ColorMap getInstance(String cMapName)
	{
		if (colorMapInstance == null)
		{
			colorMapInstance = new ColorMap(cMapName);
		}
		else
		{
			colorMapInstance.loadColorMap(cMapName);
			currentMap = cMapName;
		}
		return colorMapInstance;
	}

	/**
	 * Constructor
	 * 
	 * @param cMap
	 *            Color map name
	 */
	private ColorMap(String cMap)
	{
		loadColorMap(cMap);
		currentMap = cMap;
	}

	/**
	 * Set the desired color map to the current instance of this class
	 * 
	 * @param cMapName
	 *            Color map name
	 * @return the instance with the new color map enabled
	 */
	public ColorMap setColorMap(String cMapName)
	{
		if (colorMapInstance == null)
		{
			colorMapInstance = new ColorMap(cMapName);
		}
		else
		{
			colorMapInstance.loadColorMap(cMapName);
			currentMap = cMapName;
		}
		return this;
	}

	/**
	 * Loads one of the four colorMaps defined for the Viridis palette.
	 * 
	 * @param mapName
	 */
	private void loadColorMap(String mapName)
	{
		switch (mapName)
		{
			case "inferno":
				colorMap = getColorMap("inferno.cmap");
				break;
			case "magma":
				colorMap = getColorMap("magma.cmap");
				break;
			case "viridis":
				colorMap = getColorMap("viridis.cmap");
				break;
			case "plasma":
				colorMap = getColorMap("plasma.cmap");
				break;
		}
	}

	/**
	 * Retrieves the colorMap from the source file
	 * 
	 * @param fileName
	 * @return
	 */
	private Color[] getColorMap(String fileName)
	{
		ArrayList<Color> tempColors = new ArrayList<Color>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(ColorMap.class.getResourceAsStream(fileName))))
		{
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null)
			{
				String values[] = sCurrentLine.split(" ");
				float red = Float.valueOf(values[0]);
				float green = Float.valueOf(values[1]);
				float blue = Float.valueOf(values[2]);
				tempColors.add(new Color(red, green, blue));
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Color[] rtn = new Color[tempColors.size()];
		for (int i = 0; i < tempColors.size(); i++)
		{
			rtn[i] = tempColors.get(i);
		}
		return rtn;
	}

	/**
	 * Retrieves a color from the colorMap.
	 * 
	 * @param pos
	 *            The position of the color in the colorMap array. It must be an integer between 0 and colorMap.length
	 * @return the java.awt.Color
	 */
	public Color getColor(int pos)
	{
		if (pos < colorMap.length)
			return colorMap[pos];
		else
			System.out.println(
					this.getClass().getName() + " Position must be a value between 0 and " + (colorMap.length - 1));
		return null;
	}

	/**
	 * Retrieves a color from the colorMap.
	 * 
	 * @param val
	 *            The value between 0 and 1
	 * @return the java.awt.Color
	 */
	public Color getColor(float val)
	{
		int pos = Integer.MAX_VALUE;
		if (val >= 0 && val <= 1)
		{
			pos = (int) Math.floor(map(val, 0, 1, 0, colorMap.length - 1));
			return colorMap[pos];
		}
		else
			System.out.println(this.getClass().getName() + " > Val:" + val + ". Val must be a value between 0 and 1");
		return null;
	}

	/**
	 * The same as getColor() but returns the java.awt.Color as integer
	 * 
	 * @param pos
	 *            integer value
	 * @return
	 */
	public int getColorRGB(int pos)
	{
		return getColor(pos).getRGB();
	}

	/**
	 * The same as getColor() but returns the java.awt.Color as integer
	 * 
	 * @param val
	 *            float value between 0 and 1
	 * @return
	 */
	public int getColorRGB(float val)
	{
		return getColor(val).getRGB();
	}

	/**
	 * Retrieves a color from the current colorMap corresponding to a value mapped within a range. It matches the min
	 * and max range values to the extreme values of the colorMap and picks the color corresponding to the linear
	 * mapping of the desired value on the colorMap
	 * 
	 * @param min
	 *            the lowest value of the range
	 * @param max
	 *            the highest value of the range
	 * @param value
	 *            the value to be mapped into the colorMap
	 * @return the java.awt.Color
	 */
	public Color getMappedColor(float min, float max, float value)
	{
		Color floorColor = Color.WHITE;
		float mapedVal = map(value, min, max, 0, colorMap.length - 1);
		floorColor = colorMap[Math.round(mapedVal)];
		return floorColor;
	}

	/**
	 * The same as getMappedColor() but returns the java.awt.Color as integer
	 * 
	 * @param min
	 * @param max
	 * @param val
	 * @return
	 */
	public int getMappedColorRGB(float min, float max, float val)
	{
		Color floorColor = Color.WHITE;
		float mapedVal = map(val, min, max, 0, colorMap.length - 1);
		floorColor = colorMap[Math.round(mapedVal)];
		return floorColor.getRGB();
	}

	/**
	 * Gets the colorMap as an array of java.awt.Color
	 * 
	 * @return
	 */
	public Color[] getColorMap()
	{
		return colorMap;
	}

	/**
	 * Gets the name of the current color map
	 * 
	 * @return
	 */
	public String getColorMapName()
	{
		return currentMap;
	}

	/**
	 * Gets the length of the colorMap. It it usually 256
	 * 
	 * @return
	 */
	public int getColorMapLength()
	{
		return colorMap.length;
	}

	public Color[] getColorPalette(int steps)
	{
		Color[] palette = new Color[steps];

		float step = colorMap.length / (float) steps;

		for (int i = 0; i < steps; i++)
		{
			float fraction = step * i;
			if (fraction > colorMap.length - step) fraction = colorMap.length - step;
			float val = map(fraction, 0, colorMap.length - step, 0, 1);
			palette[i] = getColor(val);
		}
		return palette;
	}
	//
	// public void show(PApplet app, int orgX, int orgY)
	// {
	//
	// for (int i = 0; i < colorMap.length; i++)
	// {
	// app.stroke(getColorRGB(i));
	// app.strokeCap(PApplet.SQUARE);
	// app.strokeWeight(1);
	// app.line(20 + orgX + i, orgY, 20 + orgX + i, orgY + 10);
	// }
	//
	// app.fill(200);
	// app.textSize(10);
	// app.text("min", orgX, orgY + 10);
	// app.text("max", 23 + orgX + colorMap.length, orgY + 10);
	// app.text(currentMap, 20 + orgX, orgY - 3);
	//
	// }

	// public void showPalette(PApplet app, int orgX, int orgY, int paletteSize)
	// {
	// Color[] palette = getColorPalette(paletteSize);
	// app.noStroke();
	// float size = 256 / palette.length;
	// for (int i = 0; i < palette.length; i++)
	// {
	// app.fill(palette[i].getRGB());
	// app.rect(orgX + (i * size) + 3, orgY, size, 10);
	// }
	//
	// app.fill(200);
	// app.textSize(10);
	// app.textAlign(PApplet.RIGHT);
	// app.text("min", orgX, orgY + 10);
	// app.textAlign(PApplet.LEFT);
	// app.text("max", orgX + 256, orgY + 10);
	// app.text(currentMap, 20 + orgX, orgY - 3);
	// app.stroke(1);
	//
	// }

	/**
	 * Linear mapping copied from processing.org
	 *
	 * @param value
	 * @param istart
	 * @param istop
	 * @param ostart
	 * @param ostop
	 * @return
	 */
	private final float map(float value, float istart, float istop, float ostart, float ostop)
	{
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}

	public static void main(String[] args)
	{
		ColorMap cm = new ColorMap("viridis");

		Color[] tmp = cm.getColorPalette(50);

		for (int i = 0; i < tmp.length; i++)
		{
			System.out.println(tmp[i].toString());
			System.out.println(tmp[i].getRGB());
		}

	}
}