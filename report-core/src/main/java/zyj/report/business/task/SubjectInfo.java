package zyj.report.business.task;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 由于mybatis的foreach不支持Map的get,set,只支持用对象来实现 #{subjectInfo.subject}、#{subjectInfo.paperId}、#{subjectInfo.subjectName}
 * @Company 广东全通教育股份公司
 * @date 2016/8/18
 */
public class SubjectInfo {
    private String paperId;
    private String subject;
    private String subjectName;
    private int type;

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SubjectInfo(String paperId, String subject, String subjectName) {
        this(paperId,subject,subjectName,0);
    }
    public SubjectInfo(String paperId, String subject, String subjectName,int type) {
        this.paperId = paperId;
        this.subject = subject;
        this.subjectName = subjectName;
        this.type = type;
    }
    public SubjectInfo(){}
}
