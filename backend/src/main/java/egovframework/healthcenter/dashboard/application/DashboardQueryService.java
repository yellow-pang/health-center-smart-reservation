package egovframework.healthcenter.dashboard.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.dashboard.dto.CongestionResponse;
import egovframework.healthcenter.dashboard.dto.DashboardSummaryResponse;
import egovframework.healthcenter.dashboard.dto.HourlyVisitResponse;
import egovframework.healthcenter.dashboard.dto.NoShowRateResponse;
import egovframework.healthcenter.dashboard.dto.ServiceWaitTimeResponse;
import egovframework.healthcenter.dashboard.dto.VisitTypeRatioResponse;
import egovframework.healthcenter.dashboard.mapper.CongestionVO;
import egovframework.healthcenter.dashboard.mapper.DashboardMapper;
import egovframework.healthcenter.dashboard.mapper.DashboardSummaryVO;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.security.MemberPrincipal;

@Service
public class DashboardQueryService {

	private static final int DEFAULT_PROCESSING_MINUTES = 5;

	private final DashboardMapper dashboardMapper;

	public DashboardQueryService(DashboardMapper dashboardMapper) {
		this.dashboardMapper = dashboardMapper;
	}

	@Transactional(readOnly = true)
	public DashboardSummaryResponse findSummary(MemberPrincipal principal, LocalDate date) {
		validateAdmin(principal);
		LocalDate targetDate = date == null ? LocalDate.now() : date;
		DashboardSummaryVO summary = dashboardMapper.selectSummary(principal.healthCenterId(), targetDate);
		return DashboardSummaryResponse.from(summary);
	}

	@Transactional(readOnly = true)
	public List<HourlyVisitResponse> findHourlyVisits(MemberPrincipal principal, LocalDate date) {
		validateAdmin(principal);
		LocalDate targetDate = resolveDate(date);
		return dashboardMapper.selectHourlyVisits(principal.healthCenterId(), targetDate)
			.stream()
			.map(HourlyVisitResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<ServiceWaitTimeResponse> findServiceWaitTimes(MemberPrincipal principal, LocalDate date) {
		validateAdmin(principal);
		LocalDate targetDate = resolveDate(date);
		return dashboardMapper.selectServiceWaitTimes(principal.healthCenterId(), targetDate)
			.stream()
			.map(ServiceWaitTimeResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public VisitTypeRatioResponse findVisitTypeRatio(MemberPrincipal principal, LocalDate date) {
		validateAdmin(principal);
		LocalDate targetDate = resolveDate(date);
		return VisitTypeRatioResponse.from(dashboardMapper.selectVisitTypeRatio(principal.healthCenterId(), targetDate));
	}

	@Transactional(readOnly = true)
	public NoShowRateResponse findNoShowRate(MemberPrincipal principal, LocalDate date) {
		validateAdmin(principal);
		LocalDate targetDate = resolveDate(date);
		return NoShowRateResponse.from(dashboardMapper.selectNoShowRate(principal.healthCenterId(), targetDate));
	}

	@Transactional(readOnly = true)
	public List<CongestionResponse> findCurrentCongestion(Long healthCenterId) {
		if (healthCenterId == null) {
			throw new BusinessException(ErrorCode.DASHBOARD_INVALID_REQUEST, "보건소 ID를 입력하세요.");
		}
		return dashboardMapper.selectCurrentCongestion(healthCenterId)
			.stream()
			.map(this::toCongestionResponse)
			.toList();
	}

	private CongestionResponse toCongestionResponse(CongestionVO congestion) {
		int waitingCount = safeInt(congestion.getWaitingCount());
		int averageProcessingMinutes = congestion.getAverageProcessingMinutes() == null
			? DEFAULT_PROCESSING_MINUTES
			: Math.max(1, congestion.getAverageProcessingMinutes());
		int estimatedWaitMinutes = waitingCount * averageProcessingMinutes;
		CongestionLevel level = CongestionLevel.resolve(waitingCount, estimatedWaitMinutes);
		return new CongestionResponse(
			congestion.getServiceTypeId(),
			congestion.getServiceTypeName(),
			waitingCount,
			estimatedWaitMinutes,
			level.name(),
			level.label
		);
	}

	private int safeInt(Integer value) {
		return value == null ? 0 : value;
	}

	private LocalDate resolveDate(LocalDate date) {
		return date == null ? LocalDate.now() : date;
	}

	private void validateAdmin(MemberPrincipal principal) {
		if (principal == null || principal.memberId() == null) {
			throw new BusinessException(ErrorCode.AUTH_REQUIRED);
		}
		if (principal.healthCenterId() == null) {
			throw new BusinessException(ErrorCode.DASHBOARD_INVALID_REQUEST, "대시보드를 조회할 보건소 정보가 없습니다.");
		}
		if (principal.role() != MemberRole.ADMIN) {
			throw new BusinessException(ErrorCode.DASHBOARD_FORBIDDEN);
		}
	}

	private enum CongestionLevel {
		LOW("여유"),
		NORMAL("보통"),
		HIGH("혼잡");

		private final String label;

		CongestionLevel(String label) {
			this.label = label;
		}

		private static CongestionLevel resolve(int waitingCount, int estimatedWaitMinutes) {
			if (waitingCount >= 16 || estimatedWaitMinutes >= 31) {
				return HIGH;
			}
			if (waitingCount >= 6 || estimatedWaitMinutes >= 11) {
				return NORMAL;
			}
			return LOW;
		}
	}
}
