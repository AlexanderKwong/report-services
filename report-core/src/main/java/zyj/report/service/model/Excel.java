package zyj.report.service.model;

import zyj.report.service.model.Sheet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CXinZhi on 2017/1/5.
 */
public class Excel {

	/**
	 * Excel 包含的 sheet 列表
	 */
	private List<Sheet> sheets;

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
	public Excel(String name, String path) {
		this.name = name;
		this.path = path;
		sheets = new ArrayList<>();
	}

	/**
	 * 构造函数
	 *
	 * @param name 名称
	 * @param path path
	 */
	public Excel(String name, String path, List<Sheet> sheets) {
		this.name = name;
		this.path = path;
		this.sheets = sheets;
	}


	public List<Sheet> getSheets() {
		return sheets;
	}

	public void setSheets(List<Sheet> sheets) {
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
