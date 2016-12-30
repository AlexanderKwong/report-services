package zyj.report.persistence.client;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import zyj.report.persistence.model.RptExpSchoolSubject;


public interface RptExpSchoolSubjectMapper {
	public List<RptExpSchoolSubject> findRptExpSchoolSubject(@Param("exambatchId") String exambatchId, @Param("cityCode") String cityCode);
	public List<Map<String,Object>> findAllSchool(@Param("exambatchId") String exambatchId, @Param("cityCode") String cityCode);
}