package zyj.main;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import zyj.report.common.SpringUtil;
import zyj.report.configuration.AppConfig;
import zyj.report.service.redis.RedisService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/8/23
 */


//首先指定Junit的Runner
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class BaseExportTest  {

//导出的参数
    private Map<String ,Object> parmter;
//导出路径
    final private String pathFile = "d:/testxls/zskcs_5/";

   /* @Before
    public void initSpring(){
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
    }*/


    protected Map<String, Object> getParmter() {
        return parmter;
    }

    protected void setParmter(String server,String exambatchId,String cityCode,String subject ,String level,Integer stuType) {
        parmter = new HashMap<>();
        parmter.put("server", server);
        parmter.put("exambatchId", exambatchId);
        parmter.put("cityCode", cityCode);
        parmter.put("subject", subject);
        parmter.put("level", level);
        parmter.put("stuType", stuType);
        parmter.put("pathFile", pathFile);
    }
    protected void setParmter(String server,String exambatchId,String cityCode,Map<String ,Object> subject ,String level,Integer stuType,String scopeId) {
        parmter = new HashMap<>();
        parmter.put("server", server);
        parmter.put("exambatchId", exambatchId);
        parmter.put("cityCode", cityCode);
//        parmter.put("subject", subject);
        parmter.put("level", level);
        parmter.put("stuType", stuType);
        parmter.put("pathFile", pathFile);
        if(subject!=null){
            parmter.put("subject", subject.get("SUBJECT"));
            parmter.put("subjectName", subject.get("SUBJECT_NAME"));
            parmter.put("paperId", subject.get("PAPER_ID"));
            parmter.put("type", subject.get("TYPE"));
        }
        if (scopeId != null){
            parmter.put(level+"Id", scopeId);
        }
    }

    @Ignore
    @Test
    public void export(){
        try{

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @After
    public  void clearCache(){
        //清缓存
        RedisService redisService = (RedisService) SpringUtil.getSpringBean(null,"redisService");
        Set<String> keys = redisService.keys( parmter.get("exambatchId") + "*");
        redisService.del(keys.toArray(new String[keys.size()]));
        System.out.println("清理缓存结束");
    }
}
