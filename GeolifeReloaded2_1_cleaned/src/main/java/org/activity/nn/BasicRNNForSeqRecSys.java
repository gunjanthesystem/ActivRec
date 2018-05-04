package org.activity.nn;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 * This example trains a RNN.WHen trained we only have to put the first character of LEARNSTRING to the RNN,and it will
 * recite the following chars.
 * 
 */
public class BasicRNNForSeqRecSys
{

	// define a sentence to learn.
	// Add a special character at the beginning so the RNN learns the complete string and ends with the marker.
	// private static final char[] LEARNSTRING = "*Der Cottbuser Postkutscher putzt den Cottbuser Postkutschkasten."
	// .toCharArray();

	private char[] trainString;
	private char[] testString;
	// a list of all possible characters
	private final List<Character> trainStringCharsList = new ArrayList<>();

	// RNN dimensions
	private int numOfNeuronsInHiddenLayers;// = 50;
	private int numOfHiddenLayers;// = 2; // number of hidden layers
	private final Random r = new Random(7894);
	private int numOfEpochs;// = 100;
	private int numOfIterations;// = 1;// set it to 10 in execution
	private MultiLayerNetwork net;
	private boolean verbose;// = true;
	// private int numOfStepsToPredictAhead;//
	private double learningRate;// = 0.001;

	public static void main(String[] args)
	{
		String s1 = "Gunjan is going to city centre. Gunjan is wearing a blue sweater. Gunjan is running marathon."
				+ " Gunjan is wearing a blue sweater.Gunjan is going to city centre. Gunjan is wearing a blue sweater."
				+ " Gunjan is running marathon. Gunjan is wearing a blue sweater.Gunjan is going to city centre."
				+ " Gunjan is going to city centre. Gunjan is running marathon. Gunjan is wearing a blue sweater."
				+ " Gunjan is wearing a blue sweater. Gunjan is wearing a blue sweater. Gunjan is wearing a blue sweater."
				+ "Gunjan is wearing a blue sweater.Gunjan is going to city centre. Gunjan is going to city centre. "
				+ "Gunjan is running marathon. Gunjan is wearing a blue sweater. Gunjan is wearing a blue sweater. "
				+ "Gunjan is going to city centre. Gunjan is running marathon. Gunjan is wearing a blue sweater."
				+ "Gunjan is wearing a blue sweater."
				+ "Gunjan is wearing a blue sweater.Gunjan is going to city centre. "
				+ "Gunjan is going to city centre. Gunjan is running marathon. Gunjan is wearing a blue sweater";

		String s = "Der Cottbuser Postkutscher putzt den Cottbuser Postkutschkasten.";

		int numOfNeuronsInHiddenLayers = 50;
		int numOfHiddenLayers = 2; // number of hidden layers
		int numOfEpochs = 100;
		int numOfIterations = 1;// set it to 10 in execution
		boolean verbose = false;

		double learningRate = 0.001;

		BasicRNNForSeqRecSys rnnA = new BasicRNNForSeqRecSys(numOfNeuronsInHiddenLayers, numOfHiddenLayers, numOfEpochs,
				numOfIterations, learningRate, verbose);

		rnnA.createTrainingString("hellohelohellohelohellohelohellohelo".toCharArray(), verbose);
		// rnnA.createTrainingString(s.toCharArray(), true);
		rnnA.configureAndTrainRNN(rnnA.numOfIterations, rnnA.learningRate, rnnA.numOfEpochs, verbose);
		rnnA.createTestString("he".toCharArray(), verbose);

		ArrayList<Character> predictedNextNValsInSeq = rnnA.predictNextNValues(9, verbose);

		System.out.println("Train seq:" + new String(rnnA.trainString));
		System.out.println("Test seq:" + new String(rnnA.testString));
		System.out.println("predictedNextNValsInSeq= " + predictedNextNValsInSeq);
		// System.exit(0);

	}

	/**
	 * 
	 * @param numOfNeuronsInHiddenLayers
	 * @param numOfHiddenLayers
	 * @param numOfEpochs
	 * @param numOfIterations
	 * @param verbose
	 * @param learningRate
	 */
	public BasicRNNForSeqRecSys(int numOfNeuronsInHiddenLayers, int numOfHiddenLayers, int numOfEpochs,
			int numOfIterations, double learningRate, boolean verbose)
	{
		super();
		this.numOfNeuronsInHiddenLayers = numOfNeuronsInHiddenLayers;
		this.numOfHiddenLayers = numOfHiddenLayers;
		this.numOfEpochs = numOfEpochs;
		this.numOfIterations = numOfIterations;
		this.learningRate = learningRate;
		this.verbose = verbose;

	}

