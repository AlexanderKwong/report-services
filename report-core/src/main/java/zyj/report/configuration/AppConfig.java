package zyj.report.configuration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractApplicationContext;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.RptTaskQueue;
import zyj.report.service.JyjRptExtService;

import java.util.*;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/28
 */
@Configuration
@ComponentScan(basePackages = "zyj.report.*")
@Import({CommonConfiguration.class, DatabaseConfiguration.class, RedisCacheConfiguration.class})
public class AppConfig {

    public static void main(String[] args) throws  Exception{

        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        final String examId = "e43548ed-677e-4f86-a762-60808eb08299";
//        final String examId = args[0];
//        final Integer stuType = Integer.valueOf(args[1]);
//        final Integer rptType = Integer.valueOf(args[2]);

        JyjRptExtService jyjRptExtService = (JyjRptExtService) context.getBean("jyjRptExtService");

        RptTaskQueue<RptTask> rptTaskQueue = jyjRptExtService.getRptTaskQueue(examId, 0, 0, Arrays.asList(new String[]{"all"}));
        //单机模式
        JyjRptExtService.MainTaskThread mainTaskThread = new JyjRptExtService.MainTaskThread(examId,examId ,rptTaskQueue);

        mainTaskThread.start();
    }

}
