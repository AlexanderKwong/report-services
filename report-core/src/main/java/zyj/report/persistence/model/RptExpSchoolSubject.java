package zyj.report.persistence.model;

import java.math.BigDecimal;

/**
 * RptExpSchoolSubjectId entity. @author MyEclipse Persistence Tools
 */

public class RptExpSchoolSubject implements java.io.Serializable {

	// Fields

	private String exambatchId;
	private String paperId;
	private String schId;
	private String schName;
	private String subject;
	private Double fullScore;
	private BigDecimal takeExamNum;
	private Double avgScore;
	private Double topScore;
	private Double upScore;
	private String fullRank;
	private BigDecimal levelANum;
	private BigDecimal levelBNum;
	private BigDecimal levelCNum;
	private BigDecimal levelDNum;
	private BigDecimal levelENum;
	private BigDecimal levelGdNum;
	private BigDecimal levelFnNum;
	private BigDecimal levelPsNum;
	private BigDecimal levelFlNum;
	private BigDecimal avgSchOrder;
	private BigDecimal avgBchOrder;
	private String passRate;
	private String bestRate;
	private String avgRate;
	private String allRate;
	private String cityCode;
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RptExpSchoolSubject){
			RptExpSchoolSubject t = (RptExpSchoolSubject) obj;
			return (exambatchId+schName+subject).equals(t.getExambatchId() + t.getSchName() + t.getSubject());
		}
		return super.equals(obj);
	}

	public String toString() {
		return "exambatchId:"+exambatchId+",schName:"+schName+",subject:"+subject+",avgScore:"+avgScore+",avgSchOrder:"+avgSchOrder;
	}

	/** default constructor */
	public RptExpSchoolSubject() {
	}

	/** full constructor */
	public RptExpSchoolSubject(String exambatchId, String paperId,
			String schId, String schName, String subject, Double fullScore,
			BigDecimal takeExamNum, Double avgScore, Double topScore,
			Double upScore, String fullRank, BigDecimal levelANum,
			BigDecimal levelBNum, BigDecimal levelCNum, BigDecimal levelDNum,
			BigDecimal levelENum, BigDecimal levelGdNum, BigDecimal levelFnNum,
			BigDecimal levelPsNum, BigDecimal levelFlNum,
			BigDecimal avgSchOrder, BigDecimal avgBchOrder, String passRate,
			String bestRate, String avgRate, String allRate, String cityCode) {
		this.exambatchId = exambatchId;
		this.paperId = paperId;
		this.schId = schId;
		this.schName = schName;
		this.subject = subject;
		this.fullScore = fullScore;
		this.takeExamNum = takeExamNum;
		this.avgScore = avgScore;
		this.topScore = topScore;
		this.upScore = upScore;
		this.fullRank = fullRank;
		this.levelANum = levelANum;
		this.levelBNum = levelBNum;
		this.levelCNum = levelCNum;
		this.levelDNum = levelDNum;
		this.levelENum = levelENum;
		this.levelGdNum = levelGdNum;
		this.levelFnNum = levelFnNum;
		this.levelPsNum = levelPsNum;
		this.levelFlNum = levelFlNum;
		this.avgSchOrder = avgSchOrder;
		this.avgBchOrder = avgBchOrder;
		this.passRate = passRate;
		this.bestRate = bestRate;
		this.avgRate = avgRate;
		this.allRate = allRate;
		this.cityCode = cityCode;
	}

	// Property accessors

	public String getExambatchId() {
		return this.exambatchId;
	}

	public void setExambatchId(String exambatchId) {
		this.exambatchId = exambatchId;
	}

	public String getPaperId() {
		return this.paperId;
	}

	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}

	public String getSchId() {
		return this.schId;
	}

	public void setSchId(String schId) {
		this.schId = schId;
	}

	public String getSchName() {
		return this.schName;
	}

	public void setSchName(String schName) {
		this.schName = schName;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Double getFullScore() {
		return this.fullScore;
	}

	public void setFullScore(Double fullScore) {
		this.fullScore = fullScore;
	}

	public BigDecimal getTakeExamNum() {
		return this.takeExamNum;
	}

	public void setTakeExamNum(BigDecimal takeExamNum) {
		this.takeExamNum = takeExamNum;
	}

	public Double getAvgScore() {
		return this.avgScore;
	}

	public void setAvgScore(Double avgScore) {
		this.avgScore = avgScore;
	}

	public Double getTopScore() {
		return this.topScore;
	}

	public void setTopScore(Double topScore) {
		this.topScore = topScore;
	}

	public Double getUpScore() {
		return this.upScore;
	}

	public void setUpScore(Double upScore) {
		this.upScore = upScore;
	}

	public String getFullRank() {
		return this.fullRank;
	}

	public void setFullRank(String fullRank) {
		this.fullRank = fullRank;
	}

	public BigDecimal getLevelANum() {
		return this.levelANum;
	}

	public void setLevelANum(BigDecimal levelANum) {
		this.levelANum = levelANum;
	}

	public BigDecimal getLevelBNum() {
		return this.levelBNum;
	}

	public void setLevelBNum(BigDecimal levelBNum) {
		this.levelBNum = levelBNum;
	}

	public BigDecimal getLevelCNum() {
		return this.levelCNum;
	}

	public void setLevelCNum(BigDecimal levelCNum) {
		this.levelCNum = levelCNum;
	}

	public BigDecimal getLevelDNum() {
		return this.levelDNum;
	}

	public void setLevelDNum(BigDecimal levelDNum) {
		this.levelDNum = levelDNum;
	}

	public BigDecimal getLevelENum() {
		return this.levelENum;
	}

	public void setLevelENum(BigDecimal levelENum) {
		this.levelENum = levelENum;
	}

	public BigDecimal getLevelGdNum() {
		return this.levelGdNum;
	}

	public void setLevelGdNum(BigDecimal levelGdNum) {
		this.levelGdNum = levelGdNum;
	}

	public BigDecimal getLevelFnNum() {
		return this.levelFnNum;
	}

	public void setLevelFnNum(BigDecimal levelFnNum) {
		this.levelFnNum = levelFnNum;
	}

	public BigDecimal getLevelPsNum() {
		return this.levelPsNum;
	}

	public void setLevelPsNum(BigDecimal levelPsNum) {
		this.levelPsNum = levelPsNum;
	}

	public BigDecimal getLevelFlNum() {
		return this.levelFlNum;
	}

	public void setLevelFlNum(BigDecimal levelFlNum) {
		this.levelFlNum = levelFlNum;
	}

	public BigDecimal getAvgSchOrder() {
		return this.avgSchOrder;
	}

	public void setAvgSchOrder(BigDecimal avgSchOrder) {
		this.avgSchOrder = avgSchOrder;
	}

	public BigDecimal getAvgBchOrder() {
		return this.avgBchOrder;
	}

	public void setAvgBchOrder(BigDecimal avgBchOrder) {
		this.avgBchOrder = avgBchOrder;
	}

	public String getPassRate() {
		return this.passRate;
	}

	public void setPassRate(String passRate) {
		this.passRate = passRate;
	}

	public String getBestRate() {
		return this.bestRate;
	}

	public void setBestRate(String bestRate) {
		this.bestRate = bestRate;
	}

	public String getAvgRate() {
		return this.avgRate;
	}

	public void setAvgRate(String avgRate) {
		this.avgRate = avgRate;
	}

	public String getAllRate() {
		return this.allRate;
	}

	public void setAllRate(String allRate) {
		this.allRate = allRate;
	}

	public String getCityCode() {
		return this.cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	

}