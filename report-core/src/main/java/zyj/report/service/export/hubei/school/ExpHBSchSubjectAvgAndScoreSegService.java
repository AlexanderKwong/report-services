package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.common.constant.EnmSegmentType;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.*;
import zyj.report.service.model.segment.Segment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 均分及各分数段人数 服务
 * @date 2017/1/17
 */
@Service
public class ExpHBSchSubjectAvgAndScoreSegService extends BaseRptService {

    private static String excelName = "均分及各分数段人数";

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
    private List<Field> getFields(Map<String, Object> params) throws ReportExportException {

        List<Field> fields = new ArrayList<>();

        MultiField root = new MultiField(excelName);
        //step1:加载固定标题
        for (String t : new String[]{"班级,CLS_NAME","应考人数,CANDIDATES_NUM","均分,AVG_SCORE","排名,AVG_SCH_ORDER","优秀人数,LEVEL_GD_NUM","排名,LEVEL_GD_RANK","及格人数,LEVEL_PS_NUM","排名,LEVEL_FN_RANK" }) {
            String[] args = t.split(",");
            root.add(new SingleField(args[0], args[1]));
        }
        //step2: 加载动态标题1
         //查最高分
        List<Map<String, Object>> schSubjectInfo =  rptExpSubjectMapper.findRptExpSubject(params);
        if (schSubjectInfo == null || schSubjectInfo.isEmpty()) throw new ReportExportException("没有查到源数据，请核查！") ;
        Float max =  Float.parseFloat(schSubjectInfo.get(0).get("TOP_SCORE").toString());
        int takeExamNum =  Integer.parseInt(schSubjectInfo.get(0).get("TAKE_EXAM_NUM").toString());
        //生成分数段
        Segment segment = new Segment(step, 0,max,takeExamNum, EnmSegmentType.ROUNDED);
        List<String> segmentNames =  segment.generateSegment();

        segmentNames.forEach(s->{
            root.add(new SingleField(">=" + s.substring(1,s.length()-1).split(",")[0], s));
        });
        params.put("segment",segment);

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

        //数据集1

        Map condition = new HashMap(params);
        condition.put("level","classes");
        List<Map<String, Object>> clsSubjectInfo =  rptExpSubjectMapper.findRptExpSubject(condition);
        CollectionsUtil.rank(clsSubjectInfo, "LEVEL_GD_NUM", "LEVEL_GD_RANK");
        CollectionsUtil.rank(clsSubjectInfo, "LEVEL_FN_NUM", "LEVEL_FN_RANK");


        //数据集2
        Segment segment = (Segment)params.get("segment");
        String key = params.get("subjectName").toString()+ "_SCORE";
        List<Map<String,Object>> result2 = baseDataService.getStudentSubjectsAndAllscore(params.get("exambatchId").toString(),params.get("schoolId").toString(),params.get("level").toString(),(Integer)params.get("stuType")).
                stream().filter(m->m.get(key) != null).collect(Collectors.toList());

        List<Map<String, Object>> result3 = segment.getPartitionStepSegmentAccTransverse(result2,key,new String[]{"CLS_ID"});

        //关联
        List<Map<String, Object>> result = CollectionsUtil.leftjoinMapByKey(clsSubjectInfo, result3, "CLS_ID");
        CollectionsUtil.orderByStringValue(result, "CLS_NAME");

        Sheet sheet = new Sheet("","全级");
        sheet.setFields(fields);
        sheet.getData().addAll(result);

        sheets.add(sheet);
        return sheets;
    }
}
