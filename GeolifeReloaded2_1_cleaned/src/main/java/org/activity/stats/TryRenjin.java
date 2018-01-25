package org.activity.stats;

import javax.script.ScriptEngine;

import org.renjin.script.RenjinScriptEngineFactory;

// ... add additional imports here ...

public class TryRenjin
{
	public static void main(String[] args) throws Exception
	{
		try
		{
			// create a script engine manager:
			RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
			// create a Renjin engine:
			ScriptEngine engine = factory.getScriptEngine();

			// ... put your Java code here ...
			// engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
			// engine.eval("print(df)");
			// engine.eval("print(lm(y ~ x, df))");

			// String s = ("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10));" + "print(df);" + "print(lm(y ~ x, df))");
			// engine.eval("library(datasets);head(airquality)");
			//
			// String s2 = "library(datasets);" + "library(utils);" + "\n" + "data(mtcars);" + "\n";
			// engine.eval("library(datasets)");
			// System.out.println(engine.eval("head(airquality)"));

			String s3 = "library(datasets);print(head(airquality));"
					+ "print(kruskal.test(Ozone ~ Month, data = airquality))";

			String s4 = "my_data <- PlantGrowth;" + "print(head(my_data));print(levels(my_data$group))";

			engine.eval(s4);

			// StringWriter outputWriter = new StringWriter();
			// engine.getContext().setWriter(outputWriter);
			// engine.eval("print(svmfit)");
			//
			// String output = outputWriter.toString();
			//
			// // Reset output to console
			// engine.getContext().setWriter(new PrintWriter(System.out));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * putInPrint
	 * 
	 * @param s
	 * @return
	 */
	public static String pip(String s)
	{
		return "print(" + s + ")";
	}
}
