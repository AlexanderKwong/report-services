package zyj.report.service.export.hubei.school;

import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmQuestionType;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by CXinZhi on 2017/2/10.
 * <p>
 * 导出 湖北版 每题得分情况（含各班） 服务
 */
@Service
public class ExpHBSchEachQuestionScoreService extends BaseRptService {

	private static String excelName = "每题得分情况（含各班）";

	private static String titleName = "每题得分情况（%s）";

	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;

	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;

	@Autowired
	BaseDataService baseDataService;

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		super.initParam(params);

		// 初始化 sheet
		List<MultiSheet> sheets = getSheets(params);

		// 初始化 excel
		MultiExcel excel = new MultiExcel(excelName + ".xls", params.get("pathFile").toString(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);
	}

	/**
	 * 初始化 sheet
	 */
	private List<MultiSheet> getSheets(Map<String, Object> params) throws ReportExportException {


		List<MultiSheet> sheets = new ArrayList<>();

		// 查询出当前有多少个班级参与
		List<Map<String, Object>> classList = baseDataService.getClassesInSchool(p().getExamBatchId(), p()
				.getSchoolId()).stream().filter(c -> (Integer.parseInt(params.get("type").toString()) == 3 || Integer.parseInt(c.get("CLS_TYPE").toString()) == Integer.parseInt(params.get("type").toString()))).collect(Collectors.toList());

		//02/14 add by kwong +++++++++++++++ 不在循环中查询
		Map condition = new HashMap(params);
		condition.put("level", "classes");

		//科目全卷数据
		List<Map<String, Object>> subject =  getPaperSheetData(condition);

		// 题目数据
		List<Map<String, Object>> questions = rptExpQuestionMapper.findRptExpQuestion(condition);

		// 题目的选择
		List<Map<String, Object>> questionItems = rptExpQuestionMapper.findRptExpQuestionItem(condition);

		//客观题 为 空、对、缺失、错误的 统计数
		condition.put("GroupBy", "cls_id");

		// 子题目数据
		List<Map<String, Object>> subQuestions = rptExpQuestionMapper.qryQuestionSubScore(condition);

		List<Map<String, Object>> objectiveNullRightMissWrongs = rptExpQuestionMapper.qryObjectiveNullRightMissWrong(condition);
		if (questions.isEmpty() || questionItems.isEmpty() || objectiveNullRightMissWrongs.isEmpty() || subject.isEmpty()) {
			throw new ReportExportException("没有查到源数据，请核查！");
		}

		Map<String, List<Map<String, Object>>> questionPartition = CollectionsUtil.partitionBy(questions, new String[]{"CLS_ID"});

		Map<String, List<Map<String, Object>>> subjectPartition = CollectionsUtil.partitionBy(subject, new String[]{"CLS_ID"});

		// 子题目数据 各个班级
		Map<String, List<Map<String, Object>>> subQuestionPartition = CollectionsUtil.partitionBy(subQuestions, new String[]{"CLS_ID"});

		Map<String, List<Map<String, Object>>> questionItemPartition = CollectionsUtil.partitionBy(questionItems, new String[]{"CLS_ID"});

		Map<String, List<Map<String, Object>>> objectivePartition = CollectionsUtil.partitionBy(objectiveNullRightMissWrongs, new String[]{"CLS_ID"});



		for (Map<String, Object> model : classList) {
			String clsId = model.get("CLS_ID").toString();
			MultiSheet sheet = new MultiSheet(clsId, model.get("CLS_NAME").toString());

			// 获取班级 所有班级 题目数据
			List<Map<String, Object>> question = questionPartition.get(clsId);

			// 获取班级 所有班级 子题目数据
			List<Map<String, Object>> subQuestion = new ArrayList<>();

			if (subQuestionPartition.size() > 0) {
				subQuestion = subQuestionPartition.get(clsId).stream().map(m -> {
					Map<String, Object> map = new HashMap<>();
					String[] nos = m.get("QUESTION_NO").toString().split("_");
					map.put("QUESTION_ORDER", m.get("QUESTION_ORDER"));
					map.put("QUESTION_NO", nos[0]);
					map.put("SUB_NO", nos[nos.length-1]);
					map.put("AVG_SCORE", m.get("AVG_SCORE"));
					map.put("QST_SCORE", m.get("QST_SCORE"));
					map.put("DIFFICULTY_NUM", m.get("DIFFICULTY_NUM"));
					map.put("STAND_POOR", m.get("STAND_POOR"));
					map.put("QST_TIPY", EnmQuestionType.SUBJECTIVITY.getCode());
					return map;
				}).collect(Collectors.toList());
			}

			// 获取班级 所有班级 题目选项
			List<Map<String, Object>> questionItem = questionItemPartition.get(clsId);

			// 获取班级 所有班级 题目选项
			List<Map<String, Object>> objectiveNullRightMissWrong = objectivePartition.get(clsId);

			if (question == null || questionItem == null || objectiveNullRightMissWrong == null) {
				System.out.println("Warn: 没有找到对应的班级数据，跳过");
				continue;
			}

			CollectionsUtil.orderByDoubleValue(question, "QUESTION_ORDER");
			CollectionsUtil.orderByStringValue(subQuestion, "SUB_NO");

			// 添加客观题数据
			sheet.getSheets().add(getObjectiveSheet(params, question.stream().filter(m -> Integer.valueOf(m.get("QST_TIPY").toString()) != 4).collect(Collectors.toList()), questionItem.stream().filter(m -> Integer.valueOf(m.get("QST_TIPY").toString()) != 4).collect(Collectors.toList()), objectiveNullRightMissWrong));

			question.addAll(subQuestion);
			CollectionsUtil.orderByDoubleValue(question, "QUESTION_ORDER");

			// 添加主观提数据
			sheet.getSheets().add(getSubjectiveSheet(params, question.stream().filter(m -> Integer.valueOf(m.get
					("QST_TIPY").toString()) == 4).collect(Collectors.toList())));

			// 添加试卷整体分析
			sheet.getSheets().add(getPaperSheet(subjectPartition.get(clsId)));

			sheets.add(sheet);

		}

		return sheets;
	}

