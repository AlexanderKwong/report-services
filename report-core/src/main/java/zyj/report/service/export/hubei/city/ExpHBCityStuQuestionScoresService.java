package zyj.report.service.export.hubei.city;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.hubei.school.ExpHBSchStuQuestionScoresService;
import zyj.report.service.model.Excel;
import zyj.report.service.model.Field;
import zyj.report.service.model.Sheet;

import java.util.List;
import java.util.Map;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 学生每题得分 服务
 * @date 2017/1/13
 */
@Service
public class ExpHBCityStuQuestionScoresService extends ExpHBSchStuQuestionScoresService{

    protected String excelName = "成绩明细表";


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
//        ExportUtil.createExcel(excel);
    }

    protected  String[]  getConfirmedTitle(){
        return   new String[]{"考号,SEQUENCE","姓名,NAME","学校,SCH_NAME","文理,TYPE_NAME","区县,AREA_NAME","%s分数,%s_SCORE","%s班名,%s_RANK_CLS","%s校名,%s_RANK_SCH","客观题,OBJECTIVE"};
    }

}
