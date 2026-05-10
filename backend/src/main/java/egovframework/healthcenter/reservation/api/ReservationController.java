package egovframework.healthcenter.reservation.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.application.ReservationCommandService;
import egovframework.healthcenter.reservation.dto.ReservationCreateRequest;
import egovframework.healthcenter.reservation.dto.ReservationCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "ReservationController", description = "예약")
public class ReservationController {

	private final ReservationCommandService reservationCommandService;

	public ReservationController(ReservationCommandService reservationCommandService) {
		this.reservationCommandService = reservationCommandService;
	}

	@PostMapping
	@Operation(
		summary = "예약 신청",
		description = "로그인 사용자가 예약 슬롯을 선택해 예약을 신청한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ReservationCreateResponse>> createReservation(
			Authentication authentication,
			@RequestBody ReservationCreateRequest request) {
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.failure("AUTH_REQUIRED", "로그인이 필요합니다."));
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(reservationCommandService.createReservation(principal, request)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.failure(resolveErrorCode(e.getMessage()), e.getMessage()));
		}
	}

	private String resolveErrorCode(String message) {
		if (message != null && message.contains("마감")) {
			return "RESERVATION_SLOT_FULL";
		}
		if (message != null && message.contains("이미 예약")) {
			return "RESERVATION_DUPLICATED";
		}
		if (message != null && message.contains("슬롯")) {
			return "RESERVATION_SLOT_NOT_FOUND";
		}
		return "RESERVATION_INVALID_REQUEST";
	}
}
