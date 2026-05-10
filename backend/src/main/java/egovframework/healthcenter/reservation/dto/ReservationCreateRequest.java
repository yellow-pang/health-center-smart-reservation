package egovframework.healthcenter.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 신청 요청")
public record ReservationCreateRequest(
	@Schema(description = "업무 유형 ID", example = "1")
	Long serviceTypeId,
	@Schema(description = "예약 슬롯 ID", example = "10")
	Long reservationSlotId,
	@Schema(description = "방문자 이름", example = "홍길동")
	String visitorName,
	@Schema(description = "방문자 연락처", example = "010-1234-5678")
	String visitorPhone
) {
}
