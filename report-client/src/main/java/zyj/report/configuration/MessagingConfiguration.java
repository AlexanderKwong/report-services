package zyj.report.configuration;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import zyj.report.business.job.ExportReportJob;
import zyj.report.model.ExportJobQueue;
import zyj.report.model.JobQueue;

import javax.jms.Destination;
import java.util.Arrays;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/20
 */
@Configuration
@EnableJms
@PropertySource("classpath:/config/activemq_configuration.properties")
public class MessagingConfiguration {

/*    private static final String DEFAULT_BROKER_URL = "tcp://192.168.16.102:61616";

    private static final String LISTEN_QUEUE = "reportRequest";
    private static final String REPLY_QUEUE = "reportResponse";*/

    @Autowired
    Environment env;

    @Bean
    public ActiveMQConnectionFactory connectionFactory(){
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(env.getProperty("DEFAULT_BROKER_URL") );
        connectionFactory.setTrustedPackages(Arrays.asList("zyj.report", "java.util"));
        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(){
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory());
        template.setDefaultDestinationName(env.getProperty("LISTEN_QUEUE"));
        return template;
    }

    @Bean
    public Destination destination(){
        return new ActiveMQQueue(env.getProperty("REPLY_QUEUE"));
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrency("1-1");
        return factory;
    }
    @Bean(name ="jobQueue")
    public ExportJobQueue jobQueue(){
        return new ExportJobQueue();
    }
}
