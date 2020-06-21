package org.activity.distances;

/**
 * see:https://rosettacode.org/wiki/Jaro_distance#Java
 * 
 * @param args
 */
public class JaroWinklerDistance implements StringDistI
{
	public static final JaroWinklerDistance STATIC = new JaroWinklerDistance();

	public double getDistance(String s, String t)
	{
		return 1 - getSimilarity(s, t);
	}

	public double getSimilarity(String s, String t)
	{
		int s_len = s.length();
		int t_len = t.length();

		if (s_len == 0 && t_len == 0) return 1;

		int match_distance = Integer.max(s_len, t_len) / 2 - 1;

		boolean[] s_matches = new boolean[s_len];
		boolean[] t_matches = new boolean[t_len];

		int matches = 0;
		int transpositions = 0;

		for (int i = 0; i < s_len; i++)
		{
			int start = Integer.max(0, i - match_distance);
			int end = Integer.min(i + match_distance + 1, t_len);

			for (int j = start; j < end; j++)
			{
				if (t_matches[j]) continue;
				if (s.charAt(i) != t.charAt(j)) continue;
				s_matches[i] = true;
				t_matches[j] = true;
				matches++;
				break;
			}
		}

		if (matches == 0) return 0;

		int k = 0;
		for (int i = 0; i < s_len; i++)
		{
			if (!s_matches[i]) continue;
			while (!t_matches[k])
				k++;
			if (s.charAt(i) != t.charAt(k)) transpositions++;
			k++;
		}

		return (((double) matches / s_len) + ((double) matches / t_len)
				+ (((double) matches - transpositions / 2.0) / matches)) / 3.0;
	}

	public static void main(String[] args)
	{
		JaroWinklerDistance d = new JaroWinklerDistance();
		org.apache.commons.text.similarity.JaroWinklerDistance jwApache = new org.apache.commons.text.similarity.JaroWinklerDistance();

		System.out.println(jwApache.apply("1", "2") + "\n\n");// ("word1", "word1"));

		System.out.println(d.getDistance("word1", "word1"));
		// System.out.println(1 - jwApache.apply("word1", "word1"));

		System.out.println(d.getDistance("word1", "word2"));
		// System.out.println(1 - jwApache.apply("word1", "word2"));

		System.out.println(d.getDistance("MARTHA", "MARHTA"));
		// System.out.println(1 - jwApache.apply("MARTHA", "MARHTA"));

		System.out.println(d.getDistance("DIXON", "DICKSONX"));
		// System.out.println(1 - jwApache.apply("DIXON", "DICKSONX"));

		System.out.println(d.getDistance("JELLYFISH", "SMELLYFISH"));
		// System.out.println(1 - jwApache.apply("JELLYFISH", "SMELLYFISH"));

	}
}
