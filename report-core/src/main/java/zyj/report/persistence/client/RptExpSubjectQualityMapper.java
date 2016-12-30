package zyj.report.persistence.client;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;


public interface RptExpSubjectQualityMapper {
	public List<Map<String,Object>> findRptExpSubjectQuality(Map<String, Object> paramter);
}