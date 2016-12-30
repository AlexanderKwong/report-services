package zyj.report.actor;

import akka.actor.UntypedActor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.RptTaskQueue;
import zyj.report.business.task.impl.EOFTask;
import zyj.report.business.task.impl.RptTaskWait;
import zyj.report.exception.report.ReportTaskDispatchException;
//import akka.remotetaskexcute.ExportTask;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/9
 */
public class DispatcherActor extends UntypedActor {

    private Logger logger = LoggerFactory.getLogger(UntypedActor.class);

    private RptTaskQueue<RptTask> rptTaskRptTaskQueue ;

    private String jobId;

    private long start;

    public DispatcherActor(String jobId,RptTaskQueue<RptTask> rptTaskRptTaskQueue) throws ReportTaskDispatchException {
        if (StringUtils.isNotBlank(jobId) && rptTaskRptTaskQueue != null && !rptTaskRptTaskQueue.isEmpty()){
            this.rptTaskRptTaskQueue = rptTaskRptTaskQueue;
            this.jobId = jobId;
        }else{
            throw new ReportTaskDispatchException("批次Id为空或没有生成任务列表！");
        }
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof String){
            if (o.equals("start")){
                while (rptTaskRptTaskQueue.size() > 0) {
                    RptTask task = (RptTask)rptTaskRptTaskQueue.remove();
                    getSender().tell(task,getSelf());
                    if(task.getClass().equals(RptTaskWait.class)){
                        logger.info(String.format("Job [%s] 暂停， 其中子任务队列剩余大小为 %d", jobId, rptTaskRptTaskQueue.size()));
                        break;
                    }
                }
            }

            if (rptTaskRptTaskQueue.isEmpty())  getSender().tell(new EOFTask(jobId),getSelf());
        }
    }

    @Override
    public void preStart() {
        logger.info(String.format("Job [%s] 就绪，其中子任务队列大小为 %d", jobId, rptTaskRptTaskQueue.size()));
        start = System.currentTimeMillis();
    }

    @Override
    public void postStop() {
        // tell the world that the calculation is complete
        long timeSpent = (System.currentTimeMillis() - start) / 1000;
        logger.info(String
                        .format("\n\tJob [%s] 完成， \n\tDispatcherActor 估计 \t\t\n\t耗时: \t%s Secs",
                                jobId, timeSpent));
    }
}
