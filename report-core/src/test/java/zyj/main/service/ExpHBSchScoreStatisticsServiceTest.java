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
public class ExpHBSchScoreStatisticsServiceTest extends BaseExportTest{

    @Before
    public void setUp(){
        Map<String,Object> subject = new HashMap<>();
        subject.put("SUBJECT","YW");
        subject.put("SUBJECT_NAME","语文");
        subject.put("PAPER_ID","dedae08b-b679-4aca-8f04-af9ca5ce1b7d");
        subject.put("TYPE",3);
        setParmter("expHBSchScoreStatisticsService",
                "7148ccdc-9e02-4a5a-99b9-362001b76222",
                "130100",
                subject,
                "school",
                1,
                "651a64b2-c04a-45b9-8fa5-8fd0359a4671");
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
