package zyj.report.service;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.business.task.RptTask;
import zyj.report.business.task.RptTaskFactory;
import zyj.report.business.task.RptTaskQueue;
import zyj.report.business.task.impl.RptParameterBase;
import zyj.report.business.task.impl.RptTaskWait;
import zyj.report.common.SpringUtil;
import zyj.report.common.logging.ThreadNameBasedDiscriminator;
import zyj.report.common.util.FileUtil;
import zyj.report.common.util.FtpUtil;
import zyj.report.exception.report.ReportConfirmException;
import zyj.report.exception.report.ReportExistException;
import zyj.report.exception.report.ReportExportException;
import zyj.report.persistence.client.JyjRptExtMapper;
import zyj.report.service.export.BaseRptService;
import zyj.report.service.redis.RedisService;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @author kuangxiaolin 2016年11月17日
 *
 *         教育局联考报表
 */


@Service
public class JyjRptExtService {

	@Autowired
	JyjRptExtMapper jyjRptExtMapper;
	@Autowired
	BaseDataService baseDataService;
	@Autowired
	RptTaskFactory rpttaskfactory;

/*	@Value("${rpt.ftpUserName}")
	String ftpUserName ;
	@Value("${rpt.ftpPassword}")
	String ftpPassword ;
	@Value("${rpt.ftpHost}")
	String ftpHost ;
	@Value("${rpt.ftpPort}")
	int ftpPort;*/

	@Value("${rpt.path}")
	String filePath;

	private static Logger logger = LoggerFactory.getLogger(JyjRptExtService.class);

