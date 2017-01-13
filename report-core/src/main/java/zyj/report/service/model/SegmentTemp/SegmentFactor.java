package zyj.report.service.model.SegmentTemp;

import zyj.report.common.constant.EnmSegmentType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CXinZhi on 2017/1/12.
 */
public class SegmentFactor {

	private static SegmentFactor factory = new SegmentFactor();

	private static Map segmentMap = new HashMap<>();

	static{
		segmentMap.put(EnmSegmentType.ROUNDED.getCode(), new RoundedSeg());
		segmentMap.put(EnmSegmentType.CEILING.getCode(), new CeilingSeg());
		segmentMap.put(EnmSegmentType.FLOOR.getCode(), new FloorSeg());
	}

	public ScoreSegment creator(Integer type){
		return (ScoreSegment) segmentMap.get(type);
	}

	public static SegmentFactor getInstance(){
		return factory;
	}

}
