package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.business.task.SubjectInfo;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSubjectType;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by CXinZhi on 2017/2/4.
 *
 * 导出 湖北版 总分排名（含各科名次） 服务
 *
 */
@Service
public class ExpHBSchAllScoreRankHaveSubService extends BaseRptService {

	private static String excelName = "总分排名（%s)";

	@Autowired
	BaseDataService baseDataService;

	@Override
	public void exportData(Map<String, Object> parmter) throws Exception {

		// 设置 参数信息
		super.initParam(parmter);

		//校验参数,暂不校验cityCode
		if (p.getExamBatchId() == null || p.getPath() == null || p.getLevel() == null)
			return;

		// 初始化 sheet
		List<Sheet> sheets = getSheets();

		// 初始化 excel
		Excel excel = new Excel(String.format(excelName, "含各科名次") + ".xls", p.getPath(), sheets);

		// 导出 excel 文件
		ExportUtil.createExcel(excel);

	}

	/**
	 * 获取文理科 Sheet
	 *
	 * @return
	 */
	private List<Sheet> getSheets() {

		List<Sheet> sheets = new ArrayList<>();

		sheets.add(getSheet(EnmSubjectType.LI));

		sheets.add(getSheet(EnmSubjectType.WEN));

		return sheets;
	}

	/**
	 * 初始化 sheet
	 *
	 * @param type
	 * @return
	 */
	private Sheet getSheet(EnmSubjectType type) {

		Sheet sheet = new Sheet(type.getCode().toString(), type.getName());

		sheet.setFields(getFields(type));

		List<Map<String, Object>> data = baseDataService.getStudentSubjectsAndAllscore(p.getExamBatchId(),
				p.getSchoolId(), p.getLevel(), p.getStuType()).stream().filter(m->Integer.parseInt(m.get
				("TYPE").toString())==type.getCode()).collect(Collectors.toList());

		zyj.report.common.CalToolUtil.sortByIndexValue2(data, "ALL_RANK");

		sheet.getData().addAll(data);


		return sheet;

	}

	/**
	 * 初始化字段列表
	 *
	 * @param type
	 * @return
	 */
	private List<Field> getFields(EnmSubjectType type) {

		//获取 本次考试科目列表
		List<SubjectInfo> subjects = getSubjectList(type);

		List<Field> fields = new ArrayList<>();

		MultiField root = new MultiField(String.format(excelName, type.getName()));

		root.add(new SingleField("考号", "SEQUENCE"));
		root.add(new SingleField("姓名", "NAME"));
		root.add(new SingleField("班级", "CLS_NAME"));

		subjects.forEach(model -> {
			if (type.getCode()==model.getType()) {
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
	 * 获取 本次考试科目列表
	 *
	 * @return
	 */
	private List<SubjectInfo> getSubjectList(EnmSubjectType type) {

		List<Map<String, Object>> subjects_cur = baseDataService.getSubjectByExamid(p.getExamBatchId());

		//产生查询考试科目列表
		List<SubjectInfo> subjectList = subjects_cur.stream().filter(m -> Integer.parseInt(m.get("TYPE")
				.toString()) == type.getCode()).map(subject -> new SubjectInfo(subject.get
				("PAPER_ID").toString(), subject.get("SUBJECT").toString(), subject.get("SUBJECT_NAME").toString(),
				(Integer) subject.get("TYPE"))).sorted((subject2, subject1) -> {
			return zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(),
					subject1.getSubject()) - zyj.report.common.CalToolUtil.indexOf(zyj.report.common.CalToolUtil.getSubjectOrder(),
					subject2.getSubject());
		}).collect(Collectors.toList());

		return subjectList;
	}

}