	public RptTaskQueue<RptTask> getRptTaskQueue(final String examId,final int stuType, final int rptType, final List<String> paperIds2Exp) throws Exception {
			// 读取考试场次信息
			Map examBatch = jyjRptExtMapper.qryExambatch(examId);
			if (examBatch == null)
				throw new ReportExportException("考试场次不存在!");

			String examName = (String) examBatch.get("NAME");
			String rptPath = filePath + "/examrpt_" + examId + "_" + rptType + "_" + stuType + "_" + paperIds2Exp;
			FileUtil.rmvDir(rptPath);

			List<Map<String, Object>> subjects2Exp = new ArrayList<Map<String, Object>>();//需要单导的科目 每个map中keySet为：PAPER_ID,SUBJECT,SUBJECT_NAME
			List<Map<String, Object>> taskList1 = new ArrayList<Map<String, Object>>();
			RptTaskQueue rpttaskqueue;
			boolean wenliFlag = false;
//			BaseRptService.updateSubjecName(examId);

			Map gradParam = new HashMap();
//			gradParam.put("finished", "true");
			gradParam.put("examId", examId);
//			gradParam.put("grade", grade);

//			List<Map> allPaperList = new ArrayList<Map>();
			List<Map<String, Object>> wenPaperList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> liPaperList = new ArrayList<Map<String, Object>>();
			//add by 邝晓林 ,科目拆分后不能根据原始科目来传参
		/*	Optional<List<Map<String, Object>>> subjectCache = DataCacheUtil.getSubjectByExamid(examId);
			if (!subjectCache.isPresent()) {
				ReportExportException err = new ReportExportException("科目缓存为空！");
				logger.error("", err);
				throw err;
			}
			List<Map<String, Object>> subjectList = subjectCache.get();*/
			List<Map<String, Object>> subjectList = baseDataService.getSubjectByExamid(examId);

			Map<String, Object> totalscoreMap = new HashMap<>();
			totalscoreMap.put("PAPER_ID", "total");
			totalscoreMap.put("SUBJECT", "total");
			totalscoreMap.put("SUBJECT_NAME", "总分");
			if (paperIds2Exp.contains("all")) {
				subjects2Exp = new ArrayList<>(subjectList);
				subjects2Exp.add(totalscoreMap);
			} else if (paperIds2Exp.contains("total")) {//这里是否直接 将文理科的总分加进去？
				subjects2Exp.add(totalscoreMap);
			} else
				subjects2Exp = subjectList.stream().filter(map -> paperIds2Exp.contains(map.get("PAPER_ID").toString())).collect(Collectors.toList());

//			if(true){//20160416 修改 孝感地区高一也会出现 文理分科 为避免这个问题 全部年级都走一次
			boolean wen = false;
			boolean li = false;

			List<Map> types = jyjRptExtMapper.qryExamType(gradParam);
			for (Map type : types) {
				if (type.get("TYPE").toString().equals("1"))
					wen = true;
				if (type.get("TYPE").toString().equals("2"))
					li = true;
				if (type.get("TYPE").toString().equals("0")) {
					wen = false;
					li = false;
					break;
				}
			}
//				if (wen == true && li == true) {//20160415 修改 避免单场考试只有文科、理科的情况
			if (wen == true || li == true) {
				wenliFlag = true;
				for (Map<String, Object> subjectInfo : subjects2Exp) {
//					String subject = (String) subjectInfo.get("SUBJECT");
					String type = (String) subjectInfo.get("TYPE");
//					if (Arrays.asList(BaseRptService.subjects_w).contains(subject)) {
//						wenPaperList.add(subjectInfo);
//					} else if (Arrays.asList(BaseRptService.subjects_l).contains(subject))
//						liPaperList.add(subjectInfo);
					if ("1".equals(type)) {
						wenPaperList.add(subjectInfo);
					} else if ("2".equals(type))
						liPaperList.add(subjectInfo);
				}
				wenPaperList.add(totalscoreMap);
				liPaperList.add(totalscoreMap);
			}
//			}
			gradParam.put("wenPaperList", wenPaperList);
			gradParam.put("liPaperList", liPaperList);
			RptParameterBase parameter = new RptParameterBase(rptType, wenliFlag, rptPath, examId, "", stuType, subjects2Exp, gradParam);
//			RptTaskFactory rpttaskfactory = SpringUtil.getSpringBean(null, "rptTaskFactory");
			rpttaskqueue = rpttaskfactory.create(parameter);

			/*************************全市报表  ***********************/
			List<Map> cityList = jyjRptExtMapper.qryExamCity(examId);
			for (Map city : cityList) {
				HashMap<String, Object> otherParams;
				Map cityParams = new HashMap(gradParam);
				String cityCode = city.get("CITYCODE").toString();
				cityParams.put("cityCode", cityCode);
				otherParams = new HashMap<String, Object>();

				String fullName = "" + city.get("NAME") + ' ' + examName;

				String rootPath = rptPath + "/" + fullName + " 报表";
				//报表参数加到队列

				RptParameterBase cityParameter = new RptParameterBase(parameter);
				cityParameter.setPathFile(rootPath);
				cityParameter.setCityCode(cityCode);
				rpttaskqueue.addCityTask(cityParameter);

				/************** 全区报表********************/
//            if (level == null || level >= 2) {
				List<Map> areaList = jyjRptExtMapper.qryExamArea(cityParams);
				for (Map area : areaList) {
					if (area == null)
						break;
					Map areaParams = new HashMap(cityParams);
					areaParams.put("areaCode", area.get("AREACODE"));
					areaParams.put("areaId", area.get("AREACODE"));
					areaParams.put("areaName", area.get("NAME"));
					//报表参数加到队列

					RptParameterBase areaParameter = new RptParameterBase(cityParameter);
					areaParameter.setOtherParams(new HashMap(areaParams));
					rpttaskqueue.addAreaTask(areaParameter);

//                    if (level == null || level >= 3) {
					/************** 全校报表*******************/
					List<Map> schoolList = jyjRptExtMapper.qryExamSchool(areaParams);
//							for (Map school : schoolList) {
					//将学校维度处理并行化
					final List<Map<String, Object>> finalSubjects2Exp = subjects2Exp;
					schoolList.parallelStream().forEach(school -> {
						try {
							Map schoolParams = new HashMap(areaParams);
							schoolParams.put("schoolId", school.get("ID"));
							schoolParams.put("schoolName", school.get("NAME"));

							schoolParams.put("subjects2Exp", finalSubjects2Exp);
							//报表参数加到队列

							RptParameterBase schoolParameter = new RptParameterBase(areaParameter);
							schoolParameter.setOtherParams(new HashMap(schoolParams));
							rpttaskqueue.addSchoolTask(schoolParameter);

//                                if (level == null || level >= 4) {
							/************************** 班级报表 *************************/
							List<Map> classList = jyjRptExtMapper.qryExamClasses(schoolParams);
							for (Map classes : classList) {
								Map classesParams = new HashMap(schoolParams);
								classesParams.put("classesId", classes.get("ID"));
								classesParams.put("classesName", classes.get("NAME"));

								//报表参数加到队列
								RptParameterBase classesParameter = new RptParameterBase(schoolParameter);
								classesParameter.setOtherParams(new HashMap(classesParams));
								rpttaskqueue.addClassTask(classesParameter);
							}
//                                }
						} catch (Exception e) {
							e.printStackTrace();

						}
					});
//                    }
				}
//            }
			}
			return rpttaskqueue;
		}


