package zyj.report.business.task.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zyj.report.business.task.RptParameter;
import zyj.report.business.task.RptTaskFactory;
import zyj.report.business.task.RptTaskQueue;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.service.BaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(value = "rptTaskFactory")
public class RptTaskFactoryImpl implements RptTaskFactory {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	BaseDataService baseDataService;

	@Override
	public RptTaskQueue create(RptParameter parameter) {

		if (parameter.getRptType() == RptParameter.TONGYONG)
			return new RptTaskQueueBase();
		else if (parameter.getRptType() == RptParameter.XIAOGAN)
			return new RptTaskQueueXG();
		else if (parameter.getRptType() == RptParameter.ZHONGSHAN)
			return new RptTaskQueueZS();
		else if (parameter.getRptType() == RptParameter.HUBEI)
			return new RptTaskQueueHB();
		return null;
	}

	/**
	 * 通用版
	 *
	 * @author admin
	 */
	@SuppressWarnings({"unchecked", "serial"})
	private class RptTaskQueueBase<RptTask> extends RptTaskQueue {

		@SuppressWarnings("rawtypes")
		@Override
		public boolean addCityTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			String rootPath = r.getPathFile();
			String cityPath = rootPath + "/" + "全市报表";
			for (Map<String, Object> subjectInfo : r.getSubjectList()) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = cityPath + "/总分";
//					FileUtil.mkexpdir(zfPath);
//				学科分析	extFetchXkfxServer
					this.add(new RptTaskBase(r, "extFetchXkfxServer", "city", cityPath));
//				各科综合	expGeKeZongHeService
					this.add(new RptTaskBase(r, "expGeKeZongHeService", "city", cityPath));
//				学生成绩  expXueShengChengJiService
					this.add(new RptTaskBase(r, "expXueShengChengJiService", "city", cityPath));
//				总分-分数段	extZffsdServer
					this.add(new RptTaskBase(r, "extZffsdServer", "city", zfPath));
					if (r.isWL()) {//分文理e

//					总分-分数段对比(横)_文科	extZffsddbhServer
						this.add(new RptTaskBase(r, "extZffsddbhServer", "city", zfPath, totalscoreMap_LK));
						this.add(new RptTaskBase(r, "extZffsddbhServer", "city", zfPath, totalscoreMap_WK));
//					平均分对比-理科	expPingJunFenDuiBiService
						this.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "city", cityPath, totalscoreMap_LK));
						this.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "city", cityPath, totalscoreMap_WK));
//					总分-各名次段分布-理科	expMingCiDuanFenBuService
						this.add(new RptTaskBase(r, "expMingCiDuanFenBuService", "city", zfPath, totalscoreMap_LK));
						this.add(new RptTaskBase(r, "expMingCiDuanFenBuService", "city", zfPath, totalscoreMap_WK));
//					总分-分数段对比(竖)1_理科	extZffsddbvServer
						this.add(new RptTaskBase(r, "extZffsddbvServer", "city1", zfPath, totalscoreMap_LK));
						this.add(new RptTaskBase(r, "extZffsddbvServer", "city1", zfPath, totalscoreMap_WK));
//					总分-分数段对比(竖)2_理科	extZffsddbvServer
						this.add(new RptTaskBase(r, "extZffsddbvServer", "city2", zfPath, totalscoreMap_LK));
						this.add(new RptTaskBase(r, "extZffsddbvServer", "city2", zfPath, totalscoreMap_WK));

					} else {

//					总分-分数段对比(横)	extZffsddbhServer
						this.add(new RptTaskBase(r, "extZffsddbhServer", "city", zfPath, totalscoreMap_NWL));
//					平均分对比-不分文理科	expPingJunFenDuiBiService
						this.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "city", cityPath, totalscoreMap_NWL));
//					总分-各名次段分布-不分文理科	expMingCiDuanFenBuService
						this.add(new RptTaskBase(r, "expMingCiDuanFenBuService", "city", zfPath, totalscoreMap_NWL));
