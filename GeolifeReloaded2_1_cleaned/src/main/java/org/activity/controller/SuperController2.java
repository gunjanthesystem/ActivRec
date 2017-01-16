package org.activity.controller;

import org.activity.util.Constant;

public class SuperController2
{

	public static void main(String[] args)
	{
		// Constant.DATABASE_NAME = "dcu_data_2";// "geolife1";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/SimpleV3/";
		// new ControllerWithoutServer();
		//
		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = "/home/gunjan/Geolife/SimpleV3/";//
		// /run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/Geolife/SimpleV3/";
		// new ControllerWithoutServer();

		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/Geolife/CaseBased/Sum/";
		// Constant.rankScoring = "sum";// product"; // "sum"
		// new ControllerWithoutServer();
		//
		// Constant.DATABASE_NAME = "dcu_data_2";// "geolife1";//
		// Constant.caseType = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/CaseBased/Sum/";
		// Constant.rankScoring = "sum";// product"; // "sum"
		// new ControllerWithoutServer();

		// String[] arrBeta = { "0.25", "0.5", "0.75", "0", "1" };
		//
		// // HALTED AT PROD0 FOR GEO DATA
		// for (int i = 0; i < arrBeta.length; i++)
		// {
		// Constant.ALPHA = Double.parseDouble(arrBeta[i]);
		//
		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = "/home/gunjan/Geolife/Sum" + arrBeta[i] + "/";
		// // Constant.outputCoreResultsPath =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/Geolife/CaseBased/Sum" + arrBeta[i] + "/";
		//
		// Constant.rankScoring = "sum"; // "sum"
		// new ControllerWithoutServer();
		//
		// // Constant.DATABASE_NAME = "dcu_data_2";// "geolife1";//
		// // Constant.caseType = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3/home/gunjan/Geolife/DCU/
		// // Constant.outputCoreResultsPath = "/home/gunjan/Geolife/DCU/Sum" + arrBeta[i] + "/";
		// // // Constant.outputCoreResultsPath =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/CaseBased/Sum" + arrBeta[i] + "/";
		// // Constant.rankScoring = "sum"; // "sum"
		// // new ControllerWithoutServer();
		//
		// }

		/** For N Gram Analysis **/
		Constant.DATABASE_NAME = "dcu_data_2";//
		Constant.caseType = "none";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		Constant.rankScoring = "none";
		Constant.outputCoreResultsPath = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data Works/stats/NGramAnalysis/";// /run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/Geolife/SimpleV3/";
		new ControllerWithoutServer();
	}
}
