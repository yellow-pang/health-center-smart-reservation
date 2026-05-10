package egovframework.healthcenter.reservation.dto;

import egovframework.healthcenter.reservation.mapper.ReservationVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 신청 응답")
public record ReservationCreateResponse(
	@Schema(description = "예약 ID", example = "100")
	Long reservationId,
	@Schema(description = "예약번호", example = "RSV-20260510-0001")
	String reservationNo,
	@Schema(description = "예약 상태", example = "RESERVED")
	String status
) {

	public static ReservationCreateResponse from(ReservationVO reservation) {
		return new ReservationCreateResponse(
			reservation.getId(),
			reservation.getReservationNo(),
			reservation.getStatus()
		);
	}
}
