package zyj.report.common.util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipInputStream;

public class ZipUtil {
	 /**
	  * 解压指定zip到根目录
	  * @param unZipfileName
	  * @throws Exception
	  */
	public static void unZip(String unZipfileName,String dir) throws IOException {// unZipfileName需要解压的zip文件名
		FileOutputStream fileOut;
		File file;
		InputStream inputStream;
		int readedBytes;
		byte[] buf = new byte[2048];
		ZipFile zipFile = new ZipFile(unZipfileName);
		for (Enumeration entries = zipFile.getEntries(); entries
				.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
//			System.out.println(entry.getName());
			file = new File(dir+entry.getName());
			
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				// 如果指定文件的目录不存在,则创建之.
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				inputStream = zipFile.getInputStream(entry);
				fileOut = new FileOutputStream(file);
				while ((readedBytes = inputStream.read(buf)) > 0) {
					fileOut.write(buf, 0, readedBytes);
				}
				fileOut.close();
				inputStream.close();
			}
		}
		zipFile.close();

	}

	
	
	/**
	 * 将文件夹打包压缩成zip
	 * 		如zipDir("c:/test","c:/test.zip");
	 * 		打开.zip看到的是c:/test/a,c:/test/b,c:/test/c ...
	 * @param srcDIr
	 * @param outFile
	 * @throws Exception
	 */
	public static void zipDir(String srcDIr, String outFile) throws IOException {
		File inputDir = new File(srcDIr);
		FileOutputStream fos = new FileOutputStream(outFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		zos.setEncoding("UTF-8");
		zipfile(inputDir.listFiles(), "", zos);
		zos.close();
	}
	
	/**
	 * 将文件夹放在压缩文件内：
	 * 		如zipDir2("c:/test","c:/test.zip");
	 * 		打开.zip看到的文件夹是c:/test
	 * @param srcDIr
	 * @param outFile
	 * @throws Exception
	 */
	public static void zipDir2(String srcDIr, String outFile) throws Exception {
		File inputDir = new File(srcDIr);
		FileOutputStream fos = new FileOutputStream(outFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		zos.setEncoding("UTF-8");
		File[] files = new File[1];
		files[0] = inputDir;
		zipfile(files, "", zos);
		zos.close();
	}
/**
 * 将多个文件压缩
 * @param files
 * @param baseFolder
 * @param zos
 * @throws Exception
 */
	private static void zipfile(File[] files, String baseFolder, ZipOutputStream zos) throws IOException {
		byte[] buffer = new byte[2048];

		// 输入问题
		FileInputStream fis = null;
		// 压缩条目
		ZipEntry entry = null;
		// 数据长度
		int count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				// 压缩子目录
				String subFolder = baseFolder + file.getName() + File.separator;
				zipfile(file.listFiles(), subFolder, zos);
				continue;
			}
			entry = new ZipEntry(baseFolder + file.getName());
			// 加入压缩条目
			zos.putNextEntry(entry);
			fis = new FileInputStream(file);
			// 读取文件数据
			while ((count = fis.read(buffer, 0, buffer.length)) != -1)
				// 写入压缩文件
				zos.write(buffer, 0, count);
			fis.close();
		}
	}

	//从压缩包中写文件
	public static void insertZipFile(String zipfile,String srcfile,String relativePath) throws Exception {
		File inputDir = new File(srcfile);
		FileOutputStream fos = new FileOutputStream(zipfile,true);
		ZipOutputStream zos = new ZipOutputStream(fos);
		zos.setEncoding("UTF-8");
		ZipEntry entry = new ZipEntry( inputDir.getName());
		// 加入压缩条目
		zos.putNextEntry(entry);
		FileInputStream fis = new FileInputStream(inputDir);
		// 读取文件数据
		int count = 0;
		byte[] buffer = new byte[2048];
		while ((count = fis.read(buffer, 0, buffer.length)) != -1)
			// 写入压缩文件
			zos.write(buffer, 0, count);
		fis.close();
		zos.flush();
		zos.close();
	}
	//往压缩包中读取文件
	public static void readZipFile(String file) throws Exception {
		java.util.zip.ZipFile zf = new java.util.zip.ZipFile(file);
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		ZipInputStream zin = new ZipInputStream(in);
		java.util.zip.ZipEntry ze;
		while ((ze = zin.getNextEntry()) != null) {
		/*	if (ze.isDirectory()) {
				System.err.println("dir - " + ze.getName());
			} else {*/
				System.err.println("file - " + ze.getName() + " : "
						+ ze.getSize() + " bytes");
			/*	long size = ze.getSize();
				if (size > 0) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(zf.getInputStream(ze)));
					String line;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
					}
					br.close();
				}
				System.out.println();*/
		//	}
		}
		zin.closeEntry();
	}
	public static void main(String[] args) throws Exception{
		String path = "D:\\testxls\\zskcs_beta5\\2016年永定区八年级升学质量检查.zip";
//		ZipUtil.readZipFile(path);
//		String relative = "龙岩市 2016年永定区八年级升学质量检查 报表/全市报表/";
		String relative = "test/";
		String srcPath = "D:\\testxls\\zskcs_5\\上线数_理科.xls";
		ZipUtil.insertZipFile(path, srcPath, relative);
	}
}