//					总分-分数段对比(竖)1_不分文理科	extZffsddbvServer
						this.add(new RptTaskBase(r, "extZffsddbvServer", "city1", zfPath, totalscoreMap_NWL));
//					总分-分数段对比(竖)1_不分文理科	extZffsddbvServer
						this.add(new RptTaskBase(r, "extZffsddbvServer", "city2", zfPath, totalscoreMap_NWL));
					}
//				总分-基本指标	expJiBenZhiBiaoService
					this.add(new RptTaskBase(r, "expJiBenZhiBiaoService", "city", zfPath, totalscoreMap_ZF));
//				总分-综合指标	expZongHeZhiBiaoService
					this.add(new RptTaskBase(r, "expZongHeZhiBiaoService", "city", zfPath, totalscoreMap_ZF));
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {//科目
					Map<String, Object> otherParams = r.getOtherParams();
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("paperId", subjectInfo.get("PAPER_ID"));
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
//					String subject = subjectInfo.get("SUBJECT").toString();
					String cityPaperPath = cityPath + "/" + subjectInfo.get("SUBJECT_NAME");
//					FileUtil.mkexpdir(cityPaperPath);
//					科目-小题分析	extFetchKmxtfxServer
					this.add(new RptTaskBase(r, "extFetchKmxtfxServer", "city", cityPaperPath, subjectInfo));
//					科目-小题平均分对比	extKmxtpjfdbServer
					this.add(new RptTaskBase(r, "extKmxtpjfdbServer", "city", cityPaperPath, subjectInfo));
//					科目-小题掌握情况分析	extXtzwqkfxServer
					this.add(new RptTaskBase(r, "extXtzwqkfxServer", "city", cityPaperPath, subjectInfo));
//					科目-分数段	extFetchKmfsdServer
					this.add(new RptTaskBase(r, "extFetchKmfsdServer", "city", cityPaperPath, subjectInfo));
//					科目-分数段对比（横)	extKmfsddbhServer
					this.add(new RptTaskBase(r, "extKmfsddbhServer", "city", cityPaperPath, subjectInfo));
//					科目-基本指标	expJiBenZhiBiaoService
					this.add(new RptTaskBase(r, "expJiBenZhiBiaoService", "city", cityPaperPath, subjectInfo));
//					科目-名次段分布	expMingCiDuanFenBuService
					this.add(new RptTaskBase(r, "expMingCiDuanFenBuService", "city", cityPaperPath, subjectInfo));
//					科目-小题分	expGeKeXiaoTiFenService
					this.add(new RptTaskBase(r, "expGeKeXiaoTiFenService", "city", cityPaperPath, subjectInfo));
//					科目-综合指标	expZongHeZhiBiaoService
					this.add(new RptTaskBase(r, "expZongHeZhiBiaoService", "city", cityPaperPath, subjectInfo));
//					科目-分数段对比（竖)1	extKmfsddbvServer
					this.add(new RptTaskBase(r, "extKmfsddbvServer", "city1", cityPaperPath, subjectInfo));
//					科目-分数段对比（竖)2	extKmfsddbvServer
					this.add(new RptTaskBase(r, "extKmfsddbvServer", "city2", cityPaperPath, subjectInfo));
				}
			}
			//加入等待
			this.add(new RptTaskWait(r.getExambatchId(), r.getPathFile()));
			return true;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean addAreaTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			String rootPath = r.getPathFile();
			Map<String, Object> otherParams = r.getOtherParams();
			String areaId = otherParams.get("areaId").toString();
			String areaName = otherParams.get("areaName").toString();
			String areaPath = rootPath + "/区县报表/" + areaName;
			for (Map<String, Object> subjectInfo : r.getSubjectList()) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = areaPath + "/总分";
//					FileUtil.mkexpdir(zfPath);
					RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), areaPath);
