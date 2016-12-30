package zyj.report.service.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpKmcjtjZSService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	BaseDataService baseDataService;
	
	
	String excelName = "科目成绩统计.xls";
	String sheetName = "科目成绩统计";

	@SuppressWarnings("unchecked")
	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {
		
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String path = (String) parmter.get("pathFile");
		String subject = (String) parmter.get("subject");
		String paperId = parmter.get("paperId").toString();
		int stuType = (Integer)parmter.get("stuType");
		String level = parmter.get("level").toString();
//校验参数,
		if(exambatchId == null ||cityCode == null ||subject == null )
			return;
		
		double step = 10; //步长
		//标题
		String scopeInTitle = level.equals("city2")?"区镇":((level.equals("city")||level.equals("area"))?"学校":"班级");
		List<String> titleList = new ArrayList<String>(Arrays.asList(new String[]{scopeInTitle,"总人数","实考人数","平均分","最高分","最低分","A等率","B等率","C等率","D等率","满分率"}));
		
		// 映射查询结果与表标题
				Map<String, String> fieldMap;
				fieldMap = new HashMap<String, String>();
				fieldMap.put("区镇", "NAME");
				fieldMap.put("学校", "NAME");
				fieldMap.put("班级", "NAME");
				fieldMap.put("总人数", "CANDIDATES_NUM");
				fieldMap.put("实考人数", "TAKE_EXAM_NUM");
				fieldMap.put("平均分", "AVG_SCORE");
				fieldMap.put("最高分", "TOP_SCORE");
				fieldMap.put("最低分", "UP_SCORE");
				fieldMap.put("A等率", "A");
				fieldMap.put("B等率", "B");
				fieldMap.put("C等率", "C");
				fieldMap.put("D等率", "D");
				fieldMap.put("满分率", "FULL_RANK");
		// 获取学校地区名		
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
//		Map<String,Map<String,Object>> areaNameMap = getAreaCache();
//		Map<String,Map<String,Object>> clsNameMap = getClassCache();
		//查全市的科目的综合指标
		List<Map<String,Object>> cityZH = rptExpSubjectMapper.qrySubjectQualityInfo2(parmter);
		if (cityZH.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");
		parmter.put("take_exam_num", cityZH.get(0).get("TAKE_EXAM_NUM"));
		
//1.获取分数列表		
		double total = Double.parseDouble(cityZH.get(0).get("TOP_SCORE").toString());
		double full = Double.parseDouble(cityZH.get(0).get("FULL_SCORE").toString());
		double avg = Double.parseDouble(cityZH.get(0).get("AVG_SCORE").toString());
		if(full>=200)
			step = 20;
		List<Integer> scoreList = new ArrayList<Integer>();
		
		double lower = full-(((int)full)%step==0?step:((int)full)%step);
		while((int)lower>=0){
			scoreList.add((int)lower);
			lower=lower-step;
		}
		parmter.put("step", step);
		parmter.put("scoreList2", scoreList);
//2.动态增加映射和标题		
		for(Integer score : scoreList){
			fieldMap.put("("+(score+(int)step)+"-"+score+"]", "UNDER"+score.toString());
			titleList.add("("+(score+(int)step)+"-"+score+"]");
		}
//3.查综合指标（报表前半段）和分数段（报表后半段）		
		List<Map<String,Object>> beanList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> schZH = null;
		List<Map<String,Object>> areaZH = null;
		List<Map<String,Object>> clsZH = null;
		
		//查学校的各个分数段人数
		List<Map<String,Object>> schFSD = null;
		//查班级的各个分数段人数
		List<Map<String,Object>> clsFSD = null;
		//查区县的各个分数段人数
		List<Map<String,Object>> areaFSD = null;
		//查全市的各个分数段人数
		Map<String,Object> cityFSD = null;
		switch(level){
		case "classes"://班
//			parmter.put("schoolId", clsNameMap.get(parmter.get("classesId")).get("SCH_ID"));
			parmter.put("schoolId", baseDataService.getClass(exambatchId, parmter.get("classesId").toString()).get("SCH_ID"));
		case "school"://校
			clsFSD =  rptExpSubjectMapper.qryScorePersonNumByClassSubject(parmter);
			clsZH = rptExpSubjectMapper.qryClassSubjectInfo2(parmter);
			for(Map<String ,Object> m : clsZH){
				m.put("NAME", m.get("CLS_NAME"));
			}
			beanList.addAll(CollectionsUtil.leftjoinMapByKey(clsZH, clsFSD, "CLS_ID"));
//			parmter.put("areaId", schNameMap.get(parmter.get("schoolId")).get("AREA_ID"));
			parmter.put("areaId", baseDataService.getSchool(exambatchId,parmter.get("schoolId").toString() ).get("AREA_ID"));
		case "area"://区
		case "city2": //分区镇而不是分学校统计
			areaFSD =  rptExpSubjectMapper.qryScorePersonNumByAreaSubject(parmter);
			areaZH = rptExpSubjectMapper.qryAreaSubjectInfo2(parmter);
			for(Map<String ,Object> m : areaZH){
					m.put("NAME", m.get("AREA_NAME"));
			}
		case "city"://市
			if (!level.equals("city2")) {
				schZH = rptExpSubjectMapper.qrySchoolSubjectInfo2(parmter);
				schFSD = rptExpSubjectMapper.qryScorePersonNumBySchoolSubject(parmter);
				for (Map<String, Object> m : schZH) {
					m.put("NAME", m.get("SCH_NAME"));
				}
				beanList.addAll(CollectionsUtil.leftjoinMapByKey(schZH, schFSD, "SCH_ID"));
			}
			if (areaFSD != null && areaZH != null)
				beanList.addAll(CollectionsUtil.leftjoinMapByKey(areaZH, areaFSD, "AREA_ID"));
			cityFSD = rptExpSubjectMapper.qryScorePersonNumByCitySubject(parmter);
			cityFSD.put("NAME", "全市");
			List<Map<String, Object>> tmp = new ArrayList<Map<String, Object>>();
			tmp.add(cityFSD);
			beanList.addAll(CollectionsUtil.leftjoinMapByKey(cityZH, tmp, "CITY_CODE"));
			break;

		}
//4.生成报表		
			String [][]conList = map2objects(fieldMap, titleList.toArray(new String[titleList.size()]), beanList);
			int [][] avgmerge = {{0,1,0,2},{1,1,9,1},{10,1,titleList.size()-1,1}};

			String[][] titelList = {new String[]{scopeInTitle,"统计项目","","","","","","","","","分数段统计"},titleList.toArray(new String[titleList.size()])};
			ExportUtil.createExpExcel(titelList, conList, avgmerge, "分" +
					scopeInTitle +
					excelName, sheetName, "分" +
					scopeInTitle +
					parmter.get("subjectName") + "科目成绩统计", path);
	}

	
}
