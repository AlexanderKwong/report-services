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
public class ExpHBSchScoreSegmentOfClassesServiceTest extends BaseExportTest{

    @Before
    public void setUp(){
        Map<String,Object> subject = new HashMap<>();
        subject.put("SUBJECT","YW");
        subject.put("SUBJECT_NAME","语文");
        subject.put("PAPER_ID","9ad63e8e-14ec-4dc9-8db5-86e572429208");
        subject.put("TYPE",0);
        setParmter("expHBSchScoreSegmentOfClassesService",
                "386840d9-bf0c-4086-a762-5ca129521950",
                "350800",
                subject,
                "school",
                1,
                "45979051-eb43-4a14-a8b5-cb027727587d");
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
