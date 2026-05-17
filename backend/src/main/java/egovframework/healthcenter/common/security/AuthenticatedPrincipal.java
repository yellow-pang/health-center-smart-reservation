package egovframework.healthcenter.common.security;

import org.springframework.security.core.Authentication;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.security.MemberPrincipal;

public final class AuthenticatedPrincipal {

	private AuthenticatedPrincipal() {
	}

	public static MemberPrincipal require(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			throw new BusinessException(ErrorCode.AUTH_REQUIRED);
		}
		return principal;
	}
}
