package org.activity.nn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.play.PlayUIServer;
import org.deeplearning4j.ui.storage.FileStatsStorage;

public class NNUI
{

	public static void main(String[] args) throws IOException
	{
		Set<Path> pathToModelsToRead = new TreeSet<>();

		try (Stream<Path> paths = Files
				.walk(Paths.get("/home/gunjan/git/GeolifeReloaded2_1_cleaned/nnTrainedStatsLog")))
		{
			paths.filter(Files::isRegularFile).peek(System.out::println).forEachOrdered(e -> pathToModelsToRead.add(e));
		}

		System.out.println("pathToModelsToRead.size()=" + pathToModelsToRead.size());

		if (false)
		{
			String pathToStatsFile =
					// "/home/gunjan/EclipseWorkspace/dl4jExamples11June2018/dl4j-examples-master/dl4j-examples/UIStorageExampleStats.dl4j";
					"/home/gunjan/git/GeolifeReloaded2_1_cleaned/nnTrainedStatsLog/UIStorageLSTMCharModelling_SeqRec.dl4j";
			// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/nnTrainedStatsLog/nullTomSawUIStorageLSTMCharModelling_SeqRec.dl4j";
			NNUiViewer(pathToStatsFile, 9003);
		}

		int portStart = 9001;
		for (Path pathToModel : pathToModelsToRead)
		{
			String pathString = pathToModel.toString();
			String[] splittedString = pathString.split("/");
			String label = splittedString[splittedString.length - 1].replace("UIStorageSeqRec", "").replace(".dl4j",
					"");
			// System.out.println(pathToModel.toString());
			System.out.println("splittedString = " + label);
			NNUiViewer(pathString, portStart++);
		}
	}

	public static void NNUiViewer(String pathToStatsFile, int portNum)
	{
		System.out.println("----> Inside NNUiViewer");
		// Load the saved stats and visualize. Go to http://localhost:9000/train
		StatsStorage statsStorage = new FileStatsStorage(new File(pathToStatsFile));
		// If file already exists: load the data from it
		// UIServer uiServer = UIServer.getInstance();
		UIServer uiServer = getUIServerOnPort(portNum);// 9003
		uiServer.attach(statsStorage);

	}

	public static UIServer getUIServerOnPort(int portToUse)
	{
		PlayUIServer playUIServer = new PlayUIServer();
		playUIServer.runMain(new String[] { "--uiPort", String.valueOf(portToUse) });
		// UIServer uiServer = playUIServer;
		// playUIServer.
		return playUIServer;
	}
}
