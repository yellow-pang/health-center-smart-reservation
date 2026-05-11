package egovframework.healthcenter.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현장 접수 응답")
public record VisitWalkInResponse(
	@Schema(description = "방문 ID", example = "201")
	Long visitId,
	@Schema(description = "대기표 ID", example = "301")
	Long queueTicketId,
	@Schema(description = "대기번호", example = "16")
	Integer ticketNumber,
	@Schema(description = "대기 상태", example = "WAITING")
	String status
) {
}
