package org.rzy.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JOne implements Filter
{
	private ServletContext context;
	static Logger log = LoggerFactory.getLogger(Filter.class);

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		Context rc = Context.begin(this.context, request, response);
		String url = request.getServletPath();
		try
		{
			long t1 = System.currentTimeMillis();
			boolean page = Pattern.compile("(.jsp|.html|.htm)$").matcher(url).find();
			if (url.indexOf(".") > 0 && !page)
			{
				chain.doFilter(Context.getRequest(), Context.getResponse());
				return;
			}
			// boolean flag =
			// Pattern.compile("(login.*|common/vc|common/login|common/logout)$").matcher(url).find();
			// if (!flag)
			// {
			// Object user = request.getSession().getAttribute("user");
			// if (user == null)
			// {
			// if (Context.isAjax())
			// {
			// response.sendError(1111);
			// }
			// else
			// {
			// response.setCharacterEncoding("UTF-8");
			// String script = "<script>alert('" + XUtil.get("10000")
			// + "');document.location='login.html';</script>";
			// response.getWriter().println(script);
			// }
			// return;
			// }
			// }
			if (url.indexOf(".") > 0)
			{
				chain.doFilter(Context.getRequest(), Context.getResponse());
				return;
			}
			String[] parts = StringUtils.split(url, '/');
			if (parts.length < 1)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			String action_name = StringUtils.capitalize(parts[0]);
			String action_method_name = (parts.length > 1) ? parts[1] : "execute";
			Class<?> cls = Class.forName("action." + action_name);
			Object[] ps = new Object[] { url, action_name, action_method_name };
			log.debug("url={}, action={}, method={}", ps);
			Object result = MethodUtils.invokeMethod(cls.newInstance(), action_method_name, null);
			if (result instanceof Result)
			{
				((Result) result).render();
			}
			long t2 = System.currentTimeMillis();
			log.debug("time=" + (t2 - t1) + "ms");
		}
		catch (Exception e)
		{
			if (e instanceof ClassNotFoundException)
			{
				log.debug(e.getMessage() + " Not Found.");
			}
			else if (e instanceof NoSuchMethodException)
			{
				log.debug(e.getMessage());
			}
			else if (e instanceof InvocationTargetException)
			{
				Throwable t = e.getCause();
				log.debug(t.getMessage());
				t.printStackTrace();
				String xhr = request.getHeader("x-requested-with");
				if (StringUtils.isNotBlank(xhr))
				{
					response.setStatus(9999);
					response.getWriter().print(t.getMessage());
				}
			}
			else
			{
				throw new ServletException(e);
			}
		}
		finally
		{
			if (rc != null)
			{
				rc.end();
			}
		}
	}

	public void destroy()
	{

	}

	public void init(FilterConfig cfg) throws ServletException
	{
		this.context = cfg.getServletContext();
	}
}