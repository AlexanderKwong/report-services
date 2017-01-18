package zyj.report.service.model.segment;

import zyj.report.common.constant.EnmSegmentType;

/**
 * Created by CXinZhi on 2017/1/12.
 */
public interface ScoreSegment {

	/**
	 *
	 * 进行数据分区
	 *
	 * @param enmSegmentType 分区类别
	 * @param score
	 * @return
	 */
	Integer doSegment(EnmSegmentType enmSegmentType,Float score);

}
