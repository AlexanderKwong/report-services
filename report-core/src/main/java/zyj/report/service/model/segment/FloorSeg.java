package zyj.report.service.model.segment;

import zyj.report.common.constant.EnmSegmentType;

/**
 * Created by CXinZhi on 2017/1/12.
 */
public class FloorSeg implements ScoreSegment {

	@Override
	public Integer doSegment(EnmSegmentType enmSegmentType, Float score) {
		return (int) score.floatValue();
	}
}
