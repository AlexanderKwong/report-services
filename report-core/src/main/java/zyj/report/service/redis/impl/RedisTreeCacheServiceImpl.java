package zyj.report.service.redis.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zyj.report.service.redis.RedisService;
import zyj.report.structure.TreeNode;

import java.util.*;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/11/18
 */
@Service
public class RedisTreeCacheServiceImpl {

    @Autowired
    RedisService redisService;

    /**
     * 将一棵树从根节点 逐级缓存
     * @param root
     */
    public List<String> cache(String key, TreeNode root){//返回包括自己在内的所有子节点Id

        //cache itself
        redisService.hmset(key + ":" + root.getLevel() + ":" + root.getId(),"value",root);
        //cache the side between itself and children( also children's children) using ids
        List<String> ids = new ArrayList<>();

        Collection<TreeNode> children = root.getChildren();
        if (children.isEmpty()){
            ids.add(root.getId());
            return ids;
        }

        //cache children
        for (TreeNode child : children){
            ids.addAll(cache(key, child));
        }
        //cache sides
        redisService.sadd(key + ":" + root.getLevel() + ":" + root.getId() + ":heirs", ids.toArray(new String[ids.size()]));

        ids.add(root.getId());
        return ids;
    }

}
