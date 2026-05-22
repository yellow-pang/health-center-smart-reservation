package egovframework.healthcenter.queue.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미처리 대기표 마감 응답")
public record ClosePendingQueueTicketsResponse(
	@Schema(description = "마감 처리일", example = "2026-05-22")
	LocalDate date,
	@Schema(description = "NO_SHOW 처리된 대기표 수", example = "3")
	Integer closedCount
) {
}
