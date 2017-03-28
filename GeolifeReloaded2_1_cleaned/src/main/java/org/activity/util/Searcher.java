package org.activity.util;

import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Searcher
{

	/**
	 * 
	 * @param rootPathToSearch
	 * @param fileNamePatternToSearch
	 * @param contentToMatch
	 */
	public static String search(String rootPathToSearch, String fileNamePatternToSearch, String contentToMatch)
	{
		StringBuilder res = new StringBuilder();
		// String rootPathToSearch = args[0];// "./";
		// String fileNamePatternToSearch = args[1];// "";
		// String contentToMatch = args[2];// "repo";
		try
		{
			Stream<Path> allPaths = Files.walk(Paths.get(rootPathToSearch), FileVisitOption.FOLLOW_LINKS);

			// find filepaths matching the file name pattern
			List<Path> pathsOfFoundFiles = allPaths.filter(e -> Files.isRegularFile(e))
					.filter(e -> e.toString().contains(fileNamePatternToSearch)).peek(System.out::println)
					.collect(Collectors.toList());

			List<Path> pathOfResultantFiles = new ArrayList<>();

			List<Path> pathOfUnreadableFiles = new ArrayList<>();// find files which contains the givent content.
			for (Path file : pathsOfFoundFiles)
			{
				// System.out.println(file.toString());
				// Files.readAllLines(file).size();
				if (Files.isReadable(file) == false)
				{
					pathOfUnreadableFiles.add(file);
					continue;
				}

				if (Files.readAllLines(file).stream()// .peek(System.out::println)
						.anyMatch(e -> e.contains(contentToMatch)))
				// Files.lines(file).anyMatch(line -> line.contains(contentToMatch)))
				{
					pathOfResultantFiles.add(file);
				}
			}

			res.append("\n--- Root folder: " + Paths.get(rootPathToSearch).toAbsolutePath().toString());
			res.append("\n--- fileNamePatternToSearch: " + fileNamePatternToSearch);
			res.append("\n       num of file matching fileNamePatternToSearch = " + pathsOfFoundFiles.size()
					+ " regular files.");

			res.append("\n--- content in file to search: " + contentToMatch);
			res.append("\n       num of Resultant files = " + pathOfResultantFiles.size());
			res.append("\n       num of Unreadable files = " + pathOfUnreadableFiles.size());
			res.append("\n-------\n--- Resultant files:\n");

			pathOfResultantFiles.forEach(e -> res.append("\n").append(e.toString()));// System.out::println);

			if (pathOfUnreadableFiles.size() > 0)
			{
				res.append("\n\n--- Unreadable files:");
				pathOfUnreadableFiles.forEach(e -> res.append("\n").append(e.toString()));// System.out::println);
			}
			// pathsOfFoundFiles.stream().filter(path -> Files.lines(path).anyMatch(line ->
			// line.contains(contentToMatch)))
			// .forEach(System.out::println);
			allPaths.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res.toString();
	}

}
