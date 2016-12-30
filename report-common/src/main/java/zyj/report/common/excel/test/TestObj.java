package zyj.report.common.excel.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class TestObj {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String,String> m = new HashMap<String,String>();
		m.put("mN", "qwe");
		BeanWrapper bw = new BeanWrapperImpl(m);
		Object propertyValue =  bw.getPropertyValue("[\"mN\"]");
		System.out.println(propertyValue);
	}

}
