package zyj.report.common.excel.file.impl;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import jxl.read.biff.BiffException;
import zyj.report.common.excel.config.ExcelConfigFactory;
import zyj.report.common.excel.config.ExcelConfigManager;

/***
 * 
 * @author
 *
 */
public class ExcelToModelUtil {
	/***
	 * 
	 * @param excelFile
	 * @param modelName   : id in excel config file
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws BiffException 
	 */
	public static List getModelList(File excelFile,String modelName) throws Exception{
		if(!excelFile.exists()){
			System.out.println( ExcelToModelUtil.class.getSimpleName()+" [getModelList:]"+excelFile.getAbsolutePath()+" does not exist.");
			return null;
		}
		ExcelConfigManager configManager = ExcelConfigFactory.getExcelConfigManger(null);
		ExcelToModelImpl etm =  new ExcelToModelImpl(excelFile,configManager.getModel(modelName));
		List modelList = etm.getModelList();
		
		return modelList;
	}
	public static List getModelList(String excelFileStr,String modelName) throws Exception{
		File excelFile=new File(excelFileStr);
		return getModelList(excelFile, modelName);
	}
}
