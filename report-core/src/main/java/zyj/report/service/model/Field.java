package zyj.report.service.model;

/**
 * Created by CXinZhi on 2017/1/5.
 */
public class Field {


	/**
	 * 字段标示
	 */
	private String mark;

	/**
	 * 字段标题
	 */
	private String title;

	public Field(String mark, String title) {
		this.mark = mark;
		this.title = title;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
