package zyj.report;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.BroadcastRouter;
import akka.routing.RandomRouter;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import zyj.report.actor.ClientActor;
import zyj.report.actor.DispatcherActor;
import zyj.report.business.Job;
import zyj.report.business.job.ExportReportJob;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.RptTaskQueue;
import zyj.report.configuration.AppConfig;
import zyj.report.model.ExportJobQueue;
import zyj.report.model.JobQueue;
import zyj.report.service.ReportMsgService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/20
 */
public class ClientApplication {

    public static void main(String[] args){
        //启动spring容器
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(
                AppConfig.class);
        //获取Job队列 引用
        ExportJobQueue jobQueue = (ExportJobQueue)context.getBean("jobQueue");

        //声明ActorSystem
        ActorSystem system = ActorSystem.create("client", ConfigFactory.load().getConfig("ClientApp"));

        //客户端的调度
//        final ActorRef remoteActor1 = system
//                .actorFor("akka://RemoteApp1@127.0.0.1:9999/user/RemoteApp1Processor");
//        final ActorRef remoteActor2 = system
//                .actorFor("akka://RemoteApp2@127.0.0.1:9998/user/RemoteApp2Processor");
//        Iterable<ActorRef> routees = Arrays.asList(new ActorRef[]{remoteActor1/*,
//                remoteActor2*/})
// ;
        List<String> remoteHost =  system.settings().config().getStringList("remote");
        List<ActorRef> routees = new ArrayList<>();
        for (String r : remoteHost){
            final ActorRef remoteActor1 = system
                .actorFor("akka://RemoteApp@"+ r +"/user/RemoteAppProcessor");
            routees.add(remoteActor1);
        }
        ActorRef randomRouter = system.actorOf(new Props(UntypedActor.class)
                .withRouter(RandomRouter.create(routees)));
        ActorRef broadcastRouter = system.actorOf(new Props(UntypedActor.class)
                .withRouter(BroadcastRouter.create(routees)));
        ActorRef clientActor = system.actorOf(new Props(() -> new ClientActor(randomRouter, broadcastRouter)));

        try{
            /**
             * test
             */
//            ((ReportMsgService)context.getBean("reportMsgService")).dispatchMsg("{\"paperExamIds\":[\"all\"],\"paperId\":\"e43548ed-677e-4f86-a762-60808eb08299\",\"reDo\":true,\"reportType\":0,\"studentType\":0}");
            ;
            while(true){

                while(!jobQueue.isEmpty()){
                    //获取job  这里用get 而不是remove是为了后续跟踪job的进度，以及异常处理
                    ExportReportJob job = (ExportReportJob)jobQueue.getWaittingJob();
                    if (job == null) continue;
                    //取出它对应的taskQueue
                    synchronized (job){
                        job.setState(Job.STATE.RUNNING);
                    }
                    RptTaskQueue<RptTask> rptTaskQueue = (RptTaskQueue<RptTask>)jobQueue.getTaskList(job).clone();
                    /***** 下面三行代码还可以封装一 下，每一个job构造一个dispatcherActor，至于是串行start还是并行start可以另外控制 *****/
                    /******* 又由于logback仅支持串行输出日志到不同文件，通过set MDC 的键值对，暂时放弃并行start ********/
                    final ActorRef dispatcherActor = system.actorOf(new Props(() -> new DispatcherActor(job.getID(),rptTaskQueue)));
                    dispatcherActor.tell("start", clientActor);

                    Thread.currentThread().sleep(2000);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Application Interrupted！");
        }finally {
            system.shutdown();
        }
    }
}
