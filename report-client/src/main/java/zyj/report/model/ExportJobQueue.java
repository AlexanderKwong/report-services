package zyj.report.model;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import zyj.report.business.Job;
import zyj.report.business.job.ExportReportJob;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.RptTaskQueue;
import zyj.report.exception.report.ReportJobTransferException;
import zyj.report.service.JyjRptExtService;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 队列代理
 * @Company 广东全通教育股份公司
 * @date 2016/9/9
 */

public class ExportJobQueue extends JobQueue<ExportReportJob>{

    @Autowired
    JyjRptExtService jyjRptExtService;

    volatile Map<String,RptTaskQueue<RptTask>> jobToTasksMap = new ConcurrentHashMap<>();

    @Override
    public void add(ExportReportJob job) throws Exception {

        synchronized (this){

            RptTaskQueue<RptTask> rptTaskQueue = tansJobToTasks(job);

            jobInit(job,rptTaskQueue);

            super.add(job);
        }

    }

    @Override
    public void jobInit(ExportReportJob job, Object... args) throws ReportJobTransferException {
        RptTaskQueue<RptTask> rptTaskRptTaskQueue = (RptTaskQueue<RptTask>)args[0];
        if(job != null && rptTaskRptTaskQueue!=null && !rptTaskRptTaskQueue.isEmpty() ){
            RptTaskQueue<RptTask> rptTaskRptTaskQueue_old = jobToTasksMap.get(job.getID());
            if (rptTaskRptTaskQueue_old != null && !rptTaskRptTaskQueue_old.isEmpty())
                throw new ReportJobTransferException("该job对应的任务集不为空，请勿重复初始化！");
            else {
                synchronized (this){
                    jobToTasksMap.putIfAbsent(job.getID(),rptTaskRptTaskQueue);
                }
            }
        }
    }

    @Override
    public boolean jobIsInited(ExportReportJob job) {
        if(job != null){
            RptTaskQueue<RptTask> rptTaskRptTaskQueue = jobToTasksMap.get(job.getID());
            if (rptTaskRptTaskQueue != null && !rptTaskRptTaskQueue.isEmpty())
                return true;
        }
        return false;
    }

    @Override
    public ExportReportJob remove(ExportReportJob job){
        synchronized (this){
            jobToTasksMap.remove(job.getID());
            return super.remove(job);
        }
    }

    public RptTaskQueue<RptTask> getTaskList(ExportReportJob job){
        return jobToTasksMap.getOrDefault(job.getID(), null);

    }


    public RptTaskQueue<RptTask> tansJobToTasks(ExportReportJob job) throws Exception {
        final String examId = job.getID();
        final int rptType = job.getRptType();
        final int stuType = job.getStuType();
        final List<String> paperIds2Exp = job.getPaperIds();
        return jyjRptExtService.getRptTaskQueue(examId,stuType,rptType,paperIds2Exp);
    }

}
