package egovframework.healthcenter.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetConfirmResponse(
	@Schema(description = "비밀번호 변경 완료 여부", example = "true")
	boolean completed
) {
}
