package zyj.report.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/5.
 */
public class MultiSheet {

	/**
	 * 表格id
	 */
	private String id;

	/**
	 * 表格名称
	 */
	private String name;

	/**
	 * 字段列表
	 */
	private List<Sheet> sheets;

	/**
	 * 字段对应的数据
	 */
	private List<Map<String, Object>> datas = new ArrayList<>();

	public MultiSheet(String id, String name) {
		this.id = id;
		this.name = name;
		sheets = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public List<Sheet> getSheets() {
		return sheets;
	}

	public void setSheets(List<Sheet> sheets) {
		this.sheets = sheets;
	}

}

