package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSegmentType;
import zyj.report.common.constant.EnmSubjectType;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpStudetScoreMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.Excel;
import zyj.report.service.model.Sheet;
import zyj.report.service.model.report.RptTemplate;
import zyj.report.service.model.segment.Segment;
import zyj.report.service.model.segment.SegmentTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by CXinZhi on 2017/1/1.
 * <p()>
 * 导出 湖北版 总分各分数段人数_横 服务
 */
@Service
public class ExpHBSchTotalScoreEachSegVerService extends BaseRptService {

	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;

	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;

	@Autowired
	BaseDataService baseDataService;

	@Value("${hb.score.10step}")
	public int step;

	private static String excelName = "总分各分数段人数_横";

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
		Excel excel = new Excel(excelName + "（含各班）.xls", p().getPath(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}

	/**
	 * 初始化 sheet
	 */
	public List<Sheet> getSheets(RptTemplate rptTemplate) {

		List<Sheet> sheets = new ArrayList<>();

		// 查询出当前有多少个班级参与
		List<Map<String, Object>> classList = baseDataService.getClassesInSchool(p().getExamBatchId(),p().getSchoolId());

		List<Map<String, Object>> subjects = baseDataService.getSubjectByExamid(p().getExamBatchId());
		if (EnmSubjectType.ALL.getCode() == Integer.parseInt(subjects.get(0).get("TYPE").toString())) {
			sheets.add(getSheet(EnmSubjectType.ALL,rptTemplate, classList));
		} else {
			// 添加文科 sheet
			sheets.add(getSheet(EnmSubjectType.WEN, rptTemplate, classList));

			// 添加理科 sheet
			sheets.add(getSheet(EnmSubjectType.LI, rptTemplate, classList));
		}

		return sheets;
	}


	/**
	 * 加载文理科数据
	 *
	 * @param type
	 * @param rptTemplate
	 * @return
	 */
	public Sheet getSheet(EnmSubjectType type, RptTemplate rptTemplate, List<Map<String, Object>> classList) {

		Sheet sheet = new Sheet(type.getCode() + "", type.getName());

		Map conditions = new HashMap<String, Object>();
		conditions.put("exambatchId", p().getExamBatchId());
		conditions.put("cityCode", p().getCityCode());
		conditions.put("schoolId", p().getSchoolId());
		conditions.put("type", type.getCode());
		conditions.put("stuType", p().getStuType());

		//读取源数据
		List<Map<String, Object>> data = rptExpStudetScoreMapper.findTotalScoreEachSegment(conditions);

		// 加载 字段
		sheet.getFields().addAll(rptTemplate.createTitle(excelName, filterClassListByType(classList, type)));

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
	public List<Map<String, Object>> getSegmentData(List<Map<String, Object>> data, Integer step, EnmSegmentType
			type) {

		//拿到总分
		Float maxScore = Float.parseFloat(data.get(0).get("ALL_TOTAL").toString());

		//汇总分数段
		Segment segment = new Segment(step, 0, maxScore, data.size(), type);

		//学校汇总
		List<Map<String, Object>> result = segment.getStepSegment(data,"ALL_TOTAL");

		List<Map<String, Object>> result2 = segment.getPartitionStepSegmentVertical2(data, "ALL_TOTAL", new String[]{"CLS_ID"},new String[]{"FREQUENCY","ACC_FREQUENCY"});
		result = CollectionsUtil.leftjoinMapByKey(result, result2, "SCORE_SEG");
		CollectionsUtil.orderByIntValueDesc(result, "index");

		return result;

	}

	/**
	 * 对 班级列表进行 文理科目类别进行 过滤
	 *
	 * @param classList
	 * @param type
	 * @return
	 */
	private List<Map<String, Object>> filterClassListByType(List<Map<String, Object>> classList,
															EnmSubjectType type) {
		return classList.stream().filter(m -> {
			if (type.getCode().toString().equals(m.get("CLS_TYPE").toString()))
				return true;
			return false;

		}).collect(Collectors.toList());
	}


}
