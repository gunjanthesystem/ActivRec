package org.activity.nn;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.controller.SuperController;
import org.activity.io.WToFile;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.apache.commons.math3.primes.Primes;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Precision;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 * Fork of org.activity.nn.BasicRNNWC2_SeqRec2018 and modified with inspiration from
 * org.activity.nn.LSTMCharModellingExample_WC1
 * <p>
 * This example trains a RNN. When trained we only have to put the first character of trainingString to the RNN, and it
 * will recite the following chars
 * 
 * @author gunjan
 * @since 25 June 2018
 */
public class LSTMCharModelling_SeqRecJun2018
{
	// private char[] trainingString;
	private char[] testString;

	PrimaryDimension givenDimension;
	// delegating this to dataset iterator
	// private final List<Character> allPossibleChars;// = new ArrayList<>(); // a list of all possible characters

	// LSTM dimensions
	// replace by variable width hidden layers set by numOfUnitsInHiddenLayersInOrder on 5 July 2018
	// private int HIDDEN_LAYER_WIDTH;// = 200;// Number of units in each LSTM layer, lstmLayerSize
	// private int HIDDEN_LAYER_COUNT;// = 2;

	private int[] numOfUnitsInHiddenLayersInOrder;

	private static final Random r = new Random(7894);

	///////////////////
	int miniBatchSize;// = 32; // Size of mini batch to use when training
	int exampleLength;// = 1000; // Length of each training example sequence to use. This could certainly be increased
	int tbpttLength;// = 50; // Length for truncated backpropagation through time. i.e., do parameter updates ever 50
					// characters
	int numEpochs;// = 1; // Total number of training epochs
	///////////////////

	MultiLayerNetwork net;

	CharIteratorJun2018 iter;

	private static LinkedHashMap<String, LSTMCharModelling_SeqRecJun2018> lstmPredictorsPrimDimForEachUserStored = new LinkedHashMap<>();
	private static LinkedHashMap<String, LSTMCharModelling_SeqRecJun2018> lstmPredictorsSecDimForEachUserStored = new LinkedHashMap<>();

	public static final LSTMCharModelling_SeqRecJun2018 getLSTMPredictorsForEachUserStored(String userID)
	{
		return lstmPredictorsPrimDimForEachUserStored.get(userID);
	}

	/**
	 * delete all stored lstmPredictorsForEachUserStored because the stored users wont be involved anymore. This is
	 * because this class was becoming too heavy and we need to make it light.
	 */
	public static final void clearLSTMPredictorsForEachUserStored()
	{
		lstmPredictorsPrimDimForEachUserStored.clear();
		lstmPredictorsSecDimForEachUserStored.clear();
	}

	public static final String getRNN1Label()
	{
		return Arrays.stream(Constant.neuronsInHiddenLayersRNN1).mapToObj(i -> String.valueOf(i))
				.collect(Collectors.joining("_")) + "_HL" + Constant.numOfTrainingEpochsInRNN1 + "Epochs"
				+ String.valueOf(Constant.learningRateInRNN1).replace("0.", "") + "LR" + Constant.exampleLengthInRNN1
				+ "ln";
	}

	/**
	 * 
	 * @param fileLocation
	 * @param commentChars
	 * @param limit
	 * @return
	 */
	private static char[] getCharArrayFromFile(String fileLocation, int limit)
	{
		Charset textFileEncoding = Charset.defaultCharset();
		StringBuilder sb = new StringBuilder();

		try
		{
			List<String> lines = Files.readAllLines(new File(fileLocation).toPath(), textFileEncoding);
			sb.append("#lines read from raw file = " + lines.size() + "\n");

			int maxSize = 0;// lines.size(); // add lines.size() to account for newline characters at end of each line
			for (String s : lines)
			{
				maxSize += s.length();
			}

			int arrayCapacity = (limit != -1) ? limit : maxSize;

			sb.append("#lines read from raw file = " + lines.size() + " maxSize = " + maxSize + " arrayCapacity = "
					+ arrayCapacity + "\n");

			char[] resultantCharArray = new char[arrayCapacity];

			sb.append("Looping over all lines\n");
			int index = 0;

			for (String s : lines)
			{
				char[] charsInThisLine = s.toCharArray();
				for (char charInThisLine : charsInThisLine)
				{
					// if (charInThisLine == '\n')
					// {
					// System.out.println("ALERT!! Skipping newline");
					// continue;
					// }
					resultantCharArray[index++] = charInThisLine;

					if (index == limit)
					{
						break;
					}
				}
				if (index == limit)
				{
					break;
				}
				// resultantCharArray[index++] = '\n';// add newline as line separator
			}

			sb.append("character.length()=" + resultantCharArray.length + " " + "\n");
			System.out.println(sb.toString());
			return resultantCharArray;
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}
		return null;
	}

	public LSTMCharModelling_SeqRecJun2018(int numOfNeuronsInHiddenLayer, int numOfHiddenLayers)
	{
		// HIDDEN_LAYER_WIDTH = numOfNeuronsInHiddenLayer;
		// HIDDEN_LAYER_COUNT = numOfHiddenLayers;

		numOfUnitsInHiddenLayersInOrder = new int[numOfHiddenLayers];
		for (int hl = 0; hl < numOfHiddenLayers; hl++)
		{
			numOfUnitsInHiddenLayersInOrder[hl] = numOfNeuronsInHiddenLayer;
		}
		// allPossibleChars = new ArrayList<>();
	}

	public LSTMCharModelling_SeqRecJun2018(int[] numOfUnitsInHiddenLayersInOrder)
	{
		this.numOfUnitsInHiddenLayersInOrder = numOfUnitsInHiddenLayersInOrder;
		// allPossibleChars = new ArrayList<>();
	}

