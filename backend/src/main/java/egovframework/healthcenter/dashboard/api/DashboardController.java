package egovframework.healthcenter.dashboard.api;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.dashboard.application.DashboardQueryService;
import egovframework.healthcenter.dashboard.dto.DashboardSummaryResponse;
import egovframework.healthcenter.member.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "DashboardController", description = "관리자 대시보드")
public class DashboardController {

	private final DashboardQueryService dashboardQueryService;

	public DashboardController(DashboardQueryService dashboardQueryService) {
		this.dashboardQueryService = dashboardQueryService;
	}

	@GetMapping("/summary")
	@Operation(
		summary = "대시보드 요약",
		description = "관리자가 날짜별 방문자 수, 현재 대기 인원, 평균 대기시간, 노쇼율을 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ResponseEntity<ApiResponse<DashboardSummaryResponse>> findSummary(
			Authentication authentication,
			@RequestParam(required = false) LocalDate date) {
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.failure("AUTH_REQUIRED", "로그인이 필요합니다."));
		}

		try {
			return ResponseEntity.ok(ApiResponse.success(dashboardQueryService.findSummary(principal, date)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.failure("DASHBOARD_INVALID_REQUEST", e.getMessage()));
		}
	}
}
