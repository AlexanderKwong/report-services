package zyj.report.service.export.hubei;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.MultiField;
import zyj.report.service.model.Sheet;
import zyj.report.service.model.SingleField;
import zyj.report.service.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author 邝晓林
 * @Description
 * @date 2017/1/11
 */
@Service
public class ExpClassScoreBasicIndexService extends BaseRptService{

    private static String excelName = "各班均分、优秀数、及格数、标准差";

    @Autowired
    RptExpSubjectMapper rptExpSubjectMapper;

    @Override
    public void exportData(Map<String, Object> params) throws Exception {
        // 初始化 filed
        List<Field> fields = getFields(params);

        // 初始化 sheet
        List<Sheet> sheets = getSheet(fields, params);

        // 初始化 excel
        Excel excel = new Excel(excelName+".xls", params.get("pathFile").toString(), sheets);

        // 导出 excel 文件
        ExportUtil.createExcel(excel);
    }

    /**
     * 初始化 Fields
     */
    private List<Field> getFields(Map<String, Object> params) {

        List<Field> fields = new ArrayList<>();

        MultiField root = new MultiField(excelName);

        //step1:加载固定标题
        for (String t : new String[]{"班级,CLS_NAME","应考人数,CANDIDATES_NUM","平均分,AVG_SCORE","全距,DISTANCE","最高分,TOP_SCORE","最低分,UP_SCORE","优秀人数,LEVEL_GD_NUM","优秀率,LEVEL_GD_RATE","良好人数,LEVEL_FN_NUM","良好率,LEVEL_FN_RATE","及格人数,LEVEL_PS_NUM","及格率,LEVEL_PS_RATE","低分人数,LEVEL_FL_NUM","低分率,LEVEL_FL_RATE","标准差,STU_SCORE_SD","众数,MODELS"}){
            String[] args = t.split(",");
            root.add(new SingleField(args[0], args[1]));
        }

        fields.add(root);

        return fields;
    }
    /**
     * 初始化 sheet
     *
     * @param fields
     */
    private List<Sheet> getSheet(List<Field> fields, Map<String, Object> params) throws ReportExportException {

        List<Sheet> sheets = new ArrayList<>();

        Map condition = new HashMap(params);
        condition.put("level","classes");
        List<Map<String, Object>> data = rptExpSubjectMapper.findRptExpSubject(condition);

        if (data.isEmpty() ) throw new ReportExportException("没有查到源数据，请核查！");

        Consumer<Map> distance = m -> m.put("DISTANCE",Integer.valueOf(m.get("TOP_SCORE").toString()) - Integer.valueOf(m.get("UP_SCORE").toString()));

        Consumer<Map> gd_rate = m -> m.put("LEVEL_GD_RATE", CalToolUtil.decimalFormat2(Double.valueOf(m.get("LEVEL_GD_NUM").toString()) *100 / Integer.valueOf(m.get("TAKE_EXAM_NUM").toString()))+"%");

        Consumer<Map> fn_rate = m -> m.put("LEVEL_FN_RATE", CalToolUtil.decimalFormat2(Double.valueOf(m.get("LEVEL_FN_NUM").toString()) *100 / Integer.valueOf(m.get("TAKE_EXAM_NUM").toString()))+"%");

        Consumer<Map> ps_rate = m -> m.put("LEVEL_PS_RATE", CalToolUtil.decimalFormat2(Double.valueOf(m.get("LEVEL_PS_NUM").toString()) *100 / Integer.valueOf(m.get("TAKE_EXAM_NUM").toString()))+"%");

        Consumer<Map> fl_rate = m -> m.put("LEVEL_FL_RATE", CalToolUtil.decimalFormat2(Double.valueOf(m.get("LEVEL_FL_NUM").toString()) *100 / Integer.valueOf(m.get("TAKE_EXAM_NUM").toString()))+"%");

        Consumer<Map> doAll = m -> {distance.accept(m);gd_rate.accept(m);fn_rate.accept(m);ps_rate.accept(m);fl_rate.accept(m);};

        data.forEach(doAll);

        Sheet sheet = new Sheet("",excelName);
        sheet.setFields(fields);
        sheet.getData().addAll(data);

        sheets.add(sheet);
        return sheets;
    }

}
