package org.activity.plotting0;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * 
 * Source:
 * https://raw.githubusercontent.com/Apress/learn-javafx-8/master/LearnJavaFX8/src/com/jdojo/image/ImageUtil.java
 *
 */
public class ImageUtil
{
	public static void saveToFile(Image image)
	{
		// Ask the user for the file name
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select an image file name");
		fileChooser.setInitialFileName("untitled");
		ExtensionFilter pngExt = new ExtensionFilter("PNG Files", "*.png");
		ExtensionFilter jpgExt = new ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg");
		fileChooser.getExtensionFilters().addAll(pngExt, jpgExt);

		File outputFile = fileChooser.showSaveDialog(null);
		if (outputFile == null)
		{
			return;
		}

		ExtensionFilter selectedExt = fileChooser.getSelectedExtensionFilter();
		String imageFormat = "png";
		if (selectedExt == jpgExt)
		{
			imageFormat = "jpg";
		}

		// Check for the file extension. Add oen, iff not specified
		String fileName = outputFile.getName().toLowerCase();
		switch (imageFormat)
		{
		case "jpg":
			if (!fileName.endsWith(".jpeg") && !fileName.endsWith(".jpg"))
			{
				outputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".jpg");
			}
			break;
		case "png":
			if (!fileName.endsWith(".png"))
			{
				outputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".png");
			}
		}

		// Convert the image to a buffered image
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

		// Save the image to the file
		try
		{
			ImageIO.write(bImage, imageFormat, outputFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
