
package org.activity.plotting;

import java.util.Arrays;
import java.util.Iterator;

import org.activity.ui.Dashboard4;
import org.activity.ui.UIConstants;
import org.activity.ui.UIUtilityBox;
import org.activity.ui.colors.ColorPalette;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Fork of TimelineChartCircle for better performance by directly using Circle object but seems to not improve
 * performance, hence deprecated
 * <p>
 * TODO: make it scrollable if smaller screen height causes overlapping of activity boxes.
 * 
 * @author gunjan
 * @since 18 Feb 2019
 */
@Deprecated
public class TimelineChartCircle2 extends XYChart<Number, String>
{

	// double heightOfActivityBox;
	boolean verbose = false;

	/**
	 * Construct a new TimelineChart with the given axis.
	 *
	 * @param xAxis
	 *            The x axis to use
	 * @param yAxis
	 *            The y axis to use
	 */
	public TimelineChartCircle2(Axis<Number> xAxis, Axis<String> yAxis)
	{
		super(xAxis, yAxis);

		if (false)
		{// disabled on 17 Feb 2019, signficantlt improves performance
			final String timelineChartCss = getClass().getResource("TimelineChart.css").toExternalForm();
			getStylesheets().add(timelineChartCss); // disabled on 13 March 2018
			setAnimated(true);
			xAxis.setAnimated(true);
			yAxis.setAnimated(true);
		}
		this.setCache(true);

	}

	private static void setBackGround(int actID, Circle circle)
	{
		// circle.setFill(ColorPalette.getInsightSecondaryColor(actID % 11));
		// circle.setFill(ColorPalette.getColors269Color(actID));
		// circle.setFill(ColorPalette.getColors269Color(Constant.getIndexOfActIDInActNames(actID)));// only works for
		// real data
		// circle.setFill(ColorPalette.getInsightSecondaryColor(Dashboard3.actIDIndexMap.get(actID)));// only
		if (Dashboard4.actIDIndexMap != null)
		{
			circle.setFill(ColorPalette.getColor(Dashboard4.actIDIndexMap.get(actID)));// only
		}
		else
		{
			circle.setFill(ColorPalette.getColors269Color(actID));
		}
		// works for real
	}

	/**
	 * Construct a new TimelineChart with the given axis and data.
	 *
	 * @param xAxis
	 *            The x axis to use
	 * @param yAxis
	 *            The y axis to use
	 * @param data
	 *            The actual data list to use so changes will be reflected in the chart.
	 */
	public TimelineChartCircle2(Axis<Number> xAxis, Axis<String> yAxis, ObservableList<Series<Number, String>> data)
	{
		this(xAxis, yAxis);
		setData(data);
	}

