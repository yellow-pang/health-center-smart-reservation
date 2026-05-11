package egovframework.healthcenter.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현장 접수 요청")
public record VisitWalkInRequest(
	@Schema(description = "업무 유형 ID", example = "1")
	Long serviceTypeId,
	@Schema(description = "방문자 이름", example = "Swagger현장접수")
	String visitorName,
	@Schema(description = "방문자 전화번호", example = "010-4567-8901")
	String visitorPhone
) {
}
