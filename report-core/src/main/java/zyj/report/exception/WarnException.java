package zyj.report.exception;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/25
 */
public class WarnException extends Exception {

    public WarnException(){
        super();
    }
    public WarnException(String message){super("WARN:"+message);}
}
