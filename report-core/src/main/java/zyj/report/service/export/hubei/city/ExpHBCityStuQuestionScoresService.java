package zyj.report.service.export.hubei.city;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.persistence.client.RptExpQuestionMapper;
import zyj.report.service.BaseDataService;
import zyj.report.service.export.hubei.school.ExpHBSchStuQuestionScoresService;

/**
 * @author 邝晓林
 * @Description 导出 湖北版 学生每题得分 服务
 * @date 2017/1/13
 */
@Service
public class ExpHBCityStuQuestionScoresService extends ExpHBSchStuQuestionScoresService{

    private String excelName = "成绩明细表";


    @Autowired
    RptExpQuestionMapper rptExpQuestionMapper;
    @Autowired
    BaseDataService baseDataService;


    protected  String[]  getConfirmedTitle(){
        return   new String[]{"考号,SEQUENCE","姓名,NAME","学校,SCH_NAME","文理,TYPE_NAME","区县,AREA_NAME","%s分数,%s_SCORE","%s班名,%s_RANK_CLS","%s校名,%s_RANK_SCH","客观题,OBJECTIVE"};
    }

}
