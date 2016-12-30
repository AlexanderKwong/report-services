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
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;

@Service
public class ExpChengJiTongJiService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;

	String excelName = "成绩统计.xls";
	String sheetName = "成绩统计";
	
	final private double step = 5; //步长
	
	@SuppressWarnings("unchecked")
	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {
		
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String path = (String) parmter.get("pathFile");
		String subject = (String) parmter.get("subject");
		int stuType = (Integer)parmter.get("stuType");
//校验参数,
		if(exambatchId == null ||cityCode == null ||subject == null)
			return;
		// 映射查询结果与表标题
				Map<String, String> fieldMap;

				fieldMap = new HashMap<String, String>();
				fieldMap.put("学校", "SCHNAME");
				fieldMap.put("实考", "TAKE_EXAM_NUM");
				fieldMap.put("缺考", "ABSENT_EXAM_STU_NUM");
				fieldMap.put("均分", "AVG_SCORE");
				fieldMap.put("离均差", "SCORE_AVG_DEV");
				fieldMap.put("标准差", "STU_SCORE_SD");
				fieldMap.put("最高分", "TOP_SCORE");
				fieldMap.put("最低分", "UP_SCORE");
				fieldMap.put("TOP80人数", "TOP80");
				fieldMap.put("TOP80百分", "TOP%80");
				fieldMap.put("TOP70人数", "TOP70");
				fieldMap.put("TOP70百分", "TOP%70");
				fieldMap.put("TOP60人数", "TOP60");
				fieldMap.put("TOP60百分", "TOP%60");
				fieldMap.put("LS40人数", "LS40");
				fieldMap.put("LS40百分", "LS%40");
		// 获取学校地区名		
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
		
		//查全市的科目的综合指标
		List<Map<String,Object>> cityZH = rptExpSubjectMapper.qrySubjectQualityInfo2(parmter);
		//查学校的科目的综合指标
		List<Map<String,Object>> schZH = rptExpSubjectMapper.qrySchoolSubjectInfo2(parmter);
		//查全市的科目缺考人数
		List<Map<String,Object>> cityStuNum = rptExpSubjectMapper.qrySubjectStuNum(parmter);
		//查学校的科目缺考人数
		parmter.put("GroupBy","sch_id");
		List<Map<String,Object>> schStuNum = rptExpSubjectMapper.qrySubjectStuNum(parmter);
		//获取总分
		double total = Double.parseDouble(cityZH.get(0).get("TOP_SCORE").toString());
		double full = Double.parseDouble(schZH.get(0).get("FULL_SCORE").toString());
		double avg = Double.parseDouble(cityZH.get(0).get("AVG_SCORE").toString());
		int top80line = (int) (full * 0.8);
		int top70line = (int) (full * 0.7);
		int top60line = (int) (full * 0.6);
		int top40line = (int) (full * 0.4);

		//产生分数段列表
		List scoreList = new ArrayList<Integer>();
		parmter.put("top80", (int)top80line);
		parmter.put("top70", (int)top70line);
		parmter.put("top60", (int)top60line);
		parmter.put("ls40", (int)top40line);
		double lower = total-(((int)total)%step==0?step:((int)total)%step);
		while((int)lower>0){
			scoreList.add((int)lower);
			lower=lower-step;
		}
		parmter.put("scoreList", scoreList);
		for(Object score : scoreList){
			fieldMap.put(">="+score.toString(), "HE"+score.toString());
		}
		fieldMap.put(">=0", "HE0");
		//查学校的各个分数段人数
		List<Map<String,Object>> schFSD = rptExpSubjectMapper.qryScorePersonNumBySchoolSubject(parmter);
		//查全市的各个分数段人数
		Map<String,Object> cityFSD = rptExpSubjectMapper.qryScorePersonNumByCitySubject(parmter);

		if (cityZH.isEmpty() || schZH.isEmpty() || schFSD.isEmpty() || cityFSD.isEmpty() ) throw new ReportExportException("没有查到源数据，请核查！");
		//构造数据字典
		Map<String, Map<String, Object>> schMap = CalToolUtil.trans(schZH, new String[]{"SCH_ID"});
		Map<String, Map<String, Object>> schStuNumMap = CalToolUtil.trans(schStuNum, new String[]{"SCH_ID"});

		//组合
		for(Map sch : schFSD ){
			Map schZHinfo = schMap.get(sch.get("SCH_ID"));
			Map schStuNumInfo = schStuNumMap.get(sch.get("SCH_ID"));
			int num = Integer.parseInt(schZHinfo.get("TAKE_EXAM_NUM").toString());
			sch.put("HE0",num );
			int ls40 =  Integer.parseInt(sch.get("LS40").toString());
			int he60 =  Integer.parseInt(sch.get("TOP60").toString());
			int he70 =  Integer.parseInt(sch.get("TOP70").toString());
			int he80 =  Integer.parseInt(sch.get("TOP80").toString());
			sch.put("LS%40",  CalToolUtil.decimalFormat2((0.0+ls40)*100/num));
			sch.put("TOP%60",  CalToolUtil.decimalFormat2((0.0 + he60) * 100 /num));
			sch.put("TOP%70",  CalToolUtil.decimalFormat2((0.0 + he70)*100 /num));
			sch.put("TOP%80", CalToolUtil.decimalFormat2((0.0 + he80) * 100 / num));
			sch.putAll(schZHinfo);
			sch.putAll(schStuNumInfo);
//			sch.put("SCHNAME", schNameMap.get(sch.get("SCH_ID")).get("SCHNAME"));
//			sch.put("SCHNAME", baseDataService.getSchool(exambatchId, sch.get("SCH_ID").toString()).get("SCHNAME"));
			//离均差
			double schAvg = Double.parseDouble(sch.get("AVG_SCORE").toString());
			sch.put("SCORE_AVG_DEV", CalToolUtil.decimalFormat2(schAvg-avg));
		}
		Map cityZHinfo = cityZH.get(0);
		Map cityStuNumInfo = cityStuNum.get(0);
		cityFSD.putAll(cityZHinfo);
		cityFSD.putAll(cityStuNumInfo);
		cityFSD.put("SCHNAME", "全市");
		int num = Integer.parseInt(cityZHinfo.get("TAKE_EXAM_NUM").toString());
		cityFSD.put("HE0", num);
		int ls40 =  Integer.parseInt(cityFSD.get("LS40").toString());
		int he60 =  Integer.parseInt(cityFSD.get("TOP60").toString());
		int he70 =  Integer.parseInt(cityFSD.get("TOP70").toString());
		int he80 =  Integer.parseInt(cityFSD.get("TOP80").toString());
		cityFSD.put("LS%40",  CalToolUtil.decimalFormat2((0.0 + ls40) * 100 /num));
		cityFSD.put("TOP%60",  CalToolUtil.decimalFormat2((0.0+he60)*100/num));
		cityFSD.put("TOP%70",  CalToolUtil.decimalFormat2((0.0+he70)*100/num));
		cityFSD.put("TOP%80",  CalToolUtil.decimalFormat2((0.0 + he80) *100/num));
		schFSD.add(cityFSD);
		CalToolUtil.replace0(schFSD, "HE");
		
		/////////////////
			String[] title1 = {"学校","实考","缺考","均分","离均差","标准差","最高分","最低分",top80line+"以上","",top70line+"以上","",top60line+"以上","","低于"+top40line,"","分数分段（下确界）  人数分布（累计表）"};
			String[] temp=	 {"","","","","","","","","人数","%","人数","%","人数","%","人数","%"};
			List t2 = new ArrayList(Arrays.asList(temp));
//			t2.addAll(scoreList);
			for(Object i : scoreList){
				t2.add(">="+i);
			}
			t2.add(">=0");
			String [] title2  = new String[t2.size()];
			for(int i = 0 ;i<t2.size();i++){
				title2[i] = t2.get(i).toString();
			}
			String [] temp2 = {"学校","实考","缺考","均分","离均差","标准差","最高分","最低分","TOP80人数","TOP80百分","TOP70人数","TOP70百分","TOP60人数","TOP60百分","LS40人数","LS40百分"};
			t2 = new ArrayList(Arrays.asList(temp2));
			for(Object i : scoreList){
				t2.add(">="+i);
			}
			t2.add(">=0");
			String [] titleArr  = new String[t2.size()];
			for(int i = 0 ;i<t2.size();i++){
				titleArr[i] = t2.get(i).toString();
			}
			String [][]conList = map2objects(fieldMap, titleArr, schFSD);
//			String titelName = (String) jyjRptExtMapper.qryExambatch(exambatchId).get("NAME")+"_"+subjects.get(subject);
			int [][] avgmerge = {{0,0,0,1},{1,0,1,1},{2,0,2,1},{3,0,3,1},{4,0,4,1},{5,0,5,1},{6,0,6,1},{7,0,7,1},{8,0,9,0},{10,0,11,0},{12,0,13,0},{14,0,15,0},{16,0,(scoreList.size()+16),0}};
			String[][] titelList = {title1,title2};
			ExportUtil.createExpExcel(titelList, conList, avgmerge, excelName, sheetName, null, path);


	}
	
	
}
