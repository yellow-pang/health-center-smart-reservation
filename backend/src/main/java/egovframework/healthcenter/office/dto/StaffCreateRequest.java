package egovframework.healthcenter.office.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직원 생성 요청")
public record StaffCreateRequest(
	@Schema(description = "이메일", example = "new.staff@test.com")
	String email,
	@Schema(description = "초기 비밀번호", example = "password1234")
	String password,
	@Schema(description = "이름", example = "신규 직원")
	String name,
	@Schema(description = "전화번호", example = "010-1000-2000")
	String phone
) {
}
