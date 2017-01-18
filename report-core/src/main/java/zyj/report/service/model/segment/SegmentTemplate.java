package zyj.report.service.model.segment;

import zyj.report.service.model.Field;
import zyj.report.service.model.report.RptTemplate;
import zyj.report.service.model.report.title.SampleSegmentRptTitle;

import java.util.List;

/**
 * Created by CXinZhi on 2017/1/18.
 */
public class SegmentTemplate implements RptTemplate {

	@Override
	public List<Field> createTitle(String excelName) {
		return new SampleSegmentRptTitle().getTitle(excelName);
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
