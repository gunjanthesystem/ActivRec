package org.activity.sanityChecks;

import java.util.concurrent.ConcurrentMap;

import org.activity.ui.PopUps;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class TestMapDB
{
	
	public static void main(String[] args)
	{
		long ct1 = System.currentTimeMillis();
		DB db = DBMaker.memoryDB().make();
		ConcurrentMap tmap1 = db.hashMap("testmap").make();
		
		for (int i = 0; i < 3000000; i++)
		{
			tmap1.put(i, "XiaYuLe");
		}
		
		long ct4 = System.currentTimeMillis();
		PopUps.showMessage("All data creation done in " + ((ct4 - ct1) / 1000) + " seconds since start");
		
		db.close();
	}
	
	// public static String generateTestString(int rounds)
	// {
	// long dt = System.currentTimeMillis();
	// StringBuffer t = new StringBuffer();
	//
	// for (int i = 1; i < rounds; i++)
	// {
	// // for (char c = 'a'; c <= 'z'; c++)
	// // {
	// // t.append(c);
	// // }
	//
	// for (int j = 1; j <= rounds; j++)
	// {
	// t.append("ajooba" + i + "_" + j + ",");
	// }
	// }
	// long lt = System.currentTimeMillis();
	// System.out.println("generateTestString took " + (lt - dt) / 1000 + " secs");
	// return t.toString();
	// }
}
