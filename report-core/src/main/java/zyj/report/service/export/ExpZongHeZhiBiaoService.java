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
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;

@Service
public class ExpZongHeZhiBiaoService extends BaseRptService {

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
		fieldMap.put("总人数", "SHOULD_EXAM_STU_NUM");
		fieldMap.put("参加人数", "TAKE_EXAM_NUM");
		fieldMap.put("平均分", "AVG_SCORE");
		fieldMap.put("排名", "RANK");
		fieldMap.put("最高分", "TOP_SCORE");
		fieldMap.put("最低分", "UP_SCORE");
		fieldMap.put("满分率", "FULL_RANK");
		fieldMap.put("优秀率", "LEVEL_GD_RATE");
		fieldMap.put("良好率", "LEVEL_FN_RATE");
		fieldMap.put("及格率", "LEVEL_PS_RATE");
		fieldMap.put("低分率", "LOW_RATE");
		fieldMap.put("超均率", "OVER_AVG_RATE");
		fieldMap.put("比均率", "RATIO_AVG_RATE");
		fieldMap.put("标准差", "STANDARD_DEV");
		fieldMap.put("难度", "DIFFICULT");
		fieldMap.put("区分度", "DISCRIMINATION");
		fieldMap.put("贡献值", "CONTRIBUTION");
		fieldMap.put("综合水平", "COMPOSITE_RATE");
		fieldMap.put("众数", "MODELS");
	}
	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {
		boolean isDistinctWL = true;
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String subject = (String) parmter.get("subject");
		String paperId = parmter.get("paperId").toString();
		String path = (String) parmter.get("pathFile");
		int stuType = (Integer)parmter.get("stuType");
		//校验参数,
		if(exambatchId == null ||cityCode == null ||subject == null)
				return;

		List<Map<String, Object>> beanList =  new ArrayList< Map<String, Object>>();
		//求贡献值的R  贡献值公式:   C= Z - R (校内平均分)
		List<Map<String,Object>> schoolList = null;
		List<Map<String,Object>> areaSubjectInfo = null;
		//得到本考次指定科目全市内的所有学校
		if(!subject.equals("ZF")){
			String	title = "学校、区县,总人数,参加人数,平均分,排名,最高分,最低分,满分率,优秀率,良好率,及格率,低分率,超均率,比均率,标准差,难度,区分度,综合水平,众数";
//由于单科出成绩的时候不能涉及别的科目，所以先干掉“贡献值”指标			
//			conditions.put("destination", "AVG_SCORE");//由于贡献度的目标分数值为可变，暂定为市科目的平均分
//			conditions.put("subjectList", CalToolUtil.getRedundantSubject());
//			double r = Double.parseDouble(jyjRptExtMapper.qryCitySubjectContribute(conditions).get("CONTRIBUTION_R").toString());
			schoolList = rptExpSubjectMapper.qrySchoolSubjectInfo(parmter);
			areaSubjectInfo = rptExpSubjectMapper.qryAreaSubjectInfo(parmter);
			if (schoolList.isEmpty() || areaSubjectInfo.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");
			beanList = getBeanList(schoolList, areaSubjectInfo);
/*			for(Map bean : beanList){
				double z  =Double.parseDouble(bean.get("AVG_SCORE").toString());
				bean.put("CONTRIBUTION",CalToolUtil.decimalFormat2(z - r) );
			}*/
			//添加全体
			Map cityInfo = (Map)rptExpSubjectMapper.qrySubjectQualityInfo(parmter).get(0);
			cityInfo.put("SCH_NAME", "全体");
			beanList.add(cityInfo);
			createXLS(beanList,"",exambatchId,path,title);
		}
		else{
			//总分的总人数以语文的总人数为准，文科总人数以文科数学人数为准，理科同理。
			String	title = "学校、区县,总人数,参加人数,平均分,排名,最高分,最低分,满分率,优秀率,良好率,及格率,低分率,超均率,比均率,标准差";
			schoolList = rptExpAllscoreMapper.qrySchoolAllScoreInfo(parmter);
			areaSubjectInfo = rptExpAllscoreMapper.qryAreaAllScoreInfo(parmter);

			//分文理时的人数
			parmter.put("GroupBy", "SCH_ID,CLS_TYPE");
			Map<String,Map<String, Object>>schPersonNum_wl = CalToolUtil.trans(rptExpAllscoreMapper.qryAllscoreStuNum(parmter), new String[]{"SCH_ID", "CLS_TYPE"});
			parmter.put("GroupBy", "AREA_ID,CLS_TYPE");
			Map<String,Map<String, Object>>areaPersonNum_wl = CalToolUtil.trans(rptExpAllscoreMapper.qryAllscoreStuNum(parmter), new String[]{"AERA_ID", "CLS_TYPE"});
			parmter.put("GroupBy", "CITY_ID,CLS_TYPE");
			Map<String,Map<String, Object>>cityPersonNum_wl = CalToolUtil.trans(rptExpAllscoreMapper.qryAllscoreStuNum(parmter), new String[]{ "CLS_TYPE"});
			//不分文理时的人数
			parmter.put("GroupBy", "SCH_ID");
			Map<String,Map<String, Object>>schPersonNum_mwl = CalToolUtil.trans(rptExpAllscoreMapper.qryAllscoreStuNum(parmter), new String[]{"SCH_ID"});
			parmter.put("GroupBy", "AREA_ID");
			Map<String,Map<String, Object>>areaPersonNum_mwl = CalToolUtil.trans(rptExpAllscoreMapper.qryAllscoreStuNum(parmter), new String[]{"AREA_ID"});
			parmter.put("GroupBy", "CITY_ID");
			Map<String, Object>cityPersonNum_mwl =(Map<String, Object>) rptExpAllscoreMapper.qryAllscoreStuNum(parmter).get(0);

			List<Map<String,Object>> schools_w = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> schools_l = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> allschools = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> area_w = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> area_l = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> allarea = new ArrayList<Map<String,Object>>();
			for(Map sch : schoolList){
				if(sch.get("TYPE").toString().equals("3")||sch.get("TYPE").toString().equals("0")){
					sch.put("SHOULD_EXAM_STU_NUM",schPersonNum_mwl.get(sch.get("SCH_ID")).get("SHOULD_EXAM_STU_NUM") );
					allschools.add(sch);
				}
				else if (sch.get("TYPE").toString().equals("1") && isDistinctWL){
					sch.put("SHOULD_EXAM_STU_NUM",schPersonNum_wl.get(sch.get("SCH_ID")+"1").get("SHOULD_EXAM_STU_NUM") );						
					schools_w.add(sch);
				}
				else if (sch.get("TYPE").toString().equals("2") && isDistinctWL){
					sch.put("SHOULD_EXAM_STU_NUM",schPersonNum_wl.get(sch.get("SCH_ID")+"2").get("SHOULD_EXAM_STU_NUM") );						
					schools_l.add(sch);
				}
			}
			
			for(Map area : areaSubjectInfo){
				area.put("SCH_NAME", area.get("AREA_NAME"));
				if(area.get("TYPE").toString().equals("0")||area.get("TYPE").toString().equals("3")){
					area.put("SHOULD_EXAM_STU_NUM",areaPersonNum_mwl.get(area.get("AREA_ID")).get("SHOULD_EXAM_STU_NUM") );
					allarea.add(area);
				}
				else if (area.get("TYPE").toString().equals("1")){
					area.put("SHOULD_EXAM_STU_NUM",areaPersonNum_wl.get(area.get("AREA_ID")+"1").get("SHOULD_EXAM_STU_NUM") );					
					area_w.add(area);
				}
				else if (area.get("TYPE").toString().equals("2")){
					area.put("SHOULD_EXAM_STU_NUM",areaPersonNum_wl.get(area.get("AREA_ID")+"2").get("SHOULD_EXAM_STU_NUM") );
					area_l.add(area);
					
				}
			}
			//添加全体
			Map<String, Object> l = null;
			Map<String, Object> w = null;
			Map<String, Object> all = null;
			List<Map<String, Object>> schAllSco = rptExpAllscoreMapper.qryCityAllScoreInfo(parmter);
			for(Map<String, Object> schAllS : schAllSco){
				if(schAllS.get("TYPE").toString().equals("1")){
					schAllS.put("SCH_NAME", "全体");
					w = new HashMap(schAllS);
					w.putAll(cityPersonNum_wl.get("1"));
				}
				if(schAllS.get("TYPE").toString().equals("2")){
					schAllS.put("SCH_NAME", "全体");
					l =  new HashMap(schAllS);
					l.putAll(cityPersonNum_wl.get("2"));
				}
				if(schAllS.get("TYPE").toString().equals("3")||schAllS.get("TYPE").toString().equals("0")){
					schAllS.put("SCH_NAME", "全体");
					all =  new HashMap(schAllS);
					all.putAll(cityPersonNum_mwl);
				}
			}			
			if(allschools.size()!=0&&allarea.size()!=0){
				beanList = getBeanList(allschools, allarea);
				beanList.add(all);
				createXLS(beanList,"",exambatchId,path,title);
			}
			if(schools_l.size()!=0&&area_l.size()!=0){
				beanList = getBeanList(schools_l, area_l);
				beanList.add(l);
				createXLS(beanList,"_理科",exambatchId,path,title);
			}
			if(schools_w.size()!=0&&area_w.size()!=0){
				beanList = getBeanList(schools_w, area_w);
				beanList.add(w);				
				createXLS(beanList,"_文科",exambatchId,path,title);
			}
		}
	}
	
	
	private Map<String,Map> trans(List<Map<String, Map>> mapList,String key){
		if(mapList == null)
			return null;
		else{
			Map<String,Map> mapMap = new HashMap<String, Map>();
			for(Map map : mapList){
				mapMap.put((String)map.get(key), map);
			}
			return mapMap;
		}
	}
	
	private void createXLS(List<Map<String, Object>> beanList,String suffix,String exambatchId,String path,String title)throws Exception {
//			String	title = "学校、区县,总人数,参加人数,平均分,排名,最高分,最低分,满分率,优秀率,良好率,及格率,低分率,超均率,比均率,标准差,难度,区分度,贡献值,综合水平,众数";
			
			String[] titleArr = title.split(",");
			String [][]conList = map2objects(fieldMap, titleArr, beanList);
//			String titelName = (String) jyjRptExtMapper.qryExambatch(exambatchId).get("NAME");
			int [][] avgmerge = {};
			String[][] titelList = {titleArr};
			ExportUtil.createExpExcel(titelList, conList, avgmerge, "综合指标" + suffix + ".xls", "综合指标" + suffix, null, path);
	}


}
