package org.activity.nn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.io.WToFile;
import org.activity.ui.PopUps;
import org.apache.commons.math3.primes.Primes;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 * This example trains a RNN. When trained we only have to put the first character of trainingString to the RNN, and it
 * will recite the following chars
 *
 */
public class BasicRNNWC2_SeqRec2018
{
	private char[] trainingString;
	private char[] testString;
	private final List<Character> allPossibleChars;// = new ArrayList<>(); // a list of all possible characters

	// RNN dimensions
	private int HIDDEN_LAYER_WIDTH = 50;
	private int HIDDEN_LAYER_COUNT = 2;
	private static final Random r = new Random(7894);

	MultiLayerNetwork net;

	private static LinkedHashMap<String, BasicRNNWC2_SeqRec2018> rnnPredictorsForEachUserStored = new LinkedHashMap<>();

	public static final BasicRNNWC2_SeqRec2018 getRNNPredictorsForEachUserStored(String userID)
	{
		return rnnPredictorsForEachUserStored.get(userID);
	}

	/**
	 * 
	 * @param network
	 * @param verbose
	 * @return
	 */
	public int getNumberOfParameters(boolean verbose)
	{
		int numOfParams = net.numParams();

		if (verbose)
		{
			System.out.println("Total number of parameters: " + numOfParams);
			for (int i = 0; i < net.getnLayers(); i++)
			{
				System.out.println("Layer " + i + " number of parameters: " + net.getLayer(i).numParams());
			}
		}
		return numOfParams;
	}

	/**
	 * delete all stored AKOMSeqPredictorLighter because the stored users wont be involved anymore. This is because this
	 * class was becoming too heavy and we need to make it light.
	 */
	public static final void clearRNNPredictorsForEachUserStored()
	{
		rnnPredictorsForEachUserStored.clear();
	}

	public BasicRNNWC2_SeqRec2018(int numOfNeuronsInHiddenLayer, int numOfHiddenLayers)
	{
		HIDDEN_LAYER_WIDTH = numOfNeuronsInHiddenLayer;
		HIDDEN_LAYER_COUNT = numOfHiddenLayers;
		allPossibleChars = new ArrayList<>();
	}

	/**
	 * Sets trainingString and allPossibleChars
	 * <p>
	 * // TODO check is trainingString as member variable is really needed?
	 * 
	 * @param trainingString
	 * @param verbose
	 */
	public void setTrainingString(String trainingString, boolean verbose)
	{
		System.out.println("setTrainingString() called");
		this.trainingString = trainingString.toCharArray();

		// create a dedicated list of possible chars in allPossibleChars
		Set<Character> trainingStringChars = trainingString.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
		allPossibleChars.addAll(trainingStringChars);

		if (verbose)
		{
			System.out.println("setTrainingString(): character List = " + allPossibleChars);
		}
	}

	/**
	 * Sets trainingString and allPossibleChars
	 * <p>
	 * // TODO check is trainingString as member variable is really needed?
	 * 
	 * @param trainingString
	 * @param verbose
	 */
	public void setTrainingString(char[] trainingStringAsCharArr, boolean verbose)
	{
		System.out.println("setTrainingString() called");
		this.trainingString = trainingStringAsCharArr;

		// create a dedicated list of possible chars in allPossibleChars
		// Set<Character> trainingStringChars = trainingString.chars().mapToObj(c -> (char)
		// c).collect(Collectors.toSet());
		Set<Character> trainingStringChars = new LinkedHashSet<>();
		for (char c : trainingStringAsCharArr)
		{
			trainingStringChars.add(c);
		}

		allPossibleChars.addAll(trainingStringChars);

		if (verbose)
		{
			System.out.println("setTrainingString(): character List = " + allPossibleChars);
		}
	}

	/**
	 * Sets testString and LEARNSTRING_CHARS_LIST
	 * 
	 * @param testString
	 * @param verbose
	 */
	public void setTestString(char[] testString, boolean verbose)
	{
		System.out.println("setTestString() called");
		this.testString = testString;

		// create a dedicated list of possible chars in allPossibleChars
		Set<Character> testStringChars = new LinkedHashSet<>();
		for (char c : testString)
		{
			testStringChars.add(c);
		}

		if ((allPossibleChars.containsAll(testStringChars)) == false)
		{
			System.err.println("\nNew chars in test string");
		}
	}