	public static void main(String args[])
	{
		if (true)
		{
			SuperController.setupCUDAEnviron();
		}

		// String trainingStringNoRecentDa="TrainingStringNoRecentDayFilter.txt"

		// char text[] = getCharArrayFromTextWithValidEnglishCharsOnly("./dataToRead/TomSawyer.txt", null, 56000);
		char trainingCharArray[] = getCharArrayFromFile("./dataToRead/TrainingStringNoRecentDayFilter.txt", -1);// TrainingString
		String outputDirectory = "./dataWritten/RNNExperiments" + DateTimeUtils.getMonthDateLabel() + "/SeqRec"
				+ LSTMCharModelling_SeqRecJun2018.getRNN1Label() + DateTimeUtils.getMonthDateHourMinLabel();

		new File(outputDirectory).mkdirs();
		Constant.setCommonPath(outputDirectory + "/");

		PrintStream consoleLogStream = WToFile.redirectConsoleOutput(
				Constant.getCommonPath() + "ConsoleLog" + DateTimeUtils.getMonthDateHourMinLabel() + ".txt");

		System.out.println("read trainingCharArray.length" + trainingCharArray.length);
		if (true)
		{
			// print the last 10 characters for
			// System.out.println("read text= \n" + Arrays.toString(trainingCharArray));
			for (int i = trainingCharArray.length - 1; i > (trainingCharArray.length - 10); i--)
			{
				System.out.println("i=" + i + " char=" + trainingCharArray[i]);
			}
		}
		// System.exit(0);
		LSTMCharModelling_SeqRecJun2018 lstm1 = new LSTMCharModelling_SeqRecJun2018(Constant.neuronsInHiddenLayersRNN1,
				false, "none", Constant.numOfTrainingEpochsInRNN1, Constant.learningRateInRNN1,
				Constant.l2RegularisationCoeffRNN1, trainingCharArray);

		consoleLogStream.close();
		System.out.println("Completed");
	}

	public static void main1(String args[])
	{
		boolean verbose = true;

		int lstmLayerSize = 200; // Number of units in each LSTM layer
		int numOfLayers = 5;
		int miniBatchSize = 3; // Size of mini batch to use when training
		int exampleLength = -1; // Length of each training example sequence to use. This could certainly be increased
		int tbpttLength = 50; // Length for truncated backpropagation through time. i.e., do parameter updates ever 50
								// characters
		int numOfTrainingEpochs = 500; // Total number of training epochs

		int numOfNextCharactersToPredict = 5;

		// define a sentence to learn.
		// Add a special character at the beginning so the RNN learns the complete string and ends with the marker.
		// String trainingString =
		// "hellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelohellohelo";
		String trainingString = "umbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrellaumbrella";
		String testString = "um";
		// "*Der Cottbuser Postkutscher putzt den Cottbuser

		exampleLength = getSplitSize(trainingString.length(), 1, 0.10);
		System.out.println("exampleLength=" + exampleLength);
		// System.exit(0);
		// Postkutschkasten.";
		System.out.println("trainingString= " + trainingString);

		CharIteratorJun2018 iter = new CharIteratorJun2018(trainingString.toCharArray(), miniBatchSize, exampleLength,
				verbose);
		System.out.println("iter.toString=\n" + iter.toString());
		int nOut = iter.totalOutcomes();
		System.out.println("nOut= " + nOut);

		LSTMCharModelling_SeqRecJun2018 rnnA = new LSTMCharModelling_SeqRecJun2018(lstmLayerSize, numOfLayers);
		// rnnA.setAllPossibleChars(trainingString, verbose);

		// rnnA.setTestString("h".toCharArray(), verbose);
		rnnA.configureAndCreateRNN(0.001, 2, 0.001, iter);

		// DataSet trainingData = rnnA.createTrainingDataset(rnnA.trainingString, rnnA.getAllpossiblechars(), verbose);

		rnnA.trainTheNetwork(numOfTrainingEpochs, iter, verbose, false);

		// List<Character> predictions = rnnA.predictNextNValues2(numOfNextCharactersToPredict,
		// iter.getAllPossCharsAsOrderedList(), false, testString.toCharArray(), rnnA.net);

		// List<Character> predictions = rnnA.predictNextNValues3(iter, numOfNextCharactersToPredict, rnnA.net,
		// testString.toCharArray(), true);

		List<Character> predictions = predictNextNValues4(testString.toCharArray(), rnnA.net, iter,
				numOfNextCharactersToPredict, verbose, null, null, testString, false);

		System.out.println("Predicted seq = " + predictions);
		// true, rnnA.getTestString());
	}

	/**
	 * 
	 * @param givenData
	 * @param verbose
	 * @return
	 */
	public static char[] flattenList(ArrayList<ArrayList<Character>> givenData, boolean verbose)
	{
		List<Character> flattenedList = new ArrayList<>();

		for (ArrayList<Character> innerList : givenData)
		{
			for (Character ch : innerList)
			{
				flattenedList.add(ch);
			}
		}

		char[] res = new char[flattenedList.size()];
		for (int i = 0; i < flattenedList.size(); i++)
		{
			res[i] = flattenedList.get(i);
		}

		if (true) // verbose)
		{
			StringBuilder sb = new StringBuilder();
			int sizeOfGivenData = givenData.stream().mapToInt(l -> l.size()).sum();
			sb.append("sizeOfGivenData = " + sizeOfGivenData + " flattenedList.size()=" + flattenedList.size()
					+ " sane =" + (sizeOfGivenData == flattenedList.size()) + "\n");
			if (sizeOfGivenData <= 5000)
			{
				sb.append("givenData=\n" + givenData + "\nflattenedList=\n" + flattenedList + "\n");
			}
			System.out.println(sb.toString());
		}

		if (true)// temporarily write to file, one long string
		{
			StringBuilder sb = new StringBuilder();
			String fileToWrite = Constant.getCommonPath() + "TrainingStringNoRecentDayFilter.txt";
			// LeakyBucket lb = new LeakyBucket(5000, Constant.getCommonPath() + "TrainingString.txt", true);
			for (int i = 0; i < res.length; i++)
			{
				sb.append(res[i]);
				if (i % 5000 == 0)
				{
					WToFile.appendLineToFileAbs(sb.toString(), fileToWrite);
					sb.setLength(0);
				}
			}
			WToFile.appendLineToFileAbs(sb.toString(), fileToWrite);
			sb.setLength(0);
			System.exit(0);

		}

		return res;
	}

