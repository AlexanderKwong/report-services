package zyj.report.persistence.client;

import java.util.List;
import java.util.Map;


public interface RptExpAllscoreMapper {

	public List findRptExpAllscore(Map conditions);

	public List qryStudentSubjectAllScore(Map conditions);
	
	public List qryWenKeAllScoreByRanking(Map conditions);

	public List qryLiKeAllScoreByRanking(Map conditions);
	
	public List qryAllScoreByRanking(Map conditions);
	
	
	public List qryAreaAllScoreInfo(Map conditions);

	public List qrySchoolAllScoreInfo(Map conditions);
	
	public List qryClassAllScoreInfo(Map conditions);
	
	public List qryCityAllScoreInfo(Map conditions);
	
	public List qryCityAllScoreInfo2(Map conditions);
	
	public List qrySchoolAllScoreInfo2(Map conditions);
	
	public List qryAreaAllScoreInfo2(Map conditions);
	
	public List qryScorePersonNumBySchoolAllscore(Map conditions);
	
	public List qryScorePersonNumByAreaAllscore(Map conditions);
	
	public Map qryScorePersonNumByCityAllscore(Map conditions);
	
	public List qryScorePersonNumByClassAllscore(Map conditions);
	
	public Map getScoreLineOfAllscoreByRank(Map conditions);

	public List qryAllscoreStuNum(Map conditions);

	/**
	 *
	 * 文理分科学校最高数量
	 *
	 * @param conditions
	 * @return
	 */
	public Float qryStudentSubjectTopScore(Map conditions);

	/**
	 *
	 * 文理分科学校参加人数量
	 *
	 * @param conditions
	 * @return
	 */
	public Integer qryStudentSubjectCountScore(Map conditions);
}