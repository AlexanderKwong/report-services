package zyj.report.business.job;

import zyj.report.business.Job;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/9/9
 */
public class AbstractReportJob implements Job {

    public final int DEFAULT = 0, XIAOGAN = 1, ZHONGSHAN = 2;

    protected STATE state;

    protected String id;

    protected int stuType;

    protected int rptType;

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        synchronized (this){
            this.state = state;
        }
    }

    public String getID() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStuType() {
        return stuType;
    }

    public void setStuType(int stuType) {
        this.stuType = stuType;
    }

    public int getRptType() {
        return rptType;
    }

    public void setRptType(int rptType) {
        this.rptType = rptType;
    }

    public int compareTo(Job o) {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  Job)
            return equals((Job)obj);
        return super.equals(obj);
    }

    public AbstractReportJob(){};

    public AbstractReportJob(String id, int stuType, int rptType) {
        this.state = STATE.WAITTING;
        this.id = id;
        this.stuType = stuType;
        this.rptType = rptType;
    }
}
