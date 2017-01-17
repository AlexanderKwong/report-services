package zyj.report.service.export.hubei;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSegmentType;
import zyj.report.common.constant.EnmSubjectType;
import zyj.report.persistence.client.RptExpStudetScoreMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.MultiField;
import zyj.report.service.model.SegmentTemp.Segment;
import zyj.report.service.model.Sheet;
import zyj.report.service.model.SingleField;
import zyj.report.service.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/1.
 * <p>
 * 导出 湖北版 总分各分数段人数 服务
 */
@Service
public class ExpTotalScoreEachSegService extends BaseRptService {

	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;

	@Autowired
	BaseDataService baseDataService;

	@Value("${hb.score.step}")
	int step;

	private static String excelName = "总分各分数段人数";

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		// 设置 参数信息
		super.exportData(params);

		//校验参数,暂不校验cityCode
		if (p.getExamBatchId() == null || p.getPath() == null || p.getCityCode() == null)
			return;

		// 初始化 sheet
		List<Sheet> sheets = getSheets();

		// 初始化 excel
		Excel excel = new Excel(excelName + ".xls", p.getPath(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}

	/**
	 * 初始化 SingleFields
	 */
	private List<Field> getSingleFields() {

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField(excelName);

		//step1:加载固定标题
		for (String t : new String[]{"分数段,SCORE_SEG", "频数,FREQUENCY", "频率,FREQUENCY_CENT",
				"累计频数,ACC_FREQUENCY", "累计频率,ACC_FREQUENCY_CENT"}) {
			String[] args = t.split(",");
			root.add(new SingleField(args[0], args[1]));
		}

		fields.add(root);
		return fields;
	}

	/**
	 * 初始化 sheet
	 */
	private List<Sheet> getSheets() {

		// 初始化 fields
		List<Field> fields = getSingleFields();
		List<Sheet> sheets = new ArrayList<>();

		// 添加文科 sheet
		sheets.add(getSheet(EnmSubjectType.WEN, fields));

		// 添加理科 sheet
		sheets.add(getSheet(EnmSubjectType.LI, fields));

		return sheets;
	}

	/**
	 * 加载文理科数据
	 *
	 * @param subjectType
	 * @param fields
	 * @return
	 */
	private Sheet getSheet(EnmSubjectType subjectType, List<Field> fields) {

		Sheet sheet = new Sheet(subjectType.getCode() + "", subjectType.getName());
		Map conditions = new HashMap<String, Object>();
		conditions.put("exambatchId", p.getExamBatchId());
		conditions.put("cityCode", p.getCityCode());
		conditions.put("schoolId", p.getSchoolId());
		conditions.put("type", subjectType.getCode());
		conditions.put("stuType", p.getStuType());

		//读取源数据
		List<Map<String, Object>> data = rptExpStudetScoreMapper.findTotalScoreEachSegment(conditions);

		// 锁定表头2行
		sheet.setFreeze(2);

		// 加载 字段
		sheet.getFields().addAll(fields);

		// 加载 各行的字段的数据
		sheet.getData().addAll(getSegmentData(data));

		return sheet;
	}

	/**
	 * 将 总分数据 转化为 各分数段数据
	 *
	 * @param data
	 * @return
	 */
	private List<Map<String, Object>> getSegmentData(List<Map<String, Object>> data) {

		Float maxScore = Float.parseFloat(data.get(0).get("ALL_TOTAL").toString());

		Segment segment = new Segment(step, 0, maxScore, data.size(), EnmSegmentType.ROUNDED);

		return segment.getStepSegment(data, "ALL_TOTAL");
	}


}
