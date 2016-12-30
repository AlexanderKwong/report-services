package zyj.report.service.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;

@Service
public class ExpJiBenZhiBiaoService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	// 映射查询结果与表标题
	final private static  Map<String, String> fieldMap ;
	static{
		fieldMap = new HashMap<String, String>();
		fieldMap.put("学校、区县", "SCH_NAME");
		fieldMap.put("人数", "TAKE_EXAM_NUM");
		fieldMap.put("客观题卷平均分", "OBJ_AVG_SCORE");
		fieldMap.put("非客观题卷平均分", "SUB_AVG_SCORE");
		fieldMap.put("全卷平均分", "AVG_SCORE");
		fieldMap.put("客观题卷标准差", "OBJ_SD");
		fieldMap.put("非客观题卷标准差", "SUB_SD");
		fieldMap.put("全卷标准差", "STU_SCORE_SD");
		fieldMap.put("全体人数", "PERSON_NUM");
		fieldMap.put("全体平均分", "AVG_SCORE");
		fieldMap.put("全体标准差", "STU_SCORE_SD");
		fieldMap.put("文科人数", "W_PERSON_NUM");
		fieldMap.put("文科平均分", "W_AVG_SCORE");
		fieldMap.put("文科标准差", "W_STU_SCORE_SD");
		fieldMap.put("理科人数", "L_PERSON_NUM");
		fieldMap.put("理科平均分", "L_AVG_SCORE");
		fieldMap.put("理科标准差", "L_STU_SCORE_SD");
		fieldMap.put("全体人数 ", "WL_PERSON_NUM");
		fieldMap.put("全体平均分 ", "WL_AVG_SCORE");
		fieldMap.put("全体标准差 ", "WL_STU_SCORE_SD");
	}
	
	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String path = (String) parmter.get("pathFile");
		String subject = (String) parmter.get("subject");
		String paperId = (String) parmter.get("paperId");
		int stuType = (Integer)parmter.get("stuType");
//校验参数,
		String title =null;
		if(exambatchId == null ||cityCode == null ||subject == null )
			return;
		if(subject.equals("ZF"))
			title ="学校、区县,全体人数 ,全体平均分 ,全体标准差 ,文科人数,文科平均分,文科标准差,理科人数,理科平均分,理科标准差";
		else
			title ="学校、区县,人数,客观题卷平均分,非客观题卷平均分,全卷平均分,客观题卷标准差,非客观题卷标准差,全卷标准差";


		List<Map<String, Object>> beanList = new ArrayList< Map<String, Object>>();
		List<Map<String,Object>> schoolList = null;
		List<Map<String,Object>> areaSubjectInfo = null;
		if(!subject.equals("ZF")){
			schoolList = rptExpSubjectMapper.qrySchoolSubjectInfo2(parmter);
			areaSubjectInfo = rptExpSubjectMapper.qryAreaSubjectInfo2(parmter);
			if (schoolList.isEmpty() || areaSubjectInfo.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");
			beanList = getBeanList(schoolList, areaSubjectInfo);
			//添加全体
			Map cityInfo = (Map)rptExpSubjectMapper.qrySubjectQualityInfo2(parmter).get(0);
			cityInfo.put("SCH_NAME", "全体");
			beanList.add(cityInfo);
		}
		else{
			schoolList = rptExpAllscoreMapper.qrySchoolAllScoreInfo2(parmter);
			areaSubjectInfo = rptExpAllscoreMapper.qryAreaAllScoreInfo2(parmter);
			if (schoolList.isEmpty() || areaSubjectInfo.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");
			beanList = getBeanList(schoolList, areaSubjectInfo);
			//添加全体
			Map cityInfo = (Map)rptExpAllscoreMapper.qryCityAllScoreInfo2(parmter).get(0);
			cityInfo.put("SCH_NAME", "全体");
			beanList.add(cityInfo);
			if(cityInfo.get("WL_PERSON_NUM").toString().equals("0")&&!cityInfo.get("PERSON_NUM").toString().equals("0"))//不分文理
				title ="学校、区县,全体人数,全体平均分,全体标准差";
			if(cityInfo.get("WL_PERSON_NUM").toString().equals("0")&&cityInfo.get("PERSON_NUM").toString().equals("0"))//文理科总分分数不等
				title ="学校、区县,文科人数,文科平均分,文科标准差,理科人数,理科平均分,理科标准差";
		}

			String[] titleArr = title.split(",");
			String [][]conList = map2objects(fieldMap, titleArr, beanList);
//			String titelName = (String) jyjRptExtMapper.qryExambatch(exambatchId).get("NAME")+"_"+subjects.get(subject);
			int [][] avgmerge = {};
			String[][] titelList = {titleArr};
			ExportUtil.createExpExcel(titelList, conList, avgmerge, "基本指标.xls", "基本指标", null, path);

	}
	
}
