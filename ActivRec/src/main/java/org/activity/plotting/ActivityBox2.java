package org.activity.plotting;

import java.util.Arrays;

import org.activity.ui.UIUtilityBox;
import org.activity.ui.colors.ColorPalette;

//javafx.geometry.Insets
//import javafx.geometry.Insets;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
//import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
//import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;

/** Candle node used for drawing a candle */
public class ActivityBox2 extends Group
{
	// private Line highLowLine = new Line();
	private Region regionBar = new Region();
	// private Rectangle regionBar = new Rectangle();
	private int actID;
	private String seriesStyleClass;
	private String dataStyleClass;
	// private boolean openAboveClose = true;
	private Tooltip tooltip = new Tooltip();
	// private static int height = 30;

	/**
	 * 
	 * @param seriesStyleClass
	 * @param dataStyleClass
	 * @param actExtraVals
	 * @param height
	 */
	ActivityBox2(String seriesStyleClass, String dataStyleClass, ActivityBoxExtraValues actExtraVals)
	{
		// if (height != -1)
		// {
		// this.height = height;
		// }
		// System.out.println("ActivityBox2() created");
		setAutoSizeChildren(false);

		// System.out.println("this.isResizable()=" + this.isResizable());

		this.setCache(true);
		this.setCacheHint(CacheHint.SPEED);

		regionBar.setCache(true);
		regionBar.setCacheHint(CacheHint.SPEED);

		getChildren().addAll(/* highLowLine, */regionBar);
		this.seriesStyleClass = seriesStyleClass;
		this.dataStyleClass = dataStyleClass;
		// updateStyleClasses();// disabled for performance
		tooltip.setGraphic(new GTooltipContent(Arrays.asList("EndTime:", "ActivityName:", "ActivityID:", "Location:")));
		UIUtilityBox.hackTooltipStartTiming(tooltip);
		Tooltip.install(regionBar, tooltip);
		// regionBar.setShape(new Circle());

		/// Start of moved from updateStyleClasses() to avoid repeated calls
		if (false)// dsiabled on 18 Feb 2019
		{
			getStyleClass().setAll("activitybox-box", seriesStyleClass, dataStyleClass);
		}
		// highLowLine.getStyleClass().setAll("candlestick-line", seriesStyleClass, dataStyleClass, aboveClose);
		// $$regionBar.getStyleClass().setAll("candlestick-bar", seriesStyleClass, dataStyleClass);// , aboveClose);
		/// End of moved from updateStyleClasses() to avoid repeated calls

		// regionBar.backgroundProperty()
		// .bind(Bindings.when(toggle.selectedProperty())
		// .then(new Background(new BackgroundFill(Color.CORNFLOWERBLUE, CornerRadii.EMPTY, Insets.EMPTY)))
		// .otherwise(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY))));
		// regionBar.setBackground(
		// new Background(new BackgroundFill(Color.PALEVIOLETRED, new CornerRadii(2), new Insets(0, 0.25, 0, 0))));
		// Color color1 = ColorPalette.getGoldFishColor(3);

		// $$regionBar.setBackground(new Background(
		// new BackgroundFill(ColorPalette.getInsightSecondaryColor(actExtraVals.getActivityID() % 11),
		// new CornerRadii(12)/* 2.5) */, new Insets(0, 0.25, 0, 0))));

		// BackgroundFill bgFill = new BackgroundFill(
		// ColorPalette.getInsightSecondaryColor(actExtraVals.getActivityID() % 11), new CornerRadii(12),
		// new Insets(0, 0.25, 0, 0));
		// regionBar.setBackground(new Background(bgFill));

		// regionBar.setShape(new Circle(10));
		// regionBar.getBackground().getFills()
		// .add(new BackgroundFill(Color.PALEVIOLETRED, CornerRadii.EMPTY, Insets.EMPTY));
		// regionBar.getBackground().getFills().add(e)
		actID = actExtraVals.getActivityID();
		setBackGround();
	}

	private void setBackGround()
	{
		// BackgroundFill bgFill = new BackgroundFill(ColorPalette.getInsightSecondaryColor(actID % 11),
		// new CornerRadii(12), new Insets(0, 0.25, 0, 0));
		regionBar.setBackground(new Background(new BackgroundFill(ColorPalette.getInsightSecondaryColor(actID % 11),
				new CornerRadii(0), new Insets(0, 0.25, 0, 0))));
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
		tooltipContent.update(endTimestamp, actName, high, low);
	}

	/**
	 * 
	 * @param seriesStyleClass
	 * @param dataStyleClass
	 */
	public void setSeriesAndDataStyleClasses(String seriesStyleClass, String dataStyleClass)
	{
		// System.out.println("setSeriesAndDataStyleClasses() called");
		this.seriesStyleClass = seriesStyleClass;
		this.dataStyleClass = dataStyleClass;
		updateStyleClasses();
	}

	// public void setHeight(int numOfUsers)
	// {
	// height = (int) (Math.pow(1.02, (-numOfUsers)) * 50);
	// }

	/**
	 * 
	 * @param x1
	 *            xAxisDisplayPosititionWRTStartTime
	 * @param x2
	 *            AxisDisplayPosititionWRTEndTime
	 */
	public void update(double x1, double x2, double height)// closeOffset, double highOffset, double lowOffset, double
															// width)
	{
		System.out.println("ActivityBox2 update() called with x1=" + x1 + " x2=" + x2);
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
		double width = x2 - x1;
		// regionBar.resizeRelocate(0, -(height / 2), width, height);// x2 - x1, x2 - x1);// -width /
		// regionBar.setX
		regionBar.resize(width, height);
		regionBar.relocate(0, -(height / 2));
		System.out.println("\nwidth (x2-x1)= " + width);
		System.out.println("\nregionBar.getWidth()= " + regionBar.getWidth());
		System.out.println("\nregionBar.getLayoutX()= " + regionBar.getLayoutX());
		System.out.println(
				"\nregionBar.getLayoutX() + regionBar.getWidth()= " + (regionBar.getLayoutX() + regionBar.getWidth()));
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
		setBackGround();// this might slow down as
	}

	/**
	 * 
	 */
	private void updateStyleClasses()
	{
		// $$ Moved from here to ActivityBox2() constructor to avoid repeated calls for performance
		// System.out.println("updateStyleClasses() called");
		// // final String aboveClose = openAboveClose ? "open-above-close" : "close-above-open";
		// getStyleClass().setAll("candlestick-candle", seriesStyleClass, dataStyleClass);
		// // highLowLine.getStyleClass().setAll("candlestick-line", seriesStyleClass, dataStyleClass, aboveClose);
		// regionBar.getStyleClass().setAll("candlestick-bar", seriesStyleClass, dataStyleClass);// , aboveClose);
	}
}
