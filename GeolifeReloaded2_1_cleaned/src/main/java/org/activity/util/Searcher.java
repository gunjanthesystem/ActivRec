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
	public static void search(String rootPathToSearch, String fileNamePatternToSearch, String contentToMatch)
	{
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

			System.out.println("--- Root folder: " + Paths.get(rootPathToSearch).toAbsolutePath().toString());
			System.out.println("--- fileNamePatternToSearch: " + fileNamePatternToSearch);
			System.out.println("       num of file matching fileNamePatternToSearch = " + pathsOfFoundFiles.size()
					+ " regular files.");

			System.out.println("--- content in file to search: " + contentToMatch);
			System.out.println("       num of Resultant files = " + pathOfResultantFiles.size());
			System.out.println("       num of Unreadable files = " + pathOfUnreadableFiles.size());
			System.out.println("\n-------\n--- Resultant files:");
			pathOfResultantFiles.forEach(System.out::println);

			if (pathOfUnreadableFiles.size() > 0)
			{
				System.out.println("\n\n--- Unreadable files:");
				pathOfUnreadableFiles.forEach(System.out::println);
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

	}

}
