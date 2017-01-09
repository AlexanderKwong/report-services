package zyj.report.business.task;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings({ "hiding", "rawtypes" })
public abstract class RptTaskQueue<RptTask> extends ConcurrentLinkedQueue implements Cloneable {

	public abstract boolean addCityTask(RptParameter parameter)  throws Exception;

	public abstract boolean addAreaTask(RptParameter parameter)  throws Exception;
	
	public abstract boolean addSchoolTask(RptParameter parameter)  throws Exception;
	
	public abstract boolean addClassTask(RptParameter parameter)  throws Exception;

	public static final Map<String,Object> totalscoreMap_NWL = getTotalScoreMap();
	public static final Map<String,Object> totalscoreMap_WK = getTotalScoreMap();
	public static final Map<String,Object> totalscoreMap_LK = getTotalScoreMap();
	public static final Map<String,Object> totalscoreMap_ZF = getTotalScoreMap();
	private static Map<String,Object> getTotalScoreMap(){
		Map<String,Object> totalscoreMap = new HashMap<>();
		totalscoreMap.put("PAPER_ID","");
		totalscoreMap.put("SUBJECT","");
		totalscoreMap.put("SUBJECT_NAME", "总分");
		totalscoreMap.put("TYPE",0);
		return totalscoreMap;
	}
	static {
		totalscoreMap_NWL.put("SUBJECT","NWL");;
		totalscoreMap_WK.put("SUBJECT","WK");;
		totalscoreMap_LK.put("SUBJECT","LK");
		totalscoreMap_ZF.put("SUBJECT","ZF");
		totalscoreMap_NWL.put("SUBJECT_NAME","");;
		totalscoreMap_WK.put("SUBJECT_NAME","文科总分");;
		totalscoreMap_LK.put("SUBJECT_NAME","理科总分");
		totalscoreMap_ZF.put("SUBJECT_NAME","总分");
		totalscoreMap_NWL.put("TYPE",0);
		totalscoreMap_WK.put("TYPE",0);
		totalscoreMap_LK.put("TYPE",0);
		totalscoreMap_ZF.put("TYPE",0);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
