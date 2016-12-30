package zyj.report.service.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.ExportUtil;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

@Service
public class ExpDjfsxZSService extends BaseRptService {
	
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;
	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;
	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	BaseDataService baseDataService;

	public void exportData(Map<String,Object> parmter) throws Exception{
		String exambatchId = parmter.get("exambatchId").toString();
		String pathFile = parmter.get("pathFile").toString();
		String grade = "";

		//读取源数据
		Map<String,Object> zf_data = rptExpSubjectMapper.qryZSzfrptABCD(parmter);
		Map<String,Object> nolisten_yy_data = rptExpQuestionMapper.qryTotalWithoutListening(parmter);
		List<Map<String,Object>> km_data = rptExpSubjectMapper.qryZSkmrptABCD(parmter);
		List<Map<String,Object>> km_data2 = rptExpSubjectMapper.findRptExpSubject(parmter);
		grade=jyjRptExtMapper.qryExamGrade(exambatchId).get(0);

		if (zf_data.isEmpty() || nolisten_yy_data.isEmpty() || km_data.isEmpty() || km_data2.isEmpty() ) throw new ReportExportException("没有查到源数据，请核查！");;
//排版
		List<Object> title = new ArrayList<Object>();
		title.add("年级");
		title.add("等级");

		List<List<Object>> resdata = new ArrayList<List<Object>>();

		List<Object> a = new ArrayList<Object>();
		a.add(grade); a.add("A");
		List<Object> b = new ArrayList<Object>();
		b.add(""); b.add("B");
		List<Object> c = new ArrayList<Object>();
		c.add(""); c.add("C");
		List<Object> d = new ArrayList<Object>();
		d.add(""); d.add("D");
		d.add("C等分数线以下的即为D等");
		List<Object> avg = new ArrayList<Object>();
		avg.add(""); avg.add("平均分");
		List<Object> num = new ArrayList<Object>();
		num.add(""); num.add("参与统计的人数");
		km_data = CollectionsUtil.leftjoinMapByKey(km_data, km_data2, "SUBJECT");
	//加入非听力
		Map<String,Object> yy_n_l = new HashMap<String, Object>();
		yy_n_l.put("SUBJECT", "YY_N_L");
		yy_n_l.put("PAPER_ID","");
		km_data.add(yy_n_l);
		List<Map<String,Object>> tmp = new ArrayList<Map<String,Object>>();
		tmp.add(nolisten_yy_data);
		km_data = CollectionsUtil.leftjoinMapByKey(km_data, tmp, "SUBJECT");
		
		zyj.report.common.CalToolUtil.sortByValue(km_data, "SUBJECT", zyj.report.common.CalToolUtil.getSubjectOrder());
		for(Map<String,Object>m : km_data){
			a.add(m.get("A"));
			b.add(m.get("B"));
			c.add(m.get("C"));
//			title.add(BaseRptService.subjects.get(m.get("PAPER_ID").toString()+m.get("SUBJECT")));
			title.add(baseDataService.getSubjectByPaperIdAndShortName(exambatchId,m.get("PAPER_ID").toString(),m.get("SUBJECT").toString()).get("SUBJECT_NAME"));
			avg.add(m.get("AVG_SCORE"));
			num.add(m.get("TAKE_EXAM_NUM"));
		}
		for(int i = 1 ; i <=3 ; i++){
			if(grade.equals("初一")&&i==2)
				continue;
			a.add(zf_data.get("ZF"+i+"_A"));
			b.add(zf_data.get("ZF"+i+"_B"));
			c.add(zf_data.get("ZF"+i+"_C"));
			d.add("");
			avg.add(zf_data.get("ZF"+i+"_AVG"));
			num.add(zf_data.get("ZF"+i+"_NUM"));
			title.add("总分"+i);
		}
		resdata.add(a);resdata.add(b);resdata.add(c);resdata.add(d);resdata.add(avg);resdata.add(num);
		
		int[][] mgArray = new int[][]{{0,2,0,7},{2,5,title.size()-1,5}};
		ExportUtil.createExpExcel(new String[][]{title.toArray(new String[title.size()])}, resdata, pathFile+"等级分数线.xls", mgArray, "等级分数线——"+grade,"说明：\n\r\t1.若某科卷面分为0分者，视为该科缺考。对部分科目未使用市试卷的学校，在其总分中已做了算法修正，基本不影响等级分数线的划定。\n\r\t2.等级分数线的划定依据2015年中考方案的规定，即分为A、B、C、D四等，其中A等30%、B等35%、C等25%、D等10% 。\n\r\t3.由于各科目参考人数不同，所以各科目平均分之和与总分并不完全一致，有少许差别。\n\r\t4.总分1=语+数+英 ; 总分2=总分1+0.8*物+0.5*化 ; 总分3=所有科目总分和。");
		
	}

}
