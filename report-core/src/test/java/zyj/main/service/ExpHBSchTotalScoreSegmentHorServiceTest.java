package zyj.main.service;

import org.junit.Before;
import org.junit.Test;
import zyj.main.BaseExportTest;
import zyj.report.common.ExportUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CXinZhi 测试总分排名
 * @Description
 * @date 2017/2/15
 */
public class ExpHBSchTotalScoreSegmentHorServiceTest extends BaseExportTest{

    @Before
    public void setUp(){
        Map<String,Object> subject = new HashMap<>();
        subject.put("SUBJECT","SW");
        subject.put("SUBJECT_NAME","生物");
        subject.put("PAPER_ID","70624e01-0980-4149-911f-5fa48725e4d3");
        subject.put("TYPE",0);
        setParmter("expHBSchTotalScoreSegmentHorService",
                "7148ccdc-9e02-4a5a-99b9-362001b76222",
                "350800",
                null,
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
