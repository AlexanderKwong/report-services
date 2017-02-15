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
 * @Description 导出 湖北版 学生选择题答案 服务
 * @date 2017/1/12
 */
@Service
public class ExpHBSchObjectiveAnswerService extends BaseRptService{

    private static String excelName = "学生选择题答案";

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

        //step1:加载固定标题
        for (String t : new String[]{"考号,SEQUENCE","姓名,NAME","班级,CLSNAME"}){
            String[] args = t.split(",");
            root.add(new SingleField(args[0], args[1]));
        }

        List<Map> questions = rptExpQuestionMapper.qryClassQuestionScore6(params);

        List<Integer> orderList = new ArrayList<Integer>();

        questions.stream().filter(m -> Integer.parseInt(m.get("QST_TIPY").toString()) != 4).forEach(m -> {
            int order = Integer.parseInt(m.get("QUESTION_ORDER").toString());
            orderList.add(order);
            root.add(new SingleField((String)m.get("QUESTION_NO"), "Q"+ order));
        });

        params.put("orderList2",orderList);
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

        List<Map<String, Object>> result = rptExpQuestionMapper.qryStudentQuestionScore(params);
        if (result.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

        Sheet sheet = new Sheet("","全级");
        sheet.setFields(fields);
        sheet.getData().addAll(result);

        sheets.add(sheet);
        return sheets;
    }
}
