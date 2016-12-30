package zyj.report.common.excel.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExcelUtil {

	public static final String MAP_KEY="excel_key";
	public static final String MAP_VALUE="bean_value";
	
	private static Map<String,SimpleDateFormat> dateFormat = new HashMap<String,SimpleDateFormat>();

	public static Map reverseMap(Map oldMap){
	    Map newMap = new HashMap();
	    for (Iterator it = oldMap.entrySet().iterator(); it.hasNext(); ) {
	      Map.Entry entry = (Map.Entry)it.next();
	      newMap.put(entry.getValue(), entry.getKey());
	    }
	    return newMap;
	}
	 
	
	public static Date parseCellToDate(String context,String format){
		Date t = null;
		try{
			if(format != null && !"".equals(format) && context != null && !"".equals(context)){
				SimpleDateFormat f = dateFormat.get(format);
				if(f == null){
					f = new SimpleDateFormat(format);
					dateFormat.put(format, f);
				}
				t = f.parse(context.trim());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return t;
	}
	
	public static String parseCamelCase(String propertyName){
		if(propertyName == null){
			return null;
		}
		char[] f = propertyName.toCharArray();
		char[] t = new char[f.length*2];
		int i = 0;
		for(char c : f){
			if(c >= 65 && c<=90){
				t[i++] = '_';
				t[i++] = c;
			} else if(c >= 97 && c<=122){
				t[i++] = (char) (c - 32);
			}else{
				t[i++] = c;
			}
		}
		return String.valueOf(t, 0, i);
		
	}
}