package egovframework.healthcenter.dashboard.dto;

import java.math.BigDecimal;

import egovframework.healthcenter.dashboard.mapper.DashboardSummaryVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 요약 응답")
public record DashboardSummaryResponse(
	@Schema(description = "오늘 방문자 수", example = "120")
	Integer todayVisitCount,
	@Schema(description = "현재 대기 인원", example = "18")
	Integer currentWaitingCount,
	@Schema(description = "평균 대기시간", example = "24")
	Integer averageWaitMinutes,
	@Schema(description = "노쇼율", example = "8.5")
	BigDecimal noShowRate
) {

	public static DashboardSummaryResponse from(DashboardSummaryVO summary) {
		return new DashboardSummaryResponse(
			summary.getTodayVisitCount(),
			summary.getCurrentWaitingCount(),
			summary.getAverageWaitMinutes(),
			summary.getNoShowRate()
		);
	}
}
