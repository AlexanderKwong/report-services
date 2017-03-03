package zyj.report.service.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.business.task.SubjectInfo;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpXueShengChengJiService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	@Autowired
	BaseDataService baseDataService;

	private final String myKey = "XueKeFenZuFenXi";//与学科分组分析共享key ，缓存内容相同


	/**
	 * level :市区 1，镇区 2
	 * 镇区报表必须要传areaId
	 */
	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {

		String level = (String) parmter.get("level");
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String path = (String) parmter.get("pathFile");
		int stuType = (Integer) parmter.get("stuType");

		boolean isDistinctWL = true;
		List<String> title_w;
		List<String> title_l;
		List<String> title;
		//校验参数,暂不校验cityCode
		if (exambatchId == null || path == null || level == null)
			return;

		// 映射查询结果与表标题
		Map<String, String> fieldMap;
		fieldMap = new HashMap<String, String>();
		fieldMap.put("考号", "SEQUENCE");
		fieldMap.put("姓名", "NAME");
		fieldMap.put("学校", "SCH_NAME");
		fieldMap.put("班级", "CLS_NAME");
		fieldMap.put("区县", "AREA_NAME");
		fieldMap.put("总分分数", "ALL_SCORE");
		fieldMap.put("总分级名", "ALL_RANK");
		fieldMap.put("总分班名", "ALL_RANK_CLS");
		fieldMap.put("总分校名", "ALL_RANK_SCH");

		// 获取学校地区名
//		Map<String, Map<String, Object>> schNameMap = getSchoolCache();
		Map max = new HashMap<String, Float>();
		//最低分
		Map min = new HashMap<String, Float>();
		//平均分
		Map avg = new HashMap<String, Float>();

//		List<Map<String, Object>> subjects_cur = getSubjectCache();
		List<Map<String, Object>> subjects_cur = baseDataService.getSubjectByExamid(exambatchId);

		title_w = new ArrayList<String>();
		title_l = new ArrayList<String>();
		title = new ArrayList<String>();
		addFinalField(title_l, level);
		addFinalField(title_w, level);
		addFinalField(title, level);
//产生查询条件List
		List<SubjectInfo> subjectList = subjects_cur.stream()
				.map(subject -> new SubjectInfo(subject.get("PAPER_ID").toString(), subject.get("SUBJECT").toString(), subject.get("SUBJECT_NAME").toString(),Integer.parseInt(subject.get("TYPE").toString())))
				.sorted((subject1, subject2) -> {
					return zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject1.getSubject()) - zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject2.getSubject());
				})
				.collect(Collectors.toList());
//设置标题 和 对应的映射
		subjectList.forEach(subjectInfo -> {
			String subject = subjectInfo.getSubject();
			String subjectName = subjectInfo.getSubjectName();
			if (subjectInfo.getType() == 1) {
				title_w.add(subjectName + "分数");
				title_w.add(subjectName + "班名");
				title_w.add(subjectName + "校名");
				title_w.add(subjectName + "级名");
			}
			if (subjectInfo.getType() == 2) {
				title_l.add(subjectName + "分数");
				title_l.add(subjectName + "班名");
				title_l.add(subjectName + "校名");
				title_l.add(subjectName + "级名");
			}
			title.add(subjectName + "分数");
			title.add(subjectName + "班名");
			title.add(subjectName + "校名");
			title.add(subjectName + "级名");
			fieldMap.put(subjectName + "分数", subjectName + "_SCORE");
			fieldMap.put(subjectName + "级名", subjectName + "_RANK");
			fieldMap.put(subjectName + "校名", subjectName + "_RANK_SCH");
			fieldMap.put(subjectName + "班名", subjectName + "_RANK_CLS");
		});
		addFinalField2(title);
		addFinalField2(title_l);
		addFinalField2(title_w);
//查询
		Map conditions = new HashMap<String, Object>();
		List<Map<String, Object>> beanList = null;

		// 查区内的学生列表
		conditions.put("exambatchId", exambatchId);
		conditions.put("cityCode", cityCode);
		conditions.put("subjectList", subjectList);
		conditions.put("stuType", stuType);
		List wStuList = new ArrayList<Map>();//文科学生
		List lStuList = new ArrayList<Map>();//理科学生
		List<Map<String, Object>> zongFen = null;
		Map<String, Map<String, Object>> d = null;
		Map<String, Map<String, Object>> d2 = null;
		String areaId = null;
		String schoolId = null;
		String classesId = null;
		String userId = null;
		Map names = null;
