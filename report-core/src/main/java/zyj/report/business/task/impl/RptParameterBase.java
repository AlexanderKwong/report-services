package zyj.report.business.task.impl;



import zyj.report.business.task.RptParameter;

import java.util.List;
import java.util.Map;

public class RptParameterBase extends RptParameter {
	
	private int rptType;
	private boolean isWL;
	private String pathFile;
	private String exambatchId;
	private String cityCode;
	private Integer stuType;
	private List<Map<String,Object>> subjectList;
	private Map<String,Object> otherParams;
	

	
	public boolean isWL() {
		return isWL;
	}

	public void setWL(boolean isWL) {
		this.isWL = isWL;
	}

	public String getPathFile() {
		return pathFile;
	}

	public void setPathFile(String pathFile) {
		this.pathFile = pathFile;
	}

	public String getExambatchId() {
		return exambatchId;
	}

	public void setExambatchId(String exambatchId) {
		this.exambatchId = exambatchId;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public Integer getStuType() {
		return stuType;
	}

	public void setStuType(Integer stuType) {
		this.stuType = stuType;
	}

	public List<Map<String,Object>> getSubjectList() {
		return subjectList;
	}

	public void setSubjectList(List<Map<String,Object>> subjectList) {
		this.subjectList = subjectList;
	}

	public Map getOtherParams() {
		return otherParams;
	}

	public void setOtherParams(Map otherParams) {
		this.otherParams = otherParams;
	}

	@Override
	public int getRptType() {
		return rptType;
	}

	public RptParameterBase(int rptType, boolean isWL, String pathFile,
			String exambatchId, String cityCode, Integer stuType,
							List<Map<String,Object>> subjectList, Map otherParams) {
		super();
		this.rptType = rptType;
		this.isWL = isWL;
		this.pathFile = pathFile;
		this.exambatchId = exambatchId;
		this.cityCode = cityCode;
		this.stuType = stuType;
		this.subjectList = subjectList;
		this.otherParams = otherParams;
	}
	public RptParameterBase(RptParameterBase r){
		super();
		this.rptType = r.getRptType();
		this.isWL = r.isWL();
		this.pathFile = r.getPathFile();
		this.exambatchId = r.getExambatchId();
		this.cityCode = r.getCityCode();
		this.stuType = r.getStuType();
		this.subjectList = r.getSubjectList();
		this.otherParams = r.getOtherParams();
	}
	public RptParameterBase() {
		// TODO Auto-generated constructor stub
	}
	
}
