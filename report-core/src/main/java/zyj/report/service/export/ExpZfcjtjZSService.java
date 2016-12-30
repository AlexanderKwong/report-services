package zyj.report.service.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.CalToolUtil;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpZfcjtjZSService extends BaseRptService {

	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	BaseDataService baseDataService;
	
	String excelName = "总分成绩统计.xls";
	String sheetName = "总分成绩统计";

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
		if(exambatchId == null ||cityCode == null ||subject == null || !subjects.containsKey(paperId+subject))
			return;
		if(subject.equalsIgnoreCase("NWL"))
			parmter.put("type", 0);
		else if(subject.equalsIgnoreCase("WK"))
			parmter.put("type", 1);
		else if(subject.equalsIgnoreCase("LK"))
			parmter.put("type", 2);
		
		double step = 30; //步长
		double lowest = 100;//最低
		// 映射查询结果与表标题
		Map<String, String> fieldMap;

		fieldMap = new HashMap<String, String>();
		fieldMap.put("区镇", "NAME");
		fieldMap.put("学校", "NAME");
		fieldMap.put("班级", "NAME");
		fieldMap.put("总人数", "CANDIDATES_NUM");
		fieldMap.put("参考人数", "TAKE_EXAM_NUM");
		fieldMap.put("平均分", "AVG_SCORE");
		fieldMap.put("最高分", "TOP_SCORE");
		fieldMap.put("最低分", "UP_SCORE");
		fieldMap.put("满分", "FULL_SCORE");
		fieldMap.put("A等率", "A");
		fieldMap.put("B等率", "B");
		fieldMap.put("C等率", "C");
		fieldMap.put("D等率", "D");
		fieldMap.put("总分1A等率", "ZF1_A_RATE");
		fieldMap.put("总分1B等率", "ZF1_B_RATE");
		fieldMap.put("总分1C等率", "ZF1_C_RATE");
		fieldMap.put("总分1D等率", "ZF1_D_RATE");
		fieldMap.put("总分2A等率", "ZF2_A_RATE");
		fieldMap.put("总分2B等率", "ZF2_B_RATE");
		fieldMap.put("总分2C等率", "ZF2_C_RATE");
		fieldMap.put("总分2D等率", "ZF2_D_RATE");
		fieldMap.put("总分3A等率", "ZF3_A_RATE");
		fieldMap.put("总分3B等率", "ZF3_B_RATE");
		fieldMap.put("总分3C等率", "ZF3_C_RATE");
		fieldMap.put("总分3D等率", "ZF3_D_RATE");

		//标题
		String scopeInTitle = level.equals("city2")?"区镇":((level.equals("city")||level.equals("area"))?"学校":"班级");
		List<String> titleList = new ArrayList<String>(Arrays.asList(new String[]{scopeInTitle,"总人数","参考人数","满分","平均分","最高分","最低分"}));//,"总分1A等率","总分1B等率","总分1C等率","总分1D等率","总分2A等率","总分2B等率","总分2C等率","总分2D等率","总分3A等率","总分3B等率","总分3C等率","总分3D等率"}));

		String grade= jyjRptExtMapper.qryExamGrade(exambatchId).get(0);

		for(int i = 1 ; i <=3 ; i++){
			if(grade.equals("初一")&&i==2)
				continue;
			titleList.add("总分"+i+"A等率");
			titleList.add("总分"+i+"B等率");
			titleList.add("总分"+i+"C等率");
			titleList.add("总分"+i+"D等率");
		}
		List<String> titleList2 = new ArrayList<String>(titleList);
		titleList2.add("分数段统计");

		// 获取学校地区名
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
//		Map<String,Map<String,Object>> areaNameMap = getAreaCache();
//		Map<String,Map<String,Object>> clsNameMap = getClassCache();
		//查全市的科目的综合指标
		List<Map<String,Object>> cityZH = rptExpAllscoreMapper.qryCityAllScoreInfo(parmter);
//		parmter.put("take_exam_num", cityZH.get(0).get("TAKE_EXAM_NUM"));
		
//1.获取分数列表		
		double total = Double.parseDouble(cityZH.get(0).get("TOP_SCORE").toString());
		double full = Double.parseDouble(cityZH.get(0).get("FULL_SCORE").toString());
		double avg = Double.parseDouble(cityZH.get(0).get("AVG_SCORE").toString());
		if(full>=500){
			step = 50;
			lowest = 200;
		}
		List<Integer> scoreList = new ArrayList<Integer>();
		
		double lower = full-(((int)full)%step==0?step:((int)full)%step);
		while((int)lower>=lowest){
			scoreList.add((int)lower);
			lower=lower-step;
		}
		parmter.put("step", (int)step);
		parmter.put("scoreList2", scoreList);
		parmter.put("highest", scoreList.get(0));
		parmter.put("lowest", scoreList.get(scoreList.size()-1));
//2.动态增加映射和标题		
		for(Integer score : scoreList){
			fieldMap.put("("+(score+(int)step)+"-"+score+"]", "UNDER"+score.toString());
			if(scoreList.indexOf(score)==0){
				titleList.add(">="+score);
				fieldMap.put(">="+score, "HE"+score.toString());
			}else if(scoreList.indexOf(score)==scoreList.size()-1){
				titleList.add("("+(score+(int)step)+"-"+score+"]");
				titleList.add("<"+score);
				fieldMap.put("<"+score, "LS"+score.toString());
			}else
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
		
		List<Map<String,Object>> allPersonNum = null;
		List<Map<String,Object>> ABCD_Rate = null;

		parmter.put("subjectListOfWL", new ArrayList(Arrays.asList(CalToolUtil.getRedundantSubject())));
		switch(level){
		case "classes"://班
//			parmter.put("schoolId", clsNameMap.get(parmter.get("classesId")).get("SCH_ID"));
			parmter.put("schoolId", baseDataService.getClass(exambatchId, parmter.get("classesId").toString()).get("SCH_ID"));
		case "school"://校
			clsFSD =  rptExpAllscoreMapper.qryScorePersonNumByClassAllscore(parmter);
			clsZH = rptExpAllscoreMapper.qryClassAllScoreInfo(parmter);
			allPersonNum = rptExpSubjectMapper.qryClassSubjectScore2(parmter);
			/**20160705 更改 增加需求总分1、2、3的ABCD等率*/
			parmter.put("GroupBy","class");
			ABCD_Rate=rptExpSubjectMapper.qryZSzfrptABCDRate(parmter);

			for(Map<String ,Object> m : clsZH){
				m.put("NAME", m.get("CLS_NAME"));
			}
			beanList.addAll(CollectionsUtil.leftjoinMapByKey(CollectionsUtil.leftjoinMapByKey(CollectionsUtil.leftjoinMapByKey(clsZH, clsFSD, "CLS_ID"), allPersonNum, "CLS_ID"), ABCD_Rate, "CLS_ID"));
//			parmter.put("areaId", schNameMap.get(parmter.get("schoolId")).get("AREA_ID"));
			parmter.put("areaId", baseDataService.getSchool(exambatchId, parmter.get("schoolId").toString()).get("AREA_ID"));
		case "area"://区
		case "city2": //分区镇而不是分学校统计
			areaFSD =  rptExpAllscoreMapper.qryScorePersonNumByAreaAllscore(parmter);
			areaZH = rptExpAllscoreMapper.qryAreaAllScoreInfo(parmter);
			allPersonNum = rptExpSubjectMapper.qryAreaSubjectScore2(parmter);
			/**20160705 更改 增加需求总分1、2、3的ABCD等率*/
			parmter.put("GroupBy","area");
			ABCD_Rate=rptExpSubjectMapper.qryZSzfrptABCDRate(parmter);

			for(Map<String ,Object> m : areaZH){
				try{
					m.put("NAME", m.get("AREA_NAME"));
				}catch(Exception e){
					m.put("NAME", "市直");
					System.out.println("Warn : 直属学校没有县区信息");
				}
			}
			areaZH=CollectionsUtil.leftjoinMapByKey(CollectionsUtil.leftjoinMapByKey(CollectionsUtil.leftjoinMapByKey(areaZH, allPersonNum, "AREA_ID"),areaFSD, "AREA_ID"),ABCD_Rate,"AREA_ID");
		case "city"://市
			if (!level.equals("city2")) {
				schZH = rptExpAllscoreMapper.qrySchoolAllScoreInfo(parmter);
				schFSD = rptExpAllscoreMapper.qryScorePersonNumBySchoolAllscore(parmter);
				allPersonNum = rptExpSubjectMapper.qrySchoolSubjectScore2(parmter);
				/**20160705 更改 增加需求总分1、2、3的ABCD等率*/
				parmter.put("GroupBy","school");
				ABCD_Rate=rptExpSubjectMapper.qryZSzfrptABCDRate(parmter);

				for (Map<String, Object> m : schZH) {
					m.put("NAME", m.get("SCH_NAME"));
				}
				beanList.addAll(CollectionsUtil.leftjoinMapByKey(CollectionsUtil.leftjoinMapByKey(CollectionsUtil.leftjoinMapByKey(schZH, schFSD, "SCH_ID"),allPersonNum,"SCH_ID"),ABCD_Rate,"SCH_ID"));
			}
			 if(areaZH!=null)
				 beanList.addAll(areaZH);
			 cityFSD = rptExpAllscoreMapper.qryScorePersonNumByCityAllscore(parmter);
			 allPersonNum = rptExpSubjectMapper.qryCitySubjectScore2(parmter);
			/**20160705 更改 增加需求总分1、2、3的ABCD等率*/
			parmter.put("GroupBy",null);
			ABCD_Rate=rptExpSubjectMapper.qryZSzfrptABCDRate(parmter);

			 cityFSD.put("NAME", "全市");
			cityFSD.putAll(allPersonNum.get(0));
			cityFSD.putAll(ABCD_Rate.get(0));
			cityFSD.putAll(cityZH.get(0));
			beanList.add(cityFSD);
//			 beanList.addAll(CollectionsUtil.leftjoinMapByKey(CollectionsUtil.leftjoinMapByKey(cityZH, tmp, "CITY_CODE"),allPersonNum,"CITY_CODE"));
			break;
			
		}
//4.生成报表		
			String [][]conList = map2objects(fieldMap, titleList.toArray(new String[titleList.size()]), beanList);

//			int [][] avgmerge = {{0,1,0,2},{1,1,1,2},{2,1,2,2},{3,1,3,2},{4,1,4,2},{5,1,5,2},{6,1,6,2},{7,1,10,1},{11,1,14,1},{15,1,18,1},{19,1,titleList.size()-1,1}};
			if(grade.equals("初一")){
				int [][] avgmerge = {{0,1,0,2},{1,1,1,2},{2,1,2,2},{3,1,3,2},{4,1,4,2},{5,1,5,2},{6,1,6,2},{7,1,10,1},{11,1,14,1},{15,1,titleList.size()-1,1}};
				//去掉 “总分*”字样
				for(int i = 7;i<=14;i++){
					titleList2.set(i, titleList2.get(i).substring(0,3));
					titleList.set(i, titleList.get(i).substring(3,6));
				}
				String[][] titelList = {titleList2.toArray(new String[titleList2.size()]),titleList.toArray(new String[titleList.size()])};
				ExportUtil.createExpExcel(titelList, conList, avgmerge, "分" + scopeInTitle + excelName, sheetName, "分" + scopeInTitle + BaseRptService.subjects.get(subject) + "总分成绩统计", path);
			}else{
				int [][] avgmerge = {{0,1,0,2},{1,1,1,2},{2,1,2,2},{3,1,3,2},{4,1,4,2},{5,1,5,2},{6,1,6,2},{7,1,10,1},{11,1,14,1},{15,1,18,1},{19,1,titleList.size()-1,1}};
				//去掉 “总分*”字样
				for(int i = 7;i<=18;i++){
					titleList2.set(i, titleList2.get(i).substring(0,3));
					titleList.set(i, titleList.get(i).substring(3,6));
				}
				String[][] titelList = {titleList2.toArray(new String[titleList2.size()]),titleList.toArray(new String[titleList.size()])};
				ExportUtil.createExpExcel(titelList, conList, avgmerge, "分" + scopeInTitle + excelName, sheetName, "分" + scopeInTitle + BaseRptService.subjects.get(subject) + "总分成绩统计", path);
			}
//			//去掉 “总分*”字样
//			for(int i = 7;i<=18;i++)
//				titleList.set(i, titleList.get(i).substring(3,6));

//			String[][] titelList = {new String[]{(level==5?"区/镇":(level<3?"学校":"班级")),"总人数","参考人数","满分","平均分","最高分","最低分","总分1","","","","总分2","","","","总分3","","","","分数段统计"},titleList.toArray(new String[titleList.size()])};
//		String[][] titelList = {titleList2.toArray(new String[titleList2.size()]),titleList.toArray(new String[titleList.size()])};
//			createExpExcel(titelList, conList, avgmerge, "分"+(level==5?"区镇":(level<3?"学校":"班级"))+excelName, sheetName, "分"+(level==5?"区镇":(level<3?"学校":"班级"))+BaseRptService.subjects.get(subject)+"总分成绩统计", path);
	}

	
}