//	try{
		if (level.equals("city")) {
			beanList = baseDataService.getStudentSubjectsAndAllscore(exambatchId, null, level, stuType);
		} else if (level.equals("area")) {
			areaId = (String) parmter.get("areaId");
			if (areaId == null)
				return;
			conditions.put("areaId",areaId);
			beanList = baseDataService.getStudentSubjectsAndAllscore(exambatchId, areaId, level, stuType);
		} else if (level.equals("school")) {
			schoolId = parmter.get("schoolId").toString();
			if (schoolId == null)
				return;
			conditions.put("schoolId",schoolId);
			beanList = baseDataService.getStudentSubjectsAndAllscore(exambatchId, schoolId, level, stuType);
		} else if (level.equals("classes")) {
			classesId = parmter.get("classesId").toString();
			if (classesId == null)
				return;
			conditions.put("classesId",classesId);
			beanList = baseDataService.getStudentSubjectsAndAllscore(exambatchId, classesId, level, stuType);
		}
//		if(!level.equals("city") && beanList==null)
//	}catch(ReportExportException e)
		/*{
			beanList = rptExpSubjectMapper.qryStudentSubjectScore2(conditions);
			zongFen = rptExpAllscoreMapper.qryStudentSubjectAllScore(conditions);
			if (beanList.isEmpty() || zongFen.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

			d2= zyj.report.common.CalToolUtil.trans(zongFen, new String[]{"AREA_ID", "SCH_ID", "CLS_ID", "USER_ID"});
//			Map<String,Map<String,Object>> clsNameMap = getClassCache();
			for(Map bean : beanList){
				//添加总分
				userId =bean.get("USER_ID").toString();
				areaId =bean.get("AREA_ID").toString();
				schoolId =bean.get("SCH_ID").toString();
				classesId =bean.get("CLS_ID").toString();
				Map zongFenOfOne = d2.get(areaId+schoolId+classesId+userId);
				bean.put("ALL_SCORE", zongFenOfOne.get("ALL_TOTAL"));
				bean.put("ALL_RANK", zongFenOfOne.get("CITY_RANK"));
				bean.put("ALL_RANK_SCH", zongFenOfOne.get("GRD_RANK"));
				bean.put("ALL_RANK_CLS", zongFenOfOne.get("CLS_RANK"));
				//添加学校地区名
//				Map cls = clsNameMap.get(classesId);
//				bean.put("CLSNAME", cls.get("CLSNAME"));
//				bean.put("SCHNAME", cls.get("SCHNAME"));
//				bean.put("AREANAME", cls.get("AREANAME"));
			}
		}*/
		for(Map bean : beanList){
			int type = Integer.parseInt(bean.get("TYPE").toString());
			if(type == 0){
				//不分文理
				isDistinctWL = false;
			}else if(type == 1){
				//文科生
				wStuList.add(bean);
			}else if (type == 2){
				//理科生
				lStuList.add(bean);
			}
		}
//////////////////////////////////////////////////////导出“学生成绩”////////////////////////////////////////////////////////////
		if (isDistinctWL) {
			if(lStuList.size()!=0){
				zyj.report.common.CalToolUtil.sortByIndexValue2(lStuList, "ALL_RANK");
				createXLS(fieldMap,lStuList, "_理科", title_l, exambatchId, path, "学生成绩");
			}
			if(wStuList.size()!=0){
				zyj.report.common.CalToolUtil.sortByIndexValue2(wStuList, "ALL_RANK");
				createXLS(fieldMap,wStuList, "_文科", title_w, exambatchId, path, "学生成绩");
			}
		} else {
			zyj.report.common.CalToolUtil.sortByIndexValue2(beanList, "ALL_RANK");
			createXLS(fieldMap,beanList, "", title, exambatchId, path, "学生成绩");
		}
