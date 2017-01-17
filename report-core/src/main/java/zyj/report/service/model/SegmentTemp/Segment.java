package zyj.report.service.model.SegmentTemp;

import zyj.report.common.constant.EnmSegmentType;

import java.text.DecimalFormat;
import java.util.*;

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
	private void initSegmentList() {
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
	 * 进行分段计算
	 *
	 * @param objectMap
	 * @param key
	 * @return
	 */
	public List<Map<String, Object>> getStepSegment(List<Map<String, Object>> objectMap, String key) {

		doSegment(objectMap, key);

		List<Map<String, Object>> stepSegment = new ArrayList<>();

		Integer total = 0;
		Integer accTotal = 0;



		Integer lest = maxScore % step;
		Integer counter = maxScore / step;

		// 对最后一个 闭区间 或者 余数 值进行特别处理
		if (lest == 0) {

			//对闭区间 值进行特别处理
			SortedMap<Integer, Integer> subMap = scoreSet.descendingMap().subMap((counter * step), (counter - 1)
					* step - 1);
			Iterator iterator = subMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iterator.next();
				total += entry.getValue();
			}
			accTotal += total;
			stepSegment.add(getMap(String.format("[%d,%d]", (counter - 1) * step, (counter * step)), total,
					accTotal, totalCount));
			counter--;

		} else {

			//对余数 值进行特别处理
			SortedMap<Integer, Integer> subMap = scoreSet.descendingMap().subMap((counter * step) + lest, (counter)
					* step - 1);
			Iterator iterator = subMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iterator.next();
				total += entry.getValue();
			}
			accTotal += total;
			stepSegment.add(getMap(String.format("[%d,%d]", (counter) * step, (counter * step) + lest),
					total, accTotal, totalCount));
		}

		// 对第一个有数 区间以下的数据进行处理
		for (int i = counter; i > 0; i--) {
			Integer form = i * step - 1;
			Integer to = (i - 1) * step - 1;
			SortedMap<Integer, Integer> subMap = scoreSet.descendingMap().subMap(form, to);
			Iterator iterator = subMap.entrySet().iterator();
			total = 0;
			while (iterator.hasNext()) {
				Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iterator.next();
				total += entry.getValue();
			}

			accTotal += total;
			stepSegment.add(getMap(String.format("[%d,%d)", to + 1, form + 1), total, accTotal, totalCount));

		}

		return stepSegment;
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


}
