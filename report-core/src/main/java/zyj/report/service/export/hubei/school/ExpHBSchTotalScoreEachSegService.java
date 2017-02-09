package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSegmentType;
import zyj.report.common.constant.EnmSubjectType;
import zyj.report.persistence.client.RptExpStudetScoreMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.Excel;
import zyj.report.service.model.Sheet;
import zyj.report.service.model.report.RptTemplate;
import zyj.report.service.model.segment.Segment;
import zyj.report.service.model.segment.SegmentTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/1.
 * <p()>
 * 导出 湖北版 总分各分数段人数 服务
 */
@Service
public class ExpHBSchTotalScoreEachSegService extends BaseRptService {


	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;

	@Autowired
	BaseDataService baseDataService;

	@Value("${hb.score.10step}")
	public int step;

	private static String excelName = "总分各分数段人数";

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		// 设置 参数信息
		super.initParam(params);

		//校验参数
		if (p().getExamBatchId() == null || p().getPath() == null || p().getCityCode() == null)
			return;

		// 设置标题模板
		SegmentTemplate segmentTemplate = new SegmentTemplate(step);

		// 初始化 sheet
		List<Sheet> sheets = getSheets(segmentTemplate);

		// 初始化 excel
		Excel excel = new Excel(excelName + ".xls", p().getPath(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}

	/**
	 * 初始化 sheet
	 */
	public List<Sheet> getSheets(RptTemplate rptTemplate) {

		List<Sheet> sheets = new ArrayList<>();

		List<Map<String, Object>> subjects = baseDataService.getSubjectByExamid(p().getExamBatchId());

		if (EnmSubjectType.ALL.getCode() == Integer.parseInt(subjects.get(0).get("TYPE").toString())) {
			sheets.add(getSheet(EnmSubjectType.ALL,rptTemplate));
		} else {
			// 添加文科 sheet
			sheets.add(getSheet(EnmSubjectType.WEN, rptTemplate));

			// 添加理科 sheet
			sheets.add(getSheet(EnmSubjectType.LI, rptTemplate));
		}

		return sheets;
	}

	/**
	 * 加载文理科数据
	 *
	 * @param subjectType
	 * @param rptTemplate
	 * @return
	 */
	public Sheet getSheet(EnmSubjectType subjectType, RptTemplate rptTemplate) {

		Sheet sheet = new Sheet(subjectType.getCode() + "", subjectType.getName());

		Map conditions = new HashMap<String, Object>();
		conditions.put("exambatchId", p().getExamBatchId());
		conditions.put("cityCode", p().getCityCode());
		conditions.put("schoolId", p().getSchoolId());
		conditions.put("type", subjectType.getCode());
		conditions.put("stuType", p().getStuType());

		//读取源数据
		List<Map<String, Object>> data = rptExpStudetScoreMapper.findTotalScoreEachSegment(conditions);

		// 加载 字段
		sheet.getFields().addAll(rptTemplate.createTitle(excelName));

		// 加载 各行的字段的数据
		sheet.getData().addAll(getSegmentData(data, ((SegmentTemplate) rptTemplate).getStep(), EnmSegmentType
				.ROUNDED));

		return sheet;
	}

	/**
	 * 将 总分数据 转化为 各分数段数据
	 *
	 * @param data
	 * @return
	 */
	public List<Map<String, Object>> getSegmentData(List<Map<String, Object>> data, Integer step, EnmSegmentType type) {
		Float maxScore = Float.parseFloat(data.get(0).get("SIGN_TOTAL").toString());

		Segment segment = new Segment(step, 0, maxScore, data.size(), type);

		return segment.getStepSegment(data, "SIGN_TOTAL");
	}


}
