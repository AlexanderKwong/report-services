package zyj.report.service.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.business.task.SubjectInfo;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpPingJunFenDuiBiService extends BaseRptService {
	

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	@Autowired
	BaseDataService baseDataService;
	
	public void exportData(Map<String,Object> parmter) throws Exception{	
		String exambatchId = parmter.get("exambatchId").toString();
		String cityCode = parmter.get("cityCode").toString();
		String subject0 =  parmter.get("subject").toString();
		String pathFile = parmter.get("pathFile").toString();
		int stuType = (Integer)parmter.get("stuType");
		String level = parmter.get("level").toString();

		// 映射查询结果与表标题
		 Map<String, String> fieldMap ;
			fieldMap = new HashMap<String, String>();
			fieldMap.put("学校、区县", "SCH_NAME");

		// 获取学校地区名		
		
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
//		Map<String,Map<String,Object>> areaNameMap = getAreaCache();
//		List<Map<String,Object>> subjects_cur = getSubjectCache();
		List<Map<String,Object>> subjects_cur = baseDataService.getSubjectByExamid(exambatchId);
		String preffix = "";
//产生查询条件List
		List<SubjectInfo> subjectList =null;
		List<SubjectInfo> subjectInfoList = subjects_cur.stream()
				.map(subject -> new SubjectInfo(subject.get("PAPER_ID").toString(), subject.get("SUBJECT").toString(), subject.get("SUBJECT_NAME").toString(),(Integer)subject.get("TYPE")))
				.sorted((subject1, subject2) -> {
					return zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject1.getSubject()) - zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject2.getSubject());
				})
				.collect(Collectors.toList());

		if("WK".equals(subject0)){
			subjectList = subjectInfoList.stream().filter(subjectInfo -> 1 == subjectInfo.getType()).collect(Collectors.toList());
			parmter.put("type", 1);
			preffix = "W_";
		}
		if("LK".equals(subject0)){
			subjectList = subjectInfoList.stream().filter(subjectInfo -> 2==subjectInfo.getType()).collect(Collectors.toList());
			parmter.put("type", 2);
			preffix = "L_";
		}
		if("NWL".equals(subject0)){
			subjectList = subjectInfoList;
			parmter.put("type", 0);
		}
		if(subjectList == null|| subjectList.isEmpty())return;
//生成标题 和对应的映射
		List<String> title=new ArrayList<String>();
		title.add("学校、区县");
		subjectList.forEach(subjectInfo -> {
			String subjectName = subjectInfo.getSubjectName();
			fieldMap.put(subjectName+"均分", subjectName+"_SCORE");
			fieldMap.put(subjectName+"名次", subjectName+"_RANK");
			title.add(subjectName+"均分");
			title.add(subjectName+"名次");
		});
		fieldMap.put("总分均分", "SUM_SCORE");
		fieldMap.put("总分名次", "SUM_RANK");
		title.add("总分均分");
		title.add("总分名次");
//查询数据集
		parmter.put("subjectList", subjectList);
		List<Map<String,Object>> beanList = new ArrayList<Map<String,Object>>();
		if ("school".equals(level)){
			List<Map<String,Object>> classesList = rptExpSubjectMapper.qryClassSubjectScore2(parmter);
			List<Map<String,Object>> classesAllscore = rptExpAllscoreMapper.qryClassAllScoreInfo(parmter);
			if (classesAllscore.isEmpty()||classesAllscore.isEmpty()) return;
			Map<String,Map<String,Object>> clsAllMap = zyj.report.common.CalToolUtil.trans(classesAllscore, new String[]{"CLS_ID"});
			for(Map cls : classesList){
				String clsid = cls.get("CLS_ID").toString();
				Map clstotal = (Map)clsAllMap.get(clsid);
				cls.put("SCH_NAME",cls.get("CLS_NAME"));
				cls.put("SUM_SCORE", clstotal.get("AVG_SCORE"));
				cls.put("SUM_RANK", clstotal.get( "RANK"));
			}
			beanList.addAll(classesList);
		}

		List<Map<String,Object>> schoolList = rptExpSubjectMapper.qrySchoolSubjectScore2(parmter);
		List<Map<String,Object>> areaList = rptExpSubjectMapper.qryAreaSubjectScore2(parmter);
		List<Map<String,Object>> schoolAllscore = rptExpAllscoreMapper.qrySchoolAllScoreInfo2(parmter);
		List<Map<String,Object>> areaAllscore = rptExpAllscoreMapper.qryAreaAllScoreInfo2(parmter);
		Map citySubject=(Map) rptExpSubjectMapper.qryCitySubjectScore2(parmter).get(0);
		if (schoolAllscore.isEmpty() || schoolList.isEmpty() || areaAllscore.isEmpty() || areaList.isEmpty())
			return;

		Map<String,Map<String,Object>> schAllMap = zyj.report.common.CalToolUtil.trans(schoolAllscore, new String[]{"SCH_ID"});
		Map<String,Map<String,Object>> areaAllMap = zyj.report.common.CalToolUtil.trans(areaAllscore, new String[]{"AREA_ID"});

		for(Map sch : schoolList){
			String schid = sch.get("SCH_ID").toString();
			Map schtotal = (Map)schAllMap.get(schid);
			sch.put("SUM_SCORE", schtotal.get(preffix+"AVG_SCORE"));
			sch.put("SUM_RANK", schtotal.get(preffix+"AVG_SCORE_RANK"));
		}
		for(Map area : areaList){
			String schid = area.get("AREA_ID").toString();
			Map areatotal = (Map)areaAllMap.get(area.get("AREA_ID"));
			area.put("SUM_SCORE", areatotal.get(preffix+"AVG_SCORE"));
			area.put("SUM_RANK", areatotal.get(preffix+"AVG_SCORE_RANK"));
		}

		beanList.addAll(getBeanList(schoolList, areaList));
		//添加全体
		citySubject.put("SCH_NAME", "全体");
		citySubject.put("SUM_SCORE", ((Map) rptExpAllscoreMapper
				.qryCityAllScoreInfo2(parmter).get(0)).get(preffix
				+ "AVG_SCORE"));
		beanList.add(citySubject);

		String[] titleArr = title.toArray(new String[title.size()]);
		String[][] conList = map2objects(fieldMap, titleArr, beanList);
		// String titelName = (String)
		// jyjRptExtMapper.qryExambatch(exambatchId).get("NAME")+"_"+subject0;
		int[][] avgmerge = {};
		String[][] titelList = { titleArr };

		ExportUtil.createExpExcel(
				titelList,
				conList,
				avgmerge,
				"平均分对比"
						+ ((("WK".equals(subject0) || "LK".equals(subject0)) ? "_" + subjects.get(subject0) + ".xls" : ".xls")), "平均分对比",
				null, pathFile);

	}
}
