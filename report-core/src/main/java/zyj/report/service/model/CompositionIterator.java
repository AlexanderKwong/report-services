package zyj.report.service.model;

import java.util.Iterator;
import java.util.Stack;

/**
 * @author 邝晓林
 * @Description
 * @date 2017/1/10
 */
public class CompositionIterator implements Iterator<zyj.report.service.model2.Field> {
    Stack<Iterator> stack = new Stack();

    public CompositionIterator(Iterator iterator){
        stack.push(iterator);
    }

    @Override
    public boolean hasNext() {
        if (stack.isEmpty())
            return false;
        else {
            Iterator iterator = stack.peek();
            if (!iterator.hasNext()){
                stack.pop();
                return hasNext();
            }else{
                return true;
            }
        }
    }

    @Override
    public zyj.report.service.model2.Field next() {
        if (hasNext()){
            Iterator<zyj.report.service.model2.Field> iterator = stack.peek();
            zyj.report.service.model2.Field field = iterator.next();
            if (field instanceof MultiField){
                stack.push(field.createIterator());
            }
            return field;
        }else
            return null;
    }

    public Integer getLevel(){
        return stack.size();
    }

}