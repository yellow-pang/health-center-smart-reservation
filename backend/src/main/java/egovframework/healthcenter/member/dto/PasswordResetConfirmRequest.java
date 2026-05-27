package egovframework.healthcenter.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetConfirmRequest(
	@Schema(description = "비밀번호 재설정 토큰")
	String resetToken,
	@Schema(description = "새 비밀번호", example = "newPassword1234")
	String newPassword,
	@Schema(description = "새 비밀번호 확인", example = "newPassword1234")
	String newPasswordConfirm
) {
}
