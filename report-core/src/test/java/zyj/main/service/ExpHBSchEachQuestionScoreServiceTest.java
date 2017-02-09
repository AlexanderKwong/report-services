package zyj.main.service;

import org.junit.Before;
import org.junit.Test;
import zyj.main.BaseExportTest;
import zyj.report.common.ExportUtil;

import java.util.HashMap;
import java.util.Map;


public class ExpHBSchEachQuestionScoreServiceTest extends BaseExportTest{

    @Before
    public void setUp(){
        Map<String,Object> subject = new HashMap<>();
        subject.put("SUBJECT","WL");
        subject.put("SUBJECT_NAME","物理");
        subject.put("PAPER_ID","3dab5722-18cc-49ca-84a2-a0744ae85ec9");
        subject.put("TYPE",2);
        setParmter("expHBSchEachQuestionScoreService",
                "386840d9-bf0c-4086-a762-5ca129521950",
                "350800",
                subject,
                "school",
                1,
                "abfa5b06-362f-4d34-9d94-ff82b09be3e1");
    }

    @Test
    public void export(){
        try{
            ExportUtil.export(getParmter());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
