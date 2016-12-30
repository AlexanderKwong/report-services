package zyj.report.common.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.SocketException;
import java.util.Properties;


public class FtpUtil {
	
    
    /** 
     * 获取FTPClient对象 
     * @param ftpHost FTP主机服务器 
     * @param ftpPassword FTP 登录密码 
     * @param ftpUserName FTP登录用户名 
     * @param ftpPort FTP端口 默认为21 
     * @return 
     */  
    public static FTPClient getFTPClient(String ftpHost, String ftpPassword,  
            String ftpUserName, int ftpPort) {  
        FTPClient ftpClient = null;  
        try {  
            ftpClient = new FTPClient();  
//            ftpClient.setControlEncoding("GBK");
//            ftpClient.setConnectTimeout(60000);
            ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器  
            ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器  
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {  
            	System.out.println("未连接到FTP，用户名或密码错误。");  
                ftpClient.disconnect();  
            } else {  
            	System.out.println("FTP连接成功。");  
            }  
        } catch (SocketException e) {  
            e.printStackTrace();  
            System.err.println("FTP的IP地址可能错误，请正确配置。");  
        } catch (IOException e) {  
            e.printStackTrace();  
            System.err.println("FTP的端口错误,请正确配置。");  
        }  
        return ftpClient;  
    }  
  
