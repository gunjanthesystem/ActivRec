package org.activity.nn;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

/**
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
public class CharacterIterator_WC1 implements DataSetIterator
{
	// Valid characters
	private char[] validCharacters;

	// Maps each character to an index ind the input/output
	private Map<Character, Integer> charToIndexMap;

	// All characters of the input file (after filtering to only those that are valid
	private char[] fileCharacters;

	// Length of each example/minibatch (number of characters)
	private int exampleLength;

	// Size of each minibatch (number of examples)
	private int miniBatchSize;

	private Random rng;

	// Offsets for the start of each example
	private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

	/**
	 * @param textFilePath
	 *            Path to text file to use for generating samples
	 * @param textFileEncoding
	 *            Encoding of the text file. Can try Charset.defaultCharset()
	 * @param miniBatchSize
	 *            Number of examples per mini-batch
	 * @param exampleLength
	 *            Number of characters in each input/output vector
	 * @param validCharacters
	 *            Character array of valid characters. Characters not present in this array will be removed
	 * @param rng
	 *            Random number generator, for repeatability if required
	 * @throws IOException
	 *             If text file cannot be loaded
	 */
	public CharacterIterator_WC1(String textFilePath, Charset textFileEncoding, int miniBatchSize, int exampleLength,
			char[] validCharacters, Random rng) throws IOException
	{
		this(textFilePath, textFileEncoding, miniBatchSize, exampleLength, validCharacters, rng, null);
	}

	/**
	 * @param textFilePath
	 *            Path to text file to use for generating samples
	 * @param textFileEncoding
	 *            Encoding of the text file. Can try Charset.defaultCharset()
	 * @param miniBatchSize
	 *            Number of examples per mini-batch
	 * @param exampleLength
	 *            Number of characters in each input/output vector
	 * @param validCharacters
	 *            Character array of valid characters. Characters not present in this array will be removed
	 * @param rng
	 *            Random number generator, for repeatability if required
	 * @param commentChars
	 *            if non-null, lines starting with this string are skipped.
	 * @throws IOException
	 *             If text file cannot be loaded
	 */
	public CharacterIterator_WC1(String textFilePath, Charset textFileEncoding, int miniBatchSize, int exampleLength,
			char[] validCharacters, Random rng, String commentChars) throws IOException
	{
		StringBuilder sb = new StringBuilder("---->Starting CharacterIterator_WC1()");

		if (!new File(textFilePath).exists())
			throw new IOException("Could not access file (does not exist): " + textFilePath);
		if (miniBatchSize <= 0) throw new IllegalArgumentException("Invalid miniBatchSize (must be >0)");

		this.validCharacters = validCharacters;
		this.exampleLength = exampleLength;
		this.miniBatchSize = miniBatchSize;
		this.rng = rng;

		// Store valid characters in a map for later use in vectorization
		charToIndexMap = new HashMap<>();
		for (int i = 0; i < validCharacters.length; i++)
		{
			charToIndexMap.put(validCharacters[i], i);
		}

		sb.append("\ncharToIndexMap.size()=" + charToIndexMap.size() + "\ncharToIndexMap set =\n"
				+ charToIndexMap.toString() + "\n--\n");

		// Load file and convert contents to a char[]
		boolean newLineValid = charToIndexMap.containsKey('\n');
		List<String> lines = Files.readAllLines(new File(textFilePath).toPath(), textFileEncoding);

		sb.append("#lines read from raw file = " + lines.size() + "\n");

		// Ignore commented lines
		if (commentChars != null)
		{
			List<String> withoutComments = new ArrayList<>();
			for (String line : lines)
			{
				if (!line.startsWith(commentChars))
				{
					withoutComments.add(line);
				}
			}
			lines = withoutComments;
		}

		int maxSize = lines.size(); // add lines.size() to account for newline characters at end of each line

		for (String s : lines)
		{
			maxSize += s.length();
		}

		sb.append("#lines ignoring commented lines read from raw file = " + lines.size() + " maxSize = " + maxSize
				+ "\n");

		char[] characters = new char[maxSize];
		int index = 0;

		sb.append("Looping over all lines\n");

		for (String s : lines)
		{
			char[] thisLine = s.toCharArray();
			for (char charInThisLine : thisLine)
			{
				if (!charToIndexMap.containsKey(charInThisLine))
				{
					continue;
				}
				characters[index++] = charInThisLine;
			}

			if (newLineValid) characters[index++] = '\n';
		}

		sb.append("character.length()=" + characters.length + "\n");

		if (index == characters.length)
		{
			fileCharacters = characters;
		}
		else
		{
			fileCharacters = Arrays.copyOfRange(characters, 0, index);
		}

		sb.append("fileCharacters.length=" + fileCharacters.length + "\n");

		if (exampleLength >= fileCharacters.length) throw new IllegalArgumentException("exampleLength=" + exampleLength
				+ " cannot exceed number of valid characters in file (" + fileCharacters.length + ")");

		int nRemoved = maxSize - fileCharacters.length;
		System.out.println("Loaded and converted file: " + fileCharacters.length + " valid characters of " + maxSize
				+ " total characters (" + nRemoved + " removed)");

		sb.append("Will call initializeOffsets()\n");
		System.out.println(sb.toString());

		initializeOffsets();

		System.out.println("Exiting CharacterIterator_WC1()");
	}

	/** A minimal character set, with a-z, A-Z, 0-9 and common punctuation etc */
	public static char[] getMinimalCharacterSet()
	{
		List<Character> validChars = new LinkedList<>();
		for (char c = 'a'; c <= 'z'; c++)
			validChars.add(c);
		for (char c = 'A'; c <= 'Z'; c++)
			validChars.add(c);
		for (char c = '0'; c <= '9'; c++)
			validChars.add(c);
		char[] temp = { '!', '&', '(', ')', '?', '-', '\'', '"', ',', '.', ':', ';', ' ', '\n', '\t' };
		for (char c : temp)
			validChars.add(c);
		char[] out = new char[validChars.size()];
		int i = 0;
		for (Character c : validChars)
			out[i++] = c;
		return out;
	}

	/** As per getMinimalCharacterSet(), but with a few extra characters */
	public static char[] getDefaultCharacterSet()
	{
		List<Character> validChars = new LinkedList<>();
		for (char c : getMinimalCharacterSet())
			validChars.add(c);
		char[] additionalChars = { '@', '#', '$', '%', '^', '*', '{', '}', '[', ']', '/', '+', '_', '\\', '|', '<',
				'>' };
		for (char c : additionalChars)
			validChars.add(c);
		char[] out = new char[validChars.size()];
		int i = 0;
		for (Character c : validChars)
			out[i++] = c;
		return out;
	}

	public char convertIndexToCharacter(int idx)
	{
		return validCharacters[idx];
	}

	public int convertCharacterToIndex(char c)
	{
		return charToIndexMap.get(c);
	}

	public char getRandomCharacter()
	{
		return validCharacters[(int) (rng.nextDouble() * validCharacters.length)];
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
		INDArray input = Nd4j.create(new int[] { currMinibatchSize, validCharacters.length, exampleLength }, 'f');
		INDArray labels = Nd4j.create(new int[] { currMinibatchSize, validCharacters.length, exampleLength }, 'f');

		sb.append("Shape of empty input tensor=" + Arrays.toString(input.shape()) + "\t");
		sb.append("Shape of empty labels tensor=" + Arrays.toString(labels.shape())
				+ "\n Will now fill up the tensors by looping");

		for (int miniBatchIndex = 0; miniBatchIndex < currMinibatchSize; miniBatchIndex++)
		{

			int startIdx = exampleStartOffsets.removeFirst();
			int endIdx = startIdx + exampleLength;
			int currCharIdx = charToIndexMap.get(fileCharacters[startIdx]); // Current input
			int c = 0;

			sb.append("\n-- miniBatchIndex= " + miniBatchIndex + "\t startIdx=" + startIdx + " endIdx=" + endIdx
					+ " currCharIdx=" + currCharIdx);

			for (int j = startIdx + 1; j < endIdx; j++, c++)
			{
				int nextCharIdx = charToIndexMap.get(fileCharacters[j]); // Next character to predict
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
		return (fileCharacters.length - 1) / miniBatchSize - 2;
	}

	public int inputColumns()
	{
		return validCharacters.length;
	}

	public int totalOutcomes()
	{
		return validCharacters.length;
	}

	public void reset()
	{
		exampleStartOffsets.clear();
		initializeOffsets();
	}

	private void initializeOffsets()
	{
		StringBuilder sb = new StringBuilder(
				"------>Inside initializeOffsets: This defines the order in which parts of the file are fetched.");
		// This defines the order in which parts of the file are fetched
		int nMinibatchesPerEpoch = (fileCharacters.length - 1) / exampleLength - 2; // -2: for end index, and for
																					// partial example

		sb.append("\nfileCharacters.length=" + fileCharacters.length + "\nexampleLength=" + exampleLength
				+ "\nnMinibatchesPerEpoch= " + nMinibatchesPerEpoch
				+ "= (fileCharacters.length - 1) / exampleLength - 2 \n");

		for (int i = 0; i < nMinibatchesPerEpoch; i++)
		{
			exampleStartOffsets.add(i * exampleLength);
		}

		sb.append("Offsets for the start of each example (before shuffling)= exampleStartOffsets.size= "
				+ exampleStartOffsets.size() + "\n" + exampleStartOffsets + "\n");

		System.out.println(sb.toString());
		Collections.shuffle(exampleStartOffsets, rng);
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
