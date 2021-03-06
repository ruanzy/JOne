package jone.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil
{

//	public static Map<String, String> getMethodVariable(String classname, String methodname)
//	{
//		Map<String, String> map = new HashMap<String, String>();
//		try
//		{
//			ClassPool pool = ClassPool.getDefault();
//			CtClass cc = pool.get(classname);
//			CtMethod cm = cc.getDeclaredMethod(methodname);
//			CtClass[] pt = cm.getParameterTypes();
//			MethodInfo methodInfo = cm.getMethodInfo();
//			CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
//			String[] paramNames = new String[cm.getParameterTypes().length];
//			LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
//					.getAttribute(LocalVariableAttribute.tag);
//			if (attr != null)
//			{
//				int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
//				for (int i = 0; i < paramNames.length; i++)
//				{
//					map.put(attr.variableName(i + pos), pt[i].getName());
//				}
//				return map;
//			}
//		}
//		catch (Exception e)
//		{
//
//		}
//		return null;
//	}
	
	public static Set<Class<?>> scan(String packageName) {
		Set<Class<?>> classSet = new HashSet<Class<?>>();
		Set<String> classFileSet = findFiles(packageName, "*.class");
		for (String classFile : classFileSet) {
			Class<?> classInFile = null;
			try {
				// classInFile = Class.forName(classFile);
				classInFile = Thread.currentThread()
						.getContextClassLoader().loadClass(classFile);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			classSet.add(classInFile);
		}
		return classSet;
	}

	/**
	 * 递归查找文件
	 * 
	 * @param baseDirName
	 *            查找的文件夹路径
	 * @param targetFileName
	 *            需要查找的文件名
	 */
	protected static Set<String> findFiles(String baseDirName, String targetFileName) {
		/**
		 * 算法简述： 从某个给定的需查找的文件夹出发，搜索该文件夹的所有子文件夹及文件，
		 * 若为文件，则进行匹配，匹配成功则加入结果集，若为子文件夹，则进队列。 队列不空，重复上述操作，队列为空，程序结束，返回结果。
		 */
		Set<String> classFiles = new HashSet<String>();
		// 判断class路径
		Enumeration<URL> baseURLs = null;
		try {
			baseURLs = ClassUtil.class.getClassLoader().getResources(
					baseDirName.replaceAll("\\.", "/"));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		URL baseURL = null;
		while (baseURLs.hasMoreElements()) {
			baseURL = baseURLs.nextElement();
			if (baseURL != null) {
				// 得到协议的名称
				String protocol = baseURL.getProtocol();
				String basePath = baseURL.getFile();

				// 如果是以文件的形式保存在服务器上
				if ("jar".equals(protocol)) {
					String[] paths = basePath.split("!/");
					// 获取jar
					try {
						classFiles.addAll(findJarFile(URLDecoder.decode(
								paths[0].replace("file:", ""), "UTF-8"),
								paths[1]));
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				} else {
					classFiles
							.addAll(findPackageFiles(basePath, targetFileName));
				}
			}
		}
		return classFiles;
	}

	/**
	 * 查找根目录下的文件
	 * 
	 * @param baseDirName
	 *            路径
	 * @param targetFileName
	 *            文件匹配
	 * @return Set
	 */
	private static Set<String> findPackageFiles(String baseDirName,
			String targetFileName) {
		Set<String> classFiles = new HashSet<String>();
		String tempName = null;
		// 判断目录是否存在
		File baseDir = null;
		try {
			baseDir = new File(URLDecoder.decode(baseDirName, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		if (!baseDir.exists() || !baseDir.isDirectory()) {
			throw new RuntimeException("Search error : " + baseDirName
					+ " is not a dir.");
		} else {
			String[] filelist = baseDir.list();
			String classname = null;
			String tem = null;
			for (String aFilelist : filelist) {
				File readfile = null;
				try {
					readfile = new File(URLDecoder.decode(baseDirName
							+ File.separator + aFilelist, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				if (readfile.isDirectory()) {
					classFiles.addAll(findPackageFiles(baseDirName
							+ File.separator + aFilelist, targetFileName));
				} else {
					tempName = readfile.getName();
					if (wildcardMatch(targetFileName, tempName)) {
						tem = readfile.getAbsoluteFile().toString()
								.replaceAll("\\\\", "/");
						classname = tem.substring(tem.indexOf("classes/")
								+ "classes/".length(), tem.indexOf(".class"));
						classFiles.add(classname.replaceAll("/", "."));
					}
				}
			}
		}
		return classFiles;
	}

	/**
	 * 通配符匹配
	 * 
	 * @param pattern
	 *            通配符模式
	 * @param str
	 *            
	 *            
	 *            匹配成功则返回true，否则返回false
	 */
	private static boolean wildcardMatch(String pattern, String str) {
		int patternLength = pattern.length();
		int strLength = str.length();
		int strIndex = 0;
		char ch;
		for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
			ch = pattern.charAt(patternIndex);
			if (ch == '*') {
				// 通配符星号*表示可以匹配任意多个字符
				while (strIndex < strLength) {
					if (wildcardMatch(pattern.substring(patternIndex + 1),
							str.substring(strIndex))) {
						return true;
					}
					strIndex++;
				}
			} else if (ch == '?') {
				// 通配符问号?表示匹配任意一个字符
				strIndex++;
				if (strIndex > strLength) {
					// 表示str中已经没有字符匹配?了。
					return false;
				}
			} else {
				if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
					return false;
				}
				strIndex++;
			}
		}
		return strIndex == strLength;
	}

	private static Set<String> findJarFile(String filePath, String packageName)
			throws IOException {
		JarFile localJarFile = new JarFile(new File(filePath));
		Set<String> classFiles = findInJar(localJarFile, packageName);
		localJarFile.close();
		return classFiles;
	}

	private static Set<String> findInJar(JarFile localJarFile, String packageName) {
		Set<String> classFiles = new HashSet<String>();
		Enumeration<JarEntry> entries = localJarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = entries.nextElement();
			String entryName = jarEntry.getName();
			if (!jarEntry.isDirectory()
					&& (packageName == null || entryName
							.startsWith(packageName))
					&& entryName.endsWith(".class")) {
				String className = entryName.replaceAll("/", ".").substring(0,
						entryName.length() - 6);
				classFiles.add(className);
			}
		}
		return classFiles;
	}

	/**
	 * 查找jar包中的class
	 * 
	 * @param baseDirName
	 *            jar路径
	 * @param includeJars
	 *            jar文件地址
	 */
	// private Set<String> findjarFiles(String baseDirName,
	// final Set<String> includeJars, String packageName) {
	// Set<String> classFiles = new HashSet<String>();
	// try {
	// // 判断目录是否存在
	// File baseDir = new File(URLDecoder.decode(baseDirName, "UTF-8"));
	// if (!baseDir.exists() || !baseDir.isDirectory()) {
	// throw new RuntimeException("Jar file scan error : "
	// + baseDirName + " is not a dir.");
	// } else {
	// String[] filelist = baseDir.list(new FilenameFilter() {
	//
	// public boolean accept(File dir, String name) {
	// return includeJars.contains(name);
	// }
	// });
	// for (String aFilelist : filelist) {
	// classFiles.addAll(findJarFile(baseDirName + File.separator
	// + aFilelist, packageName));
	// }
	// }
	//
	// } catch (IOException e) {
	// throw new RuntimeException(e.getMessage(), e);
	// }
	// return classFiles;
	//
	// }
}
