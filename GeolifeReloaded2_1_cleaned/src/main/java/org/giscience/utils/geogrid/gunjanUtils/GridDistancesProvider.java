package org.giscience.utils.geogrid.gunjanUtils;

import java.util.HashMap;

import org.activity.io.Serializer;
import org.activity.objects.PairedIndicesTo1DArrayConverter;

public class GridDistancesProvider
{
	HashMap<Integer, Double> gridIndexPairHaversineDist;
	PairedIndicesTo1DArrayConverter pairedIndicesTo1DArrayConverter;

	public GridDistancesProvider(String pathToSerialisedGridIndexPairDist,
			String pathToSerialisedGridIndexPairDistConverter)
	{
		HashMap<Integer, Double> gridIndexPairHaversineDist = (HashMap<Integer, Double>) Serializer
				.kryoDeSerializeThis(pathToSerialisedGridIndexPairDist);
		System.out.println("deserialised gridIndexPairHaversineDist.size()= " + gridIndexPairHaversineDist.size());
		PairedIndicesTo1DArrayConverter pairedIndicesTo1DArrayConverter = (PairedIndicesTo1DArrayConverter) Serializer
				.kryoDeSerializeThis(pathToSerialisedGridIndexPairDistConverter);
		System.out
				.println("deserialised pairedIndicesTo1DArrayConverter= " + pairedIndicesTo1DArrayConverter.toString());
	}
}
