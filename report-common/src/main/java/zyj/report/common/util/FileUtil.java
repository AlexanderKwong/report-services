package zyj.report.common.util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {

	public static void delFile(String path) throws Exception {
		File ffile = new File(path);
		if (ffile.exists()) {
			ffile.delete();
		}
	}
	
	/**
	 * 创建目录
	 * @param path 
	 * @throws Exception
	 */
	public static  void mkexpdir(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.mkdirs();
			} catch (Exception e) {
				throw new Exception("创建目录失败：" + path + "!",e);
			}
		}
	}
	public static void main (String[] args) throws Exception{
		String dir = "d:/ttt/zskcs_5";
		rmvDir(dir);
	}
	
	public static void zipDir(String srcDIr, String outFile) throws Exception {
		File inputDir = new File(srcDIr);
		FileOutputStream fos = new FileOutputStream(outFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		zos.setEncoding("UTF-8");
		zipfile(inputDir.listFiles(), "", zos);
		zos.close();
		fos.close();
	}
	
	public static void zipDir2(String srcDIr, String outFile) throws Exception {
		File inputDir = new File(srcDIr);
		FileOutputStream fos = new FileOutputStream(outFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		zos.setEncoding("UTF-8");
		File[] files = new File[1];
		files[0] = inputDir;
		zipfile(files, "", zos);
		zos.close();
		fos.close();
	}

	public static void zipfile(File[] files, String baseFolder, ZipOutputStream zos)
			throws Exception {
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

	
	public static void rmvDir(String dir) {
		File file = new File(dir);
		rmvFile(file);
	}

	public static void rmvFile(File dir) {
		// 删除子文件/子目录
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();

			for (int i = 0; i < children.length; i++) {
				rmvFile(children[i]);
			}
		}
		// 删除文件/目录
		try {
			dir.delete();
		} catch (Exception e) {
			// Nothing
		}
		return;
	}
}
