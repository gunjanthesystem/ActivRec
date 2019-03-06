package org.activity.plotting;

import java.util.Collections;
import java.util.List;

import org.activity.ui.colors.ColorPalette;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;

/*
 * Canvas is normally not resizable but by overriding isResizable() and
 * binding its width and height to the width and height of the cell it will
 * automatically resize.
 */
class ResizableCanvasG1 extends Canvas
{
	private List<List<String>> data = Collections.emptyList();
	int indexOfStartTSInEachDataPoint;
	int indexOfEndTSInEachDataPoint;
	int indexOfActIDInEachDataPoint;

	public ResizableCanvasG1()
	{
		final Tooltip tooltip = new Tooltip();
		tooltip.setText("data.size()=" + data.size());
		Tooltip.install(this, tooltip);
		// pf.setTooltip(tooltip);
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

	public void setData(List<List<String>> data)
	{
		this.data = data;
		this.indexOfStartTSInEachDataPoint = 1;
		this.indexOfEndTSInEachDataPoint = 2;
		this.indexOfActIDInEachDataPoint = 5;
	}

	public void setData(List<List<String>> data, int indexOfStartTS, int indexOfEndTS, int indexOfActID)
	{
		this.data = data;
		this.indexOfStartTSInEachDataPoint = indexOfStartTS;
		this.indexOfEndTSInEachDataPoint = indexOfEndTS;
		this.indexOfActIDInEachDataPoint = indexOfActID;
	}

	/*
	 * Draw a chart based on the data provided by the model.
	 */
	void draw()
	{
		GraphicsContext gc = getGraphicsContext2D();
		double width = getWidth();
		double height = getHeight();

		gc.clearRect(0, 0, width, height);

		// Stop[] stops = new Stop[] { new Stop(0, Color.SKYBLUE), new Stop(1, Color.SKYBLUE.darker().darker()) };
		// LinearGradient gradient = new LinearGradient(0, 0, 0, 300, false, CycleMethod.NO_CYCLE, stops);
		// gc.setFill(gradient);
		double availableHeight = height * .5;
		double counter = 0;

		// Color emptyColor = Color.WHITE;

		double cursorStartTS = Integer.valueOf(data.get(0).get(indexOfStartTSInEachDataPoint));

		double barHeight = (availableHeight);// * 100) / 100;
		double y = (height - barHeight) / 2;

		int numOfDaysToShowInFullWidth = 10;
		int numOfSecsInFullWidth = numOfDaysToShowInFullWidth * 86400;// 24*60*60
		double pixelsPerSecond = width / numOfSecsInFullWidth;
		// System.out.println("pixelsPerSecond = " + pixelsPerSecond);

		for (List<String> actDetails : data)
		{
			// System.out.println("2-data.size()= " + data.size());
			double startTSInSec = Double.valueOf(actDetails.get(indexOfStartTSInEachDataPoint));
			double endTSInSecs = Double.valueOf(actDetails.get(indexOfEndTSInEachDataPoint));
			// if (cursorStartTS < startTS){}
			int actID = Integer.valueOf(actDetails.get(indexOfActIDInEachDataPoint));

			// double x = width / /* 365 */ counter;
			// double barWidth = width / 365 + 1;

			double x = (startTSInSec - cursorStartTS) * pixelsPerSecond;
			double barWidth = ((endTSInSecs - cursorStartTS) * pixelsPerSecond) - x;

			gc.setFill(ColorPalette.getInsightSecondaryColor(actID % 11));
			gc.fillRect(x, y, barWidth, barHeight);

			// System.out.println("x=" + x + " y =" + y + " barWidth =" + barWidth + " startTSInSec= " + startTSInSec
			// + " endTSInSecs= " + endTSInSecs + " durationInMins = " + (endTSInSecs - startTSInSec) / 60.0
			// + " width= " + width);
			counter++;
		}
	}

}