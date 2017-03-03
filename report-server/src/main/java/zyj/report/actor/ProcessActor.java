package zyj.report.actor;

import akka.actor.*;
import akka.japi.Function;
import akka.routing.RoundRobinRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import zyj.report.business.Task;
import zyj.report.business.progress.TaskProgress;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.impl.*;

import java.util.HashMap;
import java.util.Map;

import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;
import static akka.actor.SupervisorStrategy.escalate;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/10
 */
public class ProcessActor  extends UntypedActor {

    private Logger logger = LoggerFactory.getLogger(ProcessActor.class);

    private ActorRef taskRouter;

    private Map<String,TaskProgress> tasksTracingMap = new HashMap<>();

    public ProcessActor(int threshold){
        taskRouter = this.getContext().actorOf(new Props(() -> new TaskRunnerActor(getSelf())).withDispatcher("pinnedDispatcher").withRouter(new RoundRobinRouter(threshold)), "RemoteAppRouter");
    };

    /**
     * (1)当收到wait或者EOF时，就会setIsAllAccepted(true);
     * (2)导出，打包上传 都是由client来控制;
     *
     **/
    @Override
    public void onReceive(Object o) throws Exception {
        
        if (o instanceof RptTask){//导出任务
            RptTask task = (RptTask)o;

            if (task instanceof RptTaskWait){ //需要等待
                tasksTracingMap.get(task.getJobId()).setIsAllAccepted(true);
                logger.info(String.format("Job [%s] 收到 [暂停] 命令。",task.getJobId()));
                return;
            }

            if (task.getState() == Task.STATE.WAITTING){ //待进行的任务

                if (tasksTracingMap.containsKey(task.getJobId())) {
                    tasksTracingMap.get(task.getJobId()).setIsAllAccepted(false);
                }else {
                    String jobRootPath = task.getPathFile().substring(0, task.getPathFile().indexOf("]")+1);
                    tasksTracingMap.putIfAbsent(task.getJobId(), new TaskProgress(task.getJobId(),jobRootPath  ,getSender(), 0));
                }

                tasksTracingMap.computeIfPresent(task.getJobId(),(id,taskProgress)->{
                    taskProgress.totalAdd(); return taskProgress;
                });

                task.setState(Task.STATE.RUNNING);
                taskRouter.tell(task);

            }else { //已经进行了的任务
                TaskProgress taskProgress = tasksTracingMap.get(task.getJobId());
                if (taskProgress == null) return;

                if (task.getState() == Task.STATE.SUCCEED){
                    taskProgress.countSucceedAdd();
                }else if (task.getState() == Task.STATE.FAILED){
                    taskProgress.countFailedAdd();
                }
                //成功 + 失败 = 总数 且 接收完成 -> 将 进度 发回去给 client
                if (taskProgress.isAllAccepted() && taskProgress.getCountSucceed()+taskProgress.getCountFailed() == taskProgress.getTotal()){
                    logger.info(String.format("Job [%s] 已接收任务数:%d，成功执行任务数:%d，失败任务数:%d ==>本阶段任务已全部完成。",taskProgress.getJobId(), taskProgress.getTotal(),taskProgress.getCountSucceed(), taskProgress.getCountFailed()));
                    taskProgress.getSender().tell(taskProgress, getSelf());

                }else {
                    logger.info(String.format("Job [%s] 已接收任务数:%d，成功执行任务数:%d，失败任务数:%d",taskProgress.getJobId(), taskProgress.getTotal(),taskProgress.getCountSucceed(), taskProgress.getCountFailed()));
                }
            }
        }
/*
        if (o instanceof RptTaskWait){ //等待任务

        }*/

        if (o instanceof EOFTask){ //eof
            EOFTask task = (EOFTask)o;
            if (tasksTracingMap.containsKey(task.getJobId())){
                tasksTracingMap.get(task.getJobId()).setIsAllAccepted(true);
                logger.info(String.format("Job [%s] 所有子任务接收完全。",task.getJobId()));
            }

            TaskProgress taskProgress = tasksTracingMap.get(task.getJobId());
            if (taskProgress.isAllAccepted() && taskProgress.getCountSucceed()+taskProgress.getCountFailed() == taskProgress.getTotal()){
                logger.info(String.format("Job [%s] 已接收任务数:%d，成功执行任务数:%d，失败任务数:%d ==>本阶段任务已全部完成。",taskProgress.getJobId(), taskProgress.getTotal(),taskProgress.getCountSucceed(), taskProgress.getCountFailed()));
                taskProgress.getSender().tell(taskProgress);
            }
            return;
        }

        if (o instanceof MergeTask){ //合并任务 完成
            MergeTask task = (MergeTask)o;
            String jobId = task.getJobId();
            TaskProgress taskProgress = tasksTracingMap.get(jobId);
            if (task.getState()== Task.STATE.WAITTING){
                //打包
                logger.info(String.format("Job [%s] 准备压缩打包",jobId));
                taskRouter.tell(task, getSelf());
            }else if (task.getState()== Task.STATE.SUCCEED){
                logger.info(String.format("Job [%s] 准备上传",jobId));
                taskProgress.setJobRootPath(task.getRptpath());
                taskRouter.tell(new UploadTask(taskProgress.getJobId(), task.getRptpath(), taskProgress.getTotal()), getSelf());
            }else if (task.getState()== Task.STATE.FAILED){
                ActorRef actorRef = taskProgress.getSender();
                actorRef.tell(String.format("Job [%s] merge failed in %s.",taskProgress.getJobId(),getSelf().path()), getSelf());
            }
        }

        if (o instanceof UploadTask){ //上传任务 完成
            UploadTask task = (UploadTask)o;
            TaskProgress taskProgress = tasksTracingMap.get(task.getJobId());
            ActorRef actorRef = taskProgress.getSender();
            if (task.getState()== Task.STATE.SUCCEED){
                logger.info(String.format("Job [%s] finished in %s.",taskProgress.getJobId(),getSelf().path()));
            }else {
                logger.info(String.format("Job [%s] upload failed in %s.",taskProgress.getJobId(),getSelf().path()));
            }
            //上传成功与否由客户端判断
            actorRef.tell(task);
        }
        if (o instanceof CleanTask){
            CleanTask task = (CleanTask)o;
            TaskProgress taskProgress = tasksTracingMap.get(task.getJobId());
            if (taskProgress != null){
                task.setRptpath(taskProgress.getJobRootPath());
                tasksTracingMap.remove(task.getJobId());
            }
            taskRouter.tell(task);

        }
    }


    private static SupervisorStrategy strategy = new OneForOneStrategy(10,
            Duration.create("10 second"), new Function<Throwable, SupervisorStrategy.Directive>() {
        public SupervisorStrategy.Directive apply(Throwable t) {
            if (t instanceof Exception) {
                return resume();
            } else if (t instanceof Error) {
                return restart();
            } else {
                return escalate();
            }
        }
    });
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
}
