package jone.monitor;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.DsType.GAUGE;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;

public class Indicators
{
	private ConcurrentMap<String, Gauge> metrics = new ConcurrentHashMap<String, Gauge>();
	private ScheduledExecutorService executor;
	private File dir;
	private int step;
	private boolean started = false;
	static final long START = Util.getTimestamp();
	static final int YEAR = 60 * 60 * 24 * 365;
	static final int DAY = 60 * 60 * 24;
	static final int HOUR = 60 * 60;
	static final int MINUTE = 60;

	public Indicators add(String ds, Gauge g)
	{
		this.metrics.put(ds, g);
		return this;
	}

	public void start(File dir, int step)
	{
		if(started){
			return;
		}
		this.dir = dir;
		this.step = step;
		executor = Executors.newSingleThreadScheduledExecutor();
		for (Map.Entry<String, Gauge> entry : metrics.entrySet())
		{
			String ds = entry.getKey();
			String fileName = ds+ ".rrd";
			createRrd(this.dir, fileName, ds, this.step);
		}
		final File d = this.dir;
		final int s = this.step;
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()
			{
				try
				{
					for (Map.Entry<String, Gauge> entry : metrics.entrySet())
					{
						String ds = entry.getKey();
						String fileName = ds+ ".rrd";
						Double v = entry.getValue().getValue();
						writeRrd(d, fileName, s, ds, v);
					}
				}
				catch (RuntimeException ex)
				{

				}
			}
		}, this.step, this.step, TimeUnit.SECONDS);
	}
	
	private void createRrd(File dir, String fileName, String ds, int step) {
		long START = Util.getTimestamp();
		RrdDb rrdDb = null;
		try {
			File f = new File(dir, fileName);
			if(!f.exists()){
				String rrdPath = f.getPath();
				RrdDef rrdDef = new RrdDef(rrdPath, START - 1, step);
				rrdDef.addDatasource(ds, GAUGE, 2 * step, 0, Double.NaN);
				rrdDef.addArchive(AVERAGE, 0.5, 1, YEAR / step);
				rrdDef.addArchive(AVERAGE, 0.5, DAY / step, YEAR / DAY);
				rrdDef.addArchive(AVERAGE, 0.5, HOUR / step, YEAR / HOUR);
				rrdDef.addArchive(AVERAGE, 0.5, MINUTE / step, YEAR / MINUTE);
				rrdDb = new RrdDb(rrdDef);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rrdDb != null){
				try
				{
					rrdDb.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void writeRrd(File dir, String fileName, int step, String ds, double v) {
		RrdDb rrdDb = null;
		try {
			String rrdPath = new File(dir, fileName).getPath();
			rrdDb = new RrdDb(rrdPath);
			Sample sample = rrdDb.createSample();
			long time = Util.normalize(Util.getTimestamp(), step);
			long lastUpdateTime = rrdDb.getLastUpdateTime();
			if(time > lastUpdateTime){
				sample.setTime(time);
				sample.setValue(ds, v);
				String s = String.format("%s,%s", time, v);
				System.out.println(s);
				sample.update();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rrdDb != null){
				try
				{
					rrdDb.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static double[] readRrd(String fileName, long start, long end) {
		RrdDb rrdDb = null;
		try {
			String rrdPath = new File("D:/metrics", "freeMemory.rrd").getPath();
			rrdDb = new RrdDb(rrdPath);
			FetchRequest request = rrdDb.createFetchRequest(AVERAGE, start, end);
			System.out.println(request.dump());
			FetchData fetchData = request.fetchData();
			return fetchData.getValues(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rrdDb != null){
				try
				{
					rrdDb.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
