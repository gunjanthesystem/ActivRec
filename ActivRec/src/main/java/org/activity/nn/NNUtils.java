package org.activity.nn;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.util.Precision;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class NNUtils
{
	/**
	 * 
	 * @param network
	 * @param verbose
	 * @return
	 */
	public static int getNumberOfParameters(MultiLayerNetwork net, boolean verbose)
	{
		int numOfParams = net.numParams();
		if (verbose)
		{
			StringBuilder sb = new StringBuilder("Total number of parameters: " + numOfParams + "\n");
			for (int i = 0; i < net.getnLayers(); i++)
			{
				sb.append("Layer " + i + " number of parameters: " + net.getLayer(i).numParams() + "\n");
			}
			System.out.println(sb.toString());
		}
		return numOfParams;
	}

	/**
	 * 
	 * @param probDistribution
	 * @param iter
	 * @return char-probablity
	 */
	public static Map<Character, Double> getCharProbMap(double[] probDistribution, CharIteratorJun2018 iter)
	{
		Map<Character, Double> charProbMap = new TreeMap<>();
		for (int index = 0; index < probDistribution.length; index++)
		{
			charProbMap.put(new Character(iter.convertIndexToCharacter(index)), probDistribution[index]);
		}
		return charProbMap;
	}

	/**
	 * <p>
	 * Sanity checked okay
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescNoShuffle(Map<K, V> map)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>()
			{
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				{
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * 
	 * @param array
	 * @param decimalPlaces
	 * @return
	 */
	public static double[] roundTheArrayVals(double[] array, int decimalPlaces)
	{
		double[] res = new double[array.length];

		for (int i = 0; i < array.length; i++)
		{
			res[i] = Precision.round(array[i], decimalPlaces);
		}
		return res;
	}

	/**
	 * 
	 * @param charProbMap
	 * @param roundedToDecimals
	 *            (just for printing)
	 * @return
	 */
	public static String getCharProbForPrint(Map<Character, Double> charProbMap, int roundedToDecimals)
	{
		// Map<Character, Double> charProbMapSorted = sortByValueDescNoShuffle(charProbMap);
		StringBuilder sb = new StringBuilder();
		for (Entry<Character, Double> e : charProbMap.entrySet())
		{
			Character ch = e.getKey();
			double roundedVal = Precision.round(e.getValue(), roundedToDecimals);
			if ((char) ch == '\n')
			{
				sb.append("\t" + "\\n" + "-" + roundedVal);
			}
			else if ((char) ch == '\t')
			{
				sb.append("\t" + "\\t" + "-" + roundedVal);
			}
			else
			{
				sb.append("\t" + e.getKey() + "-" + roundedVal);
			}
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param arr
	 * @return
	 */
	public static char[] listToCharArr(List<Character> listOfChars)
	{
		char[] res = new char[listOfChars.size()];
		int index = 0;
		for (Character ch : listOfChars)
		{
			res[index++] = ch;
		}
		return res;
	}

	/**
	 * 
	 * @param iter
	 * @param numSamples
	 * @param init
	 * @return 3d array {numOfSample,numOfUniqueCHars,lengthOfInitialisation}
	 */
	public static INDArray createInputTensor(CharIteratorJun2018 iter, int numSamples, char[] init)
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

}
