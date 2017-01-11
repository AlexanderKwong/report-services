package zyj.report.service.model2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * @author 邝晓林
 * @Description
 * @date 2017/1/9
 */
public class MultiField implements Field{

    ArrayList<Field> fields = new ArrayList<>();

    private String title;

    private String mark;

    public MultiField(String title) {
        this(title,"");
    }

    public MultiField(String title, String mark) {
        this.title = title;
        this.mark = mark;
    }

    public void add(Field field){
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
    public Iterator<Field> createIterator() {
        return new CompositionIterator(fields.iterator());
    }


}
