package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSegmentType;
import zyj.report.common.constant.EnmSubjectType;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpAllscoreMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.*;
import zyj.report.service.model.segment.Segment;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by CXinZhi on 2017-01-23.
 * <p()>
 * 导出 湖北版 总分各分数段人数_竖（班级） 服务
 */
@Service
public class ExpHBSchTotalScoreSegmentHorService extends BaseRptService {

	private static String excelName = "总分各分数段人数_竖";

	@Autowired
	BaseDataService baseDataService;

	@Autowired
	RptExpAllscoreMapper rptExpAllscoreMapper;



	@Value("${hb.score.10step}")
	Integer step;

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		super.initParam(params);

		// 初始化 sheet
		List<Sheet> sheets = getSheets();

		// 初始化 excel
		Excel excel = new Excel(excelName + ".xls", params.get("pathFile").toString(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}

	/**
	 * 初始化 Fields
	 */
	private Map<String,Object> getFields(EnmSubjectType type) throws ReportExportException {

		Map<String,Object> data = new HashMap<>();

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField(excelName);

		//step1:加载固定标题
		for (String t : new String[]{"班级,CLS_NAME", "应考人数,TAKE_EXAM_NUM", "均分,AVG_SCORE", "排名,RANK", "最高分,TOP_SCORE"}) {
			String[] args = t.split(",");
			root.add(new SingleField(args[0], args[1]));
		}

		Map<String, Object> params = new HashMap<>();
		params.put("exambatchId",p().getExamBatchId());
		params.put("schoolId",p().getSchoolId());
		params.put("type",type.getCode());



		//step2: 加载动态标题1 查最高分
		Float max = rptExpAllscoreMapper.qryStudentSubjectTopScore(params);

		//step2: 加载动态标题1
		int takeExamNum = rptExpAllscoreMapper.qryStudentSubjectCountScore(params);

		//生成分数段
		Segment segment = new Segment(step, 0, max, takeExamNum, EnmSegmentType.ROUNDED);

		List<String> segmentNames = segment.generateSegment();

		segmentNames.forEach(s -> {
			root.add(new SingleField(">=" + s.substring(1, s.length() - 1).split(",")[0], s));
		});

		params.put("segment", segment);

		fields.add(root);

		data.put("fields",fields);
		data.put("segment",segment);

		return data;
	}

	/**
	 * 获取 文理科 两个类型的 sheet
	 *
	 * @return
	 * @throws ReportExportException
	 */
	private List<Sheet> getSheets() throws ReportExportException {
		List<Sheet> sheets = new ArrayList<>();


		List<Map<String, Object>> subjects = baseDataService.getSubjectByExamid(p().getExamBatchId());

		if (EnmSubjectType.ALL.getCode() == Integer.parseInt(subjects.get(0).get("TYPE").toString())) {
			sheets.add(getSheet(EnmSubjectType.ALL));
		} else {
			// 文科
			sheets.add(getSheet(EnmSubjectType.WEN));

			// 理科
			sheets.add(getSheet(EnmSubjectType.LI));
		}




		return sheets;

	}

	/**
	 * 初始化 sheet
	 *
	 * @param type
	 */
	private Sheet getSheet(EnmSubjectType type) throws ReportExportException {

		Sheet sheet = new Sheet("", type.getName());
		Map<String,Object> data = getFields(type);
		sheet.setFields((List<Field>)data.get("fields"));

		Map<String, Object> params = new HashMap<>();
		params.put("exambatchId",p().getExamBatchId());
		params.put("schoolId",p().getSchoolId());
		params.put("type",type.getCode());

		//数据集1
		List<Map<String, Object>> clsSubjectInfo = rptExpAllscoreMapper.qryClassAllScoreInfo(params);

		//数据集2
		Segment segment = (Segment) data.get("segment");

		List<Map<String, Object>> result2 = baseDataService.getStudentSubjectsAndAllscore(p().getExamBatchId(), p().getSchoolId(), p().getLevel(),p().getStuType()).stream().filter(m-> type.getCode()== Integer.parseInt(m.get("TYPE").toString())).collect(Collectors
				.toList());

		List<Map<String, Object>> result3 = segment.getPartitionStepSegmentAccTransverse(result2, "ALL_SCORE",
				new String[]{"CLS_ID"});

		//关联
		List<Map<String, Object>> result = CollectionsUtil.leftjoinMapByKey(clsSubjectInfo, result3, "CLS_ID");

		CollectionsUtil.orderByIntValue(result, "RANK");


		sheet.getData().addAll(result);

		return sheet;
	}
}
