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
import zyj.report.persistence.client.RptExpSubjectMapper;

@Service
public class ExpZhengTaiFenBuService extends BaseRptService {
	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;

//	String excelName = "正态分布分析";
//	String sheetName = "正态分布分析";
//	String titleName = "";

	final private String title = "分数段,频数,频率,累计频数,累计频率";

	// 映射查询结果与表标题
	final private static Map<String, String> fieldMap;
	static {
		fieldMap = new HashMap<String, String>();
		fieldMap.put("分数段", "SCORE_SECTION");
		fieldMap.put("频数", "NUMBER");
		fieldMap.put("频率", "FREQUENCY");
		fieldMap.put("累计频数", "C_NUMBER");
		fieldMap.put("累计频率", "C_FREQUENCY");
	}
	
	final private double step = 5;

	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String path = (String) parmter.get("pathFile");
		String paperId = parmter.get("paperId").toString();
		String subject = (String) parmter.get("subject");
		String level =(String)parmter.get("level") ;
		int stuType = (Integer)parmter.get("stuType");
		
		double upper  ;//分数区间上界
		double lower ;//分数区间下界
		double person_num;
		List<Map<String, Object>> beanList ;
//校验参数,
		if(exambatchId == null ||cityCode == null ||subject == null || !subjects.containsKey(paperId+subject))
			return;
		// 获取学校地区名		
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();

		parmter.put("orderBy", "SIGN_TOTAL  desc");
		List<Map<String, Object>> stuList = new ArrayList< Map<String, Object>>();
		beanList = new ArrayList< Map<String, Object>>();
		String areaId = null;
		String schoolId = null;
		String classesId = null;
		//获取不同维度下的学生科目信息
		switch(level){
		case "city":
			stuList = rptExpSubjectMapper.qryStudentSubjectScore(parmter);
			break;
		case "area"://其实不用生成区
			areaId = (String) parmter.get("areaId");
			if(areaId == null )
				return;
			stuList = rptExpSubjectMapper.qryStudentSubjectScore(parmter);
			break;
		case "school":
			schoolId = (String) parmter.get("schoolId");
			if(schoolId == null )
				return;
			stuList = rptExpSubjectMapper.qryStudentSubjectScore(parmter);
			break;
		case "classes":
			classesId = (String) parmter.get("classesId");
			if(classesId == null )
				return;
			stuList = rptExpSubjectMapper.qryStudentSubjectScore(parmter);
			break;
		}
		if (stuList.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

		//获取该试卷的总分
		Map<String,Object> data = new HashMap<String, Object>(); 
		double total  = 0;
		if(stuList.size()!=0){
			person_num = stuList.size();
			total = Double.parseDouble(stuList.get(0).get("FULL_SCORE").toString());
			upper = total ;//区间上界
			lower =  total-(total%step==0?step:total%step);//区间下界
			data.put("upper", upper);
			data.put("lower", lower);
			data.put("beanList", beanList);
			data.put("person_num", person_num);
			Map<String,Object> newSpace = newOneSpace(data);
			newSpace.put("SCORE_SECTION", "["+lower+"-"+upper+"]");//闭区间
			for(Map stu : stuList){
				double score_cur = Double.parseDouble(stu.get("SIGN_TOTAL").toString());
				newSpace = findMyPlace(score_cur, newSpace,data);					
			}
			//统计最后一个有学生的区间
			calLastSpace(newSpace,data);
			//将剩下的区间分完
			while(Double.parseDouble(data.get("lower").toString()) >= 0){
				calLastSpace(newOneSpace(data),data);
			}

			beanList = (List) data.get("beanList");
			String[] titleArr = title.split(",");
			String[][] conList = map2objects(fieldMap, titleArr, beanList);
			// String titelName = (String)
			// jyjRptExtMapper.qryExambatch(exambatchId).get("NAME")+"_"+subjects.get(subject);
			int[][] avgmerge = {};
			String[][] titelList = { titleArr };
			ExportUtil.createExpExcel(titelList, conList, avgmerge, "正太分布.xls", "正太分布",
					null, path);

		}

	}
	/**
	 * 找到学生分数对应的区间，区间NUMBER自加，返回此区间
	 * @param myScore
	 * @param space
	 * @return
	 */
	private Map<String,Object> findMyPlace(double myScore,Map<String,Object> space,Map<String,Object> data){
		double upper = Double.parseDouble(data.get("upper").toString());
		double lower = Double.parseDouble(data.get("lower").toString());
//		
		if(lower <= myScore){
			//更新区间Map的频数
			getValueIncrease(space, "NUMBER");
			return space;//返回新建的区间(List中没有)
		}else{
			//统计上一个区间Map
			calLastSpace(space,data);
			//新建下一个区间Map
			Map<String,Object> newSpace = newOneSpace(data);
			//递归
			return findMyPlace(myScore, newSpace,data);
		}
	}
	
	//新建下一个区间Map
	private Map<String,Object> newOneSpace(Map<String,Object> data){
		double upper = Double.parseDouble(data.get("upper").toString());
		double lower = Double.parseDouble(data.get("lower").toString());
		Map<String,Object> newSpace = new HashMap<String, Object>();
		newSpace.put("SCORE_SECTION", "["+lower+"-"+upper+")");
		newSpace.put("NUMBER", 0);
		newSpace.put("C_NUMBER", 0);
		newSpace.put("FREQUENCY", 0.0+"%");	
		newSpace.put("C_FREQUENCY", 0.0+"%");	
		return newSpace;
	}
	
	//统计上一个区间Map
	private void calLastSpace(Map<String,Object> lastSpace,Map<String,Object> data){
		double upper = Double.parseDouble(data.get("upper").toString());
		double lower = Double.parseDouble(data.get("lower").toString());
		double person_num = Double.parseDouble(data.get("person_num").toString());
		List<Map<String,Object>> beanList = (List)data.get("beanList");
		//获得频数
		int num = Integer.parseInt(lastSpace.get("NUMBER").toString());
//		开始统计
		lastSpace.put("FREQUENCY", CalToolUtil.decimalFormat2((0.0+num)/person_num*100)+"%");	
		//获得上一区间的累计频数
		int c_num = 0;
		if(beanList.size() != 0)
			c_num = Integer.parseInt(beanList.get(beanList.size()-1).get("C_NUMBER").toString());
		lastSpace.put("C_NUMBER", (c_num+num));
		lastSpace.put("C_FREQUENCY",  CalToolUtil.decimalFormat2((c_num+num+0.0)/person_num*100)+"%");		
		beanList.add(lastSpace);
		//更新上下界,和beanList
		upper = lower;
		lower = lower - step;
		data.put("upper", upper);
		data.put("lower", lower);
		data.put("beanList", beanList);
	}
}
