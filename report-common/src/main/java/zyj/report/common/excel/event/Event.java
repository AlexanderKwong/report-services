package zyj.report.common.excel.event;

import zyj.report.common.excel.entity.ErrorInfo;

public interface Event {
	
	public boolean berfore(Object obj, ErrorInfo errorInfo, String sheel, int row);
	
	public boolean after(Object obj, ErrorInfo errorInfo, String sheel, int row);

}
