package egovframework.healthcenter.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
	@Schema(description = "Access Token")
	String accessToken,
	@Schema(description = "Refresh Token")
	String refreshToken,
	@Schema(description = "회원 정보")
	MemberResponse member
) {
}
