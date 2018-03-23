package org.activity.ui.temp;

import java.util.Collections;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/*
 * Canvas is normally not resizable but by overriding isResizable() and
 * binding its width and height to the width and height of the cell it will
 * automatically resize.
 */
class ResizableCanvas extends Canvas
{

	private List<Double> data = Collections.emptyList();

	public ResizableCanvas()
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

	public void setData(List<Double> data)
	{
		this.data = data;
	}

	/*
	 * Draw a chart based on the data provided by the model.
	 */
	void draw()
	{
		GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, getWidth(), getHeight());

		Stop[] stops = new Stop[] { new Stop(0, Color.SKYBLUE), new Stop(1, Color.SKYBLUE.darker().darker()) };
		LinearGradient gradient = new LinearGradient(0, 0, 0, 300, false, CycleMethod.NO_CYCLE, stops);

		gc.setFill(gradient);

		double availableHeight = getHeight() * .8;
		double counter = 0;
		for (Double value : data)
		{
			double x = getWidth() / 365 * counter;
			double barHeight = availableHeight * value / 100;
			double barWidth = getWidth() / 365 + 1;
			gc.fillRect(x, getHeight() - barHeight, barWidth, barHeight);
			counter++;
		}
	}

}