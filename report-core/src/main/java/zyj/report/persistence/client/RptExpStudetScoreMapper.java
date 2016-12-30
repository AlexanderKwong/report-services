package zyj.report.persistence.client;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;


public interface RptExpStudetScoreMapper {
	public List<Map<String,Object>> findRptExpStudetAllScore(Map<String, Object> param);
	public List<Map<String,Object>> findAllSchoolForRptExpStudetAllScore(Map<String, Object> param);
	public List<Map<String,Object>> findRptExpStudetScore(Map<String, Object> param);
	public List<Map<String,Object>> findAllSchoolForRptExpStudetScore(Map<String, Object> param);
	public List<Map<String,Object>> findAllAreaForRptExpStudetScore(Map<String, Object> param);
	public List<Map<String,Object>> findAllAreaForRptExpStudetAllScore(Map<String, Object> param);
	
}