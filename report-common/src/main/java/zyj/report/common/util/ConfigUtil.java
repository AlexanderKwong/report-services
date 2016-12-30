package zyj.report.common.util;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import zyj.report.common.excel.config.impl.ExcelConfigManagerImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {

	public static  Properties readPath(String configName) throws FileNotFoundException, IOException{

		InputStream in = null;
		in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(configName);
		if(in == null){
			in = ConfigUtil.class.getClassLoader().getResourceAsStream(configName);
		}
		if(in == null){
			in = ConfigUtil.class.getResourceAsStream(configName);
		}
		if(in == null){
			in = new PathMatchingResourcePatternResolver().getResource("classpath:"+configName).getInputStream();
		}
		Properties properties = new Properties();
		properties.load(in);
		in.close();
		return properties;
	}
	
	public static void main(String[] args) {
	     Properties properties = null;
		try {
			properties = ConfigUtil.readPath("env.properties");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

         String ftpUserName = properties.getProperty("ftpUserName");  
         String ftpPassword = properties.getProperty("ftpPassword");  
         String ftpHost = properties.getProperty("ftpHost");  
         int ftpPort = Integer.valueOf(properties.getProperty("ftpPort"))  
                 .intValue();  
         
         System.out.println(ftpUserName);
         System.out.println(ftpPassword);
         System.out.println(ftpHost);
         System.out.println(ftpPort);
	}
}
