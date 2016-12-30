package zyj.report.actor;

import akka.actor.UntypedActor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zyj.report.business.Job;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.RptTaskQueue;
import zyj.report.business.task.impl.CleanTask;
import zyj.report.business.task.impl.EOFTask;
import zyj.report.business.task.impl.RptTaskWait;
import zyj.report.common.SpringUtil;
import zyj.report.exception.report.ReportTaskDispatchException;
import zyj.report.messaging.MessageSender;
import zyj.report.model.JobQueue;
import zyj.report.service.ReportMsgService;
import zyj.report.service.redis.RedisService;

import java.util.Set;
//import akka.remotetaskexcute.ExportTask;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/9
 */
public class ResultActor extends UntypedActor {

    private Logger logger = LoggerFactory.getLogger(ResultActor.class);

    JobQueue jobQueue;

    ReportMsgService reportMsgService;

    RedisService redisService;

    public ResultActor() {
        jobQueue = SpringUtil.getSpringBean(null,"jobQueue");
        reportMsgService = SpringUtil.getSpringBean(null,"reportMsgService");
        redisService = (RedisService)SpringUtil.getSpringBean(null,"redisService");
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof String){ //格式： jobId_status_message
            String[] args = ((String)o).split("_");
            Job job = jobQueue.getJobById(args[0]);
            if (job == null){
                logger.error("ERROR:client没有这个job的数据，无法从队列中清除。");
                return;
            }
            //三个步骤: 1）清除队列中的批次；2）发MQ消息给调用者；3）让client和server都清除该批次的数据（保留日志）；4）清理redis缓存
            if ("succeed".equals(args[1])){
                job.setState(Job.STATE.SUCCEED);
            }else if ("failed".equals(args[1])){
                job.setState(Job.STATE.FAILED);
            }
            jobQueue.remove(job);
            reportMsgService.send(job, args[2]);
            getSender().tell(new CleanTask(args[0]));
            Set<String> keys = redisService.keys(job.getID() + "*");
            redisService.del(keys.toArray(new String[keys.size()]));
        }
    }

}
