package org.activity.nashorn;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Experiment
{

	public static void main(String[] args)
	{
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		try
		{
			String javaScript = "print('Hello World!');";
			engine.eval(javaScript);
		}
		catch (ScriptException e)
		{
			e.printStackTrace();
		}
	}

}
