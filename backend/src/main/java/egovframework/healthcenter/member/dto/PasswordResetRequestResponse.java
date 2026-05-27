package egovframework.healthcenter.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetRequestResponse(
	@Schema(description = "요청 접수 여부", example = "true")
	boolean accepted,
	@Schema(description = "개발 환경 확인용 재설정 토큰. 운영 환경에서는 null로 둔다.")
	String developmentResetToken
) {
}