    /** 
     * 去 服务器的FTP路径下上读取文件 
     *  
     * @param ftpUserName 
     * @param ftpPassword 
     * @param ftpPath 
     * @param
     * @return 
     */  
    public static String readConfigFileForFTP(String ftpUserName, String ftpPassword,  
            String ftpPath, String ftpHost, int ftpPort, String fileName) {  
        StringBuffer resultBuffer = new StringBuffer();  
        FileInputStream inFile = null;  
        InputStream in = null;  
        FTPClient ftpClient = null;
        System.out.println("开始读取绝对路径" + ftpPath + "文件!");
        try {  
            ftpClient = getFTPClient(ftpHost, ftpPassword, ftpUserName,  
                    ftpPort);  
            ftpClient.setControlEncoding("UTF-8"); // 中文支持  
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);  
            ftpClient.enterLocalPassiveMode();  
            ftpClient.changeWorkingDirectory(ftpPath);  
            in = ftpClient.retrieveFileStream(fileName);  
        } catch (FileNotFoundException e) {
            System.out.println("没有找到" + ftpPath + "文件");
            e.printStackTrace();  
            return "下载配置文件失败，请联系管理员.";  
        } catch (SocketException e) {
            System.out.println("连接FTP失败.");
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();
            System.out.println("文件读取错误。");
            e.printStackTrace();  
            return "配置文件读取失败，请联系管理员.";  
        }  
        if (in != null) {  
            BufferedReader br = new BufferedReader(new InputStreamReader(in));  
            String data = null;  
            try {  
                while ((data = br.readLine()) != null) {  
                    resultBuffer.append(data + "\n");  
                }  
            } catch (IOException e) {
                System.out.println("文件读取错误。");
                e.printStackTrace();  
                return "配置文件读取失败，请联系管理员.";  
            }finally{  
                try {  
                    ftpClient.disconnect();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }else{
            System.out.println("in为空，不能读取。");
            return "配置文件读取失败，请联系管理员.";  
        }  
        return resultBuffer.toString();  
    }  
    /** 
     * 本地上传文件到FTP服务器 
     *  
     * @param ftpPath 
     *            远程文件路径FTP 
     * @throws IOException 
     */  
    public static void upload(String ftpPath, String ftpUserName, String ftpPassword,  
            String ftpHost, int ftpPort, String fileContent,  
            String writeTempFielPath) {  
        FTPClient ftpClient = null;
        System.out.println("开始上传文件到FTP.");
        try {  
            ftpClient = getFTPClient(ftpHost, ftpPassword,  
                    ftpUserName, ftpPort);  
            // 设置PassiveMode传输  
            ftpClient.enterLocalPassiveMode();  
            // 设置以二进制流的方式传输  
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);  
            // 对远程目录的处理  
            String remoteFileName = ftpPath;  
            if (ftpPath.contains("/")) {  
                remoteFileName = ftpPath  
                        .substring(ftpPath.lastIndexOf("/") + 1);  
            }  
            // FTPFile[] files = ftpClient.listFiles(new  
            // String(remoteFileName));  
            // 先把文件写在本地。在上传到FTP上最后在删除  
            boolean writeResult = write(remoteFileName, fileContent,  
                    writeTempFielPath);  
            if (writeResult) {  
                File f = new File(writeTempFielPath + "/" + remoteFileName);  
                InputStream in = new FileInputStream(f);  
                ftpClient.storeFile(remoteFileName, in);  
                in.close();
                System.out.println("上传文件" + remoteFileName + "到FTP成功!");
                f.delete();  
            } else {
                System.out.println("写文件失败!");
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally{  
            try {  
                ftpClient.disconnect();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
  
    /** 
     * 把配置文件先写到本地的一个文件中取 
     *  
     * @param
     * @param
     * @return 
     */  
    public static boolean write(String fileName, String fileContext,  
            String writeTempFielPath) {  
        try {
            System.out.println("开始写配置文件");
            File f = new File(writeTempFielPath + "/" + fileName);  
            if(!f.exists()){  
                if(!f.createNewFile()){
                    System.out.println("文件不存在，创建失败!");
                }  
            }  
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));  
            bw.write(fileContext.replaceAll("\n", "\r\n"));  
            bw.flush();  
            bw.close();  
            return true;  
        } catch (Exception e) {
            System.out.println("写文件没有成功");
            e.printStackTrace();  
            return false;  
        }  
    }  
    
    
    public static void main(String[] args) {  
        int ftpPort = 0;  
        String ftpUserName = "";  
        String ftpPassword = "";  
        String ftpHost = "";  
        String ftpPath = "";  
        String writeTempFielPath = "";  
        try {  
            InputStream in = FtpUtil.class.getClassLoader()  
                    .getResourceAsStream("env.properties");  
            if (in == null) {
                System.out.println("配置文件env.properties读取失败");
            } else {  
                Properties properties = new Properties();  
                properties.load(in);  
                ftpUserName = properties.getProperty("ftpUserName");  
                ftpPassword = properties.getProperty("ftpPassword");  
                ftpHost = properties.getProperty("ftpHost");  
                ftpPort = Integer.valueOf(properties.getProperty("ftpPort"))  
                        .intValue();  
                ftpPath = properties.getProperty("ftpPath");  
//                writeTempFielPath = properties.getProperty("writeTempFielPath");  
                  

         /*       String result = FtpUtil.readConfigFileForFTP(ftpUserName, ftpPassword, ftpPath, ftpHost, ftpPort, "huawei_220.248.192.200.cfg");  
                System.out.println("读取配置文件结果为：" + result);  
                  

                ftpPath = ftpPath + "/" + "huawei_220.248.192.200_new1.cfg";  
                FtpUtil.upload(ftpPath, ftpUserName, ftpPassword, ftpHost, ftpPort, result, writeTempFielPath);  
          */
                in.close();  
                FTPClient  ftpClient = getFTPClient(ftpHost, ftpPassword,  
                        ftpUserName, ftpPort);  

                // 设置PassiveMode传输  
                ftpClient.enterLocalPassiveMode();  
                // 设置以二进制流的方式传输  
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);  
                File f = new File("D:/testxls/zskcs_beta5/examrpt_3a10296a-3e69-4f91-8d2e-7cba33b97fc6.zip");  
                InputStream input = new FileInputStream(f);  
              boolean a =   ftpClient.storeFile("examrpt_3a10296a-3e69-4f91-8d2e-7cba33b97fc6.zip", input);
              System.out.println(a);
                input.close();  
                System.out.println("上传文件" +"examrpt_3a10296a-3e69-4f91-8d2e-7cba33b97fc6.zip" + "到FTP成功!");  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}
