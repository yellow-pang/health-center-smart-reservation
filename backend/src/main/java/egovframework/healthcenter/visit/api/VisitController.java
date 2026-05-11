package egovframework.healthcenter.visit.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.visit.application.VisitCommandService;
import egovframework.healthcenter.visit.dto.VisitCheckInRequest;
import egovframework.healthcenter.visit.dto.VisitCheckInResponse;
import egovframework.healthcenter.visit.dto.VisitWalkInRequest;
import egovframework.healthcenter.visit.dto.VisitWalkInResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/visits")
@Tag(name = "VisitController", description = "방문")
public class VisitController {

	private final VisitCommandService visitCommandService;

	public VisitController(VisitCommandService visitCommandService) {
		this.visitCommandService = visitCommandService;
	}

	@PostMapping("/check-in")
	@Operation(
		summary = "예약자 체크인",
		description = "직원 또는 관리자가 예약번호로 방문 체크인하고 대기번호를 발급한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<VisitCheckInResponse>> checkIn(
			Authentication authentication,
			@RequestBody VisitCheckInRequest request) {
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.failure("AUTH_REQUIRED", "로그인이 필요합니다."));
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(visitCommandService.checkIn(principal, request)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(resolveStatus(e.getMessage()))
				.body(ApiResponse.failure(resolveErrorCode(e.getMessage()), e.getMessage()));
		}
	}

	@PostMapping("/walk-in")
	@Operation(
		summary = "현장 접수",
		description = "직원 또는 관리자가 예약 없이 방문자를 접수하고 대기번호를 발급한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<VisitWalkInResponse>> walkIn(
			Authentication authentication,
			@RequestBody VisitWalkInRequest request) {
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.failure("AUTH_REQUIRED", "로그인이 필요합니다."));
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(visitCommandService.walkIn(principal, request)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(resolveStatus(e.getMessage()))
				.body(ApiResponse.failure(resolveErrorCode(e.getMessage()), e.getMessage()));
		}
	}

	private String resolveErrorCode(String message) {
		if (message != null && message.contains("권한")) {
			return "FORBIDDEN";
		}
		if (message != null && message.contains("업무 유형")) {
			return "SERVICE_TYPE_NOT_FOUND";
		}
		if (message != null && message.contains("찾을 수 없습니다")) {
			return "RESERVATION_NOT_FOUND";
		}
		if (message != null && message.contains("이미 체크인")) {
			return "ALREADY_CHECKED_IN";
		}
		return "VISIT_INVALID_REQUEST";
	}

	private HttpStatus resolveStatus(String message) {
		if (message != null && message.contains("권한")) {
			return HttpStatus.FORBIDDEN;
		}
		if (message != null && message.contains("찾을 수 없습니다")) {
			return HttpStatus.NOT_FOUND;
		}
		if (message != null && message.contains("이미 체크인")) {
			return HttpStatus.CONFLICT;
		}
		return HttpStatus.BAD_REQUEST;
	}
}
