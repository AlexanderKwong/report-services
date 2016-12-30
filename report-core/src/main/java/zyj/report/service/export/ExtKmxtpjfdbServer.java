package zyj.report.service.export;

import java.util.ArrayList;
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
import zyj.report.service.BaseDataService;

@Service
public class ExtKmxtpjfdbServer extends BaseRptService {
	
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;
	@Autowired
	BaseDataService baseDataService;

	public void exportData(Map<String,Object> parmter) throws Exception {
		String exambatchId = parmter.get("exambatchId").toString();
		String subject = parmter.get("subject").toString();
		String pathFile = parmter.get("pathFile").toString();
		String level = parmter.get("level").toString();

// 获取学校地区名
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
//		Map<String,Map<String,Object>> areaNameMap = getAreaCache();
//		Map<String,Map<String,Object>> clsNameMap = getClassCache();
		Map<String,Map<String,Object>> schNameMap = CalToolUtil.trans(baseDataService.getSchools(exambatchId), new String[]{"SCH_ID"});
		Map<String,Map<String,Object>> areaNameMap = CalToolUtil.trans(baseDataService.getAreas(exambatchId), new String[]{"AREA_ID"});
		Map<String,Map<String,Object>> clsNameMap = CalToolUtil.trans(baseDataService.getClasses(exambatchId), new String[]{"CLS_ID"});
		Map<String, Object> util = new HashMap<String, Object>();
		util.put("SCH_NAME", schNameMap);
		util.put("AREA_NAME", areaNameMap);
		util.put("CLS_NAME", clsNameMap);

		//报表标题行
		List<Map<String,Object>> question = null;
		List<Map<String,Object>> orderitem = rptExpQuestionMapper.findAllQuestionOrderItem(parmter);
		if (orderitem.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");
		String[] titleList = new String[2*orderitem.size()+1];
		String[] itemList = new String[orderitem.size()];
		titleList[0] = "学校、区县";
		for(int i=0,j=1,size=orderitem.size();i<size;i++){
			Map<String,Object> m =  orderitem.get(i);
			titleList[j++] = DataCacheUtil.getQuestionTypeName(ObjectUtils.toString(m.get("QST_TIPY")))+ObjectUtils.toString(m.get("QUESTION_NO"));
			titleList[j++] = DataCacheUtil.getQuestionTypeName(ObjectUtils.toString(m.get("QST_TIPY")))+ObjectUtils.toString(m.get("QUESTION_NO")) + "%";
			itemList[i] = ObjectUtils.toString(m.get("QUESTION_ORDER"));
		}

		//获取报表内容
		List<List<Object>> beanList = new LinkedList<List<Object>>();
		if ("school".equals(level)){//查多一次班级的
			parmter.put("level","classes");
			question = rptExpQuestionMapper.findRptExpQuestion(parmter);
			List<List<Object>> clsList = getQuestionRow(question, itemList,"CLS_ID","CLS_NAME",util);
			beanList.addAll(clsList);
		}
		//获得学校数据
		parmter.put("level","school");
		question = rptExpQuestionMapper.findRptExpQuestion(parmter);
		List<List<Object>> schList = getQuestionRow(question, itemList,"SCH_ID","SCH_NAME",util);
		//获得镇区数据
		parmter.put("level", "area");
		question = rptExpQuestionMapper.findRptExpQuestion(parmter);
		List<List<Object>> areaList = getQuestionRow(question, itemList,"AREA_ID","AREA_NAME",util);
		//获取全市数据
		parmter.put("level", "city");
		question = rptExpQuestionMapper.findRptExpQuestion(parmter);
		List<List<Object>> cityList = getQuestionRow(question, itemList,"CITY_CODE","CITY",util);

		//转换全区
		Map<String,List<Object>> areaMap = trans2(areaList, 0);


		//在每个镇区的学校末加上一行统计镇区数据
		String areaId_buf = null;
		String areaId_cur = null;
		for(List sch : schList){
			String name =sch.get(0).toString();
			areaId_cur = name.substring(0, name.indexOf("_"));
			if(areaId_cur == null ){
				if(areaId_buf == null){
					beanList.add(sch);		
				}
				else{
					List area = new ArrayList<Object>();
					Map areaInfo = areaNameMap.get(areaId_buf);
					if(areaInfo!=null){
						beanList.add(areaMap.get(areaId_buf+"_"+areaInfo.get("AREA_NAME")));
					}else{
						beanList.add(areaMap.get("市直"));
					}
					beanList.add(sch);
					areaId_buf = areaId_cur;
				}
			}
			else if(areaId_cur.equals(areaId_buf)){
				beanList.add(sch);				
			}
			else if (areaId_buf == null){
				beanList.add(sch);				
				areaId_buf = areaId_cur;
			}
			else {
				List area = new ArrayList<Object>();
				Map areaInfo = areaNameMap.get(areaId_buf);
				if(areaInfo!=null){
					beanList.add(areaMap.get(areaId_buf+"_"+areaInfo.get("AREA_NAME")));
				}else{
					beanList.add(areaMap.get("市直"));
				}
				beanList.add(sch);
				areaId_buf = areaId_cur;
			}
		}
		//加上最后一个地区
		if(areaId_cur !=null )
			if(areaId_cur.equals(areaId_buf)){
				List area = new ArrayList<Object>();
				Map areaInfo = areaNameMap.get(areaId_buf);
				if(areaInfo!=null){
					beanList.add(areaMap.get(areaId_buf+"_"+areaInfo.get("AREA_NAME")));
				}else{
					beanList.add(areaMap.get("市直"));
				}
			}
		//去除学校名前的“地区名_”
		for(List<Object> one : beanList){
			String name = one.get(0).toString();
			int index = name.indexOf("_");
			if(index != -1){
				name = name.replace(name.subSequence(0, index+1), "");
				one.set(0, name);
			}
		}
		//加上全体
		beanList.addAll(cityList);
		ExportUtil.createExpExcel(titleList, beanList, pathFile+"科目小题平均分对比.xls");
	}

	private Map<String,List<Object>> trans2(List<List<Object>> list ,int key){
		Map<String,List<Object>> map = new HashMap<String, List<Object>>();
		if(list!=null){
			for(List<Object>l : list){
				String k =l.get(key).toString();
				if(!map.containsKey(k))
					map.put(k, l);
			}
		}
		return map;
	}
	
	private List<List<Object>> getQuestionRow(List<Map<String,Object>>question,String[] itemList,String KEY,String name,Map<String,Object>util ) throws ReportExportException {

		if (question.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");
		//报表输出数据
		List<List<Object>> conList = new LinkedList<List<Object>>();
		//转换数据程序
		Map<String,Map<String,Object>> questiontrans = CalToolUtil.trans(question, new String[]{KEY,"QUESTION_ORDER"});
			
		
		Set<String> k = new HashSet<String>();
		for(Map<String,Object> m : question){
			String id = m.get(KEY).toString();
			if(!k.contains(id)){
				k.add(id);
				List<Object> resdatalist= new LinkedList<Object>();
				try{
					resdatalist.add(m.get("AREA_ID").toString()+"_"+((Map<String,Map<String,Map>>)util.get(name)).get(id).get(name));
				}catch(NullPointerException e){
					if(name.equals("CITY"))
						resdatalist.add("全体");
					else if(name.equals("AREA_NAME"))
						resdatalist.add("市直");
					else 
						resdatalist.add("");
				}
				for(int i = 0;i<itemList.length;i++){
					String questionOrder = itemList[i];
					String key =   id  + questionOrder;
					Map<String,Object> d = questiontrans.get(key);
					try{
					resdatalist.add(d.get("AVG_SCORE"));
					resdatalist.add(CalToolUtil.decimalFormat2(Double.parseDouble(d.get("SCORE_RANK").toString())));
					}catch(Exception e){
						System.out.println("Warn : 该区域没有找到该题信息，应该是选做题");
						resdatalist.add("");
						resdatalist.add("");
					}
				}
				conList.add(resdatalist);
			}
		}
		CalToolUtil.sortByIndexValue2(conList, 0);
		return conList;
	}
}
