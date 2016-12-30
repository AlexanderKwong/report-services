package zyj.report.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/20
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CacheAround {

    String value() default "";
}