	/**
	 * to split one long sequence into multiple sequences
	 * 
	 * @param lengthOfSingleLongSeq
	 * @param thresholdForSmallerSize
	 * @param minSmallerSizeRatio
	 * @return
	 */
	public static int getSplitSize(int lengthOfSingleLongSeq, int thresholdForSmallerSize, double minSmallerSizeRatio)
	{
		System.out.println("Inside getSplitSize(): lengthOfSingleLongSeq= " + lengthOfSingleLongSeq);

		double smallerSplitApproxSize = minSmallerSizeRatio * lengthOfSingleLongSeq;
		System.out.println("smallerSplitApproxSize= " + smallerSplitApproxSize);
		// int thresholdForSmallerSize = 100;
		int smallerSizeSelected = lengthOfSingleLongSeq;

		if (Primes.isPrime(lengthOfSingleLongSeq))
		{
			System.out.println("------------------\nWARNING! Cannot split cleanly as lengthOfSingleLongSeq= "
					+ lengthOfSingleLongSeq
					+ " is PRIME!!\nDeleting 1 from lengthOfSingleLongSeq. Hence you MUST DELETE one character from the sequence to be split.\n------------------\n");
			lengthOfSingleLongSeq -= 1;
		}

		if (smallerSplitApproxSize <= thresholdForSmallerSize)
		{
			System.out.println("Warning!: probably u do not need to split as smallerSplitApproxSize= "
					+ smallerSplitApproxSize + " <=" + thresholdForSmallerSize);
		}
		else
		{
			int smallerSizeTemp = (int) Math.ceil(smallerSplitApproxSize);
			// find the smallerSize (<thresholdForSmallerSize), which is a divisor of lengthOfSingleLongSeq
			while (lengthOfSingleLongSeq % smallerSizeTemp != 0)
			{
				smallerSizeTemp += 1;
			}
			smallerSizeSelected = smallerSizeTemp;
			System.out.println("smallerSizeSelected=" + smallerSizeSelected);
		}
		return smallerSizeSelected;
	}

	/**
	 * 
	 * @param trainingString
	 * @param userID
	 * @param verbose
	 */
	public LSTMCharModelling_SeqRecJun2018(ArrayList<ArrayList<Character>> trainingString, String userID,
			boolean verbose)
	{

		this(Constant.neuronsInHiddenLayersRNN1, verbose, userID, Constant.numOfTrainingEpochsInRNN1,
				Constant.learningRateInRNN1, Constant.l2RegularisationCoeffRNN1,
				LSTMCharModelling_SeqRecJun2018.flattenList(trainingString, verbose));
	}

	// /**
	// *
	// * @param numOfNeuronsInHiddenLayer
	// * @param numOfHiddenLayers
	// * @param verbose
	// * @param userID
	// * @param numOfTrainingEpochs
	// * @param learningRate
	// * @param trainingString
	// */
	// public BasicRNNWC2_SeqRec2018(int numOfNeuronsInHiddenLayer, int numOfHiddenLayers, boolean verbose, String
	// userID,
	// int numOfTrainingEpochs, double learningRate, char[] trainingString, Set<Character> setOfAllPossibleChars)
	// {
	// this.allPossibleChars.addAll(setOfAllPossibleChars);
	//
	// }

	/**
	 * <ol>
	 * <li>Set paramaters</li>
	 * <li>Create char iterator</li>
	 * <li>configureAndCreateRNN</li>
	 * <li>trainTheNetwork</li>
	 * <li>store the RNN if suitable</li>
	 * </ol>
	 * 
	 * @param neuronsInEachHiddenLayerInOrder
	 * @param verbose
	 * @param userID
	 * @param numOfTrainingEpochs
	 * @param learningRate
	 * @param l2RegularisationCoeff
	 * @param trainingString
	 */
	public LSTMCharModelling_SeqRecJun2018(int[] neuronsInEachHiddenLayerInOrder, boolean verbose, String userID,
			int numOfTrainingEpochs, double learningRate, double l2RegularisationCoeff, char[] trainingString)
	{
		this(neuronsInEachHiddenLayerInOrder);// this(numOfNeuronsInHiddenLayer, numOfHiddenLayers);
		long t1 = System.currentTimeMillis();

		// Length of each training example sequence to use. This could certainly be increased
		int exampleLength = Constant.exampleLengthInRNN1;// 1000;// getSplitSize(trainingString.length, 1000, 0.02);
		int miniBatchSize = Constant.miniBatchSizeInRNN1;// 256; // Size of mini batch to use when training
		int lengthOfTBPTT = (int) (0.10 * exampleLength);// trainingString.length);

		// System.out.println("Inside LSTMCharModelling_SeqRecJun2018: neuronsInHiddenLayer= "
		// + Arrays.toString(neuronsInEachHiddenLayerInOrder) + " numOfTrainingEpochs=" + numOfTrainingEpochs
		// + " learningRate=" + learningRate + " userID=" + userID + " trainingString.length="
		// + trainingString.length + " exampleLength=" + exampleLength + " miniBatchSize=" + miniBatchSize
		// + " lengthOfTBPTT=" + lengthOfTBPTT);

		String nnConfig = "Hidden Layers: " + Arrays.toString(neuronsInEachHiddenLayerInOrder)
				+ "\nnumOfTrainingEpochs: " + numOfTrainingEpochs + "\nlearningRate: " + learningRate
				+ "\nl2Regularisation: " + l2RegularisationCoeff + "\nexampleLength: " + exampleLength
				+ "\nminiBatchSize: " + miniBatchSize + "\nlengthOfTBPTT: " + lengthOfTBPTT + "\nuserID: " + userID
				+ "\ntrainingString.length: " + trainingString.length + "\n";

		WToFile.appendLineToFileAbs(nnConfig, Constant.getCommonPath() + "NNConfig.csv");
		System.out.println("Inside LSTMCharModelling_SeqRecJun2018: " + nnConfig);

		// String nnConfig = "nHL:" + (net.getnLayers() - 2) + "neHL:" + Constant.numOfNeuronsInEachHiddenLayerInRNN1 +
		// "";
		// + " trainingString=\n" + new String(trainingString) + "\n");

		// TODO TEMP
		if (false)// temporarily trimming training data to last N characters only
		{
			int N = 50;
			char[] trimmedTrainingData = new char[N];
			for (int i = 0; i < N; i++)
			{
				trimmedTrainingData[i] = trainingString[i];
			}
			trainingString = trimmedTrainingData;
			System.out.println("WARNING! TRIMMING TRAINING STRING SIZE TO " + N);
		}

		try
		{
			this.iter = new CharIteratorJun2018(trainingString, miniBatchSize, exampleLength, verbose);
			int nOut = iter.totalOutcomes();
			System.out.println("iter.toString=\n" + iter.toString() + "\nnOut= " + nOut);

			// LSTMCharModelling_SeqRecJun2018 rnnA = new LSTMCharModelling_SeqRecJun2018(lstmLayerSize, numOfLayers);
			// rnnA.setAllPossibleChars(trainingString, verbose);

			// rnnA.setTestString("h".toCharArray(), verbose);
			this.configureAndCreateRNN(learningRate, lengthOfTBPTT, l2RegularisationCoeff, iter);

			// DataSet trainingData = rnnA.createTrainingDataset(rnnA.trainingString, rnnA.getAllpossiblechars(),
			// verbose);

			this.trainTheNetwork(numOfTrainingEpochs, iter, verbose, Constant.doVisualizationRNN1);

			// List<Character> predictions = rnnA.predictNextNValues2(numOfNextCharactersToPredict,
			// iter.getAllPossCharsAsOrderedList(), false, testString.toCharArray(), rnnA.net);

			// List<Character> predictions = rnnA.predictNextNValues3(iter, numOfNextCharactersToPredict, rnnA.net,
			// testString.toCharArray(), true);

			// List<Character> predictions = sampleCharactersFromNetwork2(testString, rnnA.net, iter,
			// numOfNextCharactersToPredict);
			//
			// System.out.println("Predicted seq = " + predictions);
			// true, rnnA.getTestString());

			///////////////////////

			// this.configureAndCreateRNN(learningRate, lengthOfTBPTT);
			// DataSet trainingData = this.createTrainingDataset(this.trainingString, this.getAllpossiblechars(),
			// verbose);
			// this.trainTheNetwork(numOfTrainingEpochs, trainingData, verbose);

			// be careful of storing this as this object can consume a lot of memory
			// storing the trained RNN
			if (Constant.sameRNNForAllRTsOfAUser || Constant.sameRNNForALLUsers)
			{
				lstmPredictorsPrimDimForEachUserStored.put(userID, this);
			}
			// rnnA.predictNextNValues2(8, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.printTracedErrorMsgWithExit("Error in RNN");
		}

		// better to exit than to continue with error
		System.out.println("Trained RNN for user " + userID + " in " + (System.currentTimeMillis() - t1) + "ms");
	}

