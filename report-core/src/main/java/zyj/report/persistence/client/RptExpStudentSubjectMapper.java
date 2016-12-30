package zyj.report.persistence.client;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;


public interface RptExpStudentSubjectMapper {
	public List<Map<String,Object>> findRptExpStudentSubject(Map<String, Object> param);
}