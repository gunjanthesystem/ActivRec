package org.activity.plotting;

import java.sql.Timestamp;
import java.util.Arrays;

import org.activity.ui.Dashboard3;
import org.activity.ui.UIUtilityBox;
import org.activity.ui.colors.ColorPalette;

import javafx.scene.CacheHint;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

/**
 * Fork from ActivityCircle to see if performance would improve if extending Circle directly without going through Group.
 * @since 17 Feb 2019, found no improvement over ActivityCircle, hence deprecated.
 */
/** Candle node used for drawing a candle */
@Deprecated
public class ActivityCircle2 extends Circle
{
	// private Line highLowLine = new Line();
	// private Circle circle;// = new Circle();
	// private Rectangle regionBar = new Rectangle();
	private int actID;
	// private String seriesStyleClass;
	// private String dataStyleClass;
	// private boolean openAboveClose = true;
	private Tooltip tooltip; // = new Tooltip();

	public boolean hasTooltip()
	{
		if (tooltip == null)
		{
			return false;
		}
		else
		{
			return true;
		}
		// (tooltipnull)?return false:return true;
	}

	// private static int height = 30;
	private static long timeTakeByTooltipHack = 0;

	/**
	 * 
	 * @param seriesStyleClass
	 * @param dataStyleClass
	 * @param actExtraVals
	 * @param height
	 */
	ActivityCircle2(String seriesStyleClass, String dataStyleClass, ActivityBoxExtraValues actExtraVals)
	{
		super();

		// setAutoSizeChildren(false);
		// System.out.println("this.isResizable()=" + this.isResizable());

		if (false)
		{
			this.setCache(true);
			this.setCacheHint(CacheHint.SPEED);
			this.setCache(true);
			this.setCacheHint(CacheHint.SPEED);
		}

		// getChildren().addAll(circle);
		// this.seriesStyleClass = seriesStyleClass;
		// this.dataStyleClass = dataStyleClass;
		// updateStyleClasses();// disabled for performance
		if (true)
		{
			tooltip = new Tooltip();
			tooltip.setGraphic(
					new GTooltipContent(Arrays.asList("EndTime:", "ActivityName:", "ActivityID:", "Location:")));

			// long t1 = System.nanoTime();
			UIUtilityBox.hackTooltipStartTiming(tooltip);
			// timeTakeByTooltipHack += (System.nanoTime() - t1);
			// System.out.println("time taken by tooltip hack" + timeTakeByTooltipHack / 1000000.0 + " ms");
			Tooltip.install(this, tooltip);
		}
		// regionBar.setShape(new Circle());

		/// Start of moved from updateStyleClasses() to avoid repeated calls
		if (false)// disabled on 17 Feb 2019
		{
			getStyleClass().setAll("activitybox-box", seriesStyleClass, dataStyleClass);
		}
		actID = actExtraVals.getActivityID();
		// System.out.println("actExtraVals= " + actExtraVals.toString());
		setBackGround();
	}

	private void setBackGround()
	{
		// circle.setFill(ColorPalette.getInsightSecondaryColor(actID % 11));
		// circle.setFill(ColorPalette.getColors269Color(actID));
		// circle.setFill(ColorPalette.getColors269Color(Constant.getIndexOfActIDInActNames(actID)));// only works for
		// real data
		// circle.setFill(ColorPalette.getInsightSecondaryColor(Dashboard3.actIDIndexMap.get(actID)));// only
		if (Dashboard3.actIDIndexMap != null)
		{
			this.setFill(ColorPalette.getColor(Dashboard3.actIDIndexMap.get(actID)));// only
		}
		else
		{
			this.setFill(ColorPalette.getColors269Color(actID));
		}
		// works for real
	}

	/**
	 * 
	 * @param open
	 * @param close
	 * @param high
	 * @param low
	 */
	public void updateTooltip(String endTimestamp, String actName, String high, String low)
	{
		// System.out.println("updateTooltip() called");
		GTooltipContent tooltipContent = (GTooltipContent) tooltip.getGraphic();
		String endTSString = new Timestamp(Double.valueOf(endTimestamp).longValue()).toString();
		// System.out.println("endTimestamp = " + endTimestamp);
		// System.out.println("endTimestamp = " + endTSString);
		//
		tooltipContent.update(endTSString, actName, high, low);
	}

