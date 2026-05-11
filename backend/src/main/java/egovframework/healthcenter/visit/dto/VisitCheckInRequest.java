package egovframework.healthcenter.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약자 체크인 요청")
public record VisitCheckInRequest(
	@Schema(description = "예약번호", example = "RSV-SWAGGER-CHECKIN-001")
	String reservationNo
) {
}
