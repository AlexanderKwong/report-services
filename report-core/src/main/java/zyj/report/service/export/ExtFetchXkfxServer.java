package zyj.report.service.export;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.persistence.client.RptExpSubjectQualityMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExtFetchXkfxServer extends BaseRptService {
	
	@Autowired
	RptExpSubjectQualityMapper rptExpSubjectQualityMapper;
	@Autowired
	BaseDataService baseDataService;

	public List<Map<String, Object>> fetchExportData(Map<String,Object> parmter) {
		String exambatchId = parmter.get("exambatchId").toString();
		String cityCode = parmter.get("cityCode").toString();
		List<Map<String,Object>> data = rptExpSubjectQualityMapper.findRptExpSubjectQuality(parmter);
//		System.out.println(data);
		zyj.report.common.CalToolUtil.sortByValue(data, "SUBJECT", zyj.report.common.CalToolUtil.getSubjectOrder());
		for(Map s : data){
//			s.put("SUBJECT_NAME", this.subjects.get(s.get("PAPER_ID").toString()+s.get("SUBJECT")));
			s.put("SUBJECT_NAME", baseDataService.getSubjectByPaperIdAndShortName(exambatchId,s.get("PAPER_ID").toString(), s.get("SUBJECT").toString() ).get("SUBJECT_NAME"));
		}
		return data;
	}

	public String getXlsFileName(){
		return "学科分析";
	}
}
