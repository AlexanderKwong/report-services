package zyj.report.business.task;
/**
 * 报表生成参数
 * @author admin
 *
 */
public abstract class RptParameter {

	public static final int TONGYONG = 0;
	public static final int XIAOGAN = 1;
	public static final int ZHONGSHAN = 2;
	
	public abstract int getRptType();
	
	
}
