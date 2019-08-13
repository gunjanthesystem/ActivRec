package org.activity.nn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Fork of CharacterIterator_WC1
 * <p>
 * 
 * Modified by Gunjan to gain understanding
 * <p>
 * A simple DataSetIterator for use in the LSTMCharModellingExample. Given a text file and a few options, generate
 * feature vectors and labels for training, where we want to predict the next character in the sequence.<br>
 * This is done by randomly choosing a position in the text file, at offsets of 0, exampleLength, 2*exampleLength, etc
 * to start each sequence. Then we convert each character to an index, i.e., a one-hot vector. Then the character 'a'
 * becomes [1,0,0,0,...], 'b' becomes [0,1,0,0,...], etc
 *
 * Feature vectors and labels are both one-hot vectors of same length
 * 
 * @author Alex Black
 */
public class CharIteratorJun2018 implements DataSetIterator
{
	// all possible (valid) characters
	private char[] allPossibleChars;

	// All characters of the input file (after filtering to only those that are valid
	private char[] trainingString;

	// Maps each character to an index ind the input/output
	private Map<Character, Integer> charToIndexMap;

	// Length of each example/minibatch (number of characters)
	private int exampleLength;

	// Size of each minibatch (number of examples)
	private int miniBatchSize;

	private static Random rng = new Random(12);

	// Offsets for the start of each example
	private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

	private boolean shuffleOffsetsForExamples = false;

	public List<Character> getAllPossCharsAsOrderedList()
	{
		List<Character> allPosCharacters = new ArrayList<Character>();
		for (char c : allPossibleChars)
		{
			allPosCharacters.add(c);
		}
		return allPosCharacters;
	}

	@Override
	public String toString()
	{
		String trainingStringToPrint = trainingString.length > 1000
				? "long training string of sizer( >1000) = " + trainingString.length
				: Arrays.toString(trainingString);

		return "CharIteratorJun2018 [\nallPossibleChars=" + Arrays.toString(allPossibleChars) + ", \ntrainingString="
				+ trainingStringToPrint + ", \ncharToIndexMap=" + charToIndexMap + ", \nexampleLength=" + exampleLength
				+ ", \nminiBatchSize=" + miniBatchSize + ", \nexampleStartOffsets=" + exampleStartOffsets
				+ ", \nshuffleOffsetsForExamples=" + shuffleOffsetsForExamples + "]";
	}

	/**
	 * @param trainingString
	 * 
	 * @param miniBatchSize
	 *            Number of examples per mini-batch
	 * @param exampleLength
	 *            Number of characters in each input/output vector
	 * @param verbose
	 * @throws IllegalArgumentException
	 */
	public CharIteratorJun2018(char[] trainingString, int miniBatchSize, int exampleLength, boolean verbose)
			// , char[] validCharacters)
			throws IllegalArgumentException
	{
		StringBuilder sb = new StringBuilder("---->Starting CharIteratorJun2018()");

		if (miniBatchSize <= 0) throw new IllegalArgumentException("Invalid miniBatchSize (must be >0)");

		this.trainingString = trainingString;
		this.allPossibleChars = extractAllPossibleChars(trainingString, verbose);// validCharacters;
		this.exampleLength = exampleLength;
		this.miniBatchSize = miniBatchSize;
		// this.rng = rng;

		// Store valid characters in a map for later use in vectorization
		charToIndexMap = new HashMap<>();
		for (int i = 0; i < allPossibleChars.length; i++)
		{
			charToIndexMap.put(allPossibleChars[i], i);
		}

		sb.append("\ncharToIndexMap.size()=" + charToIndexMap.size() + "\ncharToIndexMap set =\n"
				+ charToIndexMap.toString() + "\n--\n");
		sb.append("trainingString.length = " + trainingString.length + "= max size\n");

		if (exampleLength >= trainingString.length) throw new IllegalArgumentException("exampleLength=" + exampleLength
				+ " cannot exceed trainingString.length (" + trainingString.length + ")");

		sb.append("Will call initializeOffsets()\n");
		System.out.println(sb.toString());

		initializeOffsets(this.shuffleOffsetsForExamples);

		System.out.println("Exiting CharIteratorJun2018()");
	}

