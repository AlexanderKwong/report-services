package zyj.report.common;

/**
 * 
 * Spring 容器接口方法
 *
 *
 */
public interface ISpringContext {
	public  <T> T getSpringBean(Class<T> clazz, String name);
}
