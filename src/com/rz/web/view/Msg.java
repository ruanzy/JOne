package com.rz.web.view;

import java.io.IOException;
import com.alibaba.fastjson.JSON;
import com.rz.web.ActionHandler;
import com.rz.web.View;

public class Msg implements View
{
	boolean result = true;

	String msg;

	public boolean getResult()
	{
		return result;
	}

	public void setResult(boolean result)
	{
		this.result = result;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public Msg(String msg)
	{
		this.msg = msg;
	}

	public Msg(boolean result, String msg)
	{
		this.result = result;
		this.msg = msg;
	}

	public void render()
	{
		ActionHandler.getResponse().setContentType("text/javascript;charset=UTF-8");
		try
		{
			ActionHandler.getResponse().getWriter().print(JSON.toJSONString(this));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
