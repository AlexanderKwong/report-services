package zyj.report.persistence.client;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;


public interface RptExpSubjectMapper {
	public List<Map<String,Object>> findRptExpSubject(Map<String, Object> param);
	
	public List qrySubjectQualityInfo(Map conditions);
	
	public List qryAreaSubjectInfo(Map conditions);
	
	public List qrySchoolSubjectInfo(Map conditions);
	
	public List qryClassSubjectInfo(Map conditions);
	
	public List qryStudentSubjectScore(Map conditions);
	
public List qryStudentSubjectScore1(Map conditions);
	
	public List qryStudentSubjectScore2(Map conditions);
	
	public List qryClassSubjectScore2(Map conditions);
	
	public List qrySchoolSubjectScore2(Map conditions);
	
	public List qryCitySubjectScore2(Map conditions);
	
	public List qryAreaSubjectScore2(Map conditions);
	
	public Map qrySubjectQuality(Map conditions);
	
	public List qrySubjectQualityByRanking(Map conditions);
	
	public List qryClassSubjectInfo2(Map conditions);
	
	public List qrySchoolSubjectInfo2(Map conditions);
	
	public List qryAreaSubjectInfo2(Map conditions);
	
	public List qrySubjectQualityInfo2(Map conditions);
	
public List qrySchPersonNumBySubject(Map conditions);
	
	public List qryAreaPersonNumBySubject(Map conditions);
	
	public List qryCityPersonNumBySubject(Map conditions);
	
//	public List qryClassPersonNumBySubject(Map conditions);

	public List qryScorePersonNumByClassSubject(Map conditions);
	
	public List qryScorePersonNumBySchoolSubject(Map conditions);
	
	public Map qryScorePersonNumByCitySubject(Map conditions);
	
	public List qryScorePersonNumByAreaSubject(Map conditions);
	
public List qryRptExpSchoolSubject(Map conditions);
	
	public List qryRptExpAreaSubject(Map conditions);
	
	public List qryRptCitySubject(Map conditions);
	
	public Map qryCitySubjectContribute(Map conditions);
	
	public List qryRptSubjectSubObJ(Map conditions);
	
	public Map qryZSzfrptABCD(Map conditions);
	
	public List qryZSkmrptABCD(Map conditions);
	
	public Map getScoreLineOfSubjectByRank(Map conditions);

	public List qryZSzfrptABCDRate(Map conditions);

	//查询各科各班的参考缺考应考
	public List<Map<String, Object>> qrySubjectStuNum(Map conditions);
}