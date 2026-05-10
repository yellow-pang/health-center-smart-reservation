package egovframework.healthcenter.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import egovframework.healthcenter.reservation.mapper.ReservationSlotVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 슬롯 응답")
public record ReservationSlotResponse(
	@Schema(description = "예약 슬롯 ID", example = "10")
	Long slotId,
	@Schema(description = "업무 유형 ID", example = "1")
	Long serviceTypeId,
	@Schema(description = "업무 유형명", example = "예방접종")
	String serviceTypeName,
	@Schema(description = "예약 날짜", example = "2026-05-10")
	LocalDate date,
	@Schema(description = "시작 시간", example = "09:00")
	LocalTime startTime,
	@Schema(description = "종료 시간", example = "09:30")
	LocalTime endTime,
	@Schema(description = "예약 가능 인원", example = "5")
	int capacity,
	@Schema(description = "현재 예약 인원", example = "2")
	int reservedCount,
	@Schema(description = "남은 예약 가능 인원", example = "3")
	int availableCount,
	@Schema(description = "예약 가능 여부", example = "true")
	boolean available
) {

	public static ReservationSlotResponse from(ReservationSlotVO slot) {
		int availableCount = Math.max(slot.getCapacity() - slot.getReservedCount(), 0);
		return new ReservationSlotResponse(
			slot.getId(),
			slot.getServiceTypeId(),
			slot.getServiceTypeName(),
			slot.getDate(),
			slot.getStartTime(),
			slot.getEndTime(),
			slot.getCapacity(),
			slot.getReservedCount(),
			availableCount,
			slot.isActive() && availableCount > 0
		);
	}
}
