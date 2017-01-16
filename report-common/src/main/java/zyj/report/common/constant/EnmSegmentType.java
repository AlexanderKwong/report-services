package zyj.report.common.constant;

/**
 * Created by CXinZhi on 2017/1/10.
 * <p>
 * 分区类别
 */
public enum EnmSegmentType {

	ROUNDED(0, "四舍五入"),
	CEILING(1, "向上取整"),
	FLOOR(1, "向下取整");

	private Integer code;
	private String name;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	EnmSegmentType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	/**
	 * 通过名称找到 枚举值
	 *
	 * @param name
	 * @return
	 */
	public static Integer getCode(String name) {
		if ((name == null) || name.equals(""))
			return null;

		for (EnmSegmentType e : values()) {
			if (e.name.equals(name)) {
				return e.getCode();
			}
		}
		return null;
	}

	/**
	 * 通过枚举值找到 名称
	 *
	 * @param v
	 * @return
	 */
	public static String getName(Integer v) {

		for (EnmSegmentType e : values()) {
			if (e.code == v) {
				return e.name;
			}
		}
		return "-";
	}


}
