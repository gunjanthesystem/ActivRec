package org.activity.nn;

import java.io.File;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.play.PlayUIServer;
import org.deeplearning4j.ui.storage.FileStatsStorage;

public class NNUI
{

	public static void main(String[] args)
	{
		String pathToStatsFile =
				// "/home/gunjan/EclipseWorkspace/dl4jExamples11June2018/dl4j-examples-master/dl4j-examples/UIStorageExampleStats.dl4j";
				"/home/gunjan/git/GeolifeReloaded2_1_cleaned/nnTrainedStatsLog/UIStorageLSTMCharModelling_SeqRec.dl4j";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/nnTrainedStatsLog/nullTomSawUIStorageLSTMCharModelling_SeqRec.dl4j";
		NNUiViewer(pathToStatsFile);
	}

	public static void NNUiViewer(String pathToStatsFile)
	{
		System.out.println("----> Inside NNUiViewer");
		// Load the saved stats and visualize. Go to http://localhost:9000/train
		StatsStorage statsStorage = new FileStatsStorage(new File(pathToStatsFile));
		// If file already exists: load the data from it
		// UIServer uiServer = UIServer.getInstance();
		UIServer uiServer = getUIServerOnPort(9003);
		uiServer.attach(statsStorage);
	}

	public static UIServer getUIServerOnPort(int portToUse)
	{
		PlayUIServer playUIServer = new PlayUIServer();
		playUIServer.runMain(new String[] { "--uiPort", String.valueOf(portToUse) });
		// UIServer uiServer = playUIServer;
		return playUIServer;
	}
}
