package plugin;

import java.lang.management.RuntimeMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import org.rzy.web.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class JvmMonitor implements Plugin
{
	private static Logger logger = LoggerFactory.getLogger(JvmMonitor.class);
	private ScheduledExecutorService executorService = null;
	private long lastProcessCpuTime = 0;
	private long lastUptime = 0;

	public void record()
	{
		String message = getMemoryUsed() + " " + getCpu() + " " + getThreadCount();
		logger.info(message);
	}

	protected int getThreadCount()
	{
		return ManagementFactory.getThreadMXBean().getThreadCount();
	}

	protected long getMemoryUsed()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024);
	}

	protected double getCpu()
	{
		OperatingSystemMXBean osbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		RuntimeMXBean runbean = java.lang.management.ManagementFactory.getRuntimeMXBean();
		long uptime = runbean.getUptime();
		long processCpuTime = osbean.getProcessCpuTime();
		int processors = osbean.getAvailableProcessors();
		double cpu = (processCpuTime - lastProcessCpuTime) / ((uptime - lastUptime) * 10000f * processors);
		lastProcessCpuTime = processCpuTime;
		lastUptime = uptime;
		return (int) cpu;
	}

	public void init(ServletContext context)
	{
		long periodSeconds = 2;
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
				record();
			}
		}, periodSeconds, periodSeconds, TimeUnit.SECONDS);
	}

	public void destroy()
	{
		executorService.shutdownNow();
	}

}