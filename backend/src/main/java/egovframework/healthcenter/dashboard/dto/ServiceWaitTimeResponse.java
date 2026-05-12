package egovframework.healthcenter.dashboard.dto;

import egovframework.healthcenter.dashboard.mapper.ServiceWaitTimeVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업무별 평균 대기시간 응답")
public record ServiceWaitTimeResponse(
	@Schema(description = "업무 유형 ID", example = "1")
	Long serviceTypeId,
	@Schema(description = "업무 유형명", example = "예방접종")
	String serviceTypeName,
	@Schema(description = "평균 대기시간", example = "24")
	Integer averageWaitMinutes,
	@Schema(description = "호출 건수", example = "18")
	Integer calledCount
) {

	public static ServiceWaitTimeResponse from(ServiceWaitTimeVO vo) {
		return new ServiceWaitTimeResponse(
			vo.getServiceTypeId(),
			vo.getServiceTypeName(),
			vo.getAverageWaitMinutes(),
			vo.getCalledCount()
		);
	}
}
