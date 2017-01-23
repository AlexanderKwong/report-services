package zyj.report.service.model.segment;

import zyj.report.common.constant.EnmSegmentType;
import zyj.report.common.util.CollectionsUtil;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by CXinZhi on 2017/1/11.
 * <p>
 * 四舍五入 分数分区
 */
public class Segment {

	// 各个分数数据统计
	TreeMap<Integer, Integer> scoreSet;

	/**
	 * 统计步长
	 */
	private Integer step;

	/**
	 * 分数段最小值
	 */
	private Integer minScore;

	/**
	 * 分数段最大值
	 */
	private Integer maxScore;

	/**
	 * 分数取整 类别
	 */
	private EnmSegmentType enmSegmentType;

	/**
	 * 参与考生数
	 */
	private Integer totalCount;
	/**
	 * 分段名
	 */
	private List<String> segmentNames;


	/**
	 * 初始化模板
	 *
	 * @param step
	 * @param minScore
	 * @param maxScore
	 */
	public Segment(Integer step, Integer minScore, Float maxScore, Integer totalCount, EnmSegmentType type) {
		this.step = step;
		this.minScore = minScore;
		ScoreSegment segment = SegmentFactor.getInstance().creator(type.getCode());
		this.maxScore = segment.doSegment(type, maxScore);
		this.enmSegmentType = type;
		this.totalCount = totalCount;
		initSegmentList();
	}

	/**
	 * 初始化分数段数据
	 */
	public void initSegmentList() {
		// 分数阶梯集合 排序
		scoreSet = new TreeMap<Integer, Integer>();
		for (int i = minScore; i < maxScore; i++) {
			scoreSet.put(i, 0);
		}
	}

	/**
	 * 按数据 进行一分一段分区
	 *
	 * @param objectMap
	 * @param key
	 */
	public void doSegment(List<Map<String, Object>> objectMap, String key) {
		objectMap.forEach(model -> {
			ScoreSegment segment = SegmentFactor.getInstance().creator(enmSegmentType.getCode());
			Integer score = segment.doSegment(enmSegmentType, Float.parseFloat(model.get(key).toString()));
			scoreSet.put(score-1, scoreSet.ceilingEntry(score-1).getValue() + 1);
		});
	}


	/**
	 * 加载 map 数据
	 *
	 * @param scoreSeg
	 * @param frequency
	 * @param accFrequency
	 * @param total
	 * @return
	 */
	private Map<String, Object> getMap(String scoreSeg, Integer frequency, Integer accFrequency,
									   Integer total) {
		DecimalFormat df = new DecimalFormat("0.00");
		Map<String, Object> field = new HashMap<>();
		field.put("SCORE_SEG", scoreSeg);
		field.put("FREQUENCY", frequency);
		field.put("FREQUENCY_CENT", df.format((float) frequency / total * 100) + "%");
		field.put("ACC_FREQUENCY", accFrequency);
		field.put("ACC_FREQUENCY_CENT", df.format((float) accFrequency / total * 100) + "%");
		return field;
	}

