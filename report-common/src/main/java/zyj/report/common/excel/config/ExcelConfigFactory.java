package zyj.report.common.excel.config;

import java.util.HashMap;
import java.util.Map;

import zyj.report.common.excel.config.impl.ExcelConfigManagerImpl;

/**
 * 
 * @author
 * 
 */
public class ExcelConfigFactory {
	
	private static Map<String, ExcelConfigManager> config = new HashMap<String, ExcelConfigManager>();

	public static ExcelConfigManager getExcelConfigManger(String fileconfig) {
		if (fileconfig == null) {
			fileconfig = "ExcelModeMapping.xml";
		}
		ExcelConfigManager cm = config.get(fileconfig);
		if (cm == null) {
			cm = new ExcelConfigManagerImpl(fileconfig);
			config.put(fileconfig, cm);
		}
		return cm;
	}
}
