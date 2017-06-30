package org.activity.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.activity.constants.DomainConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;

public class Utils
{

	public static void main(String args[])
	{
		getUserIDsFromMetaFiles("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWrittenNGramBaseline/", "meta.csv",
				"/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWrittenNGramBaseline/UserIDsByFolder.csv");
	}

	/**
	 * 
	 * @param commonPath
	 * @param fileNameToRead
	 * @param fileNameToWrite
	 */
	public static void getUserIDsFromMetaFiles(String commonPath, String fileNameToRead, String fileNameToWrite)
	{
		LinkedHashMap<String, ArrayList<String>> usersInGroups = new LinkedHashMap<>();
		StringBuilder sb = new StringBuilder();
		sb.append("UserGroupFolder,UserID\n");
		try
		{
			String[] userGroups = DomainConstants.gowallaUserGroupsLabels;
			for (String userGroup : userGroups)
			{
				String absfileNameToRead = commonPath + userGroup + "/" + fileNameToRead;

				ArrayList<String> userIDs =
						(ArrayList<String>) ReadingFromFile.oneColumnReaderString(absfileNameToRead, "_", 0, false);

				userIDs.stream().forEach(e -> sb.append(userGroup + "," + e + "\n"));
				usersInGroups.put(userGroup, userIDs);
			}
			WritingToFile.writeToNewFile(sb.toString(), fileNameToWrite);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