	public TreeMap<Integer, Integer> getScoreSegments() {
		return scoreSet;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	/**
	 *  横向转置的分组统计矩阵，结果每一行是一个分段的频数 累计 统计，Map中多个列以partitionKey串接 为Key
	 * @param objectMap 数据集
	 * @param key 成绩所在列名
	 * @param partitionKeys 分组根据列名
	 * @return
	 */
	public List<Map<String, Object>> getPartitionStepSegmentVertical(List<Map<String, Object>> objectMap, String key, String[] partitionKeys) {

		Map<String, List<Map<String, Object>>> partitions = CollectionsUtil.partitionBy(objectMap, partitionKeys);

		Map<String,Map<String,Object>> resultMap = new HashMap<>();

		partitions.entrySet().forEach( entry ->{
			String partitionKey = entry.getKey();
			this.initSegmentList();
			List<Map<String, Object>> partitionResult = this.getStepSegment(entry.getValue(),key);
			partitionResult.forEach(m->{
				resultMap.computeIfAbsent(m.get("SCORE_SEG").toString(), (value1) -> {
					HashMap hm = new HashMap();
					hm.put("SCORE_SEG", value1);
//					hm.put("index",value1.split(",")[0].substring(1));
					return hm;
				});
				resultMap.get(m.get("SCORE_SEG")).put(partitionKey,m.get("FREQUENCY"));
			});
		});
		List<Map<String, Object>> result = new ArrayList<>(resultMap.values());
//		CollectionsUtil.orderByIntValueDesc(result,"index" );
		CollectionsUtil.orderBySpecifiedValue(result, "SCORE_SEG", generateSegment().toArray());
		return result;
	}

    /**
     * 横向转置的分组统计矩阵，结果每一行（Map）是一个分组内 所有分段的频数累计 统计，partitionKey 以及 各个分段的 SCORE_SEG 的值 作为map的key
     * @param objectMap
     * @param key
     * @param partitionKeys
     * @return
     */
	public List<Map<String, Object>> getPartitionStepSegmentAccTransverse(List<Map<String, Object>> objectMap, String key, String[] partitionKeys) {

		Map<String, List<Map<String, Object>>> partitions = CollectionsUtil.partitionBy(objectMap, partitionKeys);

//		Map<String,Map<String,Object>> resultMap = new HashMap<>();
		List<Map<String,Object>> resultList = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		for (String k : partitionKeys){
			sb.append(k) ;
		}
		final String newKey = sb.toString();

		partitions.entrySet().stream().forEach( entry ->{
			String partitionKey = entry.getKey();
			this.initSegmentList();
			List<Map<String, Object>> partitionResult = this.getStepSegment(entry.getValue(),key);
			Map<String, Object> row = new HashMap<String, Object>();
			row.put(newKey,partitionKey );
			partitionResult.forEach(m->{
				row.put(m.get("SCORE_SEG").toString(),m.get("ACC_FREQUENCY"));
			});
			resultList.add(row);
		});

		return resultList;
	}

    /**
     * 横向转置的分组统计矩阵，结果每一行（Map）是一个分组内 所有分段的频数 统计，partitionKey 以及 各个分段的 SCORE_SEG 的值 作为map的key
     * @param objectMap
     * @param key
     * @param partitionKeys
     * @return
     */
	public List<Map<String, Object>> getPartitionStepSegmentTransverse(List<Map<String, Object>> objectMap, String key, String[] partitionKeys) {

		Map<String, List<Map<String, Object>>> partitions = CollectionsUtil.partitionBy(objectMap, partitionKeys);

//		Map<String,Map<String,Object>> resultMap = new HashMap<>();
		List<Map<String,Object>> resultList = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		for (String k : partitionKeys){
			sb.append(k) ;
		}
		final String newKey = sb.toString();

		partitions.entrySet().stream().forEach( entry ->{
			String partitionKey = entry.getKey();
			this.initSegmentList();
			List<Map<String, Object>> partitionResult = this.getStepSegment(entry.getValue(),key);
			Map<String, Object> row = new HashMap<String, Object>();
			row.put(newKey,partitionKey );
			partitionResult.forEach(m->{
				row.put(m.get("SCORE_SEG").toString(),m.get("FREQUENCY"));
			});
			resultList.add(row);
		});

		return resultList;
	}

    /**
     * 产生对应步长的分数段
     * @return
     */
	public List<String> generateSegment() {
		if (segmentNames == null) {

			segmentNames = new ArrayList<>();

			Integer lest = maxScore % step;
			Integer counter = maxScore / step;

			// 对第一个 闭区间 或者 余数 值进行特别处理
			if (lest == 0) {
				//对闭区间 值进行特别处理
				segmentNames.add(String.format("[%d,%d]", (counter - 1) * step, (counter * step)));
				counter--;

			} else {
				//对余数 值进行特别处理
				segmentNames.add(String.format("[%d,%d]", (counter) * step, (counter * step) + lest));
			}

			// 对第一个有数 区间以下的数据进行处理
			for (int i = counter; i > 0; i--) {
				Integer form = i * step - 1;
				Integer to = (i - 1) * step - 1;

				segmentNames.add(String.format("[%d,%d)", to + 1, form + 1));

			}
		}
		return segmentNames;
	}
	/**
	 * 进行分段计算
	 *
	 * @param objectMap
	 * @param key
	 * @return
	 */
	public List<Map<String, Object>> getStepSegment(List<Map<String, Object>> objectMap, String key) {

		doSegment(objectMap, key);

		if (segmentNames == null || segmentNames.isEmpty())  generateSegment();

		List<Map<String, Object>> stepSegment = new ArrayList<>();

		Integer total = 0;
		Integer accTotal = 0;

		for (String s : segmentNames)  {
			String[] fromTo = s.substring(1,s.length()-1).split(",");
			Integer from = segmentNames.indexOf(s) == 0 ? Integer.parseInt(fromTo[1]):Integer.parseInt(fromTo[1])-1;
			Integer to = Integer.parseInt(fromTo[0]) -1  ;

			SortedMap<Integer, Integer> subMap = scoreSet.descendingMap().subMap(from, to);
			Iterator iterator = subMap.entrySet().iterator();
			total = 0;
			while (iterator.hasNext()) {
				Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iterator.next();
				total += entry.getValue();
			}

			accTotal += total;
			stepSegment.add(getMap(s, total, accTotal, totalCount));
		}

		return stepSegment;
	}

}
