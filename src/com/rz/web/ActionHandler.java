package com.rz.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionHandler
{
	private static Logger log = LoggerFactory.getLogger(ActionHandler.class);
	private ServletContext servletContext;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private final static ThreadLocal<ActionHandler> actionHandler = new ThreadLocal<ActionHandler>();

	// private Interceptor[] interceptors = null;

	private ActionHandler()
	{
	}

	static ActionHandler create(ServletContext servletContext, HttpServletRequest req, HttpServletResponse res)
	{
		ActionHandler ac = new ActionHandler();
		ac.servletContext = servletContext;
		ac.request = req;
		ac.response = res;
		actionHandler.set(ac);
		return ac;
	}

	void handle() throws IOException, ServletException
	{
		String url = request.getServletPath();
		String[] parts = url.substring(1).split("/");
		String _action = parts[0];
		String action = Character.toTitleCase(_action.charAt(0)) + _action.substring(1);
		String actionMethod = (parts.length > 1) ? parts[1] : "execute";
		String ip = request.getRemoteAddr();
		String m = request.getMethod();
		Object[] ps = new Object[] { ip, m, url };
		log.debug("{} {} {}", ps);
		try
		{
			Class<?> cls = Class.forName("action." + action);
			Method method = cls.getMethod(actionMethod);
			Object result = method.invoke(cls.newInstance());
			if (result instanceof View)
			{
				((View) result).render();
			}
		}
		catch (Exception e)
		{
			if (e instanceof ClassNotFoundException || e instanceof NoSuchMethodException)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			else if (e instanceof InvocationTargetException)
			{
				Throwable t = e.getCause();
				t.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
				return;
			}
			else
			{
				throw new ServletException(e);
			}
		}
		finally
		{
			actionHandler.remove();
		}
	}

	public static ServletContext getServletContext()
	{
		return actionHandler.get().servletContext;
	}

	public static HttpServletRequest getRequest()
	{
		return actionHandler.get().request;
	}

	public static HttpServletResponse getResponse()
	{
		return actionHandler.get().response;
	}

	public static HttpSession getSession(boolean create)
	{
		return getRequest().getSession(create);
	}
}
