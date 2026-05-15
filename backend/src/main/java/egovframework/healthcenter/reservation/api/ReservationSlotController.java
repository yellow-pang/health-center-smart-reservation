package egovframework.healthcenter.reservation.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.reservation.application.ReservationSlotCommandService;
import egovframework.healthcenter.reservation.application.ReservationSlotQueryService;
import egovframework.healthcenter.reservation.dto.ReservationSlotCreateRequest;
import egovframework.healthcenter.reservation.dto.ReservationSlotResponse;
import egovframework.healthcenter.reservation.dto.ReservationSlotUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "ReservationSlotController", description = "예약 가능 시간")
public class ReservationSlotController {

	private final ReservationSlotQueryService reservationSlotQueryService;
	private final ReservationSlotCommandService reservationSlotCommandService;

	public ReservationSlotController(
			ReservationSlotQueryService reservationSlotQueryService,
			ReservationSlotCommandService reservationSlotCommandService) {
		this.reservationSlotQueryService = reservationSlotQueryService;
		this.reservationSlotCommandService = reservationSlotCommandService;
	}

	@GetMapping("/reservation-slots")
	@Operation(
		summary = "예약 가능 시간 조회",
		description = "업무 유형과 날짜 기준으로 활성 예약 슬롯을 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<List<ReservationSlotResponse>>> findReservationSlots(
			@RequestParam Long serviceTypeId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		try {
			return ResponseEntity.ok(ApiResponse.success(
				reservationSlotQueryService.findAvailableSlots(serviceTypeId, date)
			));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.failure("RESERVATION_SLOT_INVALID_REQUEST", e.getMessage()));
		}
	}

	@PostMapping("/admin/reservation-slots")
	@Operation(
		summary = "예약 슬롯 생성",
		description = "관리자가 업무 유형별 예약 가능 시간과 정원을 생성한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ReservationSlotResponse>> createReservationSlot(
			@RequestBody ReservationSlotCreateRequest request) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(reservationSlotCommandService.createSlot(request)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.failure("RESERVATION_SLOT_INVALID_REQUEST", e.getMessage()));
		}
	}

	@PutMapping("/admin/reservation-slots/{id}")
	@Operation(
		summary = "예약 슬롯 수정",
		description = "관리자가 업무 유형, 날짜, 시간, 정원, 사용 여부를 수정한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ReservationSlotResponse>> updateReservationSlot(
			@PathVariable Long id,
			@RequestBody ReservationSlotUpdateRequest request) {
		try {
			return ResponseEntity.ok(ApiResponse.success(reservationSlotCommandService.updateSlot(id, request)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.failure("RESERVATION_SLOT_INVALID_REQUEST", e.getMessage()));
		}
	}

	@PatchMapping("/admin/reservation-slots/{id}/deactivate")
	@Operation(
		summary = "예약 슬롯 비활성화",
		description = "관리자가 예약 슬롯을 삭제하지 않고 비활성화한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<ReservationSlotResponse>> deactivateReservationSlot(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(ApiResponse.success(reservationSlotCommandService.deactivateSlot(id)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.failure("RESERVATION_SLOT_INVALID_REQUEST", e.getMessage()));
		}
	}
}
