package egovframework.healthcenter.member.dto;

import egovframework.healthcenter.member.mapper.MemberVO;
import egovframework.healthcenter.member.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 응답")
public record MemberResponse(
	@Schema(description = "회원 ID", example = "1")
	Long id,
	@Schema(description = "소속 보건소 ID", example = "1")
	Long healthCenterId,
	@Schema(description = "이메일", example = "staff@test.com")
	String email,
	@Schema(description = "이름", example = "보건소 직원")
	String name,
	@Schema(description = "역할", example = "STAFF")
	String role
) {

	public static MemberResponse from(MemberVO member) {
		return new MemberResponse(
			member.getId(),
			member.getHealthCenterId(),
			member.getEmail(),
			member.getName(),
			member.getRole()
		);
	}

	public static MemberResponse from(MemberPrincipal principal) {
		return new MemberResponse(
			principal.memberId(),
			principal.healthCenterId(),
			principal.email(),
			principal.name(),
			principal.role().name()
		);
	}
}
