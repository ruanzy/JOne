package jone.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jone.R;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class WebUtil
{
	static Logger log = LoggerFactory.getLogger(WebUtil.class);

	private WebUtil()
	{
	}

	public static class Request
	{
		public static HttpServletRequest get()
		{
			return ActionContext.getRequest();
		}

		public static void setCharacterEncoding(String encoding)
		{
			try
			{
				get().setCharacterEncoding(encoding);
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}

		public static void attr(String key, Object value)
		{
			get().setAttribute(key, value);
		}

		public static Object attr(String key)
		{
			return get().getAttribute(key);
		}
	}

	public static class Response
	{
		public static HttpServletResponse get()
		{
			return ActionContext.getResponse();
		}

		public static void setContentType(String type)
		{
			get().setContentType(type);
		}

		public static void setCharacterEncoding(String encoding)
		{
			get().setCharacterEncoding(encoding);
		}

		public static OutputStream getOutputStream()
		{
			try
			{
				return get().getOutputStream();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}

		public static PrintWriter getWriter()
		{
			try
			{
				return get().getWriter();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}

		public static void write(String txt)
		{
			getWriter().print(txt);
		}
	}

	public static class Cookies
	{
		public static void add(Cookie cookie)
		{
			HttpServletResponse response = WebUtil.Response.get();
			response.addCookie(cookie);
		}

		public static String get(String cookieName)
		{
			String cookieValue = null;
			Cookie[] cks = WebUtil.Request.get().getCookies();
			if (cks != null)
			{
				for (Cookie cookie : cks)
				{
					if (cookieName.equals(cookie.getName()))
					{
						cookieValue = cookie.getValue();
						break;
					}
				}
			}
			return cookieValue;
		}

		public static List<Cookie> getAll()
		{
			List<Cookie> all = null;
			Cookie[] cks = WebUtil.Request.get().getCookies();
			if (cks != null)
			{
				all = Arrays.asList(cks);
			}
			return all;
		}

		public static void clear(String cookieName)
		{
			Cookie[] cks = WebUtil.Request.get().getCookies();
			if (cks != null)
			{
				for (Cookie cookie : cks)
				{
					if (cookieName.equals(cookie.getName()))
					{
						cookie.setPath("/");
						cookie.setMaxAge(0);
						WebUtil.Response.get().addCookie(cookie);
						break;
					}
				}
			}
		}
	}

	public static class Session
	{
		public static HttpSession get()
		{
			return get(false);
		}

		public static HttpSession get(boolean create)
		{
			return WebUtil.Request.get().getSession(create);
		}

		public static void clear()
		{
			get().invalidate();
		}

		public static void attr(String key, Object value)
		{
			get().setAttribute(key, value);
		}

		public static Object attr(String key)
		{
			return get().getAttribute(key);
		}
	}

	public static class Application
	{
		public static ServletContext get()
		{
			return ActionContext.getServletContext();
		}

		public static void attr(String key, Object value)
		{
			get().setAttribute(key, value);
		}

		public static Object attr(String key)
		{
			return get().getAttribute(key);
		}

		public static void setUserres(String user)
		{
			attr("USERRES", user);
		}

		public static String getUserres()
		{
			Object user = attr("USERRES");
			return (String) user;
		}
	}

	public static String getWebRoot()
	{
		return WebUtil.Application.get().getRealPath("/");
	}

	public static String getMethod()
	{
		return WebUtil.Request.get().getMethod();
	}

	public static Map<String, String> getParameters()
	{
		Map<String, String> ps = new HashMap<String, String>();
		Enumeration<?> em = WebUtil.Request.get().getParameterNames();
		if (em.hasMoreElements())
		{
			while (em.hasMoreElements())
			{
				String k = (String) em.nextElement();
				String v = getParameter(k);
				ps.put(k, v);
			}
		}
		return ps;
	}

	public static R params()
	{
		R ps = new R();
		Enumeration<?> em = WebUtil.Request.get().getParameterNames();
		if (em.hasMoreElements())
		{
			while (em.hasMoreElements())
			{
				String k = (String) em.nextElement();
				String v = getParameter(k);
				ps.put(k, v);
			}
		}
		return ps;
	}

	private static String getRequestPayload()
	{
		StringBuilder sb = new StringBuilder();
		try
		{

			BufferedReader br = new BufferedReader(new InputStreamReader(
					WebUtil.Request.get().getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static Map<String, Object> getRequesBody()
	{
		String json = getRequestPayload();
		Map<String, Object> o = JSON.parseObject(json);
		return o;
	}
	
	public static <T> T getRequesBody(Class<T> cls)
	{
		String json = getRequestPayload();
		T t = JSON.parseObject(json, cls);
		return t;
	}

	public static String getParameter(String name)
	{
		return WebUtil.Request.get().getParameter(name);
	}
	
	public static String param(String name)
	{
		return WebUtil.Request.get().getParameter(name);
	}

	public static String getIP()
	{
		return WebUtil.Request.get().getRemoteAddr();
	}

	public static String getHeader(String key)
	{
		return WebUtil.Request.get().getHeader(key);
	}

	public static void setHeader(String key, String value)
	{
		WebUtil.Response.get().addHeader(key, value);
	}

	public static boolean isAjax()
	{
		String xhr = getHeader("X-Requested-With");
		return (xhr != null) && ("XMLHttpRequest".equals(xhr));
	}

	public static boolean isAdmin(String username, String password)
	{
		return ("admin").equals(username) && ("162534").equals(password);
	}

	public static void forward(String url)
	{
		RequestDispatcher rd = WebUtil.Request.get().getRequestDispatcher(url);
		try
		{
			rd.forward(WebUtil.Request.get(), WebUtil.Response.get());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void redirect(String url)
	{
		try
		{
			WebUtil.Response.get().sendRedirect(url);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void setUserinfo(String userinfo)
	{
		WebUtil.Session.attr("RZY_USER", userinfo.split("_")[0]);
		WebUtil.Session.attr("user", userinfo.split("_")[0]);
		// String domain = WebUtil.Request.get().getServerName();
		Cookie token = new Cookie("SSOTOKEN", userinfo);
		token.setMaxAge(30 * 60);
		token.setPath("/");
		WebUtil.Cookies.add(token);
	}

	public static String getUserinfo()
	{
		Object userinfo = (String) WebUtil.Session.attr("RZY_USER");
		if (userinfo != null)
		{
			return (String) userinfo;
		}
		return null;
	}

	// public static String getUser()
	// {
	// String ssotoken = null;
	// Cookie[] cks = WebUtil.Request.get().getCookies();
	// if (cks != null)
	// {
	// for (Cookie cookie : cks)
	// {
	// if ("SSOTOKEN".equals(cookie.getName()))
	// {
	// ssotoken = cookie.getValue();
	// break;
	// }
	// }
	// }
	// if (ssotoken != null)
	// {
	// String[] arr = ssotoken.split("_");
	// return arr[0];
	// }
	// return null;
	// }
	private static String getToken()
	{
		String token = WebUtil.Request.get().getParameter("token");
		if (null == token)
		{
			token = getHeader("Authorization");
		}
		return token;
	}

	public static String getUser()
	{
		String token = getToken();
		return TokenUtil.getUser(token);
	}
	
	public static List<FileItem> getFiles()
	{
		return UploadHelper.getFiles();
	}
	
	public static void download(File file)
	{
		String fileName = FilenameUtils.getName(file.getPath());
		WebUtil.Response.get().setHeader("Content-type", "text/plain;charset=UTF-8");
		WebUtil.Response.get().setHeader("Content-Disposition", "attachment; filename=" + fileName);
		WebUtil.Response.get().setCharacterEncoding("UTF-8");
		OutputStream pw = null;
		try
		{
			pw = WebUtil.Response.get().getOutputStream();
			IOUtils.copy(new FileInputStream(file), pw);
		}
		catch (Exception e)
		{

		}
		finally
		{
			try
			{
				pw.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static String getVersion()
	{
		return JOne.class.getPackage().getImplementationVersion();
	}
}