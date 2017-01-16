package zyj.report.service.export;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.business.task.SubjectInfo;
import zyj.report.common.CalToolUtil;
import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpShangXianShu2Service extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;
	@Autowired
	BaseDataService baseDataService;

	@Override
	public void exportData(Map<String, Object> parmter)throws Exception  {
		String exambatchId = parmter.get("exambatchId").toString();
		String subject = ObjectUtils.toString(parmter.get("subject"));
		String pathFile = parmter.get("pathFile").toString();
		String subject0 = (String) parmter.get("subject");
		int stuType = (Integer)parmter.get("stuType");

		List<SubjectInfo> subjectList =null;
//		List<Map<String,Object>> subjects_cur = getSubjectCache();
		List<Map<String,Object>> subjects_cur = baseDataService.getSubjectByExamid(exambatchId);
				List<SubjectInfo> subjectInfoList = subjects_cur.stream()
				.map(subjectInfo -> new SubjectInfo(subjectInfo.get("PAPER_ID").toString(), subjectInfo.get("SUBJECT").toString(), subjectInfo.get("SUBJECT_NAME").toString(),(Integer)subjectInfo.get("TYPE")))
				.sorted((subject1,subject2)->{
					return CalToolUtil.indexOf(CalToolUtil.getSubjectOrder(),subject1.getSubject())- CalToolUtil.indexOf(CalToolUtil.getSubjectOrder(),subject2.getSubject());
				})
				.collect(Collectors.toList());

		if("WK".equals(subject0)){
			subjectList = subjectInfoList.stream().filter(subjectInfo -> 1 == subjectInfo.getType()).collect(Collectors.toList());
			parmter.put("type", 1);
		}
		if("LK".equals(subject0)){
			subjectList = subjectInfoList.stream().filter(subjectInfo -> 2 == subjectInfo.getType()).collect(Collectors.toList());
			parmter.put("type", 2);
		}
		if("NWL".equals(subject0)){
			subjectList = subjectInfoList;
			parmter.put("type", 0);
		}
		if(subjectList == null|| subjectList.isEmpty())return;

		List<List<Object>>  beanList = new ArrayList<List<Object>>();
		//添加总分数据
		//-------------------------------------------
		List<Integer> rowIsTitle = new ArrayList<Integer>();
		parmter.put("rownum", 0);
		parmter.put("rowIsTitle", rowIsTitle);
		int[] rankLine = CalToolUtil.getSubjectRankLine(subject0);
		double[] scoreLine = new double[rankLine.length];
		//根据排名段获取分数线
		parmter.put("rankList",  rankLine);
		Map  scoreLineMap = rptExpAllscoreMapper.getScoreLineOfAllscoreByRank(parmter);
		for(int i =0;i< rankLine.length;i++){
			scoreLine[i] = Double.parseDouble(scoreLineMap.get("SCOREOFHEAD"+rankLine[i]).toString());
		}
		if(scoreLine != null)
			beanList.addAll(getBeansFromModel(scoreLine, parmter));
		//获取一个model的行数,用于设格式
		int rownum =beanList.size();
		//getModel
		Map conditions = new HashMap<String, Object>(parmter);
		for(SubjectInfo s : subjectList){
			conditions.put("subject", s);
			conditions.put("paperId",s.getPaperId());
			rankLine = CalToolUtil.getSubjectRankLine(s.getSubject());
			conditions.put("rankList", rankLine);
			scoreLineMap = rptExpSubjectMapper.getScoreLineOfSubjectByRank(conditions);
			for(int i =0;i< rankLine.length;i++){
				scoreLine[i] = Double.parseDouble(scoreLineMap.get("SCOREOFHEAD"+rankLine[i]).toString());
			}
			if(scoreLine != null)
				beanList.addAll(getBeansFromModel(scoreLine, conditions));
		}

			int num =scoreLine.length;
			List<Integer> rows =(List)conditions.get("rowIsTitle");
			int[][] mgArray = new int[2000][];
			int k = 0;
			for(int row : rows){
				if(rows.indexOf(row-1)==-1&&rows.indexOf(row+1)!=-1)
				{
				mgArray[k] =new int[]{1,row,num*4,row};
				k++;
				for(int j = 1;j<=num;j++,k=k+2){
					mgArray[k] = new int[]{1+4*(j-1),row+1,4+4*(j-1),row+1};
					mgArray[k+1] = new int[]{1+4*(j-1),row+2,4+4*(j-1),row+2};
				}
				}
			}
			mgArray = Arrays.copyOf(mgArray, k);
			ExportUtil.createExpExcel(rows, beanList, pathFile+"上线数"+((("WK".equals(subject)||"LK".equals(subject))?"_"+subjects.get(subject).substring(0,2)+".xls":".xls")), mgArray);
	}
	/**
	 *
	 * @param scoreLine 分数线数组
	 * @param conditions 通用参数，包括科目， 考次，cityCode, stuType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<List<Object>> getBeansFromModel(double[] scoreLine ,Map conditions) throws ReportExportException {
		// 获取学校地区名
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
		List<List<Object>>  beanList = new ArrayList<List<Object>>();
		// 1.1 获得元数据
		//获得批数：如一批，二批，三批，四批
		int num = scoreLine.length;
		List<String> scoreList = new ArrayList<String>();
		 for(double s : scoreLine){
			 scoreList.add(String.valueOf(s).replace(".", "_"));
		 }
		conditions.put("scoreList", scoreList);
		String subject = conditions.get("subject").toString();
		List<Map<String,Object>> numsOfScore = null;
		List<Map<String,Object>> numsOfExam =null;
		if("WK".equals(subject) || "LK".equals(subject) || "NWL".equals(subject)){
			//总分
			numsOfScore = rptExpAllscoreMapper.qryScorePersonNumBySchoolAllscore(conditions);
//			String prefix = subject.substring(0, 1);
//			if(prefix.equals("N"))
//				prefix = "";
//			conditions.put("subject", prefix+"SX%");//用数学的人数作为总人数
			numsOfExam = rptExpAllscoreMapper.qrySchoolAllScoreInfo(conditions);
		}else{//科目
			numsOfScore =  rptExpSubjectMapper.qryScorePersonNumBySchoolSubject(conditions);
			numsOfExam = rptExpSubjectMapper.qrySchPersonNumBySubject(conditions);
		}
		Map<String,Map<String,Object>> numsOfExamMap = CalToolUtil.trans(numsOfExam, new String[]{"SCH_ID"});
		//填充数据
		//2.1 添加表头
		conditions.put("subject", subject);
		beanList.addAll(addTitle(conditions));
		//2.2 计算
		int sumTakeExamNum = 0;
		int[] sumCompletedNum = new int[num];
		for(Map sch : numsOfScore){
			List<Object>  bean = new ArrayList<Object>();
			String schid = sch.get("SCH_ID").toString();
			String schName = sch.get("SCHNAME").toString();
			bean.add(schName);
			//获取实考人数
			int  numsOfSchExam = Integer.parseInt(numsOfExamMap.get(schid).get("TAKE_EXAM_NUM").toString());
			sumTakeExamNum += numsOfSchExam;
			//获取分数线完成人数
			int j =0;//用于统计全体的上线人数
			for(String i : scoreList){
				bean.add(0);//目标
				int  numsOfSchScore = Integer.parseInt(sch.get("HE"+i).toString());
				sumCompletedNum[j] +=numsOfSchScore;
				bean.add(numsOfSchScore);//完成
				bean.add((numsOfSchScore+0.0)/numsOfSchExam);//完成率
				bean.add(0);//排名
				j++;
			}
			beanList.add(bean);
			getValueIncrease(conditions, "rownum");
		}
		//2.3 排名
		List<List<Object>> backup = new ArrayList<List<Object>>(beanList.subList(4, beanList.size()));
		for(int i = 0,j=3;i<num;i++,j=j+4){
		/*	CalToolUtil.sortByIndexValue(backup, j);
			int index = 0;
			for(int k = 4;k<beanList.size();k++){
				List<Object> sch = beanList.get(k);
				index = backup.size()-backup.indexOf(sch);
				sch.set(j+1, index);
				sch.set(j, CalToolUtil.decimalFormat2(Double.parseDouble(sch.get(j).toString())*100)+"%");
			}*/
			Integer [] order = CalToolUtil.indexSordOfList(beanList.subList(4, beanList.size()), j);
			for(int k = 4,z=0 ;k<beanList.size();k++,z++){
				List<Object> sch = beanList.get(k);
				sch.set(j+1, order[z]);
				BigDecimal f=new BigDecimal(Double.parseDouble(sch.get(j).toString())*100);
				sch.set(j, f.setScale(2,   RoundingMode.HALF_UP).doubleValue()+"%");
			}
		}
