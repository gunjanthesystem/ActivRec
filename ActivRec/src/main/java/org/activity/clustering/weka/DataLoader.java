package org.activity.clustering.weka;

import org.activity.io.CSV2Arff;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Loads a CSV file by converting it into arff file and if class index is absent then last attribute is set as class
 * index
 * 
 * @author gunjan
 *
 */
public class DataLoader
{
	String outputArffFile;

	/**
	 * Loads a CSV file by converting it into arff file.
	 * 
	 * @param inputFilePath
	 * @param outputFilePath
	 * @param fieldSeparator
	 * @since 28 Feb 2019
	 */
	DataLoader(String inputFilePath, String outputFilePath, String fieldSeparator)
	{
		DataSource source;
		try
		{
			System.out.println(
					"Inside DataLoader:\ninputFilePath=" + inputFilePath + "\noutputFilePath=" + outputFilePath);
			// PopUps.showMessage("entering data loader");
			CSV2Arff convertor = new CSV2Arff(inputFilePath, outputFilePath, fieldSeparator);// "./SampleDatasets/Mine/Output2.arff");
			// convertor.
			this.outputArffFile = convertor.getOutPutFileName();

			source = new DataSource(outputArffFile);
			Instances data = source.getDataSet();
			// BufferedReader datafile = new BufferedReader(new FileReader(pathToDataFile));
			// Instances data = new Instances(datafile);
			//
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if (data.classIndex() == -1) data.setClassIndex(data.numAttributes() - 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Loads a CSV file by converting it into arff file.
	 * 
	 * @param pathToDataFile
	 */
	DataLoader(String inputFilePath, String outputFilePath)
	{
		DataSource source;
		try
		{
			System.out.println(
					"Inside DataLoader:\ninputFilePath=" + inputFilePath + "\noutputFilePath=" + outputFilePath);
			// PopUps.showMessage("entering data loader");
			CSV2Arff convertor = new CSV2Arff(inputFilePath, outputFilePath);// "./SampleDatasets/Mine/Output2.arff");
			// convertor.
			this.outputArffFile = convertor.getOutPutFileName();

			source = new DataSource(outputArffFile);
			Instances data = source.getDataSet();
			// BufferedReader datafile = new BufferedReader(new FileReader(pathToDataFile));
			// Instances data = new Instances(datafile);
			//
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if (data.classIndex() == -1) data.setClassIndex(data.numAttributes() - 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return the arff file name with the path
	 */
	public String getArffFileName()
	{
		return this.outputArffFile;
	}
}
