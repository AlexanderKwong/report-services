package zyj.report.common.constant;

/**
 * Created by CXinZhi on 2017/1/10.
 * <p>
 * 科目类别
 */
public enum EnmSubjectType {

	ALL(0, "不分文理科"),
	WEN(1, "文科"),
	LI(2, "理科"),
	WEN_LI_ZONG(3,"综合文理");


	private Integer code;
	private String name;

	public Integer getCode() {
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

	EnmSubjectType(int code, String name) {
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

		for (EnmSubjectType e : values()) {
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

		for (EnmSubjectType e : values()) {
			if (e.code == v) {
				return e.name;
			}
		}
		return "-";
	}


}
