package zyj.report.service.export;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpGeKeZongHeService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	@Autowired
	BaseDataService baseDataService;


	// 映射查询结果与表标题
		final private static  Map<String, String> fieldMap ;
		static{
			fieldMap = new HashMap<String, String>();
			fieldMap.put("科目","SUBJECT_NAME");
			fieldMap.put("总人数","CANDIDATES_NUM");
			fieldMap.put("参加人数","TAKE_EXAM_NUM");
			fieldMap.put("平均分","AVG_SCORE");
			fieldMap.put("排名","RANK");
			fieldMap.put("级均分","GRADE_AVG_SCORE");
			fieldMap.put("最高分","TOP_SCORE");
			fieldMap.put("最低分","UP_SCORE");
			fieldMap.put("满分率","FULL_RANK");
			fieldMap.put("优秀率","LEVEL_GD_RATE");
			fieldMap.put("良好率","LEVEL_FN_RATE");
			fieldMap.put("及格率","LEVEL_PS_RATE");
			fieldMap.put("低分率","LOW_RATE");
			fieldMap.put("超均率","OVER_AVG_RATE");
			fieldMap.put("比均率","RATIO_AVG_RATE");
			fieldMap.put("标准差","STANDARD_DEV");
			fieldMap.put("级难度","DIFFICULT");
			fieldMap.put("级区分度","DISCRIMINATION");
			fieldMap.put("难度","DIFFICULT");
			fieldMap.put("区分度","DISCRIMINATION");
		}
	
		
	@Override
	public void exportData(Map<String, Object> params) throws Exception {
//		用于生成市(1)，区(2)，学校(3)，年级(4)的“各科综合”报表
		String level =(String)params.get("level") ;
		String exambatchId =(String) params.get("exambatchId");
		String cityCode =(String) params.get("cityCode");
		String path = (String) params.get("pathFile");
		int stuType = (Integer)params.get("stuType");
		//校验参数,暂不校验cityCode
		if(exambatchId == null  ||path == null ||level == null)
			return;

		//标题
		String title  ="科目,总人数,参加人数,平均分,排名,级均分,最高分,最低分,满分率,优秀率,良好率,及格率,低分率,超均率,比均率,标准差,级难度,级区分度";
		if(level.equals("city")){
			title  = "科目,总人数,参加人数,平均分,最高分,最低分,满分率,优秀率,良好率,及格率,低分率,标准差,级难度,级区分度";
		}
		
		List<Map<String,Object>> beanList = null ;

		//分情况获取参数
		String areaId = null;
		String schoolId = null;
		String classesId = null;
		Object liKeYingKao = null;
		Object wenKeYingKao = null;
		switch(level){
		case "city"://查全市
			
			beanList = rptExpSubjectMapper.qrySubjectQualityInfo(params);
			for(Map sub:beanList){//有没有文理科数学的应考人数
				if(sub.get("SUBJECT").toString().indexOf("WSX")!=-1||sub.get("SUBJECT").toString().indexOf("WZ")!=-1)
					wenKeYingKao = sub.get("CANDIDATES_NUM");
				if(sub.get("SUBJECT").toString().indexOf("LSX")!=-1||sub.get("SUBJECT").toString().indexOf("LZ")!=-1)
					liKeYingKao = sub.get("CANDIDATES_NUM");
			}
			if(beanList.size() != 0){
				List<Map> cityAllSco = rptExpAllscoreMapper.qryCityAllScoreInfo(params);
				for(Map cityAllS : cityAllSco){
					if(cityAllS.get("TYPE").toString().equals("1")){
						cityAllS.put("SUBJECT", "WK");
						cityAllS.put("CANDIDATES_NUM", wenKeYingKao);//随便拿一科的应考人数作为应考人数
						cityAllS.put("SUBJECT_NAME","文科总分");
						beanList.add(cityAllS);
					}
					if(cityAllS.get("TYPE").toString().equals("2")){
						cityAllS.put("SUBJECT", "LK");
						cityAllS.put("CANDIDATES_NUM", liKeYingKao);//随便拿一科的应考人数作为应考人数
						cityAllS.put("SUBJECT_NAME","理科总分");
						beanList.add(cityAllS);
					}
					if(cityAllS.get("TYPE").toString().equals("0")){
						cityAllS.put("SUBJECT", "ZF");
						cityAllS.put("CANDIDATES_NUM", beanList.get(0).get("CANDIDATES_NUM"));//随便拿一科的应考人数作为应考人数
						cityAllS.put("SUBJECT_NAME","总分");
						beanList.add(cityAllS);
					}
				}
			}
			break;
		case "area"://查地区
			areaId = (String )params.get("areaId");
			if(areaId == null)
				return;
			beanList = rptExpSubjectMapper.qryAreaSubjectInfo(params);
			for(Map sub:beanList){//有没有文理科数学的应考人数
				if(sub.get("SUBJECT").toString().indexOf("WSX")!=-1||sub.get("SUBJECT").toString().indexOf("WZ")!=-1)
					wenKeYingKao = sub.get("CANDIDATES_NUM");
				if(sub.get("SUBJECT").toString().indexOf("LSX")!=-1||sub.get("SUBJECT").toString().indexOf("LZ")!=-1)
					liKeYingKao = sub.get("CANDIDATES_NUM");
			}
			if(beanList.size() != 0){
				List<Map> areaAllSco = rptExpAllscoreMapper.qryAreaAllScoreInfo(params);
				for(Map areaAllS : areaAllSco){
					if(areaAllS.get("TYPE").toString().equals("1")){
						areaAllS.put("SUBJECT", "WK");
						areaAllS.put("CANDIDATES_NUM",wenKeYingKao);//随便拿一科的应考人数作为应考人数
						areaAllS.put("SUBJECT_NAME","文科总分");
						beanList.add(areaAllS);
					}
					if(areaAllS.get("TYPE").toString().equals("2")){
						areaAllS.put("SUBJECT", "LK");
						areaAllS.put("CANDIDATES_NUM",liKeYingKao );//随便拿一科的应考人数作为应考人数
						areaAllS.put("SUBJECT_NAME","理科总分");
						beanList.add(areaAllS);
					}
					if(areaAllS.get("TYPE").toString().equals("0")){
						areaAllS.put("SUBJECT", "ZF");
						areaAllS.put("CANDIDATES_NUM", beanList.get(0).get("CANDIDATES_NUM"));//随便拿一科的应考人数作为应考人数
						areaAllS.put("SUBJECT_NAME","总分");
						beanList.add(areaAllS);
					}
				}
			}
			break;
		case "school"://查学校
//			areaId = (String )params.get("areaId");
			schoolId = (String )params.get("schoolId");
			if(schoolId == null)
				return;
			beanList = rptExpSubjectMapper.qrySchoolSubjectInfo(params);
			for(Map sub:beanList){//有没有文理科数学的应考人数
				if(sub.get("SUBJECT").toString().indexOf("WSX")!=-1||sub.get("SUBJECT").toString().indexOf("WZ")!=-1)
					wenKeYingKao = sub.get("CANDIDATES_NUM");
				if(sub.get("SUBJECT").toString().indexOf("LSX")!=-1||sub.get("SUBJECT").toString().indexOf("LZ")!=-1)
					liKeYingKao = sub.get("CANDIDATES_NUM");
			}
			if(beanList.size() != 0){
				List<Map> schAllSco = rptExpAllscoreMapper.qrySchoolAllScoreInfo(params);
				for(Map schAllS : schAllSco){
					if(schAllS.get("TYPE").toString().equals("1")){
						schAllS.put("SUBJECT", "WK");
						schAllS.put("CANDIDATES_NUM", wenKeYingKao);//随便拿一科的应考人数作为应考人数
						schAllS.put("SUBJECT_NAME","文科总分");
						beanList.add(schAllS);
					}
					if(schAllS.get("TYPE").toString().equals("2")){
						schAllS.put("SUBJECT", "LK");
						schAllS.put("CANDIDATES_NUM", liKeYingKao);//随便拿一科的应考人数作为应考人数
						schAllS.put("SUBJECT_NAME","理科总分");
						beanList.add(schAllS);
					}
					if(schAllS.get("TYPE").toString().equals("0")){
						schAllS.put("SUBJECT", "ZF");
						schAllS.put("CANDIDATES_NUM", beanList.get(0).get("CANDIDATES_NUM"));//随便拿一科的应考人数作为应考人数
						schAllS.put("SUBJECT_NAME","总分");
						beanList.add(schAllS);
					}
				}
			}
			break;
		case "classes"://查班级
			//缺总分
			classesId = (String )params.get("classesId");
			if(classesId == null)
				return;
			params.put("classesId", classesId);
			beanList = rptExpSubjectMapper.qryClassSubjectInfo(params);
			if(beanList.size() != 0){
				List<Map> totalList  = rptExpAllscoreMapper.qryClassAllScoreInfo(params);
				for(Map total : totalList){
				if(total.get("TYPE").toString().equals("0")){
					total.put("SUBJECT", "ZF");
					total.put("CANDIDATES_NUM", beanList.get(0).get("CANDIDATES_NUM"));//随便拿一科的应考人数作为应考人数
					total.put("SUBJECT_NAME","总分");
					beanList.add(total);
				}
				else if(total.get("TYPE").toString().equals("1")){
					total.put("SUBJECT", "WK");
					total.put("CANDIDATES_NUM", beanList.get(0).get("CANDIDATES_NUM"));//随便拿一科的应考人数作为应考人数
					total.put("SUBJECT_NAME","文科总分");
					beanList.add(total);
				}
				else if(total.get("TYPE").toString().equals("2")){
					total.put("SUBJECT", "LK");
					total.put("CANDIDATES_NUM", beanList.get(0).get("CANDIDATES_NUM"));//随便拿一科的应考人数作为应考人数
					total.put("SUBJECT_NAME","理科总分");
					beanList.add(total);
				}
				}
				
			}
			break;
			
		}
		if (beanList.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");;
		List<Map<String, Object>>subjectList =  baseDataService.getSubjectByExamid(exambatchId);
		Map<String , Map<String, Object>> subjectMap = CalToolUtil.trans(subjectList,new String[]{"PAPER_ID","SUBJECT"});
		for(Map s : beanList){
			if (s.get("SUBJECT_NAME") == null){
				String paperId = s.get("PAPER_ID")==null?"":s.get("PAPER_ID").toString();
				s.putIfAbsent("SUBJECT_NAME", ObjectUtils.toString(subjectMap.get(paperId + s.get("SUBJECT")).get("SUBJECT_NAME")));
			}
		}
		zyj.report.common.CalToolUtil.sortByValue(beanList, "SUBJECT",
				zyj.report.common.CalToolUtil.getSubjectOrder());

		String[] titleArr = title.split(",");
		String[][] conList = map2objects(fieldMap, titleArr, beanList);
		int[][] avgmerge = {};
		String[][] titelList = { titleArr };
		ExportUtil.createExpExcel(titelList, conList, avgmerge, "各科综合.xls", "各科综合", null,
				path);

	}

	
	
}