//				总分-分数段	extZffsdServer
					rpttaskseries.add(new RptTaskBase(r, "extZffsdServer", "area", zfPath, null, areaId));
//				各科综合	expGeKeZongHeService
					rpttaskseries.add(new RptTaskBase(r, "expGeKeZongHeService", "area", areaPath, null, areaId));
//				学生成绩	expXueShengChengJiService
					rpttaskseries.add(new RptTaskBase(r, "expXueShengChengJiService", "area", areaPath, null, areaId));
					if (r.isWL()) {
						//平均分对比-理科	expPingJunFenDuiBiService
						rpttaskseries.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "area", areaPath, totalscoreMap_LK, areaId));
						rpttaskseries.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "area", areaPath, totalscoreMap_WK, areaId));
//					总分-分数段对比(竖)_理科	extZffsddbvServer
						rpttaskseries.add(new RptTaskBase(r, "extZffsddbvServer", "area", zfPath, totalscoreMap_LK, areaId));
						rpttaskseries.add(new RptTaskBase(r, "extZffsddbvServer", "area", zfPath, totalscoreMap_WK, areaId));
					} else {
						rpttaskseries.add(new RptTaskBase(r, "extZffsddbvServer", "area", zfPath, totalscoreMap_NWL, areaId));
						//平均分对比-理科	expPingJunFenDuiBiService
						rpttaskseries.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "area", areaPath, totalscoreMap_NWL, areaId));
					}
					this.add(rpttaskseries);
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {//科目
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("areaCode", areaId);
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
					String areaPaperPath = areaPath + "/" + subjectInfo.get("SUBJECT_NAME");
//					FileUtil.mkexpdir(areaPaperPath);
					RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), areaPaperPath);
//					科目-小题平均分对比	extKmxtpjfdbServer
					rpttaskseries.add(new RptTaskBase(r, "extKmxtpjfdbServer", "area", areaPaperPath, subjectInfo, areaId));
//					科目-小题分析	extFetchKmxtfxServer
					rpttaskseries.add(new RptTaskBase(r, "extFetchKmxtfxServer", "area", areaPaperPath, subjectInfo, areaId));
//					科目-分数段	extFetchKmfsdServer
					rpttaskseries.add(new RptTaskBase(r, "extFetchKmfsdServer", "area", areaPaperPath, subjectInfo, areaId));
//					科目-小题分	expGeKeXiaoTiFenService
					rpttaskseries.add(new RptTaskBase(r, "expGeKeXiaoTiFenService", "area", areaPaperPath, subjectInfo, areaId));
//					科目分数段对比   extKmfsddbvServer
					rpttaskseries.add(new RptTaskBase(r, "extKmfsddbvServer", "area", areaPaperPath, subjectInfo, areaId));
					this.add(rpttaskseries);
				}
			}
			return true;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean addSchoolTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			Map<String, Object> otherParams = r.getOtherParams();
			String rootPath = r.getPathFile();
			String schoolId = otherParams.get("schoolId").toString();
			String schoolName = otherParams.get("schoolName").toString();
			String areaName = otherParams.get("areaName").toString();
			String schoolPath = rootPath + "/学校报表/" + areaName + "/" + schoolName + "/全校报表";
			RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), schoolPath);
			for (Map<String, Object> subjectInfo : r.getSubjectList()) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = schoolPath + "/总分";
//					FileUtil.mkexpdir(zfPath);
//				总分-分数段	extZffsdServer
					rpttaskseries.add(new RptTaskBase(r, "extZffsdServer", "school", zfPath, null, schoolId));
//				各科综合	expGeKeZongHeService
					rpttaskseries.add(new RptTaskBase(r, "expGeKeZongHeService", "school", schoolPath, null, schoolId));
