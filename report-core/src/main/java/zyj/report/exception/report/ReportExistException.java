package zyj.report.exception.report;

import zyj.report.exception.WarnException;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 报表已生成又再次请求时抛出
 * @Company 广东全通教育股份公司
 * @date 2016/8/17
 */
public class ReportExistException extends WarnException{
    public ReportExistException(){super();};

    public ReportExistException(String message){super(message);};
}
