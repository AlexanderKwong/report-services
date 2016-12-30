package zyj.report.service.export;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.RptExpStudetScoreMapper;

@Service
public class ExtZffsdServer extends BaseRptService {
	
	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;
	@Value("${zf.score.step}")
	int step;
	@Value("${zf.score.top}")
	int top;
	@Value("${zf.score.bottom}")
	int bottom;
	// 映射查询结果与表标题
	final private static  Map<String, String> fieldMap ;
	static{
		fieldMap = new HashMap<String, String>();
		fieldMap.put("分数段","FSD");
		fieldMap.put("人数","RS");
		fieldMap.put("比例","BL");
		fieldMap.put("累计人数","LJS");
		fieldMap.put("累计比例","LJBL");
		fieldMap.put("文科人数","WRS");
		fieldMap.put("文科比例","WBL");
		fieldMap.put("文科累计人数","WLJS");
		fieldMap.put("文科累计比例","WLJBL");
		fieldMap.put("理科人数","LRS");
		fieldMap.put("理科比例","LBL");
		fieldMap.put("理科累计人数","LLJS");
		fieldMap.put("理科累计比例","LLJBL");
	}
	
	
	
//	public List<Map<String, Object>> fetchExportData(Map<String,Object> parmter) {
	public void exportData(Map<String,Object> parmter) throws Exception{
		String exambatchId = parmter.get("exambatchId").toString();
		String cityCode = parmter.get("cityCode").toString();
		String path = (String) parmter.get("pathFile");
		//读取源数据
		List<Map<String,Object>> data = rptExpStudetScoreMapper.findRptExpStudetAllScore(parmter);
		//标题
		String title = "分数段,人数,比例,累计人数,累计比例,文科人数,文科比例,文科累计人数,文科累计比例,理科人数,理科比例,理科累计人数,理科累计比例";
		//报表输出数据
		List<Map<String,Object>> resdata = new LinkedList<Map<String,Object>>();
		
		if(data == null && data.size() == 0){
//			return resdata;
			return;
		}
		
		for(String t : new String[]{"总人数","最高分","最低分","平均分"}){
			Map<String,Object> row = new HashMap<String,Object>();
			row.put("FSD", t);
			resdata.add(row);
		}
		
		for(int i = top;i>=bottom-step;i-=step){
			Map<String,Object> row = new HashMap<String,Object>();
			row.put("FSD", getFsdKey(i));
			row.put("RS", 0);
			row.put("WRS", 0);
			row.put("LRS", 0);
			resdata.add(row);
		}
		
		DecimalFormat    dft   = new DecimalFormat("#0.00");
		NumberFormat fmt = NumberFormat.getPercentInstance();
		fmt.setMaximumFractionDigits(2);
		
		//转换数据程序
		Map<String,Map<String,Object>> d = CalToolUtil.trans(resdata, new String[]{"FSD"});
		double totalScore = 0;
		double wkTotalScore = 0;
		double lkTotalScore = 0;
		double total = 0;
		double wkTotal = 0;
		double lkTotal = 0;
		double maxTotalScore = 0;
		double minTotalScore = 999.0;
		double maxWkTotalScore = 0;
		double minWkTotalScore = 999.0;
		double maxLkTotalScore = 0;
		double minLkTotalScore = 999.0;
		for(Map<String,Object> m : data){
			double score = Double.parseDouble(m.get("ALL_TOTAL").toString());
			String flag = ObjectUtils.toString(m.get("TYPE"));
			String fsdkey = getFsdKey((int)score);
			if(!"3".equals(flag)){
				totalScore += score;
				total ++;
				maxTotalScore = (score > maxTotalScore)?score:maxTotalScore;
				minTotalScore = (score < minTotalScore)?score:minTotalScore;
				Map<String,Object> row = d.get(fsdkey);
				if(row == null){
					System.out.println(fsdkey);
				}
				row.put("RS",((Integer) row.get("RS"))+1);
			}
			if("1".equals(flag)){
				wkTotalScore += score;
				wkTotal ++;
				maxWkTotalScore = (score > maxWkTotalScore)?score:maxWkTotalScore;
				minWkTotalScore = (score < minWkTotalScore)?score:minWkTotalScore;
				Map<String,Object> row = d.get(fsdkey);
				row.put("WRS",((Integer) row.get("WRS"))+1);
			}
			if("2".equals(flag)){
				lkTotalScore += score;
				lkTotal ++;
				maxLkTotalScore = (score > maxLkTotalScore)?score:maxLkTotalScore;
				minLkTotalScore = (score < minLkTotalScore)?score:minLkTotalScore;
				Map<String,Object> row = d.get(fsdkey);
				row.put("LRS",((Integer) row.get("LRS"))+1);
			}
		}
		Map<String,Object> row = d.get("总人数");
		row.put("RS", total);
		row.put("WRS", wkTotal);
		row.put("LRS", lkTotal);
		row = d.get("最高分");
		row.put("RS", maxTotalScore);
		row.put("WRS", maxWkTotalScore);
		row.put("LRS", maxLkTotalScore);
		row = d.get("最低分");
		row.put("RS", minTotalScore);
		row.put("WRS", minWkTotalScore);
		row.put("LRS", minLkTotalScore);
		row = d.get("平均分");
		row.put("RS", dft.format(total!=0?(totalScore/total):0));
		row.put("WRS", dft.format(wkTotal!=0?(wkTotalScore/wkTotal):0));
		row.put("LRS", dft.format(lkTotal!=0?(lkTotalScore/lkTotal):0));
		
		//计算
		for(int i=4,dsize=resdata.size();i<dsize;i++){
			Map<String,Object> m  = resdata.get(i);
			m.put("BL", fmt.format(total!=0?((Integer)m.get("RS")/total):0));
			if(i == 4){
				m.put("LJS", (Integer)m.get("RS"));
			}else{
				m.put("LJS", (Integer)m.get("RS") + (Integer)resdata.get(i-1).get("LJS"));
			}
			m.put("LJBL", fmt.format(total!=0?((Integer)m.get("LJS")/total):0));
			//-----------------------
			m.put("WBL", fmt.format(wkTotal!=0?((Integer)m.get("WRS")/wkTotal):0));
			if(i == 4){
				m.put("WLJS", (Integer)m.get("WRS"));
			}else{
				m.put("WLJS", (Integer)m.get("WRS") + (Integer)resdata.get(i-1).get("WLJS"));
			}
			m.put("WLJBL", fmt.format(wkTotal!=0?((Integer)m.get("WLJS")/wkTotal):0));
			//-----------------------
			m.put("LBL", fmt.format(lkTotal!=0?((Integer)m.get("LRS")/lkTotal):0));
			if(i == 4){
				m.put("LLJS", (Integer)m.get("LRS"));
			}else{
				m.put("LLJS", (Integer)m.get("LRS") + (Integer)resdata.get(i-1).get("LLJS"));
			}
			m.put("LLJBL", fmt.format(lkTotal!=0?((Integer)m.get("LLJS")/lkTotal):0));
		}
		title = rmColumnsOfNoResult(resdata);
//		return resdata;
		
			String[] titleArr = title.split(",");
			String [][]conList = map2objects(fieldMap, titleArr, resdata);
			int [][] avgmerge = {};
			String[][] titelList = {titleArr};
			ExportUtil.createExpExcel(titelList, conList, avgmerge, "总分分数段.xls", "总分分数段", null, path);

	}
	
