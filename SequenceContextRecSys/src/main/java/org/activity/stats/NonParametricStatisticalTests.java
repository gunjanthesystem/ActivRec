package org.activity.stats;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import javanpst.data.structures.dataTable.DataTable;
import javanpst.tests.equality.kruskalWallisTest.KruskalWallisTest;
import javanpst.tests.location.wilcoxonRankSumTest.WilcoxonRankSumTest;

/**
 * A sample to test the difference of means between two samples, using Wilcoxon Ranks-Sum test and Normal Scores (van
 * der Warden) test
 *
 * @author Joaqu�n Derrac Rus (jderrac@decsai.ugr.es)
 * @version 1.0
 */
public class NonParametricStatisticalTests
{
	public static void main(String args[])
	{
		/**
		 * Two samples
		 */
		double twoSamples[][] = { { 12.6, 16.4 }, { 11.2, 15.4 }, { 11.4, 14.1 }, { 9.4, 14.0 }, { 13.2, 13.4 },
				{ 12.0, 11.3 } };

		double[] a = { 12.6, 11.2, 11.4, 9.4, 13.2, 12 };
		double[] b = { 16.4, 15.4, 14.1, 14.0, 13.4, 11.3 };

		/**
		 * K independant samples:
		 */
		double kSamples[][] = { { 19, 14, 12, 38 }, { 22, 21, 1, 39 }, { 25, 2, 5, 40 }, { 24, 6, 8, 30 },
				{ 29, 10, 4, 31 }, { 26, 16, 13, 32 }, { 37, 17, 9, 33 }, { 23, 11, 15, 36 }, { 27, 18, 3, 34 },
				{ 28, 7, 20, 35 } };

		moeaMannWitneyUTest(a, b);
		apacheCommonsMannWitneyUTest(twoSamples);
		System.out.println("-----------------");

		apacheCommonsMannWitneyUTest(a, b);
		System.out.println("-----------------");

		javaNPSTWilcoxonRankSumTest(twoSamples);
		System.out.println("-----------------");

		// javaNPSTWilcoxonRankSumTest(a, b);
		// System.out.println("-----------------");

		javaNPSTKruskalWallisTest(kSamples);
		System.out.println("-----------------");
	}

	/**
	 * 
	 * @param a
	 * @param b
	 */
	public static void moeaMannWitneyUTest(double a[], double b[])
	{
		System.out.println("moeaMannWitneyUTest");
		org.moeaframework.util.statistics.MannWhitneyUTest test = new org.moeaframework.util.statistics.MannWhitneyUTest();

		test.addAll(a, 0);
		test.addAll(b, 1);
		double sig = 0.01;
		System.out.println("at " + sig + ": null hypothesis is rejected = " + test.test(sig));
	}

	/**
	 * 
	 * @param a
	 * @param b
	 */
	public static void apacheCommonsMannWitneyUTest(double a[], double b[])
	{
		System.out.println("Inside apacheCommonsMannWitneyUTest ");
		MannWhitneyUTest muT = new MannWhitneyUTest();
		System.out.println("muT.mannWhitneyU= " + muT.mannWhitneyU(a, b));
		System.out.println("muT.mannWhitneyUTest= " + muT.mannWhitneyUTest(a, b));

	}

	/**
	 * 
	 * @param samples
	 */
	public static void apacheCommonsMannWitneyUTest(double samples[][])
	{
		System.out.println("Inside apacheCommonsMannWitneyUTest ");
		// Data is formatted
		DataTable data = new DataTable(samples);
		MannWhitneyUTest muT = new MannWhitneyUTest();
		System.out.println("muT.mannWhitneyU= " + muT.mannWhitneyU(data.getColumn(0), data.getColumn(1)));
		System.out.println("muT.mannWhitneyUTest= " + muT.mannWhitneyUTest(data.getColumn(0), data.getColumn(1)));

	}

	// /**
	// *
	// * @param a
	// * @param b
	// */
	// public static void javaNPSTWilcoxonRankSumTest(double a[], double b[])
	// {
	// DataTable data = new DataTable();
	// data.setColumns(2);
	// data.setColumn(0, a);
	// data.setColumn(1, b);
	// System.out.println(data.toString());
	// System.out.println(data.getRows() + "," + data.getColumns());
	// // Create tests
	// WilcoxonRankSumTest test = new WilcoxonRankSumTest(data);
	//
	// // Run Wilcoxon test
	// test.doTest();
	//
	// // Print results of the test
	// System.out.println("Results of Wilcoxon Ranks-Sum test:\n" + test.printReport());
	// System.out.println("*******************\n");
	// }// end-method

	/**
	 * 
	 * @param samples
	 */
	public static void javaNPSTWilcoxonRankSumTest(double samples[][])
	{
		// Data is formatted
		DataTable data = new DataTable(samples);

		// data.getColumn(0);
		System.out.println(data.toString());
		System.out.println(data.getRows() + "," + data.getColumns());
		// Create tests
		WilcoxonRankSumTest test = new WilcoxonRankSumTest(data);

		// Run Wilcoxon test
		test.doTest();

		// Print results of the test
		System.out.println("Results of Wilcoxon Ranks-Sum test:\n" + test.printReport());
		System.out.println("*******************\n");
	}// end-method

	/**
	 * 
	 * @param samples
	 */
	public static void javaNPSTKruskalWallisTest(double samples[][])
	{
		// Data is formatted
		DataTable data = new DataTable(samples);

		System.out.println(data.toString());
		System.out.println(data.getRows() + "," + data.getColumns());

		// Create tests
		KruskalWallisTest kw = new KruskalWallisTest(data);

		// Run Kruskal-Wallis test
		kw.doTest();

		// Print results of Kruskal-Wallis test
		System.out.println("Results of Kruskal-Wallis test:\n" + kw.printReport());

		// print results of the multiple comparisons procedure
		System.out.println(
				"Results of the multiple comparisons procedure:\n" + kw.printMultipleComparisonsProcedureReport());
	}

	// /**
	// *
	// * @param a
	// * @param b
	// */
	// public static void javaNPSTKruskalWallisTest(double a[], double b[])
	// {
	// DataTable data = new DataTable();
	//
	// data.setColumns(2);
	// data.setColumn(0, a);
	// data.setColumn(1, b);
	// System.out.println(data.toString());
	// System.out.println(data.getRows() + "," + data.getColumns());
	//
	// // Create tests
	// KruskalWallisTest kw = new KruskalWallisTest(data);
	//
	// // Run Kruskal-Wallis test
	// kw.doTest();
	//
	// // Print results of Kruskal-Wallis test
	// System.out.println("Results of Kruskal-Wallis test:\n" + kw.printReport());
	//
	// // print results of the multiple comparisons procedure
	// System.out.println(
	// "Results of the multiple comparisons procedure:\n" + kw.printMultipleComparisonsProcedureReport());
	// }
}// end-class