package org.activity.plotting0;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class Plot1 extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			Group root = new Group();
			Scene scene = new Scene(root, 400, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setTitle("Hello World");

			Button btn = new Button();
			btn.setLayoutX(200);
			btn.setLayoutY(200);
			btn.setText("I am a Button");

			// btn.setOnAction(new EventHandler<ActionEvent>()
			// {
			// public void handle(ActionEvent event)
			// {
			// System.out.println("Button pressed");
			// }
			// }
			// );

			btn.setOnAction(event -> System.out.println("Button Pressed (lambda)"));

			root.getChildren().add(btn);

			primaryStage.setScene(scene);

			primaryStage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
