package zyj.report.service.export;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zyj.report.common.DataCacheUtil;
import zyj.report.common.ExportUtil;
import zyj.report.persistence.client.RptExpStudetScoreMapper;

@Service
public class ExtZffsdXGServer extends BaseRptService {
	
	@Autowired
	RptExpStudetScoreMapper rptExpStudetScoreMapper;
	
	private int fullScore ;
	private int lower ;//下界
	private int step =5;

	public void exportData(Map<String,Object> parmter) throws Exception{
		String exambatchId = parmter.get("exambatchId").toString();
		String subject = ObjectUtils.toString(parmter.get("subject"));
		String pathFile = parmter.get("pathFile").toString();
		// 获取学校地区名		
		
//		Map<String,Map<String,Object>> schNameMap = getSchoolCache();
		if("WK".equals(subject)){
			parmter.put("flag", 1);
		}
		if("LK".equals(subject)){
			parmter.put("flag", 2);
		}
		if("NWL".equals(subject)){
			parmter.put("flag", 0);
		}
		//读取源数据
		List<Map<String,Object>> data = rptExpStudetScoreMapper.findRptExpStudetAllScore(parmter);
		
		String[] titleList = new String[256];
		Map<String ,Integer> titleMap = new HashMap<String ,Integer>();
		titleList[0] = "学校";
		titleList[1] = "人数";
		titleList[2] = "均分";
		titleList[3] = "排名";
		titleList[4] = "最高分";
		int length = 5;
		//获取总分
		fullScore = Integer.parseInt(data.get(0).get("FULL_SCORE").toString());
		lower = fullScore-(fullScore%step==0?step:fullScore%step);
		
		for(int i = lower,j=5;i>=0;i-=step,j++,length++){
			titleList[j] = getFsdKey(i);
			titleMap.put(getFsdKey(i), j);
		}
		titleList = Arrays.copyOf(titleList, length);
		
		//数据列
		List<List<Object>> conList = new LinkedList<List<Object>>();
		Map<String, List<Object>> conListMap = new HashMap<String, List<Object>>();//建立行数据的索引
		
		//totalScore 总分,total,maxTotalScore,sort 计算总分等用
		Map<String, Map<String,Object>> schQuotaMap = new HashMap<String,Map<String,Object>>();
		
		for(Map<String,Object> m : data){
			String schid = m.get("SCH_ID").toString();
			String schName = m.get("SCH_NAME").toString();
			//每一个学校只输出一行数据
			if(!conListMap.containsKey(schName)){
				//行数据初始化
				List<Object> resdatalist= new LinkedList<Object>();
				resdatalist.add(schName);//TODO 从缓存读取学校名字
				for(String title : titleList){
					resdatalist.add(0);
				}
				conList.add(resdatalist);
				conListMap.put(schName, resdatalist);
				Map<String,Object> quotamap = new HashMap<String,Object>();
				quotamap.put("totalScore", 0.0);
				quotamap.put("total", 0.0);
				quotamap.put("maxTotalScore", 0.0);
				schQuotaMap.put(schName, quotamap);
			}
			//得到分数段计算入累计人数, fsd:分数段
			double score = Double.parseDouble(m.get("ALL_TOTAL").toString());
			List<Object> row = conListMap.get(schName);
			int start = ((int) score/step*step);
			start = (start>=lower?lower:start);
			for(int i=start;i>=0;i-=step){
				String fsdkey = getFsdKey((int) i);
				int totalnum = (Integer) row.get(titleMap.get(fsdkey));
				row.remove((int)titleMap.get(fsdkey));
				row.add(titleMap.get(fsdkey), totalnum+1);
			}
			Map<String,Object> quotamap = schQuotaMap.get(schName);
			quotamap.put("totalScore", (Double)quotamap.get("totalScore")+score);
			quotamap.put("total", (Double)quotamap.get("total")+1);
			quotamap.put("maxTotalScore", (Double)quotamap.get("maxTotalScore")>score?(Double)quotamap.get("maxTotalScore"):score);
			quotamap.put("averageScore", (Double)quotamap.get("totalScore")/ (Double)quotamap.get("total"));
		}
		
		zyj.report.common.CalToolUtil.sort(new LinkedList(schQuotaMap.values()), "averageScore", "ranking");
		
		DecimalFormat    dft   = new DecimalFormat("#0.00");
		
		
		for(List<Object> con : conList){
			String schName = con.get(0).toString();
			Map<String,Object> quotamap = schQuotaMap.get(schName);
			con.remove(1);
			con.add(1, (int)Double.parseDouble(quotamap.get("total").toString()));
			con.remove(2);
			con.add(2,  dft.format( quotamap.get("averageScore")));
			con.remove(3);
			con.add(3,  quotamap.get("ranking"));
			con.remove(4);
			con.add(4,  quotamap.get("maxTotalScore"));
		}
		zyj.report.common.CalToolUtil.removeAllZeroColumn(conList, 5);
		int times = titleList.length - conList.get(0).size();
		for(int i =0,j=5;i<=times;i++,j++)
			titleList[j]=null;
		String[] titleArr = new String[256];
		int i,j ;
		for( i = 0,j=0; i<titleList.length;i++){
			if(titleList[i] != null){
				titleArr[j] = titleList[i];
				j++;
			}		
		}
		titleArr = Arrays.copyOf(titleArr, j);
		//全局0置空
		zyj.report.common.CalToolUtil.replace0(conList, 5);
		ExportUtil.createExpExcel(titleArr, conList, pathFile+"总分分数段"+("NWL".equals(subject)?"":"_"+subjects.get(subject))+".xls");
		
	}
	
	private String getFsdKey(int score){
		if(score >= lower){
			return ">="+lower;
		}
		int i = score/step;
		return ">=" + (i*step);
	}
/*public static void main(String[] args) {
	List <Map < String ,Object>> a = new ArrayList<Map<String,Object>>();
	Map<String,Object> aa = new HashMap<String, Object>();
	Map<String,Object> aaa = new HashMap<String, Object>();
	aaa.put("hhh", "1");
	aa.put("haha", new HashMap<String, Object>(aaa));
	a.add(new HashMap<String, Object>(aa));
	aaa.put("hhh", "2");
	aa.put("haha", new HashMap<String, Object>(aaa));
	a.add(new HashMap<String, Object>(aa));
	aaa.put("hhh", "3");
	aa.put("haha", new HashMap<String, Object>(aaa));
	a.add(new HashMap<String, Object>(aa));
	aaa.put("hhh", "4");
	aa.put("haha", new HashMap<String, Object>(aaa));
	a.add(new HashMap<String, Object>(aa));
	System.out.println(a);
	}*/
}
