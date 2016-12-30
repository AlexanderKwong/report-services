package zyj.report.service.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import zyj.report.common.CalToolUtil;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpStudetScoreMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExtZffsddbvServer extends BaseRptService {
	
	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;
	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	@Autowired
	BaseDataService baseDataService;
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
		String level = parmter.get("level").toString();
		
		// 获取学校地区名		
		
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
//		Map<String,Map<String,Object>> areaNameMap = getAreaCache();
		Map<String,Map<String,Object>> schNameMap = CalToolUtil.trans(baseDataService.getSchools(exambatchId), new String[]{"SCH_ID"});
		Map<String,Map<String,Object>> areaNameMap = CalToolUtil.trans(baseDataService.getAreas(exambatchId), new String[]{"AREA_ID"});
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
		List<Map<String,Object>> schooldata = null;
		List<Map<String,Object>> data = rptExpStudetScoreMapper.findRptExpStudetAllScore(parmter);
		if(level.equals("city1")||level.equals("area"))
			schooldata = rptExpStudetScoreMapper.findAllSchoolForRptExpStudetAllScore(parmter);
		if(level.equals("city2"))
			schooldata = rptExpStudetScoreMapper.findAllAreaForRptExpStudetAllScore(parmter);
		if(data.size()==0||schooldata.size()==0)return ;
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
				Map tmp = schNameMap.get(schid);
				schName = tmp.get("SCH_NAME").toString();
				areaName = tmp.get("AREA_NAME")==null?"市直":tmp.get("AREA_NAME").toString();
				title = areaName+"_"+schName;
			}else if(level.equals("city2")){
				schid = m.get("AREA_ID").toString();
				areaName =areaNameMap.get(schid)==null?"市直":areaNameMap.get(schid).get("AREA_NAME").toString();
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
				Map tmp = schNameMap.get(schid);
				schName = tmp.get("SCH_NAME").toString();
				areaName = tmp.get("AREA_NAME")==null?"市直":tmp.get("AREA_NAME").toString();
				title = areaName+"_"+schName;
			}else if(level.equals("city2")){
				schid = m.get("AREA_ID").toString();
				areaName = areaNameMap.get(schid)==null?"市直":areaNameMap.get(schid).get("AREA_NAME").toString();
				title = areaName;
			}
			//得到分数段计算入累计人数, fsd:分数段
			double score = Double.parseDouble(m.get("ALL_TOTAL").toString());
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
					statistic = rptExpAllscoreMapper.qrySchoolAllScoreInfo2(parmter);
					for(Map s :  statistic){
						String schid1 = s.get("SCH_ID").toString();
						Map schInfo = schNameMap.get(schid1);
						s.put("AREANAME_SCHNAME", (schInfo.get("AREA_NAME")==null?"市直":schInfo.get("AREA_NAME").toString())+"_"+schInfo.get("SCH_NAME").toString());
					}
					dic = CalToolUtil.trans(statistic, new String[]{"AREANAME_SCHNAME"});
				}else if(level.equals("city2")){
					statistic = rptExpAllscoreMapper.qryAreaAllScoreInfo2(parmter);
					for(Map s :  statistic){
						String schid1 = s.get("AREA_ID").toString();
						Map areaInfo = areaNameMap.get(schid1);
						s.put("AREANAME", areaInfo==null?"市直":areaInfo.get("AREA_NAME"));
					}
					dic = CalToolUtil.trans(statistic, new String[]{"AREA_NAME"});
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
				//提取前缀
				String preffix = "";
				if("WK".equals(subject))
					preffix = "W_";
				else if("LK".equals(subject))
					preffix = "L_";
				for(int i = 1;i<titleList.length-1;i++){
					Map<String, Object> one = dic.get(titleList[i]);
					takeExamNum.add(one.get(preffix+"PERSON_NUM"));
					max.add(one.get(preffix+"TOP_SCORE"));
					min.add(one.get(preffix+"UP_SCORE"));
					avg.add(one.get(preffix+"AVG_SCORE"));
					rank.add(one.get(preffix+"AVG_SCORE_RANK"));
				}
				Map cityInfo=null;
				if(!level.equals("area")){
					cityInfo= (Map)rptExpAllscoreMapper.qryCityAllScoreInfo2(parmter).get(0);
				}else{
					cityInfo= (Map)rptExpAllscoreMapper.qryAreaAllScoreInfo2(parmter).get(0);
				}
				takeExamNum.add(cityInfo.get(preffix+"PERSON_NUM"));//总体
				max.add(cityInfo.get(preffix+"TOP_SCORE"));
				min.add(cityInfo.get(preffix+"UP_SCORE"));
				avg.add(cityInfo.get(preffix+"AVG_SCORE"));
				rank.add(cityInfo.get(preffix+"AVG_SCORE_RANK"));
				conList.add(takeExamNum);
				conList.add(max);
				conList.add(min);
				conList.add(avg);
				conList.add(rank);
				ExportUtil.createExpExcel(titleList, conList, pathFile+"总分分数段对比（竖）"+(level.charAt(level.length()-1) =='a'?"":level.charAt(level.length()-1)) +(StringUtils.isBlank(subjects.get(subject))?"":"_"+subjects.get(subject))+".xls");
		
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
