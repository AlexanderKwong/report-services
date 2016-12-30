package zyj.report.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zyj.report.business.Task;
import zyj.report.business.progress.TaskProgress;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.impl.*;
import zyj.report.common.util.FileUtil;
import zyj.report.common.util.ZipUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/9
 */
public class ClientActor extends UntypedActor {

    private Logger logger = LoggerFactory.getLogger(ClientActor.class);

    private ActorRef remoteServer = null;

    private ActorRef broadcastRemoteServer = null;

    private ActorRef resultActor = null;

    private Map<String,TaskProgress> tasksTracingMap = new HashMap<>();


    /**
     * @param
     */
    public ClientActor(ActorRef inRemoteServer,ActorRef broadcastRemoteServer) {

        this.remoteServer = inRemoteServer;
        this.broadcastRemoteServer = broadcastRemoteServer;
        this.resultActor = this.context().actorOf(new Props(ResultActor.class));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            String msg = (String) message;
            logger.info(msg);
        }
        if (message instanceof RptTask) {
            RptTask task = (RptTask) message;
            String jobId = task.getJobId();
            if (task instanceof RptTaskWait){
                tasksTracingMap.get(jobId).setIsAllAccepted(true);
                broadcastRemoteServer.tell(task,getSelf());
            }
            else{

                tasksTracingMap.putIfAbsent(jobId, new TaskProgress(jobId, task.getPathFile().substring(0, task.getPathFile().indexOf("]")+1),getSender(), 0));
                if(tasksTracingMap.get(jobId).isAllAccepted()){//如果已经完成一个阶段的，则重新开启，并清空进度
                    TaskProgress taskProgress =  tasksTracingMap.get(jobId);
                    taskProgress.setIsAllAccepted(false);
                    taskProgress.setCountSucceed(0);
                    taskProgress.setCountFailed(0);
                }
                tasksTracingMap.computeIfPresent(jobId, (id, taskProgress) -> {
                    taskProgress.totalAdd();
                    return taskProgress;
                });
                remoteServer.tell(task, getSelf());
            }
        }else if (message instanceof EOFTask){
            EOFTask task = (EOFTask)message;
            TaskProgress taskProgress = tasksTracingMap.get(task.getJobId());
            if (taskProgress.isAllAccepted()){
                //已经完成 -> 打包上传
                logger.info(String.format("Job [%s] 所有任务已经执行完毕，共%d个子任务 --> 请求各节点打包上传",taskProgress.getJobId(), taskProgress.getTotal()));
                broadcastRemoteServer.tell(new MergeTask(taskProgress.getJobId(),taskProgress.getJobRootPath()));
                //将成功失败数清零，用于统计上传
                taskProgress.setCountSucceed(0);
                taskProgress.setCountFailed(0);
                //回收dispatcher
                this.context().system().stop(getSender());
            }else{
                logger.info(String.format("Job [%s] 所有任务已经派送完毕，共%d个子任务 ",taskProgress.getJobId(), taskProgress.getTotal()));
                taskProgress.setIsAllAccepted(true);
                broadcastRemoteServer.tell(task,getSelf());
            }
        }

        if (message instanceof TaskProgress){//server回复进度
            TaskProgress taskProgress_reply = (TaskProgress)message;
            String jobId = taskProgress_reply.getJobId();
            if (!tasksTracingMap.containsKey(jobId)){
                logger.error("ERROR:client没有这个job的数据。");
                return;
            }else {
                TaskProgress taskProgress = tasksTracingMap.get(jobId);
                taskProgress.countSucceedAdd(taskProgress_reply.getCountSucceed());
                taskProgress.countFailedAdd(taskProgress_reply.getCountFailed());
                logger.info(String.format("Job [%s] 已接收任务数:%d，成功执行任务数:%d，失败任务数:%d ",taskProgress.getJobId(), taskProgress.getTotal(),taskProgress.getCountSucceed(), taskProgress.getCountFailed()));
                if (taskProgress_reply.getCountFailed() != 0){
                    logger.info(String.format("Job [%s] 节点%s存在失败任务，成功执行任务数:%d，失败任务数:%d ",taskProgress_reply.getJobId(), getSender().path(),taskProgress_reply.getCountSucceed(), taskProgress_reply.getCountFailed()));
                }
                if (taskProgress.isAllAccepted() && taskProgress.getCountSucceed()+taskProgress.getCountFailed() == taskProgress.getTotal()){
                    if (taskProgress.getCountFailed() != 0){
                        //TODO 发消息 -> 失败, 同时清除记录
                        resultActor.tell(taskProgress.getJobId() + "_" + "failed_存在子任务执行失败。", getSelf());
                    }else{
                        taskProgress.getSender().tell("start", getSelf());
                    }
                }
            }
        }

