package zyj.report.common.excel.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import zyj.report.common.excel.event.Event;

/**
 * 
 * @author
 * 
 */
public class RuturnConfig {

	private String className = null;

	private Set<Integer> enableColumn;
	
	private Event event;

	private Map propertyMap = new HashMap();
	
	private String mapModel;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map getPropertyMap() {
		return propertyMap;
	}

	public void setPropertyMap(Map propertyMap) {
		this.propertyMap = propertyMap;
	}

	public Set<Integer> getEnableColumn() {
		return enableColumn;
	}

	public void setEnableColumn(Set<Integer> enableColumn) {
		this.enableColumn = enableColumn;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String getMapModel() {
		return mapModel;
	}

	public void setMapModel(String mapModel) {
		this.mapModel = mapModel;
	}

}
