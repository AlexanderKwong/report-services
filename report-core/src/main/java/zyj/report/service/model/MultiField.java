package zyj.report.service.model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author 邝晓林
 * @Description
 * @date 2017/1/9
 */
public class MultiField implements zyj.report.service.model2.Field {

    ArrayList<zyj.report.service.model.Field> fields = new ArrayList<>();

    private String title;

    private String mark;

    public MultiField(String title) {
        this(title,"");
    }

    public MultiField(String title, String mark) {
        this.title = title;
        this.mark = mark;
    }

    public void add(zyj.report.service.model2.Field field){
        fields.add(field);
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
    public Iterator<zyj.report.service.model2.Field> createIterator() {
        return new CompositionIterator(fields.iterator());
    }


}
