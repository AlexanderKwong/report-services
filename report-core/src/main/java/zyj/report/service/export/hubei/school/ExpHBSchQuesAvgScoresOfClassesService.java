package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.MultiField;
import zyj.report.service.model.Sheet;
import zyj.report.service.model.SingleField;
import zyj.report.service.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 各题平均得分（含各班） 服务
 * @date 2017/1/16
 */
@Service
public class ExpHBSchQuesAvgScoresOfClassesService extends BaseRptService{

    private static String excelName = "各题平均得分";

    @Autowired
    RptExpQuestionMapper rptExpQuestionMapper;

    @Autowired
    BaseDataService baseDataService;

    @Override
    public void exportData(Map<String, Object> params) throws Exception {
        // 初始化 filed
        List<Field> fields = getFields(params);

        // 初始化 sheet
        List<Sheet> sheets = getSheet(fields, params);

        // 初始化 excel
        Excel excel = new Excel(excelName+"（含各班）.xls", params.get("pathFile").toString(), sheets);

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
        for (String t : new String[]{"题目名称,QUESTION_NO"}) {
            String[] args = t.split(",");
            root.add(new SingleField(args[0], args[1]));
        }
        //step2: 加载动态标题1
        List<Map<String, Object>> classesInSchool = baseDataService.getClassesInSchool(params.get("exambatchId").toString(),params.get("schoolId").toString());
        CalToolUtil.sortByIndexValue(classesInSchool,"CLS_NAME");

        for (Map<String,Object> cls : classesInSchool){
            if (Integer.parseInt(cls.get("CLS_TYPE").toString()) == Integer.parseInt(params.get("type").toString()) || Integer.parseInt(params.get("type").toString())==3)
                 root.add(new SingleField(cls.get("CLS_NAME").toString(),cls.get("CLS_ID").toString()));
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
        List<Map<String,Object>> clsQuestions =  rptExpQuestionMapper.findRptExpQuestion(condition);
        if (clsQuestions.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

        List<Map<String,Object>> result =  clsQuestions.stream().map(m-> {
            Map<String,Object> row = new HashMap<>();
            row.put("QUESTION_NO",m.get("QUESTION_NO").toString());
            row.put("QUESTION_ORDER",m.get("QUESTION_ORDER").toString());
            row.put(m.get("CLS_ID").toString(), m.get("AVG_SCORE").toString());
            return row;
        }).collect(Collectors.groupingBy(r -> r.get("QUESTION_ORDER")))./*forEach((k,v) -> System.out.println(String.format("key:%s, value:%s",k,v)));*/

        values().stream().map(list->{
            Map<String,Object> row = new HashMap<>();
            list.forEach(m->{
                row.putAll(m);
            });
            return row;
        }).collect(Collectors.toList());

        CalToolUtil.sortByIndexValue(result, "QUESTION_ORDER");

        Sheet sheet = new Sheet("",excelName);
        sheet.setFields(fields);
        sheet.getData().addAll(result);

        sheets.add(sheet);
        return sheets;
    }
}
