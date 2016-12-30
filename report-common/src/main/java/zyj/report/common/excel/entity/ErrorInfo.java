package zyj.report.common.excel.entity;

import java.util.LinkedList;
import java.util.List;

public class ErrorInfo {
	
	
	private List<String> errorList = new LinkedList<String>();
	
	private int maxError = 15;
	
	private int count = 0;
	
	private static char[] CW = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	
	public boolean error(String errorInfo){
		boolean result = false;
		if((count++) < maxError){
			errorList.add(errorInfo);
			result = true;
		}
		return result;
	}
	
	public void rollback(int count){
		for(int i=errorList.size()-1;i>=0&&count>0;i--,count--){
			this.count--;
			errorList.remove(i);
		}
	}
	
	public boolean hasError(){
		return errorList.size() > 0;
	}
	
	
	public String print(){
		StringBuilder builder = new StringBuilder();
		for(String str : errorList){
			builder.append(str);
		}
		return builder.toString();
	}
	
	public static String getHtmlErrorModer(String sheel,int row, int column,String excelTitleName,String errorInfo){
		return "<p style=\"padding-bottom: 2px;\"><font color=\"#FF0000\">error</font>: [sheet:" + sheel + "][行:" + row + "][列:" + column + "(" + change(column) + ")][" + excelTitleName + "]  " + errorInfo + "</p>";
	}
	
	public static String getHtmlErrorModer(String sheel,int row,String errorInfo){
		return "<p style=\"padding-bottom: 2px;\"><font color=\"#FF0000\">error</font>: [sheet:" + sheel + "][行:" + row + "]  " + errorInfo + "</p>";
	}
	
	public static String change(int column){
		column = column -1;
		int i = column / 26;
		int j = column % 26;
		String t = "";
		if(i>0){
			return String.valueOf(CW[i-1])+String.valueOf(CW[j]);
		}
		return String.valueOf(CW[j]);
	}



	public int getMaxError() {
		return maxError;
	}



	public void setMaxError(int maxError) {
		this.maxError = maxError;
	}


	public int getCount() {
		return count;
	}

}
