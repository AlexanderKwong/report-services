package zyj.report.common.constant;

/**
 * Created by CXinZhi on 2017/1/10.
 * <p>
 * 科目类别
 */
public enum EnmQuestionType {

	SINGLE(1, "单选"),
	MULTIPLE(2, "多选"),
	JUDGE(3, "判断"),
	SUBJECTIVITY(4, "主观");

	private int code;
	private String name;

	private EnmQuestionType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



}
