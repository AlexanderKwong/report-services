package zyj.report;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.kernel.Bootable;
import akka.routing.RoundRobinRouter;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zyj.report.actor.TaskRunnerActor;
import zyj.report.actor.ProcessActor;
import zyj.report.configuration.AppConfig;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/26
 */
public class ServerApplication  implements Bootable {


//    private ActorRef taskRouter;

    private ActorRef processActor;

    private ActorSystem system;

    public ServerApplication(int thresHold){

        system = ActorSystem.create("RemoteApp", ConfigFactory.load().getConfig("RemoteApp"));

//        taskRouter = system.actorOf(new Props(() -> new TaskRunnerActor(processActor)).withDispatcher("pinnedDispatcher").withRouter(new RoundRobinRouter(threadHold)), "RemoteApp1Router");
//
//        processActor = system.actorOf(new Props(()-> new ProcessActor(taskRouter)),"RemoteApp1Processor");

        //上面相当于 taskRouter、preocessActor的父引用都是/user(ActorSystem)，这样违背了 “包工头”管理“工人”的模式，因为是processActor分配工作给taskRouter

        processActor = system.actorOf(new Props(()-> new ProcessActor(thresHold)),"RemoteAppProcessor");
    }

    public static void main(String[] args){
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(
                AppConfig.class);
        new ServerApplication(10);
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }
}
