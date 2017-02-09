package zyj.report.service.export.hubei.city;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSegmentType;
import zyj.report.common.constant.EnmSubjectType;
import zyj.report.persistence.client.RptExpStudetScoreMapper;
import zyj.report.service.export.hubei.school.ExpHBSchTotalScoreEachSegService;
import zyj.report.service.model.Excel;
import zyj.report.service.model.Sheet;
import zyj.report.service.model.report.RptTemplate;
import zyj.report.service.model.segment.SegmentTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/17.
 * <p()>
 * 全市报表 -- 科目 总分个分数段
 */
@Service
public class ExpHBCitySubTotalScoreEachSegService extends ExpHBSchTotalScoreEachSegService {

	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;

	@Value("${hb.score.1step}")
	int step;

	private static String excelName = "总分各分数段人数";

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		// 设置 参数信息
		super.initParam(params);

		//校验参数,暂不校验cityCode
		if (p().getExamBatchId() == null || p().getPath() == null || p().getCityCode() == null)
			return;

		RptTemplate rptTemplate = new SegmentTemplate(step);

		// 初始化 sheet
		List<Sheet> sheets = getSheets(rptTemplate);

		// 初始化 excel
		Excel excel = new Excel(excelName + ".xls", p().getPath(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}


	@Override
	public Sheet getSheet(EnmSubjectType subjectType, RptTemplate rptTemplate) {

		Sheet sheet = new Sheet(subjectType.getCode() + "", subjectType.getName());
		Map conditions = new HashMap<String, Object>();

		conditions.put("exambatchId", p().getExamBatchId());
		conditions.put("cityCode", p().getCityCode());
		conditions.put("type", subjectType.getCode());
		conditions.put("stuType", p().getStuType());

		//读取源数据
		List<Map<String, Object>> data = rptExpStudetScoreMapper.findTotalScoreEachSegment(conditions);

		// 加载 字段
		sheet.getFields().addAll(rptTemplate.createTitle(excelName));

		// 加载 各行的字段的数据
		sheet.getData().addAll(getSegmentData(data, ((SegmentTemplate) rptTemplate).getStep(),
				EnmSegmentType.ROUNDED));

		return sheet;
	}


}
