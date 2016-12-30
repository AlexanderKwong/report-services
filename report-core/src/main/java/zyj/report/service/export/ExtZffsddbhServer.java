package zyj.report.service.export;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.RptExpStudetScoreMapper;

@Service
public class ExtZffsddbhServer extends BaseRptService {
	
	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;
	@Value("${zf.score.step}")
	int step;
	@Value("${zf.score.top}")
	int top;
	@Value("${zf.score.bottom}")
	int bottom;

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
		if(data.size()==0)return;
		
		String[] titleList = new String[(top-bottom)/step +3];
		Map<String ,Integer> titleMap = new HashMap<String ,Integer>();
		titleList[0] = "学校、区县";
		for(int i = top,j=1;i>=bottom-step;i-=step,j++){
			titleList[j] = getFsdKey(i);
			titleMap.put(getFsdKey(i), j);
		}
		
		//数据列
		List<List<Object>> conList = new LinkedList<List<Object>>();
		Map<String, List<Object>> conListMap = new HashMap<String, List<Object>>();//建立行数据的索引
		
		for(Map<String,Object> m : data){
//			String schid = m.get("SCH_ID").toString();
//			Map tmp = schNameMap.get(schid);
			String schName = m.get("SCH_NAME").toString();
			String areaName = m.get("AREA_NAME").toString();
			//每一个学校只输出一行数据
			if(!conListMap.containsKey(areaName+"_"+schName)){
				//行数据初始化
				List<Object> resdatalist= new LinkedList<Object>();
				resdatalist.add(areaName+"_"+schName);//TODO 从缓存读取学校名字
				for(String title : titleList){
					resdatalist.add(0);
				}
				conList.add(resdatalist);
				conListMap.put(areaName+"_"+schName, resdatalist);
			}
			//每一地区只输出一行数据
			if(!conListMap.containsKey(areaName)){
				//行数据初始化
				List<Object> resdatalist= new LinkedList<Object>();
				resdatalist.add(areaName);//TODO 从缓存读取学校名字
				for(String title : titleList){
					resdatalist.add(0);
				}
				conList.add(resdatalist);
				conListMap.put(areaName, resdatalist);
			}
			//得到分数段计算入累计人数, fsd:分数段
			double score = Double.parseDouble(m.get("ALL_TOTAL").toString());
			String fsdkey = getFsdKey((int) score);
			//更新学校
			List<Object> row = conListMap.get(areaName+"_"+schName);
			int totalnum = (Integer) row.get(titleMap.get(fsdkey));
			row.remove((int)titleMap.get(fsdkey));
			row.add(titleMap.get(fsdkey), totalnum+1);
			//更新地区
			List<Object> row1 = conListMap.get(areaName);
			int totalnum1 = (Integer) row1.get(titleMap.get(fsdkey));
			row1.remove((int)titleMap.get(fsdkey));
			row1.add(titleMap.get(fsdkey), totalnum1+1);
		}
		//按地区名排序
		zyj.report.common.CalToolUtil.sortByIndexValue2(conList, 0);
		//去除学校名前的“地区名_”
		for(List<Object> one : conList){
			String name = one.get(0).toString();
			int index = name.indexOf("_");
			if(index != -1){
				name = name.replace(name.subSequence(0, index+1), "");
				one.set(0, name);
			}
		}
		ExportUtil.createExpExcel(titleList, conList, pathFile + "总分分数段对比（横）" + (StringUtils.isBlank(subjects.get(subject)) ? "" : "_" + subjects.get(subject)) + ".xls");
		
	}
	
	private String getFsdKey(int score){
		if(score >= top){
			return ">="+top;
		}
		if(score < bottom){
			return "<"+bottom;
		}
		int i = score/step;
		return "["+(i*step)+ "," + (i+1)*step+")";
	}

}
