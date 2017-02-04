package zyj.report.service.model.report;

import zyj.report.service.model.Field;

import java.util.List;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/18.
 */
public interface RptTitle {

	List<Field> getTitle(String excelName);

	/**
	 * 新建字段
	 *
	 * @return
	 */
	List<Field> getTitle(String excelName, List<Map<String, Object>> exFields) throws Exception;

}
