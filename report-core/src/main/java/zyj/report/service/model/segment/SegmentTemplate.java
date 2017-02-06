package zyj.report.service.model.segment;

import zyj.report.service.model.Field;
import zyj.report.service.model.report.RptTemplate;
import zyj.report.service.model.report.title.MulClassSegmentRptTitle;
import zyj.report.service.model.report.title.SampleSegmentRptTitle;

import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/18.
 */
public class SegmentTemplate implements RptTemplate {

	@Override
	public List<Field> createTitle(String excelName) {
		return new SampleSegmentRptTitle().getTitle(excelName);
	}

	@Override
	public List<Field> createTitle(String excelName, List<Map<String, Object>> exTitle) {
		return new MulClassSegmentRptTitle().getTitle(excelName,exTitle);
	}


	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	private Integer step;

	public SegmentTemplate(Integer step) {
		this.step = step;
	}

}
