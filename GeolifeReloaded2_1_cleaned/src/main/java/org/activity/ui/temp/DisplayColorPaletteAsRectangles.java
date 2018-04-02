package org.activity.ui.temp;

import java.util.List;

import jViridis.ColorMap;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class DisplayColorPaletteAsRectangles extends Application
{
	public static void main(String[] args)
	{
		Application.launch(args);
	}

	public static Color awtColorToJavaFXColor(java.awt.Color awtColor)
	{
		return javafx.scene.paint.Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue(),
				(awtColor.getAlpha() / 255.0));
	}

	@Override
	public void start(Stage stage)
	{
		// x=0, y=0, width=100, height=50, fill=LIGHTGRAY, stroke=null

		String nameOfPallete = ColorMap.VIRIDIS;
		int numOfColors = 10;

		List<Rectangle> coloredRectsViridis = getColoredRectangles(numOfColors, nameOfPallete);
		List<Rectangle> coloredRectsViridis2 = getColoredRectangles(25, nameOfPallete);
		List<Rectangle> coloredRectsMagma = getColoredRectangles(numOfColors, ColorMap.MAGMA);
		List<Rectangle> coloredRectsMagma2 = getColoredRectangles(25, ColorMap.MAGMA);
		List<Rectangle> coloredRectsInferno = getColoredRectangles(numOfColors, ColorMap.INFERNO);
		List<Rectangle> coloredRectsInferno2 = getColoredRectangles(25, ColorMap.INFERNO);

		// List<HBox> hboxes = FXCollections.observableArrayList();

		HBox hbox1 = new HBox();
		hbox1.getChildren().addAll(coloredRectsViridis);

		HBox hbox2 = new HBox();
		hbox2.getChildren().addAll(coloredRectsViridis2);

		HBox hbox3 = new HBox();
		hbox3.getChildren().addAll(coloredRectsMagma);

		HBox hbox4 = new HBox();
		hbox4.getChildren().addAll(coloredRectsMagma2);

		HBox hbox5 = new HBox();
		hbox5.getChildren().addAll(coloredRectsInferno);

		HBox hbox6 = new HBox();
		hbox6.getChildren().addAll(coloredRectsInferno2);

		VBox vbox = new VBox();
		vbox.getChildren().add(hbox1);
		vbox.getChildren().add(hbox2);
		vbox.getChildren().add(hbox3);
		vbox.getChildren().add(hbox4);
		vbox.getChildren().add(hbox5);
		vbox.getChildren().add(hbox6);

		ScrollPane root = new ScrollPane();
		// root.getChildren().add(vbox);
		root.setContent(vbox);

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Showing Color Palette: " + nameOfPallete);
		stage.setWidth(1000);
		stage.setMinHeight(700);
		stage.setMinWidth(1000);
		stage.setHeight(600);
		stage.show();
	}

	/*
	 * 
	 */
	private List<Rectangle> getColoredRectangles(int numOfColors, String colorMapName)
	{
		ColorMap v = ColorMap.getInstance(colorMapName);// .show(this, 50, 30);
		System.out.println("v.getColorMapLength()= " + v.getColorMapLength());
		System.out.println("ColorMap =" + v.getColorMapName());

		java.awt.Color[] colorsInPallete = v.getColorPalette(numOfColors);
		int eachRectHeight = 200;
		int eachRectWidth = 50;
		List<Rectangle> coloredRects = FXCollections.observableArrayList();
		double xPos = 0, yPos = 0;

		int counter = 0;

		for (java.awt.Color c : colorsInPallete)
		{
			xPos += eachRectWidth;
			// yPos;//

			Rectangle coloredRectangle = new Rectangle(xPos, yPos, eachRectWidth, eachRectHeight);
			coloredRectangle.setFill(awtColorToJavaFXColor(c));
			coloredRectangle.setStroke(Color.BLACK);
			coloredRectangle.setArcWidth(10);
			coloredRectangle.setArcHeight(10);
			coloredRects.add(coloredRectangle);
			counter += 1;
		}
		return coloredRects;
	}
}