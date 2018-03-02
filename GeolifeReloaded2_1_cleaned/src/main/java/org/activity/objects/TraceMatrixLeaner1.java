package org.activity.objects;

import java.util.Arrays;

import org.activity.ui.PopUps;
import org.activity.util.IntegerUtils;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Inspired by optimisation suggested
 * in:http://www.javaworld.com/article/2077496/testing-debugging/java-tip-130--do-you-know-your-data-size-.html?page=2
 * http://stackoverflow.com/questions/2512082/java-multi-dimensional-array-vs-one-dimensional
 * 
 * @author gunjan
 *
 */
public class TraceMatrixLeaner1
{

	private char threeDCharMatrix[][];// not reducing 3d to 1d since the innermost array is of jagged sizes.
	private int lengthOfCell[];
	// private static int lengthOfEachOpDesc;// = 7;
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
	public TraceMatrixLeaner1(int lengthOfWord1, int lengthOfWord2)
	{
		// threeDCharMatrix = StringUtils.create3DCharArrayOptimalSize(lengthOfWord1, lengthOfWord2, true);
		nRows = lengthOfWord1 + 1;
		nCols = lengthOfWord2 + 1;

		// maxSizeOfCell = (lengthOfWord1 + lengthOfWord2) * lengthOfEachOpDesc;
		try
		{
			threeDCharMatrix = new char[nRows * nCols][];// maxSizeOfCell

			for (int rowIndex = 0; rowIndex < nRows; rowIndex++)
			{
				int rowIndexEff = rowIndex * nCols;
				int numOfDigitsInRowIndex = IntegerUtils.getNumOfDigits4(rowIndex);

				for (int colIndex = 0; colIndex < nCols; colIndex++)
				{
					int rowColIndexEff = rowIndexEff + colIndex;
					int numOfDigitsInColIndex = IntegerUtils.getNumOfDigits4(colIndex);

					int lengthOfCell = (rowIndex + colIndex) * (5 + numOfDigitsInRowIndex + numOfDigitsInColIndex);// lengthOfEachOpDesc;

					// System.out.println(
					// " -- rowIndex: " + rowIndex + " colIndex: " + colIndex + " lengthOfCell: " + lengthOfCell);
					threeDCharMatrix[rowColIndexEff] = new char[lengthOfCell];

					for (int k = 0; k < lengthOfCell /* maxSizeOfCell//((i + j) * lengthOfEachOpDesc) */; k++)
					{
						threeDCharMatrix[rowColIndexEff][k] = 'x';
					}
					// WritingToFile.appendLineToFileAbsolute(
					// rowIndex + "," + colIndex + "," + lengthOfCell + ","
					// + PerformanceAnalytics.getHeapPercentageFreeValue() + "\n",
					// "TraceMatrixConstructorHeapConsumption.csv");

				}
			}

			// IntegerUtils.getNumOfDigits4(nRows -1 =lengthOfWord1 )
			maxSizeOfCell = 5 + IntegerUtils.getNumOfDigits4(lengthOfWord1)
					+ IntegerUtils.getNumOfDigits4(lengthOfWord2);
			lengthOfCell = new int[nRows * nCols];
			this.resetLengthOfCells();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println(PopUps.getTracedErrorMsg("Exception  in TraceMatrix("));
			System.exit(-1);
		}
		// System.out.println("this.toString() :\n" + this.toStringActualLength());
		// for (int i = 0; i < nRows; ++i)
		// {
		// for (int j = 0; j < nCols; ++j)
		// {
		// lengthOfCell[i][j] = 0;// 0;// (i + j) * lengthOfEachOpDesc;
		// }
		// }
	}

