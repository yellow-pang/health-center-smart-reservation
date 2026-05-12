package egovframework.healthcenter.dashboard.dto;

import java.math.BigDecimal;

import egovframework.healthcenter.dashboard.mapper.NoShowRateVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "노쇼율 응답")
public record NoShowRateResponse(
	@Schema(description = "노쇼율 계산 대상 예약 수", example = "100")
	Integer targetReservationCount,
	@Schema(description = "노쇼 예약 수", example = "8")
	Integer noShowReservationCount,
	@Schema(description = "노쇼율", example = "8.0")
	BigDecimal noShowRate
) {

	public static NoShowRateResponse from(NoShowRateVO vo) {
		return new NoShowRateResponse(
			vo.getTargetReservationCount(),
			vo.getNoShowReservationCount(),
			vo.getNoShowRate()
		);
	}
}
