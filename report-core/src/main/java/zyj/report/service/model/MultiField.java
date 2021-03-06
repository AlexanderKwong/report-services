package zyj.report.service.model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author 邝晓林
 * @Description
 * @date 2017/1/9
 */
public class MultiField implements zyj.report.service.model.Field {

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

    public void add(zyj.report.service.model.Field field){
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

    public MultiField copy(String rename) {
        MultiField object = null;
        String tmp = this.title;
        try {
            this.title = rename;
            object = (MultiField)super.clone();
        } catch (CloneNotSupportedException exception) {
            System.err.println("MultiField is not Cloneable");
        }finally {
            this.title = tmp;
        }
        return object;
    }
}
