package zyj.report.business.task.impl;


import zyj.report.business.task.RptTask;

import java.util.LinkedList;
import java.util.List;

public class RptTaskSeries extends RptTask {

	private List<RptTaskBase> taskSeries;
	
	private String pathFile;
	
	public String getPathFile() {
		return pathFile;
	}
	public void setPathFile(String pathFile) {
		this.pathFile = pathFile;
	}
	public RptTaskSeries(String exambatchId) {
		super(exambatchId);
		taskSeries = new LinkedList<RptTaskBase>();
	}
	
	public RptTaskSeries(String exambatchId, String pathFile) {
		this(exambatchId);
		this.pathFile = pathFile;
	}
	
	public void add(RptTaskBase e) {
		taskSeries.add(e);
	}
	
	@Override
	public void run() throws Exception {
		
		for(RptTaskBase taskBase : taskSeries){

			taskBase.run();
		}

	}

}
