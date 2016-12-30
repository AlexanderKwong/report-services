package zyj.report.business.task.impl;

import org.apache.commons.lang.StringUtils;
import zyj.report.business.Task;
import zyj.report.business.task.RptTask;
import zyj.report.common.util.ConfigUtil;
import zyj.report.common.util.FileUtil;
import zyj.report.common.util.HostUtil;
import zyj.report.common.util.StringUtil;

import java.util.Properties;
import java.util.Random;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/26
 */
public class MergeTask implements Task {

    private String rptpath;

    private String id = "merge";

    private String jobId;

    private STATE state;

    public MergeTask(String jobId, String rptpath){
        this.state = STATE.WAITTING;
        this.jobId = jobId;
        this.rptpath = rptpath;
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
//        //打包的名字为原来的文件夹名 加上终端名
//        Properties p = ConfigUtil.readPath("config/pathConf.properties");
//        String hostname = p.getProperty("hostname", "");
        String hostname = StringUtil.stringFilter(HostUtil.getHostName());
        //如果是未知的hostname，就尽量避免重复
        if (HostUtil.UNKNOWN_HOST.equals(hostname)) hostname += new Random(System.currentTimeMillis()).nextInt(999);
        else if (StringUtil.isBlank(hostname)) hostname = HostUtil.UNKNOWN_HOST + new Random(System.currentTimeMillis()).nextInt(999);

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
