package egovframework.healthcenter.dashboard.dto;

import egovframework.healthcenter.dashboard.mapper.HourlyVisitVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시간대별 방문자 수 응답")
public record HourlyVisitResponse(
	@Schema(description = "시간", example = "9")
	Integer hour,
	@Schema(description = "방문자 수", example = "12")
	Integer visitCount
) {

	public static HourlyVisitResponse from(HourlyVisitVO vo) {
		return new HourlyVisitResponse(vo.getHour(), vo.getVisitCount());
	}
}
