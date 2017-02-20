package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.util.CollectionsUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.service.model.Field;
import zyj.report.service.model.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 各题平均得分（含各班） 服务
 * @date 2017/1/16
 */
@Service
public class ExpHBSchQuesAvgScoresWithSubQuesOfClassesService extends ExpHBSchQuesAvgScoresOfClassesService{

    @Autowired
    RptExpQuestionMapper rptExpQuestionMapper;


    /**
     * 初始化 sheet
     *
     * @param fields
     */
    protected List<Sheet> getSheet(List<Field> fields, Map<String, Object> params) throws ReportExportException {

        //获取不含小题的数据表
        List<Sheet> sheets = super.getSheet(fields, params);
        List<Map<String, Object>> result = sheets.get(0).getData();

        //加入小题数据
        params.put("GroupBy","CLS_ID");
        List<Map<String, Object>> subQuesOfClasses = rptExpQuestionMapper.qryQuestionSubScore(params);
        Map<String, List<Map<String, Object>>> subQuesOfClassesMap =  CollectionsUtil.partitionBy(subQuesOfClasses, new String[]{"QUESTION_ORDER"});
        List<Map<String, Object>> result1 = new ArrayList<>();
        subQuesOfClassesMap.entrySet().forEach(e->{
            Map<String, Object> row = new HashMap<>();
            row.put("QUESTION_ORDER",e.getKey());
            e.getValue().forEach(c->{
                row.put(c.get("CLS_ID").toString(), c.get("AVG_SCORE").toString());
                row.putIfAbsent("QUESTION_NO", c.get("QUESTION_NO").toString());
            });
            result1.add(row);
        });

        //数据union
        result.addAll(result1);

        //排序
        CollectionsUtil.orderByDoubleValue(result,"QUESTION_ORDER");

        return sheets;
    }
}