        if (message instanceof UploadTask){ //上传任务 完成
            UploadTask task = (UploadTask)message;
            TaskProgress taskProgress = tasksTracingMap.get(task.getJobId());
            if (task.getState()== Task.STATE.SUCCEED){
                //检查是否真的有这个文件存在于本地目录
                File upload = new File(task.getRptFilePath());
                if (!upload.exists()){
                    logger.error("ERROR:没有找到上传的文件。");
                    taskProgress.countFailedAdd(task.getTotal());
                }else {
                    //将上传的进度更新
                    taskProgress.countSucceedAdd(task.getTotal());
                }

            }else {
                taskProgress.countFailedAdd(task.getTotal());
            }

            if (taskProgress.isAllAccepted() && taskProgress.getCountSucceed()+taskProgress.getCountFailed() == taskProgress.getTotal()){

                if (taskProgress.getCountFailed() != 0 ) {
                    logger.error("ERROR:client上传压缩包失败。");
                    resultActor.tell(taskProgress.getJobId() + "_" + "failed_client上传压缩包失败" , getSelf());
                    return;
                }

                //本地合并
                String rootPath = taskProgress.getJobRootPath();
                int index = rootPath.lastIndexOf("/");
                String srcPath = rootPath.substring(0,index);
                String fileName = rootPath.substring(index+1);
                try {
                    //合并
                    merge(srcPath, fileName);
                    //TODO 发消息 -> 成功
                    resultActor.tell(taskProgress.getJobId() + "_" + "succeed_报表生成完毕", getSelf());
                }catch (IOException e){
                    logger.error("ERROR:合并压缩包失败。",e);
                    //TODO 发消息 -> 失败
                    resultActor.tell(taskProgress.getJobId() + "_" + "failed_合并压缩包失败" , getSelf());
                }

            }
        }
        if (message instanceof CleanTask){
            CleanTask task = (CleanTask)message;
            tasksTracingMap.remove(task.getJobId());
            broadcastRemoteServer.tell(task, getSelf());
        }
    }

    private void merge(String srcPath, String fileName) throws IOException {
        long mills = System.currentTimeMillis();
        String destPath = srcPath+"/merge_"+fileName;
        logger.debug("源路径为： "+srcPath);
        logger.debug("目标路径为： "+destPath);
        File f = new File(srcPath);
        File ff = new File(destPath);

        FileUtil.rmvDir(destPath);
        ff.mkdir();
        File[] files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith("zip")&&name.startsWith(fileName))
                    return true;
                else
                    return false;
            }
        });
        logger.debug("正在合并，请耐心等待！");
        for (File file : files) {
            ZipUtil.unZip(file.getAbsolutePath(), destPath + "/");
        }
        String[]filenames =  new File(destPath).list();
        for (String string : filenames) {
            logger.debug(string);
        }
        String fname = srcPath+"/" + fileName;
        ZipUtil.zipDir(destPath, fname+".zip");
        FileUtil.rmvDir(destPath);
        //todo 这里应该将其它子节点传过来的包删掉 测试阶段先不删，留着
        long cost  = System.currentTimeMillis() - mills;
        logger.debug("success!耗时 ： " + cost);
    }
}