//				学生成绩	expXueShengChengJiService
					rpttaskseries.add(new RptTaskBase(r, "expXueShengChengJiService", "school", schoolPath, null, schoolId));
					if (r.isWL()) {
						//平均分对比-理科	expPingJunFenDuiBiService
						rpttaskseries.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "school", schoolPath, totalscoreMap_LK, schoolId));
						rpttaskseries.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "school", schoolPath, totalscoreMap_WK, schoolId));
					} else {
						//平均分对比	expPingJunFenDuiBiService
						rpttaskseries.add(new RptTaskBase(r, "expPingJunFenDuiBiService", "school", schoolPath, totalscoreMap_NWL, schoolId));
					}
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("paperId", subjectInfo.get("PAPER_ID"));
					whereParams.put("schoolId", schoolId);
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
					String subject = subjectInfo.get("SUBJECT").toString();
					String schoolPaperPath = schoolPath + "/" + subjectInfo.get("SUBJECT_NAME");
//						FileUtil.mkexpdir(schoolPaperPath);
//					科目-小题平均分对比	extKmxtpjfdbServer
					rpttaskseries.add(new RptTaskBase(r, "extKmxtpjfdbServer", "school", schoolPaperPath, subjectInfo, schoolId));
//					科目-小题分析	extFetchKmxtfxServer
					rpttaskseries.add(new RptTaskBase(r, "extFetchKmxtfxServer", "school", schoolPaperPath, subjectInfo, schoolId));
//					科目-分数段	extFetchKmfsdServer
					rpttaskseries.add(new RptTaskBase(r, "extFetchKmfsdServer", "school", schoolPaperPath, subjectInfo, schoolId));
//					科目-小题分	expGeKeXiaoTiFenService
					rpttaskseries.add(new RptTaskBase(r, "expGeKeXiaoTiFenService", "school", schoolPaperPath, subjectInfo, schoolId));
				}
			}
			this.add(rpttaskseries);
			return true;

		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean addClassTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			String rootPath = r.getPathFile();
			Map<String, Object> otherParams = r.getOtherParams();
			String classesId = otherParams.get("classesId").toString();
			String classesName = otherParams.get("classesName").toString();
			String schoolName = otherParams.get("schoolName").toString();
			String areaName = otherParams.get("areaName").toString();
			String classesPath = rootPath + "/学校报表/" + areaName + "/" + schoolName + "/" + classesName;
			/*	Optional<Map<String, Map<String, Object>>> classCache = DataCacheUtil.getClassesByExamid(r.getExambatchId());
				if (!classCache.isPresent()) throw new ReportExportException("班级缓存为空！");
				Map<String, Map<String, Object>> classesInfo = classCache.get();
				Map<String, Object> classInfo = classesInfo.get(classesId);*/
			Map<String, Object> classInfo = baseDataService.getClass(r.getExambatchId(), classesId);
			if (classInfo == null) return false;//过滤没有试卷的班级  原因是 学生数据升级的时候 没搞好 以至于 发布了一个没人考试的班级

			RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), classesPath);
			List<Map<String, Object>> wenPaperList = (List<Map<String, Object>>) otherParams.get("wenPaperList");
			List<Map<String, Object>> liPaperList = (List<Map<String, Object>>) otherParams.get("liPaperList");
			List<Map<String, Object>> paperList = null;
			if (classInfo.get("CLS_TYPE").toString().equals("1"))
				paperList = wenPaperList;
			else if (classInfo.get("CLS_TYPE").toString().equals("2"))
				paperList = liPaperList;
			else if (classInfo.get("CLS_TYPE").toString().equals("0"))
				paperList = r.getSubjectList();
			else
				return false;
			for (Map<String, Object> subjectInfo : paperList) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = classesPath + "/总分";
//							FileUtil.mkexpdir(zfPath);
//				总分-分数段	extZffsdServer
					rpttaskseries.add(new RptTaskBase(r, "extZffsdServer", "classes", zfPath, null, classesId));
//				各科综合	expGeKeZongHeService
					rpttaskseries.add(new RptTaskBase(r, "expGeKeZongHeService", "classes", classesPath, null, classesId));