	public char[] getTrainString()
	{
		return trainString;
	}

	public void setTrainString(char[] trainString)
	{
		this.trainString = trainString;
	}

	public char[] getTestString()
	{
		return testString;
	}

	public void setTestString(char[] testString)
	{
		this.testString = testString;
	}

	public int getNumOfNeuronsInHiddenLayers()
	{
		return numOfNeuronsInHiddenLayers;
	}

	public void setNumOfNeuronsInHiddenLayers(int numOfNeuronsInHiddenLayers)
	{
		this.numOfNeuronsInHiddenLayers = numOfNeuronsInHiddenLayers;
	}

	public int getNumOfHiddenLayers()
	{
		return numOfHiddenLayers;
	}

	public void setNumOfHiddenLayers(int numOfHiddenLayers)
	{
		this.numOfHiddenLayers = numOfHiddenLayers;
	}

	public int getNumOfEpochs()
	{
		return numOfEpochs;
	}

	public void setNumOfEpochs(int numOfEpochs)
	{
		this.numOfEpochs = numOfEpochs;
	}

	public int getNumOfIterations()
	{
		return numOfIterations;
	}

	public void setNumOfIterations(int numOfIterations)
	{
		this.numOfIterations = numOfIterations;
	}

	public MultiLayerNetwork getNet()
	{
		return net;
	}

