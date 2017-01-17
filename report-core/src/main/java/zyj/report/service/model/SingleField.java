package zyj.report.service.model;

import java.util.Iterator;

/**
 * @author 邝晓林
 * @Description
 * @date 2017/1/9
 */
public class SingleField implements zyj.report.service.model.Field {

    private String title;

    private String mark;

    public SingleField(String title, String mark) {
        this.title = title;
        this.mark = mark;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getMark() {
        return mark;
    }

    @Override
    public Iterator<zyj.report.service.model.Field> createIterator() {
        throw new UnsupportedOperationException();
    }
}
