package org.activity.nn;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * To get a better grasp on using ND4J
 * 
 * @author gunjan
 *
 */
public class ND4jExperiments
{
	public static void main(String args[])
	{
		int nRows = 2;
		int nColumns = 3;
		int nDepth = 5;

		// Create INDArray of zeros
		INDArray zeros_2_3 = Nd4j.zeros(nRows, nColumns);

		INDArray ones_2 = Nd4j.ones(nRows);

		// Create one of all ones
		INDArray ones_2_3 = Nd4j.ones(nRows, nColumns);

		// vstack
		INDArray vstack = Nd4j.vstack(ones_2_3, zeros_2_3);

		// System.out.println("### zeros_2_3 ####");
		// System.out.println(zeros_2_3 + "\n");

		System.out.println("### ones_2 ####");
		System.out.println(ones_2 + "\n");

		System.out.println("### ones_2_3 ####");
		System.out.println(ones_2_3 + "\n");

		// System.out.println("### VSTACK ####");
		// System.out.println(vstack + "\n");

		// Create one of all ones
		INDArray ones_2_3_5 = Nd4j.ones(nRows, nColumns, nDepth);
		System.out.println("### ones_2_3_5 ####");
		System.out.println(ones_2_3_5 + "\n");
	}

}
