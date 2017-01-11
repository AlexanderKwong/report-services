package zyj.report.common;

import com.google.common.collect.Table;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zyj.report.common.excel.file.impl.ModelToExcelUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.Excel;
import zyj.report.service.model.Sheet;
import zyj.report.service.model2.CompositionIterator;
import zyj.report.service.model2.Field;
import zyj.report.service.model2.SingleField;

import java.io.File;
import java.util.*;


public class ExportUtil {

	private static Logger logger = LoggerFactory.getLogger(ExportUtil.class);

	public static void export(Map<String, Object> parmter) throws Exception {
		String server = parmter.get("server").toString();
		String pathFile = parmter.get("pathFile").toString();
		String level = parmter.get("level").toString().toLowerCase();
		logger.debug("-----------------------start export from " + server + " on level " + level);
		BaseRptService baseRptService = (BaseRptService) SpringUtil.getSpringBean(null, server);
		if (server.startsWith("extFetch")) {
			List<Map<String, Object>> data = baseRptService.fetchExportData(parmter);
			if (data.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");
			String fileName = baseRptService.getXlsFileName();
			String modelName = server.replace("extFetch", "").replace("Server", "").toLowerCase();
			ModelToExcelUtil.model2Excel(pathFile + fileName + ".xls", "excel/" + modelName + ".xml", modelName + level, data);
		} else {
			baseRptService.exportData(parmter);
		}
		logger.debug("-----------------------end export from " + server + " on level " + level);
	}

	public static void main(String[] args) throws Exception {
	/*	String[] titleList = new String[]{"科目","fenshu"};
		List<List<Object>> conList = new LinkedList<List<Object>>();
		List<Object> m = new LinkedList<Object>();
		m.add("语文");
		m.add("98.01");
		conList.add(m);
		m = new LinkedList<Object>();
		m.add("文数学");
		m.add("100");
		conList.add(m);
		createExpExcel(titleList, conList, "D:/111.xls");*/

		/*List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		Map con = new HashMap<String, Object>();
		con.put("deptName", "1");
		con.put("deptCode", "2");
		con.put("sendDate", new Date());
		con.put("sendFileName", "永久");
		res.add(con);
		ModelToExcelUtil.model2Excel("d:/测试model.xls", "excel/" + "testmodel" + ".xml", "testmodel", res);*/

	}

	public static void createExpExcel(String[] titleList, List<List<Object>> conList, String pathFile) throws Exception {
		// 生成XLS报表
		WritableFont titlefont = new WritableFont(
				WritableFont.createFont("宋体"), 10);
		WritableCellFormat titlecf = new WritableCellFormat(titlefont);
		titlecf.setAlignment(Alignment.CENTRE);
		titlecf.setBackground(Colour.GRAY_25);
		titlecf.setBorder(Border.ALL, BorderLineStyle.THIN);
		titlecf.setVerticalAlignment(VerticalAlignment.CENTRE);
		CellView cellView = new CellView();
		cellView.setAutosize(true); //设置自动大小  
		// 创建报表文件
		WritableWorkbook book = Workbook.createWorkbook(new File(pathFile));
		// 创建报表页
		WritableSheet sheet = book.createSheet("sheet1", 0);
		// 设置标题
		int rownum = 0;
		//设置表头
		for (int j = 0; j < titleList.length; j++) {
			WritableCell label = new Label(j, rownum, titleList[j], titlecf);
			sheet.addCell(label);
		}
		rownum++;
		//填充数据
		if (conList != null && conList.size() > 0) {
			for (List<Object> m : conList) {
				for (int j = 0; j < titleList.length; j++) {
					if (toStr(m.get(j)).getBytes("GBK").length > 9)
						sheet.setColumnView(j, cellView);//根据内容自动设置列宽 
					WritableCell label = createCell(j, rownum, m.get(j));
					sheet.addCell(label);
				}
				rownum++;
			}
		}
		book.write();//save excel

		book.close();//close excel file
//		createExpExcel(new String[][]{titleList}, conList, pathFile, new int[][]{}, null, null);
	}

	public static void createExpExcel(String[][] titleList, List<List<Object>> conList, String pathFile, int[][] mgArray, String titelName) throws Exception {
		createExpExcel(titleList, conList, pathFile, mgArray, titelName, null);
	}

	public static void createExpExcel(String[][] titleList, List<List<Object>> conList, String pathFile, int[][] mgArray, String titelName, String ps) throws Exception {
		// 生成XLS报表
		WritableFont titlefont = new WritableFont(
				WritableFont.createFont("宋体"), 10);
		WritableCellFormat titlecf = new WritableCellFormat(titlefont);
		titlecf.setAlignment(Alignment.CENTRE);
		titlecf.setBackground(Colour.GRAY_25);
		titlecf.setBorder(Border.ALL, BorderLineStyle.THIN);
		titlecf.setVerticalAlignment(VerticalAlignment.CENTRE);

		WritableFont headfont = new WritableFont(WritableFont.createFont("宋体"),
				16);
		WritableCellFormat headcf = new WritableCellFormat(titlecf);
		headcf.setFont(headfont);
		// 创建报表文件
		WritableWorkbook book = Workbook.createWorkbook(new File(pathFile));
		// 创建报表页
		WritableSheet sheet = book.createSheet("sheet1", 0);
		CellView cellView = new CellView();
		cellView.setAutosize(true); //设置自动大小  
		// 设置标题
		int rownum = 0;
		if (titelName != null && !"".equals(titelName)) {
			WritableCell label = new Label(0, 0, titelName, headcf);
			sheet.addCell(label);
			int titleNum = 0;
			for (int n = 0; n < titleList.length; n++) {
				titleNum = titleNum > titleList[n].length ? titleNum : titleList[n].length;
			}
			for (int i = 1; i < titleNum; i++) {
				sheet.addCell(new Label(i, 0, ""));
			}
			sheet.mergeCells(0, 0, titleNum - 1, 0);
			rownum++;
		}
		//设置表头
		for (int n = 0; n < titleList.length; n++) {
			for (int j = 0; j < titleList[n].length; j++) {
//						sheet.setColumnView(j, 100);
				WritableCell label = new Label(j, rownum, titleList[n][j], titlecf);
				sheet.addCell(label);
			}
			rownum++;
		}
		//填充数据
		if (conList != null && conList.size() > 0) {
			for (List<Object> m : conList) {
				for (int j = 0; j < m.size(); j++) {
					if (toStr(m.get(j)).getBytes("GBK").length > 9)
						sheet.setColumnView(j, cellView);//根据内容自动设置列宽 
					WritableCell label = createCell(j, rownum, m.get(j));
					sheet.addCell(label);
				}
				rownum++;
			}
		}
		//合并单元格
		for (int n = 0; n < mgArray.length; n++) {
			sheet.mergeCells(mgArray[n][0], mgArray[n][1], mgArray[n][2], mgArray[n][3]);
		}
		//添加说明
		if (!StringUtils.isBlank(ps)) {
			WritableFont titlefont2 = new WritableFont(
					WritableFont.createFont("宋体"), 10);
			WritableCellFormat cf = new WritableCellFormat(titlefont);
			cf.setAlignment(Alignment.LEFT);
			cf.setBorder(Border.ALL, BorderLineStyle.THIN);
			cf.setVerticalAlignment(VerticalAlignment.CENTRE);
//				cf.setWrap(true);
			sheet.mergeCells(0, rownum, titleList[0].length - 1, rownum);

			WritableCell label = createCell(0, rownum, ps, cf);
			sheet.addCell(label);

		}


		book.write();//save excel

		book.close();//close excel file

	}

	public static void createExpExcel(List<Integer> titleListNum, List<List<Object>> conList, String pathFile, int[][] mgArray) throws Exception {
		// 生成XLS报表
		WritableFont titlefont = new WritableFont(
				WritableFont.createFont("宋体"), 10);
		WritableCellFormat titlecf = new WritableCellFormat(titlefont);
		titlecf.setAlignment(Alignment.CENTRE);
		titlecf.setBackground(Colour.GRAY_25);
		titlecf.setBorder(Border.ALL, BorderLineStyle.THIN);
		titlecf.setVerticalAlignment(VerticalAlignment.CENTRE);
		CellView cellView = new CellView();
		cellView.setAutosize(true); //设置自动大小  
		// 创建报表文件
		WritableWorkbook book = Workbook.createWorkbook(new File(pathFile));
		// 创建报表页
		WritableSheet sheet = book.createSheet("sheet1", 0);
		// 设置标题
		int rownum = 0;
//		//设置表头
//		for(int j = 0;j < titleList.length;j++){
//			WritableCell label = new Label(j, rownum, titleList[j], titlecf);
//			sheet.addCell(label);
//		}

		int column = conList.get(0).size();
		//填充数据
		if (conList != null && conList.size() > 0) {
			for (List<Object> m : conList) {
				boolean isTitle = false;
				int index = conList.indexOf(m);
				if (titleListNum.contains(index))
					isTitle = true;
				for (int j = 0; j < column; j++) {
					if (toStr(m.get(j)).getBytes("GBK").length > 9)
						sheet.setColumnView(j, cellView);//根据内容自动设置列宽 
					WritableCell label = null;
					if (isTitle)
						label = createCell(j, rownum, m.get(j), titlecf);
					else
						label = createCell(j, rownum, m.get(j));
					sheet.addCell(label);
				}
				rownum++;
			}
		}
		//合并单元格
		for (int n = 0; n < mgArray.length; n++) {
			sheet.mergeCells(mgArray[n][0], mgArray[n][1], mgArray[n][2], mgArray[n][3]);
		}
		book.write();//save excel

		book.close();//close excel file

	}

	private static WritableCell createCell(int colno, int rowno, Object val)
			throws RowsExceededException, WriteException {
		return createCell(colno, rowno, val, null);
	}

	private static WritableCell createCell(int colno, int rowno, Object val,
										   WritableCellFormat cf) throws RowsExceededException, WriteException {
		WritableCell cell = null;

		if (cf == null) {
			WritableFont titlefont = new WritableFont(
					WritableFont.createFont("宋体"), 10);
			cf = new WritableCellFormat(titlefont);
			cf.setAlignment(Alignment.CENTRE);
			cf.setBorder(Border.ALL, BorderLineStyle.THIN);
			cf.setVerticalAlignment(VerticalAlignment.CENTRE);
		}
		cell = new Label(colno, rowno, ObjectUtils.toString(val), cf);

		return cell;
	}

	private static String toStr(Object value) {
		if (value == null)
			return "";
		else
			return value.toString();
	}

	/**
	 * @param titelList 表头列表
	 * @param conList   内容
	 * @param mgArray   合并数组 0：起始单元格的列号，1：起始单元格的行号，2：向下合并的列数，3：
	 * @param excelName excel名称
	 * @param sheetName sheet名称
	 * @param titelName 标题
	 * @param path      路径
	 * @throws Exception
	 */
	public static void createExpExcel(String[][] titelList, String[][] conList, int[][] mgArray, String excelName, String sheetName, String titelName, String path) throws Exception {
		// 生成XLS报表
		WritableFont titlefont = new WritableFont(
				WritableFont.createFont("宋体"), 10);
		WritableCellFormat titlecf = new WritableCellFormat(titlefont);
		titlecf.setAlignment(Alignment.CENTRE);
		titlecf.setBackground(Colour.GRAY_25);
		titlecf.setBorder(Border.ALL, BorderLineStyle.THIN);
		titlecf.setVerticalAlignment(VerticalAlignment.CENTRE);

		WritableFont headfont = new WritableFont(WritableFont.createFont("宋体"),
				16);
		WritableCellFormat headcf = new WritableCellFormat(titlecf);
		headcf.setFont(headfont);
		String fileName = path + excelName;
		// 创建报表文件
		WritableWorkbook book = Workbook.createWorkbook(new File(fileName));
		// 创建报表页
		WritableSheet sheet = book.createSheet(sheetName, 0);
		CellView cellView = new CellView();
		cellView.setAutosize(true); //设置自动大小
		// 设置标题
		int rownum = 0;
		if (titelName != null && !"".equals(titelName)) {
			WritableCell label = new Label(0, 0, titelName, headcf);
			sheet.addCell(label);
			int titleNum = 0;
			for (int n = 0; n < titelList.length; n++) {
				titleNum = titleNum > titelList[n].length ? titleNum : titelList[n].length;
			}
			for (int i = 1; i < titleNum; i++) {
				sheet.addCell(new Label(i, 0, ""));
			}
			sheet.mergeCells(0, 0, titleNum - 1, 0);
			rownum++;
		}

		//设置表头
		for (int n = 0; n < titelList.length; n++) {
			for (int j = 0; j < titelList[n].length; j++) {
//				sheet.setColumnView(j, 100);
				WritableCell label = new Label(j, rownum, titelList[n][j], titlecf);
				sheet.addCell(label);
			}
			rownum++;
		}
		//填充数据
		for (int n = 0; n < conList.length; n++) {
			for (int j = 0; j < conList[n].length; j++) {
				if (toStr(conList[n][j]).getBytes("GBK").length > 9)
					sheet.setColumnView(j, cellView);//根据内容自动设置列宽

				WritableCell label = createCell(j, rownum, conList[n][j]);
				sheet.addCell(label);
			}
			rownum++;
		}
		//合并单元格
		for (int n = 0; n < mgArray.length; n++) {
			sheet.mergeCells(mgArray[n][0], mgArray[n][1], mgArray[n][2], mgArray[n][3]);
		}
		book.write();
		book.close();
	}

	/*	private  WritableCell createCell(int colno, int rowno, Object val)
				throws RowsExceededException, WriteException {
			return createCell(colno, rowno, val, null);
		}

		private  WritableCell createCell(int colno, int rowno, Object val,WritableCellFormat cf) throws RowsExceededException, WriteException {
			WritableCell cell = null;

			if (cf == null) {
				WritableFont titlefont = new WritableFont(
						WritableFont.createFont("宋体"), 10);
				cf = new WritableCellFormat(titlefont);
				cf.setAlignment(Alignment.CENTRE);
				cf.setBorder(Border.ALL, BorderLineStyle.THIN);
				cf.setVerticalAlignment(VerticalAlignment.CENTRE);
			}
			cell = new Label(colno, rowno, toStr(val), cf);

			return cell;
		}*/
	public static void createExpExcel(String[][] titelList, String[] rowkeys, String[] columnkeys, Table<String, String, Object> content, int[][] mgArray, String excelName, String sheetName, String titelName, String path) throws Exception {
	}


	/**
	 * 新增加 导出 Excel 方法
	 *
	 * @param excel
	 * @throws Exception
	 */
	public static void createExcel(Excel excel) throws Exception {
		// 生成XLS报表
		WritableFont titlefont = new WritableFont(WritableFont.createFont("宋体"), 10);
		WritableCellFormat titlecf = new WritableCellFormat(titlefont);

		titlecf.setAlignment(Alignment.CENTRE);
		titlecf.setBackground(Colour.GRAY_25);
		titlecf.setBorder(Border.ALL, BorderLineStyle.THIN);
		titlecf.setVerticalAlignment(VerticalAlignment.CENTRE);

		WritableFont headfont = new WritableFont(WritableFont.createFont("宋体"), 16);
		WritableCellFormat headcf = new WritableCellFormat(titlecf);

		WritableCellFormat cellcf = new WritableCellFormat(titlefont);
		cellcf.setWrap(true);
		cellcf.setAlignment(Alignment.CENTRE);

		headcf.setFont(headfont);

		String fileName = excel.getPath() + excel.getName();

		// 创建报表文件
		WritableWorkbook book = Workbook.createWorkbook(new File(fileName));

		CellView cellView = new CellView();

		// 循环报表 Sheet
		for (Sheet model : excel.getSheets()) {

			// 创建报表页
			WritableSheet sheet = book.createSheet(model.getName(), 0);
			sheet.getSettings().setVerticalFreeze(model.getFreeze());

			// 设置标题
			int rowNum = 0;
			if (model.getLevel1Title() != null && !"".equals(model.getLevel1Title())) {
				WritableCell label = new Label(0, 0, model.getLevel1Title(), headcf);
				try {
					sheet.addCell(label);
				} catch (WriteException e) {
					e.printStackTrace();
				}

				for (int i = 1; i < model.getFields().size(); i++) {
					sheet.addCell(new Label(i, 0, ""));
				}
				sheet.mergeCells(0, 0, model.getFields().size() - 1, 0);
				rowNum++;
			}

			//设置表头
			for (int i = 0; i < model.getFields().size(); i++) {
				WritableCell label = new Label(i, rowNum, model.getFields().get(i).getTitle(), titlecf);

				if (model.getFields().get(i).getTitle().length() > 5) {
					cellView.setSize(model.getFields().get(i).getTitle().length() * 512);
					sheet.setColumnView(i, cellView);//根据内容自动设置列宽
				}

				sheet.addCell(label);
			}
			rowNum++;

			//填充数据
			for (int n = 0; n < model.getData().size(); n++) {
				Map bean = model.getData().get(n);
				for (int i = 0; i < model.getFields().size(); i++) {
					WritableCell label = createCell(i, rowNum, bean.get(model.getFields().get(i).getMark()) == null ? "" :
							bean.get(model.getFields().get(i).getMark()).toString(), cellcf);

					sheet.addCell(label);
				}
				rowNum++;
			}
		}

		book.write();
		book.close();
	}

	public static void createExcel(zyj.report.service.model2.Excel excel) throws Exception {
		// 生成XLS报表
		WritableFont titlefont = new WritableFont(WritableFont.createFont("宋体"), 10);
		WritableCellFormat titlecf = new WritableCellFormat(titlefont);

		titlecf.setAlignment(Alignment.CENTRE);
		titlecf.setBackground(Colour.GRAY_25);
		titlecf.setBorder(Border.ALL, BorderLineStyle.THIN);
		titlecf.setVerticalAlignment(VerticalAlignment.CENTRE);

		WritableFont headfont = new WritableFont(WritableFont.createFont("宋体"), 16);
		WritableCellFormat headcf = new WritableCellFormat(titlecf);

		headcf.setFont(headfont);

		String fileName = excel.getPath() + excel.getName();

		// 创建报表文件
		WritableWorkbook book = Workbook.createWorkbook(new File(fileName));

		CellView cellView = new CellView();
		cellView.setAutosize(true); //设置自动大小


		// 循环报表 Sheet
		for (zyj.report.service.model2.Sheet model : excel.getSheets()) {

			// 创建报表页
			WritableSheet sheet = book.createSheet(model.getName(), 0);

			// 设置标题
			int rowNum = 0;

			List<Field> fields = model.getFields();
			List<SingleField> singleFields = new ArrayList<>();
			Iterator<Field> iterator = null;

			//
			int column = 0;
			int row = 0;
			int sameRoot = 1;//有共同的根: 0 ;没有: 1
			String [][] tmp = new String[20][255];//xls列数无法超过255列
			List<int[]> notBlank = new ArrayList<>();

			if (fields.size() <= 0) return;
			else if (fields.size() == 1) {
				sameRoot = 0;
				notBlank.add(new int[]{row, column});
				Field field = fields.get(0);
				tmp[row][column] = field.getTitle();
				iterator = field.createIterator();
			}
			else iterator = new CompositionIterator(fields.iterator());

			while(iterator.hasNext()){

				int rowIndex = ((CompositionIterator)iterator).getLevel()-sameRoot;
				int columnIndex = column;
				notBlank.add(new int[]{rowIndex, columnIndex});

				row = Math.max(rowIndex,row);

				Field field = iterator.next();
//				System.out.println("title:" + field.getTitle() + "\trow:"+rowIndex+"\tcolumn:"+columnIndex);
				tmp[rowIndex][columnIndex] = field.getTitle();
				if (field instanceof SingleField) {
					singleFields.add((SingleField)field);
					column ++ ;
				}
			}
			row ++;

			//格式化表头
			String [][] head = new String[row][column];
			head = Arrays.copyOf( tmp, row );
			for (int i = 0 ; i< head .length; i++) head[i] = Arrays.copyOf( head[i], column );


			//设置表头
			for (; rowNum < head.length; rowNum++) {
				String[] headRow = head[rowNum];
				for (int c = 0; c < headRow.length ; c++){
					WritableCell label = new Label(c, rowNum, toStr(headRow[c]), titlecf);
					sheet.addCell(label);
				}
			}

			//纵向合并
			int [] previousNotBlank = null;
			for (int[] nb : notBlank){
				//纵向合并情况 ： 前一个非空元素 不在当前列(即前一个是叶子节点)，则合并前一个元素所在坐标 到列末
				if (previousNotBlank != null && previousNotBlank[1] != nb[1] ){
					sheet.mergeCells(previousNotBlank[1], previousNotBlank[0], previousNotBlank[1], row -1);
				}
				previousNotBlank = nb;
			}
			//默认最后一个元素一定是叶子节点
			sheet.mergeCells(previousNotBlank[1], previousNotBlank[0], previousNotBlank[1], row -1);
			//横向合并
			notBlank.sort((arr1, arr2)->{
				if (arr1[0] != arr2[0]) return arr1[0] - arr2[0];
				else return arr1[1] - arr2[1];
			});
			previousNotBlank = null;
			for (int[] nb : notBlank){
				//横向合并情况1 ： 前一个非空元素 不在当前行，则合并前一个元素所在坐标 到行末
				if (previousNotBlank != null && previousNotBlank[0] != nb[0] ){
					sheet.mergeCells(previousNotBlank[1], previousNotBlank[0], column-1, previousNotBlank[0]);
				}
				//横向合并情况2 ： 前一个非空元素 在当前行 且距离当前格大于1，则合并前一个元素所在坐标 到 当前坐标的前一格
				if (previousNotBlank != null && previousNotBlank[0] == nb[0] && nb[1] - previousNotBlank[1] > 1){
					sheet.mergeCells(previousNotBlank[1], previousNotBlank[0], nb[1] -1, previousNotBlank[0]);
				}
				previousNotBlank = nb;
			}//最后一个默认是最右边

			//填充数据
			for (int n = 0; n < model.getData().size(); n++) {

				Map bean = model.getData().get(n);
				for (int i = 0; i < singleFields.size(); i++) {

					WritableCell label = createCell(i, rowNum, bean.get(singleFields.get(i).getMark()) == null ? "" :
							bean.get(singleFields.get(i).getMark()).toString());
					sheet.addCell(label);
				}
				rowNum++;
			}
		}

		book.write();
		book.close();
	}

}

