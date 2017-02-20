package zyj.report.service.model.report.title;

import zyj.report.service.model.Field;
import zyj.report.service.model.MultiField;
import zyj.report.service.model.SingleField;
import zyj.report.service.model.report.RptTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/18.
 */
public class MulClassSegmentRptTitle implements RptTitle {

	@Override
	public List<Field> getTitle(String excelName, List<Map<String, Object>> exFields) throws NullPointerException {

		List<Field> fields = new ArrayList<>();
		MultiField root = new MultiField(excelName);


		root.add(new SingleField("分数段", "SCORE_SEG"));

		MultiField sum = new MultiField("汇总");
		//step1:加载固定标题
		for (String t : new String[]{"人数,FREQUENCY", "累计,ACC_FREQUENCY"}) {
			String[] args = t.split(",");
			sum.add(new SingleField(args[0], args[1]));
		}
		root.add(sum);

		//step2:加载扩展标题
		if (exFields == null)
			throw new NullPointerException("新建多班级分数段标题，存在班级列表");

		exFields.forEach(m -> {
			MultiField multiField = new MultiField(m.get("CLS_NAME").toString());
			multiField.add(new SingleField("人数", m.get("CLS_ID").toString() + "_FREQUENCY"));
			multiField.add(new SingleField("累计", m.get("CLS_ID").toString() + "_ACC_FREQUENCY"));
			root.add(multiField);
		});

		fields.add(root);

		return fields;
	}

	@Override
	public List<Field> getTitle(String excelName) {
		return null;
	}


}
