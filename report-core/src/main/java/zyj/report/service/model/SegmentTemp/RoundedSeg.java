package zyj.report.service.model.SegmentTemp;

import zyj.report.common.constant.EnmSegmentType;

/**
 * Created by CXinZhi on 2017/1/12.
 */
public class RoundedSeg implements ScoreSegment {

	@Override
	public Integer doSegment(EnmSegmentType enmSegmentType, Float score) {
		return Math.round(score);
	}
}
