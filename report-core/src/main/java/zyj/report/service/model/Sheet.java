package zyj.report.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/5.
 */
public class Sheet{

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
	private List<zyj.report.service.model.Field> fields;

	/**
	 * 字段对应的数据
	 */
	private List<Map<String,Object>> datas = new ArrayList<>();

	/**
	 * 冻结表头
	 */
	private Integer freeze;

	/**
	 *  数据为 list 转化为 二维数组
	 *
	 * @return
	 */
	public String[][] getDataOnArray() {

		String[][] objArrList = new String[datas.size()][];
		for (int j = 0; j < datas.size(); j++) {

			String[] row = new String[fields.size()];
			Map bean = datas.get(j);
			for (int i = 0; i < fields.size(); i++) {
				row[i] = bean.get(fields.get(i).getMark()) == null ? "" : bean.get(fields.get(i).getMark()).toString();
			}
			objArrList[j] = row;
		}
		return objArrList;
	}

	public Sheet(String id, String name) {
		this.id = id;
		this.name = name;
		fields = new ArrayList<>();
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

	public List<zyj.report.service.model.Field> getFields() {
		return fields;
	}

	public void setFields(List<zyj.report.service.model.Field> fields) {
		this.fields = fields;
	}

	public List<Map<String, Object>> getData() {
		return datas;
	}

	public void setData(List<Map<String, Object>> data) {
		this.datas = data;
	}

	public Integer getFreeze() {
		return freeze;
	}

	public void setFreeze(Integer freeze) {
		this.freeze = freeze;
	}
}