	/**
	 * 获得客观题的sheet
	 *
	 * @param params
	 * @param question     客观题
	 * @param questionitem 客观题选项
	 * @return
	 * @throws ReportExportException
	 */
	private Sheet getObjectiveSheet(Map<String, Object> params, List<Map<String, Object>> question, List<Map<String, Object>> questionitem, List<Map<String, Object>> objectiveNullRightMissWrong) throws ReportExportException {

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField("客观题");

		//step1:加载固定标题
		addRegularFields(root, new String[]{"题目名称,QUESTION_NO", "平均分,AVG_SCORE", "难度,DIFFICULTY_NUM", "区分度,DIS_DEGREE", "标准差,STAND_POOR", "空白人数,NULL_NUM", "漏涂人数,MISS_NUM", "错涂人数,WRONG_NUM", "正确人数,RIGHT_NUM"});

		//step2:加载动态标题
		Map<String, Map<String, Object>> questionitemtrans = zyj.report.common.CalToolUtil.trans(questionitem, new String[]{"EXAMBATCH_ID", "SUBJECT", "QUESTION_ORDER"});

		Map<String, Map<String, Object>> nullRightMissWrongTrans = zyj.report.common.CalToolUtil.trans(objectiveNullRightMissWrong, new String[]{"EXAMBATCH_ID", "SUBJECT", "QUESTION_ORDER"});

		//报表输出数据
		List<Map<String, Object>> resdata = question;

		Set<String> chosen = new HashSet<>();
		for (Map<String, Object> m : resdata) {

			String exambatchId = ObjectUtils.toString(m.get("EXAMBATCH_ID"));
			String subject = ObjectUtils.toString(m.get("SUBJECT"));
			String questionOrder = ObjectUtils.toString(m.get("QUESTION_ORDER"));
			String k = exambatchId + subject + questionOrder;

			Map<String, Object> item = questionitemtrans.get(k);
			m.putAll(nullRightMissWrongTrans.get(k));
			if (item != null) {
				String opt = ObjectUtils.toString(item.get("OPT_DETAIL"));
				if (StringUtils.isNotBlank(opt)) {
					try {
						JSONObject optJson = JSONObject.fromString(opt);
						String[] t1 = zyj.report.common.CalToolUtil.getAllCombination(1);
						String[] t2 = zyj.report.common.CalToolUtil.getAllCombination(2);
						for (int i = 0; i < 23; i++) {
							String value = getValue(optJson, t2[i]);
							if (value.equals("0")) {
								m.put(t1[i], "");
							} else {
								m.put(t1[i], value);
								chosen.add(t1[i]);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		List<String> chosenList = new ArrayList<String>(chosen);
		CalToolUtil.sortByValue(chosenList, CalToolUtil.getAllCombination(1));

		for (String c : chosenList)
			root.add(new SingleField(String.format("选%s人数", c), c));

		fields.add(root);

		Sheet sheet = new Sheet("objective", "客观题");

		sheet.getData().addAll(resdata);

		sheet.setFields(fields);

		return sheet;
	}

	/**
	 * 获得主观题的sheet
	 *
	 * @param params
	 * @param question 主观题
	 * @return
	 * @throws ReportExportException
	 */
	private Sheet getSubjectiveSheet(Map<String, Object> params, List<Map<String, Object>> question) throws ReportExportException {

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField("主观题");

		//step1:加载固定标题
		addRegularFields(root, new String[]{"题目名称,QUESTION_NO", "小题,SUB_NO", "满分,QST_SCORE", "均分,AVG_SCORE", "难度,DIFFICULTY_NUM", "区分度,DIS_DEGREE", "标准差,STAND_POOR"});

		fields.add(root);

		Sheet sheet = new Sheet("subjective", "主观题");
		sheet.getData().addAll(question);
		sheet.setFields(fields);

		return sheet;
	}

	/**
	 * 获取全卷的sheet的数据DATA
	 *
	 * @param condition
	 * @return
	 * @throws ReportExportException
	 */
	private List<Map<String,Object>> getPaperSheetData(Map<String, Object> condition) throws ReportExportException {

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField("全卷");

		//step1:加载固定标题
		addRegularFields(root, new String[]{"最高分,TOP_SCORE", "最低分,UP_SCORE", "全距,DISTANCE", "均分,AVG_SCORE", "优秀人数,LEVEL_GD_NUM", "及格人数,LEVEL_PS_NUM", "众数,MODELS", "区分度,STU_SCORE_DIFFERENT", "难度,STU_SCORE_DIFFICUT", "信度,STU_SCORE_RELIABILITY", "标准差,STU_SCORE_SD"});

		fields.add(root);

		Map params = new HashMap(condition);
		params.put("level", "classes");

		List<Map<String, Object>> data = rptExpSubjectMapper.findRptExpSubject(params);


		if (data.isEmpty())
			throw new ReportExportException("没有查到源数据，请核查！");

		// 全距
		Consumer<Map> addDistance = m -> m.put("DISTANCE", Float.valueOf(m.get("TOP_SCORE").toString()) -
				Float.valueOf(m.get("UP_SCORE").toString()));

		params.put("level", "city");

		List<Map<String, Object>> cityData = rptExpSubjectMapper.findRptExpSubject(params);

		String difficuty = cityData.get(0).get("STU_SCORE_DIFFICUT").toString();
		String reliability = cityData.get(0).get("STU_SCORE_RELIABILITY").toString();
		String discrimination = cityData.get(0).get("STU_SCORE_DIFFERENT").toString();

		// 难度
		Consumer<Map> addDifficuty = m -> m.put("STU_SCORE_DIFFICUT", difficuty);

		// 信度
		Consumer<Map> addReliability = m -> m.put("STU_SCORE_RELIABILITY", reliability);

		//区分度
		Consumer<Map> addDiscrimination = m -> m.put("STU_SCORE_DIFFERENT", discrimination);

		Consumer<Map> doAll = m -> {
			addDistance.accept(m);
			addDifficuty.accept(m);
			addReliability.accept(m);
			addDiscrimination.accept(m);
		};

		data.forEach(doAll);

		return data;
	}

	/**
	 * 获取全卷的sheet
	 *
	 * @param data
	 * @return
	 * @throws ReportExportException
	 */
	private Sheet getPaperSheet(List<Map<String, Object>> data) throws ReportExportException {

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField("试卷整体分析（全卷）");

		//step1:加载固定标题
		addRegularFields(root, new String[]{"最高分,TOP_SCORE", "最低分,UP_SCORE", "全距,DISTANCE", "均分,AVG_SCORE", "优秀人数,LEVEL_GD_NUM", "及格人数,LEVEL_PS_NUM", "众数,MODELS", "区分度,STU_SCORE_DIFFERENT", "难度,STU_SCORE_DIFFICUT", "信度,STU_SCORE_RELIABILITY", "标准差,STU_SCORE_SD"});

		fields.add(root);

		Sheet sheet = new Sheet("paper", "全卷");
		sheet.getData().addAll(data);
		sheet.setFields(fields);

		return sheet;
	}

	private void addRegularFields(MultiField root, String[] titleAndMark) {

		for (String t : titleAndMark) {
			String[] args = t.split(",");
			root.add(new SingleField(args[0], args[1]));
		}
	}

	private String getValue(JSONObject optJson, String key) {
		if (optJson.has(key)) {
			return optJson.getString(key);
		}
		return "0";
	}

}
