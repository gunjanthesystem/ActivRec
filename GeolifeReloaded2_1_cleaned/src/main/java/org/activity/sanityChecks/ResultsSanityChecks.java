package org.activity.sanityChecks;

import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sanity checks the output of experiments for symptoms of issues
 * 
 * @author gunjan
 *
 */
public class ResultsSanityChecks
{

	public ResultsSanityChecks()
	{

	}

	public static void main(String v[])
	{
		// String rootPath =
		// "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/APR12ED1.0AllActsFDStFilter0hrs/All/";
		String rootPath = "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/APR12ED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrs";
		assertSameNumOfRTsAcrossAllMUsForUsers(rootPath, true);
	}

	public static boolean assertSameNumOfRTsAcrossAllMUsForUsers(String rootPath, boolean verbose)
	{
		boolean isSame = true;

		String nameOfRTCountFile = "CountTimeCategoryOfRecommPoitns.csv";
		Set<Path> filesToMatch = getFileswithNamePattern(rootPath, nameOfRTCountFile, verbose);

		List<Path> filesToMatchAsList = new ArrayList<>(filesToMatch);

		StringBuilder sb = new StringBuilder();
		sb.append("num of filesToMatchAsList = " + filesToMatchAsList.size() + "\n");

		try
		{
			for (int i = 0; i < filesToMatchAsList.size(); i++)
			{
				for (int j = i + 1; j < filesToMatchAsList.size(); j++)
				{
					// System.out.println("i = " + i + " j =" + j);
					String file1Content = new String(Files.readAllBytes(filesToMatchAsList.get(i)));
					String file2Content = new String(Files.readAllBytes(filesToMatchAsList.get(j)));

					sb.append("\n---------file1Content=\n" + file1Content + "\n");
					sb.append("\n---------file2Content=\n" + file2Content + "\n");

					if (file1Content.equals(file2Content) == false)
					{
						System.out.println("\nassertSameNumOfRTsAcrossAllMUsForUsers FAILS: \nfile:"
								+ filesToMatchAsList.get(i) + " and \nfile:" + filesToMatchAsList.get(j)
								+ "\n\t have DIFFERENT content !!");
						return false;
					}

				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (verbose)
		{
			System.out.println(sb.toString());
		}
		System.out.println("\nassertSameNumOfRTsAcrossAllMUsForUsers success: all files in: " + rootPath
				+ " having filename matching pattern:" + nameOfRTCountFile + "\n\t have IDENTICAL content");

		return isSame;
	}

	public static Set<Path> getFileswithNamePattern(String rootPathToSearch, String fileNamePatternToSearch,
			boolean verbose)
	{
		Set<Path> pathsOfFoundFiles = new TreeSet<>(); // filepaths matching the file name pattern

		StringBuilder res = new StringBuilder();
		res.append("\n--- Inside getFileswithNamePattern\n---  Root folder: "
				+ Paths.get(rootPathToSearch).toAbsolutePath().toString());
		res.append("\n--- fileNamePatternToSearch: " + fileNamePatternToSearch);

		try
		{
			Stream<Path> allPaths = Files.walk(Paths.get(rootPathToSearch), FileVisitOption.FOLLOW_LINKS);
			res.append("\n\n---   found files with names matching '" + fileNamePatternToSearch + "':\n ");

			// find filepaths matching the file name pattern
			pathsOfFoundFiles = allPaths.filter(e -> Files.isRegularFile(e))
					.filter(e -> e.toString().contains(fileNamePatternToSearch))
					.peek(e -> res.append("\t-" + e.toString() + "\n")).collect(Collectors.toSet());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (verbose)
		{
			System.out.println(res.toString() + "\n");
		}
		return pathsOfFoundFiles;
	}

}