	/**
	 * Called to update and layout the content for the plot .
	 * <p>
	 * This method is being called whenever the window is resized
	 */
	@Override
	protected void layoutPlotChildren()
	{
		if (verbose)
		{
			System.out.println("\nTimelineChartCircle.layoutPlotChildren() called");
		}
		// we have nothing to layout if no data is present
		if (getData() == null)
		{
			return;
		}

		double radiusOfCircle = this.getHeight() / (getData().size() * 3);
		radiusOfCircle = Math.min(Math.max(radiusOfCircle, 5), 15);// bound between 5 and 30
		// radiusOfCircle = 5;
		// double radiusOfCircle

		Axis<String> yAxis = getYAxis();
		Axis<Number> xAxis = getXAxis();

		// $ System.out.println("getData().size()= " + );
		// update activity box positions
		for (int index = 0; index < getData().size(); index++)
		{
			Series<Number, String> series = getData().get(index);
			Iterator<XYChart.Data<Number, String>> iter = getDisplayedDataIterator(series);
			// $ System.out.println("series.getData().size()= " + series.getData().size());

			while (iter.hasNext())
			{
				XYChart.Data<Number, String> item = iter.next();
				Number displayedXVal = getCurrentDisplayedXValue(item);
				String displayedYVal = getCurrentDisplayedYValue(item);
				double xDispPosition = xAxis.getDisplayPosition(displayedXVal);
				double yDispPosition = yAxis.getDisplayPosition(displayedYVal);

				// to find best height start
				// System.out.println("item= " + item);
				// System.out.println("displayedXVal= " + displayedXVal);
				// System.out.println("displayedYVal= " + displayedYVal);
				// System.out.println("xDispPosition= " + xDispPosition);
				// System.out.println("yDispPosition= " + yDispPosition);
				// System.out.println("--> yAxis.getMaxHeight() =" + yAxis.getMaxHeight());
				// System.out.println("--> yAxis.getMinHeight() =" + yAxis.getMinHeight());
				// System.out.println("--> yAxis.getPrefHeight() =" + yAxis.getPrefHeight());
				// System.out.println("--> yAxis.autoRangingProperty() =" + yAxis.autoRangingProperty());//true
				// $$ System.out.println("--> this.height() =" + this.getHeight());
				// $$ System.out.println("--> heightOfActBox = " + heightOfActBox);
				// heightOfActBox+emptySpaceAboveActBox+emptySpaceBelowActBox = (heightOfChart/numOfUsers)
				// heightOfActBox+0.25*heightOfActBox+0.25*heightOfActBox = (heightOfChart/numOfUsers)
				// to find best height end

				// $$System.out.println("displayedXVal=" + displayedXVal + " displayedXVal=" + displayedYVal);
				// $$System.out.println("xDispPosition=" + xDispPosition + " yDispPosition=" + yDispPosition);

				Node itemNode = item.getNode();
				ActivityBoxExtraValues extra = (ActivityBoxExtraValues) item.getExtraValue();

				// if (itemNode instanceof ActivityCircle && extra != null)
				if (itemNode instanceof Circle && extra != null)
				{
					double endTS = xAxis.getDisplayPosition(extra.getEndTimestamp());

					// System.out.println("extra.getEndTimestamp()= " + extra.getEndTimestamp());
					// System.out.println("endTS= " + endTS);
					// StringBuilder sb = new StringBuilder();
					// IntStream.rangeClosed(1, 10).map(i -> i * 5)
					// .forEachOrdered(i -> sb.append("\nxval= " + i + " pos=" + xAxis.getDisplayPosition(i)));
					// System.out.println(sb.toString());
					// double high = xAxis.getDisplayPosition(extra.getHigh());
					// double low = xAxis.getDisplayPosition(extra.getLow());
					// calculate activity box width
					// double activityBoxWidth = -1;

					// update activity box
					// ActivityCircle activityCircle = (ActivityCircle) itemNode;
					Circle activityCircle = (Circle) itemNode;
					// activityCircle.update(xDispPosition, yDispPosition, radiusOfCircle);
					activityCircle.setCenterX(xDispPosition);
					activityCircle.setCenterY(yDispPosition);
					activityCircle.setRadius(radiusOfCircle);
					// extra.getClose());// , 0, 0);// // close - y, high - y, low - y,activityBoxWidth);

					// moved to series added to improve performance by reducing calls to updateTooltip
					// $$activityBox.updateTooltip(String.valueOf(extra.getEndTimestamp()),
					// $$ String.valueOf(extra.getActivityName()), String.valueOf(extra.getActivityID()),
					// $$String.valueOf(extra.getActivityID()));

					// position the activity box
					// $activityCircle.setLayoutX(xDispPosition);
					// $activityCircle.setLayoutY(yDispPosition);

					// System.out.println("\nxDispPosition= " + xDispPosition);
					// System.out.println("endTS= " + endTS);
					// System.out.println("displayedXVal= " + displayedXVal);
					// System.out.println("displayedYVal= " + displayedYVal);

					///////////////////////////
					if (true)
					{
						if (UIConstants.haveTooltip)// TODO
						{
							Tooltip tooltip = new Tooltip();
							if (UIConstants.useLightweightTooltip)
							{
								tooltip.setText("TooltipText");
							}
							else
							{
								tooltip.setGraphic(new GTooltipContent(
										Arrays.asList("EndTime:", "ActivityName:", "ActivityID:", "Location:")));
							}
							// long t1 = System.nanoTime();
							UIUtilityBox.hackTooltipStartTiming(tooltip);
							// timeTakeByTooltipHack += (System.nanoTime() - t1);
							// System.out.println("time taken by tooltip hack" + timeTakeByTooltipHack / 1000000.0 + "
							// ms");
							Tooltip.install(activityCircle, tooltip);
						}
						// regionBar.setShape(new Circle());

						/// Start of moved from updateStyleClasses() to avoid repeated calls
						if (false)// disabled on 17 Feb 2019
						{
							// getStyleClass().setAll("activitybox-box", seriesStyleClass, dataStyleClass);
						}
						// System.out.println("actExtraVals= " + actExtraVals.toString());
						setBackGround(extra.getActivityID(), activityCircle);
					}
				}

				////////////////////////////
			}

		}

	}

	@Override
	protected void dataItemChanged(Data<Number, String> item)
	{
		System.out.println("dataItemChanged() called");
	}

	@Override
	protected void dataItemAdded(Series<Number, String> series, int itemIndex, Data<Number, String> item)
	{
		System.out.println("dataItemAdded() called");

		Node actBox = createActivityBox(getData().indexOf(series), item, itemIndex, -1);
		if (shouldAnimate())
		{
			actBox.setOpacity(0.2);
			getPlotChildren().add(actBox);
			// fade in new activity Box
			final FadeTransition ft = new FadeTransition(Duration.millis(100), actBox);
			ft.setToValue(1);
			ft.play();
		}
		else
		{
			getPlotChildren().add(actBox);
		}
		// always draw average line on top
		if (series.getNode() != null)
		{
			series.getNode().toFront();
		}
	}

	@Override
	protected void dataItemRemoved(Data<Number, String> item, Series<Number, String> series)
	{
		System.out.println("dataItemRemoved() called");
		final Node actBox = item.getNode();
		if (shouldAnimate())
		{
			// fade out old activity box
			final FadeTransition ft = new FadeTransition(Duration.millis(100), actBox);
			ft.setToValue(0);
			ft.setOnFinished((ActionEvent actionEvent) ->
				{
					getPlotChildren().remove(actBox);
				});
			ft.play();
		}
		else
		{
			getPlotChildren().remove(actBox);
		}
	}

