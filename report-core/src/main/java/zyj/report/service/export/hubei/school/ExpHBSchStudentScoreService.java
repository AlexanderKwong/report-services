package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.business.task.SubjectInfo;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by CXinZhi on 2017/1/1.
 * <p()>
 * 导出 湖北版 学生各科成绩和总分（班级） 服务
 */
@Service
public class ExpHBSchStudentScoreService extends BaseRptService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;

	@Autowired
	BaseDataService baseDataService;

	private static String excelName = "学生各科成绩和总分（%s）";

	@Override
	public void exportData(Map<String, Object> params) throws Exception {

		// 设置 参数信息
		super.initParam(params);

		//校验参数,暂不校验cityCode
		if (p().getExamBatchId() == null || p().getPath() == null || p().getLevel() == null)
			return;

		// 初始化 sheet
		List<Sheet> sheets = getSheet();

		// 初始化 excel
		Excel excel = new Excel(String.format(excelName, "班级") + ".xls", p().getPath(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}

	/**
	 * 初始化 Fields
	 */
	private List<Field> getFields(Map<String, Object> claModel, String rootName) {

		//获取 本次考试科目列表
		List<SubjectInfo> subjects = getSubjectList();

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField(rootName);

		root.add(new SingleField("考号", "SEQUENCE"));
		root.add(new SingleField("姓名", "NAME"));
		root.add(new SingleField("班级", "CLS_NAME"));

		subjects.forEach(model -> {
			if (claModel.get("CLS_TYPE").toString().equals(model.getType() + "")) {
				root.add(new SingleField(model.getSubjectName() + "分数", model.getSubjectName() + "_SCORE"));
				root.add(new SingleField(model.getSubjectName() + "班名", model.getSubjectName() + "_RANK_CLS"));
				root.add(new SingleField(model.getSubjectName() + "校名", model.getSubjectName() + "_RANK_SCH"));
			}
		});

		root.add(new SingleField("总分分数", "ALL_SCORE"));
		root.add(new SingleField("标准分", "ALL_RANK"));
		root.add(new SingleField("总分班名", "ALL_RANK_CLS"));
		root.add(new SingleField("总分校名", "ALL_RANK_SCH"));

		fields.add(root);

		return fields;
	}

	/**
	 * 初始化 sheet
	 */
	private List<Sheet> getSheet() {

		//获取 本次考试科目列表
		List<SubjectInfo> subjectList = getSubjectList();

		// 查询出当前有多少个班级参与
		List<Map<String, Object>> classList = baseDataService.getClassesInSchool(p().getExamBatchId(),p()
				.getSchoolId());

		List<Sheet> sheets = new ArrayList<>();

		Map conditions = new HashMap<String, Object>();

		// 查区内的学生列表
		conditions.put("exambatchId", p().getExamBatchId());
		conditions.put("cityCode", p().getCityCode());
		conditions.put("subjectList", subjectList);
		conditions.put("stuType", p().getStuType());

		// 加载 各个班级的 sheet
		classList.forEach(model -> {

			Sheet sheet = new Sheet(model.get("CLS_ID").toString(), model.get("CLS_NAME").toString());

			sheet.getFields().addAll(getFields(model, String
					.format(excelName, model.get("CLS_NAME").toString())));

			conditions.put("classesId", p().getClassesId());
			List<Map<String, Object>> data = baseDataService.getStudentSubjectsAndAllscore(p().getExamBatchId(),
					model.get("CLS_ID").toString(), "classes", p().getStuType());

			zyj.report.common.CalToolUtil.sortByIndexValue2(data, "ALL_RANK");

			sheet.getData().addAll(data);

			sheets.add(sheet);
		});

		return sheets;
	}

	/**
	 * 获取 本次考试科目列表
	 *
	 * @return
	 */
	private List<SubjectInfo> getSubjectList() {

		try{
			List<Map<String, Object>> subjects_cur = baseDataService.getSubjectByExamid(p().getExamBatchId());

			//产生查询考试科目列表
			List<SubjectInfo> subjectList = subjects_cur.stream().map(subject -> new SubjectInfo(subject.get
					("PAPER_ID").toString(), subject.get("SUBJECT").toString(), subject.get("SUBJECT_NAME")
					.toString(), Integer.parseInt(subject.get("TYPE").toString()))).sorted(
					(subject2, subject1) -> {
						return zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject1.getSubject()) - zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(), subject2.getSubject());
					}).collect(Collectors.toList());

			return subjectList;

		}
		catch (Exception e){
			throw e;
		}

	}


}
