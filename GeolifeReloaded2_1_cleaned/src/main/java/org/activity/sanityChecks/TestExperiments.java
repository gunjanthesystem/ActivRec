package org.activity.sanityChecks;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.activity.distances.AlignmentBasedDistance;
import org.activity.io.ReadingFromFile;
import org.activity.objects.Pair;
import org.activity.stats.entropy.EntropyUtilityBelt;
import org.activity.stats.entropy.MultiscaleEntropy;
import org.activity.stats.entropy.SampleEntropyG;
import org.activity.stats.entropy.SampleEntropyIQM;
import org.activity.ui.PopUps;
import org.activity.util.Constant;
import org.activity.util.HjorthParameters;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;

//import org.apache.commons.collections.keyvalue.MultiKey;

public class TestExperiments
{
	
	public static void rankExp()
	{
		double[][] mat = new double[5][2];
		
		for (int i = 0; i < 5; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				mat[i][j] = i;
			}
		}
		
		KendallsCorrelation kt = new KendallsCorrelation();
		SpearmansCorrelation sp = new SpearmansCorrelation();
		
		double a1[] = { 1, 1, 2, 3, 2 };
		double a2[] = { 3, 2, 1, 1, 2 };
		
		System.out.println(kt.correlation(a1, a2));
		System.out.println(sp.correlation(a1, a2));
		
	}
	
	public static void checkEditDistance()
	{
		// Constant.setDatabaseName("dcu_data_2");
		// Constant.verboseLevenstein = true;
		// new AlignmentBasedDistance().getMySimpleLevenshteinDistance("AAB", "AAS", 1, 1, 2);
		// // new AlignmentBasedDistance().getWeightedLevenshteinDistance("AAB", "AAS", 1, 1, 2);
		Constant.verboseLevenstein = true;
		
		System.out.println(new AlignmentBasedDistance().getMySimpleLevenshteinDistance("gunjankumar", "manaligaur", 1, 1, 2));
		System.out.println(AlignmentBasedDistance.getMySimpleLevenshteinDistanceWithoutTrace("gunjankumar", "manaligaur", 1, 1, 2));
		// new AlignmentBasedDistance().getSimpleLevenshteinDistance("AAB", "AAS", 1, 1, 2);
	}
	
	/**
	 * Returns the median of the int values of the string.
	 * 
	 * @param s
	 * @return
	 */
	public static double getMedian(String s)
	{
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			System.out.print(c + " ");
			System.out.println((double) c);
			stats.addValue(c);
		}
		
		double median = stats.getPercentile(50);
		System.out.println("median = " + median);
		
		return 0;
	}
	
	public static double getMedian2(String s)
	{
		
		double[] arr = new double[s.length()];
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			System.out.print(c + " ");
			System.out.println((double) c);
			arr[i] = (double) c;
		}
		DescriptiveStatistics stats = new DescriptiveStatistics(arr);
		double median = stats.getPercentile(50);
		System.out.println("median = " + median);
		
		return 0;
	}
	
	public static double getMedian(double[] vals)
	{
		if (vals.length == 1)
			return vals[0];
		
		double median = new DescriptiveStatistics(vals).getPercentile(50);
		
		System.out.println(" vals are: " + Arrays.toString(vals) + " median= " + median);
		
		if (median == Double.NaN)
		{
			PopUps.showError("NAN Error" + (" vals are: " + Arrays.toString(vals) + " median= " + median));
		}
		
		return median;
	}
	
	public static double getMedian3(String s)
	{
		
		double[] arr = new double[s.length()];
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			System.out.print(c + " ");
			System.out.println((double) c);
			arr[i] = (double) c;
		}
		DescriptiveStatistics stats = new DescriptiveStatistics(arr);
		double median = stats.getPercentile(50);
		System.out.println("median = " + new DescriptiveStatistics(arr).getPercentile(50));
		
		double a[] = { 3610, 4563 };
		System.out.println("median = " + getMedian(a));
		
		return 0;
	}
	
	public static void checkPair()
	{
		Pair<String, String> p1 = new Pair("gunjan", "manali");
		Pair<String, String> p2 = new Pair("manali", "gunjan");
		Pair<String, String> p3 = new Pair("manali", "manali");
		
		System.out.println("p1:" + p1.toString() + ",p2:" + p2.toString() + "  equals:" + p1.equals(p2) + "  ignore order:"
				+ p1.equalsIgnoreOrder(p2));
		System.out.println("p1:" + p1.toString() + ",p3:" + p3.toString() + "  equals:" + p1.equals(p3) + "  ignore order:"
				+ p1.equalsIgnoreOrder(p3));
		System.out.println("p3:" + p3.toString() + ",p3:" + p3.toString() + "  equals:" + p3.equals(p3) + "  ignore order:"
				+ p3.equalsIgnoreOrder(p3));
		System.out.println(p1.equals(p2) + "  ignore order:" + p1.equalsIgnoreOrder(p2));
	}
	
	public static void checkSet()
	{
		LinkedHashSet<String> s1 = new LinkedHashSet<String>();
		s1.add("gunjan");
		s1.add("ajooba");
		s1.add("ajooba");
		s1.add("Ouch");
		
		LinkedHashSet<String> s2 = new LinkedHashSet<String>();
		s2.add("Ouch");
		s2.add("gunjan");
		s2.add("ajooba");
		s2.add("ajooba");
		s2.add("ajooba");
		s2.add("ajooba");
		
		System.out.println(s1.equals(s2));
		
		// MultiKey m1= new MultiKey({"gunjan","gunjan"});
	}
	
	public static void testRJava()
	{
		// new R-engine
		// Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
		// if (!re.waitForR())
		// {
		// System.out.println("Cannot load R");
		// return;
		// }
		//
		// // print a random number from uniform distribution
		// System.out.println(re.eval("runif(1)").asDouble());
		//
		// // done...
		// re.end();
		
	}
	
	public static double[] getTimeSeriesVals(String filePath)
	{
		List<Double> vals = ReadingFromFile.oneColumnReaderDouble(filePath, ",", 1, false);
		Double[] vals2 = vals.toArray(new Double[vals.size()]);
		double[] vals3 = ArrayUtils.toPrimitive(vals2);
		return vals3;
	}
	
	public static double[] getRandomSeries(int size)
	{
		double vArray[] = new double[size];
		for (int i = 0; i < size; i++)
		{
			Random rn = new Random();
			double gg = 0 + rn.nextInt(100 - 0 + 1);
			vArray[i] = gg;
		}
		return vArray;
	}
	
	// public static void CSampleEntropy(int ll, double r, double sd, int scaleFactor)
	// {
	// double vArray[] = getRandomSeries(200);
	//
	// int M_MAX = 40, m_maxG = 4;
	// int i, k, l, numOfSegmentsMinusOne;
	// int cont[] = new int[M_MAX + 1];
	// double tolerance = 0;
	//
	// numOfSegmentsMinusOne = (vArray.length / scaleFactor) - m_maxG;
	// tolerance = r * sd;
	//
	// for (i = 0; i < M_MAX; i++)
	// cont[i] = 0;
	//
	// for (i = 0; i < numOfSegmentsMinusOne; ++i)
	// {
	// for (l = i + 1; l < numOfSegmentsMinusOne; ++l)
	// { /* self-matches are not counted */
	// k = 0;
	// while (k < m_maxG && fabs(coarsedAllDataPointsG[i + k] - coarsedAllDataPointsG[l + k]) <= tolerance)
	// {
	// cont[++k]++;
	// }
	// if (k == m_maxG && fabs(coarsedAllDataPointsG[i + m_maxG] - coarsedAllDataPointsG[l + m_maxG]) <= tolerance)
	// {
	// cont[m_maxG + 1]++;
	// }
	// }
	// }
	//
	// for (i = 1; i <= m_maxG; i++)
	// if (cont[i + 1] == 0 || cont[i] == 0)
	// SE_G[ll][c_G][scaleFactor][i] = -log((double) 1 / ((numOfSegmentsMinusOne) * (numOfSegmentsMinusOne - 1)));
	// else
	// SE_G[ll][c_G][scaleFactor][i] = -log((double) cont[i + 1] / cont[i]);
	// }
	
	public static double[] getNaiveSeries(int size)
	{
		double vArray[] = new double[size];
		for (int i = 0; i < size; i++)
		{
			vArray[i] = i + 1;
		}
		return vArray;
	}
	
	public static double[] getPureRegularSeries(int size)
	{
		double vArray[] = new double[size];
		
		for (int i = 0; i < size;)
		{
			for (int j = 10; j <= 40;)
			{
				vArray[i] = j;
				j += 10;
				i++;
			}
		}
		
		return vArray;
	}
	
	public static Vector<Double> arrayToVector(double[] arr)
	{
		Vector<Double> vec = new Vector<Double>();
		for (double d : arr)
		{
			vec.add(d);
		}
		
		return vec;
	}
	
	public static void sampleEntropyOnSymbolicSeqExperiment(String timeSeriesFile)
	{
		// String timeSeriesFile =
		// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/SampEnExperiments/10ActivityNameSequenceIntInvalidsExpungedDummyTimeTimes10.csv";
		
		int m = 2;
		
		double[] valsTS = getTimeSeriesVals(timeSeriesFile);// getPureRegularSeries(400);//
		double sd = getSD(valsTS);
		
		System.out.print("Input data:" + timeSeriesFile + " \n\t");
		for (double v : valsTS)
		{
			System.out.print(v + " ,");
		}
		
		double sampleEntropyGunjan = SampleEntropyG.getSampleEntropyG(valsTS, m, 0);// getSampleEntropy(input, m +
																					// i, tolerance);
		System.out.printf("\nSample Entropy gunjan = %f\n\n", sampleEntropyGunjan);// System.out.printf("dexp: %f\n", dexp);
		// sampleEntropies2[0]);
		
	}
	
	public static void sampleEntropyExperiments()
	{
		String timeSeriesFile =
				"/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/TimeSeries2/geolife1JUL6TimeSeriesAnalysis_UsersAbove10RTs/testy5.csv";
		double r = 0.2;// 00001;
		// int mMax = 2;
		int maxScale = 2;
		int m = 2;
		int mMax = m;
		
		double[] valsTS = getTimeSeriesVals(timeSeriesFile);// getPureRegularSeries(400);//
		double sd = getSD(valsTS);
		
		System.out.print("Input data:" + timeSeriesFile + " \n\t");
		for (double v : valsTS)
		{
			System.out.print(v + " ,");
		}
		
		double sampleEntropyIQM = new SampleEntropyIQM().calcSampleEntropy(arrayToVector(valsTS), m, r, 1);
		double sampleEntropyGunjan = SampleEntropyG.getSampleEntropyG(valsTS, m, r * sd);// getSampleEntropy(input, m +
																							// i, tolerance);
		double[] sampleEntropies2 = EntropyUtilityBelt.getSampleEntropies(valsTS, r, sd, mMax);
		
		System.out.println(" r *sd = " + r * sd);
		
		System.out.printf("\nSample Entropy IQM = %f", sampleEntropyIQM);// System.out.printf("dexp: %f\n", dexp);
		System.out.printf("\nSample Entropy gunjan = %f", sampleEntropyGunjan);// System.out.printf("dexp: %f\n", dexp);
		System.out.println("\nSample Entropies Timo:");// , size = " + sampleEntropies2.length + " first value = " +
														// sampleEntropies2[0]);
		
		for (int k1 = 0; k1 < sampleEntropies2.length; k1++)
		{
			System.out.println("\t[m= " + (k1 + 1) + "] =" + sampleEntropies2[k1] + "  ");
		}
		
		System.out.println("\nMSE Analysis: (scale factor, m)");
		MultiscaleEntropy mse = new MultiscaleEntropy(valsTS, r, maxScale, mMax);
		double[][] mseResults = mse.getMSE();
		for (int row = 0; row < mseResults.length; row++)
		{
			for (int col = 0; col < mseResults[row].length; col++)
			{
				System.out.println("\t(scale= " + (row + 1) + ", m=" + (col + 1) + ") = " + (mseResults[row][col]));
			}
		}
	}
	
	public static double getSD(double[] vals)
	{
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		double sd = ds.getStandardDeviation();
		return ds.getStandardDeviation();
	}
	
	public static void executeApacheExec()
	{
		String line = "runmse \"/home/gunjan/cprograms/Testy1\" \"/home/gunjan/cprograms/delete7.mse\"";// mseG -m 1 -M
																										// 12 -b 1 -r
																										// 0.2 -R 0.2 -n
																										// 2
																										// <\"/home/gunjan/cprograms/Testy1\">
																										// \"/home/gunjan/cprograms/delete14.mse\"";
		CommandLine cmdLine = CommandLine.parse(line);
		System.out.println("Command line as string: " + cmdLine);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(1);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		try
		{
			int exitValue = executor.execute(cmdLine);
		}
		catch (ExecuteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void checkMatlabControl() throws MatlabConnectionException, MatlabInvocationException
	{
		// Create a proxy, which we will use to control MATLAB
		MatlabProxyFactory factory = new MatlabProxyFactory();
		MatlabProxy proxy = factory.getProxy();
		
		// Display 'hello world' just like when using the demo
		// proxy.eval("disp('hello world')");
		
		// Set a variable, add to it, retrieve it, and print the result
		proxy.setVariable("a", 5);
		proxy.eval("a = a + 6");
		double result = ((double[]) proxy.getVariable("a"))[0];
		System.out.println("Result: " + result);
		
		// Disconnect the proxy from MATLAB
		proxy.disconnect();
	}
	
	public static void checkHjorthParameters()
	{
		String timeSeriesFile =
				"/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/TimeSeries2/geolife1JUL6TimeSeriesAnalysis_UsersAbove10RTs/testy5.csv";
		double[] valsTS = getTimeSeriesVals(timeSeriesFile);// getPureRegularSeries(400);//
		
		HjorthParameters hp = new HjorthParameters(valsTS);
		
		System.out.println(" HjorthParameters --- ");
		System.out.println("Activity = " + hp.getActivity());
		System.out.println("Mobility = " + hp.getMobility());
		System.out.println("Complexity = " + hp.getComplexity());
		
		double sampleEntropyGunjan = SampleEntropyG.getSampleEntropyG(valsTS, 2, 0.02 * getSD(valsTS));// getSampleEntropy(input,
																										// m + i,
																										// tolerance);
		System.out.printf("\nSample Entropy gunjan = %f", sampleEntropyGunjan);// System.out.printf("dexp: %f\n", dexp);
	}
	
	public static void main(String[] args)
	{
		try
		{
			// String a = "Usrer8";
			//
			// a = a.replaceAll("[^0-9]", "");
			// String a = "FirstAjooba";// FirstCluster__SecondCluster__";
			//
			// String splitted[] = a.split("__");
			//
			// for (int i = 0; i < splitted.length; i++)
			// {
			// System.out.println(splitted[i] + "..");
			// }
			// BufferedWriter a = WritingToFile.getBufferedWriterForNewFile("temp1");
			// BufferedWriter b = WritingToFile.getBufferedWriterForNewFile("temp1");
			//
			// Arrays.asList(a, b).stream().close();
			//
			// System.out.println("ajoobaaaaaa");
			// Integer k = null;
			try
			{
				// WritingToFile.writeToNewFile("field1,field2,field3\n", "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Temp/temp.csv");
				// WritingToFile.appendLineToFileAbsolute("test,test,test", "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Temp/temp.csv");
				// double[][] arr = { { 1, 2, 5 }, { 3, 4, 5 } };
				// System.out.println(arr.length * arr[0].length);
				// checkMatlabControl();
				// checkHjorthParameters();
				// SampleEntropyG se = new SampleEntropyG();
				// executeApacheExec();
				sampleEntropyOnSymbolicSeqExperiment("/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/SampEnExperiments/"
						+ "10ActivityNameSequenceIntInvalidsExpungedDummyTimeTimes1.csv");
				sampleEntropyOnSymbolicSeqExperiment("/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/SampEnExperiments/"
						+ "10ActivityNameSequenceIntInvalidsExpungedDummyTimeTimes10.csv");
				sampleEntropyOnSymbolicSeqExperiment("/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/SampEnExperiments/"
						+ "10ActivityNameSequenceIntInvalidsExpungedDummyTimeTimes100.csv");
				sampleEntropyOnSymbolicSeqExperiment("/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/SampEnExperiments/"
						+ "10ActivityNameSequenceIntInvalidsExpungedDummyTimeNewSymbols.csv");
				
				// sampleEntropyExperiments();
				// SampleEntropyG se = new SampleEntropyG();
				// sampleEntropyExperiments();
				// System.out.println(UtilityBelt
				// .executeShellCommand("mseG -m 1 -M 12 -b 1 -r 0.000001 -R 0.000001 -n 1 </home/gunjan/cprograms/Testy1>/home/gunjan/cprograms/delete1_3.mse"));
				// System.out.println(UtilityBelt.executeShellCommand("mseG -m 1 -M 12 -b 1 -r 0.000001 -R 0.000001 -n 1 <Testy1>delete1_3.mse"));//
				// /home/gunjan/cprograms"));
				// System.out.println(UtilityBelt.executeShellCommand("ls") + "\n\n");// /home/gunjan/cprograms"));
				
				// System.out.println(UtilityBelt.executeShellCommand2("ls"));// /home/gunjan/cprograms"));
				// System.out.println(UtilityBelt.executeShellCommand("traker2"));
				// System.out.println("\n" + UtilityBelt.executeShellCommand("pwd"));
				// System.out.println(UtilityBelt.executeShellCommand("ls -l"));
				
				// System.out.println(UtilityBelt
				// .executeShellCommand4("mseG -m 1 -M 12 -b 1 -r 0.000001 -R 0.000001 -n 1 </home/gunjan/cprograms/Testy1>/home/gunjan/cprograms/delete1_4.mse"));
				// System.out.println(UtilityBelt.executeShellCommand4("mseG -m 1 -M 12 -b 1 -r 0.000001 -R 0.000001 -n 1 <Testy1>delete1_4.mse"));
				// UtilityBelt.getShannonEntropy("abcabcabc");
				// UtilityBelt.getShannonEntropy("aaabbbccc");
				// SampleEntropy sampleEntropy = new SampleEntropy();
				//
				// Vector<Double> v = new Vector<Double>();
				//
				// for (int i = 0; i < 397; i++)
				// {
				// Random rn = new Random();
				// double gg = 0 + rn.nextInt(100 - 0 + 1);
				// v.addElement(new Double(gg));
				// }
				//
				// double vArray[] = new double[v.size()];
				//
				// for (int i = 0; i < v.size(); i++)
				// {
				// vArray[i] = v.get(i);
				// System.out.println(v.get(i) + " -- " + vArray[i]);
				// }
				//
				// DescriptiveStatistics ds = new DescriptiveStatistics(vArray);
				// double sd = ds.getStandardDeviation();
				// System.out.println("Standard deviation = " + ds.getStandardDeviation());
				//
				// double sEntropy = sampleEntropy.calcSampleEntropy(v, 2, 0.20 * sd, 1);
				//
				// double[] sampleEntropies2 = EntropyUtilityBelt.getSampleEntropies(vArray, 0.2, sd, 2);
				// System.out.println("Sample Entropy = " + sEntropy);
				// System.out.println("Sample Entropy 2, size = " + sampleEntropies2.length + " first value = " +
				// sampleEntropies2[0]);
				//
				// for (int k1 = 0; k1 < sampleEntropies2.length; k1++)
				// {
				// System.out.println("[" + k1 + "] =" + sampleEntropies2[k1] + " ");
				// }
				// testRJava();
				// /////////////
				
				// UtilityBelt.assertEquals(k, 2);
				// checkEditDistance();
				// checkPair();
				// double ldouble = 0.000000000000111112222750;
				
				// System.out.println((new BigDecimal(Double.toString(ldouble))).toPlainString());
				// System.out.println(TimelineStats.getNGramFrequencyDistributionTest("abcabcabc", 2));
				// System.out.println("-------------------");
				// System.out.println(TimelineStats.getNGramOccurrenceDistributionWithIndices("abcabcabc", 2));
				// String a = String.valueOf(2);
				// String b = String.valueOf(2);
				// System.out.println(UtilityBelt.assertEquals(2, 2));
				
				// for (int i = 5; i < 15; i++)
				// {
				// for (int j = i + 1; j < 7; j++)
				// {
				// System.out.println(" i= " + i + " j= " + j);
				// }
				// }
				// checkSet();
				// System.out.print(UtilityBelt.assertEquals(new Integer(1), new Integer(2)));
				// System.out.print assert (1 == 1);
				// rankExp();
				// double number;
				//
				// // System.out.println("number is " + number);
				//
				// HashMap<Pair<Integer, Integer>, ArrayList<String>> map = new HashMap<Pair<Integer, Integer>,
				// ArrayList<String>>();
				//
				// ArrayList<String> names = new ArrayList<String>();
				// names.add("gunjan");
				//
				// map.put(new Pair(12, 24), names);
				//
				// if (map.containsKey(new Pair(12, 24)))
				// {
				// System.out.println("Already there");
				// }
				// else
				// {
				// System.out.println("Not there");
				// }
				// // System.out.println(UtilityBelt.executeShellCommand("fortune"));
				// checkEditDistance();
				// // getMedian("abcdef");
				// // getMedian2("abcdef");
				// getMedian3("abcdef");
				// // checkSAX();
				// Pair<Double, Double> a = new Pair<Double, Double>(21.34, 32.45);
				// Object k = (Object) a;
				// Pair<Double, Double> d = (Pair) (k);
				// System.out.println(k);
				// System.out.println(d);
				// System.out.println(UtilityBelt.getShannonEntropy("gunjan"));
				// System.out.println(UtilityBelt.getShannonEntropy("gunjangunjangunjangunjangunjangunjangunjanmanalimanalimanalimanalimanalimanalimanalimanalimanalimanalimanali"));
				// //
				// long a = 12;
				// long b = 17;
				// System.out.println(GunjanUtils.getCompactHilbertCurveIndex(12, 55));
				// // ConnectDatabase.truncateAllData("geolife1");
				// // String number = "123.34344456666";
				// // System.out.println(UtilityBelt.round(number, 6));
				// // System.out.println("String representation = " + gunjanSAX.SAXUtilityBelt.getSAXString(vals,
				// stamps, 10));
				// // ArrayList<String> arr = new ArrayList<String>();
				// //
				// // arr.add("1000.00");
				// // arr.add("2000.50");
				// // arr.add("2000.80");
				// // arr.add("8000.10");
				// //
				// // System.out.println(UtilityBelt.averageDecimalsAsStrings(arr));
				// {
				// int num = 12;
				// }
				// int num = 24;
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
			// String ajooba = "Gunjan";
			//
			// String returned = change(ajooba);
			//
			// System.out.println("ajooba=" + ajooba);
			// System.out.println("returned=" + returned);
			// System.out.println("ajooba=" + ajooba);
			
			// System.out.println(com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
			// System.out.println(String.format("%03d",2));
			//
			// System.out.println(UtilityBelt.ceilNearestMultipleOf(126.3,50));
			
			// Timestamp a = new Timestamp(2014, 4, 4, 4, 4, 4, 0);
			//
			// Timestamp a2 = new Timestamp(2014, 4, 4, 4, 4, 24, 0);
			//
			// long t = 20;
			// System.out.println(UtilityBelt.isSameTimeInTolerance(a, a2, t));
			// 53.31871 -6.27484
			//
			// System.out.println(UtilityBelt.isValidActivityName("Not Available"));
			// System.out.println(UtilityBelt.isValidActivityName("Unknown"));
			// System.out.println(UtilityBelt.isValidActivityName(new String("Not Available").trim()));
			// double homeLat= 53.31871;
			// double homeLon= -6.27484;
			//
			// double ucdLat= 53.30956;
			// double ucdLon= -6.21862;
			//
			//
			// // System.out.println(UtilityBelt.haversine(homeLat, homeLon, ucdLat, ucdLon)+"kms");
			// //3.8719445514425677kms
			// System.out.println(UtilityBelt.haversine(39.895078, 116.368533, 39.940934, 116.347075)+"kms");
			// //5.418928813530423kms
			//
			//
			// System
			// for(long a=1;a<999999;a++ )
			// {
			// for(long b=1;b<999999;b++ )
			// {
			// for(long c=1;c<999999;c++ )
			// {
			// for(long d=1;d<999999;d++ )
			// {
			// System.out.println("aa "+a+" "+b+" "+c+" "+d);
			// }
			// }
			// }
			// }
			
			// double lat = 99.142777;
			//
			// ArrayList<BigDecimal> arr= new ArrayList<BigDecimal> ();
			//
			// BigDecimal a = new BigDecimal("-12.45");
			// arr.add(a);
			//
			// BigDecimal b = new BigDecimal("-12.45");
			// arr.add(b);
			// userAllDatesTimeslines
			//
			// BigDecimal c = new BigDecimal("1.1");
			// arr.add(c);
			//
			// BigDecimal d = new BigDecimal("3.22");
			// arr.add(d);
			//
			//
			// System.out.println(UtilityBelt.average(arr));
			//
			//
			// ArrayList<String> arr2= new ArrayList<String> ();
			//
			// String a2 = new String("-12.45");
			// arr2.add(a2);
			//
			// String b2 = new String("-12.45");
			// arr2.add(b2);
			//
			//
			// String c2 = new String("1.1");
			// arr2.add(c2);
			//
			// String d2 = new String("3.22");
			// arr2.add(d2);
			//
			// System.out.println(UtilityBelt.averageDecimalsAsStrings(arr2));
			//
			
			// System.out.println(new BigDecimal(lat));//,new MathContext(2)));
			// System.out.println(new BigDecimal("-9.142777").add(new BigDecimal("1.5")));
			// System.out.println(new BigDecimal("0"));//,new
			// System.out.println(BigDecimal.ZERO);//,new
			// test2();
			// String s=new
			// String("LACACACACACACACACACACACACACACACACACACAJABABCACACACJCACACACACACACACACACACACACACACAJABACAL");
			// String s2=new String("GUNJANCACACACAJgBACAL");
			//
			// EditSimilarity ed= new EditSimilarity();
			// long t1=System.currentTimeMillis();
			//
			// for(int i=0;i<200000;i++)
			// {
			// Pair<String,Double> res= ed.mySimpleLevenshteinDistance(s, s2);
			// System.out.println("res="+res.toString());
			// }
			//
			//
			// long t2=System.currentTimeMillis();
			//
			//
			// System.out.println("Time taken="+(t2-t1)/1000);
			// // System.out.println(s.length());
			// System.out.println(s.substring(85, 88));
			// //PopUps.showMessage("Program Execution Finished");
			// System.out.println(ConnectDatabase.getActivityID("Unknown"));
			// System.out.println(ConnectDatabase.getActivityName(11));
			
			// System.out.println(isMinimum(2,1,2,3));
			/*
			 * ArrayList<String> dimensionNames=ConnectDatabase.getDimensionNames();
			 * 
			 * 
			 * for(String dimensionName: dimensionNames) { System.out.println(dimensionName); }
			 */
			
			/*
			 * System.out.println(LevenshteinDistance("gunjan","gunjk"));
			 * 
			 * 
			 * System.out.println(LD("gunjan","gunjk"));
			 */// System.out.println(" string utils="+StringUtils.getLevenshteinDistance("axwb", "ab"));
			
			// System.out.println(minDistance("axwb","ab"));
			/* System.out.println(recursiveLevenshteinDistance("gunjan",6,"gunjk",5)); */
			
			// ///////Testing the breaking of activities spanning over days into those contained in single day
			/*
			 * Timestamp ts= new Timestamp(2014-1900,3,13,14,9,6,0); long durationInSeconds = 12*24*60*60; TreeMap<Timestamp,Long>
			 * map=DatabaseCreatorDCU.breakActivityEventOverDays(ts, durationInSeconds);
			 * 
			 * for (Map.Entry<Timestamp, Long> entry : map.entrySet()) { System.out.println("Start date="+entry.getKey()+" Duration in seconds:"+entry.getValue());
			 * 
			 * }
			 */// //////////////////
				//
				// Duration a= Duration.ofMinutes(30);
				// System.out.println(a.toHours());
			
		}
		
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String change(String s)
	{
		// s = "manali";
		try
		{
			throw new Exception("Error:Holy Christ!");
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return s.replace('u', 'm');
	}
	
	public static void checkSAX()
	{
		Timestamp a = new Timestamp(2014, 4, 4, 4, 4, 4, 0);
		
		double vals[] = { 62728.0, 63089.0, 63498.0, 63716.0, 63936.0, 65023.0, 64471.0, 65459.0, 65768.0, 36136.0, 36553.0, 36882.0 };// { 12, 14, 23.5, 22, 4 };
		long stamps[] = new long[12];
		
		System.out.println("Timeseries:");
		
		for (int i = 0; i < 12; i++)
		{
			stamps[i] = a.getTime() + i * 60 * 1000;
		}
		
		for (int i = 0; i < 12; i++)
		{
			System.out.println(vals[i] + ":" + stamps[i]);
		}
		
		System.out.println("///////////");
		
		try
		{
			// Timeseries ts = new Timeseries(vals, stamps);
			//
			// String resultant = SAXFactory.ts2string(ts, 5, new NormalAlphabet(), 10);
			//
			
			// System.out.println("String representation = " + gunjanSAX.SAXUtilityBelt.getSAXString(vals, stamps, 5,
			// 10));
			System.out.println("String representation = " + org.activity.util.SAXUtils.getSAXString(vals, stamps, 12, 20));
			
			// System.out.println(Double.MAX_VALUE);
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void test2()
	{
		String trace = "_D(1-0)_D(2-0)_D(3-0)_D(4-0)_N(5-1)_N(16-2)";
		String[] splitted = trace.split("_");
		double dAct = 0, dFeat = 0;
		
		// for(int i=1;i<splitted.length;i++)
		// {
		// String op= splitted[i];
		// String[] splitOps= op.split("\\(");
		// System.out.println(splitted[i]);
		// System.out.println(splitOps[0]);
		// System.out.println(splitOps[1].charAt(0));
		// System.out.println(splitOps[1].charAt(2));
		// // if(splitOps[0].equals('D'))
		// // {
		// // costL1 += 1; //1d*costReplaceFullActivityObject;
		// // }
		// //
		// // else if(splitOps[0].equals('I'))
		// // {
		// // costL1 += 1; //1d*costReplaceFullActivityObject;
		// // }
		// //
		// // else if(splitOps[0].equals('S'))
		// // {
		// // costL1 += 2; //2d*costReplaceFullActivityObject;
		// // }
		// //
		// // else if(splitOps[0].equals('N'))
		// // {
		// // // String corrds= splitOps[0]
		// // //costL2+=
		// // }
		// }
		//
		for (int i = 1; i < splitted.length; i++)
		{
			String op = splitted[i];
			String[] splitOps = op.split("\\(");
			
			System.out.println(splitted[i]); // D(1-0)
			
			System.out.println(splitOps[0]); // D
			System.out.println(splitOps[1].charAt(0));// 1
			System.out.println(splitOps[1].charAt(2));// 0
			
			String splitCo[] = splitOps[1].split("-");
			System.out.println(splitCo[0]);// 1
			System.out.println(splitCo[1]);// 0
			
			String splitCoAgain[] = splitCo[1].split("\\)");
			System.out.println(splitCoAgain[0]);// 0
			if (splitOps[0].equals('D'))
			{
				dAct += 1; // 1d*costReplaceFullActivityObject;
			}
			
			else if (splitOps[0].equals('I'))
			{
				dAct += 1; // 1d*costReplaceFullActivityObject;
			}
			
			else if (splitOps[0].equals('S'))
			{
				dAct += 2; // 2d*costReplaceFullActivityObject;
			}
			
			else if (splitOps[0].equals('N'))
			{
				String corrds = splitOps[0];
				
				int coordOfAO1 = Character.getNumericValue(splitOps[1].charAt(0));
				int coordOfAO2 = Character.getNumericValue(splitOps[1].charAt(2));
				
				dFeat += 1.5;// getDFeat(activityObjects1.get(coordOfAO1),activityObjects2.get(coordOfAO2));
				
			}
		}
		
	}
	
	public static double numMinDistance(String word1, String word2) // http://www.programcreek.com/2013/12/edit-distance-in-java/
	{
		double costLevelOne = 0d;
		
		int len1 = word1.length();
		int len2 = word2.length();
		
		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];
		
		for (int i = 0; i <= len1; i++)
		{
			dp[i][0] = i;
		}
		
		for (int j = 0; j <= len2; j++)
		{
			dp[0][j] = j;
		}
		
		// iterate though, and check last char
		int numOfInsertions = 0, numOfDeletions = 0, numOfReplacements = 0;
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);
				
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				}
				else
				{
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;
					
					int min = -99999;
					if (isMinimum(replace, replace, insert, delete))
					{
						min = replace;
						numOfReplacements++;
					}
					
					else if (isMinimum(insert, replace, insert, delete))
					{
						min = insert;
						numOfInsertions++;
					}
					else if (isMinimum(delete, replace, insert, delete))
					{
						min = delete;
						numOfDeletions++;
					}
					
					dp[i + 1][j + 1] = min;
				}
			}
		}
		
		System.out.println("\n-----Matrix for edit distance calculation---------");
		
		for (int i = 0; i <= len1; i++)
		{
			for (int j = 0; j <= len2; j++)
			{
				System.out.print(" " + dp[i][j]);
			}
			System.out.println();
		}
		System.out.println("-------------");
		
		System.out.println("Number of insertions =" + numOfInsertions);
		System.out.println("Number of deletions =" + numOfDeletions);
		System.out.println("Number of replacements =" + numOfReplacements);
		
		System.out.println("Cost at level one=" + costLevelOne);
		System.out.println("dp[len1][len2]=" + dp[len1][len2]);
		
		return costLevelOne;
	}
	
	public static boolean isMinimum(double tocheck, double a, double b, double c)
	{
		return tocheck == Math.min(Math.min(a, b), c);
	}
	
	public static int LevenshteinDistance(String s0, String s1)
	{
		int len0 = s0.length() + 1;
		int len1 = s1.length() + 1;
		
		// the array of distances
		int[] cost = new int[len0];
		int[] newcost = new int[len0];
		
		// initial cost of skipping prefix in String s0
		for (int i = 0; i < len0; i++)
			cost[i] = i;
		
		// dynamicaly computing the array of distances
		
		// transformation cost for each letter in s1
		for (int j = 1; j < len1; j++)
		{
			// initial cost of skipping prefix in String s1
			newcost[0] = j - 1;
			
			// transformation cost for each letter in s0
			for (int i = 1; i < len0; i++)
			{
				
				// matching current letters in both strings
				int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;
				
				// computing cost for each transformation
				int cost_replace = cost[i - 1] + match;
				int cost_insert = cost[i] + 1;
				int cost_delete = newcost[i - 1] + 1;
				
				// keep minimum cost
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
			}
			
			// swap cost/newcost arrays
			int[] swap = cost;
			cost = newcost;
			newcost = swap;
		}
		
		// the distance is the cost for transforming all letters in both strings
		return cost[len0 - 1];
	}
	
	public static int minimum(int a, int b, int c)
	{
		return Math.min(Math.min(a, b), c);
	}
	
	// len_s and len_t are the number of characters in string s and t respectively
	public static int recursiveLevenshteinDistance(String s, int len_s, String t, int len_t)
	{
		int cost;
		/* base case: empty strings */
		if (len_s == 0)
			return len_t;
		if (len_t == 0)
			return len_s;
		
		/* test if last characters of the strings match */
		if (s.charAt(len_s - 1) == t.charAt(len_t - 1))
			cost = 0;
		else
			cost = 1;
		
		/* return minimum of delete char from s, delete char from t, and delete char from both */
		return minimum(recursiveLevenshteinDistance(s, len_s - 1, t, len_t) + 1, recursiveLevenshteinDistance(s, len_s, t, len_t - 1) + 1,
				recursiveLevenshteinDistance(s, len_s - 1, t, len_t - 1) + cost);
	}
	
	public static int minDistance(String word1, String word2) // http://www.programcreek.com/2013/12/edit-distance-in-java/
	{
		int len1 = word1.length();
		int len2 = word2.length();
		
		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];
		
		for (int i = 0; i <= len1; i++)
		{
			dp[i][0] = i;
		}
		
		for (int j = 0; j <= len2; j++)
		{
			dp[0][j] = j;
		}
		
		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);
				
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				}
				else
				{
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;
					
					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					
					dp[i + 1][j + 1] = min;
				}
			}
		}
		
		System.out.println("\n-----Matrix for edit distance calculation---------");
		
		for (int i = 0; i <= len1; i++)
		{
			for (int j = 0; j <= len2; j++)
			{
				System.out.print(" " + dp[i][j]);
			}
			System.out.println();
		}
		System.out.println("-------------");
		
		return dp[len1][len2];
	}
	
	// /////
	// *****************************
	// Compute Levenshtein distance
	// //http://people.cs.pitt.edu/~kirk/cs1501/Pruhs/Spring2006/assignments/editdistance/Levenshtein%20Distance.htm
	// *****************************
	
	public static int LD(String s, String t)
	{
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost
		
		// Step 1
		
		n = s.length();
		m = t.length();
		
		if (n == 0)
		{
			return m;
		}
		
		if (m == 0)
		{
			return n;
		}
		
		d = new int[n + 1][m + 1];
		
		// Step 2
		
		for (i = 0; i <= n; i++)
		{
			d[i][0] = i;
		}
		
		for (j = 0; j <= m; j++)
		{
			d[0][j] = j;
		}
		
		// Step 3
		
		for (i = 1; i <= n; i++)
		{
			
			s_i = s.charAt(i - 1);
			
			// Step 4
			
			for (j = 1; j <= m; j++)
			{
				
				t_j = t.charAt(j - 1);
				
				// Step 5
				
				if (s_i == t_j)
				{
					cost = 0;
				}
				else
				{
					cost = 1;
				}
				
				// Step 6
				
				d[i][j] = minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
				
			}
			
		}
		
		// Step 7
		System.out.println("\n-----Matrix for edit distance calculation---------");
		
		for (int u = 0; u <= n; u++)
		{
			for (int y = 0; y <= m; y++)
			{
				System.out.print(" " + d[u][y]);
			}
			System.out.println();
		}
		System.out.println("-------------");
		
		return d[n][m];
		
	}
	
}