	/**
	 * with indexed for loop for improved performance
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @param charArray
	 * @param charsToAdd
	 */
	public void addCharsToCell(int rowIndex, int colIndex, char[] c1Array, char c2, char c3, char c4, int i5, char c6,
			int i7, char c8)
	{
		// System.out.println("--- entering addCharsToCell with (" + rowIndex + "," + colIndex + "), adding"
		// + String.valueOf(c1Array) + "," + c2 + "," + c3 + "," + c4 + "," + i5 + "," + c6 + "," + i7 + "," + c8
		// + "\n traceMatrix:\n" + this.toString());
		// int rowIndexEff = rowIndex * nCols;
		int effectiveIndex = rowIndex * nCols + colIndex;
		int currLengthOfThisCell = lengthOfCell[effectiveIndex];

		// System.out.println("currLengthOfThisCell at begin = " + currLengthOfThisCell);
		try
		{
			// if (c1Array != null) {
			int c1ArrayLength = c1Array.length;
			for (int i = 0; i < c1ArrayLength; i++)// char charToAdd : c1Array)
			{
				threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c1Array[i];
			}
			// System.out.println("currLengthOfThisCell after c1 = " + currLengthOfThisCell);// 63

			// }

			threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c2;
			// 64
			threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c3;

			threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c4;
			// 66

			char[] i5Array = IntegerUtils.intToCharArray(i5);
			int i5ArrayLength = i5Array.length;
			for (int i = 0; i < i5ArrayLength; i++)
			// char charToAdd : IntegerUtils.intToCharArray(i5))//
			{
				threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = i5Array[i];
			}

			threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c6;

			char[] i7Array = IntegerUtils.intToCharArray(i7);
			int i7ArrayLength = i7Array.length;
			for (int i = 0; i < i7ArrayLength; i++)// char charToAdd : IntegerUtils.intToCharArray(i7))
			{
				threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = i7Array[i];
			}

			// System.out.println("currLengthOfThisCell at c8 = " + currLengthOfThisCell);
			threeDCharMatrix[effectiveIndex][currLengthOfThisCell] = c8;

			// System.out.println("currEndPointOfThisCell = " + currEndPointOfThisCell);
			lengthOfCell[effectiveIndex] = currLengthOfThisCell + 1;

			// System.out.println(
			// "--- exiting addCharsToCell with" + String.valueOf(c1Array) + "," + c2 + "," + c3 + "," + c4 + ","
			// + i5 + "," + c6 + "," + i7 + "," + c8 + "\n traceMatrix:\n" + this.toStringActualLength());

		}
		catch (Exception e)
		{
			System.out.println("Exception: nRows= " + nRows + ", nCols= " + nCols);// + ", maxSizeOfCell = " +
																					// maxSizeOfCell);// );
			// System.out.println("traceMatrix:\n" + this.toString());
			// $$ System.err.println(("Error in org.activity.objects.TraceMatrix.addCharsToCell() , rowIndex=" +
			// rowIndex
			// + ", colIndex=" + colIndex + "\n traceMatrix:\n" + this.toStringActualLength()));
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	public char[] getCellAtIndex(int rowIndex, int colIndex)
	{
		// System.out.println("Inside getCellAtIndex, rowIndex:" + rowIndex + " colIndex:" + colIndex);
		int effectiveIndex = rowIndex * nCols + colIndex;
		int lengthOfCellValue = lengthOfCell[effectiveIndex];
		char[] cellWord = null;
		// $$ char[] allWithGarbage = null;
		try
		{
			cellWord = new char[lengthOfCellValue];
			for (int k = 0; k < lengthOfCellValue; k++)
			{
				cellWord[k] = threeDCharMatrix[effectiveIndex][k];
			}
			// $$ allWithGarbage = threeDCharMatrix[effectiveIndex];
		}
		catch (Exception e)
		{
			System.out.println("traceMatrix:\n" + this.toString());
			System.out.println("rowIndex=" + rowIndex + ",colIndex=" + colIndex);
			e.printStackTrace();
		}
		// System.out.println("getCellAtIndex, rowIndex:" + rowIndex + " colIndex:" + colIndex + "cellword=["
		// + String.valueOf(cellWord) + "]" + " allWithGarbage.length:" + allWithGarbage.length);

		// System.out.println("cellword=[" + String.valueOf(cellWord) + "]");
		// System.out.println("Exiting getCellAtIndex");
		return cellWord;
	}

	/**
	 * Only 'D' 'I' 'N' 'S' chars to avoid string splitting later which is is performance degrader.
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 * @since 1 March 2018
	 */
	public char[] getCellAtIndexOnlyDISN(int rowIndex, int colIndex)
	{
		// System.out.println("Inside getCellAtIndex, rowIndex:" + rowIndex + " colIndex:" + colIndex);
		CharOpenHashSet disn = new CharOpenHashSet(Arrays.asList('D', 'I', 'N', 'S'));

		int effectiveIndex = rowIndex * nCols + colIndex;
		int lengthOfCellValue = lengthOfCell[effectiveIndex];
		char[] cellWordDISN = null;
		CharArrayList res = new CharArrayList();
		// $$ char[] allWithGarbage = null;
		try
		{
			cellWordDISN = new char[lengthOfCellValue];
			for (int k = 0; k < lengthOfCellValue; k++)
			{
				char cVal = threeDCharMatrix[effectiveIndex][k];
				if (disn.contains(cVal))
				{
					cellWordDISN[k] = threeDCharMatrix[effectiveIndex][k];
					res.add(threeDCharMatrix[effectiveIndex][k]);
				}
			}
			// $$ allWithGarbage = threeDCharMatrix[effectiveIndex];
		}
		catch (Exception e)
		{
			System.out.println("traceMatrix:\n" + this.toString());
			System.out.println("rowIndex=" + rowIndex + ",colIndex=" + colIndex);
			e.printStackTrace();
		}
		// System.out.println("getCellAtIndex, rowIndex:" + rowIndex + " colIndex:" + colIndex + "cellword=["
		// + String.valueOf(cellWord) + "]" + " allWithGarbage.length:" + allWithGarbage.length);

		// System.out.println("cellword=[" + String.valueOf(cellWord) + "]");
		// System.out.println("Exiting getCellAtIndex");
		// System.out.println("res= " + res.to);
		return res.toCharArray();
	}

	public Pair<int[], int[]> getCellAtIndexOnlyCoordinates(int rowIndex, int colIndex)
	{
		int effectiveIndex = rowIndex * nCols + colIndex;
		int lengthOfCellValue = lengthOfCell[effectiveIndex];
		IntArrayList cellWordCoord1 = new IntArrayList();
		IntArrayList cellWordCoord2 = new IntArrayList();
		// int coord1Index = 0, coord2Index = 0;
		// $$ char[] allWithGarbage = null;
		try
		{
			// System.out.println("lengthOfCellValue=" + lengthOfCellValue);
			// char currentChar = 'z', prevChar = 'c', nextChar = 'z';
			// StringBuilder int1 = new StringBuilder();
			// StringBuilder int2 = new StringBuilder();
			for (int k = 0; k < lengthOfCellValue - 1; k++)
			{
				// currentChar = threeDCharMatrix[effectiveIndex][k];
				// prevChar = threeDCharMatrix[effectiveIndex][k - 1];
				// nextChar = threeDCharMatrix[effectiveIndex][k + 1];
				// // System.out.println("cval= " + String.valueOf(cVal));
				// // System.out.println("is digit= " + Character.isDigit(cVal));
				//
				// if (prevChar == '(')
				// {
				// int1.append(currentChar);
				// continue;
				// }
				// if (nextChar == '-')
				// {
				// int1.append(currentChar);
				// continue;
				// }
				char cVal = threeDCharMatrix[effectiveIndex][k];
				// System.out.println("k=" + k);
				// System.out.println("cVal=" + cVal);

				if (cVal == '(')
				{
					// System.out.println("prev (");
					StringBuilder int1 = new StringBuilder();
					while (threeDCharMatrix[effectiveIndex][++k] != '-')
					{
						int1.append(threeDCharMatrix[effectiveIndex][k]);
					}
					int v = Integer.valueOf(int1.toString());
					cellWordCoord1.add(v);
					// System.out.println("-- v=" + v);
					k -= 1;
					continue;
				}
				if (cVal == '-')
				{
					// System.out.println("prev (");
					StringBuilder int2 = new StringBuilder();
					while (threeDCharMatrix[effectiveIndex][++k] != ')')
					{
						int2.append(threeDCharMatrix[effectiveIndex][k]);
					}
					int v = Integer.valueOf(int2.toString());
					cellWordCoord2.add(v);
					// System.out.println("-- v2=" + v);
				}

				// if (threeDCharMatrix[effectiveIndex][k - 1] == '-')
				// {
				// System.out.println("prev -");
				// StringBuilder int2;
				// do
				// {
				// int2 = new StringBuilder(threeDCharMatrix[effectiveIndex][k]);
				// k++;
				// }
				// while (threeDCharMatrix[effectiveIndex][k + 1] != ')');
				// int v = Integer.valueOf(int2.toString());
				// cellWordCoord2[coord2Index++] = v;
				// System.out.println("-- v=" + v);
				// }
				// if (threeDCharMatrix[effectiveIndex][k + 1] == '-')
				// {
				// System.out.println("next -");
				// }

			}
			// $$ allWithGarbage = threeDCharMatrix[effectiveIndex];
		}
		catch (Exception e)
		{
			System.out.println("traceMatrix:\n" + this.toString());
			System.out.println("rowIndex=" + rowIndex + ",colIndex=" + colIndex);
			e.printStackTrace();
		}
		// System.out.println("getCellAtIndex, rowIndex:" + rowIndex + " colIndex:" + colIndex + "cellword=["
		// + String.valueOf(cellWord) + "]" + " allWithGarbage.length:" + allWithGarbage.length);

		// System.out.println("cellword=[" + String.valueOf(cellWord) + "]");
		// System.out.println("Exiting getCellAtIndex");
		return new Pair<>(cellWordCoord1.toIntArray(), cellWordCoord2.toIntArray());

	}

	/**
	 * coordinates
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @return {array of first pos coords, array of second pos coords}
	 * @since 1 March 2018
	 * 
	 */
	public Pair<int[], int[]> getCellAtIndexOnlyCoordinates0(int rowIndex, int colIndex)
	{
		// System.out.println("Inside getCellAtIndex, rowIndex:" + rowIndex + " colIndex:" + colIndex);
		// CharOpenHashSet disn = new CharOpenHashSet(Arrays.asList('D', 'I', 'N', 'S'));

		int effectiveIndex = rowIndex * nCols + colIndex;
		int lengthOfCellValue = lengthOfCell[effectiveIndex];
		int[] cellWordCoord1 = null;
		int[] cellWordCoord2 = null;
		// $$ char[] allWithGarbage = null;
		try
		{
			cellWordCoord1 = new int[lengthOfCellValue];
			cellWordCoord2 = new int[lengthOfCellValue];
			// char preceedingChar = 'z';
			System.out.println("lengthOfCellValue" + lengthOfCellValue);

			for (int k = 1; k < (lengthOfCellValue - 1); k++)// not starting form 0
			{
				// char cVal = threeDCharMatrix[effectiveIndex][k];
				System.out.println("k=" + k);
				System.out.println("threeDCharMatrix[effectiveIndex][k]=" + threeDCharMatrix[effectiveIndex][k]);

				if (threeDCharMatrix[effectiveIndex][k - 1] == '(')// disn.contains(cVal))
				{
					System.out.println("matched opening prev");
					StringBuilder newInt = new StringBuilder();
					while (threeDCharMatrix[effectiveIndex][k + 1] != '-')
					{
						// if(Character.isDigit(threeDCharMatrix[effectiveIndex][k])
						newInt.append(threeDCharMatrix[effectiveIndex][k]);
						System.out.println("building coord 1" + newInt.toString());
						k++;
					}
					cellWordCoord1[k] = Integer.valueOf(newInt.toString());
				}

				if (threeDCharMatrix[effectiveIndex][k - 1] == '-')// disn.contains(cVal))
				{
					StringBuilder newInt = new StringBuilder();
					System.out.println("matched - prev");
					while (threeDCharMatrix[effectiveIndex][k + 1] != ')')
					{
						// if(Character.isDigit(threeDCharMatrix[effectiveIndex][k])
						newInt.append(threeDCharMatrix[effectiveIndex][k]);
						System.out.println("building coord 2" + newInt.toString());
						k++;
					}
					cellWordCoord2[k] = Integer.valueOf(newInt.toString());
				}

			}
			// $$ allWithGarbage = threeDCharMatrix[effectiveIndex];
		}
		catch (Exception e)
		{
			System.out.println("traceMatrix:\n" + this.toString());
			System.out.println("rowIndex=" + rowIndex + ",colIndex=" + colIndex);
			e.printStackTrace();
		}
		// System.out.println("getCellAtIndex, rowIndex:" + rowIndex + " colIndex:" + colIndex + "cellword=["
		// + String.valueOf(cellWord) + "]" + " allWithGarbage.length:" + allWithGarbage.length);

		// System.out.println("cellword=[" + String.valueOf(cellWord) + "]");
		// System.out.println("Exiting getCellAtIndex");
		return new Pair<>(cellWordCoord1, cellWordCoord2);
	}

	/**
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	public String getCellAtIndexFixedLength(int rowIndex, int colIndex)
	{
		int effectiveIndex = rowIndex * nCols + colIndex;
		int lengthOfCellValue = lengthOfCell[effectiveIndex];

		char[] cellWord;
		// if (lengthOfCellValue == -1)
		// {// if empty cell
		// // cellWord = null;//
		// lengthOfCellValue = 0;
		// }

		cellWord = new char[lengthOfCellValue];

		for (int k = 0; k < lengthOfCellValue; k++)
		{
			cellWord[k] = threeDCharMatrix[effectiveIndex][k];
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

	/**
	 * 
	 */
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
	 * @return
	 */
	public String toStringActualLength()
	{
		StringBuilder sb = new StringBuilder(
				"----- nRows:" + nRows + ", nCols:" + nCols + ", maxSizeOfCell:" + maxSizeOfCell + " -----\n");
		for (int rowIndex = 0; rowIndex < nRows; rowIndex++)
		{
			for (int colIndex = 0; colIndex < nCols; colIndex++)
			{
				String cellAtIndex = String.valueOf(getCellAtIndex(rowIndex, colIndex));
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
	public void resetLengthOfCells()
	{
		for (int i = 0; i < nRows; i++)
		{
			int iEff = i * nCols;
			for (int j = 0; j < nCols; j++)
			{
				lengthOfCell[iEff + j] = 0;
			}
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(lengthOfCell);
		result = prime * result + maxSizeOfCell;
		result = prime * result + nCols;
		result = prime * result + nRows;
		result = prime * result + Arrays.deepHashCode(threeDCharMatrix);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TraceMatrixLeaner1 other = (TraceMatrixLeaner1) obj;
		if (!Arrays.equals(lengthOfCell, other.lengthOfCell)) return false;
		if (maxSizeOfCell != other.maxSizeOfCell) return false;
		if (nCols != other.nCols) return false;
		if (nRows != other.nRows) return false;
		if (!Arrays.deepEquals(threeDCharMatrix, other.threeDCharMatrix)) return false;
		return true;
	}
	// Disabled below this

	// /**
	// *
	// * @param rowIndex
	// * @param colIndex
	// * @param charArray
	// * @param charsToAdd
	// */
	// public void addCharsToCell2(int rowIndex, int colIndex, char[] c1Array, char c2, char c3, char c4, int i5, char
	// c6,
	// int i7, char c8)
	// {
	// // System.out.println("--- entering addCharsToCell with (" + rowIndex + "," + colIndex + "), adding"
	// // + String.valueOf(c1Array) + "," + c2 + "," + c3 + "," + c4 + "," + i5 + "," + c6 + "," + i7 + "," + c8
	// // + "\n traceMatrix:\n" + this.toString());
	// // int rowIndexEff = rowIndex * nCols;
	// int effectiveIndex = rowIndex * nCols + colIndex;
	// int currLengthOfThisCell = lengthOfCell[effectiveIndex];
	//
	// // System.out.println("currLengthOfThisCell at begin = " + currLengthOfThisCell);
	// try
	// {
	// // if (c1Array != null) {
	// for (char charToAdd : c1Array)
	// {
	// threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = charToAdd;
	// }
	// // System.out.println("currLengthOfThisCell after c1 = " + currLengthOfThisCell);// 63
	//
	// // }
	//
	// threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c2;
	// // 64
	// threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c3;
	//
	// threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c4;
	// // 66
	// for (char charToAdd : IntegerUtils.intToCharArray(i5))// Integer.toString(i5).toCharArray())
	// {
	// threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = charToAdd;
	// }
	//
	// threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = c6;
	//
	// for (char charToAdd : IntegerUtils.intToCharArray(i7))// Integer.toString(i7).toCharArray())
	// {
	// threeDCharMatrix[effectiveIndex][currLengthOfThisCell++] = charToAdd;
	// }
	//
	// // System.out.println("currLengthOfThisCell at c8 = " + currLengthOfThisCell);
	// threeDCharMatrix[effectiveIndex][currLengthOfThisCell] = c8;
	//
	// // System.out.println("currEndPointOfThisCell = " + currEndPointOfThisCell);
	// lengthOfCell[effectiveIndex] = currLengthOfThisCell + 1;
	//
	// // System.out.println(
	// // "--- exiting addCharsToCell with" + String.valueOf(c1Array) + "," + c2 + "," + c3 + "," + c4 + ","
	// // + i5 + "," + c6 + "," + i7 + "," + c8 + "\n traceMatrix:\n" + this.toStringActualLength());
	//
	// }
	// catch (Exception e)
	// {
	// System.out.println("nRows= " + nRows + ", nCols= " + nCols);// + ", maxSizeOfCell = " + maxSizeOfCell);// );
	// // System.out.println("traceMatrix:\n" + this.toString());
	// // $$ System.err.println(("Error in org.activity.objects.TraceMatrix.addCharsToCell() , rowIndex=" +
	// // rowIndex
	// // + ", colIndex=" + colIndex + "\n traceMatrix:\n" + this.toStringActualLength()));
	// e.printStackTrace();
	// System.exit(-1);
	// }
	//
	// }
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
