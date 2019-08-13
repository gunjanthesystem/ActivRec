package org.activity.clustering.weka;

import java.io.File;
import java.io.PrintStream;
import java.time.LocalDateTime;

import org.activity.io.WToFile;
import org.activity.ui.PopUps;

/**
 * Used for Debugging purposes
 * 
 * @author gunjan
 *
 */
public class Controller
{
	/**
	 * Used for Debugging purposes
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{

		DataLoader dl = new DataLoader("./SampleDatasets/Mine/Sample1.csv", null);

		String directoryToWrite = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/"
				+ LocalDateTime.now().getMonth().toString().substring(0, 3) + LocalDateTime.now().getDayOfMonth();
		new File(directoryToWrite).mkdir();
		String pathToWrite = directoryToWrite + "/";

		String outputArffFile = dl.getArffFileName();

		String typeOfClustering = "EMClustering";// EMClustering KMeans
		PrintStream consoleLogStream;
		try
		{
			consoleLogStream = WToFile.redirectConsoleOutput(pathToWrite + typeOfClustering + "ConsoleLog.txt");
			switch (typeOfClustering)
			{
			case "KMeans":
				KMeans kmeans = new KMeans(outputArffFile, pathToWrite + typeOfClustering + "Results", 3);
				break;
			case "EMClustering":
				EMClustering emClustering = new EMClustering(outputArffFile,
						pathToWrite + typeOfClustering + "Results");
				break;
			default:
				System.err.println("Unknown clustering type: " + typeOfClustering);
				PopUps.showError("Unknown clustering type: " + typeOfClustering);
			}
			consoleLogStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
