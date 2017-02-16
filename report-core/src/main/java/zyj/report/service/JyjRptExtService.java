package zyj.report.service;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zyj.report.business.task.RptParameter;
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

import java.awt.font.FontRenderContext;
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
			boolean wenliFlag = false;

			Map gradParam = new HashMap();
			gradParam.put("examId", examId);
			gradParam.put("examName", examName);


			//取得所有的科目
			List<Map<String, Object>> subjectList = baseDataService.getSubjectByExamid(examId);

			//除了科目，加上“总分” 来导出
			Map<String, Object> totalscoreMap = new HashMap<>();
			totalscoreMap.put("PAPER_ID", "total");
			totalscoreMap.put("SUBJECT", "total");
			totalscoreMap.put("SUBJECT_NAME", "总分");
			totalscoreMap.put("TYPE", "0");
			if (paperIds2Exp.contains("all")) {
				subjects2Exp = new ArrayList<>(subjectList);
				subjects2Exp.add(totalscoreMap);
			} else if (paperIds2Exp.contains("total")) {//这里是否直接 将文理科的总分加进去？
				subjects2Exp.add(totalscoreMap);
			} else
				subjects2Exp = subjectList.stream().filter(map -> paperIds2Exp.contains(map.get("PAPER_ID").toString())).collect(Collectors.toList());

			//判断此考试是否包含文理科
			boolean wen = false;
			boolean li = false;
			List<Map<String, Object>> wenPaperList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> liPaperList = new ArrayList<Map<String, Object>>();
			List<Map> types = jyjRptExtMapper.qryExamType(gradParam);
			for (Map type : types) {
				if (Integer.parseInt(type.get("TYPE").toString()) == 1)
					wen = true;
				if (Integer.parseInt(type.get("TYPE").toString()) == 2)
					li = true;
				if (Integer.parseInt(type.get("TYPE").toString()) == 0) {
					wen = false;
					li = false;
					break;
				}
			}

			if (wen == true || li == true) {
				wenliFlag = true;
				for (Map<String, Object> subjectInfo : subjects2Exp) {
					int type =  Integer.parseInt(subjectInfo.get("TYPE").toString());
					if (1 == type) {
						wenPaperList.add(subjectInfo);
					} else if (2 == type)
						liPaperList.add(subjectInfo);
				}
				wenPaperList.add(totalscoreMap);
				liPaperList.add(totalscoreMap);
			}
			gradParam.put("wenPaperList", wenPaperList);
			gradParam.put("liPaperList", liPaperList);

			//初始化参数
			RptParameterBase parameter = new RptParameterBase(rptType, wenliFlag, rptPath, examId, "", stuType, subjects2Exp, gradParam);

			//添加任务
			return createTaskQueue(parameter);

		}

	/**
	 * 根据参数创建任务队列
	 * @param parameter
	 * @return
	 * @throws Exception
	 */
	private RptTaskQueue createTaskQueue(RptParameterBase parameter) throws Exception {

		RptTaskQueue rpttaskqueue = rpttaskfactory.create(parameter);
		//获取查询条件
		String examId = parameter.getExambatchId();
		Map gradParam = parameter.getOtherParams();
		String examName = gradParam.get("examName").toString();

		List<Map<String, Object>> classesInfo =  baseDataService.getClasses(examId);
		List<Map<String, Object>> schoolsInfo =  baseDataService.getSchools(examId);
		List<Map<String, Object>> areasInfo =  baseDataService.getAreas(examId);
		List<Map> cityInfo = jyjRptExtMapper.qryExamCity(examId);
		if (cityInfo.size() != 1) throw new ReportExportException("找不到唯一参考地市");

		//获取地市，设置参数
		Map city = cityInfo.get(0);
		Map cityParams = new HashMap(gradParam);
		String cityCode = city.get("CITYCODE").toString();
		cityParams.put("cityCode", cityCode);
		String fullName = "" + city.get("NAME") + ' ' + examName;
		String rootPath = parameter.getPathFile() + "/" + fullName + " 报表";
		//添加市维度的报表任务
		RptParameterBase cityParameter = new RptParameterBase(parameter);
		cityParameter.setPathFile(rootPath);
		cityParameter.setCityCode(cityCode);
		rpttaskqueue.addCityTask(cityParameter);
		//添加区维度的报表任务
		for (Map area : areasInfo){
			Map areaParams = new HashMap(cityParams);
			areaParams.put("areaId", area.get("AREA_ID"));
			areaParams.put("areaName", area.get("AREA_NAME"));
			RptParameterBase areaParameter = new RptParameterBase(cityParameter);
			areaParameter.setOtherParams(new HashMap(areaParams));
			rpttaskqueue.addAreaTask(areaParameter);
		}
		//添加校维度的报表任务
		for (Map school : schoolsInfo){
			Map schoolParams = new HashMap(cityParams);
			schoolParams.put("schoolId", school.get("SCH_ID"));
			schoolParams.put("schoolName", school.get("SCH_NAME"));
			schoolParams.put("areaId", school.get("AREA_ID"));
			schoolParams.put("areaName", school.get("AREA_NAME"));
			RptParameterBase schoolParameter = new RptParameterBase(cityParameter);
			schoolParameter.setOtherParams(new HashMap(schoolParams));
			rpttaskqueue.addSchoolTask(schoolParameter);
		}
		//添加班维度的报表任务
		for (Map clazz : classesInfo){
			Map classesParams = new HashMap(cityParams);
			classesParams.put("schoolId", clazz.get("SCH_ID"));
			classesParams.put("schoolName", clazz.get("SCH_NAME"));
			classesParams.put("areaId", clazz.get("AREA_ID"));
			classesParams.put("areaName", clazz.get("AREA_NAME"));
			classesParams.put("classesId", clazz.get("CLS_ID"));
			classesParams.put("classesName", clazz.get("CLS_NAME"));
			RptParameterBase classesParameter = new RptParameterBase(cityParameter);
			classesParameter.setOtherParams(new HashMap(classesParams));
			rpttaskqueue.addClassTask(classesParameter);
		}

		return rpttaskqueue;
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
			long start = System.currentTimeMillis();
			Set<String> keys = redisService.keys(curExamId + "*");
			long keysCost = System.currentTimeMillis();
			logger.info(String.format("匹配KEYS耗时: %s(ms)", keysCost - start));
			logger.info("共删除缓存数据条数为：" + redisService.delWithPipline(keys.toArray(new String[keys.size()])));
			logger.info(String.format("删除KEYS耗时: %s(ms)",System.currentTimeMillis() - keysCost));
			logger.info(String.format("清缓存耗时: %s(ms)",System.currentTimeMillis() - start));
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
