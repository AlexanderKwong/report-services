package zyj.main.service;

import org.junit.Before;
import org.junit.Test;
import zyj.main.BaseExportTest;
import zyj.report.common.ExportUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/10/26
 */
public class ExpShangXianShuServiceTest  extends BaseExportTest {

    @Before
    public void setUp(){
        Map<String,Object> subject = new HashMap<>();
        subject.put("SUBJECT","WK");
        subject.put("SUBJECT_NAME","文科总分");
        subject.put("PAPER_ID","");
        subject.put("TYPE",1);
        setParmter("expShangXianShuService",
                "bb95bc41-b7ff-40be-884e-940d1bdc0568",
                "420900",
                subject,
                "city",
                1,
                null);

        zyj.report.common.CalToolUtil.setSubjectScoreLine(new String[][]{
                { "LYW_S", "105.5", "89", "64" },
                { "LSX", "126", "79", "20" },
                { "LYY_S", "111.5", "69.5", "25.5" },
                { "S_ZH_WL", "75", "42", "13" },
                { "S_ZH_HX", "82", "52", "13" },
                { "S_ZH_SW", "70", "47", "17" },
                { "WYW_S", "106.5", "92.5", "66" },
                { "WSX", "110", "70", "20" },
                { "WYY_S", "113", "76", "27.5" },
                { "S_ZH_DL", "68", "47", "17" },
                { "S_ZH_ZS", "64.5", "50", "25" },
                { "S_ZH_LS", "73", "58", "27" },
               // { "WZ", "109", "104", "95.0" },
               // { "LZ", "109", "104", "95.0" },
                { "LK", "494.5", "358", "180" },
                { "WK", "465.5", "369.5", "213.5" }

        });
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
