package zyj.report.common.excel.entity;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author
 * 
 */
public class RuturnPropertyParam {
	/***
	 * property name in java bean
	 */
	private String name = null;
	
	private String nameForMap = null;

	private String column = null;

	private String excelTitleName = null;

	private String dataType = null;
	
	private String maxLength = null;

	private String dateFormat = null;

	private String defaultValue = null;
	
	private boolean isConvertable = false;
	
	private Map<String, String> convertMap = null;
	
	private Map<String, String> convertMap2 = null;
	
	private List<String> converValueList = null;
	
	private boolean notNull = false;
	
	private int viewLength;
	
	private String dictypecode;
	
	private String diccode;

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getExcelTitleName() {
		return excelTitleName;
	}

	public void setExcelTitleName(String excelTitleName) {
		this.excelTitleName = excelTitleName;
	}

	public String getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isConvertable() {
		return isConvertable;
	}

	public void setConvertable(boolean isConvertable) {
		this.isConvertable = isConvertable;
	}

	public Map<String, String> getConvertMap() {
		return convertMap;
	}

	public void setConvertMap(Map<String, String> convertMap) {
		this.convertMap = convertMap;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	public int getViewLength() {
		return viewLength;
	}

	public void setViewLength(int viewLength) {
		this.viewLength = viewLength;
	}

	public String getDictypecode() {
		return dictypecode;
	}

	public void setDictypecode(String dictypecode) {
		this.dictypecode = dictypecode;
	}

	public String getDiccode() {
		return diccode;
	}

	public void setDiccode(String diccode) {
		this.diccode = diccode;
	}

	public Map<String, String> getConvertMap2() {
		return convertMap2;
	}

	public void setConvertMap2(Map<String, String> convertMap2) {
		this.convertMap2 = convertMap2;
	}

	public List<String> getConverValueList() {
		return converValueList;
	}

	public void setConverValueList(List<String> converValueList) {
		this.converValueList = converValueList;
	}

	public String getNameForMap() {
		return nameForMap;
	}

	public void setNameForMap(String nameForMap) {
		this.nameForMap = nameForMap;
	}

}
