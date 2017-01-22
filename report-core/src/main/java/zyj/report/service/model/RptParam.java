package zyj.report.service.model;

import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/18.
 */
public class RptParam {

	private String level;
	private String examBatchId;
	private String cityCode;
	private String path;
	private String areaId;
	private String schoolId;
	private String classesId;
	private Integer stuType;
	private String subject;
	private String subjectName;
	private String paperId;
	private Integer type;

	public RptParam() {

	}

	public void initParam(Map<String, Object> objectMap) {
		level = (String) objectMap.get("level");
		examBatchId = (String) objectMap.get("exambatchId");
		cityCode = (String) objectMap.get("cityCode");
		path = (String) objectMap.get("pathFile");
		stuType = (Integer) objectMap.get("stuType");
		areaId = (String) objectMap.get("areaId");
		schoolId = (String) objectMap.get("schoolId");
		classesId = (String) objectMap.get("classesId");
		subject = (String) objectMap.get("subject");
		subjectName = (String) objectMap.get("subjectName");
		paperId = (String) objectMap.get("paperId");
		type = (Integer) objectMap.get("type");
	}

	/**
	 * 导出级别: city,area,school,classes
	 *
	 * @return
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * 考试科目ID
	 *
	 * @return
	 */
	public String getExamBatchId() {
		return examBatchId;
	}

	/**
	 * 城市ID
	 *
	 * @return
	 */
	public String getCityCode() {
		return cityCode;
	}

	/**
	 * excel 地址
	 *
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 区县ID
	 *
	 * @return
	 */
	public String getAreaId() {
		return areaId;
	}

	/**
	 * 学校ID
	 *
	 * @return
	 */
	public String getSchoolId() {
		return schoolId;
	}

	/**
	 * 班级ID
	 *
	 * @return
	 */
	public String getClassesId() {
		return classesId;
	}

	/**
	 * 学生类型 用于过滤应往届
	 *
	 * @return
	 */
	public Integer getStuType() {
		return stuType;
	}


	/**
	 * 科目简称: 如 WL : 物理
	 *
	 * @return
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * 科目中文名字
	 *
	 * @return
	 */
	public String getSubjectName() {
		return subjectName;
	}

	/**
	 * 考试科目ID
	 *
	 * @return
	 */
	public String getPaperId() {
		return paperId;
	}

	/**
	 * 文理类别: 0 不分文理，1 文 2 理科
	 *
	 * @return
	 */
	public Integer getType() {
		return type;
	}
}
