package egovframework.healthcenter.common.logging;

import org.slf4j.MDC;

import egovframework.healthcenter.member.security.MemberPrincipal;

public final class AuditLogSupport {

	private AuditLogSupport() {
	}

	public static String traceId() {
		String traceId = MDC.get(RequestTraceConstants.TRACE_ID);
		return traceId == null || traceId.isBlank() ? "-" : traceId;
	}

	public static String memberId(MemberPrincipal principal) {
		return principal == null || principal.memberId() == null ? "-" : String.valueOf(principal.memberId());
	}

	public static String healthCenterId(MemberPrincipal principal) {
		return principal == null || principal.healthCenterId() == null ? "-" : String.valueOf(principal.healthCenterId());
	}

	public static String role(MemberPrincipal principal) {
		return principal == null || principal.role() == null ? "-" : principal.role().name();
	}
}
