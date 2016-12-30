package zyj.report.common.excel.test;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import zyj.report.common.excel.file.impl.ModelToExcelUtil;


public class Test{
    

public static void main(String[] args) throws Exception {
	List l = new LinkedList<Map<String,Object>>();
	Map<String,Object> m = new HashMap<String,Object>();
	m.put("deptName", "啊");
	m.put("deptCode", 111.111);
	m.put("sendFileName", "send_2");
	m.put("sendDate", new Date());
	l.add(m);
	ModelToExcelUtil.model2Excel("d:/1.xls", "zyj/report/common/excel/test/ExcelModeMapping.xml", "testModel", l);
	
}
}
