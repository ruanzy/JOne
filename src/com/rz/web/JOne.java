package com.rz.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JOne implements Filter
{
	private ServletContext context;
	private Initializer initializer;

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String url = request.getServletPath();
		request.setCharacterEncoding("UTF-8");
		boolean isStatic = url.lastIndexOf(".") != -1;
		if (isStatic)
		{
			chain.doFilter(request, response);
			return;
		}
		try
		{
			ActionContext ac = ActionContext.create(context, request, response);
			new ActionInvocation(ac).invoke();
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
		finally
		{
			ActionContext.destroy();
		}
	}

	public void destroy()
	{
	}

	public void init(FilterConfig cfg) throws ServletException
	{
		String _initializer = cfg.getInitParameter("initializer");
		try
		{
			this.context = cfg.getServletContext();
			if (_initializer != null)
			{
				Class<?> initializercls = Class.forName(_initializer);
				this.initializer = (Initializer) (initializercls.newInstance());
				this.initializer.init(this.context);
			}
			StringBuffer sb = new StringBuffer();
			sb.append("*************************************").append("\r\n");
			sb.append("**                                 **").append("\r\n");
			sb.append("**          JOne Satrting...       **").append("\r\n");
			sb.append("**                                 **").append("\r\n");
			sb.append("*************************************");
			System.out.println(sb);
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}
}