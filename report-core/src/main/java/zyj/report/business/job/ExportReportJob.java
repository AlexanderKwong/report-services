package zyj.report.business.job;

import zyj.report.business.Job;

import java.util.List;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/9/18
 */
public class ExportReportJob extends AbstractReportJob {

    private boolean redo;

    private List<String> paperIds;

    public boolean getRedo() {
        return redo;
    }

    public void setRedo(boolean redo) {
        this.redo = redo;
    }

    public List<String> getPaperIds() {
        return paperIds;
    }

    public void setPaperIds(List<String> paperIds) {
        this.paperIds = paperIds;
    }

    public ExportReportJob(String id, int stuType, int rptType,boolean redo,List<String> paperIds) {
        super(id, stuType, rptType);
        this.redo = redo;
        this.paperIds = paperIds;
    }

    @Override
    public String toString() {
        return "{" +
                "\"paperExamIds\":" + "\"null\"" +
                ", \"paperId\":\"" + getID() +
                "\", \"reDo\":" + redo +
                ", \"reportType\":" + getRptType() +
                ", \"studentType\":" + getStuType() +
                ", \"status\":" + (getState()== Job.STATE.WAITTING?0:(getState()==STATE.RUNNING?1:(getState()==STATE.SUCCEED?2:(getState()==STATE.SUCCEED?3:-1))))+
                '}';
    }
}
