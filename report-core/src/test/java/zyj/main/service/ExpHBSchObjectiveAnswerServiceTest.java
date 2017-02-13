package zyj.main.service;

import org.junit.Before;
import org.junit.Test;
import zyj.main.BaseExportTest;
import zyj.report.common.ExportUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 邝晓林
 * @Description
 * @date 2017/1/11
 */
public class ExpHBSchObjectiveAnswerServiceTest extends BaseExportTest{

    @Before
    public void setUp(){
        Map<String,Object> subject = new HashMap<>();
        subject.put("SUBJECT","SW");
        subject.put("SUBJECT_NAME","生物");
        subject.put("PAPER_ID","70624e01-0980-4149-911f-5fa48725e4d3");
        subject.put("TYPE",0);
        setParmter("expHBSchObjectiveAnswerService",
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
