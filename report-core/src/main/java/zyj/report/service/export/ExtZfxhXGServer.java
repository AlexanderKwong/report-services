package zyj.report.service.export;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.RptExpStudetScoreMapper;

@Service
public class ExtZfxhXGServer extends BaseRptService {
	
	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;

	private int fullScore ;
	private int lower ;//下界
	private int step =5;
	
	public void exportData(Map<String,Object> parmter) throws Exception{
		String exambatchId = parmter.get("exambatchId").toString();
		String subject = ObjectUtils.toString(parmter.get("subject"));
		String pathFile = parmter.get("pathFile").toString();
		
		// 获取学校地区名		
		
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
		if("WK".equals(subject)){
			parmter.put("flag", 1);
		}
		if("LK".equals(subject)){
			parmter.put("flag", 2);
		}
		if("NWL".equals(subject)){
			parmter.put("flag", 0);
		}
		//读取源数据
		List<Map<String,Object>> data = rptExpStudetScoreMapper.findRptExpStudetAllScore(parmter);
		List<Map<String,Object>> schooldata = rptExpStudetScoreMapper.findAllSchoolForRptExpStudetAllScore(parmter);
		
		String[] titleList = new String[2*schooldata.size()+3];
		titleList[0] =  "分数段";
		titleList[1] =  "总分人数";
		titleList[2] =  "总分累计";
		Map<String ,Integer> titleMap = new HashMap<String ,Integer>();
		//获取总分
		fullScore = Integer.parseInt(data.get(0).get("FULL_SCORE").toString());
		lower = fullScore-(fullScore%step==0?step:fullScore%step);
		for(int i = 3,size=titleList.length,j=0;i<size;i+=2,j++){
			Map<String,Object> m = schooldata.get(j);
//			String schid = m.get("SCH_ID").toString();
			String schName = m.get("SCH_NAME").toString();
			titleList[i] = schName+"人数";//TODO 从缓存读取学校名字
			titleList[i+1] = "累计";
			titleMap.put(schName+"RS", i);
			titleMap.put(schName+"LJ", i+1);
		}
		
		//数据列
		List<List<Object>> conList = new LinkedList<List<Object>>();
		Map<String, List<Object>> conListMap = new HashMap<String, List<Object>>();//建立行数据的索引
		for(int i = lower;i>=0;i-=step){
			String fsdkey= getFsdKey(i);
			List<Object> row = new LinkedList<Object>();
			row.add(fsdkey);
			for(int j= 1,size=titleList.length;j<size;j++){
				row.add(0);
			}
			conList.add(row);
			conListMap.put(fsdkey, row);
		}
		
		for(Map<String,Object> m : data){
			String schid = m.get("SCH_ID").toString();
			String schName = m.get("SCH_NAME").toString();
			//得到分数段计算入累计人数, fsd:分数段
			double score = Double.parseDouble(m.get("ALL_TOTAL").toString());
			String fsdkey = getFsdKey((int) score);
			List<Object> row = conListMap.get(fsdkey);
			int index = titleMap.get(schName+"RS");
			int totalnum = (Integer) row.get(index);
			row.remove(index);
			row.add(index, totalnum+1);
			row.remove(index+1);
			row.add(index+1, totalnum+1);
			totalnum = (Integer) row.get(1);
			row.remove(1);
			row.add(1,totalnum +1);
			row.remove(2);
			row.add(2,totalnum +1);
		}
		
		//计算累计数
		for(int i=1,size=conList.size();i<size;i++){
			List<Object> lastrow = conList.get(i-1);
			List<Object> row = conList.get(i);
			for(int j=2;j<titleList.length;j+=2){
				int total = (Integer)lastrow.get(j) + (Integer)row.get(j);
				row.remove(j);
				row.add(j,total);
			}
		}
		//移除全是0行
		zyj.report.common.CalToolUtil.removeAllZeroRow(conList, 1);
		//全局0置空
		zyj.report.common.CalToolUtil.replace0(conList, 1);
		ExportUtil.createExpExcel(titleList, conList, pathFile + "总分细化" + ("NWL".equals(subject) ? "" : "_" + subjects.get(subject)) + ".xls");
		
	}
	
	private String getFsdKey(int score){
		if(score >= lower){
			return ">="+lower;
		}
		int i = score/step;
		return (i*step)+ "-" + (i+1)*step;
	}

}
