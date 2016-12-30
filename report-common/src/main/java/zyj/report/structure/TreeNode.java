package zyj.report.structure;

import java.util.*;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description 线程不安全的树结构
 * @Company 广东全通教育股份公司
 * @date 2016/11/18
 */
public class TreeNode extends HashMap<String, Object> {

    Map<String, TreeNode> childrenMap = new HashMap<>();

    private TreeNode parent;

    protected String id ;

    private int level = 0;//这是树的第几层，root节点是0

    public TreeNode getParent(){
        return parent;
    }

    public Map<String, TreeNode> getChildrenMap() {
        return childrenMap;
    }

    public void setChildrenMap(Map<String, TreeNode> childrenMap) {
        this.childrenMap = childrenMap;
    }

    public void setParent(TreeNode parent) {
        if (parent != null) {

            if (parent.getChildrenMap().containsKey(this.getId())) return;
            //子节点 添加父节点的引用
            this.parent = parent;
            //父节点 添加子节点的引用
            parent.getChildrenMap().put(this.getId(), this);
            //子节点维护 level字段, 并通知子节点的所有子节点更新level
            this.setLevel(parent.getLevel() + 1);
        }else System.out.println("Warn :  parent 为空");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Collection<TreeNode> getChildren(){
        return childrenMap.values();
    }

    public TreeNode getChild(String id){
        return childrenMap.get(id);
    }

    public void addChild(TreeNode child){

        child.setParent(this);
    }

    public int getLevel() {
        return level;
    }
    //每次setlevel都必须更新子节点的level
    protected void setLevel(int level) {
        this.level = level;
        Collection<TreeNode> children = this.getChildren();
        if (children.isEmpty()){
            return ;
        }
        for (TreeNode child : children){
            child.setLevel(level+1);
        }
    }

    public TreeNode(TreeNode parent, String id) {
        this.setParent( parent);
        this.id = id;
    }

    public TreeNode(Map<? extends String, ?> selfMsg, TreeNode parent, String id) {
        super(selfMsg);
        this.id = id;
        this.setParent( parent);
    }

    @Override
    public String toString() {
        Set<Map.Entry<String,Object>> entrys = this.entrySet();
        Iterator<Map.Entry<String,Object>> i = entrys.iterator();

        StringBuilder sb = new StringBuilder();
        sb.append("|-当前是树结构的第").append(level).append("层，id = " ).append(id).append("\n");

        while(i.hasNext()){
            Map.Entry<String, Object> entry = i.next();
            sb.append("|---key:").append(entry.getKey()).append(" -- value:").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public void printAllChildrenStack(){
        System.out.println(this);
        Collection<TreeNode> children = this.getChildren();
        if (children.isEmpty()){
            return ;
        }
        for (TreeNode child : children){
            child.printAllChildrenStack();
        }
    }
}
