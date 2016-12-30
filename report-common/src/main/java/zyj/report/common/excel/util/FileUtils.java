package zyj.report.common.excel.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils
{
  private FileUtils()
  {
    throw new Error("Don't let anyone instantiate this class.");
  }

  public static BufferedReader getBufferReaderFromFile(String filename, String charset) throws FileNotFoundException
  {
    InputStream ss = new FileInputStream(filename);
    InputStreamReader ireader = null;
    try {
      ireader = new InputStreamReader(ss, charset);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    BufferedReader reader = new BufferedReader(ireader);
    return reader;
  }

  public static BufferedReader getBufferReaderFromFile(File file, String charset) throws FileNotFoundException
  {
    InputStream ss = new FileInputStream(file);

    BufferedReader reader = null;
    try
    {
      InputStreamReader ireader;
      if (charset == null)
        ireader = new InputStreamReader(ss, "ISO-8859-1");
      else {
        ireader = new InputStreamReader(ss, charset);
      }
      reader = new BufferedReader(ireader);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return reader;
  }

  public static BufferedInputStream getBufferedInputStreamFromFile(String filename) throws FileNotFoundException
  {
    InputStream ss = new FileInputStream(filename);
    BufferedInputStream bff = new BufferedInputStream(ss);
    return bff;
  }

  public static ByteArrayInputStream getInputSream2byteArr(byte[] bytes)
  {
    ByteArrayInputStream inputStreamClient = new ByteArrayInputStream(bytes);
    return inputStreamClient;
  }
  

//  public static ByteArrayInputStream getInputSream2hexString(String hex)
//  {
//    return getInputSream2byteArr(SystemUtil.toBytes(hex));
//  }

  public static int isFileContains(File file, String regex, String charset)
  {
    try {
      BufferedReader reader = getBufferReaderFromFile(file, charset);
      return isFileContains(reader, regex);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return 0;
  }

  public static int isFileContains(String file, String regex, String charset)
  {
    try
    {
      BufferedReader reader = getBufferReaderFromFile(file, charset);
      return isFileContains(reader, regex);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return 0;
  }

  public static int isFileContains(Reader reader, String regex)
  {
    String readedLine = null;
    BufferedReader br = null;
    try {
      int changedRow = 0;
      br = new BufferedReader(reader);
      while ((readedLine = br.readLine()) != null) {
        changedRow++;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(readedLine);
        if (m.find()) {
          int i = changedRow; return i;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        if (br != null)
          br.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    return 0;
  }

  @Deprecated
  public static String getFullContent(StringReader reader)
  {
    BufferedReader bfreader = new BufferedReader(reader);
    String content = getFullContent(bfreader);
    try {
      if (bfreader != null)
        bfreader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return content;
  }

  @Deprecated
  public static String getFullContent(InputStream is)
  {
    InputStreamReader inputReader = new InputStreamReader(is);
    BufferedReader bureader = new BufferedReader(inputReader);
    String content = getFullContent(bureader);

    return content;
  }

  @Deprecated
  public static String getFullContent(InputStreamReader inputReader) {
    BufferedReader bur = new BufferedReader(inputReader);
    return getFullContent(bur);
  }

  @Deprecated
  public static String getFullContent(Reader reader)
  {
    BufferedReader br = new BufferedReader(reader);
    return getFullContent(br);
  }

  @Deprecated
  public static String getFullContent(CharArrayReader reader)
  {
    BufferedReader bfreader = new BufferedReader(reader);
    return getFullContent(bfreader);
  }

  public static String getByteBufferContent4array(ByteBuffer byteb) {
    if (byteb == null) {
      return null;
    }
    byteb.flip();
    return new String(byteb.array());
  }

  public static String getByteBufferContent(ByteBuffer bytebuffer) {
    bytebuffer.flip();
    byte[] content = new byte[bytebuffer.limit()];
    bytebuffer.get(content);
    return new String(content);
  }

  @Deprecated
  public static String getFullContent(BufferedReader reader)
  {
    StringBuilder sb = new StringBuilder();
    String readedLine = null;
    try {
      while ((readedLine = reader.readLine()) != null) {
        sb.append(readedLine);
        sb.append(System.getProperty("line.separator"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    String content = sb.toString();
    int length_CRLF = System.getProperty("line.separator").length();
    if (content.length() <= length_CRLF) {
      return content;
    }
    return content.substring(0, content.length() - length_CRLF);
  }

  public static String getFullContent2(File file, String charset) throws IOException
  {
    if (file.exists()) {
      FileInputStream fis = new FileInputStream(file);
      return getFullContent2(fis, charset);
    }
    return null;
  }

  public static String getFullContent2(InputStream in, String charset)
    throws IOException
  {
    int step = 1024;
    BufferedInputStream bis = new BufferedInputStream(in);

    byte[] receData = new byte[step];

    int readLength = 0;

    int offset = 0;

    int byteLength = step;

    while ((readLength = bis.read(receData, offset, byteLength - offset)) != -1)
    {
      offset += readLength;

      if (byteLength - offset <= step / 2) {
        byte[] tempData = new byte[receData.length + step];
        System.arraycopy(receData, 0, tempData, 0, offset);
        receData = tempData;
        byteLength = receData.length;
      }
    }

    return new String(receData, 0, offset, charset);
  }

  public static String getFullContent(String fileName, String charset)
  {
    BufferedReader reader = null;
    try {
      File file = new File(fileName);

      return charset == null ? getFullContent(file) : getFullContent(
        file, charset);
    } finally {
      if (reader != null)
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }

  public static String getFullContent(String fileName)
  {
    return getFullContent(fileName, "ISO-8859-1");
  }

  public static String getFullContent(File file) {
    return getFullContent(file, null);
  }

  public static String getFullContent(File file, String charset) {
    BufferedReader reader = null;
    if (!file.exists()) {
      System.out.println("getFullContent: file(" + file.getAbsolutePath() + 
        ") does not exist.");
      return null;
    }
    if (charset == null)
      charset = "ISO-8859-1";
    try
    {
      reader = getBufferReaderFromFile(file, charset);
      return getFullContent(reader);
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public static BufferedWriter getBufferedWriter(String fileName)
  {
    File file = new File(fileName);
    if (!file.exists()) {
      System.out.println("getFullContent: file(" + file.getAbsolutePath() + 
        ") does not exist.");
      return null;
    }
    try {
      FileWriter fileWrite = new FileWriter(file);
      return new BufferedWriter(fileWrite);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static BufferedWriter getBufferedWriter(File file) {
    if (!file.exists()) {
      System.out.println("getFullContent: file(" + file.getAbsolutePath() + 
        ") does not exist.");
      return null;
    }
    try
    {
      FileWriter fileWrite = new FileWriter(file);
      return new BufferedWriter(fileWrite);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void writeToFile(String fileName, String fileContent)
  {
    BufferedWriter bufferedWriter = getBufferedWriter(fileName);
    if (bufferedWriter == null) {
      System.out.println("writeToFile : bufferedWriter is null");
      return;
    }
    try {
      bufferedWriter.write(fileContent);
      bufferedWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        bufferedWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void writeToFile(File file, StringBuffer stringbuf, String charset)
  {
    if (!file.exists()) {
      System.out.println("getFullContent: file(" + file.getAbsolutePath() + 
        ") does not exist.");
      return;
    }
    writeToFile(file, stringbuf.toString(), charset);
  }

  public static void writeToFile(String fileName, StringBuffer fileContent) {
    writeToFile(fileName, fileContent.toString());
  }

  public static void writeToFile(File file, String fileContent, String charset) {
    if (!file.exists()) {
      System.out.println("getFullContent: file(" + file.getAbsolutePath() + 
        ") does not exist.");
      return;
    }
    OutputStreamWriter osw = null;
    try {
      OutputStream os = new FileOutputStream(file);
      osw = new OutputStreamWriter(os, charset);
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    BufferedWriter bufferedWriter = new BufferedWriter(osw);
    try {
      bufferedWriter.write(fileContent);
      bufferedWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        bufferedWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void writeToFile(File file, InputStream ins) {
    if (!file.exists()) {
      System.out.println("getFullContent: file(" + file.getAbsolutePath() + 
        ") does not exist.");
      return;
    }
    try {
      FileOutputStream fileouts = new FileOutputStream(file);
      int resultInt = -1;
      try {
        while ((resultInt = ins.read()) != -1)
          fileouts.write(resultInt);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      finally
      {
        try
        {
          fileouts.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      try
      {
        fileouts.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void writeToFile(File file, InputStreamReader insr)
  {
    if (!file.exists()) {
      System.out.println("getFullContent: file(" + file.getAbsolutePath() + 
        ") does not exist.");
      return;
    }
    try {
      FileOutputStream fileouts = new FileOutputStream(file);
      int resultInt = -1;
      try {
        while ((resultInt = insr.read()) != -1)
          fileouts.write(resultInt);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      finally
      {
        try
        {
          fileouts.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      try
      {
        fileouts.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void writeObjectToFile(String fileName, Object obj)
    throws Exception
  {
    ObjectOutputStream output = new ObjectOutputStream(
      new FileOutputStream(fileName));
    output.writeObject(obj);
    output.close();
  }

  public static Object readFromFile(String fileName)
    throws Exception
  {
    ObjectInputStream input = new ObjectInputStream(new FileInputStream(
      fileName));
    Object obj = input.readObject();
    input.close();
    return obj;
  }

  public static boolean isFile(String filePath)
  {
    File file = new File(filePath);
    if (file.exists()) {
      return true;
    }
    return false;
  }

  public static void printFileList(ArrayList<File> files)
  {
    if ((files == null) || (files.size() == 0)) {
      System.out.println(" ");
    }
    for (int i = 0; i < files.size(); i++) {
      String fileName = ((File)files.get(i)).getName();
      System.out.println(fileName);
    }
  }

//  public static void main(String[] args)
//  {
//    System.out.println(sizeOfFile("e:\\test\\cc.txt"));
//  }

  public static void open_directory(String folder)
  {
    File file = new File(folder);
    if (!file.exists()) {
      return;
    }
    Runtime runtime = null;
    try {
      runtime = Runtime.getRuntime();
      if (!System.getProperty("os.name").toLowerCase().contains("window"))
      {
        runtime.exec("nautilus " + folder);
      }
      else runtime.exec("cmd /c start explorer " + folder); 
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    } finally {
      if (runtime != null)
        runtime.runFinalization();
    }
  }

  public static void open_file(String filePath)
  {
    File file = new File(filePath);
    if (!file.exists()) {
      return;
    }
    Runtime runtime = null;
    try {
      runtime = Runtime.getRuntime();
      if (!System.getProperty("os.name").toLowerCase().contains("window"))
      {
        runtime.exec("nautilus " + filePath);
      }
      else runtime.exec("cmd /c start explorer /select,/e, " + filePath); 
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    } finally {
      if (runtime != null)
        runtime.runFinalization();
    }
  }

//  public static long sizeOfFile(String filePath)
//  {
//    String content = CMDUtil.getResult4cmd("dir " + filePath);
//    content = content.replaceAll("\r", " ").replaceAll("\n", " ");
//    int index = filePath.lastIndexOf('\\');
//    String regex = "\\s*((\\d+[,])*\\d+)\\s" + 
//      filePath.substring(index + 1);
//    Pattern p = Pattern.compile(regex);
//    Matcher m = p.matcher(content);
//    long sizeLong = 0L;
//    if (m.find()) {
//      String sizeStr = m.group(1);
//      if ((sizeStr != null) && (!"".equals(sizeStr))) {
//        sizeLong = Long.parseLong(sizeStr);
//      }
//    }
//    return sizeLong;
//  }

  public static String getStringKeyboard()
    throws IOException
  {
    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader br = new BufferedReader(isr);
    String s = br.readLine();
    return s;
  }

  public static int getIntKeyboard() {
    Scanner sca = new Scanner(System.in);
    return sca.nextInt();
  }

  public static File mkdirRecurse(File file)
  {
    int count = countChar(file.getAbsolutePath(), File.separatorChar);
    for (int i = 0; i < count - 1; i++) {
      File parentFile = getParentFile(file, count - i - 1);
      if (!parentFile.isDirectory()) {
        parentFile.mkdir();
      }
    }
    return file;
  }

  public static int countChar(String s, char c)
  {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c) {
        count++;
      }
    }
    return count;
  }

  public static File getParentFile(File leafFile, int deep) {
    File parentFile = leafFile;
    for (int i = 0; i < deep; i++) {
      parentFile = parentFile.getParentFile();
    }
    return parentFile;
  }
}
