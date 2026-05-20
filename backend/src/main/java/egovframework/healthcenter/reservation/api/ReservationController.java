package egovframework.healthcenter.reservation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.common.security.AuthenticatedPrincipal;
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
	public ApiResponse<List<ReservationResponse>> findMyReservations(Authentication authentication) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ApiResponse.success(reservationQueryService.findMyReservations(principal));
	}

	@GetMapping("/{reservationId}")
	@Operation(
		summary = "예약 상세 조회",
		description = "예약자 본인 또는 같은 보건소의 직원/관리자가 예약 상세를 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<ReservationResponse> findReservationDetail(
			Authentication authentication,
			@PathVariable Long reservationId) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ApiResponse.success(reservationQueryService.findReservationDetail(principal, reservationId));
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
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ResponseEntity.status(201)
			.body(ApiResponse.success(reservationCommandService.createReservation(principal, request)));
	}

	@DeleteMapping("/{reservationId}")
	@Operation(
		summary = "예약 취소",
		description = "예약자 본인 또는 같은 보건소 직원/관리자가 방문 1시간 전까지 예약을 취소한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<Void>> cancelReservation(
			Authentication authentication,
			@PathVariable Long reservationId) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		reservationCommandService.cancelReservation(principal, reservationId);
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
