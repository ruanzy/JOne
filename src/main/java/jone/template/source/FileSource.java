package jone.template.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import jone.template.EngineConfig;

/**
 * FileSource 用于从普通文件中加载模板内容
 */
public class FileSource implements ISource {
	
	private String finalFileName;
	private String fileName;
	private String encoding;
	
	private long lastModified;
	
	public FileSource(String baseTemplatePath, String fileName, String encoding) {
		this.finalFileName = buildFinalFileName(baseTemplatePath, fileName);
		this.fileName = fileName;
		this.encoding= encoding;
	}
	
	public FileSource(String baseTemplatePath, String fileName) {
		this(baseTemplatePath, fileName, EngineConfig.DEFAULT_ENCODING);
	}
	
	public boolean isModified() {
		return lastModified != new File(finalFileName).lastModified();
	}
	
	public String getKey() {
		return fileName;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public String getFinalFileName() {
		return finalFileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public StringBuilder getContent() {
		File file = new File(finalFileName);
		if (!file.exists()) {
			throw new RuntimeException("File not found : " + finalFileName);
		}
		
		// 极为重要，否则在开发模式下 isModified() 一直返回 true，缓存一直失效（原因是 lastModified 默认值为 0）
		this.lastModified = file.lastModified();
		
		return loadFile(file, encoding);
	}
	
	private String buildFinalFileName(String baseTemplatePath, String fileName) {
		char firstChar = fileName.charAt(0);
		String finalFileName;
		if (firstChar == '/' || firstChar == '\\') {
			finalFileName = baseTemplatePath + fileName;
		} else {
			finalFileName = baseTemplatePath + File.separator + fileName;
		}
		return finalFileName;
	}
	
	public static StringBuilder loadFile(File file, String encoding) {
		StringBuilder ret = new StringBuilder((int)file.length() + 3);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
			// br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			if (line != null) {
				ret.append(line);
			} else {
				return ret;
			}
			
			while ((line=br.readLine()) != null) {
				ret.append('\n').append(line);
			}
			return ret;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// com.jfinal.kit.LogKit.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("File name: ").append(fileName).append("\n");
		sb.append("Final file name: ").append(finalFileName).append("\n");
		sb.append("Last modified: ").append(lastModified).append("\n");
		return sb.toString();
	}
}




