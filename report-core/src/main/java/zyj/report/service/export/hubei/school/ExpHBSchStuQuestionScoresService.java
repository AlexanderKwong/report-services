package zyj.report.service.export.hubei.school;

import org.apache.commons.lang.ObjectUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 学生每题得分 服务
 * @date 2017/1/13
 */
@Service
public class ExpHBSchStuQuestionScoresService extends BaseRptService{

    protected String excelName = "学生每题得分";


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
        Excel excel = new Excel(this.excelName+".xls", params.get("pathFile").toString(), sheets);

        // 导出 excel 文件
        ExportUtil.createExcel(excel);
    }

    /**
     * 初始化 Fields
     */
    protected List<Field> getFields(Map<String, Object> params) {

        List<Field> fields = new ArrayList<>();

        String subject_name = params.get("subjectName").toString();

        MultiField root = new MultiField(subject_name + excelName);

        //step1:加载固定标题
        for (String t : getConfirmedTitle()){
            String[] args = t.split(",");
            root.add(new SingleField(String.format(args[0] ,subject_name), String.format(args[1] ,subject_name)));
        }
        //step2: 加载动态标题1
        List<Map> questions = rptExpQuestionMapper.qryClassQuestionScore6(params);
        /*******/
        //查询小题
        List<Map> subQuestions = rptExpQuestionMapper.qryQuestionSub(params);//按小题号排序
        Map<String,List<Map<String,Object>>> subQuestionMap = new HashMap<String,List<Map<String,Object>>>();

        for(Map<String,Object> obj : subQuestions){
            String k = "";
            for(String t : new String[]{"PAPER_ID","SUBJECT","PARENT_QUESTION_ORDER"}){
                k = k + ObjectUtils.toString(obj.get(t));
            }
            List<Map<String, Object>> sameParentOrder =   subQuestionMap.get(k);
            if (sameParentOrder == null) {
                sameParentOrder = new ArrayList<Map<String, Object>>();
                subQuestionMap.put(k, sameParentOrder);
            }
            sameParentOrder.add(obj);
        }
        /*******/

        String paperId = ObjectUtils.toString(params.get("paperId"));
        String shortName = ObjectUtils.toString(params.get("subject"));
        questions.stream().filter(m -> Integer.parseInt(m.get("QST_TIPY").toString()) == 4).forEach(m -> {
            int order = Integer.parseInt(m.get("QUESTION_ORDER").toString());
            root.add(new SingleField((String)m.get("QUESTION_NO"), "Q"+ order));
            //补充小题
            List<Map<String, Object>> subQuestionsOfThisParent  = subQuestionMap.get(paperId + shortName + order);
            if (subQuestionsOfThisParent != null && !subQuestionsOfThisParent.isEmpty()){
                for (Map<String, Object> sq : subQuestionsOfThisParent){
                    String sub_no = ObjectUtils.toString(sq.get("QUESTION_NO"));
                    String sub_order = sq.get("QUESTION_ORDER").toString().replace(".", "_");
                    root.add(new SingleField(sub_no, "Q"+sub_order));
                }
            }
        });

        fields.add(root);

        return fields;
    }


    /**
     * 初始化 sheet
     *
     * @param fields
     */
    protected List<Sheet> getSheet(List<Field> fields, Map<String, Object> params) throws ReportExportException {

        List<Sheet> sheets = new ArrayList<>();

//        List<Map<String, Object>> result1 = rptExpStudentSubjectMapper.findRptExpStudentSubject(params);
        String level = params.get("level").toString();
        String exambatchId = params.get("exambatchId").toString();
        Integer stuType = (Integer)params.get("stuType");

        String key = params.get("subjectName").toString()+ "_SCORE";
        List<Map<String, Object>> result1 = baseDataService.getStudentSubjectsAndAllscore(exambatchId, (String) params.get(level + "Id"), level, stuType)
            .stream().filter(m->m.get(key) != null).collect(Collectors.toList());

        List<Map<String, Object>> result2 = baseDataService.getStudentQuestion(exambatchId,(String) params.get(level + "Id"),level,stuType,params.get("paperId").toString(),params.get("subject").toString());

        if (result1.isEmpty() || result2.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

        List<Map<String, Object>> result = CollectionsUtil.leftjoinMapByKey(result1,result2,"USER_ID");

        result.forEach(m -> {
            int type = Integer.parseInt(m.get("TYPE").toString());
            if (type == 1) m.put("TYPE_NAME","文科");
            else if (type == 2) m.put("TYPE_NAME","理科");
        });
        zyj.report.common.CalToolUtil.sortByIndexValue(result, String.format("%s_RANK_SCH", params.get("subjectName")));

        Sheet sheet = new Sheet("",excelName);
        sheet.setFields(fields);
        sheet.getData().addAll(result);

        sheets.add(sheet);
        return sheets;
    }

    protected  String[]  getConfirmedTitle(){
        return   new String[]{"考号,SEQUENCE","姓名,NAME","文理科,TYPE_NAME","班级,CLS_NAME","%s分数,%s_SCORE","%s班名,%s_RANK_CLS","%s校名,%s_RANK_SCH","客观题,OBJECTIVE"};
    }

}
