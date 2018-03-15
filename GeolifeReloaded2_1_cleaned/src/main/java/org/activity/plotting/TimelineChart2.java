
package org.activity.plotting;

import java.util.Iterator;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

/**
 * TODO: make it scrollable if smaller screen height causes overlapping of activity boxes.
 * 
 * @author gunjan
 *
 */
public class TimelineChart2 extends XYChart<Number, String>
{

	// double heightOfActivityBox;

	/**
	 * Construct a new TimelineChart with the given axis.
	 *
	 * @param xAxis
	 *            The x axis to use
	 * @param yAxis
	 *            The y axis to use
	 */
	public TimelineChart2(Axis<Number> xAxis, Axis<String> yAxis)
	{
		super(xAxis, yAxis);
		final String timelineChartCss = getClass().getResource("TimelineChart.css").toExternalForm();
		getStylesheets().add(timelineChartCss); // disabled on 13 March 2018
		setAnimated(true);
		xAxis.setAnimated(true);
		yAxis.setAnimated(true);
		this.setCache(true);

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
	public TimelineChart2(Axis<Number> xAxis, Axis<String> yAxis, ObservableList<Series<Number, String>> data)
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
		System.out.println("\nTimelineChart2.layoutPlotChildren() called");

		// we have nothing to layout if no data is present
		if (getData() == null)
		{
			return;
		}

		int numOfSeries = getData().size();
		// $ System.out.println("getData().size()= " + );
		// update activity box positions
		for (int index = 0; index < getData().size(); index++)
		{
			Series<Number, String> series = getData().get(index);
			Iterator<XYChart.Data<Number, String>> iter = getDisplayedDataIterator(series);
			// $ System.out.println("series.getData().size()= " + series.getData().size());

			while (iter.hasNext())
			{
				Axis<String> yAxis = getYAxis();
				Axis<Number> xAxis = getXAxis();

				XYChart.Data<Number, String> item = iter.next();
				Number displayedXVal = getCurrentDisplayedXValue(item);
				String displayedYVal = getCurrentDisplayedYValue(item);
				double xDispPosition = xAxis.getDisplayPosition(displayedXVal);
				double yDispPosition = yAxis.getDisplayPosition(displayedYVal);

				// to find best height start
				// System.out.println("--> yAxis.getMaxHeight() =" + yAxis.getMaxHeight());
				// System.out.println("--> yAxis.getMinHeight() =" + yAxis.getMinHeight());
				// System.out.println("--> yAxis.getPrefHeight() =" + yAxis.getPrefHeight());
				// System.out.println("--> yAxis.autoRangingProperty() =" + yAxis.autoRangingProperty());//true
				// $$ System.out.println("--> this.height() =" + this.getHeight());
				double heightOfActBox = this.getHeight() / (numOfSeries * 1.5);
				heightOfActBox = Math.min(Math.max(heightOfActBox, 5), 30);// bound between 5 and 30
				// $$ System.out.println("--> heightOfActBox = " + heightOfActBox);
				// heightOfActBox+emptySpaceAboveActBox+emptySpaceBelowActBox = (heightOfChart/numOfUsers)
				// heightOfActBox+0.25*heightOfActBox+0.25*heightOfActBox = (heightOfChart/numOfUsers)
				// to find best height end

				// $$System.out.println("displayedXVal=" + displayedXVal + " displayedXVal=" + displayedYVal);
				// $$System.out.println("xDispPosition=" + xDispPosition + " yDispPosition=" + yDispPosition);

				Node itemNode = item.getNode();
				ActivityBoxExtraValues extra = (ActivityBoxExtraValues) item.getExtraValue();

				if (itemNode instanceof ActivityBox2 && extra != null)
				{
					double endTS = xAxis.getDisplayPosition(extra.getEndTimestamp());
					// double high = xAxis.getDisplayPosition(extra.getHigh());
					// double low = xAxis.getDisplayPosition(extra.getLow());
					// calculate activity box width
					// double activityBoxWidth = -1;

					// update activity box
					ActivityBox2 activityBox = (ActivityBox2) itemNode;
					activityBox.update(xDispPosition, endTS, heightOfActBox);
					// extra.getClose());// , 0, 0);// // close - y, high - y, low - y,activityBoxWidth);

					// moved to series added to improve performance by reducing calls to updateTooltip
					// $$activityBox.updateTooltip(String.valueOf(extra.getEndTimestamp()),
					// $$ String.valueOf(extra.getActivityName()), String.valueOf(extra.getActivityID()),
					// $$String.valueOf(extra.getActivityID()));

					// position the activity box
					activityBox.setLayoutX(xDispPosition);
					activityBox.setLayoutY(yDispPosition);
				}

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
		System.out.println("seriesAdded() called for seriesIndex= " + seriesIndex);
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
			((ActivityBox2) actBox).updateTooltip(String.valueOf(extra.getEndTimestamp()),
					String.valueOf(extra.getActivityName()), String.valueOf(extra.getActivityID()),
					String.valueOf(extra.getActivityID()));
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
	private Node createActivityBox(int seriesIndex, final XYChart.Data item, int itemIndex, int heightOfActivityBox)
	{
		// System.out.println("createActivityBox() called");
		Node actBox = item.getNode();
		// check if ActivityBox has already been created
		if (actBox instanceof ActivityBox2)
		{
			((ActivityBox2) actBox).setSeriesAndDataStyleClasses("series" + seriesIndex, "data" + itemIndex);
		}
		else
		{
			actBox = new ActivityBox2("series" + seriesIndex, "data" + itemIndex,
					(ActivityBoxExtraValues) item.getExtraValue());
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
