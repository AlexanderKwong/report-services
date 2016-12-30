package zyj.report.service.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpXkfxtfxZSService extends BaseRptService {
	
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	BaseDataService baseDataService;

	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {
		String exambatchId = parmter.get("exambatchId").toString();
		String cityCode = parmter.get("cityCode").toString();
		String subject0 = parmter.get("subject").toString();
		String paperId0 = parmter.get("paperId").toString();
		String path = parmter.get("pathFile").toString();
		//读取源数据
		List<Map<String,Object>> question = rptExpQuestionMapper.findRptExpQuestion(parmter);
		List<Map<String,Object>> questionitem = rptExpQuestionMapper.findRptExpQuestionItem(parmter);
		if (question.isEmpty() || questionitem.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");
		
		//转换数据程序
//		Map<String,Map<String,Object>> questionitemtrans = CalToolUtil.trans(questionitem, new String[]{"EXAMBATCH_ID","AREA_ID","SCH_ID","CLS_ID","SUBJECT","QUESTION_ORDER"});
		Map<String,Map<String,Object>> questionitemtrans = CalToolUtil.trans(questionitem, new String[]{"PAPER_ID", "SUBJECT", "QUESTION_ORDER"});
		
		//报表输出数据
//		List<Map<String,Object>> resdata = question;
		List<List<Object>> resdata = new LinkedList<List<Object>>();
		
		//计数器，统计最大选项数
		int count = 0;
		
		String[] t1 = CalToolUtil.getAllCombination(1);
		String[] t2 = CalToolUtil.getAllCombination(2);
		for(Map<String,Object> m : question){
			List<Object> row = new ArrayList<Object>();
			row.add(m.get("QUESTION_NO"));
			row.add(getQtype(m.get("QST_TIPY").toString()));
			row.add(m.get("QST_SCORE"));
			row.add(m.get("TAKE_EXAM_NUM"));
			row.add(m.get("AVG_SCORE"));
			row.add(m.get("TOP_SCORE"));
			row.add(m.get("UP_SCORE"));
			row.add(m.get("FULL_RANK"));
			row.add(m.get("STAND_POOR"));
			row.add(m.get("DIS_DEGREE"));
			row.add(m.get("DIFFICULTY_NUM"));

			//利用索引找到选项内容
			String paperId = ObjectUtils.toString(m.get("PAPER_ID"));
			String subject = ObjectUtils.toString(m.get("SUBJECT"));
			String  questionOrder = ObjectUtils.toString(m.get("QUESTION_ORDER"));
			double takeExamNum = Double.parseDouble(ObjectUtils.toString(m.get("TAKE_EXAM_NUM")));
			String k = paperId + subject + questionOrder;
			
			Map<String,Object> item = questionitemtrans.get(k);
			if(item != null){
				String opt = ObjectUtils.toString(item.get("OPT_DETAIL"));
				if(StringUtils.isNotBlank(opt)){
					try{
						JSONObject optJson = JSONObject.fromString(opt);
						for(int i =0;i<23;i++){
							String v= getValue(optJson,t2[i]);
							m.put(t1[i], v.equals("0")?"":v);
							m.put(t1[i]+"%", v.equals("0")?"":CalToolUtil.decimalFormat2(Double.parseDouble(v)*100/takeExamNum)+"%");
							count = (!v.equals("0")&&(i+1)>count)?i+1:count;
						}
					}catch(Exception e){e.printStackTrace();}
				}
			}
			//动态添加选项
			for(int i = 0;i<count;i++){
				row.add(m.get(t1[i]));
				row.add(m.get(t1[i]+"%"));
			}
			resdata.add(row);
		}


		//标题长度
		int titleLength = 11+count*2;
		
		//添加全卷
		Map<String, Object> all =rptExpSubjectMapper.findRptExpSubject(parmter).get(0);
		List<Object> row = new ArrayList<Object>();
		row.add("全卷");
		row.add("--");
		row.add(all.get("FULL_SCORE"));
		row.add(all.get("TAKE_EXAM_NUM"));
		row.add(all.get("AVG_SCORE"));
		row.add(all.get("TOP_SCORE"));
		row.add(all.get("UP_SCORE"));
		row.add(all.get("FULL_RANK"));
		row.add(all.get("STU_SCORE_SD"));
		while(row.size()<titleLength){
			row.add("--");
		}
		resdata.add(row);
		
	//生成标题
		String[] title1 = {"题目","题型","满分值","实考人数","平均分","最高分","最低分","满分率","标准差","区分度","难度系数"};
		String[] title2 = {"","","","","","","","","","",""};
		List<String> tt1 = new ArrayList(Arrays.asList(title1));
		List<String> tt2 = new ArrayList(Arrays.asList(title2));
		for(int i = 0;i<count;i++){
			tt1.add("选"+t1[i]);
			tt1.add("");	
			tt2.add("人数");
			tt2.add("选"+t1[i]+"率");
		}
		title1 = tt1.toArray(new String[tt1.size()]);
		title2 = tt2.toArray(new String[tt2.size()]);
		int [][] mgArray =new int[titleLength][4]; 
//			{{0,1,0,2},{1,1,1,2},{2,1,2,2},{3,1,3,2},{4,1,4,2},{5,1,5,2},{6,1,6,2},{7,1,7,2},{8,1,8,2},{9,1,9,2},{10,1,10,2}};
		for(int i = 0;i<11;i++){
			mgArray[i]=new int[]{i,1,i,2};
		}
		for(int i = 11;i<titleLength-1;i=i+2){
			mgArray[i] = new int[]{i, 1, i + 1, 1};
		}
		ExportUtil.createExpExcel(new String[][]{title1, title2}, formatList(resdata, titleLength), mgArray, "分学科分析.xls", "分学科分析", baseDataService.getSubjectByPaperIdAndShortName(exambatchId,paperId0,subject0).get("SUBJECT_NAME") + "学科分小题分析", path);
		
	}
	
	private String getValue(JSONObject optJson,String key){
		if(optJson.has(key)){
			return optJson.getString(key);
		}
		return "0";
	}

	private String getQtype(String qst_type){
		if(qst_type.trim().equals("1"))
			return "单选";
		else if(qst_type.trim().equals("2"))
			return "多选";
		else if(qst_type.trim().equals("3"))
			return "判断";
		else if(qst_type.trim().equals("4"))
			return "主观";
		else 
			return "未知";
	}
	private String [][]  formatList(List<List<Object>> resList,int count){
		String [][] resArr = new String[resList.size()][count];
		for (int j = 0;j<resList.size();j++) {
			String[] r = new String[count];
			List<Object> list = new ArrayList<>(resList.get(j));
			while(list.size()>count){
				list.remove(list.size() - 1);
			}
			while(list.size()<count) {
				list.add("");
			}
			for(int i = 0; i<count ;i++){
				r[i] = ObjectUtils.toString(list.get(i));
			}
			resArr[j]=r;
		}
		return resArr;
	}
	public String getXlsFileName(){
		return "科目小题分析";
	}
}
