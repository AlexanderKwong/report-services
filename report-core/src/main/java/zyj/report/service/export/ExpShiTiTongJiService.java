package zyj.report.service.export;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpQuestionMapper;

@Service
public class ExpShiTiTongJiService extends BaseRptService {
	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;

	String excelName = "试题统计.xls";
	String sheetName = "试题统计";
	String titleName = "";



	// 映射查询结果与表标题
	final private static Map<String, String> fieldMap;
	static {
		fieldMap = new HashMap<String, String>();
		fieldMap.put("难度（P）区分度（R)", "PR");
		fieldMap.put("题号", "NO");
		fieldMap.put("题量", "TILIANG");
		fieldMap.put("比例", "BILI");
	}
	
	final private String title = "难度（P）区分度（R),题号,题量,比例";
	
	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {
		
		String exambatchId = (String) parmter.get("exambatchId");
		String cityCode = (String) parmter.get("cityCode");
		String path = (String) parmter.get("pathFile");
		String subject = (String) parmter.get("subject");
		String paperId = parmter.get("paperId").toString();
		int stuType = (Integer)parmter.get("stuType");
//校验参数,
		if(exambatchId == null ||cityCode == null ||subject == null ||!subjects.containsKey(paperId+subject))
			return;

		List<Map<String,Object>> questionSuitable = rptExpQuestionMapper.qryQuestionSuitable(parmter);
		if (questionSuitable.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

		String data;
		 Reader inStream=null;
		for(Map situation : questionSuitable){
			int nandu = Integer.parseInt(situation.get("P").toString());
			int qufendu = Integer.parseInt(situation.get("R").toString());
			if(nandu == 0 && qufendu == 1)
				situation.put("PR", "难度适合  区分度合适");
			else if (nandu == 0 && qufendu == 0)
				situation.put("PR", "难度适合  区分度不合适");
			else if (nandu == 1 && qufendu == 1)
				situation.put("PR", "难度偏难  区分度合适");
			else if (nandu == -1 && qufendu == 1)
				situation.put("PR", "难度偏易  区分度合适");
			else if (nandu == 1 && qufendu == 0)
				situation.put("PR", "难度偏难  区分度不合适");
			else if (nandu == -1 && qufendu == 0)
				situation.put("PR", "难度偏易  区分度不合适");
		}
		
		String[] titleArr = title.split(",");
		String [][]conList = map2objects(fieldMap, titleArr, questionSuitable);
//		String titelName = (String) jyjRptExtMapper.qryExambatch(exambatchId).get("NAME")+"_"+subjects.get(subject);
		int [][] avgmerge = {};
		String[][] titelList = {titleArr};
		ExportUtil.createExpExcel(titelList, conList, avgmerge, excelName, sheetName, null, path);
		
	}
	
}
