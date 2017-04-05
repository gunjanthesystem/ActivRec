package org.activity.objects;

import org.activity.ui.PopUps;

/**
 * 
 * @author gunjan
 *
 */
public class TraceMatrix_backup5April2017
{

	private char threeDCharMatrix[][][];
	private int endPoints[][];
	private final static int lengthOfEachOpDesc = 7;
	int nRows, nCols, maxSizeOfCell;
	// private static String reusable = "";

	// public static void main(String args[])
	// {
	// TraceMatrix tm = new TraceMatrix(3, 4);
	// System.out.println("Tracematrix:\n" + tm.toString());
	// }

	/**
	 * 
	 * @param lengthOfWord1
	 * @param lengthOfWord2
	 */
	public TraceMatrix_backup5April2017(int lengthOfWord1, int lengthOfWord2)
	{
		// threeDCharMatrix = StringUtils.create3DCharArrayOptimalSize(lengthOfWord1, lengthOfWord2, true);
		nRows = lengthOfWord1 + 1;
		nCols = lengthOfWord2 + 1;
		maxSizeOfCell = (lengthOfWord1 + lengthOfWord2) * lengthOfEachOpDesc;

		threeDCharMatrix = new char[nRows][nCols][maxSizeOfCell];

		for (int i = 0; i < nRows; ++i)
		{
			for (int j = 0; j < nCols; ++j)
			{
				for (int k = 0; k < maxSizeOfCell/* ((i + j) * lengthOfEachOpDesc) */; k++)
				{
					threeDCharMatrix[i][j][k] = 'x';
				}
			}
		}

		endPoints = new int[nRows][nCols];
		for (int i = 0; i < nRows; ++i)
		{
			for (int j = 0; j < nCols; ++j)
			{
				endPoints[i][j] = -1;// 0;// (i + j) * lengthOfEachOpDesc;
			}
		}
	}

	// /**
	// * Disabled as not used at the moment
	// * @param rowIndex
	// * @param colIndex
	// * @param charToAdd
	// */
	// public void addCharToCell(int rowIndex, int colIndex, char charToAdd)
	// {
	// try
	// {
	// int currEndPointOfThisCell = endPoints[rowIndex][colIndex];
	// threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell + 1] = charToAdd;
	// endPoints[rowIndex][colIndex] = currEndPointOfThisCell + 1;
	// }
	// catch (Exception e)
	// {
	// PopUps.getCurrentStackTracedErrorMsg(
	// "Error in org.activity.objects.TraceMatrix.addCharToCell(int, int, char)");
	// e.printStackTrace();
	// }
	// }

	// /**
	// * Disabled as not used at the moment
	// * @param rowIndex
	// * @param colIndex
	// * @param charsToAdd
	// */
	// public void addCharsToCell(int rowIndex, int colIndex, char... charsToAdd)
	// {
	// try
	// {
	// int currEndPointOfThisCell = endPoints[rowIndex][colIndex];
	// for (char charToAdd : charsToAdd)
	// {
	// threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell + 1] = charToAdd;
	// currEndPointOfThisCell += 1;
	// endPoints[rowIndex][colIndex] = currEndPointOfThisCell;
	// }
	// }
	// catch (Exception e)
	// {
	// PopUps.getCurrentStackTracedErrorMsg("Error in org.activity.objects.TraceMatrix.addCharsToCell()");
	// e.printStackTrace();
	// }
	// }

	// /**
	// * Disabled as not used at the moment
	// * @param rowIndex
	// * @param colIndex
	// * @param charArray
	// * @param charsToAdd
	// */
	// public void addCharsToCell(int rowIndex, int colIndex, char[] charArray, char... charsToAdd)
	// {
	// try
	// {
	// int currEndPointOfThisCell = endPoints[rowIndex][colIndex];
	//
	// for (char charToAdd : charArray)
	// {
	// threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell + 1] = charToAdd;
	// currEndPointOfThisCell += 1;
	// endPoints[rowIndex][colIndex] = currEndPointOfThisCell;
	// }
	//
	// for (char charToAdd : charsToAdd)
	// {
	// threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell + 1] = charToAdd;
	// currEndPointOfThisCell += 1;
	// endPoints[rowIndex][colIndex] = currEndPointOfThisCell;
	// }
	// }
	// catch (Exception e)
	// {
	// PopUps.getCurrentStackTracedErrorMsg("Error in org.activity.objects.TraceMatrix.addCharsToCell()");
	// e.printStackTrace();
	// }
	// }

	/**
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @param charArray
	 * @param charsToAdd
	 */
	public void addCharsToCell(int rowIndex, int colIndex, char[] c1Array, char c2, char c3, char c4, int i5, char c6,
			int i7, char c8)
	{

		System.out.println("--- entering addCharsToCell with (" + rowIndex + "," + colIndex + "), adding"
				+ (c1Array).toString() + "," + c2 + "," + c3 + "," + c4 + "," + i5 + "," + c6 + "," + i7 + "," + c8
				+ "\n traceMatrix:\n" + this.toString());
		int currEndPointOfThisCell = endPoints[rowIndex][colIndex];
		try
		{
			if (c1Array != null)
			{
				for (char charToAdd : c1Array)
				{
					currEndPointOfThisCell += 1;
					threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell] = charToAdd;
				}
			}

			currEndPointOfThisCell += 1;
			threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell] = c2;

