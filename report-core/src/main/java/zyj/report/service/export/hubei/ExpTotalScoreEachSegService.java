package zyj.report.service.export.hubei;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSubjectType;
import zyj.report.persistence.client.RptExpStudetScoreMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.Excel;
import zyj.report.service.model.Field;
import zyj.report.service.model.Sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 导出 湖北版 总分各分数段人数 服务
 */
@Service
public class ExpTotalScoreEachSegService extends BaseRptService {

	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;

	@Autowired
	BaseDataService baseDataService;

	private static String excelName = "总分各分数段人数";

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		// 设置 参数信息
		super.exportData(params);

		//校验参数,暂不校验cityCode
		if (p.getExamBatchId() == null || p.getPath() == null || p.getCityCode() == null)
			return;

		//读取源数据
		List<Map<String, Object>> data = rptExpStudetScoreMapper.findRptExpStudetAllScore(params);

		// 初始化 sheet
		List<Sheet> sheets = getSheets(data);

		// 初始化 excel
		Excel excel = new Excel(excelName + ".xls", p.getPath(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}

	/**
	 * 初始化 Fields
	 */
	private List<Field> getFields() {
		List<Field> fields = new ArrayList<>();
		fields.add(new Field("SCORE_SEG", "分数段"));
		fields.add(new Field("FREQUENCY", "频数"));
		fields.add(new Field("FREQUENCY_CENT", "频率"));
		fields.add(new Field("ACCUM_FREQUENCY", "累计频数"));
		fields.add(new Field("ACCUM_FREQUENCY_CENT", "累计频率"));

		return fields;
	}

	/**
	 * 初始化 sheet
	 */
	private List<Sheet> getSheets(List<Map<String, Object>> data) {

		// 初始化 fields
		List<Field> fields = getFields();

		List<Sheet> sheets = new ArrayList<>();

		sheets.add(getSheet(EnmSubjectType.LI, fields, data));
		sheets.add(getSheet(EnmSubjectType.WEN, fields, data));

		return sheets;
	}

	/**
	 * 加载文理科数据
	 *
	 * @param enmSubjectType
	 * @param fields
	 * @return
	 */
	private Sheet getSheet(EnmSubjectType enmSubjectType, List<Field> fields, List<Map<String, Object>> data) {

		Sheet sheet = new Sheet(enmSubjectType.getCode() + "", enmSubjectType.getName(), excelName);

		// 锁定表头2行
		sheet.setFreeze(2);

		// 加载 字段
		sheet.getFields().addAll(fields);

		// 加载 各行的字段的数据
		sheet.getData().addAll(data);

		return sheet;
	}


}
