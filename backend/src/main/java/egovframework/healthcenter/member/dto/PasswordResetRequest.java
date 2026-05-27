package egovframework.healthcenter.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetRequest(
	@Schema(description = "가입 이메일", example = "citizen@example.com")
	String email,
	@Schema(description = "휴대폰 번호", example = "010-0000-0001")
	String phone
) {
}
