package egovframework.healthcenter.office.dto;

import egovframework.healthcenter.office.mapper.StaffVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직원 응답")
public record StaffResponse(
	@Schema(description = "회원 ID", example = "2")
	Long id,
	@Schema(description = "소속 보건소 ID", example = "1")
	Long healthCenterId,
	@Schema(description = "이메일", example = "staff@test.com")
	String email,
	@Schema(description = "이름", example = "보건소 직원")
	String name,
	@Schema(description = "전화번호", example = "010-0000-0002")
	String phone,
	@Schema(description = "역할", example = "STAFF")
	String role,
	@Schema(description = "사용 여부", example = "true")
	boolean active
) {

	public static StaffResponse from(StaffVO staff) {
		return new StaffResponse(
			staff.getId(),
			staff.getHealthCenterId(),
			staff.getEmail(),
			staff.getName(),
			staff.getPhone(),
			staff.getRole(),
			staff.isActive()
		);
	}
}
