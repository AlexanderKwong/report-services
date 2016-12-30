package zyj.report.exception.report;

import zyj.report.exception.ErrorException;
import zyj.report.exception.WarnException;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 导出异常，在导出过程抛出
 * @Company 广东全通教育股份公司
 * @date 2016/8/17
 */
public class ReportExportException extends ErrorException {

    public ReportExportException(){super();};

    public ReportExportException(String message){super(message);};
}
