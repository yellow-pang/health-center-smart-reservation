package egovframework.healthcenter.visit.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.common.security.AuthenticatedPrincipal;
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
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success(visitCommandService.checkIn(principal, request)));
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
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.success(visitCommandService.walkIn(principal, request)));
	}
}
