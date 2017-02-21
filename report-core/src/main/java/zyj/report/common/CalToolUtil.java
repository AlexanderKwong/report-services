package zyj.report.common;

import com.alibaba.druid.sql.visitor.functions.Substring;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class CalToolUtil {
	
	public static double round(double num , int scale){
		BigDecimal f=new BigDecimal(num);
		 double a = f.setScale(scale,   RoundingMode.HALF_UP).doubleValue();
		 return a;
	}
	
	/**
	 * 去除列表的全零列，从第index列开始算起
	 * @param conList
	 * @param index
	 */
	public static void removeAllZeroColumn(List<List<Object>> conList, int index){
		int res = conList.get(0).size();
		for(List con : conList){
			for(int i = index;i < con.size();i++) {
				Object o =con.get(i);
				if(o == null)
					continue;
				if(!o.toString().trim().equals("0")&&i<res){
					res = i;
					break;
				}
			}
		}
		for(List con : conList)
			for(int i = 0; i<res-index;i++)
				con.remove(index);
	}
	public static void removeAllZeroRow(List<List<Object>> conList, int index){
		List<List<Object>> tmp = new ArrayList<List<Object>>();
			for(List con : conList){
				boolean indexIsFound = false;
				for(int i = index;i<con.size();i++){
					Object one = con.get(i);
					if(one == null)
						continue;
					if(!one.toString().trim().equals("0")){
						indexIsFound = true;
						break;						
					}
				}
				if(!indexIsFound)
					tmp.add(con);
				else
					break;
			}
		conList.removeAll(tmp);
	}
	public static void replace0(List<List<Object>> conList,int index){
		for(List con : conList){
			for(int i = index;i<con.size();i++){
				Object one = con.get(i);
				if(one == null)
					continue;
				if(one.toString().trim().equals("0")){
					one = "";
					con.set(i, one);
				}
			}
		}
	}
	public static void replace0(List<Map<String,Object>> conList,String startWith){
		for(Map con : conList){
			Set<Map.Entry<String,Object>> es = con.entrySet();
			for (Map.Entry<String,Object> entry:es) { 
				if(entry.getKey().startsWith(startWith)){
					Object o =entry.getValue();
					if(o == null)
						continue;
					if(o.toString().trim().equals("0")){
						o = "";
						con.put(entry.getKey(), o);
					}
				}
			}
		}
	}
	
	public static Map<String,Map<String,Object>> trans(List<Map<String,Object>> d, String[] key){
		Map<String,Map<String,Object>> map = new HashMap<String,Map<String,Object>>();
		if(d != null){
			for(Map<String,Object> obj : d){
				String k = "";
				for(String t : key){
					k = k + ObjectUtils.toString(obj.get(t));
				}
				map.put(k, obj);
			}
		}
		return map;
	}
	public static Map<String,Map<String,Object>> trans(List<Map<String,Object>> d, String[] key,String seperator){
		if(StringUtils.isBlank(seperator)) return trans(d,key);
		Map<String,Map<String,Object>> map = new HashMap<String,Map<String,Object>>();
		if(d != null){
			for(Map<String,Object> obj : d){
				String k = "";
				for(String t : key){
					k = k + ObjectUtils.toString(obj.get(t))+seperator;
				}
				map.put(k, obj);
			}
		}
		return map;
	}
	/**
	 * 横向求和
	 * @param d
	 * @param sumkey
	 * @param reskey
	 */
	@SuppressWarnings("unchecked")
	public static void sum(List d,final String[] sumkey,String reskey){
		for(Object obj : d){
			Map<String,Object> m = (Map<String,Object>)obj;
			double sum = 0;
			for(String t : sumkey){
				sum += Double.parseDouble(m.get(t).toString());
			}
			m.put(reskey, sum);
		}
	}
	/**
	 * 纵向求平均、最大、最小
	 * @param d
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map maxIndexOfList(List d,final int index){
		Map statistic = new HashMap<String, String>();
		double sum = 0.0;
		double max = 0.0;
		double min  = 999.0;
		double  score = 0.0;
		for(Object obj : d){
			List<Object> m = (List<Object>)obj;
			try{
			score = Double.parseDouble(m.get(index).toString());
			}catch(Exception e){
				score = 0;
			}
			sum += score;
			if(score > max)
				max = score;
			if(score < min)
				min = score;
		}
		statistic.put("avg", decimalFormat2(sum/d.size()));
		statistic.put("max", decimalFormat2(max));
		statistic.put("min", decimalFormat2(min));
		statistic.put("sum", decimalFormat2(sum));
		return statistic;
	}
	
	@SuppressWarnings("unchecked")
	public static Integer[] indexSordOfList(List<List<Object>> d,int index ){
		if(d!=null){
			Double[] aa = new Double[d.size()];
			Integer[] aabackup= new Integer[d.size()];
			for(int i = 0;i<d.size();i++){
				List dd = (List)d.get(i);
				aa[i] = Double.parseDouble(dd.get(index).toString());
			}
			
			for(int i =1; i<=aa.length;){
				int[] b = findMax(aa);
				for(int j :b){
						aabackup[j] = i;
					aa[j] = null;
				}
				i=i+b.length;
			}
			return aabackup;
		}
		return null;
	}
	private static int[] findMax(Double [] arr){
		int [] maxIndex = new int[arr.length];
		if(arr.length==1)
			return new int[]{0};
		double max = 0.0;
		for(int i =0;i<arr.length;i++){
			if(arr[i]!=null)
				max = Math.max(max, arr[i]);			
		}
		int j=0;
		for(int i =0;i<arr.length;i++){
			if(arr[i]==null)
				continue;
			if(max == arr[i]){
				maxIndex[j] = i;
				j++;
			}
		}
		maxIndex = Arrays.copyOf(maxIndex, j);
		return maxIndex;
	}
	/**
	 * Map.get(key)的String值按指定String数组来排序
	 * @param d
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public static void sortByValue(List<Map<String,Object>> d, final String key, final Object[] value){
		if(d != null){
			Collections.sort(d,new Comparator<Object>() {
				public int compare(Object o1,Object o2) {
					Map<String,Object> m1 = (Map<String,Object>)o1;
					Map<String,Object> m2 = (Map<String,Object>)o2;
					Object k1 = m1.get(key);
					Object k2 = m2.get(key);
					int i1 = indexOf(value, k1);
					int i2 = indexOf(value, k2);
					return i1-i2;
				}
			});
		}
	}
	/**
	 * 按指定字符串排序
	 * @param d
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public static void sortByValue(List<String> d, final Object[] value){
		if(d != null){
			Collections.sort(d,new Comparator<Object>() {
				public int compare(Object o1,Object o2) {
					String m1 = (String)o1;
					String m2 = (String)o2;
					int i1 = indexOf(value, m1);
					int i2 = indexOf(value, m2);
					return i1-i2;
				}
			});
		}
	}


	/**
	 * 按List.get(index)的double值来排序
	 * @param d
	 * @param index
	 */
	@SuppressWarnings("unchecked")
	public static void sortByIndexValueNa(List<List<Object>> d ,final int index){
		if(d!= null){
			Collections.sort(d,new Comparator<Object>() {
				public int compare(Object o1,Object o2) {
					List<Object> m1 = (List<Object>)o1;
					List<Object> m2 = (List<Object>)o2;
					double k1 = Double.parseDouble(m1.get(index).toString());
					double k2 = Double.parseDouble(m2.get(index).toString());
					return (int)((k1-k2)*10000);//将其放大一万倍判断是否相等
				}
			});
		}
	}

	/**
	 * 按Map.get(key)的String值来排序
	 * @param d
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public static void sortByIndexValue(List<Map<String,Object>> d ,final String key){
		if(d!= null){
			Collections.sort(d,new Comparator<Object>() {
				public int compare(Object o1,Object o2) {
					Map<String,Object> m1 = (Map<String,Object>)o1;
					Map<String,Object> m2 = (Map<String,Object>)o2;
					String k1 = m1.get(key).toString();
					String k2 = m2.get(key).toString();
					return k1.compareTo(k2);
				}
			});
		}
	}
	/**
	 * 按Map.get(key)的String值来排序
	 * @param d
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public static void sortByIndexValue(List<Map<String,Object>> d ,final String[] keys){
		if(d!= null){
			Collections.sort(d,new Comparator<Object>() {
				public int compare(Object o1,Object o2) {
					Map<String,Object> m1 = (Map<String,Object>)o1;
					Map<String,Object> m2 = (Map<String,Object>)o2;
					String k1 = "";
					String k2 = "";
					for(int i = 0; i < keys.length;i++){
						k1 += m1.get(keys[i]).toString();
						k2 += m2.get(keys[i]).toString();
						
					}
					return k1.compareTo(k2);
				}
			});
		}
	}
/**
 * 按Map.get(key)的int值来排序
 * @param d
 * @param key
 */
	@SuppressWarnings("unchecked")
	public static void sortByIndexValue2(List<Map<String,Object>> d ,final String key){
		if(d!= null){
			Collections.sort(d,new Comparator<Object>() {
				public int compare(Object o1,Object o2) {
					Map<String,Object> m1 = (Map<String,Object>)o1;
					Map<String,Object> m2 = (Map<String,Object>)o2;
					int k1 = Integer.parseInt(m1.get(key).toString());
					int k2 = Integer.parseInt(m2.get(key).toString());
					return k1-k2;
				}
			});
		}
	}

	//根据该单元的String值大小排序
	@SuppressWarnings("unchecked")
	public static void sortByIndexValue2(List<List<Object>> d ,final int index){
		if(d!= null){
			Collections.sort(d,new Comparator<Object>() {
				public int compare(Object o1,Object o2) {
					List<Object> m1 = (List<Object>)o1;
					List<Object> m2 = (List<Object>)o2;
					String k1 = m1.get(index).toString();
					String k2 = m2.get(index).toString();
					return k2.compareTo(k1);
				}
			});
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void sort(List d,final String sortkey,String reskey){
		Collections.sort(d,new Comparator<Object>() {
			public int compare(Object o1,Object o2) {
				Map<String,Object> m1 = (Map<String,Object>)o1;
				Map<String,Object> m2 = (Map<String,Object>)o2;
				double d1 = Double.parseDouble(m1.get(sortkey).toString());
				double d2 = Double.parseDouble(m2.get(sortkey).toString());
				if(d1 > d2){
					return -1;
				}
				if(d1 == d2){
					return 0;
				}
				return 1;
			}
		});
		double d1 = Double.MIN_VALUE;
		int i = 1;
		int j = 1;
		for(Object obj : d){
			Map<String,Object> m = (Map<String,Object>)obj;
			double d2 = Double.parseDouble(m.get(sortkey).toString());
			if(d1 != d2){
				m.put(reskey, j);
				i = j;
			}else{
				m.put(reskey, i);
			}
			j++;
			d1 = d2;
		}
	}
	
	public static int indexOf(Object[] f, Object o){
		if(o != null){
			for(int i=0;i<f.length;i++){
				if(f[i].toString().equals(o.toString())){
					return i;
				}
			}
		}
		return -1;
	}

	static String[] redundantSubject = new String[]{"WYW_S","WYY_S","WSX_S","LYW_S","LYY_S","LSX_S"}; 
/**
 * 获取应该过滤的科目
 */
	public static String [] getRedundantSubject(){
		return redundantSubject;
	}
	static String[] combination1 = new String[]{
		"A","B","C","D","E",
		"AB","AC","AD","AE","BC","BD","BE","CD","CE","DE",
		"ABC","ABD","ACD","ADE","BCD","BDE","CDE",
		"ABCD"};
	static String[] combination2 = new String[]{
		"A","B","C","D","E",
		"A,B","A,C","A,D","A,E","B,C","B,D","B,E","C,D","C,E","D,E",
		"A,B,C","A,B,D","A,C,D","A,D,E","B,C,D","B,D,E","C,D,E",
		"A,B,C,D"};
	public static String[] getAllCombination(int model) {
		if(model == 1){
			return combination1;
		}
		if(model == 2){
			return combination2;
		}
		return null;
	}
	private  static final String[] orderToSort = new String[]{"WYW_S","LYW_S","YW","WSX","LSX","WSX_S","LSX_S","SX","YY_N_L"/*英语非听力*/,"WYY_S","LYY_S","YY","DL","LS","ZZ","SW","HX","WL","S_ZH_DL","S_ZH_LS","S_ZH_ZS","S_ZH_SW","S_ZH_HX","S_ZH_WL","WZ","LZ","WK","LK","ZF"}; 
	public  static String[] getSubjectOrder(){
		return orderToSort;
	}
	
	private static String[][] scoreLine = new String[][] {
			{ "LYW_S", "102", "94", "83", "62" },
			{ "LSX_S", "124", "108", "84.0", "35" },
			{ "LYY_S", "87", "72", "52", "23" },
			{ "WL", "84", "73", "59", "31" },
			{ "HX", "80", "71", "55.0", "32" },
			{ "SW", "88", "80", "68.0", "35" },
			{ "WYW_S", "109", "104", "95.0", "80" },
			{ "WSX_S", "102", "92", "76", "42" },
			{ "WYY_S", "93", "85", "75.0", "44" },
			{ "DL", "55", "55", "48.0", "31" },
			{ "ZZ", "59", "60", "52", "33" },
			{ "LS", "73", "68", "61.0", "42" },
			{ "WZ", "109", "104", "95.0", "80" },
			{ "LZ", "109", "104", "95.0", "80" },
			{ "LK", "562", "497", "397.5", "207" },
			{ "WK", "490", "453.5", "405.5", "262" },
			{ "ZF", "0", "0", "0", "0" } };
	
	public static  synchronized void setSubjectScoreLine(String[][] scoreLine_cur){
		scoreLine = scoreLine_cur;
	}
	
	/**
	 * 获取科目分数线
	 * @param subject
	 * @return
	 */
	public static double[] getSubjectScoreLine(String subject){
		double[] scoreline = null;
		for(String[] sub : scoreLine){
			if(sub[0].equals(subject)){
				scoreline = new double[scoreLine[0].length-1];
				for(int i =1;i < sub.length;i++){
					scoreline[i-1] = Double.parseDouble(sub[i]);
				}
				break;
			}
		}
		return scoreline;
	}
	private static String[][] rankLine = new String[][] {
		{ "LYW_S", "15", "110", "900", "2300" },
		{ "LSX", "15", "110", "900", "2300"  },
		{ "LYY_S", "15", "110", "900", "2300"  },
		{ "S_ZH_WL", "15", "110", "900", "2300"   },
		{ "S_ZH_HX", "15", "110", "900", "2300"  },
		{ "S_ZH_SW","15", "110", "900", "2300"  },
		{ "WYW_S", "10", "55", "350", "1650" },
		{ "WSX","10", "55", "350", "1650"},
		{ "WYY_S", "10", "55", "350", "1650" },
		{ "S_ZH_DL", "10", "55", "350", "1650" },
		{ "S_ZH_ZS", "10", "55", "350", "1650" },
		{ "S_ZH_LS", "10", "55", "350", "1650"},
		{ "WZ", "10", "55", "350", "1650" },
		{ "LZ", "15", "110", "900", "2300"},
		{ "LK", "9", "88", "668", "2345" },
		{ "WK", "5", "53", "244", "1549" },
		{ "ZF", "0", "0", "0", "0" } };
	public static  synchronized void setSubjectRankLine(String[][] rankLine_cur){
		rankLine = rankLine_cur;
	}
	/**
	 * 获取科目名次段分数线
	 * @param subject
	 * @return
	 */
	public static int[] getSubjectRankLine(String subject){
		int[] rankline = null;
		for(String[] sub : rankLine){
			if(sub[0].equals(subject)){
				rankline = new int[rankLine[0].length-1];
				for(int i =1;i < sub.length;i++){
					rankline[i-1] = Integer.parseInt(sub[i]);
				}
				break;
			}
		}
		return rankline;
	}
	private static String join(String t, String split){
		char[] cArray = t.toCharArray();
		t = "";
		for(char c : cArray){
			t = t + c + split;
		}
		return t.substring(0,t.length()-1);
	}

	public static String decimalFormat2(double a){
		//格式化小数，保留两位
			DecimalFormat formater = new DecimalFormat();
			formater.setMaximumFractionDigits(2);
			formater.setGroupingSize(0);
			formater.setRoundingMode(RoundingMode.HALF_UP);
			return formater.format(a);
	}

	public static String decimalFormat4(double a){
		//格式化小数，保留两位
			DecimalFormat formater = new DecimalFormat();
			formater.setMaximumFractionDigits(4);
			formater.setGroupingSize(0);
			formater.setRoundingMode(RoundingMode.HALF_UP);
			return formater.format(a);
	}
	
	public static void main(String[] args) {
		List<Map<String,Object>> d = new LinkedList<Map<String,Object>>();
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("a", 1.1);
		m.put("c", "A");
		d.add(m);
		Map<String,Object> m1 = new HashMap<String,Object>();
		m1.put("a", 1);
		m1.put("c", "B");
		d.add(m1);
		Map<String,Object> m2= new HashMap<String,Object>();
		m2.put("a", 1.3);
		m2.put("c", "C");
		d.add(m2);
		Map<String,Object> m3= new HashMap<String,Object>();
		m3.put("a", 1.1);
		m3.put("c", "D");
		d.add(m3);
		Map<String,Object> m5= new HashMap<String,Object>();
		m5.put("a", 1.1);
		m5.put("c", "E");
		d.add(m5);
		Map<String,Object> m6= new HashMap<String,Object>();
		m6.put("a", 1.3);
		m6.put("c", "F");
		d.add(m6);
		Map<String,Object> m7= new HashMap<String,Object>();
		m7.put("a", 1.7);
		m7.put("c", "G");
		d.add(m7);
		CalToolUtil.sortByValue(d, "c", new String[]{"F", "E", "G"});
		for(Map<String,Object> t : d){
			System.out.println(t);
		}
		t();
		System.out.println(95/5*5);
	}
	
	static void t(){
		String t = "分数段,人数,比例,累计人数,累计比例,文科人数,文科比例,文科累计人数,文科累计比例,理科人数,理科比例,理科累计人数,理科累计比例";
		String[] p = t.split(",");
		for(int i=0;i<p.length;i++){
			System.out.println("		<property name=\"\"  column=\""+(i+1)+"\"  excelTitleName=\""+p[i]+"\"  dataType=\"String\" />");
		}
	}

}