	public static void main(String args[])
	{
		boolean verbose = true;
		// define a sentence to learn.
		// Add a special character at the beginning so the RNN learns the complete string and ends with the marker.
		String trainingString = "hellohelohellohelohellohelohellohelohellohelohellohelohhhhh";// "*Der Cottbuser
																								// Postkutscher
																								// putzt den Cottbuser

		getSplitSize(55846, 100, 0.05);
		System.exit(0);
		// Postkutschkasten.";
		System.out.println("trainingString= " + trainingString);
		BasicRNNWC2_SeqRec2018 rnnA = new BasicRNNWC2_SeqRec2018(50, 2);
		rnnA.setTrainingString(trainingString, verbose);
		rnnA.setTestString("h".toCharArray(), verbose);
		rnnA.configureAndCreateRNN(0.001, -1);
		DataSet trainingData = rnnA.createTrainingDataset(rnnA.trainingString, rnnA.getAllpossiblechars(), verbose);

		int numOfTrainingEpochs = 500;
		rnnA.trainTheNetwork(numOfTrainingEpochs, trainingData, verbose);
		rnnA.predictNextNValues2(8, true, rnnA.getTestString());
	}

	/**
	 * 
	 * @param givenData
	 * @param verbose
	 * @return
	 */
	public static char[] flattenList(ArrayList<ArrayList<Character>> givenData, boolean verbose)
	{
		List<Character> flattenedList = new ArrayList();

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

		if (verbose)
		{
			System.out.println("givenData=\n" + givenData);
			System.out.println("\nflattenedList=\n" + flattenedList);
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
		int smallerSizeSelected = -1;

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
		return lengthOfSingleLongSeq;
	}

	/**
	 * 
	 * @param trainingString
	 * @param userID
	 * @param verbose
	 */
	public BasicRNNWC2_SeqRec2018(ArrayList<ArrayList<Character>> trainingString, String userID, boolean verbose)
	{

		this(Constant.numOfNeuronsInEachHiddenLayerInRNN1, Constant.numOfHiddenLayersInRNN1, verbose, userID,
				Constant.numOfTrainingEpochsInRNN1, Constant.learningRateInRNN1,
				BasicRNNWC2_SeqRec2018.flattenList(trainingString, verbose));
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
	 * 
	 * @param numOfNeuronsInHiddenLayer
	 * @param numOfHiddenLayers
	 * @param verbose
	 * @param userID
	 * @param numOfTrainingEpochs
	 * @param learningRate
	 * @param trainingString
	 */
	public BasicRNNWC2_SeqRec2018(int numOfNeuronsInHiddenLayer, int numOfHiddenLayers, boolean verbose, String userID,
			int numOfTrainingEpochs, double learningRate, char[] trainingString)
	{
		this(numOfNeuronsInHiddenLayer, numOfHiddenLayers);
		long t1 = System.currentTimeMillis();
		int lengthOfTBPTT = (int) (0.25 * trainingString.length);

		System.out.println("Inside BasicRNNWC2_SeqRec2018: numOfNeuronsInHiddenLayer= " + numOfNeuronsInHiddenLayer
				+ " numOfHiddenLayers=" + numOfHiddenLayers + " numOfTrainingEpochs=" + numOfTrainingEpochs
				+ " learningRate=" + learningRate + " userID=" + userID + " trainingString.length="
				+ trainingString.length + " lengthOfTBPTT=" + lengthOfTBPTT);
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
			this.setTrainingString(trainingString, verbose);
			// rnnA.setTestString("h".toCharArray(), true);
			this.configureAndCreateRNN(learningRate, lengthOfTBPTT);
			DataSet trainingData = this.createTrainingDataset(this.trainingString, this.getAllpossiblechars(), verbose);

			this.trainTheNetwork(numOfTrainingEpochs, trainingData, verbose);

			// be careful of storing this as this object can consume a lot of memory
			if (Constant.sameRNNForAllRTsOfAUser)
			{
				rnnPredictorsForEachUserStored.put(userID, this);
			}
			// rnnA.predictNextNValues2(8, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.printTracedErrorMsgWithExit("Error in RNN");
		}

		// better to exit than to continue with error
		System.out.println("Trained RNN for user " + userID + "  in " + (System.currentTimeMillis() - t1) + "ms");
	}

	/**
	 * see: https://deeplearning4j.org/usingrnns
	 * 
	 * @param numOfTrainingEpochs
	 * @param trainingData
	 * @param verbose
	 */
	private void trainTheNetwork(int numOfTrainingEpochs, DataSet trainingData, boolean verbose)
	{
		System.out.println("trainTheNetwork called()");
		// StringBuilder sb = new StringBuilder();
		System.out.println("this.allPossibleChars= " + this.allPossibleChars);

		for (int epoch = 0; epoch < numOfTrainingEpochs; epoch++)
		{
			long t1 = System.currentTimeMillis();

			System.out.println("---------- Start of Training Epoch " + epoch);

			// train the data
			net.fit(trainingData);
			System.out.println("after fit");

			// clear current stance from the last example
			net.rnnClearPreviousState();
			System.out.println("cleared previous state");

			// put the first character into the rnn as an initialisation
			INDArray testInit = Nd4j.zeros(allPossibleChars.size());
			testInit.putScalar(allPossibleChars.indexOf(trainingString[0]), 1);
			// $$System.out.println("testInit = " + testInit);

			// run one step -> IMPORTANT: rnnTimeStep() must be called, not
			// output()
			// the output shows what the net thinks what should come next
			INDArray output = net.rnnTimeStep(testInit);
			// $$System.out.println("output initial - " + output);

			// now the net should guess LEARNSTRING.length more characters
			if (false)
			{
				for (char charFromTrainString : trainingString)
				{
					System.out.println("\ncharFromTrainString= " + charFromTrainString);
					// first process the last output of the network to a concrete
					// neuron, the neuron with the highest output has the highest
					// chance to get chosen
					int sampledCharacterIdx = Nd4j.getExecutioner().exec(new IMax(output), 1).getInt(0);

					// print the chosen output
					System.out.print("\n Prediction (chosen output)=" + allPossibleChars.get(sampledCharacterIdx));

					// use the last output as input
					INDArray nextInput = Nd4j.zeros(allPossibleChars.size());
					nextInput.putScalar(sampledCharacterIdx, 1);
					output = net.rnnTimeStep(nextInput);
				}
			}

			long timeTakenForThisEpoch = System.currentTimeMillis() - t1;
			System.out.println("---------- End of Training Epoch " + epoch + " took " + timeTakenForThisEpoch + " ms");
			WToFile.appendLineToFileAbs(epoch + "," + timeTakenForThisEpoch + "\n",
					Constant.getCommonPath() + "RNN1TimeTakenPerEpoch.csv");
		}
		System.out.println("End of Training");
	}

	/**
	 * 
	 * @param N
	 * @param verbose
	 * @param testString
	 * @return
	 * @deprecated
	 */
	public List<Character> predictNextNValues2(int N, boolean verbose, ArrayList<Character> testString)
	{
		char[] testStringArr = new char[testString.size()];// TODO MOST LIKELY INCORRECT, CHECK IF EMPTY
		return predictNextNValues2(N, verbose, testStringArr);
	}

	/**
	 * 
	 * @param N
	 * @param verbose
	 * @return
	 */
	public List<Character> predictNextNValues2(int N, boolean verbose, char[] testString)
	{
		System.out.println("predictNextNValues2() called:");
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

		return predVals;
	}

	/**
	 * 
	 * @param learningRate
	 * @param tbpttLength
	 *            length for truncate back propagation through time
	 */
	public void configureAndCreateRNN(double learningRate, int tbpttLength)
	// int iterations, double learningRate, int numOfEpochs, boolean verbose)
	{
		System.out.println("configureAndCreateRNN() called");
		/////////////// Start of set common parameters for NN
		NeuralNetConfiguration.Builder nnConfigBuilder = new NeuralNetConfiguration.Builder();
		nnConfigBuilder.seed(123);
		nnConfigBuilder.biasInit(0);
		nnConfigBuilder.miniBatch(false);
		nnConfigBuilder.updater(new RmsProp(learningRate));// 0.001));
		nnConfigBuilder.weightInit(WeightInit.XAVIER);
		/////////////// End of set common parameters for NN

		ListBuilder listOfConfigsBuilder = nnConfigBuilder.list();

		/////////////// Start of configure the hidden layers iteratively
		// first difference, for rnns we need to use LSTM.Builder
		for (int layerIndex = 0; layerIndex < HIDDEN_LAYER_COUNT; layerIndex++)
		{
			LSTM.Builder hiddenLayerBuilder = new LSTM.Builder();
			hiddenLayerBuilder.nIn(layerIndex == 0 ? allPossibleChars.size() : HIDDEN_LAYER_WIDTH);
			hiddenLayerBuilder.nOut(HIDDEN_LAYER_WIDTH);
			// adopted activation function from LSTMCharModellingExample seems to work well with RNNs
			hiddenLayerBuilder.activation(Activation.TANH);
			listOfConfigsBuilder.layer(layerIndex, hiddenLayerBuilder.build());
		}
		/////////////// End of configure the hidden layers iteratively

		/////////////// Start of configure the output layer
		// we need to use RnnOutputLayer for our RNN
		RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunction.MCXENT);
		// softmax normalizes the output neurons, the sum of all outputs is 1
		// this is required for our sampleFromDistribution-function
		outputLayerBuilder.activation(Activation.SOFTMAX);
		outputLayerBuilder.nIn(HIDDEN_LAYER_WIDTH);
		outputLayerBuilder.nOut(allPossibleChars.size());
		listOfConfigsBuilder.layer(HIDDEN_LAYER_COUNT, outputLayerBuilder.build());
		/////////////// End of configure the output layer

		// finish builder
		listOfConfigsBuilder.pretrain(false);
		listOfConfigsBuilder.backprop(true);

		// Start of added on 20 June
		if (tbpttLength > 0)// truncated backpropagation through time.
		{
			listOfConfigsBuilder.backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength)
					.tBPTTBackwardLength(tbpttLength);
		}
		// End of added on 20 June

