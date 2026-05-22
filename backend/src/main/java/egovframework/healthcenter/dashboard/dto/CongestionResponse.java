package egovframework.healthcenter.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 혼잡도 응답")
public record CongestionResponse(
	@Schema(description = "업무 유형 ID", example = "1")
	Long serviceTypeId,
	@Schema(description = "업무 유형명", example = "예방접종")
	String serviceTypeName,
	@Schema(description = "현재 혼잡도 반영 인원", example = "18")
	Integer waitingCount,
	@Schema(description = "예상 대기시간", example = "35")
	Integer estimatedWaitMinutes,
	@Schema(description = "혼잡도 코드", example = "HIGH")
	String congestionLevel,
	@Schema(description = "혼잡도 표시명", example = "혼잡")
	String congestionLabel
) {
}
