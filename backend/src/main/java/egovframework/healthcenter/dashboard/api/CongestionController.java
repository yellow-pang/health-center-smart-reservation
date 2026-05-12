package egovframework.healthcenter.dashboard.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.dashboard.application.DashboardQueryService;
import egovframework.healthcenter.dashboard.dto.CongestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/congestion")
@Tag(name = "CongestionController", description = "현재 혼잡도")
public class CongestionController {

	private static final long DEFAULT_HEALTH_CENTER_ID = 1L;

	private final DashboardQueryService dashboardQueryService;

	public CongestionController(DashboardQueryService dashboardQueryService) {
		this.dashboardQueryService = dashboardQueryService;
	}

	@GetMapping("/current")
	@Operation(summary = "현재 혼잡도", description = "업무 유형별 현재 대기 인원과 예상 대기시간 기반 혼잡도를 조회한다.")
	public ApiResponse<List<CongestionResponse>> findCurrentCongestion(
			@RequestParam(required = false) Long healthCenterId) {
		Long targetHealthCenterId = healthCenterId == null ? DEFAULT_HEALTH_CENTER_ID : healthCenterId;
		return ApiResponse.success(dashboardQueryService.findCurrentCongestion(targetHealthCenterId));
	}
}
