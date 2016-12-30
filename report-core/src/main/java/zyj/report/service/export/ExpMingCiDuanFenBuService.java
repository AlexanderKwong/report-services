package zyj.report.service.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpMingCiDuanFenBuService extends BaseRptService {
	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	@Autowired
	BaseDataService baseDataService;
	
	final private String	title ="学校，区县,<=10,<=30,<=50,<=100,<=200,<=300";

	// 映射查询结果与表标题
		final private static  Map<String, String> fieldMap ;
		static{
			fieldMap = new HashMap<String, String>();
			fieldMap.put("学校，区县", "SCH_NAME");
			fieldMap.put("<=10", "LE10");
			fieldMap.put("<=30", "LE30");
			fieldMap.put("<=50", "LE50");
			fieldMap.put("<=100", "LE100");
			fieldMap.put("<=200", "LE200");
			fieldMap.put("<=300", "LE300");
		}
	
	/**
	 * 根据传入不同的subject来生成对应subject的报表，其中WZF代表文科总分，理科总分
	 */
	@Override
	public void exportData(Map<String, Object> parmter)throws Exception  {
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String path = (String) parmter.get("pathFile");
		String subject = (String) parmter.get("subject");
		String paperId = parmter.get("paperId").toString();
		int stuType = (Integer)parmter.get("stuType");
//校验参数,
		if(exambatchId == null ||cityCode == null ||subject == null )
			return;

		List<Map<String, Object>> beanList = new ArrayList< Map<String, Object>>();
		List<Map<String,Object>> stuList = null;
		if(subject.equals("NWL")){
			//不分文理科的总分
			stuList  = rptExpAllscoreMapper.qryAllScoreByRanking(parmter);
		}
		else if(subject.equals("WK")){
			//得到本考次文科前三百名的学生
			stuList  = rptExpAllscoreMapper.qryWenKeAllScoreByRanking(parmter);
		}else if(subject.equals("LK")){
			//得到本考次理科前三百名的学生
			stuList  = rptExpAllscoreMapper.qryLiKeAllScoreByRanking(parmter);
		}else{
			//得到本考次学科前三百名的学生
			stuList = rptExpSubjectMapper.qrySubjectQualityByRanking(parmter);
		}
		if(stuList.size()==0) return;
			
		//得到本考次全市内的所有学校
		List<Map<String,Object>> schoolList = jyjRptExtMapper.qryAllSchoolByCity(parmter);
		//开始统计
		//存储 areId : Map 键值对，其中Map报表中的一行，意义是统计地区数据
		Map<String, Map> areaMap = new HashMap<String, Map>();
		for(Map sch : schoolList){
			initCalMap(sch);
			//初始化学校对应的地区
			Map areaOfSch = areaMap.get(sch.get("AREA_ID"));
			if(areaOfSch == null){
				areaOfSch = new HashMap<String, Object>();
				initCalMap(areaOfSch);
				areaOfSch.put("AREA_NAME", baseDataService.getArea(exambatchId,sch.get("AREA_ID").toString()).get("AREA_NAME"));
				areaMap.put(sch.get("AREA_ID").toString(), areaOfSch);
			}			
		}
		for(Map stu : stuList){
			String stuAreaId = (String)stu.get("AREA_ID");
			String stuSchoolId = (String)stu.get("SCH_ID");
			int stuRanking = Integer.parseInt(stu.get("CITY_RANK").toString());
			//找到相应的学校
			for(Map sch : schoolList){
				
				if(stuSchoolId.equals(sch.get("SCH_ID"))){
					//找到相应的地区
					Map areaCount = areaMap.get(stuAreaId);
					//预期效果：排名<=10的，LE10+1,LE30+1,LE50+1LE100+1,LE200+1,LE200+1,LE300+1
					//排名11-30的,LE30+1,LE50+1LE100+1,LE200+1,LE200+1,LE300+1,如此类推
					switch((stuRanking-1)/100){
					case 0://1-100
						switch((stuRanking-1)/10){
						case 0://1-10
							getValueIncrease(areaCount, "LE10");//地区统计
							getValueIncrease(sch, "LE10");//学校统计
						case 1://11-20
						case 2://21-30
							getValueIncrease(areaCount, "LE30");
							getValueIncrease(sch, "LE30");
						case 3://31-40
						case 4://41-50
							getValueIncrease(areaCount, "LE50");
							getValueIncrease(sch, "LE50");
						case 5://51-60	
						case 6://61-70
						case 7://71-80
						case 8://81-90
						case 9://91-100
							getValueIncrease(areaCount, "LE100");
							getValueIncrease(sch, "LE100");
						}
					case 1://101-200
						getValueIncrease(areaCount, "LE200");
						getValueIncrease(sch, "LE200");
					case 2://201-300
						getValueIncrease(areaCount, "LE300");
						getValueIncrease(sch, "LE300");
					}
					break;
				}
			}
			
		}

		beanList = addAreaInfo(schoolList, areaMap);

		String[] titleArr = title.split(",");
		String [][]conList = map2objects(fieldMap, titleArr, beanList);
		int [][] avgmerge = {};
		String[][] titelList = {titleArr};
		ExportUtil.createExpExcel(titelList, conList, avgmerge, "名次段分布" + (("WK".equals(subject) || "LK".equals(subject)) ? (StringUtils.isBlank(subjects.get(subject)) ? "" : "_" + subjects.get(subject)) + ".xls" : ".xls"), "名次段分布", null, path);

	}
private void initCalMap(Map<String,Object> m){
		m.put("LE10", 0);
		m.put("LE30", 0);
		m.put("LE50", 0);
		m.put("LE100", 0);
		m.put("LE200", 0);
		m.put("LE300", 0);
	
}

}
