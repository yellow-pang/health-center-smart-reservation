package egovframework.healthcenter.dashboard.dto;

import java.math.BigDecimal;

import egovframework.healthcenter.dashboard.mapper.VisitTypeRatioVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약/현장 방문 비율 응답")
public record VisitTypeRatioResponse(
	@Schema(description = "전체 방문 수", example = "120")
	Integer totalVisitCount,
	@Schema(description = "예약 방문 수", example = "80")
	Integer reservedVisitCount,
	@Schema(description = "현장 접수 수", example = "40")
	Integer walkInVisitCount,
	@Schema(description = "예약 방문 비율", example = "66.7")
	BigDecimal reservedVisitRatio,
	@Schema(description = "현장 접수 비율", example = "33.3")
	BigDecimal walkInVisitRatio
) {

	public static VisitTypeRatioResponse from(VisitTypeRatioVO vo) {
		return new VisitTypeRatioResponse(
			vo.getTotalVisitCount(),
			vo.getReservedVisitCount(),
			vo.getWalkInVisitCount(),
			vo.getReservedVisitRatio(),
			vo.getWalkInVisitRatio()
		);
	}
}