	private String getFsdKey(int score){
		if(score >= top	){
			return ">="+top;
		}
		if(score < bottom){
			return "<"+bottom;
		}
		int i = score/step;
		return "["+(i*step)+ "," + (i+1)*step+")";
	}
/*	public String getXlsFileName(){
		return "总分分数段";
	}*/
	
	//去除统计没有的结果
	private  String rmColumnsOfNoResult(List<Map<String,Object>> resdata){
		String title = "分数段,人数,比例,累计人数,累计比例,文科人数,文科比例,文科累计人数,文科累计比例,理科人数,理科比例,理科累计人数,理科累计比例";
		Map<String,Map<String,Object>> d = CalToolUtil.trans(resdata, new String[]{"FSD"});
		Map<String,Object> row = d.get("总人数");
		if(row.get("WRS").toString().equals("0.0")&&!row.get("LRS").toString().equals("0.0")){//没有人数
/*			for (Map<String, Object> map : resdata) {
				map.remove("WRS");
				map.remove("WBL");
				map.remove("WLJS");
				map.remove("WLJBL");
			}*/
			title = "分数段,人数,比例,累计人数,累计比例,理科人数,理科比例,理科累计人数,理科累计比例";
		}
		if(!row.get("WRS").toString().equals("0.0")&&row.get("LRS").toString().equals("0.0")){//没有人数
			/*for (Map<String, Object> map : resdata) {
				map.remove("LRS");
				map.remove("LBL");
				map.remove("LLJS");
				map.remove("LLJBL");
			}*/
			title = "分数段,人数,比例,累计人数,累计比例,文科人数,文科比例,文科累计人数,文科累计比例";
		}
		if(row.get("WRS").toString().equals("0.0")&&row.get("LRS").toString().equals("0.0")){//没有人数
			title = "分数段,人数,比例,累计人数,累计比例";
		}
		return title;
	}

}
