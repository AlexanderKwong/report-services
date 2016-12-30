package zyj.report.business.task.impl;

import org.apache.commons.net.ftp.FTPClient;
import zyj.report.business.Task;
import zyj.report.common.util.ConfigUtil;
import zyj.report.common.util.FileUtil;
import zyj.report.common.util.FtpUtil;
import zyj.report.exception.report.ReportJobZipUploadException;
import zyj.report.exception.report.ReportTaskDispatchException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/26
 */
public class UploadTask implements Task {
    //报表生成文件，/data/.../****.zip，生成在终端机的什么位置，就上传到客户机的什么位置
    private String rptFilePath;

    private String id = "upload";

    private String jobId;
    //上传的是total个子任务的压缩包
    private Integer total;

    private STATE state;

    public UploadTask(String jobId, String rptFilePath, Integer total){
        this.jobId = jobId;
        this.rptFilePath = rptFilePath;
        this.total = total;
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public void run() throws Exception {
        if (rptFilePath==null && !rptFilePath.endsWith("zip")){
            throw new ReportJobZipUploadException(String.format("Job [%s] 上传失败，非法上传路径 [%s] ",jobId, rptFilePath));
        }
        upload(getRptFilePath());
    }

    @Override
    public STATE getState() {
        return state;
    }

    @Override
    public void setState(STATE state) {
        this.state = state;
    }

    public String getRptFilePath() {
        return rptFilePath;
    }

    public void setRptFilePath(String rptFilePath) {
        this.rptFilePath = rptFilePath;
    }

    public boolean upload(String filePath) throws ReportJobZipUploadException {
        try {

            Properties properties = ConfigUtil.readPath("config/pathConf.properties");

            String ftpUserName = properties.getProperty("rpt.ftpUserName");
            String ftpPassword = properties.getProperty("rpt.ftpPassword");
            String ftpHost = properties.getProperty("rpt.ftpHost");
            int ftpPort = Integer.valueOf(properties.getProperty("rpt.ftpPort"))
                    .intValue();

            FTPClient ftpClient = FtpUtil.getFTPClient(ftpHost, ftpPassword,
                    ftpUserName, ftpPort);

            // 设置PassiveMode传输
            ftpClient.enterLocalPassiveMode();
            // 设置以二进制流的方式传输
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            String filename = filePath;
            if (filePath.contains("/")) {
                filename = filePath
                        .substring(filePath.lastIndexOf("/") + 1);
            }
            InputStream input = null;
            File f = new File(filePath);
            input = new FileInputStream(f);
            try {
                boolean a = ftpClient.storeFile(filename, input);
                System.out.println("上传文件成功？ " + a);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new ReportJobZipUploadException("上传失败");
            } finally {
                input.close();
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new ReportJobZipUploadException("上传失败");
        }
    }
}