		// create network
		MultiLayerConfiguration conf = listOfConfigsBuilder.build();

		// Set the multilayer network for the RNN
		net = new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(1));
	}

	/**
	 * CREATE OUR TRAINING DATA
	 * <p>
	 * create input and output arrays: SAMPLE_INDEX, INPUT_NEURON, SEQUENCE_POSITION
	 * 
	 * @param trainingString
	 * @param allPossibleChars
	 * @param verbose
	 * @return
	 */
	private DataSet createTrainingDataset(char[] trainingString, List<Character> allPossibleChars, boolean verbose)
	{
		System.out.println("createTrainingDataset() called");
		StringBuilder sb = new StringBuilder();

		// Its actually a 3D array, first dimension being 1, I guess we can view it as
		// a 2D array with num_rows= num_of_poss_chars, num_cols = length of training string
		INDArray input = Nd4j.zeros(1, allPossibleChars.size(), trainingString.length);
		INDArray labels = Nd4j.zeros(1, allPossibleChars.size(), trainingString.length);

		// loop through our sample-sentence
		sb.append("prepareTrainingDataset():\nLooping through sample sentence (trainingString):"
				+ new String(trainingString) + "\n");

		int samplePos = 0;
		for (char currentChar : trainingString)
		{
			// small hack: when currentChar is the last, take the first char as
			// nextChar - not really required. Added to this hack by adding a starter first character.
			char nextChar = trainingString[(samplePos + 1) % (trainingString.length)];
			// $$sb.append("\nsamplePos = " + samplePos + "\tcurrentChar = " + currentChar + "\tnextChar = " +
			// nextChar);

			// input neuron for current-char is 1 at "samplePos"
			input.putScalar(new int[] { 0, allPossibleChars.indexOf(currentChar), samplePos }, 1);
			// $$ sb.append("\nInput NDArray = \n" + input.toString());

			// output neuron for next-char is 1 at "samplePos"
			labels.putScalar(new int[] { 0, allPossibleChars.indexOf(nextChar), samplePos }, 1);
			// $$sb.append("\nOutput NDArray = \n" + labels.toString());

			samplePos++;
		}

		if (verbose)
		{
			sb.append("---> Create Dataset: \n");
			sb.append("\nCreated Input NDArray = \n" + input.toString());
			sb.append("\nCreated Output NDArray = \n" + labels.toString());
			System.out.println(sb.toString());
		}

		return new DataSet(input, labels);
	}

	/**
	 * 
	 * @param LEARNSTRING
	 * @param LEARNSTRING_CHARS_LIST
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

	public char[] getTrainingString()
	{
		return trainingString;
	}

	public char[] getTestString()
	{
		return testString;
	}

	public List<Character> getAllpossiblechars()
	{
		return allPossibleChars;
	}

	public int getHIDDEN_LAYER_WIDTH()
	{
		return HIDDEN_LAYER_WIDTH;
	}

	public int getHIDDEN_LAYER_COUNT()
	{
		return HIDDEN_LAYER_COUNT;
	}

	public MultiLayerNetwork getNet()
	{
		return net;
	}
}
