package zyj.report.common;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：缓存一些必要的，重复使用的数据
 * PS:会有内存泄漏的危险
 * @author 邝晓林
 *
 */
public class DataCacheUtil {
	private static Logger logger = LoggerFactory.getLogger(DataCacheUtil.class);

//	private static SpringContextImp springContext = new SpringContextImp();

	private static Map<String,String> questionTypeNameMap = CollectionsUtil.parserToMap("{\"1\":\"单选\",\"2\":\"多选\",\"3\":\"\",\"4\":\"\"}") ;
/*
	private static Map<String,Map<String,Object>> schNameMap;
	private static Map<String,Map<String,Object>> areaNameMap;
	private volatile static String exambatchId = "";

//	private static String subjectOfExam ;
	private static Map<String,Map<String,Object>> classesInfoMap;
	private static Map<String,Map<String,Map<String,Object>>> beanListCache;//超级无敌数据字典
	//每行是一个科目信息,任意行的第一列 为paperId,第二列为shortName，第三列为subjectName
	private static List<Map<String,Object>> paperidsAndSubjects;
*/

	public static String getQuestionTypeName(String qstTipy){
		return ObjectUtils.toString(questionTypeNameMap.get(qstTipy));
	}
/*	@Deprecated
	public static  Optional<Map<String,Map<String,Object>>> getSchNameMap(){
		if(!StringUtils.isBlank(exambatchId))
			return Optional.of(schNameMap);
		else return Optional.ofNullable(null);
	}
	@Deprecated
	public static  Optional<Map<String,Map<String,Object>>> getAreaNameMap(){
		if(!StringUtils.isBlank(exambatchId))
			return Optional.of(areaNameMap);
		else return Optional.ofNullable(null);
	}
	@Deprecated
	public static  Optional<Map<String,Map<String,Object>>> getClassInfoMap(){
		if(!StringUtils.isBlank(exambatchId))
			return Optional.of(classesInfoMap);
		else return Optional.ofNullable(null);
	}
	@Deprecated
	public static  Optional<List<Map<String,Object>>> getSubjectInfoMap(){
		if(!StringUtils.isBlank(exambatchId))
			return Optional.of(paperidsAndSubjects);
		else return Optional.ofNullable(null);
	}

	public static  Optional<Map<String,Map<String,Object>>> getSchNameMap(String exambatchId_cur){
		if(StringUtils.isBlank(exambatchId_cur))
			return Optional.ofNullable(null);
		else if (exambatchId_cur.equals(exambatchId))
			return Optional.ofNullable(schNameMap);
		else if (StringUtils.isBlank(exambatchId )){
			JyjRptExtMapper tmp = (JyjRptExtMapper) SpringUtil.getSpringBean(null, "jyjRptExtMapper");
			List<Map<String,Object>> schs = tmp.qryAreaAndSchName(exambatchId_cur);
			schNameMap =  CalToolUtil.trans(schs, new String[]{"SCH_ID"});
			return Optional.of(schNameMap);
		}else{
			return Optional.ofNullable(null);
		}
	}

	public static  Optional<Map<String,Map<String,Object>>> getAreaNameMap(String exambatchId_cur){
		if(StringUtils.isBlank(exambatchId_cur))
			return Optional.ofNullable(null);
		else if (exambatchId_cur.equals(exambatchId))
			return Optional.ofNullable(areaNameMap);
		else if (StringUtils.isBlank(exambatchId )){
			JyjRptExtMapper tmp = (JyjRptExtMapper) SpringUtil.getSpringBean(null, "jyjRptExtMapper");
			List<Map<String,Object>> schs = tmp.qryAreaName(exambatchId_cur);
			areaNameMap =  CalToolUtil.trans(schs, new String[]{"AREA_ID"});
			return Optional.of(areaNameMap);
		}else{
			return Optional.ofNullable(null);
		}
	}

	public static synchronized String getSubjectByExamid(String exambatchId_cur){
		if(StringUtils.isBlank(exambatchId_cur))
			return null;
		else if (exambatchId_cur.equals(exambatchId)&&subjectOfExam!=null)
			return subjectOfExam;
		else {
			exambatchId = exambatchId_cur;
//			JyjRptExtMapper tmp = (JyjRptExtMapper) springContext
//					.getSpringBean(null, "jyjRptExtMapper");
			JyjRptExtMapper tmp = (JyjRptExtMapper) SpringUtil.getSpringBean(null, "jyjRptExtMapper");
			subjectOfExam = tmp.qrySubjectsByExam(exambatchId);
			return subjectOfExam;
		}
	}


	public static  synchronized Optional<List<Map<String,Object>>> getSubjectByExamid(String exambatchId_cur){
		if(StringUtils.isBlank(exambatchId_cur))
			return Optional.ofNullable(null);
		else if (exambatchId_cur.equals(exambatchId))
			return Optional.ofNullable(paperidsAndSubjects);
		else if (StringUtils.isBlank(exambatchId )){
			JyjRptExtMapper tmp = (JyjRptExtMapper) SpringUtil.getSpringBean(null, "jyjRptExtMapper");
			paperidsAndSubjects = tmp.qrySubjectInfoByExam(exambatchId_cur);

			return Optional.of(paperidsAndSubjects);
		}else{
			return Optional.ofNullable(null);
		}
	}


	public static synchronized  Optional<Map<String,Map<String,Object>>> getClassesByExamid(String exambatchId_cur){
		if(StringUtils.isBlank(exambatchId_cur))
			return Optional.ofNullable(null);
		else if(exambatchId_cur.equals(exambatchId)){
			return Optional.ofNullable(classesInfoMap);
		}else if (StringUtils.isBlank(exambatchId )){
			JyjRptExtMapper tmp = (JyjRptExtMapper) SpringUtil.getSpringBean(null, "jyjRptExtMapper");
			List<Map<String,Object>> tmpList = tmp.qryClassesInfo(exambatchId_cur);
			classesInfoMap = CalToolUtil.trans(tmpList, new String[]{"CLS_ID"});
			return Optional.of(classesInfoMap);
		}else{
			return Optional.ofNullable(null);
		}
	}

	public static synchronized void  setBeanListCache(Map<String,Map<String,Object>> beansMap,String key){
		if(beanListCache == null ){
//			beanListCache = new WeakHashMap<String,Map<String,Map<String,Object>>>();
			beanListCache = new HashMap<String,Map<String,Map<String,Object>>>();
		}
		beanListCache.put(key,beansMap );
	}
	public static Optional<List<Map<String,Object>>> getBeanListCache(String key,String startWith){
		if(beanListCache != null){
			
			List<Map<String,Object>> beanList;
			Map<String,Map<String,Object>> cache=beanListCache.get(key);
			if(cache == null)  return Optional.ofNullable(null);
			Set<Map.Entry<String,Map<String,Object>>> entrySet = cache.entrySet();

			beanList = entrySet.stream().filter(entry->entry.getKey().startsWith(startWith)).map(entry->entry.getValue()).collect(Collectors.toList());
			return beanList.isEmpty()?Optional.ofNullable(null):Optional.of(beanList);
		}
		else return Optional.ofNullable(null);
	}*/
	/************************************************************/
/*	public static void init(){
//		JyjRptExtMapper tmp = (JyjRptExtMapper)springContext.getSpringBean(null, "jyjRptExtMapper");

		System.out.println("-------学校名初始化成功");

		System.out.println("-------地区名初始化成功");
	}
	
	static{init();}
*/
	/**
	 * 缓存 地区、学校。班级、科目 信息
	 * @param
	 */
	/*public static void init(String exambatchId_cur) throws ReportExportException {
		if(!StringUtils.isBlank(exambatchId)){
			ReportExportException err = new ReportExportException("已缓存其它批次，请清除缓存后在进行初始化！");
			logger.error("",err);
			throw err;
		}
		getSchNameMap(exambatchId_cur);
		logger.info("-------学校缓存初始化成功");
		getAreaNameMap(exambatchId_cur);
		logger.info("-------地区缓存初始化成功");
		getClassesByExamid(exambatchId_cur);
		logger.info("-------班级缓存初始化成功");
		getSubjectByExamid(exambatchId_cur);
		logger.info("-------科目缓存初始化成功");

		exambatchId = exambatchId_cur;
	}*/
	
	//清除缓存
	/*public static void removeAllCache(){
		exambatchId = "";
//		subjectOfExam =null;
		beanListCache=null;
		classesInfoMap = null;
		schNameMap = null;
		areaNameMap = null;
		paperidsAndSubjects = null;
	}*/
}
