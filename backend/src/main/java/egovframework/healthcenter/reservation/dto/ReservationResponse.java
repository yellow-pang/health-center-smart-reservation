package egovframework.healthcenter.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import egovframework.healthcenter.reservation.mapper.ReservationVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 조회 응답")
public record ReservationResponse(
	@Schema(description = "예약 ID", example = "100")
	Long reservationId,
	@Schema(description = "예약번호", example = "RSV-20260510-0001")
	String reservationNo,
	@Schema(description = "업무 유형 ID", example = "1")
	Long serviceTypeId,
	@Schema(description = "업무 유형명", example = "예방접종")
	String serviceTypeName,
	@Schema(description = "예약 슬롯 ID", example = "10")
	Long reservationSlotId,
	@Schema(description = "예약일", example = "2026-05-10")
	LocalDate date,
	@Schema(description = "시작 시간", example = "09:00")
	LocalTime startTime,
	@Schema(description = "종료 시간", example = "09:30")
	LocalTime endTime,
	@Schema(description = "방문자 이름", example = "홍길동")
	String visitorName,
	@Schema(description = "방문자 연락처", example = "010-1234-5678")
	String visitorPhone,
	@Schema(description = "예약 상태", example = "RESERVED")
	String status,
	@Schema(description = "예약 생성 시각")
	LocalDateTime reservedAt
) {

	public static ReservationResponse from(ReservationVO reservation) {
		return new ReservationResponse(
			reservation.getId(),
			reservation.getReservationNo(),
			reservation.getServiceTypeId(),
			reservation.getServiceTypeName(),
			reservation.getReservationSlotId(),
			reservation.getSlotDate(),
			reservation.getStartTime(),
			reservation.getEndTime(),
			reservation.getVisitorName(),
			reservation.getVisitorPhone(),
			reservation.getStatus(),
			reservation.getReservedAt()
		);
	}
}
