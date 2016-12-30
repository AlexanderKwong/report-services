package zyj.report.common.excel.file.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import zyj.report.common.excel.config.RuturnConfig;
import zyj.report.common.excel.entity.ErrorInfo;
import zyj.report.common.excel.entity.RuturnPropertyParam;
import zyj.report.common.excel.event.Event;
import zyj.report.common.excel.util.ExcelUtil;
import zyj.report.common.excel.util.ValueWidget;

/**
 * 
 * @author
 * 
 */
public class ExcelToModelImpl {

	private File excelFile = null;

	private RuturnConfig excelConfig = null;

	private Workbook book;

	private ErrorInfo errorInfo;

	public ExcelToModelImpl(File excelFile, RuturnConfig excelConfig)
			throws BiffException, IOException {
		errorInfo = new ErrorInfo();
		this.excelConfig = excelConfig;
		this.excelFile = excelFile;
		book = Workbook.getWorkbook(this.excelFile);
	}

	public void colse() {
		if (book != null) {
			book.close();
		}
	}

	public List getModelList() throws Exception {
		List modelList = new ArrayList();
		int sheelSize = book.getNumberOfSheets();
		for (int i = 0; i < sheelSize; i++) {
			getModelList(modelList, i);
		}
		book.close();
		return modelList;
	}

	public List getModelList(int sheelIndex) throws Exception {
		List modelList = new ArrayList();
		getModelList(modelList, sheelIndex);
		book.close();
		return modelList;
	}

	public boolean hasError() {
		return errorInfo.hasError();
	}

	public String getErrorInfo() {
		return errorInfo.print();
	}

	private void getModelList(List modelList, int sheelIndex) throws Exception {

		Sheet sheet = book.getSheet(sheelIndex);
		Event event = excelConfig.getEvent();
		boolean enableColumnFlag = excelConfig.getEnableColumn().size() > 0;
		for (int i = 1; i < sheet.getRows(); i++) {
			Object obj = this.getModelInstance(excelConfig.getClassName());
			if(event != null){
				if(!event.berfore(obj, errorInfo, sheet.getName(), i)){
					break;
				}
			}
			BeanWrapper bw = new BeanWrapperImpl(obj);
			
			boolean breakRowLoop = false;
			
			// 判断关键行信息全部为空的话就自动忽略
			if(enableColumnFlag){
				boolean isEnable = false;
				for(Integer c : excelConfig.getEnableColumn()){
					if(c != null){
						String value = sheet.getCell(c, i).getContents().trim();
						isEnable = isEnable || (value != null && !"".equals(value.trim()));
					}
				}
				if(!isEnable){
					continue;
				}
			}
			
			for (int j = 0; j < sheet.getColumns(); j++) {
				String excelTitleName = sheet.getCell(j, 0).getContents()
						.trim();
				String value = sheet.getCell(j, i).getContents().trim();
				RuturnPropertyParam propertyBean = (RuturnPropertyParam) excelConfig
						.getPropertyMap().get(excelTitleName);

				if (propertyBean != null) {
					if (value == null || value.length() < 1) {
						value = propertyBean.getDefaultValue();
					}

					// 验证单元格非空
					if (propertyBean.isNotNull()) {
						if (value == null
								|| (value = value.trim()).length() < 1) {
							if (!errorInfo.error(ErrorInfo.getHtmlErrorModer(
									sheet.getName(), i + 1, j + 1,
									excelTitleName, "不能为空"))) {
								breakRowLoop = true;
								break;
							}
							continue;
						}
					}

					String dateType = propertyBean.getDataType();
					Date date2 = null;
					if (value != null && !"".equals(value.trim())) {
						if (propertyBean.isConvertable()) {
							value = propertyBean.getConvertMap2().get(value);
							if (!ValueWidget.isHasValue(value)) {
								if (!errorInfo.error(ErrorInfo.getHtmlErrorModer(
												sheet.getName(),i + 1,j + 1,excelTitleName,
												"单元格值只能是"+ propertyBean.getConverValueList().toString()))) {
									breakRowLoop = true;
									break;
								}
								continue;
							}
						}
						if (dateType.equalsIgnoreCase("Date")) {
							String dateFormat = propertyBean.getDateFormat();
							if (ValueWidget.isHasValue(dateFormat)) {
								date2 = ExcelUtil.parseCellToDate(value,
										dateFormat);
							}
							if (date2 == null) {
								if (!errorInfo.error(ErrorInfo.getHtmlErrorModer(sheet.getName(),
												i + 1, j + 1, excelTitleName,
												"日期格式不对,请输入格式:" + dateFormat))) {
									breakRowLoop = true;
									break;
								}
							}
						}
						if (propertyBean.getMaxLength() != null
								&& !"".equals(propertyBean.getMaxLength()
										.trim())) {
							int maxLen = Integer.parseInt(propertyBean
									.getMaxLength());
							if (value.getBytes("gb2312").length > maxLen) {
								if (!errorInfo.error(ErrorInfo.getHtmlErrorModer(sheet.getName(),
												i + 1, j + 1, excelTitleName,
												"超出最大长度(" + maxLen + ")限制"))) {
									breakRowLoop = true;
									break;
								}
							}
						}
					}
					if (dateType.equalsIgnoreCase("Date")) {
						bw.setPropertyValue(propertyBean.getName(), date2);
					} else {
						bw.setPropertyValue(propertyBean.getName(), value);
					}

				}

			}
			modelList.add(obj);
			if(event != null){
				if(!event.after(obj, errorInfo, sheet.getName(), i)){
					break;
				}
			}
			if(breakRowLoop){
				break;
			}
		}
	}

	/***
	 * Instantiate object
	 * 
	 * @param className
	 * @return
	 */
	private Object getModelInstance(String className) {
		Object obj = null;
		try {
			obj = Class.forName(className).newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
