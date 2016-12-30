package zyj.report.exception.report;

import zyj.report.exception.WarnException;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 报表缓存异常
 * @Company 广东全通教育股份公司
 * @date 2016/8/17
 */
public class ReportCacheException extends WarnException {

    public ReportCacheException(){
        super();
    }
    public ReportCacheException(String message){super(message);}
}