////////////////////////////////////////////////////////导出“学科分组分析”////////////////////////////////////////////////////////////
	if(level.equals("city")||level.equals("area")){
		Map citySubject =  null;
		Map cityAllS = null;
		if(level.equals("city")){
			citySubject =  (Map) rptExpSubjectMapper.qryCitySubjectScore2(conditions).get(0);
			cityAllS = (Map) rptExpAllscoreMapper.qryCityAllScoreInfo2(conditions).get(0);
		}else if(level.equals("area")){
			citySubject =  (Map) rptExpSubjectMapper.qryAreaSubjectScore2(conditions).get(0);
			cityAllS = (Map) rptExpAllscoreMapper.qryAreaAllScoreInfo2(conditions).get(0);
		}
		for(SubjectInfo subject : subjectList){
			max.put(subject.getSubjectName()+"_SCORE", citySubject.get(subject.getSubjectName()+"_TOP"));
			min.put(subject.getSubjectName()+"_SCORE", citySubject.get(subject.getSubjectName()+"_UP"));
			avg.put(subject.getSubjectName()+"_SCORE", citySubject.get(subject.getSubjectName()+"_SCORE"));
		}
		if(isDistinctWL){
		//最高分最低分平均分
			max.put("SEQUENCE","最高分");
			min.put("SEQUENCE","最低分");
			avg.put("SEQUENCE","平均分");
			if(lStuList.size()!=0){
				max.put("ALL_SCORE", cityAllS.get("L_TOP_SCORE"));
				min.put("ALL_SCORE", cityAllS.get("L_UP_SCORE"));
				avg.put("ALL_SCORE", cityAllS.get("L_AVG_SCORE"));
				zyj.report.common.CalToolUtil.sortByIndexValue(lStuList, "SCH_ID");
				zyj.report.common.CalToolUtil.sortByIndexValue(lStuList, "SEQUENCE");
				lStuList.add(max);
				lStuList.add(min);
				lStuList.add(avg);
				createXLS(fieldMap,lStuList, "_理科", title_l,exambatchId,path,"学科分组分析");
			}
			if(wStuList.size()!=0){
				max.put("ALL_SCORE", cityAllS.get("W_TOP_SCORE"));
				min.put("ALL_SCORE", cityAllS.get("W_UP_SCORE"));
				avg.put("ALL_SCORE", cityAllS.get("W_AVG_SCORE"));
				zyj.report.common.CalToolUtil.sortByIndexValue(wStuList, "SCH_ID");
				zyj.report.common.CalToolUtil.sortByIndexValue(wStuList, "SEQUENCE");
				wStuList.add(max);
				wStuList.add(min);
				wStuList.add(avg);
				createXLS(fieldMap,wStuList, "_文科", title_w,exambatchId,path,"学科分组分析");
			}
		}else{
			max.put("SEQUENCE","最高分");
			min.put("SEQUENCE","最低分");
			avg.put("SEQUENCE","平均分");
			max.put("ALL_SCORE", cityAllS.get("TOP_SCORE"));
			min.put("ALL_SCORE", cityAllS.get("UP_SCORE"));
			avg.put("ALL_SCORE", cityAllS.get("AVG_SCORE"));
			zyj.report.common.CalToolUtil.sortByIndexValue(beanList, "SCH_ID");
			zyj.report.common.CalToolUtil.sortByIndexValue(beanList, "SEQUENCE");
			beanList.add(max);
			beanList.add(min);
			beanList.add(avg);
			createXLS(fieldMap,beanList, "", title,exambatchId,path,"学科分组分析");
		}
	}
	}

	private void createXLS(Map<String,String>fieldMap, List<Map<String, Object>> beanList,String suffix,List<String> title ,String exambatchId,String path,String excelName)throws Exception {
			String[] titleArr = title.toArray(new String[title.size()]);
			String [][]conList = map2objects(fieldMap, titleArr, beanList);
//			String titelName = (String) statsRptExtMapper.qryExambatch(exambatchId).get("NAME");
			int [][] avgmerge = {};
			String[][] titelList = {titleArr};
			ExportUtil.createExpExcel(titelList, conList, avgmerge, excelName + suffix + ".xls", excelName + suffix, null, path);
	}
	private void addFinalField(List<String> title,String level){
		title.add("考号");
		title.add("姓名");
//		if(level != 4){//-
		title.add("班级");//+
		title.add("学校");
		title.add("区县");
//		}else{//-
//			title.add("班级");//-
//			title.add("学校");//-
//		}

	}
	private void addFinalField2(List<String> title){
		title.add("总分分数");
		title.add("总分班名");
		title.add("总分校名");
		title.add("总分级名");
	}

}
