package org.activity.plotting0;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ScaledSnapshot extends Application
{

	@Override
	public void start(Stage primaryStage)
	{

		final Text text = new Text("Sample Text");
		text.setFont(Font.font("This is Georgia", 272));
		final ImageView unscaled = new ImageView(text.snapshot(null, null));
		final ImageView scaled = createScaledView(text, 4);

		final GridPane root = new GridPane();
		root.setPadding(new Insets(8));
		root.setHgap(8);
		root.setVgap(8);

		root.add(new Label("Original"), 0, 0);
		root.add(text, 1, 0);
		root.add(createSizeLabel(text), 2, 0);

		root.add(new Label("Unscaled"), 0, 1);
		root.add(unscaled, 1, 1);
		root.add(createSizeLabel(unscaled), 2, 1);

		root.add(new Label("Scaled"), 0, 2);
		root.add(scaled, 1, 2);
		root.add(createSizeLabel(scaled), 2, 2);

		Node lc = LineChartApp2.createLineChart();
		SnapshotParameters snP = new SnapshotParameters();
		// snP.setViewport(viewport);
		final ImageView lcImage = new ImageView(lc.snapshot(null, null));
		lcImage.minWidth(1000);
		lcImage.setFitHeight(1000);
		lcImage.setFitWidth(1000);
		lcImage.minHeight(1000);

		System.out.println("lcImage.getViewport() = " + lcImage.getViewport());
		System.out.println("lcImage.getFitHeight() = " + lcImage.getFitHeight());
		root.add(new Label("Chart1"), 0, 3);
		root.add(lcImage, 1, 3);
		root.add(createSizeLabel(lcImage), 2, 3);

		primaryStage.setScene(new Scene(root));
		primaryStage.setTitle("Scale Snapshot");
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	private static ImageView createScaledView(Node node, int scale)
	{
		final Bounds bounds = node.getLayoutBounds();

		final WritableImage image = new WritableImage((int) Math.round(bounds.getWidth() * scale),
				(int) Math.round(bounds.getHeight() * scale));

		final SnapshotParameters spa = new SnapshotParameters();
		spa.setTransform(javafx.scene.transform.Transform.scale(scale, scale));

		final ImageView view = new ImageView(node.snapshot(spa, image));
		view.setFitWidth(bounds.getWidth());
		view.setFitHeight(bounds.getHeight());

		return view;
	}

	private static Label createSizeLabel(Node node)
	{
		double width, height;

		if (node instanceof ImageView)
		{
			final ImageView view = (ImageView) node;
			width = view.getImage().getWidth();
			height = view.getImage().getHeight();
		}
		else
		{
			width = node.getLayoutBounds().getWidth();
			height = node.getLayoutBounds().getHeight();
		}

		return new Label(String.format("%.2f x %.2f", width, height));
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