//				学生成绩	expXueShengChengJiService
					rpttaskseries.add(new RptTaskBase(r, "expXueShengChengJiService", "classes", classesPath, null, classesId));
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("paperId", subjectInfo.get("PAPER_ID"));
					whereParams.put("classesId", classesId);
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
					String classesPaperPath = classesPath + "/" + subjectInfo.get("SUBJECT_NAME");
//						FileUtil.mkexpdir(classesPaperPath);
//					科目-小题分析	extFetchKmxtfxServer
					rpttaskseries.add(new RptTaskBase(r, "extFetchKmxtfxServer", "classes", classesPaperPath, subjectInfo, classesId));
//					科目-分数段	extFetchKmfsdServer
					rpttaskseries.add(new RptTaskBase(r, "extFetchKmfsdServer", "classes", classesPaperPath, subjectInfo, classesId));
//					科目-小题分	expGeKeXiaoTiFenService
					rpttaskseries.add(new RptTaskBase(r, "expGeKeXiaoTiFenService", "classes", classesPaperPath, subjectInfo, classesId));
				}
			}
			this.add(rpttaskseries);
			return true;
		}

	}

	/**
	 * 中山版
	 *
	 * @author admin
	 */
	private class RptTaskQueueZS extends RptTaskQueue {

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public boolean addCityTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			Map<String, Object> otherParams = r.getOtherParams();
			String rootPath = r.getPathFile();
			String cityPath = rootPath + "/" + "全市报表";
			for (Map<String, Object> subjectInfo : r.getSubjectList()) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = cityPath + "/总分";
//						FileUtil.mkexpdir(zfPath);
//				成绩汇总  expCjhzZSService
					this.add(new RptTaskBase(r, "expCjhzZSService", "city", zfPath));

					if (r.isWL()) {//分文理

					} else {
//					总分-分区成绩统计	expZfcjtjZSServer
						this.add(new RptTaskBase(r, "expZfcjtjZSService", "city2", zfPath, totalscoreMap_NWL));
//					总分-分学校成绩统计	expZfcjtjZSServer
						this.add(new RptTaskBase(r, "expZfcjtjZSService", "city", zfPath, totalscoreMap_NWL));
//					等级分数线	expDjfsxZSServer
						this.add(new RptTaskBase(r, "expDjfsxZSService", "city", zfPath));
					}
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("paperId", subjectInfo.get("PAPER_ID"));
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
					String cityPaperPath = cityPath + "/" + subjectInfo.get("SUBJECT_NAME");
//						FileUtil.mkexpdir(cityPaperPath);
//					分学校分科目分析	expKmcjtjZSService
					this.add(new RptTaskBase(r, "expKmcjtjZSService", "city", cityPaperPath, subjectInfo));
//					分区分科目分析	expKmcjtjZSService
					this.add(new RptTaskBase(r, "expKmcjtjZSService", "city2", cityPaperPath, subjectInfo));
//					分学科分小题分析	expXkfxtfxZSService
					this.add(new RptTaskBase(r, "expXkfxtfxZSService", "city", cityPaperPath, subjectInfo));
//					得分率对比	expKmxtdfldbZSService
					this.add(new RptTaskBase(r, "expKmxtdfldbZSService", "city", cityPaperPath, subjectInfo));
//					平均分对比	expKmxtpjfdbZSService
					this.add(new RptTaskBase(r, "expKmxtpjfdbZSService", "city", cityPaperPath, subjectInfo));
				}
			}
			//加入等待
			this.add(new RptTaskWait(r.getPathFile()));
			return true;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public boolean addAreaTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			String rootPath = r.getPathFile();
			Map<String, Object> otherParams = r.getOtherParams();
			String areaId = otherParams.get("areaId").toString();
			String areaName = otherParams.get("areaName").toString();
			String areaPath = rootPath + "/" + areaName + "/全区报表";

			RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), areaPath);
			for (Map<String, Object> subjectInfo : r.getSubjectList()) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = areaPath + "/总分";
