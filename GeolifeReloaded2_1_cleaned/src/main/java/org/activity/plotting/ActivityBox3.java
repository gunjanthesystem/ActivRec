package org.activity.plotting;

import java.util.Arrays;
import java.util.List;

import org.activity.ui.UIUtilityBox;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

/** Candle node used for drawing a candle */
public class ActivityBox3 extends Canvas
{
	// private Line highLowLine = new Line();
	// private Canvas regionBar = new Canvas();
	// private Rectangle regionBar = new Rectangle();

	double x, y, width, height;

	private int actID;
	// private String seriesStyleClass;
	// private String dataStyleClass;
	// private boolean openAboveClose = true;
	private Tooltip tooltip;// = new Tooltip();
	Color bgColor;
	// private static int height = 30;

	/**
	 * 
	 * @param seriesStyleClass
	 * @param dataStyleClass
	 * @param actExtraVals
	 * @param height
	 */
	ActivityBox3(String seriesStyleClass, String dataStyleClass, ActivityBoxExtraValues actExtraVals, Color bgColor)
	{

		this.bgColor = bgColor;
		/*
		 * Make sure the canvas draws its content again when its size changes.
		 */
		widthProperty().addListener(it -> draw());
		heightProperty().addListener(it -> draw());

		// updateStyleClasses();// disabled for performance
		tooltip = new Tooltip();
		tooltip.setGraphic(new GTooltipContent(Arrays.asList("EndTime:", "ActivityName:", "ActivityID:", "Location:")));
		UIUtilityBox.hackTooltipStartTiming(tooltip);
		Tooltip.install(this, tooltip);
		// regionBar.setShape(new Circle());

		/// Start of moved from updateStyleClasses() to avoid repeated calls
		// getStyleClass().setAll("activitybox-box", seriesStyleClass, dataStyleClass);
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
		// draw();
	}

	@Override
	public boolean isResizable()
	{
		return true;
	}

	@Override
	public double prefWidth(double height)
	{
		return getWidth();
	}

	@Override
	public double prefHeight(double width)
	{
		return getHeight();
	}

	public void setData(List<Double> data)
	{
		// this.data = data;
	}

	/*
	 * Draw a chart based on the data provided by the model.
	 */
	void draw()
	{
		GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, getWidth(), getHeight());

		// Stop[] stops = new Stop[] { new Stop(0, Color.SKYBLUE), new Stop(1, Color.SKYBLUE.darker().darker()) };
		// LinearGradient gradient = new LinearGradient(0, 0, 0, 300, false, CycleMethod.NO_CYCLE, stops);
		gc.setFill(this.bgColor);
	}

	// private void setBackGround()
	// {
	// // BackgroundFill bgFill = new BackgroundFill(ColorPalette.getInsightSecondaryColor(actID % 11),
	// // new CornerRadii(12), new Insets(0, 0.25, 0, 0));
	// regionBar.setBackground(new Background(new BackgroundFill(ColorPalette.getInsightSecondaryColor(actID % 11),
	// new CornerRadii(12), new Insets(0, 0.25, 0, 0))));
	// }

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
		this.resizeRelocate(0, -(height / 2), x2 - x1, height);// x2 - x1, x2 - x1);// -width /
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
		draw();// this might slow down as
	}

}
