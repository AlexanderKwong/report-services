package zyj.report.business;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/9/9
 */
public interface Job extends Comparable<Job>{

    public enum  STATE{
        WAITTING,
        RUNNING,
        SUCCEED,
        FAILED
    }

    public  String getID();

    public STATE getState();

    public void setState(STATE state);

    default boolean equals(Job another){
        if(another != null)
            return this.getID().equals(another.getID());
        else return false;
    }
}
