package egovframework.healthcenter.reservation.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.reservation.application.ReservationCommandService;
import egovframework.healthcenter.reservation.application.ReservationQueryService;
import egovframework.healthcenter.reservation.dto.ReservationCreateRequest;
import egovframework.healthcenter.reservation.dto.ReservationCreateResponse;
import egovframework.healthcenter.reservation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "ReservationController", description = "예약")
public class ReservationController {

	private final ReservationCommandService reservationCommandService;
	private final ReservationQueryService reservationQueryService;

	public ReservationController(
			ReservationCommandService reservationCommandService,
			ReservationQueryService reservationQueryService) {
		this.reservationCommandService = reservationCommandService;
		this.reservationQueryService = reservationQueryService;
	}

	@GetMapping("/me")
	@Operation(
		summary = "내 예약 조회",
		description = "현재 로그인한 사용자의 예약 목록을 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<List<ReservationResponse>>> findMyReservations(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.failure("AUTH_REQUIRED", "로그인이 필요합니다."));
		}

		return ResponseEntity.ok(ApiResponse.success(reservationQueryService.findMyReservations(principal)));
	}

	@GetMapping("/{reservationId}")
	@Operation(
		summary = "예약 상세 조회",
		description = "예약자 본인 또는 같은 보건소의 직원/관리자가 예약 상세를 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ReservationResponse>> findReservationDetail(
			Authentication authentication,
			@PathVariable Long reservationId) {
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.failure("AUTH_REQUIRED", "로그인이 필요합니다."));
		}

		try {
			return ResponseEntity.ok(ApiResponse.success(
				reservationQueryService.findReservationDetail(principal, reservationId)
			));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(resolveStatus(e.getMessage()))
				.body(ApiResponse.failure(resolveErrorCode(e.getMessage()), e.getMessage()));
		}
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
		if (message != null && message.contains("권한")) {
			return "FORBIDDEN";
		}
		if (message != null && message.contains("예약 정보를 찾을 수 없습니다")) {
			return "RESERVATION_NOT_FOUND";
		}
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

	private HttpStatus resolveStatus(String message) {
		if (message != null && message.contains("권한")) {
			return HttpStatus.FORBIDDEN;
		}
		if (message != null && message.contains("예약 정보를 찾을 수 없습니다")) {
			return HttpStatus.NOT_FOUND;
		}
		return HttpStatus.BAD_REQUEST;
	}
}
