package egovframework.healthcenter.member.dto;

public record SocialSignupRequest(
	String completionToken,
	String email,
	String name
) {
}