	public char convertIndexToCharacter(int idx)
	{
		return allPossibleChars[idx];
	}

	public int convertCharacterToIndex(char c)
	{
		return charToIndexMap.get(c);
	}

	public char getRandomCharacter()
	{
		return allPossibleChars[(int) (rng.nextDouble() * allPossibleChars.length)];
	}

	public boolean hasNext()
	{
		return exampleStartOffsets.size() > 0;
	}

	public DataSet next()
	{
		return next(miniBatchSize);
	}

	/**
	 * Create a new Dataset with inputs (numOfExamples,numOfValidChars,lenghtOfExample) and labels
	 * (numOfExamples,numOfValidChars,lenghtOfExample) tensors.
	 * <p>
	 * Like the standard next method but allows a customizable number of examples returned
	 * <p>
	 * Specified by: next(...) in DataSetIterator
	 * 
	 * @param numOfExamples
	 * 
	 */
	public DataSet next(int numOfExamples)
	{
		StringBuilder sb = new StringBuilder(
				"------>Inside CharacterIterator_WC1.next(int) with miniBatchSize= " + numOfExamples + "\n");

		if (exampleStartOffsets.size() == 0) throw new NoSuchElementException();

		int currMinibatchSize = Math.min(numOfExamples, exampleStartOffsets.size());
		sb.append("exampleStartOffsets.size()=" + exampleStartOffsets.size()
				+ "\ncurrMinibatchSize (number of examples in minibatch)=" + currMinibatchSize
				+ "\nWill now allocate memory space by created INDArrays for inputs and labels\n");

		// Allocate space:
		// Note the order here:
		// dimension 0 = number of examples in minibatch
		// dimension 1 = size of each vector (i.e., number of characters)
		// dimension 2 = length of each time series/example
		// Why 'f' order here? See http://deeplearning4j.org/usingrnns.html#data section "Alternative: Implementing a
		// custom DataSetIterator"
		INDArray input = Nd4j.create(new int[] { currMinibatchSize, allPossibleChars.length, exampleLength }, 'f');
		INDArray labels = Nd4j.create(new int[] { currMinibatchSize, allPossibleChars.length, exampleLength }, 'f');

		sb.append("Shape of empty input tensor=" + Arrays.toString(input.shape()) + "\t");
		sb.append("Shape of empty labels tensor=" + Arrays.toString(labels.shape())
				+ "\n Will now fill up the tensors by looping");

		for (int miniBatchIndex = 0; miniBatchIndex < currMinibatchSize; miniBatchIndex++)
		{

			int startIdx = exampleStartOffsets.removeFirst();
			int endIdx = startIdx + exampleLength;
			int currCharIdx = charToIndexMap.get(trainingString[startIdx]); // Current input
			int c = 0;

			sb.append("\n-- miniBatchIndex= " + miniBatchIndex + "\t startIdx=" + startIdx + " endIdx=" + endIdx
					+ " currCharIdx=" + currCharIdx);

			for (int j = startIdx + 1; j < endIdx; j++, c++)
			{
				int nextCharIdx = charToIndexMap.get(trainingString[j]); // Next character to predict
				input.putScalar(new int[] { miniBatchIndex, currCharIdx, c }, 1.0);
				labels.putScalar(new int[] { miniBatchIndex, nextCharIdx, c }, 1.0);
				currCharIdx = nextCharIdx;
			}
		}

		// sb.append("\ninput=\n" + input.toString());
		// sb.append("\nlables=\n" + labels.toString() + "\n");

		sb.append("\n\tShape of input tensor=" + Arrays.toString(input.shape()) + "\n");
		sb.append("\tShape of labels tensor=" + Arrays.toString(labels.shape()) + "\n");
		System.out.println(sb.toString() + "\n------>Exiting CharacterIterator_WC1.next(int)\n");

		return new DataSet(input, labels);
	}

