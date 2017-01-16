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
public class ExpShangXianShu2ServiceTest extends BaseExportTest {

    @Before
    public void setUp(){
        Map<String,Object> subject = new HashMap<>();
        subject.put("SUBJECT","LK");
        subject.put("SUBJECT_NAME","理科总分");
        subject.put("PAPER_ID","");
        setParmter("expShangXianShu2Service",
                "f4ad3e83-3777-4c70-a96a-d9f5a4a038f1",
                "130600",
                subject,
                "city",
                1,
                null);

        zyj.report.common.CalToolUtil.setSubjectRankLine(new String[][]{
                { "LYW_S", "8280", "21750" },
                { "LSX", "8280", "21750"},
                { "LYY_S", "8280", "21750"},
                { "S_ZH_WL", "8280", "21750" },
                { "S_ZH_HX", "8280", "21750"},
                { "S_ZH_SW", "8280", "21750"},
                { "WYW_S", "1720", "8250"},
                { "WSX", "1720", "8250"},
                { "WYY_S", "1720", "8250" },
                { "S_ZH_DL", "1720", "8250" },
                { "S_ZH_ZS", "1720", "8250" },
                { "S_ZH_LS", "1720", "8250" },
                { "WZ", "1720", "8250"},
                { "LZ", "8280", "21750"},
                { "LK", "8280", "21750" },
                { "WK", "1720", "8250" }

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
