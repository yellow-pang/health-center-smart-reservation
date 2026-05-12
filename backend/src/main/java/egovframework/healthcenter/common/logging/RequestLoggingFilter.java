package egovframework.healthcenter.common.logging;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import egovframework.healthcenter.member.security.MemberPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		long startedAt = System.currentTimeMillis();
		String traceId = resolveTraceId(request);
		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		MDC.put(RequestTraceConstants.TRACE_ID, traceId);
		response.setHeader(RequestTraceConstants.TRACE_ID_HEADER, traceId);

		try {
			filterChain.doFilter(wrappedRequest, response);
		} catch (IOException | ServletException | RuntimeException e) {
			log.warn(
				"event=http.request.exception traceId={} method={} uri={} query={} memberId={} role={} exception={}",
				traceId,
				request.getMethod(),
				request.getRequestURI(),
				safeQueryString(request),
				currentMemberId(),
				currentRole(),
				e.getClass().getSimpleName()
			);
			throw e;
		} finally {
			long elapsedMs = System.currentTimeMillis() - startedAt;
			log.info(
				"event=http.request.completed traceId={} method={} uri={} query={} status={} latencyMs={} memberId={} role={} healthCenterId={}",
				traceId,
				request.getMethod(),
				request.getRequestURI(),
				safeQueryString(request),
				response.getStatus(),
				elapsedMs,
				currentMemberId(),
				currentRole(),
				currentHealthCenterId()
			);
			MDC.remove(RequestTraceConstants.TRACE_ID);
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri.startsWith("/css/")
			|| uri.startsWith("/js/")
			|| uri.startsWith("/images/")
			|| uri.startsWith("/static/")
			|| uri.equals("/favicon.ico");
	}

	private String resolveTraceId(HttpServletRequest request) {
		String headerTraceId = request.getHeader(RequestTraceConstants.TRACE_ID_HEADER);
		if (headerTraceId != null && !headerTraceId.isBlank()) {
			return sanitizeTraceId(headerTraceId);
		}
		return UUID.randomUUID().toString();
	}

	private String sanitizeTraceId(String traceId) {
		String value = traceId.trim();
		if (value.length() > 64) {
			return value.substring(0, 64);
		}
		return value;
	}

	private String safeQueryString(HttpServletRequest request) {
		String queryString = request.getQueryString();
		if (queryString == null || queryString.isBlank()) {
			return "-";
		}
		String masked = queryString.replaceAll("(?i)([^&=]*(password|token|phone)[^&=]*)=[^&]*", "$1=***");
		return masked.length() > 200 ? masked.substring(0, 200) : masked;
	}

	private String currentMemberId() {
		return currentPrincipal()
			.map(MemberPrincipal::memberId)
			.map(String::valueOf)
			.orElse("-");
	}

	private String currentHealthCenterId() {
		return currentPrincipal()
			.map(MemberPrincipal::healthCenterId)
			.map(String::valueOf)
			.orElse("-");
	}

	private String currentRole() {
		return currentPrincipal()
			.map(MemberPrincipal::role)
			.map(String::valueOf)
			.orElse("-");
	}

	private Optional<MemberPrincipal> currentPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
			return Optional.empty();
		}
		return Optional.of(principal);
	}
}
