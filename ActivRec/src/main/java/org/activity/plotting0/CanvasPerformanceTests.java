package org.activity.plotting0;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @see https://stackoverflow.com/a/44056730/230513
 */
public class CanvasPerformanceTests extends Application
{

	private static int W = 1600;
	private static int H = 1600;

	@Override
	public void start(Stage stage)
	{
		W = (int) Screen.getPrimary().getVisualBounds().getMaxX();
		H = (int) Screen.getPrimary().getVisualBounds().getMaxY();
		stage.setTitle("CanvasTaskTest");
		StackPane root = new StackPane();
		Canvas canvas = new Canvas(W, H);
		root.getChildren().add(canvas);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
		CanvasTask task = new CanvasTask();

		task.valueProperty()
				.addListener((ObservableValue<? extends Canvas> observable, Canvas oldValue, Canvas newValue) ->
					{
						root.getChildren().remove(oldValue);
						root.getChildren().add(newValue);
					});
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	private static class CanvasTask extends Task<Canvas>
	{

		private int strokeCount;

		@Override
		protected Canvas call() throws Exception
		{
			Canvas canvas = null;
			for (int i = 1; i < 50; i++)
			{
				canvas = new Canvas(W, H);
				GraphicsContext gc = canvas.getGraphicsContext2D();
				strokeCount = 0;
				long start = System.nanoTime();
				drawTree(gc, W / 2, H - 50, -Math.PI / 2, i);
				double dt = (System.nanoTime() - start) / 1_000d;
				gc.fillText("Depth: " + i + "; Strokes: " + strokeCount + "; Time : " + String.format("%1$07.1f", dt)
						+ " µs", 8, H - 8);
				Thread.sleep(200); // simulate rendering latency
				updateValue(canvas);
			}
			return canvas;
		}

		private void drawTree(GraphicsContext gc, int x1, int y1, double angle, int depth)
		{
			if (depth == 0)
			{
				return;
			}
			int x2 = x1 + (int) (Math.cos(angle) * depth * 5);
			int y2 = y1 + (int) (Math.sin(angle) * depth * 5);
			gc.strokeLine(x1, y1, x2, y2);
			strokeCount++;
			drawTree(gc, x2, y2, angle - Math.PI / 8, depth - 1);
			drawTree(gc, x2, y2, angle + Math.PI / 8, depth - 1);
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}

}