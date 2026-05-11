package egovframework.healthcenter.queue.dto;

import java.time.LocalDateTime;

import egovframework.healthcenter.queue.mapper.QueueTicketVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기표 응답")
public record QueueTicketResponse(
	@Schema(description = "대기표 ID", example = "301")
	Long queueTicketId,
	@Schema(description = "방문 ID", example = "201")
	Long visitId,
	@Schema(description = "업무 유형 ID", example = "1")
	Long serviceTypeId,
	@Schema(description = "업무 유형명", example = "예방접종")
	String serviceTypeName,
	@Schema(description = "대기번호", example = "16")
	Integer ticketNumber,
	@Schema(description = "대기 상태", example = "WAITING")
	String status,
	@Schema(description = "방문 유형", example = "WALK_IN")
	String visitType,
	@Schema(description = "방문자 이름", example = "Swagger현장접수")
	String visitorName,
	@Schema(description = "방문자 전화번호", example = "010-4567-8901")
	String visitorPhone,
	@Schema(description = "발급 일시", example = "2026-05-11T10:30:00")
	LocalDateTime issuedAt,
	@Schema(description = "호출 일시", example = "2026-05-11T10:35:00")
	LocalDateTime calledAt,
	@Schema(description = "처리 시작 일시", example = "2026-05-11T10:36:00")
	LocalDateTime startedAt,
	@Schema(description = "처리 완료 일시", example = "2026-05-11T10:45:00")
	LocalDateTime completedAt,
	@Schema(description = "보류 일시", example = "2026-05-11T10:38:00")
	LocalDateTime holdAt
) {

	public static QueueTicketResponse from(QueueTicketVO ticket) {
		return new QueueTicketResponse(
			ticket.getId(),
			ticket.getVisitId(),
			ticket.getServiceTypeId(),
			ticket.getServiceTypeName(),
			ticket.getTicketNumber(),
			ticket.getStatus(),
			ticket.getVisitType(),
			ticket.getVisitorName(),
			ticket.getVisitorPhone(),
			ticket.getIssuedAt(),
			ticket.getCalledAt(),
			ticket.getStartedAt(),
			ticket.getCompletedAt(),
			ticket.getHoldAt()
		);
	}
}