	/**
	 * 
	 * @param seriesStyleClass
	 * @param dataStyleClass
	 */
	public void setSeriesAndDataStyleClasses(String seriesStyleClass, String dataStyleClass)
	{
		// System.out.println("setSeriesAndDataStyleClasses() called");
		// this.seriesStyleClass = seriesStyleClass;
		// this.dataStyleClass = dataStyleClass;
		// updateStyleClasses();
	}

	/**
	 * 
	 * @param x1
	 *            xAxisDisplayPosititionWRTStartTime
	 * @param x2
	 *            AxisDisplayPosititionWRTEndTime
	 */
	public void update(double xCenter, double yCenter, double radius)
	{
		// System.out.println("ActivityCircle update() called with xCenter=" + xCenter + " yCenter=" + yCenter);
		// circle.relocate(xCenter, yCenter);
		// PopUps.printTracedWarningMsg("\n---\n");
		// $$ Disabled for performance.
		// $$updateStyleClasses();

		// openAboveClose = closeOffset > 0;
		// highLowLine.setStartY(highOffset);
		// highLowLine.setEndY(lowOffset);

		// highLowLine.setStartX(highOffset);
		// highLowLine.setEndX(lowOffset);

		// if (width == -1)
		// {width = regionBar.prefWidth(-1);
		// }
		// if (openAboveClose)
		// {
		// $$System.out.println("Inside ActivityBox2.update(): x1=" + x1 + " x2=" + x2);
		// $$ regionBar.resizeRelocate(0, -(height / 2), x2 - x1, height);// x2 - x1, x2 - x1);// -width
		// double width = x2 - x1;
		// regionBar.resizeRelocate(0, -(height / 2), width, height);// x2 - x1, x2 - x1);// -width /
		// regionBar.setX
		// circle.resize(width, height);
		// circle/
		// circle.relocate(0, -(height / 2));

		this.setCenterX(xCenter);
		this.setCenterY(yCenter);
		this.setRadius(radius);

		// System.out.println("Circle center()=" + circle.getCenterX() + "," + circle.getCenterY());
		// System.out.println("Circle layout()=" + circle.getLayoutX() + "," + circle.getLayoutY());
		// System.out.println("Circle radius=" + circle.getRadius());
		// System.out.println("\nwidth (x2-x1)= " + width);
		// System.out.println("\nregionBar.getWidth()= " + circle.getWidth());
		// System.out.println("\nregionBar.getLayoutX()= " + circle.getLayoutX());
		// System.out.println(
		// "\nregionBar.getLayoutX() + regionBar.getWidth()= " + (circle.getLayoutX() + circle.getWidth()));
		// 2, 0, width,
		// regionBar.resizeRelocate(0, -(height / 2), x2 - x1, height);// x2 - x1, x2 - x1);// -width / 2, 0, width,
		// closeOffset);

		// $$regionBar.resizeRelocate(0, -(height / 2), 10, height);// x2 - x1, x2 - x1);// -width / 2, 0, width,
		// y coordinate = -(height / 2), to vertically align the activity bos

		// }
		// else
		// {
		// regionBar.resizeRelocate(-width / 2, closeOffset, width, -closeOffset);
		// }
		// setBackGround();// this might slow down as
	}

	// /**
	// *
	// */
	// private void updateStyleClasses()
	// {
	// // $$ Moved from here to ActivityBox2() constructor to avoid repeated calls for performance
	// // System.out.println("updateStyleClasses() called");
	// // // final String aboveClose = openAboveClose ? "open-above-close" : "close-above-open";
	// // getStyleClass().setAll("candlestick-candle", seriesStyleClass, dataStyleClass);
	// // // highLowLine.getStyleClass().setAll("candlestick-line", seriesStyleClass, dataStyleClass, aboveClose);
	// // regionBar.getStyleClass().setAll("candlestick-bar", seriesStyleClass, dataStyleClass);// , aboveClose);
	// }
}
