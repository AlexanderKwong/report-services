package zyj.report.service.model.report.title;

import zyj.report.service.model.Field;
import zyj.report.service.model.MultiField;
import zyj.report.service.model.report.RptTitle;
import zyj.report.service.model.SingleField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/18.
 */
public class SampleSegmentRptTitle implements RptTitle {

	@Override
	public List<Field> getTitle(String excelName) {

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

	@Override
	public List<Field> getTitle(String excelName, List<Map<String, Object>> exFields) {
		return null;
	}
}
