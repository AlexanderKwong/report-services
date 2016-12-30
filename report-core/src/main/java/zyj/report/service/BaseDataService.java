package zyj.report.service;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.annotation.CacheAfter;
import zyj.report.annotation.CacheBefore;
import zyj.report.annotation.ParentScope;
import zyj.report.annotation.Scope;
import zyj.report.business.task.SubjectInfo;
import zyj.report.common.SpringUtil;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：缓存一些必要的，重复使用的数据
 * PS:会有内存泄漏的危险
 * @author 邝晓林
 *
 */
@Service
public class BaseDataService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	/**
	 * 获取镇区
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.AREA)
	public   List<Map<String,Object>> getAreas(String exambatchId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else{
			List<Map<String, Object>> schoolsInfo = getSchools(exambatchId);
			Set<String> areaIdSet = new HashSet<>();
			List<Map<String, Object>> areasInfo =  schoolsInfo.stream().map(s -> {
				String schoolId = ObjectUtils.toString(s.get("AREA_ID"));
				if (!Objects.isNull(schoolId) && !areaIdSet.contains(schoolId)) {
					areaIdSet.add(schoolId);
					HashMap<String, Object> area = new HashMap<String, Object>();
					area.put("CITY_ID", s.get("CITY_ID"));
					area.put("CITY_NAME", s.get("CITY_NAME"));
					area.put("AREA_ID", s.get("AREA_ID"));
					area.put("AREA_NAME", s.get("AREA_NAME"));
					return area;
				} else return null;
			}).distinct().filter(m -> !Objects.isNull(m)).collect(Collectors.toList());
			return areasInfo;
		}
	}

	/**
	 * 获取镇区
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.AREA)
	public  Map<String,Object> getArea( String exambatchId,@ParentScope(Scope.AREA) String areaId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else{
			List<Map<String,Object>> areasInfo = getAreas(exambatchId);
			Optional<Map<String, Object>> areaInfo =  areasInfo.stream().filter(a->areaId.equals(a.get("AREA_ID"))).findFirst();
			return areaInfo.get();
		}
	}

	/**
	 * 获取学校
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.SCHOOL)
	public    List<Map<String,Object>> getSchools(String exambatchId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else {
			List<Map<String,Object>> classesInfo = getClasses(exambatchId);
			Set<String> schoolIdSet = new HashSet<>();
			List<Map<String, Object>> schoolsInfo =  classesInfo.stream().map(c -> {
				String schoolId = ObjectUtils.toString(c.get("SCH_ID"));
				if (!Objects.isNull(schoolId) && !schoolIdSet.contains(schoolId)) {
					schoolIdSet.add(schoolId);
					HashMap<String, Object> sch = new HashMap<String, Object>();
					sch.put("CITY_ID", c.get("CITY_ID"));
					sch.put("CITY_NAME", c.get("CITY_NAME"));
					sch.put("AREA_ID", c.get("AREA_ID"));
					sch.put("AREA_NAME", c.get("AREA_NAME"));
					sch.put("SCH_ID", c.get("SCH_ID"));
					sch.put("SCH_NAME", c.get("SCH_NAME"));
					return sch;
				} else return null;
			}).distinct().filter(m -> !Objects.isNull(m)).collect(Collectors.toList());
			return schoolsInfo;
		}
	}
	/**
	 * 获取学校
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.SCHOOL)
	public    List<Map<String,Object>> getSchoolsInArea(String exambatchId, @ParentScope(Scope.AREA) String areaId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else {
			List<Map<String, Object>> schoolsInfo = getSchools(exambatchId);
			List<Map<String, Object>> schoolsInfoInArea = schoolsInfo.stream().filter(s->areaId.equals(s.get("AREA_ID"))).collect(Collectors.toList());
			return schoolsInfoInArea;
		}
	}
	/**
	 * 获取学校
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.SCHOOL)
	public  Map<String,Object> getSchool(String exambatchId,@ParentScope(Scope.SCHOOL) String schoolId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else {
			List<Map<String,Object>> schoolsInfo = getSchools(exambatchId);
			Optional<Map<String, Object>> schoolInfo =  schoolsInfo.stream().filter(s -> schoolId.equals(s.get("SCH_ID"))).findFirst();
			return schoolInfo.get();
		}
	}

	/**
	 * 获取班级
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.CLASS)
	public    List<Map<String,Object>> getClasses(String exambatchId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else {
//			JyjRptExtMapper tmp = (JyjRptExtMapper) SpringUtil.getSpringBean(null, "jyjRptExtMapper");
			List<Map<String,Object>> classesInfoMap = jyjRptExtMapper.qryClassesInfo(exambatchId);
			return classesInfoMap;
		}
	}

	/**
	 * 获取班级
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.CLASS)
	public     List<Map<String,Object>> getClassesInArea(String exambatchId,@ParentScope(Scope.AREA) String areaId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else {
			List<Map<String,Object>> classesInfo = getClasses(exambatchId);
			List<Map<String, Object>> classesInfoInArea =  classesInfo.stream().filter(c -> areaId.equals(ObjectUtils.toString(c.get("AREA_ID")))).collect(Collectors.toList());
			return classesInfoInArea;
		}
	}

	/**
	 * 获取班级
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.CLASS)
	public     List<Map<String,Object>>  getClassesInSchool(String exambatchId,@ParentScope(Scope.SCHOOL) String schoolId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else {
			List<Map<String,Object>> classesInfo = getClasses(exambatchId);
			List<Map<String, Object>> classesInfoInSchool =  classesInfo.stream().filter(c -> schoolId.equals(ObjectUtils.toString(c.get("SCH_ID")))).collect(Collectors.toList());
			return classesInfoInSchool;
		}
	}
	/**
	 * 获取班级
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.CLASS)
	public   Map<String,Object> getClass(String exambatchId,@ParentScope(Scope.CLASS) String classesId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else {
			List<Map<String,Object>> classesInfo = getClasses(exambatchId);
			Optional<Map<String, Object>> classInfo = classesInfo.stream().filter(c -> classesId.equals(ObjectUtils.toString(c.get("CLS_ID")))).findFirst();
			return classInfo.get();
		}
	}

	/**
	 * 获取科目
	 * @param exambatchId
	 * @return
	 */
	@CacheBefore(scope = Scope.SUBJECT)
	public  List<Map<String,Object>> getSubjectByExamid(String exambatchId){
		if(StringUtils.isBlank(exambatchId))
			return null;
		else {
			List<Map<String,Object>> paperidsAndSubjects = jyjRptExtMapper.qrySubjectInfoByExam(exambatchId);
			return paperidsAndSubjects;
		}
	}

	@CacheBefore(scope = Scope.SUBJECT)
	public   Map<String,Object> getSubjectByPaperIdAndShortName(String exambatchId, String paperId, String shortNme){
		if(StringUtils.isBlank(exambatchId) || StringUtils.isBlank(paperId) || StringUtils.isBlank(shortNme))
			return null;
		else {
			Optional<Map<String, Object>> subjectInfo =  getSubjectByExamid(exambatchId).stream().filter(s->paperId.equals(s.get("PAPER_ID")) && shortNme.equals(s.get("SUBJECT"))).findFirst();
			return subjectInfo.get();
		}
	}

	@CacheBefore(scope = Scope.STUDENT)
	public List<Map<String, Object>> getStudentQuestion(String exambatchId, String parentScopeId, String level, Integer stuType, String paperId, String shortName){
		if (StringUtils.isBlank(exambatchId) || StringUtils.isBlank(paperId) || StringUtils.isBlank(shortName) || StringUtils.isBlank(level))
			return null;
		else {
			HashMap<String, Object> params = new HashMap<>();
			params.put("exambatchId",exambatchId);
			params.put("paperId",paperId);
			params.put("subject",shortName);
			params.put("student_type", stuType);
			params.put(level+"Id",parentScopeId);
			List<Map> questions = rptExpQuestionMapper.qryClassQuestionScore6(params);

			List<Integer> orderList = new ArrayList<Integer>();
			for (Map map : questions) {
				orderList.add(Integer.parseInt(map.get("QUESTION_ORDER").toString()));
			}
			params.put("orderList", orderList);
			List<Map<String,Object>>  beanList = rptExpQuestionMapper.qryStudentQuestionScore(params);
			return beanList;
		}
	}

	@CacheBefore(scope = Scope.STUDENT)
	public List<Map<String, Object>> getStudentSubjectsAndAllscore(String exambatchId, String parentScopeId, String level, Integer stuType){
		if (StringUtils.isBlank(exambatchId) ||  StringUtils.isBlank(level))
			return null;
		else{
			List<Map<String, Object>> subjects_cur = getSubjectByExamid(exambatchId);
			List<SubjectInfo> subjectList = subjects_cur.stream()
					.map(subject -> new SubjectInfo(subject.get("PAPER_ID").toString(), subject.get("SUBJECT").toString(), subject.get("SUBJECT_NAME").toString(),Integer.valueOf(subject.get("TYPE").toString())))
					.sorted((subject1, subject2) -> {
						return zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject1.getSubject()) - zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject2.getSubject());
					})
					.collect(Collectors.toList());
			HashMap<String, Object> params = new HashMap<>();
			params.put("exambatchId",exambatchId);
			params.put("student_type", stuType);
			params.put("subjectList", subjectList);
			params.put(level+"Id",parentScopeId);
			//TODO 这里等待时间过长，可以用futureTask优化
			List<Map<String, Object>>beanList = rptExpSubjectMapper.qryStudentSubjectScore2(params);
			List<Map<String, Object>>zongFen = rptExpAllscoreMapper.qryStudentSubjectAllScore(params);

			Map<String, Map<String, Object>> d = zyj.report.common.CalToolUtil.trans(zongFen, new String[]{"AREA_ID", "SCH_ID", "CLS_ID", "USER_ID"});
			beanList.parallelStream().forEach(m->{
				String userId = m.get("USER_ID").toString();
				String areaId = m.get("AREA_ID").toString();
				String schoolId = m.get("SCH_ID").toString();
				String classesId = m.get("CLS_ID").toString();
				Map zongFenOfOne = d.get(areaId + schoolId + classesId + userId);
				m.put("ALL_SCORE", zongFenOfOne.get("ALL_TOTAL"));
				m.put("ALL_RANK", zongFenOfOne.get("CITY_RANK"));
				m.put("ALL_RANK_SCH", zongFenOfOne.get("GRD_RANK"));
				m.put("ALL_RANK_CLS", zongFenOfOne.get("CLS_RANK"));
			});
			return beanList;
		}
	}

	/*****************内容缓存******************//*
	public   void  setBeanListCache(Map<String,Map<String,Object>> beansMap,String key){
		if(beanListCache == null ){
//			beanListCache = new WeakHashMap<String,Map<String,Map<String,Object>>>();
			beanListCache = new HashMap<String,Map<String,Map<String,Object>>>();
		}
		beanListCache.put(key,beansMap );
	}
	public  Optional<List<Map<String,Object>>> getBeanListCache(String key,String startWith){
		if(beanListCache != null){
			
			List<Map<String,Object>> beanList;
			Map<String,Map<String,Object>> cache=beanListCache.get(key);
			if(cache == null)  return Optional.ofNullable(null);
			Set<Map.Entry<String,Map<String,Object>>> entrySet = cache.entrySet();
			beanList = entrySet.stream().filter(entry->entry.getKey().startsWith(startWith)).map(entry->entry.getValue()).collect(Collectors.toList());
			return beanList.isEmpty()?Optional.ofNullable(null):Optional.of(beanList);
		}
		else return Optional.ofNullable(null);
	}
	*//************************************************************//*
	*//**
	 * 缓存 地区、学校。班级、科目 信息
	 * @param exambatchId
	 *//*
	public  void init(String exambatchId) throws ReportExportException {
		if(!StringUtils.isBlank(exambatchId)){
			ReportExportException err = new ReportExportException("已缓存其它批次，请清除缓存后在进行初始化！");
			logger.error("",err);
			throw err;
		}
		getSchNameMap(exambatchId);
		logger.info("-------学校缓存初始化成功");
		getAreaNameMap(exambatchId);
		logger.info("-------地区缓存初始化成功");
		getClassesByExamid(exambatchId);
		logger.info("-------班级缓存初始化成功");
		getSubjectByExamid(exambatchId);
		logger.info("-------科目缓存初始化成功");

	}
	
	//清除缓存
	public  void removeAllCache(){

	}*/
}
