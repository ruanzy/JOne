package com.rz.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang.time.DateUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.rz.data.db.DB;

public class LogHandle
{
	private static LinkedBlockingQueue<OpLog> queue = new LinkedBlockingQueue<OpLog>(5000);
	private static DB _db;
	private static boolean start = false;

	public static void start(DB db)
	{
		if (!start)
		{
			_db = db;
			new Thread()
			{
				public void run()
				{
					LinkedList<OpLog> list = new LinkedList<OpLog>();
					while (true)
					{
						try
						{
							queue.drainTo(list, 500);
							write(list);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		JobManager.addJob("ClearLog", "ClearLogs", "0 59 23 L * ?", ClearLog.class, new HashMap<String, Object>(), true);
	}

	public static void stop()
	{
		_db = null;
	}

	private static void write(LinkedList<OpLog> logs)
	{
		try
		{
			int rows = 0;
			if (logs.size() > 0)
			{
				_db.begin();
				_db.beginBatch("insert into logs(operator, ip, time, operation, memo) values(?,?,?,?,?)");
				for (OpLog log : logs)
				{
					try
					{
						String operator = log.getOperator();
						String ip = log.getIp();
						String time = log.getTime();
						String operation = log.getOperation();
						String memo = log.getMemo();
						_db.addBatch(new Object[] { operator, ip, time, operation, memo });
						rows++;
						if (0 == rows % 500)
						{
							_db.excuteBatch();
							_db.commit();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				try
				{
					_db.excuteBatch();
					_db.commit();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				_db.endBatch();
				_db.close();
				logs.clear();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void put(OpLog log)
	{
		try
		{
			queue.put(log);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void clear()
	{
		try
		{
			String sql = "delete from logs where time < ?";
			Date date = DateUtils.addDays(new Date(), -30);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			_db.update(sql, sdf.format(date));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
class ClearLog implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		LogHandle.clear();
	}
	
}
