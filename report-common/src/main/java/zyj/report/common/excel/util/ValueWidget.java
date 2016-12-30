package zyj.report.common.excel.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class ValueWidget
{
  public static boolean isMargin(String input)
  {
    if ((input != null) && ("".endsWith(input))) {
      return true;
    }
    return false;
  }

  public static boolean isHasValue(String input)
  {
    if ((input != null) && (!"".equals(input))) {
      return true;
    }
    return false;
  }

  public static boolean isNullOrEmpty(Object obj)
  {
    if (obj == null) {
      return true;
    }
    if ((obj instanceof CharSequence)) {
      return ((CharSequence)obj).length() == 0;
    }
    if ((obj instanceof Collection)) {
      return ((Collection)obj).isEmpty();
    }
    if ((obj instanceof Map)) {
      return ((Map)obj).isEmpty();
    }
    if ((obj instanceof Object[])) {
      Object[] object = (Object[])obj;
      boolean empty = true;
      for (int i = 0; i < object.length; i++)
        if (!isNullOrEmpty(object[i])) {
          empty = false;
          break;
        }
      return empty;
    }
    return false;
  }

  public static boolean isHasWhiteSpace(String input)
  {
    String regex = " \t\r\n";
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (regex.indexOf(c) >= 0) {
        return true;
      }
    }
    return false;
  }

  public static boolean isValidLong(String value)
  {
    try
    {
      Long.parseLong(value);
    }
    catch (NumberFormatException e)
    {
      return false;
    }
    return true;
  }

  public static boolean isValidInt(String value)
  {
    try {
      Integer.parseInt(value);
    }
    catch (NumberFormatException e)
    {
      return false;
    }
    return true;
  }

  public static boolean isValidPositiveLong(String value)
  {
    try
    {
      Long i = Long.valueOf(Long.parseLong(value));
      if (i.longValue() <= 0L)
      {
        return false;
      }
    }
    catch (NumberFormatException e)
    {
      return false;
    }
    return true;
  }

  public static boolean isValidPositiveInteger(String value)
  {
    try
    {
      int i = Integer.parseInt(value);
      if (i <= 0)
      {
        return false;
      }
    }
    catch (NumberFormatException e)
    {
      return false;
    }
    return true;
  }

  public static boolean isValidNegativeLong(String value)
  {
    try
    {
      Long i = Long.valueOf(Long.parseLong(value));
      if (i.longValue() >= 0L)
      {
        return false;
      }
    }
    catch (NumberFormatException e)
    {
      return false;
    }
    return true;
  }

  public static boolean isValidDirectory(String path) {
    File file = new File(path);
    if (file.isDirectory())
    {
      return true;
    }
    return false;
  }

  public static boolean isBetweenPositiveInteger(int min, int max, String value)
  {
    if (isValidPositiveLong(value))
    {
      Long temp = Long.valueOf(Long.parseLong(value));
      if ((temp.longValue() >= min) && (temp.longValue() <= max))
      {
        return true;
      }
    }
    return false;
  }

  public static boolean isBetweenInteger(int min, int max, String value)
  {
    if (isValidLong(value))
    {
      Long temp = Long.valueOf(Long.parseLong(value));
      if ((temp.longValue() >= min) && (temp.longValue() <= max))
      {
        return true;
      }
    }
    return false;
  }

  public static boolean isValidIP(String IP)
  {
    if ((IP == null) || (IP.length() < 1))
    {
      return false;
    }
    if (IP.indexOf(':') > -1)
    {
      return isValidV6IP(IP);
    }

    return isValidV4IP(IP);
  }

  public static boolean isValidV6IP(String ip)
  {
    if ((ip == null) || (ip.length() < 3))
    {
      return false;
    }
    int interfaceIndex = ip.lastIndexOf('%');
    if (interfaceIndex > -1)
    {
      String num = ip.substring(interfaceIndex + 1);
      try
      {
        Integer.parseInt(num);
      }
      catch (NumberFormatException e)
      {
        return false;
      }

      ip = ip.substring(0, interfaceIndex);
    }
    int singleIdx = ip.indexOf("::");
    int hasDouble = 0;
    ArrayList tokens = new ArrayList();
    if (((ip.startsWith(":")) && (!ip.startsWith("::"))) || ((ip.endsWith(":")) && (!ip.endsWith("::"))))
    {
      return false;
    }
    if (singleIdx != -1)
    {
      hasDouble = 1;
      if (ip.indexOf("::", singleIdx + 1) != -1)
      {
        return false;
      }
    }
    StringTokenizer st = new StringTokenizer(ip, ":");
    while (st.hasMoreTokens())
    {
      String token = st.nextToken();
      if (token.length() > 4)
      {
        return false;
      }
      char[] chars = token.toCharArray();
      for (int i = 0; i < chars.length; i++)
      {
        if ((!Character.isDigit(chars[i])) && ((chars[i] < 'a') || (chars[i] > 'f')) && ((chars[i] < 'A') || (chars[i] > 'F')))
        {
          return false;
        }
      }
      tokens.add(token);
    }

    if ((tokens.size() + hasDouble > 8) || ((tokens.size() < 8) && (hasDouble == 0)))
    {
      return false;
    }

    return true;
  }

  public static boolean isValidV4IP(String ip)
  {
    if (ip == null)
    {
      return false;
    }

    if ((ip.trim().indexOf("..") > -1) || (ip.trim().startsWith(".")) || (ip.trim().endsWith(".")))
    {
      return false;
    }

    StringTokenizer stringtokenizer = new StringTokenizer(ip, ".");
    if (stringtokenizer.countTokens() != 4)
    {
      return false;
    }

    try
    {
      int tempInt = 0;
      while (stringtokenizer.hasMoreTokens())
      {
        tempInt = Integer.parseInt(stringtokenizer.nextToken());
        if ((tempInt > 255) || (tempInt < 0))
        {
          return false;
        }
      }
    }
    catch (NumberFormatException e)
    {
      return false;
    }

    return true;
  }

  public static ArrayList<?> getArr4Collection(Collection<?> coll) {
    ArrayList arrs = new ArrayList();
    for (Iterator localIterator = coll.iterator(); localIterator.hasNext(); ) { Object obj = localIterator.next();
      arrs.add(obj);
    }
    return arrs;
  }

  public static String splitAndFilterString(String input, int length)
  {
    if ((input == null) || (input.trim().equals("")))
    {
      return "";
    }

    String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
      "<[^>]*>", "");
    str = str.replaceAll("[(/>)<]", "");
    int len = str.length();
    if (len <= length)
    {
      return str;
    }

    str = str.substring(0, length);
    str = str + "......";

    return str;
  }

  public static String splitAndFilterString(String input)
  {
    if ((input == null) || (input.trim().equals("")))
    {
      return "";
    }

    String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
      "<[^>]*>", "");
    str = str.replaceAll("[(/>)<]", "");
    return str;
  }

  public static String capitalize(String str)
  {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}