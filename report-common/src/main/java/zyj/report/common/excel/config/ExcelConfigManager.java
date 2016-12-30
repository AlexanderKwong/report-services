package zyj.report.common.excel.config;

import java.util.Map;

import org.dom4j.Element;

/**
 * 
 * @author
 *
 */
public interface ExcelConfigManager {
	
	public Element getModelElement(String modelName);
	
	public RuturnConfig getModel(String modelName);
	
	public boolean setConvertMap(String modelName, int column, Map<String, String> m);
	
}
