package zyj.report.service.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CXinZhi on 2017/1/5.
 *
 */
public class MultiExcel {

	/**
	 * Excel 包含的 sheet 列表
	 */
	private List<MultiSheet> sheets;

	/**
	 * excel 名称
	 */
	private String name;

	/**
	 * excel 路径
	 */
	private String path;

	/**
	 * 构造函数
	 *
	 * @param name 名称
	 * @param path path
	 */
	public MultiExcel(String name, String path) {
		this.name = name;
		this.path = path;
		sheets = new ArrayList<>();
	}

	/**
	 * 构造函数
	 * @param name 名称
	 * @param path path
	 */
	public MultiExcel(String name, String path, List<MultiSheet> sheets) {
		this.name = name;
		this.path = path;
		this.sheets = sheets;
	}

	public List<MultiSheet> getSheets() {
		return sheets;
	}

	public void setSheets(List<MultiSheet> sheets) {
		this.sheets = sheets;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