//////////////添加地区
		beanList.add(addFoot(sumCompletedNum, sumTakeExamNum));
		getValueIncrease(conditions, "rownum");
		return beanList;
	}
	/**
	 * 添加表头
	 * @param conditions
	 * @return
	 */
	private List<List<Object>> addTitle(Map conditions){
		List rowIsTitle = (List)conditions.get("rowIsTitle");
		rowIsTitle.add(conditions.get("rownum"));
		getValueIncrease(conditions, "rownum");
		rowIsTitle.add(conditions.get("rownum"));
		getValueIncrease(conditions, "rownum");
		rowIsTitle.add(conditions.get("rownum"));
		getValueIncrease(conditions, "rownum");
		rowIsTitle.add(conditions.get("rownum"));
		getValueIncrease(conditions, "rownum");
		List<List<Object>>  beanList = new ArrayList<List<Object>>();
		List<Object>  bean1 = new ArrayList<Object>();
		bean1.add("学科");
		bean1.add(this.subjects.get(conditions.get("subject").toString()));
		List<Object>  bean2 = new ArrayList<Object>();
		List scoreList = (List) conditions.get("scoreList");
		bean2.add("批次");
		List<Object>  bean3 = new ArrayList<Object>();
		bean3.add("分数线");
		List<Object>  bean4 = new ArrayList<Object>();
		bean4.add("目标/完成");
		for(int i = 0;i<scoreList.size();i++){//加空字符用于合并单元格
			bean2.add((i+1)+"批");
			BigDecimal f=new BigDecimal(Double.parseDouble(scoreList.get(i).toString().replace("_", ".")));
			bean3.add( f.setScale(2,   RoundingMode.HALF_UP).doubleValue()+"0");
			bean3.add("");
			bean3.add("");
			bean3.add("");
			bean2.add("");
			bean2.add("");
			bean2.add("");
			bean1.add("");
			bean1.add("");
			bean1.add("");
			bean1.add("");
			bean4.add("目标");
			bean4.add("完成");
			bean4.add("完成率");
			bean4.add("排名");
		}
		bean1.remove(bean1.size()-1);//移除最后一个空字符
		beanList.add(bean1);
		beanList.add(bean2);
		beanList.add(bean3);
		beanList.add(bean4);
		return beanList;
	}
	/**
	 * 表脚，即全体数据
	 * @param sumCompletedNum
	 * @param sumTakeExamNum
	 * @return
	 */
	private List<Object> addFoot(int[] sumCompletedNum , int sumTakeExamNum){
		List<Object> bean = new ArrayList<Object>();
		bean.add("全区县");
		for(int i : sumCompletedNum){
			bean.add(0);
			bean.add(i);
			BigDecimal f=new BigDecimal((i+0.0)*100/sumTakeExamNum);
			bean.add( f.setScale(2,   RoundingMode.HALF_UP).doubleValue()+"%");
			bean.add("--");
		}
		return bean;
	}
	public static void main(String[] args) {
		BigDecimal f=new BigDecimal(1.7654321);
		 double a = f.setScale(2,   RoundingMode.HALF_UP).doubleValue();
		 double b = 1.7654321;
		 String c =CalToolUtil.decimalFormat2(b);
		 System.out.println(a);
		 System.out.println(c);
	}
}
