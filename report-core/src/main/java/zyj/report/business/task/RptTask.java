package zyj.report.business.task;

import zyj.report.business.Task;

import java.util.UUID;

public abstract class RptTask implements Task{

	private static final long serialVersionUID = -5898989470754667710L;
	//执行
	public abstract void run() throws Exception;
	//任务生成文件路径
	public abstract String getPathFile() ;

	//状态
	private STATE state;

	private String id;
	//批次号
	private String jobId;

	public RptTask(String jobId) {
		this(UUID.randomUUID().toString(),jobId);
	}

	public RptTask(String id, String jobId) {
		this(jobId,null,id);
	}

	public RptTask(String jobId, STATE state, String id) {
		this.jobId = jobId;
		if (state == null)
			this.state = STATE.WAITTING;
		else
			this.state = state;
		this.id = id;
	}


	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Override
	public STATE getState() {
		synchronized (this){
			return state;
		}
	}

	public void setState(STATE state) {
		synchronized (this){
			this.state = state;
		}
	}
}
