package zyj.report.service.export.hubei.school;

import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmQuestionType;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 试卷整体分析 服务
 * @date 2017/1/12
 */
@Service
public class ExpHBSchPaperStatisticService extends BaseRptService {

	private static String excelName = "试卷整体分析";

	@Value("${hubei.subject.score.step}")
	int step;

	@Autowired
	RptExpQuestionMapper rptExpQuestionMapper;
	@Autowired
	RptExpSubjectMapper rptExpSubjectMapper;

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		// 初始化 sheet
		List<Sheet> sheets = getSheets(params);

		// 初始化 excel
		Excel excel = new Excel(excelName + ".xls", params.get("pathFile").toString(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);
	}

	/**
	 * 初始化 sheet
	 */
	private List<Sheet> getSheets(Map<String, Object> params) throws ReportExportException {

		List<Sheet> sheets = new ArrayList<>();

		sheets.addAll(getQuestionSheets(params));

		sheets.add(getPaperSheet(params));

		return sheets;
	}

	/**
	 * 增加题目的统计，包括主观题和客观题
	 *
	 * @param params
	 * @return
	 * @throws ReportExportException
	 */
	private List<Sheet> getQuestionSheets(Map<String, Object> params) throws ReportExportException {

		List<Sheet> sheets = new ArrayList<>();
		//读取源数据
		Map condition = new HashMap(params);
//        condition.put("level","classes");
		List<Map<String, Object>> question = rptExpQuestionMapper.findRptExpQuestion(condition);
		List<Map<String, Object>> questionitem = rptExpQuestionMapper.findRptExpQuestionItem(condition);

		// 子题目数据
		List<Map<String, Object>> subQuestions = rptExpQuestionMapper.qryQuestionSubScore(condition);

		// 获取班级 所有班级 子题目数据
		List<Map<String, Object>> subQuestion = subQuestions.stream().map(m -> {
			Map<String, Object> map = new HashMap<>();
			String[] nos = m.get("QUESTION_NO").toString().split("_");
			map.put("QUESTION_NO", nos[0]);
			map.put("SUB_NO", nos[1]);
			map.put("AVG_SCORE", m.get("AVG_SCORE"));
			map.put("QST_SCORE", m.get("QST_SCORE"));
			map.put("QST_TIPY", EnmQuestionType.SUBJECTIVITY.getCode());
			return map;
		}).collect(Collectors.toList());

		condition.put("GroupBy", "sch_id");
		List<Map<String, Object>> objectiveNullRightMissWrong = rptExpQuestionMapper.qryObjectiveNullRightMissWrong(condition);
		if (question.isEmpty() || questionitem.isEmpty())
			throw new ReportExportException("没有查到源数据，请核查！");


		sheets.add(getObjectiveSheet(params, question.stream().filter(m -> Integer.valueOf(m.get("QST_TIPY").toString()) != 4).collect(Collectors.toList()), questionitem.stream().filter(m -> Integer.valueOf(m.get("QST_TIPY").toString()) != 4).collect(Collectors.toList()), objectiveNullRightMissWrong));

		CollectionsUtil.orderByStringValue(subQuestion, "SUB_NO");
		question.addAll(subQuestion);
		CollectionsUtil.orderByStringValue(question, "QUESTION_NO");

		sheets.add(getSubjectiveSheet(params, question.stream().filter(m -> Integer.valueOf(m.get("QST_TIPY").toString()) == 4).collect(Collectors.toList())));

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

		MultiField root = new MultiField("试卷整体分析（客观题）");

		//step1:加载固定标题
		addRegularFields(root, new String[]{"题目名称,QUESTION_NO", "平均分,AVG_SCORE", "难度,DIFFICULTY_NUM", "区分度,DIS_DEGREE", "标准差,STAND_POOR", "空白人数,NULL_NUM", "漏涂人数,MISS_NUM", "错涂人数,WRONG_NUM", "正确人数,RIGHT_NUM"});
		//step2:加载动态标题

		Map<String, Map<String, Object>> questionitemtrans = zyj.report.common.CalToolUtil.trans(questionitem, new String[]{"EXAMBATCH_ID", "SUBJECT", "QUESTION_ORDER"});
		Map<String, Map<String, Object>> ullRightMissWrongTrans = zyj.report.common.CalToolUtil.trans(objectiveNullRightMissWrong, new String[]{"EXAMBATCH_ID", "SUBJECT", "QUESTION_ORDER"});
//报表输出数据
		List<Map<String, Object>> resdata = question;
		Set<String> chosen = new HashSet<>();
		for (Map<String, Object> m : resdata) {
			String exambatchId = ObjectUtils.toString(m.get("EXAMBATCH_ID"));
			String subject = ObjectUtils.toString(m.get("SUBJECT"));
			String questionOrder = ObjectUtils.toString(m.get("QUESTION_ORDER"));
//            double takeExamNum = Double.parseDouble(ObjectUtils.toString(m.get("TAKE_EXAM_NUM")));
			String k = exambatchId + subject + questionOrder;

			Map<String, Object> item = questionitemtrans.get(k);
			m.putAll(ullRightMissWrongTrans.get(k));
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
		for (String c : chosenList) root.add(new SingleField(String.format("选%s人数", c), c));

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

		MultiField root = new MultiField("试卷整体分析（主观题）");

		//step1:加载固定标题
		addRegularFields(root, new String[]{"题目名称,QUESTION_NO",/*"小题,",*/"满分,QST_SCORE", "均分,AVG_SCORE", "难度,DIFFICULTY_NUM", "区分度,DIS_DEGREE", "标准差,STAND_POOR"});

		fields.add(root);

		Sheet sheet = new Sheet("subjective", "主观题");
		sheet.getData().addAll(question);
		sheet.setFields(fields);

		return sheet;
	}

	/**
	 * 获取全卷的sheet
	 *
	 * @param params
	 * @return
	 * @throws ReportExportException
	 */
	private Sheet getPaperSheet(Map<String, Object> params) throws ReportExportException {

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField("试卷整体分析（全卷）");

		//step1:加载固定标题
		addRegularFields(root, new String[]{"最高分,TOP_SCORE", "最低分,UP_SCORE", "全距,DISTANCE", "均分,AVG_SCORE", "优秀人数,LEVEL_GD_NUM", "及格人数,LEVEL_PS_NUM", "众数,MODELS", "难度,STU_SCORE_DIFFICUT", "信度,STU_SCORE_RELIABILITY", "标准差,STU_SCORE_SD"});

		fields.add(root);

		List<Map<String, Object>> data = rptExpSubjectMapper.findRptExpSubject(params);

		if (data.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

		Consumer<Map> addDistance = m -> m.put("DISTANCE", CalToolUtil.decimalFormat2(Float.valueOf(m.get("TOP_SCORE").toString()) - Float.valueOf(m.get("UP_SCORE").toString())));

		params.put("level", "city");
		List<Map<String, Object>> cityData = rptExpSubjectMapper.findRptExpSubject(params);
		String difficuty = cityData.get(0).get("STU_SCORE_DIFFICUT").toString();
		String reliability = cityData.get(0).get("STU_SCORE_RELIABILITY").toString();
		Consumer<Map> addDifficuty = m -> m.put("STU_SCORE_DIFFICUT", difficuty);
		Consumer<Map> addReliability = m -> m.put("STU_SCORE_RELIABILITY", reliability);

		Consumer<Map> doAll = m -> {
			addDistance.accept(m);
			addDifficuty.accept(m);
			addReliability.accept(m);
		};

		data.forEach(doAll);

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