			currEndPointOfThisCell += 1;
			threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell] = c3;

			currEndPointOfThisCell += 1;
			threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell] = c4;

			for (char charToAdd : Integer.toString(i5).toCharArray())
			{
				currEndPointOfThisCell += 1;
				threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell] = charToAdd;
			}

			currEndPointOfThisCell += 1;
			threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell] = c6;

			for (char charToAdd : Integer.toString(i7).toCharArray())
			{
				currEndPointOfThisCell += 1;
				threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell] = charToAdd;
			}

			currEndPointOfThisCell += 1;
			threeDCharMatrix[rowIndex][colIndex][currEndPointOfThisCell] = c8;

			// System.out.println("currEndPointOfThisCell = " + currEndPointOfThisCell);
			endPoints[rowIndex][colIndex] = currEndPointOfThisCell;
		}
		catch (Exception e)
		{
			System.out.println("nRows= " + nRows + ", nCols= " + nCols + ", maxSizeOfCell = " + maxSizeOfCell);// );
			System.out.println("traceMatrix:\n" + this.toString());
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in org.activity.objects.TraceMatrix.addCharsToCell() , rowIndex=" + rowIndex + ", colIndex="
							+ colIndex));
			e.printStackTrace();
		}
		System.out.println("--- exiting addCharsToCell with" + String.valueOf(c1Array) + "," + c2 + "," + c3 + "," + c4
				+ "," + i5 + "," + c6 + "," + i7 + "," + c8 + "\n traceMatrix:\n" + this.toString());

	}

	// /**
	// *
	// * @param rowIndex
	// * @param colIndex
	// * @return
	// */
	// public String getCellAtIndexString(int rowIndex, int colIndex)
	// {
	// String toReturn = "e";
	// int endPointOfCell = endPoints[rowIndex][colIndex];
	//
	// if (endPointOfCell != 0)
	// {
	// char[] cellWord = new char[endPointOfCell];
	// for (int k = 0; k < endPointOfCell; k++)
	// {
	// cellWord[k] = threeDCharMatrix[rowIndex][colIndex][k];
	// }
	// toReturn = String.valueOf(cellWord);
	// }
	// return toReturn;
	// }

	/**
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	public char[] getCellAtIndex(int rowIndex, int colIndex)
	{
		int endPointOfCell = endPoints[rowIndex][colIndex];
		char[] cellWord = null;
		try
		{

			if (endPointOfCell == -1)
			{ // in case the cell was empty. return 0 length char array
				// endPointOfCell = 0;
				return null;
			}
			else
			{
				cellWord = new char[endPointOfCell];
				for (int k = 0; k <= endPointOfCell; k++)
				{
					cellWord[k] = threeDCharMatrix[rowIndex][colIndex][k];
				}
			}

		}
		catch (Exception e)
		{
			System.out.println("traceMatrix:\n" + this.toString());
			System.out.println("rowIndex=" + rowIndex + ",colIndex=" + colIndex);
			e.printStackTrace();
		}
		return cellWord;
	}

	/**
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	public String getCellAtIndexFixedLength(int rowIndex, int colIndex)
	{
		int endPointOfCell = endPoints[rowIndex][colIndex];

		char[] cellWord;
		if (endPointOfCell == -1)
		{// if empty cell
			// cellWord = null;//
			endPointOfCell = 0;
		}

		cellWord = new char[endPointOfCell];

		for (int k = 0; k < endPointOfCell; k++)
		{
			cellWord[k] = threeDCharMatrix[rowIndex][colIndex][k];
		}

		StringBuffer res = new StringBuffer(String.valueOf(cellWord));
		int resLen = res.length();
		while (resLen < this.maxSizeOfCell)
		{
			res.append(".");
			resLen += 1;
		}
		return res.toString();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder(
				"----- nRows:" + nRows + ", nCols:" + nCols + ", maxSizeOfCell:" + maxSizeOfCell + " -----\n");
		for (int rowIndex = 0; rowIndex < nRows; rowIndex++)
		{
			for (int colIndex = 0; colIndex < nCols; colIndex++)
			{
				String cellAtIndex = String.valueOf(getCellAtIndexFixedLength(rowIndex, colIndex));
				// System.out.println(">> cellAtIndex = [" + cellAtIndex + "] >> cellLength=" + cellAtIndex.length());
				sb.append("|").append(cellAtIndex);
			}
			sb.append("\n");
		}
		sb.append("\n----------\n");
		return sb.toString();
	}

	/**
	 * 
	 */
	public void resetAll()
	{
		// for (int i = 0; i < nRows; ++i)
		// {
		// for (int j = 0; j < nCols; ++j)
		// {
		// for (int k = 0; k < i + j; k++)
		// {
		// threeDCharMatrix[i][j][k] = 'x';
		// }
		// }
		// }

		for (int i = 0; i < nRows; i++)
		{
			for (int j = 0; j < nCols; j++)
			{
				endPoints[i][j] = -1;
			}
		}
	}

	// /**
	// *
	// * @return
	// */
	// public char[][][] getThreeDCharMatrix()
	// {
	// return threeDCharMatrix;
	// }
	//
	// public int[][] getEndPoints()
	// {
	// return endPoints;
	// }

	// public int getnRow()
	// {
	// return nRows;
	// }
	//
	// public int getnCols()
	// {
	// return nCols;
	// }
	//
	// public int getnCells()
	// {
	// return maxSizeOfCell;
	// }

}
