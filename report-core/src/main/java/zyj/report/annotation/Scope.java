package zyj.report.annotation;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/11/15
 */
public enum Scope {

    SUBJECT(9, "科目"),
    STUDENT(4, "学生"),
    CLASS(3, "班级"),
    SCHOOL(2, "学校"),
    AREA(1, "区县"),
    CITY(0, "市区");

    private Integer code;
    private String name;

    Scope(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.valueOf(getCode());
    }
}
