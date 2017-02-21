package zyj.report.service.export.hubei.school;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.common.CalToolUtil;
import zyj.report.common.ExportUtil;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.RptExpSubjectMapper;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.model.MultiField;
import zyj.report.service.model.Sheet;
import zyj.report.service.model.SingleField;
import zyj.report.service.model.*;

import java.util.*;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 成绩统计表（含各班） 服务
 * @date 2017/1/9
 */
@Service
public class ExpHBSchScoreStatisticsService extends BaseRptService {

    private static String excelName = "成绩统计表";

    @Value("${hubei.subject.score.step}")
    int step;

    @Autowired
    RptExpSubjectMapper rptExpSubjectMapper;

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

        MultiField root = new MultiField(params.get("subjectName")+excelName);

        //step1:加载固定标题
        for (String t : new String[]{"班级,CLS_NAME","实考人数,TAKE_EXAM_NUM","缺考人数,ABSENT_EXAM_STU_NUM","均分,AVG_SCORE","离均差,SCORE_AVG_DEV","标准差,STU_SCORE_SD","最高分,TOP_SCORE","最低分,UP_SCORE"}){
            String[] args = t.split(",");
            root.add(new SingleField(args[0], args[1]));
        }

        //step2:加载动态标题1
            //拿到总分
        List<Map<String, Object>> schSubject =  rptExpSubjectMapper.findRptExpSubject(params);
        double max = Double.parseDouble(schSubject.get(0).get("TOP_SCORE").toString());
        int top80line = (int) (max * 0.8);
        int top70line = (int) (max * 0.7);
        int top60line = (int) (max * 0.6);
        int top40line = (int) (max * 0.4);
        params.put("top80", (int)top80line);
        params.put("top70", (int)top70line);
        params.put("top60", (int)top60line);
        params.put("ls40", (int)top40line);
        MultiField top80 = new MultiField(top80line + "以上");
        top80.add(new SingleField("人数","TOP80"));
        top80.add(new SingleField("%","TOP%80"));
        MultiField top70 = new MultiField(top70line + "以上");
        top70.add(new SingleField("人数","TOP70"));
        top70.add(new SingleField("%","TOP%70"));

        MultiField top60 = new MultiField(top60line + "以上");
        top60.add(new SingleField("人数","TOP60"));
        top60.add(new SingleField("%","TOP%60"));
        MultiField less40 = new MultiField("低于" + top40line);
        less40.add(new SingleField("人数","LS40"));
        less40.add(new SingleField("%","LS%40"));
        root.add(top80);
        root.add(top70);
        root.add(top60);
        root.add(less40);

        //step3:加载动态标题2
        MultiField fsd = new MultiField("分数分段（下确界）  人数分布（累计表）");
        List scoreList = new ArrayList<Integer>();
        double lower = max-(((int)max)%step==0?step:((int)max)%step);
        while((int)lower>=0){
            scoreList.add((int)lower);
            fsd.add(new SingleField(">=" + (int)lower, "HE" + (int)lower));
            lower=lower-step;
        }
        params.put("scoreList", scoreList);
        root.add(fsd);

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

        //查班级的各个分数段人数
        List<Map<String,Object>> clsFSD = rptExpSubjectMapper.qryScorePersonNumByClassSubject(params);
        //查学校的各个分数段人数
        List<Map<String,Object>> schFSD = rptExpSubjectMapper.qryScorePersonNumBySchoolSubject(params);
        //查班级的科目的综合指标
        List<Map<String,Object>> clsZH = rptExpSubjectMapper.qryClassSubjectInfo2(params);
        //查学校的科目的综合指标
        List<Map<String,Object>> schZH = rptExpSubjectMapper.qrySchoolSubjectInfo2(params);
        //查班级的科目缺考人数
        params.put("GroupBy","cls_id");
        List<Map<String,Object>> clsStuNum = rptExpSubjectMapper.qrySubjectStuNum(params);
        //查学校的科目缺考人数
        params.put("GroupBy","sch_id");
        List<Map<String,Object>> schStuNum = rptExpSubjectMapper.qrySubjectStuNum(params);

