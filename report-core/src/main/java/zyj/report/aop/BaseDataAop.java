package zyj.report.aop;

import org.apache.commons.lang.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import zyj.report.annotation.CacheBefore;
import zyj.report.annotation.ParentScope;
import zyj.report.annotation.Scope;
import zyj.report.exception.report.ReportCacheException;
import zyj.report.exception.report.ReportExportException;
import zyj.report.service.redis.RedisService;
import zyj.report.service.redis.impl.RedisTreeCacheServiceImpl;
import zyj.report.structure.TreeNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 邝晓林
 * @version V1.0
 * @Description
 * @Company 广东全通教育股份公司
 * @date 2016/11/6
 */
@Aspect
public class BaseDataAop {

    private Logger logger = LoggerFactory.getLogger(BaseDataAop.class);
    @Autowired
    RedisService redisService;
    @Autowired
    RedisTreeCacheServiceImpl redisTreeCacheService;

    @Pointcut("within(zyj.report.service.BaseDataService)")
    public void cacheBefore() {}	//通过执行不同的Pointcut是执行不同的切面方法

    @Pointcut("execution(* zyj.report.service.BaseDataService.getClasses(..))")
    public void cacheAfter() {}	//通过执行不同的Pointcut是执行不同的切面方法

    @Pointcut("@annotation(zyj.report.annotation.CacheBefore)")
    public void cacheBeforeOnAnnotation() {}	//通过执行不同的Pointcut是执行不同的切面方法

   /* @Before("cacheBeforeOnAnnotation() && args(paramter) ")
    public void test(String... paramter){
        System.out.println("get in ");
    }*/