//						FileUtil.mkexpdir(zfPath);
//				成绩汇总  expCjhzZSService
					rpttaskseries.add(new RptTaskBase(r, "expCjhzZSService", "area", zfPath, totalscoreMap_NWL, areaId));

					if (r.isWL()) {

					} else {
//					总分-分学校成绩统计	expZfcjtjZSServer
						rpttaskseries.add(new RptTaskBase(r, "expZfcjtjZSService", "area", zfPath, totalscoreMap_NWL, areaId));
					}
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("areaCode", areaId);
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
					String areaPaperPath = areaPath + "/" + subjectInfo.get("SUBJECT_NAME");
//						FileUtil.mkexpdir(areaPaperPath);
//					分学校分科目分析	expKmcjtjZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmcjtjZSService", "area", areaPaperPath, subjectInfo, areaId));
//					分学科分小题分析	expXkfxtfxZSService
					rpttaskseries.add(new RptTaskBase(r, "expXkfxtfxZSService", "area", areaPaperPath, subjectInfo, areaId));
//					得分率对比	expKmxtdfldbZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmxtdfldbZSService", "area", areaPaperPath, subjectInfo, areaId));
//					平均分对比	expKmxtpjfdbZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmxtpjfdbZSService", "area", areaPaperPath, subjectInfo, areaId));
				}
			}
			this.add(rpttaskseries);
			return true;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public boolean addSchoolTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			String rootPath = r.getPathFile();
			Map<String, Object> otherParams = r.getOtherParams();
			String schoolId = otherParams.get("schoolId").toString();
			String schoolName = otherParams.get("schoolName").toString();
			String areaName = otherParams.get("areaName").toString();
			String schoolPath = rootPath + "/" + areaName + "/" + schoolName + "/全校报表";

			RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), schoolPath);
			for (Map<String, Object> subjectInfo : r.getSubjectList()) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = schoolPath + "/总分";
//						FileUtil.mkexpdir(zfPath);
//				总分-分班级成绩统计	expZfcjtjZSServer
					rpttaskseries.add(new RptTaskBase(r, "expZfcjtjZSService", "school", zfPath, totalscoreMap_NWL, schoolId));
//				成绩汇总  expCjhzZSService
					rpttaskseries.add(new RptTaskBase(r, "expCjhzZSService", "school", zfPath, totalscoreMap_NWL, schoolId));
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("paperId", subjectInfo.get("PAPER_ID"));
					whereParams.put("schoolId", schoolId);
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
					String schoolPaperPath = schoolPath + "/" + subjectInfo.get("SUBJECT_NAME");
//						FileUtil.mkexpdir(schoolPaperPath);
//					分学校分科目分析	expKmcjtjZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmcjtjZSService", "school", schoolPaperPath, subjectInfo, schoolId));
//					分学科分小题分析	expXkfxtfxZSService
					rpttaskseries.add(new RptTaskBase(r, "expXkfxtfxZSService", "school", schoolPaperPath, subjectInfo, schoolId));