        if (clsZH.isEmpty() || schZH.isEmpty() || schFSD.isEmpty() || clsFSD.isEmpty() ) throw new ReportExportException("没有查到源数据，请核查！");

        Map<String, Map<String, Object>> clsMap = CalToolUtil.trans(clsZH, new String[]{"CLS_ID"});
        Map<String, Map<String, Object>> clsStuNumMap = CalToolUtil.trans(clsStuNum, new String[]{"CLS_ID"});

        double avg = Double.parseDouble(schZH.get(0).get("AVG_SCORE").toString());
        //组合
        for(Map cls : clsFSD ){
            Map clsZHinfo = clsMap.get(cls.get("CLS_ID"));
            Map clsStuNumInfo = clsStuNumMap.get(cls.get("CLS_ID"));
            int num = Integer.parseInt(clsZHinfo.get("TAKE_EXAM_NUM").toString());
            int ls40 =  Integer.parseInt(cls.get("LS40").toString());
            int he60 =  Integer.parseInt(cls.get("TOP60").toString());
            int he70 =  Integer.parseInt(cls.get("TOP70").toString());
            int he80 =  Integer.parseInt(cls.get("TOP80").toString());
            cls.put("LS%40",  CalToolUtil.decimalFormat2((0.0+ls40)*100/num));
            cls.put("TOP%60",  CalToolUtil.decimalFormat2((0.0 + he60) * 100 /num));
            cls.put("TOP%70",  CalToolUtil.decimalFormat2((0.0 + he70)*100 /num));
            cls.put("TOP%80", CalToolUtil.decimalFormat2((0.0 + he80) * 100 / num));
            cls.putAll(clsZHinfo);
            cls.putAll(clsStuNumInfo);
            //离均差
            double schAvg = Double.parseDouble(cls.get("AVG_SCORE").toString());

            cls.put("SCORE_AVG_DEV", CalToolUtil.decimalFormat2(schAvg-avg));
        }
        Map schRow = schFSD.get(0);
        Map schZHinfo = schZH.get(0);
        Map schStuNumInfo = schStuNum.get(0);
        schRow.putAll(schZHinfo);
        schRow.putAll(schStuNumInfo);
        schRow.put("CLS_NAME", "全校");
        int num = Integer.parseInt(schZHinfo.get("TAKE_EXAM_NUM").toString());
        int ls40 =  Integer.parseInt(schRow.get("LS40").toString());
        int he60 =  Integer.parseInt(schRow.get("TOP60").toString());
        int he70 =  Integer.parseInt(schRow.get("TOP70").toString());
        int he80 =  Integer.parseInt(schRow.get("TOP80").toString());
        schRow.put("LS%40",  CalToolUtil.decimalFormat2((0.0 + ls40) * 100 /num));
        schRow.put("TOP%60",  CalToolUtil.decimalFormat2((0.0+he60)*100/num));
        schRow.put("TOP%70",  CalToolUtil.decimalFormat2((0.0+he70)*100/num));
        schRow.put("TOP%80",  CalToolUtil.decimalFormat2((0.0 + he80) *100/num));
        schRow.put("SCORE_AVG_DEV","--");

        //增加百分比
        Map schPercentageRow = new HashMap(schRow);
        Iterator<Map.Entry> i = schPercentageRow.entrySet().iterator();
        while (i.hasNext()){
            Map.Entry e = i.next();
            String key = e.getKey().toString();
            if (key.startsWith("HE")){
                schPercentageRow.put(key, CalToolUtil.decimalFormat2(Float.parseFloat(e.getValue().toString())*100/num));
            }else{
                schPercentageRow.put(key, "--");
            }
        }
        schPercentageRow.put("CLS_NAME", "全校%");


        Sheet sheet = new Sheet("",excelName);
        sheet.setFields(fields);
        sheet.getData().addAll(clsFSD);
        sheet.getData().add(schRow);
        sheet.getData().add(schPercentageRow);

        sheets.add(sheet);
        return sheets;
    }
}
