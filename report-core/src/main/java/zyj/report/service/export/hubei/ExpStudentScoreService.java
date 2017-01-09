package zyj.report.service.export.hubei;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.business.task.SubjectInfo;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.Excel;
import zyj.report.service.model.Field;
import zyj.report.service.model.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导出 湖北版 学生各科成绩和总分（班级） 服务
 */
@Service
public class ExpStudentScoreService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;

	@Autowired
	BaseDataService baseDataService;

	private static String excelName = "学生各科成绩和总分（%s)";

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		// 设置 参数信息
		super.exportData(params);

		//校验参数,暂不校验cityCode
		if (p.getExamBatchId() == null || p.getPath() == null || p.getLevel() == null)
			return;

		// 查询出当前有多少个班级参与
		List<Map<String, Object>> classList = jyjRptExtMapper.qryClassesInfo(p.getExamBatchId());

		// 初始化 filed
		List<Field> fields = getFields(classList);

		// 初始化 sheet
		List<Sheet> sheets = getSheet(fields, classList);

		// 初始化 excel
		Excel excel = new Excel(String.format(excelName, "班级") + ".xls", p.getPath(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}

	/**
	 * 初始化 Fields
	 */
	private List<Field> getFields(List<Map<String, Object>> classList) {

		//获取 本次考试科目列表
		List<SubjectInfo> subjects = getSubjectList(p.getExamBatchId());

		List<Field> fields = new ArrayList<>();

		fields.add(new Field("考号", "SEQUENCE", "考号"));
		fields.add(new Field("姓名", "NAME", "姓名"));
		fields.add(new Field("班级", "CLS_NAME", "班级"));
		fields.add(new Field("区县", "AREA_NAME", "区县"));

		classList.forEach(claModel -> {

			subjects.forEach(model -> {

				if (claModel.get("TYPE").toString().equals(model.getType())) {

					fields.add(new Field(model.getSubjectName() + "分数", model.getSubjectName() + "_SCORE", model.getSubjectName() + "分数"));
					fields.add(new Field(model.getSubjectName() + "班名", model.getSubjectName() + "_RANK_CLS", model.getSubjectName() + "班名"));
					fields.add(new Field(model.getSubjectName() + "校名", model.getSubjectName() + "_RANK_SCH", model.getSubjectName() + "校名"));
				}
			});
		});

		fields.add(new Field("总分分数", "ALL_SCORE", "总分分数"));
		fields.add(new Field("标准分", "ALL_RANK", "标准分"));
		fields.add(new Field("总分班名", "ALL_RANK_CLS", "总分班名"));
		fields.add(new Field("总分校名", "ALL_RANK_SCH", "总分校名"));

		return fields;
	}

	/**
	 * 初始化 sheet
	 *
	 * @param fields
	 */
	private List<Sheet> getSheet(List<Field> fields, List<Map<String, Object>> classList) {

		//获取 本次考试科目列表
		List<SubjectInfo> subjectList = getSubjectList(p.getExamBatchId());

		List<Sheet> sheets = new ArrayList<>();


		Map conditions = new HashMap<String, Object>();

		// 查区内的学生列表
		conditions.put("exambatchId", p.getExamBatchId());
		conditions.put("cityCode", p.getCityCode());
		conditions.put("subjectList", subjectList);
		conditions.put("stuType", p.getStuType());

		// 加载 各个班级的 sheet
		classList.forEach(model -> {

			Sheet sheet = new Sheet(model.get("CLS_ID").toString(), model.get("CLS_NAME").toString(), String
					.format(excelName, model.get("CLS_NAME").toString()));

			sheet.getFields().addAll(fields);

			// 锁定表头2行
			sheet.setFreeze(2);

			conditions.put("classesId", p.getClassesId());
			List<Map<String, Object>> data = baseDataService.getStudentSubjectsAndAllscore(p.getExamBatchId(),
					model.get("CLS_ID").toString(), p.getLevel(), p.getStuType());

			zyj.report.common.CalToolUtil.sortByIndexValue2(data, "ALL_RANK");

			sheet.getData().addAll(data);

			sheets.add(sheet);
		});

		return sheets;
	}

	/**
	 * 获取 本次考试科目列表
	 *
	 * @param batchId
	 * @return
	 */
	private List<SubjectInfo> getSubjectList(String batchId) {

		List<Map<String, Object>> subjects_cur = baseDataService.getSubjectByExamid(p.getExamBatchId());

		//产生查询考试科目列表
		List<SubjectInfo> subjectList = subjects_cur.stream().map(subject -> new SubjectInfo(subject.get
				("PAPER_ID").toString(), subject.get("SUBJECT").toString(), subject.get("SUBJECT_NAME").toString(), (Integer) subject.get("TYPE"))).sorted(
				(subject1, subject2) -> {
					return zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject1.getSubject()) - zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject2.getSubject());
				}).collect(Collectors.toList());

		return subjectList;
	}


}
