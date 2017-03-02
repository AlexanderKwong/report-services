package zyj.report.business.task;

/**
 * 报表生成参数
 *
 * @author admin
 */
public abstract class RptParameter {

	/**
	 *
	 * TONGYONG : 通用
	 * XIAOGAN	: 孝感
	 * ZHONGSHAN: 中山
	 * HUBEI	: 湖北
	 */
	public static final int TONGYONG = 0, XIAOGAN = 1, ZHONGSHAN = 2, HUBEI = 3;

	public abstract int getRptType();

	public String getVersionName(){
		switch (getRptType()){
			case TONGYONG:
				return "通用版";
			case XIAOGAN:
				return "孝感版";
			case ZHONGSHAN:
				return "中山版";
			case HUBEI:
				return "湖北版";
			default:
				return "";
		}
	}
}
