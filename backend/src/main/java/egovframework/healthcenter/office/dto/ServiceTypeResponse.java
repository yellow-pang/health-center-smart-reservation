package egovframework.healthcenter.office.dto;

import egovframework.healthcenter.office.mapper.ServiceTypeVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업무 유형 응답")
public record ServiceTypeResponse(
	@Schema(description = "업무 유형 ID", example = "1")
	Long id,
	@Schema(description = "보건소 ID", example = "1")
	Long healthCenterId,
	@Schema(description = "업무 코드", example = "VACCINATION")
	String code,
	@Schema(description = "업무명", example = "예방접종")
	String name,
	@Schema(description = "업무 설명", example = "예방접종 예약 및 현장 접수")
	String description,
	@Schema(description = "기본 예약 가능 인원", example = "5")
	int defaultCapacity,
	@Schema(description = "사용 여부", example = "true")
	boolean active
) {

	public static ServiceTypeResponse from(ServiceTypeVO serviceType) {
		return new ServiceTypeResponse(
			serviceType.getId(),
			serviceType.getHealthCenterId(),
			serviceType.getCode(),
			serviceType.getName(),
			serviceType.getDescription(),
			serviceType.getDefaultCapacity(),
			serviceType.isActive()
		);
	}
}