//					得分率对比	expKmxtdfldbZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmxtdfldbZSService", "school", schoolPaperPath, subjectInfo, schoolId));
//					平均分对比	expKmxtpjfdbZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmxtpjfdbZSService", "school", schoolPaperPath, subjectInfo, schoolId));
				}
			}
			this.add(rpttaskseries);
			return true;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public boolean addClassTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			String rootPath = r.getPathFile();
			Map<String, Object> otherParams = r.getOtherParams();
			String classesId = otherParams.get("classesId").toString();
			/*	Optional<Map<String, Map<String, Object>>> classCache = DataCacheUtil.getClassesByExamid(r.getExambatchId());
				if (!classCache.isPresent()) throw new ReportExportException("班级缓存为空！");
				Map<String, Map<String, Object>> classesInfo = classCache.get();
				Map<String,Object> classInfo = classesInfo.get(classesId);*/
			Map<String, Object> classInfo = baseDataService.getClass(r.getExambatchId(), classesId);
			if (classInfo == null) return false;
			String classesName = otherParams.get("classesName").toString();
			String schoolName = otherParams.get("schoolName").toString();
			String areaName = otherParams.get("areaName").toString();
			String classesPath = rootPath + "/" + areaName + "/" + schoolName + "/" + classesName;

			RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), classesPath);

			List<Map<String, Object>> wenPaperList = (List<Map<String, Object>>) otherParams.get("wenPaperList");
			List<Map<String, Object>> liPaperList = (List<Map<String, Object>>) otherParams.get("liPaperList");
			List<Map<String, Object>> paperList = null;
			if (classInfo.get("CLS_TYPE").toString().equals("1"))
				paperList = wenPaperList;
			else if (classInfo.get("CLS_TYPE").toString().equals("2"))
				paperList = liPaperList;
			else if (classInfo.get("CLS_TYPE").toString().equals("0"))
				paperList = r.getSubjectList();
			for (Map<String, Object> subjectInfo : paperList) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = classesPath + "/总分";
//						FileUtil.mkexpdir(zfPath);
//				总分-分班级成绩统计	expZfcjtjZSServer
					rpttaskseries.add(new RptTaskBase(r, "expZfcjtjZSService", "classes", zfPath, totalscoreMap_NWL, classesId));
//				成绩汇总  expCjhzZSService
					rpttaskseries.add(new RptTaskBase(r, "expCjhzZSService", "classes", zfPath, totalscoreMap_NWL, classesId));
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("paperId", subjectInfo.get("PAPER_ID"));
					whereParams.put("classesId", classesId);
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
					String classesPaperPath = classesPath + "/" + subjectInfo.get("SUBJETC_NAME");
//						FileUtil.mkexpdir(classesPaperPath);
//					分学校分科目分析	expKmcjtjZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmcjtjZSService", "classes", classesPaperPath, subjectInfo, classesId));
//					分学科分小题分析	expXkfxtfxZSService
					rpttaskseries.add(new RptTaskBase(r, "expXkfxtfxZSService", "classes", classesPaperPath, subjectInfo, classesId));
