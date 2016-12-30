package zyj.report.business.task.impl;

import zyj.report.business.Task;
import zyj.report.common.util.ConfigUtil;
import zyj.report.common.util.FileUtil;

import java.util.Properties;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/26
 */
public class CleanTask implements Task {

    private String rptpath;

    private String id = "clean";

    private String jobId;

    private STATE state;

    public CleanTask(String jobId/*, String rptpath*/){
        this.state = STATE.WAITTING;
        this.jobId = jobId;
//        this.rptpath = rptpath;
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

    public String getRptpath() {
        return rptpath;
    }

    public void setRptpath(String rptpath) {
        this.rptpath = rptpath;
    }

    @Override
    public void run() throws Exception {
        //打包的名字为原来的文件夹名 加上终端名
        Properties p = ConfigUtil.readPath("config/pathConf.properties");
        String hostname = p.getProperty("hostname", "");
        String newPath = rptpath +"_" + hostname + ".zip";
        FileUtil.zipDir(rptpath, newPath);
        FileUtil.rmvDir(rptpath);
        setRptpath(newPath);
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
