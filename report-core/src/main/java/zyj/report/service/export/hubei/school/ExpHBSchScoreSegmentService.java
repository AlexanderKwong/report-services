package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSegmentType;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.segment.Segment;
import zyj.report.service.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 各分数段人数 服务
 * @date 2017/1/17
 */
@Service
public class ExpHBSchScoreSegmentService extends BaseRptService {

    private static String excelName = "各分数段人数";

    @Autowired
    BaseDataService baseDataService;

    @Autowired
    RptExpSubjectMapper rptExpSubjectMapper;

    @Value("${hubei.subject.score.step}")
    Integer step;

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

        MultiField root = new MultiField(params.get("subjectName") + excelName);
        //step1:加载固定标题
        for (String t : new String[]{"分数段,SCORE_SEG","频数,FREQUENCY","频率,FREQUENCY_CENT","累计频数,ACC_FREQUENCY","累计频率,ACC_FREQUENCY_CENT" }) {
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

        //拿到总分
        Map condition = new HashMap(params);
        condition.put("level","city");
        List<Map<String, Object>> citySubject =  rptExpSubjectMapper.findRptExpSubject(condition);
        if (citySubject.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

        Float full = Float.parseFloat(citySubject.get(0).get("FULL_SCORE").toString());

        String key = params.get("subjectName").toString()+ "_SCORE";
        List<Map<String,Object>> result1 = baseDataService.getStudentSubjectsAndAllscore(params.get("exambatchId").toString(),params.get("schoolId").toString(),params.get("level").toString(),(Integer)params.get("stuType")).
                stream().filter(m->m.get(key) != null).collect(Collectors.toList());
        if (result1.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！");

        Segment segment = new Segment(step,0,full,result1.size(), EnmSegmentType.ROUNDED);
        List<Map<String, Object>> result = segment.getStepSegment(result1,key);

        Sheet sheet = new Sheet("",excelName);
        sheet.setFields(fields);
        sheet.getData().addAll(result);

        sheets.add(sheet);
        return sheets;
    }
}
