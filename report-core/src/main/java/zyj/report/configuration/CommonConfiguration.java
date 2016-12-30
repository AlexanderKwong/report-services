package zyj.report.configuration;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import zyj.report.common.SpringUtil;
import zyj.report.common.util.InstantiationTracingBeanPostProcessor;

import java.io.IOException;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/20
 */
@Configuration
public class CommonConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() throws IOException {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/config/*.properties" ));
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public InstantiationTracingBeanPostProcessor instantiationTracingBeanPostProcessor(){
        return new InstantiationTracingBeanPostProcessor();
    }

    @Bean
    public CustomizeAnnotationProxy customizeAnnotationProxy() throws Exception {
        CustomizeAnnotationProxy customizeAnnotationProxy = new CustomizeAnnotationProxy();
        SpringUtil.initSprintUtil(customizeAnnotationProxy);
        return customizeAnnotationProxy;
    }

}
