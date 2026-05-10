package egovframework.healthcenter.office.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업무 유형 수정 요청")
public record ServiceTypeUpdateRequest(
	@Schema(description = "업무명", example = "예방접종")
	String name,
	@Schema(description = "업무 설명", example = "예방접종 예약 및 현장 접수")
	String description,
	@Schema(description = "기본 예약 가능 인원", example = "5")
	Integer defaultCapacity,
	@Schema(description = "사용 여부", example = "true")
	Boolean active
) {
}
