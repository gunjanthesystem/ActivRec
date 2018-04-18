package org.activity.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.activity.constants.DomainConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;

public class Utils
{
	static String mortarUsr;
	static String engineUsr;
	static final String howitzerUsr = engineUsr = mortarUsr = "gunjan";
	static final String clarityUsr = "gunjankumar";
	static final String howitzerHost = "howitzer.ucd.ie";
	static final String engineHost = "theengine.ucd.ie";
	static final String mortarHost = "mortar.ucd.ie";
	static final String clarityHost = "claritytrec.ucd.ie";
	static final String localHost = "local";

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

				ArrayList<String> userIDs = (ArrayList<String>) ReadingFromFile.oneColumnReaderString(absfileNameToRead,
						"_", 0, false);

				userIDs.stream().forEach(e -> sb.append(userGroup + "," + e + "\n"));
				usersInGroups.put(userGroup, userIDs);
			}
			WToFile.writeToNewFile(sb.toString(), fileNameToWrite);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param host
	 * @return
	 */
	protected static String getPassWordForHost(String host)
	{
		String passwd = null;
		switch (host)
		{
			case Utils.howitzerHost:
				passwd = ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/serverInfo.kry", ",", 0, false)
						.get(0);
				break;
			case Utils.mortarHost:
				passwd = ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/serverInfo.kry", ",", 1, false)
						.get(0);
				break;
			case Utils.engineHost:
				passwd = ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/serverInfo.kry", ",", 2, false)
						.get(0);
				break;
			case Utils.clarityHost:
				passwd = ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/serverInfo.kry", ",", 3, false)
						.get(0);
				break;

		}
		return passwd;
	}

	/**
	 * 
	 * @param host
	 * @return
	 */
	protected static String getUserForHost(String host)
	{
		switch (host)
		{
			case Utils.clarityHost:
				return Utils.clarityUsr;
			case Utils.mortarHost:
				return Utils.mortarUsr;
			case Utils.engineHost:
				return Utils.engineUsr;
			case Utils.howitzerHost:
				return Utils.howitzerUsr;

		}
		return host;
	}

}
