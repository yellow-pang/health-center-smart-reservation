package egovframework.healthcenter.office.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직원 수정 요청")
public record StaffUpdateRequest(
	@Schema(description = "이름", example = "보건소 직원")
	String name,
	@Schema(description = "전화번호", example = "010-0000-0002")
	String phone,
	@Schema(description = "사용 여부", example = "true")
	Boolean active
) {
}
