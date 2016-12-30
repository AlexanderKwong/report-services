package zyj.report.business.task.impl;

import zyj.report.business.Task;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 标志着结束的Task
 * @Company 广东全通教育股份公司
 * @date 2016/10/25
 */
public class EOFTask implements Task{

    private static final long serialVersionUID = -5898989476544667710L;

    private String id = "EOF";

    private String jobId;

    private STATE state;

    public EOFTask(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    @Override
    public void run() throws Exception {

    }

    @Override
    public STATE getState() {
        return state;
    }

    @Override
    public void setState(STATE state) {
        this.state = state;
    }
}
