package zyj.report.service.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zyj.report.common.constant.EnmSubjectType;
import zyj.report.exception.report.ReportExportException;
import zyj.report.service.model.RptParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseRptService {

	private Logger logger = LoggerFactory.getLogger(BaseRptService.class);

	private static String[][] grades = new String[][]{{"x1", "小学1年级"},
			{"x2", "小学2年级"}, {"x3", "小学3年级"}, {"x4", "小学4年级"},
			{"x5", "小学5年级"}, {"x6", "小学6年级"}, {"c1", "初中1年级"},
			{"c2", "初中2年级"}, {"c3", "初中3年级"}, {"c4", "初中4年级"},
			{"g1", "高中1年级"}, {"g2", "高中2年级"}, {"g3", "高中3年级"}};

	public static String getGradeName(String id) {
		for (String[] grade : grades) {
			if (id.equals(grade[0])) {
				return grade[1];
			}
		}
		return null;
	}

	// 科目
	public static final Map<String, String> subjects = new HashMap<String, String>();

	static {
		initSubjectName();
	}

	//文科的科目
//	public static final String[] subjects_w = new String[]{"WYW_S","WSX","WSX_S","WYY_S","DL","LS","ZZ","S_ZH_DL","S_ZH_LS","S_ZH_ZS","WZ"};
	//理科的科目
//	public static final String[] subjects_l = new String[]{"LYW_S","LSX","LSX_S","LYY_S","WL","HX","SW","S_ZH_WL","S_ZH_HX","S_ZH_SW","LZ"};
	//拆科的科目
//	public static final String[] subjects_s=  new String[]{"WYW_S","WYY_S","LYW_S","LYY_S","WSX_S","S_ZH_DL","S_ZH_LS","S_ZH_ZS","LSX_S","S_ZH_WL","S_ZH_HX","S_ZH_SW"};

	public static void initSubjectName() {
		subjects.put("YW", "语文");
		subjects.put("SX", "数学");
		subjects.put("YY", "英语");
		subjects.put("YY_N_L", "英语笔试");//英语非导入成绩总和
		subjects.put("WL", "物理");
		subjects.put("HX", "化学");
		subjects.put("SW", "生物");
		subjects.put("DL", "地理");
		subjects.put("ZZ", "政治");
		subjects.put("LS", "历史");
		subjects.put("JK", "健康");
		subjects.put("KX", "科学");
		subjects.put("XX", "信息");
		subjects.put("TY", "体育");
		subjects.put("MU", "音乐");
		subjects.put("MS", "美术");
		subjects.put("SF", "书法");
//		subjects.put("SF", "习作");
		subjects.put("SP", "思想品德");
//		subjects.put("WZ", "政史综合");///////////////////为了宿迁那傻逼
//		subjects.put( "LZ", "理化综合");////////////////////为了宿迁那傻逼
		subjects.put("WZ", "文科综合");///////////正常
		subjects.put("LZ", "理科综合");///////////正常
		subjects.put("WSX", "文科数学");
		subjects.put("LSX", "理科数学");
		subjects.put("RY", "日语");
		subjects.put("SH", "社会");
		subjects.put("ZH", "综合");
		subjects.put("ZF", "总分");
		subjects.put("WK", "文科总分");
		subjects.put("LK", "理科总分");
		subjects.put("LSX_S", "理科数学");
		subjects.put("WSX_S", "文科数学");
		subjects.put("WYW_S", "文科语文");
		subjects.put("LYW_S", "理科语文");
		subjects.put("WYY_S", "文科英语");
		subjects.put("LYY_S", "理科英语");
		subjects.put("S_ZH_WL", "物理");
		subjects.put("S_ZH_HX", "化学");
		subjects.put("S_ZH_SW", "生物");
		subjects.put("S_ZH_DL", "地理");
		subjects.put("S_ZH_ZS", "政治");
		subjects.put("S_ZH_LS", "历史");
		subjects.put("S_ZH_SP", "思想品德");
		subjects.put("NWL", "");
	}


	public List<Map<String, Object>> fetchExportData(Map<String, Object> parmter) throws Exception {
		return null;
	}

	public String getXlsFileName() {
		return null;
	}

	public void exportData(Map<String, Object> parmter) throws Exception {

	}


	/**
	 * 将查询得到的List<Map>转为String[][]
	 *
	 * @param fieldMap beanList中MAP的每个字段与titles映射到Object[]对应titles下标的元素
	 * @param titleArr EXCEL中每个字段的标题
	 * @param beanList 查询数据库得到map组成的list
	 * @return
	 */
	protected String[][] map2objects(Map<String, String> fieldMap, String[] titleArr, List<Map<String, Object>> beanList) {
		String[][] objArrList = new String[beanList.size()][];
		for (int j = 0; j < beanList.size(); j++) {
			String[] row = new String[titleArr.length];
			Map bean = beanList.get(j);
			for (int i = 0; i < titleArr.length; i++) {
				row[i] = toStr(bean.get(fieldMap.get(titleArr[i])));
			}
			//System.out.println(Arrays.asList(row));
			objArrList[j] = row;
		}
		return objArrList;
	}


	private String toStr(Object value) {
		if (value == null)
			return "";
		else
			return value.toString();
	}


	/**
	 * 在按AREA_ID排序的学校的LIST中，每个地区段的学校的下一行插入一行地区的统计。
	 *
	 * @param schoolList
	 * @param areaMap
	 * @return
	 */
	protected List<Map<String, Object>> addAreaInfo(List<Map<String, Object>> schoolList, Map<String, Map> areaMap) throws ReportExportException {
		List<Map<String, Object>> beanList = new ArrayList<Map<String, Object>>();
	/*	Optional<Map<String,Map<String,Object>>> areaCache = DataCacheUtil.getAreaNameMap();
		Optional<Map<String,Map<String,Object>>> schoolCache = DataCacheUtil.getSchNameMap();
		if(!areaCache.isPresent() || !schoolCache.isPresent()){
//			LoggerUtil.logAndThrowException(this.logger, ReportExportException.class,"镇区或学校缓存为空！");
			ReportExportException err = new ReportExportException("镇区或学校缓存为空！");
			logger.error("",err);
			throw err;
		}
		Map<String,Map<String,Object>> areaNameMap = areaCache.get();
		Map<String,Map<String,Object>> schNameMap = getSchoolCache();*/
//		BaseDataService baseDataService = (BaseDataService)SpringUtil.getSpringBean(null,"baseDataService");
		//在每个镇区的学校末加上一行统计镇区数据
		String areaId_buf = null;
		String areaId_cur = null;
		for (Map sch : schoolList) {
//		Map schInfo = schNameMap.get(sch.get("SCH_ID").toString());
//		Map schInfo = baseDataService.getSchool(exambatchId, sch.get("SCH_ID").toString());
//		sch.put("SCHNAME", schInfo==null?"匿名学校":schInfo.get("SCHNAME"));
			areaId_cur = (String) sch.get("AREA_ID");
			if (areaId_cur == null) {
				if (areaId_buf == null) {
					beanList.add(sch);
				} else {
					Map area = areaMap.get(areaId_buf);
//				Map areaInfo = areaNameMap.get(areaId_buf);
//				Map areaInfo = baseDataService.getArea(exambatchId, areaId_buf);
//				area.put("SCHNAME", areaInfo==null?"市直":areaInfo.get("AREANAME"));
					beanList.add(area);
					beanList.add(sch);
					areaId_buf = areaId_cur;
				}
			} else if (areaId_cur.equals(areaId_buf)) {
				beanList.add(sch);
			} else if (areaId_buf == null) {
				beanList.add(sch);
				areaId_buf = areaId_cur;
			} else {
				Map area = areaMap.get(areaId_buf);
//			Map areaInfo = areaNameMap.get(areaId_buf);
//			Map areaInfo = baseDataService.getArea(exambatchId, areaId_buf);
//			area.put("SCHNAME", areaInfo==null?"市直":areaInfo.get("AREANAME"));
				area.put("SCH_NAME", area.get("AREA_NAME"));
				beanList.add(area);
				beanList.add(sch);
				areaId_buf = areaId_cur;
			}
		}
		//加上最后一个地区
		if (areaId_cur != null)
			if (areaId_cur.equals(areaId_buf)) {
				Map area = areaMap.get(areaId_buf);
//			Map areaInfo = areaNameMap.get(areaId_buf);
//			Map areaInfo = baseDataService.getArea(exambatchId, areaId_buf);
//			area.put("SCHNAME", areaInfo==null?"市直":areaInfo.get("AREANAME"));
				area.put("SCH_NAME", area.get("AREA_NAME"));
				beanList.add(area);
			}
		return beanList;
	}

	/**
	 * 将学校的LIST和地区的LIST和在一起，地区加在相同学校区间的下一行
	 *
	 * @param schoolList
	 * @param allarea
	 * @return
	 */
	protected List getBeanList(List<Map<String, Object>> schoolList, List<Map<String, Object>> allarea) throws ReportExportException {
		Map<String, Map> areaMap = new HashMap<String, Map>();
		for (Map area : allarea) {
			String areaId = (String) area.get("AREA_ID");
			areaMap.put(areaId, area);
		}
		return addAreaInfo(schoolList, areaMap);
	}

	/**
	 * 让Map中对应key的value自增，限数字
	 *
	 * @param count
	 * @param key
	 */
	protected void getValueIncrease(Map count, String key) {
		try {
			int value = (Integer) count.get(key);
			value = value + 1;
			count.put(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("字段自增失败，请检查该字段是否为数字");
		}
	}

	public static void main(String[] args) {

	}

	public void initParam(Map<String, Object> param) {
		paramLocal.get().initParam(param);
	}

	public static RptParam p() {
		return paramLocal.get();
	}

	/**
	 * 获取 文理 分科的 sheet 名称
	 * @param type
	 * @param sheetName
	 * @return
	 */
	protected static String getWenLiSheetName(EnmSubjectType type, String sheetName) {
		if (type.equals(EnmSubjectType.ALL)) {
			return sheetName;
		} else
			return type.getName();
	}

	/**
	 * 获取 文理 分科的 Field 名称
	 * @param type
	 * @param sheetName
	 * @return
	 */
	protected static String getWenLiFieldName(EnmSubjectType type, String sheetName) {
		if (type.equals(EnmSubjectType.ALL)) {
			return sheetName;
		} else
			return sheetName + "(" + type.getName() + ")";
	}

	private static ThreadLocal<RptParam> paramLocal = new ThreadLocal<RptParam>() {
		@Override
		protected RptParam initialValue() {
			return new RptParam();
		}
	};
}