//					得分率对比	expKmxtdfldbZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmxtdfldbZSService", "classes", classesPaperPath, subjectInfo, classesId));
//					平均分对比	expKmxtpjfdbZSService
					rpttaskseries.add(new RptTaskBase(r, "expKmxtpjfdbZSService", "classes", classesPaperPath, subjectInfo, classesId));
				}
			}
			this.add(rpttaskseries);
			return true;
		}

	}

	/**
	 * 孝感版
	 *
	 * @author admin
	 */
	private class RptTaskQueueXG extends RptTaskQueue {

		@Override
		public boolean addCityTask(RptParameter parameter) throws Exception {
			RptParameterBase r = (RptParameterBase) parameter;
			String rootPath = r.getPathFile();
			String cityPath = rootPath + "/" + "全市报表";

			Map<String, Object> otherParams = r.getOtherParams();
			for (Map<String, Object> subjectInfo : r.getSubjectList()) {
				if (subjectInfo.get("SUBJECT").toString().equals("total")) {
					String zfPath = cityPath + "/总分";
//						FileUtil.mkexpdir(zfPath);
//				学科分析	extFetchXkfxServer
					this.add(new RptTaskBase(r, "extFetchXkfxServer", "city", zfPath));
//				学生成绩  expXueShengChengJiService
					this.add(new RptTaskBase(r, "expXueShengChengJiService", "city", zfPath));
					if (r.isWL()) {//分文理
//					理科总分分段统计表	extZffsdXGServer
						this.add(new RptTaskBase(r, "extZffsdXGServer", "city", zfPath, totalscoreMap_LK));
						this.add(new RptTaskBase(r, "extZffsdXGServer", "city", zfPath, totalscoreMap_WK));
//					理科总分细化表	extZfxhXGServer
						this.add(new RptTaskBase(r, "extZfxhXGServer", "city", zfPath, totalscoreMap_LK));
						this.add(new RptTaskBase(r, "extZfxhXGServer", "city", zfPath, totalscoreMap_WK));

					} else {
//					理科总分分段统计表	extZffsdXGServer
						this.add(new RptTaskBase(r, "extZffsdXGServer", "city", zfPath, totalscoreMap_NWL));
//					理科总分细化表	extZfxhXGServer
						this.add(new RptTaskBase(r, "extZfxhXGServer", "city", zfPath, totalscoreMap_NWL));
					}
				}
				if (!subjectInfo.get("SUBJECT").toString().equals("total")) {
					HashMap<String, Object> whereParams = new HashMap(otherParams);
					whereParams.put("paperId", subjectInfo.get("PAPER_ID"));
					//若该科目没有参加考试的班级，则不执行下面代码
					List<Map> classList = jyjRptExtMapper.qryExamClasses(whereParams);
					if (classList.size() <= 0)
						continue;
					String cityPaperPath = cityPath + "/" + subjectInfo.get("SUBJETC_NAME");
//						FileUtil.mkexpdir(cityPaperPath);
					RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), cityPaperPath);
//					科目-小题分析	extFetchKmxtfxServer
					rpttaskseries.add(new RptTaskBase(r, "extFetchKmxtfxServer", "city", cityPaperPath, subjectInfo));
//					科目-综合指标	expZongHeZhiBiaoService
					rpttaskseries.add(new RptTaskBase(r, "expZongHeZhiBiaoService", "city", cityPaperPath, subjectInfo));
//					正太分布	expZhengTaiFenBuService
					rpttaskseries.add(new RptTaskBase(r, "expZhengTaiFenBuService", "city", cityPaperPath, subjectInfo));
//					成绩统计	expChengJiTongJiService
					rpttaskseries.add(new RptTaskBase(r, "expChengJiTongJiService", "city", cityPaperPath, subjectInfo));
//					试题统计 expShiTiTongJiService
					rpttaskseries.add(new RptTaskBase(r, "expShiTiTongJiService", "city", cityPaperPath, subjectInfo));
					this.add(rpttaskseries);
				}
			}
			return true;
		}

		@Override
		public boolean addAreaTask(RptParameter parameter) throws Exception {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean addSchoolTask(RptParameter parameter) throws Exception {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean addClassTask(RptParameter parameter) throws Exception {
			// TODO Auto-generated method stub
			return false;
		}
	}

	/**
	 * 湖北版
	 *
	 * @author admin
	 */
	private class RptTaskQueueHB extends RptTaskQueue {

		@Override
		public boolean addCityTask(RptParameter parameter) throws Exception {

			return true;
		}

		@Override
		public boolean addAreaTask(RptParameter parameter) throws Exception {
			// TODO Auto-generated method stub
			return false;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public boolean addSchoolTask(RptParameter parameter) throws Exception {

			RptParameterBase r = (RptParameterBase) parameter;
			String rootPath = r.getPathFile();
			Map<String, Object> otherParams = r.getOtherParams();
			String schoolId = otherParams.get("schoolId").toString();
			String schoolName = otherParams.get("schoolName").toString();
			String areaName = otherParams.get("areaName").toString();
			String schoolPath = rootPath + "/" + areaName + "/" + schoolName;

			RptTaskSeries rpttaskseries = new RptTaskSeries(r.getExambatchId(), schoolPath);

			rpttaskseries.add(new RptTaskBase(r, "expStudentScoreService", "classes", schoolPath+"/多科",
					null, schoolId));

			this.add(rpttaskseries);
			return true;
		}

		@Override
		public boolean addClassTask(RptParameter parameter) throws Exception {
			// TODO Auto-generated method stub
			return false;
		}
	}
}
