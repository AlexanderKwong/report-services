package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.MultiField;
import zyj.report.service.model.Sheet;
import zyj.report.service.model.SingleField;
import zyj.report.service.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 试卷难易程度 服务
 * @date 2017/1/16
 */
@Service
public class ExpHBSchPaperDifficultyService extends BaseRptService {

    private static String excelName = "试卷难易程度";

    @Autowired
    RptExpQuestionMapper rptExpQuestionMapper;

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

        String subject_name = params.get("subjectName").toString();
        //step1:加载固定标题
        for (String t : new String[]{"难度（P）区分度（R),PR", "题号,NO", "题量,TILIANG", "比例,BILI"}) {
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

        List<Map<String,Object>> questionSuitable = rptExpQuestionMapper.qryQuestionSuitable(params);
        if (questionSuitable.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

        for(Map situation : questionSuitable){
            int nandu = Integer.parseInt(situation.get("P").toString());
            int qufendu = Integer.parseInt(situation.get("R").toString());
            if(nandu == 0 && qufendu == 1)
                situation.put("PR", "难度适合  区分度合适");
            else if (nandu == 0 && qufendu == 0)
                situation.put("PR", "难度适合  区分度不合适");
            else if (nandu == 1 && qufendu == 1)
                situation.put("PR", "难度偏难  区分度合适");
            else if (nandu == -1 && qufendu == 1)
                situation.put("PR", "难度偏易  区分度合适");
            else if (nandu == 1 && qufendu == 0)
                situation.put("PR", "难度偏难  区分度不合适");
            else if (nandu == -1 && qufendu == 0)
                situation.put("PR", "难度偏易  区分度不合适");
        }
        Sheet sheet = new Sheet("",excelName);
        sheet.setFields(fields);
        sheet.getData().addAll(questionSuitable);

        sheets.add(sheet);
        return sheets;
    }

}
