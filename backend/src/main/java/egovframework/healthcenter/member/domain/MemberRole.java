package egovframework.healthcenter.member.domain;

public enum MemberRole {
	CITIZEN,
	GUARDIAN,
	STAFF,
	ADMIN;

	public String authority() {
		return "ROLE_" + name();
	}
}
