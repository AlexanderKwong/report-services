package zyj.report.service.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import zyj.report.common.CalToolUtil;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpStudetScoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExtKmfsddbvServer extends BaseRptService {
	
	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;
	@Autowired
	BaseDataService baseDataService;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Value("${km.score.step}")
	int step;
	@Value("${km.score.top}")
	int top;
	@Value("${km.score.bottom}")
	int bottom;
	
	public void exportData(Map<String,Object> parmter) throws Exception{
		String exambatchId = parmter.get("exambatchId").toString();
		String subject = ObjectUtils.toString(parmter.get("subject"));
		String pathFile = parmter.get("pathFile").toString();
		String level = parmter.get("level").toString();
		// 获取学校地区名		
		
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
//		Map<String,Map<String,Object>> areaNameMap = getAreaCache();
		//读取源数据
		List<Map<String,Object>> data = rptExpStudetScoreMapper.findRptExpStudetScore(parmter);
		List<Map<String,Object>> schooldata = null;
		if(level.equals("city1")||level.equals("area"))
			schooldata = rptExpStudetScoreMapper.findAllSchoolForRptExpStudetScore(parmter);
		if(level.equals("city2"))
			schooldata = rptExpStudetScoreMapper.findAllAreaForRptExpStudetScore(parmter);
		if (data.isEmpty() || schooldata.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

		String[] titleList = new String[schooldata.size()+2];
		titleList[0] =  "分数段";
		titleList[titleList.length-1] = "全体";
		Map<String ,Integer> titleMap = new HashMap<String ,Integer>();
		String schid ="";
		String areaName="";
		String schName="";
		String title = "";
		for(int i = 1,size=titleList.length-1;i<size;i++){
			Map<String,Object> m = schooldata.get(i-1);
			if(level.equals("city1")||level.equals("area")){
				schid = m.get("SCH_ID").toString();

				areaName = m.get("AREA_NAME").toString();
				schName = m.get("SCH_NAME").toString();
				title = areaName+"_"+schName;
			}else if(level.equals("city2")){
				areaName = m.get("AREA_NAME").toString();
				title = areaName;
			}
			titleList[i] = title;//TODO 从缓存读取学校名字
			titleMap.put(title, i);
		}
		
		//数据列
		List<List<Object>> conList = new LinkedList<List<Object>>();
		Map<String, List<Object>> conListMap = new HashMap<String, List<Object>>();//建立行数据的索引
		for(int i = top;i>=bottom-step;i-=step){
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
			if(level.equals("city1")||level.equals("area")){
				schid = m.get("SCH_ID").toString();
//				Map tmp = schNameMap.get(schid);
				areaName = m.get("AREA_NAME").toString();
				schName = m.get("SCH_NAME").toString();
				title = areaName+"_"+schName;
			}else if(level.equals("city2")){
				areaName = m.get("AREA_NAME").toString();
				title = areaName;
			}
			//得到分数段计算入累计人数, fsd:分数段
			double score = Double.parseDouble(m.get("SIGN_TOTAL").toString());
			String fsdkey = getFsdKey((int) score);
			List<Object> row = conListMap.get(fsdkey);
			int totalnum = (Integer) row.get(titleMap.get(title));
			row.remove((int)titleMap.get(title));
			row.add(titleMap.get(title), totalnum+1);
		}
		
		//计算全体分数
		for(List<Object> row :  conList){
			int total = 0;
			for(int i = 1,size=row.size()-1;i<size;i++){
				total +=  (Integer)row.get(i);
			}
			row.add(row.size()-1, total);
		}
		//添加统计
		List<Map<String,Object>> statistic = null;
		Map<String,Map<String,Object>> dic = null;
		if(level.equals("city1")||level.equals("area")){
			statistic = rptExpSubjectMapper.qryRptExpSchoolSubject(parmter);
			for(Map s :  statistic){
				String schid1 = s.get("SCH_ID").toString();
//				Map schInfo = schNameMap.get(schid1);
//				s.put("AREANAME_SCHNAME", (schInfo.get("AREANAME")==null?"市直":schInfo.get("AREANAME").toString())+"_"+schInfo.get("SCHNAME").toString());
				Map schInfo = baseDataService.getSchool(exambatchId, schid1);
				s.put("AREANAME_SCHNAME", schInfo.get("AREA_NAME")+"_"+schInfo.get("SCH_NAME").toString());
			}
			dic = CalToolUtil.trans(statistic, new String[]{"AREANAME_SCHNAME"});
		}else if(level.equals("city2")){
			statistic = rptExpSubjectMapper.qryRptExpAreaSubject(parmter);
			for(Map s :  statistic){
//				String schid1 = s.get("AREA_ID").toString();
//				Map areaInfo = areaNameMap.get(schid1);
				s.put("AREANAME", s.get("AREA_NAME"));
			}
			dic = CalToolUtil.trans(statistic, new String[]{"AREANAME"});
		}

		List<Object> avg = new ArrayList<Object>();
		avg.add("平均分");
		List<Object> max = new ArrayList<Object>();
		max.add("最高分");
		List<Object> min = new ArrayList<Object>();
		min.add("最低分");
		List<Object> takeExamNum = new ArrayList<Object>();
		takeExamNum.add("人数");	
		List<Object> rank = new ArrayList<Object>();
		rank.add("排名");
		for(int i = 1;i<titleList.length-1;i++){
			Map<String, Object> one = dic.get(titleList[i]);
			takeExamNum.add(one.get("TAKE_EXAM_NUM"));
			max.add(one.get("TOP_SCORE"));
			min.add(one.get("UP_SCORE"));
			avg.add(one.get("AVG_SCORE"));
			rank.add(one.get("AVG_SCORE_RANK"));
		}
		Map cityInfo=null;
		if(level.startsWith("city"))
			cityInfo= (Map)rptExpSubjectMapper.qryRptCitySubject(parmter).get(0);
		else if(level.equals("area"))
			cityInfo= (Map)rptExpSubjectMapper.qryRptExpAreaSubject(parmter).get(0);
		takeExamNum.add(cityInfo.get("TAKE_EXAM_NUM"));//总体
		max.add(cityInfo.get("TOP_SCORE"));
		min.add(cityInfo.get("UP_SCORE"));
		avg.add(cityInfo.get("AVG_SCORE"));
		rank.add(cityInfo.get("AVG_SCORE_RANK"));
		conList.add(takeExamNum);
		conList.add(max);
		conList.add(min);
		conList.add(avg);
		conList.add(rank);
		ExportUtil.createExpExcel(titleList, conList, pathFile+"科目分数段对比（竖）"+(level.charAt(level.length()-1) =='a'?"":level.charAt(level.length()-1)) +".xls");
		
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
