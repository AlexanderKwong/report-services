package zyj.report.business;

import java.io.Serializable;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 最小粒度
 * @Company 广东全通教育股份公司
 * @date 2016/9/9
 */
public interface Task  extends Serializable {

    public enum  STATE{
        WAITTING,
        RUNNING,
        SUCCEED,
        FAILED
    }

    //任务id
    String getId();
    //该任务所属的批量操作job的id
    String getJobId();
    //任务执行
    void run()  throws Exception;

    public STATE getState();

    public void setState(STATE state);
}
