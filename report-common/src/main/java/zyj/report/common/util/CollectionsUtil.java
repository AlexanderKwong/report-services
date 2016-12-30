package zyj.report.common.util;

import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;

import java.util.*;

public class CollectionsUtil {
	
	@SuppressWarnings("rawtypes")
	public static Map parserToMap(String s){  
	    Map map=new HashMap();  
	    JSONObject json=JSONObject.fromObject(s);  
	    Iterator keys=json.keys();  
	    while(keys.hasNext()){  
	        String key=(String) keys.next();  
	        String value=json.get(key).toString();  
	        if(value.startsWith("{")&&value.endsWith("}")){  
	            map.put(key, parserToMap(value));  
	        }else{  
	            map.put(key, value);  
	        }  
	  
	    }  
	    return map;  
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

	public static List<Map<String,Object>> leftjoinMapByKey( List<Map<String,Object>> a, List<Map<String,Object>> b,String key){
		Map<String,Map<String,Object>> aDic= trans(a, new String[]{key});
		Map<String,Map<String,Object>> bDic= trans(b, new String[]{key});
		List<Map<String,Object>> res = new ArrayList<Map<String,Object>>();
		for (Map.Entry<String,Map<String,Object>> entry:aDic.entrySet()) { 
			String k =entry.getKey();
			Map<String,Object> m = new HashMap<String, Object>(entry.getValue());
			try{
			Map<String,Object> n =new HashMap<String, Object>(bDic.get(k));
			m.putAll(n);
			}catch(Exception e){
				System.out.println("Warn : b表中没有key为 "+k+"的字段");
			}
			res.add(m);
		} 
		return res;
	}

	public static boolean containsKey(List<Map<String,Object>> l,String key,String value){
		for(Map<String,Object> m : l){
			if(m.containsKey(key)){
				return m.get(key).equals(value);
			}
		}
		return false;
	}

	public static  List<Map<String,Object>> groupby( List<Map<String,Object>> old,String[] keys ,Map<String,Object> otherParams){
		Map<String,Map<String,Object>> map = trans(old,keys);
		List<Map<String,Object>> new_map = (List)map.values();
		if(otherParams!=null)
		for(Map m : new_map){
			m.putAll(otherParams);
		}
		return new_map;
	}
}
