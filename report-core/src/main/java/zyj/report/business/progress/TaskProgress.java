package zyj.report.business.progress;


import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/27
 */
public class TaskProgress implements Serializable{
    private static final long serialVersionUID = -5898989470754612345L;

    String jobId;

    String jobRootPath;

    ActorRef sender;

    int total;

    int countSucceed;

    int countFailed;

    boolean isAllAccepted;

    public TaskProgress(String jobId, ActorRef sender, int total) {
        this(jobId,null,sender,total);
    }

    public TaskProgress(String jobId, String jobRootPath, ActorRef sender, int total) {
        this.jobId = jobId;
        this.jobRootPath = jobRootPath;
        this.sender = sender;
        this.total = total;
        this.countSucceed = 0;
        this.countFailed = 0;
        this.isAllAccepted = false;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobRootPath() {
        return jobRootPath;
    }

    public void setJobRootPath(String jobRootPath) {
        this.jobRootPath = jobRootPath;
    }

    public ActorRef getSender() {
        return sender;
    }

    public void setSender(ActorRef sender) {
        this.sender = sender;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCountSucceed() {
        return countSucceed;
    }

    public void setCountSucceed(int countSucceed) {
        this.countSucceed = countSucceed;
    }

    public int getCountFailed() {
        return countFailed;
    }

    public void setCountFailed(int countFailed) {
        this.countFailed = countFailed;
    }

    public boolean isAllAccepted() {
        return isAllAccepted;
    }

    public void setIsAllAccepted(boolean isAllAccepted) {
        this.isAllAccepted = isAllAccepted;
    }

    public void totalAdd(){
        this.total++;
    }

    public void countSucceedAdd(){
        this.countSucceed++;
    }

    public void countFailedAdd(){
        this.countFailed++;
    }

    public void countSucceedAdd(int succeedNum){ this.countSucceed = this.countSucceed + succeedNum ; }

    public void countFailedAdd(int failedNum){ this.countFailed = this.countFailed + failedNum ; }
}
