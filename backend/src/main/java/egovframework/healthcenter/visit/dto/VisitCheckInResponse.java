package egovframework.healthcenter.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약자 체크인 응답")
public record VisitCheckInResponse(
	@Schema(description = "방문 ID", example = "200")
	Long visitId,
	@Schema(description = "대기표 ID", example = "300")
	Long queueTicketId,
	@Schema(description = "대기번호", example = "15")
	Integer ticketNumber,
	@Schema(description = "대기 상태", example = "WAITING")
	String status
) {
}
