package org.activity.io;

public class ServerUtils
{
	static String mortarUsr;
	static String engineUsr;
	public static final String howitzerUsr = engineUsr = mortarUsr = "gunjan";
	public static final String clarityUsr = "gunjankumar";
	public static final String howitzerHost = "howitzer.ucd.ie";
	public static final String engineHost = "theengine.ucd.ie";
	public static final String mortarHost = "mortar.ucd.ie";
	public static final String clarityHost = "claritytrec.ucd.ie";
	public static final String localHost = "local";

	/**
	 * 
	 * @param host
	 * @return
	 */
	public static String getPassWordForHost(String host)
	{
		String passwd = null;
		switch (host)
		{
		case howitzerHost:
			passwd = ReadingFromFile.oneColumnReaderString("./dataToPRead/Jan26/serverInfo.kry", ",", 0, false).get(0);
			break;
		case mortarHost:
			passwd = ReadingFromFile.oneColumnReaderString("./dataToPRead/Jan26/serverInfo.kry", ",", 1, false).get(0);
			break;
		case engineHost:
			passwd = ReadingFromFile.oneColumnReaderString("./dataToPRead/Jan26/serverInfo.kry", ",", 2, false).get(0);
			break;
		case clarityHost:
			passwd = ReadingFromFile.oneColumnReaderString("./dataToPRead/Jan26/serverInfo.kry", ",", 3, false).get(0);
			break;

		}
		return passwd;
	}

	/**
	 * 
	 * @param host
	 * @return
	 */
	public static String getUserForHost(String host)
	{
		switch (host)
		{
		case clarityHost:
			return clarityUsr;
		case mortarHost:
			return mortarUsr;
		case engineHost:
			return engineUsr;
		case howitzerHost:
			return howitzerUsr;

		}
		return host;
	}

}
