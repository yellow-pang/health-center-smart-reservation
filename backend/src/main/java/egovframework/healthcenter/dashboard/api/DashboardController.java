package egovframework.healthcenter.dashboard.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.common.security.AuthenticatedPrincipal;
import egovframework.healthcenter.dashboard.application.DashboardQueryService;
import egovframework.healthcenter.dashboard.dto.DashboardSummaryResponse;
import egovframework.healthcenter.dashboard.dto.HourlyVisitResponse;
import egovframework.healthcenter.dashboard.dto.NoShowRateResponse;
import egovframework.healthcenter.dashboard.dto.ServiceWaitTimeResponse;
import egovframework.healthcenter.dashboard.dto.VisitTypeRatioResponse;
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
	public ApiResponse<DashboardSummaryResponse> findSummary(
			Authentication authentication,
			@RequestParam(required = false) LocalDate date) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ApiResponse.success(dashboardQueryService.findSummary(principal, date));
	}

	@GetMapping("/hourly-visits")
	@Operation(
		summary = "시간대별 방문자 수",
		description = "관리자가 날짜별 시간대 방문자 수를 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<List<HourlyVisitResponse>> findHourlyVisits(
			Authentication authentication,
			@RequestParam(required = false) LocalDate date) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ApiResponse.success(dashboardQueryService.findHourlyVisits(principal, date));
	}

	@GetMapping("/service-wait-times")
	@Operation(
		summary = "업무별 평균 대기시간",
		description = "관리자가 날짜별 업무 유형 평균 대기시간을 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<List<ServiceWaitTimeResponse>> findServiceWaitTimes(
			Authentication authentication,
			@RequestParam(required = false) LocalDate date) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ApiResponse.success(dashboardQueryService.findServiceWaitTimes(principal, date));
	}

	@GetMapping("/visit-type-ratio")
	@Operation(
		summary = "예약/현장 방문 비율",
		description = "관리자가 날짜별 예약 방문과 현장 접수 비율을 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<VisitTypeRatioResponse> findVisitTypeRatio(
			Authentication authentication,
			@RequestParam(required = false) LocalDate date) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ApiResponse.success(dashboardQueryService.findVisitTypeRatio(principal, date));
	}

	@GetMapping("/no-show-rate")
	@Operation(
		summary = "노쇼율",
		description = "관리자가 날짜별 취소 예약 제외 기준 노쇼율을 조회한다.",
		security = {@SecurityRequirement(name = "Authorization")}
	)
	public ApiResponse<NoShowRateResponse> findNoShowRate(
			Authentication authentication,
			@RequestParam(required = false) LocalDate date) {
		MemberPrincipal principal = AuthenticatedPrincipal.require(authentication);
		return ApiResponse.success(dashboardQueryService.findNoShowRate(principal, date));
	}
}
