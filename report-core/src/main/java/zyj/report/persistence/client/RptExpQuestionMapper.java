package zyj.report.persistence.client;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;


public interface RptExpQuestionMapper {
	public List<Map<String,Object>> findRptExpQuestion(Map<String, Object> param);
	
	public List<Map<String,Object>> findRptExpQuestionItem(Map<String, Object> param);
	
	public List<Map<String,Object>> findRptExpSchoolQuestion(Map<String, Object> param);
	
	public List<Map<String,Object>> findRptExpSchoolQuestionItem(Map<String, Object> param);
	
	public List<Map<String,Object>> findRptExpClassQuestion(Map<String, Object> param);
	
	public List<Map<String,Object>> findRptExpAreaQuestion(Map<String, Object> param);
	
	public List<Map<String,Object>> findAllQuestionOrderItem(Map<String, Object> param);
	
	public List qryClassQuestionScore6(Map conditions);
	
	public List qryStudentQuestionScore(Map conditions);
	
	public List qryQuestionSuitable(Map conditions);
	
	public Map qryTotalWithoutListening(Map conditions);

	public List<Map<String, Object>> qryObjectiveNullRightMissWrong(Map<String, Object> param);
}