package zyj.report.common.excel.event;

import zyj.report.common.excel.entity.ErrorInfo;

public class DefaultEvent implements Event {
	

	public boolean berfore(Object obj,ErrorInfo errorInfo,String sheel,int row) {
		return true;
	}

	public boolean after(Object obj,ErrorInfo errorInfo,String sheel,int row) {
		return true;
	}

}