	public boolean isVerbose()
	{
		return verbose;
	}

	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}

	public double getLearningRate()
	{
		return learningRate;
	}

	public void setLearningRate(double learningRate)
	{
		this.learningRate = learningRate;
	}

	public void visualiseNN()
	{
		//
		// // Initialize the user interface backend

		//
		// // Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in
		// // memory.
		// StatsStorage statsStorage = new InMemoryStatsStorage(); // Alternative: new FileStatsStorage(File), for
		// saving
		// // and loading later
		//
		// // Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
		// uiServer.attach(statsStorage);
		//
		// // Then add the StatsListener to collect this information from the network, as it trains
		// net.setListeners(new StatsListener(statsStorage));
	}

	/**
	 *
	 * @param N
	 * @param verbose
	 * @return
	 */
	private ArrayList<Character> predictNextNValues(int N, boolean verbose)
	{
		ArrayList<Character> predictionsInSeq = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		INDArray testInit = prepareTestInitDataset(testString, trainStringCharsList, verbose);

		// clear current stance from the last example
		net.rnnClearPreviousState();
		sb.append("\ncleared previous state");

		// initialisation with current timeline
		sb.append("testInit = " + testInit);

		// run one step -> IMPORTANT: rnnTimeStep() must be called, not
		// output()
		// the output shows what the net thinks what should come next
		INDArray output = net.rnnTimeStep(testInit);

		for (int i = 0; i < N; i++)
		{
			int sampledCharacterIdx = Nd4j.getExecutioner().exec(new IMax(output), 1).getInt(0);

			Character predictedChar = trainStringCharsList.get(sampledCharacterIdx);
			predictionsInSeq.add(predictedChar);

			// print the chosen output
			sb.append("\n\n-- prediction@Step" + (i + 1) + "=" + predictedChar);
			sb2.append("\n\n-- prediction@Step" + (i + 1) + "=" + predictedChar);

			// use the last output as input
			INDArray nextInput = Nd4j.zeros(trainStringCharsList.size());
			nextInput.putScalar(sampledCharacterIdx, 1);

			output = net.rnnTimeStep(nextInput);
			sb.append("\nnextInput = " + nextInput);
			sb.append("\noutput = " + output);
		}

		if (verbose)
		{
			System.out.print(sb.toString() + "\n");
		}
		else
		{
			System.out.print(sb2.toString() + "\n");
		}

		return predictionsInSeq;
	}

	/**
	 * Creates an instance and sets numOfNeuronsInHiddenLayer and numOfHiddenLayers
	 * 
	 * @param numOfNeuronsInHiddenLayer
	 * @param numOfHiddenLayers
	 */
	public BasicRNNForSeqRecSys(int numOfNeuronsInHiddenLayer, int numOfHiddenLayers)
	{
		this.numOfNeuronsInHiddenLayers = numOfNeuronsInHiddenLayer;
		this.numOfHiddenLayers = numOfHiddenLayers;
	}

	/**
	 * Initialise trainString (a char array) and the list of letters in train
	 * 
	 * @param trainingString
	 * @param verbose
	 */
	public void createTrainingString(char[] trainingString, boolean verbose)
	{
		System.out.println("----Entering createTrainingString()");
		trainString = trainingString;

		// create a dedicated list of possible chars in LEARNSTRING_CHARS_LIST
		LinkedHashSet<Character> LEARNSTRING_CHARS = new LinkedHashSet<>();
		for (char c : trainString)
		{
			LEARNSTRING_CHARS.add(c);
		}
		trainStringCharsList.addAll(LEARNSTRING_CHARS);

		if (verbose)
		{
			System.out.println("Character List = " + trainStringCharsList);
		}
		System.out.println("----Exiting createTrainingString()");
	}

	/**
	 * Initialise testString and lcheck if all letters in test and in list of letters in train
	 * 
	 * @param testString
	 * @param verbose
	 * @return -1 if there are chars in test which are not in train, otherwise return 0, i.e., the return value is just
	 *         for check.
	 */
	public int createTestString(char[] testString, boolean verbose)
	{
		System.out.println("----Entering createTestString()");
		this.testString = testString;

		// create a dedicated list of possible chars in LEARNSTRING_CHARS_LIST
		LinkedHashSet<Character> testString_CHARS = new LinkedHashSet<>();

		for (char c : testString)
		{
			testString_CHARS.add(c);
		}

		if ((trainStringCharsList.containsAll(testString_CHARS)) == false)
		{
			System.err.println("\nNew chars in test string");
			return -1;
		}
		System.out.println("----Exiting createTestString()");
		return 0;

	}

	/**
	 * TODO: SEEMS SOME METHODS THEY HAVE BEEN DISABLED IN THE NEW VERSION
	 * 
	 * @param iterations
	 * @param learningRate
	 * @param numOfEpochs
	 * @param verbose
	 */
	public void configureAndTrainRNN(int iterations, double learningRate, int numOfEpochs, boolean verbose)
	{
		System.out.println("----Entering configureAndTrainRNN()");
		StringBuilder sb = new StringBuilder();

		this.numOfEpochs = numOfEpochs;
		// some common parameters
		NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
		// builder.iterations(iterations);//TODO: SEEMS THEY HAVE BEEN DISABLED IN THE NEW VERSION
		// builder.learningRate(learningRate);//TODO: SEEMS THEY HAVE BEEN DISABLED IN THE NEW VERSION
		builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
		builder.seed(123);
		builder.biasInit(0);
		builder.miniBatch(false);
		builder.updater(Updater.RMSPROP);
		builder.weightInit(WeightInit.XAVIER);

		ListBuilder listBuilder = builder.list();

		if (verbose)
		{
			sb.append("ListBuilder = " + listBuilder.toString() + "\n\nbuilder = " + builder.toString());
		}

		// for rnns we need to use GravesLSTM.Builder
		// Configure the hidden layers iteratively
		for (int i = 0; i < numOfHiddenLayers; i++)
		{
			GravesLSTM.Builder hiddenLayerBuilder = new GravesLSTM.Builder();

			// num of neuron in first hidden layer is the number of alphabets
			hiddenLayerBuilder.nIn(i == 0 ? trainStringCharsList.size() : numOfNeuronsInHiddenLayers);
			hiddenLayerBuilder.nOut(numOfNeuronsInHiddenLayers);
			// adopted activation function from GravesLSTMCharModellingExample
			// seems to work well with RNNs
			hiddenLayerBuilder.activation(Activation.TANH);
			listBuilder.layer(i, hiddenLayerBuilder.build());
		}

		// we need to use RnnOutputLayer for our RNN
		RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunction.MCXENT);
		// softmax normalizes the output neurons, the sum of all outputs is 1

		// this is required for our sampleFromDistribution-function
		outputLayerBuilder.activation(Activation.SOFTMAX);
		outputLayerBuilder.nIn(numOfNeuronsInHiddenLayers);
		outputLayerBuilder.nOut(trainStringCharsList.size());
		listBuilder.layer(numOfHiddenLayers, outputLayerBuilder.build());

		// finish builder
		listBuilder.pretrain(false);
		listBuilder.backprop(true);

		// create network
		MultiLayerConfiguration conf = listBuilder.build();
		net = new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(1));

		DataSet trainingData = prepareDataset(trainString, trainStringCharsList, verbose);

		// some epochs
		for (int epoch = 0; epoch < numOfEpochs; epoch++)
		{
			sb.append("Epoch " + epoch);

			// train the data
			net.fit(trainingData); // iterates as many times as builder.iteration

			sb.append("after fit");

			// clear current stance from the last example
			net.rnnClearPreviousState();
			sb.append("cleared previous state");

			// put the first character into the rrn as an initialisation
			INDArray testInit = Nd4j.zeros(trainStringCharsList.size());
			testInit.putScalar(trainStringCharsList.indexOf(trainString[0]), 1);

			sb.append("testInit = " + testInit);

			// run one step -> IMPORTANT: rnnTimeStep() must be called, not
			// output()
			// the output shows what the net thinks what should come next
			INDArray output = net.rnnTimeStep(testInit);
			sb.append("output initial - " + output);

			// now the net should guess LEARNSTRING.length more characters
			for (char dummy : trainString)
			{
				sb.append("\ndummy= " + dummy);
				// first process the last output of the network to a concrete
				// neuron, the neuron with the highest output cas the highest
				// chance to get chosen
				int sampledCharacterIdx = Nd4j.getExecutioner().exec(new IMax(output), 1).getInt(0);

				// print the chosen output
				sb.append("\nprediction=" + trainStringCharsList.get(sampledCharacterIdx));

				// use the last output as input
				INDArray nextInput = Nd4j.zeros(trainStringCharsList.size());
				nextInput.putScalar(sampledCharacterIdx, 1);

				output = net.rnnTimeStep(nextInput);
				sb.append("\nnextInput = " + nextInput);
				sb.append("output = " + output);

			}
			System.out.print("\n");
		}

		if (verbose)
		{
			System.out.println(sb.toString() + "\n");
		}
		System.out.println("----Exiting configureAndTrainRNN()");
	}

	/**
	 *
	 * @param LEARNSTRING
	 * @param LEARNSTRING_CHARS_LIST
	 * @param verbose
	 * @return
	 */
	private DataSet prepareDataset(char[] LEARNSTRING, List<Character> LEARNSTRING_CHARS_LIST, boolean verbose)
	{
		System.out.println("----Entering prepareDataset()");
		/*
		 * CREATE OUR TRAINING DATA
		 */
		// create input and output arrays: SAMPLE_INDEX, INPUT_NEURON,
		// SEQUENCE_POSITION
		INDArray input = Nd4j.zeros(1, LEARNSTRING_CHARS_LIST.size(), LEARNSTRING.length);
		INDArray labels = Nd4j.zeros(1, LEARNSTRING_CHARS_LIST.size(), LEARNSTRING.length);

		// loop through our sample-sentence
		int samplePos = 0;

		StringBuilder sb = new StringBuilder();

		// Building input and label matrices
		sb.append("Looping through sample sentence:" + new String(LEARNSTRING) + "\n");
		for (char currentChar : LEARNSTRING)
		{
			// small hack: when currentChar is the last, take the first char as
			// nextChar - not really required. Added to this hack by adding a starter first character.
			char nextChar = LEARNSTRING[(samplePos + 1) % (LEARNSTRING.length)];

			sb.append("\nsamplePos = " + samplePos + "currentChar = " + currentChar + "\nnextChar = " + nextChar);

			// input neuron for current-char is 1 at "samplePos"
			input.putScalar(new int[] { 0, LEARNSTRING_CHARS_LIST.indexOf(currentChar), samplePos }, 1);
			sb.append("\nInput NDArray = \n" + input.toString());

			// output neuron for next-char is 1 at "samplePos"
			labels.putScalar(new int[] { 0, LEARNSTRING_CHARS_LIST.indexOf(nextChar), samplePos }, 1);
			// put in 1 in row "LEARNSTRING_CHARS_LIST.indexOf(nextChar)," and col "samplePos", 0 in other places

			sb.append("\nOutput NDArray = \n" + labels.toString());
			samplePos++;
		}

		if (verbose)
		{
			System.out.println(sb.toString());
		}
		System.out.println("----Exiting prepareDataset()");
		return new DataSet(input, labels);
	}

	/**
	 *
	 * @param LEARNSTRING
	 * @param LEARNSTRING_CHARS_LIST
	 * @param verbose
	 * @return
	 */
	private INDArray prepareTestInitDataset(char[] testString, List<Character> LEARNSTRING_CHARS_LIST, boolean verbose)
	{
		System.out.println("testString =" + new String(testString));
		INDArray testInit = Nd4j.zeros(LEARNSTRING_CHARS_LIST.size());

		for (char currentChar : testString)
		{
			testInit.putScalar(LEARNSTRING_CHARS_LIST.indexOf(currentChar), 1);
		}
		// System.out.println("testInit = " + testInit.toString());
		return testInit;
	}
}
