package zyj.report.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 执行某个查询方法前先查缓存。有则不用查询DB，直接从缓存中取数据
 * @Company 广东全通教育股份公司
 * @date 2016/10/20
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CacheBefore {

    String value() default "value";

    Scope scope();
}
