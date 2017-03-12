package org.activity.io;

import java.io.File;
import java.io.IOException;

import org.activity.clustering.weka.WekaUtilityBelt;

import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * Converts csv to arff file and writes the arff file.
 * 
 * @author gunjan
 *
 */
public class CSV2Arff
{
	String inputFileName, outputfileName;

	/**
	 * Converts csv to arff file and writes the arff file.
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 */
	public CSV2Arff(String inputFileName, String outputFileName)
	{
		this.inputFileName = inputFileName;
		this.outputfileName = outputFileName;
		// String inputFileName = "./SampleDatasets/Mine/Sample1.csv", outputFileName =
		// "./SampleDatasets/Mine/Sample1.arff";
		// load CSV
		CSVLoader loader = new CSVLoader();

		// PopUps.showMessage("entering csv loader with " + inputFileName + " and " + outputFileName);
		try
		{
			loader.setSource(new File(inputFileName));

			Instances data = loader.getDataSet();

			// save ARFF
			WekaUtilityBelt.writeArffAbsolute(data, outputFileName);
			// ArffSaver saver = new ArffSaver();
			// saver.setInstances(data);
			// saver.setFile(new File(outputFileName));
			// saver.setDestination(new File(outputFileName));
			// saver.writeBatch();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String args[])
	{
		String inputFileName = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/geolife1_JUN25/TimelineFeatureVectors.csv";
		// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/JUN25/TimelineFeatureVectors.csv";
		String oututFileName = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/geolife1_JUN25/TimelineFeatureVectors.arff";
		// /run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/JUN25/TimelineFeatureVectors.arff";
		new CSV2Arff(inputFileName, oututFileName);
	}

	/**
	 * 
	 * @return
	 */
	public String getOutPutFileName()
	{
		return this.outputfileName;
	}
}