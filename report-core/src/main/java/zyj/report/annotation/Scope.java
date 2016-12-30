package zyj.report.annotation;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/11/15
 */
public enum Scope {

    SUBJECT,
    STUDENT,
    CLASS,
    SCHOOL,
    AREA,
    CITY;

    @Override
    public String toString() {
        switch (this){
            case SUBJECT:
                return "9";
            case STUDENT:
                return "4";
            case CLASS:
                return "3";
            case SCHOOL:
                return "2";
            case AREA:
                return "1";
            case CITY:
                return "0";
            default:
                return "null";
        }
    }
}
