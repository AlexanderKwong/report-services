package zyj.report.business.task.impl;


import zyj.report.business.task.RptTask;

public class RptTaskWait extends RptTask {


	private String pathFile;
		
	public RptTaskWait(String jobId) {
		super("wait",jobId);
	}

	public RptTaskWait(String jobId, String pathFile) {
		this(jobId);
		this.pathFile = pathFile;
	}
	
	
	@Override
	public void run() throws Exception {
		

	}
	@Override
	public String getPathFile()  {
		
		return pathFile;
	}

}
