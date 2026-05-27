package egovframework.healthcenter.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FindIdResponse(
	@Schema(description = "가입 이메일 확인 여부", example = "true")
	boolean found,
	@Schema(description = "마스킹된 가입 이메일", example = "ci***@example.com")
	String maskedEmail
) {
}
