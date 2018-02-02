package org.activity.io;

import java.io.InputStream;
import java.util.List;

import org.activity.objects.Pair;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SFTPFile
{

	static final String howitzerUsr = "gunjan";
	static final String howitzerHost = "howitzer.ucd.ie";
	static final int port = 22;

	public static void main(String args[])
	{

		String remoteFile = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/AllPerDirectTopKAgreements_0.csv";

		try
		{
			Pair<InputStream, Session> inputAndSession = getInputStreamForSFTPFile(howitzerHost, port, remoteFile,
					howitzerUsr,
					ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/serverInfo.kry", ",", 0, false).get(0));
			InputStream out = inputAndSession.getFirst();

			// BufferedReader br = new BufferedReader(new InputStreamReader(out));
			// String line;
			// while ((line = br.readLine()) != null)
			// System.out.println(line);
			// br.close();

			// List<Double> res = ReadingFromFile.oneColumnReaderDouble(out, ",", 0, false);
			// System.out.println("res = " + res);

			List<List<Double>> res2 = ReadingFromFile.nColumnReaderDouble(out, ",", false);
			System.out.println("res = " + res2);
			inputAndSession.getSecond().disconnect();

		}
		catch (Exception e)
		{
			System.err.print(e);
		}
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param remoteFile
	 * @param user
	 * @param passwd
	 * @return
	 */
	public static Pair<InputStream, Session> getInputStreamForSFTPFile(String host, int port, String remoteFile,
			String user, String passwd)
	{
		InputStream out = null;
		Session session = null;

		try
		{
			JSch jsch = new JSch();

			session = jsch.getSession(user, host, port);
			session.setPassword(passwd);

			session.setConfig("StrictHostKeyChecking", "no");
			System.out.println("Establishing Connection...");
			session.connect();
			System.out.println("Connection established.");
			System.out.println("Creating SFTP Channel.");
			ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();
			System.out.println("SFTP Channel created.");

			out = sftpChannel.get(remoteFile);

		}
		catch (Exception e)
		{
			System.out.println("Exception to read: " + remoteFile);
			System.out.println("user=" + user + " passwd=" + passwd + " host=" + host);
			e.printStackTrace();
		}

		return new Pair<InputStream, Session>(out, session);
	}

	// public static void main(String args[])
	// {
	// String user = "gunjan";
	// String password = "rasgullakaseoul";
	// String host = "howitzer.ucd.ie";
	// int port = 22;
	//
	// String remoteFile =
	// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/AllPerDirectTopKAgreements_0.csv";
	//
	// try
	// {
	// JSch jsch = new JSch();
	// Session session = jsch.getSession(user, host, port);
	// session.setPassword(password);
	// session.setConfig("StrictHostKeyChecking", "no");
	// System.out.println("Establishing Connection...");
	// session.connect();
	// System.out.println("Connection established.");
	// System.out.println("Creating SFTP Channel.");
	// ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
	// sftpChannel.connect();
	// System.out.println("SFTP Channel created.");
	//
	// InputStream out = null;
	// out = sftpChannel.get(remoteFile);
	// BufferedReader br = new BufferedReader(new InputStreamReader(out));
	// String line;
	// while ((line = br.readLine()) != null)
	// System.out.println(line);
	// br.close();
	// }
	// catch (Exception e)
	// {
	// System.err.print(e);
	// }
	// }
}