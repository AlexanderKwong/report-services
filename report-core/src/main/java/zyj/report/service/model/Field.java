package zyj.report.service.model;

import java.util.Iterator;

/**
 * @author 邝晓林
 * @Description
 * @date 2017/1/9
 */
public interface Field extends Cloneable{

	String getTitle();

	String getMark();

	Iterator<Field> createIterator();
}
