package zyj.report.business.task;


/**
 * 报告生成任务工厂
 * @author admin
 *
 */
public interface  RptTaskFactory {
	
	/**
	 * 根据条件产生任务队列
	 * @return
	 */
	public RptTaskQueue create(RptParameter parameter);
	
	
	
}
