package zyj.report.service.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;

import java.util.Map;

/**
 *
 *  导出 湖北版 学生各科成绩和总分（班级） 模板
 *
 */
@Service
public class ExpStudentScoreService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;

	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;

	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;

	@Autowired
	BaseDataService baseDataService;

	private final String myKey = "StudentScore";//与学科分组分析共享key ，缓存内容相同


	/**
	 * level :市区 1，镇区 2
	 * 镇区报表必须要传areaId
	 */
	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {


	}



}
