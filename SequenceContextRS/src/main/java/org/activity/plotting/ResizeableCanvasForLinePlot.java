package org.activity.plotting;

import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

/*
 * Canvas is normally not resizable but by overriding isResizable() and
 * binding its width and height to the width and height of the cell it will
 * automatically resize.
 */
public class ResizeableCanvasForLinePlot extends Canvas
{

	// private List<List<String>> data = Collections.emptyList();
	ObservableList<Series<Double, Double>> data = null;

	public ResizeableCanvasForLinePlot()// ObservableList<Series<Double, Double>> dataGiven)
	{

		final Tooltip tooltip = new Tooltip();
		// tooltip.setText("data.size()=" + data.size());
		// data = dataGiven;
		// setData(dataGiven);
		System.out.println("data.size()=" + data.size());
		// Tooltip.install(this, tooltip);
		// pf.setTooltip(tooltip);
		draw();
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

	public void setData(ObservableList<Series<Double, Double>> dataGiven)
	{
		this.data = dataGiven;
	}

	/*
	 * Draw a chart based on the data provided by the model.
	 */
	void draw()
	{// Do one series first

		Series<Double, Double> firstSeries = data.get(0);
		String seriesName = firstSeries.getName();
		System.out.println("Drawing seriesName: " + seriesName);

		GraphicsContext gc = getGraphicsContext2D();
		double width = getWidth();
		double height = getHeight();

		gc.clearRect(0, 0, width, height);
		gc.strokeRoundRect(10, 10, 50, 50, 10, 10);

		if (false)
		{
			// Stop[] stops = new Stop[] { new Stop(0, Color.SKYBLUE), new Stop(1, Color.SKYBLUE.darker().darker()) };
			// LinearGradient gradient = new LinearGradient(0, 0, 0, 300, false, CycleMethod.NO_CYCLE, stops);
			// gc.setFill(gradient);
			double availableHeight = height * .5;
			double counter = 0;

			// Color emptyColor = Color.WHITE;
			ObservableList<Data<Double, Double>> dataToPlot = firstSeries.getData();

			double cursorStartTS = (dataToPlot.get(0).getXValue());// data.get(0).get(1));

			double barHeight = (availableHeight);// * 100) / 100;
			double y = (height - barHeight) / 2;

			int numOfDaysToShowInFullWidth = 10;
			int numOfSecsInFullWidth = numOfDaysToShowInFullWidth * 86400;// 24*60*60
			double pixelsPerSecond = width / numOfSecsInFullWidth;
			// System.out.println("pixelsPerSecond = " + pixelsPerSecond);

			// for (List<String> actDetails : dataToPlot)
			for (int i = 0; i < dataToPlot.size(); i++)
			{
				Data<Double, Double> dataPoint = dataToPlot.get(i);
				// System.out.println("2-data.size()= " + data.size());
				double startTSInSec = dataPoint.getXValue() - 1;// Double.valueOf(actDetails.get(1));
				double endTSInSecs = dataPoint.getXValue() + 1;// Double.valueOf(actDetails.get(2));
				// if (cursorStartTS < startTS){}
				double actID = dataPoint.getYValue();// Integer.valueOf(actDetails.get(5));

				// double x = width / /* 365 */ counter;
				// double barWidth = width / 365 + 1;

				double x = (startTSInSec - cursorStartTS) * pixelsPerSecond;
				double barWidth = ((endTSInSecs - cursorStartTS) * pixelsPerSecond) - x;

				gc.setFill(Color.RED);// ColorPalette.getInsightSecondaryColor(actID % 11));
				gc.fillRect(x, y, barWidth, barHeight);

				// System.out.println("x=" + x + " y =" + y + " barWidth =" + barWidth + " startTSInSec= " +
				// startTSInSec
				// + " endTSInSecs= " + endTSInSecs + " durationInMins = " + (endTSInSecs - startTSInSec) / 60.0
				// + " width= " + width);
				counter++;
			}
		}
	}

}