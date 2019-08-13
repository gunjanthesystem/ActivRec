package org.activity.ui.temp;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

/*
 * Canvas is normally not resizable but by overriding isResizable() and
 * binding its width and height to the width and height of the cell it will
 * automatically resize.
 */
class ActBoxCanvas extends Canvas
{

	// private List<Double> data = Collections.emptyList();
	double x, y, width, height;
	int actID;

	public ActBoxCanvas(double x, double y, double width, double height, int actID)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.actID = actID;

		/*
		 * Make sure the canvas draws its content again when its size changes.
		 */
		widthProperty().addListener(it -> draw());
		heightProperty().addListener(it -> draw());

		final Tooltip tooltip = new Tooltip();
		tooltip.setText("actID=" + actID);
		Tooltip.install(this, tooltip);
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

	public void setData(double x, double y, double width, double height, int actID)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.actID = actID;
	}

	/*
	 * Draw a chart based on the data provided by the model.
	 */
	void draw()
	{
		GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, getWidth(), getHeight());
		gc.setFill(Color.AQUA);
		gc.fillRect(x, y, width, height);

	}

}