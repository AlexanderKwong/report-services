package zyj.report.service.export.hubei.school;

import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.service.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 学生每题得分（含各班） 服务
 * @date 2017/1/13
 */
@Service
public class ExpHBSchStuQuesScoresOfClassesService extends ExpHBSchStuQuestionScoresService {

    private static String excelName = "学生每题得分%s";

    @Override
    public void exportData(Map<String, Object> params) throws Exception {
        // 初始化 filed
        List<Field> fields = getFields(params);

        // 初始化 sheet
        List<Sheet> sheets = getSheet(fields, params);

        // 初始化 excel
        Excel excel = new Excel(String.format(excelName,"（含各班）") +".xls", params.get("pathFile").toString(), sheets);

        // 导出 excel 文件
        ExportUtil.createExcel(excel);
    }

    /**
     * 初始化 sheet
     *
     * @param fields
     */
    @Override
    protected List<Sheet> getSheet(List<Field> fields, Map<String, Object> params) throws ReportExportException {

        List<Sheet> sheets = new ArrayList<>();
        //得到待格式化数据
        List<Sheet> sheetsToFormat = super.getSheet(fields, params);
        List<Map<String, Object>> dataToFormat = sheetsToFormat.get(0).getData();
        MultiField rootToFormat = (MultiField) fields.get(0);
        //获取所有班级
        List<Map<String, Object>> classes = baseDataService.getClassesInSchool(params.get("exambatchId").toString(),params.get("schoolId").toString())
                .stream().filter(c->Integer.parseInt(c.get("CLS_TYPE").toString()) == Integer.parseInt(params.get("type").toString())).collect(Collectors.toList());

        CollectionsUtil.orderByStringValue(classes, "CLS_NAME");

        classes.forEach(c -> {

            String clsName = c.get("CLS_NAME").toString();
            String clsId = c.get("CLS_ID").toString();

            Sheet sheetFormatted = new Sheet(clsId, clsName);

            //设置标题
            MultiField root = rootToFormat.copy(String.format(excelName, "（"+clsName+"）"));

            List<Field> fieldsFormatted = new ArrayList<Field>();
            fieldsFormatted.add(root);

            sheetFormatted.setFields(fieldsFormatted);

            sheets.add(sheetFormatted);

        });

        //设置内容
        Map<String, List<Map<String, Object>>> dataFormattedMapping =  CollectionsUtil.partitionBy( dataToFormat, new String[]{"CLS_ID"});

        sheets.forEach(sheet -> {
            List<Map<String, Object>> dataFormatted = dataFormattedMapping.get(sheet.getId());
            CollectionsUtil.orderByStringValue(dataFormatted, "SEQUENCE");
            sheet.getData().addAll(dataFormatted);
        });

        return sheets;
    }

}
