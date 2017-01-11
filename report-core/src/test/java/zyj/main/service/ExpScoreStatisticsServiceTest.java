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
public class ExpScoreStatisticsServiceTest extends BaseExportTest{

    @Before
    public void setUp(){
        Map<String,Object> subject = new HashMap<>();
        subject.put("SUBJECT","SW");
        subject.put("SUBJECT_NAME","生物");
        subject.put("PAPER_ID","045cdafc-e85b-4496-a8fe-faa4363f5314");
        subject.put("TYPE",2);
        setParmter("expScoreStatisticsService",
                "7943ec7c-956c-4cd1-8ada-65e35097aa95",
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
