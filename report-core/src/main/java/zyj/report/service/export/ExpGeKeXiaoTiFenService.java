package zyj.report.service.export;

import java.util.ArrayList;
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
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpGeKeXiaoTiFenService extends BaseRptService {
	
	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;
	@Autowired
	BaseDataService baseDataService;

	private final String myKey = "GeKeXiaoTiFen";

	private final String SEPERATOR = "///";

/**
 * 必传参数有 level，path，paperId
 * 生成市报表只需传上面的必要参数
 * 生成镇区、学校、班级请分别传areaId、schoolId、classesId
 * @throws Exception 
 */
	@Override
	public void exportData(Map<String, Object> params) throws Exception {

			String level =(String)params.get("level") ;
			String exambatchId =(String) params.get("exambatchId");
			String cityCode =(String) params.get("cityCode");
			String path = (String) params.get("pathFile");
			String subject = (String) params.get("subject");
			String paperId = (String) params.get("paperId");
			int stuType = (Integer)params.get("stuType");
			//校验参数,暂不校验cityCode	
			if(subject == null  ||path == null ||level == null )
				return;
			// 映射查询结果与表标题
			Map<String, String> fieldMap ;

			fieldMap = new HashMap<String, String>();
			fieldMap.put("考号","SEQUENCE");
			fieldMap.put("姓名","NAME");
			fieldMap.put("学校","SCHNAME");
			fieldMap.put("区县","AREANAME");
			fieldMap.put("排名","RANK");
			fieldMap.put("全卷","TOTAL");
			fieldMap.put("客观题","OBJECTIVE");
			fieldMap.put("非客观题","SUBJECTIVE");
			fieldMap.put("班级","CLSNAME");

			List<Map> questions = rptExpQuestionMapper.qryClassQuestionScore6(params);
			//标题
			String title  ="考号"+SEPERATOR+"姓名"+SEPERATOR+"班级"+SEPERATOR+"学校"+SEPERATOR+"区县"+SEPERATOR+"全卷"+SEPERATOR+"客观题"+SEPERATOR+"非客观题"+SEPERATOR+"";

			List<Integer> orderList = new ArrayList<Integer>();
			StringBuffer sb = new StringBuffer();
			for (Map map : questions) {
//				System.out.println(map);
				String no = (String)map.get("QUESTION_NO");
				orderList.add(Integer.parseInt(map.get("QUESTION_ORDER").toString()));
				int type =Integer.parseInt( map.get("QST_TIPY").toString());
				if( type == 1){
					sb.append("单选");
					sb.append(no);
					sb.append(SEPERATOR);
					fieldMap.put("单选"+no, "Q"+map.get("QUESTION_ORDER").toString());
				}else if(type ==2){
					sb.append("多选");
					sb.append(no);
					sb.append(SEPERATOR);
					fieldMap.put("多选"+no, "Q"+map.get("QUESTION_ORDER").toString());
				}else if(type==3){
					sb.append("判断");
					sb.append(no);
					sb.append(SEPERATOR);
					fieldMap.put("判断"+no, "Q"+map.get("QUESTION_ORDER").toString());
				}else if(type==4){
					//有待修改，此处应该增加主观题的得分情况
					sb.append(no);
					sb.append(SEPERATOR);
					fieldMap.put(no, "Q"+map.get("QUESTION_ORDER").toString());
				}
			}
			if(sb.length()>1)
				sb.replace(sb.length()-SEPERATOR.length(),sb.length() , "");
			title = title+sb.toString();
//			System.out.println(title);
			params.put("orderList", orderList);
			List<Map<String,Object>> beanList = null ;
			String areaId = null;
			String schoolId = null;
			String classesId = null;
			String clsName = null;
			String schName = null;
			String areaName = null;
			Map name ;

			switch (level) {
				case "city":
					//查市内的学生列表
					beanList = baseDataService.getStudentQuestion(exambatchId, null , level, stuType, paperId, subject);
					break;
				case "area":
					//查区内的学生列表
					areaId = (String) params.get("areaId");
					if (areaId == null)
						return;
					beanList = baseDataService.getStudentQuestion(exambatchId, areaId , level, stuType, paperId, subject);
					CalToolUtil.sortByIndexValue(beanList, "SEQUENCE");
					break;
				case "school":
					//查校内的学生列表
					schoolId = (String) params.get("schoolId");
					if (schoolId == null)
						return;
					beanList = baseDataService.getStudentQuestion(exambatchId, schoolId , level, stuType, paperId, subject);
					CalToolUtil.sortByIndexValue(beanList, "SEQUENCE");
					break;
				case "classes":
					//查年级的学生列表
					classesId = (String) params.get("classesId");
					if (classesId == null)
						return;
					beanList = baseDataService.getStudentQuestion(exambatchId, classesId , level, stuType, paperId, subject);
					CalToolUtil.sortByIndexValue(beanList, "SEQUENCE");
					break;
			}
		String[] titleArr = title.split(SEPERATOR);
		String[][] conList = map2objects(fieldMap, titleArr, beanList);
		int[][] avgmerge = {};
		String[][] titelList = {titleArr};
		ExportUtil.createExpExcel(titelList, conList, avgmerge, "小题分.xls", "小题分", null, path);


	}

	
}
