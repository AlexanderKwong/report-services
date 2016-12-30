package zyj.report.model;

import org.apache.commons.lang.StringUtils;
import zyj.report.business.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 队列代理的模版类
 * @Company 广东全通教育股份公司
 * @date 2016/9/9
 */

public abstract class JobQueue<J extends Job> {

//含有状态的bean要注意线程安全，
    protected volatile Queue<J> jobs = new ConcurrentLinkedQueue<>();

    /**
     * 加进队列前 必须完成初始化
     * @param job
     * @throws Exception
     */
    public void add(J job) throws Exception {

        if(jobIsInited(job))
            synchronized (this){
                jobs.add(job);
            }
    }

    /**
     * 子类决定 何时 怎么 初始化job
     * @param job
     * @param args 初始化参数
     * @throws Throwable
     */
    public abstract void jobInit(J job, Object... args) throws Throwable;

    /**
     * 子类决定 job是否初始化完
     * @param job
     * @return
     */
    public abstract boolean jobIsInited(J job);

    public  J remove(J job){
        synchronized (this) {
            jobs.remove(job);
        }
        return null;
    }

    public void set(J job){

    }

    public J get(J job){
        return null;
    }

    public J get(int index){
        return null;
    }

    public int size(){
        return jobs.size();
    }

    public boolean isEmpty(){
        synchronized (this){
            return jobs.isEmpty();
        }
    }

    public J getWaittingJob(){
        for(J job : jobs){
            if(job.getState()==Job.STATE.WAITTING) return job;
        }
        return null;
    }

    public J getJobById(String jobId){
        if (StringUtils.isBlank(jobId)) return null;
        for(J job : jobs){
            if(job.getID().equals(jobId)) return job;
        }
        return null;
    }


    public boolean contains(J job){
        synchronized (this ){
            return jobs.contains(job);
        }
    }


}