	@Override
	protected void seriesAdded(Series<Number, String> series, int seriesIndex)
	{
		// handle any data already in series
		// $$ System.out.println("seriesAdded() called for seriesIndex= " + seriesIndex);
		// PopUps.printTracedWarningMsg("who calls me:");

		int numOfSeriesAlreadyAdded = this.getData().size();
		// System.out.println("numOfSeriesAlreadyAdded = " + numOfSeriesAlreadyAdded);

		for (int j = 0; j < series.getData().size(); j++)
		{
			XYChart.Data item = series.getData().get(j);
			Node actBox = createActivityBox(seriesIndex, item, j, -1);

			/// Start
			// moving update tooltip from update layout to here so tooltips are not updated (updateTooltip()) everytime
			/// the windows is resized
			ActivityBoxExtraValues extra = (ActivityBoxExtraValues) item.getExtraValue();
			// if (false)
			{
				// ((ActivityCircle) actBox).updateTooltip(String.valueOf(extra.getEndTimestamp()),
				// String.valueOf(extra.getActivityName()), String.valueOf(extra.getActivityID()),
				// String.valueOf(extra.getActivityID()));

			}
			/// End

			if (shouldAnimate())
			{
				actBox.setOpacity(0);
				getPlotChildren().add(actBox);
				// fade in new activity box
				final FadeTransition ft = new FadeTransition(Duration.millis(100), actBox);
				ft.setToValue(1);
				ft.play();
			}
			else
			{
				getPlotChildren().add(actBox);
			}
		}

	}

	@Override
	protected void seriesRemoved(Series<Number, String> series)
	{
		// remove all activity box nodes
		for (XYChart.Data<Number, String> d : series.getData())
		{
			final Node activityBox = d.getNode();
			if (shouldAnimate())
			{
				// fade out old activity box
				final FadeTransition ft = new FadeTransition(Duration.millis(100), activityBox);
				ft.setToValue(0);
				ft.setOnFinished((ActionEvent actionEvent) ->
					{
						getPlotChildren().remove(activityBox);
					});
				ft.play();
			}
			else
			{
				getPlotChildren().remove(activityBox);
			}
		}
	}

	/**
	 * Create a new ActivityBox node to represent a single data item
	 *
	 * @param seriesIndex
	 *            The index of the series the data item is in
	 * @param item
	 *            The data item to create node for
	 * @param itemIndex
	 *            The index of the data item in the series
	 * @param heightOfActivityBox
	 * @return New ActivityBox node to represent the give data item
	 */
	private Node createActivityBox(int seriesIndex, final XYChart.Data<Number, String> item, int itemIndex,
			int heightOfActivityBox)
	{
		// System.out.println("createActivityBox() called");
		Node actBox = item.getNode();
		// check if ActivityBox has already been created
		// if (actBox instanceof ActivityCircle)
		if (actBox instanceof Circle)
		{
			// ((Circle) actBox).setSeriesAndDataStyleClasses("series" + seriesIndex, "data" + itemIndex);
		}
		else
		{
			actBox = new Circle();
			// ActivityCircle("series" + seriesIndex, "data" + itemIndex,
			// (ActivityBoxExtraValues) item.getExtraValue());
			item.setNode(actBox);
		}
		return actBox;
	}

	/**
	 * This is called when the range has been invalidated and we need to update it. If the axis are auto ranging then we
	 * compile a list of all data that the given axis has to plot and call invalidateRange() on the axis passing it that
	 * data.
	 */
	// @Override
	// protected void updateAxisRange()
	// {
	// // System.out.println("updateAxisRange() called");
	// // For timeline chart we need to override this method as we need
	// // to let the axis know that they need to be able to cover the area
	// // occupied by the high to low range not just its center data value.
	// final Axis<Number> xa = getXAxis();
	// final Axis<String> ya = getYAxis();
	// List<Number> xData = null;
	// List<String> yData = null;
	// if (xa.isAutoRanging())
	// {
	// xData = new ArrayList<Number>();
	// }
	// if (ya.isAutoRanging())
	// {
	// yData = new ArrayList<String>();
	// }
	// if (xData != null || yData != null)
	// {
	// for (XYChart.Series<Number, String> series : getData())
	// {
	// for (XYChart.Data<Number, String> data : series.getData())
	// {
	// if (yData != null)
	// {
	// yData.add(data.getYValue());
	// }
	// if (yData != null)
	// {
	// ActivityBoxExtraValues extras = (ActivityBoxExtraValues) data.getExtraValue();
	// if (extras != null)
	// {
	// // xData.add(extras.getHigh());
	// xData.add(extras.getActivityID());
	// }
	// else
	// {
	// xData.add(data.getXValue());
	// }
	// }
	// }
	// }
	// if (xData != null)
	// {
	// xa.invalidateRange(xData);
	// }
	// if (yData != null)
	// {
	// ya.invalidateRange(yData);
	// }
	// }
	// }

}