    /**
     * 缓存的格式是  (1)节点(hashmap)是 exambatchId:treenodeLevel:id （2）节点的所有子节点集合(set)是 exambatchId:treenodeLevel:id:heirs (3) 学生所有科目的分数和总分作为一条JSON存储，各科小题分作为一条JSON存储
     * @param pjp
     * @throws Throwable
     */
     @Around("cacheBefore() && cacheBeforeOnAnnotation () ")
    public Object tryGetCache(ProceedingJoinPoint pjp/*, String... paramter*/) throws Throwable {
//        System.out.println("开始查缓存...");
         logger.debug("开始查缓存...");
        Object[] paramter =  pjp.getArgs();
        if(paramter.length <= 0) {
            throw new ReportCacheException("exambatchId是必传参数！");
        }
        String exambatchId = ObjectUtils.toString(paramter[0]);
        Method method =  ((MethodSignature) pjp.getSignature()).getMethod();
        Scope scope =  method.getDeclaredAnnotation(CacheBefore.class).scope();
        String value =  method.getDeclaredAnnotation(CacheBefore.class).value();
         boolean findCache = true;
        if (paramter.length == 1){//只有exambatchId --> 将这个维度下的所有key拿出来，再去重（因为有heirs），查询
            Set<String> keySet =  redisService.keys(exambatchId + ":" + scope + "*");
            if (keySet.size() != 0){

                Set<String> keys = new HashSet<>();
                for (String key : keySet){
                    keys.add(exambatchId + ":" + scope + ":" + key.split(":")[2]);
                }
                List<Map<String, String>> result = redisService.hgetWithPipline(value, keys.toArray(new String[keys.size()]));
                if (!result.isEmpty()){
                    return result;
                }else findCache = false;
            }else{
                findCache = false;
            }
        }
        else if (paramter.length == 2){// paramter[0]是exambatchId, paramter[1]是parentScope --> 分两种情况(1)scope == parentScope 即取出某个节点，此时parentScopeId应该是要取出的节点Id;(2)parentScope < scopre，即在大范围内找子集
            String scopeId = ObjectUtils.toString(paramter[1]);
            ParentScope parentScope = null;
            try {
                parentScope = (ParentScope)method.getParameterAnnotations()[1][0];
            }catch (Exception e){
                throw new ReportCacheException("没有找到parentScope的注解。");
            }

            if ( parentScope == null) {
                throw new ReportCacheException("没有找到parentScope的注解。");
            }else if ( parentScope.value() == scope){//第一种情况
                Map<String,String> queryResult =  (Map<String,String>)redisService.hmget(exambatchId + ":" + scope + ":" + scopeId, value);
                if (queryResult != null && !queryResult.isEmpty()){
                    return queryResult;
                }else findCache = false;
            }else {//第二种情况
                List<Map<String, String>> queryResult =  redisService.<Map<String, String>>sortAndGet(exambatchId + ":" + parentScope.value() + ":" + scopeId + ":heirs", null, exambatchId + ":" + scope + ":*->" + value);
                if (queryResult!=null && !queryResult.isEmpty()) return queryResult;
                else findCache = false;
            }
        }else if(paramter.length == 3){// 获取科目 paramter[0] == exambatchId ;paramter[1] == paperId ; paramter[2] == subjectShortName

            if (scope != Scope.SUBJECT)  throw new ReportCacheException("没有匹配上参数个数。") ;
            String paperId = ObjectUtils.toString(paramter[1]);
            String shortName = ObjectUtils.toString(paramter[2]);
            Map<String,String> queryResult =  (Map<String,String>)redisService.hmget(exambatchId + ":" + scope + ":" + paperId + "_" + shortName, value);
            if (queryResult!=null && queryResult.isEmpty()) return queryResult;
            else  findCache = false;

        }else if (paramter.length == 4 || paramter.length == 6){// 缓存学生科目和总分 paramter[0] ==  exambatchId,  paramter[1] ==  parentScopeId,  paramter[2] ==  level,  paramter[3] ==  stuType

            if (scope != Scope.STUDENT)  throw new ReportCacheException("没有匹配上参数个数。") ;
            String field = "";
            String parentScopeId = ObjectUtils.toString(paramter[1]);
            String level = ObjectUtils.toString(paramter[2]);
            Integer stuType = (Integer)paramter[3];
            if (paramter.length == 6){
                String paperId = ObjectUtils.toString(paramter[4]);
                String shortName = ObjectUtils.toString(paramter[5]);
                field += "XIAOTIFEN_" + paperId + "_" + shortName;
            }else field += "XUESHENGCHENGJI";

            Scope parentScope = transLevelToScope(level);
            //逻辑是：先找到该维度下面的所有班级，再求这些班级的下面学生的并集，再用管道获取这些人（总比keys * 要快）
            if ( parentScope == Scope.CLASS){//第一种情况
                Set<String> studentId =  redisService.smembers(exambatchId + ":" + parentScope + ":" + parentScopeId + ":heirs");
                if (studentId != null && !studentId.isEmpty()){
                    List<String>  keys = new ArrayList<>();
                    for (String s : studentId){
                        keys.add(exambatchId + ":" + scope + ":" + s);
                    }
                    List<Map<String,String>>queryResult = redisService.hgetWithPipline(field, keys.toArray(new String[keys.size()]));
                    if (queryResult != null && !queryResult.isEmpty()){
                        return queryResult;
                    }else findCache = false;
                }else findCache = false;

            }else {//第二种情况,先要根据parentScopeId获得classesId
                List<Map<String, String>> classes =  redisService.<Map<String, String>>sortAndGet(exambatchId + ":" + parentScope + ":" + parentScopeId + ":heirs", null, exambatchId + ":" + Scope.CLASS + ":*->value" );
                if (classes !=null && !classes.isEmpty()){
                    List<String>  keys = new ArrayList<>();
                    for (Map<String, String> c : classes){
                        keys.add(exambatchId + ":" + Scope.CLASS + ":" + c.get("CLS_ID") + ":heirs");
                    }
                    Set<String> studentId =  redisService.sunion(keys);
                    if (studentId != null && !studentId.isEmpty()){
                        keys.clear();
                        for (String s : studentId){
                            keys.add(exambatchId + ":" + scope + ":" + s);
                        }
                        List<Map<String,String>>queryResult = redisService.hgetWithPipline(field, keys.toArray(new String[keys.size()]));
                        if (queryResult!=null && !queryResult.isEmpty()) return queryResult;
                        else findCache = false;
                    }else findCache = false;
                }else findCache = false;
            }
        }else throw new ReportCacheException("没有匹配上参数个数。");

         if (!findCache){
             Object result = pjp.proceed(paramter);
             if (method.getName().equals("getClasses")){//cache
                 cacheBaseData(exambatchId, (List<Map<String,Object>>)result);
             }else if(method.getName().equals("getSubjectByExamid")){
                 for (Map s : (List<Map<String,Object>>)result)
                     redisService.hmset(exambatchId + ":" + scope + ":" + s.get("PAPER_ID")+ "_" +s.get("SUBJECT"), value,s );
             }else if (method.getName().equals("getStudentSubjectsAndAllscore") || method.getName().equals("getStudentQuestion")){
                 String level = ObjectUtils.toString(paramter[2]);
                 Scope parentScope = transLevelToScope(level);
                 String field = "";
                 if (parentScope != Scope.CITY) return result;
                 if (paramter.length == 4 )
                     field += "XUESHENGCHENGJI";
                 else if(paramter.length == 6){
                     String paperId = ObjectUtils.toString(paramter[4]);
                     String shortName = ObjectUtils.toString(paramter[5]);
                     field += "XIAOTIFEN_" + paperId + "_" + shortName;
                 }
                 List<Map<String,String>> stuList = (List<Map<String,String>>)result;
                 Map<String, Set<String>> classesHeirs = new HashMap<>();
                 HashMap<String,Map<String,HashMap>> map = new HashMap<>();
                 if(stuList != null){
                     for(Map<String,String> obj : stuList){
                         String classesId =  obj.get("CLS_ID");
                         String userId = obj.get("USER_ID");
                         //将学生加到班级下面
                         Set<String> stuInClass = classesHeirs.get(exambatchId + ":" + Scope.CLASS + ":" + classesId + ":heirs");
                         if(stuInClass == null) {
                             stuInClass = new HashSet<>();
                             classesHeirs.put(exambatchId + ":" + Scope.CLASS + ":" + classesId + ":heirs", stuInClass);
                         }
                         stuInClass.add(userId);
                         //构造学生KV对
                         String k = exambatchId + ":" + scope + ":" + userId;
                         HashMap Field = new HashMap<>();
                         Field.put(field, obj);
                         map.put(k, Field);
                     }
                     redisService.hmsetWithPipline(map);//缓存学生
                     //缓存班级学生集合
                    for (Map.Entry<String,Set<String>> entry : classesHeirs.entrySet()){
                        redisService.sadd(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
                    }
                 }
             }

             return result;
         }
         else  throw new ReportCacheException("未知异常！");
    }

//    @AfterThrowing("cacheBefore()")
//      public void testThrow(){
//        System.out.println("ERROR");
//    }

//    @AfterReturning(value = "cacheAfter()" ,returning = "classesInfo")
    public void cacheBaseData(String exambatchId, List<Map<String,Object>> classesInfo) throws ReportExportException {
        if (classesInfo.isEmpty()) return;
//        System.out.println("获得所有 班级，学校，镇区，市区");
        logger.debug("获得所有 班级，学校，镇区，市区");
        /****************获取学校列表***********************/
        Set<String> schoolIdSet = new HashSet<>();
        List<Map<String, Object>> schoolsInfo =  classesInfo.stream().map(c -> {
            String schoolId = ObjectUtils.toString(c.get("SCH_ID"));
            if (!Objects.isNull(schoolId) && !schoolIdSet.contains(schoolId)) {
                schoolIdSet.add(schoolId);
                HashMap<String, Object> sch = new HashMap<String, Object>();
                sch.put("CITY_ID", c.get("CITY_ID"));
                sch.put("CITY_NAME", c.get("CITY_NAME"));
                sch.put("AREA_ID", c.get("AREA_ID"));
                sch.put("AREA_NAME", c.get("AREA_NAME"));
                sch.put("SCH_ID", c.get("SCH_ID"));
                sch.put("SCH_NAME", c.get("SCH_NAME"));
                return sch;
            } else return null;
        }).distinct().filter(m -> !Objects.isNull(m)).collect(Collectors.toList());
        /****************获取镇区列表***********************/
        Set<String> areaIdSet = new HashSet<>();
        List<Map<String, Object>> areasInfo =  schoolsInfo.stream().map(s -> {
            String schoolId = ObjectUtils.toString(s.get("AREA_ID"));
            if (!Objects.isNull(schoolId) && !areaIdSet.contains(schoolId)) {
                areaIdSet.add(schoolId);
                HashMap<String, Object> area = new HashMap<String, Object>();
                area.put("CITY_ID", s.get("CITY_ID"));
                area.put("CITY_NAME", s.get("CITY_NAME"));
                area.put("AREA_ID", s.get("AREA_ID"));
                area.put("AREA_NAME", s.get("AREA_NAME"));
                return area;
            } else return null;
        }).distinct().filter(m -> !Objects.isNull(m)).collect(Collectors.toList());
        /****************获取市区***********************/
        Set<String> cityIdSet = new HashSet<>();
        List<Map<String, Object>> citiesInfo = areasInfo.stream().map(a -> {
            String cityId = ObjectUtils.toString(a.get("CITY_ID"));
            if (!Objects.isNull(cityId) && !cityIdSet.contains(cityId)) {
                cityIdSet.add(cityId);
                HashMap<String, Object> city = new HashMap<String, Object>();
                city.put("CITY_ID", a.get("CITY_ID"));
                city.put("CITY_NAME", a.get("CITY_NAME"));
                return city;
            } else return null;
        }).distinct().filter(m -> !Objects.isNull(m)).collect(Collectors.toList());
        if(citiesInfo.size() != 1 ) throw new ReportExportException("暂时不支持跨市考试！");


    //生成一棵树
        TreeNode city = new TreeNode(citiesInfo.get(0),null,ObjectUtils.toString(citiesInfo.get(0).get("CITY_ID")));
        for (Map<String, Object> a : areasInfo ){
            TreeNode area = new TreeNode(a,city,ObjectUtils.toString(a.get("AREA_ID")));
        }
        for (Map<String, Object>s : schoolsInfo){
            TreeNode school = new TreeNode(s,city.getChild(ObjectUtils.toString(s.get("AREA_ID"))),ObjectUtils.toString(s.get("SCH_ID")));
        }
        for (Map<String, Object>c : classesInfo){
            TreeNode classes = new TreeNode(c,city.getChild(ObjectUtils.toString(c.get("AREA_ID"))).getChild(ObjectUtils.toString(c.get("SCH_ID"))),ObjectUtils.toString(c.get("CLS_ID")));
        }
        //调试用  输出树结构
//        city.printAllChildrenStack();

//        System.out.println("开始塞缓存..");
        logger.debug("开始塞缓存..");
        long start = System.currentTimeMillis();
        redisTreeCacheService.cache(exambatchId, city);
//        System.out.println(String.format("缓存结束，耗时：%d毫秒",System.currentTimeMillis()-start));
        logger.debug(String.format("缓存结束，耗时：%d毫秒",System.currentTimeMillis()-start));
    }


    private Scope transLevelToScope(String level){
         switch (level){
             case "city": return Scope.CITY;
             case "area": return Scope.AREA;
             case "school": return Scope.SCHOOL;
             case "classes": return Scope.CLASS;
             case "student": return Scope.STUDENT;
             default: return Scope.CITY;
         }
    }
}