	public int totalExamples()
	{
		return (trainingString.length - 1) / miniBatchSize - 2;
	}

	public int inputColumns()
	{
		return allPossibleChars.length;
	}

	public int totalOutcomes()
	{
		return allPossibleChars.length;
	}

	public void reset()
	{
		exampleStartOffsets.clear();
		initializeOffsets(this.shuffleOffsetsForExamples);
	}

	/**
	 * 
	 * @param shuffleOffsets
	 */
	private void initializeOffsets(boolean shuffleOffsets)
	{
		StringBuilder sb = new StringBuilder(
				"------>Inside initializeOffsets: This defines the order in which parts of the file are fetched. trainingString.length="
						+ trainingString.length + " exampleLength=" + exampleLength);
		// This defines the order in which parts of the file are fetched
		int nMinibatchesPerEpoch = (trainingString.length - 1) / exampleLength - 2; // -2: for end index, and for
																					// partial example

		sb.append("\ntrainingString.length=" + trainingString.length + "\nexampleLength=" + exampleLength
				+ "\nnMinibatchesPerEpoch= " + nMinibatchesPerEpoch
				+ "= (trainingString.length - 1) / exampleLength - 2 \n");

		for (int i = 0; i < nMinibatchesPerEpoch; i++)
		{
			exampleStartOffsets.add(i * exampleLength);
		}

		sb.append("Offsets for the start of each example (before shuffling)= exampleStartOffsets.size= "
				+ exampleStartOffsets.size() + "\n" + exampleStartOffsets + "\n");

		if (shuffleOffsets)
		{
			Collections.shuffle(exampleStartOffsets, rng);
			sb.append("Shuffling offsets\n");
		}
		else
		{
			sb.append("No shuffling of offsets\n");
		}

		System.out.println(sb.toString() + "------>Exiting initializeOffsets");
	}

	public char[] getAllPossibleChars()
	{
		return allPossibleChars;
	}

	/**
	 * Sets trainingString and allPossibleChars
	 * <p>
	 *
	 * 
	 * @param trainingString
	 * @param verbose
	 * @return
	 */
	public static char[] extractAllPossibleChars(char[] trainingString, boolean verbose)
	{
		Set<Character> setOfUniqueChars = new TreeSet<>();

		for (char ch : trainingString)
		{
			setOfUniqueChars.add(ch);
		}

		// create a dedicated list of possible chars in allPossibleChars
		char[] allPossibleChars = new char[setOfUniqueChars.size()];
		int index = 0;
		for (char ch : setOfUniqueChars)
		{
			allPossibleChars[index++] = ch;
		}

		if (verbose)
		{
			System.out.println("allPossibleChars(): character List = " + Arrays.toString(allPossibleChars));
		}
		return allPossibleChars;
	}

	/**
	 * 
	 * @param testString
	 * @param allPossibleChars
	 * @param verbose
	 * @return
	 */
	public static boolean validCharsInTestString(char[] testString, Set<Character> allPossibleChars, boolean verbose)
	{
		List<Character> invalidChars = new ArrayList<>();

		for (char ch : testString)
		{
			Character chO = new Character(ch);
			if (allPossibleChars.contains(chO) == false)
			{
				invalidChars.add(chO);
			}
		}

		return invalidChars.size() > 0 ? false : true;
	}

	public boolean resetSupported()
	{
		return true;
	}

	@Override
	public boolean asyncSupported()
	{
		return true;
	}

	public int batch()
	{
		return miniBatchSize;
	}

	public int cursor()
	{
		return totalExamples() - exampleStartOffsets.size();
	}

	public int numExamples()
	{
		return totalExamples();
	}

	public void setPreProcessor(DataSetPreProcessor preProcessor)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public DataSetPreProcessor getPreProcessor()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public List<String> getLabels()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
