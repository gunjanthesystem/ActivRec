package org.activity.objects;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * @author gunjan
 * @since 23 July
 */
public class PairedIndicesTo1DArrayStorage implements Serializable
{
	private static final long serialVersionUID = 1L;
	private double[] valsStored;
	private int numOfUniqueIndices;// n

	// /**
	// * for sanity check
	// * @param args
	// */
	// public static void main(String args[])
	// {
	// int size = 7;
	// Random r = new Random();
	// PairedIndicesTo1DArrayStorage pi = new PairedIndicesTo1DArrayStorage(size);
	//
	// for (int i = 0; i < size - 1; i++)
	// {
	// for (int j = i + 1; j < size; j++)
	// {
	// int val = r.nextInt(100);
	// System.out.println("i= " + i + " j= " + j + " val= " + val);
	// pi.addVal(i, j, val);
	// }
	// }
	//
	// System.out.println(pi.toString());
	// }

	/**
	 * n
	 * 
	 * @param numOfUniqueIndices
	 *            n
	 */
	public PairedIndicesTo1DArrayStorage(int numOfUniqueIndices)
	{
		this.numOfUniqueIndices = numOfUniqueIndices;
		valsStored = new double[(numOfUniqueIndices * (numOfUniqueIndices - 1)) / 2];
	}

	/**
	 * Note: indices are expected to start from 0
	 * 
	 * @param index1
	 * @param index2
	 */
	public void addVal(int index1, int index2, double valToAdd)
	{
		int oneDArrayIndex = pairedIndicesTo1DArrayIndex(index1, index2, numOfUniqueIndices);
		valsStored[oneDArrayIndex] = valToAdd;
	}

	/**
	 * Note: indices are expected to start from 0
	 * 
	 * @param index1
	 * @param index2
	 */
	public double getVal(int index1, int index2)
	{
		int oneDArrayIndex = pairedIndicesTo1DArrayIndex(index1, index2, valsStored.length);
		return valsStored[oneDArrayIndex];
	}

	public static int pairedIndicesTo1DArrayIndex(int index1, int index2, int totalNumOfIndices)
	{
		int lowerIndex = -1, higherIndex = -1;
		if (index1 <= index2)
		{
			lowerIndex = index1;
			higherIndex = index2;
		}
		else
		{
			higherIndex = index1;
			lowerIndex = index2;
		}

		int tempVal = 0;
		for (int i = 1; i <= lowerIndex; i++)
		{
			tempVal += (totalNumOfIndices - i);
		}

		int oneDArrayIndex = tempVal + (higherIndex - lowerIndex) - 1;
		// System.out.println(
		// "lowerIndex = " + lowerIndex + " higherIndex=" + higherIndex + " oneDArrayIndex=" + oneDArrayIndex);
		return oneDArrayIndex;
	}

	public double[] getValsStored()
	{
		return valsStored;
	}

	public int getNumOfUniqueIndices()
	{
		return numOfUniqueIndices;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + numOfUniqueIndices;
		result = prime * result + Arrays.hashCode(valsStored);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PairedIndicesTo1DArrayStorage other = (PairedIndicesTo1DArrayStorage) obj;
		if (numOfUniqueIndices != other.numOfUniqueIndices) return false;
		if (!Arrays.equals(valsStored, other.valsStored)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "PairedIndicesTo1DArray [valsStored=" + Arrays.toString(valsStored) + ", numOfUniquePairedIndices="
				+ numOfUniqueIndices + "]";
	}

}