	public CharIteratorJun2018 getIter()
	{
		return iter;
	}

	public void setIter(CharIteratorJun2018 iter)
	{
		this.iter = iter;
	}

	/**
	 * see: https://deeplearning4j.org/usingrnns
	 * 
	 * @param numOfTrainingEpochs
	 * @param iter
	 * @param verbose
	 * @param dovisualizationrnn1
	 *            org.activity.nn.LSTMCharModelling_SeqRecJun2018.trainTheNetwork(int, CharIteratorJun2018, boolean,
	 *            boolean)
	 */
	private void trainTheNetwork(int numOfTrainingEpochs, CharIteratorJun2018 iter, boolean verbose,
			boolean dovisualizationrnn1)
	{
		long t00Fit = System.currentTimeMillis();
		// StringBuilder sb = new StringBuilder();
		System.out.println("---> Inside trainTheNetwork called()\n allPossibleChars= "
				+ new String(iter.getAllPossibleChars()) + "\nWill start training now\n");

		// int generateSamplesEveryNMinibatches = 10; // How frequently to generate samples from the network?
		// String generationInitialization = null; // Optional character initialization; a random character is used if
		// null //int nCharactersToSample = 300; int nSamplesToGenerate = 2; int miniBatchNumber = 0;

		if (dovisualizationrnn1)
		{
			File statsFile = new File(
					Constant.getCommonPath() + "UIStorageSeqRec" + LSTMCharModelling_SeqRecJun2018.getRNN1Label()
							+ DateTimeUtils.getMonthDateHourMinLabel() + ".dl4j");

			if (statsFile.exists())
			{
				PopUps.showMessage("Warning in trainTheNetwork: " + statsFile + " already exists");
			}

			StatsStorage statsStorage = new FileStatsStorage(statsFile);
			net.setListeners(new StatsListener(statsStorage), new ScoreIterationListener(10));
		}

		for (int epochCount = 0; epochCount < numOfTrainingEpochs; epochCount++)
		{
			// long t1 = System.currentTimeMillis();
			System.out.println("========== epochCount = " + epochCount);

			int iterationCount = 0;

			while (iter.hasNext())
			{
				System.out.println(
						"--- Starting iteration : iterationCount = " + ++iterationCount + " = countOfCallsToNext");
				DataSet ds = iter.next();

				long t0Fit = System.currentTimeMillis();
				net.fit(ds);// train the data
				long t1Fit = System.currentTimeMillis();
				System.out.println("finished fit: " + (t1Fit - t0Fit) + " ms");
			}
			iter.reset(); // Reset iterator for another epoch
		}

		System.out.println("End of Training took: " + (System.currentTimeMillis() - t00Fit) + " ms");
		System.out.println("---> Exiting trainTheNetwork called()");
		// System.exit(0);// TODO
	}

	// /**
	// *
	// * @param N
	// * @param verbose
	// * @param testString
	// * @return
	// */
	// public List<Character> predictNextNValues2(int N, boolean verbose, ArrayList<Character> testString,
	// MultiLayerNetwork net)
	// {
	// char[] testStringArr = new char[testString.size()];
	// return predictNextNValues2(N, verbose, testStringArr, net);
	// }

	/**
	 * 
	 * @param N
	 * @param allPossibleChars
	 * @param verbose
	 * @param testString
	 * @param net
	 * @return
	 */
	public List<Character> predictNextNValues2(int N, List<Character> allPossibleChars, boolean verbose,
			char[] testString, MultiLayerNetwork net)
	{
		System.out.println("---> Entering predictNextNValues2():");
		List<Character> predVals = new ArrayList<>(N);

		StringBuilder sb = new StringBuilder();
		INDArray testInit = prepareTestInitDataset(testString, allPossibleChars, verbose);
		sb.append("testInit = " + testInit);

		// clear current stance from the last example
		net.rnnClearPreviousState();
		sb.append("cleared previous state");

		// System.out.print("prediction@Step1=" + LEARNSTRING_CHARS_LIST.get(sampledCharacterIdx));
		// now the net should guess N-1 more characters
		INDArray nextInput = testInit;
		for (int i = 0; i < N; i++)
		{
			// run one step -> IMPORTANT: rnnTimeStep() must be called, not
			// output()
			// the output shows what the net thinks what should come next
			INDArray output = net.rnnTimeStep(nextInput);

			// first process the last output of the network to a concrete
			// neuron, the neuron with the highest output has the highest
			// chance to get chosen
			int sampledCharacterIdx = Nd4j.getExecutioner().exec(new IMax(output), 1).getInt(0);

			char predicatedVal = allPossibleChars.get(sampledCharacterIdx);
			// print the chosen output
			sb.append("\n-- prediction@Step" + (i + 1) + "=" + predicatedVal);
			predVals.add(predicatedVal);

			// use the last output as input
			nextInput = Nd4j.zeros(allPossibleChars.size());
			nextInput.putScalar(sampledCharacterIdx, 1);
		}

		System.out.print(sb.toString() + "\n");
		System.out.println("---> Exiting predictNextNValues2()");
		return predVals;
	}

