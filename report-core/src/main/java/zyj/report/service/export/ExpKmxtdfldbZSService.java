package zyj.report.service.export;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.CalToolUtil;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpKmxtdfldbZSService extends BaseRptService {
	
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	BaseDataService baseDataService;
	
	public void exportData(Map<String,Object> parmter) throws Exception {
		String exambatchId = parmter.get("exambatchId").toString();
		String subject = parmter.get("subject").toString();
		String pathFile = parmter.get("pathFile").toString();
		String level = parmter.get("level").toString();

		// 获取学校地区名		
		Map<String,Map<String,Object>> schNameMap = CalToolUtil.trans(baseDataService.getSchools(exambatchId),new String[]{"SCH_ID"});
		Map<String,Map<String,Object>> areaNameMap = CalToolUtil.trans(baseDataService.getAreas(exambatchId), new String[]{"AREA_ID"});
		Map<String,Map<String,Object>> clsNameMap = CalToolUtil.trans(baseDataService.getClasses(exambatchId), new String[]{"CLS_ID"});
		Map<String, Object> util = new HashMap<String, Object>();
		util.put("SCHNAME", schNameMap);
		util.put("AREANAME", areaNameMap);
		util.put("CLSNAME", clsNameMap);
		
		List<Map<String,Object>> orderitem = rptExpQuestionMapper.findAllQuestionOrderItem(parmter);
		//报表标题行
		String[] titleList1 = new String[2*orderitem.size()+1+6];
		String[] titleList2 = new String[2*orderitem.size()+1+6];
		String[] itemList = new String[orderitem.size()];
		titleList1[0] = (level.equals("city")||level.equals("area"))?"学校":"班级";titleList2[0] = "";
		int j=1;
		for(int i=0,size=orderitem.size();i<size;i++){
			Map<String,Object> m =  orderitem.get(i);
			titleList1[j] = "第"+ObjectUtils.toString(m.get("QUESTION_NO"))+"题";
			titleList2[j] = "满分";
			j++;
			titleList1[j] = "";
			titleList2[j] = "得分率";
			j++;
			itemList[i] = ObjectUtils.toString(m.get("QUESTION_ORDER"));
		}
		for(String s : new String[]{"客观题","主观题","全卷"}){
			titleList1[j] = s;
			titleList2[j] = "满分";
			j++;
			titleList1[j] = "";
			titleList2[j] = "得分率";
			j++;
		}
		
		//报表输出数据
		List<List<Object>> conList = new LinkedList<List<Object>>();
		List<List<Object>> temp = null;
		//每题信息
		List<Map<String,Object>> question = null;
		//每个学科（所有题汇总），主客观情况
		List<Map<String,Object>> sub = null;
		//当前科目，主客观情况
		Map<String,Object> subCur = null;
		subCur =(Map) rptExpSubjectMapper.qryRptSubjectSubObJ(parmter).get(0);
		if (orderitem.isEmpty() ||subCur.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

		switch(level){
		case "classes"://班
			parmter.put("schoolId", clsNameMap.get(parmter.get("classesId")).get("SCH_ID"));
		case "school"://校
			parmter.put("level", "classes");
			parmter.put("areaId",schNameMap.get(parmter.get("schoolId")).get("AREA_ID"));
			question = rptExpQuestionMapper.findRptExpQuestion(parmter);
			sub = rptExpSubjectMapper.findRptExpSubject(parmter);
			conList.addAll(getQuestionDetail(question,sub,subCur,itemList,"CLS_ID","CLSNAME",util));
		case "area"://区
			parmter.put("level", "area");
			question = rptExpQuestionMapper.findRptExpQuestion(parmter);
			sub = rptExpSubjectMapper.findRptExpSubject(parmter);
			temp =getQuestionDetail(question,sub,subCur,itemList,"AREA_ID","AREANAME",util);
		case "city"://市
			parmter.put("level", "school");
			question = rptExpQuestionMapper.findRptExpQuestion(parmter);
			sub = rptExpSubjectMapper.findRptExpSubject(parmter);
			conList.addAll(getQuestionDetail(question,sub,subCur,itemList,"SCH_ID","SCHNAME",util));
			if(temp!=null)
				conList.addAll(temp);
			parmter.put("level", "city");
			sub = rptExpSubjectMapper.findRptExpSubject(parmter);
			conList.addAll(getQuestionDetail(orderitem,sub,subCur,itemList,"CITY_CODE","全市",util));
			break;
		}
		int [][] avgmerge = new int[orderitem.size()+4][4];
		avgmerge[0]=new int[]{0,1,0,2};
		for(int i = 1;i<=avgmerge.length-1;i++){
			avgmerge[i]=new int[]{i*2-1,1,i*2,1};
		}
			
		ExportUtil.createExpExcel(new String[][]{titleList1,titleList2}, conList, pathFile+"科目小题得分率对比.xls",avgmerge,parmter.get("subjectName")+"科目小题得分率对比");
	}
	



	/**
	 * 将所有小题合并一行，并在后面添加主客观题全卷的情况
	 * @param question 小题明细
	 * @param sub 小题汇总
	 * @param subAll 科目概况
	 * @param itemList 标题，题号
	 * @param KEY 对应level的id
	 * @param name 对应level的名称
	 * @return
	 */
	private List<List<Object>> getQuestionDetail(List<Map<String,Object>>question,List<Map<String,Object>>sub,Map<String,Object>subAll,String[] itemList,String KEY,String name,Map<String,Object>util){
		//转换数据程序
		Map<String,Map<String,Object>> questiontrans = CalToolUtil.trans(question, new String[]{KEY,"QUESTION_ORDER"});
		Map<String,Map<String,Object>> subtrans = CalToolUtil.trans(sub, new String[]{KEY});
		List<List<Object>> conList = new LinkedList<List<Object>>();
		
		double obj_total = Double.parseDouble(subAll.get("OBJ_TOTAL")==null?"0":subAll.get("OBJ_TOTAL").toString());
		double sub_total = Double.parseDouble(subAll.get("SUB_TOTAL")==null?"0":subAll.get("SUB_TOTAL").toString());
		int fullscore = Integer.parseInt(subAll.get("FULL_SCORE").toString());
		
		Set<String> k = new HashSet<String>();
		for(Map<String,Object> m : question){
			String id = m.get(KEY).toString();
			if(!k.contains(id)){
				k.add(id);
				List<Object> resdatalist= new LinkedList<Object>();
				try{
					resdatalist.add(((Map<String,Map<String,Map>>)util.get(name)).get(id).get(name));
				}catch(NullPointerException e){
					resdatalist.add("全市");
				}
				for(int i = 0;i<itemList.length;i++){
					String questionOrder = itemList[i];
					String key =   id  + questionOrder;
					Map<String,Object> d = questiontrans.get(key);
					try{
						resdatalist.add(d.get("QST_SCORE"));
						resdatalist.add(CalToolUtil.decimalFormat2(Double.parseDouble(d.get("SCORE_RANK").toString()))+"%");
					}catch(Exception e){
						System.out.println("Warn : 该区域没有找到该题信息，应该是选做题");
						resdatalist.add("");
						resdatalist.add("");
					}
				}
				double obj_avg = Double.parseDouble(subtrans.get(id).get("OBJ_AVG_SCORE").toString());
				double sub_avg = Double.parseDouble(subtrans.get(id).get("SUB_AVG_SCORE").toString());
				double all_avg = Double.parseDouble(subtrans.get(id).get("AVG_SCORE").toString());
				resdatalist.add(obj_total==0?"":obj_total);
				resdatalist.add(obj_total==0?"":CalToolUtil.decimalFormat2(obj_avg/obj_total*100)+"%");
				resdatalist.add(sub_total==0?"":sub_total);
				resdatalist.add(sub_total==0?"":CalToolUtil.decimalFormat2(sub_avg/sub_total*100)+"%");
				resdatalist.add(fullscore);
				resdatalist.add(CalToolUtil.decimalFormat2(all_avg/fullscore*100)+"%");
				conList.add(resdatalist);
			}
		}
		CalToolUtil.sortByIndexValue2(conList, 0);
		return conList;
	}
}
