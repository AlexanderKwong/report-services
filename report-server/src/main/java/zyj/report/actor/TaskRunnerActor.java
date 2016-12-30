package zyj.report.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zyj.report.business.Task;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.impl.CleanTask;
import zyj.report.business.task.impl.MergeTask;
import zyj.report.business.task.impl.UploadTask;
import zyj.report.common.logging.ThreadNameBasedDiscriminator;
import zyj.report.common.util.ConfigUtil;
import zyj.report.common.util.FileUtil;
import zyj.report.common.util.FtpUtil;
import zyj.report.exception.report.ReportExportException;

import java.io.*;
import java.util.Properties;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/9
 */
public class TaskRunnerActor extends UntypedActor {

    private Logger logger = LoggerFactory.getLogger(TaskRunnerActor.class);

    private ActorRef processActor;
//由于 经过路由转发的 消息 无法getSender，所以 只能通过传进来
    public TaskRunnerActor(ActorRef processActor){
        this.processActor = processActor;
    }

    @Override
    public void onReceive(Object o) throws Exception {

        //根据Thread.getName()来打log，而线程名由task.getJobId()来决定
        if (o instanceof Task){
            String jobId = ((Task) o).getJobId();
            ThreadNameBasedDiscriminator.setLogFileName(jobId);

            if (o instanceof MergeTask){ // MergeTask 也是RPTtask特殊的一种
//                logger.info(String.format("Job [%s] 准备压缩打包",jobId));
                MergeTask mergeTask = ((MergeTask)o);
                try{

                    mergeTask.run();
                    mergeTask.setState(Task.STATE.SUCCEED);
                    processActor.tell(mergeTask);
                }catch (Exception e){
                    mergeTask.setState(Task.STATE.FAILED);
                    processActor.tell(mergeTask);
                }

            } else if (o instanceof RptTask){
                RptTask task = (RptTask)o;
                try {
                    task.run();
                    task.setState(Task.STATE.SUCCEED);
                    processActor.tell(task);

                } catch (Throwable e) {

                    String dir = task.getPathFile();
                    logger.error("ERR:未知异常!--" + dir);
                    e.printStackTrace();

                    String logDir = dir.substring(0, dir.indexOf("]") + 1) + "/ErrorLog.txt";
                    File log = new File(logDir);
                    try {
                        if (!log.exists())
                            log.createNewFile();
                        logfile(logDir, dir, e);

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
//                processActor.tell(String.format("TASK %s Of Job %s FAILED in Actor %s using Thread %s", task.getId(), task.getJobId(), getSelf().path(), Thread.currentThread().getName()));
                    task.setState(Task.STATE.FAILED);
                    processActor.tell(task);
                }

            }else if (o instanceof UploadTask){
//                logger.info(String.format("Job [%s] 准备上传",jobId));
                UploadTask task = (UploadTask)o;
                try {
                    task.run();
                    task.setState(Task.STATE.SUCCEED);
                    processActor.tell(task);
                }catch (Exception e){
                    e.printStackTrace();
                    task.setState(Task.STATE.FAILED);
                    processActor.tell(task);
                }

            }else if (o instanceof CleanTask){
                //TODO 这里应调用cleanTask的run()来清理本机生成的该批次的zip文件，测试阶段暂时不清理
                CleanTask task = (CleanTask)o;
                logger.info(String.format("Job [%s] 清除数据完毕。.",task.getJobId()));
            }
        }
    }
    public boolean logfile(String dir,String msg , Throwable e) throws IOException {
        boolean flag = false;
        byte[] buff = new byte[] {};
        String message = "ERR:未知异常!--" + msg+"\r\n";
        buff = message.getBytes();
        FileOutputStream out = new FileOutputStream(dir, true);
        out.write("\r\n".getBytes());
        out.write(buff);
        e.printStackTrace(new PrintStream(out));
        out.flush();
        out.close();
        flag = true;
        return flag;
    }

}