	public void createTaskQueue(String examId, RptTaskQueue rpttaskqueue, Map gradParam)	{
		List<Map<String, Object>> classesInfo =  baseDataService.getClasses(examId);
		List<Map<String, Object>> schoolsInfo =  baseDataService.getSchools(examId);
		List<Map<String, Object>> areasInfo =  baseDataService.getAreas(examId);

	}

/***************************************   test   ************************************************************/
	private static String curExamId = "";

	private static Integer maxThreadNum = 30;

	private static AtomicInteger curThreadNum = new AtomicInteger(0);

	private static String curProgress = "--";

public static class MainTaskThread extends Thread {
	String examname;

//	String rptpath;

	RptTaskQueue taskQueue;

	public MainTaskThread(String examId, String examname, RptTaskQueue taskQueue) {
		synchronized(curExamId) {
			curExamId = examId;
		}
		this.examname = examname;
//		this.rptpath = rptpath;
		this.taskQueue = taskQueue;
//			this.schPath = schPath;

		//根据Thread.getName()来打log，而线程名由task.getJobId()来决定
		ThreadNameBasedDiscriminator.setLogFileName(curExamId);
	}

	@SuppressWarnings("rawtypes")
	public void run(){
		logger.info("导出开始：" + examname);

		curThreadNum.getAndSet(0);
		int lastProcess = -1;
		int newProcess = 0;
		int total = taskQueue.size();
		while (taskQueue.size() > 0) {
			try {
				// 小于maxThreadNum个线程则新开线程
				if (curThreadNum.get() < maxThreadNum) {

					curThreadNum.getAndIncrement();

					//更新进度
					curProgress = total - taskQueue.size() + "/" + (total + maxThreadNum);
					logger.info("进度:" + curProgress);

					RptTask task = (RptTask)taskQueue.remove();
					if(task.getClass().equals(RptTaskWait.class)){
						while(true){
							logger.info("==========当前子线程数" + curThreadNum.get() + "============");

							if (curThreadNum.get() <= 1)
								break;
							Thread.sleep(1000);
						}
						curThreadNum.getAndDecrement();
					}else{
						@SuppressWarnings("unchecked")
						Thread subtask = new SubTaskThread(task);
						subtask.start();
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// 等待子线程已经全部结束
		while (true) {
			try {
				//更新进度
				newProcess = total + maxThreadNum - curThreadNum.get();
				if (newProcess != lastProcess) {
					curProgress = newProcess + "/" + (total + maxThreadNum);
					logger.info("进度:" + curProgress);
					lastProcess = newProcess;
				}

				if (curThreadNum.get() <= 0)
					break;

				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//压缩报告文件
		try{
			//清缓存
			RedisService redisService = (RedisService)SpringUtil.getSpringBean(null,"redisService");
			Set<String> keys = redisService.keys(curExamId + "*");
			redisService.del(keys.toArray(new String[keys.size()]));

			//创建压缩目录
//			FileUtil.zipDir(rptpath, rptpath + ".zip");
//			FileUtil.rmvDir(rptpath);
			//上传到126 126的机子不用 切记切记！！！！
//			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("导出结束：" + examname);
		synchronized(curExamId){
			curExamId = "";
		}
	}
}

	public static class SubTaskThread extends Thread {
		RptTask task ;

		public SubTaskThread(RptTask task) {
			this.task = task;
			//根据Thread.getName()来打log，而线程名由task.getJobId()来决定
			ThreadNameBasedDiscriminator.setLogFileName(curExamId);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run() {
			try {
				task.run();
			} catch (Throwable e) {

				String dir = task.getPathFile();
				logger.info("ERR:未知异常!--" + dir);
				e.printStackTrace();

				String logDir =dir.substring(0, dir.indexOf("]")+1)+"/ErrorLog.txt";
				File log = new File(logDir);
				try {
					if(!log.exists())
						log.createNewFile();
					logfile(logDir,dir,e);

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} finally {
				curThreadNum.getAndDecrement();
			}
		}
	}
	public static boolean logfile(String dir,String msg , Throwable e) throws IOException {
		boolean flag = false;
		byte[] buff = new byte[] {};
		String message = "ERR:未知异常!--" + msg+"\r\n";
		buff = message.getBytes();
		FileOutputStream out = new FileOutputStream(dir, true);
		out.write("\r\n".getBytes());
		out.write(buff);
		e.printStackTrace(new PrintStream(out));
		out.flush();
		out.close();
		flag = true;
		return flag;
	}


}
