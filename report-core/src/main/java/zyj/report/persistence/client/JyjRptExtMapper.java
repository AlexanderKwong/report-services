package zyj.report.persistence.client;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * @author chengshubiao 2015年10月20日
 * 
 * 教育局联考报表
 */

public interface JyjRptExtMapper {

/*用于缓存基础数据*/
	public List<Map<String,Object>> qrySubjectInfoByExam(@Param("exambatchId") String exambatchId);
	
	public List qryAreaAndSchName(@Param("exambatchId") String exambatchId);
	
	public List qryAreaName(@Param("exambatchId") String exambatchId);

	public List qryClassesInfo(@Param("exambatchId") String exambatchId);
/*用于缓存基础数据*/

	public List qryAllSchoolByCity(Map conditions);
	
	public List qryAllAreaByCity(Map conditions);
	
	
public List<String> qryExamGrade(String id);

	public Map qryExambatch(String id);
	
	public List<Map> qryPaperList(Map param);
	
	public int qryClassesInWenAndLiCnt(Map param);
	
	public int qryClassesNotInWenAndLiCnt(Map param);
	
	public List<Map> qryExamCity(String id);
	
	public List<Map> qryExamArea(Map param);
	
	public List<Map> qryExamSchool(Map param);
	
	public List<Map> qryExamClasses(Map param);
	
	public List<Map> qryExamType(Map param);
	
	//20160312新增 为自动生成 设置页面
	public List<Map<String,Object>> qryExamRecent(Map param);
	
	public List<Map<String,Object>> qryQuestionsByExamAndSubject(Map param);
	
	public List<Map<String,Object>> qryIsChooseQuestionsByExamAndSubject(Map param);
	
	public int insertOneIntoWLQuestion(Map param);
	
	public int  deleteOneFromWLQuestion(Map param);
	
	public List<Map<String,Object>> qryWLQuestion(Map param);
	
	public List<Map<String,Object>> qryStuType(Map param);
	public List<Map<String,Object>> qryStuNum(Map param);
//	public List<Map<String,Object>> qryWLQuestion(Map param);
	
	public List<Map<String,Object>> qryExpProcess(Map param);
	
	public int delExpProcess(Map param);
	
	public int insertExpProcess(Map param);
	
	public int updateExpProcess(Map param);
	
	//20160518 添加 对学生试卷的校验   下面四条 查出来记录不为空或者不等于0就有问题
	public Map checkpaper1(@Param("paperId") String paperId);
	public Map checkpaper2(@Param("paperId") String paperId);
	public Map checkpaper3(@Param("paperId") String paperId);
	public Map checkpaper4(@Param("paperId") String paperId);

	//20160524 增加动态配置科目名
	public  List<Map<String,Object>> updateSubjectName();

}