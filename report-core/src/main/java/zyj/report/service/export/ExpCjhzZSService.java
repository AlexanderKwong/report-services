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
import zyj.report.common.CalToolUtil;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service     
public class ExpCjhzZSService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	@Autowired
	BaseDataService baseDataService;

	private final String myKey = "ChengJiHuiZong";//与学科分组分析共享key ，缓存内容相同

	/**
	 * level :市区 1，镇区 2
	 * 镇区报表必须要传areaId
	 */
	@Override
	public void exportData(Map<String, Object> parmter) throws Exception  {
	
		String level = (String) parmter.get("level");
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String path = (String) parmter.get("pathFile");
		int stuType = (Integer)parmter.get("stuType");
		
		boolean isDistinctWL = true;
		List<String> title_w ;
		List<String> title_l ;
		List<String> title ;
		//校验参数,暂不校验cityCode	
		if(exambatchId == null  ||path == null ||level == null )
			return;
		
		// 映射查询结果与表标题
				Map<String, String> fieldMap ;
				fieldMap = new HashMap<String, String>();
				fieldMap.put("考号", "SEQUENCE");
//				fieldMap.put("学号", "SEQUENCE0");
				fieldMap.put("姓名", "NAME");
				fieldMap.put("学校", "SCH_NAME");
				fieldMap.put("班级", "CLS_NAME");
				fieldMap.put("区镇", "AREA_NAME");
				fieldMap.put("总分", "ALL_SCORE");

//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
//		Map<String,Map<String,Object>> clsNameMap = getClassCache();
//
//		List<Map<String,Object>> subjects_cur = getSubjectCache();

		List<Map<String,Object>> subjects_cur = baseDataService.getSubjectByExamid(exambatchId);
		title_w = new ArrayList<String>();
		title_l = new ArrayList<String>();
		title = new ArrayList<String>();
		addFinalField(title_l,level);
		addFinalField(title_w,level);
		addFinalField(title,level);
//产生查询条件List
		List<SubjectInfo> subjectList = subjects_cur.stream()
				.map(subject -> new SubjectInfo(subject.get("PAPER_ID").toString(), subject.get("SUBJECT").toString(), subject.get("SUBJECT_NAME").toString(),(Integer)subject.get("TYPE")))
				.sorted((subject1,subject2)->{
					return CalToolUtil.indexOf(CalToolUtil.getSubjectOrder(),subject1.getSubject())- CalToolUtil.indexOf(CalToolUtil.getSubjectOrder(),subject2.getSubject());
				})
				.collect(Collectors.toList());
//设置标题 和 对应的映射
		subjectList.forEach(subjectInfo -> {
			String subject = subjectInfo.getSubject();
			String subjectName = subjectInfo.getSubjectName();
			if (1 == subjectInfo.getType()) {
				title_w.add(subjectName );
			}
			if (2 == subjectInfo.getType()) {
				title_l.add(subjectName );
			}
			title.add(subjectName );
			fieldMap.put(subjectName , subjectName + "_SCORE");
		});

		addFinalField2(title);
		addFinalField2(title_l);
		addFinalField2(title_w);
		
		Map conditions = new HashMap<String, Object>();
		List<Map<String, Object>> beanList = null;
		
		// 查区内的学生列表
		conditions.put("exambatchId", exambatchId);
		conditions.put("cityCode", cityCode);
		conditions.put("subjectList", subjectList);
		conditions.put("stuType", stuType);
		List wStuList = new ArrayList<Map>();//文科学生
		List lStuList = new ArrayList<Map>();//理科学生
		String areaId = null;
		String schoolId = null;
		String classesId = null;
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
		if (beanList.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");;

		for(Map bean : beanList){
			/*if(level.equals("classes")){//添加班级名
				bean.put("CLSNAME", names.get("CLSNAME"));
			}	*/
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
				CalToolUtil.sortByIndexValue(lStuList, new String[]{"AREANAME","SCHNAME","CLSNAME","SEQUENCE"});
				createXLS(fieldMap,lStuList, "_理科", title_l, exambatchId, path, "成绩汇总");
			}
			if(wStuList.size()!=0){
				CalToolUtil.sortByIndexValue(wStuList, new String[]{"AREANAME","SCHNAME","CLSNAME","SEQUENCE"});
				createXLS(fieldMap,wStuList, "_文科", title_w, exambatchId, path, "成绩汇总");
			}
		} else {
//			try{
				CalToolUtil.sortByIndexValue(beanList, new String[]{"AREANAME","SCHNAME","CLSNAME","SEQUENCE"});
//			}catch(Exception e){
//				
//			}
			createXLS(fieldMap,beanList, "", title, exambatchId, path, "成绩汇总");
		}

	}
	
	private void createXLS(Map<String,String>fieldMap, List<Map<String, Object>> beanList,String suffix,List<String> title ,String exambatchId,String path,String excelName)throws Exception {
			String[] titleArr = title.toArray(new String[title.size()]);
			String [][]conList = map2objects(fieldMap, titleArr, beanList);
//			String titelName = (String) statsRptExtMapper.qryExambatch(exambatchId).get("NAME");
			int [][] avgmerge = {};
			String[][] titelList = {titleArr};
			ExportUtil.createExpExcel(titelList, conList, avgmerge, excelName + suffix + ".xls", excelName + suffix, "成绩汇总", path);
	}
	private void addFinalField(List<String> title,String level){
		if(level.equals("city") ){
			title.add("区镇");
		}
		title.add("学校");
		title.add("班级");
		title.add("姓名");
//		title.add("学号");
		title.add("考号");

	}
	private void addFinalField2(List<String> title){
		title.add("总分");
	}
public static void main(String[] args) {
	List<Map<String,Object>> a = new ArrayList<Map<String,Object>>();
	Map<String,Object> aa = new HashMap<String, Object>();
	Map<String,Object> bb = new HashMap<String, Object>();
	Map<String,Object> cc = new HashMap<String, Object>();
	Map<String,Object> dd = new HashMap<String, Object>();
	aa.put("AREANAME", "小榄镇");
	aa.put("SCHNAME", "小榄镇菊城中学");
	aa.put("CLSNAME", "初三(3)班");
	bb.put("AREANAME", "小榄镇");
	bb.put("SCHNAME", "小榄镇菊城中学");
	bb.put("CLSNAME", "初三(5)班");
	cc.put("AREANAME", "小榄镇");
	cc.put("SCHNAME", "小榄镇菊城中学");
	cc.put("CLSNAME", "初三(4)班");
	dd.put("AREANAME", "小榄镇");
	dd.put("SCHNAME", "小榄镇菊城中学");
	dd.put("CLSNAME", "初三(8)班");
	a.add(aa);a.add(dd);a.add(bb);a.add(cc);
	CalToolUtil.sortByIndexValue(a, new String[]{"AREANAME","SCHNAME","CLSNAME"});
	System.out.println(a);
}
}
