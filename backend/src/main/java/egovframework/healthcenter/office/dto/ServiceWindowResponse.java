package egovframework.healthcenter.office.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "창구 응답")
public record ServiceWindowResponse(
	@Schema(description = "창구 ID", example = "1")
	Long id,
	@Schema(description = "보건소 ID", example = "1")
	Long healthCenterId,
	@Schema(description = "창구 번호", example = "1")
	Integer windowNumber,
	@Schema(description = "창구명", example = "1번 창구")
	String name,
	@Schema(description = "창구 상태", example = "OPEN")
	String status,
	@Schema(description = "사용 여부", example = "true")
	boolean active,
	@Schema(description = "담당 업무 유형 목록")
	List<ServiceTypeResponse> serviceTypes
) {
}
