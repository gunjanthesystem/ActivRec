package org.activity.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.activity.io.WritingToFile;
import org.activity.objects.Triple;
import org.activity.probability.ProbabilityUtilityBelt;

public class Searcher
{

	/**
	 * <p>
	 * Specifically created for large files
	 * <p>
	 * Sanity checked
	 * 
	 * @param absFileNameToSearchIn
	 * @param stringToSearch
	 * @param ignoreCase
	 * @return
	 */
	public static boolean fileContainsString(Path absFileNameToSearchIn, String stringToSearch, boolean ignoreCase)
	{
		boolean found = false;
		long numLines = 0;
		String line;
		try
		{
			BufferedReader br = Files.newBufferedReader(absFileNameToSearchIn);
			while ((line = br.readLine()) != null)
			{
				if (ignoreCase)
				{
					if (line.toLowerCase().contains(stringToSearch.toLowerCase()))
					{
						System.out.println("line=\n" + line);
						return true;
					}
				}
				else
				{
					if (line.contains(stringToSearch))
					{
						System.out.println("line=\n" + line);
						return true;
					}
				}
				numLines += 1;
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//
		return found;
	}

	/**
	 * Returns the list of lines in the file absFileNameToSearchIn which contains the string stringToSearch
	 * <p>
	 * Specifically created for large files
	 * <p>
	 * 
	 * 
	 * @param absFileNameToSearchIn
	 * @param stringToSearch
	 * @param ignoreCase
	 * @return List{lines containing the stringToSearch}
	 */
	public static List<String> fileContainsString2(Path absFileNameToSearchIn, String stringToSearch,
			boolean ignoreCase)
	{
		List<String> linesContainingTargetString = new ArrayList<>();
		long numLines = 0;
		String line;
		try
		{
			BufferedReader br = Files.newBufferedReader(absFileNameToSearchIn);
			while ((line = br.readLine()) != null)
			{
				if (ignoreCase)
				{
					if (line.toLowerCase().contains(stringToSearch.toLowerCase()))
					{
						linesContainingTargetString.add(line);
					}
				}
				else
				{
					if (line.contains(stringToSearch))
					{
						linesContainingTargetString.add(line);
					}
				}
				numLines += 1;
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//
		return linesContainingTargetString;
	}

	/**
	 * 
	 * @param rootPathToSearch
	 * @param fileNamePatternToSearch
	 * @param contentToMatch
	 */
	public static String search(String rootPathToSearch, String fileNamePatternToSearch, String contentToMatch)
	{
		StringBuilder res = new StringBuilder();
		res.append("\n--- Root folder: " + Paths.get(rootPathToSearch).toAbsolutePath().toString());
		res.append("\n--- fileNamePatternToSearch: " + fileNamePatternToSearch);
		res.append("\n--- content in file to search: " + contentToMatch);
		// String rootPathToSearch = args[0];// "./";
		// String fileNamePatternToSearch = args[1];// "";
		// String contentToMatch = args[2];// "repo";
		try
		{
			Stream<Path> allPaths = Files.walk(Paths.get(rootPathToSearch), FileVisitOption.FOLLOW_LINKS);

			res.append("\n---   found files matching fileNamePatternToSearch:\n ");

			// find filepaths matching the file name pattern
			List<Path> pathsOfFoundFiles = allPaths.filter(e -> Files.isRegularFile(e))
					.filter(e -> e.toString().contains(fileNamePatternToSearch))
					.peek(e -> res.append("\t-" + e.toString() + "\n")).collect(Collectors.toList());
			res.append("\n---   num of files matching " + fileNamePatternToSearch + " = " + pathsOfFoundFiles.size()
					+ " regular files.");

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

			res.append("\n---   num of Resultant files matching " + fileNamePatternToSearch + " containing "
					+ contentToMatch + " = " + pathOfResultantFiles.size());

			res.append("\n---   Resultant Files matching " + fileNamePatternToSearch + " containing " + contentToMatch
					+ ":\n");
			pathOfResultantFiles.forEach(e -> res.append("\t-" + e.toString() + "\n"));// System.out::println);

			res.append("\n---   num of Unreadable files = " + pathOfUnreadableFiles.size());
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

	/**
	 * 
	 * @param rootPathToSearch
	 * @param fileNamePatternToSearch
	 * @param contentToMatch
	 * @param absFileNameOfMatchedLinesToWrite
	 *            if non-empty, then write the matched lines to this file
	 * @return ( Set of files whose name contain fileNamePatternToSearch, Set of files whose name contain
	 *         fileNamePatternToSearch and content contains contentToMatch, Result log)
	 */
	public static Triple<Set<Path>, Set<Path>, String> search2(String rootPathToSearch, String fileNamePatternToSearch,
			String contentToMatch, String absFileNameOfMatchedLinesToWrite)
	{
		Set<Path> pathsOfFoundFiles = new TreeSet<>(); // filepaths matching the file name pattern
		Set<Path> pathsOfResultantFiles = new TreeSet<>();// filepaths matching the file name pattern and contains
															// contentToMatch
		Set<Path> pathsOfUnreadableFiles = new TreeSet<>();// find files which contains the givent content.

		StringBuilder res = new StringBuilder();
		res.append(
				"\n--- Inside search2\n---  Root folder: " + Paths.get(rootPathToSearch).toAbsolutePath().toString());
		res.append("\n--- fileNamePatternToSearch: " + fileNamePatternToSearch);
		res.append("\n--- content in file to search: " + contentToMatch);
		res.append("\n absFileNameOfMatchedLinesToWrite: " + absFileNameOfMatchedLinesToWrite);
		try
		{
			Stream<Path> allPaths = Files.walk(Paths.get(rootPathToSearch), FileVisitOption.FOLLOW_LINKS);

			res.append("\n\n---   found files with names matching '" + fileNamePatternToSearch + "':\n ");
			// find filepaths matching the file name pattern
			pathsOfFoundFiles = allPaths.filter(e -> Files.isRegularFile(e))
					.filter(e -> e.toString().contains(fileNamePatternToSearch))
					.peek(e -> res.append("\t-" + e.toString() + "\n")).collect(Collectors.toSet());
			res.append("---   num of files matching " + fileNamePatternToSearch + " = " + pathsOfFoundFiles.size()
					+ " regular files.");

			for (Path file : pathsOfFoundFiles)
			{
				// System.out.println(file.toString());
				// Files.readAllLines(file).size();
				if (Files.isReadable(file) == false)
				{
					pathsOfUnreadableFiles.add(file);
					continue;
				}

				// // Start of disabled on 12 Feb 2018 in order to read very large files
				// if (Files.readAllLines(file).stream()// .peek(System.out::println)
				// .anyMatch(e -> e.contains(contentToMatch)))
				// // Files.lines(file).anyMatch(line -> line.contains(contentToMatch)))
				// // End of disabled on 12 Feb 2018 in order to read very large files*/

				if (absFileNameOfMatchedLinesToWrite.length() < 1)
				{// no need to write matched lines to file
					if (fileContainsString(file, contentToMatch, true) == true)
					{
						pathsOfResultantFiles.add(file);
					}
				}
				else
				{// write matched lines to file
					List<String> linesContainingMatchedString = fileContainsString2(file, contentToMatch, true);

					// System.out.println("linesContainingMatchedString=\n" + linesContainingMatchedString);
					String s = // "File: " + file + "\nLines containing " + contentToMatch + ":-\n"
							"\n" + linesContainingMatchedString.stream().collect(Collectors.joining("\n"));
					// System.out.println(
					// "linesContainingMatchedString=\n" + linesContainingMatchedString + "\ns=" + s.toString());
					if (linesContainingMatchedString.size() > 0)
					{
						WritingToFile.appendLineToFileAbsolute(s, absFileNameOfMatchedLinesToWrite);
						pathsOfResultantFiles.add(file);
					}
				}
			}

			res.append("\n\n---   Resultant Files matching " + fileNamePatternToSearch + " containing " + contentToMatch
					+ ":\n");
			pathsOfResultantFiles.forEach(e -> res.append("\t-" + e.toString() + "\n"));// System.out::println);
			res.append("---   num of Resultant files matching " + fileNamePatternToSearch + " containing "
					+ contentToMatch + " = " + pathsOfResultantFiles.size());

			res.append("\n\n---   num of Unreadable files = " + pathsOfUnreadableFiles.size());
			if (pathsOfUnreadableFiles.size() > 0)
			{
				res.append("\n\n--- Unreadable files:");
				pathsOfUnreadableFiles.forEach(e -> res.append("\n").append(e.toString()));// System.out::println);
			}
			// pathsOfFoundFiles.stream().filter(path -> Files.lines(path).anyMatch(line ->
			// line.contains(contentToMatch)))
			// .forEach(System.out::println);
			allPaths.close();
			res.append("\n--- Exiting search2----------\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new Triple<Set<Path>, Set<Path>, String>(pathsOfFoundFiles, pathsOfResultantFiles, res.toString());// res.toString();
	}

	/**
	 * Find files which contains fileNamePatternToSearch in the name and contains contentToMatch in its contents. And
	 * delete files having fileNamePatternToSearch in the name and NOT having contentToMatch in content with probability
	 * of probabilityToDeleteFilesMatchingNameButNotContent.
	 * 
	 * @param rootPathToSearch
	 * @param fileNamePatternToSearch
	 * @param contentToMatch
	 * @param ratioOfSafeFilesToDelete
	 * @return
	 */
	public static String searchAndRandomDelete2(String rootPathToSearch, String fileNamePatternToSearch,
			List<String> listOfContentsToMatch, double ratioOfSafeFilesToDelete)
	{
		StringBuilder res = new StringBuilder("Inside searchAndRandomDelete2");
		res.append("\n--- Root folder: " + Paths.get(rootPathToSearch).toAbsolutePath().toString());
		res.append("\n--- fileNamePatternToSearch: " + fileNamePatternToSearch);
		res.append("\n--- content in file to search: " + listOfContentsToMatch);
		res.append("\n--- ratioOfSafeFilesToDelete: " + ratioOfSafeFilesToDelete);
		res.append("\n***** Will delete files having '" + fileNamePatternToSearch + "' in the name and not having '"
				+ listOfContentsToMatch + "' in content" + " with ratioOfSafeFilesToDelete of "
				+ ratioOfSafeFilesToDelete + "*****");
		try
		{
			Set<Path> filesWithNameMatchOverAllContentsToMatch = new LinkedHashSet<>(); // one item for each content to
																						// match
			Set<Path> filesWithNameAndContentMatchOverAllContentsToMatch = new LinkedHashSet<>();

			for (String contentToMatch : listOfContentsToMatch)
			{
				Triple<Set<Path>, Set<Path>, String> result = search2(rootPathToSearch, fileNamePatternToSearch,
						contentToMatch, "");
				filesWithNameMatchOverAllContentsToMatch.addAll(result.getFirst());
				filesWithNameAndContentMatchOverAllContentsToMatch.addAll(result.getSecond());

				res.append("\n------------For content to match: " + contentToMatch + "------------\n");
				res.append(result.getThird().toString()
						+ "\n--------------------------------------------------------------------\n");
			}
			res.append("pathsOfFoundFilesOverAllContentsToMatch.size()="
					+ filesWithNameMatchOverAllContentsToMatch.size());
			filesWithNameMatchOverAllContentsToMatch.forEach(e -> res.append("\n").append(e.toString()));
			res.append("\n");

			res.append("\npathsOfResultantFilessOverAllContentsToMatch.size()="
					+ filesWithNameAndContentMatchOverAllContentsToMatch.size());
			filesWithNameAndContentMatchOverAllContentsToMatch.forEach(e -> res.append("\n").append(e.toString()));
			res.append("\n");
			/////// new to this method wrt to search()

			// Paths of files which match the file name pattern, do not contain the content to search for and has been
			// selected for deletion. Primary motivation for deletion to save storage space.
			List<Path> canBeDeletedFilesNotHavContent = new ArrayList<>(filesWithNameMatchOverAllContentsToMatch);
			canBeDeletedFilesNotHavContent.removeAll(filesWithNameAndContentMatchOverAllContentsToMatch);

			int numOfFilesToDelete = (int) (ratioOfSafeFilesToDelete * canBeDeletedFilesNotHavContent.size());
			res.append("\n numOfFilesToDelete=" + numOfFilesToDelete + "\n");

			List<Path> toBeDeletedFilesNotHavContent = ProbabilityUtilityBelt
					.selectNObjsWithoutReplacement(canBeDeletedFilesNotHavContent, numOfFilesToDelete);

			if (canBeDeletedFilesNotHavContent.size() == 0)
			{
				res.append(
						"\n\n NO FILES COULD BE DELETED as all files with matching filename have the content,\n i.e.,pathsOfFoundFiles.size():"
								+ filesWithNameMatchOverAllContentsToMatch.size() + "= pathsOfResultantFiles.size():"
								+ filesWithNameAndContentMatchOverAllContentsToMatch.size());
			}

			res.append("\n---   Resultant Files which can be deleted:\n");
			canBeDeletedFilesNotHavContent.forEach(e -> res.append("\t-" + e.toString() + "\n"));

			res.append("\n---   canBeDeletedFilesNotHavContent.size=" + canBeDeletedFilesNotHavContent.size()
					+ "\tfilesWithNameMatchOverAllContentsToMatch.size="
					+ filesWithNameMatchOverAllContentsToMatch.size()
					+ "\t% of filesWithNameMatchOverAllContentsToMatch which can be deleted="
					+ (canBeDeletedFilesNotHavContent.size() * 1.0 / filesWithNameMatchOverAllContentsToMatch.size()));

			res.append("\n---   Resultant Files to be deleted:\n");
			toBeDeletedFilesNotHavContent.forEach(e -> res.append("\t-" + e.toString() + "\n"));

			res.append("\n---   toBeDeletedFilesNotHavContent.size=" + toBeDeletedFilesNotHavContent.size()
					+ "\tfilesWithNameMatchOverAllContentsToMatch.size="
					+ filesWithNameMatchOverAllContentsToMatch.size()
					+ "\t% of filesWithNameMatchOverAllContentsToMatch to be deleted="
					+ (toBeDeletedFilesNotHavContent.size() * 1.0 / filesWithNameMatchOverAllContentsToMatch.size())
					+ "\n---   % of canBeDeletedFilesNotHavContent to be deleted="
					+ (toBeDeletedFilesNotHavContent.size() * 1.0 / canBeDeletedFilesNotHavContent.size()));

			System.out.println(" deleting files started");
			toBeDeletedFilesNotHavContent.parallelStream().forEach(f ->
				{
					try
					{
						Files.delete(f);
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				});
			System.out.println(" deleting files end");

			res.append("\n --------- \n");
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res.toString();
	}

	/**
	 * Find files which contains fileNamePatternToSearch in the name and contains contentToMatch in its contents. And
	 * delete files having fileNamePatternToSearch in the name and NOT having contentToMatch in content with probability
	 * of probabilityToDeleteFilesMatchingNameButNotContent.
	 * 
	 * @param rootPathToSearch
	 * @param fileNamePatternToSearch
	 * @param contentToMatch
	 * @param probabilityToDeleteFilesMatchingNameButNotContent
	 * @return
	 */
	public static String searchAndRandomDelete(String rootPathToSearch, String fileNamePatternToSearch,
			String contentToMatch, double probabilityToDeleteFilesMatchingNameButNotContent)
	{
		StringBuilder res = new StringBuilder();
		res.append("\n--- Root folder: " + Paths.get(rootPathToSearch).toAbsolutePath().toString());
		res.append("\n--- fileNamePatternToSearch: " + fileNamePatternToSearch);
		res.append("\n--- content in file to search: " + contentToMatch);
		res.append("\n--- probabilityToDeleteFilesMatchingNameButNotContent: "
				+ probabilityToDeleteFilesMatchingNameButNotContent);
		res.append("\n***** Will delete files having '" + fileNamePatternToSearch + "' in the name and not having '"
				+ contentToMatch + "' in content" + " with probability of "
				+ probabilityToDeleteFilesMatchingNameButNotContent + "*****");
		// String rootPathToSearch = args[0];// "./";
		// String fileNamePatternToSearch = args[1];// "";
		// String contentToMatch = args[2];// "repo";
		try
		{
			Stream<Path> allPaths = Files.walk(Paths.get(rootPathToSearch), FileVisitOption.FOLLOW_LINKS);

			res.append("\n\n---   found files with names matching fileNamePatternToSearch:\n ");

			// find filepaths matching the file name pattern
			Set<Path> pathsOfFoundFiles = allPaths.filter(e -> Files.isRegularFile(e))
					.filter(e -> e.toString().contains(fileNamePatternToSearch))
					.peek(e -> res.append("\t-" + e.toString() + "\n")).collect(Collectors.toSet());
			res.append("---   num of files with names matching " + fileNamePatternToSearch + " = "
					+ pathsOfFoundFiles.size() + " regular files.");

			Set<Path> pathsOfResultantFiles = new TreeSet<>();
			Set<Path> pathsOfUnreadableFiles = new TreeSet<>();// find files which contains the givent content.

			for (Path file : pathsOfFoundFiles)
			{
				// System.out.println(file.toString());
				// Files.readAllLines(file).size();
				if (Files.isReadable(file) == false)
				{
					pathsOfUnreadableFiles.add(file);
					continue;
				}

				if (Files.readAllLines(file).stream()// .peek(System.out::println)
						.anyMatch(e -> e.contains(contentToMatch)))
				// Files.lines(file).anyMatch(line -> line.contains(contentToMatch)))
				{
					pathsOfResultantFiles.add(file);
				}
			}

			res.append("\n\n---   Resultant Files matching " + fileNamePatternToSearch + " containing " + contentToMatch
					+ ":\n");
			pathsOfResultantFiles.forEach(e -> res.append("\t-" + e.toString() + "\n"));// System.out::println);
			res.append("--   num of Resultant files matching " + fileNamePatternToSearch + " containing "
					+ contentToMatch + " = " + pathsOfResultantFiles.size() + "\n");

			res.append("\n---   num of Unreadable files = " + pathsOfUnreadableFiles.size());
			if (pathsOfUnreadableFiles.size() > 0)
			{
				res.append("\n\n--- Unreadable files:");
				pathsOfUnreadableFiles.forEach(e -> res.append("\n").append(e.toString()));// System.out::println);
			}
			// pathsOfFoundFiles.stream().filter(path -> Files.lines(path).anyMatch(line ->
			// line.contains(contentToMatch)))
			// .forEach(System.out::println);
			/////// new to this method wrt to search()

			// Paths of files which match the file name pattern, do not contain the content to search for and has been
			// selected for deletion. Primary motivation for deletion to save storage space.
			Set<Path> pathsOfDeletedFileNotHavContent = new TreeSet<>();

			for (Path pathOfFoundFile : pathsOfFoundFiles)
			{
				// Only delete file if it does not contain the content to search for
				if (pathsOfResultantFiles.contains(pathOfFoundFile) == false)
				{
					if (ProbabilityUtilityBelt.trueWithProbability(probabilityToDeleteFilesMatchingNameButNotContent))
					{
						pathsOfDeletedFileNotHavContent.add(pathOfFoundFile);
						Files.delete(pathOfFoundFile);
					}
				}
			}

			if (pathsOfDeletedFileNotHavContent.size() == 0)
			{
				res.append(
						"\n\n NO FILES COULD BE DELETED as all files with matching filename have the content,\n i.e.,pathsOfFoundFiles.size():"
								+ pathsOfFoundFiles.size() + "= pathsOfResultantFiles.size():"
								+ pathsOfResultantFiles.size());
			}
			res.append("\n\npathsOfDeletedFileNotHavContent.size=" + pathsOfDeletedFileNotHavContent.size()
					+ "\n% of found files selected for deletion="
					+ (pathsOfDeletedFileNotHavContent.size() * 1.0 / pathsOfFoundFiles.size()));
			res.append("\n---   Resultant Files to be deleted:\n");
			pathsOfDeletedFileNotHavContent.forEach(e -> res.append("\t-" + e.toString() + "\n"));// System.out::println);
			res.append("\n --------- \n");
			/////// new to this method wrt to search()

			allPaths.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res.toString();
	}

	public static void main1(String args[])
	{
		System.out.println(fileContainsString(Paths.get("/home/gunjan/rcloneLogs.txt"), "ajooba", true));
	}

	public static void main0(String[] args)
	{
		System.out.println("Testing");
		// String result = searchAndRandomDelete("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/Test/",
		// "consoleLog", "rror", 0.8);

		String result = searchAndRandomDelete2("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/Test/",
				"consoleLog", Arrays.asList("rror", "xception"), 0.75);

		System.out.println("result= " + result);

		// System.out.println("--------------------");
		// String result2 = search("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/Test/", "consoleLog",
		// "rror");
		// System.out.println("result2= " + result2);
		//
		// for (int i = 0; i < 100; i++)
		// {
		// System.out.println(trueWithProbability(0.33));
		// }
	}

	// /**
	// *
	// * @param rootPathToSearch
	// * @param fileNamePatternToSearch
	// * @param contentToMatch
	// */
	// public static String searchAndRandomDelete(String rootPathToSearch, String fileNamePatternToSearch,
	// String contentToMatch, double probabilityOfDeletion)
	// {
	// StringBuilder res = new StringBuilder();
	// List<Path> deletedFiles = new ArrayList<>();
	//
	// try
	// {
	// Stream<Path> allPaths = Files.walk(Paths.get(rootPathToSearch), FileVisitOption.FOLLOW_LINKS);
	//
	// // find filepaths matching the file name pattern
	// List<Path> pathsOfFoundFiles = allPaths.filter(e -> Files.isRegularFile(e))
	// .filter(e -> e.toString().contains(fileNamePatternToSearch)).peek(System.out::println)
	// .collect(Collectors.toList());
	//
	// List<Path> pathOfResultantFiles = new ArrayList<>();
	//
	// List<Path> pathOfUnreadableFiles = new ArrayList<>();// find files which contains the givent content.
	// for (Path file : pathsOfFoundFiles)
	// {
	// // System.out.println(file.toString());
	// // Files.readAllLines(file).size();
	// if (Files.isReadable(file) == false)
	// {
	// pathOfUnreadableFiles.add(file);
	// continue;
	// }
	//
	// if (Files.readAllLines(file).stream()// .peek(System.out::println)
	// .anyMatch(e -> e.contains(contentToMatch)))
	// // Files.lines(file).anyMatch(line -> line.contains(contentToMatch)))
	// {
	// pathOfResultantFiles.add(file);
	// }
	// }
	//
	// res.append("\n--- Root folder: " + Paths.get(rootPathToSearch).toAbsolutePath().toString());
	// res.append("\n--- fileNamePatternToSearch: " + fileNamePatternToSearch);
	// res.append("\n num of file matching fileNamePatternToSearch = " + pathsOfFoundFiles.size()
	// + " regular files.");
	// res.append("\n file matching fileNamePatternToSearch are:\n");
	// // pathsOfFoundFiles.stream().forEachOrdered(p -> res.append("\n-" + p.toAbsolutePath().toString()));
	// //
	// res.append("\n--- content in file to search: " + contentToMatch);
	// res.append("\n num of Resultant files = " + pathOfResultantFiles.size());
	// res.append("\n num of Unreadable files = " + pathOfUnreadableFiles.size());
	// res.append("\n-------\n--- Resultant files:\n");
	//
	// // pathOfResultantFiles.forEach(e -> res.append("\n").append(e.toString()));// System.out::println);
	//
	// if (pathOfUnreadableFiles.size() > 0)
	// {
	// res.append("\n\n--- Unreadable files:");
	// pathOfUnreadableFiles.forEach(e -> res.append("\n").append(e.toString()));// System.out::println);
	// }
	// // pathsOfFoundFiles.stream().filter(path -> Files.lines(path).anyMatch(line ->
	// // line.contains(contentToMatch)))
	// // .forEach(System.out::println);
	// allPaths.close();
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// return res.toString();
	// }

}
