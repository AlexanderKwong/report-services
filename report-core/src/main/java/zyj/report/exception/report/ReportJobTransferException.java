package zyj.report.exception.report;

import zyj.report.exception.WarnException;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 报表确认异常，在其他报表进行中，报表进度应答，已生成报表时抛出
 * @Company 广东全通教育股份公司
 * @date 2016/8/17
 */
public class ReportJobTransferException extends WarnException{

    public ReportJobTransferException(){
        super();
    }
    public ReportJobTransferException(String message){super(message);}
}
