package zyj.report.common.excel.file.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import zyj.report.common.excel.config.ExcelConfigFactory;
import zyj.report.common.excel.config.ExcelConfigManager;
import zyj.report.common.excel.config.RuturnConfig;
import zyj.report.common.excel.entity.RuturnPropertyParam;

/***
 * convert java beans to excel
 * 
 * @author
 *
 */
public class ModelToExcelUtil {

	/***
	 * export to excel file from java benas
	 * convert java beans to excel file
	 * 
	 * @param excelFile
	 * @param modelName
	 * @param models
	 * @return
	 * @throws IOException
	 * @throws RowsExceededException
	 * @throws WriteException
	 * @author
	 */
	public static void model2Excel(OutputStream out,String configFile, String modelName, List models)
			throws IOException, RowsExceededException, WriteException {
		
		//read "ImportExcelToModel.xml" file
		ExcelConfigManager configManager = ExcelConfigFactory.getExcelConfigManger(configFile);
		RuturnConfig returnConfig = configManager.getModel(modelName);
		WritableWorkbook wb = Workbook.createWorkbook(out);
		WritableSheet wsheet = wb.createSheet("sheet1", 0);
	/*	WritableFont font1 = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD);//Set the table header in bold
		WritableCellFormat format1 = new WritableCellFormat(font1);*/
		WritableFont titlefont = new WritableFont(
				WritableFont.createFont("宋体"), 10);
		WritableCellFormat titlecf = new WritableCellFormat(titlefont);
		titlecf.setAlignment(Alignment.CENTRE);
		titlecf.setBackground(Colour.GRAY_25);
		titlecf.setBorder(Border.ALL, BorderLineStyle.THIN);
		titlecf.setVerticalAlignment(VerticalAlignment.CENTRE);

		Map propertyMap = returnConfig.getPropertyMap();
		int columns_size = propertyMap.size();
		
		//Setting  excel table header sequence according column in xml;
		//column start from one
		for (Iterator it = propertyMap.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			RuturnPropertyParam modelProperty = (RuturnPropertyParam) propertyMap
					.get(key);
			int column = Integer.parseInt(modelProperty.getColumn());//sequence  in excel file
			wsheet.addCell(new Label(column - 1, 0, key, titlecf));
			if(modelProperty.getViewLength() > 0){
				wsheet.setColumnView(column-1, modelProperty.getViewLength());
			}
		}

		//write real data to excel file from beans list
		if( models != null){
			for (int i = 0; i < models.size(); i++) {
				Object obj = models.get(i);
				BeanWrapper bw = null;
				Map map = null;
				if(obj instanceof Map){
					map = (Map) obj;
				}else{
					bw = new BeanWrapperImpl(obj);
				}
				for (int k = 0; k < columns_size; k++) {
					String excelTitleName = wsheet.getCell(k, 0).getContents()
							.trim();//title name in excel
					RuturnPropertyParam modelProperty = (RuturnPropertyParam) propertyMap.get(excelTitleName);
					String beanPropertyName = modelProperty.getName();//property name in java object
					if(beanPropertyName == null || "".equals(beanPropertyName)){
						continue;
					}
					Object propertyValue;
					if(map != null){
						propertyValue = map.get(modelProperty.getNameForMap());
						if(propertyValue == null){
							propertyValue = map.get(modelProperty.getName());
						}
					}else{
						propertyValue = bw.getPropertyValue(beanPropertyName);
					}
					if(propertyValue != null){
						String dataType=modelProperty.getDataType();
						if(dataType.equalsIgnoreCase("Date")){//convert date to string
							SimpleDateFormat sdf = new SimpleDateFormat(modelProperty.getDateFormat());
							propertyValue= sdf.format((Date)propertyValue);
						}
						if(modelProperty.isConvertable()){//whether is convertable ,see xml file
							Map<String,String> convertMap=modelProperty.getConvertMap();
							propertyValue=(String) convertMap.get(propertyValue.toString());
						}
					}else{
						propertyValue = "";
					}
					
					if(propertyValue == null){
						propertyValue = "";
					}
					//set format of content data 
					WritableFont titlefont_con = new WritableFont(
							WritableFont.createFont("宋体"), 10);
					WritableCellFormat cf = new WritableCellFormat(titlefont_con);
					cf.setAlignment(Alignment.CENTRE);
					cf.setBorder(Border.ALL, BorderLineStyle.THIN);
					cf.setVerticalAlignment(VerticalAlignment.CENTRE);
					//
					wsheet.addCell(new Label(Integer.parseInt(modelProperty
							.getColumn()) - 1, i + 1, propertyValue.toString(),cf));
				}
			}
		}
		wb.write();//save excel
		
		wb.close();//close excel file
		
		out.close();
		//return excelFile;
	}

	/***
	 * 
	 * @param excelFileStr   :path of excel file;type:String
	 * @param modelName
	 * @param models
	 * @return
	 * @throws IOException
	 * @throws RowsExceededException
	 * @throws WriteException
	 * @author Li xucan
	 */
	public static File model2Excel(String excelFileStr,String configFile, String modelName, List models)
			throws IOException, RowsExceededException, WriteException {
		File excelFile=new File(excelFileStr);
		if (excelFile.exists()) {//delete excel file if this file has exist
			excelFile.delete();
		}
		if (!excelFile.exists()) {
			excelFile.createNewFile();//create a new excel file
		}
		model2Excel(new FileOutputStream(excelFile), configFile, modelName, models);
		return excelFile;
	}
}
