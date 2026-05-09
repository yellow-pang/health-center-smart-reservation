package egovframework.healthcenter.member.security;

import egovframework.healthcenter.member.domain.MemberRole;

public record MemberPrincipal(
	Long memberId,
	Long healthCenterId,
	String email,
	String name,
	MemberRole role
) {

	public String authority() {
		return role.authority();
	}
}
