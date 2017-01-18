package zyj.report.common.constant;

/**
 * Created by CXinZhi on 2017/1/10.
 * <p>
 *  学生类别
 */
public enum EnmStudentType {

	CURRSTU(0, "应届学生"),
	RESTU(1, "往届学生"),
	STUTYPE2(2, "类型2"),
	STUTYPE3(3, "类型3"),
	STUTYPE4(4, "类型4"),
	STUTYPE5(5, "类型5");

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

	EnmStudentType(int code, String name) {
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

		for (EnmStudentType e : values()) {
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

		for (EnmStudentType e : values()) {
			if (e.code == v) {
				return e.name;
			}
		}
		return "-";
	}


}