	/**
	 * 
	 * @param iter
	 * @param numSamples
	 * @param init
	 * @return 3d array {numOfSample,numOfUniqueCHars,lengthOfInitialisation}
	 */
	private static INDArray createInputTensor(CharIteratorJun2018 iter, int numSamples, char[] init)
	{
		INDArray initializationInput = Nd4j.zeros(numSamples, iter.inputColumns(), init.length);

		for (int lengthIndex = 0; lengthIndex < init.length; lengthIndex++)
		{
			int charIndex = iter.convertCharacterToIndex(init[lengthIndex]);
			for (int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++)
			{
				initializationInput.putScalar(new int[] { sampleIndex, charIndex, lengthIndex }, 1.0f);
			}
		}
		return initializationInput;
	}

	/**
	 * Fork of predictNextNValues3() mixed with elements from sampleCharactersFromNetwork2()
	 * 
	 * @param iter
	 * @param numOfNextCharactersToPredict
	 * @param net
	 * @param testString
	 * @param verbose
	 * @return
	 */
	public List<Character> predictNextNValues3(CharIteratorJun2018 iter, int numOfNextCharactersToPredict,
			MultiLayerNetwork net, char[] testString, boolean verbose)

	{
		int numSamples = 1, sampleIndex = 0;
		StringBuilder sbLog = new StringBuilder();
		List<Character> predVals = new ArrayList<>(numOfNextCharactersToPredict);

		try
		{
			System.out.println("---> Entering predictNextNValues3():");
			List<Character> allPossibleChars = iter.getAllPossCharsAsOrderedList();

			sbLog.append("\tinitialization=" + new String(testString) + "\n");
			sbLog.append(
					"allPossibleChars.size()= " + allPossibleChars.size() + " allPossibleChars=" + allPossibleChars);
			// INDArray testInit = prepareTestInitDataset(testString, allPossibleChars, verbose);
			// sb.append("testInit = " + testInit);

			INDArray testInit = createInputTensor(iter, numSamples, testString);
			sbLog.append("\tinitialization.shape()= " + Arrays.toString(testInit.shape()) + "\n");

			// clear current stance from the last example
			net.rnnClearPreviousState();
			sbLog.append("\tcleared previous state\t");

			// System.out.print("prediction@Step1=" + LEARNSTRING_CHARS_LIST.get(sampledCharacterIdx));
			// now the net should guess N-1 more characters
			INDArray nextInput = testInit;

			for (int i = 0; i < numOfNextCharactersToPredict; i++)
			{
				// run one step -> IMPORTANT: rnnTimeStep() must be called, not
				// output()
				// the output shows what the net thinks what should come next
				INDArray output = net.rnnTimeStep(nextInput);
				sbLog.append("\toutput.shape() =" + Arrays.toString(output.shape()) + "\n");
				output = output.tensorAlongDimension(output.size(2) - 1, 1, 0); // Gets the last time step output
				sbLog.append("\tAfter TAD: output.shape()=" + Arrays.toString(output.shape()) + "\n");

				double[] outputProbDistribution = new double[iter.totalOutcomes()];
				for (int charIndex = 0; charIndex < outputProbDistribution.length; charIndex++)
				{
					outputProbDistribution[charIndex] = output.getDouble(sampleIndex, charIndex);
				}
				Map<Character, Double> charProbMapSorted = NNUtils
						.sortByValueDescNoShuffle(NNUtils.getCharProbMap(outputProbDistribution, iter));

				sbLog.append("\tsampleIndex=" + sampleIndex + "\toutputProbDistribution.length="
						+ outputProbDistribution.length + "\toutputProbDistribution=\n"
						+ NNUtils.getCharProbForPrint(charProbMapSorted, 5) + "\n");

				// first process the last output of the network to a concrete
				// neuron, the neuron with the highest output has the highest
				// chance to get chosen
				int sampledCharacterIdx = Nd4j.getExecutioner().exec(new IMax(output), 1).getInt(0);

				char predicatedVal = allPossibleChars.get(sampledCharacterIdx);
				char predicatedVal2 = Collections
						.max(charProbMapSorted.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
				// print the chosen output
				sbLog.append("\n-- prediction@Step" + (i + 1) + ": predicatedVal= " + predicatedVal
						+ " predicatedVal2= " + predicatedVal2);

				predVals.add(predicatedVal);

				// use the last output as input
				nextInput = Nd4j.zeros(numSamples, allPossibleChars.size());
				// Prepare next time step input
				nextInput.putScalar(new int[] { sampleIndex, sampledCharacterIdx }, 1.0f);
				sbLog.append("\n\tnextInput.shape()=" + Arrays.toString(nextInput.shape()) + "\n");
				// nextInput.putScalar(//sampledCharacterIdx, 1.0f);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		finally
		{
			System.out.print(sbLog.toString() + "\n");
		}

		System.out.println("---> Exiting predictNextNValues3()");
		return predVals;
	}

	/**
	 * Fork and modification of sampleCharactersFromNetwork2()
	 * <p>
	 * 
	 * @param initialization
	 * @param net
	 * @param iter
	 * @param numOfNextCharactersToPredict
	 * @param timeAtRecommForLoggingOnly
	 * @param dateAtRecommForLoggingOnly
	 * @param userIDForLoggingOnly
	 * @param writeDistributionToFile
	 * 
	 * @return
	 */
	public List<Character> predictNextNValues5(char[] init, int numOfNextCharactersToPredict, boolean verbose,
			Date dateAtRecommForLoggingOnly, Time timeAtRecommForLoggingOnly, String userIDForLoggingOnly,
			boolean writeDistributionToFile)
	{
		return predictNextNValues4(init, this.net, this.iter, numOfNextCharactersToPredict, verbose,
				dateAtRecommForLoggingOnly, timeAtRecommForLoggingOnly, userIDForLoggingOnly, writeDistributionToFile);
	}

	/**
	 * Fork and modification of sampleCharactersFromNetwork2()
	 * <p>
	 * 
	 * @param init
	 * @param net
	 * @param iter
	 * @param numOfNextCharactersToPredict
	 * @param verbose
	 * @param timeAtRecommForLoggingOnly
	 * @param dateAtRecommForLoggingOnly
	 * @param userIDForLoggingOnly
	 * @param writeDistributionToFile
	 * @return
	 */
	private static List<Character> predictNextNValues4(char[] init, MultiLayerNetwork net, CharIteratorJun2018 iter,
			int numOfNextCharactersToPredict, boolean verbose, Date dateAtRecommForLoggingOnly,
			Time timeAtRecommForLoggingOnly, String userIDForLoggingOnly, boolean writeDistributionToFile)
	{
		int numSamples = 1;

		StringBuilder sbLog = new StringBuilder("---> Entering sampleCharactersFromNetwork\n");
		List<Character> allPossibleChars = iter.getAllPossCharsAsOrderedList();
		List<Character> predVals = new ArrayList<>(numOfNextCharactersToPredict);

		// Set up initialization. If no initialization: use a random character
		if (init.length == 0)
		{
			PopUps.showError("Empty initialisation!!");
		}

		sbLog.append("\tinitialization=" + new String(init) + "\n");

		//////////// Create input tensor from the initialization
		// char[] init = initialization.toCharArray();
		INDArray initializationInput = createInputTensor(iter, numSamples, init);
		sbLog.append("\tinitializationInput.shape()= " + Arrays.toString(initializationInput.shape()) + "\n");

		// Sample from network (and feed samples back into input) one character at a time (for all samples)
		// Sampling is done in parallel here
		net.rnnClearPreviousState();
		sbLog.append("\tcleared previous states.\n");

		INDArray output = net.rnnTimeStep(initializationInput);// first prediction as output tensor
		sbLog.append("\toutput.shape() (first prediction) =" + Arrays.toString(output.shape()) + "\n");
		output = output.tensorAlongDimension(output.size(2) - 1, 1, 0); // Gets the last time step output
		sbLog.append("\tAfter TAD: output.shape()=" + Arrays.toString(output.shape()) + "\n");

		for (int nextIndex = 0; nextIndex < numOfNextCharactersToPredict; nextIndex++)
		{
			// Set up next input (single time step) by sampling from previous output
			INDArray nextInput = Nd4j.zeros(numSamples, iter.inputColumns());
			sbLog.append("\n\t--nextIndex=" + nextIndex + "\tnextInput.shape()=" + Arrays.toString(nextInput.shape())
					+ "\n");

			// Output is a probability distribution. Sample from this for each example we want to generate, and add it
			// to the new input
			for (int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++)
			{
				double[] outputProbDistribution = new double[iter.totalOutcomes()];
				for (int charIndex = 0; charIndex < outputProbDistribution.length; charIndex++)
				{
					outputProbDistribution[charIndex] = output.getDouble(sampleIndex, charIndex);
				}

				///////////
				Map<Character, Double> charProbMap = NNUtils.getCharProbMap(outputProbDistribution, iter);
				if (writeDistributionToFile)
				{
					writePredictionProbabilityDistribution(charProbMap, dateAtRecommForLoggingOnly,
							timeAtRecommForLoggingOnly, nextIndex, userIDForLoggingOnly);
				}
				Map<Character, Double> charProbMapSorted = NNUtils.sortByValueDescNoShuffle(charProbMap);

				sbLog.append("\tsampleIndex=" + sampleIndex + "\toutputProbDistribution.length="
						+ outputProbDistribution.length + "\toutputProbDistribution=\n"
						+ NNUtils.getCharProbForPrint(charProbMapSorted, 5) + "\n");

				/////////////

				int sampledCharacterIdx = Nd4j.getExecutioner().exec(new IMax(output), 1).getInt(0);

				char predicatedVal = allPossibleChars.get(sampledCharacterIdx);
				char predicatedVal2 = Collections
						.max(charProbMapSorted.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
				// print the chosen output
				sbLog.append("\n-- prediction@Step" + (nextIndex + 1) + ": predicatedVal= " + predicatedVal
						+ " predicatedVal2= " + predicatedVal2);

				predVals.add(predicatedVal);

				///

				nextInput.putScalar(new int[] { sampleIndex, sampledCharacterIdx }, 1.0f); // Prepare next time step
																							// input
			}

			output = net.rnnTimeStep(nextInput); // Do one time step of forward pass
			sbLog.append("\toutput.shape()=" + Arrays.toString(output.shape()) + "\n");
		}

		System.out.println(sbLog.toString());

		return predVals;
	}

	/**
	 * Write prediction probability distribution to a file
	 * 
	 * @param charProbMap
	 * @param timeAtRecommForLoggingOnly
	 * @param dateAtRecommForLoggingOnly
	 * @param nextIndex
	 * @param userIDForLoggingOnly
	 */
	private static void writePredictionProbabilityDistribution(Map<Character, Double> charProbMap,
			Date dateAtRecommForLoggingOnly, Time timeAtRecommForLoggingOnly, int nextIndex,
			String userIDForLoggingOnly)
	{
		String absFileNameToWrite = Constant.getCommonPath() + "PredictionProbabilityDistribution.csv";
		int roundedToDecimals = 7;

		// if the file does not exist, write new file with header
		if (new File(absFileNameToWrite).isFile() == false)
		{
			String header = "UserID,DateAtRecomm,TimeAtRecomm,NextIndex,";
			header += charProbMap.entrySet().stream().map(e -> String.valueOf(e.getKey()))
					.collect(Collectors.joining(","));

			WToFile.writeToNewFile(header.toString() + ",MaxProb,MinProb,StdDev,FirstQuartile,ThirdQuartile\n",
					absFileNameToWrite);
		}

		StringBuilder sb = new StringBuilder(userIDForLoggingOnly + "," + dateAtRecommForLoggingOnly + ","
				+ timeAtRecommForLoggingOnly + "," + nextIndex);

		List<Double> probDistribution = charProbMap.entrySet().stream().map(e -> e.getValue())
				.collect(Collectors.toList());
		DescriptiveStatistics ds = StatsUtils.getDescriptiveStatistics(probDistribution);

		charProbMap.entrySet().stream()
				.forEachOrdered(e -> sb.append("," + Precision.round(e.getValue(), roundedToDecimals)));

		sb.append("," + Precision.round(ds.getMax(), roundedToDecimals) + ","
				+ Precision.round(ds.getMin(), roundedToDecimals) + ","
				+ Precision.round(ds.getStandardDeviation(), roundedToDecimals) + ","
				+ Precision.round(ds.getPercentile(50), roundedToDecimals) + ","
				+ Precision.round(ds.getPercentile(75), roundedToDecimals));

		WToFile.appendLineToFileAbs(sb.toString() + "\n", absFileNameToWrite);
	}

	/**
	 * 
	 * @param learningRate
	 * @param tbpttLength
	 *            length for truncate back propagation through time
	 * @param l2RegularisationCoeff
	 * 
	 * @param iter
	 */
	public void configureAndCreateRNN(double learningRate, int tbpttLength, double l2RegularisationCoeff,
			CharIteratorJun2018 iter)
	// int iterations, double learningRate, int numOfEpochs, boolean verbose)
	{
		System.out.println("---Inside configureAndCreateRNN() called");
		/////////////// Start of set common parameters for NN
		NeuralNetConfiguration.Builder nnConfigBuilder = new NeuralNetConfiguration.Builder();
		nnConfigBuilder.seed(123);
		nnConfigBuilder.l2(l2RegularisationCoeff);// 0.001);
		nnConfigBuilder.weightInit(WeightInit.XAVIER);
		nnConfigBuilder.updater(new RmsProp(learningRate));
		// nnConfigBuilder.biasInit(0);
		// nnConfigBuilder.miniBatch(false);

		/////////////// End of set common parameters for NN

		ListBuilder listOfConfigBuilder = nnConfigBuilder.list();

		/////////////// Start of configure the hidden layers iteratively
		// first difference, for rnns we need to use LSTM.Builder

		int numOfInputs = iter.inputColumns();
		int numOfOutputs = iter.totalOutcomes();
		System.out.println("numOfInputs= " + numOfInputs);

		// for (int layerIndex = 0; layerIndex < HIDDEN_LAYER_COUNT; layerIndex++)
		for (int hiddenLayerIndex = 0; hiddenLayerIndex < this.numOfUnitsInHiddenLayersInOrder.length; hiddenLayerIndex++)
		{
			int numOfUnitsInThisHiddenLayer = numOfUnitsInHiddenLayersInOrder[hiddenLayerIndex];
			LSTM.Builder hiddenLayerBuilder = new LSTM.Builder();
			// check if first hidden layer
			hiddenLayerBuilder.nIn(hiddenLayerIndex == 0 ? numOfInputs : numOfUnitsInThisHiddenLayer);
			hiddenLayerBuilder.nOut(numOfUnitsInThisHiddenLayer);
			// adopted activation function from LSTMCharModellingExample seems to work well with RNNs
			hiddenLayerBuilder.activation(Activation.TANH);
			listOfConfigBuilder.layer(hiddenLayerIndex, hiddenLayerBuilder.build());
		}
		/////////////// End of configure the hidden layers iteratively

		/////////////// Start of configure the output layer
		int numOfUnitInLastHiddenLayer = numOfUnitsInHiddenLayersInOrder[numOfUnitsInHiddenLayersInOrder.length - 1];

		// we need to use RnnOutputLayer for our RNN
		RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunction.MCXENT);
		// softmax normalizes the output neurons, the sum of all outputs is 1
		// this is required for our sampleFromDistribution-function
		outputLayerBuilder.activation(Activation.SOFTMAX);
		outputLayerBuilder.nIn(numOfUnitInLastHiddenLayer);// HIDDEN_LAYER_WIDTH);
		outputLayerBuilder.nOut(numOfOutputs);
		// listOfConfigBuilder.layer(HIDDEN_LAYER_COUNT, outputLayerBuilder.build());
		listOfConfigBuilder.layer(numOfUnitsInHiddenLayersInOrder.length, outputLayerBuilder.build());
		/////////////// End of configure the output layer

		// finish builder
		listOfConfigBuilder.pretrain(false);
		listOfConfigBuilder.backprop(true);

		// Start of added on 20 June
		if (tbpttLength > 0)// truncated backpropagation through time.
		{
			listOfConfigBuilder.backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength)
					.tBPTTBackwardLength(tbpttLength);
		}
		// End of added on 20 June

		// create network
		MultiLayerConfiguration conf = listOfConfigBuilder.build();

		// Set the multilayer network for the RNN
		net = new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(1));

		NNUtils.getNumberOfParameters(net, true);// not essential, just for info

		System.out.println("ANN configured and initialized\n---Exiting configureAndCreateRNN()");
	}

	/**
	 * 
	 * @param testString
	 * @param allPossibleChars
	 * @param verbose
	 * @return
	 */
	private INDArray prepareTestInitDataset(char[] testString, List<Character> allPossibleChars, boolean verbose)
	{
		System.out.println("prepareTestInitDataset(): testString =" + new String(testString));
		INDArray testInit = Nd4j.zeros(allPossibleChars.size());

		for (char currentChar : testString)
		{
			testInit.putScalar(allPossibleChars.indexOf(currentChar), 1);
		}
		// System.out.println("testInit = " + testInit.toString());
		return testInit;
	}

	// public char[] getTestString()
	// {
	// return testString;
	// }

	// public int getHIDDEN_LAYER_WIDTH()
	// {
	// return HIDDEN_LAYER_WIDTH;
	// }

	public int getHIDDEN_LAYER_COUNT()
	{
		return this.numOfUnitsInHiddenLayersInOrder.length;
	}

	public int[] getNumOfUnitsInHiddenLayersInOrder()
	{
		return numOfUnitsInHiddenLayersInOrder;
	}

	public MultiLayerNetwork getNet()
	{
		return net;
	}

	/**
	 * 
	 * @param fileLocation
	 * @param commentChars
	 * @param limit
	 * @return
	 */
	private static char[] getCharArrayFromTextWithValidEnglishCharsOnly(String fileLocation, String commentChars,
			int limit)
	{
		// String fileLocation = "";
		// String commentChars = "";
		Charset textFileEncoding = Charset.defaultCharset();
		StringBuilder sb = new StringBuilder();

		try
		{
			char[] validCharacters = CharacterIterator_WC1.getMinimalCharacterSet(); // Which characters are allowed?
			Set<Character> validCharSet = new HashSet<>(validCharacters.length);
			for (char ch : validCharacters)
			{
				validCharSet.add(ch);
			}

			// Load file and convert contents to a char[]
			boolean newLineValid = validCharSet.contains('\n');
			List<String> lines = Files.readAllLines(new File(fileLocation).toPath(), textFileEncoding);
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

			char[] characters = new char[limit];
			int index = 0;

			sb.append("Looping over all lines\n");

			for (String s : lines)
			{
				char[] thisLine = s.toCharArray();
				for (char charInThisLine : thisLine)
				{
					if (!validCharSet.contains(charInThisLine))
					{
						continue;
					}
					characters[index++] = charInThisLine;
					if (index == limit)
					{
						break;
					}
				}
				if (index == limit)
				{
					break;
				}

				if (newLineValid)
				{
					characters[index++] = '\n';
				}
			}

			sb.append("character.length()=" + characters.length + "\n");
			System.out.println(sb.toString());
			return characters;

		}
		catch (Exception e)
		{
			e.printStackTrace();

		}
		return null;
	}

	// public char[] getTrainingString()
	// {
	// return trainingString;
	// }
	// public List<Character> getAllpossiblechars()
	// {
	// return allPossibleChars;
	// }

	// /**
	// * CREATE OUR TRAINING DATA
	// * <p>
	// * create input and output arrays: SAMPLE_INDEX, INPUT_NEURON, SEQUENCE_POSITION
	// *
	// * @param trainingString
	// * @param allPossibleChars
	// * @param verbose
	// * @return
	// */
	// private DataSet createTrainingDataset(char[] trainingString, List<Character> allPossibleChars, boolean verbose)
	// {
	// System.out.println("createTrainingDataset() called");
	// StringBuilder sb = new StringBuilder();
	//
	// // Its actually a 3D array, first dimension being 1, I guess we can view it as
	// // a 2D array with num_rows= num_of_poss_chars, num_cols = length of training string
	// INDArray input = Nd4j.zeros(1, allPossibleChars.size(), trainingString.length);
	// INDArray labels = Nd4j.zeros(1, allPossibleChars.size(), trainingString.length);
	//
	// // loop through our sample-sentence
	// sb.append("prepareTrainingDataset():\nLooping through sample sentence (trainingString):"
	// + new String(trainingString) + "\n");
	//
	// int samplePos = 0;
	// for (char currentChar : trainingString)
	// {
	// // small hack: when currentChar is the last, take the first char as
	// // nextChar - not really required. Added to this hack by adding a starter first character.
	// char nextChar = trainingString[(samplePos + 1) % (trainingString.length)];
	// // $$sb.append("\nsamplePos = " + samplePos + "\tcurrentChar = " + currentChar + "\tnextChar = " +
	// // nextChar);
	//
	// // input neuron for current-char is 1 at "samplePos"
	// input.putScalar(new int[] { 0, allPossibleChars.indexOf(currentChar), samplePos }, 1);
	// // $$ sb.append("\nInput NDArray = \n" + input.toString());
	//
	// // output neuron for next-char is 1 at "samplePos"
	// labels.putScalar(new int[] { 0, allPossibleChars.indexOf(nextChar), samplePos }, 1);
	// // $$sb.append("\nOutput NDArray = \n" + labels.toString());
	//
	// samplePos++;
	// }
	//
	// if (verbose)
	// {
	// sb.append("---> Create Dataset: \n");
	// sb.append("\nCreated Input NDArray = \n" + input.toString());
	// sb.append("\nCreated Output NDArray = \n" + labels.toString());
	// System.out.println(sb.toString());
	// }
	//
	// return new DataSet(input, labels);
	// }

	// public static void mainOld(String args[])
	// {
	// boolean verbose = true;
	// // define a sentence to learn.
	// // Add a special character at the beginning so the RNN learns the complete string and ends with the marker.
	// String trainingString = "hellohelohellohelohellohelohellohelohellohelohellohelohhhhh";
	// // "*Der Cottbuser Postkutscher putzt den Cottbuser
	// getSplitSize(55846, 100, 0.05);
	// System.exit(0);
	// // Postkutschkasten.";
	// System.out.println("trainingString= " + trainingString);
	//
	// LSTMCharModelling_SeqRecJun2018 rnnA = new LSTMCharModelling_SeqRecJun2018(50, 2);
	// rnnA.setTrainingString(trainingString, verbose);
	// rnnA.setTestString("h".toCharArray(), verbose);
	// rnnA.configureAndCreateRNN(0.001, -1);
	// DataSet trainingData = rnnA.createTrainingDataset(rnnA.trainingString, rnnA.getAllpossiblechars(), verbose);
	//
	// int numOfTrainingEpochs = 500;
	// rnnA.trainTheNetwork(numOfTrainingEpochs, trainingData, verbose);
	// rnnA.predictNextNValues2(8, true, rnnA.getTestString());
	// }
	// /**
	// * Sets trainingString and allPossibleChars
	// * <p>
	// *
	// *
	// * @param trainingString
	// * @param verbose
	// */
	// public void setAllPossibleChars(String trainingString, boolean verbose)
	// {
	// System.out.println("setTrainingString() called");
	// this.trainingString = trainingString.toCharArray();
	//
	// // create a dedicated list of possible chars in allPossibleChars
	// Set<Character> trainingStringChars = trainingString.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
	// allPossibleChars.addAll(trainingStringChars);
	//
	// if (verbose)
	// {
	// System.out.println("setAllPossibleChars(): character List = " + allPossibleChars);
	// }
	// }

	// /**
	// * Sets trainingString and allPossibleChars
	// * <p>
	// * // check is trainingString as member variable is really needed?
	// *
	// * @param trainingString
	// * @param verbose
	// */
	// public void setTrainingString(char[] trainingStringAsCharArr, boolean verbose)
	// {
	// System.out.println("setTrainingString() called");
	// this.trainingString = trainingStringAsCharArr;
	//
	// // create a dedicated list of possible chars in allPossibleChars
	// // Set<Character> trainingStringChars = trainingString.chars().mapToObj(c -> (char)
	// // c).collect(Collectors.toSet());
	// Set<Character> trainingStringChars = new LinkedHashSet<>();
	// for (char c : trainingStringAsCharArr)
	// {
	// trainingStringChars.add(c);
	// }
	//
	// allPossibleChars.addAll(trainingStringChars);
	//
	// if (verbose)
	// {
	// System.out.println("setTrainingString(): character List = " + allPossibleChars);
	// }
	// }

	// /**
	// * Sets testString and LEARNSTRING_CHARS_LIST
	// *
	// * @param testString
	// * @param verbose
	// */
	// public void setTestString(char[] testString, boolean verbose)
	// {
	// System.out.println("setTestString() called");
	// this.testString = testString;
	//
	// // create a dedicated list of possible chars in allPossibleChars
	// Set<Character> testStringChars = new LinkedHashSet<>();
	// for (char c : testString)
	// {
	// testStringChars.add(c);
	// }
	//
	// if ((allPossibleChars.containsAll(testStringChars)) == false)
	// {
	// System.err.println("\nNew chars in test string");
	// }
	// }
}
