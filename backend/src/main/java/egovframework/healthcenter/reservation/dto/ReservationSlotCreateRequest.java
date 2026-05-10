package egovframework.healthcenter.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 슬롯 생성 요청")
public record ReservationSlotCreateRequest(
	@Schema(description = "업무 유형 ID", example = "1")
	Long serviceTypeId,
	@Schema(description = "예약 날짜", example = "2026-05-10")
	LocalDate date,
	@Schema(description = "시작 시간", example = "09:00")
	LocalTime startTime,
	@Schema(description = "종료 시간", example = "09:30")
	LocalTime endTime,
	@Schema(description = "예약 가능 인원", example = "5")
	Integer capacity
) {
}
