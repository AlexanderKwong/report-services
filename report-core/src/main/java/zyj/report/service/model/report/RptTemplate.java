package zyj.report.service.model.report;

import zyj.report.service.model.Field;

import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/18.
 * <p>
 * zyj 模板工厂
 */
public interface RptTemplate {

	/**
	 * 新建字段
	 *
	 * @return
	 */
	List<Field> createTitle(String excelName);

	List<Field> createTitle(String excelName, List<Map<String, Object>> exTitle);

}
