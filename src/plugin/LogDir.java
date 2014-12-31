package plugin;

import org.rzy.web.Plugin;

public class LogDir implements Plugin
{

	public void init()
	{
		String logDir = "D:/Jonelogs";
		System.setProperty("logDir", logDir);
	}

	public void destroy()
	{
		System.getProperties().remove("logDir");
	}

}
