package zyj.report.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/20
 */
@Configuration
@ComponentScan(basePackages = "zyj.report.*")
@PropertySource("classpath:/config/database_configuration.properties")
public class DatabaseConfiguration {

    /*@Value("${dbpool.url}")
    String url;
    @Value("${dbpool.username}")
    String username;
    @Value("${dbpool.password}")
    String password;
    @Value("${dbpool.driver}")
    String driver;*/

    //选用Spring更推荐的获得properties的方式，减少类成员项，但是在类声明处需加注解@PropertySource并指明路径/多路径
    @Autowired
    Environment env;


    @Bean(name="sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactoryBean() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:zyj/report/persistence/map/*.xml" ));
//        mapperScannerConfigurer().setSqlSessionFactory( sqlSessionFactoryBean.getObject());
        return sqlSessionFactoryBean;
    }

    //这里必须声明为static，不声明为静态的话就必须将方法的持有者（即Configuration对象）初始化，才能访问其方法，而静态的方法在类装载后就可以生成bean
    //若实例化了Configuration对象后再生成bean，此时成员对象都被初始化了，实测比PropertySourcePlaceholderConfigurer更早初始化，此时读不出配置
    @Bean
    public static MapperScannerConfigurer mapperScannerConfigurer() {

        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage("zyj.report.persistence");
//        mapperScannerConfigurer.setSqlSessionFactory( sqlSessionFactoryBean().getObject());

        // spring在初始化上下文后调用refresh()->invokeBeanFactoryPostProcessors()，这个方法中会找出所有bean匹配类型为BeanDefinitionRegistryPostProcessor的
        // 来将其实例化，并调用postProcessBeanDefinitionRegistry()，而MapperScannerConfigurer继承这个类
        // 为了避免提前初始化dataSource，使其能够使用动态配置（即在static的PropertySourcesPlaceholderConfigurer被实例化后之后再实例化）
        //可采取的措施有两种：1 、在 SqlSessionFactoryBean()中调用mapperScannerConfigurer().setSqlSessionFactory( sqlSessionFactoryBean.getObject());
        //但是这样加大了耦合，选用 2、
        //使用spring推荐的方式，使用bean名字来代替bean引用，从而避免将关联的Bean实例化
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return mapperScannerConfigurer;
    }

    @Bean(initMethod = "init",destroyMethod = "close")
    public DataSource dataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();

        druidDataSource.setUrl(env.getProperty("dbpool.url"));
        druidDataSource.setUsername(env.getProperty("dbpool.username"));
        druidDataSource.setPassword(env.getProperty("dbpool.password"));
        druidDataSource.setDriverClassName(env.getProperty("dbpool.driver"));

        druidDataSource.setInitialSize(env.getRequiredProperty("dbpool.initialSize", Integer.class));
        druidDataSource.setMinIdle(env.getRequiredProperty("dbpool.minIdle", Integer.class));
        druidDataSource.setMaxActive(env.getRequiredProperty("dbpool.maxActive", Integer.class));
        druidDataSource.setMaxWait(env.getRequiredProperty("dbpool.maxWait", Integer.class));
        druidDataSource.setTimeBetweenEvictionRunsMillis(env.getRequiredProperty("dbpool.timeBetweenEvictionRunsMills", Integer.class));
        druidDataSource.setMinEvictableIdleTimeMillis(env.getRequiredProperty("dbpool.minEvictableIdleTimeMillis", Integer.class));

        druidDataSource.setValidationQuery(env.getRequiredProperty("dbpool.validationQuery"));
        druidDataSource.setTestWhileIdle(env.getRequiredProperty("dbpool.testWhileIdle", Boolean.class));
        druidDataSource.setTestOnBorrow(env.getRequiredProperty("dbpool.testOnBorrow", Boolean.class));
        druidDataSource.setTestOnReturn(env.getRequiredProperty("dbpool.testOnReturn", Boolean.class));

        druidDataSource.setPoolPreparedStatements(env.getRequiredProperty("dbpool.poolPreparedStatements", Boolean.class));
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(env.getRequiredProperty("dbpool.maxPoolPreparedStatementPerConnectionSize", Integer.class));
        druidDataSource.setRemoveAbandoned(env.getRequiredProperty("dbpool.removeAbandoned", Boolean.class));
        druidDataSource.setRemoveAbandonedTimeout(env.getRequiredProperty("dbpool.removeAbandonedTimeout", Integer.class));
        druidDataSource.setLogAbandoned(env.getRequiredProperty("dbpool.logAbandoned", Boolean.class));

        return druidDataSource;
    }

}
