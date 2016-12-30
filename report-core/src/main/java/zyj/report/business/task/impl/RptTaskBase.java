package zyj.report.business.task.impl;

import org.apache.commons.lang.StringUtils;
import zyj.report.business.Task;
import zyj.report.business.task.RptTask;
import zyj.report.common.ExportUtil;
import zyj.report.common.util.FileUtil;
import zyj.report.exception.report.ReportExportException;


import java.util.HashMap;
import java.util.Map;

public class RptTaskBase extends RptTask {

	private String serviceName;
	private String pathFile;
	private String exambatchId;
	private String cityCode;
	private Integer stuType;
	private String level;
	private Map<String,Object> subject;
	private String scopeId;
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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
		this.setJobId(exambatchId);
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

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public Map<String,Object>  getSubject() {
		return subject;
	}

	public void setSubject(Map<String,Object>  subject) {
		this.subject = subject;
	}

	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}

	@Override
	public void run() throws Exception{
		Map<String,Object> parmter = new HashMap<String,Object>();
		parmter.put("server", serviceName);
		parmter.put("exambatchId", exambatchId);
		parmter.put("cityCode", cityCode);
		parmter.put("pathFile", pathFile + "/");
		parmter.put("stuType", stuType);
		parmter.put("level", level);
//		parmter.put("subject", subject);
		if(this.subject!=null){
			parmter.put("subject", subject.get("SUBJECT"));
			parmter.put("subjectName", subject.get("SUBJECT_NAME"));
			parmter.put("paperId", subject.get("PAPER_ID"));
		}
		if(this.scopeId!=null)
			parmter.put(level+"Id", scopeId);

		if (createDirectoryIfNotExists(getPathFile())){
			ExportUtil.export(parmter);
		}else{
			throw new ReportExportException("目录不存在。");
		}

	}

	public RptTaskBase(String serviceName, String pathFile, String exambatchId,
			String cityCode, Integer stuType, String level,Map<String,Object>  subject) {
		super(exambatchId);
		this.serviceName = serviceName;
		this.pathFile = pathFile;
		this.exambatchId = exambatchId;
		this.cityCode = cityCode;
		this.stuType = stuType;
		this.level = level;
		this.subject = subject;
	}
	public RptTaskBase(RptParameterBase r,String serviceName, String level) {
		this(r, serviceName,  level, null);
	}
	public RptTaskBase(RptParameterBase r,String serviceName, String level,String pathFile) {
		this(r, serviceName,  level, pathFile,null);
	}
	public RptTaskBase(RptParameterBase r,String serviceName, String level,String pathFile,Map<String,Object>  subject) {
		this(r, serviceName, level, pathFile, subject, null);
	}
	public RptTaskBase(RptParameterBase r,String serviceName, String level,String pathFile,Map<String,Object>  subject,String scopeId) {
		super(r.getExambatchId());
		this.serviceName = serviceName;
		this.exambatchId = r.getExambatchId();
		this.pathFile = pathFile;
		this.cityCode = r.getCityCode();
		this.stuType = r.getStuType();
		this.level = level;
		this.subject = subject;
		if(pathFile==null)
			this.pathFile = r.getPathFile();
		if(scopeId!=null)
			this.scopeId = scopeId;
	}
	/**
	 * 如果要创建文件夹不存在，则创建，创建失败返回false，否则返回true
	 * @param path
	 * @return
	 */
	private boolean createDirectoryIfNotExists(String path){
		if (StringUtils.isBlank(path)) return false;

		try {
			FileUtil.mkexpdir(path);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
