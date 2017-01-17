package zyj.report.service.export.hubei;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.persistence.client.RptExpStudentSubjectMapper;
import zyj.report.service.BaseDataService;
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
 * @Description
 * @date 2017/1/13
 */
@Service
public class ExpStuQuestionScoresService extends BaseRptService{

    private static String excelName = "学生每题得分";

    @Autowired
    RptExpQuestionMapper rptExpQuestionMapper;
    @Autowired
    BaseDataService baseDataService;
    @Autowired
    RptExpStudentSubjectMapper rptExpStudentSubjectMapper;

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
        for (String t : new String[]{"考号,SEQUENCE","姓名,NAME","文理科,TYPE_NAME","班级,CLSNAME",subject_name+"分数,SIGN_TOTAL",subject_name+"班名,CLS_RANK",subject_name+"校名,GRD_RANK"}){
            String[] args = t.split(",");
            root.add(new SingleField(args[0], args[1]));
        }
        //step2: 加载动态标题1
        List<Map> questions = rptExpQuestionMapper.qryClassQuestionScore6(params);

        List<Integer> orderList = new ArrayList<Integer>();

        questions.stream().filter(m -> Integer.parseInt(m.get("QST_TIPY").toString()) == 4).forEach(m -> {
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

        List<Map<String, Object>> result1 = rptExpStudentSubjectMapper.findRptExpStudentSubject(params);

        List<Map<String, Object>> result2 = baseDataService.getStudentQuestion(params.get("exambatchId").toString(),params.get("schoolId").toString(),"school",(Integer)params.get("stuType"),params.get("paperId").toString(),params.get("subject").toString());

        List<Map<String, Object>> result = CollectionsUtil.leftjoinMapByKey(result1,result2,"USER_ID");

        result.forEach(m -> {
            int type = Integer.parseInt(m.get("TYPE").toString());
            if (type == 1) m.put("TYPE_NAME","文科");
            else if (type == 2) m.put("TYPE_NAME","理科");
        });
        zyj.report.common.CalToolUtil.sortByIndexValue(result, "SEQUENCE");

        Sheet sheet = new Sheet("",excelName);
        sheet.setFields(fields);
        sheet.getData().addAll(result);

        sheets.add(sheet);
        return sheets;
    }

}
