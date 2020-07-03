package org.activity.ui;

import org.activity.ui.colors.ColorPalette;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * Contains immutable objects which can be reused for better performance
 * 
 * @author gunjan
 *
 */
public class ReusableElements
{
	private Background grayBackground;
	private Background linenBackground;
	private Background chartreuseBackground;
	private Background blanchedalmondBackground;
	private Background lightslategreyBackground;
	private Border bottomBorder1, bottomBorder2;

	public ReusableElements()
	{

		// CornerRadii.EMPTY, Insets.EMPTY)
		grayBackground = new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY));
		chartreuseBackground = new Background(new BackgroundFill(Color.CHARTREUSE, CornerRadii.EMPTY, Insets.EMPTY));
		blanchedalmondBackground = new Background(
				new BackgroundFill(Color.BLANCHEDALMOND, CornerRadii.EMPTY, Insets.EMPTY));
		linenBackground = new Background(new BackgroundFill(Color.LINEN, CornerRadii.EMPTY, Insets.EMPTY));
		lightslategreyBackground = new Background(
				new BackgroundFill(Color.LIGHTSLATEGREY, CornerRadii.EMPTY, Insets.EMPTY));

		Color c1 = ColorPalette.getTwoToneMudCyanColor(0);
		bottomBorder1 = new Border(new BorderStroke(c1, c1, c1, c1, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
				BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths(1, 1, 7, 1),
				Insets.EMPTY));

		c1 = ColorPalette.getTwoToneMudCyanColor(1);
		bottomBorder2 = new Border(new BorderStroke(c1, c1, c1, c1, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
				BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths(1, 1, 7, 1),
				Insets.EMPTY));
	}

	public Background getGrayBackground()
	{
		return grayBackground;
	}

	public Background getLinenBackground()
	{
		return linenBackground;
	}

	public Background getChartreuseBackground()
	{
		return chartreuseBackground;
	}

	public Background getBlanchedalmondBackground()
	{
		return blanchedalmondBackground;
	}

	public Background getLightslategreyBackground()
	{
		return lightslategreyBackground;
	}

	public Border getBottomBorder1()
	{
		return bottomBorder1;
	}

	public Border getBottomBorder2()
	{
		return bottomBorder2;
	}

}
