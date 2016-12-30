package zyj.report.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.actor.DispatcherActor;
import zyj.report.business.Job;
import zyj.report.business.job.AbstractReportJob;
import zyj.report.business.job.CancelReportJob;
import zyj.report.business.job.ExportReportJob;
import zyj.report.business.job.QueryReportJob;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.RptTaskQueue;
import zyj.report.exception.report.ReportJobTransferException;
import zyj.report.messaging.MessageSender;
import zyj.report.model.ExportJobQueue;
import zyj.report.model.JobQueue;

import java.io.File;
import java.util.*;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/25
 */
@Service
public class ReportMsgService {

    private Logger logger = LoggerFactory.getLogger(ReportMsgService.class);
    @Autowired
    MessageSender messageSender;
    @Autowired
    JobQueue jobQueue;
    @Value("${rpt.path}")
    String rptPath;

    public void send(Job job, String msg) throws Exception {
        AbstractReportJob rptJob = (AbstractReportJob)job;
        logger.info(String.format("Messaging to ***, Job [%s], is Succeed? [%b], with information [%s]",job.getID(), rptJob.getState()== Job.STATE.SUCCEED ? true:false , msg));
        String data = "{" +
                "\"paperId\":\"" + rptJob.getID() +
                "\", \"reportType\":" + rptJob.getRptType() +
                ", \"studentType\":" + rptJob.getStuType() +
                ", \"reportStatus\":" + (job.getState()== Job.STATE.WAITTING?0:(rptJob.getState()== Job.STATE.RUNNING?1:(rptJob.getState()== Job.STATE.SUCCEED?2:(rptJob.getState()== Job.STATE.FAILED?3:-1))))+
                ", \"fileUrl\":\"http://10.136.13.54:8080/download/report/dldrpt?examid=" + rptJob.getID() + "&stutype=" + rptJob.getStuType() + "&rpttype=" + rptJob.getRptType() +
                "\"}";
        messageSender.sendMessage(data);
    }

    private Job transMsgToJob(String data) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromString(data);
            JSONArray paperexamidsJSON = jsonObject.getJSONArray("paperExamIds");
            List<String> paperexamids = new ArrayList<>();
            for(Iterator i = paperexamidsJSON.iterator();i.hasNext();){
                paperexamids.add(i.next().toString());
            }

            String paperId = jsonObject.getString("paperId");
//            int status = jsonObject.getInt("status");
            int stuType = jsonObject.getInt("studentType");
            int rptType = jsonObject.getInt("reportType");
            boolean redo = jsonObject.getBoolean("reDo");
            ExportReportJob exportReportJob = new ExportReportJob(paperId,stuType,rptType,redo,paperexamids);
            return exportReportJob;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
      /*  List<String> paperids = Arrays.asList(new String[]{"all"});
        return new ExportReportJob("e43548ed-677e-4f86-a762-60808eb08299",0,0,true,paperids);*/
    }

    public void dispatchMsg(String data) throws Exception {
        Job job = transMsgToJob(data);

        if (job instanceof ExportReportJob){

            ExportReportJob exportReportJob = (ExportReportJob)job;
            //检查本地是否有这个Job的历史报表
            if (checkJobHistory(exportReportJob)){
                if (exportReportJob.getRedo()){//重新生成，将本地的删除
                    deleteHistoryData(exportReportJob);
                }else{
                    send(exportReportJob, "存在历史报表，请直接下载或者重新生成！");
                }
            }

            if (exportReportJob != null && !jobQueue.contains(exportReportJob)){

                jobQueue.add(exportReportJob);
            }
            else throw new ReportJobTransferException("队列中已经包含这个job!");


//            tell((ExportReportJob)job,rptTaskQueue);

        }else if (job instanceof QueryReportJob){

        }else if (job instanceof CancelReportJob){

        }

    }



    private boolean checkJobHistory(ExportReportJob job){
        File[] a = new File(rptPath).listFiles();
        for (File file : a) {
            for (String pid : job.getPaperIds()) {
                if (file.getName().indexOf(job.getID()+"_"+job.getRptType()+"_"+job.getStuType()+"_")!=-1&&file.getName().endsWith(".zip")&&(file.getName().indexOf(pid) != -1||file.getName().indexOf("all") != -1)){
                    return true;

                }
            }
        }
        return false;
    }

    private void deleteHistoryData(ExportReportJob job){
        File[] a = new File(rptPath).listFiles();
        for (File file : a) {
            for (String pid : job.getPaperIds()) {
                if (file.getName().indexOf(job.getID()+"_"+job.getRptType()+"_"+job.getStuType()+"_")!=-1&&file.getName().endsWith(".zip")&&(file.getName().indexOf(pid) != -1||file.getName().indexOf("all") != -1)){
                        file.delete();
                }
            }
        }
    }
}