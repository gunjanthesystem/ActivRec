package org.activity.objects;

import java.io.Serializable;

/**
 * 
 * @author gunjan
 * @since 23 July
 */
public class PairedIndicesTo1DArrayConverter implements Serializable
{

	private static final long serialVersionUID = -1642924826236812750L;
	private int numOfUniqueIndices;// n
	private int sizeOf1DArray;
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
	public PairedIndicesTo1DArrayConverter(int numOfUniqueIndices)
	{
		this.numOfUniqueIndices = numOfUniqueIndices;
		sizeOf1DArray = (numOfUniqueIndices * (numOfUniqueIndices - 1)) / 2;
	}

	/**
	 * CREATED FOR SERIALISATION PURPOSE
	 */
	public PairedIndicesTo1DArrayConverter()
	{
		super();
	}

	/**
	 * 
	 * @param index1
	 * @param index2
	 *
	 * @return
	 */
	public int pairedIndicesTo1DArrayIndex(int index1, int index2)// , int totalNumOfIndices)
	{
		int totalNumOfIndices = numOfUniqueIndices;// n
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

	// public static int[] from1DArrayIndexToPairedIndices(int oneDIndex, int totalNumOfIndices)
	// {
	// int[] pairedIndices = new int[2];// {lower,higher}
	// return pairedIndices;
	// }

	public int getNumOfUniqueIndices()
	{
		return numOfUniqueIndices;
	}

	public int getSizeOf1DArray()
	{
		return sizeOf1DArray;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + numOfUniqueIndices;
		result = prime * result + sizeOf1DArray;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PairedIndicesTo1DArrayConverter other = (PairedIndicesTo1DArrayConverter) obj;
		if (numOfUniqueIndices != other.numOfUniqueIndices) return false;
		if (sizeOf1DArray != other.sizeOf1DArray) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "PairedIndicesTo1DArrayConverter [numOfUniqueIndices=" + numOfUniqueIndices + ", sizeOf1DArray="
				+ sizeOf1DArray + "]";
	}

}
