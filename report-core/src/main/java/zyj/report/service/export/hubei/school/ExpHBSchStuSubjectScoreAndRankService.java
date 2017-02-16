package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 学生单科成绩与排名 服务
 * @date 2017/1/12
 */
@Service
public class ExpHBSchStuSubjectScoreAndRankService extends BaseRptService{

    private static String excelName = "学生单科成绩与排名";

    @Autowired
    BaseDataService baseDataService;

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

        String subjectName = params.get("subjectName").toString();

        MultiField root = new MultiField( excelName);

        //step1:加载固定标题
        for (String t : new String[]{"考号,SEQUENCE","姓名,NAME","班级,CLS_NAME","文理,TYPE_NAME","%s分数,%s_SCORE","标准分,STANDARD_SCORE","%s班名,%s_RANK_CLS","%s校名,%s_RANK_SCH"}){
            String[] args = t.split(",");
            root.add(new SingleField(String.format(args[0],subjectName) , String.format(args[1],subjectName)));
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

        String key = params.get("subjectName").toString()+ "_SCORE";
        //查学校维度的数据
        List<Map<String, Object>> schSubject = rptExpSubjectMapper.findRptExpSubject(params);
        if (schSubject == null || schSubject.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

        //获取平均分和标准差，用于计算标准分
        Float avgScore = Float.parseFloat(schSubject.get(0).get("AVG_SCORE").toString());
        Float stdDev = Float.parseFloat(schSubject.get(0).get("STU_SCORE_SD").toString());

        //定义标准分算子
        Function<Map<String,Object>,Map<String,Object>> calcStdDev = m -> {
            Float stuScore = Float.parseFloat(m.get(key).toString());
            String stdScore = CalToolUtil.decimalFormat2((stuScore - avgScore) * 100 / stdDev + 500);
            m.put("STANDARD_SCORE",stdScore);
            return m;
        };
        Function<Map<String,Object>,Map<String,Object>> mapTypeName = m -> {
            int type = Integer.parseInt(m.get("TYPE").toString());
            if (type == 1) m.put("TYPE_NAME","文科");
            else if (type == 2) m.put("TYPE_NAME","理科");
            return m;
        };

        //获取学生数据，并计算标准分
        List<Map<String, Object>> result = baseDataService.getStudentSubjectsAndAllscore(params.get("exambatchId").toString(),params.get("schoolId").toString(),"school",(Integer )params.get("stuType"))
                .stream().filter(m->m.get(key) != null).map(calcStdDev).map(mapTypeName).collect(Collectors.toList());

        CollectionsUtil.orderByStringValue(result, "SEQUENCE");

        Sheet sheet = new Sheet("",excelName);
        sheet.setFields(fields);
        sheet.getData().addAll(result);

        sheets.add(sheet);
        return sheets;
    }
}
