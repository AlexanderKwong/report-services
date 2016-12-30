package zyj.report.service.export;

import java.util.ArrayList;
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
public class ExtXtzwqkfxServer extends BaseRptService {
	
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;
	@Autowired
	BaseDataService baseDataService;

	public void exportData(Map<String,Object> parmter) throws Exception {
		String exambatchId = parmter.get("exambatchId").toString();
		String subject = parmter.get("subject").toString();
		String pathFile = parmter.get("pathFile").toString();
		//读取源数据
		List<Map<String,Object>> question = rptExpQuestionMapper.findRptExpSchoolQuestion(parmter);
		List<Map<String,Object>> orderitem = rptExpQuestionMapper.findAllQuestionOrderItem(parmter);
		if (question.isEmpty() || orderitem.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

		Map<String,Map<String,Object>> areaNameMap =  CalToolUtil.trans(baseDataService.getAreas(exambatchId), new String[]{"AREA_ID"});
		//转换数据程序
		Map<String,Map<String,Object>> questiontrans = CalToolUtil.trans(question, new String[]{"EXAMBATCH_ID", "SCH_ID", "SUBJECT", "QUESTION_ORDER"});
		
		//报表标题行
		String[] titleList = new String[4*orderitem.size()+1];
		String[] itemList = new String[orderitem.size()];
		titleList[0] = "学校、区县";
		for(int i=0,j=1,size=orderitem.size();i<size;i++){
			Map<String,Object> m =  orderitem.get(i);
			titleList[j++] = DataCacheUtil.getQuestionTypeName(ObjectUtils.toString(m.get("QST_TIPY")))+ObjectUtils.toString(m.get("QUESTION_NO")) + "优";
			titleList[j++] = DataCacheUtil.getQuestionTypeName(ObjectUtils.toString(m.get("QST_TIPY")))+ObjectUtils.toString(m.get("QUESTION_NO")) + "良";
			titleList[j++] = DataCacheUtil.getQuestionTypeName(ObjectUtils.toString(m.get("QST_TIPY")))+ObjectUtils.toString(m.get("QUESTION_NO")) + "中";
			titleList[j++] = DataCacheUtil.getQuestionTypeName(ObjectUtils.toString(m.get("QST_TIPY")))+ObjectUtils.toString(m.get("QUESTION_NO")) + "差";
			itemList[i] = ObjectUtils.toString(m.get("QUESTION_ORDER"));
		}
		
		//报表输出数据
		List<List<Object>> conList = new LinkedList<List<Object>>();
		
		Set<String> k = new HashSet<String>();
		for(Map<String,Object> m : question){
			String schid = m.get("SCH_ID").toString();
			if(!k.contains(schid)){
				k.add(schid);
				List<Object> resdatalist= new LinkedList<Object>();
				resdatalist.add(m.get("AREA_ID")+"_"+m.get("SCH_NAME"));
				for(int i = 0;i<itemList.length;i++){
					String questionOrder = itemList[i];
					String key = exambatchId + schid + subject + questionOrder;
					Map<String,Object> d = questiontrans.get(key);
					try{
					resdatalist.add(d.get("EXCE_NUM"));
					resdatalist.add(d.get("MIDD_NUM"));
					resdatalist.add(d.get("PASS_NUM"));
					resdatalist.add(d.get("UNPASS_NUM"));
					}catch(Exception e){
						System.out.println("Warn : 该区域没有找到该题信息，应该是选做题");
						resdatalist.add("");
						resdatalist.add("");
						resdatalist.add("");
						resdatalist.add("");
					}
				}
				conList.add(resdatalist);
			}
		}
		CalToolUtil.sortByIndexValue2(conList, 0);
		//全体数据
		List<Object> zongTi =  new ArrayList<Object>();
		zongTi.add("全体");
		for(int j =1;j<conList.get(0).size();j++){
			Map statistic = CalToolUtil.maxIndexOfList(conList, j);//将除了镇区内所有学校的每一列求和
			zongTi.add( statistic.get("sum"));
		}
		//在每个镇区的学校末加上一行统计镇区数据
		List<List<Object>> beanList = new LinkedList<List<Object>>();
		String areaId_buf = null;
		String areaId_cur = null;
		for(List sch : conList){
			String name =sch.get(0).toString();
			areaId_cur = name.substring(0, name.indexOf("_"));
			if(areaId_cur == null ){
				if(areaId_buf == null){
					beanList.add(sch);		
				}
				else{
//					Map area = areaMap.get(areaId_buf);
					List area = new ArrayList<Object>();
					Map areaInfo = areaNameMap.get(areaId_buf);
					area.add(areaInfo==null?areaId_buf+"_"+"市直":areaId_buf+"_"+areaInfo.get("AREA_NAME"));
					beanList.add(area);
					sumSchAsArea(beanList);
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
//				Map area = areaMap.get(areaId_buf);
				List area = new ArrayList<Object>();
				Map areaInfo = areaNameMap.get(areaId_buf);
				area.add(areaInfo==null?areaId_buf+"_"+"市直":areaId_buf+"_"+areaInfo.get("AREA_NAME"));
				beanList.add(area);
				sumSchAsArea(beanList);
				beanList.add(sch);
				areaId_buf = areaId_cur;
			}
		}
		//加上最后一个地区
		if(areaId_cur !=null )
			if(areaId_cur.equals(areaId_buf)){
//				Map area = areaMap.get(areaId_buf);
				List area = new ArrayList<Object>();
				Map areaInfo = areaNameMap.get(areaId_buf);
				area.add(areaInfo==null?areaId_buf+"_"+"市直":areaId_buf+"_"+areaInfo.get("AREA_NAME"));
				beanList.add(area);
				sumSchAsArea(beanList);
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
		beanList.add(zongTi);
		ExportUtil.createExpExcel(titleList, beanList, pathFile+"小题掌握情况分析.xls");
	}
	
	private String getValue(JSONObject optJson,String key){
		if(optJson.has(key)){
			return optJson.getString(key);
		}
		return "";
	}

	private void sumSchAsArea(List<List<Object>> beanList){
		int size = beanList.size();
		String tmp = beanList.get(size-1).get(0).toString();
		String areaId = tmp.substring(0, tmp.indexOf("_"));
		int rownum = 0;
		for(int i = size-1; i >=0;i--){
			String tmp1 = beanList.get(i).get(0).toString();
			if(!tmp1.startsWith(areaId)){
				rownum = i+1;
				break;
			}
		}
		for(int j =1;j<beanList.get(0).size();j++){
			Map statistic = CalToolUtil.maxIndexOfList(beanList.subList(rownum, size-1), j);//将除了镇区内所有学校的每一列求和
			beanList.get(size-1).add( statistic.get("sum"));
		}
	}
}
