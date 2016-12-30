package zyj.report.service.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.persistence.client.RptExpQuestionMapper;

@Service
public class ExtFetchKmxtfxServer extends BaseRptService {
	
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;

	public List<Map<String, Object>> fetchExportData(Map<String,Object> parmter) {
		String exambatchId = parmter.get("exambatchId").toString();
		String cityCode = parmter.get("cityCode").toString();
		//读取源数据
		List<Map<String,Object>> question = rptExpQuestionMapper.findRptExpQuestion(parmter);
		List<Map<String,Object>> questionitem = rptExpQuestionMapper.findRptExpQuestionItem(parmter);

		if (question.isEmpty() || questionitem.isEmpty()) return new ArrayList<>();
		//转换数据程序
		Map<String,Map<String,Object>> questionitemtrans = zyj.report.common.CalToolUtil.trans(questionitem, new String[]{"EXAMBATCH_ID", "SUBJECT", "QUESTION_ORDER"});
		
		//报表输出数据
		List<Map<String,Object>> resdata = question;
		
		for(Map<String,Object> m : resdata){

			String subject = ObjectUtils.toString(m.get("SUBJECT"));
			String  questionOrder = ObjectUtils.toString(m.get("QUESTION_ORDER"));
			double takeExamNum = Double.parseDouble(ObjectUtils.toString(m.get("TAKE_EXAM_NUM")));
			String k = exambatchId + subject + questionOrder;
			
			Map<String,Object> item = questionitemtrans.get(k);
			if(item != null){
				String opt = ObjectUtils.toString(item.get("OPT_DETAIL"));
				if(StringUtils.isNotBlank(opt)){
					try{
						JSONObject optJson = JSONObject.fromString(opt);
						String[] t1 = zyj.report.common.CalToolUtil.getAllCombination(1);
						String[] t2 = zyj.report.common.CalToolUtil.getAllCombination(2);
						for(int i =0;i<23;i++){
							m.put(t1[i], getValue(optJson,t2[i]).equals("0")?"": zyj.report.common.CalToolUtil.decimalFormat2(Double.parseDouble(getValue(optJson, t2[i])) * 100 / takeExamNum)+"%");
						}
					}catch(Exception e){e.printStackTrace();}
				}
			}
		}
		return resdata;
	}
	
	private String getValue(JSONObject optJson,String key){
		if(optJson.has(key)){
			return optJson.getString(key);
		}
		return "0";
	}

	public String getXlsFileName(){
		return "科目小题分析";
	}
}
